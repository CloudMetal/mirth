package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DFTP03 extends Message{	
	public _DFTP03(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _ROL.class, _PV1.class, _PV2.class, _ROL.class, _DB1.class, _ORC.class, _OBR.class, _NTE.class, _OBX.class, _NTE.class, _FT1.class, _PR1.class, _ROL.class, _ORC.class, _OBR.class, _NTE.class, _OBX.class, _NTE.class, _DG1.class, _DRG.class, _GT1.class, _IN1.class, _IN2.class, _IN3.class, _ROL.class, _ACC.class};
		repeats = new int[]{0, 0, 0, 0, -1, 0, 0, -1, -1, 0, 0, -1, 0, -1, 0, 0, -1, 0, 0, -1, 0, -1, -1, 0, -1, 0, 0, -1, -1, 0};
		required = new boolean[]{true, true, true, false, false, false, false, false, false, false, true, false, true, false, true, true, false, false, true, false, true, false, false, false, false, true, false, false, false, false};
		groups = new int[][]{{11, 12, 0, 0}, {13, 14, 0, 1}, {10, 14, 0, 1}, {16, 17, 0, 1}, {19, 20, 0, 0}, {21, 22, 0, 1}, {18, 22, 0, 1}, {15, 22, 1, 1}, {26, 29, 0, 1}}; 
		description = "Post Detail Financial Transaction";
		name = "DFTP03";
	}
}
