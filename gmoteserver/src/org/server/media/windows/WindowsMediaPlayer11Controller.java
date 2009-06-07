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

package org.gmote.server.media.windows;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.List;

import org.gmote.common.FileInfo;
import org.gmote.common.Protocol.Command;
import org.gmote.common.media.MediaMetaInfo;
import org.gmote.server.ServerUtil;
import org.gmote.server.media.MediaPlayerInterface;
import org.gmote.server.media.PlayerUtil;
import org.gmote.server.media.UnsupportedCommandException;


/**
 * Handles specific commands to send to Windows Media Player 11.
 * @author Marc
 *
 */
public class WindowsMediaPlayer11Controller implements MediaPlayerInterface {

  Robot robot;
  private boolean isRunning = false;
  
  public WindowsMediaPlayer11Controller() {
    try {
      robot = new Robot();
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  public void controlPlayer(Command command) throws UnsupportedCommandException {
    if (command == Command.PAUSE || command == Command.PLAY) {
      // Pause and Play
      robot.keyPress(KeyEvent.VK_CONTROL);
      robot.keyPress(KeyEvent.VK_P);
      robot.keyRelease(KeyEvent.VK_CONTROL);
    } else if (command == Command.STOP) {
      // STOP
      robot.keyPress(KeyEvent.VK_CONTROL);
      robot.keyPress(KeyEvent.VK_S);
      robot.keyRelease(KeyEvent.VK_CONTROL);
    } else if (command == Command.VOLUME_DOWN) {
      // Volume Down
      robot.keyPress(KeyEvent.VK_F8);
    } else if (command == Command.VOLUME_UP) {
      // Volume Up
      robot.keyPress(KeyEvent.VK_F9);
    } else if (command == Command.MUTE || command == Command.UNMUTE) {
      // Mute and Unmute
      robot.keyPress(KeyEvent.VK_F7);
    } else if (command == Command.REWIND) {
      // Skip Backward
      robot.keyPress(KeyEvent.VK_LEFT);
    } else if (command == Command.FAST_FORWARD) {
      // Skip Forward
      robot.keyPress(KeyEvent.VK_RIGHT);
    } else if (command == Command.CLOSE) {
      robot.keyPress(KeyEvent.VK_ALT);
      robot.keyPress(KeyEvent.VK_F4);
      robot.keyRelease(KeyEvent.VK_ALT);
      robot.waitForIdle();
      // The first time took it out of full screen. Now, close it.
      robot.keyPress(KeyEvent.VK_ALT);
      robot.keyPress(KeyEvent.VK_F4);
      robot.keyRelease(KeyEvent.VK_ALT);
      isRunning = false;
    } else {
      throw new UnsupportedCommandException("The following command is not currently supported by the Windows Media Player client: " + command.name());
    }

  }

  public void fullScreen() {
    robot.delay(5000);
    robot.waitForIdle();
    robot.keyPress(KeyEvent.VK_ALT);
    robot.keyPress(KeyEvent.VK_TAB);
    robot.waitForIdle();
    robot.keyRelease(KeyEvent.VK_ALT);
    robot.waitForIdle();
    robot.keyPress(KeyEvent.VK_F11);
    robot.waitForIdle();

  }

  @Override
  public void runFile(FileInfo fileInfo) {
    // For now, assume that media player is the default player.
    ServerUtil.instance().startFileInDefaultApplication(fileInfo.getAbsolutePath());
    fullScreen();
    isRunning = true;
  }

  @Override
  public MediaMetaInfo getNewMediaInfo() {
    return new MediaMetaInfo("", "Windows Media Player", "", PlayerUtil
        .loadImage("dvd.png"), false);
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
  public void initialise(String[] arguments) {
    
  }

  @Override
  public boolean isRunning() {
    return isRunning;
  }
}
