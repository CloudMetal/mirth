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
 * The Original Code is Mirth-Ant.
 *
 * The Initial Developer of the Original Code is
 * Coalese Corporation.
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Andrzej Taramina <andrzej@coalese.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.ant;

import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.User;

/**
 * an ant task to list all mirth users
 *
 * @author andrzej@coalese.com
 */

public class UserListTask extends AbstractMirthTask
{
	
	/* (non-Javadoc)
   * @see org.apache.tools.ant.Task#execute()
   */
	
	public void executeTask() throws BuildException
	{
		try {
			connectClient();
			commandUserList();
			disconnectClient();
		} 
		catch( ClientException e ) {
			throw( new BuildException( "Mirth client exception caught: " + e.getMessage(), e ) );
		} 
	}
	
	private void commandUserList() throws ClientException 
	{
		List<User> users = client.getUser( null );
		
		System.out.println("ID\tUser\t\tName\t\t\tEmail");
		
		for( Iterator<User> iter = users.iterator(); iter.hasNext(); ) {
			User user = iter.next();
			System.out.println( user.getId() + "\t\t" + user.getUsername() + "\t\t" + user.getFullName() + "\t\t\t" + user.getEmail() );
		}
	}
	
	
}
