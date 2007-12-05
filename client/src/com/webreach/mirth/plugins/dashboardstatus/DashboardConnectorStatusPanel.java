/*
 * DashboardConnectorStatusPanel.java
 *
 * Created on October 10, 2007, 3:40 PM
 */

package com.webreach.mirth.plugins.dashboardstatus;

import com.webreach.mirth.client.ui.*;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.prefs.Preferences;
import java.awt.event.*;

import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;

import javax.swing.*;

/**
 *
 * @author  chrisr
 */

public class DashboardConnectorStatusPanel extends javax.swing.JPanel {

    private static final String ID_COLUMN_HEADER = "Id";
    private static final String CHANNEL_COLUMN_HEADER = "Channel";
    private static final String TIME_COLUMN_HEADER = "Timestamp";
    private static final String CONNECTOR_INFO_COLUMN_HEADER = "Connector Info";
    private static final String EVENT_COLUMN_HEADER = "Event";
    private static final String INFORMATION_COLUMN_HEADER = "Info";
    
    private static final int PAUSED = 0;
    private static final int RESUMED = 1;
    private HashMap<String, Integer> channelStates = new HashMap<String, Integer>();
    
    private JPopupMenu rightclickPopup;

    private ImageIcon greenBullet;      //  CONNECTED
    private ImageIcon yellowBullet;     //  BUSY
    private ImageIcon redBullet;        //  DISCONNECTED
    private ImageIcon blueBullet;       //  INITIALIZED
    private ImageIcon blackBullet;	    //  DONE
    private static final String NO_CHANNEL_SELECTED = "No Channel Selected";
    private String selectedChannel;
    private DashboardConnectorStatusClient dcsc;
    private Preferences userPreferences;
    private Frame parent;
    private int currentDashboardLogSize;


    /** Creates new form DashboardConnectorStatusPanel */
    public DashboardConnectorStatusPanel(DashboardConnectorStatusClient dcsc)
    {
        this.parent = PlatformUI.MIRTH_FRAME;        
        this.dcsc = dcsc;
        greenBullet = new ImageIcon(Frame.class.getResource("images/bullet_green.png"));
        yellowBullet = new ImageIcon(Frame.class.getResource("images/bullet_yellow.png"));
        redBullet = new ImageIcon(Frame.class.getResource("images/bullet_red.png"));
        blueBullet = new ImageIcon(Frame.class.getResource("images/bullet_blue.png"));
        blackBullet = new ImageIcon(Frame.class.getResource("images/bullet_black.png"));
        channelStates.put(NO_CHANNEL_SELECTED, RESUMED);

        initComponents();
        
        clearLog.setIcon(UIConstants.CLEAR_LOG_ICON);
        clearLog.setToolTipText("Clear Displayed Log");

        logSizeChange.setIcon(UIConstants.CHANGE_LOGSIZE_ICON);
        logSizeChange.setToolTipText("Change Log Display Size");

        makeLogTable();

        logSizeTextField.setDocument(new MirthFieldConstraints(3, false, false, true));     // max 999. all numbers. default to 250.
        userPreferences = Preferences.systemNodeForPackage(Mirth.class);
        currentDashboardLogSize = userPreferences.getInt("dashboardLogSize", 250);
        logSizeTextField.setText(currentDashboardLogSize + "");

    }

    /**
     * Makes the status table with all current server information.
     */
    public void makeLogTable()
    {
        updateTable(null, false);
        logTable.setDoubleBuffered(true);
        logTable.setSelectionMode(0);
        logTable.getColumnExt(ID_COLUMN_HEADER).setVisible(false);
        logTable.getColumnExt(EVENT_COLUMN_HEADER).setCellRenderer(new ImageCellRenderer());
        logTable.packTable(UIConstants.COL_MARGIN);
        logTable.setRowHeight(UIConstants.ROW_HEIGHT);
        logTable.setOpaque(true);
        logTable.setRowSelectionAllowed(false);
        logTable.setSortable(true);
        logTable.setFocusable(false);
        logTable.setHorizontalScrollEnabled(true);
        logTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);

