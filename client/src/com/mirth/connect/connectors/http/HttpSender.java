/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.mirth.connect.connectors.http;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.connectors.ConnectorClass;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.QueuedSenderProperties;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.util.ConnectionTestResponse;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class HttpSender extends ConnectorClass {

    private final int NAME_COLUMN = 0;
    private final int VALUE_COLUMN = 1;
    private final String NAME_COLUMN_NAME = "Name";
    private final String VALUE_COLUMN_NAME = "Value";
    private int propertiesLastIndex = -1;
    private int headerLastIndex = -1;
    private HashMap channelList;

    public HttpSender() {
        name = HttpSenderProperties.name;
        initComponents();

        reconnectIntervalField.setDocument(new MirthFieldConstraints(0, false, false, true));
        queuePollIntervalField.setDocument(new MirthFieldConstraints(0, false, false, true));

        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);

        queryParametersPane.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deselectRows(queryParametersTable, queryParametersDeleteButton);
            }
        });
        headersPane.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deselectRows(headersTable, headersDeleteButton);
            }
        });
        queryParametersDeleteButton.setEnabled(false);
        headersDeleteButton.setEnabled(false);
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(HttpSenderProperties.DATATYPE, name);
        properties.put(HttpSenderProperties.HTTP_URL, urlField.getText());

        if (postButton.isSelected()) {
            properties.put(HttpSenderProperties.HTTP_METHOD, "post");
        } else if (getButton.isSelected()) {
            properties.put(HttpSenderProperties.HTTP_METHOD, "get");
        } else if (putButton.isSelected()) {
            properties.put(HttpSenderProperties.HTTP_METHOD, "put");
        } else if (deleteButton.isSelected()) {
            properties.put(HttpSenderProperties.HTTP_METHOD, "delete");
        }

        if (multipartYesButton.isSelected()) {
            properties.put(HttpSenderProperties.HTTP_MULTIPART, UIConstants.YES_OPTION);
        } else {
            properties.put(HttpSenderProperties.HTTP_MULTIPART, UIConstants.NO_OPTION);
        }

        properties.put(HttpSenderProperties.HTTP_SOCKET_TIMEOUT, sendTimeoutField.getText());

        if (authenticationYesRadio.isSelected()) {
            properties.put(HttpSenderProperties.HTTP_USE_AUTHENTICATION, UIConstants.YES_OPTION);
        } else {
            properties.put(HttpSenderProperties.HTTP_USE_AUTHENTICATION, UIConstants.NO_OPTION);
        }

        if (authenticationTypeBasicRadio.isSelected()) {
            properties.put(HttpSenderProperties.HTTP_AUTHENTICATION_TYPE, "Basic");
        } else {
            properties.put(HttpSenderProperties.HTTP_AUTHENTICATION_TYPE, "Digest");
        }
        
        properties.put(HttpSenderProperties.HTTP_USERNAME, usernameField.getText());
        properties.put(HttpSenderProperties.HTTP_PASSWORD, new String(passwordField.getPassword()));

        if (includeResponseHeadersYesButton.isSelected()) {
            properties.put(HttpSenderProperties.HTTP_INCLUDE_HEADERS_IN_RESPONSE, UIConstants.YES_OPTION);
        } else {
            properties.put(HttpSenderProperties.HTTP_INCLUDE_HEADERS_IN_RESPONSE, UIConstants.NO_OPTION);
        }

        properties.put(HttpSenderProperties.HTTP_REPLY_CHANNEL_ID, channelList.get((String) channelNames.getSelectedItem()));

        properties.put(QueuedSenderProperties.QUEUE_POLL_INTERVAL, queuePollIntervalField.getText());
        properties.put(QueuedSenderProperties.RECONNECT_INTERVAL, reconnectIntervalField.getText());

        if (usePersistentQueuesYesRadio.isSelected()) {
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.YES_OPTION);
        } else {
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.NO_OPTION);
        }

        if (rotateMessagesCheckBox.isSelected()) {
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.YES_OPTION);
        } else {
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.NO_OPTION);
        }

        properties.put(HttpSenderProperties.HTTP_CHARSET, parent.getSelectedEncodingForConnector(charsetEncodingCombobox));

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(HttpSenderProperties.HTTP_PARAMETERS, serializer.toXML(getAdditionalProperties()));
        properties.put(HttpSenderProperties.HTTP_HEADERS, serializer.toXML(getHeaderProperties()));

        properties.put(HttpSenderProperties.HTTP_CONTENT_TYPE, contentTypeField.getText());
        properties.put(HttpSenderProperties.HTTP_CONTENT, contentTextArea.getText());

        return properties;
    }

    public void setProperties(Properties props) {
        resetInvalidProperties();

        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

        urlField.setText((String) props.get(HttpSenderProperties.HTTP_URL));

        if (((String) props.get(HttpSenderProperties.HTTP_METHOD)).equalsIgnoreCase("post")) {
            postButton.setSelected(true);
            postButtonActionPerformed(null);
        } else if (((String) props.get(HttpSenderProperties.HTTP_METHOD)).equalsIgnoreCase("get")) {
            getButton.setSelected(true);
            getButtonActionPerformed(null);
        } else if (((String) props.get(HttpSenderProperties.HTTP_METHOD)).equalsIgnoreCase("put")) {
            putButton.setSelected(true);
            putButtonActionPerformed(null);
        } else if (((String) props.get(HttpSenderProperties.HTTP_METHOD)).equalsIgnoreCase("delete")) {
            deleteButton.setSelected(true);
            deleteButtonActionPerformed(null);
        }

        if (((String) props.get(HttpSenderProperties.HTTP_MULTIPART)).equals(UIConstants.YES_OPTION)) {
            multipartYesButton.setSelected(true);
        } else {
            multipartNoButton.setSelected(true);
        }

        checkMultipartEnabled();

        sendTimeoutField.setText(props.getProperty(HttpSenderProperties.HTTP_SOCKET_TIMEOUT));

        if (((String) props.get(HttpSenderProperties.HTTP_USE_AUTHENTICATION)).equals(UIConstants.YES_OPTION)) {
            authenticationYesRadio.setSelected(true);
            authenticationYesRadioActionPerformed(null);
        } else {
            authenticationNoRadio.setSelected(true);
            authenticationNoRadioActionPerformed(null);
        }

        if (((String) props.get(HttpSenderProperties.HTTP_AUTHENTICATION_TYPE)).equalsIgnoreCase("Basic")) {
            authenticationTypeBasicRadio.setSelected(true);
        } else if (((String) props.get(HttpSenderProperties.HTTP_AUTHENTICATION_TYPE)).equalsIgnoreCase("Digest")) {
            authenticationTypeDigestRadio.setSelected(true);
        }

        usernameField.setText(props.getProperty(HttpSenderProperties.HTTP_USERNAME));
        passwordField.setText(props.getProperty(HttpSenderProperties.HTTP_PASSWORD));

        if (((String) props.get(HttpSenderProperties.HTTP_INCLUDE_HEADERS_IN_RESPONSE)).equals(UIConstants.YES_OPTION)) {
            includeResponseHeadersYesButton.setSelected(true);
        } else {
            includeResponseHeadersNoButton.setSelected(true);
        }

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();

        if (((String) props.get(HttpSenderProperties.HTTP_PARAMETERS)).length() > 0) {
            setAdditionalProperties((Properties) serializer.fromXML((String) props.get(HttpSenderProperties.HTTP_PARAMETERS)));
        } else {
            setAdditionalProperties(new Properties());
        }

        if (((String) props.get(HttpSenderProperties.HTTP_HEADERS)).length() > 0) {
            setHeaderProperties((Properties) serializer.fromXML((String) props.get(HttpSenderProperties.HTTP_HEADERS)));
        } else {
            setHeaderProperties(new Properties());
        }

        contentTypeField.setText((String) props.get(HttpSenderProperties.HTTP_CONTENT_TYPE));

        contentTextArea.setText((String) props.get(HttpSenderProperties.HTTP_CONTENT));

        queuePollIntervalField.setText((String) props.get(QueuedSenderProperties.QUEUE_POLL_INTERVAL));
        reconnectIntervalField.setText((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL));

        if (((String) props.get(QueuedSenderProperties.USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION)) {
            usePersistentQueuesYesRadio.setSelected(true);
            usePersistentQueuesYesRadioActionPerformed(null);
        } else {
            usePersistentQueuesNoRadio.setSelected(true);
            usePersistentQueuesNoRadioActionPerformed(null);
        }

        if (((String) props.get(QueuedSenderProperties.ROTATE_QUEUE)).equals(UIConstants.YES_OPTION)) {
            rotateMessagesCheckBox.setSelected(true);
        } else {
            rotateMessagesCheckBox.setSelected(false);
        }

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, (String) props.get(HttpSenderProperties.HTTP_CHARSET));

        ArrayList<String> channelNameArray = new ArrayList<String>();
        channelList = new HashMap();
        channelList.put("None", "sink");
        channelNameArray.add("None");

        String selectedChannelName = "None";

        for (Channel channel : parent.channels.values()) {
            if (((String) props.get(HttpSenderProperties.HTTP_REPLY_CHANNEL_ID)).equalsIgnoreCase(channel.getId())) {
                selectedChannelName = channel.getName();
            }

            channelList.put(channel.getName(), channel.getId());
            channelNameArray.add(channel.getName());
        }
        channelNames.setModel(new javax.swing.DefaultComboBoxModel(channelNameArray.toArray()));

        channelNames.setSelectedItem(selectedChannelName);

        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    public Properties getDefaults() {
        return new HttpSenderProperties().getDefaults();
    }

    public void setAdditionalProperties(Properties properties) {
        Object[][] tableData = new Object[properties.size()][2];

        queryParametersTable = new MirthTable();

        int j = 0;
        Iterator i = properties.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            tableData[j][NAME_COLUMN] = (String) entry.getKey();
            tableData[j][VALUE_COLUMN] = (String) entry.getValue();
            j++;
        }

        queryParametersTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[]{NAME_COLUMN_NAME, VALUE_COLUMN_NAME}) {

            boolean[] canEdit = new boolean[]{true, true};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        queryParametersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow(queryParametersTable) != -1) {
                    propertiesLastIndex = getSelectedRow(queryParametersTable);
                    queryParametersDeleteButton.setEnabled(true);
                } else {
                    queryParametersDeleteButton.setEnabled(false);
                }
            }
        });

        class HTTPTableCellEditor extends AbstractCellEditor implements TableCellEditor {

            JComponent component = new JTextField();
            Object originalValue;
            boolean checkProperties;

            public HTTPTableCellEditor(boolean checkProperties) {
                super();
                this.checkProperties = checkProperties;
            }

            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                // 'value' is value contained in the cell located at (rowIndex,
                // vColIndex)
                originalValue = value;

                if (isSelected) {
                    // cell (and perhaps other cells) are selected
                }

                // Configure the component with the specified value
                ((JTextField) component).setText((String) value);

                // Return the configured component
                return component;
            }

            public Object getCellEditorValue() {
                return ((JTextField) component).getText();
            }

            public boolean stopCellEditing() {
                String s = (String) getCellEditorValue();

                if (checkProperties && (s.length() == 0 || checkUniqueProperty(s))) {
                    super.cancelCellEditing();
                } else {
                    parent.enableSave();
                }

                queryParametersDeleteButton.setEnabled(true);

                return super.stopCellEditing();
            }

            public boolean checkUniqueProperty(String property) {
                boolean exists = false;

                for (int i = 0; i < queryParametersTable.getRowCount(); i++) {
                    if (queryParametersTable.getValueAt(i, NAME_COLUMN) != null && ((String) queryParametersTable.getValueAt(i, NAME_COLUMN)).equalsIgnoreCase(property)) {
                        exists = true;
                    }
                }

                return exists;
            }

            /**
             * Enables the editor only for double-clicks.
             */
            public boolean isCellEditable(EventObject evt) {
                if (evt instanceof MouseEvent && ((MouseEvent) evt).getClickCount() >= 2) {
                    queryParametersDeleteButton.setEnabled(false);
                    return true;
                }
                return false;
            }
        }
        ;

        // Set the custom cell editor for the Destination Name column.
        queryParametersTable.getColumnModel().getColumn(queryParametersTable.getColumnModel().getColumnIndex(NAME_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(true));

        // Set the custom cell editor for the Destination Name column.
        queryParametersTable.getColumnModel().getColumn(queryParametersTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(false));

        queryParametersTable.setSelectionMode(0);
        queryParametersTable.setRowSelectionAllowed(true);
        queryParametersTable.setRowHeight(UIConstants.ROW_HEIGHT);
        queryParametersTable.setDragEnabled(false);
        queryParametersTable.setOpaque(true);
        queryParametersTable.setSortable(false);
        queryParametersTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            queryParametersTable.setHighlighters(highlighter);
        }

        queryParametersPane.setViewportView(queryParametersTable);
    }

    public void setHeaderProperties(Properties properties) {
        Object[][] tableData = new Object[properties.size()][2];

        headersTable = new MirthTable();

        int j = 0;
        Iterator i = properties.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            tableData[j][NAME_COLUMN] = (String) entry.getKey();
            tableData[j][VALUE_COLUMN] = (String) entry.getValue();
            j++;
        }

        headersTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[]{NAME_COLUMN_NAME, VALUE_COLUMN_NAME}) {

            boolean[] canEdit = new boolean[]{true, true};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        headersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow(headersTable) != -1) {
                    headerLastIndex = getSelectedRow(headersTable);
                    headersDeleteButton.setEnabled(true);
                } else {
                    headersDeleteButton.setEnabled(false);
                }
            }
        });

        class HTTPTableCellEditor extends AbstractCellEditor implements TableCellEditor {

            JComponent component = new JTextField();
            Object originalValue;
            boolean checkProperties;

            public HTTPTableCellEditor(boolean checkProperties) {
                super();
                this.checkProperties = checkProperties;
            }

            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                // 'value' is value contained in the cell located at (rowIndex,
                // vColIndex)
                originalValue = value;

                if (isSelected) {
                    // cell (and perhaps other cells) are selected
                }

                // Configure the component with the specified value
                ((JTextField) component).setText((String) value);

                // Return the configured component
                return component;
            }

            public Object getCellEditorValue() {
                return ((JTextField) component).getText();
            }

            public boolean stopCellEditing() {
                String s = (String) getCellEditorValue();

                if (checkProperties && (s.length() == 0 || checkUniqueProperty(s))) {
                    super.cancelCellEditing();
                } else {
                    parent.enableSave();
                }

                headersDeleteButton.setEnabled(true);

                return super.stopCellEditing();
            }

            public boolean checkUniqueProperty(String property) {
                boolean exists = false;

                for (int i = 0; i < headersTable.getRowCount(); i++) {
                    if (headersTable.getValueAt(i, NAME_COLUMN) != null && ((String) headersTable.getValueAt(i, NAME_COLUMN)).equalsIgnoreCase(property)) {
                        exists = true;
                    }
                }

                return exists;
            }

            /**
             * Enables the editor only for double-clicks.
             */
            public boolean isCellEditable(EventObject evt) {
                if (evt instanceof MouseEvent && ((MouseEvent) evt).getClickCount() >= 2) {
                    headersDeleteButton.setEnabled(false);
                    return true;
                }
                return false;
            }
        }
        ;

        // Set the custom cell editor for the Destination Name column.
        headersTable.getColumnModel().getColumn(headersTable.getColumnModel().getColumnIndex(NAME_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(true));

        // Set the custom cell editor for the Destination Name column.
        headersTable.getColumnModel().getColumn(headersTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(false));

        headersTable.setSelectionMode(0);
        headersTable.setRowSelectionAllowed(true);
        headersTable.setRowHeight(UIConstants.ROW_HEIGHT);
        headersTable.setDragEnabled(false);
        headersTable.setOpaque(true);
        headersTable.setSortable(false);
        headersTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            headersTable.setHighlighters(highlighter);
        }

        headersPane.setViewportView(headersTable);
    }

    public Map getAdditionalProperties() {
        Properties properties = new Properties();

        for (int i = 0; i < queryParametersTable.getRowCount(); i++) {
            if (((String) queryParametersTable.getValueAt(i, NAME_COLUMN)).length() > 0) {
                properties.put(((String) queryParametersTable.getValueAt(i, NAME_COLUMN)), ((String) queryParametersTable.getValueAt(i, VALUE_COLUMN)));
            }
        }

        return properties;
    }

    public Map getHeaderProperties() {
        Properties properties = new Properties();

        for (int i = 0; i < headersTable.getRowCount(); i++) {
            if (((String) headersTable.getValueAt(i, NAME_COLUMN)).length() > 0) {
                properties.put(((String) headersTable.getValueAt(i, NAME_COLUMN)), ((String) headersTable.getValueAt(i, VALUE_COLUMN)));
            }
        }

        return properties;
    }

    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectRows(MirthTable table, JButton button) {
        table.clearSelection();
        button.setEnabled(false);
    }

    /** Get the currently selected table index */
    public int getSelectedRow(MirthTable table) {
        if (table.isEditing()) {
            return table.getEditingRow();
        } else {
            return table.getSelectedRow();
        }
    }

    /**
     * Get the name that should be used for a new property so that it is unique.
     */
    private String getNewPropertyName(MirthTable table) {
        String temp = "Property ";

        for (int i = 1; i <= table.getRowCount() + 1; i++) {
            boolean exists = false;
            for (int j = 0; j < table.getRowCount(); j++) {
                if (((String) table.getValueAt(j, NAME_COLUMN)).equalsIgnoreCase(temp + i)) {
                    exists = true;
                }
            }
            if (!exists) {
                return temp + i;
            }
        }
        return "";
    }

    public boolean checkProperties(Properties props, boolean highlight) {
        resetInvalidProperties();
        boolean valid = true;

        if (((String) props.getProperty(HttpSenderProperties.HTTP_URL)).length() == 0) {
            valid = false;
            if (highlight) {
                urlField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (((String) props.getProperty(HttpSenderProperties.HTTP_SOCKET_TIMEOUT)).length() == 0) {
            valid = false;
            if (highlight) {
                sendTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (((String) props.get(QueuedSenderProperties.USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION)) {

            if (((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL)).length() == 0) {
                valid = false;
                if (highlight) {
                    reconnectIntervalField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (((String) props.get(QueuedSenderProperties.QUEUE_POLL_INTERVAL)).length() == 0) {
                valid = false;
                if (highlight) {
                    queuePollIntervalField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        if (((String) props.getProperty(HttpSenderProperties.HTTP_METHOD)).equalsIgnoreCase("post") || ((String) props.getProperty(HttpSenderProperties.HTTP_METHOD)).equalsIgnoreCase("put")) {
            if (((String) props.getProperty(HttpSenderProperties.HTTP_CONTENT_TYPE)).length() == 0) {
                valid = false;
                if (highlight) {
                    contentTypeField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (((String) props.getProperty(HttpSenderProperties.HTTP_CONTENT)).length() == 0) {
                valid = false;
                if (highlight) {
                    contentTextArea.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        return valid;
    }

    private void resetInvalidProperties() {
        urlField.setBackground(null);
        sendTimeoutField.setBackground(null);
        queuePollIntervalField.setBackground(null);
        reconnectIntervalField.setBackground(null);
        contentTypeField.setBackground(null);
        contentTextArea.setBackground(null);
    }

    public String doValidate(Properties props, boolean highlight) {
        String error = null;

        if (!checkProperties(props, highlight)) {
            error = "Error in the form for connector \"" + getName() + "\".\n\n";
        }

        return error;
    }

    private void checkMultipartEnabled() {
        if (postButton.isSelected()) {
            multipartLabel.setEnabled(true);
            multipartYesButton.setEnabled(true);
            multipartNoButton.setEnabled(true);
        } else {
            multipartLabel.setEnabled(false);
            multipartYesButton.setEnabled(false);
            multipartNoButton.setEnabled(false);
            multipartNoButton.setSelected(true);
        }
    }

    private void setContentEnabled(boolean enabled) {
        contentTypeLabel.setEnabled(enabled);
        contentTypeField.setEnabled(enabled);
        contentLabel.setEnabled(enabled);
        contentTextArea.setEnabled(enabled);
    }
    
    private void setQueryParametersEnabled(boolean enabled) {
        queryParametersLabel.setEnabled(enabled);
        queryParametersPane.setEnabled(enabled);
        queryParametersTable.setEnabled(enabled);
        queryParametersNewButton.setEnabled(enabled);
        
        deselectRows(queryParametersTable, queryParametersDeleteButton);
    }

    private void setAuthenticationEnabled(boolean enabled) {
        authenticationTypeLabel.setEnabled(enabled);
        authenticationTypeBasicRadio.setEnabled(enabled);
        authenticationTypeDigestRadio.setEnabled(enabled);

        usernameLabel.setEnabled(enabled);
        usernameField.setEnabled(enabled);

        if (!enabled) {
            usernameField.setText("");
        }

        passwordLabel.setEnabled(enabled);
        passwordField.setEnabled(enabled);

        if (!enabled) {
            passwordField.setText("");
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

        methodButtonGroup = new javax.swing.ButtonGroup();
        responseHeadersButtonGroup = new javax.swing.ButtonGroup();
        usePersistantQueuesButtonGroup = new javax.swing.ButtonGroup();
        multipartButtonGroup = new javax.swing.ButtonGroup();
        authenticationButtonGroup = new javax.swing.ButtonGroup();
        authenticationTypeButtonGroup = new javax.swing.ButtonGroup();
        urlLabel = new javax.swing.JLabel();
        urlField = new com.mirth.connect.client.ui.components.MirthTextField();
        queryParametersNewButton = new javax.swing.JButton();
        queryParametersDeleteButton = new javax.swing.JButton();
        queryParametersPane = new javax.swing.JScrollPane();
        queryParametersTable = new com.mirth.connect.client.ui.components.MirthTable();
        queryParametersLabel = new javax.swing.JLabel();
        methodLabel = new javax.swing.JLabel();
        postButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        getButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        channelNames = new com.mirth.connect.client.ui.components.MirthComboBox();
        URL1 = new javax.swing.JLabel();
        headersPane = new javax.swing.JScrollPane();
        headersTable = new com.mirth.connect.client.ui.components.MirthTable();
        headersLabel = new javax.swing.JLabel();
        headersNewButton = new javax.swing.JButton();
        headersDeleteButton = new javax.swing.JButton();
        responseHeadersLabel = new javax.swing.JLabel();
        includeResponseHeadersYesButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        includeResponseHeadersNoButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        rotateMessagesCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        usePersistentQueuesNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        usePersistentQueuesYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel36 = new javax.swing.JLabel();
        reconnectIntervalLabel = new javax.swing.JLabel();
        reconnectIntervalField = new com.mirth.connect.client.ui.components.MirthTextField();
        putButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        deleteButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        testConnection = new javax.swing.JButton();
        multipartLabel = new javax.swing.JLabel();
        multipartYesButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        multipartNoButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        contentTextArea = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea(true,false);
        contentLabel = new javax.swing.JLabel();
        contentTypeField = new com.mirth.connect.client.ui.components.MirthTextField();
        contentTypeLabel = new javax.swing.JLabel();
        authenticationLabel = new javax.swing.JLabel();
        authenticationYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        authenticationNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        usernameField = new com.mirth.connect.client.ui.components.MirthTextField();
        usernameLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new com.mirth.connect.client.ui.components.MirthPasswordField();
        authenticationTypeDigestRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        authenticationTypeBasicRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        authenticationTypeLabel = new javax.swing.JLabel();
        charsetEncodingLabel = new javax.swing.JLabel();
        charsetEncodingCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        sendTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        sendTimeoutLabel = new javax.swing.JLabel();
        queuePollIntervalField = new com.mirth.connect.client.ui.components.MirthTextField();
        queuePollIntervalLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        urlLabel.setText("URL:");

        urlField.setToolTipText("Enter the URL of the HTTP server to send each message to.");

        queryParametersNewButton.setText("New");
        queryParametersNewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queryParametersNewButtonActionPerformed(evt);
            }
        });

        queryParametersDeleteButton.setText("Delete");
        queryParametersDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queryParametersDeleteButtonActionPerformed(evt);
            }
        });

        queryParametersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Value"
            }
        ));
        queryParametersTable.setToolTipText("Query parameters are encoded as x=y pairs as part of the request URL, separated from it by a '?' and from each other by an '&'.");
        queryParametersPane.setViewportView(queryParametersTable);

        queryParametersLabel.setText("Query Parameters:");

        methodLabel.setText("Method:");

        postButton.setBackground(new java.awt.Color(255, 255, 255));
        postButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        methodButtonGroup.add(postButton);
        postButton.setText("POST");
        postButton.setToolTipText("Selects the HTTP operation used to send each message.");
        postButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        postButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                postButtonActionPerformed(evt);
            }
        });

        getButton.setBackground(new java.awt.Color(255, 255, 255));
        getButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        methodButtonGroup.add(getButton);
        getButton.setText("GET");
        getButton.setToolTipText("Selects the HTTP operation used to send each message.");
        getButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        getButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getButtonActionPerformed(evt);
            }
        });

        channelNames.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        channelNames.setToolTipText("<html>Selects a channel to send the response from the HTTP server as a new inbound message<br> or None to ignore the response from the HTTP server.</html>");

        URL1.setText("Send Response to:");

        headersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Value"
            }
        ));
        headersTable.setToolTipText("Header parameters are encoded as HTTP headers in the HTTP request sent to the server.");
        headersPane.setViewportView(headersTable);

        headersLabel.setText("Headers:");

        headersNewButton.setText("New");
        headersNewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headersNewButtonActionPerformed(evt);
            }
        });

        headersDeleteButton.setText("Delete");
        headersDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headersDeleteButtonActionPerformed(evt);
            }
        });

        responseHeadersLabel.setText("Include Response Headers:");

        includeResponseHeadersYesButton.setBackground(new java.awt.Color(255, 255, 255));
        includeResponseHeadersYesButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        responseHeadersButtonGroup.add(includeResponseHeadersYesButton);
        includeResponseHeadersYesButton.setText("Yes");
        includeResponseHeadersYesButton.setToolTipText("<html>If yes is selected, the HTTP headers of the response received are included as part of the response.<br>If no is selected, the HTTP headers are not included.</html>");
        includeResponseHeadersYesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        includeResponseHeadersNoButton.setBackground(new java.awt.Color(255, 255, 255));
        includeResponseHeadersNoButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        responseHeadersButtonGroup.add(includeResponseHeadersNoButton);
        includeResponseHeadersNoButton.setText("No");
        includeResponseHeadersNoButton.setToolTipText("<html>If yes is selected, the HTTP headers of the response received are included as part of the response.<br>If no is selected, the HTTP headers are not included.</html>");
        includeResponseHeadersNoButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        rotateMessagesCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        rotateMessagesCheckBox.setText("Rotate Messages in Queue");
        rotateMessagesCheckBox.setToolTipText("<html>If checked, upon unsuccessful re-try, it will rotate and put the queued message to the back of the queue<br> in order to prevent it from clogging the queue and to let the other subsequent messages in queue be processed.<br>If the order of messages processed is important, this should be unchecked.</html>");

        usePersistentQueuesNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        usePersistentQueuesNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        usePersistantQueuesButtonGroup.add(usePersistentQueuesNoRadio);
        usePersistentQueuesNoRadio.setSelected(true);
        usePersistentQueuesNoRadio.setText("No");
        usePersistentQueuesNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        usePersistentQueuesNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usePersistentQueuesNoRadioActionPerformed(evt);
            }
        });

        usePersistentQueuesYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        usePersistentQueuesYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        usePersistantQueuesButtonGroup.add(usePersistentQueuesYesRadio);
        usePersistentQueuesYesRadio.setText("Yes");
        usePersistentQueuesYesRadio.setToolTipText("<html>If checked, the connector will store any messages that are unable to be successfully processed in a file-based queue.<br>Messages will be automatically resent until the queue is manually cleared or the message is successfully sent.<br>The default queue location is (Mirth Directory)/.mule/queuestore/(ChannelID),<br> where (Mirth Directory) is the main Mirth install root and (ChannelID) is the unique id of the current channel.</html>");
        usePersistentQueuesYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        usePersistentQueuesYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usePersistentQueuesYesRadioActionPerformed(evt);
            }
        });

        jLabel36.setText("Use Persistent Queues:");

        reconnectIntervalLabel.setText("Reconnect Interval (ms):");

        reconnectIntervalField.setToolTipText("<html>The amount of time that should elapse between retry attempts to send messages in the queue.</html>");

        putButton.setBackground(new java.awt.Color(255, 255, 255));
        putButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        methodButtonGroup.add(putButton);
        putButton.setText("PUT");
        putButton.setToolTipText("Selects the HTTP operation used to send each message.");
        putButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        putButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                putButtonActionPerformed(evt);
            }
        });

        deleteButton.setBackground(new java.awt.Color(255, 255, 255));
        deleteButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        methodButtonGroup.add(deleteButton);
        deleteButton.setText("DELETE");
        deleteButton.setToolTipText("Selects the HTTP operation used to send each message.");
        deleteButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        testConnection.setText("Test Connection");
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        multipartLabel.setText("Multipart:");

        multipartYesButton.setBackground(new java.awt.Color(255, 255, 255));
        multipartYesButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        multipartButtonGroup.add(multipartYesButton);
        multipartYesButton.setText("Yes");
        multipartYesButton.setToolTipText("Set to use multipart in the Content-Type header. Multipart can only be used with POST.");
        multipartYesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        multipartNoButton.setBackground(new java.awt.Color(255, 255, 255));
        multipartNoButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        multipartButtonGroup.add(multipartNoButton);
        multipartNoButton.setText("No");
        multipartNoButton.setToolTipText("Set not to use multipart in the Content-Type header.");
        multipartNoButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        contentTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        contentTextArea.setToolTipText("The HTTP message body.");

        contentLabel.setText("Content:");

        contentTypeField.setToolTipText("The HTTP message body MIME type.");

        contentTypeLabel.setText("Content Type:");

        authenticationLabel.setText("Authentication:");

        authenticationYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        authenticationYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        authenticationButtonGroup.add(authenticationYesRadio);
        authenticationYesRadio.setText("Yes");
        authenticationYesRadio.setToolTipText("<html>Turning on authentication uses a username and password to communicate with the HTTP server.</html>");
        authenticationYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        authenticationYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                authenticationYesRadioActionPerformed(evt);
            }
        });

        authenticationNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        authenticationNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        authenticationButtonGroup.add(authenticationNoRadio);
        authenticationNoRadio.setText("No");
        authenticationNoRadio.setToolTipText("<html>Turning on authentication uses a username and password to communicate with the HTTP server.</html>");
        authenticationNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        authenticationNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                authenticationNoRadioActionPerformed(evt);
            }
        });

        usernameField.setToolTipText("The username used to connect to the HTTP server.");

        usernameLabel.setText("Username:");

        passwordLabel.setText("Password:");

        passwordField.setToolTipText("The password used to connect to the HTTP server.");

        authenticationTypeDigestRadio.setBackground(new java.awt.Color(255, 255, 255));
        authenticationTypeDigestRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        authenticationTypeButtonGroup.add(authenticationTypeDigestRadio);
        authenticationTypeDigestRadio.setText("Digest");
        authenticationTypeDigestRadio.setToolTipText("Use the digest authentication scheme.");
        authenticationTypeDigestRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        authenticationTypeBasicRadio.setBackground(new java.awt.Color(255, 255, 255));
        authenticationTypeBasicRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        authenticationTypeButtonGroup.add(authenticationTypeBasicRadio);
        authenticationTypeBasicRadio.setText("Basic");
        authenticationTypeBasicRadio.setToolTipText("Use the basic authentication scheme.");
        authenticationTypeBasicRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        authenticationTypeLabel.setText("Authentication Type:");

        charsetEncodingLabel.setText("Charset Encoding:");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));
        charsetEncodingCombobox.setToolTipText("<html>Select the character set encoding used by the sender of the message,<br> or Default to assume the default character set encoding for the JVM running Mirth.</html>");

        sendTimeoutField.setToolTipText("<html>Sets the socket timeout (SO_TIMEOUT) in milliseconds to be used when executing the method.<br>A timeout value of zero is interpreted as an infinite timeout.</html>");

        sendTimeoutLabel.setText("Send Timeout (ms):");

        queuePollIntervalField.setToolTipText("<html>The amount of time that should elapse between polls of an empty queue to check for queued messages.</html>");

        queuePollIntervalLabel.setText("Queue Poll Interval (ms):");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(multipartLabel)
                    .addComponent(methodLabel)
                    .addComponent(URL1)
                    .addComponent(reconnectIntervalLabel)
                    .addComponent(queryParametersLabel)
                    .addComponent(headersLabel)
                    .addComponent(urlLabel)
                    .addComponent(contentLabel)
                    .addComponent(contentTypeLabel)
                    .addComponent(charsetEncodingLabel)
                    .addComponent(responseHeadersLabel)
                    .addComponent(sendTimeoutLabel)
                    .addComponent(authenticationLabel)
                    .addComponent(authenticationTypeLabel)
                    .addComponent(usernameLabel)
                    .addComponent(jLabel36)
                    .addComponent(passwordLabel)
                    .addComponent(queuePollIntervalLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(queuePollIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(usePersistentQueuesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(usePersistentQueuesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rotateMessagesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap())
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(authenticationTypeBasicRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(authenticationTypeDigestRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap())
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(authenticationYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(authenticationNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addContainerGap())
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(sendTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap())
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(includeResponseHeadersYesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(includeResponseHeadersNoButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addContainerGap())
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(contentTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                                                    .addContainerGap())
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(multipartYesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(multipartNoButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addContainerGap())
                                                .addGroup(layout.createSequentialGroup()
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                            .addComponent(urlField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(testConnection))
                                                        .addGroup(layout.createSequentialGroup()
                                                            .addComponent(postButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(getButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(putButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(channelNames, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGap(14, 14, 14))
                                                .addGroup(layout.createSequentialGroup()
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addComponent(contentTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(headersPane, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE))
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                .addComponent(headersNewButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(headersDeleteButton)))
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                            .addComponent(queryParametersPane, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addComponent(queryParametersDeleteButton)
                                                                .addComponent(queryParametersNewButton)))
                                                        .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addContainerGap())))))))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap()))))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {queryParametersDeleteButton, queryParametersNewButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(urlLabel)
                    .addComponent(urlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(testConnection))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(methodLabel)
                    .addComponent(postButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(getButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(putButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(multipartLabel)
                    .addComponent(multipartYesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(multipartNoButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sendTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sendTimeoutLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(URL1)
                    .addComponent(channelNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(includeResponseHeadersYesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(includeResponseHeadersNoButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseHeadersLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(authenticationYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(authenticationNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(authenticationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(authenticationTypeBasicRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(authenticationTypeDigestRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(authenticationTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usePersistentQueuesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usePersistentQueuesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rotateMessagesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(queuePollIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(queuePollIntervalLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reconnectIntervalLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(charsetEncodingLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(queryParametersLabel)
                            .addComponent(queryParametersNewButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(queryParametersDeleteButton))
                    .addComponent(queryParametersPane, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headersLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(headersNewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(headersDeleteButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(headersPane, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(contentTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(contentTypeLabel))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contentLabel)
                    .addComponent(contentTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void headersDeleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_headersDeleteButtonActionPerformed
    {//GEN-HEADEREND:event_headersDeleteButtonActionPerformed
        if (getSelectedRow(headersTable) != -1 && !headersTable.isEditing()) {
            ((DefaultTableModel) headersTable.getModel()).removeRow(getSelectedRow(headersTable));

            if (headersTable.getRowCount() != 0) {
                if (headerLastIndex == 0) {
                    headersTable.setRowSelectionInterval(0, 0);
                } else if (headerLastIndex == headersTable.getRowCount()) {
                    headersTable.setRowSelectionInterval(headerLastIndex - 1, headerLastIndex - 1);
                } else {
                    headersTable.setRowSelectionInterval(headerLastIndex, headerLastIndex);
                }
            }

            parent.enableSave();
        }
    }//GEN-LAST:event_headersDeleteButtonActionPerformed

    private void headersNewButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_headersNewButtonActionPerformed
    {//GEN-HEADEREND:event_headersNewButtonActionPerformed
        ((DefaultTableModel) headersTable.getModel()).addRow(new Object[]{getNewPropertyName(headersTable), ""});
        headersTable.setRowSelectionInterval(headersTable.getRowCount() - 1, headersTable.getRowCount() - 1);
        parent.enableSave();
    }//GEN-LAST:event_headersNewButtonActionPerformed

private void usePersistentQueuesNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePersistentQueuesNoRadioActionPerformed
    rotateMessagesCheckBox.setEnabled(false);
    queuePollIntervalLabel.setEnabled(false);
    queuePollIntervalField.setEnabled(false);
    reconnectIntervalField.setEnabled(false);
    reconnectIntervalLabel.setEnabled(false);
}//GEN-LAST:event_usePersistentQueuesNoRadioActionPerformed

private void usePersistentQueuesYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePersistentQueuesYesRadioActionPerformed
    rotateMessagesCheckBox.setEnabled(true);
    queuePollIntervalLabel.setEnabled(true);
    queuePollIntervalField.setEnabled(true);
    reconnectIntervalField.setEnabled(true);
    reconnectIntervalLabel.setEnabled(true);
}//GEN-LAST:event_usePersistentQueuesYesRadioActionPerformed

private void postButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_postButtonActionPerformed
    setContentEnabled(true);
    checkMultipartEnabled();
    setQueryParametersEnabled(true);
}//GEN-LAST:event_postButtonActionPerformed

private void getButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getButtonActionPerformed
    setContentEnabled(false);
    checkMultipartEnabled();
    setQueryParametersEnabled(true);
}//GEN-LAST:event_getButtonActionPerformed

private void putButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_putButtonActionPerformed
    setContentEnabled(true);
    checkMultipartEnabled();
    setQueryParametersEnabled(true);
}//GEN-LAST:event_putButtonActionPerformed

private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
    setContentEnabled(false);
    checkMultipartEnabled();
    setQueryParametersEnabled(true);
}//GEN-LAST:event_deleteButtonActionPerformed

private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
    parent.setWorking("Testing connection...", true);

    SwingWorker worker = new SwingWorker<Void, Void>() {

        public Void doInBackground() {

            try {
                ConnectionTestResponse response = (ConnectionTestResponse) parent.mirthClient.invokeConnectorService(name, "testConnection", getProperties());

                if (response == null) {
                    throw new ClientException("Failed to invoke service.");
                } else if (response.getType().equals(ConnectionTestResponse.Type.SUCCESS)) {
                    parent.alertInformation(parent, response.getMessage());
                } else {
                    parent.alertWarning(parent, response.getMessage());
                }

                return null;
            } catch (ClientException e) {
                parent.alertError(parent, e.getMessage());
                return null;
            }
        }

        public void done() {
            parent.setWorking("", false);
        }
    };

    worker.execute();
}//GEN-LAST:event_testConnectionActionPerformed

private void queryParametersDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryParametersDeleteButtonActionPerformed
    if (getSelectedRow(queryParametersTable) != -1 && !queryParametersTable.isEditing()) {
        ((DefaultTableModel) queryParametersTable.getModel()).removeRow(getSelectedRow(queryParametersTable));

        if (queryParametersTable.getRowCount() != 0) {
            if (propertiesLastIndex == 0) {
                queryParametersTable.setRowSelectionInterval(0, 0);
            } else if (propertiesLastIndex == queryParametersTable.getRowCount()) {
                queryParametersTable.setRowSelectionInterval(propertiesLastIndex - 1, propertiesLastIndex - 1);
            } else {
                queryParametersTable.setRowSelectionInterval(propertiesLastIndex, propertiesLastIndex);
            }
        }

        parent.enableSave();
    }
}//GEN-LAST:event_queryParametersDeleteButtonActionPerformed

private void queryParametersNewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryParametersNewButtonActionPerformed
    ((DefaultTableModel) queryParametersTable.getModel()).addRow(new Object[]{getNewPropertyName(queryParametersTable), ""});
    queryParametersTable.setRowSelectionInterval(queryParametersTable.getRowCount() - 1, queryParametersTable.getRowCount() - 1);
    parent.enableSave();
}//GEN-LAST:event_queryParametersNewButtonActionPerformed

private void authenticationYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_authenticationYesRadioActionPerformed
    setAuthenticationEnabled(true);
}//GEN-LAST:event_authenticationYesRadioActionPerformed

private void authenticationNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_authenticationNoRadioActionPerformed
    setAuthenticationEnabled(false);
}//GEN-LAST:event_authenticationNoRadioActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL1;
    private javax.swing.ButtonGroup authenticationButtonGroup;
    private javax.swing.JLabel authenticationLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton authenticationNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton authenticationTypeBasicRadio;
    private javax.swing.ButtonGroup authenticationTypeButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton authenticationTypeDigestRadio;
    private javax.swing.JLabel authenticationTypeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton authenticationYesRadio;
    private com.mirth.connect.client.ui.components.MirthComboBox channelNames;
    private com.mirth.connect.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private javax.swing.JLabel charsetEncodingLabel;
    private javax.swing.JLabel contentLabel;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea contentTextArea;
    private com.mirth.connect.client.ui.components.MirthTextField contentTypeField;
    private javax.swing.JLabel contentTypeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton deleteButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton getButton;
    private javax.swing.JButton headersDeleteButton;
    private javax.swing.JLabel headersLabel;
    private javax.swing.JButton headersNewButton;
    private javax.swing.JScrollPane headersPane;
    private com.mirth.connect.client.ui.components.MirthTable headersTable;
    private com.mirth.connect.client.ui.components.MirthRadioButton includeResponseHeadersNoButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton includeResponseHeadersYesButton;
    private javax.swing.JLabel jLabel36;
    private javax.swing.ButtonGroup methodButtonGroup;
    private javax.swing.JLabel methodLabel;
    private javax.swing.ButtonGroup multipartButtonGroup;
    private javax.swing.JLabel multipartLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton multipartNoButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton multipartYesButton;
    private com.mirth.connect.client.ui.components.MirthPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton postButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton putButton;
    private javax.swing.JButton queryParametersDeleteButton;
    private javax.swing.JLabel queryParametersLabel;
    private javax.swing.JButton queryParametersNewButton;
    private javax.swing.JScrollPane queryParametersPane;
    private com.mirth.connect.client.ui.components.MirthTable queryParametersTable;
    private com.mirth.connect.client.ui.components.MirthTextField queuePollIntervalField;
    private javax.swing.JLabel queuePollIntervalLabel;
    private com.mirth.connect.client.ui.components.MirthTextField reconnectIntervalField;
    private javax.swing.JLabel reconnectIntervalLabel;
    private javax.swing.ButtonGroup responseHeadersButtonGroup;
    private javax.swing.JLabel responseHeadersLabel;
    private com.mirth.connect.client.ui.components.MirthCheckBox rotateMessagesCheckBox;
    private com.mirth.connect.client.ui.components.MirthTextField sendTimeoutField;
    private javax.swing.JLabel sendTimeoutLabel;
    private javax.swing.JButton testConnection;
    private com.mirth.connect.client.ui.components.MirthTextField urlField;
    private javax.swing.JLabel urlLabel;
    private javax.swing.ButtonGroup usePersistantQueuesButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton usePersistentQueuesNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton usePersistentQueuesYesRadio;
    private com.mirth.connect.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
}
