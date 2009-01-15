package com.webreach.mirth.server.mule.adaptors;

import java.io.Reader;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.mule.umo.MessagingException;
import org.mule.umo.UMOException;

import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.SerializerFactory;

public class HL7v2Adaptor extends Adaptor implements BatchAdaptor {

	protected void populateMessage(boolean emptyFilterAndTransformer) throws AdaptorException {
		messageObject.setRawDataProtocol(com.webreach.mirth.model.MessageObject.Protocol.HL7V2);
		messageObject.setTransformedDataProtocol(com.webreach.mirth.model.MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(com.webreach.mirth.model.MessageObject.Protocol.HL7V2);
		
		try {
			if (emptyFilterAndTransformer) {
				populateMetadataFromEncoded(source);
				messageObject.setEncodedData(source);
			} else {
				String xmlMessage = serializer.toXML(source);
				populateMetadataFromXML(xmlMessage);
				messageObject.setTransformedData(xmlMessage);
			}
		} catch (Exception e) {
			handleException(e);
		}
	}

	@Override
	public IXMLSerializer<String> getSerializer(Map properties) {
		return SerializerFactory.getSerializer(Protocol.HL7V2, properties);
	}

	public void processBatch(Reader src, Map properties, BatchMessageProcessor dest)
		throws MessagingException, UMOException
	{
		// TODO: The values of these parameters should come from the protocol
		// properties passed to processBatch
		// TODO: src is a character stream, not a byte stream
		byte startOfMessage = (byte) 0x0B;
		byte endOfMessage = (byte) 0x1C;
		byte endOfRecord = (byte) 0x0D;

		Scanner scanner = new Scanner(src);
		scanner.useDelimiter(Pattern.compile("\r\n|\r|\n"));
		StringBuilder message = new StringBuilder();
		char data[] = { (char) startOfMessage, (char) endOfMessage };
		while (scanner.hasNext()) {
			String line = scanner.next().replaceAll(new String(data, 0, 1), "").replaceAll(new String(data, 1, 1), "").trim();

			if ((line.length() == 0) || line.equals((char) endOfMessage) || line.startsWith("MSH")) {
				if (message.length() > 0) {
					dest.processBatchMessage(message.toString());
					message = new StringBuilder();
				}

				while ((line.length() == 0) && scanner.hasNext()) {
					line = scanner.next();
				}

				if (line.length() > 0) {
					message.append(line);
					message.append((char) endOfRecord);
				}
			} else if (line.startsWith("FHS") || line.startsWith("BHS") || line.startsWith("BTS") || line.startsWith("FTS")){
				//ignore batch headers
				if (!scanner.hasNext()) {
					dest.processBatchMessage(message.toString());
					message = new StringBuilder();
				}
			} else {
				message.append(line);
				message.append((char) endOfRecord);

				if (!scanner.hasNext()) {
					dest.processBatchMessage(message.toString());
					message = new StringBuilder();
				}
			}
		}

		scanner.close();
	}
}
