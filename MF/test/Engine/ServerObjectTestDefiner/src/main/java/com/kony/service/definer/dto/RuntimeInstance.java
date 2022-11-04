package com.kony.service.definer.dto;

import java.util.List;

/**
 * DTO of RuntimeInstance
 *
 * @author Aditya Mankal
 */
public class RuntimeInstance {

	private String name;
	private String hostURL;
	private String authServiceURL;
	private List<Application> applications;

	/**
	 * @param name
	 * @param hostURL
	 * @param authServiceURL
	 * @param applications
	 */
	public RuntimeInstance(String name, String hostURL, String authServiceURL, List<Application> applications) {
		super();
		this.name = name;
		this.hostURL = hostURL;
		this.authServiceURL = authServiceURL;
		this.applications = applications;
	}

	/**
	 * 
	 */
	public RuntimeInstance() {
		super();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the hostURL
	 */
	public String getHostURL() {
		return hostURL;
	}

	/**
	 * @param hostURL the hostURL to set
	 */
	public void setHostURL(String hostURL) {
		this.hostURL = hostURL;
	}

	/**
	 * @return the authServiceURL
	 */
	public String getAuthServiceURL() {
		return authServiceURL;
	}

	/**
	 * @param authServiceURL the authServiceURL to set
	 */
	public void setAuthServiceURL(String authServiceURL) {
		this.authServiceURL = authServiceURL;
	}

	/**
	 * @return the applications
	 */
	public List<Application> getApplications() {
		return applications;
	}

	/**
	 * @param applications the applications to set
	 */
	public void setApplications(List<Application> applications) {
		this.applications = applications;
	}

}
