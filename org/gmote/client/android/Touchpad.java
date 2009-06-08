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

package org.gmote.client.android;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.gmote.common.Protocol.MouseEvent;
import org.gmote.common.Protocol.UdpPacketTypes;
import org.gmote.common.packet.AbstractPacket;
import org.gmote.common.packet.KeyboardEventPacket;
import org.gmote.common.packet.MouseClickPacket;
import org.gmote.common.packet.MouseWheelPacket;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

public class Touchpad extends Activity implements BaseActivity{

  private static final String DEBUG_TAG = "Gmote";

  GestureDetector gestureDetector = null;

  ActivityUtil mUtil = null;
  ProgressDialog mDialog = null;
  
  View mContentView = null;
  View mPasswordEntryView = null;
  
  private float mX = 0;
  private float mY = 0;

  private long timeOfLastPosX = 0;
  private long timeOfLastNegX = 0;
  private long timeOfLastPosY = 0;
  private long timeOfLastNegY = 0;
  
  private float posXAcceleration = 0;
  private float negXAcceleration = 0;
  private float posYAcceleration = 0;
  private float negYAcceleration = 0;
  
  private static final float ACCELERATION_DECAY = (float)0.1;
  private static final float MOUSE_SENSITIVITY_DEFAULT = (float)-1.4;
  private static final float MOUSE_ACCELERATION_DEFAULT = (float)0.5;
  
  private float mouseSensitivity = MOUSE_SENSITIVITY_DEFAULT;
  private float mouseAccelerationDamper = MOUSE_ACCELERATION_DEFAULT;
  
  private Remote remoteInstance;
  
  // Object that we will wait on when the mouse is not moving.
  private Object waitForMouseMove = new Object();
  
  private int serverUdpPort;
  
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    mUtil = new ActivityUtil();
    mUtil.onCreate(icicle, this);
    
    remoteInstance = Remote.getInstance();
    
    gestureDetector = new GestureDetector(gestureListener);
    gestureDetector.setIsLongpressEnabled(true);
    setContentView(R.layout.touchpad);
    mContentView = findViewById(R.id.touchpad);
    mContentView.setFocusable(true);
    mContentView.setFocusableInTouchMode(true);

