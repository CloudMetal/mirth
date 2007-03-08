/*
 * $Header: /home/projects/mule/scm/mule/providers/email/src/java/org/mule/providers/email/Pop3sConnector.java,v 1.1 2005/10/10 14:00:15 rossmason Exp $
 * $Revision: 1.1 $
 * $Date: 2005/10/10 14:00:15 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.server.mule.providers.email;

import org.mule.umo.lifecycle.InitialisationException;

/**
 * Creates a Pop3s secured connection to a pop3 mailbox
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.1 $
 */
public class Pop3sConnector extends Pop3Connector {

    public static final String DEFAULT_SOCKET_FACTORY = "javax.net.ssl.SSLSocketFactory";

    private String socketFactory = DEFAULT_SOCKET_FACTORY;
    private String socketFactoryFallback = "false";
    private String trustStore = null;
    private String trustStorePassword = null;

    public static final int DEFAULT_POP3S_PORT = 995;

    public Pop3sConnector() {
        setPort(DEFAULT_POP3S_PORT);
    }

    public String getProtocol()
    {
        return "pop3s";
    }

    public void doInitialise() throws InitialisationException {
        super.doInitialise();
        System.setProperty("mail." + getProtocol() + ".ssl", "true");
        System.setProperty("mail." + getProtocol() + ".socketFactory.class", getSocketFactory());
        System.setProperty("mail." + getProtocol() + ".socketFactory.fallback", getSocketFactoryFallback());

        if(getTrustStore()!=null) {
            System.setProperty("javax.net.ssl.trustStore", getTrustStore());
            if(getTrustStorePassword()!=null) {
                System.setProperty("javax.net.ssl.trustStorePassword", getTrustStorePassword());
            }
        }
    }

    public String getSocketFactory() {
        return socketFactory;
    }

    public void setSocketFactory(String sslSocketFactory) {
        this.socketFactory = sslSocketFactory;
    }

    public String getSocketFactoryFallback() {
        return socketFactoryFallback;
    }

    public void setSocketFactoryFallback(String socketFactoryFallback) {
        this.socketFactoryFallback = socketFactoryFallback;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }
}
