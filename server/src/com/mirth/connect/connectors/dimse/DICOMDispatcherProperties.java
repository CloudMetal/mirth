/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.dimse;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorPropertiesInterface;

public class DICOMDispatcherProperties extends ConnectorProperties implements QueueConnectorPropertiesInterface {
    
    private QueueConnectorProperties queueConnectorProperties;
    
    private String host;
    private String port;
    private String applicationEntity;
    private String localHost;
    private String localPort;
    private String localApplicationEntity;
    private String template;
    private String acceptTo;
    private String async;
    private String bufSize;
    private String connectTo;
    private String priority;
    private String passcode;
    private boolean pdv1;
    private String rcvpdulen;
    private String reaper;
    private String releaseTo;
    private String rspTo;
    private String shutdownDelay;
    private String sndpdulen;
    private String soCloseDelay;
    private String sorcvbuf;
    private String sosndbuf;
    private boolean stgcmt;
    private boolean tcpDelay;
    private boolean ts1;
    private boolean uidnegrsp;
    private String username;

    private String keyPW;
    private String keyStore;
    private String keyStorePW;
    private boolean noClientAuth;
    private boolean nossl2;
    private String tls;
    private String trustStore;
    private String trustStorePW;
    
    public DICOMDispatcherProperties() {
        queueConnectorProperties = new QueueConnectorProperties();

        host = "127.0.0.1";
        port = "104";
        template = "${DICOMMESSAGE}";
        acceptTo = "5000";
        async = "0";
        bufSize = "1";
        connectTo = "0";
        priority = "med";
        passcode = "";
        pdv1 = false;
        rcvpdulen = "16";
        reaper = "10";
        releaseTo = "5";
        rspTo = "60";
        shutdownDelay = "1000";
        sndpdulen = "16";
        soCloseDelay = "50";        
        sorcvbuf = "0";
        sosndbuf = "0";
        stgcmt = false;
        tcpDelay = true;
        ts1 = false;
        uidnegrsp = false;
        username = "";
        applicationEntity = "";
        
        keyPW = "";
        keyStore = "";
        keyStorePW = "";
        noClientAuth = true;
        nossl2 = true;
        tls = "notls";
        trustStore = "";
        trustStorePW = "";
        
        localHost = "";
        localPort = "";
        localApplicationEntity = "";
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getApplicationEntity() {
        return applicationEntity;
    }

    public void setApplicationEntity(String applicationEntity) {
        this.applicationEntity = applicationEntity;
    }

    public String getLocalHost() {
        return localHost;
    }

    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }

    public String getLocalPort() {
        return localPort;
    }

    public void setLocalPort(String localPort) {
        this.localPort = localPort;
    }

    public String getLocalApplicationEntity() {
        return localApplicationEntity;
    }

    public void setLocalApplicationEntity(String localApplicationEntity) {
        this.localApplicationEntity = localApplicationEntity;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getAcceptTo() {
        return acceptTo;
    }

    public void setAcceptTo(String acceptTo) {
        this.acceptTo = acceptTo;
    }

    public String getAsync() {
        return async;
    }

    public void setAsync(String async) {
        this.async = async;
    }

    public String getBufSize() {
        return bufSize;
    }

    public void setBufSize(String bufSize) {
        this.bufSize = bufSize;
    }

    public String getConnectTo() {
        return connectTo;
    }

    public void setConnectTo(String connectTo) {
        this.connectTo = connectTo;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public boolean isPdv1() {
        return pdv1;
    }

    public void setPdv1(boolean pdv1) {
        this.pdv1 = pdv1;
    }

    public String getRcvpdulen() {
        return rcvpdulen;
    }

    public void setRcvpdulen(String rcvpdulen) {
        this.rcvpdulen = rcvpdulen;
    }

    public String getReaper() {
        return reaper;
    }

    public void setReaper(String reaper) {
        this.reaper = reaper;
    }

    public String getReleaseTo() {
        return releaseTo;
    }

    public void setReleaseTo(String releaseTo) {
        this.releaseTo = releaseTo;
    }

    public String getRspTo() {
        return rspTo;
    }

    public void setRspTo(String rspTo) {
        this.rspTo = rspTo;
    }

    public String getShutdownDelay() {
        return shutdownDelay;
    }

    public void setShutdownDelay(String shutdownDelay) {
        this.shutdownDelay = shutdownDelay;
    }

    public String getSndpdulen() {
        return sndpdulen;
    }

    public void setSndpdulen(String sndpdulen) {
        this.sndpdulen = sndpdulen;
    }

    public String getSoCloseDelay() {
        return soCloseDelay;
    }

    public void setSoCloseDelay(String soCloseDelay) {
        this.soCloseDelay = soCloseDelay;
    }

    public String getSorcvbuf() {
        return sorcvbuf;
    }

    public void setSorcvbuf(String sorcvbuf) {
        this.sorcvbuf = sorcvbuf;
    }

    public String getSosndbuf() {
        return sosndbuf;
    }

    public void setSosndbuf(String sosndbuf) {
        this.sosndbuf = sosndbuf;
    }

    public boolean isStgcmt() {
        return stgcmt;
    }

    public void setStgcmt(boolean stgcmt) {
        this.stgcmt = stgcmt;
    }

    public boolean isTcpDelay() {
        return tcpDelay;
    }

    public void setTcpDelay(boolean tcpDelay) {
        this.tcpDelay = tcpDelay;
    }

    public boolean isTs1() {
        return ts1;
    }

    public void setTs1(boolean ts1) {
        this.ts1 = ts1;
    }

    public boolean isUidnegrsp() {
        return uidnegrsp;
    }

    public void setUidnegrsp(boolean uidnegrsp) {
        this.uidnegrsp = uidnegrsp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKeyPW() {
        return keyPW;
    }

    public void setKeyPW(String keyPW) {
        this.keyPW = keyPW;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getKeyStorePW() {
        return keyStorePW;
    }

    public void setKeyStorePW(String keyStorePW) {
        this.keyStorePW = keyStorePW;
    }

    public boolean isNoClientAuth() {
        return noClientAuth;
    }

    public void setNoClientAuth(boolean noClientAuth) {
        this.noClientAuth = noClientAuth;
    }

    public boolean isNossl2() {
        return nossl2;
    }

    public void setNossl2(boolean nossl2) {
        this.nossl2 = nossl2;
    }

    public String getTls() {
        return tls;
    }

    public void setTls(String tls) {
        this.tls = tls;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStorePW() {
        return trustStorePW;
    }

    public void setTrustStorePW(String trustStorePW) {
        this.trustStorePW = trustStorePW;
    }

    public void setQueueConnectorProperties(QueueConnectorProperties queueConnectorProperties) {
        this.queueConnectorProperties = queueConnectorProperties;
    }

    @Override
    public QueueConnectorProperties getQueueConnectorProperties() {
        return queueConnectorProperties;
    }

    @Override
    public String getProtocol() {
        return "DICOM";
    }

    @Override
    public String getName() {
        return "DICOM Sender";
    }

    @Override
    public String toFormattedString() {
        // TODO Auto-generated method stub
        return null;
    }

}
