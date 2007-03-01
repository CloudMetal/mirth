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

import java.util.Properties;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;

/**
 * A form that extends from ConnectorClass.  All methods implemented
 * are described in ConnectorClass.
 */
public class FileReader extends ConnectorClass
{
    /** Creates new form FileWriter */
    private final String DATATYPE = "DataType";
    private final String FILE_DIRECTORY = "host";
    private final String FILE_POLLING_FREQUENCY = "pollingFrequency";
    private final String FILE_MOVE_TO_PATTERN = "moveToPattern";
    private final String FILE_MOVE_TO_DIRECTORY = "moveToDirectory";
    private final String FILE_DELETE_AFTER_READ = "autoDelete";
    private final String FILE_CHECK_FILE_AGE = "checkFileAge";
    private final String FILE_FILE_AGE = "fileAge";
    private final String FILE_SORT_BY = "sortAttribute";
    private final String FILE_PROCESS_BATCH_FILES = "processBatchFiles";
    private final String SORT_BY_NAME = "name";
    private final String SORT_BY_SIZE = "size";
    private final String SORT_BY_DATE = "date";
    private final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";
    private final String FILE_FILTER = "fileFilter";
    
    public FileReader()
    {
        name = "File Reader";
        initComponents();
        pollingFreq.setDocument(new MirthFieldConstraints(0, false, true));
        fileAge.setDocument(new MirthFieldConstraints(0, false, true));
        //ast:encoding activation
        parent.setupCharsetEncodingForChannel(charsetEncodingCombobox);
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(FILE_DIRECTORY, directoryField.getText().replace('\\', '/'));
        properties.put(FILE_POLLING_FREQUENCY, pollingFreq.getText());
        properties.put(FILE_MOVE_TO_PATTERN, moveToPattern.getText());
        properties.put(FILE_MOVE_TO_DIRECTORY, moveToDirectory.getText().replace('\\', '/'));

        if (deleteAfterReadYes.isSelected())
            properties.put(FILE_DELETE_AFTER_READ, UIConstants.YES_OPTION);
        else
            properties.put(FILE_DELETE_AFTER_READ, UIConstants.NO_OPTION);

        if (checkFileAgeYes.isSelected())
            properties.put(FILE_CHECK_FILE_AGE, UIConstants.YES_OPTION);
        else
            properties.put(FILE_CHECK_FILE_AGE, UIConstants.NO_OPTION);

        properties.put(FILE_FILE_AGE, fileAge.getText());
        
        if(((String)sortBy.getSelectedItem()).equals("Name"))
            properties.put(FILE_SORT_BY,SORT_BY_NAME);
        else if(((String)sortBy.getSelectedItem()).equals("Size"))
            properties.put(FILE_SORT_BY,SORT_BY_SIZE);
        else if(((String)sortBy.getSelectedItem()).equals("Date"))
            properties.put(FILE_SORT_BY,SORT_BY_DATE);
        //ast:encoding        
        properties.put(CONNECTOR_CHARSET_ENCODING,parent.
                        getSelectedEncodingForChannel(charsetEncodingCombobox));
        properties.put(FILE_FILTER, fileNameFilter.getText());
        
        if (processBatchFilesYes.isSelected())
            properties.put(FILE_PROCESS_BATCH_FILES, UIConstants.YES_OPTION);
        else
            properties.put(FILE_PROCESS_BATCH_FILES, UIConstants.NO_OPTION);

        return properties;
    }

