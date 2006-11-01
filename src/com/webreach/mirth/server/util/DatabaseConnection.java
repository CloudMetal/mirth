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


package com.webreach.mirth.server.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Properties;

import javax.sql.rowset.CachedRowSet;

import org.apache.log4j.Logger;

import com.sun.rowset.CachedRowSetImpl;

/**
 * A DatabaseConnection provides a connection to the internal Mirth database.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 * 
 */
public class DatabaseConnection {
	private Logger logger = Logger.getLogger(this.getClass());
	private Connection connection;
	private String address;

	/**
	 * Initiliazes a database connection.
	 * 
	 * @throws SQLException
	 */
	public DatabaseConnection(String address, Properties info) throws SQLException {
		logger.debug("creating new database connection: address=" + address + ", " + info);
		this.address = address;
		connection = DriverManager.getConnection(address, info);
	}

	/**
	 * Returns the database address.
	 * 
	 * @return
	 */
	public String getAddress() {
		return this.address;
	}

	/**
	 * Executes a query on the database and returns a ResultSet.
	 * 
	 * @param expression
	 *            the query expression to be executed.
	 * @return the result of the query.
	 * @throws SQLException
	 */
	public synchronized ResultSet executeQuery(String expression) throws SQLException {
		Statement statement = null;

		try {
			statement = connection.createStatement();
			logger.debug("executing query:\n" + expression);
			return statement.executeQuery(expression);
		} catch (SQLException e) {
			throw e;
		} finally {
			DatabaseUtil.close(statement);
		}
	}

	/**
	 * Executes a query on the database and returns a CachedRowSet.
	 * 
	 * @param expression
	 *            the query expression to be executed.
	 * @return the result of the query.
	 * @throws SQLException
	 */
	public synchronized CachedRowSet executeCachedQuery(String expression) throws SQLException {
		Statement statement = null;

		try {
			statement = connection.createStatement();
			logger.debug("executing query:\n" + expression);
			ResultSet result = statement.executeQuery(expression);
			CachedRowSetImpl crs = new CachedRowSetImpl();
			crs.populate(result);
			DatabaseUtil.close(result);
			return crs;
		} catch (SQLException e) {
			throw e;
		} finally {
			DatabaseUtil.close(statement);
		}
	}

	/**
	 * Executes an update on the database and returns the row count.
	 * 
	 * @param expression
	 *            the update query to be executed.
	 * @return a count of the number of updated rows.
	 * @throws SQLException
	 */
	public synchronized int executeUpdate(String expression) throws SQLException {
		Statement statement = null;

		try {
			statement = connection.createStatement();
			logger.debug("executing update:\n" + expression);
			return statement.executeUpdate(expression);
		} catch (SQLException e) {
			throw e;
		} finally {
			DatabaseUtil.close(statement);
		}
	}

	/**
	 * Executes a prepared statement on the database and returns the row count.
	 * 
	 * @param expression
	 *            the prepared statement to be executed
	 * @param parameters
	 *            the parameteres for the prepared statement
	 * @return a count of the number of updated rows.
	 * @throws SQLException
	 */
	public synchronized int executeUpdate(String expression, ArrayList parameters) throws SQLException {
		PreparedStatement statement = null;

		try {
			statement = connection.prepareStatement(expression);
			logger.debug("executing prepared statement:\n" + expression);

			ListIterator iterator = parameters.listIterator();

			while (iterator.hasNext()) {
				int index = iterator.nextIndex() + 1;
				Object value = iterator.next();
				logger.debug("adding parameter: index=" + index + ", value=" + value);
				statement.setObject(index, value);
			}

			return statement.executeUpdate();
		} catch (SQLException e) {
			throw e;
		} finally {
			DatabaseUtil.close(statement);
		}
	}

	/**
	 * Closes the database connection.
	 * 
	 */
	public void close() {
		try {
			if ((connection != null) && (!connection.isClosed())) {
				logger.debug("closing database connection");
				connection.close();
			} else {
				logger.warn("connection is null or already closed");
			}
		} catch (SQLException e) {
			logger.warn(e);
		}
	}

	/**
	 * Sets this connection's auto-commit mode to the given state.
	 * 
	 * @param autoCommit
	 * @throws SQLException
	 */
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		connection.setAutoCommit(autoCommit);
	}

	/**
	 * Undoes all changes made in the current transaction and releases any
	 * database locks currently held by this Connection object.
	 * 
	 * @throws SQLException
	 */
	public void rollback() throws SQLException {
		connection.rollback();
	}

	/**
	 * Makes all changes made since the previous commit/rollback permanent and
	 * releases any database locks currently held by this DatabaseConnection
	 * object.
	 * 
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		connection.commit();
	}
}
