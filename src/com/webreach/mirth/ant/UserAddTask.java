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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import com.webreach.mirth.client.core.Client;
import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.ServerConfiguration;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.ChannelStatus.State;

import java.util.Iterator;
import java.util.List;

/**
 * an ant task to add a mirth user
 *
 * @author andrzej@coalese.com
 */

public class UserAddTask extends AbstractMirthTask
{
	protected 	String          userid      = "";
	protected 	String          pswd        = "";
	protected 	String          name        = "";
	protected 	String          email       = "";
	
	
	/**
     * @param pswd
     */
	public void setPswd( String pswd )
	{
		this.pswd = pswd;
	}
	
	
	/**
     * @param userid
     */
	public void setUserid( String userid )
	{
		this.userid = userid;
	}
	
	/**
     * @param name
     */
	public void setName( String name )
	{
		this.name = name;
	}
	
	
	/**
     * @param email
     */
	public void setEmail( String email )
	{
		this.email = email;
	}
	
	
	/* (non-Javadoc)
   * @see org.apache.tools.ant.Task#execute()
   */
	
	public void executeTask() throws BuildException
	{
		try {
			if( userid.length() > 0 ) {
				connectClient();
				commandUserAdd();
				disconnectClient();
			} else {
				throw( new BuildException( "Userid no specified" ) );
			}
		} 
		catch( ClientException e ) {
			throw( new BuildException( "Mirth client exception caught: " + e.getMessage(), e ) );
		} 
	}
	
	private void commandUserAdd() throws ClientException, BuildException
	{
		User newUser = new User();
		
		newUser.setUsername( userid );
		newUser.setFullName( name );
		newUser.setEmail( email );
		
		List<User> users = client.getUser( null );
		
		for( Iterator<User> iter = users.iterator(); iter.hasNext(); ) {
			User luser = iter.next();
			if( luser.getUsername().equalsIgnoreCase( userid ) ) {
				throw( new BuildException( "Unable to add user: userid already in use: " + userid ) );
			}
		}
		
		try {
			client.updateUser( newUser, pswd );
			System.out.println( "User \"" + userid + "\" added successfully." );
		} 
		catch( Exception e ) {
			throw( new BuildException( "Unable to add user \"" + userid + "\": " + e ) );
		}
	}
	
	
}
