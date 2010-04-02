/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.CodeTemplateController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class CodeTemplateServlet extends MirthServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isUserLoggedIn(request)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try {
				CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();
				ObjectXMLSerializer serializer = new ObjectXMLSerializer();
				PrintWriter out = response.getWriter();
				String operation = request.getParameter("op");

				if (operation.equals("getCodeTemplate")) {
					response.setContentType("application/xml");
					CodeTemplate codeTemplate = (CodeTemplate) serializer.fromXML(request.getParameter("codeTemplate"));
					out.println(serializer.toXML(codeTemplateController.getCodeTemplate(codeTemplate)));
				} else if (operation.equals("updateCodeTemplates")) {
					List<CodeTemplate> codeTemplates = (List<CodeTemplate>) serializer.fromXML(request.getParameter("codeTemplates"));
					codeTemplateController.updateCodeTemplates(codeTemplates);
				} else if (operation.equals("removeCodeTemplate")) {
					CodeTemplate codeTemplate = (CodeTemplate) serializer.fromXML(request.getParameter("codeTemplate"));
					codeTemplateController.removeCodeTemplate(codeTemplate);
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}
