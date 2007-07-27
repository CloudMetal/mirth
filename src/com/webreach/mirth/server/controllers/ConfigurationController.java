/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.UUID;
import java.util.Map.Entry;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.Configuration;
import com.webreach.mirth.model.DriverInfo;
import com.webreach.mirth.model.ServerConfiguration;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.Command;
import com.webreach.mirth.server.CommandQueue;
import com.webreach.mirth.server.builders.MuleConfigurationBuilder;
import com.webreach.mirth.server.tools.ClassPathResource;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.server.util.JMXConnection;
import com.webreach.mirth.server.util.JMXConnectionFactory;
import com.webreach.mirth.server.util.JavaScriptUtil;
import com.webreach.mirth.server.util.SqlConfig;
import com.webreach.mirth.util.Encrypter;
import com.webreach.mirth.util.PropertyLoader;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 * @author geraldb
 * 
 */
public class ConfigurationController {
	private Logger logger = Logger.getLogger(this.getClass());
	private SystemLogger systemLogger = SystemLogger.getInstance();
	public static String mirthHomeDir = new File(ClassPathResource.getResourceURI("mirth.properties")).getParentFile().getParent();
	public static File serverPropertiesFile = new File(mirthHomeDir + System.getProperty("file.separator") + "server.properties");
	public static File serverIdPropertiesFile = new File(mirthHomeDir + System.getProperty("file.separator") + "server.id");
	private static Properties versionProperties = PropertyLoader.loadProperties("version");
	private static SecretKey encryptionKey = null;
	private static String serverId = null;
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();
	private static final String CHARSET = "ca.uhn.hl7v2.llp.charset";
	private boolean isEngineStarting = false;
	
    private ExtensionController extensionController = ExtensionController.getInstance();
    private ScriptController scriptController = ScriptController.getInstance();

	// Mirth status codes
	public static final int STATUS_OK = 0;
	public static final int STATUS_UNAVAILABLE = 1;
	public static final int STATUS_ENGINE_STARTING = 2;

	// singleton pattern
	private static ConfigurationController instance = null;

	private ConfigurationController() {

	}

	public static ConfigurationController getInstance() {
		synchronized (ConfigurationController.class) {
			if (instance == null)
				instance = new ConfigurationController();

			return instance;
		}
	}

	public void initialize() {
		try {
			// ast: If an user has choosen one, overwrite the platform encoding
			// character
			Properties mirthProperties = PropertyLoader.loadProperties("mirth");

			if (mirthProperties.getProperty(CHARSET) != null) {
				System.setProperty(CHARSET, mirthProperties.getProperty(CHARSET));
			}

			// Check for server GUID and generate a new one if it doesn't exist
			Properties serverIdProperties = getPropertiesFromFile(serverIdPropertiesFile);

			if ((serverIdProperties.getProperty("server.id") != null) && (serverIdProperties.getProperty("server.id").length() > 0)) {
				serverId = serverIdProperties.getProperty("server.id");
			} else {
				logger.debug("generating unique server id");
				serverId = getGuid();
				serverIdProperties.setProperty("server.id", serverId);
				setPropertiesToFile(serverIdPropertiesFile, serverIdProperties);
			}
		} catch (Exception e) {
			logger.warn(e);
		}

		// critical steps
		try {
			loadEncryptionKey();
		} catch (Exception e) {
			logger.error("could not initialize configuration settings", e);
		}
	}

	/*
	 * Return the server GUID
	 */
	public String getServerId() {
		return serverId;
	}

	// ast: Get the list of all avaiable encodings for this JVM
	public List<String> getAvaiableCharsetEncodings() throws ControllerException {
		logger.debug("Retrieving avaiable character encodings");

		try {
			SortedMap<String, Charset> avaiablesCharsets = Charset.availableCharsets();
			List<String> simpleAvaiablesCharset = new ArrayList<String>();

			for (Iterator iter = avaiablesCharsets.values().iterator(); iter.hasNext();) {
				Charset charset = (Charset) iter.next();
				String charsetName = charset.name();

				try {
					if ((charsetName == null) || (charsetName.equals("")))
						charsetName = charset.aliases().iterator().next();
				} catch (Exception e) {
					charsetName = "UNKNOWN";
				}

				simpleAvaiablesCharset.add(charsetName);
			}

			return simpleAvaiablesCharset;
		} catch (Exception e) {
			logger.error("Error at getAvaiableCharsetEncodings", e);
			throw new ControllerException(e);
		}
	}

	public Properties getServerProperties() throws ControllerException {
		return getPropertiesFromFile(serverPropertiesFile);
	}

	public void setServerProperties(Properties properties) throws ControllerException {
		setPropertiesToFile(serverPropertiesFile, properties);
	}

