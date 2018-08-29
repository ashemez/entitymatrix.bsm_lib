package com.gizit.bsm.beans;

import com.gizit.bsm.generic.IntField;
import com.gizit.bsm.generic.StringField;

public class ReportDefBean extends Bean {
	public IntField report_id = new IntField();
	public StringField report_name = new StringField();
	public IntField service_instance_id = new IntField();
	public StringField service_instance_name = new StringField();
	public IntField startDate = new IntField();
	public IntField endDate = new IntField();
	public StringField startDateStr = new StringField();
	public StringField endDateStr = new StringField();
	public StringField queryType = new StringField();
	public StringField createdby = new StringField();
}
