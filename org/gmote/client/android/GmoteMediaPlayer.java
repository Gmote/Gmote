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

package org.gmote.client.android;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import org.gmote.common.Protocol.Command;
import org.gmote.common.media.MediaMetaInfo;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Handles media player events.
 */
public class GmoteMediaPlayer {
  
  private static final int MAX_RECONNECT_ATTEMPTS = 4;
  private static final int REWIND_FF_JUMP = 15000;
  // Messages that are sent out by this class.
  public static final int BUFFERING_UPDATE = 1;
  public static final int MEDIA_PLAYER_ERROR = 2;
  public static final int MEDIA_INFO_UPDATE = 3;
  public static final int SESSION_ERROR = 4;
  public static final int MEDIA_DURATION_UPDATE = 5;
  public static final int PREPARING_MEDIA = 6;
  
  private static final int VOLUME_UNIT = 1;
  private static final int MAX_VOLUME = 15;
  private int volume = 10;
  private int errorRetryCount = 0;
  private boolean inErrorState = false;
  
  // Use two media players, one for playing, the other for buffering the next
  // song. (note: advanced buffering not implemented yet. We should do it as
  // soon as we can since it will improve playlist performance when over 3G).
  List<MediaPlayer> mediaPlayers = new ArrayList<MediaPlayer>();

  private List<String> songs;
  private int songIndex;
  private Handler updateListener;
  private MediaPlayer activePlayer;

  boolean muted = false;

  public GmoteMediaPlayer(Handler updateListener) {
    mediaPlayers.add(createMediaPlayer());
    this.updateListener = updateListener;
  }

  private MediaPlayer createMediaPlayer() {
    MediaPlayer mp = new MediaPlayer();
    mp.setOnBufferingUpdateListener(new BufferingListener());
    mp.setOnCompletionListener(new CompletionListener());
    mp.setOnErrorListener(new ErrorListener());
    mp.setOnPreparedListener(new PreparedListener());
    mp.setOnSeekCompleteListener(new SeekCompleteListener());
    mp.setVolume(volume, volume);
    return mp;
  }

  public synchronized void handleCommand(Command command) {
    if (inErrorState) {
      Log.i(ActivityUtil.DEBUG_TAG, "Ignoring gmote media player command due to in error state");
      return;
    }
    MediaPlayer mediaPlayer = getActivePlayer();
    if (mediaPlayer == null) {
      return;
    }

    if (command == Command.FAST_FORWARD) {
      fastForward();
    } else if (command == Command.REWIND) {
      rewind();
    } else if (command == Command.FAST_FORWARD_LONG) {
      nextSong();
    } else if (command == Command.REWIND_LONG) {
      previousSong();
    } else if (command == Command.PAUSE) {
      pause();
    } else if (command == Command.PLAY) {
      play();
    } else if (command == Command.STOP) {
      stop();
    } else if (command == Command.VOLUME_DOWN) {
      lowerVolume();
    } else if (command == Command.VOLUME_UP) {
      increaseVolume();
    } else if (command == Command.MUTE) {
      mute();
    }
  }

  private void increaseVolume() {
    volume = Math.min(MAX_VOLUME, volume + VOLUME_UNIT);
    activePlayer.setVolume(volume, volume);
    muted = false;
    Log.i(ActivityUtil.DEBUG_TAG, "Volume set: " + volume);
  }

  private void lowerVolume() {
    volume = Math.max(0, volume - VOLUME_UNIT);
    activePlayer.setVolume(volume, volume);
    muted = false;
    Log.i(ActivityUtil.DEBUG_TAG, "Volume set: " + volume);
  }

  private void mute() {
    if (muted) {
      activePlayer.setVolume(volume, volume);
    } else {
      activePlayer.setVolume(0, 0);
    }
    muted = (muted == false);
  }

  /**
   *
   * @param songs
   */
  public synchronized void playSongs(List<String> songs, int startingSongIndex) {
    inErrorState = false;
    this.songs = songs;
    this.songIndex = startingSongIndex;
    if (activePlayer != null) {
      activePlayer.reset();
    }
    fetchSong();
  }

  private void nextSong() {
    songIndex = (songIndex + 1) % songs.size();
    activePlayer.reset();
    fetchSong();
  }

  private void previousSong() {
    songIndex = (songIndex - 1) % songs.size();
    activePlayer.reset();
    fetchSong();
  }

  private void fastForward() {
    activePlayer.seekTo(Math.min(activePlayer.getCurrentPosition() + REWIND_FF_JUMP, activePlayer.getDuration()));
  }

  private void rewind() {
    activePlayer.seekTo(Math.max(activePlayer.getCurrentPosition() - REWIND_FF_JUMP, 0));
  }

  private void pause() {
    activePlayer.pause();
  }

  private synchronized void play() {
    activePlayer.start();
  }

  private void stop() {
    activePlayer.seekTo(0);
    activePlayer.pause();
  }

  public synchronized int getSongPosition() {
    if (activePlayer != null) {
      return activePlayer.getCurrentPosition();
    } else {
      return 0;
    }

  }

  private synchronized String getSongName() {
    String strUrl = URLDecoder.decode(songs.get(songIndex));
    URL url;
    try {
      url = new URL(strUrl);
    } catch (MalformedURLException e) {
      Log.e(ActivityUtil.DEBUG_TAG, e.getMessage(), e);
      return null;
    }
    String nameAndPath = url.getFile();
    nameAndPath = nameAndPath.substring("/files/".length());
    return nameAndPath;
  }

