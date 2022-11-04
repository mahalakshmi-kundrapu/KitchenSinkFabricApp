package com.kony.service.test.report;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kony.service.dto.TestSummary;

/**
 * Class to export Test Summary to Text File, JSON File and HTML File
 * 
 * @author Aditya Mankal
 *
 */
public class ReportGenerator {

	private static final Logger LOG = LogManager.getLogger(ReportGenerator.class);
	public static final String REPORT_FILE_DATE_FORMAT = "MM-dd-yyyy_HH-mm-ss";

	public static void exportResults(TestSummary testSummary, String resultsDirectoryPath) {

		if (StringUtils.isBlank(resultsDirectoryPath)) {
			LOG.error("Invalid Directory Path. Path cannot be empty.");
		}

		File resultsDirectoryFilePointer = new File(resultsDirectoryPath);
		if (!resultsDirectoryFilePointer.exists() || !resultsDirectoryFilePointer.isDirectory() || !resultsDirectoryFilePointer.canRead()
				|| !resultsDirectoryFilePointer.canWrite()) {
			LOG.error("Unable to access the directory. Directory Path" + resultsDirectoryPath);
			return;
		}

		TextReportGenerator.exportReport(testSummary, resultsDirectoryFilePointer);
		JSONReportGenerator.exportReport(testSummary, resultsDirectoryFilePointer);
		HTMLReportGenerator.exportReport(testSummary, resultsDirectoryFilePointer);
	}

}
