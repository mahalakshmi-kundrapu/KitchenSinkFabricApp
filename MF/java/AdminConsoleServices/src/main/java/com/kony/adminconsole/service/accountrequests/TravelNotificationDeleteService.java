package com.kony.adminconsole.service.accountrequests;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
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
import com.konylabs.middleware.dataobject.Result;

public class TravelNotificationDeleteService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(TravelNotificationDeleteService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            if (requestInstance.getParameter("request_id") == null) {
                Result failedResult = new Result();
                ErrorCodeEnum.ERR_20704.setErrorCode(failedResult);
                return failedResult;
            }
            String requestId = requestInstance.getParameter("request_id");
            ServiceURLEnum readURL = ServiceURLEnum.NOTIFICATIONCARDINFO_READ;
            ServiceURLEnum deleteURL = ServiceURLEnum.NOTIFICATIONCARDINFO_DELETE;
            JSONObject deleteNotificationCardResponseJSON = deleteNotificationCardInfo(requestId, readURL, deleteURL,
                    authToken, requestInstance);
            Result result = new Result();
            if (deleteNotificationCardResponseJSON != null) {

                if (deleteNotificationCardResponseJSON != null
                        && deleteNotificationCardResponseJSON.has(FabricConstants.OPSTATUS)
                        && deleteNotificationCardResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {

                    Map<String, String> postParametersMap = new HashMap<String, String>();
                    postParametersMap.clear();
                    postParametersMap.put("id", requestId);
                    String deleteTravelNotificationResponse = Executor.invokeService(
                            ServiceURLEnum.TRAVELNOTIFICATION_DELETE, postParametersMap, null, requestInstance);
                    JSONObject deleteTravelNotificationResponseJSON = CommonUtilities
                            .getStringAsJSONObject(deleteTravelNotificationResponse);
                    if (deleteTravelNotificationResponseJSON != null
                            && deleteTravelNotificationResponseJSON.has(FabricConstants.OPSTATUS)
                            && deleteTravelNotificationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                        // DO NOTHING
                    } else {
                        ErrorCodeEnum.ERR_20323.setErrorCode(result);
                        return result;
                    }
                } else {
                    ErrorCodeEnum.ERR_20323.setErrorCode(result);
                    return result;
                }
            }

            return result;
        } catch (Exception e) {
            LOG.error("Exception in Travel Notification Delete Service", e);
            Result failedResult = new Result();
            ErrorCodeEnum.ERR_20324.setErrorCode(failedResult);
            return failedResult;
        }
    }

    public JSONObject deleteNotificationCardInfo(String requestId, ServiceURLEnum readURL, ServiceURLEnum deleteURL,
            String authToken, DataControllerRequest requestInstance) {
        JSONObject result = null;
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        postParametersMap.put(ODataQueryConstants.SELECT, "id");
        postParametersMap.put(ODataQueryConstants.FILTER, "Notification_id eq '" + requestId + "'");
        String readNotificationCardResponse = Executor.invokeService(readURL, postParametersMap, null, requestInstance);
        JSONObject readNotificationCardResponseJSON = CommonUtilities
                .getStringAsJSONObject(readNotificationCardResponse);
        int opStatusCodeForread = readNotificationCardResponseJSON.getInt(FabricConstants.OPSTATUS);
        if (opStatusCodeForread == 0) {
            JSONArray readNotificationCardResponseJSONArray = readNotificationCardResponseJSON
                    .getJSONArray("notificationcardinfo");
            for (int indexVar = 0; indexVar < readNotificationCardResponseJSONArray.length(); indexVar++) {
                JSONObject currTravelNotificationJSONObject = readNotificationCardResponseJSONArray
                        .getJSONObject(indexVar);
                String id = currTravelNotificationJSONObject.getString("id");
                postParametersMap.put("id", id);
                String deleteNotificationCardResponse = Executor.invokeService(deleteURL, postParametersMap, null,
                        requestInstance);
                result = CommonUtilities.getStringAsJSONObject(deleteNotificationCardResponse);
            }
        }
        return result;
    }

}