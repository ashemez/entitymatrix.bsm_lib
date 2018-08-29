package com.gizit.bsm.beans;

import com.gizit.bsm.generic.IntField;
import com.gizit.bsm.generic.StringField;

public class KPIBean extends Bean {

	public StringField kpiname = new StringField();
	public StringField service_instance_name = new StringField();
	public StringField lastMeasuredValue = new StringField();
	public StringField status = new StringField();
	public StringField lastran = new StringField();
	public StringField kpiid = new StringField();
	public StringField sid = new StringField();
	public StringField kpiquery = new StringField();
	public StringField interval = new StringField();
	public StringField timeunit = new StringField();
	public StringField thrsmarrule = new StringField();
	public StringField thrsbadrule = new StringField();
	public StringField dsid = new StringField();
	public StringField isrunning = new StringField();
	public StringField dsname = new StringField();
	public StringField createdby = new StringField();
}
