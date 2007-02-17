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
import java.util.StringTokenizer;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;

/**
 * A form that extends from ConnectorClass.  All methods implemented
 * are described in ConnectorClass.
 */
public class HTTPListener extends ConnectorClass
{
    Frame parent;
    /** Creates new form HTTPListener */
    private final String DATATYPE = "DataType";
    private final String HTTP_ADDRESS = "host";
    private final String HTTP_PORT = "port";
    private final String HTTP_RECEIVE_TIMEOUT = "receiveTimeout";
    private final String HTTP_BUFFER_SIZE = "bufferSize";
    private final String HTTP_KEEP_CONNECTION_OPEN = "keepSendSocketOpen";
    private final String HTTP_START_OF_MESSAGE_CHARACTER = "messageStart";
    private final String HTTP_END_OF_MESSAGE_CHARACTER = "messageEnd";
    private final String HTTP_FIELD_SEPARATOR = "fieldSeparator";
    private final String HTTP_RECORD_SEPARATOR = "recordSeparator";
    private final String HTTP_SEND_ACK = "sendACK";

    public HTTPListener()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        name = "HTTP Listener";
        initComponents();
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        String listenerIPAddress = listenerIPAddressField.getText() + "." + listenerIPAddressField1.getText() + "." + listenerIPAddressField2.getText() + "." + listenerIPAddressField3.getText();
        properties.put(HTTP_ADDRESS, listenerIPAddress);
        properties.put(HTTP_PORT, listenerPortField.getText());
        properties.put(HTTP_RECEIVE_TIMEOUT, receiveTimeoutField.getText());
        properties.put(HTTP_BUFFER_SIZE, bufferSizeField.getText());

        if (keepConnectionOpenYesRadio.isSelected())
            properties.put(HTTP_KEEP_CONNECTION_OPEN, UIConstants.YES_OPTION);
        else
            properties.put(HTTP_KEEP_CONNECTION_OPEN, UIConstants.NO_OPTION);

