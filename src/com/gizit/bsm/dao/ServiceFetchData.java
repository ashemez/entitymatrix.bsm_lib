package com.gizit.bsm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.gizit.bsm.beans.Service;
import com.gizit.bsm.dao.ChartDAO;
import com.gizit.bsm.dao.ReportDAO;
import com.gizit.bsm.helpers.Helper;
import com.gizit.managers.ConnectionManager;

public class ServiceFetchData {
	
	private static String CurrentTimeWindow() {
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR);
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		String dateInString = day + "-" + month + "-" + year + " 00:00:00";

		long de = 0;
		try {
			Date date = sdf.parse(dateInString);
			de = date.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String destr = String.valueOf(de);
		
		destr = destr.substring(0, destr.length() - 3);
		
		return destr;
    }
	
	public static ArrayList<Service> getAllServices(int page, int[] statusFilter, HashMap<Integer, String> servicePermissions, String spattern, boolean isAdmin) {
		ConnectionManager connectionManager = new ConnectionManager();
		//ConnectionDB connectiondb = new ConnectionDB();
		//Connection connection = connectiondb.openConnection();
		System.out.println(" ***  isadmin: " + isAdmin);
		System.out.println(" ***   servicePermissions.containsKey(0): " + servicePermissions.containsKey(0));
		
		String allowedSrvList = "";
		// sid 0 means all services are allowed
		if(!isAdmin || !servicePermissions.containsKey(0)) {
			int scnt = 0;
			boolean sidzerocheck = false;
			for(int sid : servicePermissions.keySet()) {
				if(sid == 0) {
					sidzerocheck = true;
				}
				if(scnt > 0)
					allowedSrvList += ",";
				
				allowedSrvList += Integer.toString(sid);
				
				scnt++;
			}
			if(sidzerocheck == true) {
				allowedSrvList = "0";
			}
		}
		System.out.println(" ***  allowedsrvlist: " + allowedSrvList);
		System.out.println(" ***  allowedSrvList.trim().equals(\"0\"): " + allowedSrvList.trim().equals("0"));
		System.out.println("spattern: " + spattern);
		ArrayList<Service> serviceList = new ArrayList<Service>();
		
		if(isAdmin || servicePermissions.size() > 0) {

			Connection connection = connectionManager.GetSMDBConnection();
			ChartDAO chartDAO = new ChartDAO();
			
			try{
				int offset = page * 12;
				//Statement statement = connection.createStatement();
				String q = "select i.service_instance_id, i.service_instance_name, i.service_instance_displayname, i.service_template_id,";
				q += " i.current_status, i.citype, i.propagate, i.status_timestamp, a.availability_metric";
				q += " from smdbadmin.service_instances i left join smdbadmin.daily_availability a";
				q += " on a.daily_availability_id in(select daily_availability_id from smdbadmin.daily_availability";
				q += " where service_instance_id=i.service_instance_id and timewindow=? order by timewindow desc limit 1) where ";
				if((!isAdmin || !servicePermissions.containsKey(0)) && (!allowedSrvList.trim().equals("") && !allowedSrvList.trim().equals("0"))) {
					q += " i.service_instance_id in(" + allowedSrvList + ") and ";
				}
				q += " i.citype='CI.BUSINESSMAINSERVICE' ";
				q += " and i.current_status in (" + Helper.joinInts(statusFilter, ",") + ")";
				if(!spattern.equals(""))
					q += " and i.service_instance_name ilike '%" + spattern + "%'";
				q += " order by i.current_status desc, a.availability_metric asc, i.service_instance_name asc limit 12";
				q += " offset " + offset;
				System.out.println(" *** " + q + " spattern: " + spattern);
				PreparedStatement statement = connection.prepareStatement(q);
				statement.setInt(1, Integer.parseInt(CurrentTimeWindow()));
				ResultSet rs = statement.executeQuery();
				while(rs.next()){
					Service service=new Service();
					service.setService_instance_id(rs.getInt("service_instance_id"));
					service.setService_instance_name(rs.getString("service_instance_name"));
					service.setService_instance_displayname(rs.getString("service_instance_displayname"));
					service.setService_template_id(rs.getInt("service_template_id"));
					service.setCurrent_status(rs.getInt("current_status"));
					service.setCi_type(rs.getString("citype"));
					service.setPropagate(rs.getInt("propagate"));
					double metric = rs.getDouble("availability_metric");
					if(metric == 0L)
						metric = 100;
					service.setAvailabilityMetric(Double.parseDouble(chartDAO.GetAvailabilityMetric(rs.getInt("service_instance_id"), 0, 0, ReportDAO.QueryType.TODAY)));
					serviceList.add(service);
				}
				rs.close();
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				//connectiondb.closeConnection(connection);
				connectionManager.CloseConnection(connection);
			}

		}
		
		return serviceList;
	}
	
