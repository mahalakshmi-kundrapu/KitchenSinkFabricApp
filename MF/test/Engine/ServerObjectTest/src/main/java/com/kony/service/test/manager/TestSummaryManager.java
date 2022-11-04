package com.kony.service.test.manager;

import org.apache.commons.lang3.StringUtils;

import com.kony.service.dao.impl.TestArticleDAOImpl;
import com.kony.service.dto.TestArticle;
import com.kony.service.dto.TestSummary;
import com.kony.service.util.CommonUtlities;

/**
 * Class to hold the single instance of Test Summary
 * (Aggregation of all Test Results)
 * 
 * @author Aditya Mankal
 *
 */
public class TestSummaryManager {

	private static final TestSummary TEST_SUMMARY = initialiseTestSummary();

	private static TestSummary initialiseTestSummary() {
		TestArticle testArticle = TestArticleDAOImpl.getInstance().getTestArticle();

		TestSummary testSummary = new TestSummary();
		testSummary.setAppName(testArticle.getAppName());
		testSummary.setAppVersion(testArticle.getAppVersion());
		testSummary.setTestEnvironmentAuthServiceURL(testArticle.getAuthServiceURL());
		testSummary.setTestEnvironmentHostURL(testArticle.getHostURL());
		testSummary.setTestInititedBy(StringUtils.isNotBlank(testArticle.getTestTriggeredBy()) ? testArticle.getTestTriggeredBy() : CommonUtlities.getLoggedInUsername());

		return testSummary;
	}

	public static TestSummary getTestSummary() {
		return TEST_SUMMARY;
	}
}
