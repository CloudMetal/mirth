/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import org.mule.MuleException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.providers.PollingMessageReceiver;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.MessagingException;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.umo.routing.RoutingException;

import sun.misc.BASE64Encoder;

import com.webreach.mirth.connectors.file.filesystems.FileInfo;
import com.webreach.mirth.connectors.file.filesystems.FileSystemConnection;
import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;
import com.webreach.mirth.server.mule.adaptors.Adaptor;
import com.webreach.mirth.server.mule.adaptors.AdaptorFactory;
import com.webreach.mirth.server.mule.adaptors.BatchAdaptor;
import com.webreach.mirth.server.mule.adaptors.BatchMessageProcessor;
import com.webreach.mirth.server.mule.transformers.JavaScriptPostprocessor;
import com.webreach.mirth.server.util.StackTracePrinter;

/**
 * <code>FileMessageReceiver</code> is a polling listener that reads files
 * from a directory.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.12 $
 */

public class FileMessageReceiver extends PollingMessageReceiver implements BatchMessageProcessor {
	private String readDir = null;
	private String moveDir = null;
	private String errorDir = null;
	private String moveToPattern = null;
	private String filenamePattern = null;
	private boolean routingError = false;

	private AlertController alertController = ControllerFactory.getFactory().createAlertController();
	private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
	private JavaScriptPostprocessor postProcessor = new JavaScriptPostprocessor();
	private TemplateValueReplacer replacer = new TemplateValueReplacer();
	private ConnectorType connectorType = ConnectorType.READER;
	private FileConnector fileConnector = null;
	
	private String originalFilename = null;

