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

package org.gmote.server.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JOptionPane;

import org.jdesktop.swingworker.SwingWorker;

public class Updater {

  private static final String CONFIRM_UPDATE_TITLE = "Confirm Gmote Server Update";
  private static final Logger LOGGER = Logger.getLogger(FileExtractor.class.getName());
  private static final String VERSION = "1.0.0";

  /**
   * @param args
   *          currentServerVersion updateUrl outputDirectory
   *          [onlyDownloadIfMajorUpdate]
   * @throws IOException 
   * @throws SecurityException 
   */
  public static void main(String[] args) throws SecurityException, IOException {
    Logger defaultLogger = Logger.getLogger("");
    for (Handler handler : defaultLogger.getHandlers()) {
      defaultLogger.removeHandler(handler);
    }
    
    FileHandler logFileHandler = new FileHandler("gmoteupdater.log");
    logFileHandler.setFormatter(new SimpleFormatter());
    logFileHandler.setLevel(Level.INFO);
    defaultLogger.addHandler(logFileHandler);
    Handler consoleHandler = new ConsoleHandler();
    consoleHandler.setEncoding("UTF-8");
    defaultLogger.addHandler(consoleHandler);
    
    if (args.length == 1 && args[0].equalsIgnoreCase("getversion")) {
      System.out.println(VERSION);
      System.exit(0);
    } else if (args.length < 3) {
      System.out
          .println("Invalid params: Usage: java Updater currentServerVersion updateUrl outputDirectory [onlyDownloadIfMajorUpdate]");
      System.exit(1);
    }
    String currentServerVersion = args[0];
    String updateUrl = args[1];
    String outputDirectory = args[2];

    // A major update is defined as a change in the second digit of a 3 digit
    // version number.
    // ex: 1.2.0 is a major update from 1.1.0, but 1.1.5 is not a major update
    // from 1.1.0.
    boolean onlyDownloadIfMajorUpdate = false;

    // Indicates that we should wait a bit before downloading, to allow the
    // server to close properly.
    boolean sleepBeforeDownload = false;
    boolean ignoreConfirmDialog = false;
    for (int i = 3; i < args.length; i++) {
      if (args[i].equalsIgnoreCase("onlyDownloadIfMajorUpdate")) {
        onlyDownloadIfMajorUpdate = true;
      } else if (args[i].equalsIgnoreCase("sleepBeforeDownload")) {
        sleepBeforeDownload = true;
      } else if (args[i].equalsIgnoreCase("ignoreConfirmDialog")) {
        ignoreConfirmDialog = true;
      }
    }

    if (sleepBeforeDownload) {
      try {
        LOGGER.info("Making sure that the Gmote Server program was closed properly...");
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }
    }

    Updater updater = new Updater();
    try {
      updater.updateSoftware(currentServerVersion, updateUrl, new File(outputDirectory),
          onlyDownloadIfMajorUpdate, ignoreConfirmDialog);
    } catch (MalformedURLException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      JOptionPane.showMessageDialog(null, e.getMessage());
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      JOptionPane.showMessageDialog(null, e.getMessage());
    } catch (Exception e) {
      // Catching exceptions here to prevent them from getting displayed to the
      // user.
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      JOptionPane.showMessageDialog(null, e.getMessage());
    }

  }

  public static String getLatestVersionNumber(String updateUrl) throws IOException {
    List<String> updateInfo = downloadUpdateInformation(updateUrl);
    if (updateInfo.isEmpty()) {
      return null;
    } else {
      return updateInfo.get(0);
    }
  }

  private void updateSoftware(String currentServerVersion, String updateUrl, File outputDirectory,
      boolean onlyDownloadIfMajorUpdate, boolean ignoreConfirmDialog) throws IOException {
    LOGGER.info("Attempting to update: " + currentServerVersion + " " + updateUrl + " "
        + outputDirectory + " OnlyDownloadIfMajorUpdate=" + onlyDownloadIfMajorUpdate);
    List<String> updateInfo = downloadUpdateInformation(updateUrl);
    if (updateInfo == null || updateInfo.size() < 2) {
      throw new IOException(
          "Content was not retrieved from update website properly. Please try again.");
    }

    // The first line of the file is the version number.
    String latestVersion = updateInfo.get(0);

    if (!ignoreConfirmDialog
        && !askUserIfShouldUpdate(currentServerVersion, latestVersion, onlyDownloadIfMajorUpdate)) {
      LOGGER
          .warning("The version on the server is not different enough from the current version. Will not update. CurrentVerison="
              + currentServerVersion
              + " LatestVersion="
              + latestVersion
              + " OnlyDownloadIfMajorUpdate=" + onlyDownloadIfMajorUpdate);
      return;
    }

    // TODO(mstogaitis): Make sure the server is closed before we attempt the
    // update.

    ProgressDialog progressDialog = ProgressDialog.getInstance();
    ProgressDialog.showProgressDialog();
    UpdateTask task = new UpdateTask(updateInfo, outputDirectory);
    task.addPropertyChangeListener(progressDialog);
    task.execute();

  }

