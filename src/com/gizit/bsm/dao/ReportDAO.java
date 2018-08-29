package com.gizit.bsm.dao;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.gizit.bsm.beans.BeanList;
import com.gizit.bsm.beans.OutageBean;
import com.gizit.bsm.beans.ReportBean;
import com.gizit.bsm.beans.ReportDefBean;
import com.gizit.bsm.beans.ServiceBean;
import com.gizit.bsm.beans.UserBean;
import com.gizit.bsm.dao.ReportDAO.QueryType;
import com.gizit.bsm.helpers.Helper;

public class ReportDAO extends DAO {

	private ServiceStatDAO srvStatDAO = new ServiceStatDAO();
	public ReportDAO() {
		super(ReportDAO.class);
	}
	
	public static class QueryType{
		public static final String TODAY = "TODAY";
		public static final String LAST_DAY = "LAST_DAY";
		public static final String LAST_7_DAYS = "LAST_7_DAYS";
		public static final String LAST_WEEK = "LAST_WEEK";
		public static final String LAST_MONTH = "LAST_MONTH";
		public static final String DATE_RANGE = "DATE_RANGE";
		public static final String CURRENT_MONTH = "CURRENT_MONTH";
	}
	
	public ReportBean GetReportBeanOfSingleService(int service_instance_id, int startingDate, int endingDate, boolean getOutageDetails, String queryType) throws SQLException, IOException {
		LOG.resultBean.Reset();
		
		ReportBean report = new ReportBean();
		OpenConn();
		report = GetOutageReport(service_instance_id, startingDate, endingDate, getOutageDetails, queryType);
		CloseConn();
		return report;
	}
	
	public BeanList<ReportBean> GetReportBeanOfAllServices(int startingDate, int endingDate, boolean getOutageDetails, String queryType) throws SQLException, IOException {
		LOG.resultBean.Reset();
		
		BeanList<ReportBean> reportList = new BeanList<ReportBean>();
		OpenConn();
	
		String q = "select service_instance_id from smdbadmin.service_instances";
    	q += " where citype='CI.BUSINESSMAINSERVICE'";
    	
    	PreparedStatement st = conn.prepareStatement(q);
    	ResultSet rs = st.executeQuery();

        while(rs.next()) {
        	
        	ReportBean report = GetOutageReport(rs.getInt("service_instance_id"), startingDate, endingDate, getOutageDetails, queryType);
        	
        	reportList.add(report);
        }
        
        rs.close();
        st.close();
        
		CloseConn();
		return reportList;
	}
	
	public HashMap<Integer, String>[] GetReportBeanOfServiceList(int[] sidList, int startingDate, int endingDate, boolean getOutageDetails, String queryType) throws SQLException, IOException {
		LOG.resultBean.Reset();
		
		HashMap<Integer, String>[] metricList = (HashMap<Integer, String>[]) new HashMap[sidList.length];
		OpenConn();
		int i = 0;
		
		for(int sid : sidList) {
			ReportBean report = GetOutageReport(sid, startingDate, endingDate, getOutageDetails, queryType);
			HashMap<Integer, String> pair = new HashMap<Integer, String>();
			pair.put(sid, Helper.DoublePrecision2(report.availabilityMetric.get()) + ":" + report.currentStatus.get());
			metricList[i] = pair;
			i++;
		}
		
		CloseConn();
		return metricList;
	}
	
