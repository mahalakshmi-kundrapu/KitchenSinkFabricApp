package com.kony.service.documentation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.kony.service.dto.Service;
import com.kony.service.dto.TestReport;
import com.kony.service.dto.TestSummary;
import com.kony.service.test.report.ReportGenerator;
import com.kony.service.util.CommonUtlities;
import com.kony.service.util.DateUtilities;
import com.kony.service.util.FileUtilities;
import com.kony.service.util.JSONUtilities;
import com.kony.service.util.RandomValueUtilities;

/**
 * Class to export Test Summary to HTML File
 * 
 * @author Aditya Mankal
 *
 */
public class APIDocumentGenerator {

	private static final Logger LOG = LogManager.getLogger(APIDocumentGenerator.class);

	private static final String ID_KEY = "id";

	private static final String HOST_URL_KEY = "hostURL";
	private static final String APP_NAME_KEY = "appName";
	private static final String APP_VERSION_KEY = "appVersion";
	private static final String GENERATED_BY_KEY = "generatedBy";
	private static final String AUTH_SERVICE_URL_KEY = "hostURL";
	private static final String GENERATION_TIME_KEY = "generationTime";

	private static final String SERVICES_KEY = "services";
	private static final String SCENARIOS_KEY = "scenarios";
	private static final String MODULE_NAME_KEY = "moduleName";
	private static final String SERVICE_URL_KEY = "serviceURL";
	private static final String SERVICE_NAME_KEY = "serviceName";
	private static final String SCENARIO_NAME_KEY = "scenarioName";
	private static final String REQUEST_BODY_KEY = "requestBody";
	private static final String RESPONSE_BODY_KEY = "responseBody";
	private static final String REQUEST_HEADERS_KEY = "requestHeaders";
	private static final String RESPONSE_HEADERS_KEY = "responseHeaders";
	private static final String IDENTITY_PROVIDER_KEY = "identityProvider";
	private static final String SERVICE_HTTP_METHOD_KEY = "serviceHTTPMethod";
	private static final String SERVICE_DESCRIPTION_KEY = "serviceDescription";
	private static final String SERVICE_INPUT_FIELDS_DESCRIPTION_KEY = "inputFieldsDocument";
	private static final String SERVICE_OUTPUT_FIELDS_DESCRIPTION_KEY = "outputFieldsDocument";

	private static final String DOCUMENT_JSON_PLACE_HOLDER = "\"$$DOCUMENT_JSON_CONTENT$$\"";
	private static final String API_DOCUMENT_TEMPLATE_PATH = "templates/APIDocumentTemplate.html";

	public static void exportDocument(TestSummary testSummary, String resultsDirectoryPath) {
		try {

			File resultsDirectoryFilePointer = new File(resultsDirectoryPath);
			String fileName = "APIDocument_" + DateUtilities.getDateAsString(testSummary.getTestStartDateTime(), ReportGenerator.REPORT_FILE_DATE_FORMAT) + ".html";
			String resultsFilePath = resultsDirectoryFilePointer.getAbsolutePath() + File.separator + fileName;

			File resultFilePointer = new File(resultsFilePath);
			if (resultFilePointer.exists()) {
				resultFilePointer.delete();
				LOG.error("File already exists. Deleted File.");
			}

			resultFilePointer.createNewFile();

			try {
				exportDocumentToFile(testSummary, resultFilePointer);
				LOG.debug("Document Exported to HTML File. Path" + resultFilePointer.getAbsolutePath());

			} catch (Exception e) {
				LOG.error("Exception while opening File Writer. Exception:", e);
			}

		} catch (Exception e) {
			LOG.error("Exception in writing result to File.");
			LOG.error("Exception: ", e);
		}
	}

	private static String getAPIDocumentationAsHTMLContent(TestSummary testSummary) throws IOException {

		JSONObject apiDocumentJSON = getAPIDocumentJSON(testSummary);
		String templateHTMLContents = FileUtilities.readFileFromClassPathToString(API_DOCUMENT_TEMPLATE_PATH);
		return templateHTMLContents.replace(DOCUMENT_JSON_PLACE_HOLDER, apiDocumentJSON.toString());
	}

	private static final String MODULES_KEY = "modules";
	private static final String DOC_META_KEY = "docMeta";

	private static JSONObject getAPIDocumentJSON(TestSummary testSummary) throws JSONException, IOException {
		JSONObject apiDocumentJSON = new JSONObject();

		JSONObject documentationMetaJSON = getDocumentationMeta(testSummary);
		apiDocumentJSON.put(DOC_META_KEY, documentationMetaJSON);

		JSONObject modulesJSON = getModulesDocument(testSummary);
		apiDocumentJSON.put(MODULES_KEY, modulesJSON);

		return apiDocumentJSON;
	}

