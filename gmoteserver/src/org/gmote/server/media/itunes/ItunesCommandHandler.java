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

package org.gmote.server.media.itunes;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.gmote.common.media.MediaMetaInfo;
import org.gmote.server.media.MediaCommandHandler;
import org.gmote.server.media.MediaInfoUpdater;

import quicktime.QTSession;
import quicktime.app.view.GraphicsImporterDrawer;
import quicktime.app.view.QTImageProducer;
import quicktime.qd.QDRect;
import quicktime.std.image.GraphicsImporter;
import quicktime.util.QTHandle;
import quicktime.util.QTUtils;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.foundation.NSAppleEventDescriptor;
import com.apple.cocoa.foundation.NSAppleScript;
import com.apple.cocoa.foundation.NSAutoreleasePool;
import com.apple.cocoa.foundation.NSData;
import com.apple.cocoa.foundation.NSMutableDictionary;

@SuppressWarnings("deprecation")
public class ItunesCommandHandler extends MediaCommandHandler {
  private static final Logger LOGGER = Logger.getLogger(ItunesCommandHandler.class
      .getName());
  private static ItunesCommandHandler instance = null;
  private static MediaMetaInfo media = null;
  private static String album = null;

  protected ItunesCommandHandler() {
    NSApplication.sharedApplication();
  }

  protected void closeMedia() {
    tellItunesTo("quit", 1);
  }

  protected void stopMedia() {
    tellItunesTo("stop", 1);
  }

  protected void rewind() {
    tellItunesTo("set player position to (player position - 12)", 1);
  }

  protected void fastForward() {
    tellItunesTo("set player position to (player position + 12)", 1);
  }

  protected void pauseMedia() {
    tellItunesTo("pause", 1);
  }

  protected void playMedia() {
    tellItunesTo("play\n\nset song repeat of current playlist to all", 1);
    MediaInfoUpdater.instance().sendMediaUpdate(getNewMediaInfo());
  }

  protected void rewindLong() {
    tellItunesTo("previous track", 1);
    MediaInfoUpdater.instance().sendMediaUpdate(getNewMediaInfo());
  }

  protected void fastForwardLong() {
    tellItunesTo("next track", 1);
    MediaInfoUpdater.instance().sendMediaUpdate(getNewMediaInfo());
  }

  protected void setVolume(int volume) {
    tellItunesTo("set sound volume to " + volume, 1);
  }

  protected int getVolume() {
    for (int i=0; i<3; i++) {
      try {
        NSAppleEventDescriptor result = tellItunesTo("get sound volume", 1);
        return result.int32Value();
      } catch(Exception e){
        // getting volume failed, try again
      }
    }
    return 50;
  }

  protected void toggleMute() {
    tellItunesTo("set mute to (not mute)", 1);
  }

  protected void fullScreen() {
    String script = "tell application \"System Events\"\n"
        + "keystroke \"f\" using command down\n" + "end tell\n";
    executeForResult(script, 1);
  }

  public List<String> getPlaylists() {
    List<String> playlists = new ArrayList<String>();
    for (int times = 0; times < 3; times++) {
      try {
        NSAppleEventDescriptor result = tellItunesTo(
            "get the name of every playlist", 20);

        int numItems = result.numberOfItems();
        for (int i = 1; i <= numItems; i++) {
          NSAppleEventDescriptor plDiscriptor = result.descriptorAtIndex(i);
          playlists.add(plDiscriptor.stringValue());
        }
        break;
      } catch (Exception e) {
      }
    }
    return playlists;
  }

  public List<String> getTracksFromPlaylist(String playlist) {
    List<String> tracks = new ArrayList<String>();
  
    for (int times = 0; times < 3; times++) {
      try {
        String script = "set theTracks to {}\n"
            + "repeat with aTrack in tracks of playlist \""
            + playlist
            + "\"\n"
            + "copy (name of aTrack) & \"|\" & (kind of aTrack) to end of theTracks\n"
            + "end repeat\n" + "theTracks\n";
        NSAppleEventDescriptor result = tellItunesTo(script, 1);

        int numItems = result.numberOfItems();
        for (int i = 1; i <= numItems; i++) {
          NSAppleEventDescriptor trackDiscriptor = result.descriptorAtIndex(i);
          tracks.add(trackDiscriptor.stringValue());
        }
        break;
      } catch (Exception e) {
      }
    }
    return tracks;
  }

  protected void launchAudio(String track, String playlist) {
    tellItunesTo("play track \"" + track + "\" of playlist \"" + playlist
        + "\"\nset song repeat of current playlist to all", 1);
  }

  protected void launchVideo(String name) {
    tellItunesTo("set visible of window 1 to true\nset frontmost to true", 1);
    tellItunesTo("set view of browser window 1 to playlist \"Movies\"", 1);
    tellItunesTo("play track \"" + name + "\" of playlist \"Movies\"", 1);
    fullScreen();
    LOGGER.log(Level.INFO, "Launched movie");
  }
  
