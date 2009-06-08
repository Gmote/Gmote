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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.gmote.common.DataReceiverIF;
import org.gmote.common.MulticastClient;
import org.gmote.common.ServerInfo;
import org.gmote.common.ServerOutOfDateException;
import org.gmote.common.TcpConnection;
import org.gmote.common.MulticastClient.ServerFoundHandler;
import org.gmote.common.packet.AbstractPacket;
import org.gmote.common.security.AuthenticationException;
import org.gmote.common.security.AuthenticationHandler;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Wrapper class responsible for finding and communicating with the server.
 *
 * @author Mimi
 *
 */
public class Remote implements DataReceiverIF {
  // Current version of the Gmote Client. We don't use the value that is in the
  // manifest since its possible that we don't have access to this value (for
  // example, when the program crashes and gets restarted by android)
  public static final String GMOTE_CLIENT_VERSION = "2.0.2";
  public static final String MINIMUM_SERVER_VERSION = "2.0.0";

  // Response codes
  public static final int NORMAL= 0;
  public static final int CONNECTION_FAILURE = 1;
  public static final int CONNECTING = 2;
  public static final int CONNECTED = 6;
  public static final int SEARCHING = 3;
  public static final int AUTHENTICATION_FAILURE = 4;
  public static final int SERVER_LIST_ADD_SERVER = 5;
  public static final int SERVER_LIST_DONE = 6;
  public static final int SERVER_OUT_OF_DATE = 7;

  // Timing constants
  public static final int MAX_ATTEMPTS = 3; // number of connection attempts before report giving up
  public static final int TIMEOUT = 3000; // milliseconds server connection timeout
  //public static final int FINDSERVERS_LIMIT = 4;
  public static final int FINDSERVERS_TIMEOUT = 6500; // milliseconds
  private static final String DEBUG_TAG = "Gmote";



  private ServerInfo server = null;
  private String password = "";
  private static Remote remote = new Remote();

  private Handler callback;
  private TcpConnection con = null;
  private Thread worker = null;
  private BlockingQueue<AbstractPacket> packetQueue = new LinkedBlockingQueue<AbstractPacket>();
  InetAddress serverInetAddress = null;
  //private WifiLock wifiLock;

  private Remote() {
    // Start a new thread that will send packets for us.
    worker = new Thread(new PacketSender());
    worker.start();
  }

  private void setCallback(Handler callback) {
    this.callback = callback;
  }

  public static synchronized Remote getInstance(Handler handler) {
    remote.setCallback(handler);
    return remote;
  }

  public static Remote getInstance() {
    return remote;
  }

  public synchronized void setServer(ServerInfo serverInfo) {
    server = serverInfo;
    Log.d(DEBUG_TAG, "Gmote# set server to: " + server.getServer() + ":" + server.getPort());
    try {
      if (serverInfo == null || serverInfo.getIp() == null) {
        serverInetAddress = null;
      } else {
        serverInetAddress = InetAddress.getByName(serverInfo.getIp());
      }
    } catch (UnknownHostException e) {
      Log.e(DEBUG_TAG, e.getMessage(), e);
      serverInetAddress = null;
    }
    disconnect();
  }

  public InetAddress getServerInetAddress() {
    return serverInetAddress;
  }

  protected synchronized void disconnect() {
    if (con != null) {
      con.closeConnection();
      con = null;
    }
    packetQueue.clear();
  }

  public synchronized void setPassword(String newPassword) {
    password = newPassword;
    Log.d(DEBUG_TAG, "Remote# set password");
  }

  public void detach() {
    callback = null;
  }


  public String getServerString() {
    if (server != null)
      return server.toString();
    return "";
  }

  public String getServerIp() {
    if (server != null)
      return server.getIp();
    return "";
  }

  public int getServerPort() {
    if (server != null) {
      return server.getPort();
    }
    return 8889;
  }

  public int getServerUdpPort() {
    if (server != null) {
      return server.getUdpPort();
    }
    return ServerInfo.DEFAULT_UDP_PORT;
  }

  public synchronized boolean isConnected() {
    return con == null? false : con.isConnected();
  }

  protected synchronized boolean connect(boolean ignoreErrors) {
    if (callback == null) {
      Log.w(DEBUG_TAG, "Callback is null in connect()");
      return false;
    }

    callback.sendEmptyMessage(CONNECTING);
    if (server == null) {
      Log.w(DEBUG_TAG, "Server was null in connect");
      disconnect();
      if (!ignoreErrors) {
        callback.sendEmptyMessage(CONNECTION_FAILURE);
      }
      return false;
    }

    for (int i = 0; i < MAX_ATTEMPTS && callback != null; i++) {
      try {
        connectToServer();
        if (callback != null) {
          callback.sendEmptyMessage(CONNECTED);
        }
        return true;
      } catch (IOException e) {
        Log.e(DEBUG_TAG, "Connection attempt " + i + " failed: " + e.getMessage(), e);
      } catch (AuthenticationException e) {
        Log.e(DEBUG_TAG, "Authentication failure: " + e.getMessage(), e);
        disconnect();
        if (callback != null) {
          callback.sendEmptyMessage(AUTHENTICATION_FAILURE);
        } else {
          Log.w(DEBUG_TAG, "Authentication failure with callback = null. We won't be able to notify anyone");
        }
        return false;
      } catch (ServerOutOfDateException e) {
        Log.e(DEBUG_TAG, "Server out of date error: " + e.getMessage(), e);
        if (callback != null) {
          callback.sendMessage(Message.obtain(callback, SERVER_OUT_OF_DATE, e.getServerVersion()));
          return true;
        } else {
          Log.e(DEBUG_TAG,"The server is out of date, but no callback was found. This means we won't be able to notify the user of the current error.");
          disconnect();
          return false;
        }
      }
    }

    Log.w(DEBUG_TAG, "Failed to connect after " + MAX_ATTEMPTS + " attempts. Aborting.");
    if (callback != null) {
      if (!ignoreErrors) {
        callback.sendEmptyMessage(CONNECTION_FAILURE);
      }
    } else {
      Log.w(DEBUG_TAG, "Connection failure, and call back is null");
    }
    disconnect();
    return false;
  }

