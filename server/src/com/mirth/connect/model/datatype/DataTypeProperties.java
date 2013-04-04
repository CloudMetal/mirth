package com.mirth.connect.model.datatype;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;

public abstract class DataTypeProperties implements Serializable {
	
	protected SerializationProperties serializationProperties;
	protected DeserializationProperties deserializationProperties;
	protected BatchProperties batchProperties;
	protected ResponseGenerationProperties responseGenerationProperties;
	protected ResponseValidationProperties responseValidationProperties;
    
    public SerializerProperties getSerializerProperties() {
        return new SerializerProperties(getSerializationProperties(), getDeserializationProperties(), getBatchProperties());
    }
    
    public SerializationProperties getSerializationProperties() {
    	return serializationProperties;
    }
    
    public DeserializationProperties getDeserializationProperties() {
    	return deserializationProperties;
    }
    
    public BatchProperties getBatchProperties() {
    	return batchProperties;
    }
    
    public ResponseGenerationProperties getResponseGenerationProperties() {
    	return responseGenerationProperties;
    }
    
    public ResponseValidationProperties getResponseValidationProperties() {
    	return responseValidationProperties;
    }
    
    public DataTypeProperties clone() {
    	return SerializationUtils.clone(this);
    }
    
    public boolean equals(Object object) {
    	if (object instanceof DataTypeProperties) {
    		DataTypeProperties properties = (DataTypeProperties) object;
    		if (equals(serializationProperties, properties.getSerializationProperties()) && equals(deserializationProperties, properties.getDeserializationProperties()) && equals(batchProperties, properties.getBatchProperties()) && equals(responseGenerationProperties, properties.getResponseGenerationProperties()) && equals(responseValidationProperties, properties.getResponseValidationProperties())) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    private boolean equals(DataTypePropertiesGroup group1, DataTypePropertiesGroup group2) {
    	if (group1 == null && group2 == null) {
    		return true;
    	} else if (group1 != null && group2 != null) {
    		return group1.equals(group2);
    	} else {
    		return false;
    	}
    }
}
