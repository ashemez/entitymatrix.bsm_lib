package com.gizit.bsm.beans;

import com.gizit.bsm.generic.IntField;
import com.gizit.bsm.generic.StringField;
import com.gizit.bsm.generic.BooleanField;

public class AltUserBean extends Bean{
	public StringField userid = new StringField();
	public StringField username = new StringField();
	public StringField password = new StringField();
	public StringField firstName = new StringField();
	public StringField lastName = new StringField();
	public BooleanField valid = new BooleanField();

	public BeanList<RoleMember> rolemembers = new BeanList<RoleMember>();
	public BeanList<GroupMember> groupmembers = new BeanList<GroupMember>();
	
	public static class RoleMember extends Bean {
		public IntField role_id = new IntField();
		public StringField role_name = new StringField();
	}
	
	public static class GroupMember extends Bean {
		public IntField group_id = new IntField();
		public StringField group_name = new StringField();
	}

}
