package com.webreach.mirth.model.hl7v2.v22.message;
import com.webreach.mirth.model.hl7v2.v22.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA23 extends Message{	
	public _ADTA23(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PV1.class, _PV2.class, _OBX.class};
		repeats = new int[]{0, 0, 0, 0, 0, -1};
		required = new boolean[]{true, true, true, true, false, false};
		groups = new int[][]{}; 
		description = "Delete A Patient Record";
		name = "ADTA23";
	}
}
