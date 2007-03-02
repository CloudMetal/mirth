/*
 * $Header: /home/projects/mule/scm/mule/providers/tcp/src/java/org/mule/providers/tcp/TcpConnector.java,v 1.11 2005/11/05 12:23:27 aperepel Exp $
 * $Revision: 1.11 $
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
package org.mule.providers.mllp;

import org.mule.config.i18n.Message;
import org.mule.management.stats.ComponentStatistics;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.providers.mllp.protocols.LlpProtocol;
import org.mule.providers.tcp.protocols.DefaultProtocol;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.util.ClassHelper;

import org.mule.MuleManager;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.config.QueueProfile;
import org.mule.impl.model.AbstractComponent;
import org.mule.util.queue.Queue;
import org.mule.util.queue.QueueManager;
import org.mule.util.queue.QueueSession;
import org.mule.umo.UMOComponent;
import org.mule.umo.provider.UMOMessageReceiver;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.server.controllers.SystemLogger;

/**
 * <code>TcpConnector</code> can bind or sent to a given tcp port on a given
 * host.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author <a href="mailto:tsuppari@yahoo.co.uk">P.Oikari</a>
 * 
 * @version $Revision: 1.11 $
 */
public class MllpConnector extends AbstractServiceEnabledConnector {
	// custom properties
	public static final String PROPERTY_CHAR_ENCODING = "charEncoding";
	public static final String PROPERTY_START_OF_MESSAGE = "messageStart";
	public static final String PROPERTY_END_OF_MESSAGE = "messageEnd";
	public static final String PROPERTY_RECORD_SEPARATOR = "recordSeparator";
	public static final String PROPERTY_END_OF_SEGMENT = "segmentEnd";
	public static final String PROPERTY_TEMPLATE = "template";
	public static final String PROPERTY_CHECKMSH15 = "checkMSH15";
    public static final String PROPERTY_ACK_NEW_CONNECTION = "ackOnNewConnection";
    public static final String PROPERTY_ACK_NEW_CONNECTION_IP = "ackIP";
    public static final String PROPERTY_ACK_NEW_CONNECTION_PORT = "ackPort";
    public static final String PROPERTY_REPLY_CHANNEL_ID = "replyChannelId";
    
    public static final String PROPERTY_TRANSFORMER_ACK = "responseFromTransformer";
	// custom properties
	private String charEncoding = "hex";
	private String messageStart = "0x1C";
	private String messageEnd = "0x0B";
	private String recordSeparator = "0x0D";
	private String segmentEnd = "0x0D";
	private String template = "message.encodedData";
	private boolean checkMSH15 = false;
	private boolean ackOnNewConnection = false;
	private String ackIP = "";
	private String ackPort = "";
	private String replyChannelId = "";
	private boolean responseFromTransformer = false;
	// ack properties
	public static final String PROPERTY_ACKCODE_SUCCESSFUL = "ackCodeSuccessful";
	public static final String PROPERTY_ACKMSG_SUCCESSFUL = "ackMsgSuccessful";
	
	public static final String PROPERTY_ACKCODE_ERROR = "ackCodeError";
	public static final String PROPERTY_ACKMSG_ERROR = "ackMsgError";
	
	public static final String PROPERTY_ACKCODE_REJECTED = "ackCodeRejected";
	public static final String PROPERTY_ACKMSG_REJECTED = "ackMsgRejected";
	
	private String ackCodeSuccessful = "AA";
	private String ackMsgSuccessful = "";
	private String ackCodeError = "AE";
	private String ackMsgError = "Error Processing Message";
	private String ackCodeRejected = "AR";
	private String ackMsgRejected = "Message Rejected";
	
