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

package org.gmote.server.media.vlc;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.blinkenlights.jid3.MP3File;
import org.gmote.common.media.MediaMetaInfo;
import org.gmote.server.media.MediaCommandHandler;
import org.gmote.server.media.MediaInfoUpdater;
import org.gmote.server.media.PlayerUtil;
import org.videolan.jvlc.Audio;
import org.videolan.jvlc.JVLC;
import org.videolan.jvlc.MediaDescriptor;
import org.videolan.jvlc.MediaPlayer;
import org.videolan.jvlc.Playlist;
import org.videolan.jvlc.VLCException;
import org.videolan.jvlc.internal.LibVlc.libvlc_meta_t;


/**
 * Handles commands when playing a playlist. We are using the deprecated
 * Playlist class since there is a bug in the current jvlc's implementation of
 * MediaList. See JVLC Playlist issue:
 * http://forum.videolan.org/viewtopic.php?f=2&t=49612&start=0&st=0&sk=t&sd=a
 * 
 * @author Marc
 * 
 */
// See class description for explanation of suppression of deprecation warning
@SuppressWarnings("deprecation")
public class VlcPlaylistCommandHandler extends MediaCommandHandler {
  private static final int MAX_ITERATION = 8;
  private static Logger LOGGER = Logger.getLogger(VlcPlaylistCommandHandler.class.getName());
  private static Playlist playlist;
  // Time to fast forward/rewind by (in milliseconds).
  private static final float POSITION_INCREMENT_TIME = 12 * 1000;

  private static VlcPlaylistCommandHandler instance = null;
  private static JVLC jvlc;
  private static int idOfFirstSong = -1;
  private String mrlOfLastMediaUpdate = "";

  // Private constructor to prevent instantiation
  private VlcPlaylistCommandHandler() {
  }

  public static VlcPlaylistCommandHandler instance(Playlist mediaPlaylist, int idOfOriginal,
      JVLC jvlcInstance) {
    playlist = mediaPlaylist;
    idOfFirstSong = idOfOriginal;
    jvlc = jvlcInstance;
    if (instance == null) {
      instance = new VlcPlaylistCommandHandler();
    }
    return instance;
  }

  @Override
  public void closeMedia() {
    stopMedia();
  }

  @Override
  public void pauseMedia() {
    try {
      playlist.togglePause();
    } catch (VLCException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  @Override
  public void playMedia() {
    mrlOfLastMediaUpdate = "";
    try {
      if (idOfFirstSong == -1) {
        playlist.play();
      } else {
        playlist.play(idOfFirstSong, new String[] {});
        idOfFirstSong = -1;
      }

    } catch (VLCException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  @Override
  public void stopMedia() {
    try {
      // Note: We DON'T do a playlist.stop() here since other operations (such
      // as rewind) will crash the jvm.
      if (!mediaIsPaused) {
        playlist.getMediaInstance().pause();
      }
      
      playlist.getMediaInstance().setPosition(0);
      for (int i = 0; playlist.isRunning() && i < 5; i++) {
        sleep(100);
      }
    } catch (VLCException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public static Playlist getPlaylist(JVLC jvlc) {
    if (playlist == null) {
      playlist = new Playlist(jvlc);
    }
    return playlist;
  }

  @Override
  public void fastForward() {
    MediaPlayer player = playlist.getMediaInstance();
    float newPosition = player.getPosition() + (POSITION_INCREMENT_TIME / player.getLength());
    player.setPosition(newPosition);
  }

  @Override
  public void rewind() {
    MediaPlayer player = playlist.getMediaInstance();
    float newPosition = player.getPosition() - (POSITION_INCREMENT_TIME / player.getLength());
    if (newPosition < 0) {
      newPosition = 0;
    }
    player.setPosition(newPosition);
  }

  @Override
  public void fastForwardLong() {
    try {
      int currentIndex = playlist.getCurrentIndex();
      playlist.next();
      waitForNextSong(currentIndex);
      MediaInfoUpdater.instance().sendMediaUpdate(getNewMediaInfo());
    } catch (VLCException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private void waitForNextSong(int currentIndex) throws VLCException {
    if (playlist.itemsCount() > 1) {
      for (int i = 0; (playlist.getCurrentIndex() == currentIndex) && i < MAX_ITERATION; i++) {
        sleep(100);
      }
    }
  }

  @Override
  public void rewindLong() {

    try {
      int currentIndex = playlist.getCurrentIndex();
      playlist.prev();
      waitForNextSong(currentIndex);
      MediaInfoUpdater.instance().sendMediaUpdate(getNewMediaInfo());
    } catch (VLCException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  @Override
  protected void setVolume(int volume) {
    Audio audio = new Audio(jvlc);
    audio.setVolume(volume);
  }

  @Override
  protected int getVolume() {
    Audio audio = new Audio(jvlc);
    return audio.getVolume();
  }

  @Override
  protected void toggleMute() {
    Audio audio = new Audio(jvlc);
    audio.toggleMute();

  }

  private void sleep(long time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }
  @Override
  public MediaMetaInfo getNewMediaInfo() {
    try {
      if (!playlist.isRunning()) {
        return null;
      }
    } catch (VLCException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(),e);
      return null;
    }  
    
    MediaDescriptor media = playlist.getMediaInstance().getMedia();
    if (media == null || media.getMrl() == null) {
      return null;
    }
    String mediaMrl = media.getMrl();
    if (mediaMrl.equals(mrlOfLastMediaUpdate)) {
      return null;
    }
    String artworkUrl = media.getMeta(libvlc_meta_t.libvlc_meta_ArtworkURL);
    
    MP3File mp3 = new MP3File(new File(mediaMrl));
    MediaMetaInfo fileInfo = PlayerUtil.getSongMetaInfo(mp3);
    useVlcMetaInfoIfNull(fileInfo, media);
    
    byte[] imageData = PlayerUtil.extractEmbeddedImageData(mp3);
    if (imageData == null && artworkUrl != null && artworkUrl.startsWith("file://")) {
      // Try to get the image from file.
      imageData = PlayerUtil.extractImageArtworkFromFile(artworkUrl);
    }
    if (imageData == null) {
      // In windows, a folder.jpg file often contains the album art
      imageData = PlayerUtil.extractImageFromFolder(mediaMrl);
    }
    
    fileInfo.setImage(imageData);
    if (imageData == null) {
      LOGGER.info("Image data is null");
    }

    mrlOfLastMediaUpdate  = mediaMrl;
    return fileInfo;
  }

  

  private void useVlcMetaInfoIfNull(MediaMetaInfo fileInfo, MediaDescriptor media) {
    // Try to use the vlc data if we wern't able to get the data any other way.
    // We use vlc's data as a backup only since it takes a little while for vlc
    // to update this data, resulting in the user first being presented with
    // partial data (for example, no album art, no album name, and a song name
    // that = file name.mp3), and then receives the correct info a few seconds
    // later.
    if (fileInfo.getTitle() == null) {
      fileInfo.setTitle(media.getMeta(libvlc_meta_t.libvlc_meta_Title));
    }

    if (fileInfo.getArtist() == null) {
      fileInfo.setTitle(media.getMeta(libvlc_meta_t.libvlc_meta_Artist));
    }
    
    if (fileInfo.getAlbum() == null) {
      fileInfo.setTitle(media.getMeta(libvlc_meta_t.libvlc_meta_Album));
    }
  }

}
