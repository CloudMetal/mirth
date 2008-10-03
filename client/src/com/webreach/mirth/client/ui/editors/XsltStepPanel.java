/*
 * XsltStepPanel.java
 *
 * Created on October 2, 2008, 10:58 AM
 */

package com.webreach.mirth.client.ui.editors;

import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;

import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
/**
 * 
 * @author chrisr
 */
public class XsltStepPanel extends BasePanel
{
    protected static SyntaxDocument mappingDoc;

    protected MirthEditorPane parent;

    /** Creates new form MapperPanel */
    public XsltStepPanel(MirthEditorPane p)
    {
        parent = p;
        initComponents();

        sourceVariableField.getDocument().addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent arg0)
            {
            }

            public void insertUpdate(DocumentEvent arg0)
            {
                updateTable();
                parent.modified = true;
            }

            public void removeUpdate(DocumentEvent arg0)
            {
                updateTable();
                parent.modified = true;
            }
        });

        resultVariableField.getDocument().addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent arg0)
            {
            }

            public void insertUpdate(DocumentEvent arg0)
            {
                parent.modified = true;
            }

            public void removeUpdate(DocumentEvent arg0)
            {
                parent.modified = true;
            }
        });

        mappingDoc = new SyntaxDocument();
        mappingDoc.setTokenMarker(new XMLTokenMarker());

        xsltTemplateTextPane.setDocument(mappingDoc);

    }

    public void updateTable()
    {
        if (parent.getSelectedRow() != -1 && !parent.getTableModel().getValueAt(parent.getSelectedRow(), parent.STEP_TYPE_COL).toString().equals("JavaScript"))
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    //parent.getTableModel().setValueAt(variableTextField.getText(), parent.getSelectedRow(), parent.STEP_NAME_COL);
                    parent.updateTaskPane(parent.getTableModel().getValueAt(parent.getSelectedRow(), parent.STEP_TYPE_COL).toString());
                }
            });
        }
    }

    public Map<Object, Object> getData()
    {
        Map<Object, Object> m = new HashMap<Object, Object>();
        m.put("Source", sourceVariableField.getText().trim());
        m.put("Result", resultVariableField.getText().trim());
        m.put("XsltTemplate", xsltTemplateTextPane.getText());
        return m;
    }

    public void setData(Map<Object, Object> data)
    {
        boolean modified = parent.modified;

        if (data != null)
        {
            sourceVariableField.setText((String) data.get("Source"));
            resultVariableField.setText((String) data.get("Result"));
            xsltTemplateTextPane.setText((String) data.get("XsltTemplate"));
        }
        else
        {
            sourceVariableField.setText("");
            resultVariableField.setText("");
            xsltTemplateTextPane.setText("");
        }

        parent.modified = modified;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        sourceVariableField = new javax.swing.JTextField();
        resultVariableField = new javax.swing.JTextField();
        xsltTemplateTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea(false,false);

        setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setText("Source:");

        jLabel2.setText("Result:");

        jLabel4.setText("XSLT Template:");

        xsltTemplateTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel2)
                    .add(jLabel4)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, sourceVariableField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, xsltTemplateTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                    .add(resultVariableField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sourceVariableField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(resultVariableField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(xsltTemplateTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField resultVariableField;
    private javax.swing.JTextField sourceVariableField;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea xsltTemplateTextPane;
    // End of variables declaration//GEN-END:variables

}