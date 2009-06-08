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

import org.gmote.common.Protocol.Command;
import org.gmote.common.Protocol.ServerErrorType;
import org.gmote.common.packet.AbstractPacket;
import org.gmote.common.packet.ServerErrorPacket;
import org.gmote.common.packet.SimplePacket;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Base class that provides the common functionalities of all activities in the
 * Gmote client
 * 
 * @author Mimi
 * 
 */
public class ActivityUtil {
  static final String DEBUG_TAG = "Gmote";
  static final int DIALOG_ENTER_PASSWORD = 1;
  static final int VIEW_VISIBLE = 0;
  static final int VIEW_GONE = 2;
  static final int DIALOG_UPDATE_CLIENT = 3;
  static final int DIALOG_BROWSE_VIEW_SETTINGS = 4;
  static final int DIALOG_HELP = 5;
  static final int DIALOG_SERVER_OUT_OF_DATE = 6;
  static final int DIALOG_SERVER_OUT_OF_DATE_NO_UPDATER = 7;
  
  Activity mActivity = null;
  View mPasswordEntryView = null;
  ProgressDialog mDialog = null;
  Menu mMenu = null;
  Remote mRemote;
  AbstractPacket mPacket = null;
  static WifiLock wifiLock = null;
  static WakeLock wakeLock = null;
  
  /** Called when the activity is first created. */
  public void onCreate(Bundle icicle, Activity activity) {
    mActivity = activity;
    mRemote = Remote.getInstance(mHandler);
    if (mRemote.getServer() == null) {
      // This can happen if there is an uncaught exception in the program. Our
      // variables will get reset, but the GmoteClient activity won't get
      // launched (it only goes back to the previous activity that is on the
      // stack).
      SharedPreferences prefs = mActivity.getSharedPreferences(GmoteClient.PREFS, Context.MODE_WORLD_READABLE);
      String serverAddress = prefs.getString(GmoteClient.KEY_SERVER, null);
      if (serverAddress != null && serverAddress.length() != 0) {
        GmoteClient.setServerIpAndPassword(prefs, serverAddress);
      }      
    }
  }

  
  public void onStart(Activity activity) {
    mActivity = activity;
    if (wifiLock == null) {
      PowerManager powerManager = (PowerManager) mActivity.getSystemService(Context.POWER_SERVICE);
      wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, DEBUG_TAG);
      wakeLock.setReferenceCounted(true);

      WifiManager wifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
      wifiLock = wifiManager.createWifiLock(DEBUG_TAG);
      wifiLock.setReferenceCounted(true);
    }
    Log.e(mActivity.getClass().getName(), "ACQUIRE");

