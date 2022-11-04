package com.kony.adminconsole.service.alertmanagement.staging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.AlertManagementHandler;
import com.kony.adminconsole.service.authmodule.APICustomIdentityService;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to Fetch the Alert Category Definition
 * 
 * @author Aditya Mankal
 */
public class AlertCategoryDefinitionGetService implements JavaService2 {

	private static final String ALERT_CATEGORY_ID_PARAM = "AlertCategoryId";

	private static final String DEFAULT_LOCALE = AlertManagementHandler.DEFAULT_LOCALE;

	private static final Logger LOG = Logger.getLogger(AlertCategoryDefinitionGetService.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) {

		Result processedResult = new Result();

		try {
			// Read Inputs
			String alertCategoryId = requestInstance.getParameter(ALERT_CATEGORY_ID_PARAM);
			LOG.debug("Received Alert Category ID:" + alertCategoryId);
			String acceptLanguage = requestInstance.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
			LOG.debug("Received Accept-Language Header:" + acceptLanguage);

			// Validate Inputs
			if (StringUtils.isBlank(alertCategoryId)) {
				// Missing Alert Category ID
				LOG.error("Missing Alert Category ID");
				ErrorCodeEnum.ERR_20920.setErrorCode(processedResult);
				return processedResult;
			}

			// Fetch Logged In User Info
			String loggedInUser = StringUtils.EMPTY;
			UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
			if (userDetailsBeanInstance != null) {
				loggedInUser = userDetailsBeanInstance.getUserId();
			}

			// Format Accept Language Identifier
			if (StringUtils.isBlank(acceptLanguage)) {
				// Consider Default Locale if Accept-Language Header is Blank
				acceptLanguage = DEFAULT_LOCALE;
				LOG.debug("Received Accept-Language Header is empty. Returning Data of Default Locale." + DEFAULT_LOCALE);
			}
			acceptLanguage = CommonUtilities.formatLanguageIdentifier(acceptLanguage);
			if (StringUtils.equalsIgnoreCase(loggedInUser, APICustomIdentityService.API_USER_ID)) {
				acceptLanguage = AlertManagementHandler.formatLocaleAsPerKonyMobileSDK(acceptLanguage);
			}

			// Get Alert Category Definition
			Record alertCategoryDefintionRecord = getAlertCategoryDefintion(alertCategoryId, acceptLanguage, requestInstance);
			processedResult.addRecord(alertCategoryDefintionRecord);

			// Get Alert Category Channels
			Dataset alertCategoryChannelsDataset = getAlertCategoryChannels(alertCategoryId, acceptLanguage, requestInstance);
			processedResult.addDataset(alertCategoryChannelsDataset);

			// Get Alert Groups
			Dataset alertGroupsDataset = getAlertTypesOfAlertCategory(alertCategoryId, acceptLanguage, requestInstance);
			processedResult.addDataset(alertGroupsDataset);

			// Get Alert Category Display Preference
			Dataset alertCategoryDisplayPreferenceDataset = getAlertCategoryDisplayPreferences(alertCategoryId, requestInstance);
			processedResult.addDataset(alertCategoryDisplayPreferenceDataset);

		} catch (ApplicationException e) {
			Result errorResult = new Result();
			LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
			e.getErrorCodeEnum().setErrorCode(errorResult);
			return errorResult;
		} catch (Exception e) {
			Result errorResult = new Result();
			LOG.error("Exception in Fetching Customer Alert Category Channel Preference. Exception:", e);
			ErrorCodeEnum.ERR_20914.setErrorCode(errorResult);
			return errorResult;
		}
		return processedResult;
	}

