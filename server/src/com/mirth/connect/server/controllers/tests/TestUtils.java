/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers.tests;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.mirth.connect.connectors.tests.TestAutoResponder;
import com.mirth.connect.connectors.tests.TestDestinationConnector;
import com.mirth.connect.connectors.tests.TestResponseTransformer;
import com.mirth.connect.connectors.tests.TestSerializer;
import com.mirth.connect.connectors.tests.TestSourceConnector;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.ChannelLock;
import com.mirth.connect.donkey.server.channel.DestinationChain;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.FilterTransformerExecutor;
import com.mirth.connect.donkey.server.channel.MetaDataReplacer;
import com.mirth.connect.donkey.server.channel.ResponseSelector;
import com.mirth.connect.donkey.server.channel.ResponseTransformerExecutor;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.controllers.MessageController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.buffered.BufferedDaoFactory;
import com.mirth.connect.donkey.server.message.DataType;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueue;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueueDataSource;
import com.mirth.connect.server.util.ResourceUtil;

public class TestUtils {
    final public static String TEST_HL7_MESSAGE = "MSH|^~\\&|LABNET|Acme Labs|||20090601105700||ORU^R01|HMCDOOGAL-0088|D|2.2\nPID|1|8890088|8890088^^^72777||McDoogal^Hattie^||19350118|F||2106-3|100 Beach Drive^Apt. 5^Mission Viejo^CA^92691^US^H||(949) 555-0025|||||8890088^^^72|604422825\nPV1|1|R|C3E^C315^B||||2^HIBBARD^JULIUS^|5^ZIMMERMAN^JOE^|9^ZOIDBERG^JOHN^|CAR||||4|||2301^OBRIEN, KEVIN C|I|1783332658^1^1||||||||||||||||||||DISNEY CLINIC||N|||20090514205600\nORC|RE|928272608|056696716^LA||CM||||20090601105600||||  C3E|||^RESULT PERFORMED\nOBR|1|928272608|056696716^LA|1001520^K|||20090601101300|||MLH25|||HEMOLYZED/VP REDRAW|20090601102400||2301^OBRIEN, KEVIN C||||01123085310001100100152023509915823509915800000000101|0000915200932|20090601105600||LAB|F||^^^20090601084100^^ST~^^^^^ST\nOBX|1|NM|1001520^K||5.3|MMOL/L|3.5-5.5||||F|||20090601105600|IIM|IIM";
    final public static String CHANNEL_ID = "newtestchannel";
    final public static String SERVER_ID = "testserver";
    
    private static Logger logger = Logger.getLogger(TestUtils.class);

    public static void runChannelTest(Channel channel, final String testMessage, final int testSize) throws Exception {
        channel.deploy();
        channel.start();
        
        SourceConnector sourceConnector = channel.getSourceConnector();
        RawMessage rawMessage = new RawMessage(testMessage);
        
        for (int i = 0; i < testSize; i++) {
            DispatchResult dispatchResult = null;
            
            try {
                dispatchResult = sourceConnector.dispatchRawMessage(rawMessage);
                
                if (dispatchResult.getChannelException() != null) {
                    throw dispatchResult.getChannelException();
                }
            } finally {
                sourceConnector.finishDispatch(dispatchResult);
            }
        }
        
        channel.stop();
        channel.undeploy();
    }

