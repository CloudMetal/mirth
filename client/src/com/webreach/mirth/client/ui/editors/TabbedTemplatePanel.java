/*
 * TabbedTemplatePanel.java
 *
 * Created on February 2, 2007, 1:50 PM
 */

package com.webreach.mirth.client.ui.editors;

import com.webreach.mirth.client.ui.FunctionListBuilder;
import com.webreach.mirth.client.ui.FunctionListItem;
import com.webreach.mirth.client.ui.HL7XMLTreePanel;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.ReferenceTableHandler;
import com.webreach.mirth.client.ui.components.MirthSyntaxTextArea;
import com.webreach.mirth.client.ui.util.SQLParserUtil;
import com.webreach.mirth.model.Channel;
import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.HL7TokenMarker;

/**
 *
 * @author  brendanh
 */
public class TabbedTemplatePanel extends javax.swing.JPanel
{
    private VariableReferenceTable globalVarTable, dbVarTable;
    private SyntaxDocument incomingHL7Doc, outgoingHL7Doc;
    private HL7XMLTreePanel incomingTreePanel, outgoingTreePanel;
    
    /** Creates new form TabbedTemplatePanel */
    public TabbedTemplatePanel()
    {
        initComponents();
        variableSplitPane.setDividerLocation((int)(PlatformUI.MIRTH_FRAME.currentContentPage.getHeight()/2));
        ArrayList<FunctionListItem> functionListItems = new FunctionListBuilder().getVariableListItems();
        globalVarTable = new VariableReferenceTable(functionListItems);
        dbVarTable = new VariableReferenceTable();
        incomingDataType.setModel(new javax.swing.DefaultComboBoxModel(PlatformUI.MIRTH_FRAME.protocols.values().toArray()));
        outgoingDataType.setModel(new javax.swing.DefaultComboBoxModel(PlatformUI.MIRTH_FRAME.protocols.values().toArray()));
        variablePanel.addComponentListener(new ComponentListener()
        {
            public void componentResized(ComponentEvent arg0)
            {
            }
            
            public void componentMoved(ComponentEvent arg0)
            {
            }
            
            public void componentShown(ComponentEvent arg0)
            {
                updateSQL();
            }
            
            public void componentHidden(ComponentEvent arg0)
            {
                
            }
        });
        
        incoming.setTreePanel("msg", ".toString()");
        outgoing.setTreePanel("tmp", "");
        
        updateSQL();
    }
    
    public void update()
    {
        updateSQL();
    }
    
    private void updateSQL()
    {
        Object sqlStatement = PlatformUI.MIRTH_FRAME.channelEditPanel.getSourceConnector().getProperties().get("query");
        if ((sqlStatement != null) && (!sqlStatement.equals("")))
        {
            SQLParserUtil spu = new SQLParserUtil((String) sqlStatement);
            updateVariables(spu.Parse());
        }
        else
        {
            updateVariables(new String[] {});
        }
    }
    
    public void resizePanes()
    {
        variableSplitPane.setDividerLocation((int)(PlatformUI.MIRTH_FRAME.currentContentPage.getHeight()/2 - PlatformUI.MIRTH_FRAME.currentContentPage.getHeight()/10));
        incoming.resizePanes();
        outgoing.resizePanes();
    }
    
