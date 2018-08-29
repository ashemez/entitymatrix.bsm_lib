package com.gizit.managers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.gizit.bsm.generic.StringField;
import com.sun.jndi.ldap.LdapCtxFactory;

public class LDAPManager {

	public String ldapHostname;
	public String ldapUsername;
	public String ldapPassword;
	public String ldapContext;
	Hashtable<String, String> ldapUserProperties;
	ResourceManager RM;
	
	public DirContext directoryContext;

	public LDAPManager() {

		RM = new ResourceManager();
		
		ldapHostname = RM.GetServerProperty("ldap-hostname");
		ldapUsername = RM.GetServerProperty("ldap-username");
		ldapPassword = RM.GetServerProperty("ldap-password");

		ldapContext = String.format("ldap://%s", ldapHostname);
		
	}
	
	public StringField firstName;
	public StringField lastName;
	public boolean LDAPAuthentication(String username, String password) {
		boolean authenticationGranted = false;
		
		firstName = new StringField();
		lastName = new StringField();

		ldapUserProperties = new Hashtable<String, String>();
		ldapUserProperties.put(Context.SECURITY_PRINCIPAL, ldapUsername);
		ldapUserProperties.put(Context.SECURITY_CREDENTIALS, password);
		
		if(!username.isEmpty() && !password.isEmpty()) {
			try {
				directoryContext = LdapCtxFactory.getLdapCtxInstance(ldapContext, ldapUserProperties);
				
				System.out.println("principle: " + username);
				authenticationGranted = true;
				String[] fullName = GetLDAPUserFullName(directoryContext, username);
				
				firstName.set(fullName[0]);
				lastName.set(fullName[1]);
				
				/*Attributes attributes = directoryContext.getAttributes("cn=" + username + "," + RM.GetServerProperty("ldap-usersContainer"));
			    Attribute givenName = attributes.get("givenName");
			    Attribute sn = attributes.get("sn");
			    firstName.set(givenName.get().toString());
			    lastName.set(sn.get().toString());*/
			} catch (NamingException e) {
				
				// else try user with domain name
				ldapUserProperties = new Hashtable<String, String>();
				
				String principle = ldapUsername + "@" + RM.GetServerProperty("ldap-usersContainer").split(",")[0].split("=")[1] + "." + RM.GetServerProperty("ldap-usersContainer").split(",")[1].split("=")[1];
				ldapUserProperties.put(Context.SECURITY_PRINCIPAL, principle);
				ldapUserProperties.put(Context.SECURITY_CREDENTIALS, password);
				
				try {
					directoryContext = LdapCtxFactory.getLdapCtxInstance(ldapContext, ldapUserProperties);
					
					System.out.println("principle: " + principle);
					authenticationGranted = true;
					String[] fullName = GetLDAPUserFullName(directoryContext, username);
					
					firstName.set(fullName[0]);
					lastName.set(fullName[1]);
					
					/*Attributes attributes = directoryContext.getAttributes("cn=" + username + "," + RM.GetServerProperty("ldap-usersContainer"));
				    Attribute givenName = attributes.get("givenName");
				    Attribute sn = attributes.get("sn");
				    firstName.set(givenName.get().toString());
				    lastName.set(sn.get().toString());*/
				} catch (NamingException e1) {
					//e1.printStackTrace();
					
					// else try user with full name
					ldapUserProperties = new Hashtable<String, String>();
					
					try {
						String[] fullname = GetLDAPUserFullName(username);
						//principle = fullname[0] + " " + fullname[1];
						principle = fullname[2];
						ldapUserProperties.put(Context.SECURITY_PRINCIPAL, principle);
						ldapUserProperties.put(Context.SECURITY_CREDENTIALS, password);
						
						directoryContext = LdapCtxFactory.getLdapCtxInstance(ldapContext, ldapUserProperties);
						
						System.out.println("principle: " + principle);
						authenticationGranted = true;
						//String[] fullName = GetLDAPUserFullName(directoryContext, username);
						
						firstName.set(fullname[0]);
						lastName.set(fullname[1]);
						
						/*Attributes attributes = directoryContext.getAttributes("cn=" + username + "," + RM.GetServerProperty("ldap-usersContainer"));
					    Attribute givenName = attributes.get("givenName");
					    Attribute sn = attributes.get("sn");
					    firstName.set(givenName.get().toString());
					    lastName.set(sn.get().toString());*/
					} catch (NamingException e11) {
						e11.printStackTrace();
					}
					
				}
			}
		}
		
		return authenticationGranted;
	}
	
	
	public ArrayList<String> GetLDAPUsers() throws NamingException {
		ldapUserProperties = new Hashtable<String, String>();
		ldapUserProperties.put(Context.SECURITY_PRINCIPAL, ldapUsername);
		ldapUserProperties.put(Context.SECURITY_CREDENTIALS, ldapPassword);
		
		DirContext dc = LdapCtxFactory.getLdapCtxInstance(ldapContext, ldapUserProperties);
		
		ArrayList<String> userList = new ArrayList<String>();
		
		SearchControls searchCtrls = new SearchControls();
		searchCtrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String filter = "(&" + RM.GetServerProperty("ldap-userSearchFilter") + ")";
		NamingEnumeration values = dc.search(RM.GetServerProperty("ldap-usersContainer"), filter, searchCtrls);
		
		while (values.hasMoreElements())
		{
			SearchResult result = (SearchResult) values.next();
			Attributes attribs = result.getAttributes();

			if (null != attribs)
			{
				for (NamingEnumeration ae = attribs.getAll(); ae.hasMoreElements();)
				{
					Attribute atr = (Attribute) ae.next();
					String attributeID = atr.getID();
					
					if(attributeID.equals("sAMAccountName")) {
						for (Enumeration vals = atr.getAll(); 
							vals.hasMoreElements();) {
							
							// username
							//System.out.println("LDAP_USER_SEARCH: " + attributeID +": "+ vals.nextElement());
							String uname = vals.nextElement().toString();
							System.out.println(uname);
							userList.add(uname);
						}
					}
				}
			}
		}
		
		return userList;
	}
	
