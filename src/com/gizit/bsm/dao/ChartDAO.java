package com.gizit.bsm.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.SortedSet;

import com.gizit.bsm.beans.BeanList;
import com.gizit.bsm.beans.NodeGroupBean;
import com.gizit.bsm.beans.NodeGroupBean.Member;
import com.gizit.bsm.dao.ReportDAO.QueryType;
import com.gizit.bsm.beans.ReportBean;
import com.gizit.bsm.beans.ResultMessageBean;
import com.gizit.bsm.helpers.Helper;
import com.gizit.managers.ConnectionManager;
import com.gizit.managers.LogManager;
import com.gizit.managers.ResourceManager;
import com.google.gson.Gson;

public class ChartDAO extends DAO {

	ReportDAO reportDAO;
	public ChartDAO() {
		super(ChartDAO.class);
		
		reportDAO = new ReportDAO();
	}
	
	public String GetAvailabilityMetric(int service_instance_id, int startingDate, int endingDate, String queryType) {
		LOG.resultBean.Reset();
		
		ReportBean report = new ReportBean();
		try {
			report = reportDAO.GetReportBeanOfSingleService(service_instance_id, startingDate, endingDate, false, queryType);
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		}
		
		if(LOG.IsError())
			return LOG.resultBean.Serialize();
		return Helper.DoublePrecision2(report.availabilityMetric.get());
	}

	public String GetAvailabilityMetricList(int[] sidList, int startingDate, int endingDate, String queryType) {
		LOG.resultBean.Reset();
		
		HashMap<Integer, String>[] metricList = (HashMap<Integer, String>[]) new HashMap[sidList.length];
		try {
			metricList = reportDAO.GetReportBeanOfServiceList(sidList, startingDate, endingDate, false, queryType);
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		}

		Gson gson = new Gson();
        String metricJson = gson.toJson(metricList);

		if(LOG.IsError())
			return LOG.resultBean.Serialize();
		return metricJson;
		
	}

