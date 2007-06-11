/*
 * $Header: /home/projects/mule/scm/mule/providers/file/src/java/org/mule/providers/file/FilenameParser.java,v 1.4 2005/07/19 00:45:01 rossmason Exp $
 * $Revision: 1.4 $
 * $Date: 2005/07/19 00:45:01 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.connectors.file;

import org.mule.umo.provider.UMOMessageAdapter;

/**
 * <code>FilenameParser</code> is a simple expression parser interface for
 * processing filenames
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.4 $
 */
public interface FilenameParser
{
    public String getFilename(UMOMessageAdapter adaptor, String pattern);
}
