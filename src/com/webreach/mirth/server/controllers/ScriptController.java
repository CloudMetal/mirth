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
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.webreach.mirth.server.util.SqlConfig;

public class ScriptController {
	private Logger logger = Logger.getLogger(this.getClass());
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();

	private static ScriptController instance = null;
	
	private ScriptController() {
		
	}
	
	public static ScriptController getInstance() {
		synchronized (ScriptController.class) {
			if (instance == null) {
				instance = new ScriptController();
			}
			
			return instance;
		}
	}   
	
	/**
	 * Adds a script with the specified id to the database. If a script with the
	 * id already exists it will be overwritten.
	 * 
	 * @param id
	 * @param script
	 * @throws ControllerException
	 */
	public void putScript(String id, String script) throws ControllerException {
		logger.debug("adding script: id=" + id);

		try {
			Map parameterMap = new HashMap();
			parameterMap.put("id", id);
			parameterMap.put("script", script);

			if (getScript(id) == null) {
				sqlMap.insert("insertScript", parameterMap);
			} else {
				sqlMap.update("updateScript", parameterMap);
			}
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	/**
	 * Returns the script with the specified id, null otherwise.
	 * 
	 * @param id
	 * @return
	 * @throws ControllerException
	 */
	public String getScript(String id) throws ControllerException {
		logger.debug("retrieving script: id=" + id);

		try {
            Object script = sqlMap.queryForObject("getScript", id);
            if(script != null)
                return (String) script;
            else 
                return null;
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}
	
	public void clearScripts() throws ControllerException {
		logger.debug("clearing scripts table");
		
		try {
			sqlMap.delete("deleteScript", null);
		} catch (SQLException e) {
			throw new ControllerException("error clearing scripts", e);
		}
	}
}
