/*
 * $Header: /home/projects/mule/scm/mule/providers/jdbc/src/java/org/mule/providers/jdbc/JdbcMessageDispatcher.java,v 1.8 2005/08/29 12:29:08 rossmason Exp $
 * $Revision: 1.8 $
 * $Date: 2005/08/29 12:29:08 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers.jdbc;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.transaction.TransactionCoordination;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.UMOTransaction;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.ConnectorException;
import org.mule.umo.provider.UMOMessageAdapter;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Guillaume Nodet
 * @version $Revision: 1.8 $
 */
public class JdbcMessageDispatcher extends AbstractMessageDispatcher
{

    private JdbcConnector connector;

    public JdbcMessageDispatcher(JdbcConnector connector)
    {
        super(connector);
        this.connector = connector;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.AbstractMessageDispatcher#doDispose()
     */
    public void doDispose()
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.AbstractMessageDispatcher#doDispatch(org.mule.umo.UMOEvent)
     */
    public void doDispatch(UMOEvent event) throws Exception
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Dispatch event: " + event);
        }
        if (event.getMessage().getPayload()== null){
        	return;
        }
        UMOEndpoint endpoint = event.getEndpoint();
        UMOEndpointURI endpointURI = endpoint.getEndpointURI();
        String writeStmt = endpointURI.getAddress();
        String str;
        if ((str = this.connector.getQuery(endpoint, writeStmt)) != null) {
            writeStmt = str;
        }
        if (writeStmt == null) {
            throw new IllegalArgumentException("Write statement should not be null");
        }
        if (!"insert".equalsIgnoreCase(writeStmt.substring(0, 6))
                && !"update".equalsIgnoreCase(writeStmt.substring(0, 6))
                && !"delete".equalsIgnoreCase(writeStmt.substring(0, 6))) {
            throw new IllegalArgumentException("Write statement should be an insert / update / delete sql statement");
        }
        List paramNames = new ArrayList();
        writeStmt = JdbcUtils.parseStatement(writeStmt, paramNames);
        Object[] paramValues = JdbcUtils.getParams(endpointURI, paramNames, event.getTransformedMessage());

        UMOTransaction tx = TransactionCoordination.getInstance().getTransaction();
        Connection con = null;
        try {
            con = this.connector.getConnection();

            int nbRows = new QueryRunner().update(con, writeStmt, paramValues);
            if (nbRows != 1) {
                logger.warn("Row count for write should be 1 and not " + nbRows);
            }
            if (tx == null) {
                JdbcUtils.commitAndClose(con);
            }
            logger.debug("Event dispatched succesfuly");
        } catch (Exception e) {
            logger.debug("Error dispatching event: " + e.getMessage(), e);
            if (tx == null) {
                JdbcUtils.rollbackAndClose(con);
            }
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.AbstractMessageDispatcher#doSend(org.mule.umo.UMOEvent)
     */
    public UMOMessage doSend(UMOEvent event) throws Exception
    {
        doDispatch(event);
        return event.getMessage();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOMessageDispatcher#receive(org.mule.umo.endpoint.UMOEndpointURI,
     *      long)
     */
    public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Trying to receive a message with a timeout of " + timeout);
        }

        String[] stmts = this.connector.getReadAndAckStatements(endpointUri, null);
        String readStmt = stmts[0];
        String ackStmt = stmts[1];
        List readParams = new ArrayList();
        List ackParams = new ArrayList();
        readStmt = JdbcUtils.parseStatement(readStmt, readParams);
        ackStmt = JdbcUtils.parseStatement(ackStmt, ackParams);

        Connection con = null;
        long t0 = System.currentTimeMillis();
        try {
            con = this.connector.getConnection();
            if (timeout < 0) {
                timeout = Long.MAX_VALUE;
            }
            Object result = null;
            do {
                result = new QueryRunner().query(con,
                                                 readStmt,
                                                 JdbcUtils.getParams(endpointUri, readParams, null),
                                                 new MapHandler());
                if (result != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Received: " + result);
                    }
                    break;
                }
                long sleep = Math.min(this.connector.getPollingFrequency(), timeout - (System.currentTimeMillis() - t0));
                if (sleep > 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("No results, sleeping for " + sleep);
                    }
                    Thread.sleep(sleep);
                } else {
                    logger.debug("Timeout");
                    return null;
                }
            } while (true);
            if (result != null && ackStmt != null) {
                int nbRows = new QueryRunner().update(con, ackStmt, JdbcUtils.getParams(endpointUri, ackParams, result));
                if (nbRows != 1) {
                    logger.warn("Row count for ack should be 1 and not " + nbRows);
                }
            }
            UMOMessageAdapter msgAdapter = this.connector.getMessageAdapter(result);
            UMOMessage message = new MuleMessage(msgAdapter);
            JdbcUtils.commitAndClose(con);
            return message;
        } catch (Exception e) {
            JdbcUtils.rollbackAndClose(con);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOMessageDispatcher#getDelegateSession()
     */
    public Object getDelegateSession() throws UMOException
    {
        try {
            return connector.getConnection();
        } catch (Exception e) {
            throw new ConnectorException(new Message(Messages.FAILED_TO_CREATE_X, "Jdbc Connection"), connector, e);
        }
    }

}
