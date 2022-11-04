package com.kony.adminconsole.service.staticcontentmanagement.privacypolicy;

import java.util.HashMap;
import java.util.Map;

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
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to Update Privacy Policy
 *
 * @author Aditya Mankal
 * 
 */
public class PrivacyPolicyEditService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(PrivacyPolicyEditService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();

            Map<String, String> postParametersMap = new HashMap<String, String>();

            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            String userID = userDetailsBeanInstance.getUserId();
            JSONObject privacyPolicyDataJSON = CommonUtilities
                    .getStringAsJSONObject(requestInstance.getParameter("PrivacyPolicyData"));

            // Call to fetch the ID value of the Privacy Policy

            postParametersMap.clear();
            postParametersMap.put(ODataQueryConstants.SELECT, "id");
            String readPrivacyPolicyResponse = Executor.invokeService(ServiceURLEnum.PRIVACYPOLICY_READ,
                    postParametersMap, null, requestInstance);
            JSONObject readPrivacyPolicyResponseJSON = CommonUtilities.getStringAsJSONObject(readPrivacyPolicyResponse);

            if (readPrivacyPolicyResponseJSON.has(FabricConstants.OPSTATUS)
                    && readPrivacyPolicyResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                String privacyPolicyID = "";
                if (readPrivacyPolicyResponseJSON.has("privacypolicy")) {
                    JSONArray privacyPolicyJSONArray = readPrivacyPolicyResponseJSON.getJSONArray("privacypolicy");
                    if (privacyPolicyJSONArray.length() >= 1) {
                        JSONObject policyJSONObject = privacyPolicyJSONArray.getJSONObject(0);
                        privacyPolicyID = policyJSONObject.getString("id");
                    }
                }
                postParametersMap.clear();

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

                postParametersMap.put("modifiedby", userID);
                postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

                Param updatePrivacyPolicyStatusParam = new Param("PrivacyPolicyEditStatus", "Successful",
                        FabricConstants.STRING);
                String updatePrivacyPolicyResponse = Executor.invokeService(ServiceURLEnum.PRIVACYPOLICY_UPDATE,
                        postParametersMap, null, requestInstance);
                JSONObject updatePrivacyPolicyResponseJSON = CommonUtilities
                        .getStringAsJSONObject(updatePrivacyPolicyResponse);
                if (updatePrivacyPolicyResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                        && updatePrivacyPolicyResponseJSON.has("updatedRecords")) {

                    if (updatePrivacyPolicyResponseJSON.getInt("updatedRecords") >= 1) {
                        // Success
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PRIVACYPOLICIES,
                                EventEnum.UPDATE, ActivityStatusEnum.SUCCESSFUL,
                                "Privacy policy update success. PrivacyPolicyID: " + privacyPolicyID);
                    } else {
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PRIVACYPOLICIES,
                                EventEnum.UPDATE, ActivityStatusEnum.FAILED,
                                "Privacy policy update failed. PrivacyPolicyID: " + privacyPolicyID);
                        updatePrivacyPolicyStatusParam.setValue("Failed");
                    }
                    processedResult.addParam(updatePrivacyPolicyStatusParam);
                    return processedResult;
                }
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PRIVACYPOLICIES, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "Privacy policy update failed. PrivacyPolicyID: " + privacyPolicyID);
                ErrorCodeEnum.ERR_20202.setErrorCode(processedResult);
                return processedResult;
            }
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PRIVACYPOLICIES, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Privacy policy update failed.");
            ErrorCodeEnum.ERR_20202.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

}