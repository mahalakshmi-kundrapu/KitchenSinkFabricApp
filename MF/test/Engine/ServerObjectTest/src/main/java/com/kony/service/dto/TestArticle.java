package com.kony.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.kony.service.test.input.VariableValues;

/**
 * DTO consolidating all input values
 *
 * @author Aditya Mankal
 */
public class TestArticle implements Serializable {

	private static final long serialVersionUID = -3454654523124637169L;

	private String hostURL;
	private String authServiceURL;

	private String appName;
	private String appVersion;

	private String appKey;
	private String appSecret;

	private List<IdentityService> identityServices = new ArrayList<IdentityService>();

	private String testTriggeredBy;

	private List<String> testPlansExecutionOrder = new ArrayList<String>();

	private List<String> testPriorities = new ArrayList<String>();// Filter Criteria: Testcases with priorties to be executed
	private List<String> testNames = new ArrayList<String>();// Filter Criteria: Testcases with names to be executed

	private List<Service> services = new ArrayList<Service>();// Initialised programatically

	private Map<String, String> variableCollectionMap = new HashMap<>();

	/**
	 * 
	 */
	public TestArticle() {
		super();
		readPropertyFileValues();
	}

	/**
	 * @param hostURL
	 * @param authServiceURL
	 * @param appName
	 * @param appVersion
	 * @param appKey
	 * @param appSecret
	 * @param identityServices
	 * @param testTriggeredBy
	 * @param testPlansExecutionOrder
	 * @param testPriorities
	 * @param testNames
	 * @param services
	 * @param variableCollectionMap
	 */
	public TestArticle(String hostURL, String authServiceURL, String appName, String appVersion, String appKey, String appSecret, List<IdentityService> identityServices,
			String testTriggeredBy, List<String> testPlansExecutionOrder, List<String> testPriorities, List<String> testNames, List<Service> services,
			Map<String, String> variableCollectionMap) {
		super();
		this.hostURL = hostURL;
		this.authServiceURL = authServiceURL;
		this.appName = appName;
		this.appVersion = appVersion;
		this.appKey = appKey;
		this.appSecret = appSecret;
		this.identityServices = identityServices;
		this.testTriggeredBy = testTriggeredBy;
		this.testPlansExecutionOrder = testPlansExecutionOrder;
		this.testPriorities = testPriorities;
		this.testNames = testNames;
		this.services = services;
		this.variableCollectionMap = variableCollectionMap;
		this.readPropertyFileValues();
	}

	/**
	 * @return the variableCollectionMap
	 */
	public Map<String, String> getVariableCollectionMap() {
		return variableCollectionMap;
	}

	/**
	 * @param variableCollectionMap the variableCollectionMap to set
	 */
	public void setVariableCollectionMap(Map<String, String> variableCollectionMap) {
		this.variableCollectionMap = variableCollectionMap;
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
	 * @return the appName
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * @param appName the appName to set
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}

	/**
	 * @return the appVersion
	 */
	public String getAppVersion() {
		return appVersion;
	}

	/**
	 * @param appVersion the appVersion to set
	 */
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	/**
	 * @return the appKey
	 */
	public String getAppKey() {
		return appKey;
	}

	/**
	 * @param appKey the appKey to set
	 */
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	/**
	 * @return the appSecret
	 */
	public String getAppSecret() {
		return appSecret;
	}

	/**
	 * @param appSecret the appSecret to set
	 */
	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	/**
	 * @return the identityServices
	 */
	public List<IdentityService> getIdentityServices() {
		return identityServices;
	}

	/**
	 * @param identityServices the identityServices to set
	 */
	public void setIdentityServices(List<IdentityService> identityServices) {
		this.identityServices = identityServices;
	}

	/**
	 * @return the services
	 */
	public List<Service> getServices() {
		return services;
	}

	/**
	 * @param services the services to set
	 */
	public void setServices(List<Service> services) {
		this.services = services;
	}

	/**
	 * @return the testTriggeredBy
	 */
	public String getTestTriggeredBy() {
		return testTriggeredBy;
	}

	/**
	 * @param testTriggeredBy the testTriggeredBy to set
	 */
	public void setTestTriggeredBy(String testTriggeredBy) {
		this.testTriggeredBy = testTriggeredBy;
	}

	/**
	 * @return the testPlansExecutionOrder
	 */
	public List<String> getTestPlansExecutionOrder() {
		return testPlansExecutionOrder;
	}

	/**
	 * @param testPlansExecutionOrder the testPlansExecutionOrder to set
	 */
	public void setTestPlansExecutionOrder(List<String> testPlansExecutionOrder) {
		this.testPlansExecutionOrder = testPlansExecutionOrder;
	}

	/**
	 * @return the testPriorities
	 */
	public List<String> getTestPriorities() {
		return testPriorities;
	}

	/**
	 * @param testPriorities the testPriorities to set
	 */
	public void setTestPriorities(List<String> testPriorities) {
		this.testPriorities = testPriorities;
	}

	/**
	 * @return the testNames
	 */
	public List<String> getTestNames() {
		return testNames;
	}

	/**
	 * @param testNames the testNames to set
	 */
	public void setTestNames(List<String> testNames) {
		this.testNames = testNames;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * Method to get the instance of an Identity Service based on Provider Name
	 * 
	 * @param providerName
	 * @return instance of IdentityService
	 */
	public IdentityService getIdentityService(String providerName) {
		if (StringUtils.isBlank(providerName)) {
			return null;
		}

		for (IdentityService identityService : identityServices) {
			if (StringUtils.equals(providerName, identityService.getProviderName())) {
				return identityService;
			}
		}
		return null;
	}

	/**
	 * Method to read the supplied property file and append the values to the variable collection map
	 */
	private void readPropertyFileValues() {

		if (this.variableCollectionMap == null) {
			this.variableCollectionMap = new HashMap<>();
		}

		Set<String> propertyNames = VariableValues.getPropertyNames();
		if (propertyNames != null && !propertyNames.isEmpty()) {
			for (String property : propertyNames) {
				if (!this.variableCollectionMap.containsKey(property)) {
					this.variableCollectionMap.put(property, VariableValues.getValue(property));
				}
			}
		}

	}
}
