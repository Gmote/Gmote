package org.gmote.common.packet;

import java.io.Serializable;

import org.gmote.common.Protocol.Command;

/**
 * A packet sent from the server to the client which contains a tile's image
 * data.
 * 
 * @author Marc Stogaitis
 * 
 */
public class TileUpdatePacket extends AbstractPacket implements Serializable{

  private static final long serialVersionUID = 1L;

  private int tileIdX, tileIdY;

  private byte[] imageData;
  
  public TileUpdatePacket(int tileIdX, int tileIdY, byte[] imageData) {
    super(Command.TILE_UPDATE);
    this.tileIdX = tileIdX;
    this.tileIdY = tileIdY;
    this.imageData = imageData;
  }

  public int getTileIdX() {
    return tileIdX;
  }

  public int getTileIdY() {
    return tileIdY;
  }

  public byte[] getImageData() {
    return imageData;
  }

  
}
