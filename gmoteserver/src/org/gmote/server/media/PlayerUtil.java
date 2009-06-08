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

package org.gmote.server.media;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.ID3Tag;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.v1.ID3V1Tag;
import org.blinkenlights.jid3.v1.ID3V1_0Tag;
import org.blinkenlights.jid3.v1.ID3V1_1Tag;
import org.blinkenlights.jid3.v2.APICID3V2Frame;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;
import org.gmote.common.Protocol.Command;
import org.gmote.common.media.MediaMetaInfo;
import org.gmote.server.settings.DefaultSettings;
import org.gmote.server.settings.DefaultSettingsEnum;


public class PlayerUtil {
  private static Logger LOGGER = Logger.getLogger(PlayerUtil.class.getName());

  // -- Constants -- //
  private static final int VOLUME_INCREMENT = 20;
  private static final int MAX_VOLUME = 100;
  private static final int SMALL_VOLUME_INCREMENT = 5;

  /**
   * Computes a new volume value and stores the value as our program default.
   * This will assure that we use the same value next time we launch a media
   * file.
   * 
   * @param currentVolume
   * @param command
   * @return
   */
  public static int computeNewVolume(int currentVolume, Command command) {
    int newVolume;

    if (command == Command.VOLUME_DOWN) {

      if ((currentVolume - VOLUME_INCREMENT) < VOLUME_INCREMENT) {
        newVolume = currentVolume - SMALL_VOLUME_INCREMENT;
      } else {
        newVolume = currentVolume - VOLUME_INCREMENT;
      }

      if (newVolume < 0) {
        newVolume = 0;
      }

    } else {
      if (currentVolume < VOLUME_INCREMENT) {
        newVolume = currentVolume + SMALL_VOLUME_INCREMENT;
      } else {
        newVolume = currentVolume + VOLUME_INCREMENT;
      }

      if (newVolume > MAX_VOLUME) {
        newVolume = MAX_VOLUME;
      }
    }

    LOGGER.info("New volume value: " + newVolume);
    DefaultSettings.instance()
        .setSetting(DefaultSettingsEnum.VOLUME, Integer.toString(newVolume));

    return newVolume;
  }
  
  public static int normalizeVolume(int volume, int min, int max) {
    return volume * MAX_VOLUME / (max - min);
  }
  
  public static int denormalizeVolume(int volume, int min, int max) {
    return volume * (max - min) / MAX_VOLUME;
  }
  
  public static byte[] loadImage(String imageName) {
    
    InputStream is = PlayerUtil.class.getResourceAsStream("/res/" + imageName);
    if (is == null) {
      return null;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    byte[] image;
    try {
      while (is.read(buffer) != -1) {
        baos.write(buffer);
      }
      image = baos.toByteArray();
      baos.close();
      is.close();
      return image;
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }
  }
  
  public static MediaMetaInfo getSongMetaInfo(MP3File mp3) {
    
    String title = null;
    String artist = null;
    String album = null;
    try {
      for (ID3Tag id3Tag : mp3.getTags()) {
        if (id3Tag instanceof ID3V1_0Tag || id3Tag instanceof ID3V1_1Tag) {
          ID3V1Tag tag = (ID3V1Tag) id3Tag;
          title = setIfNull(title, tag.getTitle());
          artist = setIfNull(artist, tag.getArtist());
          album = setIfNull(album, tag.getAlbum());
          
        } else if (id3Tag instanceof ID3V2_3_0Tag) {
          ID3V2_3_0Tag tag = (ID3V2_3_0Tag)id3Tag;
          title = setIfNull(title, tag.getTitle());
          artist = setIfNull(artist, tag.getArtist());
          album = setIfNull(album, tag.getAlbum());
        }
      }
    } catch (ID3Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
    
    return new MediaMetaInfo(title, artist, album,null,true);
  }
  
  private static String setIfNull(String metaField, String metaData) {
    return (metaField == null) ? metaData : metaField; 
  }
  
  public static byte[] extractEmbeddedImageData(MP3File mp3) {

    try {
      for (ID3Tag tag : mp3.getTags()) {

        if (tag instanceof ID3V2_3_0Tag) {
          ID3V2_3_0Tag tag2 = (ID3V2_3_0Tag) tag;

          if (tag2.getAPICFrames() != null && tag2.getAPICFrames().length > 0) {
            // Simply take the first image that is available.
            APICID3V2Frame frame = tag2.getAPICFrames()[0];
            return frame.getPictureData();
          }
        }
      }
    } catch (ID3Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
    return null;
  }
  
  public static byte[] extractImageFromFolder(String mediaMrl) {
    File file = new File(mediaMrl);
    file = new File(file.getParent() + File.separator + "Folder.jpg");
    byte[] imageData = null;
    if (file.exists()) {
      imageData = extractImageArtworkFromFile(file.getAbsolutePath());
    }
    return imageData;
  }
  
  public static byte[] extractImageArtworkFromFile(String artworkUrl) {
    Image img = Toolkit.getDefaultToolkit().getImage(artworkUrl);
    for (int i = 0; i < 5 && img.getWidth(null) < 0 && img.getHeight(null) < 0; i++) {
      // It takes a bit of time to get info about an image.
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }
    }

    if (img.getWidth(null) > 0 && img.getHeight(null) > 0) {
      BufferedImage bu = new BufferedImage(img.getWidth(null), img.getHeight(null),
          BufferedImage.TYPE_INT_RGB);
      Graphics g = bu.getGraphics();
      g.drawImage(img, 0, 0, null);
      g.dispose();

      ByteArrayOutputStream bas = new ByteArrayOutputStream();
      try {
        ImageIO.write(bu, "PNG", bas);
        return bas.toByteArray();
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }
    }
    return null;
  }
}
