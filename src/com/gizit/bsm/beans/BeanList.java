package com.gizit.bsm.beans;

import java.util.ArrayList;

import com.gizit.bsm.generic.FieldSerializer;

public class BeanList<T> extends ArrayList<T> {

	private static final long serialVersionUID = 1L;

	public String Serialize() {
		return FieldSerializer.serializeFieldClassList(this).toString().replaceAll("BadField", "5").replaceAll("WarnField", "3");
	}
	
	public String SerializeWithoutLabels() {
		return FieldSerializer.serializeFieldClassListWithoutLabels(this).toString();
	}
	
}
