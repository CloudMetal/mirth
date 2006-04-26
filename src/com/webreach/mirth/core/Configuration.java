package com.webreach.mirth.core;

import java.io.FileOutputStream;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.sun.tools.xjc.generator.validator.StringOutputStream;
import com.webreach.mirth.core.util.ChannelMarshaller;
import com.webreach.mirth.core.util.ChannelUnmarshaller;
import com.webreach.mirth.core.util.ConfigurationBuilderException;
import com.webreach.mirth.core.util.ConfigurationException;
import com.webreach.mirth.core.util.DatabaseConnection;
import com.webreach.mirth.core.util.DatabaseUtil;
import com.webreach.mirth.core.util.MarshalException;
import com.webreach.mirth.core.util.MuleConfigurationBuilder;
import com.webreach.mirth.core.util.PropertyLoader;
import com.webreach.mirth.core.util.UnmarshalException;


public class Configuration {
	private List<User> users;
	private Properties properties;
	private List<Transport> transports;
	private List<Channel> channels;
	private static Configuration instance = null;
	private DatabaseConnection dbConnection;
	private boolean initialized = false;

	private Configuration() {
		
	}

	public static Configuration getInstance() {
		synchronized (Configuration.class) {
			if (instance == null)
				instance = new Configuration();

			return instance;
		}
	}
	
	public void initialize() throws ConfigurationException {
		try {
			loadUsers();
			loadTransports();
			loadProperties();
			loadChannels();
			initialized = true;
		} catch (ConfigurationException e) {
			throw e;
		}
	}
	
	public boolean isInitialized() {
		return initialized;
	}

