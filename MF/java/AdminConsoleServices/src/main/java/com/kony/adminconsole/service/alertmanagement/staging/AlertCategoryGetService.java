package com.kony.adminconsole.service.alertmanagement.staging;

import java.util.HashMap;
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
 * Service to retrieve the Alert Categories
 * 
 * @author Aditya Mankal
 */
public class AlertCategoryGetService implements JavaService2 {

	private static final String DEFAULT_LOCALE = AlertManagementHandler.DEFAULT_LOCALE;
	private static final Logger LOG = Logger.getLogger(AlertCategoryGetService.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) {

		Result processedResult = new Result();

		try {
			// Read Inputs
			String acceptLanguage = requestInstance.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
			LOG.debug("Received Accept-Language Header:" + acceptLanguage);

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

			// Construct Input Map
			Map<String, String> inputMap = new HashMap<>();
			if (StringUtils.equals(acceptLanguage, DEFAULT_LOCALE)) {
				inputMap.put(ODataQueryConstants.FILTER, "(alertcategorytext_LanguageCode eq '" + acceptLanguage + "')");
			} else {
				inputMap.put(ODataQueryConstants.FILTER,
						"(alertcategorytext_LanguageCode eq '" + acceptLanguage + "' or alertcategorytext_LanguageCode eq '" + DEFAULT_LOCALE + "')");
			}
			inputMap.put(ODataQueryConstants.ORDER_BY, "alertcategory_DisplaySequence asc");

			// Fetch Alert Categories
			String readAlertCategoryViewResponse = Executor.invokeService(ServiceURLEnum.ALERTCATEGORY_VIEW_READ, inputMap, null, requestInstance);
			JSONObject readAlertCategoryViewResponseJSON = CommonUtilities.getStringAsJSONObject(readAlertCategoryViewResponse);

			if (readAlertCategoryViewResponseJSON != null && readAlertCategoryViewResponseJSON.has(FabricConstants.OPSTATUS)
					&& readAlertCategoryViewResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
					&& readAlertCategoryViewResponseJSON.has("alertcategory_view")) {
				LOG.debug("Successful CRUD Operation");

				Dataset recordsDataset = new Dataset();
				recordsDataset.setId("records");

				// Construct Result dataset
				JSONArray alertCategoriesJSONArray = readAlertCategoryViewResponseJSON.optJSONArray("alertcategory_view");
				alertCategoriesJSONArray = CommonUtilities.filterRecordsByLocale(alertCategoriesJSONArray, "alertcategorytext_LanguageCode", "alertcategory_id", DEFAULT_LOCALE);
				alertCategoriesJSONArray = CommonUtilities.sortJSONArrayOfJSONObjects(alertCategoriesJSONArray, "alertcategory_DisplaySequence", true, true);

				// Filter Records by Locale
				if (alertCategoriesJSONArray != null && alertCategoriesJSONArray.length() > 0) {
					JSONObject currJSONObject;
					for (Object currObject : alertCategoriesJSONArray) {
						if (currObject instanceof JSONObject) {
							currJSONObject = (JSONObject) currObject;
							Record currRecord = new Record();
							for (String key : currJSONObject.keySet()) {
								currRecord.addParam(new Param(key, currJSONObject.optString(key), FabricConstants.STRING));
							}
							recordsDataset.addRecord(currRecord);
						}
					}
				}
				processedResult.addDataset(recordsDataset);

			} else {
				LOG.error("Failed CRUD Operation");
				ErrorCodeEnum.ERR_20915.setErrorCode(processedResult);
			}

		} catch (Exception e) {
			Result errorResult = new Result();
			LOG.debug("Runtime Exception.Exception Trace:", e);
			ErrorCodeEnum.ERR_20915.setErrorCode(errorResult);
			return errorResult;
		}
		return processedResult;
	}
}
