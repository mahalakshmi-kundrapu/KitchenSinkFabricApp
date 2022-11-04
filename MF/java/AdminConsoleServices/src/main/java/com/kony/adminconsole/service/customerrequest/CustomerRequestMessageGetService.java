package com.kony.adminconsole.service.customerrequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.kony.adminconsole.utilities.StatusEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to retrieve the Request Messages as per the search criteria
 *
 * @author Aditya Mankal
 * 
 */
public class CustomerRequestMessageGetService implements JavaService2 {

    private static final String EMAIL_COMM_TYPE_KEY = "COMM_TYPE_EMAIL";
    private static final String ARCHIVED_REQUEST_STATUS_ID = StatusEnum.SID_ARCHIVED.name();
    private static final Logger LOG = Logger.getLogger(CustomerRequestMessageGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        try {
            String customerID, customerName, customerFirstName, customerMiddleName, customerLastName, customerUsername,
                    messageRepliedBy, requestSubject, requestAssignedTo, requestCategory, requestID, requestStatusID,
                    dateInitialPoint, dateFinalPoint;

            Result processedResult = new Result();

            customerID = requestInstance.getParameter("customerID");
            customerName = requestInstance.getParameter("customerName");
            customerFirstName = requestInstance.getParameter("customerFirstName");
            customerMiddleName = requestInstance.getParameter("customerMiddleName");
            customerLastName = requestInstance.getParameter("customerLastName");
            customerUsername = requestInstance.getParameter("customerUsername");

            requestID = requestInstance.getParameter("requestID");
            requestSubject = requestInstance.getParameter("requestSubject");
            requestStatusID = requestInstance.getParameter("requestStatusID");
            requestAssignedTo = requestInstance.getParameter("requestAssignedTo");
            requestCategory = requestInstance.getParameter("requestCategory");

            messageRepliedBy = requestInstance.getParameter("messageRepliedBy");

            dateInitialPoint = requestInstance.getParameter("dateInitialPoint");
            dateFinalPoint = requestInstance.getParameter("dateFinalPoint");

            Map<String, String> inputMap = getFilterQueryMap(customerID, customerName, customerFirstName,
                    customerMiddleName, customerLastName, customerUsername, messageRepliedBy, requestSubject,
                    requestAssignedTo, requestCategory, requestID, requestStatusID, dateInitialPoint, dateFinalPoint);

            String operationResponse;
            if (requestStatusID != null && requestStatusID.contains(ARCHIVED_REQUEST_STATUS_ID)) {
                operationResponse = Executor.invokeService(
                        ServiceURLEnum.CUSTOMER_REQUEST_ARCHIVED_MESSAGE_SEARCH_PROC_SERVICE, inputMap, null,
                        requestInstance);
            } else {
                operationResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_REQUEST_MESSAGE_SEARCH_PROC_SERVICE,
                        inputMap, null, requestInstance);
            }
            JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
            if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                    || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                    || !operationResponseJSON.has("records")) {
                throw new ApplicationException(ErrorCodeEnum.ERR_20125);
            }

            Record resultRecord = new Record();
            resultRecord.setId("records");

            Record requestRecord = new Record();
            requestRecord.setId(requestID);
            Dataset messageThreadDataset = new Dataset();
            messageThreadDataset.setId("MessageThread");
            requestRecord.addDataset(messageThreadDataset);

            String currRequestMessageID = null;
            Record currMessageAttachmentRecord = null;
            JSONObject currRequestJSONObject = null;
            ArrayList<String> listOfInitialisedMessages = new ArrayList<String>();
            ArrayList<String> listOfInitialisedAttachments = new ArrayList<String>();

