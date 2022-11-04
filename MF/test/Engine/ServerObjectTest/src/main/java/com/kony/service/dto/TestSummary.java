package com.kony.service.dto;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DTO of Test Summary
 *
 * @author Aditya Mankal
 */
public class TestSummary {

	private String appName;
	private String appVersion;

	private Date testStartDateTime;
	private Date testCompletionDateTime;

	private String testInititedBy;

	private String testEnvironmentHostURL;
	private String testEnvironmentAuthServiceURL;

	private Map<Service, List<TestReport>> testResults;

	/**
	 * 
	 */
	public TestSummary() {
		super();
	}

	/**
	 * @param appName
	 * @param appVersion
	 * @param testStartDateTime
	 * @param testCompletionDateTime
	 * @param testInititedBy
	 * @param testEnvironmentHostURL
	 * @param testEnvironmentAuthServiceURL
	 * @param testResults
	 */
	public TestSummary(String appName, String appVersion, Date testStartDateTime, Date testCompletionDateTime, String testInititedBy, String testEnvironmentHostURL,
			String testEnvironmentAuthServiceURL, Map<Service, List<TestReport>> testResults) {
		super();
		this.appName = appName;
		this.appVersion = appVersion;
		this.testStartDateTime = testStartDateTime;
		this.testCompletionDateTime = testCompletionDateTime;
		this.testInititedBy = testInititedBy;
		this.testEnvironmentHostURL = testEnvironmentHostURL;
		this.testEnvironmentAuthServiceURL = testEnvironmentAuthServiceURL;
		this.testResults = testResults;
	}

	/**
	 * @return the duration
	 */
	public double getDuration() {
		return computeDuration(testStartDateTime, testCompletionDateTime);
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
	 * @return the testStartDateTime
	 */
	public Date getTestStartDateTime() {
		return testStartDateTime;
	}

	/**
	 * @param testStartDateTime the testStartDateTime to set
	 */
	public void setTestStartDateTime(Date testStartDateTime) {
		this.testStartDateTime = testStartDateTime;
	}

	/**
	 * @return the testCompletionDateTime
	 */
	public Date getTestCompletionDateTime() {
		return testCompletionDateTime;
	}

	/**
	 * @param testCompletionDateTime the testCompletionDateTime to set
	 */
	public void setTestCompletionDateTime(Date testCompletionDateTime) {
		this.testCompletionDateTime = testCompletionDateTime;
	}

	/**
	 * @return the testInititedBy
	 */
	public String getTestInititedBy() {
		return testInititedBy;
	}

	/**
	 * @param testInititedBy the testInititedBy to set
	 */
	public void setTestInititedBy(String testInititedBy) {
		this.testInititedBy = testInititedBy;
	}

	/**
	 * @return the testEnvironmentHostURL
	 */
	public String getTestEnvironmentHostURL() {
		return testEnvironmentHostURL;
	}

	/**
	 * @param testEnvironmentHostURL the testEnvironmentHostURL to set
	 */
	public void setTestEnvironmentHostURL(String testEnvironmentHostURL) {
		this.testEnvironmentHostURL = testEnvironmentHostURL;
	}

	/**
	 * @return the testEnvironmentAuthServiceURL
	 */
	public String getTestEnvironmentAuthServiceURL() {
		return testEnvironmentAuthServiceURL;
	}

	/**
	 * @param testEnvironmentAuthServiceURL the testEnvironmentAuthServiceURL to set
	 */
	public void setTestEnvironmentAuthServiceURL(String testEnvironmentAuthServiceURL) {
		this.testEnvironmentAuthServiceURL = testEnvironmentAuthServiceURL;
	}

	/**
	 * @return the testResults
	 */
	public Map<Service, List<TestReport>> getTestResults() {
		return testResults;
	}

	/**
	 * @param testResults the testResults to set
	 */
	public void setTestResults(Map<Service, List<TestReport>> testResults) {
		this.testResults = testResults;
	}

	private double computeDuration(Date testStartDateTime, Date testCompletionDateTime) {
		if (testStartDateTime != null && testCompletionDateTime != null) {
			return (testCompletionDateTime.getTime() - testStartDateTime.getTime());
		}
		return 0.0;
	}
}
