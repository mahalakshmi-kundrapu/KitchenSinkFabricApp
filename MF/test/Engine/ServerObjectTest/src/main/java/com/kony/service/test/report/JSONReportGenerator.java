package com.kony.service.test.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.kony.service.constants.IdentityConstants;
import com.kony.service.constants.TestResult;
import com.kony.service.dto.Service;
import com.kony.service.dto.TestReport;
import com.kony.service.dto.TestSummary;
import com.kony.service.util.DateUtilities;
import com.kony.service.util.FileUtilities;
import com.kony.service.util.JSONUtilities;

/**
 * Class to export Test Summary to JSON File
 * 
 * @author Aditya Mankal
 *
 */
class JSONReportGenerator {

	private static final Logger LOG = LogManager.getLogger(JSONReportGenerator.class);

	private static final String TESTS_ROOT_ELEMENT_KEY = "tests";
	private static final String TEST_PLANS_EXECUTION_ORDER_KEY = "testPlanExecutionOrder";

	private static final String META_KEY = "suiteMeta";

	private static final String APP_NAME_KEY = "appName";
	private static final String APP_VERSION_KEY = "appVersion";

	private static final String SERVICES_ROOT_ELEMENT_KEY = "services";

	private static final String SERVICE_URL_KEY = "serviceURL";
	private static final String SERVICE_NAME_KEY = "serviceName";
	private static final String MODULE_NAME_KEY = "moduleName";
	private static final String SERVICE_HTTP_MEHTOD_KEY = "serviceHTTPMethod";
	private static final String SERVICE_DESCRIPTION_KEY = "serviceDescription";
	private static final String SERVICE_INPUT_FIELDS_DESC_KEY = "inputFieldsDocument";
	private static final String SERVICE_OUTPUT_FIELDS_DESC_KEY = "outputFieldsDocument";

	private static final String TEST_MESSAGE_KEY = "validationMessage";
	private static final String TEST_STATUS_KEY = "testStatus";
	private static final String TEST_PRIORITY_KEY = "testPriority";
	private static final String TEST_NAME_KEY = "testName";
	private static final String TEST_STATUS_CODE_KEY = "statusCode";
	private static final String TEST_RESPONSE_TIME_KEY = "responseTime";

	private static final String TEST_REQUEST_BODY_KEY = "requestBody";
	private static final String TEST_RESPONSE_BODY_KEY = "responseBody";
	private static final String TEST_REQUEST_HEADERS_KEY = "requestHeaders";
	private static final String TEST_RESPONSE_HEADERS_KEY = "responseHeaders";
	private static final String TEST_RESPONSE_BODY_ASSERTIONS_KEY = "responseBodyAssertions";
	private static final String TEST_RESPONSE_HEADERS_ASSERTIONS_KEY = "responseHeaderAssertions";

	private static final String TOTAL_TEST_CASES_COUNT_KEY = "totalCases";
	private static final String PASSED_TEST_CASES_COUNT_KEY = "passedCases";
	private static final String FAILED_TEST_CASES_COUNT_KEY = "failedCases";
	private static final String ERROR_TEST_CASES_COUNT_KEY = "errorCases";
	private static final String SKIPPED_TEST_CASES_COUNT_KEY = "skippedCases";
	private static final String PASS_PERCENTAGE_KEY = "passPercentage";

	private static final String TEST_SUITE_TRIGGERED_BY_KEY = "testSuiteTriggeredBy";
	private static final String TEST_SUITE_DURATION_KEY = "testDuration";
	private static final String TEST_SUITE_HOST_ENVIRONMENT_URL_KEY = "hostURL";
	private static final String TEST_SUITE_AUTH_SERVICE_URL_KEY = "authServiceURL";
	private static final String TEST_SUITE_START_DATE_TIME_KEY = "testStartDateTime";
	private static final String TEST_SUITE_COMPLETION_DATE_TIME_KEY = "testCompletionDateTime";

