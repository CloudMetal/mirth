package com.webreach.mirth.client.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.ExtensionPoint;
import com.webreach.mirth.model.ExtensionPointDefinition;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.plugins.DashboardColumnPlugin;
import com.webreach.mirth.plugins.DashboardPanelPlugin;

public class DashboardPanel extends javax.swing.JPanel {

    private final String STATUS_COLUMN_NAME = "Status";
    private final String NAME_COLUMN_NAME = "Name";
    private final String RECEIVED_COLUMN_NAME = "Received";
    private final String QUEUED_COLUMN_NAME = "Queued";
    private final String SENT_COLUMN_NAME = "Sent";
    private final String ERROR_COLUMN_NAME = "Errored";
    private final String FILTERED_COLUMN_NAME = "Filtered";
    private final String ALERTED_COLUMN_NAME = "Alerted";
    private Frame parent;
    private Map<String, DashboardColumnPlugin> loadedColumnPluginsBeforeStatus = new HashMap<String, DashboardColumnPlugin>();
    private Map<String, DashboardColumnPlugin> loadedColumnPluginsAfterStatus = new HashMap<String, DashboardColumnPlugin>();
    private Map<String, DashboardPanelPlugin> loadedPanelPlugins = new HashMap<String, DashboardPanelPlugin>();

    /** Creates new form DashboardPanel */
    public DashboardPanel() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        statusPane.setDoubleBuffered(true);
        loadColumnPlugins();
        split.setBottomComponent(null);
        split.setDividerSize(0);
        split.setOneTouchExpandable(true);
        loadPanelPlugins();
        ChangeListener changeListener = new ChangeListener() {

            public void stateChanged(ChangeEvent changeEvent) {
                JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                loadPanelPlugin(sourceTabbedPane.getTitleAt(index));
            }
        };
        tabs.addChangeListener(changeListener);

        makeStatusTable();

