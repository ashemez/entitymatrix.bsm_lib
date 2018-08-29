package com.gizit.bsm.beans;

public class Service {
	
	public Service() {}
	
	public Service(int service_instance_id, String service_instance_name, String service_instance_displayname,
			int service_template_id, int current_status, String ci_type, int propagate, double metric) {
		super();
		this.service_instance_id = service_instance_id;
		this.service_instance_name = service_instance_name;
		this.service_instance_displayname = service_instance_displayname;
		this.service_template_id = service_template_id;
		this.current_status = current_status;
		this.ci_type = ci_type;
		this.propagate = propagate;
		this.availability_metric = metric;
	}
	
	private int service_instance_id;
	private String service_instance_name;
	private String service_instance_displayname;
	private int service_template_id;
	private int current_status;
	private String ci_type;
	private int propagate;
	private double availability_metric;
	
	public int getService_instance_id() {
		return service_instance_id;
	}
	public void setService_instance_id(int service_instance_id) {
		this.service_instance_id = service_instance_id;
	}
	public String getService_instance_name() {
		return service_instance_name;
	}
	public void setService_instance_name(String service_instance_name) {
		this.service_instance_name = service_instance_name;
	}
	public String getService_instance_displayname() {
		return service_instance_displayname;
	}
	public void setService_instance_displayname(String service_instance_displayname) {
		this.service_instance_displayname = service_instance_displayname;
	}
	public int getService_template_id() {
		return service_template_id;
	}
	public void setService_template_id(int service_template_id) {
		this.service_template_id = service_template_id;
	}
	public int getCurrent_status() {
		return current_status;
	}
	public void setCurrent_status(int current_status) {
		this.current_status = current_status;
	}
	public String getCi_type() {
		return ci_type;
	}
	public void setCi_type(String ci_type) {
		this.ci_type = ci_type;
	}
	public int getPropagate() {
		return propagate;
	}
	public void setPropagate(int propagate) {
		this.propagate = propagate;
	}
	public double getAvailabilityMetric() {
		return availability_metric;
	}
	public void setAvailabilityMetric(double metric) {
		this.availability_metric = metric;
	}

}
