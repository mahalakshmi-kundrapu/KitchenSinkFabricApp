package com.kony.adminconsole.service.staticcontentmanagement.securityquestions;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
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
 * Service to Create Security Questions
 *
 * @author Aditya Mankal
 * 
 */
public class SecurityQuestionsCreateService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(SecurityQuestionsCreateService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            int successfulInserts = 0, failedInserts = 0;
            Map<String, String> postParametersMap = new HashMap<String, String>();
            Result processedResult = new Result();
            Param opStatusCodeParam = new Param();
            opStatusCodeParam.setName(FabricConstants.OPSTATUS);
            opStatusCodeParam.setType(FabricConstants.INT);

            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            String userID = userDetailsBeanInstance.getUserId();

            JSONArray securityQuestionsJSONArray = new JSONArray(requestInstance.getParameter("SecurityQuestionsList"));

            for (int indexVar = 0; indexVar < securityQuestionsJSONArray.length(); indexVar++) {
                postParametersMap.clear();
                try {
                    JSONObject currQuestionJSONObject = securityQuestionsJSONArray.getJSONObject(indexVar);
                    String questionID = CommonUtilities.getNewId().toString();
                    postParametersMap.put("id", questionID);
                    if (currQuestionJSONObject.has("Question"))
                        postParametersMap.put("Question", currQuestionJSONObject.getString("Question"));
                    if (currQuestionJSONObject.has("Status_id"))
                        postParametersMap.put("Status_id", currQuestionJSONObject.getString("Status_id"));
                    else
                        postParametersMap.put("Status_id", StatusEnum.SID_ACTIVE.name());
                    postParametersMap.put("createdby", userID);
                    postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
                    String insertOperationResponse = Executor.invokeService(ServiceURLEnum.SECURITYQUESTION_CREATE,
                            postParametersMap, null, requestInstance);
                    JSONObject insertOperationResponseJSON = CommonUtilities
                            .getStringAsJSONObject(insertOperationResponse);
                    if (insertOperationResponseJSON != null && insertOperationResponseJSON.has(FabricConstants.OPSTATUS)
                            && insertOperationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYQUESTIONS,
                                EventEnum.CREATE, ActivityStatusEnum.SUCCESSFUL,
                                "Secure questions create success. questionID: " + questionID);
                        successfulInserts++;
                    } else {
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYQUESTIONS,
                                EventEnum.CREATE, ActivityStatusEnum.FAILED,
                                "Secure questions create failed. questionID: " + questionID);
                        failedInserts++;
                    }
                } catch (JSONException e) {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYQUESTIONS, EventEnum.CREATE,
                            ActivityStatusEnum.FAILED, "Secure questions create failed. errmsg" + e.getMessage());
                    failedInserts++;
                }
            }
            processedResult.addParam(
                    new Param("Successful Insert Count", Integer.toString(successfulInserts), FabricConstants.STRING));
            processedResult.addParam(
                    new Param("Failed Insert Count", Integer.toString(failedInserts), FabricConstants.STRING));
            if (failedInserts > 0) {
                ErrorCodeEnum.ERR_20241.setErrorCode(processedResult);
            }
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }
}