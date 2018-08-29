package com.gizit.bsm.beans;

import java.util.ArrayList;

import com.gizit.bsm.beans.NodeGroupBean.Member;
import com.gizit.bsm.generic.DoubleField;
import com.gizit.bsm.generic.IntField;
import com.gizit.bsm.generic.StringField;

public class NodeGroupBean extends Bean {

	public IntField parentSid = new IntField();
	public IntField node_group_id = new IntField();
	public StringField group_name = new StringField();
	public DoubleField bad_weight = new DoubleField();
	public BeanList<Member> members = new BeanList<Member>();
	
	public static NodeGroupBean CreateBean(String groupName, double badweight, int[] members, int parentSid) {
		NodeGroupBean ngBean = new NodeGroupBean();
		ngBean.group_name.set(groupName);
		ngBean.bad_weight.set(badweight);
		ngBean.parentSid.set(parentSid);
		for(int sid : members) {
			Member m = new Member();
			m.sid.set(sid);
			m.servicename.set("");
			ngBean.members.add(m);
		}
		return ngBean;
    }

	public String GetMemberSidsAsStringList() {
		String memberList = "";
		if(this.members.size() > 0)
        {
        	int cnt = 0;
        	while(cnt < this.members.size()) {
        		if(cnt > 0)
	        		memberList += ",";
        		memberList += this.members.get(cnt).sid.get();

        		cnt++;
        	}
        }
		return memberList;
	}
	
	public static class Member extends Bean {
		public IntField sid = new IntField();
		public StringField servicename = new StringField();
	}

}
