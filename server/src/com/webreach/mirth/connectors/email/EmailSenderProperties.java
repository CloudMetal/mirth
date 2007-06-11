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

package com.webreach.mirth.connectors.email;

import java.util.Properties;

public class EmailSenderProperties
{
    public static final String name = "Email Sender";

    public static final String DATATYPE = "DataType";
    public static final String EMAIL_ADDRESS = "hostname";
    public static final String EMAIL_PORT = "smtpPort";
    public static final String EMAIL_USERNAME = "username";
    public static final String EMAIL_PASSWORD = "password";
    public static final String EMAIL_TO = "toAddresses";
    public static final String EMAIL_FROM = "fromAddress";
    public static final String EMAIL_SUBJECT = "subject";
    public static final String EMAIL_BODY = "body";
    public static final String EMAIL_REPLY_TO = "replyToAddresses"; 

    public static Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(EMAIL_ADDRESS, "");
        properties.put(EMAIL_PORT, "");
        properties.put(EMAIL_USERNAME, "");
        properties.put(EMAIL_PASSWORD, "");
        properties.put(EMAIL_TO, "");
        properties.put(EMAIL_FROM, "");
        properties.put(EMAIL_SUBJECT, "");
        properties.put(EMAIL_BODY, "");
        return properties;
    }
}
