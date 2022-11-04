package com.kony.service.definer.dto;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kony.service.dto.IdentityService;

/**
 * DTO of ServiceRequest
 *
 * @author Aditya Mankal
 */
public class ServiceRequest implements Serializable {

	private static final long serialVersionUID = 8106134794666475786L;
	private String url;

	private String requestBody;
	private String requestMethod;
	private Map<String, String> requestHeaders;

	private String appKey;
	private String appSecret;
	private String securityLevel;
	private String authServiceURL;
	private String identityServiceName;
	private IdentityService identityService;

	private String customAuthBody;
	private boolean isCustomAuthRequest;
	private Map<String, String> customAuthHeaders;

	/**
	 * 
	 */
	public ServiceRequest() {
		super();
	}

	/**
	 * @param url
	 * @param requestBody
	 * @param requestMethod
	 * @param appKey
	 * @param appSecret
	 * @param securityLevel
	 * @param authServiceURL
	 * @param identityServiceName
	 * @param identityService
	 * @param isCustomAuthRequest
	 * @param customAuthBody
	 * @param customAuthHeaders
	 * @param requestHeaders
	 */
	public ServiceRequest(String url, String requestBody, String requestMethod, String appKey, String appSecret, String securityLevel, String authServiceURL,
			String identityServiceName, IdentityService identityService, boolean isCustomAuthRequest, String customAuthBody, Map<String, String> customAuthHeaders,
			Map<String, String> requestHeaders) {
		super();
		this.url = url;
		this.requestBody = requestBody;
		this.requestMethod = requestMethod;
		this.appKey = appKey;
		this.appSecret = appSecret;
		this.securityLevel = securityLevel;
		this.authServiceURL = authServiceURL;
		this.identityServiceName = identityServiceName;
		this.identityService = identityService;
		this.isCustomAuthRequest = isCustomAuthRequest;
		this.customAuthBody = customAuthBody;
		this.customAuthHeaders = customAuthHeaders;
		this.requestHeaders = requestHeaders;
	}

	/**
	 * @return the securityLevel
	 */
	public String getSecurityLevel() {
		return securityLevel;
	}

	/**
	 * @param securityLevel the securityLevel to set
	 */
	public void setSecurityLevel(String securityLevel) {
		this.securityLevel = securityLevel;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the requestMethod
	 */
	public String getRequestMethod() {
		return requestMethod;
	}

	/**
	 * @param requestMethod the requestMethod to set
	 */
	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	/**
	 * @return the identityServiceName
	 */
	public String getIdentityServiceName() {
		return identityServiceName;
	}

	/**
	 * @param identityServiceName the identityServiceName to set
	 */
	public void setIdentityServiceName(String identityServiceName) {
		this.identityServiceName = identityServiceName;
	}

	/**
	 * @return the requestBody
	 */
	public String getRequestBody() {
		return requestBody;
	}

	/**
	 * @param requestBody the requestBody to set
	 */
	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	/**
	 * @return the requestHeaders
	 */
	public Map<String, String> getRequestHeaders() {
		return requestHeaders;
	}

	/**
	 * @param requestHeaders the requestHeaders to set
	 */
	public void setRequestHeaders(Map<String, String> requestHeaders) {
		this.requestHeaders = requestHeaders;
	}

	/**
	 * @return the customAuthBody
	 */
	public String getCustomAuthBody() {
		return customAuthBody;
	}

	/**
	 * @param customAuthBody the customAuthBody to set
	 */
	public void setCustomAuthBody(String customAuthBody) {
		this.customAuthBody = customAuthBody;
	}

	/**
	 * @return the customAuthHeaders
	 */
	public Map<String, String> getCustomAuthHeaders() {
		return customAuthHeaders;
	}

	/**
	 * @param customAuthHeaders the customAuthHeaders to set
	 */
	public void setCustomAuthHeaders(Map<String, String> customAuthHeaders) {
		this.customAuthHeaders = customAuthHeaders;
	}

	/**
	 * @return the isCustomAuthRequest
	 */
	@JsonProperty(value = "isCustomAuthRequest")
	public boolean isCustomAuthRequest() {
		return isCustomAuthRequest;
	}

	/**
	 * @param isCustomAuthRequest the isCustomAuthRequest to set
	 */
	public void setIsCustomAuthRequest(boolean isCustomAuthRequest) {
		this.isCustomAuthRequest = isCustomAuthRequest;
	}

	/**
	 * @return the identityService
	 */
	public IdentityService getIdentityService() {
		return identityService;
	}

	/**
	 * @param identityService the identityService to set
	 */
	public void setIdentityService(IdentityService identityService) {
		this.identityService = identityService;
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
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
