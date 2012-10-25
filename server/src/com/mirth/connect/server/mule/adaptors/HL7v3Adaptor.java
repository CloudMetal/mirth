/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.mule.adaptors;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.mirth.connect.model.MessageObject;

public class HL7v3Adaptor extends Adaptor {
	protected void populateMessage(boolean emptyFilterAndTransformer) throws AdaptorException {
		messageObject.setRawDataProtocol(MessageObject.Protocol.HL7V3);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.HL7V3);
		messageObject.setType("XML");
		messageObject.setTransformedData(source);
		
		if (emptyFilterAndTransformer) {
			messageObject.setEncodedData(source);
		}

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document xmlDoc = docBuilder.parse(new InputSource(new StringReader(source)));
			messageObject.setSource(new String());
			messageObject.setType(xmlDoc.getDocumentElement().getNodeName());
			messageObject.setVersion("3.0");
		} catch (Exception e) {
			handleException(e);
		}
	}
}
