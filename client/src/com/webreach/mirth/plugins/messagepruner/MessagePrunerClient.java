/*
 * MessagePrunerClient.java
 *
 * Created on June 22, 2007, 5:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.plugins.messagepruner;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.SwingWorker;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.plugins.ClientPanelPlugin;

public class MessagePrunerClient extends ClientPanelPlugin
{
    public MessagePrunerClient(String name)
    {
        super(name, true, true);

        getTaskPane().setTitle("Pruner Tasks");
        setComponent(new MessagePrunerPanel());
        setVisibleTasks(getRefreshIndex(), getRefreshIndex(), true);
        setVisibleTasks(getSaveIndex(), getSaveIndex(), false);
        getComponent().addMouseListener(getPopupMenuMouseAdapter());
    }

    public void doRefresh()
    {
        setWorking("Loading pruner properties...", true);

        final Properties serverProperties = new Properties();
        final List<String[]> log = new LinkedList<String[]>();
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                try
                {
                	if (!confirmLeave())
                		return null;

                	if (getPropertiesFromServer() != null) {
                	    serverProperties.putAll(getPropertiesFromServer());
                	}
               	        
                	log.addAll((List<String[]>) invoke("getLog", null));
                }
                catch (ClientException e)
                {
                    alertException(parent, e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done()
            {
                ((MessagePrunerPanel) getComponent()).setProperties(serverProperties, log);
                setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doSave()
    {
        setWorking("Saving pruner properties...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                try
                {
                    save();
                }
                catch (ClientException e)
                {
                    alertException(parent, e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done()
            {
            	disableSave();
                setWorking("", false);
            }
        };

        worker.execute();
    }
    
    public void refresh() throws ClientException
    {
        
    }
    
    public void save() throws ClientException
    {
        setPropertiesToServer(((MessagePrunerPanel) getComponent()).getProperties());
    }
    
    public void start()
    {

    }

    public void stop()
    {

    }
    
    public void display()
    {
    	doRefresh();
    }
}
