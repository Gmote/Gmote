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

package org.gmote.client.android;

import org.gmote.common.ServerInfo;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GmoteClient extends Activity {
  private static final String DEBUG_TAG = "Gmote";
  static final String PREFS = "prefs";
  static final String KEY_SERVER = "server";
  static final String KEY_PORT = "port";
  static final String KEY_PASSWORD = "password";
  static final String KEY_UDP_PORT = "udpport";
  static final String KEY_IN_STREAM_MODE = "stream_mode";
  static final String KEY_IS_MANUAL_IP = "is_manual_ip";
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle icicle){
    super.onCreate(icicle);
    Log.d(DEBUG_TAG, "Client# onCreate");
    
    String appVersion = getVersionNumber();
    Log.w(DEBUG_TAG, "ManifestVersion: " + appVersion + " ClientVersion" + Remote.GMOTE_CLIENT_VERSION);
    if (!Remote.GMOTE_CLIENT_VERSION.equalsIgnoreCase(appVersion)) {
      Log.w(DEBUG_TAG, "Manifest version doesn't match APP_VERSION. These two version numbers should always be in sync. Please update the approprivate value.");
    }
    
    SharedPreferences prefs = getSharedPreferences(PREFS, MODE_WORLD_WRITEABLE);
    String server = prefs.getString(KEY_SERVER, null);
    
    if (server == null) {
      setContentView(R.layout.welcome);
      // "Email me the link" button
      TextView sendEmail = (TextView) findViewById(R.id.email_link);
      sendEmail.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          sendEmail();
        }
      });
      
      Button continueButton = (Button) findViewById(R.id.welcome_continue);
      
      continueButton.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
              listServers();
          }
      });
      
    } else {
      String serverIp = Remote.getInstance().getServerIp();
      // Only set the server if it's not already set (if we don't do this, it's
      // possible that the user will play a song on the phone, hit 'home' and
      // re-enter the app which would close the connection to the server.
      if (serverIp == null || serverIp.length() == 0) {
        setServerIpAndPassword(prefs, server);
      }
      startController();
    }
  }
  
  public static boolean isManualIp(SharedPreferences prefs) {
    return prefs.getBoolean(GmoteClient.KEY_IS_MANUAL_IP, false);
  }

  public static void setServerIpAndPassword(SharedPreferences prefs, String serverAddress) {

    int port = prefs.getInt(KEY_PORT, ServerInfo.DEFAULT_PORT);
    int udpPort = prefs.getInt(KEY_UDP_PORT, ServerInfo.DEFAULT_UDP_PORT);
    Remote.getInstance().setServer(new ServerInfo(serverAddress, port, udpPort));
    Remote.getInstance().setPassword(prefs.getString(KEY_PASSWORD, ""));

  }
    void startController() {
      Intent intent = new Intent();
      intent.setClass(GmoteClient.this, ButtonControl.class);
      startActivity(intent);
      finish();
    }
    
    void listServers() {
      Intent intent = new Intent();
      intent.setClass(GmoteClient.this, ListServers.class);
      startActivity(intent);
      finish();
    }
    
    private void sendEmail() {
      Intent emailIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"));
      // We call the gmail application directly since there is a bug in the normal mail application
      // that prevents it from interpreting the EXTRA_SUBJECT and EXTRA_TEXT properly.
      emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Gmote Server Link");
      emailIntent.putExtra(Intent.EXTRA_TEXT,
        "Welcome to Gmote!\n\nTo install the Gmote server, please click on the following link from the computer(s) you wish to control:\nhttp://www.gmote.org/server");
      try {
        startActivity(emailIntent);
      } catch (ActivityNotFoundException e) {
        // Try letting the user pick his own mail application. He will need to
        // copy the server url from the 'to' field
        emailIntent.setData(Uri.parse("mailto:http://www.gmote.org/server"));
        emailIntent.setComponent(null);
        try {
          startActivity(emailIntent);
        } catch (ActivityNotFoundException e2) {
          // Giving up.
          Toast.makeText(this, "Unable to launch mail application. " + e2.getMessage(), 5);
        }
      }
      
    }
    
    private String getVersionNumber() {
    String version = "0.0.0";
    try {
      PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
      version = pi.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(DEBUG_TAG, "Package name not found", e);
    }
    if (version.equals("0.0.0")) {
      Log.w(DEBUG_TAG, "Unable to find the app's version number: " + version);
    }
    return version; 
} 
  
}