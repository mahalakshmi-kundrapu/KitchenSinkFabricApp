package com.kony.service.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kony.service.constants.SecurityLevel;
import com.kony.service.dto.Assertion;
import com.kony.service.dto.FieldDescription;
import com.kony.service.dto.Service;
import com.kony.service.dto.TestCase;

/**
 * Method to parse the export(v2.1) of a Postman-Collection and update the Test-Plans
 * 
 * @author Aditya Mankal
 *
 */
public class PostmanCollectionParser {

	private static final String COLLECTION_PATH = "/Volumes/Data/Workspace/Staging/AdminConsole-Team.postman_collection.json";
	private static final String TEST_PLANS_INPUT_DIRECTORY = "/Volumes/Data/Workspace/Staging/InputTestPlans";
	private static final String TEST_PLANS_OUTPUT_DIRECTORY = "/Volumes/Data/Workspace/Staging/ProcessedTestPlans";
	private static final String SERVICE_DOCUMENT_OUTPUT_DIRECTORY = "/Volumes/Data/Workspace/Staging/ServiceDocuments";

	private static final String X_KONY_AUTHORIZATION_HEADER_KEY = "X-Kony-Authorization";

	private static final String X_KONY_AC_API_ACCESS_BY_HEADER_KEY = "X-Kony-AC-API-Access-By";
	private static final String X_KONY_AC_API_ACCESS_BY_HEADER_VALUE = "OLB";

	private static final String API_IDENTITY_SERVICE_NAME = "KonyBankingAdminConsoleAPIIdentityService";
	private static final String DEFAULT_IDENTITY_SERVICE_NAME = "KonyBankingAdminConsoleIdentityService";

	private static final int DEFAULT_ID_LENGTH = 8;
	private static final int DEFAULT_TEST_CASE_SEQUENCE = 1;
	private static final String DEFAULT_SECURITY_LEVEL = "AuthenticatedAppUser";

	private static final String DEFAULT_TEST_CASE_PRIORITY = "P0";
	private static final String DEFAULT_TEST_CASE_NAME = "Status Check";

	private static final String POSTMAN_REQUEST_URL_PREFIX = "{{HTTP-Protocol}}://{{Host-URL}}/";

	private static final String FILE_PATH_SEPERATOR = System.getProperty("file.separator");

	// Map between defined Test-Plans and corresponding Postman Request
	private static Map<Service, PostmanRequest> servicePostmanRequestJSONMap = new HashMap<>();

	// List of all Postman Requests
	private static List<PostmanRequest> postmanRequestList = new ArrayList<>();

	// List of Postman Requests for which Test-Plans have not been defined
	private static List<PostmanRequest> pendingPostmanRequestList = new ArrayList<>();

	public static void main(String[] args) throws IOException {

		boolean isServiceDocumentGenerationMode = false; // Toggle This

		// Verify Input Directory and Create Output Directory
		processInputOutputArtifacts();

//		migrateServiceDocumentToUpdatedFormat();

		if (isServiceDocumentGenerationMode == true) {
			generateServiceHTMLDocumentation();
		} else {

			boolean isUpdateMode = false; // Toggle This

			// Populate the Service - PostmanRequest Map and PostmanRequest List
			parsePostmanCollection();

			if (isUpdateMode == true) {
				// Update the contents of Test Plans
				updateTestPlans();
			} else {
				// Create missing Test Plans
				createMissingTestPlans();
			}
		}

		System.out.println("### - Done - ###");

	}

