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

package com.webreach.mirth.connectors.sftp;

import java.util.Properties;

import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import com.webreach.mirth.connectors.ConnectorClass;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class SFTPReader extends ConnectorClass
{
    /** Creates new form FTPReader */

    public SFTPReader()
    {
        name = SFTPReaderProperties.name;
        initComponents();
        pollingFrequencyField.setDocument(new MirthFieldConstraints(0, false, false, true));
        fileAge.setDocument(new MirthFieldConstraints(0, false, false, true));
        // ast:encoding activation
        parent.setupCharsetEncodingForChannel(charsetEncodingCombobox);
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(SFTPReaderProperties.DATATYPE, name);
        properties.put(SFTPReaderProperties.FTP_HOST, FTPURLField.getText());

        properties.put(SFTPReaderProperties.FTP_USERNAME, FTPUsernameField.getText());
        properties.put(SFTPReaderProperties.FTP_PASSWORD, new String(FTPPasswordField.getPassword()));
        properties.put(SFTPReaderProperties.FTP_POLLING_FREQUENCY, pollingFrequencyField.getText());
        
        //common file properties
        properties.put(SFTPReaderProperties.FILE_MOVE_TO_PATTERN, moveToPattern.getText());
        properties.put(SFTPReaderProperties.FILE_MOVE_TO_DIRECTORY, moveToDirectory.getText().replace('\\', '/'));

        if (deleteAfterReadYes.isSelected())
            properties.put(SFTPReaderProperties.FILE_DELETE_AFTER_READ, UIConstants.YES_OPTION);
        else
            properties.put(SFTPReaderProperties.FILE_DELETE_AFTER_READ, UIConstants.NO_OPTION);

        if (checkFileAgeYes.isSelected())
            properties.put(SFTPReaderProperties.FILE_CHECK_FILE_AGE, UIConstants.YES_OPTION);
        else
            properties.put(SFTPReaderProperties.FILE_CHECK_FILE_AGE, UIConstants.NO_OPTION);

        properties.put(SFTPReaderProperties.FILE_FILE_AGE, fileAge.getText());

        if (((String) sortBy.getSelectedItem()).equals("Name"))
            properties.put(SFTPReaderProperties.FILE_SORT_BY, SFTPReaderProperties.SORT_BY_NAME);
        else if (((String) sortBy.getSelectedItem()).equals("Size"))
            properties.put(SFTPReaderProperties.FILE_SORT_BY, SFTPReaderProperties.SORT_BY_SIZE);
        else if (((String) sortBy.getSelectedItem()).equals("Date"))
            properties.put(SFTPReaderProperties.FILE_SORT_BY, SFTPReaderProperties.SORT_BY_DATE);
        // ast:encoding
        properties.put(SFTPReaderProperties.CONNECTOR_CHARSET_ENCODING, parent.getSelectedEncodingForChannel(charsetEncodingCombobox));
        properties.put(SFTPReaderProperties.FILE_FILTER, fileNameFilter.getText());

        if (processBatchFilesYes.isSelected())
            properties.put(SFTPReaderProperties.FILE_PROCESS_BATCH_FILES, UIConstants.YES_OPTION);
        else
            properties.put(SFTPReaderProperties.FILE_PROCESS_BATCH_FILES, UIConstants.NO_OPTION);

        if (fileTypeBinary.isSelected())
            properties.put(SFTPReaderProperties.FILE_TYPE, UIConstants.YES_OPTION);
        else
            properties.put(SFTPReaderProperties.FILE_TYPE, UIConstants.NO_OPTION);
       
        return properties;
    }

    public void setProperties(Properties props)
    {
        resetInvalidProperties();
        
        FTPURLField.setText((String) props.get(SFTPReaderProperties.FTP_HOST));

        FTPUsernameField.setText((String) props.get(SFTPReaderProperties.FTP_USERNAME));
        FTPPasswordField.setText((String) props.get(SFTPReaderProperties.FTP_PASSWORD));
        pollingFrequencyField.setText((String) props.get(SFTPReaderProperties.FTP_POLLING_FREQUENCY));
        
        //common file properties
        moveToPattern.setText((String) props.get(SFTPReaderProperties.FILE_MOVE_TO_PATTERN));
        moveToDirectory.setText((String) props.get(SFTPReaderProperties.FILE_MOVE_TO_DIRECTORY));
        if (((String) props.get(SFTPReaderProperties.FILE_DELETE_AFTER_READ)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            deleteAfterReadYes.setSelected(true);
            deleteAfterReadYesActionPerformed(null);
        }
        else
        {
            deleteAfterReadNo.setSelected(true);
            deleteAfterReadNoActionPerformed(null);
        }
        if (((String) props.get(SFTPReaderProperties.FILE_CHECK_FILE_AGE)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            checkFileAgeYes.setSelected(true);
            checkFileAgeYesActionPerformed(null);
        }
        else
        {
            checkFileAgeNo.setSelected(true);
            checkFileAgeNoActionPerformed(null);
        }

        fileAge.setText((String) props.get(SFTPReaderProperties.FILE_FILE_AGE));

        if (props.get(SFTPReaderProperties.FILE_SORT_BY).equals(SFTPReaderProperties.SORT_BY_NAME))
            sortBy.setSelectedItem("Name");
        else if (props.get(SFTPReaderProperties.FILE_SORT_BY).equals(SFTPReaderProperties.SORT_BY_SIZE))
            sortBy.setSelectedItem("Size");
        else if (props.get(SFTPReaderProperties.FILE_SORT_BY).equals(SFTPReaderProperties.SORT_BY_DATE))
            sortBy.setSelectedItem("Date");
        // ast:encoding
        parent.sePreviousSelectedEncodingForChannel(charsetEncodingCombobox, (String) props.get(SFTPReaderProperties.CONNECTOR_CHARSET_ENCODING));
        fileNameFilter.setText((String) props.get(SFTPReaderProperties.FILE_FILTER));

        if (((String) props.get(SFTPReaderProperties.FILE_PROCESS_BATCH_FILES)).equalsIgnoreCase(UIConstants.YES_OPTION))
            processBatchFilesYes.setSelected(true);
        else
            processBatchFilesNo.setSelected(true);

        if (((String) props.get(SFTPReaderProperties.FILE_TYPE)).equalsIgnoreCase(UIConstants.YES_OPTION))
            fileTypeBinary.setSelected(true);
        else
            fileTypeASCII.setSelected(true);
    }

    public Properties getDefaults()
    {
        return SFTPReaderProperties.getDefaults();
    }

    public boolean checkProperties(Properties props)
    {
        resetInvalidProperties();
        boolean valid = true;
        
        if (((String) props.get(SFTPReaderProperties.FTP_HOST)).length() == 0)
        {
            valid = false;
            FTPURLField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(SFTPReaderProperties.FILE_FILTER)).length() == 0)
        {
            valid = false;
            fileNameFilter.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(SFTPReaderProperties.FTP_POLLING_FREQUENCY)).length() == 0)
        {
            valid = false;
            pollingFrequencyField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(SFTPReaderProperties.FTP_USERNAME)).length() == 0)
        {
            valid = false;
            FTPUsernameField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(SFTPReaderProperties.FTP_PASSWORD)).length() == 0)
        {
            valid = false;
            FTPPasswordField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(SFTPReaderProperties.FILE_CHECK_FILE_AGE)).equals(UIConstants.YES_OPTION))
        {
            if (((String) props.get(SFTPReaderProperties.FILE_FILE_AGE)).length() == 0)
            {
                valid = false;
                fileAge.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        
        return valid;
    }
    
    private void resetInvalidProperties()
    {
        FTPURLField.setBackground(null);
        fileNameFilter.setBackground(null);
        pollingFrequencyField.setBackground(null);
        FTPUsernameField.setBackground(null);
        FTPPasswordField.setBackground(null);
        fileAge.setBackground(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        URL = new javax.swing.JLabel();
        FTPURLField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel7 = new javax.swing.JLabel();
        fileTypeBinary = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileTypeASCII = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        FTPUsernameLabel = new javax.swing.JLabel();
        FTPUsernameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        FTPPasswordField = new com.webreach.mirth.client.ui.components.MirthPasswordField();
        FTPPasswordLabel = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        pollingFrequencyField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel8 = new javax.swing.JLabel();
        fileNameFilter = new com.webreach.mirth.client.ui.components.MirthTextField();
        moveToDirectoryLabel = new javax.swing.JLabel();
        moveToFileLabel = new javax.swing.JLabel();
        moveToPattern = new com.webreach.mirth.client.ui.components.MirthTextField();
        moveToDirectory = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel11 = new javax.swing.JLabel();
        deleteAfterReadYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        deleteAfterReadNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        checkFileAgeNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        checkFileAgeYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel12 = new javax.swing.JLabel();
        fileAgeLabel = new javax.swing.JLabel();
        fileAge = new com.webreach.mirth.client.ui.components.MirthTextField();
        processBatchFilesNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        processBatchFilesYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel14 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        sortBy = new com.webreach.mirth.client.ui.components.MirthComboBox();
        charsetEncodingCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel41 = new javax.swing.JLabel();
        mirthVariableList1 = new com.webreach.mirth.client.ui.components.MirthVariableList();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setMinimumSize(new java.awt.Dimension(200, 200));
        URL.setText("Host:");

        jLabel7.setText("File Type:");

        fileTypeBinary.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeBinary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(fileTypeBinary);
        fileTypeBinary.setText("Binary");
        fileTypeBinary.setMargin(new java.awt.Insets(0, 0, 0, 0));

        fileTypeASCII.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeASCII.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(fileTypeASCII);
        fileTypeASCII.setSelected(true);
        fileTypeASCII.setText("ASCII");
        fileTypeASCII.setMargin(new java.awt.Insets(0, 0, 0, 0));

        FTPUsernameLabel.setText("Username:");

        FTPPasswordField.setFont(new java.awt.Font("Tahoma", 0, 11));

        FTPPasswordLabel.setText("Password:");

        jLabel9.setText("Polling Frequency (ms):");

        jLabel8.setText("Filename Filter Pattern:");

        moveToDirectoryLabel.setText("Move-to Directory:");

        moveToFileLabel.setText("Move-to File Name:");

        jLabel11.setText("Delete File After Read:");

        deleteAfterReadYes.setBackground(new java.awt.Color(255, 255, 255));
        deleteAfterReadYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(deleteAfterReadYes);
        deleteAfterReadYes.setText("Yes");
        deleteAfterReadYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        deleteAfterReadYes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteAfterReadYesActionPerformed(evt);
            }
        });

        deleteAfterReadNo.setBackground(new java.awt.Color(255, 255, 255));
        deleteAfterReadNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(deleteAfterReadNo);
        deleteAfterReadNo.setSelected(true);
        deleteAfterReadNo.setText("No");
        deleteAfterReadNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        deleteAfterReadNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteAfterReadNoActionPerformed(evt);
            }
        });

        checkFileAgeNo.setBackground(new java.awt.Color(255, 255, 255));
        checkFileAgeNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(checkFileAgeNo);
        checkFileAgeNo.setSelected(true);
        checkFileAgeNo.setText("No");
        checkFileAgeNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkFileAgeNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                checkFileAgeNoActionPerformed(evt);
            }
        });

        checkFileAgeYes.setBackground(new java.awt.Color(255, 255, 255));
        checkFileAgeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(checkFileAgeYes);
        checkFileAgeYes.setText("Yes");
        checkFileAgeYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkFileAgeYes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                checkFileAgeYesActionPerformed(evt);
            }
        });

        jLabel12.setText("Check File Age:");

        fileAgeLabel.setText("File Age (ms):");

        processBatchFilesNo.setBackground(new java.awt.Color(255, 255, 255));
        processBatchFilesNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(processBatchFilesNo);
        processBatchFilesNo.setSelected(true);
        processBatchFilesNo.setText("No");
        processBatchFilesNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        processBatchFilesNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                processBatchFilesNoActionPerformed(evt);
            }
        });

        processBatchFilesYes.setBackground(new java.awt.Color(255, 255, 255));
        processBatchFilesYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(processBatchFilesYes);
        processBatchFilesYes.setText("Yes");
        processBatchFilesYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel14.setText("Process Batch Files:");

        jLabel3.setText("Sort Files By:");

        sortBy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Date", "Name", "Size" }));

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "UTF-8", "ISO-8859-1", "UTF-16 (le)", "UTF-16 (be)", "UTF-16 (bom)", "US-ASCII" }));
        charsetEncodingCombobox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                charsetEncodingComboboxActionPerformed(evt);
            }
        });

        jLabel41.setText("Encoding:");

        mirthVariableList1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        mirthVariableList1.setModel(new javax.swing.AbstractListModel()
        {
            String[] strings = { "DATE", "COUNT", "UUID", "SYSTIME", "ORIGINALNAME" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, URL)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, FTPUsernameLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, FTPPasswordLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel8)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel9)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, moveToDirectoryLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, moveToFileLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel11)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel12)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, fileAgeLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel7)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel14)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel41))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(FTPURLField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(FTPUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(FTPPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fileNameFilter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pollingFrequencyField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(moveToDirectory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(moveToPattern, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(deleteAfterReadYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deleteAfterReadNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(checkFileAgeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(checkFileAgeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(fileAge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(fileTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fileTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(processBatchFilesYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(processBatchFilesNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(sortBy, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5)
                .add(mirthVariableList1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel8)
                            .add(fileNameFilter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel9)
                            .add(pollingFrequencyField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(moveToDirectoryLabel)
                            .add(moveToDirectory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(moveToFileLabel)
                            .add(moveToPattern, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel11)
                            .add(deleteAfterReadYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(deleteAfterReadNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel12)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(checkFileAgeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(checkFileAgeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(fileAge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(fileAgeLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(fileTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(fileTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel7))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel14)
                            .add(processBatchFilesYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(processBatchFilesNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(sortBy, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel41)))
                    .add(layout.createSequentialGroup()
                        .add(49, 49, 49)
                        .add(mirthVariableList1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void deleteAfterReadYesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteAfterReadYesActionPerformed
    {//GEN-HEADEREND:event_deleteAfterReadYesActionPerformed
        moveToDirectory.setEnabled(false);
        moveToPattern.setEnabled(false);
        
        moveToDirectoryLabel.setEnabled(false);
        moveToFileLabel.setEnabled(false);
        
        moveToDirectory.setText("");
        moveToPattern.setText("");
    }//GEN-LAST:event_deleteAfterReadYesActionPerformed

    private void deleteAfterReadNoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteAfterReadNoActionPerformed
    {//GEN-HEADEREND:event_deleteAfterReadNoActionPerformed
        moveToDirectory.setEnabled(true);
        moveToPattern.setEnabled(true);
        
        moveToDirectoryLabel.setEnabled(true);
        moveToFileLabel.setEnabled(true);
    }//GEN-LAST:event_deleteAfterReadNoActionPerformed

    private void charsetEncodingComboboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_charsetEncodingComboboxActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_charsetEncodingComboboxActionPerformed

    private void processBatchFilesNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processBatchFilesNoActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_processBatchFilesNoActionPerformed

    private void checkFileAgeYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkFileAgeYesActionPerformed
        fileAgeLabel.setEnabled(true);
        fileAge.setEnabled(true);
    }//GEN-LAST:event_checkFileAgeYesActionPerformed

    private void checkFileAgeNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkFileAgeNoActionPerformed
        fileAgeLabel.setEnabled(false);
        fileAge.setEnabled(false);
    }//GEN-LAST:event_checkFileAgeNoActionPerformed

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
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.ButtonGroup buttonGroup7;
    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.webreach.mirth.client.ui.components.MirthRadioButton checkFileAgeNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton checkFileAgeYes;
    private com.webreach.mirth.client.ui.components.MirthRadioButton deleteAfterReadNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton deleteAfterReadYes;
    private com.webreach.mirth.client.ui.components.MirthTextField fileAge;
    private javax.swing.JLabel fileAgeLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField fileNameFilter;
    private com.webreach.mirth.client.ui.components.MirthRadioButton fileTypeASCII;
    private com.webreach.mirth.client.ui.components.MirthRadioButton fileTypeBinary;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private com.webreach.mirth.client.ui.components.MirthVariableList mirthVariableList1;
    private com.webreach.mirth.client.ui.components.MirthTextField moveToDirectory;
    private javax.swing.JLabel moveToDirectoryLabel;
    private javax.swing.JLabel moveToFileLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField moveToPattern;
    private com.webreach.mirth.client.ui.components.MirthTextField pollingFrequencyField;
    private com.webreach.mirth.client.ui.components.MirthRadioButton processBatchFilesNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton processBatchFilesYes;
    private com.webreach.mirth.client.ui.components.MirthComboBox sortBy;
    // End of variables declaration//GEN-END:variables

}
