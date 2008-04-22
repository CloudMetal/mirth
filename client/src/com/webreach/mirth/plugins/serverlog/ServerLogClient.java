package com.webreach.mirth.plugins.serverlog;

import com.webreach.mirth.plugins.DashboardPanelPlugin;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.ChannelStatus;

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: chrisr
 * Date: Oct 26, 2007
 * Time: 11:28:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class ServerLogClient extends DashboardPanelPlugin
{
    private ServerLogPanel serverLogPanel;
    private LinkedList<String[]> serverLogs;
    private static final String GET_SERVER_LOGS = "getMirthServerLogs";
    private static final String REMOVE_SESSIONID = "removeSessionId";
    private static final String SERVER_PLUGIN_NAME = "Server Log";
    private int currentServerLogSize;

    public ServerLogClient(String name)
    {
        super(name);
        serverLogs = new LinkedList<String[]>();
        serverLogPanel = new ServerLogPanel(this);
        currentServerLogSize = serverLogPanel.getCurrentServerLogSize();
        setComponent(serverLogPanel);
    }

    public void clearLog() {
        serverLogs.clear();
        serverLogPanel.updateTable(null);
    }

    public void resetServerLogSize(int newServerLogSize) {

        // the log size is always set to 100 on the server.
        // on the client side, the max size is 99.  whenever that changes, only update the client side logs. the logs on the server will always be intact.
        // Q. Does this log size affect all the channels? - Yes, it should.

        // update (refresh) log only if the new logsize got smaller.
        if (newServerLogSize < currentServerLogSize) {
            // if log size got reduced...  remove that much extra LastRows.
            synchronized(this) {
                while (newServerLogSize < serverLogs.size()) {
                    serverLogs.removeLast();
                }
            }
            serverLogPanel.updateTable(serverLogs);
        }

        // reset currentServerLogSize.
        currentServerLogSize = newServerLogSize;
    }

    // used for setting actions to be called for updating when there is no status selected
    public void update() {
        if (!serverLogPanel.isPaused()) {
            LinkedList<String[]> serverLogReceived = new LinkedList<String[]>();
            //get logs from server
            try {
                serverLogReceived = (LinkedList<String[]>) PlatformUI.MIRTH_FRAME.mirthClient.invokePluginMethod(SERVER_PLUGIN_NAME, GET_SERVER_LOGS, null);
            } catch (ClientException e) {
                e.printStackTrace();
            }

            if (serverLogReceived.size() > 0) {
                synchronized(this) {
                    for (int i = serverLogReceived.size()-1; i >= 0; i--) {
                        while (currentServerLogSize <= serverLogs.size()) {
                            serverLogs.removeLast();
                        }
                        serverLogs.addFirst(serverLogReceived.get(i));
                    }
                }

                // for mirth.log, channel being selected does not matter. display either way.
                serverLogPanel.updateTable(serverLogs);
            }
        }
    }

    // used for setting actions to be called for updating when there is a status selected
    public void update(ChannelStatus status) {

        // Mirth Server Log is irrelevant with Channel Status.  so just call the default update() method.
        update();

    }

    // used for starting processes in the plugin when the program is started
    public void start() {

    }

    // used for stopping processes in the plugin when the program is exited
    public void stop() {
        // invoke method to remove everything involving this client's sessionId.
        try {
            // FYI, method below returns a boolean value.
            // returned 'true' - sessionId found and removed.
            // returned 'false' - sessionId not found. - should never be this case.
            // either way, the sessionId is gone.
            PlatformUI.MIRTH_FRAME.mirthClient.invokePluginMethod(SERVER_PLUGIN_NAME, REMOVE_SESSIONID, null);
        } catch (ClientException e) {
            parent.alertException(e.getStackTrace(), e.getMessage());
        }
    }
}