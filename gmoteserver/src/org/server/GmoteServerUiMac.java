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

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import org.gmote.server.settings.StartupSettings;
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import com.apple.eawt.ApplicationEvent;

/**
 * @author Mimi
 *
 */
public class GmoteServerUiMac extends GmoteServerUi {
  SystemTray tray = SystemTray.getDefaultSystemTray();
  TrayIcon trayIcon;
  JPopupMenu popupMenu;

  public GmoteServerUiMac(GmoteServer server) {
    super(server);
    this.server = server;
  }

  void initializeUi() {   
    JMenuItem item;
    
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
        e.printStackTrace();
    }
    if( Integer.parseInt(System.getProperty("java.version").substring(2,3)) >=5 )
        System.setProperty("javax.swing.adjustPopupLocationToFit", "false");
    
    popupMenu = new JPopupMenu("Gmote Menu");

    addMediaPlayerControls();

    JMenu settingsMenu = new JMenu("Settings");
    popupMenu.add(settingsMenu);
    
    item = new JMenuItem("Change password");
    item.addActionListener(settingsListener);
    settingsMenu.add(item);

    item = new JMenuItem("Change media paths");
    item.addActionListener(mediaPathListener);
    settingsMenu.add(item);
    
    JMenu helpMenu = new JMenu("Help");
    popupMenu.add(helpMenu);
    
    item = new JMenuItem("Show local ip address");
    item.addActionListener(ipAddressListener);
    helpMenu.add(item);
    
    item = new JMenuItem("Show settings and logs folder");
    item.addActionListener(logFolderListener);
    helpMenu.add(item);      
    
    item = new JMenuItem("Connection Help");
    item.addActionListener(helpListener);
    helpMenu.add(item);
    
    
    popupMenu.addSeparator();
    item = new JMenuItem("Quit");
    item.addActionListener(exitListener);
    popupMenu.add(item);

    ImageIcon i = new ImageIcon(GmoteServerUiMac.class.getResource("/res/gmote_icon_s.png"));
    trayIcon = new TrayIcon(i, "Gmote", popupMenu);
    trayIcon.setIconAutoSize(true);
    tray.addTrayIcon(trayIcon);
    
  }

  void handleExtraSettings(StartupSettings settings) {
    
  }
  
  public static void main(String[] args) {
    GmoteServer server = new GmoteServer();
    GmoteServerUi ui = new GmoteServerUiMac(server);
    ui.sharedMain(args);
  }

  public void addMediaPlayerControls() {
    if (!mediaPlayerControlsVisible) {
      JMenuItem item;
      item = new JMenuItem("Pause");
      item.addActionListener(pauseListener);
      popupMenu.insert(item, 0);

      item = new JMenuItem("Play");
      item.addActionListener(playListener);
      popupMenu.insert(item, 0);
      
      item = new JMenuItem("Previous");
      item.addActionListener(previousListener);
      popupMenu.insert(item, 0);
      
      item = new JMenuItem("Next");
      item.addActionListener(nextListener);
      popupMenu.insert(item, 0);
      
      popupMenu.addSeparator();
      mediaPlayerControlsVisible = true;
    }
  }
  
  public void removeMediaPlayerControls() {
  }
  
  public void quit(ApplicationEvent e) {  
    System.exit(0);
  }
}
