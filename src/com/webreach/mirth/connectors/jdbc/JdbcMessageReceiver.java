/*
 * $Header: /home/projects/mule/scm/mule/providers/jdbc/src/java/org/mule/providers/jdbc/JdbcMessageReceiver.java,v 1.10 2005/10/23 15:21:21 holger Exp $
 * $Revision: 1.10 $
 * $Date: 2005/10/23 15:21:21 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.connectors.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mule.impl.MuleMessage;
import org.mule.providers.ConnectException;
import org.mule.providers.TransactedPollingMessageReceiver;
import org.mule.transaction.TransactionCoordination;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOMessage;
import org.mule.umo.UMOTransaction;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.Status;
import com.webreach.mirth.server.mule.transformers.JavaScriptPostprocessor;
import com.webreach.mirth.server.util.CompiledScriptCache;
import com.webreach.mirth.server.util.JavaScriptScopeUtil;

/**
 * @author Guillaume Nodet
 * @version $Revision: 1.10 $
 */
public class JdbcMessageReceiver extends TransactedPollingMessageReceiver {
	Logger scriptLogger = Logger.getLogger("jdbc-receiver");
	private JdbcConnector connector;
	private String readStmt;
	private String ackStmt;
	private List readParams;
	private List ackParams;
	private Map jdbcMap;
	private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
	private AlertController alertController = AlertController.getInstance();
	private MonitoringController monitoringController = MonitoringController.getInstance();
	private JavaScriptPostprocessor postprocessor = new JavaScriptPostprocessor();
	public JdbcMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint) throws InitialisationException {
		super(connector, component, endpoint, new Long(((JdbcConnector) connector).getPollingFrequency()));

		if (((JdbcConnector) connector).getPollingType().equals(JdbcConnector.POLLING_TYPE_TIME))
			setTime(((JdbcConnector) connector).getPollingTime());
		else
			setFrequency(((JdbcConnector) connector).getPollingFrequency());

		this.receiveMessagesInTransaction = false;
		this.connector = (JdbcConnector) connector;
		monitoringController.updateStatus(connector, Status.IDLE);
	}

	public JdbcMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint, String readStmt, String ackStmt) throws InitialisationException {
		super(connector, component, endpoint, new Long(((JdbcConnector) connector).getPollingFrequency()));

		this.receiveMessagesInTransaction = false;
		this.connector = (JdbcConnector) connector;

		if (((JdbcConnector) connector).getPollingType().equals(JdbcConnector.POLLING_TYPE_TIME))
			setTime(((JdbcConnector) connector).getPollingTime());
		else
			setFrequency(((JdbcConnector) connector).getPollingFrequency());

		this.readParams = new ArrayList();
		this.readStmt = JdbcUtils.parseStatement(readStmt, this.readParams);
		this.ackParams = new ArrayList();
		this.ackStmt = JdbcUtils.parseStatement(ackStmt, this.ackParams);
		monitoringController.updateStatus(connector, Status.IDLE);
	}

	public void doConnect() throws Exception {
		if (!connector.isUseScript()) {
			Connection con = null;
			try {
				con = this.connector.getConnection(null);
			} catch (Exception e) {
				throw new ConnectException(e, this);
			} finally {
				JdbcUtils.close(con);
			}
		}
	}

	public void doDisconnect() throws ConnectException {
	// noop
	}

	public void processMessage(Object message) throws Exception {
		monitoringController.updateStatus(connector, Status.PROCESSING);
		try {
			if (this.connector.isUseScript() && connector.isUseAck()) {
				// dispatch messages
				UMOMessageAdapter msgAdapter = this.connector.getMessageAdapter(message);
				UMOMessage umoMessage = new MuleMessage(msgAdapter);
				// we should get an MO back (if we're synchronized...)
				umoMessage = routeMessage(umoMessage, endpoint.isSynchronous());

				Context context = Context.enter();
				Scriptable scope = new ImporterTopLevel(context);
				// load variables in JavaScript scope
				JavaScriptScopeUtil.buildScope(scope, connector.getName(), scriptLogger);
				scope.put("dbMap", scope, jdbcMap);
				scope.put("resultMap", scope, message);
				if (umoMessage != null) {
					MessageObject messageObject = (MessageObject) umoMessage.getPayload();
					messageObject = postprocessor.doPostProcess(messageObject);
					scope.put("responseMap", scope, messageObject.getResponseMap());
				}
				// get the script from the cache and execute it
				Script compiledScript = compiledScriptCache.getCompiledScript(this.connector.getAckScriptId());
				if (compiledScript == null) {
					logger.error("Database query update could not be found in cache");
					throw new Exception("Database query update script could not be found in cache");
				} else {
					compiledScript.exec(context, scope);
				}
			} else {
				Connection con = null;
				UMOTransaction tx = TransactionCoordination.getInstance().getTransaction();
				Exception ackException = null;

				try {
					try {
						if (connector.isUseAck() && this.ackStmt != null) {
							con = this.connector.getConnection(null);
							Object[] ackParams = JdbcUtils.getParams(getEndpointURI(), this.ackParams, message);
							int nbRows = new QueryRunner().update(con, this.ackStmt, ackParams);
							if (nbRows != 1) {
								logger.warn("Row count for ack should be 1 and not " + nbRows);
							}
						}
					} catch (Exception ue) {
						logger.error("Error in the ACK sentence of the JDBC connection, but the message is being sent anyway" + ue);
						ackException = ue;
					}
					UMOMessageAdapter msgAdapter = this.connector.getMessageAdapter(message);
					UMOMessage umoMessage = new MuleMessage(msgAdapter);
					UMOMessage retMessage = routeMessage(umoMessage, tx, tx != null || endpoint.isSynchronous());
					postprocessor.doPostProcess(retMessage.getPayload());
					if (ackException != null)
						throw ackException;
				} catch (ConnectException ce) {
					throw new Exception(((ConnectException) ce).getCause());
				} finally {
					if (tx == null) {
						if (con != null)
							JdbcUtils.close(con);
					}
				}
			}
		} catch (Exception e) {
			alertController.sendAlerts(((JdbcConnector) connector).getChannelId(), Constants.ERROR_406, null, e);
			throw e;
		}
	}

	public List getMessages() throws Exception {
		monitoringController.updateStatus(connector, Status.POLLING);
		try {
			if (this.connector.isUseScript()) {
				Context context = Context.enter();
				Scriptable scope = new ImporterTopLevel(context);

				// load variables in JavaScript scope
				JavaScriptScopeUtil.buildScope(scope, connector.getName(), scriptLogger);
				// each time we poll, we want to clear the map.
				// we need to document this
				jdbcMap = new HashMap();
				scope.put("dbMap", scope, jdbcMap);
				// get the script from the cache and execute it
				Script compiledScript = compiledScriptCache.getCompiledScript(this.connector.getScriptId());

				if (compiledScript == null) {
					logger.error("Database script could not be found in cache");
					throw new Exception("Database script could not be found in cache");
				} else {
					Object result = compiledScript.exec(context, scope);

					if (result instanceof NativeJavaObject) {
						Object javaRetVal = ((NativeJavaObject) result).unwrap();

						if (javaRetVal instanceof CachedRowSet) {
							MapListHandler handler = new MapListHandler();
							Object rows = handler.handle((CachedRowSet) javaRetVal);
							return (List) rows;
						} else if (javaRetVal instanceof RowSet) {
							MapListHandler handler = new MapListHandler();
							Object rows = handler.handle((RowSet) javaRetVal);
							return (List) rows;
						} else if (javaRetVal instanceof List) {
							return (List) javaRetVal;
						} else {
							logger.error("Got a result of: " + javaRetVal.toString());
						}
					} else {
						logger.error("Got a result of: " + result.toString());
					}

					Script ackScript = compiledScriptCache.getCompiledScript(this.connector.getAckScriptId());

					return null;
				}
			} else {
				Connection con = null;

				try {
					try {
						con = this.connector.getConnection(null);
					} catch (SQLException e) {
						throw new ConnectException(e, this);
					}

					Object[] readParams = JdbcUtils.getParams(getEndpointURI(), this.readParams, null);
					Object results = new QueryRunner().query(con, this.readStmt, readParams, new MapListHandler());
					return (List) results;
				} finally {
					JdbcUtils.close(con);
				}
			}

		} catch (Exception e) {
			alertController.sendAlerts(((JdbcConnector) connector).getChannelId(), Constants.ERROR_406, null, e);
			throw e;
		}finally{
			monitoringController.updateStatus(connector, Status.IDLE);
		}
	}

}
