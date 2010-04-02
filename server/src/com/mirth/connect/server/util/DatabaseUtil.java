/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImpl;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;

public class DatabaseUtil {

	private static Logger logger = Logger.getLogger(DatabaseUtil.class);

	/**
	 * Closes the specified ResultSet.
	 * 
	 * @param result
	 *            the ResultSet to be closed.
	 * @throws RuntimeException
	 */
	public static void close(ResultSet result) throws RuntimeException {
		try {
			if (result != null) {
				result.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Closes the specified Statement.
	 * 
	 * @param statement
	 *            the Statement to be closed.
	 * @throws RuntimeException
	 */
	public static void close(Statement statement) throws RuntimeException {
		try {
			if (statement != null) {
				statement.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void close(DatabaseConnection connection) throws RuntimeException {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void close(Connection connection) throws RuntimeException {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void executeScript(File script, boolean ignoreErrors) throws Exception {
		SqlMapClient sqlMap = SqlConfig.getSqlMapClient();

		Connection conn = null;
		ResultSet resultSet = null;
		Statement statement = null;

		try {
			conn = sqlMap.getDataSource().getConnection();
			statement = conn.createStatement();

			Scanner s = new Scanner(script);
			while (s.hasNextLine()) {
				StringBuilder sb = new StringBuilder();
				boolean blankLine = false;

				while (s.hasNextLine() && !blankLine) {
					String temp = s.nextLine();

					if (temp.trim().length() > 0)
						sb.append(temp + " ");
					else
						blankLine = true;
				}

				String statementString = sb.toString().trim();
				if (statementString.length() > 0) {
					try {
						statement.execute(statementString);
						conn.commit();
					} catch (SQLException se) {
						if (!ignoreErrors) {
							throw se;
						} else {
							logger.error("Error was encountered and ignored while executing statement: " + statementString, se);
							conn.rollback();
						}
					}
				}
			}

		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			close(statement);
			close(resultSet);
			close(conn);
		}
	}

	public static void executeScript(List<String> script, boolean ignoreErrors) throws Exception {
		SqlMapClient sqlMap = SqlConfig.getSqlMapClient();

		Connection conn = null;
		ResultSet resultSet = null;
		Statement statement = null;

		try {
			conn = sqlMap.getDataSource().getConnection();
			statement = conn.createStatement();

			for (String statementString : script) {
				statementString = statementString.trim();
				if (statementString.length() > 0) {
					try {
						statement.execute(statementString);
						conn.commit();
					} catch (SQLException se) {
						if (!ignoreErrors) {
							throw se;
						} else {
							logger.error("Error was encountered and ignored while executing statement: " + statementString, se);
							conn.rollback();
						}
					}
				}
			}

		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			close(statement);
			close(resultSet);
			close(conn);
		}
	}
	
    public static boolean statementExists(String statement) {
        try {
            SqlMapExecutorDelegate delegate = ((SqlMapClientImpl) SqlConfig.getSqlMapClient()).getDelegate();
            delegate.getMappedStatement(statement);
        } catch (SqlMapException sme) {
            // The statement does not exist
            return false;
        }

        return true;
    }
	
}