	public ArrayList<String> GetLDAPUsers(String pattern) throws NamingException {
		System.out.println("searching: " + pattern);
		
		ldapUserProperties = new Hashtable<String, String>();
		ldapUserProperties.put(Context.SECURITY_PRINCIPAL, ldapUsername);
		ldapUserProperties.put(Context.SECURITY_CREDENTIALS, ldapPassword);
				
		DirContext dc = LdapCtxFactory.getLdapCtxInstance(ldapContext, ldapUserProperties);
		
		ArrayList<String> userList = new ArrayList<String>();
		
		SearchControls searchCtrls = new SearchControls();
		searchCtrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String filter = RM.GetServerProperty("ldap-userSearchFilter");
		filter = filter.replaceAll("SEARCH_PATTERN", pattern);
		System.out.println("updatedfilter: " +  filter);
		//filter = "(&" + filter + "(sAMAccountName=*" + pattern + "*))";
		NamingEnumeration values = dc.search(RM.GetServerProperty("ldap-usersContainer"), filter, searchCtrls);
		
		while (values.hasMoreElements())
		{
			SearchResult result = (SearchResult) values.next();
			Attributes attribs = result.getAttributes();

			if (null != attribs)
			{
				for (NamingEnumeration ae = attribs.getAll(); ae.hasMoreElements();)
				{
					Attribute atr = (Attribute) ae.next();
					String attributeID = atr.getID();

					if(attributeID.equals("sAMAccountName")) {
						for (Enumeration vals = atr.getAll(); 
							vals.hasMoreElements();) {
							
							// username
							//System.out.println("LDAP_USER_SEARCH: " + attributeID +": "+ vals.nextElement());
							String uname = vals.nextElement().toString();
							System.out.println(uname);
							userList.add(uname);
						}
					}
				}
			}
		}
		
		return userList;
	}
	
