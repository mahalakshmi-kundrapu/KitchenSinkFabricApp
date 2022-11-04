package com.kony.adminconsole.service.alertmanagement.staging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.commons.utils.ThreadExecutor;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.AlertManagementHandler;
import com.kony.adminconsole.handler.CustomerHandler;
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
 * Service to retrieve the Alert Category Channel Preference of Customers
 * 
 * @author Aditya Mankal
 */
public class AlertCategoryChannelPreferenceGetService implements JavaService2 {

	private static final String DEFAULT_LOCALE = AlertManagementHandler.DEFAULT_LOCALE;

	private static final String ACCOUNT_ID_PARAM = "AccountId";
	private static final String ACCOUNT_TYPE_PARAM = "AccountTypeId";
	private static final String CUSTOMER_ID_PARAM = "CustomerId";

	private static final String IS_SUBSCRIBED_PARAM = "isSubscribed";
	private static final String ALERT_CATEGORY_ID_PARAM = "AlertCategoryId";
	private static final String IS_CHANNEL_SUPPORTED_PARAM = "isChannelSupported";

	private static final Logger LOG = Logger.getLogger(AlertCategoryChannelPreferenceGetService.class);

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
			String customerId = requestInstance.getParameter(CUSTOMER_ID_PARAM);
			LOG.debug("Is Customer ID Null?" + StringUtils.isBlank(customerId));
			String accountId = requestInstance.getParameter(ACCOUNT_ID_PARAM);
			LOG.debug("Is Account ID Null?" + StringUtils.isBlank(accountId));
			String accountTypeId = requestInstance.getParameter(ACCOUNT_TYPE_PARAM);
			LOG.debug("Is Account Type Null?" + StringUtils.isBlank(accountTypeId));

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
			String formattedAcceptLanguage = acceptLanguage;

			// Validate Customer Id by resolving username
			CustomerHandler customerHandler = new CustomerHandler();
			String customerUsername = customerHandler.getCustomerUsername(customerId, requestInstance);
			if (StringUtils.isBlank(customerUsername)) {
				// Unrecognized Customer. Customer Type Could not be resolved
				LOG.error("Unrecognized Customer");
				ErrorCodeEnum.ERR_20539.setErrorCode(processedResult);
				return processedResult;
			}

			final String accountType_Id = accountTypeId;
			// Fetch list of supported channels of Alert Category
			Callable<List<String>> supportedChannelListCallable = new Callable<List<String>>() {
				@Override
				public List<String> call() throws ApplicationException {
					return AlertManagementHandler.getSupportedChannelsOfAlertCategory(alertCategoryId, requestInstance);
				}
			};
			// Fetch customer channel preferences of Alert Category
			Callable<List<String>> customerChannelPreferencesCallable = new Callable<List<String>>() {
				@Override
				public List<String> call() throws ApplicationException {
					return AlertManagementHandler.getCustomerChannelPreferencesOfAlertCategory(customerId, accountId, accountType_Id, alertCategoryId, requestInstance);
				}
			};

			// Fetch Channels
			Callable<String> channelsCallable = new Callable<String>() {
				@Override
				public String call() throws ApplicationException {
					Map<String, String> inputMap = new HashMap<>();
					if (StringUtils.equals(formattedAcceptLanguage, DEFAULT_LOCALE)) {
						inputMap.put(ODataQueryConstants.FILTER, "(channeltext_LanguageCode eq '" + formattedAcceptLanguage + "')");
					} else {
						inputMap.put(ODataQueryConstants.FILTER,
								"(channeltext_LanguageCode eq '" + formattedAcceptLanguage + "' or channeltext_LanguageCode eq '" + DEFAULT_LOCALE + "')");
					}
					LOG.debug("FILTER QUERY:" + inputMap.get(ODataQueryConstants.FILTER));
					return Executor.invokeService(ServiceURLEnum.CHANNEL_VIEW_READ, inputMap, null, requestInstance);
				}
			};

			Future<List<String>> supportedChannelsListFuture = ThreadExecutor.execute(supportedChannelListCallable);
			Future<List<String>> customerChannelPreferencesFuture = ThreadExecutor.execute(customerChannelPreferencesCallable);
			Future<String> channelsFuture = ThreadExecutor.execute(channelsCallable);

			List<String> supportedChannelsList = supportedChannelsListFuture.get();
			List<String> customerChannelPreferences = customerChannelPreferencesFuture.get();
			String readChannelViewResponse = channelsFuture.get();
			JSONObject readChannelViewResponseJSON = CommonUtilities.getStringAsJSONObject(readChannelViewResponse);

			// Collate Response
			if (readChannelViewResponseJSON != null && readChannelViewResponseJSON.has(FabricConstants.OPSTATUS) &&
					readChannelViewResponseJSON.getInt(FabricConstants.OPSTATUS) == 0 && readChannelViewResponseJSON.has("channel_view")) {

				// Successful Fabric Operation
				LOG.debug("Successful CRUD Operation");

				JSONObject currJSONObject;
				String currChannelID;

				Dataset dataset = new Dataset();
				dataset.setId("records");

				JSONArray channelViewJSONArray = readChannelViewResponseJSON.optJSONArray("channel_view");

				// Filter Records based on Locale
				channelViewJSONArray = CommonUtilities.filterRecordsByLocale(channelViewJSONArray, "channeltext_LanguageCode", "channel_id", DEFAULT_LOCALE);

				// Construct Response Dataset
				for (Object currObject : channelViewJSONArray) {
					if (currObject instanceof JSONObject) {
						currJSONObject = (JSONObject) currObject;
						currChannelID = currJSONObject.optString("channel_id");
						Record currRecord = new Record();
						for (String currKey : currJSONObject.keySet()) {
							currRecord.addParam(new Param(currKey, currJSONObject.optString(currKey), FabricConstants.STRING));
						}
						currRecord.addParam(new Param(IS_CHANNEL_SUPPORTED_PARAM, String.valueOf(supportedChannelsList.contains(currChannelID)), FabricConstants.STRING));
						currRecord.addParam(new Param(IS_SUBSCRIBED_PARAM, String.valueOf(customerChannelPreferences.contains(currChannelID)), FabricConstants.STRING));
						dataset.addRecord(currRecord);
					}
				}
				// Add current Dataset to Result Object
				processedResult.addDataset(dataset);
			} else {
				LOG.error("Failed CRUD Operation");
				throw new ApplicationException(ErrorCodeEnum.ERR_20914);
			}

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

		LOG.debug("Returning Success Response");
		return processedResult;
	}

}