	public Properties getPropertiesFromFile(File inputFile) throws ControllerException {
		logger.debug("retrieving " + inputFile.getName() + " properties");

		FileInputStream fileInputStream = null;

		try {
			inputFile.createNewFile();
			fileInputStream = new FileInputStream(inputFile);
			Properties properties = new Properties();
			properties.load(fileInputStream);
			return properties;
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			try {
				fileInputStream.close();
			} catch (IOException e) {
				logger.warn(e);
			}
		}
	}

	public void setPropertiesToFile(File inputFile, Properties properties) throws ControllerException {
		logger.debug("setting " + inputFile.getName() + " properties");

		FileOutputStream fileOutputStream = null;

		try {
			inputFile.createNewFile();
			fileOutputStream = new FileOutputStream(inputFile);
			properties.store(fileOutputStream, "Updated server properties");
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			try {
				fileOutputStream.flush();
				fileOutputStream.close();
			} catch (IOException e) {
				logger.warn(e);
			}
		}
	}

	public String getGuid() throws ControllerException {
		return UUID.randomUUID().toString();
	}

	/**
	 * Creates a new configuration and restarts the Mule engine.
	 * 
	 * @throws ControllerException
	 */
	public void deployChannels() throws ControllerException {
		logger.debug("deploying channels");

		ScriptController scriptController = ScriptController.getInstance();
		TemplateController templateController = TemplateController.getInstance();

		stopAllChannels();
		scriptController.clearScripts();
		templateController.clearTemplates();

		try {
			ChannelController channelController = ChannelController.getInstance();
			channelController.initialize();

			// instantiate a new configuration builder given the current channel
			// and transport list
			List<Channel> channels = channelController.getChannel(null);
			MuleConfigurationBuilder builder = new MuleConfigurationBuilder(channels, extensionController.getConnectorMetaData());
			// add the newly generated configuration to the database
			addConfiguration(builder.getConfiguration());
			// update the storeMessages reference
			channelController.updateChannelCache(channels);
			
            ExtensionController.getInstance().deployTriggered();
			// restart the mule engine which will grab the latest configuration
			// from the database
            
			CommandQueue queue = CommandQueue.getInstance();
			queue.addCommand(new Command(Command.Operation.RESTART));
            
		} catch (Exception e) {
			throw new ControllerException(e);
		}
        
		systemLogger.logSystemEvent(new SystemEvent("Channels deployed."));
	}
    
    public void compileScripts(List<Channel> channels) throws ControllerException 
    {
        Map<String, String> globalScripts = getGlobalScripts();
        
        Iterator i = globalScripts.entrySet().iterator();
        while (i.hasNext())
        {
            Entry entry = (Entry) i.next();
            if(!(entry.getKey().toString().equals("preprocessor") && globalScripts.get(entry.getKey()).equals("// Modify the message variable below to pre process data\r\n// This script applies across all channels\r\nreturn message;")))
                JavaScriptUtil.getInstance().compileScript(entry.getKey().toString(), globalScripts.get(entry.getKey()));
        }
        
        for(Channel channel : channels)
        {
            if(channel.isEnabled())
            {
                JavaScriptUtil.getInstance().compileScript(channel.getId() + "_deploy", channel.getDeployScript());
                JavaScriptUtil.getInstance().compileScript(channel.getId() + "_shutdown", channel.getShutdownScript());
            }
            else
            {
                JavaScriptUtil.getInstance().removeScriptFromCache(channel.getId() + "_deploy");
                JavaScriptUtil.getInstance().removeScriptFromCache(channel.getId() + "_shutdown");
            }
        }
    }
    
    public void executeChannelDeployScripts(List<Channel> channels)
    {
        for(Channel channel : channels)
        {
            String scriptType = "deploy";
            JavaScriptUtil.getInstance().executeScript(channel.getId() + "_" + scriptType, scriptType, channel.getId());
        }
    }
    
    public void executeChannelShutdownScripts(List<Channel> channels)
    {
        for(Channel channel : channels)
        {
            String scriptType = "shutdown";
            JavaScriptUtil.getInstance().executeScript(channel.getId() + "_" + scriptType, scriptType, channel.getId());
        }
    }
    
    public void executeGlobalDeployScript()
    {
        executeGlobalScript("deploy");
    }
    
    public void executeGlobalShutdownScript()
    {
        executeGlobalScript("shutdown");
    }
    
    public void executeGlobalScript(String scriptType)
    {
        JavaScriptUtil.getInstance().executeScript(scriptType, scriptType, null);
    }
    
