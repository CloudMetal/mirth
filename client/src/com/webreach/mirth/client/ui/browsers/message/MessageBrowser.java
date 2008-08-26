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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import java.lang.reflect.Constructor;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.DateFormatter;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.EDITokenMarker;
import org.syntax.jedit.tokenmarker.HL7TokenMarker;
import org.syntax.jedit.tokenmarker.X12TokenMarker;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;
import org.w3c.dom.Document;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.core.ListHandlerException;
import com.webreach.mirth.client.core.MessageListHandler;
import com.webreach.mirth.client.ui.EditMessageDialog;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.MirthFileFilter;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.RefreshTableModel;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.ViewContentDialog;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import com.webreach.mirth.client.ui.components.MirthSyntaxTextArea;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.client.ui.util.FileUtil;
import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.model.ExtensionPoint;
import com.webreach.mirth.model.ExtensionPointDefinition;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.converters.DocumentSerializer;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.model.util.ImportConverter;
import com.webreach.mirth.plugins.AttachmentViewer;

/**
 * The message browser panel.
 */
public class MessageBrowser extends javax.swing.JPanel
{
    private final int FIRST_PAGE = 0;
    private final int PREVIOUS_PAGE = -1;
    private final int NEXT_PAGE = 1;
    private final String MESSAGE_ID_COLUMN_NAME = "Message ID";
    private final String DATE_COLUMN_NAME = "Date";
    private final String CONNECTOR_COLUMN_NAME = "Connector";
    private final String STATUS_COLUMN_NAME = "Status";
    private final String SCOPE_COLUMN_NAME = "Scope";
    private final String KEY_COLUMN_NAME = "Variable";
    private final String VALUE_COLUMN_NAME = "Value";
    private final String TYPE_COLUMN_NAME = "Type";
    private final String SOURCE_COLUMN_NAME = "Source";
    private final String PROTOCOL_COLUMN_NAME = "Protocol";
    private final String NUMBER_COLUMN_NAME = "#";
    private final String ATTACHMENTID_COLUMN_NAME = "Attachment Id";
            
    private Frame parent;
    private MessageListHandler messageListHandler;
    private List<MessageObject> messageObjectList;
    private MessageObjectFilter messageObjectFilter;
    private int messageCount = -1;
    private int currentPage = 0;
    private int pageSize;
    private MessageBrowserAdvancedFilter advSearchFilterPopup;
    private Map<String, AttachmentViewer> loadedPanelPlugins = new HashMap<String, AttachmentViewer>();    
    private JPopupMenu attachmentPopupMenu;

    /**
     * Constructs the new message browser and sets up its default
     * information/layout.
     */
    public MessageBrowser()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        makeMessageTable();
        makeMappingsTable();
        loadPanelPlugins();
        updateAttachmentsTable(null, true);
        descriptionTabbedPane.remove(attachmentsPane);
        
