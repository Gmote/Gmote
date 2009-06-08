package org.gmote.common.packet;

import java.io.Serializable;

import org.gmote.common.Protocol.Command;

/**
 * A reply to SimplePacket(Command.TILE_INFO_REQ). It returns a width and height
 * that represents a union of all available screens.
 * 
 * @author Marc Stogaitis
 * 
 */
public class TileInfoReply extends AbstractPacket implements Serializable{

  private static final long serialVersionUID = 1L;

  int screenWidth;
  int screenHeight;
  int tileSize;
  
  public TileInfoReply(int screenWidth, int screenHeight, int tileSize) {
    super(Command.TILE_INFO_REPLY);
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    this.tileSize = tileSize;
  }

  public int getScreenWidth() {
    return screenWidth;
  }

  public int getScreenHeight() {
    return screenHeight;
  }
  
  public int getTileSize() {
    return tileSize;
  }
}
