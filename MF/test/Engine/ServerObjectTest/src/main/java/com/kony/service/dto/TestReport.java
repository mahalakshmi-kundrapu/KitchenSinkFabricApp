package com.kony.service.dto;

import java.io.Serializable;
import java.util.Map;

import org.apache.http.HttpHeaders;

import com.kony.service.constants.TestResult;

/**
 * DTO of Test Result Attributes
 *
 * @author Aditya Mankal
 */
public class TestReport implements Serializable {

	private static final long serialVersionUID = -2805777155906975333L;

	private TestCase test;
	private TestResult result;
	private String responseBody;
	private String testMessage;
	private Map<String, String> responseHeaders;
	private Map<Assertion, String> assertionResults;
	private int statusCode;
	private int httpStatusCode;
	private double duration;
	private int contentLength;

	/**
	 * 
	 */
	public TestReport() {
		super();
	}

	/**
	 * @param test
	 * @param requestBody
	 * @param responseBody
	 * @param responseHeaders
	 * @param result
	 * @param assertionResults
	 * @param statusCode
	 * @param httpStatusCode
	 * @param duration
	 */
	public TestReport(TestCase test, String responseBody, Map<String, String> responseHeaders, TestResult result, String testMessage, Map<Assertion, String> assertionResults,
			int statusCode, int httpStatusCode, double duration) {
		super();
		this.test = test;
		this.responseBody = responseBody;
		this.responseHeaders = responseHeaders;
		this.result = result;
		this.testMessage = testMessage;
		this.assertionResults = assertionResults;
		this.statusCode = statusCode;
		this.httpStatusCode = httpStatusCode;
		this.duration = duration;

		this.contentLength = computeContentLength();
	}

	/**
	 * @return the test
	 */
	public TestCase getTest() {
		return test;
	}

	/**
	 * @param test the test to set
	 */
	public void setTest(TestCase test) {
		this.test = test;
	}

	/**
	 * @return the result
	 */
	public TestResult getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(TestResult result) {
		this.result = result;
	}

	/**
	 * @return the responseBody
	 */
	public String getResponseBody() {
		return responseBody;
	}

	/**
	 * @param responseBody the responseBody to set
	 */
	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	/**
	 * @return the responseHeaders
	 */
	public Map<String, String> getResponseHeaders() {
		return responseHeaders;
	}

	/**
	 * @param responseHeaders the responseHeaders to set
	 */
	public void setResponseHeaders(Map<String, String> responseHeaders) {
		this.responseHeaders = responseHeaders;
	}

	/**
	 * @return the assertionResults
	 */
	public Map<Assertion, String> getAssertionResults() {
		return assertionResults;
	}

	/**
	 * @param assertionResults the assertionResults to set
	 */
	public void setAssertionResults(Map<Assertion, String> assertionResults) {
		this.assertionResults = assertionResults;
	}

	/**
	 * @return the statusCode
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode the statusCode to set
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * @return the httpStatusCode
	 */
	public int getHttpStatusCode() {
		return httpStatusCode;
	}

	/**
	 * @param httpStatusCode the httpStatusCode to set
	 */
	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}

	/**
	 * @return the duration
	 */
	public double getDuration() {
		return duration;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(double duration) {
		this.duration = duration;
	}

	/**
	 * @return the testMessage
	 */
	public String getTestMessage() {
		return testMessage;
	}

	/**
	 * @param testMessage the testMessage to set
	 */
	public void setTestMessage(String testMessage) {
		this.testMessage = testMessage;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @return the contentLength
	 */
	public int getContentLength() {
		return contentLength;
	}

	@Override
	public String toString() {
		return this.test.getName();
	}

	private int computeContentLength() {
		try {
			return Integer.parseInt(responseHeaders.get(HttpHeaders.CONTENT_LENGTH.toString()));
		} catch (Exception e) {
			return 0;
		}
	}

}
