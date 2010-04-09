/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class DocumentSerializer implements IXMLSerializer<Document> {
    private Logger logger = Logger.getLogger(this.getClass());
    private String[] cDataElements = null;

    public DocumentSerializer() {
        
    }
    
    public DocumentSerializer(String[] cDataElements) {
        this.cDataElements = cDataElements;
    }

    public String toXML(Document source) {
        Writer writer = new StringWriter();

        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            
            // When Saxon-B is on the classpath setting this attribute throws an
            // IllegalArgumentException.
            try {
                factory.setAttribute("indent-number", new Integer(4));
            } catch (IllegalArgumentException ex) {
                logger.warn("Could not set Document Serializer attribute: indent-number", ex);
            }
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");

            if (source.getDoctype() != null) {
                String publicDoctype = source.getDoctype().getPublicId();
                String systemDoctype = source.getDoctype().getSystemId();

                if ((publicDoctype != null) && (publicDoctype.length() > 0)) {
                    transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, publicDoctype);
                }

                if ((systemDoctype != null) && (systemDoctype.length() > 0)) {
                    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemDoctype);
                }
            }

            if (cDataElements != null) {
                StringBuilder cDataElementsString = new StringBuilder();

                for (int i = 0; i < cDataElements.length; i++) {
                    cDataElementsString.append(cDataElements[i] + " ");
                }

                transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, cDataElementsString.toString());
            }

            transformer.transform(new DOMSource(source), new StreamResult(writer));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return writer.toString();
    }

    public Document fromXML(String source) {
        Document document = null;

        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(source)));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return document;
    }

    public Map<String, String> getMetadata() {
        return null;
    }

    public Map<String, String> getMetadataFromDocument(Document doc) throws SerializerException {
        return null;
    }

    public Map<String, String> getMetadataFromEncoded(String source) throws SerializerException {
        return null;
    }

    public Map<String, String> getMetadataFromXML(String xmlSource) throws SerializerException {
        return null;
    }
}
