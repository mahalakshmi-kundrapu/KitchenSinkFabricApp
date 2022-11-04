package com.kony.adminconsole.service.customerrequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.exception.InvalidFileNameException;
import com.kony.adminconsole.commons.handler.MultipartPayloadHandler;
import com.kony.adminconsole.commons.handler.MultipartPayloadHandler.FormItem;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.dto.CustomerRequestBean;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.handler.CustomerRequestAndMessagesHandler;
import com.kony.adminconsole.service.authmodule.APICustomIdentityService;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to handle Customer Requests, Messages and the attachments
 *
 * @author Aditya Mankal
 */
public class CustomerRequestAndRequestMessageManageService implements JavaService2 {

    private static final String OPERATION_SUCCESS_CODE = "0";
    private static final String OPERATION_FAILURE_CODE = "-1";

    private static final String DRAFT_MESSAGE_STATUS = "DRAFT";
    private static final int ATTACHMENT_MAX_FILE_SIZE_IN_BYTES = 5000000; // 5MB
    private static final String UPDATE_CUSTOMER_REQUEST_METHOD_ID = "updateCustomerRequest";

    private static final String MEDIA_DOWNLOAD_OBJECT_SERVICE_URL =
            "services/data/v1/CustomerManagementObjService/operations/CustomerRequest/downloadMessageAttachment?mediaId=";

