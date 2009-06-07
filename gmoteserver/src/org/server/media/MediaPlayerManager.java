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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.gmote.common.FileInfo.FileType;
import org.gmote.server.GmoteServer;
import org.gmote.server.settings.SupportedFiletypeSettings;

public class MediaPlayerManager {
  private static final String UNROCOGNIZED_MEDIA_PLAYER_ERROR_MESSAGE = "Unrecognized media player string in default_settings.txt "
          + "config file in parameter 'PLAYER'. Please visit www.gmote.org "
          + "to get more information on config files.";

  private static final String MEDIA_PLAYER_ERROR_MESSAGE = "There was an error starting a gmote media player. Please visit http:/www.gmote.org/faq for more information\n";

  private static final Logger LOGGER = Logger.getLogger(GmoteServer.class.getName());
  
  Map<String,MediaPlayerInterface> mediaPlayerInstances = new HashMap<String, MediaPlayerInterface>();
  
  private String arguments[] = null;
  
  private static MediaPlayerManager instance = new MediaPlayerManager();
  
  private MediaPlayerManager() {
  
  }
  
  public static MediaPlayerManager getInstance() {
    return instance;
  }

  /**
   * Initializes the media manager with arguments that will be passed to the
   * media players.
   * 
   * @param arguments
   */
  public void initialize(String[] arguments) {
    this.arguments = arguments;
    // Make an instance of the default player for music and movies to accelerate
    // first playback. If the players are the same, only one instance will be created.
    getMediaPlayer(FileType.MUSIC);
    getMediaPlayer(FileType.VIDEO);
    
  }
  
  public MediaPlayerInterface getMediaPlayer(FileType fileType) {
    String mediaPlayerClassName = SupportedFiletypeSettings.getMediaPlayerBindingName(fileType);
    if (mediaPlayerClassName == null) {
      LOGGER.warning("Could not determine media player class name for file type: " + fileType);
      return null;
    }
    return getMediaPlayerInstance(mediaPlayerClassName);
  }
  
  public MediaPlayerInterface getMediaPlayer(String fileName) {
    String mediaPlayerClassName = SupportedFiletypeSettings.getMediaPlayerBindingName(fileName);
    return getMediaPlayerInstance(mediaPlayerClassName);
  }
  
  public MediaPlayerInterface getRunningMediaPlayer() {
    MediaPlayerInterface currentPlayer = null;
    FileType[] preferredFileTypes = {FileType.DVD_DRIVE, FileType.VIDEO, FileType.MUSIC};
    for (FileType type : preferredFileTypes) {
      currentPlayer = getMediaPlayer(type);
      if (currentPlayer != null && currentPlayer.isRunning()) {
        LOGGER.info("GetRunningMediaPlayer = " + type);
        break;
      }
    }
    return currentPlayer;
  }

  /**
   * Creates a new instance of the media player and adds it to the cache. This
   * is only needed if something went wrong with a media player and it should be
   * reloaded.
   */
  public MediaPlayerInterface reloadMediaPlayer(String mediaPlayerClassName) {
    mediaPlayerInstances.remove(mediaPlayerClassName);
    return getMediaPlayerInstance(mediaPlayerClassName);
  }

  private MediaPlayerInterface getMediaPlayerInstance(String mediaPlayerClassName) {
    if (!mediaPlayerInstances.containsKey(mediaPlayerClassName)) {
      MediaPlayerInterface mediaPlayer = createNewMediaPlayerInstance(mediaPlayerClassName);
      mediaPlayerInstances.put(mediaPlayerClassName, mediaPlayer);
    }
    return mediaPlayerInstances.get(mediaPlayerClassName);
  }

  private MediaPlayerInterface createNewMediaPlayerInstance(String mediaPlayerClassName) {
    LOGGER.info("Creating media player with name: " + mediaPlayerClassName);
    MediaPlayerInterface mediaPlayer;
    try {
      mediaPlayer = (MediaPlayerInterface) Class.forName(mediaPlayerClassName).newInstance();
      mediaPlayer.initialise(arguments);
      return mediaPlayer;
    } catch (ClassCastException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      JOptionPane.showMessageDialog(null, MEDIA_PLAYER_ERROR_MESSAGE + e.getMessage());
    } catch (InstantiationException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      JOptionPane.showMessageDialog(null, MEDIA_PLAYER_ERROR_MESSAGE + e.getMessage());
      System.exit(1);
    } catch (IllegalAccessException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      JOptionPane.showMessageDialog(null, MEDIA_PLAYER_ERROR_MESSAGE + e.getMessage());
      System.exit(1);
    } catch (ClassNotFoundException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      JOptionPane.showMessageDialog(null, MEDIA_PLAYER_ERROR_MESSAGE + e.getMessage());
    }
    LOGGER.severe(UNROCOGNIZED_MEDIA_PLAYER_ERROR_MESSAGE);
    JOptionPane.showMessageDialog(null, UNROCOGNIZED_MEDIA_PLAYER_ERROR_MESSAGE);
    System.exit(1);
    return null;
  }
}
