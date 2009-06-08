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

import java.util.ArrayList;
import java.util.List;

import org.gmote.common.ServerInfo;
import org.gmote.common.packet.ListReplyPacket;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

/**
 * The ListServers activity. Finds available servers using a multicast call and
 * lists the results.
 * 
 * @author Mimi
 * 
 */
public class ListServers extends ListActivity {
  private static final String DEBUG_TAG = "Gmote";
  private static final int DIALOG_SERVER_NOT_FOUND = 1;
  private static final int DIALOG_ENTER_IP = 2;
  private static final int DIALOG_NO_WIFI = 3;
  private static final int DIALOG_NONE = -1;
  protected static final String SKIP_FIND_SERVERS = "skip_find_servers";

  int currentDialog = DIALOG_NONE;
  ListReplyPacket reply;
  private Remote mRemote;
  String mPath = null;
  List<ServerInfo> servers = new ArrayList<ServerInfo>();
  ArrayAdapter<ServerInfo> arrayAdapter;
  ProgressDialog mDialog = null;
  View mManualEntryView;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(DEBUG_TAG, "ListServers onCreate");
    
    arrayAdapter = new ArrayAdapter<ServerInfo>(ListServers.this,
        android.R.layout.simple_list_item_1, servers);
    setListAdapter(arrayAdapter);
    getListView().setTextFilterEnabled(true);
  }

  @Override
  public void onStart() {
    super.onStart();
    mRemote = Remote.getInstance(mHandler);
    Intent intent = getIntent();
    boolean skipFindServers = intent.getBooleanExtra(SKIP_FIND_SERVERS, false);
    if (!skipFindServers) {
      listServers();
    } else {
      showDialog(DIALOG_SERVER_NOT_FOUND);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (mDialog != null)
      mDialog.dismiss();
    mRemote.detach();
  }
  
  @Override
  public void onResume() {
    super.onResume();
    mRemote = Remote.getInstance(mHandler);
  }

  /*
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    
    if (currentDialog != DIALOG_NONE) {
      dismissDialog(currentDialog);
      showDialog(currentDialog);
    }
  }
  */
  
  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    ServerInfo server = arrayAdapter.getItem(position);

    if (server.getServer() == null) {
      showDialog(DIALOG_SERVER_NOT_FOUND);
    } else {
      saveServerPrefererences(server, false);
      Remote.getInstance().setServer(server);
      startController();
    }
  }
  
  private void saveServerPrefererences(ServerInfo server, boolean isManualIp) {
    SharedPreferences.Editor editor = getSharedPreferences(GmoteClient.PREFS,
        MODE_WORLD_WRITEABLE).edit();
    editor.putString(GmoteClient.KEY_SERVER, server.getIp());
    editor.putInt(GmoteClient.KEY_PORT, server.getPort());
    editor.putInt(GmoteClient.KEY_UDP_PORT, server.getUdpPort());
    editor.putBoolean(GmoteClient.KEY_IS_MANUAL_IP, isManualIp);
    editor.commit();
  }
  
  Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      if (mDialog != null) {
        mDialog.dismiss();
        mDialog = null;
      }
      
      if (msg.what == Remote.SERVER_LIST_ADD_SERVER) {
        ServerInfo server = (ServerInfo) msg.obj;
        if (!servers.contains(server)) {
          arrayAdapter.add(server);
          
        }
        
      } else if (msg.what == Remote.SERVER_LIST_DONE) {
        
        if (arrayAdapter.isEmpty()) {
          showDialog(DIALOG_SERVER_NOT_FOUND);
        } 
        
        arrayAdapter.add(new ServerInfo(null) {
          public String toString() {
            return "I don't see my server or I want to enter my ip manually";
          }
        }); 
      }
    }
  };

  void listServers() {
    WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
    if (wifiManager.isWifiEnabled()) {
      fetchServerList();
    } else {
      showDialog(DIALOG_NO_WIFI);
    }
  }
  
  void fetchServerList() {
    arrayAdapter.clear();
    mDialog = ProgressDialog.show(ListServers.this, null,
        "Searching for servers. This may take a few seconds.");
    mRemote.getServerList(mHandler);
  }

  void startController() {
    Intent intent = new Intent();
    intent.setClass(ListServers.this, ButtonControl.class);
    startActivity(intent);
    finish();
  }

  @Override
  protected Dialog onCreateDialog(int id) { // TODO(mimi): string constants
    currentDialog = id;
    switch (id) {
    case DIALOG_NO_WIFI:
      System.out.println("create no_wifi dialog");
      return new AlertDialog.Builder(ListServers.this)
      .setTitle("Alert")
      .setMessage(
          "Please enable Wifi on your phone, and connect it to the network that your server is on.")
      .setPositiveButton("Ok",
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              currentDialog = DIALOG_NONE;
              startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
            }
          }).setNegativeButton("Enter IP manually",
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              currentDialog = DIALOG_NONE;
              showDialog(DIALOG_ENTER_IP);
            }
          }).create();
    case DIALOG_SERVER_NOT_FOUND:
      
      return new AlertDialog.Builder(ListServers.this)
          .setTitle("Setup")
          .setMessage(
              "Make sure that your gmote server is installed, running, and connected to the same wireless network as this phone. Also, please verify your firewall settings.\n\nYou can find more help at http://www.gmote.org/faq\n")
          .setPositiveButton("Find server",
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                  currentDialog = DIALOG_NONE;
                  listServers();
                }
              }).setNegativeButton("Enter IP manually",
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                  currentDialog = DIALOG_NONE;
                  showDialog(DIALOG_ENTER_IP);
                }
              }).create();
    case DIALOG_ENTER_IP:
      LayoutInflater factory = LayoutInflater.from(this);
      mManualEntryView = factory.inflate(R.layout.server_form, null);
      ScrollView scrollView = new ScrollView(this);
      scrollView.addView(mManualEntryView);
      EditText serverEdit = (EditText) mManualEntryView
      .findViewById(R.id.server_edit);
      serverEdit.setText(mRemote.getServerIp());
      AlertDialog serverDialog = new AlertDialog.Builder(ListServers.this)
          .setView(scrollView).setTitle("Enter Ip Manually")
          .setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              currentDialog = DIALOG_NONE;
              EditText serverEdit = (EditText) mManualEntryView
                  .findViewById(R.id.server_edit);
              EditText portEdit = (EditText) mManualEntryView
                  .findViewById(R.id.port_edit);
              EditText udpPortEdit = (EditText) mManualEntryView
              .findViewById(R.id.udp_port_edit);
              String name = serverEdit.getText().toString();
              int port;
              try {
                port = Integer.parseInt(portEdit.getText().toString());
              } catch (Exception e) {
                port = ServerInfo.DEFAULT_PORT;
              }
              int udpPort;
              try {
                udpPort = Integer.parseInt(udpPortEdit.getText().toString());
              } catch (Exception e) {
                udpPort = ServerInfo.DEFAULT_UDP_PORT;
              }
              

              ServerInfo server = new ServerInfo(name, port, udpPort);
              saveServerPrefererences(server, true);
              Remote.getInstance().setServer(server);
              Toast.makeText(ListServers.this, "Set server to " + server,
                  Toast.LENGTH_LONG).show();
              startController();
            }
          }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              currentDialog = DIALOG_NONE;
            }
          }).create();
      return serverDialog;
    }
    return null;
  }

}
