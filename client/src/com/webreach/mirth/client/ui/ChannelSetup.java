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

package com.webreach.mirth.client.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.client.ui.editors.filter.FilterPane;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.client.ui.panels.reference.ReferenceListFactory;
import com.webreach.mirth.client.ui.util.VariableListUtil;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.connectors.jdbc.DatabaseWriterProperties;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.Filter;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.Rule;
import com.webreach.mirth.model.Step;
import com.webreach.mirth.model.Transformer;
import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.converters.ObjectCloner;
import com.webreach.mirth.model.converters.ObjectClonerException;
import com.webreach.mirth.util.PropertyVerifier;

/** The channel editor panel. Majority of the client application */
public class ChannelSetup extends javax.swing.JPanel
{
    private static final String DESTINATION_DEFAULT = "File Writer";
    private static final String SOURCE_DEFAULT = "LLP Listener";
    private static final String DATABASE_READER = "Database Reader";
    private static final String HTTP_LISTENER = "HTTP Listener";
    private final String STATUS_COLUMN_NAME = "Status";
    private final String DESTINATION_COLUMN_NAME = "Destination";
    private final String CONNECTOR_TYPE_COLUMN_NAME = "Connector Type";
    private final int SOURCE_TAB_INDEX = 1;
    private final int DESTINATIONS_TAB_INDEX = 2;
    private final String DATA_TYPE_KEY = "DataType";
    public Channel currentChannel;
    public String lastIndex = "";
    public TransformerPane transformerPane = new TransformerPane();
    public FilterPane filterPane = new FilterPane();
    private Frame parent;
    private boolean isDeleting = false;
    private boolean loadingChannel = false;
    private boolean channelValidationFailed = false;
    public Map<String, ConnectorMetaData> transports;
    private ArrayList<String> sourceConnectors;
    private ArrayList<String> destinationConnectors;

    /**
     * Creates the Channel Editor panel. Calls initComponents() and sets up the
     * model, dropdowns, and mouse listeners.
     */
    public ChannelSetup()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        
        initComponents();
        
        numDays.setDocument(new MirthFieldConstraints(3, false, false, true));
        summaryNameField.setDocument(new MirthFieldConstraints(40, false, true, true));
        
        incomingProtocol.setModel(new javax.swing.DefaultComboBoxModel(parent.protocols.values().toArray()));
        
