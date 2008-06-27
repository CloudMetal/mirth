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

package com.webreach.mirth.connectors.dimse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;

import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Step;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class DICOMListener extends ConnectorClass
{
    /**
     * Creates new form DICOMListener
     */

    public DICOMListener()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        name = DICOMListenerProperties.name;
        initComponents();
    }

    @Override
    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DICOMListenerProperties.DICOM_ADDRESS, listenerAddressField.getText());
        properties.put(DICOMListenerProperties.DICOM_PORT, listenerPortField.getText());
        properties.put(DICOMListenerProperties.DICOM_ASYNC, asyncField.getText());
        properties.put(DICOMListenerProperties.DICOM_BUFSIZE, bufsizeField.getText());

        if(pdv1Yes.isSelected()){
            properties.put(DICOMListenerProperties.DICOM_PDV1,UIConstants.YES_OPTION);
        }
        else {
            properties.put(DICOMListenerProperties.DICOM_PDV1,UIConstants.NO_OPTION);
        }
        if(bigendianYes.isSelected()){
            properties.put(DICOMListenerProperties.DICOM_BIGENDIAN,UIConstants.YES_OPTION);
        }
        else {
            properties.put(DICOMListenerProperties.DICOM_BIGENDIAN,UIConstants.NO_OPTION);
        }
        if(deftsYes.isSelected()){
            properties.put(DICOMListenerProperties.DICOM_DEFTS,UIConstants.YES_OPTION);
        }
        else {
            properties.put(DICOMListenerProperties.DICOM_DEFTS,UIConstants.NO_OPTION);
        }      
        if(nativeYes.isSelected()){
            properties.put(DICOMListenerProperties.DICOM_NATIVE,UIConstants.YES_OPTION);
        }
        else {
            properties.put(DICOMListenerProperties.DICOM_NATIVE,UIConstants.NO_OPTION);
        }             
        properties.put(DICOMListenerProperties.DICOM_DEST,destField.getText());
        properties.put(DICOMListenerProperties.DICOM_RCVPDULEN,rcvpdulenField.getText());
        properties.put(DICOMListenerProperties.DICOM_SNDPDULEN,sndpdulenField.getText());        
        properties.put(DICOMListenerProperties.DICOM_REAPER,reaperField.getText());
        properties.put(DICOMListenerProperties.DICOM_RELEASETO, releasetoField.getText());
        properties.put(DICOMListenerProperties.DICOM_REQUESTTO, requesttoField.getText());
        properties.put(DICOMListenerProperties.DICOM_RSPDELAY, rspdelayField.getText());
        
        properties.put(DICOMListenerProperties.DICOM_IDLETO, idletoField.getText());
        properties.put(DICOMListenerProperties.DICOM_SOCLOSEDELAY,soclosedelayField.getText());
        properties.put(DICOMListenerProperties.DICOM_SORCVBUF,sorcvbufField.getText());
        properties.put(DICOMListenerProperties.DICOM_SOSNDBUF,sosndbufField.getText());
        if(tcpdelayYes.isSelected()){
            properties.put(DICOMListenerProperties.DICOM_TCPDELAY,UIConstants.YES_OPTION);
        }
        else {
            properties.put(DICOMListenerProperties.DICOM_TCPDELAY,UIConstants.NO_OPTION);
        }

        
        properties.put(DICOMListenerProperties.DICOM_KEYPW, keyPasswordField.getText());
        properties.put(DICOMListenerProperties.DICOM_KEYSTORE,keyStoreField.getText());
        properties.put(DICOMListenerProperties.DICOM_KEYSTOREPW, keyStorePasswordField.getText());

        if(noclientauthYes.isSelected()){
            properties.put(DICOMListenerProperties.DICOM_NOCLIENTAUTH,UIConstants.YES_OPTION);
        }
        else {
            properties.put(DICOMListenerProperties.DICOM_NOCLIENTAUTH,UIConstants.NO_OPTION);
        }
        if(nossl2Yes.isSelected()){
            properties.put(DICOMListenerProperties.DICOM_NOSSL2,UIConstants.YES_OPTION);
        }
        else {
            properties.put(DICOMListenerProperties.DICOM_NOSSL2,UIConstants.NO_OPTION);
        }
        if(tlsaes.isSelected()){
            properties.put(DICOMListenerProperties.DICOM_TLS,"aes");
        }
        else if(tls3des.isSelected()){
            properties.put(DICOMListenerProperties.DICOM_TLS,"3des");
        }
        else if(tlswithout.isSelected()){
            properties.put(DICOMListenerProperties.DICOM_TLS,"without");
        }
        else {
            properties.put(DICOMListenerProperties.DICOM_TLS,"notls");
        }
        properties.put(DICOMListenerProperties.DICOM_TRUSTSTORE,truststoreField.getText());
        properties.put(DICOMListenerProperties.DICOM_TRUSTSTOREPW,truststorepwField.getText());
        
        
        
        return properties;
    }

    @Override
    public void setProperties(Properties props)
    {
        resetInvalidProperties();
        
        listenerAddressField.setText((String) props.get(DICOMListenerProperties.DICOM_ADDRESS));
        listenerPortField.setText((String) props.get(DICOMListenerProperties.DICOM_PORT));
        
        listenerAddressField.setText((String) props.get(DICOMListenerProperties.DICOM_ADDRESS));
        listenerPortField.setText((String) props.get(DICOMListenerProperties.DICOM_PORT));
        asyncField.setText((String) props.get(DICOMListenerProperties.DICOM_ASYNC));
        bufsizeField.setText((String) props.get(DICOMListenerProperties.DICOM_BUFSIZE));        
        rcvpdulenField.setText((String) props.get(DICOMListenerProperties.DICOM_RCVPDULEN));        
        reaperField.setText((String) props.get(DICOMListenerProperties.DICOM_REAPER));        
        releasetoField.setText((String) props.get(DICOMListenerProperties.DICOM_RELEASETO));   
        requesttoField.setText((String) props.get(DICOMListenerProperties.DICOM_REQUESTTO));   
        idletoField.setText((String) props.get(DICOMListenerProperties.DICOM_IDLETO));   
        rspdelayField.setText((String) props.get(DICOMListenerProperties.DICOM_RSPDELAY));   
        sndpdulenField.setText((String) props.get(DICOMListenerProperties.DICOM_SNDPDULEN));        
        soclosedelayField.setText((String) props.get(DICOMListenerProperties.DICOM_SOCLOSEDELAY));        
        sorcvbufField.setText((String) props.get(DICOMListenerProperties.DICOM_SORCVBUF));        
        sosndbufField.setText((String) props.get(DICOMListenerProperties.DICOM_SOSNDBUF));        
        destField.setText((String) props.get(DICOMListenerProperties.DICOM_DEST));  
     
        if (((String) props.get(DICOMListenerProperties.DICOM_PDV1)).equals(UIConstants.YES_OPTION))
            pdv1Yes.setSelected(true);
        else
            pdv1No.setSelected(true);
        if (((String) props.get(DICOMListenerProperties.DICOM_BIGENDIAN)).equals(UIConstants.YES_OPTION)){
            bigendianYes.setSelected(true);
            bigendianYesActionPerformed(null);
        }
        else {
            bigendianNo.setSelected(true); 
            bigendianNoActionPerformed(null);
        }
        if (((String) props.get(DICOMListenerProperties.DICOM_DEFTS)).equals(UIConstants.YES_OPTION)) {
            deftsYes.setSelected(true);
            deftsYesActionPerformed(null);
        }
        else {
            deftsNo.setSelected(true);    
            deftsNoActionPerformed(null);
        }
        if (((String) props.get(DICOMListenerProperties.DICOM_NATIVE)).equals(UIConstants.YES_OPTION)) {
            nativeYes.setSelected(true);
            nativeYesActionPerformed(null);
        }
        else {
            nativeNo.setSelected(true);
            nativeNoActionPerformed(null);
        }
        if (((String) props.get(DICOMListenerProperties.DICOM_TCPDELAY)).equals(UIConstants.YES_OPTION))
            tcpdelayYes.setSelected(true);
        else
            tcpdelayNo.setSelected(true);

        keyPasswordField.setText((String) props.get(DICOMListenerProperties.DICOM_KEYPW));
        keyStoreField.setText((String) props.get(DICOMListenerProperties.DICOM_KEYSTORE));
        keyStorePasswordField.setText((String) props.get(DICOMListenerProperties.DICOM_KEYSTOREPW));        
        truststoreField.setText((String) props.get(DICOMListenerProperties.DICOM_TRUSTSTORE));        
        truststorepwField.setText((String) props.get(DICOMListenerProperties.DICOM_TRUSTSTOREPW));        
        if (((String) props.get(DICOMListenerProperties.DICOM_NOCLIENTAUTH)).equals(UIConstants.YES_OPTION))
            noclientauthYes.setSelected(true);
        else
            noclientauthNo.setSelected(true);
        if (((String) props.get(DICOMListenerProperties.DICOM_NOSSL2)).equals(UIConstants.YES_OPTION))
            nossl2Yes.setSelected(true);
        else
            nossl2No.setSelected(true);
        if (((String) props.get(DICOMListenerProperties.DICOM_TLS)).equals("aes")) {
            tlsaes.setSelected(true);
            tlsaesActionPerformed(null);
        }
        else if(((String) props.get(DICOMListenerProperties.DICOM_TLS)).equals("3des")){
            tls3des.setSelected(true);
            tls3desActionPerformed(null);
        }
        else if(((String) props.get(DICOMListenerProperties.DICOM_TLS)).equals("without")){
            tlswithout.setSelected(true);
            tlswithoutActionPerformed(null);
        }
        else { 
            tlsno.setSelected(true);
            tlsnoActionPerformed(null);
        }
        
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();
        
        updateResponseDropDown();
        

        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    @Override
    public Properties getDefaults()
    {
        return new DICOMListenerProperties().getDefaults();
    }

    @Override
    public boolean checkProperties(Properties props, boolean highlight)
    {
        resetInvalidProperties();
        boolean valid = true;
        
        if (((String) props.get(DICOMListenerProperties.DICOM_ADDRESS)).length() <= 3)
        {
            valid = false;
            if (highlight)
            	listenerAddressField.setBackground(UIConstants.INVALID_COLOR); 
        }
        if (((String) props.get(DICOMListenerProperties.DICOM_PORT)).length() == 0)
        {
            valid = false;
            if (highlight)
            	listenerPortField.setBackground(UIConstants.INVALID_COLOR);
        }
        
        return valid;
    }
    
    private void resetInvalidProperties()
    {
        listenerAddressField.setBackground(null);
        listenerPortField.setBackground(null);
    }
    
    @Override
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
    private void initComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        buttonGroup8 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        listenerPortField = new com.webreach.mirth.client.ui.components.MirthTextField();
        listenerAddressField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel5 = new javax.swing.JLabel();
        asyncField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel17 = new javax.swing.JLabel();
        pdv1Yes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        pdv1No = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel18 = new javax.swing.JLabel();
        reaperField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel19 = new javax.swing.JLabel();
        releasetoField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel23 = new javax.swing.JLabel();
        soclosedelayField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel22 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        sndpdulenField = new com.webreach.mirth.client.ui.components.MirthTextField();
        rcvpdulenField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        sorcvbufField = new com.webreach.mirth.client.ui.components.MirthTextField();
        sosndbufField = new com.webreach.mirth.client.ui.components.MirthTextField();
        bufsizeField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel27 = new javax.swing.JLabel();
        tcpdelayYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        tcpdelayNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel20 = new javax.swing.JLabel();
        requesttoField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel21 = new javax.swing.JLabel();
        idletoField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel26 = new javax.swing.JLabel();
        rspdelayField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel28 = new javax.swing.JLabel();
        bigendianYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        bigendianNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel29 = new javax.swing.JLabel();
        deftsYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        deftsNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel30 = new javax.swing.JLabel();
        destField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel31 = new javax.swing.JLabel();
        nativeYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        nativeNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel32 = new javax.swing.JLabel();
        tls3des = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        tlsaes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        tlswithout = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        tlsno = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel12 = new javax.swing.JLabel();
        noclientauthYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        noclientauthNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel13 = new javax.swing.JLabel();
        nossl2Yes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        nossl2No = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel10 = new javax.swing.JLabel();
        keyStoreField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel33 = new javax.swing.JLabel();
        truststoreField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel9 = new javax.swing.JLabel();
        keyStorePasswordField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel34 = new javax.swing.JLabel();
        truststorepwField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel11 = new javax.swing.JLabel();
        keyPasswordField = new com.webreach.mirth.client.ui.components.MirthTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel1.setText("Listener Address:");

        jLabel2.setText("Listener Port:");

        jLabel5.setText("Max Async operations:");

        jLabel17.setText("Pack PDV:");

        pdv1Yes.setBackground(new java.awt.Color(255, 255, 255));
        pdv1Yes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(pdv1Yes);
        pdv1Yes.setText("Yes");
        pdv1Yes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        pdv1No.setBackground(new java.awt.Color(255, 255, 255));
        pdv1No.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(pdv1No);
        pdv1No.setSelected(true);
        pdv1No.setText("No");
        pdv1No.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel18.setText("DIMSE-RSP interval period (s):");

        jLabel19.setText("A-RELEASE-RP timeout (s):");

        jLabel23.setText("Socket Close Delay After A-ABORT (ms):");

        jLabel22.setText("P-DATA-TF PDUs max length sent (KB):");

        jLabel16.setText("P-DATA-TF PDUs  max length received (KB):");

        jLabel6.setText("Transcoder Buffer Size (KB):");

        jLabel25.setText("Send Socket Buffer Size (KB):");

        jLabel24.setText("Receive Socket Buffer Size (KB):");

        jLabel27.setText("TCP Delay:");

        tcpdelayYes.setBackground(new java.awt.Color(255, 255, 255));
        tcpdelayYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(tcpdelayYes);
        tcpdelayYes.setSelected(true);
        tcpdelayYes.setText("Yes");
        tcpdelayYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        tcpdelayNo.setBackground(new java.awt.Color(255, 255, 255));
        tcpdelayNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(tcpdelayNo);
        tcpdelayNo.setText("No");
        tcpdelayNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel20.setText("ASSOCIATE-RQ timeout (ms):");

        jLabel21.setText("DIMSE-RQ timeout (ms):");

        jLabel26.setText("DIMSE-RSP delay (ms):");

        jLabel28.setText("Accept Explict VR Big Endian:");

        bigendianYes.setBackground(new java.awt.Color(255, 255, 255));
        bigendianYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(bigendianYes);
        bigendianYes.setText("Yes");
        bigendianYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bigendianYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bigendianYesActionPerformed(evt);
            }
        });

        bigendianNo.setBackground(new java.awt.Color(255, 255, 255));
        bigendianNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(bigendianNo);
        bigendianNo.setSelected(true);
        bigendianNo.setText("No");
        bigendianNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bigendianNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bigendianNoActionPerformed(evt);
            }
        });

        jLabel29.setText("Only Accept Default Transfer Syntax:");

        deftsYes.setBackground(new java.awt.Color(255, 255, 255));
        deftsYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(deftsYes);
        deftsYes.setText("Yes");
        deftsYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        deftsYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deftsYesActionPerformed(evt);
            }
        });

        deftsNo.setBackground(new java.awt.Color(255, 255, 255));
        deftsNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(deftsNo);
        deftsNo.setSelected(true);
        deftsNo.setText("No");
        deftsNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        deftsNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deftsNoActionPerformed(evt);
            }
        });

        jLabel30.setText("Store Recieved Objects in Directory:");

        jLabel31.setText("Only Uncompressed Pixel Data:");

        nativeYes.setBackground(new java.awt.Color(255, 255, 255));
        nativeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(nativeYes);
        nativeYes.setText("Yes");
        nativeYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nativeYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nativeYesActionPerformed(evt);
            }
        });

        nativeNo.setBackground(new java.awt.Color(255, 255, 255));
        nativeNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(nativeNo);
        nativeNo.setSelected(true);
        nativeNo.setText("No");
        nativeNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nativeNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nativeNoActionPerformed(evt);
            }
        });

        jLabel32.setText("TLS:");

        tls3des.setBackground(new java.awt.Color(255, 255, 255));
        tls3des.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(tls3des);
        tls3des.setText("3DES");
        tls3des.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tls3des.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tls3desActionPerformed(evt);
            }
        });

        tlsaes.setBackground(new java.awt.Color(255, 255, 255));
        tlsaes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(tlsaes);
        tlsaes.setText("AES");
        tlsaes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tlsaes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlsaesActionPerformed(evt);
            }
        });

        tlswithout.setBackground(new java.awt.Color(255, 255, 255));
        tlswithout.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(tlswithout);
        tlswithout.setText("Without");
        tlswithout.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tlswithout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlswithoutActionPerformed(evt);
            }
        });

        tlsno.setBackground(new java.awt.Color(255, 255, 255));
        tlsno.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(tlsno);
        tlsno.setSelected(true);
        tlsno.setText("No TLS");
        tlsno.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tlsno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlsnoActionPerformed(evt);
            }
        });

        jLabel12.setText("Client Authentication TLS:");

        noclientauthYes.setBackground(new java.awt.Color(255, 255, 255));
        noclientauthYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(noclientauthYes);
        noclientauthYes.setSelected(true);
        noclientauthYes.setText("Yes");
        noclientauthYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        noclientauthNo.setBackground(new java.awt.Color(255, 255, 255));
        noclientauthNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(noclientauthNo);
        noclientauthNo.setText("No");
        noclientauthNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel13.setText("Accept ssl v2 TLS handshake:");

        nossl2Yes.setBackground(new java.awt.Color(255, 255, 255));
        nossl2Yes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup8.add(nossl2Yes);
        nossl2Yes.setSelected(true);
        nossl2Yes.setText("Yes");
        nossl2Yes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        nossl2No.setBackground(new java.awt.Color(255, 255, 255));
        nossl2No.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup8.add(nossl2No);
        nossl2No.setText("No");
        nossl2No.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel10.setText("Keystore:");

        jLabel33.setText("Trust Store:");

        jLabel9.setText("Keystore Password:");

        jLabel34.setText("Trust Store Password:");

        jLabel11.setText("Key Password:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel11)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel10)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel13)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel28)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel29)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel31)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel27)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel30)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel5)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel17)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel18)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel19)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel20)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel26)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel21)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel23)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel32)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel12)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel33))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(destField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 103, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(deftsYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deftsNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(bigendianYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bigendianNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(nativeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nativeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(tcpdelayYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(tcpdelayNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(nossl2Yes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nossl2No, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(noclientauthYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(noclientauthNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(keyPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(listenerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(pdv1Yes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pdv1No, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(asyncField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(listenerAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 189, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rspdelayField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(releasetoField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(soclosedelayField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(requesttoField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(reaperField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(idletoField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(jLabel22)
                                    .add(jLabel16))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(sndpdulenField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(rcvpdulenField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(56, 56, 56)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                            .add(jLabel25)
                                            .add(jLabel24))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(sosndbufField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(sorcvbufField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel6)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(bufsizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                            .add(tls3des, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(tlsaes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(tlswithout, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(tlsno, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, truststoreField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, keyStoreField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(jLabel34)
                                .add(jLabel9))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, keyStorePasswordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, truststorepwField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)))))
                .addContainerGap(76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(listenerAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(listenerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(asyncField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pdv1Yes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel17)
                    .add(pdv1No, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(25, 25, 25)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel19)
                                    .add(releasetoField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel23)
                                    .add(soclosedelayField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel20)
                                    .add(requesttoField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(jLabel18)
                                .add(reaperField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel21)
                            .add(idletoField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel26)
                            .add(rspdelayField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(sndpdulenField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel22))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(rcvpdulenField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel16))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(sosndbufField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel24))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(sorcvbufField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel25))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(bufsizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel6))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel28)
                    .add(bigendianYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(bigendianNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(deftsYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel29)
                    .add(deftsNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nativeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(nativeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel31))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tcpdelayYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel27)
                    .add(tcpdelayNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel30)
                    .add(destField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel32)
                    .add(tls3des, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(tlsaes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(tlswithout, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(tlsno, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel12)
                            .add(noclientauthYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(noclientauthNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel13)
                            .add(nossl2Yes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(nossl2No, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel10)
                            .add(keyStoreField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel33)
                            .add(truststoreField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel11)
                            .add(keyPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(47, 47, 47)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(keyStorePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel9))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel34)
                            .add(truststorepwField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(24, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tlsnoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tlsnoActionPerformed
// TODO add your handling code here:
        // disable
        keyStoreField.setEnabled(false);
        keyPasswordField.setEnabled(false);
        keyStorePasswordField.setEnabled(false);
        truststoreField.setEnabled(false);
        truststorepwField.setEnabled(false);
        nossl2No.setEnabled(false);
        nossl2Yes.setEnabled(false);
        noclientauthNo.setEnabled(false);
        noclientauthYes.setEnabled(false);
        jLabel12.setEnabled(false);
        jLabel13.setEnabled(false);
        jLabel9.setEnabled(false);
        jLabel30.setEnabled(false);
        jLabel10.setEnabled(false);
        jLabel29.setEnabled(false);
        jLabel11.setEnabled(false);
        jLabel33.setEnabled(false);
        jLabel34.setEnabled(false);
    }//GEN-LAST:event_tlsnoActionPerformed

    private void tlswithoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tlswithoutActionPerformed
// TODO add your handling code here:
        keyStoreField.setEnabled(true);
        keyPasswordField.setEnabled(true);
        keyStorePasswordField.setEnabled(true);
        truststoreField.setEnabled(true);
        truststorepwField.setEnabled(true);
        nossl2No.setEnabled(true);
        nossl2Yes.setEnabled(true);
        noclientauthNo.setEnabled(true);
        noclientauthYes.setEnabled(true);
        jLabel12.setEnabled(true);
        jLabel13.setEnabled(true);
        jLabel9.setEnabled(true);
        jLabel30.setEnabled(true);
        jLabel10.setEnabled(true);
        jLabel29.setEnabled(true);
        jLabel11.setEnabled(true);
        jLabel33.setEnabled(true);
        jLabel34.setEnabled(true);
    }//GEN-LAST:event_tlswithoutActionPerformed

    private void tlsaesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tlsaesActionPerformed
// TODO add your handling code here:
        keyStoreField.setEnabled(true);
        keyPasswordField.setEnabled(true);
        keyStorePasswordField.setEnabled(true);
        truststoreField.setEnabled(true);
        truststorepwField.setEnabled(true);
        nossl2No.setEnabled(true);
        nossl2Yes.setEnabled(true);
        noclientauthNo.setEnabled(true);
        noclientauthYes.setEnabled(true);
        jLabel12.setEnabled(true);
        jLabel13.setEnabled(true);
        jLabel9.setEnabled(true);
        jLabel30.setEnabled(true);
        jLabel10.setEnabled(true);
        jLabel29.setEnabled(true);
        jLabel11.setEnabled(true);
        jLabel33.setEnabled(true);
        jLabel34.setEnabled(true);        
    }//GEN-LAST:event_tlsaesActionPerformed

    private void tls3desActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tls3desActionPerformed
// TODO add your handling code here:
        keyStoreField.setEnabled(true);
        keyPasswordField.setEnabled(true);
        keyStorePasswordField.setEnabled(true);
        truststoreField.setEnabled(true);
        truststorepwField.setEnabled(true);
        nossl2No.setEnabled(true);
        nossl2Yes.setEnabled(true);
        noclientauthNo.setEnabled(true);
        noclientauthYes.setEnabled(true);
        jLabel12.setEnabled(true);
        jLabel13.setEnabled(true);
        jLabel9.setEnabled(true);
        jLabel30.setEnabled(true);
        jLabel10.setEnabled(true);
        jLabel29.setEnabled(true);
        jLabel11.setEnabled(true);
        jLabel33.setEnabled(true);
        jLabel34.setEnabled(true);        
    }//GEN-LAST:event_tls3desActionPerformed

    private void nativeNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nativeNoActionPerformed
// TODO add your handling code here:
        if(bigendianNo.isSelected()){
            jLabel29.setEnabled(true);
            deftsYes.setEnabled(true);
            deftsNo.setEnabled(true);    
        }
    }//GEN-LAST:event_nativeNoActionPerformed

    private void deftsNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deftsNoActionPerformed
// TODO add your handling code here:
        jLabel31.setEnabled(true);
        nativeYes.setEnabled(true);
        nativeNo.setEnabled(true);
        jLabel28.setEnabled(true);
        bigendianYes.setEnabled(true);
        bigendianNo.setEnabled(true);
        
    }//GEN-LAST:event_deftsNoActionPerformed

    private void bigendianNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bigendianNoActionPerformed
// TODO add your handling code here:
        if(nativeNo.isSelected()){
            jLabel29.setEnabled(true);
            deftsYes.setEnabled(true);
            deftsNo.setEnabled(true); 
        }       
    }//GEN-LAST:event_bigendianNoActionPerformed

    private void bigendianYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bigendianYesActionPerformed
