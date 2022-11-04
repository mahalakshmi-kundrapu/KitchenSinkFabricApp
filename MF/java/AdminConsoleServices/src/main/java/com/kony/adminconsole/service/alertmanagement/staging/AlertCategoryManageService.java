package com.kony.adminconsole.service.alertmanagement.staging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to manage the Alert Category Settings
 * 
 * @author Aditya Mankal
 */
public class AlertCategoryManageService implements JavaService2 {

	private static final String EDIT_ALERT_CATEGORY_METHOD_ID = "editAlertCategory";
	private static final String REORDER_ALERT_CATEGORY_METHOD_ID = "reorderAlertCategory";

	private static final String CATEGORY_ORDER_PARAM = "categoryOrder";
	private static final String CATEGORY_NAME_PARAM = "categoryName";
	private static final String CATEGORY_CODE_PARAM = "categoryCode";
	private static final String CHANNELS_PARAM = "channels";
	private static final String STATUS_ID_PARAM = "statusId";
	private static final String ADDED_DISPLAY_PREFERENCE_PARAM = "addedDisplayPreferences";
	private static final String REMOVED_DISPLAY_PREFERENCE_PARAM = "removedDisplayPreferences";
	private static final String CONTAINS_ACCOUNT_LEVEL_ALERTS_PARAM = "containsAccountLevelAlerts";

