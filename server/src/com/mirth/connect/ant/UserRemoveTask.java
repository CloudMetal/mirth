/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.ant;

import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.User;

/**
 * an ant task to remove a mirth user
 * 
 * @author andrzej@coalese.com
 */

public class UserRemoveTask extends AbstractMirthTask {
	protected String userid = "";

	/**
	 * @param userid
	 */
	public void setUserid(String userid) {
		this.userid = userid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */

	public void executeTask() throws BuildException {
		try {
			if (userid.length() > 0) {
				connectClient();
				commandUserRemove();
				disconnectClient();
			} else {
				throw (new BuildException("Userid no specified"));
			}
		} catch (ClientException e) {
			throw (new BuildException("Mirth client exception caught: " + e.getMessage(), e));
		}
	}

	private void commandUserRemove() throws ClientException {
		if (userid.equalsIgnoreCase(user)) {
			throw (new BuildException("Cannot remove current user"));
		}

		List<User> users = client.getUser(null);

		for (Iterator<User> iter = users.iterator(); iter.hasNext();) {
			User u = iter.next();
			if (u.getId().toString().equalsIgnoreCase(userid) || u.getUsername().equalsIgnoreCase(userid)) {
				client.removeUser(u);
				System.out.println("User \"" + u.getUsername() + "\" successfully removed.");
				break;
			}
		}
	}

}
