package com.kony.adminconsole.service.termandcondition;

/**
 * Service to Manage Requests related to TermAndCondition
 * 
 * @author Chandan Gupta - KH2516
 *
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import com.kony.adminconsole.utilities.StatusEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class TermAndConditionManageService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(TermAndConditionManageService.class);

    private static final String EDIT_TERMS_AND_CONDITIONS_METHOD_NAME = "editTermsAndConditions";
    private static final String GET_TERMS_AND_CONDITIONS_METHOD_NAME = "getTermsAndConditions";
    private static final String GET_ALL_TERMS_AND_CONDITIONS_METHOD_NAME = "getAllTermsAndConditions";
    private static final String CREATE_TERMS_AND_CONDITIONS_VERSION_METHOD_NAME = "createTermsAndConditionsVersion";
    private static final String DELETE_TERMS_AND_CONDITIONS_VERSION_METHOD_NAME = "deleteTermsAndConditionsVersion";

    private static final String DEFAULT_TERM_AND_CONDITION_CODE = "DBX_Default_TnC";
    private static final String DEFAULT_LANGUAGE_CODE = "en-US";
    private static final String DEFAULT_DRAFT_VERSION = "N/A";
    private static final String MIN_VERSION = "1.0";

    private static final String TANDC_DRAFT_STATUS_ID = StatusEnum.SID_TANDC_DRAFT.name();
    private static final String TANDC_ARCHIVED_STATUS_ID = StatusEnum.SID_TANDC_ARCHIVED.name();
    private static final String TANDC_ACTIVE_STATUS_ID = StatusEnum.SID_TANDC_ACTIVE.name();

    private static final int TERM_AND_CONDITION_TITLE_MIN_CHARACTERS = 3;
    private static final int TERM_AND_CONDITION_TITLE_MAX_CHARACTERS = 50;
    private static final int TERM_AND_CONDITION_DESCRIPTION_MAX_CHARACTERS = 250;
    private static final int TERM_AND_CONDITION_VERSION_DESCRIPTION_MAX_CHARACTERS = 250;

    private static final String APP_PREFERENCES_PARAM = "appPreferences";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            if (StringUtils.equalsIgnoreCase(methodID, EDIT_TERMS_AND_CONDITIONS_METHOD_NAME)) {
                return editTermsAndConditions(requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, GET_TERMS_AND_CONDITIONS_METHOD_NAME)) {
                return getTermsAndConditions(requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, GET_ALL_TERMS_AND_CONDITIONS_METHOD_NAME)) {
                return getAllTermsAndConditions(requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, CREATE_TERMS_AND_CONDITIONS_VERSION_METHOD_NAME)) {
                return createTermsAndConditionsVersion(requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, DELETE_TERMS_AND_CONDITIONS_VERSION_METHOD_NAME)) {
                return deleteTermsAndConditionsVersion(requestInstance);
            }
            return null;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    public Result deleteTermsAndConditionsVersion(DataControllerRequest requestInstance) {

        Result result = new Result();

        try {

            String termAndConditionCode = requestInstance.getParameter("termsAndConditionsCode");
            String languageCode = requestInstance.getParameter("languageCode");

            String auditSuccessDescription = "Terms and conditions draft version delete successful. Code: "
                    + termAndConditionCode + "/" + "Language Code: " + languageCode + "/" + "Version: N/A";

            if (StringUtils.isBlank(termAndConditionCode)) {
                ErrorCodeEnum.ERR_20266.setErrorCode(result);
                LOG.error("Term and condition code is a mandatory input");
                LOG.error("Failed to delete Terms and Conditions draft version");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.DELETE,
                        ActivityStatusEnum.FAILED, "Failed to delete Terms and Conditions draft version");
                return result;
            }

            if (StringUtils.isBlank(languageCode)) {
                languageCode = DEFAULT_LANGUAGE_CODE;
            }

            // Fetching T&C id from termandcondition table
            String termAndConditionId = null;
            termAndConditionId = getTermAndConditionId(requestInstance, termAndConditionCode, result);
            if (termAndConditionId == null) {
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20264.setErrorCode(result);
                LOG.error("Failed to get Terms and Conditions");
                LOG.error("Failed to delete Terms and Conditions draft version");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.DELETE,
                        ActivityStatusEnum.FAILED, "Failed to delete Terms and Conditions draft version");
                return result;
            }

            String termAndConditionTextId = null;
            termAndConditionTextId = getTermAndConditionTextId(requestInstance, termAndConditionId, languageCode,
                    TANDC_DRAFT_STATUS_ID, result);
            if (termAndConditionTextId == null) {
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20265.setErrorCode(result);
                LOG.error("Failed to get Terms and Conditions text");
                LOG.error("Failed to delete Terms and Conditions draft version");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.DELETE,
                        ActivityStatusEnum.FAILED, "Failed to delete Terms and Conditions draft version");
                return result;
            }

            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put("id", termAndConditionTextId);

            String deleteTermAndConditionTextResponse = Executor
                    .invokeService(ServiceURLEnum.TERMANDCONDITIONTEXT_DELETE, inputMap, null, requestInstance);

            JSONObject deleteTermAndConditionTextResponseJSON = CommonUtilities
                    .getStringAsJSONObject(deleteTermAndConditionTextResponse);
            if (deleteTermAndConditionTextResponseJSON != null
                    && deleteTermAndConditionTextResponseJSON.has(FabricConstants.OPSTATUS)
                    && deleteTermAndConditionTextResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                LOG.error("Successfully deleted Terms and Conditions draft version");
                result.addParam(new Param("status", "Success", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.DELETE,
                        ActivityStatusEnum.SUCCESSFUL, auditSuccessDescription);
            } else {
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20280.setErrorCode(result);
                LOG.error("Failed to delete Terms and Conditions draft version");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.DELETE,
                        ActivityStatusEnum.FAILED, "Failed to delete Terms and Conditions draft version");
            }

        } catch (Exception e) {
            ErrorCodeEnum.ERR_20280.setErrorCode(result);
            LOG.error("Failed to delete Terms and Conditions draft version", e);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "Failed to delete Terms and Conditions draft version");
        }
        return result;
    }

    public Result getTermsAndConditions(DataControllerRequest requestInstance) {

        Result result = new Result();

        try {

            String termAndConditionCode = null, languageCode = null, termAndConditonId = null;

            termAndConditionCode = requestInstance.getParameter("termsAndConditionsCode");
            languageCode = requestInstance.getParameter("languageCode");

            // If termAndConditionCode is not passed we'll send default T&C
            if (StringUtils.isBlank(termAndConditionCode)) {
                termAndConditionCode = DEFAULT_TERM_AND_CONDITION_CODE;
            }

            // If languageCode is not passed we'll set default languageCode
            if (StringUtils.isBlank(languageCode)) {
                languageCode = DEFAULT_LANGUAGE_CODE;
            }

            // Fetching T&C id from termandcondition table
            Map<String, String> inputBodyMap = new HashMap<String, String>();
            inputBodyMap.put(ODataQueryConstants.FILTER, "Code eq '" + termAndConditionCode + "'");
            inputBodyMap.put(ODataQueryConstants.SELECT, "id");
            String readTermAndConditionResponse = Executor.invokeService(ServiceURLEnum.TERMANDCONDITION_READ,
                    inputBodyMap, null, requestInstance);
            JSONObject readTermAndConditionResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readTermAndConditionResponse);
            if (readTermAndConditionResponseJSON != null
                    && readTermAndConditionResponseJSON.has(FabricConstants.OPSTATUS)
                    && readTermAndConditionResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readTermAndConditionResponseJSON.has("termandcondition")) {
                JSONArray readTermAndConditionJSONArray = readTermAndConditionResponseJSON
                        .optJSONArray("termandcondition");
                if (readTermAndConditionJSONArray == null || readTermAndConditionJSONArray.length() < 1) {
                    LOG.error("Failed to get Terms and Conditions");
                    ErrorCodeEnum.ERR_20264.setErrorCode(result);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                } else {
                    JSONObject currTermAndConditionRecord = readTermAndConditionJSONArray.getJSONObject(0);
                    termAndConditonId = currTermAndConditionRecord.getString("id");
                }
            } else {
                LOG.error("Failed to get Terms and Conditions");
                ErrorCodeEnum.ERR_20264.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Fetch T&C Content from termandconditiontext table
            inputBodyMap.clear();
            inputBodyMap.put(ODataQueryConstants.FILTER, "TermAndConditionId eq '" + termAndConditonId
                    + "' and LanguageCode eq '" + languageCode + "' and Status_id eq '" + TANDC_ACTIVE_STATUS_ID + "'");
            inputBodyMap.put(ODataQueryConstants.SELECT, "Content, ContentType_id, Version_Id");
            String readTermAndConditionTextResponse = Executor.invokeService(ServiceURLEnum.TERMANDCONDITIONTEXT_READ,
                    inputBodyMap, null, requestInstance);
            JSONObject readTermAndConditionTextResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readTermAndConditionTextResponse);
            if (readTermAndConditionTextResponseJSON != null
                    && readTermAndConditionTextResponseJSON.has(FabricConstants.OPSTATUS)
                    && readTermAndConditionTextResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readTermAndConditionTextResponseJSON.has("termandconditiontext")) {
                JSONArray readTermAndConditionTextJSONArray = readTermAndConditionTextResponseJSON
                        .optJSONArray("termandconditiontext");
                if (readTermAndConditionTextJSONArray == null || readTermAndConditionTextJSONArray.length() < 1) {
                    // If content is null returning content for default language
                    inputBodyMap.clear();
                    inputBodyMap.put(ODataQueryConstants.FILTER,
                            "TermAndConditionId eq '" + termAndConditonId + "' and LanguageCode eq '"
                                    + DEFAULT_LANGUAGE_CODE + "' and Status_id eq '" + TANDC_ACTIVE_STATUS_ID + "'");
                    inputBodyMap.put(ODataQueryConstants.SELECT, "Content, ContentType_id, Version_Id");
                    String _readTermAndConditionTextResponse = Executor.invokeService(
                            ServiceURLEnum.TERMANDCONDITIONTEXT_READ, inputBodyMap, null, requestInstance);
                    JSONObject _readTermAndConditionTextResponseJSON = CommonUtilities
                            .getStringAsJSONObject(_readTermAndConditionTextResponse);
                    if (_readTermAndConditionTextResponseJSON != null
                            && _readTermAndConditionTextResponseJSON.has(FabricConstants.OPSTATUS)
                            && _readTermAndConditionTextResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                            && _readTermAndConditionTextResponseJSON.has("termandconditiontext")) {
                        JSONArray _readTermAndConditionTextJSONArray = _readTermAndConditionTextResponseJSON
                                .optJSONArray("termandconditiontext");
                        if (_readTermAndConditionTextJSONArray == null
                                || _readTermAndConditionTextJSONArray.length() < 1) {
                            LOG.error("Failed to get Terms and Conditions text");
                            ErrorCodeEnum.ERR_20265.setErrorCode(result);
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            return result;
                        } else {
                            JSONObject _currTermAndConditionTextRecord = _readTermAndConditionTextJSONArray
                                    .getJSONObject(0);
                            Param _currTermAndConditionContentParam = new Param("termsAndConditionsContent",
                                    _currTermAndConditionTextRecord.optString("Content"), FabricConstants.STRING);
                            result.addParam(_currTermAndConditionContentParam);
                            String termAndConditionContentTypeId = _currTermAndConditionTextRecord
                                    .getString("ContentType_id");
                            Param contentTypeId_Param = new Param("contentTypeId", termAndConditionContentTypeId,
                                    FabricConstants.STRING);
                            result.addParam(contentTypeId_Param);
                            String termAndConditionVersionId = _currTermAndConditionTextRecord.getString("Version_Id");
                            Param versionId_Param = new Param("versionId", termAndConditionVersionId,
                                    FabricConstants.STRING);
                            result.addParam(versionId_Param);
                            result.addParam(new Param("status", "Success", FabricConstants.STRING));
                            return result;
                        }
                    } else {
                        LOG.error("Failed to get Terms and Conditions text");
                        ErrorCodeEnum.ERR_20265.setErrorCode(result);
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        return result;
                    }
                } else {
                    JSONObject currTermAndConditionTextRecord = readTermAndConditionTextJSONArray.getJSONObject(0);
                    Param currTermAndConditionContentParam = new Param("termsAndConditionsContent",
                            currTermAndConditionTextRecord.optString("Content"), FabricConstants.STRING);
                    result.addParam(currTermAndConditionContentParam);
                    String termAndConditionContentTypeId = currTermAndConditionTextRecord.getString("ContentType_id");
                    Param contentTypeId_Param = new Param("contentTypeId", termAndConditionContentTypeId,
                            FabricConstants.STRING);
                    result.addParam(contentTypeId_Param);
                    String termAndConditionVersionId = currTermAndConditionTextRecord.getString("Version_Id");
                    Param versionId_Param = new Param("versionId", termAndConditionVersionId, FabricConstants.STRING);
                    result.addParam(versionId_Param);
                    result.addParam(new Param("status", "Success", FabricConstants.STRING));
                    return result;
                }
            } else {
                LOG.error("Failed to get Terms and Conditions text");
                ErrorCodeEnum.ERR_20265.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }
        } catch (Exception e) {
            LOG.error("Failed to get Terms and Conditions text. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20265.setErrorCode(result);
        }
        return result;
    }

    public Result editTermsAndConditions(DataControllerRequest requestInstance) {

        Result result = new Result();

        try {

            String loggedInUserId = null;
            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            if (loggedInUserDetails != null) {
                loggedInUserId = loggedInUserDetails.getUserId();
            }

            String termAndConditionCode = requestInstance.getParameter("termsAndConditionsCode");
            String languageCode = requestInstance.getParameter("languageCode");
            String contentType = requestInstance.getParameter("contentType");
            String termAndConditionTitle = requestInstance.getParameter("termsAndConditionsTitle");
            String termAndConditionDescription = requestInstance.getParameter("termsAndConditionsDescription");
            String termAndConditionContent = requestInstance.getParameter("termsAndConditionsContent");
            String auditSuccessDescription = "Terms and conditions update successful. Code: " + termAndConditionCode
                    + "/" + "Language Code: " + languageCode + "/" + "Content Type: " + contentType + "/" + "Title: "
                    + termAndConditionTitle + "/" + "Description: " + termAndConditionDescription;

            if (StringUtils.isBlank(termAndConditionCode)) {
                ErrorCodeEnum.ERR_20266.setErrorCode(result);
                LOG.error("Term and condition code is a mandatory input");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Terms and Conditions update failed. Code: " + termAndConditionCode + "/" + "Language Code: "
                                + languageCode + "/" + "Title: " + termAndConditionTitle + "/" + "Description: "
                                + termAndConditionDescription);
                return result;
            }

            if (StringUtils.isBlank(languageCode)) {
                languageCode = DEFAULT_LANGUAGE_CODE;
            }

            // Making contentType as non mandatory for backward compatibility
            /*
             * if (StringUtils.isBlank(contentType)) { ErrorCodeEnum.ERR_20272.setErrorCode(result);
             * LOG.error("Terms and conditions content type is a mandatory input");
             * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
             * ActivityStatusEnum.FAILED, "Terms and Conditions update failed."); return result; }
             */

            if (StringUtils.isBlank(termAndConditionTitle)) {
                ErrorCodeEnum.ERR_20267.setErrorCode(result);
                LOG.error("Terms and conditions title is a mandatory input");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Terms and Conditions update failed. Code: " + termAndConditionCode + "/" + "Language Code: "
                                + languageCode + "/" + "Title: " + termAndConditionTitle + "/" + "Description: "
                                + termAndConditionDescription);
                return result;
            }

            if (StringUtils.length(termAndConditionTitle) < TERM_AND_CONDITION_TITLE_MIN_CHARACTERS
                    || StringUtils.length(termAndConditionTitle) > TERM_AND_CONDITION_TITLE_MAX_CHARACTERS) {
                ErrorCodeEnum.ERR_20269.setErrorCode(result);
                LOG.error("Terms and conditions title length is not in range");
                result.addParam(new Param("message",
                        "Terms and conditions title should have a minimum of " + TERM_AND_CONDITION_TITLE_MIN_CHARACTERS
                                + " and a maximum of " + TERM_AND_CONDITION_TITLE_MAX_CHARACTERS + " characters",
                        FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Terms and Conditions update failed. Code: " + termAndConditionCode + "/" + "Language Code: "
                                + languageCode + "/" + "Title: " + termAndConditionTitle + "/" + "Description: "
                                + termAndConditionDescription);
                return result;
            }

            if (StringUtils.length(termAndConditionDescription) > TERM_AND_CONDITION_DESCRIPTION_MAX_CHARACTERS) {
                ErrorCodeEnum.ERR_20269.setErrorCode(result);
                LOG.error("Terms and conditions description length is not in range");
                result.addParam(new Param("message",
                        "Terms and conditions description should have a maximum of "
                                + TERM_AND_CONDITION_DESCRIPTION_MAX_CHARACTERS + " characters",
                        FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Terms and Conditions update failed. Code: " + termAndConditionCode + "/" + "Language Code: "
                                + languageCode + "/" + "Title: " + termAndConditionTitle + "/" + "Description: "
                                + termAndConditionDescription);
                return result;
            }

            // Making termAndConditionContent as non mandatory for backward compatibility
            /*
             * if (StringUtils.isBlank(termAndConditionContent)) { ErrorCodeEnum.ERR_20270.setErrorCode(result);
             * LOG.error("Terms and conditions content is a mandatory input");
             * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
             * ActivityStatusEnum.FAILED, "Terms and Conditions update failed."); return result; }
             */

            // Fetching T&C id from termandcondition table
            String termAndConditionId = null;
            Map<String, String> inputBodyMap = new HashMap<String, String>();
            inputBodyMap.put(ODataQueryConstants.FILTER, "Code eq '" + termAndConditionCode + "'");
            inputBodyMap.put(ODataQueryConstants.SELECT, "id");
            String readTermAndConditionResponse = Executor.invokeService(ServiceURLEnum.TERMANDCONDITION_READ,
                    inputBodyMap, null, requestInstance);
            JSONObject readTermAndConditionResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readTermAndConditionResponse);
            if (readTermAndConditionResponseJSON != null
                    && readTermAndConditionResponseJSON.has(FabricConstants.OPSTATUS)
                    && readTermAndConditionResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readTermAndConditionResponseJSON.has("termandcondition")) {
                JSONArray readTermAndConditionJSONArray = readTermAndConditionResponseJSON
                        .optJSONArray("termandcondition");
                if (readTermAndConditionJSONArray == null || readTermAndConditionJSONArray.length() < 1) {
                    LOG.error("Failed to get Term and Condition");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                            ActivityStatusEnum.FAILED,
                            "Terms and Conditions update failed. Code: " + termAndConditionCode + "/"
                                    + "Language Code: " + languageCode + "/" + "Title: " + termAndConditionTitle + "/"
                                    + "Description: " + termAndConditionDescription);
                    ErrorCodeEnum.ERR_20264.setErrorCode(result);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                } else {
                    JSONObject currTermAndConditionRecord = readTermAndConditionJSONArray.getJSONObject(0);
                    termAndConditionId = currTermAndConditionRecord.getString("id");
                }
            } else {
                LOG.error("Failed to get Terms and Conditions");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Terms and Conditions update failed. Code: " + termAndConditionCode + "/" + "Language Code: "
                                + languageCode + "/" + "Title: " + termAndConditionTitle + "/" + "Description: "
                                + termAndConditionDescription);
                ErrorCodeEnum.ERR_20264.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Updating Title, Description, ContentModifiedBy and ContentModifiedOn in
            // termandcondition table
            inputBodyMap.clear();
            inputBodyMap.put("id", termAndConditionId);
            inputBodyMap.put("Title", termAndConditionTitle);
            inputBodyMap.put("Description", termAndConditionDescription);
            inputBodyMap.put("modifiedby", loggedInUserDetails.getUserName());
            inputBodyMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            String editTermAndConditionResponse = Executor.invokeService(ServiceURLEnum.TERMANDCONDITION_UPDATE,
                    inputBodyMap, null, requestInstance);
            JSONObject editTermAndConditionResponseJSON = CommonUtilities
                    .getStringAsJSONObject(editTermAndConditionResponse);
            if (!(editTermAndConditionResponseJSON != null
                    && editTermAndConditionResponseJSON.has(FabricConstants.OPSTATUS)
                    && editTermAndConditionResponseJSON.getInt(FabricConstants.OPSTATUS) == 0)) {
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20269.setErrorCode(result);
                LOG.error("Failed to Edit Terms and Conditions");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Terms and Conditions update failed. Code: " + termAndConditionCode + "/" + "Language Code: "
                                + languageCode + "/" + "Title: " + termAndConditionTitle + "/" + "Description: "
                                + termAndConditionDescription);
                return result;
            }

            // Fetching termAndConditionText id from termandconditiontext table
            if (StringUtils.isNotBlank(contentType) && StringUtils.isNotBlank(termAndConditionContent)) {
                String termAndConditonTextId = null;
                inputBodyMap.clear();
                inputBodyMap.put(ODataQueryConstants.FILTER,
                        "TermAndConditionId eq '" + termAndConditionId + "' and LanguageCode eq '" + languageCode
                                + "' and Status_id eq '" + TANDC_ACTIVE_STATUS_ID + "'");
                inputBodyMap.put(ODataQueryConstants.SELECT, "id");
                String readTermAndConditionTextResponse = Executor
                        .invokeService(ServiceURLEnum.TERMANDCONDITIONTEXT_READ, inputBodyMap, null, requestInstance);
                JSONObject readTermAndConditionTextResponseJSON = CommonUtilities
                        .getStringAsJSONObject(readTermAndConditionTextResponse);
                if (readTermAndConditionTextResponseJSON != null
                        && readTermAndConditionTextResponseJSON.has(FabricConstants.OPSTATUS)
                        && readTermAndConditionTextResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                        && readTermAndConditionTextResponseJSON.has("termandconditiontext")) {
                    JSONArray readTermAndConditionTextJSONArray = readTermAndConditionTextResponseJSON
                            .optJSONArray("termandconditiontext");
                    if (readTermAndConditionTextJSONArray == null || readTermAndConditionTextJSONArray.length() < 1) {
                        LOG.error("Failed to get Terms and Conditions text");
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                                ActivityStatusEnum.FAILED,
                                "Terms and Conditions update failed. Code: " + termAndConditionCode + "/"
                                        + "Language Code: " + languageCode + "/" + "Title: " + termAndConditionTitle
                                        + "/" + "Description: " + termAndConditionDescription);
                        ErrorCodeEnum.ERR_20265.setErrorCode(result);
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        return result;
                    } else {
                        JSONObject currTermAndConditionTextRecord = readTermAndConditionTextJSONArray.getJSONObject(0);
                        termAndConditonTextId = currTermAndConditionTextRecord.getString("id");
                    }
                } else {
                    LOG.error("Failed to get Terms and Conditions text");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                            ActivityStatusEnum.FAILED,
                            "Terms and Conditions update failed. Code: " + termAndConditionCode + "/"
                                    + "Language Code: " + languageCode + "/" + "Title: " + termAndConditionTitle + "/"
                                    + "Description: " + termAndConditionDescription);
                    ErrorCodeEnum.ERR_20265.setErrorCode(result);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                }

                // Supporting Backward compatibility. Updating active termandconditionrecord
                // Updating content in termandconditiontext table
                inputBodyMap.clear();
                inputBodyMap.put("id", termAndConditonTextId);
                inputBodyMap.put("TermAndConditionId", termAndConditionId);
                inputBodyMap.put("LanguageCode", languageCode);
                inputBodyMap.put("ContentType_id", contentType);
                inputBodyMap.put("Content",
                        CommonUtilities.encodeToBase64(CommonUtilities.encodeURI(termAndConditionContent)));
                inputBodyMap.put("ContentModifiedBy", loggedInUserDetails.getUserName());
                inputBodyMap.put("ContentModifiedOn", CommonUtilities.getISOFormattedLocalTimestamp());
                inputBodyMap.put("rtx", "Content");
                inputBodyMap.put("modifiedby", loggedInUserDetails.getUserName());
                inputBodyMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
                String editTermAndConditionTextResponse = Executor
                        .invokeService(ServiceURLEnum.TERMANDCONDITIONTEXT_UPDATE, inputBodyMap, null, requestInstance);
                JSONObject editTermAndConditionTextResponseJSON = CommonUtilities
                        .getStringAsJSONObject(editTermAndConditionTextResponse);
                if (!(editTermAndConditionTextResponseJSON != null
                        && editTermAndConditionTextResponseJSON.has(FabricConstants.OPSTATUS)
                        && editTermAndConditionTextResponseJSON.getInt(FabricConstants.OPSTATUS) == 0)) {
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    ErrorCodeEnum.ERR_20271.setErrorCode(result);
                    LOG.error("Failed to Edit Terms and Conditions text");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                            ActivityStatusEnum.FAILED,
                            "Terms and Conditions update failed. Code: " + termAndConditionCode + "/"
                                    + "Language Code: " + languageCode + "/" + "Title: " + termAndConditionTitle + "/"
                                    + "Description: " + termAndConditionDescription);
                    return result;
                }
            }

            // Edit Term and condition App Preferences
            String appPreferencesStr = requestInstance.getParameter(APP_PREFERENCES_PARAM);
            JSONObject appPreferencesJSON = CommonUtilities.getStringAsJSONObject(appPreferencesStr);
            auditSuccessDescription += "/appPreference:";
            if (appPreferencesJSON != null) {
                Set<String> supportedAppsSet = new HashSet<>();
                Set<String> unSupportedAppsSet = new HashSet<>();
                for (String key : appPreferencesJSON.keySet()) {
                    if ((!supportedAppsSet.contains(key)) && (!unSupportedAppsSet.contains(key))) {
                        if (StringUtils.equalsIgnoreCase(appPreferencesJSON.optString(key), "TRUE")) {
                            supportedAppsSet.add(key);
                        } else if (StringUtils.equalsIgnoreCase(appPreferencesJSON.optString(key), "FALSE")) {
                            unSupportedAppsSet.add(key);
                        }
                        auditSuccessDescription += " " + key + ": " + appPreferencesJSON.getBoolean(key);
                    } else {
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        ErrorCodeEnum.ERR_20273.setErrorCode(result);
                        LOG.error("Duplicate entry of app for applicable apps");
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                                ActivityStatusEnum.FAILED,
                                "Terms and Conditions update failed. Code: " + termAndConditionCode + "/"
                                        + "Language Code: " + languageCode + "/" + "Title: " + termAndConditionTitle
                                        + "/" + "Description: " + termAndConditionDescription);
                        return result;
                    }
                }
                LOG.debug("Setting Terms and conditions App Preferences");

                // Fetch Existing Association
                Set<String> associatedAppsSet = new HashSet<>();
                Map<String, String> associatedTermAndConditionAppIdMap = new HashMap<String, String>();
                inputBodyMap.clear();
                inputBodyMap.put(ODataQueryConstants.FILTER, "TermAndConditionId eq '" + termAndConditionId + "'");
                inputBodyMap.put(ODataQueryConstants.SELECT, "AppId, id");
                String readTermAndConditionAppResponse = Executor.invokeService(ServiceURLEnum.TERMANDCONDITIONAPP_READ,
                        inputBodyMap, null, requestInstance);
                JSONObject readTermAndConditionAppResponseJSON = CommonUtilities
                        .getStringAsJSONObject(readTermAndConditionAppResponse);
                if (readTermAndConditionAppResponseJSON == null
                        || !readTermAndConditionAppResponseJSON.has(FabricConstants.OPSTATUS)
                        || readTermAndConditionAppResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                        || !readTermAndConditionAppResponseJSON.has("termandconditionapp")) {
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    ErrorCodeEnum.ERR_20274.setErrorCode(result);
                    LOG.error("Failed to update terms and conditions applicable app");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                            ActivityStatusEnum.FAILED,
                            "Terms and Conditions update failed. Code: " + termAndConditionCode + "/"
                                    + "Language Code: " + languageCode + "/" + "Title: " + termAndConditionTitle + "/"
                                    + "Description: " + termAndConditionDescription);
                    return result;
                }

                JSONArray termAndConditionAppJSONArray = readTermAndConditionAppResponseJSON
                        .optJSONArray("termandconditionapp");
                for (Object currObj : termAndConditionAppJSONArray) {
                    if (currObj instanceof JSONObject) {
                        JSONObject currJSON = (JSONObject) currObj;
                        if (currJSON.has("AppId")) {
                            associatedAppsSet.add(currJSON.optString("AppId"));
                            associatedTermAndConditionAppIdMap.put(currJSON.optString("AppId"),
                                    currJSON.optString("id"));
                        }
                    }
                }

                // Handle Supported Apps
                if ((supportedAppsSet != null) && (!supportedAppsSet.isEmpty())) {
                    for (String appId : supportedAppsSet) {
                        if (!associatedAppsSet.contains(appId)) {
                            // Creating term and condition app record
                            inputBodyMap.clear();
                            String termAndConditionAppId = Long.toString(CommonUtilities.getNumericId());
                            inputBodyMap.put("id", termAndConditionAppId);
                            inputBodyMap.put("TermAndConditionId", termAndConditionId);
                            inputBodyMap.put("AppId", appId);
                            inputBodyMap.put("createdby", loggedInUserId);
                            inputBodyMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());

                            String createTermAndConditionAppResponse = Executor.invokeService(
                                    ServiceURLEnum.TERMANDCONDITIONAPP_CREATE, inputBodyMap, null, requestInstance);
                            JSONObject createTermAndConditionAppResponseJSON = CommonUtilities
                                    .getStringAsJSONObject(createTermAndConditionAppResponse);
                            if (createTermAndConditionAppResponseJSON == null
                                    || !createTermAndConditionAppResponseJSON.has(FabricConstants.OPSTATUS)
                                    || createTermAndConditionAppResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                                ErrorCodeEnum.ERR_20275.setErrorCode(result);
                                LOG.error("Failed to create terms and conditions applicable app record");
                                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                                        ActivityStatusEnum.FAILED,
                                        "Terms and Conditions update failed. Code: " + termAndConditionCode + "/"
                                                + "Language Code: " + languageCode + "/" + "Title: "
                                                + termAndConditionTitle + "/" + "Description: "
                                                + termAndConditionDescription);
                                return result;
                            }
                        }
                    }
                }

                // Handle UnSupported Apps
                if ((unSupportedAppsSet != null) && (!unSupportedAppsSet.isEmpty())) {
                    for (String appId : unSupportedAppsSet) {
                        if (associatedAppsSet.contains(appId)) {
                            // Deleting term and condition app record
                            inputBodyMap.clear();
                            inputBodyMap.put("id", associatedTermAndConditionAppIdMap.get(appId));
                            String deleteTermAndConditionAppResponse = Executor.invokeService(
                                    ServiceURLEnum.TERMANDCONDITIONAPP_DELETE, inputBodyMap, null, requestInstance);
                            JSONObject deleteTermAndConditionAppResponseJSON = CommonUtilities
                                    .getStringAsJSONObject(deleteTermAndConditionAppResponse);
                            if (deleteTermAndConditionAppResponseJSON == null
                                    || !deleteTermAndConditionAppResponseJSON.has(FabricConstants.OPSTATUS)
                                    || deleteTermAndConditionAppResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                                ErrorCodeEnum.ERR_20276.setErrorCode(result);
                                LOG.error("Failed to delete terms and conditions applicable app record");
                                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                                        ActivityStatusEnum.FAILED,
                                        "Terms and Conditions update failed. Code: " + termAndConditionCode + "/"
                                                + "Language Code: " + languageCode + "/" + "Title: "
                                                + termAndConditionTitle + "/" + "Description: "
                                                + termAndConditionDescription);
                                return result;
                            }
                        }
                    }
                }

                LOG.debug("Terms and conditions App Preferences Set");
            }

            result.addParam(new Param("status", "Success", FabricConstants.STRING));
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL, auditSuccessDescription);

        } catch (Exception e) {
            LOG.error("Failed to Edit Terms and Conditions text");
            ErrorCodeEnum.ERR_20271.setErrorCode(result);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Terms and Conditions update failed.");
            return result;
        }
        return result;
    }

    public Result getAllTermsAndConditions(DataControllerRequest requestInstance) {

        Result result = new Result();

        try {

            // Fetching appId and appName from app table
            Map<String, String> inputBodyMap = new HashMap<String, String>();
            Map<String, String> appIdNameMap = new HashMap<String, String>();
            inputBodyMap.put(ODataQueryConstants.SELECT, "id, Name");
            String readAppResponse = Executor.invokeService(ServiceURLEnum.APP_READ, inputBodyMap, null,
                    requestInstance);
            JSONObject readAppResponseJSON = CommonUtilities.getStringAsJSONObject(readAppResponse);
            if (readAppResponseJSON != null && readAppResponseJSON.has(FabricConstants.OPSTATUS)
                    && readAppResponseJSON.getInt(FabricConstants.OPSTATUS) == 0 && readAppResponseJSON.has("app")) {
                JSONArray readAppJSONArray = readAppResponseJSON.optJSONArray("app");
                if ((readAppJSONArray != null) && (readAppJSONArray.length() > 0)) {
                    for (int indexVar = 0; indexVar < readAppJSONArray.length(); indexVar++) {
                        JSONObject appJSONObject = readAppJSONArray.getJSONObject(indexVar);
                        String appId = appJSONObject.getString("id");
                        String appName = appJSONObject.getString("Name");
                        appIdNameMap.put(appId, appName);
                    }
                }
            }

            // Fetching contentTypeId and contentTypeName from contenttype table
            inputBodyMap.clear();
            Map<String, String> contentTypeIdNameMap = new HashMap<String, String>();
            inputBodyMap.put(ODataQueryConstants.SELECT, "id, Name");
            String readContentTypeResponse = Executor.invokeService(ServiceURLEnum.CONTENTTYPE_READ, inputBodyMap, null,
                    requestInstance);
            JSONObject readContentTypeResponseJSON = CommonUtilities.getStringAsJSONObject(readContentTypeResponse);
            if (readContentTypeResponseJSON != null && readContentTypeResponseJSON.has(FabricConstants.OPSTATUS)
                    && readContentTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readContentTypeResponseJSON.has("contenttype")) {
                JSONArray readContentTypeJSONArray = readContentTypeResponseJSON.optJSONArray("contenttype");
                if ((readContentTypeJSONArray != null) && (readContentTypeJSONArray.length() > 0)) {
                    for (int indexVar = 0; indexVar < readContentTypeJSONArray.length(); indexVar++) {
                        JSONObject contentTypeJSONObject = readContentTypeJSONArray.getJSONObject(indexVar);
                        String contentTypeId = contentTypeJSONObject.getString("id");
                        String contentTypeName = contentTypeJSONObject.getString("Name");
                        contentTypeIdNameMap.put(contentTypeId, contentTypeName);
                    }
                }
            }

            // Fetching languageId and languageName from locale table
            inputBodyMap.clear();
            Map<String, String> languageIdNameMap = new HashMap<String, String>();
            inputBodyMap.put(ODataQueryConstants.SELECT, "Code, Language");
            String readlanguageResponse = Executor.invokeService(ServiceURLEnum.LOCALE_READ, inputBodyMap, null,
                    requestInstance);
            JSONObject readlanguageResponseJSON = CommonUtilities.getStringAsJSONObject(readlanguageResponse);
            if (readlanguageResponseJSON != null && readlanguageResponseJSON.has(FabricConstants.OPSTATUS)
                    && readlanguageResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readlanguageResponseJSON.has("locale")) {
                JSONArray readlanguageJSONArray = readlanguageResponseJSON.optJSONArray("locale");
                if ((readlanguageJSONArray != null) && (readlanguageJSONArray.length() > 0)) {
                    for (int indexVar = 0; indexVar < readlanguageJSONArray.length(); indexVar++) {
                        JSONObject languageJSONObject = readlanguageJSONArray.getJSONObject(indexVar);
                        String languageId = languageJSONObject.getString("Code");
                        String languageName = languageJSONObject.getString("Language");
                        languageIdNameMap.put(languageId, languageName);
                    }
                }
            }

            // Fetching T&C id, Code, Title, Description, ContentModifiedBy and
            // ContentModifiedOn from termandcondition table
            String termAndConditionId = null, termAndConditionCode = null, termAndConditionTitle = null,
                    termAndConditionDescription = null;
            inputBodyMap.clear();
            inputBodyMap.put(ODataQueryConstants.SELECT, "id, Code, Title, Description");
            String readTermAndConditionResponse = Executor.invokeService(ServiceURLEnum.TERMANDCONDITION_READ,
                    inputBodyMap, null, requestInstance);
            JSONObject readTermAndConditionResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readTermAndConditionResponse);
            if (readTermAndConditionResponseJSON != null
                    && readTermAndConditionResponseJSON.has(FabricConstants.OPSTATUS)
                    && readTermAndConditionResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readTermAndConditionResponseJSON.has("termandcondition")) {
                JSONArray readTermAndConditionJSONArray = readTermAndConditionResponseJSON
                        .optJSONArray("termandcondition");
                if ((readTermAndConditionJSONArray != null) && (readTermAndConditionJSONArray.length() > 0)) {
                    Dataset termsAndConditionsDataset = new Dataset();
                    termsAndConditionsDataset.setId("termsAndConditions");
                    JSONObject termAndConditionJSONObject = null;
                    for (int indexVar = 0; indexVar < readTermAndConditionJSONArray.length(); indexVar++) {
                        termAndConditionJSONObject = readTermAndConditionJSONArray.getJSONObject(indexVar);
                        Record currRecord = new Record();
                        termAndConditionId = termAndConditionJSONObject.getString("id");
                        termAndConditionCode = termAndConditionJSONObject.getString("Code");
                        termAndConditionTitle = termAndConditionJSONObject.getString("Title");
                        termAndConditionDescription = termAndConditionJSONObject.getString("Description");

                        // Skipping records that does not have content for default language
                        inputBodyMap.clear();
                        inputBodyMap.put(ODataQueryConstants.FILTER,
                                "TermAndConditionId eq '" + termAndConditionId + "' and LanguageCode eq '"
                                        + DEFAULT_LANGUAGE_CODE + "' and Status_id eq '" + TANDC_ACTIVE_STATUS_ID
                                        + "'");
                        inputBodyMap.put(ODataQueryConstants.SELECT, "Content");
                        String _readTermAndConditionTextResponse = Executor.invokeService(
                                ServiceURLEnum.TERMANDCONDITIONTEXT_READ, inputBodyMap, null, requestInstance);
                        JSONObject _readTermAndConditionTextResponseJSON = CommonUtilities
                                .getStringAsJSONObject(_readTermAndConditionTextResponse);
                        if (_readTermAndConditionTextResponseJSON != null
                                && _readTermAndConditionTextResponseJSON.has(FabricConstants.OPSTATUS)
                                && _readTermAndConditionTextResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                                && _readTermAndConditionTextResponseJSON.has("termandconditiontext")) {
                            JSONArray _readTermAndConditionTextJSONArray = _readTermAndConditionTextResponseJSON
                                    .optJSONArray("termandconditiontext");
                            if ((_readTermAndConditionTextJSONArray != null)
                                    && (_readTermAndConditionTextJSONArray.length() > 0)) {
                                String content = _readTermAndConditionTextJSONArray.getJSONObject(0)
                                        .optString("Content");
                                if (StringUtils.isEmpty(content))
                                    continue;
                            }
                        }

                        Param termAndConditionCode_Param = new Param("code", termAndConditionCode,
                                FabricConstants.STRING);
                        currRecord.addParam(termAndConditionCode_Param);
                        Param termAndConditionTitle_Param = new Param("title", termAndConditionTitle,
                                FabricConstants.STRING);
                        currRecord.addParam(termAndConditionTitle_Param);
                        Param termAndConditionDescription_Param = new Param("description", termAndConditionDescription,
                                FabricConstants.STRING);
                        currRecord.addParam(termAndConditionDescription_Param);

                        // Fetch supportedApps from termandconditionapp
                        Set<String> supportedAppsSet = new HashSet<String>();
                        Dataset appPreferencesDataset = new Dataset();
                        appPreferencesDataset.setId("appPreferences");
                        inputBodyMap.clear();
                        inputBodyMap.put(ODataQueryConstants.FILTER,
                                "TermAndConditionId eq '" + termAndConditionId + "'");
                        inputBodyMap.put(ODataQueryConstants.SELECT, "AppId");
                        String readTermAndConditionAppResponse = Executor.invokeService(
                                ServiceURLEnum.TERMANDCONDITIONAPP_READ, inputBodyMap, null, requestInstance);
                        JSONObject readTermAndConditionAppResponseJSON = CommonUtilities
                                .getStringAsJSONObject(readTermAndConditionAppResponse);
                        if (readTermAndConditionAppResponseJSON != null
                                && readTermAndConditionAppResponseJSON.has(FabricConstants.OPSTATUS)
                                && readTermAndConditionAppResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                                && readTermAndConditionAppResponseJSON.has("termandconditionapp")) {
                            JSONArray readTermAndConditionAppResponseJSONArray = readTermAndConditionAppResponseJSON
                                    .optJSONArray("termandconditionapp");
                            if ((readTermAndConditionAppResponseJSONArray != null)
                                    && (readTermAndConditionAppResponseJSONArray.length() > 0)) {
                                for (int index = 0; index < readTermAndConditionAppResponseJSONArray
                                        .length(); index++) {
                                    // Adding supported apps records
                                    String appId = readTermAndConditionAppResponseJSONArray.getJSONObject(index)
                                            .optString("AppId");
                                    Record appRecord = new Record();
                                    Param appId_Param = new Param("appId", appId, FabricConstants.STRING);
                                    appRecord.addParam(appId_Param);
                                    if (appIdNameMap.containsKey(appId)) {
                                        Param appName_Param = new Param("appName", appIdNameMap.get(appId),
                                                FabricConstants.STRING);
                                        appRecord.addParam(appName_Param);
                                    }
                                    Param isSupported_Param = new Param("isSupported", "true", FabricConstants.STRING);
                                    appRecord.addParam(isSupported_Param);
                                    supportedAppsSet.add(appId);
                                    appPreferencesDataset.addRecord(appRecord);
                                }
                            }
                        }

                        // Adding unsupported apps record
                        if ((appIdNameMap != null) && (!appIdNameMap.isEmpty())) {
                            for (Map.Entry<String, String> appElement : appIdNameMap.entrySet()) {
                                String appId = (String) appElement.getKey();
                                if (!supportedAppsSet.contains(appId)) {
                                    Record appRecord = new Record();
                                    Param appId_Param = new Param("appId", appId, FabricConstants.STRING);
                                    appRecord.addParam(appId_Param);
                                    Param appName_Param = new Param("appName", appIdNameMap.get(appId),
                                            FabricConstants.STRING);
                                    appRecord.addParam(appName_Param);
                                    Param isSupported_Param = new Param("isSupported", "false", FabricConstants.STRING);
                                    appRecord.addParam(isSupported_Param);
                                    appPreferencesDataset.addRecord(appRecord);
                                }
                            }
                        }

                        currRecord.addDataset(appPreferencesDataset);

                        // For backward compatibilty we need below 2 param from termandconditiontext
                        // table
                        String contentModifiedByForOneRecord = null, contentModifiedOnForOneRecord = null;

                        // Fetch T&C Content, languageCode and T&C content type from
                        // termandconditiontext table
                        inputBodyMap.clear();
                        inputBodyMap.put(ODataQueryConstants.FILTER,
                                "TermAndConditionId eq '" + termAndConditionId + "'");
                        inputBodyMap.put(ODataQueryConstants.SELECT,
                                "LanguageCode, Content, ContentType_id, ContentModifiedBy, ContentModifiedOn, Status_id, Version_Id, Description");
                        String readTermAndConditionTextResponse = Executor.invokeService(
                                ServiceURLEnum.TERMANDCONDITIONTEXT_READ, inputBodyMap, null, requestInstance);
                        JSONObject readTermAndConditionTextResponseJSON = CommonUtilities
                                .getStringAsJSONObject(readTermAndConditionTextResponse);
                        if (readTermAndConditionTextResponseJSON != null
                                && readTermAndConditionTextResponseJSON.has(FabricConstants.OPSTATUS)
                                && readTermAndConditionTextResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                                && readTermAndConditionTextResponseJSON.has("termandconditiontext")) {
                            JSONArray readTermAndConditionTextJSONArray = readTermAndConditionTextResponseJSON
                                    .optJSONArray("termandconditiontext");
                            if ((readTermAndConditionTextJSONArray != null)
                                    && (readTermAndConditionTextJSONArray.length() > 0)) {
                                Dataset termsAndConditionsVersionDataset = createTermsAndConditionsVersionDataset(
                                        readTermAndConditionTextJSONArray, contentTypeIdNameMap, languageIdNameMap);
                                // Not changing below logic to support backward compatibility
                                JSONObject termAndConditionTextJSONObject = null;
                                Record termsAndConditionsContentRecords = new Record();
                                termsAndConditionsContentRecords.setId("termsAndConditionsContent");
                                for (int index = 0; index < readTermAndConditionTextJSONArray.length(); index++) {
                                    termAndConditionTextJSONObject = readTermAndConditionTextJSONArray
                                            .getJSONObject(index);

                                    if (index == 0) {
                                        contentModifiedByForOneRecord = readTermAndConditionTextJSONArray
                                                .getJSONObject(0).getString("ContentModifiedBy");
                                        contentModifiedOnForOneRecord = readTermAndConditionTextJSONArray
                                                .getJSONObject(0).getString("ContentModifiedOn");
                                    }

                                    String languageCode = termAndConditionTextJSONObject.getString("LanguageCode");
                                    String termAndConditionContent = termAndConditionTextJSONObject
                                            .optString("Content");
                                    String termAndConditionContentTypeId = termAndConditionTextJSONObject
                                            .getString("ContentType_id");
                                    String termsAndConditionsContentModifiedBy = termAndConditionTextJSONObject
                                            .getString("ContentModifiedBy");
                                    String termsAndConditionsContentModifiedOn = termAndConditionTextJSONObject
                                            .getString("ContentModifiedOn");
                                    String termsAndConditionsStatusId = termAndConditionTextJSONObject
                                            .getString("Status_id");
                                    String termsAndConditionsVersionId = termAndConditionTextJSONObject
                                            .getString("Version_Id");
                                    String termsAndConditionsVersionDescription = termAndConditionTextJSONObject
                                            .optString("Description");

                                    String termAndConditionContentTypeName = null;
                                    if (contentTypeIdNameMap.containsKey(termAndConditionContentTypeId))
                                        termAndConditionContentTypeName = contentTypeIdNameMap
                                                .get(termAndConditionContentTypeId);
                                    String languageName = null;
                                    if (languageIdNameMap.containsKey(languageCode))
                                        languageName = languageIdNameMap.get(languageCode);

                                    Record contentLangRecord = new Record();
                                    contentLangRecord.setId(languageCode);
                                    Param content_Param = new Param("content", termAndConditionContent,
                                            FabricConstants.STRING);
                                    contentLangRecord.addParam(content_Param);
                                    Param contentTypeId_Param = new Param("contentTypeId",
                                            termAndConditionContentTypeId, FabricConstants.STRING);
                                    contentLangRecord.addParam(contentTypeId_Param);
                                    Param contentType_Param = new Param("contentTypeName",
                                            termAndConditionContentTypeName, FabricConstants.STRING);
                                    contentLangRecord.addParam(contentType_Param);
                                    Param languageCode_Param = new Param("languageCode", languageCode,
                                            FabricConstants.STRING);
                                    contentLangRecord.addParam(languageCode_Param);
                                    Param languageName_Param = new Param("languageName", languageName,
                                            FabricConstants.STRING);
                                    contentLangRecord.addParam(languageName_Param);
                                    Param contentModifiedBy_Param = new Param("contentModifiedBy",
                                            termsAndConditionsContentModifiedBy, FabricConstants.STRING);
                                    contentLangRecord.addParam(contentModifiedBy_Param);
                                    Param contentModifiedOn_Param = new Param("contentModifiedOn",
                                            termsAndConditionsContentModifiedOn, FabricConstants.STRING);
                                    contentLangRecord.addParam(contentModifiedOn_Param);
                                    Param statusId_Param = new Param("statusId", termsAndConditionsStatusId,
                                            FabricConstants.STRING);
                                    contentLangRecord.addParam(statusId_Param);
                                    Param versionId_Param = new Param("versionId", termsAndConditionsVersionId,
                                            FabricConstants.STRING);
                                    contentLangRecord.addParam(versionId_Param);
                                    Param versionDescription_Param = new Param("versionDescription",
                                            termsAndConditionsVersionDescription, FabricConstants.STRING);
                                    contentLangRecord.addParam(versionDescription_Param);
                                    termsAndConditionsContentRecords.addRecord(contentLangRecord);
                                }
                                currRecord.addRecord(termsAndConditionsContentRecords);
                                currRecord.addDataset(termsAndConditionsVersionDataset);
                            }
                        } else {
                            LOG.error("Failed to get Term and Condition text");
                            ErrorCodeEnum.ERR_20265.setErrorCode(result);
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            return result;
                        }
                        // For Backward compatibility
                        Param termAndConditionContentModifiedBy_Param = new Param("contentModifiedBy",
                                contentModifiedByForOneRecord, FabricConstants.STRING);
                        currRecord.addParam(termAndConditionContentModifiedBy_Param);
                        Param termAndConditionContentModifiedOn_Param = new Param("contentModifiedOn",
                                contentModifiedOnForOneRecord, FabricConstants.STRING);
                        currRecord.addParam(termAndConditionContentModifiedOn_Param);

                        termsAndConditionsDataset.addRecord(currRecord);
                    }
                    result.addDataset(termsAndConditionsDataset);
                }
            }
            result.addParam(new Param("status", "Success", FabricConstants.STRING));
            return result;
        } catch (Exception e) {
            LOG.error("Failed to get Terms and Conditions text. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20265.setErrorCode(result);
        }
        return result;
    }

    public Dataset createTermsAndConditionsVersionDataset(JSONArray readTermAndConditionTextJSONArray,
            Map<String, String> contentTypeIdNameMap, Map<String, String> languageIdNameMap) {

        Dataset termsAndConditionsVersionDataset = new Dataset();
        termsAndConditionsVersionDataset.setId("termsAndConditionsVersion");
        JSONObject termAndConditionTextJSONObject = null;

        for (int index = 0; index < readTermAndConditionTextJSONArray.length(); index++) {

            termAndConditionTextJSONObject = readTermAndConditionTextJSONArray.getJSONObject(index);

            String languageCode = termAndConditionTextJSONObject.getString("LanguageCode");
            String termAndConditionContent = termAndConditionTextJSONObject.optString("Content");
            String termAndConditionVersionDescription = termAndConditionTextJSONObject.optString("Description");
            String termAndConditionContentTypeId = termAndConditionTextJSONObject.getString("ContentType_id");
            String termsAndConditionsContentModifiedBy = termAndConditionTextJSONObject.getString("ContentModifiedBy");
            String termsAndConditionsContentModifiedOn = termAndConditionTextJSONObject.getString("ContentModifiedOn");
            String termsAndConditionsStatusId = termAndConditionTextJSONObject.getString("Status_id");
            String termsAndConditionsVersionId = termAndConditionTextJSONObject.getString("Version_Id");

            String termAndConditionContentTypeName = null;
            if (contentTypeIdNameMap.containsKey(termAndConditionContentTypeId))
                termAndConditionContentTypeName = contentTypeIdNameMap.get(termAndConditionContentTypeId);
            String languageName = null;
            if (languageIdNameMap.containsKey(languageCode))
                languageName = languageIdNameMap.get(languageCode);

            Record termsAndConditionsVersionRecord = new Record();
            termsAndConditionsVersionRecord.setId(languageCode);
            Param content_Param = new Param("content", termAndConditionContent, FabricConstants.STRING);
            termsAndConditionsVersionRecord.addParam(content_Param);
            Param contentTypeId_Param = new Param("contentTypeId", termAndConditionContentTypeId,
                    FabricConstants.STRING);
            termsAndConditionsVersionRecord.addParam(contentTypeId_Param);
            Param contentType_Param = new Param("contentTypeName", termAndConditionContentTypeName,
                    FabricConstants.STRING);
            termsAndConditionsVersionRecord.addParam(contentType_Param);
            Param languageCode_Param = new Param("languageCode", languageCode, FabricConstants.STRING);
            termsAndConditionsVersionRecord.addParam(languageCode_Param);
            Param languageName_Param = new Param("languageName", languageName, FabricConstants.STRING);
            termsAndConditionsVersionRecord.addParam(languageName_Param);
            Param contentModifiedBy_Param = new Param("contentModifiedBy", termsAndConditionsContentModifiedBy,
                    FabricConstants.STRING);
            termsAndConditionsVersionRecord.addParam(contentModifiedBy_Param);
            Param contentModifiedOn_Param = new Param("contentModifiedOn", termsAndConditionsContentModifiedOn,
                    FabricConstants.STRING);
            termsAndConditionsVersionRecord.addParam(contentModifiedOn_Param);
            Param statusId_Param = new Param("statusId", termsAndConditionsStatusId, FabricConstants.STRING);
            termsAndConditionsVersionRecord.addParam(statusId_Param);
            Param versionId_Param = new Param("versionId", termsAndConditionsVersionId, FabricConstants.STRING);
            termsAndConditionsVersionRecord.addParam(versionId_Param);
            Param versionDescription_Param = new Param("versionDescription", termAndConditionVersionDescription,
                    FabricConstants.STRING);
            termsAndConditionsVersionRecord.addParam(versionDescription_Param);
            termsAndConditionsVersionDataset.addRecord(termsAndConditionsVersionRecord);
        }
        return termsAndConditionsVersionDataset;
    }

    public Result createTermsAndConditionsVersion(DataControllerRequest requestInstance) {

        Result result = new Result();

        try {

            String termAndConditionCode = requestInstance.getParameter("termsAndConditionsCode");
            String languageCode = requestInstance.getParameter("languageCode");
            String contentType = requestInstance.getParameter("contentType");
            String termAndConditionVersionDescription = requestInstance.getParameter("versionDescription");
            String termAndConditionContent = requestInstance.getParameter("termsAndConditionsContent");
            String isSave = requestInstance.getParameter("isSave");

            String auditSuccessDescription = "Terms and conditions version create successful. Code: "
                    + termAndConditionCode + "/" + "Language Code: " + languageCode + "/" + "Content Type: "
                    + contentType + "/" + "Description: " + termAndConditionVersionDescription;

            if (StringUtils.isBlank(termAndConditionCode)) {
                ErrorCodeEnum.ERR_20266.setErrorCode(result);
                LOG.error("Term and condition code is a mandatory input");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED,
                        "Terms and Conditions create failed. Code: " + termAndConditionCode + "/" + "Language Code: "
                                + languageCode + "/" + "Content Type: " + contentType + "/" + "Description: "
                                + termAndConditionVersionDescription);
                return result;
            }

            if (StringUtils.isBlank(languageCode)) {
                languageCode = DEFAULT_LANGUAGE_CODE;
            }

            if (StringUtils.isBlank(contentType)) {
                ErrorCodeEnum.ERR_20272.setErrorCode(result);
                LOG.error("Terms and conditions content type is a mandatory input");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED,
                        "Terms and Conditions version create failed. Code: " + termAndConditionCode + "/"
                                + "Language Code: " + languageCode + "/" + "Content Type: " + contentType + "/"
                                + "Description: " + termAndConditionVersionDescription);
                return result;
            }

            if (StringUtils.isBlank(termAndConditionContent)) {
                ErrorCodeEnum.ERR_20270.setErrorCode(result);
                LOG.error("Terms and conditions content is a mandatory input");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED,
                        "Terms and Conditions version create failed. Code: " + termAndConditionCode + "/"
                                + "Language Code: " + languageCode + "/" + "Content Type: " + contentType + "/"
                                + "Description: " + termAndConditionVersionDescription);
                return result;
            }

            if (StringUtils.length(
                    termAndConditionVersionDescription) > TERM_AND_CONDITION_VERSION_DESCRIPTION_MAX_CHARACTERS) {
                ErrorCodeEnum.ERR_20277.setErrorCode(result);
                LOG.error("Terms and conditions version description length is not in range");
                result.addParam(new Param("message",
                        "Terms and conditions version description should have a maximum of "
                                + TERM_AND_CONDITION_VERSION_DESCRIPTION_MAX_CHARACTERS + " characters",
                        FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED,
                        "Terms and Conditions version create failed. Code: " + termAndConditionCode + "/"
                                + "Language Code: " + languageCode + "/" + "Content Type: " + contentType + "/"
                                + "Description: " + termAndConditionVersionDescription);
                return result;
            }

            // Fetching T&C id from termandcondition table
            String termAndConditionId = null;
            termAndConditionId = getTermAndConditionId(requestInstance, termAndConditionCode, result);
            if (termAndConditionId == null) {
                LOG.error("Failed to get Terms and Conditions Id");
                ErrorCodeEnum.ERR_20264.setErrorCode(result);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED,
                        "Terms and Conditions version create failed. Code: " + termAndConditionCode + "/"
                                + "Language Code: " + languageCode + "/" + "Content Type: " + contentType + "/"
                                + "Description: " + termAndConditionVersionDescription);
                return result;
            }

            if (StringUtils.equalsIgnoreCase(isSave, "true")) {
                String termAndConditionTextId = null;
                termAndConditionTextId = getTermAndConditionTextId(requestInstance, termAndConditionId, languageCode,
                        TANDC_DRAFT_STATUS_ID, result);
                if (termAndConditionTextId == null) {
                    createTermAndConditionText(requestInstance, termAndConditionId, languageCode, contentType,
                            termAndConditionContent, termAndConditionVersionDescription, DEFAULT_DRAFT_VERSION,
                            TANDC_DRAFT_STATUS_ID, result);
                    if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                        LOG.error("Terms and Conditions draft version create failed");
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                                ActivityStatusEnum.FAILED,
                                "Terms and Conditions draft version create failed. Code: " + termAndConditionCode + "/"
                                        + "Language Code: " + languageCode + "/" + "Content Type: " + contentType + "/"
                                        + "Description: " + termAndConditionVersionDescription);
                        return result;
                    }
                } else {
                    editTermAndConditionText(requestInstance, termAndConditionTextId, termAndConditionId, languageCode,
                            contentType, termAndConditionContent, termAndConditionVersionDescription,
                            DEFAULT_DRAFT_VERSION, TANDC_DRAFT_STATUS_ID, result);
                    if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                        LOG.error("Terms and Conditions draft version update failed");
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                                ActivityStatusEnum.FAILED,
                                "Terms and Conditions draft version update failed. Code: " + termAndConditionCode + "/"
                                        + "Language Code: " + languageCode + "/" + "Content Type: " + contentType + "/"
                                        + "Description: " + termAndConditionVersionDescription);
                        return result;
                    }
                }
            } else {
                String termAndConditionActiveTextId = null;
                String termAndConditionDraftTextId = null;
                String termAndConditionArchivedTextId = null;
                termAndConditionActiveTextId = getTermAndConditionTextId(requestInstance, termAndConditionId,
                        languageCode, TANDC_ACTIVE_STATUS_ID, result);
                termAndConditionDraftTextId = getTermAndConditionTextId(requestInstance, termAndConditionId,
                        languageCode, TANDC_DRAFT_STATUS_ID, result);
                termAndConditionArchivedTextId = getTermAndConditionTextId(requestInstance, termAndConditionId,
                        languageCode, TANDC_ARCHIVED_STATUS_ID, result);

                // Creating 1.0 version record
                if (termAndConditionActiveTextId == null && termAndConditionArchivedTextId == null) {
                    if (termAndConditionDraftTextId != null) {
                        editTermAndConditionText(requestInstance, termAndConditionDraftTextId, termAndConditionId,
                                languageCode, contentType, termAndConditionContent, termAndConditionVersionDescription,
                                MIN_VERSION, TANDC_ACTIVE_STATUS_ID, result);
                        if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                                    ActivityStatusEnum.FAILED,
                                    "Terms and Conditions version create failed. Code: " + termAndConditionCode + "/"
                                            + "Language Code: " + languageCode + "/" + "Content Type: " + contentType
                                            + "/" + "Description: " + termAndConditionVersionDescription);
                            return result;
                        }
                    } else {
                        createTermAndConditionText(requestInstance, termAndConditionId, languageCode, contentType,
                                termAndConditionContent, termAndConditionVersionDescription, MIN_VERSION,
                                TANDC_ACTIVE_STATUS_ID, result);
                        if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                                    ActivityStatusEnum.FAILED,
                                    "Terms and Conditions version create failed. Code: " + termAndConditionCode + "/"
                                            + "Language Code: " + languageCode + "/" + "Content Type: " + contentType
                                            + "/" + "Description: " + termAndConditionVersionDescription);
                            return result;
                        }
                    }
                    auditSuccessDescription += "/Version: " + MIN_VERSION;
                    result.addParam(new Param("status", "Success", FabricConstants.STRING));
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                            ActivityStatusEnum.SUCCESSFUL, auditSuccessDescription);
                    return result;
                }

                if (termAndConditionActiveTextId == null) {
                    LOG.error("Terms and conditions has no previous active version");
                    ErrorCodeEnum.ERR_20279.setErrorCode(result);
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                            ActivityStatusEnum.FAILED, "Terms and conditions has no previous active version");
                    return result;
                }

                String newVersionId = null;
                newVersionId = getNewVersion(requestInstance, termAndConditionId, languageCode, result);

                auditSuccessDescription += "/Version: " + newVersionId;

                if (newVersionId == null) {
                    LOG.error("Failed to create Terms and Conditions version");
                    ErrorCodeEnum.ERR_20277.setErrorCode(result);
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                            ActivityStatusEnum.FAILED,
                            "Terms and Conditions version create failed. Code: " + termAndConditionCode + "/"
                                    + "Language Code: " + languageCode + "/" + "Content Type: " + contentType + "/"
                                    + "Description: " + termAndConditionVersionDescription);
                    return result;
                }

                if (termAndConditionDraftTextId != null) {
                    editTermAndConditionText(requestInstance, termAndConditionDraftTextId, termAndConditionId,
                            languageCode, contentType, termAndConditionContent, termAndConditionVersionDescription,
                            newVersionId, TANDC_ACTIVE_STATUS_ID, result);
                    if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                                ActivityStatusEnum.FAILED,
                                "Terms and Conditions version create failed. Code: " + termAndConditionCode + "/"
                                        + "Language Code: " + languageCode + "/" + "Content Type: " + contentType + "/"
                                        + "Description: " + termAndConditionVersionDescription);
                        return result;
                    }
                } else {
                    createTermAndConditionText(requestInstance, termAndConditionId, languageCode, contentType,
                            termAndConditionContent, termAndConditionVersionDescription, newVersionId,
                            TANDC_ACTIVE_STATUS_ID, result);
                    if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                                ActivityStatusEnum.FAILED,
                                "Terms and Conditions version create failed. Code: " + termAndConditionCode + "/"
                                        + "Language Code: " + languageCode + "/" + "Content Type: " + contentType + "/"
                                        + "Description: " + termAndConditionVersionDescription);
                        return result;
                    }
                }
                editTermAndConditionTextStatus(requestInstance, termAndConditionActiveTextId, TANDC_ARCHIVED_STATUS_ID,
                        result);
                if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                            ActivityStatusEnum.FAILED,
                            "Terms and Conditions version create failed. Code: " + termAndConditionCode + "/"
                                    + "Language Code: " + languageCode + "/" + "Content Type: " + contentType + "/"
                                    + "Description: " + termAndConditionVersionDescription);
                    return result;
                }
            }

            result.addParam(new Param("status", "Success", FabricConstants.STRING));
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                    ActivityStatusEnum.SUCCESSFUL, auditSuccessDescription);

        } catch (Exception e) {
            LOG.error("Failed to create Terms and Conditions version");
            ErrorCodeEnum.ERR_20277.setErrorCode(result);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "Failed to create Terms and Conditions version");
            return result;
        }
        return result;
    }

    public void editTermAndConditionText(DataControllerRequest requestInstance, String termAndConditionTextId,
            String termAndConditionId, String languageCode, String contentType, String termAndConditionContent,
            String termAndConditionVersionDescription, String version, String statusId, Result result) {

        try {

            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);

            Map<String, String> inputBodyMap = new HashMap<String, String>();
            inputBodyMap.put("id", termAndConditionTextId);
            inputBodyMap.put("TermAndConditionId", termAndConditionId);
            inputBodyMap.put("LanguageCode", languageCode);
            inputBodyMap.put("ContentType_id", contentType);
            inputBodyMap.put("Content",
                    CommonUtilities.encodeToBase64(CommonUtilities.encodeURI(termAndConditionContent)));
            inputBodyMap.put("Description", termAndConditionVersionDescription);
            inputBodyMap.put("ContentModifiedBy", loggedInUserDetails.getUserName());
            inputBodyMap.put("ContentModifiedOn", CommonUtilities.getISOFormattedLocalTimestamp());
            inputBodyMap.put("rtx", "Content");
            inputBodyMap.put("Version_Id", version);
            inputBodyMap.put("Status_id", statusId);
            inputBodyMap.put("modifiedby", loggedInUserDetails.getUserName());
            inputBodyMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            String editTermAndConditionTextResponse = Executor.invokeService(ServiceURLEnum.TERMANDCONDITIONTEXT_UPDATE,
                    inputBodyMap, null, requestInstance);
            JSONObject editTermAndConditionTextResponseJSON = CommonUtilities
                    .getStringAsJSONObject(editTermAndConditionTextResponse);
            if (!(editTermAndConditionTextResponseJSON != null
                    && editTermAndConditionTextResponseJSON.has(FabricConstants.OPSTATUS)
                    && editTermAndConditionTextResponseJSON.getInt(FabricConstants.OPSTATUS) == 0)) {
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20271.setErrorCode(result);
                LOG.error("Failed to update Terms and Conditions text");
                return;
            }
        } catch (Exception e) {
            LOG.error("Failed to edit Terms and Conditions version");
            ErrorCodeEnum.ERR_20278.setErrorCode(result);
            return;
        }
    }

    public void editTermAndConditionTextStatus(DataControllerRequest requestInstance, String termAndConditionTextId,
            String statusId, Result result) {

        try {

            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);

            Map<String, String> inputBodyMap = new HashMap<String, String>();
            inputBodyMap.put("id", termAndConditionTextId);
            inputBodyMap.put("Status_id", statusId);
            inputBodyMap.put("modifiedby", loggedInUserDetails.getUserName());
            inputBodyMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            String editTermAndConditionTextResponse = Executor.invokeService(ServiceURLEnum.TERMANDCONDITIONTEXT_UPDATE,
                    inputBodyMap, null, requestInstance);
            JSONObject editTermAndConditionTextResponseJSON = CommonUtilities
                    .getStringAsJSONObject(editTermAndConditionTextResponse);
            if (!(editTermAndConditionTextResponseJSON != null
                    && editTermAndConditionTextResponseJSON.has(FabricConstants.OPSTATUS)
                    && editTermAndConditionTextResponseJSON.getInt(FabricConstants.OPSTATUS) == 0)) {
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20271.setErrorCode(result);
                LOG.error("Failed to update Terms and Conditions text");
                return;
            }
        } catch (Exception e) {
            LOG.error("Failed to edit Terms and Conditions version");
            ErrorCodeEnum.ERR_20278.setErrorCode(result);
            return;
        }
    }

    public void createTermAndConditionText(DataControllerRequest requestInstance, String termAndConditionId,
            String languageCode, String contentType, String termAndConditionContent,
            String termAndConditionVersionDescription, String version, String statusId, Result result) {

        try {
            String loggedInUserId = null;
            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            if (loggedInUserDetails != null) {
                loggedInUserId = loggedInUserDetails.getUserId();
            }

            // Create termandconditiontext record
            String termAndConditionTextId = Long.toString(CommonUtilities.getNumericId());
            Map<String, String> inputBodyMap = new HashMap<String, String>();
            inputBodyMap.put("id", termAndConditionTextId);
            inputBodyMap.put("TermAndConditionId", termAndConditionId);
            inputBodyMap.put("LanguageCode", languageCode);
            inputBodyMap.put("ContentType_id", contentType);
            inputBodyMap.put("Content",
                    CommonUtilities.encodeToBase64(CommonUtilities.encodeURI(termAndConditionContent)));
            inputBodyMap.put("Description", termAndConditionVersionDescription);
            inputBodyMap.put("ContentModifiedBy", loggedInUserDetails.getUserName());
            inputBodyMap.put("ContentModifiedOn", CommonUtilities.getISOFormattedLocalTimestamp());
            inputBodyMap.put("rtx", "Content");
            inputBodyMap.put("Version_Id", version);
            inputBodyMap.put("Status_id", statusId);
            inputBodyMap.put("createdby", loggedInUserId);
            inputBodyMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());

            String createTermAndConditionTextResponse = Executor
                    .invokeService(ServiceURLEnum.TERMANDCONDITIONTEXT_CREATE, inputBodyMap, null, requestInstance);
            JSONObject createTermAndConditionTextResponseJSON = CommonUtilities
                    .getStringAsJSONObject(createTermAndConditionTextResponse);
            if (!(createTermAndConditionTextResponseJSON != null
                    && createTermAndConditionTextResponseJSON.has(FabricConstants.OPSTATUS)
                    && createTermAndConditionTextResponseJSON.getInt(FabricConstants.OPSTATUS) == 0)) {
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20277.setErrorCode(result);
                LOG.error("Failed to create Terms and Conditions version");
                return;
            }
        } catch (Exception e) {
            LOG.error("Failed to create Terms and Conditions version");
            ErrorCodeEnum.ERR_20277.setErrorCode(result);
            return;
        }
    }

    public String getTermAndConditionTextId(DataControllerRequest requestInstance, String termAndConditionId,
            String languageCode, String statusId, Result result) {
        String termAndConditionTextId = null;
        Map<String, String> inputBodyMap = new HashMap<String, String>();
        inputBodyMap.put(ODataQueryConstants.FILTER, "TermAndConditionId eq '" + termAndConditionId
                + "' and LanguageCode eq '" + languageCode + "' and Status_id eq '" + statusId + "'");
        inputBodyMap.put(ODataQueryConstants.SELECT, "id");
        String readTermAndConditionTextResponse = Executor.invokeService(ServiceURLEnum.TERMANDCONDITIONTEXT_READ,
                inputBodyMap, null, requestInstance);
        JSONObject readTermAndConditionTextResponseJSON = CommonUtilities
                .getStringAsJSONObject(readTermAndConditionTextResponse);
        if (readTermAndConditionTextResponseJSON != null
                && readTermAndConditionTextResponseJSON.has(FabricConstants.OPSTATUS)
                && readTermAndConditionTextResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readTermAndConditionTextResponseJSON.has("termandconditiontext")) {
            JSONArray readTermAndConditionTextJSONArray = readTermAndConditionTextResponseJSON
                    .optJSONArray("termandconditiontext");
            if (!(readTermAndConditionTextJSONArray == null || readTermAndConditionTextJSONArray.length() < 1)) {
                JSONObject currTermAndConditionTextRecord = readTermAndConditionTextJSONArray.getJSONObject(0);
                termAndConditionTextId = currTermAndConditionTextRecord.getString("id");
            }
        }

        return termAndConditionTextId;
    }

    public String getTermAndConditionId(DataControllerRequest requestInstance, String termAndConditionCode,
            Result result) {

        String termAndConditionId = null;
        Map<String, String> inputBodyMap = new HashMap<String, String>();
        inputBodyMap.put(ODataQueryConstants.FILTER, "Code eq '" + termAndConditionCode + "'");
        inputBodyMap.put(ODataQueryConstants.SELECT, "id");
        String readTermAndConditionResponse = Executor.invokeService(ServiceURLEnum.TERMANDCONDITION_READ, inputBodyMap,
                null, requestInstance);
        JSONObject readTermAndConditionResponseJSON = CommonUtilities
                .getStringAsJSONObject(readTermAndConditionResponse);
        if (readTermAndConditionResponseJSON != null && readTermAndConditionResponseJSON.has(FabricConstants.OPSTATUS)
                && readTermAndConditionResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readTermAndConditionResponseJSON.has("termandcondition")) {
            JSONArray readTermAndConditionJSONArray = readTermAndConditionResponseJSON.optJSONArray("termandcondition");
            if (!(readTermAndConditionJSONArray == null || readTermAndConditionJSONArray.length() < 1)) {
                JSONObject currTermAndConditionRecord = readTermAndConditionJSONArray.getJSONObject(0);
                termAndConditionId = currTermAndConditionRecord.getString("id");
            }
        }

        return termAndConditionId;
    }

    public String getNewVersion(DataControllerRequest requestInstance, String termAndConditionId, String languageCode,
            Result result) {
        // Fetching termAndConditionText id from termandconditiontext table
        String latestVersion = null;
        String newVersionId = null;
        Map<String, String> inputBodyMap = new HashMap<String, String>();
        inputBodyMap.put(ODataQueryConstants.FILTER, "TermAndConditionId eq '" + termAndConditionId
                + "' and LanguageCode eq '" + languageCode + "' and Status_id eq '" + TANDC_ACTIVE_STATUS_ID + "'");
        inputBodyMap.put(ODataQueryConstants.SELECT, "Version_Id");
        String readTermAndConditionTextResponse = Executor.invokeService(ServiceURLEnum.TERMANDCONDITIONTEXT_READ,
                inputBodyMap, null, requestInstance);
        JSONObject readTermAndConditionTextResponseJSON = CommonUtilities
                .getStringAsJSONObject(readTermAndConditionTextResponse);
        if (readTermAndConditionTextResponseJSON != null
                && readTermAndConditionTextResponseJSON.has(FabricConstants.OPSTATUS)
                && readTermAndConditionTextResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readTermAndConditionTextResponseJSON.has("termandconditiontext")) {
            JSONArray readTermAndConditionTextJSONArray = readTermAndConditionTextResponseJSON
                    .optJSONArray("termandconditiontext");
            if (readTermAndConditionTextJSONArray == null || readTermAndConditionTextJSONArray.length() < 1) {
                LOG.error("Failed to get Terms and Conditions text");
                ErrorCodeEnum.ERR_20265.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return newVersionId;
            } else {
                JSONObject currTermAndConditionTextRecord = readTermAndConditionTextJSONArray.getJSONObject(0);
                latestVersion = currTermAndConditionTextRecord.getString("Version_Id");
            }
        } else {
            LOG.error("Failed to get Terms and Conditions text");
            ErrorCodeEnum.ERR_20265.setErrorCode(result);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            return newVersionId;
        }
        newVersionId = String.valueOf(round(Double.parseDouble(latestVersion) + 0.1, 1));
        return newVersionId;
    }

    public double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}