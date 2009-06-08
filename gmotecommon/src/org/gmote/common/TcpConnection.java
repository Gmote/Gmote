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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gmote.common.Protocol.Command;
import org.gmote.common.Protocol.ServerErrorType;
import org.gmote.common.packet.AbstractPacket;
import org.gmote.common.packet.AuthenticationReq;
import org.gmote.common.packet.ServerErrorPacket;
import org.gmote.common.packet.SimplePacket;
import org.gmote.common.security.AuthenticationException;
import org.gmote.common.security.AuthenticationHandler;
import org.gmote.common.security.IncompatibleClientException;


/**
 * Container to hold a connection to a remote host. It has two constructors to
 * allow us either to wait for an incoming connection or to connect to a remote
 * host. Allows us to send data to the remote host. It also starts a thread that
 * listens for incoming data and notifies the appropriate DataReceiverIF.
 *
 * @author Marc
 *
 */
public class TcpConnection {
  private static final int CONNECTION_ESTABLISH_TIMEOUT = 1000 * 3;

  final private static Logger LOGGER = Logger.getLogger(TcpConnection.class
      .getName());

  private static ServerSocket mServer = null;

  private Socket connectionSocket;

  private ObjectOutputStream connectionOutput;

  private ObjectInputStream connectionInput;

  private DataReceiverIF receiver; // Who to notify when data is received.

  private AuthenticationHandler authHandler;

  private String sessionId = null;
  /**
   * Constructor typically used when setting up a server. Should call the
   * listenForConnections method after calling this constructor.
   *
   */
  public TcpConnection(AuthenticationHandler authHandler) {
    this.authHandler = authHandler;
  }

  /**
   * Listens for connections from incoming clients.
   *
   * @param port
   *          the port to listen on.
   * @param receiver
   *          an object that implements a method which can be called
   *          asynchronously when data is received from the client.
   * @param passProvider
   *          an object that supplies the password that must be entered by a
   *          client in order to establish a connection.
   *
   * @return A pointer to the new data handling thread, or null if
   *         startNewThread is false.
   * @throws IOException
   * @throws StreamCorruptedException
   *           if the stream is not a java object stream. For example, this
   *           exception is thrown when an HTTP client tries to establish a
   *           connection. It can be safely caught and safely re-opened into a
   *           non-object stream for furthur processing.
   */
  public Thread listenForConnections(int port, DataReceiverIF receiver, PasswordProvider passProvider)
      throws IOException, StreamCorruptedException {
    // Wait for a connection.
    System.out.println("Waiting for a connection on port: " + port);

    ServerSocket server = getServerInstance(port);
    Socket serverSocket = server.accept();

    String password = passProvider.fetchPassword();

    handleConnectionEstablished(serverSocket, receiver, false);

    boolean authSucceeded = handleServerSideAuthentication(serverSocket, password);
    if (!authSucceeded) {
      return null;
    }
    return startPacketReceivingThread();
  }

  private static ServerSocket getServerInstance(int port) throws IOException {
    if (mServer == null) {
      mServer = new ServerSocket(port);
    }
    return mServer;
  }

  /**
   * Allows the program to connect to a remote host. A separate listening thread
   * will be launched to handle packet reception
   *
   * @param port
   * @param host
   * @param receiver
   *          Will be notified when data arrives.
   * @throws IOException
   * @throws ServerOutOfDateException
   *           If the server is out of date. Note that if this exception is
   *           thrown, the listening thread won't be established but we'll still
   *           establish a connection to the server in order to allow the client
   *           to notify the server that it should update itself.
   * @returns A pointer to the thread that is launched.
   */
  public Thread connectToServerAsync(int port, String host,
      DataReceiverIF receiver, int timeout, String password) throws IOException, AuthenticationException, ServerOutOfDateException {
    // Establish a connection to the remote host.
    System.out.println("Connecting to host: " + host + ":" + port);
    InetAddress address = InetAddress.getByName(host);
    InetSocketAddress socketAddress = new InetSocketAddress(address, port);
    Socket remoteHost = new Socket();

    
    remoteHost.connect(socketAddress, timeout);

    System.out.println("Successfully connected to host: " + host + ":" + port);
    handleConnectionEstablished(remoteHost, receiver, true);
    handleClientSideAuthentication(password);
    return startPacketReceivingThread();

  }