    public static Channel createChannel(String channelId, String serverId, SourceConnector sourceConnector, DestinationConnector destinationConnector) {
        Channel channel = new Channel();
        channel.lock(ChannelLock.DEBUG);
        channel.setChannelId(channelId);
        channel.setServerId(serverId);
        channel.setEnabled(true);
        channel.setSourceConnector(sourceConnector);
        channel.setPreProcessor(new TestPreProcessor());
        channel.setPostProcessor(new TestPostProcessor());

        FilterTransformerExecutor filterTransformer = new FilterTransformerExecutor(new DataType("XML", new TestSerializer(), null, new TestAutoResponder()), new DataType("XML", new TestSerializer(), null, new TestAutoResponder()));
        filterTransformer.setFilterTransformer(new TestFilterTransformer());
        channel.setSourceFilterTransformer(filterTransformer);

        sourceConnector.setChannel(channel);

        DestinationChain chain = new DestinationChain();
        filterTransformer = new FilterTransformerExecutor(new DataType("XML", new TestSerializer(), null, new TestAutoResponder()), new DataType("XML", new TestSerializer(), null, new TestAutoResponder()));
        filterTransformer.setFilterTransformer(new TestFilterTransformer());
        chain.addDestination(1, filterTransformer, destinationConnector);
        channel.getDestinationChains().add(chain);
        destinationConnector.setChannelId(channelId);
        
        ResponseTransformerExecutor responseTransformerExecutor = new ResponseTransformerExecutor(new DataType("XML", new TestSerializer(), null, new TestAutoResponder()), new DataType("XML", new TestSerializer(), null, new TestAutoResponder()));
        responseTransformerExecutor.setResponseTransformer(new TestResponseTransformer());
        destinationConnector.setResponseTransformerExecutor(responseTransformerExecutor);

        ConnectorMessageQueue queue = new ConnectorMessageQueue();
        queue.setDataSource(new ConnectorMessageQueueDataSource(channelId, 1, Status.QUEUED, false, Donkey.getInstance().getDaoFactory()));
        destinationConnector.setQueue(queue);

        return channel;
    }

    public static Channel createChannel(String channelId, String serverId, boolean respondAfterProcessing, int numChains, int destinationsPerChain) {
        // create channel
        Channel channel = new Channel();
        channel.lock(ChannelLock.DEBUG);
        channel.setChannelId(channelId);
        channel.setServerId(serverId);
        channel.setEnabled(true);
        channel.setDaoFactory(new BufferedDaoFactory(Donkey.getInstance().getDaoFactory()));
        channel.setPreProcessor(new TestPreProcessor());
        channel.setPostProcessor(new TestPostProcessor());

        FilterTransformerExecutor filterTransformer = new FilterTransformerExecutor(new DataType("XML", new TestSerializer(), null, new TestAutoResponder()), new DataType("XML", new TestSerializer(), null, new TestAutoResponder()));
        filterTransformer.setFilterTransformer(new TestFilterTransformer());
        channel.setSourceFilterTransformer(filterTransformer);

        // create source connector
        TestSourceConnector sourceConnector = new TestSourceConnector();
        sourceConnector.setChannel(channel);
        sourceConnector.setRespondAfterProcessing(respondAfterProcessing);
        sourceConnector.setMetaDataReplacer(new MetaDataReplacer());
        
        channel.setSourceConnector(sourceConnector);
        channel.setResponseSelector(new ResponseSelector(sourceConnector.getInboundDataType()));
        channel.getResponseSelector().setRespondFromName("destination1");
        
        int metaDataId = 1;

        // create destination chains
        for (int i = 0; i < numChains; i++) {
            DestinationChain chain = new DestinationChain();
            chain.setChannelId(channelId);
            chain.setMetaDataReplacer(sourceConnector.getMetaDataReplacer());
            
            channel.getDestinationChains().add(chain);

            for (int j = 0; j < destinationsPerChain; j++) {
                ConnectorMessageQueue queue = new ConnectorMessageQueue();
                queue.setDataSource(new ConnectorMessageQueueDataSource(channelId, metaDataId, Status.QUEUED, false, Donkey.getInstance().getDaoFactory()));

                TestDestinationConnector testDestinationConnector = new TestDestinationConnector();
                testDestinationConnector.setChannelId(channelId);
                testDestinationConnector.setDestinationName("destination" + metaDataId);
                testDestinationConnector.setEnabled(true);
                testDestinationConnector.setQueue(queue);
                
                ResponseTransformerExecutor responseTransformerExecutor = new ResponseTransformerExecutor(new DataType("XML", new TestSerializer(), null, new TestAutoResponder()), new DataType("XML", new TestSerializer(), null, new TestAutoResponder()));
                responseTransformerExecutor.setResponseTransformer(new TestResponseTransformer());
                testDestinationConnector.setResponseTransformerExecutor(responseTransformerExecutor);

                filterTransformer = new FilterTransformerExecutor(new DataType("XML", new TestSerializer(), null, new TestAutoResponder()), new DataType("XML", new TestSerializer(), null, new TestAutoResponder()));
                filterTransformer.setFilterTransformer(new TestFilterTransformer());
                chain.addDestination(metaDataId++, filterTransformer, testDestinationConnector);
            }
        }

        return channel;
    }