    private static final Logger LOG = Logger.getLogger(CustomerRequestAndRequestMessageManageService.class);

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {

            String csrId;
            String createdby;
            String modifiedBy;
            String messageId;
            String accountId;
            String requestId;
            String customerId;
            String isNewRequest;
            String messageStatus;
            String recipientList;
            String requestStatus;
            String softDeleteFlag;
            String hardDeleteFlag;
            String requestSubject;
            String requestPriority;
            String customerUsername;
            String isDiscardRequest;
            String requestCategoryId;
            String messageDescription;
            String markAllAsReadFlag;
            String discardedAttachments;
            String loggedInCustomerUsername;

            Result processedResult = new Result();

            // Bean containing the Request Information
            CustomerRequestBean customerRequestBean = new CustomerRequestBean();

            /*
             * Data From client is sent as MultiPart-Form-Data or Application/JSON. Below Code is to process the data
             * and to add it to the requestInstance. THE OPERATIONS USING THIS SERVICE CODE MUST BE OF TYPE PASS-THROUGH
             * ONLY IN ALL CASES
             */
            boolean isValidRequestInstance = validateRequestInstanceAndInitialiseBean(requestInstance, processedResult,
                    customerRequestBean);

            if (isValidRequestInstance == false) {
                LOG.error("Invalid Request Payload. Failed to Initialise Request Bean");
                return processedResult;
            }

            // Read Input Headers
            loggedInCustomerUsername = requestInstance.getHeader("username");

            /*
             * Initialize the bean with the data which remains same across customers even in case of message to a group
             * and/or list of customers
             */
            csrId = StringUtils.trim(requestInstance.getParameter("assignedto"));
            customerRequestBean.setCsrId(csrId);

            requestId = StringUtils.trim(requestInstance.getParameter("requestid"));
            customerRequestBean.setRequestId(requestId);

            accountId = StringUtils.trim(requestInstance.getParameter("accountid"));
            customerRequestBean.setAccountId(accountId);

            messageId = StringUtils.trim(requestInstance.getParameter("messageid"));
            customerRequestBean.setMessageId(messageId);

            requestPriority = StringUtils.trim(requestInstance.getParameter("priority"));
            customerRequestBean.setRequestPriority(requestPriority);

            requestStatus = StringUtils.trim(requestInstance.getParameter("requeststatus"));
            customerRequestBean.setRequestStatus(requestStatus);

            messageStatus = StringUtils.trim(requestInstance.getParameter("messagestatus"));
            customerRequestBean.setMessageStatus(messageStatus);

            requestSubject = StringUtils.trim(requestInstance.getParameter("requestsubject"));
            customerRequestBean.setRequestSubject(requestSubject);

            markAllAsReadFlag = StringUtils.trim(requestInstance.getParameter("markallasread"));
            isDiscardRequest = requestInstance.getParameter("isDiscardRequest");

            requestCategoryId = StringUtils.trim(requestInstance.getParameter("requestcategory_id"));
            customerRequestBean.setRequestCategoryId(requestCategoryId);

            messageDescription = StringUtils.trim(requestInstance.getParameter("messagedescription"));
            customerRequestBean.setMessageDescription(messageDescription);

            discardedAttachments = StringUtils.trim(requestInstance.getParameter("discardedAttachments"));
            customerRequestBean.setDiscardedAttachments(discardedAttachments);

            softDeleteFlag = StringUtils.trim(requestInstance.getParameter("softdelete"));
            hardDeleteFlag = StringUtils.trim(requestInstance.getParameter("harddelete"));

            customerUsername = StringUtils.trim(requestInstance.getParameter("username"));
            customerRequestBean.setCurrentUsername(customerUsername);

            createdby = StringUtils.trim(requestInstance.getParameter("createdby"));
            modifiedBy = StringUtils.trim(requestInstance.getParameter("modifiedby"));
            customerId = StringUtils.trim(requestInstance.getParameter("customer_id"));
            isNewRequest = StringUtils.trim(requestInstance.getParameter("isNewRequest"));
            recipientList = StringUtils.trim(requestInstance.getParameter("recipientList"));

            // Fetched User Details from Identity Scope
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            customerRequestBean.setUserDetailsBeanInstance(userDetailsBeanInstance);

            boolean isCreateRequest = StringUtils.isBlank(customerRequestBean.getRequestId());
            customerRequestBean.setCreateRequest(isCreateRequest);
            LOG.debug("isCreateRequest:" + customerRequestBean.isCreateRequest());

            // Check if the Request is from Customer360 (OR) OLB/MB
            if (StringUtils.equalsIgnoreCase(userDetailsBeanInstance.getUserId(),
                    APICustomIdentityService.API_USER_ID)) {

                // Request is from Client Application - OLB/MB
                LOG.debug("Request is from Client Application - OLB/MB");

                createdby = loggedInCustomerUsername;
                // Check the availability of current logged-in user from modifiedBy parameter
                if (StringUtils.isBlank(loggedInCustomerUsername) && StringUtils.isNotBlank(modifiedBy)) {
                    loggedInCustomerUsername = modifiedBy;
                }
                customerRequestBean.setRequestByAdmin(false);
                requestInstance.setAttribute("isServiceBeingAccessedByOLB", true);

                /* Perform Security Checks */
                if (StringUtils.isBlank(loggedInCustomerUsername)) {
                    // Missing mandatory input value - Customer Username
                    LOG.error("Username of the Logged In Customer is a mandatory input, and has not been provided.");
                    ErrorCodeEnum.ERR_20132.setErrorCode(processedResult);
                    return processedResult;
                }

                // Verify if the Customer username in payload and Logged-In Customer's username
                // are the same
                if (!StringUtils.equals(customerUsername, loggedInCustomerUsername)) {
                    // Customer username has been tampered in the input payload
                    LOG.error("Customer username has been tampered in the input payload");
                    ErrorCodeEnum.ERR_21027.setErrorCode(processedResult);
                    return processedResult;
                }

                if (!customerRequestBean.isCreateRequest()) {
                    // Indicates an update/reply of/to an existing request. Verify if the Customer
                    // is the owner of the request
                    String requestOwner = CustomerRequestAndMessagesHandler
                            .getCustomerUsernameFromRequestId(customerRequestBean.getRequestId(), requestInstance);
                    if (!StringUtils.equals(requestOwner, loggedInCustomerUsername)) {
                        LOG.error("Unauthorised Request. Logged In Customer is the not the owner of the request");
                        ErrorCodeEnum.ERR_21027.setErrorCode(processedResult);
                        return processedResult;
                    }
                }

            } else {
                // Request is from Customer 360
                LOG.debug("Request is from Customer 360");
                customerRequestBean.setRequestByAdmin(true);
                customerRequestBean.setCurrentUsername(userDetailsBeanInstance.getUserName());
            }

            LOG.debug("isAdminRequest:" + customerRequestBean.isRequestByAdmin());

            boolean isDraftMessage = StringUtils.equalsIgnoreCase(messageStatus, DRAFT_MESSAGE_STATUS);
            customerRequestBean.setDraftMessage(isDraftMessage);
            LOG.debug("isDraftMessage:" + customerRequestBean.isDraftMessage());

            List<String> invalidRecipientsList = new ArrayList<>();
            List<String> processedListOfRecipients = new ArrayList<>();
            List<String> listOfRecipients = (ArrayList<String>) CommonUtilities
                    .getStringifiedArrayAsList(recipientList);
            CustomerHandler customerHandler = new CustomerHandler();

            if (listOfRecipients != null && listOfRecipients.size() > 0) {
                String currEntry = StringUtils.EMPTY;
                String currGroupId = StringUtils.EMPTY;

                /*
                 * Initial Assumption: Assume the entries to be customer usernames and attempt to resolve Customer Ids
                 */

                // customerUsernameIdMap map contains a mapping between the CustomerUsername and
                // the CustomerId
                Map<String, String> customerUsernameIdMap = customerHandler.getCustomersIdList(requestInstance,
                        listOfRecipients);

                // Removing the username of the resolved customer from the list of recipients
                listOfRecipients.removeAll(customerUsernameIdMap.keySet());

                // Adding the customerId of the resolved customers to the expanded list of
                // recipients
                processedListOfRecipients.addAll(customerUsernameIdMap.values());

                /*
                 * Intermediate Assumption: Assume the unresolved entries in the listOfRecipients to be names of
                 * customer groups Resolve the customerId of the customers who are a part of the group
                 */
                for (int indexVar = 0; indexVar < listOfRecipients.size(); indexVar++) {

                    // currEntry is a Group Name
                    currEntry = listOfRecipients.get(indexVar);

                    // Resolving the Group Id of the current Group by relying on the group name
                    currGroupId = getMemberGroupId(requestInstance, currEntry);
                    if (StringUtils.isBlank(currGroupId)) {
                        // Invalid Entry. Non Existing group and Non Existing customer
                        invalidRecipientsList.add(currEntry);
                        continue;
                    }

                    // Valid Group. Resolve the CustomerId of the customers who are a part of the
                    // group and add to the processed list of recipients
                    processedListOfRecipients
                            .addAll(customerHandler.getCustomersOfAGroup(requestInstance, currGroupId));
                }
            }

            /*
             * Case where the customer username is passed as 'username' - Backward Compatibility with OLB/RB
             */
            if (StringUtils.isNotBlank(customerUsername)) {
                String currCustomerId = customerHandler.getCustomerId(customerUsername, requestInstance);
                if (StringUtils.isNotBlank(currCustomerId) && !processedListOfRecipients.contains(currCustomerId)) {
                    processedListOfRecipients.add(currCustomerId);
                }
            }

            // Removing the duplicate entries from processedListOfRecipients to avoid
            // duplicate requests
            CommonUtilities.removeDuplicatesInList(processedListOfRecipients);

            // Left over entries are deemed to be invalid recipients and are listed in the
            // response
            if (invalidRecipientsList.size() > 0) {
                processedResult.addParam(
                        new Param("invalidRecipientsList", invalidRecipientsList.toString(), FabricConstants.STRING));
            }
            if (processedListOfRecipients.size() == 1) {
                customerRequestBean.setCustomerId(processedListOfRecipients.get(0));
            }

            // Validate the Request Payload
            String errorMessage = CustomerRequestAndMessagesHandler
                    .validateCustomerRequestData(processedListOfRecipients, customerRequestBean);
            if (StringUtils.isNotBlank(errorMessage)) {
                errorMessage = errorMessage.trim();
                LOG.error("Invalid Customer Request Payload. Detailed Error Message:" + errorMessage);
                processedResult.addParam(new Param("validationError", errorMessage, FabricConstants.STRING));
                ErrorCodeEnum.ERR_20131.setErrorCode(processedResult);
                return processedResult;
            }

            // Request to Mark All Messages As Read
            if (StringUtils.equalsIgnoreCase(markAllAsReadFlag, String.valueOf(true))
                    || StringUtils.equals(markAllAsReadFlag, "1")) {
                customerRequestBean.setCreateRequest(false);
                Record markAllAsReadRecord = CustomerRequestAndMessagesHandler
                        .markAllMessagesOfCustomerRequestAsRead(requestInstance, customerRequestBean, modifiedBy);
                checkForOperationSuccessAndPrepareServiceResult(markAllAsReadRecord, processedResult);
                processedResult.addRecord(markAllAsReadRecord);
                return processedResult;
            }

            // Request to Soft Delete a Request
            if (StringUtils.isNotBlank(softDeleteFlag)) {
                customerRequestBean.setCreateRequest(false);
                Record softDeleteRequestRecord = CustomerRequestAndMessagesHandler
                        .softDeleteCustomerRequest(requestInstance, customerRequestBean, softDeleteFlag, modifiedBy);
                checkForOperationSuccessAndPrepareServiceResult(softDeleteRequestRecord, processedResult);
                processedResult.addRecord(softDeleteRequestRecord);
                return processedResult;
            }

            // Request to Hard Delete a Request
            if (StringUtils.equalsIgnoreCase(hardDeleteFlag, String.valueOf(true))
                    || StringUtils.equals(hardDeleteFlag, "1")) {
                customerRequestBean.setCreateRequest(false);
                Record hardDeleteRequestRecord = CustomerRequestAndMessagesHandler
                        .hardDeleteCustomerRequest(requestInstance, customerRequestBean, hardDeleteFlag, modifiedBy);
                checkForOperationSuccessAndPrepareServiceResult(hardDeleteRequestRecord, processedResult);
                processedResult.addRecord(hardDeleteRequestRecord);
                return processedResult;
            }

            // Check if the call is to discard a Request
            if (isDiscardRequest != null && isDiscardRequest.equalsIgnoreCase(String.valueOf(true))) {
                customerRequestBean.setCreateRequest(false);
                boolean isDiscardRequestSuccessful = true;
                if (isNewRequest != null && isNewRequest.equalsIgnoreCase(String.valueOf(true))) {
                    /*
                     * Discard call on a Request which hasn't been sent yet. Request is currently in Draft state.
                     * Discard all data related to Request
                     */
                    isDiscardRequestSuccessful = CustomerRequestAndMessagesHandler
                            .discardCustomerRequestAndRequestMessages(requestInstance, customerRequestBean, false);
                } else {
                    /*
                     * Discard call on a new message of an Existing Request. Discard only the current Message and it's
                     * Attachments(if any)
                     */
                    isDiscardRequestSuccessful = CustomerRequestAndMessagesHandler
                            .discardMessageAndMessageAttachments(requestInstance, customerRequestBean, messageId);
                }
                if (!isDiscardRequestSuccessful) {
                    ErrorCodeEnum.ERR_20120.setErrorCode(processedResult);
                }
                processedResult.addParam(new Param("status", "success", FabricConstants.STRING));
                return processedResult;
            }

            if (processedListOfRecipients.size() == 1 || methodId.equalsIgnoreCase(UPDATE_CUSTOMER_REQUEST_METHOD_ID)) {
                // Implies a Create/Reply to a Single Recipient OR Update Request

                if (processedListOfRecipients.size() == 1) {
                    // True in case of a Create/Reply to a Single Recipient
                    LOG.debug("Create/Reply to a Single Recipient");
                    if (customerRequestBean.isCreateRequest()) {
                        // Implies creation of a new Customer Request
                        LOG.debug("Scenario: Create New Customer Request");
                        requestId = "REQ" + CommonUtilities.getNumericId();
                        LOG.debug("Generated Request Id:" + requestId);
                        customerRequestBean.setRequestId(requestId);
                    }
                    customerId = processedListOfRecipients.get(0);
                }

                if (StringUtils.isNotBlank(customerUsername) && StringUtils.isBlank(customerId)) {
                    // Input - CustomerUsername only. Resolve Customer Id
                    customerId = customerHandler.getCustomerId(customerUsername, requestInstance);
                } else if (StringUtils.isNotBlank(customerId) && StringUtils.isBlank(customerUsername)) {
                    // Input - CustomerId only. Resolve Customer username
                    customerUsername = customerHandler.getCustomerUsername(customerId, requestInstance);
                }

                // Relying on the parameter createdby in case of the Customer not being resolved
                // in above 2 cases
                if (StringUtils.isBlank(customerUsername) && StringUtils.isNotBlank(createdby)) {
                    customerUsername = createdby;
                    customerId = customerHandler.getCustomerId(createdby, requestInstance);
                }
                if (methodId.equalsIgnoreCase(UPDATE_CUSTOMER_REQUEST_METHOD_ID)
                        && StringUtils.isBlank(customerUsername)
                        && StringUtils.isNotBlank(customerRequestBean.getRequestId())) {
                    // Resolving the Customer username from the Request in case of Customer not
                    // being resolved in above 3 cases
                    customerUsername = CustomerRequestAndMessagesHandler
                            .getCustomerUsernameFromRequestId(customerRequestBean.getRequestId(), requestInstance);
                }

                if (customerRequestBean.isRequestByAdmin() == false) {
                    customerRequestBean.setCurrentUsername(customerUsername);
                }

                customerRequestBean.setCustomerId(customerId);
                customerRequestBean.setCustomerUsername(customerUsername);

                if (customerRequestBean.isRequestByAdmin() == true) {
                    JSONArray listOfDisacardedAttachmentsJSONArray = CommonUtilities
                            .getStringAsJSONArray(discardedAttachments);
                    if (listOfDisacardedAttachmentsJSONArray != null
                            && listOfDisacardedAttachmentsJSONArray.length() > 0
                            && (StringUtils.equalsIgnoreCase(isDiscardRequest, String.valueOf(true))
                                    || StringUtils.equalsIgnoreCase(isDiscardRequest, "1"))) {
                        // Request is to discard an attachment
                        Record discardAttachmentsRecord = processDiscardedMessageAttachments(requestInstance,
                                customerRequestBean);
                        checkForOperationSuccessAndPrepareServiceResult(discardAttachmentsRecord, processedResult);
                        processedResult.addRecord(discardAttachmentsRecord);
                        return processedResult;
                    }
                }

                // Fetch Customer Details and Initialise Bean
                JSONObject customerDetailsJSONObject = customerHandler.getCustomerNameDetails(customerUsername,
                        requestInstance);
                if (customerDetailsJSONObject != null) {
                    customerRequestBean.setCustomerFirstName(customerDetailsJSONObject.optString("FirstName"));
                    customerRequestBean.setCustomerMiddleName(customerDetailsJSONObject.optString("MiddleName"));
                    customerRequestBean.setCustomerLastName(customerDetailsJSONObject.optString("LastName"));
                    customerRequestBean.setCustomerSalutation(customerDetailsJSONObject.optString("Salutation"));
                }

                Record currCustomerRecord = new Record();
                currCustomerRecord
                        .setId(StringUtils.isBlank(customerUsername) ? "unresolvedCustomerUsername" : customerUsername);

                Record customerRequestRecord = processCustomerRequest(requestInstance, customerRequestBean);
                customerRequestRecord.setId("customerRequest");
                currCustomerRecord.addRecord(customerRequestRecord);

                boolean isProcessCustomerRequestSuccessful = checkForOperationSuccessAndPrepareServiceResult(
                        customerRequestRecord, processedResult);
                LOG.debug("isProcessRequestSuccessful:" + isProcessCustomerRequestSuccessful);

                if (isProcessCustomerRequestSuccessful == true) {
                    Record processRequestMessageRecord = processRequestMessage(requestInstance, customerRequestBean);
                    if (processRequestMessageRecord != null) {
                        processRequestMessageRecord.setId("requestMessage");
                        currCustomerRecord.addRecord(processRequestMessageRecord);
                        checkForOperationSuccessAndPrepareServiceResult(processRequestMessageRecord, processedResult);
                    }
                }
                processedResult.addRecord(currCustomerRecord);
                return processedResult;

            } else if (processedListOfRecipients.size() > 1) {

                // Implies a Create Request to a group and/or multiple customers
                LOG.debug("Create Request to a group and/or multiple customers");
                boolean hasErrorOccured = false;
                boolean currentMessageSuccessState = true;

                Record operationRecord = new Record();
                operationRecord.setId("operationRecord");
                processedResult.addRecord(operationRecord);

                customerRequestBean.setCreateRequest(true);
                customerRequestBean.setCurrentUsername(userDetailsBeanInstance.getUserName());

                // customerUsernameMap is a map between customer Id's and Customer usernames
                Map<String, String> customerUsernameMap = customerHandler.getCustomersUsernameList(requestInstance,
                        processedListOfRecipients);

                for (int indexVar = 0; indexVar < processedListOfRecipients.size(); indexVar++) {
                    currentMessageSuccessState = true;
                    customerId = processedListOfRecipients.get(indexVar);
                    customerUsername = customerUsernameMap.get(customerId);
                    if (StringUtils.isBlank(customerUsername)) {
                        continue;
                    }
                    requestId = "REQ" + CommonUtilities.getNumericId();
                    customerRequestBean.setRequestId(requestId);
                    customerRequestBean.setCustomerId(customerId);
                    customerRequestBean.setCustomerUsername(customerUsername);
                    Record processCustomerRequestRecord = processCustomerRequest(requestInstance, customerRequestBean);
                    boolean isProcessRequestSuccessful = checkForOperationSuccessState(processCustomerRequestRecord);
                    if (isProcessRequestSuccessful == true) {
                        LOG.debug("Processed Request for Customer with Id:" + customerRequestBean.getCustomerId());
                        Record processRequestMessageRecord = processRequestMessage(requestInstance,
                                customerRequestBean);
                        boolean isProcessRequestMessageSuccesful = checkForOperationSuccessState(
                                processRequestMessageRecord);
                        if (isProcessRequestMessageSuccesful == true) {
                            LOG.debug("Processed Request Message for Customer with Id:"
                                    + customerRequestBean.getCustomerId());
                        } else {
                            LOG.error("Error in Processing Request Message for Customer with Id:"
                                    + customerRequestBean.getCustomerId());
                            hasErrorOccured = true;
                            currentMessageSuccessState = false;
                        }
                    } else {
                        LOG.error(
                                "Error in Processing Request. Customer with Id:" + customerRequestBean.getCustomerId());
                        hasErrorOccured = true;
                        currentMessageSuccessState = false;
                    }
                    operationRecord.addParam(new Param(customerUsername, Boolean.toString(currentMessageSuccessState),
                            FabricConstants.STRING));

                    // Reset bean
                    customerRequestBean.setRequestId(null);
                    customerRequestBean.setCustomerId(null);
                    customerRequestBean.setCustomerUsername(null);
                    customerRequestBean.setMessageId(null);
                }

                if (hasErrorOccured) {
                    ErrorCodeEnum.ERR_20131.setErrorCode(processedResult);
                }
            } else {
                // Implies an invalid group/customer. Or group without any customers
                LOG.error("Invalid group/customer. Or group without any customers");
                Record operationRecord = new Record();
                operationRecord.setId("operationRecord");
                processedResult.addParam(new Param("customerRequest",
                        "Invalid group/customer name (OR) Group may not have any customers assigned"));
                processedResult.addRecord(operationRecord);
                ErrorCodeEnum.ERR_20138.setErrorCode(processedResult);
            }
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

    private Record processCustomerRequest(DataControllerRequest requestInstance,
            CustomerRequestBean customerRequestBean) {

        Record operationRecord = new Record();
        operationRecord.addParam(new Param("requestId", customerRequestBean.getRequestId(), FabricConstants.STRING));
        Param operationStatusCodeParam = new Param("statusCode", OPERATION_SUCCESS_CODE, FabricConstants.INT);
        operationRecord.addParam(operationStatusCodeParam);

        String requestId = customerRequestBean.getRequestId();
        String requestSubject = customerRequestBean.getRequestSubject();
        String requestCategoryId = customerRequestBean.getRequestCategoryId();
        String customerId = customerRequestBean.getCustomerId();
        String requestPriority = customerRequestBean.getRequestPriority();
        String assignedCSRId = customerRequestBean.getCsrId();
        String accountId = customerRequestBean.getAccountId();
        String requestStatus = customerRequestBean.getRequestStatus();
        String userId = customerRequestBean.getUserDetailsBeanInstance().getUserId();

        StringBuffer errorMessageBuffer = new StringBuffer();
        errorMessageBuffer.append("ERROR:");

        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("id", requestId);
        LOG.debug("isCreateRequest:" + customerRequestBean.isCreateRequest());

        if (customerRequestBean.isCreateRequest()) {
            if (StringUtils.isBlank(assignedCSRId)) {
                // Default behavior - Assigned to Current Logged in CSR
                customerRequestBean.setCsrId(userId);
                assignedCSRId = userId;
            }
            inputMap.put("RequestCategory_id", requestCategoryId);
            inputMap.put("Customer_id", customerId);
            inputMap.put("Status_id", requestStatus);
            inputMap.put("Priority", requestPriority);
            inputMap.put("RequestSubject", requestSubject);
            if (customerRequestBean.isRequestByAdmin()) {
                // Set AssignedTo only when the call is from an Admin
                inputMap.put("AssignedTo", assignedCSRId);
            }

            inputMap.put("Accountid", accountId);
            inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
            inputMap.put("createdby", customerRequestBean.getCurrentUsername());
        } else { // Update an Existing Request

            if (StringUtils.isNotBlank(requestSubject)) {
                inputMap.put("RequestSubject", requestSubject);
            }
            if (StringUtils.isNotBlank(customerId)) {
                inputMap.put("Customer_id", customerId);
            }
            if (StringUtils.isNotBlank(requestCategoryId)) {
                inputMap.put("RequestCategory_id", requestCategoryId);
            }
            if (StringUtils.isNotBlank(requestPriority)) {
                inputMap.put("Priority", requestPriority);
            }
            if (StringUtils.isNotBlank(assignedCSRId) && customerRequestBean.isRequestByAdmin()) {
                inputMap.put("AssignedTo", assignedCSRId);
            }
            if (StringUtils.isNotBlank(requestStatus)) {
                inputMap.put("Status_id", requestStatus);
            }
            if (StringUtils.isNotBlank(accountId)) {
                inputMap.put("Accountid", accountId);
            }
            inputMap.put("modifiedby", customerRequestBean.getCurrentUsername());
            if (StringUtils.isNotBlank(CommonUtilities.decodeFromBase64(customerRequestBean.getMessageDescription()))) {
                inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
                /*
                 * Do not change the lastmodifiedts of a Request in case of an update call without a message description
                 */
            }
        }
        if (customerRequestBean.isRequestByAdmin()) {
            inputMap.put("lastupdatedbycustomer", "0");
        } else {
            inputMap.put("lastupdatedbycustomer", "1");
        }

        String serviceResponse;
        EventEnum eventEnum = null;
        ActivityStatusEnum activityStatusEnum = null;
        if (customerRequestBean.isCreateRequest()) {
            eventEnum = EventEnum.CREATE;
            serviceResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERREQUEST_CREATE, inputMap, null,
                    requestInstance);
        } else {
            eventEnum = EventEnum.UPDATE;
            serviceResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERREQUEST_UPDATE, inputMap, null,
                    requestInstance);
        }

        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON != null && serviceResponseJSON.has(FabricConstants.OPSTATUS)
                && serviceResponseJSON.optInt(FabricConstants.OPSTATUS) == 0) {
            LOG.debug("Process Customer Request Successful");
            activityStatusEnum = ActivityStatusEnum.SUCCESSFUL;

        } else {
            LOG.error("Manage Customer Request Failed");
            activityStatusEnum = ActivityStatusEnum.FAILED;
            operationRecord.addParam(new Param("errorMessage", serviceResponse, FabricConstants.STRING));
            operationStatusCodeParam.setValue(OPERATION_FAILURE_CODE);
            if (customerRequestBean.isCreateRequest()) {
                operationRecord.removeParamByName("requestId");
            }
        }

        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MESSAGES, eventEnum, activityStatusEnum,
                "Request subject: " + requestSubject);
        return operationRecord;
    }

    private Record processRequestMessage(DataControllerRequest requestInstance,
            CustomerRequestBean customerRequestBean) {

        Record operationRecord = new Record();
        Map<String, String> inputMap = new HashMap<>();
        Param statusCodeParam = new Param("statusCode", OPERATION_SUCCESS_CODE, FabricConstants.INT);
        operationRecord.addParam(statusCodeParam);

        boolean isExistingMessage = false;

        String messageId = customerRequestBean.getMessageId();
        String customerRequestId = customerRequestBean.getRequestId();
        String messageDescription = customerRequestBean.getMessageDescription();
        String repliedBy, repliedByName, repliedById;

        if (StringUtils.isBlank(messageDescription) && !customerRequestBean.isCreateRequest()) {
            return null; // Implies a case of Update Request where there is no Message Data.
        }

        if (customerRequestBean.isRequestByAdmin()) {
            repliedById = customerRequestBean.getUserDetailsBeanInstance().getUserId();
            repliedByName = customerRequestBean.getUserDetailsBeanInstance().getFirstName() + " "
                    + customerRequestBean.getUserDetailsBeanInstance().getMiddleName() + " "
                    + customerRequestBean.getUserDetailsBeanInstance().getLastName();
            repliedBy = "ADMIN|CSR";
        } else {
            repliedById = customerRequestBean.getCustomerId();
            repliedByName = customerRequestBean.getCustomerSalutation() + customerRequestBean.getCustomerFirstName()
                    + " " + customerRequestBean.getCustomerMiddleName() + " "
                    + customerRequestBean.getCustomerLastName();
            repliedBy = "CUSTOMER";
        }
        LOG.debug("Replied By:" + repliedBy);

        if (StringUtils.isNotBlank(messageId)) {
            // Indicates an existing message in Draft State. It is to be updated with latest
            // content and status
            isExistingMessage = true;
        } else {
            messageId = CommonUtilities.getNewId().toString(); // Create a new message
            customerRequestBean.setMessageId(messageId);
        }
        LOG.debug("isExistingMessage:" + isExistingMessage);
        operationRecord.addParam(new Param("messageId", messageId, FabricConstants.STRING));

        int replySequence = 0; // Default Message Reply Sequence.
        if (!customerRequestBean.isCreateRequest()) {
            // Fetch the Message Reply sequence of the latest message in case of an existing
            // request
            inputMap.put(ODataQueryConstants.SELECT, "ReplySequence");
            inputMap.put(ODataQueryConstants.FILTER,
                    "CustomerRequest_id eq '" + customerRequestBean.getRequestId() + "'");
            inputMap.put(ODataQueryConstants.ORDER_BY, "ReplySequence desc");
            inputMap.put(ODataQueryConstants.TOP, "1");
            String readRequestMessageResponse = Executor.invokeService(ServiceURLEnum.REQUESTMESSAGE_READ, inputMap,
                    null, requestInstance);
            JSONObject readRequestMessageResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readRequestMessageResponse);
            if (readRequestMessageResponseJSON != null && readRequestMessageResponseJSON.has(FabricConstants.OPSTATUS)
                    && readRequestMessageResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readRequestMessageResponseJSON.has("requestmessage")) {
                JSONArray requestMessageRecords = readRequestMessageResponseJSON.getJSONArray("requestmessage");
                if (requestMessageRecords.length() >= 1) {
                    if (requestMessageRecords.getJSONObject(0).has("ReplySequence")) {
                        replySequence = requestMessageRecords.getJSONObject(0).optInt("ReplySequence");
                    }
                }
            }
        }
        replySequence++;

        inputMap.clear();
        inputMap.put("id", messageId);
        inputMap.put("CustomerRequest_id", customerRequestId);
        inputMap.put("MessageDescription", messageDescription);
        inputMap.put("rtx", "MessageDescription");// Used in Preprocessor to decode the encoded message description
        inputMap.put("RepliedBy", repliedBy);
        inputMap.put("RepliedBy_Name", repliedByName);
        inputMap.put("RepliedBy_id", repliedById);
        inputMap.put("ReplySequence", Integer.toString(replySequence));
        if (StringUtils.equalsIgnoreCase(customerRequestBean.getMessageStatus(), DRAFT_MESSAGE_STATUS)) {
            inputMap.put("IsRead", DRAFT_MESSAGE_STATUS);
        } else {
            inputMap.put("IsRead", customerRequestBean.isRequestByAdmin() ? "FALSE" : "TRUE");
        }

        String operationResponse;
        if (isExistingMessage == false) {
            inputMap.put("createdby", customerRequestBean.getCurrentUsername());
            inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
            operationResponse = Executor.invokeService(ServiceURLEnum.REQUESTMESSAGE_CREATE, inputMap, null,
                    requestInstance);
        } else {
            inputMap.put("modifiedby", customerRequestBean.getCurrentUsername());
            inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            operationResponse = Executor.invokeService(ServiceURLEnum.REQUESTMESSAGE_UPDATE, inputMap, null,
                    requestInstance);
        }
        JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
        if (operationResponseJSON != null && operationResponseJSON.has(FabricConstants.OPSTATUS)
                && operationResponseJSON.optInt(FabricConstants.OPSTATUS) == 0) {

            LOG.debug("Manage Request Message Successful");
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MESSAGES, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL,
                    "Request message updation successful. Request subject:" + customerRequestBean.getRequestSubject());

            inputMap.clear();
            inputMap.put("id", customerRequestId);
            inputMap.put("lastupdatedbycustomer", customerRequestBean.isRequestByAdmin() ? "0" : "1");
            inputMap.put("modifiedby", customerRequestBean.getCurrentUsername());
            inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            Executor.invokeService(ServiceURLEnum.CUSTOMERREQUEST_UPDATE, inputMap, null, requestInstance);

            // Process Message Attachments on Successful Creation of Request Message
            if (operationResponseJSON != null && operationResponseJSON.has("requestmessage")) {
                operationRecord.addRecord(processMessageAttachments(requestInstance, customerRequestBean));
            }

        } else {
            LOG.error("Manage Request Message Failure");
            operationRecord.removeParamByName("messageId");
            operationRecord.addParam(new Param("errorMessage", operationResponse, FabricConstants.STRING));
            statusCodeParam.setValue(OPERATION_FAILURE_CODE);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.MESSAGES, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED,
                    "Request message updation failed. Request subject:" + customerRequestBean.getRequestSubject());
        }

        return operationRecord;
    }

    private Record processMessageAttachments(DataControllerRequest requestInstance,
            CustomerRequestBean customerRequestBeanInstance) {

        Record attachmentsRecord = new Record();
        attachmentsRecord.setId("attachments");

        Record uploadAttachments = processUploadedMessageAttachments(requestInstance, customerRequestBeanInstance);
        if (uploadAttachments != null) {
            attachmentsRecord.addRecord(uploadAttachments);
        }

        Record discardAttachments = processDiscardedMessageAttachments(requestInstance, customerRequestBeanInstance);
        if (discardAttachments != null) {
            attachmentsRecord.addRecord(discardAttachments);
        }

        return attachmentsRecord;
    }

    private Record processUploadedMessageAttachments(DataControllerRequest requestInstance,
            CustomerRequestBean customerRequestBean) {

        boolean hasAttachments = false;

        Record operationRecord = new Record();
        operationRecord.setId("upload");

        Dataset uploadAttachmentsDataset = new Dataset();
        uploadAttachmentsDataset.setId("uploadAttachments");
        operationRecord.addDataset(uploadAttachmentsDataset);

        String messageAttachmentId, requestMessageId, attachmentTypeId, mediaId, serviceResponse;
        requestMessageId = customerRequestBean.getMessageId();

        Map<String, String> inputMap = new HashMap<>();
        Map<String, String> mediaCollection = customerRequestBean.getMediaCollection();

        if (mediaCollection == null) {
            mediaCollection = new HashMap<>();
            customerRequestBean.setMediaCollection(mediaCollection);
        }

        JSONObject manageMessageAttachmentResponseJSON;
        List<FormItem> formItems = customerRequestBean.getFormItems();

        Map<String, String> messageAttachments = getMessageAttachments(requestMessageId, requestInstance);

        if (formItems != null && !formItems.isEmpty()) {
            for (FormItem currFormItem : formItems) {

                if (currFormItem.isFile()) {

                    if (messageAttachments.containsKey(currFormItem.getFileName())) {
                        // Attachment has alreay been uploaded during a draft request
                        continue;
                    }

                    hasAttachments = true;
                    Record currAttachmentRecord = new Record();
                    currAttachmentRecord
                            .addParam(new Param("fileName", currFormItem.getFileName(), FabricConstants.STRING));

                    // Validate Attachment File Type
                    attachmentTypeId = CustomerRequestAndMessagesHandler
                            .validateFileTypeAndGetAttachmentTypeId(currFormItem.getFileExtension());
                    if (attachmentTypeId.equalsIgnoreCase("INVALID_FILE")) {
                        currAttachmentRecord.addParam(new Param("errorMessage",
                                "Invalid File Type. Allowed file types are .txt .doc .docx .pdf .png .jpeg .jpg",
                                FabricConstants.STRING));
                        continue;
                    }

                    if (currFormItem.getFileSize() > ATTACHMENT_MAX_FILE_SIZE_IN_BYTES) {
                        currAttachmentRecord
                                .addParam(
                                        new Param("errorMessage",
                                                "Invalid File Size.File Size cannot be greater than "
                                                        + ATTACHMENT_MAX_FILE_SIZE_IN_BYTES + "Bytes",
                                                FabricConstants.STRING));
                        continue;
                    }

                    if (mediaCollection.containsKey(currFormItem.getFileName())) {
                        // Existing Attachment. Reuse the entry from Media Table
                        mediaId = mediaCollection.get(currFormItem.getFileName());
                        currAttachmentRecord.addParam(
                                new Param("isExistingAttachment", String.valueOf(true), FabricConstants.STRING));
                    } else {
                        // New Attachment - Upload File
                        mediaId = CommonUtilities.getNewId().toString();
                        inputMap.put("id", mediaId);
                        inputMap.put("Name", currFormItem.getFileName());
                        inputMap.put("Type", currFormItem.getFileContentType());
                        inputMap.put("Description", requestMessageId + " MEDIA: " + currFormItem.getFileName());
                        String downloadURL = "/" + MEDIA_DOWNLOAD_OBJECT_SERVICE_URL + mediaId + "&authToken=";
                        inputMap.put("Url", downloadURL);
                        inputMap.put("Content", CommonUtilities.encodeFile(currFormItem.getFile()));
                        inputMap.put("Size", Long.toString(currFormItem.getFileSize()));
                        inputMap.put("createdby", customerRequestBean.getCurrentUsername());
                        inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
                        serviceResponse = Executor.invokeService(ServiceURLEnum.MEDIA_CREATE, inputMap, null,
                                requestInstance);
                        manageMessageAttachmentResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);

                        currAttachmentRecord.addParam(
                                new Param("isExistingAttachment", String.valueOf(false), FabricConstants.STRING));
                        if (manageMessageAttachmentResponseJSON == null
                                || !manageMessageAttachmentResponseJSON.has(FabricConstants.OPSTATUS)
                                        && manageMessageAttachmentResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                            // Failed file upload
                            continue;
                        }
                        mediaCollection.put(currFormItem.getFileName(), mediaId);
                    }

                    // Link uploaded attachment to message
                    inputMap.clear();
                    messageAttachmentId = CommonUtilities.getNewId().toString();
                    inputMap.put("id", messageAttachmentId);
                    inputMap.put("RequestMessage_id", requestMessageId);
                    inputMap.put("AttachmentType_id", attachmentTypeId);
                    inputMap.put("Media_id", mediaId);
                    inputMap.put("createdby", customerRequestBean.getCurrentUsername());
                    inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
                    serviceResponse = Executor.invokeService(ServiceURLEnum.MESSAGEATTACHMENT_CREATE, inputMap, null,
                            requestInstance);
                    uploadAttachmentsDataset.addRecord(currAttachmentRecord);
                    currAttachmentRecord
                            .addParam(new Param("attachmentId", messageAttachmentId, FabricConstants.STRING));
                    currAttachmentRecord.addParam(new Param("mediaId", mediaId, FabricConstants.STRING));

                }

            }
        }

        return hasAttachments ? operationRecord : null;
    }

    private Map<String, String> getMessageAttachments(String messageId, DataControllerRequest requestInstance) {

        Map<String, String> messageAttachmentNames = new HashMap<>();
        if (requestInstance == null || StringUtils.isBlank(messageId)) {
            return messageAttachmentNames;
        }

        // Fetch Media Ids
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put(ODataQueryConstants.FILTER, "RequestMessage_id eq '" + messageId + "'");
        inputMap.put(ODataQueryConstants.SELECT, "Media_id");
        String serviceResponse = Executor.invokeService(ServiceURLEnum.MESSAGEATTACHMENT_READ, inputMap, null,
                requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || !serviceResponseJSON.has("messageattachment")) {
            LOG.error("Failed CRUD Operation. Read Message Attachment. Response:" + serviceResponse);
            return messageAttachmentNames;
        }
        JSONObject currJSON = null;
        List<String> mediaIds = new ArrayList<>();

        JSONArray messageAttachmentIdsArray = serviceResponseJSON.optJSONArray("messageattachment");
        if (messageAttachmentIdsArray != null && messageAttachmentIdsArray.length() > 0) {
            for (Object currObject : messageAttachmentIdsArray) {
                if (currObject instanceof JSONObject) {
                    currJSON = (JSONObject) currObject;
                    if (currJSON.has("Media_id")) {
                        mediaIds.add(currJSON.optString("Media_id"));
                    }
                }
            }
        }
        if (mediaIds.isEmpty()) {
            return messageAttachmentNames;
        }

        // Fetch File Names
        inputMap.clear();
        StringBuffer filterQueryBuffer = new StringBuffer();
        for (String mediaId : mediaIds) {
            filterQueryBuffer.append("id eq '" + mediaId + "' or ");
        }
        String filerQuery = filterQueryBuffer.toString();
        filerQuery = CommonUtilities.replaceLastOccuranceOfString(filerQuery, " or ", StringUtils.EMPTY);
        filerQuery = filerQuery.trim();
        inputMap.put(ODataQueryConstants.FILTER, filerQuery);
        inputMap.put(ODataQueryConstants.SELECT, "id,Name");
        serviceResponse = Executor.invokeService(ServiceURLEnum.MEDIA_READ, inputMap, null, requestInstance);
        serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0 || !serviceResponseJSON.has("media")) {
            LOG.error("Failed CRUD Operation. Read Media. Response:" + serviceResponse);
            return messageAttachmentNames;
        }
        JSONArray mediaIdsArray = serviceResponseJSON.optJSONArray("media");
        if (mediaIdsArray != null && mediaIdsArray.length() > 0) {
            String mediaId, attachmentName;
            for (Object currObject : mediaIdsArray) {
                if (currObject instanceof JSONObject) {
                    currJSON = (JSONObject) currObject;
                    if (currJSON.has("id") && currJSON.has("Name")) {
                        mediaId = currJSON.optString("id");
                        attachmentName = currJSON.optString("Name");
                        messageAttachmentNames.put(attachmentName, mediaId);
                    }
                }
            }
        }

        return messageAttachmentNames;
    }

    private Record processDiscardedMessageAttachments(DataControllerRequest requestInstance,
            CustomerRequestBean customerRequestBean) {

        String discardedAttachments = customerRequestBean.getDiscardedAttachments();
        JSONArray discardedAttachmentsJSONArray = CommonUtilities.getStringAsJSONArray(discardedAttachments);

        if (discardedAttachmentsJSONArray != null) {

            Record operationRecord = new Record();
            operationRecord.setId("discardAttachments");

            JSONArray messageAttachmentsJSONArray;
            JSONObject responseJSON, currMessageAttachmentJSONObject;
            String mediaId, currMessageAttachmentId, response;
            Map<String, String> inputMap = new HashMap<>();

            String requestMessageId = customerRequestBean.getMessageId();

            for (Object currMediaObject : discardedAttachmentsJSONArray) {
                mediaId = currMediaObject.toString();

                inputMap.put(ODataQueryConstants.SELECT, "id, Media_id");
                inputMap.put(ODataQueryConstants.FILTER,
                        "RequestMessage_id eq '" + requestMessageId + "' and Media_id eq '" + mediaId + "'");
                inputMap.put(ODataQueryConstants.TOP, "1");
                response = Executor.invokeService(ServiceURLEnum.MESSAGEATTACHMENT_READ, inputMap, null,
                        requestInstance);
                responseJSON = CommonUtilities.getStringAsJSONObject(response);

                if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
                        && responseJSON.optInt(FabricConstants.OPSTATUS) == 0
                        && responseJSON.has("messageattachment")) {

                    // Fetch Message Attachment-Id
                    messageAttachmentsJSONArray = responseJSON.getJSONArray("messageattachment");

                    if (messageAttachmentsJSONArray.length() > 0) {
                        currMessageAttachmentJSONObject = messageAttachmentsJSONArray.optJSONObject(0);
                        if (currMessageAttachmentJSONObject != null && currMessageAttachmentJSONObject.has("id")
                                && currMessageAttachmentJSONObject.has("Media_id")) {

                            // Delete Message Attachment Entry
                            currMessageAttachmentId = currMessageAttachmentJSONObject.getString("id");
                            inputMap.put("id", currMessageAttachmentId);
                            Executor.invokeService(ServiceURLEnum.MESSAGEATTACHMENT_DELETE, inputMap, null,
                                    requestInstance);

                            // Delete Media Entry
                            inputMap.clear();
                            mediaId = currMessageAttachmentJSONObject.getString("Media_id");
                            inputMap.put("id", mediaId);
                            Executor.invokeService(ServiceURLEnum.MEDIA_DELETE, inputMap, null, requestInstance);
                        }
                    }

                }

                operationRecord.addParam(new Param("mediaId", mediaId, FabricConstants.STRING));
            }
            return operationRecord;
        }
        return null;
    }

    private String getMemberGroupId(DataControllerRequest requestInstance, String groupName) {

        String groupId = StringUtils.EMPTY;
        if (StringUtils.isBlank(groupName)) {
            return groupName;
        }

        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(ODataQueryConstants.SELECT, "id");
        queryMap.put(ODataQueryConstants.FILTER, "Name eq '" + groupName + "'");
        queryMap.put(ODataQueryConstants.TOP, "1");

        String readMemberGroupResponse = Executor.invokeService(ServiceURLEnum.MEMBERGROUP_READ, queryMap, null,
                requestInstance);
        JSONObject readMemberGroupResponseJSON = CommonUtilities.getStringAsJSONObject(readMemberGroupResponse);

        if (readMemberGroupResponseJSON != null && readMemberGroupResponseJSON.has(FabricConstants.OPSTATUS)
                && readMemberGroupResponseJSON.optInt(FabricConstants.OPSTATUS) == 0
                && readMemberGroupResponseJSON.has("membergroup")) {

            JSONArray groupJSONArray = readMemberGroupResponseJSON.getJSONArray("membergroup");
            if (groupJSONArray != null && groupJSONArray.length() > 0) {
                JSONObject currGroupObject = groupJSONArray.optJSONObject(0);
                if (currGroupObject != null && currGroupObject.has("id")) {
                    groupId = currGroupObject.optString("id");
                }
            }
        }
        return groupId;
    }

    private boolean checkForOperationSuccessAndPrepareServiceResult(Record operationRecord, Result resultObject) {
        if (operationRecord == null) {
            return false;
        }
        if (operationRecord.getParamByName("statusCode") != null) {
            if (!operationRecord.getParamByName("statusCode").getValue().equalsIgnoreCase("0")) {
                ErrorCodeEnum.ERR_20121.setErrorCode(resultObject);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean checkForOperationSuccessState(Record operationRecord) {
        if (operationRecord.getParamByName("statusCode") != null) {
            if (!operationRecord.getParamByName("statusCode").getValue().equalsIgnoreCase("0")) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({ "deprecation" })
    private boolean validateRequestInstanceAndInitialiseBean(DataControllerRequest requestInstance,
            Result processedResult, CustomerRequestBean customerRequestBeanInstance) throws IOException {
        if (requestInstance == null) {
            ErrorCodeEnum.ERR_20561.setErrorCode(processedResult);
            LOG.error("Data Controller Request Instance is null. Cannot Process Request");
            return false;

        }
        /*
         * Data From client is sent as MultiPart-Form-Data or Application/JSON. Below Code is to process the data and to
         * add it to the requestInstance. THE OPERATIONS USING THIS SERVICE CODE MUST BE OF TYPE PASS-THROUGH ONLY IN
         * ALL CASES
         */
        List<FormItem> formItems = null;
        try {
            formItems = MultipartPayloadHandler.handleMultipart(requestInstance);
        } catch (FileSizeLimitExceededException fslee) {
            ErrorCodeEnum.ERR_20563.setErrorCode(processedResult);
            return false;
        } catch (InvalidFileNameException ifne) {
            ErrorCodeEnum.ERR_20542.setErrorCode(processedResult);
            return false;
        }

        if (MultipartPayloadHandler.isMultipartRequest(requestInstance)) {
            LOG.error("Content-Type:MultiPart Request");
            if (formItems != null) {
                for (FormItem currFormItem : formItems) {
                    requestInstance.addRequestParam_(currFormItem.getParamName(), currFormItem.getParamValue());
                    LOG.debug("KEY:" + currFormItem.getParamName() + "-->VALUE:" + currFormItem.getParamValue());
                }
            }
        } else { // Content-Type -> application/JSON
            LOG.error("Content-Type:application/JSON");
            HttpServletRequest httpServletRequest = null;
            String requestJSON = null;
            if ((httpServletRequest = (HttpServletRequest) requestInstance.getOriginalRequest()) != null) {
                requestJSON = IOUtils.toString(httpServletRequest.getInputStream(), StandardCharsets.UTF_8);
            }
            if (StringUtils.isBlank(requestJSON)) {
                ErrorCodeEnum.ERR_20982.setErrorCode(processedResult);
                LOG.error("ERROR: Invalid Request. Request Payload has been found to be Empty.");
                return false;
            } else {
                JSONObject requestJSONObject = new JSONObject(requestJSON);
                for (String currKey : requestJSONObject.keySet()) {
                    Object currValObj = requestJSONObject.opt(currKey);
                    if (currValObj != null) {
                        LOG.debug("KEY:" + currKey + "-->VALUE:" + String.valueOf(currValObj));
                        requestInstance.addRequestParam_(currKey, String.valueOf(currValObj));
                    }
                }
            }
        }
        customerRequestBeanInstance.setFormItems(formItems);
        return true;
    }
}