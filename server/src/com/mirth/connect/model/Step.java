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

import com.mirth.connect.util.EqualsUtil;

public class Step implements Serializable {
	private int sequenceNumber;
	private String name;
	private String script;
	private String type;
	private Object data;

	public Object getData() {
		return this.data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScript() {
		return this.script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public int getSequenceNumber() {
		return this.sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		
		if (!(that instanceof Step)) {
			return false;
		}
		
		Step step = (Step) that;
		
		return
			EqualsUtil.areEqual(this.getSequenceNumber(), step.getSequenceNumber()) &&
			EqualsUtil.areEqual(this.getName(), step.getName()) &&
			EqualsUtil.areEqual(this.getScript(), step.getScript()) &&
			EqualsUtil.areEqual(this.getType(), step.getType());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("sequenceNumber=" + getSequenceNumber() + ", ");
		builder.append("name=" + getName() + ", ");
		builder.append("script=" + getScript() + ", ");
		builder.append("type=" + getType() + ", ");
		builder.append("data=" + getData().toString());
		builder.append("]");
		return builder.toString();
	}
}
