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
import com.gizit.bsm.beans.KPIRuleBean;
import com.gizit.bsm.beans.NodeGroupBean;
import com.gizit.bsm.beans.NodeGroupBean.Member;
import com.gizit.bsm.beans.OutputRuleBean;
import com.gizit.bsm.beans.ResultMessageBean;
import com.gizit.bsm.helpers.Helper;
import com.gizit.managers.ConnectionManager;
import com.gizit.managers.LogManager;
import com.gizit.managers.ResourceManager;

public class DataSourceDAO extends DAO {

	public DataSourceDAO() {
		super(DataSourceDAO.class);
	}

	/**
	 * Returns KPI related datasource info
	 * @param dsid datasource id
	 * @return datasource_name, datasource_type, host, port, dbname, username, password
	 */
	private String[] GetKPIQueryConnectionInfo(int dsid) {
		LOG.resultBean.Reset();
		
    	String[] queryDS = new String[7];

    	try {
			OpenConn();
			
			Statement st = conn.createStatement();
			String q = "select datasource_name, datasource_type, host, port, dbname, username,password from smdbadmin.datasources";
			q += " where datasource_id=" + dsid;
			ResultSet rs = st.executeQuery(q);
			
			while(rs.next()){
				queryDS[0] = rs.getString(1);
				queryDS[1] = rs.getString(2);
				queryDS[2] = rs.getString(3);
				queryDS[3] = rs.getString(4);
				queryDS[4] = rs.getString(5);
				queryDS[5] = rs.getString(6);
				queryDS[6] = rs.getString(7);
	        }
			
			st.close();
			rs.close();
			
		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
    	
    	return queryDS;
    }
    
    Connection kpiQueryConn = null;
    /**
     * Connects to KPI related datasource and sets the global connection variable kpiQueryConn
     * @param dsid datasource id
     */
    private void ConnectKPIQueryDS(int dsid)
    {
    	String[] queryDS = GetKPIQueryConnectionInfo(dsid);
    	
    	ConnectionManager.Properties connProps = new ConnectionManager.Properties();		
		connProps.datasourceType = queryDS[1];
		connProps.dbname = queryDS[4];
		connProps.host = queryDS[2];
		connProps.port = queryDS[3];
		connProps.username = queryDS[5];
		connProps.password = queryDS[6];
		
		System.out.println("DBNAME:"+connProps.dbname);
		System.out.println("HOST:"+connProps.host);
		System.out.println("PORT:"+connProps.port);
		System.out.println("USERNAME:"+connProps.username);
		System.out.println("PASSWORD:"+connProps.password);
    	
		kpiQueryConn = connectionManager.CreateConnection(connProps);
    }
    
    /**
     * Disconnects from KPI datasource
     */
    private void DisconnectKPIQueryDS()
    {
    	connectionManager.CloseConnection(kpiQueryConn);
    }
    
    /**
     * Saves a new datasource or updates an existing one according to the given name parameter
     * @param name datasource name
     * @param dstype datasource type
     * @param host hostname or IP address
     * @param port datasource port
     * @param dbname can be either DB name or SID
     * @param username
     * @param pw
     * @return
     */
    public String SaveDS(String name,
    		String dstype,
    		String host,
    		String port,
    		String dbname,
    		String username,
    		String pw)
    {
    	LOG.resultBean.Reset();
        try {
        	int dsid = 0;
        	
			OpenConn();
			Statement st = conn.createStatement();
	        String q = "select datasource_id from smdbadmin.datasources where datasource_name='" + name + "'";
	        ResultSet rs = st.executeQuery(q);
	        while(rs.next()) {
	        	dsid = rs.getInt(1);
	        }
	        rs.close();
			st.close();
			
			Statement st1 = conn.createStatement();
			if(dsid > 0)
			{
				q = "update smdbadmin.datasources set datasource_type='" + dstype + "',";
				q += " host='" + host + "', port='" + port + "', dbname='" + dbname + "', username='" + username + "', password='" + pw + "'";
				q += " where datasource_id=" + dsid;
			} else {
	        	q = "insert into smdbadmin.datasources(datasource_name, datasource_type, host, port, dbname, username, password)";
		        q += " values('" + name + "'"
		        		+ ", '" + dstype + "', '" + host + "', '" + port + "', '" + dbname + "', '" + username + "', '" + pw + "')";
			}
			st1.executeUpdate(q);
			st1.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_DS_Saved"));
		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        return LOG.resultBean.Serialize();
    }
    
    /**
     * Returns datasource definition info
     * @param dsid datasource id
     * @return serialized DSBean
     */
    public String GetDS(int dsid)
    {
    	LOG.resultBean.Reset();
    	
    	DSBean dsBean = new DSBean();

        try {
			OpenConn();
	        Statement st;
	        st = conn.createStatement();
	        String q = "select datasource_id, datasource_name, datasource_type, host, port, dbname, username, password from smdbadmin.datasources";
	        q += " where datasource_id=" + dsid;
	        ResultSet rs = st.executeQuery(q);

	        while(rs.next()){
	        	dsBean.dsid.set(rs.getString(1));
	        	dsBean.dsname.set(rs.getString(2));
	        	dsBean.dstype.set(rs.getString(3));
	        	dsBean.host.set(rs.getString(4));
	        	dsBean.port.set(rs.getString(5));
	        	dsBean.dbname.set(rs.getString(6));
	        	dsBean.username.set(rs.getString(7));
	        	dsBean.password.set(rs.getString(8));
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
        return dsBean.Serialize();
    }
    
    /**
     * Returns datasource definition list
     * @return serialized BeanList of DSBean
     */
    public String GetDSList()
    {
    	LOG.resultBean.Reset();
    	
    	BeanList<DSBean> dsList = new BeanList<DSBean>();

        try {
			OpenConn();
	        Statement st;
	        st = conn.createStatement();
	        String q = "select datasource_id, datasource_name, datasource_type, host, port, dbname, username from smdbadmin.datasources order by datasource_type asc, datasource_name asc";
	        ResultSet rs = st.executeQuery(q);

	        while(rs.next()){
	        	DSBean dsBean = new DSBean();
	        	
	        	dsBean.dsid.set(rs.getString(1));
	        	dsBean.dsname.set(rs.getString(2));
	        	dsBean.dstype.set(rs.getString(3));
	        	dsBean.host.set(rs.getString(4));
	        	dsBean.port.set(rs.getString(5));
	        	dsBean.dbname.set(rs.getString(6));
	        	dsBean.username.set(rs.getString(7));
	        	dsBean.password.set("");
	        	
	        	dsList.add(dsBean);
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
        return dsList.Serialize();
    }
    
    /**
     * Deletes a datasource definition
     * @param dsid datasource id
     * @return serialized ResultBean
     */
    public String DeleteDS(int dsid)
    {
    	LOG.resultBean.Reset();
    	
        try {
			OpenConn();
	        Statement st;
	        st = conn.createStatement();
	        String q = "delete from smdbadmin.datasources where datasource_id=" + dsid;
			st.executeUpdate(q);
			st.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_DS_Deleted"));
			
		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        return LOG.resultBean.Serialize();
    }
    
    /**
     * Returns KPI query columns
     * @param dsid datasource id
     * @param query KPI query
     * @return serialized BeanList of KPIRuleBean
     */
    public String GetDSKPIQueryColumn(int dsid,String query)
    {
    	LOG.resultBean.Reset();
    	
    	BeanList<KPIRuleBean> kpiRuleBeanList = new BeanList<KPIRuleBean>();
        try {
        	ConnectKPIQueryDS(dsid);
	        Statement st;
	        st = kpiQueryConn.createStatement();
	        String q = query;
	        ResultSet rs = st.executeQuery(q);
	        
	        ResultSetMetaData rsmd = rs.getMetaData();
	        int columnCount = rsmd.getColumnCount();
	        System.out.println("ColumnCount:"+columnCount);

	        for (int i = 1; i <= columnCount; i++ ) {
	        	KPIRuleBean kpiRuleBean = new KPIRuleBean();

	          //String name = rsmd.getColumnName(i);
	        	
	          kpiRuleBean.id.set(rsmd.getColumnName(i));
	          kpiRuleBean.label.set(rsmd.getColumnName(i));
	          kpiRuleBean.type.set("integer");
	          kpiRuleBean.input.set("text");
	          kpiRuleBean.operators.set(new String[] {"equal", "not_equal", "less", "less_or_equal", "greater", "greater_or_equal"});

	          kpiRuleBeanList.add(kpiRuleBean);
	        }
	        
	        rs.close();
	        st.close();
	        
	        LOG.Success(RM.GetErrorString("SUCCESS_KPI_Query_Column"));
	        
			//CloseConn();
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (NullPointerException e) {
			LOG.Error(e);
		} finally {
			DisconnectKPIQueryDS();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return kpiRuleBeanList.Serialize();
    }
    
    
    
}
