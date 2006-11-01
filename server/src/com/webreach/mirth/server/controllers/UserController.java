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


package com.webreach.mirth.server.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.truemesh.squiggle.MatchCriteria;
import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;
import com.webreach.mirth.model.User;
import com.webreach.mirth.server.util.DatabaseConnection;
import com.webreach.mirth.server.util.DatabaseConnectionFactory;
import com.webreach.mirth.server.util.DatabaseUtil;

public class UserController {
	private Logger logger = Logger.getLogger(this.getClass());

	/**
	 * Returns a List containing the User with the specified <code>userId</code>.
	 * If the <code>userId</code> is <code>null</code>, all users are
	 * returned.
	 * 
	 * @param userId
	 *            the ID of the User to be returned.
	 * @return a List containing the User with the specified <code>userId</code>,
	 *         a List containing all users otherwise.
	 * @throws ControllerException
	 */
	public List<User> getUsers(Integer userId) throws ControllerException {
		logger.debug("retrieving user list: user id = " + userId);

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			Table users = new Table("users");
			SelectQuery select = new SelectQuery(users);

			select.addColumn(users, "id");
			select.addColumn(users, "username");
			select.addColumn(users, "password");

			if (userId != null) {
				select.addCriteria(new MatchCriteria(users, "id", MatchCriteria.EQUALS, userId.toString()));
			}

			result = dbConnection.executeQuery(select.toString());
			return getUserList(result);
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			DatabaseUtil.close(dbConnection);
		}
	}

	/**
	 * Converts a ResultSet to a List of User objects.
	 * 
	 * @param result
	 *            the ResultSet to be converted.
	 * @return a List of User objects.
	 * @throws SQLException
	 */
	private List<User> getUserList(ResultSet result) throws SQLException {
		ArrayList<User> users = new ArrayList<User>();

		while (result.next()) {
			User user = new User();
			user.setId(result.getInt("id"));
			user.setUsername(result.getString("username"));
			user.setPassword(result.getString("password"));
			users.add(user);
		}

		return users;
	}

	/**
	 * If a User with the specified User's ID already exists, the User will be
	 * updated. Otherwise, the User will be added.
	 * 
	 * @param user
	 *            User to be updated.
	 * @throws ControllerException
	 */
	public void updateUser(User user) throws ControllerException {
		logger.debug("updating user: " + user.toString());

		DatabaseConnection dbConnection = null;
		
		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			String statement = null;
			ArrayList<Object> parameters = new ArrayList<Object>();

			if (getUsers(user.getId()).isEmpty()) {
				statement = "insert into users (id, username, password) values (?, ?, ?)";
				parameters.add(user.getId());
				parameters.add(user.getUsername());
				parameters.add(user.getPassword());
			} else {
				statement = "update users set username = ?, password = ? where id = ?";
				parameters.add(user.getUsername());
				parameters.add(user.getPassword());
				parameters.add(user.getId());
			}

			dbConnection.executeUpdate(statement, parameters);
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(dbConnection);
		}
	}

	/**
	 * Removes the user with the specified ID.
	 * 
	 * @param userId
	 *            ID of the User to be removed.
	 * @throws ControllerException
	 */
	public void removeUser(int userId) throws ControllerException {
		logger.debug("removing user: " + userId);

		DatabaseConnection dbConnection = null;
		
		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			
			String statement = "delete from users where id = ?";
			ArrayList<Object> parameters = new ArrayList<Object>();
			parameters.add(userId);
			
			dbConnection.executeUpdate(statement, parameters);
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(dbConnection);
		}
	}

	/**
	 * Returns a User ID given a valid username and password, -1 otherwise.
	 * 
	 * @param username
	 *            the username of the User to be authenticated.
	 * @param password
	 *            the password of the User to be authenticated.
	 * @return a User ID given a valid username and password, -1 otherwise.
	 * @throws ControllerException
	 */
	public int authenticateUser(String username, String password) throws ControllerException {
		logger.debug("authenticating user: " + username);

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			
			Table users = new Table("users");
			SelectQuery select = new SelectQuery(users);
			select.addColumn(users, "id");
			select.addCriteria(new MatchCriteria(users, "username", MatchCriteria.EQUALS, username));
			select.addCriteria(new MatchCriteria(users, "password", MatchCriteria.EQUALS, password));
			
			result = dbConnection.executeQuery(select.toString());

			while (result.next()) {
				return result.getInt("id");
			}

			return -1;
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			DatabaseUtil.close(dbConnection);
		}
	}

}
