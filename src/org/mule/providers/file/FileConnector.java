/*

 * $Header: /home/projects/mule/scm/mule/providers/file/src/java/org/mule/providers/file/FileConnector.java,v 1.14 2005/11/12 20:55:57 lajos Exp $ $Revision: 1.14 $ $Date: 2005/11/12 20:55:57 $

 * ------------------------------------------------------------------------------------------------------

 * 

 * Copyright (c) SymphonySoft Limited. All rights reserved. http://www.symphonysoft.com

 * 

 * The software in this package is published under the terms of the BSD style

 * license a copy of which has been included with this distribution in the

 * LICENSE.txt file.

 *  

 */

package org.mule.providers.file;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.config.MuleProperties;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.providers.file.filters.FilenameWildcardFilter;
import org.mule.transformers.simple.ByteArrayToSerializable;
import org.mule.transformers.simple.SerializableToByteArray;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOMessageReceiver;
import org.mule.util.Utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * <code>FileConnector</code> is used for setting up listeners on a directory
 * and for writing files to a directory. The connecotry provides support for
 * defining file output patterns and filters for receiving files.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.14 $
 */

public class FileConnector extends AbstractServiceEnabledConnector
{
    /**
     * logger used by this class
     */
    private static transient Log logger = LogFactory.getLog(FileConnector.class);

    // These are properties that can be overridden on the Receiver by the
    // endpoint
    // declaration
    public static final String PROPERTY_POLLING_FREQUENCY = "pollingFrequency";
    public static final String PROPERTY_FILE_AGE = "fileAge";
    public static final String PROPERTY_FILENAME = "filename";
    public static final String PROPERTY_ORIGINAL_FILENAME = "originalFilename";
    public static final String PROPERTY_OUTPUT_PATTERN = "outputPattern";
    public static final String PROPERTY_MOVE_TO_PATTERN = "moveToPattern";
    public static final String PROPERTY_MOVE_TO_DIRECTORY = "moveToDirectory";
    public static final String PROPERTY_DELETE_ON_READ = "autoDelete";
    public static final String PROPERTY_DIRECTORY = "directory";
    public static final String PROPERTY_TEMPLATE = "template";

    public static final long DEFAULT_POLLING_FREQUENCY = 1000;

    /**
     * Time in milliseconds to poll. On each poll the poll() method is called
     */
    private long pollingFrequency = 0;

    private String moveToPattern = null;

    private String writeToDirectoryName = null;

    private String moveToDirectoryName = null;

    private String outputPattern = null;

    private boolean outputAppend = false;

    private boolean autoDelete = true;

    private boolean checkFileAge = false;

    private long fileAge = 0;
    
    private String template = null;

    private FileOutputStream outputStream = null;

    private boolean serialiseObjects = false;

    public FilenameParser filenameParser = new SimpleFilenameParser();

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.AbstractConnector#doInitialise()
     */
    public FileConnector()
    {
        filenameParser = new SimpleFilenameParser();
    }

    protected Object getReceiverKey(UMOComponent component, UMOEndpoint endpoint) {
        if(endpoint.getFilter()!=null) {
            return endpoint.getEndpointURI().getAddress() + "/" + ((FilenameWildcardFilter)endpoint.getFilter()).getPattern();
        }
        return endpoint.getEndpointURI().getAddress();
    }

