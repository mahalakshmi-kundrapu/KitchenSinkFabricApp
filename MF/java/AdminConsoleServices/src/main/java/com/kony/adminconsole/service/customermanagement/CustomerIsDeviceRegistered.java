package com.kony.adminconsole.service.customermanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class CustomerIsDeviceRegistered implements JavaService2 {

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse dataControllerResponse) throws Exception {

        Result processedResult = new Result();

        try {
            String deviceID = requestInstance.getParameter("Device_id");
            String username = requestInstance.getParameter("username");

            if (StringUtils.isBlank(username)) {
                ErrorCodeEnum.ERR_20612.setErrorCode(processedResult);
                return processedResult;
            }
            if (StringUtils.isBlank(deviceID)) {
                ErrorCodeEnum.ERR_20537.setErrorCode(processedResult);
                return processedResult;
            }

            CustomerHandler customerHandler = new CustomerHandler();
            String customerID = customerHandler.getCustomerId(username, requestInstance);

            if (StringUtils.isBlank(customerID)) {
                ErrorCodeEnum.ERR_20536.setErrorCode(processedResult);
                return processedResult;
            }

            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put(ODataQueryConstants.FILTER, "id eq '" + deviceID + "' and Customer_id eq '"
                    + customerID + "' and " + "Status_id eq 'SID_DEVICE_REGISTERED'");

            JSONObject readEndpointResponse = CommonUtilities.getStringAsJSONObject(Executor
                    .invokeService(ServiceURLEnum.CUSTOMERDEVICE_READ, postParametersMap, null, requestInstance));

            if (readEndpointResponse != null && readEndpointResponse.has(FabricConstants.OPSTATUS)
                    && readEndpointResponse.getInt(FabricConstants.OPSTATUS) == 0
                    && readEndpointResponse.has("customerdevice")) {
                if (readEndpointResponse.getJSONArray("customerdevice").length() > 0) {
                    processedResult.addParam(new Param("status", "true", FabricConstants.STRING));
                } else {
                    processedResult.addParam(new Param("status", "false", FabricConstants.STRING));
                }
            } else {
                ErrorCodeEnum.ERR_20612.setErrorCode(processedResult);
                processedResult.addParam(
                        new Param("FailureReason", String.valueOf(readEndpointResponse), FabricConstants.STRING));
            }

        } catch (Exception e) {
            ErrorCodeEnum.ERR_20612.setErrorCode(processedResult);
            processedResult.addParam(new Param("FailureReason", e.getMessage(), FabricConstants.STRING));
        }
        return processedResult;
    }

}