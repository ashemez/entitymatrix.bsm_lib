package com.gizit.bsm.beans;

import com.gizit.bsm.generic.IntField;
import com.gizit.bsm.generic.StringField;

public class AlarmBean extends Bean {
	
	public IntField ServerSerial = new IntField();
	public StringField Manager = new StringField();
	public StringField Node = new StringField();
	public StringField Summary = new StringField();
	public StringField Severity = new StringField();
	public StringField FirstOccurrence = new StringField();
	public StringField LastOccurrence = new StringField();
	public StringField BSM_Identity = new StringField();

}
