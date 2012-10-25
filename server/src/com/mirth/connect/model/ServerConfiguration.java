/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("serverConfiguration")
public class ServerConfiguration implements Serializable {
    private String date;
    private List<Channel> channels = null;
    private List<User> users = null;
    private List<Alert> alerts = null;
    private List<CodeTemplate> codeTemplates = null;
    private ServerSettings serverSettings = null;
    private UpdateSettings updateSettings = null;
    private Map<String, String> globalScripts = null;
    private Map<String, Properties> pluginProperties = null;

    public List<Alert> getAlerts() {
        return this.alerts;
    }

    public void setAlerts(List<Alert> alerts) {
        this.alerts = alerts;
    }

    public List<Channel> getChannels() {
        return this.channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public ServerSettings getServerSettings() {
        return serverSettings;
    }

    public void setServerSettings(ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
    }

    public UpdateSettings getUpdateSettings() {
        return updateSettings;
    }

    public void setUpdateSettings(UpdateSettings updateSettings) {
        this.updateSettings = updateSettings;
    }

    public List<User> getUsers() {
        return this.users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<CodeTemplate> getCodeTemplates() {
        return this.codeTemplates;
    }

    public void setCodeTemplates(List<CodeTemplate> codeTemplates) {
        this.codeTemplates = codeTemplates;
    }

    public Map<String, String> getGlobalScripts() {
        return globalScripts;
    }

    public void setGlobalScripts(Map<String, String> globalScripts) {
        this.globalScripts = globalScripts;
    }

    public Map<String, Properties> getPluginProperties() {
        return pluginProperties;
    }

    public void setPluginProperties(Map<String, Properties> pluginProperties) {
        this.pluginProperties = pluginProperties;
    }
}