// TODO add your handling code here:
        jLabel29.setEnabled(false);
        deftsYes.setEnabled(false);
        deftsNo.setEnabled(false);
        deftsNo.setSelected(true);             
    }//GEN-LAST:event_bigendianYesActionPerformed

    private void deftsYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deftsYesActionPerformed
// TODO add your handling code here:
        jLabel31.setEnabled(false);
        nativeYes.setEnabled(false);
        nativeNo.setEnabled(false);
        nativeNo.setSelected(true);        
        jLabel28.setEnabled(false);
        bigendianYes.setEnabled(false);
        bigendianNo.setEnabled(false);
        bigendianNo.setSelected(true);                
        
    }//GEN-LAST:event_deftsYesActionPerformed

    private void nativeYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nativeYesActionPerformed
// TODO add your handling code here:
        jLabel29.setEnabled(false);
        deftsYes.setEnabled(false);
        deftsNo.setEnabled(false);
        deftsNo.setSelected(true);           
        
    }//GEN-LAST:event_nativeYesActionPerformed
    
    @Override
    public void updateResponseDropDown()
    {
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();
        
        
        Channel channel = parent.channelEditPanel.currentChannel;
        
        Set<String> variables = new LinkedHashSet<String>();

        variables.add("None");

        List<Step> stepsToCheck = new ArrayList<Step>();
        stepsToCheck.addAll(channel.getSourceConnector().getTransformer().getSteps());

        List<String> scripts = new ArrayList<String>();

        for (Connector connector : channel.getDestinationConnectors()) {
            if (connector.getTransportName().equals("Database Writer"))  {
                if (connector.getProperties().getProperty("useScript").equals(UIConstants.YES_OPTION)) {
                    scripts.add(connector.getProperties().getProperty("script"));
                }
            }
            else if (connector.getTransportName().equals("JavaScript Writer")) {
                scripts.add(connector.getProperties().getProperty("script"));
            }
            variables.add(connector.getName());
            stepsToCheck.addAll(connector.getTransformer().getSteps());
        }

        Pattern pattern = Pattern.compile(RESULT_PATTERN);

        int i = 0;
        for (Iterator it = stepsToCheck.iterator(); it.hasNext();)  {
            Step step = (Step) it.next();
            Map data;
            data = (Map) step.getData();

            if (step.getType().equalsIgnoreCase(TransformerPane.JAVASCRIPT_TYPE))  {
                Matcher matcher = pattern.matcher(step.getScript());
                while (matcher.find()) {
                    String key = matcher.group(1);
                    variables.add(key);
                }
            }
            else if (step.getType().equalsIgnoreCase(TransformerPane.MAPPER_TYPE)) {
                if (data.containsKey(UIConstants.IS_GLOBAL)) {
                    if (((String) data.get(UIConstants.IS_GLOBAL)).equalsIgnoreCase(UIConstants.IS_GLOBAL_RESPONSE))
                        variables.add((String) data.get("Variable"));
                }
            }
        }
        scripts.add(channel.getPreprocessingScript());
        scripts.add(channel.getPostprocessingScript());

        for (String script : scripts) {
            if (script != null && script.length() > 0) {
                Matcher matcher = pattern.matcher(script);
                while (matcher.find()) {
                    String key = matcher.group(1);
                    variables.add(key);
                }
            }
        }
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }
        
    private void ackOnNewConnectionNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_ackOnNewConnectionNoActionPerformed
    {// GEN-HEADEREND:event_ackOnNewConnectionNoActionPerformed

    }// GEN-LAST:event_ackOnNewConnectionNoActionPerformed

    private void ackOnNewConnectionYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_ackOnNewConnectionYesActionPerformed
    {// GEN-HEADEREND:event_ackOnNewConnectionYesActionPerformed
     
    }// GEN-LAST:event_ackOnNewConnection   YesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.components.MirthTextField asyncField;
    private com.webreach.mirth.client.ui.components.MirthRadioButton bigendianNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton bigendianYes;
    private com.webreach.mirth.client.ui.components.MirthTextField bufsizeField;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.ButtonGroup buttonGroup7;
    private javax.swing.ButtonGroup buttonGroup8;
    private com.webreach.mirth.client.ui.components.MirthRadioButton deftsNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton deftsYes;
    private com.webreach.mirth.client.ui.components.MirthTextField destField;
    private com.webreach.mirth.client.ui.components.MirthTextField idletoField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel9;
    private com.webreach.mirth.client.ui.components.MirthTextField keyPasswordField;
    private com.webreach.mirth.client.ui.components.MirthTextField keyStoreField;
    private com.webreach.mirth.client.ui.components.MirthTextField keyStorePasswordField;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerAddressField;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerPortField;
    private com.webreach.mirth.client.ui.components.MirthRadioButton nativeNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton nativeYes;
    private com.webreach.mirth.client.ui.components.MirthRadioButton noclientauthNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton noclientauthYes;
    private com.webreach.mirth.client.ui.components.MirthRadioButton nossl2No;
    private com.webreach.mirth.client.ui.components.MirthRadioButton nossl2Yes;
    private com.webreach.mirth.client.ui.components.MirthRadioButton pdv1No;
    private com.webreach.mirth.client.ui.components.MirthRadioButton pdv1Yes;
    private com.webreach.mirth.client.ui.components.MirthTextField rcvpdulenField;
    private com.webreach.mirth.client.ui.components.MirthTextField reaperField;
    private com.webreach.mirth.client.ui.components.MirthTextField releasetoField;
    private com.webreach.mirth.client.ui.components.MirthTextField requesttoField;
    private com.webreach.mirth.client.ui.components.MirthTextField rspdelayField;
    private com.webreach.mirth.client.ui.components.MirthTextField sndpdulenField;
    private com.webreach.mirth.client.ui.components.MirthTextField soclosedelayField;
    private com.webreach.mirth.client.ui.components.MirthTextField sorcvbufField;
    private com.webreach.mirth.client.ui.components.MirthTextField sosndbufField;
    private com.webreach.mirth.client.ui.components.MirthRadioButton tcpdelayNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton tcpdelayYes;
    private com.webreach.mirth.client.ui.components.MirthRadioButton tls3des;
    private com.webreach.mirth.client.ui.components.MirthRadioButton tlsaes;
    private com.webreach.mirth.client.ui.components.MirthRadioButton tlsno;
    private com.webreach.mirth.client.ui.components.MirthRadioButton tlswithout;
    private com.webreach.mirth.client.ui.components.MirthTextField truststoreField;
    private com.webreach.mirth.client.ui.components.MirthTextField truststorepwField;
    // End of variables declaration//GEN-END:variables

}
