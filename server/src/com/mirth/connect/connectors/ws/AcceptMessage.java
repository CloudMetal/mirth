/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.ws;

public abstract class AcceptMessage {
    protected WebServiceMessageReceiver webServiceMessageReceiver;

    public AcceptMessage(WebServiceMessageReceiver webServiceMessageReceiver) {
        this.webServiceMessageReceiver = webServiceMessageReceiver;
    }
}
