package org.gmote.client.android;

import java.net.DatagramSocket;
import java.net.SocketException;

import org.gmote.common.Protocol.Command;
import org.gmote.common.Protocol.MouseEvent;
import org.gmote.common.packet.AbstractPacket;
import org.gmote.common.packet.KeyboardEventPacket;
import org.gmote.common.packet.SimplePacket;
import org.gmote.common.packet.TileClickReq;
import org.gmote.common.packet.TileInfoReply;
import org.gmote.common.packet.TileSetReq;
import org.gmote.common.packet.TileUpdatePacket;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

public class RemoteDesktop extends Activity implements BaseActivity {
  static final String DEBUG_TAG = "Gmote";
  static ActivityUtil mUtil = null;

  Remote remoteInstance;
  int serverUdpPort;
  
  // bitmap
  Bitmap bitmap;
  int bmWidth;
  int bmHeight;
  BitmapFactory.Options bmOptions;
  Rect bmRect;
  
  // tile
  Canvas tileCanvas;
  Rect tileRect;
  Object waitForTile = new Object();
  int tileSize;
  int tileMaxX;
  int tileMaxY;
  int tileNumVisibleX;
  int tileNumVisibleY;
  int tile1X = -1;
  int tile1Y = -1;
  
  // view
  BitmapView main;
  int width;
  int height;

  // motion
  GestureDetector gestureDetector = null;
  private float mX = 0;
  private float mY = 0;
  private float prevX = -1;
  private float prevY = -1;
  private int clickX = -10;
  private int clickY = -10;
  private long timeOfLastClick = 0;
  private long timeOfLastPosX = 0;
  private long timeOfLastNegX = 0;
  private long timeOfLastPosY = 0;
  private long timeOfLastNegY = 0;

  private float posXAcceleration = 0;
  private float negXAcceleration = 0;
  private float posYAcceleration = 0;
  private float negYAcceleration = 0;

  private static final float ACCELERATION_DECAY = (float) 0.1;
  private static final float MOUSE_SENSITIVITY_DEFAULT = (float) -1.4;
  private static final float MOUSE_ACCELERATION_DEFAULT = (float) 0.5;

  private float mouseSensitivity = MOUSE_SENSITIVITY_DEFAULT;
  private float mouseAccelerationDamper = MOUSE_ACCELERATION_DEFAULT;

  // Object that we will wait on when the screen is not scrolling.
  private Object waitForScroll = new Object();

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    Log.d(DEBUG_TAG, "RemoteDesktop# oncreate");


    mUtil = new ActivityUtil();
    mUtil.onCreate(icicle, this);
    remoteInstance = Remote.getInstance();

    main = new BitmapView(this);

    tileCanvas = new Canvas();
    tileRect = new Rect();
    bmRect = new Rect();

    gestureDetector = new GestureDetector(gestureListener);
    gestureDetector.setIsLongpressEnabled(true);
    setContentView(main);
    
