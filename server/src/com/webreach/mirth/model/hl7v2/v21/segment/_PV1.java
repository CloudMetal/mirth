package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PV1 extends Segment {
	public _PV1(){
		fields = new Class[]{_SI.class, _ID.class, _ID.class, _ID.class, _ST.class, _ID.class, _CN.class, _CN.class, _CN.class, _ID.class, _ID.class, _ID.class, _ID.class, _ID.class, _ID.class, _ID.class, _CN.class, _ID.class, _NM.class, _ID.class, _ID.class, _ID.class, _ID.class, _ID.class, _DT.class, _NM.class, _NM.class, _ID.class, _ID.class, _DT.class, _ST.class, _NM.class, _NM.class, _ID.class, _DT.class, _ID.class, _ID.class, _ID.class, _ID.class, _ID.class, _ID.class, _ID.class, _ID.class, _TS.class, _TS.class, _NM.class, _NM.class, _NM.class, _NM.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Patient Class", "Assigned Patient Location", "Admission Type", "Pre-Admit Number", "Prior Patient Location", "Attending Doctor", "Refering Doctor", "Consulting Doctor", "Hospital Service", "Temporary Location", "Pre-Admit Test Indicator", "Re-Admission Indicator", "Admit Source", "Ambulatory Status", "Vip Indicators", "Admitting Doctor", "Patient Type", "Visit Number", "Financial Class", "Charge Price Indicator", "Courtesy Code", "Credit Rating", "Contract Code", "Contract Effective Date", "Contract Amount", "Contract Period", "Interest Code", "Transfer to Bad Debt Code", "Transfer to Bad Debt Date", "Bad Debt Agency Code", "Bad Debt Transfer Amount", "Bad Debt Recovery Amount", "Delete Account Indicator", "Delete Account Date", "Discharge Disposition", "Discharged to Location", "Diet Type", "Servicing Facility", "Bed Status", "Account Status", "Pending Location", "Prior Temporary Location", "Admit Date/Time", "Discharge Date/Time", "Current Patient Balance", "Total Charges", "Total Adjustments", "Total Payments"};
		description = "Patient Visit";
		name = "PV1";
	}
}
