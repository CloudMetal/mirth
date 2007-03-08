/* 
 * $Header: /home/projects/mule/scm/mule/providers/file/src/java/org/mule/providers/file/FileMessageDispatcherFactory.java,v 1.3 2005/06/03 01:20:31 gnt Exp $
 * $Revision: 1.3 $
 * $Date: 2005/06/03 01:20:31 $
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
package com.webreach.mirth.server.mule.providers.file;

import org.mule.umo.UMOException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageDispatcherFactory;

/**
 * @author Ross Mason <p/> //TODO document
 */
public class FileMessageDispatcherFactory implements UMOMessageDispatcherFactory
{
    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnectorSessionFactory#create(org.mule.umo.provider.UMOConnector)
     */
    public UMOMessageDispatcher create(UMOConnector connector) throws UMOException
    {
        return new FileMessageDispatcher((FileConnector) connector);
    }

}
