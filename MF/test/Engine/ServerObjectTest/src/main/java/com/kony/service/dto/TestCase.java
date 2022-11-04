package com.kony.service.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO of Test Attributes
 *
 * @author Aditya Mankal
 */
public class TestCase implements Serializable {

	private static final long serialVersionUID = -8685206194788392314L;

	private String id;
	private String name;
	private int testSequence;
	private String requestBody;
	private String testPriority;
	private String securityLevel;
	private String customAuthBody;
	private String identityService;
	private boolean isCustomAuthTest;
	private Map<String, String> requestHeaders;
	private List<Assertion> responseBodyAssertions;
	private List<Assertion> responseHeaderAssertions;
	private Map<String, String> customAuthHeaders;

	/**
	 * 
	 */
	public TestCase() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 * @param testSequence
	 * @param requestBody
	 * @param testPriority
	 * @param securityLevel
	 * @param customAuthBody
	 * @param identityService
	 * @param isCustomAuthTest
	 * @param requestHeaders
	 * @param responseBodyAssertions
	 * @param responseHeaderAssertions
	 * @param customAuthHeaders
	 */
	public TestCase(String id, String name, int testSequence, String requestBody, String testPriority, String securityLevel, String customAuthBody, String identityService,
			boolean isCustomAuthTest, Map<String, String> requestHeaders, List<Assertion> responseBodyAssertions, List<Assertion> responseHeaderAssertions,
			Map<String, String> customAuthHeaders) {
		super();
		this.id = id;
		this.name = name;
		this.testSequence = testSequence;
		this.requestBody = requestBody;
		this.testPriority = testPriority;
		this.securityLevel = securityLevel;
		this.customAuthBody = customAuthBody;
		this.identityService = identityService;
		this.isCustomAuthTest = isCustomAuthTest;
		this.requestHeaders = requestHeaders;
		this.responseBodyAssertions = responseBodyAssertions;
		this.responseHeaderAssertions = responseHeaderAssertions;
		this.customAuthHeaders = customAuthHeaders;
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
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
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
	 * @return the identityService
	 */
	public String getIdentityService() {
		return identityService;
	}

	/**
	 * @param identityService the identityService to set
	 */
	public void setIdentityService(String identityService) {
		this.identityService = identityService;
	}

	/**
	 * @return the testPriority
	 */
	public String getTestPriority() {
		return testPriority;
	}

	/**
	 * @param testPriority the testPriority to set
	 */
	public void setTestPriority(String testPriority) {
		this.testPriority = testPriority;
	}

	/**
	 * @return the testSequence
	 */
	public int getTestSequence() {
		return testSequence;
	}

	/**
	 * @param testSequence the testSequence to set
	 */
	public void setTestSequence(int testSequence) {
		this.testSequence = testSequence;
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
	 * @return the responseBodyAssertions
	 */
	public List<Assertion> getResponseBodyAssertions() {
		return responseBodyAssertions;
	}

	/**
	 * @param responseBodyAssertions the responseBodyAssertions to set
	 */
	public void setResponseBodyAssertions(List<Assertion> responseBodyAssertions) {
		this.responseBodyAssertions = responseBodyAssertions;
	}

	/**
	 * @return the responseHeaderAssertions
	 */
	public List<Assertion> getResponseHeaderAssertions() {
		return responseHeaderAssertions;
	}

	/**
	 * @param responseHeaderAssertions the responseHeaderAssertions to set
	 */
	public void setResponseHeaderAssertions(List<Assertion> responseHeaderAssertions) {
		this.responseHeaderAssertions = responseHeaderAssertions;
	}

	/**
	 * @return the isCustomAuthTest
	 */
	@JsonProperty(value = "isCustomAuthTest")
	public boolean isCustomAuthTest() {
		return isCustomAuthTest;
	}

	/**
	 * @param isCustomAuthTest the isCustomAuthTest to set
	 */
	public void setIsCustomAuthTest(boolean isCustomAuthTest) {
		this.isCustomAuthTest = isCustomAuthTest;
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
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "Test: " + name;
	}

}
