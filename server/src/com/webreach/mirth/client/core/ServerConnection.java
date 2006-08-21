package com.webreach.mirth.client.core;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;

import com.webreach.mirth.client.core.ssl.EasySSLProtocolSocketFactory;

public class ServerConnection {
	private HttpClient client;
	private String address;
	
	public ServerConnection(String address) {
		this.address = address;
		client = new HttpClient();
		Protocol mirthHttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 8443);
		Protocol.registerProtocol("https", mirthHttps);
	}
	
	/**
	 * Executes a POST method on a servlet with a set of parameters.
	 * 
	 * @param servletName The name of the servlet.
	 * @param params An array of NameValuePair objects.
	 * @return
	 * @throws ClientException
	 */
	public String executePostMethod(String servletName, NameValuePair[] params) throws ClientException {
		PostMethod post = null;
		
		try {
			post = new PostMethod(address + servletName);
			post.setRequestBody(params);

			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			return post.getResponseBodyAsString();
		} catch (Exception e) {
			throw new ClientException(e);
		} finally {
			post.releaseConnection();
		}
	}

}
