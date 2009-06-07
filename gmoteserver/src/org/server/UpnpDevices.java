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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gmote.common.TcpConnection;

import net.sbbi.upnp.impls.InternetGatewayDevice;

public class UpnpDevices {
  final private static Logger LOGGER = Logger.getLogger(TcpConnection.class
      .getName());
  
  private static final int UPNP_TIMEOUT = 1000 * 15;
  private static UpnpDevices instance = null;
  
  InternetGatewayDevice[] devices = null;
  
  public UpnpDevices() {
    initDevices();
  }
  
  public InternetGatewayDevice[] getDevices() {
    return devices;
  }

  public static synchronized UpnpDevices instance() {
    if (instance == null) {
      instance = new UpnpDevices();
    }
    return instance;
  }
  
  private void initDevices() {  
    try {
      devices = InternetGatewayDevice.getDevices(UPNP_TIMEOUT);      
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
    
    if (devices == null || devices.length == 0) {
      LOGGER.log(Level.WARNING,
              "Unable to contact the router through upnp. This means that we will not be able " +
              "to setup port forwarding automatically. If you want to use this application " + 
              "outside of your home network, you may have to setup port forwarding on your router manually.");
    } else {
      LOGGER.info("Number of upnp devices found: " + devices.length);
    }
    
    
  }
}
