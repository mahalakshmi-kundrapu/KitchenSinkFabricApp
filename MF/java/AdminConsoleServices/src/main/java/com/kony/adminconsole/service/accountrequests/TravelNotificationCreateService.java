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
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class TravelNotificationCreateService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(TravelNotificationCreateService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        CustomerHandler customerHandler = new CustomerHandler();
        String userId = customerHandler.getCustomerId(requestInstance.getParameter("Username"), requestInstance);
        try {
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String travelNotificationId = "";
            Result failedResult = new Result();
            if (requestInstance.getParameter("Destinations") == null) {
                ErrorCodeEnum.ERR_20706.setErrorCode(failedResult);
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
                if (!(isRecordExists(status, authToken, ServiceURL, requestInstance.getParameter("Cards"), userId,
                        requestInstance.getParameter("StartDate"), requestInstance.getParameter("EndDate"),
                        requestInstance.getParameter("Destinations"), requestInstance))) {
                    // Record Doesn't exists
                    Map<String, String> postParametersMap = new HashMap<String, String>();
                    travelNotificationId = Long.toString(CommonUtilities.getNumericId());
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
                        String createTravelNotificationResponse = Executor.invokeService(
                                ServiceURLEnum.TRAVELNOTIFICATION_CREATE, postParametersMap, null, requestInstance);
                        JSONObject createTravelNotificationResponseJSON = CommonUtilities
                                .getStringAsJSONObject(createTravelNotificationResponse);
                        if (createTravelNotificationResponseJSON != null
                                && createTravelNotificationResponseJSON.has(FabricConstants.OPSTATUS)
                                && createTravelNotificationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                            postParametersMap.clear();
                            for (int i = 0; i < cardArray.length(); i++) {
                                String notificationCardInfoId = String.valueOf(CommonUtilities.getNewId());
                                postParametersMap.put("id", notificationCardInfoId);
                                postParametersMap.put("Notification_id", travelNotificationId);
                                postParametersMap.put("Customer_id", userId);
                                postParametersMap.put("createdby", userId);
                                postParametersMap.put("CardName", ((JSONObject) cardArray.get(i)).getString("name"));
                                postParametersMap.put("CardNumber",
                                        ((JSONObject) cardArray.get(i)).getString("number"));

                                String createNotificationCardInfoResponse = Executor.invokeService(
                                        ServiceURLEnum.NOTIFICATIONCARDINFO_CREATE, postParametersMap, null,
                                        requestInstance);
                                JSONObject createNotificationCardInfoResponseJSON = CommonUtilities
                                        .getStringAsJSONObject(createNotificationCardInfoResponse);
                                if (createNotificationCardInfoResponseJSON != null
                                        && createNotificationCardInfoResponseJSON.has(FabricConstants.OPSTATUS)
                                        && createNotificationCardInfoResponseJSON
                                                .getInt(FabricConstants.OPSTATUS) == 0) {
                                    // DO NOTHING
                                } else {
                                    ErrorCodeEnum.ERR_20321.setErrorCode(failedResult);
                                    return failedResult;
                                }
                            }
                            Result result = new Result();
                            result.addParam(
                                    new Param("request_id", travelNotificationId.toString(), FabricConstants.STRING));
                            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TRAVELNOTIFICATION,
                                    EventEnum.CREATE, ActivityStatusEnum.SUCCESSFUL, "Travel Notification Created"
                                            + travelNotificationId.toString() + " Customer id: " + userId);
                            return result;
                        } else {
                            ErrorCodeEnum.ERR_20321.setErrorCode(failedResult);
                            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TRAVELNOTIFICATION,
                                    EventEnum.CREATE, ActivityStatusEnum.FAILED,
                                    "Travel Notification Creation Failed. Customer id: " + userId);
                            return failedResult;
                        }

                    }

                }
            }
            ErrorCodeEnum.ERR_20710.setErrorCode(failedResult);
            return failedResult;
        } catch (Exception e) {
            LOG.error("Exception in Travel Notification Create Service", e);
            Result failedResult = new Result();
            ErrorCodeEnum.ERR_20711.setErrorCode(failedResult);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.TRAVELNOTIFICATION, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "Travel Notification Creation Failed. Customer id: " + userId);
            return failedResult;
        }
    }

    private boolean isRecordExists(String status, String authToken, ServiceURLEnum serviceURLEnum, String cards,
            String User_id, String StartDate, String EndDate, String Destinations,
            DataControllerRequest requestInstance) {
        Boolean isCardFound = false;
        Boolean isDestinationFound = false;
        JSONArray cardArray = new JSONArray(cards);
        JSONArray destinationsArray = new JSONArray(Destinations);

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "customerId eq '" + User_id + "' and startDate ge '"
                + StartDate + "' and endDate le '" + EndDate + "' and Status_id eq '" + status + "'");
        postParametersMap.put(ODataQueryConstants.SELECT, "destinations,cardNumber");
        String readTravelNotificationResponse = Executor.invokeService(serviceURLEnum, postParametersMap, null,
                requestInstance);

        JSONObject readTravelNotificationResponseJSON = CommonUtilities
                .getStringAsJSONObject(readTravelNotificationResponse);
        int opStatusCode = readTravelNotificationResponseJSON.getInt(FabricConstants.OPSTATUS);
        if (opStatusCode == 0) {
            JSONArray readTravelNotificationResponseJSONArray = readTravelNotificationResponseJSON
                    .getJSONArray("travelnotifications_view");
            if (readTravelNotificationResponseJSONArray.length() > 0) {
                for (int indexVar = 0; indexVar < readTravelNotificationResponseJSONArray.length(); indexVar++) {
                    JSONObject currTravelNotificationJSONObject = readTravelNotificationResponseJSONArray
                            .getJSONObject(indexVar);
                    String cardIdsString = currTravelNotificationJSONObject.getString("cardNumber");
                    String destinationsString = currTravelNotificationJSONObject.getString("destinations");
                    for (int i = 0; i < cardArray.length(); i++) {
                        String cardId = ((JSONObject) cardArray.get(i)).getString("number");
                        if (cardIdsString.indexOf(cardId) >= 0) {
                            isCardFound = true;
                        }
                    }
                    for (int i = 0; i < destinationsArray.length(); i++) {
                        String destination = destinationsArray.getString(i).toString();
                        if (destinationsString.indexOf(destination) >= 0) {
                            isDestinationFound = true;
                        }
                    }
                }
            }
        }
        if (isCardFound && isDestinationFound) {
            return true;
        }
        return false;
    }

}