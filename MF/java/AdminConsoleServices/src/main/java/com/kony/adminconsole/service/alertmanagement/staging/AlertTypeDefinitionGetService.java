package com.kony.adminconsole.service.alertmanagement.staging;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
 * Service to Fetch the Alert Type Definition
 * 
 * @author Aditya Mankal
 */
public class AlertTypeDefinitionGetService implements JavaService2 {

	private static final String ALERT_TYPE_ID_PARAM = "AlertTypeId";
	private static final String DEFAULT_LOCALE = AlertManagementHandler.DEFAULT_LOCALE;

	private static final Logger LOG = Logger.getLogger(AlertCategoryDefinitionGetService.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) {

		Result processedResult = new Result();

		try {
			// Read Inputs
			String alertTypeId = requestInstance.getParameter(ALERT_TYPE_ID_PARAM);
			LOG.debug("Received Alert Type ID:" + alertTypeId);
			String acceptLanguage = requestInstance.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
			LOG.debug("Received Accept-Language Header:" + acceptLanguage);

			// Validate Inputs
			if (StringUtils.isBlank(alertTypeId)) {
				// Missing Alert Category ID
				LOG.error("Missing Alert Type ID");
				ErrorCodeEnum.ERR_20920.setErrorCode(processedResult);
				return processedResult;
			}

			// Format Accept Language Identifier
			if (StringUtils.isBlank(acceptLanguage)) {
				// Consider Default Locale if Accept-Language Header is Blank
				acceptLanguage = DEFAULT_LOCALE;
				LOG.debug("Received Accept-Language Header is empty. Returning Data of Default Locale." + DEFAULT_LOCALE);
			}
			acceptLanguage = CommonUtilities.formatLanguageIdentifier(acceptLanguage);

			// Fetch Alert Type Definition
			Record alertTypeDefintion = getAlertTypeDefinition(alertTypeId, acceptLanguage, requestInstance);
			processedResult.addRecord(alertTypeDefintion);

			// Fetch associated Applications of Alert Type
			Dataset alertTypeApps = getAlertTypeAppAssociation(alertTypeId, requestInstance);
			processedResult.addDataset(alertTypeApps);

			// Fetch associated User Types of Alert Type
			Dataset alertTypeUserTypes = getAlertTypeUserTypeAssociation(alertTypeId, requestInstance);
			processedResult.addDataset(alertTypeUserTypes);

			// Fetch associated Account Types of Alert Types
			Dataset accountTypes = getAlertTypeAccountTypeAssociation(alertTypeId, requestInstance);
			processedResult.addDataset(accountTypes);

			// Fetch associated Alert Sub Types
			Dataset alertSubTypes = getAlertSubTypes(alertTypeId, requestInstance);
			processedResult.addDataset(alertSubTypes);

			// Fetch Display Preferences
			Dataset alertTypeDisplayPreferences = getAlertTypeDisplayPreferences(alertTypeId, requestInstance);
			processedResult.addDataset(alertTypeDisplayPreferences);

		} catch (ApplicationException e) {
			Result errorResult = new Result();
			LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
			e.getErrorCodeEnum().setErrorCode(errorResult);
			return errorResult;
		} catch (Exception e) {
			Result errorResult = new Result();
			LOG.error("Exception in Fetching Customer Alert Category Definition. Checked Involved Operations. Exception:", e);
			ErrorCodeEnum.ERR_20920.setErrorCode(errorResult);
			return errorResult;
		}
		return processedResult;
	}

