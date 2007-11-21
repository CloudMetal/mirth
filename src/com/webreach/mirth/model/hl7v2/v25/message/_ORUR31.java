package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORUR31 extends Message{	
	public _ORUR31(){
		segments = new Class[]{_MSH.class, _SFT.class, _PID.class, _PD1.class, _PV1.class, _PV2.class, _ORC.class, _OBR.class, _NTE.class, _TQ1.class, _TQ2.class, _OBX.class, _NTE.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0, 0, -1, 0, -1, 0, -1};
		required = new boolean[]{true, false, true, false, true, false, true, true, false, true, false, true, false};
		groups = new int[][]{{5, 6, 0, 0}, {10, 11, 0, 1}, {12, 13, 1, 1}}; 
		description = "Unsolicited New Point-Of-Care Observation Message - Search For An Order";
		name = "ORUR31";
	}
}