	/**
	 * Method to generate the Postman Documentation of Services
	 * 
	 * @throws IOException
	 */
	private static void generateServiceHTMLDocumentation() throws IOException {

		System.out.println("Generating Service Documents...");

		boolean writeStatus;
		List<Service> services = getServices();
		StringBuffer currentDocument = null;
		String description, currFileContents, currFileName, currFilePath;

		for (Service service : services) {

			currentDocument = new StringBuffer();
			description = StringUtils.EMPTY;

			// Set Service Description
			if (StringUtils.isNotBlank(service.getDescription())) {
				description = service.getDescription();
			}
			currentDocument.append("<blockquote>" + description + "</blockquote>");

			// Set Input Fields Description
			if (service.getInputFieldsDocument() != null && service.getInputFieldsDocument().size() > 0) {
				// Service has Input Fields Defined
				currentDocument.append("<div class='tile'><h4>Inputs</h4> <table class='api-detail'> <thead> <tr> <th>Parameter</th> <th>Description</th> </tr></thead> <tbody> ");
				for (FieldDescription fieldDescription : service.getInputFieldsDocument()) {
					currentDocument.append("<tr>");
					currentDocument.append("<td>" + fieldDescription.getName() + "</td>");
					currentDocument.append("<td>" + fieldDescription.getDescription() + "</td>");
					currentDocument.append("</tr>");
				}
				currentDocument.append("</tbody></table></div>");
			} else {
				// Service has No Input Fields
				currentDocument.append("<div class='tile'><h4>Inputs</h4> <p>No Inputs Needed</p></div>");
			}

			// Set Output Fields Description
			if (service.getOutputFieldsDocument() != null && service.getOutputFieldsDocument().size() > 0) {
				// Service has Output Fields Defined
				currentDocument.append("<div class='tile'><h4>Outputs</h4> <table class='api-detail'> <thead> <tr> <th>Parameter</th> <th>Description</th> </tr></thead> <tbody> ");
				for (FieldDescription fieldDescription : service.getOutputFieldsDocument()) {
					currentDocument.append("<tr>");
					currentDocument.append("<td>" + fieldDescription.getName() + "</td>");
					currentDocument.append("<td>" + fieldDescription.getDescription() + "</td>");
					currentDocument.append("</tr>");
				}
				currentDocument.append("</tbody></table></div>");
			} else {
				// Service has No Output Fields
				currentDocument.append("<div class='tile'><h4>Inputs</h4> <p>No Outputs Defined</p></div>");
			}

			// Export Document to HTML File
			currentDocument.trimToSize();
			currFileContents = currentDocument.toString().trim();
			currFileName = "Service_" + service.getModuleName() + "_" + service.getName();
			currFilePath = SERVICE_DOCUMENT_OUTPUT_DIRECTORY + FILE_PATH_SEPERATOR + currFileName + ".html";
			writeStatus = FileUtilities.writeStringToFile(currFileContents, currFilePath);
			if (writeStatus == true) {
				System.out.println("  SUCCESS: Exported Service Document:" + currFilePath);
			} else {
				System.out.println("  ERROR: Failed to Service Document:" + currFilePath);
			}
		}
	}

	/**
	 * Method to update the test-plans based on postman-requests defintion
	 */
	@SuppressWarnings("unused")
	private static void migrateServiceDocumentToUpdatedFormat() throws IOException {
		List<Service> updatedServices = new ArrayList<>();

		List<Service> services = getServices();

		for (Service service : services) {
			for (TestCase test : service.getTests()) {
				test.setSecurityLevel(SecurityLevel.AUTHENTICATED_APP_USER.getValue());
			}
			updatedServices.add(service);
		}

		// Display Pending Services
		displayUpdatedService(updatedServices);

		// Delete previous state of Processed Services
		deleteUpdatedServicesFromInputDirectory(updatedServices);

		// Dump Updated Services
		System.out.println(System.lineSeparator() + "Dumping Updated Services To Output Directory...");
		String currContent, currFileName;
		for (Service service : updatedServices) {
			currContent = beautifyJSON(JSONUtilities.stringify(service));
			currFileName = "Service_" + service.getModuleName() + "_" + service.getName();
			FileUtilities.writeStringToFile(currContent, TEST_PLANS_OUTPUT_DIRECTORY + FILE_PATH_SEPERATOR + currFileName + ".json");
		}
	}

