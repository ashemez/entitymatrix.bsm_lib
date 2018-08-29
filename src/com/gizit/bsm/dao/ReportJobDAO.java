package com.gizit.bsm.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.gizit.bsm.beans.BeanList;
import com.gizit.bsm.beans.OutageBean;
import com.gizit.bsm.beans.ReportBean;
import com.gizit.bsm.beans.ReportDefBean;
import com.gizit.bsm.beans.ReportJobBean;
import com.gizit.bsm.helpers.Helper;

public class ReportJobDAO extends DAO {

	public ReportJobDAO() {
		super(ReportJobDAO.class);
	}
	
	public String GetReportJob(int report_job_id) {
		ReportJobBean reportJob = new ReportJobBean();
		try {
			OpenConn();
			
			String q = "select report_job_name, recurrent, sendmail, addresslist, cron, isenabled, subject, msgbody";
			q += " from smdbadmin.report_job where report_job_id=?";
			PreparedStatement st = conn.prepareStatement(q);
			st.setInt(1, report_job_id);
			ResultSet rs = st.executeQuery();

			while(rs.next()) {
				reportJob.report_job_id.set(report_job_id);
				reportJob.report_job_name.set(rs.getString("report_job_name"));
				reportJob.recurrent.set(rs.getInt("recurrent"));
				reportJob.sendmail.set(rs.getInt("sendmail"));
				reportJob.addresslist.set(rs.getString("addresslist"));
				reportJob.subject.set(rs.getString("subject"));
				reportJob.msgbody.set(rs.getString("msgbody"));
				reportJob.cron.set(rs.getString("cron"));
				reportJob.isenabled.set(rs.getInt("isenabled"));
			}
			
			rs.close();
			st.close();
			
			// get report list
			q = "select report_id from smdbadmin.reportjob_list where report_job_id=?";
			PreparedStatement st1 = conn.prepareStatement(q);
			st1.setInt(1, report_job_id);
			ResultSet rs1 = st1.executeQuery();
			while(rs1.next()) {
				reportJob.report_list.add(this.GetCreatedReport(rs1.getInt(1)));
			}
			rs1.close();
			st1.close();
			
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		if(LOG.IsError())
			return LOG.resultBean.Serialize();
		return reportJob.Serialize();
	}
	
	private ReportDefBean GetCreatedReport(int report_id) throws SQLException {
		LOG.resultBean.Reset();
		
		ReportDefBean report = new ReportDefBean();

		String q = "select r.report_name, r.service_instance_id, i.service_instance_name,";
		q += " r.query_type, r.starting_date, r.ending_date from smdbadmin.report r";
		q += " left join smdbadmin.service_instances i on(i.service_instance_id=r.service_instance_id)";
		q += " where r.report_id=" + report_id;
		PreparedStatement st = conn.prepareStatement(q);
		ResultSet rs = st.executeQuery();

		while(rs.next()) {
			report.report_id.set(report_id);
			report.report_name.set(rs.getString("report_name"));
			report.service_instance_id.set(rs.getInt("service_instance_id"));
			report.service_instance_name.set(rs.getString("service_instance_name"));
			report.startDate.set(rs.getInt("starting_date"));
			report.endDate.set(rs.getInt("ending_date"));
			report.queryType.set(rs.getString("query_type"));
		}

		rs.close();
		st.close();
		
		return report;
	}
	
	public String GetReportJobList(String username, boolean isAdmin) {
		LOG.resultBean.Reset();
		
		BeanList<ReportJobBean> reportJobList = new BeanList<ReportJobBean>();
		
		try {
			OpenConn();
			
			String q = "select j.report_job_id, j.report_job_name, j.recurrent, j.sendmail, j.addresslist, j.cron, j.isenabled,";
			q += " u.username from smdbadmin.report_job j";
			q += " left join smdbadmin.users u on(u.userid=j.createdby)";
			if(!isAdmin)
				q += " where createdby=(select userid from smdbadmin.users where username=?)";
			q += " order by report_job_name";
			System.out.println(q + " " + username);
			PreparedStatement st = conn.prepareStatement(q);
			if(!isAdmin)
				st.setString(1, username);
			ResultSet rs = st.executeQuery();

			while(rs.next()) {
				ReportJobBean report = new ReportJobBean();
				report.report_job_id.set(rs.getInt("report_job_id"));
				report.report_job_name.set(rs.getString("report_job_name"));
				report.recurrent.set(rs.getInt("recurrent"));
				report.sendmail.set(rs.getInt("sendmail"));
				report.addresslist.set(rs.getString("addresslist"));
				report.cron.set(rs.getString("cron"));
				report.isenabled.set(rs.getInt("isenabled"));
				report.createdby.set(rs.getString("username"));
				reportJobList.add(report);
			}
			
			rs.close();
			st.close();
			
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		if(LOG.IsError())
			return LOG.resultBean.Serialize();
		return reportJobList.Serialize();
	}
	
	public String SaveReportJob(int report_job_id, String report_job_name, int sendmail, String addresslist,
			String cron, int recurrent, int isenabled, int[] reportList, String subject, String message, String username) {
		String q = "select count(*) cnt from smdbadmin.report_job where report_job_name=?";
		if(report_job_id > 0)
			q += " and report_job_id!=" + report_job_id;

		try {
			OpenConn();
			
			PreparedStatement st1 = conn.prepareStatement(q);

			st1.setString(1, report_job_name);
			ResultSet rs = st1.executeQuery();
			rs.next();
			int cnt = rs.getInt("cnt");
			rs.close();
			st1.close();
			
			if(cnt > 0) {
				LOG.Warn(RM.GetErrorString("WARN_REPORTJOB_ALREADYEXIST"));
			} else {
				if(report_job_id > 0)
				{
					q = "update smdbadmin.report_job set report_job_name=?, sendmail=?, addresslist=?,";
					q += " cron=?, recurrent=?, isenabled=?, subject=?, msgbody=? where report_job_id=?";
					PreparedStatement st = conn.prepareStatement(q);
					st.setString(1, report_job_name);
					st.setInt(2,  sendmail);
					st.setString(3, addresslist);
					st.setString(4, cron);
					st.setInt(5, recurrent);
					st.setInt(6, isenabled);
					st.setString(7, subject);
					st.setString(8, message);
					st.setInt(9, report_job_id);
					st.executeUpdate();
					st.close();
				} else {
					q = "insert into smdbadmin.report_job(report_job_name, sendmail, addresslist, cron, recurrent, isenabled, subject, msgbody, createdby, createdat)";
					q += " values(?, ?, ?, ?, ?, ?, ?, ?, (select userid from smdbadmin.users where username=?), EXTRACT(EPOCH FROM (select current_timestamp)))";
					PreparedStatement st = conn.prepareStatement(q);
					st.setString(1, report_job_name);
					st.setInt(2,  sendmail);
					st.setString(3, addresslist);
					LOG.Debug("addresslist: " + addresslist);
					st.setString(4, cron);
					st.setInt(5, recurrent);
					st.setInt(6, isenabled);
					st.setString(7, subject);
					st.setString(8, message);
					st.setString(9, username);
					st.executeUpdate();
					st.close();
				}
				
				if(reportList != null) {
					q = "select report_job_id from smdbadmin.report_job where report_job_name=?";
					PreparedStatement st2 = conn.prepareStatement(q);
					st2.setString(1, report_job_name);
					ResultSet rs2 = st2.executeQuery();
					while(rs2.next()) {
						int rjid = rs2.getInt("report_job_id");
						q = "delete from smdbadmin.reportjob_list where report_job_id=?";
						PreparedStatement st3 = conn.prepareStatement(q);
						st3.setInt(1, rjid);
						st3.executeUpdate();
						st3.close();
						
						q = "insert into smdbadmin.reportjob_list(report_job_id, report_id) values(?, ?)";
						PreparedStatement st4 = conn.prepareStatement(q);
						for(int rid : reportList) {
							st4.setInt(1, rjid);
							st4.setInt(2, rid);
							st4.executeUpdate();
						}
						st4.close();
					}
					rs2.close();
					st2.close();
				}
				
				LOG.Success(RM.GetErrorString("SUCCESS_Updated_ReportJob"));
			}
		
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

		return LOG.resultBean.Serialize();
	}
	
	public String DeleteReportJob(int reportjobid) {
		LOG.resultBean.Reset();
		try{
			OpenConn();
			String q = "delete from smdbadmin.report_job where report_job_id=?;"
					+ "delete from smdbadmin.reportjob_list where report_job_id=?;";
			PreparedStatement stmt = conn.prepareStatement(q);
			stmt.setInt(1, reportjobid);
			stmt.setInt(2, reportjobid);
			stmt.executeUpdate();
			stmt.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Deleted_ReportJob"));
			
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		return LOG.resultBean.Serialize();
	}

}