	public static ArrayList<Service> getAllSearchedServices(int[] statusFilter) {
		ConnectionManager connectionManager = new ConnectionManager();
		Connection connection = connectionManager.GetSMDBConnection();
		ArrayList<Service> serviceList = new ArrayList<Service>();
		ChartDAO chartDAO = new ChartDAO();
		try{
			//int offset = page * 12;
			//Statement statement = connection.createStatement();
			String q = "select i.service_instance_id, i.service_instance_name, i.service_instance_displayname, i.service_template_id," + 
					" i.current_status, i.citype, i.propagate, i.status_timestamp, a.availability_metric" + 
					" from smdbadmin.service_instances i left join smdbadmin.daily_availability a" + 
					" on a.daily_availability_id in(select daily_availability_id from smdbadmin.daily_availability" + 
					" where service_instance_id=i.service_instance_id and timewindow=? order by timewindow desc limit 1)" + 
					" where i.citype='CI.BUSINESSMAINSERVICE' " + 
					" and i.current_status in (" + Helper.joinInts(statusFilter, ",") + ")"+
					" order by i.current_status desc, a.availability_metric asc, i.service_instance_name asc";
			PreparedStatement statement = connection.prepareStatement(q);
			statement.setInt(1, Integer.parseInt(CurrentTimeWindow()));
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				Service service=new Service();
				service.setService_instance_id(rs.getInt("service_instance_id"));
				service.setService_instance_name(rs.getString("service_instance_name"));
				service.setService_instance_displayname(rs.getString("service_instance_displayname"));
				service.setService_template_id(rs.getInt("service_template_id"));
				service.setCurrent_status(rs.getInt("current_status"));
				service.setCi_type(rs.getString("citype"));
				service.setPropagate(rs.getInt("propagate"));
				double metric = rs.getDouble("availability_metric");
				if(metric == 0L)
					metric = 100;
				service.setAvailabilityMetric(Double.parseDouble(chartDAO.GetAvailabilityMetric(rs.getInt("service_instance_id"), 0, 0, ReportDAO.QueryType.TODAY)));
				serviceList.add(service);
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			connectionManager.CloseConnection(connection);
		}

		return serviceList;
	}

