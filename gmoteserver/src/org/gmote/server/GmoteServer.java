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
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.gmote.common.DataReceiverIF;
import org.gmote.common.FileInfo;
import org.gmote.common.TcpConnection;
import org.gmote.common.FileInfo.FileSource;
import org.gmote.common.FileInfo.FileType;
import org.gmote.common.Protocol.Command;
import org.gmote.common.Protocol.CommandType;
import org.gmote.common.Protocol.ServerErrorType;
import org.gmote.common.media.MediaMetaInfo;
import org.gmote.common.packet.AbstractPacket;
import org.gmote.common.packet.KeyboardEventPacket;
import org.gmote.common.packet.LaunchUrlPacket;
import org.gmote.common.packet.ListReplyPacket;
import org.gmote.common.packet.ListReqPacket;
import org.gmote.common.packet.MouseClickPacket;
import org.gmote.common.packet.MouseMovePacket;
import org.gmote.common.packet.MouseWheelPacket;
import org.gmote.common.packet.RunFileReqPacket;
import org.gmote.common.packet.ServerErrorPacket;
import org.gmote.common.packet.SimplePacket;
import org.gmote.common.packet.TileClickReq;
import org.gmote.common.packet.TileSetReq;
import org.gmote.server.media.MediaInfoUpdater;
import org.gmote.server.media.MediaPlayerInterface;
import org.gmote.server.media.MediaPlayerManager;
import org.gmote.server.media.UnsupportedCommandException;
import org.gmote.server.settings.BaseMediaPaths;
import org.gmote.server.settings.DefaultSettings;
import org.gmote.server.settings.DefaultSettingsEnum;
import org.gmote.server.settings.SupportedFiletypeSettings;
import org.gmote.server.updater.Updater;
import org.gmote.server.visualtouchpad.VisualTouchpad;

@SuppressWarnings("deprecation")
public class GmoteServer implements DataReceiverIF {

  private static final Logger LOGGER = Logger.getLogger(GmoteServer.class.getName());
  static final String VERSION = "2.0.2";
  static final String MINIMUM_CLIENT_VERSION = "2.0.0";

  private GmoteServerUi serverUi;

  MediaPlayerManager mediaPlayerManager;
  MediaPlayerInterface activeMediaPlayer = null;

  /**
   * Starts the server.
   * 
   * @param ui
   * @param arguments
   *          The command line arguments that were passed to the program in a
   *          key=value format. (ex: loglevel=ALL)
   * @throws IOException
   */
  void startServer(GmoteServerUi ui, String[] arguments) throws IOException {
    this.serverUi = ui;

    // Start a thread that will supply our ip to clients.
    int mouseUdpPort = MulticastServerThread.MULTICAST_LISTENING_PORT;
    try {
      mouseUdpPort = Integer.parseInt(DefaultSettings.instance().getSetting(
          DefaultSettingsEnum.UDP_PORT));
    } catch (NumberFormatException e) {
      LOGGER
          .warning("There was an error reading the udp port from the config file. Using default setting. "
              + e.getMessage());
      DefaultSettings.instance().setSetting(DefaultSettingsEnum.UDP_PORT,
          Integer.toString(mouseUdpPort));
    }

    MulticastServerThread.listenForIpRequests(mouseUdpPort);

    // Initialize the media player manager.
    mediaPlayerManager = MediaPlayerManager.getInstance();
    mediaPlayerManager.initialize(arguments);

    // Start the tcp threads that will handle connections.
    TcpConnectionHandler.instance(this).listenOnAllIpAddresses();
  }


