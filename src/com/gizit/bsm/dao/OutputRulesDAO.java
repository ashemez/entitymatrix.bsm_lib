package com.gizit.bsm.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.gizit.bsm.beans.BeanList;
import com.gizit.bsm.beans.NodeGroupBean;
import com.gizit.bsm.beans.NodeGroupBean.Member;
import com.gizit.bsm.beans.OutputRuleBean;
import com.gizit.bsm.beans.ResultMessageBean;
import com.gizit.bsm.helpers.Helper;
import com.gizit.managers.ConnectionManager;
import com.gizit.managers.LogManager;
import com.gizit.managers.ResourceManager;

public class OutputRulesDAO extends DAO {

	public OutputRulesDAO() {
		super(OutputRulesDAO.class);
	}

	public String GetNodeGroupsAndNonGroupedNodesOfCurrentFunctionalGroup(int parentSid)
    {
    	LOG.resultBean.Reset();
    	
    	BeanList<OutputRuleBean> orBeanList = new BeanList<OutputRuleBean>();

        try {
			OpenConn();
	        String q = "select i.service_instance_id, i.service_instance_name, g.group_name, g.node_group_id, g.bad_weight from smdbadmin.service_instances i" + 
	        		" left join smdbadmin.service_instance_relations r on(r.service_instance_id=i.service_instance_id)" + 
	        		" left join smdbadmin.node_group g on(g.node_group_id=r.node_group_id)" + 
	        		" where r.parent_instance_id=?";
	        q += " order by g.group_name asc, i.service_instance_name asc";
	        PreparedStatement st = conn.prepareStatement(q);
	        st.setInt(1, parentSid);
	        ResultSet rs = st.executeQuery();

	        HashMap<Integer, Integer> usedGroupIdList = new HashMap<Integer, Integer>();
	        HashMap<Integer, Integer> usedSidList = new HashMap<Integer, Integer>();

	        while(rs.next()) {
	        	OutputRuleBean orBean = new OutputRuleBean();
	        	
	        	int groupId = rs.getInt(4);
	        	int sid = rs.getInt(1);

	        	String groupName = "none";
	        	String serviceName = rs.getString(2);

	        	if(groupId == 0) {
	        		
	        		if(!usedSidList.containsKey(sid))
	        		{
	        			usedSidList.put(sid, 1);
	        			
	        			orBean.id.set("s_" + sid);
		        		orBean.label.set(serviceName);
		        		orBeanList.add(orBean);
	        		}
	        	}
	        	else {
	        		if(!usedGroupIdList.containsKey(groupId))
	        		{
	        			usedGroupIdList.put(groupId, 1);
	        			
	        			groupName = rs.getString(3);
	        			
	        			orBean.id.set("g_" + groupId);
		        		orBean.label.set(groupName);
		        		orBeanList.add(orBean);
	        		}
	        	}
	        }
	        
	        rs.close();
	        st.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS"));
			
		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        
        return orBeanList.Serialize();
    }

	public String SaveOutputRule(int parentSid, String sqlcondition)
    {
    	LOG.resultBean.Reset();
    	
        try {
        	int oid = 0;
        	
			OpenConn();
	        String q = "select outputrule_id from smdbadmin.output_rules where parent_instance_id=?";
	        PreparedStatement st = conn.prepareStatement(q);
	        st.setInt(1, parentSid);
	        ResultSet rs = st.executeQuery();
	        while(rs.next()) {
	        	oid = rs.getInt(1);
	        }
	        rs.close();
			st.close();
			
			
			PreparedStatement st1;
	        if(oid > 0) {
	        	q = "update smdbadmin.output_rules set condition=? where outputrule_id=?";
	        	st1 = conn.prepareStatement(q);
	        	st1.setString(1, sqlcondition);
	        	st1.setInt(2, oid);
	        } else {
	        	q = "insert into smdbadmin.output_rules(parent_instance_id, condition)";
		        q += " values(?, ?)";
		        st1 = conn.prepareStatement(q);
		        st1.setInt(1, parentSid);
		        st1.setString(2, sqlcondition);
	        }
			st1.executeUpdate();
			st1.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_OutputRule_Saved"));
			
		} catch (IOException e1) {
			LOG.Error(e1);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        return LOG.resultBean.Serialize();
    }
	
	public String GetOutputRule(int parentSid)
    {
    	LOG.resultBean.Reset();
    	
    	String outputrule = "";
        try {
			OpenConn();
	        String q = "select condition from smdbadmin.output_rules";
	        q += " where parent_instance_id=?";
	        PreparedStatement st = conn.prepareStatement(q);
	        st.setInt(1, parentSid);
	        ResultSet rs = st.executeQuery();

	        while(rs.next()){
        		outputrule = rs.getString(1);
	        }
	        rs.close();
	        st.close();

		} catch (IOException e1) {
			LOG.Error(e1);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return outputrule;
    }
	
	public String DeleteOutputRule(int parentSid)
    {
    	LOG.resultBean.Reset();
    	
        try {
			OpenConn();
	        String q = "delete smdbadmin.output_rules where parent_instance_id=?";
	        PreparedStatement st = conn.prepareStatement(q);
	        st.setInt(1, parentSid);
			st.executeUpdate();
			st.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS"));
		} catch (IOException e1) {
			LOG.Error(e1);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        return LOG.resultBean.Serialize();
    }
	
}
