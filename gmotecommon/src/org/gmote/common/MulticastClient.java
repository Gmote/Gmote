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

package org.gmote.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gmote.common.Protocol.UdpPacketTypes;

//import android.util.Log;

//import com.r3mote.server.R3moteServer;

public class MulticastClient {
  private static Logger LOGGER = Logger.getLogger(MulticastClient.class.getName());  
  public static final String FIELD_SEPARATOR = "|";
  public static final String FIELD_SEPARATOR_REGEX = "\\|";

  /**
   * Tries to determine the name and IP of all available servers. Returns null
   * if not found.
   * 
   * @param timeout the maximum amount of time to wait until we stop listening for servers.
   * @param serverFoundHandler
   *          A handler that will be notified as servers are found. This is
   *          useful if you want to display servers in a list at they arrive.
   *          Pass null if you do not wish to receive notifications.
   * @return
   */
  public List<ServerInfo> findServers(int timeout, ServerFoundHandler serverFoundHandler) {
    List<ServerInfo> servers = new ArrayList<ServerInfo>();

    try {

      DatagramSocket socket = new DatagramSocket();
      InetAddress groupAddr = InetAddress.getByName("230.0.0.1");
      
      byte[] outbuf = createDiscoveryRequest(socket);
      sendPacket(socket, groupAddr, outbuf);
      
      // Send out a legacy discovery request in case the server's version is <= 1.2
      //outbuf = createLegacyDiscoveryRequest(socket);
      //sendPacket(socket, groupAddr, outbuf);
      
      DatagramPacket reply;
      
      long startTime = System.currentTimeMillis();
      
      int elapsedTime = 0;
      while (elapsedTime < timeout) {
         
        socket.setSoTimeout(Math.min(timeout - elapsedTime, 3000));

        byte[] replyBuffer = new byte[1024];
        reply = new DatagramPacket(replyBuffer, replyBuffer.length);
        
        try {
          socket.receive(reply);
          String dataReceived = new String(reply.getData()).trim();
          String[] fields = dataReceived.split(FIELD_SEPARATOR_REGEX);
          if (fields.length == 4) {
            ServerInfo servInfo = new ServerInfo(fields[0], fields[1], Integer.parseInt(fields[2]), Integer.parseInt(fields[3]));
            servers.add(servInfo);
            if (serverFoundHandler != null) {
              serverFoundHandler.onServerFound(servInfo);
            }
          }
          elapsedTime = (int) (System.currentTimeMillis() - startTime);
        } catch (SocketTimeoutException ste) {
          // Resend the service notification request.
          elapsedTime = (int) (System.currentTimeMillis() - startTime);
          if (elapsedTime < timeout) {
            sendPacket(socket, groupAddr, outbuf);
          }
        }
      }

      return servers;
    } catch (SocketException e) {
      LOGGER.log(Level.SEVERE,e.getMessage(),e);
      return servers;
    } catch (IOException e) {
      return servers;
    }
  }

  private void sendPacket(DatagramSocket socket, InetAddress groupAddr, byte[] outbuf)
      throws IOException {
    DatagramPacket packet;
    packet = new DatagramPacket(outbuf, outbuf.length, groupAddr,
        9901);
    outbuf = createLegacyDiscoveryRequest(socket);
    socket.send(packet);
  }
  
  /**
   * Used by server who's versions are <= 1.2
   * @param socket
   * @return
   */
  private byte[] createLegacyDiscoveryRequest(DatagramSocket socket) {
    byte[] outbuf = ("gmoteping" + FIELD_SEPARATOR + socket.getLocalPort()).getBytes();
    return outbuf;
  }

  /**
   * Creates a packet that has an id number and the port that we are listening
   * on. We don't use any object streaming since this channel is also used for
   * mouse movements packets which needs to be extremely fast.
   * 
   * @param socket
   * @return
   */
  private byte[] createDiscoveryRequest(DatagramSocket socket) {
    byte[] outbuf = new byte[5];
    outbuf[0] = UdpPacketTypes.SERVICE_DISCOVERY.getId();
    
    int port = socket.getLocalPort();
    outbuf[1] = (byte)port;
    outbuf[2] = (byte)(port >>> 8);
    outbuf[3] = (byte)(port >>> 16);
    outbuf[4] = (byte)(port >>> 24);
    
    return outbuf;
  }
  
  public interface ServerFoundHandler {
    /**
     * Called when a server is found.
     * @param server The server that was found.
     */
    public void onServerFound(ServerInfo server);
  }
}