	public String GetCalculatedOutageReportOfSingleService(int service_instance_id, int startingDate, int endingDate, boolean getOutageDetails, String queryType) {
		LOG.resultBean.Reset();
		
		BeanList<ReportBean> reportList = new BeanList<ReportBean>();
		ReportBean report = new ReportBean();
		try {
			OpenConn();
			
			report = GetOutageReport(service_instance_id, startingDate, endingDate, getOutageDetails, queryType);

		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		reportList.add(report);
		// sort reportlist
		reportList.sort((r1, r2) -> r1.availabilityMetric.get().compareTo(r2.availabilityMetric.get()));
		
		if(LOG.IsError())
			return LOG.resultBean.Serialize();
		return reportList.Serialize();
	}
	
	public String GetCalculatedOutageReportOfChildren(int service_instance_id, int startingDate, int endingDate, boolean getOutageDetails, String queryType) {
		LOG.resultBean.Reset();
		BeanList<ReportBean> reportList = new BeanList<ReportBean>();
		
		String sq = "select service_instance_id from smdbadmin.service_instance_relations where parent_instance_id=" + service_instance_id;
		try {
			
			OpenConn();
			
			PreparedStatement cst = conn.prepareStatement(sq);
			ResultSet crs = cst.executeQuery();
			
			while(crs.next()) {
				reportList.add(GetOutageReport(crs.getInt(1), startingDate, endingDate, getOutageDetails, queryType));
			}
			
			crs.close();
			cst.close();

		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		// sort reportlist
		reportList.sort((r1, r2) -> r1.availabilityMetric.get().compareTo(r2.availabilityMetric.get()));
		
		if(LOG.IsError())
			return LOG.resultBean.Serialize();
		return reportList.Serialize();
	}
	
	ArrayList<Integer> usedSid;
	BeanList<ReportBean> srvTreeReport = new BeanList<ReportBean>();
	public String GetReportOfServiceTree(int service_instance_id, int startingDate, int endingDate, boolean getOutageDetails, String queryType) {
		usedSid = new ArrayList<Integer>();
		srvTreeReport = new BeanList<ReportBean>();
		
		try {
			OpenConn();
			
			this.GetCalculatedOutageReportOfServiceTree(service_instance_id, startingDate, endingDate, getOutageDetails, queryType);
			
			CloseConn();
		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		}
		
		srvTreeReport.sort((r1, r2) -> r1.availabilityMetric.get().compareTo(r2.availabilityMetric.get()));
		
		if(LOG.IsError())
			return LOG.resultBean.Serialize();
		return srvTreeReport.Serialize();
		
		
	}
	
	private void GetCalculatedOutageReportOfServiceTree(int service_instance_id, int startingDate, int endingDate, boolean getOutageDetails, String queryType) throws IOException, SQLException {
		LOG.resultBean.Reset();
		
		ReportBean report = GetOutageReport(service_instance_id, startingDate, endingDate, getOutageDetails, queryType);
		srvTreeReport.add(report);
		
		// iterate children
		String sq = "select service_instance_id from smdbadmin.service_instance_relations where parent_instance_id=" + service_instance_id;
		PreparedStatement cst = conn.prepareStatement(sq);
		ResultSet crs = cst.executeQuery();
		
		while(crs.next()) {
			int sid = crs.getInt(1);
			if(!usedSid.contains(sid)) {
				LOG.Debug("Iterating sid: " + sid);
				usedSid.add(sid);
				GetCalculatedOutageReportOfServiceTree(sid, startingDate, endingDate, getOutageDetails, queryType);	
			}
		}

		crs.close();
		cst.close();
	}
	
	public HashMap<String, BeanList<ReportBean>> GetListOfDailyReportOfAllServices(String queryType) throws SQLException, IOException {
		
		OpenConn();
		
		String q = "select service_instance_id, service_instance_name from smdbadmin.service_instances where citype='CI.BUSINESSMAINSERVICE'";
		PreparedStatement st = conn.prepareStatement(q);
		ResultSet rs = st.executeQuery();
		
		HashMap<String, BeanList<ReportBean>> allReports = new HashMap<String, BeanList<ReportBean>>();
		while(rs.next()) {
			BeanList<ReportBean> repList = GetListOfDailyReportWithouOpenConn(rs.getInt("service_instance_id"), 0, 0, false, queryType);
			repList.sort((r1, r2) -> r1.startDate.get().compareTo(r2.startDate.get()));
			allReports.put(rs.getString("service_instance_name"), repList);
		}
		
		rs.close();
		st.close();
		
		CloseConn();
		
		return allReports;
	}
	
	private BeanList<ReportBean> GetListOfDailyReportWithouOpenConn(int service_instance_id, int startingDate, int endingDate, boolean getOutageDetails, String queryType) throws SQLException {
		
		int DayCount = 0;
		switch(queryType)
		{
		case QueryType.TODAY:
			startingDate = Helper.TodayStartingDate().intValue();
			endingDate = startingDate + 86400;
			break;
		case QueryType.LAST_DAY:
			startingDate = Helper.LastDayStartingDate().intValue();
			endingDate = startingDate + 86400;
			break;
		case QueryType.LAST_7_DAYS:
			startingDate = Helper.LastSevenDaysStartingDate().intValue();
			endingDate = startingDate + 86400 * 7;
			break;
		case QueryType.LAST_WEEK:
			startingDate = Helper.LastWeekStartingDate().intValue();
			endingDate = startingDate + 86400 * 7;
			break;
		case QueryType.LAST_MONTH:
			startingDate = Helper.LastMonthStartingDate().intValue();
			DayCount = Helper.DayCountOfMonth(startingDate * 1000L);
			endingDate = startingDate + 86400 * DayCount;
			break;
		case QueryType.CURRENT_MONTH:
			startingDate = Helper.CurrentMonthStartingDate().intValue();
			DayCount = Helper.DayCountOfMonth(startingDate * 1000L);
			endingDate = startingDate + 86400 * DayCount;
			break;
		case QueryType.DATE_RANGE:
			break;
		}
		
		int dateRange = endingDate - startingDate;
		int remainder = dateRange % 86400;
		int dayCount = (int)Math.floor((double)(dateRange / 86400));
		
		LOG.Debug("dayCount: " + dayCount + " remainder: " + remainder);
		
		BeanList<ReportBean> reportList = new BeanList<ReportBean>();
		for(int i=0; i<dayCount; i++) {
			int startD = startingDate + i * 86400;
			int endD = startD + 86400;
			reportList.add(GetOutageReport(service_instance_id, startD, endD, false, QueryType.DATE_RANGE));
		}

		if(remainder > 0) {
			int startD = startingDate + dayCount * 86400;
			int endD = startD + remainder;
			reportList.add(GetOutageReport(service_instance_id, startD, endD, false, QueryType.DATE_RANGE));
		}
		
		reportList.sort((r1, r2) -> r1.availabilityMetric.get().compareTo(r2.availabilityMetric.get()));
		
		return reportList;
	}

	public BeanList<ReportBean> GetListOfDailyReport(int service_instance_id, int startingDate, int endingDate, boolean getOutageDetails, String queryType) throws SQLException, IOException {
		
		OpenConn();
		int DayCount = 0;
		switch(queryType)
		{
		case QueryType.TODAY:
			startingDate = Helper.TodayStartingDate().intValue();
			endingDate = startingDate + 86400;
			break;
		case QueryType.LAST_DAY:
			startingDate = Helper.LastDayStartingDate().intValue();
			endingDate = startingDate + 86400;
			break;
		case QueryType.LAST_7_DAYS:
			startingDate = Helper.LastSevenDaysStartingDate().intValue();
			endingDate = startingDate + 86400 * 7;
			break;
		case QueryType.LAST_WEEK:
			startingDate = Helper.LastWeekStartingDate().intValue();
			endingDate = startingDate + 86400 * 7;
			break;
		case QueryType.LAST_MONTH:
			startingDate = Helper.LastMonthStartingDate().intValue();
			DayCount = Helper.DayCountOfMonth(startingDate * 1000L);
			endingDate = startingDate + 86400 * DayCount;
			break;
		case QueryType.CURRENT_MONTH:
			startingDate = Helper.CurrentMonthStartingDate().intValue();
			DayCount = Helper.DayCountOfMonth(startingDate * 1000L);
			endingDate = startingDate + 86400 * DayCount;
			break;
		case QueryType.DATE_RANGE:
			break;
		}
		
		int dateRange = endingDate - startingDate;
		int remainder = dateRange % 86400;
		int dayCount = (int)Math.floor((double)(dateRange / 86400));
		
		LOG.Debug("dayCount: " + dayCount + " remainder: " + remainder);
		
		BeanList<ReportBean> reportList = new BeanList<ReportBean>();
		for(int i=0; i<dayCount; i++) {
			int startD = startingDate + i * 86400;
			int endD = startD + 86400;
			reportList.add(GetOutageReport(service_instance_id, startD, endD, false, QueryType.DATE_RANGE));
		}

		if(remainder > 0) {
			int startD = startingDate + dayCount * 86400;
			int endD = startD + remainder;
			reportList.add(GetOutageReport(service_instance_id, startD, endD, false, QueryType.DATE_RANGE));
		}
		
		CloseConn();
		
		reportList.sort((r1, r2) -> r1.availabilityMetric.get().compareTo(r2.availabilityMetric.get()));
		
		return reportList;
	}
	
	private ReportBean GetOutageReport(int service_instance_id, int startingDate, int endingDate, boolean getOutageDetails, String queryType) throws SQLException {
		LOG.resultBean.Reset();

		int DayCount = 0;
		switch(queryType)
		{
		case QueryType.TODAY:
			startingDate = Helper.TodayStartingDate().intValue();
			endingDate = startingDate + 86400;
			break;
		case QueryType.LAST_DAY:
			startingDate = Helper.LastDayStartingDate().intValue();
			endingDate = startingDate + 86400;
			break;
		case QueryType.LAST_7_DAYS:
			startingDate = Helper.LastSevenDaysStartingDate().intValue();
			endingDate = startingDate + 86400 * 7;
			break;
		case QueryType.LAST_WEEK:
			startingDate = Helper.LastWeekStartingDate().intValue();
			endingDate = startingDate + 86400 * 7;
			break;
		case QueryType.LAST_MONTH:
			startingDate = Helper.LastMonthStartingDate().intValue();
			DayCount = Helper.DayCountOfMonth(startingDate * 1000L);
			endingDate = startingDate + 86400 * DayCount;
			break;
		case QueryType.CURRENT_MONTH:
			startingDate = Helper.CurrentMonthStartingDate().intValue();
			DayCount = Helper.DayCountOfMonth(startingDate * 1000L);
			endingDate = startingDate + 86400 * DayCount;
			break;
		case QueryType.DATE_RANGE:
			break;
		}
		
		LOG.Debug("****** startingDate: " + startingDate + ", endingDate: " + endingDate);
		
		String q = "select c.outage_duration, c.status_timestamp, c.service_id, i.current_status";
		q += " from smdbadmin.service_status_change c";
		q += " left join smdbadmin.service_instances i on(i.service_instance_id=c.service_id)";
		q += " where c.service_id=" + service_instance_id + " and (c.prev_service_status=5";
		q += " and ( (c.status_timestamp>" + startingDate + " and c.status_timestamp<" + endingDate + ")";
		q += " or (c.status_timestamp>" + endingDate + " and c.status_timestamp - c.outage_duration<" + endingDate + ")";
		q += " or (c.status_timestamp>" + startingDate + " and c.status_timestamp - c.outage_duration<" + startingDate + ")))";
		q += " order by c.service_id, c.status_timestamp desc";
		LOG.Debug("****** Main report query: " + q);
		PreparedStatement st = conn.prepareStatement(q);
		ResultSet rs = st.executeQuery();

		String srvName = "";
		String citype = "";
		q = "select service_instance_name, citype from smdbadmin.service_instances where service_instance_id=" + service_instance_id;
		//LOG.Debug(q);
		PreparedStatement sst = conn.prepareStatement(q);
		ResultSet srs = sst.executeQuery();
		srs.next();
		srvName = srs.getString(1);
		citype = srs.getString(2);
		srs.close();
		sst.close();
		
		double totalOutageDuration = 0.0;
		ReportBean report = new ReportBean();
		report.service_instance_id.set(service_instance_id);
		report.service_instance_name.set(srvName);
		report.citype.set(citype);
		report.startDate.set(startingDate);
		report.endDate.set(endingDate);
		report.startDateStr.set(Helper.UTCToDateStr(startingDate));
		report.endDateStr.set(Helper.UTCToDateStr(endingDate));
		report.availabilityMetric.set(100.0);
		report.outageDuration.set(0.0);
		report.queryType.set(queryType);


		int currentSid = 0;
		int outageCount = 0;
		report.currentStatus.set(0);
		totalOutageDuration = 0.0;
		while(rs.next()) {
			report.currentStatus.set(rs.getInt("current_status"));
			outageCount++;
			
			currentSid = rs.getInt(3);

			double outageDuration = rs.getDouble(1);
			double outageTimestamp = rs.getDouble(2);
			
			OutageBean outage = new OutageBean();
			outage.service_instance_id.set(currentSid);
			outage.service_instance_name.set(srvName);
			outage.outageStart.set((int)(outageTimestamp - outageDuration));
			outage.outageStartStr.set(Helper.UTCToDateStr((outageTimestamp - outageDuration)));
			outage.outageEnd.set((int)outageTimestamp);
			outage.outageEndStr.set(Helper.UTCToDateStr(outageTimestamp));

			// set outage duration within the given start-end dates
			if(outageTimestamp > endingDate && outageTimestamp - outageDuration < startingDate)
			{
				outage.outageDuration.set(Helper.DoublePrecision2Double((double)(endingDate - startingDate)));
			}
			else if(outageTimestamp > endingDate && outageTimestamp - outageDuration > startingDate)
			{
				outage.outageDuration.set(Helper.DoublePrecision2Double((double)(endingDate - (outageTimestamp - outageDuration))));
			}
			else if(outageTimestamp < endingDate && outageTimestamp - outageDuration > startingDate)
			{
				outage.outageDuration.set(Helper.DoublePrecision2Double((double)outageDuration));
			}
			else if(outageTimestamp < endingDate && outageTimestamp - outageDuration < startingDate)
			{
				outage.outageDuration.set(Helper.DoublePrecision2Double((double)(outageTimestamp - startingDate)));
			}

			totalOutageDuration += outage.outageDuration.get();
			
			report.Outages.add(outage);
		}

		rs.close();
		st.close();

		// ongoing outage
		int currentTime = (int) (Helper.CurrentTimeUTCMilliSecond() / 1000);
		int endTime = currentTime;
		if(endingDate < endTime)
			endTime = endingDate;
		OutageBean ongoingOutage = GetOngoingOutage(service_instance_id, startingDate, endingDate, queryType, report, srvName);
		if(ongoingOutage != null) {
			report.Outages.add(ongoingOutage);
			if(ongoingOutage.outageStart.get() < endingDate)
			{
				if(ongoingOutage.outageStart.get() >= startingDate) {
					ongoingOutage.outageDuration.set(Double.parseDouble(Helper.DoublePrecision2((double)(endTime - ongoingOutage.outageStart.get()))));
					if(service_instance_id == 11289)
						LOG.Debug("****** 1 totaling " + totalOutageDuration + "  " + ongoingOutage.outageDuration.get());

				}
				else {
					ongoingOutage.outageDuration.set(Double.parseDouble(Helper.DoublePrecision2((double)(endTime - startingDate))));
					if(service_instance_id == 11289)
						LOG.Debug("****** 2 totaling " + totalOutageDuration + "  " + ongoingOutage.outageDuration.get());
				}
			}

			totalOutageDuration += ongoingOutage.outageDuration.get();
			
			LOG.Debug("****** totaling " + totalOutageDuration);
		}

		int dateRange = endingDate - startingDate;
		LOG.Debug("****** " + totalOutageDuration + " " + dateRange);
		double averageAvailability = ((dateRange - totalOutageDuration) / dateRange) * 100;
		if(dateRange == 0)
			averageAvailability = 0.0;
		report.availabilityMetric.set(Helper.DoublePrecision2Double(averageAvailability));
		report.outageDuration.set(Helper.DoublePrecision2Double(totalOutageDuration));
		
		if(averageAvailability < 0) {
			report.availabilityMetric.set(0.0);
			report.outageDuration.set(Helper.DoublePrecision2Double((double)(endTime - startingDate)));
		}

		return report;
	}
	
	private OutageBean GetOngoingOutage(int sid, int startingDate, int endingDate, String queryType, ReportBean report, String srvName) throws SQLException {
		//String srvName = "";
		
		OutageBean outage = null;
		// additionally add the ongoing outages data once
		String q = "select i.current_status, c.outage_duration, c.status_timestamp from smdbadmin.service_instances i";
		q += " left join smdbadmin.service_status_change c on(i.service_instance_id=c.service_id and c.status_timestamp<" + endingDate + ")";
		q += " where c.service_id=" + sid + " and c.service_status=5 and i.current_status=5";
		q += " order by c.status_timestamp desc limit 1";
		//LOG.Debug("Main report ongoing outage query: " + q);
		if(sid == 11289)
			LOG.Debug("Main report ongoing outage query: " + q);
		PreparedStatement st2 = conn.prepareStatement(q);
		ResultSet rs2 = st2.executeQuery();
		double ongoingOutage = 0.0;
		while(rs2.next()) {
			report.currentStatus.set(rs2.getInt("current_status"));
			
			if(rs2.getInt("current_status") == 5) {
				double outageStartTimestamp = rs2.getDouble(3);
				outage = new OutageBean();
				outage.service_instance_id.set(sid);
				outage.service_instance_name.set(srvName);
				outage.outageStart.set((int)(outageStartTimestamp));
				outage.outageStartStr.set(Helper.UTCToDateStr(outageStartTimestamp));
				outage.outageEnd.set(0);
				outage.outageEndStr.set(RM.GetLabelString("ONGOING_OUTAGE"));
				if(outageStartTimestamp < endingDate)
				{
					if(outageStartTimestamp >= startingDate)
						outage.outageDuration.set(Helper.DoublePrecision2Double((double)(endingDate - outageStartTimestamp)));
					else
						outage.outageDuration.set(Helper.DoublePrecision2Double((double)(endingDate - startingDate)));
				}
				
				ongoingOutage = outage.outageDuration.get();
			}
		}
		rs2.close();
		st2.close();
		
		return outage;
	}
	
	public String CreateNewReport(int sid, int startingDate, int endingDate, String queryType, String name, String username) {
		LOG.resultBean.Reset();

		int DayCount = 0;
		switch(queryType)
		{
		case QueryType.TODAY:
			startingDate = Helper.TodayStartingDate().intValue();
			endingDate = startingDate + 86400;
			break;
		case QueryType.LAST_DAY:
			startingDate = Helper.LastDayStartingDate().intValue();
			endingDate = startingDate + 86400;
			break;
		case QueryType.LAST_7_DAYS:
			startingDate = Helper.LastSevenDaysStartingDate().intValue();
			endingDate = startingDate + 86400 * 7;
			break;
		case QueryType.LAST_WEEK:
			startingDate = Helper.LastWeekStartingDate().intValue();
			endingDate = startingDate + 86400 * 7;
			break;
		case QueryType.LAST_MONTH:
			startingDate = Helper.LastMonthStartingDate().intValue();
			DayCount = Helper.DayCountOfMonth(startingDate * 1000L);
			endingDate = startingDate + 86400 * DayCount;
			break;
		case QueryType.CURRENT_MONTH:
			startingDate = Helper.CurrentMonthStartingDate().intValue();
			DayCount = Helper.DayCountOfMonth(startingDate * 1000L);
			endingDate = startingDate + 86400 * DayCount;
			break;
		case QueryType.DATE_RANGE:
			break;
		}
		
		ReportBean report = new ReportBean();
		report.service_instance_id.set(sid);
		report.endDate.set(startingDate);
		report.startDate.set(endingDate);
		report.queryType.set(queryType);
		
		String q = "select count(*) from smdbadmin.report where report_name=?";
		try {
			OpenConn();
			
			PreparedStatement st1 = conn.prepareStatement(q);
			st1.setString(1, name);
			ResultSet rs1 = st1.executeQuery();
			rs1.next();
			if(rs1.getInt(1) > 0) {
				LOG.Warn(RM.GetErrorString("WARN_REPORT_ALREADYEXIST"));
			} else {
				q = "insert into smdbadmin.report(report_name, service_instance_id, query_type, starting_date, ending_date,";
				q += " createdby, createdat)";
				q += " values(?, ?, ?, ?, ?, (select userid from smdbadmin.users where username=?), EXTRACT(EPOCH FROM (select current_timestamp)))";
				PreparedStatement st2 = conn.prepareStatement(q);
				st2.setString(1, name);
				st2.setInt(2, sid);
				st2.setString(3, queryType);
				st2.setInt(4, startingDate);
				st2.setInt(5, endingDate);
				st2.setString(6, username);
				st2.executeUpdate();
				st2.close();
				LOG.Success(RM.GetErrorString("SUCCESS_REPORT_Created"));
			}

			rs1.close();
			st1.close();
			
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		return LOG.resultBean.Serialize();
	}
	
	public String UpdateReport(int sid, int startingDate, int endingDate, String queryType, String name) {
		LOG.resultBean.Reset();

		int DayCount = 0;
		int repcount=0;
		switch(queryType)
		{
		case QueryType.TODAY:
			startingDate = Helper.TodayStartingDate().intValue();
			endingDate = startingDate + 86400;
			break;
		case QueryType.LAST_DAY:
			startingDate = Helper.LastDayStartingDate().intValue();
			endingDate = startingDate + 86400;
			break;
		case QueryType.LAST_7_DAYS:
			startingDate = Helper.LastSevenDaysStartingDate().intValue();
			endingDate = startingDate + 86400 * 7;
			break;
		case QueryType.LAST_WEEK:
			startingDate = Helper.LastWeekStartingDate().intValue();
			endingDate = startingDate + 86400 * 7;
			break;
		case QueryType.LAST_MONTH:
			startingDate = Helper.LastMonthStartingDate().intValue();
			DayCount = Helper.DayCountOfMonth(startingDate * 1000L);
			endingDate = startingDate + 86400 * DayCount;
			break;
		case QueryType.CURRENT_MONTH:
			startingDate = Helper.CurrentMonthStartingDate().intValue();
			DayCount = Helper.DayCountOfMonth(startingDate * 1000L);
			endingDate = startingDate + 86400 * DayCount;
			break;
		case QueryType.DATE_RANGE:
			break;
		}
		
		ReportBean report = new ReportBean();
		report.service_instance_id.set(sid);
		report.endDate.set(startingDate);
		report.startDate.set(endingDate);
		report.queryType.set(queryType);
		
		try {
			OpenConn();
			
			String q = "select count(*) from smdbadmin.report where report_name=?";
			
			PreparedStatement st1 = conn.prepareStatement(q);
			st1.setString(1, name);
			ResultSet rs1 = st1.executeQuery();
			rs1.next();
			
			if(rs1.getInt(1) > 0) {
				q = "UPDATE smdbadmin.report SET service_instance_id=?, query_type=?, starting_date=?, ending_date=?";
				q +=" WHERE report_name=?";
				PreparedStatement st3 = conn.prepareStatement(q);
				st3.setInt(1, sid);
				st3.setString(2, queryType);
				st3.setInt(3, startingDate);
				st3.setInt(4, endingDate);
				st3.setString(5, name);
				st3.executeUpdate();
				st3.close();
				LOG.Success(RM.GetErrorString("SUCCESS_REPORT_Updated"));
			} else {
				q = "insert into smdbadmin.report(report_name, service_instance_id, query_type, starting_date, ending_date)";
				q += " values(?, ?, ?, ?, ?)";
				PreparedStatement st2 = conn.prepareStatement(q);
				st2.setString(1, name);
				st2.setInt(2, sid);
				st2.setString(3, queryType);
				st2.setInt(4, startingDate);
				st2.setInt(5, endingDate);
				st2.executeUpdate();
				st2.close();
				LOG.Success(RM.GetErrorString("SUCCESS_REPORT_Created"));
			}
			
			rs1.close();
			st1.close();
			
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		return LOG.resultBean.Serialize();
	}
	
	public ReportDefBean GetCreatedReport(int report_id) throws SQLException {
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
			report.report_name.set(rs.getString(1));
			report.service_instance_id.set(rs.getInt(2));
			report.service_instance_name.set(rs.getString(3));
			report.startDate.set(rs.getInt(5));
			report.endDate.set(rs.getInt(6));
			report.startDateStr.set(Helper.UTCToDateStr(rs.getInt(5)));
			report.endDateStr.set(Helper.UTCToDateStr(rs.getInt(6)));
			report.queryType.set(rs.getString(4));
		}

		rs.close();
		st.close();

		return report;
	}
	
	public String GetCreatedReportOutput(int report_id) {
		LOG.resultBean.Reset();
		
		BeanList<ReportBean> reportList = new BeanList<ReportBean>();
		ReportBean report = new ReportBean();
		try {
			OpenConn();
			
			ReportDefBean reportDef = GetCreatedReport(report_id);
			report = GetOutageReport(reportDef.service_instance_id.get(), reportDef.startDate.get(), reportDef.endDate.get(), true, reportDef.queryType.get());
			
			CloseConn();
		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		}
		
		reportList.add(report);
		
		reportList.sort((r1, r2) -> r1.availabilityMetric.get().compareTo(r2.availabilityMetric.get()));
		
		if(LOG.IsError())
			return LOG.resultBean.Serialize();
		return reportList.Serialize();
	}
	
	public ReportBean GetCreatedReportBean(int report_id) {
		LOG.resultBean.Reset();

		ReportBean report = new ReportBean();
		try {
			OpenConn();
			
			ReportDefBean reportDef = GetCreatedReport(report_id);
			report = GetOutageReport(reportDef.service_instance_id.get(), reportDef.startDate.get(), reportDef.endDate.get(), true, reportDef.queryType.get());
			
			CloseConn();
		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		}
		
		return report;
	}

	private BeanList<ReportBean> GetServiceTreeReportByReportId(int report_id) {
		usedSid = new ArrayList<Integer>();
		srvTreeReport = new BeanList<ReportBean>();
		
		try {
			OpenConn();
			
			ReportDefBean reportDef = GetCreatedReport(report_id);
			this.GetCalculatedOutageReportOfServiceTree(
					reportDef.service_instance_id.get(),
					reportDef.startDate.get(),
					reportDef.endDate.get(),
					true, reportDef.queryType.get());
			
			CloseConn();
		} catch (IOException e) {
			LOG.Error(e);
		} catch (SQLException e) {
			LOG.Error(e);
		}

		srvTreeReport.sort((r1, r2) -> r1.availabilityMetric.get().compareTo(r2.availabilityMetric.get()));
		
		return srvTreeReport;

	}
	
	public String GetCreatedReportList(String username, List<Integer> groups, boolean isAdmin) {
		LOG.resultBean.Reset();
		
		BeanList<ReportDefBean> reportList = new BeanList<ReportDefBean>();

		try {
			OpenConn();
			
			String uidList = "";
			if(groups.size() > 0) {
				String gidList = "";
				int cnt = 0;
				for(int gid : groups) {
					if(cnt > 0)
						gidList += ",";
					gidList += gid;
					cnt++;
				}

				String gq = "select user_id from smdbadmin.user_groups where group_id in(" + gidList + ")";
				PreparedStatement gst = conn.prepareStatement(gq);
				ResultSet grs = gst.executeQuery();
				int c = 0;
				while(grs.next()) {
					if(c>0)
						uidList += ",";
					uidList += grs.getInt("user_id");
					c++;
				}
				grs.close();
				gst.close();

			}
			
			String q = "select r.report_id, r.report_name, r.service_instance_id, i.service_instance_name,";
			q += " r.query_type, r.starting_date, r.ending_date, u.username from smdbadmin.report r";
			q += " left join smdbadmin.service_instances i on(i.service_instance_id=r.service_instance_id)";
			q += " left join smdbadmin.users u on(r.createdby=u.userid)";
			// filter for user
			if(!isAdmin)
				q += " where r.createdby=(select userid from smdbadmin.users where username=?)";

			/*if(groups.size() > 0) {
				q += " where r.createdby in(" + uidList + ")";
			} else {
				q += " where r.createdby=(select userid from smdbadmin.users where username=?)";
			}*/
			
			q += " order by service_instance_id";
			PreparedStatement st = conn.prepareStatement(q);
			if(!isAdmin)
				st.setString(1, username);
			ResultSet rs = st.executeQuery();

			while(rs.next()) {
				ReportDefBean report = new ReportDefBean();
				report.report_id.set(rs.getInt(1));
				report.report_name.set(rs.getString(2));
				report.service_instance_id.set(rs.getInt(3));
				report.service_instance_name.set(rs.getString(4));
				report.startDate.set(rs.getInt(6));
				report.endDate.set(rs.getInt(7));
				report.startDateStr.set(Helper.UTCToDateStr(rs.getInt(6)));
				report.endDateStr.set(Helper.UTCToDateStr(rs.getInt(7)));
				report.queryType.set(rs.getString(5));
				report.createdby.set(rs.getString("username"));
				reportList.add(report);
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
		return reportList.Serialize();
	}

	public String GetMainServiceList() {
		LOG.resultBean.Reset();
		
		BeanList<ServiceBean> serviceList = new BeanList<ServiceBean>();
		try {
			OpenConn();
			
			String q = "select service_instance_id, service_instance_name from smdbadmin.service_instances ";
			q += " where citype='CI.BUSINESSMAINSERVICE' order by service_instance_name asc";
			PreparedStatement st = conn.prepareStatement(q);
			ResultSet rs = st.executeQuery();
			
			while(rs.next()) {
				ServiceBean srvBean = new ServiceBean();
				srvBean.service_instance_id.set(rs.getInt(1));
				srvBean.service_instance_name.set(rs.getString(2));
				serviceList.add(srvBean);
			}
			
			rs.close();
			st.close();

		} catch (IOException | SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		if(LOG.IsError())
			return LOG.resultBean.Serialize();
		return serviceList.Serialize();
	}
	
	public String GetMainServiceCount() {
		LOG.resultBean.Reset();
		String serviceCount = "";
		try {
			OpenConn();
			
			String q = "select count(*) from smdbadmin.service_instances ";
			q += " where citype='CI.BUSINESSMAINSERVICE'";
			PreparedStatement st = conn.prepareStatement(q);
			ResultSet rs = st.executeQuery();
			int count = 0;
			while(rs.next()) {
				count = rs.getInt(1);
			}
			serviceCount = "{\"count\":";
			serviceCount += count;
			serviceCount += "}";

			rs.close();
			st.close();

		} catch (IOException | SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		return serviceCount;		
		
	}
	
	public String GetAllServiceList(HashMap<Integer, String> allowedServiceList, boolean isAdmin) {
		LOG.resultBean.Reset();
		
		String srvIdList = "";
		int cnt = 0;
		boolean sidzerocheck = false;
		for(int sid : allowedServiceList.keySet()) {
			if(sid == 0) {
				sidzerocheck = true;
			}
			if(cnt>0)
				srvIdList += ",";
			srvIdList += sid;
			
			cnt++;
		}
		if(sidzerocheck == true) {
			srvIdList = "0";
		}
		
		BeanList<ServiceBean> serviceList = new BeanList<ServiceBean>();
		try {
			OpenConn();
			
			String q = "select service_instance_id, service_instance_name from smdbadmin.service_instances where citype='CI.BUSINESSMAINSERVICE'";
			if((!isAdmin || !allowedServiceList.containsKey(0))  && (!srvIdList.trim().equals("") && !srvIdList.trim().equals("0")))
				q += " and service_instance_id in(" + srvIdList + ")";
			PreparedStatement st = conn.prepareStatement(q);
			ResultSet rs = st.executeQuery();
			
			while(rs.next()) {
				ServiceBean srvBean = new ServiceBean();
				srvBean.service_instance_id.set(rs.getInt(1));
				srvBean.service_instance_name.set(rs.getString(2));
				serviceList.add(srvBean);
			}
			
			rs.close();
			st.close();

		} catch (IOException | SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		if(LOG.IsError())
			return LOG.resultBean.Serialize();
		return serviceList.Serialize();
	}
	
	public String DeleteReport(int reportid) {
		LOG.resultBean.Reset();
		try{
			OpenConn();
			String q = "delete from smdbadmin.report where report_id=?;"
					+ "delete from smdbadmin.reportjob_list where report_id=?;";
			PreparedStatement stmt = conn.prepareStatement(q);
			stmt.setInt(1, reportid);
			stmt.setInt(2, reportid);
			stmt.executeUpdate();
			stmt.close();
			
			LOG.Success(RM.GetErrorString("SUCCESS_Deleted_Report"));
			
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		return LOG.resultBean.Serialize();
	}
	
	public String GetReport(int reportid) {
		LOG.resultBean.Reset();
		ReportDefBean report=new ReportDefBean();
		try{
			OpenConn();
			String q = "select * from smdbadmin.report where report_id=?;";
			PreparedStatement statement = conn.prepareStatement(q);
			statement.setInt(1, reportid);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				report.report_id.set(rs.getInt("report_id"));
				report.report_name.set(rs.getString("report_name"));
				report.service_instance_id.set(rs.getInt("service_instance_id"));
				report.queryType.set(rs.getString("query_type"));
				report.startDateStr.set(rs.getString("starting_date"));
				report.endDateStr.set(rs.getString("ending_date"));
			}
			rs.close();
			statement.close();
			LOG.Success(RM.GetErrorString("SUCCESS_Retreived_Report"));
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return report.Serialize();
	}
	
}
