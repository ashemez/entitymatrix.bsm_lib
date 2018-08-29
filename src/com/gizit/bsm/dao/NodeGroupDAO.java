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
import com.gizit.bsm.beans.ResultMessageBean;
import com.gizit.bsm.helpers.Helper;
import com.gizit.managers.ConnectionManager;
import com.gizit.managers.LogManager;
import com.gizit.managers.ResourceManager;

public class NodeGroupDAO extends DAO {

	public NodeGroupDAO() {
		super(NodeGroupDAO.class);
	}

	public String GetNodeGroup(int groupId)
    {
    	LOG.resultBean.Reset();

    	NodeGroupBean ngBean = new NodeGroupBean();
        try {
			OpenConn();
	        String q = "select g.node_group_id, g.group_name, g.bad_weight, i.service_instance_id, i.service_instance_name from smdbadmin.node_group g";
	        q += " left join smdbadmin.service_instance_relations r on(r.node_group_id=g.node_group_id) ";
	        q += " left join smdbadmin.service_instances i on(i.service_instance_id=r.service_instance_id)";
	        q += " where g.node_group_id=?";
	        q += " order by g.group_name asc, i.service_instance_name asc";
	        PreparedStatement st = conn.prepareStatement(q);
	        st.setInt(1, groupId);
	        ResultSet rs = st.executeQuery();

	        boolean entered = false;
	        while(rs.next()) {
	        	if(!entered) {
	        		entered = true;
	        		ngBean.node_group_id.set(groupId);
	        		ngBean.group_name.set(rs.getString(2));
	        		ngBean.bad_weight.set(Double.parseDouble(rs.getString(3)));
	        	}

	        	Member member = new Member();
	        	member.sid.set(rs.getInt(4));
	        	member.servicename.set(rs.getString(5));
	        	ngBean.members.add(member);
	        }
	        
	        rs.close();
	        st.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Retreived_NodeGroup"));
			
		} catch (IOException e1) {
			LOG.Error(e1);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return ngBean.Serialize();
    }
    
	public String GetNodeGroupsOfCurrentFunctionalGroup(int parentSid)
    {
		LOG.resultBean.Reset();
		
    	BeanList<NodeGroupBean> ngBeanList = new BeanList<NodeGroupBean>();
    	
        try {
			OpenConn();
	        String q = "select g.node_group_id, g.group_name, g.bad_weight, i.service_instance_id, i.service_instance_name from smdbadmin.node_group g";
	        q += " left join smdbadmin.service_instance_relations r on(r.node_group_id=g.node_group_id) ";
	        q += " left join smdbadmin.service_instances i on(i.service_instance_id=r.service_instance_id)";
	        q += " where g.parent_instance_id=?";
	        q += " order by g.group_name asc, i.service_instance_name asc";
	        PreparedStatement st = conn.prepareStatement(q);
	        st.setInt(1, parentSid);
	        ResultSet rs = st.executeQuery();
	        
	        HashMap<Integer, Integer> usedGroupIdList = new HashMap<Integer, Integer>();

	        NodeGroupBean currentNGBean = null;
	        while(rs.next()) {
	        	int groupId = rs.getInt(1);
	        	if(!usedGroupIdList.containsKey(groupId)) {
	        		usedGroupIdList.put(groupId, 1);

	        		currentNGBean = new NodeGroupBean();
	        		currentNGBean.node_group_id.set(groupId);
	        		currentNGBean.group_name.set(rs.getString(2));
	        		currentNGBean.bad_weight.set(Double.parseDouble(rs.getString(3)));

	        		ngBeanList.add(currentNGBean);
	        	}

	        	Member member = new Member();
	        	member.sid.set(rs.getInt(4));
	        	member.servicename.set(rs.getString(5));
	        	currentNGBean.members.add(member);
	        }
	        
	        rs.close();
	        st.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Retreived_NodeGroupList"));
			
		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return ngBeanList.Serialize();
    }
    
	public String CreateNodeGroup(NodeGroupBean ngBean)
    {
		LOG.resultBean.Reset();
		
        try {
			OpenConn();
	        String q = "insert into smdbadmin.node_group(group_name, bad_weight, parent_instance_id)";
	        q += " values(?, ?, ?)";
	        PreparedStatement st = conn.prepareStatement(q);
	        st.setString(1, ngBean.group_name.get());
	        st.setDouble(2, ngBean.bad_weight.get());
	        st.setInt(3, ngBean.parentSid.get());
			st.executeUpdate();
			st.close();
			
			q = "select node_group_id from smdbadmin.node_group where group_name=? and parent_instance_id=?";
			PreparedStatement st1 = conn.prepareStatement(q);
			st1.setString(1, ngBean.group_name.get());
			st1.setInt(2, ngBean.parentSid.get());
	        ResultSet rs = st1.executeQuery();
	        rs.next();
	        int groupId = rs.getInt(1);
	        rs.close();
	        st1.close();
	        
	        ngBean.node_group_id.set(groupId);
			
	        q = "update smdbadmin.service_instance_relations set node_group_id=?";
	        q += " where parent_instance_id=? and service_instance_id in(" + ngBean.GetMemberSidsAsStringList() + ")";
	        PreparedStatement st2 = conn.prepareStatement(q);
	        
	        LOG.Debug(" *** " + ngBean.GetMemberSidsAsStringList());
	        st2.setInt(1, groupId);
	        st2.setInt(2, ngBean.parentSid.get());
	        st2.executeUpdate();
	        st2.close();

	        LOG.Success(RM.GetErrorString("SUCCESS_Created_NodeGroup"));
	        
		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        return LOG.resultBean.Serialize();
    }
	
	public String UpdateNodeGroup(NodeGroupBean ngBean)
    {
		LOG.resultBean.Reset();
		
        try {
			OpenConn();
	        String q = "select node_group_id from smdbadmin.node_group where group_name='" + ngBean.group_name.get() + "'";
	        PreparedStatement st11 = conn.prepareStatement(q);
	        ResultSet rs1 = st11.executeQuery();
	        rs1.next();
	        ngBean.node_group_id.set(rs1.getInt("node_group_id"));
	        rs1.close();
	        st11.close();
	        
	        q = "update smdbadmin.node_group set group_name=?, bad_weight=?";
	        q += " where node_group_id=?";
	        PreparedStatement st = conn.prepareStatement(q);
	        st.setString(1, ngBean.group_name.get());
	        st.setDouble(2, ngBean.bad_weight.get());
	        st.setInt(3, ngBean.node_group_id.get());
			st.executeUpdate();
			st.close();
			
			String uq = "update smdbadmin.service_instance_relations set node_group_id=0 where node_group_id=?";
			uq += "; update smdbadmin.service_instance_relations set node_group_id=?";
			uq += " where service_instance_id in(" + ngBean.GetMemberSidsAsStringList() + ") and parent_instance_id=?;";
			LOG.Debug(uq);
			PreparedStatement st1 = conn.prepareStatement(uq);
			st1.setInt(1, ngBean.node_group_id.get());
			st1.setInt(2, ngBean.node_group_id.get());
			st1.setInt(3, ngBean.parentSid.get());
			st1.executeUpdate();
			st1.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Updated_NodeGroup"));
			
		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        return LOG.resultBean.Serialize();
    }
    
	public String DeleteNodeGroup(int groupId)
    {
		LOG.resultBean.Reset();
		
        try {
			OpenConn();
	        String q = "delete from smdbadmin.node_group where node_group_id=?";
	        q += "; update smdbadmin.service_instance_relations set node_group_id=0 where node_group_id=?;";
	        PreparedStatement st = conn.prepareStatement(q);
	        st.setInt(1, groupId);
	        st.setInt(2, groupId);
			st.executeUpdate();
			st.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Deleted_NodeGroup"));
			
		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        return LOG.resultBean.Serialize();
    }

}
