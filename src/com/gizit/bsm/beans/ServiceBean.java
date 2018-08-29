package com.gizit.bsm.beans;

import com.gizit.bsm.generic.IntField;
import com.gizit.bsm.generic.StringField;

public class ServiceBean extends Bean {
	public IntField service_instance_id = new IntField();
	public StringField service_instance_name = new StringField();
}