        channelView.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showChannelEditPopupMenu(evt, false);
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showChannelEditPopupMenu(evt, false);
            }
        });
        
    
        transports = this.parent.getConnectorMetaData();
        sourceConnectors = new ArrayList<String>();
        destinationConnectors = new ArrayList<String>();
        Iterator i = transports.entrySet().iterator();
        while (i.hasNext())
        {
            Entry entry = (Entry) i.next();
            try{
            
            	ConnectorMetaData metaData = transports.get(entry.getKey());
           
                if (metaData.getType() == ConnectorMetaData.Type.LISTENER && metaData.isEnabled())
                {
                    sourceConnectors.add(metaData.getName());
                    
                    try
                    {
                        parent.sourceConnectors.add((ConnectorClass) Class.forName(metaData.getClientClassName()).newInstance());
                    }
                    catch (Exception e)
                    {
                        parent.alertError("Could not load class: " + metaData.getClientClassName());
                    }
                }
                if (metaData.getType() == ConnectorMetaData.Type.SENDER && metaData.isEnabled())
                {
                    destinationConnectors.add(metaData.getName());
                    
                    try
                    {
                        parent.destinationConnectors.add((ConnectorClass) Class.forName(metaData.getClientClassName()).newInstance());
                    }
                    catch (Exception e)
                    {
                        parent.alertError("Could not load class: " + metaData.getClientClassName());
                    }
                }
            }catch(ClassCastException castException){
            	System.out.println("Unable to load plugin");
            }
        }
        
        Collections.sort(sourceConnectors);
        Collections.sort(destinationConnectors);
        
        channelView.setMaximumSize(new Dimension(450, 3000));
    }
    
    /**
     * Shows the trigger-button popup menu. If the trigger was pressed on a row
     * of the destination table, that row should be selected as well.
     */
    private void showChannelEditPopupMenu(java.awt.event.MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = destinationTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                destinationTable.setRowSelectionInterval(row, row);
            }
            parent.channelEditPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    /**
     * Is called to load the transformer pane on either the source or
     * destination
     */
    public String editTransformer()
    {
        String name = "";
        boolean changed = parent.changesHaveBeenMade();
        boolean transformerPaneLoaded = false;
        if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX)
        {
            name = "Source";
            transformerPaneLoaded = transformerPane.load(currentChannel.getSourceConnector(), currentChannel.getSourceConnector().getTransformer(), changed);
        }
        
        else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX)
        {
            int destination = getDestinationConnectorIndex((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME)));
            transformerPaneLoaded = transformerPane.load(currentChannel.getDestinationConnectors().get(destination), currentChannel.getDestinationConnectors().get(destination).getTransformer(), changed);
            name = currentChannel.getDestinationConnectors().get(destination).getName();
        }
        if (!transformerPaneLoaded)
        {
            parent.taskPaneContainer.add(parent.getOtherPane());
            parent.setCurrentContentPage(parent.channelEditPanel);
            parent.setCurrentTaskPaneContainer(parent.taskPaneContainer);
            name = "Edit Channel - " + parent.channelEditPanel.currentChannel.getName();            
            parent.channelEditPanel.updateComponentShown();                
        }
        return name;
    }
    
    /** Is called to load the filter pane on either the source or destination */
    public String editFilter()
    {
        String name = "";
        boolean changed = parent.changesHaveBeenMade();
        boolean filterPaneLoaded = false;
        
        if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX)
        {
            name = "Source";
            filterPaneLoaded = filterPane.load(currentChannel.getSourceConnector(), currentChannel.getSourceConnector().getFilter(), currentChannel.getSourceConnector().getTransformer(), changed);
        }
        
        else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX)
        {
            Connector destination = currentChannel.getDestinationConnectors().get(getDestinationConnectorIndex((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME))));
            filterPaneLoaded = filterPane.load(destination, destination.getFilter(), destination.getTransformer(), changed);
            name = destination.getName();
        }
        
        if (!filterPaneLoaded)
        {
            parent.taskPaneContainer.add(parent.getOtherPane());
            parent.setCurrentContentPage(parent.channelEditPanel);
            parent.setCurrentTaskPaneContainer(parent.taskPaneContainer);
            name = "Edit Channel - " + parent.channelEditPanel.currentChannel.getName();            
            parent.channelEditPanel.updateComponentShown();        
        }
        return name;
    }
    
    /**
     * Makes the destination table with a parameter that is true if a new
     * destination should be added as well.
     */
    public void makeDestinationTable(boolean addNew)
    {
        List<Connector> destinationConnectors;
        Object[][] tableData;
        int tableSize;
        
        destinationConnectors = currentChannel.getDestinationConnectors();
        tableSize = destinationConnectors.size();
        if (addNew)
            tableSize++;
        tableData = new Object[tableSize][3];
        for (int i = 0; i < tableSize; i++)
        {
            if (tableSize - 1 == i && addNew)
            {
                Connector connector = makeNewConnector(true);
                connector.getTransformer().setInboundProtocol(null);
                connector.getTransformer().setOutboundProtocol(null);
                connector.setName(getNewDestinationName(tableSize));
                connector.setTransportName(DESTINATION_DEFAULT);
                
                if (connector.isEnabled())
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_blue.png")), UIConstants.ENABLED_STATUS);
                else
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_black.png")), UIConstants.DISABLED_STATUS);
                tableData[i][1] = connector.getName();
                tableData[i][2] = connector.getTransportName();
                
                destinationConnectors.add(connector);
            }
            else
            {

                if (destinationConnectors.get(i).isEnabled())
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_blue.png")), UIConstants.ENABLED_STATUS);
                else
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_black.png")), UIConstants.DISABLED_STATUS);
                tableData[i][1] = destinationConnectors.get(i).getName();
                tableData[i][2] = destinationConnectors.get(i).getTransportName();
            }
        }
        
        destinationTable = new MirthTable();
        
        destinationTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] { STATUS_COLUMN_NAME, DESTINATION_COLUMN_NAME, CONNECTOR_TYPE_COLUMN_NAME })
        {
            boolean[] canEdit = new boolean[] { false, true, false };
            
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit[columnIndex];
            }
        });
        
        // Set the custom cell editor for the Destination Name column.
        destinationTable.getColumnModel().getColumn(destinationTable.getColumnModel().getColumnIndex(DESTINATION_COLUMN_NAME)).setCellEditor(new DestinationTableCellEditor());
        
        // Must set the maximum width on columns that should be packed.
        destinationTable.getColumnExt(STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        destinationTable.getColumnExt(STATUS_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        
        // Set the cell renderer for the status column.
        destinationTable.getColumnExt(STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());
        
        destinationTable.setSelectionMode(0);
        destinationTable.setRowSelectionAllowed(true);
        destinationTable.setRowHeight(UIConstants.ROW_HEIGHT);
        
        destinationTable.setFocusable(true);
        destinationTable.setSortable(false);
        
        destinationTable.setOpaque(true);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            ((JXTable) destinationTable).setHighlighters(highlighter);
        }
        
        // This action is called when a new selection is made on the destination
        // table.
        destinationTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                if (!evt.getValueIsAdjusting())
                {
                    // if it's currently editing, make sure to reset the last index to the new value
                    if (destinationTable.isEditing())
                        lastIndex = (String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME));
                    
                    int last = getDestinationIndex(lastIndex);
                    
                    if (last != -1 && last != destinationTable.getRowCount() && !isDeleting)
                    {
                        int connectorIndex = getDestinationConnectorIndex((String) destinationTable.getValueAt(last, getColumnNumber(DESTINATION_COLUMN_NAME)));
                        Connector destinationConnector = currentChannel.getDestinationConnectors().get(connectorIndex);
                        destinationConnector.setProperties(destinationConnectorClass.getProperties());
                    }
                    
                    if (!loadConnector())
                    {
                        if (getDestinationIndex(lastIndex) == destinationTable.getRowCount())
                            destinationTable.setRowSelectionInterval(last - 1, last - 1);
                        else
                            destinationTable.setRowSelectionInterval(last, last);
                    }
                    else
                    {
                        lastIndex = ((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME)));
                    }
                    
                    checkVisibleDestinationTasks();
                }
            }
        });
        destinationTable.requestFocus();
        // Mouse listener for trigger-button popup on the table.
        destinationTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showChannelEditPopupMenu(evt, true);
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showChannelEditPopupMenu(evt, true);
            }
        });
        
        // Checks to see what to set the new row selection to based on
        // last index and if a new destination was added.
        int last = getDestinationIndex(lastIndex);
        if (addNew)
            destinationTable.setRowSelectionInterval(destinationTable.getRowCount() - 1, destinationTable.getRowCount() - 1);
        else if (last == -1)
            destinationTable.setRowSelectionInterval(0, 0); // Makes sure the
        // event is called
        // when the table is
        // created.
        else if (last == destinationTable.getRowCount())
            destinationTable.setRowSelectionInterval(last - 1, last - 1);
        else
            destinationTable.setRowSelectionInterval(last, last);
        
        destinationTablePane.setViewportView(destinationTable);
        destinationTablePane.setWheelScrollingEnabled(true);
        // Mouse listener for trigger-button popup on the table pane (not actual
        // table).
        destinationTablePane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showChannelEditPopupMenu(evt, false);
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showChannelEditPopupMenu(evt, false);
            }
        });
        // Key Listener trigger for CTRL-S
        destinationTable.addKeyListener(new KeyListener()
        {
            public void keyPressed(KeyEvent e)
            {
                // TODO Auto-generated method stub
                if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown())
                {
                	PlatformUI.MIRTH_FRAME.doContextSensitiveSave();
                }
            }
            
            public void keyReleased(KeyEvent e)
            {
                // TODO Auto-generated method stub
                
            }
            
            public void keyTyped(KeyEvent e)
            {
                // TODO Auto-generated method stub
                
            }
        });
        destinationTable.addMouseWheelListener(new MouseWheelListener()
        {
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                destinationTablePane.getMouseWheelListeners()[0].mouseWheelMoved(e);
            }
            
        });
    }
    
    /** Get the index of a destination by being passed its name. */
    private int getDestinationIndex(String destinationName)
    {
        for (int i = 0; i < destinationTable.getRowCount(); i++)
        {
            if (((String) destinationTable.getValueAt(i, getColumnNumber(DESTINATION_COLUMN_NAME))).equalsIgnoreCase(destinationName))
                return i;
        }
        return -1;
    }
    
    /**
     * Get the name that should be used for a new destination so that it is
     * unique.
     */
    private String getNewDestinationName(int size)
    {
        String temp = "Destination ";
        
        for (int i = 1; i <= size; i++)
        {
            boolean exists = false;
            for (int j = 0; j < size - 1; j++)
            {
                if (((String) destinationTable.getValueAt(j, getColumnNumber(DESTINATION_COLUMN_NAME))).equalsIgnoreCase(temp + i))
                    exists = true;
            }
            if (!exists)
                return temp + i;
        }
        return "";
    }
    
    /** Get the column index number based on its name. */
    private int getColumnNumber(String name)
    {
        for (int i = 0; i < destinationTable.getColumnCount(); i++)
        {
            if (destinationTable.getColumnName(i).equalsIgnoreCase(name))
                return i;
        }
        return -1;
    }
    
    /** Get the currently selected destination index */
    public int getSelectedDestinationIndex()
    {
        if (destinationTable.isEditing())
            return destinationTable.getEditingRow();
        else
            return destinationTable.getSelectedRow();
    }
    
    /** Get a destination connector index by passing in its name */
    private int getDestinationConnectorIndex(String destinationName)
    {
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        for (int i = 0; i < destinationConnectors.size(); i++)
        {
            if (destinationConnectors.get(i).getName().equalsIgnoreCase(destinationName))
                return i;
        }
        return -1;
    }
    
    /** Loads a selected connector and returns true on success. */
    public boolean loadConnector()
    {
        List<Connector> destinationConnectors;
        String destinationName;
        
        if (getSelectedDestinationIndex() != -1)
            destinationName = (String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME));
        else
            return false;
        
        if (currentChannel != null && currentChannel.getDestinationConnectors() != null)
        {
            destinationConnectors = currentChannel.getDestinationConnectors();
            for (int i = 0; i < destinationConnectors.size(); i++)
            {
                if (destinationConnectors.get(i).getName().equalsIgnoreCase(destinationName))
                {
                    boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();
                    destinationSourceDropdown.setSelectedItem(destinationConnectors.get(i).getTransportName());
                    parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
                    
                    return true;
                }
            }
        }
        return false;
    }
    
    /** Sets the overall panel to edit the channel with the given channel index. */
    public void editChannel(Channel channel)
    {
        loadingChannel = true;
        channelValidationFailed = false;
        lastIndex = "";
        currentChannel = channel;
        
        PropertyVerifier.checkConnectorProperties(currentChannel, transports);
        
        sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(sourceConnectors.toArray()));
        destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(destinationConnectors.toArray()));
        
        loadChannelInfo();
        makeDestinationTable(false);
        setDestinationVariableList();
        loadingChannel = false;
        channelView.setSelectedIndex(0);
    }
    
    /**
     * Adds a new channel that is passed in and then sets the overall panel to
     * edit that channel.
     */
    public void addChannel(Channel channel)
    {
        loadingChannel = true;
        lastIndex = "";
        currentChannel = channel;
        
        sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(sourceConnectors.toArray()));
        destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(destinationConnectors.toArray()));
        
        Connector sourceConnector = makeNewConnector(false);
        sourceConnector.setName("sourceConnector");
        sourceConnector.setTransportName(SOURCE_DEFAULT);
        Transformer sourceTransformer = new Transformer();
        sourceTransformer.setInboundProtocol(Protocol.HL7V2);
        sourceTransformer.setOutboundProtocol(null);
        sourceConnector.setTransformer(sourceTransformer);
        
        currentChannel.setSourceConnector(sourceConnector);
        setLastModified();
        loadChannelInfo();
        makeDestinationTable(true);
        setDestinationVariableList();
        loadingChannel = false;
        channelView.setSelectedIndex(0);
        summaryNameField.requestFocus();
        parent.enableSave();
    }
    
    private void setLastModified()
    {
        currentChannel.setLastModified(Calendar.getInstance());
    }
    
    private void updateRevision()
    {
        summaryRevision.setText("Revision: " + currentChannel.getRevision());
    }
    
    private void updateLastModified()
    {
        lastModified.setText("Last Modified: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentChannel.getLastModified().getTime()));
    }    
    
    /** Load all of the saved channel information into the channel editor */
    private void loadChannelInfo()
    {        
        parent.setPanelName("Edit Channel - " + currentChannel.getName());
        summaryNameField.setText(currentChannel.getName());
        summaryDescriptionText.setText(currentChannel.getDescription());
        updateRevision();
        updateLastModified();
        
        if (currentChannel.isEnabled())
            summaryEnabledCheckbox.setSelected(true);
        else
            summaryEnabledCheckbox.setSelected(false);
        
        if (currentChannel.getSourceConnector().getTransformer().getInboundProtocol() != null)
        {
            incomingProtocol.setSelectedItem(parent.protocols.get(currentChannel.getSourceConnector().getTransformer().getInboundProtocol()));
        }
        
        LinkedHashMap<String, String> scriptMap = new LinkedHashMap<String, String>();
        if (currentChannel.getPreprocessingScript() != null)
            scriptMap.put(ScriptPanel.PREPROCESSOR_SCRIPT, currentChannel.getPreprocessingScript());
        else
            scriptMap.put(ScriptPanel.PREPROCESSOR_SCRIPT, "// Modify the message variable below to pre process data\nreturn message;");
        
        if (currentChannel.getDeployScript() != null)
            scriptMap.put(ScriptPanel.DEPLOY_SCRIPT, currentChannel.getDeployScript());
        else
            scriptMap.put(ScriptPanel.DEPLOY_SCRIPT, "// This script executes once when the mule engine is started\n// You only have access to the globalMap here to persist data\nreturn;");
        
        if (currentChannel.getShutdownScript() != null)
            scriptMap.put(ScriptPanel.SHUTDOWN_SCRIPT, currentChannel.getShutdownScript());
        else
            scriptMap.put(ScriptPanel.SHUTDOWN_SCRIPT, "// This script executes once when the mule engine is stopped\n// You only have access to the globalMap here to persist data\nreturn;");
        
        if (currentChannel.getPostprocessingScript() != null)
            scriptMap.put(ScriptPanel.POSTPROCESSOR_SCRIPT, currentChannel.getPostprocessingScript());
        else
            scriptMap.put(ScriptPanel.POSTPROCESSOR_SCRIPT, "// This script executes once after a message has been processed\nreturn;");
        
        scripts.setScripts(scriptMap);
        
        PropertyVerifier.checkChannelProperties(currentChannel);
        
        if (((String) currentChannel.getProperties().get("transactional")).equalsIgnoreCase("true"))
            transactionalCheckBox.setSelected(true);
        else
            transactionalCheckBox.setSelected(false);
        
        if (((String) currentChannel.getProperties().get("removeNamespace")).equalsIgnoreCase("false"))
            removeNamespaceCheckBox.setSelected(false);
        else
            removeNamespaceCheckBox.setSelected(true);
        
        if (((String) currentChannel.getProperties().get("synchronous")).equalsIgnoreCase("false"))
            synchronousCheckBox.setSelected(false);
        else
            synchronousCheckBox.setSelected(true);
        
        if (((String) currentChannel.getProperties().get("encryptData")).equalsIgnoreCase("true"))
            encryptMessagesCheckBox.setSelected(true);
        else
            encryptMessagesCheckBox.setSelected(false);
        
        if (currentChannel.getSourceConnector().getTransformer().getInboundProtocol() == null)
            currentChannel.getSourceConnector().getTransformer().setInboundProtocol(MessageObject.Protocol.HL7V2);
        
        if (((String) currentChannel.getProperties().get("store_messages")).equalsIgnoreCase("false"))
        {
            storeMessages.setSelected(false);
            storeMessagesAll.setEnabled(false);
            storeMessagesAll.setSelected(true);
            storeMessagesDays.setEnabled(false);
            storeMessagesErrors.setEnabled(false);
            storeFiltered.setEnabled(false);
            numDays.setText("");
            numDays.setEnabled(false);
            days.setEnabled(false);
        }
        else
        {
            storeMessages.setSelected(true);
            
            if (((String) currentChannel.getProperties().get("error_messages_only")).equalsIgnoreCase("true"))
                storeMessagesErrors.setSelected(true);
            else
                storeMessagesErrors.setSelected(false);
            
            storeFiltered.setEnabled(true);
            
            if (((String) currentChannel.getProperties().get("dont_store_filtered")).equalsIgnoreCase("true"))
                storeFiltered.setSelected(true);
            else
                storeFiltered.setSelected(false);
            
            if (!((String) currentChannel.getProperties().get("max_message_age")).equalsIgnoreCase("-1"))
            {
                numDays.setText((String) currentChannel.getProperties().get("max_message_age"));
                storeMessagesDays.setSelected(true);
                numDays.setEnabled(true);
            }
            else
            {
                storeMessagesAll.setSelected(true);
                numDays.setText("");
            }
        }
        
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();
        
        sourceSourceDropdown.setSelectedItem(currentChannel.getSourceConnector().getTransportName());
        checkSourceDataType();
        
        if (((String) currentChannel.getProperties().get("initialState")).equalsIgnoreCase("started"))
            initialState.setSelectedItem("Started");
        else
            initialState.setSelectedItem("Stopped");
        
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }
    
    /**
     * Update channels scripts
     */
    public void updateScripts()
    {
        currentChannel.setPreprocessingScript(scripts.getScripts().get(ScriptPanel.PREPROCESSOR_SCRIPT));
        currentChannel.setDeployScript(scripts.getScripts().get(ScriptPanel.DEPLOY_SCRIPT));
        currentChannel.setShutdownScript(scripts.getScripts().get(ScriptPanel.SHUTDOWN_SCRIPT));
        currentChannel.setPostprocessingScript(scripts.getScripts().get(ScriptPanel.POSTPROCESSOR_SCRIPT));
    }
    
    /**
     * Save all of the current channel information in the editor to the actual
     * channel
     */
    public boolean saveChanges()
    {
        if (!parent.checkChannelName(summaryNameField.getText(), currentChannel.getId()))
            return false;
        
        boolean enabled = summaryEnabledCheckbox.isSelected();
        
        currentChannel.getSourceConnector().setProperties(sourceConnectorClass.getProperties());
        
        if (parent.currentContentPage == transformerPane)
        {
            transformerPane.accept(false);
            transformerPane.modified = false; // TODO: Check this. Fix to
            // prevent double save on
            // confirmLeave
        }
        if (parent.currentContentPage == filterPane)
        {
            filterPane.accept(false);
            filterPane.modified = false; // TODO: Check this. Fix to prevent
            // double save on confirmLeave
        }
        
        Connector temp;
        
        temp = currentChannel.getDestinationConnectors().get(getDestinationConnectorIndex((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME))));
        temp.setProperties(destinationConnectorClass.getProperties());
        
        currentChannel.setName(summaryNameField.getText());
        currentChannel.setDescription(summaryDescriptionText.getText());
        
        updateScripts();
        
        setLastModified();
        
        // Set the default protocols if transformers have never been visited
        
        Transformer sourceTransformer = currentChannel.getSourceConnector().getTransformer();
        
        for (MessageObject.Protocol protocol : MessageObject.Protocol.values())
        {
            if (parent.protocols.get(protocol).equals((String) incomingProtocol.getSelectedItem()))
            {
                sourceTransformer.setInboundProtocol(protocol);
            }
        }
        
        if (sourceTransformer.getOutboundProtocol() == null)
            sourceTransformer.setOutboundProtocol(sourceTransformer.getInboundProtocol());
        
        for (Connector c : currentChannel.getDestinationConnectors())
        {
            Transformer destinationTransformer = c.getTransformer();
            
            if (destinationTransformer.getInboundProtocol() == null)
            {
                destinationTransformer.setInboundProtocol(sourceTransformer.getOutboundProtocol());
            }
            
            if (destinationTransformer.getOutboundProtocol() == null)
            {
                destinationTransformer.setOutboundProtocol(destinationTransformer.getInboundProtocol());
            }
        }
        
        if (transactionalCheckBox.isSelected())
            currentChannel.getProperties().put("transactional", "true");
        else
            currentChannel.getProperties().put("transactional", "false");
        
        if (removeNamespaceCheckBox.isSelected())
            currentChannel.getProperties().put("removeNamespace", "true");
        else
            currentChannel.getProperties().put("removeNamespace", "false");
        
        if (synchronousCheckBox.isSelected())
            currentChannel.getProperties().put("synchronous", "true");
        else
            currentChannel.getProperties().put("synchronous", "false");
        
        if (encryptMessagesCheckBox.isSelected())
            currentChannel.getProperties().put("encryptData", "true");
        else
            currentChannel.getProperties().put("encryptData", "false");
        
        if(storeFiltered.isSelected())
            currentChannel.getProperties().put("dont_store_filtered", "true");
        else
            currentChannel.getProperties().put("dont_store_filtered", "false");
        
        if (storeMessages.isSelected())
        {
            currentChannel.getProperties().put("store_messages", "true");
            if (storeMessagesAll.isSelected())
                currentChannel.getProperties().put("max_message_age", "-1");
            else
                currentChannel.getProperties().put("max_message_age", numDays.getText());
            
            if (storeMessagesErrors.isSelected())
                currentChannel.getProperties().put("error_messages_only", "true");
            else
                currentChannel.getProperties().put("error_messages_only", "false");
        }
        else
        {
            currentChannel.getProperties().put("store_messages", "false");
            currentChannel.getProperties().put("max_message_age", "-1");
        }
        
        if (((String) initialState.getSelectedItem()).equalsIgnoreCase("Stopped"))
            currentChannel.getProperties().put("initialState", "stopped");
        else
            currentChannel.getProperties().put("initialState", "started");
        
        String validationMessage = checkAllForms(currentChannel);
        if (validationMessage != null)
        {
            enabled = false;
            
            // If there is an error on one of the forms, then run the
            // validation on the current form to display any errors.
        	if (channelView.getSelectedComponent() == destination)
        	{
        		// If the destination is enabled...
            	if (currentChannel.getDestinationConnectors().get(getDestinationConnectorIndex((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME)))).isEnabled())
            		destinationConnectorClass.checkProperties(destinationConnectorClass.getProperties(), true);
        	}
        	else if (channelView.getSelectedComponent() == source)
        		sourceConnectorClass.checkProperties(sourceConnectorClass.getProperties(), true);
            
            summaryEnabledCheckbox.setSelected(false);
            
            parent.alertCustomError(validationMessage, CustomErrorDialog.ERROR_SAVING_CHANNEL);
        }
        
        // Set the channel to enabled or disabled after it has been validated
        currentChannel.setEnabled(enabled);
        
        boolean updated = true;
        
        try
        {
            if (!parent.channels.containsKey(currentChannel.getId()))
                currentChannel.setId(parent.mirthClient.getGuid());
            
            updated = parent.updateChannel(currentChannel);
            
            try
            {
                currentChannel = (Channel)ObjectCloner.deepCopy(parent.channels.get(currentChannel.getId()));
                if (parent.currentContentPage == transformerPane)
                {
                    if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX)
                    {
                        transformerPane.reload(currentChannel.getSourceConnector(), currentChannel.getSourceConnector().getTransformer());
                    }
                    
                    else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX)
                    {
                        int destination = getDestinationConnectorIndex((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME)));
                        transformerPane.reload(currentChannel.getDestinationConnectors().get(destination), currentChannel.getDestinationConnectors().get(destination).getTransformer());
                    }
                }
                if (parent.currentContentPage == filterPane)
                {
                    if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX)
                    {
                        filterPane.reload(currentChannel.getSourceConnector(), currentChannel.getSourceConnector().getFilter());
                    }
                    
                    else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX)
                    {
                        Connector destination = currentChannel.getDestinationConnectors().get(getDestinationConnectorIndex((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME))));
                        filterPane.reload(destination, destination.getFilter());
                    }
                }
                updateRevision();
                updateLastModified();
            }
            catch (ObjectClonerException e)
            {
                parent.alertException(e.getStackTrace(), e.getMessage());
            }
        }
        catch (ClientException e)
        {
            parent.alertException(e.getStackTrace(), e.getMessage());
        }
        
        return updated;
    }
    
    /** Adds a new destination. */
    public void addNewDestination()
    {
        makeDestinationTable(true);
        destinationTablePane.getViewport().setViewPosition(new Point(0, destinationTable.getRowHeight() * destinationTable.getRowCount()));
        parent.enableSave();
    }
    
    public void cloneDestination()
    {
        if (parent.changesHaveBeenMade())
        {
            if (parent.alertOption("You must save your channel before cloning.  Would you like to save your channel now?"))
                saveChanges();
            else
                return;
        }
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        String destinationName = (String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME));
        
        Connector destination = null;
        try
        {
            destination = (Connector) ObjectCloner.deepCopy(destinationConnectors.get(getDestinationConnectorIndex(destinationName)));
        }
        catch (ObjectClonerException e)
        {
            parent.alertException(e.getStackTrace(), e.getMessage());
            return;
        }
        
        destination.setName(getNewDestinationName(destinationConnectors.size() + 1));
        destinationConnectors.add(destination);
        makeDestinationTable(false);
        parent.enableSave();
    }
    
    public void enableDestination()
    {
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        Connector destination = destinationConnectors.get(getSelectedDestinationIndex());
        destination.setEnabled(true);
        makeDestinationTable(false);
        parent.enableSave();
        
        // If validation has failed, then highlight any errors on this form.
    	if (channelValidationFailed)
    		destinationConnectorClass.checkProperties(destinationConnectorClass.getProperties(), true);
    }
    
    public void disableDestination()
    {
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        
        // Check to make sure at least two destinations are currently enabled.
        int enabledCount = 0;
        for (int i = 0; i < destinationConnectors.size(); i++)
        {
            if (destinationConnectors.get(i).isEnabled())
                enabledCount++;
        }
        if (enabledCount <= 1)
        {
            parent.alertError("You must have at least one destination enabled.");
            return;
        }
        
        Connector destination = destinationConnectors.get(getSelectedDestinationIndex());
        destination.setEnabled(false);
        makeDestinationTable(false);
        parent.enableSave();
        
        // If validation has failed then errors might be highlighted.
        // Remove highlights on this form.
        if (channelValidationFailed)
        	destinationConnectorClass.checkProperties(destinationConnectorClass.getProperties(), false);
    }
    
    /** Deletes the selected destination. */
    public void deleteDestination()
    {
        isDeleting = true;
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        if (destinationConnectors.size() == 1)
        {
            JOptionPane.showMessageDialog(parent, "You must have at least one destination.");
            return;
        }
        
        boolean enabledDestination = false;
        // if there is an enabled destination besides the one being deleted, set enabledDestination to true.
        for (int i = 0; i < destinationConnectors.size(); i++)
        {
        	if (destinationConnectors.get(i).isEnabled() && (i != getDestinationConnectorIndex((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME)))))
        	{
        		enabledDestination = true;
        	}
        }
        
        if (!enabledDestination)
        {
        	JOptionPane.showMessageDialog(parent, "You must have at least one destination enabled.");
            return;
        }
        
        destinationConnectors.remove(getDestinationConnectorIndex((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME))));
        
        makeDestinationTable(false);
        parent.enableSave();
        isDeleting = false;
    }
    
    /**
     * Checks to see which tasks (move up, move down, enable, and distable) should be available for
     * destinations and enables or disables them.
     */
    public void checkVisibleDestinationTasks()
    {
        if (channelView.getSelectedComponent() == destination)
        {
            // enable and disable
            List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
            Connector destination = destinationConnectors.get(getSelectedDestinationIndex());
            if (destination.isEnabled())
            {
                parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 5, 5, false);
                parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 6, 6, true);
            }
            else
            {
                parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 5, 5, true);
                parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 6, 6, false);
            }
            
            // move up and move down
            if (getSelectedDestinationIndex() == 0)
                parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 7, 7, false);
            else
                parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 7, 7, true);
            
            if (getSelectedDestinationIndex() == destinationTable.getRowCount() - 1)
                parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 8, 8, false);
            else
                parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 8, 8, true);
        }
    }
    
    /**
     * Moves the selected destination to the previous spot in the array list.
     */
    public void moveDestinationUp()
    {
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        int destinationIndex = getSelectedDestinationIndex();
        
        destinationConnectors.add(destinationIndex - 1, destinationConnectors.get(destinationIndex));
        destinationConnectors.remove(destinationIndex + 1);
        
        makeDestinationTable(false);
        setDestinationVariableList();
        parent.enableSave();
    }
    
    /**
     * Moves the selected destination to the next spot in the array list.
     */
    public void moveDestinationDown()
    {
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        int destinationIndex = getSelectedDestinationIndex();
        
        destinationConnectors.add(destinationIndex + 2, destinationConnectors.get(destinationIndex));
        destinationConnectors.remove(destinationIndex);
        
        makeDestinationTable(false);
        setDestinationVariableList();
        parent.enableSave();
    }
    
    /**
     * Checks all of the connectors in this channel and returns the errors found.
     * 
     * @param channel
     * @return
     */
    public String checkAllForms(Channel channel)
    {
        String errors = "";
        ConnectorClass tempConnector = null;
        Properties tempProps = null;
        
        // Check source connector
        for (int i = 0; i < parent.sourceConnectors.size(); i++)
        {
            if (parent.sourceConnectors.get(i).getName().equalsIgnoreCase(channel.getSourceConnector().getTransportName()))
            {
                tempConnector = parent.sourceConnectors.get(i);
                tempProps = channel.getSourceConnector().getProperties();
                
                errors += validateFilterRules(channel.getSourceConnector());
                errors += validateTransformerSteps(channel.getSourceConnector());
            }
        }
        if (tempConnector != null)
        {        	
        	String validationMessage = tempConnector.doValidate(tempProps, false);
        	if (validationMessage != null)
        		errors += validationMessage;
        }
        
        // Check destination connector
        for (int i = 0; i < channel.getDestinationConnectors().size(); i++)
        {
        	// Only check the destination connector if it is enabled.
        	if (channel.getDestinationConnectors().get(i).isEnabled())
        	{
	            for (int j = 0; j < parent.destinationConnectors.size(); j++)
	            {
	                if (parent.destinationConnectors.get(j).getName().equalsIgnoreCase(channel.getDestinationConnectors().get(i).getTransportName()))
	                {
	                    tempConnector = parent.destinationConnectors.get(j);
	                    tempProps = channel.getDestinationConnectors().get(i).getProperties();
	                    
	                    errors += validateFilterRules(channel.getDestinationConnectors().get(i));
	                    errors += validateTransformerSteps(channel.getDestinationConnectors().get(i));
	                }
	            }
	            if (tempConnector != null) 
	            {
	            	String validationMessage = tempConnector.doValidate(tempProps, false);
	            	if (validationMessage != null)
	            		errors += validationMessage;
	            }
	            
	            tempConnector = null;
	            tempProps = null;
        	}
        }
        
        errors += validateScripts(channel);
        
        if (errors.equals(""))
        {
        	errors = null;
        	channelValidationFailed = false;
        }
        else
        {
        	channelValidationFailed = true;
        }
        
        return errors;
    }
    
    private String validateTransformerSteps(Connector connector)
    {
    	String errors = "";
    	
    	for (Step step : connector.getTransformer().getSteps())
        {
        	String validationMessage = this.transformerPane.validateStep(step);
        	if (validationMessage != null)
        	{
        		errors += "Error in connector \"" + connector.getName() + "\" at step " + step.getSequenceNumber() + " (\"" + step.getName()+ "\"):\n" + validationMessage + "\n\n";
        	}
        }
    	
    	return errors;
    }
    
    private String validateFilterRules(Connector connector)
    {
    	String errors = "";
    	
    	for (Rule rule : connector.getFilter().getRules())
        {
        	String validationMessage = this.filterPane.validateRule(rule);
        	if (validationMessage != null)
        	{
        		errors += "Error in connector \"" + connector.getName() + "\" at rule " + rule.getSequenceNumber() + " (\"" + rule.getName()+ "\"):\n" + validationMessage + "\n\n";
        	}
        }
    	
    	return errors;
    }
    
    private String validateScripts(Channel channel)
    {
    	String errors = "";
    	
    	String validationMessage = this.scripts.validateScript(channel.getDeployScript());
    	if (validationMessage != null)
    		errors += "Error in channel script \"" + ScriptPanel.DEPLOY_SCRIPT + "\":\n" + validationMessage + "\n\n";
    	
    	validationMessage = this.scripts.validateScript(channel.getPreprocessingScript());
    	if (validationMessage != null)
    		errors += "Error in channel script \"" + ScriptPanel.PREPROCESSOR_SCRIPT + "\":\n" + validationMessage + "\n\n";
    	
    	validationMessage = this.scripts.validateScript(channel.getPostprocessingScript());
    	if (validationMessage != null)
    		errors += "Error in channel script \"" + ScriptPanel.POSTPROCESSOR_SCRIPT + "\":\n" + validationMessage + "\n\n";
    	
    	validationMessage = this.scripts.validateScript(channel.getShutdownScript());
    	if (validationMessage != null)
    		errors += "Error in channel script \"" + ScriptPanel.SHUTDOWN_SCRIPT + "\":\n" + validationMessage + "\n\n";
    	
    	return errors;
    }
    
    public void doValidate()
    {
        if (source.isVisible())
        {
        	String validationMessage = sourceConnectorClass.doValidate(sourceConnectorClass.getProperties(), true);
            if (validationMessage != null)
                parent.alertCustomError(validationMessage, CustomErrorDialog.ERROR_VALIDATING_CONNECTOR);
            else
                parent.alertInformation("The connector was successfully validated.");
        }
        else
        {
        	String validationMessage = destinationConnectorClass.doValidate(destinationConnectorClass.getProperties(), true);
        	if (validationMessage != null)
                parent.alertWarning(validationMessage);
            else
                parent.alertInformation("The connector was successfully validated.");
        }
    }
    
    public void validateScripts()
    {
        scripts.validateCurrentScript();
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        filterButtonGroup = new javax.swing.ButtonGroup();
        validationButtonGroup = new javax.swing.ButtonGroup();
        buttonGroup1 = new javax.swing.ButtonGroup();
        channelView = new javax.swing.JTabbedPane();
        summary = new javax.swing.JPanel();
        summaryNameLabel = new javax.swing.JLabel();
        summaryDescriptionLabel = new javax.swing.JLabel();
        summaryNameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        summaryPatternLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        summaryDescriptionText = new com.webreach.mirth.client.ui.components.MirthTextPane();
        jLabel1 = new javax.swing.JLabel();
        initialState = new com.webreach.mirth.client.ui.components.MirthComboBox();
        encryptMessagesCheckBox = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        storeMessages = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        storeMessagesAll = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        storeMessagesDays = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        days = new javax.swing.JLabel();
        numDays = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel3 = new javax.swing.JLabel();
        storeMessagesErrors = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        incomingProtocol = new com.webreach.mirth.client.ui.components.MirthComboBox();
        storeFiltered = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        jPanel1 = new javax.swing.JPanel();
        summaryEnabledCheckbox = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        transactionalCheckBox = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        removeNamespaceCheckBox = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        synchronousCheckBox = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        summaryRevision = new javax.swing.JLabel();
        lastModified = new javax.swing.JLabel();
        source = new javax.swing.JPanel();
        sourceSourceDropdown = new com.webreach.mirth.client.ui.components.MirthComboBox();
        sourceSourceLabel = new javax.swing.JLabel();
        sourceConnectorPane = new javax.swing.JScrollPane();
        sourceConnectorClass = new com.webreach.mirth.connectors.ConnectorClass();
        destination = new javax.swing.JPanel();
        destinationSourceDropdown = new com.webreach.mirth.client.ui.components.MirthComboBox();
        destinationSourceLabel = new javax.swing.JLabel();
        destinationVariableList = new com.webreach.mirth.client.ui.VariableList();
        destinationConnectorPane = new javax.swing.JScrollPane();
        destinationConnectorClass = new com.webreach.mirth.connectors.ConnectorClass();
        destinationTablePane = new javax.swing.JScrollPane();
        destinationTable = new com.webreach.mirth.client.ui.components.MirthTable();
        scripts = new ScriptPanel(ReferenceListFactory.CHANNEL_CONTEXT);

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        channelView.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        channelView.setFocusable(false);
        summary.setBackground(new java.awt.Color(255, 255, 255));
        summary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        summary.setFocusable(false);
        summary.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                summaryComponentShown(evt);
            }
        });

        summaryNameLabel.setText("Channel Name:");

        summaryDescriptionLabel.setText("Description:");

        summaryNameField.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyReleased(java.awt.event.KeyEvent evt)
            {
                summaryNameFieldKeyReleased(evt);
            }
        });

        summaryPatternLabel1.setText("Incoming Data:");

        jScrollPane1.setViewportView(summaryDescriptionText);

        jLabel1.setText("Initial State:");

        initialState.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Started", "Stopped" }));

        encryptMessagesCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        encryptMessagesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        encryptMessagesCheckBox.setText("Encrypt messages in database");
        encryptMessagesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        storeMessages.setBackground(new java.awt.Color(255, 255, 255));
        storeMessages.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        storeMessages.setText("Store message data");
        storeMessages.setMargin(new java.awt.Insets(0, 0, 0, 0));
        storeMessages.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                storeMessagesActionPerformed(evt);
            }
        });

        storeMessagesAll.setBackground(new java.awt.Color(255, 255, 255));
        storeMessagesAll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(storeMessagesAll);
        storeMessagesAll.setText("Store indefinitely");
        storeMessagesAll.setMargin(new java.awt.Insets(0, 0, 0, 0));
        storeMessagesAll.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                storeMessagesAllActionPerformed(evt);
            }
        });

        storeMessagesDays.setBackground(new java.awt.Color(255, 255, 255));
        storeMessagesDays.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(storeMessagesDays);
        storeMessagesDays.setText("Prune messages after");
        storeMessagesDays.setMargin(new java.awt.Insets(0, 0, 0, 0));
        storeMessagesDays.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                storeMessagesDaysActionPerformed(evt);
            }
        });

        days.setText("day(s)");

        jLabel3.setText("Messages:");

        storeMessagesErrors.setBackground(new java.awt.Color(255, 255, 255));
        storeMessagesErrors.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        storeMessagesErrors.setText("With errors only");
        storeMessagesErrors.setMargin(new java.awt.Insets(0, 0, 0, 0));
        storeMessagesErrors.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                storeMessagesErrorsActionPerformed(evt);
            }
        });

        incomingProtocol.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        storeFiltered.setBackground(new java.awt.Color(255, 255, 255));
        storeFiltered.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        storeFiltered.setText("Do not store filtered messages");
        storeFiltered.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        summaryEnabledCheckbox.setBackground(new java.awt.Color(255, 255, 255));
        summaryEnabledCheckbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        summaryEnabledCheckbox.setSelected(true);
        summaryEnabledCheckbox.setText("Enabled");
        summaryEnabledCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        transactionalCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        transactionalCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        transactionalCheckBox.setText("Use transactional endpoints");
        transactionalCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        removeNamespaceCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        removeNamespaceCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        removeNamespaceCheckBox.setText("Strip namespace from messages");
        removeNamespaceCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        synchronousCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        synchronousCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        synchronousCheckBox.setText("Synchronize channel");
        synchronousCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        synchronousCheckBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                synchronousCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(summaryEnabledCheckbox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(removeNamespaceCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(synchronousCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(transactionalCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(summaryEnabledCheckbox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(transactionalCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(removeNamespaceCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(synchronousCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(22, 22, 22))
        );

        summaryRevision.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        summaryRevision.setText("Revision: ");

        lastModified.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lastModified.setText("Last Modified: ");

        org.jdesktop.layout.GroupLayout summaryLayout = new org.jdesktop.layout.GroupLayout(summary);
        summary.setLayout(summaryLayout);
        summaryLayout.setHorizontalGroup(
            summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(summaryLayout.createSequentialGroup()
                .addContainerGap()
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, summaryDescriptionLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, summaryPatternLabel1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, summaryNameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(summaryLayout.createSequentialGroup()
                        .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, incomingProtocol, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, initialState, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(summaryNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(encryptMessagesCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(lastModified, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, summaryRevision, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)))
                    .add(summaryLayout.createSequentialGroup()
                        .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(storeMessages, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(summaryLayout.createSequentialGroup()
                                .add(35, 35, 35)
                                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(summaryLayout.createSequentialGroup()
                                        .add(storeMessagesAll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(storeMessagesDays, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(4, 4, 4)
                                        .add(numDays, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(days))
                                    .add(storeMessagesErrors, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(storeFiltered, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 292, Short.MAX_VALUE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 624, Short.MAX_VALUE))
                .addContainerGap())
        );
        summaryLayout.setVerticalGroup(
            summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(summaryLayout.createSequentialGroup()
                .addContainerGap()
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(summaryLayout.createSequentialGroup()
                        .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(summaryLayout.createSequentialGroup()
                                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(summaryNameLabel)
                                    .add(summaryNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(summaryPatternLabel1)
                                    .add(incomingProtocol, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel1)
                                    .add(initialState, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel3)
                                    .add(encryptMessagesCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(storeMessages, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(storeFiltered, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(storeMessagesErrors, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 83, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(storeMessagesAll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(numDays, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(storeMessagesDays, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(days)))
                    .add(summaryLayout.createSequentialGroup()
                        .add(summaryRevision)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lastModified)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(summaryDescriptionLabel)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE))
                .addContainerGap())
        );
        channelView.addTab("Summary", summary);

        source.setBackground(new java.awt.Color(255, 255, 255));
        source.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        source.setFocusable(false);
        source.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                sourceComponentShown(evt);
            }
        });

        sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TCP/IP", "Database", "Email" }));
        sourceSourceDropdown.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                sourceSourceDropdownActionPerformed(evt);
            }
        });

        sourceSourceLabel.setText("Connector Type:");

        sourceConnectorPane.setBackground(new java.awt.Color(255, 255, 255));
        sourceConnectorPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Connetor Class", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        sourceConnectorClass.setBackground(new java.awt.Color(255, 255, 255));
        org.jdesktop.layout.GroupLayout sourceConnectorClassLayout = new org.jdesktop.layout.GroupLayout(sourceConnectorClass);
        sourceConnectorClass.setLayout(sourceConnectorClassLayout);
        sourceConnectorClassLayout.setHorizontalGroup(
            sourceConnectorClassLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 1428, Short.MAX_VALUE)
        );
        sourceConnectorClassLayout.setVerticalGroup(
            sourceConnectorClassLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 646, Short.MAX_VALUE)
        );
        sourceConnectorPane.setViewportView(sourceConnectorClass);

        org.jdesktop.layout.GroupLayout sourceLayout = new org.jdesktop.layout.GroupLayout(source);
        source.setLayout(sourceLayout);
        sourceLayout.setHorizontalGroup(
            sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sourceLayout.createSequentialGroup()
                .addContainerGap()
                .add(sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sourceConnectorPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 701, Short.MAX_VALUE)
                    .add(sourceLayout.createSequentialGroup()
                        .add(sourceSourceLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sourceSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        sourceLayout.setVerticalGroup(
            sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sourceLayout.createSequentialGroup()
                .addContainerGap()
                .add(sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sourceSourceLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sourceSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sourceConnectorPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
                .addContainerGap())
        );
        channelView.addTab("Source", source);

        destination.setBackground(new java.awt.Color(255, 255, 255));
        destination.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        destination.setFocusable(false);
        destination.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                destinationComponentShown(evt);
            }
        });

        destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TCP/IP", "Database", "Email" }));
        destinationSourceDropdown.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                destinationSourceDropdownActionPerformed(evt);
            }
        });

        destinationSourceLabel.setText("Connector Type:");

        destinationConnectorPane.setBackground(new java.awt.Color(255, 255, 255));
        destinationConnectorPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Connector Class", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        destinationConnectorClass.setBackground(new java.awt.Color(255, 255, 255));
        org.jdesktop.layout.GroupLayout destinationConnectorClassLayout = new org.jdesktop.layout.GroupLayout(destinationConnectorClass);
        destinationConnectorClass.setLayout(destinationConnectorClassLayout);
        destinationConnectorClassLayout.setHorizontalGroup(
            destinationConnectorClassLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 624, Short.MAX_VALUE)
        );
        destinationConnectorClassLayout.setVerticalGroup(
            destinationConnectorClassLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 601, Short.MAX_VALUE)
        );
        destinationConnectorPane.setViewportView(destinationConnectorClass);

        destinationTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        destinationTablePane.setViewportView(destinationTable);

        org.jdesktop.layout.GroupLayout destinationLayout = new org.jdesktop.layout.GroupLayout(destination);
        destination.setLayout(destinationLayout);
        destinationLayout.setHorizontalGroup(
            destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(destinationLayout.createSequentialGroup()
                .addContainerGap()
                .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, destinationTablePane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 701, Short.MAX_VALUE)
                    .add(destinationLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(destinationConnectorPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(destinationVariableList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 186, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, destinationLayout.createSequentialGroup()
                        .add(destinationSourceLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(destinationSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        destinationLayout.setVerticalGroup(
            destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(destinationLayout.createSequentialGroup()
                .addContainerGap()
                .add(destinationTablePane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 165, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(destinationSourceLabel)
                    .add(destinationSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(destinationVariableList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                    .add(destinationConnectorPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE))
                .addContainerGap())
        );
        channelView.addTab("Destinations", destination);

        scripts.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                scriptsComponentShown(evt);
            }
        });

        channelView.addTab("Scripts", scripts);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(channelView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 726, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(channelView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void synchronousCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_synchronousCheckBoxActionPerformed
    {//GEN-HEADEREND:event_synchronousCheckBoxActionPerformed
        if (!synchronousCheckBox.isSelected())
        {
            boolean disableSynch = parent.alertOption("Disabling synchronization is not recommended for the following reasons:\n" +
                    "    1) Destinations are not guaranteed to run in order, so they cannot reference each other.\n" +
                    "    2) Your source cannot send a response from any of the destinations.\n" +
                    "    3) Your source connector may be modified to to ensure compatibility with requirement #2.\n" +
                    "    4) Turning off synchronization will disable the post-processor." +
                    "Are you sure you want to disable synchronization of your channel?");
            
            if (!disableSynch)
                synchronousCheckBox.setSelected(true);
        }
        
        sourceConnectorClass.updateResponseDropDown();
    }//GEN-LAST:event_synchronousCheckBoxActionPerformed

    private void scriptsComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_scriptsComponentShown
    {//GEN-HEADEREND:event_scriptsComponentShown
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 10, false);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 12, 12, true);
    }//GEN-LAST:event_scriptsComponentShown
    
    private void summaryNameFieldKeyReleased(java.awt.event.KeyEvent evt)// GEN-FIRST:event_summaryNameFieldKeyReleased
    {// GEN-HEADEREND:event_summaryNameFieldKeyReleased
        currentChannel.setName(summaryNameField.getText());
        parent.setPanelName("Edit Channel - " + currentChannel.getName());
    }// GEN-LAST:event_summaryNameFieldKeyReleased
    
    private void storeMessagesErrorsActionPerformed(java.awt.event.ActionEvent evt)
    {
        // TODO add your handling code here:
    }
    
    private void storeMessagesDaysActionPerformed(java.awt.event.ActionEvent evt)
    {
        numDays.setEnabled(true);
    }
    
    private void storeMessagesAllActionPerformed(java.awt.event.ActionEvent evt)
    {
        numDays.setEnabled(false);
        numDays.setText("");
    }
    
    private void storeMessagesActionPerformed(java.awt.event.ActionEvent evt)
    {
        if (storeMessages.isSelected())
        {
            storeMessagesAll.setEnabled(true);
            storeMessagesDays.setEnabled(true);
            storeMessagesErrors.setEnabled(true);
            storeFiltered.setEnabled(true);
            days.setEnabled(true);
        }
        else
        {
            storeMessagesAll.setEnabled(false);
            storeMessagesDays.setEnabled(false);
            storeMessagesErrors.setEnabled(false);
            storeFiltered.setEnabled(false);
            days.setEnabled(false);
            numDays.setText("");
            numDays.setEnabled(false);
        }
    }
    
    /** Action when the summary tab is shown. */
    private void summaryComponentShown(java.awt.event.ComponentEvent evt)// GEN-FIRST:event_summaryComponentShown
    {
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 10, false);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 12, 12, false);
    }
    
    /** Action when the source tab is shown. */
    private void sourceComponentShown(java.awt.event.ComponentEvent evt)// GEN-FIRST:event_sourceComponentShown
    {
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 1, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 2, 8, false);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 9, 11, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 12, 12, false);
        
        
        int connectorIndex = getDestinationConnectorIndex((String) destinationTable.getValueAt(destinationTable.getSelectedRow(), getColumnNumber(DESTINATION_COLUMN_NAME)));
        Connector destinationConnector = currentChannel.getDestinationConnectors().get(connectorIndex);
        destinationConnector.setProperties(destinationConnectorClass.getProperties());
        updateScripts();
        
        sourceConnectorClass.updateResponseDropDown();
        
        // If validation has failed, then highlight any errors on this form.
        if (channelValidationFailed)
        	sourceConnectorClass.checkProperties(sourceConnectorClass.getProperties(), true);
    }
    
    /** Action when the destinations tab is shown. */
    private void destinationComponentShown(java.awt.event.ComponentEvent evt)// GEN-FIRST:event_destinationComponentShown
    {
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 1, true);
        
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 2, 10, true);
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 12, 12, false);
        checkVisibleDestinationTasks();
        
        // If validation has failed and this destination is enabled, then highlight any errors on this form.
        if (channelValidationFailed && currentChannel.getDestinationConnectors().get(getDestinationConnectorIndex((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME)))).isEnabled())
        	destinationConnectorClass.checkProperties(destinationConnectorClass.getProperties(), true);
    }
    
    /** Action when an action is performed on the source connector type dropdown. */
    private void sourceSourceDropdownActionPerformed(java.awt.event.ActionEvent evt)
    {
        // If a channel is not being loaded then alert the user when necessary
        // that
        // changing the connector type will lose all current connector data.
        if (!loadingChannel)
        {
            if (sourceConnectorClass.getName() != null && sourceConnectorClass.getName().equals((String) sourceSourceDropdown.getSelectedItem()))
                return;
            
            if (!PropertyVerifier.compareProps(sourceConnectorClass.getProperties(), sourceConnectorClass.getDefaults()))
            {
                boolean changeType = parent.alertOption("Are you sure you would like to change this connector type and lose all of the current connector data?");
                if (!changeType)
                {
                    sourceSourceDropdown.setSelectedItem(sourceConnectorClass.getProperties().get(DATA_TYPE_KEY));
                    return;
                }
            }
        }
        
        // Get the selected source connector and set it.
        for (int i = 0; i < parent.sourceConnectors.size(); i++)
        {
            if (parent.sourceConnectors.get(i).getName().equalsIgnoreCase((String) sourceSourceDropdown.getSelectedItem()))
            {
                sourceConnectorClass = parent.sourceConnectors.get(i);
            }
        }
        
        // Sets all of the properties, transformer, filter, etc. on the new
        // source connector.
        Connector sourceConnector = currentChannel.getSourceConnector();
        if (sourceConnector != null)
        {
            String dataType = sourceConnector.getProperties().getProperty(DATA_TYPE_KEY);
            if (dataType == null)
                dataType = "";
            
            if (sourceConnector.getProperties().size() == 0 || !dataType.equals((String) sourceSourceDropdown.getSelectedItem()))
            {
                String name = sourceConnector.getName();
                changeConnectorType(sourceConnector, false);
                sourceConnector.setName(name);
                sourceConnectorClass.setProperties(sourceConnectorClass.getDefaults());
                sourceConnector.setProperties(sourceConnectorClass.getProperties());
            }
            
            sourceConnector.setTransportName((String) sourceSourceDropdown.getSelectedItem());
            currentChannel.setSourceConnector(sourceConnector);
            sourceConnectorClass.setProperties(sourceConnector.getProperties());
        }
        
        checkSourceDataType();
        
        sourceConnectorPane.setViewportView(sourceConnectorClass);
        ((TitledBorder) sourceConnectorPane.getBorder()).setTitle(sourceConnectorClass.getName());
        sourceConnectorPane.repaint();
        
        // If validation has failed, then highlight any errors on this form.
        if (channelValidationFailed)
        	sourceConnectorClass.checkProperties(sourceConnectorClass.getProperties(), true);
    }
    
    /**
     * Action when an action is performed on the destination connector type
     * dropdown. Fires off either generateMultipleDestinationPage() or
     * generateSingleDestinationPage()
     */
    private void destinationSourceDropdownActionPerformed(java.awt.event.ActionEvent evt)
    {
        // If a channel is not being loaded then alert the user when necessary
        // that
        // changing the connector type will lose all current connector data.
        if (!loadingChannel)
        {
            if (destinationConnectorClass.getName() != null && destinationConnectorClass.getName().equals((String) destinationSourceDropdown.getSelectedItem()) && lastIndex.equals((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME))))
                return;
            
            // if the selected destination is still the same AND the default
            // properties/transformer/filter have
            // not been changed from defaults then ask if the user would
            // like to really change connector type.
            if (lastIndex.equals((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME))) && (!PropertyVerifier.compareProps(destinationConnectorClass.getProperties(), destinationConnectorClass.getDefaults()) || currentChannel.getDestinationConnectors().get(getDestinationConnectorIndex((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME)))).getFilter().getRules().size() > 0 || currentChannel.getDestinationConnectors().get(getDestinationConnectorIndex((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME)))).getTransformer().getSteps().size() > 0))
            {
                boolean changeType = parent.alertOption("Are you sure you would like to change this connector type and lose all of the current connector data?");
                if (!changeType)
                {
                    destinationSourceDropdown.setSelectedItem(destinationConnectorClass.getProperties().get(DATA_TYPE_KEY));
                    return;
                }
            }
        }
        generateMultipleDestinationPage();
        
        // If validation has failed and this destination is enabled, then highlight any errors on this form.
        if (channelValidationFailed && currentChannel.getDestinationConnectors().get(getDestinationConnectorIndex((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME)))).isEnabled())
        	destinationConnectorClass.checkProperties(destinationConnectorClass.getProperties(), true);
    }
    
    public void generateMultipleDestinationPage()
    {
        // Get the selected destination connector and set it.
        for (int i = 0; i < parent.destinationConnectors.size(); i++)
        {
            if (parent.destinationConnectors.get(i).getName().equalsIgnoreCase((String) destinationSourceDropdown.getSelectedItem()))
                destinationConnectorClass = parent.destinationConnectors.get(i);
        }
        
        // Get the currently selected destination connector.
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        int connectorIndex = getDestinationConnectorIndex((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME)));
        Connector destinationConnector = destinationConnectors.get(connectorIndex);
        
        String dataType = destinationConnector.getProperties().getProperty(DATA_TYPE_KEY);
        if (dataType == null)
            dataType = "";
        
        // Debug with:
        // System.out.println(destinationConnector.getTransportName() + " " +
        // (String)destinationSourceDropdown.getSelectedItem());
        
        // Set to defaults on first load of connector or if it has changed
        // types.
        if (destinationConnector.getProperties().size() == 0 || !dataType.equals((String) destinationSourceDropdown.getSelectedItem()))
        {
            String name = destinationConnector.getName();
            changeConnectorType(destinationConnector, true);
            destinationConnector.setName(name);
            destinationConnectorClass.setProperties(destinationConnectorClass.getDefaults());
            destinationConnector.setProperties(destinationConnectorClass.getProperties());
        }
        
        // set db writer to have different suffix and prefix for drag and drop for js
        try
        {
            if(destinationConnectorClass.getName().equals(DatabaseWriterProperties.name))
            {
                if(destinationConnectorClass.getProperties().getProperty(DatabaseWriterProperties.DATABASE_USE_JS).equalsIgnoreCase(UIConstants.YES_OPTION))
                {
                    destinationVariableList.setPrefixAndSuffix("$(", ")");
                }
            }
            else
            {
                destinationVariableList.setPrefixAndSuffix("${", "}");
            }
        }
        catch(Exception e)
        {
            destinationVariableList.setPrefixAndSuffix("${", "}");
        }
        
        // Set the transport name of the destination connector and set it in the
        // list.
        destinationConnector.setTransportName((String) destinationSourceDropdown.getSelectedItem());
        destinationConnectors.set(connectorIndex, destinationConnector);
        
        // If the connector type has changed then set the new value in the
        // destination table.
        if (destinationConnector.getTransportName() != null && !((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(CONNECTOR_TYPE_COLUMN_NAME))).equals(destinationConnector.getTransportName()) && getSelectedDestinationIndex() != -1)
            destinationTable.setValueAt((String) destinationSourceDropdown.getSelectedItem(), getSelectedDestinationIndex(), getColumnNumber(CONNECTOR_TYPE_COLUMN_NAME));
        
        // Debug with:
        // System.out.println(destinationConnector.getProperties().toString());
        destinationConnectorClass.setProperties(destinationConnector.getProperties());
        setDestinationVariableList();
        
        destinationConnectorPane.setViewportView(destinationConnectorClass);
        ((TitledBorder) destinationConnectorPane.getBorder()).setTitle(destinationConnectorClass.getName());
        destinationConnectorPane.repaint();
    }
    
    private Set<String> getMultipleDestinationRules(Connector currentDestination)
    {
        Set<String> concatenatedRules = new LinkedHashSet<String>();
        VariableListUtil.getRuleVariables(concatenatedRules, currentChannel.getSourceConnector(), false);
        
        // add only the global variables
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        Iterator<Connector> it = destinationConnectors.iterator();
        boolean seenCurrent = false;
        while (it.hasNext())
        {
            Connector destination = it.next();
            if (currentDestination == destination)
            {
                seenCurrent = true;
                // add all the variables
                VariableListUtil.getRuleVariables(concatenatedRules, destination, true);
            }
            else if (!seenCurrent)
            {
                // add only the global variables
                VariableListUtil.getRuleVariables(concatenatedRules, destination, false);
                concatenatedRules.add(destination.getName());
            }
        }
        return concatenatedRules;
    }
    
    private Set<String> getMultipleDestinationStepVariables(Connector currentDestination)
    {
        Set<String> concatenatedSteps = new LinkedHashSet<String>();
        VariableListUtil.getStepVariables(concatenatedSteps, currentChannel.getSourceConnector(), false);
        
        // add only the global variables
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        Iterator<Connector> it = destinationConnectors.iterator();
        boolean seenCurrent = false;
        while (it.hasNext())
        {
            Connector destination = it.next();
            if (currentDestination == destination)
            {
                seenCurrent = true;
                // add all the variables
                VariableListUtil.getStepVariables(concatenatedSteps, destination, true);
            }
            else if (!seenCurrent)
            {
                // add only the global variables
            	VariableListUtil.getStepVariables(concatenatedSteps, destination, false);
            	concatenatedSteps.add(destination.getName());
            }
        }
        return concatenatedSteps;
    }
    
    /** Sets the destination variable list from the transformer steps */
    public void setDestinationVariableList()
    {
        int destination = getDestinationConnectorIndex((String) destinationTable.getValueAt(getSelectedDestinationIndex(), getColumnNumber(DESTINATION_COLUMN_NAME)));
        Set<String> concatenatedRuleVariables = getMultipleDestinationRules(currentChannel.getDestinationConnectors().get(destination));
        Set<String> concatenatedStepVariables = getMultipleDestinationStepVariables(currentChannel.getDestinationConnectors().get(destination));
        concatenatedRuleVariables.addAll(concatenatedStepVariables);
        destinationVariableList.setVariableListInbound(concatenatedRuleVariables);
        destinationVariableList.setDestinationMappingsLabel();
        destinationVariableList.repaint();
    }
    
    /** Returns a new connector, that has a new transformer and filter */
    public Connector makeNewConnector(boolean isDestination)
    {
        Connector c = new Connector();
        c.setEnabled(true);
        
        Transformer dt = new Transformer();
        Filter df = new Filter();
        
        if (isDestination)
            c.setMode(Connector.Mode.DESTINATION);
        else
            c.setMode(Connector.Mode.SOURCE);
        
        c.setTransformer(dt);
        c.setFilter(df);
        return c;
    }
    
    /** Changes the connector type without clearing filter and transformer */
    public void changeConnectorType(Connector c, boolean isDestination)
    {
        Transformer oldTransformer = c.getTransformer();
        Filter oldFilter = c.getFilter();
        
        if (isDestination)
            c = makeNewConnector(true);
        else
            c = makeNewConnector(false);
        
        c.setTransformer(oldTransformer);
        c.setFilter(oldFilter);
    }
    
    /** Returns the source connector class */
    public ConnectorClass getSourceConnector()
    {
        return sourceConnectorClass;
    }
    
    /** Returns the destination connector class */
    public ConnectorClass getDestinationConnector()
    {
        return destinationConnectorClass;
    }
    
    public void checkSourceDataType()
    {
        if (((String) sourceSourceDropdown.getSelectedItem()).equals(DATABASE_READER) || ((String) sourceSourceDropdown.getSelectedItem()).equals(HTTP_LISTENER))
        {
            incomingProtocol.setSelectedItem((String) parent.protocols.get(MessageObject.Protocol.XML));
            incomingProtocol.setEnabled(false);
        }
        else
        {
            incomingProtocol.setEnabled(true);
        }
    }
    
    public void updateComponentShown()
    {
        if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX)
        {
            sourceComponentShown(null);
        }
        else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX)
        {
            destinationComponentShown(null);
        }
    }
    
    public String getSourceDatatype()
    {
        return (String) incomingProtocol.getSelectedItem();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTabbedPane channelView;
    private javax.swing.JLabel days;
    private javax.swing.JPanel destination;
    private com.webreach.mirth.connectors.ConnectorClass destinationConnectorClass;
    private javax.swing.JScrollPane destinationConnectorPane;
    private com.webreach.mirth.client.ui.components.MirthComboBox destinationSourceDropdown;
    private javax.swing.JLabel destinationSourceLabel;
    private com.webreach.mirth.client.ui.components.MirthTable destinationTable;
    private javax.swing.JScrollPane destinationTablePane;
    public com.webreach.mirth.client.ui.VariableList destinationVariableList;
    private com.webreach.mirth.client.ui.components.MirthCheckBox encryptMessagesCheckBox;
    private javax.swing.ButtonGroup filterButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthComboBox incomingProtocol;
    private com.webreach.mirth.client.ui.components.MirthComboBox initialState;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lastModified;
    private com.webreach.mirth.client.ui.components.MirthTextField numDays;
    private com.webreach.mirth.client.ui.components.MirthCheckBox removeNamespaceCheckBox;
    private com.webreach.mirth.client.ui.ScriptPanel scripts;
    private javax.swing.JPanel source;
    private com.webreach.mirth.connectors.ConnectorClass sourceConnectorClass;
    private javax.swing.JScrollPane sourceConnectorPane;
    private com.webreach.mirth.client.ui.components.MirthComboBox sourceSourceDropdown;
    private javax.swing.JLabel sourceSourceLabel;
    private com.webreach.mirth.client.ui.components.MirthCheckBox storeFiltered;
    private com.webreach.mirth.client.ui.components.MirthCheckBox storeMessages;
    private com.webreach.mirth.client.ui.components.MirthRadioButton storeMessagesAll;
    private com.webreach.mirth.client.ui.components.MirthRadioButton storeMessagesDays;
    private com.webreach.mirth.client.ui.components.MirthCheckBox storeMessagesErrors;
    private javax.swing.JPanel summary;
    private javax.swing.JLabel summaryDescriptionLabel;
    private com.webreach.mirth.client.ui.components.MirthTextPane summaryDescriptionText;
    private com.webreach.mirth.client.ui.components.MirthCheckBox summaryEnabledCheckbox;
    private com.webreach.mirth.client.ui.components.MirthTextField summaryNameField;
    private javax.swing.JLabel summaryNameLabel;
    private javax.swing.JLabel summaryPatternLabel1;
    private javax.swing.JLabel summaryRevision;
    public com.webreach.mirth.client.ui.components.MirthCheckBox synchronousCheckBox;
    private com.webreach.mirth.client.ui.components.MirthCheckBox transactionalCheckBox;
    private javax.swing.ButtonGroup validationButtonGroup;
    // End of variables declaration//GEN-END:variables
    
}
