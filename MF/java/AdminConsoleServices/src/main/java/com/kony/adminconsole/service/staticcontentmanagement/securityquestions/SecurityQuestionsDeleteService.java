package com.kony.adminconsole.service.staticcontentmanagement.securityquestions;

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
 * Service to Delete Security Questions
 *
 * @author Aditya Mankal
 * 
 */
public class SecurityQuestionsDeleteService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(SecurityQuestionsDeleteService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();

            Map<String, String> postParametersMap = new HashMap<String, String>();

            String questionID = requestInstance.getParameter("id");
            postParametersMap.put("id", questionID);
            String deleteSecurityQuestionsResponse = Executor.invokeService(ServiceURLEnum.SECURITYQUESTION_DELETE,
                    postParametersMap, null, requestInstance);
            JSONObject deleteSecurityQuestionsResponseJSON = CommonUtilities
                    .getStringAsJSONObject(deleteSecurityQuestionsResponse);
            if (deleteSecurityQuestionsResponseJSON != null
                    && deleteSecurityQuestionsResponseJSON.has(FabricConstants.OPSTATUS)
                    && deleteSecurityQuestionsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {

                if (deleteSecurityQuestionsResponseJSON.has("deletedRecords")) {
                    Param deletedRecords_Param = new Param("deletedRecords",
                            Integer.toString(deleteSecurityQuestionsResponseJSON.getInt("deletedRecords")),
                            FabricConstants.INT);
                    processedResult.addParam(deletedRecords_Param);
                    if (deleteSecurityQuestionsResponseJSON.getInt("deletedRecords") >= 1)
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYQUESTIONS,
                                EventEnum.DELETE, ActivityStatusEnum.SUCCESSFUL,
                                "Secure questions delete success. questionID: " + questionID);
                }

                return processedResult;
            }
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SECURITYQUESTIONS, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "Secure questions delete failed. questionID: " + questionID);
            ErrorCodeEnum.ERR_20243.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}