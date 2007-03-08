/*
 * $Header: /home/projects/mule/scm/mule/providers/email/src/java/org/mule/providers/email/MailUtils.java,v 1.3 2005/10/10 14:00:15 rossmason Exp $
 * $Revision: 1.3 $
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.config.i18n.Messages;
import org.mule.util.Utility;
import org.mule.umo.provider.UMOConnector;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.net.InetAddress;
import java.util.Properties;

/**
 * Contains javax.mail.Session helpers.
 * 
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.3 $
 */
public class MailUtils
{
    /**
     * The logger used for this class
     */
    protected final static transient Log logger = LogFactory.getLog(MailUtils.class);


    /**
     * Creates a new Mail session based on a Url.  this method will also add an Smtp Authenticator
     * if a password is set on the URL
     * @param url
     * @return
     */
    public static Session createMailSession(URLName url, MailConnector connector)
    {
        if (url == null) {
            throw new IllegalArgumentException(new org.mule.config.i18n.Message(Messages.X_IS_NULL, "URL").toString());
        }
        String protocol = connector.getProtocol().toLowerCase();
        boolean secure = false;
        if(protocol.equals("smtps")) {
            protocol = "smtp";
            secure=true;
        } else if(protocol.equals("pop3s")) {
            protocol = "pop3";
            secure = true;
        } else if(protocol.equals("imaps")) {
            protocol = "imap";
            secure = true;
        }

        Properties props = System.getProperties();
        props.put("mail." + protocol +".host", url.getHost());
        int port = url.getPort();
        if(port==-1) {
            port = connector.getPort();
        }
        props.put("mail." + protocol + ".port", String.valueOf(port));

        if(secure) {
            System.setProperty("mail." + protocol + ".socketFactory.port", String.valueOf(port));

        }
        Session session;
        if (url.getPassword() != null) {
            props.put("mail." + protocol + ".auth", "true");
            Authenticator auth = connector.getAuthenticator();
            if(auth==null) {
                auth = new DefaultAuthenticator(url.getUsername(), url.getPassword());
                logger.debug("No Authenticator set on Connector: " + connector.getName() + ". Using default.");
            }
            session = Session.getInstance(props, auth);
        } else {
            session = Session.getDefaultInstance(props, null);
        }
        return session;
    }

    public static String internetAddressesToString(InternetAddress[] addresses) {
        if(addresses==null || addresses.length==0) return Utility.EMPTY_STRING;
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < addresses.length; i++) {
            InternetAddress address = addresses[i];
            buf.append(address.getAddress()).append(", ");
        }
        String result = buf.toString();
        if(result.endsWith(", ")) result.substring(0, result.length()-2);
        return result;
    }

    public static String internetAddressesToString(InternetAddress address) {
        return internetAddressesToString(new InternetAddress[]{address});
    }

    public static String mailAddressesToString(Address[] addresses) {
        if(addresses==null || addresses.length==0) return Utility.EMPTY_STRING;
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < addresses.length; i++) {
            Address address = addresses[i];
            buf.append(address.toString()).append(", ");
        }
        String result = buf.toString();
        if(result.endsWith(", ")) result.substring(0, result.length()-2);
        return result;
    }

    public static String mailAddressesToString(Address address) {
        return mailAddressesToString(new Address[]{address});
    }

    public static InternetAddress[] StringToInternetAddresses(String address) throws AddressException
    {
        InternetAddress[] inetaddresses = null;
        if (!(address == null || "".equals(address))) {
            inetaddresses = InternetAddress.parse(address, false);
        } else {
            throw new NullPointerException(new org.mule.config.i18n.Message(Messages.X_IS_NULL, "Email address").toString());
        }
        return inetaddresses;
    }

    /**
     * DefaultAuthenticator is used to do simple authentication when the SMTP
     * server requires it.
     */
    private static class DefaultAuthenticator extends javax.mail.Authenticator
    {
        private String username = null;
        private String password = null;

        public DefaultAuthenticator(String user, String pwd) {
            username = user;
            password = pwd;
        }
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }
}
