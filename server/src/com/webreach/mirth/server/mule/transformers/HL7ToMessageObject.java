package com.webreach.mirth.server.mule.transformers;

import org.apache.log4j.Logger;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.model.converters.SerializerException;

public class HL7ToMessageObject extends AbstractTransformer {
	private Logger logger = Logger.getLogger(this.getClass());
	private ER7Serializer serializer = new ER7Serializer();

	public HL7ToMessageObject() {
		super();
		registerSourceType(String.class);
		setReturnClass(MessageObject.class);
	}

	@Override
	public Object doTransform(Object src) throws TransformerException {
		String rawData = (String) src;
		MessageObject messageObject = new MessageObject();
		messageObject.setRawData(rawData);
		messageObject.setRawDataProtocol(MessageObject.Protocol.HL7);
		
		try {
			messageObject.setTransformedData(sanitize(serializer.toXML(rawData)));	
		} catch (SerializerException e) {
			messageObject.setErrors(e.toString());
		}
		
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.HL7);
		messageObject.setStatus(MessageObject.Status.RECEIVED);
		return messageObject;
	}

	// cleans up the XML
	public String sanitize(String source) {
		source.replaceAll("&#xd;", "");
		return source;
	}
}