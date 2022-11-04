package com.kony.adminconsole.service.alertmanagement.staging;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.AlertManagementHandler;
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
 * Service to retrieve the Master Data of Alert Mangement
 * 
 * @author Aditya Mankal
 */
public class AlertsMasterDataService implements JavaService2 {

	private static final Logger LOG = Logger.getLogger(AlertsMasterDataService.class);
	private static final String DEFAULT_LOCALE = AlertManagementHandler.DEFAULT_LOCALE;

	private static final String GET_EVENT_TYPES_METHOD_NAME = "getEventTypes";
	private static final String GET_ALERT_CHANNELS_METHOD_NAME = "getAlertChannels";
	private static final String GET_EVENT_SUBTYPES_METHOD_NAME = "getEventSubTypes";
	private static final String GET_ALERT_ATTRIBUTES_METHOD_NAME = "getAlertAttributes";
	private static final String GET_ALERT_CONDITIONS_METHOD_NAME = "getAlertConditions";
	private static final String GET_ALERT_CONTENT_FIELDS_METHOD_NAME = "getAlertContentFields";

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) {

		try {
			// Read Inputs
			String acceptLanguage = requestInstance.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
			LOG.debug("Received Accept-Language Header:" + acceptLanguage);

			// Format Accept Language Identifier
			if (StringUtils.isBlank(acceptLanguage)) {
				// Consider Default Locale if Accept-Language Header is Blank
				acceptLanguage = DEFAULT_LOCALE;
				LOG.debug("Received Accept-Language Header is empty. Returning Data of Default Locale." + DEFAULT_LOCALE);
			}
			acceptLanguage = CommonUtilities.formatLanguageIdentifier(acceptLanguage);

			// Get Alert Attributes
			if (StringUtils.equalsIgnoreCase(methodID, GET_ALERT_ATTRIBUTES_METHOD_NAME)) {
				return getAlertAttributes(acceptLanguage, requestInstance);
			}

			// Get Alert Conditions
			if (StringUtils.equalsIgnoreCase(methodID, GET_ALERT_CONDITIONS_METHOD_NAME)) {
				return getAlertConditions(acceptLanguage, requestInstance);
			}

			// Get Alert Channels
			if (StringUtils.equalsIgnoreCase(methodID, GET_ALERT_CHANNELS_METHOD_NAME)) {
				return getAlertChannels(acceptLanguage, requestInstance);
			}

			// Get Event Types
			if (StringUtils.equalsIgnoreCase(methodID, GET_EVENT_TYPES_METHOD_NAME)) {
				return getEventTypes(requestInstance);
			}

			// Get Event SubTypes
			if (StringUtils.equalsIgnoreCase(methodID, GET_EVENT_SUBTYPES_METHOD_NAME)) {
				return getEventSubTypes(requestInstance);
			}

			// Get Alert Content Fields
			if (StringUtils.equalsIgnoreCase(methodID, GET_ALERT_CONTENT_FIELDS_METHOD_NAME)) {
				return getAlertContentFields(requestInstance);
			}

			return new Result();
		} catch (ApplicationException e) {
			Result errorResult = new Result();
			LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
			e.getErrorCodeEnum().setErrorCode(errorResult);
			return errorResult;
		} catch (Exception e) {
			Result errorResult = new Result();
			LOG.debug("Runtime Exception.Exception Trace:", e);
			ErrorCodeEnum.ERR_20927.setErrorCode(errorResult);
			return errorResult;
		}

	}

	/**
	 * Method to get the list of channels supported by Alert Engine
	 * 
	 * @param acceptLanguage
	 * @param requestInstance
	 * @return operation Result
	 * @throws ApplicationException
	 */
	private Result getAlertChannels(String acceptLanguage, DataControllerRequest requestInstance) throws ApplicationException {

		Result operationResult = new Result();

		// Construct Input Map
		Map<String, String> inputMap = new HashMap<>();
		if (StringUtils.equals(acceptLanguage, DEFAULT_LOCALE)) {
			inputMap.put(ODataQueryConstants.FILTER, "(channeltext_LanguageCode eq '" + acceptLanguage + "')");
		} else {
			inputMap.put(ODataQueryConstants.FILTER, "(channeltext_LanguageCode eq '" + acceptLanguage + "' or channeltext_LanguageCode eq '" + DEFAULT_LOCALE + "')");
		}
		LOG.debug("FILTER QUERY:" + inputMap.get(ODataQueryConstants.FILTER));

		// Fetch Channels
		String readChannelViewResponse = Executor.invokeService(ServiceURLEnum.CHANNEL_VIEW_READ, inputMap, null, requestInstance);
		JSONObject readChannelViewResponseJSON = CommonUtilities.getStringAsJSONObject(readChannelViewResponse);

		if (readChannelViewResponseJSON == null || !readChannelViewResponseJSON.has(FabricConstants.OPSTATUS) ||
				readChannelViewResponseJSON.getInt(FabricConstants.OPSTATUS) != 0 || !readChannelViewResponseJSON.has("channel_view")) {
			LOG.error("Failed CRUD Operation");
			throw new ApplicationException(ErrorCodeEnum.ERR_20897);
		}

		// Successful Fabric Operation
		LOG.debug("Successful CRUD Operation");

		JSONObject currJSONObject;
		Dataset dataset = new Dataset();
		dataset.setId("channels");

		JSONArray channelViewJSONArray = readChannelViewResponseJSON.optJSONArray("channel_view");

		// Filter Records based on Locale
		channelViewJSONArray = CommonUtilities.filterRecordsByLocale(channelViewJSONArray, "channeltext_LanguageCode", "channel_id", DEFAULT_LOCALE);

		// Construct Response Dataset
		for (Object currObject : channelViewJSONArray) {
			if (currObject instanceof JSONObject) {
				currJSONObject = (JSONObject) currObject;
				Record currRecord = new Record();
				for (String currKey : currJSONObject.keySet()) {
					currRecord.addParam(new Param(currKey, currJSONObject.optString(currKey), FabricConstants.STRING));
				}
				dataset.addRecord(currRecord);
			}
		}
		// Add current Dataset to Result Object
		operationResult.addDataset(dataset);

		return operationResult;
	}

	/**
	 * Method to get the Alert Attributes
	 * 
	 * @param acceptLanguage
	 * @param requestInstance
	 * @return Operation Result
	 * @throws ApplicationException
	 */
	private Result getAlertAttributes(String acceptLanguage, DataControllerRequest requestInstance) throws ApplicationException {

		Result processedResult = new Result();

		Dataset alertAttributesDataset = new Dataset();
		alertAttributesDataset.setId("alertAttributes");

		LOG.debug("Fetching Alert Attributes");
		Map<String, Record> alertAttributesMap = AlertManagementHandler.getAlertAttributes(acceptLanguage, requestInstance);
		LOG.debug("Fetched Alert Attributes");

		// Construct Result Dataset
		for (Entry<String, Record> entry : alertAttributesMap.entrySet()) {
			alertAttributesDataset.addRecord(entry.getValue());
		}
		processedResult.addDataset(alertAttributesDataset);
		return processedResult;
	}

	/**
	 * Method to get the Alert Conditions
	 * 
	 * @param acceptLanguage
	 * @param requestInstance
	 * @return Operation Result
	 * @throws ApplicationException
	 */
	private Result getAlertConditions(String acceptLanguage, DataControllerRequest requestInstance) throws ApplicationException {

		Result processedResult = new Result();

		Dataset alertConditionsDataset = new Dataset();
		alertConditionsDataset.setId("alertConditions");

		LOG.debug("Fetching Alert Conditions");
		Map<String, Record> alertConditionMap = AlertManagementHandler.getAlertConditions(acceptLanguage, requestInstance);
		LOG.debug("Fetched Alert Conditions");

		// Construct Result Dataset
		for (Entry<String, Record> entry : alertConditionMap.entrySet()) {
			alertConditionsDataset.addRecord(entry.getValue());
		}
		processedResult.addDataset(alertConditionsDataset);
		return processedResult;

	}

	/**
	 * Method to get the Event Types
	 * 
	 * @param requestInstance
	 * @return Operation Result
	 * @throws ApplicationException
	 */
	private Result getEventTypes(DataControllerRequest requestInstance) throws ApplicationException {
		Result processedResult = new Result();

		Dataset dataset = new Dataset();
		dataset.setId("eventTypes");

		// Fetch Event Types
		String operationResponse = Executor.invokeService(ServiceURLEnum.EVENTTYPE_READ, new HashMap<>(), new HashMap<>(), requestInstance);
		JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

		if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS) ||
				operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0 || !operationResponseJSON.has("eventtype")) {
			LOG.error("Failed CRUD Operation");
			throw new ApplicationException(ErrorCodeEnum.ERR_20895);
		}
		// Successful Fabric Operation
		LOG.debug("Successful CRUD Operation");

		JSONArray recordsArray = operationResponseJSON.optJSONArray("eventtype");

		// Construct Response Dataset
		JSONObject currJSONObject;
		for (Object currObject : recordsArray) {
			if (currObject instanceof JSONObject) {
				currJSONObject = (JSONObject) currObject;
				Record currRecord = new Record();
				for (String currKey : currJSONObject.keySet()) {
					currRecord.addParam(new Param(currKey, currJSONObject.optString(currKey), FabricConstants.STRING));
				}
				dataset.addRecord(currRecord);
			}
		}
		// Add current Dataset to Result Object
		processedResult.addDataset(dataset);

		return processedResult;

	}

	/**
	 * Method to get the Event SubTypes
	 * 
	 * @param requestInstance
	 * @return Operation Result
	 * @throws ApplicationException
	 */
	private Result getEventSubTypes(DataControllerRequest requestInstance) throws ApplicationException {
		Result processedResult = new Result();

		String eventTypeId = requestInstance.getParameter("eventTypeId");
		if (StringUtils.isBlank(eventTypeId)) {
			LOG.error("Missing mandatory Input: Event Type Id");
			ErrorCodeEnum.ERR_20896.setErrorCode(processedResult);
			processedResult.addParam(new Param("message", "EventTypeId is a mandatory input", FabricConstants.STRING));
			return processedResult;
		}

		Dataset dataset = new Dataset();
		dataset.setId("eventSubTypes");

		// Prepare Input Map
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put(ODataQueryConstants.FILTER, "eventtypeid eq '" + eventTypeId + "'");

		// Fetch Event Types
		String operationResponse = Executor.invokeService(ServiceURLEnum.EVENTSUBTYPE_READ, inputMap, null, requestInstance);
		JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

		if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS) ||
				operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0 || !operationResponseJSON.has("eventsubtype")) {
			LOG.error("Failed CRUD Operation. Response:" + operationResponse);
			throw new ApplicationException(ErrorCodeEnum.ERR_20896);
		}
		// Successful Fabric Operation
		LOG.debug("Successful CRUD Operation");

		JSONArray recordsArray = operationResponseJSON.optJSONArray("eventsubtype");

		// Construct Response Dataset
		JSONObject currJSONObject;
		for (Object currObject : recordsArray) {
			if (currObject instanceof JSONObject) {
				currJSONObject = (JSONObject) currObject;
				Record currRecord = new Record();
				for (String currKey : currJSONObject.keySet()) {
					currRecord.addParam(new Param(currKey, currJSONObject.optString(currKey), FabricConstants.STRING));
				}
				dataset.addRecord(currRecord);
			}
		}
		// Add current Dataset to Result Object
		processedResult.addDataset(dataset);

		return processedResult;
	}

	/**
	 * Method to get the Alert Content Fields
	 * 
	 * @param requestInstance
	 * @return Operation Result
	 * @throws ApplicationException
	 */
	private Result getAlertContentFields(DataControllerRequest requestInstance) throws ApplicationException {
		Result processedResult = new Result();

		Dataset dataset = new Dataset();
		dataset.setId("alertContentFields");

		// Fetch Event Types
		String operationResponse = Executor.invokeService(ServiceURLEnum.ALERTCONTENTFIELDS_READ, new HashMap<>(), new HashMap<>(), requestInstance);
		JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

		if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS) ||
				operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0 || !operationResponseJSON.has("alertcontentfields")) {
			LOG.error("Failed CRUD Operation");
			throw new ApplicationException(ErrorCodeEnum.ERR_20895);
		}
		// Successful Fabric Operation
		LOG.debug("Successful CRUD Operation");

		JSONArray recordsArray = operationResponseJSON.optJSONArray("alertcontentfields");

		// Construct Response Dataset
		JSONObject currJSONObject;
		for (Object currObject : recordsArray) {
			if (currObject instanceof JSONObject) {
				currJSONObject = (JSONObject) currObject;
				Record currRecord = new Record();
				for (String currKey : currJSONObject.keySet()) {
					currRecord.addParam(new Param(currKey, currJSONObject.optString(currKey), FabricConstants.STRING));
				}
				dataset.addRecord(currRecord);
			}
		}
		// Add current Dataset to Result Object
		processedResult.addDataset(dataset);

		return processedResult;

	}

}
