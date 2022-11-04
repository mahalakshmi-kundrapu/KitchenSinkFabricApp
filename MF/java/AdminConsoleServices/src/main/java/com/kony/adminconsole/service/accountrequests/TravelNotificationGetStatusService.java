package com.kony.adminconsole.service.accountrequests;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.handler.StatusHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class TravelNotificationGetStatusService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(TravelNotificationGetStatusService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result result = new Result();
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            StatusHandler statusHandler = new StatusHandler();
            CustomerHandler customerHandler = new CustomerHandler();
            if (requestInstance.getParameter("CardNumbers") == null) {
                Result failedResult = new Result();
                ErrorCodeEnum.ERR_20709.setErrorCode(failedResult);
                return failedResult;
            } else if (requestInstance.getParameter("Username") == null) {
                Result failedResult = new Result();
                ErrorCodeEnum.ERR_20705.setErrorCode(failedResult);
                return failedResult;
            }
            String idsArray = requestInstance.getParameter("CardNumbers");
            String username = requestInstance.getParameter("Username");
            String userid = customerHandler.getCustomerId(username, requestInstance);
            String status = statusHandler.getStatusIdForGivenStatus(null, "Active", authToken, requestInstance);
            JSONArray object = new JSONArray(idsArray);
            Dataset cardStatusDataSet = new Dataset();
            Map<String, String> postParametersMap = new HashMap<String, String>();
            cardStatusDataSet.setId("CardStatus");
            for (int i = 0; i < object.length(); i++) {
                Record currRecord = new Record();
                postParametersMap.clear();
                postParametersMap.put(ODataQueryConstants.SELECT, "id,Notification_id");
                postParametersMap.put(ODataQueryConstants.FILTER,
                        "CardNumber eq '" + object.get(i) + "' and Customer_id eq '" + userid + "'");
                String readNotificationCardResponse = Executor.invokeService(ServiceURLEnum.NOTIFICATIONCARDINFO_READ,
                        postParametersMap, null, requestInstance);
                JSONObject readNotificationCardResponseJSON = CommonUtilities
                        .getStringAsJSONObject(readNotificationCardResponse);
                if (readNotificationCardResponseJSON != null
                        && readNotificationCardResponseJSON.has(FabricConstants.OPSTATUS)
                        && readNotificationCardResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                    JSONArray readNotificationCardResponseJSONArray = readNotificationCardResponseJSON
                            .getJSONArray("notificationcardinfo");
                    if (readNotificationCardResponseJSONArray.length() > 0) {
                        postParametersMap.clear();
                        postParametersMap.put(ODataQueryConstants.FILTER, "id eq '"
                                + readNotificationCardResponseJSONArray.getJSONObject(0).getString("Notification_id")
                                + "' and Status_id eq '" + status + "'");
                        String readTravelNotificationStatus = Executor.invokeService(
                                ServiceURLEnum.TRAVELNOTIFICATION_READ, postParametersMap, null, requestInstance);
                        JSONObject readTravelNotificationStatusResponseJSON = CommonUtilities
                                .getStringAsJSONObject(readTravelNotificationStatus);
                        if (readTravelNotificationStatusResponseJSON.length() > 0) {
                            Param id_Param = new Param("id", object.get(i).toString(), FabricConstants.STRING);
                            Param status_Param = new Param("status", "YES", FabricConstants.STRING);
                            currRecord.addParam(id_Param);
                            currRecord.addParam(status_Param);
                        } else {
                            Param id_Param = new Param("id", object.get(i).toString(), FabricConstants.STRING);
                            Param status_Param = new Param("status", "NO", FabricConstants.STRING);
                            currRecord.addParam(id_Param);
                            currRecord.addParam(status_Param);
                        }
                    } else {
                        Param id_Param = new Param("id", object.get(i).toString(), FabricConstants.STRING);
                        Param status_Param = new Param("status", "NO", FabricConstants.STRING);
                        currRecord.addParam(id_Param);
                        currRecord.addParam(status_Param);
                    }
                    cardStatusDataSet.addRecord(currRecord);

                } else {
                    ErrorCodeEnum.ERR_20322.setErrorCode(result);
                    return result;
                }

            }
            result.addDataset(cardStatusDataSet);
            return result;
        } catch (Exception e) {
            LOG.error("Exception in Travel Notification Get Status Service", e);
            Result failedResult = new Result();
            ErrorCodeEnum.ERR_20324.setErrorCode(failedResult);
            return failedResult;
        }
    }

}