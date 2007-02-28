package com.webreach.mirth.server.controllers.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webreach.mirth.model.Alert;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.tools.ScriptRunner;
import com.webreach.mirth.server.util.UUIDGenerator;

public class AlertControllerTest extends TestCase {
	private AlertController alertController = new AlertController();
	private ConfigurationController configurationController = new ConfigurationController();
	private List<Alert> sampleAlertList;
	
	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("derby-database.sql");

		// initialize the configuration controller to cache encryption key
		configurationController.initialize();

		sampleAlertList = new ArrayList<Alert>();
		
		for (int i = 0; i < 10; i++) {
			Alert sampleAlert = new Alert();
			sampleAlert.setId(UUIDGenerator.getUUID());
			sampleAlert.setName("Sample Alert" + i);
			sampleAlert.setEnabled(true);
			sampleAlert.setExpression("exception");
			sampleAlert.setTemplate("template");

			for (int j = 0; j < 10; j++) {
				sampleAlert.getChannels().add("channel" + String.valueOf(j));
				sampleAlert.getEmails().add("test" + j + "@test.com");
			}
			
			sampleAlertList.add(sampleAlert);
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetAlert() throws ControllerException {
		insertSampleAlerts();
		
		for (Iterator iter = sampleAlertList.iterator(); iter.hasNext();) {
			Alert sampleAlert = (Alert) iter.next();
			List<Alert> testAlertList = alertController.getAlert(sampleAlert);

			Assert.assertFalse(testAlertList.isEmpty());
		}
	}

	public void testGetAlertByChannelId() throws ControllerException {
		insertSampleAlerts();
		
		for (Iterator iter = sampleAlertList.iterator(); iter.hasNext();) {
			Alert sampleAlert = (Alert) iter.next();
			List<Alert> testAlertList = alertController.getAlertByChannelId("channel0");
			
			Assert.assertFalse(testAlertList.isEmpty());
			Assert.assertEquals(10, testAlertList.size());			
		}
	}

	public void testUpdateAlert() throws ControllerException {
		Alert sampleAlert = sampleAlertList.get(0);
		alertController.updateAlert(sampleAlert);
		List<Alert> testAlertList = alertController.getAlert(sampleAlert);
		Alert testAlert = testAlertList.get(0);
		
		Assert.assertEquals(1, testAlertList.size());
		Assert.assertEquals(sampleAlert, testAlert);
	}
	
	public void testRemoveAlert() throws ControllerException {
		insertSampleAlerts();
		
		Alert sampleAlert = sampleAlertList.get(0);
		alertController.removeAlert(sampleAlert);
		List<Alert> testAlertList = alertController.getAlert(null);

		Assert.assertFalse(testAlertList.contains(sampleAlert));
	}
	
	public void insertSampleAlerts() throws ControllerException {
		for (Iterator iter = sampleAlertList.iterator(); iter.hasNext();) {
			Alert sampleAlert = (Alert) iter.next();
			alertController.updateAlert(sampleAlert);
		}
	}
}
