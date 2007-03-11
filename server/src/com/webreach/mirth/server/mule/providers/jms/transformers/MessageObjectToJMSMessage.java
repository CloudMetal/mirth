package com.webreach.mirth.server.mule.providers.jms.transformers;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.util.StackTracePrinter;

public class MessageObjectToJMSMessage extends AbstractJmsTransformer {
	private MessageObjectController messageObjectController = new MessageObjectController();
    private static transient Log logger = LogFactory.getLog(MessageObjectToJMSMessage.class);

	public Object doTransform(Object src) throws TransformerException {
		
		if (src instanceof MessageObject) {
			MessageObject messageObject = (MessageObject) src;
			if (messageObject.getStatus().equals(MessageObject.Status.FILTERED)){
				return null;
			}
		
			Message message = transformToMessage(messageObject.getEncodedData());
            try {
            	message.setStringProperty("MIRTH_MESSAGE_ID", messageObject.getId());
            } catch (JMSException e) {
                //Various Jms servers have slightly different rules to what can be set as an object property on the message
                //As such we have to take a hit n' hope approach
                if(logger.isDebugEnabled()) logger.debug("Unable to set property '" + encodeHeader("MIRTH_MESSAGE_ID") + "': " + e.getMessage());
            }
			return message;
		}	
		return null;
	}
}
