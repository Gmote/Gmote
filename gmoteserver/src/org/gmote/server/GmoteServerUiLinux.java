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
import java.awt.AWTException;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.util.logging.Level;

import org.gmote.server.settings.StartupSettings;
import org.gmote.server.settings.StartupSettingsEnum;

/**
 * @author Mimi
 *
 */
public class GmoteServerUiLinux extends GmoteServerUi{
  private TrayIcon trayIcon;
  private PopupMenu popupMenu;
  private boolean mediaPlayerControlsVisible = false;

  public GmoteServerUiLinux(GmoteServer server) {
    super(server);
  }
  
  public static void main(String[] args) {
    GmoteServer server = new GmoteServer();
    GmoteServerUi ui = new GmoteServerUiWindows(server);
    ui.sharedMain(args);
  }
  
  public void initializeUi() {
    showTrayIcon();
  }
  public void handleExtraSettings(StartupSettings settings) {
    
    if (!settings.getSetting(StartupSettingsEnum.POPUP_SHOWN)) {
     trayIcon.displayMessage("Welcome To Gmote!",
          "Right click on the Gmote icon any time to see a list of options", MessageType.INFO);
     settings.setSetting(StartupSettingsEnum.POPUP_SHOWN, true);
    }
  }
 
  public void showTrayIcon() {
    if (SystemTray.isSupported()) {

      SystemTray tray = SystemTray.getSystemTray();
      Image image = Toolkit.getDefaultToolkit().getImage(
          this.getClass().getResource("/res/gmote_icon_s.png"));

      popupMenu = new PopupMenu();
      MenuItem item;

      Menu settingsMenu = new Menu("Settings");
      popupMenu.add(settingsMenu);
      
      item = new MenuItem("Change password");
      item.addActionListener(settingsListener);
      settingsMenu.add(item);

      item = new MenuItem("Change media paths");
      item.addActionListener(mediaPathListener);
      settingsMenu.add(item);
      
      Menu helpMenu = new Menu("Help");
      popupMenu.add(helpMenu);
      
      item = new MenuItem("Show local ip address");
      item.addActionListener(ipAddressListener);
      helpMenu.add(item);
      
      item = new MenuItem("Show settings and logs folder");
      item.addActionListener(logFolderListener);
      helpMenu.add(item);      
      
      item = new MenuItem("Connection Help");
      item.addActionListener(helpListener);
      helpMenu.add(item);
      
      popupMenu.addSeparator();
      item = new MenuItem("Exit");
      item.addActionListener(exitListener);
      popupMenu.add(item);

      trayIcon = new TrayIcon(image, "Gmote Server", popupMenu);
      trayIcon.setImageAutoSize(true);

      try {
        tray.add(trayIcon);
      } catch (AWTException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }

    } else {
      LOGGER.warning("Tray icon is not supported");
    }
  }

  public void addMediaPlayerControls() {
    if (!mediaPlayerControlsVisible) {
      popupMenu.insertSeparator(0);
      
      MenuItem item;
      item = new MenuItem("Pause");
      item.addActionListener(pauseListener);
      popupMenu.insert(item, 0);

      item = new MenuItem("Play");
      item.addActionListener(playListener);
      popupMenu.insert(item, 0);
      
      item = new MenuItem("Previous");
      item.addActionListener(previousListener);
      popupMenu.insert(item, 0);
      
      item = new MenuItem("Next");
      item.addActionListener(nextListener);
      popupMenu.insert(item, 0);
      
      mediaPlayerControlsVisible = true;
    }
  }
  
  public void removeMediaPlayerControls(){
    popupMenu.remove(0);
    popupMenu.remove(0);
    popupMenu.remove(0);
    mediaPlayerControlsVisible = false;
  }


}
