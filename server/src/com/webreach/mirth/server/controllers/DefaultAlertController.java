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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.velocity.tools.generic.DateTool;
import org.mule.providers.TemplateValueReplacer;

import com.webreach.mirth.model.Alert;
import com.webreach.mirth.server.builders.ErrorMessageBuilder;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.server.util.SMTPConnection;
import com.webreach.mirth.server.util.SMTPConnectionFactory;
import com.webreach.mirth.server.util.SqlConfig;
import com.webreach.mirth.util.PropertyLoader;

public class DefaultAlertController extends AlertController {
    private Logger logger = Logger.getLogger(this.getClass());
    private ChannelStatisticsController statisticsController = ControllerFactory.getFactory().createChannelStatisticsController();
    private ErrorMessageBuilder errorBuilder = new ErrorMessageBuilder();

    private static DefaultAlertController instance = null;

    private DefaultAlertController() {

    }

    public static AlertController create() {
        synchronized (DefaultAlertController.class) {
            if (instance == null) {
                instance = new DefaultAlertController();
            }

            return instance;
        }
    }

    public List<Alert> getAlert(Alert alert) throws ControllerException {
        logger.debug("getting alert: " + alert);

        try {
            List<Alert> alerts = SqlConfig.getSqlMapClient().queryForList("Alert.getAlert", alert);

            for (Alert currentAlert : alerts) {
                List<String> channelIds = SqlConfig.getSqlMapClient().queryForList("Alert.getChannelIdsByAlertId", currentAlert.getId());
                currentAlert.setChannels(channelIds);

                List<String> emails = SqlConfig.getSqlMapClient().queryForList("Alert.getEmailsByAlertId", currentAlert.getId());
                currentAlert.setEmails(emails);
            }

            return alerts;
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public List<Alert> getAlertByChannelId(String channelId) throws ControllerException {
        logger.debug("getting alert by channel id: " + channelId);

        try {
            List<Alert> alerts = SqlConfig.getSqlMapClient().queryForList("Alert.getAlertByChannelId", channelId);

            for (Alert currentAlert : alerts) {
                List<String> channelIds = SqlConfig.getSqlMapClient().queryForList("Alert.getChannelIdsByAlertId", currentAlert.getId());
                currentAlert.setChannels(channelIds);

                List<String> emails = SqlConfig.getSqlMapClient().queryForList("Alert.getEmailsByAlertId", currentAlert.getId());
                currentAlert.setEmails(emails);
            }

            return alerts;
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public void updateAlerts(List<Alert> alerts) throws ControllerException {
        // remove all alerts
        removeAlert(null);

        for (Alert alert : alerts) {
            insertAlert(alert);
        }
    }

    private void insertAlert(Alert alert) throws ControllerException {
        try {
            Alert alertFilter = new Alert();
            alertFilter.setId(alert.getId());

            try {
                SqlConfig.getSqlMapClient().startTransaction();

                logger.debug("adding alert: " + alert);
                SqlConfig.getSqlMapClient().insert("Alert.insertAlert", alert);

                logger.debug("adding channel alerts");

                for (String channelId : alert.getChannels()) {
                    Map params = new HashMap();
                    params.put("alertId", alert.getId());
                    params.put("channelId", channelId);
                    SqlConfig.getSqlMapClient().insert("Alert.insertChannelAlert", params);
                }

                logger.debug("adding alert emails");

                for (String email : alert.getEmails()) {
                    Map params = new HashMap();
                    params.put("alertId", alert.getId());
                    params.put("email", email);
                    SqlConfig.getSqlMapClient().insert("Alert.insertAlertEmail", params);
                }

                SqlConfig.getSqlMapClient().commitTransaction();
            } finally {
                SqlConfig.getSqlMapClient().endTransaction();
            }
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public void removeAlert(Alert alert) throws ControllerException {
        logger.debug("removing alert: " + alert);

        try {
            SqlConfig.getSqlMapClient().delete("Alert.deleteAlert", alert);

            if (DatabaseUtil.statementExists("Alert.vacuumAlertTable")) {
                SqlConfig.getSqlMapClient().update("Alert.vacuumAlertTable");
            }

        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public void sendAlerts(String channelId, String errorType, String customMessage, Throwable e) {
        String errorMessage = errorBuilder.buildErrorMessage(errorType, customMessage, e);

        try {
            for (Alert alert : getAlertByChannelId(channelId)) {
                if (alert.isEnabled() && isAlertableError(alert.getExpression(), errorMessage)) {
                    statisticsController.incrementAlertedCount(channelId);
                    sendAlertEmails(alert.getSubject(), alert.getEmails(), alert.getTemplate(), errorMessage, channelId);
                }
            }
        } catch (ControllerException ce) {
            logger.error(ce);
        }
    }

    private boolean isAlertableError(String expression, String errorMessage) {
        if ((expression != null) && (expression.length() > 0)) {
            return Pattern.compile(expression).matcher(errorMessage).find();
        } else {
            return false;
        }
    }

    private void sendAlertEmails(String subject, List<String> emails, String template, String errorMessage, String channelId) throws ControllerException {
        TemplateValueReplacer replacer = new TemplateValueReplacer();
        Properties properties = ControllerFactory.getFactory().createConfigurationController().getServerProperties();
        final String fromAddress = PropertyLoader.getProperty(properties, "smtp.from");
        final String toAddresses = generateEmailList(emails);
        String body = errorMessage;
        
        if (subject != null) {
            subject = replacer.replaceValues(subject);
        }
        
        if ((subject == null) || (subject.length() == 0)) {
            subject = "Mirth Connect Alert";
        }

        if (template != null) {
            Map<String, Object> context = new HashMap<String, Object>();
            context.put("channelName", ControllerFactory.getFactory().createChannelController().getChannelName(channelId));
            context.put("ERROR", errorMessage);
            context.put("error", errorMessage);
            context.put("SYSTIME", String.valueOf(System.currentTimeMillis()));
            context.put("date", new DateTool());
            body = replacer.replaceValues(template, context);
        }

        final String finalSubject = subject;
        final String finalBody = body;

        new Thread(new Runnable() {
            public void run() {
                try {
                    SMTPConnection connection = SMTPConnectionFactory.createSMTPConnection();
                    connection.send(toAddresses, null, fromAddress, finalSubject, finalBody);
                } catch (Exception e) {
                    logger.error("Could not send alert email.", e);
                }
            }
        }).start();
    }

    private String generateEmailList(List<String> emails) {
        StringBuilder builder = new StringBuilder();

        for (String email : emails) {
            builder.append(email + ",");
        }

        return builder.toString();
    }
}
