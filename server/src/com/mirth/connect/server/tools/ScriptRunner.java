/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.derby.tools.ij;
import org.apache.log4j.Logger;

import com.mirth.connect.server.util.DatabaseUtil;

public class ScriptRunner {
    private static Logger logger = Logger.getLogger("ScriptRunner");

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java ScriptRunner scriptFile");
        } else {
            runScript(args[0]);
        }
    }

    public static void runScript(String scriptFile) {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            Connection connection = DriverManager.getConnection("jdbc:derby:mirthdb;create=true");
            DataInputStream in = new DataInputStream(new FileInputStream(new File(scriptFile)));
            DataOutputStream out = new DataOutputStream(new FileOutputStream("scriptrunner.log"));
            ij.runScript(connection, in, "UTF-8", out, "UTF-8");
        } catch (Exception e) {
            logger.error("error executing script", e);
        }
    }

    /**
     * This will run the script using the database connector that has been
     * defined
     * 
     * @param scriptFile
     */
    public static void runScript(File scriptFile) {
        try {
            DatabaseUtil.executeScript(scriptFile, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
