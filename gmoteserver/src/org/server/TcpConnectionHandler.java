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

import java.io.StreamCorruptedException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.gmote.common.DataReceiverIF;
import org.gmote.common.PasswordProvider;
import org.gmote.common.TcpConnection;
import org.gmote.common.security.AuthenticationHandler;
import org.gmote.server.media.MediaInfoUpdater;
import org.gmote.server.visualtouchpad.VisualTouchpad;

public class TcpConnectionHandler {
  private static final Logger LOGGER = Logger.getLogger(TcpConnectionHandler.class.getName());
  private static final int MAX_OLD_SESSION_IDS = 5;

  private static TcpConnectionHandler instance = null;
  
  private static DataReceiverIF dataReceiver;
  
  private List<String> addressListeningOn = new ArrayList<String>();
  
  private UpnpUtil upnpUtil = new UpnpUtil();

  private TcpConnectionHandler() {
    
  }
  
  public static synchronized TcpConnectionHandler instance() {
    if (instance == null) {
      instance = new TcpConnectionHandler();
    }
    return instance;
  }
  
  public static synchronized TcpConnectionHandler instance(DataReceiverIF newDataReceiver) {
    dataReceiver = newDataReceiver;
    if (instance == null) {
      instance = new TcpConnectionHandler();
    }
    return instance;
  }
  
  public synchronized void addConnectionListener(InetAddress address) {
    addressListeningOn.add(address.getHostAddress().toLowerCase());
    int port = upnpUtil.getPort(address);
    new Thread(new ConnectionReceiverThread(port)).start();
  }
  
  public synchronized boolean isListeningOnAddress(InetAddress address) {
    return addressListeningOn.contains(address.getHostAddress().toLowerCase());
  }
  
  public synchronized void listenOnAllIpAddresses() throws SocketException {
    for (InetAddress address : ServerUtil.findAllLocalIpAddresses(true)) {
      addConnectionListener(address);
    }
  }
  
  private class ConnectionReceiverThread implements Runnable {

    private int tcpPort;

    public ConnectionReceiverThread(int tcpPort) {
      this.tcpPort = tcpPort;
    }

    @Override
    public void run() {
      // Circular list that will contain the last 5 session ids.
      List<String> latestSessionIds = new ArrayList<String>(MAX_OLD_SESSION_IDS);

      PasswordProvider passProvider = new PasswordProvider() {
        public String fetchPassword() {
          return StringEncrypter.readPasswordFromFile();
        }
      };

      AuthenticationHandler authHandler = new AuthenticationHandler(GmoteServer.VERSION,
          GmoteServer.MINIMUM_CLIENT_VERSION);

      while (true) {
        TcpConnection con = null;
        try {
          LOGGER.info("Waiting for TCP connection on port: " + tcpPort);
          con = new TcpConnection(authHandler);
          Thread thread = con.listenForConnections(tcpPort, dataReceiver, passProvider);
          if (thread != null) {
            MediaInfoUpdater.instance().setClientConnection(con);
            MulticastServerThread.setConnectedClientIp(con.getConnectedClientAddress());
            addToSessionList(con.getSessionId(), latestSessionIds);
            VisualTouchpad.instance().setConnection(con);
          }
        } catch (StreamCorruptedException e) {
          // This may be an HTTP request. Try to handle it.
          LOGGER
              .warning("Encountered a StreamCorruptedException: trying to handle it as an HTTP request");
          Socket connectionSocket;
          if (con != null && (connectionSocket = con.getConnectionSocket()) != null) {
            GmoteHttpServer httpServer = new GmoteHttpServer(connectionSocket);
            httpServer.handleHttpRequestAsync(latestSessionIds);
          } else {
            LOGGER.log(Level.SEVERE,
                "Unable to handle StreamCorruptedException: " + e.getMessage(), e);
          }

        } catch (BindException e) {
          // The port is already in use. We'll exit.
          LOGGER.log(Level.SEVERE, e.getMessage(), e);
          String errorMessage = "Unable to use port: "
              + tcpPort
              + ". There may be an instance of"
              + " Gmote already running. Please close it and try again. For more help, please visit:\nhttp://www.gmote.org";
          LOGGER.warning(errorMessage);
          JOptionPane.showMessageDialog(null, errorMessage);
          System.exit(1);
        } catch (Exception e) {
          // Catching all exceptions since this is the top layer of our app and
          // we'll try to recover from these exceptions.
          LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
      }

    }

    private void addToSessionList(String sessionId, List<String> latestSessionIds) {
      if (latestSessionIds.size() >= MAX_OLD_SESSION_IDS) {
        latestSessionIds.remove(0);
      }
      latestSessionIds.add(sessionId);
    }

  }
}
