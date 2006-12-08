package com.webreach.mirth.server.controllers.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.tools.ScriptRunner;

public class ChannelControllerTest extends TestCase {
	private ChannelController channelController = new ChannelController();
	private ConfigurationController configurationController = new ConfigurationController();
	private List<Channel> sampleChannelList;
	
	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("derby-database.sql");

		// initialize the configuration controller to cache encryption key
		configurationController.initialize();

		sampleChannelList = new ArrayList<Channel>();
		
		for (int i = 0; i < 10; i++) {
			Channel sampleChannel = new Channel();
			sampleChannel.setId(configurationController.getGuid());
			sampleChannel.setName("Channel" + i);
			sampleChannel.setDescription("This is a sample channel");
			sampleChannel.setEnabled(true);
			sampleChannel.setVersion(configurationController.getVersion());
			sampleChannel.setRevision(0);
			sampleChannel.setDirection(Channel.Direction.INBOUND);
			sampleChannel.setProtocol(Channel.Protocol.HL7);
			sampleChannel.setMode(Channel.Mode.ROUTER);
			sampleChannel.setSourceConnector(new Connector());
			sampleChannel.setPreprocessingScript("return 1;");
			
			Properties sampleProperties = new Properties();
			sampleProperties.setProperty("testProperty", "true");
			sampleChannel.setProperties(sampleProperties);
			
			sampleChannelList.add(sampleChannel);
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testUpdateChannel() throws ControllerException {
		Channel sampleChannel = sampleChannelList.get(0);
		channelController.updateChannel(sampleChannel, true);
		List<Channel> testChannelList = channelController.getChannel(sampleChannel);
		Channel testChannel = testChannelList.get(0);
		
		Assert.assertEquals(1, testChannelList.size());
		Assert.assertEquals(sampleChannel, testChannel);
	}
	
	public void testGetChannel() throws ControllerException {
		insertSampleChannels();
		
		List<Channel> testChannelList = channelController.getChannel(null);
		
		for (Iterator iter = sampleChannelList.iterator(); iter.hasNext();) {
			Channel sampleChannel = (Channel) iter.next();
			Assert.assertTrue(testChannelList.contains(sampleChannel));
		}
	}
	
	public void testRemoveChannel() throws ControllerException {
		insertSampleChannels();
		
		Channel sampleChannel = sampleChannelList.get(0);
		channelController.removeChannel(sampleChannel);
		List<Channel> testChannelList = channelController.getChannel(null);

		Assert.assertFalse(testChannelList.contains(sampleChannel));
	}
	
	public void insertSampleChannels() throws ControllerException {
		for (Iterator iter = sampleChannelList.iterator(); iter.hasNext();) {
			Channel sampleChannel = (Channel) iter.next();
			channelController.updateChannel(sampleChannel, true);
		}
	}

}
