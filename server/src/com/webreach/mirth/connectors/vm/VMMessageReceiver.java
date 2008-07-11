/* 
 * $Header: /home/projects/mule/scm/mule/providers/vm/src/java/org/mule/providers/vm/VMMessageReceiver.java,v 1.19 2005/10/23 15:14:35 holger Exp $
 * $Revision: 1.19 $
 * $Date: 2005/10/23 15:14:35 $
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */
package com.webreach.mirth.connectors.vm;

import java.util.List;

import org.mule.MuleException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.providers.TransactedPollingMessageReceiver;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.util.queue.Queue;
import org.mule.util.queue.QueueSession;

import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;
import com.webreach.mirth.server.mule.transformers.JavaScriptPostprocessor;
import com.webreach.mirth.server.util.VMRegistry;

/**
 * <code>VMMessageReceiver</code> is a listener of events from a mule
 * component which then simply <p/> passes the events on to the target
 * component.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.19 $
 */
public class VMMessageReceiver extends TransactedPollingMessageReceiver {
	private VMConnector vmConnector;
	private String componentName;
	private Object lock = new Object();
    private MonitoringController monitoringController = MonitoringController.getInstance();
    private JavaScriptPostprocessor postProcessor = new JavaScriptPostprocessor();
    private ConnectorType connectorType = ConnectorType.LISTENER;
    
	public VMMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint) throws InitialisationException {
		super(connector, component, endpoint, new Long(10));
		this.vmConnector = (VMConnector) connector;
		componentName = component.getDescriptor().getName() + "_source_connector";
		receiveMessagesInTransaction = endpoint.getTransactionConfig().isTransacted();
		VMRegistry.getInstance().register(this.getEndpointURI().getAddress(), this);
	}
	
	public void doConnect() throws Exception {
		if (vmConnector.isQueueEvents()) {
			// Ensure we can create a vm queue
			QueueSession queueSession = vmConnector.getQueueSession();
			queueSession.getQueue(endpoint.getEndpointURI().getAddress());
		}
		monitoringController.updateStatus(componentName, connectorType,  Event.INITIALIZED, null);
	}
	
	public void doDisconnect() throws Exception {}

	public void doStop() throws UMOException{
		super.doStop();
		monitoringController.updateStatus(componentName, connectorType, Event.DISCONNECTED, null);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.UMOEventListener#onEvent(org.mule.umo.UMOEvent)
	 */
	public void onEvent(UMOEvent event) throws UMOException {
		monitoringController.updateStatus(componentName, connectorType, Event.BUSY, null);
		if (vmConnector.isQueueEvents()) {
			QueueSession queueSession = vmConnector.getQueueSession();
			Queue queue = queueSession.getQueue(endpoint.getEndpointURI().getAddress());
			try {
				queue.put(event);
			} catch (InterruptedException e) {
				throw new MuleException(new Message(Messages.INTERRUPTED_QUEUING_EVENT_FOR_X, this.endpoint.getEndpointURI()), e);
			} catch (Exception e) { 
				throw new MuleException(e);
			}
		} else {
			try{
			UMOMessage msg = new MuleMessage(event.getTransformedMessage(), event.getProperties());
			if (msg != null){
				postProcessor.doPostProcess(msg.getPayload());
			}
			synchronized (lock) {
				routeMessage(msg);
			}
			}catch (UMOException e){
				throw e;
			}finally{
				monitoringController.updateStatus(componentName, connectorType, Event.DONE, null);
			}
		}
	}

	public Object dispatchMessage(UMOEvent event) throws UMOException {
		monitoringController.updateStatus(componentName, connectorType, Event.BUSY, null);
		try{
			UMOMessage umoMessage = routeMessage(new MuleMessage(event.getMessage(), event.getProperties(), event.getMessage()), event.isSynchronous());
			if (umoMessage != null){
				postProcessor.doPostProcess(umoMessage.getPayload());
			}
			return umoMessage;
		}catch (UMOException e){
			throw e;
		}finally{
			monitoringController.updateStatus(componentName, connectorType, Event.DONE, null);
		}		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.UMOSyncChainSupport#onCall(org.mule.umo.UMOEvent)
	 */
	public Object onCall(UMOEvent event) throws UMOException {
		monitoringController.updateStatus(componentName, connectorType, Event.BUSY, null);
		try{
			UMOMessage umoMessage = routeMessage(new MuleMessage(event.getTransformedMessage(), event.getProperties(), event.getMessage()), event.isSynchronous());
			if (umoMessage != null){
				postProcessor.doPostProcess(umoMessage.getPayload());
			}
			return umoMessage;
		}catch (UMOException e){
			throw e;
		}finally{
			monitoringController.updateStatus(componentName, connectorType, Event.DONE, null);
		}		
	}

	protected List getMessages() throws Exception {
		QueueSession qs = vmConnector.getQueueSession();
		Queue queue = qs.getQueue(endpoint.getEndpointURI().getAddress());
		UMOEvent event = (UMOEvent) queue.take();
		//TODO: Check post processor logic on this
		monitoringController.updateStatus(componentName, connectorType, Event.BUSY, null);
		try{
			UMOMessage umoMessage = routeMessage(new MuleMessage(event.getTransformedMessage(), event.getProperties()), true);
			if (umoMessage != null){
				postProcessor.doPostProcess(umoMessage.getPayload());
			}
			monitoringController.updateStatus(componentName, connectorType, Event.DONE, null);
		}catch (UMOException e){
			throw e;
		}finally{
			monitoringController.updateStatus(componentName, connectorType, Event.DONE, null);
		}	
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.providers.TransactionEnabledPollingMessageReceiver#processMessage(java.lang.Object)
	 */
	protected void processMessage(Object msg) throws Exception {
	// This method is never called as the
	// message is processed when received
	}

}
