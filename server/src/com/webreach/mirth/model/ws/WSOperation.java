package com.webreach.mirth.model.ws;

import java.util.ArrayList;
import java.util.List;

public class WSOperation {
	private String name;
	private List<WSParameter> parameters;
	private String soapActionURI;
	private String namespace;

	public WSOperation() {
		this.parameters = new ArrayList<WSParameter>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<WSParameter> getParameters() {
		return this.parameters;
	}

	public void setParameters(List<WSParameter> parameters) {
		this.parameters = parameters;
	}

	public String getSoapActionURI() {
		return soapActionURI;
	}

	public void setSoapActionURI(String soapActionURI) {
		this.soapActionURI = soapActionURI;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
}
