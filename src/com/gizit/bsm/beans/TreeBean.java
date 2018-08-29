package com.gizit.bsm.beans;

import java.util.ArrayList;

import com.gizit.bsm.generic.StringField;

public class TreeBean extends Bean {
	
	public StringField name = new StringField();
	public StringField sid = new StringField();
	public StringField status = new StringField();
	public StringField citype = new StringField();
	public StringField consistent = new StringField();
	public StringField badbad = new StringField();
	public StringField badmarginal = new StringField();
	public StringField marginalbad = new StringField();
	public StringField marginalmarginal = new StringField();
	public StringField compmodel = new StringField();
	public StringField source_ci_id = new StringField();
	public StringField hasChild = new StringField();
	public ArrayList<TreeBean> children = new ArrayList<TreeBean>();

}
