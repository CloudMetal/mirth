package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA37 extends Message{	
	public _ADTA37(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _PV1.class, _DB1.class, _PID.class, _PD1.class, _PV1.class, _DB1.class};
		repeats = new int[]{0, 0, 0, 0, 0, -1, 0, 0, 0, -1};
		required = new boolean[]{true, true, true, false, false, false, true, false, false, false};
		groups = new int[][]{}; 
		description = "Unlink Patient Information";
		name = "ADTA37";
	}
}
