package com.webreach.mirth.model;

import com.webreach.mirth.util.EqualsUtil;

public class ExtensionPoint {
	public enum Mode {
		SERVER, CLIENT
	};
	public enum Type{
		SERVER_PLUGIN, SERVER_CONNECTOR_STATUS, CLIENT_PANEL, CLIENT_VOCABULARY, CLIENT_TRANSFORMER_STEP, CLIENT_DASHBOARD
	};
	private String name;
	private Type type;
	private String className;
	private Mode mode;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public Mode getMode() {
		return mode;
	}
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		
		if (!(that instanceof ExtensionPoint)) {
			return false;
		}
		
		ExtensionPoint extensionPoint = (ExtensionPoint) that;
		
		return
			EqualsUtil.areEqual(this.getClassName(), this.getClassName()) &&
            EqualsUtil.areEqual(this.getMode(), extensionPoint.getMode()) &&
            EqualsUtil.areEqual(this.getType(), extensionPoint.getType());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("classname=" + getClassName() + ", ");
		builder.append("mode=" + getMode().toString() + ", ");
        builder.append("type=" + getType() + "]");
		return builder.toString();
	}
	
}
