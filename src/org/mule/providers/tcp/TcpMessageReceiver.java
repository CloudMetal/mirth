/*
 * $Header: /home/projects/mule/scm/mule/providers/tcp/src/java/org/mule/providers/tcp/TcpMessageReceiver.java,v 1.23 2005/11/05 12:23:27 aperepel Exp $
 * $Revision: 1.23 $
 * $Date: 2005/11/05 12:23:27 $
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
package org.mule.providers.tcp;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.impl.ResponseOutputStream;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.ConnectException;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.Disposable;
import org.mule.umo.lifecycle.DisposeException;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.providers.tcp.protocols.*;
import com.webreach.mirth.util.ACKGenerator;

/**
 * <code>TcpMessageReceiver</code> acts like a tcp server to receive socket
 * requests.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author <a href="mailto:tsuppari@yahoo.co.uk">P.Oikari</a>
 *
 * @version $Revision: 1.23 $
 */
public class TcpMessageReceiver extends AbstractMessageReceiver implements Work
{
    protected ServerSocket serverSocket = null;

    public TcpMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint)
            throws InitialisationException
    {
        super(connector, component, endpoint);
    }

    public void doConnect() throws ConnectException
    {
        disposing.set(false);
        URI uri = endpoint.getEndpointURI().getUri();
        try {
            serverSocket = createSocket(uri);
        } catch (Exception e) {
            throw new org.mule.providers.ConnectException(new Message("tcp", 1, uri), e, this);
        }

        try {
            getWorkManager().scheduleWork(this, WorkManager.INDEFINITE, null, null);
        } catch (WorkException e) {
            throw new ConnectException(new Message(Messages.FAILED_TO_SCHEDULE_WORK), e, this);
        }
    }

    public void doDisconnect() throws ConnectException
    {
        // this will cause the server thread to quit
        disposing.set(true);
        try {
	    if (serverSocket != null) {
            	serverSocket.close();
            }
        } catch (IOException e) {
            logger.warn("Failed to close server socket: " + e.getMessage(), e);
        }
    }

    protected ServerSocket createSocket(URI uri) throws Exception
    {
        String host = uri.getHost();
        int backlog = ((TcpConnector) connector).getBacklog();
        if (host == null || host.length() == 0) {
            host = "localhost";
        }
        InetAddress inetAddress = InetAddress.getByName(host);
        if (inetAddress.equals(InetAddress.getLocalHost()) || inetAddress.isLoopbackAddress()
                || host.trim().equals("localhost")) {
            return new ServerSocket(uri.getPort(), backlog);
        } else {
            return new ServerSocket(uri.getPort(), backlog, inetAddress);
        }
    }

    /**
     * Obtain the serverSocket
     */
    public ServerSocket getServerSocket()
    {
        return serverSocket;
    }

    public void run()
    {
        while (!disposing.get()) {
            if (connector.isStarted() && !disposing.get()) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
	            logger.trace("Server socket Accepted on: " + serverSocket.getLocalPort());
                } catch (java.io.InterruptedIOException iie) {
                    logger.debug("Interupted IO doing serverSocket.accept: " + iie.getMessage());
                } catch (Exception e) {
                    if (!connector.isDisposed() && !disposing.get()) {
                        logger.warn("Accept failed on socket: " + e, e);
                        handleException(new ConnectException(e, this));
                    }
                }
                if (socket != null) {
                    try {
                        Work work = createWork(socket);
                        try {
                            getWorkManager().scheduleWork(work, WorkManager.IMMEDIATE, null, null);
                        } catch (WorkException e) {
                            logger.error("Tcp Server receiver Work was not processed: " + e.getMessage(), e);
                        }
                    } catch (SocketException e) {
                        handleException(e);
                    }

                }
            }
        }
    }

    public void release()
    {
    }

    public void doDispose()
    {
        try {
            if (serverSocket != null && !serverSocket.isClosed())
                serverSocket.close();
            serverSocket = null;

        } catch (Exception e) {
            logger.error(new DisposeException(new Message("tcp", 2), e));
        }
        logger.info("Closed Tcp port");
    }

    protected Work createWork(Socket socket) throws SocketException {
        return new TcpWorker(socket);
    }

    protected class TcpWorker implements Work, Disposable
    {
        protected Socket socket = null;
        protected DataInputStream dataIn;
        protected DataOutputStream dataOut;
        protected AtomicBoolean closed = new AtomicBoolean(false);
        protected TcpProtocol protocol;

        public TcpWorker(Socket socket)
        {
            this.socket = socket;

            final TcpConnector tcpConnector = ((TcpConnector) connector);
            this.protocol = tcpConnector.getTcpProtocol();
            tcpConnector.updateReceiveSocketsCount(true);
            try {
                socket.setReceiveBufferSize(tcpConnector.getBufferSize());
                socket.setSendBufferSize(tcpConnector.getBufferSize());
                socket.setSoTimeout(tcpConnector.getReceiveTimeout());
                socket.setTcpNoDelay(true);
                socket.setKeepAlive(tcpConnector.isKeepAlive());
            } catch (SocketException e) {
                logger.error("Failed to set Socket properties: " + e.getMessage(), e);
            }

            logger.info("TCP connection from " + socket.getRemoteSocketAddress().toString() + " on port " + socket.getLocalPort());
        }

        public void release()
        {
            dispose();
        }

        public void dispose()
        {
            closed.set(true);
            try {
                if (socket != null && !socket.isClosed()) {
                    logger.debug("Closing listener: " + socket.getLocalSocketAddress().toString());
                    //socket.shutdownInput();
                    //socket.shutdownOutput();
                    socket.close();
                }
            } catch (IOException e) {
                logger.error("Socket close failed with: " + e);
            }
            finally {
                ((TcpConnector) connector).updateReceiveSocketsCount(false);
            }
        }

        /**
         * Accept requests from a given TCP port
         */
        public void run()
        {
            try {
                dataIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                dataOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                while (!socket.isClosed() && !disposing.get()) {

                    	byte[] b;
			try {
				b = protocol.read(dataIn);
        	            	// end of stream
                	    	if (b == null) {
                	    	    break;
                	    	}

                	    	byte[] result = processData(b);
                	    	if (result != null) {
                	    	    protocol.write(dataOut, result);
                    		}
                    		dataOut.flush();
			} catch (SocketTimeoutException e) {
				if (!socket.getKeepAlive()) {
					break;
				}
			}
                }
            } catch (Exception e) {
                handleException(e);
            } finally {
                dispose();
            }
        }

        protected byte[] processData(byte[] data) throws Exception
        {
		data = fixMshFields(new String(data)).getBytes();

		UMOMessageAdapter adapter = connector.getMessageAdapter(data);
		OutputStream os = new ResponseOutputStream(socket.getOutputStream(), socket);
		UMOMessage returnMessage = routeMessage(new MuleMessage(adapter), endpoint.isSynchronous(), os);
		generateACK(new String(data), os);
		if (returnMessage != null) {
                	return returnMessage.getPayloadAsBytes();
            	} else {
                	return null;
		}
        }

	private void generateACK(String message, OutputStream os) throws Exception, IOException {
		if  (((TcpConnector) connector).getSendACK()){
			String ACK = new ACKGenerator().generateAckResponse(message);
			
			logger.debug("Sending ACK: " + ACK);
			((TcpConnector)connector).getTcpProtocol().write(os, ACK.getBytes());
		}
		}
    }

        public String fixMshFields(String message) {

                if (message.startsWith("<")) {
                        return message;
                }

                String[] segments = message.split("\\r");
                if (segments[0].startsWith("MSH")) {

                        String fieldSep = String.valueOf(segments[0].charAt(3));
                        String recSep   = String.valueOf(segments[0].charAt(4));

                        String[] fields = segments[0].split("\\" + fieldSep, -1);

                        // always at least 12 fields
                        if (fields.length > 8 && fields.length < 12) {
                                logger.warn("MSH in message from " + fields[3] + " only contains " + fields.length + " fields");
                                String[] oldFields = fields;
                                fields = new String[12];
                                System.arraycopy(oldFields, 0, fields, 0, oldFields.length);
                        }
			
                        // default HL7 version to 2.1
                        if (fields[11] == null || fields[11].equals("")) {
                                logger.warn("No HL7 version set in message from " + fields[3]);
                                fields[11] = "2.1";
                        }
			
			if (fields[10] == null || fields[10].equals("")) {
				logger.warn("No MSH-11 set in message from " + fields[3]);
				fields[10] = "P";
			}

                        // fix MSH-9
                        String[] msh9 = fields[8].split("\\" + recSep, -1);
                        if ((msh9[0] == null || msh9[0].equals("")) && (msh9[1].equals("A04") || msh9[1].equals("A08"))) {
                                logger.warn("No MSH-9-1 set in message from " + fields[3]);
                                msh9[0] = "ADT";
                        }

                        // ignore MSH-9-3
                        fields[8] = msh9[0] + recSep + msh9[1];

                        // fix MSH-10
                        if (fields[9] == null || fields[9].equals("")) {
                                logger.warn("No MCID set in message from " + fields[3]);
                                fields[9] = "INVALID_MCID";
                        }

                        // reassemble msh
                        StringBuffer msh = new StringBuffer();
                        for (int i=0; i < fields.length; i++) {
                                if (i != 0) {
                                        msh.append(fieldSep);
                                }
                                msh.append(fields[i]);
                        }
                        segments[0] = msh.toString();

			// specific fixup for one very broken sender
			if (segments[1].matches("^:\\d+PID:.*")) {
				logger.warn("Fixing malformed PID segment in message from " + fields[3]);
				String[] fixPid = segments[1].split("PID:");
				segments[1] = "PID:" + fixPid[1];
			}

                        // reassemble message
                        StringBuffer segs = new StringBuffer();
                        for (int i=0; i < segments.length; i++) {
                                if (i != 0) {
                                        segs.append("\r");
                                }
                                segs.append(segments[i]);
                        }
                        message = segs.toString();
                }

                return message;
        }
	
}
