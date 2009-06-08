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

package org.gmote.common.packet;

import java.io.Serializable;

import org.gmote.common.Protocol.Command;
import org.gmote.common.media.MediaMetaInfo;


/**
 * A packet that contains detailed information about a single media/file
 * 
 * @author Mimi
 * 
 */
public class MediaInfoPacket extends AbstractPacket implements Serializable{

  private static final long serialVersionUID = 1L;

  MediaMetaInfo media;

  public MediaInfoPacket(MediaMetaInfo media) {
    super(Command.MEDIA_INFO);
    this.media = media;
  }

  public MediaMetaInfo getMedia() {
    return media;
  }
  
  @Override
  public String toString() {
    String ret = super.toString();
    ret += media.toString();
    return ret;
  }
}
