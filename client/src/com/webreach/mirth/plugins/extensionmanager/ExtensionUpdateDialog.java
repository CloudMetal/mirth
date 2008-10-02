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

package com.webreach.mirth.plugins.extensionmanager;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.Filter;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.jdesktop.swingx.decorator.PatternFilter;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.core.IgnoredComponent;
import com.webreach.mirth.client.ui.BareBonesBrowserLaunch;
import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.RefreshTableModel;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.MetaData;
import com.webreach.mirth.model.UpdateInfo;

/** Creates the About Mirth dialog. The content is loaded from about.txt. */
public class ExtensionUpdateDialog extends javax.swing.JDialog
{
    private ExtensionManagerClient parent;
    
    private final String EXTENSION_NEW_COLUMN_NAME = "New Extension";
    private final String EXTENSION_INSTALL_COLUMN_NAME = "Install";
    private final String EXTENSION_TYPE_COLUMN_NAME = "Type";
    private final String EXTENSION_NAME_COLUMN_NAME = "Name";
    private final String EXTENSION_INSTALLED_VERSION_COLUMN_NAME = "Installed Version";
    private final String EXTENSION_UPDATE_VERSION_COLUMN_NAME = "Update Version";
    private final String EXTENSION_IGNORE_COLUMN_NAME = "Ignore";
    private final int EXTENSION_TABLE_NUMBER_OF_COLUMNS = 7;
    private final int EXTENSION_NEW_COLUMN_NUMBER = 0;
    private final int EXTENSION_INSTALL_COLUMN_NUMBER = 1;
    private final int EXTENSION_TYPE_COLUMN_NUMBER = 2;
    private final int EXTENSION_NAME_COLUMN_NUMBER = 3;
    private final int EXTENSION_UPDATE_VERSION_COLUMN_NUMBER = 5;
    private final int EXTENSION_IGNORE_COLUMN_NUMBER = 6;
    private Map<String, MetaData> extensions = new HashMap<String, MetaData>();
    private Map<String, UpdateInfo> extensionUpdates = new HashMap<String, UpdateInfo>();
    private ExtensionUtil pluginUtil = new ExtensionUtil();
    private boolean cancel = false;
    
    public ExtensionUpdateDialog(ExtensionManagerClient parent) throws ClientException
    {
    	this(parent, null);
    }
    
