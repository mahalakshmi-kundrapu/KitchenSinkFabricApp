package com.kony.adminconsole.service.useraccessrestriction;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class RoleToGroupMappingGet implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(RoleToGroupMappingGet.class);
    private static final String INPUT_INTERNAL_ROLE_ID = "InternalRole_id";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();

        String inputCurrentInternalRole = requestInstance.getParameter(INPUT_INTERNAL_ROLE_ID);

        if (StringUtils.isBlank(inputCurrentInternalRole)) {
            ErrorCodeEnum.ERR_21596.setErrorCode(processedResult);
            return processedResult;
        }

        try {

            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put(ODataQueryConstants.FILTER, "InternalRole_id eq '" + inputCurrentInternalRole + "'");

            String readEndpointResponse = Executor.invokeService(
                    ServiceURLEnum.INTERNAL_ROLE_TO_CUSTOMER_ROLE_MAPPING_VIEW_READ, postParametersMap, null,
                    requestInstance);
            JSONObject readResponse = CommonUtilities.getStringAsJSONObject(readEndpointResponse);
            if (readResponse == null || !readResponse.has(FabricConstants.OPSTATUS)
                    || readResponse.getInt(FabricConstants.OPSTATUS) != 0
                    || !readResponse.has("internal_role_to_customer_role_mapping_view")) {
                processedResult
                        .addParam(new Param("FailureReason", String.valueOf(readResponse), FabricConstants.STRING));
                ErrorCodeEnum.ERR_21598.setErrorCode(processedResult);
                return processedResult;
            }
            // Add dataset
            Dataset responseDataset = CommonUtilities.constructDatasetFromJSONArray(
                    readResponse.getJSONArray("internal_role_to_customer_role_mapping_view"));
            responseDataset.setId("internal_role_to_customer_role_mapping");
            processedResult.addDataset(responseDataset);

            return processedResult;
        } catch (Exception e) {
            LOG.error("Exception occurred while processing roles to groups mapping get service ", e);
            ErrorCodeEnum.ERR_21593.setErrorCode(processedResult);
            return processedResult;
        }
    }
}