	public FileMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint, String readDir, String moveDir, String moveToPattern, String errorDir, Long frequency) throws InitialisationException {
		super(connector, component, endpoint, frequency);
		this.readDir = replacer.replaceValues(readDir);
		this.moveDir = replacer.replaceValues(moveDir);
		this.moveToPattern = replacer.replaceValues(moveToPattern);
		this.errorDir = replacer.replaceValues(errorDir);
		this.fileConnector = (FileConnector) connector;

		if (fileConnector.getPollingType().equals(FileConnector.POLLING_TYPE_TIME))
			setTime(fileConnector.getPollingTime());
		else
			setFrequency(fileConnector.getPollingFrequency());

		filenamePattern = replacer.replaceValues(fileConnector.getFileFilter());
		monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
	}

	public void doConnect() throws Exception {
		FileSystemConnection con = fileConnector.getConnection(getEndpointURI(), null);
		fileConnector.releaseConnection(getEndpointURI(), con, null);
	}

	public void doDisconnect() throws Exception {
	}

	public void poll() {

		monitoringController.updateStatus(connector, connectorType, Event.CONNECTED);
		try {

			FileInfo[] files = listFiles();

			if (files == null) {
				return;
			}

			// sort files by specified attribute before processing
			sortFiles(files);
			routingError = false;
			
			for (int i = 0; i < files.length; i++) {
				//
				if (!routingError && !files[i].isDirectory()) {
					monitoringController.updateStatus(connector, connectorType, Event.BUSY);
					processFile(files[i]);
					monitoringController.updateStatus(connector, connectorType, Event.DONE);
				}
			}
		}
		catch (Exception e) {
			
			alertController.sendAlerts(((FileConnector) connector).getChannelId(), Constants.ERROR_403, null, e);
			handleException(e);
		}
		finally {
			
			monitoringController.updateStatus(connector, connectorType, Event.DONE);
		}
	}

	public void sortFiles(FileInfo[] files) {
		String sortAttribute = ((FileConnector) connector).getSortAttribute();

		if (sortAttribute.equals(FileConnector.SORT_DATE)) {
			Arrays.sort(files, new Comparator<FileInfo>() {
				public int compare(FileInfo file1, FileInfo file2) {
					return Float.compare(file1.getLastModified(), file2.getLastModified());
				}
			});
		} else if (sortAttribute.equals(FileConnector.SORT_SIZE)) {
			Arrays.sort(files, new Comparator<FileInfo>() {
				public int compare(FileInfo file1, FileInfo file2) {
					return Float.compare(file1.getSize(), file2.getSize());
				}
			});
		} else {
			Arrays.sort(files, new Comparator<FileInfo>() {
				public int compare(FileInfo file1, FileInfo file2) {
					return file1.getName().compareTo(file2.getName());
				}
			});
		}
	}

	/** Converts the supplied message into a MuleMessage and routes it.
	 * @param message The message to be converted and routed.
	 * @throws MessagingException, UMOException
	 */
	public void processBatchMessage(String message)
		throws MessagingException, UMOException
	{
		UMOMessageAdapter messageAdapter = connector.getMessageAdapter(message);
		messageAdapter.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, originalFilename);
		UMOMessage umoMessage = routeMessage(new MuleMessage(messageAdapter), endpoint.isSynchronous());
		if (umoMessage != null) {
			postProcessor.doPostProcess(umoMessage.getPayload());
		}
	}

	public synchronized void processFile(FileInfo file) throws UMOException {
		
		boolean checkFileAge = fileConnector.isCheckFileAge();
		if (checkFileAge) {
			long fileAge = fileConnector.getFileAge();
			long lastMod = file.getLastModified();
			long now = (new java.util.Date()).getTime();
			if ((now - lastMod) < fileAge)
				return;
		}
		
		String destinationDir = null;
		String destinationName = null;
		originalFilename = file.getName();
		UMOMessageAdapter adapter = connector.getMessageAdapter(file);
		adapter.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, originalFilename);

		if (moveDir != null) {
			destinationName = file.getName();

			if (moveToPattern != null) {
				destinationName = fileConnector.getFilenameParser().getFilename(adapter, moveToPattern);
			}

			destinationDir = moveDir;
		}

		boolean resultOfFileMoveOperation = false;

		try {
			// Perform some quick checks to make sure file can be processed
			if (file.isDirectory()) {
				// ignore directories
			} else if (!(file.isReadable() && file.isFile())) {
				// it's either not readable, or something odd like a link */
				throw new MuleException(new Message(Messages.FILE_X_DOES_NOT_EXIST, file.getName()));
			} else {
				
				Exception fileProcessedException = null;
				
				try {

					// ast: use the user-selected encoding
					if (fileConnector.isProcessBatchFiles()) {
						processBatch(file);
					} else {
						byte[] contents = getBytesFromFile(file);
						String message = "";
						if (fileConnector.isBinary()) {
							BASE64Encoder encoder = new BASE64Encoder();
							message = encoder.encode(contents);
						} else {
							message = new String(contents, fileConnector.getCharsetEncoding());
						}
						adapter = connector.getMessageAdapter(message);
						adapter.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, originalFilename);
						UMOMessage umoMessage = routeMessage(new MuleMessage(adapter), endpoint.isSynchronous());
						if (umoMessage != null) {
							postProcessor.doPostProcess(umoMessage.getPayload());
						}
					}
				} catch (RoutingException e) {
					logger.error("Unable to route." + StackTracePrinter.stackTraceToString(e));
					
					// routingError is reset to false at the beginning of the poll method
					routingError = true;
					
					if (errorDir != null) {
						logger.error("Moving file to error directory: " + errorDir);
						destinationDir = errorDir;
						destinationName = file.getName();
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
					fileProcessedException = new MuleException(new Message(Messages.FAILED_TO_READ_PAYLOAD, file.getName()));
				}

				// move the file if needed
				if (destinationDir != null) {
					deleteFile(destinationName, destinationDir, true);

					resultOfFileMoveOperation = renameFile(file.getName(), readDir, destinationName, destinationDir);

					if (!resultOfFileMoveOperation) {
						throw new MuleException(new Message("file", 4, pathname(file.getName(), readDir), pathname(destinationName, destinationDir)));
					}
				}
				else if (fileConnector.isAutoDelete()) {
					//adapter.getPayloadAsBytes();

					// no moveTo directory
					if (destinationDir == null) {
						
						resultOfFileMoveOperation = deleteFile(file.getName(), readDir, false);

						if (!resultOfFileMoveOperation) {
							throw new MuleException(new Message("file", 3, pathname(file.getName(), readDir)));
						}
					}
				}
				
				if (fileProcessedException != null) {
					throw fileProcessedException;
				}
			}
		} catch (Exception e) {
			alertController.sendAlerts(((FileConnector) connector).getChannelId(), Constants.ERROR_403, "", e);
			handleException(e);
		}
	}

	/** Convert a directory path and a filename into a pathname */
	private String pathname(String name, String dir) {

		if (dir != null && dir.length() > 0) {

			return dir + "/" + name;
		}
		else {
			
			return name;
		}
	}

	/** Process a single file as a batched message source */
	private void processBatch(FileInfo file) throws Exception{
		UMOEndpointURI uri = endpoint.getEndpointURI();
		Protocol protocol = Protocol.valueOf(fileConnector.getInboundProtocol());
		Adaptor adaptor = AdaptorFactory.getAdaptor(protocol);

		if (adaptor instanceof BatchAdaptor) {
			BatchAdaptor batchAdaptor = (BatchAdaptor) adaptor;
			FileSystemConnection con = fileConnector.getConnection(uri, null);
			Reader in = null;
			try {
				in = new InputStreamReader(con.readFile(file.getName(), readDir), fileConnector.getCharsetEncoding());
				Map protocolProperties = fileConnector.getProtocolProperties();
				protocolProperties.put("batchScriptId", fileConnector.getChannelId());
				batchAdaptor.processBatch(in, fileConnector.getProtocolProperties(), this);
			}
			finally {
				if (in != null) {
					in.close();
				}
				con.closeReadFile();
				fileConnector.releaseConnection(uri, con, null);
			}
		} else {
		    throw new Exception("Data type " + protocol + " does not support batch processing.", e);
		}
	}

	/** Delete a file */
	private boolean deleteFile(String name, String dir, boolean mayNotExist) throws Exception {
		
		UMOEndpointURI uri = endpoint.getEndpointURI();
		FileSystemConnection con = fileConnector.getConnection(uri, null);
		try {
			
			con.delete(name, dir, mayNotExist);
			return true;
		}
		catch (Exception e) {

			if (mayNotExist) {
				return true;
			}
			else {
				logger.info("Unable to delete destination file");
				return false;
			}
		}
		finally {
			fileConnector.releaseConnection(uri, con, null);
		}
	}

	private boolean renameFile(String fromName, String fromDir, String toName, String toDir) throws Exception {
		
		UMOEndpointURI uri = endpoint.getEndpointURI();
		FileSystemConnection con = fileConnector.getConnection(uri, null);
		try {

			con.move(fromName, fromDir, toName, toDir);
			return true;
		}
		catch (Exception e) {

			return false;
		}
		finally {
			fileConnector.releaseConnection(uri, con, null);
		}
	}

	// Returns the contents of the file in a byte array.
	private byte[] getBytesFromFile(FileInfo file) throws Exception {
		
		UMOEndpointURI uri = endpoint.getEndpointURI();
		FileSystemConnection con = fileConnector.getConnection(uri, null);

		try {
			InputStream is = con.readFile(file.getName(), readDir);
	
			// Get the size of the file
			long length = file.getSize();
	
			// You cannot create an array using a long type.
			// It needs to be an int type.
			// Before converting to an int type, check
			// to ensure that file is not larger than Integer.MAX_VALUE.
			if (length > Integer.MAX_VALUE) {
				// File is too large
				// TODO: throw new ??Exception("Implementation restriction: file too large.");
			}
	
			// Create the byte array to hold the data
			byte[] bytes = new byte[(int) length];
	
			// Read in the bytes
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}
	
			// Ensure all the bytes have been read in
			if (offset < bytes.length) {
				throw new IOException("Could not completely read file " + file.getName());
			}
	
			// Close the input stream and return bytes
			is.close();
			con.closeReadFile();
			return bytes;
		}
		finally {
			fileConnector.releaseConnection(uri, con, null);
		}
	}

	/**
	 * Get a list of files to be processed.
	 * 
	 * @return a list of files to be processed.
	 * @throws org.mule.MuleException
	 *             which will wrap any other exceptions or errors.
	 */
	FileInfo[] listFiles() throws Exception {
		
		UMOEndpointURI uri = endpoint.getEndpointURI();
		FileSystemConnection con = fileConnector.getConnection(uri, null);

		try {
			return con.listFiles(readDir, filenamePattern).toArray(new FileInfo[0]);
		}
		finally {
			fileConnector.releaseConnection(uri, con, null);
		}
	}

	public boolean isRoutingError() {
		return routingError;
	}

	public void setRoutingError(boolean routingError) {
		this.routingError = routingError;
	}

}