	/**
	 * Method to update the test-plans based on postman-requests defintion
	 */
	private static void updateTestPlans() throws IOException {
		// Traverse Service - PostmanRequest Map
		boolean isUpdated;
		List<Service> updatedServices = new ArrayList<>();
		for (Entry<Service, PostmanRequest> entry : servicePostmanRequestJSONMap.entrySet()) {
			isUpdated = false;
			System.out.println(System.lineSeparator() + "##### ----- #####");
			System.out.println("  Updating Service:" + entry.getKey().getName());

			// Update Service Module Name
			isUpdated = setServiceModuleName(entry.getKey(), entry.getValue());

			// Update Service Documentation
			isUpdated = setServiceDocument(entry.getKey(), entry.getValue());

			// Add Updated Service to list
			if (isUpdated == true) {
				updatedServices.add(entry.getKey());
				System.out.println("  Service Updated");
			} else {
				System.out.println("  Service Update Skipped");
			}

			System.out.println("##### ----- #####");
		}

		// Display Pending Services
		displayUpdatedService(updatedServices);

		// Delete previous state of Processed Services
		deleteUpdatedServicesFromInputDirectory(updatedServices);

		// Dump Updated Services
		System.out.println(System.lineSeparator() + "Dumping Updated Services To Output Directory...");
		String currContent, currFileName;
		for (Service service : updatedServices) {
			currContent = beautifyJSON(JSONUtilities.stringify(service));
			currFileName = "Service_" + service.getModuleName() + "_" + service.getName();
			FileUtilities.writeStringToFile(currContent, TEST_PLANS_OUTPUT_DIRECTORY + FILE_PATH_SEPERATOR + currFileName + ".json");
		}
	}

	/**
	 * Method to create the missing test-plans based on postman-requests defintion
	 */
	private static void createMissingTestPlans() throws IOException {

		int count = 0;
		TestPlanFile currTestPlanFile;
		String currFilePath, currFileContents;
		boolean writeStatus;
		System.out.println("Generating Test-Plans...");
		for (PostmanRequest postmanRequest : pendingPostmanRequestList) {
			currTestPlanFile = generateTestPlan(postmanRequest);
			if (currTestPlanFile != null) {
				count++;
				currFilePath = TEST_PLANS_OUTPUT_DIRECTORY + FILE_PATH_SEPERATOR + currTestPlanFile.getFileName();
				currFileContents = beautifyJSON(currTestPlanFile.getFileContents());
				writeStatus = FileUtilities.writeStringToFile(currFileContents, currFilePath);
				if (writeStatus == true) {
					System.out.println("  SUCCESS: Exported Test Plan:" + currFilePath);
				} else {
					System.out.println("  ERROR: Failed to exported Test Plan:" + currFilePath);
				}
			} else {
				System.out.println("  WARNING: Failed to generate Test Plan: Request JSON:" + postmanRequest.getRequestJSON().toString());
			}
		}
		System.out.println("Test-Plans generation complete. Count of Exported Test Plans:" + count);

	}

	private static Map<String, Integer> consumedServiceNames = new HashMap<>();