  /**
   * Allows the program to connect to a remote host. This is synchronous,
   * therefore the caller must call readPacket() on this object in order to
   * receive data.
   *
   * @throws ServerOutOfDateException
   *           If the server is out of date. Note that if this exception is
   *           thrown, we'll still establish a connection to the server in order
   *           to allow the client to notify the server that it should update
   *           itself.
   */
  public void connectToServerSync(int port, String host, String password) throws IOException,
      AuthenticationException, ServerOutOfDateException {
    // Establish a connection to the remote host.
    System.out.println("Connecting to host: " + host + ":" + port);
    InetAddress address = InetAddress.getByName(host);
    Socket remoteHost = new Socket(address, port);

    System.out.println("Successfully connected to host: " + host + ":" + port);
    handleConnectionEstablished(remoteHost, receiver, true);
    handleClientSideAuthentication(password);

  }

  /**
   * Opens up the proper data streams for the connection.
   *
   * @throws IOException
   *           if there was a problem opening a stream.
   * @throws StreamCorruptedException
   *           if the stream is not a java object stream. For example, this
   *           exception is thrown when an HTTP client tries to establish a
   *           connection. It can be safely caught and safely re-opened into a
   *           non-object stream for furthur processing.
   */
  private void handleConnectionEstablished(Socket connectionSocket,
      DataReceiverIF receiver, boolean outputStreamFirst)
      throws IOException, StreamCorruptedException {
    this.connectionSocket = connectionSocket;
    this.receiver = receiver;
 
    if (outputStreamFirst) {
      // The client should construct the output stream first, since if both the
      // server and the client construct an input stream without having created
      // an output stream, both will lock and wait for an initial amount of data
      // to be sent. We need to construct the server's input stream first since,
      // in the case of an http request, we don't want those initial bytes
      // written (which get automatically written when you create
      // ObjectOutputStream).
      connectionOutput = new ObjectOutputStream(connectionSocket
          .getOutputStream());
    }
    
    int originalSoTimeout = connectionSocket.getSoTimeout();
    try {
      connectionSocket.setSoTimeout(CONNECTION_ESTABLISH_TIMEOUT);
      connectionInput = new ObjectInputStream(connectionSocket.getInputStream());
    } finally {
      connectionSocket.setSoTimeout(originalSoTimeout);
    }
    
    if (!outputStreamFirst) {
      connectionOutput = new ObjectOutputStream(connectionSocket
          .getOutputStream());
    }

  }

  private Thread startPacketReceivingThread() {
    // Start the receiving thread which will wait for data to arrive.
    DataReceiver receiverThread = new DataReceiver(this);
    receiverThread.start();
    return receiverThread;
  }

