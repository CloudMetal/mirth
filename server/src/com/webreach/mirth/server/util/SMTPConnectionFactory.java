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


package com.webreach.mirth.server.util;

import java.util.Properties;

import com.webreach.mirth.server.controllers.ConfigurationController;

public class SMTPConnectionFactory {
	public static SMTPConnection createSMTPConnection() throws Exception {
		Properties properties = (new ConfigurationController()).getServerProperties();
		String host = properties.getProperty("smtp.host");

		int port = 25;

		if (properties.getProperty("smtp.port") != null && !properties.getProperty("smtp.port").equals("")) {
			port = Integer.valueOf(properties.getProperty("smtp.port")).intValue();
		}

		String username = properties.getProperty("smtp.username");
		String password = properties.getProperty("smtp.password");
		
		return new SMTPConnection(host, port, username, password);
	}
}