  private synchronized void connectToServer() throws IOException, AuthenticationException, ServerOutOfDateException {

    con = new TcpConnection(new AuthenticationHandler(GMOTE_CLIENT_VERSION, MINIMUM_SERVER_VERSION));
    Log.i(DEBUG_TAG, "Connecting to server: " + server.getIp() + ":" + server.getPort());
    con.connectToServerAsync(server.getPort(), server.getIp(), (DataReceiverIF) Remote.this, TIMEOUT, password);
  }

  protected synchronized void queuePacket(AbstractPacket packet) {
    try {
      packetQueue.put(packet);
    } catch (InterruptedException e) {
      Log.e(DEBUG_TAG, e.getMessage(), e);
    }
  }

  public void handleReceiveData(AbstractPacket reply, TcpConnection connection) {
    if (callback != null) {
      System.out.println("handleRecieveData1()");
      callback.sendMessage(Message.obtain(callback, -1, reply));
    } else {
      System.out.println("handleRecieveData2()");
      Log.w(DEBUG_TAG, "Received a packet, but call back is null, so I won't be able to deliver it to anyone.");
    }
  }

  public void getServerList(Handler findServerCallback) {
    Thread serverFinder = new Thread(new ServerFinder(findServerCallback));
    serverFinder.start();
  }

  protected class ServerFinder implements Runnable {

    private Handler findServerCallback;

    public ServerFinder(Handler findServerCallback) {
      this.findServerCallback = findServerCallback;
    }

    public void run() {
      Log.e(DEBUG_TAG, "Creating MC");
      MulticastClient mc = new MulticastClient();

      ServerFoundHandler serverFoundHandler = new ServerFoundHandler() {

        public void onServerFound(ServerInfo server) {
          if (findServerCallback != null) {
            findServerCallback.sendMessage(Message.obtain(findServerCallback, SERVER_LIST_ADD_SERVER, server));
          } else {
            Log.w(DEBUG_TAG, "Find Server callback was null. We can't notify anyone that we found a new server.");
          }
        }};

      mc.findServers(FINDSERVERS_TIMEOUT, serverFoundHandler);
      Log.e(DEBUG_TAG, "Got Servers");
      if (findServerCallback != null) {
        findServerCallback.sendMessage(Message.obtain(findServerCallback, SERVER_LIST_DONE));
      } else {
        Log.w(DEBUG_TAG, "Find Server callback was null. We can't notify anyone that find server has finished.");
      }
    }
  }

  class PacketSender implements Runnable {

    public void run() {
      AbstractPacket packet;
      while (true) {
        // Get the packet that is at the head of the queue, waiting if
        // necessary.
        try {
          packet = packetQueue.take();
        } catch (InterruptedException e) {
          Log.w(DEBUG_TAG, e.getMessage(), e);
          packet = null;
        } catch (Exception e) {
          Log.e(DEBUG_TAG, e.getMessage(), e);
          packet = null;
          createNewQueue();
        }

        if (packet != null) {
            try {
              sendPacketToServer(packet);
            } catch (Exception e) {
              Log.d(DEBUG_TAG, "Send packet failed. " + e.getMessage(), e);
              disconnect();
            }
          }

      }
    }

    private synchronized void createNewQueue() {
      packetQueue = new LinkedBlockingQueue<AbstractPacket>(); // recreate the queue if problem occured
    }

    private synchronized void sendPacketToServer(AbstractPacket packet) throws IOException {
      // Try to connect.
      // We'll try this twice since the connection may be down but we don't know about it.

      boolean tryAgain = false;
      do  {
        if (con != null ||  connect(false)) {
          try {
            con.sendPacket(packet);
            tryAgain = false;
          } catch (IOException e) {
            Log.e(DEBUG_TAG, e.getMessage(), e);
            disconnect();
            tryAgain = (tryAgain == false);
            if (!tryAgain) {
              if (callback != null) {
                callback.sendEmptyMessage(CONNECTION_FAILURE);
              } else {
                Log.e(DEBUG_TAG, "Unable to notify client of io error in send packet since callback is null");
              }
            }
          }
        } else {
          tryAgain = false;
        }
      } while (tryAgain);
    }
  }

  public String getSessionId() {
    if (con == null) {
      return null;
    }
    return con.getSessionId();
  }

  public ServerInfo getServer() {
    return server;
  }
}