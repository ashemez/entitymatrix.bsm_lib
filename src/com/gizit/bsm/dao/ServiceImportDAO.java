package com.gizit.bsm.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;

import com.gizit.bsm.generic.StringField;
import com.gizit.bsm.helpers.Helper;

public class ServiceImportDAO extends DAO {
	
	public ServiceImportDAO() {
		super(ServiceImportDAO.class);
	}

	/*
	private int GetServiceID(String _ciname){
 	   int _sid = 0;
 	   _ciname = _ciname.replaceAll("'", "''");
 	   String q = "select service_instance_id from smdbadmin.service_instances where service_instance_name='" + _ciname + "'";
 	   try {
 		   Statement st = conn.createStatement();
 		   ResultSet ccr = st.executeQuery(q);

			if(ccr.next())
	    	      _sid = ccr.getInt(1);
			
			ccr.close();
			st.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
 	   return _sid; 
	}
	
	private String GetXMLVal(String xml, String key) {
		return "";
	}
	
	String VFCITBSMMAPPING_DATA;
	String baseMxmRestService;
	HashMap<Integer, Integer> usedSidList = new HashMap<Integer, Integer>();
	public String ImportServiceThroughMxmRest(String mainServiceName, String url) {
		usedSidList = new HashMap<Integer, Integer>();
		
		baseMxmRestService = "https://maximo.vodafone.com.tr/maxrest/rest/os/";
		
		// read VFCITBSMMAPPING table
		VFCITBSMMAPPING_DATA = GetRestXmlOut(baseMxmRestService + "VFCITBSMMAPPING?parenttype=CI.BUSINESSMAINSERVICE");

		// read CI data of mainServiceName
		String SRV_CI_DATA = GetRestXmlOut("CI?ciname=" + mainServiceName);
		
		int parentSid = GetServiceID(mainServiceName);
		ImportSrv(parentSid, GetXMLVal(SRV_CI_DATA, "CINUM"), "105175");
		
		return "";
	}
	
    private void ImportSrv(int parentSID, String cinum, String citype)
    {
    	try {
    		usedSidList.put(parentSID, 1);
		    // get sourcetype, parenttype and relationtype from relation type mapping table
		    // and insert into service_instances and service_instance_relations
		    String psq = "select sourcetype, targettype, relationtype from VFCITBSMMAPPING where parenttype=" + citype;
	        Statement st = mxmConn.createStatement();
	        ResultSet psr = st.executeQuery(psq);

	         while(psr.next()){
	        	 
	            // select children from cirelation according to ci-type mapping
	            boolean queryThis = true; 
	            
	            String CIRELATION_URL = "CIRELATION?RELATIONNUM=" + GetXMLVal(VFCITBSMMAPPING_DATA, "relationtype");
	            String SOURCE_CLASSSTRUCTURE_URL = "CLASSSTRUCTURE?CLASSSTRUCTUREID=";
	            String TARGET_CLASSSTRUCTURE_URL = "CLASSSTRUCTURE?CLASSSTRUCTUREID=";
	            String SOURCE_CI_URL = "CI?";
	            String TARGET_CI_URL = "CI?";

	            if(citype.equals(GetXMLVal(VFCITBSMMAPPING_DATA, "sourcetype"))) {
	            	
	            	CIRELATION_URL += "&SOURCECI=" + cinum;
	            	SOURCE_CLASSSTRUCTURE_URL += citype;
	            	TARGET_CLASSSTRUCTURE_URL += GetXMLVal(VFCITBSMMAPPING_DATA, "targettype");
	            	
	            } else if(citype.equals(GetXMLVal(VFCITBSMMAPPING_DATA, "targettype"))){
	            	
	            	CIRELATION_URL += "&TARGETCI=" + cinum;
	            	SOURCE_CLASSSTRUCTURE_URL += GetXMLVal(VFCITBSMMAPPING_DATA, "sourcetype");
	            	TARGET_CLASSSTRUCTURE_URL += citype;
	            	
	            } else {
	               queryThis = false;
	            }
	            
	            String CIRELATION_DATA = GetRestXmlOut(baseMxmRestService + CIRELATION_URL);
	            
	            String SOURCE_CLASSSTRUCTURE_DATA = GetRestXmlOut(baseMxmRestService + SOURCE_CLASSSTRUCTURE_URL);
	            
	            String TARGET_CLASSSTRUCTURE_DATA = GetRestXmlOut(baseMxmRestService + TARGET_CLASSSTRUCTURE_URL);
	            
	            SOURCE_CI_URL += "CLASSSTRUCTUREID=" + GetXMLVal(SOURCE_CLASSSTRUCTURE_DATA, "CLASSSTRUCTUREID");
	            SOURCE_CI_URL += "&CINUM=" + GetXMLVal(CIRELATION_DATA, "SOURCECI");
	            String SOURCE_CI_DATA = GetRestXmlOut(baseMxmRestService + SOURCE_CI_URL);
	            
	            TARGET_CI_URL += "CLASSSTRUCTUREID=" + GetXMLVal(TARGET_CLASSSTRUCTURE_DATA, "CLASSSTRUCTUREID");
	            TARGET_CI_URL += "&CINUM=" + GetXMLVal(CIRELATION_DATA, "TARGETCI");
	            String TARGET_CI_DATA = GetRestXmlOut(baseMxmRestService + TARGET_CI_URL);
	            
	            //if(cinum.equals("OI-0677C5A4A995448A802C404003A791A0"))
	            //	System.out.println(rq);
	            
	            if(queryThis){
			        Statement st1 = mxmConn.createStatement();
			        ResultSet cir = st1.executeQuery(rq);

	                  // insert into service_instances if does not exist
	                  // insert into service_instance_relations if does not exist
	                  while(cir.next()){
	                     String parentName = cir.getString(6);
	                     String childName = cir.getString(7);
	                     String childCITYPE = cir.getString(8);
	                     String childClassStruct = cir.getString(5);
	                     String childCINUM = cir.getString(3);
	                     
	                     if(citype.equals(psr.getString(2))){
	                        parentName = cir.getString(7);
	                        childName = cir.getString(6);
	                        childCITYPE = cir.getString(9); 
	                        childClassStruct = cir.getString(4);
	                        childCINUM = cir.getString(2); 
	                     }

	                     
	                     if(parentName.equals("MAXIMO APP")) {
	                    	 System.out.println(parentName + " ** " + childName + ", childCITYPE " + childCITYPE);
	                     }

	                     if(citype.equals("105017")) {
	                    	 //System.out.println("105017 ***** parentName: " + parentName + ", childName:" + childName);
	                     }

	                     int childSID = GetServiceID(childName);

	                     if(childSID > 0){
	                        String srq = "select count(*) cnt from smdbadmin.service_instance_relations where parent_instance_id=" + parentSID + " and service_instance_id=" + childSID;
	                        Statement st2 = conn.createStatement();
					        ResultSet srrr = st2.executeQuery(srq);
					        srrr.next();
	                        if(srrr.getInt(1) == 0){
	                           String sriq = "insert into smdbadmin.service_instance_relations(service_instance_id, parent_instance_id, node_group_id)";
	                           sriq = sriq + " values(" + childSID + "," + parentSID + ",0)";
	                           Statement st3 = conn.createStatement();
	                           st3.executeUpdate(sriq);
	                           
	                           st3.close();
	                        }
	                        
	                        //System.out.println("**** CHILD INSERTED, PARENT RELATION: " + childSID + " ** " + parentSID + " **** childName:" + childName);
	                        //System.out.println(rq);
	                        srrr.close();
	                        st2.close();
	                     } else {
	                    	String tmpChild = childName.replaceAll("'", "''");
	                        String ciq = "insert into smdbadmin.service_instances(service_instance_name, service_instance_displayname, current_status, citype, propagate, status_timestamp)";
	                        ciq = ciq + " values('" + tmpChild + "', '" + tmpChild + "', 0, '" + childCITYPE + "', 0, 0)";
	                        Statement st4 = conn.createStatement();
	                        st4.executeUpdate(ciq);
	                        childSID = GetServiceID(childName);
	                        st4.close();
	 
	                        String sriq = "insert into smdbadmin.service_instance_relations(service_instance_id, parent_instance_id, node_group_id)";
	                        sriq = sriq + " values(" + childSID + "," + parentSID + ",0)";
	                        Statement st5 = conn.createStatement();
	                        st5.executeUpdate(sriq);
	                        st5.close();
	                        
	                        //System.out.println("**** CHILD INSERTED, PARENT RELATION: " + childSID + " ** " + parentSID + " **** childName:" + tmpChild + ", parentName: " + parentName + " *** " + rq);
	                     }

	                     if(!usedSidList.containsKey(childSID))
	                    	 ImportSrv(childSID, childCINUM, childClassStruct);

	                  }
	                  
	                  cir.close();
	                  st1.close();
	            }

	         }
	         
	         psr.close();
	         st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	public String GetRestXmlOut(String resturl) {
		
		String outputStr = "";
		try {

			//URL url = new URL("http://bsp.mits.ch/buch/job/attributes/");
			URL url = new URL(resturl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/xml");
			//conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			int rowcnt = 0;
			while ((output = br.readLine()) != null) {
				final byte[] bytes = output.getBytes();
				
				if (Helper.isUTF8(bytes))
					outputStr += Helper.printSkippedBomString(output.getBytes()) + "\n";
				else
					outputStr += output + "\n";
				
				rowcnt++;
			}
			
			//System.out.println(outputStr);

			conn.disconnect();

		} catch (MalformedURLException e) {
			LOG.Error(e);
		} catch (IOException e) {
			LOG.Error(e);
		}

		return outputStr;
	}
	*/

}
