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

package com.webreach.mirth.connectors.jms;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class JMSReader extends ConnectorClass
{
    private final int PROPERTY_COLUMN = 0;

    private final int VALUE_COLUMN = 1;

    private final String PROPERTY_COLUMN_NAME = "Property";

    private final String VALUE_COLUMN_NAME = "Value";

    private int lastIndex = -1;
    
    private final String TRANSACTED = "Transacted";
    private final String AUTO_ACKNOWLEDGE = "Auto Acknowledge";
    private final String CLIENT_ACKNOWLEDGE = "Client Acknowledge";
    private final String DUPLICATES_OK = "Duplicates OK";
    
    /** Creates new form JMSReader */

    public JMSReader()
    {
        name = JMSReaderProperties.name;
        initComponents();
        specDropDown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1.1", "1.0.2b" }));
        propertiesPane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectRows();
            }
        });
        ackMode.setModel(new javax.swing.DefaultComboBoxModel(new String[] { TRANSACTED, AUTO_ACKNOWLEDGE, CLIENT_ACKNOWLEDGE, DUPLICATES_OK }));
        deleteButton.setEnabled(false);
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(JMSReaderProperties.DATATYPE, name);
        properties.put(JMSReaderProperties.JMS_SPECIFICATION, (String) specDropDown.getSelectedItem());

        if (durableNo.isSelected())
            properties.put(JMSReaderProperties.JMS_DURABLE, UIConstants.NO_OPTION);
        else
            properties.put(JMSReaderProperties.JMS_DURABLE, UIConstants.YES_OPTION);
            
        if (useJNDIYes.isSelected())
        {
            properties.put(JMSReaderProperties.JMS_USE_JNDI, UIConstants.YES_OPTION);
            properties.put(JMSReaderProperties.JMS_URL, jmsURL.getText());
            properties.put(JMSReaderProperties.JMS_INITIAL_FACTORY, jndiInitialFactory.getText());
            properties.put(JMSReaderProperties.JMS_CONNECTION_FACTORY_JNDI, connectionFactoryJndi.getText());
        }
        else
        {
            properties.put(JMSReaderProperties.JMS_USE_JNDI, "0");
            properties.put(JMSReaderProperties.JMS_CONNECTION_FACTORY_CLASS, connectionFactoryClass.getText());
        }
        
        if(((String) ackMode.getSelectedItem()).equals(TRANSACTED))
            properties.put(JMSReaderProperties.JMS_ACK_MODE, "0");
        else if(((String) ackMode.getSelectedItem()).equals(AUTO_ACKNOWLEDGE))
            properties.put(JMSReaderProperties.JMS_ACK_MODE, "1");
        else if(((String) ackMode.getSelectedItem()).equals(CLIENT_ACKNOWLEDGE))
            properties.put(JMSReaderProperties.JMS_ACK_MODE, "2");
        else if(((String) ackMode.getSelectedItem()).equals(DUPLICATES_OK))
            properties.put(JMSReaderProperties.JMS_ACK_MODE, "3");
        
        properties.put(JMSReaderProperties.JMS_CLIENT_ID, cliendId.getText());
        properties.put(JMSReaderProperties.JMS_USERNAME, username.getText());
        properties.put(JMSReaderProperties.JMS_PASSWORD, String.valueOf(password.getPassword()));
        properties.put(JMSReaderProperties.JMS_QUEUE, queue.getText());
        properties.put(JMSReaderProperties.JMS_SELECTOR, selector.getText());
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(JMSReaderProperties.JMS_ADDITIONAL_PROPERTIES, serializer.toXML(getAdditionalProperties()));

        return properties;
    }

    public void setProperties(Properties props)
    {
        resetInvalidProperties();
        
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

        specDropDown.setSelectedItem(props.get(JMSReaderProperties.JMS_SPECIFICATION));

        if (((String) props.get(JMSReaderProperties.JMS_DURABLE)).equalsIgnoreCase(UIConstants.NO_OPTION))
        {
            durableNo.setSelected(true);
            durableNoActionPerformed(null);
        }
        else
        {
            durableYes.setSelected(true);
            durableYesActionPerformed(null);
        }

        cliendId.setText((String) props.get(JMSReaderProperties.JMS_CLIENT_ID));
        username.setText((String) props.get(JMSReaderProperties.JMS_USERNAME));
        password.setText((String) props.get(JMSReaderProperties.JMS_PASSWORD));
        queue.setText((String) props.get(JMSReaderProperties.JMS_QUEUE));
        selector.setText((String) props.get(JMSReaderProperties.JMS_SELECTOR));

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();

        if (((String) props.get(JMSReaderProperties.JMS_ADDITIONAL_PROPERTIES)).length() > 0)
            setAdditionalProperties((Properties) serializer.fromXML((String) props.get(JMSReaderProperties.JMS_ADDITIONAL_PROPERTIES)));
        else
            setAdditionalProperties(new Properties());
        
        if (((String) props.get(JMSReaderProperties.JMS_USE_JNDI)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            useJNDIYes.setSelected(true);
            useJNDIYesActionPerformed(null);
        }
        else
        {
            useJNDINo.setSelected(true);
            useJNDINoActionPerformed(null);
        }
        
        jmsURL.setText((String) props.get(JMSReaderProperties.JMS_URL));
        jndiInitialFactory.setText((String) props.get(JMSReaderProperties.JMS_INITIAL_FACTORY));
        connectionFactoryJndi.setText((String) props.get(JMSReaderProperties.JMS_CONNECTION_FACTORY_JNDI));
        connectionFactoryClass.setText((String) props.get(JMSReaderProperties.JMS_CONNECTION_FACTORY_CLASS));
        
        if(props.get(JMSReaderProperties.JMS_ACK_MODE).equals("0"))
            ackMode.setSelectedItem(TRANSACTED);
        else if(props.get(JMSReaderProperties.JMS_ACK_MODE).equals("1"))
            ackMode.setSelectedItem(AUTO_ACKNOWLEDGE);
        else if(props.get(JMSReaderProperties.JMS_ACK_MODE).equals("2"))
            ackMode.setSelectedItem(CLIENT_ACKNOWLEDGE);
        else if(props.get(JMSReaderProperties.JMS_ACK_MODE).equals("3"))
            ackMode.setSelectedItem(DUPLICATES_OK);
            
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    public Properties getDefaults()
    {
        return new JMSReaderProperties().getDefaults();
    }

    public void setAdditionalProperties(Properties properties)
    {
        Object[][] tableData = new Object[properties.size()][2];

        propertiesTable = new MirthTable();

        int j = 0;
        Iterator i = properties.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry entry = (Map.Entry) i.next();
            tableData[j][PROPERTY_COLUMN] = (String) entry.getKey();
            tableData[j][VALUE_COLUMN] = (String) entry.getValue();
            j++;
        }

        propertiesTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] { PROPERTY_COLUMN_NAME, VALUE_COLUMN_NAME })
        {
            boolean[] canEdit = new boolean[] { true, true };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit[columnIndex];
            }
        });

        propertiesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                if (getSelectedRow() != -1)
                {
                    lastIndex = getSelectedRow();
                    deleteButton.setEnabled(true);
                }
                else
                    deleteButton.setEnabled(false);
            }
        });

        class JMSTableCellEditor extends AbstractCellEditor implements TableCellEditor
        {
            JComponent component = new JTextField();

            Object originalValue;

            boolean checkProperties;

            public JMSTableCellEditor(boolean checkProperties)
            {
                super();
                this.checkProperties = checkProperties;
            }

            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
            {
                // 'value' is value contained in the cell located at (rowIndex,
                // vColIndex)
                originalValue = value;

                if (isSelected)
                {
                    // cell (and perhaps other cells) are selected
                }

                // Configure the component with the specified value
                ((JTextField) component).setText((String) value);

                // Return the configured component
                return component;
            }

            public Object getCellEditorValue()
            {
                return ((JTextField) component).getText();
            }

            public boolean stopCellEditing()
            {
                String s = (String) getCellEditorValue();

                if (checkProperties && (s.length() == 0 || checkUniqueProperty(s)))
                    super.cancelCellEditing();
                else
                    parent.enableSave();

                deleteButton.setEnabled(true);

                return super.stopCellEditing();
            }

            public boolean checkUniqueProperty(String property)
            {
                boolean exists = false;

                for (int i = 0; i < propertiesTable.getRowCount(); i++)
                {
                    if (propertiesTable.getValueAt(i, PROPERTY_COLUMN) != null && ((String) propertiesTable.getValueAt(i, PROPERTY_COLUMN)).equalsIgnoreCase(property))
                        exists = true;
                }

                return exists;
            }

            /**
             * Enables the editor only for double-clicks.
             */
            public boolean isCellEditable(EventObject evt)
            {
                if (evt instanceof MouseEvent && ((MouseEvent) evt).getClickCount() >= 2)
                {
                    deleteButton.setEnabled(false);
                    return true;
                }
                return false;
            }
        };

        // Set the custom cell editor for the Destination Name column.
        propertiesTable.getColumnModel().getColumn(propertiesTable.getColumnModel().getColumnIndex(PROPERTY_COLUMN_NAME)).setCellEditor(new JMSTableCellEditor(true));

        // Set the custom cell editor for the Destination Name column.
        propertiesTable.getColumnModel().getColumn(propertiesTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new JMSTableCellEditor(false));

        propertiesTable.setSelectionMode(0);
        propertiesTable.setRowSelectionAllowed(true);
        propertiesTable.setRowHeight(UIConstants.ROW_HEIGHT);
        propertiesTable.setDragEnabled(false);
        propertiesTable.setOpaque(true);
        propertiesTable.setSortable(false);
        propertiesTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            propertiesTable.setHighlighters(highlighter);
        }

        propertiesPane.setViewportView(propertiesTable);
    }

    public Properties getAdditionalProperties()
    {
        Properties properties = new Properties();

        for (int i = 0; i < propertiesTable.getRowCount(); i++)
            if (((String) propertiesTable.getValueAt(i, PROPERTY_COLUMN)).length() > 0)
                properties.put(((String) propertiesTable.getValueAt(i, PROPERTY_COLUMN)), ((String) propertiesTable.getValueAt(i, VALUE_COLUMN)));

        return properties;
    }

    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectRows()
    {
        propertiesTable.clearSelection();
        deleteButton.setEnabled(false);
    }

    /** Get the currently selected destination index */
    public int getSelectedRow()
    {
        if (propertiesTable.isEditing())
            return propertiesTable.getEditingRow();
        else
            return propertiesTable.getSelectedRow();
    }

    /**
     * Get the name that should be used for a new property so that it is unique.
     */
    private String getNewPropertyName()
    {
        String temp = "Property ";

        for (int i = 1; i <= propertiesTable.getRowCount() + 1; i++)
        {
            boolean exists = false;
            for (int j = 0; j < propertiesTable.getRowCount(); j++)
            {
                if (((String) propertiesTable.getValueAt(j, PROPERTY_COLUMN)).equalsIgnoreCase(temp + i))
                {
                    exists = true;
                }
            }
            if (!exists)
                return temp + i;
        }
        return "";
    }

    public boolean checkProperties(Properties props, boolean highlight)
    {
        resetInvalidProperties();
        boolean valid = true;
        if (((String) props.get(JMSReaderProperties.JMS_USE_JNDI)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {    
            if (((String) props.getProperty(JMSReaderProperties.JMS_URL)).length() == 0)
            {
                valid = false;
                if (highlight)
                	jmsURL.setBackground(UIConstants.INVALID_COLOR);
            }
            if (((String) props.getProperty(JMSReaderProperties.JMS_CONNECTION_FACTORY_JNDI)).length() == 0)
            {
                valid = false;
                if (highlight)
                	connectionFactoryJndi.setBackground(UIConstants.INVALID_COLOR);
            }
            if (((String) props.getProperty(JMSReaderProperties.JMS_INITIAL_FACTORY)).length() == 0)
            {
                valid = false;
                if (highlight)
                	jndiInitialFactory.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        else
        {
            if (((String) props.getProperty(JMSReaderProperties.JMS_CONNECTION_FACTORY_CLASS)).length() == 0)
            {
                valid = false;
                if (highlight)
                	connectionFactoryClass.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        
        if (((String) props.getProperty(JMSReaderProperties.JMS_QUEUE)).length() == 0)
        {
            valid = false;
            if (highlight)
            	queue.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.getProperty(JMSReaderProperties.JMS_DURABLE)).equals(UIConstants.YES_OPTION))
        {
            if (((String) props.getProperty(JMSReaderProperties.JMS_CLIENT_ID)).length() == 0)
            {
                valid = false;
                if (highlight)
                	cliendId.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        
        return valid;
    }
    
    private void resetInvalidProperties()
    {
        jmsURL.setBackground(null);
        connectionFactoryJndi.setBackground(null);
        queue.setBackground(null);
        jndiInitialFactory.setBackground(null);
        cliendId.setBackground(null);
        connectionFactoryClass.setBackground(null);
    }
    
    public String doValidate(Properties props, boolean highlight)
    {
    	String error = null;
    	
    	if (!checkProperties(props, highlight))
    		error = "Error in the form for connector \"" + getName() + "\".\n\n";
    	
    	return error;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        durableButtonGroup = new javax.swing.ButtonGroup();
        useJndiButtonGroup = new javax.swing.ButtonGroup();
        jLabel3 = new javax.swing.JLabel();
        specDropDown = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel4 = new javax.swing.JLabel();
        clientIdLabel = new javax.swing.JLabel();
        jmsUrlLabel = new javax.swing.JLabel();
        connectionFactoryJndiLabel = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        durableNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        durableYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        cliendId = new com.webreach.mirth.client.ui.components.MirthTextField();
        jmsURL = new com.webreach.mirth.client.ui.components.MirthTextField();
        connectionFactoryJndi = new com.webreach.mirth.client.ui.components.MirthTextField();
        queue = new com.webreach.mirth.client.ui.components.MirthTextField();
        jndiInitialFactory = new com.webreach.mirth.client.ui.components.MirthTextField();
        jndiInitialFactoryLabel = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        username = new com.webreach.mirth.client.ui.components.MirthTextField();
        password = new com.webreach.mirth.client.ui.components.MirthPasswordField();
        jLabel12 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        selector = new com.webreach.mirth.client.ui.components.MirthTextField();
        propertiesPane = new javax.swing.JScrollPane();
        propertiesTable = new com.webreach.mirth.client.ui.components.MirthTable();
        jLabel2 = new javax.swing.JLabel();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        useJNDIYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        useJNDINo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        connectionFactoryClassLabel = new javax.swing.JLabel();
        connectionFactoryClass = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel13 = new javax.swing.JLabel();
        ackMode = new com.webreach.mirth.client.ui.components.MirthComboBox();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel3.setText("Specification:");

        specDropDown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel4.setText("Durable:");

        clientIdLabel.setText("Client ID:");

        jmsUrlLabel.setText("Provider URL:");

        connectionFactoryJndiLabel.setText("Connection Factory Name:");

        jLabel9.setText("Destination:");

        durableNo.setBackground(new java.awt.Color(255, 255, 255));
        durableNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        durableButtonGroup.add(durableNo);
        durableNo.setSelected(true);
        durableNo.setText("No");
        durableNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        durableNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                durableNoActionPerformed(evt);
            }
        });

        durableYes.setBackground(new java.awt.Color(255, 255, 255));
        durableYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        durableButtonGroup.add(durableYes);
        durableYes.setText("Yes");
        durableYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        durableYes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                durableYesActionPerformed(evt);
            }
        });

        queue.setAutoscrolls(false);

        jndiInitialFactoryLabel.setText("Initial Context Factory:");

        jLabel11.setText("Username:");

        password.setFont(new java.awt.Font("Tahoma", 0, 11));

        jLabel12.setText("Password:");

        jLabel1.setText("Selector Expression:");

        propertiesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "Property", "Value"
            }
        ));
        propertiesPane.setViewportView(propertiesTable);

        jLabel2.setText("Additional Properties:");

        newButton.setText("New");
        newButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                newButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("Use JNDI:");

        useJNDIYes.setBackground(new java.awt.Color(255, 255, 255));
        useJNDIYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useJndiButtonGroup.add(useJNDIYes);
        useJNDIYes.setText("Yes");
        useJNDIYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useJNDIYes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                useJNDIYesActionPerformed(evt);
            }
        });

        useJNDINo.setBackground(new java.awt.Color(255, 255, 255));
        useJNDINo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useJndiButtonGroup.add(useJNDINo);
        useJNDINo.setSelected(true);
        useJNDINo.setText("No");
        useJNDINo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useJNDINo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                useJNDINoActionPerformed(evt);
            }
        });

        connectionFactoryClassLabel.setText("Connection Factory Class:");

        connectionFactoryClass.setAutoscrolls(false);

        jLabel13.setText("ACK Mode:");

        ackMode.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel13)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, clientIdLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel4)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel12)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel11)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel9)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, connectionFactoryClassLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, connectionFactoryJndiLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jndiInitialFactoryLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jmsUrlLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel5)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(useJNDIYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(useJNDINo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(specDropDown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jmsURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jndiInitialFactory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(connectionFactoryJndi, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(queue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(username, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(password, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(durableYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(durableNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(propertiesPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(newButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(deleteButton)))
                    .add(connectionFactoryClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, ackMode, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, selector, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, cliendId, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(specDropDown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(useJNDIYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(useJNDINo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jmsURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jmsUrlLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jndiInitialFactory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jndiInitialFactoryLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(connectionFactoryJndiLabel)
                    .add(connectionFactoryJndi, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(connectionFactoryClassLabel)
                    .add(connectionFactoryClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(queue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(username, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel12)
                    .add(password, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(durableYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(durableNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cliendId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(clientIdLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(selector, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel13)
                    .add(ackMode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(newButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deleteButton))
                    .add(jLabel2)
                    .add(propertiesPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void useJNDINoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_useJNDINoActionPerformed
    {//GEN-HEADEREND:event_useJNDINoActionPerformed
        jmsURL.setEnabled(false);
        jmsUrlLabel.setEnabled(false);
        jndiInitialFactory.setEnabled(false);
        jndiInitialFactoryLabel.setEnabled(false);
        connectionFactoryJndi.setEnabled(false);
        connectionFactoryJndiLabel.setEnabled(false);
        connectionFactoryClass.setEnabled(true);
        connectionFactoryClassLabel.setEnabled(true);
        
    }//GEN-LAST:event_useJNDINoActionPerformed

    private void useJNDIYesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_useJNDIYesActionPerformed
    {//GEN-HEADEREND:event_useJNDIYesActionPerformed
        jmsURL.setEnabled(true);
        jmsUrlLabel.setEnabled(true);
        jndiInitialFactory.setEnabled(true);
        jndiInitialFactoryLabel.setEnabled(true);
        connectionFactoryJndi.setEnabled(true);
        connectionFactoryJndiLabel.setEnabled(true);
        connectionFactoryClass.setEnabled(false);
        connectionFactoryClassLabel.setEnabled(false);
    }//GEN-LAST:event_useJNDIYesActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_deleteButtonActionPerformed
    {// GEN-HEADEREND:event_deleteButtonActionPerformed
        if (getSelectedRow() != -1 && !propertiesTable.isEditing())
        {
            ((DefaultTableModel) propertiesTable.getModel()).removeRow(getSelectedRow());

            if (propertiesTable.getRowCount() != 0)
            {
                if (lastIndex == 0)
                    propertiesTable.setRowSelectionInterval(0, 0);
                else if (lastIndex == propertiesTable.getRowCount())
                    propertiesTable.setRowSelectionInterval(lastIndex - 1, lastIndex - 1);
                else
                    propertiesTable.setRowSelectionInterval(lastIndex, lastIndex);
            }

            parent.enableSave();
        }
    }// GEN-LAST:event_deleteButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_newButtonActionPerformed
    {// GEN-HEADEREND:event_newButtonActionPerformed
        ((DefaultTableModel) propertiesTable.getModel()).addRow(new Object[] { getNewPropertyName(), "" });
        propertiesTable.setRowSelectionInterval(propertiesTable.getRowCount() - 1, propertiesTable.getRowCount() - 1);
        parent.enableSave();
    }// GEN-LAST:event_newButtonActionPerformed

    private void durableYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_durableYesActionPerformed
    {// GEN-HEADEREND:event_durableYesActionPerformed
        cliendId.setEnabled(true);
        clientIdLabel.setEnabled(true);
    }// GEN-LAST:event_durableYesActionPerformed

    private void durableNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_durableNoActionPerformed
    {// GEN-HEADEREND:event_durableNoActionPerformed
        cliendId.setEnabled(false);
        clientIdLabel.setEnabled(false);
        cliendId.setText("");
    }// GEN-LAST:event_durableNoActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.components.MirthComboBox ackMode;
    private com.webreach.mirth.client.ui.components.MirthTextField cliendId;
    private javax.swing.JLabel clientIdLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField connectionFactoryClass;
    private javax.swing.JLabel connectionFactoryClassLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField connectionFactoryJndi;
    private javax.swing.JLabel connectionFactoryJndiLabel;
    private javax.swing.JButton deleteButton;
    private javax.swing.ButtonGroup durableButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton durableNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton durableYes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel9;
    private com.webreach.mirth.client.ui.components.MirthTextField jmsURL;
    private javax.swing.JLabel jmsUrlLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField jndiInitialFactory;
    private javax.swing.JLabel jndiInitialFactoryLabel;
    private javax.swing.JButton newButton;
    private com.webreach.mirth.client.ui.components.MirthPasswordField password;
    private javax.swing.JScrollPane propertiesPane;
    private com.webreach.mirth.client.ui.components.MirthTable propertiesTable;
    private com.webreach.mirth.client.ui.components.MirthTextField queue;
    private com.webreach.mirth.client.ui.components.MirthTextField selector;
    private com.webreach.mirth.client.ui.components.MirthComboBox specDropDown;
    private com.webreach.mirth.client.ui.components.MirthRadioButton useJNDINo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton useJNDIYes;
    private javax.swing.ButtonGroup useJndiButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthTextField username;
    // End of variables declaration//GEN-END:variables

}
