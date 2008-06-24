/*
 * Copyright 2002-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, 2002, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.wsif.schema;

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * A class to represent a &lt;simpleType&gt; element in a schema
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class SimpleType extends SchemaType implements Serializable {

	static final long serialVersionUID = 1L;
	
	private String name = "";
	private QName typeName = null;

	/**
	 * Constructor
	 * @param el The dom element for this simpleType
	 */	
    SimpleType(Element el, String tns) {
        typeName = getAttributeQName(el, "name", tns);
        
        // If the element has no name, we cannot map it. Don't do any more processing
        // of this type
        if (typeName == null) return;        
        
        name = typeName.getLocalPart();    	
    }

	/**
	 * @see SchemaType#isComplex()
	 */    
	public boolean isComplex() {
		return false;
	}

	/**
	 * @see SchemaType#getTypeName()
	 */	
	public QName getTypeName() {
		return typeName;
	}
	
	/**
	 * @see SchemaType#isSimple()
	 */ 
    public boolean isSimple() {
        return true;
    }	
}
