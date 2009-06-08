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

package org.gmote.server.media.appledvd;

import org.gmote.common.media.MediaMetaInfo;
import org.gmote.server.media.MediaCommandHandler;
import org.gmote.server.media.PlayerUtil;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.foundation.NSAppleEventDescriptor;
import com.apple.cocoa.foundation.NSAppleScript;
import com.apple.cocoa.foundation.NSMutableDictionary;

@SuppressWarnings("deprecation")
public class DvdPlayerCommandHandler extends MediaCommandHandler {
  private static final int MAX_VOLUME = 255;
  private static final int MIN_VOLUME = 0;

  private static DvdPlayerCommandHandler instance = null;
  
  public static DvdPlayerCommandHandler instance() {
    if (instance  == null) {
      instance = new DvdPlayerCommandHandler();
    }
    return instance;
  }

  protected DvdPlayerCommandHandler() {
    NSApplication.sharedApplication();
  }

  /* Run commands with DVD Player */
  private NSAppleEventDescriptor tellDvdPlayerTo(String actions) {
    String script = "tell application \"DVD Player\"\n" + actions + "\n end tell\n";
    return executeForResult(script, 2);
  }

  private NSAppleEventDescriptor executeForResult(String script, int timeout) {
    script = "with timeout " + timeout + " seconds\n" + script + "\nend timeout\n";
    // System.out.println("\nExecuting script for result: " + script);

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
      // System.out.println("Results: " + results.toString());
    }
    return results;
  }
  
  protected boolean running() {
    try {
      String script = "tell application \"System Events\"\n"
          + "set isRunning to ((application processes whose (name is equal to \"DVD Player\")) count)\n"
          + "end tell\n" + "if isRunning is greater than 0 then\n"
          + "return true\n" + "else\n" + "return false\n" + "end if";
      NSAppleEventDescriptor result = executeForResult(script, 3);
      return result.booleanValue();
    } catch (Exception e) {
    }
    return false;
  }
  
  protected void closeMedia() {
    tellDvdPlayerTo("quit");
  }

  protected void stopMedia() {
    tellDvdPlayerTo("set elapsed time to 0");
    tellDvdPlayerTo("stop dvd");
  }

  protected void rewind() {
    tellDvdPlayerTo("set elapsed time to (elapsed time - 12)");
  }

  protected void fastForward() {
    tellDvdPlayerTo("set elapsed time to (elapsed time + 12)");
  }

  protected void pauseMedia() {
    tellDvdPlayerTo("pause dvd");
  }

  protected void playMedia() {
    tellDvdPlayerTo("play dvd");
  }

  protected void rewindLong() {
    tellDvdPlayerTo("play previous chapter");
  }

  protected void fastForwardLong() {
    tellDvdPlayerTo("play next chapter");
  }

  protected void setVolume(int volume) {
    tellDvdPlayerTo("set audio volume to " + PlayerUtil.denormalizeVolume(volume, MIN_VOLUME, MAX_VOLUME));
  }

  protected int getVolume() {
    NSAppleEventDescriptor result = tellDvdPlayerTo("get audio volume");
    return PlayerUtil.normalizeVolume(result.int32Value(), MIN_VOLUME, MAX_VOLUME);
  }

  protected void toggleMute() {
    tellDvdPlayerTo("set audio muted to (not audio muted)");
  }

  protected void fullScreen() {
    String script = "tell application \"System Events\"\n"
        + "keystroke \"f\" using command down\n" + "end tell\n";
    executeForResult(script, 1);
  }
  public MediaMetaInfo getNewMediaInfo() {
    return null;
  }
  
  protected void launchDvd() {
    String actions = "set interaction override to false\nplay dvd\ngo return to dvd\nset viewer visibility to true\n";
    tellDvdPlayerTo(actions);
    tellDvdPlayerTo("activate");
    tellDvdPlayerTo("set viewer full screen to true");
  }
  
  public boolean isMediaOpen() {
    return false;
  }
}
