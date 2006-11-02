/* 
 * $Header: /home/projects/mule/scm/mule/providers/file/src/java/org/mule/providers/file/FileMessageDispatcher.java,v 1.8 2005/11/12 20:55:57 lajos Exp $
 * $Revision: 1.8 $
 * $Date: 2005/11/12 20:55:57 $
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
package org.mule.providers.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URLDecoder;

import org.mule.MuleException;
import org.mule.MuleManager;
import org.mule.config.i18n.Message;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.VariableFilenameParser;
import org.mule.providers.file.filters.FilenameWildcardFilter;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.util.Utility;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.controllers.MessageObjectController;

/**
 * <code>FileMessageDispatcher</code> is used to read/write files to the
 * filesystem and
 * 
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.8 $
 */
public class FileMessageDispatcher extends AbstractMessageDispatcher {
	private FileConnector connector;
	private MessageObjectController messageObjectController = new MessageObjectController();

	public FileMessageDispatcher(FileConnector connector) {
		super(connector);
		this.connector = connector;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOConnectorSession#dispatch(org.mule.umo.UMOEvent)
	 */
	public void doDispatch(UMOEvent event) throws Exception {
		TemplateValueReplacer replacer = new TemplateValueReplacer();
		UMOEndpointURI uri = event.getEndpoint().getEndpointURI();
		FileOutputStream fos = null;

		try {
			Object data = event.getTransformedMessage();

			if (data instanceof MessageObject) {
				MessageObject messageObject = (MessageObject) data;
				if (messageObject.getStatus().equals(MessageObject.Status.REJECTED)){
					return;
				}
				
				String filename = (String) event.getProperty(FileConnector.PROPERTY_FILENAME);

				if (filename == null) {
					String pattern = (String) event.getProperty(FileConnector.PROPERTY_OUTPUT_PATTERN);

					if (pattern == null) {
						pattern = connector.getOutputPattern();
					}

					filename = generateFilename(event, pattern, messageObject);
				}

				if (filename == null) {
					throw new IOException("Filename is null");
				}

				String template = replacer.replaceValues(connector.getTemplate(), messageObject, filename);
				File file = Utility.createFile(uri.getAddress() + "/" + filename);
				byte[] buffer = template.getBytes();

				if (connector.isOutputAppend()) {
					buffer = (new String(buffer) + "\r\n").getBytes();
				}

				logger.info("Writing file to: " + file.getAbsolutePath());
				fos = new FileOutputStream(file, connector.isOutputAppend());
				fos.write(buffer);
				
				// update the message status to sent
				messageObject.setStatus(MessageObject.Status.SENT);
				messageObjectController.updateMessage(messageObject);
			} else {
				logger.warn("received data is not of expected type");
			}
		} catch (Exception e) {
			connector.handleException(e);
		} finally {
			if (fos != null) {
				fos.close();	
			}
		}
	}

	/**
	 * Will attempt to do a receive from a directory, if the endpointUri
	 * resolves to a file name the file will be returned, otherwise the first
	 * file in the directory according to the filename filter configured on the
	 * connector.
	 * 
	 * @param endpointUri
	 *            a path to a file or directory
	 * @param timeout
	 *            this is ignored when doing a receive on this dispatcher
	 * @return a message containing file contents or null if there was notthing
	 *         to receive
	 * @throws Exception
	 */
	public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
		File file = new File(endpointUri.getAddress());
		File result = null;
		FilenameFilter filenameFilter = null;
		String filter = (String) endpointUri.getParams().get("filter");
		if (filter != null) {
			filter = URLDecoder.decode(filter, MuleManager.getConfiguration().getEncoding());
			filenameFilter = new FilenameWildcardFilter(filter);
		}
		if (file.exists()) {
			if (file.isFile()) {
				result = file;
			} else if (file.isDirectory()) {
				result = getNextFile(endpointUri.getAddress(), filenameFilter);
			}
			if (result != null) {
				boolean checkFileAge = connector.getCheckFileAge();
				if (checkFileAge) {
					long fileAge = connector.getFileAge();
					long lastMod = result.lastModified();
					long now = (new java.util.Date()).getTime();
					if ((now - lastMod) < fileAge) {
						return null;
					}
				}

				MuleMessage message = new MuleMessage(connector.getMessageAdapter(result));
				if (connector.getMoveToDirectory() != null) {
					File destinationFile = new File(connector.getMoveToDirectory(), result.getName());
					if (!result.renameTo(destinationFile)) {
						logger.error("Failed to move file: " + result.getAbsolutePath() + " to " + destinationFile.getAbsolutePath());
					}
				}
				result.delete();
				return message;
			}
		}
		return null;
	}

	private File getNextFile(String dir, FilenameFilter filter) throws UMOException {
		File[] files = new File[] {};
		File file = new File(dir);
		File result = null;
		try {
			if (file.exists()) {
				if (file.isFile()) {
					result = file;
				} else if (file.isDirectory()) {
					if (filter != null) {
						files = file.listFiles(filter);
					} else {
						files = file.listFiles();
					}
					if (files.length > 0) {
						result = files[0];
					}
				}
			}
			return result;
		} catch (Exception e) {
			throw new MuleException(new Message("file", 1), e);
		}
	}

	public UMOMessage doSend(UMOEvent event) throws Exception {
		doDispatch(event);
		return event.getMessage();
	}

	public Object getDelegateSession() throws UMOException {
		return null;
	}

	public void doDispose() {}
	
	private String generateFilename(UMOEvent event, String pattern, MessageObject messageObject) {
		if (connector.getFilenameParser() instanceof VariableFilenameParser) {
			VariableFilenameParser filenameParser = (VariableFilenameParser) connector.getFilenameParser();
			filenameParser.setMessageObject(messageObject);
			return filenameParser.getFilename(event.getMessage(), pattern);
		} else {
			return connector.getFilenameParser().getFilename(event.getMessage(), pattern);
		}
	}
}
