package com.kony.adminconsole.service.customermanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
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

/**
 * CustomerUpdateDeviceStatus service will register a device for a customer
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerRegisterDevice implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerRegisterDevice.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        try {
            CustomerHandler customerHandler = new CustomerHandler();

            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String deviceID = requestInstance.getParameter("Device_id");
            String customerID = requestInstance.getParameter("Customer_id");
            String statusID = requestInstance.getParameter("Status_id");
            String deviceName = requestInstance.getParameter("DeviceName");
            String lastUsedIp = requestInstance.getParameter("LastUsedIp");
            String lastLoginTime = requestInstance.getParameter("LastLoginTime");
            String operatingSystem = requestInstance.getParameter("OperatingSystem");
            String channel_id = requestInstance.getParameter("channel_id");
            String isTracking = requestInstance.getParameter("isTracking");
            String appid = requestInstance.getParameter("appid");
            String username = null;

            if ((customerID == null || StringUtils.isBlank(customerID))
                    && requestInstance.getParameter("username") != null) {
                requestInstance.setAttribute("isServiceBeingAccessedByOLB", true);
                username = requestInstance.getParameter("username");
                if (StringUtils.isBlank(username)) {
                    ErrorCodeEnum.ERR_20612.setErrorCode(processedResult);
                    Param statusParam = new Param("Status", "Failure", FabricConstants.STRING);
                    processedResult.addParam(statusParam);
                    return processedResult;
                }
                customerID = customerHandler.getCustomerId(username, requestInstance);
            }

            if (StringUtils.isNotBlank(channel_id)) {
                if (channel_id.equalsIgnoreCase("Mobile"))
                    channel_id = "CH_ID_MOB";
                else if (channel_id.equalsIgnoreCase("Desktop")) {
                    channel_id = "CH_ID_INT";
                    if (operatingSystem.contains("Android") || operatingSystem.contains("iPhone")
                            || operatingSystem.contains("iPad")) {
                        channel_id = "CH_ID_MOB_INT";
                    }
                } else if (channel_id.equalsIgnoreCase("Tablet"))
                    channel_id = "CH_ID_TABLET";
                else if (channel_id.equalsIgnoreCase("MobileWeb"))
                    channel_id = "CH_ID_MOB_INT";
                else
                    channel_id = "CH_ID_MOB";
            } else {
                channel_id = "CH_ID_MOB";
            }

            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put(ODataQueryConstants.FILTER,
                    "id eq '" + deviceID + "' and Customer_id eq '" + customerID + "'");

            JSONObject readEndpointResponse = CommonUtilities.getStringAsJSONObject(Executor
                    .invokeService(ServiceURLEnum.CUSTOMERDEVICE_READ, postParametersMap, null, requestInstance));
            if (readEndpointResponse != null && readEndpointResponse.has(FabricConstants.OPSTATUS)
                    && readEndpointResponse.getInt(FabricConstants.OPSTATUS) == 0) {
                if (readEndpointResponse.has("customerdevice")
                        && readEndpointResponse.getJSONArray("customerdevice").length() > 0) {
                    String previousStatus = readEndpointResponse.getJSONArray("customerdevice").getJSONObject(0)
                            .getString("Status_id");
                    if (StringUtils.isNotBlank(isTracking) && isTracking.equalsIgnoreCase("true")) {
                        statusID = previousStatus;
                    } else {
                        statusID = "SID_DEVICE_REGISTERED";
                    }
                    return updateCustomerDevice(requestInstance, processedResult, authToken, customerID, deviceID,
                            statusID, deviceName, lastUsedIp, lastLoginTime, operatingSystem, channel_id, username,
                            appid);
                }
            }
            if (StringUtils.isNotBlank(isTracking) && isTracking.equalsIgnoreCase("true")) {
                statusID = "SID_DEVICE_ACTIVE";
            } else {
                statusID = "SID_DEVICE_REGISTERED";
            }
            return createCustomerDevice(requestInstance, processedResult, authToken, customerID, deviceID, statusID,
                    deviceName, lastUsedIp, lastLoginTime, operatingSystem, channel_id, username, appid);
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    public static Result createCustomerDevice(DataControllerRequest requestInstance, Result processedResult,
            String authToken, String customerID, String deviceID, String statusID, String deviceName, String lastUsedIp,
            String lastLoginTime, String operatingSystem, String channel_id, String username, String appid) {
        if (StringUtils.isBlank(customerID))
            ErrorCodeEnum.ERR_20613.setErrorCode(processedResult);
        else if (StringUtils.isBlank(statusID))
            ErrorCodeEnum.ERR_20692.setErrorCode(processedResult);
        else if (StringUtils.isBlank(channel_id))
            ErrorCodeEnum.ERR_20693.setErrorCode(processedResult);
        else if (StringUtils.isBlank(deviceName))
            ErrorCodeEnum.ERR_20694.setErrorCode(processedResult);
        else if (StringUtils.isBlank(lastUsedIp))
            ErrorCodeEnum.ERR_20695.setErrorCode(processedResult);
        else if (StringUtils.isBlank(lastLoginTime))
            ErrorCodeEnum.ERR_20697.setErrorCode(processedResult);
        else if (StringUtils.isBlank(operatingSystem))
            ErrorCodeEnum.ERR_20696.setErrorCode(processedResult);

        if (processedResult.getParamByName(FabricConstants.ERR_MSG) != null
                && StringUtils.isNotBlank(processedResult.getParamByName(FabricConstants.ERR_MSG).getName())) {
            processedResult.addParam(new Param("Status", "Failure", FabricConstants.STRING));
            return processedResult;
        }

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("id", deviceID);
        postParametersMap.put("Customer_id", customerID);
        postParametersMap.put("Status_id", statusID);
        postParametersMap.put("DeviceName", deviceName);
        postParametersMap.put("LastUsedIp", lastUsedIp);
        postParametersMap.put("LastLoginTime", lastLoginTime);
        postParametersMap.put("OperatingSystem", operatingSystem);
        postParametersMap.put("Channel_id", channel_id);
        postParametersMap.put("appid", appid);
        postParametersMap.put("createdby", username);
        postParametersMap.put("modifiedby", username);
        postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
        postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
        postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());

        JSONObject createEndpointResponse = CommonUtilities.getStringAsJSONObject(
                Executor.invokeService(ServiceURLEnum.CUSTOMERDEVICE_CREATE, postParametersMap, null, requestInstance));
        if (createEndpointResponse == null || (!createEndpointResponse.has(FabricConstants.OPSTATUS))
                || (createEndpointResponse.getInt(FabricConstants.OPSTATUS) != 0)) {
            LOG.error("Failed to register a device!");
            processedResult.addParam(
                    new Param("RegisterResponse", String.valueOf(createEndpointResponse), FabricConstants.STRING));
            processedResult.addParam(new Param("Status", "Failed", FabricConstants.STRING));
            processedResult.addParam(
                    new Param("OperationCode", ErrorCodeEnum.ERR_20691.getErrorCodeAsString(), FabricConstants.INT));
            return processedResult;
        }

        Param statusParam = new Param("Status", "Successful", FabricConstants.STRING);
        processedResult.addParam(new Param("OperationCode", "0", FabricConstants.INT));
        processedResult.addParam(statusParam);
        return processedResult;
    }

    public static Result updateCustomerDevice(DataControllerRequest requestInstance, Result processedResult,
            String authToken, String customerID, String deviceID, String statusID, String deviceName, String lastUsedIp,
            String lastLoginTime, String operatingSystem, String channel_id, String username, String appid) {

        if (StringUtils.isBlank(customerID))
            ErrorCodeEnum.ERR_20613.setErrorCode(processedResult);
        else if (StringUtils.isEmpty(statusID))
            ErrorCodeEnum.ERR_20692.setErrorCode(processedResult);
        else if (StringUtils.isEmpty(lastUsedIp))
            ErrorCodeEnum.ERR_20695.setErrorCode(processedResult);
        else if (StringUtils.isEmpty(lastLoginTime))
            ErrorCodeEnum.ERR_20697.setErrorCode(processedResult);

        if (processedResult.getParamByName(FabricConstants.ERR_MSG) != null
                && StringUtils.isNotBlank(processedResult.getParamByName(FabricConstants.ERR_MSG).getName())) {
            processedResult.addParam(new Param("Status", "Failure", FabricConstants.STRING));
            return processedResult;
        }

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("id", deviceID);
        postParametersMap.put("Customer_id", customerID);
        postParametersMap.put("Status_id", statusID);
        if (StringUtils.isNotBlank(deviceName))
            postParametersMap.put("DeviceName", deviceName);
        postParametersMap.put("LastUsedIp", lastUsedIp);
        postParametersMap.put("LastLoginTime", lastLoginTime);
        if (StringUtils.isNotBlank(operatingSystem))
            postParametersMap.put("OperatingSystem", operatingSystem);
        if (StringUtils.isNotBlank(channel_id))
            postParametersMap.put("Channel_id", channel_id);
        if (StringUtils.isNotBlank(appid))
            postParametersMap.put("appid", appid);
        postParametersMap.put("modifiedby", username);
        postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

        JSONObject updateEndpointResponse = CommonUtilities.getStringAsJSONObject(
                Executor.invokeService(ServiceURLEnum.CUSTOMERDEVICE_UPDATE, postParametersMap, null, requestInstance));
        if (updateEndpointResponse == null || (!updateEndpointResponse.has(FabricConstants.OPSTATUS))
                || (updateEndpointResponse.getInt(FabricConstants.OPSTATUS) != 0)) {
            LOG.error("Failed to register a device!");
            processedResult.addParam(
                    new Param("RegisterResponse", String.valueOf(updateEndpointResponse), FabricConstants.STRING));
            processedResult.addParam(new Param("Status", "Failed", FabricConstants.STRING));
            processedResult.addParam(
                    new Param("OperationCode", ErrorCodeEnum.ERR_20691.getErrorCodeAsString(), FabricConstants.INT));
            return processedResult;
        }

        Param statusParam = new Param("Status", "Successful", FabricConstants.STRING);
        processedResult.addParam(new Param("OperationCode", "0", FabricConstants.INT));
        processedResult.addParam(statusParam);
        return processedResult;
    }

}