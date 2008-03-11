package com.webreach.mirth.server.mule.adaptors;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.webreach.mirth.model.MessageObject;

public class HL7v3Adaptor extends Adaptor {
	protected void populateMessage(boolean emptyFilterAndTransformer) throws AdaptorException {
		messageObject.setRawDataProtocol(MessageObject.Protocol.HL7V3);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.HL7V3);
		messageObject.setType("XML");
		messageObject.setTransformedData(source.trim());
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;

		try {
			docBuilder = docFactory.newDocumentBuilder();
			Document xmlDoc = docBuilder.parse(new InputSource(new StringReader(source.trim())));
			messageObject.setSource(new String());
			messageObject.setType(xmlDoc.getDocumentElement().getNodeName());
			messageObject.setVersion("3.0");
		} catch (Exception e) {
			handleException(e);
		}
	}
}
