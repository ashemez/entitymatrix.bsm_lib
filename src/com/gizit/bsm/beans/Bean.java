package com.gizit.bsm.beans;

import com.gizit.bsm.generic.FieldSerializer;

public abstract class Bean {

	public String Serialize() {
		return FieldSerializer.serializeFieldClass(this).toString();
	}
	
	public String SerializeWithoutLabel() {
		return FieldSerializer.serializeFieldClassWithoutLabels(this).toString();
	}

}
