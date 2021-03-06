/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.js;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.donkey.model.channel.ResponseConnectorPropertiesInterface;

public class JavaScriptReceiverProperties extends ConnectorProperties implements PollConnectorPropertiesInterface, ResponseConnectorPropertiesInterface {
    public static final String NAME = "JavaScript Reader";

    private PollConnectorProperties pollConnectorProperties;
    private ResponseConnectorProperties responseConnectorProperties;
    private String script;

    public JavaScriptReceiverProperties() {
        pollConnectorProperties = new PollConnectorProperties();
        responseConnectorProperties = new ResponseConnectorProperties(false);

        script = "";
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public String toFormattedString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PollConnectorProperties getPollConnectorProperties() {
        return pollConnectorProperties;
    }

    @Override
    public ResponseConnectorProperties getResponseConnectorProperties() {
        return responseConnectorProperties;
    }
}
