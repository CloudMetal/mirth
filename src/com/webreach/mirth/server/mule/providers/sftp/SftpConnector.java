package com.webreach.mirth.server.mule.providers.sftp;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.providers.VariableFilenameParser;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.ConnectorException;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageReceiver;

import com.jcraft.jsch.ChannelSftp;
import com.webreach.mirth.server.mule.providers.file.FilenameParser;

public class SftpConnector extends AbstractServiceEnabledConnector {
	public static final String PROPERTY_ORIGINAL_FILENAME = "originalFilename";
	public static final String PROPERTY_POLLING_FREQUENCY = "pollingFrequency";
	public static final String PROPERTY_FILENAME = "filename";
	public static final String PROPERTY_OUTPUT_PATTERN = "outputPattern";
	public static final String PROPERTY_TEMPLATE = "template";
	public static final String PROPERTY_BINARY = "binary";
	public static final String PROPERTY_FILE_AGE = "fileAge";
	public static final String PROPERTY_FILE_FILTER = "fileFilter";
	public static final String PROPERTY_MOVE_TO_PATTERN = "moveToPattern";
	public static final String PROPERTY_MOVE_TO_DIRECTORY = "moveToDirectory";
	public static final String PROPERTY_DELETE_ON_READ = "autoDelete";
	public static final String PROPERTY_DIRECTORY = "directory";
	public static final String PROPERTY_SORT_ATTRIBUTE = "sortAttribute";
	public static final String PROPERTY_BATCH_PROCESS = "processBatchFiles";
	public static final String PROPERTY_CHANNEL_ID = "channelId";
	public static final String SORT_NAME = "name";;
	public static final String SORT_DATE = "date";
	public static final String SORT_SIZE = "size";

	// ast: encoding Charset
	public static final String PROPERTY_CHARSET_ENCODING = "charsetEncoding";
	public static final String CHARSET_KEY = "ca.uhn.hl7v2.llp.charset";
	public static final String DEFAULT_CHARSET_ENCODING = System.getProperty(CHARSET_KEY, java.nio.charset.Charset.defaultCharset().name());

	private String username;
	private String password;
	private long pollingFrequency = 0;
	private String outputPattern = null;
	private String template = null;
	private FilenameParser filenameParser = new VariableFilenameParser();
	private Map pools = new HashMap();
	private boolean binary;
	private String moveToPattern = null;
	private String writeToDirectoryName = null;
	private String moveToDirectory = null;
	private String sortAttribute = SORT_NAME;
	private boolean outputAppend = false;
	private boolean autoDelete = true;
	private boolean checkFileAge = false;
	private String fileFilter = "*.*";
	private long fileAge = 0;
	private boolean processBatchFiles = true;
	private String channelId;
	private String charsetEncoding = DEFAULT_CHARSET_ENCODING;

	public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception {
		long polling = pollingFrequency;
		Map props = endpoint.getProperties();

		if (props != null) {
			// Override properties on the endpoint for the specific endpoint
			String tempPolling = (String) props.get(PROPERTY_POLLING_FREQUENCY);

			if (tempPolling != null) {
				polling = Long.parseLong(tempPolling);
			}
		}

		if (polling <= 0) {
			polling = 1000;
		}

		logger.debug("set polling frequency to: " + polling);
		return serviceDescriptor.createMessageReceiver(this, component, endpoint, new Object[] { new Long(polling) });
	}

	public ChannelSftp getClient(UMOEndpointURI uri) throws Exception {
		ObjectPool pool = getClientPool(uri);
		return (ChannelSftp) pool.borrowObject();
	}

