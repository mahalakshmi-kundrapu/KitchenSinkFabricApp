package com.kony.service.test.report;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.kony.service.dto.TestSummary;
import com.kony.service.util.DateUtilities;
import com.kony.service.util.FileUtilities;

/**
 * Class to export Test Summary to HTML File
 * 
 * @author Aditya Mankal
 *
 */
public class HTMLReportGenerator {

	private static final Logger LOG = LogManager.getLogger(HTMLReportGenerator.class);

	private static final String RESULTS_JSON_PLACE_HOLDER = "\"$$RESULT_JSON_CONTENT$$\"";
	private static final String RESULTS_HTML_TEMPLATE_PATH = "templates/TestReportTemplate.html";

	/**
	 * @param testSummary
	 * @param resultsDirectoryFilePointer
	 */
	public static void exportReport(TestSummary testSummary, File resultsDirectoryFilePointer) {
		try {
			String fileName = "ServicesTestSummary_" + DateUtilities.getDateAsString(testSummary.getTestStartDateTime(), ReportGenerator.REPORT_FILE_DATE_FORMAT) + ".html";
			String resultsFilePath = resultsDirectoryFilePointer.getAbsolutePath() + File.separator + fileName;

			File resultFilePointer = new File(resultsFilePath);
			if (resultFilePointer.exists()) {
				resultFilePointer.delete();
				LOG.error("File already exists. Deleted File.");
			}

			resultFilePointer.createNewFile();

			try {
				exportReportToFile(testSummary, resultFilePointer);
				LOG.debug("Report Exported to HTML File. Path" + resultFilePointer.getAbsolutePath());

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
	 * @return HTMLReportContent
	 * @throws IOException
	 */
	public static String getTestSummaryAsHTMLMessage(TestSummary testSummary) throws IOException {
		JSONObject servicesJSONObject = JSONReportGenerator.getTestSummaryAsJSONObject(testSummary);
		String templateHTMLContents = FileUtilities.readFileFromClassPathToString(RESULTS_HTML_TEMPLATE_PATH);
		return templateHTMLContents.replace(RESULTS_JSON_PLACE_HOLDER, servicesJSONObject.toString());
	}

	/**
	 * @param testSummary
	 * @param HTMLReportContent
	 * @throws IOException
	 */
	private static void exportReportToFile(TestSummary testSummary, File resultsFilePointer) throws IOException {

		String fileContents = getTestSummaryAsHTMLMessage(testSummary);
		String filePath = resultsFilePointer.getAbsolutePath();
		FileUtilities.writeStringToFile(fileContents, filePath);
	}

}
