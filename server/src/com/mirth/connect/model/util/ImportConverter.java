/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.MessageObject.Protocol;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class ImportConverter {
    private static ObjectXMLSerializer serializer = new ObjectXMLSerializer();

    private enum Direction {
        INBOUND, OUTBOUND
    }

    /*
     * Method used to convert messages from one version to another.
     */
    public static String convertMessage(String message) throws Exception {
        message = convertPackageNames(message);
        return message;
    }
    
    /**
     * Manually change the any old package XML to the new package
     * @param xml
     * @return updated xml
     */
    private static String convertPackageNames(String xml) {
        return xml.replaceAll("com.webreach.mirth", "com.mirth.connect");
    }

    public static ServerConfiguration convertServerConfiguration(String serverConfiguration) throws Exception {
        serverConfiguration = convertPackageNames(serverConfiguration);
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;
        DocumentBuilder builder;

        builder = factory.newDocumentBuilder();
        document = builder.parse(new InputSource(new StringReader(serverConfiguration)));

        // Remove users from the server configuration file if they were there.
        Element documentElement = document.getDocumentElement();
        NodeList users = documentElement.getElementsByTagName("users");
        if (users != null && users.getLength() > 0) {
            documentElement.removeChild(users.item(0));
        }

        Element channelsRoot = (Element) document.getElementsByTagName("channels").item(0);
        NodeList channels = document.getElementsByTagName("com.mirth.connect.model.Channel");
        List<Channel> channelList = new ArrayList<Channel>();
        int length = channels.getLength();

        for (int i = 0; i < length; i++) {
            // Must get node 0 because the first channel is removed each
            // iteration
            Element channel = (Element) channels.item(0);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter sw = new StringWriter();
            trans.transform(new DOMSource(channel), new StreamResult(sw));
            String channelDocXML = sw.toString();

            channelList.add((Channel) serializer.fromXML(convertChannelString(channelDocXML)));

            channelsRoot.removeChild(channel);
        }

        DocumentSerializer docSerializer = new DocumentSerializer();
        serverConfiguration = docSerializer.toXML(document);
        
        serverConfiguration = convertGlobalScripts(serverConfiguration);
        
        serverConfiguration = convertAlerts(serverConfiguration);
        
        serverConfiguration = convertCodeTemplates(serverConfiguration);
        
        ServerConfiguration config = (ServerConfiguration) serializer.fromXML(serverConfiguration);
        config.setChannels(channelList);
        
        return config;
    }
    
    public static String convertGlobalScripts(String globalScriptsXml) {
        globalScriptsXml = convertPackageNames(globalScriptsXml);
        
        return globalScriptsXml;
    }
    
    public static String convertAlerts(String alertsXml) {
        alertsXml = convertPackageNames(alertsXml);
        
        return alertsXml;
    }

    public static String convertCodeTemplates(String codeTemplatesXML) throws Exception {
        codeTemplatesXML = convertPackageNames(codeTemplatesXML);
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;
        DocumentBuilder builder;

        builder = factory.newDocumentBuilder();
        document = builder.parse(new InputSource(new StringReader(codeTemplatesXML)));

        NodeList codeTemplates = document.getElementsByTagName("com.mirth.connect.model.CodeTemplate");
        int length = codeTemplates.getLength();

        for (int i = 0; i < length; i++) {
            Element codeTemplate = (Element) codeTemplates.item(i);
            NodeList versions = codeTemplate.getElementsByTagName("version");

            // If there is no version, then this is a migration to 2.0 and the
            // scope should be incremented by 1 if its value is not currently 0
            // (global map). Global Channel Map was added in position 1 for 2.0.
            if (versions.getLength() == 0) {
                Element scope = (Element) codeTemplate.getElementsByTagName("scope").item(0);
                int scopeValue = Integer.parseInt(scope.getTextContent());
                if (scopeValue != 0) {
                    scopeValue++;
                    scope.setTextContent(Integer.toString(scopeValue));
                }
            }
        }

        DocumentSerializer docSerializer = new DocumentSerializer();

        return docSerializer.toXML(document);
    }

    public static List<CodeTemplate> convertCodeTemplates(List<CodeTemplate> codeTemplates) throws Exception {
        return (List<CodeTemplate>) serializer.fromXML(convertCodeTemplates(serializer.toXML(codeTemplates)));
    }

    public static Channel convertChannelObject(Channel channel) throws Exception {
        return (Channel) serializer.fromXML(convertChannelString(serializer.toXML(channel)));
    }

    public static String convertChannelFile(File channel) throws Exception {
        return convertChannelString(read(channel));
    }

    public static String convertChannelString(String channel) throws Exception {
        channel = convertPackageNames(channel);
        
        String contents = removeInvalidHexChar(channel);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;
        DocumentBuilder builder;

        builder = factory.newDocumentBuilder();
        document = builder.parse(new InputSource(new StringReader(contents)));

        return convertChannel(document);
    }

    /*
     * Upgrade pre-1.4 channels to work with 1.4+
     */
    public static String convertChannel(Document document) throws Exception {
        String channelXML = "";
        Element channelRoot = document.getDocumentElement();

        String version = channelRoot.getElementsByTagName("version").item(0).getTextContent();

        int majorVersion = Integer.parseInt(version.split("\\.")[0]);
        int minorVersion = Integer.parseInt(version.split("\\.")[1]);
        int patchVersion = Integer.parseInt(version.split("\\.")[2]);

        if (majorVersion < 2) {
            if (minorVersion < 4) {
                Direction direction = null;
                Element sourceConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("sourceConnector").item(0);
                Element destinationConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("destinationConnectors").item(0);
                NodeList destinationsConnectors = destinationConnectorRoot.getElementsByTagName("com.mirth.connect.model.Connector");

                Node channelDirection = channelRoot.getElementsByTagName("direction").item(0);

                if (channelDirection.getTextContent().equals("INBOUND"))
                    direction = Direction.INBOUND;
                else if (channelDirection.getTextContent().equals("OUTBOUND"))
                    direction = Direction.OUTBOUND;

                channelRoot.removeChild(channelDirection);

                NodeList modeElements = channelRoot.getElementsByTagName("mode");

                for (int i = 0; i < modeElements.getLength(); i++) {
                    if (((Element) modeElements.item(i)).getParentNode() == channelRoot) {
                        channelRoot.removeChild(modeElements.item(i));
                    }
                }

                channelRoot.removeChild(channelRoot.getElementsByTagName("protocol").item(0));

                NodeList transportNames = channelRoot.getElementsByTagName("transportName");
                for (int i = 0; i < transportNames.getLength(); i++) {
                    if (transportNames.item(i).getTextContent().equals("PDF Writer")) {
                        transportNames.item(i).setTextContent("Document Writer");
                    }
                }

                NodeList properyNames = channelRoot.getElementsByTagName("property");
                for (int i = 0; i < properyNames.getLength(); i++) {
                    Node nameAttribute = properyNames.item(i).getAttributes().getNamedItem("name");
                    if (properyNames.item(i).getAttributes().getLength() > 0 && nameAttribute != null) {
                        if (nameAttribute.getNodeValue().equals("DataType")) {
                            if (properyNames.item(i).getTextContent().equals("PDF Writer")) {
                                properyNames.item(i).setTextContent("Document Writer");
                            }
                        }
                    }
                }

                Element modeElement = document.createElement("mode");
                modeElement.setTextContent(Connector.Mode.SOURCE.toString());
                sourceConnectorRoot.appendChild(modeElement);

                updateFilterFor1_4((Element) sourceConnectorRoot.getElementsByTagName("filter").item(0));
                if (direction == Direction.OUTBOUND)
                    updateTransformerFor1_4(document, (Element) sourceConnectorRoot.getElementsByTagName("transformer").item(0), Protocol.XML, Protocol.XML);
                else
                    updateTransformerFor1_4(document, (Element) sourceConnectorRoot.getElementsByTagName("transformer").item(0), Protocol.HL7V2, Protocol.HL7V2);

                for (int i = 0; i < destinationsConnectors.getLength(); i++) {
                    modeElement = document.createElement("mode");
                    modeElement.setTextContent(Connector.Mode.DESTINATION.toString());

                    Element destinationsConnector = (Element) destinationsConnectors.item(i);
                    destinationsConnector.appendChild(modeElement);

                    updateFilterFor1_4((Element) destinationsConnector.getElementsByTagName("filter").item(0));

                    if (direction == Direction.OUTBOUND)
                        updateTransformerFor1_4(document, (Element) destinationsConnector.getElementsByTagName("transformer").item(0), Protocol.XML, Protocol.HL7V2);
                    else
                        updateTransformerFor1_4(document, (Element) destinationsConnector.getElementsByTagName("transformer").item(0), Protocol.HL7V2, Protocol.HL7V2);

                }
            }

            if (minorVersion < 5) {
                updateTransformerFor1_5(document);
            }

            if (minorVersion < 6) {
                // Go through each connector and set it to enabled if that
                // property doesn't exist.

                Element sourceConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("sourceConnector").item(0);
                Element destinationConnectorRoot = (Element) document.getDocumentElement().getElementsByTagName("destinationConnectors").item(0);
                NodeList destinationsConnectors = destinationConnectorRoot.getElementsByTagName("com.mirth.connect.model.Connector");

                // Check SOURCE CONNECTOR node for "enabled" element which is
                // added by migration automatically. Add it if not found.
                if (!nodeChildrenContains(sourceConnectorRoot, "enabled")) {
                    Element enabledSource = document.createElement("enabled");
                    enabledSource.setTextContent("true");
                    sourceConnectorRoot.appendChild(enabledSource);
                } else {
                    // set it anyway, in case xstream auto set it to false.
                    setBooleanNode(sourceConnectorRoot, "enabled", true);
                }

                // Check CONNECTOR node for "enabled" element which is added by
                // migration automatically. Add it if not found.
                for (int i = 0; i < destinationsConnectors.getLength(); i++) {
                    Element destinationConnector = (Element) destinationsConnectors.item(i);

                    if (!nodeChildrenContains(destinationConnector, "enabled")) {
                        Element enabledDestination = document.createElement("enabled");
                        enabledDestination.setTextContent("true");
                        destinationConnector.appendChild(enabledDestination);
                    } else {
                        // set it anyway, in case xstream auto set it to false.
                        setBooleanNode(destinationConnector, "enabled", true);
                    }
                }

                if (!nodeChildrenContains(channelRoot, "deployScript")) {
                    Element deployScript = document.createElement("deployScript");
                    deployScript.setTextContent("// This script executes once when the mule engine is started\n// You only have access to the globalMap here to persist data\nreturn;");
                    channelRoot.appendChild(deployScript);
                }

                if (!nodeChildrenContains(channelRoot, "shutdownScript")) {
                    Element shutdownScript = document.createElement("shutdownScript");
                    shutdownScript.setTextContent("// This script executes once when the mule engine is stopped\n// You only have access to the globalMap here to persist data\nreturn;");
                    channelRoot.appendChild(shutdownScript);
                }

                if (!nodeChildrenContains(channelRoot, "postprocessingScript")) {
                    Element postprocessorScript = document.createElement("postprocessingScript");
                    postprocessorScript.setTextContent("// This script executes once after a message has been processed\nreturn;");
                    channelRoot.appendChild(postprocessorScript);
                }
            }

            if (minorVersion < 7) {
                if (!nodeChildrenContains(channelRoot, "lastModified")) {
                    Element lastModified = document.createElement("lastModified");
                    Element time = document.createElement("time");
                    Element timezone = document.createElement("timezone");

                    Calendar calendar = Calendar.getInstance();
                    time.setTextContent(calendar.getTimeInMillis() + "");
                    timezone.setTextContent(calendar.getTimeZone().getDisplayName());

                    lastModified.appendChild(time);
                    lastModified.appendChild(timezone);

                    channelRoot.appendChild(lastModified);
                }

                updateFilterFor1_7(document);
                updateTransformerFor1_7(document);
            }

            if (minorVersion < 8) {
                // Run for all versions prior to 1.7.1
                if (minorVersion < 7 || (minorVersion == 7 && patchVersion < 1)) {
                    updateTransformerFor1_7_1(document);
                }
                convertChannelConnectorsFor1_8(document, channelRoot);
            }

            // Run for all versions prior to 1.8.2
            if (minorVersion < 8 || (minorVersion == 8 && patchVersion < 1)) {
                updateTransformerFor1_8_1(document);
            }

            // Run for all versions prior to 2.0
            convertChannelConnectorsFor2_0(document, channelRoot);
        }

        DocumentSerializer docSerializer = new DocumentSerializer();
        channelXML = docSerializer.toXML(document);

        return updateLocalAndGlobalVariables(channelXML);
    }

    /**
     * Convert the source and destination connectors for the channel from
     * pre-1.8 to 1.8
     */
    public static void convertChannelConnectorsFor1_8(Document document, Element channelRoot) throws Exception {
        Element sourceConnectorRoot = (Element) channelRoot.getElementsByTagName("sourceConnector").item(0);
        Element destinationConnectorRoot = (Element) channelRoot.getElementsByTagName("destinationConnectors").item(0);
        NodeList destinationsConnectors = destinationConnectorRoot.getElementsByTagName("com.mirth.connect.model.Connector");

        // Convert the source connector
        convertOneConnectorFor1_8(document, sourceConnectorRoot);

        // Convert all destination connectors
        for (int i = 0; i < destinationsConnectors.getLength(); i++) {

            Element destinationConnector = (Element) destinationsConnectors.item(i);
            convertOneConnectorFor1_8(document, destinationConnector);
        }
    }

    /**
     * Update the child property elements of a properties element
     * 
     * @param document
     *            The document to use to generate new Elements.
     * @param properties
     *            The properties element to be modified.
     * @param defaultProperties
     *            Properties to be added only if they are missing.
     * @param changeProperties
     *            Properties to be added if missing, or changed if already
     *            present.
     */
    public static void updateProperties(Document document, Element properties, Map<String, String> defaultProperties, Map<String, String> changeProperties) throws Exception {
        // Make a working copy of the properies so we can remove existing
        // properties
        Map<String, String> missingProperties = new HashMap<String, String>();
        missingProperties.putAll(defaultProperties);
        missingProperties.putAll(changeProperties);

        // Removes all existing properties from the working copy, and changes
        // the value of any
        // change properties
        NodeList existingProperties = properties.getElementsByTagName("property");
        for (int i = 0; i < existingProperties.getLength(); i++) {

            Node existingProperty = existingProperties.item(i);
            Node existingPropertyNameAttribute = existingProperty.getAttributes().getNamedItem("name");
            String existingPropertyName = existingPropertyNameAttribute.getNodeValue();
            if (missingProperties.containsKey(existingPropertyName)) {
                if (changeProperties.containsKey(existingPropertyName)) {
                    existingProperty.setTextContent(changeProperties.get(existingPropertyName));
                }
                missingProperties.remove(existingPropertyName);
            }
        }

        // And any remaining default or change properties
        for (Map.Entry<String, String> thisEntry : missingProperties.entrySet()) {
            Element newProperty = document.createElement("property");
            newProperty.setAttribute("name", thisEntry.getKey());
            newProperty.setTextContent(thisEntry.getValue());
            properties.appendChild(newProperty);
        }
    }

    /** Get the child transport node of a connector */
    public static Node getConnectorTransportNode(Element connectorRoot) throws Exception {

        // There will be exactly one.
        NodeList transportNames = connectorRoot.getElementsByTagName("transportName");

        return transportNames.item(0);
    }

    /** Get the child properties element of a connector */
    public static Element getPropertiesElement(Element connectorRoot) throws Exception {
        NodeList propertiesElements = connectorRoot.getElementsByTagName("properties");
        return (Element) propertiesElements.item(0);
    }

    /** Convert a single source or destination connector from pre-1.8 to 1.8 */
    public static void convertOneConnectorFor1_8(Document document, Element connectorRoot) throws Exception {

        Node transportNode = getConnectorTransportNode(connectorRoot);
        String transportNameText = transportNode.getTextContent();

        Element propertiesElement = getPropertiesElement(connectorRoot);

        // Properties to be added if they're missing.
        Map<String, String> propertyDefaults = new HashMap<String, String>();
        propertyDefaults.put("charsetEncoding", "DEFAULT_ENCODING");
        propertyDefaults.put("FTPAnonymous", "1");
        propertyDefaults.put("outputAppend", "0");
        propertyDefaults.put("passive", "1");
        propertyDefaults.put("password", "anonymous");
        propertyDefaults.put("username", "anonymous");
        propertyDefaults.put("validateConnections", "1");

        // Properties to be added if missing, or reset if present
        Map<String, String> propertyChanges = new HashMap<String, String>();

        if (transportNameText.equals("File Reader")) {

            propertyDefaults.put("scheme", "file");
            updateProperties(document, propertiesElement, propertyDefaults, propertyChanges);
        } else if (transportNameText.equals("File Writer")) {

            propertyDefaults.put("scheme", "file");
            updateProperties(document, propertiesElement, propertyDefaults, propertyChanges);
        } else if (transportNameText.equals("FTP Reader")) {

            transportNode.setTextContent("File Reader");

            propertyDefaults.put("scheme", "ftp");
            propertyChanges.put("DataType", "File Reader");

            updateProperties(document, propertiesElement, propertyDefaults, propertyChanges);
        } else if (transportNameText.equals("FTP Writer")) {

            transportNode.setTextContent("File Writer");

            propertyDefaults.put("scheme", "ftp");
            propertyChanges.put("DataType", "File Writer");

            updateProperties(document, propertiesElement, propertyDefaults, propertyChanges);
        } else if (transportNameText.equals("SFTP Reader")) {

            transportNode.setTextContent("File Reader");

            propertyDefaults.put("scheme", "sftp");
            propertyDefaults.put("FTPAnonymous", "0");
            propertyChanges.put("DataType", "File Reader");

            updateProperties(document, propertiesElement, propertyDefaults, propertyChanges);
        } else if (transportNameText.equals("SFTP Writer")) {

            transportNode.setTextContent("File Writer");

            propertyDefaults.put("scheme", "sftp");
            propertyDefaults.put("FTPAnonymous", "0");
            propertyChanges.put("DataType", "File Writer");

            updateProperties(document, propertiesElement, propertyDefaults, propertyChanges);
        }
    }

    public static void convertChannelConnectorsFor2_0(Document document, Element channelRoot) throws Exception {
        Element sourceConnectorRoot = (Element) channelRoot.getElementsByTagName("sourceConnector").item(0);
        Element destinationConnectorRoot = (Element) channelRoot.getElementsByTagName("destinationConnectors").item(0);
        NodeList destinationsConnectors = destinationConnectorRoot.getElementsByTagName("com.mirth.connect.model.Connector");

        // Convert the source connector
        convertSoapConnectorFor2_0(document, sourceConnectorRoot);

        // Convert all destination connectors
        for (int i = 0; i < destinationsConnectors.getLength(); i++) {
            Element destinationConnector = (Element) destinationsConnectors.item(i);
            convertSoapConnectorFor2_0(document, destinationConnector);
        }
    }

    /** Convert soap connector and destination settings */
    public static void convertSoapConnectorFor2_0(Document document, Element connectorRoot) throws Exception {

        // convert SOAP reader and SOAP writer to the new formats
        Node transportNode = getConnectorTransportNode(connectorRoot);
        String transportNameText = transportNode.getTextContent();
        String attribute = "";
        String value = "";
        Element propertiesElement = getPropertiesElement(connectorRoot);

        // Default Properties
        Map<String, String> propertyDefaults = new HashMap<String, String>();

        // Properties to be added if missing, or reset if present
        Map<String, String> propertyChanges = new HashMap<String, String>();

        // logic to deal with SOAP listener settings
        if (transportNameText.equals("SOAP Listener")) {
            NodeList properties = connectorRoot.getElementsByTagName("property");

            // set defaults
            propertyDefaults.put("DataType", "Web Service Listener");
            propertyDefaults.put("host", "0.0.0.0");
            propertyDefaults.put("port", "8081");
            propertyDefaults.put("receiverClassName", "com.mirth.connect.connectors.ws.DefaultAcceptMessage");
            propertyDefaults.put("receiverServiceName", "Mirth");
            propertyDefaults.put("receiverResponseValue", "None");
            ObjectXMLSerializer serializer = new ObjectXMLSerializer();
            propertyDefaults.put("receiverUsernames", serializer.toXML(new ArrayList<String>()));
            propertyDefaults.put("receiverPasswords", serializer.toXML(new ArrayList<String>()));

            // rename properties
            for (int i = 0; i < properties.getLength(); i++) {
                // get the current attribute and current value
                attribute = properties.item(i).getAttributes().item(0).getTextContent();
                value = properties.item(i).getTextContent();

                // Now rename attributes
                if (attribute.equals("responseValue")) {
                    propertyChanges.put("receiverResponseValue", value);
                }

                if (attribute.equals("serviceName")) {
                    propertyChanges.put("receiverServiceName", value);
                }
            }

            // set changes
            propertyChanges.put("host", "0.0.0.0");
            propertyChanges.put("DataType", "Web Service Listener");

            // set new name of transport node
            transportNode.setTextContent("Web Service Listener");

            // update properties
            updateProperties(document, propertiesElement, propertyDefaults, propertyChanges);

        } else if (transportNameText.equals("SOAP Sender")) {
            // get properties
            NodeList properties = connectorRoot.getElementsByTagName("property");

            // disable connector
            document.getElementsByTagName("enabled").item(0).setTextContent("false");

            // set defaults
            propertyDefaults.put("DataType", "Web Service Sender");
            propertyDefaults.put("host", "");
            propertyDefaults.put("dispatcherWsdlCacheId", "");
            propertyDefaults.put("dispatcherWsdlUrl", "");
            propertyDefaults.put("dispatcherService", "");
            propertyDefaults.put("dispatcherPort", "");
            propertyDefaults.put("dispatcherOperation", "Press Get Operations");
            propertyDefaults.put("dispatcherUseAuthentication", "0");
            propertyDefaults.put("dispatcherUsername", "");
            propertyDefaults.put("dispatcherPassword", "");
            propertyDefaults.put("dispatcherEnvelope", "");
            propertyDefaults.put("dispatcherOneWay", "0");
            propertyDefaults.put("dispatcherUseMtom", "0");
            propertyDefaults.put("dispatcherReplyChannelId", "sink");

            ObjectXMLSerializer serializer = new ObjectXMLSerializer();

            ArrayList<String> defaultOperations = new ArrayList<String>();
            defaultOperations.add("Press Get Operations");
            propertyDefaults.put("dispatcherWsdlOperations", serializer.toXML(defaultOperations));

            propertyDefaults.put("dispatcherAttachmentNames", serializer.toXML(new ArrayList<String>()));
            propertyDefaults.put("dispatcherAttachmentContents", serializer.toXML(new ArrayList<String>()));
            propertyDefaults.put("dispatcherAttachmentTypes", serializer.toXML(new ArrayList<String>()));

            // NodeList properties = document.getElementsByTagName("property");
            for (int i = 0; i < properties.getLength(); i++) {
                // get the current attribute and current value
                attribute = properties.item(i).getAttributes().item(0).getTextContent();
                value = properties.item(i).getTextContent();

                // Now rename attributes
                if (attribute.equals("attachmentNames")) {
                    propertyChanges.put("dispatcherAttachmentNames", value);
                }

                if (attribute.equals("wsdlUrl")) {
                    propertyChanges.put("dispatcherWsdlUrl", value);
                }

                if (attribute.equals("replyChannelId")) {
                    propertyChanges.put("dispatcherReplyChannelId", value);
                }

                if (attribute.equals("attachmentContents")) {
                    propertyChanges.put("dispatcherAttachmentContents", value);
                }

                if (attribute.equals("soapEnvelope")) {
                    propertyChanges.put("dispatcherEnvelope", value);
                }

                if (attribute.equals("attachmentTypes")) {
                    propertyChanges.put("dispatcherAttachmentTypes", value);
                }
            }

            propertyChanges.put("DataType", "Web Service Sender");

            // set new name of transport node
            transportNode.setTextContent("Web Service Sender");
            updateProperties(document, propertiesElement, propertyDefaults, propertyChanges);
        }
    }

    public static String convertFilter(String filterXml) throws Exception {
        filterXml = convertPackageNames(filterXml);
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(filterXml)));

            updateFilterFor1_4(document.getDocumentElement());
            updateFilterFor1_7(document);

            DocumentSerializer docSerializer = new DocumentSerializer();
            filterXml = docSerializer.toXML(document);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return updateLocalAndGlobalVariables(filterXml);
    }

    /**
     * Convert a connector. Added in 1.8 and shouldn't be called on pre-1.8
     * connectors.
     * 
     * @param connector
     * @return
     * @throws Exception
     */
    public static String convertConnector(String connectorXml) throws Exception {
        connectorXml = convertPackageNames(connectorXml);
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;
        DocumentBuilder builder;

        builder = factory.newDocumentBuilder();
        document = builder.parse(new InputSource(new StringReader(connectorXml)));
        Element connectorRoot = document.getDocumentElement();

        Node transportNode = getConnectorTransportNode(connectorRoot);
        String transportNameText = transportNode.getTextContent();

        // SOAP connectors only existed before 2.0, so convert it for 2.0
        if (transportNameText.equals("SOAP Sender") || transportNameText.equals("SOAP Listener")) {
            convertSoapConnectorFor2_0(document, connectorRoot);
        }

        DocumentSerializer docSerializer = new DocumentSerializer();
        connectorXml = docSerializer.toXML(document);

        return connectorXml;
    }

    public static String convertTransformer(String transformerXml, Protocol incoming, Protocol outgoing) throws Exception {
        transformerXml = convertPackageNames(transformerXml);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;
        DocumentBuilder builder;

        builder = factory.newDocumentBuilder();
        document = builder.parse(new InputSource(new StringReader(transformerXml)));
        Element transformerRoot = document.getDocumentElement();

        updateTransformerFor1_4(document, transformerRoot, incoming, outgoing);
        updateTransformerFor1_5(document);
        updateTransformerFor1_7(document);
        updateTransformerFor1_7_1(document);

        DocumentSerializer docSerializer = new DocumentSerializer();
        transformerXml = docSerializer.toXML(document);

        return updateLocalAndGlobalVariables(transformerXml);
    }

    private static void updateFilterFor1_4(Element filterElement) {
        Element filterTemplate = null;

        if (filterElement.getElementsByTagName("template").getLength() > 0) {
            filterTemplate = (Element) filterElement.getElementsByTagName("template").item(0);
            if (filterTemplate != null)
                filterElement.removeChild(filterElement.getElementsByTagName("template").item(0));
        }
    }

    /*
     * Upgrade pre-1.4 channels' transformers and filters to work with 1.4+
     */
    private static void updateTransformerFor1_4(Document document, Element transformerRoot, Protocol incoming, Protocol outgoing) {

        String template = "";
        Element transformerTemplate = null;

        if (transformerRoot.getElementsByTagName("template").getLength() > 0) {
            transformerTemplate = (Element) transformerRoot.getElementsByTagName("template").item(0);
            if (transformerTemplate != null)
                template = transformerTemplate.getTextContent();
        }

        Element inboundTemplateElement = null, outboundTemplateElement = null, inboundProtocolElement = null, outboundProtocolElement = null;
        if (transformerRoot.getElementsByTagName("inboundTemplate").getLength() == 0)
            inboundTemplateElement = document.createElement("inboundTemplate");
        if (transformerRoot.getElementsByTagName("outboundTemplate").getLength() == 0)
            outboundTemplateElement = document.createElement("outboundTemplate");
        if (transformerRoot.getElementsByTagName("inboundProtocol").getLength() == 0) {
            inboundProtocolElement = document.createElement("inboundProtocol");
            inboundProtocolElement.setTextContent(incoming.toString());
        }
        if (transformerRoot.getElementsByTagName("outboundProtocol").getLength() == 0) {
            outboundProtocolElement = document.createElement("outboundProtocol");
            outboundProtocolElement.setTextContent(outgoing.toString());
        }

        if (transformerTemplate != null) {
            if (incoming == Protocol.HL7V2 && outgoing == Protocol.HL7V2) {
                inboundTemplateElement.setTextContent(template);
            } else if (outgoing == Protocol.HL7V2) {
                outboundTemplateElement.setTextContent(template);
            }
        }

        if (transformerRoot.getElementsByTagName("inboundTemplate").getLength() == 0)
            transformerRoot.appendChild(inboundTemplateElement);
        if (transformerRoot.getElementsByTagName("outboundTemplate").getLength() == 0)
            transformerRoot.appendChild(outboundTemplateElement);
        if (transformerRoot.getElementsByTagName("inboundProtocol").getLength() == 0)
            transformerRoot.appendChild(inboundProtocolElement);
        if (transformerRoot.getElementsByTagName("outboundProtocol").getLength() == 0)
            transformerRoot.appendChild(outboundProtocolElement);

        // replace HL7 Message builder with Message Builder
        NodeList steps = transformerRoot.getElementsByTagName("com.mirth.connect.model.Step");

        for (int i = 0; i < steps.getLength(); i++) {
            Element step = (Element) steps.item(i);
            NodeList stepTypesList = step.getElementsByTagName("type");
            if (stepTypesList.getLength() > 0) {
                Element stepType = (Element) stepTypesList.item(0);
                if (stepType.getTextContent().equals("HL7 Message Builder")) {
                    stepType.setTextContent("Message Builder");
                }

                if (stepType.getTextContent().equals("Message Builder") || stepType.getTextContent().equals("Mapper")) {
                    boolean foundRegex = false, foundDefaultValue = false;
                    Element data = (Element) step.getElementsByTagName("data").item(0);
                    NodeList entries = data.getElementsByTagName("entry");

                    for (int j = 0; j < entries.getLength(); j++) {
                        NodeList strings = ((Element) entries.item(j)).getElementsByTagName("string");

                        if (strings.getLength() > 0) {
                            if (strings.item(0).getTextContent().equals("RegularExpressions"))
                                foundRegex = true;
                            else if (strings.item(0).getTextContent().equals("DefaultValue"))
                                foundDefaultValue = true;

                            if (strings.item(0).getTextContent().equals("isGlobal")) {
                                if (strings.item(1).getTextContent().equals("0"))
                                    strings.item(1).setTextContent("channel");
                                else if (strings.item(1).getTextContent().equals("1"))
                                    strings.item(1).setTextContent("global");
                            }
                        }
                    }

                    if (!foundRegex)
                        data.appendChild(createRegexElement(document));
                    if (!foundDefaultValue)
                        data.appendChild(createDefaultValueElement(document));
                }
            }
        }

        if (transformerTemplate != null)
            transformerRoot.removeChild((Node) transformerTemplate);

    }

    private static void updateTransformerFor1_5(Document document) {
        Element inboundPropertiesElement, outboundPropertiesElement;

        NodeList transformers = document.getElementsByTagName("transformer");

        for (int i = 0; i < transformers.getLength(); i++) {
            Element transformerRoot = (Element) transformers.item(i);

            if (transformerRoot.getElementsByTagName("inboundProtocol").item(0).getTextContent().equals("HL7V2") && transformerRoot.getElementsByTagName("inboundProperties").getLength() == 0) {
                inboundPropertiesElement = document.createElement("inboundProperties");

                Element handleRepetitionsProperty = document.createElement("property");
                handleRepetitionsProperty.setAttribute("name", "handleRepetitions");
                handleRepetitionsProperty.setTextContent("false");

                Element useStrictValidationProperty = document.createElement("property");
                useStrictValidationProperty.setAttribute("name", "useStrictValidation");
                useStrictValidationProperty.setTextContent("false");

                Element useStrictParserProperty = document.createElement("property");
                useStrictParserProperty.setAttribute("name", "useStrictParser");
                useStrictParserProperty.setTextContent("true");

                inboundPropertiesElement.appendChild(handleRepetitionsProperty);
                inboundPropertiesElement.appendChild(useStrictValidationProperty);
                inboundPropertiesElement.appendChild(useStrictParserProperty);

                transformerRoot.appendChild(inboundPropertiesElement);
            }

            if (transformerRoot.getElementsByTagName("outboundProtocol").item(0).getTextContent().equals("HL7V2") && transformerRoot.getElementsByTagName("outboundProperties").getLength() == 0) {
                outboundPropertiesElement = document.createElement("outboundProperties");

                Element handleRepetitionsProperty = document.createElement("property");
                handleRepetitionsProperty.setAttribute("name", "handleRepetitions");
                handleRepetitionsProperty.setTextContent("false");

                Element useStrictValidationProperty = document.createElement("property");
                useStrictValidationProperty.setAttribute("name", "useStrictValidation");
                useStrictValidationProperty.setTextContent("false");

                Element useStrictParserProperty = document.createElement("property");
                useStrictParserProperty.setAttribute("name", "useStrictParser");
                useStrictParserProperty.setTextContent("true");

                outboundPropertiesElement.appendChild(handleRepetitionsProperty);
                outboundPropertiesElement.appendChild(useStrictValidationProperty);
                outboundPropertiesElement.appendChild(useStrictParserProperty);

                transformerRoot.appendChild(outboundPropertiesElement);
            }
        }
    }

    private static void updateFilterFor1_7(Document document) {
        // add data element to Rules
        NodeList rules = document.getElementsByTagName("com.mirth.connect.model.Rule");

        for (int i = 0; i < rules.getLength(); i++) {
            Element rule = (Element) rules.item(i);

            if (rule.getElementsByTagName("type").getLength() == 0) {
                Element typeElement = document.createElement("type");
                typeElement.setTextContent("JavaScript");
                rule.appendChild(typeElement);
            }

            if (rule.getElementsByTagName("data").getLength() == 0) {
                Element dataElement = document.createElement("data");
                dataElement.setAttribute("class", "map");

                Element entryElement = document.createElement("entry");
                Element keyElement = document.createElement("string");
                Element valueElement = document.createElement("string");

                keyElement.setTextContent("Script");
                valueElement.setTextContent(rule.getElementsByTagName("script").item(0).getTextContent());

                entryElement.appendChild(keyElement);
                entryElement.appendChild(valueElement);

                dataElement.appendChild(entryElement);

                rule.appendChild(dataElement);
            }
        }
    }

    private static void updateTransformerFor1_7(Document document) {
        Element inboundPropertiesElement, outboundPropertiesElement;

        NodeList transformers = document.getElementsByTagName("transformer");

        for (int i = 0; i < transformers.getLength(); i++) {
            Element transformerRoot = (Element) transformers.item(i);

            // Update the inbound protocol properties.
            if (transformerRoot.getElementsByTagName("inboundProtocol").item(0).getTextContent().equals("HL7V2")) {
                if (transformerRoot.getElementsByTagName("inboundProperties").getLength() != 0) {

                    inboundPropertiesElement = (Element) transformerRoot.getElementsByTagName("inboundProperties").item(0);

                    // Find out if encodeEntities already exists. If it was 1.5
                    // it won't.
                    boolean hasEncodeEntities = false;
                    NodeList propertyNames = inboundPropertiesElement.getElementsByTagName("property");
                    for (int j = 0; j < propertyNames.getLength(); j++) {
                        Node nameAttribute = propertyNames.item(j).getAttributes().getNamedItem("name");
                        if (propertyNames.item(j).getAttributes().getLength() > 0 && nameAttribute != null) {
                            if (nameAttribute.getNodeValue().equals("encodeEntities")) {
                                hasEncodeEntities = true;
                            }
                        }
                    }

                    // Add encodeEntities if it doesn't exist.
                    if (!hasEncodeEntities) {
                        Element encodeEntities = document.createElement("property");
                        encodeEntities.setAttribute("name", "encodeEntities");
                        encodeEntities.setTextContent("true");

                        inboundPropertiesElement.appendChild(encodeEntities);
                    }

                    Element convertLFtoCRProperty = document.createElement("property");
                    convertLFtoCRProperty.setAttribute("name", "convertLFtoCR");
                    convertLFtoCRProperty.setTextContent("true");

                    inboundPropertiesElement.appendChild(convertLFtoCRProperty);
                } else {
                    inboundPropertiesElement = document.createElement("inboundProperties");

                    Element convertLFtoCRProperty = document.createElement("property");
                    convertLFtoCRProperty.setAttribute("name", "convertLFtoCR");
                    convertLFtoCRProperty.setTextContent("true");

                    Element encodeEntitiesProperty = document.createElement("property");
                    encodeEntitiesProperty.setAttribute("name", "encodeEntities");
                    encodeEntitiesProperty.setTextContent("true");

                    Element handleRepetitionsProperty = document.createElement("property");
                    handleRepetitionsProperty.setAttribute("name", "handleRepetitions");
                    handleRepetitionsProperty.setTextContent("false");

                    Element useStrictValidationProperty = document.createElement("property");
                    useStrictValidationProperty.setAttribute("name", "useStrictValidation");
                    useStrictValidationProperty.setTextContent("false");

                    Element useStrictParserProperty = document.createElement("property");
                    useStrictParserProperty.setAttribute("name", "useStrictParser");
                    useStrictParserProperty.setTextContent("false");

                    inboundPropertiesElement.appendChild(convertLFtoCRProperty);
                    inboundPropertiesElement.appendChild(encodeEntitiesProperty);
                    inboundPropertiesElement.appendChild(handleRepetitionsProperty);
                    inboundPropertiesElement.appendChild(useStrictValidationProperty);
                    inboundPropertiesElement.appendChild(useStrictParserProperty);

                    transformerRoot.appendChild(inboundPropertiesElement);
                }
            }

            // Update the outbound protocol properties.
            if (transformerRoot.getElementsByTagName("outboundProtocol").item(0).getTextContent().equals("HL7V2")) {
                if (transformerRoot.getElementsByTagName("outboundProperties").getLength() != 0) {
                    outboundPropertiesElement = (Element) transformerRoot.getElementsByTagName("outboundProperties").item(0);

                    // Find out if encodeEntities already exists. If it was 1.5
                    // it won't.
                    boolean hasEncodeEntities = false;
                    NodeList propertyNames = outboundPropertiesElement.getElementsByTagName("property");
                    for (int j = 0; j < propertyNames.getLength(); j++) {
                        Node nameAttribute = propertyNames.item(j).getAttributes().getNamedItem("name");
                        if (propertyNames.item(j).getAttributes().getLength() > 0 && nameAttribute != null) {
                            if (nameAttribute.getNodeValue().equals("encodeEntities")) {
                                hasEncodeEntities = true;
                            }
                        }
                    }

                    // Add encodeEntities if it doesn't exist.
                    if (!hasEncodeEntities) {
                        Element encodeEntities = document.createElement("property");
                        encodeEntities.setAttribute("name", "encodeEntities");
                        encodeEntities.setTextContent("true");

                        outboundPropertiesElement.appendChild(encodeEntities);
                    }

                    Element convertLFtoCRProperty = document.createElement("property");
                    convertLFtoCRProperty.setAttribute("name", "convertLFtoCR");
                    convertLFtoCRProperty.setTextContent("true");

                    outboundPropertiesElement.appendChild(convertLFtoCRProperty);
                } else {
                    outboundPropertiesElement = document.createElement("outboundProperties");

                    Element convertLFtoCRProperty = document.createElement("property");
                    convertLFtoCRProperty.setAttribute("name", "convertLFtoCR");
                    convertLFtoCRProperty.setTextContent("true");

                    Element encodeEntitiesProperty = document.createElement("property");
                    encodeEntitiesProperty.setAttribute("name", "encodeEntities");
                    encodeEntitiesProperty.setTextContent("true");

                    Element handleRepetitionsProperty = document.createElement("property");
                    handleRepetitionsProperty.setAttribute("name", "handleRepetitions");
                    handleRepetitionsProperty.setTextContent("false");

                    Element useStrictValidationProperty = document.createElement("property");
                    useStrictValidationProperty.setAttribute("name", "useStrictValidation");
                    useStrictValidationProperty.setTextContent("false");

                    Element useStrictParserProperty = document.createElement("property");
                    useStrictParserProperty.setAttribute("name", "useStrictParser");
                    useStrictParserProperty.setTextContent("false");

                    outboundPropertiesElement.appendChild(convertLFtoCRProperty);
                    outboundPropertiesElement.appendChild(encodeEntitiesProperty);
                    outboundPropertiesElement.appendChild(handleRepetitionsProperty);
                    outboundPropertiesElement.appendChild(useStrictValidationProperty);
                    outboundPropertiesElement.appendChild(useStrictParserProperty);

                    transformerRoot.appendChild(outboundPropertiesElement);
                }
            }
        }
    }

    private static void updateTransformerFor1_7_1(Document document) {
        Element inboundPropertiesElement, outboundPropertiesElement;

        NodeList transformers = document.getElementsByTagName("transformer");

        for (int i = 0; i < transformers.getLength(); i++) {
            Element transformerRoot = (Element) transformers.item(i);

            // Update the inbound protocol properties.
            if (transformerRoot.getElementsByTagName("inboundProtocol").item(0).getTextContent().equals("HL7V2") || transformerRoot.getElementsByTagName("inboundProtocol").item(0).getTextContent().equals("EDI")) {
                if (transformerRoot.getElementsByTagName("inboundProperties").getLength() != 0) {
                    inboundPropertiesElement = (Element) transformerRoot.getElementsByTagName("inboundProperties").item(0);

                    // Remove encode entities if it exists.
                    NodeList propertyNames = inboundPropertiesElement.getElementsByTagName("property");
                    for (int j = 0; j < propertyNames.getLength(); j++) {
                        Node nameAttribute = propertyNames.item(j).getAttributes().getNamedItem("name");
                        if (propertyNames.item(j).getAttributes().getLength() > 0 && nameAttribute != null) {
                            if (nameAttribute.getNodeValue().equals("encodeEntities")) {
                                inboundPropertiesElement.removeChild(propertyNames.item(j));
                            }
                        }
                    }

                    // Override convertLFtoCR and set it to "true".
                    propertyNames = inboundPropertiesElement.getElementsByTagName("property");
                    for (int j = 0; j < propertyNames.getLength(); j++) {
                        Node nameAttribute = propertyNames.item(j).getAttributes().getNamedItem("name");
                        if (propertyNames.item(j).getAttributes().getLength() > 0 && nameAttribute != null) {
                            if (nameAttribute.getNodeValue().equals("convertLFtoCR")) {
                                propertyNames.item(j).setTextContent("true");
                            }
                        }
                    }
                }
            }

            // Update the outbound protocol properties.
            if (transformerRoot.getElementsByTagName("outboundProtocol").item(0).getTextContent().equals("HL7V2") || transformerRoot.getElementsByTagName("outboundProtocol").item(0).getTextContent().equals("EDI")) {
                if (transformerRoot.getElementsByTagName("outboundProperties").getLength() != 0) {
                    outboundPropertiesElement = (Element) transformerRoot.getElementsByTagName("outboundProperties").item(0);

                    // Remove encode entities if it exists.
                    NodeList propertyNames = outboundPropertiesElement.getElementsByTagName("property");
                    for (int j = 0; j < propertyNames.getLength(); j++) {
                        Node nameAttribute = propertyNames.item(j).getAttributes().getNamedItem("name");
                        if (propertyNames.item(j).getAttributes().getLength() > 0 && nameAttribute != null) {
                            if (nameAttribute.getNodeValue().equals("encodeEntities")) {
                                outboundPropertiesElement.removeChild(propertyNames.item(j));
                            }
                        }
                    }

                    // Override convertLFtoCR and set it to "true".
                    propertyNames = outboundPropertiesElement.getElementsByTagName("property");
                    for (int j = 0; j < propertyNames.getLength(); j++) {
                        Node nameAttribute = propertyNames.item(j).getAttributes().getNamedItem("name");
                        if (propertyNames.item(j).getAttributes().getLength() > 0 && nameAttribute != null) {
                            if (nameAttribute.getNodeValue().equals("convertLFtoCR")) {
                                propertyNames.item(j).setTextContent("true");
                            }
                        }
                    }
                }
            }
        }
    }

    private static void updateTransformerFor1_8_1(Document document) {
        Element inboundPropertiesElement, outboundPropertiesElement;

        NodeList transformers = document.getElementsByTagName("transformer");

        for (int i = 0; i < transformers.getLength(); i++) {
            Element transformerRoot = (Element) transformers.item(i);

            // Update the inbound protocol properties.
            if (transformerRoot.getElementsByTagName("inboundProtocol").item(0).getTextContent().equals("XML") || transformerRoot.getElementsByTagName("inboundProtocol").item(0).getTextContent().equals("HL7V2") || transformerRoot.getElementsByTagName("inboundProtocol").item(0).getTextContent().equals("HL7V3")) {

                if (transformerRoot.getElementsByTagName("inboundProperties").getLength() == 0) {
                    inboundPropertiesElement = document.createElement("inboundProperties");
                    transformerRoot.appendChild(inboundPropertiesElement);
                }

                inboundPropertiesElement = (Element) transformerRoot.getElementsByTagName("inboundProperties").item(0);

                Element stripNamespacesProperty = document.createElement("property");
                stripNamespacesProperty.setAttribute("name", "stripNamespaces");
                stripNamespacesProperty.setTextContent("true");

                // Override stripNamespaces on inbound if the old
                // removeNamespace was set to false
                NodeList propertyNames = document.getElementsByTagName("property");
                for (int j = 0; j < propertyNames.getLength(); j++) {
                    Node nameAttribute = propertyNames.item(j).getAttributes().getNamedItem("name");
                    if (propertyNames.item(j).getAttributes().getLength() > 0 && nameAttribute != null) {
                        if (nameAttribute.getNodeValue().equals("removeNamespace")) {
                            stripNamespacesProperty.setTextContent(propertyNames.item(j).getTextContent());
                        }
                    }
                }

                inboundPropertiesElement.appendChild(stripNamespacesProperty);
            }

            // Update the outbound protocol properties.
            if (transformerRoot.getElementsByTagName("outboundProtocol").item(0).getTextContent().equals("XML") || transformerRoot.getElementsByTagName("outboundProtocol").item(0).getTextContent().equals("HL7V2") || transformerRoot.getElementsByTagName("outboundProtocol").item(0).getTextContent().equals("HL7V3")) {

                if (transformerRoot.getElementsByTagName("outboundProperties").getLength() == 0) {
                    outboundPropertiesElement = document.createElement("outboundProperties");
                    transformerRoot.appendChild(outboundPropertiesElement);
                }

                outboundPropertiesElement = (Element) transformerRoot.getElementsByTagName("outboundProperties").item(0);

                Element stripNamespacesProperty = document.createElement("property");
                stripNamespacesProperty.setAttribute("name", "stripNamespaces");
                stripNamespacesProperty.setTextContent("true");

                outboundPropertiesElement.appendChild(stripNamespacesProperty);
            }
        }
    }

    public static Element createRegexElement(Document document) {
        Element entryElement = document.createElement("entry");
        Element regexElement = document.createElement("string");
        // Element stringArrayElement = document.createElement("string-array");
        Element listElement = document.createElement("list");

        regexElement.setTextContent("RegularExpressions");

        entryElement.appendChild(regexElement);
        entryElement.appendChild(listElement);

        return entryElement;
    }

    public static Element createDefaultValueElement(Document document) {
        Element entryElement = document.createElement("entry");
        Element defaultValueElement = document.createElement("string");
        Element defaultValueValueElement = document.createElement("string");

        defaultValueElement.setTextContent("DefaultValue");

        entryElement.appendChild(defaultValueElement);
        entryElement.appendChild(defaultValueValueElement);

        return entryElement;
    }

    public static String updateLocalAndGlobalVariables(String xml) throws Exception {
        xml = xml.replaceAll("localMap.put", "channelMap.put");
        xml = xml.replaceAll("localMap.get", "channelMap.get");
        return xml;
    }

    private static String read(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        StringBuilder contents = new StringBuilder();
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                contents.append(line + "\n");
            }
        } finally {
            reader.close();
        }

        return contents.toString();
    }

    /**
     * Removes certain invalid characters
     */
    private static String removeInvalidHexChar(String string) {
        String result = string;
        for (char i = 0x0; i <= 0x8; i++) {
            result = result.replace(i, ' ');
        }
        for (char i = 0xB; i <= 0xC; i++) {
            result = result.replace(i, ' ');
        }
        for (char i = 0xE; i <= 0x1F; i++) {
            result = result.replace(i, ' ');
        }
        return result;
    }

    private static boolean nodeChildrenContains(Node parent, String elementName) {
        NodeList children = parent.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeName().equals(elementName)) {
                return true;
            }
        }

        return false;
    }

    private static void setBooleanNode(Node parent, String elementName, boolean value) {
        NodeList children = parent.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeName().equals(elementName)) {
                child.setTextContent(Boolean.toString(value));
            }
        }
    }
}
