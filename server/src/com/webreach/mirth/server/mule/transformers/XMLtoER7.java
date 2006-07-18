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


/**
 * Encodes an HL7 message String in XML. 
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 */
package com.webreach.mirth.server.mule.transformers;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.server.mule.util.ER7Util;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.XMLParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class XMLtoER7 extends AbstractTransformer {
	private Logger logger = Logger.getLogger(this.getClass());
	private final String HL7XML = "HL7 XML";
	private final String HL7ER7 = "HL7 ER7";
	public XMLtoER7() {
		super();
		//registerSourceType(String.class);
		setReturnClass(HashMap.class);
	}

	public Object doTransform(Object source) throws TransformerException {
		HashMap returnMap = new HashMap();
		if (source instanceof HashMap){
			((HashMap)source).put(HL7ER7, new ER7Util().ConvertToER7(getPayload((HashMap)source)));
			return source;
		}else if (source instanceof String){
			returnMap.put(HL7ER7,new ER7Util().ConvertToER7((String)source));
			returnMap.put(HL7XML,new ER7Util().ConvertToXML(source));
			return returnMap;
		}else
			return null;
	}
	private String getPayload(HashMap message) {
		return message.get(HL7XML).toString();
	}

}
