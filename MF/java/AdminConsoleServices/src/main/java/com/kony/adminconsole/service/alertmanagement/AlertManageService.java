package com.kony.adminconsole.service.alertmanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
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
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to Create, Update and Delete the Alerts. Functions as per the invoked operation.
 *
 * @author Aditya Mankal
 * 
 */

public class AlertManageService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(AlertManageService.class);
    private static final int ALERT_NAME_MIN_CHARS = 5;
    private static final int ALERT_NAME_MAX_CHARS = 50;
    private static final int ALERT_DESC_MIN_CHARS = 10;
    private static final int ALERT_DESC_MAX_CHARS = 250;
    private static final int ALERT_CONTENT_MIN_CHARS = 10;
    private static final int ALERT_CONTENT_MAX_CHARS = 250;
    private static final String CREATE_ALERT_OPERATION_NAME = "createAlert";
    private static final String UPDATE_ALERT_OPERATION_NAME = "updateAlert";
    private static final String DELETE_ALERT_OPERATION_NAME = "deleteAlert";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);

            String alertID = requestInstance.getParameter("alertID");
            String alertName = requestInstance.getParameter("alertName");
            String alertTypeID = requestInstance.getParameter("alertTypeID");
            String alertDescription = requestInstance.getParameter("alertDescription");
            String alertStatusID = requestInstance.getParameter("status_ID");
            String alertContent = requestInstance.getParameter("alertContent");

            String isAlertEnabledOn_SMS = requestInstance.getParameter("isAlertEnabledOn_SMS");
            String isAlertEnabledOn_EmailEnabled = requestInstance.getParameter("isAlertEnabledOn_Email");
            String isAlertEnabledOn_PushEnabled = requestInstance.getParameter("isAlertEnabledOn_Push");
            String isAlertEnabledOn_All = requestInstance.getParameter("isAlertEnabledOn_All");

            if (methodID.equalsIgnoreCase(CREATE_ALERT_OPERATION_NAME)) {
                return createAlert(userDetailsBeanInstance, alertName, alertTypeID, alertDescription, alertContent,
                        alertStatusID, isAlertEnabledOn_SMS, isAlertEnabledOn_EmailEnabled,
                        isAlertEnabledOn_PushEnabled, isAlertEnabledOn_All, requestInstance, authToken);
            }

            else if (methodID.equalsIgnoreCase(UPDATE_ALERT_OPERATION_NAME)) {
                return updateAlert(userDetailsBeanInstance, alertID, alertName, alertTypeID, alertDescription,
                        alertContent, alertStatusID, isAlertEnabledOn_SMS, isAlertEnabledOn_EmailEnabled,
                        isAlertEnabledOn_PushEnabled, isAlertEnabledOn_All, requestInstance, authToken);
            }

            else if (methodID.equalsIgnoreCase(DELETE_ALERT_OPERATION_NAME)) {
                return deleteAlert(userDetailsBeanInstance, alertID, requestInstance, authToken);
            }

            return new Result();
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    private Result updateAlert(UserDetailsBean userDetailsBeanInstance, String alertID, String alertName,
            String alertTypeID, String alertDescription, String alertContent, String alertStatusID,
            String isAlertEnabledOn_SMS, String isAlertEnabledOn_EmailEnabled, String isAlertEnabledOn_PushEnabled,
            String isAlertEnabledOn_All, DataControllerRequest requestInstance, String authToken) {
        LOG.debug("In updateAlert");
        Result processedResult = new Result();

        Map<String, String> postParametersMap = new HashMap<String, String>();

        boolean isValidData = true;
        StringBuffer errorMessageBuffer = new StringBuffer();
        errorMessageBuffer.append("ERROR:\n");

        if (StringUtils.isBlank(alertID)) {
            isValidData = false;
            errorMessageBuffer
                    .append("\nAlert ID is a mandatory input for an Update Request.It cannot be a NULL/Empty input.");
        } else {
            postParametersMap.put("id", alertID);
        }

        if (alertTypeID != null) {
            postParametersMap.put("AlertType_id", alertTypeID);
        }

        if (alertName != null) {
            if (alertName.length() < ALERT_NAME_MIN_CHARS || alertName.length() > ALERT_NAME_MAX_CHARS) {
                isValidData = false;
                errorMessageBuffer.append("\nAlert Name should have atleast " + ALERT_NAME_MIN_CHARS
                        + " characters and a maximum of " + ALERT_NAME_MAX_CHARS + " characters");
            } else {
                postParametersMap.put("Name", alertName);
            }
        }

        if (alertDescription != null) {
            if (alertDescription.length() < ALERT_DESC_MIN_CHARS || alertDescription.length() > ALERT_DESC_MAX_CHARS) {
                isValidData = false;
                errorMessageBuffer.append("\nAlert Description should have atleast " + ALERT_DESC_MIN_CHARS
                        + " characters and a maximum of " + ALERT_DESC_MAX_CHARS + " characters");
            } else {
                postParametersMap.put("Description", alertDescription);
            }
        }

        if (alertContent != null) {
            if (alertContent.length() < ALERT_CONTENT_MIN_CHARS || alertContent.length() > ALERT_CONTENT_MAX_CHARS) {
                isValidData = false;
                errorMessageBuffer.append("\nAlert Content should have atleast " + ALERT_CONTENT_MIN_CHARS
                        + " characters and a maximum of " + ALERT_CONTENT_MAX_CHARS + " characters");
            } else {
                postParametersMap.put("AlertContent", alertContent);
            }
        }

        if (alertStatusID != null) {
            postParametersMap.put("Status_id", alertStatusID);
        }
        if (isValidData) {
            if (isAlertEnabledOn_All != null && (isAlertEnabledOn_All.equalsIgnoreCase("TRUE")
                    || isAlertEnabledOn_All.equalsIgnoreCase("1") || isAlertEnabledOn_All.equalsIgnoreCase("YES"))) {
                postParametersMap.put("IsPushActive", "1");
                postParametersMap.put("IsSmsActive", "1");
                postParametersMap.put("IsEmailActive", "1");
            } else {
                if (isAlertEnabledOn_SMS != null) {
                    if (isAlertEnabledOn_SMS.equalsIgnoreCase("TRUE") || isAlertEnabledOn_SMS.equalsIgnoreCase("1")
                            || isAlertEnabledOn_SMS.equalsIgnoreCase("YES"))
                        postParametersMap.put("IsSmsActive", "1");
                    else
                        postParametersMap.put("IsSmsActive", "0");
                }

                if (isAlertEnabledOn_EmailEnabled != null) {
                    if (isAlertEnabledOn_EmailEnabled.equalsIgnoreCase("TRUE")
                            || isAlertEnabledOn_EmailEnabled.equalsIgnoreCase("1")
                            || isAlertEnabledOn_EmailEnabled.equalsIgnoreCase("YES"))
                        postParametersMap.put("IsEmailActive", "1");
                    else
                        postParametersMap.put("IsEmailActive", "0");
                }

                if (isAlertEnabledOn_PushEnabled != null) {
                    if (isAlertEnabledOn_PushEnabled.equalsIgnoreCase("TRUE")
                            || isAlertEnabledOn_PushEnabled.equalsIgnoreCase("1")
                            || isAlertEnabledOn_PushEnabled.equalsIgnoreCase("YES"))
                        postParametersMap.put("IsPushActive", "1");
                    else
                        postParametersMap.put("IsPushActive", "0");
                }
            }

            String updateAlertResponse = Executor.invokeService(ServiceURLEnum.ALERT_UPDATE, postParametersMap, null,
                    requestInstance);
            JSONObject updateAlertResponseJSON = CommonUtilities.getStringAsJSONObject(updateAlertResponse);
            if (updateAlertResponseJSON != null && updateAlertResponseJSON.has(FabricConstants.OPSTATUS)
                    && updateAlertResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                LOG.debug("Update Alert Call Status: Successful. Alert Id:" + alertID);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
                        ActivityStatusEnum.SUCCESSFUL, "Alert update successful. Alert name: " + alertName);
                return processedResult;
            } else {
                LOG.debug("Update Alert Call Status: Failed. Alert Id:" + alertID);
                ErrorCodeEnum.ERR_20903.setErrorCode(processedResult);
                processedResult.addParam(new Param("updateAlertResponse", updateAlertResponse, FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "Alert update failed. Alert name: " + alertName);
                return processedResult;
            }
        } else {
            LOG.error("Alert Data Validation Status: Invalid. Alert Id:" + alertID);
            ErrorCodeEnum.ERR_20901.setErrorCode(processedResult);
            processedResult
                    .addParam(new Param("validationError", errorMessageBuffer.toString(), FabricConstants.STRING));
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Alert data is invalid. Alert name: " + alertName);
            return processedResult;
        }

    }

    private Result createAlert(UserDetailsBean userDetailsBeanInstance, String alertName, String alertTypeID,
            String alertDescription, String alertContent, String alertStatusID, String isAlertEnabledOn_SMS,
            String isAlertEnabledOn_EmailEnabled, String isAlertEnabledOn_PushEnabled, String isAlertEnabledOn_All,
            DataControllerRequest requestInstance, String authToken) {
        LOG.debug("In createAlert");
        Result processedResult = new Result();

        Map<String, String> postParametersMap = new HashMap<String, String>();

        boolean isValidData = true;
        StringBuffer errorMessageBuffer = new StringBuffer();
        errorMessageBuffer.append("ERROR:\n");

        String alertID = CommonUtilities.getNewId().toString();
        postParametersMap.put("id", alertID);

        if (alertTypeID != null && alertTypeID.length() > 1) {
            postParametersMap.put("AlertType_id", alertTypeID);
        } else {
            isValidData = false;
            errorMessageBuffer.append("\nAlert Type is a mandatory Input and it cannot be NULL.");
        }

        if (alertName == null || alertName.length() < ALERT_NAME_MIN_CHARS
                || alertName.length() > ALERT_NAME_MAX_CHARS) {
            isValidData = false;
            errorMessageBuffer.append("\nAlert Name should have atleast " + ALERT_NAME_MIN_CHARS
                    + " characters and a maximum of " + ALERT_NAME_MAX_CHARS + " characters");
        } else {
            postParametersMap.put("Name", alertName);
        }

        if (alertDescription == null || alertDescription.length() < ALERT_DESC_MIN_CHARS
                || alertDescription.length() > ALERT_DESC_MAX_CHARS) {
            isValidData = false;
            errorMessageBuffer.append("\nAlert Description should have atleast " + ALERT_DESC_MIN_CHARS
                    + " characters and a maximum of " + ALERT_DESC_MAX_CHARS + " characters");
        } else {
            postParametersMap.put("Description", alertDescription);
        }

        if (alertContent == null || alertContent.length() < ALERT_CONTENT_MIN_CHARS
                || alertContent.length() > ALERT_CONTENT_MAX_CHARS) {
            isValidData = false;
            errorMessageBuffer.append("\nAlert Content should have atleast " + ALERT_CONTENT_MIN_CHARS
                    + " characters and a maximum of " + ALERT_CONTENT_MAX_CHARS + " characters");
        } else {
            postParametersMap.put("AlertContent", alertContent);
        }

        if (isValidData) {
            LOG.debug("Alert Data Validation Status: Valid");
            if (alertStatusID != null) {
                postParametersMap.put("Status_id", alertStatusID);
            } else
                postParametersMap.put("Status_id", StatusEnum.SID_ACTIVE.name());

            if (isAlertEnabledOn_All != null && (isAlertEnabledOn_PushEnabled.equalsIgnoreCase("TRUE")
                    || isAlertEnabledOn_PushEnabled.equalsIgnoreCase("1")
                    || isAlertEnabledOn_PushEnabled.equalsIgnoreCase("YES"))) {
                postParametersMap.put("IsPushActive", "1");
                postParametersMap.put("IsSmsActive", "1");
                postParametersMap.put("IsEmailActive", "1");
            } else {
                if (isAlertEnabledOn_SMS != null) {
                    if (isAlertEnabledOn_SMS.equalsIgnoreCase("TRUE") || isAlertEnabledOn_SMS.equalsIgnoreCase("1")
                            || isAlertEnabledOn_SMS.equalsIgnoreCase("YES"))
                        postParametersMap.put("IsSmsActive", "1");
                    else
                        postParametersMap.put("IsSmsActive", "0");
                } else
                    postParametersMap.put("IsSmsActive", "0");

                if (isAlertEnabledOn_EmailEnabled != null) {
                    if (isAlertEnabledOn_EmailEnabled.equalsIgnoreCase("TRUE")
                            || isAlertEnabledOn_EmailEnabled.equalsIgnoreCase("1")
                            || isAlertEnabledOn_EmailEnabled.equalsIgnoreCase("YES"))
                        postParametersMap.put("IsEmailActive", "1");
                    else
                        postParametersMap.put("IsEmailActive", "0");
                } else
                    postParametersMap.put("IsEmailActive", "0");

                if (isAlertEnabledOn_PushEnabled != null) {
                    if (isAlertEnabledOn_PushEnabled.equalsIgnoreCase("TRUE")
                            || isAlertEnabledOn_PushEnabled.equalsIgnoreCase("1")
                            || isAlertEnabledOn_PushEnabled.equalsIgnoreCase("YES"))
                        postParametersMap.put("IsPushActive", "1");
                    else
                        postParametersMap.put("IsPushActive", "0");
                } else
                    postParametersMap.put("IsPushActive", "0");
            }
            String createAlertResponse = Executor.invokeService(ServiceURLEnum.ALERT_CREATE, postParametersMap, null,
                    requestInstance);
            JSONObject createAlertResponseJSON = CommonUtilities.getStringAsJSONObject(createAlertResponse);
            if (createAlertResponseJSON != null && createAlertResponseJSON.has(FabricConstants.OPSTATUS)
                    && createAlertResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                LOG.debug("Create Alert Call Status: Successful. Alert Id:" + alertID);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.CREATE,
                        ActivityStatusEnum.SUCCESSFUL, "Alert create successful. Alert name: " + alertName);
                return processedResult;
            } else {
                LOG.debug("Create Alert Call Status: Failed");
                ErrorCodeEnum.ERR_20902.setErrorCode(processedResult);
                processedResult.addParam(new Param("createAlertResponse", createAlertResponse, FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED, "Alert create failed. Alert name: " + alertName);
                return processedResult;
            }
        } else {
            LOG.error("Alert Data Validation Status: Invalid");
            ErrorCodeEnum.ERR_20901.setErrorCode(processedResult);
            processedResult
                    .addParam(new Param("validationError", errorMessageBuffer.toString(), FabricConstants.STRING));
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "Alert data is invalid. Alert name: " + alertName);
            return processedResult;
        }

    }

    private Result deleteAlert(UserDetailsBean userDetailsBeanInstance, String alertID,
            DataControllerRequest requestInstance, String authToken) {

        Result processedResult = new Result();
        LOG.debug("In deleteAlert");
        Map<String, String> postParametersMap = new HashMap<String, String>();

        if (StringUtils.isBlank(alertID)) {
            LOG.debug("Missing Mandatory Input: alertId");
            String errorMessage = "Alert ID is a mandatory input for a Delete Request.It cannot be a NULL/Empty input.";
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "Alert delete failed");
            ErrorCodeEnum.ERR_20901.setErrorCode(processedResult);
            processedResult.addParam(new Param("validationError", errorMessage, FabricConstants.STRING));
            return processedResult;
        } else {
            postParametersMap.put("id", alertID);
            String deleteAlertResponse = Executor.invokeService(ServiceURLEnum.ALERT_DELETE, postParametersMap, null,
                    requestInstance);
            JSONObject deleteAlertResponseJSON = CommonUtilities.getStringAsJSONObject(deleteAlertResponse);

            if (deleteAlertResponseJSON != null && deleteAlertResponseJSON.has(FabricConstants.OPSTATUS)
                    && deleteAlertResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                LOG.debug("Delete Alert call Status:Successful. Alert Id:" + alertID);
                ;
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.DELETE,
                        ActivityStatusEnum.SUCCESSFUL, "Alert delete successful");
                return processedResult;
            } else {
                LOG.debug("Delete Alert call Failed. Alert Id:" + alertID);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.DELETE,
                        ActivityStatusEnum.FAILED, "Alert delete failed");
                ErrorCodeEnum.ERR_20904.setErrorCode(processedResult);
                processedResult.addParam(new Param("deleteAlertResponse", deleteAlertResponse, FabricConstants.STRING));
                return processedResult;
            }
        }
    }
}