	/**
	 * Method to create a Test-Plan based on Postman Request
	 * 
	 * @param postmanRequest
	 * @return instance of TestPlanFile
	 * @throws IOException
	 */
	private static TestPlanFile generateTestPlan(PostmanRequest postmanRequest) throws IOException {

		int frequency = 1;
		JSONObject postmanRequestJSON = postmanRequest.getRequestJSON();
		JSONObject requestJSON = postmanRequestJSON.optJSONObject("request");
		if (postmanRequestJSON == null || requestJSON == null) {
			return null;
		}

		// Create Service Instance
		Service service = new Service();

		// Set Service URL
		String serviceURL = stripPostmanRequestURLPrefixAndGetRequestURL(postmanRequestJSON);
		if (!StringUtils.startsWith(serviceURL, "services")) {
			return null;
		}
		service.setUrl(serviceURL.trim().replaceAll(" +", " "));

		// Set Module Name
		String moduleName = postmanRequest.getModuleName();
		moduleName = moduleName.replaceAll("[/\\\\]+", "-").trim().replaceAll(" +", " ");
		moduleName = moduleName.replace("- ", "-");
		moduleName = moduleName.replace("_ ", "_");
		service.setModuleName(moduleName.trim());

		// Set Service Name
		String serviceName = StringUtils.EMPTY;
		JSONObject urlJSON = requestJSON.optJSONObject("url");
		if (urlJSON != null) {
			JSONArray pathArray = urlJSON.optJSONArray("path");
			if (pathArray != null) {
				serviceName = getRequestName(pathArray);
			}
		}
		if (StringUtils.isBlank(serviceName)) {
			// Generate a random service name
			serviceName = String.valueOf(generateRandomDigits(5));
		}
		serviceName = formatName(serviceName);
		serviceName = serviceName.replaceAll("[/\\\\]+", "-").trim().replaceAll(" +", " ");
		serviceName = serviceName.replace("- ", "-");
		serviceName = serviceName.replace("_ ", "_");
		serviceName = serviceName + "_AUTO";
		service.setName(serviceName.trim().replaceAll(" +", " "));

		// Set Service Description, Input Fields Description and Output Fields Description
		setServiceDocument(service, postmanRequest);

		// Set Service HTTP Method
		String httpMethod = requestJSON.optString("method");
		service.setHttpMethod(httpMethod.trim().replaceAll(" +", " "));

		// Parse Request-Headers
		String identityServiceName = DEFAULT_IDENTITY_SERVICE_NAME;
		Map<String, String> requestHeadersMap = new HashMap<>();
		JSONArray requestHeadersArray = requestJSON.optJSONArray("header");
		if (requestHeadersArray != null) {
			JSONObject currJSON = null;
			for (Object currObject : requestHeadersArray) {
				currJSON = (JSONObject) currObject;
				if (StringUtils.equalsIgnoreCase(currJSON.optString("key").trim(), X_KONY_AUTHORIZATION_HEADER_KEY)) {
					// Ignore
				} else if (StringUtils.equalsIgnoreCase(currJSON.optString("key").trim(), X_KONY_AC_API_ACCESS_BY_HEADER_KEY)) {
					// API Identity Service
					identityServiceName = API_IDENTITY_SERVICE_NAME;
					requestHeadersMap.put(X_KONY_AC_API_ACCESS_BY_HEADER_KEY, X_KONY_AC_API_ACCESS_BY_HEADER_VALUE);
				} else {
					requestHeadersMap.put(currJSON.optString("key").trim(), currJSON.optString("value").trim());
				}
			}
		}

		// Parse Request-Body
		String requestBody = StringUtils.EMPTY;
		JSONObject bodyJSON = requestJSON.optJSONObject("body");
		if (bodyJSON != null) {
			String requestMode = bodyJSON.optString("mode");
			requestBody = bodyJSON.optString(requestMode);
		}

		// Create Test-Case Instance
		TestCase testCase = new TestCase();

		// Set Test-Case Name
		testCase.setName(DEFAULT_TEST_CASE_NAME);

		// Set Test Sequence
		testCase.setTestSequence(DEFAULT_TEST_CASE_SEQUENCE);

		// Set Test Priority
		testCase.setTestPriority(DEFAULT_TEST_CASE_PRIORITY);

		// Set Test-Case Id
		testCase.setId(String.valueOf(generateRandomDigits(DEFAULT_ID_LENGTH)));

		// Set Security Level
		testCase.setSecurityLevel(DEFAULT_SECURITY_LEVEL);

		// Set Identity Service
		testCase.setIdentityService(identityServiceName);
		testCase.setIsCustomAuthTest(false);
		testCase.setCustomAuthHeaders(new HashMap<>());

		// Set Request Headers
		testCase.setRequestHeaders(requestHeadersMap);

		// Set Request Body
		requestBody = requestBody.replaceAll("\\r", "");
		requestBody = requestBody.replaceAll("\\t", "");
		requestBody = requestBody.replaceAll("\\n", "");
		requestBody = requestBody.replaceAll(" +", " ");
		testCase.setRequestBody(requestBody.trim());

		// Prepare Response Body Assertions
		List<Assertion> responseBodyAssertions = new ArrayList<>();

		// opstatus Assertion
		Assertion opstatusAssertion = new Assertion();
		opstatusAssertion.setId(String.valueOf(generateRandomDigits(DEFAULT_ID_LENGTH)));
		opstatusAssertion.setPath("$.opstatus");
		opstatusAssertion.setDataType("number");
		opstatusAssertion.setOperator("equals");
		opstatusAssertion.setValue("0");
		opstatusAssertion.setIsNullable(false);
		opstatusAssertion.setIsValueAgnostic(false);
		responseBodyAssertions.add(opstatusAssertion);

		// DBPErrorCode Assertion
		Assertion dbpErrorCodeAssertion = new Assertion();
		dbpErrorCodeAssertion.setId(String.valueOf(generateRandomDigits(DEFAULT_ID_LENGTH)));
		dbpErrorCodeAssertion.setPath("$.dbpErrCode");
		dbpErrorCodeAssertion.setDataType("number");
		dbpErrorCodeAssertion.setOperator("equals");
		dbpErrorCodeAssertion.setValue("0");
		dbpErrorCodeAssertion.setIsNullable(true);
		dbpErrorCodeAssertion.setIsValueAgnostic(false);
		responseBodyAssertions.add(dbpErrorCodeAssertion);

		// Set Test Assertions
		testCase.setResponseBodyAssertions(responseBodyAssertions);
		testCase.setResponseHeaderAssertions(new ArrayList<Assertion>());

		// Add Test Case to Service
		List<TestCase> testCases = new ArrayList<>();
		testCases.add(testCase);
		service.setTests(testCases);

		// Maintain a track of File names
		if (consumedServiceNames.keySet().contains((serviceName))) {
			// Service with the same name already exists
			frequency = consumedServiceNames.get(serviceName).intValue() + 1;
		} else {
			frequency = 1;
		}
		consumedServiceNames.put(serviceName, Integer.valueOf(frequency));
		if (frequency > 1) {
			// Append the frequency value to the seviceName to ensure unique file names
			serviceName = serviceName + "_" + String.valueOf(frequency);
		}

		// Return Test-Plan File
		String fileName;
		String fileContents = JSONUtilities.stringify(service);
		if (serviceName.startsWith(moduleName)) {
			fileName = "Service_" + serviceName + ".json";
		} else {
			fileName = "Service_" + moduleName + "_" + serviceName + ".json";
		}

		TestPlanFile testPlanFile = new TestPlanFile(fileName, fileContents);
		return testPlanFile;

	}