            JSONArray customerRequestRecordsArray = operationResponseJSON.getJSONArray("records");
            for (int indexVar = 0; indexVar < customerRequestRecordsArray.length(); indexVar++) {
                Object currRequestObject = customerRequestRecordsArray.get(indexVar);
                if (!(currRequestObject instanceof JSONObject)) {
                    continue;
                }

                Dataset currMessageAttachmentsDataset = null;
                currRequestJSONObject = (JSONObject) currRequestObject;

                for (String currKey : currRequestJSONObject.keySet()) {
                    if (currKey.startsWith("customerrequest") || currKey.startsWith("customer")) {
                        Param currRequestEntryItemParam = new Param(currKey, currRequestJSONObject.optString(currKey),
                                FabricConstants.STRING);
                        requestRecord.addParam(currRequestEntryItemParam);
                    }
                }

                Record currMessageRecord = null;
                if (!currRequestJSONObject.has("requestmessage_id")) {
                    continue;
                }
                currRequestMessageID = currRequestJSONObject.getString("requestmessage_id");

                // Message Thread - INIT START
                if (listOfInitialisedMessages.contains(currRequestMessageID)) {
                    int targetIndex = 0;
                    for (int innerIndexVar = 0; innerIndexVar < messageThreadDataset.getAllRecords()
                            .size(); innerIndexVar++) {
                        if (messageThreadDataset.getRecord(innerIndexVar).getParam("requestmessage_id").getValue()
                                .equalsIgnoreCase(currRequestMessageID)) {
                            targetIndex = innerIndexVar;
                            break;
                        }
                    }
                    currMessageRecord = messageThreadDataset.getRecord(targetIndex);
                    currMessageAttachmentsDataset = currMessageRecord.getDatasetById("MessageAttachments");
                } else {
                    currMessageRecord = new Record();
                    currMessageAttachmentsDataset = new Dataset();
                    currMessageAttachmentsDataset.setId("MessageAttachments");
                    currMessageRecord.addDataset(currMessageAttachmentsDataset);
                    messageThreadDataset.addRecord(currMessageRecord);
                    listOfInitialisedMessages.add(currRequestMessageID);
                    if (StringUtils.isBlank(customerID) && currRequestJSONObject.has("customerrequest_Customer_id"))
                        customerID = currRequestJSONObject.optString("customerrequest_Customer_id");// Fetching the
                                                                                                    // customer
                    // ID for later user to fetch the customer Email
                }

                // Message Thread - INIT END

                // Message Thread - START
                for (String currKey : currRequestJSONObject.keySet()) {
                    if (currKey.startsWith("requestmessage_")) {
                        Param currRequestMessageEntryItemParam = new Param(currKey,
                                currRequestJSONObject.optString(currKey), FabricConstants.STRING);
                        currMessageRecord.addParam(currRequestMessageEntryItemParam);
                    }
                }

                if (currRequestJSONObject.has("media_id")) {
                    String currMediaID = currRequestJSONObject.optString("media_id");
                    if (!listOfInitialisedAttachments.contains(currMediaID)) {
                        currMessageAttachmentRecord = new Record();
                        currMessageAttachmentRecord.setId(currMediaID);
                        for (String currKey : currRequestJSONObject.keySet()) {
                            if (currKey.startsWith("messageattachment_") || currKey.startsWith("media_")) {
                                Param currRequestMessageAttachmentEntryItemParam = new Param(currKey,
                                        currRequestJSONObject.optString(currKey), FabricConstants.STRING);
                                currMessageAttachmentRecord.addParam(currRequestMessageAttachmentEntryItemParam);
                            }
                        }
                        currMessageAttachmentsDataset.addRecord(currMessageAttachmentRecord);
                    }
                }
                // Message Thread - END
            }

