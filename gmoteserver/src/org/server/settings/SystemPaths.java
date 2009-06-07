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

public enum SystemPaths {

  DEFAULT_SETTINGS("default_settings.txt"),
  SUPPORTED_FILE_TYPES("supported_filetypes.txt"),
  BASE_PATHS("base_paths.txt"),
  PASSWORD("data_settings.txt"),
  STARTUP_SETTINGS("startup_settings.txt"),
  GMOTE_LOG("gmote.log"),
  GMOTE_ERROR_LOG("gmote-error.log"),
  PREFERED_PORTS("prefered_ports.txt");

  String name;
  public static String ROOT_PATH = null;
  public String getName() {
    return name;
  }

  SystemPaths(String pathName) {
    name = pathName;
  }

  public static String getRootPath() {
    if (ROOT_PATH == null) {
      ROOT_PATH = System.getProperty("user.home");
      if (PlatformUtil.isMac()) {
        ROOT_PATH =  ROOT_PATH + "/Library/Gmote";
      } else if (PlatformUtil.isWindows()) {
        ROOT_PATH = ROOT_PATH + "/Application Data/Gmote";
      } else if (PlatformUtil.isLinux()){
        ROOT_PATH = ROOT_PATH + "/gmotedata/"; //TODO(mstogaitis): put linux files in the correct directory.
      } else {
        ROOT_PATH = ROOT_PATH + "/Application Data/Gmote";
      }
    }
    return ROOT_PATH;
  }

  public String getFullPath() {
    return getRootPath() + "/" + name;
  }
}
