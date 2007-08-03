/*
 * MessageTreeTemplate.java
 *
 * Created on February 2, 2007, 2:58 PM
 */

package com.webreach.mirth.client.ui.editors;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.beans.HL7Properties;
import com.webreach.mirth.client.ui.beans.NCPDPProperties;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingworker.SwingWorker;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.EDITokenMarker;
import org.syntax.jedit.tokenmarker.HL7TokenMarker;
import org.syntax.jedit.tokenmarker.X12TokenMarker;

import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.TreePanel;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.beans.EDIProperties;
import com.webreach.mirth.client.ui.beans.X12Properties;
import com.webreach.mirth.model.MessageObject;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;

/**
 *
 * @author brendanh
 */
public class MessageTreeTemplate extends javax.swing.JPanel
{
    public final String DEFAULT_TEXT = "Paste a sample message here.";
    
    private SyntaxDocument HL7Doc;
    
    private TreePanel treePanel;
    
    private String currentMessage = "";
    
    private String data;
    
    private Properties dataProperties;
    
    private Timer timer;
    
    /** Creates new form MessageTreeTemplate */
    public MessageTreeTemplate()
    {
        
    }
    
    public MessageTreeTemplate(String data)
    {
        this.data = data;
        
        initComponents();
        
        try
        {
            resizePanes();
        }
        catch (Exception e)
        {
            
        }
        
        if (data.equals(UIConstants.INCOMING_DATA))
        {
            dataType.setEnabled(false);
            setTreePanel("msg", ".toString()");
        }
        else if (data.equals(UIConstants.OUTGOING_DATA))
        {
            dataType.setEnabled(true);
            setTreePanel("tmp", "");
        }
        
        dataType.setModel(new javax.swing.DefaultComboBoxModel(PlatformUI.MIRTH_FRAME.protocols.values().toArray()));
        
        HL7Doc = new SyntaxDocument();
        HL7Doc.setTokenMarker(new HL7TokenMarker());
        pasteBox.setDocument(HL7Doc);
        //  pasteBox.setPreferredSize(new Dimension(100,100));
        //  pasteBox.setFont(EditorConstants.DEFAULT_FONT);
        
        // handles updating the tree
        pasteBox.getDocument().addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent e)
            {
                updateText();
            }
            
            public void insertUpdate(DocumentEvent e)
            {
                updateText();
            }
            