  public static boolean askUserIfShouldUpdate(String currentServerVersion,
      String latestServerVersion, boolean onlyDownloadIfMajorUpdate) {

    boolean shouldUpdate = Updater.shouldUpdate(currentServerVersion, latestServerVersion,
        onlyDownloadIfMajorUpdate);

    int answer;
    if (shouldUpdate) {
      answer = JOptionPane.showConfirmDialog(null,
          "A newer version of the server is available. Would you like to install it now?",
          CONFIRM_UPDATE_TITLE, JOptionPane.YES_NO_OPTION);
    } else {
      answer = JOptionPane.showConfirmDialog(null,
          "You do not need an udpate at this time. The version that you currenty have is "
              + currentServerVersion + " and the latest available version is "
              + latestServerVersion + ". Would you like to update anyway?", CONFIRM_UPDATE_TITLE,
          JOptionPane.YES_NO_OPTION);
    }
    return (answer == JOptionPane.YES_OPTION);
  }

  /**
   * Returns true if the server should be updated based on version numbers.
   * 
   * @return
   */
  private static boolean shouldUpdate(String currentServerVersion, String latestServerVersion,
      boolean onlyDownloadIfMajorUpdate) {

    int[] currentVersion = convertVersion(currentServerVersion);
    int[] latestVersion = convertVersion(latestServerVersion);

    if (currentVersion[0] > latestVersion[0]) {
      return false;
    } else if (currentVersion[0] < latestVersion[0]) {
      return true;
    }

    if (currentVersion[1] > latestVersion[1]) {
      return false;
    } else if (currentVersion[1] < latestVersion[1]) {
      return true;
    } else if (onlyDownloadIfMajorUpdate) {
      return false;
    }

    if (currentVersion[2] < latestVersion[2]) {
      return true;
    }
    // The versions are identical.
    return false;

  }

  /**
   * Converts a string version number into ints.
   * 
   * @param versionNumber
   *          a version number in the form 1.2.3 or 1.2
   * @return an array of 3 integers representing the version number.
   */
  private static int[] convertVersion(String versionNumber) {
    String versionSplit[] = versionNumber.split("\\.");
    int version[] = new int[3];
    version[0] = Integer.parseInt(versionSplit[0]);
    version[1] = Integer.parseInt(versionSplit[1]);
    if (versionSplit.length == 3) {
      // Some older version of the server only had two digits (ex: 1.2)
      version[2] = Integer.parseInt(versionSplit[2]);
    } else {
      version[2] = 0;
    }
    return version;
  }

  /**
   * Downloads a page from the server that will contain information about the
   * current update.
   * 
   * @throws IOException
   */
  static List<String> downloadUpdateInformation(String updateUrl) throws IOException {
    URL serverUrl = new URL(updateUrl);
    BufferedReader reader = new BufferedReader(new InputStreamReader(serverUrl.openStream()));

    List<String> content = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      content.add(line);
    }
    reader.close();
    return content;
  }

  public class UpdateTask extends SwingWorker<Void, Void> {

    List<String> addresses;
    File outputDirectory;

    public UpdateTask(List<String> addresses, File outputDirectory) {
      this.addresses = addresses;
      this.outputDirectory = outputDirectory;
    }

    @Override
    protected Void doInBackground() {
      List<File> downloadedFiles = new ArrayList<File>();
      updateStatusLabel("Step 1: Downloading files from web...");
      for (int i = 1; i < addresses.size(); i++) {
        String[] addressFields = addresses.get(i).split(" ");
        String url = addressFields[0];
        long totalBytes = Long.parseLong(addressFields[1]);
        File downloadedFile;
        try {
          downloadedFile = FileDownloader.download(url, totalBytes, this);
        } catch (IOException e) {
          LOGGER.log(Level.SEVERE, e.getMessage(), e);
          JOptionPane.showMessageDialog(null, e.getMessage());
          return null;
        }

        if (downloadedFile != null) {
          downloadedFiles.add(downloadedFile);
        } else {
          String message = "Received a null file when downloading an update. Aborting...";
          LOGGER.severe(message);
          JOptionPane.showMessageDialog(null, message);
          return null;
        }
      }
      updateStatusLabel("Step 2: Extracting files...");
      for (File downloadedFile : downloadedFiles) {
        if (downloadedFile.getName().toLowerCase().endsWith(".zip")) {
          // Extract the zip file.
          try {
            FileExtractor.unzipArchive(downloadedFile, outputDirectory, this);
          } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            JOptionPane.showMessageDialog(null, "Error while extracting zip file. Please make "
                + "sure that the GmoteServer is closed and then visit www.gmote.org to"
                + " download the update manually.\n" + e.getMessage());
            return null;
          }
        } else {
          // This is an installer. Simply launch it.
          String fullCommand =  "cmd /c start /D" + downloadedFile.getParentFile().getAbsolutePath() + " " + downloadedFile.getName();
          try {
            Runtime.getRuntime().exec(fullCommand);
            System.exit(0);
          } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            JOptionPane.showMessageDialog(null, "Error while starting gmote update package. Please "
                + "visit www.gmote.org to download the update manually. " + e.getMessage());
            return null;
          }
          return null;
          
        }
      }
      updateStatusLabel("Done :). Please start the Gmote Server.");
      return null;
    }

    void updateStatusLabel(String text) {
      firePropertyChange("updatestatuslabel", null, text);

    }

    void setProgressForTask(int progress) {
      setProgress(progress);
    }

    int getProgressForTask() {
      return getProgress();
    }

    public void setProgressBarSize(int size) {
      firePropertyChange("changeprogressbarsize", null, size);
    }
  }
}
