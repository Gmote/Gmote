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

public class PlatformUtil {
     
  public static boolean isWindows() {
    String osName = System.getProperty("os.name").toLowerCase();
    return osName.contains("windows");
  }
  
  public static boolean isMac() {
    String osName = System.getProperty("os.name").toLowerCase();
    return osName.contains("mac") || osName.contains("darwin");
  }

  public static boolean isLinux() {
    String osName = System.getProperty("os.name").toLowerCase();
    return osName.contains("linux");
  }
  
  public static String getOsName() {
    return System.getProperty("os.name");
  }
}
