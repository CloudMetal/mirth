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

package com.webreach.mirth.connectors.http;

import java.util.Properties;

import com.webreach.mirth.connectors.ConnectorProperties;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

public class HTTPSenderProperties implements ConnectorProperties
{
    public static final String name = "HTTP Sender";
	
    public static final String DATATYPE = "DataType";
    public static final String HTTP_URL = "host";
    public static final String HTTP_METHOD = "method";
    public static final String HTTP_ADDITIONAL_PROPERTIES = "requestVariables";
    public static final String CHANNEL_ID = "replyChannelId";
    public static final String CHANNEL_NAME = "channelName";

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(HTTP_URL, "");
        properties.put(HTTP_METHOD, "post");
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(HTTP_ADDITIONAL_PROPERTIES, serializer.toXML(new Properties()));
        properties.put(CHANNEL_ID, "sink");
        properties.put(CHANNEL_NAME, "None");
        return properties;
    }
}
