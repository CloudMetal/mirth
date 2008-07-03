package com.webreach.mirth.util;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelProperties;
import com.webreach.mirth.model.ComponentProperties;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.Waypoint;
import com.webreach.mirth.model.WaypointProperties;

public class PropertyVerifier
{
    /** A method to compare two properties file to check if they are the same. */
    public static boolean compareProps(Properties p1, Properties p2)
    {
        Enumeration<?> propertyKeys = p1.propertyNames();
        while (propertyKeys.hasMoreElements())
        {
            String key = (String) propertyKeys.nextElement();
            // System.out.println(key + " " + p1.getProperty(key) + " " +
            // p2.getProperty(key));
            if (p1.getProperty(key) == null)
            {
                if (p2.getProperty(key) != null)
                    return false;
            }
            else if (!p1.getProperty(key).equals(p2.getProperty(key)))
                return false;
        }
        return true;
    }
    
    /** A method to add default properties to a waypoint. */
    public static void checkWaypointProperties(Waypoint waypoint)
    {
        fixMissingOrInvalidProperties(new WaypointProperties().getDefaults(), waypoint.getProperties());
    }
    
    /** A method to add default properties to a channel. */
    public static void checkChannelProperties(Channel channel)
    {
        fixMissingOrInvalidProperties(new ChannelProperties().getDefaults(), channel.getProperties());
    }
    
    /** A method to add default connector properties to a channel. */
    public static void checkConnectorProperties(Channel channel, Map<String, ConnectorMetaData> connectorData)
    {
        PropertyVerifier.checkPropertyValidity(channel.getSourceConnector(), connectorData);
    
        List<Connector> destinations = channel.getDestinationConnectors();
        for (int i = 0; i < destinations.size(); i++)
        {
            PropertyVerifier.checkPropertyValidity(destinations.get(i), connectorData);
        }
    }
    
    /**
     * Gets the default properties for a connector, and fixes invalid/missing properties
     */
    private static void checkPropertyValidity(Connector connector, Map<String, ConnectorMetaData> connectorData)
    {
        
        Properties properties = connector.getProperties();
        Properties propertiesDefaults = null;
        
        try
        {
            propertiesDefaults = ((ComponentProperties)Class.forName(connectorData.get(connector.getTransportName()).getSharedClassName()).newInstance()).getDefaults();
            fixMissingOrInvalidProperties(propertiesDefaults, properties);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }        
    }
    
    /**
     * Checks for properties that are new or not used and adds or removes them from a Properties object.
     */    
    private static void fixMissingOrInvalidProperties(Properties propertiesDefaults, Properties properties)
    {
        Enumeration<?> propertyKeys;
        propertyKeys = properties.propertyNames();
        while (propertyKeys.hasMoreElements())
        {
            String key = (String) propertyKeys.nextElement();
            if (propertiesDefaults.getProperty(key) == null)
            {
                properties.remove(key);
            }
        }

        propertyKeys = propertiesDefaults.propertyNames();
        while (propertyKeys.hasMoreElements())
        {
            String key = (String) propertyKeys.nextElement();
            if (properties.getProperty(key) == null)
            {
                if (propertiesDefaults.getProperty(key) != null)
                    properties.put(key, propertiesDefaults.getProperty(key));
            }
        }
    }
}
