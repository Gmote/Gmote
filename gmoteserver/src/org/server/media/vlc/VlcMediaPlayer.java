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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.gmote.common.FileInfo;
import org.gmote.common.FileInfo.FileType;
import org.gmote.common.Protocol.Command;
import org.gmote.common.media.MediaMetaInfo;
import org.gmote.server.PlatformUtil;
import org.gmote.server.ServerUtil;
import org.gmote.server.StringEncrypter;
import org.gmote.server.media.MediaCommandHandler;
import org.gmote.server.media.MediaInfoUpdater;
import org.gmote.server.media.MediaPlayerInterface;
import org.gmote.server.media.UnsupportedCommandException;
import org.gmote.server.settings.DefaultSettings;
import org.gmote.server.settings.DefaultSettingsEnum;
import org.gmote.server.settings.SupportedFiletypeSettings;
import org.videolan.jvlc.Audio;
import org.videolan.jvlc.JVLC;
import org.videolan.jvlc.LoggerMessage;
import org.videolan.jvlc.LoggerVerbosityLevel;
import org.videolan.jvlc.MediaDescriptor;
import org.videolan.jvlc.MediaPlayer;
import org.videolan.jvlc.Playlist;
import org.videolan.jvlc.VLCException;
import org.videolan.jvlc.Video;
import org.videolan.jvlc.event.MediaPlayerListener;

@SuppressWarnings("deprecation")
public class VlcMediaPlayer implements MediaPlayerInterface {
  private static final int MAX_DELAY_ATTEMPTS = 5;
  private static Logger LOGGER = Logger.getLogger(VlcMediaPlayer.class.getName());
  private static final String VLC_LOG_NAME = "/logs/vlc.log";

  private static final long VIDEO_DELAY_TIMEOUT = 10 * 1000;

  // VLC Player.
  JVLC jvlc;
  MediaCommandHandler commandHandler;
  VlcMediaPlayerFrame mediaPlayerFrame;

  // Player state.
  private boolean playingVideo = false;

  public VlcMediaPlayer() {
  }

  @Override
  public void initialise(String[] arguments) {
    // Construct a vlc media player object.
    if (PlatformUtil.isLinux()) {
      String[] param = new String[] { "-vvv", "--fullscreen", "--extraintf=hotkeys",
          "--no-plugins-cache" };
      jvlc = new JVLC(param);
    } else {
      String vlcPath = ServerUtil.instance().findInstallDirectory().replaceAll("/", "\\\\") + "\\bin\\vlc";
      for (String arg : arguments) {
        String[] argSplit = arg.split("=");
        if (argSplit.length == 2) {
          if (argSplit[0].equalsIgnoreCase("vlcpath")) {
            vlcPath = argSplit[1];
          }
        }
      }
      
      String[] param = new String[] { "-vvv", "--plugin-path=" + vlcPath + "\\plugins",
      "--no-plugins-cache" };
      
      try {
        jvlc = new JVLC(param);
     } catch (UnsatisfiedLinkError e) {
        String message = "Error while gmote tried to start vlc. Make sure that vlc is installed and in your system PATH variable. Please see the logs for more details or visit www.gmote.org/faq : " + e.getMessage();
        LOGGER.log(Level.SEVERE, message, e);
        JOptionPane.showMessageDialog(null, message);
        System.exit(1);
      }
    }
    
    if (Boolean.parseBoolean(DefaultSettings.instance().getSetting(DefaultSettingsEnum.LOG_VLC))) {
      jvlc.setLogVerbosity(LoggerVerbosityLevel.INFO);
    } else {
      jvlc.setLogVerbosity(LoggerVerbosityLevel.WARNING);
    }
    jvlc.setLogVerbosity(LoggerVerbosityLevel.DEBUG);
    writeVlcLog();
    
  }
  
  @Override
  public synchronized void controlPlayer(Command command) throws UnsupportedCommandException {

    // Delegate to the appropriate handler.
    if (commandHandler != null) {

      commandHandler.executeCommand(command);

      if (command == Command.CLOSE) {
        doClose();
      }
    }
  }