	/**
	 * Method to get basic attributes of Alert category as a Record
	 * 
	 * @param alertCategoryId
	 * @param acceptLanguage
	 * @param requestInstance
	 * @return Record containing Alert Category Information
	 * @throws ApplicationException
	 */
	private Record getAlertCategoryDefintion(String alertCategoryId, String acceptLanguage, DataControllerRequest requestInstance) throws ApplicationException {

		if (requestInstance == null) {
			// Data Controller Request Instance is NULL. Throw Application Exception.
			LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception.");
			throw new ApplicationException(ErrorCodeEnum.ERR_20927);
		}

		Record operationRecord = new Record();
		operationRecord.setId("categoryDefintion");

		if (StringUtils.isBlank(alertCategoryId)) {
			// Missing Mandatory Input. Return Empty Record
			LOG.error("Alert Category Id value is empty. Returning empty record.");
			return operationRecord;
		}

		if (StringUtils.isBlank(acceptLanguage)) {
			// Consider Default Locale if Accept-Language Header is Blank
			acceptLanguage = DEFAULT_LOCALE;
			LOG.debug("Received Accept-Language Header is empty. Returning Data of Default Locale." + DEFAULT_LOCALE);
		}

		// Construct Filter Query
		String filterQuery;
		if (StringUtils.equals(acceptLanguage, DEFAULT_LOCALE)) {
			filterQuery = "(alertcategory_id eq '" + alertCategoryId + "' and alertcategorytext_LanguageCode eq '" + acceptLanguage + "')";
		} else {
			filterQuery = "alertcategory_id eq '" + alertCategoryId + "' and (alertcategorytext_LanguageCode eq '" + acceptLanguage + "' or alertcategorytext_LanguageCode eq '"
					+ DEFAULT_LOCALE + "')";
		}

		// Construct Input Map
		Map<String, String> paramaterMap = new HashMap<>();
		paramaterMap.put(ODataQueryConstants.FILTER, filterQuery);
		paramaterMap.put(ODataQueryConstants.ORDER_BY, "alertcategory_DisplaySequence");
		paramaterMap.put(ODataQueryConstants.SELECT,
				"alertcategory_id,alertcategory_status_id,alertcategory_accountLevel,alertcategory_DisplaySequence,alertcategorytext_LanguageCode,alertcategory_Name,alertcategorytext_DisplayName,alertcategorytext_Description");

		// Read Alert Category View
		String readAlertCategoryViewResponse = Executor.invokeService(ServiceURLEnum.ALERTCATEGORY_VIEW_READ, paramaterMap, null, requestInstance);
		JSONObject readAlertCategoryViewResponseJSON = CommonUtilities.getStringAsJSONObject(readAlertCategoryViewResponse);
		if (readAlertCategoryViewResponseJSON == null || !readAlertCategoryViewResponseJSON.has(FabricConstants.OPSTATUS)
				|| readAlertCategoryViewResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
				|| !readAlertCategoryViewResponseJSON.has("alertcategory_view")) {
			// Failed CRUD Operation
			LOG.error("Failed to Read Alert Category View");
			throw new ApplicationException(ErrorCodeEnum.ERR_20915);
		}

		LOG.debug("Read Alert Category View");
		// Parse Response and construct result Record
		JSONArray alertCategoriesJSONArray = readAlertCategoryViewResponseJSON.optJSONArray("alertcategory_view");
		alertCategoriesJSONArray = CommonUtilities.filterRecordsByLocale(alertCategoriesJSONArray, "alertcategorytext_LanguageCode", "alertcategory_id", DEFAULT_LOCALE);

		// Construct Response Record
		LOG.debug("Constructing Response Record");
		if (alertCategoriesJSONArray != null && alertCategoriesJSONArray.length() > 0) {
			JSONObject currJSON;
			if (alertCategoriesJSONArray.optJSONObject(0) != null) {
				currJSON = alertCategoriesJSONArray.optJSONObject(0);
				operationRecord.addParam(new Param("categoryCode", currJSON.optString("alertcategory_id"), FabricConstants.STRING));
				operationRecord.addParam(new Param("categoryName", currJSON.optString("alertcategory_Name"), FabricConstants.STRING));
				operationRecord.addParam(new Param("displayName", currJSON.optString("alertcategorytext_DisplayName"), FabricConstants.STRING));
				operationRecord.addParam(new Param("languageCode", currJSON.optString("alertcategorytext_LanguageCode"), FabricConstants.STRING));
				operationRecord.addParam(new Param("categoryDisplaySequence", currJSON.optString("alertcategory_DisplaySequence"), FabricConstants.STRING));
				operationRecord.addParam(new Param("containsAccountLevelAlerts", currJSON.optString("alertcategory_accountLevel"), FabricConstants.STRING));
				operationRecord.addParam(new Param("categoryDescription", currJSON.optString("alertcategorytext_Description"), FabricConstants.STRING));
				operationRecord.addParam(new Param("categoryStatus", currJSON.optString("alertcategory_status_id"), FabricConstants.STRING));
			}
		}

		LOG.debug("Returning success response");
		return operationRecord;
	}

