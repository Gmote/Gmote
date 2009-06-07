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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.UIManager;

public class PasswordSettingsUi {

  private JDialog jDialog = null; // @jve:decl-index=0:visual-constraint="216,44"
  private JDesktopPane jDesktopPane = null;
  private JLabel lblPassword = null;
  private JPasswordField jPasswordField = null;
  private JLabel lblPasswordConfirm = null;
  private JPasswordField jPasswordConfirm = null;
  private JButton cmdOk = null;
  private JButton cmdCancel = null;
  
  private boolean result = false;
  /**
   * This method initializes jFrame
   * 
   * @return true if the OK button was pressed.
   */
  public boolean showFrame() {

    ComponentAdapter adapter = new ComponentAdapter() {

      @Override
      public void componentShown(ComponentEvent e) {
        jPasswordField.requestFocusInWindow();
      }

    };

    jDialog = new PasswordDialog(null, true);
    jDialog.addComponentListener(adapter);
    jDialog.setSize(new Dimension(460, 166));
    jDialog.setTitle("Change Password");
    jDialog.setContentPane(getJDesktopPane());
    jDialog.getRootPane().setDefaultButton(getCmdOk());
    getJDesktopPane().setFocusable(true);
    getJDesktopPane().requestFocusInWindow();
    getJDesktopPane().setBackground(UIManager.getColor("window"));

    jDialog.setVisible(true);
      
    return result;
  }

  /**
   * This method initializes jDesktopPane
   * 
   * @return javax.swing.JDesktopPane
   */
  private JDesktopPane getJDesktopPane() {
    if (jDesktopPane == null) {
      lblPasswordConfirm = new JLabel();
      lblPasswordConfirm.setBounds(new Rectangle(15, 30, 290, 16));
      lblPasswordConfirm.setText("Create a new password for your server:");
      lblPassword = new JLabel();
      lblPassword.setBounds(new Rectangle(15, 60, 290, 15));
      lblPassword.setText("Please re-enter the password to confirm:");
      
      jDesktopPane = new JDesktopPane();
      jDesktopPane.setName("Settings");
      jDesktopPane.add(lblPassword, null);
      jDesktopPane.add(getJPasswordField(), null);
      jDesktopPane.add(lblPasswordConfirm, null);
      jDesktopPane.add(getJPasswordConfirm(), null);
      jDesktopPane.add(getCmdOk(), null);
      jDesktopPane.add(getCmdCancel(), null);
      
    }
    return jDesktopPane;
  }

  /**
   * This method initializes jPasswordField
   * 
   * @return javax.swing.JPasswordField
   */
  private JPasswordField getJPasswordField() {
    if (jPasswordField == null) {
      jPasswordField = new JPasswordField();
      jPasswordField.setLocation(new Point(290, 30));
      jPasswordField.setSize(new Dimension(158, 20));
    }
    return jPasswordField;
  }

  /**
   * This method initializes jPasswordConfirm
   * 
   * @return javax.swing.JPasswordField
   */
  private JPasswordField getJPasswordConfirm() {
    if (jPasswordConfirm == null) {
      jPasswordConfirm = new JPasswordField();
      jPasswordConfirm.setBounds(new Rectangle(290, 60, 158, 20));
    }
    return jPasswordConfirm;
  }

  /**
   * This method initializes cmdOk
   * 
   * @return javax.swing.JButton
   */
  private JButton getCmdOk() {
    if (cmdOk == null) {
      cmdOk = new JButton();
      cmdOk.setText("OK");
      cmdOk.setSize(new Dimension(85, 28));
      cmdOk.setLocation(new Point(135, 90));
      
      cmdOk.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          String password = new String(getJPasswordField().getPassword());
          String passwordConfirmation = new String(getJPasswordConfirm().getPassword());

          if (password.equals(passwordConfirmation)) {
            try {
              StringEncrypter.writePasswordToFile(password);
              result = true;
              jDialog.setVisible(false);
            } catch (EncryptionException e1) {
              JOptionPane.showMessageDialog(null, "Error: " + e1.getMessage());
            }
          } else {
            JOptionPane.showMessageDialog(null,
                "Oups, looks like the passwords are not the same. Please try again.");

          }
        }
      });
    }
    return cmdOk;
  }

  /**
   * This method initializes cmdCancel
   * 
   * @return javax.swing.JButton
   */
  private JButton getCmdCancel() {
    if (cmdCancel == null) {
      cmdCancel = new JButton();
      cmdCancel.setLocation(new Point(225, 90));
      cmdCancel.setText("Cancel");
      cmdCancel.setSize(new Dimension(85, 28));
      cmdCancel.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          result = false;
          jDialog.setVisible(false);
        }
      });
    }
    return cmdCancel;
  }
  
  public class PasswordDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    PasswordDialog(JFrame frame, boolean modal) {
      super(frame,modal);
    }
    
  }

}
