package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _NR extends Composite {
	public _NR(){
		fields = new Class[]{_NM.class, _NM.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Low Value", "High Value"};
		description = "Wertebereich";
		name = "NR";
	}
}
