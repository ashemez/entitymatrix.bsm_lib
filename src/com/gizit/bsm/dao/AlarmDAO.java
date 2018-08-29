package com.gizit.bsm.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.gizit.bsm.beans.AlarmBean;
import com.gizit.bsm.beans.BeanList;
import com.gizit.bsm.helpers.Helper;
import com.gizit.managers.ConnectionManager;
import com.gizit.managers.LogManager;
import com.gizit.managers.ResourceManager;

public class AlarmDAO extends DAO {
	
	private ServiceStatDAO srvStatDAO;
	public AlarmDAO() {
		super(AlarmDAO.class);
		
		srvStatDAO = new ServiceStatDAO();
	}
    
	List<String[]> alarmDSList = new ArrayList<String[]>();
	/**
	 * Retrieves Netcool connection information defined in smdbadmin.alarm.ds where dstype is netcool
	 * @return host, port, username, password in a String array
	 */
    private String[] GetNetcoolConnectionInfo() {
    	LOG.resultBean.Reset();
    	
    	String[] netcoolDS = new String[4];

    	/*try {
			OpenConn();

			String q = "select host, port, username, password from smdbadmin.alarm_ds where dstype='netcool';";
			PreparedStatement st = conn.prepareStatement(q);
			ResultSet rs = st.executeQuery();

			while(rs.next()){
				netcoolDS[0] = rs.getString(1);
				netcoolDS[1] = rs.getString(2);
				netcoolDS[2] = rs.getString(3);
				netcoolDS[3] = rs.getString(4);
	        }

			rs.close();
			st.close();

		} catch (IOException e) {
			LOG.Error(e);
			LOG.Warn(RM.GetErrorString("WARN_AlarmDS_NotAvailable"));
		} catch (SQLException e) {
			LOG.Error(e);
			LOG.Warn(RM.GetErrorString("WARN_AlarmDS_NotAvailable"));
		} finally {
			CloseConn();
		}*/
    	
		netcoolDS[0] = RM.GetServerProperty("alarmdb-host");
		netcoolDS[1] = RM.GetServerProperty("alarmdb-port");
		netcoolDS[2] = RM.GetServerProperty("alarmdb-username");
		netcoolDS[3] = RM.GetServerProperty("alarmdb-password");
    	
    	return netcoolDS;
    }
    
	// netcool object server connection
    Connection netcoolConn = null;
    /**
     * Connects to Netcool datasource and sets the global connection variable netcoolConn
     */
    private void ConnectNetcoolOS()
    {
    	String[] netcoolDS = GetNetcoolConnectionInfo();
    	
    	ConnectionManager.Properties connProps = new ConnectionManager.Properties();		
		connProps.datasourceType = "OMNIBUSOS";
		connProps.dbname = "";
		connProps.host = netcoolDS[0];
		connProps.port = netcoolDS[1];
		connProps.username = netcoolDS[2];
		connProps.password = netcoolDS[3];
    	
		netcoolConn = connectionManager.CreateConnection(connProps);
    }
    
    /**
     * Disconnects from Netcool datasource
     */
    private void DisconnectNetcoolOS()
    {
    	connectionManager.CloseConnection(netcoolConn);
    }
    
    private int SID = 0;
    private String SrvNameList = "";
    
    /**
     * Returns an alarm list as a JSON formatted string
     * @param sid service instance id for retrieving the alarm list of the whole service tree structure starting from this id
     * @return JSON formatted alarm list or JSON formatted ResultBean string if error occurs
     */
    
	public String getAlarmList(int sid){
		/*if(SID != sid) {
			SID = sid;
			//srvStatDAO.ServiceTreeThroughBeans(sid);
			SrvNameList = srvStatDAO.GetUniqueCIListForAlarmsAndStat(sid)[0];
			
			System.out.println("SrvNameList " + SrvNameList);
		}*/

		/*if(SID != sid) {
			SID = sid;
			SrvNameList = srvStatDAO.GetUniqueCIListForAlarmsAndStat(sid)[0];
		}*/
		SrvNameList = srvStatDAO.GetUniqueCIListForAlarmsAndStat(sid)[0];
		
		//System.out.println("CI List for alarms " + SrvNameList);
		
		LOG.resultBean.Reset();

		BeanList<AlarmBean> alarmList = new BeanList<AlarmBean>();
		
		ConnectNetcoolOS();
        if(netcoolConn != null) {
	        try {

	        	if(!SrvNameList.equals("")) {
		    		String query = "select Identifier, ServerSerial, Type, Class, Manager, Node, NodeAlias,";
		            query += " Summary, Severity, to_char(FirstOccurrence) as FO, to_char(LastOccurrence) as LO, BSM_Identity, FirstOccurrence, LastOccurrence from alerts.status";
		            query += " where Type in(1,20) and BSM_Identity like '^OI-'";
		            query += " and BSM_Identity in(" + SrvNameList + ") order by LastOccurrence desc";
		            System.out.println(query);
		        	
		            PreparedStatement stmt = netcoolConn.prepareStatement(query);
		            ResultSet rs = stmt.executeQuery();
		
		            while(rs.next())
		            {
		            	AlarmBean alarmBean = new AlarmBean();
		            	int serverserial = rs.getInt(2);
		            	String manager = rs.getString(5);
		            	String node = rs.getString(6);
		            	String summary = rs.getString(8);
		            	int severity = rs.getInt(9);
		            	String firstoccurrence = rs.getString(10);
		            	firstoccurrence = Helper.UTCToDateStr((double)rs.getInt("FirstOccurrence"));
		            	String lastoccurrence = rs.getString(11);
		            	lastoccurrence = Helper.UTCToDateStr((double)rs.getInt("LastOccurrence"));
		            	String bsm_identity = rs.getString(12);
		
		            	alarmBean.ServerSerial.set(serverserial);
		            	alarmBean.Manager.set(manager);
		            	alarmBean.Node.set(node);
		            	alarmBean.Summary.set(summary);
		            	String severityStr = "Clear";
		            	switch(severity) {
		            	case 5:
		            		severityStr = "Critical";
		            		break;
		            	case 4:
		            		severityStr = "Major";
		            		break;
		            	case 3:
		            		severityStr = "Minor";
		            		break;
		            	case 2:
		            		severityStr = "Warning";
		            		break;
		            	case 1:
		            		severityStr = "Indeterminate";
		            		break;
		            	}
		            	alarmBean.Severity.set(severityStr);
		            	alarmBean.FirstOccurrence.set(firstoccurrence);
		            	alarmBean.LastOccurrence.set(lastoccurrence);
		            	alarmBean.BSM_Identity.set(bsm_identity);
		
		            	alarmList.add(alarmBean);
		            	
		            }
	
		            rs.close();
		            stmt.close();
	        	}
	        } catch (SQLException e) {
	        	LOG.Error(e);
	        	LOG.Warn(RM.GetErrorString("WARN_AlarmDS_QueryErrOccurred"));
	        } finally {
	        	DisconnectNetcoolOS();
	        }
	        
        } else {
        	LOG.Warn(RM.GetErrorString("WARN_AlarmDS_NotAvailable"));
        }
        
        if(LOG.IsError())
        	LOG.resultBean.Serialize();
        return "{ \"data\":" + alarmList.SerializeWithoutLabels() + " }";
	}
	
}