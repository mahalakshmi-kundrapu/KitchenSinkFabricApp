package com.kony.adminconsole.service.staticcontentmanagement.policies;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
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
import com.konylabs.middleware.dataobject.Result;

public class PolicyUpdateService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(PolicyUpdateService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {

            Result result = new Result();
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            Param statusParam;
            String policyId = "";
            String policyDescription = "";
            String systemUser = "";
            if (requestInstance.getParameter("id") != null) {
                policyId = requestInstance.getParameter("id");
                if (StringUtils.isBlank(policyId)) {
                    ErrorCodeEnum.ERR_20761.setErrorCode(result);
                    return result;
                }
            } else {
                ErrorCodeEnum.ERR_20762.setErrorCode(result);
                return result;
            }
            if (requestInstance.getParameter("policyDescription") != null) {
                policyDescription = requestInstance.getParameter("policyDescription");
                if (StringUtils.isBlank(policyId)) {
                    ErrorCodeEnum.ERR_20763.setErrorCode(result);
                    return result;
                }
            } else {
                ErrorCodeEnum.ERR_20764.setErrorCode(result);
                return result;
            }
            if (requestInstance.getParameter("systemUser") != null) {
                systemUser = requestInstance.getParameter("systemUser");
            }
            try {
                // update
                JSONObject updateResponseJSON = updatePolicyStatus(policyId, policyDescription, authToken, systemUser,
                        requestInstance);
                if (updateResponseJSON != null && updateResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    ErrorCodeEnum.ERR_20765.setErrorCode(result);
                    return result;
                }
            } catch (Exception e) {
                ErrorCodeEnum.ERR_20765.setErrorCode(result);
                statusParam = new Param("Status", "Error", FabricConstants.STRING);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CREDENTIALPOLICIES, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "Policies update failed. PolicyID: " + policyId);
                result.addParam(statusParam);
            }
            statusParam = new Param("Status", "UPDATE SUCCESSFUL", FabricConstants.STRING);
            result.addParam(statusParam);
            return result;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    private JSONObject updatePolicyStatus(String policyId, String policyDescription, String authToken,
            String systemUser, DataControllerRequest requestInstance) {
        JSONObject updateResponseJSON = null;
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        postParametersMap.put("id", policyId);
        postParametersMap.put("PolicyDescription", policyDescription);
        postParametersMap.put("rtx", "PolicyDescription");
        postParametersMap.put("modifiedby", systemUser);
        postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

        String updatePeriodicLimitResponse = Executor.invokeService(ServiceURLEnum.POLICIES_UPDATE, postParametersMap,
                null, requestInstance);

        updateResponseJSON = CommonUtilities.getStringAsJSONObject(updatePeriodicLimitResponse);
        if (updateResponseJSON != null && updateResponseJSON.has(FabricConstants.OPSTATUS)
                && updateResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CREDENTIALPOLICIES, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL, "Policies update success. PolicyID: " + policyId);
            return updateResponseJSON;
        }

        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CREDENTIALPOLICIES, EventEnum.UPDATE,
                ActivityStatusEnum.FAILED, "Policies update failed. PolicyID: " + policyId);
        return updateResponseJSON;

    }
}