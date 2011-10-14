/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.Properties;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("updateSettings")
public class UpdateSettings extends AbstractSettings implements Serializable, Auditable {

    private static final String FIRST_LOGIN = "firstlogin";
    private static final String UPDATES_ENABLED = "update.enabled";
    private static final String STATS_ENABLED = "stats.enabled";
    private static final String UPDATE_URL = "update.url";
    private static final String LAST_STATS_TIME = "stats.time";

    private Boolean firstLogin;
    private Boolean updatesEnabled;
    private Boolean statsEnabled;
    private String updateUrl;
    private Long lastStatsTime;

    public UpdateSettings() {

    }

    public UpdateSettings(Properties properties) {
        setProperties(properties);
    }

    public Properties getProperties() {
        Properties properties = new Properties();

        if (getFirstLogin() != null) {
            properties.put(FIRST_LOGIN, BooleanUtils.toIntegerObject(getFirstLogin()).toString());
        }
        if (getUpdatesEnabled() != null) {
            properties.put(UPDATES_ENABLED, BooleanUtils.toIntegerObject(getUpdatesEnabled()).toString());
        }
        if (getStatsEnabled() != null) {
            properties.put(STATS_ENABLED, BooleanUtils.toIntegerObject(getStatsEnabled()).toString());
        }
        if (getUpdateUrl() != null) {
            properties.put(UPDATE_URL, getUpdateUrl());
        }
        if (getLastStatsTime() != null) {
            properties.put(LAST_STATS_TIME, getLastStatsTime().toString());
        }

        return properties;
    }

    public void setProperties(Properties properties) {
        setFirstLogin(intToBooleanObject((String) properties.get(FIRST_LOGIN)));
        setUpdatesEnabled(intToBooleanObject((String) properties.get(UPDATES_ENABLED)));
        setStatsEnabled(intToBooleanObject((String) properties.get(STATS_ENABLED)));
        setUpdateUrl((String) properties.get(UPDATE_URL));
        setLastStatsTime(toLongObject((String) properties.get(LAST_STATS_TIME)));
    }

    public Boolean getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(Boolean firstLogin) {
        this.firstLogin = firstLogin;
    }

    public Boolean getUpdatesEnabled() {
        return updatesEnabled;
    }

    public void setUpdatesEnabled(Boolean updatesEnabled) {
        this.updatesEnabled = updatesEnabled;
    }

    public Boolean getStatsEnabled() {
        return statsEnabled;
    }

    public void setStatsEnabled(Boolean statsEnabled) {
        this.statsEnabled = statsEnabled;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public Long getLastStatsTime() {
        return lastStatsTime;
    }

    public void setLastStatsTime(Long lastStatsTime) {
        this.lastStatsTime = lastStatsTime;
    }

    @Override
    public String toAuditString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }

}
