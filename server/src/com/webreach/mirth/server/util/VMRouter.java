package com.webreach.mirth.server.util;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleEvent;
import org.mule.impl.MuleMessage;
import org.mule.impl.MuleSession;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.DispatchException;
import org.mule.util.queue.Queue;
import org.mule.util.queue.QueueSession;

import com.webreach.mirth.connectors.vm.VMConnector;
import com.webreach.mirth.connectors.vm.VMMessageReceiver;
import com.webreach.mirth.model.MessageObject;

public class VMRouter {
    private static transient Log logger = LogFactory.getLog(VMRouter.class);

	public void routeMessage(String channelName, String message) {
		routeMessage(channelName, message, true);
	}

	public void routeMessage(String channelName, String message, boolean useQueue) {
		String channelId = ChannelController.getChannelId(channelName);
		routeMessageByChannelId(channelId, message, useQueue, true);
	}

	public void routeMessage(String channelName, String message, boolean useQueue, boolean synchronised) {
		String channelId = ChannelController.getChannelId(channelName);
		routeMessageByChannelId(channelId, message, useQueue, synchronised);
	}

    public void routeMessageByChannelId(String channelId, Object message, boolean useQueue, boolean synchronised) {
        MessageObject messageObject = (MessageObject) message;
        UMOMessage umoMessage = new MuleMessage(messageObject.getRawData());

        // set the properties from the context
        for (Iterator iterator = messageObject.getContext().entrySet().iterator(); iterator.hasNext();) {
            Entry entry = (Entry) iterator.next();
            umoMessage.setProperty(entry.getKey(), entry.getValue());
        }

        VMMessageReceiver receiver = VMRegistry.getInstance().get(channelId);
        UMOEvent event = new MuleEvent(umoMessage, receiver.getEndpoint(), new MuleSession(), false);

        try {
            doDispatch(event, receiver, useQueue, synchronised);
        } catch (Exception e) {
            logger.error("Unable to route: " + e.getMessage());
        }
    }

    private void doDispatch(UMOEvent event, VMMessageReceiver receiver, boolean useQueue, boolean synchronised) throws Exception {
        UMOEndpointURI endpointUri = event.getEndpoint().getEndpointURI();

        if (endpointUri == null) {
            throw new DispatchException(new Message(Messages.X_IS_NULL, "Endpoint"), event.getMessage(), event.getEndpoint());
        }
        
        if (useQueue) {
            QueueSession session = ((VMConnector) receiver.getConnector()).getQueueSession();
            Queue queue = session.getQueue(endpointUri.getAddress());
            queue.put(event);
        } else {
            if (receiver == null) {
                logger.warn("No receiver for endpointUri: " + event.getEndpoint().getEndpointURI());
                return;
            }
            
            receiver.routeMessage(event.getMessage(), synchronised);
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("dispatched Event on endpointUri: " + endpointUri);
        }
    }
}
