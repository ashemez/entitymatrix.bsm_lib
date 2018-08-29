package com.gizit.bsm.beans;

import com.gizit.bsm.generic.StringField;

public class RoleSrvPermBean extends RoleBean {
	
	public BeanList<SrvPerm> ServicePermissions = new BeanList<SrvPerm>();
	
	public static class SrvPerm extends Bean {
		public StringField service_instance_id = new StringField();
		public StringField service_instance_name = new StringField();
		public StringField permission_type_id = new StringField();
		public StringField permission_type = new StringField();
	}

}