	/**
	 * Method to fetch the display preferences of Alert Category
	 * 
	 * @param alertCategoryId
	 * @param acceptLanguage
	 * @param requestInstance
	 * @return Display Preferences of Alert Category
	 * @throws ApplicationException
	 */
	private Dataset getAlertCategoryDisplayPreferences(String alertCategoryId, DataControllerRequest requestInstance) throws ApplicationException {

		Dataset operationDataset = new Dataset();
		operationDataset.setId("displayPreferences");

		if (requestInstance == null) {
			// Data Controller Request Instance is NULL. Throw Application Exception.
			LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception.");
			throw new ApplicationException(ErrorCodeEnum.ERR_20898);
		}
		if (StringUtils.isBlank(alertCategoryId)) {
			// Missing Mandatory Input. Return Empty Record
			LOG.error("Alert Category Id value is empty. Returning empty record.");
			return operationDataset;
		}

		// Construct Filter Query
		StringBuffer filterQueryBuffer = new StringBuffer();
		filterQueryBuffer.append("AlertCategoryId eq '" + alertCategoryId + "'");

		// Prepare Input Map
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put(ODataQueryConstants.FILTER, filterQueryBuffer.toString());
		String operationResponse = Executor.invokeService(ServiceURLEnum.DBXALERTCATEGORYTEXT_READ, inputMap, null, requestInstance);
		JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
		if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS) || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
				|| !operationResponseJSON.has("dbxalertcategorytext")) {
			LOG.error("Failed to Read dbxalertcategorytext");
			throw new ApplicationException(ErrorCodeEnum.ERR_20898);
		}
		LOG.debug("Successfully Read dbxalertcategorytext");
		JSONArray alertTypeTextJSONArray = operationResponseJSON.optJSONArray("dbxalertcategorytext");
		if (alertTypeTextJSONArray != null && alertTypeTextJSONArray.length() > 0) {
			JSONObject jsonObject = null;
			for (Object currObject : alertTypeTextJSONArray) {
				jsonObject = (JSONObject) currObject;
				Record currRecord = new Record();
				for (String currKey : jsonObject.keySet()) {
					currRecord.addParam(new Param(currKey, jsonObject.optString(currKey), FabricConstants.STRING));
				}
				operationDataset.addRecord(currRecord);
			}
		}
		return operationDataset;
	}

	/**
	 * Method to Fetch Channels of Alert Category
	 * 
	 * @param alertCategoryId
	 * @param acceptLanguage
	 * @param requestInstance
	 * @return Dataset containing Channels of Alert Category
	 * @throws ApplicationException
	 */
	private Dataset getAlertCategoryChannels(String alertCategoryId, String acceptLanguage, DataControllerRequest requestInstance) throws ApplicationException {
		Dataset categoryChannelsDataset = new Dataset();
		categoryChannelsDataset.setId("categoryChannels");

		if (requestInstance == null) {
			// Data Controller Request Instance is NULL. Throw Application Exception.
			LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception.");
			throw new ApplicationException(ErrorCodeEnum.ERR_20912);
		}
		if (StringUtils.isBlank(alertCategoryId)) {
			// Missing Mandatory Input. Return Empty Record
			LOG.error("Alert Category Id value is empty. Returning empty record.");
			return categoryChannelsDataset;
		}

		// Fetch supported channels of Alert Category
		LOG.debug("Fetching supported channels of Alert Category");
		List<String> supportedChannelsList = AlertManagementHandler.getSupportedChannelsOfAlertCategory(alertCategoryId, requestInstance);
		LOG.debug("Fetched supported channels of Alert Category");

		// Construct input map
		Map<String, String> inputMap = new HashMap<>();
		if (StringUtils.equals(acceptLanguage, DEFAULT_LOCALE)) {
			inputMap.put(ODataQueryConstants.FILTER, "(channeltext_LanguageCode eq '" + acceptLanguage + "')");
		} else {
			inputMap.put(ODataQueryConstants.FILTER, "(channeltext_LanguageCode eq '" + acceptLanguage + "' or channeltext_LanguageCode eq '" + DEFAULT_LOCALE + "')");
		}

		// Fetch all channels and collate supported channel information
		String readChannelViewResponse = Executor.invokeService(ServiceURLEnum.CHANNEL_VIEW_READ, inputMap, null, requestInstance);
		JSONObject readChannelViewResponseJSON = CommonUtilities.getStringAsJSONObject(readChannelViewResponse);

		if (readChannelViewResponseJSON == null || !readChannelViewResponseJSON.has(FabricConstants.OPSTATUS) ||
				readChannelViewResponseJSON.getInt(FabricConstants.OPSTATUS) != 0 || !readChannelViewResponseJSON.has("channel_view")) {
			// Failed CRUD Operation
			LOG.error("Failed to Communication Channels");
			throw new ApplicationException(ErrorCodeEnum.ERR_20912);
		}
		// Successful CRUD Operation
		LOG.debug("Read Communication Channels");

		String currChannelID;
		JSONObject currJSONObject;

		// Parse response and construct result Dataset
		JSONArray channelViewJSONArray = readChannelViewResponseJSON.optJSONArray("channel_view");
		channelViewJSONArray = CommonUtilities.filterRecordsByLocale(channelViewJSONArray, "channeltext_LanguageCode", "channel_id", DEFAULT_LOCALE);
		for (Object currObject : channelViewJSONArray) {
			if (currObject instanceof JSONObject) {
				currJSONObject = (JSONObject) currObject;
				currChannelID = currJSONObject.optString("channel_id");
				Record currRecord = new Record();
				currRecord.addParam(new Param("channelID", currChannelID, FabricConstants.STRING));
				currRecord.addParam(new Param("channelDisplayName", currJSONObject.optString("channeltext_Description"), FabricConstants.STRING));
				currRecord.addParam(new Param("isChannelSupported", String.valueOf(supportedChannelsList.contains(currChannelID)), FabricConstants.STRING));
				categoryChannelsDataset.addRecord(currRecord);
			}
		}

		return categoryChannelsDataset;
	}

	/**
	 * Method to fetch the Alert Types of Alert Category
	 * 
	 * @param alertCategoryId
	 * @param acceptLanguage
	 * @param requestInstance
	 * @return Dataset containing Alert Types
	 * @throws ApplicationException
	 */
	private Dataset getAlertTypesOfAlertCategory(String alertCategoryId, String acceptLanguage, DataControllerRequest requestInstance) throws ApplicationException {
		Dataset alertTypesDataset = new Dataset();
		alertTypesDataset.setId("alertGroups");

		if (requestInstance == null) {
			// Data Controller Request Instance is NULL. Throw Application Exception.
			LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception.");
			throw new ApplicationException(ErrorCodeEnum.ERR_20927);
		}
		if (StringUtils.isBlank(alertCategoryId)) {
			// Missing Mandatory Input. Return Empty Record
			LOG.error("Alert Category Id value is empty. Returning empty record.");
			return alertTypesDataset;
		}

		// Fetch the Alert Types of Alert Category
		LOG.debug("Fetching Alert Types of Alert Category. Alert Category Id:" + alertCategoryId);
		JSONArray alertTypesArray = AlertManagementHandler.getAlertTypesOfAlertCategory(alertCategoryId, acceptLanguage, requestInstance);
		LOG.debug("Fetched Alert Types of Alert Category. Alert Category Id:" + alertCategoryId);

		// Parse Alert Types Data and construct Result Dataset
		if (alertTypesArray != null && alertTypesArray.length() > 0) {
			JSONObject currJSON;
			for (Object currObject : alertTypesArray) {
				if (currObject instanceof JSONObject) {
					Record currAlertTypeRecord = new Record();
					currJSON = (JSONObject) currObject;
					currAlertTypeRecord.addParam(new Param("name", currJSON.optString("alerttype_Name"), FabricConstants.STRING));
					currAlertTypeRecord.addParam(new Param("code", currJSON.optString("alerttype_id"), FabricConstants.STRING));
					currAlertTypeRecord.addParam(new Param("statusId", currJSON.optString("alerttype_Status_id"), FabricConstants.STRING));
					currAlertTypeRecord.addParam(new Param("displayName", currJSON.optString("alerttypetext_DisplayName"), FabricConstants.STRING));
					currAlertTypeRecord.addParam(new Param("languageCode", currJSON.optString("alerttypetext_LanguageCode"), FabricConstants.STRING));
					currAlertTypeRecord.addParam(new Param("displaySequence", currJSON.optString("alerttype_DisplaySequence"), FabricConstants.STRING));
					if (StringUtils.equalsIgnoreCase(currJSON.optString("alerttype_IsGlobal"), "TRUE")
							|| StringUtils.equalsIgnoreCase(currJSON.optString("alerttype_IsGlobal"), "1")) {
						currAlertTypeRecord.addParam(new Param("type", "Global Alert", FabricConstants.STRING));
					} else {
						currAlertTypeRecord.addParam(new Param("type", "User Alert", FabricConstants.STRING));
					}
					alertTypesDataset.addRecord(currAlertTypeRecord);
				}
			}
		}

		return alertTypesDataset;
	}

}