	/**
	 * @param testSummary
	 * @param resultsDirectoryFilePointer
	 */
	public static void exportReport(TestSummary testSummary, File resultsDirectoryFilePointer) {
		try {
			String fileName = "ServicesTestSummary_" + DateUtilities.getDateAsString(testSummary.getTestStartDateTime(), ReportGenerator.REPORT_FILE_DATE_FORMAT) + ".json";
			String resultsFilePath = resultsDirectoryFilePointer.getAbsolutePath() + File.separator + fileName;

			File resultFilePointer = new File(resultsFilePath);
			if (resultFilePointer.exists()) {
				resultFilePointer.delete();
				LOG.error("File already exists. Deleted File.");
			}

			resultFilePointer.createNewFile();

			try {
				exportReportToFile(testSummary, resultFilePointer);
				LOG.debug("Report Exported to JSON File. Path:" + resultFilePointer.getAbsolutePath());

			} catch (Exception e) {
				LOG.error("Exception while opening File Writer. Exception:", e);
			}

		} catch (Exception e) {
			LOG.error("Exception in writing result to File.");
			LOG.error("Exception: ", e);
		}
	}

	/**
	 * @param testSummary
	 * @param resultsFilePointer
	 * @throws IOException
	 */
	private static void exportReportToFile(TestSummary testSummary, File resultsFilePointer) throws IOException {
		JSONObject servicesJSONObject = getTestSummaryAsJSONObject(testSummary);
		String fileContents = servicesJSONObject.toString();
		FileUtilities.writeStringToFile(fileContents, resultsFilePointer.getAbsolutePath());
	}

	public static JSONObject getTestMetaAsJSONObject(TestSummary testSummary) throws JSONException, IOException {
		JSONObject testSummaryJSONObject = new JSONObject();

		testSummaryJSONObject.put(TEST_SUITE_DURATION_KEY, testSummary.getDuration());
		testSummaryJSONObject.put(TEST_SUITE_START_DATE_TIME_KEY, testSummary.getTestStartDateTime().toString());
		testSummaryJSONObject.put(TEST_SUITE_COMPLETION_DATE_TIME_KEY, testSummary.getTestCompletionDateTime().toString());
		testSummaryJSONObject.put(TEST_SUITE_HOST_ENVIRONMENT_URL_KEY, testSummary.getTestEnvironmentHostURL());
		testSummaryJSONObject.put(TEST_SUITE_AUTH_SERVICE_URL_KEY, testSummary.getTestEnvironmentAuthServiceURL());
		testSummaryJSONObject.put(TEST_SUITE_TRIGGERED_BY_KEY, testSummary.getTestInititedBy());

		testSummaryJSONObject.put(APP_NAME_KEY, testSummary.getAppName());
		testSummaryJSONObject.put(APP_VERSION_KEY, testSummary.getAppVersion());

		Map<Service, List<TestReport>> testResultsMap = testSummary.getTestResults();
		List<TestReport> currServiceTestReportsList = null;
		double countOfPassedTestCases = 0;
		double countOfFailedTestCases = 0;
		double countOfSkippedTestCases = 0;
		double countOfErrorTestCases = 0;
		double countOfTotalTestCases = 0;
		List<String> testPlanExecutionOrder = new ArrayList<>();

		for (Entry<Service, List<TestReport>> currEntry : testResultsMap.entrySet()) {
			testPlanExecutionOrder.add(currEntry.getKey().getName());

			currServiceTestReportsList = currEntry.getValue();
			for (TestReport currTestResult : currServiceTestReportsList) {

				if (TestResult.PASS == currTestResult.getResult()) {
					countOfPassedTestCases++;
				} else if (TestResult.FAIL == currTestResult.getResult()) {
					countOfFailedTestCases++;
				} else if (TestResult.ERROR == currTestResult.getResult()) {
					countOfErrorTestCases++;
				} else if (TestResult.SKIP == currTestResult.getResult()) {
					countOfSkippedTestCases++;
				}
			}
		}
		countOfTotalTestCases = countOfPassedTestCases + countOfFailedTestCases + countOfSkippedTestCases + countOfErrorTestCases;
		double passPercentage = 0;
		if (countOfTotalTestCases != 0) {
			passPercentage = (countOfPassedTestCases / countOfTotalTestCases) * 100;
			passPercentage = (double) Math.round(passPercentage * 100d) / 100d;
		}
		testSummaryJSONObject.put(PASS_PERCENTAGE_KEY, passPercentage);
		testSummaryJSONObject.put(TOTAL_TEST_CASES_COUNT_KEY, (int) countOfTotalTestCases);
		testSummaryJSONObject.put(PASSED_TEST_CASES_COUNT_KEY, (int) countOfPassedTestCases);
		testSummaryJSONObject.put(FAILED_TEST_CASES_COUNT_KEY, (int) countOfFailedTestCases);
		testSummaryJSONObject.put(ERROR_TEST_CASES_COUNT_KEY, (int) countOfErrorTestCases);
		testSummaryJSONObject.put(SKIPPED_TEST_CASES_COUNT_KEY, (int) countOfSkippedTestCases);
		testSummaryJSONObject.put(TEST_PLANS_EXECUTION_ORDER_KEY, JSONUtilities.getCollectionAsJSONArray(testPlanExecutionOrder));
		return testSummaryJSONObject;
	}