    /**
     * Registers a listener for a particular directory The following properties
     * can be overriden in the endpoint declaration
     * <ul>
     * <li>moveToDirectory</li>
     * <li>filterPatterns</li>
     * <li>filterClass</li>
     * <li>pollingFrequency</li>
     * </ul>
     */
    public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception
    {
        String readDir = endpoint.getEndpointURI().getAddress();
        long polling = this.pollingFrequency;

        String moveTo = moveToDirectoryName;
        Map props = endpoint.getProperties();
        if (props != null) {
            // Override properties on the endpoint for the specific endpoint
            String move = (String) props.get(PROPERTY_MOVE_TO_DIRECTORY);
            if (move != null) {
                moveTo = move;
            }
            String tempPolling = (String) props.get(PROPERTY_POLLING_FREQUENCY);
            if (tempPolling != null) {
                polling = Long.parseLong(tempPolling);
            }
            Long tempFileAge = (Long) props.get(PROPERTY_FILE_AGE);
            if (tempFileAge != null) {
                setFileAge(tempFileAge.longValue());
            }
        }
        if (polling <= 0) {
            polling = DEFAULT_POLLING_FREQUENCY;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("set polling frequency to: " + polling);
        }
        try {
            return serviceDescriptor.createMessageReceiver(this, component, endpoint, new Object[] { readDir, moveTo,
                    moveToPattern, new Long(polling) });

        } catch (Exception e) {
            throw new InitialisationException(new Message(Messages.FAILED_TO_CREATE_X_WITH_X,
                                                          "Message Receiver",
                                                          serviceDescriptor.getMessageReceiver()), e, this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOConnector#stop()
     */
    protected synchronized void doStop() throws UMOException
    {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.warn("Failed to close file output stream on stop: " + e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOConnector#getProtocol()
     */
    public String getProtocol()
    {
        return "FILE";
    }

    public FilenameParser getFilenameParser()
    {
        return filenameParser;
    }

    public void setFilenameParser(FilenameParser filenameParser)
    {
        this.filenameParser = filenameParser;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.AbstractConnector#doDispose()
     */
    protected void doDispose()
    {
        try {
            doStop();
        } catch (UMOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @return Returns the moveToDirectoryName.
     */
    public String getMoveToDirectory()
    {
        return moveToDirectoryName;
    }

    /**
     * @param dir The moveToDirectoryName to set.
     */
    public void setMoveToDirectory(String dir) throws IOException
    {
        this.moveToDirectoryName = dir;
    }

    /**
     * @return Returns the outputAppend.
     */
    public boolean isOutputAppend()
    {
        return outputAppend;
    }

    /**
     * @param outputAppend The outputAppend to set.
     */
    public void setOutputAppend(boolean outputAppend)
    {
        this.outputAppend = outputAppend;
    }

    /**
     * @return Returns the outputPattern.
     */
    public String getOutputPattern()
    {
        return outputPattern;
    }

    /**
     * @param outputPattern The outputPattern to set.
     */
    public void setOutputPattern(String outputPattern)
    {
        this.outputPattern = outputPattern;
    }

    /**
     * @return Returns the outputStream.
     */
    public FileOutputStream getOutputStream()
    {
        return outputStream;
    }

    /**
     * @param outputStream The outputStream to set.
     */
    public void setOutputStream(FileOutputStream outputStream)
    {
        this.outputStream = outputStream;
    }

    /**
     * @return Returns the pollingFrequency.
     */
    public long getPollingFrequency()
    {
        return pollingFrequency;
    }

    /**
     * @param pollingFrequency The pollingFrequency to set.
     */
    public void setPollingFrequency(long pollingFrequency)
    {
        this.pollingFrequency = pollingFrequency;
    }

    /**
     * @return Returns the fileAge.
     */
    public long getFileAge()
    {
        return fileAge;
    }

    public boolean getCheckFileAge() {
	return checkFileAge;
    }

    /**
     * @param fileAge The fileAge in seconds to set.
     */
    public void setFileAge(long fileAge)
    {
        this.fileAge = fileAge;
	this.checkFileAge = true;
    }

    /**
     * @return Returns the writeToDirectory.
     */
    public String getWriteToDirectory()
    {
        return writeToDirectoryName;
    }
    /**
     * @return Contents
     */
    public String getTemplate(){
    	return template;
    }
    /**
     * 
     * @param val = template to set
     */
    public void setTemplate(String val){
    	template = val;
    }

    /**
     * @param dir The writeToDirectory to set.
     */
    public void setWriteToDirectory(String dir) throws IOException
    {
        this.writeToDirectoryName = dir;
        if (writeToDirectoryName != null) {
            File writeToDirectory = Utility.openDirectory((writeToDirectoryName));
            if (!(writeToDirectory.canRead()) || !writeToDirectory.canWrite()) {
                throw new IOException("Error on initialization, Write To directory does not exist or is not read/write");
            }
        }
    }

    public boolean isSerialiseObjects()
    {
        return serialiseObjects;
    }

    public void setSerialiseObjects(boolean serialiseObjects)
    {
        // set serialisable transformers on the connector if this is set
        if (serialiseObjects) {
            if (serviceOverrides == null)
                serviceOverrides = new Properties();
            serviceOverrides.setProperty(MuleProperties.CONNECTOR_INBOUND_TRANSFORMER,
                                         ByteArrayToSerializable.class.getName());
            serviceOverrides.setProperty(MuleProperties.CONNECTOR_OUTBOUND_TRANSFORMER,
                                         SerializableToByteArray.class.getName());
        }

        this.serialiseObjects = serialiseObjects;
    }

    public boolean isAutoDelete()
    {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete)
    {
        this.autoDelete = autoDelete;
        if (!autoDelete) {
            if (serviceOverrides == null)
                serviceOverrides = new Properties();
            if (serviceOverrides.getProperty(MuleProperties.CONNECTOR_MESSAGE_ADAPTER) == null)
            	serviceOverrides.setProperty(MuleProperties.CONNECTOR_MESSAGE_ADAPTER, TextLineMessageAdapter.class.getName());
        }
    }

    public String getMoveToPattern()
    {
        return moveToPattern;
    }

    public void setMoveToPattern(String moveToPattern)
    {
        this.moveToPattern = moveToPattern;
    }
}