    wifiLock.acquire();
    wakeLock.acquire();
  }
  
  public void onStop() {
    if (wifiLock != null) {
      Log.e(mActivity.getClass().getName(), "RELEASE");
      wifiLock.release();
      wakeLock.release();
    }
  }

  public void onResume() {
    mRemote = Remote.getInstance(mHandler);
  }

  public void onPause() {
    cancelDialog();
  }
  
  public boolean onCreateOptionsMenu(Menu menu) {
    // Hold on to this
    mMenu = menu;
    MenuInflater inflater = mActivity.getMenuInflater();
    inflater.inflate(R.menu.control, menu);
    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.menui_browse:
      startActivityByClass(Browse.class);
      break;
    case R.id.menui_remote_control:
      startActivityByClass(ButtonControl.class);
      break;
    case R.id.menui_settings:
      startActivityByClass(ListServers.class);
      break;
    case R.id.menui_touchpad:
      startActivityByClass(Touchpad.class);
      break;
    case R.id.menui_help:
      mActivity.showDialog(DIALOG_HELP);
      break;
    case R.id.menui_web_browser:
      startActivityByClass(WebBrowser.class);
      break;
    }
  
    return true;
  }

  @SuppressWarnings("unchecked")
  void startActivityByClass(Class c) {
    Intent intent = new Intent();
    intent.setClass(mActivity, c);
    mActivity.startActivity(intent);
  }
  
  void startActivityByClassName(String name) {
    Intent intent = new Intent();
    try {
      intent.setClass(mActivity, Class.forName(name));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    mActivity.startActivity(intent);
  }

  /**
   * Extracts packet from a message and handles messages common to most
   * activities.
   * 
   * @param msg
   * @return AbstractPacket or null
   */
  Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      if (msg.what == Remote.CONNECTING) {
        if (mDialog == null) {
          mDialog = ProgressDialog.show(mActivity, null,
              "Connecting to the server");
        }
        return;
      }

      if (msg.what == Remote.AUTHENTICATION_FAILURE) {
        cancelDialog();
        mActivity.showDialog(DIALOG_ENTER_PASSWORD);
      } else if (msg.what == Remote.CONNECTED) {
        cancelDialog();
      } else if (msg.what == Remote.CONNECTION_FAILURE || msg.obj == null) {
        cancelDialog();

        Toast.makeText(mActivity, "Connection problem", Toast.LENGTH_SHORT)
            .show();
        
        Intent intent = new Intent();
        intent.setClass(mActivity, ListServers.class);
        
        boolean skipFindServers = GmoteClient.isManualIp(mActivity.getSharedPreferences(GmoteClient.PREFS, Context.MODE_WORLD_READABLE));
        intent.putExtra(ListServers.SKIP_FIND_SERVERS, skipFindServers);
        mActivity.startActivity(intent);
        mActivity.finish();
      } else if (msg.what == Remote.SERVER_OUT_OF_DATE) {
        cancelDialog();
        String serverVersion = (String)msg.obj;
        if (serverVersion.equalsIgnoreCase("1.2")) {
          // Version 1.2 of the server did not have an updater.
          mActivity.showDialog(DIALOG_SERVER_OUT_OF_DATE_NO_UPDATER);
        } else {
          mActivity.showDialog(DIALOG_SERVER_OUT_OF_DATE);
        }
        
        
      } else {

        AbstractPacket reply = (AbstractPacket) msg.obj;
        if (reply.getCommand() != Command.MEDIA_INFO) {
          cancelDialog();
        }
       
        if (reply.getCommand() == Command.SERVER_ERROR) {
          ServerErrorPacket errorPacket = (ServerErrorPacket) reply;
          int errorTypeOrdinal = errorPacket.getErrorTypeOrdinal();
          ServerErrorType errorType;
          // Determine which kind of error this is, making sure to handle new types of errors properly.
          if (errorTypeOrdinal < ServerErrorType.values().length) {
            errorType = ServerErrorType.values()[errorTypeOrdinal];
          } else {
            errorType = ServerErrorType.UNSPECIFIED_ERROR;
          }
          
          if (errorType == ServerErrorType.INCOMPATIBLE_CLIENT) {
            mRemote.detach();
            mActivity.showDialog(DIALOG_UPDATE_CLIENT);
          } else {
            Toast.makeText(mActivity,
                errorPacket.getErrorDescription(),
                Toast.LENGTH_LONG).show();
          }
            
        } else {
          ((BaseActivity) mActivity).handleReceivedPacket(reply);
        }
      }
    }
  };

  void send(AbstractPacket packet) {
    mPacket = packet;
    mRemote.queuePacket(packet);
  }

  protected Dialog onCreateDialog(int id) {
    if (id == DIALOG_ENTER_PASSWORD) {
    
      LayoutInflater factory = LayoutInflater.from(mActivity);
      mPasswordEntryView = factory.inflate(R.layout.password_form, null);
      return new AlertDialog.Builder(mActivity).setTitle(
          "Please enter your password").setView(mPasswordEntryView)
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              EditText passwordEdit = (EditText) mPasswordEntryView
                  .findViewById(R.id.password_edit);
              String password = passwordEdit.getText().toString();
              Remote.getInstance().setPassword(password);

              if (mPacket != null) {
                mRemote.queuePacket(mPacket);
              }

              // save password
              SharedPreferences.Editor editor = mActivity.getSharedPreferences(
                  GmoteClient.PREFS, Activity.MODE_WORLD_WRITEABLE).edit();
              editor.putString(GmoteClient.KEY_PASSWORD, password);
              editor.commit();
            }
          }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
          }).create();
    } else if (id == DIALOG_UPDATE_CLIENT) {
      
      LayoutInflater factory = LayoutInflater.from(mActivity);
      final View updateClientView = factory.inflate(R.layout.update_client, null);
      return new AlertDialog.Builder(mActivity).setTitle(
          "Client Update Required").setView(updateClientView)
          .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:org.gmote.client.android"));
              mActivity.startActivity(intent);
              mActivity.finish();
            }
          }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              mActivity.finish();
            }
          }).create();
    } else if (id == DIALOG_HELP) {
      return createHelpDialog();
    } else if (id == DIALOG_SERVER_OUT_OF_DATE) {
      return createServerOutOfDateDialog();
    } else if (id == DIALOG_SERVER_OUT_OF_DATE_NO_UPDATER) {
      return createServerOutOfDateNoUpdaterDialog();
    }
    
    return null;
  }

  private Dialog createServerOutOfDateNoUpdaterDialog() {
    String dialogMessage = "The Gmote server is out of date and is incompatible with the current version of Gmote that is on your phone.\n\nPlease exit the Gmote Server and install the latest server software from http://www.gmote.org/server on your computer.\n";
    final View alertDialogView = createLinkifiedAlertView(dialogMessage);
    
    return new AlertDialog.Builder(mActivity)
    .setView(alertDialogView)
    .setTitle("Update")
    .setPositiveButton("Ok",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            Remote.getInstance().disconnect();
            dialog.dismiss();
          }
        })
    .create();
  }

  private Dialog createServerOutOfDateDialog() {
    String dialogMessage = "The Gmote server is out of date and is incompatible with the current version of Gmote that is on your phone.\n\nClick Ok to update your server. Clicking cancel will terminate your connection to the server.\n";
    final View alertDialogView = createLinkifiedAlertView(dialogMessage);
    
    return new AlertDialog.Builder(mActivity)
    .setView(alertDialogView)
    .setTitle("Update")
    .setPositiveButton("Update",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            send(new SimplePacket(Command.UPDATE_SERVER_REQUEST));
            try {
              Thread.sleep(300);
            } catch (InterruptedException e) {
              Log.e(DEBUG_TAG, e.getMessage(), e);
            }
            Remote.getInstance().disconnect();
            dialog.dismiss();
            mActivity.finish();
          }
        })
    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
            Remote.getInstance().disconnect();
            dialog.dismiss();
            mActivity.finish();
          }
        }).create();
  }

  private Dialog createHelpDialog() {
    
    String dialogMessage = "If you are unable to connect to the Gmote server or are experiencing other technical issues, please visit:\nhttp://www.gmote.org/faq\n\nTo lean how to use Gmote, please visit:\nhttp://www.gmote.org/howto\n\nTo learn about GmoteTouch, please visit:\nhttp://www.gmote.org/gmotetouch\n\nTip: To skip to the next song in a playlist, hold down the right arrow button for 2 seconds.\n\nTip: Try using android's 'back' arrow key to return to your media list instead of browsing to it again.\n\nTip: To right click in GmoteTouch mode, press down on the screen for 2 seconds.\n\nTip: Press the 'play' button while in PowerPoint, PDF or ImageViewer mode to launch a slide show. Press the close button (top right circle button) to close the slideshow.\n\n";
    final View alertDialogView = createLinkifiedAlertView(dialogMessage);
    
    return new AlertDialog.Builder(mActivity)
    .setView(alertDialogView)
    .setTitle("Help")
    .setPositiveButton("Ok",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            dialog.dismiss();
          }
        }).create();
  }

  private View createLinkifiedAlertView(String dialogMessage) {
    LayoutInflater factory = LayoutInflater.from(mActivity);
    final View alertDialogView = factory.inflate(R.layout.alert_dialog_text_view, null);
    TextView tv = (TextView)alertDialogView.findViewById(R.id.alert_text_view);
    tv.setText(dialogMessage);
    return alertDialogView;
  }
  
  public static AlertDialog showMessageBox(Context context, String title, String message) {
    return new AlertDialog.Builder(context)
    .setTitle(title)
    .setMessage(message)
    .setPositiveButton("Ok",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            dialog.dismiss();
          }
        }).show();
  }

  void showProgressDialog(String text) {
    mDialog = ProgressDialog.show(mActivity, null, text);
  }

  void cancelDialog() {
    if (mDialog != null) {
      mDialog.dismiss();
      mDialog = null;
    }
  }
}
