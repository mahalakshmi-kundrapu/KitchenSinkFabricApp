package com.kony.adminconsole.service.alertmanagement.staging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
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
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.service.authmodule.APICustomIdentityService;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
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
 * Service to retrieve the Alert Type Preference of Customers
 * 
 * @author Aditya Mankal
 */
public class AlertTypePreferenceGetService implements JavaService2 {

	private static final String DEFAULT_LOCALE = AlertManagementHandler.DEFAULT_LOCALE;

	private static final String ACCOUNT_ID_PARAM = "AccountId";
	private static final String ACCOUNT_TYPE_PARAM = "AccountTypeId";
	private static final String CUSTOMER_ID_PARAM = "CustomerId";
	private static final String ALERT_CATEGORY_ID_PARAM = "AlertCategoryId";
	private static final String ACCOUNTS = "accounts";

	private static final String IS_SUBSCRIBED_PARAM = "isSubscribed";
	private static final String IS_INITIAL_LOAD_PARAM = "isInitialLoad";

	private static final Logger LOG = Logger.getLogger(AlertTypePreferenceGetService.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) {

		Result processedResult = new Result();
		try {
			// Read Inputs
			String alertCategoryID = requestInstance.getParameter(ALERT_CATEGORY_ID_PARAM);
			LOG.debug("Received Alert Category ID:" + alertCategoryID);
			String customerID = requestInstance.getParameter(CUSTOMER_ID_PARAM);
			LOG.debug("Is Customer ID Null?" + StringUtils.isBlank(customerID));
			String accountID = requestInstance.getParameter(ACCOUNT_ID_PARAM);
			LOG.debug("Is Account ID Null?" + StringUtils.isBlank(accountID));
			String accountTypeId = requestInstance.getParameter(ACCOUNT_TYPE_PARAM);
			LOG.debug("Is Account Type Null?" + StringUtils.isBlank(accountTypeId));
			String accounts = requestInstance.getParameter(ACCOUNTS);
			LOG.debug("Is Accounts Null?" + StringUtils.isBlank(accounts));
			String acceptLanguage = requestInstance.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
			LOG.debug("Received Accept-Language Header:" + acceptLanguage);

			// Resolve Customer Type based on Customer ID
			CustomerHandler customerHandler = new CustomerHandler();
			String customerTypeID = customerHandler.getCustomerType(customerID, requestInstance);
			if (StringUtils.isBlank(customerTypeID)) {
				// Unrecognized Customer. Customer Type Could not be resolved
				LOG.error("Unrecognized Customer. Customer Type Could not be resolved");
				ErrorCodeEnum.ERR_20539.setErrorCode(processedResult);
				return processedResult;
			}
			
			if(StringUtils.isNotBlank(accountTypeId) && !AlertManagementHandler.getAccountTypeIdsMap(requestInstance).containsKey(accountTypeId)) {
				LOG.error("Invalid account type");
				ErrorCodeEnum.ERR_20549.setErrorCode(processedResult);
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

			JSONObject currJSON;
			List<String> applicableAlertTypes = new ArrayList<>();

			// Get IDs of Alerts Applicable to current Customer Type
			Set<String> alertTypesOfCustomerType = AlertManagementHandler.getAlertTypesofCustomerType(customerTypeID, requestInstance);
			applicableAlertTypes.addAll(alertTypesOfCustomerType);

			// Get Active and Non-Global Alert Types Data
			JSONArray alertTypes = AlertManagementHandler.getAlertTypes(applicableAlertTypes, alertCategoryID, acceptLanguage, false, true, requestInstance);
			
			List<String> validAccountAlerts = null;
			//Get valid account alerts
			if(StringUtils.isNotBlank(accountID) || StringUtils.isNotBlank(accountTypeId)) {
				validAccountAlerts = getListOfValidAccountAlerts(requestInstance, accountID, accountTypeId, accounts);
			}
			
			// Sort Alert Types as per Display-Sequence Order
			alertTypes = CommonUtilities.sortJSONArrayOfJSONObjects(alertTypes, "alerttype_DisplaySequence", true, true);

			// Get List of Alert Conditions and Alert Attributes of Applicable Alert Types
			Set<String> alertConditionIds = new HashSet<>();
			Set<String> alertAttributeIds = new HashSet<>();
			for (Object currObject : alertTypes) {
				if (currObject instanceof JSONObject) {
					currJSON = (JSONObject) currObject;
					if (currJSON.has("alerttype_AlertConditionId") && StringUtils.isNotBlank(currJSON.optString("alerttype_AlertConditionId"))) {
						alertConditionIds.add(currJSON.optString("alerttype_AlertConditionId"));
					}
					if (currJSON.has("alerttype_AttributeId") && StringUtils.isNotBlank(currJSON.optString("alerttype_AttributeId"))) {
						alertAttributeIds.add(currJSON.optString("alerttype_AttributeId"));
					}
				}
			}

			// Get Alert Attributes
			Map<String, Record> alertAttributesMap = AlertManagementHandler.getAlertAttributes(alertAttributeIds, acceptLanguage, requestInstance);

			// Get Alert Conditions
			Map<String, Record> alertConditionMap = AlertManagementHandler.getAlertConditions(alertConditionIds, acceptLanguage, requestInstance);

			// Get Alert Types subscribed by customer
			JSONArray subscribedAlertTypes = AlertManagementHandler.getCustomerAlerts(customerID, accountID, accountTypeId, requestInstance);

			String currAlertTypeId, currAlertConditionId, currAlertAttributeId;

			// Convert Subscribed Alert Types data to Map
			Map<String, Record> subscribedAlertTypesMap = new HashMap<>();
			for (Object currObject : subscribedAlertTypes) {

				if (currObject instanceof JSONObject) {
					currJSON = (JSONObject) currObject;
					Record currAlertTypeRecord = new Record();
					currAlertTypeRecord.setId("preference");

					for (String key : currJSON.keySet()) {
						currAlertTypeRecord.addParam(new Param(key, currJSON.optString(key), FabricConstants.STRING));
					}
					currAlertTypeId = currJSON.optString("AlertTypeId");
					subscribedAlertTypesMap.put(currAlertTypeId, currAlertTypeRecord);
				}
			}

			// Collate applicable Alert Type Data and Subscribed Alert Type Data
			Dataset recordsDataset = new Dataset();
			recordsDataset.setId("records");
			for (Object currObject : alertTypes) {

				if (currObject instanceof JSONObject) {
					currJSON = (JSONObject) currObject;
					Record currAlertRecord = new Record();

					if(validAccountAlerts != null && !validAccountAlerts.contains(currJSON.getString("alerttype_id"))) {
						//Skip adding the alert if it is not valid for the account type
						continue;
					}
					
					// Add Alert Type Information
					for (String key : currJSON.keySet()) {
						currAlertRecord.addParam(new Param(key, currJSON.optString(key), FabricConstants.STRING));
					}

					// Add Alert Type Attributes Information
					currAlertAttributeId = currJSON.optString("alerttype_AttributeId");
					if (StringUtils.isNotBlank(currAlertAttributeId) && alertAttributesMap.containsKey(currAlertAttributeId)) {
						currAlertRecord.addRecord(alertAttributesMap.get(currAlertAttributeId));
					}

					// Add Alert Type Condition Information
					currAlertConditionId = currJSON.optString("alerttype_AlertConditionId");
					if (StringUtils.isNotBlank(currAlertConditionId) && alertConditionMap.containsKey(currAlertConditionId)) {
						currAlertRecord.addRecord(alertConditionMap.get(currAlertConditionId));
					}

					// Add Alert Type Subscription Status
					currAlertTypeId = currJSON.optString("alerttype_id");
					if (subscribedAlertTypesMap.containsKey(currAlertTypeId)) {
						currAlertRecord.addParam(new Param(IS_SUBSCRIBED_PARAM, "true", FabricConstants.STRING));
						if(subscribedAlertTypesMap.get(currAlertTypeId).getParamByName("Value1") != null) {
							currAlertRecord.addParam(new Param("alerttype_Value1",subscribedAlertTypesMap.get(currAlertTypeId).getParamByName("Value1").getValue()));
						}
						if(subscribedAlertTypesMap.get(currAlertTypeId).getParamByName("Value2") != null) {
							currAlertRecord.addParam(new Param("alerttype_Value2",subscribedAlertTypesMap.get(currAlertTypeId).getParamByName("Value2").getValue()));
						}
					} else {
						currAlertRecord.addParam(new Param(IS_SUBSCRIBED_PARAM, "false", FabricConstants.STRING));
					}

					recordsDataset.addRecord(currAlertRecord);
				}
			}
			processedResult.addDataset(recordsDataset);

			// Add Alert Category Subscription and Initial Load Status
			Record categorySwitchRecord = getCategorySubscriptionStatus(alertCategoryID, customerID, accountID,accountTypeId, requestInstance);
			processedResult.addRecord(categorySwitchRecord);

		} catch (ApplicationException e) {
			Result errorResult = new Result();
			LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
			e.getErrorCodeEnum().setErrorCode(errorResult);
			return errorResult;
		} catch (Exception e) {
			Result errorResult = new Result();
			LOG.error("Unexpected Exception.Exception Trace:", e);
			ErrorCodeEnum.ERR_20926.setErrorCode(errorResult);
			return errorResult;
		}
		return processedResult;
	}

	/**
	 * Method to fetch Subscription Status of Customer to an Alert Category
	 * 
	 * @param alertCategoryID
	 * @param customerID
	 * @param accountID
	 * @param requestInstance
	 * @return
	 * @throws ApplicationException
	 */
	public Record getCategorySubscriptionStatus(String alertCategoryID, String customerID, String accountID, String accountTypeId, DataControllerRequest requestInstance) throws ApplicationException {
		if (requestInstance == null) {
			throw new ApplicationException(ErrorCodeEnum.ERR_20925);
		}

		Map<String, String> inputMap = new HashMap<>();

		StringBuffer filterQueryBuffer = new StringBuffer();
		filterQueryBuffer.append("Customer_id eq '" + customerID + "' and AlertCategoryId eq '" + alertCategoryID + "'");

		if (StringUtils.isNotBlank(accountID)) {
			filterQueryBuffer.append(" and AccountID eq '" + accountID + "'");
		}
		if (StringUtils.isNotBlank(accountTypeId)) {
			filterQueryBuffer.append(" and AccountType eq '" + accountTypeId + "'");
		}
		filterQueryBuffer.trimToSize();
		String filterQuery = filterQueryBuffer.toString();
		filterQuery = filterQuery.trim();
		inputMap.put(ODataQueryConstants.FILTER, filterQuery);

		String readCustomerAlertSwitchResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERALERTSWITCH_READ, inputMap, null, requestInstance);
		JSONObject readCustomerAlertSwitchResponseJSON = CommonUtilities.getStringAsJSONObject(readCustomerAlertSwitchResponse);
		if (readCustomerAlertSwitchResponseJSON == null || !readCustomerAlertSwitchResponseJSON.has(FabricConstants.OPSTATUS)
				|| readCustomerAlertSwitchResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
				|| !readCustomerAlertSwitchResponseJSON.has("customeralertswitch")) {
			throw new ApplicationException(ErrorCodeEnum.ERR_20925);
		}

		boolean isSubscribed = false;
		boolean isInitialLoad = false;

		JSONArray customerAlertSwitchArray = readCustomerAlertSwitchResponseJSON.optJSONArray("customeralertswitch");
		if (customerAlertSwitchArray != null && customerAlertSwitchArray.length() > 0 && customerAlertSwitchArray.get(0) instanceof JSONObject) {
			JSONObject alertSwitchJSONObject = customerAlertSwitchArray.optJSONObject(0);
			isInitialLoad = false;
			if (alertSwitchJSONObject.has("Status_id") && StringUtils.equalsIgnoreCase(StatusEnum.SID_SUBSCRIBED.name(), alertSwitchJSONObject.optString("Status_id"))) {
				isSubscribed = true;
			} else {
				isSubscribed = false;
			}
		} else {
			isInitialLoad = true;
			isSubscribed = false;
		}

		Record subscriptionRecord = new Record();
		subscriptionRecord.setId("categorySubscription");
		subscriptionRecord.addParam(new Param(IS_SUBSCRIBED_PARAM, String.valueOf(isSubscribed), FabricConstants.STRING));
		subscriptionRecord.addParam(new Param(IS_INITIAL_LOAD_PARAM, String.valueOf(isInitialLoad), FabricConstants.STRING));

		return subscriptionRecord;
	}
	
