package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RSPK25 extends Message{	
	public _RSPK25(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _ERR.class, _QAK.class, _QPD.class, _RCP.class, _STF.class, _PRA.class, _ORG.class, _AFF.class, _LAN.class, _EDU.class, _CER.class, _DSC.class};
		repeats = new int[]{0, -1, 0, -1, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 0};
		required = new boolean[]{true, false, true, false, true, true, true, true, false, false, false, false, false, false, false};
		groups = new int[][]{{8, 14, 1, 1}}; 
		description = "Personnel Information by Segment Response";
		name = "RSPK25";
	}
}
