package com.kony.adminconsole.service.staticcontentmanagement.privacypolicy;

import java.util.HashMap;
import java.util.Map;

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

/**
 * Service to Delete Privacy Policy
 *
 * @author Aditya Mankal
 * 
 */
public class PrivacyPolicyDeleteService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(PrivacyPolicyDeleteService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Map<String, String> postParametersMap = new HashMap<String, String>();

            String privacyPolicyID = requestInstance.getParameter("id");
            Result processedResult = new Result();
            postParametersMap.clear();
            postParametersMap.put("id", privacyPolicyID);
            String deletePrivacyPolicyResponse = Executor.invokeService(ServiceURLEnum.PRIVACYPOLICY_DELETE,
                    postParametersMap, null, requestInstance);
            JSONObject deletePrivacyPolicyResponseJSON = CommonUtilities
                    .getStringAsJSONObject(deletePrivacyPolicyResponse);
            Param deletePrivacyPolicyStatusParam = new Param("PrivacyPolicyDeleteStatus", "Successful",
                    FabricConstants.STRING);
            if (deletePrivacyPolicyResponseJSON.has(FabricConstants.OPSTATUS)
                    && deletePrivacyPolicyResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                if (deletePrivacyPolicyResponseJSON.has("deletedRecords")
                        && deletePrivacyPolicyResponseJSON.getInt("deletedRecords") >= 1) {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PRIVACYPOLICIES, EventEnum.DELETE,
                            ActivityStatusEnum.SUCCESSFUL,
                            "Privacy policy delete failed. PrivacyPolicyID: " + privacyPolicyID);
                } else {
                    deletePrivacyPolicyStatusParam.setValue("Failed");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PRIVACYPOLICIES, EventEnum.DELETE,
                            ActivityStatusEnum.FAILED,
                            "Privacy policy delete failed. PrivacyPolicyID: " + privacyPolicyID);
                }

                processedResult.addParam(deletePrivacyPolicyStatusParam);
                return processedResult;
            }
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PRIVACYPOLICIES, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "Privacy policy delete failed. PrivacyPolicyID: " + privacyPolicyID);
            ErrorCodeEnum.ERR_20203.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

}