	private List<String> getListOfValidAccountAlerts(DataControllerRequest requestInstance,
			String accountID, String accountTypeId, String accounts) throws JSONException, ApplicationException {
		
		if(StringUtils.isNotBlank(accountID)) {
			//Fetch account type from accounts information
			if(StringUtils.isBlank(accounts)) {
				return null;
			}
			JSONArray accountsJSON = null;
			try {
				accountsJSON = new JSONArray(accounts);
			}catch(Exception e) {
				LOG.error("Invalid accounts object received");
				return null;
			}
			for(int index=0; index < accountsJSON.length(); index++) {
				JSONObject account = accountsJSON.getJSONObject(index);
				if(account.getString("accountID").equalsIgnoreCase(accountID)) {
					accountTypeId = AlertManagementHandler.getAccountTypesMap(requestInstance).get(account.getString("accountType")).toString();
					break;
				}
			}
		}
		
		//Construct valid alert type list
		Map<String,String> inputMap = new HashMap<>();
		inputMap.put(ODataQueryConstants.FILTER, "AccountTypeId eq '"+accountTypeId+"'");
		String readAlertTypeAccountTypeStr = Executor.invokeService(ServiceURLEnum.ALERTTYPEACCOUNTTYPE_READ, inputMap, null, requestInstance);
		JSONObject readAlertTypeAccountType = CommonUtilities.getStringAsJSONObject(readAlertTypeAccountTypeStr);
		if (readAlertTypeAccountType == null || !readAlertTypeAccountType.has(FabricConstants.OPSTATUS)
				|| readAlertTypeAccountType.getInt(FabricConstants.OPSTATUS) != 0 || !readAlertTypeAccountType.has("alerttypeaccounttype")) {
			throw new ApplicationException(ErrorCodeEnum.ERR_20846);
		}
		List<String> validAlerts = new ArrayList<>();
		JSONArray alertTypeAccountTypesArray = readAlertTypeAccountType.getJSONArray("alerttypeaccounttype");
		alertTypeAccountTypesArray.forEach((alertObject) -> {
			JSONObject alert = (JSONObject)alertObject;
			validAlerts.add(alert.getString("AlertTypeId"));
		});
		
		return validAlerts;
	}
	
}

