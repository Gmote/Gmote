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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gmote.common.FileInfo.FileType;
import org.gmote.server.PlatformUtil;
import org.gmote.server.media.basic.DefaultFilePlayer;
import org.gmote.server.media.basic.PowerPointPlayer;

/**
 * Allows us to determine which file type we support and which media player binding
 * should be used to play a file type.
 * 
 * @author Marc Stogaitis
 * 
 */
public class SupportedFiletypeSettings {
  private static final String FIELD_SEPARATOR = ":";
  private static final String FILE_GROUP_PREFIX = "filegroup";
  private static final Logger LOGGER = Logger.getLogger(SupportedFiletypeSettings.class.getName());

  private static SupportedFiletypeSettings instance = null;
  private Map<String, String> defaultPlayerExceptions = new HashMap<String, String>();
  private Map<FileType, String> defaultPlayerForFileType = new HashMap<FileType, String>();
  private Map<String, FileType> fileExtensionToFileType = new HashMap<String, FileType>();
  
  // Private constructor to prevent instantiation.
  private SupportedFiletypeSettings(String fileName) {
    LOGGER.info("Initializing supported file types settings.");
    loadSupportedTypes(fileName);
    LOGGER.info("Done initializing supported file types settings.");
  }
  
  public static synchronized SupportedFiletypeSettings getInstance() {
    if (instance == null) {
      instance = new SupportedFiletypeSettings(SystemPaths.SUPPORTED_FILE_TYPES.getFullPath());
    }
    return instance;
  }
  
  
  /**
   * Determines the type of file from it's name by looking at it's extension.
   */
  public static FileType fileNameToFileType(String fileName) {
    String extension = extractFileExtension(fileName);
    if (extension == null) {
      return FileType.UNKNOWN;
    }
    return extensionToFileType(extension);
  }
  
  /**
   * Determines which media player should be used to play a file based on it's file extension.
   */
  public static String getMediaPlayerBindingName(String fileName) {
    String extension = extractFileExtension(fileName);
    SupportedFiletypeSettings fileTypeSettings = getInstance();
    if (extension == null) {
      return fileTypeSettings.defaultPlayerForFileType.get(FileType.UNKNOWN);
    } else if (fileTypeSettings.defaultPlayerExceptions.containsKey(extension)) {
      return fileTypeSettings.defaultPlayerExceptions.get(extension);
    } else {
      FileType fileType = extensionToFileType(extension);
      return fileTypeSettings.defaultPlayerForFileType.get(fileType);
    }
  }
  
  public static String getMediaPlayerBindingName(FileType fileType) {
    return getInstance().defaultPlayerForFileType.get(fileType);
  }
  
  public static String extractFileExtension(String fileName) {
    if (fileName == null) {
      return null;
    }
    String[] fileSplit = fileName.split("\\.");
    return fileSplit[fileSplit.length - 1].toLowerCase();
  }

  private static FileType extensionToFileType(String fileExtension) {
    if (fileExtension == null
        || !getInstance().fileExtensionToFileType.containsKey(fileExtension.toLowerCase())) {
      return FileType.UNKNOWN;
    }
    return getInstance().fileExtensionToFileType.get(fileExtension.toLowerCase());
  }
  

