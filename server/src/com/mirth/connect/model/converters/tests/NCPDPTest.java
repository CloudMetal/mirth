/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model.converters.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.webreach.mirth.model.converters.DocumentSerializer;
import com.webreach.mirth.model.converters.NCPDPSerializer;
import com.webreach.mirth.model.converters.NCPDPXMLHandler;
import com.webreach.mirth.model.converters.SerializerException;
import com.webreach.mirth.model.converters.Stopwatch;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Jun 6, 2007
 * Time: 2:04:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class NCPDPTest {
	public static void main(String[] args) {
		String testMessage = "";
        ArrayList<String> testFiles = new ArrayList<String>();
        testFiles.add("C:\\NCPDP_51_B1_Request.txt");
        testFiles.add("C:\\NCPDP_51_B1_Request_v2.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response_v2.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response_v3.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response_v4.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response_v5.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response_v6.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response_v7.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response_v8.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response_v9.txt");
        testFiles.add("C:\\NCPDP_51_B2_Request.txt");
        testFiles.add("C:\\NCPDP_51_B2_Request_v2.txt");
        testFiles.add("C:\\NCPDP_51_B2_Response.txt");
        testFiles.add("C:\\NCPDP_51_B2_Response_v2.txt");
        testFiles.add("C:\\NCPDP_51_B2_Response_v3.txt");
        testFiles.add("C:\\NCPDP_51_B3_Request.txt");
        testFiles.add("C:\\NCPDP_51_B3_Response.txt");
        testFiles.add("C:\\NCPDP_51_B3_Response_v2.txt");
        testFiles.add("C:\\NCPDP_51_B3_Response_v3.txt");
        testFiles.add("C:\\NCPDP_51_E1_Request.txt");
        testFiles.add("C:\\NCPDP_51_E1_Response.txt");
        testFiles.add("C:\\NCPDP_51_E1_Response_v2.txt");
        testFiles.add("C:\\NCPDP_51_E1_Response_v3.txt");
        testFiles.add("C:\\NCPDP_51_E1_Response_v4.txt");
        testFiles.add("C:\\NCPDP_51_E1_Response_v5.txt");
        testFiles.add("C:\\NCPDP_51_N1_Request.txt");
        testFiles.add("C:\\NCPDP_51_N2_Request.txt");
        testFiles.add("C:\\NCPDP_51_P1_Request.txt");
        testFiles.add("C:\\NCPDP_51_P1_Response.txt");
        testFiles.add("C:\\NCPDP_51_P1_Response_v2.txt");
        testFiles.add("C:\\NCPDP_51_P1_Response_v3.txt");
        testFiles.add("C:\\NCPDP_51_P2_Request.txt");
        testFiles.add("C:\\NCPDP_51_P2_Response.txt");
        testFiles.add("C:\\NCPDP_51_P3_Request.txt");
        testFiles.add("C:\\NCPDP_51_P3_Response.txt");
        testFiles.add("C:\\NCPDP_51_P3_Response_v2.txt");
        testFiles.add("C:\\NCPDP_51_P4_Request.txt");
        testFiles.add("C:\\NCPDP_51_P4_Response.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_1.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_2.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_3.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_4.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_5.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_6.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_7.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_8.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_9.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_10.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_11.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_12.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_13.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_14.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_15.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_16.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_17.txt");

        Iterator iterator = testFiles.iterator();
        while(iterator.hasNext()){
            String fileName = (String) iterator.next();
            try {
                testMessage = new String(getBytesFromFile(new File(fileName)));
                System.out.println("Processing test file:" + fileName);
                //System.out.println(testMessage);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {

                long totalExecutionTime = 0;
                int iterations = 1;
                for (int i = 0; i < iterations; i++) {
                    totalExecutionTime+=runTest(testMessage);
                }

                //System.out.println("Execution time average: " + totalExecutionTime/iterations + " ms");
            }
            // System.out.println(new X12Serializer().toXML("SEG*1*2**4*5"));
            catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

	private static long runTest(String testMessage) throws SerializerException, SAXException, IOException {
		Stopwatch stopwatch = new Stopwatch();
		Properties properties = new Properties();
        String SchemaUrl = "/ncpdp.xsd";
        properties.put("useStrictParser", "true");
        properties.put("http://java.sun.com/xml/jaxp/properties/schemaSource",SchemaUrl);
        stopwatch.start();
		NCPDPSerializer serializer = new NCPDPSerializer(properties);
		String xmloutput = serializer.toXML(testMessage);
		//System.out.println(xmloutput);
		DocumentSerializer docser = new DocumentSerializer();
		Document doc = docser.fromXML(xmloutput);
		XMLReader xr = XMLReaderFactory.createXMLReader();

        NCPDPXMLHandler handler = new NCPDPXMLHandler("\u001E","\u001D","\u001C");

        xr.setContentHandler(handler);
		xr.setErrorHandler(handler);
        xr.setFeature("http://xml.org/sax/features/validation", true);
        xr.setFeature("http://apache.org/xml/features/validation/schema", true);
        xr.setFeature("http://apache.org/xml/features/validation/schema-full-checking",true);
        xr.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage","http://www.w3.org/2001/XMLSchema");
        xr.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",SchemaUrl);
        xr.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource","/ncpdp.xsd");
        xr.parse(new InputSource(new StringReader(xmloutput)));
		stopwatch.stop();

		//System.out.println(docser.toXML(doc)); //handler.getOutput());
		//System.out.println(handler.getOutput());
        //System.out.println(xmloutput);
        if (handler.getOutput().toString().replace('\n', '\r').trim().equals(testMessage.replaceAll("\\r\\n", "\r").trim())) {
			System.out.println("Test Successful!");
		} else {
			String original = testMessage.replaceAll("\\r\\n", "\r").trim();
			String newm = handler.getOutput().toString().replace('\n', '\r').trim();
			for (int i = 0; i < original.length(); i++){
				if (original.charAt(i) == newm.charAt(i)){
					System.out.print(newm.charAt(i));
				}else{
					System.out.println("");
					System.out.print("Saw: ");
					System.out.println(newm.charAt(i));
					System.out.print("Expected: ");
					System.out.print(original.charAt(i));
					break;
				}
			}
			System.out.println("Test Failed!");
		}
		return stopwatch.toValue();
	}
	// Returns the contents of the file in a byte array.
	private static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}
}
