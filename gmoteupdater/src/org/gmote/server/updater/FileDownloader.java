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

package org.gmote.server.updater;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gmote.server.updater.Updater.UpdateTask;

public class FileDownloader {
  private static final Logger LOGGER = Logger.getLogger(FileDownloader.class.getName());

  /**
   * Downloads a file from the Internet into a temporary local file.
   * @param updateTask 
   * 
   * @return A file object representing the local file.
   * @throws IOException 
   */
  public static File download(String address, long totalSizeInBytes,  UpdateTask updateTask) throws IOException {
    OutputStream out = null;
    URLConnection conn = null;
    InputStream in = null;
    File tempFile = null;
    try {
      URL url = new URL(address);
      String[] addressFields = address.split("\\.");
      String fileType = addressFields[addressFields.length - 1];
      tempFile = File.createTempFile("gmoteupdate", "." + fileType);
      out = new BufferedOutputStream(new FileOutputStream(tempFile));
      conn = url.openConnection();
      in = conn.getInputStream();
      byte[] buffer = new byte[1024];
      int numRead;
      long numWritten = 0;
      updateTask.setProgressBarSize(100);
      int counter = 0;
      while ((numRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, numRead);
        numWritten += numRead;
        
        counter++;
        if (counter % 10 == 0) {
          updateTask.setProgressForTask(Math.min((int)(((double)numWritten / totalSizeInBytes) * 100), 99));
        }
      }
    } finally {

      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
      }

      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
      }
      updateTask.setProgressForTask(100);
      
    }
    
    return tempFile;
  }
}