	public static final int DEFAULT_SOCKET_TIMEOUT = 5000;
	public static final int DEFAULT_ACK_TIMEOUT = 5000;
	public static final int DEFAULT_BUFFER_SIZE = 64 * 1024;
	public static final long DEFAULT_POLLING_FREQUENCY = 10;
	public static final int DEFAULT_BACKLOG = 256;
	private int sendTimeout = DEFAULT_SOCKET_TIMEOUT;
	private int receiveTimeout = DEFAULT_SOCKET_TIMEOUT;
	private int bufferSize = DEFAULT_BUFFER_SIZE;
	private int backlog = DEFAULT_BACKLOG;
	private boolean sendACK = false;
	private String tcpProtocolClassName = DefaultProtocol.class.getName();
	private LlpProtocol llpProtocol;

	// ast: Queue variables
	private boolean usePersistentQueues = false;
	private int maxQueues = 16;
	private QueueProfile queueProfile;
	private UMOComponent component = null;
	private int ackTimeout = DEFAULT_ACK_TIMEOUT;
        //ast: encoding Charset
        public static final String PROPERTY_CHARSET_ENCODING = "charsetEncoding";
        public static final String CHARSET_KEY = "ca.uhn.hl7v2.llp.charset";
        public static final String DEFAULT_CHARSET_ENCODING =System.getProperty(CHARSET_KEY, java.nio.charset.Charset.defaultCharset().name());
        private String charsetEncoding = DEFAULT_CHARSET_ENCODING;

	// /////////////////////////////////////////////
	// Does this protocol have any connected sockets?
	// /////////////////////////////////////////////
	private boolean sendSocketValid = false;
	private int receiveSocketsCount = 0;

	// //////////////////////////////////////////////////////////////////////
	// Properties for 'keepSocketConnected' TcpMessageDispatcher
	// //////////////////////////////////////////////////////////////////////
	public static final int KEEP_RETRYING_INDEFINETLY = 100;
	public static final int DEFAULT_RETRY_TIMES = 100;
	private boolean keepSendSocketOpen = false;

	// Time to sleep between reconnects in msecs
	private int reconnectMillisecs = 10000;

	// -1 try to reconnect forever
	private int maxRetryCount = DEFAULT_RETRY_TIMES;
	private boolean keepAlive = true;

        //ast: overload of the creator, to allow the test of the charset Encoding
        public MllpConnector(){
            super();
            ////ast: try to set the default encoding
            this.setCharsetEncoding(DEFAULT_CHARSET_ENCODING);            
        }
        
	public boolean isKeepSendSocketOpen() {
		return keepSendSocketOpen;
	}

	public void setKeepSendSocketOpen(boolean keepSendSocketOpen) {
		this.keepSendSocketOpen = keepSendSocketOpen;
	}

	public int getReconnectMillisecs() {
		return reconnectMillisecs;
	}

	public void setReconnectMillisecs(int reconnectMillisecs) {
		this.reconnectMillisecs = reconnectMillisecs;
	}

	public int getMaxRetryCount() {
		return maxRetryCount;
	}

	public void setMaxRetryCount(int maxRetryCount) {
		// Dont set negative numbers
		if (maxRetryCount >= KEEP_RETRYING_INDEFINETLY) {
			this.maxRetryCount = maxRetryCount;
		} else if (maxRetryCount < 0) {
			this.maxRetryCount = 0;
		} else {
			this.maxRetryCount = maxRetryCount;
		}
	}

	// //////////////////////////////////////////////////////////////////////
	public void doInitialise() throws InitialisationException {
		super.doInitialise();
		if (llpProtocol == null) {
			try {
				llpProtocol = new LlpProtocol();
				llpProtocol.setTcpConnector(this);
			} catch (Exception e) {
				throw new InitialisationException(new Message("mllp", 3), e);
			}
		}
		// ast: configure the queue (if selected)
		if (isQueueEvents() && (queueProfile == null)) {
			queueProfile = MuleManager.getConfiguration().getQueueProfile();
		}
	}

	public String getProtocol() {
		return "MLLP";
	}

