package com.gizit.bsm.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.gizit.bsm.beans.BeanList;
import com.gizit.bsm.beans.NodeGroupBean;
import com.gizit.bsm.beans.NodeGroupBean.Member;
import com.gizit.bsm.beans.ResultMessageBean;
import com.gizit.bsm.beans.UserBean;
import com.gizit.bsm.beans.RoleBean;
import com.gizit.bsm.beans.GroupBean;
import com.gizit.bsm.helpers.Helper;
import com.gizit.managers.ConnectionManager;
import com.gizit.managers.LogManager;
import com.gizit.managers.ResourceManager;
import com.google.gson.Gson;

public class GroupsDAO extends DAO {

	public GroupsDAO() {
		super(GroupsDAO.class);
	}
	
	/**
	 * Returns all user groups info list as JSON string, each group info contains group_id and group_name
	 * @return group info list as JSON string: group_id, group_name
	 */
    public String GetAllGroups()
    {
    	LOG.resultBean.Reset();
    	
    	String grpJson = "";
    	
        try {
			OpenConn();

			grpJson = "[";
			
			String q = "select group_id, group_name from smdbadmin.groups order by group_name desc";
	        PreparedStatement pst = conn.prepareStatement(q);
	        ResultSet rs = pst.executeQuery();
	        
	        int i = 0;
	        while(rs.next()) {
	        	
	        	if(i > 0)
	        		grpJson += ",";
	        	
	        	grpJson += "{";
	        	grpJson += "\"group_id\":\"" + rs.getInt(1) + "\",";
	        	grpJson += "\"group_name\":\"" + rs.getString(2) + "\"";
	        	grpJson += "}";
	        	
	        	i++;
	        }
	        
	        grpJson += "]";
	        
	        rs.close();
	        pst.close();
	        
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return grpJson;
    }
    
    /**
     * Returns group info for given group_id
     * @param groupid group_id
     * @return serialized GroupBean
     */
	public String GetGroup(int groupid) {
		LOG.resultBean.Reset();
		GroupBean group=new GroupBean();
		try{
			OpenConn();
			String q = "select * from smdbadmin.groups where group_id=?;";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, groupid);
			ResultSet rs = statement.executeQuery();
			
			while(rs.next()){
				group.group_id.set(Integer.toString(rs.getInt("group_id")));
				group.group_name.set(rs.getString("group_name"));
			}
			
			rs.close();
			statement.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Retreived_Group"));
			
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return group.Serialize();
	}
    
	/**
	 * Returns enrolled user group info list as JSON string, each group info contains group_id and group_name
	 * @return group info list as JSON string: group_id, group_name
	 */
    public String GetEnrolledGroups(String username)
    {
    	LOG.resultBean.Reset();
    	
    	String grpJson = "";
    	
        try {
			OpenConn();

			grpJson = "[";
			
			String q = "select r.group_id, r.group_name from smdbadmin.groups r ";
			q += " left join smdbadmin.user_groups ur on (ur.group_id=r.group_id)";
			q += " left join smdbadmin.users u on(u.userid=ur.user_id)";
			q += " where u.username=?";
			q += " order by group_name desc";
	        PreparedStatement pst = conn.prepareStatement(q);
	        pst.setString(1, username);
	        ResultSet rs = pst.executeQuery();
	        
	        int i = 0;
	        while(rs.next()) {
	        	
	        	if(i > 0)
	        		grpJson += ",";
	        	
	        	grpJson += "{";
	        	grpJson += "\"group_id\":\"" + rs.getInt(1) + "\",";
	        	grpJson += "\"group_name\":\"" + rs.getString(2) + "\"";
	        	grpJson += "}";
	        	
	        	i++;
	        }
	        
	        grpJson += "]";
	        
	        rs.close();
	        pst.close();
	        
	        LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_User_Enrolled_Groups"));
	        
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return grpJson;
    }

    /**
     * Returns unenrolled user group info list as JSON string, each group info contains group_id and group_name for given username
     * @param username
     * @return group info list as JSON string: group_id, group_name
     */
    public String GetUnenrolledGroups(String username)
    {
    	LOG.resultBean.Reset();
    	
    	String grpJson = "";
    	
        try {
			OpenConn();

			grpJson = "[";
			
			String q = "select group_id, group_name from smdbadmin.groups ";
			q += " where group_id not in(select g.group_id from smdbadmin.groups g ";
			q += " left join smdbadmin.user_groups ug on (ug.group_id=g.group_id)";
			q += " left join smdbadmin.users u on(u.userid=ug.user_id)";
			q += " where u.username=?)";
			q += " order by group_name desc";
	        PreparedStatement pst = conn.prepareStatement(q);
	        pst.setString(1, username);
	        ResultSet rs = pst.executeQuery();
	        
	        int i = 0;
	        while(rs.next()) {
	        	
	        	if(i > 0)
	        		grpJson += ",";
	        	
	        	grpJson += "{";
	        	grpJson += "\"group_id\":\"" + rs.getInt(1) + "\",";
	        	grpJson += "\"group_name\":\"" + rs.getString(2) + "\"";
	        	grpJson += "}";
	        	
	        	i++;
	        }
	        
	        grpJson += "]";
	        
	        rs.close();
	        pst.close();
	        
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return grpJson;
    }
    
    /**
     * Returns user info list of a group
     * @param groupid
     * @return Serialized BeanList of UserBean
     */
	public String GetGroupEnrolledUsers(int groupid) {
		LOG.resultBean.Reset();
		BeanList<UserBean> userList = new BeanList<UserBean>();
		try{
			OpenConn();
			String q = "select u.userid, u.username from smdbadmin.user_groups ug left join smdbadmin.users u on (u.userid=ug.user_id) where group_id = ?;";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, groupid);
			ResultSet rs = statement.executeQuery();
			
			while(rs.next()){
				UserBean user=new UserBean();	
				user.userid.set(Integer.parseInt(rs.getString("userid")));
				user.username.set(rs.getString("username"));
				userList.add(user);
			}
			
			rs.close();
			statement.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_User_UnEnrolled_Groups"));
		} catch (SQLException e) {
			LOG.Error(e);
		}catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return userList.Serialize();
	}
	
	public String GetGroupUnEnrolledUsers(int groupid) {
		LOG.resultBean.Reset();
		BeanList<UserBean> userList = new BeanList<UserBean>();
		try{
			OpenConn();
			String q = "select u.userid, u.username from smdbadmin.users u where u.userid not in (select user_id from smdbadmin.user_groups where group_id = ?);";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, groupid);
			ResultSet rs = statement.executeQuery();
			
			while(rs.next()){
				UserBean user=new UserBean();	
				user.userid.set(Integer.parseInt(rs.getString("userid")));
				user.username.set(rs.getString("username"));
				userList.add(user);
			}
			
			rs.close();
			statement.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_Group_UnEnrolled_Users"));
		} catch (SQLException e) {
			LOG.Error(e);
		}catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return userList.Serialize();		
	}
	
	public String GetGroupEnrolledRoles(int groupid) {
		LOG.resultBean.Reset();
		BeanList<RoleBean> roleList = new BeanList<RoleBean>();
		try{
			OpenConn();
			String q = "select r.role_id, r.role_name from smdbadmin.group_roles gr left join smdbadmin.roles r on (r.role_id=gr.role_id) where group_id = ?;";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, groupid);
			ResultSet rs = statement.executeQuery();
			
			while(rs.next()){
				RoleBean role=new RoleBean();	
				role.role_id.set(rs.getString("role_id"));
				role.role_name.set(rs.getString("role_name"));
				roleList.add(role);
			}
			
			rs.close();
			statement.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_Group_Enrolled_Roles"));
		} catch (SQLException e) {
			LOG.Error(e);
		}catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return roleList.Serialize();
	}
	