    public ExtensionUpdateDialog(ExtensionManagerClient parent, List<UpdateInfo> updateInfoList) throws ClientException
    {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = parent;
        
        initComponents();
        extensions.putAll(PlatformUI.MIRTH_FRAME.getPluginMetaData());
        extensions.putAll(PlatformUI.MIRTH_FRAME.getConnectorMetaData());
        
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = PlatformUI.MIRTH_FRAME.getSize();
        Point loc = PlatformUI.MIRTH_FRAME.getLocation();
        
        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
        	setLocationRelativeTo(null);
        } else {
	        setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }
        
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
            	closeButtonActionPerformed(null);
            }
        });
        
        progressBar.setVisible(false);
        
        makeLoadedExtensionsTable();
        
        if (updateInfoList == null) {
        	checkForUpdatesButtonActionPerformed(null);
        } else {
        	extensionUpdates = new HashMap<String, UpdateInfo>();
        	for (UpdateInfo updateInfo : updateInfoList) {
            	extensionUpdates.put(updateInfo.getName(), updateInfo);
            }
        	
        	updateExtensionsTable();
        }
        
        setVisible(true);
    }

    /**
     * Makes the loaded connectors table
     */
    public void makeLoadedExtensionsTable()
    {
        
        loadedExtensionTable = new MirthTable();
        loadedExtensionTable.setModel(new RefreshTableModel(new Object[][]{}, new String[] { EXTENSION_NEW_COLUMN_NAME, EXTENSION_INSTALL_COLUMN_NAME, EXTENSION_TYPE_COLUMN_NAME, EXTENSION_NAME_COLUMN_NAME, EXTENSION_INSTALLED_VERSION_COLUMN_NAME,  EXTENSION_UPDATE_VERSION_COLUMN_NAME, EXTENSION_IGNORE_COLUMN_NAME })
        {
            boolean[] canEdit = new boolean[] { false, true, false, false, false, false, true };
            
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit[columnIndex];
            }
        });
        loadedExtensionTable.setDragEnabled(false);
        loadedExtensionTable.setRowSelectionAllowed(true);
        loadedExtensionTable.setRowHeight(UIConstants.ROW_HEIGHT);
        loadedExtensionTable.setFocusable(false);
        loadedExtensionTable.setOpaque(true);
        loadedExtensionTable.getTableHeader().setReorderingAllowed(true);
        loadedExtensionTable.setSortable(true);
        loadedExtensionTable.setSelectionMode(0);
        
        loadedExtensionTable.getColumnExt(EXTENSION_NEW_COLUMN_NAME).setVisible(false);
        
        loadedExtensionTable.getColumnExt(EXTENSION_INSTALL_COLUMN_NAME).setMaxWidth(50);
        loadedExtensionTable.getColumnExt(EXTENSION_INSTALL_COLUMN_NAME).setMinWidth(50);
        
        loadedExtensionTable.getColumnExt(EXTENSION_TYPE_COLUMN_NAME).setMinWidth(75);
        
        loadedExtensionTable.getColumnExt(EXTENSION_NAME_COLUMN_NAME).setMinWidth(75);
        
        loadedExtensionTable.getColumnExt(EXTENSION_INSTALLED_VERSION_COLUMN_NAME).setMaxWidth(120);
        loadedExtensionTable.getColumnExt(EXTENSION_INSTALLED_VERSION_COLUMN_NAME).setMinWidth(90);
        
        loadedExtensionTable.getColumnExt(EXTENSION_UPDATE_VERSION_COLUMN_NAME).setMaxWidth(120);
        loadedExtensionTable.getColumnExt(EXTENSION_UPDATE_VERSION_COLUMN_NAME).setMinWidth(90);
        
        loadedExtensionTable.getColumnExt(EXTENSION_IGNORE_COLUMN_NAME).setMaxWidth(50);
        loadedExtensionTable.getColumnExt(EXTENSION_IGNORE_COLUMN_NAME).setMinWidth(50);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            loadedExtensionTable.setHighlighters(highlighter);
        }
        loadedExtensionTable.addMouseListener(new MouseListener()
        {
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    int row = loadedExtensionTable.convertRowIndexToModel(loadedExtensionTable.getSelectedRow());
                    if (row > -1 && extensionUpdates != null)
                    {
                        UpdateInfo updateInfo = extensionUpdates.get(loadedExtensionTable.getModel().getValueAt(row, EXTENSION_NAME_COLUMN_NUMBER));
                        String type = updateInfo.getType().toString();
                        String name =  updateInfo.getName();
                        String version =  updateInfo.getVersion();
                        String author =  updateInfo.getAuthor();
                        String url =  updateInfo.getUri();
                        String description  = updateInfo.getDescription();
                                            
                        new ExtensionInfoDialog(name, type, author, version, url, description);
                    }
                }
            }
            public void mouseEntered(MouseEvent e)
            {
            }
            public void mouseExited(MouseEvent e)
            {
            }
            public void mousePressed(MouseEvent e)
            {
            }
            public void mouseReleased(MouseEvent e)
            {
            }
        });
        loadedExtensionTable.addMouseWheelListener(new MouseWheelListener()
        {
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                loadedExtensionScrollPane.getMouseWheelListeners()[0].mouseWheelMoved(e);
            }
            
        });
        loadedExtensionScrollPane.setViewportView(loadedExtensionTable);
        
        updateTableFilters();
    }
    
    public void installUpdates()
    {
        installSelectedButton.setEnabled(false);
        checkForUpdatesButton.setEnabled(false);
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
        	private boolean installedUpdates = false;
            public Void doInBackground()
            {
                for (int i = 0; i < loadedExtensionTable.getModel().getRowCount(); i++)
                {
                	if (loadedExtensionTable.getModel().getValueAt(i,EXTENSION_TYPE_COLUMN_NUMBER).equals(UpdateInfo.Type.SERVER)) {
                		if (((Boolean)loadedExtensionTable.getModel().getValueAt(i,EXTENSION_INSTALL_COLUMN_NUMBER)).booleanValue()) {
                			String serverName = (String) loadedExtensionTable.getModel().getValueAt(i, EXTENSION_NAME_COLUMN_NUMBER);
                			String serverUrl = extensionUpdates.get(serverName).getUri();
                			boolean downloadServer = parent.alertOkCancel(progressBar, "The server cannot be automatically upgraded. Press OK to download it now from:\n" + serverUrl);
                			
                			if (downloadServer) {
                				BareBonesBrowserLaunch.openURL(serverUrl);
                			}
                		}
                	}
                    
                }
                
                for (int i = 0; i < loadedExtensionTable.getModel().getRowCount(); i++)
                {
                    boolean install = ((Boolean)loadedExtensionTable.getModel().getValueAt(i,EXTENSION_INSTALL_COLUMN_NUMBER)).booleanValue();
                    if (install)
                    {
                        String name = (String)loadedExtensionTable.getModel().getValueAt(i, EXTENSION_NAME_COLUMN_NUMBER);
                        UpdateInfo plugin = extensionUpdates.get(name);
                        statusLabel.setText("Downloading extension: " + plugin.getName());
                        if (cancel)
                        {
                            break;
                        }
                        progressBar.setVisible(true);
                        File file = pluginUtil.downloadFileToDisk(plugin.getUri(), statusLabel, progressBar);
                        progressBar.setVisible(false);
                        if (cancel)
                        {
                            break;
                        }
                        statusLabel.setText("Updating extension: " + plugin.getName());
                        parent.install(file);
                        installedUpdates = true;
                    }
                }
                
                return null;
            }
            
            public void done()
            {
            	checkForUpdatesButton.setEnabled(true);
            	if (installedUpdates){
	                statusLabel.setText("Updates Installed!");
	                parent.finishInstall();
	                dispose();
            	}
            }
        };
        
        worker.execute();
    }
    
    public void updateExtensionsListAndTable() {
    	
    	progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        statusLabel.setText("Checking for updates...");        
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
            	extensionUpdates = new HashMap<String, UpdateInfo>();
            	
                List<UpdateInfo> updateInfoList = new ArrayList<UpdateInfo>();
                
        		try {
        			updateInfoList = PlatformUI.MIRTH_FRAME.getUpdateClient(progressBar).getUpdates();
        		} catch (ClientException e) {
        			parent.alertException(progressBar, e.getStackTrace(), e.getMessage());
        		}
                
                for (UpdateInfo updateInfo : updateInfoList) {
                	extensionUpdates.put(updateInfo.getName(), updateInfo);
                }
                
                return null;
            }
            
            public void done()
            {
            	updateExtensionsTable();
            	progressBar.setIndeterminate(false);
            }
        };
        
        worker.execute();        
    }
    
    private void updateExtensionsTable()
    {
        Object[][] tableData = null;
        int tableSize = 0;
        
        if (extensionUpdates.size() > 0){
        	statusLabel.setText("Ready to Install Updates!");
        	installSelectedButton.setEnabled(true);
        } else {
        	statusLabel.setText("No Updates Found.");
        }
        
        tableSize = extensionUpdates.size();
        progressBar.setVisible(false);
        tableData = new Object[tableSize][EXTENSION_TABLE_NUMBER_OF_COLUMNS];
        
        int i = 0;
        for (UpdateInfo updateInfo: extensionUpdates.values())
        {
        	tableData[i][0] = updateInfo.isNew();
       		tableData[i][1] = !updateInfo.isIgnored();
            tableData[i][2] = updateInfo.getType();
            tableData[i][3] = updateInfo.getName();
            
            String installedVersion = "";
            if (updateInfo.getType().equals(UpdateInfo.Type.SERVER)) {
            	installedVersion = PlatformUI.SERVER_VERSION;
            } else if (!updateInfo.isNew()){
            	installedVersion = extensions.get(updateInfo.getName()).getPluginVersion();
            }
            
            tableData[i][4] = installedVersion;
            tableData[i][5] = updateInfo.getVersion();
            tableData[i][6] = updateInfo.isIgnored();
            i++;
        }
        
        if (loadedExtensionTable != null)
        {
            RefreshTableModel model = (RefreshTableModel) loadedExtensionTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
        }
        progressBar.setValue(0);
    }
	
    private void updateTableFilters() {
        List<Filter> filterList = new ArrayList<Filter>();

        if (updatesCheckBox.isSelected() && newCheckBox.isSelected()) {
        	filterList.add(new PatternFilter("true|false", 0, EXTENSION_NEW_COLUMN_NUMBER));
        } else if (updatesCheckBox.isSelected()) {
        	filterList.add(new PatternFilter("false", 0, EXTENSION_NEW_COLUMN_NUMBER));
        } else if (newCheckBox.isSelected()) {
        	filterList.add(new PatternFilter("true", 0, EXTENSION_NEW_COLUMN_NUMBER));
        } else {
        	filterList.add(new PatternFilter("displayNone", 1, EXTENSION_NEW_COLUMN_NUMBER));
        }
        
        if (!ignoredCheckBox.isSelected()) {
        	filterList.add(new PatternFilter("false", 0, EXTENSION_IGNORE_COLUMN_NUMBER));
        }
        
        FilterPipeline filterPipeline = null;
        if (filterList.size() > 0) {
        	Filter[] filterArray = filterList.toArray(new Filter[filterList.size()]);
        	filterPipeline = new FilterPipeline(filterArray);
        }
        
        loadedExtensionTable.setFilters(filterPipeline);
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        checkForUpdatesButton = new javax.swing.JButton();
        installSelectedButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        loadedExtensionScrollPane = new javax.swing.JScrollPane();
        loadedExtensionTable = new com.webreach.mirth.client.ui.components.MirthTable();
        progressBar = new javax.swing.JProgressBar();
        ignoredCheckBox = new javax.swing.JCheckBox();
        newCheckBox = new javax.swing.JCheckBox();
        updatesCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Mirth Updater");
        setModal(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMaximumSize(null);
        jPanel1.setMinimumSize(new java.awt.Dimension(400, 400));

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        statusLabel.setText("Idle");

        checkForUpdatesButton.setText("Check for Updates");
        checkForUpdatesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkForUpdatesButtonActionPerformed(evt);
            }
        });

        installSelectedButton.setText("Install Selected");
        installSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installSelectedButtonActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel2.setText("Mirth Updates");

        loadedExtensionScrollPane.setMaximumSize(null);
        loadedExtensionScrollPane.setMinimumSize(null);
        loadedExtensionScrollPane.setPreferredSize(new java.awt.Dimension(350, 200));

        loadedExtensionTable.setModel(new RefreshTableModel(new Object[][]{}, new String[] { EXTENSION_INSTALL_COLUMN_NAME, EXTENSION_TYPE_COLUMN_NAME, EXTENSION_NAME_COLUMN_NAME, EXTENSION_INSTALLED_VERSION_COLUMN_NAME,  EXTENSION_UPDATE_VERSION_COLUMN_NAME, EXTENSION_IGNORE_COLUMN_NAME }));
        loadedExtensionScrollPane.setViewportView(loadedExtensionTable);

        ignoredCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        ignoredCheckBox.setSelected(true);
        ignoredCheckBox.setText("Ignored");
        ignoredCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoredCheckBoxActionPerformed(evt);
            }
        });

        newCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        newCheckBox.setSelected(true);
        newCheckBox.setText("New");
        newCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newCheckBoxActionPerformed(evt);
            }
        });

        updatesCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        updatesCheckBox.setSelected(true);
        updatesCheckBox.setText("Updates");
        updatesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updatesCheckBoxActionPerformed(evt);
            }
        });

        jLabel1.setText("Show:");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 222, Short.MAX_VALUE)
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(updatesCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(newCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ignoredCheckBox))
                    .add(loadedExtensionScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(progressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(installSelectedButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(checkForUpdatesButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(closeButton))
                    .add(statusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 437, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(ignoredCheckBox)
                    .add(newCheckBox)
                    .add(updatesCheckBox)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(loadedExtensionScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusLabel)
                .add(10, 10, 10)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(closeButton)
                        .add(checkForUpdatesButton)
                        .add(installSelectedButton))
                    .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void checkForUpdatesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkForUpdatesButtonActionPerformed
        checkForUpdatesButton.setEnabled(false);
        installSelectedButton.setEnabled(false);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                updateExtensionsListAndTable();
                return null;
            }
            
            public void done()
            {
            	checkForUpdatesButton.setEnabled(true);
            }
        };
        
        worker.execute();
        
}//GEN-LAST:event_checkForUpdatesButtonActionPerformed
    
    private void installSelectedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installSelectedButtonActionPerformed
        installUpdates();
}//GEN-LAST:event_installSelectedButtonActionPerformed
    
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeButtonActionPerformed
    {//GEN-HEADEREND:event_closeButtonActionPerformed
        cancel = true;
        
        try {
	        List<IgnoredComponent> ignoredComponents = PlatformUI.MIRTH_FRAME.getUpdateClient(this).getIgnoredComponents();
	        
	        for (int i = 0; i < loadedExtensionTable.getModel().getRowCount(); i++) {
	        	String componentName = (String)loadedExtensionTable.getModel().getValueAt(i, EXTENSION_NAME_COLUMN_NUMBER);
	        	String componentVersion = (String)loadedExtensionTable.getModel().getValueAt(i, EXTENSION_UPDATE_VERSION_COLUMN_NUMBER);
	        	IgnoredComponent component = new IgnoredComponent(componentName, componentVersion);
	        	if ((Boolean)loadedExtensionTable.getModel().getValueAt(i, EXTENSION_IGNORE_COLUMN_NUMBER) && !ignoredComponents.contains(component)) {
	        		ignoredComponents.add(component);
	        	} else if (!(Boolean)loadedExtensionTable.getModel().getValueAt(i, EXTENSION_IGNORE_COLUMN_NUMBER) && ignoredComponents.contains(component)) {
	        		ignoredComponents.remove(component);
	        	}
	        }
	        
			PlatformUI.MIRTH_FRAME.getUpdateClient(this).setIgnoredComponents(ignoredComponents);
		} catch (ClientException e) {
			parent.alertException(this, e.getStackTrace(), e.getMessage());
		}
        
        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

private void updatesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updatesCheckBoxActionPerformed
	updateTableFilters();
}//GEN-LAST:event_updatesCheckBoxActionPerformed

private void newCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newCheckBoxActionPerformed
	updateTableFilters();
}//GEN-LAST:event_newCheckBoxActionPerformed

private void ignoredCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoredCheckBoxActionPerformed
	updateTableFilters();
}//GEN-LAST:event_ignoredCheckBoxActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton checkForUpdatesButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JCheckBox ignoredCheckBox;
    private javax.swing.JButton installSelectedButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane loadedExtensionScrollPane;
    private com.webreach.mirth.client.ui.components.MirthTable loadedExtensionTable;
    private javax.swing.JCheckBox newCheckBox;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JCheckBox updatesCheckBox;
    // End of variables declaration//GEN-END:variables
    
}
