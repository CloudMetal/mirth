/*
 * DashboardPanel.java
 *
 * Created on February 22, 2007, 12:57 PM
 */

package com.webreach.mirth.client.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.ConditionalHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.ExtensionPoint;
import com.webreach.mirth.model.ExtensionPointDefinition;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.util.MessageVocabulary;
import com.webreach.mirth.plugins.DashboardColumnPlugin;
import com.webreach.mirth.plugins.TransformerStepPlugin;

/**
 *
 * @author brendanh
 */
public class DashboardPanel extends javax.swing.JPanel
{
    private final String STATUS_COLUMN_NAME = "Status";
    private final String NAME_COLUMN_NAME = "Name";
    private final String RECEIVED_COLUMN_NAME = "Received";
    private final String QUEUED_COLUMN_NAME = "Queued";
    private final String SENT_COLUMN_NAME = "Sent";
    private final String ERROR_COLUMN_NAME = "Errored";
    private final String FILTERED_COLUMN_NAME = "Filtered";
    private int lastRow;
    private Frame parent;
    private Map<String, DashboardColumnPlugin> loadedColumnPlugins = new HashMap<String, DashboardColumnPlugin>();
    
    /** Creates new form DashboardPanel */
    public DashboardPanel()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        lastRow = -1;
        initComponents();
        statusPane.setDoubleBuffered(true);
        loadPlugins();
        makeStatusTable();
        statusPane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showStatusPopupMenu(evt, false);
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showStatusPopupMenu(evt, false);
            }
            
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectRows();
            }
        });
        this.setDoubleBuffered(true);
    }
    // Extension point for ExtensionPoint.Type.CLIENT_VOCABULARY
    @ExtensionPointDefinition(mode = ExtensionPoint.Mode.CLIENT, type = ExtensionPoint.Type.CLIENT_DASHBOARD_COLUMN)
    public void loadPlugins()
    {
        try
        {
            Map<String, PluginMetaData> plugins = parent.mirthClient.getPluginMetaData();
            for (PluginMetaData metaData : plugins.values())
            {
                if (metaData.isEnabled())
                {
                    for (ExtensionPoint extensionPoint : metaData.getExtensionPoints())
                    {
                        try
                        {
                            if (extensionPoint.getMode().equals(ExtensionPoint.Mode.CLIENT) && extensionPoint.getType().equals(ExtensionPoint.Type.CLIENT_DASHBOARD_COLUMN) && extensionPoint.getClassName() != null && extensionPoint.getClassName().length() > 0)
                            {
                                String pluginName = extensionPoint.getName();
                                DashboardColumnPlugin columnPlugin = (DashboardColumnPlugin) Class.forName(extensionPoint.getClassName()).getDeclaredConstructors()[0].newInstance(new Object[]{pluginName, this});
                                loadedColumnPlugins.put(columnPlugin.getColumnHeader(), columnPlugin);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    /**
     * Makes the status table with all current server information.
     */
    public void makeStatusTable()
    {
        updateTable();
        
        statusTable.setDoubleBuffered(true);
        
        statusTable.setSelectionMode(0);
        
        statusTable.getColumnExt(STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        statusTable.getColumnExt(RECEIVED_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        statusTable.getColumnExt(SENT_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        statusTable.getColumnExt(ERROR_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        statusTable.getColumnExt(FILTERED_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        statusTable.getColumnExt(QUEUED_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        
        statusTable.getColumnExt(STATUS_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(RECEIVED_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(SENT_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(ERROR_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(FILTERED_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(QUEUED_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(NAME_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        
        statusTable.getColumnExt(STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());
        statusTable.getColumnExt(RECEIVED_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        statusTable.getColumnExt(SENT_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        statusTable.getColumnExt(ERROR_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        statusTable.getColumnExt(FILTERED_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        statusTable.getColumnExt(QUEUED_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        
        statusTable.getColumnExt(RECEIVED_COLUMN_NAME).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        statusTable.getColumnExt(SENT_COLUMN_NAME).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        statusTable.getColumnExt(ERROR_COLUMN_NAME).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        statusTable.getColumnExt(FILTERED_COLUMN_NAME).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        statusTable.getColumnExt(QUEUED_COLUMN_NAME).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        
        for(DashboardColumnPlugin plugin : loadedColumnPlugins.values())
        {
            String columnName = plugin.getColumnHeader();
            statusTable.getColumnExt(columnName).setMaxWidth(plugin.getMaxWidth());;
            statusTable.getColumnExt(columnName).setMinWidth(plugin.getMinWidth());
            statusTable.getColumnExt(columnName).setCellRenderer(plugin.getCellRenderer());
            statusTable.getColumnExt(columnName).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        }
        
        statusTable.packTable(UIConstants.COL_MARGIN);
        
        statusTable.setRowHeight(UIConstants.ROW_HEIGHT);
        statusTable.setOpaque(true);
        statusTable.setRowSelectionAllowed(true);
        
        statusTable.setSortable(true);
        
        statusPane.setViewportView(statusTable);
        
        statusTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                StatusListSelected(evt);
            }
        });
        statusTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showStatusPopupMenu(evt, true);
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showStatusPopupMenu(evt, true);
            }
            
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                if (evt.getClickCount() >= 2)
                    parent.doShowMessages();
            }
        });
    }
    
    public synchronized void updateTable()
    {
        Object[][] tableData = null;
        
        if (parent.status != null)
        {
            tableData = new Object[parent.status.size()][7 + loadedColumnPlugins.size()];
            for (int i = 0; i < parent.status.size(); i++)
            {
                ChannelStatus tempStatus = parent.status.get(i);
                try
                {
                    ChannelStatistics tempStats = parent.mirthClient.getStatistics(tempStatus.getChannelId());
                    tableData[i][2] = tempStats.getReceived();
                    tableData[i][3] = tempStats.getFiltered();
                    tableData[i][4] = tempStats.getQueued();
                    tableData[i][5] = tempStats.getSent();
                    tableData[i][6] = tempStats.getError();
                    int j = 7;
                    for (DashboardColumnPlugin plugin : loadedColumnPlugins.values())
                    {
                        tableData[i][j] = plugin.getTableData(tempStatus);
                        j++;
                    }
                }
                catch (ClientException ex)
                {
                    parent.alertException(ex.getStackTrace(), ex.getMessage());
                }
                
                if (tempStatus.getState() == ChannelStatus.State.STARTED)
                    tableData[i][0] = new CellData(new ImageIcon(Frame.class.getResource("images/bullet_green.png")), "Started");
                else if (tempStatus.getState() == ChannelStatus.State.STOPPED)
                    tableData[i][0] = new CellData(new ImageIcon(Frame.class.getResource("images/bullet_red.png")), "Stopped");
                else if (tempStatus.getState() == ChannelStatus.State.PAUSED)
                    tableData[i][0] = new CellData(new ImageIcon(Frame.class.getResource("images/bullet_yellow.png")), "Paused");
                
                tableData[i][1] = tempStatus.getName();
                
            }
            
        }
        
        if (statusTable != null)
        {
            lastRow = statusTable.getSelectedRow();
            RefreshTableModel model = (RefreshTableModel) statusTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            statusTable = new MirthTable();
            String[] defaultColumns = new String[] { STATUS_COLUMN_NAME,
            NAME_COLUMN_NAME, RECEIVED_COLUMN_NAME,
            FILTERED_COLUMN_NAME, QUEUED_COLUMN_NAME,
            SENT_COLUMN_NAME, ERROR_COLUMN_NAME };
            ArrayList<String> columns = new ArrayList<String>();
            for (int i = 0; i < defaultColumns.length; i++)
            {
                columns.add(defaultColumns[i]);
            }
            for (DashboardColumnPlugin plugin : loadedColumnPlugins.values())
            {
                columns.add(plugin.getColumnHeader());
            }
            
            statusTable.setModel(new RefreshTableModel(tableData, columns.toArray(new String[0]))
            {
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return false;
                }
            });
        }
        
        if (lastRow >= 0 && lastRow < statusTable.getRowCount())
            statusTable.setRowSelectionInterval(lastRow, lastRow);
        else
            lastRow = UIConstants.ERROR_CONSTANT;
        
        // Add the highlighters.  Always add the error highlighter.
        HighlighterPipeline highlighter = new HighlighterPipeline();
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
        }
        
        Highlighter errorHighlighter = new ConditionalHighlighter(Color.PINK, null, -1, -1)
        {
            @Override
            protected Color computeBackground(Component arg0, ComponentAdapter arg1)
            {
                arg0.setBackground(Color.PINK);
                return super.computeBackground(arg0, arg1);
            }
            
            @Override
            protected boolean test(ComponentAdapter adapter)
            {
                if (adapter.column == statusTable.getColumnNumber(ERROR_COLUMN_NAME))
                {
                    if (((Integer)statusTable.getValueAt(adapter.row, adapter.column)).intValue() > 0)
                        return true;
                }
                return false;
            }
        };
        
        highlighter.addHighlighter(errorHighlighter);
        statusTable.setHighlighters(highlighter);
    }
    
    /**
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed.
     */
    private void showStatusPopupMenu(java.awt.event.MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = statusTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                statusTable.setRowSelectionInterval(row, row);
            }
            else
                deselectRows();
            parent.statusPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    /*
     * Action when something on the status list has been selected. Sets all
     * appropriate tasks visible.
     */
    private void StatusListSelected(ListSelectionEvent evt)
    {
        int row = statusTable.getSelectedRow();
        if (row >= 0 && row < statusTable.getRowCount())
        {
            int columnNumber = statusTable.getColumnNumber(STATUS_COLUMN_NAME);
            parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 2, 5, true);
            
            if (((CellData) statusTable.getValueAt(row, columnNumber)).getText().equals("Started"))
            {
                
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 6, 6, false);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 7, 7, true);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 8, 8, true);
            }
            else if (((CellData) statusTable.getValueAt(row, columnNumber)).getText().equals("Paused"))
            {
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 6, 6, true);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 7, 7, false);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 8, 8, true);
            }
            else
            {
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 2, 2, false);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 6, 6, true);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 7, 7, false);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 8, 8, false);
            }
        }
    }
    
    /**
     * Gets the index of the selected status row.
     */
    public synchronized int getSelectedStatus()
    {
        for (int i = 0; i < parent.status.size(); i++)
        {
            if (statusTable.getSelectedRow() > -1 && ((String) statusTable.getValueAt(statusTable.getSelectedRow(), statusTable.getColumnNumber(NAME_COLUMN_NAME))).equalsIgnoreCase(parent.status.get(i).getName()))
                return i;
        }
        return UIConstants.ERROR_CONSTANT;
    }
    
    public void deselectRows()
    {
        statusTable.deselectRows();
        parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 2, -1, false);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        statusPane = new javax.swing.JScrollPane();
        statusTable = null;

        statusPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        statusPane.setViewportView(statusTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(statusPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(statusPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE));
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane statusPane;

    private com.webreach.mirth.client.ui.components.MirthTable statusTable;
    // End of variables declaration//GEN-END:variables
    
}