  /**
   * Called when a packet is received from the user.
   */
  public synchronized void handleReceiveData(AbstractPacket packet, TcpConnection connection) {
    LOGGER.info("Received command: " + packet.toString());
    Command command = packet.getCommand();

    AbstractPacket returnPacket = null;

    if (command == Command.BASE_LIST_REQ) {
      // Return the base list of directories the client has access to.
      List<FileInfo> existingBasePaths = new ArrayList<FileInfo>();
      for (FileInfo path : BaseMediaPaths.getInstance().getBasePaths()) {
        // Make sure that we only return paths that exist.
        if (new File(path.getAbsolutePath()).exists()) {
          existingBasePaths.add(path);
        }
      }

      // Get the files from the media library exposed by the media player.
      // TODO(mstogaitis): Right now, we're only looking at the files from the
      // music media player. We should look into how to handle cases where we
      // might have multiple media players (for example, if there's a vlc player
      // and windows media player binding installed). There's a tradeoff between
      // how loading media players that the user doesn't need (say he's only
      // using the default player and doesn't care that we support additional
      // players), vs being able to display media info from the media libraries
      // of all the media players we support.
      List<FileInfo> libraryList = mediaPlayerManager.getMediaPlayer(FileType.MUSIC)
          .getBaseLibraryFiles();
      if (libraryList != null) {
        existingBasePaths.addAll(libraryList);
      }

      returnPacket = new ListReplyPacket(existingBasePaths.toArray(new FileInfo[existingBasePaths
          .size()]));
    } else if (command == Command.LIST_REQ) {
      // Return a list of files.
      ListReqPacket listReqPacket = (ListReqPacket) packet;

      if (listReqPacket.getFileInfo().getFileSource() == FileSource.MEDIA_LIBRARY) {
        List<FileInfo> libraryFiles = mediaPlayerManager.getMediaPlayer(FileType.MUSIC)
            .getLibrarySubFiles(listReqPacket.getFileInfo());
        returnPacket = new ListReplyPacket(libraryFiles.toArray(new FileInfo[libraryFiles.size()]));
      } else {
        // The file is on the file system.
        String path = listReqPacket.getPath();
        File file = new File(path);
        if (file.exists()) {
          if (ServerUtil.instance().isDvdDrive(file) && ServerUtil.instance().driveHasDvd(file)) {
            returnPacket = new SimplePacket(Command.PLAY_DVD);
            sendPacket(connection, returnPacket);
            returnPacket = null;
            runMedia(new FileInfo(path, path, FileType.DVD_DRIVE, false, FileSource.FILE_SYSTEM));
          } else {
            returnPacket = createListFilesPacket(path);
          }
        } else {
          returnPacket = createFileNotExistErrorPacket();
        }
      }

    } else if (command == Command.RUN) {
      // Run a file in its default application.
      FileInfo fileInfo = ((RunFileReqPacket) packet).getFileInfo();
      returnPacket = runMedia(fileInfo);

    } else if (command.getCommandType() == CommandType.MEDIA_PLAYER) {
      // Handle media player operations (PAUSE, MUTE etc.).
      try {
        getMediaPlayer().controlPlayer(command);
        returnPacket = new SimplePacket(Command.SUCCESS);

        if (command == Command.CLOSE) {
          if (activeMediaPlayer != null && !activeMediaPlayer.isRunning()) {
            setActiveMediaPlayer((MediaPlayerInterface) null);
          }
        }
      } catch (UnsupportedCommandException e) {
        returnPacket = new ServerErrorPacket(ServerErrorType.UNSUPPORTED_COMMAND.ordinal(), e
            .getMessage());
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }
    } else if (command == Command.MOUSE_MOVE_REQ) {
      // We will move the mouse.
      // Depricated. We now send over udp.
      legacyMouseMove(packet);
    } else if (command == Command.MOUSE_CLICK_REQ) {
      // We will do a single click.
      TrackpadHandler.instance().hanldeMouseClickCommand((MouseClickPacket) packet);
    } else if (command == Command.KEYBOARD_EVENT_REQ) {
      TrackpadHandler.instance().handleKeyPressCommand((KeyboardEventPacket) packet);
    } else if (command == Command.MOUSE_WHEEL_REQ) {
      TrackpadHandler.instance().handleMouseWheelCommand((MouseWheelPacket) packet);
    } else if (command == Command.UPDATE_SERVER_REQUEST) {
      updateServer();
    } else if (command == Command.SHOW_ALL_FILES_REQ) {
      DefaultSettings.instance().setSetting(DefaultSettingsEnum.SHOW_ALL_FILES, "true");
    } else if (command == Command.SHOW_PLAYABLE_FILES_ONLY_REQ) {
      DefaultSettings.instance().setSetting(DefaultSettingsEnum.SHOW_ALL_FILES, "false");
    } else if (command == Command.MEDIA_INFO_REQ) {
      returnPacket = MediaInfoUpdater.instance().handleMediaInfoReq(packet);
    } else if (command == Command.LAUNCH_URL_REQ) {
      String url = ((LaunchUrlPacket) packet).getUrl();
      BrowserLauncherUtil.openURL(url);  
    } else if (command == Command.TILE_SET_REQ) {
      VisualTouchpad.instance().tileUpdateRequest((TileSetReq) packet);
    } else if (command == Command.TILE_CLICK_REQ) {
      VisualTouchpad.instance().tileClickRequest((TileClickReq) packet);
    } else if (command == Command.TILE_INFO_REQ) {
      returnPacket = VisualTouchpad.instance().createScreenInfoReply();
      VisualTouchpad.instance().clearTileImages();
    }

    if (returnPacket != null) {
      sendPacket(connection, returnPacket);
      LOGGER.info("Sent reply to client");
    } else if (command != Command.MOUSE_CLICK_REQ && command != Command.MOUSE_MOVE_REQ
        && command != Command.KEYBOARD_EVENT_REQ) {
      LOGGER.warning("Did not send a return packet for an incomming request: " + packet);
    }
  }

