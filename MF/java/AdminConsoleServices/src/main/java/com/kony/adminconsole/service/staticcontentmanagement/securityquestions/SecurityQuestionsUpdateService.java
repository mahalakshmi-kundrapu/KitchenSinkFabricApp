package com.kony.adminconsole.service.staticcontentmanagement.securityquestions;

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
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to Update(Activate/Deactivate) Security Questions
 *
 * @author Aditya Mankal
 * 
 */
public class SecurityQuestionsUpdateService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(SecurityQuestionsUpdateService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            String userID = userDetailsBeanInstance.getUserId();

            Map<String, String> postParametersMap = new HashMap<String, String>();

            String questionID = requestInstance.getParameter("id");
            String statusID = requestInstance.getParameter("status_ID");
            postParametersMap.put("id", questionID);
            postParametersMap.put("Status_id", statusID);
            postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            postParametersMap.put("modifiedby", userID);

            String updateSecurityQuestionsResponse = Executor.invokeService(ServiceURLEnum.SECURITYQUESTION_UPDATE,
                    postParametersMap, null, requestInstance);
            JSONObject updateSecurityQuestionsResponseJSON = CommonUtilities
                    .getStringAsJSONObject(updateSecurityQuestionsResponse);
            if (updateSecurityQuestionsResponseJSON != null
                    && updateSecurityQuestionsResponseJSON.has(FabricConstants.OPSTATUS)
                    && updateSecurityQuestionsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {

                if (updateSecurityQuestionsResponseJSON.has("updatedRecords")) {
                    Param updatedRecords_Param = new Param("updatedRecords",
                            Integer.toString(updateSecurityQuestionsResponseJSON.getInt("updatedRecords")),
                            FabricConstants.INT);
                    processedResult.addParam(updatedRecords_Param);
                    if (updateSecurityQuestionsResponseJSON.getInt("updatedRecords") >= 1) {
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYQUESTIONS,
                                EventEnum.UPDATE, ActivityStatusEnum.SUCCESSFUL,
                                "Secure questions update success. questionID: " + questionID);
                    }
                }
                return processedResult;
            }
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYQUESTIONS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Secure questions update failed. questionID: " + questionID);
            ErrorCodeEnum.ERR_20242.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}