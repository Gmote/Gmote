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

/////////////////////////////////////////////////////////
//Bare Bones Browser Launch                          //
//Version 1.5 (December 10, 2005)                    //
//By Dem Pilafian                                    //
//Supports: Mac OS X, GNU/Linux, Unix, Windows XP    //
//Example Usage:                                     //
// String url = "http://www.centerkey.com/";       //
// BareBonesBrowserLaunch.openURL(url);            //
//Public Domain Software -- Free to Use as You Like  //
/////////////////////////////////////////////////////////

import java.lang.reflect.Method;

import javax.swing.JOptionPane;

public class BrowserLauncherUtil {
  
 private static final String errMsg = "Error attempting to launch web browser";

 @SuppressWarnings("unchecked")
public static void openURL(String url) {
    String osName = System.getProperty("os.name");
    try {
      if (osName.startsWith("Mac OS")) {
        Class fileMgr = Class.forName("com.apple.eio.FileManager");
        Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
        openURL.invoke(null, new Object[] { url });
      } else if (osName.startsWith("Windows"))
        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
      else { // assume Unix or Linux
        String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
        String browser = null;
        for (int count = 0; count < browsers.length && browser == null; count++)
          if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] }).waitFor() == 0)
            browser = browsers[count];
        if (browser == null)
          throw new Exception("Could not find web browser");
        else
          Runtime.getRuntime().exec(new String[] { browser, url });
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage());
    }
  }
}
