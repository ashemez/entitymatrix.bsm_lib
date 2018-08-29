package com.gizit.bsm.beans;

import com.gizit.bsm.generic.DoubleField;
import com.gizit.bsm.generic.IntField;
import com.gizit.bsm.generic.StringField;

public class OutageBean extends Bean {
	public IntField outageStart = new IntField();
	public IntField outageEnd = new IntField();
	public StringField outageStartStr = new StringField();
	public StringField outageEndStr = new StringField();
	public IntField service_instance_id = new IntField();
	public StringField service_instance_name = new StringField();
	public DoubleField outageDuration = new DoubleField();
}
