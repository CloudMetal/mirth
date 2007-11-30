/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.util.AttachmentUtil;

public class MessageObjectServlet extends MirthServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            try {
                MessageObjectController messageObjectController = MessageObjectController.getInstance();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                PrintWriter out = response.getWriter();
                String operation = request.getParameter("op");
                String uid = null;
                boolean useNewTempTable = false;

                if (request.getParameter("uid") != null && !request.getParameter("uid").equals("")) {
                    uid = request.getParameter("uid");
                    useNewTempTable = true;
                }
                else {
                    uid = request.getSession().getId();
                }

                if (operation.equals("createMessagesTempTable")) {
                    String filter = request.getParameter("filter");
                    response.setContentType("text/plain");
                    out.println(messageObjectController.createMessagesTempTable((MessageObjectFilter) serializer.fromXML(filter), uid, useNewTempTable));
                } else if (operation.equals("removeFilterTables")) {
                    messageObjectController.removeFilterTable(uid);
                } else if (operation.equals("getMessagesByPage")) {
                    String page = request.getParameter("page");
                    String pageSize = request.getParameter("pageSize");
                    String maxMessages = request.getParameter("maxMessages");
                    response.setContentType("application/xml");
                    out.print(serializer.toXML(messageObjectController.getMessagesByPage(Integer.parseInt(page), Integer.parseInt(pageSize), Integer.parseInt(maxMessages), uid)));
                } else if (operation.equals("getMessagesByPageLimit")) {
                    String page = request.getParameter("page");
                    String pageSize = request.getParameter("pageSize");
                    String maxMessages = request.getParameter("maxMessages");
                    String filter = request.getParameter("filter");
                    response.setContentType("application/xml");
                    out.print(serializer.toXML(messageObjectController.getMessagesByPageLimit(Integer.parseInt(page), Integer.parseInt(pageSize), Integer.parseInt(maxMessages), uid, (MessageObjectFilter) serializer.fromXML(filter))));
                } else if (operation.equals("removeMessages")) {
                    String filter = request.getParameter("filter");
                    messageObjectController.removeMessages((MessageObjectFilter) serializer.fromXML(filter));
                } else if (operation.equals("clearMessages")) {
                    String channelId = request.getParameter("data");
                    messageObjectController.clearMessages(channelId);
                } else if (operation.equals("reprocessMessages")) {
                    String filter = request.getParameter("filter");
                    messageObjectController.reprocessMessages((MessageObjectFilter) serializer.fromXML(filter));
                } else if (operation.equals("processMessage")) {
                    String message = request.getParameter("message");
                    messageObjectController.processMessage((MessageObject) serializer.fromXML(message));
                } else if (operation.equals("importMessage")) {
                    String message = request.getParameter("message");
                    messageObjectController.importMessage((MessageObject) serializer.fromXML(message));
                } else if (operation.equals("getAttachment")) {
                    response.setContentType("application/xml");
                    Attachment attachment = messageObjectController.getAttachment((String) request.getParameter("attachmentId"));
                    out.println(serializer.toXML(attachment));
                } else if(operation.equals("getAttachmentsByMessageId")){
                    response.setContentType("application/xml");
                    List<Attachment> list = messageObjectController.getAttachmentsByMessageId((String) request.getParameter("messageId"));
                    out.println(serializer.toXML(list));
                } else if(operation.equals("getAttachmentIdsByMessageId")){
                    response.setContentType("application/xml");
                    List<Attachment> list = messageObjectController.getAttachmentIdsByMessageId((String) request.getParameter("messageId"));
                    out.println(serializer.toXML(list));
                } else if(operation.equals("insertAttachment")) {
                    String attachment = request.getParameter("attachment");
                    messageObjectController.insertAttachment((Attachment) serializer.fromXML(attachment));
                } else if(operation.equals("getDICOMMessage")) {
                    //response.setContentType("application/xml");
                    String message = request.getParameter("message");
                    String dicomMessage = AttachmentUtil.getDICOMRawData((MessageObject) serializer.fromXML(message));
                    out.println(dicomMessage);
                } else if(operation.equals("deleteAttachments")) {
                    String message = request.getParameter("message");
                    messageObjectController.deleteAttachments((MessageObject) serializer.fromXML(message));
                } else if(operation.equals("deleteUnusedAttachments")) {
                    messageObjectController.deleteUnusedAttachments();
                }
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }
}
