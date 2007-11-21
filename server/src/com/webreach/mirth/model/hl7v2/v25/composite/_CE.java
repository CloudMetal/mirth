package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CE extends Composite {
	public _CE(){
		fields = new Class[]{_ST.class, _ST.class, _ID.class, _ST.class, _ST.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Identifier", "Text", "Name of Coding System", "Alternate Identifier", "Alternate Text", "Name of Alternate Coding System"};
		description = "Coded Element";
		name = "CE";
	}
}
