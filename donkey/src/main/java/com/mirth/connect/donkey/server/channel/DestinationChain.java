/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.channel.components.FilterTransformerExecutor;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.util.DonkeyCloner;
import com.mirth.connect.donkey.util.DonkeyClonerFactory;
import com.mirth.connect.donkey.util.ThreadUtils;

public class DestinationChain implements Callable<List<ConnectorMessage>> {
    private String channelId;
    private ConnectorMessage message;
    private List<Integer> metaDataIds = new ArrayList<Integer>();
    private List<Integer> enabledMetaDataIds = new ArrayList<Integer>();
    private Map<Integer, FilterTransformerExecutor> filterTransformerExecutors = new HashMap<Integer, FilterTransformerExecutor>();
    private Map<Integer, DestinationConnector> destinationConnectors = new LinkedHashMap<Integer, DestinationConnector>();
    private MetaDataReplacer metaDataReplacer;
    private List<MetaDataColumn> metaDataColumns = new ArrayList<MetaDataColumn>();
    private DonkeyCloner cloner = DonkeyClonerFactory.getInstance().getCloner();
    private DonkeyDaoFactory daoFactory;
    private StorageSettings storageSettings;
    private Logger logger = Logger.getLogger(getClass());

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public void addDestination(int metaDataId, FilterTransformerExecutor filterTransformerExecutor, DestinationConnector connector) {
        if (!metaDataIds.contains(metaDataId)) {
            metaDataIds.add(metaDataId);
        }

        if (!enabledMetaDataIds.contains(metaDataId)) {
            enabledMetaDataIds.add(metaDataId);
        }

        filterTransformerExecutors.put(metaDataId, filterTransformerExecutor);
        destinationConnectors.put(metaDataId, connector);
    }

    public Map<Integer, FilterTransformerExecutor> getFilterTransformerExecutors() {
        return filterTransformerExecutors;
    }

    public Map<Integer, DestinationConnector> getDestinationConnectors() {
        return destinationConnectors;
    }

    public void setMessage(ConnectorMessage message) {
        this.message = message;
    }

    public List<Integer> getMetaDataIds() {
        return metaDataIds;
    }

    public List<Integer> getEnabledMetaDataIds() {
        return enabledMetaDataIds;
    }

    public void setEnabledMetaDataIds(List<Integer> enabledMetaDataIds) {
        this.enabledMetaDataIds = enabledMetaDataIds;
    }

    public MetaDataReplacer getMetaDataReplacer() {
        return metaDataReplacer;
    }

    public void setMetaDataReplacer(MetaDataReplacer metaDataReplacer) {
        this.metaDataReplacer = metaDataReplacer;
    }

    public List<MetaDataColumn> getMetaDataColumns() {
        return metaDataColumns;
    }

    public void setMetaDataColumns(List<MetaDataColumn> metaDataColumns) {
        this.metaDataColumns = metaDataColumns;
    }

