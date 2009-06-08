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

import org.gmote.common.Protocol;
import org.gmote.common.Protocol.Command;


/**
 * Reports a server-side error to the device.
 * 
 * @author Marc
 * 
 */
public class ServerErrorPacket extends AbstractPacket implements Serializable{

  private static final long serialVersionUID = 1L;

  String errorDescription;
  int errorTypeOrdinal;

  /**
   * Creates a server error packet
   * 
   * @param errorTypeOrdinal
   *          the ordinal of the Protocol.ServerErrorType. We don't pass the
   *          enum directly for backwards compatibility reasons
   *          see {@link Protocol.ServerErrorType}
   * @param errorDescription a description of the error that may be displayed to the user.
   */
  public ServerErrorPacket(int errorTypeOrdinal, String errorDescription) {
    super(Command.SERVER_ERROR);
    this.errorDescription = errorDescription;
    this.errorTypeOrdinal = errorTypeOrdinal;
  }
  
  public int getErrorTypeOrdinal() {
    return errorTypeOrdinal;
  }

  public String getErrorDescription() {
    return errorDescription;
  }
}