            // Fetch other Customer Information as per the need
            if (StringUtils.isNotBlank(customerID)) {
                inputMap.clear();
                inputMap.put(ODataQueryConstants.FILTER,
                        "Customer_id eq '" + customerID + "' and Type_id eq '" + EMAIL_COMM_TYPE_KEY + "'");
                inputMap.put(ODataQueryConstants.SELECT, "Value");
                String readCustomerCommunicationResponse = Executor
                        .invokeService(ServiceURLEnum.CUSTOMERCOMMUNICATION_READ, inputMap, null, requestInstance);
                JSONObject readCustomerCommunicationResponseJSON = CommonUtilities
                        .getStringAsJSONObject(readCustomerCommunicationResponse);

                if (readCustomerCommunicationResponseJSON != null
                        && readCustomerCommunicationResponseJSON.has(FabricConstants.OPSTATUS)
                        && readCustomerCommunicationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                        && readCustomerCommunicationResponseJSON.has("customercommunication")) {

                    JSONObject currJSONObject;
                    JSONArray customerCommunicationRecords = readCustomerCommunicationResponseJSON
                            .getJSONArray("customercommunication");
                    StringBuffer emailIDBuffer = new StringBuffer();
                    for (int indexVar = 0; indexVar < customerCommunicationRecords.length(); indexVar++) {
                        Object currObject = customerCommunicationRecords.get(indexVar);
                        if (currObject instanceof JSONObject) {
                            currJSONObject = (JSONObject) currObject;
                            if (currJSONObject.has("Value")) {
                                emailIDBuffer.append(currJSONObject.optString("Value") + ",");
                            }
                        }
                    }
                    String emailIDString = CommonUtilities.replaceLastOccuranceOfString(emailIDBuffer.toString(), ",",
                            "");
                    Param emailIDParam = new Param("customerEmail", emailIDString, FabricConstants.STRING);
                    requestRecord.addParam(emailIDParam);
                } else {
                    throw new ApplicationException(ErrorCodeEnum.ERR_20879);
                }
            }
            Dataset recordsDataset = new Dataset();
            recordsDataset.setId("records");
            recordsDataset.addRecord(requestRecord);

            processedResult.addDataset(recordsDataset);
            return processedResult;
        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20921.setErrorCode(errorResult);
            return errorResult;
        }

    }

    private Map<String, String> getFilterQueryMap(String customerID, String customerName, String customerFirstName,
            String customerMiddleName, String customerLastName, String customerUsername, String messageRepliedBy,
            String requestSubject, String requestAssignedTo, String requestCategory, String requestID,
            String requestStatusID, String dateInitialPoint, String dateFinalPoint) {

        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("_customerID", StringUtils.isNotBlank(customerID) ? customerID : StringUtils.EMPTY);
        inputMap.put("_customerName", StringUtils.isNotBlank(customerName) ? customerName : StringUtils.EMPTY);
        inputMap.put("_customerFirstName",
                StringUtils.isNotBlank(customerFirstName) ? customerFirstName : StringUtils.EMPTY);
        inputMap.put("_customerMiddleName",
                StringUtils.isNotBlank(customerMiddleName) ? customerMiddleName : StringUtils.EMPTY);
        inputMap.put("_customerLastName",
                StringUtils.isNotBlank(customerLastName) ? customerLastName : StringUtils.EMPTY);
        inputMap.put("_customerUsername",
                StringUtils.isNotBlank(customerUsername) ? customerUsername : StringUtils.EMPTY);
        inputMap.put("_messageRepliedBy",
                StringUtils.isNotBlank(messageRepliedBy) ? messageRepliedBy : StringUtils.EMPTY);
        inputMap.put("_requestSubject", StringUtils.isNotBlank(requestSubject) ? requestSubject : StringUtils.EMPTY);
        inputMap.put("_requestAssignedTo",
                StringUtils.isNotBlank(requestAssignedTo) ? requestAssignedTo : StringUtils.EMPTY);
        inputMap.put("_requestCategory", StringUtils.isNotBlank(requestCategory) ? requestCategory : StringUtils.EMPTY);
        inputMap.put("_requestID", StringUtils.isNotBlank(requestID) ? requestID : StringUtils.EMPTY);
        inputMap.put("_requestStatusID", StringUtils.isNotBlank(requestStatusID) ? requestStatusID : StringUtils.EMPTY);
        inputMap.put("_dateInitialPoint",
                StringUtils.isNotBlank(dateInitialPoint) ? dateInitialPoint : StringUtils.EMPTY);
        inputMap.put("_dateFinalPoint", StringUtils.isNotBlank(dateFinalPoint) ? dateFinalPoint : StringUtils.EMPTY);
        return inputMap;
    }

}