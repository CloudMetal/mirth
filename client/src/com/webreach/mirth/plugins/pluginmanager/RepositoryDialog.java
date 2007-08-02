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

package com.webreach.mirth.plugins.pluginmanager;

import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.RefreshTableModel;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.MetaData;
import com.webreach.mirth.model.PluginMetaData;

/** Creates the About Mirth dialog. The content is loaded from about.txt. */
public class RepositoryDialog extends javax.swing.JDialog 
{
    private PluginManagerClient parent;
    private final String EXTENSION_TYPE_COLUMN_NAME = "Type";
    private final String EXTENSION_NAME_COLUMN_NAME = "Extension Name";
    private final String EXTENSION_VERSION_COLUMN_NAME = "Version";
    //private final String EXTENSION_MIRTHVERSION_COLUMN_NAME = "Mirth Version";
    private final String EXTENSION_AUTHOR_COLUMN_NAME = "Author";
    private final String EXTENSION_DESCRIPTION_COLUMN_NAME = "Description";
    //private final String EXTENSION_URL_COLUMN_NAME = "Url";
    private final String EXTENSION_INSTALL_COLUMN_NAME = "Install";
    private PluginUtil pluginUtil = new PluginUtil();
    private boolean cancel = false;
    private ExtensionInfo[] extensionInfo = null;
    /**
     * Creates new form ViewContentDialog
     */
    public RepositoryDialog(PluginManagerClient parent) throws ClientException
    {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = parent;
        
        initComponents();
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = PlatformUI.MIRTH_FRAME.getSize();
        Point loc = PlatformUI.MIRTH_FRAME.getLocation();
        setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        setVisible(true);
        makeLoadedExtensionsTable();
        
        
        checkForUpdatesButtonActionPerformed(null);         
    }
    /**
     * Makes the loaded connectors table
     */
    public void makeLoadedExtensionsTable()
    {
        //updateLoadedExtensionsTable();

        loadedExtensionTable = new MirthTable();
        loadedExtensionTable.setModel(new RefreshTableModel(new Object[][]{}, new String[] {EXTENSION_TYPE_COLUMN_NAME,
        EXTENSION_NAME_COLUMN_NAME, EXTENSION_VERSION_COLUMN_NAME,
        EXTENSION_AUTHOR_COLUMN_NAME, EXTENSION_DESCRIPTION_COLUMN_NAME,
        EXTENSION_INSTALL_COLUMN_NAME})
        {
            boolean[] canEdit = new boolean[] { false, false, false, false, false, false, true };
            
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit[columnIndex];
            }
        });
        loadedExtensionTable.setDragEnabled(false);
        loadedExtensionTable.setRowSelectionAllowed(false);
        loadedExtensionTable.setRowHeight(UIConstants.ROW_HEIGHT);
        loadedExtensionTable.setFocusable(false);
        loadedExtensionTable.setOpaque(true);
        loadedExtensionTable.getTableHeader().setReorderingAllowed(true);
        loadedExtensionTable.setSortable(true);
        loadedExtensionTable.setSelectionMode(0);
        loadedExtensionTable.getColumnExt(EXTENSION_TYPE_COLUMN_NAME).setMinWidth(75);
        loadedExtensionTable.getColumnExt(EXTENSION_TYPE_COLUMN_NAME).setMaxWidth(280);
       
        loadedExtensionTable.getColumnExt(EXTENSION_NAME_COLUMN_NAME).setMinWidth(75);
        
        loadedExtensionTable.getColumnExt(EXTENSION_VERSION_COLUMN_NAME).setMaxWidth(120);
        loadedExtensionTable.getColumnExt(EXTENSION_VERSION_COLUMN_NAME).setMinWidth(90);
        
        
        loadedExtensionTable.getColumnExt(EXTENSION_AUTHOR_COLUMN_NAME).setMaxWidth(120);
        loadedExtensionTable.getColumnExt(EXTENSION_AUTHOR_COLUMN_NAME).setMinWidth(90);
        
       // loadedExtensionTable.getColumnExt(EXTENSION_DESCRIPTION_COLUMN_NAME).setMaxWidth(50);
        loadedExtensionTable.getColumnExt(EXTENSION_DESCRIPTION_COLUMN_NAME).setMinWidth(150);
        
