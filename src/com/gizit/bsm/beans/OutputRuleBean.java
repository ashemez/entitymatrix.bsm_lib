package com.gizit.bsm.beans;

import com.gizit.bsm.generic.StringField;
import com.gizit.bsm.generic.StringFieldArray;

public class OutputRuleBean extends Bean {

	public StringField id = new StringField();
	public StringField label = new StringField();
	public StringField type = new StringField();
	public StringField input = new StringField();
	public Values values = new Values();
	public StringFieldArray operators = new StringFieldArray();
	
	public OutputRuleBean () {
		input.set("select");
		type.set("integer");
		values.BadField.set("Bad");
		values.WarnField.set("Warn");
		values.GoodField.set("Good");
		operators.set(new String[] {"equal", "not_equal"});
	}
	
	public static class Values extends Bean {
		public StringField BadField = new StringField();
		public StringField WarnField = new StringField();
		public StringField GoodField = new StringField();
	}
	
	public String Serialize() {
		return super.Serialize().replaceAll("BadField", "5").replaceAll("WarnField", "3").replaceAll("GoodField", "0");
	}
}
