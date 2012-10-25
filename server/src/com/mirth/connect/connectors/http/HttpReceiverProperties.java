/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.ListenerConnectorProperties;
import com.mirth.connect.donkey.model.channel.ListenerConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.donkey.model.channel.ResponseConnectorPropertiesInterface;

public class HttpReceiverProperties extends ConnectorProperties implements ListenerConnectorPropertiesInterface, ResponseConnectorPropertiesInterface {
    private ListenerConnectorProperties listenerConnectorProperties;
    private ResponseConnectorProperties responseConnectorProperties;

    private boolean bodyOnly;
    private String responseContentType;
    private String responseStatusCode;
    private Map<String, String> responseHeaders;
    private String charset;
    private String contextPath;
    private String timeout;

    public HttpReceiverProperties() {
        listenerConnectorProperties = new ListenerConnectorProperties("80");
        responseConnectorProperties = new ResponseConnectorProperties("None", new String[] { "None" });

        this.bodyOnly = true;
        this.responseContentType = "text/plain";
        this.responseStatusCode = "";
        this.responseHeaders = new LinkedHashMap<String, String>();
        this.charset = "UTF-8";
        this.contextPath = "";
        this.timeout = "0";
    }

    public boolean isBodyOnly() {
        return bodyOnly;
    }

    public void setBodyOnly(boolean bodyOnly) {
        this.bodyOnly = bodyOnly;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public void setResponseContentType(String responseContentType) {
        this.responseContentType = responseContentType;
    }

    public String getResponseStatusCode() {
        return responseStatusCode;
    }

    public void setResponseStatusCode(String responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    @Override
    public String getProtocol() {
        return "HTTP";
    }

    @Override
    public String getName() {
        return "HTTP Listener";
    }

    @Override
    public String toFormattedString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ListenerConnectorProperties getListenerConnectorProperties() {
        return listenerConnectorProperties;
    }

    @Override
    public ResponseConnectorProperties getResponseConnectorProperties() {
        return responseConnectorProperties;
    }
}
