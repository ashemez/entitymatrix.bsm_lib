package com.gizit.bsm.dao;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.gizit.bsm.beans.BeanList;
import com.gizit.bsm.beans.TreeBean;
import com.gizit.bsm.helpers.Helper;
import com.gizit.bsm.tree.TreeStructure;
import com.gizit.bsm.tree.TreeStructure.Node;
import com.google.gson.Gson;


public class ServiceStatDAO extends DAO {

	public ServiceStatDAO() {
		super(ServiceStatDAO.class);
	}
    
	HashMap<Integer, Integer> usedSidListForServiceTreeStatus = new HashMap<Integer, Integer>();
    HashMap<Integer, Integer>[] usedSidListArr;
    public String sidList = "";
    private void BuildServiceTreeStatusList(Node node){
    	if(!usedSidListForServiceTreeStatus.containsKey(node.sid)) {
    		usedSidListForServiceTreeStatus.put(node.sid, node.status);
        	if(!sidList.equals("")) {
        		sidList += "," + node.sid;
        	}
        	else {
        		sidList += node.sid;
        	}
        	
        	for(Node n : node.childNodes)
        	{
        		BuildServiceTreeStatusList(n);
        	}
        }
    }
    
    
    public String getServiceTreeStatusNew(String sidListStr) {
    	LOG.resultBean.Reset();

    	usedSidListForServiceTreeStatus = new HashMap<Integer, Integer>();
    	
    	/*String sidListChr = "";
    	int cnt = 0;
    	for(int sid : sidList) {
    		
    		if(cnt > 0)
    			sidListChr += ",";
    		
    		sidListChr += sid;
    		
    		cnt++;
    	}*/
    	
    	String json = "";
    	
        try{
        	OpenConn();
        	
        	String q = "SELECT service_instance_id, current_status from smdbadmin.service_instances where service_instance_id in(" + sidListStr + ")";
        	PreparedStatement stc = conn.prepareStatement(q);
			ResultSet RSc = stc.executeQuery();
			while(RSc.next())
			{
				usedSidListForServiceTreeStatus.put(RSc.getInt(1), RSc.getInt(2));
			}
			RSc.close();
			stc.close();

            usedSidListArr = (HashMap<Integer, Integer>[]) new HashMap[usedSidListForServiceTreeStatus.size()];
            
            int i = 0;
            for (int key : usedSidListForServiceTreeStatus.keySet())
            {
            	HashMap<Integer, Integer> aaa = new HashMap<Integer, Integer>();
            	aaa.put(key, usedSidListForServiceTreeStatus.get(key));
            	usedSidListArr[i] = aaa;
            	i++;
            }
            
            Gson gson = new Gson();
            json = gson.toJson(usedSidListArr);
            
            LOG.Success(RM.GetErrorString("SUCCESS"));
            
            return json;
        } catch (IOException | SQLException ex) {

        	LOG.Error(ex);
        	
        	return LOG.resultBean.Serialize();
        } finally {
        	CloseConn();
        }
    }
    
    private void GetServiceTreeStatus() {
    	LOG.resultBean.Reset();
    	
        try {
        	String q = "SELECT service_instance_id, current_status from smdbadmin.service_instances where service_instance_id in(" + sidList + ")";
        	PreparedStatement stc = conn.prepareStatement(q);
			ResultSet RSc = stc.executeQuery();
			while(RSc.next())
			{
				usedSidListForServiceTreeStatus.put(RSc.getInt(1), RSc.getInt(2));
			}
			RSc.close();
			stc.close();
		} catch (SQLException e) {
			LOG.Error(e);
		}
    }
    
