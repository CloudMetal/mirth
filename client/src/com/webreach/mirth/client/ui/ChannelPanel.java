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

import java.awt.Point;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.model.Channel;

/** The main channel list panel view. */
public class ChannelPanel extends javax.swing.JPanel
{
    private final String STATUS_COLUMN_NAME = "Status";
    private final String NAME_COLUMN_NAME = "Name";
    private final String ID_COLUMN_NAME = "Id";
    private final int ID_COLUMN_NUMBER = 4;
    private final String ENABLED_STATUS = "Enabled";
    
    private JScrollPane channelPane;
    private JXTable channelTable;
    private Frame parent;
    
    /** Creates new form ChannelPanel */
    public ChannelPanel() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
    }
    
    /** Initializes the pane, makes the table, adds the mouse listeners, and sets the layout */
    public void initComponents()
    {
        channelPane = new JScrollPane();
        
        makeChannelTable();
        
        channelPane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showChannelPopupMenu(evt, false);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showChannelPopupMenu(evt, false);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectRows();
            }
        });
        
        channelPane.setComponentPopupMenu(parent.channelPopupMenu);
        
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(channelPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, channelTable.getWidth(), Short.MAX_VALUE)
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(channelPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, channelTable.getHeight(), Short.MAX_VALUE)
                );
    }
    
    /** Creates the channel table */
    public void makeChannelTable()
    {
        channelTable = new JXTable();
        Object[][] tableData = null;
        channelPane.setBorder(BorderFactory.createEmptyBorder());
        channelTable.setBorder(BorderFactory.createEmptyBorder());
        if(parent.channels != null)
        {
            tableData = new Object[parent.channels.size()][5];
            
            int i = 0;
            for (Channel channel : parent.channels.values())
            {
                if (channel.isEnabled())
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_blue.png")),"Enabled");
                else
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_black.png")),"Disabled");

                tableData[i][1] = channel.getName();
                tableData[i][2] = channel.getId();
                i++;
            }
        }
            
        channelTable.setModel(new javax.swing.table.DefaultTableModel(
            tableData,
            new String []
        {
            STATUS_COLUMN_NAME, NAME_COLUMN_NAME, ID_COLUMN_NAME
        }
        ) {
            boolean[] canEdit = new boolean []
            {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
                
        channelTable.setSelectionMode(0);        
        
        // Must set the maximum width on columns that should be packed.
        channelTable.getColumnExt(STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        
        channelTable.getColumnExt(STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());
        channelTable.getColumnExt(ID_COLUMN_NAME).setVisible(false);
        channelTable.packTable(UIConstants.COL_MARGIN);

        channelTable.setRowHeight(UIConstants.ROW_HEIGHT);
        channelTable.setOpaque(true);
        channelTable.setRowSelectionAllowed(true);
        
        // Set highlighter.
        if(Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            channelTable.setHighlighters(highlighter);
        }
        
        channelPane.setViewportView(channelTable);
        
        channelTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                ChannelListSelected(evt);
            }
        });
        
        // listen for trigger button and double click to edit channel.
        channelTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showChannelPopupMenu(evt, true);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showChannelPopupMenu(evt, true);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                if (evt.getClickCount() >= 2)
                    parent.doEditChannel();
            }
        });
    }
    
    /** Show the popup menu on trigger button press (right-click).
     *  If it's on the table then the row should be selected, if not
     *  any selected rows should be deselected first.
     */
    private void showChannelPopupMenu(java.awt.event.MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = channelTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                channelTable.setRowSelectionInterval(row, row);
            }
            else
                deselectRows();
            parent.channelPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    /** The action called when a Channel is selected.  Sets tasks as well. */
    private void ChannelListSelected(ListSelectionEvent evt)
    {
        int row = channelTable.getSelectedRow();
        
        if(row >= 0)
        {
            parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 5, -1, true);

            int columnNumber = getColumnNumber(STATUS_COLUMN_NAME);
            if (((CellData)channelTable.getValueAt(row, columnNumber)).getText().equals(ENABLED_STATUS))
                parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 9, 9, false);
            else
                parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 10, 10, false);
        }
    }
    
    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectRows()
    {
        channelTable.clearSelection();
        parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 5, -1, false);
    }
    
    /** Gets the selected channel index that corresponds to the saved channels list */
    public Channel getSelectedChannel()
    {
        if (channelTable.getSelectedRow() != -1)
        {
            String channelId = (String) channelTable.getModel().getValueAt(channelTable.convertRowIndexToModel(channelTable.getSelectedRow()), ID_COLUMN_NUMBER);
            return parent.channels.get(channelId);
        }
        
        return null;
    }
    
    /** Sets a channel to be selected by taking it's id */
    public boolean setSelectedChannel(String channelId)
    {
        int i = 0;
        for (Channel channel : parent.channels.values())
        {
            if (channelId.equals(channelTable.getModel().getValueAt(i, ID_COLUMN_NUMBER)))
            {
                int row = channelTable.convertRowIndexToView(i);
                channelTable.setRowSelectionInterval(row,row);
                return true;
            }
            i++;
        }
        return false;
    }
    
    /** Gets a column index by taking it's name */
    private int getColumnNumber(String name)
    {
        for (int i = 0; i < channelTable.getColumnCount(); i++)
        {
            if (channelTable.getColumnName(i).equalsIgnoreCase(name))
                return i;
        }
        return -1;
    }
}
