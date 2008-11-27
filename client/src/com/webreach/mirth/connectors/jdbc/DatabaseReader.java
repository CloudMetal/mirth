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

package com.webreach.mirth.connectors.jdbc;

import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.parsers.DocumentBuilderFactory;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;
import org.syntax.jedit.tokenmarker.TSQLTokenMarker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import com.webreach.mirth.client.ui.util.SQLParserUtil;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.connectors.jdbc.DatabaseMetadataDialog.STATEMENT_TYPE;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.DriverInfo;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.DocumentSerializer;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class DatabaseReader extends ConnectorClass
{
    /** Creates new form DatabaseWriter */
    
    private static SyntaxDocument sqlMappingDoc;
    
    private static SyntaxDocument sqlUpdateMappingDoc;
    
    private static SyntaxDocument jsMappingDoc;
    
    private static SyntaxDocument jsUpdateMappingDoc;
    
    private List<DriverInfo> drivers;
    
    private Timer timer;
    
    public DatabaseReader()
    {
        name = DatabaseReaderProperties.name;
        
        try
        {
            drivers = this.parent.mirthClient.getDatabaseDrivers();
        }
        catch (ClientException e)
        {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
        }
        
        initComponents();
        
        drivers.add(0, new DriverInfo("Please Select One", "Please Select One"));
        String[] driverNames = new String[drivers.size()];
        
        for (int i = 0; i < drivers.size(); i++)
        {
            driverNames[i] = drivers.get(i).getName();
        }
        
        databaseDriverCombobox.setModel(new javax.swing.DefaultComboBoxModel(driverNames));
        
        sqlMappingDoc = new SyntaxDocument();
        sqlMappingDoc.setTokenMarker(new TSQLTokenMarker());
        sqlUpdateMappingDoc = new SyntaxDocument();
        sqlUpdateMappingDoc.setTokenMarker(new TSQLTokenMarker());
        jsMappingDoc = new SyntaxDocument();
        jsMappingDoc.setTokenMarker(new JavaScriptTokenMarker());
        jsUpdateMappingDoc = new SyntaxDocument();
        jsUpdateMappingDoc.setTokenMarker(new JavaScriptTokenMarker());
        
        databaseSQLTextPane.setDocument(sqlMappingDoc);
        databaseUpdateSQLTextPane.setDocument(sqlUpdateMappingDoc);
        
        sqlMappingDoc.addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent e)
            {
            }
            
            public void removeUpdate(DocumentEvent e)
            {
                update();
            }
            
            public void insertUpdate(DocumentEvent e)
            {
                update();
            }
        });
        
        jsMappingDoc.addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent e)
            {
            }
            
            public void removeUpdate(DocumentEvent e)
            {
                update();
            }
            
            public void insertUpdate(DocumentEvent e)
            {
                update();
            }
        });
        
        pollingFrequency.setDocument(new MirthFieldConstraints(0, false, false, true));
    }
    
    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DatabaseReaderProperties.DATATYPE, name);
        properties.put(DatabaseReaderProperties.DATABASE_HOST, DatabaseReaderProperties.DATABASE_HOST_VALUE);
        
        for (int i = 0; i < drivers.size(); i++)
        {
            DriverInfo driver = drivers.get(i);
            if (driver.getName().equalsIgnoreCase(((String) databaseDriverCombobox.getSelectedItem())))
                properties.put(DatabaseReaderProperties.DATABASE_DRIVER, driver.getClassName());
        }
        
        properties.put(DatabaseReaderProperties.DATABASE_URL, databaseURLField.getText());
        properties.put(DatabaseReaderProperties.DATABASE_USERNAME, databaseUsernameField.getText());
        properties.put(DatabaseReaderProperties.DATABASE_PASSWORD, new String(databasePasswordField.getPassword()));
        
        if (processResultsInOrderYesButton.isSelected())
            properties.put(DatabaseReaderProperties.DATABASE_PROCESS_RESULTS_IN_ORDER, UIConstants.YES_OPTION);
        else
            properties.put(DatabaseReaderProperties.DATABASE_PROCESS_RESULTS_IN_ORDER, UIConstants.NO_OPTION);
        
        if (useJavaScriptYes.isSelected())
        {
            properties.put(DatabaseReaderProperties.DATABASE_USE_JS, UIConstants.YES_OPTION);
            properties.put(DatabaseReaderProperties.DATABASE_JS_SQL_STATEMENT, databaseSQLTextPane.getText());
            properties.put(DatabaseReaderProperties.DATABASE_JS_ACK, databaseUpdateSQLTextPane.getText());
            properties.put(DatabaseReaderProperties.DATABASE_SQL_STATEMENT, "");
            properties.put(DatabaseReaderProperties.DATABASE_ACK, "");
        }
        else
        {
            properties.put(DatabaseReaderProperties.DATABASE_USE_JS, UIConstants.NO_OPTION);
            properties.put(DatabaseReaderProperties.DATABASE_SQL_STATEMENT, databaseSQLTextPane.getText());
            properties.put(DatabaseReaderProperties.DATABASE_ACK, databaseUpdateSQLTextPane.getText());
            properties.put(DatabaseReaderProperties.DATABASE_JS_SQL_STATEMENT, "");
            properties.put(DatabaseReaderProperties.DATABASE_JS_ACK, "");
        }
        
        if (readOnUpdateYes.isSelected())
            properties.put(DatabaseReaderProperties.DATABASE_USE_ACK, UIConstants.YES_OPTION);
        else
            properties.put(DatabaseReaderProperties.DATABASE_USE_ACK, UIConstants.NO_OPTION);
        
        if (pollingIntervalButton.isSelected())
        {
            properties.put(DatabaseReaderProperties.DATABASE_POLLING_TYPE, "interval");
            properties.put(DatabaseReaderProperties.DATABASE_POLLING_FREQUENCY, pollingFrequency.getText());
        }
        else
        {
            properties.put(DatabaseReaderProperties.DATABASE_POLLING_TYPE, "time");
            properties.put(DatabaseReaderProperties.DATABASE_POLLING_TIME, pollingTime.getDate());
        }
        
        return properties;
    }
    
    public void setProperties(Properties props)
    {
        resetInvalidProperties();
        
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();
        
        for (int i = 0; i < drivers.size(); i++)
        {
            DriverInfo driver = drivers.get(i);
            if (driver.getClassName().equalsIgnoreCase(((String) props.get(DatabaseReaderProperties.DATABASE_DRIVER))))
                databaseDriverCombobox.setSelectedItem(driver.getName());
        }
        
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
        databaseURLField.setText((String) props.get(DatabaseReaderProperties.DATABASE_URL));
        databaseUsernameField.setText((String) props.get(DatabaseReaderProperties.DATABASE_USERNAME));
        databasePasswordField.setText((String) props.get(DatabaseReaderProperties.DATABASE_PASSWORD));
        databaseSQLTextPane.setText((String) props.get(DatabaseReaderProperties.DATABASE_SQL_STATEMENT));
        
        if (((String) props.get(DatabaseReaderProperties.DATABASE_PROCESS_RESULTS_IN_ORDER)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            processResultsInOrderYesButton.setSelected(true);
        }
        else
        {
        	processResultsInOrderNoButton.setSelected(true);
        }
        
        if (((String) props.get(DatabaseReaderProperties.DATABASE_USE_JS)).equals(UIConstants.YES_OPTION))
        {
            useJavaScriptYes.setSelected(true);
            useJavaScriptYesActionPerformed(null);
            databaseSQLTextPane.setText((String) props.get(DatabaseReaderProperties.DATABASE_JS_SQL_STATEMENT));
            databaseUpdateSQLTextPane.setText((String) props.get(DatabaseReaderProperties.DATABASE_JS_ACK));
        }
        else
        {
            useJavaScriptNo.setSelected(true);
            useJavaScriptNoActionPerformed(null);
            databaseSQLTextPane.setText((String) props.get(DatabaseReaderProperties.DATABASE_SQL_STATEMENT));
            databaseUpdateSQLTextPane.setText((String) props.get(DatabaseReaderProperties.DATABASE_ACK));
        }
        
        if (((String) props.get(DatabaseReaderProperties.DATABASE_USE_ACK)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            readOnUpdateYes.setSelected(true);
            readOnUpdateYesActionPerformed(null);
        }
        else
        {
            readOnUpdateNo.setSelected(true);
            readOnUpdateNoActionPerformed(null);
        }
        
        if (((String) props.get(DatabaseReaderProperties.DATABASE_POLLING_TYPE)).equalsIgnoreCase("interval"))
        {
            pollingIntervalButton.setSelected(true);
            pollingIntervalButtonActionPerformed(null);
            pollingFrequency.setText((String) props.get(DatabaseReaderProperties.DATABASE_POLLING_FREQUENCY));
        }
        else
        {
            pollingTimeButton.setSelected(true);
            pollingTimeButtonActionPerformed(null);
            pollingTime.setDate((String) props.get(DatabaseReaderProperties.DATABASE_POLLING_TIME));
        }
    }
    
    public Properties getDefaults()
    {
        return new DatabaseReaderProperties().getDefaults();
    }
    
    public boolean checkProperties(Properties props, boolean highlight)
    {
        resetInvalidProperties();
        boolean valid = true;
        
        if (((String) props.get(DatabaseReaderProperties.DATABASE_URL)).length() == 0)
        {
            valid = false;
            if (highlight)
            	databaseURLField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(DatabaseReaderProperties.DATABASE_POLLING_TYPE)).equalsIgnoreCase("interval") && ((String) props.get(DatabaseReaderProperties.DATABASE_POLLING_FREQUENCY)).length() == 0)
        {
            valid = false;
            if (highlight)
            	pollingFrequency.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(DatabaseReaderProperties.DATABASE_POLLING_TYPE)).equalsIgnoreCase("time") && ((String) props.get(DatabaseReaderProperties.DATABASE_POLLING_TIME)).length() == 0)
        {
            valid = false;
            if (highlight)
            	pollingTime.setBackground(UIConstants.INVALID_COLOR);
        }
        if ((((String) props.get(DatabaseReaderProperties.DATABASE_SQL_STATEMENT)).length() == 0) && (((String) props.get(DatabaseReaderProperties.DATABASE_JS_SQL_STATEMENT)).length() == 0))
        {
            valid = false;
            if (highlight)
            	databaseSQLTextPane.setBackground(UIConstants.INVALID_COLOR);
        }
        
        if (((String) props.get(DatabaseReaderProperties.DATABASE_USE_ACK)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            if (((((String) props.get(DatabaseReaderProperties.DATABASE_JS_ACK)).length() == 0) && (((String) props.get(DatabaseReaderProperties.DATABASE_ACK)).length() == 0)))
            {
                valid = false;
                if (highlight)
                	databaseUpdateSQLTextPane.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if ((((String) props.get(DatabaseReaderProperties.DATABASE_DRIVER)).equals("Please Select One")))
        {
            valid = false;
            if (highlight)
            	databaseDriverCombobox.setBackground(UIConstants.INVALID_COLOR);
        }
        
        return valid;
    }
    
    private void resetInvalidProperties()
    {
        databaseURLField.setBackground(null);
        pollingFrequency.setBackground(null);
        pollingTime.setBackground(null);
        databaseSQLTextPane.setBackground(null);
        databaseUpdateSQLTextPane.setBackground(null);
        databaseDriverCombobox.setBackground(UIConstants.COMBO_BOX_BACKGROUND);
    }
    
    public String doValidate(Properties props, boolean highlight)
    {
    	String error = null;
    	
    	if (!checkProperties(props, highlight))
    		error = "Error in the form for connector \"" + getName() + "\".\n\n";
    	
    	String script = ((String) props.get(DatabaseReaderProperties.DATABASE_JS_SQL_STATEMENT));
    	String onUpdateScript = ((String) props.get(DatabaseReaderProperties.DATABASE_JS_ACK));
    	
    	if (script.length() != 0)
    	{
	    	Context context = Context.enter();
	        try
	        {
	            context.compileString("function rhinoWrapper() {" + script + "\n}", UUID.randomUUID().toString(), 1, null);
	        }
	        catch (EvaluatorException e)
	        {
	        	if (error == null)
	        		error = "";
	            error += "Error in connector \"" + getName() + "\" at Javascript:\nError on line " + e.lineNumber() + ": " + e.getMessage() + ".\n\n";
	        }
	        catch (Exception e)
	        {
	        	if (error == null)
	        		error = "";
	        	error += "Error in connector \"" + getName() + "\" at Javascript:\nUnknown error occurred during validation.";
	        }
	        
	        Context.exit();
    	}
    	
    	if (((String) props.get(DatabaseReaderProperties.DATABASE_USE_ACK)).equalsIgnoreCase(UIConstants.YES_OPTION) && onUpdateScript.length() != 0)
    	{
	    	Context context = Context.enter();
	        try
	        {
	            context.compileString("function rhinoWrapper() {" + onUpdateScript + "\n}", UUID.randomUUID().toString(), 1, null);
	        }
	        catch (EvaluatorException e)
	        {
	        	if (error == null)
	        		error = "";
	            error += "Error in connector \"" + getName() + "\" at On-Update Javascript:\nError on line " + e.lineNumber() + ": " + e.getMessage() + ".\n\n";
	        }
	        catch (Exception e)
	        {
	        	if (error == null)
	        		error = "";
	        	error += "Error in connector \"" + getName() + "\" at Javascript:\nUnknown error occurred during validation.";
	        }
	        
	        Context.exit();
    	}
	        
        return error;
    }

    private void update()
    {
        class UpdateTimer extends TimerTask
        {
            public void run()
            {
                
                PlatformUI.MIRTH_FRAME.setWorking("Parsing...", true);
                updateSQL();
                PlatformUI.MIRTH_FRAME.setWorking("", false);
            }
        }
        if (timer == null)
        {
            timer = new Timer();
            timer.schedule(new UpdateTimer(), 1000);
        }
        else
        {
            timer.cancel();
            timer = new Timer();
            timer.schedule(new UpdateTimer(), 1000);
        }
    }
    
    private void updateSQL()
    {
        Object sqlStatement = databaseSQLTextPane.getText();
        String[] data;
        
        if ((sqlStatement != null) && (!sqlStatement.equals("")))
        {
            SQLParserUtil spu = new SQLParserUtil((String) sqlStatement);
            data = spu.Parse();
            
        }
        else
        {
            data = new String[] {};
        }
        dbVarList.setListData(data);
        updateIncomingData(data);
    }
    
    private void updateIncomingData(String[] data)
    {
        try
        {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element resultElement = document.createElement("result");
            for (int i = 0; i < data.length; i++)
            {
                Element columnElement = document.createElement(data[i]);
                columnElement.setTextContent("value");
                resultElement.appendChild(columnElement);
            }
            document.appendChild(resultElement);
            
            DocumentSerializer docSerializer = new DocumentSerializer();
            docSerializer.setPreserveSpace(false);
            
            String xml = docSerializer.toXML(document);
            
//            parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().setInboundTemplate(xml.replaceAll("\\r\\n", "\n"));  // Not required with current text area
            parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().setInboundTemplate(xml);
            
            if (parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundProtocol() == MessageObject.Protocol.XML
                    && parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundTemplate() != null
                    && parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundTemplate().length() == 0)
            {
                List<Connector> list = parent.channelEditPanel.currentChannel.getDestinationConnectors();
                for (Connector c : list)
                {
//                    c.getTransformer().setInboundTemplate(xml.replaceAll("\\r\\n", "\n"));  // Not required with current text area
                    c.getTransformer().setInboundTemplate(xml);
                }
            }
        }
        catch (Exception ex)
        {
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        sqlLabel = new javax.swing.JLabel();
        databaseDriverCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        databaseURLField = new com.webreach.mirth.client.ui.components.MirthTextField();
        databaseUsernameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        databasePasswordField = new com.webreach.mirth.client.ui.components.MirthPasswordField();
        onUpdateLabel = new javax.swing.JLabel();
        pollingFrequencyLabel = new javax.swing.JLabel();
        pollingFrequency = new com.webreach.mirth.client.ui.components.MirthTextField();
        readOnUpdateYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        readOnUpdateNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        dbVarList = new com.webreach.mirth.client.ui.components.MirthVariableList();
        databaseSQLTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea(true,false);
        databaseUpdateSQLTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea(true,false);
        jLabel6 = new javax.swing.JLabel();
        useJavaScriptYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        useJavaScriptNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        generateConnection = new javax.swing.JButton();
        generateUpdateConnection = new javax.swing.JButton();
        pollingTimeButton = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        pollingIntervalButton = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel5 = new javax.swing.JLabel();
        pollingTimeLabel = new javax.swing.JLabel();
        pollingTime = new com.webreach.mirth.client.ui.components.MirthTimePicker();
        generateSelect = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        generateUpdateSelect = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        processResultsInOrderNoButton = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        processResultsInOrderYesButton = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel10 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel1.setText("Driver:");

        jLabel2.setText("URL:");

        jLabel3.setText("Username:");

        jLabel4.setText("Password:");

        sqlLabel.setText("SQL:");

        databaseDriverCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sun JDBC-ODBC Bridge", "ODBC - MySQL", "ODBC - PostgresSQL", "ODBC - SQL Server/Sybase", "ODBC - Oracle 10g Release 2" }));
        databaseDriverCombobox.setToolTipText("Specifies the type of database driver to use to connect to the database.");

        databaseURLField.setToolTipText("<html>The JDBC URL to connect to the database. This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");

        databaseUsernameField.setToolTipText("<html>The user name to connect to the database. This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");

        databasePasswordField.setToolTipText("<html>The password to connect to the database. This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");

        onUpdateLabel.setText("On-Update SQL:");

        pollingFrequencyLabel.setText("Polling Frequency (ms):");

        pollingFrequency.setToolTipText("<html>If the \"Interval\" Polling Type is selected, the number of milliseconds between polls must be entered here.<br>Avoid extremely small values because polling can be a somewhat time-consuming operation.</html>");

        readOnUpdateYes.setBackground(new java.awt.Color(255, 255, 255));
        readOnUpdateYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(readOnUpdateYes);
        readOnUpdateYes.setText("Yes");
        readOnUpdateYes.setToolTipText("<html>When using a database reader, it is usually necessary to execute a separate SQL statement<br>to mark the message that was just fetched as processed, so it will not be fetched again the next time a poll occurs.<br>Selecting \"Yes\" for Run On-Update Statement turns this behavior on.<br>Selecting \"No\" turns this behavior off.</html>");
        readOnUpdateYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        readOnUpdateYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readOnUpdateYesActionPerformed(evt);
            }
        });

        readOnUpdateNo.setBackground(new java.awt.Color(255, 255, 255));
        readOnUpdateNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(readOnUpdateNo);
        readOnUpdateNo.setSelected(true);
        readOnUpdateNo.setText("No");
        readOnUpdateNo.setToolTipText("<html>When using a database reader, it is usually necessary to execute a separate SQL statement<br>to mark the message that was just fetched as processed, so it will not be fetched again the next time a poll occurs.<br>Selecting \"Yes\" for Run On-Update Statement turns this behavior on.<br>Selecting \"No\" turns this behavior off.</html>");
        readOnUpdateNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        readOnUpdateNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readOnUpdateNoActionPerformed(evt);
            }
        });

        jLabel8.setText("Run On-Update Statment:");

        dbVarList.setToolTipText("<html>This list is populated with mappings based on the select statement in the JavaScript or SQL editor.<br>These mappings can dragged into the On-Update JavaScript or SQL editors.</html>");
        jScrollPane1.setViewportView(dbVarList);

        databaseSQLTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        databaseUpdateSQLTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel6.setText("Use JavaScript");

        useJavaScriptYes.setBackground(new java.awt.Color(255, 255, 255));
        useJavaScriptYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(useJavaScriptYes);
        useJavaScriptYes.setText("Yes");
        useJavaScriptYes.setToolTipText("Implement JavaScript code using JDBC to get the messages to be processed and mark messages in the database as processed.");
        useJavaScriptYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useJavaScriptYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useJavaScriptYesActionPerformed(evt);
            }
        });

        useJavaScriptNo.setBackground(new java.awt.Color(255, 255, 255));
        useJavaScriptNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(useJavaScriptNo);
        useJavaScriptNo.setText("No");
        useJavaScriptNo.setToolTipText("Specify the SQL statements to get messages to be processed and mark messages in the database as processed.");
        useJavaScriptNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useJavaScriptNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useJavaScriptNoActionPerformed(evt);
            }
        });

        generateConnection.setText("Connection");
        generateConnection.setToolTipText("<html>If \"Yes\" is selected for Use JavaScript, this button is enabled.<br>When clicked, it inserts boilerplate Connection construction code into the JavaScript control at the current caret location.</html>");
        generateConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateConnectionActionPerformed(evt);
            }
        });

        generateUpdateConnection.setText("Connection");
        generateUpdateConnection.setToolTipText("<html>If \"Yes\" is selected for Use JavaScript and \"Yes\" is selected for Run On-Update Statement, this button is enabled.<br>When clicked, it inserts boilerplate Connection construction code into the On-Update JavaScript control at the current caret location.</html>");
        generateUpdateConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateUpdateConnectionActionPerformed(evt);
            }
        });

        pollingTimeButton.setBackground(new java.awt.Color(255, 255, 255));
        pollingTimeButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(pollingTimeButton);
        pollingTimeButton.setText("Time");
        pollingTimeButton.setToolTipText("The connector will poll once a day at the time specified in the Polling Time control.");
        pollingTimeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pollingTimeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pollingTimeButtonActionPerformed(evt);
            }
        });

        pollingIntervalButton.setBackground(new java.awt.Color(255, 255, 255));
        pollingIntervalButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(pollingIntervalButton);
        pollingIntervalButton.setText("Interval");
        pollingIntervalButton.setToolTipText("The connector will poll every n milliseconds, where n is specified in the Polling Frequency control.");
        pollingIntervalButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pollingIntervalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pollingIntervalButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("Polling Type:");

        pollingTimeLabel.setText("Polling Time (daily):");

        pollingTime.setToolTipText("If the \"Time\" Polling Type is selected, the time of day to poll must be entered here.");

        generateSelect.setText("Select");
        generateSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateSelectActionPerformed(evt);
            }
        });

        jLabel7.setText("Generate:");

        generateUpdateSelect.setText("Update");
        generateUpdateSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateUpdateSelectActionPerformed(evt);
            }
        });

        jLabel9.setText("Generate:");

        processResultsInOrderNoButton.setBackground(new java.awt.Color(255, 255, 255));
        processResultsInOrderNoButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(processResultsInOrderNoButton);
        processResultsInOrderNoButton.setText("No");
        processResultsInOrderNoButton.setToolTipText("Specify the SQL statements to get messages to be processed and mark messages in the database as processed.");
        processResultsInOrderNoButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        processResultsInOrderYesButton.setBackground(new java.awt.Color(255, 255, 255));
        processResultsInOrderYesButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(processResultsInOrderYesButton);
        processResultsInOrderYesButton.setText("Yes");
        processResultsInOrderYesButton.setToolTipText("Implement JavaScript code using JDBC to get the messages to be processed and mark messages in the database as processed.");
        processResultsInOrderYesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel10.setText("Process Results in Order:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(sqlLabel)
                    .add(jLabel1)
                    .add(jLabel2)
                    .add(jLabel3)
                    .add(jLabel4)
                    .add(pollingFrequencyLabel)
                    .add(jLabel8)
                    .add(onUpdateLabel)
                    .add(jLabel6)
                    .add(pollingTimeLabel)
                    .add(jLabel5)
                    .add(jLabel10))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(pollingIntervalButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pollingTimeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(272, 272, 272))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(databaseUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(databaseURLField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(databaseDriverCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                    .add(readOnUpdateYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(readOnUpdateNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 81, Short.MAX_VALUE)
                                    .add(jLabel9)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(generateUpdateConnection)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(generateUpdateSelect))
                                .add(databaseSQLTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
                                .add(layout.createSequentialGroup()
                                    .add(databaseUpdateSQLTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 95, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .addContainerGap())
                        .add(databasePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(layout.createSequentialGroup()
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(layout.createSequentialGroup()
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(pollingTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                            .add(layout.createSequentialGroup()
                                                .add(processResultsInOrderYesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(processResultsInOrderNoButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .add(75, 75, 75))
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                                .add(useJavaScriptYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(useJavaScriptNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 6, Short.MAX_VALUE)
                                    .add(jLabel7)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(generateConnection)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(generateSelect))
                                .add(pollingFrequency, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .addContainerGap()))))
        );

        layout.linkSize(new java.awt.Component[] {generateUpdateConnection, generateUpdateSelect}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {generateConnection, generateSelect}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(databaseDriverCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(databaseURLField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel2))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(databaseUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(databasePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel4))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel5)
                            .add(pollingIntervalButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(pollingTimeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(pollingFrequency, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(pollingFrequencyLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(pollingTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(pollingTimeLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel10)
                            .add(processResultsInOrderYesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(processResultsInOrderNoButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel6)
                            .add(useJavaScriptYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(useJavaScriptNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(generateSelect)
                        .add(generateConnection)
                        .add(jLabel7)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sqlLabel)
                    .add(databaseSQLTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(readOnUpdateYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(readOnUpdateNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(generateUpdateConnection)
                    .add(generateUpdateSelect)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(databaseUpdateSQLTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                    .add(onUpdateLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void pollingIntervalButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_pollingIntervalButtonActionPerformed
    {//GEN-HEADEREND:event_pollingIntervalButtonActionPerformed
        pollingFrequencyLabel.setEnabled(true);
        pollingTimeLabel.setEnabled(false);
        pollingFrequency.setEnabled(true);
        pollingTime.setEnabled(false);
    }//GEN-LAST:event_pollingIntervalButtonActionPerformed
    
    private void pollingTimeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_pollingTimeButtonActionPerformed
    {//GEN-HEADEREND:event_pollingTimeButtonActionPerformed
        pollingFrequencyLabel.setEnabled(false);
        pollingTimeLabel.setEnabled(true);
        pollingFrequency.setEnabled(false);
        pollingTime.setEnabled(true);
    }//GEN-LAST:event_pollingTimeButtonActionPerformed
    
    private void generateUpdateConnectionActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_generateUpdateConnectionActionPerformed
    {//GEN-HEADEREND:event_generateUpdateConnectionActionPerformed
    	String connString = "// This update script will be executed once for ever result returned from the above query.\n";
    	connString += generateConnectionString();
    	databaseUpdateSQLTextPane.setText(connString +"\n\n" + databaseUpdateSQLTextPane.getText());
        databaseUpdateSQLTextPane.requestFocus();
        databaseUpdateSQLTextPane.setCaretPosition(databaseUpdateSQLTextPane.getText().indexOf("\n\n") + 1);
        parent.enableSave();
}//GEN-LAST:event_generateUpdateConnectionActionPerformed

private void generateSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateSelectActionPerformed
	showDatabaseMetaData(STATEMENT_TYPE.SELECT_TYPE);
}//GEN-LAST:event_generateSelectActionPerformed

private void generateUpdateSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateUpdateSelectActionPerformed
	showDatabaseMetaData(STATEMENT_TYPE.UPDATE_TYPE);
}//GEN-LAST:event_generateUpdateSelectActionPerformed
    
	public void showDatabaseMetaData(STATEMENT_TYPE type) { 
		Properties connectionProperties = getProperties();
	    if (((String) connectionProperties.get(DatabaseWriterProperties.DATABASE_URL)).length() == 0 || (((String) connectionProperties.get(DatabaseWriterProperties.DATABASE_DRIVER)).equals("Please Select One"))) { 
	        parent.alertError(parent, "A valid Driver and URL are required to perform this operation.");
	    } else { 
	        new DatabaseMetadataDialog(this, type, connectionProperties);
	    }
	}

	public void setSelectText(String statement) { 
		if(!useJavaScriptYes.isSelected()) { 
			databaseSQLTextPane.setText(statement + ";\n\n" + databaseSQLTextPane.getText());
		} else { 
			StringBuilder connectionString = new StringBuilder();
        	connectionString.append("var result = dbConn.executeCachedQuery(\"");
        	connectionString.append(statement.replaceAll("\\n", " "));
            connectionString.append("\");\n");
            databaseSQLTextPane.setSelectedText("\n" + connectionString.toString());
        }
		parent.enableSave();
    }   
    
    public void setUpdateText(List<String> statements) { 
        if(!useJavaScriptYes.isSelected()) { 
	    	for(String statement : statements) {
	        	databaseUpdateSQLTextPane.setText(statement.replaceAll("\\?", "") + ";\n\n" + databaseUpdateSQLTextPane.getText());
	        }
        } else { 
        	StringBuilder connectionString = new StringBuilder();
        	for(String statement : statements) {
        		connectionString.append("var result = dbConn.executeUpdate(\"");
        		connectionString.append(statement.replaceAll("\\n", " "));
                connectionString.append("\");\n");
	        }
        	databaseUpdateSQLTextPane.setSelectedText("\n" + connectionString.toString());
        }
        
        parent.enableSave();
    }   
        
    private void generateConnectionActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_generateConnectionActionPerformed
    {// GEN-HEADEREND:event_generateConnectionActionPerformed
        String connString = generateConnectionString();
        connString += "\n// You may access this result below with $('column_name')\nreturn result;";
    	databaseSQLTextPane.setText(connString +"\n\n" + databaseSQLTextPane.getText());
    	databaseSQLTextPane.requestFocus();
    	databaseSQLTextPane.setCaretPosition(databaseSQLTextPane.getText().indexOf("\n\n") + 1);
    	parent.enableSave();
    }// GEN-LAST:event_generateConnectionActionPerformed
    
    private String generateConnectionString()
    {
        String driver = "";
        
        for (int i = 0; i < drivers.size(); i++)
        {
            DriverInfo driverInfo = drivers.get(i);
            if (driverInfo.getName().equalsIgnoreCase(((String) databaseDriverCombobox.getSelectedItem())))
                driver = driverInfo.getClassName();
        }
        
        StringBuilder connectionString = new StringBuilder();
        connectionString.append("var dbConn = DatabaseConnectionFactory.createDatabaseConnection('");
        connectionString.append(driver + "','" + databaseURLField.getText() + "','");
        connectionString.append(databaseUsernameField.getText() + "','" +  new String(databasePasswordField.getPassword()) + "\');\n");
        connectionString.append("\ndbConn.close();");
        
        return connectionString.toString();
    }
        
    private void useJavaScriptNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_useJavaScriptNoActionPerformed
    {// GEN-HEADEREND:event_useJavaScriptNoActionPerformed
        sqlLabel.setText("SQL:");
        onUpdateLabel.setText("On-Update SQL:");
        databaseSQLTextPane.setDocument(sqlMappingDoc);
        databaseUpdateSQLTextPane.setDocument(sqlUpdateMappingDoc);
        databaseSQLTextPane.setText("");
        databaseUpdateSQLTextPane.setText("");
        generateConnection.setEnabled(false);
        updateSQL();
        
        generateUpdateConnection.setEnabled(false);
        dbVarList.setPrefixAndSuffix("${", "}");
    }// GEN-LAST:event_useJavaScriptNoActionPerformed
    
    private void useJavaScriptYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_useJavaScriptYesActionPerformed
    {// GEN-HEADEREND:event_useJavaScriptYesActionPerformed
        sqlLabel.setText("JavaScript:");
        onUpdateLabel.setText("On-Update JavaScript:");
        databaseSQLTextPane.setDocument(jsMappingDoc);
        databaseUpdateSQLTextPane.setDocument(jsUpdateMappingDoc);
        String connString = generateConnectionString();
        String query = connString + "\n// You may access this result below with $('column_name')\nreturn result;";
        databaseSQLTextPane.setText(query);
        String update = "// This update script will be executed once for ever result returned from the above query.\n" + connString;
    	databaseUpdateSQLTextPane.setText(update);
        generateConnection.setEnabled(true);
        updateSQL();
        
        if (readOnUpdateYes.isSelected())
            generateUpdateConnection.setEnabled(true);
        dbVarList.setPrefixAndSuffix("$('", "')");
    }// GEN-LAST:event_useJavaScriptYesActionPerformed
    
    private void readOnUpdateNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_readOnUpdateNoActionPerformed
    {// GEN-HEADEREND:event_readOnUpdateNoActionPerformed
        onUpdateLabel.setEnabled(false);
        databaseUpdateSQLTextPane.setEnabled(false);
        generateUpdateConnection.setEnabled(false);
    }// GEN-LAST:event_readOnUpdateNoActionPerformed
    
    private void readOnUpdateYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_readOnUpdateYesActionPerformed
    {// GEN-HEADEREND:event_readOnUpdateYesActionPerformed
        onUpdateLabel.setEnabled(true);
        databaseUpdateSQLTextPane.setEnabled(true);
        
        if (useJavaScriptYes.isSelected())
            generateUpdateConnection.setEnabled(true);
    }// GEN-LAST:event_readOnUpdateYesActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private com.webreach.mirth.client.ui.components.MirthComboBox databaseDriverCombobox;
    private com.webreach.mirth.client.ui.components.MirthPasswordField databasePasswordField;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea databaseSQLTextPane;
    private com.webreach.mirth.client.ui.components.MirthTextField databaseURLField;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea databaseUpdateSQLTextPane;
    private com.webreach.mirth.client.ui.components.MirthTextField databaseUsernameField;
    private com.webreach.mirth.client.ui.components.MirthVariableList dbVarList;
    private javax.swing.JButton generateConnection;
    private javax.swing.JButton generateSelect;
    private javax.swing.JButton generateUpdateConnection;
    private javax.swing.JButton generateUpdateSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel onUpdateLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField pollingFrequency;
    private javax.swing.JLabel pollingFrequencyLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton pollingIntervalButton;
    private com.webreach.mirth.client.ui.components.MirthTimePicker pollingTime;
    private com.webreach.mirth.client.ui.components.MirthRadioButton pollingTimeButton;
    private javax.swing.JLabel pollingTimeLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton processResultsInOrderNoButton;
    private com.webreach.mirth.client.ui.components.MirthRadioButton processResultsInOrderYesButton;
    private com.webreach.mirth.client.ui.components.MirthRadioButton readOnUpdateNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton readOnUpdateYes;
    private javax.swing.JLabel sqlLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton useJavaScriptNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton useJavaScriptYes;
    // End of variables declaration//GEN-END:variables
    
}
