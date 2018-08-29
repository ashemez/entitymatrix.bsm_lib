package com.gizit.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

public class ConnectionManager {

	public static class Properties{
		public String datasourceType;
		public String host;
		public String port;
		public String dbname;
		public String username;
		public String password;
	}

	LogManager LOG;
	private BasicDataSource smDS;
	public ConnectionManager() {
		smDS = SMDataSource.getInstance().getBds();		
		LOG = new LogManager(ConnectionManager.class);
	}
	
	// create smdb connection
	public Connection GetSMDBConnection() {
	    try {
	    	Connection c = null;
	    	while (c == null)
	    		c = smDS.getConnection();
	    	return c;
	    	
		} catch (SQLException e) {
			LOG.Error(e);
		}
	    return null;
	}
	
	// create other jdbc connection
	public Connection CreateConnection(Properties connProps) {
		return OpenJDBCConn(connProps);
	}

    public Connection OpenJDBCConn(Properties connProps) {
    	String url = "";
    	String driver = "";

    	try {
		    	switch(connProps.datasourceType) {
		    	case "ORACLE":
		    		url = "jdbc:oracle:thin:@" + connProps.host + ":" + connProps.port + ":" + connProps.dbname;
		    		driver = "oracle.jdbc.driver.OracleDriver";
		    		Class.forName(driver);
		    		break;
		    	case "SQLSERVER":
		    		url = "jdbc:sqlserver://" + connProps.host + ":" + connProps.port + ";databaseName=" + connProps.dbname + ";user=" + connProps.username + ";password=" + connProps.password;
		    		driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		    		Class.forName(driver);
		    		break;
		    	case "POSTGRESQL":
		    		url = "jdbc:postgresql://" + connProps.host + ":" + connProps.port + "/" + connProps.dbname;
		    		driver = "org.postgresql.Driver";
		    		Class.forName(driver);
		    		break;
		    	case "OMNIBUSOS":
		    		url = "jdbc:sybase:Tds:" + connProps.host + ":" + connProps.port;
		    		driver = "com.sybase.jdbc3.jdbc.SybDriver";
		    		Class.forName(driver);
		    		break;
		    	case "DB2":
		    		url = "jdbc:db2://" + connProps.host + ":" + connProps.port + "/" + connProps.dbname + ":user=" + connProps.username + ";password=" + connProps.password + ";";;
		    		driver = "com.ibm.db2.jcc.DB2Driver";
		    		Class.forName(driver);
		    		break;
		    	}
		    	
		    	// start retry
		    	if(connProps.datasourceType.equals("SQLSERVER") || connProps.datasourceType.equals("DB2"))
		    		return DriverManager.getConnection(url);
		    	else
		    		return DriverManager.getConnection(url, connProps.username, connProps.password);
		}
    	catch (SQLException e) {
    		LOG.Error(e);
		} catch (ClassNotFoundException e) {
			LOG.Error(e);
		}
    	
    	return null;
    }
    
    public void CloseConnection(Connection conn) {
    	try {
    		if(conn != null)
    			if(!conn.isClosed()) {
    				conn.close();
    			}
		} catch (SQLException e) {
			LOG.Error(e);
		}
    }

}
