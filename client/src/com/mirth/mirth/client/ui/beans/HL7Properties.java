/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.client.ui.beans;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;

public class HL7Properties implements Serializable {

    public static final String PROP_SAMPLE_PROPERTY = "sampleProperty";
    private PropertyChangeSupport propertySupport;

    public HL7Properties() {
        propertySupport = new PropertyChangeSupport(this);
    }
    /**
     * Holds value of property useStrictParser.
     */
    private boolean useStrictParser = false;

    /**
     * Getter for property validateMessage.
     * @return Value of property validateMessage.
     */
    public boolean isUseStrictParser() {
        return this.useStrictParser;
    }

    /**
     * Setter for property validateMessage.
     * @param validateMessage New value of property validateMessage.
     */
    public void setUseStrictParser(boolean useStrictParser) {
        this.useStrictParser = useStrictParser;
    }
    /**
     * Holds value of property useStrictValidation.
     */
    private boolean useStrictValidation = false;

    /**
     * Getter for property useStrictValidation.
     * @return Value of property useStrictValidation.
     */
    public boolean isUseStrictValidation() {
        return this.useStrictValidation;
    }

    /**
     * Setter for property useStrictValidation.
     * @param useStrictValidation New value of property useStrictValidation.
     */
    public void setUseStrictValidation(boolean useStrictValidation) {
        this.useStrictValidation = useStrictValidation;
    }
    /**
     * Holds value of property stripNamespaces.
     */
    private boolean stripNamespaces = true;

    /**
     * Getter for property stripNamespaces.
     * @return Value of property stripNamespaces.
     */
    public boolean isStripNamespaces() {
        return this.stripNamespaces;
    }

    /**
     * Setter for property stripNamespaces.
     * @param stripNamespaces New value of property stripNamespaces.
     */
    public void setStripNamespaces(boolean stripNamespaces) {
        this.stripNamespaces = stripNamespaces;
    }
    /**
     * Holds value of property handleRepetitions.
     */
    private boolean handleRepetitions = false;

    /**
     * Getter for property handleRepetitions.
     * @return Value of property handleRepetitions.
     */
    public boolean isHandleRepetitions() {
        return this.handleRepetitions;
    }

    /**
     * Setter for property handleRepetitions.
     * @param handleRepetitions New value of property handleRepetitions.
     */
    public void setHandleRepetitions(boolean handleRepetitions) {
        this.handleRepetitions = handleRepetitions;
    }
    /**
     * Holds value of property convertLFtoCR.
     */
    private boolean convertLFtoCR = true;

    /**
     * Getter for property convertLFtoCR.
     * @return Value of property convertLFtoCR.
     */
    public boolean isConvertLFtoCR() {
        return this.convertLFtoCR;
    }

    /**
     * Setter for property convertLFtoCR.
     * @param convertLFtoCR New value of property convertLFtoCR.
     */
    public void setConvertLFtoCR(boolean convertLFtoCR) {
        this.convertLFtoCR = convertLFtoCR;
    }
    /**
     * Holds value of property handleSubcomponents.
     */
    private boolean handleSubcomponents = false;

    /**
     * Getter for property handleSubcomponents.
     * @return Value of property handleSubcomponents.
     */
    public boolean isHandleSubcomponents() {
        return this.handleSubcomponents;
    }

    /**
     * Setter for property handleSubcomponents.
     * @param handleSubcomponents New value of property handleSubcomponents.
     */
    public void setHandleSubcomponents(boolean handleSubcomponents) {
        this.handleSubcomponents = handleSubcomponents;
    }

}
