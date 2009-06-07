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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JOptionPane;

import org.gmote.common.Protocol.Command;
import org.gmote.server.media.UnsupportedCommandException;
import org.gmote.server.settings.BaseMediaPaths;
import org.gmote.server.settings.DefaultSettings;
import org.gmote.server.settings.DefaultSettingsEnum;
import org.gmote.server.settings.StartupSettings;
import org.gmote.server.settings.StartupSettingsEnum;
import org.gmote.server.settings.SupportedFiletypeSettings;
import org.gmote.server.settings.SystemPaths;

public abstract class GmoteServerUi {
  static final Level DEFAULT_LOG_LEVEL = Level.WARNING;
  static final String LOG_FILE_NAME = "/logs/gmote.log";
  static final String ERROR_LOG_FILE_NAME = "/logs/gmote-errors.log";
  static Logger LOGGER = Logger.getLogger(GmoteServerUi.class.getName());
  static FileHandler logFileHandler;
  protected ActionListener exitListener, settingsListener, mediaPathListener, helpListener, 
    ipAddressListener, pauseListener, playListener, nextListener, previousListener, logFolderListener, shuffleSongsListener;
  GmoteServer server;
  
  protected boolean mediaPlayerControlsVisible = false;

  public GmoteServerUi(GmoteServer server) {
    this.server = server;
  }

