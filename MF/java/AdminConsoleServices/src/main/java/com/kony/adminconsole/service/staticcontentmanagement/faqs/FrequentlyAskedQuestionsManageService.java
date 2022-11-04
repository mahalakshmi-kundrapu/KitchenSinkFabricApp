package com.kony.adminconsole.service.staticcontentmanagement.faqs;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
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
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to manage the FAQs(Create,Update,Delete)
 *
 * @author Aditya Mankal
 * 
 */
public class FrequentlyAskedQuestionsManageService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(FrequentlyAskedQuestionsManageService.class);

    private static final int QUESTION_MAX_LENGTH = 300;

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Map<String, String> postParametersMap = new HashMap<String, String>();
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            String userID = userDetailsBeanInstance.getUserId();
            String masterStatusID = requestInstance.getParameter("Status_id");
            String masterDeleteFlag = requestInstance.getParameter("RemoveFlag");
            String targetFAQs = requestInstance.getParameter("listOfFAQs");

            JSONArray targetFAQsJSONArray = CommonUtilities.getStringAsJSONArray(targetFAQs);

            String manageFAQResponse;
            JSONObject manageFAQResponseJSON;

            if (StringUtils.equalsIgnoreCase(masterDeleteFlag, "TRUE")
                    || StringUtils.equalsIgnoreCase(masterDeleteFlag, "1")) {
                return deleteTargetFAQs(userID, targetFAQsJSONArray, authToken, requestInstance);
            }

            if (StringUtils.isNotBlank(masterStatusID)) {
                return changeStatusOfTargetFAQs(masterStatusID, userID, targetFAQsJSONArray, authToken,
                        requestInstance);
            }

            Record successFAQManageOpRecord = new Record();
            successFAQManageOpRecord.setId("successFAQManageOpRecord");

            Record failureFAQManageOpRecord = new Record();
            failureFAQManageOpRecord.setId("failureFAQManageOpRecord");

            if (targetFAQsJSONArray != null) {
                for (int indexVar = 0; indexVar < targetFAQsJSONArray.length(); indexVar++) {
                    Param currFAQOpParam = new Param();
                    currFAQOpParam.setType(FabricConstants.STRING);

                    boolean isCreateRequest = false;
                    String currFAQId = "";

                    JSONObject currFAQDataJSON = targetFAQsJSONArray.getJSONObject(indexVar);
                    if (!currFAQDataJSON.has("id")
                            || StringUtils.equalsIgnoreCase(currFAQDataJSON.getString("id"), "NULL")) {
                        isCreateRequest = true;
                        currFAQId = CommonUtilities.getNewId().toString();
                    } else {
                        currFAQId = currFAQDataJSON.getString("id");
                    }
                    postParametersMap.put("id", currFAQId);
                    if (isCreateRequest) {
                        currFAQOpParam.setName("CreateFAQ_" + currFAQId);
                    } else {
                        currFAQOpParam.setName("UpdateFAQ_" + currFAQId);
                    }

                    if (currFAQDataJSON.has("RemoveFlag")) {
                        String deleteFlagValue = currFAQDataJSON.getString("RemoveFlag");
                        if (StringUtils.equalsIgnoreCase(deleteFlagValue, "TRUE")
                                || StringUtils.equalsIgnoreCase(deleteFlagValue, "1")) {
                            manageFAQResponse = Executor.invokeService(ServiceURLEnum.FAQS_DELETE, postParametersMap,
                                    null, requestInstance);
                            currFAQOpParam.setName("DeleteFAQ_" + currFAQId);
                            manageFAQResponseJSON = CommonUtilities.getStringAsJSONObject(manageFAQResponse);
                            if (manageFAQResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                                successFAQManageOpRecord.addParam(
                                        new Param("Delete_" + currFAQId, manageFAQResponse, FabricConstants.STRING));
                                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.FAQS, EventEnum.DELETE,
                                        ActivityStatusEnum.FAILED, "FAQ delete failed. FAQID: " + currFAQId);
                            } else {
                                failureFAQManageOpRecord.addParam(
                                        new Param("Delete_" + currFAQId, manageFAQResponse, FabricConstants.STRING));
                                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.FAQS, EventEnum.DELETE,
                                        ActivityStatusEnum.SUCCESSFUL, "FAQ delete successful. FAQID: " + currFAQId);
                            }
                            continue;
                        }
                    }

                    if (currFAQDataJSON.has("Channel_id"))
                        postParametersMap.put("Channel_id", currFAQDataJSON.getString("Channel_id"));
                    if (currFAQDataJSON.has("CategoryId"))
                        postParametersMap.put("FaqCategory_Id", currFAQDataJSON.getString("CategoryId"));
                    if (currFAQDataJSON.has("QuestionCode"))
                        postParametersMap.put("QuestionCode", currFAQDataJSON.getString("QuestionCode"));
                    if (currFAQDataJSON.has("Question")) {
                        String question = currFAQDataJSON.getString("Question");
                        if (question.length() > QUESTION_MAX_LENGTH) {
                            currFAQOpParam
                                    .setValue("Question can have a maximum of " + QUESTION_MAX_LENGTH + "characters");
                            failureFAQManageOpRecord.addParam(currFAQOpParam);
                            continue;
                        }
                        postParametersMap.put("Question", question);
                    }
                    if (currFAQDataJSON.has("Answer"))
                        postParametersMap.put("Answer", currFAQDataJSON.getString("Answer"));
                    if (currFAQDataJSON.has("Status_id"))
                        postParametersMap.put("Status_id", currFAQDataJSON.getString("Status_id"));

                    if (isCreateRequest) {
                        postParametersMap.put("createdby", userID);
                        postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
                        manageFAQResponse = Executor.invokeService(ServiceURLEnum.FAQS_CREATE, postParametersMap, null,
                                requestInstance);
                    } else {
                        postParametersMap.put("modifiedby", userID);
                        postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
                        manageFAQResponse = Executor.invokeService(ServiceURLEnum.FAQS_UPDATE, postParametersMap, null,
                                requestInstance);
                    }
                    currFAQOpParam.setValue(manageFAQResponse);
                    manageFAQResponseJSON = CommonUtilities.getStringAsJSONObject(manageFAQResponse);
                    if (manageFAQResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                        failureFAQManageOpRecord.addParam(currFAQOpParam);
                        if (isCreateRequest)
                            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.FAQS, EventEnum.CREATE,
                                    ActivityStatusEnum.FAILED, "FAQ create failed. FAQID: " + currFAQId);
                        else
                            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.FAQS, EventEnum.UPDATE,
                                    ActivityStatusEnum.FAILED, "FAQ update failed. FAQID: " + currFAQId);

                    } else {
                        successFAQManageOpRecord.addParam(currFAQOpParam);
                        if (isCreateRequest)
                            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.FAQS, EventEnum.CREATE,
                                    ActivityStatusEnum.SUCCESSFUL, "FAQ create successful. FAQID: " + currFAQId);
                        else
                            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.FAQS, EventEnum.UPDATE,
                                    ActivityStatusEnum.SUCCESSFUL, "FAQ update successful. FAQID: " + currFAQId);
                    }
                }
            }
            Result processedResult = new Result();
            if (failureFAQManageOpRecord.getAllParams().size() > 0) {
                ErrorCodeEnum.ERR_20162.setErrorCode(processedResult);
                processedResult.addRecord(failureFAQManageOpRecord);
            }
            processedResult.addRecord(successFAQManageOpRecord);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    private Result deleteTargetFAQs(String userID, JSONArray targetFAQsJSONArray, String authToken,
            DataControllerRequest requestInstance) {

        Result processedResult = new Result();
        String deleteFAQResponse;
        JSONObject deleteFAQResponseJSON;

        Record successfulDeleteRecord = new Record();
        successfulDeleteRecord.setId("SuccessDeleteRecordOps");

        Record failedDeleteRecord = new Record();
        failedDeleteRecord.setId("FailureDeleteRecordOps");

        boolean hasDeleteFAQFailed = false;

        Map<String, String> postParametersMap = new HashMap<String, String>();
        for (int indexVar = 0; indexVar < targetFAQsJSONArray.length(); indexVar++) {
            postParametersMap.clear();
            String currFAQId = targetFAQsJSONArray.getString(indexVar);
            postParametersMap.put("id", currFAQId);
            deleteFAQResponse = Executor.invokeService(ServiceURLEnum.FAQS_DELETE, postParametersMap, null,
                    requestInstance);
            deleteFAQResponseJSON = CommonUtilities.getStringAsJSONObject(deleteFAQResponse);
            if (deleteFAQResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                failedDeleteRecord
                        .addParam(new Param("Delete_" + currFAQId, deleteFAQResponse, FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.FAQS, EventEnum.DELETE,
                        ActivityStatusEnum.FAILED, "FAQ delete failed. FAQID: " + currFAQId);
                hasDeleteFAQFailed = true;
            } else {
                successfulDeleteRecord
                        .addParam(new Param("Delete_" + currFAQId, deleteFAQResponse, FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.FAQS, EventEnum.DELETE,
                        ActivityStatusEnum.SUCCESSFUL, "FAQ delete successful. FAQID: " + currFAQId);
            }
        }
        if (hasDeleteFAQFailed) {
            ErrorCodeEnum.ERR_20163.setErrorCode(processedResult);
            processedResult.addRecord(failedDeleteRecord);
        }
        processedResult.addRecord(successfulDeleteRecord);
        return processedResult;

    }

    private Result changeStatusOfTargetFAQs(String masterStatusID, String userID, JSONArray targetFAQsJSONArray,
            String authToken, DataControllerRequest requestInstance) {

        Result processedResult = new Result();
        String changeFAQStatusResponse;
        JSONObject changeFAQStatusResponseJSON;

        Record successfulEditRecord = new Record();
        successfulEditRecord.setId("SuccessEditRecordOps");

        Record failedEditRecord = new Record();
        failedEditRecord.setId("FailureRecordOps");

        boolean hasEditFAQFailed = false;

        Map<String, String> postParametersMap = new HashMap<String, String>();
        for (int indexVar = 0; indexVar < targetFAQsJSONArray.length(); indexVar++) {
            postParametersMap.clear();
            String currFAQId = targetFAQsJSONArray.getString(indexVar);
            postParametersMap.put("id", currFAQId);
            postParametersMap.put("Status_id", masterStatusID);
            postParametersMap.put("modifiedby", userID);
            postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            changeFAQStatusResponse = Executor.invokeService(ServiceURLEnum.FAQS_UPDATE, postParametersMap, null,
                    requestInstance);
            changeFAQStatusResponseJSON = CommonUtilities.getStringAsJSONObject(changeFAQStatusResponse);
            if (changeFAQStatusResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                failedEditRecord
                        .addParam(new Param("EditFAQ_" + currFAQId, changeFAQStatusResponse, FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.FAQS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "FAQ update failed. FAQID: " + currFAQId);
                hasEditFAQFailed = true;
            } else {
                successfulEditRecord
                        .addParam(new Param("EditFAQ_" + currFAQId, changeFAQStatusResponse, FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.FAQS, EventEnum.UPDATE,
                        ActivityStatusEnum.SUCCESSFUL, "FAQ update successful. FAQID: " + currFAQId);
            }
        }
        if (hasEditFAQFailed) {
            ErrorCodeEnum.ERR_20162.setErrorCode(processedResult);
            processedResult.addRecord(failedEditRecord);
        }
        processedResult.addRecord(successfulEditRecord);
        return processedResult;

    }

}