  /**
   * Closes the media. Synchronized since we can call this method by receiving
   * an request from the client or by receiving an 'end reached' event on the
   * media listener.
   */
  private synchronized void doClose() {

    if (playingVideo && mediaPlayerFrame != null) {
      mediaPlayerFrame.closeFrame();
      
    }
    playingVideo = false;
    Runtime.getRuntime().gc();
  }

  /**
   * Launches a media file in the media player.
   * 
   * @throws UnsupportedEncodingException
   * @throws UnsupportedCommandException
   * 
   * @see {@link VlcPlaylistCommandHandler} for information about deprecation
   *      warning
   */
  @Override
  public synchronized void runFile(FileInfo fileInfo)
      throws UnsupportedEncodingException, UnsupportedCommandException {

    // Stop the player if it is already playing
    MediaInfoUpdater.instance().setPlayerToPoll(null);
    if (commandHandler != null) {
      if (playingVideo) {
        controlPlayer(Command.CLOSE);
      } else {
        controlPlayer(Command.STOP);
      }
    }

    // Set the default volume.
    Audio audio = new Audio(jvlc);
    audio.setVolume(Integer.parseInt(DefaultSettings.instance().getSetting(
        DefaultSettingsEnum.VOLUME)));

    String fileName = fileInfo.getAbsolutePath();
    FileType fileType = fileInfo.getFileType();
    
    if (fileType == FileType.PLAYLIST || fileType == FileType.MUSIC) {
      runMusic(fileName, fileType);
    } else {
      // Setup the player with the current file to play.
      runMovie(fileName, fileType);
    }
  }
  
  @Override
  public MediaMetaInfo getNewMediaInfo() {
    if (commandHandler == null) {
      return null;
    }
    return commandHandler.getNewMediaInfo();
  }
  

  private void runMovie(String fileName, FileType fileType) throws UnsupportedCommandException {
    // Appends dvdsimple to the file name so that vlc will skip all of the dvd
    // menus.
    if (fileType == FileType.DVD_DRIVE) {
      fileName = "dvdsimple://" + fileName;
    }

    MediaDescriptor mediaDescriptor = new MediaDescriptor(jvlc, fileName);
    MediaPlayer player = mediaDescriptor.getMediaPlayer();

    commandHandler = VlcDefaultCommandHandler.instance(player, jvlc);
    commandHandler.setMediaIsOpen(true);
    
    if (!PlatformUtil.isLinux()) {
      // Setup the frame that the player will play in.
      mediaPlayerFrame = new VlcMediaPlayerFrame(this, player);
      // Make the window maximized.
      mediaPlayerFrame.createFullScreenWindow();
  
      // Tell VLC about the frame it will play in.
      jvlc.setVideoOutput(mediaPlayerFrame.getCanvas());
    }
    playingVideo = true;
    controlPlayer(Command.PLAY);

    long startTime = new Date().getTime();
    while (!player.hasVideoOutput() && new Date().getTime() - startTime < VIDEO_DELAY_TIMEOUT) {
      sleep(100);
    }
    if (!player.hasVideoOutput()) {
      controlPlayer(Command.CLOSE);
      LOGGER.log(Level.SEVERE, "Unable to launch video file");
    } else if (PlatformUtil.isLinux()){
    	Video video = new Video(jvlc);
    	video.setFullscreen(player, true);
    }
  }

