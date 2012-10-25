/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tests;

import java.util.ArrayList;
import java.util.List;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.DataType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.PauseException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.DestinationChain;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.channel.components.FilterTransformerExecutor;
import com.mirth.connect.donkey.server.data.DonkeyDao;

public class DummyChannel extends Channel {
    private List<Long> queuedMessageIds = new ArrayList<Long>();
    private List<Long> processedMessageIds = new ArrayList<Long>();

    public DummyChannel(String channelId, String serverId, SourceConnector sourceConnector, DestinationConnector destinationConnector) {
        super();

        setEnabled(true);
        setChannelId(channelId);
        setServerId(serverId);

        if (sourceConnector != null) {
            sourceConnector.setChannel(this);
            setSourceConnector(sourceConnector);
        }

        if (destinationConnector != null) {
            destinationConnector.setChannelId(channelId);
            destinationConnector.setResponseTransformer(new TestResponseTransformer());
            DestinationChain chain = new DestinationChain();
            chain.addDestination(1, new FilterTransformerExecutor(new DataType("XML", new TestSerializer()), new DataType("XML", new TestSerializer())), destinationConnector);
            getDestinationChains().add(chain);
        }
    }

    @Override
    public void start() throws StartException {
        SourceConnector sourceConnector = getSourceConnector();

        if (sourceConnector != null) {
            sourceConnector.start();
        }
    }

    @Override
    public void stop() throws StopException {
        SourceConnector sourceConnector = getSourceConnector();

        if (sourceConnector != null) {
            sourceConnector.stop();
        }
    }
    
    @Override
    public void halt() throws StopException {
        SourceConnector sourceConnector = getSourceConnector();

        if (sourceConnector != null) {
            sourceConnector.halt();
        }
    }

    @Override
    public void pause() throws PauseException {
        try {
            stop();
        } catch (StopException e) {
            throw new PauseException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public int getDestinationCount() {
        return 1;
    }

    @Override
    public void queue(ConnectorMessage sourceMessage) {
        queuedMessageIds.add(sourceMessage.getMessageId());
    }

    @Override
    public Message process(ConnectorMessage sourceMessage, boolean waitForDestinations) throws InterruptedException {
        processedMessageIds.add(sourceMessage.getMessageId());

        sourceMessage.setTransformed(new MessageContent(sourceMessage.getChannelId(), sourceMessage.getMessageId(), 0, ContentType.TRANSFORMED, sourceMessage.getRaw().getContent(), sourceMessage.getRaw().isEncrypted()));
        sourceMessage.setEncoded(new MessageContent(sourceMessage.getChannelId(), sourceMessage.getMessageId(), 0, ContentType.ENCODED, sourceMessage.getRaw().getContent(), sourceMessage.getRaw().isEncrypted()));

        ConnectorMessage destinationMessage = new ConnectorMessage();
        destinationMessage.setMessageId(sourceMessage.getMessageId());
        destinationMessage.setServerId(sourceMessage.getServerId());
        destinationMessage.setStatus(Status.TRANSFORMED);
        destinationMessage.setMetaDataId(1);
        destinationMessage.setRaw(sourceMessage.getRaw());
        destinationMessage.setTransformed(new MessageContent(destinationMessage.getChannelId(), destinationMessage.getMessageId(), destinationMessage.getMetaDataId(), ContentType.TRANSFORMED, destinationMessage.getRaw().getContent(), destinationMessage.getRaw().isEncrypted()));
        destinationMessage.setEncoded(new MessageContent(destinationMessage.getChannelId(), destinationMessage.getMessageId(), destinationMessage.getMetaDataId(), ContentType.ENCODED, destinationMessage.getRaw().getContent(), destinationMessage.getRaw().isEncrypted()));

        Message message = new Message();
        message.setMessageId(sourceMessage.getMessageId());
        message.setChannelId(getChannelId());
        message.setServerId(getServerId());
        message.getConnectorMessages().put(0, sourceMessage);
        message.getConnectorMessages().put(1, destinationMessage);

        sourceMessage.getResponseMap().put(Constants.RESPONSE_POST_PROCESSOR, new Response(Status.SENT, ""));

        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();
        
        for (DestinationChain chain : getDestinationChains()) {
            for (Integer metaDataId : chain.getMetaDataIds()) {
                chain.getDestinationConnectors().get(metaDataId).process(dao, destinationMessage, destinationMessage.getStatus());
            }
        }

        dao.commit();
        dao.close();
        
        return message;
    }

    public List<Long> getQueuedMessageIds() {
        return queuedMessageIds;
    }

    public List<Long> getProcessedMessageIds() {
        return processedMessageIds;
    }
}