	public String LastDayAvailability(int sid)
    {
    	LOG.resultBean.Reset();

    	double metric = 100;
        try {
			OpenConn();

	        String q = "select availability_metric from smdbadmin.daily_availability";
	        q += " where service_instance_id=? and timewindow=?";
	        q += " order by timewindow desc limit 1";
	        PreparedStatement pst = conn.prepareStatement(q);
	        pst.setInt(1, sid);
	        pst.setInt(2, Integer.parseInt(Helper.CurrentTimeWindow()));
	        ResultSet rs = pst.executeQuery();
	        while(rs.next()){
	        	metric = rs.getDouble(1);
	        }
	        rs.close();
	        pst.close();
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();

        DecimalFormat df = new DecimalFormat("###.##");
        return df.format(metric);
    }
	
	public String LastDayAvailabilityList(int[] sid)
    {
    	LOG.resultBean.Reset();

    	HashMap<Integer, String> metricList = new HashMap<Integer, String>();
    	double metric = 100;
        try {
			OpenConn();

			String sidList = Helper.joinInts(sid, ",");
			DecimalFormat df = new DecimalFormat("###.##");
			
			//System.out.println("sdfs " + destr);
	        //Statement st = conn.createStatement();
	        String q = "select s.service_instance_id, a.availability_metric, s.current_status from smdbadmin.service_instances s" + 
	        		" left join smdbadmin.daily_availability a  on(s.service_instance_id=a.service_instance_id and a.timewindow=?)" + 
	        		" where s.service_instance_id in(" + sidList + ")";
	        PreparedStatement pst = conn.prepareStatement(q);
	        pst.setInt(1, Integer.parseInt(Helper.CurrentTimeWindow()));
	        ResultSet rs = pst.executeQuery();

	        HashMap<Integer, Boolean> sidUsed = new HashMap<Integer, Boolean>();
	        while(rs.next()){
	        	sidUsed.put(rs.getInt(1), true);
	        	double mm = rs.getDouble(2);
	        	if(mm == 0L)
	        		metricList.put(rs.getInt(1), df.format(100.0) + ":" + rs.getString(3));
	        	else
	        		metricList.put(rs.getInt(1), df.format(mm) + ":" + rs.getString(3));
	        }

	        rs.close();
	        pst.close();
	        
	        /*for(int id : sid) {
	        	if(!sidUsed.containsKey(id)) {
	        		metricList.put(id, df.format(100.0));
	        	}
	        }*/

		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        
    	HashMap<Integer, String>[] metricListArr = (HashMap<Integer, String>[]) new HashMap[metricList.size()];
    	int i = 0;
        for (int key : metricList.keySet())
        {
        	HashMap<Integer, String> aaa = new HashMap<Integer, String>();
        	aaa.put(key, metricList.get(key));
        	metricListArr[i] = aaa;
        	i++;
        }
        Gson gson = new Gson();
        String metricJson = gson.toJson(metricListArr);
        
        return metricJson;
    }
	
    public class AvailabilityMetric{
    	public int da_id;
    	public String timestamp;
    	public double outage_duration;
    	public double availability_metric;
    	public int timewindow;
    }
    
    public String GetChart(int service_instance_id, int startingDate, int endingDate, String queryType) {
    	LOG.resultBean.Reset();
		
    	String chart = "";
    	String labels = "";
    	String dataSet = "";
    	
    	BeanList<ReportBean> reportList = new BeanList<ReportBean>();
		try {
			reportList = reportDAO.GetListOfDailyReport(service_instance_id, startingDate, endingDate, false, queryType);
			reportList.sort((r1, r2) -> r1.startDate.get().compareTo(r2.startDate.get()));
			
			labels = "[";
	        dataSet = "[";
	        int itemcnt = 0;
			for(ReportBean report : reportList) {
				if(itemcnt > 0) {
	        		labels += ",";
	        		dataSet += ",";
	        	}
	        	labels += "\"" + report.startDateStr.get().split(" ")[0] + "\"";
	        	dataSet += Helper.DoublePrecision2(report.availabilityMetric.get());
	        	
	        	itemcnt++;
			}
			dataSet += "]";
	        labels += "]";
	        
	        String chartTitle = "";
	        switch(queryType)
			{
			case QueryType.TODAY:
				chartTitle = "Today";
				break;
			case QueryType.LAST_DAY:
				chartTitle = "Last day";
				break;
			case QueryType.LAST_7_DAYS:
				chartTitle = "Last 7 days";
				break;
			case QueryType.LAST_WEEK:
				chartTitle = "Last week";
				break;
			case QueryType.LAST_MONTH:
				chartTitle = "Last month";
				break;
			case QueryType.DATE_RANGE:
				break;
			}
	        
	        chart = "{";
	        chart += "\"type\":\"bar\",";
	        chart += "\"data\":{\"labels\":" + labels + ",";
	        chart += "\"datasets\":";
	        chart += "[{";
	        chart += "\"label\":\"" + chartTitle + " daily availability chart\", \"backgroundColor\": \"#EEE\"," + 
	        		"\"borderWidth\": 1," + 
	        		"\"hoverBackgroundColor\": \"rgba(232,105,90,0.8)\"," + 
	        		"\"hoverBorderColor\": \"orange\"," + 
	        		"\"scaleStepWidth\": 1,";
	        
	        chart += "\"data\":" + dataSet;
	        chart += "}]},";
	        chart += "\"options\": {" + 
	        		"					  \"legend\": {" + 
	        		"				            \"labels\": {" + 
	        		"				                \"fontColor\": \"white\"," + 
	        		"				                \"fontSize\": 12" + 
	        		"				            }" + 
	        		"				        }," + 
	        		"				        \"scales\": {" + 
	        		"				            \"yAxes\": [{" + 
	        		"				                \"ticks\": {" + 
	        		"				                    \"beginAtZero\":true," + 
	        		"				                    \"steps\":10," + 
	        		"				                    \"stepValue\":10," + 
	        		"				                    \"max\":100," + 
	        		"				                    \"fontColor\": \"white\"" + 
	        		"				                }" + 
	        		"				            }]," + 
	        		"				            \"xAxes\": [{" + 
	        		"				                \"ticks\": {" + 
	        		"				                    \"fontColor\": \"white\"" + 
	        		"				                }" + 
	        		"				            }]" + 
	        		"				        }" + 
	        		"				    }";
	        chart += "}";
	        
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		}
		
		if(LOG.IsError())
			return LOG.resultBean.Serialize();
		return chart;
    }
    
    public String GetWeeklyChart(int sid, int weekCount)
    {
    	LOG.resultBean.Reset();
    	
    	String chart = "";
    	String labels = "";
    	String dataSet = "";
        try {
        	
			OpenConn();
	        Statement st;
	        st = conn.createStatement();
	        int limit = 7 * weekCount;
	        int beginingTW = Integer.parseInt(Helper.CurrentTimeWindow());
	        int endingTW = beginingTW - (6 * 86400);
	        
	        String q = "select daily_availability_id, to_timestamp(timewindow) tw, outage_duration, availability_metric, timewindow from smdbadmin.daily_availability";
	        q += " where service_instance_id=" + sid + " and timewindow>=" + endingTW + " order by timewindow desc limit " + limit;
	        ResultSet rs = st.executeQuery(q);

	        HashMap<Integer, AvailabilityMetric> metricList = new HashMap<Integer, AvailabilityMetric>();
	        for(int i=0; i<7; i++)
	        {
	        	AvailabilityMetric am = new AvailabilityMetric();
	        	am.availability_metric = 100.0;
	        	am.da_id = 0;
	        	am.outage_duration = 0;
	        	am.timewindow = beginingTW - i * 86400;

	        	Calendar cal = Calendar.getInstance();
	        	cal.setTimeInMillis(am.timewindow * 1000L);
				String dateStr = (cal.get(Calendar.YEAR)) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH);
	        	am.timestamp = dateStr;
	        	
	        	metricList.put(beginingTW - i * 86400, am);
	        }

	        DecimalFormat df = new DecimalFormat("###.##");
	        while(rs.next()){
	        	int tw = rs.getInt(5);
	        	AvailabilityMetric m = metricList.get(tw);
	        	m.availability_metric = Double.parseDouble(df.format(rs.getDouble(4)));

	        	Calendar cal = Calendar.getInstance();
	        	cal.setTimeInMillis(tw * 1000L);
				String dateStr = (cal.get(Calendar.YEAR)) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH);
	        	//m.timestamp = dateStr;
	        }
	        
	        labels = "[";
	        dataSet = "[";
	        int itemcnt = 0;
	        Map<Integer, AvailabilityMetric> sortedMetricList = new TreeMap<Integer, AvailabilityMetric>(metricList);
	        Iterator<Entry<Integer, AvailabilityMetric>> it = sortedMetricList.entrySet().iterator();
	        while(it.hasNext()) {
	        	Map.Entry<Integer, AvailabilityMetric> pair = (Map.Entry<Integer, AvailabilityMetric>)it.next();
	        	AvailabilityMetric m = (AvailabilityMetric)pair.getValue();
	        	if(itemcnt > 0) {
	        		labels += ",";
	        		dataSet += ",";
	        	}
	        	labels += "\"" + m.timestamp + "\"";
	        	dataSet += m.availability_metric;
	        	
	        	itemcnt++;
	        }
	        
	        dataSet += "]";
	        labels += "]";
	        
	        chart = "{";
	        chart += "\"type\":\"bar\",";
	        chart += "\"data\":{\"labels\":" + labels + ",";
	        chart += "\"datasets\":";
	        chart += "[{";
	        chart += "\"label\":\"" + weekCount + " week daily availability chart\", \"backgroundColor\": \"#EEE\"," + 
	        		"\"borderWidth\": 1," + 
	        		"\"hoverBackgroundColor\": \"rgba(232,105,90,0.8)\"," + 
	        		"\"hoverBorderColor\": \"orange\"," + 
	        		"\"scaleStepWidth\": 1,";
	        
	        chart += "\"data\":" + dataSet;
	        chart += "}]},";
	        chart += "\"options\": {" + 
	        		"					  \"legend\": {" + 
	        		"				            \"labels\": {" + 
	        		"				                \"fontColor\": \"white\"," + 
	        		"				                \"fontSize\": 12" + 
	        		"				            }" + 
	        		"				        }," + 
	        		"				        \"scales\": {" + 
	        		"				            \"yAxes\": [{" + 
	        		"				                \"ticks\": {" + 
	        		"				                    \"beginAtZero\":true," + 
	        		"				                    \"steps\":10," + 
	        		"				                    \"stepValue\":10," + 
	        		"				                    \"max\":100," + 
	        		"				                    \"fontColor\": \"white\"" + 
	        		"				                }" + 
	        		"				            }]," + 
	        		"				            \"xAxes\": [{" + 
	        		"				                \"ticks\": {" + 
	        		"				                    \"fontColor\": \"white\"" + 
	        		"				                }" + 
	        		"				            }]" + 
	        		"				        }" + 
	        		"				    }";
	        chart += "}";

	        rs.close();
	        st.close();
	        
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

        if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return chart;
    }
    
    public String GetStatusPieChart() {
    	LOG.resultBean.Reset();
    	
    	String chart = "";
    	String labels = "";
    	String dataSet = "";
    	String bgColors = "";
    	String hoverColors = "";
    	try {
			OpenConn();
			
			String q = "select count(*) cnt, current_status from smdbadmin.service_instances";
	    	q += " where citype='CI.BUSINESSMAINSERVICE' group by current_status order by current_status";
	    	
	    	PreparedStatement st = conn.prepareStatement(q);
	    	ResultSet rs = st.executeQuery();
	    	
	    	labels = "[";
	        dataSet = "[";
	        bgColors = "[";
	        hoverColors = "[";
	        int itemcnt = 0;
	        while(rs.next()) {
				if(itemcnt > 0) {
	        		labels += ",";
	        		dataSet += ",";
	        		bgColors += ",";
	        		hoverColors += ",";
	        	}
				String lbl = "Good";
				switch(rs.getInt("current_status")) {
				case 0:
					lbl = "Good";
					bgColors += "\"green\"";
					hoverColors += "\"lightgreen\"";
					break;
				case 3:
					lbl = "Marginal";
					bgColors += "\"orange\"";
					hoverColors += "\"lightsalmon\"";
					break;
				case 5:
					lbl = "Bad";
					bgColors += "\"red\"";
					hoverColors += "\"lightpink\"";
					break;
				}
	        	labels += "\"" + lbl + "\"";
	        	dataSet += rs.getInt("cnt");
	        	
	        	itemcnt++;
			}
			dataSet += "]";
	        labels += "]";
	        bgColors += "]";
	        hoverColors += "]";
	        
	        rs.close();
	        st.close();
	    	
	        chart = "{";
	        chart += "\"type\":\"pie\",";
	        chart += "\"data\":{\"labels\":" + labels + ",";
	        chart += "\"datasets\":";
	        chart += "[{";
	        chart += "\"label\":\" Overall Service Status Distribution\"," + 
	                "\"backgroundColor\": " + bgColors + "," + 
	        		"\"borderWidth\": 1," + 
	        		"\"hoverBackgroundColor\": " + hoverColors + "," + 
	        		"\"hoverBorderColor\": \"orange\"," + 
	        		"\"scaleStepWidth\": 1,";
	        
	        chart += "\"data\":" + dataSet;
	        chart += "}]},";
	        chart += "\"options\": {" + 
	        		" \"title\": { \"display\": true, \"text\": \"Overall Service Status Distribution\" }, " +
	        		"					  \"legend\": {" + 
	        		"				            \"labels\": {" + 
	        		"				                \"fontColor\": \"black\"," + 
	        		"				                \"fontSize\": 12" + 
	        		"				            }" + 
	        		"				        }" + 
	        		"				    }";
	        chart += "}";
	    	
		} catch (IOException | SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

    	if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return chart;

    }
    
    
    public String GetLastSevenDayChartAllServices() {
    	LOG.resultBean.Reset();
		
    	String chart = "";
    	String labels = "";
    	String dataSet = "";
    	String labelSets = "";
    	String dataSets = "";

    	HashMap<String, BeanList<ReportBean>> reportList = new HashMap<String, BeanList<ReportBean>>();
		try {
			reportList = reportDAO.GetListOfDailyReportOfAllServices(QueryType.LAST_7_DAYS);
			
			chart = "{";
			
			dataSets = "[";
			labelSets = "[";
			int rcnt = 0;
			for(Map.Entry<String, BeanList<ReportBean>> entry : reportList.entrySet()) {
			    String srv = entry.getKey();
			    BeanList<ReportBean> reps = entry.getValue();

			    if(rcnt > 0)
			    {
			    	dataSets += ",";
			    	labelSets += ",";
			    }
			    labels = "[";
		        dataSet = "[";
		        int itemcnt = 0;
				for(ReportBean report : reps) {
					if(itemcnt > 0) {
		        		labels += ",";
		        		dataSet += ",";
		        	}
		        	labels += "\"" + report.startDateStr.get().split(" ")[0] + "\"";
		        	dataSet += Helper.DoublePrecision2(report.availabilityMetric.get());
		        	
		        	itemcnt++;
				}
				dataSet += "]";
		        labels += "]";
		        
		        chart += "\"type\":\"line\",";
		        chart += "\"data\":{\"labels\":" + labels + ",";
		        chart += "\"datasets\":";
		        chart += "[{";
		        chart += "\"label\":\"" + srv + "\", \"backgroundColor\": \"blue\"," + 
		        		"\"borderWidth\": 1," + 
		        		"\"hoverBackgroundColor\": \"rgba(232,105,90,0.8)\"," + 
		        		"\"hoverBorderColor\": \"orange\"," + 
		        		"\"scaleStepWidth\": 1,";
		        
		        chart += "\"data\":" + dataSet;
		        chart += "}]},";
		        
		        rcnt++;
			}
			dataSets += "]";
			labelSets += "]";

	        chart += "\"options\": {" + 
	        		"					  \"legend\": {" + 
	        		"				            \"labels\": {" + 
	        		"				                \"fontColor\": \"black\"," + 
	        		"				                \"fontSize\": 12" + 
	        		"				            }" + 
	        		"				        }," + 
	        		"				        \"scales\": {" + 
	        		"				            \"yAxes\": [{" + 
	        		"				                \"ticks\": {" + 
	        		"				                    \"beginAtZero\":true," + 
	        		"				                    \"steps\":10," + 
	        		"				                    \"stepValue\":10," + 
	        		"				                    \"max\":100," + 
	        		"				                    \"fontColor\": \"white\"" + 
	        		"				                }" + 
	        		"				            }]," + 
	        		"				            \"xAxes\": [{" + 
	        		"				                \"ticks\": {" + 
	        		"				                    \"fontColor\": \"white\"" + 
	        		"				                }" + 
	        		"				            }]" + 
	        		"				        }" + 
	        		"				    }";
	        chart += "}";
	        
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		}
		
		if(LOG.IsError())
			return LOG.resultBean.Serialize();
		return chart;
    }
    
    public String GetLastMonthAvailChartAllServicesPieChart(String queryType) {
    	LOG.resultBean.Reset();
    	
    	HashMap<Double, Integer> availCnt = new HashMap<Double, Integer>();
    	
    	String chart = "";
    	String labels = "";
    	String dataSet = "";
    	String bgColors = "";
    	String hoverColors = "";
    	try {
			OpenConn();
	    	
			BeanList<ReportBean> reportList = reportDAO.GetReportBeanOfAllServices(0, 0, false, queryType);
	    	
	    	labels = "[";
	        dataSet = "[";
	        bgColors = "[";
	        hoverColors = "[";
	        
	        for(ReportBean report : reportList) {
				if(availCnt.get(report.availabilityMetric.get()) == null) {
					availCnt.put(report.availabilityMetric.get(), 1);
				} else {
					availCnt.put(report.availabilityMetric.get(), availCnt.get(report.availabilityMetric.get()) + 1);
				}
			}
	        
	        int itemcnt = 0;
	        for(Map.Entry<Double, Integer> entry : availCnt.entrySet()) {
	        	Double key = entry.getKey();
	        	Integer val = entry.getValue();
	        	
	        	if(itemcnt > 0) {
	        		labels += ",";
	        		dataSet += ",";
	        		bgColors += ",";
	        		hoverColors += ",";
	        	}
	        	
	        	bgColors += "\"" + getRandomColor() + "\"";
	        	labels += "\"" + key + "%\"";
	        	dataSet += val;
	        	
	        	itemcnt++;
	        	
	        }
	        
			dataSet += "]";
	        labels += "]";
	        bgColors += "]";
	        hoverColors += "]";
	    	
	        String title = "";
	        switch(queryType) {
	        case QueryType.LAST_MONTH:
	        	title = "Last Month";
	        	break;
	        case QueryType.CURRENT_MONTH:
	        	title = "Current Month";
	        	break;
	        }
	        chart = "{";
	        chart += "\"type\":\"pie\",";
	        chart += "\"data\":{\"labels\":" + labels + ",";
	        chart += "\"datasets\":";
	        chart += "[{";
	        chart += "\"label\":\"" + title + " Availability Percentage Distribution \"," + 
	                "\"backgroundColor\": " + bgColors + "," + 
	        		"\"borderWidth\": 1," + 
	        		//"\"hoverBackgroundColor\": " + hoverColors + "," + 
	        		"\"hoverBorderColor\": \"orange\"," + 
	        		"\"scaleStepWidth\": 1,";
	        
	        chart += "\"data\":" + dataSet;
	        chart += "}]},";
	        chart += "\"options\": {\"responsive\": true," + 
	        		" \"title\": { \"display\": true, \"text\": \"" + title + " Availability Percentage Distribution\" }, " +
	        		"					  \"legend\": {" + 
	        		"				            \"labels\": {" + 
	        		"				                \"fontColor\": \"black\"," + 
	        		"				                \"fontSize\": 12" + 
	        		"				            }" + 
	        		"				        }" + 
	        		"				    }";
	        chart += "}";
	    	
		} catch (IOException | SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}

    	if(LOG.IsError())
        	return LOG.resultBean.Serialize();
        return chart;

    }
    
    private String getRandomColor() {
  	  String[] letters = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
  	  String color = "#";
  	  for (int i = 0; i < 6; i++) {
  	    color += letters[(int)Math.floor(Math.random() * 16)];
  	  }
  	  return color;
  }
    
    private int getRandomNum() {
    	  return (int)Math.floor(Math.random() * 16);
    }
    
    public String GetTopTenChart() {
    	LOG.resultBean.Reset();

    	HashMap<String, String> availSrv = new HashMap<String, String>();
    	
    	String chart = "";
    	String labels = "";
    	String dataSet = "";
    	String bgColors = "";
    	String hoverColors = "";
    	
		try {
			
			BeanList<ReportBean> reportList = reportDAO.GetReportBeanOfAllServices(0, 0, false, QueryType.LAST_MONTH);
	    	
	    	labels = "[";
	        dataSet = "[";
	        bgColors = "[";
	        hoverColors = "[";
	        
	        for(ReportBean report : reportList) {
	        	//availSrv.put(report.service_instance_id.get() + ":" + report.availabilityMetric.get(), report.service_instance_name.get());
	        	availSrv.put(report.service_instance_id.get() + ":" + Double.toString(report.availabilityMetric.get() - (double)getRandomNum()), "TestSrv" + report.service_instance_id.get());
			}
	        
	        SortedSet<String> keys = new TreeSet<>(availSrv.keySet());
	        int topten = 0;
	        for (String key : keys) {
	        	if(topten < 10) {
	        		String value = availSrv.get(key);
	        		
	        		if(topten > 0) {
		        		labels += ",";
		        		dataSet += ",";
		        		bgColors += ",";
		        		hoverColors += ",";
		        	}
		        	
		        	bgColors += "\"" + getRandomColor() + "\"";
		        	labels += "\"" + value + "\"";
		        	dataSet += key.split(":")[1];
		        	
	        	}
	        	
	        	topten ++;
	        }
	        
			dataSet += "]";
	        labels += "]";
	        bgColors += "]";
	        hoverColors += "]";
	        
	        // *---------------------------------------------------------------------
	        
	        chart = "{";
	        chart += "\"type\":\"bar\",";
	        chart += "\"data\":{\"labels\":" + labels + ",";
	        chart += "\"datasets\":";
	        chart += "[{";
	        chart += "\"label\":\"Availability\", \"backgroundColor\": \"#5882FA\"," + 
	        		"\"borderWidth\": 1," + 
	        		"\"hoverBackgroundColor\": \"rgba(232,105,90,0.8)\"," + 
	        		"\"hoverBorderColor\": \"orange\"," + 
	        		"\"scaleStepWidth\": 1,";
	        
	        chart += "\"data\":" + dataSet;
	        chart += "}]},";
	        chart += "\"options\": {" + 
	        		" \"title\": { \"display\": true, \"text\": \"10 Services having the worst availability\" }, " +
	        		"					  \"legend\": {" + 
	        		"				            \"labels\": {" + 
	        		"				                \"fontColor\": \"black\"," + 
	        		"				                \"fontSize\": 12" + 
	        		"				            }" + 
	        		"				        }," + 
	        		"				        \"scales\": {" + 
	        		"				            \"yAxes\": [{" + 
	        		"				                \"ticks\": {" + 
	        		"				                    \"beginAtZero\":true," + 
	        		"				                    \"steps\":10," + 
	        		"				                    \"stepValue\":10," + 
	        		"				                    \"max\":100," + 
	        		"				                    \"fontColor\": \"black\"" + 
	        		"				                }" + 
	        		"				            }]," + 
	        		"				            \"xAxes\": [{" + 
	        		"				                \"ticks\": {" + 
	        		"				                    \"fontColor\": \"black\"," + 
	        		"				                    \"overrideRotation\": 90" + 
	        		"				                }" + 
	        		"				            }]" + 
	        		"				        }" + 
	        		"				    }";
	        chart += "}";
	        
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		}
		
		if(LOG.IsError())
			return LOG.resultBean.Serialize();
		return chart;
    }
    
}
