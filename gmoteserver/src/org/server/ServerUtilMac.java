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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.foundation.NSAppleEventDescriptor;
import com.apple.cocoa.foundation.NSAppleScript;
import com.apple.cocoa.foundation.NSMutableDictionary;

public class ServerUtilMac extends ServerUtil{
  public ServerUtilMac() {}
  public void initialize() {
  }

  @Override
  public void startFileInDefaultApplication(String fileName) {
      String script =  "do shell script \"open '"+ fileName +"'\"";
      
      try {
        executeForResult(script, 10);
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }
  }
  
  @Override
  public void startFileInSeparateprocess(String command) {
    executeShellScript(command);
  }

  @Override
  public String findInstallDirectory() {
    File curdir = new File("../../");
    String path = "";
    try {
      path = curdir.getCanonicalPath();
      LOGGER.log(Level.WARNING, "Current directory: " + path);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }

    return path;
  }
  
  public static boolean executeShellScript(String command) {
    String script =  "do shell script \"" + command + "\"";
    
    try {
      executeForResult(script, 10);
      return true;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
    return false;
  }
  
  public static NSAppleEventDescriptor executeForResult(String script, int timeout) {
    NSApplication.sharedApplication();

    script = "with timeout " + timeout + " seconds\n" + script + "\nend timeout\n";
    LOGGER.info("\nExecuting script for result: " + script);

    // This creates a new NSAppleScript object
    // to execute the script
    NSAppleScript myScript = new NSAppleScript(script);

    // This dictionary holds any errors
    // that are encountered during script execution
    NSMutableDictionary errors = new NSMutableDictionary();

    // Execute the script!
    NSAppleEventDescriptor results = myScript.execute(errors);

    // Print out results
    if (results != null) {
      LOGGER.info("Results: " + results.toString());
    }
    return results;
  }
  @Override
  public String getUpdateUrl() {
    return "http://www.gmote.org/download/latest_version_mac.txt";
  }
}
