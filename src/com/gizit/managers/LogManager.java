package com.gizit.managers;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import com.gizit.bsm.beans.ResultMessageBean;
import com.gizit.bsm.generic.StringField;
import com.gizit.bsm.helpers.Helper;

public class LogManager {

	public static class Level {
		public static final int OFF = 5;
		public static final int ERROR = 4;
		public static final int WARN = 3;
		public static final int INFO = 2;
		public static final int DEBUG = 1;
		public static final int TRACE = 0;
	}
	
	public static class State {
		public static final String SUCCESS = "SUCCESS";
		public static final String ERROR = "ERROR";
		public static final String WARN = "WARN";
		public static final String INFO = "INFO";
		public static final String DEBUG = "DEBUG";
	}

	
	private int LogLevel;
	
	public ResultMessageBean resultBean;
	public StringField username = new StringField();
	
	private String className = "";
	ResourceManager RM;
	public <T> LogManager(Class<T> clazz) {
		className = clazz.getName();

		RM = new ResourceManager();
		
		//LogLevel = Level.DEBUG;
		switch(RM.GetServerProperty("log_level")) {
		case "DEBUG":
			LogLevel = Level.DEBUG;
			break;
		case "INFO":
			LogLevel = Level.INFO;
			break;
		case "WARN":
			LogLevel = Level.WARN;
			break;
		case "ERROR":
			LogLevel = Level.ERROR;
			break;
		case "OFF":
			LogLevel = Level.OFF;
			break;
		}
		
		username.set("");
		
		resultBean = new ResultMessageBean();
	}
	
	public boolean IsError() {
		return this.resultBean.state.get().equals(State.ERROR) || this.resultBean.state.get().equals(State.WARN);
	}
	
	public void Error(Exception e) {
		int lineNum = Thread.currentThread().getStackTrace()[2].getLineNumber();
		
		String logMsg = Helper.CurrentTimeUTCMilliSecond() + " " + username.get() + " ERROR " + className + " Line: " + lineNum + " " + e.getMessage();
		
		if(LogLevel <= Level.ERROR) {
			System.out.println(Helper.CurrentTimeUTCMilliSecond() + " " + username.get() + " ERROR " + className + " Line: " + lineNum + " " + e.getMessage());
			e.printStackTrace();
		}
		
		if(!username.get().trim().isEmpty())
		{
			UserAuditLog(logMsg);
		}

		/*String msg = e.getMessage();
		if(msg == null)
			msg = "null";
		if(e.getMessage() != null)
			resultBean.message.set(Helper.EscapeJsonStr(e.getMessage()));
		else
			resultBean.message.set("An error occured");*/
		
		// set a user friendly error message
		/*String[] exceptionFullName = e.getClass().getName().split(".");
		switch(exceptionFullName[exceptionFullName.length - 1]) {
		case "IOException":
			Warn(RM.GetErrorString("WARN_CONNERR_RETRY"), lineNum);
			break;
		case "SQLException":
			Warn(RM.GetErrorString("WARN_CONNERR_RETRY"), lineNum);
			break;
		case "NamingException":
			Warn(RM.GetErrorString("WARN_LDAP_NamingException"), lineNum);
			break;
		case "ParseException":
			Warn(RM.GetErrorString("WARN_ParseException"), lineNum);
			break;
		case "ClassNotFoundException":
			Warn(RM.GetErrorString("WARN_ClassNotFoundException"), lineNum);
			break;
		case "MissingResourceException":
			Warn(RM.GetErrorString("WARN_MissingResourceException"), lineNum);
			break;
		case "NullPointerException":
			Warn(RM.GetErrorString("WARN_NullPointerException"), lineNum);
			break;
		default:
			Warn(RM.GetErrorString("WARN_NullPointerException"), lineNum);
			break;
		}*/
		
		resultBean.state.set(State.ERROR);
		
		resultBean.operation.set(className);
	}
	public void Error(Exception e, String ExtraInfo) {
		int lineNum = Thread.currentThread().getStackTrace()[2].getLineNumber();
		
		String logMsg = Helper.CurrentTimeUTCMilliSecond() + " " + username.get() + " ERROR " + className + " Line: " + lineNum + " " + e.getMessage() + " " + ExtraInfo;
		
		System.out.println("e.getClass().getName(): " + e.getClass().getName());
		
		if(!username.get().trim().isEmpty())
		{
			UserAuditLog(logMsg);
		}
		
		if(LogLevel <= Level.ERROR) {
			System.out.println(Helper.CurrentTimeUTCMilliSecond() + " " + username.get() + " ERROR " + className + " Line: " + lineNum + " " + e.getMessage() + " " + ExtraInfo);
			e.printStackTrace();
		}

		resultBean.state.set(State.ERROR);
		resultBean.message.set(Helper.EscapeJsonStr(e.getMessage()));
		resultBean.operation.set(className + "  " + ExtraInfo);
	}
	