    public void setProperties(Properties props)
    {
        directoryField.setText((String)props.get(FILE_DIRECTORY));
        pollingFreq.setText((String)props.get(FILE_POLLING_FREQUENCY));
        moveToPattern.setText((String)props.get(FILE_MOVE_TO_PATTERN));
        moveToDirectory.setText((String)props.get(FILE_MOVE_TO_DIRECTORY));
        if(((String)props.get(FILE_DELETE_AFTER_READ)).equalsIgnoreCase(UIConstants.YES_OPTION))
            deleteAfterReadYes.setSelected(true);
        else
            deleteAfterReadNo.setSelected(true);
        if(((String)props.get(FILE_CHECK_FILE_AGE)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            checkFileAgeYes.setSelected(true);
            checkFileAgeYesActionPerformed(null);
        }
        else
        {
            checkFileAgeNo.setSelected(true);
            checkFileAgeNoActionPerformed(null);
        }

        fileAge.setText((String)props.get(FILE_FILE_AGE));
        
        if(props.get(FILE_SORT_BY).equals(SORT_BY_NAME))
            sortBy.setSelectedItem("Name");
        else if(props.get(FILE_SORT_BY).equals(SORT_BY_SIZE))
            sortBy.setSelectedItem("Size");
        else if(props.get(FILE_SORT_BY).equals(SORT_BY_DATE))
            sortBy.setSelectedItem("Date");
        //ast:encoding        
        parent.sePreviousSelectedEncodingForChannel(charsetEncodingCombobox,
                        (String)props.get(CONNECTOR_CHARSET_ENCODING));
        fileNameFilter.setText((String)props.get(FILE_FILTER));
        
        if(((String)props.get(FILE_PROCESS_BATCH_FILES)).equalsIgnoreCase(UIConstants.YES_OPTION))
            processBatchFilesYes.setSelected(true);
        else
            processBatchFilesNo.setSelected(true);
    }
    
    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(FILE_DIRECTORY, "");
        properties.put(FILE_POLLING_FREQUENCY, "1000");
        properties.put(FILE_MOVE_TO_PATTERN, "");
        properties.put(FILE_MOVE_TO_DIRECTORY, "");
        properties.put(FILE_DELETE_AFTER_READ, UIConstants.NO_OPTION);
        properties.put(FILE_CHECK_FILE_AGE, UIConstants.NO_OPTION);
        properties.put(FILE_FILE_AGE, "0");
        properties.put(FILE_SORT_BY, SORT_BY_DATE);
        //ast: encoding
        properties.put(CONNECTOR_CHARSET_ENCODING, UIConstants.DEFAULT_ENCODING_OPTION);
        properties.put(FILE_FILTER, "*.*");
        properties.put(FILE_PROCESS_BATCH_FILES, UIConstants.YES_OPTION);
        return properties;
    }
    
