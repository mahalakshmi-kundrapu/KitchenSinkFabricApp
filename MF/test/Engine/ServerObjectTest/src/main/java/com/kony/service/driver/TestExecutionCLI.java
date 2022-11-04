package com.kony.service.driver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kony.service.dao.TestArticleDAO;
import com.kony.service.dao.impl.TestArticleDAOImpl;
import com.kony.service.documentation.APIDocumentGenerator;
import com.kony.service.dto.TestArticle;
import com.kony.service.dto.TestSummary;
import com.kony.service.messaging.EmailService;
import com.kony.service.messaging.IMessagingService;
import com.kony.service.test.executor.TestNGExecutor;
import com.kony.service.test.report.HTMLReportGenerator;
import com.kony.service.test.report.ReportGenerator;

/**
 * Main Class providing the Command-Line-Interface for the Test Executor
 *
 * @author Aditya Mankal
 */
public class TestExecutionCLI {

	private static final String PROP_TEST_ARTICLE_JSON_FILE_PATH = "test.testArticle";
	private static final String PROP_TEST_PLANS_DIRECTORY_PATH = "test.testPlansDirectory";
	private static final String PROP_RESULTS_RECIPIENT_LIST = "test.results.recipientList";
	private static final String PROP_VARIABLE_VALUES_PROPERTY_FILE_PATH = "test.variableValuesPropertyFilePath";
	private static final String PROP_RESULTS_DIRECTORY_PATH = "test.results.directoryPath";

	public static final String TEST_ARTICLE_JSON_PATH = System.getProperty(PROP_TEST_ARTICLE_JSON_FILE_PATH);
	public static final String TEST_PLANS_DIRECTORY_PATH = System.getProperty(PROP_TEST_PLANS_DIRECTORY_PATH);
	public static final String TEST_RESULTS_RECIPIENT_LIST = System.getProperty(PROP_RESULTS_RECIPIENT_LIST);
	public static final String TEST_VARIABLE_VALUES_PROPERTY_FILE_PATH = System
			.getProperty(PROP_VARIABLE_VALUES_PROPERTY_FILE_PATH);
	public static final String TEST_RESULTS_DIRECTORY_PATH = System.getProperty(PROP_RESULTS_DIRECTORY_PATH);

	private static final Logger LOG = LogManager.getLogger(TestExecutionCLI.class);

	public static void main(String[] args) {

		try {

			// Read Input Parameters
			LOG.debug("Property->" + PROP_RESULTS_DIRECTORY_PATH + ":" + TEST_RESULTS_DIRECTORY_PATH);
			LOG.debug("Property->" + PROP_RESULTS_RECIPIENT_LIST + ":" + TEST_RESULTS_RECIPIENT_LIST);
			LOG.debug("Property->" + PROP_TEST_PLANS_DIRECTORY_PATH + ":" + TEST_PLANS_DIRECTORY_PATH);
			LOG.debug("Property->" + PROP_TEST_ARTICLE_JSON_FILE_PATH + ":" + TEST_ARTICLE_JSON_PATH);
			LOG.debug("Property->" + PROP_VARIABLE_VALUES_PROPERTY_FILE_PATH + ":"
					+ TEST_VARIABLE_VALUES_PROPERTY_FILE_PATH);

			// Validate Test Article
			if (StringUtils.isBlank(TEST_ARTICLE_JSON_PATH)) {
				System.out.println("System property -D " + PROP_TEST_ARTICLE_JSON_FILE_PATH + " is a mandatory input.");
				System.exit(1);
			} else {
				File testArticleJSONFile = new File(TEST_ARTICLE_JSON_PATH);
				if (!testArticleJSONFile.exists() || testArticleJSONFile.isDirectory()
						|| !testArticleJSONFile.canRead()) {
					System.out.println("Cannot access Environment JSON File. Specified path:" + TEST_ARTICLE_JSON_PATH);
					System.exit(1);
				}
			}

			// Validate Test Plans Directory
			if (StringUtils.isBlank(TEST_PLANS_DIRECTORY_PATH)) {
				System.out.println("System property -D " + PROP_TEST_PLANS_DIRECTORY_PATH + " is a mandatory input.");
				System.exit(1);
			} else {
				File testPlansDirectoryFile = new File(TEST_PLANS_DIRECTORY_PATH);

				if (!testPlansDirectoryFile.exists() || !testPlansDirectoryFile.isDirectory()
						|| !testPlansDirectoryFile.canRead()) {
					System.out.println("Cannot access Test Plans Directory Directory. Specified path:"
							+ TEST_PLANS_DIRECTORY_PATH);
					System.exit(1);
				}
			}

			// Validate Results Directory
			if (StringUtils.isBlank(TEST_RESULTS_DIRECTORY_PATH)) {
				System.out.println("System property -D " + PROP_RESULTS_DIRECTORY_PATH + " is a mandatory input.");
				System.exit(1);
			} else {
				File testResultsDirectoryFile = new File(TEST_RESULTS_DIRECTORY_PATH);
				if (!testResultsDirectoryFile.exists()) {
					testResultsDirectoryFile.mkdirs();
				}
				if (!testResultsDirectoryFile.exists() || !testResultsDirectoryFile.isDirectory()
						|| !testResultsDirectoryFile.canRead() || !testResultsDirectoryFile.canWrite()) {
					System.out
							.println("Cannot access Results Directory. Specified path:" + TEST_RESULTS_DIRECTORY_PATH);
					System.exit(1);
				}
			}

			// Construct Test Article
			TestArticleDAO testArticleDAO = TestArticleDAOImpl.getInstance();
			TestArticle testArticle = testArticleDAO.getTestArticle();

			// Execute Test Article
			TestSummary testSummary = TestNGExecutor.executeTestSuite(testArticle, TEST_RESULTS_DIRECTORY_PATH);
			if (testSummary == null || testSummary.getTestResults() == null || testSummary.getTestResults().isEmpty()) {
				LOG.error("Invalid Test Suite. Test Summary is NULL. Test Suite Terminated.");
				System.exit(1);
			}

			// Export Results to File(s)
			ReportGenerator.exportResults(testSummary, TEST_RESULTS_DIRECTORY_PATH);

			// Export API Documentation
			APIDocumentGenerator.exportDocument(testSummary, TEST_RESULTS_DIRECTORY_PATH);

			// Parse recipients e-mail List
			List<String> resultsRecipientList = new ArrayList<String>();
			if (StringUtils.isNotBlank(TEST_RESULTS_RECIPIENT_LIST)) {
				if (TEST_RESULTS_RECIPIENT_LIST.contains(",")) {
					String resultRecpients[] = TEST_RESULTS_RECIPIENT_LIST.split(",");
					for (int index = 0; index < resultRecpients.length; index++) {
						resultsRecipientList.add(resultRecpients[index]);
					}
				} else {
					resultsRecipientList.add(TEST_RESULTS_RECIPIENT_LIST);
				}
			}

			// Send Result to recipients
			if (!resultsRecipientList.isEmpty()) {
				String fileName = "ServicesTestSummary_" + testSummary.getTestStartDateTime().toString() + ".html";
				String content = HTMLReportGenerator.getTestSummaryAsHTMLMessage(testSummary);
				IMessagingService messagingService = new EmailService();
				for (String currRecipient : resultsRecipientList) {
					messagingService.sendMessage(currRecipient, "Fabric Services Test Result", "Test Report Attached",
							MediaType.TEXT_PLAIN, content, fileName, MediaType.TEXT_HTML);
				}
			}

		} catch (Exception e) {
			LOG.error(e);
			e.printStackTrace();
		}
	}

}
