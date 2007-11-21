package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OBX extends Segment {
	public _OBX(){
		fields = new Class[]{_SI.class, _ID.class, _CE.class, _ST.class, _ST.class, _CE.class, _ST.class, _IS.class, _NM.class, _ID.class, _ID.class, _TS.class, _ST.class, _TS.class, _CE.class, _XCN.class, _CE.class, _EI.class, _TS.class};
		repeats = new int[]{0, 0, 0, 0, -1, 0, 0, -1, 0, -1, 0, 0, 0, 0, 0, -1, -1, -1, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Value Type", "Observation Identifier", "Observation Sub-id", "Observation Value", "Units", "References Range", "Abnormal Flags", "Probability", "Nature of Abnormal Test", "Observation Result Status", "Effective Date of Reference Range", "User Defined Access Checks", "Date/Time of the Observation", "Producer's ID", "Responsible Observer", "Observation Method", "Equipment Instance Identifier", "Date/Time of the Analysis"};
		description = "Observation/Result";
		name = "OBX";
	}
}
