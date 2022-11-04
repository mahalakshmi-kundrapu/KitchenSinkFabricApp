package com.kony.adminconsole.service.alertmanagement;

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

public class AlertTypeManageService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(AlertTypeManageService.class);

    private static final int ALERTTYPE_NAME_MIN_CHARS = 5;
    private static final int ALERTTYPE_NAME_MAX_CHARS = 30;
    private static final int ALERTTYPE_DESC_MIN_CHARS = 5;
    private static final int ALERTTYPE_DESC_MAX_CHARS = 250;

    private static final String CREATE_ALERTTYPE_OPERATION_NAME = "createAlertType";
    private static final String UPDATE_ALERTTYPE_OPERATION_NAME = "updateAlertType";
    private static final String DELETE_ALERTTYPE_OPERATION_NAME = "deleteAlertType";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);

            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            String userID = userDetailsBeanInstance.getUserId();

            String alertTypeID = requestInstance.getParameter("alertTypeID");
            String alertTypeName = requestInstance.getParameter("alertTypeName");
            String alertTypeDescription = requestInstance.getParameter("alertTypeDescription");
            String alertTypeStatusID = requestInstance.getParameter("status_ID");
            String isAlertSubscriptionRequired = requestInstance.getParameter("isAlertSubscriptionRequired");

            if (methodID.equalsIgnoreCase(CREATE_ALERTTYPE_OPERATION_NAME)) {
                return createAlertType(userID, alertTypeName, alertTypeDescription, alertTypeStatusID,
                        isAlertSubscriptionRequired, requestInstance, authToken);
            }

            else if (methodID.equalsIgnoreCase(UPDATE_ALERTTYPE_OPERATION_NAME)) {
                return updateAlertType(userID, alertTypeID, alertTypeName, alertTypeDescription, alertTypeStatusID,
                        isAlertSubscriptionRequired, requestInstance, authToken);
            }

            else if (methodID.equalsIgnoreCase(DELETE_ALERTTYPE_OPERATION_NAME)) {
                return deleteAlertType(userID, alertTypeID, requestInstance, authToken);
            }
            return new Result();
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    private Result deleteAlertType(String userID, String alertTypeID, DataControllerRequest requestInstance,
            String authToken) {
        LOG.debug("In deleteAlertType");
        Result processedResult = new Result();
        Map<String, String> postParametersMap = new HashMap<String, String>();

        if (StringUtils.isBlank(alertTypeID)) {
            String errorMessage =
                    "AlertType ID is a mandatory input for a Delete Request.It cannot be a NULL/Empty input.";
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "AlertType delete failed");
            ErrorCodeEnum.ERR_20905.setErrorCode(processedResult);
            processedResult.addParam(new Param("validationError", errorMessage, FabricConstants.STRING));
            return processedResult;
        } else {
            // Delete linked Alerts
            postParametersMap.put(ODataQueryConstants.SELECT, "id");
            postParametersMap.put(ODataQueryConstants.FILTER, "AlertType_id eq '" + alertTypeID + "'");
            String readAlertResponse = Executor.invokeService(ServiceURLEnum.ALERT_READ, postParametersMap, null,
                    requestInstance);
            postParametersMap.clear();
            postParametersMap.put("AlertType_id", alertTypeID);

            JSONObject readAlertResponseJSON = CommonUtilities.getStringAsJSONObject(readAlertResponse);
            if (readAlertResponseJSON != null && readAlertResponseJSON.has(FabricConstants.OPSTATUS)
                    && readAlertResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readAlertResponseJSON.has("alert")) {
                Record deleteAlertsRecord = new Record();
                deleteAlertsRecord.setId("deleteAlerts");
                processedResult.addRecord(deleteAlertsRecord);
                JSONArray alertRecordsJSONArray = readAlertResponseJSON.getJSONArray("alert");
                for (int indexVar = 0; indexVar < alertRecordsJSONArray.length(); indexVar++) {
                    JSONObject currAlertJSONObject = alertRecordsJSONArray.getJSONObject(indexVar);
                    if (currAlertJSONObject.has("id")) {
                        String currAlertID = currAlertJSONObject.optString("id");
                        postParametersMap.put("id", currAlertID);
                        String deleteAlertResponse = Executor.invokeService(ServiceURLEnum.ALERT_DELETE,
                                postParametersMap, null, requestInstance);
                        JSONObject deleteAlertResponseJSON = CommonUtilities.getStringAsJSONObject(deleteAlertResponse);
                        if (deleteAlertResponseJSON != null && deleteAlertResponseJSON.has(FabricConstants.OPSTATUS)
                                && deleteAlertResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.DELETE,
                                    ActivityStatusEnum.SUCCESSFUL, "Alert delete successful");
                        } else {
                            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.DELETE,
                                    ActivityStatusEnum.FAILED, "Alert delete failed");
                        }
                        postParametersMap.remove("id");
                        deleteAlertsRecord.addParam(
                                new Param("DeleteAlert_" + currAlertID, deleteAlertResponse, FabricConstants.STRING));
                    }
                }
            }

            postParametersMap.clear();
            postParametersMap.put("id", alertTypeID);
            String deleteAlertTypeResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPE_DELETE, postParametersMap,
                    null, requestInstance);
            JSONObject deleteAlertTypeResponseJSON = CommonUtilities.getStringAsJSONObject(deleteAlertTypeResponse);
            if (deleteAlertTypeResponseJSON != null && deleteAlertTypeResponseJSON.has(FabricConstants.OPSTATUS)
                    && deleteAlertTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                LOG.debug("Alert Type Delete Status:Successful. Alert Type Id:" + alertTypeID);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.DELETE,
                        ActivityStatusEnum.SUCCESSFUL, "AlertType delete successful");
                return processedResult;
            } else {
                LOG.debug("Alert Type Delete Status:Failed. Alert Type Id:" + alertTypeID);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.DELETE,
                        ActivityStatusEnum.FAILED, "AlertType delete failed");
                ErrorCodeEnum.ERR_20908.setErrorCode(processedResult);
                return processedResult;
            }
        }
    }

    private Result updateAlertType(String userID, String alertTypeID, String alertTypeName, String alertTypeDescription,
            String alertTypeStatusID, String isAlertSubscriptionRequired, DataControllerRequest requestInstance,
            String authToken) {

        Result processedResult = new Result();
        Map<String, String> postParametersMap = new HashMap<String, String>();
        boolean isValidData = true;
        StringBuffer errorMessageBuffer = new StringBuffer();
        errorMessageBuffer.append("ERROR:\n");

        if (StringUtils.isBlank(alertTypeID)) {
            isValidData = false;
            errorMessageBuffer.append(
                    "\nAlertType ID is a mandatory input for an Update Request.It cannot be a NULL/Empty input.");
        } else {
            postParametersMap.put("id", alertTypeID);
        }

        if (alertTypeName != null) {
            if (alertTypeName.length() < ALERTTYPE_NAME_MIN_CHARS
                    || alertTypeName.length() > ALERTTYPE_NAME_MAX_CHARS) {
                isValidData = false;
                errorMessageBuffer.append("\nAlert Type Name should have atleast " + ALERTTYPE_NAME_MIN_CHARS
                        + " characters and a maximum of " + ALERTTYPE_NAME_MAX_CHARS + " characters");
            } else {
                postParametersMap.put("Name", alertTypeName);
            }
        }

        if (alertTypeDescription != null) {
            if (alertTypeDescription.length() < ALERTTYPE_DESC_MIN_CHARS
                    || alertTypeDescription.length() > ALERTTYPE_DESC_MAX_CHARS) {
                isValidData = false;
                errorMessageBuffer.append("\nAlert Type Description should have atleast " + ALERTTYPE_DESC_MIN_CHARS
                        + " characters and a maximum of " + ALERTTYPE_DESC_MAX_CHARS + " characters");
            } else {
                postParametersMap.put("Description", alertTypeDescription);
            }
        }

        if (isAlertSubscriptionRequired != null) {
            if (isAlertSubscriptionRequired.equalsIgnoreCase("TRUE")
                    || isAlertSubscriptionRequired.equalsIgnoreCase("1")
                    || isAlertSubscriptionRequired.equalsIgnoreCase("YES"))
                postParametersMap.put("IsSubscriptionNeeded", "1");
            else
                postParametersMap.put("IsSubscriptionNeeded", "0");
        }

        /*
         * if(alertTypeStatusID!=null) { postParametersMap.put("Status_ID", alertTypeStatusID); }
         */

        if (isValidData) {
            String updateAlertTypeResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPE_UPDATE, postParametersMap,
                    null, requestInstance);
            JSONObject updateAlertTypeResponseJSON = CommonUtilities.getStringAsJSONObject(updateAlertTypeResponse);
            if (updateAlertTypeResponseJSON != null && updateAlertTypeResponseJSON.has(FabricConstants.OPSTATUS)
                    && updateAlertTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                LOG.debug("Alert Type Update Status:Successful. Alert Type Id:" + alertTypeID);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
                        ActivityStatusEnum.SUCCESSFUL, "AlertType update success");
                return processedResult;
            } else {
                LOG.debug("Alert Type Update Status:Failure. Alert Type Id:" + alertTypeID);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "AlertType update failed");
                processedResult.addParam(
                        new Param("updateAlertTypeResponse", updateAlertTypeResponse, FabricConstants.STRING));
                ErrorCodeEnum.ERR_20907.setErrorCode(processedResult);
                return processedResult;
            }
        } else {
            LOG.debug("Alert Type Delete Status:Failed. Alert Type Id:" + alertTypeID);
            ErrorCodeEnum.ERR_20905.setErrorCode(processedResult);
            processedResult.addParam(new Param("updateAlert", errorMessageBuffer.toString(), FabricConstants.STRING));
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "AlertType invalid data");
            return processedResult;
        }

    }

    private Result createAlertType(String userID, String alertTypeName, String alertTypeDescription,
            String alertTypeStatusID, String isAlertSubscriptionRequired, DataControllerRequest requestInstance,
            String authToken) {
        LOG.debug("createAlertType");

        Result processedResult = new Result();

        Map<String, String> postParametersMap = new HashMap<String, String>();
        boolean isValidData = true;
        StringBuffer errorMessageBuffer = new StringBuffer();
        errorMessageBuffer.append("ERROR:\n");

        String alertTypeID = CommonUtilities.getNewId().toString();
        postParametersMap.put("id", alertTypeID);

        if (alertTypeName == null || alertTypeName.length() < ALERTTYPE_NAME_MIN_CHARS
                || alertTypeName.length() > ALERTTYPE_NAME_MAX_CHARS) {
            isValidData = false;
            errorMessageBuffer.append("\nAlertType Name should have atleast " + ALERTTYPE_NAME_MIN_CHARS
                    + " characters and a maximum of " + ALERTTYPE_NAME_MAX_CHARS + " characters");
        } else {
            postParametersMap.put("Name", alertTypeName);
        }

        if (alertTypeDescription == null || alertTypeDescription.length() < ALERTTYPE_DESC_MIN_CHARS
                || alertTypeDescription.length() > ALERTTYPE_DESC_MAX_CHARS) {
            isValidData = false;
            errorMessageBuffer.append("\nAlertType Description should have atleast " + ALERTTYPE_DESC_MAX_CHARS
                    + " characters and a maximum of " + ALERTTYPE_DESC_MAX_CHARS + " characters");
        } else {
            postParametersMap.put("Description", alertTypeDescription);
        }

        if (isAlertSubscriptionRequired.equalsIgnoreCase("TRUE") || isAlertSubscriptionRequired.equalsIgnoreCase("1")
                || isAlertSubscriptionRequired.equalsIgnoreCase("YES"))
            postParametersMap.put("IsSubscriptionNeeded", "1");
        else
            postParametersMap.put("IsSubscriptionNeeded", "0");

        /*
         * if(alertTypeStatusID!=null) { postParametersMap.put("Status_ID", alertTypeStatusID); }
         */

        if (isValidData) {
            String createAlertTypeResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPE_CREATE, postParametersMap,
                    null, requestInstance);
            JSONObject createAlertTypeResponseJSON = CommonUtilities.getStringAsJSONObject(createAlertTypeResponse);
            if (createAlertTypeResponseJSON != null && createAlertTypeResponseJSON.has(FabricConstants.OPSTATUS)
                    && createAlertTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                LOG.debug("Create Alert Type Call Status:Successful. Alert Type Id:" + alertTypeID);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.CREATE,
                        ActivityStatusEnum.SUCCESSFUL, "AlertType create successful");
                return processedResult;
            } else {
                LOG.debug("Create Alert Type Call Status:Failed. Alert Type Id:" + alertTypeID);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED, "AlertType create failed");
                ErrorCodeEnum.ERR_20906.setErrorCode(processedResult);
                processedResult.addParam(
                        new Param("createAlertTypeResponse", createAlertTypeResponse, FabricConstants.STRING));
                return processedResult;
            }
        } else {
            LOG.debug("Alert Type Delete Status:Failed. Alert Type Id:" + alertTypeID);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "AlertType create failed, invalid data");
            processedResult.addParam(new Param("createAlert", errorMessageBuffer.toString(), FabricConstants.STRING));
            ErrorCodeEnum.ERR_20905.setErrorCode(processedResult);
            return processedResult;
        }
    }

}