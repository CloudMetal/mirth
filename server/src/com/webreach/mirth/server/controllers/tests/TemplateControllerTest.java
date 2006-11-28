package com.webreach.mirth.server.controllers.tests;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.TemplateController;
import com.webreach.mirth.server.tools.ScriptRunner;

public class TemplateControllerTest extends TestCase {
	private TemplateController templateController = new TemplateController();
	private ConfigurationController configurationController = new ConfigurationController();

	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("database.sql");

		// initialize the configuration controller to cache encryption key
		configurationController.initialize();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testPutScript() throws ControllerException {
		String id = configurationController.getGuid();
		String template = "<sample><test>hello world</test></sample>";
		templateController.putTemplate(id, template);

		Assert.assertEquals(template, templateController.getTemplate(id));
	}

	public void testGetScript() throws ControllerException {
		String id = configurationController.getGuid();
		String template = "<sample><test>hello world</test></sample>";
		templateController.putTemplate(id, template);

		Assert.assertEquals(template, templateController.getTemplate(id));
	}
}