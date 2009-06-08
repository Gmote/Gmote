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

package org.gmote.server;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gmote.common.Protocol.MouseEvent;
import org.gmote.common.packet.KeyboardEventPacket;
import org.gmote.common.packet.MouseClickPacket;
import org.gmote.common.packet.MouseWheelPacket;

/**
 * Handles requests to move the mouse send keyboard commands.
 * @author Marc
 *
 */
public class TrackpadHandler {
  private static final Logger LOGGER = Logger.getLogger(TrackpadHandler.class
      .getName());
  private static TrackpadHandler instance = null;
  
  Robot robot = null;
  int mouseX = 1;
  int mouseY = 1;
  
  /**
   * Private constructor to prevent instantiation.
   */
  private TrackpadHandler() {
    try {
      robot = new Robot();
    } catch (AWTException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }
  
  /**
   * Gets an instance of this class.
   */
  public static TrackpadHandler instance() {
    if (instance == null) {
      instance = new TrackpadHandler();
    }
    return instance;
  }
  
  /**
   * Moves the mouse on the screen of a user specified amount.
   */
  public void handleMoveMouseCommand(short diffX, short diffY) {
    PointerInfo pointerInfo = MouseInfo.getPointerInfo();
    // On mac, sometimes the pointerinfo is null. This is why we initialize the
    // values based on where the mouse previously was.
    int newX = mouseX + diffX;
    int newY = mouseY + diffY;
     
    if (pointerInfo != null) {
      Point location = pointerInfo.getLocation();
      if (location != null) {
        int currentX = location.x;
        int currentY = location.y;
        newX = currentX + diffX;
        newY = currentY + diffY;
      }
    }
    
    if (!pointIsOnAScreen(newX, newY)) {
      // Move the cursor to the edge.
      Rectangle currentScreen = pointerInfo.getDevice().getDefaultConfiguration().getBounds();
      newX = clampToScreen(newX, currentScreen.x, currentScreen.width);
      newY = clampToScreen(newY, currentScreen.y, currentScreen.height);
    }
    
    robot.mouseMove(newX, newY);
    mouseX = newX;
    mouseY = newY;
    
  }

  private int clampToScreen(int newCoordinate, int screenCoordinate, int screenSize) {
    if (newCoordinate < screenCoordinate) {
      newCoordinate = screenCoordinate;
    } else if (newCoordinate >= screenCoordinate + screenSize) {
      newCoordinate = screenCoordinate + screenSize - 1;
    }
    return newCoordinate;
  }

  private boolean pointIsOnAScreen(int newX, int newY) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gs = ge.getScreenDevices();
    for (GraphicsDevice g : gs) {
      if (g.getDefaultConfiguration().getBounds().contains(newX, newY)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Performs a mouse click based on what was sent by the user.
   */
  public void hanldeMouseClickCommand(MouseClickPacket packet) {
    MouseEvent mouseEvent = packet.getMouseEvent();
    MouseUtil.doMouseEvent(mouseEvent, robot);
  }
 
  public void handleKeyPressCommand(KeyboardEventPacket packet) {
    int keyCode = packet.getKeyCode();
    if (keyCode < 0) {
      // Handle special characters
      if (keyCode == KeyboardEventPacket.DELETE_KEYCODE) {
        type(new int[] {KeyEvent.VK_BACK_SPACE});
      }
    } else {
      char keyChar = (char)keyCode;
      type(keyCodes(keyChar));
    }
    
  }
  
  public void handleMouseWheelCommand(MouseWheelPacket packet) {
    if (PlatformUtil.isMac()) {
      robot.mouseWheel(packet.getWheelAmount() * -1);
    } else {
      robot.mouseWheel(packet.getWheelAmount());
    }
    
  }
  
  /**
   * Type the codes provided. Multiple codes can result if the character is uppercase
   * or special e.g. SHIFT + a (A) or SHIFT + 1 (!)
   */
  private void type(int[] code) {
    int count = code.length;
    int pos = 0;

    try {
      while (count > 1 && pos < count - 1) {
        robot.keyPress(code[pos++]);
      }

      try {
        robot.keyPress(code[pos]);
        robot.keyRelease(code[pos]);
      } catch (IllegalArgumentException e) {
        // Catch the exception here so that we have a chance to
        // do the keyRelease on the keys the previously worked.
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }

      while (count > 1 && pos > 0) {
        robot.keyRelease(code[--pos]);
      }
    } catch (IllegalArgumentException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  /**
   * Maps the specified character to key code acceptable to the Robot. The keys
   * are mapped corresponding to the code from the
   * <code>java.awt.event.KeyEvent</code> associated with the SHIFT key (if
   * necessary).
   * 
   * @param key
   *          the character code to be converted.
   * 
   * @return the array of key codes. If the character is uppercase or special,
   *         the first code returned in the array is the SHIFT key.
   */
  private int[] keyCodes(char key) {
    int[] codes = new int[0];

    if (key >= '0' && key <= '9') {
      codes = new int[] { Character.getNumericValue(key) + 48 };
    } else if (key >= 'a' && key <= 'z') {
      codes = new int[] { Character.getNumericValue(key) + 55 };
    } else if (key >= 'A' && key <= 'Z') {
      codes = new int[] { KeyEvent.VK_SHIFT, Character.getNumericValue(key) + 55 };
    } else if (key == '!') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_1 };
    } else if (key == '@') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_2 };
    } else if (key == '#') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_3 };
    } else if (key == '$') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_4 };
    } else if (key == '%') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_5 };
    } else if (key == '^') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_6 };
    } else if (key == '&') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_7 };
    } else if (key == '*') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_8 };
    } else if (key == '(') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_9 };
    } else if (key == ')') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_0 };
    } else if (key == '<') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA };
    } else if (key == '>') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD };
    } else if (key == '?') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH };
    } else if (key == '|') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH };
    } else if (key == '_') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_MINUS };
    } else if (key == '+') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_EQUALS };
    } else if (key == '{') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET };
    } else if (key == '}') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET };
    } else if (key == ':') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_SEMICOLON };
    } else if (key == '\"') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_QUOTE };
    } else if (key == '~') {
      codes = new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE };
    } else {
      codes = new int[] { keyCode(key) };
    }

    return codes;
  }

  /**
   * Maps the specified character to key code acceptable to the Robot. This key
   * can be directly mapped to the code from the
   * <code>java.awt.event.KeyEvent</code> class.
   * 
   * @param key
   *          the character code to be converted.
   * 
   * @return the corresponding code mapped from the
   *         <code>java.awt.event.KeyEvent</code> class.
   */
  private int keyCode(char key) {
    int code;

    switch (key) {
    case '\\':
      code = KeyEvent.VK_BACK_SLASH;
      break;
    case '[':
      code = KeyEvent.VK_OPEN_BRACKET;
      break;
    case ']':
      code = KeyEvent.VK_CLOSE_BRACKET;
      break;
    case '.':
      code = KeyEvent.VK_PERIOD;
      break;
    case '\'':
      code = KeyEvent.VK_QUOTE;
      break;
    case '/':
      code = KeyEvent.VK_DIVIDE;
      break;
    case '-':
      code = KeyEvent.VK_MINUS;
      break;
    case ',':
      code = KeyEvent.VK_COMMA;
      break;
    case ';':
      code = KeyEvent.VK_SEMICOLON;
      break;
    case '\t':
      code = KeyEvent.VK_TAB;
      break;
    default:
      int keyValue = (int)key;
      if (keyValue == 247) {
        code = KeyEvent.VK_DIVIDE;
      } else if (keyValue == 215) {
        code = KeyEvent.VK_MULTIPLY;
      } else if (keyValue == 61) {
        code = KeyEvent.VK_EQUALS;
      } else if (keyValue == 96) {
        code = KeyEvent.VK_BACK_QUOTE;
      } else if (keyValue == 10) {
        code = KeyEvent.VK_ENTER;
      } else {
        code = KeyEvent.VK_SPACE;
      }
      break;
    }

    return code;
  }
  // Implemented by http://www.codeproject.com/KB/cs/runawayapp.aspx
  
}