    new Thread(new PainterThread()).start();
    new Thread(new TilerThread()).start();
  }

  // ---------------------------------------------------------------------------------------------------
  // GRAPHICS
  
  public class PainterThread implements Runnable {
    public void run() {
      while (true) {
          synchronized (waitForScroll) {
            try {
              waitForScroll.wait();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          //Log.d(DEBUG_TAG, "scrolled, main.postInvalidate");
          main.postInvalidate();
      }
    }
  }

  public class TilerThread implements Runnable {
    public void run() {
      while (true) {
          synchronized (waitForTile) {
            try {
              waitForTile.wait();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          Log.d(DEBUG_TAG, "got tile, main.postInvalidate");
          main.postInvalidate();
      }
    }
  }
  private synchronized void updateTile(int idx, int idy, byte[] data) {
    Log.d(DEBUG_TAG, "updateTile: " + idx +","+ idy);
    tileRect.offsetTo(idx * tileSize, idy * tileSize);
    tileCanvas.drawBitmap(BitmapFactory.decodeByteArray(data, 0, data.length, bmOptions), null, tileRect, new Paint());
    synchronized(waitForTile) {
      waitForTile.notifyAll();
    }
    //main.postInvalidate(tileRect.left, tileRect.top, tileRect.right, tileRect.bottom);
    //main.invalidate();
  }

  private synchronized void doDraw(Canvas canvas, Paint paint) {
    if (bitmap!=null) {
      canvas.drawBitmap(bitmap, mX, mY, paint);
      //Log.d(DEBUG_TAG, "drawing " + mX + ", " + mY);
    }
  }

  class BitmapView extends View {
    Paint paint;

    public BitmapView(Context context) {
      super(context);
      paint = new Paint();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
      super.onLayout(changed, left, top, right, bottom);
      width = right;
      height = bottom;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
      doDraw(canvas, paint);
    }
  }

  void getTileInfo() {
    mUtil.send(new SimplePacket(Command.TILE_INFO_REQ));
  }
  
  void initializeGraphics() {
    bmRect = new Rect(0, 0, bmWidth, bmHeight);
    tileRect = new Rect(0, 0, tileSize, tileSize);
    if (bitmap != null) {
      bitmap.recycle();
    }
    bitmap = Bitmap.createBitmap(bmWidth, bmHeight, Bitmap.Config.RGB_565);
    tileCanvas.setBitmap(bitmap);
    bmOptions = new BitmapFactory.Options();
    bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;
    tileMaxX = bmWidth/tileSize;
    tileMaxY = bmHeight/tileSize;
    Log.d(DEBUG_TAG, "width="+width+" height="+height+" tileMaxX="+tileMaxX + " tileMaxY="+tileMaxY + " tileSize="+tileSize);
  }
  
  // ---------------------------------------------------------------------------------------------------
  // GENERAL
  @Override
  public void onStart() {
    super.onStart();
    Log.d(DEBUG_TAG, "RemoteDesktop# onstart");

    mUtil.onStart(this);
    serverUdpPort = remoteInstance.getServerUdpPort();
    getTileInfo();
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(DEBUG_TAG, "RemoteDesktop# resume");

    mUtil.onResume();
    getTileInfo();
    //bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.screenshot)).getBitmap();
  }
  
  @Override
  public void onPause() {
    super.onPause();
    Log.d(DEBUG_TAG, "RemoteDesktop# onpause");

    mUtil.onPause();
  }

  @Override
  public void onStop() {
    super.onStop();
    Log.d(DEBUG_TAG, "RemoteDesktop# onstop");
    mUtil.onStop();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    mUtil.onCreateOptionsMenu(menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    return mUtil.onOptionsItemSelected(item);
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    return mUtil.onCreateDialog(id);
  }

  public void handleReceivedPacket(AbstractPacket reply) {
    if (reply.getCommand() == Command.TILE_INFO_REPLY) {
      Log.d(DEBUG_TAG, "RemoteDesktop# got tile info ");
      TileInfoReply ti = (TileInfoReply) reply;
      bmWidth = ti.getScreenWidth();
      bmHeight = ti.getScreenHeight();
      tileSize = ti.getTileSize();

      initializeGraphics();
      new Thread(new TileReqSenderThread()).start();
      //sendTcpTileReq();
    } else if (reply.getCommand() == Command.TILE_UPDATE) {
      Log.d(DEBUG_TAG, "RemoteDesktop# got tile update ");
      TileUpdatePacket update = (TileUpdatePacket)reply;
      updateTile(update.getTileIdX(), update.getTileIdY(), update.getImageData());
    } else {
      Log.e(DEBUG_TAG,"Unexpected packet in RemoteDesktop: " + reply.getCommand());
      return;
    }
  }

  // ---------------------------------------------------------------------------------------------------
  // MOTION
  GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
    public boolean onDown(MotionEvent e) {
      return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
        float velocityY) {
      return false;
    }

    public void onLongPress(MotionEvent e) {
      mouseClick((int)e.getRawX(), (int)e.getRawY(), MouseEvent.RIGHT_CLICK);
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
        float distanceY) {
      incrementDistance(distanceX * mouseSensitivity, distanceY
          * mouseSensitivity);
      synchronized (waitForScroll) {
        waitForScroll.notifyAll();
      }

      return true;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
      mouseClick((int)e.getRawX(), (int)e.getRawY(), MouseEvent.SINGLE_CLICK);
      return true;
    }
  };
  
  
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      finish();
    } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
     // mouseClick(MouseEvent.SINGLE_CLICK);
    } else {
      KeyCharacterMap kmap = KeyCharacterMap.load(event.getDeviceId());

      int c = kmap.get(keyCode, event.getMetaState());
      
      if (c != 0) {
        mUtil.send(new KeyboardEventPacket(c));
      } else {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
          mUtil.send(new KeyboardEventPacket(KeyboardEventPacket.DELETE_KEYCODE));
        } else if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_SYM) {
          mUtil.send(new KeyboardEventPacket(KeyboardEventPacket.SEARCH_KEYCODE));
        }
      }
    }
    return false;
  }

  private synchronized void incrementDistance(float distanceX, float distanceY) {
    long currentTime = System.currentTimeMillis();
    if (distanceX > 0) {
      posXAcceleration = computeAcceleration(timeOfLastPosX, currentTime,
          posXAcceleration, distanceX);
      timeOfLastPosX = currentTime;
      mX += distanceX + posXAcceleration;
    } else {
      negXAcceleration = computeAcceleration(timeOfLastNegX, currentTime,
          negXAcceleration, Math.abs(distanceX));
      timeOfLastNegX = currentTime;
      mX += distanceX + (negXAcceleration * -1); // * distanceX * -1);
    }

    if (distanceY > 0) {
      posYAcceleration = computeAcceleration(timeOfLastPosY, currentTime,
          posYAcceleration, distanceY);
      timeOfLastPosY = currentTime;
      mY += distanceY + posYAcceleration;
    } else {
      negYAcceleration = computeAcceleration(timeOfLastNegY, currentTime,
          negYAcceleration, Math.abs(distanceY));
      timeOfLastNegY = currentTime;
      mY += distanceY + (negYAcceleration * -1); // * distanceX * -1);
    }
    clamp();
  }
  private void clamp() {
    if(mX > 0)
      mX = 0;
    else if (mX < -bmWidth + width)
      mX = -bmWidth + width;
    
    if(mY > 0)
      mY = 0;
    else if (mY < -bmHeight + height)
      mY = -bmHeight + height;
      
  }

  private float computeAcceleration(long timeOfLastMove, long currentTime,
      float lastAcceleration, float distanceMoved) {

    // Decay the current acceleration value.
    lastAcceleration -= (currentTime - timeOfLastMove) * ACCELERATION_DECAY;
    // Add acceleration based on the current movement.
    lastAcceleration += distanceMoved;

    if (lastAcceleration < 0) {
      lastAcceleration = 0;
    }

    return lastAcceleration * mouseAccelerationDamper;
  }
  
  @Override
  public boolean onTouchEvent(MotionEvent event) {
     return gestureDetector.onTouchEvent(event);
  }
  
  void mouseClick(int x, int y, MouseEvent evt) {
    int idx, idy, offsetx, offsety;
    long currentTime = System.currentTimeMillis();

    if ((currentTime - timeOfLastClick < 1400) && clickX > 0) {
      // for double-clicks
      x = clickX;
      y = clickY;
    } else {
      x -= mX;
      y -= mY;
      clickX = x;
      clickY = y;
    }
    
    idx = x/tileSize;
    idy = y/tileSize;
    offsetx = x - idx * tileSize;
    offsety = y - idy * tileSize;

    mUtil.send(new TileClickReq(idx, idy, offsetx, offsety, evt));
    
    timeOfLastClick = currentTime;
  }
  
  public class TileReqSenderThread implements Runnable {
    
    public void run() {
      DatagramSocket socket = null;
      try {
        socket = new DatagramSocket();
      } catch (SocketException e) {
        Log.e(DEBUG_TAG, e.getMessage(), e);
      }
      
      while (true) {
        sendTcpTileReq();
        //sendUdpTileReq(socket);
      }
    }
  }
  
  private void sendTcpTileReq() {
    if ((mX != prevX || mY != prevY) && (bmRect.contains((int)-mX, (int)-mY))) {
      int x, y, x1, y1, x2, y2;
      x = (int) -mX / tileSize;
      y = (int) -mY / tileSize;

      if (x != tile1X || y != tile1Y) {
        x1 = Math.max(x, 0);
        y1 = Math.max(y, 0);
        x2 = Math.min(x + width/tileSize + 1, tileMaxX);
        y2 = Math.min(y + height/tileSize + 1, tileMaxY);
        mUtil.send(new TileSetReq(x1, y1, x2, y2));
        tile1X = x;
        tile1Y = y;
      }
      
      prevX = mX;
      prevY = mY;
    }
  }