    public boolean checkProperties(Properties props)
    {
        if(((String)props.get(FILE_DIRECTORY)).length() > 0 && ((String)props.get(FILE_POLLING_FREQUENCY)).length() > 0 && 
        ((String)props.get(FILE_FILE_AGE)).length() > 0)
            return true;
        return false;
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        directoryField = new com.webreach.mirth.client.ui.components.MirthTextField();
        pollingFreq = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel4 = new javax.swing.JLabel();
        moveToPattern = new com.webreach.mirth.client.ui.components.MirthTextField();
        moveToDirectory = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        fileAgeLabel = new javax.swing.JLabel();
        deleteAfterReadYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        deleteAfterReadNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        checkFileAgeYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        checkFileAgeNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileAge = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel3 = new javax.swing.JLabel();
        sortBy = new com.webreach.mirth.client.ui.components.MirthComboBox();
        charsetEncodingCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel41 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        mirthVariableList1 = new com.webreach.mirth.client.ui.components.MirthVariableList();
        jLabel8 = new javax.swing.JLabel();
        fileNameFilter = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel9 = new javax.swing.JLabel();
        processBatchFilesYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        processBatchFilesNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel1.setText("Directory to read:");

        jLabel2.setText("Polling Frequency (ms):");

        jLabel4.setText("Move-to File Name:");

        jLabel5.setText("Move-to Directory:");

        jLabel6.setText("Delete File After Read:");

        jLabel7.setText("Check File Age:");

        fileAgeLabel.setText("File Age (ms):");

        deleteAfterReadYes.setBackground(new java.awt.Color(255, 255, 255));
        deleteAfterReadYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(deleteAfterReadYes);
        deleteAfterReadYes.setText("Yes");
        deleteAfterReadYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        deleteAfterReadNo.setBackground(new java.awt.Color(255, 255, 255));
        deleteAfterReadNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(deleteAfterReadNo);
        deleteAfterReadNo.setSelected(true);
        deleteAfterReadNo.setText("No");
        deleteAfterReadNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        checkFileAgeYes.setBackground(new java.awt.Color(255, 255, 255));
        checkFileAgeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(checkFileAgeYes);
        checkFileAgeYes.setText("Yes");
        checkFileAgeYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkFileAgeYes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                checkFileAgeYesActionPerformed(evt);
            }
        });

        checkFileAgeNo.setBackground(new java.awt.Color(255, 255, 255));
        checkFileAgeNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(checkFileAgeNo);
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

        mirthVariableList1.setModel(new javax.swing.AbstractListModel()
        {
            String[] strings = { "DATE", "COUNT", "UUID", "SYSTIME", "ORIGINALNAME" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(mirthVariableList1);

        jLabel8.setText("Filename Filter Pattern:");

        jLabel9.setText("Process Batch Files:");

        processBatchFilesYes.setBackground(new java.awt.Color(255, 255, 255));
        processBatchFilesYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(processBatchFilesYes);
        processBatchFilesYes.setText("Yes");
        processBatchFilesYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        processBatchFilesNo.setBackground(new java.awt.Color(255, 255, 255));
        processBatchFilesNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(processBatchFilesNo);
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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel1)
                    .add(jLabel8)
                    .add(jLabel2)
                    .add(jLabel5)
                    .add(jLabel4)
                    .add(jLabel6)
                    .add(jLabel7)
                    .add(jLabel9)
                    .add(fileAgeLabel)
                    .add(jLabel3)
                    .add(jLabel41))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sortBy, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fileAge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(processBatchFilesYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(processBatchFilesNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fileNameFilter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(directoryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(pollingFreq, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(moveToDirectory, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                            .add(moveToPattern, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(deleteAfterReadYes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(checkFileAgeYes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(checkFileAgeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(deleteAfterReadNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 84, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {moveToDirectory, moveToPattern}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel1)
                            .add(directoryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel8)
                            .add(fileNameFilter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(pollingFreq, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel2))
                        .add(7, 7, 7)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel5)
                            .add(moveToDirectory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel4)
                            .add(moveToPattern, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(6, 6, 6)
                                .add(jLabel6)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel7)
                                    .add(checkFileAgeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(checkFileAgeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(deleteAfterReadYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(deleteAfterReadNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                    .add(layout.createSequentialGroup()
                        .add(76, 76, 76)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 84, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(processBatchFilesYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(processBatchFilesNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fileAgeLabel)
                    .add(fileAge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(sortBy, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel41)
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void processBatchFilesNoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_processBatchFilesNoActionPerformed
    {//GEN-HEADEREND:event_processBatchFilesNoActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_processBatchFilesNoActionPerformed

    private void charsetEncodingComboboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_charsetEncodingComboboxActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_charsetEncodingComboboxActionPerformed

    private void checkFileAgeNoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkFileAgeNoActionPerformed
    {//GEN-HEADEREND:event_checkFileAgeNoActionPerformed
        fileAgeLabel.setEnabled(false);
        fileAge.setEnabled(false);
    }//GEN-LAST:event_checkFileAgeNoActionPerformed

    private void checkFileAgeYesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkFileAgeYesActionPerformed
    {//GEN-HEADEREND:event_checkFileAgeYesActionPerformed
        fileAgeLabel.setEnabled(true);
        fileAge.setEnabled(true);
    }//GEN-LAST:event_checkFileAgeYesActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.webreach.mirth.client.ui.components.MirthRadioButton checkFileAgeNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton checkFileAgeYes;
    private com.webreach.mirth.client.ui.components.MirthRadioButton deleteAfterReadNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton deleteAfterReadYes;
    private com.webreach.mirth.client.ui.components.MirthTextField directoryField;
    private com.webreach.mirth.client.ui.components.MirthTextField fileAge;
    private javax.swing.JLabel fileAgeLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField fileNameFilter;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane2;
    private com.webreach.mirth.client.ui.components.MirthVariableList mirthVariableList1;
    private com.webreach.mirth.client.ui.components.MirthTextField moveToDirectory;
    private com.webreach.mirth.client.ui.components.MirthTextField moveToPattern;
    private com.webreach.mirth.client.ui.components.MirthTextField pollingFreq;
    private com.webreach.mirth.client.ui.components.MirthRadioButton processBatchFilesNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton processBatchFilesYes;
    private com.webreach.mirth.client.ui.components.MirthComboBox sortBy;
    // End of variables declaration//GEN-END:variables

}
