package com.gizit.bsm.beans;

import com.gizit.bsm.generic.StringField;

public class RoleResourcePermBean extends RoleBean {
	
	public BeanList<ResPerm> ResourcePermissions = new BeanList<ResPerm>();
	
	public static class ResPerm extends Bean {
		public StringField resource_type_id = new StringField();
		public StringField resource_type = new StringField();
		public StringField permission_type_id = new StringField();
		public StringField permission_type = new StringField();
	}

}
