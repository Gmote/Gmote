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
 * Represents a packet that will be sent over the network. Specific subclasses
 * should be created to store command specific data (such as a list of files for
 * the LIST_REPLY command).
 */
public abstract class AbstractPacket implements Serializable{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final String LINE_BREAK = "\n";

  Command command;

  public AbstractPacket(Command command) {
    this.command = command;
  }

  public Command getCommand() {
    return command;
  }

  @Override
  public String toString() {
    return "Command: " + command.name() + LINE_BREAK;
  }
}
