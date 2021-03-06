/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.event.ConnectorEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.event.ConnectorEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.util.AttachmentUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ErrorMessageBuilder;

public class VmDispatcher extends DestinationConnector {
    private static final String SOURCE_CHANNEL_ID = "sourceChannelId";
    private static final String SOURCE_CHANNEL_IDS = "sourceChannelIds";
    private static final String SOURCE_MESSAGE_ID = "sourceMessageId";
    private static final String SOURCE_MESSAGE_IDS = "sourceMessageIds";

    private VmDispatcherProperties connectorProperties;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private ExecutorService executor;
    private int timeout;
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (VmDispatcherProperties) getConnectorProperties();
        timeout = NumberUtils.toInt(connectorProperties.getResponseTimeout(), 0);

        if (timeout > 0) {
            /*
             * This executor is created on deploy so it can be shutdown on
             * undeploy. If it were created on start, it would need to be
             * shutdown on stop. However, we don't want to shutdown the executor
             * on stop because we want to let any lingering messages finish.
             * 
             * A cached thread pool is used in case multiple threads are reading
             * from the source queue.
             */
            executor = Executors.newCachedThreadPool();
        }
    }

    @Override
    public void onUndeploy() throws UndeployException {
        if (executor != null) {
            executor.shutdown();
        }
    }

    @Override
    public void onStart() throws StartException {
        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.IDLE));
    }

    @Override
    public void onStop() throws StopException {
        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.DISCONNECTED));
    }

    @Override
    public void onHalt() throws HaltException {
        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.DISCONNECTED));
    }

    @Override
    public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
        VmDispatcherProperties vmDispatcherProperties = (VmDispatcherProperties) connectorProperties;

        vmDispatcherProperties.setChannelId(replacer.replaceValues(vmDispatcherProperties.getChannelId(), connectorMessage));
        vmDispatcherProperties.setChannelTemplate(replacer.replaceValues(vmDispatcherProperties.getChannelTemplate(), connectorMessage));
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage message) {
        VmDispatcherProperties vmDispatcherProperties = (VmDispatcherProperties) connectorProperties;

        String targetChannelId = vmDispatcherProperties.getChannelId();
        String currentChannelId = getChannelId();

        eventController.dispatchEvent(new ConnectorEvent(currentChannelId, getMetaDataId(), getDestinationName(), ConnectorEventType.SENDING, "Target Channel: " + targetChannelId));

        String responseData = null;
        String responseError = null;
        String responseStatusMessage = null;
        Status responseStatus = Status.QUEUED; // Always set the status to QUEUED

        try {
            if (!targetChannelId.equals("none")) {
                boolean isBinary = ExtensionController.getInstance().getDataTypePlugins().get(this.getOutboundDataType().getType()).isBinary();
                byte[] data = AttachmentUtil.reAttachMessage(vmDispatcherProperties.getChannelTemplate(), message, Constants.ATTACHMENT_CHARSET, isBinary);

                RawMessage rawMessage;

                if (isBinary) {
                    rawMessage = new RawMessage(data);
                } else {
                    rawMessage = new RawMessage(StringUtils.newString(data, Constants.ATTACHMENT_CHARSET));
                }

                Map<String, Object> rawChannelMap = rawMessage.getChannelMap();
                Map<String, Object> channelMap = message.getChannelMap();

                /*
                 * Build the lists of source channel and message Ids if this
                 * channel is not the start of the chain.
                 */
                List<String> sourceChannelIds = getSourceChannelIds(channelMap);
                List<Long> sourceMessageIds = getSourceMessageIds(channelMap);

                // Add the current channelId to the chain if it is built
                if (sourceChannelIds != null) {
                    sourceChannelIds.add(currentChannelId);
                    rawChannelMap.put(SOURCE_CHANNEL_IDS, sourceChannelIds);
                }

                // Add the current messageId to the chain if it is built
                if (sourceMessageIds != null) {
                    sourceMessageIds.add(message.getMessageId());
                    rawChannelMap.put(SOURCE_MESSAGE_IDS, sourceMessageIds);
                }

                // Always store the originating channelId and messageId
                rawChannelMap.put(SOURCE_CHANNEL_ID, currentChannelId);
                rawChannelMap.put(SOURCE_MESSAGE_ID, message.getMessageId());

                // Remove the reference to the raw message so its doesn't hold the entire message in memory.
                data = null;

                DispatchResult dispatchResult = null;

                if (timeout > 0) {
                    dispatchResult = executor.submit(new DispatchTask(targetChannelId, rawMessage)).get(timeout, TimeUnit.MILLISECONDS);
                } else {
                    dispatchResult = ControllerFactory.getFactory().createEngineController().dispatchRawMessage(targetChannelId, rawMessage);
                }

                if (dispatchResult.getSelectedResponse() != null) {
                    // If a response was returned from the channel then use that message
                    responseData = dispatchResult.getSelectedResponse().getMessage();
                }
            }

            responseStatus = Status.SENT;
            responseStatusMessage = "Message routed successfully to channel id: " + targetChannelId;
        } catch (Throwable e) {
            Throwable cause;
            if (e instanceof ExecutionException) {
                cause = e.getCause();
            } else {
                cause = e;
            }

            String shortMessage = "Error routing message to channel id: " + targetChannelId;
            String longMessage = shortMessage;

            if (e instanceof TimeoutException) {
                longMessage += ". A cycle may be present where a channel is attempting to dispatch a message to itself. If this is the case, enable queuing on the source or destination connectors, otherwise try increasing the response timeout.";
            }

            eventController.dispatchEvent(new ErrorEvent(currentChannelId, getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), longMessage, cause));
            responseStatusMessage = ErrorMessageBuilder.buildErrorResponse(shortMessage, cause);
            responseError = ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), longMessage, cause);
        } finally {
            eventController.dispatchEvent(new ConnectorEvent(currentChannelId, getMetaDataId(), getDestinationName(), ConnectorEventType.IDLE));
        }

        return new Response(responseStatus, responseData, responseStatusMessage, responseError);
    }

    private List<String> getSourceChannelIds(Map<String, Object> map) {
        Object object = map.get(SOURCE_CHANNEL_ID);

        List<String> sourceChannelIds = null;

        /*
         * If the source channel id already exists, then a source channel id
         * list needs to be created to store the historical channel ids.
         */
        if (object != null && object instanceof String) {
            String sourceChannelId = (String) object;
            sourceChannelIds = new ArrayList<String>();

            Object listObject = map.get(SOURCE_CHANNEL_IDS);

            /*
             * If the source channel id list already exists, add all items into
             * the new list. Otherwise only add the previous channel id to the
             * new list.
             */
            if (listObject == null) {
                sourceChannelIds.add(sourceChannelId);
            } else {
                try {
                    sourceChannelIds.addAll((List<String>) listObject);
                } catch (ClassCastException e) {
                    sourceChannelIds.add(sourceChannelId);
                }
            }
        }

        return sourceChannelIds;
    }

    private List<Long> getSourceMessageIds(Map<String, Object> map) {
        Object object = map.get(SOURCE_MESSAGE_ID);

        List<Long> sourceMessageIds = null;

        /*
         * If the source message id already exists, then a source message id
         * list needs to be created to store the historical message ids.
         */
        if (object != null && object instanceof Long) {
            Long sourceMessageId = (Long) object;
            sourceMessageIds = new ArrayList<Long>();

            Object listObject = map.get(SOURCE_MESSAGE_IDS);

            /*
             * If the source message id list already exists, add all items into
             * the new list. Otherwise only add the previous message id to the
             * new list.
             */
            if (listObject == null) {
                sourceMessageIds.add(sourceMessageId);
            } else {
                try {
                    sourceMessageIds.addAll((List<Long>) listObject);
                } catch (ClassCastException e) {
                    sourceMessageIds.add(sourceMessageId);
                }
            }
        }

        return sourceMessageIds;
    }

    private class DispatchTask implements Callable<DispatchResult> {

        private String channelId;
        private RawMessage rawMessage;

        public DispatchTask(String channelId, RawMessage rawMessage) {
            this.channelId = channelId;
            this.rawMessage = rawMessage;
        }

        @Override
        public DispatchResult call() throws ChannelException {
            return ControllerFactory.getFactory().createEngineController().dispatchRawMessage(channelId, rawMessage);
        }
    }
}
