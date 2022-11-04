package com.kony.service.test.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kony.service.constants.TestResult;
import com.kony.service.dto.Service;
import com.kony.service.dto.TestReport;
import com.kony.service.dto.TestSummary;
import com.kony.service.util.DateUtilities;
import com.kony.service.util.JSONUtilities;

/**
 * Class to export Test Summary to Text File
 * 
 * @author Aditya Mankal
 *
 */
class TextReportGenerator {

	private static final String LINE_SEPERATOR = System.lineSeparator();
	private static final Logger LOG = LogManager.getLogger(TextReportGenerator.class);

	public static void exportReport(TestSummary testSummary, File resultsDirectoryFilePointer) {
		try {
			String fileName = "ServicesTestSummary_" + DateUtilities.getDateAsString(testSummary.getTestStartDateTime(), ReportGenerator.REPORT_FILE_DATE_FORMAT) + ".txt";
			String resultsFilePath = resultsDirectoryFilePointer.getAbsolutePath() + File.separator + "Results_" + fileName;

			File resultFilePointer = new File(resultsFilePath);
			if (resultFilePointer.exists()) {
				resultFilePointer.delete();
				LOG.debug("File already exists. Deleted File.");
			}

			try (FileWriter fileWriter = new FileWriter(resultFilePointer, true)) {
				addTestSummaryMetaToFile(testSummary, fileWriter);
				addTestResultsToFile(testSummary, fileWriter);
				LOG.debug("Report Exported to Text File. Path" + resultFilePointer.getAbsolutePath());

			} catch (Exception e) {
				LOG.error("Exception while opening File Writer. Exception:", e);
			}

		} catch (Exception e) {
			LOG.error("Exception in writing result to File.");
			LOG.error("Exception: ", e);
		}
	}

	private static void addTestResultsToFile(TestSummary testSummary, FileWriter fileWriter) throws IOException {

		Map<Service, List<TestReport>> testReport = testSummary.getTestResults();
		Set<Entry<Service, List<TestReport>>> testResultsEntrySet = testReport.entrySet();

		Service currService;
		List<TestReport> currTestResultsList;

		for (Entry<Service, List<TestReport>> entry : testResultsEntrySet) {
			currService = entry.getKey();
			addServiceMetaToFile(currService, fileWriter);

			currTestResultsList = entry.getValue();
			for (TestReport testResult : currTestResultsList) {
				addTestResultMetaToFile(testResult, fileWriter);
			}

			addServiceSummaryToFile(currService, currTestResultsList, fileWriter);
		}

	}

	private static void addTestResultMetaToFile(TestReport testResult, FileWriter fileWriter) throws IOException {

		fileWriter.append(LINE_SEPERATOR);
		fileWriter.append(LINE_SEPERATOR + "------------ Test ------------");
		fileWriter.append(LINE_SEPERATOR + "Test Case:");
		fileWriter.append(LINE_SEPERATOR + "		Name:" + testResult.getTest().getName());
		fileWriter.append(LINE_SEPERATOR + "		Request Body:" + testResult.getTest().getRequestBody());
		fileWriter.append(LINE_SEPERATOR + "		Response Body Assertions:" + JSONUtilities.getCollectionAsJSONArray(testResult.getTest().getResponseBodyAssertions()))
				.toString();
		fileWriter.append(LINE_SEPERATOR + "		Request Headers:" + testResult.getTest().getRequestHeaders());
		fileWriter.append(LINE_SEPERATOR + "		Response Headers Assertions:" + JSONUtilities.getCollectionAsJSONArray(testResult.getTest().getResponseHeaderAssertions()))
				.toString();

		fileWriter.append(LINE_SEPERATOR + "		Test Priority:" + testResult.getTest().getTestPriority());
		fileWriter.append(LINE_SEPERATOR + "		Test Identity Service Provider:" + testResult.getTest().getIdentityService());

		fileWriter.append(LINE_SEPERATOR);
		fileWriter.append(LINE_SEPERATOR + "Test Result:");
		fileWriter.append(LINE_SEPERATOR + "		HTTP Status Code:" + testResult.getHttpStatusCode());
		fileWriter.append(LINE_SEPERATOR + "		Operation Status Code:" + testResult.getStatusCode());
		fileWriter.append(LINE_SEPERATOR + "		Duration(ms):" + testResult.getDuration());
		fileWriter.append(LINE_SEPERATOR + "		Response Body:" + testResult.getResponseBody());
		fileWriter.append(LINE_SEPERATOR + "		Response Headers:" + testResult.getResponseHeaders());
		fileWriter.append(LINE_SEPERATOR + "		Response Length:(Bytes)" + testResult.getContentLength());
		fileWriter.append(LINE_SEPERATOR + "		Test Result:" + testResult.getResult());
		fileWriter.append(LINE_SEPERATOR + "--------------------------------");

	}

