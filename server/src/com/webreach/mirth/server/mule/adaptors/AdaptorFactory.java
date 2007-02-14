package com.webreach.mirth.server.mule.adaptors;

import com.webreach.mirth.model.MessageObject.Protocol;

public class AdaptorFactory {
	public static Adaptor getAdaptor(Protocol protocol) {
		if (protocol.equals(Protocol.HL7V2)) {
			return new HL7v2Adaptor();
		} else if (protocol.equals(Protocol.HL7V3)) {
			return new HL7v3Adaptor();
		} else {
			return new XMLAdaptor();
		}
	}
}
