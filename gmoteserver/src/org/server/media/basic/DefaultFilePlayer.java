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

package org.gmote.server.media.basic;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gmote.common.FileInfo;
import org.gmote.common.Protocol.Command;
import org.gmote.common.media.MediaMetaInfo;
import org.gmote.server.ServerUtil;
import org.gmote.server.media.MediaPlayerInterface;
import org.gmote.server.media.PlayerUtil;
import org.gmote.server.media.UnsupportedCommandException;

/**
 * Simple player that simply launches files in the operating system's default
 * player.
 * 
 * @author Marc Stogaitis
 */
public class DefaultFilePlayer implements MediaPlayerInterface {
  private static final Logger LOGGER = Logger.getLogger(DefaultFilePlayer.class.getName());
  private boolean isRunning = false;
  public void controlPlayer(Command command) throws UnsupportedCommandException {
    try {
      Robot robot = new Robot();

      if (command == Command.FAST_FORWARD || command == Command.FAST_FORWARD_LONG) {
        robot.keyPress(KeyEvent.VK_RIGHT);
        robot.keyRelease(KeyEvent.VK_RIGHT);
      } else if (command == Command.REWIND || command == Command.REWIND_LONG) {
        robot.keyPress(KeyEvent.VK_LEFT);
        robot.keyRelease(KeyEvent.VK_LEFT);
      } else if (command == Command.CLOSE) {
        CommonOperations.sendCloseCommand();
        isRunning = false;
      } else if (command == Command.PLAY) {
        // Maximizes a window
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_SPACE);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.keyRelease(KeyEvent.VK_SPACE);
        robot.keyPress(KeyEvent.VK_X);
        robot.keyRelease(KeyEvent.VK_X);
        
      }

    } catch (AWTException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }

  }

  public List<FileInfo> getBaseLibraryFiles() {
    return null;
  }

  public List<FileInfo> getLibrarySubFiles(FileInfo fileInfo) {
    return null;
  }

  public MediaMetaInfo getNewMediaInfo() {
    return new MediaMetaInfo("", "File Viewer", "", PlayerUtil
        .loadImage("file.png"), false);
  }

  public void initialise(String[] arguments) {

  }

  public void runFile(FileInfo fileInfo) throws UnsupportedEncodingException,
      UnsupportedCommandException {
    ServerUtil.instance().startFileInDefaultApplication(fileInfo.getAbsolutePath());
    isRunning = true;
  }

  public boolean isRunning() {
    return isRunning;
  }

}
