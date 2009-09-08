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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.webreach.mirth.connectors.ConnectorService;
import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.ExtensionPoint;
import com.webreach.mirth.model.ExtensionPointDefinition;
import com.webreach.mirth.model.MetaData;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.plugins.ServerPlugin;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.server.util.FileUtil;
import com.webreach.mirth.util.ExtensionUtil;

public class DefaultExtensionController extends ExtensionController {
    private Logger logger = Logger.getLogger(this.getClass());
    private Map<String, PluginMetaData> plugins;
    private Map<String, ServerPlugin> loadedPlugins = null;
    private Map<String, ConnectorMetaData> connectors = null;
    private List<String> clientLibraries = null;
    private Map<String, ConnectorMetaData> protocols = null;

    // singleton pattern
    private static DefaultExtensionController instance = null;
    
    public static ExtensionController create() {
        synchronized (DefaultExtensionController.class) {
            if (instance == null) {
                instance = new DefaultExtensionController();
                instance.loadExtensions();
            }

            return instance;
        }
    }

    private DefaultExtensionController() {

    }

    private void loadExtensions() {
        try {
            loadConnectorMetaData();
            loadPluginMetaData();
            loadClientLibraries();
        } catch (Exception e) {
            logger.error("could not initialize extension settings", e);
            return;
        }

        initPlugins();
    }
    
    // Extension point for ExtensionPoint.Type.SERVER_PLUGIN
    @ExtensionPointDefinition(mode = ExtensionPoint.Mode.SERVER, type = ExtensionPoint.Type.SERVER_PLUGIN)
    public void initPlugins() {
        loadedPlugins = new HashMap<String, ServerPlugin>();

        for (PluginMetaData metaData : plugins.values()) {
            try {
                if (metaData.isEnabled()) {
                    for (ExtensionPoint extensionPoint : metaData.getExtensionPoints()) {
                        if (extensionPoint.getMode() == ExtensionPoint.Mode.SERVER && extensionPoint.getType() == ExtensionPoint.Type.SERVER_PLUGIN && extensionPoint.getClassName() != null && extensionPoint.getClassName().length() > 0) {
                            ServerPlugin serverPlugin = (ServerPlugin) Class.forName(extensionPoint.getClassName()).newInstance();
                            String pluginName = extensionPoint.getName();
                            Properties properties = null;

                            try {
                                properties = getPluginProperties(pluginName);
                                if (properties == null) {
                                    properties = serverPlugin.getDefaultProperties();
                                    if (properties != null) {
                                        setPluginProperties(pluginName, properties);
                                    }
                                }
                            } catch (Exception e) {
                                properties = serverPlugin.getDefaultProperties();
                                if (properties == null) {
                                    properties = new Properties();
                                }
                                setPluginProperties(pluginName, properties);
                            }
                            serverPlugin.init(properties);
                            loadedPlugins.put(pluginName, serverPlugin);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error initializing extensions.", e);
            }
        }
    }

    public boolean isExtensionEnabled(String name) {
        for (PluginMetaData plugin : plugins.values()) {
            if (plugin.isEnabled() && plugin.getName().equals(name))
                return true;
        }
        for (ConnectorMetaData connector : connectors.values()) {
            if (connector.isEnabled() && connector.getName().equals(name))
                return true;
        }

        return false;
    }

    public void startPlugins() {
        for (ServerPlugin plugin : loadedPlugins.values())
            plugin.start();
    }

    public void updatePlugin(String name, Properties properties) {
        loadedPlugins.get(name).update(properties);
    }

    public void deployTriggered() {
        for (ServerPlugin plugin : loadedPlugins.values())
            plugin.onDeploy();
    }

    public void stopPlugins() {
        for (ServerPlugin plugin : loadedPlugins.values())
            plugin.stop();
    }

    public Object invoke(String name, String method, Object object, String sessionId) {
        return loadedPlugins.get(name).invoke(method, object, sessionId);
    }

    public Object invokeConnectorService(String name, String method, Object object, String sessionsId) throws Exception {
        ConnectorMetaData connectorMetaData = connectors.get(name);

        if (connectorMetaData.getServiceClassName() != null) {
            ConnectorService connectorService = (ConnectorService) Class.forName(connectorMetaData.getServiceClassName()).newInstance();
            return connectorService.invoke(method, object, sessionsId);
        }

        return null;
    }

    public void installExtension(FileItem fileItem) throws ControllerException {
        ExtensionUtil.installExtension(fileItem);
    }
    
    public void uninstallExtension(String packageName) throws ControllerException {
    	File uninstallFile = new File(getExtensionsPath() + EXTENSIONS_UNINSTALL_FILE);

    	try {
			FileWriter fileWriter = new FileWriter(uninstallFile, true);
			fileWriter.write(packageName + System.getProperty("line.separator"));
			fileWriter.close();
			
			for (PluginMetaData plugin : plugins.values()) {
				if (plugin.getPath().equals(packageName) && plugin.getSqlScript() != null) {
					String contents = FileUtil.read(ExtensionController.getExtensionsPath() + plugin.getPath() + System.getProperty("file.separator") + plugin.getSqlScript());
                    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(contents)));
                    
                    String script = getUninstallScript(document);

                    if (script != null) {
	                    List<String> scriptList = new LinkedList<String>();
	
	                    script = script.trim();
	                    StringBuilder sb = new StringBuilder();
	                    boolean blankLine = false;
	                    Scanner s = new Scanner(script);
	
	                    while (s.hasNextLine()) {
	                        String temp = s.nextLine();
	
	                        if (temp.trim().length() > 0)
	                            sb.append(temp + " ");
	                        else
	                            blankLine = true;
	
	                        if (blankLine || !s.hasNextLine()) {
	                            scriptList.add(sb.toString().trim());
	                            blankLine = false;
	                            sb.delete(0, sb.length());
	                        }
	                    }
	                    
	                    // If there was an uninstall script, then save the script to
	                    // run later and remove the schema.plugin from extensions.properties
	                    if (scriptList.size() > 0) {
		                    List<String> uninstallScripts = getUninstallScripts();
		                    uninstallScripts.addAll(scriptList);
		                    setUninstallScripts(uninstallScripts);
		                    
		                    Properties extensionsProperties = getExtensionsProperties();
		                    extensionsProperties.remove("schema." + plugin.getPath());
		                    setExtensionsProperties(extensionsProperties);
	                    }
                    }
				}
			}
		} catch (Exception e) {
			throw new ControllerException(e);
		}
    }
    
    private String getUninstallScript(Document document) {
    	String script = null;
        Element uninstallElement = (Element) document.getElementsByTagName("uninstall").item(0);
        String databaseType = ControllerFactory.getFactory().createConfigurationController().getDatabaseType();

        NodeList scriptNodes = uninstallElement.getElementsByTagName("script");

        for (int i = 0; i < scriptNodes.getLength(); i++) {
            Node scriptNode = scriptNodes.item(i);
            Node scriptNodeAttribute = scriptNode.getAttributes().getNamedItem("type");

            String[] dbTypes = scriptNodeAttribute.getTextContent().split(",");
            for (int k = 0; k < dbTypes.length; k++) {
                if (dbTypes[k].equals("all") || dbTypes[k].equals(databaseType)) {
                    script = scriptNode.getTextContent();
                }
            }
        }
        return script;
    }

    public void setPluginProperties(String pluginName, Properties properties) throws ControllerException {
        logger.debug("setting " + pluginName + " properties");

        FileOutputStream fileOutputStream = null;
        
        String packageName = getPackageName(pluginName);

        try {
            File propertiesFile = new File(getExtensionsPath() + packageName + System.getProperty("file.separator") + PLUGIN_PROPERTIES_FILE);
            fileOutputStream = new FileOutputStream(propertiesFile);
            properties.store(fileOutputStream, "Updated " + packageName + " properties");
        } catch (Exception e) {
            throw new ControllerException(e);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                logger.warn(e);
            }
        }
    }

