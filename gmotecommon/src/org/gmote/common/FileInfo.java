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

package org.gmote.common;

import java.io.Serializable;

public class FileInfo implements Serializable, Comparable<FileInfo>{

  public enum FileType {
    MUSIC,
    VIDEO,
    PLAYLIST, 
    DVD_DRIVE,
    POWER_POINT,
    IMAGE,
    UNKNOWN,
    PDF;
  }
  
  public enum FileSource {
    FILE_SYSTEM, // File is on the file system (ex: c:\myfile.mp3)
    MEDIA_LIBRARY // File is in the library of a media player (ex: returned by itunes).
  }

  private static final long serialVersionUID = 1L;
  
  String fileName;
  String absolutePath;
  FileType fileType = null;
  FileSource fileSource;
  
  boolean isDirectory;
   
  public FileInfo(String fileName, String absolutePath, FileType fileType, boolean isDirectory, FileSource fileSource) {
    this.fileName = fileName;
    this.absolutePath = absolutePath;
    this.fileType = fileType;
    this.isDirectory = isDirectory;
    this.fileSource = fileSource;
  }
  
  public FileSource getFileSource() {
    return fileSource;
  }

  public FileInfo() {
    //TODO(mstogaitis): investigate if we really need to set this to true.
    this.isDirectory = true;
  }

  public String getAbsolutePath() {
    return absolutePath;
  }

  public boolean isDirectory() {
    return isDirectory;
  }
  
  public String getFileName() {
    if (fileName.equals("")) {
      return getAbsolutePath();
    }
   return fileName; 
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FileInfo)) {
      return false;
    }
    FileInfo other = (FileInfo) obj;
    
    if (!verifyEquality(fileName, other.fileName)) {
      return false;
    }
    
    if (!verifyEquality(absolutePath, other.absolutePath)) {
      return false;
    }
    
    if (!verifyEquality(fileType, other.fileType)) {
      return false;
    }
    
    if (!verifyEquality(fileSource, other.fileSource)) {
      return false;
    }
    return true;
  }

  private boolean verifyEquality(Object obj1, Object obj2) {
    if (onlyOneIsNull(obj1, obj2)) {
      return false;
    } else if (!(obj1 == null) && !obj1.equals(obj2)) {
      return false;
    }
    return true;
  }
  
  @Override
  public int hashCode() {
    int hashCode = 0;
    hashCode += 17 * getHash(fileName);
    hashCode += 19 * getHash(absolutePath);
    hashCode += 23 * getHash(fileType);
    hashCode += 29 * getHash(fileSource);
    return hashCode;
  }

  private int getHash(Object obj) {
    if (obj == null) {
      return 0;
    }
    return obj.hashCode();
  }

  private boolean onlyOneIsNull(Object obj1, Object obj2) {
    return (obj1 == null && obj2 != null || obj1 != null && obj2 == null);
  }

  public boolean isControllable() {
    //TODO(mimi): is this method still necessary?
    /*
    if (FileType.MUSIC == fileType || FileType.VIDEO == fileType || FileType.PLAYLIST == fileType
        || FileType.DVD_DRIVE == fileType || fileType == FileType.UNKNOWN
        || fileType == FileType.POWER_POINT || fileType == FileType.IMAGE) {
      return true;
    }
    */
    return !isDirectory;
  }
  
  /**
   * Returns the type of file, or null if it's a directory or the file extension is unknown.
   * @return
   */
  public FileType getFileType() {
    return fileType;
  }
  
  @Override
  public String toString() {
    if (fileType != null) {
      return getAbsolutePath() + "|" + fileType.name();
    } else {
      return getAbsolutePath();
    }
    
  }

  public int compareTo(FileInfo arg0) {
    return getAbsolutePath().toLowerCase().compareTo(arg0.absolutePath.toLowerCase());
  }
  
}
