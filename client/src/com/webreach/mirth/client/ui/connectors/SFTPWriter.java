/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.client.ui.connectors;

import java.awt.Color;
import java.util.Properties;

import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class SFTPWriter extends ConnectorClass
{
    /** Creates new form FTPReader */
    private final String DATATYPE = "DataType";

    private final String SFTP_ADDRESS = "host";

    private final String SFTP_USERNAME = "username";

    private final String SFTP_PASSWORD = "password";
    
    private final String SFTP_OUTPUT_PATTERN = "outputPattern";

    private final String SFTP_CONTENTS = "template";
    
    private final String SFTP_BINARY = "binary";

    public SFTPWriter()
    {
        name = "SFTP Writer";
        initComponents();
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(SFTP_ADDRESS, FTPURLField.getText());
        properties.put(SFTP_USERNAME, FTPUsernameField.getText());
        properties.put(SFTP_PASSWORD, new String(FTPPasswordField.getPassword()));
        properties.put(SFTP_OUTPUT_PATTERN, outputPatternField.getText());
        properties.put(SFTP_CONTENTS, ftpContentsTextPane.getText());
        if (fileTypeBinary.isSelected())
            properties.put(SFTP_BINARY, UIConstants.YES_OPTION);
        else
            properties.put(SFTP_BINARY, UIConstants.NO_OPTION);

        return properties;
    }

    public void setProperties(Properties props)
    {
        resetInvalidProperties();
        
        FTPURLField.setText((String) props.get(SFTP_ADDRESS));
        FTPUsernameField.setText((String) props.get(SFTP_USERNAME));
        FTPPasswordField.setText((String) props.get(SFTP_PASSWORD));
        outputPatternField.setText((String) props.get(SFTP_OUTPUT_PATTERN));
        ftpContentsTextPane.setText((String) props.get(SFTP_CONTENTS));
        if (((String) props.get(SFTP_BINARY)).equalsIgnoreCase(UIConstants.YES_OPTION))
            fileTypeBinary.setSelected(true);
        else
            fileTypeASCII.setSelected(true);
    }

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(SFTP_ADDRESS, "");
        properties.put(SFTP_USERNAME, "");
        properties.put(SFTP_PASSWORD, "");
        properties.put(SFTP_OUTPUT_PATTERN, "");
        properties.put(SFTP_CONTENTS, "");
        properties.put(SFTP_BINARY, UIConstants.NO_OPTION);
        return properties;
    }

    public boolean checkProperties(Properties props)
    {
        resetInvalidProperties();
        boolean valid = true;
        
        if (((String) props.get(SFTP_ADDRESS)).length() == 0)
        {
            valid = false;
            FTPURLField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(SFTP_OUTPUT_PATTERN)).length() == 0)
        {
            valid = false;
            outputPatternField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(SFTP_CONTENTS)).length() == 0)
        {
            valid = false;
            ftpContentsTextPane.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(SFTP_USERNAME)).length() == 0)
        {
            valid = false;
            FTPUsernameField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(SFTP_PASSWORD)).length() == 0)
        {
            valid = false;
            FTPPasswordField.setBackground(UIConstants.INVALID_COLOR);
        }
        
        return valid;
    }
    
    private void resetInvalidProperties()
    {
        FTPURLField.setBackground(null);
        outputPatternField.setBackground(null);
        ftpContentsTextPane.setBackground(null);
        FTPUsernameField.setBackground(null);
        FTPPasswordField.setBackground(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        URL = new javax.swing.JLabel();
        FTPURLField = new com.webreach.mirth.client.ui.components.MirthTextField();
        outputPatternField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel5 = new javax.swing.JLabel();
        FTPUsernameLabel = new javax.swing.JLabel();
        FTPUsernameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        FTPPasswordField = new com.webreach.mirth.client.ui.components.MirthPasswordField();
        FTPPasswordLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        fileTypeASCII = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileTypeBinary = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel7 = new javax.swing.JLabel();
        ftpContentsTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        URL.setText("Host:");

        jLabel5.setText("Filename:");

        FTPUsernameLabel.setText("Username:");

        FTPPasswordField.setFont(new java.awt.Font("Tahoma", 0, 11));

        FTPPasswordLabel.setText("Password:");

        jLabel3.setText("Template:");

        fileTypeASCII.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeASCII.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(fileTypeASCII);
        fileTypeASCII.setSelected(true);
        fileTypeASCII.setText("ASCII");
        fileTypeASCII.setMargin(new java.awt.Insets(0, 0, 0, 0));

        fileTypeBinary.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeBinary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(fileTypeBinary);
        fileTypeBinary.setText("Binary");
        fileTypeBinary.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel7.setText("File Type:");

        ftpContentsTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(FTPPasswordLabel)
                            .add(FTPUsernameLabel)
                            .add(URL))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(FTPUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(FTPURLField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(FTPPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(4, 4, 4)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel3)
                            .add(jLabel7)
                            .add(jLabel5))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(outputPatternField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(ftpContentsTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                                    .add(layout.createSequentialGroup()
                                        .add(fileTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(fileTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(URL)
                    .add(FTPURLField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(FTPUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(FTPUsernameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(FTPPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(FTPPasswordLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(outputPatternField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(fileTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fileTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(ftpContentsTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.components.MirthPasswordField FTPPasswordField;
    private javax.swing.JLabel FTPPasswordLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField FTPURLField;
    private com.webreach.mirth.client.ui.components.MirthTextField FTPUsernameField;
    private javax.swing.JLabel FTPUsernameLabel;
    private javax.swing.JLabel URL;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private com.webreach.mirth.client.ui.components.MirthRadioButton fileTypeASCII;
    private com.webreach.mirth.client.ui.components.MirthRadioButton fileTypeBinary;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea ftpContentsTextPane;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private com.webreach.mirth.client.ui.components.MirthTextField outputPatternField;
    // End of variables declaration//GEN-END:variables

}