            public void removeUpdate(DocumentEvent e)
            {
                updateText();
            }
        });
        pasteBox.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if (e.getButton() == MouseEvent.BUTTON2){
					if (pasteBox.getText().equals(DEFAULT_TEXT))
			        {
			            pasteBox.setText("");
			        }
				}
			}

			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				if (e.getButton() == MouseEvent.BUTTON2){
					 if (pasteBox.getText().length() == 0)
				     {
				         pasteBox.setText(DEFAULT_TEXT);
				     }
				}
			}
        
        });
    }
    
    private void updateText()
    {
        class UpdateTimer extends TimerTask
        {
            
            @Override
            public void run()
            {
                
                if (!currentMessage.equals(pasteBox.getText())){
                	PlatformUI.MIRTH_FRAME.setWorking("Parsing...", true);
                
                	String message = pasteBox.getText();
                	currentMessage = message;
                	treePanel.setMessage(dataProperties, (String) dataType.getSelectedItem(), message, DEFAULT_TEXT, dataProperties);
                	PlatformUI.MIRTH_FRAME.setWorking("", false);
                	
                }
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
            PlatformUI.MIRTH_FRAME.setWorking("", false);
            timer = new Timer();
            timer.schedule(new UpdateTimer(), 1000);
        }
        
        //treePanel.revalidate();
        //treePanel.repaint();
    }
    
    public void setTreePanel(String prefix, String suffix)
    {
        treePanel = new TreePanel(prefix, suffix);
        treeScrollPane.setViewportView(treePanel);
    }
    
    public String getMessage()
    {
        if (pasteBox.getText().equals(DEFAULT_TEXT))
            return "";
        else
            return pasteBox.getText().replace('\n', '\r');
    }
    
    public void setMessage(String msg)
    {
        if (msg != null)
            msg = msg.replace('\r', '\n');
        pasteBox.setText(msg);
        pasteBoxFocusLost(null);
        updateText();
    }
    
    public void clearMessage()
    {
        treePanel.clearMessage();
        pasteBoxFocusLost(null);
        updateText();
    }
    
    public void setProtocol(String protocol)
    {
        dataType.setSelectedItem(protocol);
        
        setDocType(protocol);
    }
    
    private void setDocType(String protocol)
    {
        if (protocol.equals("HL7 v2.x"))
        {
            HL7Doc.setTokenMarker(new HL7TokenMarker());
        }
        else if (protocol.equals("EDI"))
        {
            HL7Doc.setTokenMarker(new EDITokenMarker());
        }
        else if (protocol.equals("X12"))
        {
            HL7Doc.setTokenMarker(new X12TokenMarker());
        }
        else if (protocol.equals("HL7 v3.0") || protocol.equals("XML"))
        {
            HL7Doc.setTokenMarker(new XMLTokenMarker());
        }
        pasteBox.setDocument(HL7Doc);
    }
    
    public String getProtocol()
    {
        return (String) dataType.getSelectedItem();
    }
    
    public Properties getDataProperties()
    {
        return dataProperties;
    }
    
    public void setDataProperties(Properties p)
    {
        if (p != null)
            dataProperties = p;
        else
            dataProperties = new Properties();
    }
    
    public void resizePanes()
    {
        split.setDividerLocation((int) (PlatformUI.MIRTH_FRAME.currentContentPage.getHeight() / 2 - PlatformUI.MIRTH_FRAME.currentContentPage.getHeight() / 10));
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
        jLabel1 = new javax.swing.JLabel();
        dataType = new javax.swing.JComboBox();
        split = new javax.swing.JSplitPane();
        pasteBox = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        treeScrollPane = new javax.swing.JScrollPane();
        properties = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jLabel1.setText("Data Type:");

        dataType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        dataType.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                dataTypeActionPerformed(evt);
            }
        });

        split.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        pasteBox.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusGained(java.awt.event.FocusEvent evt)
            {
                pasteBoxFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                pasteBoxFocusLost(evt);
            }
        });

        split.setLeftComponent(pasteBox);

        treeScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        treeScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        split.setRightComponent(treeScrollPane);

        properties.setText("Properties");
        properties.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                propertiesActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dataType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(properties)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, split, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(dataType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(properties))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(split, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // GEN-FIRST:event_propertiesActionPerformed
    private void propertiesActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-HEADEREND:event_propertiesActionPerformed
        PlatformUI.MIRTH_FRAME.enableSave();
        currentMessage = "";
        if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.EDI)))
            new BoundPropertiesSheetDialog(dataProperties, new EDIProperties());
        else if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.X12)))
            new BoundPropertiesSheetDialog(dataProperties, new X12Properties());
        else if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.HL7V2)))
            new BoundPropertiesSheetDialog(dataProperties, new HL7Properties());
        else if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.NCPDP)))
            new BoundPropertiesSheetDialog(dataProperties, new NCPDPProperties());
        updateText();
    }// GEN-LAST:event_propertiesActionPerformed
    
    // GEN-FIRST:event_dataTypeActionPerformed
    private void dataTypeActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-HEADEREND:event_dataTypeActionPerformed
        PlatformUI.MIRTH_FRAME.enableSave();
        currentMessage = "";
        if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.X12)) ||
                ((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.EDI)) ||
                ((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.HL7V2)) ||
                ((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.NCPDP)))
            properties.setEnabled(true);
        else
            properties.setEnabled(false);
        dataProperties = new Properties();
        setDocType((String)dataType.getSelectedItem());
        updateText();
        
    }// GEN-LAST:event_dataTypeActionPerformed
    
    // GEN-FIRST:event_pasteBoxFocusLost
    private void pasteBoxFocusLost(java.awt.event.FocusEvent evt)
    {// GEN-HEADEREND:event_pasteBoxFocusLost
        if (pasteBox.getText().length() == 0)
        {
            pasteBox.setText(DEFAULT_TEXT);
        }
    }// GEN-LAST:event_pasteBoxFocusLost
    
    // GEN-FIRST:event_pasteBoxFocusGained
    private void pasteBoxFocusGained(java.awt.event.FocusEvent evt)
    {// GEN-HEADEREND:event_pasteBoxFocusGained
        if (pasteBox.getText().equals(DEFAULT_TEXT))
        {
            pasteBox.setText("");
        }
    }// GEN-LAST:event_pasteBoxFocusGained
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox dataType;
    private javax.swing.JLabel jLabel1;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea pasteBox;
    private javax.swing.JButton properties;
    private javax.swing.JSplitPane split;
    private javax.swing.JScrollPane treeScrollPane;
    // End of variables declaration//GEN-END:variables
    
}
