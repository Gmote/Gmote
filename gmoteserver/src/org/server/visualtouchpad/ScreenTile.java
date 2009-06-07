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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.gmote.common.Protocol.MouseEvent;
import org.gmote.server.MouseUtil;

/**
 * Represents a single screen tile. Is capable of taking screen shots of itself
 * and comparing it with previous screen shots.
 * 
 * @author Marc Stogaitis
 */
public class ScreenTile {
  private int idX;
  private int idY;
  
  // Location of tile relative to all screens.
  private Rectangle tileRect;
  
  private List<Rectangle> boundsWithinIndividualScreen = new ArrayList<Rectangle>();
  private List<Screen> screens = new ArrayList<Screen>();
  
  private byte[] lastImageSeen;
  
  public ScreenTile(int idX, int idY, Rectangle tileRect) {
    this.idX = idX;
    this.idY = idY;
    this.tileRect = tileRect;
  }

  /**
   * Tells this tile which screen it is associated with. On single monitor
   * systems, this will simply be the main screen. On multi-monitor systems,
   * this tile can be split across more than one screen when near a screen
   * boundary.
   */
  public void addScreen(Screen screen) {
    screens.add(screen);
    Rectangle screenBounds = screen.getBounds();
    Rectangle boundsWithinScreen = new Rectangle(tileRect.x - screenBounds.x, tileRect.y - screenBounds.y, TileHandler.TILE_SIZE, TileHandler.TILE_SIZE);
    boundsWithinIndividualScreen.add(boundsWithinScreen);
  }

  /**
   * Takes a screen shot at the position on the screen which this tile
   * represents. Returns null if the image has not changed since the last time
   * this method was called, or if this is an 'off the screen tile' which can
   * happen when you have more than one monitor with different sizes.
   */
  public byte[] takeImage() throws IOException {
    // TODO(mstogaitis): handle the case where tiles are part of several
    // screens.
    if (screens.size() == 0) {
      return null;
    }
    Screen screen = screens.get(0);
    Robot robot = screen.getRobot();
    BufferedImage bufferedImage = robot.createScreenCapture(boundsWithinIndividualScreen.get(0));

    ByteArrayOutputStream bas = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "PNG", bas);
    byte[] imageData = bas.toByteArray();
    // TODO(mstogaitis): Investigate using a hash here
    if (Arrays.equals(imageData, lastImageSeen)) {
      return null;
    } else {
      lastImageSeen = imageData;
      return imageData;
    }
  }
  
  public void clickMouse(int pixelOffsetX, int pixelOffsetY, MouseEvent mouseEvent) {
    if (screens.size() == 0) {
      return;
    }
    Screen screen = screens.get(0);
    Robot robot = screen.getRobot();
    
    Rectangle bounds = boundsWithinIndividualScreen.get(0);
    robot.mouseMove(bounds.x + pixelOffsetX, bounds.y + pixelOffsetY);
    MouseUtil.doMouseEvent(mouseEvent, robot);
  }
  
  public int getIdX() {
    return idX;
  }

  public int getIdY() {
    return idY;
  }

  
  @Override
  public boolean equals(Object obj) {
    if((obj == null) || (obj.getClass() != this.getClass())) return false; 
    ScreenTile otherObj = (ScreenTile)obj;
    return otherObj.idX == idX && otherObj.idY == idY;
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + idX;
    hash = hash * 29 + idY;
    return hash;
  }
  
  @Override
  public String toString() {
    return idX + " " + idY;
  }

  public void clearImage() {
    lastImageSeen = null;
    
  }
}
