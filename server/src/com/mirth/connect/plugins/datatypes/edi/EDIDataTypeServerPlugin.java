package com.mirth.connect.plugins.datatypes.edi;

import com.mirth.connect.donkey.server.message.AutoResponder;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;
import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.server.message.DefaultAutoResponder;

public class EDIDataTypeServerPlugin extends DataTypeServerPlugin {
    private DataTypeDelegate dataTypeDelegate = new EDIDataTypeDelegate();

    @Override
    public String getPluginPointName() {
        return dataTypeDelegate.getName();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public AutoResponder getAutoResponder(SerializationProperties serializationProperties, ResponseGenerationProperties generationProperties) {
        return new DefaultAutoResponder();
    }

    @Override
    protected DataTypeDelegate getDataTypeDelegate() {
        return dataTypeDelegate;
    }
}
