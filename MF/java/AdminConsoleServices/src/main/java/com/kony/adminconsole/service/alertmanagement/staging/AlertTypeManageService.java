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
import com.kony.adminconsole.handler.AlertManagementHandler;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.kony.adminconsole.utilities.StatusEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to manage the Alert Type Settings
 * 
 * @author Aditya Mankal
 */
public class AlertTypeManageService implements JavaService2 {

	private static final String REORDER_ALERT_TYPE_METHOD_ID = "reorderAlertType";
	private static final String CREATE_ALERT_TYPE_METHOD_ID = "createAlertType";
	private static final String EDIT_ALERT_TYPE_METHOD_ID = "editAlertType";

	private static final String ALERT_NAME_PARAM = "alertName";
	private static final String ALERT_CODE_PARAM = "alertCode";
	private static final String STATUS_ID_PARAM = "statusId";

	private static final String ATTRIBUTE_ID_PARAM = "attributeId";
	private static final String CONDITION_ID_PARAM = "conditionId";
	private static final String VALUE_1_PARAM = "value1";
	private static final String VALUE_2_PARAM = "value2";

	private static final String IS_GLOBAL_ALERT_PARAM = "isGlobalAlert";
	private static final String APP_PREFERENCES_PARAM = "appPreferences";
	private static final String USER_TYPE_PREFERENCES_PARAM = "userTypePreferences";
	private static final String ACCOUNT_TYPE_PREFERENCES_PARAM = "accountTypePreference";
	private static final String ADDED_DISPLAY_PREFERENCE_PARAM = "addedDisplayPreferences";
	private static final String REMOVED_DISPLAY_PREFERENCE_PARAM = "removedDisplayPreferences";

	private static final String TYPE_ORDER_PARAM = "typeOrder";
	private static final String CATEGORY_CODE_PARAM = "alertCategoryCode";

	private static final int ALERT_NAME_MIN_LENGTH = 5;
	private static final int ALERT_NAME_MAX_LENGTH = 50;

