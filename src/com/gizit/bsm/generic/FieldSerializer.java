package com.gizit.bsm.generic;

import java.util.ArrayList;
import java.util.HashMap;

import com.gizit.bsm.beans.BeanList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class FieldSerializer {

    public static <T, S> JsonElement serializeFieldClass(T src) {
        JsonObject jsonObj = new JsonObject();
        
        Class<?> objClass = src.getClass();

        Gson gson = new Gson();
        
        try {
	        for(java.lang.reflect.Field field : objClass.getFields()) {
	            String name = field.getName();
	            if(field.getType() == IntField.class || field.getType().getComponentType() == IntField.class) {
	            	IntField value = (IntField)field.get(src);
	            	jsonObj.addProperty(name, value.get());
	            }
	            if(field.getType() == DoubleField.class || field.getType().getComponentType() == DoubleField.class) {
	            	DoubleField value = (DoubleField)field.get(src);
	            	jsonObj.addProperty(name, value.get());
	            }
	            else if(field.getType() == StringFieldArray.class || field.getType().getComponentType() == StringFieldArray.class) {
	            	StringFieldArray value = (StringFieldArray)field.get(src);
	            	jsonObj.add(name, value.toJsonArray());
	            }
	            else if(field.getType() == StringField.class || field.getType().getComponentType() == StringField.class) {
	            	StringField value = (StringField)field.get(src);
	            	jsonObj.addProperty(name, value.get());
	            }
	            else if(field.getType().getGenericSuperclass() == com.gizit.bsm.beans.Bean.class) {
	            	com.gizit.bsm.beans.Bean value = (com.gizit.bsm.beans.Bean)field.get(src);
	            	jsonObj.add(name, serializeFieldClass(value));
	            }
	            else if(field.getType() == ArrayList.class || field.getType() == BeanList.class) {
	            	ArrayList<T> value = (ArrayList<T>)field.get(src);
	            	jsonObj.add(name, serializeFieldClassList(value));
	            }
        	}
        } catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return jsonObj;
    }
    
    public static <T> JsonElement serializeFieldClassList(ArrayList<T> src) {
        JsonArray JA = new JsonArray();

        if(src.size() > 0) {
        	int i = 0;
        	while(i < src.size()) {
        		JA.add(serializeFieldClass(src.get(i)));
        		i++;
        	}
        }
        return JA;
    }
    
    public static <T> JsonElement serializeFieldClassWithoutLabels(T src) {
    	JsonArray JA = new JsonArray();
        
        Class<?> objClass = src.getClass();

        try {
	        for(java.lang.reflect.Field field : objClass.getFields()) {	            
	            if(field.getType() == IntField.class || field.getType().getComponentType() == IntField.class) {
	            	IntField value = (IntField)field.get(src);
	            	JA.add(value.get());
	            }
	            if(field.getType() == DoubleField.class || field.getType().getComponentType() == DoubleField.class) {
	            	DoubleField value = (DoubleField)field.get(src);
	            	JA.add(value.get());
	            }
	            else if(field.getType() == StringFieldArray.class || field.getType().getComponentType() == StringFieldArray.class) {
	            	StringFieldArray value = (StringFieldArray)field.get(src);
	            	JA.add(value.toJsonArray());
	            }
	            else if(field.getType() == StringField.class || field.getType().getComponentType() == StringField.class) {
	            	StringField value = (StringField)field.get(src);
	            	JA.add(value.get());
	            }
	            else if(field.getType().getGenericSuperclass() == com.gizit.bsm.beans.Bean.class) {
	            	com.gizit.bsm.beans.Bean value = (com.gizit.bsm.beans.Bean)field.get(src);
	            	JA.add(serializeFieldClass(value));
	            }
	            else if(field.getType() == ArrayList.class || field.getType() == BeanList.class) {
	            	ArrayList<T> value = (ArrayList<T>)field.get(src);
	            	JA.add(serializeFieldClassListWithoutLabels(value));
	            }
	        }
        } catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return JA;
    }
    
    public static <T> JsonElement serializeFieldClassListWithoutLabels(ArrayList<T> src) {
        JsonArray JA = new JsonArray();

        if(src.size() > 0) {
        	int i = 0;
        	while(i < src.size()) {
        		JA.add(serializeFieldClassWithoutLabels(src.get(i)));
        		i++;
        	}
        }
        return JA;
    }
    
}