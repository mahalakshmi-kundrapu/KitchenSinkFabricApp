package com.kony.adminconsole.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.handler.MultipartPayloadHandler.FormItem;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.dto.CustomerRequestBean;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.kony.adminconsole.utilities.StatusEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;

/**
 * 
 * Handler to manage Customer Requests and Request Messages
 * 
 * @author Aditya Mankal
 * 
 */
public class CustomerRequestAndMessagesHandler {

    private static final String FAILED_OPERATION_CODE = "-1";
    public static final int ATTACHMENT_MAX_FILE_SIZE_IN_BYTES = 5000000; // 5MB
    public static final int REQUEST_SUBJECT_MIN_CHARS = 1;
    public static final int REQUEST_MESSAGE_MIN_CHARS = 1;
    public static final int REQUEST_MESSAGE_MAX_CHARS = 5000;
    public static final int REQUEST_SUBJECT_MAX_CHARS = 50;
    public static final String DEFAULT_REQUEST_CATEGORY_ID = "RCID_GENERALBANKING";
    public static final String DEFAULT_REQUEST_PRIORITY = "MEDIUM";
    public static final String DEFAULT_REQUEST_STATUS_ID = StatusEnum.SID_OPEN.name();
    private static final Logger LOG = Logger.getLogger(CustomerRequestAndMessagesHandler.class);

    public String getLatestMessageIdOfRequest(DataControllerRequest requestInstance, String requestId) {
        String requestMessageId = null;
        String manageMessageResponse;
        if (StringUtils.isBlank(requestId))
            return requestMessageId;
        Map<String, String> inputMap = new HashMap<String, String>();
        JSONObject manageMessageResponseJSON;
        inputMap.put(ODataQueryConstants.SELECT, "id");
        inputMap.put(ODataQueryConstants.FILTER, "CustomerRequest_id eq '" + requestId + "'");
        inputMap.put(ODataQueryConstants.ORDER_BY, "createdts desc");
        inputMap.put(ODataQueryConstants.TOP, "1");
        manageMessageResponse = Executor.invokeService(ServiceURLEnum.REQUESTMESSAGE_READ, inputMap, null,
                requestInstance);
        manageMessageResponseJSON = CommonUtilities.getStringAsJSONObject(manageMessageResponse);
        if (manageMessageResponseJSON != null && manageMessageResponseJSON.has(FabricConstants.OPSTATUS)
                && manageMessageResponseJSON.optInt(FabricConstants.OPSTATUS) == 0
                && manageMessageResponseJSON.has("requestmessage")) {
            LOG.debug("REQUESTMESSAGE_READ Status:Successful");
            JSONArray requestMessageJSONArray = manageMessageResponseJSON.getJSONArray("requestmessage");
            if (requestMessageJSONArray.length() > 0) {
                JSONObject currMessageJSONObject = requestMessageJSONArray.getJSONObject(0);
                if (currMessageJSONObject.has("id")) {
                    requestMessageId = currMessageJSONObject.optString("id");
                }
            }
        } else {
            LOG.error("REQUESTMESSAGE_READ Status:Failed");
        }
        return requestMessageId;
    }

    public static String getLatestDraftMessageIdOfRequest(DataControllerRequest requestInstance, String requestId) {
        String requestMessageId = null;
        String manageMessageResponse;
        if (StringUtils.isBlank(requestId))
            return requestMessageId;
        Map<String, String> inputMap = new HashMap<String, String>();
        JSONObject manageMessageResponseJSON;
        inputMap.put(ODataQueryConstants.SELECT, "id");
        inputMap.put(ODataQueryConstants.FILTER, "CustomerRequest_id eq '" + requestId + "' and isRead eq'DRAFT'");
        inputMap.put(ODataQueryConstants.ORDER_BY, "createdts desc");
        inputMap.put(ODataQueryConstants.TOP, "1");
        manageMessageResponse = Executor.invokeService(ServiceURLEnum.REQUESTMESSAGE_READ, inputMap, null,
                requestInstance);
        manageMessageResponseJSON = CommonUtilities.getStringAsJSONObject(manageMessageResponse);
        if (manageMessageResponseJSON != null && manageMessageResponseJSON.has(FabricConstants.OPSTATUS)
                && manageMessageResponseJSON.optInt(FabricConstants.OPSTATUS) == 0
                && manageMessageResponseJSON.has("requestmessage")) {
            LOG.debug("REQUESTMESSAGE_READ Status:Successful");
            JSONArray requestMessageJSONArray = manageMessageResponseJSON.getJSONArray("requestmessage");
            if (requestMessageJSONArray.length() > 0) {
                JSONObject currMessageJSONObject = requestMessageJSONArray.getJSONObject(0);
                if (currMessageJSONObject.has("id")) {
                    requestMessageId = currMessageJSONObject.optString("id");
                }
            }
        } else {
            LOG.error("REQUESTMESSAGE_READ Status:Failed");
        }
        return requestMessageId;
    }

