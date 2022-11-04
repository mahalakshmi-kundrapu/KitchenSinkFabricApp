package com.kony.adminconsole.service.staticcontentmanagement.privacypolicy;

import java.util.HashMap;
import java.util.Map;

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
 * Service to create Privacy Policy
 *
 * @author Aditya Mankal
 * 
 */

public class PrivacyPolicyCreateService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(PrivacyPolicyCreateService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Map<String, String> postParametersMap = new HashMap<String, String>();

            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            String userID = userDetailsBeanInstance.getUserId();
            JSONObject privacyPolicyDataJSON = CommonUtilities
                    .getStringAsJSONObject(requestInstance.getParameter("PrivacyPolicyData"));

            String privacyPolicyID = CommonUtilities.getNewId().toString();
            postParametersMap.put("id", privacyPolicyID);

            if (privacyPolicyDataJSON.has("Channel_id"))
                postParametersMap.put("Channel_id", privacyPolicyDataJSON.getString("Channel_id"));

            if (privacyPolicyDataJSON.has("Description")) {
                postParametersMap.put("Description", privacyPolicyDataJSON.getString("Description"));
                postParametersMap.put("rtx", "Description");
            }

            if (privacyPolicyDataJSON.has("Status_id"))
                postParametersMap.put("Status_id", privacyPolicyDataJSON.getString("Status_id"));
            else
                postParametersMap.put("Status_id", StatusEnum.SID_ACTIVE.name());

            postParametersMap.put("createdby", userID);
            postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());

            Result processedResult = new Result();
            String createPrivacyPolicyResponse = Executor.invokeService(ServiceURLEnum.PRIVACYPOLICY_CREATE,
                    postParametersMap, null, requestInstance);
            JSONObject createPrivacyPolicyResponseJSON = CommonUtilities
                    .getStringAsJSONObject(createPrivacyPolicyResponse);
            if (createPrivacyPolicyResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                if (createPrivacyPolicyResponseJSON.has("privacypolicy")) {
                    // Success
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PRIVACYPOLICIES, EventEnum.CREATE,
                            ActivityStatusEnum.SUCCESSFUL,
                            "Privacy policy create success. PrivacyPolicyID: " + privacyPolicyID);
                    Param createPrivacyPolicySuccessParam = new Param("PrivacyPolicyCreateStatus", "Successful",
                            FabricConstants.STRING);
                    processedResult.addParam(createPrivacyPolicySuccessParam);

                } else {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PRIVACYPOLICIES, EventEnum.CREATE,
                            ActivityStatusEnum.FAILED,
                            "Privacy policy create failed. PrivacyPolicyID: " + privacyPolicyID);
                    Param createPrivacyPolicyFailureParam = new Param("PrivacyPolicyCreateStatus", "Failed",
                            FabricConstants.STRING);
                    processedResult.addParam(createPrivacyPolicyFailureParam);
                }
                return processedResult;
            }
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PRIVACYPOLICIES, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "Privacy policy create failed. PrivacyPolicyID: " + privacyPolicyID);
            ErrorCodeEnum.ERR_20201.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

}