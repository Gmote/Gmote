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

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TileHandler {
  private static final Logger LOGGER = Logger.getLogger(TileHandler.class.getName());

  public static final int TILE_SIZE = 128;

  private Map<ScreenTile, ScreenTile> tiles = new HashMap<ScreenTile, ScreenTile>();
  
  // Rectangle that contains the union of all of the screens on the
  // computer.
  private Rectangle allScreenRect = new Rectangle();
  
  public TileHandler() {
    try {
      List<Screen> individualScreens = new ArrayList<Screen>(); 
      allScreenRect = initScreen(individualScreens);
      initTileList(allScreenRect, individualScreens);
    } catch (AWTException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }
  
  /**
   * Markes all the images as 'dirty'.
   */
  public void clearTileImages() {
    for (ScreenTile tile : tiles.values()) {
      tile.clearImage();
    }
  }
  
  public Rectangle getAllScreenRect() {
    return allScreenRect;
  }
  
  /**
   * Initialize screen.
   */
  private Rectangle initScreen(List<Screen> individualScreens)
      throws AWTException {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] graphicDevices = ge.getScreenDevices();

    Rectangle allScreenRect = new Rectangle();
    for (GraphicsDevice g : graphicDevices) {
      Rectangle bounds = g.getDefaultConfiguration().getBounds();

      allScreenRect = allScreenRect.union(bounds);
      individualScreens.add(new Screen(bounds, new Robot(g)));
    }
    LOGGER.info("Screen rectangle: " + allScreenRect);
    return allScreenRect;
  }

  /**
   * Initialize tile list.
   */
  private void initTileList(Rectangle allScreenRect, List<Screen> individualScreens) {
    int tileIdX = 0;
    int tileIdY = 0;
    for (int tileY = allScreenRect.y; tileY < allScreenRect.height; tileY += TILE_SIZE) {
      for (int tileX = allScreenRect.x; tileX < allScreenRect.width; tileX += TILE_SIZE) {

        Rectangle tileRect = new Rectangle(tileX, tileY, TILE_SIZE, TILE_SIZE);
        ScreenTile tile = new ScreenTile(tileIdX, tileIdY, tileRect);
        tiles.put(tile, tile);

        for (Screen screen : individualScreens) {
          if (screen.isTileOnScreen(tileRect)) {
            tile.addScreen(screen);
          }
        }
        tileIdX++;
      }
      tileIdX = 0;
      tileIdY++;
    }
  }

  public ScreenTile getTile(int tileIdX, int tileIdY) {
    return tiles.get(new ScreenTile(tileIdX, tileIdY, null));
  }
}
