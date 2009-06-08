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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.gmote.common.MulticastClient;
import org.gmote.common.Protocol.UdpPacketTypes;
import org.gmote.server.settings.PreferredPorts;


/**
 * Listens on a Multicast port and returns the name and ip of this host.
 * @author Marc
 *
 */
public class MulticastServerThread implements Runnable {
  
  final private static Logger LOGGER = Logger.getLogger(MulticastServerThread.class.getName());

  public static final int MULTICAST_LISTENING_PORT = 9901;
  public static final String GROUP_NAME = "230.0.0.1";
  
  
  // The port used to exchange udp data. This is where we exchange data such as
  // mouse packets. It can be the same as the udp service discovery port, or
  // different.
  private static int serverUdpListeningPort = MULTICAST_LISTENING_PORT;
  
  private String groupName;
  // The listening port of the current socket.
  private int socketListeningPort;
  
  private static InetAddress connectedClientIp = null;
  /**
   * Creates a thread that will send out discovery notifications. We explicitly
   * listen on each local interface since this resolves the following bug which
   * is related to having multiple network cards:
   * <p>
   * 1. Listen on socket without providing a specific ip (by default, java will
   * listen on all network interfaces)
   * </p>
   * <p>
   * 2. Receive an IP request from a client
   * </p>
   * <p>
   * 3. If we have multiple local ip's (ex: a wifi connection and wired
   * connection), we won't know which ip to return (and will often return the
   * wrong one if we simply call InetAddress.getLocalHost)
   * 
   * Note: If we pass in null for 'localIpAddress', the we will revert back to
   * listening on all local ip addresses and take a guess as to which ip we
   * should return to the client. This mechanism should only be used if there is
   * a problem listening on local interfaces.
   * </p>
   * 
   */
  public MulticastServerThread(String groupName, int socketListeningPort) {
    
    this.groupName = groupName;
    this.socketListeningPort = socketListeningPort;
  }
  
  public void run() {
    MulticastSocket socket = join(groupName, socketListeningPort);
    if (socket == null) {
      String message = "Unable to join the multicast socket. Is the computer connected to a network? Exiting Gmote.";
      LOGGER.severe(message);
      JOptionPane.showMessageDialog(null, message);
      System.exit(1);
    }
    byte[] inBuffer = new byte[500];
    LOGGER.info("Listening for udp packets on " + socketListeningPort);
    int errorCount = 0;
    while (true) {
      try {
        receivePacket(socket, inBuffer);
      } catch (Exception e) {
        // Catching all exceptions here since this is the top level of our
        // multicast thread and we never want it to die.
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
        
        errorCount++;
        if (errorCount >= 10) {
          JOptionPane.showMessageDialog(null, "Too many errors in udp class. Please see the logs for more details or visit http://www.gmote.org/faq -- " + e.getMessage());
          System.exit(1);
        }
        
      }
    }
  }
  
  private MulticastSocket join(String groupName, int udpPort) {
    try {
      
      MulticastSocket msocket;
      msocket = new MulticastSocket(udpPort);
      if (groupName != null) {
        InetAddress group = InetAddress.getByName(groupName);
        msocket.joinGroup(group);
      }
      return msocket;
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE,e.getMessage(),e);
    }
    return null;
  }

  public void receivePacket(MulticastSocket multicastSocket, byte[] inBuffer) {
    try {
      DatagramPacket packet = new DatagramPacket(inBuffer, inBuffer.length);

      // Wait for packet
      LOGGER.fine("Waiting for udp packet request on port " + multicastSocket.getLocalPort());

      multicastSocket.receive(packet);

      if (inBuffer[0] == UdpPacketTypes.SERVICE_DISCOVERY.getId()) {
        handleServiceDiscoveryRequest(multicastSocket, packet, inBuffer);
      } else if (inBuffer[0] == UdpPacketTypes.MOUSE_MOVE.getId()) {
        handleMouseMoveRequest(packet, inBuffer);
      } else {
        LOGGER.info("Received unrecognized udp packet. Ignoring it.");
        //handleLegacyServiceDiscoveryRequest(multicastSocket, packet);
      }
      
      
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE,e.getMessage(),e);
    }
  }

  /**
   * Handles a mouse move request from the client. In order to accept the mouse
   * move event, the client must have an active tcp connection with our server
   * (we verify this by matching the ip address).
   * 
   * TODO(mstogaitis): Consider a better security mechanism such as signing the
   * mouse packets with the password.
   * 
   * @param packet
   * @param data
   *          a 5 byte packet, byte 0 contains an identifier, bytes 1 and 2
   *          contain a 'short' describing the XMovement, and bytes 3 and 4
   *          contain a short describing the YMovement.
   */
  private void handleMouseMoveRequest(DatagramPacket packet, byte[] data) {

    InetAddress clientAddress = packet.getAddress();
    if (isCorrectIp(clientAddress)) {
      if (packet.getLength() == 5) {
        short xDiff = data[2];
        xDiff = (short) ((xDiff << 8) & 0xFF00);
        xDiff = (short) (xDiff | (data[1] & 0x00FF));

        short yDiff = data[4];
        yDiff = (short) ((yDiff << 8 & 0xFF00));
        yDiff = (short) (yDiff | (data[3] & 0x00FF));
        TrackpadHandler.instance().handleMoveMouseCommand(xDiff, yDiff);
      }
    } else {
      LOGGER
          .warning("Received a mouse move request from an ip who is not connected to us: packetIp="
              + clientAddress + " tcpConnectionIp=" + connectedClientIp + ". Ignoring the packet.");
    }
  }
  
  private static synchronized boolean isCorrectIp(InetAddress clientAddress) {
    return clientAddress.equals(connectedClientIp);
  }

  public static synchronized void setConnectedClientIp(InetAddress clientIp) {
    connectedClientIp = clientIp;
  }
  
  /**
   * Sends a packet to the client identifying our server's name and ip.
   * 
   * @param multicastSocket
   * @param packet
   * @param data
   *          Byte 0 is the packet id, bytes 1 to 4 is an int identifying the
   *          port of the client to which we should send a reply.
   * @throws IOException
   * @throws UnknownHostException
   */
  private void handleServiceDiscoveryRequest(MulticastSocket multicastSocket,
      DatagramPacket packet, byte data[]) throws UnknownHostException, IOException {
    LOGGER.info("Received an ip request");
    if (packet.getLength() == 5) {
      int port = data[4] << (24 & 0xFF000000);  
      port = port | ((data[3] << 16) & 0x00FF0000);
      port = port | ((data[2] << 8) & 0x0000FF00);
      port = port | (data[1] & 0x000000FF);
      
      sendDiscoveryReply(multicastSocket, packet, port);
    }
  }

  /**
   * In version <= 1.2 of the client, we did not use the convention of having
   * the first byte of a udp packet identify the packet type. Therefore, for
   * backwards compatibility, we'll verify this packet starts with 'gmoteping|'
   * which was our original message identifier. If it doesn't we just ignore the
   * message.
   * 
   * @param multicastSocket
   * @param packet
   * @throws IOException
   */
