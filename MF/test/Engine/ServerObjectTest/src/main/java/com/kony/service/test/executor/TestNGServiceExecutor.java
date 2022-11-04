package com.kony.service.test.executor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.kony.service.constants.TestResult;
import com.kony.service.dao.impl.TestArticleDAOImpl;
import com.kony.service.dto.Service;
import com.kony.service.dto.TestArticle;
import com.kony.service.dto.TestCase;
import com.kony.service.dto.TestReport;
import com.kony.service.test.manager.TestManager;
import com.kony.service.test.manager.TestSummaryManager;
import com.kony.service.util.CommonUtlities;

/**
 * Class forming the TestNG Test Template for Test Cases on Services
 * 
 * @author Aditya Mankal
 *
 */
public class TestNGServiceExecutor implements ITest {

	private static final String LINE_SEPERATOR = System.lineSeparator();

	private static final Logger LOG = LogManager.getLogger(TestNGServiceExecutor.class);

	private Service service = null;
	private boolean isValidTestArticle = true;

	private static Date testStartDate = null;
	private static Date testCompletionDate = null;

	private List<TestReport> testReportList = null;
	private ThreadLocal<String> testName = new ThreadLocal<>();
	private static Map<Service, List<TestReport>> testResults = new LinkedHashMap<>();

	private static TestArticle testArticle = TestArticleDAOImpl.getInstance().getTestArticle();

	List<Service> services = TestManager.sortServices(testArticle.getServices(), testArticle);;

	/**
	 * Before Suite Method. Triggered before Test Suite Execution
	 */
	@BeforeSuite(alwaysRun = true)
	public void beforeSuite() {
		LOG.debug("In Before Suite");
		if (testArticle == null) {
			LOG.error("Invalid Test Article Instance. Cannot execute Tests");
			isValidTestArticle = false;
		}

		if (StringUtils.isBlank(testArticle.getHostURL())) {
			LOG.error("Invalid Environment Host URL. Cannot execute Tests");
			isValidTestArticle = false;
		}

		if (StringUtils.isBlank(testArticle.getAuthServiceURL())) {
			LOG.error("Invalid Environment Auth Service URL. Cannot execute Tests");
			isValidTestArticle = false;
		}

		if (isValidTestArticle == false) {
			return;
		}

		// Account Name of user Triggering the Tests
		String testTriggeredBy = getTestTriggeredBy();

		// Test Start Date Time
		testStartDate = new Date();

		LOG.debug("============================== Test Execution - START ==============================");
		LOG.debug("Test Triggered By:" + testTriggeredBy);
		LOG.debug("Test Start Time:" + testStartDate.toString());
		LOG.debug("Test Environment Host URL:" + testArticle.getHostURL());
		LOG.debug("Test Environment Auth Service URL:" + testArticle.getAuthServiceURL());
		LOG.debug("============================================================================" + LINE_SEPERATOR);
	}

	/**
	 * Data Provider method for TestNG Test
	 * 
	 * @param context
	 * @return 2D Object Array. Object[Service][TestCase]
	 */
	@DataProvider(name = "TestDataProvider")
	public Object[][] getTestData(ITestContext context) {

		if (service == null) {
			LOG.debug("In DataProvider. ERROR: Service Instance is NULL.");
			return new Object[0][0];
		}
		LOG.debug("In DataProvider. Service Name:" + service.getName());

		List<TestCase> testCases = service.getTests();
		int testCasesCount = testCases.size();

		Object[][] testData = new Object[testCasesCount][2];
		
		for (int index = 0; index < testCasesCount; index++) {
			testData[index][0] = service;
			testData[index][1] = testCases.get(index);
		}
		return testData;
	}

	@BeforeClass(alwaysRun = true)
	public void beforeClass(ITestContext context) {
		String serviceName = context.getName();

		LOG.debug("In Before Class. Service Name:" + serviceName);
		testReportList = new ArrayList<>();
		for (Service currService : services) {
			if (StringUtils.equals(currService.getName(), serviceName)) {
				service = currService;
				break;
			}
		}
		if (service == null) {
			LOG.debug("Skipping Test cases of Service'" + serviceName + "'. Possible Reasons: 1.Invalid Test Name 2.Service does match Test Configuration Filter Criteria.");
		} else {
			LOG.debug("Resolved Service with name'" + serviceName + "'.Proceeding to execute Tests...");
		}
	}

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod(Method method, Object[] testData) {
		if (isValidTestArticle == false) {
			throw new SkipException("Skipping Test due to Invalid Test Configuration...");
		}
		TestCase testCase = (TestCase) testData[1];
		TestManager.substitutePlaceHolderValues(testCase, testArticle.getVariableCollectionMap());
		testName.set(method.getName() + "_" + testData[0]);
		LOG.debug("In Before Method. Test Name:" + testCase.getName());
	}

