package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SURP09 extends Message{	
	public _SURP09(){
		segments = new Class[]{_MSH.class, _FAC.class, _PSH.class, _PDC.class, _PSH.class, _FAC.class, _PDC.class, _NTE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, true, true, true, true, true};
		groups = new int[][]{{3, 4, 1, 1}, {6, 8, 1, 1}, {2, 8, 1, 1}}; 
		description = "Summary Product Experience Report";
		name = "SURP09";
	}
}