	/**
	 * A shorthand property setting timeout for both SEND and RECEIVE sockets.
	 */
	public void setTimeout(int timeout) {
		setSendTimeout(timeout);
		setReceiveTimeout(timeout);
	}

	public int getSendTimeout() {
		return this.sendTimeout;
	}

	public boolean getSendACK() {
		return sendACK;
	}

	public void setSendACK(boolean ack) {
		sendACK = ack;
	}

	public void setSendTimeout(int timeout) {
		if (timeout < 0) {
			timeout = DEFAULT_SOCKET_TIMEOUT;
		}
		this.sendTimeout = timeout;
	}

	// ////////////////////////////////////////////
	// New independednt Socket timeout for receiveSocket
	// ////////////////////////////////////////////
	public int getReceiveTimeout() {
		return receiveTimeout;
	}

	public void setReceiveTimeout(int timeout) {
		if (timeout < 0)
			timeout = DEFAULT_SOCKET_TIMEOUT;
		this.receiveTimeout = timeout;
	}

	public boolean isSendSocketValid() {
		return sendSocketValid;
	}

	public void setSendSocketValid(boolean validity) {
		this.sendSocketValid = validity;
	}

	public boolean hasReceiveSockets() {
		return receiveSocketsCount > 0;
	}

	/**
	 * Update the number of receive sockets.
	 * 
	 * @param addSocket
	 *            increase the number if true, decrement otherwise
	 */
	public synchronized void updateReceiveSocketsCount(boolean addSocket) {
		if (addSocket) {
			this.receiveSocketsCount++;
		} else {
			this.receiveSocketsCount--;
		}
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		if (bufferSize < 1)
			bufferSize = DEFAULT_BUFFER_SIZE;
		this.bufferSize = bufferSize;
	}

