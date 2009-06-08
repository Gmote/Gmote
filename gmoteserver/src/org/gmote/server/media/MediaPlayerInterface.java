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

package org.gmote.server.media;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.gmote.common.FileInfo;
import org.gmote.common.Protocol.Command;
import org.gmote.common.media.MediaMetaInfo;

public interface MediaPlayerInterface {

  public void initialise(String[] arguments);
  
  /**
   * Launches a file in this media controller.
   * 
   * @param fileName The file to launch.
   * @param fileType
   * @throws UnsupportedEncodingException
   * @throws UnsupportedCommandException
   */
  public void runFile(FileInfo fileInfo) throws UnsupportedEncodingException,
      UnsupportedCommandException;

  /**
   * Handles commands to the player, such as pause, play etc.
   * 
   * @throws UnsupportedCommandException
   */
  public void controlPlayer(Command command) throws UnsupportedCommandException;

  /**
   * @returns the new media information if media has changed, null otherwise
   */
  public MediaMetaInfo getNewMediaInfo();

  
  /**
   * Returns a list of files that is at the base of a media library. This is
   * useful when media players such as Itunes support their own media library.
   * If the player doesn't support this feature, returns null.
   */
  public List<FileInfo> getBaseLibraryFiles();
  
  public List<FileInfo> getLibrarySubFiles(FileInfo fileInfo);

  public boolean isRunning();

}
