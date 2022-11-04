package com.kony.adminconsole.service.customermanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * This service will fetch the entitlements for a given customer Used by OLB, MB, SBB client applications
 * 
 * @author Alahari Prudhvi Akhil
 * 
 */

public class CustomerEntitlementsGet implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerEntitlementsGet.class);
    private static final String INPUT_USERNAME = "userName";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        String userName = requestInstance.getParameter(INPUT_USERNAME);
        CustomerHandler customerHandler = new CustomerHandler();

        try {
            if (StringUtils.isBlank(userName)) {
                ErrorCodeEnum.ERR_20612.setErrorCode(processedResult);
                return processedResult;
            }
            // Fetch customer id from username
            String customerId = customerHandler.getCustomerId(userName, requestInstance);
            if (StringUtils.isBlank(customerId)) {
                ErrorCodeEnum.ERR_20613.setErrorCode(processedResult);
                return processedResult;
            }

            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put("customerId", customerId);
            String readEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_ENTITLEMENTS_ORCHESTRATION,
                    postParametersMap, null, requestInstance);
            JSONObject readResponse = CommonUtilities.getStringAsJSONObject(readEndpointResponse);
            if (!readResponse.has(FabricConstants.OPSTATUS) || readResponse.getInt(FabricConstants.OPSTATUS) != 0
                    || !readResponse.has("Addresses") || !readResponse.has("EmailIds")
                    || !readResponse.has("ContactNumbers") || !readResponse.has("isSecurityQuestionConfigured")
                    || !readResponse.has("services")) {
                processedResult.addParam(new Param("FailureReason", String.valueOf(readEndpointResponse)));
                ErrorCodeEnum.ERR_20983.setErrorCode(processedResult);
                return processedResult;
            }
            Dataset addressDataset = CommonUtilities
                    .constructDatasetFromJSONArray(readResponse.getJSONArray("Addresses"));
            addressDataset.setId("Addresses");
            Dataset emailsDataset = CommonUtilities
                    .constructDatasetFromJSONArray(readResponse.getJSONArray("EmailIds"));
            emailsDataset.setId("EmailIds");
            Dataset phoneNumbersDataset = CommonUtilities
                    .constructDatasetFromJSONArray(readResponse.getJSONArray("ContactNumbers"));
            phoneNumbersDataset.setId("ContactNumbers");
            Dataset entitlementsDataset = CommonUtilities
                    .constructDatasetFromJSONArray(readResponse.getJSONArray("services"));
            entitlementsDataset.setId("services");

            processedResult.addDataset(addressDataset);
            processedResult.addDataset(emailsDataset);
            processedResult.addDataset(phoneNumbersDataset);
            processedResult.addParam(
                    new Param("isSecurityQuestionConfigured", readResponse.getString("isSecurityQuestionConfigured")));
            processedResult.addDataset(entitlementsDataset);

        } catch (Exception e) {
            LOG.error("Exception occurred while processing customer entitlements service ", e);
            ErrorCodeEnum.ERR_20983.setErrorCode(processedResult);
            return processedResult;
        }
        return processedResult;
    }
}