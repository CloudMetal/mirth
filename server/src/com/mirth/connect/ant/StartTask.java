/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.ant;

import org.apache.tools.ant.BuildException;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.ChannelStatus.State;

/**
 * an ant task to start all mirth channels
 * 
 * @author andrzej@coalese.com
 */

public class StartTask extends AbstractMirthTask {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */

	public void executeTask() throws BuildException {
		try {
			connectClient();
			commandStartAll();
			disconnectClient();
		} catch (ClientException e) {
			throw (new BuildException("Mirth client exception caught: " + e.getMessage(), e));
		}
	}

	private void commandStartAll() throws ClientException {
		for (ChannelStatus channel : client.getChannelStatusList()) {
			if (channel.getState().equals(State.STOPPED) || channel.getState().equals(State.PAUSED)) {
				if (channel.getState().equals(State.PAUSED)) {
					client.resumeChannel(channel.getChannelId());
					System.out.println("Channel " + channel.getName() + " Resumed");
				} else {
					client.startChannel(channel.getChannelId());
					System.out.println("Channel " + channel.getName() + " Started");
				}
			}
		}
	}

}
