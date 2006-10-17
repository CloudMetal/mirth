/*
 * $Header: /home/projects/mule/scm/mule/providers/jms/src/java/org/mule/providers/jms/JmsMessageReceiver.java,v 1.26 2005/11/02 12:09:24 rossmason Exp $
 * $Revision: 1.26 $
 * $Date: 2005/11/02 12:09:24 $
 * -----------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 */
package org.mule.providers.jms;

import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.ConnectException;
import org.mule.providers.jms.filters.JmsSelectorFilter;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.lifecycle.LifecycleException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.util.PropertiesHelper;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

/**
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author <a href=mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.26 $
 *
 */
public class JmsMessageReceiver extends AbstractMessageReceiver implements MessageListener
{

    protected JmsConnector connector;
    protected RedeliveryHandler redeliveryHandler;
    protected MessageConsumer consumer;
    protected Session session;


    public JmsMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint)
            throws InitialisationException
    {
        super(connector, component, endpoint);
        this.connector = (JmsConnector) connector;

        try {
            redeliveryHandler = this.connector.createRedeliveryHandler();
            redeliveryHandler.setConnector(this.connector);
        } catch (Exception e) {
            throw new InitialisationException(e, this);
        }
    }

    public void doConnect() throws Exception
    {
		createConsumer();
    }

    public void doDisconnect() throws Exception
    {
    	closeConsumer();
    }

    public void onMessage(Message message)
    {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Message received it is of type: " + message.getClass().getName());
                if (message.getJMSDestination() != null) {
                    logger.debug("Message received on " + message.getJMSDestination() + " ("
                            + message.getJMSDestination().getClass().getName() + ")");
                } else {
                    logger.debug("Message received on unknown destination");
                }
                logger.debug("Message CorrelationId is: " + message.getJMSCorrelationID());
                logger.debug("Jms Message Id is: " + message.getJMSMessageID());
            }

            if (message.getJMSRedelivered()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Message with correlationId: " + message.getJMSCorrelationID()
                            + " is redelivered. handing off to Exception Handler");
                }
                redeliveryHandler.handleRedelivery(message);
            }

            UMOMessageAdapter adapter = connector.getMessageAdapter(message);
            routeMessage(new MuleMessage(adapter));
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void doStart() throws UMOException {
        try {
            consumer.setMessageListener(this);
        } catch (JMSException e) {
            throw new LifecycleException(e, this);
        }
    }

    public void doStop() throws UMOException {
        try {
            consumer.setMessageListener(null);
        } catch (JMSException e) {
            throw new LifecycleException(e, this);
        }
    }

    protected void closeConsumer()
    {
        JmsUtils.closeQuietly(consumer);
        consumer = null;
        JmsUtils.closeQuietly(session);
        session = null;
    }

    /**
     * Create a consumer for the jms destination
     *
     * @throws Exception
     */
    protected void createConsumer() throws Exception
    {
    	try {
	        JmsSupport jmsSupport = this.connector.getJmsSupport();
	        // Create session if none exists
	        if (session == null) {
	    		session = this.connector.getSession(endpoint);
	        }

	        // Create destination
	        String resourceInfo = endpoint.getEndpointURI().getResourceInfo();
	        boolean topic = (resourceInfo != null && "topic".equalsIgnoreCase(resourceInfo));

            //todo MULE20 remove resource Info support
            if(!topic) topic = PropertiesHelper.getBooleanProperty(endpoint.getProperties(), "topic", false);

	        Destination dest = jmsSupport.createDestination(session, endpoint.getEndpointURI().getAddress(), topic);

	        // Extract jms selector
	        String selector = null;
	        if (endpoint.getFilter() != null && endpoint.getFilter() instanceof JmsSelectorFilter) {
	            selector = ((JmsSelectorFilter) endpoint.getFilter()).getExpression();
	        } else if (endpoint.getProperties() != null) {
	            // still allow the selector to be set as a property on the endpoint
	            // to be backward compatable
	            selector = (String) endpoint.getProperties().get(JmsConstants.JMS_SELECTOR_PROPERTY);
	        }
	        String tempDurable = (String) endpoint.getProperties().get("durable");
	        boolean durable = connector.isDurable();
	        if (tempDurable != null)
	            durable = Boolean.valueOf(tempDurable).booleanValue();

	        // Get the durable subscriber name if there is one
	        String durableName = (String) endpoint.getProperties().get("durableName");
	        if (durableName == null && durable && dest instanceof Topic) {
	            durableName = "mule." + connector.getName() + "." + endpoint.getEndpointURI().getAddress();
	            logger.debug("Jms Connector for this receiver is durable but no durable name has been specified. Defaulting to: "
	                    + durableName);
	        }

	        // Create consumer
	        consumer = jmsSupport.createConsumer(session, dest, selector, connector.isNoLocal(), durableName);
    	} catch (JMSException e) {
    		throw new ConnectException(e, this);
    	}
    }

}