	/**
	 * Method to get the Request Name from URL path array
	 * 
	 * @param pathArray
	 * @return Request Name
	 */
	private static String getRequestName(JSONArray pathArray) {
		String requestName = StringUtils.EMPTY;
		if (pathArray == null) {
			return requestName;
		}
		int pathArrayLength = pathArray.length();
		if (pathArray.length() > 1) {
			requestName = StringUtils.capitalize(pathArray.getString(pathArrayLength - 2)) + "-" + StringUtils.capitalize(pathArray.getString(pathArrayLength - 1));
		} else {
			requestName = StringUtils.capitalize(pathArray.getString(pathArrayLength - 1));
		}
		return requestName;
	}

	/**
	 * Method to display the list of updated services on the console
	 * 
	 * @param updatedServices
	 */
	private static void displayUpdatedService(List<Service> updatedServices) {
		System.out.println(System.lineSeparator() + "Pending Services:");
		for (Service service : updatedServices) {
			System.out.println("  " + service.getName());
		}

	}

	/**
	 * Method to delete the list of updated services from the Input Console
	 * 
	 * @param updatedServices
	 */
	private static void deleteUpdatedServicesFromInputDirectory(List<Service> updatedServices) {
		File currentFile = null;
		String currPath = null;
		System.out.println(System.lineSeparator() + "Deleting previous state of updated Services:");
		for (Service service : updatedServices) {
			System.out.println(System.lineSeparator() + "  Deleting Service:" + service.getName());
			currPath = TEST_PLANS_INPUT_DIRECTORY + FILE_PATH_SEPERATOR + "Service_" + service.getModuleName() + "_" + service.getName() + ".json";
			currentFile = new File(currPath);
			currentFile.delete();
			currPath = TEST_PLANS_INPUT_DIRECTORY + FILE_PATH_SEPERATOR + "Service_" + service.getName() + ".json";
			currentFile = new File(currPath);
			currentFile.delete();
			System.out.println("  Deleted Service:" + service.getName());

		}

	}

	/**
	 * Method to set the Module name of a Service. This method does NOT override the module name if it is already set
	 * 
	 * @param service
	 * @param postmanRequest
	 * @return update status
	 */
	private static boolean setServiceModuleName(Service service, PostmanRequest postmanRequest) {
		if (service == null) {
			return false;
		}
		// Set Module Name
		if (StringUtils.isBlank(service.getModuleName())) {
			service.setModuleName(postmanRequest.getModuleName());
			return true;
		}
		return false;
	}

