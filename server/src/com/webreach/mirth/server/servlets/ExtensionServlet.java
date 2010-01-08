package com.webreach.mirth.server.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.ExtensionLibrary;
import com.webreach.mirth.model.MetaData;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.ExtensionController;

public class ExtensionServlet extends MirthServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isUserLoggedIn(request)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
				ObjectXMLSerializer serializer = new ObjectXMLSerializer();
				PrintWriter out = response.getWriter();
				FileItem multiPartFile = null;
				String operation = "";
				Map<String, String> multipartParameters = new HashMap<String, String>();
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				
				if (isMultipart) {
					// we need to load properties from the multipart data
					DiskFileItemFactory factory = new DiskFileItemFactory();
					
					String location = ExtensionController.getExtensionsPath() + "install_temp" + System.getProperty("file.separator");
					File locationFile = new File(location);
					if (!locationFile.exists()){
						locationFile.mkdir();
					}
					
					factory.setRepository(locationFile);

					ServletFileUpload upload = new ServletFileUpload(factory);
					List<FileItem> items = upload.parseRequest(request);
					
					for (FileItem item : items) {
						if (item.isFormField()) {
							multipartParameters.put(item.getFieldName(), item.getString());
						} else {
							// only supports a single file
							multiPartFile = item;
						}
					}
					operation = multipartParameters.get("op");
				} else {
					operation = request.getParameter("op");
				}
				if (operation.equals("getPluginProperties")) {
					response.setContentType("application/xml");
					String name = request.getParameter("name");
					out.println(serializer.toXML(extensionController.getPluginProperties(name)));
				} else if (operation.equals("setPluginProperties")) {
					String name = request.getParameter("name");
					Properties properties = (Properties) serializer.fromXML(request.getParameter("properties"));
					extensionController.setPluginProperties(name, properties);
					extensionController.updatePlugin(name, properties);
				} else if (operation.equals("getPluginMetaData")) {
					out.println(serializer.toXML(extensionController.getPluginMetaData(), new Class[] { MetaData.class, PluginMetaData.class, ExtensionLibrary.class }));
				} else if (operation.equals("setPluginMetaData")) {
					Map<String, PluginMetaData> metaData = (Map<String, PluginMetaData>) serializer.fromXML(request.getParameter("metaData"), new Class[] { MetaData.class, PluginMetaData.class, ExtensionLibrary.class });
					extensionController.savePluginMetaData(metaData);
				} else if (operation.equals("getConnectorMetaData")) {
					response.setContentType("application/xml");
					out.println(serializer.toXML(extensionController.getConnectorMetaData(), new Class[] { MetaData.class, ConnectorMetaData.class, ExtensionLibrary.class }));
				} else if (operation.equals("setConnectorMetaData")) {
					Map<String, ConnectorMetaData> metaData = (Map<String, ConnectorMetaData>) serializer.fromXML(request.getParameter("metaData"), new Class[] { MetaData.class, ConnectorMetaData.class, ExtensionLibrary.class });
					extensionController.saveConnectorMetaData(metaData);
				} else if (operation.equals("isExtensionEnabled")) {
					String extensionName = request.getParameter("name");
					out.println(extensionController.isExtensionEnabled(extensionName));
				} else if (operation.equals("invoke")) {
					String name = request.getParameter("name");
					String method = request.getParameter("method");
					Object object = serializer.fromXML(request.getParameter("object"));
					String sessionId = request.getSession().getId();
					out.println(serializer.toXML(extensionController.invoke(name, method, object, sessionId)));
				} else if (operation.equals("invokeConnectorService")) {
					String name = request.getParameter("name");
					String method = request.getParameter("method");
					Object object = serializer.fromXML(request.getParameter("object"));
					String sessionId = request.getSession().getId();
					out.println(serializer.toXML(extensionController.invokeConnectorService(name, method, object, sessionId)));
				} else if (operation.equals("uninstallExtension")) {
					String packageName = request.getParameter("packageName");
					extensionController.uninstallExtension(packageName);
				} else if (operation.equals("installExtension")) {
					// This is a multi-part method, so we need our parameters
					// from the new map
					extensionController.installExtension(multiPartFile);
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}