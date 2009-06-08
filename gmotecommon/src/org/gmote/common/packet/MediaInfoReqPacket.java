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


/**
 * A request to get meta data about a file, such as song title and image.
 * 
 * @author Marc
 * 
 */
public class MediaInfoReqPacket extends AbstractPacket implements Serializable{

  private static final long serialVersionUID = 1L;

  String pathAndFileName;
  boolean forceImageUpdate = false;
  /**
   * Constructor.
   * @param pathAndFileName The full path and filename of the file.  
   */
  public MediaInfoReqPacket(String pathAndFileName, boolean forceImageUpdate) {
    super(Command.MEDIA_INFO_REQ);
    this.pathAndFileName = pathAndFileName;
    this.forceImageUpdate = forceImageUpdate;
  }
  
  public boolean isForceImageUpdate() {
    return forceImageUpdate;
  }

  public String getPathAndFileName() {
    return pathAndFileName;
  }

}
