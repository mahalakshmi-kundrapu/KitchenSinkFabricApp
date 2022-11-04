package com.kony.adminconsole.service.alertmanagement.staging;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
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
import com.kony.adminconsole.utilities.StatusEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to create/edit the Alert Sub Type Details
 *
 * @author Aditya Mankal
 */
public class AlertSubTypeManageService implements JavaService2 {

	private static final String SUB_ALERT_NAME_PARAM = "name";
	private static final String SUB_ALERT_CODE_PARAM = "code";
	private static final String ALERT_TYPE_CODE = "alertTypeCode";
	private static final String STATUS_ID_PARAM = "statusId";
	private static final String SUB_ALERT_DESCRIPTION_PARAM = "description";

	private static final String ADDED_TEMPLATES_PARAM = "addedTemplates";
	private static final String REMOVED_TEMPLATES_PARAM = "removedTemplates";

	private static final int SUB_ALERT_NAME_MIN_LENGTH = 5;
	private static final int SUB_ALERT_NAME_MAX_LENGTH = 50;

	private static final int SUB_ALERT_DESCRIPTION_MIN_LENGTH = 5;
	private static final int SUB_ALERT_DESCRIPTION_MAX_LENGTH = 200;

	private static final String CREATE_ALERT_SUB_TYPE_METHOD_ID = "createAlertSubType";
	private static final String EDIT_ALERT_SUB_TYPE_METHOD_ID = "editAlertSubType";

	private static final Logger LOG = Logger.getLogger(AlertSubTypeManageService.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) {

		Result processedResult = new Result();

		EventEnum eventEnum = EventEnum.UPDATE;
		String activityMessage = StringUtils.EMPTY;

		try {

			LOG.debug("Method Id:" + methodID);
			activityMessage = "Alert SubType Id:" + requestInstance.getParameter(SUB_ALERT_CODE_PARAM);

			// Fetch Logged In User Info
			String loggedInUser = StringUtils.EMPTY;
			UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);

			if (userDetailsBeanInstance != null) {
				loggedInUser = userDetailsBeanInstance.getUserId();
			}

			if (StringUtils.equals(methodID, CREATE_ALERT_SUB_TYPE_METHOD_ID)) {
				processedResult = setAlertSubTypeDefintion(requestInstance, true, loggedInUser);
				eventEnum = EventEnum.CREATE;
			}

			else if (StringUtils.equals(methodID, EDIT_ALERT_SUB_TYPE_METHOD_ID)) {
				processedResult = setAlertSubTypeDefintion(requestInstance, false, loggedInUser);
				eventEnum = EventEnum.UPDATE;

			} else {
				// Unsupported Method Id. Return Empty Result
				LOG.debug("Unsupported Method Id. Returning Empty Result");
				return new Result();
			}

			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, eventEnum,
					ActivityStatusEnum.SUCCESSFUL, activityMessage);

