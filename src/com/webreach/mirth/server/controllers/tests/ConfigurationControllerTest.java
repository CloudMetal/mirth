package com.webreach.mirth.server.controllers.tests;

import junit.framework.TestCase;

import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.tools.ScriptRunner;

public class ConfigurationControllerTest extends TestCase {
	private ConfigurationController configurationController = ConfigurationController.getInstance();

	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("derby-database.sql");

		// initialize the configuration controller to cache encryption key
		configurationController.initialize();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
}