  /**
   * Handles client side authentication.
   *
   * @param password
   * @throws AuthenticationException
   * @throws ServerOutOfDateException
   */
  private void handleClientSideAuthentication(String password) throws IOException,
      AuthenticationException, ServerOutOfDateException {
    try {
      AuthenticationReq challenge = (AuthenticationReq) readPacket();
      sendPacket(authHandler.generateReplyToChallenge(password, challenge
          .getChallenge()));
      AbstractPacket result = readPacket();
      if (result.getCommand() != Command.SUCCESS) {
        throw new AuthenticationException();
      }
      if (!authHandler.isVersionCompatible(challenge.getServerVersion())) {
        throw new ServerOutOfDateException("The Gmote server is out of date. Server Version: " + challenge.getServerVersion() + " Client Version: " + authHandler.getAppVersion(), challenge.getServerVersion());
      }
      sessionId = challenge.getChallenge();
    }catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Performs authentication on the server side. Returns true if the
   * authentication succeeded.
   */
  private boolean handleServerSideAuthentication(Socket connectionSocket, String password)
      throws IOException {
    // Perform authentication.
    try {
      String challenge = authHandler.generateServerChallenge();
      authHandler.performAuthentication(this, password, challenge);
      sendPacket(new SimplePacket(Command.SUCCESS));
      sessionId = challenge;
      return true;
    } catch (AuthenticationException e) {
      LOGGER.log(Level.WARNING, "Authentication of client failed: "
          + connectionSocket.getRemoteSocketAddress());
      sendPacket(new ServerErrorPacket(ServerErrorType.AUTHENTICATION_FAILURE.ordinal(),
      "Authentication Failed. You may have entered the wrong password."));
      sleep(1000);
      connectionSocket.close();
      connectionOutput.close();
      connectionInput.close();
      return false;
    } catch (IncompatibleClientException e) {
      LOGGER.log(Level.WARNING, "Authentication of client failed: " + e.getMessage());
      // Sending a 'success' packet followed by an error packet. This is due
      // to backwards compatibility since <= 1.2 clients don't expect to
      // receive update notifications.
      sendPacket(new SimplePacket(Command.SUCCESS));

      // Send this error packet for older clients that don't handle the update request.
      sendPacket(new ServerErrorPacket(ServerErrorType.INCOMPATIBLE_CLIENT.ordinal(), e.getMessage()));

      connectionSocket.close();
      connectionOutput.close();
      connectionInput.close();
      return false;
    }

  }

  /**
   * Sends a packet over the network.
   */
  public void sendPacket(AbstractPacket packet) throws IOException {
    connectionOutput.writeObject(packet);
    connectionOutput.flush();
    connectionOutput.reset();
  }

  /**
   * Allows the caller to read a packet. This should only be used in conjunction
   * with connectToServerSync().
   */
  public AbstractPacket readPacket() throws IOException, ClassNotFoundException {
    return (AbstractPacket) connectionInput.readObject();
  }

  /**
   * Allows the caller to read a packet. This should only be used in conjunction
   * with connectToServerSync().
   * @param timeout milliseconds to wait for read operation.
   */
  public AbstractPacket readPacket(int timeout) throws IOException, ClassNotFoundException, SocketTimeoutException {

    try {
      connectionSocket.setSoTimeout(timeout);
      return (AbstractPacket) connectionInput.readObject();
    } finally {
      connectionSocket.setSoTimeout(0);
    }


  }

  /**
   * Thread used to monitor the connection and handle incomming messages.
   *
   * @author Marc
   *
   */
  public class DataReceiver extends Thread {
    // The TcpConnection that started this thread.
    TcpConnection connection;

    /**
     *
     * @param connection
     *          The connection that this object is listening on.
     */
    public DataReceiver(TcpConnection connection) {
      super("DataReceiver");
      this.connection = connection;
    }

    @Override
    public void run() {
      // Receives data sent on the TCP connection.
      try {
        while (true) {
          AbstractPacket packet = (AbstractPacket) connectionInput.readObject();
          receiver.handleReceiveData(packet, connection);
        }
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      } catch (ClassNotFoundException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      } finally {
        closeConnection();
      }
    }
  }

  /**
   * Attempts to release the port.
   *
   */
  public void closeConnection() {
    try {
      if (connectionSocket != null) {
        connectionSocket.close();
      }

    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public InetAddress getConnectedClientAddress() {
    return connectionSocket.getInetAddress();
  }

  public boolean isConnected() {
    return (connectionSocket == null) ? false : connectionSocket.isConnected()
        && !connectionSocket.isClosed();
  }

  public Socket getConnectionSocket() {
    return connectionSocket;
  }

  public String getSessionId() {
    return sessionId;
  }
  
  private void sleep(long timeInMili) {
    try {
      Thread.sleep(timeInMili);
    } catch (InterruptedException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }
}