	public int getBacklog() {
		return backlog;
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	public LlpProtocol getLlpProtocol() {
		return llpProtocol;
	}

	public void setLlpProtocol(LlpProtocol llpProtocol) {
		this.llpProtocol = llpProtocol;
	}

	public String getTcpProtocolClassName() {
		return tcpProtocolClassName;
	}

	public void setTcpProtocolClassName(String protocolClassName) {
		this.tcpProtocolClassName = protocolClassName;
	}

	public boolean isRemoteSyncEnabled() {
		return true;
	}

	public String getCharEncoding() {
		return this.charEncoding;
	}

	public void setCharEncoding(String charEncoding) {
		this.charEncoding = charEncoding;
	}

	public String getMessageEnd() {
		return this.messageEnd;
	}

	public void setMessageEnd(String messageEnd) {
		this.messageEnd = messageEnd;
	}

	public String getMessageStart() {
		return this.messageStart;
	}

	public void setMessageStart(String messageStart) {
		this.messageStart = messageStart;
	}

	public String getRecordSeparator() {
		return this.recordSeparator;
	}

	public void setRecordSeparator(String recordSeparator) {
		this.recordSeparator = recordSeparator;
	}

	public String getSegmentEnd() {
		return this.segmentEnd;
	}

	public void setSegmentEnd(String segmentEnd) {
		this.segmentEnd = segmentEnd;
	}

	public char stringToChar(String source) {
		return source.charAt(0);
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

        //ast: set the charset Encoding
        public void setCharsetEncoding(String charsetEncoding) {                
                if ((charsetEncoding==null) || (charsetEncoding=="") || (charsetEncoding.equalsIgnoreCase("DEFAULT_ENCODING"))) charsetEncoding=DEFAULT_CHARSET_ENCODING;
                logger.debug("FileConnector: trying to set the encoding to "+charsetEncoding);                
		try{
                    byte b[]={20,21,22,23};
                    String k=new String(b,charsetEncoding);
                    this.charsetEncoding = charsetEncoding;
                }catch(Exception e){
                    //set the encoding to the default one: this charset can't launch an exception
                    this.charsetEncoding=java.nio.charset.Charset.defaultCharset().name();
                    logger.error("Impossible to use ["+charsetEncoding+"] as the Charset Encoding: changing to the platform default ["+this.charsetEncoding+"]");
                    SystemLogger systemLogger = new SystemLogger();                  
                    SystemEvent event = new SystemEvent("Exception occured in channel.");
                    event.setDescription("Impossible to use ["+charsetEncoding+"] as the Charset Encoding: changing to the platform default ["+this.charsetEncoding+"]");
                    systemLogger.logSystemEvent(event);
                }
	}
        //ast: get the charset Encoding
        public String getCharsetEncoding() {
            if ((this.charsetEncoding==null) || (this.charsetEncoding =="") || (this.charsetEncoding.equalsIgnoreCase("DEFAULT_ENCODING"))){
                //Default Charset
                return DEFAULT_CHARSET_ENCODING;
            } 
		return(this.charsetEncoding);
	}
        
	/***************************************************************************
	 * ***************ast: Queue functions**********************
	 **************************************************************************/

	public void setAckTimeout(int timeout) {
		if (timeout < 0) {
			timeout = DEFAULT_ACK_TIMEOUT;
		}
		this.ackTimeout = timeout;
	}

	public int getAckTimeout() {
		return (ackTimeout);
	}

	/*
	 * Overload method to avoid error startting the channel after an stop
	 * (non-Javadoc)
	 * 
	 * @see org.mule.providers.AbstractConnector#registerListener(org.mule.umo.UMOComponent,
	 *      org.mule.umo.endpoint.UMOEndpoint)
	 */
	public UMOMessageReceiver registerListener(UMOComponent component, UMOEndpoint endpoint) throws Exception {
		UMOMessageReceiver r = null;
		this.component = component;
		try {
			r = super.registerListener(component, endpoint);
		} catch (org.mule.umo.provider.ConnectorException e) {
			logger.warn("Trying to reconnect a listener: this is not an error with this kind of router");
		}
		return r;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOConnector#registerListener(org.mule.umo.UMOSession,
	 *      org.mule.umo.endpoint.UMOEndpoint)
	 */
	public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception {
		this.component = component;
		if (usePersistentQueues) {
			configureQueues(endpoint);
			return new MllpMessageResponseQueued(this, component, endpoint, (long) 10000);
		} else {
			return super.createReceiver(component, endpoint);

		}

	}

	public String getQueueName(UMOEndpoint endpoint) {
		String adr = "dummy";
		String adr_securefile;
		try {
			//adr = endpoint.getEndpointURI().getAddress();
			adr = this.getName();
		} catch (Throwable t) {
		}
		adr_securefile = adr.replace("\\", "");
		adr_securefile = adr_securefile.replace("/", "");
		adr_securefile = adr_securefile.replace(":", "_");
		adr_securefile = adr_securefile.replace(" ", "_");

		return adr_securefile;
	}

	public String getErrorQueueName(UMOEndpoint endpoint) {
		return "_Error" + getQueueName(endpoint);
	}

	public void configureQueues(UMOEndpoint endpoint) throws Exception {

		try {
			queueProfile.configureQueue(getQueueName(endpoint));
			queueProfile.configureQueue(getErrorQueueName(endpoint));
		} catch (Throwable t) {
			logger.warn("It's impossible to configure a queue for the endooint " + t);
		}
	}

	public boolean isQueueEvents() {
		return usePersistentQueues;
	}

	public void setQueueEvents(boolean usePersistentQueues) {
		this.usePersistentQueues = usePersistentQueues;
	}

	public boolean isUsePersistentQueues() {
		return usePersistentQueues;
	}

	public void setUsePersistentQueues(boolean usePersistentQueues) {
		this.usePersistentQueues = usePersistentQueues;
	}

	public QueueProfile getQueueProfile() {
		return queueProfile;
	}

	public void setQueueProfile(QueueProfile queueProfile) {
		this.queueProfile = queueProfile;
	}

	public void setMaxQueues(int maxQueues) {
		this.maxQueues = maxQueues;
	}

	public int getMaxQueues(int maxQueues) {
		return this.maxQueues;
	}

	public Queue getQueue(UMOEndpoint endpoint) throws InitialisationException {

		QueueSession qs = getQueueSession();
		Queue q = qs.getQueue(getQueueName(endpoint));
		return q;

	}

	public Queue getErrorQueue(UMOEndpoint endpoint) throws InitialisationException {

		QueueSession qs = getQueueSession();
		Queue q = qs.getQueue(getErrorQueueName(endpoint));
		return q;

	}

	public QueueSession getQueueSession() throws InitialisationException {

		QueueManager qm = MuleManager.getInstance().getQueueManager();

		logger.debug("Retrieving new queue session from queue manager " + this.getName());

		QueueSession session = qm.getQueueSession();

		return session;
	}

	public void incErrorStatistics() {
		incErrorStatistics(component);
	}

	public void incErrorStatistics(UMOComponent umoComponent) {
		ComponentStatistics statistics = null;
		
		if (umoComponent != null)
			component = umoComponent;

		if (component == null) {
			return;
		}
		
		if (!(component instanceof AbstractComponent)) {
			return;
		}
			
		try {
			statistics = ((AbstractComponent) component).getStatistics();
			if (statistics == null) {
				return;
			}
			statistics.incExecutionError();
		} catch (Throwable t) {
			logger.error("Error setting statistics ");
		}
	}

	public String getAckCodeError() {
		return ackCodeError;
	}

	public void setAckCodeError(String ackCodeError) {
		this.ackCodeError = ackCodeError;
	}

	public String getAckCodeRejected() {
		return ackCodeRejected;
	}

	public void setAckCodeRejected(String ackCodeRejected) {
		this.ackCodeRejected = ackCodeRejected;
	}

	public String getAckCodeSuccessful() {
		return ackCodeSuccessful;
	}

	public void setAckCodeSuccessful(String ackCodeSuccessful) {
		this.ackCodeSuccessful = ackCodeSuccessful;
	}

	public String getAckMsgError() {
		return ackMsgError;
	}

	public void setAckMsgError(String ackMsgError) {
		this.ackMsgError = ackMsgError;
	}

	public String getAckMsgRejected() {
		return ackMsgRejected;
	}

	public void setAckMsgRejected(String ackMsgRejected) {
		this.ackMsgRejected = ackMsgRejected;
	}

	public String getAckMsgSuccessful() {
		return ackMsgSuccessful;
	}

	public void setAckMsgSuccessful(String ackMsgSuccessful) {
		this.ackMsgSuccessful = ackMsgSuccessful;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public boolean isCheckMSH15() {
		return checkMSH15;
	}

	public void setCheckMSH15(boolean checkMSH15) {
		this.checkMSH15 = checkMSH15;
	}

	public boolean isAckOnNewConnection() {
		return ackOnNewConnection;
	}

	public void setAckOnNewConnection(boolean ackOnNewConnection) {
		this.ackOnNewConnection = ackOnNewConnection;
	}

	public String getAckIP() {
		return ackIP;
	}

	public void setAckIP(String ackIP) {
		this.ackIP = ackIP;
	}

	public String getAckPort() {
		return ackPort;
	}

	public void setAckPort(String ackPort) {
		this.ackPort = ackPort;
	}

	public String getReplyChannelId() {
		return replyChannelId;
	}

	public void setReplyChannelId(String replyChannelId) {
		this.replyChannelId = replyChannelId;
	}

	public boolean isResponseFromTransformer() {
		return responseFromTransformer;
	}

	public void setResponseFromTransformer(boolean responseFromTransformer) {
		this.responseFromTransformer = responseFromTransformer;
	}
}
