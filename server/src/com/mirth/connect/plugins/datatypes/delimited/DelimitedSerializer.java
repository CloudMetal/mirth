/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.delimited;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.mirth.connect.donkey.model.message.XmlSerializerException;
import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.converters.XMLPrettyPrinter;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class DelimitedSerializer implements IXMLSerializer {
    private Logger logger = Logger.getLogger(this.getClass());

    private DelimitedSerializationProperties serializationProperties;
    private DelimitedDeserializationProperties deserializationProperties;
    private DelimitedBatchProperties batchProperties;

    public DelimitedSerializer(SerializerProperties properties) {
        serializationProperties = (DelimitedSerializationProperties) properties.getSerializationProperties();
        deserializationProperties = (DelimitedDeserializationProperties) properties.getDeserializationProperties();
        batchProperties = (DelimitedBatchProperties) properties.getBatchProperties();
    }
    
    @Override
    public boolean isSerializationRequired(boolean toXml) {
        boolean serializationRequired = false;
        
        //TODO Optimize which properties require serialization and which can be done via transformWithoutSerializing
        if (toXml) {
            if (!serializationProperties.getColumnDelimiter().equals(",") || !serializationProperties.getRecordDelimiter().equals("\\n") || serializationProperties.getColumnWidths() != null || !serializationProperties.getQuoteChar().equals("\"") || !serializationProperties.isEscapeWithDoubleQuote() || !serializationProperties.getQuoteEscapeChar().equals("\\") || serializationProperties.getColumnNames() != null || serializationProperties.isNumberedRows() || !serializationProperties.isIgnoreCR() || batchProperties.getBatchSkipRecords() != 0 || !batchProperties.isBatchSplitByRecord() || !batchProperties.getBatchMessageDelimiter().equals("") || batchProperties.isBatchMessageDelimiterIncluded() || !batchProperties.getBatchGroupingColumn().equals("") || !batchProperties.getBatchScript().equals("")) {
                serializationRequired = true;
            }
        } else {
            if (!deserializationProperties.getColumnDelimiter().equals(",") || !deserializationProperties.getRecordDelimiter().equals("\\n") || deserializationProperties.getColumnWidths() != null || !deserializationProperties.getQuoteChar().equals("\"") || !deserializationProperties.isEscapeWithDoubleQuote() || !deserializationProperties.getQuoteEscapeChar().equals("\\")) {
                serializationRequired = true;
            }
        }

        return serializationRequired;
    }
    
    @Override
    public String transformWithoutSerializing(String message, XmlSerializer outboundSerializer) {
        return null;
    }

    @Override
    public String fromXML(String source) throws XmlSerializerException {

        StringBuilder builder = new StringBuilder();

        try {

            DelimitedXMLHandler handler = new DelimitedXMLHandler(deserializationProperties);
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            xr.parse(new InputSource(new StringReader(source)));
            builder.append(handler.getOutput());
        } catch (Exception e) {
            String exceptionMessage = e.getClass().getName() + ":" + e.getMessage();
            logger.error(exceptionMessage);
            throw new XmlSerializerException("Error converting XML to delimited text", e, ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_500, "Error converting XML to delimited text", e));
        }
        return builder.toString();
    }

    @Override
    public Map<String, String> getMetadataFromDocument(Document doc) throws XmlSerializerException {
        Map<String, String> map = new HashMap<String, String>();
        populateMetadata(map);
        return map;
    }

    private void populateMetadata(Map<String, String> map) {
        // There is no meaningful meta data available in the delimited text case
        // for version, type and source, so populate empty strings.
        map.put("version", "");
        map.put("type", "delimited");
        map.put("source", "");
    }

    @Override
    public String toXML(String source) throws XmlSerializerException {
        try {
            StringWriter stringWriter = new StringWriter();
            XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
            serializer.setEncodeEntities(true);
            DelimitedReader delimitedReader = new DelimitedReader(serializationProperties);
            delimitedReader.setContentHandler(serializer);
            delimitedReader.parse(new InputSource(new StringReader(source)));
            return stringWriter.toString();
        } catch (Exception e) {
            logger.error("Error converting delimited text to XML.", e);
        }

        return new String();
    }
}
