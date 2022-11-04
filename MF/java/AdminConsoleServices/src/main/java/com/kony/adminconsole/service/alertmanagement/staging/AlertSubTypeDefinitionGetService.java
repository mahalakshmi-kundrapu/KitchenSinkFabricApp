package com.kony.adminconsole.service.alertmanagement.staging;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to fetch the Alert Sub Type Details
 *
 * @author Aditya Mankal
 */
public class AlertSubTypeDefinitionGetService implements JavaService2 {

	private static final String LOCALE_PARAM = "Locale";
	private static final String SUB_ALERT_ID_PARAM = "SubAlertId";
	private static final String TEMPLATE_STATUS_PARAM = "TemplateStatus";

	private static final Logger LOG = Logger.getLogger(AlertSubTypeDefinitionGetService.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) {

		Result processedResult = new Result();
		try {
			// Read Inputs
			String subAlertId = requestInstance.getParameter(SUB_ALERT_ID_PARAM);
			LOG.debug("Received Sub-Alert ID:" + subAlertId);

			String templateStatusId = requestInstance.getParameter(TEMPLATE_STATUS_PARAM);
			LOG.debug("Received Template Status ID:" + templateStatusId);

			String locale = requestInstance.getParameter(LOCALE_PARAM);
			LOG.debug("Received Locale ID:" + locale);

			// Validate Inputs
			if (StringUtils.isBlank(subAlertId)) {
				// Missing Alert Category ID
				LOG.error("Missing Sub Alert ID");
				ErrorCodeEnum.ERR_20910.setErrorCode(processedResult);
				return processedResult;
			}

			// Format Language Identifier
			locale = CommonUtilities.formatLanguageIdentifier(locale);

			// Get Alert SubType Definition
			Record alertSubTypeDefinitionRecord = getAlertSubTypeDefinition(subAlertId, requestInstance);
			processedResult.addRecord(alertSubTypeDefinitionRecord);

			// Get Alert SubType Communication Templates
			Record communicationTemplatesRecord = getCommunicationTemplatesOfAlertSubType(subAlertId, locale, templateStatusId, requestInstance);
			processedResult.addRecord(communicationTemplatesRecord);

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
	 * Method to get the Alert Sub Type data as a Record
	 * 
	 * @param subAlertId
	 * @param requestInstance
	 * @return Record containing the details of Alert Sub Type
	 * @throws ApplicationException
	 */
	private Record getAlertSubTypeDefinition(String subAlertId, DataControllerRequest requestInstance) throws ApplicationException {
		Record operationRecord = new Record();
		operationRecord.setId("subAlertDefinition");

		if (requestInstance == null) {
			// Data Controller Request Instance is NULL. Throw Application Exception.
			LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception");
			throw new ApplicationException(ErrorCodeEnum.ERR_20927);
		}
		if (StringUtils.isBlank(subAlertId)) {
			// Missing Mandatory Inputs. Returning Empty Record
			LOG.error("Missing Mandatory Inputs. Returning Empty Record");
			return operationRecord;
		}

		Map<String, String> inputMap = new HashMap<>();
		String filterQuery = "id eq '" + subAlertId + "'";
		inputMap.put(ODataQueryConstants.FILTER, filterQuery);
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
			if (alertSubTypeJSONArray.opt(0) instanceof JSONObject) {
				JSONObject currJSON = alertSubTypeJSONArray.optJSONObject(0);
				operationRecord.addParam(new Param("code", currJSON.optString("id"), FabricConstants.STRING));
				operationRecord.addParam(new Param("name", currJSON.optString("Name"), FabricConstants.STRING));
				operationRecord.addParam(new Param("description", currJSON.optString("Description"), FabricConstants.STRING));
				operationRecord.addParam(new Param("status", currJSON.optString("Status_id"), FabricConstants.STRING));
			}

		}
		return operationRecord;
	}

	/**
	 * Method to get the Communication Templates of an Alert SubType
	 * 
	 * @param subAlertId
	 * @param acceptLanguage
	 * @param templateStatus
	 * @param requestInstance
	 * @return Record containing the Communication Templates of an Alert SubType
	 * @throws ApplicationException
	 */
	private Record getCommunicationTemplatesOfAlertSubType(String subAlertId, String acceptLanguage, String templateStatusId, DataControllerRequest requestInstance)
			throws ApplicationException {

		Record resultRecord = new Record();
		resultRecord.setId("communicationTemplates");

		if (requestInstance == null) {
			// Data Controller Request Instance is NULL. Throw Application Exception.
			LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception");
			throw new ApplicationException(ErrorCodeEnum.ERR_20927);
		}
		if (StringUtils.isBlank(subAlertId)) {
			// Missing Mandatory Inputs. Returning Empty Record
			LOG.error("Missing Mandatory Inputs. Returning Empty Record");
			return resultRecord;
		}

		// Construct Filter Query
		String filterQuery = "communicationtemplate_AlertSubTypeId eq '" + subAlertId + "'";
		if (StringUtils.isNoneBlank(acceptLanguage)) {
			filterQuery = filterQuery + " and communicationtemplate_LanguageCode eq '" + acceptLanguage + "'";
		}
		if (StringUtils.isNotBlank(templateStatusId)) {
			filterQuery = filterQuery + " communicationtemplate_Status_id eq '" + templateStatusId + "'";
		}

		// Prepare Input Map
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put(ODataQueryConstants.FILTER, filterQuery);

		// Fetch Communication Templates
		String readCommunicationTemplateChannelViewResponse = Executor.invokeService(ServiceURLEnum.COMMUNICATIONTEMPLATE_CHANNEL_VIEW_READ, inputMap, null, requestInstance);

		// Check Fetch Status
		JSONObject readCommunicationTemplateChannelViewResponseJSON = CommonUtilities.getStringAsJSONObject(readCommunicationTemplateChannelViewResponse);
		if (readCommunicationTemplateChannelViewResponseJSON == null || !readCommunicationTemplateChannelViewResponseJSON.has(FabricConstants.OPSTATUS)
				|| readCommunicationTemplateChannelViewResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
				|| !readCommunicationTemplateChannelViewResponseJSON.has("communicationtemplate_channel_view")) {
			LOG.error("Failed to Read communicationtemplate_channel_view");
			throw new ApplicationException(ErrorCodeEnum.ERR_20911);
		}
		LOG.debug("Successfully Read communicationtemplate_channel_view");

		JSONObject currJSON = null;
		String currLocale, currStatus, currChannel;
		Map<String, Record> localeChannelMap = null;
		Map<String, Map<String, Record>> statusRecordMap = new HashMap<>();
		JSONArray communicationTemplateArray = readCommunicationTemplateChannelViewResponseJSON.optJSONArray("communicationtemplate_channel_view");

		// Construct Response in hierarchical order. Nesting Sequence: Status -> Channels -> Locales ->
		for (Object currObj : communicationTemplateArray) {
			if (currObj instanceof JSONObject) {

				currJSON = (JSONObject) currObj;
				currLocale = currJSON.optString("communicationtemplate_LanguageCode");
				currStatus = currJSON.optString("communicationtemplate_Status_id");
				currChannel = currJSON.optString("communicationtemplate_ChannelID");

				// Verify values
				if (StringUtils.isBlank(currStatus) || StringUtils.isBlank(currLocale) || StringUtils.isBlank(currChannel)) {
					continue;
				}

				/*
				 * Each Status Record contains a list of Locale Records,
				 * and each Locale Record contains a list of Channel Records
				 */
				if (statusRecordMap.containsKey(currStatus)) {
					localeChannelMap = statusRecordMap.get(currStatus);
				} else {
					localeChannelMap = new HashMap<>();
					statusRecordMap.put(currStatus, localeChannelMap);
				}

				// Check if the current locale has already been associated to the current Status Record
				Record currLocaleRecord = null;
				if (localeChannelMap.containsKey(currLocale)) {
					currLocaleRecord = localeChannelMap.get(currLocale);
				} else {
					currLocaleRecord = new Record();
					currLocaleRecord.setId(currLocale);
					localeChannelMap.put(currLocale, currLocaleRecord);
				}

				// Create a new record for every channel of the current locale
				Record currChannelRecord = new Record();
				currChannelRecord.setId(currChannel);

				// Populate the template content
				currChannelRecord.addParam(new Param("templateId", currJSON.optString("communicationtemplate_id"), FabricConstants.STRING));
				currChannelRecord.addParam(new Param("templateName", currJSON.optString("communicationtemplate_Name"), FabricConstants.STRING));
				currChannelRecord.addParam(new Param("languageCode", currJSON.optString("communicationtemplate_LanguageCode"), FabricConstants.STRING));
				currChannelRecord.addParam(new Param("statusId", currJSON.optString("communicationtemplate_Status_id"), FabricConstants.STRING));

				currChannelRecord.addParam(new Param("channelId", currJSON.optString("communicationtemplate_ChannelID"), FabricConstants.STRING));
				currChannelRecord.addParam(new Param("channelDisplayName", currJSON.optString("channeltext_Description"), FabricConstants.STRING));
				currChannelRecord.addParam(new Param("content", currJSON.optString("communicationtemplate_Text"), FabricConstants.STRING));

				if (currJSON.has("communicationtemplate_Subject")) {
					currChannelRecord.addParam(new Param("templateSubject", currJSON.optString("communicationtemplate_Subject"), FabricConstants.STRING));
				}
				if (currJSON.has("communicationtemplate_SenderName")) {
					currChannelRecord.addParam(new Param("senderName", currJSON.optString("communicationtemplate_SenderName"), FabricConstants.STRING));
				}
				if (currJSON.has("communicationtemplate_SenderEmail")) {
					currChannelRecord.addParam(new Param("senderEmail", currJSON.optString("communicationtemplate_SenderEmail"), FabricConstants.STRING));
				}
				currChannelRecord.addParam(new Param("softdeleteflag", currJSON.optString("communicationtemplate_softdeleteflag"), FabricConstants.STRING));

				// Add the current Channel Record to the Current Locale Record
				currLocaleRecord.addRecord(currChannelRecord);
			}
		}

		// Traverse the prepared map to construct the Result Record
		for (Entry<String, Map<String, Record>> statusRecordMapEntry : statusRecordMap.entrySet()) {

			currStatus = statusRecordMapEntry.getKey();

			// Allocate one record per status
			Record currStatusRecord = new Record();
			currStatusRecord.setId(currStatus);

			// Add the Templates of the current status to the status record
			localeChannelMap = statusRecordMapEntry.getValue();
			for (Entry<String, Record> localeChannelMapEntry : localeChannelMap.entrySet()) {
				currStatusRecord.addRecord(localeChannelMapEntry.getValue());
			}

			// Add the prepared status record to the Result Record
			resultRecord.addRecord(currStatusRecord);
		}

		return resultRecord;
	}

}
