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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Script;

public class CompiledScriptCache {
	private Logger logger = Logger.getLogger(this.getClass());
	private Map<String, Script> compiledScripts = new HashMap<String, Script>();
	private Map<String, String> sourceScripts = new HashMap<String, String>();

	// singleton pattern
	private static CompiledScriptCache instance = null;

	private CompiledScriptCache() {
		
	}

	public static CompiledScriptCache getInstance() {
		synchronized (CompiledScriptCache.class) {
			if (instance == null)
				instance = new CompiledScriptCache();

			return instance;
		}
	}

	public Script getCompiledScript(String id) {
		return compiledScripts.get(id);
	}

	public String getSourceScript(String id) {
		return sourceScripts.get(id);
	}
	
	public void putCompiledScript(String id, Script compiledScript, String sourceScript) {
		logger.debug("adding script to cache");
		compiledScripts.put(id, compiledScript);
		sourceScripts.put(id, sourceScript);
	}
    
    public void removeCompiledScript(String id) {
        logger.debug("removing script from cache");
        compiledScripts.remove(id);
        sourceScripts.remove(id);
    }
}
