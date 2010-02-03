/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.client.ui;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A panel that contains the MirthVariableList.
 */
public class VariableList extends javax.swing.JPanel {

    /** Creates new form VariableList */
    public VariableList() {
        initComponents();
    }

    public void setPrefixAndSuffix(String prefix, String suffix) {
        mirthVariableList.setPrefixAndSuffix(prefix, suffix);
    }

    /**
     * Set the variable list from a list of steps.
     */
    public void setVariableListInbound(Set<String> additionalVariables) {
        LinkedHashSet<String> variables = new LinkedHashSet<String>();
        int i = 0;
        variables.add("Message ID");
        variables.add("Raw Data");
        variables.add("Transformed Data");
        variables.add("Encoded Data");
        variables.add("Message Source");
        variables.add("Message Type");
        variables.add("Message Version");
        variables.add("Date");
        variables.add("Formatted Date");
        variables.add("Timestamp");
        variables.add("Unique ID");
        variables.add("Original File Name");
        variables.add("Count");
        variables.add("Entity Encoder");
        variables.add("CDATA Tag");
        variables.add("DICOM Message Raw Data");
        variables.add("Message with Attachment Data");
        variables.addAll(additionalVariables);

        mirthVariableList.removeAll();
        mirthVariableList.setListData(variables.toArray());

        jScrollPane1.setViewportView(mirthVariableList);
    }

    public void setVariableListOutbound() {
        ArrayList<String> variables = new ArrayList<String>();
        variables.add("Raw Data");
        variables.add("Transformed Data");
        variables.add("Encoded Data");
        mirthVariableList.removeAll();
        mirthVariableList.setListData(variables.toArray());
        jScrollPane1.setViewportView(mirthVariableList);
    }

    public void setSourceMappingsLabel() {
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Source Mappings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
    }

    public void setDestinationMappingsLabel() {
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Destination Mappings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        mirthVariableList = new com.webreach.mirth.client.ui.components.MirthVariableList();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder("Variable List"));

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        mirthVariableList.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mirthVariableList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(mirthVariableList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private com.webreach.mirth.client.ui.components.MirthVariableList mirthVariableList;
    // End of variables declaration//GEN-END:variables
}