  public synchronized MediaMetaInfo getNewMediaInfo() {
    try {
      NSAppleEventDescriptor result = tellItunesTo("get {name, artist, album} of current track", 1);
      String title = result.descriptorAtIndex(1).stringValue();
      String artist = result.descriptorAtIndex(2).stringValue();

      if (media != null && media.getTitle().equals(title) && media.getArtist().equals(artist)) {
        return null;
      } else {
        String newAlbum = result.descriptorAtIndex(3).stringValue();
        media = new MediaMetaInfo();
        media.setTitle(title);
        media.setArtist(artist);
        media.setAlbum(newAlbum);
        
        if (!newAlbum.equals(album)) {
          album = newAlbum;
          getArtwork();
        } else {
          media.setImage(new byte[]{1});
        }
      }
        
      return media;
    } catch(java.lang.NullPointerException e) {
    } catch(Exception e) {
      LOGGER.log(Level.INFO, e.getMessage(), e);
    }
    media = null;
    return media;
  }

  private void getArtwork() {
    NSAppleEventDescriptor result;
    try {
      int myPool = NSAutoreleasePool.push();

      result = tellItunesTo("get data of artwork 1 of current track", 1);
      NSData data = result.data();
      byte[] pictbytes = data.bytes(0, data.length());
      byte []newpictbytes = new byte[512+pictbytes.length];
      System.arraycopy(pictbytes,0,newpictbytes,512,pictbytes.length);
      
      if(QTSession.isInitialized() == false) {
        QTSession.open();
      }
      QTHandle qt = new QTHandle(newpictbytes); 
      GraphicsImporter gc = new GraphicsImporter(QTUtils.toOSType("PICT"));
      gc.setDataHandle(qt);
      QDRect qdRect = gc.getNaturalBounds();
      GraphicsImporterDrawer myDrawer = new quicktime.app.view.GraphicsImporterDrawer(gc);
      QTImageProducer qtProducer = new QTImageProducer (myDrawer, new Dimension(qdRect.getWidth(),qdRect.getHeight()));
      Image img = Toolkit.getDefaultToolkit().createImage(qtProducer);
      
      BufferedImage bu = new BufferedImage(img.getWidth(null),img.getHeight(null),BufferedImage.TYPE_INT_RGB);
      Graphics g = bu.getGraphics();
      g.drawImage(img,0,0,null);
      g.dispose();
      
      ByteArrayOutputStream bas = new ByteArrayOutputStream();
      ImageIO.write(bu, "PNG", bas);
      byte[] image = bas.toByteArray();
      //// System.out.println("++ image size: " + image.length);
      media.setImage(image);
      
      NSAutoreleasePool.pop(myPool);
    } catch(java.lang.NullPointerException e) {
    } catch(Exception e) {
      LOGGER.log(Level.INFO, e.getMessage(), e);
    } finally {
        QTSession.close();
    }
  }
  
  public static ItunesCommandHandler instance() {
    if (instance  == null) {
      instance = new ItunesCommandHandler();
    }
    return instance;
  }
  
  protected boolean running() {
    try {
      String script = "tell application \"System Events\"\n" +
        "set isRunning to ((application processes whose (name is equal to \"iTunes\")) count)\n" +
        "end tell\n" +
        "if isRunning is greater than 0 then\n" +
        "return true\n" +
        "else\n" +
        "return false\n" + 
        "end if";
      NSAppleEventDescriptor result = executeForResult(script, 3);
      System.out.println("running: " +  result);
      return result.booleanValue();
    } catch (Exception e) {
      LOGGER.log(Level.INFO, e.getMessage(), e);
    }
    return false;
  }
  
  public boolean isMediaOpen() {
    return running();
  }
  
  protected boolean isMediaPaused() {
    return false; //TODO(mimi)
  }
  
  /* Run commands with iTunes */
  private NSAppleEventDescriptor tellItunesTo(String actions, int timeout) {
    String script = "tell application \"iTunes\"\n" + actions + "\n end tell\n";
    return executeForResult(script, timeout);
  }

  private synchronized NSAppleEventDescriptor executeForResult(String script, int timeout) {
    script = "with timeout " + timeout + " seconds\n" + script + "\nend timeout\n";
    //LOGGER.log(Level.INFO, "\nExecuting script for result: " + script);

    // This creates a new NSAppleScript object
    // to execute the script
    NSAppleScript myScript = new NSAppleScript(script);

    // This dictionary holds any errors
    // that are encountered during script execution
    NSMutableDictionary errors = new NSMutableDictionary();

    // Execute the script!
    NSAppleEventDescriptor results = myScript.execute(errors);

    // Print out results
    //if (results != null) {
      //LOGGER.log(Level.INFO, "Results: " + results.toString());
    //}
    return results;
  }

  void launchFile(String fileName) {
    String fileNameForScript = fileName.replace('/', ':');
    tellItunesTo("play file \"" + fileNameForScript + "\"", 2);
  }
  
}