  private void runMusic(String fileName, FileType fileType) throws UnsupportedCommandException {
    Playlist playList;
    // Special handling for playlists since there is a bug in jvlc related to
    // playlists.
    playList = new Playlist(jvlc);

    int idOfOriginal = -1;
    try {

      if (fileType == FileType.PLAYLIST) {
        playList.add(fileName, "Playlist");
        playList.next();
      } else {
        // Add all of the files of the directory in the playlist.
        File originalFile = new File(fileName);
        
        File[] allFilesInDirectory = originalFile.getParentFile().listFiles();
        if (DefaultSettings.instance().getSetting(DefaultSettingsEnum.SHUFFLE_SONGS)
            .equalsIgnoreCase("true")) {
          Collections.shuffle(Arrays.asList(allFilesInDirectory));
        }
        
        for (File file : allFilesInDirectory) {
          String name = file.getName();
          FileType type = SupportedFiletypeSettings.fileNameToFileType(name);

          if (type == FileType.MUSIC) {

            int id = playList.add(file.getAbsolutePath(), file.getName());
            if (originalFile.equals(file)) {
              idOfOriginal = id;
            }
          }
        }
      }
    } catch (VLCException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
    commandHandler = VlcPlaylistCommandHandler.instance(playList, idOfOriginal, jvlc);
    commandHandler.setMediaIsOpen(true);
    controlPlayer(Command.PLAY);
    try {
      for (int i = 0; i < MAX_DELAY_ATTEMPTS && idOfOriginal != -1
          && (playList.getCurrentIndex() != idOfOriginal); i++) {
        sleep(100);
      }
    } catch (VLCException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      throw new UnsupportedCommandException(e.getMessage());
    }

    // Wait until the song starts playing.
    sleep(100);
    try {
      long startTime = new Date().getTime();
      while (!playList.isRunning() && new Date().getTime() - startTime < VIDEO_DELAY_TIMEOUT) {
        sleep(100);
      }
    } catch (VLCException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
    
    
    MediaInfoUpdater.instance().sendMediaUpdate(getNewMediaInfo());
    MediaInfoUpdater.instance().setPlayerToPoll(this);

  }

  private void writeVlcLog() {

    if (Boolean.parseBoolean(DefaultSettings.instance().getSetting(DefaultSettingsEnum.LOG_VLC))) {
      Iterator<LoggerMessage> it = jvlc.getLogger().iterator();

      try {
        URL url = StringEncrypter.class.getResource(VLC_LOG_NAME);

        BufferedWriter writer = new BufferedWriter(new FileWriter(url.getPath().replaceAll("%20",
            " ")));

        while (it.hasNext()) {
          LoggerMessage message = it.next();
          writer.write(new Date() + " " + message.getMessage());
          writer.newLine();
        }
        writer.close();

      } catch (FileNotFoundException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }

  /**
   * Convenience function that simply logs the exception.
   * 
   * @param timeInMili
   */
  private void sleep(long timeInMili) {
    try {
      Thread.sleep(timeInMili);
    } catch (InterruptedException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  /**
   * Allows us to close media files automatically when they are done playing.
   * NOTE: We stopped using this class since we experienced this behavior: - If
   * we played a movie, closed it, played it again closed it... and did this
   * several times, the entire app would eventually crash. We would get the
   * following error in the logs: *** LibVLC Exception not handled: This object
   * event manager doesn't know about
   * 'libvlc_MediaPlayerPlaying,0C06F720,00000000' event observer Set a
   * breakpoint in 'libvlc_exception_not_handled' to debug. LibVLC Exception not
   * handled: This object event manager doesn't know about
   * 'libvlc_MediaPlayerPaused,0C06F720,00000000' event observer Set a
   * breakpoint in 'libvlc_exception_not_handled' to debug. LibVLC Exception not
   * handled: This object event manager doesn't know about
   * 'libvlc_MediaPlayerStopped,0C06F720,00000000' event observer Set a
   * breakpoint in 'libvlc_exception_not_handled' to debug. LibVLC Exception not
   * handled: This object event manager doesn't know about
   * 'libvlc_MediaPlayerForward,0C06F720,00000000' event observer
   * 
   * @author Marc
   * 
   */
  public class PlayerListener implements MediaPlayerListener {

    @Override
    public void endReached(MediaPlayer mediaPlayer) {

    }

    @Override
    public void errorOccurred(MediaPlayer mediaPlayer) {
      LOGGER.severe("errorOccurred was called");
    }

    @Override
    public void paused(MediaPlayer mediaPlayer) {
    }

    @Override
    public void playing(MediaPlayer mediaPlayer) {
    }

    @Override
    public void positionChanged(MediaPlayer mediaPlayer) {
    }

    @Override
    public void stopped(MediaPlayer mediaPlayer) {
    }

    @Override
    public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
    }

  }

  @Override
  public List<FileInfo> getBaseLibraryFiles() {
    return null;
  }

  @Override
  public List<FileInfo> getLibrarySubFiles(FileInfo fileInfo) {
    return null;
  }

  @Override
  public boolean isRunning() {
    return true;
  }

}
