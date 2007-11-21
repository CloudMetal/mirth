package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _AUI extends Composite {
	public _AUI(){
		fields = new Class[]{_ST.class, _DT.class, _ST.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Authorization Number", "Date", "Source"};
		description = "Authorization Information";
		name = "AUI";
	}
}