    public String getServiceTreeStatus(int sid) {
    	LOG.resultBean.Reset();
    	
    	sidList = "";
    	String json = "";
        try{
        	usedSidListForServiceTreeStatus = new HashMap<Integer, Integer>();
            OpenConn();
            InitServiceTreeForStat(sid);
	    	TSBuild2(ts2.rootNode);
	    	BuildServiceTreeStatusList(ts2.rootNode);
	    	GetServiceTreeStatus();

            usedSidListArr = (HashMap<Integer, Integer>[]) new HashMap[usedSidListForServiceTreeStatus.size()];
            
            int i = 0;
            for (int key : usedSidListForServiceTreeStatus.keySet())
            {
            	HashMap<Integer, Integer> aaa = new HashMap<Integer, Integer>();
            	aaa.put(key, usedSidListForServiceTreeStatus.get(key));
            	usedSidListArr[i] = aaa;
            	i++;
            }
            
            Gson gson = new Gson();
            json = gson.toJson(usedSidListArr);
            
            LOG.Success(RM.GetErrorString("SUCCESS"));
            
            return json;
        } catch (IOException ex) {

        	LOG.Error(ex);
        	
        	return LOG.resultBean.Serialize();
        } finally {
        	CloseConn();
        }
    }
    
    TreeStructure ts;
    TreeStructure ts2;
    private void InitServiceTree(int sid) {
    	LOG.resultBean.Reset();
    	
    	ts = new TreeStructure();
    	try {
    		String q = "SELECT service_instance_name, current_status, citype, source_ci_id,";
    		q += " compmodel, badbad, badmarginal, marginalbad, marginalmarginal";
    		q += " from smdbadmin.service_instances where service_instance_id=?";
    		PreparedStatement stc = conn.prepareStatement(q);
    		stc.setInt(1, sid);
            ResultSet rs = stc.executeQuery();
            rs.next();
            String sname = rs.getString("service_instance_name");
            int cstat = rs.getInt("current_status");
            String citype = rs.getString("citype");
            String ciid = rs.getString("source_ci_id");
            String compmodel = rs.getString("compmodel");
            int badbad = rs.getInt("badbad");
            int badmarginal = rs.getInt("badmarginal");
            int marginalbad = rs.getInt("marginalbad");
            int marginalmarginal = rs.getInt("marginalmarginal");
            rs.close();
            stc.close();
            
			ts.AddRoot(sid, sname, cstat, citype, ciid);
			ts.rootNode.compmodel = compmodel;
			ts.rootNode.badbad = badbad;
			ts.rootNode.badmarginal = badmarginal;
			ts.rootNode.marginalbad = marginalbad;
			ts.rootNode.marginalmarginal = marginalmarginal;
			
		} catch (SQLException e) {
			LOG.Error(e);
		}
    }
    
    private void InitServiceTreeForStat(int sid) {
    	LOG.resultBean.Reset();
    	
    	rootBean = new TreeBean();
    	
    	ts2 = new TreeStructure();
    	try {
    		String q = "SELECT service_instance_name, current_status, citype, source_ci_id,";
    		q += " compmodel, badbad, badmarginal, marginalbad, marginalmarginal";
    		q += " from smdbadmin.service_instances where service_instance_id=?";
    		PreparedStatement stc = conn.prepareStatement(q);
    		stc.setInt(1, sid);
            ResultSet rs = stc.executeQuery();
            rs.next();
            String sname = rs.getString("service_instance_name");
            int cstat = rs.getInt("current_status");
            String citype = rs.getString("citype");
            String ciid = rs.getString("source_ci_id");
            String compmodel = rs.getString("compmodel");
            int badbad = rs.getInt("badbad");
            int badmarginal = rs.getInt("badmarginal");
            int marginalbad = rs.getInt("marginalbad");
            int marginalmarginal = rs.getInt("marginalmarginal");
            rs.close();
            stc.close();
            
			ts2.AddRoot(sid, sname, cstat, citype, ciid);
			ts2.rootNode.compmodel = compmodel;
			ts2.rootNode.badbad = badbad;
			ts2.rootNode.badmarginal = badmarginal;
			ts2.rootNode.marginalbad = marginalbad;
			ts2.rootNode.marginalmarginal = marginalmarginal;

		} catch (SQLException e) {
			LOG.Error(e);
		}
    }
    
