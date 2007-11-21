package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CN extends Composite {
	public _CN(){
		fields = new Class[]{_ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _IS.class, _IS.class, _HD.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"ID Number (ST)", "Family Name", "Given Name", "Middle Initial or Name", "Suffix", "Prefix", "Degree", "Source Table", "Assigning Authority"};
		description = "Composite ID Number and Name";
		name = "CN";
	}
}
