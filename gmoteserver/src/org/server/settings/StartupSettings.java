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

import org.gmote.server.PlatformUtil;


/**
 * Allows us to keep a key-value pair of 
 * @author Marc
 *
 */
public class StartupSettings extends AbstractSettings<StartupSettingsEnum, Boolean> {
  private static StartupSettings instance = null;

  private StartupSettings(String fileName) {
    super(fileName, false);
  }

  public static synchronized StartupSettings instance() {
    if (instance == null) {
      instance = new StartupSettings(SystemPaths.STARTUP_SETTINGS.getFullPath());
    }
    return instance;
  }

  @Override
  protected Boolean convertValue(String value) {
    return Boolean.valueOf(value);
  }

  @Override
  protected Enum<StartupSettingsEnum> convertKey(String key) {
    return StartupSettingsEnum.valueOf(key);
  }

  public static void createDefaultFile() {
    if (PlatformUtil.isWindows()) {
      // We only know how to handle dvd drive identification on windows for now.
      instance().setSetting(StartupSettingsEnum.ADDED_DVD_DRIVES, false);
    } else {
      instance().setSetting(StartupSettingsEnum.ADDED_DVD_DRIVES, true);
    }
    instance().setSetting(StartupSettingsEnum.ADDED_TO_STARTUP, true);
    instance().setSetting(StartupSettingsEnum.PASSWORD_SHOWN, false);
    instance().setSetting(StartupSettingsEnum.PATH_SHOWN, false);
    instance().setSetting(StartupSettingsEnum.POPUP_SHOWN, false);
    
    
   
    
    
  }
}