	public String[] GetLDAPUserFullName(DirContext dc, String pattern) throws NamingException {
		System.out.println("searching: " + pattern);
		
		String firstName = pattern;
		String lastName = pattern;
		
		SearchControls searchCtrls = new SearchControls();
		searchCtrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String filter = RM.GetServerProperty("ldap-userSearchFilter");
		filter = filter.replaceAll("SEARCH_PATTERN", pattern);
		filter = filter.replaceAll("\\*", "");
		NamingEnumeration values = dc.search(RM.GetServerProperty("ldap-usersContainer"), filter, searchCtrls);
		
		while (values.hasMoreElements())
		{
			SearchResult result = (SearchResult) values.next();
			Attributes attribs = result.getAttributes();

			if (null != attribs)
			{
				for (NamingEnumeration ae = attribs.getAll(); ae.hasMoreElements();)
				{
					Attribute atr = (Attribute) ae.next();
					String attributeID = atr.getID();
					
					// firstname
					if(attributeID.equals("givenName")) {
						for (Enumeration vals = atr.getAll(); 
							vals.hasMoreElements();) {
							
							firstName = vals.nextElement().toString();
							System.out.println("Found firstname: " + firstName);
						}
					}
					
					// lastname
					if(attributeID.equals("sn")) {
						for (Enumeration vals = atr.getAll(); 
							vals.hasMoreElements();) {
							
							lastName = vals.nextElement().toString();
							System.out.println("Found lastname: " + lastName);
						}
					}
				}
			}
		}
		
		if(lastName.equals(firstName)) {
			lastName = "";
		}
		
		return new String[] { firstName, lastName };
	}
	
	
	public String[] GetLDAPUserFullName(String pattern) throws NamingException {
		ldapUserProperties = new Hashtable<String, String>();
		ldapUserProperties.put(Context.SECURITY_PRINCIPAL, ldapUsername);
		ldapUserProperties.put(Context.SECURITY_CREDENTIALS, ldapPassword);
				
		DirContext dc = LdapCtxFactory.getLdapCtxInstance(ldapContext, ldapUserProperties);
		
		String firstName = pattern;
		String lastName = pattern;
		String DN = "";
		
		SearchControls searchCtrls = new SearchControls();
		searchCtrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String filter = RM.GetServerProperty("ldap-userSearchFilter");
		filter = filter.replaceAll("SEARCH_PATTERN", pattern);
		filter = filter.replaceAll("\\*", "");
		NamingEnumeration values = dc.search(RM.GetServerProperty("ldap-usersContainer"), filter, searchCtrls);
		
		while (values.hasMoreElements())
		{
			SearchResult result = (SearchResult) values.next();
			Attributes attribs = result.getAttributes();

			if (null != attribs)
			{
				for (NamingEnumeration ae = attribs.getAll(); ae.hasMoreElements();)
				{
					Attribute atr = (Attribute) ae.next();
					String attributeID = atr.getID();
					
					// firstname
					if(attributeID.equals("givenName")) {
						for (Enumeration vals = atr.getAll(); 
							vals.hasMoreElements();) {
							
							firstName = vals.nextElement().toString();
							System.out.println("Found firstname: " + firstName);
						}
					}
					
					// lastname
					if(attributeID.equals("sn")) {
						for (Enumeration vals = atr.getAll(); 
							vals.hasMoreElements();) {
							
							lastName = vals.nextElement().toString();
							System.out.println("Found lastname: " + lastName);
						}
					}
					
					// DN
					if(attributeID.equals("distinguishedName")) {
						for (Enumeration vals = atr.getAll(); 
							vals.hasMoreElements();) {
							
							DN = vals.nextElement().toString();
							System.out.println("DN: " + DN);
						}
					}
				}
			}
		}
		
		if(lastName.equals(firstName)) {
			lastName = "";
		}
		
		return new String[] { firstName, lastName, DN };
	}
	
}