	private static final Logger LOG = Logger.getLogger(AlertCategoryManageService.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) {

		Result processedResult = new Result();

		EventEnum eventEnum = EventEnum.UPDATE;
		String activityMessage = StringUtils.EMPTY;

		try {
			LOG.debug("Method Id:" + methodID);
			activityMessage = "Alert Category Id:" + requestInstance.getParameter(CATEGORY_CODE_PARAM);

			// Fetch Logged In User Info
			String loggedInUser = StringUtils.EMPTY;
			UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
			if (userDetailsBeanInstance != null) {
				loggedInUser = userDetailsBeanInstance.getUserId();
			}

			if (StringUtils.equalsIgnoreCase(methodID, EDIT_ALERT_CATEGORY_METHOD_ID)) {
				eventEnum = EventEnum.UPDATE;
				processedResult = editAlertCategory(requestInstance, loggedInUser);
				processedResult.addParam(new Param("status", "success", FabricConstants.STRING));

			} else if (StringUtils.equalsIgnoreCase(methodID, REORDER_ALERT_CATEGORY_METHOD_ID)) {
				eventEnum = EventEnum.UPDATE;
				processedResult = reorderAlertCategory(requestInstance, loggedInUser);
				processedResult.addParam(new Param("status", "success", FabricConstants.STRING));

			} else {
				// Unsupported Method Id. Return Empty Result
				LOG.debug("Unsupported Method Id. Returning Empty Result");
				return new Result();
			}

			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, eventEnum,
					ActivityStatusEnum.SUCCESSFUL, activityMessage);

		} catch (ApplicationException e) {
			LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);

			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, eventEnum,
					ActivityStatusEnum.FAILED, activityMessage);

			Result errorResult = new Result();
			e.getErrorCodeEnum().setErrorCode(errorResult);
			errorResult.addParam(new Param("status", "failure", FabricConstants.STRING));
			return errorResult;

		} catch (Exception e) {
			LOG.debug("Runtime Exception.Exception Trace:", e);

			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, eventEnum,
					ActivityStatusEnum.FAILED, activityMessage);

			Result errorResult = new Result();
			errorResult.addParam(new Param("status", "failure", FabricConstants.STRING));
			ErrorCodeEnum.ERR_20921.setErrorCode(errorResult);
			return errorResult;
		}
		return processedResult;
	}

	/**
	 * Method to edit the ALert Category
	 * 
	 * @param requestInstance
	 * @return operation Result
	 * @throws ApplicationException
	 */
	private Result editAlertCategory(DataControllerRequest requestInstance, String loggedInUser)
			throws ApplicationException {

		if (requestInstance == null) {
			LOG.error("DataControllerRequest Instance is NULL. Returning Error Response");
			throw new ApplicationException(ErrorCodeEnum.ERR_20908);
		}

		Result operationResult = new Result();

		// Read Inputs
		String categoryName = requestInstance.getParameter(CATEGORY_NAME_PARAM);
		String categoryCode = requestInstance.getParameter(CATEGORY_CODE_PARAM);
		String channels = requestInstance.getParameter(CHANNELS_PARAM);
		String containsAccountLevelAlerts = requestInstance.getParameter(CONTAINS_ACCOUNT_LEVEL_ALERTS_PARAM);
		String statusId = requestInstance.getParameter(STATUS_ID_PARAM);
		String addedDisplayPreference = requestInstance.getParameter(ADDED_DISPLAY_PREFERENCE_PARAM);
		String removedDisplayPreference = requestInstance.getParameter(REMOVED_DISPLAY_PREFERENCE_PARAM);

		// Validate Inputs
		if (StringUtils.isBlank(categoryCode)) {
			LOG.error("Missing Mandatory Input: AlertCategoryCode");
			ErrorCodeEnum.ERR_20920.setErrorCode(operationResult);
			return operationResult;
		}

		// Edit Alert Category Definition
		LOG.debug("Setting Alert Category Definition...");
		Record editAlerCategoryDefinitionRecord = setAlertCategoryDefinition(categoryCode, categoryName, statusId,
				containsAccountLevelAlerts, loggedInUser, requestInstance);
		operationResult.addRecord(editAlerCategoryDefinitionRecord);
		LOG.debug("Alert Category Definition Set");

		// Edit Alert Category Channels
		JSONObject channelsJSON = CommonUtilities.getStringAsJSONObject(channels);
		if (channelsJSON != null) {
			List<String> supportedChannelsList = new ArrayList<>();
			List<String> unSupportedChannelsList = new ArrayList<>();
			for (String key : channelsJSON.keySet()) {
				if (StringUtils.equalsIgnoreCase(channelsJSON.optString(key), "TRUE")) {
					supportedChannelsList.add(key);
				} else if (StringUtils.equalsIgnoreCase(channelsJSON.optString(key), "FALSE")) {
					unSupportedChannelsList.add(key);
				}
			}
			LOG.debug("Setting Alert Category Channels...");
			Record editAlertCategoryChannels = setAlertCategoryChannels(categoryCode, supportedChannelsList,
					unSupportedChannelsList, loggedInUser, requestInstance);
			operationResult.addRecord(editAlertCategoryChannels);
			LOG.debug("Alert Category Channels Set");
		}

		// Edit Alert Category Text
		LOG.debug("Setting Alert Category Display Preferences...");
		Record setAlertCategoryDisplayPreferencesRecord = setAlertCategoryDisplayPreferences(categoryCode,
				addedDisplayPreference, removedDisplayPreference, loggedInUser, requestInstance);
		operationResult.addRecord(setAlertCategoryDisplayPreferencesRecord);
		LOG.debug("Alert Category Display Preferences Set");

		return operationResult;

	}

	/**
	 * Method to set the Alert Category Channel Texts
	 * 
	 * @param categoryCode
	 * @param addedDisplayPreference
	 * @param removedDisplayPreference
	 * @param loggedInUserId
	 * @param requestInstance
	 * @return operation Record
	 * @throws ApplicationException
	 */
	private Record setAlertCategoryDisplayPreferences(String categoryCode, String addedDisplayPreference,
			String removedDisplayPreference, String loggedInUserId, DataControllerRequest requestInstance)
			throws ApplicationException {

		if (requestInstance == null) {
			LOG.error("DataControllerRequest Instance is NULL. Returning Error Response");
			throw new ApplicationException(ErrorCodeEnum.ERR_20903);
		}

		Record categoryTextsRecord = new Record();
		categoryTextsRecord.setId("categoryTexts");

		String currOperationResponse = StringUtils.EMPTY, currLocale = StringUtils.EMPTY;
		JSONObject currOperationResponseJSON;

		JSONObject currJSON = null;
		Map<String, String> inputMap = new HashMap<>();

		// Fetch Existing Association
		inputMap.put(ODataQueryConstants.FILTER, "AlertCategoryId eq '" + categoryCode + "'");
		inputMap.put(ODataQueryConstants.SELECT, "LanguageCode");
		currOperationResponse = Executor.invokeService(ServiceURLEnum.DBXALERTCATEGORYTEXT_READ, inputMap, null,
				requestInstance);
		currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
		if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
				|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
			LOG.error("Failed CRUD Operation:" + ServiceURLEnum.DBXALERTCATEGORYTEXT_READ.name());
			throw new ApplicationException(ErrorCodeEnum.ERR_20903);
		}
		LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.DBXALERTCATEGORYTEXT_READ.name());
		Set<String> associatedDisplayPreferencesSet = new HashSet<>();
		JSONArray associatedDisplayPreferencesJSONArray = currOperationResponseJSON
				.optJSONArray("dbxalertcategorytext");
		if (associatedDisplayPreferencesJSONArray != null && associatedDisplayPreferencesJSONArray.length() > 0) {
			for (Object currObj : associatedDisplayPreferencesJSONArray) {
				if (currObj instanceof JSONObject) {
					currJSON = (JSONObject) currObj;
					if (currJSON.has("LanguageCode")) {
						associatedDisplayPreferencesSet.add(currJSON.optString("LanguageCode"));
					}
				}
			}
		}

		// Handle Removed Display Preferences
		inputMap.clear();
		inputMap.put("AlertCategoryId", categoryCode);
		JSONArray removedDisplayPreferencesJSONArray = CommonUtilities.getStringAsJSONArray(removedDisplayPreference);
		if (removedDisplayPreferencesJSONArray != null && removedDisplayPreferencesJSONArray.length() > 0) {
			for (Object currObject : removedDisplayPreferencesJSONArray) {
				if (currObject instanceof String) {
					currLocale = (String) currObject;
					if (associatedDisplayPreferencesSet.contains(currLocale)) {
						LOG.debug("Removing Display Preference for Locale:" + currLocale);
						inputMap.put("LanguageCode", currLocale);
						currOperationResponse = Executor.invokeService(ServiceURLEnum.DBXALERTCATEGORYTEXT_DELETE,
								inputMap, null, requestInstance);
						currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
						if (currOperationResponseJSON == null
								|| !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
								|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
							LOG.error("Failed CRUD Operation:" + ServiceURLEnum.DBXALERTCATEGORYTEXT_DELETE.name());
							throw new ApplicationException(ErrorCodeEnum.ERR_20903);
						}
						LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.DBXALERTCATEGORYTEXT_DELETE.name());
						associatedDisplayPreferencesSet.remove(currLocale);
					}
					categoryTextsRecord.addParam(new Param(currLocale, "Text Removed", FabricConstants.STRING));
				}
			}
		}

		// Handle Added Display Preferences
		JSONArray addedDisplayPreferencesJSONArray = CommonUtilities.getStringAsJSONArray(addedDisplayPreference);
		if (addedDisplayPreferencesJSONArray != null && addedDisplayPreferencesJSONArray.length() > 0) {

			for (Object currObject : addedDisplayPreferencesJSONArray) {
				if (currObject instanceof JSONObject) {
					currJSON = (JSONObject) currObject;
					if (currJSON.has("languageCode") && currJSON.has("displayName") && currJSON.has("description")) {

						inputMap.clear();
						currLocale = currJSON.optString("languageCode");
						inputMap.put("AlertCategoryId", categoryCode);
						inputMap.put("LanguageCode", currLocale);
						inputMap.put("DisplayName", currJSON.optString("displayName"));
						inputMap.put("Description", currJSON.optString("description"));

						if (associatedDisplayPreferencesSet.contains(currLocale)) {
							LOG.debug("Updating Display Preference for Locale:" + currLocale);
							inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
							inputMap.put("updatedby", loggedInUserId);
							currOperationResponse = Executor.invokeService(ServiceURLEnum.DBXALERTCATEGORYTEXT_UPDATE,
									inputMap, null, requestInstance);
							currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
						} else {
							LOG.debug("Creating Display Preference for Locale:" + currLocale);
							inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
							inputMap.put("createdby", loggedInUserId);
							currOperationResponse = Executor.invokeService(ServiceURLEnum.DBXALERTCATEGORYTEXT_CREATE,
									inputMap, null, requestInstance);
							currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
						}
						associatedDisplayPreferencesSet.add(currJSON.optString("languageCode"));

						if (currOperationResponseJSON == null
								|| !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
								|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
							throw new ApplicationException(ErrorCodeEnum.ERR_20904);
						}
						LOG.debug("Successful CRUD Operation");
						categoryTextsRecord.addParam(new Param(currLocale, "Text Added", FabricConstants.STRING));
					}
				}
			}
		}

		return categoryTextsRecord;
	}

	/**
	 * Method to Set Alert Category Channels
	 * 
	 * @param categoryCode
	 * @param supportedChannelsList
	 * @param unSupportedChannelsList
	 * @param loggedInUserId
	 * @param requestInstance
	 * @return operation Record
	 * @throws ApplicationException
	 */
	private Record setAlertCategoryChannels(String categoryCode, List<String> supportedChannelsList,
			List<String> unSupportedChannelsList, String loggedInUserId, DataControllerRequest requestInstance)
			throws ApplicationException {

		if (requestInstance == null) {
			LOG.error("DataControllerRequest Instance is NULL. Returning Error Response");
			throw new ApplicationException(ErrorCodeEnum.ERR_20906);
		}

		Record setAlertCategoryChannelsRecord = new Record();
		setAlertCategoryChannelsRecord.setId("categoryChannels");

		Map<String, String> inputMap = new HashMap<>();

		JSONObject currOperationResponseJSON, currJSON;
		String currOperationResponse = StringUtils.EMPTY, timestamp = StringUtils.EMPTY;

		// Fetch Existing Association
		Set<String> associatedChannelsSet = new HashSet<>();
		inputMap.put(ODataQueryConstants.FILTER, "AlertCategoryId eq '" + categoryCode + "'");
		inputMap.put(ODataQueryConstants.SELECT, "ChannelID");
		currOperationResponse = Executor.invokeService(ServiceURLEnum.ALERTCATEGORYCHANNEL_READ, inputMap, null,
				requestInstance);
		currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
		if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
				|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
				|| !currOperationResponseJSON.has("alertcategorychannel")) {
			LOG.error("Failed CRUD Operation:" + ServiceURLEnum.ALERTCATEGORYCHANNEL_READ.name());
			throw new ApplicationException(ErrorCodeEnum.ERR_20906);
		}
		LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.ALERTCATEGORYCHANNEL_READ.name());
		JSONArray alertCategoryChannelsJSONArray = currOperationResponseJSON.optJSONArray("alertcategorychannel");
		for (Object currObj : alertCategoryChannelsJSONArray) {
			if (currObj instanceof JSONObject) {
				currJSON = (JSONObject) currObj;
				if (currJSON.has("ChannelID")) {
					associatedChannelsSet.add(currJSON.optString("ChannelID"));
				}
			}
		}

		// Handle Supported Channels
		if (supportedChannelsList != null && !supportedChannelsList.isEmpty()) {
			inputMap.clear();
			inputMap.put("AlertCategoryId", categoryCode);
			inputMap.put("createdby", loggedInUserId);
			for (String channelId : supportedChannelsList) {
				if (!associatedChannelsSet.contains(channelId)) {
					LOG.debug("Adding Support for Channel:" + channelId);
					timestamp = CommonUtilities.getISOFormattedLocalTimestamp();
					inputMap.put("createdts", timestamp);
					inputMap.put("ChannelID", channelId);
					currOperationResponse = Executor.invokeService(ServiceURLEnum.ALERTCATEGORYCHANNEL_CREATE, inputMap,
							null, requestInstance);
					currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
					if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
							|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						LOG.error("Failed CRUD Operation:" + ServiceURLEnum.ALERTCATEGORYCHANNEL_CREATE.name());
						throw new ApplicationException(ErrorCodeEnum.ERR_20906);
					}
					LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.ALERTCATEGORYCHANNEL_CREATE.name());
				}
				setAlertCategoryChannelsRecord
						.addParam(new Param(channelId, String.valueOf(true), FabricConstants.STRING));
				associatedChannelsSet.add(channelId);
			}
		}

		// Handle Removed Channels
		if (unSupportedChannelsList != null && !unSupportedChannelsList.isEmpty()) {
			inputMap.clear();
			inputMap.put("AlertCategoryId", categoryCode);
			for (String channelId : unSupportedChannelsList) {
				if (associatedChannelsSet.contains(channelId)) {
					LOG.debug("Removing Support for Channel:" + channelId);
					inputMap.put("ChannelID", channelId);
					currOperationResponse = Executor.invokeService(ServiceURLEnum.ALERTCATEGORYCHANNEL_DELETE, inputMap,
							null, requestInstance);
					currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
					if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
							|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						LOG.error("Failed CRUD Operation:" + ServiceURLEnum.ALERTCATEGORYCHANNEL_DELETE.name());
						throw new ApplicationException(ErrorCodeEnum.ERR_20905);
					}
					LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.ALERTCATEGORYCHANNEL_DELETE.name());
					associatedChannelsSet.remove(channelId);
				}
				setAlertCategoryChannelsRecord
						.addParam(new Param(channelId, String.valueOf(false), FabricConstants.STRING));
			}
		}

		return setAlertCategoryChannelsRecord;
	}

	/**
	 * Method to set the Alert Category Definition
	 * 
	 * @param categoryCode
	 * @param categoryName
	 * @param statusId
	 * @param containsAccountLevelAlerts
	 * @param loggedInUserId
	 * @param requestInstance
	 * @return operation Record
	 * @throws ApplicationException
	 */
	private Record setAlertCategoryDefinition(String categoryCode, String categoryName, String statusId,
			String containsAccountLevelAlerts, String loggedInUserId, DataControllerRequest requestInstance)
			throws ApplicationException {

		Record setAlertCategoryRecord = new Record();
		setAlertCategoryRecord.setId("setAlertCategoryDefinition");

		boolean isCreateMode = true;
		if (StringUtils.isBlank(categoryCode)) {
			// Consider as Create Mode when CategroyCode is Empty
			isCreateMode = true;
			categoryCode = CommonUtilities.getNewId().toString();
		} else {
			// Consider as Update Mode when CategroyCode is Non-Empty
			isCreateMode = false;
		}
		LOG.debug("isCreateMode:" + String.valueOf(isCreateMode));

		if (requestInstance == null) {
			LOG.error("DataControllerRequest Instance is NULL. Returning Error Response");
			if (isCreateMode == true) {
				throw new ApplicationException(ErrorCodeEnum.ERR_20907);
			} else {
				throw new ApplicationException(ErrorCodeEnum.ERR_20908);
			}
		}

		// Set Input Map
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put("id", categoryCode);
		if (StringUtils.isNotBlank(categoryName)) {
			inputMap.put("Name", categoryName);
		}
		if (StringUtils.isNotBlank(statusId)) {
			inputMap.put("status_id", statusId);
		}
		if (StringUtils.equalsIgnoreCase(containsAccountLevelAlerts, "TRUE")) {
			inputMap.put("accountLevel", "1");
		} else if (StringUtils.equalsIgnoreCase(containsAccountLevelAlerts, "FALSE")) {
			inputMap.put("accountLevel", "0");
		}

		// Create/Update Alert Category
		String setAlertCategoryResponse = StringUtils.EMPTY;
		String timestamp = CommonUtilities.getISOFormattedLocalTimestamp();
		if (isCreateMode) {
			inputMap.put("createdts", timestamp);
			inputMap.put("createdby", loggedInUserId);
			setAlertCategoryResponse = Executor.invokeService(ServiceURLEnum.DBXALERTCATEGORY_CREATE, inputMap, null,
					requestInstance);
		} else {
			inputMap.put("lastmodifiedts", timestamp);
			inputMap.put("modifiedby", loggedInUserId);
			setAlertCategoryResponse = Executor.invokeService(ServiceURLEnum.DBXALERTCATEGORY_UPDATE, inputMap, null,
					requestInstance);
		}
		JSONObject setAlertCategoryResponseJSON = CommonUtilities.getStringAsJSONObject(setAlertCategoryResponse);
		if (setAlertCategoryResponseJSON == null || !setAlertCategoryResponseJSON.has(FabricConstants.OPSTATUS)
				|| setAlertCategoryResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
			if (isCreateMode) {
				LOG.error("Failed CRUD Operation:" + ServiceURLEnum.DBXALERTCATEGORY_CREATE.name());
				throw new ApplicationException(ErrorCodeEnum.ERR_20907);
			} else {
				LOG.error("Failed CRUD Operation:" + ServiceURLEnum.DBXALERTCATEGORY_UPDATE.name());
				throw new ApplicationException(ErrorCodeEnum.ERR_20908);
			}
		}
		LOG.debug("Successful CRUD Operation");

		setAlertCategoryRecord.addParam(new Param("status", "Success", FabricConstants.STRING));
		return setAlertCategoryRecord;
	}

	/**
	 * Method to set the Alert Category Order
	 * 
	 * @param requestInstance
	 * @param loggedInUserId
	 * @return operation Result
	 * @throws ApplicationException
	 */
	private Result reorderAlertCategory(DataControllerRequest requestInstance, String loggedInUserId)
			throws ApplicationException {

		if (requestInstance == null) {
			LOG.error("DataControllerRequest Instance is NULL. Returning Error Response");
			throw new ApplicationException(ErrorCodeEnum.ERR_20908);
		}
		Result operationResult = new Result();

		// Read Input
		String categoryOrder = requestInstance.getParameter(CATEGORY_ORDER_PARAM);

		// Validate Input
		JSONObject categoryOrderJSONObject = null;
		if (StringUtils.isNotBlank(categoryOrder)) {
			try {
				categoryOrderJSONObject = new JSONObject(categoryOrder);
			} catch (NullPointerException | JSONException e) {
				// Malformed Alert Category Channel Preference JSON
				LOG.error("Malformed Alert Category Order JSON");
				ErrorCodeEnum.ERR_20928.setErrorCode(operationResult);
				return operationResult;
			}
		}

		// Set Alert Category Order
		String currCategoryId = StringUtils.EMPTY, currOperationResponse = StringUtils.EMPTY;
		JSONObject currOperationResponseJSON = null;
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put("modifiedby", loggedInUserId);
		int currCategoryDisplaySequence = 0;
		if (categoryOrderJSONObject != null) {
			for (String key : categoryOrderJSONObject.keySet()) {
				if (categoryOrderJSONObject.opt(key) instanceof Integer) {
					currCategoryId = key;
					currCategoryDisplaySequence = categoryOrderJSONObject.optInt(key);
					inputMap.put("id", currCategoryId);
					inputMap.put("DisplaySequence", String.valueOf(currCategoryDisplaySequence));
					inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
					currOperationResponse = Executor.invokeService(ServiceURLEnum.DBXALERTCATEGORY_UPDATE, inputMap, null,
							requestInstance);
					currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
					if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
							|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						LOG.error("Failed CRUD Operation:" + ServiceURLEnum.DBXALERTCATEGORY_UPDATE.name());
						throw new ApplicationException(ErrorCodeEnum.ERR_20908);
					}
					LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.DBXALERTCATEGORY_UPDATE.name());

				}
			}
		}
		return operationResult;
	}

}
