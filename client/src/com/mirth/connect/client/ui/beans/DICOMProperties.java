/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.beans;

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
