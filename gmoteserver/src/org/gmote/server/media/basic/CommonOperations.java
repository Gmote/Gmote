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

import org.gmote.server.PlatformUtil;

public class CommonOperations {
  public static void sendCloseCommand() throws AWTException {
    Robot robot = new Robot();
    if (PlatformUtil.isMac()) {
      robot.keyPress(KeyEvent.VK_META);
      robot.keyPress(KeyEvent.VK_W);
      robot.keyRelease(KeyEvent.VK_W);
      robot.keyRelease(KeyEvent.VK_META);
    } else {
      robot.keyPress(KeyEvent.VK_ALT);
      robot.keyPress(KeyEvent.VK_F4);
      robot.keyRelease(KeyEvent.VK_F4);
      robot.keyRelease(KeyEvent.VK_ALT);
    }
  }
}
