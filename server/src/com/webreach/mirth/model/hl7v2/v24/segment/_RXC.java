package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RXC extends Segment {
	public _RXC(){
		fields = new Class[]{_ID.class, _CE.class, _NM.class, _CE.class, _NM.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"RX Component Type", "Component Code", "Component Amount", "Component Units", "Component Strength", "Component Strength Units", "Supplementary Code"};
		description = "Pharmacy/Treatment Component Order";
		name = "RXC";
	}
}
