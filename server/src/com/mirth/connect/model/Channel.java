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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A Channel is the main element of the Mirth architecture. Channels connect a
 * single source with multiple destinations which are represented by Connectors.
 * 
 */

@XStreamAlias("channel")
public class Channel implements Serializable {
	private String id;
	private String name;
	private String description;
	private boolean enabled;
	private String version;
	private Calendar lastModified;
	private int revision;
	private Connector sourceConnector;
	private List<Connector> destinationConnectors = new ArrayList<Connector>();
	private Properties properties = new Properties();
	private String preprocessingScript;
    private String postprocessingScript;
    private String deployScript;
    private String shutdownScript;

	public Channel() {

	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getRevision() {
		return this.revision;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Connector getSourceConnector() {
		return this.sourceConnector;
	}

	public void setSourceConnector(Connector sourceConnector) {
		this.sourceConnector = sourceConnector;
	}

	public List<Connector> getDestinationConnectors() {
		return this.destinationConnectors;
	}
	
	public List<Connector> getEnabledDestinationConnectors() {
		List<Connector> enabledConnectors = new ArrayList<Connector>();
		for(Connector connector : getDestinationConnectors()) {
			if(connector.isEnabled()) { 
				enabledConnectors.add(connector);
			}
		}
		return enabledConnectors;
	}

	public void setDestinationConnectors(List<Connector> destinationConnectors) {
		this.destinationConnectors = destinationConnectors;
	}

	public Properties getProperties() {
		return this.properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
    
    public String getPostprocessingScript() {
        return postprocessingScript;
    }

    public void setPostprocessingScript(String postprocessingScript) {
        this.postprocessingScript = postprocessingScript;
    }

    public String getPreprocessingScript() {
        return preprocessingScript;
    }

    public void setPreprocessingScript(String preprocessingScript) {
        this.preprocessingScript = preprocessingScript;
    }
	
    public String getDeployScript() {
        return this.deployScript;
    }

    public void setDeployScript(String deployScript) {
        this.deployScript = deployScript;
    }
    
    public String getShutdownScript() {
        return this.shutdownScript;
    }

    public void setShutdownScript(String shutdownScript) {
        this.shutdownScript = shutdownScript;
    }
    
	public Calendar getLastModified() {
		return lastModified;
	}

	public void setLastModified(Calendar lastModified) {
		this.lastModified = lastModified;
	}
    
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		
		if (!(that instanceof Channel)) {
			return false;
		}

		Channel channel = (Channel) that;
			
		return
			ObjectUtils.equals(this.getId(), channel.getId()) &&
			ObjectUtils.equals(this.getName(), channel.getName()) &&
			ObjectUtils.equals(this.getDescription(), channel.getDescription()) &&
			ObjectUtils.equals(this.isEnabled(), channel.isEnabled()) &&
			ObjectUtils.equals(this.getLastModified(), channel.getLastModified()) &&
			ObjectUtils.equals(this.getVersion(), channel.getVersion()) &&
			ObjectUtils.equals(this.getRevision(), channel.getRevision()) &&
			ObjectUtils.equals(this.getSourceConnector(), channel.getSourceConnector()) &&
			ObjectUtils.equals(this.getDestinationConnectors(), channel.getDestinationConnectors()) &&
			ObjectUtils.equals(this.getProperties(), channel.getProperties()) &&
            ObjectUtils.equals(this.getShutdownScript(), channel.getShutdownScript()) &&
            ObjectUtils.equals(this.getDeployScript(), channel.getDeployScript()) &&
            ObjectUtils.equals(this.getPostprocessingScript(), channel.getPostprocessingScript()) &&
			ObjectUtils.equals(this.getPreprocessingScript(), channel.getPreprocessingScript());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("id=" + getId() + ", ");
		builder.append("name=" + getName() + ", ");
		builder.append("enabled=" + isEnabled() + ", ");
		builder.append("version=" + getVersion());
		builder.append("]");
		return builder.toString();
	}
}
