package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _PPN extends Composite {
	public _PPN(){
		fields = new Class[]{_ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _IS.class, _HD.class, _ID.class, _ST.class, _ID.class, _IS.class, _HD.class, _TS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"ID Number", "Family Name", "Given Name", "Middle Initial or Name", "Suffix", "Prefix", "Degree", "Source Table", "Assigning Authority", "Name Type Code", "Identifier Check Digit", "Code Identifying Check Digit Scheme Employed", "Identifier Type Code", "Assigning Facility", "Datetime Action Performed"};
		description = "Performing Person Time Stamp";
		name = "PPN";
	}
}