//
//  private void sendUdpTileReq(DatagramSocket socket) {
//    DatagramPacket packet = makeDatagramPacketIfNeeded();
//    if (packet != null) {
//      Log.i(DEBUG_TAG, "Sending packet");
//      try {
//        if (!remoteInstance.isConnected()) {
//          remoteInstance.connect(false);
//        }
//        socket.send(packet);
//        Log.i(DEBUG_TAG, "Packet sent");
//      } catch (IOException e) {
//        Log.e(DEBUG_TAG, e.getMessage(), e);
//      }
//    } else {
//      try {
//        synchronized (waitForScroll) {
//          waitForScroll.wait();
//        }
//      } catch (InterruptedException e) {
//        Log.e(DEBUG_TAG, e.getMessage(), e);
//        return;
//      }
//    }
//  }
//  private synchronized DatagramPacket makeDatagramPacketIfNeeded() {
//    DatagramPacket packet = null;
//    
//    // FIXME!
//    if (mX != prevX || mY != prevY) {
//      byte[] buf = new byte[5];
//      packet= new DatagramPacket(buf, buf.length, remoteInstance.getServerInetAddress(), serverUdpPort);
//      Log.i(DEBUG_TAG, remoteInstance.getServerIp());
//      prevX = mX; prevY = mY;
//    }
//    
//    return packet;
//  }

}