	private static void addServiceMetaToFile(Service service, FileWriter fileWriter) throws IOException {
		fileWriter.append(LINE_SEPERATOR);
		fileWriter.append(LINE_SEPERATOR + "========== Service ==========");
		fileWriter.append(LINE_SEPERATOR + "Service Name:" + service.getName());
		fileWriter.append(LINE_SEPERATOR + "Service URL:" + service.getUrl());
		fileWriter.append(LINE_SEPERATOR + "Service HTTP Method:" + service.getHttpMethod().toUpperCase());
		fileWriter.append(LINE_SEPERATOR + "Total Test Cases:" + service.getTests().size());
	}

	private static void addServiceSummaryToFile(Service service, List<TestReport> testResults, FileWriter fileWriter) throws IOException {

		double countOfPassedTestCases = 0, countOfFailedTestCases = 0, countofErrorTestCases = 0, countOfSkippedTestCases = 0;

		for (TestReport currTestResult : testResults) {
			if (TestResult.PASS == currTestResult.getResult()) {
				countOfPassedTestCases++;
			} else if (TestResult.FAIL == currTestResult.getResult()) {
				countOfFailedTestCases++;
			} else if (TestResult.ERROR == currTestResult.getResult()) {
				countofErrorTestCases++;
			} else if (TestResult.SKIP == currTestResult.getResult()) {
				countOfSkippedTestCases++;
			}
		}
		double totalCountOfTestCases = countOfPassedTestCases + countOfFailedTestCases;

		double passPercentage = (countOfPassedTestCases / totalCountOfTestCases) * 100;
		double failPercentage = 100 - passPercentage;

		fileWriter.append(LINE_SEPERATOR);
		fileWriter.append(LINE_SEPERATOR + "========== Service Summary ==========");

		fileWriter.append(LINE_SEPERATOR + " Total Count of Passed Test Cases:" + countOfPassedTestCases);
		fileWriter.append(LINE_SEPERATOR + " Total Count of Failed Test Cases:" + countOfFailedTestCases);
		fileWriter.append(LINE_SEPERATOR + " Total Count of Error Test Cases:" + countofErrorTestCases);
		fileWriter.append(LINE_SEPERATOR + " Total Count of Skipped Test Cases:" + countOfSkippedTestCases);
		fileWriter.append(LINE_SEPERATOR + " Pass percentage of Test Cases:" + passPercentage + "%");
		fileWriter.append(LINE_SEPERATOR + " Fail percentage of Test Cases:" + failPercentage + "%");
		fileWriter.append(LINE_SEPERATOR + " Total Count of Test Test Cases:" + totalCountOfTestCases);
		fileWriter.append(LINE_SEPERATOR + "=====================================");
		fileWriter.append(LINE_SEPERATOR);
		fileWriter.append(LINE_SEPERATOR);
		fileWriter.append(LINE_SEPERATOR);
	}

	private static void addTestSummaryMetaToFile(TestSummary testSummary, FileWriter fileWriter) throws IOException {

		fileWriter.append(LINE_SEPERATOR + "============================== Test Summary ==============================");
		fileWriter.append(LINE_SEPERATOR + "Test Triggered By:" + testSummary.getTestInititedBy());
		fileWriter.append(LINE_SEPERATOR + "Test Trigger Time:" + testSummary.getTestStartDateTime().toString());
		fileWriter.append(LINE_SEPERATOR + "Test Completion Time:" + testSummary.getTestCompletionDateTime().toString());
		fileWriter.append(LINE_SEPERATOR + "Test Duration(ms):" + testSummary.getDuration());
		fileWriter.append(LINE_SEPERATOR + "Test Environment Host URL:" + testSummary.getTestEnvironmentHostURL());
		fileWriter.append(LINE_SEPERATOR + "Test Environment Auth Service URL:" + testSummary.getTestEnvironmentAuthServiceURL());
		fileWriter.append(LINE_SEPERATOR + "==========================================================================");
		fileWriter.append(LINE_SEPERATOR);

	}

}
