package com.kony.adminconsole.service.featuresandactions;

/**
 * Service to Manage Requests related to Feature
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

public class FeatureManageService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(FeatureManageService.class);

    private static final String GET_ALL_FEATURES_METHOD_NAME = "getAllFeatures";
    private static final String EDIT_FEATURE_AND_ACTION_LIMITS_METHOD_NAME = "editFeatureAndActionLimits";
    private static final String GET_ACTIONS_METHOD_NAME = "getFeatureActions";
    
    private static final String MAX_DAILY_LIMIT_PARAM = "DAILY_LIMIT";
    private static final String MAX_TRANSACTION_LIMIT_PARAM = "MAX_TRANSACTION_LIMIT";
    private static final String MIN_TRANSACTION_LIMIT_PARAM = "MIN_TRANSACTION_LIMIT";
    private static final String MAX_WEEKLY_LIMIT_PARAM = "WEEKLY_LIMIT";
    
    private static final String FEATURE_ACTIVE_STATUS = "SID_FEATURE_ACTIVE";
    private static final String FEATURE_INACTIVE_STATUS = "SID_FEATURE_INACTIVE";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            if (StringUtils.equalsIgnoreCase(methodID, GET_ALL_FEATURES_METHOD_NAME)) {
                return getAllFeatures(requestInstance);
            }  else if (StringUtils.equalsIgnoreCase(methodID, GET_ACTIONS_METHOD_NAME)) {
                return getFeatureActions(requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, EDIT_FEATURE_AND_ACTION_LIMITS_METHOD_NAME)) {
                return editFeatureAndActionLimits(requestInstance);
            }
            return null;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }
    
    public Result getFeatureActions(DataControllerRequest requestInstance) {
        Result result = new Result();
        try {
            
            String featureId = requestInstance.getParameter("featureId");
            if (StringUtils.isBlank(featureId)) {
                ErrorCodeEnum.ERR_21350.setErrorCode(result);
                LOG.error("Feature Id cannot be empty");
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Fetch action list from featureaction table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.FILTER, "Feature_id eq '" + featureId + "'");
            inputMap.put(ODataQueryConstants.SELECT, "id, name, isMFAApplicable, Type_id, description, TermsAndConditions_id");

            String readActionResponse = Executor.invokeService(ServiceURLEnum.FEATUREACTION_READ, inputMap, null,
                    requestInstance);

            JSONObject readActionResponseJSON = CommonUtilities.getStringAsJSONObject(readActionResponse);
            if (readActionResponseJSON != null && readActionResponseJSON.has(FabricConstants.OPSTATUS)
                    && readActionResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readActionResponseJSON.has("featureaction")) {
                JSONArray readActionJSONArray = readActionResponseJSON.optJSONArray("featureaction");
                Dataset actionDataset = new Dataset();
                actionDataset.setId("actions");
                if ((readActionJSONArray != null) && (readActionJSONArray.length() > 0)) {
                    for (int indexVar = 0; indexVar < readActionJSONArray.length(); indexVar++) {
                        JSONObject actionJSONObject = readActionJSONArray.getJSONObject(indexVar);
                        Record currRecord = new Record();
                        String actionId = actionJSONObject.optString("id");
                        Param actionId_Param = new Param("actionId", actionId, FabricConstants.STRING);
                        currRecord.addParam(actionId_Param);
                        String actionName = actionJSONObject.optString("name");
                        Param actionName_Param = new Param("actionName", actionName, FabricConstants.STRING);
                        currRecord.addParam(actionName_Param);
                        String isMFAApplicable = actionJSONObject.optString("isMFAApplicable");
                        Param isMFAApplicable_Param = new Param("isMFAApplicable", isMFAApplicable, FabricConstants.STRING);
                        currRecord.addParam(isMFAApplicable_Param);
                        String typeId = actionJSONObject.optString("Type_id");
                        Param typeId_Param = new Param("Type_id", typeId, FabricConstants.STRING);
                        currRecord.addParam(typeId_Param);
                        String description = actionJSONObject.optString("description");
                        Param description_Param = new Param("description", description, FabricConstants.STRING);
                        currRecord.addParam(description_Param);
                        Dataset limits = createLimitRecord(actionId, requestInstance, result);
                        if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                            return result;
                        }
                        currRecord.addDataset(limits);
                        String termsAndConditionsId = actionJSONObject.optString("TermsAndConditions_id");
                        Record termAndConditionRecord = createTermAndConditionRecord(termsAndConditionsId, requestInstance, result);
                        if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                            return result;
                        }
                        currRecord.addRecord(termAndConditionRecord);
                        
                        actionDataset.addRecord(currRecord);
                    }
                    result.addDataset(actionDataset);
                    return result;
                }
            } else {
                result.addParam(new Param("message", readActionResponse, FabricConstants.STRING));
                LOG.error("Failed to Fetch Action Response: " + readActionResponse);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_21310.setErrorCode(result);
                return result;
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in Fetching Action. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_21310.setErrorCode(result);
        }
        return result;
    }
    
    public Result getAllFeatures(DataControllerRequest requestInstance) {
        Result result = new Result();
        try {
        	
        	HashMap<String, String> roleIdNameMap = new HashMap<String, String>();
        	roleIdNameMap = createRoleIdNameMap(requestInstance);

            // Fetch feature list from feature table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.SELECT, "id, name, description, Type_id, Status_id, Service_Fee");

            String readFeatureResponse = Executor.invokeService(ServiceURLEnum.FEATURE_READ, inputMap, null,
                    requestInstance);

            JSONObject readFeatureResponseJSON = CommonUtilities.getStringAsJSONObject(readFeatureResponse);
            if (readFeatureResponseJSON != null && readFeatureResponseJSON.has(FabricConstants.OPSTATUS)
                    && readFeatureResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readFeatureResponseJSON.has("feature")) {
                JSONArray readFeatureJSONArray = readFeatureResponseJSON.optJSONArray("feature");
                Dataset featureDataset = new Dataset();
                featureDataset.setId("features");
                if ((readFeatureJSONArray != null) && (readFeatureJSONArray.length() > 0)) {
                    for (int indexVar = 0; indexVar < readFeatureJSONArray.length(); indexVar++) {
                        JSONObject featureJSONObject = readFeatureJSONArray.getJSONObject(indexVar);
                        Record currRecord = new Record();
                        String featureId = featureJSONObject.optString("id");
                        Param featureId_Param = new Param("id", featureId, FabricConstants.STRING);
                        currRecord.addParam(featureId_Param);
                        String featureName = featureJSONObject.optString("name");
                        Param featureName_Param = new Param("name", featureName, FabricConstants.STRING);
                        currRecord.addParam(featureName_Param);
                        String featureDescription = featureJSONObject.optString("description");
                        Param featureDescription_Param = new Param("description", featureDescription, FabricConstants.STRING);
                        currRecord.addParam(featureDescription_Param);
                        String featureType = featureJSONObject.optString("Type_id");
                        Param featureType_Param = new Param("Type_id", featureType, FabricConstants.STRING);
                        currRecord.addParam(featureType_Param);
                        String featureStatus = featureJSONObject.optString("Status_id");
                        Param featureStatus_Param = new Param("Status_id", featureStatus, FabricConstants.STRING);
                        currRecord.addParam(featureStatus_Param);
                        String serviceFee = featureJSONObject.optString("Service_Fee");
                        Param serviceFee_Param = new Param("Service_Fee", serviceFee, FabricConstants.STRING);
                        currRecord.addParam(serviceFee_Param);
                        Dataset featureTypeDataset = createFeatureTypeDataset(featureId, requestInstance, roleIdNameMap, result);
                        if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                            return result;
                        }
                        currRecord.addDataset(featureTypeDataset);
                        Record featureDisplayNameRecord = createFeatureDisplayNameRecord(featureId, requestInstance, result);
                        if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                            return result;
                        }
                        currRecord.addRecord(featureDisplayNameRecord);
                        
                        featureDataset.addRecord(currRecord);
                    }
                    result.addDataset(featureDataset);
                    return result;
                }
            } else {
                result.addParam(new Param("message", readFeatureResponse, FabricConstants.STRING));
                LOG.error("Failed to Fetch Feature Response: " + readFeatureResponse);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_21352.setErrorCode(result);
                return result;
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in Fetching Feature. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_21352.setErrorCode(result);
        }
        return result;
    }
    
    public Dataset createLimitRecord(String actionId, DataControllerRequest requestInstance, Result result) {

    	Dataset limitDataset = new Dataset();
    	limitDataset.setId("limits");
        
        try {

            // Fetch action limits from actionlimit table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.SELECT, "LimitType_id, value");
            inputMap.put(ODataQueryConstants.FILTER, "Action_id eq '" + actionId + "'");

            String readActionLimitResponse = Executor.invokeService(ServiceURLEnum.ACTIONLIMIT_READ, inputMap, null,
                    requestInstance);

            JSONObject readActionLimitResponseJSON = CommonUtilities.getStringAsJSONObject(readActionLimitResponse);
            if (readActionLimitResponseJSON != null && readActionLimitResponseJSON.has(FabricConstants.OPSTATUS)
                    && readActionLimitResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readActionLimitResponseJSON.has("actionlimit")) {
                JSONArray readActionLimitJSONArray = readActionLimitResponseJSON.optJSONArray("actionlimit");
                if ((readActionLimitJSONArray != null) && (readActionLimitJSONArray.length() > 0)) {
                    for (int indexVar = 0; indexVar < readActionLimitJSONArray.length(); indexVar++) {
                    	Record limitRecord = new Record();
                        JSONObject actionLimitJSONObject = readActionLimitJSONArray.getJSONObject(indexVar);
                        String type = actionLimitJSONObject.optString("LimitType_id");
                        String value = actionLimitJSONObject.optString("value");
                        
                        Param type_Param = new Param("type", type,
                                FabricConstants.STRING);
                        limitRecord.addParam(type_Param);
                        Param value_Param = new Param("value", value,
                                FabricConstants.STRING);
                        limitRecord.addParam(value_Param);
                        limitDataset.addRecord(limitRecord);
                    }
                    return limitDataset;
                }
            } else {
                result.addParam(new Param("message", readActionLimitResponse, FabricConstants.STRING));
                LOG.error("Failed to Fetch Feature limits Response: " + readActionLimitResponse);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_21356.setErrorCode(result);
                return limitDataset;
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in Fetching Feature limits. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_21356.setErrorCode(result);
            return limitDataset;
        }
        return limitDataset;
    }
    
    public Record createTermAndConditionRecord(String termAndConditionId, DataControllerRequest requestInstance, Result result) {

        Record tAndCRecord = new Record();
        tAndCRecord.setId("termandcondition");
        
        try {

            // Fetch term and condition list from termandconditiontext table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.SELECT, "LanguageCode, Content, ContentType_id");
            inputMap.put(ODataQueryConstants.FILTER, "TermAndConditionId eq '" + termAndConditionId + "' and Status_id eq '" + "SID_TANDC_ACTIVE'");

            String readTAndCResponse = Executor.invokeService(ServiceURLEnum.TERMANDCONDITIONTEXT_READ, inputMap, null,
                    requestInstance);

            JSONObject readTandCResponseJSON = CommonUtilities.getStringAsJSONObject(readTAndCResponse);
            if (readTandCResponseJSON != null && readTandCResponseJSON.has(FabricConstants.OPSTATUS)
                    && readTandCResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readTandCResponseJSON.has("termandconditiontext")) {
                JSONArray readTandCJSONArray = readTandCResponseJSON.optJSONArray("termandconditiontext");
                if ((readTandCJSONArray != null) && (readTandCJSONArray.length() > 0)) {
                    for (int indexVar = 0; indexVar < readTandCJSONArray.length(); indexVar++) {
                        JSONObject tAndCJSONObject = readTandCJSONArray.getJSONObject(indexVar);
                        String localeId = tAndCJSONObject.optString("LanguageCode");
                        String content = tAndCJSONObject.optString("Content");
                        String contentType = tAndCJSONObject.optString("ContentType_id");
                        
                        Record contentLangRecord = new Record();
                        contentLangRecord.setId(localeId);
                        Param content_Param = new Param("content", content,
                                FabricConstants.STRING);
                        contentLangRecord.addParam(content_Param);
                        Param contentType_Param = new Param("contentType", contentType,
                                FabricConstants.STRING);
                        contentLangRecord.addParam(contentType_Param);
                        tAndCRecord.addRecord(contentLangRecord);
                    }
                    return tAndCRecord;
                }
            } else {
                result.addParam(new Param("message", readTAndCResponse, FabricConstants.STRING));
                LOG.error("Failed to Fetch action term and condition Response: " + readTAndCResponse);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_21357.setErrorCode(result);
                return tAndCRecord;
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in Fetching action term and condition. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_21357.setErrorCode(result);
            return tAndCRecord;
        }
        return tAndCRecord;
    }
    
    public Record createFeatureDisplayNameRecord(String featureId, DataControllerRequest requestInstance, Result result) {

        Record displayRecord = new Record();
        displayRecord.setId("featureDisplayName");
        
        try {

            // Fetch feature display name list from featuredisplaynamedescription table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.SELECT, "Locale_id, DisplayName, DisplayDescription");
            inputMap.put(ODataQueryConstants.FILTER, "Feature_id eq '" + featureId + "'");

            String readFeatureDisplayResponse = Executor.invokeService(ServiceURLEnum.FEATUREDISPLAYNAMEDESCRIPTION_READ, inputMap, null,
                    requestInstance);

            JSONObject readFeatureDisplayResponseJSON = CommonUtilities.getStringAsJSONObject(readFeatureDisplayResponse);
            if (readFeatureDisplayResponseJSON != null && readFeatureDisplayResponseJSON.has(FabricConstants.OPSTATUS)
                    && readFeatureDisplayResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readFeatureDisplayResponseJSON.has("featuredisplaynamedescription")) {
                JSONArray readFeatureDisplayJSONArray = readFeatureDisplayResponseJSON.optJSONArray("featuredisplaynamedescription");
                if ((readFeatureDisplayJSONArray != null) && (readFeatureDisplayJSONArray.length() > 0)) {
                    for (int indexVar = 0; indexVar < readFeatureDisplayJSONArray.length(); indexVar++) {
                        JSONObject featureDisplayJSONObject = readFeatureDisplayJSONArray.getJSONObject(indexVar);
                        String localeId = featureDisplayJSONObject.optString("Locale_id");
                        String displayName = featureDisplayJSONObject.optString("DisplayName");
                        String displayDescription = featureDisplayJSONObject.optString("DisplayDescription");
                        
                        Record contentLangRecord = new Record();
                        contentLangRecord.setId(localeId);
                        Param displayName_Param = new Param("displayName", displayName,
                                FabricConstants.STRING);
                        contentLangRecord.addParam(displayName_Param);
                        Param displayDescription_Param = new Param("displayDescription", displayDescription,
                                FabricConstants.STRING);
                        contentLangRecord.addParam(displayDescription_Param);
                        displayRecord.addRecord(contentLangRecord);
                    }
                    return displayRecord;
                }
            } else {
                result.addParam(new Param("message", readFeatureDisplayResponse, FabricConstants.STRING));
                LOG.error("Failed to Fetch Feature Display Response: " + readFeatureDisplayResponse);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_21355.setErrorCode(result);
                return displayRecord;
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in Fetching Feature Display. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_21355.setErrorCode(result);
            return displayRecord;
        }
        return displayRecord;
    }
    
    HashMap<String, String> createRoleIdNameMap(DataControllerRequest requestInstance) {
    	
    	HashMap<String, String> roleIdNameMap = new HashMap<String, String>();
    	
    	try {

            // Fetch Role type list from membergrouptype table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.SELECT, "id, description");

            String readMemberGroupTypeResponse = Executor.invokeService(ServiceURLEnum.MEMBERGROUPTYPE_READ, inputMap, null,
                    requestInstance);

            JSONObject readMemberGroupTypeResponseJSON = CommonUtilities.getStringAsJSONObject(readMemberGroupTypeResponse);
            if (readMemberGroupTypeResponseJSON != null && readMemberGroupTypeResponseJSON.has(FabricConstants.OPSTATUS)
                    && readMemberGroupTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readMemberGroupTypeResponseJSON.has("membergrouptype")) {
                JSONArray readMemberGroupTypeJSONArray = readMemberGroupTypeResponseJSON.optJSONArray("membergrouptype");
                if ((readMemberGroupTypeJSONArray != null) && (readMemberGroupTypeJSONArray.length() > 0)) {
                    for (int indexVar = 0; indexVar < readMemberGroupTypeJSONArray.length(); indexVar++) {
                        JSONObject memberGroupTypeJSONObject = readMemberGroupTypeJSONArray.getJSONObject(indexVar);
                        String featureTypeId = memberGroupTypeJSONObject.optString("id");
                        String featureTypeName = memberGroupTypeJSONObject.optString("description");
                        roleIdNameMap.put(featureTypeId, featureTypeName);
                    }
                }
            }
    	} catch (Exception e) {
            LOG.error("Unexepected Error in Fetching Member Group Type. Exception: ", e);
        }
    	return roleIdNameMap;
    }
    
    public Dataset createFeatureTypeDataset(String featureId, DataControllerRequest requestInstance, HashMap<String, String> roleIdNameMap, Result result) {

        Dataset featureTypeDataset = new Dataset();
        featureTypeDataset.setId("roleTypes");
        try {

            // Fetch Role type list from featureroletype table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.SELECT, "RoleType_id");
            inputMap.put(ODataQueryConstants.FILTER,
                    "Feature_id eq '" + featureId + "'");

            String readFeatureTypeResponse = Executor.invokeService(ServiceURLEnum.FEATUREROLETYPE_READ, inputMap, null,
                    requestInstance);

            JSONObject readFeatureTypeResponseJSON = CommonUtilities.getStringAsJSONObject(readFeatureTypeResponse);
            if (readFeatureTypeResponseJSON != null && readFeatureTypeResponseJSON.has(FabricConstants.OPSTATUS)
                    && readFeatureTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readFeatureTypeResponseJSON.has("featureroletype")) {
                JSONArray readFeatureTypeJSONArray = readFeatureTypeResponseJSON.optJSONArray("featureroletype");
                if ((readFeatureTypeJSONArray != null) && (readFeatureTypeJSONArray.length() > 0)) {
                    for (int indexVar = 0; indexVar < readFeatureTypeJSONArray.length(); indexVar++) {
                        JSONObject featureTypeJSONObject = readFeatureTypeJSONArray.getJSONObject(indexVar);
                        Record currRecord = new Record();
                        String featureTypeId = featureTypeJSONObject.optString("RoleType_id");
                        Param featureTypeId_Param = new Param("id", featureTypeId, FabricConstants.STRING);
                        currRecord.addParam(featureTypeId_Param);
                        Param featureTypeName_Param = new Param("name", roleIdNameMap.get(featureTypeId), FabricConstants.STRING);
                        currRecord.addParam(featureTypeName_Param);
                        featureTypeDataset.addRecord(currRecord);
                    }
                    return featureTypeDataset;
                }
            } else {
                result.addParam(new Param("message", readFeatureTypeResponse, FabricConstants.STRING));
                LOG.error("Failed to Fetch Feature Type Response: " + readFeatureTypeResponse);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_21354.setErrorCode(result);
                return featureTypeDataset;
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in Fetching Feature Type. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_21354.setErrorCode(result);
            return featureTypeDataset;
        }
        return featureTypeDataset;
    }
    
    public Result editFeatureAndActionLimits(DataControllerRequest requestInstance) {

        Result result = new Result();

        try {
            
        	String loggedInUserId = null;
            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            if (loggedInUserDetails != null) {
                loggedInUserId = loggedInUserDetails.getUserId();
            }

            // Validate FeatureId
            String featureId = requestInstance.getParameter("featureId");
            if (StringUtils.isBlank(featureId)) {
            	LOG.error("Feature Id is a mandatory input");
            	ErrorCodeEnum.ERR_21350.setErrorCode(result);
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	return result;
            }

            // Validate ServiceFee
            String serviceFee = requestInstance.getParameter("serviceFee");
            if (StringUtils.isBlank(serviceFee)) {
            	LOG.error("Service fee is a mandatory input");
            	ErrorCodeEnum.ERR_21358.setErrorCode(result);
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	return result;
            }
            
            if(!isDecimalNumber(serviceFee)) {
            	LOG.error("Service fee should be decimal");
            	ErrorCodeEnum.ERR_21368.setErrorCode(result);
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	return result;
            }

            // Validate StatusId
            String statusId = requestInstance.getParameter("statusId");
            if (StringUtils.isBlank(statusId)) {
                LOG.error("Status id cannot be empty");
                ErrorCodeEnum.ERR_20186.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }
            
            if(!(StringUtils.equals(statusId, FEATURE_ACTIVE_STATUS) || StringUtils.equals(statusId, FEATURE_INACTIVE_STATUS))) {
            	LOG.error("Status id can be active or inactive");
                ErrorCodeEnum.ERR_21369.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Validate LocaleId
            String localeId = requestInstance.getParameter("localeId");
            if (StringUtils.isBlank(localeId)) {
            	LOG.error("Locale id cannot be empty");
            	ErrorCodeEnum.ERR_21109.setErrorCode(result);
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	return result;
            }

            // Validate DisplayName
            String displayName = requestInstance.getParameter("displayName");
            if (StringUtils.isBlank(displayName)) {
            	LOG.error("Feature display name cannot be empty");
            	ErrorCodeEnum.ERR_21359.setErrorCode(result);
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	return result;
            }

            // Validate DisplayDescription
            String displayDescription = requestInstance.getParameter("displayDescription");
            if (StringUtils.isBlank(displayDescription)) {
            	LOG.error("Feature display description cannot be empty");
            	ErrorCodeEnum.ERR_21360.setErrorCode(result);
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	return result;
            }

            // Validate MinValPerTrans
            String minValPerTrans = requestInstance.getParameter("minValPerTrans");
            if (StringUtils.isBlank(minValPerTrans)) {
            	LOG.error("Minimum value per transaction cannot be empty");
            	ErrorCodeEnum.ERR_21361.setErrorCode(result);
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	return result;
            }
            
            if(!isDecimalNumber(minValPerTrans)) {
            	LOG.error("Minimum value per transaction should be decimal");
            	ErrorCodeEnum.ERR_21371.setErrorCode(result);
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	return result;
            }

            // Validate MaxTransLimit
            String maxTransLimit = requestInstance.getParameter("maxTransLimit");
            if (StringUtils.isBlank(maxTransLimit)) {
            	LOG.error("Maximum transaction limit cannot be empty");
            	ErrorCodeEnum.ERR_21362.setErrorCode(result);
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	return result;
            }
            
            if(!isDecimalNumber(maxTransLimit)) {
            	LOG.error("Maximum transaction limit should be decimal");
            	ErrorCodeEnum.ERR_21372.setErrorCode(result);
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	return result;
            }

            // Validate MaxDailyLimit
            String maxDailyLimit = requestInstance.getParameter("maxDailyLimit");
            if (StringUtils.isBlank(maxDailyLimit)) {
            	LOG.error("Maximum daily limit cannot be empty");
            	ErrorCodeEnum.ERR_21363.setErrorCode(result);
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	return result;
            }
            
            if(!isDecimalNumber(maxDailyLimit)) {
            	LOG.error("Maximum daily limit should be decimal");
            	ErrorCodeEnum.ERR_21373.setErrorCode(result);
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	return result;
            }

            // Validate MaxWeeklyLimit
            String maxWeeklyLimit = requestInstance.getParameter("maxWeeklyLimit");
            if (StringUtils.isBlank(maxWeeklyLimit)) {
            	LOG.error("Maximum weekly limit cannot be empty");
            	ErrorCodeEnum.ERR_21364.setErrorCode(result);
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	return result;
            }
            
            if(!isDecimalNumber(maxWeeklyLimit)) {
            	LOG.error("Maximum weekly limit should be decimal");
            	ErrorCodeEnum.ERR_21370.setErrorCode(result);
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	return result;
            }

            // Validate ActionId
            String actionId = requestInstance.getParameter("actionId");
            if (StringUtils.isBlank(actionId)) {
            	LOG.error("Action Id is mandatory input");
            	ErrorCodeEnum.ERR_20866.setErrorCode(result);
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	return result;
            }
            
            //Updating statusId and serviceFee in feature table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put("id", featureId);
            inputMap.put("Status_id", statusId);
            inputMap.put("Service_Fee", serviceFee);
            inputMap.put("modifiedby", loggedInUserDetails.getUserName());
            inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

            String editFeatureResponse = Executor.invokeService(ServiceURLEnum.FEATURE_UPDATE, inputMap,
            		null, requestInstance);

            JSONObject editFeatureResponseJSON = CommonUtilities
            		.getStringAsJSONObject(editFeatureResponse);
            if ((editFeatureResponseJSON != null) && editFeatureResponseJSON.has(FabricConstants.OPSTATUS)
            		&& editFeatureResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            	LOG.debug("Feature updated successfully.");
            } else {
            	LOG.error("Failed to edit Feature");
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	ErrorCodeEnum.ERR_21365.setErrorCode(result);
            	return result;
            }

            //Updating displayName and displayDescription in feature table
            inputMap.clear();
            inputMap.put(ODataQueryConstants.SELECT, "Feature_id");
            inputMap.put(ODataQueryConstants.FILTER, "Feature_id eq '" + featureId + "' and Locale_id eq '" + localeId + "'");
            String readFeatureDisplayResponse = Executor.invokeService(ServiceURLEnum.FEATUREDISPLAYNAMEDESCRIPTION_READ, inputMap,
            		null, requestInstance);
            
            inputMap.clear();
            inputMap.put("Feature_id", featureId);
            inputMap.put("Locale_id", localeId);
            inputMap.put("DisplayName", displayName);
            inputMap.put("DisplayDescription", displayDescription);
            JSONObject readFeatureDisplayResponseJSON = CommonUtilities.getStringAsJSONObject(readFeatureDisplayResponse);
            if (readFeatureDisplayResponseJSON != null && readFeatureDisplayResponseJSON.has(FabricConstants.OPSTATUS)
                    && readFeatureDisplayResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readFeatureDisplayResponseJSON.has("featuredisplaynamedescription")) {
                JSONArray readFeatureTypeJSONArray = readFeatureDisplayResponseJSON.optJSONArray("featuredisplaynamedescription");
                if (!((readFeatureTypeJSONArray != null) && (readFeatureTypeJSONArray.length() > 0))) {
                	inputMap.put("createdby", loggedInUserId);
                    inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
                    String createFeatureDisplayResponse = Executor.invokeService(ServiceURLEnum.FEATUREDISPLAYNAMEDESCRIPTION_CREATE, inputMap,
                            null, requestInstance);

                    JSONObject createFeatureDisplayResponseJSON = CommonUtilities.getStringAsJSONObject(createFeatureDisplayResponse);
                    if ((createFeatureDisplayResponseJSON != null) && createFeatureDisplayResponseJSON.has(FabricConstants.OPSTATUS)
                    		&& createFeatureDisplayResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                    	LOG.debug("Feature display created successfully.");
                    } else {
                    	LOG.error("Failed to created Feature display");
                    	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    	ErrorCodeEnum.ERR_21374.setErrorCode(result);
                    	return result;
                    }
                } else {
                	inputMap.put("modifiedby", loggedInUserDetails.getUserName());
                    inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

                    String editFeatureDisplayResponse = Executor.invokeService(ServiceURLEnum.FEATUREDISPLAYNAMEDESCRIPTION_UPDATE, inputMap,
                    		null, requestInstance);

                    JSONObject editFeatureDisplayResponseJSON = CommonUtilities
                    		.getStringAsJSONObject(editFeatureDisplayResponse);
                    if ((editFeatureDisplayResponseJSON != null) && editFeatureDisplayResponseJSON.has(FabricConstants.OPSTATUS)
                    		&& editFeatureDisplayResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                    	LOG.debug("Feature display updated successfully.");
                    } else {
                    	LOG.error("Failed to edit Feature display");
                    	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    	ErrorCodeEnum.ERR_21366.setErrorCode(result);
                    	return result;
                    }
                }
            }

            //Updating minValPerTrans in actionlimit table
            inputMap.clear();
            inputMap.put("Action_id", actionId);
            inputMap.put("LimitType_id", MIN_TRANSACTION_LIMIT_PARAM);
            inputMap.put("value", minValPerTrans);
            inputMap.put("modifiedby", loggedInUserDetails.getUserName());
            inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

            String editActionLimitMinTransResponse = Executor.invokeService(ServiceURLEnum.ACTIONLIMIT_UPDATE, inputMap,
            		null, requestInstance);

            JSONObject editActionLimitMinTransResponseJSON = CommonUtilities
            		.getStringAsJSONObject(editActionLimitMinTransResponse);
            if ((editActionLimitMinTransResponseJSON != null) && editActionLimitMinTransResponseJSON.has(FabricConstants.OPSTATUS)
            		&& editActionLimitMinTransResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            	LOG.debug("Action limit updated successfully.");
            } else {
            	LOG.error("Failed to edit action limit");
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	ErrorCodeEnum.ERR_21366.setErrorCode(result);
            	return result;
            }
            
            //Updating maxTransLimit in actionlimit table
            inputMap.clear();
            inputMap.put("Action_id", actionId);
            inputMap.put("LimitType_id", MAX_TRANSACTION_LIMIT_PARAM);
            inputMap.put("value", maxTransLimit);
            inputMap.put("modifiedby", loggedInUserDetails.getUserName());
            inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

            String editActionLimitMaxTransResponse = Executor.invokeService(ServiceURLEnum.ACTIONLIMIT_UPDATE, inputMap,
            		null, requestInstance);

            JSONObject editActionLimitMaxTransResponseJSON = CommonUtilities
            		.getStringAsJSONObject(editActionLimitMaxTransResponse);
            if ((editActionLimitMaxTransResponseJSON != null) && editActionLimitMaxTransResponseJSON.has(FabricConstants.OPSTATUS)
            		&& editActionLimitMaxTransResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            	LOG.debug("Action limit updated successfully.");
            } else {
            	LOG.error("Failed to edit action limit");
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	ErrorCodeEnum.ERR_21366.setErrorCode(result);
            	return result;
            }

            //Updating maxDailyLimit in actionlimit table
            inputMap.clear();
            inputMap.put("Action_id", actionId);
            inputMap.put("LimitType_id", MAX_DAILY_LIMIT_PARAM);
            inputMap.put("value", maxDailyLimit);
            inputMap.put("modifiedby", loggedInUserDetails.getUserName());
            inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

            String editActionLimitMaxDailyResponse = Executor.invokeService(ServiceURLEnum.ACTIONLIMIT_UPDATE, inputMap,
            		null, requestInstance);

            JSONObject editActionLimitMaxDailyResponseJSON = CommonUtilities
            		.getStringAsJSONObject(editActionLimitMaxDailyResponse);
            if ((editActionLimitMaxDailyResponseJSON != null) && editActionLimitMaxDailyResponseJSON.has(FabricConstants.OPSTATUS)
            		&& editActionLimitMaxDailyResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            	LOG.debug("Action limit updated successfully.");
            } else {
            	LOG.error("Failed to edit action limit");
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	ErrorCodeEnum.ERR_21366.setErrorCode(result);
            	return result;
            }
            
            //Updating maxWeeklyLimit in actionlimit table
            inputMap.clear();
            inputMap.put("Action_id", actionId);
            inputMap.put("LimitType_id", MAX_WEEKLY_LIMIT_PARAM);
            inputMap.put("value", maxDailyLimit);
            inputMap.put("modifiedby", loggedInUserDetails.getUserName());
            inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

            String editActionLimitMaxWeeklyResponse = Executor.invokeService(ServiceURLEnum.ACTIONLIMIT_UPDATE, inputMap,
            		null, requestInstance);

            JSONObject editActionLimitMaxWeeklyResponseJSON = CommonUtilities
            		.getStringAsJSONObject(editActionLimitMaxWeeklyResponse);
            if ((editActionLimitMaxWeeklyResponseJSON != null) && editActionLimitMaxWeeklyResponseJSON.has(FabricConstants.OPSTATUS)
            		&& editActionLimitMaxWeeklyResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            	LOG.debug("Action limit updated successfully.");
            } else {
            	LOG.error("Failed to edit action limit");
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	ErrorCodeEnum.ERR_21366.setErrorCode(result);
            	return result;
            }

            result.addParam(new Param("status", "Success", FabricConstants.STRING));

        } catch (Exception e) {
            LOG.error("Unexepected Error in edit feature flow. Exception: ", e);
            ErrorCodeEnum.ERR_21365.setErrorCode(result);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
        }

        return result;
    }
    
    public static boolean isDecimalNumber(String s) {
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}