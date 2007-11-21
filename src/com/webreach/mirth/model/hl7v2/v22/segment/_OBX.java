package com.webreach.mirth.model.hl7v2.v22.segment;
import com.webreach.mirth.model.hl7v2.v22.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OBX extends Segment {
	public _OBX(){
		fields = new Class[]{_SI.class, _ID.class, _CE.class, _ST.class, _ST.class, _CE.class, _ST.class, _ID.class, _NM.class, _ID.class, _ID.class, _TS.class, _ST.class, _TS.class, _CE.class, _CN.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Value Type", "Observation Identifier", "Observation Sub-ID", "Observation Value", "Units", "Reference Range", "Abnormal Flag", "Probability", "0", "1", "2", "3", "4", "5", "6"};
		description = "Observation/Result";
		name = "OBX";
	}
}
