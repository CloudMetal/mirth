/*
 * DashboardPanel.java
 *
 * Created on February 22, 2007, 12:57 PM
 */

package com.webreach.mirth.client.ui;

import java.awt.Point;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.model.ChannelStatus;

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
    private final String ERROR_COLUMN_NAME = "Errors";
    private final String FILTERED_COLUMN_NAME = "Filtered";
    private int lastRow;
    private Frame parent;

    /** Creates new form DashboardPanel */
    public DashboardPanel()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        lastRow = -1;
        initComponents();
        statusPane.setDoubleBuffered(true);
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

        statusTable.packTable(UIConstants.COL_MARGIN);

        statusTable.setRowHeight(UIConstants.ROW_HEIGHT);
        statusTable.setOpaque(true);
        statusTable.setRowSelectionAllowed(true);

        statusTable.setSortable(true);

        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            statusTable.setHighlighters(highlighter);
        }

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

    public void updateTable()
    {
        Object[][] tableData = null;

        if (parent.status != null)
        {
            tableData = new Object[parent.status.size()][7];
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
                }
                catch (ClientException ex)
                {
                    ex.printStackTrace();
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
            statusTable.setModel(new RefreshTableModel(tableData, new String[] { STATUS_COLUMN_NAME, NAME_COLUMN_NAME, RECEIVED_COLUMN_NAME, FILTERED_COLUMN_NAME, QUEUED_COLUMN_NAME, SENT_COLUMN_NAME, ERROR_COLUMN_NAME })
            {
                boolean[] canEdit = new boolean[] { false, false, false, false, false, false, false, false };

                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }

        if (lastRow >= 0 && lastRow < statusTable.getRowCount())
            statusTable.setRowSelectionInterval(lastRow, lastRow);
        else
            lastRow = UIConstants.ERROR_CONSTANT;
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
            parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 2, -1, true);

            int columnNumber = statusTable.getColumnNumber(STATUS_COLUMN_NAME);
            if (((CellData) statusTable.getValueAt(row, columnNumber)).getText().equals("Started"))
            {
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 3, 4, true);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 5, 5, false);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 6, 6, true);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 7, 7, true);
            }
            else if (((CellData) statusTable.getValueAt(row, columnNumber)).getText().equals("Paused"))
            {
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 3, 4, true);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 5, 5, true);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 6, 6, false);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 7, 7, true);
            }
            else
            {
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 3, 4, true);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 5, 5, true);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 6, 6, false);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 7, 7, false);
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