			return processedResult;

		} catch (ApplicationException e) {
			LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);

			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, eventEnum,
					ActivityStatusEnum.FAILED, activityMessage);

			Result errorResult = new Result();
			errorResult.addParam(new Param("status", "failure", FabricConstants.STRING));
			e.getErrorCodeEnum().setErrorCode(errorResult);
			return errorResult;
		} catch (Exception e) {
			LOG.error("Unexpected Exception.Exception Trace:", e);

			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, eventEnum,
					ActivityStatusEnum.FAILED, activityMessage);

			Result errorResult = new Result();
			errorResult.addParam(new Param("status", "failure", FabricConstants.STRING));
			ErrorCodeEnum.ERR_20926.setErrorCode(errorResult);
			return errorResult;
		}
	}

	/**
	 * Method to create/update an Alert Sub Type
	 * 
	 * @param requestInstance
	 * @param isCreateMode
	 * @param loggedInUser
	 * @return operation Result
	 * @throws ApplicationException
	 * @throws UnsupportedEncodingException 
	 */
	private Result setAlertSubTypeDefintion(DataControllerRequest requestInstance, boolean isCreateMode, String loggedInUser) throws ApplicationException, UnsupportedEncodingException {
		Result operationResult = new Result();

		String code = requestInstance.getParameter(SUB_ALERT_CODE_PARAM);
		LOG.debug("Recieved Sub Alert Code:" + code);

		String alertTypeCode = requestInstance.getParameter(ALERT_TYPE_CODE);
		LOG.debug("Recieved Alert Type Code:" + alertTypeCode);

		String name = requestInstance.getParameter(SUB_ALERT_NAME_PARAM);
		LOG.debug("Recieved Sub Alert Name:" + name);

		String description = requestInstance.getParameter(SUB_ALERT_DESCRIPTION_PARAM);
		LOG.debug("Recieved Sub Alert Description Length:" + StringUtils.length(description));

		String statusId = requestInstance.getParameter(STATUS_ID_PARAM);
		LOG.debug("Recieved Status Id:" + statusId);

		String addedTemplatesStr = requestInstance.getParameter(ADDED_TEMPLATES_PARAM);
		JSONArray addedTemplates = CommonUtilities.getStringAsJSONArray(addedTemplatesStr);

		String removedTemplatesStr = requestInstance.getParameter(REMOVED_TEMPLATES_PARAM);
		JSONArray removedTemplates = CommonUtilities.getStringAsJSONArray(removedTemplatesStr);

		// Alert Code is a mandatory input for both create/update requests. Alert Code is chosen as a dropdown from the client
		if (StringUtils.isBlank(code)) {
			LOG.error("Invalid Input: SubAlertCode");
			operationResult.addParam(new Param("message", "Sub Alert Code cannot be empty"));
			operationResult.addParam(new Param("status", "failure", FabricConstants.STRING));
			ErrorCodeEnum.ERR_20900.setErrorCode(operationResult);
			return operationResult;
		}

		if (StringUtils.isBlank(alertTypeCode)) {
			LOG.error("Invalid Input: AlertTypeCode");
			operationResult.addParam(new Param("message", "Alert Type Code cannot be empty"));
			operationResult.addParam(new Param("status", "failure", FabricConstants.STRING));
			ErrorCodeEnum.ERR_20900.setErrorCode(operationResult);
			return operationResult;
		}

		if (isCreateMode == true || StringUtils.isNotBlank(name)) {
			if (StringUtils.length(name) < SUB_ALERT_NAME_MIN_LENGTH || StringUtils.length(name) > SUB_ALERT_NAME_MAX_LENGTH) {
				LOG.error("Invalid Input: SubAlertName");
				operationResult.addParam(new Param("message",
						"Sub Alert Name should have a minimum of " + SUB_ALERT_NAME_MIN_LENGTH +
								" characters and a maximum of " + SUB_ALERT_NAME_MAX_LENGTH + " characters",
						FabricConstants.STRING));
				operationResult.addParam(new Param("status", "failure", FabricConstants.STRING));
				ErrorCodeEnum.ERR_20900.setErrorCode(operationResult);
				return operationResult;
			}
		}

		if (isCreateMode == true || StringUtils.isNotBlank(description)) {
			if (StringUtils.length(description) < SUB_ALERT_DESCRIPTION_MIN_LENGTH || StringUtils.length(description) > SUB_ALERT_DESCRIPTION_MAX_LENGTH) {
				LOG.error("Invalid Input: SubAlertDescription");
				operationResult.addParam(new Param("message",
						"Sub Alert Description should have a minimum of " + SUB_ALERT_DESCRIPTION_MIN_LENGTH +
								" characters and a maximum of " + SUB_ALERT_DESCRIPTION_MAX_LENGTH + " characters",
						FabricConstants.STRING));
				operationResult.addParam(new Param("status", "failure", FabricConstants.STRING));
				ErrorCodeEnum.ERR_20900.setErrorCode(operationResult);
				return operationResult;
			}
		}

		// Status Id
		if (isCreateMode == true || (StringUtils.isNotBlank(statusId))) {
			if (!StatusEnum.isValidStatusCode(statusId)) {
				LOG.error("Invalid Input: statusId");
				operationResult.addParam(new Param("message", "Invalid Status Id"));
				ErrorCodeEnum.ERR_20900.setErrorCode(operationResult);
				operationResult.addParam(new Param("status", "failure", FabricConstants.STRING));
				return operationResult;
			}
		}

		// Prepare Input Map
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put("id", code);
		inputMap.put("AlertTypeId", alertTypeCode);
		if (StringUtils.isNotBlank(name)) {
			inputMap.put("Name", name);
		}
		if (StringUtils.isNotBlank(description)) {
			inputMap.put("Description", description);
		}
		if (StringUtils.isNotBlank(statusId)) {
			inputMap.put("Status_id", statusId);
		}

		// Create/Update Alert Sub Type
		String operationResponse;
		if (isCreateMode == true) {
			inputMap.put("createdby", loggedInUser);
			inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
			operationResponse = Executor.invokeService(ServiceURLEnum.ALERTSUBTYPE_CREATE, inputMap, null, requestInstance);
		} else {
			inputMap.put("modifiedby", loggedInUser);
			inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
			operationResponse = Executor.invokeService(ServiceURLEnum.ALERTSUBTYPE_UPDATE, inputMap, null, requestInstance);
		}
		JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
		if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS) || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
			LOG.error("Failed CRUD Operation");
			operationResult.addParam(new Param("message", operationResponse, FabricConstants.STRING));
			ErrorCodeEnum.ERR_20900.setErrorCode(operationResult);
			return operationResult;
		}

		// Set Alert Sub Type Communication Templates
		setAlertSubTypeCommunicationTemplates(requestInstance, code, addedTemplates, removedTemplates, loggedInUser);
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
				ActivityStatusEnum.SUCCESSFUL, "AlertSubType Update Success");
		operationResult.addParam(new Param("status", "success", FabricConstants.STRING));
		return operationResult;

	}

	/**
	 * Method to create/update/delete communication templates of an Alert Sub Type
	 * 
	 * @param requestInstance
	 * @param alertTypeCode
	 * @param addedTemplates
	 * @param removedTemplates
	 * @param loggedInUser
	 * @throws ApplicationException
	 * @throws UnsupportedEncodingException 
	 */
	private void setAlertSubTypeCommunicationTemplates(DataControllerRequest requestInstance, String alertTypeCode, JSONArray addedTemplates, JSONArray removedTemplates,
			String loggedInUser) throws ApplicationException, UnsupportedEncodingException {

		Record operationRecord = new Record();
		operationRecord.setId("communicationTemplates");

		String operationResponse;
		JSONObject currJSON, operationResponseJSON;

		Map<String, String> inputMap = new HashMap<>();

		// Fetch Associated Templates
		// <Key, CommunicationTemplateId>
		Map<String, String> associatedTemplates = new HashMap<>();
		inputMap.put(ODataQueryConstants.FILTER, "AlertSubTypeId eq '" + alertTypeCode + "'");
		operationResponse = Executor.invokeService(ServiceURLEnum.COMMUNICATIONTEMPLATE_READ, inputMap, null, requestInstance);
		operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
		if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS) || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
				|| !operationResponseJSON.has("communicationtemplate")) {
			LOG.error("Failed CRUD Operation");
			throw new ApplicationException(ErrorCodeEnum.ERR_20900);
		}
		LOG.debug("Successful CRUD Operation");
		JSONArray communicationTemplateJSONArray = operationResponseJSON.optJSONArray("communicationtemplate");

		String currTemplateId;
		StringBuffer currKeyBuffer = null;
		for (Object currObject : communicationTemplateJSONArray) {
			if (currObject instanceof JSONObject) {
				currJSON = (JSONObject) currObject;
				currKeyBuffer = new StringBuffer();

				if (StringUtils.isNotBlank(currJSON.optString("LanguageCode"))) {
					currKeyBuffer.append(currJSON.optString("LanguageCode") + "$");
				}
				if (StringUtils.isNotBlank(currJSON.optString("ChannelID"))) {
					currKeyBuffer.append(currJSON.optString("ChannelID") + "$");
				}
				if (StringUtils.isNotBlank(currJSON.optString("Status_id"))) {
					currKeyBuffer.append(currJSON.optString("Status_id"));
				}
				currTemplateId = currJSON.optString("Id");
				associatedTemplates.put(currKeyBuffer.toString(), currTemplateId);
			}
		}

		// Handle Added Templates
		if (addedTemplates != null && addedTemplates.length() > 0) {
			inputMap.clear();
			String currLocale, currStatusId, currChannelId, currContent, currName, currSubject, currSenderName, currSenderEmail;
			for (Object currObject : addedTemplates) {
				if (currObject instanceof JSONObject) {
					currJSON = (JSONObject) currObject;
					currKeyBuffer = new StringBuffer();

					currLocale = currJSON.optString("locale");
					currChannelId = currJSON.optString("channelId");
					currStatusId = currJSON.optString("statusId");
					currContent = currJSON.optString("content");

					if (StringUtils.isNotBlank(currLocale) && StatusEnum.isValidStatusCode(currStatusId) && StringUtils.isNotBlank(currChannelId)) {

						currName = currJSON.optString("name");
						currSubject = currJSON.optString("subject");
						currSenderName = currJSON.optString("senderName");
						currSenderEmail = currJSON.optString("senderEmail");

						inputMap.clear();
						inputMap.put("AlertSubTypeId", alertTypeCode);

						if (StringUtils.isNotBlank(currName)) {
							inputMap.put("Name", currName);
						}
						if (StringUtils.isNotBlank(currLocale)) {
							inputMap.put("LanguageCode", currLocale);
						}
						if (StringUtils.isNotBlank(currChannelId)) {
							inputMap.put("ChannelID", currChannelId);
						}
						if (StringUtils.isNotBlank(currStatusId)) {
							inputMap.put("Status_id", currStatusId);
						}
						if (StringUtils.isNotBlank(currContent)) {
							inputMap.put("Text", CommonUtilities.encodeToBase64(CommonUtilities.encodeURI(currContent)));
							inputMap.put("rtx", "Text");
						}
						if (StringUtils.isNotBlank(currSubject)) {
							inputMap.put("Subject", currSubject);
						}
						if (StringUtils.isNotBlank(currSenderName)) {
							inputMap.put("SenderName", currSenderName);
						}
						if (StringUtils.isNotBlank(currSenderEmail)) {
							inputMap.put("SenderEmail", currSenderEmail);
						}

						currKeyBuffer.append(currLocale + "$" + currChannelId + "$" + currStatusId);
						if (associatedTemplates.containsKey(currKeyBuffer.toString())) {
							// Existing Template. Update record
							currTemplateId = associatedTemplates.get(currKeyBuffer.toString());
							inputMap.put("Id", currTemplateId);
							inputMap.put("modifiedby", loggedInUser);
							inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
							operationResponse = Executor.invokeService(ServiceURLEnum.COMMUNICATIONTEMPLATE_UPDATE, inputMap, null, requestInstance);
						} else {
							// Non Existing Template. Create record
							currTemplateId = String.valueOf(CommonUtilities.getNumericId());
							inputMap.put("Id", currTemplateId);
							inputMap.put("createdby", currLocale);
							inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
							operationResponse = Executor.invokeService(ServiceURLEnum.COMMUNICATIONTEMPLATE_CREATE, inputMap, null, requestInstance);
							associatedTemplates.put(currKeyBuffer.toString(), currTemplateId);
						}
						operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
						if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS) || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
							LOG.error("Failed CRUD Operation. Attempted Template Record:" + currKeyBuffer.toString());
							throw new ApplicationException(ErrorCodeEnum.ERR_20900);
						}
						LOG.debug("Successful CRUD Operation. Attempted Template Record:" + currKeyBuffer.toString());
					}
				}
			}
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
					ActivityStatusEnum.SUCCESSFUL, "AlertSubType Update Success");
		}

		if (removedTemplates != null && removedTemplates.length() > 0) {
			inputMap.clear();
			for (int index = 0; index < removedTemplates.length(); index++) {
				if (removedTemplates.opt(index) instanceof String) {

					currTemplateId = removedTemplates.optString(index);
					if (associatedTemplates.values().contains(currTemplateId)) {

						inputMap.put("Id", currTemplateId);
						operationResponse = Executor.invokeService(ServiceURLEnum.COMMUNICATIONTEMPLATE_DELETE, inputMap, null, requestInstance);

						operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
						if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS) || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
							LOG.error("Failed CRUD Operation. Attempted Template Id:" + currTemplateId);
							throw new ApplicationException(ErrorCodeEnum.ERR_20900);
						}
						LOG.debug("Successful CRUD Operation. Attempted Template Id:" + currTemplateId);
					}
				}

			}
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
					ActivityStatusEnum.SUCCESSFUL, "AlertSubType Update Success");
		}
	}

}
