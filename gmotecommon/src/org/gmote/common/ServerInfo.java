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

public class ServerInfo {
  
  public static final int DEFAULT_PORT = 8889;
  public static final int DEFAULT_UDP_PORT = 9901;
  
  private String ip = null;
  private String name = null;
  private int port = DEFAULT_PORT;
  private int udpPort = DEFAULT_UDP_PORT;
  
  public ServerInfo(String ip, String name, int port, int udpPort) {
    this.ip = ip;
    this.name = name;
    this.port = port;
    this.udpPort = udpPort;
  }

  public ServerInfo(String server, int port, int udpPort) {
    this.ip = server;
    this.port = port;
    this.udpPort = udpPort;
  }
  
  public ServerInfo(String server) {
    this.ip = server;
  }
  
  public String getIp() {
    return ip;
  }
  public String getName() {
    return name;
  }
  public int getPort() {
    return port;
  }
  public int getUdpPort() {
    return udpPort;
  }
  public String getServer() {
    if (name != null) {
      return name;
    } else if (ip != null) {
      return ip;
    } 
    return null;
  }

  @Override
  public String toString() {
    return getServer() + ":" + port;
  }

  @Override
  public boolean equals(Object obj) {
    if((obj == null) || (obj.getClass() != this.getClass())) return false; 
      
    ServerInfo otherObj = (ServerInfo)obj;
    if (otherObj.ip == null && ip == null || otherObj.ip.equals(ip)) {
      if (otherObj.name == null && name == null || otherObj.name.equals(name)) {
        if (otherObj.port == port && otherObj.udpPort == udpPort) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + (ip == null ? 0 : ip.hashCode());
    hash = hash * 29 + (name == null ? 0 : name.hashCode());
    hash = hash * 17 + port;
    hash = hash * 17 + udpPort;
    return hash;
  }
}
