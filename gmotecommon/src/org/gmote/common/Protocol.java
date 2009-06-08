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

package org.gmote.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the protocol that will be uesd between the client and the server.
 * 
 * @author Marc
 * 
 */
public class Protocol {

  /**
   * Enum that contains the list of commands that can be sent from an Android
   * client to the Server.
   * 
   */
  
  public enum Command {
    // Commands that our server will perform.
    BASE_LIST_REQ(CommandType.R3MOTE_SERVER), // Device requests the list of base file paths from the server.
    LIST_REQ(CommandType.R3MOTE_SERVER),
    LIST_REPLY(CommandType.R3MOTE_SERVER),
    MEDIA_INFO_REQ(CommandType.R3MOTE_SERVER),
    MEDIA_INFO(CommandType.R3MOTE_SERVER),
    RUN(CommandType.R3MOTE_SERVER), // Launch a specific file in its default editor.
    SERVER_ERROR(CommandType.R3MOTE_SERVER), // To report errors to the client.
    SUCCESS(CommandType.R3MOTE_SERVER), // When the request succeeded.
    AUTH_REQ(CommandType.R3MOTE_SERVER), // Server requests that the client authenticates.
    AUTH_REPLY(CommandType.R3MOTE_SERVER), // Authentication reply.
    PLAY_DVD(CommandType.R3MOTE_SERVER), // Returned by the server when a browse req is issued to a drive which has a dvd.
    MOUSE_MOVE_REQ(CommandType.R3MOTE_SERVER), // Requests that the server moves the mouse.
    MOUSE_CLICK_REQ(CommandType.R3MOTE_SERVER), // Requests that the server clicks the mouse.
    KEYBOARD_EVENT_REQ(CommandType.R3MOTE_SERVER), // Client sends a keystroke to the server.
    MOUSE_WHEEL_REQ(CommandType.R3MOTE_SERVER),
    UPDATE_SERVER_REQUEST(CommandType.R3MOTE_SERVER), // Client requests that the server updates itself.
    SHOW_ALL_FILES_REQ(CommandType.R3MOTE_SERVER), // Client wants us to send all files, not just playable ones.
    SHOW_PLAYABLE_FILES_ONLY_REQ(CommandType.R3MOTE_SERVER), // Client wants to see only playable files.
    LAUNCH_URL_REQ(CommandType.R3MOTE_SERVER),
    
    // Media Player commands.
    PLAY(CommandType.MEDIA_PLAYER), 
    PAUSE(CommandType.MEDIA_PLAYER),
    STOP(CommandType.MEDIA_PLAYER),
    REWIND(CommandType.MEDIA_PLAYER),
    FAST_FORWARD(CommandType.MEDIA_PLAYER),
    REWIND_LONG(CommandType.MEDIA_PLAYER),
    FAST_FORWARD_LONG(CommandType.MEDIA_PLAYER),
    VOLUME_UP(CommandType.MEDIA_PLAYER),
    VOLUME_DOWN(CommandType.MEDIA_PLAYER),
    MUTE(CommandType.MEDIA_PLAYER),
    UNMUTE(CommandType.MEDIA_PLAYER),
    CLOSE(CommandType.MEDIA_PLAYER), 
    
    TILE_SET_REQ(CommandType.R3MOTE_SERVER),
    TILE_UPDATE(CommandType.R3MOTE_SERVER), 
    TILE_CLICK_REQ(CommandType.R3MOTE_SERVER), 
    TILE_INFO_REQ(CommandType.R3MOTE_SERVER),
    TILE_INFO_REPLY(CommandType.R3MOTE_SERVER),
    
    ;
    
    
    CommandType commandType;
    Command(CommandType type) {
      commandType = type;
    }
    public CommandType getCommandType() {
      return commandType;
    }
    
    public static List<Command> getMediaPlayerCommands(){
      List<Command> commands = new ArrayList<Command>();
      for (Command command : Command.values()) {
        if (command.getCommandType() == CommandType.MEDIA_PLAYER) {
          commands.add(command);
        }
      }
      return commands;
    }
    
  }

  /*
   * Allow us to split our commands into different types.
   */
  public enum CommandType {
    MEDIA_PLAYER,
    R3MOTE_SERVER;
  }
  
  public enum MouseEvent {
    SINGLE_CLICK,
    DOUBLE_CLICK,
    RIGHT_CLICK,
    LEFT_MOUSE_DOWN,
    LEFT_MOUSE_UP;
  }
  
  /**
   * The first byte of a udp packet will be used to identify the type of packet.
   * @author Marc
   *
   */
  public enum UdpPacketTypes {

    SERVICE_DISCOVERY((byte) 0),
    MOUSE_MOVE((byte) 1);

    private byte id;

    UdpPacketTypes(byte id) {
      this.id = id;
    }

    public byte getId() {
      return id;
    }
  }
  
  /**
   * Identifies the error messages that the server sends to the client.
   *  
   * NOTE:
   * The ORDER IS IMPORTANT since we will pass the ordinal to error packets
   * intead of the enum directly to be compatible with older clients (which will
   * get a serialization exception if they try to unserialize a packet with an
   * unknown enum and therefore won't show an 'update your client' message to
   * the client.). Never delete a constant, and append new ones to the end.
   * 
   * @author Marc Stogaitis
   * 
   */
  public enum ServerErrorType {
    INCOMPATIBLE_CLIENT, // Sent when the client is out of date.
    AUTHENTICATION_FAILURE,
    UNSUPPORTED_COMMAND,
    UNSPECIFIED_ERROR, 
    INVALID_FILE(), 
  }
}
