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


package com.webreach.mirth.model;

import java.io.Serializable;

import com.webreach.mirth.util.EqualsUtil;

public class PluginMetaData implements MetaData, Serializable {
	private String name;
    private String author;
    private String pluginVersion;
    private String mirthVersion;
    private boolean enabled;
	private String serverClassName = null;
	private String clientClassName = null;

	public String getServerClassName() {
		return this.serverClassName;
	}

	public void setServerClassName(String serverClassName) {
		this.serverClassName = serverClassName;
    }
    
	public String getClientClassName() {
		return clientClassName;
	}

	public void setClientClassName(String clientClassName) {
		this.clientClassName = clientClassName;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
    
    public String getMirthVersion()
    {
        return mirthVersion;
    }
    
    public void setMirthVersion(String mirthVersion)
    {
        this.mirthVersion = mirthVersion;
    }

    public String getPluginVersion()
    {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion)
    {
        this.pluginVersion = pluginVersion;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		
		if (!(that instanceof PluginMetaData)) {
			return false;
		}
		
		PluginMetaData plugin = (PluginMetaData) that;
		
		return
			EqualsUtil.areEqual(this.getName(), plugin.getName()) &&
            EqualsUtil.areEqual(this.getAuthor(), plugin.getAuthor()) &&
            EqualsUtil.areEqual(this.getPluginVersion(), plugin.getPluginVersion()) &&
            EqualsUtil.areEqual(this.getMirthVersion(), plugin.getMirthVersion()) &&
            EqualsUtil.areEqual(this.isEnabled(), plugin.isEnabled()) &&
            EqualsUtil.areEqual(this.getServerClassName(), plugin.getServerClassName()) &&
            EqualsUtil.areEqual(this.getClientClassName(), plugin.getClientClassName());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("name=" + getName() + ", ");
		builder.append("author=" + getAuthor().toString() + ", ");
        builder.append("pluginVersion=" + getPluginVersion() + ", ");
        builder.append("mirthVersion=" + getMirthVersion() + ", ");
        builder.append("enabled=" + isEnabled() + ", ");
		builder.append("serverClassName=" + getServerClassName() + ", ");
        builder.append("clientClassName=" + getClientClassName());
		builder.append("]");
		return builder.toString();
	}

}