    public Properties getPluginProperties(String pluginName) throws ControllerException {
        logger.debug("retrieving " + pluginName + " properties");

        FileInputStream fileInputStream = null;
        Properties properties = null;
        
        String packageName = getPackageName(pluginName);
        
        try {
            File propertiesFile = new File(getExtensionsPath() + packageName + System.getProperty("file.separator") + PLUGIN_PROPERTIES_FILE);
            if (!propertiesFile.exists()) {
                return null;
            }
            fileInputStream = new FileInputStream(propertiesFile);
            properties = new Properties();
            properties.load(fileInputStream);
        } catch (Exception e) {
            throw new ControllerException(e);
        } finally {
            try {
                if (fileInputStream != null)
                    fileInputStream.close();
            } catch (IOException e) {
                logger.warn(e);
            }
        }

        return properties;
    }
    
    /**
     * Sometimes the pluginName passed in is actually the 
     * extensionPoint name.  In this case, we need to use 
     * the extensionPoint name to search for the pluginName.
     * 
     * @param pluginName Plugin or Extension Point name
     * @return The package name of the plugin/extension point
     */
    private String getPackageName(String pluginName) {
        String packageName = null;
        
        if (plugins.get(pluginName) == null) {
        	for (PluginMetaData metaData : plugins.values()) {
        		for (ExtensionPoint extensionPoint : metaData.getExtensionPoints()) {
        			if (extensionPoint.getName().equals(pluginName)) {
        				packageName = metaData.getPath();
        			}
        		}
        	}
        } else {
        	packageName = plugins.get(pluginName).getPath();
        }
        
        return packageName;
    }

    public Map<String, ConnectorMetaData> getConnectorMetaData() throws ControllerException {
        logger.debug("retrieving connector metadata");
        return this.connectors;
    }

    private void loadConnectorMetaData() throws ControllerException {
        logger.debug("loading connector metadata");
        this.connectors = (Map<String, ConnectorMetaData>) ExtensionUtil.loadExtensionMetaData(ExtensionType.CONNECTOR);
        this.protocols = new HashMap<String, ConnectorMetaData>();

        for (ConnectorMetaData connectorMetaData : this.connectors.values()) {
            String protocol = connectorMetaData.getProtocol();
            if (protocol.indexOf(':') > -1) {
                String[] protocolStrings = protocol.split(":");
                for (int i = 0; i < protocolStrings.length; i++) {
                    protocols.put(protocolStrings[i], connectorMetaData);
                }
            } else {
                protocols.put(connectorMetaData.getProtocol(), connectorMetaData);
            }
        }
    }