	private static JSONObject getDocumentationMeta(TestSummary testSummary) {
		JSONObject documentMetaJSON = new JSONObject();
		documentMetaJSON.put(APP_NAME_KEY, testSummary.getAppName());
		documentMetaJSON.put(HOST_URL_KEY, testSummary.getTestEnvironmentHostURL());
		documentMetaJSON.put(AUTH_SERVICE_URL_KEY, testSummary.getTestEnvironmentAuthServiceURL());
		documentMetaJSON.put(APP_VERSION_KEY, testSummary.getAppVersion());
		documentMetaJSON.put(GENERATED_BY_KEY, testSummary.getTestInititedBy());
		documentMetaJSON.put(GENERATION_TIME_KEY, testSummary.getTestCompletionDateTime());
		return documentMetaJSON;
	}

	private static JSONObject getModulesDocument(TestSummary testSummary) throws JSONException, IOException {

		Service currService = null;
		JSONObject currModuleJSON = null;
		JSONObject currServiceJSON = null;
		JSONObject currServiceScenarioJSON = null;
		List<TestReport> currentReportList = null;
		String currModuleName = StringUtils.EMPTY;
		Map<String, JSONObject> moduleJSONMap = new HashMap<>();

		Map<Service, List<TestReport>> servicesReportList = testSummary.getTestResults();

		// Traverse the List of Services and their corresponding Service Reports
		for (Entry<Service, List<TestReport>> serviceReport : servicesReportList.entrySet()) {

			currService = serviceReport.getKey();
			currentReportList = serviceReport.getValue();
			currModuleName = StringUtils.trim(currService.getModuleName());

			// Fetch the JSONObject of the current Module
			if (!moduleJSONMap.containsKey(currModuleName)) {
				currModuleJSON = new JSONObject();
				currModuleJSON.put(ID_KEY, RandomValueUtilities.getNumericId());
				currModuleJSON.put(MODULE_NAME_KEY, currModuleName);
				currModuleJSON.put(SERVICES_KEY, new JSONObject());
				moduleJSONMap.put(currModuleName, currModuleJSON);
			}
			currModuleJSON = moduleJSONMap.get(currModuleName);

			// Prepare JSONObject of current Service
			currServiceJSON = new JSONObject();
			currServiceJSON.put(ID_KEY, RandomValueUtilities.getNumericId());
			currServiceJSON.put(SERVICE_NAME_KEY, currService.getName());
			currServiceJSON.put(SERVICE_HTTP_METHOD_KEY, currService.getHttpMethod());
			currServiceJSON.put(SERVICE_URL_KEY, currService.getUrl());
			currServiceJSON.put(SERVICE_DESCRIPTION_KEY, currService.getDescription());
			if (currService.getInputFieldsDocument() != null && !currService.getInputFieldsDocument().isEmpty()) {
				currServiceJSON.put(SERVICE_INPUT_FIELDS_DESCRIPTION_KEY, JSONUtilities.getCollectionAsJSONArray(currService.getInputFieldsDocument()));
			}
			if (currService.getOutputFieldsDocument() != null && !currService.getOutputFieldsDocument().isEmpty()) {
				currServiceJSON.put(SERVICE_OUTPUT_FIELDS_DESCRIPTION_KEY, JSONUtilities.getCollectionAsJSONArray(currService.getOutputFieldsDocument()));
			}

			// Prepare the Scenarios JSONObject of the current Service
			currServiceScenarioJSON = new JSONObject();

			for (TestReport currentTestReport : currentReportList) {
				JSONObject currentScenarioJSON = new JSONObject();

				currentScenarioJSON.put(SCENARIO_NAME_KEY, currentTestReport.getTest().getName());
				currentScenarioJSON.put(IDENTITY_PROVIDER_KEY, currentTestReport.getTest().getIdentityService());

				currentScenarioJSON.put(REQUEST_BODY_KEY, currentTestReport.getTest().getRequestBody());
				currentScenarioJSON.put(REQUEST_HEADERS_KEY, CommonUtlities.maskAuthTokenInMap(currentTestReport.getTest().getRequestHeaders()));

				currentScenarioJSON.put(RESPONSE_BODY_KEY, currentTestReport.getResponseBody());
				currentScenarioJSON.put(RESPONSE_HEADERS_KEY, currentTestReport.getResponseHeaders());

				currServiceScenarioJSON.put(currentTestReport.getTest().getName(), currentScenarioJSON);
			}
			// Add the Service Scenarios JSONObject to the current Service JSONObject
			currServiceJSON.put(SCENARIOS_KEY, currServiceScenarioJSON);

			// Add the Service JSONObject to the current Module JSONObject
			currModuleJSON.getJSONObject(SERVICES_KEY).put(currService.getName(), currServiceJSON);
		}

		// Traverse the Modules JSONObject Map to construct the Result JSONObject
		JSONObject modulesDocumentJSON = new JSONObject();
		for (Entry<String, JSONObject> entry : moduleJSONMap.entrySet()) {
			modulesDocumentJSON.put(entry.getKey(), entry.getValue());
		}

		return modulesDocumentJSON;
	}

	/**
	 * @param testSummary
	 * @param HTMLReportContent
	 * @throws IOException
	 */
	private static void exportDocumentToFile(TestSummary testSummary, File resultsFilePointer) throws IOException {

		String fileContents = getAPIDocumentationAsHTMLContent(testSummary);
		String filePath = resultsFilePointer.getAbsolutePath();
		FileUtilities.writeStringToFile(fileContents, filePath);
	}

}
