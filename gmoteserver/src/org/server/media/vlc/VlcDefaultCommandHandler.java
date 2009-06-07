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

import org.gmote.common.media.MediaMetaInfo;
import org.gmote.server.media.MediaCommandHandler;
import org.videolan.jvlc.Audio;
import org.videolan.jvlc.JVLC;
import org.videolan.jvlc.MediaPlayer;


public class VlcDefaultCommandHandler extends MediaCommandHandler {

  private static MediaPlayer player;
  private static JVLC jvlc;
  
  private static VlcDefaultCommandHandler instance = null;
  
  // Private constructor to prevent instantiation
  private VlcDefaultCommandHandler() {}
  
  public static VlcDefaultCommandHandler instance(MediaPlayer mediaPlayer, JVLC jvlcInstance) {
    player = mediaPlayer;
    jvlc = jvlcInstance;
    if (instance == null) {
      instance = new VlcDefaultCommandHandler();
    }
    return instance;
  }
  
  /* (non-Javadoc)
   * @see com.r3mote.server.media.VlcCommandHandler#closeMedia(boolean)
   */
  protected void closeMedia() {
    player.stop();
    //player.getMediaDescriptor().release();
  }

  /* (non-Javadoc)
   * @see com.r3mote.server.media.VlcCommandHandler#stopMedia()
   */
  protected void stopMedia() {
    player.setPosition(0);
    pauseMedia();
  }

  /* (non-Javadoc)
   * @see com.r3mote.server.media.VlcCommandHandler#rewind()
   */
  protected void rewind() {
    rewindByTime(POSITION_INCREMENT_SEC * 1000);
  }

  private void rewindByTime(float time) {
    float newPosition = player.getPosition() - (time / player.getLength());
    if (newPosition < 0) {
      newPosition = 0;
    }
    player.setPosition(newPosition);
  }

  /* (non-Javadoc)
   * @see com.r3mote.server.media.VlcCommandHandler#fastForward()
   */
  protected void fastForward() {
    fastForwardByTime(POSITION_INCREMENT_SEC * 1000);
  }

  private void fastForwardByTime(float time) {
    float newPosition = player.getPosition() + (time / player.getLength());
    // The position is a number between 0 and 1.
    player.setPosition(newPosition);
  }

  /* (non-Javadoc)
   * @see com.r3mote.server.media.VlcCommandHandler#pauseMedia()
   */
  protected void pauseMedia() {
    player.pause();
  }

  /* (non-Javadoc)
   * @see com.r3mote.server.media.VlcCommandHandler#playMedia()
   */
  protected void playMedia() {
    player.play();
  }

  @Override
  protected void fastForwardLong() {
    fastForwardByTime(LONG_POSITION_INCREMENT_SEC * 1000);
  }

  @Override
  protected void rewindLong() {
    rewindByTime(LONG_POSITION_INCREMENT_SEC * 1000);
  }

  protected MediaPlayer getPlayer() {
    return player;
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

  @Override
  public MediaMetaInfo getNewMediaInfo() {
    // TODO(mstogaitis): Might want to pass the video name here.
    return null;
  }


}
