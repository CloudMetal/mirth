package com.webreach.mirth.model.converters;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.derby.tools.sysinfo;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class HL7Test {

	public static void main(String[] args) {
		      
		String testMessage = "";
		String testXML = null;
		try {
			testMessage = new String(getBytesFromFile(new File(args[0])));
			if (args.length>1){
				testXML =  new String(getBytesFromFile(new File(args[1])));
			}
			System.out.println(testMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			
			long totalExecutionTime = 0;
			int iterations = 1;
			for (int i = 0; i < iterations; i++) {
				totalExecutionTime+=runTest(testMessage, testXML);
			}
			
			System.out.println("Execution time average: " + totalExecutionTime/iterations + " ms");
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

	private static long runTest(String testMessage, String xml) throws SerializerException, SAXException, IOException {
		Stopwatch stopwatch = new Stopwatch();
		Properties properties = new Properties();
		properties.put("useStrictParser", "false");
		properties.put("handleRepetitions", "true");
		stopwatch.start();
		ER7Serializer serializer = new ER7Serializer(properties);
		String xmloutput = xml;
		if (xml == null){
			xmloutput = serializer.toXML(testMessage);
		}
		String er7 = serializer.fromXML(xmloutput);
		stopwatch.stop();
		//System.out.println(xmloutput);
		DocumentSerializer docser = new DocumentSerializer();
		docser.setPreserveSpace(false);
		Document doc = docser.fromXML(xmloutput);
		
		
		
		System.out.println(docser.toXML(doc)); 
		System.out.println(er7);
		if (er7.replace('\n', '\r').trim().equals(testMessage.replaceAll("\\r\\n", "\r").trim())) {
			System.out.println("Test Successful!");
		} else {
			String original = testMessage.replaceAll("\\r\\n", "\r").trim();
			String newm = er7.replace('\n', '\r').trim();
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
			System.out.println("\nTest Failed!");
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
