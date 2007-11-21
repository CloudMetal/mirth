package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PR1 extends Segment {
	public _PR1(){
		fields = new Class[]{_SI.class, _IS.class, _CE.class, _ST.class, _TS.class, _IS.class, _NM.class, _XCN.class, _IS.class, _NM.class, _XCN.class, _XCN.class, _CE.class, _NM.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Procedure Coding Method", "Procedure Code", "Procedure Description", "Procedure Date/Time", "Procedure Functional Type", "Procedure Minutes", "Anesthesiologist", "Anesthesia Code", "Anesthesia Minutes", "Surgeon", "Procedure Practitioner", "Consent Code", "Procedure Priority", "Associated Diagnosis Code"};
		description = "Procedures";
		name = "PR1";
	}
}
