package com.kony.adminconsole.service.useraccessrestriction;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.RoleHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class RoleToGroupMappingEdit implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(RoleToGroupMappingEdit.class);
    private static final String INPUT_INTERNAL_ROLE_ID = "InternalRole_id";
    private static final String INPUT_ADDED_ROLES = "AddedRoles";
    private static final String INPUT_REMOVED_ROLES = "RemovedRoles";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        String inputCurrentInternalRole = requestInstance.getParameter(INPUT_INTERNAL_ROLE_ID);
        String inputAddedRoles = requestInstance.getParameter(INPUT_ADDED_ROLES);
        String inputRemovedRoles = requestInstance.getParameter(INPUT_REMOVED_ROLES);

        if (StringUtils.isBlank(inputCurrentInternalRole)) {
            ErrorCodeEnum.ERR_21596.setErrorCode(processedResult);
            return processedResult;
        }

        if (StringUtils.isBlank(inputAddedRoles)) {
            ErrorCodeEnum.ERR_21594.setErrorCode(processedResult);
            return processedResult;
        }

        if (StringUtils.isBlank(inputRemovedRoles)) {
            ErrorCodeEnum.ERR_21595.setErrorCode(processedResult);
            return processedResult;
        }

        try {
            JSONArray addedRoles = new JSONArray(inputAddedRoles);
            JSONArray removedRoles = new JSONArray(inputRemovedRoles);

            RoleHandler.editMappingForUserroleToCustomerRole(inputCurrentInternalRole, addedRoles, removedRoles,
                    processedResult, requestInstance);
            processedResult.addParam(new Param("Status", "Successful", FabricConstants.STRING));
            return processedResult;
        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;

        } catch (Exception e) {
            LOG.error("Exception occurred while processing roles to groups mapping edit service ", e);
            ErrorCodeEnum.ERR_21592.setErrorCode(processedResult);
            return processedResult;
        }
    }
}
