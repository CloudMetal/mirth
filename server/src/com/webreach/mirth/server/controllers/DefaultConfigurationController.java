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

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.UUID;
import java.util.Map.Entry;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.DriverInfo;
import com.webreach.mirth.model.PasswordRequirements;
import com.webreach.mirth.model.ServerConfiguration;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.util.PasswordRequirementsChecker;
import com.webreach.mirth.server.Command;
import com.webreach.mirth.server.CommandQueue;
import com.webreach.mirth.server.tools.ClassPathResource;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.server.util.JMXConnection;
import com.webreach.mirth.server.util.JMXConnectionFactory;
import com.webreach.mirth.server.util.JavaScriptUtil;
import com.webreach.mirth.server.util.SqlConfig;
import com.webreach.mirth.util.Encrypter;
import com.webreach.mirth.util.PropertyVerifier;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 * @author geraldb
 * 
 */
public class DefaultConfigurationController extends ConfigurationController {
    private static final String PROPERTIES_CORE = "core";

    private static final String CHANNEL_POSTPROCESSOR_DEFAULT_SCRIPT = "// This script executes once after a message has been processed\nreturn;";
    private static final String GLOBAL_PREPROCESSOR_DEFAULT_SCRIPT = "// Modify the message variable below to pre process data\n// This script applies across all channels\nreturn message;";
    private static final String GLOBAL_POSTPROCESSOR_DEFAULT_SCRIPT = "// This script executes once after a message has been processed\n// This script applies across all channels\nreturn;";
    private static final String GLOBAL_DEPLOY_DEFAULT_SCRIPT = "// This script executes once when the mule engine is started\n// You only have access to the globalMap here to persist data\nreturn;";
    private static final String GLOBAL_SHUTDOWN_DEFAULT_SCRIPT = "// This script executes once when the mule engine is stopped\n// You only have access to the globalMap here to persist data\nreturn;";

    private Logger logger = Logger.getLogger(this.getClass());
    private EventController systemLogger = ControllerFactory.getFactory().createEventController();
    private String baseDir = new File(ClassPathResource.getResourceURI("mirth.properties")).getParentFile().getParent();
    private static SecretKey encryptionKey = null;
    private static String serverId = null;
    private static final String CHARSET = "ca.uhn.hl7v2.llp.charset";
    private static final String TEMP_DIR = "mirthTempDir";
    private boolean isEngineStarting = true;
    private JavaScriptUtil javaScriptUtil = JavaScriptUtil.getInstance();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
    private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
    private PasswordRequirements passwordRequirements;
    private static PropertiesConfiguration versionConfig;
    private static PropertiesConfiguration mirthConfig;

    // singleton pattern
    private static DefaultConfigurationController instance = null;

    private DefaultConfigurationController() {

    }

    public static ConfigurationController create() {
        synchronized (DefaultConfigurationController.class) {
            if (instance == null) {
                instance = new DefaultConfigurationController();
                instance.initialize();
            }

            return instance;
        }
    }

    private void initialize() {
        try {
            mirthConfig = new PropertiesConfiguration("mirth.properties");
            versionConfig = new PropertiesConfiguration("version.properties");
            
            if (mirthConfig.getString(TEMP_DIR) != null) {
                File tempDir = new File(StringUtils.replace(mirthConfig.getString(TEMP_DIR), "${mirthHomeDir}", baseDir));

                if (!tempDir.exists()) {
                    if (tempDir.mkdirs()) {
                        logger.debug("Created temp dir: " + tempDir.getAbsolutePath());    
                    } else {
                        logger.debug("Could not creat temp dir: " + tempDir.getAbsolutePath());
                    }
                }

                System.setProperty("java.io.tmpdir", tempDir.getAbsolutePath());
                logger.debug("Set tmpdir to: " + tempDir.getAbsolutePath());
            }

            if (mirthConfig.getString(CHARSET) != null) {
                System.setProperty(CHARSET, mirthConfig.getString(CHARSET));
            }

            // Check for server GUID and generate a new one if it doesn't exist
            PropertiesConfiguration serverIdConfig = new PropertiesConfiguration(new File(baseDir + System.getProperty("file.separator") + "server.id"));

            if ((serverIdConfig.getString("server.id") != null) && (serverIdConfig.getString("server.id").length() > 0)) {
                serverId = serverIdConfig.getString("server.id");
            } else {
                logger.debug("generating unique server id");
                serverId = getGuid();
                serverIdConfig.setProperty("server.id", serverId);
                serverIdConfig.save();
            }

            loadDefaultProperties();

            this.passwordRequirements = PasswordRequirementsChecker.getInstance().loadPasswordRequirements();
        } catch (Exception e) {
            logger.warn(e);
        }
    }

