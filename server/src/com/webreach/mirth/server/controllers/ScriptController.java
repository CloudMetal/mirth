/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.controllers;


public abstract class ScriptController extends Controller {
    public static ScriptController getInstance() {
        return ControllerFactory.getFactory().createScriptController();
    }
    
    /**
     * Adds a script with the specified id to the database. If a script with the
     * id already exists it will be overwritten.
     * 
     * @param id
     * @param script
     * @throws ControllerException
     */
    public abstract void putScript(String id, String script) throws ControllerException;

    /**
     * Returns the script with the specified id, null otherwise.
     * 
     * @param id
     * @return
     * @throws ControllerException
     */
    public abstract String getScript(String id) throws ControllerException;

    public abstract void clearScripts() throws ControllerException;
}
