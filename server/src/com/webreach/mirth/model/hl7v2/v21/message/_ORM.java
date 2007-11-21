package com.webreach.mirth.model.hl7v2.v21.message;
import com.webreach.mirth.model.hl7v2.v21.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORM extends Message{	
	public _ORM(){
		segments = new Class[]{_MSH.class, _NTE.class, _PID.class, _NTE.class, _PV1.class, _ORC.class, _ORO.class, _OBR.class, _RX1.class, _NTE.class, _OBX.class, _NTE.class, _BLG.class};
		repeats = new int[]{0, -1, 0, -1, 0, 0, 0, 0, 0, -1, 0, -1, 0};
		required = new boolean[]{true, false, true, false, false, true, true, true, true, false, true, false, false};
		groups = new int[][]{{3, 5, 0, 0}, {7, 9, 1, 1}, {11, 12, 0, 1}, {7, 12, 0, 0}, {6, 13, 1, 1}}; 
		description = "Order Message";
		name = "ORM";
	}
}
