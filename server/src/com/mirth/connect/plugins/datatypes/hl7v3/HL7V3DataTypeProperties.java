/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v3;

import com.mirth.connect.model.datatype.DataTypeProperties;

public class HL7V3DataTypeProperties extends DataTypeProperties {
    
    public HL7V3DataTypeProperties() {
        serializationProperties = new HL7V3SerializationProperties();
    }

}
