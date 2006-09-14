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


package com.webreach.mirth.client.ui.browsers.message;

import com.webreach.mirth.client.core.ListHandlerException;
import com.webreach.mirth.client.core.MessageListHandler;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.CenterCellRenderer;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import com.webreach.mirth.client.ui.components.MirthTextPane;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import java.awt.Cursor;
import java.awt.Point;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

/**
 * The message browser panel.
 */
public class MessageBrowser extends javax.swing.JPanel
{
    private final int FIRST_PAGE = 0;
    private final int PREVIOUS_PAGE = -1;
    private final int NEXT_PAGE = 1;
    private final String MESSAGE_ID_COLUMN_NAME = "Message ID";
    private final String CHANNEL_ID_COLUMN_NAME = "Channel ID";
    private final String DATE_COLUMN_NAME = "Date";
    private final String CONNECTOR_COLUMN_NAME = "Connector";
    private final String STATUS_COLUMN_NAME = "Status";
    private final String KEY_COLUMN_NAME = "Key";
    private final String VALUE_COLUMN_NAME = "Value";
    
    private JScrollPane eventPane;
    private JScrollPane mappingsPane;
    private JXTable eventTable;
    private Frame parent;
    private MessageListHandler messageListHandler;
    private List<MessageObject> messageObjectList;
    private MessageObjectFilter messageObjectFilter;
    
    /**
     * Constructs the new message browser and sets up its default information/layout.
     */
    public MessageBrowser()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        
        mappingsPane = new JScrollPane();
        makeMappingsTable(new String[0][0]);
        
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(MappingsPanel);
        MappingsPanel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mappingsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mappingsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
        