	/**
	 * @param testSummary
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject getTestSummaryAsJSONObject(TestSummary testSummary) throws JSONException, IOException {

		Service currService;
		String currServiceName;
		String currTestName;

		List<TestReport> currServiceTestsResults;

		Map<Service, List<TestReport>> testResults = testSummary.getTestResults();
		Set<Entry<Service, List<TestReport>>> testResultsEntrySet = testResults.entrySet();

		JSONObject resultJSONObject = new JSONObject();

		JSONObject servicesJSONObject = new JSONObject();
		resultJSONObject.put(SERVICES_ROOT_ELEMENT_KEY, servicesJSONObject);

		int currCountOfPassedTestCases = 0, currCountOfFailedTestCases = 0, currCountOfErrorTestCases = 0, currCountOfSkippedTestCases = 0;

		for (Entry<Service, List<TestReport>> currEntry : testResultsEntrySet) {

			currCountOfPassedTestCases = 0;
			currCountOfFailedTestCases = 0;
			currCountOfErrorTestCases = 0;
			currCountOfSkippedTestCases = 0;

			currService = currEntry.getKey();
			currServiceTestsResults = currEntry.getValue();
			currServiceName = currService.getName();

			JSONObject currServiceJSONObject = new JSONObject();
			currServiceJSONObject.put(SERVICE_NAME_KEY, currService.getName());
			currServiceJSONObject.put(MODULE_NAME_KEY, currService.getModuleName());
			currServiceJSONObject.put(SERVICE_URL_KEY, currService.getUrl());
			currServiceJSONObject.put(SERVICE_HTTP_MEHTOD_KEY, StringUtils.upperCase(currService.getHttpMethod()));
			currServiceJSONObject.put(SERVICE_DESCRIPTION_KEY, currService.getDescription());
			if (currService.getInputFieldsDocument() != null && !currService.getInputFieldsDocument().isEmpty()) {
				currServiceJSONObject.put(SERVICE_INPUT_FIELDS_DESC_KEY, JSONUtilities.getCollectionAsJSONArray(currService.getInputFieldsDocument()));
			}
			if (currService.getOutputFieldsDocument() != null && !currService.getOutputFieldsDocument().isEmpty()) {
				currServiceJSONObject.put(SERVICE_OUTPUT_FIELDS_DESC_KEY, JSONUtilities.getCollectionAsJSONArray(currService.getOutputFieldsDocument()));
			}

			// Prepare Collection of Tests of Current Service
			JSONObject currServiceTestsJSONObject = new JSONObject();
			for (TestReport currTestResult : currServiceTestsResults) {
				currTestName = currTestResult.getTest().getName();
				JSONObject currTestJSONObject = new JSONObject();
				currTestJSONObject.put(TEST_MESSAGE_KEY, JSONUtilities.stringify(currTestResult.getAssertionResults()));
				currTestJSONObject.put(TEST_STATUS_KEY, currTestResult.getResult());
				currTestJSONObject.put(TEST_PRIORITY_KEY, currTestResult.getTest().getTestPriority());
				currTestJSONObject.put(TEST_NAME_KEY, currTestResult.getTest().getName());

				currTestJSONObject.put(TEST_STATUS_CODE_KEY, currTestResult.getStatusCode());

				currTestJSONObject.put(TEST_RESPONSE_TIME_KEY, currTestResult.getDuration());

				currTestJSONObject.put(TEST_REQUEST_BODY_KEY, currTestResult.getTest().getRequestBody());
				currTestJSONObject.put(TEST_REQUEST_HEADERS_KEY, JSONUtilities.getMapAsJSONObject(trimAuthTokenInMap(currTestResult.getTest().getRequestHeaders())));

				currTestJSONObject.put(TEST_RESPONSE_BODY_KEY, currTestResult.getResponseBody());
				currTestJSONObject.put(TEST_RESPONSE_BODY_ASSERTIONS_KEY, JSONUtilities.getCollectionAsJSONArray(currTestResult.getTest().getResponseBodyAssertions()));

				currTestJSONObject.put(TEST_RESPONSE_HEADERS_KEY, JSONUtilities.getMapAsJSONObject(currTestResult.getResponseHeaders()));
				currTestJSONObject.put(TEST_RESPONSE_HEADERS_ASSERTIONS_KEY, JSONUtilities.getCollectionAsJSONArray(currTestResult.getTest().getResponseHeaderAssertions()));

				currServiceTestsJSONObject.put(currTestName, currTestJSONObject);

				if (TestResult.PASS == currTestResult.getResult()) {
					currCountOfPassedTestCases++;
				} else if (TestResult.FAIL == currTestResult.getResult()) {
					currCountOfFailedTestCases++;
				} else if (TestResult.ERROR == currTestResult.getResult()) {
					currCountOfErrorTestCases++;
				} else if (TestResult.SKIP == currTestResult.getResult()) {
					currCountOfSkippedTestCases++;
				}
			}

			currServiceJSONObject.put(PASSED_TEST_CASES_COUNT_KEY, currCountOfPassedTestCases);
			currServiceJSONObject.put(FAILED_TEST_CASES_COUNT_KEY, currCountOfFailedTestCases);
			currServiceJSONObject.put(ERROR_TEST_CASES_COUNT_KEY, currCountOfErrorTestCases);
			currServiceJSONObject.put(SKIPPED_TEST_CASES_COUNT_KEY, currCountOfSkippedTestCases);

			currServiceJSONObject.put(TESTS_ROOT_ELEMENT_KEY, currServiceTestsJSONObject);
			servicesJSONObject.put(currServiceName, currServiceJSONObject);
		}

		JSONObject suiteMetaJSONObject = getTestMetaAsJSONObject(testSummary);
		resultJSONObject.put(META_KEY, suiteMetaJSONObject);

		return resultJSONObject;
	}

	/**
	 * Method to mask the X-Kony Auth Token value in the Map with the string "X-Kony-Auth-Token"
	 * 
	 * @param map
	 * @return Map without the X-Kony Auth Token
	 */
	private static Map<String, String> trimAuthTokenInMap(Map<String, String> map) {
		if (map == null || !map.containsKey(IdentityConstants.X_KONY_AUTHORIZATION)) {
			return map;
		}
		String authToken = map.get(IdentityConstants.X_KONY_AUTHORIZATION);

		if (StringUtils.isNotBlank(authToken) && StringUtils.length(authToken) > 6) {
			authToken = authToken.substring(0, 5) + "..." + authToken.substring(authToken.length() - 5);
		}
		map.put(IdentityConstants.X_KONY_AUTHORIZATION, authToken);
		return map;
	}
}
