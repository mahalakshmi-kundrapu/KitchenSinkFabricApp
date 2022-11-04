package com.kony.service.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * DTO of Identity Service Attributes
 *
 * @author Aditya Mankal
 */
public class IdentityService implements Serializable {

	private static final long serialVersionUID = 452811282903381902L;

	private String requestBody;
	private String providerName;
	private int authTokenValidityInSeconds;
	private Map<String, String> requestHeaders;

	/**
	 * @param providerName
	 * @param requestBody
	 * @param authTokenValidityInSeconds
	 * @param requestHeaders
	 */
	public IdentityService(String providerName, String requestBody, int authTokenValidityInSeconds, Map<String, String> requestHeaders) {
		super();
		this.providerName = providerName;
		this.requestBody = requestBody;
		this.authTokenValidityInSeconds = authTokenValidityInSeconds;
		this.requestHeaders = requestHeaders;
	}

	/**
	 * 
	 */
	public IdentityService() {
		super();
	}

	/**
	 * @return the providerName
	 */
	public String getProviderName() {
		return providerName;
	}

	/**
	 * @param providerName the providerName to set
	 */
	public void setProviderName(String providerName) {
		this.providerName = providerName;
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
	 * @return the authTokenValidityInSeconds
	 */
	public int getAuthTokenValidityInSeconds() {
		return authTokenValidityInSeconds;
	}

	/**
	 * @param authTokenValidityInSeconds the authTokenValidityInSeconds to set
	 */
	public void setAuthTokenValidityInSeconds(int authTokenValidityInSeconds) {
		this.authTokenValidityInSeconds = authTokenValidityInSeconds;
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
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