        this.setDoubleBuffered(true);
    }

    // Extension point for ExtensionPoint.Type.CLIENT_DASHBOARD_COLUMN
    @ExtensionPointDefinition(mode = ExtensionPoint.Mode.CLIENT, type = ExtensionPoint.Type.CLIENT_DASHBOARD_COLUMN)
    public void loadColumnPlugins() {
        try {
            Map<String, PluginMetaData> plugins = parent.getPluginMetaData();
            for (PluginMetaData metaData : plugins.values()) {
                if (metaData.isEnabled()) {
                    for (ExtensionPoint extensionPoint : metaData.getExtensionPoints()) {
                        try {
                            if (extensionPoint.getMode().equals(ExtensionPoint.Mode.CLIENT) && extensionPoint.getType().equals(ExtensionPoint.Type.CLIENT_DASHBOARD_COLUMN) && extensionPoint.getClassName() != null && extensionPoint.getClassName().length() > 0) {
                                String pluginName = extensionPoint.getName();
                                Class clazz = Class.forName(extensionPoint.getClassName());
                                Constructor[] constructors = clazz.getDeclaredConstructors();
                                for (int i = 0; i < constructors.length; i++) {
                                    Class parameters[];
                                    parameters = constructors[i].getParameterTypes();
                                    // load plugin if the number of parameters is 2.
                                    if (parameters.length == 2) {

                                        DashboardColumnPlugin columnPlugin = (DashboardColumnPlugin) constructors[i].newInstance(new Object[]{pluginName, this});
                                        if (columnPlugin.showBeforeStatusColumn()) {
                                            loadedColumnPluginsBeforeStatus.put(columnPlugin.getColumnHeader(), columnPlugin);
                                        } else {
                                            loadedColumnPluginsAfterStatus.put(columnPlugin.getColumnHeader(), columnPlugin);
                                        }
                                        i = constructors.length;

                                    }
                                }
                            }
                        } catch (Exception e) {
                            parent.alertException(this, e.getStackTrace(), e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
        }
    }

    // Extension point for ExtensionPoint.Type.CLIENT_DASHBOARD_PANE
    @ExtensionPointDefinition(mode = ExtensionPoint.Mode.CLIENT, type = ExtensionPoint.Type.CLIENT_DASHBOARD_PANE)
    public void loadPanelPlugins() {
        try {
            Map<String, PluginMetaData> plugins = parent.getPluginMetaData();
            for (PluginMetaData metaData : plugins.values()) {
                if (metaData.isEnabled()) {
                    for (ExtensionPoint extensionPoint : metaData.getExtensionPoints()) {
                        try {
                            if (extensionPoint.getMode().equals(ExtensionPoint.Mode.CLIENT) && extensionPoint.getType().equals(ExtensionPoint.Type.CLIENT_DASHBOARD_PANE) && extensionPoint.getClassName() != null && extensionPoint.getClassName().length() > 0) {
                                String pluginName = extensionPoint.getName();
                                Class clazz = Class.forName(extensionPoint.getClassName());
                                Constructor[] constructors = clazz.getDeclaredConstructors();
                                for (int i = 0; i < constructors.length; i++) {
                                    Class parameters[];
                                    parameters = constructors[i].getParameterTypes();
                                    // load plugin if the number of parameters is 1.
                                    if (parameters.length == 1) {

                                        DashboardPanelPlugin panelPlugin = (DashboardPanelPlugin) constructors[i].newInstance(new Object[]{pluginName});
                                        loadedPanelPlugins.put(pluginName, panelPlugin);
                                        i = constructors.length;

                                    }
                                }
                            }
                        } catch (Exception e) {
                            parent.alertException(this, e.getStackTrace(), e.getMessage());
                        }
                    }
                }
            }

            if (loadedPanelPlugins.size() > 0) {
                for (DashboardPanelPlugin plugin : loadedPanelPlugins.values()) {
                    if (plugin.getComponent() != null) {
                        tabs.addTab(plugin.getName(), plugin.getComponent());
                    }
                }

                split.setBottomComponent(tabs);
                split.setDividerSize(6);
                split.setDividerLocation((int) (3 * Preferences.userNodeForPackage(Mirth.class).getInt("height", UIConstants.MIRTH_HEIGHT) / 5));
                split.setResizeWeight(0.5);
            }
        } catch (Exception e) {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
        }
    }

    public void loadDefaultPanel() {
        if (loadedPanelPlugins.keySet().iterator().hasNext()) {
            loadPanelPlugin(loadedPanelPlugins.keySet().iterator().next());
        }
    }

    public void loadPanelPlugin(String pluginName) {
        DashboardPanelPlugin plugin = loadedPanelPlugins.get(pluginName);
        if (plugin != null && getSelectedStatuses().size() != 0) {
            plugin.update(getSelectedStatuses());
        } else {
            plugin.update();
        }
    }

    public Map<String, DashboardPanelPlugin> getLoadedPanelPlugins() {
        return loadedPanelPlugins;
    }

    public Map<String, DashboardColumnPlugin> getLoadedColumnPluginsBeforeStatus() {
        return loadedColumnPluginsBeforeStatus;
    }

    public Map<String, DashboardColumnPlugin> getLoadedColumnPluginsAfterStats() {
        return loadedColumnPluginsAfterStatus;
    }

    public synchronized void updateCurrentPluginPanel() {
        if (loadedPanelPlugins.size() > 0) {
            loadPanelPlugin(tabs.getTitleAt(tabs.getSelectedIndex()));
        }
    }

    /**
     * Makes the status table with all current server information.
     */
    public void makeStatusTable() {
        updateTable(null);

        statusTable.setDoubleBuffered(true);

        statusTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        for (DashboardColumnPlugin plugin : loadedColumnPluginsBeforeStatus.values()) {
            String columnName = plugin.getColumnHeader();
            statusTable.getColumnExt(columnName).setMaxWidth(plugin.getMaxWidth());
            statusTable.getColumnExt(columnName).setMinWidth(plugin.getMinWidth());
            statusTable.getColumnExt(columnName).setCellRenderer(plugin.getCellRenderer());
        }

        statusTable.getColumnExt(STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        statusTable.getColumnExt(RECEIVED_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        statusTable.getColumnExt(SENT_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        statusTable.getColumnExt(ERROR_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        statusTable.getColumnExt(FILTERED_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        statusTable.getColumnExt(QUEUED_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        statusTable.getColumnExt(ALERTED_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);

        statusTable.getColumnExt(STATUS_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(RECEIVED_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(SENT_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(ERROR_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(FILTERED_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(QUEUED_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(ALERTED_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(NAME_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);

        statusTable.getColumnExt(STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());
        statusTable.getColumnExt(RECEIVED_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        statusTable.getColumnExt(SENT_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        statusTable.getColumnExt(ERROR_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        statusTable.getColumnExt(FILTERED_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        statusTable.getColumnExt(QUEUED_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        statusTable.getColumnExt(ALERTED_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());

        for (DashboardColumnPlugin plugin : loadedColumnPluginsAfterStatus.values()) {
            String columnName = plugin.getColumnHeader();
            statusTable.getColumnExt(columnName).setMaxWidth(plugin.getMaxWidth());
            ;
            statusTable.getColumnExt(columnName).setMinWidth(plugin.getMinWidth());
            statusTable.getColumnExt(columnName).setCellRenderer(plugin.getCellRenderer());
        }

        statusTable.packTable(UIConstants.COL_MARGIN);

        statusTable.setRowHeight(UIConstants.ROW_HEIGHT);
        statusTable.setOpaque(true);
        statusTable.setRowSelectionAllowed(true);

        statusTable.setSortable(true);

        statusPane.setViewportView(statusTable);

        statusTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                StatusListSelected(evt);
            }
        });
        statusTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (statusTable.rowAtPoint(new Point(evt.getX(), evt.getY())) == -1) {
                    return;
                }

                if (evt.getClickCount() >= 2) {
                    parent.doShowMessages();
                }
            }
        });
    }

    public synchronized void updateTable(Object[][] tableData) {
        if (statusTable != null) {
            int[] selectedStatuses = statusTable.getSelectedRows();

            RefreshTableModel model = (RefreshTableModel) statusTable.getModel();
            model.refreshDataVector(tableData);

            for (int i = 0; i < selectedStatuses.length; i++) {
                if (selectedStatuses[i] != -1 && selectedStatuses[i] < statusTable.getModel().getRowCount()) {
                    statusTable.addRowSelectionInterval(selectedStatuses[i], selectedStatuses[i]);
                }
            }
        } else {
            statusTable = new MirthTable();
            String[] defaultColumns = new String[]{STATUS_COLUMN_NAME,
                NAME_COLUMN_NAME, RECEIVED_COLUMN_NAME,
                FILTERED_COLUMN_NAME, QUEUED_COLUMN_NAME,
                SENT_COLUMN_NAME, ERROR_COLUMN_NAME, ALERTED_COLUMN_NAME};
            ArrayList<String> columns = new ArrayList<String>();
            for (DashboardColumnPlugin plugin : loadedColumnPluginsBeforeStatus.values()) {
                columns.add(plugin.getColumnHeader());
            }
            for (int i = 0; i < defaultColumns.length; i++) {
                columns.add(defaultColumns[i]);
            }
            for (DashboardColumnPlugin plugin : loadedColumnPluginsAfterStatus.values()) {
                columns.add(plugin.getColumnHeader());
            }

            statusTable.setModel(new RefreshTableModel(tableData, columns.toArray(new String[0])) {

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return false;
                }
            });
        }

        // Add the highlighters.  Always add the error highlighter.
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            statusTable.addHighlighter(highlighter);
        }

        HighlightPredicate errorHighlighterPredicate = new HighlightPredicate() {

            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                if (adapter.column == statusTable.getColumnViewIndex(ERROR_COLUMN_NAME)) {
                    if (((Integer) statusTable.getValueAt(adapter.row, adapter.column)).intValue() > 0) {
                        return true;
                    }
                }
                return false;
            }
        };

        Highlighter errorHighlighter = new ColorHighlighter(errorHighlighterPredicate, Color.PINK, Color.BLACK, Color.PINK, Color.BLACK);
        statusTable.addHighlighter(errorHighlighter);
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed.  Deselects the rows if no row was selected.
     */
    private void checkSelectionAndPopupMenu(java.awt.event.MouseEvent evt) {
        int row = statusTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
        if (row == -1) {
            deselectRows();
        }

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                if (!statusTable.isRowSelected(row)) {
                    statusTable.setRowSelectionInterval(row, row);
                }
            }
            parent.statusPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    /*
     * Action when something on the status list has been selected. Sets all
     * appropriate tasks visible.
     */
    private void StatusListSelected(ListSelectionEvent evt) {
        int[] rows = statusTable.getSelectedModelRows();
        int column = statusTable.getColumnModelIndex(STATUS_COLUMN_NAME);

        if (rows.length > 0) {
            parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 1, -1, true); // show all
            parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 5, 7, false); // hide start, pause, and stop

            for (int i = 0; i < rows.length; i++) {
                if (((CellData) statusTable.getModel().getValueAt(rows[i], column)).getText().equals("Started")) {
                    parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 6, 6, true);
                    parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 7, 7, true);
                } else if (((CellData) statusTable.getModel().getValueAt(rows[i], column)).getText().equals("Paused")) {
                    parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 5, 5, true);
                    parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 7, 7, true);
                } else {
                    parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 5, 5, true);
                    parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 7, 7, false);
                }
            }

            updateCurrentPluginPanel();
        }
    }

    /**
     * Gets the index of the selected status row.
     */
    public synchronized List<ChannelStatus> getSelectedStatuses() {
        int[] statuses = statusTable.getSelectedModelRows();
        List<ChannelStatus> selectedStatuses = new ArrayList<ChannelStatus>();

        for (int i = 0; i < statuses.length; i++) {
            for (ChannelStatus status : parent.status) {
                if (((String) statusTable.getModel().getValueAt(statuses[i], statusTable.getColumnModelIndex(NAME_COLUMN_NAME))).equalsIgnoreCase(status.getName())) {
                    selectedStatuses.add(status);
                }
            }
        }

        return selectedStatuses;
    }

    public void deselectRows() {
        statusTable.clearSelection();
        parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 1, -1, false);
        updateCurrentPluginPanel();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        split = new javax.swing.JSplitPane();
        statusPane = new javax.swing.JScrollPane();
        statusTable = null;
        tabs = new javax.swing.JTabbedPane();

        split.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        split.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        statusPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        statusPane.setViewportView(statusTable);

        split.setTopComponent(statusPane);
        split.setRightComponent(tabs);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(split, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(split, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane split;
    private javax.swing.JScrollPane statusPane;
    private com.webreach.mirth.client.ui.components.MirthTable statusTable;
    private javax.swing.JTabbedPane tabs;
    // End of variables declaration//GEN-END:variables
}
