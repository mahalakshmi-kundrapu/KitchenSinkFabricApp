package com.kony.adminconsole.service.accountrequests;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.handler.StatusHandler;
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

public class TravelNotificationCancelService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(TravelNotificationCancelService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            String authToken = CommonUtilities.getAuthToken(requestInstance);
            StatusHandler statusHandler = new StatusHandler();
            CustomerHandler customerHandler = new CustomerHandler();
            String status = statusHandler.getStatusIdForGivenStatus(null, "Cancelled", authToken, requestInstance);

            if (StringUtils.isBlank(requestInstance.getParameter("request_id"))) {
                Result failedResult = new Result();
                ErrorCodeEnum.ERR_20704.setErrorCode(failedResult);
                return failedResult;
            }
            if (StringUtils.isBlank(requestInstance.getParameter("Username"))) {
                Result failedResult = new Result();
                ErrorCodeEnum.ERR_20705.setErrorCode(failedResult);
                return failedResult;
            }
            String request_id = requestInstance.getParameter("request_id");
            String customerid = customerHandler.getCustomerId(requestInstance.getParameter("Username"),
                    requestInstance);
            return updateStatusForTravelNotification(request_id, customerid, status, authToken, requestInstance);
        } catch (Exception e) {
            LOG.error("Exception in Travel Notification cancel Service", e);
            Result failedResult = new Result();
            ErrorCodeEnum.ERR_20324.setErrorCode(failedResult);
            return failedResult;
        }
    }

    public Result updateStatusForTravelNotification(String request_id, String customerid, String status,
            String authToken, DataControllerRequest requestInstance) {
        Result result = new Result();

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("id", request_id);
        postParametersMap.put("Status_id", status);

        String updateTravelNotificationResponse = Executor.invokeService(ServiceURLEnum.TRAVELNOTIFICATION_UPDATE,
                postParametersMap, null, requestInstance);
        JSONObject updateTravelNotificationResponseJSON = CommonUtilities
                .getStringAsJSONObject(updateTravelNotificationResponse);

        if (updateTravelNotificationResponseJSON != null
                && updateTravelNotificationResponseJSON.has(FabricConstants.OPSTATUS)
                && updateTravelNotificationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TRAVELNOTIFICATION, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL,
                    "Travel Notification with Id:" + request_id + " updated successfully, Customer id:" + customerid);
            result.addParam(new Param("status", "success", FabricConstants.STRING));
            return result;

        } else {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TRAVELNOTIFICATION, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED,
                    "Travel Notification with Id:" + request_id + " updation failed, Customer id:" + customerid);
            result.addParam(new Param("status", "failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20711.setErrorCode(result);
            return result;
        }

    }

}