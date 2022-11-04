package com.kony.adminconsole.service.mfa;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to Manage Requests related to MFAScenario
 * 
 * @author Chandan Gupta - KH2516
 *
 */

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

public class MFAScenarioManageService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(MFAScenarioManageService.class);

    private static final String GET_MFA_MODE_METHOD_NAME = "getMFAMode";
    private static final String GET_MFA_TYPE_METHOD_NAME = "getMFAType";
    private static final String GET_FREQUENCY_TYPE_METHOD_NAME = "getFrequencyType";
    private static final String GET_ACTION_METHOD_NAME = "getAction";
    private static final String CREATE_MFA_SCENARIO_METHOD_NAME = "createMFAScenario";
    private static final String EDIT_MFA_SCENARIO_METHOD_NAME = "editMFAScenario";
    private static final String DELETE_MFA_SCENARIO_METHOD_NAME = "deleteMFAScenario";
    private static final String GET_MFA_SCENARIO_METHOD_NAME = "getMFAScenario";
    private static final String GET_MFA_VARIABLE_REFERENCE_METHOD_NAME = "getMFAVariableReference";
    private static final String GET_MFA_FEATURE_METHOD_NAME = "getMFAFeature";

    private static final String DEFAULT_FREQUENCY_TYPE_ID = "ALWAYS";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {

            if (StringUtils.equalsIgnoreCase(methodID, GET_MFA_MODE_METHOD_NAME)) {
                return getMFAMode(requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, GET_MFA_TYPE_METHOD_NAME)) {
                return getMFAType(requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, GET_FREQUENCY_TYPE_METHOD_NAME)) {
                return getFrequencyType(requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, GET_ACTION_METHOD_NAME)) {
                return getAction(requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, CREATE_MFA_SCENARIO_METHOD_NAME)) {
                return createOrEditMFAScenario(requestInstance, CREATE_MFA_SCENARIO_METHOD_NAME);
            } else if (StringUtils.equalsIgnoreCase(methodID, EDIT_MFA_SCENARIO_METHOD_NAME)) {
                return createOrEditMFAScenario(requestInstance, EDIT_MFA_SCENARIO_METHOD_NAME);
            } else if (StringUtils.equalsIgnoreCase(methodID, DELETE_MFA_SCENARIO_METHOD_NAME)) {
                return deleteMFAScenario(requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, GET_MFA_SCENARIO_METHOD_NAME)) {
                return getMFAScenario(requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, GET_MFA_VARIABLE_REFERENCE_METHOD_NAME)) {
                return getMFAVariableReference(requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, GET_MFA_FEATURE_METHOD_NAME))
                return getMFAFeature(requestInstance);

            return null;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    public Result getMFAMode(DataControllerRequest requestInstance) {
        Result result = new Result();
        try {

            // Validate ActionId
            String actionId = requestInstance.getParameter("actionId");
            if (StringUtils.isBlank(actionId)) {
                ErrorCodeEnum.ERR_20866.setErrorCode(result);
                LOG.error("Action Id is a mandatory input");
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Fetch MFAId from featureaction table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.FILTER, "id eq '" + actionId + "'");
            inputMap.put(ODataQueryConstants.SELECT, "MFA_id, isMFAApplicable, Feature_id");

            String readFeatureActionResponse = Executor.invokeService(ServiceURLEnum.FEATUREACTION_READ, inputMap, null,
                    requestInstance);

            String mfaId = null;
            String isMFAApplicable = null;
            String featureId = null;

            JSONObject readFeatureActionResponseJSON = CommonUtilities.getStringAsJSONObject(readFeatureActionResponse);
            if (readFeatureActionResponseJSON != null && readFeatureActionResponseJSON.has(FabricConstants.OPSTATUS)
                    && readFeatureActionResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readFeatureActionResponseJSON.has("featureaction")) {
                JSONArray readFeatureActionJSONArray = readFeatureActionResponseJSON.optJSONArray("featureaction");
                if (readFeatureActionJSONArray == null || readFeatureActionJSONArray.length() < 1) {
                    ErrorCodeEnum.ERR_21334.setErrorCode(result);
                    LOG.error("Invalid FeatureActionId");
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                } else {
                    JSONObject currFeatureActionRecord = readFeatureActionJSONArray.getJSONObject(0);
                    mfaId = currFeatureActionRecord.optString("MFA_id");
                    isMFAApplicable = currFeatureActionRecord.getString("isMFAApplicable");
                    featureId = currFeatureActionRecord.getString("Feature_id");
                }
            } else {
                ErrorCodeEnum.ERR_21334.setErrorCode(result);
                LOG.error("Invalid FeatureActionId");
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }
            
            if(StringUtils.equals(isMFAApplicable, "false")) {
            	ErrorCodeEnum.ERR_21349.setErrorCode(result);
                LOG.error("MFA is not applicable");
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }
            
            // Fetch FeatureStatus from feature table
            inputMap.clear();
            inputMap.put(ODataQueryConstants.FILTER, "id eq '" + featureId + "'");
            inputMap.put(ODataQueryConstants.SELECT, "Status_id");

            String readFeatureResponse = Executor.invokeService(ServiceURLEnum.FEATURE_READ, inputMap, null,
            		requestInstance);

            String featureStatus = null;

            JSONObject readFeatureResponseJSON = CommonUtilities.getStringAsJSONObject(readFeatureResponse);
            if (readFeatureResponseJSON != null && readFeatureResponseJSON.has(FabricConstants.OPSTATUS)
            		&& readFeatureResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
            		&& readFeatureResponseJSON.has("feature")) {
            	JSONArray readFeatureJSONArray = readFeatureResponseJSON.optJSONArray("feature");
            	if (readFeatureJSONArray == null || readFeatureJSONArray.length() < 1) {
            		ErrorCodeEnum.ERR_21352.setErrorCode(result);
            		LOG.error("Failed to fetch feature");
            		result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            		return result;
            	} else {
            		JSONObject currFeatureRecord = readFeatureJSONArray.getJSONObject(0);
            		featureStatus = currFeatureRecord.optString("Status_id");
            		if(!StringUtils.equals(featureStatus, "SID_FEATURE_ACTIVE")) {
            			ErrorCodeEnum.ERR_21353.setErrorCode(result);
                		LOG.error("Feature is not active");
                		result.addParam(new Param("status", "Feature is not active", FabricConstants.STRING));
                		return result;
            		}
            	}
            } else {
            	ErrorCodeEnum.ERR_21352.setErrorCode(result);
        		LOG.error("Failed to fetch feature");
        		result.addParam(new Param("status", "Failure", FabricConstants.STRING));
        		return result;
            }

            // Fetch mfa scenarios from mfa table
            inputMap.clear();
            inputMap.put(ODataQueryConstants.FILTER, "id eq '" + mfaId + "'");
            inputMap.put(ODataQueryConstants.SELECT,
                    "Status_id, FrequencyType_id, FrequencyValue, PrimaryMFAType, SecondaryMFAType, SMSText, EmailSubject, EmailBody");

            String readMFAResponse = Executor.invokeService(ServiceURLEnum.MFA_READ, inputMap, null,
                    requestInstance);

            String isMFARequired = null;
            String frequencyTypeId = null;
            String frequencyValue = null;
            String primaryMFATypeId = null;
            String secondaryMFATypeId = null;
            String smsText = null;
            String emailSubject = null;
            String emailBody = null;

            JSONObject readMFAResponseJSON = CommonUtilities.getStringAsJSONObject(readMFAResponse);
            if (readMFAResponseJSON != null && readMFAResponseJSON.has(FabricConstants.OPSTATUS)
                    && readMFAResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readMFAResponseJSON.has("mfa")) {
                JSONArray readMFAJSONArray = readMFAResponseJSON.optJSONArray("mfa");
                if (readMFAJSONArray == null || readMFAJSONArray.length() < 1) {
                    ErrorCodeEnum.ERR_21341.setErrorCode(result);
                    LOG.error("Failed to fetch MFA Scenario");
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                } else {
                    JSONObject currAppActionRecord = readMFAJSONArray.getJSONObject(0);
                    String statusId = currAppActionRecord.optString("Status_id");
                    if (statusId.equals("SID_ACTIVE"))
                        isMFARequired = "true";
                    else
                        isMFARequired = "false";
                    frequencyTypeId = currAppActionRecord.optString("FrequencyType_id");
                    frequencyValue = currAppActionRecord.optString("FrequencyValue");
                    primaryMFATypeId = currAppActionRecord.optString("PrimaryMFAType");
                    secondaryMFATypeId = currAppActionRecord.optString("SecondaryMFAType");
                    smsText = currAppActionRecord.optString("SMSText");
                    emailSubject = currAppActionRecord.optString("EmailSubject");
                    emailBody = currAppActionRecord.optString("EmailBody");
                }
            } else {
                LOG.error("MFA Scenario is empty");
                result.addParam(new Param("status", "Success", FabricConstants.STRING));
                return result;
            }

            Param isMFARequired_Param = new Param("isMFARequired", isMFARequired, FabricConstants.STRING);
            result.addParam(isMFARequired_Param);
            Param frequencyTypeId_Param = new Param("frequencyTypeId", frequencyTypeId, FabricConstants.STRING);
            result.addParam(frequencyTypeId_Param);
            Param frequencyValue_Param = new Param("frequencyValue", frequencyValue, FabricConstants.STRING);
            result.addParam(frequencyValue_Param);
            Param primaryMFATypeId_Param = new Param("primaryMFATypeId", primaryMFATypeId, FabricConstants.STRING);
            result.addParam(primaryMFATypeId_Param);
            Param secondaryMFATypeId_Param = new Param("secondaryMFATypeId", secondaryMFATypeId,
                    FabricConstants.STRING);
            result.addParam(secondaryMFATypeId_Param);

            // Fetch mfaTypeIds from mfatype table
            inputMap.clear();
            inputMap.put(ODataQueryConstants.FILTER,
                    "id eq '" + primaryMFATypeId + "' or id eq '" + secondaryMFATypeId + "'");
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
                if ((readMFATypeJSONArray == null) || (readMFATypeJSONArray.length() < 2)) {
                    ErrorCodeEnum.ERR_21340.setErrorCode(result);
                    LOG.error("Primary mfa type and secondary mfa type is invalid");
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                } else {
                    for (int indexVar = 0; indexVar < readMFATypeJSONArray.length(); indexVar++) {
                        JSONObject mfaTypeJSONObject = readMFATypeJSONArray.getJSONObject(indexVar);
                        Record currRecord = new Record();
                        String mfaTypeId = mfaTypeJSONObject.getString("id");
                        Param mfaTypeId_Param = new Param("mfaTypeId", mfaTypeId, FabricConstants.STRING);
                        currRecord.addParam(mfaTypeId_Param);
                        String mfaTypeName = mfaTypeJSONObject.getString("Name");
                        Param mfaTypeName_Param = new Param("mfaTypeName", mfaTypeName, FabricConstants.STRING);
                        currRecord.addParam(mfaTypeName_Param);

                        // Adding MFA Scenarios fields if mfaType is Secure Access Code
                        if (mfaTypeId.equals("SECURE_ACCESS_CODE")) {
                            Param smsText_Param = new Param("smsText", smsText, FabricConstants.STRING);
                            currRecord.addParam(smsText_Param);
                            Param emailSubject_Param = new Param("emailSubject", emailSubject, FabricConstants.STRING);
                            currRecord.addParam(emailSubject_Param);
                            Param emailBody_Param = new Param("emailBody", emailBody, FabricConstants.STRING);
                            currRecord.addParam(emailBody_Param);
                        }

                        // Fetching configurations
                        inputMap.clear();
                        inputMap.put(ODataQueryConstants.FILTER,
                                "MFA_id eq '" + mfaTypeJSONObject.getString("id") + "'");
                        inputMap.put(ODataQueryConstants.SELECT, "MFAKey_id, value");

                        String readMFAConfigResponse = Executor.invokeService(ServiceURLEnum.MFACONFIGURATIONS_READ,
                                inputMap, null, requestInstance);

                        Dataset mfaConfigurationsDataset = new Dataset();
                        mfaConfigurationsDataset.setId("mfaConfigurations");

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
                                    Param mfaKeyId_Param = new Param("mfaKey", mfaKeyId, FabricConstants.STRING);
                                    mfaKeyRecord.addParam(mfaKeyId_Param);
                                    String mfaKeyValue = mfaConfigJSONObject.optString("value");
                                    Param mfaKeyValue_Param = new Param("mfaValue", mfaKeyValue,
                                            FabricConstants.STRING);
                                    mfaKeyRecord.addParam(mfaKeyValue_Param);
                                    mfaConfigurationsDataset.addRecord(mfaKeyRecord);
                                }
                            }
                        } else {
                            result.addParam(new Param("message", readMFAConfigResponse, FabricConstants.STRING));
                            LOG.error("Failed to Fetch MFAConfig Response: " + readMFAConfigResponse);
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            ErrorCodeEnum.ERR_21304.setErrorCode(result);
                            return result;
                        }

                        currRecord.addDataset(mfaConfigurationsDataset);
                        mfaTypeDataset.addRecord(currRecord);
                    }
                    result.addDataset(mfaTypeDataset);
                    return result;
                }
            } else {
                ErrorCodeEnum.ERR_21340.setErrorCode(result);
                LOG.error("Primary mfa type and secondary mfa type is invalid");
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in Fetching mfaConfiguration and Scenario. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_21342.setErrorCode(result);
        }
        return result;
    }

    public Result getMFAType(DataControllerRequest requestInstance) {
        Result result = new Result();
        try {

            // Fetch mfaType from mfatype table
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
                        mfaTypeDataset.addRecord(currRecord);
                    }
                    result.addDataset(mfaTypeDataset);
                    return result;
                }
            } else {
                result.addParam(new Param("message", readMFATypeResponse, FabricConstants.STRING));
                LOG.error("Failed to Fetch MFA Type Response: " + readMFATypeResponse);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_21305.setErrorCode(result);
                return result;
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in Fetching MFA Type. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_21305.setErrorCode(result);
        }
        return result;
    }

    public Result getFrequencyType(DataControllerRequest requestInstance) {
        Result result = new Result();
        try {

            // Fetch frequencytype from frequencytype table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.SELECT, "id, Description");

            String readFrequencyTypeResponse = Executor.invokeService(ServiceURLEnum.FREQUENCYTYPE_READ, inputMap, null,
                    requestInstance);

            JSONObject readFrequencyTypeResponseJSON = CommonUtilities.getStringAsJSONObject(readFrequencyTypeResponse);
            if (readFrequencyTypeResponseJSON != null && readFrequencyTypeResponseJSON.has(FabricConstants.OPSTATUS)
                    && readFrequencyTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readFrequencyTypeResponseJSON.has("frequencytype")) {
                JSONArray readFrequencyTypeJSONArray = readFrequencyTypeResponseJSON.optJSONArray("frequencytype");
                Dataset frequencyTypeDataset = new Dataset();
                frequencyTypeDataset.setId("frequencyTypes");
                if ((readFrequencyTypeJSONArray != null) && (readFrequencyTypeJSONArray.length() > 0)) {
                    for (int indexVar = 0; indexVar < readFrequencyTypeJSONArray.length(); indexVar++) {
                        JSONObject frequencyTypeJSONObject = readFrequencyTypeJSONArray.getJSONObject(indexVar);
                        Record currRecord = new Record();
                        String frequencyTypeId = frequencyTypeJSONObject.getString("id");
                        Param frequencyTypeId_Param = new Param("frequencyTypeId", frequencyTypeId,
                                FabricConstants.STRING);
                        currRecord.addParam(frequencyTypeId_Param);
                        String frequencyTypeName = frequencyTypeJSONObject.getString("Description");
                        Param frequencyTypeName_Param = new Param("frequencyTypeName", frequencyTypeName,
                                FabricConstants.STRING);
                        currRecord.addParam(frequencyTypeName_Param);
                        frequencyTypeDataset.addRecord(currRecord);
                    }
                    result.addDataset(frequencyTypeDataset);
                    return result;
                }
            } else {
                result.addParam(new Param("message", readFrequencyTypeResponse, FabricConstants.STRING));
                LOG.error("Failed to Fetch Frequency Type Response: " + readFrequencyTypeResponse);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_21309.setErrorCode(result);
                return result;
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in Fetching Frequency Type. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_21309.setErrorCode(result);
        }
        return result;
    }

    public Result getAction(DataControllerRequest requestInstance) {
        Result result = new Result();
        try {

            String actionType = requestInstance.getParameter("actionType");
            if (StringUtils.isBlank(actionType)) {
                ErrorCodeEnum.ERR_20783.setErrorCode(result);
                LOG.error("Action type cannot be empty");
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }
            
            String featureId = requestInstance.getParameter("featureId");
            if (StringUtils.isBlank(featureId)) {
                ErrorCodeEnum.ERR_21350.setErrorCode(result);
                LOG.error("Feature Id cannot be empty");
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Fetch action list from featureaction table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.FILTER, "Type_id eq '" + actionType + "' and Feature_id eq '" + featureId + "'");
            inputMap.put(ODataQueryConstants.SELECT, "id, name, isMFAApplicable");

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
                        String isMFAApplicable = null;
                        isMFAApplicable = actionJSONObject.optString("isMFAApplicable");
                        if(StringUtils.equals(isMFAApplicable, "false"))
                        	continue;
                        Record currRecord = new Record();
                        String actionId = actionJSONObject.getString("id");
                        Param actionId_Param = new Param("actionId", actionId, FabricConstants.STRING);
                        currRecord.addParam(actionId_Param);
                        String actionName = actionJSONObject.getString("name");
                        Param actionName_Param = new Param("actionName", actionName, FabricConstants.STRING);
                        currRecord.addParam(actionName_Param);
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

    public Result getMFAFeature(DataControllerRequest requestInstance) {
        Result result = new Result();
        try {

            // Fetch feature list from feature and featureaction table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.SELECT, "feature_id, feature_name");

            String readFeatureResponse = Executor.invokeService(ServiceURLEnum.MFA_C360_FEATURE_GET_PROC, inputMap, null,
                    requestInstance);

            JSONObject readFeatureResponseJSON = CommonUtilities.getStringAsJSONObject(readFeatureResponse);
            if (readFeatureResponseJSON != null && readFeatureResponseJSON.has(FabricConstants.OPSTATUS)
                    && readFeatureResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readFeatureResponseJSON.has("records")) {
                JSONArray readFeatureJSONArray = readFeatureResponseJSON.optJSONArray("records");
                Dataset featureDataset = new Dataset();
                featureDataset.setId("features");
                if ((readFeatureJSONArray != null) && (readFeatureJSONArray.length() > 0)) {
                    for (int indexVar = 0; indexVar < readFeatureJSONArray.length(); indexVar++) {
                        JSONObject featureJSONObject = readFeatureJSONArray.getJSONObject(indexVar);
                        Record currRecord = new Record();
                        String featureId = featureJSONObject.getString("feature_id");
                        Param featureId_Param = new Param("featureId", featureId, FabricConstants.STRING);
                        currRecord.addParam(featureId_Param);
                        String featureName = featureJSONObject.getString("feature_name");
                        Param featureName_Param = new Param("featureName", featureName, FabricConstants.STRING);
                        currRecord.addParam(featureName_Param);
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
    
    public Result createOrEditMFAScenario(DataControllerRequest requestInstance, String methodName) {

        Result result = new Result();
        EventEnum event = EventEnum.UPDATE;
        String eventFailureDescription = "MFA Scenario update failed";

        try {
            String loggedInUserId = null;
            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            if (loggedInUserDetails != null) {
                loggedInUserId = loggedInUserDetails.getUserId();
            }

            if (methodName.equals(CREATE_MFA_SCENARIO_METHOD_NAME)) {
                event = EventEnum.CREATE;
                eventFailureDescription = "MFA Scenario create failed";
            }

            // Validate ActionId
            String actionId = requestInstance.getParameter("actionId");
            if (StringUtils.isBlank(actionId)) {
                LOG.error("Action Id is a mandatory input");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                        ActivityStatusEnum.FAILED, eventFailureDescription);
                ErrorCodeEnum.ERR_20866.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Validate ActionType
            String actionType = requestInstance.getParameter("actionType");
            if (StringUtils.isBlank(actionType)) {
                LOG.error("Action type cannot be empty");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                        ActivityStatusEnum.FAILED, eventFailureDescription);
                ErrorCodeEnum.ERR_20783.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Fetch ActionName and MFAId from featureaction table
            String actionName = null;
            String mfaId = null;
            String isMFAApplicable = null;
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.FILTER, "id eq '" + actionId + "' and Type_id eq '" + actionType + "'");
            inputMap.put(ODataQueryConstants.SELECT, "name, MFA_id, isMFAApplicable");

            String readActionResponse = Executor.invokeService(ServiceURLEnum.FEATUREACTION_READ, inputMap, null,
                    requestInstance);

            JSONObject readActionResponseJSON = CommonUtilities.getStringAsJSONObject(readActionResponse);
            if (readActionResponseJSON != null && readActionResponseJSON.has(FabricConstants.OPSTATUS)
                    && readActionResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readActionResponseJSON.has("featureaction")) {
                JSONArray readActionJSONArray = readActionResponseJSON.optJSONArray("featureaction");
                if (readActionJSONArray == null || readActionJSONArray.length() < 1) {
                    LOG.error("No record found for action id and action type");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                            ActivityStatusEnum.FAILED, eventFailureDescription);
                    ErrorCodeEnum.ERR_21320.setErrorCode(result);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                } else {
                    JSONObject currActionRecord = readActionJSONArray.getJSONObject(0);
                    actionName = currActionRecord.getString("name");
                    mfaId = currActionRecord.optString("MFA_id");
                    isMFAApplicable = currActionRecord.optString("isMFAApplicable");
                }
            } else {
                LOG.error("No record found for action id and action type");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                        ActivityStatusEnum.FAILED, eventFailureDescription);
                ErrorCodeEnum.ERR_21320.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }
            
            if(StringUtils.equals(isMFAApplicable, "false")) {
            	ErrorCodeEnum.ERR_21349.setErrorCode(result);
                LOG.error("MFA is not applicable");
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            String frequencyTypeId = null, frequencyValue = null, frequencyTypeDescription = "Always";

            if (actionType.equals("MONETARY")) {

                // Validate FrequencyTypeId
                frequencyTypeId = requestInstance.getParameter("frequencyTypeId");
                if (StringUtils.isBlank(frequencyTypeId)) {
                    LOG.error("Frequency Type Id cannot be empty");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                            ActivityStatusEnum.FAILED, eventFailureDescription);
                    ErrorCodeEnum.ERR_21321.setErrorCode(result);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                }

                // Fetch frequencyTypeId from frequencytype table
                inputMap.clear();
                inputMap.put(ODataQueryConstants.FILTER, "id eq '" + frequencyTypeId + "'");
                inputMap.put(ODataQueryConstants.SELECT, "id, Description");

                String readFrequencyTypeResponse = Executor.invokeService(ServiceURLEnum.FREQUENCYTYPE_READ, inputMap,
                        null, requestInstance);

                JSONObject readFrequencyTypeResponseJSON = CommonUtilities
                        .getStringAsJSONObject(readFrequencyTypeResponse);
                if (readFrequencyTypeResponseJSON != null && readFrequencyTypeResponseJSON.has(FabricConstants.OPSTATUS)
                        && readFrequencyTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                        && readFrequencyTypeResponseJSON.has("frequencytype")) {
                    JSONArray readFrequencyTypeJSONArray = readFrequencyTypeResponseJSON.optJSONArray("frequencytype");
                    if (readFrequencyTypeJSONArray == null || readFrequencyTypeJSONArray.length() < 1) {
                        ErrorCodeEnum.ERR_21322.setErrorCode(result);
                        LOG.error("Invalid Frequency Type Id");
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                                ActivityStatusEnum.FAILED, eventFailureDescription);
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        return result;
                    } else {
                        JSONObject currFrequencyTypeRecord = readFrequencyTypeJSONArray.getJSONObject(0);
                        frequencyTypeDescription = currFrequencyTypeRecord.getString("Description");
                    }
                } else {
                    ErrorCodeEnum.ERR_21322.setErrorCode(result);
                    LOG.error("Invalid Frequency Type Id");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                            ActivityStatusEnum.FAILED, eventFailureDescription);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                }

                if (frequencyTypeId.equals("VALUE_BASED")) {
                    // Validate FrequencyValue
                    frequencyValue = requestInstance.getParameter("frequencyValue");
                    if (StringUtils.isBlank(frequencyValue)) {
                        ErrorCodeEnum.ERR_21323.setErrorCode(result);
                        LOG.error("Frequency Value cannot be empty");
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                                ActivityStatusEnum.FAILED, eventFailureDescription);
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        return result;
                    }
                }
            } else {
                frequencyTypeId = DEFAULT_FREQUENCY_TYPE_ID;
            }

            // Validate mfaScenarioDescription
            String mfaScenarioDescription = requestInstance.getParameter("mfaScenarioDescription");
            if (StringUtils.isBlank(mfaScenarioDescription)) {
                ErrorCodeEnum.ERR_21324.setErrorCode(result);
                LOG.error("MFA Scenario Description cannot be empty");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                        ActivityStatusEnum.FAILED, eventFailureDescription);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Validate PrimaryMFATypeId
            String primaryMFATypeId = requestInstance.getParameter("primaryMFATypeId");
            if (StringUtils.isBlank(primaryMFATypeId)) {
                LOG.error("MFAType cannot be empty");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                        ActivityStatusEnum.FAILED, eventFailureDescription);
                ErrorCodeEnum.ERR_21300.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Validate SecondaryMFATypeId
            String secondaryMFATypeId = requestInstance.getParameter("secondaryMFATypeId");
            if (StringUtils.isBlank(secondaryMFATypeId)) {
                LOG.error("MFAType cannot be empty");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                        ActivityStatusEnum.FAILED, eventFailureDescription);
                ErrorCodeEnum.ERR_21300.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            if (primaryMFATypeId.equals(secondaryMFATypeId)) {
                LOG.error("Primary MFA Type and Secondary MFA Type should be different");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                        ActivityStatusEnum.FAILED, eventFailureDescription);
                ErrorCodeEnum.ERR_21325.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Fetch mfaTypeId from mfatype table
            String primaryMFATypeName = null, secondaryMFATypeName = null;
            inputMap.clear();
            inputMap.put(ODataQueryConstants.FILTER,
                    "id eq '" + primaryMFATypeId + "' or id eq '" + secondaryMFATypeId + "'");
            inputMap.put(ODataQueryConstants.SELECT, "id, Name");

            String readMFATypeResponse = Executor.invokeService(ServiceURLEnum.MFATYPE_READ, inputMap, null,
                    requestInstance);

            JSONObject readMFATypeResponseJSON = CommonUtilities.getStringAsJSONObject(readMFATypeResponse);
            if (readMFATypeResponseJSON != null && readMFATypeResponseJSON.has(FabricConstants.OPSTATUS)
                    && readMFATypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readMFATypeResponseJSON.has("mfatype")) {
                JSONArray readMFATypeJSONArray = readMFATypeResponseJSON.optJSONArray("mfatype");
                if (readMFATypeJSONArray == null || readMFATypeJSONArray.length() < 2) {
                    LOG.error("Invalid MFA Type");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                            ActivityStatusEnum.FAILED, eventFailureDescription);
                    ErrorCodeEnum.ERR_21326.setErrorCode(result);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                } else {
                    String currMFATypeId = null;
                    for (int i = 0; i < readMFATypeJSONArray.length(); i++) {
                        JSONObject readMFATypeObj = readMFATypeJSONArray.getJSONObject(i);
                        currMFATypeId = readMFATypeObj.getString("id");
                        if (currMFATypeId.equals(primaryMFATypeId)) {
                            primaryMFATypeName = readMFATypeObj.getString("Name");
                        } else if (currMFATypeId.equals(secondaryMFATypeId)) {
                            secondaryMFATypeName = readMFATypeObj.getString("Name");
                        }
                    }
                }
            } else {
                LOG.error("Invalid MFA Type");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                        ActivityStatusEnum.FAILED, eventFailureDescription);
                ErrorCodeEnum.ERR_21326.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            String smsText = "";
            String emailSubject = "";
            String emailBody = "";

            if (primaryMFATypeId.equals("SECURE_ACCESS_CODE") || secondaryMFATypeId.equals("SECURE_ACCESS_CODE")) {

                // Validate SMS Text
                smsText = requestInstance.getParameter("smsText");
                if (StringUtils.isBlank(smsText)) {
                    ErrorCodeEnum.ERR_21329.setErrorCode(result);
                    LOG.error("SMS Text cannot be empty");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                            ActivityStatusEnum.FAILED, eventFailureDescription);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                }

                // Validate EmailSubject
                emailSubject = requestInstance.getParameter("emailSubject");
                if (StringUtils.isBlank(emailSubject)) {
                    ErrorCodeEnum.ERR_21330.setErrorCode(result);
                    LOG.error("Email Subject cannot be empty");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                            ActivityStatusEnum.FAILED, eventFailureDescription);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                }

                // Validate EmailBody
                emailBody = requestInstance.getParameter("emailBody");
                if (StringUtils.isBlank(emailBody)) {
                    ErrorCodeEnum.ERR_21331.setErrorCode(result);
                    LOG.error("Email Body cannot be empty");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                            ActivityStatusEnum.FAILED, eventFailureDescription);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                }
            }

            // Validate MFA Scenario Status Id
            String mfaScenarioStatusId = requestInstance.getParameter("mfaScenarioStatusId");
            if (StringUtils.isBlank(mfaScenarioStatusId)) {
                ErrorCodeEnum.ERR_21332.setErrorCode(result);
                LOG.error("MFA Scenario StatusId cannot be empty");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                        ActivityStatusEnum.FAILED, eventFailureDescription);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            String mfaScenarioStatusName = null;
            if (mfaScenarioStatusId.equals("SID_ACTIVE"))
                mfaScenarioStatusName = "Active";
            else
                mfaScenarioStatusName = "Inactive";

            if (!(mfaScenarioStatusId.equals("SID_ACTIVE") || mfaScenarioStatusId.equals("SID_INACTIVE"))) {
                ErrorCodeEnum.ERR_21333.setErrorCode(result);
                LOG.error("Invalid status ID for MFA scenario");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                        ActivityStatusEnum.FAILED, eventFailureDescription);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            inputMap.clear();
            inputMap.put("FrequencyType_id", frequencyTypeId);
            inputMap.put("FrequencyValue", frequencyValue);
            inputMap.put("Status_id", mfaScenarioStatusId);
            inputMap.put("Description", mfaScenarioDescription);
            inputMap.put("PrimaryMFAType", primaryMFATypeId);
            inputMap.put("SecondaryMFAType", secondaryMFATypeId);
            inputMap.put("SMSText", smsText);
            inputMap.put("EmailSubject", emailSubject);
            inputMap.put("EmailBody", emailBody);

            if (methodName.equals(CREATE_MFA_SCENARIO_METHOD_NAME)) {            
            	if(!StringUtils.equals(mfaId, "")) {
            		LOG.error("MFA Scenario already present for ActionId");
            		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
            				ActivityStatusEnum.FAILED, eventFailureDescription);
            		ErrorCodeEnum.ERR_21345.setErrorCode(result);
            		result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            		return result;
            	}
            	
                // Creating MFA Scenario
                String mfaScenarioId = Long.toString(CommonUtilities.getNumericId());
                inputMap.put("id", mfaScenarioId);
                inputMap.put("createdby", loggedInUserId);
                inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());

                String createMFAResponse = Executor.invokeService(ServiceURLEnum.MFA_CREATE, inputMap,
                        null, requestInstance);

                JSONObject createMFAResponseJSON = CommonUtilities.getStringAsJSONObject(createMFAResponse);
                if ((createMFAResponseJSON != null)
                        && createMFAResponseJSON.has(FabricConstants.OPSTATUS)
                        && createMFAResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                    LOG.debug("MFA Scenario created successfully.");
                    if (frequencyTypeId.equals("ALWAYS"))
                        frequencyValue = "NA";
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                            ActivityStatusEnum.SUCCESSFUL,
                            "MFA Scenario created successfully. " + "/" + "Status:"
                                    + mfaScenarioStatusName + "/" + "Action type:" + actionType + "/"
                                    + "Action:" + actionName + "/" + "Frequency:" + frequencyTypeDescription + "/"
                                    + "Value:" + frequencyValue + "/" + "MFA scenario description:"
                                    + mfaScenarioDescription + "/" + "Primary MFA:" + primaryMFATypeName + "/"
                                    + "Secondary MFA:" + secondaryMFATypeName);
                    result.addParam(new Param("status", "Success", FabricConstants.STRING));
                } else {
                    LOG.error("Failed to create MFA Scenario");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                            ActivityStatusEnum.FAILED, eventFailureDescription);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    ErrorCodeEnum.ERR_21335.setErrorCode(result);
                    return result;
                }

                // Updating MFAId in featureaction table
                inputMap.clear();
                inputMap.put("id", actionId);
                inputMap.put("MFA_id", mfaScenarioId);
                Executor.invokeService(ServiceURLEnum.FEATUREACTION_UPDATE, inputMap, null, requestInstance);
                
            } else if (methodName.equals(EDIT_MFA_SCENARIO_METHOD_NAME)) {

                // Getting mfa scenario data from mfa table
                Map<String, String> inputParameterMap = new HashMap<String, String>();
                
                inputParameterMap.put(ODataQueryConstants.FILTER, "id eq '" + mfaId + "'");
                inputParameterMap.put(ODataQueryConstants.SELECT,
                        "id, FrequencyType_id, FrequencyValue, Status_id, Description, PrimaryMFAType, SecondaryMFAType");

                String readMFAResponse = Executor.invokeService(ServiceURLEnum.MFA_READ,
                        inputParameterMap, null, requestInstance);

                String oldFrequencyTypeId = null;
                String oldFrequencyValue = null;
                String oldStatusId = null;
                String oldDescription = null;
                String oldPrimaryMFAType = null;
                String oldSecondaryMFAType = null;

                JSONObject readMFAResponseJSON = CommonUtilities
                        .getStringAsJSONObject(readMFAResponse);
                if (readMFAResponseJSON != null && readMFAResponseJSON.has(FabricConstants.OPSTATUS)
                        && readMFAResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                        && readMFAResponseJSON.has("mfa")) {
                    JSONArray readMFAJSONArray = readMFAResponseJSON.optJSONArray("mfa");
                    if (readMFAJSONArray == null || readMFAJSONArray.length() < 1) {
                        ErrorCodeEnum.ERR_21336.setErrorCode(result);
                        LOG.error("MFA scenario is absent");
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                                ActivityStatusEnum.FAILED, eventFailureDescription);
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        return result;
                    } else {
                        JSONObject mfaRecord = readMFAJSONArray.getJSONObject(0);
                        oldFrequencyTypeId = mfaRecord.getString("FrequencyType_id");
                        if (oldFrequencyTypeId.equals("VALUE_BASED"))
                            oldFrequencyValue = mfaRecord.getString("FrequencyValue");
                        oldStatusId = mfaRecord.getString("Status_id");
                        oldDescription = mfaRecord.getString("Description");
                        oldPrimaryMFAType = mfaRecord.getString("PrimaryMFAType");
                        oldSecondaryMFAType = mfaRecord.getString("SecondaryMFAType");
                    }
                } else {
                    ErrorCodeEnum.ERR_21336.setErrorCode(result);
                    LOG.error("MFA scenario is absent");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                            ActivityStatusEnum.FAILED, eventFailureDescription);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                }

                // Editing MFA Scenario
                inputMap.put("id", mfaId);
                inputMap.put("modifiedby", loggedInUserDetails.getUserName());
                inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

                String editMFAResponse = Executor.invokeService(ServiceURLEnum.MFA_UPDATE, inputMap,
                        null, requestInstance);

                JSONObject editMFAResponseJSON = CommonUtilities
                        .getStringAsJSONObject(editMFAResponse);
                if ((editMFAResponseJSON != null) && editMFAResponseJSON.has(FabricConstants.OPSTATUS)
                        && editMFAResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                    LOG.debug("MFA Scenario updated successfully.");
                    String auditSuccessDescription = "MFA Scenario updated successfully. " +
                    		"Action type:" + actionType + "/" + "Action:" + actionName;
                    if (!mfaScenarioStatusId.equals(oldStatusId))
                        auditSuccessDescription += "/" + "Status:" + mfaScenarioStatusName;
                    if (!frequencyTypeId.equals(oldFrequencyTypeId))
                        auditSuccessDescription += "/" + "Frequency:" + frequencyTypeDescription;
                    if ((frequencyTypeId.equals("VALUE_BASED")) && (!frequencyValue.equals(oldFrequencyValue)))
                        auditSuccessDescription += "/" + "Value:" + frequencyValue;
                    if (!mfaScenarioDescription.equals(oldDescription))
                        auditSuccessDescription += "/" + "MFA scenario description:" + mfaScenarioDescription;
                    if (!primaryMFATypeId.equals(oldPrimaryMFAType))
                        auditSuccessDescription += "/" + "Primary MFA:" + primaryMFATypeName;
                    if (!secondaryMFATypeId.equals(oldSecondaryMFAType))
                        auditSuccessDescription += "/" + "Secondary MFA:" + secondaryMFATypeName;

                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                            ActivityStatusEnum.SUCCESSFUL, auditSuccessDescription);
                    result.addParam(new Param("status", "Success", FabricConstants.STRING));
                } else {
                    LOG.error("Failed to edit MFA Scenario");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                            ActivityStatusEnum.FAILED, eventFailureDescription);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    ErrorCodeEnum.ERR_21337.setErrorCode(result);
                    return result;
                }
            }

            result.addParam(new Param("status", "Success", FabricConstants.STRING));

        } catch (Exception e) {
            LOG.error("Unexepected Error in create or edit MFA Scenario flow. Exception: ", e);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, event,
                    ActivityStatusEnum.FAILED, eventFailureDescription);
            ErrorCodeEnum.ERR_21338.setErrorCode(result);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
        }

        return result;
    }

    public Result deleteMFAScenario(DataControllerRequest requestInstance) {
        Result result = new Result();
        try {

            // Validate actionId
            String actionId = requestInstance.getParameter("actionId");
            if (StringUtils.isBlank(actionId)) {
                ErrorCodeEnum.ERR_20866.setErrorCode(result);
                LOG.error("ActionId cannot be empty");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, EventEnum.DELETE,
                        ActivityStatusEnum.FAILED, "MFA Scenario delete failed");
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Fetch MFAId from featureaction table
            String mfaId = null;
            String actionName = null;
            String isMFAApplicable = null;
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.FILTER, "id eq '" + actionId + "'");
            inputMap.put(ODataQueryConstants.SELECT, "MFA_id, name, isMFAApplicable");

            String readActionResponse = Executor.invokeService(ServiceURLEnum.FEATUREACTION_READ, inputMap, null,
                    requestInstance);

            JSONObject readActionResponseJSON = CommonUtilities.getStringAsJSONObject(readActionResponse);
            if (readActionResponseJSON != null && readActionResponseJSON.has(FabricConstants.OPSTATUS)
                    && readActionResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readActionResponseJSON.has("featureaction")) {
                JSONArray readActionJSONArray = readActionResponseJSON.optJSONArray("featureaction");
                if (readActionJSONArray == null || readActionJSONArray.length() < 1) {
                    LOG.error("No record found for Action Id");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, EventEnum.DELETE,
                            ActivityStatusEnum.FAILED, "MFA Scenario delete failed");
                    ErrorCodeEnum.ERR_21351.setErrorCode(result);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                } else {
                    JSONObject currActionRecord = readActionJSONArray.getJSONObject(0);
                    mfaId = currActionRecord.optString("MFA_id");
                    actionName = currActionRecord.optString("name");
                    isMFAApplicable = currActionRecord.optString("isMFAApplicable");
                }
            } else {
                LOG.error("No record found for Action Id");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, EventEnum.DELETE,
                        ActivityStatusEnum.FAILED, "MFA Scenario delete failed");
                ErrorCodeEnum.ERR_21351.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }
            
            if(StringUtils.equals(isMFAApplicable, "false")) {
            	ErrorCodeEnum.ERR_21349.setErrorCode(result);
                LOG.error("MFA is not applicable for this action");
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }
            
            // Updating MFAId in featureaction table to null
            inputMap.clear();
            inputMap.put("id", actionId);
            inputMap.put("MFA_id", "NULL");
            String editActionResponse = Executor.invokeService(ServiceURLEnum.FEATUREACTION_UPDATE, inputMap, null, requestInstance);

            JSONObject editActionResponseJSON = CommonUtilities.getStringAsJSONObject(editActionResponse);
            if (!(editActionResponseJSON != null && editActionResponseJSON.has(FabricConstants.OPSTATUS)
            		&& editActionResponseJSON.getInt(FabricConstants.OPSTATUS) == 0)) {
            	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            	ErrorCodeEnum.ERR_21339.setErrorCode(result);
            	LOG.error("Error in deleting mfa scenario.");
            	AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, EventEnum.DELETE,
            			ActivityStatusEnum.FAILED, "MFA Scenario delete failed");
            }

            //Deleting mfa scenario record
            inputMap.clear();
            inputMap.put("id", mfaId);

            String deleteMFAResponse = Executor.invokeService(ServiceURLEnum.MFA_DELETE, inputMap,
                    null, requestInstance);

            JSONObject deleteMFAResponseJSON = CommonUtilities.getStringAsJSONObject(deleteMFAResponse);
            if (deleteMFAResponseJSON != null && deleteMFAResponseJSON.has(FabricConstants.OPSTATUS)
                    && deleteMFAResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                result.addParam(new Param("status", "Success", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, EventEnum.DELETE,
                        ActivityStatusEnum.SUCCESSFUL, "MFA Scenario delete successful. Action: " + actionName);
            } else {
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_21339.setErrorCode(result);
                LOG.error("Error in deleting mfa scenario.");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, EventEnum.DELETE,
                        ActivityStatusEnum.FAILED, "MFA Scenario delete failed");
            }

        } catch (Exception e) {
            ErrorCodeEnum.ERR_21339.setErrorCode(result);
            LOG.error("Error in deleting mfa scenario.", e);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "MFA Scenario delete failed");
        }
        return result;
    }

    public Result getMFAScenario(DataControllerRequest requestInstance) {
        Result result = new Result();
        try {

            // Fetch mfa scenario from mfa table
            String serverActionId = requestInstance.getParameter("actionId");
            Map<String, String> inputMap = new HashMap<String, String>();

            if (StringUtils.isNotBlank(serverActionId)) {
            	// Fetch MFAId from featureaction table
                String mfaId = null;
                String isMFAApplicable = null;
                inputMap.put(ODataQueryConstants.FILTER, "id eq '" + serverActionId + "'");
                inputMap.put(ODataQueryConstants.SELECT, "MFA_id, isMFAApplicable");

                String readActionResponse = Executor.invokeService(ServiceURLEnum.FEATUREACTION_READ, inputMap, null,
                        requestInstance);

                JSONObject readActionResponseJSON = CommonUtilities.getStringAsJSONObject(readActionResponse);
                if (readActionResponseJSON != null && readActionResponseJSON.has(FabricConstants.OPSTATUS)
                        && readActionResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                        && readActionResponseJSON.has("featureaction")) {
                    JSONArray readActionJSONArray = readActionResponseJSON.optJSONArray("featureaction");
                    if (readActionJSONArray == null || readActionJSONArray.length() < 1) {
                        LOG.error("No record found for Action Id");
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, EventEnum.DELETE,
                                ActivityStatusEnum.FAILED, "MFA Scenario delete failed");
                        ErrorCodeEnum.ERR_21351.setErrorCode(result);
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        return result;
                    } else {
                        JSONObject currActionRecord = readActionJSONArray.getJSONObject(0);
                        mfaId = currActionRecord.optString("MFA_id");
                        isMFAApplicable = currActionRecord.optString("isMFAApplicable");
                        if(StringUtils.equals(isMFAApplicable, "false")) {
                        	ErrorCodeEnum.ERR_21349.setErrorCode(result);
                            LOG.error("MFA is not applicable for this action");
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            return result;
                        }
                    }
                } else {
                    LOG.error("No record found for Action Id");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MFASCENARIOS, EventEnum.DELETE,
                            ActivityStatusEnum.FAILED, "MFA Scenario delete failed");
                    ErrorCodeEnum.ERR_21351.setErrorCode(result);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                }

                inputMap.clear();
                inputMap.put(ODataQueryConstants.FILTER, "id eq '" + mfaId + "'");
            }

            inputMap.put(ODataQueryConstants.SELECT,
                    "id, FrequencyType_id, FrequencyValue, Status_id, Description, PrimaryMFAType, SecondaryMFAType, SMSText, EmailSubject, EmailBody");

            String readMFAResponse = Executor.invokeService(ServiceURLEnum.MFA_READ, inputMap, null,
                    requestInstance);

            JSONObject readMFAResponseJSON = CommonUtilities.getStringAsJSONObject(readMFAResponse);
            if (readMFAResponseJSON != null && readMFAResponseJSON.has(FabricConstants.OPSTATUS)
                    && readMFAResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readMFAResponseJSON.has("mfa")) {
                JSONArray readMFAJSONArray = readMFAResponseJSON.optJSONArray("mfa");
                Dataset mfaDataset = new Dataset();
                mfaDataset.setId("mfaScenarios");
                if ((readMFAJSONArray != null) && (readMFAJSONArray.length() > 0)) {
                    for (int indexVar = 0; indexVar < readMFAJSONArray.length(); indexVar++) {
                        JSONObject mfaJSONObject = readMFAJSONArray.getJSONObject(indexVar);
                        Record currRecord = new Record();

                        String mfaId = mfaJSONObject.optString("id");
                        String frequencyTypeId = mfaJSONObject.optString("FrequencyType_id");
                        String frequencyValue = mfaJSONObject.optString("FrequencyValue");
                        String mfaScenarioStatusId = mfaJSONObject.optString("Status_id");
                        String mfaScenarioDescription = mfaJSONObject.optString("Description");
                        String primaryMFATypeId = mfaJSONObject.optString("PrimaryMFAType");
                        String secondaryMFATypeId = mfaJSONObject.optString("SecondaryMFAType");
                        String smsText = mfaJSONObject.optString("SMSText");
                        String emailSubject = mfaJSONObject.optString("EmailSubject");
                        String emailBody = mfaJSONObject.optString("EmailBody");

                        // Fetching actionId, actionName and actionType from featureaction table
                        inputMap.clear();
                        inputMap.put(ODataQueryConstants.FILTER, "MFA_id eq '" + mfaId + "'");
                        inputMap.put(ODataQueryConstants.SELECT, "id, Feature_id, Type_id, name, isMFAApplicable");

                        String readActionResponse = Executor.invokeService(ServiceURLEnum.FEATUREACTION_READ, inputMap, null,
                                requestInstance);
                        
                        String actionId = null;
                        String featureId = null;
                        String actionName = null;
                        String actionType = null;
                        String featureName = null;
                        String featureStatus = null;
                        String isMFAApplicable = null;

                        JSONObject readActionResponseJSON = CommonUtilities.getStringAsJSONObject(readActionResponse);
                        if (readActionResponseJSON != null && readActionResponseJSON.has(FabricConstants.OPSTATUS)
                                && readActionResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                                && readActionResponseJSON.has("featureaction")) {
                            JSONArray readActionJSONArray = readActionResponseJSON.optJSONArray("featureaction");
                            if (readActionJSONArray == null || readActionJSONArray.length() < 1) {
                                LOG.error("Failed to Fetch Action");
                                ErrorCodeEnum.ERR_21310.setErrorCode(result);
                                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                                return result;
                            } else {
                                JSONObject currActionRecord = readActionJSONArray.getJSONObject(0);
                                actionName = currActionRecord.optString("name");
                                actionType = currActionRecord.optString("Type_id");
                                actionId = currActionRecord.optString("id");
                                featureId = currActionRecord.optString("Feature_id");
                                isMFAApplicable = currActionRecord.optString("isMFAApplicable");
                                if(StringUtils.equals(isMFAApplicable, "false"))
                                	continue;
                            }
                        } else {
                            LOG.error("Failed to Fetch Action");
                            ErrorCodeEnum.ERR_21310.setErrorCode(result);
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            return result;
                        }
                        
                        // Fetching featureName from feature table
                        inputMap.clear();
                        inputMap.put(ODataQueryConstants.FILTER, "id eq '" + featureId + "'");
                        inputMap.put(ODataQueryConstants.SELECT, "name, Status_id");

                        String readFeatureResponse = Executor.invokeService(ServiceURLEnum.FEATURE_READ, inputMap, null,
                        		requestInstance);

                        JSONObject readFeatureResponseJSON = CommonUtilities.getStringAsJSONObject(readFeatureResponse);
                        if (readFeatureResponseJSON != null && readFeatureResponseJSON.has(FabricConstants.OPSTATUS)
                        		&& readFeatureResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                        		&& readFeatureResponseJSON.has("feature")) {
                        	JSONArray readFeatureJSONArray = readFeatureResponseJSON.optJSONArray("feature");
                        	if (readFeatureJSONArray == null || readFeatureJSONArray.length() < 1) {
                        		LOG.error("Failed to Fetch Feature");
                        		ErrorCodeEnum.ERR_21352.setErrorCode(result);
                        		result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        		return result;
                        	} else {
                        		JSONObject currFeatureRecord = readFeatureJSONArray.getJSONObject(0);
                        		featureName = currFeatureRecord.optString("name");
                        		featureStatus = currFeatureRecord.optString("Status_id");
                        		if(!StringUtils.equals(featureStatus, "SID_FEATURE_ACTIVE"))
                                	continue;
                        	}
                        } else {
                        	LOG.error("Failed to Fetch Action");
                        	ErrorCodeEnum.ERR_21310.setErrorCode(result);
                        	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        	return result;
                        }

                        // Creating actionRecord
                        Record actionRecord = new Record();
                        actionRecord.setId("action");

                        Param actionId_Param = new Param("actionId", actionId, FabricConstants.STRING);
                        actionRecord.addParam(actionId_Param);
                        Param featureId_Param = new Param("featureId", featureId, FabricConstants.STRING);
                        actionRecord.addParam(featureId_Param);
                        Param featureName_Param = new Param("featureName", featureName, FabricConstants.STRING);
                        actionRecord.addParam(featureName_Param);
                        Param actionName_Param = new Param("actionName", actionName, FabricConstants.STRING);
                        actionRecord.addParam(actionName_Param);
                        Param actionType_Param = new Param("actionType", actionType, FabricConstants.STRING);
                        actionRecord.addParam(actionType_Param);

                        currRecord.addRecord(actionRecord);

                        // Fetching frequencyTypeName from frequencytype table
                        inputMap.clear();
                        inputMap.put(ODataQueryConstants.FILTER, "id eq '" + frequencyTypeId + "'");
                        inputMap.put(ODataQueryConstants.SELECT, "Description");

                        String readFrequencyTypeResponse = Executor.invokeService(ServiceURLEnum.FREQUENCYTYPE_READ,
                                inputMap, null, requestInstance);

                        String frequencyTypeName = null;

                        JSONObject readFrequencyTypeResponseJSON = CommonUtilities
                                .getStringAsJSONObject(readFrequencyTypeResponse);
                        if (readFrequencyTypeResponseJSON != null
                                && readFrequencyTypeResponseJSON.has(FabricConstants.OPSTATUS)
                                && readFrequencyTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                                && readFrequencyTypeResponseJSON.has("frequencytype")) {
                            JSONArray readFrequencyTypeJSONArray = readFrequencyTypeResponseJSON
                                    .optJSONArray("frequencytype");
                            if (readFrequencyTypeJSONArray == null || readFrequencyTypeJSONArray.length() < 1) {
                                ErrorCodeEnum.ERR_21309.setErrorCode(result);
                                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                                LOG.error("Failed to Fetch Frequency Type");
                                return result;
                            } else {
                                JSONObject currFrequencyTypeRecord = readFrequencyTypeJSONArray.getJSONObject(0);
                                frequencyTypeName = currFrequencyTypeRecord.optString("Description");
                            }
                        } else {
                            ErrorCodeEnum.ERR_21309.setErrorCode(result);
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            LOG.error("Failed to Fetch Frequency Type");
                            return result;
                        }

                        // Creating frequencyTypeRecord
                        Record frequencyTypeRecord = new Record();
                        frequencyTypeRecord.setId("frequencyType");

                        Param frequencyTypeId_Param = new Param("frequencyTypeId", frequencyTypeId,
                                FabricConstants.STRING);
                        frequencyTypeRecord.addParam(frequencyTypeId_Param);
                        Param frequencyTypeName_Param = new Param("frequencyTypeName", frequencyTypeName,
                                FabricConstants.STRING);
                        frequencyTypeRecord.addParam(frequencyTypeName_Param);
                        Param frequencyValue_Param = new Param("frequencyValue", frequencyValue,
                                FabricConstants.STRING);
                        frequencyTypeRecord.addParam(frequencyValue_Param);

                        currRecord.addRecord(frequencyTypeRecord);

                        Param appActionId_Param = new Param("actionId", actionId, FabricConstants.STRING);
                        currRecord.addParam(appActionId_Param);
                        Param mfaScenarioDescription_Param = new Param("mfaScenarioDescription", mfaScenarioDescription,
                                FabricConstants.STRING);
                        currRecord.addParam(mfaScenarioDescription_Param);
                        Param primaryMFATypeId_Param = new Param("primaryMFATypeId", primaryMFATypeId,
                                FabricConstants.STRING);
                        currRecord.addParam(primaryMFATypeId_Param);
                        Param secondaryMFATypeId_Param = new Param("secondaryMFATypeId", secondaryMFATypeId,
                                FabricConstants.STRING);
                        currRecord.addParam(secondaryMFATypeId_Param);
                        Param smsText_Param = new Param("smsText", smsText, FabricConstants.STRING);
                        currRecord.addParam(smsText_Param);
                        Param emailSubject_Param = new Param("emailSubject", emailSubject, FabricConstants.STRING);
                        currRecord.addParam(emailSubject_Param);
                        Param emailBody_Param = new Param("emailBody", emailBody, FabricConstants.STRING);
                        currRecord.addParam(emailBody_Param);
                        Param mfaScenarioStatusId_Param = new Param("mfaScenarioStatusId", mfaScenarioStatusId,
                                FabricConstants.STRING);
                        currRecord.addParam(mfaScenarioStatusId_Param);

                        // Fetching primaryMFAType and secondaryMFAType from mfatype table
                        inputMap.clear();
                        inputMap.put(ODataQueryConstants.FILTER,
                                "id eq '" + primaryMFATypeId + "' or id eq '" + secondaryMFATypeId + "'");
                        inputMap.put(ODataQueryConstants.SELECT, "id, Name");

                        String readMFATypeResponse = Executor.invokeService(ServiceURLEnum.MFATYPE_READ, inputMap, null,
                                requestInstance);

                        JSONObject readMFATypeResponseJSON = CommonUtilities.getStringAsJSONObject(readMFATypeResponse);
                        if (readMFATypeResponseJSON != null && readMFATypeResponseJSON.has(FabricConstants.OPSTATUS)
                                && readMFATypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                                && readMFATypeResponseJSON.has("mfatype")) {
                            JSONArray readMFATypeJSONArray = readMFATypeResponseJSON.optJSONArray("mfatype");
                            Record mfaTypeRecord = new Record();
                            mfaTypeRecord.setId("mfaTypes");
                            if ((readMFATypeJSONArray == null) || (readMFATypeJSONArray.length() < 2)) {
                                ErrorCodeEnum.ERR_21340.setErrorCode(result);
                                LOG.error("Primary mfa type and secondary mfa type is invalid");
                                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                                return result;
                            } else {
                                for (int index = 0; index < readMFATypeJSONArray.length(); index++) {
                                    JSONObject mfaTypeJSONObject = readMFATypeJSONArray.getJSONObject(index);
                                    Record currMFATypeRecord = new Record();
                                    currMFATypeRecord.setId(mfaTypeJSONObject.getString("id"));
                                    Param mfaTypeId_Param = new Param("mfaTypeId", mfaTypeJSONObject.getString("id"),
                                            FabricConstants.STRING);
                                    currMFATypeRecord.addParam(mfaTypeId_Param);
                                    String mfaTypeName = mfaTypeJSONObject.getString("Name");
                                    Param mfaTypeName_Param = new Param("mfaTypeName", mfaTypeName,
                                            FabricConstants.STRING);
                                    currMFATypeRecord.addParam(mfaTypeName_Param);

                                    mfaTypeRecord.addRecord(currMFATypeRecord);
                                }
                                currRecord.addRecord(mfaTypeRecord);
                            }
                        } else {
                            ErrorCodeEnum.ERR_21340.setErrorCode(result);
                            LOG.error("Primary mfa type and secondary mfa type is invalid");
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            return result;
                        }
                        mfaDataset.addRecord(currRecord);
                    }
                    result.addDataset(mfaDataset);
                }
            } else {
                result.addParam(new Param("message", readMFAResponse, FabricConstants.STRING));
                LOG.error("Failed to Fetch MFA Scenario Response: " + readMFAResponse);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_21341.setErrorCode(result);
                return result;
            }

            result.addParam(new Param("status", "Success", FabricConstants.STRING));
            return result;

        } catch (Exception e) {
            LOG.error("Unexepected Error in Fetching MFA Scenario. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_21341.setErrorCode(result);
        }
        return result;
    }

    public Result getMFAVariableReference(DataControllerRequest requestInstance) {
        Result result = new Result();
        try {

            // Fetch mfavariablereference data from mfavariablereference table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.SELECT, "Code, Name");

            String readMFAVariableReferenceResponse = Executor.invokeService(ServiceURLEnum.MFAVARIABLEREFERENCE_READ,
                    inputMap, null, requestInstance);

            JSONObject readMFAVariableReferenceResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readMFAVariableReferenceResponse);
            if (readMFAVariableReferenceResponseJSON != null
                    && readMFAVariableReferenceResponseJSON.has(FabricConstants.OPSTATUS)
                    && readMFAVariableReferenceResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readMFAVariableReferenceResponseJSON.has("mfavariablereference")) {
                JSONArray readMFAVariableReferenceJSONArray = readMFAVariableReferenceResponseJSON
                        .optJSONArray("mfavariablereference");
                Dataset mfavariableReferenceDataset = new Dataset();
                mfavariableReferenceDataset.setId("mfavariablereference");
                if ((readMFAVariableReferenceJSONArray != null) && (readMFAVariableReferenceJSONArray.length() > 0)) {
                    for (int indexVar = 0; indexVar < readMFAVariableReferenceJSONArray.length(); indexVar++) {
                        JSONObject mfaVariableReferenceJSONObject = readMFAVariableReferenceJSONArray
                                .getJSONObject(indexVar);
                        Record currRecord = new Record();
                        String code = mfaVariableReferenceJSONObject.getString("Code");
                        Param code_Param = new Param("Code", code, FabricConstants.STRING);
                        currRecord.addParam(code_Param);
                        String name = mfaVariableReferenceJSONObject.getString("Name");
                        Param name_Param = new Param("Name", name, FabricConstants.STRING);
                        currRecord.addParam(name_Param);
                        mfavariableReferenceDataset.addRecord(currRecord);
                    }
                    result.addDataset(mfavariableReferenceDataset);
                    return result;
                }
            } else {
                result.addParam(new Param("message", readMFAVariableReferenceResponse, FabricConstants.STRING));
                LOG.error("Failed to Fetch MFA Variable Reference Response: " + readMFAVariableReferenceResponse);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_21346.setErrorCode(result);
                return result;
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in Fetching MFA Variable Reference data. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_21346.setErrorCode(result);
        }
        return result;
    }

}
