package com.gizit.bsm.beans;

import com.gizit.bsm.generic.StringField;
import com.gizit.bsm.generic.StringFieldArray;

public class KPIRuleBean extends Bean {

	public StringField id = new StringField();
	public StringField label = new StringField();
	public StringField type = new StringField();
	public StringField input = new StringField();
	public StringFieldArray operators = new StringFieldArray();
	
	public KPIRuleBean () {
		input.set("select");
		type.set("integer");
		operators.set(new String[] {"equal", "not_equal"});
	}

}
