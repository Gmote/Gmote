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

package org.gmote.server.visualtouchpad;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

public class Screen {
  private Rectangle screenBounds;
  private Robot robot;
  
  public Screen(Rectangle screenBounds, Robot robot) {
    this.screenBounds = screenBounds;
    this.robot = robot;
  }
  
  public BufferedImage takeScreenShot(Rectangle bounds) {
    return robot.createScreenCapture(bounds);
  }
  
  public boolean isTileOnScreen(Rectangle tileRect) {
    return screenBounds.intersects(tileRect);
  }

  public Rectangle getBounds() {
    return screenBounds;
  }

  public Robot getRobot() {
    return robot;
  }
}