    private void loadDefaultProperties() throws Exception {
        Properties serverProperties = getServerProperties();

        // update.url
        String updateUrl = serverProperties.getProperty("update.url");

        // migrate old update url
        if ((updateUrl != null) && (updateUrl.equalsIgnoreCase("http://updates.mirthproject.org"))) {
            serverProperties.setProperty("update.url", "http://updates.mirthcorp.com");
        }

        // set new update url
        if ((updateUrl == null) || (updateUrl.length() == 0)) {
            serverProperties.setProperty("update.url", "http://updates.mirthcorp.com");
        }

        // update.enabled
        String updateEnabled = serverProperties.getProperty("update.enabled");

        if ((updateEnabled == null) || (updateEnabled.length() == 0)) {
            serverProperties.setProperty("update.enabled", "1");
        }

        // stats.enabled
        String updateIdent = serverProperties.getProperty("stats.enabled");

        if ((updateIdent == null) || (updateIdent.length() == 0)) {
            serverProperties.setProperty("stats.enabled", "1");
        }

        // firstlogin
        String firstLogin = serverProperties.getProperty("firstlogin");

        if ((firstLogin == null) || (firstLogin.length() == 0)) {
            serverProperties.setProperty("firstlogin", "1");
        }

        // Check for the old "clearGlobal" property and migrate it.
        String clearGlobal = serverProperties.getProperty("clearGlobal");

        if ((clearGlobal != null) && (clearGlobal.length() > 0)) {
            serverProperties.setProperty("server.resetglobalvariables", clearGlobal);
            serverProperties.remove("clearGlobal");
        }

        // server.resetglobalvariables
        String resetGlobalVariables = serverProperties.getProperty("server.resetglobalvariables");

        if ((resetGlobalVariables == null) || (resetGlobalVariables.length() == 0)) {
            serverProperties.setProperty("server.resetglobalvariables", "1");
        }

        // smtp.useAuthentication
        String useAuthentication = serverProperties.getProperty("smtp.requireAuthentication");
        String smtpAuth = serverProperties.getProperty("smtp.auth");

        if ((useAuthentication != null) && (useAuthentication.length() > 0)) {
            serverProperties.setProperty("smtp.auth", useAuthentication);
            serverProperties.remove("smtp.requireAuthentication");
        } else if ((smtpAuth == null) || (smtpAuth.length() == 0)) {
            serverProperties.setProperty("smtp.auth", "0");
        }

        // smtp.secure
        String secure = serverProperties.getProperty("smtp.secure");

        if ((secure == null) || (secure.length() == 0)) {
            serverProperties.setProperty("smtp.secure", "none");
        }

        setServerProperties(serverProperties);
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

            for (Charset charset : avaiablesCharsets.values()) {
                String charsetName = charset.name();

                try {
                    if ((charsetName == null) || (charsetName.equals(""))) {
                        charsetName = charset.aliases().iterator().next();
                    }
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
        return getPropertiesForGroup(PROPERTIES_CORE);
    }

    public void setServerProperties(Properties properties) throws ControllerException {
        for (Object name : properties.keySet()) {
            saveProperty(PROPERTIES_CORE, (String) name, (String) properties.get(name));    
        }
    }

    public String getGuid() throws ControllerException {
        return UUID.randomUUID().toString();
    }

    /**
     * Updates the existing Mule configuration with new channels.
     * 
     * @throws ControllerException
     */
    public void deployChannels(List<Channel> channels) throws ControllerException {
        logger.debug("deploying " + channels.size() + " channels");

        try {
            CommandQueue.getInstance().addCommand(new Command(Command.Operation.DEPLOY_CHANNELS, channels));
            ControllerFactory.getFactory().createChannelController().loadChannelCache();
        } catch (Exception e) {
            throw new ControllerException(e);
        }

        systemLogger.logSystemEvent(new SystemEvent("Channels deployed."));
    }

    public void undeployChannels(List<String> channelIds) throws ControllerException {
        logger.debug("undeploying " + channelIds.size() + " channels");

        try {
            Command command = new Command(Command.Operation.UNDEPLOY_CHANNELS);
            command.setParameter(channelIds);
            CommandQueue.getInstance().addCommand(command);
            ControllerFactory.getFactory().createChannelController().loadChannelCache();
        } catch (Exception e) {
            throw new ControllerException(e);
        }

        systemLogger.logSystemEvent(new SystemEvent("Channels un-deployed."));
    }

    /**
     * Creates a new configuration and restarts the Mule engine.
     * 
     * @throws ControllerException
     */
    public void deployAllChannels() throws ControllerException {
        logger.debug("deploying channels");

        scriptController.clearScripts();
        ControllerFactory.getFactory().createTemplateController().clearTemplates();

        try {
            ChannelController channelController = ControllerFactory.getFactory().createChannelController();
            channelController.loadChannelCache();
            List<Channel> channels = channelController.getChannel(null);

            // update the storeMessages reference
            channelController.refreshChannelCache(channels);
            ControllerFactory.getFactory().createExtensionController().deployTriggered();
            deployChannels(channels);
        } catch (Exception e) {
            throw new ControllerException(e);
        }

        systemLogger.logSystemEvent(new SystemEvent("Channels deployed."));
    }

    public void compileScripts(List<Channel> channels) throws Exception {

        for (Entry<String, String> entry : getGlobalScripts().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.equals(GLOBAL_PREPROCESSOR_KEY)) {
                if (!javaScriptUtil.compileAndAddScript(key, value, GLOBAL_PREPROCESSOR_DEFAULT_SCRIPT, false)) {
                    logger.debug("removing global preprocessor");
                    javaScriptUtil.removeScriptFromCache(GLOBAL_PREPROCESSOR_KEY);
                }
            } else if (key.equals(GLOBAL_POSTPROCESSOR_KEY)) {
                if (!javaScriptUtil.compileAndAddScript(key, value, GLOBAL_POSTPROCESSOR_DEFAULT_SCRIPT, false)) {
                    logger.debug("removing global postprocessor");
                    javaScriptUtil.removeScriptFromCache(GLOBAL_POSTPROCESSOR_KEY);
                }
            } else {
                // add the DEPLOY and SHUTDOWN scripts,
                // which do not have defaults
                if (!javaScriptUtil.compileAndAddScript(key, value, "", false)) {
                    logger.debug("remvoing " + key);
                    javaScriptUtil.removeScriptFromCache(key);
                }
            }
        }

        for (Channel channel : channels) {
            if (channel.isEnabled()) {
                javaScriptUtil.compileAndAddScript(channel.getId() + "_Deploy", channel.getDeployScript(), null, false);
                javaScriptUtil.compileAndAddScript(channel.getId() + "_Shutdown", channel.getShutdownScript(), null, false);

                // only compile and run post processor if its not the default
                if (!javaScriptUtil.compileAndAddScript(channel.getId() + "_Postprocessor", channel.getPostprocessingScript(), CHANNEL_POSTPROCESSOR_DEFAULT_SCRIPT, true)) {
                    logger.debug("removing " + channel.getId() + "_Postprocessor");
                    javaScriptUtil.removeScriptFromCache(channel.getId() + "_Postprocessor");
                }

            } else {
                javaScriptUtil.removeScriptFromCache(channel.getId() + "_Deploy");
                javaScriptUtil.removeScriptFromCache(channel.getId() + "_Shutdown");
                javaScriptUtil.removeScriptFromCache(channel.getId() + "_Postprocessor");
            }
        }
    }

    public void executeChannelDeployScripts(List<Channel> channels) {
        for (Channel channel : channels) {
            String scriptType = GLOBAL_DEPLOY_KEY;
            javaScriptUtil.executeScript(channel.getId() + "_" + scriptType, scriptType, channel.getId());
        }
    }

    public void executeChannelShutdownScripts(List<Channel> channels) {
        for (Channel channel : channels) {
            String scriptType = GLOBAL_SHUTDOWN_KEY;
            javaScriptUtil.executeScript(channel.getId() + "_" + scriptType, scriptType, channel.getId());
        }
    }

    public void executeGlobalDeployScript() {
        executeGlobalScript(GLOBAL_DEPLOY_KEY);
    }

    public void executeGlobalShutdownScript() {
        executeGlobalScript(GLOBAL_SHUTDOWN_KEY);
    }

    public void executeGlobalScript(String scriptType) {
        javaScriptUtil.executeScript(scriptType, scriptType, "");
    }

    public Map<String, String> getGlobalScripts() throws ControllerException {
        Map<String, String> scripts = new HashMap<String, String>();

        String deployScript = scriptController.getScript(GLOBAL_DEPLOY_KEY);
        String shutdownScript = scriptController.getScript(GLOBAL_SHUTDOWN_KEY);
        String preprocessorScript = scriptController.getScript(GLOBAL_PREPROCESSOR_KEY);
        String postprocessorScript = scriptController.getScript(GLOBAL_POSTPROCESSOR_KEY);

        if (deployScript == null || deployScript.equals("")) {
            deployScript = GLOBAL_DEPLOY_DEFAULT_SCRIPT;
        }

        if (shutdownScript == null || shutdownScript.equals("")) {
            shutdownScript = GLOBAL_SHUTDOWN_DEFAULT_SCRIPT;
        }

        if (preprocessorScript == null || preprocessorScript.equals("")) {
            preprocessorScript = GLOBAL_PREPROCESSOR_DEFAULT_SCRIPT;
        }

        if (postprocessorScript == null || postprocessorScript.equals("")) {
            postprocessorScript = GLOBAL_POSTPROCESSOR_DEFAULT_SCRIPT;
        }

        scripts.put(GLOBAL_DEPLOY_KEY, deployScript);
        scripts.put(GLOBAL_SHUTDOWN_KEY, shutdownScript);
        scripts.put(GLOBAL_PREPROCESSOR_KEY, preprocessorScript);
        scripts.put(GLOBAL_POSTPROCESSOR_KEY, postprocessorScript);

        return scripts;
    }

    public void setGlobalScripts(Map<String, String> scripts) throws ControllerException {
        for (Entry<String, String> entry : scripts.entrySet()) {
            scriptController.putScript(entry.getKey().toString(), scripts.get(entry.getKey()));
        }
    }

    public String getDatabaseType() {
        return getPropertiesForGroup(PROPERTIES_CORE).getProperty("database");
    }

    public SecretKey getEncryptionKey() {
        return encryptionKey;
    }

    public void loadEncryptionKey() {
        logger.debug("loading encryption key");

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();

        try {
            String data = (String) SqlConfig.getSqlMapClient().queryForObject("Configuration.getKey");
            boolean isKeyFound = false;

            if (data != null) {
                logger.debug("encryption key found");
                encryptionKey = (SecretKey) serializer.fromXML(data);
                isKeyFound = true;
            }

            if (!isKeyFound) {
                logger.debug("no key found, creating new encryption key");
                encryptionKey = KeyGenerator.getInstance(Encrypter.DES_ALGORITHM).generateKey();
                SqlConfig.getSqlMapClient().insert("Configuration.insertKey", serializer.toXML(encryptionKey));
            }
        } catch (Exception e) {
            logger.error("error loading encryption key", e);
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
                    DriverInfo driver = new DriverInfo(driverElement.getAttribute("name"), driverElement.getAttribute("class"), driverElement.getAttribute("template"));
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
        return versionConfig.getString("mirth.version");
    }

    public int getSchemaVersion() {
        return versionConfig.getInt("schema.version", -1);
    }

    public String getBuildDate() {
        return mirthConfig.getString("mirth.date");
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
                    logger.warn("could not retrieve status of engine", e);
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
            connection = SqlConfig.getSqlMapClient().getDataSource().getConnection();
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
        ChannelController channelController = ControllerFactory.getFactory().createChannelController();
        AlertController alertController = ControllerFactory.getFactory().createAlertController();
        CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();

        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setChannels(channelController.getChannel(null));
        serverConfiguration.setAlerts(alertController.getAlert(null));
        serverConfiguration.setCodeTempaltes(codeTemplateController.getCodeTemplate(null));
        serverConfiguration.setProperties(getServerProperties());
        serverConfiguration.setGlobalScripts(getGlobalScripts());
        return serverConfiguration;
    }

    public void setServerConfiguration(ServerConfiguration serverConfiguration) throws ControllerException {
        ChannelController channelController = ControllerFactory.getFactory().createChannelController();
        AlertController alertController = ControllerFactory.getFactory().createAlertController();
        CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();

        setServerProperties(serverConfiguration.getProperties());

        if (serverConfiguration.getChannels() != null) {

            for (Channel channel : channelController.getChannel(null)) {
                boolean found = false;

                for (Channel newChannel : serverConfiguration.getChannels()) {
                    if (newChannel.getId().equals(channel.getId())) {
                        found = true;
                    }
                }

                if (!found) {
                    channelController.removeChannel(channel);
                }
            }

            for (Channel channel : serverConfiguration.getChannels()) {
                PropertyVerifier.checkChannelProperties(channel);
                PropertyVerifier.checkConnectorProperties(channel, extensionController.getConnectorMetaData());
                channelController.updateChannel(channel, true);
            }
        }

        if (serverConfiguration.getAlerts() != null) {
            alertController.removeAlert(null);
            alertController.updateAlerts(serverConfiguration.getAlerts());
        }

        if (serverConfiguration.getCodeTemplates() != null) {
            codeTemplateController.removeCodeTemplate(null);
            codeTemplateController.updateCodeTemplates(serverConfiguration.getCodeTemplates());
        }

        if (serverConfiguration.getGlobalScripts() != null) {
            setGlobalScripts(serverConfiguration.getGlobalScripts());
        }
    }

    public boolean isEngineStarting() {
        return isEngineStarting;
    }

    public void setEngineStarting(boolean isEngineStarting) {
        this.isEngineStarting = isEngineStarting;
    }

    public void shutdown() {
        CommandQueue.getInstance().addCommand(new Command(Command.Operation.SHUTDOWN_SERVER));
    }

    public String getQueuestorePath() {
        String muleQueue = getPropertiesForGroup(PROPERTIES_CORE).getProperty("mule.queue");
        String queuestorePath = StringUtils.replace(muleQueue, "${mirthHomeDir}", baseDir);
        queuestorePath += File.separator + "queuestore";
        return queuestorePath;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public PasswordRequirements getPasswordRequirements() {
        return passwordRequirements;
    }

    public Properties getPropertiesForGroup(String category) {
        logger.debug("retrieving properties: category=" + category);

        try {
            Map<String, String> result = SqlConfig.getSqlMapClient().queryForMap("Configuration.selectPropertiesForCategory", category, "name", "value");
            
            if (!result.isEmpty()) {
                Properties properties = new Properties();
                properties.putAll(result);
                return properties;
            }
        } catch (Exception e) {
            logger.error("Could not retrieve properties: category=" + category, e);
        }

        return null;
    }

    public String getProperty(String category, String name) {
        logger.debug("retrieving property: category=" + category + ", name=" + name);

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("category", category);
            parameterMap.put("name", name);
            return (String) SqlConfig.getSqlMapClient().queryForObject("Configuration.selectProperty", parameterMap);
        } catch (Exception e) {
            logger.error("Could not retrieve property: category=" + category + ", name=" + name, e);
        }

        return null;
    }

    public void saveProperty(String category, String name, String value) {
        logger.debug("storing property: category=" + category + ", name=" + name);

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("category", category);
            parameterMap.put("name", name);
            parameterMap.put("value", value);

            if (getProperty(category, name) == null) {
                SqlConfig.getSqlMapClient().insert("Configuration.insertProperty", parameterMap);
            } else {
                SqlConfig.getSqlMapClient().insert("Configuration.updateProperty", parameterMap);
            }
            
            if (DatabaseUtil.statementExists("Configuration.vacuumConfigurationTable")) {
                SqlConfig.getSqlMapClient().update("Configuration.vacuumConfigurationTable");
            }
        } catch (Exception e) {
            logger.error("Could not store property: category=" + category + ", name=" + name, e);
        }
    }

    public void removeProperty(String category, String name) {
        logger.debug("deleting property: category=" + category + ", name=" + name);

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("category", category);
            parameterMap.put("name", name);
            SqlConfig.getSqlMapClient().delete("Configuration.deleteProperty", parameterMap);
        } catch (Exception e) {
            logger.error("Could not delete property: category=" + category + ", name=" + name, e);
        }
    }
}
