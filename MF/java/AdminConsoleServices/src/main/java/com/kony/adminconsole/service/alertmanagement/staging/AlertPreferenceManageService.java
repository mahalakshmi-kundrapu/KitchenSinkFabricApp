package com.kony.adminconsole.service.alertmanagement.staging;

import java.util.HashMap;
import java.util.HashSet;
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
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.service.authmodule.APICustomIdentityService;
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
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to set Customer Alert Category Preference and Alert Type Preference
 * 
 * @author Aditya Mankal
 *
 */
public class AlertPreferenceManageService implements JavaService2 {

	private static final String ACCOUNT_ID_FLAG = "*";
	private static final String ACCOUNT_ID_PARAM = "AccountId";
	private static final String ACCOUNT_TYPE_PARAM = "AccountTypeId";
	private static final String CUSTOMER_ID_PARAM = "CustomerId";
	private static final String IS_SUBSCRIBED_PARAM = "isSubscribed";
	private static final String ALERT_CATEGORY_ID_PARAM = "AlertCategoryId";
	private static final String CATEGORY_CHANNEL_PREFERENCE_PARAM = "channelPreference";
	private static final String CATEGORY_TYPE_PREFERENCE_PARAM = "typePreference";

	private static final Logger LOG = Logger.getLogger(AlertPreferenceManageService.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) {

		Result processedResult = new Result();
		String customerId = StringUtils.EMPTY;
		String loggedInUser = StringUtils.EMPTY;
		try {
			// Read Inputs
			String alertCategoryId = requestInstance.getParameter(ALERT_CATEGORY_ID_PARAM);
			LOG.debug("Received Alert Category ID:" + alertCategoryId);
			String isSubscribedStr = requestInstance.getParameter(IS_SUBSCRIBED_PARAM);
			LOG.debug("Received is Subscribed:" + isSubscribedStr);
			customerId = requestInstance.getParameter(CUSTOMER_ID_PARAM);
			LOG.debug("Is Customer ID Null?" + StringUtils.isBlank(customerId));
			String accountId = requestInstance.getParameter(ACCOUNT_ID_PARAM);
			LOG.debug("Is Account ID Null?" + StringUtils.isBlank(accountId));
			String accountTypeId = requestInstance.getParameter(ACCOUNT_TYPE_PARAM);
			LOG.debug("Is account Type Name Null?" + StringUtils.isBlank(accountTypeId));
			String alertCategoryChannelPreference = requestInstance.getParameter(CATEGORY_CHANNEL_PREFERENCE_PARAM);
			LOG.debug("Received Category Channel Preference:" + alertCategoryChannelPreference);
			String alertCategoryTypePreference = requestInstance.getParameter(CATEGORY_TYPE_PREFERENCE_PARAM);
			LOG.debug("Received Type Channel Preference:" + alertCategoryTypePreference);

			// Validate Inputs
			if (StringUtils.isBlank(alertCategoryId)) {
				// Missing Alert Category ID
				LOG.error("Missing Alert Category ID");
				ErrorCodeEnum.ERR_20920.setErrorCode(processedResult);
				return processedResult;
			}
			if (StringUtils.isBlank(customerId)) {
				// Missing Alert Customer ID
				LOG.error("Missing Customer ID");
				ErrorCodeEnum.ERR_20688.setErrorCode(processedResult);
				return processedResult;
			}
			// Validate Customer Id by resolving username
			CustomerHandler customerHandler = new CustomerHandler();
			String customerUsername = customerHandler.getCustomerUsername(customerId, requestInstance);
			if (StringUtils.isBlank(customerUsername)) {
				// Unrecognized Customer. Customer Type Could not be resolved
				LOG.error("Unrecognized Customer");
				ErrorCodeEnum.ERR_20539.setErrorCode(processedResult);
				return processedResult;
			}

			if(StringUtils.isNotBlank(accountTypeId) && !AlertManagementHandler.getAccountTypeIdsMap(requestInstance).containsKey(accountTypeId)) {
				LOG.error("Invalid account type");
				ErrorCodeEnum.ERR_20549.setErrorCode(processedResult);
				return processedResult;
			}
			
			JSONArray alertCategoryChannelPreferenceJSONArray = null, alertTypePreferenceJSONArray = null;
			if (StringUtils.isNotBlank(alertCategoryChannelPreference)) {
				try {
					alertCategoryChannelPreferenceJSONArray = new JSONArray(alertCategoryChannelPreference);
				} catch (NullPointerException | JSONException e) {
					// Malformed Alert Category Channel Preference JSON
					LOG.error("Malformed Alert Category Channel Preference JSON");
					ErrorCodeEnum.ERR_20928.setErrorCode(processedResult);
					return processedResult;
				}
			}
			if (StringUtils.isNotBlank(alertCategoryTypePreference)) {
				try {
					alertTypePreferenceJSONArray = new JSONArray(alertCategoryTypePreference);
				} catch (NullPointerException | JSONException e) {
					// Malformed Alert Category Type Preference JSON
					LOG.error("Malformed Alert Category Type Preference JSON");
					ErrorCodeEnum.ERR_20928.setErrorCode(processedResult);
					return processedResult;
				}
			}

			// Fetch Logged In User Info
			UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
			if (userDetailsBeanInstance != null) {
				loggedInUser = userDetailsBeanInstance.getUserId();
			}

			// Set Alert Category Preference
			if (StringUtils.equalsIgnoreCase(isSubscribedStr, "TRUE")
					|| StringUtils.equalsIgnoreCase(isSubscribedStr, "FALSE")) {
				boolean isSubscribed = StringUtils.equalsIgnoreCase(isSubscribedStr, "TRUE");
				Record alertCategoryPreferenceRecord = setCustomerAlertCategoryPreference(customerId, alertCategoryId,
						accountId, accountTypeId, isSubscribed, loggedInUser, requestInstance);
				processedResult.addRecord(alertCategoryPreferenceRecord);
			}

			// Set Alert Category Channel Preference
			if (alertCategoryChannelPreferenceJSONArray != null
					&& alertCategoryChannelPreferenceJSONArray.length() > 0) {
				Record alertCategoryChannelPreferenceRecord = setCustomerAlertCategoryChannelPreference(customerId,
						alertCategoryId, accountId, accountTypeId, alertCategoryChannelPreferenceJSONArray, loggedInUser,
						requestInstance);
				processedResult.addRecord(alertCategoryChannelPreferenceRecord);
			}

			// Set Alert Type Preference
			if (alertTypePreferenceJSONArray != null && alertTypePreferenceJSONArray.length() > 0) {
				Record alertTypePreferenceRecord = setCustomerAlertTypePreference(customerId, alertCategoryId,
						accountId, accountTypeId, alertTypePreferenceJSONArray, loggedInUser, requestInstance);
				processedResult.addRecord(alertTypePreferenceRecord);
			}
			auditActivity(customerId, loggedInUser, true, requestInstance);

		} catch (ApplicationException e) {
			auditActivity(customerId, loggedInUser, false, requestInstance);
			Result errorResult = new Result();
			LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
			e.getErrorCodeEnum().setErrorCode(errorResult);
			return errorResult;
		} catch (Exception e) {
			auditActivity(customerId, loggedInUser, false, requestInstance);
			Result errorResult = new Result();
			LOG.debug("Runtime Exception.Exception Trace:", e);
			ErrorCodeEnum.ERR_20927.setErrorCode(errorResult);
			return errorResult;
		}

		return processedResult;
	}

