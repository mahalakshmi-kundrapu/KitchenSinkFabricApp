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
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * CustomerUpdateDeviceStatus service will update device status of a customer
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerUpdateDeviceInformation implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerRegisterDevice.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();
            CustomerHandler customerHandler = new CustomerHandler();

            requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String deviceID = requestInstance.getParameter("Device_id");
            String customerID = requestInstance.getParameter("Customer_id");
            String statusID = requestInstance.getParameter("Status_id");
            String lastUsedIp = requestInstance.getParameter("LastUsedIp");
            String lastLoginTime = requestInstance.getParameter("LastLoginTime");
            String userName = null;

            if ((customerID == null || StringUtils.isBlank(customerID))
                    && requestInstance.getParameter("username") != null) {
                requestInstance.setAttribute("isServiceBeingAccessedByOLB", true);
                userName = requestInstance.getParameter("username");
                if (StringUtils.isBlank(userName)) {
                    ErrorCodeEnum.ERR_20612.setErrorCode(processedResult);
                    Param statusParam = new Param("Status", "Update failed", FabricConstants.STRING);
                    processedResult.addParam(statusParam);
                    return processedResult;

                }

                customerID = customerHandler.getCustomerId(userName, requestInstance);
            }

            if (StringUtils.isBlank(customerID)) {
                ErrorCodeEnum.ERR_20613.setErrorCode(processedResult);
                Param statusParam = new Param("Status", "Update failed", FabricConstants.STRING);
                processedResult.addParam(statusParam);
                return processedResult;

            }

            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put("id", deviceID);
            postParametersMap.put("Customer_id", customerID);

            if (!StringUtils.isEmpty(statusID)) {
                postParametersMap.put("Status_id", statusID);
            }

            if (!StringUtils.isEmpty(lastLoginTime)) {
                postParametersMap.put("LastLoginTime", lastLoginTime);
            }

            if (!StringUtils.isEmpty(lastUsedIp)) {
                postParametersMap.put("LastUsedIp", lastUsedIp);
            }

            postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

            JSONObject updateEndpointResponse = CommonUtilities.getStringAsJSONObject(Executor
                    .invokeService(ServiceURLEnum.CUSTOMERDEVICE_UPDATE, postParametersMap, null, requestInstance));
            if (updateEndpointResponse == null || (!updateEndpointResponse.has(FabricConstants.OPSTATUS))
                    || updateEndpointResponse.getInt(FabricConstants.OPSTATUS) != 0) {
                LOG.error("Failed to update customer device information");
                processedResult.addParam(
                        new Param("UpdateResponse", String.valueOf(updateEndpointResponse), FabricConstants.STRING));
                processedResult.addParam(new Param("Status", "Failed", FabricConstants.STRING));
                processedResult.addParam(new Param("OperationCode", ErrorCodeEnum.ERR_20691.getErrorCodeAsString(),
                        FabricConstants.INT));
                return processedResult;
            }

            processedResult.addParam(
                    new Param("UpdateResponse", String.valueOf(updateEndpointResponse), FabricConstants.STRING));
            Param statusParam = new Param("Status", "Update successful", FabricConstants.STRING);
            processedResult.addParam(statusParam);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}