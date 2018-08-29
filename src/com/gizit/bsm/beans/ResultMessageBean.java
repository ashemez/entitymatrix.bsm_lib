package com.gizit.bsm.beans;

import com.gizit.bsm.generic.StringField;

public class ResultMessageBean extends Bean {
	public StringField state = new StringField();
	public StringField message = new StringField();
	public StringField operation = new StringField();
	
	public void Reset() {
		this.state.set("");
		this.message.set("");
		this.operation.set("");
	}
}
