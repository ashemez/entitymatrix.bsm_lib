package com.gizit.bsm.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import com.gizit.bsm.beans.BeanList;
import com.gizit.bsm.beans.DSBean;
import com.gizit.bsm.beans.KPIBean;
import com.gizit.bsm.beans.KPIRuleBean;
import com.gizit.bsm.beans.NodeGroupBean;
import com.gizit.bsm.beans.NodeGroupBean.Member;
import com.gizit.bsm.beans.OutputRuleBean;
import com.gizit.bsm.beans.ResultMessageBean;
import com.gizit.bsm.helpers.Helper;
import com.gizit.managers.ConnectionManager;
import com.gizit.managers.LogManager;
import com.gizit.managers.ResourceManager;

public class KPIDAO extends DAO {

	public KPIDAO() {
		super(KPIDAO.class);
	}
	
    public String SaveKPI(String name,
    		int sid,
    		String query,
    		int interval,
    		String timeUnit,
    		String thresholdMarginalRule,
    		String thresholdBadRule,
    		int dsid,
    		String username
    		)
    {
    	LOG.resultBean.Reset();
    	
        try {
        	int kpiId = 0;
        	
			OpenConn();
			Statement st = conn.createStatement();
	        String q = "select kpi_id from smdbadmin.kpi where kpi_name='"+name+"'";
	        ResultSet rs = st.executeQuery(q);
	        while(rs.next()) {
	        	kpiId = rs.getInt(1);
	        }
	        rs.close();
			st.close();
			
			if(kpiId > 0)
			{
				q = "update smdbadmin.kpi set service_instance_id='" + sid + "',";
				//q = "update smdbadmin.kpi set ";
				q += " kpi_query='" + query + "', interval='" + interval + "', time_unit='" + timeUnit + "', threshold_marginal_rule=?, threshold_bad_rule=?, datasource_id='" + dsid + "'";
				q += " where kpi_id=" + kpiId;
				
				PreparedStatement st1 = conn.prepareStatement(q);
				st1.setString(1, thresholdMarginalRule);
				st1.setString(2, thresholdBadRule);
				st1.executeUpdate();
				st1.close();
				
				LOG.Success(RM.GetErrorString("SUCCESS_Updated_KPI"));
				
			} else {
	        	q = "insert into smdbadmin.kpi(service_instance_id, kpi_name, kpi_query, interval, time_unit, threshold_marginal_rule, threshold_bad_rule, datasource_id, isrunning, createdby, createdat)";
		        q += " values("+sid+""
		        		+ ", '" + name + "', '" + query + "', " + interval + ", '" + timeUnit + "', ?, ?, " + dsid + ",0, (select userid from smdbadmin.users where username=?), EXTRACT(EPOCH FROM (select current_timestamp)))";
		        
		        PreparedStatement st1 = conn.prepareStatement(q);
				st1.setString(1, thresholdMarginalRule);
				st1.setString(2, thresholdBadRule);
				st1.setString(3, username);
				st1.executeUpdate();
				st1.close();
				
				LOG.Success(RM.GetErrorString("SUCCESS_Created_KPI"));
			}

		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        return LOG.resultBean.Serialize();
    }
    
    public String UpdateRunKpi(int kpiId, int isrunning) {
    	LOG.resultBean.Reset();
    	
        try {
        	
			OpenConn();
			
			Statement st1 = conn.createStatement();
			
			String q = "update smdbadmin.kpi set isrunning='" + isrunning + "'";
			q += " where kpi_id=" + kpiId;

			st1.executeUpdate(q);
			st1.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Updated_KPI"));
			
		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        return LOG.resultBean.Serialize();
    }
    
    public String GetKPI(int kpiId)
    {
    	LOG.resultBean.Reset();
    	
    	KPIBean kpiBean = new KPIBean();
    	
        try {
			OpenConn();

	        Statement st = conn.createStatement();
	        String q = "select kpi.kpi_id, kpi.service_instance_id, kpi.kpi_name, kpi.kpi_query,";
	        q += " kpi.interval, kpi.time_unit, kpi.last_ran, kpi.threshold_marginal_rule, kpi.threshold_bad_rule,";
	        q += " kpi.datasource_id,kpi.isrunning, ds.datasource_name, last_ran, last_measured_value from smdbadmin.kpi kpi"; 
	        q += " left join smdbadmin.datasources ds on kpi.datasource_id = ds.datasource_id";
	        q+= " where kpi.kpi_id=" + kpiId;
	        ResultSet rs = st.executeQuery(q);

	        while(rs.next()){
	        	kpiBean.kpiid.set(rs.getString(1));
	        	kpiBean.sid.set(rs.getString(2));
	        	kpiBean.kpiname.set(rs.getString(3));
	        	kpiBean.kpiquery.set(rs.getString(4));
	        	kpiBean.interval.set(rs.getString(5));
	        	kpiBean.timeunit.set(rs.getString(6));
	        	kpiBean.thrsmarrule.set(rs.getString(8));
	        	kpiBean.thrsbadrule.set(rs.getString(9));
	        	kpiBean.dsid.set(rs.getString(10));
	        	kpiBean.isrunning.set(rs.getString(11));
	        	kpiBean.dsname.set(rs.getString(12));
	        }

	        rs.close();
	        st.close();

		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return kpiBean.Serialize();
    }
    
    public String GetKPIList(String username, boolean isAdmin)
    {
    	LOG.resultBean.Reset();
    	
    	BeanList<KPIBean> kpiBeanList = new BeanList<KPIBean>();
    	
        try {
			OpenConn();
	        String q = "select kpi.kpi_id, kpi.service_instance_id, kpi.kpi_name, kpi.kpi_query,";
	        q += " kpi.interval, kpi.time_unit, kpi.last_ran, kpi.threshold_marginal_rule, kpi.threshold_bad_rule,";
	        q += " kpi.datasource_id, kpi.isrunning,ds.datasource_name, kpi.status, u.username from smdbadmin.kpi kpi"; 
	        q += " left join smdbadmin.datasources ds on (kpi.datasource_id = ds.datasource_id)";
	        q += " left join smdbadmin.users u on(u.userid=kpi.createdby)";
	        if(!isAdmin)
	        	q += " where u.userid=(select userid from smdbadmin.users where username=?)";
	        PreparedStatement st = conn.prepareStatement(q);
	        if(!isAdmin)
	        	st.setString(1, username);
	        System.out.println(q + " " + username);
	        ResultSet rs = st.executeQuery();

	        while(rs.next()){
	        	KPIBean kpiBean = new KPIBean();
	        	
	        	kpiBean.kpiid.set(rs.getString(1));
	        	kpiBean.sid.set(rs.getString(2));
	        	kpiBean.kpiname.set(rs.getString(3));
	        	kpiBean.kpiquery.set(rs.getString(4));
	        	kpiBean.interval.set(rs.getString(5));
	        	kpiBean.timeunit.set(rs.getString(6));
	        	kpiBean.thrsmarrule.set(rs.getString(8));
	        	kpiBean.thrsbadrule.set(rs.getString(9));
	        	kpiBean.dsid.set(rs.getString(10));
	        	kpiBean.isrunning.set(rs.getString(11));
	        	kpiBean.dsname.set(rs.getString(12));
	        	kpiBean.status.set(rs.getString("status"));
	        	kpiBean.createdby.set(rs.getString("username"));
	        	
	        	kpiBeanList.add(kpiBean);
	        }
	        
	        rs.close();
	        st.close();
	        
		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return kpiBeanList.Serialize();
    }
    
    public String GetKPIListForTable(int sid)
    {
    	LOG.resultBean.Reset();
    	
    	BeanList<KPIBean> kpiBeanList = new BeanList<KPIBean>();
    	
        try {
			OpenConn();
	        Statement st = conn.createStatement();
	        String q = "select kpi.kpi_id, kpi.service_instance_id, kpi.kpi_name, kpi.kpi_query, kpi.interval, kpi.time_unit, kpi.last_ran, kpi.threshold_marginal_rule, kpi.threshold_bad_rule, kpi.datasource_id,kpi.isrunning,ds.datasource_name, kpi.status,"; 
	        q += " s.service_instance_name, kpi.last_measured_value";
	        q += " from smdbadmin.kpi kpi left join smdbadmin.datasources ds on kpi.datasource_id = ds.datasource_id";
	        q += " left join smdbadmin.service_instances s on (s.service_instance_id=kpi.service_instance_id)";
	        q += " where kpi.service_instance_id=" + sid;
	        ResultSet rs = st.executeQuery(q);

	        while(rs.next()){
	        	KPIBean kpiBean = new KPIBean();
	        	
	        	kpiBean.kpiid.set(rs.getString(1));
	        	kpiBean.sid.set(rs.getString(2));
	        	kpiBean.kpiname.set(rs.getString(3));
	        	kpiBean.kpiquery.set(rs.getString(4));
	        	kpiBean.interval.set(rs.getString(5));
	        	kpiBean.timeunit.set(rs.getString(6));
	        	kpiBean.thrsmarrule.set(rs.getString(8));
	        	kpiBean.thrsbadrule.set(rs.getString(9));
	        	kpiBean.dsid.set(rs.getString(10));
	        	kpiBean.isrunning.set(rs.getString(11));
	        	kpiBean.dsname.set(rs.getString(12));
	        	String stat = rs.getString("status");
	        	if(stat == null) {
	        		stat = "Good";
	        	} else if(stat.equals("0"))
	        	{
	        		stat = "Good";
	        	} else if(stat.equals("5")) {
	        		stat = "Bad";
	        	} else {
					stat = "Marginal";
				}
	        	kpiBean.status.set(stat);
	        	kpiBean.service_instance_name.set(rs.getString("service_instance_name"));
	        	String measuredVal = rs.getString("last_measured_value");
	        	if(measuredVal == null)
	        		measuredVal = "-";
	        	kpiBean.lastMeasuredValue.set(measuredVal);
	        	String date = Helper.UTCToDateStr(rs.getDouble("last_ran"));
	        	if(rs.getDouble("last_ran") == 0)
	        		date = "-";
	        	kpiBean.lastran.set(date);
	        	
	        	kpiBeanList.add(kpiBean);
	        }
	        
	        rs.close();
	        st.close();

		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return "{ \"data\":" + kpiBeanList.SerializeWithoutLabels() + " }";
    }
    
    public String DeleteKPI(int kpiID)
    {
    	LOG.resultBean.Reset();
    	
        try {
			OpenConn();
	        Statement st;
	        st = conn.createStatement();
	        String q = "delete from smdbadmin.kpi where kpi_id=" + kpiID;
	        
			st.executeUpdate(q);
			st.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Deleted_KPI"));
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
