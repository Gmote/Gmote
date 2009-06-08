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

package org.gmote.server.updater;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * Displays a status bar and label indicating the current progress.
 * 
 * @author Marc Stogaitis
 */
public class ProgressDialog extends JPanel implements PropertyChangeListener {

  private static final long serialVersionUID = 1L;
  private static ProgressDialog progressDialogInstance = new ProgressDialog();
  private JProgressBar progressBar;
  JLabel statusLabel;
  
  public ProgressDialog() {
    super(new BorderLayout());

    progressBar = new JProgressBar(0, 100);
    progressBar.setValue(0);
    progressBar.setStringPainted(true);

    statusLabel = new JLabel("Gmote Updater                                                ");
    
    
    JPanel panel = new JPanel(new GridLayout(2,1));
    panel.add(statusLabel);
    panel.add(progressBar);
    add(panel, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

  }

  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equalsIgnoreCase("progress")) {
      int progress = (Integer) evt.getNewValue();
      progressBar.setValue(progress);
    } else if (evt.getPropertyName().equalsIgnoreCase("changeprogressbarsize")) {
      int max = (Integer) evt.getNewValue();
      progressBar.setMaximum(max);
    } else if (evt.getPropertyName().equalsIgnoreCase("updatestatuslabel")) {
      String text = (String)evt.getNewValue();
      statusLabel.setText(text);
    }
  }
  
  private static void makeFrame() {
      // Make the frame.
      JFrame frame = new JFrame("Gmote Updater");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      JComponent component = getInstance();
      component.setOpaque(true); 
      frame.setContentPane(component);

      Toolkit toolkit = Toolkit.getDefaultToolkit(); 
      Dimension screenSize = toolkit.getScreenSize();
            
      frame.pack();
      
      // Center the frame.
      int x = (screenSize.width - 200) / 2; 
      int y = (screenSize.height - 200) / 2; 
      // Set the new frame location 
      frame.setLocation(x, y); 
      
      frame.setVisible(true);
  }

  public static ProgressDialog getInstance() {
    return progressDialogInstance ;
  }


  public static void showProgressDialog() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
          makeFrame();
      }
  });
  }
  
  

}
