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
import org.gmote.server.media.basic.CommonOperations;

/**
 * Simple player that simply launches files in the operating system's default
 * player.
 * 
 * @author Marc Stogaitis
 */
public class WindowsPdfPlayer implements MediaPlayerInterface {
  private static final Logger LOGGER = Logger.getLogger(WindowsPdfPlayer.class.getName());
  private int openCount = 0;
  
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
        openCount = Math.max(0, openCount - 1);
      } else if (command == Command.PLAY) {
        // Will start a slideshow in pdf viewer.
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_L);
        robot.keyRelease(KeyEvent.VK_L);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        openCount = 2;
      } else if (command == Command.STOP) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_EQUALS);
        robot.keyRelease(KeyEvent.VK_EQUALS);
        robot.keyRelease(KeyEvent.VK_CONTROL);
      } else if (command == Command.PAUSE) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_MINUS);
        robot.keyRelease(KeyEvent.VK_MINUS);
        robot.keyRelease(KeyEvent.VK_CONTROL);
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
    return new MediaMetaInfo("", "PDF Viewer", "", PlayerUtil
        .loadImage("file.png"), false);

  }

  public void initialise(String[] arguments) {

  }

  public void runFile(FileInfo fileInfo) throws UnsupportedEncodingException,
      UnsupportedCommandException {
    ServerUtil.instance().startFileInDefaultApplication(fileInfo.getAbsolutePath());
    openCount = 1;
  }

  public boolean isRunning() {
    return openCount > 0;
  }

}
