package com.kony.service.test.manager;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.Reporter;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.kony.service.constants.Datatype;
import com.kony.service.constants.IdentityConstants;
import com.kony.service.constants.SecurityLevel;
import com.kony.service.constants.TestResult;
import com.kony.service.core.http.HTTPConnector;
import com.kony.service.dto.Assertion;
import com.kony.service.dto.HTTPResponse;
import com.kony.service.dto.IdentityResponse;
import com.kony.service.dto.IdentityService;
import com.kony.service.dto.Service;
import com.kony.service.dto.TestArticle;
import com.kony.service.dto.TestCase;
import com.kony.service.dto.TestReport;
import com.kony.service.exception.AuthenticationFailureException;
import com.kony.service.exception.InvalidServiceConfigurationException;
import com.kony.service.identity.AnonymousAppUserIdentityManager;
import com.kony.service.identity.AuthenticatedAppUserIdentityManager;
import com.kony.service.identity.IdentityManager;
import com.kony.service.test.executor.TestNGServiceExecutor;
import com.kony.service.util.JSONUtilities;

import net.minidev.json.JSONArray;

/**
 * Class to perform the basic operations on Test Plans and Test Reports
 *
 * @author Aditya Mankal
 */
public class TestManager {

	private static Map<String, IdentityManager> authenticatedAppUserIdentityManagerMap = new HashMap<>();

	private static final Logger LOG = LogManager.getLogger(TestNGServiceExecutor.class);

	public static final String MISSING_KEY_MESSAGE_TEMPLATE = "Entry Missing";
	public static final String TYPE_MISMTACH_MESSAGE_TEMPLATE = "Type Mismatch";
	public static final String VALUE_MISMTACH_MESSAGE_TEMPLATE = "Value Mismatch";
	public static final String UNRECOGNISED_VALUE_TYPE_MESSAGE = "Unrecognised Value Type";
	public static final String INVALID_JSON_PATH_EXPRESSION = "Invalid JSON Path Expression";
	public static final String MISSING_KEY_IN_ONE_OR_MORE_OBJECTS_MESSAGE_TEMPLATE = "Entry Missing in 1 or more objects";

	public static final String REQUEST_TIMEOUT_MESSAGE_TEMPLATE = "Request Timeout";
	public static final String AUTH_FAILURE_MESSAGE_TEMPLATE = "Failed to Fetch Auth Token";
	public static final String SERVICE_CALL_FAILURE_MESSAGE_TEMPLATE = "Failed to Execute Service Call";