        this.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                if (evt.isPopupTrigger())
                    parent.messagePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }

            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                if (evt.isPopupTrigger())
                    parent.messagePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        });

        String[] statusValues = new String[MessageObject.Status.values().length + 1];
        statusValues[0] = "ALL";
        for (int i = 1; i < statusValues.length; i++)
            statusValues[i] = MessageObject.Status.values()[i - 1].toString();

        statusComboBox.setModel(new javax.swing.DefaultComboBoxModel(statusValues));        
        
        pageSizeField.setDocument(new MirthFieldConstraints(3, false, false, true));
                
        messagePane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showMessagePopupMenu(evt, false);
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showMessagePopupMenu(evt, false);
            }
            
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectRows();
            }
        });
        attachmentsPane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectAttachmentRows();
            }
        });
        attachmentPopupMenu = new JPopupMenu();
        JMenuItem viewAttach = new JMenuItem("View Attachment");
        viewAttach.setIcon(new ImageIcon(Frame.class.getResource("images/attach.png")));
        viewAttach.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                viewAttachment();
            }
        });
        attachmentPopupMenu.add(viewAttach);

        advSearchFilterPopup = new MessageBrowserAdvancedFilter(parent, "Advanced Search Filter", true);
        advSearchFilterPopup.setVisible(false);
    }
    
    public Object[][] updateAttachmentList(MessageObject message){
        if(message == null) 
            return null;
        try {
            String attachMessId;
            if(message.getCorrelationId() != null) {
                attachMessId = message.getCorrelationId();
            }
            else {
                attachMessId = message.getId();
            }            
            List<Attachment> attachments = parent.mirthClient.getAttachmentIdsByMessageId(attachMessId);
            Iterator i = attachments.iterator();
            ArrayList attachData = new ArrayList();
            int count = 1;
            ArrayList<String> types = new ArrayList();
            // get arraylist of all types
            while(i.hasNext()){
                Attachment a = (Attachment) i.next();
                String type = a.getType();
                if(!types.contains(type)){
                    types.add(type);
                }
            }
            Iterator typesIterator = types.iterator();
            while(typesIterator.hasNext()){
                String type = (String) typesIterator.next();
                Iterator attachmentIterator = attachments.iterator();
                // If handle multiples
                if(getAttachmentViewer(type) != null && getAttachmentViewer(type).handleMultiple()){
                    String number = Integer.toString(count);
                    String attachment_Ids = "";
                    int j = 0;
                    while(attachmentIterator.hasNext()){
                        Attachment a = (Attachment) attachmentIterator.next();
                        if(type.equals(a.getType())){
                            if(attachment_Ids.equals("")){
                                attachment_Ids = a.getAttachmentId();
                            }
                            else {
                                count++;
                                attachment_Ids = attachment_Ids + ", " + a.getAttachmentId();
                            }
                        }
                    }   
                    if(!number.equals(Integer.toString(count))){
                        number = number + " - " + Integer.toString(count);
                    } 
                    Object[] rowData = new Object[3];
                    // add to attach Data
                    rowData[0] = number;
                    rowData[1] = type;
                    rowData[2] = attachment_Ids;
                    attachData.add(rowData);
                }
                // else do them seperate
                else {
                    while(attachmentIterator.hasNext()){
                        Attachment a = (Attachment) attachmentIterator.next();
                        if(a.getType().equals(type)){
                            Object[] rowData = new Object[3];
                            rowData[0] = Integer.toString(count);
                            rowData[1] = a.getType();
                            rowData[2] = a.getAttachmentId();
                            attachData.add(rowData);
                            count++;
                        }
                    }
                }
            }
            Object[][] temp = new Object[attachData.size()][3];
            Iterator varIter = attachData.iterator();
            int rowCount = 0;
            while(varIter.hasNext()){
                temp[rowCount] = (Object[]) varIter.next();
                rowCount++;
            }
            return temp;
        }
        catch(Exception e){
            e.printStackTrace();    
        }
        return null;
    }
    
    
    // Extension point for ExtensionPoint.Type.CLIENT_DASHBOARD_PANE
    @ExtensionPointDefinition(mode = ExtensionPoint.Mode.CLIENT, type = ExtensionPoint.Type.ATTACHMENT_VIEWER)
    public void loadPanelPlugins()
    {
        try
        {
            Map<String, PluginMetaData> plugins = parent.getPluginMetaData();
            for (PluginMetaData metaData : plugins.values())
            {
                if (metaData.isEnabled())
                {
                    for (ExtensionPoint extensionPoint : metaData.getExtensionPoints())
                    {
                        try
                        {
                            if (extensionPoint.getMode().equals(ExtensionPoint.Mode.CLIENT) && extensionPoint.getType().equals(ExtensionPoint.Type.ATTACHMENT_VIEWER) && extensionPoint.getClassName() != null && extensionPoint.getClassName().length() > 0)
                            {
                                String pluginName = extensionPoint.getName();
                                Class clazz = Class.forName(extensionPoint.getClassName());
                                Constructor[] constructors = clazz.getDeclaredConstructors();
                                for (int i=0; i < constructors.length; i++) {
                                    Class parameters[];
                                    parameters = constructors[i].getParameterTypes();
                                    // load plugin if the number of parameters is 1.
                                    if (parameters.length == 1) {

                                        AttachmentViewer attachmentViewer = (AttachmentViewer) constructors[i].newInstance(new Object[] { pluginName });
                                        loadedPanelPlugins.put(pluginName, attachmentViewer);
                                        i = constructors.length;
                                    }
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            parent.alertException(this, e.getStackTrace(), e.getMessage());
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
        }
    }
    public AttachmentViewer getAttachmentViewer(String type){
        if(loadedPanelPlugins.size() > 0)
        {
            for(AttachmentViewer plugin : loadedPanelPlugins.values())
            {
                if(plugin.getViewerType().equals(type)){
                    return plugin;
                }
            }
        }
        return null;
    }    
    
    /**
     * Loads up a clean message browser as if a new one was constructed.
     */
    public void loadNew()
    {
        // Set the default page size
        pageSize = Preferences.systemNodeForPackage(Mirth.class).getInt("messageBrowserPageSize", 20);
    	pageSizeField.setText(pageSize + "");
        
        // use the start filters and make the table.
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 6, -1, false);
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 7, 7, true);
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 9, 9, true);
        messageListHandler = null;
        
        statusComboBox.setSelectedIndex(0);

        advSearchFilterPopup.reset();

        long currentTime = System.currentTimeMillis();
        
        mirthDatePicker1.setDateInMillis(currentTime);
        mirthDatePicker2.setDateInMillis(currentTime);     
        
        mirthTimePicker1.setDate("00:00 am");
        mirthTimePicker2.setDate("11:59 pm");
        
        filterButtonActionPerformed(null);
        descriptionTabbedPane.setSelectedIndex(0);
    }
    
    /**
     * Refreshes the panel with the curent filter information.
     */
    public void refresh()
    {
        filterButtonActionPerformed(null);
    }
    
    public MessageObject getMessageObjectById(String messageId){
       if(messageObjectList != null){
           Iterator i = messageObjectList.iterator();
           while(i.hasNext()){
                MessageObject message = (MessageObject) i.next();
                if(message.getId().equals(messageId)){
                    return message;
                }
           }
       }  
       return null;
    }
    
    public void importMessages()
    {
        JFileChooser importFileChooser = new JFileChooser();
        importFileChooser.setFileFilter(new MirthFileFilter("XML"));
        
        File currentDir = new File(Preferences.systemNodeForPackage(Mirth.class).get("currentDirectory", ""));
        if (currentDir.exists())
            importFileChooser.setCurrentDirectory(currentDir);
        
        int returnVal = importFileChooser.showOpenDialog(this);
        File importFile = null;
        String channelId = parent.getSelectedChannelIdFromDashboard();
        
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            Preferences.systemNodeForPackage(Mirth.class).put("currentDirectory", importFileChooser.getCurrentDirectory().getPath());
            importFile = importFileChooser.getSelectedFile();
            String messageXML = "";
            BufferedReader br = null;
            
            try
            {
                String endOfMessage = "</com.webreach.mirth.model.MessageObject>";
                br = new BufferedReader(new FileReader(importFile));
                StringBuffer buffer = new StringBuffer();
                String line;
                
                while((line = br.readLine()) != null)
                {
                    buffer.append(line);
                
                    if(line.equals(endOfMessage))
                    {
                        messageXML = ImportConverter.convertMessage(buffer.toString());

                        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                        MessageObject importMessage;
                        importMessage = (MessageObject) serializer.fromXML(messageXML);
                        importMessage.setChannelId(channelId);
                            
                        try
                        {
                            importMessage.setId(parent.mirthClient.getGuid());
                            parent.mirthClient.importMessage(importMessage);
                        }
                        catch (Exception e)
                        {
                            parent.alertException(this, e.getStackTrace(),"Unable to connect to server. Stopping import. " + e.getMessage());
                            br.close();
                            return;
                        }
                        
                        buffer.delete(0, buffer.length());
                    }
                    
                }
                parent.alertInformation(this, "All messages have been successfully imported.");
                br.close();
            }
            catch (Exception e)
            {
                if(br != null)
                {    
                    try
                    {
                        br.close();
                    } 
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }
                parent.alertException(this, e.getStackTrace(), "Invalid message file. Message importing will stop. " + e.getMessage());
            }
        }
    }
    
    /**
     * Export the current messages to XML or HTML
     */
    public void exportMessages()
    {        
        JFileChooser exportFileChooser = new JFileChooser();
        
        File currentDir = new File(Preferences.systemNodeForPackage(Mirth.class).get("currentDirectory", ""));
        if (currentDir.exists())
            exportFileChooser.setCurrentDirectory(currentDir);
        
        exportFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        exportFileChooser.setFileFilter(new MirthFileFilter("XML"));
        int returnVal = exportFileChooser.showSaveDialog(parent);
        File exportFile = null;
        File exportDirectory = null;
        
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
        	MessageListHandler tempMessageListHandler = null;
            try
            {
                exportFile = exportFileChooser.getSelectedFile();
                int length = exportFile.getName().length();
                StringBuffer messages = new StringBuffer();

                if (exportFile.exists())
                    if (!parent.alertOption(this, "The file " + exportFile.getName() + " already exists.  Would you like to overwrite it?"))
                        return;
                
                if (length < 4 || !exportFile.getName().substring(length - 4, length).equals(".xml"))
                    exportFile = new File(exportFile.getAbsolutePath() + ".xml");
                
                FileUtil.write(exportFile, "", false);
                
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                
                tempMessageListHandler = parent.mirthClient.getMessageListHandler(messageListHandler.getFilter(), pageSize, true);
                List<MessageObject> messageObjects = tempMessageListHandler.getFirstPage();
                
                while(messageObjects.size() > 0)
                {
                    for (int i = 0; i < messageObjects.size(); i++)
                    {
                        messages.append(serializer.toXML(messageObjects.get(i)));
                        messages.append("\n");
                        FileUtil.write(exportFile, messages.toString(), true);
                        messages.delete(0,messages.length());
                    }
                    
                    messageObjects = tempMessageListHandler.getNextPage();
                }
                
                parent.alertInformation(this, "All messages were written successfully to " + exportFile.getPath() + ".");
            }
            catch (Exception ex)
            {
                parent.alertError(this, "File could not be written.");
            }
            finally
            {
            	if (tempMessageListHandler != null)
            	{
                    try
                    {
                            tempMessageListHandler.removeFilterTables();
                    }
                    catch (ClientException e)
                    {
                            parent.alertException(this, e.getStackTrace(), e.getMessage());
                    }
            	}
            }
        }
    }
    
    public void updateMessageTable(List<MessageObject> messageObjectList)
    {
        Object[][] tableData = null;

        if (messageObjectList != null)
        {
            tableData = new Object[messageObjectList.size()][7];
            
            for (int i = 0; i < messageObjectList.size(); i++)
            {
                MessageObject messageObject = messageObjectList.get(i);
                
                tableData[i][0] = messageObject.getId();
                
                Calendar calendar = messageObject.getDateCreated();
                
                tableData[i][1] = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS:%1$tL", calendar);
                tableData[i][2] = messageObject.getConnectorName();
                
                tableData[i][3] = messageObject.getType();
                tableData[i][4] = messageObject.getSource();
                tableData[i][5] = messageObject.getStatus();
                tableData[i][6] = messageObject.getRawDataProtocol();
            }
        }
        else
        {
            tableData = new Object[0][7];
        }
        
        int messageObjectListSize = 0;
        if (messageObjectList != null)
            messageObjectListSize = messageObjectList.size();
        
        if (currentPage == 0)
            previousPageButton.setEnabled(false);
        else
            previousPageButton.setEnabled(true);
        
        int numberOfPages = getNumberOfPages(pageSize, messageCount);
        if (messageObjectListSize < pageSize || pageSize == 0)
            nextPageButton.setEnabled(false);
        else if (currentPage == numberOfPages)
            nextPageButton.setEnabled(false);
        else
            nextPageButton.setEnabled(true);
        
        int startResult;
        if (messageObjectListSize == 0)
            startResult = 0;
        else
            startResult = (currentPage * pageSize) + 1;
        
        int endResult;
        if (pageSize == 0)
            endResult = messageObjectListSize;
        else
            endResult = (currentPage + 1) * pageSize;
        
        if (messageObjectListSize < pageSize)
            endResult = endResult - (pageSize - messageObjectListSize);
        
        if (messageCount == -1)
            resultsLabel.setText("Results " + startResult + " - " + endResult);
        else
            resultsLabel.setText("Results " + startResult + " - " + endResult + " of " + messageCount);

        if (messageTable != null)
        {
            //lastRow = messageTable.getSelectedRow();
            RefreshTableModel model = (RefreshTableModel) messageTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            messageTable = new MirthTable();
            messageTable.setModel(new RefreshTableModel(tableData, new String[] { MESSAGE_ID_COLUMN_NAME, DATE_COLUMN_NAME, CONNECTOR_COLUMN_NAME, TYPE_COLUMN_NAME, SOURCE_COLUMN_NAME, STATUS_COLUMN_NAME, PROTOCOL_COLUMN_NAME })
            {
                boolean[] canEdit = new boolean[] { false, false, false, false, false, false, false };
                
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }

        /*if (lastRow >= 0 && lastRow < messageTable.getRowCount())
            messageTable.setRowSelectionInterval(lastRow, lastRow);
        else
            lastRow = UIConstants.ERROR_CONSTANT;*/
        
        // Set highlighter.
        HighlighterPipeline highlighter = new HighlighterPipeline();
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
        }
        messageTable.setHighlighters(highlighter);
        deselectRows();
    }
    
    /**
     * Creates the table with all of the information given after being filtered
     * by the specified 'filter'
     */
    private void makeMessageTable()
    {
        updateMessageTable(null);     
        
        messageTable.setSelectionMode(0);
        
        messageTable.getColumnExt(MESSAGE_ID_COLUMN_NAME).setVisible(false);
        messageTable.getColumnExt(DATE_COLUMN_NAME).setMinWidth(100);
        
        messageTable.setRowHeight(UIConstants.ROW_HEIGHT);
        messageTable.setOpaque(true);
        messageTable.setRowSelectionAllowed(true);
        deselectRows();
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            messageTable.setHighlighters(highlighter);
        }
        
        messagePane.setViewportView(messageTable);
        jSplitPane1.setLeftComponent(messagePane);

        messageTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                MessageListSelected(evt);
            }
        });
        
        messageTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showMessagePopupMenu(evt, true);
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showMessagePopupMenu(evt, true);
            }
            
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                if (evt.getClickCount() >= 2)
                {
                    int row = getSelectedMessageIndex();
                    if(row >= 0)
                    {
                        MessageObject currentMessage = messageObjectList.get(row);
                        new EditMessageDialog(currentMessage);
                    }
                }
            }
        });
    }
    
    private void getMessageTableData(MessageListHandler handler, int page)
    {    	
        if (handler != null)
        {
            // Do all paging information below.
            try
            {
                messageCount = handler.getSize();
                currentPage = handler.getCurrentPage();
                pageSize = handler.getPageSize();
                
                if (page == FIRST_PAGE)
                {
                    messageObjectList = handler.getFirstPage();
                    currentPage = handler.getCurrentPage();
                }
                else if (page == PREVIOUS_PAGE)
                {
                    if (currentPage == 0)
                        return;
                    messageObjectList = handler.getPreviousPage();
                    currentPage = handler.getCurrentPage();
                }
                else if (page == NEXT_PAGE)
                {
                    int numberOfPages = getNumberOfPages(pageSize, messageCount);
                    if (currentPage == numberOfPages)
                        return;
                    
                    messageObjectList = handler.getNextPage();
                    if (messageObjectList.size() == 0)
                        messageObjectList = handler.getPreviousPage();
                    currentPage = handler.getCurrentPage();
                }
                
            }
            catch (ListHandlerException e)
            {
                messageObjectList = null;
                parent.alertException(this, e.getStackTrace(), e.getMessage());
            }
        }
    }
    
    private int getNumberOfPages(int pageSize, int messageCount)
    {
        int numberOfPages;
        if (messageCount == -1)
        	return -1;
        if (pageSize == 0)
            numberOfPages = 0;
        else
        {
            numberOfPages = messageCount / pageSize;
            if ((messageCount != 0) && ((messageCount % pageSize) == 0))
                numberOfPages--;
        }
        
        return numberOfPages;
    }
    
    public void updateMappingsTable(String[][] tableData, boolean cleared)
    {
         if (tableData == null || tableData.length == 0)
        {
            tableData = new String[1][3];
            if (cleared)
                tableData[0][1] = "Please select a message to view mappings.";
            else
                tableData[0][1] = "There are no mappings present.";
            tableData[0][0] = "";
            tableData[0][2] = "";
        }  
        
        if (mappingsTable != null)
        {
            RefreshTableModel model = (RefreshTableModel) mappingsTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            mappingsTable = new MirthTable();
            
            mappingsTable.setModel(new RefreshTableModel(tableData, new String[] { SCOPE_COLUMN_NAME, KEY_COLUMN_NAME, VALUE_COLUMN_NAME })
            {
                boolean[] canEdit = new boolean[] { false, false, false };
                
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }

        
        // Set highlighter.
        HighlighterPipeline highlighter = new HighlighterPipeline();
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
        }
        mappingsTable.setHighlighters(highlighter);
        
    }
    
    public void updateAttachmentsTable(MessageObject currentMessage, boolean cleared)
    {
        
        Object[][] tableData = updateAttachmentList(currentMessage);

        // Create attachment Table if it has not been created yet. 
        if (attachmentTable != null)
        {
            RefreshTableModel model = (RefreshTableModel) attachmentTable.getModel();
            if(tableData != null){
                model.refreshDataVector(tableData);
            }
        }
        else
        {
            attachmentTable = new MirthTable();
            attachmentTable.setModel(new RefreshTableModel(tableData, new String[] { NUMBER_COLUMN_NAME, TYPE_COLUMN_NAME, ATTACHMENTID_COLUMN_NAME})
            {
                boolean[] canEdit = new boolean[] { false, false, false };
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
            attachmentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent evt)
                {
                    if(attachmentTable != null && attachmentTable.getSelectedRow() != -1){
                        parent.setVisibleTasks(parent.messageTasks,parent.messagePopupMenu, 11,11,true);
                    }
                    else {
                        parent.setVisibleTasks(parent.messageTasks,parent.messagePopupMenu,11,11,false);
                    }
                }
            }); 
             // listen for trigger button and double click to edit channel.
            attachmentTable.addMouseListener(new java.awt.event.MouseAdapter()
            {
                public void mousePressed(java.awt.event.MouseEvent evt)
                {
                    showAttachmentPopupMenu(evt, true);
                }
                
                public void mouseReleased(java.awt.event.MouseEvent evt)
                {
                    showAttachmentPopupMenu(evt, true);
                }
                public void mouseClicked(java.awt.event.MouseEvent evt)
                {
                    if (evt.getClickCount() >= 2)
                    {
                        viewAttachment();// do view
                        
                    }
                }
            });    
            // Set highlighter.
            HighlighterPipeline highlighter = new HighlighterPipeline();
            if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
            {
                highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            }
            attachmentTable.setHighlighters(highlighter);    
            attachmentTable.setSelectionMode(0);
            attachmentTable.getColumnExt(NUMBER_COLUMN_NAME).setMinWidth(UIConstants.WIDTH_SHORT_MIN);
            attachmentTable.getColumnExt(NUMBER_COLUMN_NAME).setMaxWidth(UIConstants.WIDTH_SHORT_MAX);
            attachmentTable.getColumnExt(TYPE_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
            attachmentTable.getColumnExt(TYPE_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
            attachmentsPane.setViewportView(attachmentTable);                
        }
    }
    
    private void makeMappingsTable()
    {
        updateMappingsTable(null, true);
                
         // listen for trigger button and double click to edit channel.
        mappingsTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                if (evt.getClickCount() >= 2)
                {
                    new ViewContentDialog((String) mappingsTable.getModel().getValueAt(mappingsTable.convertRowIndexToModel(mappingsTable.getSelectedRow()), 2));
                }
            }
        });
        
        mappingsTable.setSelectionMode(0);
        mappingsTable.getColumnExt(SCOPE_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        mappingsTable.getColumnExt(SCOPE_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        
        // Disable HTML in a column.
        DefaultTableCellRenderer noHTMLRenderer = new DefaultTableCellRenderer();
        noHTMLRenderer.putClientProperty("html.disable", Boolean.TRUE);
        mappingsTable.getColumnExt(VALUE_COLUMN_NAME).setCellRenderer(noHTMLRenderer);
        
        mappingsPane.setViewportView(mappingsTable);
    }
    
    /**
     * Shows the trigger button (right-click) popup menu.
     */
    private void showMessagePopupMenu(java.awt.event.MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = messageTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                if (row > -1)
                {
                    messageTable.setRowSelectionInterval(row, row);
                }
            }
            else
                deselectRows();
            parent.messagePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    /**
     * Shows the trigger button (right-click) popup menu.
     */
    private void showAttachmentPopupMenu(java.awt.event.MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = attachmentTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                if (row > -1)
                {
                    attachmentTable.setRowSelectionInterval(row, row);
                }
            }
            else
                deselectAttachmentRows();
            attachmentPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }    
    
    /**
     * Deselects all rows in the table and clears the description information.
     */
    public void deselectRows()
    {
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 6, -1, false);
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 7, 7, true);
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 9, 9, true);
        if (messageTable != null)
        {
            messageTable.clearSelection();
            clearDescription();
        }
    }

    /**
     * Deselects all rows in the table and clears the description information.
     */
    public void deselectAttachmentRows()
    {
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 11, 11, false);
//        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 7, 7, true);
        if (attachmentTable != null)
        {
            attachmentTable.clearSelection();
//            clearDescription();
        }
    }
    
    /**
     * Clears all description information.
     */
    public void clearDescription()
    {
        RawMessageTextPane.setDocument(new SyntaxDocument());
        RawMessageTextPane.setText("Select a message to view the raw message.");
        TransformedMessageTextPane.setDocument(new SyntaxDocument());
        TransformedMessageTextPane.setText("Select a message to view the transformed message.");
        EncodedMessageTextPane.setDocument(new SyntaxDocument());
        EncodedMessageTextPane.setText("Select a message to view the encoded message.");
        ErrorsTextPane.setDocument(new SyntaxDocument());
        ErrorsTextPane.setText("Select a message to view any errors.");
        updateMappingsTable(new String[0][0], true);
        updateAttachmentsTable(null, true);
        descriptionTabbedPane.remove(attachmentsPane);
    }
    
    private int getSelectedMessageIndex()
    {
        int row = -1;
        if (messageTable.getSelectedRow() > -1)
        {
            row = messageTable.convertRowIndexToModel(messageTable.getSelectedRow());
        }
        return row;
    }
    
    /**
     * An action for when a row is selected in the table.
     */
    private void MessageListSelected(ListSelectionEvent evt)
    {
        if (!evt.getValueIsAdjusting())
        {
            int row = getSelectedMessageIndex();
            
            if (row >= 0)
            {
                parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 6, -1, true);
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                MessageObject currentMessage = messageObjectList.get(row);

                setCorrectDocument(RawMessageTextPane, currentMessage.getRawData(), currentMessage.getRawDataProtocol());
                setCorrectDocument(TransformedMessageTextPane, currentMessage.getTransformedData(), currentMessage.getTransformedDataProtocol());
                setCorrectDocument(EncodedMessageTextPane, currentMessage.getEncodedData(), currentMessage.getEncodedDataProtocol());
                setCorrectDocument(ErrorsTextPane, currentMessage.getErrors(), null);
                if(currentMessage.isAttachment()){
                    if(descriptionTabbedPane.indexOfTab("Attachments") == -1)
                        descriptionTabbedPane.addTab("Attachments", attachmentsPane);
                    updateAttachmentsTable(currentMessage,true);
                }
                else {
                    descriptionTabbedPane.remove(attachmentsPane);
                }
                Map connectorMap = currentMessage.getConnectorMap();
                Map channelMap = currentMessage.getChannelMap();
                Map responseMap = currentMessage.getResponseMap();
                
                String[][] tableData = new String[connectorMap.size() + channelMap.size() + responseMap.size()][3];
                int i = 0;
                
                Iterator connectorMapSetIterator = connectorMap.entrySet().iterator();
                for (; connectorMapSetIterator.hasNext(); i++)
                {
                    Entry variableMapEntry = (Entry) connectorMapSetIterator.next();
                    tableData[i][0] = "Connector";
                    tableData[i][1] = variableMapEntry.getKey().toString();
                    tableData[i][2] = variableMapEntry.getValue().toString();
                }
                
                Iterator channelMapSetIterator = channelMap.entrySet().iterator();
                for (; channelMapSetIterator.hasNext(); i++)
                {
                    Entry variableMapEntry = (Entry) channelMapSetIterator.next();
                    tableData[i][0] = "Channel";
                    tableData[i][1] = variableMapEntry.getKey().toString();
                    tableData[i][2] = variableMapEntry.getValue().toString();
                }
                
                Iterator responseMapSetIterator = responseMap.entrySet().iterator();
                for (; responseMapSetIterator.hasNext(); i++)
                {
                    Entry variableMapEntry = (Entry) responseMapSetIterator.next();
                    tableData[i][0] = "Response";
                    tableData[i][1] = variableMapEntry.getKey().toString();
                    tableData[i][2] = variableMapEntry.getValue().toString();
                }
                
                
                updateMappingsTable(tableData, false);
                
                if(attachmentTable == null || attachmentTable.getSelectedRow() == -1 || descriptionTabbedPane.indexOfTab("Attachments") == -1){
                    parent.setVisibleTasks(parent.messageTasks,parent.messagePopupMenu, 11,11,false);
                }                
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }
    
    private void setCorrectDocument(MirthSyntaxTextArea textPane, String message, MessageObject.Protocol protocol)
    {
        SyntaxDocument newDoc = new SyntaxDocument();
        
        if (message != null)
        {
            if (protocol != null)
            {
                if (protocol.equals(MessageObject.Protocol.HL7V2) || protocol.equals(MessageObject.Protocol.NCPDP) || protocol.equals(MessageObject.Protocol.DICOM))
                {
                    newDoc.setTokenMarker(new HL7TokenMarker());
                }
                else if (protocol.equals(MessageObject.Protocol.XML) || protocol.equals(Protocol.HL7V3))
                {
                    newDoc.setTokenMarker(new XMLTokenMarker());
                    DocumentSerializer serializer = new DocumentSerializer();
                    serializer.setPreserveSpace(false);
                   
                        try
                        {
                            Document doc = serializer.fromXML(message);
                            message = serializer.toXML(doc);
                        }
                        catch(Exception e)
                        {
                            System.out.println(e.getMessage());
                        }
                    
                }
                else if (protocol.equals(MessageObject.Protocol.X12))
                {
                    newDoc.setTokenMarker(new X12TokenMarker());
                }
                else if (protocol.equals(MessageObject.Protocol.EDI))
                {
                    newDoc.setTokenMarker(new EDITokenMarker());
                }
            }
            
            textPane.setDocument(newDoc);
            textPane.setText(message);
        }
        else
        {
            textPane.setDocument(newDoc);
            textPane.setText("");
        }
        
        textPane.setCaretPosition(0);
    }
    
    /**
     * Returns the ID of the selected message in the table.
     */
    public String getSelectedMessageID()
    {
        int column = -1;
        for (int i = 0; i < messageTable.getModel().getColumnCount(); i++)
        {
            if (messageTable.getModel().getColumnName(i).equals(MESSAGE_ID_COLUMN_NAME))
                column = i;
        }
        return ((String) messageTable.getModel().getValueAt(messageTable.convertRowIndexToModel(messageTable.getSelectedRow()), column));
    }
    
    /**
     * Returns the current MessageObjectFilter that is set.
     */
    public MessageObjectFilter getCurrentFilter()
    {
        return messageObjectFilter;
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        filterPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        resultsLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        pageSizeField = new com.webreach.mirth.client.ui.components.MirthTextField();
        previousPageButton = new javax.swing.JButton();
        nextPageButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        mirthDatePicker1 = new com.webreach.mirth.client.ui.components.MirthDatePicker();
        jLabel3 = new javax.swing.JLabel();
        mirthDatePicker2 = new com.webreach.mirth.client.ui.components.MirthDatePicker();
        jLabel2 = new javax.swing.JLabel();
        filterButton = new javax.swing.JButton();
        advSearchButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        statusComboBox = new javax.swing.JComboBox();
        mirthTimePicker1 = new com.webreach.mirth.client.ui.components.MirthTimePicker();
        mirthTimePicker2 = new com.webreach.mirth.client.ui.components.MirthTimePicker();
        jSplitPane1 = new javax.swing.JSplitPane();
        descriptionTabbedPane = new javax.swing.JTabbedPane();
        RawMessagePanel = new javax.swing.JPanel();
        RawMessageTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        TransformedMessagePanel = new javax.swing.JPanel();
        TransformedMessageTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        EncodedMessagePanel = new javax.swing.JPanel();
        EncodedMessageTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        mappingsPane = new javax.swing.JScrollPane();
        mappingsTable = null;
        ErrorsPanel = new javax.swing.JPanel();
        ErrorsTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        attachmentsPane = new javax.swing.JScrollPane();
        attachmentTable = null;
        messagePane = new javax.swing.JScrollPane();
        messageTable = null;

        setBackground(new java.awt.Color(255, 255, 255));
        filterPanel.setBackground(new java.awt.Color(255, 255, 255));
        filterPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        resultsLabel.setForeground(new java.awt.Color(204, 0, 0));
        resultsLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        resultsLabel.setText("Results");

        jLabel6.setText("Page Size:");

        previousPageButton.setText("<");
        previousPageButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                previousPageButtonActionPerformed(evt);
            }
        });

        nextPageButton.setText(">");
        nextPageButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                nextPageButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(83, Short.MAX_VALUE)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(resultsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 222, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jLabel6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pageSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 66, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(previousPageButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nextPageButton))))
        );

        jPanel3Layout.linkSize(new java.awt.Component[] {nextPageButton, previousPageButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .add(resultsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pageSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(previousPageButton)
                    .add(nextPageButton)))
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Search", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 0)));

        jLabel3.setText("Start Time:");

        jLabel2.setText("End Time:");

        filterButton.setText("Search");
        filterButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                filterButtonActionPerformed(evt);
            }
        });

        advSearchButton.setText("Advanced...");
        advSearchButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                advSearchButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("Status:");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel2)
                    .add(jLabel5)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(mirthDatePicker1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mirthTimePicker1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 93, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(statusComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(advSearchButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 93, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(mirthDatePicker2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mirthTimePicker2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 93, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filterButton)
                .add(27, 27, 27))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {advSearchButton, mirthTimePicker1, mirthTimePicker2}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mirthDatePicker1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3)
                    .add(mirthTimePicker1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(mirthDatePicker2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mirthTimePicker2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(statusComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(filterButton)
                    .add(advSearchButton)))
        );

        org.jdesktop.layout.GroupLayout filterPanelLayout = new org.jdesktop.layout.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 399, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.5);
        descriptionTabbedPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        descriptionTabbedPane.setFocusable(false);
        RawMessagePanel.setBackground(new java.awt.Color(255, 255, 255));
        RawMessagePanel.setFocusable(false);
        RawMessageTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        RawMessageTextPane.setEditable(false);

        org.jdesktop.layout.GroupLayout RawMessagePanelLayout = new org.jdesktop.layout.GroupLayout(RawMessagePanel);
        RawMessagePanel.setLayout(RawMessagePanelLayout);
        RawMessagePanelLayout.setHorizontalGroup(
            RawMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(RawMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(RawMessageTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE)
                .addContainerGap())
        );
        RawMessagePanelLayout.setVerticalGroup(
            RawMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(RawMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(RawMessageTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionTabbedPane.addTab("Raw Message", RawMessagePanel);

        TransformedMessagePanel.setBackground(new java.awt.Color(255, 255, 255));
        TransformedMessagePanel.setFocusable(false);
        TransformedMessageTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        TransformedMessageTextPane.setEditable(false);

        org.jdesktop.layout.GroupLayout TransformedMessagePanelLayout = new org.jdesktop.layout.GroupLayout(TransformedMessagePanel);
        TransformedMessagePanel.setLayout(TransformedMessagePanelLayout);
        TransformedMessagePanelLayout.setHorizontalGroup(
            TransformedMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(TransformedMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(TransformedMessageTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE)
                .addContainerGap())
        );
        TransformedMessagePanelLayout.setVerticalGroup(
            TransformedMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(TransformedMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(TransformedMessageTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionTabbedPane.addTab("Transformed Message", TransformedMessagePanel);

        EncodedMessagePanel.setBackground(new java.awt.Color(255, 255, 255));
        EncodedMessagePanel.setFocusable(false);
        EncodedMessageTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        EncodedMessageTextPane.setEditable(false);

        org.jdesktop.layout.GroupLayout EncodedMessagePanelLayout = new org.jdesktop.layout.GroupLayout(EncodedMessagePanel);
        EncodedMessagePanel.setLayout(EncodedMessagePanelLayout);
        EncodedMessagePanelLayout.setHorizontalGroup(
            EncodedMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(EncodedMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(EncodedMessageTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE)
                .addContainerGap())
        );
        EncodedMessagePanelLayout.setVerticalGroup(
            EncodedMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(EncodedMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(EncodedMessageTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionTabbedPane.addTab("Encoded Message", EncodedMessagePanel);

        mappingsPane.setViewportView(mappingsTable);

        descriptionTabbedPane.addTab("Mappings", mappingsPane);

        ErrorsPanel.setBackground(new java.awt.Color(255, 255, 255));
        ErrorsPanel.setFocusable(false);
        ErrorsTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ErrorsTextPane.setEditable(false);

        org.jdesktop.layout.GroupLayout ErrorsPanelLayout = new org.jdesktop.layout.GroupLayout(ErrorsPanel);
        ErrorsPanel.setLayout(ErrorsPanelLayout);
        ErrorsPanelLayout.setHorizontalGroup(
            ErrorsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ErrorsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(ErrorsTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE)
                .addContainerGap())
        );
        ErrorsPanelLayout.setVerticalGroup(
            ErrorsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ErrorsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(ErrorsTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionTabbedPane.addTab("Errors", ErrorsPanel);

        attachmentsPane.setViewportView(attachmentTable);

        descriptionTabbedPane.addTab("Attachments", attachmentsPane);

        jSplitPane1.setRightComponent(descriptionTabbedPane);

        messagePane.setViewportView(messageTable);

        jSplitPane1.setLeftComponent(messagePane);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 742, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(filterPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public void viewAttachment(){    
        String attachId = (String) attachmentTable.getModel().getValueAt(attachmentTable.convertRowIndexToModel(attachmentTable.getSelectedRow()), 2);
        final String attachType = (String) attachmentTable.getModel().getValueAt(attachmentTable.convertRowIndexToModel(attachmentTable.getSelectedRow()), 1);
        String[] attachmentIdArray = attachId.split(", ");
        ArrayList<String> attachmentIds = new ArrayList<String>();
        for(int i=0;i<attachmentIdArray.length;i++){
        	attachmentIds.add(attachmentIdArray[i]);    
        }
        try {
            final AttachmentViewer attachmentViewer = getAttachmentViewer(attachType);
            final ArrayList<String> finalAttachmentIds = attachmentIds;
            if(attachmentViewer != null){
            	
                parent.setWorking("Loading " + attachType + " viewer...", true);
                
                SwingWorker worker = new SwingWorker<Void, Void>()
                {
                    public Void doInBackground()
                    {
                    	attachmentViewer.viewAttachments(finalAttachmentIds);
                        return null;
                    }
                    
                    public void done()
                    {
                        parent.setWorking("", false);
                    }
                };
                worker.execute();
            }                
            else {
                parent.alertInformation(this, "No Attachment Viewer plugin installed for type: " + attachType);
            }
        }
        catch(Exception e){}
       
    }
    
    private void advSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advSearchButtonActionPerformed

        // display the advanced search filter pop up window.
        String connector = advSearchFilterPopup.getConnector();
        String messageSource = advSearchFilterPopup.getMessageSource();
        String messageType = advSearchFilterPopup.getMessageType();
        String containingKeyword = advSearchFilterPopup.getContainingKeyword();
        boolean includeRawMessage = advSearchFilterPopup.isIncludeRawMessage();
        boolean includeTransformedMessage = advSearchFilterPopup.isIncludeTransformedMessage();
        boolean includeEncodedMessage = advSearchFilterPopup.isIncludeEncodedMessage();
        String protocol = advSearchFilterPopup.getProtocol();

        advSearchFilterPopup = new MessageBrowserAdvancedFilter(parent, "Advanced Search Filter", true);
        advSearchFilterPopup.setFieldValues(connector, messageSource, messageType, containingKeyword, includeRawMessage, includeTransformedMessage, includeEncodedMessage, protocol);
        
        advSearchFilterPopup.setVisible(true);
        
    }//GEN-LAST:event_advSearchButtonActionPerformed
    
    private void nextPageButtonActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_nextPageButtonActionPerformed
        parent.setWorking("Loading next page...", true);
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
            	getMessageTableData(messageListHandler, NEXT_PAGE);
                return null;
            }
            
            public void done()
            {
                if (messageListHandler != null) {
                    updateMessageTable(messageObjectList);
                } else {
                    updateMessageTable(null);
                }
                parent.setWorking("", false);
            }
        };
        worker.execute();
        
    }// GEN-LAST:event_nextPageButtonActionPerformed
    
    private void previousPageButtonActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_previousPageButtonActionPerformed
        parent.setWorking("Loading previous page...", true);
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
            	getMessageTableData(messageListHandler, PREVIOUS_PAGE);
                return null;
            }
            
            public void done()
            {
                if (messageListHandler != null) {
                    updateMessageTable(messageObjectList);
                } else {
                    updateMessageTable(null);
                }
                parent.setWorking("", false);
            }
        };
        worker.execute();
    }// GEN-LAST:event_previousPageButtonActionPerformed
    
    /**
     * An action when the filter button is pressed. Creates the actual filter
     * and remakes the table with that filter.
     */
    private void filterButtonActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_filterButtonActionPerformed      
        messageObjectFilter = new MessageObjectFilter();
        
        if (mirthDatePicker1.getDate() != null && mirthDatePicker2.getDate() != null && 
                mirthTimePicker1.getDate() != null && mirthTimePicker2.getDate() != null)
        {
            SimpleDateFormat timeDateFormat = new SimpleDateFormat("hh:mm aa");
            DateFormatter timeFormatter = new DateFormatter(timeDateFormat);
            
            Date startDate = mirthDatePicker1.getDate();
            Date endDate = mirthDatePicker2.getDate();
            
            String startTime = mirthTimePicker1.getDate();
            String endTime = mirthTimePicker2.getDate();
            
            Date startTimeDate;
            Date endTimeDate;
            
            try
            {
                startTimeDate = (Date)timeFormatter.stringToValue(startTime);
                endTimeDate = (Date)timeFormatter.stringToValue(endTime);
            }
            catch(Exception e)
            {
                parent.alertError(this, "Invalid date.");
                return;
            }

            Calendar startDateCalendar = Calendar.getInstance();
            Calendar endDateCalendar = Calendar.getInstance();
            Calendar startTimeCalendar = Calendar.getInstance();
            Calendar endTimeCalendar = Calendar.getInstance();

            startDateCalendar.setTime(startDate);
            endDateCalendar.setTime(endDate);
            startTimeCalendar.setTime(startTimeDate);
            endTimeCalendar.setTime(endTimeDate);

            Calendar startCalendar = Calendar.getInstance();
            Calendar endCalendar = Calendar.getInstance();
            
            startCalendar.set(startDateCalendar.get(Calendar.YEAR), startDateCalendar.get(Calendar.MONTH), startDateCalendar.get(Calendar.DATE), startTimeCalendar.get(Calendar.HOUR_OF_DAY), startTimeCalendar.get(Calendar.MINUTE), startTimeCalendar.get(Calendar.SECOND));
            endCalendar.set(endDateCalendar.get(Calendar.YEAR), endDateCalendar.get(Calendar.MONTH), endDateCalendar.get(Calendar.DATE), endTimeCalendar.get(Calendar.HOUR_OF_DAY), endTimeCalendar.get(Calendar.MINUTE), endTimeCalendar.get(Calendar.SECOND));
            
            if (startCalendar.getTimeInMillis() > endCalendar.getTimeInMillis())
        {
                parent.alertError(this, "Start date cannot be after the end date.");
                return;
        }
            
            messageObjectFilter.setStartDate(startCalendar);
            messageObjectFilter.setEndDate(endCalendar);
            
        }

        messageObjectFilter.setChannelId(parent.getSelectedChannelIdFromDashboard());

        if (!((String) statusComboBox.getSelectedItem()).equalsIgnoreCase("ALL"))
        {
            for (int i = 0; i < MessageObject.Status.values().length; i++)
            {
                if (((String) statusComboBox.getSelectedItem()).equalsIgnoreCase(MessageObject.Status.values()[i].toString()))
                    messageObjectFilter.setStatus(MessageObject.Status.values()[i]);
            }
        }

        // Get the advanced search criteria.        
        if (!advSearchFilterPopup.getConnector().equals("")) {
            messageObjectFilter.setConnectorName(advSearchFilterPopup.getConnector());
        }
        if (!advSearchFilterPopup.getMessageSource().equals("")) {
            messageObjectFilter.setSource(advSearchFilterPopup.getMessageSource());
        }
        if (!advSearchFilterPopup.getMessageType().equals("")) {
            messageObjectFilter.setType(advSearchFilterPopup.getMessageType());
        }
        if (!advSearchFilterPopup.getContainingKeyword().equals("")) {
            messageObjectFilter.setSearchCriteria(advSearchFilterPopup.getContainingKeyword());
        }
        if (advSearchFilterPopup.isIncludeRawMessage()) {
            messageObjectFilter.setSearchRawData(true);
        }
        if (advSearchFilterPopup.isIncludeTransformedMessage()) {
            messageObjectFilter.setSearchTransformedData(true);
        }
        if (advSearchFilterPopup.isIncludeEncodedMessage()) {
            messageObjectFilter.setSearchEncodedData(true);
        }

        if (advSearchFilterPopup.getProtocol().equalsIgnoreCase("ALL")) {
            // clear the protocol search criteria.
            messageObjectFilter.setProtocol(null);
        } else {
            for (int i=0; i < MessageObject.Protocol.values().length; i++) {
                if (advSearchFilterPopup.getProtocol().equalsIgnoreCase(MessageObject.Protocol.values()[i].toString())) {
                    messageObjectFilter.setProtocol(MessageObject.Protocol.values()[i]);
                }
            }
        }

        if (!pageSizeField.getText().equals(""))
            pageSize = Integer.parseInt(pageSizeField.getText());

        parent.setWorking("Loading messages...", true);
        
        if (messageListHandler == null)
            updateMessageTable(null);

        class MessageWorker extends SwingWorker<Void, Void>
        {
            public Void doInBackground()
            {
                try
                {
                    messageListHandler = parent.mirthClient.getMessageListHandler(messageObjectFilter, pageSize, false);
                }
                catch (ClientException e)
                {
                    parent.alertException(parent, e.getStackTrace(), e.getMessage());
                }
                getMessageTableData(messageListHandler, FIRST_PAGE);
                return null;
            }

            public void done()
            {
                if (messageListHandler != null) {
                    updateMessageTable(messageObjectList);
                } else {
                    updateMessageTable(null);
                }
                parent.setWorking("", false);
            }
        };
        MessageWorker worker = new MessageWorker();
        worker.execute();
    }// GEN-LAST:event_filterButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel EncodedMessagePanel;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea EncodedMessageTextPane;
    private javax.swing.JPanel ErrorsPanel;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea ErrorsTextPane;
    private javax.swing.JPanel RawMessagePanel;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea RawMessageTextPane;
    private javax.swing.JPanel TransformedMessagePanel;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea TransformedMessageTextPane;
    private javax.swing.JButton advSearchButton;
    private com.webreach.mirth.client.ui.components.MirthTable attachmentTable;
    private javax.swing.JScrollPane attachmentsPane;
    private javax.swing.JTabbedPane descriptionTabbedPane;
    private javax.swing.JButton filterButton;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JScrollPane mappingsPane;
    private com.webreach.mirth.client.ui.components.MirthTable mappingsTable;
    private javax.swing.JScrollPane messagePane;
    private com.webreach.mirth.client.ui.components.MirthTable messageTable;
    private com.webreach.mirth.client.ui.components.MirthDatePicker mirthDatePicker1;
    private com.webreach.mirth.client.ui.components.MirthDatePicker mirthDatePicker2;
    private com.webreach.mirth.client.ui.components.MirthTimePicker mirthTimePicker1;
    private com.webreach.mirth.client.ui.components.MirthTimePicker mirthTimePicker2;
    private javax.swing.JButton nextPageButton;
    private com.webreach.mirth.client.ui.components.MirthTextField pageSizeField;
    private javax.swing.JButton previousPageButton;
    private javax.swing.JLabel resultsLabel;
    private javax.swing.JComboBox statusComboBox;
    // End of variables declaration//GEN-END:variables
    
}
