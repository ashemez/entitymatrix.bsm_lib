package com.gizit.bsm.dao;

import java.io.IOException;
import java.sql.Connection;

import com.gizit.bsm.generic.StringField;
import com.gizit.managers.ConnectionManager;
import com.gizit.managers.LogManager;
import com.gizit.managers.ResourceManager;

public class DAO {
	LogManager LOG;
	ResourceManager RM;
	ConnectionManager connectionManager;
	Connection conn = null;
	
	public <T> DAO(Class<T> clazz) {
		LOG = new LogManager(clazz);
		RM = new ResourceManager();
		connectionManager = new ConnectionManager();
	}
	
	/**
	 * Gets SMDB connection
	 * @throws IOException
	 */
	protected void OpenConn() throws IOException { conn = connectionManager.GetSMDBConnection(); }
	
	/**
	 * Closes open SMDB connection
	 */
	protected void CloseConn() { connectionManager.CloseConnection(conn); }
	
	private String username = "";
	public String getUsername() {
		return this.username;
	}
	public void setUsername(String value) {
		if(this.username != value)
			this.LOG.username.set(value);
		this.username = value;
	}

}
