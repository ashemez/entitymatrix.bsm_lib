package com.gizit.bsm.generic;

import com.gizit.bsm.generic.Field;
import com.google.gson.JsonArray;

public class StringFieldArray extends Field<String[]>{

	public JsonArray toJsonArray() {
		JsonArray ja = new JsonArray();
		for(int i=0; i<this.get().length; i++) {
			ja.add(this.get()[i]);
		}
		
		return ja;
	}
	
}
