package com.kony.adminconsole.service.mfa;

/**
 * Service to Manage Requests related to MFAConfiguration
 * 
 * @author Chandan Gupta - KH2516
 *
 */

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
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class MFAConfigurationManageService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(MFAConfigurationManageService.class);

    private static final int DEFAULT_SAC_CODE_LENGTH_MIN_LENGTH = 3;
    private static final int DEFAULT_SAC_CODE_LENGTH_MAX_LENGTH = 8;
    private static final int DEFAULT_SAC_CODE_EXPIRES_AFTER_MIN_LENGTH = 1;
    private static final int DEFAULT_SAC_CODE_EXPIRES_AFTER_MAX_LENGTH = 10;
    private static final int DEFAULT_SAC_MAX_RESEND_REQUESTS_ALLOWED_MIN_LENGTH = 0;
    private static final int DEFAULT_SAC_MAX_RESEND_REQUESTS_ALLOWED_MAX_LENGTH = 5;
    private static final int DEFAULT_SAC_MAX_FAILED_ATTEMPTS_ALLOWED_MIN_LENGTH = 1;
    private static final int DEFAULT_SAC_MAX_FAILED_ATTEMPTS_ALLOWED_MAX_LENGTH = 5;
    private static final int DEFAULT_SQ_NUMBER_OF_QUESTION_ASKED_MIN_LENGTH = 1;
    private static final int DEFAULT_SQ_NUMBER_OF_QUESTION_ASKED_MAX_LENGTH = 2;
    private static final int DEFAULT_SQ_MAX_FAILED_ATTEMPTS_ALLOWED_MIN_LENGTH = 1;
    private static final int DEFAULT_SQ_MAX_FAILED_ATTEMPTS_ALLOWED_MAX_LENGTH = 5;

    private static final String EDIT_MFA_CONFIGURATION_METHOD_NAME = "editMFAConfiguration";
    private static final String GET_MFA_CONFIGURATION_METHOD_NAME = "getMFAConfiguration";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            if (StringUtils.equalsIgnoreCase(methodID, EDIT_MFA_CONFIGURATION_METHOD_NAME)) {
                return editMFAConfiguration(requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, GET_MFA_CONFIGURATION_METHOD_NAME)) {
                return getMFAConfiguration(requestInstance);
            }

            return null;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    public Result editMFAConfiguration(DataControllerRequest requestInstance) {

        Result operationResult = new Result();
        try {
            int sacCodeLengthMinLength = DEFAULT_SAC_CODE_LENGTH_MIN_LENGTH;
            int sacCodeLengthMaxLength = DEFAULT_SAC_CODE_LENGTH_MAX_LENGTH;
            int sacCodeExpiresAfterMinLength = DEFAULT_SAC_CODE_EXPIRES_AFTER_MIN_LENGTH;
            int sacCodeExpiresAfterMaxLength = DEFAULT_SAC_CODE_EXPIRES_AFTER_MAX_LENGTH;
            int sacMaxResendRequestsAllowedMinLength = DEFAULT_SAC_MAX_RESEND_REQUESTS_ALLOWED_MIN_LENGTH;
            int sacMaxResendRequestsAllowedMaxLength = DEFAULT_SAC_MAX_RESEND_REQUESTS_ALLOWED_MAX_LENGTH;
            int sacMaxFailedAttemptsAllowedMinLength = DEFAULT_SAC_MAX_FAILED_ATTEMPTS_ALLOWED_MIN_LENGTH;
            int sacMaxFailedAttemptsAllowedMaxLength = DEFAULT_SAC_MAX_FAILED_ATTEMPTS_ALLOWED_MAX_LENGTH;
            int sqNumberOfQuestionAskedMinLength = DEFAULT_SQ_NUMBER_OF_QUESTION_ASKED_MIN_LENGTH;
            int sqNumberOfQuestionAskedMaxLength = DEFAULT_SQ_NUMBER_OF_QUESTION_ASKED_MAX_LENGTH;
            int sqMaxFailedAttemptsAllowedMinLength = DEFAULT_SQ_MAX_FAILED_ATTEMPTS_ALLOWED_MIN_LENGTH;
            int sqMaxFailedAttemptsAllowedMaxLength = DEFAULT_SQ_MAX_FAILED_ATTEMPTS_ALLOWED_MAX_LENGTH;

            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);

            String mfaType = requestInstance.getParameter("mfaTypeId");

            if (StringUtils.isBlank(mfaType)) {
                ErrorCodeEnum.ERR_21300.setErrorCode(operationResult);
                LOG.error("MFA Type is empty");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "MFA Configuration update failed");
                return operationResult;
            }

            String mfaTypeName = null;

            Map<String, String> inputBodyMap = new HashMap<String, String>();
            inputBodyMap.put(ODataQueryConstants.FILTER, "id eq '" + mfaType + "'");
            inputBodyMap.put(ODataQueryConstants.SELECT, "id, Name");
            String readMFATypeResponse = Executor.invokeService(ServiceURLEnum.MFATYPE_READ, inputBodyMap, null,
                    requestInstance);
            JSONObject readMFATypeResponseJSON = CommonUtilities.getStringAsJSONObject(readMFATypeResponse);
            if (!(readMFATypeResponseJSON != null && readMFATypeResponseJSON.has(FabricConstants.OPSTATUS)
                    && readMFATypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0)) {
                operationResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_21303.setErrorCode(operationResult);
                LOG.error("Failed to Read MFA Type");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "MFA Configuration update failed");
                return operationResult;
            } else {
                JSONArray readMFATypeResponseJSONArray = readMFATypeResponseJSON.optJSONArray("mfatype");
                if (!(readMFATypeResponseJSONArray == null || readMFATypeResponseJSONArray.length() < 1)) {
                    JSONObject currMFATypeRecord = readMFATypeResponseJSONArray.getJSONObject(0);
                    mfaTypeName = currMFATypeRecord.getString("Name");
                }
            }

            String auditSuccessDescription = "MFA Configuration update successful. MFA: " + mfaTypeName;

            boolean isLockUser = false, isLogoutUser = false;
            String lockUserVal = "false", logoutUserVal = "false";

            JSONArray readMFAConfigJSONArray = new JSONArray(requestInstance.getParameter("mfaConfigurations"));
            if ((readMFAConfigJSONArray != null) && (readMFAConfigJSONArray.length() > 0)) {
                for (int indexVar = 0; indexVar < readMFAConfigJSONArray.length(); indexVar++) {
                    JSONObject mfaConfigJSONObject = readMFAConfigJSONArray.getJSONObject(indexVar);
                    String mfaKey = mfaConfigJSONObject.getString("mfaKey");
                    String mfaValue = mfaConfigJSONObject.getString("mfaValue");

                    if (StringUtils.isBlank(mfaValue)) {
                        ErrorCodeEnum.ERR_21311.setErrorCode(operationResult);
                        LOG.error("Failed to Read MFA Key Value");
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS,
                                EventEnum.UPDATE, ActivityStatusEnum.FAILED, "MFA Configuration update failed");
                        return operationResult;
                    }

                    String oldMFAConfigValue = null;
                    inputBodyMap.clear();
                    inputBodyMap.put(ODataQueryConstants.FILTER,
                            "MFA_id eq '" + mfaType + "' and MFAKey_id eq '" + mfaKey + "'");
                    inputBodyMap.put(ODataQueryConstants.SELECT, "MFA_id, MFAKey_id, value");
                    String readMFAConfigResponse = Executor.invokeService(ServiceURLEnum.MFACONFIGURATIONS_READ,
                            inputBodyMap, null, requestInstance);
                    JSONObject readMFAConfigResponseJSON = CommonUtilities.getStringAsJSONObject(readMFAConfigResponse);
                    if (!(readMFAConfigResponseJSON != null && readMFAConfigResponseJSON.has(FabricConstants.OPSTATUS)
                            && readMFAConfigResponseJSON.getInt(FabricConstants.OPSTATUS) == 0)) {
                        operationResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        ErrorCodeEnum.ERR_21304.setErrorCode(operationResult);
                        LOG.error("Failed to Read MFA Config");
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS,
                                EventEnum.UPDATE, ActivityStatusEnum.FAILED, "MFA Configuration update failed");
                        return operationResult;
                    } else {
                        JSONArray readMFAConfigResponseJSONArray = readMFAConfigResponseJSON
                                .optJSONArray("mfaconfigurations");
                        if (!(readMFAConfigResponseJSONArray == null || readMFAConfigResponseJSONArray.length() < 1)) {
                            JSONObject MFAConfigRecord = readMFAConfigResponseJSONArray.getJSONObject(0);
                            oldMFAConfigValue = MFAConfigRecord.getString("value");
                        }
                    }

                    // Min and Max length validations
                    if (mfaKey.equals("SAC_CODE_LENGTH") && ((Integer.parseInt(mfaValue) < sacCodeLengthMinLength)
                            || (Integer.parseInt(mfaValue) > sacCodeLengthMaxLength))) {
                        ErrorCodeEnum.ERR_21312.setErrorCode(operationResult);
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS,
                                EventEnum.UPDATE, ActivityStatusEnum.FAILED, "MFA Configuration update failed");
                        LOG.error("Secure Access code length value should be between min and max value");
                        return operationResult;
                    } else if (mfaKey.equals("SAC_CODE_EXPIRES_AFTER")
                            && ((Integer.parseInt(mfaValue) < sacCodeExpiresAfterMinLength)
                                    || (Integer.parseInt(mfaValue) > sacCodeExpiresAfterMaxLength))) {
                        ErrorCodeEnum.ERR_21313.setErrorCode(operationResult);
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS,
                                EventEnum.UPDATE, ActivityStatusEnum.FAILED, "MFA Configuration update failed");
                        LOG.error("Secure Access code expire after should be between min and max value");
                        return operationResult;
                    } else if (mfaKey.equals("SAC_MAX_RESEND_REQUESTS_ALLOWED")
                            && ((Integer.parseInt(mfaValue) < sacMaxResendRequestsAllowedMinLength)
                                    || (Integer.parseInt(mfaValue) > sacMaxResendRequestsAllowedMaxLength))) {
                        ErrorCodeEnum.ERR_21314.setErrorCode(operationResult);
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS,
                                EventEnum.UPDATE, ActivityStatusEnum.FAILED, "MFA Configuration update failed");
                        LOG.error("Secure Access code max resend request allowed should be between min and max value");
                        return operationResult;
                    } else if (mfaKey.equals("MAX_FAILED_ATTEMPTS_ALLOWED") && mfaType.equals("SECURE_ACCESS_CODE")
                            && ((Integer.parseInt(mfaValue) < sacMaxFailedAttemptsAllowedMinLength)
                                    || (Integer.parseInt(mfaValue) > sacMaxFailedAttemptsAllowedMaxLength))) {
                        ErrorCodeEnum.ERR_21315.setErrorCode(operationResult);
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS,
                                EventEnum.UPDATE, ActivityStatusEnum.FAILED, "MFA Configuration update failed");
                        LOG.error("Secure Access code max failed attempt allowed should be between min and max value");
                        return operationResult;
                    } else if (mfaKey.equals("SQ_NUMBER_OF_QUESTION_ASKED")
                            && ((Integer.parseInt(mfaValue) < sqNumberOfQuestionAskedMinLength)
                                    || (Integer.parseInt(mfaValue) > sqNumberOfQuestionAskedMaxLength))) {
                        ErrorCodeEnum.ERR_21316.setErrorCode(operationResult);
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS,
                                EventEnum.UPDATE, ActivityStatusEnum.FAILED, "MFA Configuration update failed");
                        LOG.error("Security Question number of questions should be between min and max value");
                        return operationResult;
                    } else if (mfaKey.equals("MAX_FAILED_ATTEMPTS_ALLOWED") && mfaType.equals("SECURITY_QUESTIONS")
                            && ((Integer.parseInt(mfaValue) < sqMaxFailedAttemptsAllowedMinLength)
                                    || (Integer.parseInt(mfaValue) > sqMaxFailedAttemptsAllowedMaxLength))) {
                        ErrorCodeEnum.ERR_21317.setErrorCode(operationResult);
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS,
                                EventEnum.UPDATE, ActivityStatusEnum.FAILED, "MFA Configuration update failed");
                        LOG.error("Security Question max failed attempt allowed should be between min and max value");
                        return operationResult;
                    }

                    if (mfaKey.equals("LOCK_USER")) {
                        isLockUser = true;
                        lockUserVal = mfaValue;
                    }
                    if (mfaKey.equals("LOGOUT_USER")) {
                        isLogoutUser = true;
                        logoutUserVal = mfaValue;
                    }

                    String mfaKeyDescription = null;

                    inputBodyMap.clear();
                    inputBodyMap.put(ODataQueryConstants.FILTER, "id eq '" + mfaKey + "'");
                    inputBodyMap.put(ODataQueryConstants.SELECT, "Description");
                    String readMFAKeyResponse = Executor.invokeService(ServiceURLEnum.MFAKEY_READ, inputBodyMap, null,
                            requestInstance);
                    JSONObject readMFAKeyResponseJSON = CommonUtilities.getStringAsJSONObject(readMFAKeyResponse);
                    if (!(readMFAKeyResponseJSON != null && readMFAKeyResponseJSON.has(FabricConstants.OPSTATUS)
                            && readMFAKeyResponseJSON.getInt(FabricConstants.OPSTATUS) == 0)) {
                        operationResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        ErrorCodeEnum.ERR_21347.setErrorCode(operationResult);
                        LOG.error("Failed to Read MFA Key");
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS,
                                EventEnum.UPDATE, ActivityStatusEnum.FAILED, "MFA Configuration update failed");
                        return operationResult;
                    } else {
                        JSONArray readMFAKeyResponseJSONArray = readMFAKeyResponseJSON.optJSONArray("mfakey");
                        if (!(readMFAKeyResponseJSONArray == null || readMFAKeyResponseJSONArray.length() < 1)) {
                            JSONObject currMFAKeyRecord = readMFAKeyResponseJSONArray.getJSONObject(0);
                            mfaKeyDescription = currMFAKeyRecord.getString("Description");
                        }
                    }

                    inputBodyMap.clear();
                    inputBodyMap.put("MFA_id", mfaType);
                    inputBodyMap.put("MFAKey_id", mfaKey);
                    inputBodyMap.put("value", mfaValue);
                    inputBodyMap.put("modifiedby", userDetailsBeanInstance.getUserName());
                    inputBodyMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
                    String editMFAConfigResponse = Executor.invokeService(ServiceURLEnum.MFACONFIGURATIONS_UPDATE,
                            inputBodyMap, null, requestInstance);
                    JSONObject editMFAConfigResponseJSON = CommonUtilities.getStringAsJSONObject(editMFAConfigResponse);
                    if (!(editMFAConfigResponseJSON != null && editMFAConfigResponseJSON.has(FabricConstants.OPSTATUS)
                            && editMFAConfigResponseJSON.getInt(FabricConstants.OPSTATUS) == 0)) {
                        operationResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        ErrorCodeEnum.ERR_21301.setErrorCode(operationResult);
                        LOG.error("Failed to Edit MFA Config");
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS,
                                EventEnum.UPDATE, ActivityStatusEnum.FAILED, "MFA Configuration update failed");
                        return operationResult;
                    }
                    LOG.error("MFA Configuration update successful. MFA: " + mfaTypeName + "/" + mfaKeyDescription + ":"
                            + mfaValue);
                    if (!mfaValue.equals(oldMFAConfigValue))
                        auditSuccessDescription += "/" + mfaKeyDescription + ":" + mfaValue;
                }
            }

            if (isLockUser && lockUserVal.equals("true") && ((!isLogoutUser) || logoutUserVal.equals("false"))) {
                inputBodyMap.clear();
                inputBodyMap.put("MFA_id", mfaType);
                inputBodyMap.put("MFAKey_id", "LOGOUT_USER");
                inputBodyMap.put("value", "true");
                inputBodyMap.put("modifiedby", userDetailsBeanInstance.getUserName());
                inputBodyMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
                String editMFAConfigResponse = Executor.invokeService(ServiceURLEnum.MFACONFIGURATIONS_UPDATE,
                        inputBodyMap, null, requestInstance);
                JSONObject editMFAConfigResponseJSON = CommonUtilities.getStringAsJSONObject(editMFAConfigResponse);
                if (!(editMFAConfigResponseJSON != null && editMFAConfigResponseJSON.has(FabricConstants.OPSTATUS)
                        && editMFAConfigResponseJSON.getInt(FabricConstants.OPSTATUS) == 0)) {
                    operationResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    ErrorCodeEnum.ERR_21301.setErrorCode(operationResult);
                    LOG.error("Failed to Edit MFA Config");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS, EventEnum.UPDATE,
                            ActivityStatusEnum.FAILED, "MFA Configuration update failed");
                    return operationResult;
                }
                auditSuccessDescription += "/" + "Logout" + ":" + "true";
                LOG.error("MFA Configuration update successful. MFA: " + mfaTypeName + "/" + "Logout User" + ":"
                        + "true");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS, EventEnum.UPDATE,
                        ActivityStatusEnum.SUCCESSFUL, "MFA Configuration update successful. MFA: " + mfaTypeName + "/"
                                + "Logout User" + ":" + "true");
            }

            if (isLockUser && lockUserVal.equals("true") && isLogoutUser && logoutUserVal.equals("false")) {
                operationResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_21306.setErrorCode(operationResult);
                LOG.error("Logout user field should be true if lock user is true");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "MFA Configuration update failed");
                return operationResult;
            }
            operationResult.addParam(new Param("status", "Success", FabricConstants.STRING));
            LOG.error("MFA Configuration updated successfully");
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL, auditSuccessDescription);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_21301.setErrorCode(operationResult);
            LOG.error("MFA Configuration update failed");
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFACONFIGURATIONS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "MFA Configuration update failed");
            return operationResult;
        }
        return operationResult;
    }

    public Result getMFAConfiguration(DataControllerRequest requestInstance) {
        Result result = new Result();
        try {

            int sacCodeLengthMinLength = DEFAULT_SAC_CODE_LENGTH_MIN_LENGTH;
            int sacCodeLengthMaxLength = DEFAULT_SAC_CODE_LENGTH_MAX_LENGTH;
            int sacCodeExpiresAfterMinLength = DEFAULT_SAC_CODE_EXPIRES_AFTER_MIN_LENGTH;
            int sacCodeExpiresAfterMaxLength = DEFAULT_SAC_CODE_EXPIRES_AFTER_MAX_LENGTH;
            int sacMaxResendRequestsAllowedMinLength = DEFAULT_SAC_MAX_RESEND_REQUESTS_ALLOWED_MIN_LENGTH;
            int sacMaxResendRequestsAllowedMaxLength = DEFAULT_SAC_MAX_RESEND_REQUESTS_ALLOWED_MAX_LENGTH;
            int sacMaxFailedAttemptsAllowedMinLength = DEFAULT_SAC_MAX_FAILED_ATTEMPTS_ALLOWED_MIN_LENGTH;
            int sacMaxFailedAttemptsAllowedMaxLength = DEFAULT_SAC_MAX_FAILED_ATTEMPTS_ALLOWED_MAX_LENGTH;
            int sqNumberOfQuestionAskedMinLength = DEFAULT_SQ_NUMBER_OF_QUESTION_ASKED_MIN_LENGTH;
            int sqNumberOfQuestionAskedMaxLength = DEFAULT_SQ_NUMBER_OF_QUESTION_ASKED_MAX_LENGTH;
            int sqMaxFailedAttemptsAllowedMinLength = DEFAULT_SQ_MAX_FAILED_ATTEMPTS_ALLOWED_MIN_LENGTH;
            int sqMaxFailedAttemptsAllowedMaxLength = DEFAULT_SQ_MAX_FAILED_ATTEMPTS_ALLOWED_MAX_LENGTH;

            // Fetch mfaTypeIds from mfatype table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.SELECT, "id, Name");

            String readMFATypeResponse = Executor.invokeService(ServiceURLEnum.MFATYPE_READ, inputMap, null,
                    requestInstance);

            JSONObject readMFATypeResponseJSON = CommonUtilities.getStringAsJSONObject(readMFATypeResponse);
            if (readMFATypeResponseJSON != null && readMFATypeResponseJSON.has(FabricConstants.OPSTATUS)
                    && readMFATypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readMFATypeResponseJSON.has("mfatype")) {
                JSONArray readMFATypeJSONArray = readMFATypeResponseJSON.optJSONArray("mfatype");
                Dataset mfaTypeDataset = new Dataset();
                mfaTypeDataset.setId("mfaTypes");
                if ((readMFATypeJSONArray != null) && (readMFATypeJSONArray.length() > 0)) {
                    for (int indexVar = 0; indexVar < readMFATypeJSONArray.length(); indexVar++) {
                        JSONObject mfaTypeJSONObject = readMFATypeJSONArray.getJSONObject(indexVar);
                        Record currRecord = new Record();
                        String mfaTypeId = mfaTypeJSONObject.getString("id");
                        Param mfaTypeId_Param = new Param("mfaTypeId", mfaTypeId, FabricConstants.STRING);
                        currRecord.addParam(mfaTypeId_Param);
                        String mfaTypeName = mfaTypeJSONObject.getString("Name");
                        Param mfaTypeName_Param = new Param("mfaTypeName", mfaTypeName, FabricConstants.STRING);
                        currRecord.addParam(mfaTypeName_Param);

                        inputMap.clear();
                        inputMap.put(ODataQueryConstants.FILTER,
                                "MFA_id eq '" + mfaTypeJSONObject.getString("id") + "'");
                        inputMap.put(ODataQueryConstants.SELECT, "MFAKey_id, value");

                        String readMFAConfigResponse = Executor.invokeService(ServiceURLEnum.MFACONFIGURATIONS_READ,
                                inputMap, null, requestInstance);

                        Record mfaConfigurationsRecord = new Record();
                        mfaConfigurationsRecord.setId("mfaConfigurations");

                        JSONObject readMFAConfigResponseJSON = CommonUtilities
                                .getStringAsJSONObject(readMFAConfigResponse);
                        if (readMFAConfigResponseJSON != null && readMFAConfigResponseJSON.has(FabricConstants.OPSTATUS)
                                && readMFAConfigResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                                && readMFAConfigResponseJSON.has("mfaconfigurations")) {
                            JSONArray readMFAConfigJSONArray = readMFAConfigResponseJSON
                                    .optJSONArray("mfaconfigurations");
                            if ((readMFAConfigJSONArray != null) && (readMFAConfigJSONArray.length() > 0)) {
                                for (int indxVar = 0; indxVar < readMFAConfigJSONArray.length(); indxVar++) {
                                    Record mfaKeyRecord = new Record();
                                    JSONObject mfaConfigJSONObject = readMFAConfigJSONArray.getJSONObject(indxVar);
                                    String mfaKeyId = mfaConfigJSONObject.getString("MFAKey_id");
                                    mfaKeyRecord.setId(mfaKeyId);
                                    Param mfaKeyId_Param = new Param("mfaKey", mfaKeyId, FabricConstants.STRING);
                                    mfaKeyRecord.addParam(mfaKeyId_Param);
                                    String mfaKeyValue = mfaConfigJSONObject.getString("value");
                                    Param mfaKeyValue_Param = new Param("mfaValue", mfaKeyValue,
                                            FabricConstants.STRING);
                                    mfaKeyRecord.addParam(mfaKeyValue_Param);

                                    if (mfaKeyId.equals("SAC_CODE_LENGTH")) {
                                        String mfaKeyMinLength = Integer.toString(sacCodeLengthMinLength);
                                        Param mfaKeyMinLength_Param = new Param("minLength", mfaKeyMinLength,
                                                FabricConstants.STRING);
                                        mfaKeyRecord.addParam(mfaKeyMinLength_Param);
                                        String mfaKeyMaxLength = Integer.toString(sacCodeLengthMaxLength);
                                        Param mfaKeyMaxLength_Param = new Param("maxLength", mfaKeyMaxLength,
                                                FabricConstants.STRING);
                                        mfaKeyRecord.addParam(mfaKeyMaxLength_Param);
                                    } else if (mfaKeyId.equals("SAC_CODE_EXPIRES_AFTER")) {
                                        String mfaKeyMinLength = Integer.toString(sacCodeExpiresAfterMinLength);
                                        Param mfaKeyMinLength_Param = new Param("minLength", mfaKeyMinLength,
                                                FabricConstants.STRING);
                                        mfaKeyRecord.addParam(mfaKeyMinLength_Param);
                                        String mfaKeyMaxLength = Integer.toString(sacCodeExpiresAfterMaxLength);
                                        Param mfaKeyMaxLength_Param = new Param("maxLength", mfaKeyMaxLength,
                                                FabricConstants.STRING);
                                        mfaKeyRecord.addParam(mfaKeyMaxLength_Param);
                                    } else if (mfaKeyId.equals("SAC_MAX_RESEND_REQUESTS_ALLOWED")) {
                                        String mfaKeyMinLength = Integer.toString(sacMaxResendRequestsAllowedMinLength);
                                        Param mfaKeyMinLength_Param = new Param("minLength", mfaKeyMinLength,
                                                FabricConstants.STRING);
                                        mfaKeyRecord.addParam(mfaKeyMinLength_Param);
                                        String mfaKeyMaxLength = Integer.toString(sacMaxResendRequestsAllowedMaxLength);
                                        Param mfaKeyMaxLength_Param = new Param("maxLength", mfaKeyMaxLength,
                                                FabricConstants.STRING);
                                        mfaKeyRecord.addParam(mfaKeyMaxLength_Param);
                                    } else if (mfaKeyId.equals("MAX_FAILED_ATTEMPTS_ALLOWED")
                                            && mfaTypeId.equals("SECURE_ACCESS_CODE")) {
                                        String mfaKeyMinLength = Integer.toString(sacMaxFailedAttemptsAllowedMinLength);
                                        Param mfaKeyMinLength_Param = new Param("minLength", mfaKeyMinLength,
                                                FabricConstants.STRING);
                                        mfaKeyRecord.addParam(mfaKeyMinLength_Param);
                                        String mfaKeyMaxLength = Integer.toString(sacMaxFailedAttemptsAllowedMaxLength);
                                        Param mfaKeyMaxLength_Param = new Param("maxLength", mfaKeyMaxLength,
                                                FabricConstants.STRING);
                                        mfaKeyRecord.addParam(mfaKeyMaxLength_Param);
                                    } else if (mfaKeyId.equals("SQ_NUMBER_OF_QUESTION_ASKED")) {
                                        String mfaKeyMinLength = Integer.toString(sqNumberOfQuestionAskedMinLength);
                                        Param mfaKeyMinLength_Param = new Param("minLength", mfaKeyMinLength,
                                                FabricConstants.STRING);
                                        mfaKeyRecord.addParam(mfaKeyMinLength_Param);
                                        String mfaKeyMaxLength = Integer.toString(sqNumberOfQuestionAskedMaxLength);
                                        Param mfaKeyMaxLength_Param = new Param("maxLength", mfaKeyMaxLength,
                                                FabricConstants.STRING);
                                        mfaKeyRecord.addParam(mfaKeyMaxLength_Param);
                                    } else if (mfaKeyId.equals("MAX_FAILED_ATTEMPTS_ALLOWED")
                                            && mfaTypeId.equals("SECURITY_QUESTIONS")) {
                                        String mfaKeyMinLength = Integer.toString(sqMaxFailedAttemptsAllowedMinLength);
                                        Param mfaKeyMinLength_Param = new Param("minLength", mfaKeyMinLength,
                                                FabricConstants.STRING);
                                        mfaKeyRecord.addParam(mfaKeyMinLength_Param);
                                        String mfaKeyMaxLength = Integer.toString(sqMaxFailedAttemptsAllowedMaxLength);
                                        Param mfaKeyMaxLength_Param = new Param("maxLength", mfaKeyMaxLength,
                                                FabricConstants.STRING);
                                        mfaKeyRecord.addParam(mfaKeyMaxLength_Param);
                                    }
                                    mfaConfigurationsRecord.addRecord(mfaKeyRecord);
                                }
                            }
                        } else {
                            result.addParam(new Param("message", readMFAConfigResponse, FabricConstants.STRING));
                            LOG.error("Failed to Fetch MFAConfig Response: " + readMFAConfigResponse);
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            ErrorCodeEnum.ERR_21304.setErrorCode(result);
                            return result;
                        }

                        currRecord.addRecord(mfaConfigurationsRecord);
                        mfaTypeDataset.addRecord(currRecord);
                    }
                    result.addDataset(mfaTypeDataset);
                    return result;
                }
            } else {
                result.addParam(new Param("message", readMFATypeResponse, FabricConstants.STRING));
                LOG.error("Failed to Fetch MFAConfig Response: " + readMFATypeResponse);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_21305.setErrorCode(result);
                return result;
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in Fetching mfaConfigurations. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_21305.setErrorCode(result);
        }
        return result;
    }
}
