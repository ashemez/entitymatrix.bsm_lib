package com.gizit.bsm.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.gizit.bsm.generic.IntField;
import com.gizit.bsm.generic.StringField;

public class ReportJobBean extends Bean {

	public IntField report_job_id = new IntField();
	public StringField report_job_name = new StringField();
	public IntField sendmail = new IntField();
	public StringField addresslist = new StringField();
	public StringField subject = new StringField();
	public StringField msgbody = new StringField();
	public StringField cron = new StringField();
	public IntField recurrent = new IntField();
	public IntField isenabled = new IntField();
	public StringField createdby = new StringField();
	public BeanList<ReportDefBean> report_list = new BeanList<ReportDefBean>();
	
	public String Serialize() {
		return super.Serialize();
	}
}
