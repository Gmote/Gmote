/**
 * Copyright 2009 Marc Stogaitis and Mimi Sun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gmote.server;

import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.gmote.common.FileInfo;
import org.gmote.common.MimeTypeResolver;
import org.gmote.common.FileInfo.FileType;
import org.gmote.server.settings.BaseMediaPaths;
import org.gmote.server.settings.SupportedFiletypeSettings;

import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * A server that responds to HTTP requests. It doesn't listen for connections
 * like a typical server as we instead share the port used by GmoteServer.
 * TcpConnection.java will handle connection routing between HTTP packets and
 * Gmote java packets. It will call this class when it notices an HTTP
 * connection.
 * 
 * @author Marc Stogaitis
 */
public class GmoteHttpServer {
  private static final Logger LOGGER = Logger.getLogger(GmoteHttpServer.class.getName());

  private static final int HTTP_OK = 200;
  private static final int HTTP_NOT_FOUND = 404;

  private Socket connectionSocket;
  
  public GmoteHttpServer(Socket connectionSocket) {
    this.connectionSocket = connectionSocket;
  }
  
  public void handleHttpRequestAsync(List<String> latestSessionIds) {
    HttpConnectionHandler conHandler = new HttpConnectionHandler(latestSessionIds);
    new Thread(conHandler).start();
  }