	public void releaseClient(UMOEndpointURI uri, ChannelSftp client) throws Exception {
		if (isCreateDispatcherPerRequest()) {
			destroyClient(uri, client);
			UMOMessageDispatcher dispatcher = getDispatcher(uri.toString());
		} else {
			if (client != null && client.isConnected()) {
				ObjectPool pool = getClientPool(uri);
				pool.returnObject(client);
			}
		}
	}
	public void SftpConnector(){
		filenameParser = new VariableFilenameParser();
	}
	public void destroyClient(UMOEndpointURI uri, ChannelSftp client) throws Exception {
		if ((client != null) && (client.isConnected())) {
			ObjectPool pool = getClientPool(uri);
			pool.invalidateObject(client);
		}
	}

	protected synchronized ObjectPool getClientPool(UMOEndpointURI uri) {
		String key = uri.getUsername() + ":" + uri.getPassword() + "@" + uri.getHost() + ":" + uri.getPort();
		ObjectPool pool = (ObjectPool) pools.get(key);

		if (pool == null) {
			pool = new GenericObjectPool(new SftpConnectionFactory(uri, username, password));
			pools.put(key, pool);
		}

		return pool;
	}

	protected void doStop() throws UMOException {
		try {
			for (Iterator iter = pools.values().iterator(); iter.hasNext();) {
				ObjectPool pool = (ObjectPool) iter.next();
				pool.close();
			}
		} catch (Exception e) {
			throw new ConnectorException(new Message(Messages.FAILED_TO_STOP_X, "FTP Connector"), this, e);
		}
	}

	public String getProtocol() {
		return "sftp";
	}

	public FilenameParser getFilenameParser() {
		return this.filenameParser;
	}

	public void setFilenameParser(FilenameParser filenameParser) {
		this.filenameParser = filenameParser;
	}

	public String getOutputPattern() {
		return this.outputPattern;
	}

	public void setOutputPattern(String outputPattern) {
		this.outputPattern = outputPattern;
	}

	public long getPollingFrequency() {
		return this.pollingFrequency;
	}

	public void setPollingFrequency(long pollingFrequency) {
		this.pollingFrequency = pollingFrequency;
	}

	public String getTemplate() {
		return this.template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isBinary() {
		return binary;
	}

	public void setBinary(boolean binary) {
		this.binary = binary;
	}

	public boolean isAutoDelete() {
		return autoDelete;
	}

	public void setAutoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
	}

	public boolean isCheckFileAge() {
		return checkFileAge;
	}

	public void setCheckFileAge(boolean checkFileAge) {
		this.checkFileAge = checkFileAge;
	}

	public long getFileAge() {
		return fileAge;
	}

	public void setFileAge(long fileAge) {
		this.fileAge = fileAge;
	}

	public String getMoveToDirectory() {
		return moveToDirectory;
	}

	public void setMoveToDirectory(String moveToDirectory) {
		this.moveToDirectory = moveToDirectory;
	}

	public String getMoveToPattern() {
		return moveToPattern;
	}

	public void setMoveToPattern(String moveToPattern) {
		this.moveToPattern = moveToPattern;
	}

	public boolean isProcessBatchFiles() {
		return processBatchFiles;
	}

	public void setProcessBatchFiles(boolean processBatchFiles) {
		this.processBatchFiles = processBatchFiles;
	}

	public String getSortAttribute() {
		return sortAttribute;
	}

	public void setSortAttribute(String sortAttribute) {
		this.sortAttribute = sortAttribute;
	}

	public String getWriteToDirectoryName() {
		return writeToDirectoryName;
	}

	public void setWriteToDirectoryName(String writeToDirectoryName) {
		this.writeToDirectoryName = writeToDirectoryName;
	}

	public String getFileFilter() {
		return fileFilter;
	}

	public void setFileFilter(String fileFilter) {
		this.fileFilter = fileFilter;
	}

	public boolean isOutputAppend() {
		return outputAppend;
	}

	public void setOutputAppend(boolean outputAppend) {
		this.outputAppend = outputAppend;
	}

	public String getCharsetEncoding() {
		return charsetEncoding;
	}

	public void setCharsetEncoding(String charsetEncoding) {
		this.charsetEncoding = charsetEncoding;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

}