	/**
	 * Method to set the Description, Input & Output Fields Description of a Service. This method does NOT override the values if they are already set
	 * 
	 * @param service
	 * @param postmanRequest
	 * @return update status
	 */
	private static boolean setServiceDocument(Service service, PostmanRequest postmanRequest) {
		if (service == null || postmanRequest.getRequestJSON() == null) {
			return false;
		}
		JSONObject requestJSON = postmanRequest.getRequestJSON();
		if (requestJSON.optJSONObject("request") == null || !requestJSON.optJSONObject("request").has("description")) {
			return false;
		}

		List<FieldDescription> inputFieldsDocument = new ArrayList<>();
		List<FieldDescription> ouputFieldsDocument = new ArrayList<>();

		String htmlDescription = requestJSON.optJSONObject("request").optString("description");
		htmlDescription = htmlDescription.replaceAll("\\\\n", "");
		htmlDescription = htmlDescription.replaceAll("\\\\r", "");
		String serviceDesc = null;
		try {
			serviceDesc = htmlDescription.substring(htmlDescription.indexOf("<span>") + "<span>".length(), htmlDescription.indexOf("</span>"));
		} catch (Exception e) {
			// Postman Service with an invalid/missing Description
		}

		String currField, currDesc;
		Document document = Jsoup.parse(htmlDescription);
		Elements tables = document.select("table");
		boolean isUpdated = false;

		if (tables.size() > 0) {
			Elements rows, cells;

			if (tables.size() == 1) {
				// No Input Fields. Only output Fields defined
				rows = tables.get(0).select("tr");
				for (int i = 0; i < rows.size(); i++) {
					cells = rows.get(i).select("td");

					if (cells.size() == 2) {
						FieldDescription currFieldDescription = new FieldDescription();
						currField = cells.get(0).text().trim();
						currDesc = cells.get(1).text().trim();
						currFieldDescription.setName(currField);
						currFieldDescription.setDescription(currDesc);
						currFieldDescription.setIsMandatory(StringUtils.containsIgnoreCase(currField, "mandatory"));
						currFieldDescription.setRange(StringUtils.EMPTY);
						inputFieldsDocument.add(currFieldDescription);
					}
				}
			}

			else {

				// Input Fields Table
				rows = tables.get(0).select("tr");
				for (int i = 0; i < rows.size(); i++) {
					cells = rows.get(i).select("td");
					if (cells.size() == 2) {
						FieldDescription currFieldDescription = new FieldDescription();
						currField = cells.get(0).text().trim();
						currDesc = cells.get(1).text().trim();
						currFieldDescription.setName(currField);
						currFieldDescription.setDescription(currDesc);
						currFieldDescription.setIsMandatory(StringUtils.containsIgnoreCase(currField, "mandatory"));
						currFieldDescription.setRange(StringUtils.EMPTY);
						inputFieldsDocument.add(currFieldDescription);
					}

				}

				// Output Fields Table
				rows = tables.get(1).select("tr");
				for (int i = 0; i < rows.size(); i++) {
					cells = rows.get(i).select("td");
					if (cells.size() == 2) {
						FieldDescription currFieldDescription = new FieldDescription();
						currField = cells.get(0).text().trim();
						currDesc = cells.get(1).text().trim();
						currFieldDescription.setName(currField);
						currFieldDescription.setDescription(currDesc);
						currFieldDescription.setIsMandatory(StringUtils.containsIgnoreCase(currField, "mandatory"));
						currFieldDescription.setRange(StringUtils.EMPTY);
						ouputFieldsDocument.add(currFieldDescription);
					}
				}
			}

			if (inputFieldsDocument.size() > 0 && (service.getInputFieldsDocument() == null || service.getInputFieldsDocument().size() == 0)) {
				service.setInputFieldsDocument(inputFieldsDocument);
				isUpdated = true;
			}

			if (ouputFieldsDocument.size() > 0 && (service.getOutputFieldsDocument() == null || service.getOutputFieldsDocument().size() == 0)) {
				service.setOutputFieldsDocument(ouputFieldsDocument);
				isUpdated = true;
			}

		}

		if (StringUtils.isBlank(service.getDescription()) && StringUtils.isNotBlank(serviceDesc)) {
			service.setDescription(serviceDesc);
			isUpdated = true;
		}

		return isUpdated;
	}