    public Map<String, String>  getGlobalScripts() throws ControllerException 
    {
        Map<String, String>  scripts = new HashMap<String, String> ();
        
        String deployScript = scriptController.getScript("deploy");
        String shutdownScript = scriptController.getScript("shutdown");
        String preprocessorScript = scriptController.getScript("preprocessor");
        
        if(deployScript == null)
            deployScript = "// This script executes once when the mule engine is started\r\n// You only have access to the globalMap here to persist data\r\nreturn;";

        if(shutdownScript == null)
            shutdownScript = "// This script executes once when the mule engine is stopped\r\n// You only have access to the globalMap here to persist data\r\nreturn;";

        if(preprocessorScript == null)
            preprocessorScript = "// Modify the message variable below to pre process data\r\n// This script applies across all channels\r\nreturn message;";
        
        
        scripts.put("deploy", deployScript);
        scripts.put("shutdown", shutdownScript);
        scripts.put("preprocessor", preprocessorScript);
        
        return scripts;
    }
    
    public void setGlobalScripts(Map<String, String> scripts) throws ControllerException 
    {
        Iterator i = scripts.entrySet().iterator();
        while (i.hasNext())
        {
            Entry entry = (Entry) i.next();
            scriptController.putScript(entry.getKey().toString(), scripts.get(entry.getKey()));
        }
    }

	private void stopAllChannels() throws ControllerException {
		ChannelStatusController channelStatusController = ChannelStatusController.getInstance();
		List<ChannelStatus> deployedChannels = channelStatusController.getChannelStatusList();

		for (Iterator iter = deployedChannels.iterator(); iter.hasNext();) {
			ChannelStatus status = (ChannelStatus) iter.next();
			channelStatusController.stopChannel(status.getChannelId());
		}
	}

    public String getMuleConfigurationPath()
    {
        Properties properties = PropertyLoader.loadProperties("mirth");
        
        String fileSeparator = System.getProperty("file.separator");
        File muleBootFile = new File(ClassPathResource.getResourceURI(properties.getProperty("mule.boot")));
        return muleBootFile.getParent() + fileSeparator + properties.getProperty("mule.config");
    }
    
	/**
	 * Returns a File with the latest Mule configuration.
	 * 
	 * @return
	 * @throws ControllerException
	 */
	public File getLatestConfiguration() throws ControllerException {
        logger.debug("retrieving latest configuration");

        Properties properties = PropertyLoader.loadProperties("mirth");

        try {
            Configuration latestConfiguration = (Configuration) sqlMap.queryForObject("getLatestConfiguration");

            if (latestConfiguration != null) {
                logger.debug("using configuration " + latestConfiguration.getId() + " created on " + latestConfiguration.getDateCreated());

                // Find the path of mule.boot because mule.config may not exist.
                // Then generate the path of mule.config in the same directory
                // as mule.boot.

                String muleConfigPath = getMuleConfigurationPath();
                
                BufferedWriter out = new BufferedWriter(new FileWriter(new File(muleConfigPath)));
                out.write(latestConfiguration.getData());
                out.close();
                return new File(muleConfigPath);
            }

            logger.debug("no configuration found, using default boot file");
            return new File(ClassPathResource.getResourceURI(properties.getProperty("mule.boot")));
        } catch (Exception e) {
            logger.error("Could not retrieve latest configuration.", e);
            return null;
        }
	}

	/**
	 * Removes the most recent configuration from the database. This is used to
	 * revert to the last good configuration.
	 * 
	 * @throws ControllerException
	 */
	public void deleteLatestConfiguration() {
		logger.debug("deleting most recent configuration");

		try {
			sqlMap.delete("deleteLatestConfiguration");
		} catch (SQLException e) {
			logger.warn("Could not delete latest configuration.", e);
		}
	}