/*  private void handleLegacyServiceDiscoveryRequest(MulticastSocket multicastSocket,
      DatagramPacket packet) throws IOException {
    LOGGER.info("Received an ip request");

    // TODO(mstogaitis): Investigate possible security issue here.
    String messageRecieved = new String(packet.getData()).trim();

    if (messageRecieved.startsWith(CLIENT_PING_PREFIX)) {
      String port = messageRecieved.substring(CLIENT_PING_PREFIX.length());
      // The client 
      int remotePort = Integer.parseInt(port);
      sendDiscoveryReply(multicastSocket, packet, remotePort);
    }
  }*/

  private void sendDiscoveryReply(MulticastSocket multicastSocket, DatagramPacket packet,
      int remotePort) throws UnknownHostException, IOException {
    // Reply with all local IPs
    byte[] replyBuff;
    
    List<InetAddress> addresses = ServerUtil.findAllLocalIpAddresses(true);

    for (InetAddress address : addresses) {
      if (!TcpConnectionHandler.instance().isListeningOnAddress(address)) {
        LOGGER.warning("Multicase server thread noticed a local ip that we are not listening on. Adding it to the listening pool");
        TcpConnectionHandler.instance().addConnectionListener(address);
      }
      
      Integer port = PreferredPorts.instance().getPreferredPort(address.getHostAddress());
      if (port == null) {
        LOGGER.severe("Prefered port is null for connection that should have been added. "
            + address.getHostAddress() + " " + PreferredPorts.instance().getPreferedPorts());
        continue;
      }
      
      replyBuff = createIpReply(address.getHostAddress(), InetAddress.getLocalHost()
          .getHostName(), port);
      DatagramPacket replyPacket = new DatagramPacket(replyBuff, replyBuff.length);
      replyPacket.setAddress(packet.getAddress());
      replyPacket.setPort(remotePort);
      LOGGER.info("Sending packet to: " + packet.getAddress());
      
      multicastSocket.send(replyPacket);
    }
  }

  /**
   * Create an IP | Hostname reply packet to send to the clients. We can't just
   * send the hostName since Android clients run under linux which has problems
   * with windows host names.
   * @param port 
   * 
   * @return
   */
  private byte[] createIpReply(String localAddress, String hostName, int port) {
    return (localAddress + MulticastClient.FIELD_SEPARATOR + hostName
        + MulticastClient.FIELD_SEPARATOR + port + MulticastClient.FIELD_SEPARATOR + serverUdpListeningPort).getBytes();

  }

  public static void listenForIpRequests(int udpPort) {
    // Ports that the server is listening on.
    serverUdpListeningPort = udpPort;
    
    MulticastServerThread multiCon;
    if (udpPort != MULTICAST_LISTENING_PORT) {
      // The user has chosen to listen for mouse events on a different port than
      // service discovery. Make sure we listen on that port as well.
      multiCon = new MulticastServerThread(null, udpPort);
      new Thread(multiCon, "UdpMouseThread").start();
    }
    
    multiCon = new MulticastServerThread(GROUP_NAME, MULTICAST_LISTENING_PORT);
    new Thread(multiCon, "MulticastThread").start();
    
    
  }
}
