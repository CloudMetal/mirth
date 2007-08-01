/*
 * PluginManagerClient.java
 *
 * Created on June 22, 2007, 5:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.plugins.pluginmanager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;

import org.jdesktop.swingworker.SwingWorker;

import sun.misc.BASE64Encoder;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.plugins.ClientPanelPlugin;

/**
 *
 * @author brendanh
 */
public class PluginManagerClient extends ClientPanelPlugin
{
    public PluginManagerClient(String name)
    {
        super(name);
        
        getTaskPane().setTitle("Manager Tasks");
        setComponent(new PluginManagerPanel(this));
        
        addTask("doRefresh", "Refresh", "Refresh loaded plugins.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/refresh.png")));
        addTask("doSave", "Save", "Save plugin settings.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/save.png")));
        addTask("doCheckForUpdates", "Check for Updates", "Checks for updates.", "", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/world_go.png")));
        addTask("doEnable","Enable Extension","Enable the currently selected extension.","", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/start.png")));
        addTask("doDisable","Disable Extension","Disable the currently selected extension.","", new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/stop.png")));
        
        setVisibleTasks(0, 2, true);
        setVisibleTasks(3, -1, false);
        
        getComponent().addMouseListener(getPopupMenuMouseAdapter());
    }
    
    public void doCheckForUpdates()
    {
        try
        {
            new UpdateDialog(this);
        }
        catch (ClientException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void doRefresh()
    {
        setWorking("Loading plugin settings...", true);
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                try
                {
                    refresh();
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }
            
            public void done()
            {
                setWorking("", false);
            }
        };
        
        worker.execute();
    }
    
    public void doSave()
    {
        setWorking("Saving plugin settings...", true);
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                try
                {
                    save();
                    alertInformation("A restart is required before your changes will take effect.");
                }
                catch (ClientException e)
                {
                    alertException(e.getStackTrace(), e.getMessage());
                }
                return null;
            }
            
            public void done()
            {
                setWorking("", false);
            }
        };
        
        worker.execute();
    }
    
    public void doEnable()
    {
        ((PluginManagerPanel) getComponent()).enableExtension();
    }
    
    public void doDisable()
    {
        ((PluginManagerPanel) getComponent()).disableExtension();
    }
    
    public void refresh() throws ClientException
    {
        ((PluginManagerPanel) getComponent()).setPluginData(PlatformUI.MIRTH_FRAME.mirthClient.getPluginMetaData());
        ((PluginManagerPanel) getComponent()).setConnectorData(PlatformUI.MIRTH_FRAME.mirthClient.getConnectorMetaData());
    }
    
    public void save() throws ClientException
    {
        PlatformUI.MIRTH_FRAME.mirthClient.setPluginMetaData(((PluginManagerPanel) getComponent()).getPluginData());
        PlatformUI.MIRTH_FRAME.mirthClient.setConnectorMetaData(((PluginManagerPanel) getComponent()).getConnectorData());
    }
    
    public boolean install(String location, File file)
    {
        try
        {
            byte[] bytes = getBytesFromFile(file);
            String contents = "";
            BASE64Encoder encoder = new BASE64Encoder();
            contents = encoder.encode(bytes);
            PlatformUI.MIRTH_FRAME.mirthClient.installExtension(location, contents);
        }
        catch(Exception e)
        {
            alertError("Invalid extension file.");
            return false;
        }
        return true;
    }
    
    // Returns the contents of the file in a byte array.
    private byte[] getBytesFromFile(File file) throws IOException
    {
        InputStream is = new FileInputStream(file);
        
        // Get the size of the file
        long length = file.length();
        
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE)
        {
            // File is too large
        }
        
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];
        
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
        {
            offset += numRead;
        }
        
        // Ensure all the bytes have been read in
        if (offset < bytes.length)
        {
            throw new IOException("Could not completely read file " + file.getName());
        }
        
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
    
    public void start()
    {
        try
        {
            refresh();
        }
        catch(ClientException e)
        {
            
        }
    }
    
    public void stop()
    {
        try
        {
            save();
        }
        catch(ClientException e)
        {
            
        }
    }
    
    public void display()
    {
        try
        {
            refresh();
        }
        catch(ClientException e)
        {
            
        }
    }
}
