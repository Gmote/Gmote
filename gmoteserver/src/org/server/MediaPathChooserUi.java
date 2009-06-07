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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import org.gmote.common.FileInfo;
import org.gmote.server.settings.BaseMediaPaths;


/**
 * Allows the user to choose which paths will appear by default on the first
 * screen of the file selector on the device.
 * 
 * @author Marc
 */
public class MediaPathChooserUi {
  //private static Logger LOGGER = Logger.getLogger(MediaPathChooserUi.class.getName());
	private JDialog jDialog = null; //@jve:decl-index=0:visual-constraint="120,25"
  private JPanel jPanel = null;
  private JTextArea txtDescription = null;
  private JPanel rightButtonPanel = null;
  private JButton cmdAddPath = null;
  private JButton cmdRemovePath = null;
  private JButton cmdClose = null;
  private JList lstPaths = null;
  DefaultListModel basePaths = new DefaultListModel();
	
	public MediaPathChooserUi() {
	  
  }

  /**
   * This method initializes jFrame
   * 
   * @return javax.swing.JFrame
   */
  public void showFrame() {

    loadBasePaths();

    if (jDialog == null) {
      jDialog = new JDialog((JFrame) null, true);
      jDialog.setSize(new Dimension(553, 371));
      jDialog.setLocation(new Point(100, 100));
      jDialog.setContentPane(getJPanel());
      jDialog.setTitle("Media Path Selector");
    }
    jDialog.setVisible(true);

  }

  /**
   * This method initializes jPanel
   * 
   * @return javax.swing.JPanel
   */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new BorderLayout());
			JPanel test = new JPanel();
			test.setBorder(BorderFactory.createLineBorder(Color.black));
			test.setLayout(new GridLayout());
			test.add(getTxtDescription());
			jPanel.add(test, BorderLayout.NORTH);
			jPanel.add(getRightButtonPanel(), BorderLayout.EAST);
			jPanel.add(getLstPaths(), BorderLayout.CENTER);
			
		}
		return jPanel;
	}
	/**
	 * This method initializes txtDescription	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getTxtDescription() {
		if (txtDescription == null) {
		  txtDescription = new JTextArea();
      txtDescription.setLineWrap(true);
      txtDescription.setWrapStyleWord(true);
      txtDescription.setEnabled(true);
      txtDescription.setEditable(false);
      txtDescription.setBackground(Color.DARK_GRAY);
      txtDescription.setForeground(Color.white);
      txtDescription.setText("Please identify the location of your movies and music. This will allow you to easily find the media you wish to run when using the remote.");
		}
		return txtDescription;
	}
	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getRightButtonPanel() {
		if (rightButtonPanel == null) {
			rightButtonPanel = new JPanel();
			BoxLayout boxLayout = new BoxLayout(rightButtonPanel, BoxLayout.Y_AXIS);
			
			rightButtonPanel.setLayout(boxLayout);
			rightButtonPanel.add(getCmdAddPath(), null);
			rightButtonPanel.add(getCmdRemovePath(), null);
			rightButtonPanel.add(getCmdClose(), null);
			
		}
		return rightButtonPanel;
	}
	/**
	 * This method initializes cmdAddPath	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdAddPath() {
		if (cmdAddPath == null) {
			cmdAddPath = new JButton();
			//TODO(mstogaitis): fix this so that it automatically takes up the width
			cmdAddPath.setText("    Add path    ");
			
			cmdAddPath.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          //Create a file chooser
          JFileChooser fc = new JFileChooser();
          fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          
          int returnVal = fc.showOpenDialog(getRightButtonPanel());
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            // Add the path to our list.
            BaseMediaPaths.getInstance().addPath(fc.getSelectedFile().getAbsolutePath());
            loadBasePaths();
          }
        }
      });
		}
		return cmdAddPath;
	}
	/**
	 * This method initializes cmdRemovePath	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdRemovePath() {
		if (cmdRemovePath == null) {
			cmdRemovePath = new JButton();
			cmdRemovePath.setText("Remove Path");
			cmdRemovePath.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          BaseMediaPaths.getInstance().removePath(lstPaths.getSelectedIndex());
          loadBasePaths();
        }
      });
		}
		return cmdRemovePath;
	}
	
	/**
   * This method initializes cmdClose  
   *  
   * @return javax.swing.JButton  
   */
  private JButton getCmdClose() {
    if (cmdClose == null) {
      cmdClose = new JButton();
      cmdClose.setText("       Done        ");
      cmdClose.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          jDialog.setVisible(false);
        }
      });
    }
    return cmdClose;
  }
	
	/**
	 * This method initializes lstPaths	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getLstPaths() {
		if (lstPaths == null) {
			lstPaths = new JList(basePaths);
			lstPaths.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      lstPaths.setSelectedIndex(0);
		}
		return lstPaths;
	}
  
  private void loadBasePaths() {
    basePaths.clear();
    List<FileInfo> paths = BaseMediaPaths.getInstance().getBasePaths();
    for (FileInfo path : paths) {
      basePaths.addElement(path.getAbsolutePath());
    }
  }
  
}