    public static Record markAllMessagesOfCustomerRequestAsRead(DataControllerRequest requestInstance,
            CustomerRequestBean customerRequestBeanInstance, String modifedBy) {
        Record operationRecord = new Record();
        operationRecord.setId("operationRecord");
        Param operationStatusCodeParam = new Param("operationStatusCodeParam", "0", FabricConstants.INT);
        operationRecord.addParam(operationStatusCodeParam);

        String requestId = customerRequestBeanInstance.getRequestId();

        boolean isValidData = true;
        if (StringUtils.isBlank(requestId)) {
            operationRecord.addParam(new Param("markMessagesAsRead",
                    "ERROR: 'requestid' is a mandatory input to mark all Messages as read.", FabricConstants.STRING));
            isValidData = false;
        }
        if (StringUtils.isBlank(modifedBy)) {
            operationRecord.addParam(new Param("markMessagesAsRead",
                    "ERROR: 'modifiedby' is a mandatory input to mark all Messages as read.", FabricConstants.STRING));
            isValidData = false;
        }
        if (!isValidData) {
            LOG.error("Invalid Payload. Error Details:" + operationRecord.getParam("markMessagesAsRead").getValue());
            operationStatusCodeParam.setValue("99");
            return operationRecord;
        }

        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put(ODataQueryConstants.SELECT, "id");
        inputMap.put(ODataQueryConstants.FILTER, "CustomerRequest_id eq '" + requestId + "' and IsRead eq 'false'");

        String manageRequestMessageResponse = Executor.invokeService(ServiceURLEnum.REQUESTMESSAGE_READ, inputMap, null,
                requestInstance);
        JSONObject manageRequestMessageResponseJSON = CommonUtilities
                .getStringAsJSONObject(manageRequestMessageResponse);
        inputMap.clear();
        inputMap.put("IsRead", "TRUE");
        inputMap.put("modifiedby", modifedBy);
        if (manageRequestMessageResponseJSON != null && manageRequestMessageResponseJSON.has(FabricConstants.OPSTATUS)
                && manageRequestMessageResponseJSON.optInt(FabricConstants.OPSTATUS) == 0
                && manageRequestMessageResponseJSON.has("requestmessage")) {
            LOG.debug("REQUESTMESSAGE_READ Status:Successful");
            JSONArray requestMessageArray = manageRequestMessageResponseJSON.getJSONArray("requestmessage");
            JSONObject currMessageJSONObject;
            boolean hasErrorOccured = false;
            for (Object currMessageObject : requestMessageArray) {
                currMessageJSONObject = (JSONObject) currMessageObject;
                if (currMessageJSONObject.has("id")) {
                    inputMap.put("id", currMessageJSONObject.optString("id"));
                    manageRequestMessageResponse = Executor.invokeService(ServiceURLEnum.REQUESTMESSAGE_UPDATE,
                            inputMap, null, requestInstance);
                    manageRequestMessageResponseJSON = CommonUtilities
                            .getStringAsJSONObject(manageRequestMessageResponse);
                    if (manageRequestMessageResponseJSON != null
                            && manageRequestMessageResponseJSON.has(FabricConstants.OPSTATUS)
                            && manageRequestMessageResponseJSON.has("updatedRecords")
                            && manageRequestMessageResponseJSON.optInt(FabricConstants.OPSTATUS) == 0
                            && manageRequestMessageResponseJSON.optInt("updatedRecords") >= 1)
                        continue;
                    hasErrorOccured = true;
                }
            }
            if (hasErrorOccured) {
                LOG.debug("Mark all messages as Read Status:Failed/Partially Failed");
                operationStatusCodeParam.setValue(FAILED_OPERATION_CODE);
                operationRecord.addParam(new Param("markMessagesAsRead",
                        "ERROR: Could not set all the Messages of the Request as read", FabricConstants.STRING));
            }
        } else {
            LOG.error("REQUESTMESSAGE_READ Status:Failed");
        }
        return operationRecord;
    }

