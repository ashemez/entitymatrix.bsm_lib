package com.gizit.bsm.dao;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TreeDAO extends DAO {
	public TreeDAO() {
		super(TreeDAO.class);
	}
	
	public String AddNewNode(int parentid, String nodeName, String citype)
    {
    	LOG.resultBean.Reset();
    	
        try {
			OpenConn();
	        
	        int sid = 0;
	        
		    // check if exists nodename
	        Statement st = conn.createStatement();
			String sq = "select service_instance_id from smdbadmin.service_instances where service_instance_name='" + nodeName + "'";
			ResultSet rs = st.executeQuery(sq);
			while(rs.next())
				sid = rs.getInt(1);
			rs.close();
			st.close();

	        // create instance
			if(sid == 0) {
				Statement st1 = conn.createStatement();
		        String iq = "insert into smdbadmin.service_instances(service_instance_name, service_instance_displayname, current_status, citype)";
		        iq += " values('" + nodeName + "', '" + nodeName + "', 0, '" + citype + "')";
		        st1.executeUpdate(iq);
		        st1.close();
				
				// get sid
				Statement st2 = conn.createStatement();
				sq = "select service_instance_id from smdbadmin.service_instances where service_instance_name='" + nodeName + "'";
				ResultSet rs1 = st2.executeQuery(sq);
				rs1.next();
				sid = rs1.getInt(1);
				rs1.close();
				st2.close();
			}
			
			// add relation
			if(sid > 0) {
				Statement st3 = conn.createStatement();
				String pq = "insert into smdbadmin.service_instance_relations(service_instance_id, parent_instance_id, node_group_id)";
				pq += " values(" + sid + ", " + parentid + ", 0)";
				st3.executeUpdate(pq);
				st3.close();
			}

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
	
	public String DeleteNode(int sid)
    {
    	LOG.resultBean.Reset();
    	
        try {
			OpenConn();
	        Statement st;
	        // delete relations
	        st = conn.createStatement();
	        String dq = "delete from smdbadmin.service_instance_relations where service_instance_id=" + sid;
	        dq += " or parent_instance_id=" + sid;
			st.executeUpdate(dq);
			st.close();

			// delete status changes
			st = conn.createStatement();
			dq = "delete from smdbadmin.service_status_change where service_id=" + sid;
			st.executeUpdate(dq);
			st.close();

			// delete availability history
			st = conn.createStatement();
			dq = "delete from smdbadmin.daily_availability where service_instance_id=" + sid;
			st.executeUpdate(dq);
			st.close();
			
			// delete related kpis
			st = conn.createStatement();
			dq = "delete from smdbadmin.kpi where service_instance_id=" + sid;
			st.executeUpdate(dq);
			st.close();
			
			// delete service instance
			st = conn.createStatement();
			dq = "delete from smdbadmin.service_instances where service_instance_id=" + sid;
			st.executeUpdate(dq);
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
	
	public String RenameNode(int sid, String nodeName)
    {
    	LOG.resultBean.Reset();
    	
        try {
			OpenConn();
	        Statement st;
	        st = conn.createStatement();
	        String iq = "update smdbadmin.service_instances set service_instance_name='" + nodeName + "', service_instance_displayname='" + nodeName + "'";
	        iq += " where service_instance_id=" + sid;
			st.executeUpdate(iq);
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
	
	public String UpdateNodeStatus(int sid, int status)
    {
    	LOG.resultBean.Reset();
    	
        try {
			OpenConn();
	        Statement st;
	        st = conn.createStatement();
	        String iq = "update smdbadmin.service_instances set status_timestamp=EXTRACT(EPOCH FROM (select current_timestamp)), current_status=" + status;
	        iq += " where service_instance_id=" + sid;
			st.executeUpdate(iq);
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
	
	public String GetNodeParentList(int sid)
    {
    	LOG.resultBean.Reset();
    	
    	String parentList = "";
        try {
			OpenConn();
	        Statement st;
	        st = conn.createStatement();
	        String q = "select r.parent_instance_id, i.service_instance_name, r.relation_weight from smdbadmin.service_instance_relations r";
	        q += " left join smdbadmin.service_instances i on(r.parent_instance_id=i.service_instance_id)";
	        q += " where r.service_instance_id=" + sid;
	        ResultSet rs = st.executeQuery(q);
	        
	        parentList = "[";
	        int cnt = 0;
	        while(rs.next()) {
	        	if(cnt > 0)
	        		parentList += ",";
	        	parentList += "{\"name\":\"" + rs.getString("service_instance_name") + "\",";
	        	parentList += "\"relation_weight\":" + rs.getInt("relation_weight") + ",";
	        	parentList += "\"id\":" + rs.getInt("parent_instance_id") + "}";
	        	cnt++;
	        }
	        parentList += "]";
	        
	        rs.close();
	        st.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS"));
			
		} catch (IOException e1) {
			LOG.Error(e1);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return parentList;
    }
	
	public String GetNodeSid(String nodeName)
    {
    	LOG.resultBean.Reset();
    	
    	String newNode = "";
        try {
			OpenConn();
	        Statement st;
	        st = conn.createStatement();
	        String q = "select service_instance_id, service_instance_name, service_instance_displayname, service_template_id, current_status, citype, propagate, status_timestamp";
	        q += " FROM smdbadmin.service_instances";
	        q += " where service_instance_name='" + nodeName + "'";
	        ResultSet rs = st.executeQuery(q);
	        
	        newNode = "{";
	        while(rs.next()){
	        	newNode += "\"sid\":\"" + rs.getString(1) + "\",";
	        	newNode += "\"sname\":\"" + rs.getString(2).replaceAll("\\s","") + "\"";
	        }
	        newNode += "}";
	        rs.close();
	        st.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS"));
			
		} catch (IOException e1) {
			LOG.Error(e1);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return newNode;
    }
	
	// attributes
	
	public String UpdateNodeOutputWeights(int sid, String compmodel, int badbad, int badmarginal, int marginalbad, int marginalmarginal, int parentweight, int parentsid) {
		LOG.resultBean.Reset();
		
		try {
			OpenConn();
			
			String q = "update smdbadmin.service_instances set compmodel=?, badbad=?, badmarginal=?, marginalbad=?, marginalmarginal=?";
			q += " where service_instance_id=?";
			PreparedStatement st = conn.prepareStatement(q);
			st.setString(1, compmodel);
			st.setInt(2, badbad);
			st.setInt(3, badmarginal);
			st.setInt(4, marginalbad);
			st.setInt(5, marginalmarginal);
			st.setInt(6, sid);
			st.executeUpdate();
			st.close();
			
			if(parentsid != 0) {
				q = "update smdbadmin.service_instance_relations set relation_weight=? where service_instance_id=? and parent_instance_id=?";
				PreparedStatement st2 = conn.prepareStatement(q);
				st2.setInt(1, parentweight);
				st2.setInt(2, sid);
				st2.setInt(3, parentsid);
				st2.executeUpdate();
				st2.close();
			}
			LOG.Success(RM.GetErrorString("SUCCESS_Node_WeightsUpdated"));
			
		} catch (IOException e) {
			LOG.Error(e);
			LOG.Warn(RM.GetErrorString("WARN_Node_WeightsUpdateProblem"));
		} catch (SQLException e) {
			LOG.Error(e);
			LOG.Warn(RM.GetErrorString("WARN_Node_WeightsUpdateProblem"));
		} catch (Exception e) {
			LOG.Error(e);
			LOG.Warn(e.getMessage());
		} finally {
			CloseConn();
		}
		
        return LOG.resultBean.Serialize();
	}

	
}
