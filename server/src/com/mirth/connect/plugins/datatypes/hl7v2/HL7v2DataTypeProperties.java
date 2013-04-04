/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v2;

import com.mirth.connect.model.datatype.DataTypeProperties;

public class HL7v2DataTypeProperties extends DataTypeProperties {
    
    public HL7v2DataTypeProperties() {
        serializationProperties = new HL7v2SerializationProperties();
        deserializationProperties = new HL7v2DeserializationProperties();
        responseGenerationProperties = new HL7v2ResponseGenerationProperties();
        responseValidationProperties = new HL7v2ResponseValidationProperties();
    }
    
}
