package com.webreach.mirth.model.converters;

import java.util.Map;
import java.util.Properties;

import com.webreach.mirth.model.MessageObject.Protocol;

public class SerializerFactory {
	public static IXMLSerializer<String> getSerializer(Protocol protocol, Map properties) {
		if (protocol.equals(Protocol.HL7V2)) {
			return new ER7Serializer(properties);
		} else if (protocol.equals(Protocol.HL7V3)) {
			return new HL7V3Serializer(properties);
		} else if (protocol.equals(Protocol.X12)) {
			return new X12Serializer(properties);
		} else if (protocol.equals(Protocol.EDI)) {
			return new EDISerializer(properties);
		} else if (protocol.equals(Protocol.NCPDP)) {
			return new NCPDPSerializer(properties);
		} else if (protocol.equals(Protocol.DICOM)) {
			return new DICOMSerializer(properties);
		} else if (protocol.equals(Protocol.DELIMITED)) {
			return new DelimitedSerializer(properties);
		} else {
			return new DefaultXMLSerializer(properties);
		}
	}

	public static ER7Serializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation, boolean handleRepetitions) {
		Properties properties = new Properties();
		properties.put("useStrictParser", Boolean.toString(useStrictParser));
		properties.put("useStrictValidation", Boolean.toString(useStrictValidation));
		properties.put("handleRepetitions", Boolean.toString(handleRepetitions));
		return new ER7Serializer(properties);
	}
	public static ER7Serializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation, boolean handleRepetitions, boolean convertLFtoCR) {
		Properties properties = new Properties();
		properties.put("useStrictParser", Boolean.toString(useStrictParser));
		properties.put("useStrictValidation", Boolean.toString(useStrictValidation));
		properties.put("handleRepetitions", Boolean.toString(handleRepetitions));
		properties.put("convertLFtoCR", Boolean.toString(convertLFtoCR));
		return new ER7Serializer(properties);
		
	}
	public static ER7Serializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation) {
		Properties properties = new Properties();
		properties.put("useStrictParser", Boolean.toString(useStrictParser));
		properties.put("useStrictValidation", Boolean.toString(useStrictValidation));
		properties.put("handleRepetitions", Boolean.toString(false));
		return new ER7Serializer(properties);
	}

	public static ER7Serializer getHL7Serializer() {
		Properties properties = new Properties();
		properties.put("useStrictParser", Boolean.toString(true));
		properties.put("useStrictValidation", Boolean.toString(false));
		properties.put("handleRepetitions", Boolean.toString(false));
		return new ER7Serializer(properties);
	}

	public static X12Serializer getX12Serializer(boolean inferDelimiters) {
		Properties properties = new Properties();
		properties.put("inferDelimiters", Boolean.toString(inferDelimiters));
		return new X12Serializer(inferDelimiters);
	}

	public static EDISerializer getEDISerializer(String segmentDelim, String elementDelim, String subelementDelim) {
		Properties properties = new Properties();
		properties.put("segmentDelimiter", segmentDelim);
		properties.put("elementDelimiter", elementDelim);
		properties.put("subelementDelimiter", subelementDelim);
		return new EDISerializer(properties);
	}
    
    public static NCPDPSerializer getNCPDPSerializer(String segmentDelim, String groupDelim, String fieldDelim) {
        Properties properties = new Properties();
        properties.put("segmentDelimiter", segmentDelim);
        properties.put("groupDelimiter", groupDelim);
        properties.put("fieldDelimiter", fieldDelim);
        return new NCPDPSerializer(properties);
    }
    public static NCPDPSerializer getNCPDPSerializer(String segmentDelim, String groupDelim, String fieldDelim, boolean useStrictValidation) {
        Properties properties = new Properties();
        properties.put("segmentDelimiter", segmentDelim);
        properties.put("groupDelimiter", groupDelim);
        properties.put("fieldDelimiter", fieldDelim);
		properties.put("useStrictValidation", Boolean.toString(useStrictValidation));
        return new NCPDPSerializer(properties);
    }
}