    public static Properties getSqlProperties() {
        PropertiesConfiguration mirthProperties = new PropertiesConfiguration();

        try {
            InputStream is = ResourceUtil.getResourceStream(SqlSession.class, "mirth.properties");
            mirthProperties.setDelimiterParsingDisabled(true);
            mirthProperties.load(is);
            IOUtils.closeQuietly(is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Properties donkeyProperties = new Properties();
        donkeyProperties.setProperty("database.driver", mirthProperties.getString("database.driver"));
        donkeyProperties.setProperty("database.url", mirthProperties.getString("database.url"));
        donkeyProperties.setProperty("database.username", mirthProperties.getString("database.username"));

        if (mirthProperties.containsKey("database.password")) {
            donkeyProperties.setProperty("database.password", mirthProperties.getString("database.password"));
        }

        return donkeyProperties;
    }

    public static Connection getConnection() throws Exception {
        Properties configuration = Donkey.getInstance().getConfiguration().getDatabaseProperties();
        String driver = configuration.getProperty("database.driver");

        if (driver != null) {
            Class.forName(driver);
        }

        Connection connection = DriverManager.getConnection(configuration.getProperty("database.url"), configuration.getProperty("database.username"), configuration.getProperty("database.password"));
        connection.setAutoCommit(false);
        return connection;
    }

    public static boolean channelExists(String channelId) throws Exception {
        boolean exists = false;
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM channels WHERE channel_id = ?");
        statement.setString(1, channelId);
        ResultSet result = statement.executeQuery();

        if (result.next()) {
            exists = true;
        }

        result.close();
        connection.close();
        return exists;
    }

    public static String getPerformanceText(String testName, int testSize, long milliseconds) {
        double seconds = ((double) milliseconds) / 1000.0;
        return StringUtils.rightPad(testName, 50) + ((int) (testSize / seconds)) + " messages/second";
    }

    public static Message createAndStoreNewMessage(RawMessage rawMessage, String channelId, String serverId, DonkeyDao dao) {
        Message message = MessageController.getInstance().createNewMessage(channelId, serverId);
        ConnectorMessage sourceMessage = new ConnectorMessage(channelId, message.getMessageId(), 0, serverId, message.getReceivedDate(), Status.RECEIVED);
        sourceMessage.setRaw(new MessageContent(channelId, message.getMessageId(), 0, ContentType.RAW, rawMessage.getRawData(), null, false));

        if (rawMessage.getChannelMap() != null) {
            sourceMessage.setChannelMap(rawMessage.getChannelMap());
        }

        message.getConnectorMessages().put(0, sourceMessage);

        dao.insertMessage(message);
        dao.insertConnectorMessage(sourceMessage, true);
        dao.insertMessageContent(sourceMessage.getRaw());
        return message;
    }
    
    @SuppressWarnings("unchecked")
    public static List<Long> getMessageIds(String channelId) throws Exception {
        return (List<Long>) selectColumn("SELECT id FROM d_m" + ChannelController.getInstance().getLocalChannelId(channelId));
    }

    public static int getNumMessages(String channelId) throws Exception {
        return getNumMessages(channelId, false);
    }

    public static int getNumMessages(String channelId, boolean onlyCountMessagesWithContent) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);

            StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM d_m" + localChannelId + " m");

            if (onlyCountMessagesWithContent) {
                query.append(" WHERE EXISTS (SELECT 1 FROM d_mc" + localChannelId + " WHERE message_id = m.id)");
            }

            connection = getConnection();
            statement = connection.prepareStatement(query.toString());
            result = statement.executeQuery();
            result.next();
            return result.getInt(1);
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
    }
    