      //  loadedExtensionTable.getColumnExt(EXTENSION_URL_COLUMN_NAME).setMaxWidth(50);
      //  loadedExtensionTable.getColumnExt(EXTENSION_URL_COLUMN_NAME).setMinWidth(75);
        
        loadedExtensionTable.getColumnExt(EXTENSION_INSTALL_COLUMN_NAME).setMaxWidth(50);
        loadedExtensionTable.getColumnExt(EXTENSION_INSTALL_COLUMN_NAME).setMinWidth(50);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            loadedExtensionTable.setHighlighters(highlighter);
        }
      //  loadedExtensionTable.packTable(UIConstants.COL_MARGIN);
        loadedExtensionScrollPane.setViewportView(loadedExtensionTable);
        loadedExtensionTable.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2){
                    int row = loadedExtensionTable.getSelectedRow();
                    if (row > -1 && extensionInfo != null){
                        
                        String type = extensionInfo[row].getType();
                        String name =  extensionInfo[row].getName();
                        String version =  extensionInfo[row].getVersion();
                        String mirthVersion =  extensionInfo[row].getMirthVersion();
                        String author =  extensionInfo[row].getAuthor();
                        String url =  extensionInfo[row].getUrl();
                        String description  = extensionInfo[row].getDescription();
                        
                        new PluginInfoDialog(name, type, author,mirthVersion, version, url, description);
                    }
                }
            }
            public void mouseEntered(MouseEvent e) {
            }
            public void mouseExited(MouseEvent e) {
            }
            public void mousePressed(MouseEvent e) {
            }
            public void mouseReleased(MouseEvent e) {
            }
        });
        loadedExtensionTable.addMouseWheelListener(new MouseWheelListener()
        {
            public void mouseWheelMoved(MouseWheelEvent e)
            {
            	loadedExtensionScrollPane.getMouseWheelListeners()[0].mouseWheelMoved(e);
            }
            
        });
    }
    
   

  
    public void installUpdates(){
    	progressBar.setIndeterminate(true);
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
            	for (int i = 0; i < loadedExtensionTable.getModel().getRowCount(); i++){
            		boolean update = ((Boolean)loadedExtensionTable.getModel().getValueAt(i,6)).booleanValue();
            		if (update){
            			String name = (String)loadedExtensionTable.getModel().getValueAt(i, 1);
            			String type = (String)loadedExtensionTable.getModel().getValueAt(i, 0);
                                if (type.equals("Connector")){
                                    statusLabel.setText("Downloading connector: " + name);
                                }else if (type.equals("Plugin")){
                                    statusLabel.setText("Downloading plugin: " + name);
                                }
	            		
	            		try {
                                        Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                        e.printStackTrace();
                                }
                                pluginUtil.downloadFile("http://www.fotw.net/upload-download/firetest041206.jpg", statusLabel);
	            		statusLabel.setText("Installing extension: " + name);
	            		try {
                                        Thread.sleep(500);
                                } catch (InterruptedException e) {
                                        e.printStackTrace();
                                }
            		}
            	}
            	
                return null;
            }
            
            public void done()
            {
            	progressBar.setIndeterminate(false);
            	statusLabel.setText("Extensions Installed!");
            	PlatformUI.MIRTH_FRAME.alertInformation("Extensions successfully installed.\r\nMirth Server must be restarted in order to load the extension.");
            	dispose();
            }
        };
        
        worker.execute();
    }
  
    public void updateLoadedExtensionsTable()
    {
        Object[][] tableData = null;
        int tableSize = 0;
        ArrayList<String> updateVersion = new ArrayList<String>();
        statusLabel.setText("Retrieving extension list...");
        progressBar.setIndeterminate(true);
        String extensionInfoXML = pluginUtil.getStringFromURL("http://extensions.mirthproject.org/repository/");
        ObjectXMLSerializer serializer = new ObjectXMLSerializer(new Class[]{ExtensionInfo.class});
        try{
        	extensionInfo = (ExtensionInfo[]) serializer.fromXML(extensionInfoXML);
        }catch (Exception e){
        	e.printStackTrace();
        }
        if (extensionInfo == null){
        	return;
        }

        statusLabel.setText("Ready to Install Extensions!");
        progressBar.setIndeterminate(false);
        tableSize = extensionInfo.length;
       
        tableData = new Object[tableSize][7];

        
        for (int i = 0; i < extensionInfo.length; i++)
        {
            tableData[i][0] = extensionInfo[i].getType();
            tableData[i][1] = extensionInfo[i].getName();
            tableData[i][2] = extensionInfo[i].getVersion();
            tableData[i][3] = extensionInfo[i].getAuthor();
            tableData[i][4] = extensionInfo[i].getDescription();
           // tableData[i][5] = extensionInfo[i].getDescription();
           // tableData[i][5] = extensionInfo[i].getUrl();
            tableData[i][5] = Boolean.TRUE;
        }
        
        
        if (loadedExtensionTable != null)
        {
            RefreshTableModel model = (RefreshTableModel) loadedExtensionTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
        }
    }

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        checkForUpdatesButton = new javax.swing.JButton();
        installUpdatesButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        loadedExtensionScrollPane = new javax.swing.JScrollPane();
        loadedExtensionTable = new com.webreach.mirth.client.ui.components.MirthTable();
        progressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Mirth Extension Repository");
        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMaximumSize(null);
        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        statusLabel.setText("Idle");

        checkForUpdatesButton.setText("Refresh Extensions");
        checkForUpdatesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkForUpdatesButtonActionPerformed(evt);
            }
        });

        installUpdatesButton.setText("Install Selected");
        installUpdatesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installUpdatesButtonActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel2.setText("Mirth Extension Repository");

        loadedExtensionScrollPane.setMaximumSize(null);
        loadedExtensionScrollPane.setMinimumSize(null);
        loadedExtensionScrollPane.setPreferredSize(new java.awt.Dimension(350, 200));
        loadedExtensionTable.setModel(new RefreshTableModel(new Object[][]{}, new String[] {EXTENSION_TYPE_COLUMN_NAME,
            EXTENSION_NAME_COLUMN_NAME, EXTENSION_VERSION_COLUMN_NAME,
            EXTENSION_AUTHOR_COLUMN_NAME, EXTENSION_DESCRIPTION_COLUMN_NAME,
            EXTENSION_INSTALL_COLUMN_NAME}));
