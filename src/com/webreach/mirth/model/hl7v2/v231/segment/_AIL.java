package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _AIL extends Segment {
	public _AIL(){
		fields = new Class[]{_SI.class, _ID.class, _PL.class, _CE.class, _CE.class, _TS.class, _NM.class, _CE.class, _NM.class, _CE.class, _IS.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Segment Action Code", "Location Resource ID", "Location Type", "Location Group", "Start Date/Time", "Start Date/Time Offset", "Start Date/Time Offset Units", "Duration", "Duration Units", "Allow Substitution Code", "Filler Status Code"};
		description = "Appointment Information - Location Resource";
		name = "AIL";
	}
}