	/**
	 * Adds a new configuraiton to the database.
	 * 
	 * @param data
	 * @throws ControllerException
	 */
	private void addConfiguration(String data) throws ControllerException {
		logger.debug("adding configuration");

		try {
			sqlMap.insert("insertConfiguration", data);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}

	public SecretKey getEncryptionKey() {
		return encryptionKey;
	}

	public void loadEncryptionKey() throws ControllerException {
		logger.debug("loading encryption key");

		ObjectXMLSerializer serializer = new ObjectXMLSerializer();

		try {
			String data = (String) sqlMap.queryForObject("getKey");
			boolean isKeyFound = false;

			if (data != null) {
				logger.debug("encryption key found");
				encryptionKey = (SecretKey) serializer.fromXML(data);
				isKeyFound = true;
			}

			if (!isKeyFound) {
				logger.debug("no key found, creating new encryption key");
				encryptionKey = KeyGenerator.getInstance(Encrypter.DES_ALGORITHM).generateKey();
				sqlMap.insert("insertKey", serializer.toXML(encryptionKey));
			}
		} catch (Exception e) {
			throw new ControllerException("error loading encryption key", e);
		}
	}

	public List<DriverInfo> getDatabaseDrivers() throws ControllerException {
		logger.debug("retrieving database driver list");
		File driversFile = new File(ClassPathResource.getResourceURI("/custom/dbdrivers.xml"));

		if (driversFile.exists()) {
			try {
				ArrayList<DriverInfo> drivers = new ArrayList<DriverInfo>();
				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(driversFile);
				Element driversElement = document.getDocumentElement();

				for (int i = 0; i < driversElement.getElementsByTagName("driver").getLength(); i++) {
					Element driverElement = (Element) driversElement.getElementsByTagName("driver").item(i);
					DriverInfo driver = new DriverInfo(driverElement.getAttribute("name"), driverElement.getAttribute("class"));
					logger.debug("found database driver: " + driver);
					drivers.add(driver);
				}

				return drivers;
			} catch (Exception e) {
				throw new ControllerException("Error during loading of database drivers file: " + driversFile.getAbsolutePath(), e);
			}
		} else {
			throw new ControllerException("Could not locate database drivers file: " + driversFile.getAbsolutePath());
		}
	}

	public String getServerVersion() {
		return versionProperties.getProperty("mirth.version");
	}
	
    public int getSchemaVersion() {
        return Integer.parseInt(versionProperties.getProperty("schema.version"));
    }
    
	public String getBuildDate() {
		return versionProperties.getProperty("mirth.date");
	}

	public int getStatus() {
		logger.debug("getting Mirth status");

		// Check if mule engine is running. First check if it is starting.
		// Also, if it is not running, double-check to see that it was not
		// starting.
		// This double-check is not foolproof, but works in most cases.
		boolean isEngineRunning = false;
		boolean localIsEngineStarting = false;
		if (isEngineStarting()) {
			localIsEngineStarting = true;
		} else {
			JMXConnection jmxConnection = null;

			try {
				jmxConnection = JMXConnectionFactory.createJMXConnection();
				Hashtable<String, String> properties = new Hashtable<String, String>();
				properties.put("type", "control");
				properties.put("name", "MuleService");

				if (!((Boolean) jmxConnection.getAttribute(properties, "Stopped"))) {
					isEngineRunning = true;
				}
			} catch (Exception e) {
				if (isEngineStarting()) {
					localIsEngineStarting = true;
				} else {
					logger.warn("could not retrieve status of engine");
					isEngineRunning = false;
				}
			} finally {
				if (jmxConnection != null) {
					jmxConnection.close();
				}
			}
		}

		// check if database is running
		boolean isDatabaseRunning = false;
		Statement statement = null;
		Connection connection = null;
		try {
			connection = sqlMap.getDataSource().getConnection();
			statement = connection.createStatement();
			statement.execute("SELECT 'STATUS_OK' FROM CONFIGURATION");
			isDatabaseRunning = true;
		} catch (Exception e) {
			logger.warn("could not retrieve status of database", e);
			isDatabaseRunning = false;
		} finally {
			DatabaseUtil.close(statement);
			DatabaseUtil.close(connection);
		}

		// If the database isn't running or the engine isn't running (only if it
		// isn't starting) return STATUS_UNAVAILABLE.
		// If it's starting, return STATUS_ENGINE_STARTING.
		// All other cases return STATUS_OK.
		if (!isDatabaseRunning || (!isEngineRunning && !localIsEngineStarting)) {
			return STATUS_UNAVAILABLE;
		} else if (localIsEngineStarting) {
			return STATUS_ENGINE_STARTING;
		} else {
			return STATUS_OK;
		}
	}

	public ServerConfiguration getServerConfiguration() throws ControllerException {
		ChannelController channelController = ChannelController.getInstance();
		AlertController alertController = AlertController.getInstance();
		UserController userController = UserController.getInstance();

		ServerConfiguration serverConfiguration = new ServerConfiguration();
		serverConfiguration.setChannels(channelController.getChannel(null));
		serverConfiguration.setAlerts(alertController.getAlert(null));
		serverConfiguration.setUsers(userController.getUser(null));
		serverConfiguration.setProperties(getServerProperties());

		return serverConfiguration;
	}

	public void setServerConfiguration(ServerConfiguration serverConfiguration) throws ControllerException {
		ChannelController channelController = ChannelController.getInstance();
		AlertController alertController = AlertController.getInstance();

		channelController.removeChannel(null);
		alertController.removeAlert(null);

		setServerProperties(serverConfiguration.getProperties());

		for (Channel channel : serverConfiguration.getChannels()) {
			channelController.updateChannel(channel, true);
		}

		alertController.updateAlerts(serverConfiguration.getAlerts());
	}

	public boolean isEngineStarting() {
		return isEngineStarting;
	}

	public void setEngineStarting(boolean isEngineStarting) {
		this.isEngineStarting = isEngineStarting;
	}
}
