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

package org.gmote.server;

import java.awt.Robot;
import java.awt.event.InputEvent;

import org.gmote.common.Protocol.MouseEvent;

public class MouseUtil {

  public static void doMouseEvent(MouseEvent mouseEvent, Robot robot) {
    if (mouseEvent == MouseEvent.SINGLE_CLICK) {
      clickMouse(InputEvent.BUTTON1_MASK, robot);
    } else if (mouseEvent == MouseEvent.RIGHT_CLICK) {
      clickMouse(InputEvent.BUTTON3_MASK, robot);
    } else if (mouseEvent == MouseEvent.DOUBLE_CLICK) {
      clickMouse(InputEvent.BUTTON1_MASK, robot);
      clickMouse(InputEvent.BUTTON1_MASK, robot);
    } else if (mouseEvent == MouseEvent.LEFT_MOUSE_DOWN) {
      robot.mousePress(InputEvent.BUTTON1_MASK);
    } else if (mouseEvent == MouseEvent.LEFT_MOUSE_UP) {
      robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }
  }

  private static void clickMouse(int buttonMask, Robot robot) {
    robot.mousePress(buttonMask);
    robot.mouseRelease(buttonMask);
  }
}
