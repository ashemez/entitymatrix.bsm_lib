package com.gizit.managers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ResourceManager {

	private ResourceBundle appbundle;
	private ResourceBundle errbundle;
	private ResourceBundle serverbundle;
	public ResourceManager() {
		appbundle = ResourceBundle.getBundle("com.gizit.managers.app");
		errbundle = ResourceBundle.getBundle("com.gizit.managers.err");
		
		
		try {
			// get server properties with environment variable
			File file = new File(System.getenv("BSVIEW_CONFPATH"));
			//File file = new File(System.getProperty("catalina.base") + "/webapps/gbsm/conf");
			URL[] urls= {
				file.toURI().toURL()
			};
			
			ClassLoader loader = new URLClassLoader(urls);
			serverbundle = ResourceBundle.getBundle("server", Locale.getDefault(), loader);
		}
		catch (MissingResourceException e){
			System.out.println("Please set the correct path to BSVIEW_CONFPATH env variable where server.properties file is located!");
			e.printStackTrace();
			/*try {
				File file1 = new File("/opt/IBM/quartz/");
				URL[] urls= {
						file1.toURI().toURL()
				};
				ClassLoader loader = new URLClassLoader(urls);
				serverbundle = ResourceBundle.getBundle("server", Locale.getDefault(), loader);
			}
			catch (MalformedURLException e1){
				e1.printStackTrace();
			}*/
		}
		catch (MalformedURLException e){
			e.printStackTrace();
		}
		
	}
	
	public String GetLabelString(String key) {
		return appbundle.getString(key);
	}
	
	public String GetErrorString(String key) {
		return errbundle.getString(key);
	}
	
	public String GetServerProperty(String key) {
		return serverbundle.getString(key);
	}
}
