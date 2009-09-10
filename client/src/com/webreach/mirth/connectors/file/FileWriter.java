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

package com.webreach.mirth.connectors.file;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.util.ConnectionTestResponse;
import org.apache.log4j.Logger;
import org.jdesktop.swingworker.SwingWorker;

import java.util.Properties;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class FileWriter extends ConnectorClass
{
    private Logger logger = Logger.getLogger(this.getClass());

    /** Creates new form FileWriter */

    public FileWriter()
    {
        name = FileWriterProperties.name;
        initComponents();
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(FileWriterProperties.DATATYPE, name);
        
        if (((String) schemeComboBox.getSelectedItem()).equals("file"))
            properties.put(FileWriterProperties.FILE_SCHEME, FileWriterProperties.SCHEME_FILE);
        else if (((String) schemeComboBox.getSelectedItem()).equals("ftp"))
            properties.put(FileWriterProperties.FILE_SCHEME, FileWriterProperties.SCHEME_FTP);
        else if (((String) schemeComboBox.getSelectedItem()).equals("sftp"))
            properties.put(FileWriterProperties.FILE_SCHEME, FileWriterProperties.SCHEME_SFTP);
        else if (((String) schemeComboBox.getSelectedItem()).equals("smb"))
            properties.put(FileWriterProperties.FILE_SCHEME, FileWriterProperties.SCHEME_SMB);
        else if (((String) schemeComboBox.getSelectedItem()).equals("webdav"))
            properties.put(FileReaderProperties.FILE_SCHEME, FileWriterProperties.SCHEME_WEBDAV);
	    else {
           	// This "can't happen"
            logger.error("Unrecognized this.schemeComboBox value '" + schemeComboBox.getSelectedItem() + "', using 'file' instead");
            properties.put(FileWriterProperties.FILE_SCHEME, FileWriterProperties.SCHEME_FILE);
        }

        if (schemeComboBox.getSelectedItem().equals("file")) {
            properties.put(FileReaderProperties.FILE_HOST, directoryField.getText().replace('\\', '/'));
        }
        else {
            properties.put(FileWriterProperties.FILE_HOST, hostField.getText() + "/" + pathField.getText());
        }
        
        properties.put(FileWriterProperties.FILE_NAME, fileNameField.getText());

        if (anonymousYes.isSelected()) {
            properties.put(FileWriterProperties.FILE_ANONYMOUS, UIConstants.YES_OPTION);
			if (((String) schemeComboBox.getSelectedItem()).equals(FileWriterProperties.SCHEME_WEBDAV)) {
				properties.put(FileWriterProperties.FILE_USERNAME, "null");
				properties.put(FileWriterProperties.FILE_PASSWORD, "null");
			}
        }
        else
            properties.put(FileWriterProperties.FILE_ANONYMOUS, UIConstants.NO_OPTION);

        properties.put(FileWriterProperties.FILE_USERNAME, usernameField.getText());
        properties.put(FileWriterProperties.FILE_PASSWORD, new String(passwordField.getPassword()));

        if (secureModeYes.isSelected())
            properties.put(FileWriterProperties.FILE_SECURE_MODE, UIConstants.YES_OPTION);
        else
            properties.put(FileWriterProperties.FILE_SECURE_MODE, UIConstants.NO_OPTION);

        if (passiveModeYes.isSelected())
            properties.put(FileWriterProperties.FILE_PASSIVE_MODE, UIConstants.YES_OPTION);
        else
            properties.put(FileWriterProperties.FILE_PASSIVE_MODE, UIConstants.NO_OPTION);

        if (validateConnectionYes.isSelected())
            properties.put(FileWriterProperties.FILE_VALIDATE_CONNECTION, UIConstants.YES_OPTION);
        else
            properties.put(FileWriterProperties.FILE_VALIDATE_CONNECTION, UIConstants.NO_OPTION);

        if (appendToFileYes.isSelected())
            properties.put(FileWriterProperties.FILE_APPEND, UIConstants.YES_OPTION);
        else
            properties.put(FileWriterProperties.FILE_APPEND, UIConstants.NO_OPTION);

        properties.put(FileWriterProperties.FILE_CONTENTS, fileContentsTextPane.getText());

        properties.put(FileWriterProperties.CONNECTOR_CHARSET_ENCODING, parent.getSelectedEncodingForConnector(charsetEncodingCombobox));

        if (fileTypeBinary.isSelected())
            properties.put(FileWriterProperties.FILE_TYPE, UIConstants.YES_OPTION);
        else
            properties.put(FileWriterProperties.FILE_TYPE, UIConstants.NO_OPTION);
        
    	logger.debug("getProperties: properties=" + properties);

        return properties;
    }

    /** Parses the scheme and URL to determine the values for the
     * directory, host and path fields, optionally storing them to
     * the fields, highlighting field errors, or just testing for
     * valid values.
     * 
     * @param props The connector properties from which to take the
     * values.
     * @param store If true, the parsed values are stored to the
     * corresponding form controls.
     * @param highlight If true, fields for which the parsed values
     * are invalid are highlighted.
     */ 
    public boolean setDirHostPath(Properties props, boolean store, boolean highlight) {
    	
    	boolean valid = true;
        Object schemeValue = props.get(FileWriterProperties.FILE_SCHEME);
    	String hostPropValue = (String) props.get(FileWriterProperties.FILE_HOST);
    	String directoryValue = "";
    	String hostValue = "";
    	String pathValue = "";
        if (schemeValue.equals(FileWriterProperties.SCHEME_FILE)) {
        	
        	directoryValue = hostPropValue;
        	if (directoryValue.length() <= 0) {
        		if (highlight) {
        			directoryField.setBackground(UIConstants.INVALID_COLOR);
        		}
        		valid = false;
        	}
        }
        else {
        	
            int splitIndex = hostPropValue.indexOf('/');
            if (splitIndex != -1)
            {
            	hostValue = hostPropValue.substring(0, splitIndex);
            	pathValue = hostPropValue.substring(splitIndex + 1);
            }
            else
            {
            	hostValue = hostPropValue;
            }
            
        	if (hostValue.length() <= 0) {
        		if (highlight) {
        			hostField.setBackground(UIConstants.INVALID_COLOR);
        		}
        		valid = false;
        	}
        }
        
        if (store) {
        	
        	directoryField.setText(directoryValue);
            hostField.setText(hostValue);
            pathField.setText(pathValue);
        }
        
        return valid;
    }

    public void setProperties(Properties props)
    {
    	logger.debug("setProperties: props=" + props);

        resetInvalidProperties();

        Object schemeValue = props.get(FileWriterProperties.FILE_SCHEME);
        if (schemeValue.equals(FileWriterProperties.SCHEME_FILE))
            schemeComboBox.setSelectedItem("file");
        else if (schemeValue.equals(FileWriterProperties.SCHEME_FTP))
            schemeComboBox.setSelectedItem("ftp");
        else if (schemeValue.equals(FileWriterProperties.SCHEME_SFTP))
            schemeComboBox.setSelectedItem("sftp");
        else if (schemeValue.equals(FileWriterProperties.SCHEME_SMB))
            schemeComboBox.setSelectedItem("smb");
        else if (schemeValue.equals(FileWriterProperties.SCHEME_WEBDAV))
            schemeComboBox.setSelectedItem("webdav");
	    else {
           	// This "can't happen"
            logger.error("Unrecognized props[\"scheme\"] value '" + schemeValue + "', using 'file' instead");
            schemeComboBox.setSelectedItem("file");
        }

        schemeComboBoxActionPerformed(null);
        
        setDirHostPath(props, true, false);
        
        fileNameField.setText((String) props.get(FileWriterProperties.FILE_NAME));

        if (((String) props.get(FileWriterProperties.FILE_ANONYMOUS)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            anonymousYes.setSelected(true);
            anonymousNo.setSelected(false);
            anonymousYesActionPerformed(null);
        }
        else
        {
            anonymousYes.setSelected(false);
            anonymousNo.setSelected(true);
            anonymousNoActionPerformed(null);
            usernameField.setText((String) props.get(FileWriterProperties.FILE_USERNAME));
            passwordField.setText((String) props.get(FileWriterProperties.FILE_PASSWORD));
        }

        if (((String) props.get(FileWriterProperties.FILE_SECURE_MODE)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            secureModeYes.setSelected(true);
            secureModeNo.setSelected(false);
	        if (schemeValue.equals(FileWriterProperties.SCHEME_WEBDAV)) {
	            hostLabel.setText("https://");
	        }
        }
        else {
            secureModeYes.setSelected(false);
            secureModeNo.setSelected(true);
	        if (schemeValue.equals(FileWriterProperties.SCHEME_WEBDAV)) {
	            hostLabel.setText("http://");
	        }
        }

        if (((String) props.get(FileWriterProperties.FILE_PASSIVE_MODE)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            passiveModeYes.setSelected(true);
            passiveModeNo.setSelected(false);
        }
        else {
            passiveModeYes.setSelected(false);
            passiveModeNo.setSelected(true);
        }

        if (((String) props.get(FileWriterProperties.FILE_VALIDATE_CONNECTION)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            validateConnectionYes.setSelected(true);
            validateConnectionNo.setSelected(false);
        }
        else {
            validateConnectionYes.setSelected(false);
            validateConnectionNo.setSelected(true);
        }

        if (((String) props.get(FileWriterProperties.FILE_APPEND)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            appendToFileYes.setSelected(true);
            appendToFileNo.setSelected(false);
        }
        else {
            appendToFileYes.setSelected(false);
            appendToFileNo.setSelected(true);
        }

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, (String) props.get(FileWriterProperties.CONNECTOR_CHARSET_ENCODING));

        fileContentsTextPane.setText((String) props.get(FileWriterProperties.FILE_CONTENTS));

        if (((String) props.get(FileWriterProperties.FILE_TYPE)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            fileTypeBinary.setSelected(true);
            fileTypeASCII.setSelected(false);
            fileTypeBinaryActionPerformed(null);
        }
        else
        {
            fileTypeBinary.setSelected(false);
            fileTypeASCII.setSelected(true);
            fileTypeASCIIActionPerformed(null);
        }
    }

    public Properties getDefaults()
    {
        return new FileWriterProperties().getDefaults();
    }

    public boolean checkProperties(Properties props, boolean highlight)
    {
        resetInvalidProperties();
        boolean valid = true;

        valid = setDirHostPath(props, false, highlight);
        if (((String) props.get(FileWriterProperties.FILE_NAME)).length() == 0)
        {
            valid = false;
            if (highlight)
            	fileNameField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(FileWriterProperties.FILE_CONTENTS)).length() == 0)
        {
            valid = false;
            if (highlight)
            	fileContentsTextPane.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(FileWriterProperties.FILE_ANONYMOUS)).equals(UIConstants.NO_OPTION))
        {
            if (((String) props.get(FileWriterProperties.FILE_USERNAME)).length() == 0)
            {
                valid = false;
                if (highlight)
                	usernameField.setBackground(UIConstants.INVALID_COLOR);
            }
            if (((String) props.get(FileWriterProperties.FILE_PASSWORD)).length() == 0)
            {
                valid = false;
                if (highlight)
                	passwordField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    private void resetInvalidProperties()
    {
        directoryField.setBackground(null);
        hostField.setBackground(null);
        pathField.setBackground(null);
        fileNameField.setBackground(null);
        fileContentsTextPane.setBackground(null);
        usernameField.setBackground(null);
        passwordField.setBackground(null);
    }
    
    public String doValidate(Properties props, boolean highlight)
    {
    	String error = null;
    	
    	if (!checkProperties(props, highlight))
    		error = "Error in the form for connector \"" + getName() + "\".\n\n";
    	
    	return error;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        schemeLabel = new javax.swing.JLabel();
        schemeComboBox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        directoryLabel = new javax.swing.JLabel();
        directoryField = new com.webreach.mirth.client.ui.components.MirthTextField();
        hostLabel = new javax.swing.JLabel();
        hostField = new com.webreach.mirth.client.ui.components.MirthTextField();
        pathLabel = new javax.swing.JLabel();
        pathField = new com.webreach.mirth.client.ui.components.MirthTextField();
        fileNameLabel = new javax.swing.JLabel();
        fileNameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        anonymousLabel = new javax.swing.JLabel();
        anonymousYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        anonymousNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        usernameLabel = new javax.swing.JLabel();
        usernameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new com.webreach.mirth.client.ui.components.MirthPasswordField();
        secureModeLabel = new javax.swing.JLabel();
        secureModeYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        secureModeNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        validateConnectionLabel = new javax.swing.JLabel();
        validateConnectionYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        validateConnectionNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        appendToFileLabel = new javax.swing.JLabel();
        appendToFileYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        appendToFileNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileTypeLabel = new javax.swing.JLabel();
        fileTypeBinary = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileTypeASCII = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        charsetEncodingCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        encodingLabel = new javax.swing.JLabel();
        templateLabel = new javax.swing.JLabel();
        fileContentsTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea(false,false);
        testConnection = new javax.swing.JButton();
        passiveModeLabel = new javax.swing.JLabel();
        passiveModeYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        passiveModeNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        schemeLabel.setText("Method:");

        schemeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "file", "ftp", "sftp", "smb", "webdav" }));
        schemeComboBox.setToolTipText("The basic method used to access files to be written - file (local filesystem), FTP, SFTP, Samba share, or WebDAV.");
        schemeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                schemeComboBoxActionPerformed(evt);
            }
        });

        directoryLabel.setText("Directory:");

        directoryField.setToolTipText("The directory (folder) where the generated file should be written.");

        hostLabel.setText("ftp://");

        hostField.setToolTipText("The name or IP address of the host (computer) on which the files will be written.");

        pathLabel.setText("/");

        fileNameLabel.setText("File Name:");

        fileNameField.setToolTipText("The file name to give to the generated file.");

        anonymousLabel.setText("Anonymous:");

        anonymousYes.setBackground(new java.awt.Color(255, 255, 255));
        anonymousYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(anonymousYes);
        anonymousYes.setText("Yes");
        anonymousYes.setToolTipText("Connects to the file anonymously instead of using a username and password.");
        anonymousYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        anonymousYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anonymousYesActionPerformed(evt);
            }
        });

        anonymousNo.setBackground(new java.awt.Color(255, 255, 255));
        anonymousNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(anonymousNo);
        anonymousNo.setSelected(true);
        anonymousNo.setText("No");
        anonymousNo.setToolTipText("Connects to the file using a username and password instead of anonymously.");
        anonymousNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        anonymousNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anonymousNoActionPerformed(evt);
            }
        });

        usernameLabel.setText("Username:");

        usernameField.setToolTipText("The user name used to gain access to the server.");

        passwordLabel.setText("Password:");

        passwordField.setToolTipText("The password used to gain access to the server.");

        secureModeLabel.setText("Secure Mode:");

        secureModeYes.setBackground(new java.awt.Color(255, 255, 255));
        secureModeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(secureModeYes);
        secureModeYes.setText("Yes");
        secureModeYes.setToolTipText("Select Yes to connect to the server in \"passive mode\". Passive mode sometimes allows a connection through a firewall that normal mode does not.");
        secureModeYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        secureModeYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secureModeYesActionPerformed(evt);
            }
        });

        secureModeNo.setBackground(new java.awt.Color(255, 255, 255));
        secureModeNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(secureModeNo);
        secureModeNo.setSelected(true);
        secureModeNo.setText("No");
        secureModeNo.setToolTipText("Select Yes to connect to the server in \"normal mode\" as opposed to passive mode.");
        secureModeNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        secureModeNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secureModeNoActionPerformed(evt);
            }
        });

        validateConnectionLabel.setText("Validate Connection:");

        validateConnectionYes.setBackground(new java.awt.Color(255, 255, 255));
        validateConnectionYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(validateConnectionYes);
        validateConnectionYes.setText("Yes");
        validateConnectionYes.setToolTipText("Select Yes to test the connection to the server before each operation.");
        validateConnectionYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        validateConnectionNo.setBackground(new java.awt.Color(255, 255, 255));
        validateConnectionNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(validateConnectionNo);
        validateConnectionNo.setSelected(true);
        validateConnectionNo.setText("No");
        validateConnectionNo.setToolTipText("Select No to skip testing the connection to the server before each operation.");
        validateConnectionNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        appendToFileLabel.setText("Append to file:");

        appendToFileYes.setBackground(new java.awt.Color(255, 255, 255));
        appendToFileYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(appendToFileYes);
        appendToFileYes.setText("Yes");
        appendToFileYes.setToolTipText("<html>If Yes is selected, messages accepted by this destination will be appended to a single file specified in the File Name.<br>If No is selected, messages accepted by this destination will replace any existing file of the same name.<br>This feature will not work if a template is used for the File Name which generates a new file name when a message is processed.</html>");
        appendToFileYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        appendToFileNo.setBackground(new java.awt.Color(255, 255, 255));
        appendToFileNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(appendToFileNo);
        appendToFileNo.setSelected(true);
        appendToFileNo.setText("No");
        appendToFileNo.setToolTipText("<html>If Yes is selected, messages accepted by this destination will be appended to a single file specified in the File Name.<br>If No is selected, messages accepted by this destination will replace any existing file of the same name.<br>This feature will not work if a template is used for the File Name which generates a new file name when a message is processed.</html>");
        appendToFileNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        fileTypeLabel.setText("File Type:");

        fileTypeBinary.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeBinary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(fileTypeBinary);
        fileTypeBinary.setText("Binary");
        fileTypeBinary.setToolTipText("<html>If ASCII is selected, messages are written as text,<br> and the character set encoding used can be selected in the Encoding control below.<br>If Binary is selected, messages are written as binary byte streams.</html>");
        fileTypeBinary.setMargin(new java.awt.Insets(0, 0, 0, 0));

        fileTypeASCII.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeASCII.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(fileTypeASCII);
        fileTypeASCII.setSelected(true);
        fileTypeASCII.setText("ASCII");
        fileTypeASCII.setToolTipText("<html>If ASCII is selected, messages are written as text,<br> and the character set encoding used can be selected in the Encoding control below.<br>If Binary is selected, messages are written as binary byte streams.</html>");
        fileTypeASCII.setMargin(new java.awt.Insets(0, 0, 0, 0));

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));
        charsetEncodingCombobox.setToolTipText("<html>Select the character encoding system to use to write the files accepted by the destination connector.<br>Selecting Default uses the default character encoding for the JVM in which Mirth is running.<br>Selecting any other value selects the corresponding character encoding.</html>");

        encodingLabel.setText("Encoding:");

        templateLabel.setText("Template:");

        fileContentsTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        testConnection.setText("Test Write");
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        passiveModeLabel.setText("Passive Mode:");

        passiveModeYes.setBackground(new java.awt.Color(255, 255, 255));
        passiveModeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(passiveModeYes);
        passiveModeYes.setText("Yes");
        passiveModeYes.setToolTipText("Select Yes to connect to the server in \"passive mode\". Passive mode sometimes allows a connection through a firewall that normal mode does not.");
        passiveModeYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        passiveModeNo.setBackground(new java.awt.Color(255, 255, 255));
        passiveModeNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(passiveModeNo);
        passiveModeNo.setSelected(true);
        passiveModeNo.setText("No");
        passiveModeNo.setToolTipText("Select Yes to connect to the server in \"normal mode\" as opposed to passive mode.");
        passiveModeNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(templateLabel)
                    .add(encodingLabel)
                    .add(fileTypeLabel)
                    .add(anonymousLabel)
                    .add(fileNameLabel)
                    .add(hostLabel)
                    .add(directoryLabel)
                    .add(schemeLabel)
                    .add(secureModeLabel)
                    .add(passwordLabel)
                    .add(validateConnectionLabel)
                    .add(appendToFileLabel)
                    .add(usernameLabel)
                    .add(passiveModeLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(schemeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(testConnection))
                    .add(layout.createSequentialGroup()
                        .add(hostField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pathLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pathField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(fileNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(anonymousYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(anonymousNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(usernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(secureModeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(secureModeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(validateConnectionYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(validateConnectionNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(appendToFileYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(appendToFileNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(fileTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fileTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fileContentsTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                    .add(directoryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(passiveModeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(passiveModeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(schemeLabel)
                    .add(schemeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(testConnection))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(directoryLabel)
                    .add(directoryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hostLabel)
                    .add(hostField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pathLabel)
                    .add(pathField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fileNameLabel)
                    .add(fileNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(anonymousLabel)
                    .add(anonymousYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(anonymousNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(usernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(usernameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwordLabel)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(secureModeLabel)
                    .add(secureModeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(secureModeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passiveModeLabel)
                    .add(passiveModeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(passiveModeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(validateConnectionLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(validateConnectionYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(validateConnectionNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(appendToFileLabel)
                    .add(appendToFileYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(appendToFileNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fileTypeLabel)
                    .add(fileTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fileTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(encodingLabel)
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(templateLabel)
                    .add(fileContentsTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void anonymousNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anonymousNoActionPerformed
        usernameLabel.setEnabled(true);
        usernameField.setEnabled(true);
        
        passwordLabel.setEnabled(true);
        passwordField.setEnabled(true);
    }//GEN-LAST:event_anonymousNoActionPerformed

    private void anonymousYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anonymousYesActionPerformed
        usernameLabel.setEnabled(false);
        usernameField.setEnabled(false);
        usernameField.setText("anonymous");
        
        passwordLabel.setEnabled(false);
        passwordField.setEnabled(false);
        passwordField.setText("anonymous");
    }//GEN-LAST:event_anonymousYesActionPerformed

    private void onSchemeChange(boolean enableHost, boolean anonymous, boolean allowAppend, String scheme) {
        
        // act like the appropriate Anonymous button was selected.
        if (anonymous) {
            
            anonymousNo.setSelected(false);
            anonymousYes.setSelected(true);
            anonymousYesActionPerformed(null);
        }
        else {
            
            anonymousNo.setSelected(true);
            anonymousYes.setSelected(false);
            anonymousNoActionPerformed(null);
        }

        hostLabel.setEnabled(enableHost);
        hostField.setEnabled(enableHost);
        pathLabel.setEnabled(enableHost);
        pathField.setEnabled(enableHost);
        directoryLabel.setEnabled(!enableHost);
        directoryField.setEnabled(!enableHost);

        anonymousLabel.setEnabled(false);
        anonymousYes.setEnabled(false);
        anonymousNo.setEnabled(false);
		passiveModeLabel.setEnabled(false);
		passiveModeYes.setEnabled(false);
		passiveModeNo.setEnabled(false);
        secureModeLabel.setEnabled(false);
        secureModeYes.setEnabled(false);
        secureModeNo.setEnabled(false);
        validateConnectionLabel.setEnabled(false);
        validateConnectionYes.setEnabled(false);
        validateConnectionNo.setEnabled(false);
        
        if (allowAppend) {
        	
        	appendToFileNo.setEnabled(true); 
        	appendToFileYes.setEnabled(true); 
        	appendToFileLabel.setEnabled(true);
        }
        else {

        	if (appendToFileYes.isSelected()) {
	    		appendToFileNo.setSelected(true);
	        	appendToFileYes.setSelected(false);
        	}
        	
        	appendToFileNo.setEnabled(false); 
        	appendToFileYes.setEnabled(false); 
        	appendToFileLabel.setEnabled(false);
        }

	    if (scheme.equals(FileWriterProperties.SCHEME_FTP)) {

			anonymousLabel.setEnabled(true);
			anonymousYes.setEnabled(true);
			anonymousNo.setEnabled(true);
			passiveModeLabel.setEnabled(true);
			passiveModeYes.setEnabled(true);
			passiveModeNo.setEnabled(true);
			validateConnectionLabel.setEnabled(true);
			validateConnectionYes.setEnabled(true);
			validateConnectionNo.setEnabled(true);

	    } else if (scheme.equals(FileWriterProperties.SCHEME_WEBDAV)) {

			anonymousLabel.setEnabled(true);
			anonymousYes.setEnabled(true);
			anonymousNo.setEnabled(true);
			secureModeLabel.setEnabled(true);
			secureModeYes.setEnabled(true);
			secureModeNo.setEnabled(true);

		    // set Passive Mode and validate connection to No.
		    passiveModeNo.setSelected(true);
		    validateConnectionNo.setSelected(true);
		    
	    }
    }

    private void schemeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_schemeComboBoxActionPerformed
        String text = (String) schemeComboBox.getSelectedItem();
        
        // if File is selected
        if (text.equals(FileWriterProperties.SCHEME_FILE)) {
            
            onSchemeChange(false, true, true, FileWriterProperties.SCHEME_FTP);
        }
        // else if FTP is selected
        else if (text.equals(FileWriterProperties.SCHEME_FTP)) {
            
            onSchemeChange(true, anonymousYes.isSelected(), true, FileWriterProperties.SCHEME_FTP);
            hostLabel.setText("ftp://");
        }
        // else if SFTP is selected
        else if (text.equals(FileWriterProperties.SCHEME_SFTP)) {
            
            onSchemeChange(true, false, true, FileWriterProperties.SCHEME_SFTP);
            hostLabel.setText("sftp://");
        }
        // else if SMB is selected
        else if (text.equals(FileWriterProperties.SCHEME_SMB)) {
            
            onSchemeChange(true, false, true, FileWriterProperties.SCHEME_SMB);
            hostLabel.setText("smb://");
        }
        // else if WEBDAV is selected
        else if (text.equals(FileWriterProperties.SCHEME_WEBDAV)) {

            onSchemeChange(true, anonymousYes.isSelected(), false, FileWriterProperties.SCHEME_WEBDAV);
            if (secureModeYes.isSelected()) {
                hostLabel.setText("https://");
            } else {
                hostLabel.setText("http://");
            }
        }
    }//GEN-LAST:event_schemeComboBoxActionPerformed

private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
parent.setWorking("Testing connection...", true);

    SwingWorker worker = new SwingWorker<Void, Void>() {

        public Void doInBackground() {
            
            try {
                ConnectionTestResponse response = (ConnectionTestResponse) parent.mirthClient.invokeConnectorService(name, "testWrite", getProperties());
                
                if (response == null) {
                    throw new ClientException("Failed to invoke service.");
                } else if(response.getType().equals(ConnectionTestResponse.Type.SUCCESS)) { 
                    parent.alertInformation(parent, response.getMessage());
                } else { 
                    parent.alertWarning(parent, response.getMessage());
                }

                return null;
            } catch (ClientException e) {
                parent.alertError(parent, e.getMessage());
                return null;
            }
        }

        public void done() {
            parent.setWorking("", false);
        }
    };

    worker.execute();
}//GEN-LAST:event_testConnectionActionPerformed

private void secureModeYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secureModeYesActionPerformed
    // only WebDAV has access to here.
    // change host label to 'https://'
    hostLabel.setText("https://");
}//GEN-LAST:event_secureModeYesActionPerformed

private void secureModeNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secureModeNoActionPerformed
    // only WebDAV has access to here.
    // change host label to 'http://'
    hostLabel.setText("http://");
}//GEN-LAST:event_secureModeNoActionPerformed

    private void fileTypeASCIIActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_fileTypeASCIIActionPerformed
    {// GEN-HEADEREND:event_fileTypeASCIIActionPerformed
        encodingLabel.setEnabled(true);
        charsetEncodingCombobox.setEnabled(true);
    }// GEN-LAST:event_fileTypeASCIIActionPerformed

    private void fileTypeBinaryActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_fileTypeBinaryActionPerformed
    {// GEN-HEADEREND:event_fileTypeBinaryActionPerformed
        encodingLabel.setEnabled(false);
        charsetEncodingCombobox.setEnabled(false);
        charsetEncodingCombobox.setSelectedIndex(0);
    }// GEN-LAST:event_fileTypeBinaryActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel anonymousLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton anonymousNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton anonymousYes;
    private javax.swing.JLabel appendToFileLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton appendToFileNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton appendToFileYes;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.webreach.mirth.client.ui.components.MirthTextField directoryField;
    private javax.swing.JLabel directoryLabel;
    private javax.swing.JLabel encodingLabel;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea fileContentsTextPane;
    private com.webreach.mirth.client.ui.components.MirthTextField fileNameField;
    private javax.swing.JLabel fileNameLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton fileTypeASCII;
    private com.webreach.mirth.client.ui.components.MirthRadioButton fileTypeBinary;
    private javax.swing.JLabel fileTypeLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField hostField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JLabel passiveModeLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton passiveModeNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton passiveModeYes;
    private com.webreach.mirth.client.ui.components.MirthPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField pathField;
    private javax.swing.JLabel pathLabel;
    private com.webreach.mirth.client.ui.components.MirthComboBox schemeComboBox;
    private javax.swing.JLabel schemeLabel;
    private javax.swing.JLabel secureModeLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton secureModeNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton secureModeYes;
    private javax.swing.JLabel templateLabel;
    private javax.swing.JButton testConnection;
    private com.webreach.mirth.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JLabel validateConnectionLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton validateConnectionNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton validateConnectionYes;
    // End of variables declaration//GEN-END:variables

}
