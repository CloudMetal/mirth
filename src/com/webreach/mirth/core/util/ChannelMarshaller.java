package com.webreach.mirth.core.util;

import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.core.Channel;
import com.webreach.mirth.core.Connector;
import com.webreach.mirth.core.Filter;
import com.webreach.mirth.core.Validator;

public class ChannelMarshaller {
	public ChannelMarshaller() {
		
	}
	
	/**
	 * Returns a DOM Document object representation of a given Channel.
	 * 
	 * @param channel
	 * @return
	 */
	public void marshal(Channel channel, OutputStream os) throws MarshalException {
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			
			// channel (root)
			Element channelElement = document.createElement("channel");
			channelElement.setAttribute("name", channel.getName());
			channelElement.setAttribute("description", channel.getDescription());

			// channel.enabled
			if (channel.isEnabled()) {
				channelElement.setAttribute("enabled", "true");	
			} else {
				channelElement.setAttribute("enabled", "false");
			}
			
			channelElement.setAttribute("initial", channel.getInitialStatus().name());
			channelElement.setAttribute("direction", channel.getDirection().name());
			
			// source connector
			Element sourceConnectorElement = marshalConnector(document, "source", channel.getSourceConnector());
			channelElement.appendChild(sourceConnectorElement);
			
			// filter
			Element filterElement = marshalFilter(document, channel.getFilter());
			channelElement.appendChild(filterElement);

			// validator
			Element validatorElement = marshalValidator(document, channel.getValidator());
			channelElement.appendChild(validatorElement);

			// destination connector
			Element destinationsElement = document.createElement("destinations");
			
			for (Iterator iter = channel.getDestinationConnectors().iterator(); iter.hasNext();) {
				Connector destinationConnector = (Connector) iter.next();
				Element destinationConnectorElement = marshalConnector(document, "destination", destinationConnector);
				destinationsElement.appendChild(destinationConnectorElement);	
			}
			
			channelElement.appendChild(destinationsElement);
			
			// finalize the document
			document.appendChild(channelElement);
			
			os.write(serialize(document).getBytes());
		} catch (MarshalException e) {
			throw e;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
	
	private Element marshalFilter(Document document, Filter filter) throws MarshalException {
		try {
			Element filterElement = document.createElement("filter");
			filterElement.setTextContent(filter.getScript());
			return filterElement;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}

	private Element marshalValidator(Document document, Validator validator) throws MarshalException {
		try {
			Element validatorElement = document.createElement("validator");
			
			for (Iterator iter = validator.getProfiles().entrySet().iterator(); iter.hasNext();) {
				Entry profile = (Entry) iter.next();
				Element profileElement = document.createElement("profile");
				profileElement.setAttribute("name", profile.getKey().toString());
				profileElement.setTextContent(profile.getValue().toString());
				validatorElement.appendChild(profileElement);
			}
			
			return validatorElement;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}

	private Element marshalConnector(Document document, String elementName, Connector connector) throws MarshalException {
		try {
			Element connectorElement = document.createElement(elementName);
			connectorElement.setAttribute("name", connector.getName());
			connectorElement.setAttribute("transport", connector.getTransport());
			
			// properties
			for (Iterator iter = connector.getProperties().entrySet().iterator(); iter.hasNext();) {
				Entry property = (Entry) iter.next();
				Element propertyElement = document.createElement("property");
				propertyElement.setAttribute("name", property.getKey().toString());
				propertyElement.setAttribute("value", property.getValue().toString());
				connectorElement.appendChild(propertyElement);
			}
			
			// transformer
			Element transformer = document.createElement("transformer");
			transformer.setAttribute("type", connector.getTransformer().getType().toString());
			
			// transformer.variables
			for (Iterator iter = connector.getTransformer().getVariables().entrySet().iterator(); iter.hasNext();) {
				Entry variable = (Entry) iter.next();
				Element scriptElement = document.createElement("variable");
				scriptElement.setAttribute("name", variable.getKey().toString());
				scriptElement.setTextContent(variable.getValue().toString());
				transformer.appendChild(scriptElement);
			}
			
			connectorElement.appendChild(transformer);
			
			return connectorElement;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}

	/**
	 * Returns a String representation of a channel Document.
	 * 
	 * @param document
	 * @return
	 */
	private String serialize(Document document) {
		String[] dataElements = { "filter", "variable", "profile" };

		OutputFormat of = new OutputFormat(document);
		of.setCDataElements(dataElements);
		of.setOmitXMLDeclaration(true);
		of.setIndenting(true);
		of.setLineSeparator("\n");

		StringWriter stringWriter = new StringWriter();
		XMLSerializer serializer = new XMLSerializer(stringWriter, of);

		try {
			serializer.serialize(document);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return stringWriter.toString();
	}

}
