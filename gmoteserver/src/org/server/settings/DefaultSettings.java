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



/**
 * Allows us to keep a key-value pair of 
 * @author Marc
 *
 */
public class DefaultSettings extends AbstractSettings<DefaultSettingsEnum, String> {
  private static DefaultSettings instance = null;

  private DefaultSettings(String fileName) {
    super(fileName, "");
  }

  public static synchronized DefaultSettings instance() {
    if (instance == null) {
      instance = new DefaultSettings(SystemPaths.DEFAULT_SETTINGS.getFullPath());
    }
    return instance;
  }

  @Override
  protected String convertValue(String value) {
    return value;
  }

  @Override
  protected Enum<DefaultSettingsEnum> convertKey(String key) {
    return DefaultSettingsEnum.valueOf(key);
  }
  
  public static void createDefaultFile() {
    instance().setSetting(DefaultSettingsEnum.VOLUME, "80");
    instance().setSetting(DefaultSettingsEnum.MONITOR_X, "1");
    instance().setSetting(DefaultSettingsEnum.MONITOR_Y, "1");
    instance().setSetting(DefaultSettingsEnum.SHOW_ALL_FILES, "false");
    instance().setSetting(DefaultSettingsEnum.UDP_PORT, "9901");
    instance().setSetting(DefaultSettingsEnum.SHUFFLE_SONGS, "false");
  }

}
