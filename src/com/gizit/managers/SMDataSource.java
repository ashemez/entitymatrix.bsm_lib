package com.gizit.managers;

import org.apache.commons.dbcp2.BasicDataSource;

public class SMDataSource {

	private BasicDataSource bds = new BasicDataSource();
	
	ResourceManager RM;
	private SMDataSource() {
		RM = new ResourceManager();
		
		String driver = "";
		String connUrl = "";
		String validationQuery = "";
		
		switch(RM.GetServerProperty("smdb-dbtype")) {
		case "POSTGRESQL":
			driver = "org.postgresql.Driver";
			connUrl = "jdbc:postgresql://" + RM.GetServerProperty("smdb-host") + ":" + RM.GetServerProperty("smdb-port") + "/" + RM.GetServerProperty("smdb-dbname");
			validationQuery = "select 1";
			break;
		case "ORACLE":
			driver = "oracle.jdbc.driver.OracleDriver";
			connUrl = "jdbc:oracle:thin:@" + RM.GetServerProperty("smdb-host") + ":" + RM.GetServerProperty("smdb-port") + ":" + RM.GetServerProperty("smdb-dbname");
			validationQuery = "select 1 from dual";
			break;
		}

		//Set database driver name
		bds.setDriverClassName(driver);
		//Set database url
		//bds.setUrl("jdbc:postgresql://smdb.cqrjmsgtawd7.us-west-2.rds.amazonaws.com:5432/smdb");
		bds.setUrl(connUrl);
		//Set database user
		bds.setUsername(RM.GetServerProperty("smdb-username"));
		//Set database password
		bds.setPassword(RM.GetServerProperty("smdb-password"));
		//Set the connection pool size
		bds.setInitialSize(0);
		bds.setMaxIdle(30);
		bds.setMaxTotal(100);
		bds.setMaxWaitMillis(60000);
		bds.setRemoveAbandonedOnBorrow(true);
		bds.setRemoveAbandonedTimeout(60);
		bds.setLogAbandoned(true);
		bds.setTestOnBorrow(true);
		bds.setValidationQuery(validationQuery);
		
	}
	
	private static class DataSourceHolder {
		private static final SMDataSource INSTANCE = new SMDataSource();
	}

	public static SMDataSource getInstance() {
		return DataSourceHolder.INSTANCE;
	}

	public BasicDataSource getBds() {
		return bds;
	}

	public void setBds(BasicDataSource bds) {
		this.bds = bds;
	}
	
}
