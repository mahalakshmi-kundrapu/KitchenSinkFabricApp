package com.kony.service.dto;

/**
 * DTO of Identity Service Response Attributes
 *
 * @author Aditya Mankal
 */
public class IdentityResponse {

	private final HTTPResponse httpResponse;
	private final String authToken;
	private final String userAttributes;
	private final String securityAttributes;
	private final boolean isLoginSuccessful;

	/**
	 * @param responseBody
	 * @param authToken
	 * @param userAttributes
	 * @param securityAttributes
	 */
	public IdentityResponse(HTTPResponse httpResponse, String authToken, String userAttributes, String securityAttributes, boolean isLoginSuccessful) {
		super();
		this.httpResponse = httpResponse;
		this.authToken = authToken;
		this.userAttributes = userAttributes;
		this.securityAttributes = securityAttributes;
		this.isLoginSuccessful = isLoginSuccessful;
	}

	/**
	 * @return the httpResponse
	 */
	public HTTPResponse getHttpResponse() {
		return httpResponse;
	}

	/**
	 * @return the authToken
	 */
	public String getAuthToken() {
		return authToken;
	}

	/**
	 * @return the userAttributes
	 */
	public String getUserAttributes() {
		return userAttributes;
	}

	/**
	 * @return the securityAttributes
	 */
	public String getSecurityAttributes() {
		return securityAttributes;
	}

	/**
	 * @return the isLoginSuccessful
	 */
	public boolean isLoginSuccessful() {
		return isLoginSuccessful;
	}

}