	/**
	 * Method to execute a Test Case
	 * 
	 * @param testArticle
	 * @param service
	 * @param test
	 * @return TestReport
	 */
	public static TestReport executeTest(TestArticle testArticle, Service service, TestCase test) {

		int currHttpStatusCode = -1;
		double currResponseTime = 0;
		TestResult currTestStatus = null;
		String currTestMessage = StringUtils.EMPTY;
		String currResponseBody = StringUtils.EMPTY;
		Map<String, String> currResponseHeaders = new HashMap<>();
		Map<Assertion, String> assertionResults = new HashMap<>();
		String currServiceURL = testArticle.getHostURL() + "/" + service.getUrl();

		try {

			// Reset the Failed Assertion Messages List
			assertionResults.clear();

			// Substitute variable references
			TestManager.substitutePlaceHolderValues(test, testArticle.getVariableCollectionMap());

			if (!StringUtils.equals(test.getSecurityLevel(), SecurityLevel.PUBLIC.getValue())) {
				IdentityResponse currIdentityServiceResponse = executeIdentityRequest(testArticle, test);
				// Set the Authentication-Token to the Header Map
				String currAuthToken = currIdentityServiceResponse.getAuthToken();
				if (StringUtils.isNotBlank(currAuthToken)) {
					test.getRequestHeaders().put(IdentityConstants.X_KONY_AUTHORIZATION, currAuthToken);
				}
			}

			// Instantiate a HTTP Client to execute the request
			LOG.debug("Initialising HTTP Client to send Request...");
			HTTPConnector httpConnector = new HTTPConnector(service.getHttpMethod(), currServiceURL, test.getRequestBody(), test.getRequestHeaders());
			LOG.debug("HTTP Client Initialised");

			// Execute HTTP Request
			LOG.debug("Executing HTTP Request...");
			HTTPResponse currHttpResponse = httpConnector.executeClientAndSendRequest(); // Execute the Request
			if (currHttpResponse == null) {
				Reporter.log("Failed to execute HTTP Request.Verify Service Configuration.");
				LOG.debug("Failed to execute HTTP Request.Verify Service Configuration.");
				throw new InvalidServiceConfigurationException("Invalid Service Configuration");
			}
			LOG.debug("Response Received");

			// Fetch the Response Attributes returned by executed HTTP Request
			currResponseBody = currHttpResponse.getResponseBody();
			currResponseHeaders = currHttpResponse.getResponseHeaders();
			currHttpStatusCode = currHttpResponse.getResponseCode();
			currResponseTime = currHttpResponse.getResponseTime();

			// Perform Assertion checks
			assertionResults = validateResponse(test, currResponseBody, testArticle);

			// Determine Test Status
			if (getCountOfFailedAssertions(assertionResults) == 0) {
				// currFailedAssertionMessages contains no error messages
				currTestStatus = TestResult.PASS;
			} else {
				if (assertionResults.values().contains(UNRECOGNISED_VALUE_TYPE_MESSAGE) || assertionResults.values().contains(INVALID_JSON_PATH_EXPRESSION)) {
					// Invalid Test Configuration
					currTestStatus = TestResult.ERROR;
				} else {
					// Failed Test Case
					currTestStatus = TestResult.FAIL;
				}
			}
			currTestMessage = JSONUtilities.stringify(assertionResults);

		} catch (SocketTimeoutException sce) {
			// Timeout in executing request
			Reporter.log("Request Timeout");
			LOG.error("Timeout in executing request with the URL" + currServiceURL);
			currTestStatus = TestResult.FAIL;
			currTestMessage = REQUEST_TIMEOUT_MESSAGE_TEMPLATE;

		} catch (InvalidServiceConfigurationException e) {
			// Invalid Test Configuration
			Reporter.log("Invalid Test Configuration");
			LOG.error("Invalid Test Configuration");
			currTestStatus = TestResult.FAIL;
			currTestMessage = SERVICE_CALL_FAILURE_MESSAGE_TEMPLATE;

		} catch (AuthenticationFailureException e) {
			// Failed to Fetch Authentication-Token
			Reporter.log("Failed to Fetch AuthToken");
			LOG.error("Failed to Fetch AuthToken. Message:", e);
			currTestStatus = TestResult.SKIP;
			currTestMessage = AUTH_FAILURE_MESSAGE_TEMPLATE;

		} catch (Exception e) {
			// Unexpected Exception in Test Execution Flow
			Reporter.log("Exception. Check Log for Trace");
			LOG.error("Exeception in Test Execution. Exception:", e);
			currTestStatus = TestResult.ERROR;
			currTestMessage = "Exception";

		}
		LOG.debug("Test Status:" + currTestStatus);
		TestReport testReport = new TestReport(test, currResponseBody, currResponseHeaders, currTestStatus, currTestMessage, assertionResults, currHttpStatusCode,
				currHttpStatusCode, currResponseTime);

		return testReport;
	}

	/**
	 * Method to subsitute the place-holders with the actual values for a service
	 * 
	 * For each service, the individual testcases are traversed and the place-holders in the
	 * Request-Body, Request-Headers, Custom-Auth-Body, Custom-Auth-Headers, Response-Body-Assertions,
	 * Response-Header-Assertion are substituted with the actual values.
	 * 
	 * WARNING: The variable collection map is updated with the execution of each test-case. To execute the test-cases with the
	 * latest variable values, call the method substitutePlaceHolderValues(TestCase testCase, Map<String, String> variableCollectionMap)
	 * for each test case during it's execution.
	 * 
	 * @param services
	 * @param variableCollectionMap
	 */
	public static void substitutePlaceHolderValues(Service service, Map<String, String> variableCollectionMap) {

		if (service == null || variableCollectionMap == null || variableCollectionMap.isEmpty()) {
			return;
		}

		List<Service> services = new ArrayList<>();
		services.add(service);

		substitutePlaceHolderValues(services, variableCollectionMap);
	}

	/**
	 * Method to subsitute the place-holders with the actual values for a list of services
	 * 
	 * For each service, the individual testcases are traversed and the place-holders in the
	 * Request-Body, Request-Headers, Custom-Auth-Body, Custom-Auth-Headers, Response-Body-Assertions,
	 * Response-Header-Assertion are substituted with the actual values.
	 * 
	 * WARNING: The variable collection map is updated with the execution of each test-case. To execute the test-cases with the
	 * latest variable values, call the method substitutePlaceHolderValues(TestCase testCase, Map<String, String> variableCollectionMap)
	 * for each test case during it's execution.
	 * 
	 * @param services
	 * @param variableCollectionMap
	 */
	public static void substitutePlaceHolderValues(List<Service> services, Map<String, String> variableCollectionMap) {

		if (services == null || services.isEmpty() || variableCollectionMap == null || variableCollectionMap.isEmpty()) {
			return;
		}

		List<TestCase> testCases;

		// Traverse the services and update constituing Test-cases
		for (Service service : services) {

			// Traverse the variable-collection map

			testCases = service.getTests();
			if (testCases == null || testCases.isEmpty()) {
				continue;
			}

			// Traverse the Test-Cases of the current service
			for (TestCase testCase : testCases) {
				// Update each property of the current Test-Case
				substitutePlaceHolderValues(testCase, variableCollectionMap);
			}

		}

	}