	private static final Logger LOG = Logger.getLogger(AlertTypeManageService.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) {

		Result processedResult = new Result();

		EventEnum eventEnum = EventEnum.UPDATE;
		String activityMessage = StringUtils.EMPTY;

		try {

			LOG.debug("Method Id:" + methodID);
			activityMessage = "Alert Type Id:" + requestInstance.getParameter(ALERT_CODE_PARAM);

			// Fetch Logged In User Info
			String loggedInUser = StringUtils.EMPTY;
			UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
			if (userDetailsBeanInstance != null) {
				loggedInUser = userDetailsBeanInstance.getUserId();
			}

			if (StringUtils.equalsIgnoreCase(methodID, REORDER_ALERT_TYPE_METHOD_ID)) {
				processedResult = reorderAlertType(requestInstance, loggedInUser);
				eventEnum = EventEnum.UPDATE;
			}

			else if (StringUtils.equalsIgnoreCase(methodID, CREATE_ALERT_TYPE_METHOD_ID)) {
				processedResult = setAlertType(requestInstance, true, loggedInUser);
				eventEnum = EventEnum.CREATE;
			}

			else if (StringUtils.equalsIgnoreCase(methodID, EDIT_ALERT_TYPE_METHOD_ID)) {
				processedResult = setAlertType(requestInstance, false, loggedInUser);
				eventEnum = EventEnum.UPDATE;
			}

			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, eventEnum,
					ActivityStatusEnum.SUCCESSFUL, activityMessage);

		} catch (ApplicationException e) {
			LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);

			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, eventEnum,
					ActivityStatusEnum.FAILED, activityMessage);

			Result errorResult = new Result();
			errorResult.addParam(new Param("status", "failure", FabricConstants.STRING));
			e.getErrorCodeEnum().setErrorCode(errorResult);
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
	 * Method to Create/Edit Alert Type
	 * 
	 * @param requestInstance
	 * @param isCreateMode
	 * @param loggedInUserId
	 * @return operation Result
	 * @throws ApplicationException
	 */
	private Result setAlertType(DataControllerRequest requestInstance, boolean isCreateMode, String loggedInUserId)
			throws ApplicationException {

		Result operationResult = new Result();

		LOG.debug("isCreateMode" + isCreateMode);

		// Alert Category Code
		String categoryCode = requestInstance.getParameter(CATEGORY_CODE_PARAM);
		if (StringUtils.isBlank(categoryCode)) {
			LOG.error("Missing Mandatory Input: AlertCategoryCode");
			ErrorCodeEnum.ERR_20920.setErrorCode(operationResult);
			operationResult.addParam(new Param("status", "failure", FabricConstants.STRING));
			return operationResult;
		}

		// Alert Name
		String alertName = requestInstance.getParameter(ALERT_NAME_PARAM);
		if (isCreateMode == true || (StringUtils.isNotBlank(alertName))) {
			if (StringUtils.length(alertName) < ALERT_NAME_MIN_LENGTH
					|| StringUtils.length(alertName) > ALERT_NAME_MAX_LENGTH) {
				LOG.error("Invalid Input: AlertName");
				operationResult
						.addParam(new Param("message", "Alert Name should have a minimum of " + ALERT_NAME_MIN_LENGTH
								+ " characters and a maximum of " + ALERT_NAME_MAX_LENGTH + " characters"));
				operationResult.addParam(new Param("status", "failure", FabricConstants.STRING));
				ErrorCodeEnum.ERR_20901.setErrorCode(operationResult);
				return operationResult;
			}
		}

		// Alert Code - Mandatory Input from Client. Code to be chosen from a pre-populated list
		String alertCode = requestInstance.getParameter(ALERT_CODE_PARAM);
		if (StringUtils.isBlank(alertCode)) {
			LOG.error("Invalid Input: AlertCode");
			operationResult.addParam(new Param("message", "Alert Code cannot be empty"));
			ErrorCodeEnum.ERR_20901.setErrorCode(operationResult);
			operationResult.addParam(new Param("status", "failure", FabricConstants.STRING));
			return operationResult;
		}
		// Check if an Alert with the given alert code already exists
		if (isCreateMode == true) {
			boolean isAlertCodeAvaialble = AlertManagementHandler.isAlertCodeAvaialable(alertCode, requestInstance);
			if (isAlertCodeAvaialble == false) {
				LOG.error("Alert with the Code :" + alertCode + " has already been defined");
				ErrorCodeEnum.ERR_20892.setErrorCode(operationResult);
				operationResult.addParam(new Param("status", "failure", FabricConstants.STRING));
				return operationResult;
			}
		}

		// Status Id
		String statusId = requestInstance.getParameter(STATUS_ID_PARAM);
		if (isCreateMode == true || (StringUtils.isNotBlank(statusId))) {
			if (!StatusEnum.isValidStatusCode(statusId)) {
				LOG.error("Invalid Input: statusId");
				operationResult.addParam(new Param("message", "Invalid Status Id"));
				ErrorCodeEnum.ERR_20901.setErrorCode(operationResult);
				operationResult.addParam(new Param("status", "failure", FabricConstants.STRING));
				return operationResult;
			}
		}

		// Is Global Alert Flag
		String isGlobalAlertStr = requestInstance.getParameter(IS_GLOBAL_ALERT_PARAM);
		if (isCreateMode == true || (StringUtils.isNotBlank(isGlobalAlertStr))) {
			if (!(StringUtils.equalsIgnoreCase(isGlobalAlertStr, "TRUE")
					|| StringUtils.equalsIgnoreCase(isGlobalAlertStr, "FALSE"))) {
				LOG.error("Invalid Input: isGlobalAlert");
				operationResult.addParam(new Param("message", "Invalid IsGlobalAlert Flag"));
				ErrorCodeEnum.ERR_20901.setErrorCode(operationResult);
				operationResult.addParam(new Param("status", "failure", FabricConstants.STRING));
				return operationResult;
			}
		}

		// Condition Id
		String conditionId = requestInstance.getParameter(CONDITION_ID_PARAM);

		// Attribute Id
		String attributeId = requestInstance.getParameter(ATTRIBUTE_ID_PARAM);

		// Value 1
		String value1 = requestInstance.getParameter(VALUE_1_PARAM);

		// Value 2
		String value2 = requestInstance.getParameter(VALUE_2_PARAM);

		Map<String, String> inputMap = new HashMap<>();
		inputMap.put("id", alertCode);
		inputMap.put("AlertCategoryId", categoryCode);

		if (StringUtils.isNotBlank(alertName)) {
			inputMap.put("Name", alertName);
		}

		if (StringUtils.isNotBlank(statusId)) {
			inputMap.put("Status_id", statusId);
		}

		if (StringUtils.equalsIgnoreCase(isGlobalAlertStr, String.valueOf(true))) {
			inputMap.put("IsGlobal", "1");
		} else if (StringUtils.equalsIgnoreCase(isGlobalAlertStr, String.valueOf(false))) {
			inputMap.put("IsGlobal", "0");
		}

		if (StringUtils.isNotBlank(attributeId)) {
			inputMap.put("AttributeId", attributeId);
		} else {
			inputMap.put("AttributeId", "NULL");
		}

		if (StringUtils.isNotBlank(conditionId)) {
			inputMap.put("AlertConditionId", conditionId);
		} else {
			inputMap.put("AlertConditionId", "NULL");
		}

		if (StringUtils.isNotBlank(value1)) {
			inputMap.put("Value1", value1);
		} else {
			inputMap.put("Value1", "NULL");
		}

		if (StringUtils.isNotBlank(value2)) {
			inputMap.put("Value2", value2);
		} else {
			inputMap.put("Value2", "NULL");
		}

		if (isCreateMode == true) {
			inputMap.put("DisplaySequence", "0");
		}

		String operationResponse;
		if (isCreateMode == true) {
			inputMap.put("createdby", loggedInUserId);
			inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
			operationResponse = Executor.invokeService(ServiceURLEnum.DBXALERTTYPE_CREATE, inputMap, null,
					requestInstance);
		} else {
			inputMap.put("modifiedby", loggedInUserId);
			inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
			operationResponse = Executor.invokeService(ServiceURLEnum.DBXALERTTYPE_UPDATE, inputMap, null,
					requestInstance);
		}
		JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
		if (operationResponseJSON == null || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
			operationResult.addParam(new Param("alertTypeDefinition", String.valueOf(false), FabricConstants.STRING));
			if (isCreateMode == true) {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.CREATE,
						ActivityStatusEnum.FAILED, "AlertType Create Failed");
				ErrorCodeEnum.ERR_20893.setErrorCode(operationResult);
			} else {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
						ActivityStatusEnum.FAILED, "AlertType Update Failed");
				ErrorCodeEnum.ERR_20902.setErrorCode(operationResult);
			}

			operationResult.addParam(new Param("message", operationResponse, FabricConstants.STRING));
			return operationResult;
		}

		operationResult.addParam(new Param("alertTypeDefinition", String.valueOf(true), FabricConstants.STRING));

		// Edit Alert Type Display Preferences
		String addedDisplayPreferencesStr = requestInstance.getParameter(ADDED_DISPLAY_PREFERENCE_PARAM);
		String removedDisplayPreferencesStr = requestInstance.getParameter(REMOVED_DISPLAY_PREFERENCE_PARAM);
		Record displayPreferencesRecord = setAlertTypeDisplayPreferences(alertCode, addedDisplayPreferencesStr,
				removedDisplayPreferencesStr, loggedInUserId, requestInstance);
		operationResult.addRecord(displayPreferencesRecord);

		// Edit Alert Type App Preferences
		String appPreferencesStr = requestInstance.getParameter(APP_PREFERENCES_PARAM);
		JSONObject appPreferencesJSON = CommonUtilities.getStringAsJSONObject(appPreferencesStr);
		if (appPreferencesJSON != null) {
			List<String> supportedAppsList = new ArrayList<>();
			List<String> unSupportedAppsList = new ArrayList<>();
			for (String key : appPreferencesJSON.keySet()) {
				if (StringUtils.equalsIgnoreCase(appPreferencesJSON.optString(key), "TRUE")) {
					supportedAppsList.add(key);
				} else if (StringUtils.equalsIgnoreCase(appPreferencesJSON.optString(key), "FALSE")) {
					unSupportedAppsList.add(key);
				}
			}
			LOG.debug("Setting Alert Type App Preferences...");
			Record editAlertTypeAppsRecord = setAlertAppPreferences(alertCode, supportedAppsList, unSupportedAppsList,
					loggedInUserId, requestInstance);
			operationResult.addRecord(editAlertTypeAppsRecord);
			LOG.debug("Alert Type App Preferences Set");
		}

		// Edit Alert Type User Type Preferences
		String userTypePreferences = requestInstance.getParameter(USER_TYPE_PREFERENCES_PARAM);
		JSONObject userTypePreferencesJSON = CommonUtilities.getStringAsJSONObject(userTypePreferences);
		if (userTypePreferencesJSON != null) {
			List<String> supportedUserTypesList = new ArrayList<>();
			List<String> unSupportedUserTypesList = new ArrayList<>();
			for (String key : userTypePreferencesJSON.keySet()) {
				if (StringUtils.equalsIgnoreCase(userTypePreferencesJSON.optString(key), "TRUE")) {
					supportedUserTypesList.add(key);
				} else if (StringUtils.equalsIgnoreCase(userTypePreferencesJSON.optString(key), "FALSE")) {
					unSupportedUserTypesList.add(key);
				}
			}
			LOG.debug("Setting Alert Type User Type Preferences...");
			Record editAlertTypeUserTypesRecord = setAlertUserTypePreferences(alertCode, supportedUserTypesList,
					unSupportedUserTypesList, loggedInUserId, requestInstance);
			operationResult.addRecord(editAlertTypeUserTypesRecord);
			LOG.debug("Alert Alert Type User Type preferences set");
		}
		
		// Edit Alert Type Account Type Preferences
		String accountTypePreferences = requestInstance.getParameter(ACCOUNT_TYPE_PREFERENCES_PARAM);
		JSONObject accountTypePreferencesJSON = CommonUtilities.getStringAsJSONObject(accountTypePreferences);
		if (accountTypePreferencesJSON != null) {
			List<String> supportedAccountTypesList = new ArrayList<>();
			List<String> unSupportedAccountTypesList = new ArrayList<>();
			for (String key : accountTypePreferencesJSON.keySet()) {
				if (StringUtils.equalsIgnoreCase(accountTypePreferencesJSON.optString(key), "TRUE")) {
					supportedAccountTypesList.add(key);
				} else if (StringUtils.equalsIgnoreCase(accountTypePreferencesJSON.optString(key), "FALSE")) {
					unSupportedAccountTypesList.add(key);
				}
			}
			LOG.debug("Setting Alert Type Account Type Preferences...");
			Record editAlertTypeUserTypesRecord = setAccountTypePreferences(alertCode, supportedAccountTypesList,
					unSupportedAccountTypesList, loggedInUserId, requestInstance);
			operationResult.addRecord(editAlertTypeUserTypesRecord);
			LOG.debug("Alert Alert Type Account Type preferences set");
		}

		return operationResult;

	}

	/**
	 * Method to set account type support of Alert Type
	 * 
	 * @param alertTypeCode
	 * @param supportedAccountTypesList
	 * @param unSupportedAccountTypesList
	 * @param loggedInUserId
	 * @param requestInstance
	 * @return operation Record
	 * @throws ApplicationException
	 */
	private Record setAccountTypePreferences(String alertTypeCode, List<String> supportedAccountTypesList,
			List<String> unSupportedAccountTypesList, String loggedInUserId, DataControllerRequest requestInstance)
			throws ApplicationException {

		if (requestInstance == null) {
			LOG.error("DataControllerRequest Instance is NULL. Returning Error Response");
			throw new ApplicationException(ErrorCodeEnum.ERR_20902);
		}

		Record setAlertTypeAccountRecord = new Record();
		setAlertTypeAccountRecord.setId("accountTypeSettings");

		Map<String, String> inputMap = new HashMap<>();
		JSONObject currOperationResponseJSON, currJSON;
		String currOperationResponse = StringUtils.EMPTY, timestamp = StringUtils.EMPTY;

		// Fetch Existing Association
		Set<String> associatedAccountTypesSet = new HashSet<>();
		inputMap.put(ODataQueryConstants.FILTER, "AlertTypeId eq '" + alertTypeCode + "'");
		inputMap.put(ODataQueryConstants.SELECT, "AccountTypeId");
		currOperationResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPEACCOUNTTYPE_READ, inputMap, null,
				requestInstance);
		currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
		if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
				|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
				|| !currOperationResponseJSON.has("alerttypeaccounttype")) {
			LOG.error("Failed CRUD Operation:" + ServiceURLEnum.ALERTTYPEACCOUNTTYPE_READ.name());
			throw new ApplicationException(ErrorCodeEnum.ERR_20902);
		}
		LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.ALERTTYPEACCOUNTTYPE_READ.name());
		JSONArray alertTypeUserTypesJSONArray = currOperationResponseJSON.optJSONArray("alerttypeaccounttype");
		for (Object currObj : alertTypeUserTypesJSONArray) {
			if (currObj instanceof JSONObject) {
				currJSON = (JSONObject) currObj;
				if (currJSON.has("AccountTypeId")) {
					associatedAccountTypesSet.add(currJSON.optString("AccountTypeId"));
				}
			}
		}

		// Handle Supported Account Types
		if (supportedAccountTypesList != null && !supportedAccountTypesList.isEmpty()) {
			inputMap.clear();
			inputMap.put("AlertTypeId", alertTypeCode);
			inputMap.put("createdby", loggedInUserId);
			for (String accountTypeId : supportedAccountTypesList) {
				if (!associatedAccountTypesSet.contains(accountTypeId)) {
					LOG.debug("Adding Support for UserType:" + accountTypeId);
					timestamp = CommonUtilities.getISOFormattedLocalTimestamp();
					inputMap.put("createdts", timestamp);
					inputMap.put("AccountTypeId", accountTypeId);
					currOperationResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPEACCOUNTTYPE_CREATE,
							inputMap, null, requestInstance);
					currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
					if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
							|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						LOG.error("Failed CRUD Operation:" + ServiceURLEnum.ALERTTYPEACCOUNTTYPE_UPDATE.name());
						throw new ApplicationException(ErrorCodeEnum.ERR_20902);
					}
					LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.ALERTTYPEACCOUNTTYPE_UPDATE.name());
				}
				setAlertTypeAccountRecord.addParam(new Param(accountTypeId, String.valueOf(true), FabricConstants.STRING));
				associatedAccountTypesSet.add(accountTypeId);
			}
		}

		// Handle Removed Account Types
		if (unSupportedAccountTypesList != null && !unSupportedAccountTypesList.isEmpty()) {
			inputMap.clear();
			inputMap.put("AlertTypeId", alertTypeCode);
			for (String accountTypeId : unSupportedAccountTypesList) {
				if (associatedAccountTypesSet.contains(accountTypeId)) {
					LOG.debug("Removing Support for account Type:" + accountTypeId);
					inputMap.put("AccountTypeId", accountTypeId);
					currOperationResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPEACCOUNTTYPE_DELETE,
							inputMap, null, requestInstance);
					currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
					if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
							|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						LOG.error("Failed CRUD Operation:" + ServiceURLEnum.ALERTTYPEACCOUNTTYPE_DELETE.name());
						throw new ApplicationException(ErrorCodeEnum.ERR_20902);
					}
					LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.ALERTTYPEACCOUNTTYPE_DELETE.name());
					associatedAccountTypesSet.remove(accountTypeId);
				}
				setAlertTypeAccountRecord.addParam(new Param(accountTypeId, String.valueOf(false), FabricConstants.STRING));
			}
		}

		return setAlertTypeAccountRecord;
	}

	
	/**
	 * Method to set App support of Alert Type
	 * 
	 * @param alertTypeCode
	 * @param supportedAppsList
	 * @param unSupportedAppsList
	 * @param loggedInUserId
	 * @param requestInstance
	 * @return operation Record
	 * @throws ApplicationException
	 */
	private Record setAlertAppPreferences(String alertTypeCode, List<String> supportedAppsList,
			List<String> unSupportedAppsList, String loggedInUserId, DataControllerRequest requestInstance)
			throws ApplicationException {

		if (requestInstance == null) {
			LOG.error("DataControllerRequest Instance is NULL. Returning Error Response");
			throw new ApplicationException(ErrorCodeEnum.ERR_20902);
		}

		Record setAlertTypeAppRecord = new Record();
		setAlertTypeAppRecord.setId("appTypeSettings");

		Map<String, String> inputMap = new HashMap<>();
		JSONObject currOperationResponseJSON, currJSON;
		String currOperationResponse = StringUtils.EMPTY, timestamp = StringUtils.EMPTY;

		// Fetch Existing Association
		Set<String> associatedAppsSet = new HashSet<>();
		inputMap.put(ODataQueryConstants.FILTER, "AlertTypeId eq '" + alertTypeCode + "'");
		inputMap.put(ODataQueryConstants.SELECT, "AppId");
		currOperationResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPEAPP_READ, inputMap, null,
				requestInstance);
		currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
		if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
				|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
				|| !currOperationResponseJSON.has("alerttypeapp")) {
			LOG.error("Failed CRUD Operation:" + ServiceURLEnum.ALERTTYPEAPP_READ.name());
			throw new ApplicationException(ErrorCodeEnum.ERR_20902);
		}
		LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.ALERTTYPEAPP_READ.name());
		JSONArray alertTypeAppsJSONArray = currOperationResponseJSON.optJSONArray("alerttypeapp");
		for (Object currObj : alertTypeAppsJSONArray) {
			if (currObj instanceof JSONObject) {
				currJSON = (JSONObject) currObj;
				if (currJSON.has("AppId")) {
					associatedAppsSet.add(currJSON.optString("AppId"));
				}
			}
		}

		// Handle Supported Apps
		if (supportedAppsList != null && !supportedAppsList.isEmpty()) {
			inputMap.clear();
			inputMap.put("AlertTypeId", alertTypeCode);
			inputMap.put("createdby", loggedInUserId);
			for (String appId : supportedAppsList) {
				if (!associatedAppsSet.contains(appId)) {
					LOG.debug("Adding Support for App:" + appId);
					timestamp = CommonUtilities.getISOFormattedLocalTimestamp();
					inputMap.put("createdts", timestamp);
					inputMap.put("AppId", appId);
					currOperationResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPEAPP_CREATE, inputMap, null,
							requestInstance);
					currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
					if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
							|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						LOG.error("Failed CRUD Operation:" + ServiceURLEnum.ALERTTYPEAPP_CREATE.name());
						throw new ApplicationException(ErrorCodeEnum.ERR_20902);
					}
					LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.ALERTTYPEAPP_CREATE.name());
				}
				setAlertTypeAppRecord.addParam(new Param(appId, String.valueOf(true), FabricConstants.STRING));
				associatedAppsSet.add(appId);
			}
		}

		// Handle Removed Apps
		if (unSupportedAppsList != null && !unSupportedAppsList.isEmpty()) {
			inputMap.clear();
			inputMap.put("AlertTypeId", alertTypeCode);
			for (String appId : unSupportedAppsList) {
				if (associatedAppsSet.contains(appId)) {
					LOG.debug("Removing Support for App:" + appId);
					inputMap.put("AppId", appId);
					currOperationResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPEAPP_DELETE, inputMap, null,
							requestInstance);
					currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
					if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
							|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						LOG.error("Failed CRUD Operation:" + ServiceURLEnum.ALERTTYPEAPP_DELETE.name());
						throw new ApplicationException(ErrorCodeEnum.ERR_20902);
					}
					LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.ALERTCATEGORYCHANNEL_DELETE.name());
					associatedAppsSet.remove(appId);
				}
				setAlertTypeAppRecord.addParam(new Param(appId, String.valueOf(false), FabricConstants.STRING));
			}
		}

		return setAlertTypeAppRecord;
	}


	
	
	/**
	 * Method to set User Type support of Alert Type
	 * 
	 * @param alertTypeCode
	 * @param supportedUserTypesList
	 * @param unSupportedUserTypesList
	 * @param loggedInUserId
	 * @param requestInstance
	 * @return operation Record
	 * @throws ApplicationException
	 */
	private Record setAlertUserTypePreferences(String alertTypeCode, List<String> supportedUserTypesList,
			List<String> unSupportedUserTypesList, String loggedInUserId, DataControllerRequest requestInstance)
			throws ApplicationException {

		if (requestInstance == null) {
			LOG.error("DataControllerRequest Instance is NULL. Returning Error Response");
			throw new ApplicationException(ErrorCodeEnum.ERR_20902);
		}

		Record setAlertTypeAppRecord = new Record();
		setAlertTypeAppRecord.setId("appTypeSettings");

		Map<String, String> inputMap = new HashMap<>();
		JSONObject currOperationResponseJSON, currJSON;
		String currOperationResponse = StringUtils.EMPTY, timestamp = StringUtils.EMPTY;

		// Fetch Existing Association
		Set<String> associatedUserTypesSet = new HashSet<>();
		inputMap.put(ODataQueryConstants.FILTER, "AlertTypeId eq '" + alertTypeCode + "'");
		inputMap.put(ODataQueryConstants.SELECT, "CustomerTypeId");
		currOperationResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPECUSTOMERTYPE_READ, inputMap, null,
				requestInstance);
		currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
		if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
				|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
				|| !currOperationResponseJSON.has("alerttypecustomertype")) {
			LOG.error("Failed CRUD Operation:" + ServiceURLEnum.ALERTTYPECUSTOMERTYPE_READ.name());
			throw new ApplicationException(ErrorCodeEnum.ERR_20902);
		}
		LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.ALERTTYPECUSTOMERTYPE_READ.name());
		JSONArray alertTypeUserTypesJSONArray = currOperationResponseJSON.optJSONArray("alerttypecustomertype");
		for (Object currObj : alertTypeUserTypesJSONArray) {
			if (currObj instanceof JSONObject) {
				currJSON = (JSONObject) currObj;
				if (currJSON.has("CustomerTypeId")) {
					associatedUserTypesSet.add(currJSON.optString("CustomerTypeId"));
				}
			}
		}

		// Handle Supported User Types
		if (supportedUserTypesList != null && !supportedUserTypesList.isEmpty()) {
			inputMap.clear();
			inputMap.put("AlertTypeId", alertTypeCode);
			inputMap.put("createdby", loggedInUserId);
			for (String userTypeId : supportedUserTypesList) {
				if (!associatedUserTypesSet.contains(userTypeId)) {
					LOG.debug("Adding Support for UserType:" + userTypeId);
					timestamp = CommonUtilities.getISOFormattedLocalTimestamp();
					inputMap.put("createdts", timestamp);
					inputMap.put("CustomerTypeId", userTypeId);
					currOperationResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPECUSTOMERTYPE_CREATE,
							inputMap, null, requestInstance);
					currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
					if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
							|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						LOG.error("Failed CRUD Operation:" + ServiceURLEnum.ALERTTYPECUSTOMERTYPE_UPDATE.name());
						throw new ApplicationException(ErrorCodeEnum.ERR_20902);
					}
					LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.ALERTTYPECUSTOMERTYPE_UPDATE.name());
				}
				setAlertTypeAppRecord.addParam(new Param(userTypeId, String.valueOf(true), FabricConstants.STRING));
				associatedUserTypesSet.add(userTypeId);
			}
		}

		// Handle Removed User Types
		if (unSupportedUserTypesList != null && !unSupportedUserTypesList.isEmpty()) {
			inputMap.clear();
			inputMap.put("AlertTypeId", alertTypeCode);
			for (String userTypeId : unSupportedUserTypesList) {
				if (associatedUserTypesSet.contains(userTypeId)) {
					LOG.debug("Removing Support for User Type:" + userTypeId);
					inputMap.put("CustomerTypeId", userTypeId);
					currOperationResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPECUSTOMERTYPE_DELETE,
							inputMap, null, requestInstance);
					currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
					if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
							|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						LOG.error("Failed CRUD Operation:" + ServiceURLEnum.ALERTTYPECUSTOMERTYPE_DELETE.name());
						throw new ApplicationException(ErrorCodeEnum.ERR_20902);
					}
					LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.ALERTTYPECUSTOMERTYPE_DELETE.name());
					associatedUserTypesSet.remove(userTypeId);
				}
				setAlertTypeAppRecord.addParam(new Param(userTypeId, String.valueOf(false), FabricConstants.STRING));
			}
		}

		return setAlertTypeAppRecord;
	}

	/**
	 * Method to set Display Preferences of Alert Type
	 * 
	 * @param alertTypeCode
	 * @param addedDisplayPreference
	 * @param removedDisplayPreference
	 * @param loggedInUserId
	 * @param requestInstance
	 * @return Operation Record
	 * @throws ApplicationException
	 */
	private Record setAlertTypeDisplayPreferences(String alertTypeCode, String addedDisplayPreference,
			String removedDisplayPreference, String loggedInUserId, DataControllerRequest requestInstance)
			throws ApplicationException {

		if (requestInstance == null) {
			LOG.error("DataControllerRequest Instance is NULL. Returning Error Response");
			throw new ApplicationException(ErrorCodeEnum.ERR_20904);
		}

		Record displayPreference = new Record();
		displayPreference.setId("displayPreference");

		String currOperationResponse = StringUtils.EMPTY, currLocale = StringUtils.EMPTY;
		JSONObject currOperationResponseJSON;

		JSONObject currJSON = null;
		Map<String, String> inputMap = new HashMap<>();

		// Fetch Existing Association
		inputMap.put(ODataQueryConstants.FILTER, "AlertTypeId eq '" + alertTypeCode + "'");
		inputMap.put(ODataQueryConstants.SELECT, "LanguageCode");
		currOperationResponse = Executor.invokeService(ServiceURLEnum.DBXALERTTYPETEXT_READ, inputMap, null,
				requestInstance);
		currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
		if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
				|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
			LOG.error("Failed CRUD Operation:" + ServiceURLEnum.DBXALERTTYPETEXT_READ.name());
			throw new ApplicationException(ErrorCodeEnum.ERR_20904);
		}
		LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.DBXALERTTYPETEXT_READ.name());
		Set<String> associatedDisplayPreferencesSet = new HashSet<>();
		JSONArray associatedDisplayPreferencesJSONArray = currOperationResponseJSON.optJSONArray("dbxalerttypetext");
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
		inputMap.put("AlertTypeId", alertTypeCode);
		JSONArray removedDisplayPreferencesJSONArray = CommonUtilities.getStringAsJSONArray(removedDisplayPreference);
		if (removedDisplayPreferencesJSONArray != null && removedDisplayPreferencesJSONArray.length() > 0) {
			for (Object currObject : removedDisplayPreferencesJSONArray) {
				if (currObject instanceof String) {
					currLocale = (String) currObject;
					if (associatedDisplayPreferencesSet.contains(currLocale)) {
						LOG.debug("Removing Display Preference for Locale:" + currLocale);
						inputMap.put("LanguageCode", currLocale);
						currOperationResponse = Executor.invokeService(ServiceURLEnum.DBXALERTTYPETEXT_DELETE, inputMap,
								null, requestInstance);
						currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
						if (currOperationResponseJSON == null
								|| !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
								|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
							LOG.error("Failed CRUD Operation:" + ServiceURLEnum.DBXALERTTYPETEXT_DELETE.name());
							throw new ApplicationException(ErrorCodeEnum.ERR_20904);
						}
						LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.DBXALERTTYPETEXT_DELETE.name());
						associatedDisplayPreferencesSet.remove(currLocale);
					}
					displayPreference.addParam(new Param(currLocale, "Text Removed", FabricConstants.STRING));
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
						inputMap.put("AlertTypeId", alertTypeCode);
						inputMap.put("LanguageCode", currLocale);
						inputMap.put("DisplayName", currJSON.optString("displayName"));
						inputMap.put("Description", currJSON.optString("description"));

						if (associatedDisplayPreferencesSet.contains(currLocale)) {
							LOG.debug("Updating Display Preference for Locale:" + currLocale);
							inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
							inputMap.put("updatedby", loggedInUserId);
							currOperationResponse = Executor.invokeService(ServiceURLEnum.DBXALERTTYPETEXT_UPDATE,
									inputMap, null, requestInstance);
							currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
						} else {
							LOG.debug("Creating Display Preference for Locale:" + currLocale);
							inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
							inputMap.put("createdby", loggedInUserId);
							currOperationResponse = Executor.invokeService(ServiceURLEnum.DBXALERTTYPETEXT_CREATE,
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
						displayPreference.addParam(new Param(currLocale, "Text Added", FabricConstants.STRING));
					}
				}
			}
		}

		return displayPreference;
	}

	/**
	 * Method to reorder the Alert Types
	 * 
	 * @param requestInstance
	 * @param loggedInUserId
	 * @return operation Result
	 * @throws ApplicationException
	 */
	private Result reorderAlertType(DataControllerRequest requestInstance, String loggedInUserId)
			throws ApplicationException {

		if (requestInstance == null) {
			LOG.error("DataControllerRequest Instance is NULL. Returning Error Response");
			throw new ApplicationException(ErrorCodeEnum.ERR_20902);
		}
		Result operationResult = new Result();

		// Read Inputs
		String typeOrder = requestInstance.getParameter(TYPE_ORDER_PARAM);
		String categoryCode = requestInstance.getParameter(CATEGORY_CODE_PARAM);

		// Validate Inputs
		if (StringUtils.isBlank(categoryCode)) {
			LOG.error("Missing Mandatory Input: AlertCategoryCode");
			ErrorCodeEnum.ERR_20920.setErrorCode(operationResult);
			return operationResult;
		}
		JSONObject typeOrderJSONObject = null;
		if (StringUtils.isNotBlank(typeOrder)) {
			try {
				typeOrderJSONObject = new JSONObject(typeOrder);
			} catch (NullPointerException | JSONException e) {
				// Malformed Alert Category Channel Preference JSON
				LOG.error("Malformed Alert Type Order JSON");
				ErrorCodeEnum.ERR_20902.setErrorCode(operationResult);
				return operationResult;
			}
		}

		// Set Alert Type Order
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put("modifiedby", loggedInUserId);
		inputMap.put("AlertCategoryId", categoryCode);

		String currTypeId = StringUtils.EMPTY, currOperationResponse = StringUtils.EMPTY;
		JSONObject currOperationResponseJSON = null;
		int currTypeDisplaySequence = 0;
		if (typeOrderJSONObject != null) {
			for (String key : typeOrderJSONObject.keySet()) {
				if (typeOrderJSONObject.opt(key) instanceof Integer) {
					currTypeId = key;
					currTypeDisplaySequence = typeOrderJSONObject.optInt(key);
					inputMap.put("id", currTypeId);
					inputMap.put("DisplaySequence", String.valueOf(currTypeDisplaySequence));
					inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
					currOperationResponse = Executor.invokeService(ServiceURLEnum.DBXALERTTYPE_UPDATE, inputMap, null,
							requestInstance);
					currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
					if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
							|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						LOG.error("Failed CRUD Operation:" + ServiceURLEnum.DBXALERTTYPE_UPDATE.name());
						throw new ApplicationException(ErrorCodeEnum.ERR_20902);
					}
					LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.DBXALERTTYPE_UPDATE.name());
				}
			}
		}

		operationResult.addParam(new Param("status", "success", FabricConstants.STRING));

		return operationResult;
	}

}
