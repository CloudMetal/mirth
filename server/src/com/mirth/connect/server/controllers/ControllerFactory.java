/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.Properties;

import com.mirth.connect.util.PropertyLoader;

public abstract class ControllerFactory {
    private static ControllerFactory factory;

    public static ControllerFactory getFactory() {
        synchronized (ControllerFactory.class) {
            if (factory == null) {
                Properties mirthProperties = PropertyLoader.loadProperties("mirth");
                String factoryClassName = mirthProperties.getProperty("controllerfactory");

                if (factoryClassName != null) {
                    try {
                        factory = (ControllerFactory) Class.forName(factoryClassName).newInstance();
                    } catch (Exception e) {
                        // couldn't find a factory
                    }
                }

                factory = new DefaultControllerFactory();
            }

            return factory;
        }
    }

    public abstract AlertController createAlertController();

    public abstract ChannelController createChannelController();

    public abstract ChannelStatisticsController createChannelStatisticsController();

    public abstract ChannelStatusController createChannelStatusController();

    public abstract CodeTemplateController createCodeTemplateController();

    public abstract ConfigurationController createConfigurationController();
    
    public abstract EngineController createEngineController();

    public abstract ExtensionController createExtensionController();

    public abstract MessageObjectController createMessageObjectController();

    public abstract MigrationController createMigrationController();

    public abstract MonitoringController createMonitoringController();

    public abstract ScriptController createScriptController();

    public abstract EventController createEventController();

    public abstract TemplateController createTemplateController();

    public abstract UserController createUserController();
}