	private void loadUsers() throws ConfigurationException {
		users = new ArrayList<User>();
		ResultSet result = null;
		
		try {
			dbConnection = new DatabaseConnection();
			result = dbConnection.query("SELECT ID, USERNAME, PASSWORD FROM USERS;");
			
			while (result.next()) {
				User user = new User();
				user.setId(result.getInt("ID"));
				user.setUsername(result.getString("USERNAME"));
				user.setPassword(result.getString("PASSWORD"));
				users.add(user);
			}
		} catch (SQLException e) {
			throw new ConfigurationException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}
	
	/**
	 * Returns a <code>List</code> of all Users.
	 * 
	 * @return
	 */
	public List<User> getUsers() throws ConfigurationException {
		if (!isInitialized()) {
			throw new ConfigurationException("Configuration must be initialized first."); 
		} 
		
		return users;
	}
	
	private void storeUsers() throws ConfigurationException {
		try {
			// TODO: write the users list to the database
			dbConnection = new DatabaseConnection();
			dbConnection.update("DELETE FROM USERS;");
			
			for (Iterator iter = users.iterator(); iter.hasNext();) {
				User user = (User) iter.next();
				StringBuffer statement = new StringBuffer();
				statement.append("INSERT INTO USERS (ID, USERNAME, PASSWORD)");
				statement.append("'" + user.getId() + "',");
				statement.append("'" + user.getUsername() + "',");
				statement.append("'" + user.getPassword() + "');");
				dbConnection.update(statement.toString());
			}
		} catch (SQLException e) {
			throw new ConfigurationException(e);
		} finally {
			dbConnection.close();
		}
	}
	
	private void loadChannels() throws ConfigurationException {
		channels = new ArrayList<Channel>();
		ResultSet result = null;
		ChannelUnmarshaller cu = new ChannelUnmarshaller();
		
		try {
			dbConnection = new DatabaseConnection();
			// TODO: create schema for Channels table (attribute name for the XML code)
			result = dbConnection.query("SELECT ID, DATA FROM CHANNELS;");
			
			while (result.next()) {
				Channel channel = cu.unmarshal(result.getString("DATA"));
				channel.setId(result.getInt("ID"));
				channels.add(channel);
			}
		} catch (SQLException e) {
			throw new ConfigurationException(e);
		} catch (UnmarshalException e) {
			throw new ConfigurationException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}
	
	/**
	 * Returns a <code>List</code> containing all Channels.
	 * 
	 * @return
	 */
	public List<Channel> getChannels() throws ConfigurationException {
		if (!isInitialized()) {
			throw new ConfigurationException("Configuration must be initialized first.");
		}
		
		return channels;
	}
	
	private void storeChannels() throws ConfigurationException {
		try {
			dbConnection = new DatabaseConnection();
			
			for (Iterator iter = channels.iterator(); iter.hasNext();) {
				Channel channel = (Channel) iter.next();
				StringBuffer insert = new StringBuffer();
				insert.append("INSERT INTO CHANNELS (ID, NAME, DATA) VALUES(");
				insert.append(channel.getId() + ",");
				insert.append("'" + channel.getName() + "'");
				
				ChannelMarshaller cm = new ChannelMarshaller();
				StringWriter stringWriter = new StringWriter();
				cm.marshal(channel, new StringOutputStream(stringWriter));
				insert.append("'" + stringWriter.toString() + "');");

				dbConnection.update(insert.toString());
			}
		} catch (SQLException e) {
			throw new ConfigurationException(e);
		} catch (MarshalException e) {
			throw new ConfigurationException(e);
		} finally {
			dbConnection.close();
		}
	}
	
	private void loadTransports() throws ConfigurationException {
		transports = new ArrayList<Transport>();
		ResultSet result = null;
		
		try {
			dbConnection = new DatabaseConnection();
			result = dbConnection.query("SELECT NAME, DISPLAY_NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS FROM TRANSPORTS;");
			
			while (result.next()) {
				Transport transport = new Transport();
				transport.setName(result.getString("NAME"));
				transport.setDisplayName(result.getString("DISPLAY_NAME"));
				transport.setClassName(result.getString("CLASS_NAME"));
				transport.setProtocol(result.getString("PROTOCOL"));
				transport.setTransformers(result.getString("TRANSFORMERS"));
				transports.add(transport);
			}
		} catch (SQLException e) {
			throw new ConfigurationException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}
	
	/**
	 * Returns a <code>List</code> containing all Transports.
	 * 
	 * @return
	 */
	public List<Transport> getTransports() throws ConfigurationException {
		if (!isInitialized()) {
			throw new ConfigurationException("Configuration must be initialized first.");
		}
		
		return transports;
	}
	
	private void storeTransports() throws ConfigurationException {
		// TODO: write the transports list to the database
	}
	
	private void loadProperties() throws ConfigurationException {
		properties = PropertyLoader.loadProperties("mirth");
		
		if (properties == null) {
			throw new ConfigurationException("Could not load properties.");
		}
	}
	
	/**
	 * Returns a <code>Properties</code> list.
	 * 
	 * @return
	 */
	public Properties getProperties() throws ConfigurationException {
		if (!isInitialized()) {
			throw new ConfigurationException("Configuration must be initialized first.");
		}
		
		return properties;
	}
	
	private void storeProperties() throws ConfigurationException {
		try {
			FileOutputStream fos = new FileOutputStream("mirth.properties");
			properties.store(fos, null);
		} catch (Exception e) {
			throw new ConfigurationException(e);
		}
	}
	
	/**
	 * Stores all configuration information to the database.
	 * 
	 */
	public void store() throws ConfigurationException {
		try {
			storeUsers();
			storeTransports();
			storeProperties();
			storeChannels();
		} catch (ConfigurationException e) {
			throw e;
		}
	}
	
	public String getMuleConfiguration() throws ConfigurationException {
		try {
			MuleConfigurationBuilder builder = new MuleConfigurationBuilder(getChannels(), getTransports());
			return builder.getConfiguration();
		} catch (ConfigurationException e) {
			throw e;
		} catch (ConfigurationBuilderException e) {
			throw new ConfigurationException("Could not generate Mule configuration.", e);
		}
	}
	
	/**
	 * Returns the next available id if avaiable, otherwise returns -1.
	 * 
	 * @return
	 */
	public int getNextId() throws RuntimeException {
		dbConnection = new DatabaseConnection();
		ResultSet result = null;
		int id = -1;
		
		try {
			result = dbConnection.query("SELECT NEXT VALUE FOR SEQ_CONFIGURATION FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_NAME='SEQ_CONFIGURATION';");
			result.next();
			
			if (result.getInt(1) > 0) {
				id = result.getInt(1);	
			}
		} catch (SQLException e) {
			throw new RuntimeException("Could not generate next unique ID.", e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
		
		return id;
	}
}
