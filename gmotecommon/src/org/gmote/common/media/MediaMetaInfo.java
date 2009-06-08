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

package org.gmote.common.media;

import java.io.Serializable;

public class MediaMetaInfo implements Serializable {
  
  private static final long serialVersionUID = -7010793207904547726L;
  
  String title = null;
  String artist = null;
  String album = null;
  byte[] image = null;
  boolean showImageOnBackground = true;
  boolean imageSameAsPrevious = false; // Used for caching (send image = null and iageIsSame = true)
  
  public MediaMetaInfo(String title, String artist, String album, byte[] image, boolean showImageOnBackground) {
    this.title = title;
    this.artist = artist;
    this.album = album;
    this.image = image;
    this.showImageOnBackground = showImageOnBackground;
  }
  
  public boolean isImageSameAsPrevious() {
    return imageSameAsPrevious;
  }

  public void setImageSameAsPrevious(boolean imageSameAsPrevious) {
    this.imageSameAsPrevious = imageSameAsPrevious;
  }

  public MediaMetaInfo() {
    
  }

  public String getTitle() {
    return title;
  }

  public String getArtist() {
    return artist;
  }

  public String getAlbum() {
    return album;
  }

  public byte[] getImage() {
    return image;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }

  public void setArtist(String artist) {
    this.artist = artist;
  }

  public void setAlbum(String album) {
    this.album = album;
  }

  public void setImage(byte[] image) {
    this.image = image;
  }
  
  public boolean isShowImageOnBackground() {
    return showImageOnBackground;
  }

  public void setShowImageOnBackground(boolean showImageOnBackground) {
    this.showImageOnBackground = showImageOnBackground;
  }
  
}
