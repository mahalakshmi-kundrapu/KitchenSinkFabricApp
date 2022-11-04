package com.kony.service.test.executor;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.FailurePolicy;
import org.testng.xml.XmlTest;

import com.kony.service.dto.Service;
import com.kony.service.dto.TestArticle;
import com.kony.service.dto.TestCase;
import com.kony.service.dto.TestSummary;
import com.kony.service.test.manager.TestManager;
import com.kony.service.test.manager.TestSummaryManager;

/**
 * Class for represent Test Cases as TestNG Tests and execute them. TestNG Test Template: {@link TestNGServiceExecutor}
 * 
 * @author Aditya Mankal
 *
 */
public class TestNGExecutor {

	private static final String PATH_SEPERATOR = System.getProperty("file.separator");

	private static final Logger LOG = LogManager.getLogger(TestNGExecutor.class);

	/**
	 * Method to prepare the TestNG Test Suite and execute it
	 * 
	 * @param testArticle
	 * @param testResultsDirectoryPath
	 * @return TestSummary
	 */
	public static TestSummary executeTestSuite(TestArticle testArticle, String testResultsDirectoryPath) {

		LOG.debug("Defining TestNG XML Suite...");

		// Prepare XML Suite
		XmlSuite xmlSuite = new XmlSuite();
		xmlSuite.setName("Server Object Test");
		xmlSuite.setConfigFailurePolicy(FailurePolicy.CONTINUE);
		xmlSuite.setPreserveOrder(true);

		List<Service> services = testArticle.getServices();

		// Filter services and set execution order as per the configuration
		services = TestManager.sortServices(testArticle.getServices(), testArticle);

		// Traverse List of Services
		for (Service service : services) {
			LOG.debug("Adding Service:" + service.getName());

			// Each Service is an XMLTest
			XmlTest xmlTest = new XmlTest();
			xmlTest.setSuite(xmlSuite);
			xmlTest.setName(service.getName());

			// Filter Test Cases and set execution order as per the configuration
			List<TestCase> testCases = service.getTests();
			testCases = TestManager.filterTestCases(testCases, testArticle);

			// Each Test Case is an XML Class
			List<XmlClass> xmlClasses = new ArrayList<>();
			for (TestCase testCase : service.getTests()) {
				LOG.debug(" Adding Testcase:" + testCase.getName());
				XmlClass xmlClass = new XmlClass();
				xmlClass.setClass(TestNGServiceExecutor.class);
				xmlClass.setName(testCase.getName());
				xmlClasses.add(xmlClass);
				LOG.debug(" Added Testcase:" + testCase.getName());
			}

			// Add XmlClasses(Test Cases) to XMLTest(Service i.e. TestPlan)
			xmlTest.setXmlClasses(xmlClasses);
			xmlSuite.addTest(xmlTest);

			LOG.debug("Added Service:" + service.getName());
		}

		LOG.debug("Services Traversed");

		List<XmlSuite> xmlSuites = new ArrayList<>();
		xmlSuites.add(xmlSuite);

		// Initiate TestNG TestRunner and Execute Tests
		LOG.debug("Initiating TestSuite Execution");
		TestNG testRunner = new TestNG();
		testRunner.setOutputDirectory(testResultsDirectoryPath + PATH_SEPERATOR + "TestNGReport");
		testRunner.setXmlSuites(xmlSuites);
		testRunner.run();
		LOG.debug("TestSuite Execution Complete");

		// Return TestSummary
		LOG.debug("Returning Test Summary");
		return TestSummaryManager.getTestSummary();
	}

}
