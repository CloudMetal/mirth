package com.webreach.mirth.client.ui.beans;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;

public class DICOMProperties extends Object implements Serializable {

    public static final String PROP_SAMPLE_PROPERTY = "sampleProperty";
    private PropertyChangeSupport propertySupport;

    public DICOMProperties() {
        propertySupport = new PropertyChangeSupport(this);
    }
    /**
     * Holds value of property includeGroupLength.
     */
    private boolean includeGroupLength = false;

    /**
     * Getter for property includeGroupLength.
     *
     * @return Value of property includeGroupLength.
     */
    public boolean isIncludeGroupLength() {
        return this.includeGroupLength;
    }

    /**
     * Setter for property includeGroupLength.
     *
     * @param includeGroupLength
     *            New value of property includeGroupLength.
     */
    public void setIncludeGroupLength(boolean includeGroupLength) {
        this.includeGroupLength = includeGroupLength;
    }
}
