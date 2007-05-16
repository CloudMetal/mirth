/*
 * $Header: /home/projects/mule/scm/mule/providers/tcp/src/java/org/mule/providers/tcp/TcpMessageDispatcher.java,v 1.12 2005/11/05 12:23:27 aperepel Exp $
 * $Revision: 1.12 $
 * $Date: 2005/11/05 12:23:27 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.server.mule.providers.mllp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.UMOConnector;
import org.mule.util.queue.Queue;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.mule.providers.mllp.protocols.LlpProtocol;
import com.webreach.mirth.server.util.BatchMessageProcessor;
import com.webreach.mirth.server.util.VMRouter;

/**
 * f <code>TcpMessageDispatcher</code> will send transformed mule events over
 * tcp.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author <a href="mailto:tsuppari@yahoo.co.uk">P.Oikari</a>
 * 
 * @version $Revision: 1.12 $
 */

public class MllpMessageDispatcher extends AbstractMessageDispatcher {
	// ///////////////////////////////////////////////////////////////
	// keepSocketOpen option variables
	// ///////////////////////////////////////////////////////////////

	protected Socket connectedSocket = null;

	// ast:queue variables
	protected Queue queue = null;

	protected Queue errorQueue = null;

	// ///////////////////////////////////////////////////////////////
	/**
	 * logger used by this class
	 */
	protected static transient Log logger = LogFactory.getLog(MllpMessageDispatcher.class);

	private MllpConnector connector;
	private MessageObjectController messageObjectController = MessageObjectController.getInstance();
	private AlertController alertController = new AlertController();
	private TemplateValueReplacer replacer = new TemplateValueReplacer();

	public MllpMessageDispatcher(MllpConnector connector) {
		super(connector);
		this.connector = connector;
	}

	// ast: set queues
	public void setQueues(UMOEndpoint endpoint) {
		// connect to the queues
		this.queue = null;
		this.errorQueue = null;

		if (connector.isUsePersistentQueues() && (endpoint != null)) {
			try {
				this.queue = connector.getQueue(endpoint);
				this.errorQueue = connector.getErrorQueue(endpoint);
			} catch (Exception e) {
				logger.error("Error setting queues to the endpoint" + e);
			}
		}
	}

	protected Socket initSocket(String endpoint) throws IOException, URISyntaxException {
		URI uri = new URI(endpoint);
		int port = uri.getPort();
		InetAddress inetAddress = InetAddress.getByName(uri.getHost());
		Socket socket = createSocket(port, inetAddress);
		socket.setReuseAddress(true);
		socket.setReceiveBufferSize(connector.getBufferSize());
		socket.setSendBufferSize(connector.getBufferSize());
		socket.setSoTimeout(connector.getSendTimeout());
		return socket;
	}