  public static void createDefaultFile() {
    String fileName = SystemPaths.SUPPORTED_FILE_TYPES.getFullPath();
    String defaultMediaPlayer = null;
    String defaultUnknownFilePlayer = DefaultFilePlayer.class.getName();

    if (PlatformUtil.isWindows() || PlatformUtil.isLinux()) {
      // Default configuration file for windows.
      defaultMediaPlayer = "org.gmote.server.media.vlc.VlcMediaPlayer";
    } else if (PlatformUtil.isMac()) {
      defaultMediaPlayer = "org.gmote.server.media.itunes.ItunesMediaPlayer";      
    } else {
      // Use vlc in case we can't determine the os.
      LOGGER.warning("Unable to determine the operating system: " + System.getProperty("os.name"));
      defaultMediaPlayer = "org.gmote.server.media.vlc.VlcMediaPlayer";
    }

    BufferedWriter writer;
    try {
      writer = new BufferedWriter(new FileWriter(fileName));
      // Print usage information.
      writer.write("# Holds information about which file types are supported as well as which media player binding should be used to play the file.");
      writer.newLine();
      writer.write("# You can change the media player binding to use for en entire group by changing the name of the player that is written beside " + FILE_GROUP_PREFIX);
      writer.newLine();
      writer.write("# You can chanage the media player binding for a specific file type by appending a ':org.gmote.somemediaplayername' beside a filetype.");
      writer.newLine();
      writer.write("# Example: mp3:org.gmote.server.media.windows.WindowsMediaPlayer11Controller");
      writer.newLine();
      writer.write("# You can find more information at: http://www.gmote.org/faq");
      writer.newLine();
      writer.newLine();
      
      Map<FileType, String[]> defaultFileTypes = generateDefaultSupportedFileType();
      for (FileType fileGroup : FileType.values()) {
        if (fileGroup == FileType.UNKNOWN) {
          writer.write(FILE_GROUP_PREFIX + FIELD_SEPARATOR + fileGroup.name() + FIELD_SEPARATOR + defaultUnknownFilePlayer);
        } else if (fileGroup == FileType.POWER_POINT) {
          writer.write(FILE_GROUP_PREFIX + FIELD_SEPARATOR + fileGroup.name() + FIELD_SEPARATOR + PowerPointPlayer.class.getName());
        } else if (fileGroup == FileType.IMAGE) {
          if (PlatformUtil.isWindows()) {
            writer.write(FILE_GROUP_PREFIX + FIELD_SEPARATOR + fileGroup.name() + FIELD_SEPARATOR + "org.gmote.server.media.windows.WindowsImagePlayer");
          } else if (PlatformUtil.isMac()) {
            writer.write(FILE_GROUP_PREFIX + FIELD_SEPARATOR + fileGroup.name() + FIELD_SEPARATOR + "org.gmote.server.media.preview.PreviewPlayer");
          } else {
            writer.write(FILE_GROUP_PREFIX + FIELD_SEPARATOR + fileGroup.name() + FIELD_SEPARATOR + "org.gmote.server.media.linux.LinuxImagePlayer");
          }
        } else if (fileGroup == FileType.DVD_DRIVE) {
          if (PlatformUtil.isMac()) {
            writer.write(FILE_GROUP_PREFIX + FIELD_SEPARATOR + fileGroup.name() + FIELD_SEPARATOR + "org.gmote.server.media.appledvd.AppleDvdPlayer");
          } else {
            writer.write(FILE_GROUP_PREFIX + FIELD_SEPARATOR + fileGroup.name() + FIELD_SEPARATOR + defaultMediaPlayer);
          }
        } else if (fileGroup == FileType.PDF){
          if (PlatformUtil.isMac()) {
            writer.write(FILE_GROUP_PREFIX + FIELD_SEPARATOR + fileGroup.name() + FIELD_SEPARATOR + "org.gmote.server.media.preview.PreviewPlayer");
          } else if(PlatformUtil.isLinux()) {
            writer.write(FILE_GROUP_PREFIX + FIELD_SEPARATOR + fileGroup.name() + FIELD_SEPARATOR + "org.gmote.server.media.linux.LinuxPdfPlayer");
          } else if(PlatformUtil.isWindows()) {
            writer.write(FILE_GROUP_PREFIX + FIELD_SEPARATOR + fileGroup.name() + FIELD_SEPARATOR + "org.gmote.server.media.windows.WindowsPdfPlayer");
          } else {
            continue;
          }
        } else {
          writer.write(FILE_GROUP_PREFIX + FIELD_SEPARATOR + fileGroup.name() + FIELD_SEPARATOR + defaultMediaPlayer);
        }
        writer.newLine();
        if (defaultFileTypes.containsKey(fileGroup)) {
          String[] supportedFileExtensions = defaultFileTypes.get(fileGroup);
          for (String fileExtension : supportedFileExtensions) {
            writer.write(fileExtension);
            writer.newLine();
          }
        }
        writer.newLine();
      }

      writer.close();
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
    SupportedFiletypeSettings.getInstance().loadSupportedTypes(fileName);
  }

  private synchronized void loadSupportedTypes(String fileName) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      String line = null;
      FileType currentFileType = null;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line != "" && !line.startsWith("#")) {
          String[] fields = line.split(FIELD_SEPARATOR);
          if (fields[0].equalsIgnoreCase(FILE_GROUP_PREFIX)) {
            // This is a new group of files.
            currentFileType = FileType.valueOf(fields[1].toUpperCase());
            defaultPlayerForFileType.put(currentFileType, fields[2]);
          } else {
            fileExtensionToFileType.put(fields[0].toLowerCase(), currentFileType);
            if (fields.length > 1) {
              // The user has specified that this file should be played in a player different from the default for the group.
              defaultPlayerExceptions.put(fields[0].toLowerCase(), fields[1]);
            }
          }
        }        
      }
      // Do a little error checking in case the config file was not written properly.
      if (!defaultPlayerForFileType.containsKey(FileType.UNKNOWN)) {
        LOGGER.warning("Did not find UNKNOWN as a file type in config file.");
        defaultPlayerForFileType.put(FileType.UNKNOWN, DefaultFilePlayer.class.getName());
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private static Map<FileType, String[]> generateDefaultSupportedFileType() {
    Map<FileType, String[]> defaultFileTypes = new HashMap<FileType, String[]>();

    // These file types are supported by vlc.
    String[] defaultMusicTypes = new String[] { "mp3", "wma", "wav", "dts", "aac", "es", "ps",
        "axa", "ac3", "a52", "flac", "midi", "voc", "m4a" };
    
    String[] defaultVideoTypes = new String[] { "avi", "wmv", "ts", "asf", "mp4", "mov", "3gp",
        "flv", "ogg", "ogm", "mkv", "pva", "axv", "dif", "dv", "mpg", "mpeg", "video_ts" };
    
    String[] defaultPlaylistTypes = new String[] { "m3u" };

    String[] defaultPowerPointTypes = new String[] {"ppt", "pps"};
    
    String[] defaultImageTypes = new String[] {"jpeg", "jpg", "png", "bmp", "gif", "tif", "tiff", "wmf", "art"};
    
    String[] defaultPdfTypes = new String[] {"pdf"};
    
    defaultFileTypes.put(FileType.MUSIC, defaultMusicTypes);
    defaultFileTypes.put(FileType.VIDEO, defaultVideoTypes);
    defaultFileTypes.put(FileType.PLAYLIST, defaultPlaylistTypes);
    defaultFileTypes.put(FileType.POWER_POINT, defaultPowerPointTypes);
    defaultFileTypes.put(FileType.IMAGE, defaultImageTypes);
    defaultFileTypes.put(FileType.PDF, defaultPdfTypes);
    return defaultFileTypes;
  }

}
