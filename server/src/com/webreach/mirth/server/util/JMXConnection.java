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

import java.util.Hashtable;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;

/**
 * A JMXConnection provides a connection to the Mule MBean server.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 * 
 */
public class JMXConnection {
	private Logger logger = Logger.getLogger(this.getClass());
	private JMXConnector jmxConnector;
	private MBeanServerConnection jmxConnection;
	private String domain;

	public JMXConnection(String address, String domain) throws Exception {
		this.domain = domain;
		JMXServiceURL url = new JMXServiceURL(address);
		jmxConnector = JMXConnectorFactory.connect(url);
		jmxConnection = jmxConnector.getMBeanServerConnection();
	}

	/**
	 * Invokes an operation on the MBean with the specified properties.
	 * 
	 * @param propertiesTable
	 *            the MBean name/value properties.
	 * @param operationName
	 *            the name of the operation.
	 * @return the result of the operation.
	 */
	public Object invokeOperation(Hashtable properties, String operation, Object[] params, String[] signature) throws Exception {
		logger.debug("invoking mbean operation: " + operation);
		return jmxConnection.invoke(new ObjectName(domain, properties), operation, params, signature);
	}

	/**
	 * Returns specified attribute from the MBean with the specified properties.
	 * 
	 * @param properties
	 * @param attribute
	 * @return
	 */
	public Object getAttribute(Hashtable properties, String attribute) throws Exception {
		logger.debug("getting mbean attribute: " + attribute);
		return jmxConnection.getAttribute(new ObjectName(domain, properties), attribute);
	}

	public Set getMBeanNames() throws Exception {
		logger.debug("getting mbean names");
		return jmxConnection.queryNames(null, null);
	}
	
	public void close() {
		try {
			if (jmxConnector != null) {
				jmxConnector.close();
			} else {
				logger.warn("could not close jmx connection");
			}
		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
	}
}
