/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.client.ui.components;

import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.JTable;

import com.webreach.mirth.client.ui.DataTypesDialog;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.beans.DelimitedProperties;
import com.webreach.mirth.client.ui.beans.EDIProperties;
import com.webreach.mirth.client.ui.beans.HL7Properties;
import com.webreach.mirth.client.ui.beans.HL7V3Properties;
import com.webreach.mirth.client.ui.beans.NCPDPProperties;
import com.webreach.mirth.client.ui.beans.X12Properties;
import com.webreach.mirth.client.ui.beans.XMLProperties;
import com.webreach.mirth.client.ui.editors.BoundPropertiesSheetDialog;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.MessageObject;

public class DataTypesButtonCellEditor extends MirthButtonCellEditor {

    private boolean source;
    
    public DataTypesButtonCellEditor(JTable table, boolean source) {
        super(table);
        this.source = source;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        int selectedRow = super.table.convertRowIndexToModel(super.table.getEditingRow());
        String dataType = (String) super.table.getModel().getValueAt(selectedRow, DataTypesDialog.DATA_TYPE_COLUMN_NUMBER);
        Channel currentChannel = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel;
        
        // Load the properties editor for the selected data type
        if (source) {
            if (selectedRow == 0) {
                loadPropertiesEditor(dataType, currentChannel.getSourceConnector().getTransformer().getInboundProperties());
            } else {
                loadPropertiesEditor(dataType, currentChannel.getSourceConnector().getTransformer().getOutboundProperties());
                
                // Also set the inbound properties for all destinations
                for (Connector connector : currentChannel.getDestinationConnectors()) {
                    connector.getTransformer().setInboundProperties(currentChannel.getSourceConnector().getTransformer().getOutboundProperties());
                }
            }
        } else {
            loadPropertiesEditor(dataType, currentChannel.getDestinationConnectors().get(selectedRow).getTransformer().getOutboundProperties());
        }
        
        PlatformUI.MIRTH_FRAME.enableSave();
    }
    
    private void loadPropertiesEditor(String dataType, Properties dataProperties) {
        if (dataType.equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.EDI))) {
            new BoundPropertiesSheetDialog(dataProperties, new EDIProperties());
        } else if (dataType.equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.X12))) {
            new BoundPropertiesSheetDialog(dataProperties, new X12Properties());
        } else if (dataType.equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.HL7V2))) {
            new BoundPropertiesSheetDialog(dataProperties, new HL7Properties());
        } else if (dataType.equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.HL7V3))) {
            new BoundPropertiesSheetDialog(dataProperties, new HL7V3Properties());
        } else if (dataType.equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.NCPDP))) {
            new BoundPropertiesSheetDialog(dataProperties, new NCPDPProperties());
        } else if (dataType.equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.DELIMITED))) {
            new BoundPropertiesSheetDialog(dataProperties, new DelimitedProperties(), 550, 370);
        } else if (dataType.equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.XML))) {
            new BoundPropertiesSheetDialog(dataProperties, new XMLProperties());
        }
    }

}