	/**
	 * Executes a particular Test of a Service
	 * 
	 * @param test
	 * @param service
	 * @return Test Report
	 */
	@Test(dataProvider = "TestDataProvider")
	public void executeTest(Service service, TestCase test) {

		LOG.debug("In Test. Test Name:" + test.getName());
		Reporter.log("<br>");
		Reporter.log("<b>Service Name:</b>&nbsp" + service.getName());
		Reporter.log("<b>Request Method:</b>&nbsp" + service.getHttpMethod());
		Reporter.log("<b>Request URL:</b>&nbsp" + service.getUrl());
		Reporter.log("<br>");

		TestReport testReport = TestManager.executeTest(testArticle, service, test);
		testReportList.add(testReport);
		if (testReport.getResult() == TestResult.PASS) {
			// Passed Test Case
			Assert.assertTrue(true);
		} else if (testReport.getResult() == TestResult.SKIP) {
			// Skipped Test Case
			throw new SkipException("Skipping Test due to Invalid Test Configuration...");
		} else if (testReport.getResult() == TestResult.FAIL) {
			// Failed Test Case
			if (TestManager.getCountOfFailedAssertions(testReport.getAssertionResults()) > 0) {
				Assert.fail("Response Assertion(s) Failure. Check Assertions of Test Case.");
			} else {
				Assert.fail(testReport.getTestMessage());
			}
		} else {
			// Error in Test Execution
			Assert.fail(testReport.getTestMessage());
		}
	}

	@AfterMethod(alwaysRun = true)
	public void afterMethod(ITestContext context) {
		LOG.debug("In After Method");
	}

	@AfterClass(alwaysRun = true)
	public void afterClass(ITestContext context) {
		LOG.debug("In After Class. Service Name:" + context.getName());
		testReportList = TestManager.sortTestReports(testReportList);
		if (service != null && testReportList != null) {
			testResults.put(service, testReportList);
			LOG.debug("Saved Test Report of current Service");
		}
	}

	/**
	 * After Suite Method. Triggered after Test Suite Execution
	 */
	@AfterSuite(alwaysRun = true)
	public void afterSuite() {

		LOG.debug("In After Suite");
		// Account Name of user Triggering the Tests
		String testTriggeredBy = getTestTriggeredBy();

		// Test End Date Time
		testCompletionDate = new Date();

		LOG.debug("============================== Test Execution - END ==============================");
		LOG.debug("Test Triggered By:" + testTriggeredBy);
		LOG.debug("Test Trigger Time:" + testStartDate.toString());
		LOG.debug("Test Completion Time:" + testCompletionDate.toString());
		LOG.debug("Test Environment Host URL:" + testArticle.getHostURL());
		LOG.debug("Test Environment Auth Service URL:" + testArticle.getAuthServiceURL());
		LOG.debug("============================================================================" + LINE_SEPERATOR);

		TestSummaryManager.getTestSummary().setAppName(testArticle.getAppName());
		TestSummaryManager.getTestSummary().setAppVersion(testArticle.getAppVersion());
		TestSummaryManager.getTestSummary().setTestStartDateTime(testStartDate);
		TestSummaryManager.getTestSummary().setTestCompletionDateTime(testCompletionDate);
		TestSummaryManager.getTestSummary().setTestInititedBy(testTriggeredBy);
		testResults = TestManager.sortTestPlanReports(testResults, testArticle.getTestPlansExecutionOrder());
		TestSummaryManager.getTestSummary().setTestResults(testResults);
	}

	@Override
	public String getTestName() {
		return this.testName.get();
	}

	/**
	 * @return Name of User Triggering the Test based on Test Configuration.
	 *         If Name has not been given in the Test Configuration input, the method returns the name of the Logged In User
	 */
	private String getTestTriggeredBy() {
		// Account Name of user Triggering the Tests
		String testTriggeredBy = StringUtils.EMPTY;
		testTriggeredBy = testArticle.getTestTriggeredBy();
		if (StringUtils.isBlank(testTriggeredBy)) {
			testTriggeredBy = CommonUtlities.getLoggedInUsername();
		}
		return testTriggeredBy;
	}

}