  private synchronized void fetchSong() {
    inErrorState = false;
    setActivePlayer(null);

    MediaPlayer player = mediaPlayers.get(0);
    player.reset();
    try {
      String sessionId = getSessionId();
      if (sessionId == null) {
        String message = "FetchSong error. No session id. (not connected to server?)";
        Log.i(ActivityUtil.DEBUG_TAG, message);
        updateListener.sendMessage(Message.obtain(updateListener, SESSION_ERROR, "Error: Can't request a file without an active session. Please try to first connect to the server by hitting browse."));
        return;
      }
      player.setDataSource(songs.get(songIndex) + "?sessionId=" + sessionId);
    } catch (IllegalArgumentException e) {
      Log.e(ActivityUtil.DEBUG_TAG, e.getMessage(), e);
    } catch (IllegalStateException e) {
      Log.e(ActivityUtil.DEBUG_TAG, e.getMessage(), e);
    } catch (IOException e) {
      Log.e(ActivityUtil.DEBUG_TAG, e.getMessage(), e);
    }
    updateListener.sendEmptyMessage(PREPARING_MEDIA);
    player.prepareAsync();

  }

  private String getSessionId() {
    Remote remote = Remote.getInstance();
    return remote.getSessionId();
  }

  private synchronized MediaPlayer getActivePlayer() {
    return activePlayer;
  }

  private synchronized void setActivePlayer(MediaPlayer mp) {
    activePlayer = mp;
  }

  /**
   * Utility function to format a duration into an easy to diaplay min:sec.
   */
  public static String formatTime(int duration) {
    int durationInSec = duration / 1000;
    int minutes = durationInSec / (60 );
    int seconds = durationInSec - (minutes * 60);
    Formatter formatter = new Formatter();
    return minutes + ":" + formatter.format("%02d", seconds);
  }

  private class BufferingListener implements MediaPlayer.OnBufferingUpdateListener {

    public void onBufferingUpdate(MediaPlayer mp, int percent) {
      Log.i(ActivityUtil.DEBUG_TAG, "BufferUpdate: "  + percent);
      updateListener.sendMessage(Message.obtain(updateListener, BUFFERING_UPDATE, new Integer(percent)));
    }
  }

  // When media file is ready for playback.
  private class PreparedListener implements MediaPlayer.OnPreparedListener {
    
    public void onPrepared(MediaPlayer mp) {
      Log.i(ActivityUtil.DEBUG_TAG, "On Prepared()");
      synchronized (GmoteMediaPlayer.this) {
        setActivePlayer(mp);
        MediaMetaInfo metaInfo = new MediaMetaInfo(getSongName(),null, null, null, false);
        updateListener.sendMessage(Message.obtain(updateListener, MEDIA_INFO_UPDATE, metaInfo));
        updateListener.sendMessage(Message.obtain(updateListener, MEDIA_DURATION_UPDATE, mp.getDuration()));
        play();
        errorRetryCount = 0;
        inErrorState = false;
      }
      
    }
  }

  // Song done playing.
  private class CompletionListener implements MediaPlayer.OnCompletionListener {
    public synchronized void onCompletion(MediaPlayer mp) {
      Log.i(ActivityUtil.DEBUG_TAG, "On Completion()");
      synchronized (GmoteMediaPlayer.this) {
        nextSong();
      }
    }
  }

  private class ErrorListener implements MediaPlayer.OnErrorListener {
    
    
    public boolean onError(MediaPlayer mp, int what, int extra) {
      Log.i(ActivityUtil.DEBUG_TAG, "MediaListener: Error: " + what + "  " + extra);
      synchronized (GmoteMediaPlayer.this) {
        inErrorState = true;
      }
      new Thread(new ErrorRecoveryRunnable(mp, what, extra)).start();
      return true;
    }
  }

  private class SeekCompleteListener implements MediaPlayer.OnSeekCompleteListener {

    public void onSeekComplete(MediaPlayer mp) {
      Log.i(ActivityUtil.DEBUG_TAG, "SeekComplete");
    }
  }

  public void setMediaPlayerListener(Handler localMediaPlayerListener) {
    this.updateListener = localMediaPlayerListener; 
  }
  
  public class ErrorRecoveryRunnable implements Runnable {

    MediaPlayer mp;
    int what; 
    int extra;
    public ErrorRecoveryRunnable(MediaPlayer mp, int what, int extra) {
      this.mp = mp;
      this.what = what;
      this.extra = extra;
    }

    public void run() {
      recoverFromError(mp, what, extra);
    }

  }

  private synchronized void recoverFromError(MediaPlayer mp, int what, int extra) {
    if (errorRetryCount < 1) {
      updateListener.sendMessage(Message.obtain(updateListener, MEDIA_PLAYER_ERROR,
          "Trying to reconnect..."));

      boolean isConnected = Remote.getInstance().isConnected();
      for (int i = 0; i < MAX_RECONNECT_ATTEMPTS && !isConnected; i++) {
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          Log.e(ActivityUtil.DEBUG_TAG, e.getMessage(), e);
        }
        isConnected = Remote.getInstance().connect(true);
        if (!isConnected) {
          updateListener.sendMessage(Message.obtain(updateListener, MEDIA_PLAYER_ERROR,
          "Reconnect attempt " + (i + 2) + " of " + MAX_RECONNECT_ATTEMPTS));
        }
      }

      synchronized (GmoteMediaPlayer.this) {
        mp.reset();
        if (isConnected) {
          fetchSong();
        }
      }
      
      if (!isConnected) {
        updateListener.sendMessage(Message.obtain(updateListener, MEDIA_PLAYER_ERROR,
            "An error occurred: " + what + "  " + extra));
      }
      
      errorRetryCount++;
    } else {
      updateListener.sendMessage(Message.obtain(updateListener, MEDIA_PLAYER_ERROR,
          "An error occurred: " + what + "  " + extra));
    }
  }
}
