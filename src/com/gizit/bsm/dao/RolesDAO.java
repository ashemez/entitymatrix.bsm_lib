package com.gizit.bsm.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.NamingException;

import com.gizit.bsm.beans.AltUserBean;
import com.gizit.bsm.beans.BeanList;
import com.gizit.bsm.beans.DSBean;
import com.gizit.bsm.beans.GroupBean;
import com.gizit.bsm.beans.KPIBean;
import com.gizit.bsm.beans.KPIRuleBean;
import com.gizit.bsm.beans.NodeGroupBean;
import com.gizit.bsm.beans.NodeGroupBean.Member;
import com.gizit.bsm.beans.OutputRuleBean;
import com.gizit.bsm.beans.ResultMessageBean;
import com.gizit.bsm.beans.RoleBean;
import com.gizit.bsm.beans.ServiceBean;
import com.gizit.bsm.beans.RoleResourcePermBean;
import com.gizit.bsm.beans.RoleResourcePermBean.ResPerm;
import com.gizit.bsm.beans.RoleSrvPermBean;
import com.gizit.bsm.beans.UserBean;
import com.gizit.bsm.beans.RoleSrvPermBean.SrvPerm;
import com.gizit.bsm.helpers.Helper;
import com.gizit.managers.ConnectionManager;
import com.gizit.managers.LDAPManager;
import com.gizit.managers.LogManager;
import com.gizit.managers.ResourceManager;

public class RolesDAO extends DAO {

	public RolesDAO() {
		super(RolesDAO.class);
	}
	
    public String CreateNewRole(String role_name, String[] srvPermList, String[] resPermList) {
    	LOG.resultBean.Reset();

        try {
			OpenConn();

			String q = "select count(*) from smdbadmin.roles where role_name=?";
			PreparedStatement st1 = conn.prepareStatement(q);
			st1.setString(1, role_name);
	        ResultSet rs = st1.executeQuery();
	        rs.next();
			
	        int cnt = rs.getInt(1);
	        
	        rs.close();
	        st1.close();
	        
	        if(cnt > 0) {
	        	String msg = RM.GetErrorString("WARN_ROLE_ALREADYEXIST");
	        	LOG.Warn(msg);
	        }
	        else {
	        	q = "insert into smdbadmin.roles(role_name) values(?)";
		        PreparedStatement st2 = conn.prepareStatement(q);
		        st2.setString(1, role_name);
		        st2.executeUpdate();
		        st2.close();
		        LOG.Warn("Insert passed---->"+srvPermList.length+"****"+resPermList.length);
		        LOG.Warn(Integer.toString(srvPermList.length));
		        LOG.Warn(Integer.toString(resPermList.length));
		        // update permissions
		        if(srvPermList.length > 0 || resPermList.length > 0) {
		        	q = "select role_id from smdbadmin.roles where role_name=?";
					PreparedStatement st3 = conn.prepareStatement(q);
					st3.setString(1, role_name);
			        ResultSet rs1 = st3.executeQuery();
			        rs1.next();
			        LOG.Warn("SrvPerm Length:"+srvPermList.length);
			        int roleId = rs1.getInt(1);
			        if(srvPermList.length > 0) {
			        	LOG.Warn("srvPermList");
			        	UpdateRoleServicePermission(roleId, srvPermList);
			        }
			        LOG.Warn("ResPerm Length:"+resPermList.length);
			        if(resPermList.length > 0) {
			        	UpdateRoleResourcePermissions(roleId, resPermList);
			        }
			        rs1.close();
			        st3.close();
		        }
		        
		        LOG.Success(RM.GetErrorString("SUCCESS_Created_Role"));
	        }

		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        return LOG.resultBean.Serialize();
    }
    
    public String GetRoleAfterInsert(String rolename) {
		LOG.resultBean.Reset();
		RoleBean role = new RoleBean();
		try{
			OpenConn();
			String q = "select * from smdbadmin.roles where role_name=?;";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setString(1, rolename);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				role.role_id.set(Integer.toString(rs.getInt("role_id")));
				role.role_name.set(rs.getString("role_name"));
			}
			rs.close();
			statement.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Retreived_Role"));
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return role.Serialize();
	}
    
	public String GetRole(int roleid) {
		LOG.resultBean.Reset();
		RoleBean role=new RoleBean();
		try{
			OpenConn();
			String q = "select * from smdbadmin.roles where role_id=?;";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, roleid);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				role.role_id.set(Integer.toString(rs.getInt("role_id")));
				role.role_name.set(rs.getString("role_name"));
			}
			rs.close();
			statement.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Retreived_Role"));
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return role.Serialize();
	}
    