	/**
	 * Method to audit the Activity Information
	 * 
	 * @param customerId
	 * @param loggedInUser
	 * @param status
	 * @param requestInstance
	 */
	private void auditActivity(String customerId, String loggedInUser, boolean status,
			DataControllerRequest requestInstance) {

		if (!StringUtils.equalsIgnoreCase(loggedInUser, APICustomIdentityService.API_USER_ID)) {
			if (status == true) {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
						ActivityStatusEnum.SUCCESSFUL, "Alert Preferences Updated. Customer id:"+customerId);
			} else {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
						ActivityStatusEnum.FAILED, "Failed to update Alert Preferences. Customer id:"+customerId);
			}
		}
	}

	/**
	 * Method to set the Customer Alert Category Preference of a customer
	 * 
	 * @param customerId
	 * @param alertCategoryId
	 * @param accountId
	 * @param isSubscribed
	 * @param loggedInUserId
	 * @param requestInstance
	 * @return Record containing status information
	 * @throws ApplicationException
	 */
	private Record setCustomerAlertCategoryPreference(String customerId, String alertCategoryId, String accountId,
			String accountTypeId, boolean isSubscribed, String loggedInUserId, DataControllerRequest requestInstance)
			throws ApplicationException {

		Record operationRecord = new Record();
		operationRecord.setId("alertCategoryPreference");

		if (requestInstance == null) {
			// Data Controller Request Instance is NULL. Throw Application Exception.
			LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception.");
			throw new ApplicationException(ErrorCodeEnum.ERR_20927);
		}

		if (StringUtils.isBlank(customerId) || StringUtils.isBlank(alertCategoryId)) {
			// Missing Mandatory Inputs. Returning Empty Record
			LOG.error("Missing Mandatory Inputs. Returning Empty Record.");
			return operationRecord;
		}

		// Fetch Customer Alert Category Association
		StringBuffer filterQueryBuffer = new StringBuffer();
		filterQueryBuffer
				.append("Customer_id eq '" + customerId + "' and AlertCategoryId eq '" + alertCategoryId + "'");

		if (StringUtils.isNotBlank(accountId)) {
			filterQueryBuffer.append(" and AccountID eq '" + accountId + "'");
		}
		if (StringUtils.isNotBlank(accountTypeId)) {
			filterQueryBuffer.append(" and AccountType eq '" + accountTypeId + "'");
		}
		
		filterQueryBuffer.trimToSize();
		String filterQuery = filterQueryBuffer.toString();

		Map<String, String> parameterMap = new HashMap<>();
		parameterMap.put(ODataQueryConstants.FILTER, filterQuery);

		String readCustomerAlertSwitchResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERALERTSWITCH_READ,
				parameterMap, null, requestInstance);
		JSONObject readCustomerAlertSwitchResponseJSON = CommonUtilities
				.getStringAsJSONObject(readCustomerAlertSwitchResponse);
		if (readCustomerAlertSwitchResponseJSON == null
				|| !readCustomerAlertSwitchResponseJSON.has(FabricConstants.OPSTATUS)
				|| readCustomerAlertSwitchResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
				|| !readCustomerAlertSwitchResponseJSON.has("customeralertswitch")) {
			// Failed to Read Customer Alert Switch
			LOG.debug("Failed CRUD Operation");
			throw new ApplicationException(ErrorCodeEnum.ERR_20927);
		}

		// Successfully Read Customer Alert Switch
		LOG.debug("Successful CRUD Operation");

		JSONArray customerAlertSwitchRecordsArray = readCustomerAlertSwitchResponseJSON
				.optJSONArray("customeralertswitch");
		boolean isInitialRegistration;
		if (customerAlertSwitchRecordsArray != null && customerAlertSwitchRecordsArray.optJSONObject(0) != null) {
			// Existing Alert Category Association
			isInitialRegistration = false;
		} else {
			// Non-Existing Alert Category Association
			isInitialRegistration = true;
		}
		LOG.debug("isInitialRegistration" + String.valueOf(isInitialRegistration));

		// Create/Update Alert Category Subscription based on current association
		parameterMap.clear();
		parameterMap.put("Customer_id", customerId);
		parameterMap.put("AlertCategoryId", alertCategoryId);
		if (StringUtils.isNotBlank(accountId)) {
			parameterMap.put("AccountID", accountId);
		}else {
			parameterMap.put("AccountID", ACCOUNT_ID_FLAG);
		}
		
		if (StringUtils.isNotBlank(accountTypeId)) {
			parameterMap.put("AccountType", accountTypeId);
		}else {
			parameterMap.put("AccountType", ACCOUNT_ID_FLAG);
		}
		
		parameterMap.put("Status_id", isSubscribed ? StatusEnum.SID_SUBSCRIBED.name() : StatusEnum.SID_UNSUBSCRIBED.name());
		String currOperation = StringUtils.EMPTY;
		String currentTime = CommonUtilities.getISOFormattedLocalTimestamp();
		String setCustomerAlertSwitchResponse = StringUtils.EMPTY;
		JSONObject setCustomerAlertSwitchResponseJSON = null;

		if (isInitialRegistration) {
			// Non-Existing Alert Category Association. Create Alert Category Preference
			currOperation = "createPreference";
			parameterMap.put("createdby", loggedInUserId);
			parameterMap.put("createdts", currentTime);
			LOG.debug("Creating Customer Alert Category Switch Record");
			setCustomerAlertSwitchResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERALERTSWITCH_CREATE,
					parameterMap, null, requestInstance);
		} else {
			// Existing Alert Category Association. Update Alert Category Preference
			currOperation = "updatePreference";
			parameterMap.put("modifiedby", loggedInUserId);
			parameterMap.put("lastmodifiedts", currentTime);
			LOG.debug("Updating Customer Alert Category Switch Record");
			setCustomerAlertSwitchResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERALERTSWITCH_UPDATE,
					parameterMap, null, requestInstance);
		}

		// Add Operation Meta
		operationRecord.addParam(new Param("operation", currOperation, FabricConstants.STRING));
		operationRecord.addParam(new Param("customerId", customerId, FabricConstants.STRING));
		operationRecord.addParam(new Param("alertCategoryId", alertCategoryId, FabricConstants.STRING));
		if (StringUtils.isNotBlank(accountId)) {
			operationRecord.addParam(new Param("accountId", accountId, FabricConstants.STRING));
		}
		if (StringUtils.isNotBlank(accountTypeId)) {
			operationRecord.addParam(new Param("accountTypeId", accountTypeId, FabricConstants.STRING));
		}

		// Check Operation Response
		setCustomerAlertSwitchResponseJSON = CommonUtilities.getStringAsJSONObject(setCustomerAlertSwitchResponse);
		if (setCustomerAlertSwitchResponseJSON == null || !setCustomerAlertSwitchResponseJSON.has(FabricConstants.OPSTATUS)
				|| setCustomerAlertSwitchResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
			LOG.debug("Failed CRUD Operation");
			operationRecord.addParam(new Param("status", "Fail", FabricConstants.STRING));
			operationRecord.addParam(new Param("serviceResponse", setCustomerAlertSwitchResponse, FabricConstants.STRING));
		} else {
			LOG.debug("Successful CRUD Operation");
			operationRecord.addParam(new Param("status", "Success", FabricConstants.STRING));
		}

		return operationRecord;
	}

	/**
	 * Method to set the Alert Type Preference of a customer
	 * 
	 * @param customerId
	 * @param alertCategoryId
	 * @param accountId
	 * @param alertTypePreferenceJSONArray
	 * @param loggedInUserId
	 * @param requestInstance
	 * @return Record containing status information
	 * @throws ApplicationException
	 */
	private Record setCustomerAlertTypePreference(String customerId, String alertCategoryId, String accountId, String accountTypeId,
			JSONArray alertTypePreferenceJSONArray, String loggedInUserId, DataControllerRequest requestInstance)
			throws ApplicationException {

		Record operationRecord = new Record();
		operationRecord.setId("alertTypePreference");
		if (requestInstance == null) {
			// Data Controller Request Instance is NULL. Throw Application Exception.
			LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception");
			throw new ApplicationException(ErrorCodeEnum.ERR_20927);
		}
		if (StringUtils.isBlank(customerId) || StringUtils.isBlank(alertCategoryId)) {
			// Missing Mandatory Inputs. Returning Empty Record
			LOG.error("Missing Mandatory Inputs. Returning Empty Record");
			return operationRecord;
		}

		if (alertTypePreferenceJSONArray != null && alertTypePreferenceJSONArray.length() > 0) {

			JSONObject currJSONObject, currOperationResponseJSON;
			String currAlertTypeId, currOperationResponse, currTimestamp, currOperation;
			Map<String, String> parameterMap = new HashMap<>();

			Dataset subscriptionDataset = new Dataset();
			subscriptionDataset.setId("subscriptionDataset");

			// Fetch Associated Alert Types
			StringBuffer filterQueryBuffer = new StringBuffer();
			filterQueryBuffer.append("Customer_id eq '" + customerId + "' and (");
			for (int index = 0; index < alertTypePreferenceJSONArray.length(); index++) {

				if (alertTypePreferenceJSONArray.get(index) instanceof JSONObject) {
					currJSONObject = alertTypePreferenceJSONArray.getJSONObject(index);
					currAlertTypeId = currJSONObject.optString("typeId");
					if (StringUtils.isNotBlank(currAlertTypeId)) {
						filterQueryBuffer.append("AlertTypeId eq '" + currAlertTypeId + "'");
					}
					if (index < alertTypePreferenceJSONArray.length() - 1) {
						filterQueryBuffer.append(" or ");
					}
				}

			}
			filterQueryBuffer.append(")");
			if (StringUtils.isNotBlank(accountId)) {
				filterQueryBuffer.append(" and AccountId eq '" + accountId + "'");
			}
			if (StringUtils.isNotBlank(accountTypeId)) {
				filterQueryBuffer.append(" and AccountType eq '" + accountTypeId + "'");
			}
			
			filterQueryBuffer.trimToSize();
			String filterQuery = filterQueryBuffer.toString();
			parameterMap.put(ODataQueryConstants.FILTER, filterQuery);
			String readCustomerAlertEntitlementResponse = Executor.invokeService(
					ServiceURLEnum.DBXCUSTOMERALERTENTITLEMENT_READ, parameterMap, null, requestInstance);
			JSONObject readCustomerAlertEntitlementResponseJSON = CommonUtilities
					.getStringAsJSONObject(readCustomerAlertEntitlementResponse);
			if (readCustomerAlertEntitlementResponseJSON == null
					|| !readCustomerAlertEntitlementResponseJSON.has(FabricConstants.OPSTATUS)
					|| readCustomerAlertEntitlementResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
					|| !readCustomerAlertEntitlementResponseJSON.has("dbxcustomeralertentitlement")) {
				// Failed to Read Customer Alert Entitlement
				LOG.debug("Failed CRUD Operation");
				throw new ApplicationException(ErrorCodeEnum.ERR_20927);
			}

			// Successfully to Read Customer Alert Entitlement
			LOG.debug("Successful CRUD Operation");

			// Analyze Associated Alert Types
			JSONArray customerAlertEntitlementArray = readCustomerAlertEntitlementResponseJSON
					.optJSONArray("dbxcustomeralertentitlement");
			Set<String> associatedCustomerAlertEntitlements = new HashSet<>();
			for (Object currObject : customerAlertEntitlementArray) {
				if (currObject instanceof JSONObject) {
					currJSONObject = (JSONObject) currObject;
					if (currJSONObject.has("AlertTypeId")) {
						associatedCustomerAlertEntitlements.add(currJSONObject.optString("AlertTypeId"));
					}
				}
			}

			// Set Alert Types Preference
			parameterMap.clear();
			parameterMap.put("Customer_id", customerId);
			if (StringUtils.isNotBlank(accountId)) {
				parameterMap.put("AccountId", accountId);
			}else {
				parameterMap.put("AccountId", ACCOUNT_ID_FLAG);
			}
			
			if (StringUtils.isNotBlank(accountTypeId)) {
				parameterMap.put("AccountType", accountTypeId);
			}else {
				parameterMap.put("AccountType", ACCOUNT_ID_FLAG);
			}

			for (Object currObject : alertTypePreferenceJSONArray) {
				if (currObject instanceof JSONObject) {
					currJSONObject = (JSONObject) currObject;
					currAlertTypeId = currJSONObject.optString("typeId");
					if (StringUtils.isNotBlank(currAlertTypeId)) {
						Record currAlertRecord = new Record();

						parameterMap.put("AlertTypeId", currAlertTypeId);

						if (StringUtils.equalsIgnoreCase(currJSONObject.optString("isSubscribed"), "TRUE")) {
							// Create Alert Type Preference
							if (StringUtils.isNotBlank(currJSONObject.optString("value1"))) {
								parameterMap.put("Value1", currJSONObject.optString("value1"));
							}

							if (StringUtils.isNotBlank(currJSONObject.optString("value2"))) {
								parameterMap.put("Value2", currJSONObject.optString("value2"));
							}

							currTimestamp = CommonUtilities.getISOFormattedLocalTimestamp();
							if (associatedCustomerAlertEntitlements.contains(currAlertTypeId)) {
								LOG.debug("Update Customer Alert Preference");
								parameterMap.put("updatedby", loggedInUserId);
								parameterMap.put("lastmodifiedts", currTimestamp);
								currOperationResponse = Executor.invokeService(
										ServiceURLEnum.DBXCUSTOMERALERTENTITLEMENT_UPDATE, parameterMap, null,
										requestInstance);
								currOperation = "updatePreference";
							} else {
								LOG.debug("Creating Customer Alert Preference");
								parameterMap.put("createdby", loggedInUserId);
								parameterMap.put("createdts", currTimestamp);
								currOperationResponse = Executor.invokeService(
										ServiceURLEnum.DBXCUSTOMERALERTENTITLEMENT_CREATE, parameterMap, null,
										requestInstance);
								currOperation = "createPreference";
							}

							// Check Create/Update operation Status
							currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
							if (currOperationResponseJSON == null
									|| !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
									|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
								throw new ApplicationException(ErrorCodeEnum.ERR_20927);
							}

						} else {
							// Remove Alert Type Preference
							if (associatedCustomerAlertEntitlements.contains(currAlertTypeId)) {
								LOG.debug("Removing Customer Alert Preference");
								currOperationResponse = Executor.invokeService(
										ServiceURLEnum.DBXCUSTOMERALERTENTITLEMENT_DELETE, parameterMap, null,
										requestInstance);
								currOperationResponseJSON = CommonUtilities
										.getStringAsJSONObject(currOperationResponse);
								if (currOperationResponseJSON == null
										|| !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
										|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
									throw new ApplicationException(ErrorCodeEnum.ERR_20927);
								}
							}
							currOperation = "removePreference";
						}

						// Set Operation Meta
						currAlertRecord.addParam(new Param("CustomerId", customerId, FabricConstants.STRING));
						currAlertRecord.addParam(new Param("AlertTypeId", currAlertTypeId, FabricConstants.STRING));
						currAlertRecord.addParam(new Param("operation", currOperation, FabricConstants.STRING));
						if (StringUtils.isNotBlank(accountId)) {
							currAlertRecord.addParam(new Param("AccountId", accountId, FabricConstants.STRING));
						}
						if (StringUtils.isNotBlank(accountTypeId)) {
							currAlertRecord.addParam(new Param("AccountType", accountTypeId, FabricConstants.STRING));
						}

						subscriptionDataset.addRecord(currAlertRecord);

						// Remove Iteration Values
						parameterMap.remove("Alert_id");
						parameterMap.remove("Value1");
						parameterMap.remove("Value2");
						parameterMap.remove("createdby");
						parameterMap.remove("createdts");
						parameterMap.remove("modifiedby");
						parameterMap.remove("lastmodifiedts");

					}

				}

			}
			operationRecord.addDataset(subscriptionDataset);
		}
		return operationRecord;
	}

	/**
	 * Method to set the Alert Category Channel Preference
	 * 
	 * @param customerId
	 * @param alertCategoryId
	 * @param accountId
	 * @param alertCategoryChannelPreferenceJSONArray
	 * @param loggedInUserId
	 * @param requestInstance
	 * @return Record containing status information
	 * @throws ApplicationException
	 */
	private Record setCustomerAlertCategoryChannelPreference(String customerId, String alertCategoryId,
			String accountId, String accountTypeId, JSONArray alertCategoryChannelPreferenceJSONArray, String loggedInUserId,
			DataControllerRequest requestInstance) throws ApplicationException {

		Record operationRecord = new Record();
		operationRecord.setId("alertCategoryChannelPreference");

		if (requestInstance == null) {
			// Data Controller Request Instance is NULL. Throw Application Exception.
			LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception");
			throw new ApplicationException(ErrorCodeEnum.ERR_20927);
		}
		if (StringUtils.isBlank(customerId) || StringUtils.isBlank(alertCategoryId)) {
			// Missing Mandatory Inputs. Returning Empty Record
			LOG.error("Missing Mandatory Inputs. Returning Empty Record");
			return operationRecord;
		}

		if (alertCategoryChannelPreferenceJSONArray != null && alertCategoryChannelPreferenceJSONArray.length() > 0) {

			// Method Local Variables
			JSONObject currJSONObject, currOperationResponseJSON;
			String currChannelId, currPreference, currOperation, currOperationResponse, currTimestamp;
			Map<String, String> parameterMap = new HashMap<>();

			Dataset subscriptionDataset = new Dataset();
			subscriptionDataset.setId("subscriptionDataset");

			// Fetch existing Alert Category Channel Association
			StringBuffer filterQueryBuffer = new StringBuffer();
			filterQueryBuffer.append("Customer_id eq '" + customerId + "'");
			filterQueryBuffer.append(" and AlertCategoryId eq '" + alertCategoryId + "'");
			
			if (StringUtils.isNotBlank(accountTypeId)) {
				filterQueryBuffer.append(" and AccountType eq '" + accountTypeId + "'");
			}else {
				filterQueryBuffer.append(" and AccountType eq '" + ACCOUNT_ID_FLAG + "'");
			}
			
			if (StringUtils.isNotBlank(accountId)) {
				filterQueryBuffer.append(" and AccountId eq '" + accountId + "'");
			} else {
				filterQueryBuffer.append(" and AccountId eq '" + ACCOUNT_ID_FLAG + "'");
			}
			filterQueryBuffer.trimToSize();
			String filterQuery = filterQueryBuffer.toString();
			parameterMap.put(ODataQueryConstants.FILTER, filterQuery);
			parameterMap.put(ODataQueryConstants.SELECT, "ChannelId");
			String readCustomerAlertCategoryChannelRespose = Executor.invokeService(
					ServiceURLEnum.CUSTOMERALERTCATEGORYCHANNEL_READ, parameterMap, null, requestInstance);
			JSONObject readCustomerAlertCategoryChannelResposeJSON = CommonUtilities
					.getStringAsJSONObject(readCustomerAlertCategoryChannelRespose);

			if (readCustomerAlertCategoryChannelResposeJSON == null
					|| !readCustomerAlertCategoryChannelResposeJSON.has(FabricConstants.OPSTATUS)
					|| readCustomerAlertCategoryChannelResposeJSON.getInt(FabricConstants.OPSTATUS) != 0
					|| !readCustomerAlertCategoryChannelResposeJSON.has("customeralertcategorychannel")) {
				// Failed to Read Alert Category Channels
				LOG.debug("Failed CRUD Operation");
				throw new ApplicationException(ErrorCodeEnum.ERR_20927);
			}

			// Successfully to Read Alert Category Channels
			LOG.debug("Successful CRUD Operation");

			// Analyze Associated Alert Category Channels
			JSONArray customerAlertCategoryChannelPreferencesArray = readCustomerAlertCategoryChannelResposeJSON
					.optJSONArray("customeralertcategorychannel");
			Set<String> associatedAlertCategoryChannels = new HashSet<>();
			for (Object currObject : customerAlertCategoryChannelPreferencesArray) {
				if (currObject instanceof JSONObject) {
					currJSONObject = (JSONObject) currObject;
					if (currJSONObject.has("ChannelId")) {
						associatedAlertCategoryChannels.add(currJSONObject.optString("ChannelId"));
					}
				}
			}
			parameterMap.clear();

			parameterMap.put("Customer_id", customerId);
			parameterMap.put("AlertCategoryId", alertCategoryId);
			if (StringUtils.isNotBlank(accountId)) {
				parameterMap.put("AccountId", accountId);
			} else {
				parameterMap.put("AccountId", ACCOUNT_ID_FLAG);
			}
			
			if (StringUtils.isNotBlank(accountTypeId)) {
				parameterMap.put("AccountType", accountTypeId);
			} else {
				parameterMap.put("AccountType", ACCOUNT_ID_FLAG);
			}

			// Set Customer Alert Category Channel Preferences
			for (Object currObject : alertCategoryChannelPreferenceJSONArray) {

				if (currObject instanceof JSONObject) {
					Record currChannelRecord = new Record();

					currJSONObject = (JSONObject) currObject;

					currChannelId = currJSONObject.optString("channelId");
					parameterMap.put("ChannelId", currChannelId);

					currPreference = currJSONObject.optString("isSubscribed");
					if (StringUtils.equalsIgnoreCase(currPreference, "TRUE")) {
						currTimestamp = CommonUtilities.getISOFormattedLocalTimestamp();
						if (associatedAlertCategoryChannels.contains(currChannelId)) {
							LOG.debug("Updating Customer Alert Category Channel Preference");
							currOperation = "updateSubscription";
							parameterMap.put("modified", loggedInUserId);
							parameterMap.put("lastmodifiedts", currTimestamp);
							currOperationResponse = Executor.invokeService(
									ServiceURLEnum.CUSTOMERALERTCATEGORYCHANNEL_UPDATE, parameterMap, null,
									requestInstance);
						} else {
							LOG.debug("Creating Customer Alert Category Channel Preference");
							currOperation = "createSubscription";
							parameterMap.put("createdby", loggedInUserId);
							parameterMap.put("createdts", currTimestamp);
							currOperationResponse = Executor.invokeService(
									ServiceURLEnum.CUSTOMERALERTCATEGORYCHANNEL_CREATE, parameterMap, null,
									requestInstance);
						}
						// Check Create/Update operation Status
						currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
						if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
								|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
							throw new ApplicationException(ErrorCodeEnum.ERR_20927);
						}

					} else {

						// Remove Alert Type Preference
						if (associatedAlertCategoryChannels.contains(currChannelId)) {
							LOG.debug("Removing Customer Alert Category Channel Preference");
							currOperation = "removeSubscription";
							currOperationResponse = Executor.invokeService(
									ServiceURLEnum.CUSTOMERALERTCATEGORYCHANNEL_DELETE, parameterMap, null,
									requestInstance);
							currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
							if (currOperationResponseJSON == null
									|| !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
									|| currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
								throw new ApplicationException(ErrorCodeEnum.ERR_20927);
							}
						}
						currOperation = "removePreference";
					}

					// Set Operation Meta
					currChannelRecord.addParam(new Param("CustomerId", customerId, FabricConstants.STRING));
					currChannelRecord.addParam(new Param("ChannelId", currChannelId, FabricConstants.STRING));
					currChannelRecord.addParam(new Param("operation", currOperation, FabricConstants.STRING));
					if (StringUtils.isNotBlank(accountId)) {
						currChannelRecord.addParam(new Param("AccountId", accountId, FabricConstants.STRING));
					}
					if (StringUtils.isNotBlank(accountTypeId)) {
						currChannelRecord.addParam(new Param("AccountTypeId", accountTypeId, FabricConstants.STRING));
					}
					subscriptionDataset.addRecord(currChannelRecord);

					// Remove iteration values
					parameterMap.remove("ChannelId");
					parameterMap.remove("Status_id");
					parameterMap.remove("createdby");
					parameterMap.remove("createdts");
					parameterMap.remove("modifiedby");
					parameterMap.remove("lastmodifiedts");
				}
			}
			operationRecord.addDataset(subscriptionDataset);
		}
		return operationRecord;
	}

}
