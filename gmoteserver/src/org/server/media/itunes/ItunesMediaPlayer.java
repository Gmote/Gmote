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

package org.gmote.server.media.itunes;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.gmote.common.FileInfo;
import org.gmote.common.FileInfo.FileSource;
import org.gmote.common.FileInfo.FileType;
import org.gmote.common.Protocol.Command;
import org.gmote.common.media.MediaMetaInfo;
import org.gmote.server.media.MediaCommandHandler;
import org.gmote.server.media.MediaInfoUpdater;
import org.gmote.server.media.MediaPlayerInterface;
import org.gmote.server.media.UnsupportedCommandException;

public class ItunesMediaPlayer implements MediaPlayerInterface {
  private static Logger LOGGER = Logger.getLogger(ItunesMediaPlayer.class
      .getName());
  MediaCommandHandler commandHandler;

  public ItunesMediaPlayer() {
    initCommandHandler();
    MediaInfoUpdater.instance().setPlayerToPoll(this);
  }

  void initCommandHandler() {
    if (commandHandler == null) {
      commandHandler = ItunesCommandHandler.instance();
    }
  }

  public void controlPlayer(Command command) throws UnsupportedCommandException {
    initCommandHandler();
    commandHandler.executeCommand(command);
    MediaInfoUpdater.instance().setPlayerToPoll(this);
  }

  public void runFile(FileInfo fileInfo) throws UnsupportedEncodingException,
      UnsupportedCommandException {
    if (commandHandler != null && commandHandler.isMediaOpen()) {
      controlPlayer(Command.STOP);
    }

    String fileName = fileInfo.getAbsolutePath();
    FileType fileType = fileInfo.getFileType();
    commandHandler = ItunesCommandHandler.instance();

    if (fileInfo.getFileSource() == FileSource.FILE_SYSTEM) {
      ((ItunesCommandHandler) commandHandler).launchFile(fileName);
    } else {
      String fileNameSplit[] = fileName.split("/");
      String playlist = fileNameSplit[0];
      String track = fileNameSplit[1];

      if (fileType == FileType.MUSIC) {
        ((ItunesCommandHandler) commandHandler).launchAudio(track, playlist);
        MediaInfoUpdater.instance().setPlayerToPoll(this);
      } else if (fileType == FileType.VIDEO) {
        ((ItunesCommandHandler) commandHandler).launchVideo(track);
      }
    }
  }

  public MediaMetaInfo getNewMediaInfo() {
    if (commandHandler != null) {
      return commandHandler.getNewMediaInfo();
    }
    return null;
  }

  public List<FileInfo> getBaseLibraryFiles() {

    List<FileInfo> fileInfoPlaylists = new ArrayList<FileInfo>();

    // Add an entry for a dvd drive
    // TODO(mimi): move this out of Itunes
    fileInfoPlaylists.add(new FileInfo("DVD", "DVD_DRIVE:DVD",
        FileType.DVD_DRIVE, false, FileSource.FILE_SYSTEM));

    ItunesCommandHandler ich = ItunesCommandHandler.instance();
    List<String> playLists = ich.getPlaylists();
    for (String playlist : playLists) {
      fileInfoPlaylists.add(new FileInfo(playlist, playlist, null, true,
          FileSource.MEDIA_LIBRARY));
    }

    return fileInfoPlaylists;
  }

  public List<FileInfo> getLibrarySubFiles(FileInfo fileInfo) {
    ItunesCommandHandler ich = ItunesCommandHandler.instance();
    List<String> tracks = ich.getTracksFromPlaylist(fileInfo.getAbsolutePath());
    List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
    int i = 0;
    for (String track : tracks) {
      String trackSplit[] = track.split("\\|");
      String name = trackSplit[0];
      String typeName = trackSplit[1].toLowerCase();

      FileType type;
      type = findFileType(typeName);
      fileInfoList.add(new FileInfo(name, 
          fileInfo.getAbsolutePath() + "/" + name, type, false,
          FileSource.MEDIA_LIBRARY));
      i++;
    }
    return fileInfoList;
  }

  private FileType findFileType(String typeName) {
    FileType type;
    if (typeName.contains("video") || typeName.contains("movie")) {
      type = FileType.VIDEO;
    } else {
      type = FileType.MUSIC;
      if (!typeName.contains("music") && !typeName.contains("audio")) {
        LOGGER.warning("Unrecognized type name: " + typeName);
      }
    }
    return type;
  }

  public void initialise(Map<String, String> arguments) {

  }

  public void initialise(String[] arguments) {

  }

  public boolean isRunning() {
    return ItunesCommandHandler.instance().running();
  }
}
