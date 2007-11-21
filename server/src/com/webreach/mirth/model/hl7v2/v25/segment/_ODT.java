package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ODT extends Segment {
	public _ODT(){
		fields = new Class[]{_CE.class, _CE.class, _ST.class};
		repeats = new int[]{0, -1, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Tray Type", "Service Period", "Text Instruction"};
		description = "Diet Tray Instructions";
		name = "ODT";
	}
}