    private void updateVariables(String[] variables)
    {
        variablePanel.remove(dbVarTable);
        dbVarTable = new VariableReferenceTable(variables);
        dbVarTable.setDragEnabled(true);
        dbVarTable.setTransferHandler(new ReferenceTableHandler());
        variablePanel.add(dbVarTable, BorderLayout.CENTER);
        //varPanel = new JPanel();
        //varPanel.setLayout(new BorderLayout())
        Channel channel = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel;
        //Now that the db reader is available on inbound, we can show the vars
        //Chrisl 9/23
        //  if (channel.getDirection().equals(Channel.Direction.OUTBOUND))
        // {
        //varPanel.add(globalVarPanel, BorderLayout.NORTH);
        //varPanel.add(dbVarPanel, BorderLayout.CENTER);
        //   }
        //   else
        //  {
        //      varPanel.add(globalVarPanel, BorderLayout.CENTER);
        //  }
        //varScrollPane.setViewportView(varPanel);
        repaint();
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
    
    public void setDefaultComponent()
    {
        tabPanel.setSelectedIndex(0);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        tabPanel = new javax.swing.JTabbedPane();
        variableTab = new javax.swing.JPanel();
        variableSplitPane = new javax.swing.JSplitPane();
        functionPanel = new javax.swing.JPanel();
        variablePanel = new javax.swing.JPanel();
        incomingTab = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        incomingDataType = new com.webreach.mirth.client.ui.components.MirthComboBox();
        incoming = new com.webreach.mirth.client.ui.editors.MessageTreeTemplate();
        outgoingTab = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        outgoingDataType = new com.webreach.mirth.client.ui.components.MirthComboBox();
        outgoing = new com.webreach.mirth.client.ui.editors.MessageTreeTemplate();

        variableSplitPane.setDividerLocation(84);
        variableSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        functionPanel.setBackground(new java.awt.Color(255, 255, 255));
        functionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Function List Builder", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        org.jdesktop.layout.GroupLayout functionPanelLayout = new org.jdesktop.layout.GroupLayout(functionPanel);
        functionPanel.setLayout(functionPanelLayout);
        functionPanelLayout.setHorizontalGroup(
            functionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 389, Short.MAX_VALUE)
        );
        functionPanelLayout.setVerticalGroup(
            functionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 53, Short.MAX_VALUE)
        );
        variableSplitPane.setTopComponent(functionPanel);

        variablePanel.setBackground(new java.awt.Color(255, 255, 255));
        variablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Variables", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        org.jdesktop.layout.GroupLayout variablePanelLayout = new org.jdesktop.layout.GroupLayout(variablePanel);
        variablePanel.setLayout(variablePanelLayout);
        variablePanelLayout.setHorizontalGroup(
            variablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 389, Short.MAX_VALUE)
        );
        variablePanelLayout.setVerticalGroup(
            variablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 405, Short.MAX_VALUE)
        );
        variableSplitPane.setRightComponent(variablePanel);

        org.jdesktop.layout.GroupLayout variableTabLayout = new org.jdesktop.layout.GroupLayout(variableTab);
        variableTab.setLayout(variableTabLayout);
        variableTabLayout.setHorizontalGroup(
            variableTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, variableSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
        );
        variableTabLayout.setVerticalGroup(
            variableTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(variableSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 525, Short.MAX_VALUE)
        );
        tabPanel.addTab("Reference", variableTab);

        incomingTab.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Data Type:");

        incomingDataType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        incomingDataType.setEnabled(false);

        org.jdesktop.layout.GroupLayout incomingTabLayout = new org.jdesktop.layout.GroupLayout(incomingTab);
        incomingTab.setLayout(incomingTabLayout);
        incomingTabLayout.setHorizontalGroup(
            incomingTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(incomingTabLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(incomingDataType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(284, Short.MAX_VALUE))
            .add(incoming, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
        );
        incomingTabLayout.setVerticalGroup(
            incomingTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(incomingTabLayout.createSequentialGroup()
                .addContainerGap()
                .add(incomingTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(incomingDataType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(incoming, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE))
        );
        tabPanel.addTab("Incoming Data", incomingTab);

        outgoingTab.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Data Type:");

        outgoingDataType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout outgoingTabLayout = new org.jdesktop.layout.GroupLayout(outgoingTab);
        outgoingTab.setLayout(outgoingTabLayout);
        outgoingTabLayout.setHorizontalGroup(
            outgoingTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(outgoingTabLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outgoingDataType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(284, Short.MAX_VALUE))
            .add(outgoing, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
        );
        outgoingTabLayout.setVerticalGroup(
            outgoingTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(outgoingTabLayout.createSequentialGroup()
                .addContainerGap()
                .add(outgoingTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(outgoingDataType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outgoing, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE))
        );
        tabPanel.addTab("Outgoing Data", outgoingTab);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tabPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tabPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel functionPanel;
    private com.webreach.mirth.client.ui.editors.MessageTreeTemplate incoming;
    private com.webreach.mirth.client.ui.components.MirthComboBox incomingDataType;
    public javax.swing.JPanel incomingTab;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private com.webreach.mirth.client.ui.editors.MessageTreeTemplate outgoing;
    private com.webreach.mirth.client.ui.components.MirthComboBox outgoingDataType;
    public javax.swing.JPanel outgoingTab;
    public javax.swing.JTabbedPane tabPanel;
    private javax.swing.JPanel variablePanel;
    private javax.swing.JSplitPane variableSplitPane;
    private javax.swing.JPanel variableTab;
    // End of variables declaration//GEN-END:variables
    
}