	/**
	 * Method to subsitute the place-holders with the actual values for a testcase
	 * 
	 * For the testcase, placeholders in
	 * Request-Body, Request-Headers, Custom-Auth-Body, Custom-Auth-Headers, Response-Body-Assertions,
	 * Response-Header-Assertion are substituted with the actual values.
	 * 
	 * @param testcase
	 * @param variableCollectionMap
	 */
	public static void substitutePlaceHolderValues(TestCase testCase, Map<String, String> variableCollectionMap) {

		List<Assertion> assertions;

		String requestBody;
		String customAuthBody;

		String headerKey;
		String headerValue;

		String expectedValue;

		Map<String, String> requestHeaders, customAuthHeaders;
		for (Entry<String, String> currEntry : variableCollectionMap.entrySet()) {
			// Update Request Body
			requestBody = testCase.getRequestBody();
			if (StringUtils.isNotBlank(requestBody)) {
				requestBody = requestBody.replace("{{" + currEntry.getKey() + "}}", currEntry.getValue());
				testCase.setRequestBody(requestBody);
			}

			// Update Request headers
			requestHeaders = testCase.getRequestHeaders();
			if (requestHeaders != null && !requestHeaders.isEmpty()) {
				for (Entry<String, String> headerEntry : requestHeaders.entrySet()) {
					headerKey = headerEntry.getKey();
					headerValue = headerEntry.getValue();
					headerValue = headerValue.replace("{{" + currEntry.getKey() + "}}", currEntry.getValue());
					requestHeaders.put(headerKey, headerValue);
				}
			}

			// Update Response Body Assertions
			assertions = testCase.getResponseBodyAssertions();
			if (assertions != null && !assertions.isEmpty()) {
				for (Assertion assertion : assertions) {
					expectedValue = String.valueOf(assertion.getValue());
					expectedValue = expectedValue.replace("{{" + currEntry.getKey() + "}}", currEntry.getValue());
					assertion.setValue(expectedValue);
				}
			}

			// Update Response Header Assertions
			assertions = testCase.getResponseHeaderAssertions();
			if (assertions != null && !assertions.isEmpty()) {
				for (Assertion assertion : assertions) {
					expectedValue = String.valueOf(assertion.getValue());
					expectedValue = expectedValue.replace("{{" + currEntry.getKey() + "}}", currEntry.getValue());
					assertion.setValue(expectedValue);
				}
			}

			// Update Custom Auth Request Body
			customAuthBody = testCase.getCustomAuthBody();
			if (StringUtils.isNoneBlank(customAuthBody)) {
				customAuthBody = customAuthBody.replace("{{" + currEntry.getKey() + "}}", currEntry.getValue());
				testCase.setCustomAuthBody(customAuthBody);
			}

			// Update Custom Auth Headers
			customAuthHeaders = testCase.getCustomAuthHeaders();
			if (customAuthHeaders != null && !customAuthHeaders.isEmpty()) {
				for (Entry<String, String> headerEntry : customAuthHeaders.entrySet()) {
					headerKey = headerEntry.getKey();
					headerValue = headerEntry.getValue();
					headerValue = headerValue.replace("{{" + currEntry.getKey() + "}}", currEntry.getValue());
					customAuthHeaders.put(headerKey, headerValue);
				}
			}
		}
	}

	/**
	 * Method to perform the Response Assertion validations
	 * 
	 * @param test
	 * @param responseBody
	 * @return Map of type<Assertion,String>. Map Info: Assertion,Assertion status message
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static Map<Assertion, String> validateResponse(TestCase test, String responseBody)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return validateResponse(test, responseBody, null);
	}

	/**
	 * Method to perform the Response Assertion validations and set the Variable Collection Map values
	 * 
	 * @param test
	 * @param responseBody
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @return Map of type<Assertion,String>. Map Info: Assertion,Assertion status message
	 */
	public static Map<Assertion, String> validateResponse(TestCase test, String responseBody, TestArticle testArticle)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		boolean isResponseValidJSON = true;
		List<Assertion> assertions = new ArrayList<Assertion>();
		assertions.addAll(test.getResponseBodyAssertions());
		assertions.addAll(test.getResponseHeaderAssertions());