	public void Warn(String msg) {
		int lineNum = Thread.currentThread().getStackTrace()[2].getLineNumber();
		
		String logMsg = Helper.CurrentTimeUTCMilliSecond() + " " + username.get() + " WARN " + className + " Line: " + lineNum + " " + msg;

		if(LogLevel <= Level.WARN)
			System.out.println(Helper.CurrentTimeUTCMilliSecond() + " " + username.get() + " WARN " + className + " Line: " + lineNum + " " + msg);
		
		if(!username.get().trim().isEmpty())
		{
			UserAuditLog(logMsg);
		}
		
		resultBean.state.set(State.WARN);
		resultBean.message.set(Helper.EscapeJsonStr(msg));
		resultBean.operation.set(className);
	}
	
	public void Warn(String msg, int linenum) {
		int lineNum = linenum;
		
		String logMsg = Helper.CurrentTimeUTCMilliSecond() + " " + username.get() + " WARN " + className + " Line: " + lineNum + " " + msg;

		if(LogLevel <= Level.WARN)
			System.out.println(Helper.CurrentTimeUTCMilliSecond() + " " + username.get() + " WARN " + className + " Line: " + lineNum + " " + msg);
		
		if(!username.get().trim().isEmpty())
		{
			UserAuditLog(logMsg);
		}
		
		resultBean.state.set(State.WARN);
		resultBean.message.set(Helper.EscapeJsonStr(msg));
		resultBean.operation.set(className);
	}
	
	public void Info(String msg) {
		int lineNum = Thread.currentThread().getStackTrace()[2].getLineNumber();
		
		String logMsg = Helper.CurrentTimeUTCMilliSecond() + " " + username.get() + " INFO " + className + " Line: " + lineNum + " " + msg;
		if(LogLevel <= Level.INFO)
			System.out.println(logMsg);
		
		if(!username.get().trim().isEmpty())
		{
			UserAuditLog(logMsg);
		}
		
		resultBean.state.set(State.INFO);
		resultBean.message.set(Helper.EscapeJsonStr(msg));
		resultBean.operation.set(className);
	}
	
	public void Debug(String msg) {
		int lineNum = Thread.currentThread().getStackTrace()[2].getLineNumber();
		String logMsg = Helper.CurrentTimeUTCMilliSecond() + " " + username.get() + " DEBUG " + className + " Line: " + lineNum + " " + msg;
		if(LogLevel <= Level.DEBUG)
			System.out.println(logMsg);
		
		if(!username.get().trim().isEmpty())
		{
			UserAuditLog(logMsg);
		}
		
		resultBean.state.set(State.DEBUG);
		resultBean.message.set(Helper.EscapeJsonStr(msg));
		resultBean.operation.set(className);
	}
	
	public void Success(String msg) {
		int lineNum = Thread.currentThread().getStackTrace()[2].getLineNumber();
		String logMsg = Helper.CurrentTimeUTCMilliSecond() + " " + username.get() + " SUCCESS " + className + " Line: " + lineNum + " " + msg;
		if(LogLevel <= Level.TRACE)
			System.out.println(logMsg);

		if(!username.get().trim().isEmpty())
		{
			UserAuditLog(logMsg);
		}
		resultBean.state.set(State.SUCCESS);
		resultBean.message.set(Helper.EscapeJsonStr(msg));
		resultBean.operation.set(className);
		
	}
	
	private void UserAuditLog(String msg) {
		try {
			FileWriter fw = new FileWriter(System.getenv("BSVIEW_CONFPATH") + "/user_audit.log", true);
			BufferedWriter out = new BufferedWriter(fw);
		    out.write(msg + "\n");
		    out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void ServiceChangeAuditLog(String msg) {
		try {
			String logMsg = Helper.CurrentTimeUTCMilliSecond() + " " + msg;

			//FileWriter fw = new FileWriter(System.getProperty("catalina.base") + "/wtpwebapps/gbsm/service_change_audit.log", true);
			FileWriter fw = new FileWriter(System.getenv("BSVIEW_CONFPATH"), true);
			BufferedWriter out = new BufferedWriter(fw);
		    out.write(logMsg + "\n");
		    out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
