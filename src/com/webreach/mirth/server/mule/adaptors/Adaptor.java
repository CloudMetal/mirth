package com.webreach.mirth.server.mule.adaptors;

import java.util.Calendar;
import java.util.Map;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.DefaultXMLSerializer;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.util.UUIDGenerator;

public abstract class Adaptor {
	private Logger logger = Logger.getLogger(this.getClass());
	protected MessageObject messageObject;
	protected String source;
	protected Map properties;
	private MessageObjectController messageObjectController = new MessageObjectController();

	public MessageObject getMessage(String source, String channelId, boolean encryptData, Map properties) throws AdaptorException {
		this.source = source;
        this.properties = properties;
		messageObject = new MessageObject();
		messageObject.setId(UUIDGenerator.getUUID());
		messageObject.setChannelId(channelId);
		messageObject.setDateCreated(Calendar.getInstance());
		messageObject.setConnectorName("Source");
		messageObject.setEncrypted(encryptData);
		messageObject.setRawData(source);

		populateMessage();

		messageObject.setStatus(MessageObject.Status.RECEIVED);
		// messageObjectController.updateMessage(messageObject);
		return messageObject;
	}

	public MessageObject convertMessage(MessageObject messageObject, String template, String channelId, boolean encryptData, Map properties) throws AdaptorException {
		this.properties = properties;
		return doConvertMessage(messageObject, template, channelId, encryptData);
	}

	protected void handleException(Throwable e) throws AdaptorException {
		logger.warn("error adapting message", e);
		messageObjectController.setError(messageObject, Constants.ERROR_301, "Error adapting message", e);
		throw new AdaptorException(e);
	}

	protected MessageObject doConvertMessage(MessageObject messageObject, String template, String channelId, boolean encryptData) throws AdaptorException {
		return messageObject;
	}

	public IXMLSerializer<String> getSerializer(Map properties) {
		return new DefaultXMLSerializer();
	}

	protected abstract void populateMessage() throws AdaptorException;

}