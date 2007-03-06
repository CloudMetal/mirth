/* 
 * $Header: /home/projects/mule/scm/mule/providers/http/src/java/org/mule/providers/http/HttpMessageReceiver.java,v 1.46 2005/11/12 09:04:17 rossmason Exp $
 * $Revision: 1.46 $
 * $Date: 2005/11/12 09:04:17 $
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
package org.mule.providers.http;

import org.apache.commons.httpclient.ChunkedInputStream;
import org.mule.config.MuleProperties;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.*;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.ConnectException;
import org.mule.providers.tcp.TcpMessageReceiver;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.util.PropertiesHelper;
import org.mule.util.monitor.Expirable;

import javax.resource.spi.work.Work;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Map.Entry;

/**
 * <code>HttpMessageReceiver</code> is a simple http server that can be used
 * to listen for http requests on a particular port
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.46 $
 */
public class HttpMessageReceiver extends TcpMessageReceiver {
    //private ExpiryMonitor keepAliveMonitor;

    public HttpMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint)
            throws InitialisationException {
        super(connector, component, endpoint);
//        if (((HttpConnector) connector).isKeepAlive()) {
//            keepAliveMonitor = new ExpiryMonitor(1000);
//        }
    }

    protected Work createWork(Socket socket) throws SocketException {
        return new HttpWorker(socket);
    }

    public void doConnect() throws ConnectException {
        //If we already have an endpoint listening on this socket don't try and
        //start another serversocket
        if (shouldConnect()) {
            super.doConnect();
        }
    }

    protected boolean shouldConnect() {
        StringBuffer requestUri = new StringBuffer();
        requestUri.append(endpoint.getProtocol()).append("://");
        requestUri.append(endpoint.getEndpointURI().getHost());
        requestUri.append(":").append(endpoint.getEndpointURI().getPort());
        requestUri.append("*");
        AbstractMessageReceiver[] temp = connector.getReceivers(requestUri.toString());
        for (int i = 0; i < temp.length; i++) {
            AbstractMessageReceiver abstractMessageReceiver = temp[i];
            if (abstractMessageReceiver.isConnected()) {
                return false;
            }
        }
        return true;
    }

    public void doDispose() {
//        if (keepAliveMonitor != null) {
//            keepAliveMonitor.dispose();
//        }
        super.doDispose();
    }

    private class HttpWorker extends TcpWorker implements Expirable {

        public HttpWorker(Socket socket) throws SocketException {
            super(socket);
            boolean keepAlive = ((HttpConnector) connector).isKeepAlive();
            if (keepAlive) {
                socket.setKeepAlive(true);
                socket.setSoTimeout(((HttpConnector) connector).getKeepAliveTimeout());
            }

        }

        public void run() {
            boolean keepAlive = ((HttpConnector) connector).isKeepAlive();
            try {
                dataIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                dataOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                do {
                    Properties headers = new Properties();
                    Object payload = parseRequest(dataIn, headers);
                    if (payload == null) {
                        break;
                    }
                    UMOMessageAdapter adapter = connector.getMessageAdapter(new Object[]{payload, headers});
                    //Removed the keep alive monitoring stuff for now
                    //nstead just wait for the client to disconnect
                    //keepAlive = adapter.getBooleanProperty(HttpConstants.HEADER_KEEP_ALIVE, keepAlive);
//                    if (keepAlive && !keepAliveRegistered) {
//                        keepAliveRegistered = true;
//                        if (keepAliveMonitor != null) {
//                            keepAliveMonitor.addExpirable(((HttpConnector) connector).getKeepAliveTimeout(), this);
//                        } else {
//                            logger.info("Request has Keep alive set but the HttpConnector has keep alive disables");
//                            keepAlive = false;
//                        }
//                    }

                    UMOMessage message = new MuleMessage(adapter);

                    if (logger.isDebugEnabled()) {
                        logger.debug(message.getProperty(HttpConnector.HTTP_REQUEST_PROPERTY));
                    }
                    OutputStream os = new ResponseOutputStream(dataOut, socket);

                    //determine if the request path on this request denotes a different receiver
                    AbstractMessageReceiver receiver = getTargetReceiver(message, endpoint);

                    UMOMessage returnMessage = null;
                    //the respone only needs to be transformed explicitly if A) the request was not served or a null result was returned
                    boolean transformResponse = false;
                    if (receiver != null) {
                        returnMessage = receiver.routeMessage(message, endpoint.isSynchronous(), os);
                        if (returnMessage == null) {
                            returnMessage = new MuleMessage("");
                            transformResponse = true;
                            RequestContext.rewriteEvent(returnMessage);
                        }
                    } else {
                        transformResponse = true;
                        String failedPath = endpoint.getEndpointURI().getScheme() + "://" +
                                endpoint.getEndpointURI().getHost() + ":" + endpoint.getEndpointURI().getPort() +
                                getRequestPath(message);

                        logger.debug("Failed to bind to " + failedPath);
                        returnMessage = new MuleMessage(new Message(Messages.CANNOT_BIND_TO_ADDRESS_X, failedPath).toString());
                        returnMessage.setIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, HttpConstants.SC_NOT_FOUND);
                        RequestContext.setEvent(new MuleEvent(returnMessage, endpoint, new MuleSession(), true));
                    }
                    Object response = returnMessage.getPayload();
                    if(transformResponse) {
                        response = connector.getDefaultResponseTransformer().transform(response);
                    }

                    if (response instanceof byte[]) {
                        dataOut.write((byte[]) response);
                    } else {
                        dataOut.write(response.toString().getBytes());
                    }
                    dataOut.flush();
//                        if (keepAliveMonitor != null) {
//                            keepAliveMonitor.resetExpirable(this);
//                        }
                } while (!socket.isClosed() && !disposing.get() && keepAlive);
                if (logger.isDebugEnabled() && socket.isClosed()) {
                    logger.debug("Peer closed connection");
                }
            } catch (Exception e) {
                handleException(e);
            } finally {
//                if (keepAliveMonitor != null) {
//                    keepAliveMonitor.removeExpirable(this);
//                }
                dispose();
            }
        }

        public void expired() {
            logger.debug("Keep alive timed out");
            dispose();
        }
    }

    protected String getRequestPath(UMOMessage message) {
        String path = (String) message.getProperty(HttpConnector.HTTP_REQUEST_PROPERTY);
        int i = path.indexOf("?");
        if (i > -1) path = path.substring(0, i);
        return path;
    }


    protected AbstractMessageReceiver getTargetReceiver(UMOMessage message, UMOEndpoint endpoint) throws ConnectException {

        String path = (String) message.getProperty(HttpConnector.HTTP_REQUEST_PROPERTY);
        int i = path.indexOf("?");
        if (i > -1) path = path.substring(0, i);

        StringBuffer requestUri = new StringBuffer();
        requestUri.append(endpoint.getProtocol()).append("://");
        requestUri.append(endpoint.getEndpointURI().getHost());
        requestUri.append(":").append(endpoint.getEndpointURI().getPort());
        //first check there is a receive on the root address
        AbstractMessageReceiver receiver = connector.getReceiver(requestUri.toString());
        //If no receiver on the root and there is a request path, look up the received based on the
        //root plus request path
        if (receiver == null && !"/".equals(path)) {
            //remove anything after the last '/'
            int x = path.lastIndexOf("/");
            if (x > 1 && path.indexOf(".") > x) {
                requestUri.append(path.substring(0, x));
            } else {
                requestUri.append(path);
            }
            receiver = connector.getReceiver(requestUri.toString());
        }
        return receiver;
    }

    protected Object parseRequest(InputStream is, Properties p) throws IOException {
        RequestInputStream req = new RequestInputStream(is);
        Object payload = null;
        String startLine = null;
        do {
            try {
                startLine = req.readline();
            } catch (IOException e) {
                logger.debug(e.getMessage());
            }
            if (startLine == null) return null;
        } while (startLine.trim().length() == 0);

        StringTokenizer tokenizer = new StringTokenizer(startLine);
        String method = tokenizer.nextToken();
        String request = tokenizer.nextToken();
        String httpVersion = tokenizer.nextToken();

        p.setProperty(HttpConnector.HTTP_METHOD_PROPERTY, method);
        p.setProperty(HttpConnector.HTTP_REQUEST_PROPERTY, request);
        p.setProperty(HttpConnector.HTTP_VERSION_PROPERTY, httpVersion);

        // Read headers from the request as set them as properties on the event
        readHeaders(req, p);
        StringBuilder propertyString = new StringBuilder();
        for (Iterator<Entry<Object,Object>> iter = p.entrySet().iterator(); iter.hasNext();) {
			Entry<Object,Object> element = iter.next();
			propertyString.append(element.getKey().toString());
			propertyString.append("=");
			propertyString.append(element.getValue());
			if (iter.hasNext()){
				propertyString.append("&");
			}
		}
        if (method.equals(HttpConstants.METHOD_GET)) {
           // payload = request.getBytes();
        	return propertyString.toString().getBytes();
        } else {
            boolean multipart = p.getProperty(HttpConstants.HEADER_CONTENT_TYPE, "").indexOf("multipart/related") > -1;
            String contentLengthHeader = p.getProperty(HttpConstants.HEADER_CONTENT_LENGTH, null);
            String chunkedString = PropertiesHelper.getStringProperty(p, HttpConstants.HEADER_TRANSFER_ENCODING, null);
            boolean chunked = "chunked".equalsIgnoreCase(chunkedString);
            if (contentLengthHeader == null && !chunked) {
                throw new IllegalStateException(HttpConstants.HEADER_CONTENT_LENGTH + " header must be set");
            }

            if (chunked) {
                byte[] buffer = new byte[1024 * 32];
                int length = -1;
                ChunkedInputStream cis = new ChunkedInputStream(req);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int offset = cis.read(buffer);
                if(offset==-1) return null;
                baos.write(buffer, 0, offset);
                while (offset >= 0) {
                    length = cis.read(buffer, offset, buffer.length);
                    if (length == -1) {
                        break;
                    }
                    baos.write(buffer, offset, length);
                    offset += length;
                }
                payload = baos.toByteArray();
            } else {
                int contentLength = Integer.parseInt(contentLengthHeader);

                if (multipart) {
                    byte[] buffer = new byte[1024];

                    payload = File.createTempFile("mime", ".att");
                    ((File) payload).deleteOnExit();
                    FileOutputStream os = new FileOutputStream((File) payload);

                    int length = -1;
                    int offset = 0;
                    while (offset != contentLength) {
                        buffer = new byte[1024];
                        length = is.read(buffer);
                        if (length != -1) {
                            os.write(buffer, 0, length);
                            offset += length;
                        }
                    }
                    os.close();
                } else {
                    byte[] buffer = new byte[contentLength];

                    int length = -1;
                    int offset = req.read(buffer);
                    while (offset >= 0 && offset < buffer.length) {
                        length = req.read(buffer, offset, buffer.length - offset);
                        if (length == -1) {
                            break;
                        }
                        offset += length;
                    }
                    payload = buffer;
                }
            }
        }
        if (payload != null && payload instanceof byte[]){
        	propertyString.append("&");
        	propertyString.append(new String((byte[])payload));
        	payload = propertyString.toString().getBytes();
        }
        
        return payload;
    }

    private void readHeaders(RequestInputStream is, Properties p) throws IOException {
        String currentKey = null;
        while (true) {
            String line = is.readline();
            if ((line == null) || (line.length() == 0)) {
                break;
            }

            if (!Character.isSpaceChar(line.charAt(0))) {
                int index = line.indexOf(':');
                if (index >= 0) {
                    currentKey = line.substring(0, index).trim();
                    if (currentKey.startsWith("X-" + MuleProperties.PROPERTY_PREFIX)) {
                        currentKey = currentKey.substring(2);
                    } else {
                        // normalize incoming header if necessary
                        String normalizedKey = (String) HttpConstants.ALL_HEADER_NAMES.get(currentKey);
                        if (normalizedKey != null) {
                            currentKey = normalizedKey;
                        }
                    }
                    String value = line.substring(index + 1).trim();
                    p.setProperty(currentKey, value);
                }
            } else if (currentKey != null) {
                String value = p.getProperty(currentKey);
                p.setProperty(currentKey, value + "\r\n\t" + line.trim());
            }
        }

    }
}
