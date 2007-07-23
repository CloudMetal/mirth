package com.webreach.mirth.server.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.webreach.mirth.model.MetaData;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.controllers.ControllerException;

public class ExtensionUtil
{
    public static Map<String, ? extends MetaData> loadExtensionMetaData(String location) throws ControllerException
    {
        FileFilter fileFilter = new FileFilter()
        {
            public boolean accept(File file)
            {
                return (!file.isDirectory() && file.getName().endsWith(".xml"));
            }
        };

        Map<String, MetaData> extensionMap = new HashMap<String, MetaData>();
        File path = new File(location);
        File[] extensionFiles = path.listFiles(fileFilter);
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();

        try
        {
            for (int i = 0; i < extensionFiles.length; i++)
            {
                File extensionFile = extensionFiles[i];
                String xml = FileUtil.read(extensionFile.getAbsolutePath());
                MetaData extensionMetadata = (MetaData) serializer.fromXML(xml);
                extensionMap.put(extensionMetadata.getName(), extensionMetadata);
            }
        }
        catch (IOException ioe)
        {
            throw new ControllerException(ioe);
        }

        return extensionMap;
    }

    public static void saveExtensionMetaData(Map<String, ? extends MetaData> metaData, String location) throws ControllerException
    {
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();

        try
        {
            Iterator i = metaData.entrySet().iterator();
            while (i.hasNext())
            {
                Entry entry = (Entry) i.next();
                FileUtil.write(entry.getKey().toString() + ".xml", false, serializer.toXML(metaData.get(entry.getKey())));

            }
        }
        catch (IOException ioe)
        {
            throw new ControllerException(ioe);
        }

    }

    public static List<String> loadExtensionLibraries(String location) throws ControllerException
    {
        // update this to use regular expression to get the client and shared
        // libraries
        FileFilter libraryFilter = new FileFilter()
        {
            public boolean accept(File file)
            {
                return (!file.isDirectory() && (file.getName().contains("-client.jar") || file.getName().contains("-shared.jar")));
            }
        };

        List<String> extensionLibs = new ArrayList<String>();
        File path = new File(location);
        File[] extensionFiles = path.listFiles(libraryFilter);

        for (int i = 0; i < extensionFiles.length; i++)
        {
            File extensionFile = extensionFiles[i];
            extensionLibs.add(extensionFile.getName());
        }

        return extensionLibs;
    }

    public static void installExtension(String location, byte[] contents) throws ControllerException
    {
        // update this to use regular expression to get the client and shared
        // libraries
        String uniqueId = UUIDGenerator.getUUID();

        ZipFile zipFile = null;
        try
        {
            File file = File.createTempFile(uniqueId, ".zip");
            String zipFileLocation = file.getAbsolutePath();       
           
            FileUtil.write(zipFileLocation, false, contents);
            zipFile = new ZipFile(zipFileLocation);

            Enumeration entries = zipFile.entries();

            while (entries.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry) entries.nextElement();

                if (entry.isDirectory())
                {
                    // Assume directories are stored parents first then
                    // children.

                    // This is not robust, just for demonstration purposes.
                    (new File(entry.getName())).mkdir();
                    continue;
                }

                copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(new File(location + entry.getName()))));
            }
        }
        catch (Exception e)
        {
            throw new ControllerException(e);
        }
        finally
        {
            if (zipFile != null)
            {
                try
                {
                    zipFile.close();
                }
                catch (Exception e)
                {
                    throw new ControllerException(e);
                }
            }
        }
    }

    public static final void copyInputStream(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);

        in.close();
        out.close();
    }

}
