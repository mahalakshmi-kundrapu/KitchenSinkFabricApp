package com.kony.adminconsole.service.card;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.exception.DBPAuthenticationException;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.DBPServices;
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

/**
 * Service to retrieve the customer card information and it's related request and notification summary
 * 
 * @author Aditya Mankal
 * 
 */
public class CardGetService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CardGetService.class);
    private static final String REQUEST_TYPE_IDENTIFIER = "REQUEST";
    private static final String NOTIFICATION_TYPE_IDENTIFIER = "NOTIFICATION";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) {
        try {
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String customerUsername = requestInstance.getParameter("customerUsername");
            if (StringUtils.isBlank(customerUsername)) {
                Result processedResult = new Result();
                ErrorCodeEnum.ERR_20863.setErrorCode(processedResult);
                return processedResult;
            }
            return getCustomerCardsData(requestInstance, customerUsername, authToken);
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    private Result getCustomerCardsData(DataControllerRequest requestInstance, String customerUsername,
            String authToken) {
        Result customerCardsResult = new Result();

        JSONObject fetchCustomerCardsResponseObject;
        try {
            fetchCustomerCardsResponseObject = DBPServices.getCustomerCards(requestInstance, customerUsername,
                    authToken);
        } catch (DBPAuthenticationException e) {
            e.getErrorCodeEnum().setErrorCode(customerCardsResult);
            return customerCardsResult;
        }

        if (fetchCustomerCardsResponseObject != null && fetchCustomerCardsResponseObject.has(FabricConstants.OPSTATUS)
                && fetchCustomerCardsResponseObject.getInt(FabricConstants.OPSTATUS) == 0) {

            Map<String, Map<Integer, Integer>> cardRequestNotificationCountData = getCardRequestNotificationCount(
                    requestInstance, customerUsername, authToken);

            Map<Integer, Integer> cardRequestCount = cardRequestNotificationCountData.get("cardRequestCount");
            Map<Integer, Integer> cardNotificationCount = cardRequestNotificationCountData.get("cardNotificationCount");

            JSONArray cardsJSONArray = fetchCustomerCardsResponseObject.has("Cards")
                    ? fetchCustomerCardsResponseObject.getJSONArray("Cards")
                    : new JSONArray();

            JSONObject currRecordJSONObject = null;

            Dataset cardsDataset = new Dataset();
            cardsDataset.setId("records");

            int currCardNumber, currRequestCount, currNotificationCount;

            for (int indexVar = 0; indexVar < cardsJSONArray.length(); indexVar++) {
                if (cardsJSONArray.get(indexVar) instanceof JSONObject) {
                    Record currRecord = new Record();
                    currRecordJSONObject = cardsJSONArray.getJSONObject(indexVar);
                    for (String currKey : currRecordJSONObject.keySet()) {
                        Param currParam = new Param(currKey, currRecordJSONObject.optString(currKey),
                                FabricConstants.STRING);
                        currRecord.addParam(currParam);
                    }
                    if (currRecordJSONObject.has("cardNumber")) {
                        currCardNumber = currRecordJSONObject.optInt("cardNumber");
                        if (cardRequestCount.containsKey(currCardNumber))
                            currRequestCount = cardRequestCount.get(currCardNumber);
                        else
                            currRequestCount = 0;
                        if (cardNotificationCount.containsKey(currCardNumber))
                            currNotificationCount = cardNotificationCount.get(currCardNumber);
                        else
                            currNotificationCount = 0;

                        Param currRequestCountParam = new Param("requestCount", String.valueOf(currRequestCount),
                                FabricConstants.STRING);
                        Param currNotificationCountParam = new Param("notificationCount",
                                String.valueOf(currNotificationCount), FabricConstants.STRING);

                        currRecord.addParam(currNotificationCountParam);
                        currRecord.addParam(currRequestCountParam);
                    }
                    cardsDataset.addRecord(currRecord);
                }
            }
            customerCardsResult.addDataset(cardsDataset);
            customerCardsResult.addRecord(getIssuerImagesRecord(requestInstance, authToken));
        } else
            ErrorCodeEnum.ERR_20301.setErrorCode(customerCardsResult);
        return customerCardsResult;
    }

    private Map<String, Map<Integer, Integer>> getCardRequestNotificationCount(DataControllerRequest requestInstance,
            String customerUsername, String authToken) {

        CustomerHandler customerHandler = new CustomerHandler();
        String customerID = customerHandler.getCustomerId(customerUsername, requestInstance);
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "customerId eq '" + customerID + "'");

        String readcardRequestNotificationCountViewReadResponse = Executor.invokeService(
                ServiceURLEnum.CARD_REQUEST_NOTIFICATION_COUNT_VIEW_READ, postParametersMap, null, requestInstance);
        JSONObject readcardRequestNotificationCountViewReadResponseJSON = CommonUtilities
                .getStringAsJSONObject(readcardRequestNotificationCountViewReadResponse);

        Map<String, Map<Integer, Integer>> cardRequestNotificationCountData =
                new HashMap<String, Map<Integer, Integer>>();
        Map<Integer, Integer> cardRequestCount = new HashMap<Integer, Integer>();
        Map<Integer, Integer> cardNotificationCount = new HashMap<Integer, Integer>();
        cardRequestNotificationCountData.put("cardRequestCount", cardRequestCount);
        cardRequestNotificationCountData.put("cardNotificationCount", cardNotificationCount);

        if (readcardRequestNotificationCountViewReadResponseJSON == null
                || !readcardRequestNotificationCountViewReadResponseJSON.has("card_request_notification_count_view"))
            return cardRequestNotificationCountData;

        JSONArray countArray = readcardRequestNotificationCountViewReadResponseJSON
                .getJSONArray("card_request_notification_count_view");
        JSONObject currRecordJSONObject = null;
        int currCardNumber, currRequestCount, currNotificationCount;

        for (int indexVar = 0; indexVar < countArray.length(); indexVar++) {
            if (countArray.get(indexVar) instanceof JSONObject) {
                currRecordJSONObject = countArray.getJSONObject(indexVar);

                if (currRecordJSONObject.has("reqType") && currRecordJSONObject.has("cardNumber")) {
                    currCardNumber = currRecordJSONObject.optInt("cardNumber");

                    if (currRecordJSONObject.optString("reqType").equalsIgnoreCase(REQUEST_TYPE_IDENTIFIER)) {
                        currRequestCount = currRecordJSONObject.optInt("requestcount");
                        cardRequestCount.put(currCardNumber, currRequestCount);

                    } else if (currRecordJSONObject.optString("reqType")
                            .equalsIgnoreCase(NOTIFICATION_TYPE_IDENTIFIER)) {
                        currNotificationCount = currRecordJSONObject.optInt("requestcount");
                        cardNotificationCount.put(currCardNumber, currNotificationCount);
                    }
                }
            }
        }
        return cardRequestNotificationCountData;
    }

    private Record getIssuerImagesRecord(DataControllerRequest requestInstance, String authToken) {
        Record issuerImagesRecord = new Record();
        issuerImagesRecord.setId("issuerImages");
        try {
            String readIssuerImagesResponse = Executor.invokeService(ServiceURLEnum.ISSUERIMAGE_READ,
                    new HashMap<String, String>(), null, requestInstance);
            JSONObject readIssuerImagesResponseJSON = CommonUtilities.getStringAsJSONObject(readIssuerImagesResponse);
            if (readIssuerImagesResponseJSON != null && readIssuerImagesResponseJSON.has(FabricConstants.OPSTATUS)
                    && readIssuerImagesResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readIssuerImagesResponseJSON.has("issuerimage")) {
                JSONArray issuerImagesArray = readIssuerImagesResponseJSON.getJSONArray("issuerimage");
                JSONObject currRecordJSONObject = null;
                for (int indexVar = 0; indexVar < issuerImagesArray.length(); indexVar++) {
                    currRecordJSONObject = issuerImagesArray.getJSONObject(indexVar);
                    Record currIssuerImageRecord = new Record();
                    if (!currRecordJSONObject.has("issuerName") || !currRecordJSONObject.has("Image"))
                        continue;
                    currIssuerImageRecord.setId(currRecordJSONObject.optString("issuerName"));
                    for (String currKey : currRecordJSONObject.keySet()) {
                        Param currParam = new Param(currKey, currRecordJSONObject.optString(currKey),
                                FabricConstants.STRING);
                        currIssuerImageRecord.addParam(currParam);
                    }
                    issuerImagesRecord.addRecord(currIssuerImageRecord);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return issuerImagesRecord;
    }

}