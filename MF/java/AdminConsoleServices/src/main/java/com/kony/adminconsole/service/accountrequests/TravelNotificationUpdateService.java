package com.kony.adminconsole.service.accountrequests;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
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
import com.konylabs.middleware.dataobject.Result;

public class TravelNotificationUpdateService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(TravelNotificationUpdateService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        CustomerHandler customerHandler = new CustomerHandler();
        String userId = customerHandler.getCustomerId(requestInstance.getParameter("Username"), requestInstance);
        try {
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            TravelNotificationDeleteService deleteClass = new TravelNotificationDeleteService();
            Result failedResult = new Result();
            ServiceURLEnum readURL = ServiceURLEnum.NOTIFICATIONCARDINFO_READ;
            ServiceURLEnum deleteURL = ServiceURLEnum.NOTIFICATIONCARDINFO_DELETE;
            if (requestInstance.getParameter("Destinations") == null) {
                ErrorCodeEnum.ERR_20706.setErrorCode(failedResult);
                return failedResult;
            } else if (requestInstance.getParameter("request_id") == null) {
                ErrorCodeEnum.ERR_20704.setErrorCode(failedResult);
                return failedResult;
            } else if (requestInstance.getParameter("Channel_id") == null) {
                ErrorCodeEnum.ERR_20703.setErrorCode(failedResult);
                return failedResult;
            } else if (requestInstance.getParameter("StartDate") == null) {
                ErrorCodeEnum.ERR_20707.setErrorCode(failedResult);
                return failedResult;
            } else if (requestInstance.getParameter("EndDate") == null) {
                ErrorCodeEnum.ERR_20708.setErrorCode(failedResult);
                return failedResult;
            } else if (requestInstance.getParameter("Cards") == null) {
                ErrorCodeEnum.ERR_20709.setErrorCode(failedResult);
                return failedResult;
            } else if (requestInstance.getParameter("Username") == null) {
                ErrorCodeEnum.ERR_20705.setErrorCode(failedResult);
                return failedResult;
            } else {
                ServiceURLEnum ServiceURL = ServiceURLEnum.TRAVELNOTIFICATIONS_VIEW_READ;
                StatusHandler statusHandler = new StatusHandler();
                RequestsCreateService requestsCreateService = new RequestsCreateService();
                String status = statusHandler.getStatusIdForGivenStatus(null, "Active", authToken, requestInstance);
                String responseId = isRecordExists(status, authToken, ServiceURL, requestInstance.getParameter("Cards"),
                        userId, requestInstance.getParameter("StartDate"), requestInstance.getParameter("EndDate"),
                        requestInstance.getParameter("Destinations"), requestInstance);
                String travelNotificationId = requestInstance.getParameter("request_id");
                if (responseId == null || travelNotificationId.equals(responseId)) {
                    Map<String, String> postParametersMap = new HashMap<String, String>();
                    JSONArray cardArray = new JSONArray(requestInstance.getParameter("Cards"));
                    JSONArray destinationsArray = new JSONArray(requestInstance.getParameter("Destinations"));
                    if (cardArray.length() > 0 && destinationsArray.length() > 0) {
                        postParametersMap.put("id", travelNotificationId);
                        postParametersMap.put("Channel_id", requestsCreateService.getchannelIdByName(
                                requestInstance.getParameter("Channel_id"), authToken, requestInstance));
                        postParametersMap.put("Status_id", status);
                        postParametersMap.put("PlannedDepartureDate", requestInstance.getParameter("StartDate"));
                        postParametersMap.put("PlannedReturnDate", requestInstance.getParameter("EndDate"));
                        String destinationString = "";
                        for (int i = 0; i < destinationsArray.length(); i++) {
                            destinationString += destinationsArray.get(i) + "-";
                        }
                        destinationString = destinationString.substring(0, destinationString.length() - 1);
                        postParametersMap.put("Destinations", destinationString);
                        if (requestInstance.getParameter("additionNotes") != null) {
                            postParametersMap.put("AdditionalNotes", requestInstance.getParameter("additionNotes"));
                        }
                        postParametersMap.put("createdby", userId);
                        postParametersMap.put("modifiedby", userId);
                        if (requestInstance.getParameter("phonenumber") != null) {
                            postParametersMap.put("phonenumber", requestInstance.getParameter("phonenumber"));
                        }
                        String updateTravelNotificationResponse = Executor.invokeService(
                                ServiceURLEnum.TRAVELNOTIFICATION_UPDATE, postParametersMap, null, requestInstance);
                        JSONObject updateTravelNotificationResponseJSON = CommonUtilities
                                .getStringAsJSONObject(updateTravelNotificationResponse);
                        Result result = new Result();
                        if (updateTravelNotificationResponseJSON != null
                                && updateTravelNotificationResponseJSON.has(FabricConstants.OPSTATUS)
                                && updateTravelNotificationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                            JSONObject deleteNotificationCardResponseJSON = deleteClass.deleteNotificationCardInfo(
                                    travelNotificationId, readURL, deleteURL, authToken, requestInstance);
                            if (deleteNotificationCardResponseJSON != null) {
                                int getOpStatusCodeDelete = deleteNotificationCardResponseJSON
                                        .getInt(FabricConstants.OPSTATUS);

                                if (getOpStatusCodeDelete == 0) {
                                    for (int i = 0; i < cardArray.length(); i++) {
                                        postParametersMap.clear();
                                        String notificationCardInfoId = String.valueOf(CommonUtilities.getNewId());
                                        postParametersMap.put("id", notificationCardInfoId);
                                        postParametersMap.put("Notification_id", travelNotificationId);
                                        postParametersMap.put("Customer_id", userId);
                                        postParametersMap.put("createdby", userId);
                                        postParametersMap.put("CardName",
                                                ((JSONObject) cardArray.get(i)).getString("name"));
                                        postParametersMap.put("CardNumber",
                                                ((JSONObject) cardArray.get(i)).getString("number"));

                                        String createNotificationCardInfoResponse = Executor.invokeService(
                                                ServiceURLEnum.NOTIFICATIONCARDINFO_CREATE, postParametersMap, null,
                                                requestInstance);
                                        JSONObject createNotificationCardInfoResponseJSON = CommonUtilities
                                                .getStringAsJSONObject(createNotificationCardInfoResponse);
                                        int opStatusCode = createNotificationCardInfoResponseJSON
                                                .getInt(FabricConstants.OPSTATUS);
                                        if (opStatusCode == 0) {
                                            // DO NOTHING
                                        } else {
                                            ErrorCodeEnum.ERR_20711.setErrorCode(result);
                                            return result;
                                        }
                                    }
                                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TRAVELNOTIFICATION,
                                            EventEnum.UPDATE, ActivityStatusEnum.SUCCESSFUL,
                                            "Travel Notification Updated" + travelNotificationId.toString()
                                                    + ". Customer id:" + userId);
                                    return result;
                                }
                            }
                        }
                    }
                }
            }
            ErrorCodeEnum.ERR_20710.setErrorCode(failedResult);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TRAVELNOTIFICATION, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Travel Notification Update Failed for request:"
                            + requestInstance.getParameter("request_id") + ". Customer id:" + userId);
            return failedResult;
        } catch (Exception e) {
            LOG.error("Exception in Travel Notification Update Service", e);
            Result failedResult = new Result();
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TRAVELNOTIFICATION, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Travel Notification Update Failed for request:"
                            + requestInstance.getParameter("request_id") + ". Customer id:" + userId);
            ErrorCodeEnum.ERR_20741.setErrorCode(failedResult);
            return failedResult;
        }
    }

    private String isRecordExists(String status, String authToken, ServiceURLEnum ServiceURL, String cards,
            String User_id, String StartDate, String EndDate, String Destinations,
            DataControllerRequest requestInstance) {
        // Boolean isCardFound = false;
        String notificationId = null;
        JSONArray cardArray = new JSONArray(cards);
        JSONArray destinationsArray = new JSONArray(Destinations);
        String destinationString = "";
        String cardString = "";
        for (int i = 0; i < destinationsArray.length(); i++) {
            destinationString += destinationsArray.get(i) + "-";
        }
        destinationString = destinationString.substring(0, destinationString.length() - 1);
        for (int i = 0; i < cardArray.length(); i++) {
            cardString += ((JSONObject) cardArray.get(i)).getString("name") + " "
                    + ((JSONObject) cardArray.get(i)).getString("number") + ",";
        }
        cardString = cardString.substring(0, cardString.length() - 1);
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER,
                "customerId eq '" + User_id + "' and startDate ge '" + StartDate + "' and endDate le '" + EndDate
                        + "' and Status_id eq '" + status + "' and cardNumber eq '" + cardString
                        + "' and destinations eq '" + destinationString + "'");
        postParametersMap.put(ODataQueryConstants.SELECT, "notificationId");
        String readTravelNotificationResponse = Executor.invokeService(ServiceURL, postParametersMap, null,
                requestInstance);

        JSONObject readTravelNotificationResponseJSON = CommonUtilities
                .getStringAsJSONObject(readTravelNotificationResponse);
        int opStatusCode = readTravelNotificationResponseJSON.getInt(FabricConstants.OPSTATUS);
        if (opStatusCode == 0) {
            JSONArray readTravelNotificationResponseJSONArray = readTravelNotificationResponseJSON
                    .getJSONArray("travelnotifications_view");
            if (readTravelNotificationResponseJSONArray.length() > 0) {
                JSONObject currTravelNotificationJSONObject = readTravelNotificationResponseJSONArray.getJSONObject(0);
                notificationId = currTravelNotificationJSONObject.getString("notificationId");
            }
        }
        return notificationId;
    }
}