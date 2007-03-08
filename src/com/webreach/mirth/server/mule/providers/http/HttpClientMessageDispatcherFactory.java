/* 
 * $Header: /home/projects/mule/scm/mule/providers/http/src/java/org/mule/providers/http/HttpClientMessageDispatcherFactory.java,v 1.3 2005/06/03 01:20:33 gnt Exp $
 * $Revision: 1.3 $
 * $Date: 2005/06/03 01:20:33 $
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */
package com.webreach.mirth.server.mule.providers.http;

import org.mule.umo.UMOException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageDispatcherFactory;

/**
 * Creates a HttpClientMessageAdapter to make client requests
 * 
 * @author Ross Mason
 */
public class HttpClientMessageDispatcherFactory implements UMOMessageDispatcherFactory
{
    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnectorSessionFactory#create(org.mule.umo.provider.UMOConnector)
     */
    public UMOMessageDispatcher create(UMOConnector connector) throws UMOException
    {
        return new HttpClientMessageDispatcher((HttpConnector) connector);
    }
}