		Map<Assertion, String> validationResponseList = new HashMap<>();
		if (assertions == null || assertions.isEmpty()) {
			return validationResponseList;
		}

		try {
			// Attempt to parse response body to a JSON Object
			new JSONObject(responseBody);
		} catch (Exception e) {
			isResponseValidJSON = false;
			LOG.debug("Response is not a valid JSON Object. Cannot validate Assertions as expected");
		}

		String currPath = null;
		String currOperator = null;
		Object currExpectedValue = null;
		String currExpectedValueType = null;
		String currPathParentElementPath = null;

		int currCountOfValues = 0;
		int expectedCountOfValues = 0;

		Object currActualValue = null;
		Object currParentValue = null;

		boolean isNullableValue;
		boolean isValueAgnostic;

		JSONArray currValuesArray = new JSONArray();

		// Set to TRUE when the given JSON path denotes multiple elements
		boolean isMultiSelectPath = false;

		boolean currValueCheckResult;
		String currAssertionResult;
		int assertionIndex = 0, valueIndex = 0;

		Reporter.log("<b>---------- Assertions ----------<b><br>");

		for (Assertion assertion : assertions) {

			currActualValue = null;
			currParentValue = null;

			assertionIndex++;
			Reporter.log("<b>Assertion Index:</b>&nbsp;" + assertionIndex);

			currPath = assertion.getPath();
			Reporter.log("<b>Attribute Path:</b>&nbsp;" + currPath);

			currExpectedValueType = assertion.getDataType();
			Reporter.log("<b>Expected Value Type:</b>&nbsp;" + currExpectedValueType);

			isValueAgnostic = assertion.isValueAgnostic();
			Reporter.log("<b>Is Value Agnostic:</b>&nbsp;" + String.valueOf(isValueAgnostic).toUpperCase());

			if (isValueAgnostic == false) {
				currExpectedValue = assertion.getValue();
				Reporter.log("<b>Expected Value:</b>&nbsp;" + currExpectedValue);

				currOperator = assertion.getOperator();
				Reporter.log("<b>Operator:</b>&nbsp;" + currOperator);
			}

			isNullableValue = assertion.isNullable();
			Reporter.log("<b>Is Nullable Value:</b>&nbsp;" + String.valueOf(isNullableValue).toUpperCase());

			currAssertionResult = StringUtils.EMPTY;
			isMultiSelectPath = false;

			Reporter.log("<b>Is Response Valid JSON:</b>&nbsp;" + String.valueOf(isResponseValidJSON).toUpperCase());

			if (!isResponseValidJSON && !isNullableValue) {
				// Response Body is not a valid JSON. Missing mandatory value
				Reporter.log("<b>Assertion Result:</b>&nbsp" + MISSING_KEY_MESSAGE_TEMPLATE);
				validationResponseList.put(assertion, MISSING_KEY_MESSAGE_TEMPLATE);
				continue;
			}

			try {

				/*
				 * -- Flow Sequence --
				 * 1. Check if value is present. If yes, proceed to step 2.
				 * 1. Check if recognized value-type specified in assertion. If yes, step 2. Else, Failed Assertion.
				 * 2. Check expected value-type and actual value-type. If match, step 3. Else, Failed Assertion.
				 * 3. Check if assertion is non-value agnostic. If yes, step 4. Else, Passed Assertion
				 * 4. Check expected value and actual value. If match, Passed Assertion. Else, Failed Assertion
				 */

				// Throws PathNotFoundException if there is not element at the specified path
				currActualValue = JsonPath.parse(responseBody).read(currPath);

				if (currActualValue == null) {
					// Value is Present but, is null
					if (!isNullableValue) {
						// Missing mandatory value
						Reporter.log("<b>Assertion Result:</b>&nbsp;" + VALUE_MISMTACH_MESSAGE_TEMPLATE);
						currAssertionResult = VALUE_MISMTACH_MESSAGE_TEMPLATE;
					} else {
						// Missing non-mandatory value. Skip Value Check and Type Check. Passed Assertion
					}
				} else {
					// Validation for Type and Value
					if (Datatype.isRecognisedDataType(currExpectedValueType)) {

						currValuesArray.clear();

						// Returned value by JSON Library is of type JSONArray when assertion path is of type multi-select
						if (currActualValue instanceof JSONArray) {
							// Assertion Path represents multiple elements
							isMultiSelectPath = true;

							if (!assertion.isNullable()) {
								// Do not remove this check
								/*
								 * Verify if the expected value is found in all objects under validation
								 * Perform this check by computing the count of objects, and comparing it
								 * with the count of elements returned by the assertion path
								 */
								currValuesArray = (JSONArray) currActualValue;
								currCountOfValues = currValuesArray.size();
								currPathParentElementPath = StringUtils.substring(currPath, 0, currPath.lastIndexOf("[*]") + 3);
								if (StringUtils.endsWith(currPathParentElementPath, "[*]")) {
									try {
										currParentValue = JsonPath.parse(responseBody).read(currPathParentElementPath);
										if (currParentValue instanceof JSONArray) {
											expectedCountOfValues = ((JSONArray) currParentValue).size();
										}

										if (currCountOfValues != expectedCountOfValues) {
											Reporter.log("<b>Assertion Result:</b>&nbsp;" + MISSING_KEY_IN_ONE_OR_MORE_OBJECTS_MESSAGE_TEMPLATE);
											validationResponseList.put(assertion, MISSING_KEY_IN_ONE_OR_MORE_OBJECTS_MESSAGE_TEMPLATE);
											continue;
										}

									} catch (PathNotFoundException e) {
										// Invalid JSON Path Expression
										Reporter.log("<b>Assertion Result:</b>&nbsp;" + MISSING_KEY_IN_ONE_OR_MORE_OBJECTS_MESSAGE_TEMPLATE);
										validationResponseList.put(assertion, MISSING_KEY_IN_ONE_OR_MORE_OBJECTS_MESSAGE_TEMPLATE);
										continue;
									} catch (Exception e) {
										// Invalid JSON Path Expression
										LOG.error("Invalid JSON Path Expression:" + currPathParentElementPath);
										Reporter.log("<b>Assertion Result:</b>&nbsp;" + INVALID_JSON_PATH_EXPRESSION);
										validationResponseList.put(assertion, INVALID_JSON_PATH_EXPRESSION);
										continue;
									}
								}
							}

						} else {
							// Assertion Path represents a single element
							currValuesArray.add(currActualValue);
						}

						// Traverse the array of values
						for (valueIndex = 0; valueIndex < currValuesArray.size(); valueIndex++) {
							currActualValue = currValuesArray.get(valueIndex);

							// String Type Check
							if (StringUtils.equals(currExpectedValueType, Datatype.STRING.toString())) {
								if (currActualValue instanceof String) {
									// Value Type Match. Proceed with further checks
									Reporter.log("<b>Found Value Type:</b>&nbsp;" + Datatype.STRING.toString());
									if (isValueAgnostic) {
										// Value Agnostic assertion. Skip Value check. Passed Assertion
									} else {
										// Non Value Agnostic assertion. Check Value
										Reporter.log("<b>Found Value:</b>&nbsp;" + currActualValue);
										currValueCheckResult = Datatype.STRING.compare(currActualValue, currExpectedValue, currOperator);
										if (currValueCheckResult == false) {
											currAssertionResult = VALUE_MISMTACH_MESSAGE_TEMPLATE;
										} else {
											// Type Match and Value Match. Passed Assertion
										}
									}
								} else {
									// Value Type mismatch. Failed Assertion
									currAssertionResult = TYPE_MISMTACH_MESSAGE_TEMPLATE;
								}
							}

							// Number Type Check
							else if (StringUtils.equals(currExpectedValueType, Datatype.NUMBER.toString())) {
								if (currActualValue instanceof Integer) {
									// Value Type Match. Proceed with further checks
									Reporter.log("<b>Found Value Type:</b>&nbsp;" + Datatype.NUMBER.toString());
									if (isValueAgnostic) {
										// Value Agnostic assertion. Skip Value check. Passed Assertion
									} else {
										// Non Value Agnostic assertion. Check Value
										Reporter.log("<b>Found Value:</b>&nbsp;" + currActualValue);
										currValueCheckResult = Datatype.NUMBER.compare(currActualValue, currExpectedValue, currOperator);
										if (currValueCheckResult == false) {
											currAssertionResult = VALUE_MISMTACH_MESSAGE_TEMPLATE;
										} else {
											// Type Match and Value Match. Passed Assertion
										}
									}
								} else {
									// Value Type mismatch. Failed Assertion
									currAssertionResult = TYPE_MISMTACH_MESSAGE_TEMPLATE;
								}
							}

							// Boolean Type Check
							else if (StringUtils.equals(currExpectedValueType, Datatype.BOOLEAN.toString())) {
								if (currActualValue instanceof Boolean) {
									// Value Type Match. Proceed with further checks
									Reporter.log("<b>Found Value Type:</b>&nbsp;" + Datatype.BOOLEAN.toString());
									if (isValueAgnostic) {
										// Value Agnostic assertion. Skip Value check. Passed Assertion
									} else {
										// Non Value Agnostic assertion. Check Value
										Reporter.log("<b>Found Value:</b>&nbsp" + currActualValue);
										currValueCheckResult = Datatype.BOOLEAN.compare(currActualValue, currExpectedValue, currOperator);
										if (currValueCheckResult == false) {
											currAssertionResult = VALUE_MISMTACH_MESSAGE_TEMPLATE;
										} else {
											// Type Match and Value Match. Passed Assertion
										}
									}
								} else {
									// Value Type mismatch. Failed Assertion
									currAssertionResult = TYPE_MISMTACH_MESSAGE_TEMPLATE;
								}
							}

							// Array Type Check
							else if (StringUtils.equals(currExpectedValueType, Datatype.ARRAY.toString())) {
								if (currActualValue instanceof Array) {
									// Value Type Match. Proceed with further checks
									Reporter.log("<b>Found Value Type:</b>&nbsp;" + Datatype.ARRAY.toString());
									if (isValueAgnostic) {
										// Value Agnostic assertion. Skip Value check. Passed Assertion
									} else {
										// Non Value Agnostic assertion. Check Value
										Reporter.log("<b>Found Value:</b>&nbsp" + currActualValue);
										currValueCheckResult = Datatype.ARRAY.compare(currActualValue, currExpectedValue, currOperator);
										if (currValueCheckResult == false) {
											currAssertionResult = VALUE_MISMTACH_MESSAGE_TEMPLATE;
										} else {
											// Type Match and Value Match. Passed Assertion
										}
									}
								} else {
									// Value Type mismatch. Failed Assertion
									currAssertionResult = TYPE_MISMTACH_MESSAGE_TEMPLATE;
								}
							}

							// Object Type Check
							else if (StringUtils.equals(currExpectedValueType, Datatype.OBJECT.toString())) {
								if (currActualValue instanceof Array) {
									// Value Type Match. Proceed with further checks
									Reporter.log("<b>Found Value Type:</b>&nbsp;" + Datatype.OBJECT.toString());
									if (isValueAgnostic) {
										// Value Agnostic assertion. Skip Value check. Passed Assertion
									} else {
										// Non Value Agnostic assertion. Check Value
										Reporter.log("<b>Found Value:</b>&nbsp;" + currActualValue);
										currValueCheckResult = Datatype.OBJECT.compare(currActualValue, currExpectedValue, currOperator);
										if (currValueCheckResult == false) {
											currAssertionResult = VALUE_MISMTACH_MESSAGE_TEMPLATE;
										} else {
											// Type Match and Value Match. Passed Assertion
										}
									}
								} else {
									// Value Type mismatch. Failed Assertion
									currAssertionResult = TYPE_MISMTACH_MESSAGE_TEMPLATE;
								}
							}

							// Skip check of further values if current assertion has failed
							if (StringUtils.isNotBlank(currAssertionResult)) {
								break;
							}
						}

					} else {
						currAssertionResult = UNRECOGNISED_VALUE_TYPE_MESSAGE;
					}
				}
			} catch (PathNotFoundException e) {
				// Missing Value

				if (!isNullableValue) {
					// Missing mandatory value
					Reporter.log("<b>Assertion Result:</b>&nbsp" + MISSING_KEY_MESSAGE_TEMPLATE);
					currAssertionResult = MISSING_KEY_MESSAGE_TEMPLATE;
				} else {
					// Missing non-mandatory value. Skip Value Check and Type Check. Passed Assertion
				}

			} catch (Exception e) {
				// Invalid JSON Path Expression
				Reporter.log("<b>Assertion Result:</b>&nbsp" + INVALID_JSON_PATH_EXPRESSION);
				validationResponseList.put(assertion, INVALID_JSON_PATH_EXPRESSION);

			} finally {
				// Display Assertion Result
				if (StringUtils.isBlank(currAssertionResult)) {
					// Passed Assertion
					currAssertionResult = "-";
					if (testArticle != null) {
						if (currActualValue != null && StringUtils.isNotBlank(assertion.getVariableName())) {
							// Add value to variable Collection Map
							testArticle.getVariableCollectionMap().put(assertion.getVariableName(), String.valueOf(currActualValue));
						}
					}
					Reporter.log("<b>Assertion Result:</b>&nbsp;PASS");
				} else if (!isResponseValidJSON) {
					// Malformed Response. Cannot perform assertions
					Reporter.log("<b>Assertion Result:</b><a style=\"color:red;\">&nbsp;MALFORMED RESPONSE<a>");
				} else {
					// Failed assertion
					Reporter.log("<b>Assertion Result:</b><a style=\"color:red;\">&nbsp;FAIL<a>");
					if (isMultiSelectPath) {
						// JSON Path is of type multi-select. Add failed element index to response path
						currAssertionResult = currAssertionResult + " ->Index:" + valueIndex;
					}
				}

				Reporter.log("<br>");
				validationResponseList.put(assertion, currAssertionResult);
			}

		}
		return validationResponseList;
	}

	/**
	 * Method to return count of Failed Assertions
	 * 
	 * @param Map returned by validateResponse method
	 * @return count of Failed Assertions
	 */
	public static int getCountOfFailedAssertions(Map<Assertion, String> responseMap) {
		int count = 0;
		Set<Entry<Assertion, String>> responseMapEntrySet = responseMap.entrySet();
		for (Entry<Assertion, String> currEntry : responseMapEntrySet) {
			if (!StringUtils.equals(currEntry.getValue(), "-")) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Method to sort the Services based on Test Configuration
	 * 
	 * @param servicesList
	 * @param testConfiguration
	 * @return filtered list of Services (Sort order as per Test Configuration)
	 */
	public static List<Service> sortServices(List<Service> servicesList, TestArticle testArticle) {
		if (testArticle == null || servicesList == null || servicesList.isEmpty() || testArticle.getTestPlansExecutionOrder() == null
				|| testArticle.getTestPlansExecutionOrder().isEmpty()) {
			return servicesList;
		}

		List<Service> sortedServicesList = new ArrayList<>();
		List<String> serviceNames = testArticle.getTestPlansExecutionOrder();
		for (String currServiceName : serviceNames) {
			for (Service currService : servicesList) {
				if (StringUtils.equals(currService.getName(), currServiceName)) {
					sortedServicesList.add(currService);
				}
			}
		}

		for (Service currService : servicesList) {
			if (!sortedServicesList.contains(currService)) {
				sortedServicesList.add(currService);
				testArticle.getTestPlansExecutionOrder().add(currService.getName());
			}
		}
		return sortedServicesList;
	}

	/**
	 * Method to sort and filter Test Cases based on Test Configuration
	 * 
	 * @param servicesList
	 * @param testConfiguration
	 * @return filtered list of Services (Sort order as per Test Configuration)
	 */
	public static List<TestCase> filterTestCases(List<TestCase> testCasesList, TestArticle testArticle) {
		if (testArticle == null || testCasesList == null || testCasesList.isEmpty()) {
			return testCasesList;
		}

		List<TestCase> filteredTestCasesList = new ArrayList<>();

		for (TestCase testCase : testCasesList) {
			if (isTestToBeExecuted(testCase, testArticle)) {
				filteredTestCasesList.add(testCase);
			}
		}

		// Sorting Test cases in order as per Test Sequence
		Collections.sort(filteredTestCasesList, new Comparator<TestCase>() {
			@Override
			public int compare(TestCase test1, TestCase test2) {
				return Integer.valueOf(test1.getTestSequence()).compareTo(test2.getTestSequence());
			}
		});

		return filteredTestCasesList;

	}

	/**
	 * Method to sort Test Reports as per the Test Execution Order
	 * 
	 * @param testResults
	 * @return sorted List of Test Reports
	 */
	public static List<TestReport> sortTestReports(List<TestReport> testReportList) {
		if (testReportList == null || testReportList.isEmpty()) {
			return testReportList;
		}
		Collections.sort(testReportList, new Comparator<TestReport>() {
			@Override
			public int compare(TestReport testReport1, TestReport testReport2) {
				return Integer.valueOf(testReport1.getTest().getTestSequence()).compareTo(testReport2.getTest().getTestSequence());
			}
		});
		return testReportList;
	}

	/**
	 * Method to sort Test Plan Reports as per the Test Plan execution Order
	 * 
	 * @param testResults
	 * @return sorted List of Test Reports
	 */
	public static Map<Service, List<TestReport>> sortTestPlanReports(Map<Service, List<TestReport>> testReportList, List<String> testPlanExecutionOrder) {
		if (testReportList == null || testReportList.isEmpty() || testPlanExecutionOrder == null || testPlanExecutionOrder.isEmpty()) {
			return testReportList;
		}
		// Initialise new Map
		Map<Service, List<TestReport>> filteredReportList = new LinkedHashMap<>();

		for (String currServiceName : testPlanExecutionOrder) {
			for (Entry<Service, List<TestReport>> testReportListEntry : testReportList.entrySet()) {
				if (StringUtils.equals(testReportListEntry.getKey().getName(), currServiceName)) {
					filteredReportList.put(testReportListEntry.getKey(), testReportListEntry.getValue());
				}
			}
		}
		return filteredReportList;
	}

	/**
	 * Method to determine if the Test is to be executed as per the current configuration
	 * 
	 * @param instance of Test Case
	 * @return boolean indicating if the Test should be executed as per the current configuration
	 */
	public static boolean isTestToBeExecuted(TestCase testCase, TestArticle testArticle) {
		if (testArticle == null) {
			// Test Configuration has not been provided. Execute All Tests.
			return true;
		}
		// Precedence 0: Test Priority
		String currTestPriority = testCase.getTestPriority();
		if (testArticle.getTestPriorities() != null && !testArticle.getTestPriorities().isEmpty()) {
			return testArticle.getTestPriorities().contains(currTestPriority);
		}
		// Precedence 1: Test Name
		String currTestName = testCase.getName();
		if (testArticle.getTestNames() != null && !testArticle.getTestNames().isEmpty()) {
			return testArticle.getTestNames().contains(currTestName);
		}
		// Test Configuration contains no specification about the tests to be executed. Default flow: TRUE
		return true;
	}

	/**
	 * Method to execute the Identity Request of a Test Case
	 * 
	 * @param testArticle
	 * @param testCase
	 * @return Identity Response
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private static IdentityResponse executeIdentityRequest(TestArticle testArticle, TestCase testCase) throws URISyntaxException, IOException {

		IdentityManager currIdentityManager = null;
		IdentityResponse currIdentityServiceResponse = null;

		if (StringUtils.equals(testCase.getSecurityLevel(), SecurityLevel.AUTHENTICATED_APP_USER.getValue())) {

			// Fetch the configured Identity Service
			IdentityService currIdentityService = testArticle.getIdentityService(testCase.getIdentityService());
			if (currIdentityService == null) {
				LOG.error("Unrecognised Identity Service");
				return null;
			}

			// Map of Identity Services and their corresponding Identity Managers
			if (authenticatedAppUserIdentityManagerMap.containsKey(testArticle.getHostURL() + testCase.getIdentityService())) {
				// Identity Manager has already been intialised for the current Identity Provider
				currIdentityManager = authenticatedAppUserIdentityManagerMap.get(testArticle.getHostURL() + testCase.getIdentityService());
			} else {
				if (testCase.isCustomAuthTest()) {
					// Consider Custom Auth Request Body and Auth Request Headers
					currIdentityService.setRequestBody(testCase.getCustomAuthBody());
					currIdentityService.setRequestHeaders(testCase.getCustomAuthHeaders());
				}

				// Initialise an Identity Manager for the current Identity Provider
				currIdentityManager = new AuthenticatedAppUserIdentityManager(
						testArticle.getAuthServiceURL(),
						currIdentityService.getProviderName(),
						testArticle.getAppKey(),
						testArticle.getAppSecret(),
						currIdentityService.getRequestBody(),
						currIdentityService.getRequestHeaders(),
						currIdentityService.getAuthTokenValidityInSeconds());

				if (!testCase.isCustomAuthTest()) {
					// Add to map only for non-custom Authentication Payload
					authenticatedAppUserIdentityManagerMap.put(testArticle.getHostURL() + testCase.getIdentityService(), currIdentityManager);
				}
			}

			LOG.debug("Identity Service:" + testCase.getIdentityService() + " Is Custom Auth Payload:" + testCase.isCustomAuthTest() + " Auth Request Body:"
					+ currIdentityService.getRequestBody().toString() + " Auth Request Headers:" + JSONUtilities.stringify(testCase.getRequestHeaders()));

			// Execute Identity Request
			currIdentityServiceResponse = currIdentityManager.executeIdentityRequest();

		} else if (StringUtils.equals(testCase.getSecurityLevel(), SecurityLevel.ANONYMOUS_APP_USER.getValue())) {
			// Anonyoumous App User
			currIdentityManager = new AnonymousAppUserIdentityManager(testArticle.getAuthServiceURL(), testArticle.getAppKey(), testArticle.getAppSecret(), 1800);

			// Execute Identity Request
			currIdentityServiceResponse = currIdentityManager.executeIdentityRequest();
		}
		return currIdentityServiceResponse;
	}

}