	private static HashMap<String, JSONArray> requestsMap = new HashMap<String, JSONArray>();

	/**
	 * Method to fetch the List of Services for which Test Plans have been created
	 * 
	 * @return List of Services
	 * @throws IOException
	 */
	private static List<Service> getServices() throws IOException {
		File servicesDirectoryFilePointer = new File(TEST_PLANS_INPUT_DIRECTORY);
		String currentServiceFileContents;
		List<Service> servicesList = new ArrayList<Service>();
		for (File currFile : servicesDirectoryFilePointer.listFiles()) {
			if (StringUtils.startsWith(currFile.getName(), "Service_")) {
				currentServiceFileContents = FileUtilities.readFileContentsToString(currFile.getAbsolutePath());
				Service currentService = JSONUtilities.parse(currentServiceFileContents, Service.class);
				servicesList.add(currentService);
			}
		}
		return servicesList;
	}

	/**
	 * Method to populate the servicePostmanRequestJSONMap
	 * 
	 * @throws IOException
	 */
	private static void parsePostmanCollection() throws IOException {
		// Construct Collection JSON
		String collectionContent = FileUtilities.readFileContentsToString(COLLECTION_PATH);
		JSONObject collectionJSON = new JSONObject(collectionContent);
		boolean isTestPlanDefined;
		// Traverse Root Items Array
		JSONArray rootItemsArray = collectionJSON.optJSONArray("item");
		for (int i = 0; i < rootItemsArray.length(); ++i) {
			JSONObject itemObject = rootItemsArray.getJSONObject(i);
			requestsMap.put(itemObject.getString("name"), new JSONArray());
			traverseItemObject(itemObject.getString("name"), itemObject);
		}

		JSONObject currRequestJSON;
		String currRequestURL_Postman, currRequestURL_Service;
		JSONArray moduleRequestsArray = null;
		List<Service> services = getServices();

		// Traverse Postman collection
		for (Map.Entry<String, JSONArray> entry : requestsMap.entrySet()) {
			moduleRequestsArray = entry.getValue();
			for (Object currRequestObject : moduleRequestsArray) {
				if (currRequestObject instanceof JSONObject) {
					currRequestJSON = (JSONObject) currRequestObject;
					currRequestURL_Postman = stripPostmanRequestURLPrefixAndGetRequestURL(currRequestJSON);
					PostmanRequest postmanRequest = new PostmanRequest(entry.getKey(), currRequestJSON);
					postmanRequestList.add(postmanRequest);

					isTestPlanDefined = false;
					// Traverse Services
					for (Service service : services) {
						currRequestURL_Service = service.getUrl();
						if (currRequestURL_Service.startsWith("/")) {
							currRequestURL_Service = currRequestURL_Service.substring(1);
						}
						if (StringUtils.equalsIgnoreCase(currRequestURL_Postman, currRequestURL_Service)) {
							servicePostmanRequestJSONMap.put(service, postmanRequest);
							isTestPlanDefined = true;
							break;
						}
					}
					if (isTestPlanDefined == false) {
						pendingPostmanRequestList.add(postmanRequest);
					}
				}
			}
		}
	}

	/**
	 * Method to validate the Tool Inputs: Postman Collection & Test-Plans Directory and to clean the Output Directory
	 */
	private static void processInputOutputArtifacts() {

		File postmanCollection = new File(COLLECTION_PATH);
		if (!postmanCollection.exists() || postmanCollection.isDirectory()) {
			System.err.println("Invalid Postman Collection File Path. Code Terminated");
			System.exit(1);
		}

		File inputDirectory = new File(TEST_PLANS_INPUT_DIRECTORY);
		if (!inputDirectory.exists() || !inputDirectory.isDirectory()) {
			System.err.println("Invalid Input Directory Path. Code Terminated");
			System.exit(1);
		}

		File outputDirectory = new File(TEST_PLANS_OUTPUT_DIRECTORY);
		if (outputDirectory.exists() || !outputDirectory.isDirectory()) {
			outputDirectory.delete();
		}
		outputDirectory.mkdir();

		outputDirectory = new File(SERVICE_DOCUMENT_OUTPUT_DIRECTORY);
		if (outputDirectory.exists() || !outputDirectory.isDirectory()) {
			outputDirectory.delete();
		}
		outputDirectory.mkdir();
	}

