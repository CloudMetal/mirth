package com.webreach.mirth.model.dicom;

import org.dcm4che2.data.ElementDictionary;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Aug 6, 2007
 * Time: 3:04:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMReference {
    private static DICOMReference instance = null;
    private ElementDictionary elementDictionary = null;
    private DICOMReference(){
        elementDictionary = ElementDictionary.getDictionary();
    }
    public String getDescription(String key, String version)
    {
        if(key != null && !key.equals("")){
            try {
                return elementDictionary.nameOf(Integer.decode("0x"+key).intValue());
            }
            catch(NumberFormatException e){
                return "";
            }
        }
        return "";
    }
    public static DICOMReference getInstance()
    {
        synchronized (DICOMReference.class)
        {
            if (instance == null)
                instance = new DICOMReference();
            return instance;
        }
    }
}
