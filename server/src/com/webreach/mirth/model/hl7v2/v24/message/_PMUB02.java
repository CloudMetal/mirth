package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PMUB02 extends Message{	
	public _PMUB02(){
		segments = new Class[]{_MSH.class, _EVN.class, _STF.class, _PRA.class, _ORG.class, _AFF.class, _LAN.class, _EDU.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, false, false, false, false, false};
		groups = new int[][]{}; 
		description = "Update Personnel Record";
		name = "PMUB02";
	}
}