    public static void close(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void close(Statement statement) {
        try {
            DbUtils.close(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void close(PreparedStatement preparedStatement) {
        try {
            DbUtils.close(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void close(ResultSet resultSet) {
        try {
            DbUtils.close(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void close(DonkeyDao dao) {
        if (dao != null && !dao.isClosed()) {
            dao.close();
        }
    }

    public static void deleteAllMessages(String channelId) throws Exception {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            dao.deleteAllMessages(channelId);
            dao.commit();
        } finally {
            dao.close();
        }
        
        fixMessageIdSequence(channelId);
    }

    public static void createTestMessages(String channelId, Message templateMessage, int count) throws Exception {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            for (int i = 0; i < count; i++) {
                insertCompleteMessage(templateMessage, dao);
            }

            dao.commit();
        } finally {
            dao.close();
        }
    }

    public static void createTestMessagesFast(String channelId, Message templateMessage, int power) throws Exception {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        deleteAllMessages(channelId);
        createTestMessages(channelId, templateMessage, 1);

        Connection connection = null;
        PreparedStatement messageStatement = null;
        PreparedStatement metaDataStatement = null;
        PreparedStatement contentStatement = null;
        long idOffset = templateMessage.getMessageId();
        
        logger.debug("Replicating messages in channel \"" + channelId + "\"");

        try {
            connection = getConnection();
            messageStatement = connection.prepareStatement("INSERT INTO d_m" + localChannelId + " (id, server_id, received_date, processed) SELECT id + ?, server_id, received_date, processed FROM d_m" + localChannelId);
            metaDataStatement = connection.prepareStatement("INSERT INTO d_mm" + localChannelId + " (id, message_id, chain_id, received_date, status, order_id) SELECT id, message_id + ?, chain_id, received_date, status, order_id FROM d_mm" + localChannelId);
            contentStatement = connection.prepareStatement("INSERT INTO d_mc" + localChannelId + " (metadata_id, message_id, content_type, content, is_encrypted, data_type) SELECT metadata_id, message_id + ?, content_type, content, is_encrypted, data_type FROM d_mc" + localChannelId);

            for (int i = 0; i < power; i++) {
                messageStatement.setLong(1, idOffset);
                metaDataStatement.setLong(1, idOffset);
                contentStatement.setLong(1, idOffset);

                messageStatement.executeUpdate();
                metaDataStatement.executeUpdate();
                contentStatement.executeUpdate();

                idOffset *= 2;

                connection.commit();
                logger.debug("# of messages in channel \"" + channelId + "\" is now " + getNumMessages(channelId));
            }
        } finally {
            close(messageStatement);
            close(metaDataStatement);
            close(contentStatement);
            close(connection);
        }
        
        fixMessageIdSequence(channelId);
        logger.debug("Finished replicating messages in channel \"" + channelId + "\"");
    }

    public static void fixMessageIdSequence(String channelId) throws Exception {
        Connection connection = null;
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        String database = (String) Donkey.getInstance().getConfiguration().getDatabaseProperties().get("database");
        Long maxId = null;
        
        if (database.equals("derby")) {
            Statement statement = null;
            ResultSet result = null;
            
            try {
                connection = getConnection();
                statement = connection.createStatement();
                result = statement.executeQuery("SELECT MAX(id) FROM d_m" + localChannelId);
                result.next();
                maxId = result.getLong(1) + 1;
                close(result);
                statement.execute("DROP SEQUENCE d_msq" + localChannelId + " RESTRICT");
                statement.execute("CREATE SEQUENCE d_msq" + localChannelId + " START WITH " + maxId + " INCREMENT BY 1 NO CYCLE");
                connection.commit();
            } finally {
                close(result);
                close(statement);
                close(connection);
            }
        } else {
            // TODO
        }
        
        logger.debug("Message ID sequence updated to: " + maxId);
    }

    private static void insertCompleteMessage(Message message, DonkeyDao dao) {
        dao.insertMessage(message);

        if (message.isProcessed()) {
            dao.markAsProcessed(message.getChannelId(), message.getMessageId());
        }

        for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
            dao.insertConnectorMessage(connectorMessage, true);

            for (ContentType contentType : ContentType.getMessageTypes()) {
                MessageContent messageContent = connectorMessage.getMessageContent(contentType);

                if (messageContent != null) {
                    dao.insertMessageContent(messageContent);
                }
            }
        }
    }

    public static double getPerSecondRate(long size, long millis, Integer precision) {
        double rate = ((double) size) / ((double) millis / 1000d);

        if (precision != null) {
            rate = Precision.round(rate, 2);
        }

        return rate;
    }
    
    public static List<?> selectColumn(String query) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            List<Object> col = new ArrayList<Object>();

            while (resultSet.next()) {
                col.add(resultSet.getObject(1));
            }

            return col;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }
    }
}
