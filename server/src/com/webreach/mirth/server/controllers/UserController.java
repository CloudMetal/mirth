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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.webreach.mirth.model.Credentials;
import com.webreach.mirth.model.User;
import com.webreach.mirth.server.util.SqlConfig;
import com.webreach.mirth.util.EncryptionException;
import com.webreach.mirth.util.FIPSEncrypter;

public class UserController {
    private Logger logger = Logger.getLogger(this.getClass());
    private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();
    private FIPSEncrypter encrypter = FIPSEncrypter.getInstance();

    private static UserController instance = null;

    private UserController() {

    }

    public static UserController getInstance() {
        synchronized (UserController.class) {
            if (instance == null) {
                instance = new UserController();
            }

            return instance;
        }
    }

    public void initialize() {
        try {
            sqlMap.update("resetUserStatus");
        } catch (SQLException e) {
            logger.error("Could not reset user status.");
        }
    }

    public List<User> getUser(User user) throws ControllerException {
        logger.debug("getting user: " + user);

        try {
            return sqlMap.queryForList("getUser", user);
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public void updateUser(User user, String plainTextPassword) throws ControllerException {
        try {
            if (user.getId() == null) {
                logger.debug("adding user: " + user);
                sqlMap.insert("insertUser", getUserMap(user, plainTextPassword));
            } else {
                logger.debug("updating user: " + user);
                sqlMap.update("updateUser", getUserMap(user, plainTextPassword));
            }
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public void removeUser(User user) throws ControllerException {
        logger.debug("removing user: " + user);

        try {
            sqlMap.delete("deleteUser", user);
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public boolean authorizeUser(User user, String plainTextPassword) throws ControllerException {
        try {
            Credentials credentials = (Credentials) sqlMap.queryForObject("getUserCredentials", user);

            if (credentials != null) {
                String checkPasswordHash = encrypter.getHash(plainTextPassword, credentials.getSalt());
                return checkPasswordHash.equals(credentials.getPassword());
            }

            return false;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public void loginUser(User user) throws ControllerException {
        try {
            sqlMap.update("loginUser", user.getId());
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public void logoutUser(User user) throws ControllerException {
        try {
            sqlMap.update("logoutUser", user.getId());
        } catch (Exception e) {
            throw new ControllerException(e);
        }

    }

    public boolean isUserLoggedIn(User user) throws ControllerException {
        try {
            return (Boolean) sqlMap.queryForObject("isUserLoggedIn", user.getId());
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    private Map getUserMap(User user, String plainTextPassword) {
        Map parameterMap = new HashMap();

        if (user.getId() != null) {
            parameterMap.put("id", user.getId());
        }

        parameterMap.put("username", user.getUsername());
        parameterMap.put("firstName", user.getFirstName());
        parameterMap.put("lastName", user.getLastName());
        parameterMap.put("organization", user.getOrganization());
        parameterMap.put("email", user.getEmail());
        parameterMap.put("phoneNumber", user.getPhoneNumber());
        parameterMap.put("description", user.getDescription());

        // hash the user's password before storing it in the database
        try {
            String salt = encrypter.getSalt();
            parameterMap.put("password", encrypter.getHash(plainTextPassword, salt));
            parameterMap.put("salt", salt);
        } catch (EncryptionException ee) {
            // ignore this
        }

        return parameterMap;
    }

    public Map<String, String> getUserPreferences(User user) throws ControllerException {
        try {
            return (Map<String, String>) sqlMap.queryForMap("getUserPreferences", user.getId(), "name", "value");
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public void setUserPreference(User user, String name, String value) throws ControllerException {
        try {
            Map parameterMap = new HashMap();
            parameterMap.put("personId", user.getId().toString());
            parameterMap.put("name", name);
            parameterMap.put("value", value);

            sqlMap.update("insertUserPreference", parameterMap);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

}
