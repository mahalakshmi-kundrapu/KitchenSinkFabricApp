package com.kony.service.dao.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kony.service.dao.TestArticleDAO;
import com.kony.service.driver.TestExecutionCLI;
import com.kony.service.dto.Service;
import com.kony.service.dto.TestArticle;
import com.kony.service.util.FileUtilities;
import com.kony.service.util.JSONUtilities;

/**
 * DAO Implementation of TestArticle
 *
 * @author Aditya Mankal
 */
public class TestArticleDAOImpl implements TestArticleDAO {

	private static final Logger LOG = LogManager.getLogger(TestArticleDAOImpl.class);

	private static TestArticle testArticle;
	private static TestArticleDAOImpl testArticleDAOImpl;

	private TestArticleDAOImpl() {
		testArticle = initialiseTestArticle();
	}

	public static TestArticleDAOImpl getInstance() {
		if (testArticleDAOImpl == null) {
			testArticleDAOImpl = new TestArticleDAOImpl();
		}
		return testArticleDAOImpl;
	}

	@Override
	public TestArticle getTestArticle() {
		return testArticle;
	}

	private TestArticle initialiseTestArticle() {
		try {
			String testArticleFilePath = TestExecutionCLI.TEST_ARTICLE_JSON_PATH;
			if (StringUtils.isNotBlank(testArticleFilePath)) {

				// Read contents of Test Article JSON
				String testArticleFileContents = FileUtilities.readFileContentsToString(testArticleFilePath);
				TestArticle testArticle = JSONUtilities.parse(testArticleFileContents, TestArticle.class);

				// Add services to Test Article
				List<Service> services = ServiceDAOImpl.getInstance().getServices();
				testArticle.setServices(services);

				// Return instance of Test Article
				return testArticle;
			}
			return new TestArticle();
		} catch (Exception e) {
			LOG.error("Exception in de-serialising TestArticle. Exception", e);
			return null;
		}
	}
}
