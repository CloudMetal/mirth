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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ChannelStatusController;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;

public class ChannelStatusServlet extends MirthServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            try {
                ChannelStatusController channelStatusController = ControllerFactory.getFactory().createChannelStatusController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                PrintWriter out = response.getWriter();
                String operation = request.getParameter("op");

                if (operation.equals(Operations.CHANNEL_START)) {
                    channelStatusController.startChannel(request.getParameter("id"));
                } else if (operation.equals(Operations.CHANNEL_STOP)) {
                    channelStatusController.stopChannel(request.getParameter("id"));
                } else if (operation.equals(Operations.CHANNEL_PAUSE)) {
                    channelStatusController.pauseChannel(request.getParameter("id"));
                } else if (operation.equals(Operations.CHANNEL_RESUME)) {
                    channelStatusController.resumeChannel(request.getParameter("id"));
                } else if (operation.equals(Operations.CHANNEL_GET_STATUS)) {
                    response.setContentType("application/xml");
                    out.print(serializer.toXML(channelStatusController.getChannelStatusList()));
                }
            } catch (ControllerException e) {
                throw new ServletException(e);
            }
        }
    }
}