    public String UpdateRole(int role_id, String role_name, String[] srvPermList, String[] resPermList) {
    	LOG.resultBean.Reset();
    	
    	LOG.Debug("Calling UpdateRole!");
    	
        try {
			OpenConn();

			String q = "select count(*) from smdbadmin.roles where role_name=? and role_id!=?";
			PreparedStatement st1 = conn.prepareStatement(q);
			st1.setString(1, role_name);
			st1.setInt(2, role_id);
	        ResultSet rs = st1.executeQuery();
	        rs.next();
			
	        int cnt = rs.getInt(1);
	        
	        rs.close();
	        st1.close();
	        
	        if(cnt > 0) {
	        	String msg = RM.GetErrorString("WARN_ROLE_ALREADYEXIST");
	        	LOG.Warn(msg);
	        }
	        else {
	        	q = "update smdbadmin.roles set role_name=? where role_id=? and role_name!=?";
		        PreparedStatement st2 = conn.prepareStatement(q);
		        st2.setString(1, role_name);
		        st2.setInt(2, role_id);
		        st2.setString(3, role_name);
		        st2.executeUpdate();
		        st2.close();
		        
		        // update permissions
		        //if(srvPermList.length > 0)
		        UpdateRoleServicePermission(role_id, srvPermList);
		        
		        //LOG.Debug("resPermList[0]: " + resPermList[0]);
		        
		        boolean updated = false;
		        for(String perm : resPermList)
		        {
		        	if(Integer.parseInt(perm) > 0)
		        		updated = true;
		        }
		        if(updated && resPermList.length > 0)
		        	UpdateRoleResourcePermissions(role_id, resPermList);
		        
		        LOG.Success(RM.GetErrorString("SUCCESS_Updated_Role"));
	        }
	        
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        return LOG.resultBean.Serialize();
    }
    
    public String UpdateRoleMembers(int role_id, String[] groupList, String[] userList) {
    	LOG.resultBean.Reset();
    	
    	LOG.Debug("Calling UpdateRoleMembers!");
        try {
			OpenConn();

	        //if(groupList.length > 0)
	        UpdateGroupRoles(role_id, groupList);
	        
	        //if(userList.length > 0)
	        UpdateUserRoles(role_id, userList);
	        
	        LOG.Success(RM.GetErrorString("SUCCESS"));
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        return LOG.resultBean.Serialize();
    }
    
    private void UpdateGroupRoles(int role_id, String[] groupList) throws SQLException {
		// delete all first
		String q = "delete from smdbadmin.group_roles where role_id=?";
		PreparedStatement st1 = conn.prepareStatement(q);
    	st1.setInt(1, role_id);
    	st1.executeUpdate();
    	st1.close();
    	
    	// insert the new list
    	q = "insert into smdbadmin.group_roles(role_id, group_id)";
    	q += " values(?, ?)";
    	PreparedStatement st2 = conn.prepareStatement(q);
		for(int i=0; i<groupList.length; i++) {
    		st2.setInt(1, role_id);
    		st2.setInt(2, Integer.parseInt(groupList[i]));
    		st2.executeUpdate();
    	}
		st2.close();
    }
    
    private void UpdateUserRoles(int role_id, String[] userList) throws SQLException {
    	LOG.Debug("updating userroles");
    	
		// delete all first
		String q = "delete from smdbadmin.user_roles where role_id=?";
		PreparedStatement st1 = conn.prepareStatement(q);
    	st1.setInt(1, role_id);
    	st1.executeUpdate();
    	st1.close();
    	
    	// insert the new list
    	q = "insert into smdbadmin.user_roles(role_id, user_id)";
    	q += " values(?, ?)";
    	PreparedStatement st2 = conn.prepareStatement(q);
		for(int i=0; i<userList.length; i++) {
    		st2.setInt(1, role_id);
    		// if user id is not numeric, which means it could be an ldap based username, create a smdb local user first
    		if(!Helper.isNumeric(userList[i])) {
    			int uid = CreateUser(userList[i]);
    			st2.setInt(2, uid);
    		} else {
    			st2.setInt(2, Integer.parseInt(userList[i]));
    		}
    		st2.executeUpdate();
    	}
		st2.close();
    }
    
    private int CreateUser(String username) {
    	int u = 0;
		try {
			// get uid if exists
			String su = "select userid from smdbadmin.users where username=?";
			PreparedStatement pst1 = conn.prepareStatement(su);
			pst1.setString(1, username);
	    	ResultSet rs1 = pst1.executeQuery();
	    	boolean exists = rs1.next();
	    	if(exists)
	    		u = rs1.getInt("userid");
	    	
	    	rs1.close();
	    	pst1.close();
	    	
	    	if(!exists) {
	    		// craete user if doesnt exist
		    	String cu = "insert into smdbadmin.users(username, firstname, lastname, password) values(?, ?, ?, ?)";
		    	PreparedStatement pst2 = conn.prepareStatement(cu);
		    	pst2.setString(1, username);
		    	pst2.setString(2, username);
		    	pst2.setString(3, username);
		    	pst2.setString(4, username);
		    	pst2.executeUpdate();
		    	pst2.close();
		    	
		    	// get uid
		    	su = "select userid from smdbadmin.users where username=?";
		    	PreparedStatement pst3 = conn.prepareStatement(su);
		    	pst3.setString(1, username);
		    	ResultSet rs3 = pst3.executeQuery();
		    	rs3.next();
		    	u = rs3.getInt("userid");
		    	
		    	rs3.close();
		    	pst3.close();
	    	}
	    	
		} catch (SQLException e) {
			LOG.Error(e);
		}
    	
    	return u;
    }
    
    private void UpdateRoleServicePermission(int role_id, String[] srvPermList) throws SQLException {
		// delete all first
		String q = "delete from smdbadmin.role_service_permissions where role_id=?";
		PreparedStatement st1 = conn.prepareStatement(q);
    	st1.setInt(1, role_id);
    	st1.executeUpdate();
    	st1.close();
    	
    	// insert the new list
    	q = "insert into smdbadmin.role_service_permissions(role_id, service_instance_id, permission_type_id)";
    	q += " values(?, ?, ?)";
    	PreparedStatement st2 = conn.prepareStatement(q);
		for(int i=0; i<srvPermList.length; i++) {
    		st2.setInt(1, role_id);
    		st2.setInt(2, Integer.parseInt(srvPermList[i]));
    		st2.setInt(3, 2);
    		st2.executeUpdate();
    	}
		st2.close();
    }
    
    private void UpdateRoleResourcePermissions(int role_id, String[] resPermList) throws SQLException {
		// delete all first
		String q = "delete from smdbadmin.role_resource_permissions where role_id=?";
		LOG.Debug("delete role perm q: " + q);
		PreparedStatement st1 = conn.prepareStatement(q);
    	st1.setInt(1, role_id);
    	st1.executeUpdate();
    	st1.close();
    	
    	HashMap<String, Integer> PERMS = new HashMap<String, Integer>();
    	String pq = "select permission_type_id, permission_type from smdbadmin.permission_types";
    	PreparedStatement pst = conn.prepareStatement(pq);
    	ResultSet prs = pst.executeQuery();
    	while(prs.next()) {
    		PERMS.put(prs.getString("permission_type"), prs.getInt("permission_type_id"));
    	}
    	prs.close();
    	pst.close();
    	
    	HashMap<String, Integer> RESOURCES = new HashMap<String, Integer>();
    	String rq = "select resource_type_id, resource_type from smdbadmin.resource_types";
    	PreparedStatement rst = conn.prepareStatement(rq);
    	ResultSet rrs = rst.executeQuery();
    	while(rrs.next()) {
    		RESOURCES.put(rrs.getString("resource_type"), rrs.getInt("resource_type_id"));
    	}
    	rrs.close();
    	rst.close();
    	
    	// insert the new list
    	q = "insert into smdbadmin.role_resource_permissions(role_id, resource_type_id, permission_type_id)";
    	q += " values(?, ?, ?)";
    	PreparedStatement st2 = conn.prepareStatement(q);
    	
    	// def res assumed
    	HashMap<Integer, String> srvDef = new HashMap<Integer, String>();
    	srvDef.put(1, "Service");
    	srvDef.put(2, "Datasource");
    	srvDef.put(3, "KPI");
    	srvDef.put(4, "Report");
    	srvDef.put(5, "Administration");
    	
		for(int i=0; i<resPermList.length; i++) {
			if(Integer.parseInt(resPermList[i]) != 0) {
				String perm_type = "VIEW";
				if(Integer.parseInt(resPermList[i]) == 2)
					perm_type = "EDIT";
				
				int k = i+1;
	    		st2.setInt(1, role_id);
	    		st2.setInt(2, RESOURCES.get(srvDef.get(k)));
	    		st2.setInt(3, PERMS.get(perm_type));
	    		st2.executeUpdate();
			}
    	}
		st2.close();
    }
    
    public String GetAllRoles()
    {
    	LOG.resultBean.Reset();

    	BeanList<RoleBean> roleList = new BeanList<RoleBean>();
        try {
			OpenConn();

			String q = "select role_id, role_name from smdbadmin.roles order by role_name desc";
	        PreparedStatement pst = conn.prepareStatement(q);
	        ResultSet rs = pst.executeQuery();

	        while(rs.next()) {
	        	RoleBean role = new RoleBean();
	        	role.role_id.set(rs.getString(1));
	        	role.role_name.set(rs.getString(2));
	        	roleList.add(role);
	        }

	        rs.close();
	        pst.close();
	        
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return roleList.Serialize();
    }
    
    public String GetEnrolledRoles(String username)
    {
    	LOG.resultBean.Reset();

    	BeanList<RoleBean> roleList = new BeanList<RoleBean>();
        try {
			OpenConn();

			String q = "select r.role_id, r.role_name from smdbadmin.roles r ";
			q += " left join smdbadmin.user_roles ur on (ur.role_id=r.role_id)";
			q += " left join smdbadmin.users u on(u.userid=ur.user_id)";
			q += " where u.username=?";
			q += " order by role_name desc";
	        PreparedStatement pst = conn.prepareStatement(q);
	        pst.setString(1, username);
	        ResultSet rs = pst.executeQuery();

	        while(rs.next()) {
	        	RoleBean role = new RoleBean();
	        	role.role_id.set(rs.getString(1));
	        	role.role_name.set(rs.getString(2));
	        	roleList.add(role);
	        }
	        
	        rs.close();
	        pst.close();
	        
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return roleList.Serialize();
    }

    public String GetUnenrolledRoles(String username)
    {
    	LOG.resultBean.Reset();
    	
    	BeanList<RoleBean> roleList = new BeanList<RoleBean>();
        try {
			OpenConn();
			
			String q = "select role_id, role_name from smdbadmin.roles ";
			q += " where role_id not in(select r.role_id from smdbadmin.roles r ";
			q += " left join smdbadmin.user_roles ur on (ur.role_id=r.role_id)";
			q += " left join smdbadmin.users u on(u.userid=ur.user_id)";
			q += " where u.username=?)";
			q += " order by role_name desc";
	        PreparedStatement pst = conn.prepareStatement(q);
	        pst.setString(1, username);
	        ResultSet rs = pst.executeQuery();

	        while(rs.next()) {
	        	RoleBean role = new RoleBean();
	        	role.role_id.set(rs.getString(1));
	        	role.role_name.set(rs.getString(2));
	        	roleList.add(role);
	        }
	        
	        rs.close();
	        pst.close();
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return roleList.Serialize();
    }
    
	public String GetRoleEnrolledUsers(int role_id) {
		LOG.resultBean.Reset();
		BeanList<UserBean> userList = new BeanList<UserBean>();
		try{
			OpenConn();
			String q = "select u.userid, u.username from smdbadmin.user_roles ur left join smdbadmin.users u on (u.userid=ur.user_id) where role_id = ?;";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, role_id);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				UserBean user=new UserBean();	
				user.userid.set(rs.getInt("userid"));
				user.username.set(rs.getString("username"));
				userList.add(user);
			}
			rs.close();
			statement.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_Role_Enrolled_Users"));
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

	public String GetRoleUnEnrolledLDAPUsers(int role_id) {
		LOG.resultBean.Reset();
		
		LDAPManager ldapMng = new LDAPManager();
		
		ArrayList<String> userList = new ArrayList<String>();
		BeanList<AltUserBean> ldapUnenrolledList = new BeanList<AltUserBean>();

		try{
			ArrayList<String> ldapUsers = ldapMng.GetLDAPUsers();
			
			OpenConn();
			
			// first get unenrolled ldap users
			String q = "select u.userid, u.username from smdbadmin.user_roles ur left join smdbadmin.users u on (u.userid=ur.user_id) where role_id = ?;";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, role_id);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				userList.add(rs.getString("username"));
			}
			rs.close();
			statement.close();
			
			for(String luser : ldapUsers) {
				if(!userList.contains(luser)) {
					AltUserBean user=new AltUserBean();	
					user.userid.set(luser);
					user.username.set(luser);
					ldapUnenrolledList.add(user);
				}
			}

			LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_Role_Enrolled_Users"));
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		} catch (NamingException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
	
		if(LOG.IsError())
        	return LOG.resultBean.Serialize();
		
        return ldapUnenrolledList.Serialize();
	}
	
	public String GetRoleUnEnrolledLDAPUsers(int role_id, String searchPattern) {
		LOG.resultBean.Reset();
		
		LDAPManager ldapMng = new LDAPManager();
		
		ArrayList<String> userList = new ArrayList<String>();
		BeanList<AltUserBean> ldapUnenrolledList = new BeanList<AltUserBean>();

		try{
			ArrayList<String> ldapUsers = ldapMng.GetLDAPUsers(searchPattern);
			
			OpenConn();
			
			// first get unenrolled ldap users
			String q = "select u.userid, u.username from smdbadmin.user_roles ur left join smdbadmin.users u on (u.userid=ur.user_id) where role_id = ?;";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, role_id);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				userList.add(rs.getString("username"));
			}
			rs.close();
			statement.close();
			
			for(String luser : ldapUsers) {
				if(!userList.contains(luser)) {
					AltUserBean user=new AltUserBean();	
					user.userid.set(luser);
					user.username.set(luser);
					ldapUnenrolledList.add(user);
				}
			}

			LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_Role_Enrolled_Users"));
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		} catch (NamingException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
	
		if(LOG.IsError())
        	return LOG.resultBean.Serialize();
		
		LOG.Debug("returning users: " + ldapUnenrolledList.Serialize());
        return ldapUnenrolledList.Serialize();
	}
	
	public String GetRoleUnEnrolledUsers(int role_id) {
		LOG.resultBean.Reset();
		
		if(RM.GetServerProperty("authentication").equals("ldap")) {
			String sss = GetRoleUnEnrolledLDAPUsers(role_id);
			LOG.Debug(sss);
			return sss;
			
		} else {
			BeanList<UserBean> userList = new BeanList<UserBean>();
			try{
				OpenConn();
				String q = "select u.userid, u.username from smdbadmin.users u where u.userid not in (select user_id from smdbadmin.user_roles where role_id = ?);";
				PreparedStatement statement = conn.prepareStatement(q);
				statement.setInt(1, role_id);
				ResultSet rs = statement.executeQuery();
				while(rs.next()){
					UserBean user=new UserBean();	
					user.userid.set(rs.getInt("userid"));
					user.username.set(rs.getString("username"));
					userList.add(user);
				}
				rs.close();
				statement.close();
				LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_Role_UnEnrolled_Users"));
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
	}
	
	public String GetRoleUnEnrolledUsers(int role_id, String searchPattern) {
		LOG.resultBean.Reset();
		
		if(RM.GetServerProperty("authentication").equals("ldap")) {
			String sss = GetRoleUnEnrolledLDAPUsers(role_id, searchPattern);
			LOG.Debug("this should be the result: " + sss);
			return sss;
			
		} else {
			BeanList<UserBean> userList = new BeanList<UserBean>();
			try{
				OpenConn();
				String q = "select u.userid, u.username from smdbadmin.users u where u.userid not in (select user_id from smdbadmin.user_roles where role_id = ?);";
				PreparedStatement statement = conn.prepareStatement(q);
				statement.setInt(1, role_id);
				ResultSet rs = statement.executeQuery();
				while(rs.next()){
					UserBean user=new UserBean();	
					user.userid.set(rs.getInt("userid"));
					user.username.set(rs.getString("username"));
					userList.add(user);
				}
				rs.close();
				statement.close();
				LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_Role_UnEnrolled_Users"));
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
	}
	
	public String GetRoleEnrolledGroups(int role_id) {
		LOG.resultBean.Reset();
		BeanList<GroupBean> groupList = new BeanList<GroupBean>();
		try{
			OpenConn();
			String q = "select g.group_id, g.group_name from smdbadmin.group_roles gr left join smdbadmin.groups g on (g.group_id=gr.group_id) where role_id = ?;";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, role_id);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				GroupBean group=new GroupBean();	
				group.group_id.set(rs.getString("group_id"));
				group.group_name.set(rs.getString("group_name"));
				groupList.add(group);
			}
			rs.close();
			statement.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_Role_Enrolled_Groups"));
		} catch (SQLException e) {
			LOG.Error(e);
		}catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return groupList.Serialize();
	}
	
	public String GetRoleUnEnrolledGroups(int role_id) {
		LOG.resultBean.Reset();
		BeanList<GroupBean> groupList = new BeanList<GroupBean>();
		try{
			OpenConn();
			String q = "select g.group_id, g.group_name from smdbadmin.groups g where g.group_id not in (select group_id from smdbadmin.group_roles where role_id = ?);";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, role_id);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				GroupBean group=new GroupBean();	
				group.group_id.set(rs.getString("group_id"));
				group.group_name.set(rs.getString("group_name"));
				groupList.add(group);
			}
			rs.close();
			statement.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_Role_UnEnrolled_Groups"));
		} catch (SQLException e) {
			LOG.Error(e);
		}catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return groupList.Serialize();
	}
    
    public String GetRoleResourcePermissions(int role_id)
    {
    	LOG.resultBean.Reset();
    	
    	RoleResourcePermBean roleResPerms = new RoleResourcePermBean();
        try {
			OpenConn();

			String q = "select r.role_id, r.role_name, rt.resource_type_id, rt.resource_type,";
			q += " pt.permission_type_id, pt.permission_type from smdbadmin.role_resource_permissions p";
			q += " left join smdbadmin.roles r on(r.role_id=p.role_id)";
			q += " left join smdbadmin.resource_types rt on(rt.resource_type_id=p.resource_type_id)";
			q += " left join smdbadmin.permission_types pt on(pt.permission_type_id=p.permission_type_id)";
			q += " where p.role_id=" + role_id;
	        PreparedStatement pst = conn.prepareStatement(q);
	        ResultSet rs = pst.executeQuery();

	        roleResPerms.role_id.set(Integer.toString(role_id));
	        while(rs.next()) {

	        	roleResPerms.role_name.set(rs.getString(2));

	        	ResPerm resPerm = new ResPerm();
	        	resPerm.resource_type_id.set(rs.getString(3));
	        	resPerm.resource_type.set(rs.getString(4));
	        	resPerm.permission_type_id.set(rs.getString(5));
	        	resPerm.permission_type.set(rs.getString(6));
	        	roleResPerms.ResourcePermissions.add(resPerm);
	        }

	        rs.close();
	        pst.close();
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        return roleResPerms.Serialize();
    }
    
    public String GetRoleServicePermissions(int role_id)
    {
    	RoleSrvPermBean roleSrvPerms = new RoleSrvPermBean();
        try {
			OpenConn();

			String q = "select r.role_id, r.role_name, p.service_instance_id, i.service_instance_name,";
			q += " pt.permission_type_id, pt.permission_type from smdbadmin.role_resource_permissions p";
			q += " left join smdbadmin.roles r on(r.role_id=p.role_id)";
			q += " left join smdbadmin.service_instances i on(i.citype='CI.BUSINESSMAINSERVICE' and i.service_instance_id=p.service_instance_id)";
			q += " left join smdbadmin.permission_types pt on(pt.permission_type_id=p.permission_type_id)";
			q += " where p.role_id=" + role_id;
	        PreparedStatement pst = conn.prepareStatement(q);
	        ResultSet rs = pst.executeQuery();
	        
	        roleSrvPerms.role_id.set(Integer.toString(role_id));
	        while(rs.next()) {
	        	roleSrvPerms.role_name.set(rs.getString(2));

	        	SrvPerm srvPerm = new SrvPerm();
	        	srvPerm.service_instance_id.set(rs.getString(1));
	        	String srvName = "All";
	        	if(rs.getString(4) != null)
	        		srvName = Helper.EscapeJsonStr(rs.getString(4));
	        	srvPerm.service_instance_name.set(srvName);
	        	srvPerm.permission_type_id.set(rs.getString(5));
	        	srvPerm.permission_type.set(rs.getString(6));
	        	roleSrvPerms.ServicePermissions.add(srvPerm);
	        }

	        rs.close();
	        pst.close();
	        
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return roleSrvPerms.Serialize();
    }
    
	public String GetRoleEnrolledServices(int role_id) {
		LOG.resultBean.Reset();
		BeanList<ServiceBean> serviceList = new BeanList<ServiceBean>();
		try{
			OpenConn();
			String q = "select rsp.service_instance_id, si.service_instance_name from smdbadmin.role_service_permissions rsp";
			q += " left join smdbadmin.service_instances si on (rsp.service_instance_id=si.service_instance_id and si.citype='CI.BUSINESSMAINSERVICE')";
			q += " where rsp.role_id=? order by service_instance_name asc";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, role_id);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				ServiceBean service=new ServiceBean();	
				service.service_instance_id.set(Integer.parseInt(rs.getString("service_instance_id")));
				service.service_instance_name.set(rs.getString("service_instance_name"));
				serviceList.add(service);
			}
			rs.close();
			statement.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_Role_Enrolled_Services"));
		} catch (SQLException e) {
			LOG.Error(e);
		}catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return serviceList.Serialize();
	}
	
	public String GetRoleUnEnrolledServices(int role_id) {
		LOG.resultBean.Reset();
		BeanList<ServiceBean> serviceList = new BeanList<ServiceBean>();
		try{
			OpenConn();;
			String q = "select * from smdbadmin.service_instances where citype='CI.BUSINESSMAINSERVICE' and service_instance_id not in";
				q += " (select si.service_instance_id from smdbadmin.service_instances si";
				q += " left join smdbadmin.role_service_permissions rsp on (rsp.service_instance_id=si.service_instance_id)";
				q += " where si.citype='CI.BUSINESSMAINSERVICE' and rsp.role_id=?) order by service_instance_name asc";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, role_id);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				ServiceBean service=new ServiceBean();	
				service.service_instance_id.set(Integer.parseInt(rs.getString("service_instance_id")));
				service.service_instance_name.set(rs.getString("service_instance_name"));
				serviceList.add(service);
			}
			rs.close();
			statement.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_Role_UnEnrolled_Services"));
		} catch (SQLException e) {
			LOG.Error(e);
		}catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return serviceList.Serialize();
	}
    
    public String DeleteRole(int roleid) {
		LOG.resultBean.Reset();
		try{
			OpenConn();
			String q = "delete from smdbadmin.roles where role_id=?;"
					+ "delete from smdbadmin.user_roles where role_id=?;"
					+ "delete from smdbadmin.group_roles where role_id=?;"				
					+ "delete from smdbadmin.role_resource_permissions where role_id=?;"
					+"delete from smdbadmin.role_service_permissions where role_id=?;";
			PreparedStatement stmt = conn.prepareStatement(q);
			stmt.setInt(1, roleid);
			stmt.setInt(2, roleid);
			stmt.setInt(3, roleid);
			stmt.setInt(4, roleid);
			stmt.setInt(5, roleid);
			stmt.executeUpdate();
			stmt.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Deleted_Role"));
			
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