    protected void setDaoFactory(DonkeyDaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    protected void setStorageSettings(StorageSettings storageSettings) {
        this.storageSettings = storageSettings;
    }

    @Override
    public List<ConnectorMessage> call() throws Exception {
        List<ConnectorMessage> messages = new ArrayList<ConnectorMessage>();
        ConnectorMessage message = this.message;

        if (message.getStatus() != Status.RECEIVED && message.getStatus() != Status.PENDING && message.getStatus() != Status.SENT) {
            logger.error("Received an invalid destination message, message status must be either RECEIVED, PENDING or SENT");
            return null;
        }

        int startMetaDataId = enabledMetaDataIds.indexOf(message.getMetaDataId());

        if (startMetaDataId == -1) {
            return null;
        }

        // loop through each metaDataId in the chain, beginning with startMetaDataId
        for (int i = startMetaDataId; i < enabledMetaDataIds.size(); i++) {
            ThreadUtils.checkInterruptedStatus();
            Integer metaDataId = enabledMetaDataIds.get(i);
            Integer nextMetaDataId = (enabledMetaDataIds.size() > (i + 1)) ? enabledMetaDataIds.get(i + 1) : null;
            ConnectorMessage nextMessage = null;
            DestinationConnector destinationConnector = destinationConnectors.get(metaDataId);

            /*
             * TRANSACTION: Process Destination
             * - Insert the custom metadata column data
             * - store the transformed content
             * - store the encoded content
             * - store the sent content (done prior to sending since the sent
             * content would be lost if the message gets queued)
             * - store the raw response content
             * - update the message status to either PENDING or QUEUED
             * - if there is a next destination in the chain, create it's
             * message (done in the next transaction if a response transformer
             * is used)
             */
            DonkeyDao dao = daoFactory.getDao();

            try {
                Status previousStatus = message.getStatus();

                try {
                    switch (message.getStatus()) {
                    // if the message status is RECEIVED, send it to the destination's filter/transformer script
                        case RECEIVED:
                            filterTransformerExecutors.get(metaDataId).processConnectorMessage(message);

                            // Insert errors if necessary
                            if (StringUtils.isNotBlank(message.getErrors())) {
                                dao.updateErrors(message);
                            }

                            // Set the source connector's custom column map
                            metaDataReplacer.setMetaDataMap(message, metaDataColumns);

                            // Store the custom columns
                            if (storageSettings.isStoreCustomMetaData() && !message.getMetaDataMap().isEmpty()) {
                                ThreadUtils.checkInterruptedStatus();
                                dao.insertMetaData(message, metaDataColumns);
                            }

                            // if the message status is TRANSFORMED, send it to the destination connector
                            // when DestinationConnector.process() returns, the message status should be either QUEUED or PENDING
                            if (message.getStatus() == Status.TRANSFORMED) {
                                // the message is queued at this point
                                message.setStatus(Status.QUEUED);

                                if (storageSettings.isStoreTransformed() && message.getTransformed() != null) {
                                    ThreadUtils.checkInterruptedStatus();
                                    dao.insertMessageContent(message.getTransformed());
                                }

                                if (storageSettings.isStoreSourceEncoded() && message.getEncoded() != null) {
                                    ThreadUtils.checkInterruptedStatus();
                                    dao.insertMessageContent(message.getEncoded());
                                }

                                if (storageSettings.isStoreMaps()) {
                                    dao.updateMaps(message);
                                }

                                destinationConnector.process(dao, message, previousStatus);
                            } else {
                                dao.updateStatus(message, previousStatus);
                            }
                            break;

                        case PENDING:
                            destinationConnectors.get(metaDataId).processPendingConnectorMessage(dao, message);
                            // Insert errors if necessary
                            if (StringUtils.isNotBlank(message.getErrors())) {
                                dao.updateErrors(message);
                            }
                            break;
                    }
                } catch (RuntimeException e) { // TODO: remove this catch since we can't determine an error code
                    // if an error occurred in processing the message through the current destination, then update the message status to ERROR and continue processing through the chain
                    logger.error("Error processing destination " + destinationConnectors.get(metaDataId).getDestinationName() + ".", e);
                    dao.rollback();
                    message.setStatus(Status.ERROR);
                    message.setErrors(e.toString());
                    dao.updateStatus(message, previousStatus);
                    // Insert errors if necessary
                    if (StringUtils.isNotBlank(message.getErrors())) {
                        dao.updateErrors(message);
                    }
                }

                // now that we're finished processing the current message, we can create the next message in the chain
                if (nextMetaDataId != null) {
                    nextMessage = new ConnectorMessage(message.getChannelId(), message.getMessageId(), nextMetaDataId, message.getServerId(), Calendar.getInstance(), Status.RECEIVED);
                    nextMessage.setChannelMap((Map<String, Object>) cloner.clone(message.getChannelMap()));
                    nextMessage.setResponseMap((Map<String, Response>) cloner.clone(message.getResponseMap()));
                    nextMessage.setRaw(new MessageContent(message.getChannelId(), message.getMessageId(), nextMetaDataId, ContentType.RAW, message.getRaw().getContent(), message.getRaw().isEncrypted()));

                    ThreadUtils.checkInterruptedStatus();
                    dao.insertConnectorMessage(nextMessage, storageSettings.isStoreMaps());
                }
                
                ThreadUtils.checkInterruptedStatus();
                
                if (message.getStatus() != Status.QUEUED) {
                	dao.commit(storageSettings.isDurable());
                } else {
                	// Block other threads from reading from or modifying the destination queue until both the current commit and queue addition finishes
                	// Otherwise the same message could be sent multiple times.
	                synchronized (destinationConnector.getQueue()) {
		                dao.commit(storageSettings.isDurable());
		
		                if (message.getStatus() == Status.QUEUED) {
		                    destinationConnector.getQueue().put(message);
		                }
	                }
                }

                messages.add(message);
            } catch (RuntimeException e) {
                // An exception caught at this point either occurred when attempting to handle an exception in the above try/catch, or when attempting to create the next destination's message, the thread cannot continue running
                logger.error("Error processing destination " + destinationConnectors.get(metaDataId).getDestinationName() + ".", e);
                throw e;
            } finally {
                dao.close();
            }

            // Set the next message in the loop
            if (nextMetaDataId != null) {
                message = nextMessage;
            }
        }

        return messages;
    }
}
