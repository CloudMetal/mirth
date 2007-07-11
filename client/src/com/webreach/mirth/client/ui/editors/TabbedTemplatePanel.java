/*
 * TabbedTemplatePanel.java
 *
 * Created on February 2, 2007, 1:50 PM
 */

package com.webreach.mirth.client.ui.editors;

import com.webreach.mirth.model.Rule;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JComboBox;

import org.syntax.jedit.SyntaxDocument;

import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.TreePanel;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.VariableListHandler;
import com.webreach.mirth.client.ui.panels.reference.VariableReferenceTable;
import com.webreach.mirth.model.Step;
import java.util.LinkedHashMap;

/**
 * 
 * @author brendanh
 */
public class TabbedTemplatePanel extends javax.swing.JPanel
{
    private VariableReferenceTable globalVarTable, dbVarTable;

    private SyntaxDocument incomingHL7Doc, outgoingHL7Doc;

    private TreePanel incomingTreePanel, outgoingTreePanel;

    private MirthEditorPane parent;

    /** Creates new form TabbedTemplatePanel */
    public TabbedTemplatePanel(MirthEditorPane p)
    {
        parent = p;
        initComponents();
        resizePanes();

        // ArrayList<ReferenceListItem> functionListItems = new
        // ReferenceListBuilder().getVariableListItems();
        variableTable = new VariableReferenceTable("Available Variables", new String[] {});
        variableTable.setDragEnabled(true);
        variableTable.setTransferHandler(new VariableListHandler("$('", "')"));
        variableListScrollPane.setViewportView(variableTable);
    }

    public void resizePanes()
    {
        variableSplitPane.setDividerLocation((int) (PlatformUI.MIRTH_FRAME.currentContentPage.getHeight() / 2 - PlatformUI.MIRTH_FRAME.currentContentPage.getHeight() / 10));
        incoming.resizePanes();
        outgoing.resizePanes();
    }

    public void updateVariables(List<Rule> rules, List<Step> steps)
    {
        variableTable.updateVariables(rules, steps);
    }

    public String getIncomingMessage()
    {
        return incoming.getMessage();
    }

    public void setIncomingMessage(String msg)
    {
        incoming.setMessage(msg);
    }

    public String getOutgoingMessage()
    {
        return outgoing.getMessage();
    }

    public void setOutgoingMessage(String msg)
    {
        outgoing.setMessage(msg);
    }

    public void setIncomingDataType(String protocol)
    {
        incoming.setProtocol(protocol);
    }

    public void setOutgoingDataType(String protocol)
    {
        outgoing.setProtocol(protocol);
    }

    public String getIncomingDataType()
    {
        return incoming.getProtocol();
    }

    public String getOutgoingDataType()
    {
        return outgoing.getProtocol();
    }

    public void setIncomingDataProperties(Properties properties)
    {
        incoming.setDataProperties(properties);
    }

    public void setOutgoingDataProperties(Properties properties)
    {
        outgoing.setDataProperties(properties);
    }

    public Properties getIncomingDataProperties()
    {
        return incoming.getDataProperties();
    }

    public Properties getOutgoingDataProperties()
    {
        return outgoing.getDataProperties();
    }

    public void setDefaultComponent()
    {
        tabPanel.setSelectedIndex(0);
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
        tabPanel = new javax.swing.JTabbedPane();
        variableTab = new javax.swing.JPanel();
        variableSplitPane = new javax.swing.JSplitPane();
        functionList = new com.webreach.mirth.client.ui.FunctionList();
        variableListScrollPane = new javax.swing.JScrollPane();
        variableTable = new com.webreach.mirth.client.ui.panels.reference.VariableReferenceTable();
        incomingTab = new javax.swing.JPanel();
        incoming = new MessageTreeTemplate(UIConstants.INCOMING_DATA);
        outgoingTab = new javax.swing.JPanel();
        outgoing = new MessageTreeTemplate(UIConstants.OUTGOING_DATA);

        variableTab.setBackground(new java.awt.Color(255, 255, 255));
        variableSplitPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        variableSplitPane.setDividerLocation(84);
        variableSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        functionList.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        variableSplitPane.setLeftComponent(functionList);

        variableListScrollPane.setViewportView(variableTable);

        variableSplitPane.setRightComponent(variableListScrollPane);

        org.jdesktop.layout.GroupLayout variableTabLayout = new org.jdesktop.layout.GroupLayout(variableTab);
        variableTab.setLayout(variableTabLayout);
        variableTabLayout.setHorizontalGroup(
            variableTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, variableSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
        );
        variableTabLayout.setVerticalGroup(
            variableTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, variableTabLayout.createSequentialGroup()
                .addContainerGap()
                .add(variableSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE))
        );
        tabPanel.addTab("Reference", variableTab);

        incomingTab.setBackground(new java.awt.Color(255, 255, 255));

        org.jdesktop.layout.GroupLayout incomingTabLayout = new org.jdesktop.layout.GroupLayout(incomingTab);
        incomingTab.setLayout(incomingTabLayout);
        incomingTabLayout.setHorizontalGroup(
            incomingTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(incoming, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
        );
        incomingTabLayout.setVerticalGroup(
            incomingTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, incoming, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
        );
        tabPanel.addTab("Incoming Data", incomingTab);

        outgoingTab.setBackground(new java.awt.Color(255, 255, 255));

        org.jdesktop.layout.GroupLayout outgoingTabLayout = new org.jdesktop.layout.GroupLayout(outgoingTab);
        outgoingTab.setLayout(outgoingTabLayout);
        outgoingTabLayout.setHorizontalGroup(
            outgoingTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(outgoing, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
        );
        outgoingTabLayout.setVerticalGroup(
            outgoingTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, outgoing, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
        );
        tabPanel.addTab("Outgoing Data", outgoingTab);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tabPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tabPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.FunctionList functionList;
    private com.webreach.mirth.client.ui.editors.MessageTreeTemplate incoming;
    public javax.swing.JPanel incomingTab;
    private com.webreach.mirth.client.ui.editors.MessageTreeTemplate outgoing;
    public javax.swing.JPanel outgoingTab;
    public javax.swing.JTabbedPane tabPanel;
    private javax.swing.JScrollPane variableListScrollPane;
    private javax.swing.JSplitPane variableSplitPane;
    private javax.swing.JPanel variableTab;
    private com.webreach.mirth.client.ui.panels.reference.VariableReferenceTable variableTable;
    // End of variables declaration//GEN-END:variables

}
