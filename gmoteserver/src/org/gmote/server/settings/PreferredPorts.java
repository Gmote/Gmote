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

package org.gmote.server.settings;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gmote.server.ServerUtil;

/**
 * Keeps a map of ip addresses and their preferred ports.
 * 
 * @author Marc Stogaitis
 */
public class PreferredPorts {
  final private static Logger LOGGER = Logger.getLogger(PreferredPorts.class.getName());
  private static PreferredPorts instance = null;
  
  private Map<String, Integer> preferredPorts = new HashMap<String, Integer>();
  
  public PreferredPorts() {
    loadData();
  }
  
  public synchronized static PreferredPorts instance() {
    if (instance == null) {
      instance = new PreferredPorts();
    }
    return instance;
  }
  
  public void addPort(String ip, int port) {
    if (preferredPorts.containsKey(ip)) {
      LOGGER.warning("Overwriting port setting for ip: " + ip + " from " + preferredPorts.get(ip) + " to " + port);
    }
    preferredPorts.put(ip, port);
    saveData();
  }

  public boolean isPortAssigned(int port) {
    for (int value : preferredPorts.values()) {
      if (value == port) {
        return true;
      }
    }
    return false;
  }
  
  public Integer getPreferredPort(String ip) {
    return preferredPorts.get(ip);
  }
  
  private void saveData() {
    ServerUtil.createIfNotExists(SystemPaths.getRootPath());
    try {
      PrintWriter writer = new PrintWriter(new FileWriter(SystemPaths.PREFERED_PORTS.getFullPath()));
      for (Map.Entry<String, Integer> entry : preferredPorts.entrySet()) {
        writer.println(entry.getKey() + "=" + entry.getValue());
      }
      writer.close();
      
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      
    }
  }
  
  private void loadData() {
    preferredPorts = new HashMap<String, Integer>();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(SystemPaths.PREFERED_PORTS.getFullPath()));
      String line = null;
      while ((line = reader.readLine()) != null) {
        String[] lineSplit = line.split("=");
        preferredPorts.put(lineSplit[0], Integer.parseInt(lineSplit[1]));
      }
    } catch (FileNotFoundException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public Map<String, Integer> getPreferedPorts() {
    return preferredPorts;
  }
}
