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

import org.gmote.common.Protocol.Command;
import org.gmote.common.media.MediaMetaInfo;

public abstract class MediaCommandHandler {
  
  //Time to fast forward/rewind by (in seconds).
  protected static final int POSITION_INCREMENT_SEC = 12;
  protected static final int LONG_POSITION_INCREMENT_SEC = 10 * 60;
  
  protected boolean mediaIsOpen = false; 
  protected boolean mediaIsPaused = false;
  
  public void executeCommand(Command command) throws UnsupportedCommandException {
    if (!isMediaOpen()) {
      // Exit if the media has been closed or not started yet.
      return;
    }
    
    if (command == Command.PLAY) {
      playMedia();
      mediaIsPaused = false;
    } else if (command == Command.PAUSE) {
      if (!isMediaPaused()) {
        pauseMedia();
        mediaIsPaused = true;
      }
    } else if (command == Command.STOP) {
      stopMedia();
      mediaIsPaused = true;
    } else if (command == Command.REWIND) {
      rewind();
    } else if (command == Command.FAST_FORWARD) {
      fastForward();
    } else if (command == Command.REWIND_LONG) {
      rewindLong();
      mediaIsPaused = false;
    } else if (command == Command.FAST_FORWARD_LONG) {
      fastForwardLong();
      mediaIsPaused = false;
    } else if (command == Command.VOLUME_UP || command == Command.VOLUME_DOWN) {
      int currentVolume = getVolume();
      int newVolume = PlayerUtil.computeNewVolume(currentVolume, command);
      setVolume(newVolume);

    } else if (command == Command.MUTE) {
      toggleMute();
    } else if (command == Command.UNMUTE) {
      toggleMute();
    } else if (command == Command.CLOSE) {
      closeMedia();
      reset();
    }

  }

  
  public boolean isMediaOpen() {
    return mediaIsOpen;
  }
  
  public boolean isMediaPlayerOpen() {
    return true;
  }
  
  protected boolean isMediaPaused() {
    return mediaIsPaused;
  }

  public void setMediaIsOpen(boolean value) {
    mediaIsOpen = value;
  }
  
  private void reset() {
    mediaIsOpen = false;
    mediaIsPaused = false;
  }
  
  public abstract MediaMetaInfo getNewMediaInfo();

  /*
	 * Media player specific commands. Subclasses should override these methods
	 * and implement the functionality
	 */
  
  protected void closeMedia() throws UnsupportedCommandException {
	  throw new UnsupportedCommandException();
  }

  protected void stopMedia() throws UnsupportedCommandException {
	  throw new UnsupportedCommandException();
  }

  protected void rewind() throws UnsupportedCommandException {
	  throw new UnsupportedCommandException();
  }

  protected void fastForward() throws UnsupportedCommandException {
	  throw new UnsupportedCommandException();
  }

  protected void pauseMedia() throws UnsupportedCommandException {
	  throw new UnsupportedCommandException();
  }

  protected void playMedia() throws UnsupportedCommandException {
	  throw new UnsupportedCommandException();
  }

  protected void rewindLong() throws UnsupportedCommandException {
	  throw new UnsupportedCommandException();
  }

  protected void fastForwardLong() throws UnsupportedCommandException {
	  throw new UnsupportedCommandException();
  }
  
  protected void setVolume(int volume) throws UnsupportedCommandException {
	  throw new UnsupportedCommandException();
  }
  
  protected int getVolume() throws UnsupportedCommandException {
	  throw new UnsupportedCommandException();
  }
  
  protected void toggleMute() throws UnsupportedCommandException {
	  throw new UnsupportedCommandException();
  }
  
}