  private void sendPacket(TcpConnection connection, AbstractPacket returnPacket) {
    // Send a return packet to the client.
    try {
      connection.sendPacket(returnPacket);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public class MediaRunner implements Runnable {
    FileInfo fileInfo;
    AbstractPacket returnPacket;

    public MediaRunner(FileInfo fileInfo) {
      this.fileInfo = fileInfo;
      // Initialise this with an error packet in case our thread gets cut off.
      returnPacket = new ServerErrorPacket(ServerErrorType.UNSPECIFIED_ERROR.ordinal(),
          "An unspecified error occurred");
    }

    public AbstractPacket getReturnPacket() {
      return returnPacket;
    }

    public void run() {
      try {
        if (fileInfo.getFileSource() == FileSource.MEDIA_LIBRARY) {
          setActiveMediaPlayer(FileType.MUSIC);
        } else if (fileInfo.getFileType() == FileType.DVD_DRIVE) {
          setActiveMediaPlayer(FileType.DVD_DRIVE);
        } else if (new File(fileInfo.getAbsolutePath()).exists()) {
          setActiveMediaPlayer(fileInfo.getFileName());
        } else {
          returnPacket = createFileNotExistErrorPacket();
          return;
        }

        activeMediaPlayer.runFile(fileInfo);
        serverUi.addMediaPlayerControls();
        returnPacket = new SimplePacket(Command.SUCCESS);

      } catch (UnsupportedEncodingException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
        returnPacket = new ServerErrorPacket(ServerErrorType.UNSPECIFIED_ERROR.ordinal(), e
            .getMessage());
      } catch (UnsupportedCommandException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
        returnPacket = new ServerErrorPacket(ServerErrorType.UNSUPPORTED_COMMAND.ordinal(), e
            .getMessage());
      }
    }
  }

  /**
   * Run a file. We are going to do this in a seperate thread since I've seen
   * cases where vlc gets blocked and blocks the rest of the application. We
   * should look into these cases and fix them so that we don't need this work
   * arround.
   * 
   * @param fileName
   * @return
   */
  private AbstractPacket runMedia(FileInfo fileInfo) {

    MediaRunner runner = new MediaRunner(fileInfo);
    Thread t = new Thread(runner, "MediaRunner");
    t.start();
    try {
      t.join();//20 * 1000);
      if (t.isAlive()) {
        // Kill the thread if it's still alive.
        t.interrupt();
        reloadActiveMediaPlayer();
      }

    } catch (InterruptedException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }

    return runner.getReturnPacket();
  }

  private AbstractPacket createFileNotExistErrorPacket() {
    AbstractPacket returnPacket;
    returnPacket = new ServerErrorPacket(ServerErrorType.INVALID_FILE.ordinal(),
        "The file does not exist.");
    return returnPacket;
  }

  /**
   * Creates a packet with the list of the files in the current directory.
   */
  ListReplyPacket createListFilesPacket(String directory) {
    File file = new File(directory);
    FileFilter filter = new FileFilter() {
      public boolean accept(File arg0) {

        if (arg0.isHidden()) {
          return false;
        }

        if (arg0.isDirectory()) {
          return true;
        }

        String name = arg0.getName().toLowerCase();
        if (SupportedFiletypeSettings.fileNameToFileType(name) != FileType.UNKNOWN) {
          return true;
        }

        boolean showAllFiles = DefaultSettings.instance().getSetting(
            DefaultSettingsEnum.SHOW_ALL_FILES).equalsIgnoreCase("true");
        if (showAllFiles) {
          return true;
        }

        return false;
      }
    };

    File[] listOfFiles = file.listFiles(filter);
    FileInfo[] fileInfo = convertFileListToFileInfo(listOfFiles);
    Arrays.sort(fileInfo);
    ListReplyPacket packet = new ListReplyPacket(fileInfo);
    return packet;
  }

  private FileInfo[] convertFileListToFileInfo(File[] files) {
    FileInfo[] fileInfo = new FileInfo[files.length];
    int index = 0;
    for (File file : files) {
      fileInfo[index] = ServerUtil.instance().fileInfoFromFile(file);
      index++;
    }
    return fileInfo;
  }

  public MediaPlayerInterface getMediaPlayer() {
    if (activeMediaPlayer == null) {
      activeMediaPlayer = mediaPlayerManager.getRunningMediaPlayer();
    }
    return activeMediaPlayer;
  }

  private void setActiveMediaPlayer(FileType fileType) {
    setActiveMediaPlayer(mediaPlayerManager.getMediaPlayer(fileType));
    MediaInfoUpdater.instance().sendMediaUpdate(activeMediaPlayer.getNewMediaInfo());
  }

  private void setActiveMediaPlayer(String fileName) {
    setActiveMediaPlayer(mediaPlayerManager.getMediaPlayer(fileName));
    MediaInfoUpdater.instance().sendMediaUpdate(activeMediaPlayer.getNewMediaInfo());
  }

  private void setActiveMediaPlayer(MediaPlayerInterface player) {
    activeMediaPlayer = player;
    if (player == null) {
      LOGGER.info("Setting active media player to null");
      MediaInfoUpdater.instance().sendMediaUpdate(new MediaMetaInfo(null, null, null, null, false));
    }
  }

  private void reloadActiveMediaPlayer() {
    activeMediaPlayer = mediaPlayerManager
        .reloadMediaPlayer(activeMediaPlayer.getClass().getName());
  }

  private void updateServer() {
    String updateUrl = ServerUtil.instance().getUpdateUrl();

    try {
      String latestVersion = Updater.getLatestVersionNumber(updateUrl);
      boolean shouldIUpdate = Updater.askUserIfShouldUpdate(VERSION, latestVersion, false);
      if (!shouldIUpdate) {
        return;
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      JOptionPane
          .showMessageDialog(
              null,
              "Unable to fetch update information. Plese try again later or visit www.gmote.org to download updates manually\n"
                  + e.getMessage());
      return;
    }

    LOGGER.warning("Updating server");
    String installDirectory = ServerUtil.instance().findInstallDirectory();
    String updaterPath = " org.gmote.server.updater.Updater ";
    String updaterArgs = " -classpath ." + File.pathSeparator + "bin/" + File.pathSeparator
        + "lib/swing-worker-1.2.jar ";
    ServerUtil.instance().startFileInSeparateprocess(
        "java " + updaterArgs + updaterPath + " " + VERSION + " " + updateUrl + " "
            + installDirectory + " sleepBeforeDownload ignoreConfirmDialog");

    // Exit the application to allow the updater to run.
    System.exit(0);
  }

  // This handles the legacy 'tcp' mouse move packet. We now use a udp mechanism
  // but we'll keep this around for backwards compatibility.
  private void legacyMouseMove(AbstractPacket packet) {
    MouseMovePacket mmp = (MouseMovePacket) packet;
    TrackpadHandler.instance().handleMoveMouseCommand(mmp.getDiffX(), mmp.getDiffY());
  }

}
