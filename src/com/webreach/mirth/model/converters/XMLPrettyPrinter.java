package com.webreach.mirth.model.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.webreach.mirth.util.Entities;

public class XMLPrettyPrinter implements ContentHandler {

	private Writer out;
	private int depth = 0; // depth in hierarchy

	private Entities encoder = Entities.getInstance();

	// I could allow the user to set a lot more details about
	// how the XML is indented; e.g. how many spaces, tabs or spaces,
	// etc.; but since this wouldn't add anything to the discussion
	// of XML I'll leave it as an exercise for the student

	public XMLPrettyPrinter(Writer out) {
		this.out = out;
	}

	public XMLPrettyPrinter(OutputStream out) {
		try {
			this.out = new OutputStreamWriter(out, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println("Something is seriously wrong." + " Your VM does not support UTF-8 encoding!");
		}
	}

	public void setDocumentLocator(Locator locator) {}

	public void startDocument() throws SAXException {

		depth = 0; // so instance can be reused
		try {
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		} catch (IOException e) {
			throw new SAXException(e);
		}

	}

	public void endDocument() throws SAXException {
		try {
			out.flush();
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	public void startElement(String name, AttributeList atts) throws SAXException {
		try {
			// indent();
			out.write("<");
			out.write(name);
			out.write(">");
			depth++;
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	public void endElement(String name) throws SAXException {
		try {
			depth--;
			// indent();
			out.write("</");
			out.write(name);
			out.write(">");
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	public void characters(char[] text, int start, int length) throws SAXException {
		try {
			// indent();
			out.write(encoder.encode(text, start, length));
			// out.write("\r\n");
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	public void ignorableWhitespace(char[] text, int start, int length) throws SAXException {

	}

	public void processingInstruction(String target, String data) throws SAXException {
		try {
			// indent();
			out.write("<?");
			out.write(target);
			out.write(" ");
			out.write(data);
			out.write("?>");
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	private void indent() throws IOException {

		int spaces = 2; // number of spaces to indent

		for (int i = 0; i < depth * spaces; i++) {
			out.write(' ');
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		endElement(localName);

	}

	public void endPrefixMapping(String prefix) throws SAXException {
	// TODO Auto-generated method stub

	}

	public void skippedEntity(String name) throws SAXException {
	// TODO Auto-generated method stub

	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

		startElement(localName, (AttributeList) atts);

	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
	// TODO Auto-generated method stub

	}

}
