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

package com.webreach.mirth.server.controllers;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.webreach.mirth.model.Alert;
import com.webreach.mirth.server.util.SqlConfig;

public class AlertController {
	private Logger logger = Logger.getLogger(this.getClass());
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();

	public List<Alert> getAlert(Alert alert) throws ControllerException {
		logger.debug("getting alert: " + alert);

		try {
			List<Alert> alerts = sqlMap.queryForList("getAlert", alert);

			for (Iterator iter = alerts.iterator(); iter.hasNext();) {
				Alert currentAlert = (Alert) iter.next();

				List<String> channelIds = sqlMap.queryForList("getChannelIdsByAlertId", currentAlert.getId());
				currentAlert.setChannels(channelIds);

				List<String> emails = sqlMap.queryForList("getEmailsByAlertId", currentAlert.getId());
				currentAlert.setEmails(emails);
			}

			return alerts;
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public void updateAlert(Alert alert) throws ControllerException {
		try {
			Alert alertFilter = new Alert();
			alertFilter.setId(alert.getId());
			
			if (getAlert(alertFilter).isEmpty()) {
				try {
					sqlMap.startTransaction();

					// insert the alert and its properties
					logger.debug("adding alert: " + alert);
					
					System.out.println(alert.getId() + " " + alert.getName());
					
					sqlMap.insert("insertAlert", alert);
					
					// insert the channel ID list
					logger.debug("adding channel alerts");

					List<String> channelIds = alert.getChannels();

					for (Iterator iter = channelIds.iterator(); iter.hasNext();) {
						String channelId = (String) iter.next();
						Map params = new HashMap();
						params.put("alertId", alert.getId());
						params.put("channelId", channelId);
						System.out.println(params);
						sqlMap.insert("insertChannelAlert", params);
					}

					// insert the email address list
					logger.debug("adding alert emails");

					List<String> emails = alert.getEmails();

					for (Iterator iter = emails.iterator(); iter.hasNext();) {
						String email = (String) iter.next();
						Map params = new HashMap();
						params.put("alertId", alert.getId());
						params.put("email", email);
						System.out.println(params);
						sqlMap.insert("insertAlertEmail", params);
					}

					sqlMap.commitTransaction();
				} finally {
					sqlMap.endTransaction();
				}
			} else {
				logger.debug("updating alert: " + alert);
//				removeAlert(alert);
//				updateAlert(alert);
			}
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public void removeAlert(Alert alert) throws ControllerException {
		logger.debug("removing alert: " + alert);

		try {
			sqlMap.delete("deleteAlert", alert);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}
}