  /**
   * 
   * @param latestSessionIds
   *          List of the last 5 session ids that we have seen. We keep more
   *          than once since there are cases where the client could request a
   *          song from the media player, re-connect, and then seek to furthur
   *          in the song, which would cause the media player to do an http
   *          request with the old session id.
   * @throws InterruptedException 
   * @throws ImageFormatException 
   */
  private void handleHttpRequest(List<String> latestSessionIds) throws ImageFormatException, InterruptedException {
    
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
      List<String> header = extractHeader(reader);
      String requestedUrl = extractFile(header.get(0));
      
      String[] urlSplit = requestedUrl.split("\\?");
      if (urlSplit.length < 2 || urlSplit[1].indexOf("=") < 0) {
        LOGGER.warning("Encountered a malformed url. It's missing a session param. Ignoring request: " + requestedUrl);
        return;
      }
      
      String sessionId = getParamValue("sessionId", urlSplit[1]);
      if (sessionId == null || !latestSessionIds.contains(sessionId)) {
        LOGGER.warning("Encountered a malformed url. It has an incorrect session param. Ignoring request: " + requestedUrl + " -- expected: " + latestSessionIds);
        return;
      }
      
      File file = new File(urlSplit[0]);
      
      if (!file.exists()) {
        throw new FileNotFoundException("The file was not found: " + file.getName());
      }
      
      if (!downloadOfFileIsAllowed(file)) {
        throw new FileNotFoundException("The user is not authorized to download this type of file. Please make sure that the file is in the base-paths and that the file type of the file is in the supported_filetypes.txt file");
      }
      
      long startingByte = extractRange(header);
      
      PrintWriter ps = new PrintWriter(connectionSocket.getOutputStream());
      printHeaders(file, startingByte,  ps);
      
      if (SupportedFiletypeSettings.fileNameToFileType(file.getName()) == FileType.IMAGE) {
        sendImage(file, new BufferedOutputStream(connectionSocket.getOutputStream()));
      } else {
        sendFile(file, startingByte, new BufferedOutputStream(connectionSocket.getOutputStream()));
      }
      
      
    } catch (UnsupportedEncodingException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    } 
  }

  /**
   * Returns true if, and only if, the file meets the following conditions: 1.
   * The file must be in a directory that is a child of 'base paths' 2. The file
   * must be of a file type that is in the supporte_filetypes.
   * 
   * This is based on the least privilege principle. It helps ensure that
   * potential intruders will only have access to media files, and that these
   * files are only
   */
  private boolean downloadOfFileIsAllowed(File file) {
    if (SupportedFiletypeSettings.fileNameToFileType(file.getName()) == FileType.UNKNOWN) {
      return false;
    }
    
    for (FileInfo path : BaseMediaPaths.getInstance().getBasePaths()) {
      // Make sure that we only return paths that exist.
      if (file.getAbsolutePath().toLowerCase().startsWith(path.getAbsolutePath().toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private long extractRange(List<String> headers) {
    
    String headerValue = getHeaderValue("Range", headers);
    if (headerValue == null) {
      return 0;
    }
    if (headerValue.startsWith("bytes=")) {
      headerValue = headerValue.substring("bytes=".length());
      String fields[] = headerValue.split("-");
      try {
        return Long.parseLong(fields[0]);
      } catch (NumberFormatException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
        return 0;
      }
    }
    return 0;
  }
  
  private String getParamValue(String paramName, String fullParam) {
    String[] paramSplit = fullParam.split("=");
    if (paramSplit.length != 2) {
      return null;
    }
    if (paramSplit[0].equalsIgnoreCase(paramName)) {
      return paramSplit[1];
    }
    
    return null;
  }

  private String getHeaderValue(String fieldName, List<String> headers) {
    for (String header : headers) {
      LOGGER.info("Header: " + header);
      if (header.startsWith(fieldName + ":") ) {
        return header.substring(header.indexOf(":") + 1).trim();
      }
    }
    return null;
  }
  private List<String> extractHeader(BufferedReader reader) throws IOException {
    String line = null;
    List<String> header = new ArrayList<String>();
    while ((line = reader.readLine()) != null && !(line.length()==0)) {
      header.add(line);
    }
    return header;
  }

  private String extractFile(String fileNameHeaderLine) throws IOException, UnsupportedEncodingException {
    LOGGER.info("Extracting file path from: " + fileNameHeaderLine);
    String[] fields = fileNameHeaderLine.split(" ");
    if (fields.length < 2) {
      throw new MalformedURLException("Invalid url. Did not find file name: " + fileNameHeaderLine);
    }
    
    String fileName = URLDecoder.decode(fields[0], "UTF-8");
    if (!fileName.startsWith("/files/")) {
      fileName = URLDecoder.decode(fields[1], "UTF-8");     
      if (!fileName.startsWith("/files/")) {
        LOGGER.warning("Invalid url. Ignoring connection request: " + fileName);
        throw new MalformedURLException("Invalid url. Url doesn't start with /files: " + fileName);
      }
    }
    fileName = fileName.substring("/files/".length());
    
    return fileName;
  }

  void sendFile(File targ, long startingByte, BufferedOutputStream dataOut) throws IOException {
    LOGGER.info("Sending file: " + targ.getAbsolutePath() + " offset: " + startingByte);
    byte[] buf = new byte[2048];
    
    InputStream is = null;
    
    if (targ.isDirectory()) {
      // listDirectory(targ, ps);
      return;
    } else {
      is = new FileInputStream(targ.getAbsolutePath());
      if (startingByte != 0) {
        long bytesSkipped = is.skip(startingByte);
        LOGGER.info("bytesSkipped = " + bytesSkipped);
      }
    }

    try {
      int n;
      while ((n = is.read(buf)) >= 0) {
        dataOut.write(buf, 0, n);
      }
    } finally {
      LOGGER.info("Done sending file");
      is.close();
    }
    dataOut.close();
    LOGGER.info("Print stream closed");
  }
  
  private void sendImage(File originalImagePath, BufferedOutputStream dataOut) throws InterruptedException, ImageFormatException, IOException {
    LOGGER.info("Converting image to smaller scale");
 // load image from INFILE
    Image image = Toolkit.getDefaultToolkit().getImage(originalImagePath.getAbsolutePath());
    MediaTracker mediaTracker = new MediaTracker(new Container());
    mediaTracker.addImage(image, 0);
    mediaTracker.waitForID(0);
    // determine thumbnail size from WIDTH and HEIGHT
    
    int imageWidth = image.getWidth(null);
    int imageHeight = image.getHeight(null);
    
    int thumbWidth = imageWidth;
    int thumbHeight = imageHeight;
    int MAX_SIZE = 500;
    if (imageWidth > MAX_SIZE || imageHeight > MAX_SIZE) {
      double imageRatio = (double)imageWidth / (double)imageHeight;
      if (imageWidth > imageHeight) {
        thumbWidth = MAX_SIZE;
        thumbHeight = (int) (thumbWidth / imageRatio);
      } else {
        thumbHeight = MAX_SIZE;
        thumbWidth = (int) (thumbHeight * imageRatio);
      }
    }
   
    // draw original image to thumbnail image object and
    // scale it to the new size on-the-fly
    BufferedImage thumbImage;
    
    thumbImage = new BufferedImage(thumbWidth, 
      thumbHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics2D = thumbImage.createGraphics();
    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
    
    if (PlatformUtil.isLinux()) {
      ImageIO.write(thumbImage, "JPEG", dataOut);
    } else {
      JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(dataOut);
      JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
      float quality = 80;
      param.setQuality(quality / 100.0f, false);

      encoder.encode(thumbImage, param);
    }
    
    dataOut.close();
    LOGGER.info("Done sending image");
  }

  public static BufferedImage shrink(BufferedImage image, int n) {
    
    int w = image.getWidth() / n;
    int h = image.getHeight() / n;
    
    BufferedImage shrunkImage =
            new BufferedImage(w, h, image.getType());
    
    for (int y=0; y < h; ++y)
        for (int x=0; x < w; ++x)
            shrunkImage.setRGB(x, y, image.getRGB(x*n, y*n));
    
    return shrunkImage;
  }

  boolean printHeaders(File targ, long startingByte, PrintWriter pw) throws IOException {
    boolean ret = false;

    if (!targ.exists()) {
      pw.println("HTTP/1.0 " + HTTP_NOT_FOUND + " not found");
      
      ret = false;
    } else {
      pw.println("HTTP/1.0 " + HTTP_OK + " OK");
      
      ret = true;
    }

    pw.println("Server: GmoteHttpServer");
    pw.println("Date: " + (new Date()));
    if (ret) {
      long fileLength = targ.length();
      if (startingByte != 0) {
        pw.println("Content-range: bytes" + startingByte + "-" + (fileLength - 1) + "/" + fileLength); 
      }
      pw.println("Content-length: " + (fileLength - startingByte));
      pw.println("Last Modified: " + (new Date(targ.lastModified())));
      String name = targ.getName();
      String ct = MimeTypeResolver.findMimeType(name);
      if (ct.equals(MimeTypeResolver.UNKNOWN_MIME_TYPE)) {
        FileType type = SupportedFiletypeSettings.fileNameToFileType(name);
        if (type == FileType.MUSIC) {
          ct = "audio/unknown";
        } else if (type == FileType.VIDEO) {
          ct = "video/unknown";
        } else {
          ct = MimeTypeResolver.findMimeTypeSlow(targ);
        }
      }
      LOGGER.info("Mime type is: " + ct);
      pw.println("Content-type: " + ct);
      
    }
    pw.println();
    pw.flush();
    return ret;
  }
  
  public class HttpConnectionHandler implements Runnable {

    private List<String> latestSessionIds;
    
    public HttpConnectionHandler(List<String> latestSessionIds) {
      this.latestSessionIds = latestSessionIds;
    }

    public void run() {
      try {
        handleHttpRequest(latestSessionIds);
        LOGGER.info("Done handlerequest(). Closing connection.");
      } catch (Exception ex) {
        // Catching all exceptions since this is the top layer of our app.
        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        
          try {
            PrintWriter ps = new PrintWriter(connectionSocket.getOutputStream());
            ps.println("HTTP/1.0 " + HTTP_NOT_FOUND + " not found " + ex.getMessage());
          } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
          }
        
      } finally {
        LOGGER.info("Closing http connection");
        try {
          connectionSocket.close();
        } catch (IOException e) {
          LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
      }
    }
  }
  
}
