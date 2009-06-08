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

import org.gmote.common.Protocol.Command;

/**
 * Allows us to send an instruction to simulate a move of the mouse wheel up or down.
 * @author Marc Stogaitis
 */
public class MouseWheelPacket extends AbstractPacket {

  private static final long serialVersionUID = 1L;
  
  private int wheelAmount;
  
  public MouseWheelPacket(int wheelAmount) {
    super(Command.MOUSE_WHEEL_REQ);
    this.wheelAmount = wheelAmount;
    
  }

  public int getWheelAmount() {
    return wheelAmount;
  }
  
}
