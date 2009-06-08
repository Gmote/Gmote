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

package org.gmote.server.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.filechooser.FileSystemView;

import org.gmote.common.FileInfo;
import org.gmote.common.FileInfo.FileSource;
import org.gmote.common.FileInfo.FileType;
import org.gmote.server.PlatformUtil;
import org.gmote.server.ServerUtil;

/**
 * Keeps track of the list of directories the user has access to by default.
 * These are at the bottom of the file tree. (ex: c:\downloads\movies)
 */
public class BaseMediaPaths {

  private static Logger LOGGER = Logger.getLogger(BaseMediaPaths.class.getName());
  private static BaseMediaPaths instance = null;
  private String fileName;
  private List<FileInfo> basePaths = new ArrayList<FileInfo>();

  private BaseMediaPaths(String fileName) {
    this.fileName = fileName;
    loadBasepathsFromFile();
  }

  public static synchronized BaseMediaPaths getInstance() {
    if (instance == null) {
      instance = new BaseMediaPaths(SystemPaths.BASE_PATHS.getFullPath());
    }
    return instance;
  }
  
  public void addPath(String path) {
    basePaths.add(ServerUtil.instance().fileInfoFromFile(new File(path)));
    saveBasepathsToFile();
  }
  
  public void removePath(int index) {
    basePaths.remove(index);
    saveBasepathsToFile();
  }
  
  public List<FileInfo> getBasePaths() {
    return Collections.unmodifiableList(basePaths);
  }

  /**
   * Creates a new, file that holds the default paths. This should only be done
   * the first time the application is launched.
   */
  public static void createDefaultFile() {
    getInstance().basePaths.clear();
    getInstance().addPath(createDefaultMediaPathForFileSystem().getAbsolutePath());
    
    if (!StartupSettings.instance().getSetting(StartupSettingsEnum.ADDED_DVD_DRIVES)) {
      // Try to automatically add the dvd drive to the list.

      if (PlatformUtil.isWindows()) {
        // TODO(mstogaitis): We'll need to do this for linux/mac as well.
        List<File> dvdDrives = findDvdDrivesWin32();
        if (dvdDrives != null) {
          for (File drive : dvdDrives) {
            getInstance().addPath(drive.getAbsolutePath());
          }
        }
        StartupSettings.instance().setSetting(StartupSettingsEnum.ADDED_DVD_DRIVES, true);
      }
    }
  }

  public static List<File> findDvdDrivesWin32() {

    FileSystemView fsv = FileSystemView.getFileSystemView();
    File[] roots = fsv.getRoots();
    if (roots.length == 1) {
      // roots[0] is Desktop, [0] of that is My Computer
      // At least, on test XP and 98SE systems
      roots = roots[0].listFiles()[0].listFiles();
    } else {
      LOGGER.warning("Unable to find dvd drive. It will have to be added manually");
      return null;
    }

    List<File> foundDrives = new ArrayList<File>();

    for (File drive : roots) {
      if (ServerUtil.instance().isDvdDrive(drive)) {
        foundDrives.add(drive);
      }
    }

    return foundDrives;
  }

  
  private void loadBasepathsFromFile() {
    basePaths.clear();
    
    try {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));

      String line;
      while ((line = reader.readLine()) != null) {
        File file = new File(line);
        if (ServerUtil.instance().isDvdDrive(file)) {
          basePaths.add(new FileInfo(file.getName(), file.getAbsolutePath(), FileType.DVD_DRIVE,
              true, FileSource.FILE_SYSTEM));
        } else {
          basePaths.add(ServerUtil.instance().fileInfoFromFile(file));
        }

      }
      reader.close();
      if (basePaths.size() == 0) {
        basePaths.add(ServerUtil.instance().fileInfoFromFile(createDefaultMediaPathForFileSystem()));
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }
  
  private void saveBasepathsToFile() {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
      for (FileInfo file : basePaths) {
        writer.write(file.getAbsolutePath());
        writer.newLine();
      }
      writer.close();
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }
  
  private static File createDefaultMediaPathForFileSystem() {
    if (PlatformUtil.isWindows()) {
      return new File("C:\\");
    } else {
      return new File(System.getProperty("user.home"));
    }
    
  }
}