    public static boolean verifyIfUserBelongsToRequest(String requestId, String customerUsername,
            DataControllerRequest requestInstance) {
        if (requestId == null || requestId.trim().contains(" ")) {
            return false;
        }

        Map<String, String> inputMap = new HashMap<>();
        inputMap.put(ODataQueryConstants.FILTER,
                "customerrequest_id eq '" + requestId + "' and customer_Username eq '" + customerUsername + "'");
        String readCustomerServiceResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_REQUEST_DETAILED_VIEW_READ,
                inputMap, null, requestInstance);
        JSONObject readCustomerServiceResponseJSON = CommonUtilities.getStringAsJSONObject(readCustomerServiceResponse);
        if (readCustomerServiceResponseJSON == null || !readCustomerServiceResponseJSON.has(FabricConstants.OPSTATUS)
                || readCustomerServiceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || !readCustomerServiceResponseJSON.has("customer_request_detailed_view")) {
            LOG.error("CUSTOMER_REQUEST_DETAILED_VIEW_READ Status:Failed");
            return false;
        }

        LOG.debug("CUSTOMER_REQUEST_DETAILED_VIEW_READ Status:Succesfull");
        JSONArray customerRequestRecordsArray = readCustomerServiceResponseJSON
                .getJSONArray("customer_request_detailed_view");
        if (customerRequestRecordsArray.length() == 0) {
            LOG.debug("User Ownership on Request:False");
            return false;
        } else {
            LOG.debug("User Ownership on Request:True");
            return true;
        }

    }

    public static Record softDeleteCustomerRequest(DataControllerRequest requestInstance,
            CustomerRequestBean customerRequestBeanInstance, String softDeleteFlag, String modifedBy) {
        Record operationRecord = new Record();
        operationRecord.setId("operationRecord");
        Param operationStatusCodeParam = new Param("operationStatusCodeParam", "0", FabricConstants.INT);
        operationRecord.addParam(operationStatusCodeParam);

        String requestId = customerRequestBeanInstance.getRequestId();

        boolean isValidData = true;
        if (StringUtils.isBlank(requestId)) {
            operationRecord.addParam(
                    new Param("softDelete", "ERROR: 'requestid' is a mandatory input for a Soft Delete Request."));
            isValidData = false;
        }
        if (StringUtils.isBlank(modifedBy)) {
            operationRecord.addParam(
                    new Param("softDelete", "ERROR: 'modifiedby' is a mandatory input for a Soft Delete Request."));
            isValidData = false;
        }
        boolean hasAccessToDelete = true;
        try {
            hasAccessToDelete = verifyIfUserBelongsToRequest(requestId, modifedBy, requestInstance);
        } catch (Exception ignored) {
        }
        if (!hasAccessToDelete) {
            operationRecord
                    .addParam(new Param("softDelete", "ERROR: user doesn't have access to Soft Delete Request."));
            isValidData = false;
        }
        if (!isValidData) {
            LOG.error("Invalid Payload. Error Details:" + operationRecord.getParam("softDelete").getValue());
            operationStatusCodeParam.setValue(FAILED_OPERATION_CODE);
            return operationRecord;
        }

        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("id", requestId);
        inputMap.put("modifiedby", modifedBy);
        /*
         * inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp()); Do not modify the
         * lastmodifiedts.It is to be updated only when a message is added to the Request
         */

        if (softDeleteFlag.equalsIgnoreCase("TRUE") || softDeleteFlag.equals("1")) {
            // Soft Delete a Request
            inputMap.put("softdeleteflag", "1");
        } else {
            // Restore a SoftDeleted Request
            inputMap.put("softdeleteflag", "0");
        }
        String manageCustomerRequestResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERREQUEST_UPDATE, inputMap,
                null, requestInstance);
        JSONObject manageCustomerRequestResponseJSON = CommonUtilities
                .getStringAsJSONObject(manageCustomerRequestResponse);
        operationRecord.addParam(new Param("softDelete", manageCustomerRequestResponse, FabricConstants.STRING));
        if (manageCustomerRequestResponseJSON != null && manageCustomerRequestResponseJSON.has(FabricConstants.OPSTATUS)
                && manageCustomerRequestResponseJSON.has("updatedRecords")
                && manageCustomerRequestResponseJSON.optInt(FabricConstants.OPSTATUS) == 0
                && manageCustomerRequestResponseJSON.optInt("updatedRecords") >= 1) {
            LOG.debug("Soft Delete Request Status:Successful");
            return operationRecord;
        }
        LOG.debug("Soft Delete Request Status:Failed");
        operationStatusCodeParam.setValue(FAILED_OPERATION_CODE);
        return operationRecord;
    }

    public static Record hardDeleteCustomerRequest(DataControllerRequest requestInstance,
            CustomerRequestBean customerRequestBeanInstance, String hardDeleteFlag, String modifedBy) {
        Record operationRecord = new Record();
        operationRecord.setId("operationRecord");
        Param operationStatusCodeParam = new Param("operationStatusCodeParam", "0", FabricConstants.INT);
        operationRecord.addParam(operationStatusCodeParam);

        String requestId = customerRequestBeanInstance.getRequestId();

        boolean isValidData = true;
        if (StringUtils.isBlank(requestId)) {
            operationRecord.addParam(
                    new Param("hardDelete", "ERROR: 'requestid' is a mandatory input for a Hard Delete Request."));
            isValidData = false;
        }
        if (StringUtils.isBlank(modifedBy)) {
            operationRecord.addParam(
                    new Param("hardDelete", "ERROR: 'modifiedby' is a mandatory input for a Hard Delete Request."));
            isValidData = false;
        }
        boolean hasAccessToDelete = true;
        try {
            hasAccessToDelete = verifyIfUserBelongsToRequest(requestId, modifedBy, requestInstance);
        } catch (Exception ignored) {
        }
        if (!hasAccessToDelete) {
            operationRecord
                    .addParam(new Param("hardDelete", "ERROR: user doesn't have access to Hard Delete Request."));
            isValidData = false;
        }

        if (!isValidData) {
            LOG.error("Invalid Payload. Error Details:" + operationRecord.getParam("hardDelete").getValue());
            operationStatusCodeParam.setValue("99");
            return operationRecord;
        }

        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("id", requestId);
        inputMap.put("modifiedby", modifedBy);
        inputMap.put("Status_id", StatusEnum.SID_DELETED.name()); // Hard Delete a Request
        /*
         * inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp()); Do not modify the
         * lastmodifiedts.It is to be updated only when a message is added to the Request
         */

        String serviceResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERREQUEST_UPDATE, inputMap, null,
                requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        operationRecord.addParam(new Param("hardDelete", serviceResponse, FabricConstants.STRING));
        if (serviceResponseJSON != null && serviceResponseJSON.has(FabricConstants.OPSTATUS)
                && serviceResponseJSON.has("updatedRecords")
                && serviceResponseJSON.optInt(FabricConstants.OPSTATUS) == 0
                && serviceResponseJSON.optInt("updatedRecords") >= 1) {
            LOG.debug("Hard Delete Request Status:Successful");
            return operationRecord;
        }

        LOG.debug("Hard Delete Request Status:Failed");
        operationStatusCodeParam.setValue(FAILED_OPERATION_CODE);
        return operationRecord;
    }

    public static boolean discardMessageAndMessageAttachments(DataControllerRequest requestInstance,
            CustomerRequestBean customerRequestBeanInstance, String requestMessageId) {

        if (StringUtils.isBlank(requestMessageId)) {
            requestMessageId = getLatestDraftMessageIdOfRequest(requestInstance,
                    customerRequestBeanInstance.getRequestId());
            // Discard only a Draft message. Any message which has been sent is not to be
            // deleted
        }

        if (StringUtils.isBlank(requestMessageId)) {
            // No draft message found. Thereby no message is to be deleted
            return true;
        }

        JSONArray messageAttachmentsJSONArray;
        JSONObject serviceResponseJSON, currMessageAttachmentJSONObject;
        String serviceResponse, currMessageAttachmentId, currMediaId;
        Map<String, String> inputMap = new HashMap<>();

        // Fetch Message Attachments
        inputMap.put(ODataQueryConstants.SELECT, "id, Media_id");
        inputMap.put(ODataQueryConstants.FILTER, "RequestMessage_id eq '" + requestMessageId + "'");
        serviceResponse = Executor.invokeService(ServiceURLEnum.MESSAGEATTACHMENT_READ, inputMap, null,
                requestInstance);
        serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.optInt(FabricConstants.OPSTATUS) == 0
                || serviceResponseJSON.has("messageattachment")
                || serviceResponseJSON.optJSONArray("messageattachment") == null) {
            return false;
        }
        messageAttachmentsJSONArray = serviceResponseJSON.getJSONArray("messageattachment");

        // Delete Message Attachments
        for (Object currMessageAttachmentObject : messageAttachmentsJSONArray) {
            currMessageAttachmentJSONObject = (JSONObject) currMessageAttachmentObject;

            if (currMessageAttachmentJSONObject.has("id") && currMessageAttachmentJSONObject.has("Media_id")) {
                currMessageAttachmentId = currMessageAttachmentJSONObject.optString("id");

                currMessageAttachmentId = currMessageAttachmentJSONObject.getString("id");
                inputMap.put("id", currMessageAttachmentId);
                serviceResponse = Executor.invokeService(ServiceURLEnum.MESSAGEATTACHMENT_DELETE, inputMap, null,
                        requestInstance);
                serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
                if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                        || serviceResponseJSON.optInt(FabricConstants.OPSTATUS) == 0) {
                    return false;
                }
                inputMap.clear();
                currMediaId = currMessageAttachmentJSONObject.getString("Media_id");
                inputMap.put("id", currMediaId);
                serviceResponse = Executor.invokeService(ServiceURLEnum.MEDIA_DELETE, inputMap, null, requestInstance);
                serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
                if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                        || serviceResponseJSON.optInt(FabricConstants.OPSTATUS) == 0) {
                    return false;
                }
            }
        }

        // Delete Request Message
        inputMap.clear();
        inputMap.put("id", requestMessageId);
        serviceResponse = Executor.invokeService(ServiceURLEnum.REQUESTMESSAGE_DELETE, inputMap, null, requestInstance);
        serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.optInt(FabricConstants.OPSTATUS) == 0) {
            return false;
        }

        return true;
    }

    public static boolean discardCustomerRequestAndRequestMessages(DataControllerRequest requestInstance,
            CustomerRequestBean customerRequestBeanInstance, boolean discardDraftMessagesOnly) {

        JSONArray requestMessageJSONArray;
        String serviceResponse, currMessageId;
        JSONObject serviceResponseJSON, currMessageJSONObject;
        Map<String, String> inputMap = new HashMap<>();

        // Fetch Request Messages
        String requestId = customerRequestBeanInstance.getRequestId();
        inputMap.put(ODataQueryConstants.SELECT, "id");
        if (discardDraftMessagesOnly) {
            inputMap.put(ODataQueryConstants.FILTER, "CustomerRequest_id eq '" + requestId + "' and isRead eq'DRAFT'");
        } else {
            inputMap.put(ODataQueryConstants.FILTER, "CustomerRequest_id eq '" + requestId + "'");
        }
        serviceResponse = Executor.invokeService(ServiceURLEnum.REQUESTMESSAGE_READ, inputMap, null, requestInstance);
        serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.optInt(FabricConstants.OPSTATUS) == 0
                        && !serviceResponseJSON.has("requestmessage")
                || serviceResponseJSON.optJSONArray("requestmessage") == null) {
            return false;
        }

        // Discard attachments of all Messages
        requestMessageJSONArray = serviceResponseJSON.getJSONArray("requestmessage");
        for (Object requestMessageObject : requestMessageJSONArray) {
            currMessageJSONObject = (JSONObject) requestMessageObject;
            if (currMessageJSONObject.has("id")) {
                currMessageId = currMessageJSONObject.getString("id");
                discardMessageAndMessageAttachments(requestInstance, customerRequestBeanInstance, currMessageId);
            }
        }

        // Discard Request
        inputMap.clear();
        inputMap.put("id", requestId);
        serviceResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERREQUEST_DELETE, inputMap, null,
                requestInstance);
        serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.optInt(FabricConstants.OPSTATUS) != 0) {
            return false;
        }

        return true;
    }

    public static String validateFileTypeAndGetAttachmentTypeId(String fileExtension) {
        if (StringUtils.isBlank(fileExtension))
            return "INVALID_FILE";
        else if (fileExtension.equalsIgnoreCase("txt"))
            return "ATTACH_TYPE_TXT";
        else if (fileExtension.equalsIgnoreCase("doc"))
            return "ATTACH_TYPE_DOC";
        else if (fileExtension.equalsIgnoreCase("docx"))
            return "ATTACH_TYPE_DOCX";
        else if (fileExtension.equalsIgnoreCase("pdf"))
            return "ATTACH_TYPE_PDF";
        else if (fileExtension.equalsIgnoreCase("png"))
            return "ATTACH_TYPE_PNG";
        else if (fileExtension.equalsIgnoreCase("jpeg"))
            return "ATTACH_TYPE_JPEG";
        else if (fileExtension.equalsIgnoreCase("jpg"))
            return "ATTACH_TYPE_JPG";
        return "INVALID_FILE";
    }

    public static String validateCustomerRequestData(List<String> expandedListOfRecipients,
            CustomerRequestBean customerRequestBeanInstance) {
        boolean isValidData = true;
        String requestId = customerRequestBeanInstance.getRequestId();
        String requestSubject = customerRequestBeanInstance.getRequestSubject();
        String requestCategoryId = customerRequestBeanInstance.getRequestCategoryId();
        String requestPriority = customerRequestBeanInstance.getRequestPriority();
        String requestStatus = customerRequestBeanInstance.getRequestStatus();
        String messageDescription = customerRequestBeanInstance.getMessageDescription();

        StringBuffer errorMessageBuffer = new StringBuffer();
        List<FormItem> formItems = customerRequestBeanInstance.getFormItems();
        String attachmentTypeId;
        if (formItems != null && !formItems.isEmpty()) {
            for (FormItem currFormItem : formItems) {
                if (currFormItem.isFile()) {
                    attachmentTypeId = CustomerRequestAndMessagesHandler
                            .validateFileTypeAndGetAttachmentTypeId(currFormItem.getFileExtension());
                    if (attachmentTypeId.equalsIgnoreCase("INVALID_FILE")) {
                        isValidData = false;
                        errorMessageBuffer.append(
                                "Invalid File Type. Allowed file types are .txt .doc .docx .pdf .png .jpeg .jpg");
                        break;
                    }
                }
            }
        }
        if (customerRequestBeanInstance.isCreateRequest()) {
            if (StringUtils.isBlank(requestSubject) || requestSubject.length() < REQUEST_SUBJECT_MIN_CHARS
                    || requestSubject.length() > REQUEST_SUBJECT_MAX_CHARS) {
                isValidData = false;
                errorMessageBuffer.append("Request Subject should have a minimum of " + REQUEST_SUBJECT_MIN_CHARS
                        + " characters and a maximum of " + REQUEST_SUBJECT_MAX_CHARS + " characters.");
            }
            if (StringUtils.isBlank(requestCategoryId)) {
                customerRequestBeanInstance.setRequestCategoryId(DEFAULT_REQUEST_CATEGORY_ID);
                requestCategoryId = DEFAULT_REQUEST_CATEGORY_ID;
            }
            if (StringUtils.isBlank(requestPriority)) {
                customerRequestBeanInstance.setRequestPriority(DEFAULT_REQUEST_PRIORITY);
                requestPriority = DEFAULT_REQUEST_PRIORITY;
            }
            if (StringUtils.isBlank(requestStatus)) {
                requestStatus = DEFAULT_REQUEST_STATUS_ID;
                customerRequestBeanInstance.setRequestStatus(requestStatus);
            }
            /*
             * Validate Message Data. In case of an Invalid Message data, do not proceed with creating a Request
             */
            String decodedMessageDescription = CommonUtilities.decodeFromBase64(messageDescription);
            if (StringUtils.isBlank(decodedMessageDescription)
                    || decodedMessageDescription.length() < REQUEST_MESSAGE_MIN_CHARS) {
                isValidData = false;
                errorMessageBuffer
                        .append("Message Body should have a minimum of " + REQUEST_MESSAGE_MIN_CHARS + " characters.");
            }
        } else {
            if (requestSubject != null) {
                if (StringUtils.isBlank(requestSubject) || requestSubject.length() < REQUEST_SUBJECT_MIN_CHARS
                        || requestSubject.length() > REQUEST_SUBJECT_MAX_CHARS) {
                    isValidData = false;
                    errorMessageBuffer.append("Request Subject should have a minimum of " + REQUEST_SUBJECT_MIN_CHARS
                            + " characters and a maximum of " + REQUEST_SUBJECT_MAX_CHARS + " characters.");
                }
            }
            if (StringUtils.isBlank(requestId)) {
                isValidData = false;
                errorMessageBuffer.append("Request Id is a Mandatory Input in an Update Request/Message call.");
            }

        }
        LOG.debug("Customer Request and Request Message:isValidData:" + isValidData);
        if (isValidData)
            return null;
        LOG.error("Error Information:" + errorMessageBuffer.toString());
        return errorMessageBuffer.toString();
    }

    public static String getCustomerUsernameFromRequestId(String requestId, DataControllerRequest requestInstance) {
        LOG.debug("Attempting to resolve customer username from Request Id");
        String customerUsername = "", customerId;
        Map<String, String> postParameterMap = new HashMap<String, String>();
        postParameterMap.put(ODataQueryConstants.SELECT, "Customer_id");
        postParameterMap.put(ODataQueryConstants.FILTER, "id eq '" + requestId + "'");
        String readCustomerRequestResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERREQUEST_READ,
                postParameterMap, null, requestInstance);
        JSONObject readCustomerRequestResponseJSON = CommonUtilities.getStringAsJSONObject(readCustomerRequestResponse);
        if (readCustomerRequestResponseJSON != null && readCustomerRequestResponseJSON.has(FabricConstants.OPSTATUS)
                && readCustomerRequestResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readCustomerRequestResponseJSON.has("customerrequest")) {
            JSONArray customerRequestRecordsJSONArray = readCustomerRequestResponseJSON.getJSONArray("customerrequest");
            if (customerRequestRecordsJSONArray != null && customerRequestRecordsJSONArray.length() > 0) {
                JSONObject currCustomerJSONObject = customerRequestRecordsJSONArray.getJSONObject(0);
                if (currCustomerJSONObject.has("Customer_id")) {
                    customerId = currCustomerJSONObject.optString("Customer_id");
                    CustomerHandler customerHandler = new CustomerHandler();
                    customerUsername = customerHandler.getCustomerUsername(customerId, requestInstance);
                }
            }

        }
        LOG.debug("Resolved Customer username" + customerUsername);
        return customerUsername;
    }
}