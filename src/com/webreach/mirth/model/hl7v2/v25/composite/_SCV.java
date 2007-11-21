package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _SCV extends Composite {
	public _SCV(){
		fields = new Class[]{_CWE.class, _ST.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Parameter Class", "Parameter Value"};
		description = "Scheduling Class Value Pair";
		name = "SCV";
	}
}
