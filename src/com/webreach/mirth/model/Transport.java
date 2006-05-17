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

public class Transport {
	public enum Type {
		LISTENER, SENDER
	};

	private String name;
	private String displayName;
	private String className;
	private String transformers;
	private String protocol;
	private Type type;

	public String getClassName() {
		return this.className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProtocol() {
		return this.protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getTransformers() {
		return this.transformers;
	}

	public void setTransformers(String transformers) {
		this.transformers = transformers;
	}

	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Transport[");
		buffer.append(getName() + ", ");
		buffer.append(getDisplayName() + ", ");
		buffer.append(getType().toString() + ", ");
		buffer.append(getClassName() + ", ");
		buffer.append("'" + getTransformers() + "', ");
		buffer.append(getProtocol());
		buffer.append("]");
		return buffer.toString();
	}

}
