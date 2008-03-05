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
import java.util.List;
import java.util.ListIterator;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicitCollection;
import com.webreach.mirth.util.EqualsUtil;

@XStreamAlias("pluginMetaData")
@XStreamImplicitCollection(value = "extensionPoints", item = "extensionPoint")
public class PluginMetaData extends MetaData implements Serializable {

	private List<ExtensionPoint> extensionPoints;

	public List<ExtensionPoint> getExtensionPoints() {
		return extensionPoints;
	}

	public void setExtensionPoints(List<ExtensionPoint> plugins) {
		this.extensionPoints = plugins;
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
			EqualsUtil.areEqual(this.getExtensionPoints(), plugin.getExtensionPoints()) &&
			EqualsUtil.areEqual(this.getUpdateUrl(), plugin.getUpdateUrl()) &&
			EqualsUtil.areEqual(this.getDescription(), plugin.getDescription());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("name=" + getName() + ", ");
		builder.append("author=" + getAuthor().toString() + ", ");
		builder.append("pluginVersion=" + getPluginVersion() + ", ");
		builder.append("mirthVersion=" + getMirthVersion() + ", ");
		
		for (ListIterator iter = extensionPoints.listIterator(); iter.hasNext();) {
			ExtensionPoint extensionPoint = (ExtensionPoint) iter.next();
			builder.append("extention-point" + iter.nextIndex() + "=" + extensionPoint.toString() + ", ");
		}
		
		builder.append("updateUrl=" + getUpdateUrl() + ", ");
		builder.append("description=" + getDescription() + ", ");
		builder.append("enabled=" + isEnabled() + "]");
		return builder.toString();
	}
}