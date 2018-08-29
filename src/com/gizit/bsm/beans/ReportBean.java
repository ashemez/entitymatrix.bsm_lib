package com.gizit.bsm.beans;

import com.gizit.bsm.generic.DoubleField;
import com.gizit.bsm.generic.IntField;
import com.gizit.bsm.generic.StringField;

public class ReportBean extends Bean {
	public IntField startDate = new IntField();
	public IntField endDate = new IntField();
	public StringField queryType = new StringField();
	public StringField startDateStr = new StringField();
	public StringField endDateStr = new StringField();
	public IntField service_instance_id = new IntField();
	public StringField service_instance_name = new StringField();
	public DoubleField outageDuration = new DoubleField();
	public DoubleField availabilityMetric = new DoubleField();
	public IntField currentStatus = new IntField();
	public BeanList<OutageBean> Outages = new BeanList<OutageBean>();
	public StringField citype = new StringField();
}