	// ast:Code changes to allow queues
	/*
	 * As doSend is never called, all the changes are made to the doSispatch
	 * method
	 */
	public void doDispatch(UMOEvent event) throws Exception {
		Socket socket = null;
		Object payload = null;
		boolean success = false;
		Exception exceptionWriting = null;
		String exceptionMessage = "";
		MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);
		if (messageObject == null) {
			return;
		}
		this.setQueues(event.getEndpoint());
		// ast: now, the stuff for queueing (and re-try)
		try {
			if (queue != null) {
				try {
					// The status should be queued, even if we are retrying
					messageObjectController.setQueued(messageObject, "Message is queued");
					queue.put(messageObject);
					return;
				} catch (Exception exq) {
					exceptionMessage = "Can't save payload to queue";
					logger.error("Can't save payload to queue\r\n\t " + exq);
					exceptionWriting = exq;
					success = false;
				}
			} else {
				int retryCount = -1;
				int maxRetries = connector.getMaxRetryCount();
				while (!success && !disposed && (retryCount < maxRetries)) {
					retryCount++;
					try {
						socket = initSocket(event.getEndpoint().getEndpointURI().getAddress());
						writeTemplatedData(socket, messageObject);
						success = true;
					} catch (Exception exs) {
						if (retryCount < maxRetries) {
							logger.warn("Can't connect to the endopint,waiting" + new Float(connector.getReconnectMillisecs() / 1000) + "seconds for reconnecting \r\n(" + exs + ")");
							try {
								Thread.sleep(connector.getReconnectMillisecs());
							} catch (Throwable t) {
								exceptionMessage = "Unable to send message. Too many retries";
								logger.error("Sending interrupption. Payload not sent");
								retryCount = maxRetries + 1;
								exceptionWriting = exs;
							}
						} else {
							exceptionMessage = "Unable to connect to destination";
							logger.error("Can't connect to the endopint: payload not sent");
							exceptionWriting = exs;

						}
					}
				}
			}
		} catch (Exception exu) {
			exceptionMessage = exu.getMessage();
			alertController.sendAlerts(((MllpConnector) connector).getChannelId(), Constants.ERROR_408, null, exu);
			logger.error("Unknown exception dispatching " + exu);
			exceptionWriting = exu;
		} 
		if (!success) {
			messageObjectController.setError(messageObject, Constants.ERROR_408, exceptionMessage, exceptionWriting);
		}
		if (success && (exceptionWriting == null)) {
			manageResponseAck(socket, event.getEndpoint(), messageObject);
		}
	}

	protected Socket createSocket(int port, InetAddress inetAddress) throws IOException {
		return new Socket(inetAddress, port);
	}

	protected void write(Socket socket, byte[] data) throws IOException {
		LlpProtocol protocol = connector.getLlpProtocol();
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		protocol.write(bos, data);
		bos.flush();
	}

	protected void write(Socket socket, MessageObject messageObject) throws Exception {
		byte[] data = messageObject.getEncodedData().getBytes(connector.getCharsetEncoding());
		LlpProtocol protocol = connector.getLlpProtocol();
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		protocol.write(bos, data);
		bos.flush();
	}

	protected void write(Socket socket, String data) throws Exception {
		LlpProtocol protocol = connector.getLlpProtocol();
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		protocol.write(bos, data.getBytes(connector.getCharsetEncoding()));
		bos.flush();
	}

	// ast: split the doSend code into three functions: sendPayload (sending)
	// and doTheRemoteSyncStuff (for remote-sync)

	public UMOMessage doSend(UMOEvent event) throws Exception {
		doDispatch(event);
		return event.getMessage();
	}

	// ast: sendPayload is called from the doSend method, or from
	// MessageResponseQueued
	public boolean sendPayload(MessageObject data, UMOEndpoint endpoint) throws Exception {
		Boolean result = false;
		Exception sendException = null;
		if (this.queue == null)
			this.queue = connector.getQueue(endpoint);
		try {
			if (!connector.isKeepSendSocketOpen()) {
				try {
					connectedSocket = initSocket(endpoint.getEndpointURI().getAddress());
				} catch (Throwable tnf) {
					connectedSocket = null;
				}
			}
			// reconnect(endpoint, connector.getMaxRetryCount());
			result = reconnect(endpoint, 1);
			if (!result)
				return result;
			try {
				// Send the templated data
				writeTemplatedData(connectedSocket, data);
				result = true;
				// If we're doing sync receive try and read return info from
				// socket
			} catch (IOException e) {
				logger.warn("Write raised exception: '" + e.getMessage() + "' attempting to reconnect.");
				doDispose();
				try {
					if (reconnect(endpoint, connector.getMaxRetryCount())) {
						write(connectedSocket, data);
						result = true;
					}
				} catch (Exception ers) {
					logger.warn("Write raised exception: '" + e.getMessage() + "' ceasing reconnecting.");
					sendException = ers;
				}
			}
		} catch (Exception e) {
			sendException = e;
		}
		
		if ((result == false) || (sendException != null)) {
			if (sendException != null) {
				messageObjectController.setError(data, Constants.ERROR_408, "Socket write exception", sendException);
				throw sendException;
			}
			return result;
		}
		// If we have reached this point, the conections has been fine
		manageResponseAck(connectedSocket, endpoint, data);
		if (!connector.isKeepSendSocketOpen()) {
			doDispose();
		}
		return result;
	}

	private void writeTemplatedData(Socket socket, MessageObject data) throws Exception {
		if (!connector.getTemplate().equals("")) {
			String template = replacer.replaceValues(connector.getTemplate(), data);
			write(socket, template);
		} else {
			write(socket, data.getEncodedData());
		}
	}

	// ast: for sync
	public UMOMessage doTheRemoteSyncStuff(UMOEndpoint endpoint) {
		return doTheRemoteSyncStuff(connectedSocket, endpoint);
	}

	public void manageResponseAck(Socket socket, UMOEndpoint endpoint, MessageObject messageObject) {
		int maxTime = connector.getAckTimeout();
		if (maxTime <= 0) { // TODO: Either make a UI setting to "not check for
			// ACK" or document this
			// We aren't waiting for an ACK
			messageObjectController.setSuccess(messageObject, "Message successfully sent");
			
			return;
		}
		byte[] theAck = getAck(socket, endpoint);

		if (theAck == null) {
			// NACK
			messageObjectController.setError(messageObject,Constants.ERROR_408,  "Timeout waiting for ACK", null);
			alertController.sendAlerts(((MllpConnector) connector).getChannelId(), Constants.ERROR_408, null, null);
			return;
		}
		try {
			String ackString = new String(theAck, connector.getCharsetEncoding());
			if (connector.getReplyChannelId() != null & !connector.getReplyChannelId().equals("") && !connector.getReplyChannelId().equals("sink")) {
				// reply back to channel
				VMRouter router = new VMRouter();
				router.routeMessageByChannelId(connector.getReplyChannelId(), ackString, true);
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			messageObjectController.setError(messageObject,Constants.ERROR_408,  "Error setting encoding: " + connector.getCharsetEncoding(), e);
			alertController.sendAlerts(((MllpConnector) connector).getChannelId(), Constants.ERROR_408, null, e);
		}
		String ackString = null;
		try {
			ackString = processResponseData(theAck);
		} catch (Throwable t) {
			logger.error("Error processing the Ack" + t);
		}
		if (ackString == null) {
			// NACK
			messageObjectController.setError(messageObject, Constants.ERROR_408, "ACK message violates LLP protocol", null);
			alertController.sendAlerts(((MllpConnector) connector).getChannelId(), Constants.ERROR_408, null, null);
			return;
		}
		ResponseAck rack = new ResponseAck(ackString);
		if (rack.getTypeOfAck()) {// Ack Ok
			messageObjectController.setSuccess(messageObject, ackString);
		} else {
			messageObjectController.setError(messageObject, Constants.ERROR_408, "NACK sent from receiver: " + rack.getErrorDescription() + ": " + ackString, null);
			alertController.sendAlerts(((MllpConnector) connector).getChannelId(), Constants.ERROR_408, null, null);
		}
	}

	public byte[] getAck(Socket socket, UMOEndpoint endpoint) {
		int maxTime = endpoint.getRemoteSyncTimeout();
		if (maxTime == 0)
			return null;
		try {
			byte[] result = receive(socket, maxTime);
			if (result == null) {
				return null;
			}
			return result;
		} catch (SocketTimeoutException e) {
			// we don't necessarily expect to receive a response here
			logger.warn("Socket timed out normally while doing a synchronous receive on endpointUri: " + endpoint.getEndpointURI());
			return null;
		} catch (Exception ex) {
			logger.info("Socket error while doing a synchronous receive on endpointUri: " + endpoint.getEndpointURI());
			return null;
		}
	}

	// ast:for syncronous
	public UMOMessage doTheRemoteSyncStuff(Socket socket, UMOEndpoint endpoint) {
		try {
			byte[] result = receive(socket, endpoint.getRemoteSyncTimeout());
			if (result == null) {
				return null;
			}
			return new MuleMessage(connector.getMessageAdapter(result));
		} catch (SocketTimeoutException e) {
			// we don't necessarily expect to receive a response here
			logger.info("Socket timed out normally while doing a synchronous receive on endpointUri: " + endpoint.getEndpointURI());
			return null;
		} catch (Exception ex) {
			logger.info("Socket error while doing a synchronous receive on endpointUri: " + endpoint.getEndpointURI());
			return null;
		}
	}

	// Function similar to the Receiver
	protected String processResponseData(byte[] data) throws Exception {

		char START_MESSAGE, END_MESSAGE, END_OF_RECORD, END_OF_SEGMENT;

		if (connector.getCharEncoding().equals("hex")) {
			START_MESSAGE = (char) Integer.decode(connector.getMessageStart()).intValue();
			END_MESSAGE = (char) Integer.decode(connector.getMessageEnd()).intValue();
			END_OF_RECORD = (char) Integer.decode(connector.getRecordSeparator()).intValue();
			END_OF_SEGMENT = (char) Integer.decode(connector.getSegmentEnd()).intValue();
		} else {
			START_MESSAGE = connector.getMessageStart().charAt(0);
			END_MESSAGE = connector.getMessageEnd().charAt(0);
			END_OF_RECORD = connector.getRecordSeparator().charAt(0);
			END_OF_SEGMENT = connector.getSegmentEnd().charAt(0);
		}

		// The next lines, removes any '\n' (decimal code 10 or 0x0A) in the
		// message
		byte[] bites = data;
		int destPointer = 0, srcPointer = 0;
		for (destPointer = 0, srcPointer = 0; srcPointer < bites.length; srcPointer++) {
			if (bites[srcPointer] != 10) {
				bites[destPointer] = bites[srcPointer];
				destPointer++;
			}
		}
		data = bites;
		// ast: use the user's encoding
		String str_data = new String(data, 0, destPointer, connector.getCharsetEncoding());

		BatchMessageProcessor batchProcessor = new BatchMessageProcessor();
		batchProcessor.setEndOfMessage((byte) END_MESSAGE);
		batchProcessor.setStartOfMessage((byte) START_MESSAGE);
		batchProcessor.setEndOfRecord((byte) END_OF_RECORD);
		Iterator<String> it = batchProcessor.processHL7Messages(str_data).iterator();
		if (it.hasNext()) {
			data = (it.next()).getBytes();
			return new String(data);
		}
		return null;
	}

	protected byte[] receive(Socket socket, int timeout) throws IOException {
		DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		if (timeout >= 0) {
			socket.setSoTimeout(timeout);
		}
		return connector.getLlpProtocol().read(dis);
	}

	public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
		Socket socket = null;
		try {
			socket = initSocket(endpointUri.getAddress());
			try {
				byte[] result = receive(socket, (int) timeout);
				if (result == null) {
					return null;
				}
				UMOMessage message = new MuleMessage(connector.getMessageAdapter(result));
				return message;
			} catch (SocketTimeoutException e) {
				// we dont necesarily expect to receive a resonse here
				logger.info("Socket timed out normally while doing a synchronous receive on endpointUri: " + endpointUri);
				return null;
			}
		} finally {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		}
	}

	public Object getDelegateSession() throws UMOException {
		return null;
	}

	public UMOConnector getConnector() {
		return connector;
	}

	public void doDispose() {
		if (null != connectedSocket && !connectedSocket.isClosed()) {
			try {
				connectedSocket.close();

				connectedSocket = null;
			} catch (IOException e) {
				logger.warn("ConnectedSocked close raised exception. Reason: " + e.getMessage());
			}
		}
	}

	// ///////////////////////////////////////////////////////////////
	// New keepSocketOpen option methods by P.Oikari
	// ///////////////////////////////////////////////////////////////
	public boolean reconnect(UMOEndpoint endpoint, int maxRetries) throws Exception {
		if (null != connectedSocket) {
			// We already have a connected socket
			return true;
		}

		boolean success = false;

		int retryCount = -1;

		while (!success && !disposed && (retryCount < maxRetries)) {
			try {
				// ast: we now work with the endpoint
				connectedSocket = initSocket(endpoint.getEndpointURI().getAddress());

				success = true;

				connector.setSendSocketValid(true);
			} catch (Exception e) {
				success = false;

				connector.setSendSocketValid(false);

				if (maxRetries != MllpConnector.KEEP_RETRYING_INDEFINETLY) {
					retryCount++;
				}
				// ast: we now work with the endpoint
				logger.warn("run() warning at host: '" + endpoint.getEndpointURI().getAddress() + "'. Reason: " + e.getMessage());

				if (retryCount < maxRetries) {
					try {
						Thread.sleep(connector.getReconnectMillisecs());
					} catch (Exception ex) {
						logger.warn("SocketConnector threadsleep interrupted. Reason: " + ex.getMessage());
					}
				} else {
					throw e;
				}
			}
		}

		return (success);
	}
}
