package com.kony.adminconsole.service.accountrequests;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.DateUtils;
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
import com.konylabs.middleware.dataobject.Result;

public class TravelNotificationViewGetService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(TravelNotificationViewGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result result = new Result();
        try {
            String authToken = CommonUtilities.getAuthToken(requestInstance);
            CustomerHandler customerHandler = new CustomerHandler();
            StatusHandler statusHandler = new StatusHandler();
            String customerName = requestInstance.getParameter("Username");
            if (customerName == null) {
                Result failedResult = new Result();
                ErrorCodeEnum.ERR_20705.setErrorCode(failedResult);
                return failedResult;
            }
            String customerId = customerHandler.getCustomerId(customerName, requestInstance);
            Map<String, String> postParametersMap = new HashMap<String, String>();
            List<String> notificationIds = new ArrayList<>();
            Dataset travelNotificationDataSet = new Dataset();
            travelNotificationDataSet.setId("TravelRequests");
            SimpleDateFormat formatter = new SimpleDateFormat(DateUtils.PATTERN_YYYY_MM_DD);
            Calendar rightNow = Calendar.getInstance();
            rightNow.add(Calendar.DAY_OF_MONTH, -90);
            Date today90 = rightNow.getTime();
            String strDate90 = formatter.format(today90);
            if (requestInstance.getParameter("isOLB") == null) {
                postParametersMap.put(ODataQueryConstants.FILTER, "customerId eq '" + customerId + "'");
            } else {
                postParametersMap.put(ODataQueryConstants.FILTER,
                        "customerId eq '" + customerId + "' and date gt '" + strDate90 + "'");
            }
            String readTravelNotificationResponse = Executor.invokeService(ServiceURLEnum.TRAVELNOTIFICATIONS_VIEW_READ,
                    postParametersMap, null, requestInstance);
            JSONObject readTravelNotificationResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readTravelNotificationResponse);
            int opStatusCode = readTravelNotificationResponseJSON.getInt(FabricConstants.OPSTATUS);
            if (opStatusCode == 0) {
                JSONArray readTravelNotificationResponseJSONArray = readTravelNotificationResponseJSON
                        .getJSONArray("travelnotifications_view");
                String expiredStatusId = statusHandler.getStatusIdForGivenStatus(null, "Expired", authToken,
                        requestInstance);
                for (int indexVar = 0; indexVar < readTravelNotificationResponseJSONArray.length(); indexVar++) {
                    JSONObject currTravelNotificationJSONObject = readTravelNotificationResponseJSONArray
                            .getJSONObject(indexVar);
                    Date startDateDateObj = formatter.parse(currTravelNotificationJSONObject.getString("startDate"));
                    Date endDateDateObj = formatter.parse(currTravelNotificationJSONObject.getString("endDate"));
                    Date currDateObj = new Date();
                    if (startDateDateObj.compareTo(currDateObj) < 0 && endDateDateObj.compareTo(currDateObj) < 0
                            && currTravelNotificationJSONObject.getString("status").equals("Active")) {
                        notificationIds.add(currTravelNotificationJSONObject.getString("notificationId"));
                        currTravelNotificationJSONObject.put("status", "Expired");
                        currTravelNotificationJSONObject.put("Status_id", expiredStatusId);
                    }
                    travelNotificationDataSet
                            .addRecord(CommonUtilities.constructRecordFromJSONObject(currTravelNotificationJSONObject));
                    updateStatusOfExpiredRecords(notificationIds, customerId, expiredStatusId, requestInstance,
                            authToken);
                    result.addDataset(travelNotificationDataSet);
                }
            } else {
                ErrorCodeEnum.ERR_20322.setErrorCode(result);
                return result;
            }
        } catch (Exception e) {
            LOG.error("Exception in Travel Notification View Get Service", e);
            ErrorCodeEnum.ERR_20324.setErrorCode(result);
        }
        return result;
    }

    private void updateStatusOfExpiredRecords(List<String> notificationIds, String customerId, String expiredStatusId,
            DataControllerRequest requestInstance, String authToken) {
        if (notificationIds != null && !notificationIds.isEmpty()) {
            TravelNotificationCancelService travelNotificationCancelService = new TravelNotificationCancelService();
            for (String notificationId : notificationIds) {
                try {
                    travelNotificationCancelService.updateStatusForTravelNotification(notificationId, customerId,
                            expiredStatusId, authToken, requestInstance);
                } catch (Exception e) {
                    LOG.error("Exception in Travel Notification View Get Service updating status for notification Id:"
                            + notificationId, e);
                }
            }
        }
    }
}