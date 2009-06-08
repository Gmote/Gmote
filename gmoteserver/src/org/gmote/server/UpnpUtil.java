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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPResponseException;

import org.gmote.server.settings.PreferredPorts;

public class UpnpUtil {
  // TODO(mstogaitis): figure out what to do about default ports (should have a
  // reasonable default for client input field defaults and discovery protocol.
  
  //TODO(mstogaitis): Add a button in the ui to 'try upnp setup again'
  
  private static final int DEFAULT_STARTING_PORT = 8851;
  private static final int MAX_PORT = 9851;
  private static final Logger LOGGER = Logger.getLogger(UpnpUtil.class.getName());
  private static final String APP_NAME = "Gmote";
  
  public int getPort(InetAddress address) {
      String ip = address.getHostAddress();
      Integer port = PreferredPorts.instance().getPreferredPort(ip);
      if (port == null || !isPortAvailableLocally(port)) {
        // Assign a new port to this ip.
        port = selectNewPort("TCP");
        PreferredPorts.instance().addPort(ip, port);
        renewRouterNat(port, address, "TCP");
      }
      return port;
  }
  
  
  private int selectNewPort(String protocol) {
    LOGGER.info("Selecting new port");
    Integer availableLocalPort = null;
    for (int selectedPort = DEFAULT_STARTING_PORT; selectedPort <= MAX_PORT; selectedPort++) {
      if (!PreferredPorts.instance().isPortAssigned(selectedPort)) {
        if (isPortAvailableLocally(selectedPort)) {
          LOGGER.info("Port " + selectedPort + " is available locally. Checking NAT settings");
          if (availableLocalPort == null) {
            availableLocalPort = selectedPort;
          }
           
          if (isPortNatAvailable(selectedPort, protocol)) {
            LOGGER.info("Available port found: " + selectedPort);
            return selectedPort;
          }
        }
      }
    }
    
    if (availableLocalPort != null) {
      LOGGER.warning("Unable to find an available NAT port. Returning first available local port: "  + availableLocalPort);
      return availableLocalPort;
    } else {
      LOGGER.warning("Unable to find available port. Returning default port. " + DEFAULT_STARTING_PORT);
      return DEFAULT_STARTING_PORT;
    }
  }

  private boolean isPortNatAvailable(int selectedPort, String protocol) { 
    InternetGatewayDevice[] devices = UpnpDevices.instance().getDevices();
    
    for (InternetGatewayDevice device : devices) {
      try {
        LOGGER.info("Looing for NAT mapping for port " + selectedPort);
        ActionResponse response = device.getSpecificPortMappingEntry(null, selectedPort, protocol);
        if (response != null) {
          LOGGER.info("NAT port mapping already exists for port " + selectedPort + " " + response);
          return false;
        }
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      } catch (UPNPResponseException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }
    }
    
    LOGGER.info("Found port that doesn't interfere with any NAT settings:  " + selectedPort);
    return true;
  }

  private String createPortDescription(InetAddress ipAddress) {
    return APP_NAME + "-" + ipAddress.getHostName();
  }

  private boolean isPortAvailableLocally(int selectedPort) {
    try {
      ServerSocket serverSocket = new ServerSocket(selectedPort);
      serverSocket.close();
      LOGGER.info("Found port that is available locally: " + selectedPort);
      return true;
    } catch (IOException e) {
      LOGGER.log(Level.INFO, "Port is not available locally: " + selectedPort + " " +  e.getMessage(), e);
      return false;
    }
  }

  /**
   * Renews or creates a NAT entry for this computer that will be available for 1 month.
   */
  private void renewRouterNat(int portChosen, InetAddress inetAddress, String protocol) {
    
    InternetGatewayDevice[] devices = UpnpDevices.instance().getDevices();
    if (devices == null || devices.length == 0) {
      LOGGER.warning("Unable to create NAT port forwarding for this router. If you want to use this application outside of your home network, you may need to setup port forwarding manually for port " + portChosen);
      return;
    }
    
    for (InternetGatewayDevice device : devices) {
      // Try to remove the port mapping if it already exists.
      try {
        LOGGER.info("Trying to delete a port mapping, if it exists " + portChosen + " " + protocol);
        device.deletePortMapping(null, portChosen, protocol);
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      } catch (UPNPResponseException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }
      
      addPort(portChosen, portChosen, inetAddress, protocol, 0, device, 0);
    }
  }
  
  /**
   * Adds a port to the router's NAT table. We will first try to add the port
   * and renew it every (to handle changing local ip's).
   * 
   * @param portChosen
   * @param externalPort
   * @param inetAddress
   * @param protocol
   *          tcp or udp
   * @param leaseDuration
   *          in seconds
   * @param device
   */
  private void addPort(int portChosen, int externalPort, InetAddress inetAddress, String protocol, int leaseDuration, 
      InternetGatewayDevice device, int tries) {
    LOGGER.info("Trying to add a port mapping: " + portChosen + " " + inetAddress + " " + protocol + " " + leaseDuration + " " + tries);
    if (tries >= 5) {
      // This should never happen, unless there's some problem with the return
      // error messages of the router.
      LOGGER.warning("Max number of tries reached in addPort");
      return;
    }
    // Add a new port mapping.
    try {
      boolean success = device.addPortMapping(createPortDescription(inetAddress), null, portChosen, externalPort, inetAddress.getHostAddress(), leaseDuration, protocol);
      if (!success) {
        LOGGER.warning("Port renew failed. Port already mapped by another client");
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    } catch (UPNPResponseException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      if (e.getDetailErrorCode() == 727) {
        // 727 ExternalPortOnlySupportsWildcard ExternalPort must be a wildcard and cannot be a specific port value
        addPort(portChosen, 0, inetAddress, protocol, leaseDuration, device, ++tries);
      } else if (e.getDetailErrorCode() == 725) {
        // 725 OnlyPermanentLeasesSupported The NAT implementation only supports permanent lease times on port mappings
        addPort(portChosen, portChosen, inetAddress, protocol, 0, device, ++tries);
      }    
    }
  }
  
  
  
  public static void main(String[] args) {
    //new UpnpUtil().addAppToRouterNat();
  }
}