    public void saveConnectorMetaData(Map<String, ConnectorMetaData> metaData) throws ControllerException {
        logger.debug("saving connector metadata");
        this.connectors = metaData;
        ExtensionUtil.saveExtensionMetaData(metaData);
    }

    public List<String> getClientLibraries() throws ControllerException {
        logger.debug("retrieving client libraries");
        return this.clientLibraries;
    }

    private void loadClientLibraries() throws ControllerException {
        logger.debug("loading client libraries");
        List<MetaData> extensionMetaData = new ArrayList<MetaData>();

        extensionMetaData.addAll(plugins.values());
        extensionMetaData.addAll(connectors.values());

        this.clientLibraries = ExtensionUtil.loadClientLibraries(extensionMetaData);
    }

    public Map<String, PluginMetaData> getPluginMetaData() throws ControllerException {
        logger.debug("retrieving plugin metadata");
        return this.plugins;
    }

    public void savePluginMetaData(Map<String, PluginMetaData> metaData) throws ControllerException {
        logger.debug("saving plugin metadata");
        this.plugins = metaData;
        ExtensionUtil.saveExtensionMetaData(metaData);
    }

    private void loadPluginMetaData() throws ControllerException {
        logger.debug("loading plugin metadata");
        this.plugins = (Map<String, PluginMetaData>) ExtensionUtil.loadExtensionMetaData(ExtensionType.PLUGIN);
    }

    public Map<String, ConnectorMetaData> getProtocols() {
        logger.debug("retrieving plugin protocols");
        return this.protocols;
    }

    public ConnectorMetaData getConnectorMetaDataByProtocol(String protocol) {
        return protocols.get(protocol);
    }

    public Map<String, ServerPlugin> getLoadedPlugins() {
        return loadedPlugins;
    }
    
    public void setExtensionsProperties(Properties properties) throws ControllerException {
        logger.debug("setting extension properties");

        FileOutputStream fileOutputStream = null;

        try {
            File propertiesFile = new File(getExtensionsPath() + EXTENSIONS_PROPERTIES_FILE);
            fileOutputStream = new FileOutputStream(propertiesFile);
            properties.store(fileOutputStream, "Updated extension properties");
        } catch (Exception e) {
            throw new ControllerException(e);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                logger.warn(e);
            }
        }
    }

    public Properties getExtensionsProperties() throws ControllerException {
        logger.debug("retrieving extension properties");

        FileInputStream fileInputStream = null;
        Properties properties = new Properties();

        try {
            File propertiesFile = new File(getExtensionsPath() + EXTENSIONS_PROPERTIES_FILE);
            if (!propertiesFile.exists()) {
                return properties;
            }
            fileInputStream = new FileInputStream(propertiesFile);
            properties.load(fileInputStream);
        } catch (Exception e) {
            throw new ControllerException(e);
        } finally {
            try {
                if (fileInputStream != null)
                    fileInputStream.close();
            } catch (IOException e) {
                logger.warn(e);
            }
        }

        return properties;
    }
    
    public void uninstallExtensions() {
    	try {
    		DatabaseUtil.executeScript(getUninstallScripts(), true);
    	} catch (Exception e) {
    		logger.error("Error uninstalling extensions.", e);
    	}
    	
    	deleteUninstallScripts();
    }
    
    public void setUninstallScripts(List<String> uninstallScripts) throws ControllerException {
    	File uninstallScriptsFile = new File(getExtensionsPath() + EXTENSIONS_UNINSTALL_SCRIPTS_FILE);
    	ObjectXMLSerializer serializer = new ObjectXMLSerializer();
    	
    	try {
    		FileUtil.write(uninstallScriptsFile.getAbsolutePath(), false, serializer.toXML(uninstallScripts));
		} catch (IOException e) {
			throw new ControllerException(e);
		}
    }
    
    public List<String> getUninstallScripts() throws ControllerException {
    	List<String> uninstallScripts = new LinkedList<String>();
    	File uninstallScriptsFile = new File(getExtensionsPath() + EXTENSIONS_UNINSTALL_SCRIPTS_FILE);
    	
    	if (uninstallScriptsFile.exists()) {
    		ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        	try {
				uninstallScripts = (List<String>) serializer.fromXML(FileUtil.read(uninstallScriptsFile.getAbsolutePath()));
			} catch (IOException e) {
				throw new ControllerException(e);
			}
    	}
    	
    	return uninstallScripts;
    }
    
    public void deleteUninstallScripts() {
    	File uninstallScriptsFile = new File(getExtensionsPath() + EXTENSIONS_UNINSTALL_SCRIPTS_FILE);
    	if (uninstallScriptsFile.exists()) {
    		uninstallScriptsFile.delete();
    	}
    }
}
