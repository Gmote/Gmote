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

package org.gmote.common.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gmote.common.TcpConnection;
import org.gmote.common.packet.AuthenticationReply;
import org.gmote.common.packet.AuthenticationReq;


/**
 * Handles authentication between the client and server. The authentication
 * process goes as follows: 1. The user enters his password on the server side
 * when the program is installed. 2. The device connects to the server. 3. The
 * server sends a 'challenge' to the device. This is a random number. 4. The
 * client responds to the challenge by generating a hash of the challenge
 * appended to the user's password. 5. The server verifies the client's
 * response.
 * 
 * This is a simple approach which is intended to prevent simple attacks, such
 * as a roommate taking control of his friend's server because they are on the
 * same WiFi network.
 * 
 * @author Marc
 */
public class AuthenticationHandler {
  public String getAppVersion() {
    return appVersion;
  }

  private static Logger LOGGER = Logger.getLogger(AuthenticationHandler.class.getName());
  
  private static final String ENCODING_NAME = "iso-8859-1";
  private static final String HASH_FUNCTION_NAME = "SHA-1";
  
  // When called from the server, this is the server version. When called from
  // the client, this is the client version.
  String appVersion;
  
  // When called from the server, this is the minimum client version supported.
  // When called from the client, this is the minimum server version supported.
  String hisMinimumVersion;

  /**
   * Creates an authentication handler. This class is shared between the client
   * and server, which means the version number passed in are relative the
   * caller.
   * 
   * @param appVersion
   *          When called from the server, this is the server version. When
   *          called from the client, this is the client version.
   * @param hisMinimumVersion
   *          When called from the server, this is the minimum client version
   *          supported. When called from the client, this is the minimum server
   *          version supported.
   */
  public AuthenticationHandler(String appVersion, String hisMinimumVersion) {
    this.appVersion = appVersion;
    this.hisMinimumVersion = hisMinimumVersion;
  }
  
  /**
   * Generates a reply that the client should send to an authentication challenge.
   * 
   * @param password
   *          the password provided by the user
   * @param challenge
   *          the challenge that was issued by the server
   * @throws NoSuchAlgorithmException
   *           if the hash algorithm was not found
   * @throws UnsupportedEncodingException
   *           if the string encoding is not supported
   */
  public AuthenticationReply generateReplyToChallenge(String password, String challenge)
      throws NoSuchAlgorithmException, UnsupportedEncodingException {
    return new AuthenticationReply(computeChallengeResponse(password, challenge), appVersion);
  }
  

  /**
   * Generates a hash of a server's challenge.
   * 
   * @param password
   *          the password provided by the user
   * @param challenge
   *          the challenge that was issued by the server
   * @throws NoSuchAlgorithmException
   *           if the hash algorithm was not found
   * @throws UnsupportedEncodingException
   *           if the string encoding is not supported
   */
  private byte[] computeChallengeResponse(String password, String challenge) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest md;
    md = MessageDigest.getInstance(HASH_FUNCTION_NAME);

    String response = password + challenge;
    md.update(response.getBytes(ENCODING_NAME), 0, response.length());
    return md.digest();
  }

  /**
   * Generates a unique challenge that the client will have to respond to.
   * 
   * @return
   */
  public String generateServerChallenge() {
    // Generate a large random number.
    Random rnd = new Random();
    long rndNum = rnd.nextLong();

    // Also append the time to this challenge. This will slightly improve the
    // chances that we do not pass the same challenge twice.
    long time = Calendar.getInstance().getTimeInMillis();

    return Long.toString(time) + Long.toString(rndNum);
  }

  /**
   * Allows the server to authenticate the client.
   * 
   * @throws AuthenticationException when there is a problem authenticating the user
   * @throws IncompatibleClientException 
   */
  public void performAuthentication(TcpConnection con, String expectedPassword, String challenge) throws AuthenticationException, IncompatibleClientException {

    // Send a challenge to the user.
    try {
      con.sendPacket(new AuthenticationReq(challenge, appVersion));

      // Wait for the response.
      LOGGER.info("Waiting for authentication reply");
      AuthenticationReply packet = null;
      try {
        packet = (AuthenticationReply) con.readPacket();
        LOGGER.info("Authentication reply received.");
      } catch (SocketTimeoutException e) {
        LOGGER.warning("Authentication failed. The client took too long to respond.");
        throw new AuthenticationException();
      }
      
      String clientVersion = packet.getClientVersion();
      LOGGER.warning("Authentication attempt: server version = " + appVersion
          + " - client version = " + clientVersion);
      
      
      if (!isVersionCompatible(clientVersion)) {
        throw new IncompatibleClientException(
            "The Gmote client that is on your phone is out of date. Please update your client by going to the Android Market");
      }
      
      final byte[] challengeReply = packet.getChallengeReply();
 
      // Determine what the expected response should be.
      final byte[] expectedChallengeReply = computeChallengeResponse(expectedPassword, challenge);

      if (!Arrays.equals(challengeReply, expectedChallengeReply)) {
        throw new AuthenticationException();
      }

    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      throw new AuthenticationException();
    } catch (NoSuchAlgorithmException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      throw new AuthenticationException();
    } catch (ClassNotFoundException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      throw new AuthenticationException();
    } catch (ClassCastException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      throw new AuthenticationException();
    }
  }

  public boolean isVersionCompatible(String hisCurrentVersion) {
    int[] minVersion = convertVersion(hisMinimumVersion);
    int[] hisVersion = convertVersion(hisCurrentVersion);
    
    if (hisVersion[0] > minVersion[0]) {
      return true;
    } else if (hisVersion[0] < minVersion[0]) {
      return false;
    }

    if (hisVersion[1] > minVersion[1]) {
      return true;
    } else if (hisVersion[1] < minVersion[1]) {
      return false;
    }

    if (hisVersion[2] < minVersion[2]) {
      return false;
    }
    
    return true;
    
  }
  
  /**
   * Converts a string version number into ints.
   * 
   * @param versionNumber
   *          a version number in the form 1.2.3 or 1.2
   * @return an array of 3 integers representing the version number.
   */
  private static int[] convertVersion(String versionNumber) {
    String versionSplit[] = versionNumber.split("\\.");
    int version[] = new int[3];
    version[0] = Integer.parseInt(versionSplit[0]);
    version[1] = Integer.parseInt(versionSplit[1]);
    if (versionSplit.length == 3) {
      // Some older version of the server only had two digits (ex: 1.2)
      version[2] = Integer.parseInt(versionSplit[2]);
    } else {
      version[2] = 0;
    }
    return version;
  }
}