  public void createActionListeners() {
    exitListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        LOGGER.info("Exiting...");
        System.exit(0);
      }
    };

    settingsListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showPasswordSettings();
      }
    };

    mediaPathListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showPathChooser();
      }
    };
    
    helpListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(null, "If you are having connection problems, verify the following:\n1. Your phone must be connected to the same network as your computer. To do this, connect the phone to your home's wireless network.\n2. Make sure that your firewall is setup properly.\n\nFor a complete list of troubleshooting steps, please visit:\nhttp://www.gmote.org/faq");
      }
    };
    
    ipAddressListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showIpAddresses();
      }
    };
    

    pauseListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          server.getMediaPlayer().controlPlayer(Command.PAUSE);
        } catch (UnsupportedCommandException e1) {
          LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
        }
      }
    };

    playListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          server.getMediaPlayer().controlPlayer(Command.PLAY);
        } catch (UnsupportedCommandException e1) {
          LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
        }
      }
    };
    
    
    previousListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          server.getMediaPlayer().controlPlayer(Command.REWIND_LONG);
        } catch (UnsupportedCommandException e1) {
          LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
        }
      }
    };
    
    nextListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          server.getMediaPlayer().controlPlayer(Command.FAST_FORWARD_LONG);
        } catch (UnsupportedCommandException e1) {
          LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
        }
      }
    };
    
    logFolderListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        
        ServerUtil.instance().startFileInDefaultApplication(SystemPaths.getRootPath());
        
      }
    };
    
    shuffleSongsListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean shuffleSongs = DefaultSettings.instance().getSetting(
            DefaultSettingsEnum.SHUFFLE_SONGS).equalsIgnoreCase("true");
        DefaultSettings.instance().setSetting(DefaultSettingsEnum.SHUFFLE_SONGS,
            Boolean.toString((shuffleSongs == false)));
        
        
        addShuffleSongMenuItem((shuffleSongs == false));
      }

      
    };
  }

  protected void addShuffleSongMenuItem(boolean shuffleSongs) {
    // Place holder that should be overwritten by subclasses who want to display this option.
    // Should make this abstract eventually but didn't want to modify mac code a few hours before launch.
  }
  
  boolean showPasswordSettings() {
    final PasswordSettingsUi settingsUi = new PasswordSettingsUi();
    return settingsUi.showFrame();
  }

  void showPathChooser() {
    MediaPathChooserUi pathChooser = new MediaPathChooserUi();
    pathChooser.showFrame();
  }
  
  void showIpAddresses() {
    List<InetAddress> addresses = null;
    try {
      addresses = ServerUtil.findAllLocalIpAddresses(true);
    } catch (SocketException e1) {
      LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
    }
    
    String message = "Your local ip address is:\n";
    if (addresses == null || addresses.size() == 0) {
      message += "Sorry, no local ip address was found. Please visit www.gmote.org for more information.";
    } else if (addresses.size() > 1) {
      message = "(Note: It looks like you have more than one local ip. If you are having problems connecting, please try each address below)\n\nYour local ip addresses are:\n";
    }
    
    for (InetAddress address : addresses) {
      message += address.getHostAddress() + "\n";
    }
    
    JOptionPane.showMessageDialog(null, message);
  }

  private static void createFiles() {
    createIfNotExists(SystemPaths.getRootPath());
    
    // Initialize the startup settings first since some of the other settings
    // might need its values.
    try {
      if (new File(SystemPaths.STARTUP_SETTINGS.getFullPath()).createNewFile()) {
        StartupSettings.createDefaultFile();
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
    
    
    for (SystemPaths path : SystemPaths.values()) {
      try {
        if (path == SystemPaths.BASE_PATHS) {

          File newFile = new File(SystemPaths.BASE_PATHS.getFullPath());
          if (!newFile.exists()) {
            // This is here for backwards compatibility. If the user previously
            // had setup a file with base paths,
            // we move it to the new location.
            try {
              URL urlOfOldFile = GmoteServerUi.class.getResource("/config_files/base_paths.txt");
              if (urlOfOldFile != null) {
                File oldFile = new File(urlOfOldFile.toURI());
                boolean success = oldFile.renameTo(newFile);
                if (!success) {
                  LOGGER
                      .warning("Unable to move previous version of base_paths.txt. The base paths will have to be setup again.");
                }

              }
            } catch (URISyntaxException e) {
              LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
          }
        }

        if (new File(path.getFullPath()).createNewFile()) {
          if (path == SystemPaths.DEFAULT_SETTINGS) {
            DefaultSettings.createDefaultFile();
          } else if (path == SystemPaths.PASSWORD) {
            StringEncrypter.writePasswordToFile("");
          } else if (path == SystemPaths.SUPPORTED_FILE_TYPES) {
            SupportedFiletypeSettings.createDefaultFile();
          } else if (path == SystemPaths.BASE_PATHS) {
            BaseMediaPaths.createDefaultFile();
          }
        }

      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      } catch (EncryptionException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }

  /**
   * Creates a directory unless it already exists
   */
  private static void createIfNotExists(String path) {
    File f=new File(path);
    if(f.exists()==false){
        f.mkdirs();
    }
  }

  private void handleStartupSettings() {
    
    if (!StartupSettings.instance().getSetting(StartupSettingsEnum.PATH_SHOWN)) {
      String message = "Welcome to Gmote!\n\nPlease make sure to click 'Unblock' or 'Accept' to any firewall dialogs\nthat may appear to avoid any connection issues.\n\n";
      JOptionPane.showMessageDialog(null, message);
    }
    
    new Thread("StartupSettings") {

      @Override
      public void run() {
        StartupSettings settings = StartupSettings.instance();

        if (!settings.getSetting(StartupSettingsEnum.PASSWORD_SHOWN)) {
          if (showPasswordSettings()) {
            settings.setSetting(StartupSettingsEnum.PASSWORD_SHOWN, true);
          }
        }

        if (!settings.getSetting(StartupSettingsEnum.PATH_SHOWN)) {
          showPathChooser();
          settings.setSetting(StartupSettingsEnum.PATH_SHOWN, true);
        }
        
        handleExtraSettings(settings);
      }
      
    }.start();
  }
  abstract void initializeUi();

  abstract void handleExtraSettings(StartupSettings settings);
  
  abstract public void addMediaPlayerControls();
  
  abstract public void removeMediaPlayerControls();

  void sharedMain(String[] args) {
    System.setProperty("jna.encoding", "UTF8");
    System.setProperty("java.net.preferIPv4Stack", "true");

    // Create necessary directories
    createFiles();
    try {
      logFileHandler = new FileHandler(SystemPaths.GMOTE_LOG.getFullPath());
      logFileHandler.setFormatter(new SimpleFormatter());

      // Allow the user to set the log level. This will allow debugging.
      Level logLevel = DEFAULT_LOG_LEVEL;
      
      Map<String, String> arguments = new HashMap<String, String>();
      for (String arg : args) {
        String[] argSplit = arg.split("=");
        if (argSplit.length == 2) {
          arguments.put(argSplit[0].toLowerCase(), argSplit[1]);
        }
      }
      
      if (arguments.containsKey("loglevel")) {
        logLevel = Level.parse(arguments.get("loglevel"));
      }
      
      Logger defaultLogger = Logger.getLogger("");
      for (Handler handler : defaultLogger.getHandlers()) {
        defaultLogger.removeHandler(handler);
      }
      
      logFileHandler.setLevel(logLevel);
      defaultLogger.addHandler(logFileHandler);
      
      FileHandler errorHandler = new FileHandler(SystemPaths.GMOTE_ERROR_LOG.getFullPath());
      errorHandler.setFormatter(new SimpleFormatter());
      errorHandler.setLevel(Level.WARNING);
      defaultLogger.addHandler(errorHandler);
      
      
      Handler consoleHandler = new ConsoleHandler();
      consoleHandler.setEncoding("UTF-8");
      defaultLogger.addHandler(consoleHandler);
      
      LOGGER.warning("Gmote Version: " + GmoteServer.VERSION);
      LOGGER.warning("OperatingSystem: " + PlatformUtil.getOsName());
      
      createActionListeners();
      
      initializeUi();

      handleStartupSettings();

      server.startServer(this, args);

    } catch (Exception e) {
      // Catching all exceptions at this point since these would be thrown to
      // the user.
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      JOptionPane.showMessageDialog(null, "There was an error running the gmote server. Please visit http:/www.gmote.org/faq for more information\n" + e.getMessage());
    } catch (Error e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      JOptionPane.showMessageDialog(null, "There was an error running the gmote server. Please visit http:/www.gmote.org/faq for more information\n" + e.getMessage());
    }
  }
}