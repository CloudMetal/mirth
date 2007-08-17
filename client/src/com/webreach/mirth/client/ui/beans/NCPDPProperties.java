/*
 * EDIProperties.java
 *
 * Created on February 15, 2007, 6:12 PM
 */

package com.webreach.mirth.client.ui.beans;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * @author brendanh
 */
public class NCPDPProperties extends Object implements Serializable
{

    public static final String PROP_SAMPLE_PROPERTY = "sampleProperty";

    private PropertyChangeSupport propertySupport;

    public NCPDPProperties()
    {
        propertySupport = new PropertyChangeSupport(this);
    }

    /**
     * Holds value of property segmentDelimiter.
     */
    private String segmentDelimiter = "0x1E";

    /**
     * Getter for property segmentDelimeter.
     * 
     * @return Value of property segmentDelimeter.
     */
    public String getSegmentDelimiter()
    {
        return this.segmentDelimiter;
    }

    /**
     * Setter for property segmentDelimeter.
     * 
     * @param segmentDelimiter
     *            New value of property segmentDelimeter.
     */
    public void setSegmentDelimiter(String segmentDelimiter)
    {
        this.segmentDelimiter = segmentDelimiter;
    }

    /**
     * Holds value of property groupDelimiter.
     */
    private String groupDelimiter = "0x1D";

    /**
     * Getter for property elementDelimiter.
     * 
     * @return Value of property elementDelimiter.
     */
    public String getGroupDelimiter()
    {
        return this.groupDelimiter;
    }

    /**
     * Setter for property elementDelimiter.
     * 
     * @param groupDelimiter
     *            New value of property elementDelimiter.
     */
    public void setGroupDelimiter(String groupDelimiter)
    {
        this.groupDelimiter = groupDelimiter;
    }

    /**
     * Holds value of property fieldDelimiter.
     */
    private String fieldDelimiter = "0x1C";

    /**
     * Getter for property subelementDelimiter.
     * 
     * @return Value of property subelementDelimiter.
     */
    public String getFieldDelimiter()
    {
        return this.fieldDelimiter;
    }

    /**
     * Setter for property subelementDelimiter.
     * 
     * @param fieldDelimiter
     *            New value of property subelementDelimiter.
     */
    public void setFieldDelimiter(String fieldDelimiter)
    {
        this.fieldDelimiter = fieldDelimiter;
    }


}