	public String GetGroupUnEnrolledRoles(int groupid) {
		LOG.resultBean.Reset();
		BeanList<RoleBean> roleList = new BeanList<RoleBean>();
		try{
			OpenConn();
			String q = "select r.role_id, r.role_name from smdbadmin.roles r where r.role_id not in (select role_id from smdbadmin.group_roles where group_id = ?);";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, groupid);
			ResultSet rs = statement.executeQuery();
			
			while(rs.next()){
				RoleBean role=new RoleBean();	
				role.role_id.set(rs.getString("role_id"));
				role.role_name.set(rs.getString("role_name"));
				roleList.add(role);
			}
			
			rs.close();
			statement.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_Group_UnEnrolled_Roles"));
		} catch (SQLException e) {
			LOG.Error(e);
		}catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return roleList.Serialize();		
	}
    
    public String CreateNewGroup(String group_name, String[] grpRoleList, String[] memberList) {
    	LOG.resultBean.Reset();
    	
        try {
			OpenConn();

			String q = "select count(*) from smdbadmin.groups where group_name=?";
			PreparedStatement st1 = conn.prepareStatement(q);
			st1.setString(1, group_name);
	        ResultSet rs = st1.executeQuery();
	        rs.next();
			
	        int cnt = rs.getInt(1);
	        
	        rs.close();
	        st1.close();
	        
	        // create group
	        if(cnt > 0)
	        	LOG.Warn(RM.GetErrorString("WARN_GROUP_ALREADYEXIST"));
	        else {
	        	q = "insert into smdbadmin.groups(group_name) values(?)";
		        PreparedStatement st2 = conn.prepareStatement(q);
		        st2.setString(1, group_name);
		        st2.executeUpdate();
		        st2.close();
		        
		        // update roles and members
		        //if(grpRoleList.length > 0 || memberList.length > 0) {
	        	q = "select group_id from smdbadmin.groups where group_name=?";
				PreparedStatement st3 = conn.prepareStatement(q);
				st3.setString(1, group_name);
		        ResultSet rs1 = st3.executeQuery();
		        rs1.next();
		        
		        int groupId = rs1.getInt(1);
		        
		        rs1.close();
		        st3.close();
		        
		        //if(grpRoleList.length > 0)
		        UpdateGroupRoles(groupId, grpRoleList);
		        
		        //if(memberList.length > 0)
		        UpdateGroupMembers(groupId, memberList);
		    }
		        
		    LOG.Success(RM.GetErrorString("SUCCESS_Created_Group"));
	        //}

		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        return LOG.resultBean.Serialize();
    }
    
    public String UpdateGroup(int group_id,String group_name,String[] grpRoleList, String[] memberList) {
    	LOG.resultBean.Reset();
    	
        try {
			OpenConn();

			String q = "select count(*) from smdbadmin.groups where group_id!=? and group_name=?;";
			PreparedStatement st1 = conn.prepareStatement(q);
			st1.setInt(1, group_id);
			st1.setString(2, group_name);
	        ResultSet rs = st1.executeQuery();
	        rs.next();
			
	        int cnt = rs.getInt(1);
	        
	        rs.close();
	        st1.close();
	        
	        if(cnt > 0)
	        	LOG.Warn(RM.GetErrorString("WARN_GROUP_ALREADYEXIST"));
	        else {
	        	q = "update smdbadmin.groups set group_name=? where group_id=?";
		        PreparedStatement st2 = conn.prepareStatement(q);
		        st2.setString(1, group_name);
		        st2.setInt(2, group_id);
		        st2.executeUpdate();
		        st2.close();
		        
		        //if(grpRoleList!= null && grpRoleList.length > 0)
		        UpdateGroupRoles(group_id, grpRoleList);
		        
		        //if(memberList!= null && memberList.length > 0)
		        UpdateGroupMembers(group_id, memberList);
		        
		        LOG.Success(RM.GetErrorString("SUCCESS_Updated_Group"));
	        }
	        
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        return LOG.resultBean.Serialize();
    }
    
    private void UpdateGroupRoles(int group_id, String[] grpRoleList) throws SQLException {
		// delete all first
		String q = "delete from smdbadmin.group_roles where group_id=?";
		PreparedStatement st1 = conn.prepareStatement(q);
    	st1.setInt(1, group_id);
    	st1.executeUpdate();
    	st1.close();
    	
    	if(grpRoleList!= null && grpRoleList.length > 0) {
	    	// insert the new list
	    	q = "insert into smdbadmin.group_roles(group_id, role_id)";
	    	q += " values(?, ?)";
	    	PreparedStatement st2 = conn.prepareStatement(q);
			for(int i=0; i<grpRoleList.length; i++) {
				if(!grpRoleList[i].trim().equals(""))
				{
		    		st2.setInt(1, group_id);
		    		st2.setInt(2, Integer.parseInt(grpRoleList[i]));
		    		st2.executeUpdate();					
				}
	    	}
			st2.close();
    	}
    }
    
    private void UpdateGroupMembers(int group_id, String[] memberList) throws SQLException {
		// delete all first
		String q = "delete from smdbadmin.user_groups where group_id=?";
		PreparedStatement st1 = conn.prepareStatement(q);
    	st1.setInt(1, group_id);
    	st1.executeUpdate();
    	st1.close();

    	if(memberList!= null && memberList.length > 0) {
	    	// insert the new list
	    	q = "insert into smdbadmin.user_groups(group_id, user_id) values(?, ?)";
	    	PreparedStatement st2 = conn.prepareStatement(q);
			for(int i=0; i<memberList.length; i++) {
				if(!memberList[i].trim().equals(""))
				{
		    		st2.setInt(1, group_id);
		    		st2.setInt(2, Integer.parseInt(memberList[i]));
		    		st2.executeUpdate();
				}
	    	}
			st2.close();
    	}
    }
    
    public String DeleteGroup(int groupid) {
 		LOG.resultBean.Reset();
 		try{
 			OpenConn();
 			String q = "delete from smdbadmin.groups where group_id=?;"
 					+ "delete from smdbadmin.user_groups where group_id=?;"
 					+ "delete from smdbadmin.group_roles where group_id=?;";
 			PreparedStatement stmt = conn.prepareStatement(q);
 			stmt.setInt(1, groupid);
 			stmt.setInt(2, groupid);
 			stmt.setInt(3, groupid);
 			stmt.executeUpdate();
 			stmt.close();
 			
 			LOG.Success(RM.GetErrorString("SUCCESS_Deleted_Group"));
 			
 		} catch (SQLException e) {
 			LOG.Error(e);
 		} catch (IOException e) {
 			LOG.Error(e);
 		} finally {
 			CloseConn();
 		}
 		
 		return LOG.resultBean.Serialize();
 	}
    
    
}
