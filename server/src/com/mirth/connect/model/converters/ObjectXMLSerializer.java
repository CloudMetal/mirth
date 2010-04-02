/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.util.Map;
import java.util.WeakHashMap;

import org.w3c.dom.Document;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class ObjectXMLSerializer implements IXMLSerializer<Object> {
    private XStream xstream;
    private static final Map<String, String> stringCache = new WeakHashMap<String, String>();

    public ObjectXMLSerializer() {
        xstream = new XStream(new XppDriver());
        xstream.registerConverter(new StringConverter(stringCache));
        xstream.setMode(XStream.NO_REFERENCES);
    }

    public ObjectXMLSerializer(Class<?>[] aliases) {
        xstream = new XStream(new XppDriver());
        xstream.registerConverter(new StringConverter(stringCache));
        xstream.processAnnotations(aliases);
        xstream.setMode(XStream.NO_REFERENCES);
    }

    public ObjectXMLSerializer(Class<?>[] aliases, Converter[] converters) {
        xstream = new XStream(new XppDriver());
        xstream.processAnnotations(aliases);
        xstream.setMode(XStream.NO_REFERENCES);

        for (int i = 0; i < converters.length; i++) {
            xstream.registerConverter(converters[i]);
        }
    }

    public String toXML(Object source) {
        return xstream.toXML(source);
    }

    public String toXML(Object source, Class<?>[] aliases) {
        xstream.processAnnotations(aliases);
        String retval = xstream.toXML(source);
        xstream.processAnnotations(new Class<?>[] {});
        return retval;
    }

    public Object fromXML(String source) {
        return xstream.fromXML(source);
    }

    public Object fromXML(String source, Class<?>[] aliases) {
        xstream.processAnnotations(aliases);
        Object retval = xstream.fromXML(source);
        xstream.processAnnotations(new Class<?>[] {});
        return retval;
    }

    public Map<String, String> getMetadata() {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, String> getMetadata(Document doc) {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, String> getMetadataFromDocument(Document doc) throws SerializerException {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, String> getMetadataFromEncoded(String source) throws SerializerException {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, String> getMetadataFromXML(String xmlSource) throws SerializerException {
        // TODO Auto-generated method stub
        return null;
    }

}
