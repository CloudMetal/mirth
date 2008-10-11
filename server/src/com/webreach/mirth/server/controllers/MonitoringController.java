package com.webreach.mirth.server.controllers;

import java.net.Socket;

import org.mule.umo.provider.UMOConnector;

import com.webreach.mirth.model.ExtensionPoint;
import com.webreach.mirth.model.ExtensionPointDefinition;

public abstract class MonitoringController extends Controller {
    public enum Event {
        CONNECTED, DISCONNECTED, INITIALIZED, BUSY, DONE, ATTEMPTING_TO_CONNECT
    };

    public enum ConnectorType {
        LISTENER, SENDER, READER, WRITER
    };
    
    public MonitoringController getInstance() {
        return ControllerFactory.getFactory().createMonitoringController();
    }

    public abstract void updateStatus(String connectorName, ConnectorType type, Event event, Socket socket);

    public abstract void updateStatus(UMOConnector connector, ConnectorType type, Event event);

    public abstract void updateStatus(UMOConnector connector, ConnectorType type, Event event, Socket socket);

    // Extension point for ExtensionPoint.Type.SERVER_PLUGIN
    @ExtensionPointDefinition(mode = ExtensionPoint.Mode.SERVER, type = ExtensionPoint.Type.SERVER_CONNECTOR_STATUS)
    public abstract void initPlugins();
}
