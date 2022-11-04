package com.kony.adminconsole.service.staticcontentmanagement.termsandconditions;

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
 * Service to Delete TermsAndConditions
 *
 * @author Aditya Mankal
 * 
 */
public class TermsAndConditionsDeleteService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(TermsAndConditionsDeleteService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {

            Result processedResult = new Result();

            Map<String, String> postParametersMap = new HashMap<String, String>();
            String termsAndConditionsID = requestInstance.getParameter("id");

            postParametersMap.clear();
            postParametersMap.put("id", termsAndConditionsID);
            String deleteTermsAndConditionsResponse = Executor.invokeService(ServiceURLEnum.TERMSANDCONDITIONS_DELETE,
                    postParametersMap, null, requestInstance);
            JSONObject deleteTermsAndConditionsResponseJSON = CommonUtilities
                    .getStringAsJSONObject(deleteTermsAndConditionsResponse);
            Param deleteTermsAndConditionsStatusParam = new Param("TermsAndConditionsDeleteStatus", "Successful",
                    FabricConstants.STRING);
            if (deleteTermsAndConditionsResponseJSON.has(FabricConstants.OPSTATUS)
                    && deleteTermsAndConditionsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                if (deleteTermsAndConditionsResponseJSON.has("deletedRecords")
                        && deleteTermsAndConditionsResponseJSON.getInt("deletedRecords") >= 1) {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.DELETE,
                            ActivityStatusEnum.SUCCESSFUL,
                            "Terms and Conditions create successful. TandC_ID: " + termsAndConditionsID);
                } else {
                    deleteTermsAndConditionsStatusParam.setValue("Failed");
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.DELETE,
                            ActivityStatusEnum.FAILED,
                            "Terms and Conditions delete failed. TandC_ID: " + termsAndConditionsID);
                }

                processedResult.addParam(deleteTermsAndConditionsStatusParam);
                return processedResult;
            }
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "Terms and Conditions delete failed. TandC_ID: " + termsAndConditionsID);
            ErrorCodeEnum.ERR_20263.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

}