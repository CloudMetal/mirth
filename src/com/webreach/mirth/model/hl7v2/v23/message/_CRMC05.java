package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _CRMC05 extends Message{	
	public _CRMC05(){
		segments = new Class[]{_MSH.class, _PID.class, _PV1.class, _CSR.class, _CSP.class};
		repeats = new int[]{0, 0, 0, 0, -1};
		required = new boolean[]{true, true, false, true, false};
		groups = new int[][]{{2, 5, 1, 1}}; 
		description = "Patient Enters Phase of Clinical Trial";
		name = "CRMC05";
	}
}
