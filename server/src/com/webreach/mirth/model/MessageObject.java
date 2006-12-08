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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.webreach.mirth.util.EqualsUtil;

public class MessageObject implements Serializable {
	public enum Protocol {
		HL7, X12, XML
	}

	public enum Status {
		UNKNOWN, RECEIVED, ACCEPTED, REJECTED, TRANSFORMED, ERROR, SENT, QUEUED
	}

	private String id;
	private String channelId;
	private String source;
	private String type;
	private Status status;
	private Calendar dateCreated;
	private String rawData;
	private Protocol rawDataProtocol;
	private String transformedData;
	private Protocol transformedDataProtocol;
	private String encodedData;
	private Protocol encodedDataProtocol;
	private Map variableMap;
	private String connectorName;
	private boolean encrypted;
	private String errors;
	private String version;

	public MessageObject() {
		this.variableMap = new HashMap();
		this.status = Status.UNKNOWN;
	}

	public String getSource() {
		return this.source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getChannelId() {
		return this.channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public Status getStatus() {
		return this.status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Calendar getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Calendar dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getEncodedData() {
		return this.encodedData;
	}

	public void setEncodedData(String encodedData) {
		this.encodedData = encodedData;
	}

	public Protocol getEncodedDataProtocol() {
		return this.encodedDataProtocol;
	}

	public void setEncodedDataProtocol(Protocol encodedDataProtocol) {
		this.encodedDataProtocol = encodedDataProtocol;
	}

	public String getRawData() {
		return this.rawData;
	}

	public void setRawData(String rawData) {
		this.rawData = rawData;
	}

	public Protocol getRawDataProtocol() {
		return this.rawDataProtocol;
	}

	public void setRawDataProtocol(Protocol rawDataProtocol) {
		this.rawDataProtocol = rawDataProtocol;
	}

	public String getTransformedData() {
		return this.transformedData;
	}

	public void setTransformedData(String transformedData) {
		this.transformedData = transformedData;
	}

	public Protocol getTransformedDataProtocol() {
		return this.transformedDataProtocol;
	}

	public void setTransformedDataProtocol(Protocol transformedDataProtocol) {
		this.transformedDataProtocol = transformedDataProtocol;
	}

	public Map getVariableMap() {
		return this.variableMap;
	}

	public void setVariableMap(Map variableMap) {
		this.variableMap = variableMap;
	}

	public boolean isEncrypted() {
		return this.encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	public String getConnectorName() {
		return this.connectorName;
	}

	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}

	public String getErrors() {
		return this.errors;
	}

	public void setErrors(String errors) {
		this.errors = errors;
	}

	public Object clone() {
		MessageObject messageObject = new MessageObject();
		messageObject.setChannelId(this.getChannelId());
		messageObject.setSource(this.getSource());
		messageObject.setType(this.getType());
		messageObject.setConnectorName(this.getConnectorName());
		messageObject.setDateCreated(this.getDateCreated());
		messageObject.setEncodedData(this.getEncodedData());
		messageObject.setEncodedDataProtocol(this.getEncodedDataProtocol());
		messageObject.setEncrypted(this.isEncrypted());
		messageObject.setErrors(this.getErrors());
		messageObject.setId(this.getId());
		messageObject.setRawData(this.getRawData());
		messageObject.setRawDataProtocol(this.getRawDataProtocol());
		messageObject.setStatus(this.getStatus());
		messageObject.setTransformedData(this.getTransformedData());
		messageObject.setTransformedDataProtocol(this.getTransformedDataProtocol());
		messageObject.setVariableMap(this.getVariableMap());
		messageObject.setVersion(this.getVersion());
		return messageObject;
	}

	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}

		if (!(that instanceof MessageObject)) {
			return false;
		}

		MessageObject messageObject = (MessageObject) that;

		return
			EqualsUtil.areEqual(this.getId(), messageObject.getId()) &&
			EqualsUtil.areEqual(this.getChannelId(), messageObject.getChannelId()) &&
			EqualsUtil.areEqual(this.getSource(), messageObject.getSource()) &&
			EqualsUtil.areEqual(this.getType(), messageObject.getType()) &&
			EqualsUtil.areEqual(this.getStatus(), messageObject.getStatus()) &&
			EqualsUtil.areEqual(this.getDateCreated(), messageObject.getDateCreated()) &&
			EqualsUtil.areEqual(this.getRawData(), messageObject.getRawData()) &&
			EqualsUtil.areEqual(this.getRawDataProtocol(), messageObject.getRawDataProtocol()) &&
			EqualsUtil.areEqual(this.getTransformedData(), messageObject.getTransformedData()) &&
			EqualsUtil.areEqual(this.getTransformedDataProtocol(), messageObject.getTransformedDataProtocol()) &&
			EqualsUtil.areEqual(this.getEncodedData(), messageObject.getEncodedData()) &&
			EqualsUtil.areEqual(this.getEncodedDataProtocol(), messageObject.getEncodedDataProtocol()) &&
			EqualsUtil.areEqual(this.getVariableMap(), messageObject.getVariableMap()) &&
			EqualsUtil.areEqual(this.getConnectorName(), messageObject.getConnectorName()) &&
			EqualsUtil.areEqual(this.isEncrypted(), messageObject.isEncrypted()) &&
			EqualsUtil.areEqual(this.getErrors(), messageObject.getErrors()) &&
			EqualsUtil.areEqual(this.getVersion(), messageObject.getVersion());
	}
}