	/**
	 * Method to strip the Postman Request URL Prefix and return the resultant URL
	 * 
	 * @param itemJSONObject
	 * @return Trimmed Request URL
	 */
	private static String stripPostmanRequestURLPrefixAndGetRequestURL(JSONObject itemJSONObject) {
		if (itemJSONObject.optJSONObject("request") == null || itemJSONObject.getJSONObject("request").getJSONObject("url") == null
				|| !itemJSONObject.getJSONObject("request").getJSONObject("url").has("raw")) {
			return StringUtils.EMPTY;
		}
		String url = itemJSONObject.getJSONObject("request").getJSONObject("url").getString("raw");
		url = url.replace(POSTMAN_REQUEST_URL_PREFIX, "");
		return url;
	}

	/**
	 * Method to recursively traverse the Item Object of a Postman Collection and add the containing Request items to the RequestMap
	 * 
	 * @param moduleName
	 * @param itemObject
	 */
	private static void traverseItemObject(String moduleName, JSONObject itemObject) {
		JSONArray itemArray = itemObject.optJSONArray("item");
		if (itemArray == null) {
			// Implies that the current item is a Request. Add to map
			requestsMap.put(moduleName, requestsMap.get(moduleName).put(itemObject));
		} else {
			// Implies that the current item is a Folder. Traverse further
			for (int index = 0; index < itemArray.length(); ++index) {
				traverseItemObject(moduleName, itemArray.getJSONObject(index));
			}
		}
	}

	private static String beautifyJSON(String json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Object obj = mapper.readValue(json, Object.class);
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
	}

	/**
	 * Slits the input string at each occurance of case-change.
	 * Converts each sub-string to Title Case
	 * Concats the sub-strings to form the result string
	 * 
	 * @param input
	 * @return Formatted Name
	 */
	private static String formatName(String input) {
		if (StringUtils.isBlank(input)) {
			return StringUtils.EMPTY;
		}
		String[] words = input.split("(?=\\p{Upper})");
		String result = StringUtils.EMPTY;
		String str = StringUtils.EMPTY;
		for (int i = 0; i < words.length; i++) {
			str = words[i];
			str = str.substring(0, 1).toUpperCase() + str.substring(1);
			if (str.length() != 1) {
				result = result + str + " ";
			} else {
				result = result + str;
			}

		}

		return result.trim();
	}

	public static int generateRandomDigits(int n) {
		int m = (int) Math.pow(10, n - 1);
		return m + new Random().nextInt(9 * m);
	}

}

/**
 * Class for internal use. Collates the File Name and File Contents
 * 
 * @author Aditya Mankal
 *
 */
class TestPlanFile {
	private String fileName;
	private String fileContents;

	/**
	 * @param fileName
	 * @param fileContents
	 */
	public TestPlanFile(String fileName, String fileContents) {
		super();
		this.fileName = fileName;
		this.fileContents = fileContents;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the fileContents
	 */
	public String getFileContents() {
		return fileContents;
	}

	/**
	 * @param fileContents the fileContents to set
	 */
	public void setFileContents(String fileContents) {
		this.fileContents = fileContents;
	}

}

/**
 * Class for internal use. Collates the service's Module-Name and the service's Request JSON from the Postman Collection
 * 
 * @author Aditya Mankal
 *
 */
class PostmanRequest {
	private String moduleName;
	private JSONObject requestJSON;

	/**
	 * @param moduleName
	 * @param requestJSON
	 */
	public PostmanRequest(String moduleName, JSONObject requestJSON) {
		super();
		this.moduleName = moduleName;
		this.requestJSON = requestJSON;
	}

	/**
	 * @return the moduleName
	 */
	public String getModuleName() {
		return moduleName;
	}

	/**
	 * @param moduleName the moduleName to set
	 */
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	/**
	 * @return the requestJSON
	 */
	public JSONObject getRequestJSON() {
		return requestJSON;
	}

	/**
	 * @param requestJSON the requestJSON to set
	 */
	public void setRequestJSON(JSONObject requestJSON) {
		this.requestJSON = requestJSON;
	}

}
