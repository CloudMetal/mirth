package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _EQP extends Segment {
	public _EQP(){
		fields = new Class[]{_CE.class, _ST.class, _TS.class, _TS.class, _FT.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Event type", "File Name", "Start Date/Time", "End Date/Time", "Transaction Data"};
		description = "Equipment/Log Service";
		name = "EQP";
	}
}