    loadMouseSettings();
    MouseSendingThread mst = new MouseSendingThread();
    new Thread(mst).start();
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    mUtil.onCreateOptionsMenu(menu);
    menu.removeItem(R.id.menui_touchpad);
    menu.removeItem(R.id.menui_settings);
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.touchpad_settings, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    if (item.getItemId() == R.id.menui_touchpad_settings) {
      mUtil.startActivityByClass(TouchpadSettings.class);
      return true;
    } else {
      return mUtil.onOptionsItemSelected(item);
    }
  }
  
  @Override
  protected Dialog onCreateDialog(int id) {
    return mUtil.onCreateDialog(id);
  }
  
  GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
    public boolean onDown(MotionEvent e) {
      return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
        float velocityY) {
      return false;
    }

    public void onLongPress(MotionEvent e) {
      mouseClick(MouseEvent.RIGHT_CLICK);
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
        float distanceY) {
      
      incrementDistance(distanceX * mouseSensitivity, distanceY * mouseSensitivity);
      synchronized(waitForMouseMove) {
        waitForMouseMove.notify();
      }
      
      return true;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
      mouseClick(MouseEvent.SINGLE_CLICK);
      return true;
    }
    
    @Override
    public boolean onDoubleTap(MotionEvent e) {
      Log.i(DEBUG_TAG, "Double Tap");
      mouseClick(MouseEvent.SINGLE_CLICK);
      return true;
    }
  };

  void mouseClick(MouseEvent evt) {
    mUtil.send(new MouseClickPacket(evt));
  }
  
  @Override
  public void onStart() {
    super.onStart();
    mUtil.onStart(this);
    serverUdpPort = Remote.getInstance().getServerUdpPort();
  }
  
  @Override
  public void onStop() {
    super.onStop();
    mUtil.onStop();
  }
  
  @Override
  public void onResume() {
    super.onResume();
    mUtil.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mUtil.onPause();
  }
  
  public void handleReceivedPacket(AbstractPacket reply) {
    
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
     return gestureDetector.onTouchEvent(event);
    
  }
  
  @Override
  public boolean dispatchTrackballEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      mouseClick(MouseEvent.LEFT_MOUSE_DOWN);
      return true;
    } else if (event.getAction() == MotionEvent.ACTION_UP) {
      mouseClick(MouseEvent.LEFT_MOUSE_UP);
      return true;
    }
    
    
    final float scaleY = event.getYPrecision();
    final float y = 0 - event.getY()* scaleY;
    
    if (y < 0) {
      mouseWheelMove(1);
    } else if (y > 0) {
      mouseWheelMove(-1);
    }

    return true;
  }
  
  void mouseWheelMove(int wheelAmount) {
    remoteInstance.queuePacket(new MouseWheelPacket(wheelAmount));
  }
  
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Log.i("onKeyDown", event.toString());
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      finish();
    } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
      mouseClick(MouseEvent.SINGLE_CLICK);
      
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
      posXAcceleration = computeAcceleration(timeOfLastPosX, currentTime, posXAcceleration, distanceX);
      timeOfLastPosX = currentTime;
      mX += distanceX + posXAcceleration;
    } else {
      negXAcceleration = computeAcceleration(timeOfLastNegX, currentTime, negXAcceleration, Math.abs(distanceX));
      timeOfLastNegX = currentTime;
      mX += distanceX + (negXAcceleration * -1); //* distanceX * -1);
    }
    
    if (distanceY > 0) {
      posYAcceleration = computeAcceleration(timeOfLastPosY, currentTime, posYAcceleration, distanceY);
      timeOfLastPosY = currentTime;
      mY += distanceY + posYAcceleration;
    } else {
      negYAcceleration = computeAcceleration(timeOfLastNegY, currentTime, negYAcceleration, Math.abs(distanceY));
      timeOfLastNegY = currentTime;
      mY += distanceY + (negYAcceleration * -1); //* distanceX * -1);
    }
  }
  
  private synchronized DatagramPacket makeDatagramPacketIfNeeded() {
    DatagramPacket packet = null;
    
    if (mX != 0 || mY != 0) {
      byte[] buf = new byte[5];
      short tempX = (short)mX;
      short tempY = (short)mY;
      buf[0] = UdpPacketTypes.MOUSE_MOVE.getId();
      buf[1] = (byte)tempX;
      buf[2] = (byte)(tempX >>> 8);
      buf[3] = (byte)tempY;
      buf[4] = (byte)(tempY >>> 8);
      //Log.i(DEBUG_TAG, "mX=" + mX + " mY=" + mY + " shortX=" + tempX + " shortY=" + tempY);
      packet= new DatagramPacket(buf, buf.length, remoteInstance.getServerInetAddress(), serverUdpPort);
      Log.i(DEBUG_TAG, remoteInstance.getServerIp());
      mX = 0; mY = 0;
    }
    
    return packet;
  }

  public class MouseSendingThread implements Runnable {
    
    public void run() {
      DatagramSocket socket = null;
      try {
        socket = new DatagramSocket();
      } catch (SocketException e) {
        Log.e(DEBUG_TAG, e.getMessage(), e);
      }
      while (true) {
        DatagramPacket packet = makeDatagramPacketIfNeeded();
        if (packet != null) {
          Log.i(DEBUG_TAG, "Sending packet");
          try {
            if (!remoteInstance.isConnected()) {
              remoteInstance.connect(false);
            }
            socket.send(packet);
            Log.i(DEBUG_TAG, "Packet sent");
          } catch (IOException e) {
            Log.e(DEBUG_TAG, e.getMessage(), e);
          }
        } else {
          try {
            synchronized (waitForMouseMove) {
              waitForMouseMove.wait();
            }
          } catch (InterruptedException e) {
            Log.e(DEBUG_TAG, e.getMessage(), e);
            return;
          }
        }
      }

    }
  }

  private float computeAcceleration(long timeOfLastMove, long currentTime, float lastAcceleration,
      float distanceMoved) {

    // Decay the current acceleration value.
    lastAcceleration -= (currentTime - timeOfLastMove) * ACCELERATION_DECAY;
    // Add acceleration based on the current movement.
    lastAcceleration += distanceMoved;

    if (lastAcceleration < 0) {
      lastAcceleration = 0;
    }

    return lastAcceleration * mouseAccelerationDamper;
  }

  public void loadMouseSettings() {
    SharedPreferences prefs = getSharedPreferences(GmoteClient.PREFS, MODE_WORLD_READABLE);
    int mouseSensitivityPref = prefs.getInt(TouchpadSettings.MOUSE_SENSITIVITY_SETTINGS_KEY, 50);
    int mouseAccelerationPref = prefs.getInt(TouchpadSettings.MOUSE_ACCELERATION_SETTINGS_KEY, 50);
    mouseSensitivity = MOUSE_SENSITIVITY_DEFAULT * ((float)mouseSensitivityPref / 50);
    mouseAccelerationDamper = MOUSE_ACCELERATION_DEFAULT * ((float)mouseAccelerationPref / 50);
    Log.i(DEBUG_TAG, "Setting Prefs: sens=" + mouseSensitivityPref + " accel=" + mouseAccelerationPref);
    Log.i(DEBUG_TAG, "Setting Mouse to: sens=" + mouseSensitivity + " accel=" + mouseAccelerationDamper);
  }
}
