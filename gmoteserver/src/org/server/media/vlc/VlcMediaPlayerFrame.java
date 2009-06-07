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

package org.gmote.server.media.vlc;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.gmote.common.Protocol.Command;
import org.gmote.server.media.UnsupportedCommandException;
import org.gmote.server.settings.DefaultSettings;
import org.gmote.server.settings.DefaultSettingsEnum;
import org.videolan.jvlc.MediaPlayer;
import org.videolan.jvlc.Video;


//users should call jvlc.setVideoOutput(jvcanvas);
public class VlcMediaPlayerFrame {
  private static Logger LOGGER = Logger.getLogger(VlcMediaPlayerFrame.class.getName());
  private static final long serialVersionUID = 1L;

  JFrame vlcFrame;
  Canvas vlcCanvas;
  
  private VlcMediaPlayer mediaPlayerController;
  private MediaPlayer player;
  
  public VlcMediaPlayerFrame(VlcMediaPlayer mediaPlayerController, MediaPlayer player) {
    this.mediaPlayerController = mediaPlayerController;
    this.player = player;
  }
  
  public void createFullScreenWindow(){
    if (vlcFrame != null && vlcFrame.isVisible()) {
      vlcFrame.setVisible(false);
    }
    // Create a new frame.
    vlcFrame = createFrame();
    
    // Setup the frame to be in full screen.
    // Note: Not using vlc's fullScreen() method since it doesn't always come
    // to the foreground when launched
    // from a java application. We are not using java 'device' fullscreen
    // method for the same reason.
    vlcFrame.setUndecorated(true);
    vlcFrame.setAlwaysOnTop(true);
    vlcFrame.setResizable(false);
    vlcFrame.setVisible(true);
    vlcFrame.setExtendedState(vlcFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
  
    // Hide the mouse. We'll move it instead of setting it to invisible since
    // we don't seem to have access to the main component where VLC is playing the movie.
    try {
      Robot r = new Robot();
      r.mouseMove(0, (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight());
    } catch (AWTException e) {
      LOGGER.log(Level.SEVERE,e.getMessage(),e);
    }
  }
  
  /**
   * Exit full screen mode.
   */
  public void changeFromFullScreenToNormal() {
    vlcFrame.setVisible(false);

    
    vlcFrame = createFrame();
    vlcFrame.setSize(500,500);
    vlcFrame.setVisible(true);
    
    //vlcFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    new Video(mediaPlayerController.jvlc).reparent(player, vlcCanvas);
  }
  
  public Canvas getCanvas() {
    return vlcCanvas;
  }
  
  public void closeFrame() {
    vlcFrame.setVisible(false);
    vlcFrame.dispose();
    vlcFrame = null;
    vlcCanvas = null;
  }

  /**
   * Sets up a frame with common settings for both full screen and normal mode.
   * @return
   */
  private JFrame createFrame() {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
    frame.setBackground(Color.BLACK);
    
    String xPosStr = DefaultSettings.instance().getSetting(DefaultSettingsEnum.MONITOR_X);
    String yPosStr = DefaultSettings.instance().getSetting(DefaultSettingsEnum.MONITOR_Y);
    
    int xPos = 0;
    int yPos = 0;
    if (xPosStr.length() > 0 && xPosStr.length() > 0) {
      try {
        xPos = Integer.parseInt(xPosStr);
        yPos = Integer.parseInt(yPosStr);
      } catch (NumberFormatException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
        xPos = 0;
        yPos = 0;
      }
    }
    
    frame.setLocation(xPos, yPos);
    // Add the canvas where VLC will paint the video.
    vlcCanvas = createCanvas();
    frame.add(vlcCanvas);
    
    // Add a key listener that will take the window out of full screen mode when
    // 'esc' is pressed.
    frame.addKeyListener(new KeyAdapter(){
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          changeFromFullScreenToNormal();
        }
      }
    });
    
    // Add a listener that will resize our canvas.
    frame.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        super.componentResized(e);
        vlcCanvas.setSize(e.getComponent().getSize());
      }

      @Override
      public void componentMoved(ComponentEvent e) {
        super.componentMoved(e);
        System.out.println("moved " + e.getComponent().getX());
        DefaultSettings.instance().setSetting(DefaultSettingsEnum.MONITOR_X,
            Integer.toString(e.getComponent().getX()));
        DefaultSettings.instance().setSetting(DefaultSettingsEnum.MONITOR_Y,
            Integer.toString(e.getComponent().getY()));
      }
    });
    
    // Add a listener that will stop the video if the 'x' button is pressed on the window.
    frame.addWindowListener(new WindowAdapter()
    {
        public void windowClosing(WindowEvent evt)
        {
          try {
            mediaPlayerController.controlPlayer(Command.CLOSE);
          } catch (UnsupportedCommandException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
          }  
        }
    });
    
    return frame;
  }
  
  /**
   * Creates a canvas where VLC will paint the video.
   * @return
   */
  private Canvas createCanvas() {
    Canvas jCanvas = new Canvas();
    jCanvas.setSize(500, 500);
    jCanvas.setBackground(Color.BLACK);
    return jCanvas;
  }
}
