package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PR1 extends Segment {
	public _PR1(){
		fields = new Class[]{_SI.class, _IS.class, _CE.class, _ST.class, _TS.class, _IS.class, _NM.class, _XCN.class, _IS.class, _NM.class, _XCN.class, _XCN.class, _CE.class, _ID.class, _CE.class, _CE.class, _IS.class, _CE.class, _EI.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, -1, -1, 0, 0, 0, -1, 0, -1, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Procedure Coding Method", "Procedure Code", "Procedure Description", "Procedure Date/Time", "Procedure Functional Type", "Procedure Minutes", "Anesthesiologist", "Anesthesia Code", "Anesthesia Minutes", "Surgeon", "Procedure Practitioner", "Consent Code", "Procedure Priority", "Associated Diagnosis Code", "Procedure Code Modifier", "Procedure Drg Type", "Tissue Type Code", "Procedure Identifier", "Procedure Action Code"};
		description = "Procedures";
		name = "PR1";
	}
}
