package com.kony.adminconsole.service.staticcontentmanagement.termsandconditions;

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
 * Service to Update TermsAndConditions
 *
 * @author Aditya Mankal
 * 
 */
public class TermsAndConditionsEditService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(TermsAndConditionsEditService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();

            Map<String, String> postParametersMap = new HashMap<String, String>();

            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            String userID = userDetailsBeanInstance.getUserId();
            JSONObject termsAndConditionsDataJSON = CommonUtilities
                    .getStringAsJSONObject(requestInstance.getParameter("TermsAndConditionsData"));

            // Call to fetch the ID value of the existing Terms And Conditions Data
            postParametersMap.clear();
            postParametersMap.put(ODataQueryConstants.SELECT, "id");
            String readTermsAndConditionsResponse = Executor.invokeService(ServiceURLEnum.TERMSANDCONDITIONS_READ,
                    postParametersMap, null, requestInstance);
            JSONObject readTermsAndConditionsResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readTermsAndConditionsResponse);

            if (readTermsAndConditionsResponseJSON != null
                    && readTermsAndConditionsResponseJSON.has(FabricConstants.OPSTATUS)
                    && readTermsAndConditionsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                String termsAndConditionsID = "";
                if (readTermsAndConditionsResponseJSON.has("termsandconditions")) {
                    JSONArray termsAndConditionsJSONArray = readTermsAndConditionsResponseJSON
                            .getJSONArray("termsandconditions");
                    if (termsAndConditionsJSONArray.length() >= 1) {
                        JSONObject termsAndConditionsJSONObject = termsAndConditionsJSONArray.getJSONObject(0);
                        termsAndConditionsID = termsAndConditionsJSONObject.getString("id");
                    }
                }
                postParametersMap.clear();

                postParametersMap.put("id", termsAndConditionsID);
                if (termsAndConditionsDataJSON.has("Channel_id"))
                    postParametersMap.put("Channel_id", termsAndConditionsDataJSON.getString("Channel_id"));

                if (termsAndConditionsDataJSON.has("Description")) {
                    postParametersMap.put("Description", termsAndConditionsDataJSON.getString("Description"));
                    postParametersMap.put("rtx", "Description");
                }

                if (termsAndConditionsDataJSON.has("Status_id"))
                    postParametersMap.put("Status_id", termsAndConditionsDataJSON.getString("Status_id"));
                else
                    postParametersMap.put("Status_id", StatusEnum.SID_ACTIVE.name());

                postParametersMap.put("modifiedby", userID);
                postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
                Param updateTermsAndConditionsStatusParam = new Param("TermsAndConditionsEditStatus", "Successful",
                        FabricConstants.STRING);
                String updateTermsAndConditionsResponse = Executor.invokeService(
                        ServiceURLEnum.TERMSANDCONDITIONS_UPDATE, postParametersMap, null, requestInstance);
                JSONObject updateTermsAndConditionsResponseJSON = CommonUtilities
                        .getStringAsJSONObject(updateTermsAndConditionsResponse);
                if (updateTermsAndConditionsResponseJSON != null
                        && updateTermsAndConditionsResponseJSON.has(FabricConstants.OPSTATUS)
                        && updateTermsAndConditionsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                        && updateTermsAndConditionsResponseJSON.has("updatedRecords")) {

                    if (updateTermsAndConditionsResponseJSON.getInt("updatedRecords") >= 1) {
                        // Success
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                                ActivityStatusEnum.SUCCESSFUL,
                                "Terms and Conditions update successful. TandC_ID: " + termsAndConditionsID);
                    } else {
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                                ActivityStatusEnum.FAILED,
                                "Terms and Conditions update failed. TandC_ID: " + termsAndConditionsID);
                        updateTermsAndConditionsStatusParam.setValue("Failed");
                    }
                    processedResult.addParam(updateTermsAndConditionsStatusParam);
                    return processedResult;
                }
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Terms and Conditions update failed. TandC_ID: " + termsAndConditionsID);
                ErrorCodeEnum.ERR_20262.setErrorCode(processedResult);
                return processedResult;
            }
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TANDC, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Terms and Conditions update failed.");
            ErrorCodeEnum.ERR_20262.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

}