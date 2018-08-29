package gbsm.gizit.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gizit.bsm.helpers.Helper;
import com.gizit.managers.ConnectionManager;
import com.gizit.managers.LogManager;

public class Authorization {
	
	public static class PermissionType {
		public static final String EDIT = "EDIT";
		public static final String VIEW = "VIEW";
		public static final String[] HouseOfPermissions = {"EDIT", "VIEW"};
	}
	
	public static class ResourceType {
		public static final String Datasource = "Datasource";
		public static final String KPI = "KPI";
		public static final String Report = "Report";
		public static final String Service = "Service";
		public static final String Administration = "Administration";
		public static final String[] HouseOfResources = {"Datasource", "KPI", "Report", "Service", "Administration"};
	}
	
	String username = "";
	public Permission permission;
	LogManager LOG;
	public Authorization(String user) {
		LOG = new LogManager(Authorization.class);
		username = user;
		permission = new Permission(username);
	}
	
	public class Permission{
		String username = "";
		
		public List<Integer> Groups;
		List<Integer> GroupRoles;
		List<Integer> UserRoles;

		public HashMap<Integer, String> ServicePermissions;
		HashMap<String, String> ResourcePermissions;
		
		Connection conn;
	    private void OpenConn()
	    {
			conn = connectionManager.GetSMDBConnection();
	    }
	    
	    private void CloseConn()
	    {
	    	connectionManager.CloseConnection(conn);
	    }
	    
	    ConnectionManager connectionManager;
		public Permission(String user) {
			username = user;

			connectionManager = new ConnectionManager();

			OpenConn();

			Groups = GetGroups();
			GroupRoles = GetGroupRoles();
			UserRoles = GetUserRoles();
			
			ServicePermissions = GetAuthorizedServiceList();
			ResourcePermissions = GetAuthorizedResourceList();
			
			CloseConn();
		}
	    
		private List<Integer> GetGroups(){
			List<Integer> groupList = new ArrayList<Integer>();
			String q = "select g.group_id from smdbadmin.user_groups g";
			q += " left join smdbadmin.users u on(u.userid=g.user_id) where u.username=?";
			try {
				PreparedStatement st = conn.prepareStatement(q);
				st.setString(1, username);
				ResultSet rs = st.executeQuery();

				while(rs.next()) {
					groupList.add(rs.getInt(1));
				}
				
				rs.close();
				st.close();
			} catch (SQLException e) {
				LOG.Error(e);
			}

			return groupList;
		}
		private List<Integer> GetGroupRoles(){
			List<Integer> groupRoles = new ArrayList<Integer>();

			if(Groups.size() > 0) {
				String gidList = Helper.joinInts((Integer[])Groups.toArray(new Integer[Groups.size()]), ",");
				
				String q = "select role_id from smdbadmin.group_roles where group_id in(" + gidList + ")";
				try {
					PreparedStatement st = conn.prepareStatement(q);
					ResultSet rs = st.executeQuery();
					
					while(rs.next()) {
						groupRoles.add(rs.getInt(1));
					}
					
					rs.close();
					st.close();
				} catch (SQLException e) {
					LOG.Error(e);
				}
			}
			return groupRoles;
		}
		private List<Integer> GetUserRoles(){
			List<Integer> userRoles = new ArrayList<Integer>();
			
			String q = "select r.role_id from smdbadmin.user_roles r";
			q += " left join smdbadmin.users u on(u.userid=r.user_id) where u.username=?";
			try {
				PreparedStatement st = conn.prepareStatement(q);
				st.setString(1, username);
				ResultSet rs = st.executeQuery();
				
				while(rs.next()) {
					userRoles.add(rs.getInt(1));
				}
				
				rs.close();
				st.close();
			} catch (SQLException e) {
				LOG.Error(e);
			}
			
			return userRoles;
		}
		
		private HashMap<Integer, String> GetAuthorizedServiceList(){
			HashMap<Integer, String> srvPermissions = new HashMap<Integer, String>();
			
			// build list of role ids from user+group roles
			String roleList = "";
			if(GroupRoles.size() > 0)
				roleList += Helper.joinInts((Integer[])GroupRoles.toArray(new Integer[GroupRoles.size()]), ",");
			
			if(UserRoles.size() > 0) {
				if(GroupRoles.size() > 0)
					roleList += ",";
				roleList += Helper.joinInts((Integer[])UserRoles.toArray(new Integer[UserRoles.size()]), ",");	
			}
			
			if(UserRoles.size() > 0 || GroupRoles.size() > 0) {
				String q = "select p.service_instance_id, t.permission_type from smdbadmin.role_service_permissions p";
				q += " left join smdbadmin.permission_types t on(p.permission_type_id=t.permission_type_id)";
				q += " where p.role_id in(" + roleList + ")";
				try {
					PreparedStatement st = conn.prepareStatement(q);
					ResultSet rs = st.executeQuery();
	
					while(rs.next()) {
						srvPermissions.put(rs.getInt("service_instance_id"), rs.getString("permission_type"));
					}
					
					rs.close();
					st.close();
				} catch (SQLException e) {
					LOG.Error(e);
				}
			}

			return srvPermissions;
		}
		
