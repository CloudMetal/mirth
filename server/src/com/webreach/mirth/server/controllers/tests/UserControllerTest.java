package com.webreach.mirth.server.controllers.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webreach.mirth.model.User;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.UserController;
import com.webreach.mirth.server.tools.ScriptRunner;

public class UserControllerTest extends TestCase {
	private UserController userController = UserController.getInstance();
	private ConfigurationController configurationController = ConfigurationController.getInstance();
	private List<User> sampleUserList;
	
	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("derby-database.sql");

		// initialize the configuration controller to cache encryption key
		configurationController.initialize();

		sampleUserList = new ArrayList<User>();
		
		for (int i = 0; i < 10; i++) {
			User sampleUser = new User();
			sampleUser.setUsername("user" + i);
			sampleUser.setFirstName("User " + i);
			sampleUser.setLastName("User " + i);
			sampleUser.setEmail("user" + i + "@email.com");
			sampleUserList.add(sampleUser);
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testUpdateUser() throws ControllerException {
		User sampleUser = sampleUserList.get(0);
		userController.updateUser(sampleUser, "password");
		List<User> testUserList = userController.getUser(sampleUser);
		User testUser = testUserList.get(0);
		
		Assert.assertEquals(1, testUserList.size());
		Assert.assertEquals(sampleUser.getUsername(), testUser.getUsername());
	}
	
	public void testGetUser() throws ControllerException {
		insertSampleUsers();
		
		for (Iterator iter = sampleUserList.iterator(); iter.hasNext();) {
			User sampleUser = (User) iter.next();
			List<User> testUserList = userController.getUser(sampleUser);
			Assert.assertFalse(testUserList.isEmpty());
		}
	}
	
	public void testRemoveUser() throws ControllerException {
		insertSampleUsers();
		
		User sampleUser = sampleUserList.get(0);
		userController.removeUser(sampleUser);
		List<User> testUserList = userController.getUser(null);

		Assert.assertFalse(testUserList.contains(sampleUser));
	}
	
	public void testAuthorizeUser() throws ControllerException {
		insertSampleUsers();
		
		User user = new User();
		user.setUsername("user0");
		assertTrue(userController.authorizeUser(user, "password"));
	}
	
	public void testLoginUser() throws ControllerException {
		insertSampleUsers();

		User testUser = userController.getUser(null).get(0);
		userController.loginUser(testUser);
		assertTrue(userController.isUserLoggedIn(testUser));
	}

	public void testLogoutUser() throws ControllerException {
		insertSampleUsers();

		User testUser = userController.getUser(null).get(0);
		userController.logoutUser(testUser);
		assertFalse(userController.isUserLoggedIn(testUser));
	}

	public void insertSampleUsers() throws ControllerException {
		for (Iterator iter = sampleUserList.iterator(); iter.hasNext();) {
			User sampleUser = (User) iter.next();
			userController.updateUser(sampleUser, "password");
		}
	}

}