        logTable.getColumnExt(TIME_COLUMN_HEADER).setMinWidth(140);
        logTable.getColumnExt(TIME_COLUMN_HEADER).setMaxWidth(140);
        logTable.getColumnExt(EVENT_COLUMN_HEADER).setMinWidth(110);
        logTable.getColumnExt(EVENT_COLUMN_HEADER).setMaxWidth(110);

        createPopupMenu();
        
        jScrollPane1.setViewportView(logTable);
    }

    public void createPopupMenu() {
        JMenuItem menuItem;

        //Create the popup menu.
        rightclickPopup = new JPopupMenu();
        menuItem = new JMenuItem("Pause Log");
        menuItem.setIcon(UIConstants.PAUSE_LOG_ICON);
        menuItem.addActionListener(new PauseResumeActionListener());
        rightclickPopup.add(menuItem);
        menuItem = new JMenuItem("Resume Log");
        menuItem.setIcon(UIConstants.RESUME_LOG_ICON);
        menuItem.addActionListener(new PauseResumeActionListener());
        rightclickPopup.add(menuItem);
        rightclickPopup.addSeparator();
        menuItem = new JMenuItem("Clear Log");
        menuItem.setIcon(UIConstants.CLEAR_LOG_ICON);
        menuItem.addActionListener(new ClearLogActionListener());
        rightclickPopup.add(menuItem);

        // initially show 'Pause', hide 'Resume'
        rightclickPopup.getComponent(0).setVisible(true);
        rightclickPopup.getComponent(1).setVisible(false);

        //Add listener to the text area so the popup menu can come up.
        MouseListener popupListener = new PopupListener(rightclickPopup);
        jScrollPane1.addMouseListener(popupListener);
        logTable.addMouseListener(popupListener);
    }

    class PauseResumeActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e) {
            if (!isPaused(selectedChannel)) {
                channelStates.put(selectedChannel, PAUSED);
            } else {
                channelStates.put(selectedChannel, RESUMED);
            }
            adjustPauseResumeButton(selectedChannel);
        }
    }

    class ClearLogActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e) {
            // "clear log" only affects on the client side.
            // because clearing log on one client should NOT affect other clients' logs.
            // clear logs on client side only.
            dcsc.clearLog(selectedChannel);
        }
    }

    class PopupListener extends MouseAdapter
    {
        JPopupMenu popup;

        PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }

        public void mousePressed(MouseEvent e) {
            checkPopup(e);
        }

        public void mouseClicked(MouseEvent e) {
            checkPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            checkPopup(e);
        }

        private void checkPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public int getCurrentDashboardLogSize() {
        return currentDashboardLogSize;
    }

    public void setSelectedChannel(String channelName) {
        selectedChannel = channelName;
    }

    public boolean isPaused(String channelName) {
        if (channelStates.containsKey(channelName)) {
            return channelStates.get(channelName) == PAUSED;
        } else {
            // first time viewing the channel log. default to RESUME.
            channelStates.put(channelName, RESUMED);
            return false;
        }
    }

    public void adjustPauseResumeButton(String channelName) {
        if (isPaused(channelName)) {
            pauseResume.setIcon(UIConstants.RESUME_LOG_ICON);
            pauseResume.setToolTipText("Resume Log");
            rightclickPopup.getComponent(0).setVisible(false);
            rightclickPopup.getComponent(1).setVisible(true);
        } else {
            pauseResume.setIcon(UIConstants.PAUSE_LOG_ICON);
            pauseResume.setToolTipText("Pause Log");
            rightclickPopup.getComponent(0).setVisible(true);
            rightclickPopup.getComponent(1).setVisible(false);
        }
    }

    /**
     * This method won't be called when it's in the PAUSED state.
     * @param channelLogs
     */
    public synchronized void updateTable(LinkedList<String[]> channelLogs, boolean channelSelected)
    {
        Object[][] tableData;
        if (channelLogs != null)
        {
            tableData = new Object[channelLogs.size()][6];
            for (int i=0; i < channelLogs.size(); i++) {

                tableData[i][0] = channelLogs.get(i)[0];       // Id (hidden)
                tableData[i][1] = channelLogs.get(i)[2];       // Timestamp
                tableData[i][2] = channelLogs.get(i)[1];       // Channel Name (hidden when viewing a specific channel)
                tableData[i][3] = channelLogs.get(i)[3];       // Connector Info

                // Event State - INITIALIZED (blue), CONNECTED (green), BUSY (yellow), DONE (black), DISCONNECTED (red)
                if (channelLogs.get(i)[4].equalsIgnoreCase("INITIALIZED"))
                    tableData[i][4] = new CellData(blueBullet, "Initialized");
                else if (channelLogs.get(i)[4].equalsIgnoreCase("CONNECTED"))
                    tableData[i][4] = new CellData(greenBullet, "Connected");
                else if (channelLogs.get(i)[4].equalsIgnoreCase("BUSY"))
                    tableData[i][4] = new CellData(yellowBullet, "Busy");
                else if (channelLogs.get(i)[4].equalsIgnoreCase("DONE"))
                    tableData[i][4] = new CellData(blackBullet, "Done");
                else if (channelLogs.get(i)[4].equalsIgnoreCase("DISCONNECTED"))
                    tableData[i][4] = new CellData(redBullet, "Disconnected");

                tableData[i][5] = channelLogs.get(i)[5];       // Infomation
            }
        } else {
            tableData = new Object[0][6];
        }

        if (logTable != null)
        {
            RefreshTableModel model = (RefreshTableModel) logTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            logTable = new MirthTable();
            logTable.setModel(new RefreshTableModel(tableData,
                                                       new String[] {ID_COLUMN_HEADER, TIME_COLUMN_HEADER,
                                                                     CHANNEL_COLUMN_HEADER, CONNECTOR_INFO_COLUMN_HEADER,
                                                                     EVENT_COLUMN_HEADER, INFORMATION_COLUMN_HEADER })
            {
                boolean[] canEdit = new boolean[] { false, false, false, false, false, false };

                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }

        // Add the highlighters.  Always add the error highlighter.
        HighlighterPipeline highlighter = new HighlighterPipeline();

        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
        }

        logTable.setHighlighters(highlighter);

        if (channelSelected) {
            logTable.getColumnExt(CHANNEL_COLUMN_HEADER).setVisible(false);
        } else {
            logTable.getColumnExt(CHANNEL_COLUMN_HEADER).setVisible(true);
        }        
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        logSizeChange = new javax.swing.JButton();
        clearLog = new javax.swing.JButton();
        pauseResume = new javax.swing.JButton();
        logSizeTextField = new javax.swing.JTextField();
        logSizeText = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        logTable = null;
        invisibleLabel = new javax.swing.JLabel();

        logSizeChange.setBorderPainted(false);
        logSizeChange.setContentAreaFilled(false);
        logSizeChange.setMargin(new java.awt.Insets(4, 4, 4, 4));
        logSizeChange.setMaximumSize(new java.awt.Dimension(24, 24));
        logSizeChange.setMinimumSize(new java.awt.Dimension(24, 24));
        logSizeChange.setPreferredSize(new java.awt.Dimension(24, 24));
        logSizeChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logSizeChangeActionPerformed(evt);
            }
        });
        logSizeChange.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logSizeChangeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logSizeChangeMouseExited(evt);
            }
        });

        clearLog.setBorderPainted(false);
        clearLog.setContentAreaFilled(false);
        clearLog.setMargin(new java.awt.Insets(4, 4, 4, 4));
        clearLog.setMaximumSize(new java.awt.Dimension(24, 24));
        clearLog.setMinimumSize(new java.awt.Dimension(24, 24));
        clearLog.setPreferredSize(new java.awt.Dimension(24, 24));
        clearLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearLogActionPerformed(evt);
            }
        });
        clearLog.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                clearLogMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                clearLogMouseExited(evt);
            }
        });

        pauseResume.setBorderPainted(false);
        pauseResume.setContentAreaFilled(false);
        pauseResume.setMargin(new java.awt.Insets(4, 4, 4, 4));
        pauseResume.setMaximumSize(new java.awt.Dimension(24, 24));
        pauseResume.setMinimumSize(new java.awt.Dimension(24, 24));
        pauseResume.setPreferredSize(new java.awt.Dimension(24, 24));
        pauseResume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseResumeActionPerformed(evt);
            }
        });
        pauseResume.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pauseResumeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pauseResumeMouseExited(evt);
            }
        });

        logSizeTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        logSizeTextField.setMaximumSize(new java.awt.Dimension(45, 19));
        logSizeTextField.setMinimumSize(new java.awt.Dimension(45, 19));
        logSizeTextField.setPreferredSize(new java.awt.Dimension(45, 19));

        logSizeText.setText("Log Size:");

        jScrollPane1.setViewportView(logTable);

        invisibleLabel.setMaximumSize(new java.awt.Dimension(28, 28));
        invisibleLabel.setMinimumSize(new java.awt.Dimension(28, 28));
        invisibleLabel.setPreferredSize(new java.awt.Dimension(28, 28));
        invisibleLabel.setRequestFocusEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(2, 2, 2)
                .add(pauseResume, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(2, 2, 2)
                .add(clearLog, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(invisibleLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 304, Short.MAX_VALUE)
                .add(logSizeText)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(logSizeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(2, 2, 2)
                .add(logSizeChange, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(2, 2, 2))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                .add(0, 0, 0)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pauseResume, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(clearLog, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(logSizeChange, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(logSizeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(logSizeText)
                    .add(invisibleLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void pauseResumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseResumeActionPerformed
        if (!isPaused(selectedChannel)) {
            channelStates.put(selectedChannel, PAUSED);
        } else {
            channelStates.put(selectedChannel, RESUMED);
        }
        adjustPauseResumeButton(selectedChannel);
    }//GEN-LAST:event_pauseResumeActionPerformed

    private void clearLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearLogActionPerformed
        // "clear log" only affects on the client side.
        // because clearing log on one client should NOT affect other clients' logs.
        // clear logs on client side only.
        dcsc.clearLog(selectedChannel);
    }//GEN-LAST:event_clearLogActionPerformed

    private void logSizeChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logSizeChangeActionPerformed
        // NOTE: the log size on the server is always 1000, which is max. because if there are multiple clients connected to the same server,
        //  it has to be able to support the maximum allowed in case some client has it set at 999.
        // i.e. this log size change only affects on the client side.
        if (logSizeTextField.getText().length() == 0)
        {
            parent.alertWarning("Please enter a valid number.");
            return;
        }
        int newDashboardLogSize = Integer.parseInt(logSizeTextField.getText());
        if (newDashboardLogSize != currentDashboardLogSize) {
            if (newDashboardLogSize <= 0) {
                parent.alertWarning("Please enter a log size that is larger than 0.");
            } else {
                userPreferences.putInt("dashboardLogSize", newDashboardLogSize);
                currentDashboardLogSize = newDashboardLogSize;
                dcsc.resetLogSize(newDashboardLogSize, selectedChannel);
            }
        }
    }//GEN-LAST:event_logSizeChangeActionPerformed

    private void logSizeChangeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logSizeChangeMouseExited
        logSizeChange.setBorderPainted(false);
        logSizeChange.setContentAreaFilled(false);
    }//GEN-LAST:event_logSizeChangeMouseExited

    private void logSizeChangeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logSizeChangeMouseEntered
        logSizeChange.setBorderPainted(true);
        logSizeChange.setContentAreaFilled(true);
    }//GEN-LAST:event_logSizeChangeMouseEntered

    private void clearLogMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clearLogMouseExited
        clearLog.setBorderPainted(false);
        clearLog.setContentAreaFilled(false);
    }//GEN-LAST:event_clearLogMouseExited

    private void clearLogMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clearLogMouseEntered
        clearLog.setBorderPainted(true);
        clearLog.setContentAreaFilled(true);
    }//GEN-LAST:event_clearLogMouseEntered

    private void pauseResumeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pauseResumeMouseExited
        pauseResume.setBorderPainted(false);
        pauseResume.setContentAreaFilled(false);
    }//GEN-LAST:event_pauseResumeMouseExited

    private void pauseResumeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pauseResumeMouseEntered
        pauseResume.setBorderPainted(true);
        pauseResume.setContentAreaFilled(true);
    }//GEN-LAST:event_pauseResumeMouseEntered


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearLog;
    private javax.swing.JLabel invisibleLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton logSizeChange;
    private javax.swing.JLabel logSizeText;
    private javax.swing.JTextField logSizeTextField;
    private com.webreach.mirth.client.ui.components.MirthTable logTable;
    private javax.swing.JButton pauseResume;
    // End of variables declaration//GEN-END:variables

}
