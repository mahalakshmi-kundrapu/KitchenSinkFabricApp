package com.kony.adminconsole.service.staticcontentmanagement.termsandconditions;

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
 * Service to Insert TermsAndConditions
 *
 * @author Aditya Mankal
 * 
 */
public class TermsAndConditionsCreateService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(TermsAndConditionsCreateService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Map<String, String> postParametersMap = new HashMap<String, String>();
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            String userID = userDetailsBeanInstance.getUserId();
            JSONObject termsAndConditionsDataJSON = CommonUtilities
                    .getStringAsJSONObject(requestInstance.getParameter("TermsAndConditionsData"));

            String termsAndConditionsID = CommonUtilities.getNewId().toString();
            postParametersMap.put("id", termsAndConditionsID);

            if (termsAndConditionsDataJSON.has("Channel_id"))
                postParametersMap.put("Channel_id", termsAndConditionsDataJSON.getString("Channel_id"));

            if (termsAndConditionsDataJSON.has("Service_id"))
                postParametersMap.put("Service_id", termsAndConditionsDataJSON.getString("Service_id"));

            if (termsAndConditionsDataJSON.has("Description")) {
                postParametersMap.put("Description", termsAndConditionsDataJSON.getString("Description"));
                postParametersMap.put("rtx", "Description");
            }

            if (termsAndConditionsDataJSON.has("Status_id"))
                postParametersMap.put("Status_id", termsAndConditionsDataJSON.getString("Status_id"));
            else
                postParametersMap.put("Status_id", StatusEnum.SID_ACTIVE.name());

            postParametersMap.put("createdby", userID);
            postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
            Result processedResult = new Result();

            String createTermsAndConditionsResponse = Executor.invokeService(ServiceURLEnum.TERMSANDCONDITIONS_CREATE,
                    postParametersMap, null, requestInstance);
            JSONObject createTermsAndConditionsResponseJSON = CommonUtilities
                    .getStringAsJSONObject(createTermsAndConditionsResponse);
            if (createTermsAndConditionsResponseJSON != null
                    && createTermsAndConditionsResponseJSON.has(FabricConstants.OPSTATUS)
                    && createTermsAndConditionsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                if (createTermsAndConditionsResponseJSON.has("termsandconditions")) {
                    // Success
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                            ActivityStatusEnum.SUCCESSFUL,
                            "Terms and Conditions create successful. TandC_ID: " + termsAndConditionsID);

                    Param createTermsAndConditionsSuccessParam = new Param("TermsAndConditionsCreateStatus",
                            "Successful", FabricConstants.STRING);
                    processedResult.addParam(createTermsAndConditionsSuccessParam);

                } else {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                            ActivityStatusEnum.FAILED,
                            "Terms and Conditions create failed. TandC_ID: " + termsAndConditionsID);
                    Param createTermsAndConditionsFailureParam = new Param("TermsAndConditionsCreateStatus", "Failed",
                            FabricConstants.STRING);
                    processedResult.addParam(createTermsAndConditionsFailureParam);
                }
                return processedResult;
            }
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "Terms and Conditions create failed. TandC_ID: " + termsAndConditionsID);
            ErrorCodeEnum.ERR_20261.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

}