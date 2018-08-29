package com.gizit.bsm.dao;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

import com.gizit.managers.ConnectionManager;
import com.gizit.managers.LogManager;
import com.gizit.managers.ResourceManager;
import com.gizit.bsm.beans.UserBean;
import com.gizit.bsm.beans.RoleBean;
import com.gizit.bsm.beans.GroupBean;
import com.gizit.bsm.beans.BeanList;


public class UsersDAO extends DAO{ 
	
	public UsersDAO() {
		super(UsersDAO.class);
	}
	
	public UserBean login(UserBean user) {
		LOG.resultBean.Reset();
		
		ResultSet rs = null;
		PreparedStatement stmt = null; 
		String username = user.username.get(); 
		String password = user.password.get(); 
		String searchQuery = "select * from smdbadmin.users where username=? AND password=?"; 
		try {
			OpenConn();
			user.username.set(username);
			user.password.set(password);
			stmt = conn.prepareStatement(searchQuery);
			stmt.setString(1, username);
			stmt.setString(2, password);
			rs = stmt.executeQuery();
			boolean more = rs.next();
			// if user does not exist set the isValid variable to false 
			if (!more) { 
				LOG.Debug("Sorry, you are not a registered user! Please sign up first");  
				user.valid.set(false); 
			} 
			//if user exists set the isValid variable to true 
			else { 
				String firstName = rs.getString("FirstName"); 
				String lastName = rs.getString("LastName");
				user.firstName.set(firstName); 
				user.lastName.set(lastName); 
				user.valid.set(true); 
				LOG.Success(RM.GetErrorString("SUCCESS_Login_User"));
			} 
			rs.close();
			stmt.close();
				
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		return user; 
   }
	
	public String ChangePassw(String username, String password){
		LOG.resultBean.Reset();

		//String username = user.username.get(); 
		//String password = user.password.get();
		try {
			OpenConn();

			String q = "update smdbadmin.users set password=?";
			q += " where username=?";

			LOG.Debug("Update users: " + q);
			
			PreparedStatement stmt = conn.prepareStatement(q);
			stmt.setString(1, password);
			stmt.setString(2, username);
        
			stmt.executeUpdate();
			stmt.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Change_Passw_User"));
		} catch (SQLException e) {
			LOG.Error(e);
		}
		catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

		return LOG.resultBean.Serialize();
	}
	
	public String AddUser(String username, String firstname, String lastname, String password){
		LOG.resultBean.Reset();
		
		/*int userid = user.userid.get();
		String username = user.username.get(); 
		String firstname = user.firstName.get();
		String lastname = user.lastName.get(); 
		String password = user.password.get();*/

		try {
			OpenConn();
			
			String q = "INSERT INTO smdbadmin.users(username, password, firstname, lastname)";
				   q += "VALUES (?, ?, ?, ?)";	
			
			PreparedStatement stmt = conn.prepareStatement(q);
			
			stmt.setString(1, username);
			stmt.setString(2, password);
			stmt.setString(3, firstname);
			stmt.setString(4, lastname);
        
			stmt.executeUpdate();
			stmt.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Created_User"));
			
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		return LOG.resultBean.Serialize();
	}
	
	public String UpdateUser(int userid, String username, String firstname, String lastname, String password){
		LOG.resultBean.Reset();

		try {
			int usrid = 0;
			OpenConn();
			
			String q = "select userid from smdbadmin.users where userid=?";
						
			PreparedStatement stmts = conn.prepareStatement(q);
			stmts.setInt(1,userid);
	        ResultSet rs = stmts.executeQuery();
	        
	        while(rs.next()) {
	        	usrid=1;
	        }
	        rs.close();
			stmts.close();
			
			
			if(usrid > 0) {
				q = "UPDATE smdbadmin.users SET username=?, password=?, firstname=?, lastname=?";
				q +=" WHERE userid=?";
			}
			else {
				q = "INSERT INTO smdbadmin.users(username, password, firstname, lastname)";
				q += "VALUES (?, ?, ?, ?)";	
			}
			
			PreparedStatement stmt = conn.prepareStatement(q);
			
			stmt.setString(1, username);
			stmt.setString(2, password);
			stmt.setString(3, firstname);
			stmt.setString(4, lastname);
			
			if(usrid > 0) {
				stmt.setInt(5, userid);
			}
        
			stmt.executeUpdate();
			stmt.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Updated_User"));
			
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		return LOG.resultBean.Serialize();
	}
	
	public String DeleteUser(int userid) {
		LOG.resultBean.Reset();
		try{
			OpenConn();
			String q = "delete from smdbadmin.users where userid=?;"
					+ "delete from smdbadmin.user_roles where user_id=?;"
					+"delete from smdbadmin.user_groups where user_id=?;";
			PreparedStatement stmt = conn.prepareStatement(q);
			stmt.setInt(1, userid);
			stmt.setInt(2, userid);
			stmt.setInt(3, userid);
			stmt.executeUpdate();
			stmt.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Deleted_User"));
			
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		return LOG.resultBean.Serialize();
	}

	public String GetUser(int userid) {
		LOG.resultBean.Reset();
		UserBean user=new UserBean();
		try{
			OpenConn();
			String q = "select * from smdbadmin.users where userid=?;";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, userid);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				user.userid.set(rs.getInt("userid"));
				user.username.set(rs.getString("username"));
				user.firstName.set(rs.getString("firstname"));
				user.lastName.set(rs.getString("lastname"));
				user.password.set(rs.getString("password"));
			}
			rs.close();
			statement.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Retreived_User"));
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return user.Serialize();
	}
	
	public String getUserAfterInsert(String username,String firstname, String lastname) {
		LOG.resultBean.Reset();
		UserBean user=new UserBean();
		try{
			OpenConn();
			String q = "select * from smdbadmin.users where username=? and firstname=? and lastname = ?;";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setString(1, username);
			statement.setString(2, firstname);
			statement.setString(3, lastname);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				user.userid.set(rs.getInt("userid"));
				user.username.set(rs.getString("username"));
				user.firstName.set(rs.getString("firstname"));
				user.lastName.set(rs.getString("lastname"));
				user.password.set(rs.getString("password"));
			}
			rs.close();
			statement.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Retreived_User"));
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return user.Serialize();
	}
	
	public String GetAllUsers() {
		LOG.resultBean.Reset();
		BeanList<UserBean> userList = new BeanList<UserBean>();
		try{
			OpenConn();
			String q = "select * from smdbadmin.users;";
			PreparedStatement statement = conn.prepareStatement(q);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				UserBean user=new UserBean();
				user.userid.set(rs.getInt("userid"));
				user.username.set(rs.getString("username"));
				user.firstName.set(rs.getString("firstname"));
				user.lastName.set(rs.getString("lastname"));
				user.password.set(rs.getString("password"));
				userList.add(user);
			}
			rs.close();
			statement.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Retreived_UserList"));
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
	
	public String GetUserEnrolledRoles(int userid) {
		LOG.resultBean.Reset();
		BeanList<RoleBean> roleList = new BeanList<RoleBean>();
		try{
			OpenConn();
			String q = "select r.role_id, r.role_name from smdbadmin.user_roles ur left join smdbadmin.roles r on (r.role_id=ur.role_id) where user_id = ?;";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, userid);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				RoleBean role=new RoleBean();	
				role.role_id.set(rs.getString("role_id"));
				role.role_name.set(rs.getString("role_name"));
				roleList.add(role);
			}
			rs.close();
			statement.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_User_Enrolled_Roles"));
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
	
	public String GetUserUnEnrolledRoles(int userid) {
		LOG.resultBean.Reset();
		BeanList<RoleBean> roleList = new BeanList<RoleBean>();
		try{
			OpenConn();
			String q = "select r.role_id, r.role_name from smdbadmin.roles r where r.role_id not in (select role_id from smdbadmin.user_roles where user_id = ?);";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, userid);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				RoleBean role=new RoleBean();	
				role.role_id.set(rs.getString("role_id"));
				role.role_name.set(rs.getString("role_name"));
				roleList.add(role);
			}
			rs.close();
			statement.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_User_UnEnrolled_Roles"));
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
	
	public String GetUserEnrolledGroups(int userid) {
		LOG.resultBean.Reset();
		BeanList<GroupBean> groupList = new BeanList<GroupBean>();
		try{
			OpenConn();
			String q = "select g.group_id, g.group_name from smdbadmin.user_groups ug left join smdbadmin.groups g on (g.group_id=ug.group_id) where user_id = ?;";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, userid);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				GroupBean group=new GroupBean();	
				group.group_id.set(rs.getString("group_id"));
				group.group_name.set(rs.getString("group_name"));
				groupList.add(group);
			}
			rs.close();
			statement.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Retrieved_User_Enrolled_Groups"));
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
	
	public String GetUserUnEnrolledGroups(int userid) {
		LOG.resultBean.Reset();
		BeanList<GroupBean> groupList = new BeanList<GroupBean>();
		try{
			OpenConn();
			String q = "select g.group_id, g.group_name from smdbadmin.groups g where g.group_id not in (select group_id from smdbadmin.user_groups where user_id = ?);";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, userid);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				GroupBean group=new GroupBean();	
				group.group_id.set(rs.getString("group_id"));
				group.group_name.set(rs.getString("group_name"));
				groupList.add(group);
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
        return groupList.Serialize();		
	}
	
	public String UpdateUserRoles(int userid, String[] roleList){
		LOG.resultBean.Reset();
		try{
			OpenConn();
			String q = "delete from smdbadmin.user_roles where user_id = ?;";
			PreparedStatement stmt = conn.prepareStatement(q);
			stmt.setInt(1, userid);
			stmt.executeUpdate();
			stmt.close();
			
			q = "INSERT INTO smdbadmin.user_roles(user_id, role_id)";
			q += " VALUES (?, ?);";
			
			LOG.Warn("RoleList:"+roleList);
	    	PreparedStatement stmt2 = conn.prepareStatement(q);
			for(int i=0; i<roleList.length; i++) {
	    		stmt2.setInt(1, userid);
	    		stmt2.setInt(2, Integer.parseInt(roleList[i]));
	    		stmt2.executeUpdate();
	    	}
			stmt2.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Updated_User_Roles"));
		} catch (SQLException e) {
			LOG.Error(e);
		}catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		return LOG.resultBean.Serialize();
		
	}
	
	public String UpdateUserGroups(int userid, String[] groupList){
		LOG.resultBean.Reset();
		try{
			OpenConn();
			String q = "delete from smdbadmin.user_groups where user_id = ?;";
			PreparedStatement stmt = conn.prepareStatement(q);
			stmt.setInt(1, userid);
			stmt.executeUpdate();
			stmt.close();
			
			q = "INSERT INTO smdbadmin.user_groups(user_id, group_id)";
			q += " VALUES (?, ?);";
			
			PreparedStatement stmt2 = conn.prepareStatement(q);
			for(int i=0; i<groupList.length; i++) {
	    		String[] roleParts = groupList[i].split(":");
	    		stmt2.setInt(1, userid);
	    		stmt2.setInt(2, Integer.parseInt(roleParts[0]));
	    		stmt2.executeUpdate();
	    	}
			stmt2.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Updated_User_Groups"));
			
		} catch (SQLException e) {
			LOG.Error(e);
		}catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		return LOG.resultBean.Serialize();
		
	}
	
	public boolean UserExists(String username) {
		String q = "select count(*) cnt from smdbadmin.users u right join smdbadmin.user_roles r on(r.user_id=u.userid) where u.username=?";
		try {
			OpenConn();
			
			PreparedStatement stmt = conn.prepareStatement(q);
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			int cnt = 0;
			if(rs.getInt("cnt") > 0)
				cnt = 1;
			rs.close();
			stmt.close();
			
			if(cnt > 0)
				return true;
			
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		return false;
	}
	
}
