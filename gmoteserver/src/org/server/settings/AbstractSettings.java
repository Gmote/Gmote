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
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractSettings<E extends Enum<E>,T> {
  final private static Logger LOGGER = Logger.getLogger(AbstractSettings.class.getName());
  
  private Map<Enum<E>, T> settings = new HashMap<Enum<E>, T>();
  
  /** The default value to return if there is no entry for a particular property **/
  private T defaultSetting;
  
  /** The file where the key-value pairs are stored **/
  private String fileName;
  
  protected AbstractSettings(String fileName, T defaultSetting) {
    this.fileName = fileName;
    this.defaultSetting = defaultSetting;
    loadSettings();
  }
  
  private void loadSettings() {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      settings.clear();
      
      String line;
      try {
        while ((line = reader.readLine()) != null) {
          String[] fields = line.split("=");
          Enum<E> key =  convertKey(fields[0]);
          settings.put(key, convertValue(fields[1]));
        }
      } finally {
        reader.close();
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE,e.getMessage(),e);
    }
  }
  
  

  private void writeSettings() {
    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
      
      for (Map.Entry<Enum<E>, T> entry : settings.entrySet()) {
        bw.write(entry.getKey() + "=" + entry.getValue());
        bw.newLine();
      }
      bw.close();
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE,e.getMessage(),e);
    }
  }
  
  public T getSetting(Enum<E> setting) {
    if (settings.containsKey(setting)) {
      return settings.get(setting);
    } else {
      return defaultSetting;
    }
  }
  
  public void setSetting(Enum<E> setting, T value) {
    settings.put(setting, value);
    writeSettings();
  }
  
  /**
   * Converts the 'value' field of a name value pair to an object. 
   * Should be implemented by a subclass
   * @param value
   */
  protected abstract T convertValue(String value);
  
  
  protected abstract Enum<E> convertKey(String key);
}

