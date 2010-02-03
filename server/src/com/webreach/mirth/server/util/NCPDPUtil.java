/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.util;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Aug 2, 2007
 * Time: 12:26:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class NCPDPUtil {

    public static String formatNCPDPNumber(String origNumber, int decimalPoints){
        if(origNumber == null || origNumber.equals("")){
            return "";
        }
        if(origNumber.endsWith("{")){
            origNumber = origNumber.replace("{","0");
        }
        else if (origNumber.endsWith("A")){
            origNumber = origNumber.replace("A","1");
        }
        else if(origNumber.endsWith("B")){
            origNumber = origNumber.replace("B","2");
        }
        else if(origNumber.endsWith("C")){
            origNumber = origNumber.replace("C","3");
        }
        else if(origNumber.endsWith("D")){
            origNumber = origNumber.replace("D","4");
        }
        else if(origNumber.endsWith("E")){
            origNumber = origNumber.replace("E","5");
        }
        else if(origNumber.endsWith("F")){
            origNumber = origNumber.replace("F","6");
        }
        else if(origNumber.endsWith("G")){
            origNumber = origNumber.replace("G","7");
        }
        else if(origNumber.endsWith("H")){
            origNumber = origNumber.replace("H","8");
        }
        else if(origNumber.endsWith("I")){
            origNumber = origNumber.replace("I","9");
        }
        else if(origNumber.endsWith("}")){
            origNumber = origNumber.replace("}","0");
            origNumber = "-" + origNumber;
        }
        else if(origNumber.endsWith("J")){
            origNumber = origNumber.replace("J","1");
            origNumber = "-" + origNumber;
        }
        else if(origNumber.endsWith("K")){
            origNumber = origNumber.replace("K","2");
            origNumber = "-" + origNumber;
        }
        else if(origNumber.endsWith("L")){
            origNumber = origNumber.replace("L","3");
            origNumber = "-" + origNumber;
        }
        else if(origNumber.endsWith("M")){
            origNumber = origNumber.replace("M","4");
            origNumber = "-" + origNumber;
        }
        else if(origNumber.endsWith("N")){
            origNumber = origNumber.replace("N","5");
            origNumber = "-" + origNumber;
        }
        else if(origNumber.endsWith("O")){
            origNumber = origNumber.replace("O","6");
            origNumber = "-" + origNumber;
        }
        else if(origNumber.endsWith("P")){
            origNumber = origNumber.replace("P","7");
            origNumber = "-" + origNumber;
        }
        else if(origNumber.endsWith("Q")){
            origNumber = origNumber.replace("Q","8");
            origNumber = "-" + origNumber;
        }
        else if(origNumber.endsWith("R")){
            origNumber = origNumber.replace("R","9");
            origNumber = "-" + origNumber;
        }

        if(decimalPoints > 0 && decimalPoints < origNumber.length()) {
            return origNumber.substring(0, origNumber.length()-decimalPoints) + "." + origNumber.substring(origNumber.length()-decimalPoints);
        }
        return origNumber;
    }

}