        properties.put(HTTP_START_OF_MESSAGE_CHARACTER, startOfMessageCharacterField.getText());
        properties.put(HTTP_END_OF_MESSAGE_CHARACTER, endOfMessageCharacterField.getText());
        properties.put(HTTP_FIELD_SEPARATOR, fieldSeparatorField.getText());
        properties.put(HTTP_RECORD_SEPARATOR, recordSeparatorField.getText());
        properties.put(HTTP_SEND_ACK, sendACKCombobox.getSelectedItem());
        return properties;
    }

    public void setProperties(Properties props)
    {
        String listenerIPAddress = (String)props.get(HTTP_ADDRESS);
        StringTokenizer IP = new StringTokenizer(listenerIPAddress, ".");
        if (IP.hasMoreTokens())
            listenerIPAddressField.setText(IP.nextToken());
        else
            listenerIPAddressField.setText("");
        if (IP.hasMoreTokens())
            listenerIPAddressField1.setText(IP.nextToken());
        else
            listenerIPAddressField1.setText("");
        if (IP.hasMoreTokens())
            listenerIPAddressField2.setText(IP.nextToken());
        else
            listenerIPAddressField2.setText("");
        if (IP.hasMoreTokens())
            listenerIPAddressField3.setText(IP.nextToken());
        else
            listenerIPAddressField3.setText("");

        listenerPortField.setText((String)props.get(HTTP_PORT));
        receiveTimeoutField.setText((String)props.get(HTTP_RECEIVE_TIMEOUT));
        bufferSizeField.setText((String)props.get(HTTP_BUFFER_SIZE));

        if(((String)props.get(HTTP_KEEP_CONNECTION_OPEN)).equals(UIConstants.YES_OPTION))
            keepConnectionOpenYesRadio.setSelected(true);
        else
            keepConnectionOpenNoRadio.setSelected(true);

        startOfMessageCharacterField.setText((String)props.get(HTTP_START_OF_MESSAGE_CHARACTER));
        endOfMessageCharacterField.setText((String)props.get(HTTP_END_OF_MESSAGE_CHARACTER));
        fieldSeparatorField.setText((String)props.get(HTTP_FIELD_SEPARATOR));
        recordSeparatorField.setText((String)props.get(HTTP_RECORD_SEPARATOR));
        sendACKCombobox.setSelectedItem(props.get(HTTP_SEND_ACK));
    }

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(HTTP_ADDRESS, "127.0.0.1");
        properties.put(HTTP_PORT, "6660");
        properties.put(HTTP_RECEIVE_TIMEOUT, "5000");
        properties.put(HTTP_BUFFER_SIZE, "65536");
        properties.put(HTTP_KEEP_CONNECTION_OPEN, UIConstants.NO_OPTION);
        properties.put(HTTP_START_OF_MESSAGE_CHARACTER, "0x0B");
        properties.put(HTTP_END_OF_MESSAGE_CHARACTER, "0x1C");
        properties.put(HTTP_FIELD_SEPARATOR, "0x7C");
        properties.put(HTTP_RECORD_SEPARATOR, "0x0D");
        properties.put(HTTP_SEND_ACK, sendACKCombobox.getItemAt(0));
        return properties;
    }
    
    public boolean checkProperties(Properties props)
    {
         if(((String)props.get(HTTP_ADDRESS)).length() > 0 && ((String)props.get(HTTP_PORT)).length() > 0 && 
        ((String)props.get(HTTP_RECEIVE_TIMEOUT)).length() > 0 && ((String)props.get(HTTP_BUFFER_SIZE)).length() > 0 &&
        ((String)props.get(HTTP_START_OF_MESSAGE_CHARACTER)).length() > 0 && ((String)props.get(HTTP_END_OF_MESSAGE_CHARACTER)).length() > 0 &&
         ((String)props.get(HTTP_FIELD_SEPARATOR)).length() > 0 && ((String)props.get(HTTP_RECORD_SEPARATOR)).length() > 0)
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
        keepConnectionOpenGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        sendACKCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        bufferSizeField = new com.webreach.mirth.client.ui.components.MirthTextField();
        receiveTimeoutField = new com.webreach.mirth.client.ui.components.MirthTextField();
        listenerIPAddressField = new com.webreach.mirth.client.ui.components.MirthTextField();
        listenerPortField = new com.webreach.mirth.client.ui.components.MirthTextField();
        recordSeparatorField = new com.webreach.mirth.client.ui.components.MirthTextField();
        startOfMessageCharacterField = new com.webreach.mirth.client.ui.components.MirthTextField();
        endOfMessageCharacterField = new com.webreach.mirth.client.ui.components.MirthTextField();
        fieldSeparatorField = new com.webreach.mirth.client.ui.components.MirthTextField();
        keepConnectionOpenYesRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        keepConnectionOpenNoRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        listenerIPAddressField1 = new com.webreach.mirth.client.ui.components.MirthTextField();
        listenerIPAddressField2 = new com.webreach.mirth.client.ui.components.MirthTextField();
        listenerIPAddressField3 = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel1.setText("Listener IP Address:");

        jLabel2.setText("Listener Port:");

        jLabel3.setText("Receive Timeout (ms):");

        jLabel4.setText("Buffer Size:");

        jLabel5.setText("Keep Connection Open:");

        jLabel34.setText("Start of Message Character:");

        jLabel35.setText("End of Message Character:");

        jLabel36.setText("Record Sparator:");

        jLabel37.setText("Field Separator:");

        jLabel38.setText("Send ACK:");

        sendACKCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Auto", "Yes", "No" }));

        keepConnectionOpenYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenYesRadio);
        keepConnectionOpenYesRadio.setText("Yes");
        keepConnectionOpenYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        keepConnectionOpenNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenNoRadio);
        keepConnectionOpenNoRadio.setText("No");
        keepConnectionOpenNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel6.setText(".");

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel7.setText(".");

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel8.setText(".");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel2)
                    .add(jLabel1)
                    .add(jLabel3)
                    .add(jLabel4)
                    .add(jLabel5)
                    .add(jLabel34)
                    .add(jLabel35)
                    .add(jLabel37)
                    .add(jLabel36)
                    .add(jLabel38))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(keepConnectionOpenYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(keepConnectionOpenNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(receiveTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(listenerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sendACKCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(startOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(endOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fieldSeparatorField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(recordSeparatorField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(bufferSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(listenerIPAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(listenerIPAddressField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel7)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(listenerIPAddressField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel8)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(listenerIPAddressField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(listenerIPAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(listenerIPAddressField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel1))
                    .add(jLabel6)
                    .add(listenerIPAddressField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8)
                    .add(listenerIPAddressField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(listenerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(receiveTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(bufferSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keepConnectionOpenYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(keepConnectionOpenNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(startOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel34))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(endOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel35))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fieldSeparatorField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel37))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(recordSeparatorField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel36))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sendACKCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel38)))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.components.MirthTextField bufferSizeField;
    private com.webreach.mirth.client.ui.components.MirthTextField endOfMessageCharacterField;
    private com.webreach.mirth.client.ui.components.MirthTextField fieldSeparatorField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.ButtonGroup keepConnectionOpenGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton keepConnectionOpenNoRadio;
    private com.webreach.mirth.client.ui.components.MirthRadioButton keepConnectionOpenYesRadio;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerIPAddressField;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerIPAddressField1;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerIPAddressField2;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerIPAddressField3;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerPortField;
    private com.webreach.mirth.client.ui.components.MirthTextField receiveTimeoutField;
    private com.webreach.mirth.client.ui.components.MirthTextField recordSeparatorField;
    private com.webreach.mirth.client.ui.components.MirthComboBox sendACKCombobox;
    private com.webreach.mirth.client.ui.components.MirthTextField startOfMessageCharacterField;
    // End of variables declaration//GEN-END:variables

}
