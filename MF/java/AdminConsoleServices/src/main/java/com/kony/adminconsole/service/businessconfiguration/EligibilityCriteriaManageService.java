package com.kony.adminconsole.service.businessconfiguration;

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
 * 
 * Service to Manage Eligibility Criteria
 *
 * @author Aditya Mankal
 *
 */
public class EligibilityCriteriaManageService implements JavaService2 {

    private static final String EDIT_ELIGIBILITY_CRITERIA_METHOD_ID = "editEligibilityCriteria";
    private static final String ADD_ELIGIBILITY_CRITERIA_METHOD_ID = "addEligibilityCriteria";
    private static final String DELETE_ELIGIBILITY_CRITERIA_METHOD_ID = "deleteEligibilityCriteria";

    private static final Logger LOG = Logger.getLogger(EligibilityCriteriaManageService.class);
    private static final int DESCRIPTION_MIN_CHARS = 5;
    private static final String ELIGIBILITY_CRITERIA_DEFAULT_STATUS_ID = StatusEnum.SID_ACTIVE.name();

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) {

        try {
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);

            String criteriaID = requestInstance.getParameter("criteriaID");
            String description = requestInstance.getParameter("description");
            String statusID = requestInstance.getParameter("status_id");

            if (StringUtils.equalsIgnoreCase(methodID, EDIT_ELIGIBILITY_CRITERIA_METHOD_ID)) {
                return editEligibilityCriteria(criteriaID, description, statusID, userDetailsBeanInstance,
                        requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, ADD_ELIGIBILITY_CRITERIA_METHOD_ID)) {
                return addEligibilityCriteria(description, statusID, userDetailsBeanInstance, requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, DELETE_ELIGIBILITY_CRITERIA_METHOD_ID)) {
                return deleteEligibilityCriteria(criteriaID, requestInstance);
            }
        } catch (Exception e) {
            Result operationResult = new Result();
            operationResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
            LOG.error("Exception in Eligibility Criteria Manage Service.", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(operationResult);
            return operationResult;
        }

        return new Result();
    }

    /**
     * Method to edit the Eligibility Criteria
     * 
     * @param criteriaID
     * @param description
     * @param statusID
     * @param userDetailsBeanInstance
     * @param requestInstance
     * @return operation Result
     */
    private Result editEligibilityCriteria(String criteriaID, String description, String statusID,
            UserDetailsBean userDetailsBeanInstance, DataControllerRequest requestInstance) {

        Result operationResult = new Result();
        try {

            // Validate Inputs
            if (StringUtils.isBlank(criteriaID)) {
                LOG.error("Missing Mandatory Input: Criteria ID");
                operationResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
                operationResult
                        .addParam(new Param("message", "Criteria ID is a mandatory Input", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20942.setErrorCode(operationResult);
                return operationResult;
            }
            if (StringUtils.isNotBlank(description)) {
                if (StringUtils.isBlank(description) || description.length() < DESCRIPTION_MIN_CHARS) {
                    LOG.error("Description length does meet the critera. Exepcted Minimum number of characters:"
                            + DESCRIPTION_MIN_CHARS);
                    ErrorCodeEnum.ERR_20942.setErrorCode(operationResult);
                    operationResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    operationResult.addParam(new Param("message",
                            "Description should have a miniumum of " + DESCRIPTION_MIN_CHARS + " characters",
                            FabricConstants.STRING));
                    return operationResult;
                }
            }

            // Prepare Input Map
            Map<String, String> inputBodyMap = new HashMap<String, String>();
            inputBodyMap.put("id", criteriaID);
            if (StringUtils.isNotBlank(statusID)) {
                inputBodyMap.put("Description", description);
            }
            if (StringUtils.isNotBlank(statusID)) {
                inputBodyMap.put("Status_id", statusID);
            }
            inputBodyMap.put("modifiedby", userDetailsBeanInstance.getUserName());
            inputBodyMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

            // Edit Eligibility Criteria
            String editEligibilityCriteriaResponse = Executor.invokeService(ServiceURLEnum.ELIGIBILITYCRITERIA_UPDATE,
                    inputBodyMap, null, requestInstance);

            // Check Operation Response
            JSONObject editEligibilityCriteriaResponseJSON = CommonUtilities
                    .getStringAsJSONObject(editEligibilityCriteriaResponse);
            if (editEligibilityCriteriaResponseJSON != null
                    && editEligibilityCriteriaResponseJSON.has(FabricConstants.OPSTATUS)
                    && editEligibilityCriteriaResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                LOG.debug("Successful CRUD Operation");
                operationResult.addParam(new Param("status", "Success", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.BUSINESSCONFIGURATION, EventEnum.UPDATE,
                        ActivityStatusEnum.SUCCESSFUL, "Edit Successful for Eligibility Criteria:" + criteriaID);
            } else {
                LOG.error("Failed CRUD Operation.Response:" + editEligibilityCriteriaResponse);
                operationResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.BUSINESSCONFIGURATION, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "Edit Failed for Eligibility Criteria:" + criteriaID);
                ErrorCodeEnum.ERR_20944.setErrorCode(operationResult);
            }
        } catch (Exception e) {
            operationResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
            LOG.error("Exception in editing eligibility criteria.", e);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.BUSINESSCONFIGURATION, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Edit Failed for Eligibility Criteria:" + criteriaID);
            ErrorCodeEnum.ERR_20944.setErrorCode(operationResult);
        }
        return operationResult;
    }

    /**
     * Method to add an Eligibility Criteria
     * 
     * @param description
     * @param statusID
     * @param userDetailsBeanInstance
     * @param requestInstance
     * @return operation Result
     */
    private Result addEligibilityCriteria(String description, String statusID, UserDetailsBean userDetailsBeanInstance,
            DataControllerRequest requestInstance) {

        Result operationResult = new Result();
        try {
            // Validate Inputs
            if (StringUtils.isBlank(description) || description.length() < DESCRIPTION_MIN_CHARS) {
                LOG.error("Description length does meet the critera. Exepcted Minimum number of characters:"
                        + DESCRIPTION_MIN_CHARS);
                ErrorCodeEnum.ERR_20942.setErrorCode(operationResult);
                operationResult.addParam(new Param("message",
                        "Description should have a miniumum of " + DESCRIPTION_MIN_CHARS + " characters",
                        FabricConstants.STRING));
                operationResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return operationResult;
            }
            if (StringUtils.isBlank(statusID)) {
                statusID = ELIGIBILITY_CRITERIA_DEFAULT_STATUS_ID;
            }

            // Prepare Input Map
            String criteriaID = CommonUtilities.getNewId().toString();
            Map<String, String> inputBodyMap = new HashMap<String, String>();
            inputBodyMap.put("id", criteriaID);
            inputBodyMap.put("createdby", userDetailsBeanInstance.getUserName());
            inputBodyMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
            inputBodyMap.put("Description", description);
            inputBodyMap.put("Status_id", statusID);

            // Create Eligibility Criteria
            String createEligibilityCriteriaResponse = Executor.invokeService(ServiceURLEnum.ELIGIBILITYCRITERIA_CREATE,
                    inputBodyMap, null, requestInstance);

            // Check Operation Response
            JSONObject createEligibilityCriteriaResponseJSON = CommonUtilities
                    .getStringAsJSONObject(createEligibilityCriteriaResponse);
            if (createEligibilityCriteriaResponseJSON != null
                    && createEligibilityCriteriaResponseJSON.has(FabricConstants.OPSTATUS)
                    && createEligibilityCriteriaResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                LOG.debug("Successful CRUD Operation");
                operationResult.addParam(new Param("status", "Success", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.BUSINESSCONFIGURATION, EventEnum.CREATE,
                        ActivityStatusEnum.SUCCESSFUL, "Create Successful for Eligibility Criteria:" + criteriaID);
            } else {
                LOG.error("Failed CRUD Operation.Response:" + createEligibilityCriteriaResponse);
                operationResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20943.setErrorCode(operationResult);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.BUSINESSCONFIGURATION, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED, " Eligibility Criteria Create Failed");
            }
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20943.setErrorCode(operationResult);
            LOG.error("Exception in creating eligibility criteria.", e);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.BUSINESSCONFIGURATION, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, " Eligibility Criteria Create Failed");
        }
        return operationResult;
    }

    /**
     * Method to delete an Eligibility Criteria
     * 
     * @param criteriaID
     * @param requestInstance
     * @return operation Result
     */
    private Result deleteEligibilityCriteria(String criteriaID, DataControllerRequest requestInstance) {

        Result operationResult = new Result();
        try {
            // Validate Inputs
            if (StringUtils.isBlank(criteriaID)) {
                LOG.debug("Missing Mandatory Input: Criteria ID");
                operationResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
                operationResult
                        .addParam(new Param("message", "Criteria ID is a mandatory Input", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20942.setErrorCode(operationResult);
                return operationResult;
            }

            // Prepare Input Map
            Map<String, String> inputBodyMap = new HashMap<String, String>();
            inputBodyMap.put("id", criteriaID);

            // Delete Eligibility Criteria
            String deleteEligibilityCriteriaResponse = Executor.invokeService(ServiceURLEnum.ELIGIBILITYCRITERIA_DELETE,
                    inputBodyMap, null, requestInstance);

            // Check Operation Status
            JSONObject deleteEligibilityCriteriaResponseJSON = CommonUtilities
                    .getStringAsJSONObject(deleteEligibilityCriteriaResponse);
            if (deleteEligibilityCriteriaResponseJSON != null
                    && deleteEligibilityCriteriaResponseJSON.has(FabricConstants.OPSTATUS)
                    && deleteEligibilityCriteriaResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                LOG.debug("Successful CRUD Operation");
                operationResult.addParam(new Param("status", "Success", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.BUSINESSCONFIGURATION, EventEnum.DELETE,
                        ActivityStatusEnum.SUCCESSFUL, "Delete Successful for Eligibility Criteria:" + criteriaID);
            } else {
                LOG.error("Failed CRUD Operation.Response:" + deleteEligibilityCriteriaResponse);
                operationResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.BUSINESSCONFIGURATION, EventEnum.DELETE,
                        ActivityStatusEnum.FAILED, "Delete Failed for Eligibility Criteria:" + criteriaID);
                ErrorCodeEnum.ERR_20945.setErrorCode(operationResult);
            }

        } catch (Exception e) {
            ErrorCodeEnum.ERR_20945.setErrorCode(operationResult);
            LOG.error("Exception in deleting eligibility criteria.", e);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.BUSINESSCONFIGURATION, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "Delete Failed for Eligibility Criteria:" + criteriaID);
        }
        return operationResult;
    }
}