loadedExtensionScrollPane.setViewportView(loadedExtensionTable);

org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
jPanel1.setLayout(jPanel1Layout);
jPanel1Layout.setHorizontalGroup(
    jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
    .add(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jLabel2)
            .add(loadedExtensionScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 567, Short.MAX_VALUE)
            .add(jPanel1Layout.createSequentialGroup()
                .add(statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                .add(9, 9, 9)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(113, 113, 113)
                .add(installUpdatesButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(checkForUpdatesButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(closeButton)))
        .addContainerGap())
    );
    jPanel1Layout.setVerticalGroup(
        jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .add(jLabel2)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(loadedExtensionScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(statusLabel))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(closeButton)
                .add(checkForUpdatesButton)
                .add(installUpdatesButton))
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
// TODO add your handling code here:
    	//Probably should be a swing worker
    	
    	 PlatformUI.MIRTH_FRAME.setWorking("Checking for updates...", true);
         
         SwingWorker worker = new SwingWorker<Void, Void>()
         {
             public Void doInBackground()
             {
                 updateLoadedExtensionsTable();
                 return null;
             }
             
             public void done()
             {
            	 PlatformUI.MIRTH_FRAME.setWorking("", false);
             }
         };
         
         worker.execute();

    }//GEN-LAST:event_checkForUpdatesButtonActionPerformed

    private void installUpdatesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installUpdatesButtonActionPerformed
    	installUpdates();
    }//GEN-LAST:event_installUpdatesButtonActionPerformed
    
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeButtonActionPerformed
    {//GEN-HEADEREND:event_closeButtonActionPerformed
    	cancel = true;
        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton checkForUpdatesButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton installUpdatesButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane loadedExtensionScrollPane;
    private com.webreach.mirth.client.ui.components.MirthTable loadedExtensionTable;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables
    
}
