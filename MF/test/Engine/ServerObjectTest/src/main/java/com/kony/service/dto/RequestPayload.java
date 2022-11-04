package com.kony.service.dto;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO of Request Payload
 *
 * @author Aditya Mankal
 */
public class RequestPayload implements Serializable {

	private static final long serialVersionUID = 8106134794666475786L;

	private String url;
	private String requestBody;
	private String requestMethod;
	private String customAuthBody;
	private String identityServiceName;
	private boolean isCustomAuthRequest;
	private Map<String, String> requestHeaders;
	private Map<String, String> customAuthHeaders;

	/**
	 * 
	 */
	public RequestPayload() {
		super();
	}

	/**
	 * @param url
	 * @param requestMethod
	 * @param identityServiceName
	 * @param requestBody
	 * @param requestHeaders
	 * @param customAuthBody
	 * @param customAuthHeaders
	 * @param isCustomAuthRequest
	 */
	public RequestPayload(String url, String requestMethod, String identityServiceName, String requestBody, Map<String, String> requestHeaders, String customAuthBody,
			Map<String, String> customAuthHeaders, boolean isCustomAuthRequest) {
		super();
		this.url = url;
		this.requestMethod = requestMethod;
		this.identityServiceName = identityServiceName;
		this.requestBody = requestBody;
		this.requestHeaders = requestHeaders;
		this.customAuthBody = customAuthBody;
		this.customAuthHeaders = customAuthHeaders;
		this.isCustomAuthRequest = isCustomAuthRequest;
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
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
