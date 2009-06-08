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

package org.gmote.server.media.appledvd;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.gmote.common.FileInfo;
import org.gmote.common.Protocol.Command;
import org.gmote.common.media.MediaMetaInfo;
import org.gmote.server.media.MediaInfoUpdater;
import org.gmote.server.media.MediaPlayerInterface;
import org.gmote.server.media.UnsupportedCommandException;

public class AppleDvdPlayer implements MediaPlayerInterface {

  public void controlPlayer(Command command) throws UnsupportedCommandException {
    DvdPlayerCommandHandler.instance().executeCommand(command);
  }

  public List<FileInfo> getBaseLibraryFiles() {
    return null;
  }

  public List<FileInfo> getLibrarySubFiles(FileInfo fileInfo) {
    return null;
  }

  public MediaMetaInfo getNewMediaInfo() {
    return null;
  }

  public void initialise(String[] arguments) {
  }

  public void runFile(FileInfo fileInfo) throws UnsupportedEncodingException,
      UnsupportedCommandException {
    DvdPlayerCommandHandler.instance().launchDvd();
    MediaInfoUpdater.instance().setPlayerToPoll(null);
  }

  public boolean isRunning() {
    return DvdPlayerCommandHandler.instance().running();
  }

}
