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
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.server.mule.components;

import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.Callable;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.util.ACKGenerator;

public class SoapChannel implements Callable, SoapService {
	public Object onCall(UMOEventContext eventContext) throws Exception {
		return eventContext.getTransformedMessage();
	}

	public String acceptMessage(String message) {
		/*
		MessageObject mo = (MessageObject)message;
	
		ACKGenerator generator = new ACKGenerator();
		try{
			return generator.generateAckResponse(mo.getRawData());
		}catch (Exception e){
			return new String();
		}
		*/
		return message;
		
	}


}