        this.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                if (evt.isPopupTrigger())
                    parent.eventPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                if (evt.isPopupTrigger())
                    parent.eventPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        });
             
        pageSizeField.setDocument(new MirthFieldConstraints(3, false, true));
        
        String[] values = new String[MessageObject.Status.values().length + 1];
        values[0] = "ALL";
        for (int i = 1; i < values.length; i++)
            values[i] = MessageObject.Status.values()[i-1].toString();
        
        statusComboBox.setModel(new javax.swing.DefaultComboBoxModel(values));
        
        eventPane = new JScrollPane();
        
        eventPane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showEventPopupMenu(evt, false);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showEventPopupMenu(evt, false);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectRows();
            }
        });
        
        eventPane.setViewportView(eventTable);
        
        jPanel2.removeAll();  
        
        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(eventPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(eventPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
        );
        
        jPanel2.updateUI();
    }
    
    /**
     * Loads up a clean message browser as if a new one was constructed.
     */
    public void loadNew()
    {
        // use the start filters and make the table.
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 2, -1, false);
        statusComboBox.setSelectedIndex(0);
        long currentTime = System.currentTimeMillis();
        mirthDatePicker1.setDateInMillis(currentTime);
        mirthDatePicker2.setDateInMillis(currentTime);
        pageSizeField.setText("20");
        
        filterButtonActionPerformed(null);
        clearDescription();
        descriptionTabbedPane.setSelectedIndex(0);
    }
    
    /**
     * Refreshes the panel with the curent filter information.
     */
    public void refresh()
    {
        deselectRows();
        filterButtonActionPerformed(null);
    }
    
    /**
     * Creates the table with all of the information given after
     * being filtered by the specified 'filter'
     */
    public void makeEventTable(MessageListHandler handler, int page) {
        eventTable = new JXTable();

        // Do all paging information below.
        try
        {
            if (page == FIRST_PAGE)
                messageObjectList = handler.getFirstPage();
            else if (page == PREVIOUS_PAGE)
                    messageObjectList = handler.getPreviousPage();
            else if (page == NEXT_PAGE)
            {
                messageObjectList = handler.getNextPage();
                if (messageObjectList.size() == 0)
                    messageObjectList = handler.getPreviousPage();
            }
            
            int pageSize = messageObjectFilter.getPageSize();
            if (pageSize == -1)
                pageSize = 0;
            
            pageSizeField.setText(pageSize + "");
            
            if (handler.getCurrentPage() == 0)
                previousPageButton.setEnabled(false);
            else
                previousPageButton.setEnabled(true);
            
            int numberOfPages;
            if (pageSize == 0)
                numberOfPages = 0;
            else
            {
                numberOfPages = handler.getSize() / pageSize;
                if ((handler.getSize() != 0) && ((handler.getSize() % pageSize) == 0))
                    numberOfPages--;
            }
            
            if (handler.getCurrentPage() == numberOfPages)
                nextPageButton.setEnabled(false);
            else
                nextPageButton.setEnabled(true);
            
            int startResult;
            if (handler.getSize() == 0)
                startResult = 0;
            else 
                startResult = (handler.getCurrentPage() * pageSize) + 1;
            
            int endResult;
            if (pageSize == 0)
                endResult = handler.getSize();
            else
                endResult = (handler.getCurrentPage() + 1) * pageSize;
            
            if (handler.getSize() < endResult)
                endResult = handler.getSize();
            resultsLabel.setText("Results " + startResult + " - " + endResult + " of " + handler.getSize());
            
        }
        catch (ListHandlerException e)
        {
            messageObjectList = null;
            parent.alertException(e.getStackTrace(), e.getMessage());
        }
        
        if (messageObjectList == null)
            return;
                
        Object[][] tableData = new Object[messageObjectList.size()][5];
        
        for (int i=0; i < messageObjectList.size(); i++)
        {
            MessageObject messageObject = messageObjectList.get(i);
            
            tableData[i][0] = messageObject.getId();
            tableData[i][1] = messageObject.getChannelId();
            
            Calendar calendar = messageObject.getDateCreated();
            
            tableData[i][2] = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar);
            tableData[i][3] = messageObject.getConnectorName();
            tableData[i][4] = messageObject.getStatus();
            
        }
                
        
        eventTable.setModel(new javax.swing.table.DefaultTableModel(
                tableData,
                new String []
        {
            MESSAGE_ID_COLUMN_NAME, CHANNEL_ID_COLUMN_NAME, DATE_COLUMN_NAME, CONNECTOR_COLUMN_NAME, STATUS_COLUMN_NAME
        }
        ) {
            boolean[] canEdit = new boolean []
            {
                false, false, false, false, false, false, false
            };
            
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        
        eventTable.setSelectionMode(0);        
        
//        eventTable.getColumnExt(MESSAGE_ID_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
//        eventTable.getColumnExt(CHANNEL_ID_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        eventTable.getColumnExt(DATE_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        eventTable.getColumnExt(CONNECTOR_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        eventTable.getColumnExt(STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        
        eventTable.getColumnExt(MESSAGE_ID_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        eventTable.getColumnExt(MESSAGE_ID_COLUMN_NAME).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        eventTable.getColumnExt(CHANNEL_ID_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        eventTable.getColumnExt(CHANNEL_ID_COLUMN_NAME).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);  
        
        eventTable.packTable(UIConstants.COL_MARGIN);    
        
        eventTable.setRowHeight(UIConstants.ROW_HEIGHT);
        eventTable.setOpaque(true);
        eventTable.setRowSelectionAllowed(true);
        clearDescription();
        
        if(Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            eventTable.setHighlighters(highlighter);
        }
        
        eventPane.setViewportView(eventTable);
        
        eventTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                EventListSelected(evt);
            }
        });
        
        eventTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showEventPopupMenu(evt, true);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showEventPopupMenu(evt, true);
            }
        });
    }
    
    private void makeMappingsTable(String[][] tableData)
    {
        JXTable mappingsTable = new JXTable();
        
        
        
        mappingsTable.setModel(new javax.swing.table.DefaultTableModel(
            tableData,
            new String []
            {
                KEY_COLUMN_NAME, VALUE_COLUMN_NAME
            }
        )
        {
            boolean[] canEdit = new boolean []
            {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        
        mappingsPane.setViewportView(mappingsTable);
    }
    
    /**
     * Shows the trigger button (right-click) popup menu.
     */
    private void showEventPopupMenu(java.awt.event.MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = eventTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                eventTable.setRowSelectionInterval(row, row);
            }
            else
                deselectRows();
            parent.eventPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    /**
     * Deselects all rows in the table and clears the description information.
     */
    public void deselectRows()
    {
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 2, -1, false);
        eventTable.clearSelection();
        clearDescription();
    }
    
    /**
     * Clears all description information.
     */
    public void clearDescription()
    {
//        RawMessageTextPane.setDocument(new HighlightedDocument());
        RawMessageTextPane.setText("Select a message to view the raw message.");
//        TransformedMessageTextPane.setDocument(new HighlightedDocument());
        TransformedMessageTextPane.setText("Select a message to view the transformed message.");
//        EncodedMessageTextPane.setDocument(new HighlightedDocument());
        EncodedMessageTextPane.setText("Select a message to view the encoded message.");
    }
    
    /**
     * An action for when a row is selected in the table.
     */
    private void EventListSelected(ListSelectionEvent evt)
    {
        if (!evt.getValueIsAdjusting())
        {
            int row = eventTable.getSelectedRow();

            if(row >= 0)
            {
                parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 2, -1, true);
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                MessageObject currentMessage = messageObjectList.get(row);
                
                setCorrectDocument(RawMessageTextPane, currentMessage.getRawData(), currentMessage.getRawDataProtocol());
                setCorrectDocument(TransformedMessageTextPane, currentMessage.getTransformedData(), currentMessage.getTransformedDataProtocol());
                setCorrectDocument(EncodedMessageTextPane, currentMessage.getEncodedData(), currentMessage.getEncodedDataProtocol());
                
                Map variableMap = currentMessage.getVariableMap();
                Iterator variableMapSetIterator = variableMap.entrySet().iterator();
                                
                String[][] tableData = new String[variableMap.size()][2];
                
                for (int i=0; variableMapSetIterator.hasNext(); i++)
                {
                    Entry variableMapEntry = (Entry)variableMapSetIterator.next();
                    tableData[i][0] = (String)variableMapEntry.getKey();
                    tableData[i][1] = (String)variableMapEntry.getValue();
                }
                
                makeMappingsTable(tableData);
                
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }
    
    private void setCorrectDocument(MirthTextPane textPane, String message, MessageObject.Protocol protocol)
    {
/*
        if (protocol.equals(MessageObject.Protocol.HL7))
            textPane.setDocument(new HighlightedDocument());
        else if (protocol.equals(MessageObject.Protocol.XML))
            textPane.setDocument(new HighlightedDocument());
        else if (protocol.equals(MessageObject.Protocol.X12))
            textPane.setDocument(new HighlightedDocument());
*/        
        textPane.setText(message.replaceAll("\r", "\n"));
        
        textPane.setCaretPosition(0);
    }
    
    /**
     * Returns the ID of the selected message in the table.
     */
    public String getSelectedMessageID()
    {
        int column = -1;
        for (int i = 0; i < eventTable.getColumnCount(); i++)
        {
            if (eventTable.getColumnName(i).equals(MESSAGE_ID_COLUMN_NAME))
                column = i;
        }
        return ((String)eventTable.getValueAt(eventTable.getSelectedRow(), column));
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        filterPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        filterButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        mirthDatePicker1 = new com.webreach.mirth.client.ui.components.MirthDatePicker();
        mirthDatePicker2 = new com.webreach.mirth.client.ui.components.MirthDatePicker();
        statusComboBox = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        previousPageButton = new javax.swing.JButton();
        nextPageButton = new javax.swing.JButton();
        pageSizeField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel6 = new javax.swing.JLabel();
        resultsLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        destinationField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        descriptionPanel = new javax.swing.JPanel();
        descriptionTabbedPane = new javax.swing.JTabbedPane();
        RawMessagePanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        RawMessageTextPane = new com.webreach.mirth.client.ui.components.MirthTextPane();
        TransformedMessagePanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        TransformedMessageTextPane = new com.webreach.mirth.client.ui.components.MirthTextPane();
        EncodedMessagePanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        EncodedMessageTextPane = new com.webreach.mirth.client.ui.components.MirthTextPane();
        MappingsPanel = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 255, 255));
        filterPanel.setBackground(new java.awt.Color(255, 255, 255));
        filterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filter By", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        jLabel3.setText("Start Date:");

        filterButton.setText("Filter");
        filterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("End Date:");

        jLabel5.setText("Status:");

        previousPageButton.setText("Previous Page");
        previousPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousPageButtonActionPerformed(evt);
            }
        });

        nextPageButton.setText("Next Page");
        nextPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextPageButtonActionPerformed(evt);
            }
        });

        jLabel6.setText("Page Size:");

        resultsLabel.setForeground(new java.awt.Color(204, 0, 0));
        resultsLabel.setText("Results");

        jLabel1.setText("Destination:");

        org.jdesktop.layout.GroupLayout filterPanelLayout = new org.jdesktop.layout.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel1)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, filterPanelLayout.createSequentialGroup()
                        .add(destinationField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 104, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(24, 24, 24)
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(statusComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 186, Short.MAX_VALUE)
                        .add(resultsLabel))
                    .add(filterPanelLayout.createSequentialGroup()
                        .add(filterButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 228, Short.MAX_VALUE)
                        .add(previousPageButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nextPageButton))
                    .add(filterPanelLayout.createSequentialGroup()
                        .add(mirthDatePicker1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(16, 16, 16)
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mirthDatePicker2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 73, Short.MAX_VALUE)
                        .add(jLabel6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pageSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(30, 30, 30))
        );

        filterPanelLayout.linkSize(new java.awt.Component[] {nextPageButton, previousPageButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterPanelLayout.createSequentialGroup()
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(filterPanelLayout.createSequentialGroup()
                        .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel1)
                            .add(destinationField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel5)
                            .add(statusComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(mirthDatePicker1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel3)
                            .add(jLabel2)
                            .add(mirthDatePicker2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(filterPanelLayout.createSequentialGroup()
                        .add(resultsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(pageSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel6))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(filterButton)
                    .add(previousPageButton)
                    .add(nextPageButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
        );

        descriptionPanel.setBackground(new java.awt.Color(255, 255, 255));
        descriptionTabbedPane.setFocusable(false);
        RawMessagePanel.setBackground(new java.awt.Color(255, 255, 255));
        RawMessagePanel.setFocusable(false);
        RawMessageTextPane.setEditable(false);
        jScrollPane3.setViewportView(RawMessageTextPane);

        org.jdesktop.layout.GroupLayout RawMessagePanelLayout = new org.jdesktop.layout.GroupLayout(RawMessagePanel);
        RawMessagePanel.setLayout(RawMessagePanelLayout);
        RawMessagePanelLayout.setHorizontalGroup(
            RawMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(RawMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
                .addContainerGap())
        );
        RawMessagePanelLayout.setVerticalGroup(
            RawMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(RawMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionTabbedPane.addTab("Raw Message", RawMessagePanel);

        TransformedMessagePanel.setBackground(new java.awt.Color(255, 255, 255));
        TransformedMessagePanel.setFocusable(false);
        TransformedMessageTextPane.setEditable(false);
        jScrollPane2.setViewportView(TransformedMessageTextPane);

        org.jdesktop.layout.GroupLayout TransformedMessagePanelLayout = new org.jdesktop.layout.GroupLayout(TransformedMessagePanel);
        TransformedMessagePanel.setLayout(TransformedMessagePanelLayout);
        TransformedMessagePanelLayout.setHorizontalGroup(
            TransformedMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(TransformedMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
                .addContainerGap())
        );
        TransformedMessagePanelLayout.setVerticalGroup(
            TransformedMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(TransformedMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionTabbedPane.addTab("Transformed Message", TransformedMessagePanel);

        EncodedMessagePanel.setBackground(new java.awt.Color(255, 255, 255));
        EncodedMessagePanel.setFocusable(false);
        EncodedMessageTextPane.setEditable(false);
        jScrollPane4.setViewportView(EncodedMessageTextPane);

        org.jdesktop.layout.GroupLayout EncodedMessagePanelLayout = new org.jdesktop.layout.GroupLayout(EncodedMessagePanel);
        EncodedMessagePanel.setLayout(EncodedMessagePanelLayout);
        EncodedMessagePanelLayout.setHorizontalGroup(
            EncodedMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(EncodedMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
                .addContainerGap())
        );
        EncodedMessagePanelLayout.setVerticalGroup(
            EncodedMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(EncodedMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionTabbedPane.addTab("Encoded Message", EncodedMessagePanel);

        MappingsPanel.setBackground(new java.awt.Color(255, 255, 255));
        MappingsPanel.setFocusable(false);
        org.jdesktop.layout.GroupLayout MappingsPanelLayout = new org.jdesktop.layout.GroupLayout(MappingsPanel);
        MappingsPanel.setLayout(MappingsPanelLayout);
        MappingsPanelLayout.setHorizontalGroup(
            MappingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 606, Short.MAX_VALUE)
        );
        MappingsPanelLayout.setVerticalGroup(
            MappingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 146, Short.MAX_VALUE)
        );
        descriptionTabbedPane.addTab("Mappings", MappingsPanel);

        org.jdesktop.layout.GroupLayout descriptionPanelLayout = new org.jdesktop.layout.GroupLayout(descriptionPanel);
        descriptionPanel.setLayout(descriptionPanelLayout);
        descriptionPanelLayout.setHorizontalGroup(
            descriptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(descriptionTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)
        );
        descriptionPanelLayout.setVerticalGroup(
            descriptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(descriptionTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(descriptionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(filterPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(descriptionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void nextPageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextPageButtonActionPerformed
        makeEventTable(messageListHandler, NEXT_PAGE);
    }//GEN-LAST:event_nextPageButtonActionPerformed

    private void previousPageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousPageButtonActionPerformed
        makeEventTable(messageListHandler, PREVIOUS_PAGE);
    }//GEN-LAST:event_previousPageButtonActionPerformed

    /**
     * An action when the filter button is pressed.  Creates
     * the actual filter and remakes the table with that filter.
     */
    private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterButtonActionPerformed
        if (mirthDatePicker1.getDate() != null && mirthDatePicker2.getDate() != null)
        {
            if (mirthDatePicker1.getDateInMillis() > mirthDatePicker2.getDateInMillis())
            {
                JOptionPane.showMessageDialog(parent, "Start date cannot be after the end date.");
                return;
            }
        }
        
        messageObjectFilter = new MessageObjectFilter();
        
        messageObjectFilter.setChannelId(parent.status.get(parent.statusListPage.getSelectedStatus()).getChannelId());
        
        if (!destinationField.getText().equals(""))
            messageObjectFilter.setConnectorName(destinationField.getText());        
        
        if (!((String)statusComboBox.getSelectedItem()).equalsIgnoreCase("ALL"))
        {
            for (int i = 0; i < MessageObject.Status.values().length; i++)
            {
                if (((String)statusComboBox.getSelectedItem()).equalsIgnoreCase(MessageObject.Status.values()[i].toString()))
                    messageObjectFilter.setStatus(MessageObject.Status.values()[i]);
            }
        }
        
        if (mirthDatePicker1.getDate() != null)
        {
            Calendar calendarStart = Calendar.getInstance();
            calendarStart.setTimeInMillis(mirthDatePicker1.getDateInMillis());
            messageObjectFilter.setStartDate(calendarStart);
        }
        if (mirthDatePicker2.getDate() != null)
        {
            Calendar calendarEnd = Calendar.getInstance();
            calendarEnd.setTimeInMillis(mirthDatePicker2.getDateInMillis());
            messageObjectFilter.setEndDate(calendarEnd);
        }
        
        if (!pageSizeField.getText().equals(""))
            messageObjectFilter.setPageSize(Integer.parseInt(pageSizeField.getText()));
        
        messageListHandler = parent.mirthClient.getMessageListHandler(messageObjectFilter);
        
        makeEventTable(messageListHandler, FIRST_PAGE);
    }//GEN-LAST:event_filterButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel EncodedMessagePanel;
    private com.webreach.mirth.client.ui.components.MirthTextPane EncodedMessageTextPane;
    private javax.swing.JPanel MappingsPanel;
    private javax.swing.JPanel RawMessagePanel;
    private com.webreach.mirth.client.ui.components.MirthTextPane RawMessageTextPane;
    private javax.swing.JPanel TransformedMessagePanel;
    private com.webreach.mirth.client.ui.components.MirthTextPane TransformedMessageTextPane;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JTabbedPane descriptionTabbedPane;
    private com.webreach.mirth.client.ui.components.MirthTextField destinationField;
    private javax.swing.JButton filterButton;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable1;
    private com.webreach.mirth.client.ui.components.MirthDatePicker mirthDatePicker1;
    private com.webreach.mirth.client.ui.components.MirthDatePicker mirthDatePicker2;
    private javax.swing.JButton nextPageButton;
    private com.webreach.mirth.client.ui.components.MirthTextField pageSizeField;
    private javax.swing.JButton previousPageButton;
    private javax.swing.JLabel resultsLabel;
    private javax.swing.JComboBox statusComboBox;
    // End of variables declaration//GEN-END:variables
    
}
