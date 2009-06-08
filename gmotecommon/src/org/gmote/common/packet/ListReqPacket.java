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

import org.gmote.common.FileInfo;
import org.gmote.common.Protocol.Command;


/**
 * A request to get the list of files in a directory.
 * 
 * @author Marc
 * 
 */
public class ListReqPacket extends AbstractPacket implements Serializable{

  private static final long serialVersionUID = 1L;

  String path;
  FileInfo fileInfo = null;

  /**
   * Constructor.
   * @param path The path of the directory where the files are located. 
   */
  public ListReqPacket(String path) {
    super(Command.LIST_REQ);
    this.path = path;
  }
  
  public ListReqPacket(FileInfo fileInfo) {
    super(Command.LIST_REQ);
    this.fileInfo = fileInfo;
    this.path = fileInfo.getAbsolutePath();
  }

  public String getPath() {
    return path;
  }

  public FileInfo getFileInfo() {
    return fileInfo;
  }
  
}
