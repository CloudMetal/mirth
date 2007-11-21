package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RTBK13 extends Message{	
	public _RTBK13(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QAK.class, _QPD.class, _RDF.class, _RDT.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, -1, 0};
		required = new boolean[]{true, true, false, true, true, true, false, false};
		groups = new int[][]{{6, 7, 0, 0}}; 
		description = "Query by Parameter/Tabular Response";
		name = "RTBK13";
	}
}
