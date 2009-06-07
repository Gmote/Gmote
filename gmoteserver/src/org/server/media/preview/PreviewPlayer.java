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

package org.gmote.server.media.preview;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gmote.common.FileInfo;
import org.gmote.common.FileInfo.FileType;
import org.gmote.common.Protocol.Command;
import org.gmote.common.media.MediaMetaInfo;
import org.gmote.server.ServerUtilMac;
import org.gmote.server.media.MediaPlayerInterface;
import org.gmote.server.media.PlayerUtil;
import org.gmote.server.media.UnsupportedCommandException;
import org.gmote.server.settings.SupportedFiletypeSettings;

/**
 * @author mimi
 *
 */
public class PreviewPlayer implements MediaPlayerInterface {
  private static final Logger LOGGER = Logger.getLogger(PreviewPlayer.class.getName());

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
          robot.keyPress(KeyEvent.VK_ESCAPE);
          robot.keyPress(KeyEvent.VK_META);
          robot.keyPress(KeyEvent.VK_W);
          robot.keyRelease(KeyEvent.VK_W);
          robot.keyRelease(KeyEvent.VK_META);
      } else if (command == Command.PLAY || command == Command.PAUSE || command == Command.STOP) {
        // Start/pause the slide show
        robot.keyPress(KeyEvent.VK_SPACE);
        robot.keyRelease(KeyEvent.VK_SPACE);
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
    return new MediaMetaInfo("", "Slideshow", "", PlayerUtil.loadImage("image_viewer.png"), false);
  }

  public void initialise(String[] arguments) {

  }

  public void runFile(FileInfo fileInfo) throws UnsupportedEncodingException,
      UnsupportedCommandException {
    String target = fileInfo.getAbsolutePath();
    
    // If file type is image, open all the images in the parent path
    if (fileInfo.getFileType() == FileType.IMAGE) {
      File file = new File(target);
      File directory = new File(file.getParent());
      
      FileFilter filter = new FileFilter() {
        public boolean accept(File arg0) {
          if (SupportedFiletypeSettings.fileNameToFileType(arg0.getName().toLowerCase()).equals(FileType.IMAGE)) {
            return true;
          }
          return false;
        }
      };
      File[] listOfFiles = directory.listFiles(filter);
      target = "";
      for (File f0 : listOfFiles) {
        target += '\'' + f0.toString() + "' ";
      }
    } else {
      target = '\''  + target + '\'';
    }
    String command = "open -a /Applications/Preview.app/ " + target;
    ServerUtilMac.executeShellScript(command);
    
    Robot robot;
    try {
      Thread.sleep(3000);
      robot = new Robot();
      robot.keyPress(KeyEvent.VK_META);
      robot.keyPress(KeyEvent.VK_SHIFT);
      robot.keyPress(KeyEvent.VK_F);
      robot.keyRelease(KeyEvent.VK_F);
      robot.keyRelease(KeyEvent.VK_SHIFT);
      robot.keyRelease(KeyEvent.VK_META);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public boolean isRunning() {
    return false;
  }
  
  static String join(Collection s, String delimiter) {
    StringBuilder builder = new StringBuilder();
    Iterator iter = s.iterator();
    while (iter.hasNext()) {
       builder.append(iter.next());
        if (iter.hasNext()) {
            builder.append(delimiter);
        }
    }
    return builder.toString();
}
}