	public static List<Integer> getServicesCategoryCount(HashMap<Integer, String> servicePermissions, String spattern, boolean isAdmin) {
		
		String allowedSrvList = "";
		// sid 0 means all services are allowed
		if((!isAdmin || !servicePermissions.containsKey(0))) {
			int scnt = 0;
			boolean sidzerocheck = false;
			for(int sid : servicePermissions.keySet()) {
				if(sid == 0) {
					sidzerocheck = true;
				}
				if(scnt > 0)
					allowedSrvList += ",";
				
				allowedSrvList += Integer.toString(sid);
				
				scnt++;
			}
			if(sidzerocheck == true) {
				allowedSrvList = "0";
			}
		}
		
		ConnectionManager connectionManager = new ConnectionManager();
		List<Integer> categoryList = new ArrayList<>();
		Integer goodcount = 0;
		Integer marginalcount = 0;
		Integer badcount = 0;
		
		if(isAdmin || servicePermissions.size() > 0) {
			Connection connection = connectionManager.GetSMDBConnection();
			try{
				String gq = "select count(*) from smdbadmin.service_instances where citype='CI.BUSINESSMAINSERVICE' and current_status = 0";
				if((!isAdmin || !servicePermissions.containsKey(0)) && (!allowedSrvList.trim().equals("") && !allowedSrvList.trim().equals("0"))) {
					gq += " and service_instance_id in(" + allowedSrvList + ") ";
				}
				if(!spattern.equals(""))
					gq += " and service_instance_name ilike '%" + spattern + "%'";
				PreparedStatement statement1 = connection.prepareStatement(gq);
				ResultSet rs1 = statement1.executeQuery();
				if(rs1.next()) {
					goodcount = rs1.getInt("count");
					//System.out.println("GoodCount:"+goodcount);
				}
				rs1.close();
				statement1.close();
				
				String aq = "select count(*) from smdbadmin.service_instances where citype='CI.BUSINESSMAINSERVICE' and current_status in (1,2,3,4)";
				if((!isAdmin || !servicePermissions.containsKey(0)) && (!allowedSrvList.trim().equals("") && !allowedSrvList.trim().equals("0"))) {
					aq += " and service_instance_id in(" + allowedSrvList + ") ";
				}
				if(!spattern.equals(""))
					aq += " and service_instance_name ilike '%" + spattern + "%'";
				PreparedStatement statement2 = connection.prepareStatement(aq);
				ResultSet rs2 = statement2.executeQuery();
				if(rs2.next()) {
					marginalcount = rs2.getInt("count");
					//System.out.println("MarginalCount:"+goodcount);
				}
				rs2.close();
				statement2.close();
				
				String bq = "select count(*) from smdbadmin.service_instances where citype='CI.BUSINESSMAINSERVICE' and current_status = 5";
				if((!isAdmin || !servicePermissions.containsKey(0)) && (!allowedSrvList.trim().equals("") && !allowedSrvList.trim().equals("0"))) {
					bq += " and service_instance_id in(" + allowedSrvList + ") ";
				}
				if(!spattern.equals(""))
					bq += " and service_instance_name ilike '%" + spattern + "%'";
				PreparedStatement statement3 = connection.prepareStatement(bq);
				ResultSet rs3 = statement3.executeQuery();
				if(rs3.next()) {
					badcount = rs3.getInt("count");
					//System.out.println("BadCount:"+goodcount);
				}
				rs3.close();
				statement3.close();
				categoryList.add(goodcount);
				categoryList.add(marginalcount);
				categoryList.add(badcount);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				
				//connectiondb.closeConnection(connection);
				connectionManager.CloseConnection(connection);
				
			}
		
		}
		
		return categoryList;
	}
	
	public static List<Integer> getSearchServicesCategoryCount(int[] sidlist) {
		ConnectionManager connectionManager = new ConnectionManager();
		Connection connection = connectionManager.GetSMDBConnection();
		List<Integer> categoryList = new ArrayList<>();
		Integer goodcount = 0;
		Integer marginalcount = 0;
		Integer badcount  = 0;
		try{
			String q = "select count(*) from smdbadmin.service_instances where citype='CI.BUSINESSMAINSERVICE' and current_status = 0" +
					" and service_instance_id in (" + Helper.joinInts(sidlist, ",") + ")";
			PreparedStatement statement1 = connection.prepareStatement(q);
			ResultSet rs1 = statement1.executeQuery();
			if(rs1.next()) {
				goodcount = rs1.getInt("count");
				//System.out.println("GoodCount:"+goodcount);
			}
			rs1.close();
			statement1.close();
			q = "select count(*) from smdbadmin.service_instances where citype='CI.BUSINESSMAINSERVICE' and current_status in (1,2,3,4)" +
					" and service_instance_id in (" + Helper.joinInts(sidlist, ",") + ")";
			PreparedStatement statement2 = connection.prepareStatement(q);
			ResultSet rs2 = statement2.executeQuery();
			if(rs2.next()) {
				marginalcount = rs2.getInt("count");
				//System.out.println("MarginalCount:"+goodcount);
			}
			rs2.close();
			statement2.close();
			q = "select count(*) from smdbadmin.service_instances where citype='CI.BUSINESSMAINSERVICE' and current_status = 5" +
					" and service_instance_id in (" + Helper.joinInts(sidlist, ",") + ")";
			PreparedStatement statement3 = connection.prepareStatement(q);
			ResultSet rs3 = statement3.executeQuery();
			if(rs3.next()) {
				badcount = rs3.getInt("count");
				//System.out.println("BadCount:"+goodcount);
			}
			rs3.close();
			statement3.close();
			categoryList.add(goodcount);
			categoryList.add(marginalcount);
			categoryList.add(badcount);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			connectionManager.CloseConnection(connection);
		}
		
		return categoryList;
	}

}
