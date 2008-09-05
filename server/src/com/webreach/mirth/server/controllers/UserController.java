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

import java.util.List;

import com.webreach.mirth.model.Preferences;
import com.webreach.mirth.model.User;

public interface UserController {
    public void initialize();

    public List<User> getUser(User user) throws ControllerException;

    public void updateUser(User user, String plainTextPassword) throws ControllerException;

    public void removeUser(User user) throws ControllerException;

    public boolean authorizeUser(User user, String plainTextPassword) throws ControllerException;

    public void loginUser(User user) throws ControllerException;

    public void logoutUser(User user) throws ControllerException;

    public boolean isUserLoggedIn(User user) throws ControllerException;

    public Preferences getUserPreferences(User user) throws ControllerException;

    public void setUserPreference(User user, String name, String value) throws ControllerException;
}
