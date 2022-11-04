package com.kony.adminconsole.service.decisionmanagement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.exception.DBPAuthenticationException;
import com.kony.adminconsole.utilities.DBPServices;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/*
* 
* @author Sai Krishna Aitha
*
*/
public class ManageDecisionRule implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(ManageDecisionRule.class);

    private static final String CREATE_DECISION_RULE_METHOD_NAME = "createDecisionRule";
    private static final String GET_DECISION_RULE_METHOD_NAME = "getDecisionRules";
    private static final String EDIT_DECISION_RULE_METHOD_NAME = "editDecisionRule";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        try {
            LOG.debug("Method Id:" + methodID);
            if (StringUtils.equalsIgnoreCase(methodID, CREATE_DECISION_RULE_METHOD_NAME)) {
                return createDecisionRule(requestInstance);
            }
            if (StringUtils.equalsIgnoreCase(methodID, GET_DECISION_RULE_METHOD_NAME)) {
                return getDecisionRules(requestInstance);
            }
            if (StringUtils.equalsIgnoreCase(methodID, EDIT_DECISION_RULE_METHOD_NAME)) {
                return editDecisionRule(requestInstance);
            }
            // Unsupported Method Id. Return Empty Result
            LOG.debug("Unsupported Method Id. Returning Empty Result");
            return new Result();
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    public Result createDecisionRule(DataControllerRequest requestInstance) throws DBPAuthenticationException {
        Result result = new Result();

        // Read Inputs
        String decisionName = requestInstance.getParameter("decisionName");
        String description = requestInstance.getParameter("description");

        // Validate Inputs
        if (StringUtils.isBlank(decisionName)) {
            LOG.error("Missing Mandatory Input: decisionName");
            ErrorCodeEnum.ERR_21571.setErrorCode(result);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            return result;
        }

        if (StringUtils.isBlank(description)) {
            LOG.error("Missing Mandatory Input: description");
            ErrorCodeEnum.ERR_21572.setErrorCode(result);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            return result;
        }
        // Create Decision
        JSONObject createDecisionruleResponse = DBPServices.createDecisionRule(decisionName, description,
                requestInstance);
        if (createDecisionruleResponse == null || !createDecisionruleResponse.has(FabricConstants.OPSTATUS)
                || createDecisionruleResponse.getInt(FabricConstants.OPSTATUS) != 0) {
            LOG.debug("Failed to create Decision");
            /*
             * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.DECISIONMANAGEMENT, eventEnum,
             * ActivityStatusEnum.FAILED, "Failed to create Decision" + decisionName);
             */
            ErrorCodeEnum.ERR_21573.setErrorCode(result);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            return result;
        }
        if (!createDecisionruleResponse.has("decisionId")) {
            LOG.debug("Failed to create Decision");
            ErrorCodeEnum.ERR_21579.setErrorCode(result);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            return result;
        }

        result.addParam(new Param("status", "Success", FabricConstants.STRING));
        result.addParam(
                new Param("decisionId", createDecisionruleResponse.getString("decisionId"), FabricConstants.STRING));
        LOG.debug("Create Decision Successful,Decision Id:" + createDecisionruleResponse.getString("decisionId"));
        /*
         * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.DECISIONMANAGEMENT, eventEnum,
         * ActivityStatusEnum.SUCCESSFUL, "Decision Id:" + createDecisionruleResponse.getString("decisionId"));
         */
        return result;
    }

    public Result getDecisionRules(DataControllerRequest requestInstance) throws DBPAuthenticationException {

        Result result = new Result();
        // Fetch Decisions
        JSONObject getDecisionrulesResponse = DBPServices.getDecisionRules(requestInstance);
        if (getDecisionrulesResponse == null || !getDecisionrulesResponse.has(FabricConstants.OPSTATUS)
                || getDecisionrulesResponse.getInt(FabricConstants.OPSTATUS) != 0) {
            LOG.debug("Failed to fetch decisions");
            ErrorCodeEnum.ERR_21574.setErrorCode(result);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            return result;
        }

        JSONArray readResponseJSONArray = getDecisionrulesResponse.optJSONArray("decisions");
        if (readResponseJSONArray == null) {
            LOG.debug("Failed to fetch decisions");
            ErrorCodeEnum.ERR_21583.setErrorCode(result);
            return result;
        }
        Dataset dataSet = new Dataset();
        dataSet.setId("decisions");
        result.addDataset(dataSet);
        for (int indexVar = 0; indexVar < readResponseJSONArray.length(); indexVar++) {
            JSONObject currJSONObject = readResponseJSONArray.getJSONObject(indexVar);
            Record currRecord = new Record();
            if (currJSONObject.length() != 0) {
                for (String currKey : currJSONObject.keySet()) {
                    if (currJSONObject.has(currKey)) {
                        currRecord.addParam(
                                new Param(currKey, currJSONObject.getString(currKey), FabricConstants.STRING));
                    }
                }
                dataSet.addRecord(currRecord);
            }
        }
        return result;
    }

    public Result editDecisionRule(DataControllerRequest requestInstance) {
        Result result = new Result();
        try {
            // Read Inputs
            String decisionId = requestInstance.getParameter("decisionId");
            String decisionName = requestInstance.getParameter("decisionName");
            String description = requestInstance.getParameter("description");
            String isActive = requestInstance.getParameter("isActive");
            String isSoftDeleted = requestInstance.getParameter("isSoftDeleted");
            // Validate Inputs
            if (StringUtils.isBlank(decisionId)) {
                LOG.error("Missing Mandatory Input: decisionId");
                ErrorCodeEnum.ERR_21575.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            if (StringUtils.isBlank(description) && StringUtils.isBlank(decisionName)) {
                LOG.debug("Description and Name shouldn't be empty");
                ErrorCodeEnum.ERR_21580.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            if (StringUtils.isBlank(isActive)) {
                isActive = StringUtils.EMPTY;
            }
            if (StringUtils.isBlank(isSoftDeleted)) {
                isSoftDeleted = StringUtils.EMPTY;
            }
            // Edit Decision
            JSONObject editDecisionruleResponse = DBPServices.editDecisionRule(decisionId, decisionName, description,
                    isActive, isSoftDeleted, requestInstance);
            if (editDecisionruleResponse == null || !editDecisionruleResponse.has(FabricConstants.OPSTATUS)
                    || editDecisionruleResponse.getInt(FabricConstants.OPSTATUS) != 0) {
                LOG.debug("Failed to edit the decision");
                /*
                 * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.DECISIONMANAGEMENT, eventEnum,
                 * ActivityStatusEnum.FAILED, "Failed to edit the decision:" + decisionId);
                 */
                ErrorCodeEnum.ERR_21576.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            String status = StringUtils.equals(editDecisionruleResponse.optString("status"), "success") ? "SUCCESS"
                    : "FAIL";
            result.addParam(new Param("status", status, FabricConstants.STRING));
            LOG.debug("Edit Decision Status:" + status);
            /*
             * AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.DECISIONMANAGEMENT, eventEnum,
             * ActivityStatusEnum.SUCCESSFUL, "Edit Successful for decision Id:" + decisionId);
             */

        } catch (Exception e) {
            LOG.error("Unexepected Error in create decision rule", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
        }
        return result;
    }

}
