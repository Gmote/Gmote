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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gmote.common.TcpConnection;
import org.gmote.common.packet.TileClickReq;
import org.gmote.common.packet.TileInfoReply;
import org.gmote.common.packet.TileSetReq;
import org.gmote.common.packet.TileUpdatePacket;

public class VisualTouchpad {
  private static final Logger LOGGER = Logger.getLogger(VisualTouchpad.class.getName());

  private static VisualTouchpad instance = null;

  private TileHandler tileHandler = new TileHandler();
  private List<ScreenTile> tilesToUpdate = new ArrayList<ScreenTile>();
  private Semaphore tilesToUpdateSemaphore = new Semaphore(0);

  private TcpConnection con;
  private TileUpdaterTask tileUpdater = null;

  private TileSetReq latestTileSet;
  private Semaphore latestTileSemaphore = new Semaphore(0);

  private VisualTouchpad() {
    // TODO(mstogaitis): we'll need to make sure that the thread gets to its
    // 'wait' before allowing queries.
    new Thread(new TileRequestHandler()).start();
  }

  /**
   * Gets an instance of this class.
   */
  public static VisualTouchpad instance() {
    if (instance == null) {
      instance = new VisualTouchpad();

    }
    return instance;
  }

  public void clearTileImages() {
    tileHandler.clearTileImages();
  }

  public void tileUpdateRequest(TileSetReq tileSet) {
    
    LOGGER.info("Received tile update request for: " + tileSet);
    if (tileSet.getTile1X() < 0 || tileSet.getTile1Y() < 0 || tileSet.getTile2X() < 0
        || tileSet.getTile2Y() < 0) {
      LOGGER.warning("Tile set request contains negative numbers. Ignoring it.");
      return;
    }

    synchronized (latestTileSemaphore) {
      latestTileSet = tileSet;
      latestTileSemaphore.release();
    }

  }

  public void tileClickRequest(TileClickReq packet) {
    LOGGER.info("Click request: " + packet.getTileIdX() + " " + packet.getTileIdY() + " "
        + packet.getPixelOffsetInTileX() + " " + packet.getPixelOffsetInTileY());
    ScreenTile tile = tileHandler.getTile(packet.getTileIdX(), packet.getTileIdY());
    tile.clickMouse(packet.getPixelOffsetInTileX(), packet.getPixelOffsetInTileY(), packet
        .getMouseEvent());
  }

  private class TileRequestHandler implements Runnable {

    @Override
    public void run() {
      while (true) {

        try {
          LOGGER.info("Acquire semaphore");
          latestTileSemaphore.acquire();
          LOGGER.info("Semaphore acquired");
        } catch (InterruptedException e) {
          LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        if (tileUpdater != null) {
          LOGGER.info("Pausing tile updater");
          tileUpdater.setThreadPaused(true);
          LOGGER.info("Done pausing tile updater");
        }

        // Synchronize on tileToUpdate so as not to interfere with the
        // tileUpdater thread.
        synchronized (tilesToUpdate) {
          tilesToUpdate.clear();
          synchronized (latestTileSemaphore) {
            for (int tileIdX = latestTileSet.getTile1X(); tileIdX <= latestTileSet.getTile2X(); tileIdX++) {
              for (int tileIdY = latestTileSet.getTile1Y(); tileIdY <= latestTileSet.getTile2Y(); tileIdY++) {
                ScreenTile tile = tileHandler.getTile(tileIdX, tileIdY);
                if (tile != null) {
                  tilesToUpdate.add(tile);
                }
              }
            }
            latestTileSemaphore.drainPermits();
          }
        }

        if (tileUpdater == null) {
          tileUpdater = new TileUpdaterTask();
          tileUpdater.start();
        }
        LOGGER.info("Unpausing tile updater");
        tileUpdater.setThreadPaused(false);
        LOGGER.info("Done unpausing tile updater");
      }
    }
  }

  private class TileUpdaterTask extends Thread {

    private Boolean threadPaused = false;

    @Override
    public void run() {
      TcpConnection conToSend;
      while (true) {
        try {
          if (isThreadPaused()) {
            LOGGER.info("TileUpdater about to acquire semaphore");
            tilesToUpdateSemaphore.acquire();
            LOGGER.info("TileUpdater acquired semaphore");
          }
        } catch (InterruptedException e1) {
          LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
        }

        synchronized (tilesToUpdate) {
          for (ScreenTile tile : tilesToUpdate) {
            if (isThreadPaused()) {
              // Leave the for loop if the thread should be paused.
              LOGGER.info("Leaving for loop since thread going to be paused");
              break;
            }
            try {
              byte[] imageData = tile.takeImage();
              if (imageData != null) {
                LOGGER.info("Sending tile " + tile.getIdX() + " " + tile.getIdY());
                conToSend = getConnection();
                conToSend.sendPacket(new TileUpdatePacket(tile.getIdX(), tile.getIdY(), imageData));
              }
            } catch (IOException e) {
              LOGGER.log(Level.SEVERE, e.getMessage(), e);
              setThreadPaused(true);
              break;
            }
          }
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
          }
        }
      }
      
    }
    
    public synchronized void setThreadPaused(boolean paused) {
      threadPaused = paused;
      if (paused) {
        tilesToUpdateSemaphore.drainPermits();
      } else {
        tilesToUpdateSemaphore.release();
      }
      
    }

    public synchronized boolean isThreadPaused() {
      return threadPaused;
    }
  }

  public TileInfoReply createScreenInfoReply() {
    Rectangle rect = tileHandler.getAllScreenRect();
    TileInfoReply reply = new TileInfoReply(rect.width, rect.height, TileHandler.TILE_SIZE);
    return reply;
  }

  public synchronized void setConnection(TcpConnection newConnection) {
    con = newConnection;
    
  }
  public synchronized TcpConnection getConnection() {
    return con;
  }
}
  