	/**
	 * Method to get the Alert Type Definition
	 * 
	 * @param alertTypeId
	 * @param acceptLanguage
	 * @param requestInstance
	 * @return Record containing Alert Type Definition
	 * @throws ApplicationException
	 */
	private Record getAlertTypeDefinition(String alertTypeId, String acceptLanguage, DataControllerRequest requestInstance) throws ApplicationException {

		Record operationRecord = new Record();
		operationRecord.setId("alertTypeDefinition");

		if (requestInstance == null) {
			// Data Controller Request Instance is NULL. Throw Application Exception.
			LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception.");
			throw new ApplicationException(ErrorCodeEnum.ERR_20927);
		}
		if (StringUtils.isBlank(alertTypeId)) {
			// Missing Mandatory Input. Return Empty Record
			LOG.error("Alert Type Id value is empty. Returning empty record.");
			return operationRecord;
		}

		LOG.debug("Attempting to Fetch Alert Type Definition");
		// Fetch Alert Type Definition
		JSONObject alertTypeDefintionJSON = AlertManagementHandler.getAlertTypeDefinition(alertTypeId, acceptLanguage, requestInstance);
		LOG.debug("Fetched Alert Type Definition");
		if (alertTypeDefintionJSON != null) {
			// Construct Record Object

			operationRecord.addParam(new Param("name", alertTypeDefintionJSON.optString("alerttype_Name", FabricConstants.STRING)));
			operationRecord.addParam(new Param("displayName", alertTypeDefintionJSON.optString("alerttypetext_DisplayName", FabricConstants.STRING)));
			operationRecord.addParam(new Param("alertCode", alertTypeDefintionJSON.optString("alerttype_id", FabricConstants.STRING)));

			if (StringUtils.equalsIgnoreCase(alertTypeDefintionJSON.optString("alerttype_IsGlobal"), "TRUE")
					|| StringUtils.equalsIgnoreCase(alertTypeDefintionJSON.optString("alerttype_IsGlobal"), "1")) {
				operationRecord.addParam(new Param("alertType", "Global Alert", FabricConstants.STRING));
			} else {
				operationRecord.addParam(new Param("alertType", "User Alert", FabricConstants.STRING));
			}
			operationRecord.addParam(new Param("languageCode", alertTypeDefintionJSON.optString("alerttypetext_LanguageCode", FabricConstants.STRING)));
			operationRecord.addParam(new Param("typeStatus", alertTypeDefintionJSON.optString("alerttype_Status_id", FabricConstants.STRING)));
			operationRecord.addParam(new Param("typeDescription", alertTypeDefintionJSON.optString("alerttypetext_Description", FabricConstants.STRING)));
			operationRecord.addParam(new Param("value1", alertTypeDefintionJSON.optString("alerttype_Value1"), FabricConstants.STRING));
			operationRecord.addParam(new Param("value2", alertTypeDefintionJSON.optString("alerttype_Value2"), FabricConstants.STRING));

			Set<String> hashset = new HashSet<String>();

			// Get Alert Attributes
			String alertAttributeId = alertTypeDefintionJSON.optString("alerttype_AttributeId");
			if (StringUtils.isNotBlank(alertAttributeId)) {
				hashset.add(alertAttributeId);
				Map<String, Record> alertAttributesMap = AlertManagementHandler.getAlertAttributes(hashset, acceptLanguage, requestInstance);
				if (alertAttributesMap.containsKey(alertAttributeId) && alertAttributesMap.get(alertAttributeId) != null)
					operationRecord.addRecord(alertAttributesMap.get(alertAttributeId));
			}

			// Get Alert Conditions
			String alertConditionId = alertTypeDefintionJSON.optString("alerttype_AlertConditionId");
			if (StringUtils.isNotBlank(alertConditionId)) {
				hashset.clear();
				hashset.add(alertConditionId);
				Map<String, Record> alertConditionMap = AlertManagementHandler.getAlertConditions(hashset, acceptLanguage, requestInstance);
				if (alertConditionMap.containsKey(alertConditionId) && alertConditionMap.get(alertConditionId) != null)
					operationRecord.addRecord(alertConditionMap.get(alertConditionId));
			}

		}
		return operationRecord;
	}

