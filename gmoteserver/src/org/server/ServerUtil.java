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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.filechooser.FileSystemView;

import org.gmote.common.FileInfo;
import org.gmote.common.FileInfo.FileSource;
import org.gmote.common.FileInfo.FileType;
import org.gmote.server.settings.SupportedFiletypeSettings;

public abstract class ServerUtil {
  private static final String VIDEO_TS = "VIDEO_TS";
  static Logger LOGGER = Logger.getLogger(ServerUtil.class.getName());
  private static ServerUtil mServerUtil = null;

  public static ServerUtil instance() {
    if (mServerUtil == null) {
      try {
        if (PlatformUtil.isWindows()) {
          mServerUtil = (ServerUtil) Class.forName(
              "org.gmote.server.ServerUtilWindows").newInstance();
        } else if (PlatformUtil.isMac()) {
          mServerUtil = (ServerUtil) Class.forName(
              "org.gmote.server.ServerUtilMac").newInstance();
        } else if (PlatformUtil.isLinux()) {
          mServerUtil = (ServerUtil) Class.forName(
          "org.gmote.server.ServerUtilLinux").newInstance();
        } else {
          // not supported yet!
          LOGGER.warning("Server util not implemented for this operating system yet");
          mServerUtil = (ServerUtil) Class.forName(
          "org.gmote.server.ServerUtilWindows").newInstance();
        }
      } catch (ClassCastException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      } catch (InstantiationException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
        System.exit(1);
      } catch (IllegalAccessException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
        System.exit(1);
      } catch (ClassNotFoundException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }
    }
    return mServerUtil;
  }
  /**
   * Run a file from the local file system in its default application.
   *
   */
  public abstract void startFileInDefaultApplication(String fileName);

  /**
   * Runs a program by starting it in its own process (not a subprocess of this
   * class like the standard exec function would do). This must be implemented
   * in each operating system
   *
   */
  public abstract void startFileInSeparateprocess(String command);

  public String findInstallDirectory() {
    File curdir = new File(".");
    String path = "";
    try {
      path = curdir.getCanonicalPath();
      LOGGER.log(Level.WARNING, "Current directory: " + path);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }

    return path;
  }

  /**
   * Returns true if the drive is a cd or dvd drive. Most dvd drives are called
   * 'cd' drives which is why we return both types.
   *
   */
  public boolean isDvdDrive(File drive) {
    if (PlatformUtil.isLinux()) {
      return drive.getAbsolutePath().equalsIgnoreCase("/cdrom");
    } else if (PlatformUtil.isWindows()) {
      FileSystemView fsv = FileSystemView.getFileSystemView();
      if (fsv.isDrive(drive)) {
        String driveDescription = fsv.getSystemTypeDescription(drive).toLowerCase();
        return driveDescription.contains("dvd") || driveDescription.contains("cd");
      }
    }
    return false;
  }

  public boolean driveHasDvd(File file) {
    if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        if (child.isDirectory() && child.getName().equalsIgnoreCase(VIDEO_TS)){
          return true;
        }
      }
    }
    return false;
  }

  public boolean folderIsRippedDvd(File folder) {
    return (folder.isDirectory() && folder.getName().equalsIgnoreCase(VIDEO_TS));
  }

  public FileInfo fileInfoFromFile(File file) {
    String fileName = file.getName();
    boolean isDirectory = file.isDirectory();
    String absolutePath = file.getAbsolutePath();
    FileType fileType = null;
    if (!isDirectory) {
      fileType = SupportedFiletypeSettings.fileNameToFileType(fileName);
    } else {
      if (folderIsRippedDvd(file)) {
        // Handle ripped dvd's as if they were media files.
        isDirectory = false;
        fileType = FileType.VIDEO;
      }
    }
    return new FileInfo(fileName, absolutePath, fileType, isDirectory, FileSource.FILE_SYSTEM);
  }

  /**
   * Returns a list of all the local ip addresses on this computer. For example,
   * a laptop may have multiple local ip addresses if it is connected to WiFi
   * while an Ethernet cable is also plugged in.
   *
   * @param ignoreLoopback if true, will not return loopback addresses (127.0.0.1)
   */
  public static List<InetAddress> findAllLocalIpAddresses(boolean ignoreLoopback) throws SocketException {
    List<InetAddress> inetAddresses = new ArrayList<InetAddress>();
    Enumeration<NetworkInterface> nics = null;

    nics = NetworkInterface.getNetworkInterfaces();

    while (nics != null && nics.hasMoreElements()) {
      NetworkInterface nic = nics.nextElement();
      Enumeration<InetAddress> ipAddresses = nic.getInetAddresses();
      while (ipAddresses.hasMoreElements()) {
        InetAddress address = ipAddresses.nextElement();
        address.getAddress();
        if (address.isSiteLocalAddress()) {
          inetAddresses.add(address);
        }
      }
    }
    return inetAddresses;
  }
  
  /**
   * Creates a directory unless it already exists
   */
  public static void createIfNotExists(String path) {
    File f=new File(path);
    if(f.exists()==false){
        f.mkdirs();
    }
  }
  
  public abstract String getUpdateUrl();
}
