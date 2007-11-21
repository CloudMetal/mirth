package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RDRRDR extends Message{	
	public _RDRRDR(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QRD.class, _QRF.class, _PID.class, _NTE.class, _ORC.class, _RXE.class, _RXR.class, _RXC.class, _RXD.class, _RXC.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, -1, 0, 0, -1, -1, 0, -1, 0};
		required = new boolean[]{true, true, false, true, false, true, false, true, true, true, false, true, false, false};
		groups = new int[][]{{6, 7, 0, 0}, {9, 11, 0, 0}, {12, 13, 1, 1}, {8, 13, 1, 1}, {4, 13, 1, 1}}; 
		description = "Pharmacy Dispense  Information Query Response";
		name = "RDRRDR";
	}
}
