package com.kony.service.dto;

import java.util.Map;

/**
 * DTO of Common HTTP Response Attributes
 *
 * @author Aditya Mankal
 */
public class HTTPResponse {

	private final int responseCode;
	private final String responseBody;
	private final double responseTime;
	private final Map<String, String> responseHeaders;

	/**
	 * @param responseHeaders
	 * @param responseCode
	 * @param responseBody
	 */
	public HTTPResponse(Map<String, String> responseHeaders, int responseCode, String responseBody) {
		super();
		this.responseHeaders = responseHeaders;
		this.responseCode = responseCode;
		this.responseBody = responseBody;
		this.responseTime = 0;
	}

	/**
	 * @param responseHeaders
	 * @param responseCode
	 * @param responseBody
	 * @param responseTime
	 */
	public HTTPResponse(Map<String, String> responseHeaders, int responseCode, String responseBody, double responseTime) {
		super();
		this.responseHeaders = responseHeaders;
		this.responseCode = responseCode;
		this.responseBody = responseBody;
		this.responseTime = responseTime;
	}

	/**
	 * @return the responseHeaders
	 */
	public Map<String, String> getResponseHeaders() {
		return responseHeaders;
	}

	/**
	 * @return the responseCode
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * @return the responseBody
	 */
	public String getResponseBody() {
		return responseBody;
	}

	/**
	 * @return the responseTime
	 */
	public double getResponseTime() {
		return responseTime;
	}

}