		private HashMap<String, String> GetAuthorizedResourceList(){
			HashMap<String, String> resPermissions = new HashMap<String, String>();
			
			// build list of role ids from user+group roles
			String roleList = "";
			if(GroupRoles.size() > 0)
				roleList += Helper.joinInts((Integer[])GroupRoles.toArray(new Integer[GroupRoles.size()]), ",");
			
			if(UserRoles.size() > 0) {
				if(GroupRoles.size() > 0)
					roleList += ",";
				roleList += Helper.joinInts((Integer[])UserRoles.toArray(new Integer[UserRoles.size()]), ",");	
			}
			if(UserRoles.size() > 0 || GroupRoles.size() > 0) {
				String q = "select r.resource_type, t.permission_type from smdbadmin.role_resource_permissions p";
				q += " left join smdbadmin.permission_types t on(p.permission_type_id=t.permission_type_id)";
				q += " left join smdbadmin.resource_types r on(r.resource_type_id=p.resource_type_id)";
				q += " where p.role_id in(" + roleList + ")";
				try {
					PreparedStatement st = conn.prepareStatement(q);
					ResultSet rs = st.executeQuery();
	
					while(rs.next()) {
						String res = rs.getString("resource_type");
						if(!resPermissions.containsKey(res)) {
							resPermissions.put(res, rs.getString("permission_type"));
						} else {
							// overwrite view permission
							if(resPermissions.get(res).equals(PermissionType.VIEW))
								resPermissions.put(res, rs.getString("permission_type"));
						}
					}
					
					rs.close();
					st.close();
				} catch (SQLException e) {
					LOG.Error(e);
				}
			}
			return resPermissions;
		}
		
		private String ServiceAuthorizedPermission(int sid) {
			if(AdministrationAuthorized())
				return PermissionType.EDIT;
			if(ServicePermissions.containsKey(0) && !ServicePermissions.containsKey(sid))
				return ServicePermissions.get(0);
			return ServicePermissions.get(sid);
		}

		private String ResourceAuthorizedPermission(String rt) {
			return ResourcePermissions.get(rt);
		}

		// service permissions
		public boolean ViewServiceAuthorized(int sid) {
			//return true;
			return ServiceAuthorizedPermission(sid).equals(PermissionType.VIEW) || EditServiceAuthorized(sid) || AdministrationAuthorized();
		}
		public boolean EditServiceAuthorized(int sid) {
			//return true;
			return ServiceAuthorizedPermission(sid).equals(PermissionType.EDIT) || AdministrationAuthorized();
		}

		// resource permissions
		public boolean ViewDatasourceAuthorized() {
			if(ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.Datasource) == null)
				return false || AdministrationAuthorized();
			return ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.Datasource).equals(PermissionType.VIEW) || EditDatasourceAuthorized() || AdministrationAuthorized();
		}
		public boolean EditDatasourceAuthorized() {
			if(ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.Datasource) == null)
				return false || AdministrationAuthorized();
			return ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.Datasource).equals(PermissionType.EDIT) || AdministrationAuthorized();
		}
		
		public boolean ViewKPIAuthorized() {
			if(ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.KPI) == null)
				return false || AdministrationAuthorized();
			return ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.KPI).equals(PermissionType.VIEW) || EditKPIAuthorized() || AdministrationAuthorized();
		}
		public boolean EditKPIAuthorized() {
			System.out.println("KPIIII: " + ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.KPI));
			if(ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.KPI) == null)
				return false || AdministrationAuthorized();
			return ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.KPI).equals(PermissionType.EDIT) || AdministrationAuthorized();
		}
		
		public boolean ViewReportAuthorized() {
			if(ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.Report) == null)
				return false || AdministrationAuthorized();
			return ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.Report).equals(PermissionType.VIEW) || EditReportAuthorized() || AdministrationAuthorized();
		}
		public boolean EditReportAuthorized() {
			if(ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.Report) == null)
				return false || AdministrationAuthorized();
			return ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.Report).equals(PermissionType.EDIT) || AdministrationAuthorized();
		}

		public boolean ViewSrvResourceAuthorized() {
			if(ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.Service) == null)
				return false || AdministrationAuthorized();
			return ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.Service).equals(PermissionType.VIEW) || EditSrvResourceAuthorized() || AdministrationAuthorized();
		}
		public boolean EditSrvResourceAuthorized() {
			if(ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.Service) == null)
				return false || AdministrationAuthorized();
			return ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.Service).equals(PermissionType.EDIT) || AdministrationAuthorized();
		}
		
		public boolean AdministrationAuthorized() {
			if(ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.Administration) == null)
				return false;
			return ResourceAuthorizedPermission(gbsm.gizit.security.Authorization.ResourceType.Administration).equals(PermissionType.EDIT);
		}
	}

}