    public String GetNodeChildren(int sid) {
    	String q = "SELECT i.service_instance_id, i.service_instance_name, i.current_status, i.citype, i.source_ci_id, ";
    	q += " i.compmodel, i.badbad, i.badmarginal, i.marginalbad, i.marginalmarginal";
    	q += " from smdbadmin.service_instance_relations r ";
    	q += " right join smdbadmin.service_instances i on(i.service_instance_id=r.service_instance_id)";
    	q += " where r.parent_instance_id=" + sid;
    	
    	BeanList<TreeBean> childList = new BeanList<TreeBean>();
    	
		try {
			OpenConn();
			
			PreparedStatement stc2 = conn.prepareStatement(q);
	        ResultSet rs2 = stc2.executeQuery();
	        
        	q = "select count(*) cnt from smdbadmin.service_instance_relations where parent_instance_id=?";
        	PreparedStatement st3 = conn.prepareStatement(q);
	        while(rs2.next()) {
	        	st3.setInt(1, rs2.getInt("service_instance_id"));
	        	ResultSet rs3 = st3.executeQuery();
	        	rs3.next();
	        	int cnt = rs3.getInt("cnt");
	        	rs3.close();
	        	
	        	TreeBean node = new TreeBean();
	        	node.name.set(rs2.getString("service_instance_name"));
	        	node.sid.set(rs2.getString("service_instance_id"));
	        	node.status.set(rs2.getString("current_status"));
	        	node.citype.set(rs2.getString("citype"));
	        	node.source_ci_id.set(rs2.getString("source_ci_id"));
	        	node.compmodel.set(rs2.getString("compmodel"));
	        	node.badbad.set(rs2.getString("badbad"));
	        	node.badmarginal.set(rs2.getString("badmarginal"));
	        	node.marginalbad.set(rs2.getString("marginalbad"));
	        	node.marginalmarginal.set(rs2.getString("marginalmarginal"));
	        	if(cnt > 0)
	        		node.hasChild.set("true");
	        	else
	        		node.hasChild.set("false");

	        	childList.add(node);
	        }
	    	
	    	rs2.close();
	    	stc2.close();
    	
		} catch (SQLException | IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
		
		return childList.Serialize();
		
    }
    
    HashMap<Integer, Node> createdNodes;
    private void TSBuild(Node parentNode) {
    	LOG.resultBean.Reset();
    	
		try {
			TSBuildstc.setInt(1, parentNode.sid);
	        ResultSet rs = TSBuildstc.executeQuery();

	        //LOG.Debug("Building service tree current sid: " + parentNode.sid);
	        int cnt = 0;
	        String childSids = "";
	        parentNode.childrenWeights = new HashMap<Integer, Integer>();
	        
	        StringBuilder strBuilder = new StringBuilder();
	        
	        ArrayList<Integer> sids = new ArrayList<Integer>();
	        while(rs.next()) {
	        	
	        	parentNode.childrenWeights.put(rs.getInt("service_instance_id"), rs.getInt("relation_weight"));
	        	if(cnt > 0) {
	        		childSids += ",";
	        		strBuilder.append(",");
	        	}
	        	childSids += rs.getInt("service_instance_id");
	        	strBuilder.append("?");
	        	sids.add(rs.getInt("service_instance_id"));
	        	
	        	cnt++;
	        	
	        	/*int childSid = rs.getInt("service_instance_id");

	        	q = "SELECT service_instance_name, current_status, citype from smdbadmin.service_instances where service_instance_id=?";
				PreparedStatement stc2 = conn.prepareStatement(q);
				stc2.setInt(1, childSid);
		        ResultSet rs2 = stc2.executeQuery();
		        rs2.next();
		        
	        	Node cnode = ts.AddChild(parentNode, childSid,
	        			rs2.getString("service_instance_name"),
	        			rs2.getInt("current_status"),
	        			rs2.getString("citype"));
	        	
	        	rs2.close();
	        	stc2.close();
	        	
	        	if(cnode != null) {
	        		TSBuild(cnode);
	        	}*/
	        }
	        
	        parentNode.childCount = cnt;
	        
	        /*String q = "SELECT service_instance_id, service_instance_name, current_status, citype, source_ci_id,";
        	q += " compmodel, badbad, badmarginal, marginalbad, marginalmarginal";
        	q += " from smdbadmin.service_instances where service_instance_id in(" + childSids + ")";*/
	        
	        if(cnt > 0 && parentNode.level < 2) {
	        	String q = "SELECT service_instance_id, service_instance_name, current_status, citype, source_ci_id, ";
	        	q += " compmodel, badbad, badmarginal, marginalbad, marginalmarginal";
	        	q += " from smdbadmin.service_instances where service_instance_id in (" + strBuilder + ")";
				PreparedStatement stc2 = conn.prepareStatement(q);

				int ind = 1;
				for(int sid : sids) {
					stc2.setInt(  ind++, sid );
					/*if(!createdNodes.containsKey(sid)) {
						stc2.setInt(  ind++, sid );
					} else {
						TSBuild(createdNodes.get(sid));
					}*/
				}
				
		        ResultSet rs2 = stc2.executeQuery();

		        while(rs2.next()) {
		        	//if(!createdNodes.containsKey(rs2.getInt("service_instance_id"))) {
			        	Node cnode = ts.AddChild(parentNode, rs2.getInt("service_instance_id"),
			        			rs2.getString("service_instance_name"),
			        			rs2.getInt("current_status"),
			        			rs2.getString("citype"),
			        			rs2.getString("source_ci_id"));

			        	if(cnode != null) {
			        		cnode.compmodel = rs2.getString("compmodel");
				        	cnode.badbad = rs2.getInt("badbad");
				        	cnode.badmarginal = rs2.getInt("badmarginal");
				        	cnode.marginalbad = rs2.getInt("marginalbad");
				        	cnode.marginalmarginal = rs2.getInt("marginalmarginal");
				        	
				        	createdNodes.put(rs2.getInt("service_instance_id"), cnode);
				        	
				        	//System.out.println("cnode: " + cnode);
			        		TSBuild(cnode);
			        	}
		        	//}
		        }
	        	
	        	rs2.close();
	        	stc2.close();
		        
	        }

	        rs.close();
		} catch (SQLException e) {
			LOG.Error(e);
		}
    }
    
    private void TSBuild2(Node parentNode) {
    	LOG.resultBean.Reset();
    	
		try {
			String q = "SELECT service_instance_id, relation_weight from smdbadmin.service_instance_relations where parent_instance_id=?";
			PreparedStatement stc = conn.prepareStatement(q);
			stc.setInt(1, parentNode.sid);
	        ResultSet rs = stc.executeQuery();
	        
	        int cnt = 0;
	        String childSids = "";
	        parentNode.childrenWeights = new HashMap<Integer, Integer>();
	        while(rs.next()) {
	        	
	        	parentNode.childrenWeights.put(rs.getInt("service_instance_id"), rs.getInt("relation_weight"));
	        	if(cnt > 0) {
	        		childSids += ",";
	        	}
	        	childSids += rs.getInt("service_instance_id");
	        	
	        	cnt++;
	        }
	        
	        if(cnt > 0) {
	        	q = "SELECT service_instance_id, service_instance_name, current_status, citype, source_ci_id,";
	        	q += " compmodel, badbad, badmarginal, marginalbad, marginalmarginal";
	        	q += " from smdbadmin.service_instances where service_instance_id in(" + childSids + ")";
				PreparedStatement stc2 = conn.prepareStatement(q);
		        ResultSet rs2x = stc2.executeQuery();

		        while(rs2x.next()) {
		        	Node cnode = ts2.AddChild(parentNode, rs2x.getInt("service_instance_id"),
		        			rs2x.getString("service_instance_name"),
		        			rs2x.getInt("current_status"),
		        			rs2x.getString("citype"),
		        			rs2x.getString("source_ci_id"));
		        	
		        	if(cnode != null) {
		        		cnode.compmodel = rs2x.getString("compmodel");
			        	cnode.badbad = rs2x.getInt("badbad");
			        	cnode.badmarginal = rs2x.getInt("badmarginal");
			        	cnode.marginalbad = rs2x.getInt("marginalbad");
			        	cnode.marginalmarginal = rs2x.getInt("marginalmarginal");
			        	
		        		TSBuild2(cnode);
		        	}
		        }
	        	
		        rs2x.close();
	        	stc2.close();
	        }

	        rs.close();
	        stc.close();
		} catch (SQLException e) {
			LOG.Error(e);
		}
    }
    
    HashMap<Integer, Boolean> inconsistentList;
    private void ConsistencyCheck(Node parentNode) {
    	if(!parentNode.canHaveChild) {
    		if(!inconsistentList.containsKey(parentNode.sid)) {
    			inconsistentList.put(parentNode.sid, true);
    		}
    	}
    	
    	if(parentNode.children.size() > 0)
    	{
    		for(Node cn : parentNode.childNodes)
    		{
    			ConsistencyCheck(cn);
    		}
    	}
    }

    public TreeBean rootBean = new TreeBean();
    // build tree through TreeBeans to serialize
    private void TBBuild(Node parentNode, TreeBean treeBean) {
    	if(!usedSid.contains(parentNode.sid)) {
    		usedSid.add(parentNode.sid);

	    	if(ServiceNameList.equals("")) {
	    		ServiceNameList += "'" + parentNode.name + "'";
	    		ServiceCINUMList += "'" + parentNode.source_ci_id + "'";
	    		ServiceIDList += parentNode.sid;
	    	} else {
	    		ServiceNameList += ",'" + parentNode.name + "'";
	    		ServiceCINUMList += ",'" + parentNode.source_ci_id + "'";
	    		ServiceIDList += "," + parentNode.sid;
	    	}
    	}
    	
    	treeBean.name.set(Helper.EscapeJsonStr(parentNode.name));
    	treeBean.sid.set(Integer.toString(parentNode.sid));
    	treeBean.status.set(Integer.toString(parentNode.status));
    	treeBean.citype.set(Helper.EscapeJsonStr(parentNode.citype));
    	treeBean.badbad.set(Double.toString(parentNode.badbad));
    	treeBean.badmarginal.set(Double.toString(parentNode.badmarginal));
    	treeBean.marginalbad.set(Double.toString(parentNode.marginalbad));
    	treeBean.marginalmarginal.set(Double.toString(parentNode.marginalmarginal));
    	treeBean.compmodel.set(parentNode.compmodel);
    	treeBean.source_ci_id.set(parentNode.source_ci_id);
    	if(parentNode.childCount > 0)
    		treeBean.hasChild.set("true");
    	else
    		treeBean.hasChild.set("false");
    	String consistent = "1";
    	if(inconsistentList.containsKey(parentNode.sid))
    		consistent = "0";
    	treeBean.consistent.set(consistent);

    	if(parentNode.children.size() > 0)
    	{
    		for(Node cn : parentNode.childNodes)
    		{
    			TreeBean cBean = new TreeBean();
    			treeBean.children.add(cBean);
    			TBBuild(cn, cBean);
    		}
    	}
    }
    
    public String ServiceNameList = "";
    public String ServiceIDList = "";
    public String ServiceCINUMList = "";
    public ArrayList<Integer> usedSid;
    
    PreparedStatement TSBuildstc;
    
    public String[] ServiceTreeThroughBeans(int sid) {
    	rootBean = new TreeBean();
    	inconsistentList = new HashMap<Integer, Boolean>();

    	ServiceNameList = "";
    	ServiceIDList = "";
    	ServiceCINUMList = "";
    	usedSid = new ArrayList<Integer>();
    	
    	createdNodes = new HashMap<Integer, Node>();
    	
    	try {
			OpenConn();
			InitServiceTree(sid);
			
			// get children of parent
	        String q = "SELECT service_instance_id, relation_weight from smdbadmin.service_instance_relations where parent_instance_id=?";
	        TSBuildstc = conn.prepareStatement(q);
	        
	        TSBuild(ts.rootNode);
	        
	        TSBuildstc.close();

	        ConsistencyCheck(ts.rootNode);
	        
	        TBBuild(ts.rootNode, rootBean);
	        
			LOG.Success(RM.GetErrorString("SUCCESS"));
		} catch (IOException | SQLException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
        
    	if(LOG.IsError())
    		return new String[] { LOG.resultBean.Serialize(), "" };
    	
        return new String[] { rootBean.Serialize(), "[" + ServiceIDList + "]" };
    }
    
    public class SrvIDCIID{
    	public int hasalarm;
    	public int sid;
    	public String source_ci_id;
    }
    
    public void UniqueCIListForAlarmsAndStat(int sid) {

    	try {
    		OpenConn();
    		
    		PreparedStatement srvst = conn.prepareStatement(ciiq);
			srvst.setInt(1, sid);
			ResultSet rs = srvst.executeQuery();

			// hold data of resultset and close immediately before recursion
			ArrayList<SrvIDCIID> childList = new ArrayList<SrvIDCIID>();
			while(rs.next()) {
				
				SrvIDCIID e = new SrvIDCIID();
				e.hasalarm = rs.getInt("hasalarm");
				e.sid = rs.getInt("service_instance_id");
				e.source_ci_id = rs.getString("source_ci_id");
				childList.add(e);
			}
			/*while(rs.next()) {
				if(rs.getInt("hasalarm") == 1) {
					if(sss != "") {
						sss += ",'" + rs.getString("source_ci_id") + "'";
						sssid += ", " + rs.getString("service_instance_id");
					}
					else {
						sss += "'" + rs.getString("source_ci_id") + "'";
						sssid += rs.getString("service_instance_id");
					}
				}
				
				if(!usedCIs.contains(rs.getInt("service_instance_id"))) {
					usedCIs.add(rs.getInt("service_instance_id"));
					UniqueCIListForAlarmsAndStat(rs.getInt("service_instance_id"));
				}
			}*/
			rs.close();
			srvst.close();
			
			CloseConn();
			
			for(SrvIDCIID e : childList) {
				if(e.hasalarm == 1) {
					if(sss != "") {
						sss += ",'" + e.source_ci_id + "'";
						sssid += ", " + e.sid;
					}
					else {
						sss += "'" + e.source_ci_id + "'";
						sssid += e.sid;
					}
				}
				
				if(!usedCIs.contains(e.sid)) {
					usedCIs.add(e.sid);
					UniqueCIListForAlarmsAndStat(e.sid);
				}
			}
			
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		}

    }
    
    String sss;
    String sssid;
    ArrayList<Integer> usedCIs;
    String ciiq = "select i.service_instance_id, i.service_instance_name, i.source_ci_id, i.hasalarm from smdbadmin.service_instances i " +
    		" left join smdbadmin.service_instance_relations r on(r.service_instance_id=i.service_instance_id) where r.parent_instance_id=?";
    public String[] GetUniqueCIListForAlarmsAndStat(int sid) {
    	
    	sss = "";
    	sssid = "[";
    	usedCIs = new ArrayList<Integer>();

		if(!usedCIs.contains(sid)) {
			usedCIs.add(sid);
			UniqueCIListForAlarmsAndStat(sid);
		}
		
		sssid += "]";
    	
    	return new String[] { sss, sssid };
    }

    private void DisablePropagation(int sid)
    {
    	LOG.resultBean.Reset();
    	
    	String q = "update smdbadmin.service_instances set propagate=2 where service_instance_id=?";
		try {
			PreparedStatement st = conn.prepareStatement(q);
			st.setInt(1, sid);
			st.executeUpdate();
			st.close();
		} catch (SQLException e) {
			LOG.Error(e);
		}
    }
    
    private String GetServiceName(int serviceID) throws SQLException
    {
        PreparedStatement st = conn.prepareStatement("SELECT service_instance_name from smdbadmin.service_instances where service_instance_id=?");
        st.setInt(1, serviceID);
        ResultSet rs1 = st.executeQuery();
        rs1.next();
        String name = rs1.getString(1);
        rs1.close();
        st.close();
        
        return name;
    }
    
    private String GetServiceType(int serviceID) throws SQLException
    {
        PreparedStatement st = conn.prepareStatement("SELECT citype from smdbadmin.service_instances where service_instance_id=?");
        st.setInt(1, serviceID);
        ResultSet rs1 = st.executeQuery();
        rs1.next();
        String name = rs1.getString(1);
        rs1.close();
        st.close();
        
        return name;
    }
    
    private int GetServiceStatus(int serviceID) throws SQLException
    {
        PreparedStatement st = conn.prepareStatement("SELECT current_status from smdbadmin.service_instances where service_instance_id=?");
        st.setInt(1, serviceID);
        ResultSet rs1 = st.executeQuery();
        rs1.next();
        int status = rs1.getInt(1);
        rs1.close();
        st.close();
        
        return status;
    }
    
    public String GetServiceCount(int[] statusFilter, HashMap<Integer, String> servicePermissions, String spattern, boolean isAdmin) {
    	LOG.resultBean.Reset();
    	
    	String allowedSrvList = "";
		// sid 0 means all services are allowed
		if((!isAdmin || !servicePermissions.containsKey(0))) {
			int scnt = 0;
			boolean sidzerocheck = false;
			for(int sid : servicePermissions.keySet()) {
				if(sid == 0) {
					sidzerocheck = true;
				}
				if(scnt > 0)
					allowedSrvList += ",";
				
				allowedSrvList += Integer.toString(sid);
				
				scnt++;
			}
			if(sidzerocheck == true) {
				allowedSrvList = "0";
			}
		}
		
    	String count = "0";
    	
    	if(isAdmin || servicePermissions.size() > 0) {
	        try {
	        	OpenConn();
	        	String q = "SELECT count(*) from smdbadmin.service_instances where citype='CI.BUSINESSMAINSERVICE' and current_status in ("+Helper.joinInts(statusFilter,",")+")";
	        	if((!isAdmin || !servicePermissions.containsKey(0)) && (!allowedSrvList.trim().equals("") && !allowedSrvList.trim().equals("0"))) {
	        		q += " and service_instance_id in(" + allowedSrvList + ")";
	        	}
	        	if(!spattern.equals(""))
	        		q += " and service_instance_name ilike '%" + spattern + "%'";
	        	PreparedStatement stc = conn.prepareStatement(q);
				ResultSet RSc = stc.executeQuery();
				RSc.next();
				
				count = RSc.getString(1);
				
				RSc.close();
				stc.close();
				
			} catch (SQLException e) {
				LOG.Error(e);
			} catch (IOException e) {
				LOG.Error(e);
			} finally {
				CloseConn();
			}
	        if(LOG.IsError())
	        	return LOG.resultBean.Serialize();
    	}
    	
        return count;
    }
    
    // TSCAN
    public String TScanAllServices() {
    	LOG.resultBean.Reset();
    	
    	try {
    		OpenConn();
	    	String q = "SELECT service_instance_id from smdbadmin.service_instances where citype='CI.BUSINESSMAINSERVICE'";
			PreparedStatement st = conn.prepareStatement(q);
			
			ResultSet rs = st.executeQuery();
			
			while(rs.next()) {
				TInitServiceTree(rs.getInt(1));
				TTSBuild(ts.rootNode);
			}
	        
			rs.close();
	        st.close();
	        
	        LOG.Success(RM.GetErrorString("SUCCESS"));
	        
		} catch (SQLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		} finally {
			CloseConn();
		}
    	
    	return LOG.resultBean.Serialize();
    }
    
    private void TInitServiceTree(int sid) {
    	LOG.resultBean.Reset();
        
    	ts = new TreeStructure();
    	
    	try {
    		String q = "SELECT service_instance_name, current_status, citype, source_ci_id,";
    		q += " compmodel, badbad, badmarginal, marginalbad, marginalmarginal";
    		q += " from smdbadmin.service_instances where service_instance_id=?";
    		PreparedStatement stc = conn.prepareStatement(q);
    		stc.setInt(1, sid);
            ResultSet rs = stc.executeQuery();
            rs.next();
            String sname = rs.getString("service_instance_name");
            int cstat = rs.getInt("current_status");
            String citype = rs.getString("citype");
            String ciid = rs.getString("source_ci_id");
            String compmodel = rs.getString("compmodel");
            int badbad = rs.getInt("badbad");
            int badmarginal = rs.getInt("badmarginal");
            int marginalbad = rs.getInt("marginalbad");
            int marginalmarginal = rs.getInt("marginalmarginal");
            rs.close();
            stc.close();
            
			ts.AddRoot(sid, sname, cstat, citype, ciid);
			ts.rootNode.compmodel = compmodel;
			ts.rootNode.badbad = badbad;
			ts.rootNode.badmarginal = badmarginal;
			ts.rootNode.marginalbad = marginalbad;
			ts.rootNode.marginalmarginal = marginalmarginal;
			
		} catch (SQLException e) {
			LOG.Error(e);
		}
    }
    
    private void TTSBuild(Node parentNode) {
    	LOG.resultBean.Reset();
    	
		try {
	        if(!parentNode.canHaveChild)
	        	DisablePropagation(parentNode.sid);

			String q = "SELECT service_instance_id, relation_weight from smdbadmin.service_instance_relations where parent_instance_id=?";
			PreparedStatement stc = conn.prepareStatement(q);
			stc.setInt(1, parentNode.sid);
	        ResultSet rs = stc.executeQuery();
	        
	        int cnt = 0;
	        String childSids = "";
	        parentNode.childrenWeights = new HashMap<Integer, Integer>();
	        while(rs.next()) {
	        	
	        	parentNode.childrenWeights.put(rs.getInt("service_instance_id"), rs.getInt("relation_weight"));
	        	if(cnt > 0) {
	        		childSids += ",";
	        	}
	        	childSids += rs.getInt("service_instance_id");
	        	
	        	cnt++;
	        }
	        
	        if(cnt > 0) {
	        	q = "SELECT service_instance_id, service_instance_name, current_status, citype, source_ci_id,";
	        	q += " compmodel, badbad, badmarginal, marginalbad, marginalmarginal";
	        	q += " from smdbadmin.service_instances where service_instance_id in(" + childSids + ")";
				PreparedStatement stc2 = conn.prepareStatement(q);
		        ResultSet rs2 = stc2.executeQuery();

		        while(rs2.next()) {
		        	Node cnode = ts.AddChild(parentNode, rs2.getInt("service_instance_id"),
		        			rs2.getString("service_instance_name"),
		        			rs2.getInt("current_status"),
		        			rs2.getString("citype"),
		        			rs2.getString("source_ci_id"));
		        	
		        	if(cnode != null) {
		        		cnode.compmodel = rs2.getString("compmodel");
			        	cnode.badbad = rs2.getInt("badbad");
			        	cnode.badmarginal = rs2.getInt("badmarginal");
			        	cnode.marginalbad = rs2.getInt("marginalbad");
			        	cnode.marginalmarginal = rs2.getInt("marginalmarginal");
			        	
		        		TTSBuild(cnode);
		        	}
		        }
	        	
	        	rs2.close();
	        	stc2.close();
	        }

	        rs.close();
	        stc.close();
		} catch (SQLException e) {
			LOG.Error(e);
		}
    }
    
    public String GetServiceStat(int sid) throws IOException {
    	LOG.resultBean.Reset();
    	
        try{
            OpenConn();
            String q = "SELECT current_status from smdbadmin.service_instances where service_instance_id=?";
            PreparedStatement st = conn.prepareStatement(q);
            st.setInt(1, sid);
            ResultSet rs1 = st.executeQuery();
            rs1.next();
            String stat = rs1.getString(1);
            rs1.close();
            st.close();
            
            return stat;
        } catch (SQLException ex) {
        	LOG.Error(ex);
        	
            return LOG.resultBean.Serialize();
        } finally {
        	CloseConn();
        }
    }
}