	/**
	 * Method to fetch the display preferences of Alert Type
	 * 
	 * @param alertTypeId
	 * @param acceptLanguage
	 * @param requestInstance
	 * @return Display Preferences of Alert Type
	 * @throws ApplicationException
	 */
	private Dataset getAlertTypeDisplayPreferences(String alertTypeId, DataControllerRequest requestInstance) throws ApplicationException {

		Dataset operationDataset = new Dataset();
		operationDataset.setId("displayPreferences");

		if (requestInstance == null) {
			// Data Controller Request Instance is NULL. Throw Application Exception.
			LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception.");
			throw new ApplicationException(ErrorCodeEnum.ERR_20899);
		}
		if (StringUtils.isBlank(alertTypeId)) {
			// Missing Mandatory Input. Return Empty Record
			LOG.error("Alert Type Id value is empty. Returning empty record.");
			return operationDataset;
		}

		// Construct Filter Query
		StringBuffer filterQueryBuffer = new StringBuffer();

		filterQueryBuffer.append("AlertTypeId eq '" + alertTypeId + "'");

		// Prepare Input Map
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put(ODataQueryConstants.FILTER, filterQueryBuffer.toString());
		String operationResponse = Executor.invokeService(ServiceURLEnum.DBXALERTTYPETEXT_READ, inputMap, null, requestInstance);
		JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
		if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS) || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
				|| !operationResponseJSON.has("dbxalerttypetext")) {
			LOG.error("Failed to Read dbxalerttypetext");
			throw new ApplicationException(ErrorCodeEnum.ERR_20899);
		}
		LOG.debug("Successfully Read dbxalerttypetext");
		JSONArray alertTypeTextJSONArray = operationResponseJSON.optJSONArray("dbxalerttypetext");
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
	 * Method to get the alert type association with defined apps
	 * 
	 * @param alertTypeId
	 * @param requestInstance
	 * @return Dataset containing alert type association with defined apps
	 * @throws ApplicationException
	 */
	private Dataset getAlertTypeAppAssociation(String alertTypeId, DataControllerRequest requestInstance) throws ApplicationException {

		Dataset operationDataset = new Dataset();
		operationDataset.setId("appTypes");

		if (requestInstance == null) {
			// Data Controller Request Instance is NULL. Throw Application Exception.
			LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception.");
			throw new ApplicationException(ErrorCodeEnum.ERR_20929);
		}
		if (StringUtils.isBlank(alertTypeId)) {
			// Missing Mandatory Input. Return Empty Record
			LOG.error("Alert Type Id value is empty. Returning empty record.");
			return operationDataset;
		}

		// Fetch Alert Type - App association
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put(ODataQueryConstants.FILTER, "alerttype_AlertTypeId eq '" + alertTypeId + "' or alerttype_AlertTypeId eq 'NULL'");
		String readAlertTypeAppViewResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPEAPP_VIEW_READ, inputMap, null, requestInstance);
		JSONObject readAlertTypeAppViewResponseJSON = CommonUtilities.getStringAsJSONObject(readAlertTypeAppViewResponse);
		if (readAlertTypeAppViewResponseJSON == null || !readAlertTypeAppViewResponseJSON.has(FabricConstants.OPSTATUS)
				|| readAlertTypeAppViewResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
				|| !readAlertTypeAppViewResponseJSON.has("alerttypeapp_view")) {
			LOG.error("Failed to Read alerttypeapp_view");
			throw new ApplicationException(ErrorCodeEnum.ERR_20929);
		}
		LOG.debug("Successfully Read alerttypeapp_view");

		JSONArray alertTypeAppsJSONArray = readAlertTypeAppViewResponseJSON.optJSONArray("alerttypeapp_view");
		if (alertTypeAppsJSONArray != null && alertTypeAppsJSONArray.length() > 0) {
			// Construct Operation Result
			JSONObject currJSON;
			for (Object currObject : alertTypeAppsJSONArray) {
				if (currObject instanceof JSONObject) {
					currJSON = (JSONObject) currObject;
					Record currRecord = new Record();
					currRecord.addParam(new Param("appId", currJSON.optString("app_Id"), FabricConstants.STRING));
					currRecord.addParam(new Param("appName", currJSON.optString("app_Name"), FabricConstants.STRING));
					currRecord.addParam(new Param("app_Description", currJSON.optString("app_Description"), FabricConstants.STRING));
					currRecord.addParam(new Param("alertTypeId", currJSON.optString("alerttype_AlertTypeId"), FabricConstants.STRING));
					currRecord.addParam(new Param("alertTypeName", currJSON.optString("alerttype_Name"), FabricConstants.STRING));
					if (StringUtils.equalsIgnoreCase(currJSON.optString("isSupported"), "TRUE") || StringUtils.equalsIgnoreCase(currJSON.optString("isSupported"), "1")) {
						currRecord.addParam(new Param("isSupported", String.valueOf(true), FabricConstants.STRING));
					} else {
						currRecord.addParam(new Param("isSupported", String.valueOf(false), FabricConstants.STRING));
					}
					operationDataset.addRecord(currRecord);
				}
			}
		}
		return operationDataset;
	}

	/**
	 * Method to get the alert type association with user types
	 * 
	 * @param alertTypeId
	 * @param requestInstance
	 * @return Dataset containing alert type association with defined apps
	 * @throws ApplicationException
	 */
	private Dataset getAlertTypeUserTypeAssociation(String alertTypeId, DataControllerRequest requestInstance) throws ApplicationException {

		Dataset operationDataset = new Dataset();
		operationDataset.setId("userTypes");

		if (requestInstance == null) {
			// Data Controller Request Instance is NULL. Throw Application Exception.
			LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception.");
			throw new ApplicationException(ErrorCodeEnum.ERR_20917);
		}
		if (StringUtils.isBlank(alertTypeId)) {
			// Missing Mandatory Input. Return Empty Record
			LOG.error("Alert Type Id value is empty. Returning empty record.");
			return operationDataset;
		}

		// Fetch Alert Type - User Type Association
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put(ODataQueryConstants.FILTER, "alerttype_AlertTypeId eq '" + alertTypeId + "' or alerttype_AlertTypeId eq 'NULL'");
		String readAlertTypeCustomerTypeViewResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPECUSTOMERTYPE_VIEW_READ, inputMap, null, requestInstance);
		JSONObject readAlertTypeCustomerTypeViewResponseJSON = CommonUtilities.getStringAsJSONObject(readAlertTypeCustomerTypeViewResponse);
		if (readAlertTypeCustomerTypeViewResponseJSON == null || !readAlertTypeCustomerTypeViewResponseJSON.has(FabricConstants.OPSTATUS)
				|| readAlertTypeCustomerTypeViewResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
				|| !readAlertTypeCustomerTypeViewResponseJSON.has("alerttypecustomertype_view")) {
			LOG.error("Failed to Read alerttypecustomertype_view");
			throw new ApplicationException(ErrorCodeEnum.ERR_20917);
		}
		LOG.debug("Successfully Read alerttypecustomertype_view");
		JSONArray alertTypeCustomerTypesJSONArray = readAlertTypeCustomerTypeViewResponseJSON.optJSONArray("alerttypecustomertype_view");
		if (alertTypeCustomerTypesJSONArray != null && alertTypeCustomerTypesJSONArray.length() > 0) {
			// Construct Operation Result
			JSONObject currJSON;
			for (Object currObject : alertTypeCustomerTypesJSONArray) {
				if (currObject instanceof JSONObject) {
					currJSON = (JSONObject) currObject;
					Record currRecord = new Record();
					currRecord.addParam(new Param("userTypeId", currJSON.optString("customertype_TypeID"), FabricConstants.STRING));
					currRecord.addParam(new Param("userTypeName", currJSON.optString("customertype_Name"), FabricConstants.STRING));
					currRecord.addParam(new Param("alertTypeId", currJSON.optString("alerttype_AlertTypeId"), FabricConstants.STRING));
					currRecord.addParam(new Param("alertTypeName", currJSON.optString("alerttype_Name"), FabricConstants.STRING));
					if (StringUtils.equalsIgnoreCase(currJSON.optString("isSupported"), "TRUE") || StringUtils.equalsIgnoreCase(currJSON.optString("isSupported"), "1")) {
						currRecord.addParam(new Param("isSupported", String.valueOf(true), FabricConstants.STRING));
					} else {
						currRecord.addParam(new Param("isSupported", String.valueOf(false), FabricConstants.STRING));
					}
					operationDataset.addRecord(currRecord);
				}
			}
		}
		return operationDataset;
	}

	/**
	 * Method to get the alert type association with account types
	 * 
	 * @param alertTypeId
	 * @param requestInstance
	 * @return Dataset containing alert type association with account types
	 * @throws ApplicationException
	 */
	private Dataset getAlertTypeAccountTypeAssociation(String alertTypeId, DataControllerRequest requestInstance) throws ApplicationException {

		Dataset operationDataset = new Dataset();
		operationDataset.setId("accountTypes");

		if (requestInstance == null) {
			// Data Controller Request Instance is NULL. Throw Application Exception.
			LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception.");
			throw new ApplicationException(ErrorCodeEnum.ERR_20916);
		}
		if (StringUtils.isBlank(alertTypeId)) {
			// Missing Mandatory Input. Return Empty Record
			LOG.error("Alert Type Id value is empty. Returning empty record.");
			return operationDataset;
		}

		// Fetch Alert Type - Account Type Association
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put(ODataQueryConstants.FILTER, "alerttype_AlertTypeId eq '" + alertTypeId + "' or alerttype_AlertTypeId eq 'NULL'");
		String readAlertTypeAccountTypeViewResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPEACCOUNTTYPE_VIEW_READ, inputMap, null, requestInstance);
		JSONObject readAlertTypeAccountTypeViewResponseJSON = CommonUtilities.getStringAsJSONObject(readAlertTypeAccountTypeViewResponse);
		if (readAlertTypeAccountTypeViewResponseJSON == null || !readAlertTypeAccountTypeViewResponseJSON.has(FabricConstants.OPSTATUS)
				|| readAlertTypeAccountTypeViewResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
				|| !readAlertTypeAccountTypeViewResponseJSON.has("alerttypeaccounttype_view")) {
			LOG.error("Failed to Read alerttypeaccounttype_view");
			throw new ApplicationException(ErrorCodeEnum.ERR_20916);
		}
		LOG.debug("Successfully Read alerttypeaccounttype_view");
		JSONArray alertTypeAccountTypesJSONArray = readAlertTypeAccountTypeViewResponseJSON.optJSONArray("alerttypeaccounttype_view");
		if (alertTypeAccountTypesJSONArray != null && alertTypeAccountTypesJSONArray.length() > 0) {
			// Construct Operation Result
			JSONObject currJSON;
			for (Object currObject : alertTypeAccountTypesJSONArray) {
				if (currObject instanceof JSONObject) {
					currJSON = (JSONObject) currObject;
					Record currRecord = new Record();
					currRecord.addParam(new Param("accountTypeId", currJSON.optString("accounttype_TypeID"), FabricConstants.STRING));
					currRecord.addParam(new Param("accountTypeName", currJSON.optString("accounttype_displayName"), FabricConstants.STRING));
					currRecord.addParam(new Param("accountTypeDescription", currJSON.optString("accounttype_TypeDescription"), FabricConstants.STRING));
					currRecord.addParam(new Param("alertTypeId", currJSON.optString("alerttype_AlertTypeId"), FabricConstants.STRING));
					currRecord.addParam(new Param("alertTypeName", currJSON.optString("alerttype_Name"), FabricConstants.STRING));
					if (StringUtils.equalsIgnoreCase(currJSON.optString("isSupported"), "TRUE") || StringUtils.equalsIgnoreCase(currJSON.optString("isSupported"), "1")) {
						currRecord.addParam(new Param("isSupported", String.valueOf(true), FabricConstants.STRING));
					} else {
						currRecord.addParam(new Param("isSupported", String.valueOf(false), FabricConstants.STRING));
					}
					operationDataset.addRecord(currRecord);
				}
			}
		}
		return operationDataset;
	}

	/**
	 * Method to get alert sub types associated to alert type
	 * 
	 * @param alertTypeId
	 * @param requestInstance
	 * @return Dataset containing alert sub types associated to alert type
	 * @throws ApplicationException
	 */
	private Dataset getAlertSubTypes(String alertTypeId, DataControllerRequest requestInstance) throws ApplicationException {

		Dataset operationDataset = new Dataset();
		operationDataset.setId("alertSubTypes");

		if (requestInstance == null) {
			// Data Controller Request Instance is NULL. Throw Application Exception.
			LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception.");
			throw new ApplicationException(ErrorCodeEnum.ERR_20946);
		}
		if (StringUtils.isBlank(alertTypeId)) {
			// Missing Mandatory Input. Return Empty Record
			LOG.error("Alert Type Id value is empty. Returning empty record.");
			return operationDataset;
		}

		// Fetch Sub-Alerts
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put(ODataQueryConstants.FILTER, "AlertTypeId eq '" + alertTypeId + "'");
		String readAlertSubTypeResponse = Executor.invokeService(ServiceURLEnum.ALERTSUBTYPE_READ, inputMap, null, requestInstance);
		JSONObject readAlertSubTypeResponseJSON = CommonUtilities.getStringAsJSONObject(readAlertSubTypeResponse);
		if (readAlertSubTypeResponseJSON == null || !readAlertSubTypeResponseJSON.has(FabricConstants.OPSTATUS)
				|| readAlertSubTypeResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
				|| !readAlertSubTypeResponseJSON.has("alertsubtype")) {
			LOG.error("Failed to Read alertsubtype");
			throw new ApplicationException(ErrorCodeEnum.ERR_20946);
		}
		LOG.debug("Successfully Read alertsubtype");
		JSONArray alertSubTypeJSONArray = readAlertSubTypeResponseJSON.optJSONArray("alertsubtype");
		if (alertSubTypeJSONArray != null && alertSubTypeJSONArray.length() > 0) {
			// Construct Operation Result
			JSONObject currJSON;
			for (Object currObject : alertSubTypeJSONArray) {
				if (currObject instanceof JSONObject) {
					currJSON = (JSONObject) currObject;
					Record currRecord = new Record();
					currRecord.addParam(new Param("code", currJSON.optString("id"), FabricConstants.STRING));
					currRecord.addParam(new Param("status", currJSON.optString("Status_id"), FabricConstants.STRING));
					currRecord.addParam(new Param("name", currJSON.optString("Name"), FabricConstants.STRING));
					currRecord.addParam(new Param("description", currJSON.optString("Description"), FabricConstants.STRING));
					operationDataset.addRecord(currRecord);
				}
			}
		}
		return operationDataset;
	}

}
