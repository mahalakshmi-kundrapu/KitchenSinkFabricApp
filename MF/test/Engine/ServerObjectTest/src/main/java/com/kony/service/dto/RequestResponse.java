package com.kony.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO of Request Response
 *
 * @author Aditya Mankal
 */
public class RequestResponse implements Serializable {

	private static final long serialVersionUID = -5051843943912051490L;

	private HTTPResponse httpResponse;
	private List<String> bodyAssertionPaths = new ArrayList<String>();
	private List<String> headerAssertionPaths = new ArrayList<String>();
	private boolean isAuthRequestSuccessful = true;

	/**
	 * 
	 */
	public RequestResponse() {
		super();
	}

	/**
	 * @param httpResponse
	 * @param bodyAssertionPaths
	 * @param headerAssertionPaths
	 * @param isAuthRequestSuccessful
	 */
	public RequestResponse(HTTPResponse httpResponse, List<String> bodyAssertionPaths, List<String> headerAssertionPaths, boolean isAuthRequestSuccessful) {
		super();
		this.httpResponse = httpResponse;
		this.bodyAssertionPaths = bodyAssertionPaths;
		this.headerAssertionPaths = headerAssertionPaths;
		this.isAuthRequestSuccessful = isAuthRequestSuccessful;
	}

	/**
	 * @return the httpResponse
	 */
	public HTTPResponse getHttpResponse() {
		return httpResponse;
	}

	/**
	 * @param httpResponse the httpResponse to set
	 */
	public void setHttpResponse(HTTPResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

	/**
	 * @return the bodyAssertionPaths
	 */
	public List<String> getBodyAssertionPaths() {
		return bodyAssertionPaths;
	}

	/**
	 * @param bodyAssertionPaths the bodyAssertionPaths to set
	 */
	public void setBodyAssertionPaths(List<String> bodyAssertionPaths) {
		this.bodyAssertionPaths = bodyAssertionPaths;
	}

	/**
	 * @return the headerAssertionPaths
	 */
	public List<String> getHeaderAssertionPaths() {
		return headerAssertionPaths;
	}

	/**
	 * @param headerAssertionPaths the headerAssertionPaths to set
	 */
	public void setHeaderAssertionPaths(List<String> headerAssertionPaths) {
		this.headerAssertionPaths = headerAssertionPaths;
	}

	/**
	 * @return the isAuthRequestSuccessful
	 */
	public boolean isAuthRequestSuccessful() {
		return isAuthRequestSuccessful;
	}

	/**
	 * @param isAuthRequestSuccessful the isAuthRequestSuccessful to set
	 */
	public void setAuthRequestSuccessful(boolean isAuthRequestSuccessful) {
		this.isAuthRequestSuccessful = isAuthRequestSuccessful;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
