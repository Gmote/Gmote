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

public class ServerUtilWindows extends ServerUtil {
  @Override
  public void startFileInDefaultApplication(String fileName) {
      String[] commands = { "cmd", "/c", "start", "\"Runner\"", "/WAIT", "/MAX", fileName };
      try {
        // Run the file
        Runtime.getRuntime().exec(commands);
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }
  }

  @Override
  public void startFileInSeparateprocess(String command) {
    LOGGER.info(command);
      try {
        // Run the file
        String fullCommand =  "cmd /c start " +  command;
        Runtime.getRuntime().exec(fullCommand);
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }
  }

  @Override
  public String getUpdateUrl() {
    return "http://www.gmote.org/download/latest_version_windows.txt";
  }
}
