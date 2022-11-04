package com.kony.adminconsole.service.customerservice;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.kony.adminconsole.utilities.StatusEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to manage the Customer Services and their corresponding Communication Records
 *
 * @author Aditya Mankal
 * 
 */
public class CustomerServiceAndCustomerServiceCommunicationManageService implements JavaService2 {

    private static final String CREATE_METHOD_ID = "createCustomerServiceAndCommunicationRecords";
    private static final String DELETE_METHOD_ID = "deleteCustomerServiceAndCommunicationRecords";
    private static final String UPDATE_METHOD_ID = "editCustomerServiceAndCommunicationRecords";

    private static final int EXTENSION_MIN_CHARS = 3;
    private static final int EXTENSION_MAX_CHARS = 5;
    private static final int SERVICE_NAME_MIN_CHARS = 5;

    private static final String COMMUNICATION_TYPE_PHONE = "COMM_TYPE_PHONE";
    private static final String COMMUNICATION_TYPE_EMAIL = "COMM_TYPE_EMAIL";

    private static final Logger LOG = Logger
            .getLogger(CustomerServiceAndCustomerServiceCommunicationManageService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        try {
            Result processedResult = new Result();
            LOG.debug("Method ID" + methodID);

            // Fetch Logged-in User Info
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            String userId = userDetailsBeanInstance.getUserId();

            // Read Service Inputs
            String customerServiceId = requestInstance.getParameter("Service_id");
            String customerServiceDetails = requestInstance.getParameter("Service_Details");
            String communicationRecords = requestInstance.getParameter("Communication_Records");
            JSONObject customerServiceDetailsJSONObject = CommonUtilities.getStringAsJSONObject(customerServiceDetails);
            JSONArray communicationRecordsJSONArray = CommonUtilities.getStringAsJSONArray(communicationRecords);

            if (StringUtils.equals(methodID, CREATE_METHOD_ID)) {
                // Create Service and Communication Records
                customerServiceId = CommonUtilities.getNewId().toString();
                processCustomerService(customerServiceDetailsJSONObject, customerServiceId, true, userId,
                        requestInstance);

                if (communicationRecordsJSONArray != null) {
                    // Create the Communication Records
                    processServiceCommunicationRecords(communicationRecordsJSONArray, customerServiceId, userId,
                            requestInstance);
                }
            }

            else if (StringUtils.equals(methodID, DELETE_METHOD_ID)) {
                // Delete Service and Communication Records
                return deleteCustomerServiceAndCommunicationRecords(userId, customerServiceId, requestInstance);
            }

            else if (StringUtils.equals(methodID, UPDATE_METHOD_ID)) {
                // Update Service
                processCustomerService(customerServiceDetailsJSONObject, customerServiceId, false, userId,
                        requestInstance);
                if (communicationRecordsJSONArray != null) {
                    // Update Communication Records
                    processServiceCommunicationRecords(communicationRecordsJSONArray, customerServiceId, userId,
                            requestInstance);
                }
            }

            return processedResult;
        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    private void processCustomerService(JSONObject serviceInfo, String serviceId, boolean isCreateServiceRequest,
            String userId, DataControllerRequest requestInstance) throws ApplicationException {

        LOG.debug("isCreateServiceRequest:" + isCreateServiceRequest);
        EventEnum eventEnum = EventEnum.UPDATE;
        if (isCreateServiceRequest == true) {
            eventEnum = EventEnum.CREATE;
        }

        if (isCreateServiceRequest == false) {
            // Missing mandatory Input
            if (StringUtils.isBlank(serviceId)) {
                LOG.error("Service ID is a mandatory input in case of Delete OR Update Service request");
                throw new ApplicationException(ErrorCodeEnum.ERR_20566);
            }
        }

        boolean isValidInput = true;
        StringBuffer errorMessageBuffer = new StringBuffer();
        String operationResponse, customerServiceDescription, customerServiceName, customerServiceStatus;
        Map<String, String> inputMap = new HashMap<String, String>();

        // Validate Inputs
        if (serviceInfo != null) {
            if (serviceInfo.has("Name")) {
                customerServiceName = serviceInfo.getString("Name");
                if (customerServiceName.length() < SERVICE_NAME_MIN_CHARS) {
                    isValidInput = false;
                    errorMessageBuffer.append(
                            "Customer Service Name should have atleast " + SERVICE_NAME_MIN_CHARS + " characters.\n");
                } else
                    inputMap.put("Name", customerServiceName);

                // Checking duplicate service name 
                Result result = new Result();
                checkDuplicateServiceName(customerServiceName, serviceId, requestInstance, result);
                if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                	throw new ApplicationException(ErrorCodeEnum.ERR_20337);
                }
            }

            if (serviceInfo.has("Description")) {
                customerServiceDescription = serviceInfo.getString("Description");
                // Add validation when there is a provision from UI to enter Customer Service
                // Description
                inputMap.put("Description", customerServiceDescription);
            }

            if (serviceInfo.has("Status_id")) {
                customerServiceStatus = serviceInfo.getString("Status_id");
                inputMap.put("Status_id", customerServiceStatus);
            } else {
                if (isCreateServiceRequest)
                    inputMap.put("Status_id", StatusEnum.SID_ACTIVE.name());
            }
        } else {
            isValidInput = false;
            errorMessageBuffer.append("Input Payload is Empty");
        }
        LOG.debug("is Valid Input?:" + isValidInput);
        if (isValidInput == false) {
            // Invalid Input. Return Error Message
            LOG.error("Inavlid Input. Info" + errorMessageBuffer.toString());
            throw new ApplicationException(ErrorCodeEnum.ERR_20380);
        }

        // Create/Update Service
        inputMap.put("id", serviceId);
        if (isCreateServiceRequest) {
            LOG.debug("Creating Service");
            operationResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERSERVICE_CREATE, inputMap, null,
                    requestInstance);
        } else {
            LOG.debug("Updating Service");
            operationResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERSERVICE_UPDATE, inputMap, null,
                    requestInstance);
        }
        JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
        if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            // Failed Service Operation
            LOG.error("Failed Operation. Response" + operationResponse);
            ErrorCodeEnum errorCode = isCreateServiceRequest ? ErrorCodeEnum.ERR_20381 : ErrorCodeEnum.ERR_20382;
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERCARE, eventEnum,
                    ActivityStatusEnum.FAILED, "Failed to process customer service");
            throw new ApplicationException(errorCode);
        }

        LOG.debug("Service processed");
        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERCARE, eventEnum,
                ActivityStatusEnum.SUCCESSFUL, "Customer Service processed Succesfully");
    }

    private void processServiceCommunicationRecords(JSONArray comminicationInfo, String serviceId, String userId,
            DataControllerRequest requestInstance) throws ApplicationException {

        Map<String, String> inputMap = new HashMap<String, String>();

        StringBuffer errorMessageBuffer;
        JSONObject currCommunicationRecordJSONObject;
        boolean isCreateRequest = true, isValidInput = true;
        String currCommunicationId, currValue, currCommType, currStatusID, currExtnsionValue, currDescriptionValue,
                currPriorityValue, currDeleteFlagValue;

        // Traverse Communication Records
        for (int indexVar = 0; indexVar < comminicationInfo.length(); indexVar++) {

            // Reset values
            currValue = StringUtils.EMPTY;
            currStatusID = StringUtils.EMPTY;
            currCommType = StringUtils.EMPTY;
            currPriorityValue = StringUtils.EMPTY;
            currExtnsionValue = StringUtils.EMPTY;
            currCommunicationId = StringUtils.EMPTY;
            currDeleteFlagValue = StringUtils.EMPTY;
            currDescriptionValue = StringUtils.EMPTY;
            errorMessageBuffer = new StringBuffer();
            isCreateRequest = true;
            isValidInput = true;

            // Reset Input Map
            inputMap.clear();
            inputMap.put("Service_id", serviceId);

            currCommunicationRecordJSONObject = comminicationInfo.getJSONObject(indexVar);
            if (StringUtils.isNotBlank(currCommunicationRecordJSONObject.optString("communicationID"))) {
                isCreateRequest = false;
                currCommunicationId = currCommunicationRecordJSONObject.optString("communicationID");
            } else {
                currCommunicationId = CommonUtilities.getNewId().toString();
            }
            inputMap.put("id", currCommunicationId);

            // Check if the request specifies the current Communication Record to be deleted
            if (isCreateRequest == false && currCommunicationRecordJSONObject.has("DeleteFlag")) {
                currDeleteFlagValue = currCommunicationRecordJSONObject.getString("DeleteFlag");
                if (StringUtils.equalsIgnoreCase(currDeleteFlagValue, String.valueOf(true))
                        || StringUtils.equalsIgnoreCase(currDeleteFlagValue, String.valueOf(1))) {
                    // Delete Communication record
                    LOG.debug("Deleting Communication Record with ID" + currCommunicationId);
                    deleteCommunicationRecord(currCommunicationId, requestInstance);
                    continue;
                }
            }

            // Validate Inputs and Prepare Input Map
            LOG.debug("Validating Inputs and Preparing Input Map");
            if (currCommunicationRecordJSONObject.has("Priority")) {
                currPriorityValue = currCommunicationRecordJSONObject.getString("Priority");
            } else {
                currPriorityValue = String.valueOf(0);
            }
            inputMap.put("Priority", currPriorityValue);

            if (currCommunicationRecordJSONObject.has("Type")) {
                currCommType = currCommunicationRecordJSONObject.getString("Type");
                inputMap.put("Type_id", currCommType);
            }
            if (currCommunicationRecordJSONObject.has("Extension")) {
                currExtnsionValue = currCommunicationRecordJSONObject.getString("Extension");
                inputMap.put("Extension", currExtnsionValue);
            }
            if (currCommunicationRecordJSONObject.has("Status_id")) {
                currStatusID = currCommunicationRecordJSONObject.getString("Status_id");
                inputMap.put("Status_id", currStatusID);
            }
            if (currCommunicationRecordJSONObject.has("Value")) {
                currValue = currCommunicationRecordJSONObject.getString("Value");
                if (StringUtils.equalsIgnoreCase(currCommType, COMMUNICATION_TYPE_PHONE)) {
                    if (currValue.length() < 10) {
                        errorMessageBuffer.append("Phone number should have atleast 10 characters.");
                        isValidInput = false;
                    }
                    if (currExtnsionValue.length() < EXTENSION_MIN_CHARS
                            || currExtnsionValue.length() > EXTENSION_MAX_CHARS) {
                        errorMessageBuffer.append("Extension should have a minimum of " + EXTENSION_MIN_CHARS
                                + " characters and a maximum of " + EXTENSION_MAX_CHARS + " characters");
                        isValidInput = false;
                    }
                } else if (StringUtils.equalsIgnoreCase(currCommType, COMMUNICATION_TYPE_EMAIL)) {
                    if (!CommonUtilities.isValidEmailID(currValue)) {
                        errorMessageBuffer.append("Invalid Email ID.");
                        isValidInput = false;
                    }
                }
                Result result = new Result();
                checkDuplicateCommunication(currCommunicationId, serviceId, currCommType, currValue, requestInstance,
                        result);
                if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                    throw new ApplicationException(ErrorCodeEnum.ERR_20338);
                }
                inputMap.put("Value", currValue);
            }
            if (currCommunicationRecordJSONObject.has("Description")) {
                currDescriptionValue = currCommunicationRecordJSONObject.getString("Description");
                inputMap.put("Description", currDescriptionValue);
            }
            LOG.debug("Is Valid Input?:" + isValidInput);

            if (isValidInput) {
                String serviceResponse;
                if (isCreateRequest) {
                    inputMap.put("createdby", userId);
                    inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
                    LOG.debug("Creating Communication Record. Communication Id:" + currCommunicationId);
                    serviceResponse = Executor.invokeService(ServiceURLEnum.SERVICECOMMUNICATION_CREATE, inputMap, null,
                            requestInstance);
                } else {
                    inputMap.put("modifiedby", userId);
                    inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
                    LOG.debug("Updating Communication Record. Communication Id:" + currCommunicationId);
                    serviceResponse = Executor.invokeService(ServiceURLEnum.SERVICECOMMUNICATION_UPDATE, inputMap, null,
                            requestInstance);
                }
                JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);

                if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                        || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    // Failed Service Operation
                    LOG.debug("Failed Operation. Response" + serviceResponse);
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERCARE, EventEnum.CREATE,
                            ActivityStatusEnum.FAILED, "Failed to process customer service");
                    throw new ApplicationException(ErrorCodeEnum.ERR_20345);
                }
                LOG.debug("Processed Communication Record with Id" + currCommunicationId);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERCARE, EventEnum.CREATE,
                        ActivityStatusEnum.SUCCESSFUL, "Customer Service Processed Succesfully");

            } else {
                // Invalid Input
                LOG.error("Invalid Service Communication Info. Skipping Create/Update for this specfic record. Info"
                        + errorMessageBuffer.toString());
            }
        }

    }

    private void checkDuplicateCommunication(String currCommunicationId, String serviceId, String currCommType,
            String currValue, DataControllerRequest requestInstance, Result result) {

        try {

            Map<String, String> inputBodyMap = new HashMap<String, String>();

            inputBodyMap.put(ODataQueryConstants.FILTER, "Service_id eq '" + serviceId + "' and Type_id eq '"
                    + currCommType + "' and Value eq '" + currValue + "'");
            inputBodyMap.put(ODataQueryConstants.SELECT, "id");

            String readServiceCommunicationResponse = Executor.invokeService(ServiceURLEnum.SERVICECOMMUNICATION_READ,
                    inputBodyMap, null, requestInstance);
            JSONObject readServiceCommunicationResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readServiceCommunicationResponse);
            if ((readServiceCommunicationResponseJSON != null
                    && readServiceCommunicationResponseJSON.has(FabricConstants.OPSTATUS)
                    && readServiceCommunicationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readServiceCommunicationResponseJSON.has("servicecommunication"))) {
                JSONArray readServiceCommunicationJSONArray = readServiceCommunicationResponseJSON
                        .optJSONArray("servicecommunication");
                if (!(readServiceCommunicationJSONArray == null || readServiceCommunicationJSONArray.length() < 1)) {
                    JSONObject currServiceCommunicationTextRecord = readServiceCommunicationJSONArray.getJSONObject(0);
                    String serviceCommunicationId = currServiceCommunicationTextRecord.getString("id");
                    if (!StringUtils.equals(currCommunicationId, serviceCommunicationId)) {
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        ErrorCodeEnum.ERR_20338.setErrorCode(result);
                        LOG.error("Duplicate entry for service communication");
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERCARE, EventEnum.UPDATE,
                                ActivityStatusEnum.FAILED, "Duplicate entry for service communication");
                    }
                }
            }
        } catch (Exception e) {
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20339.setErrorCode(result);
            LOG.error("Failed to fetch service communication");
        }
    }

    private void checkDuplicateServiceName(String serviceName, String serviceId, DataControllerRequest requestInstance, Result result) {

        try {

            Map<String, String> inputBodyMap = new HashMap<String, String>();

            inputBodyMap.put(ODataQueryConstants.FILTER, "Name eq '" + serviceName + "'");
            inputBodyMap.put(ODataQueryConstants.SELECT, "id");

            String readCustomerServiceResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERSERVICE_READ,
                    inputBodyMap, null, requestInstance);
            JSONObject readCustomerServiceResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readCustomerServiceResponse);
            if ((readCustomerServiceResponseJSON != null
                    && readCustomerServiceResponseJSON.has(FabricConstants.OPSTATUS)
                    && readCustomerServiceResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readCustomerServiceResponseJSON.has("customerservice"))) {
                JSONArray readCustomerServiceJSONArray = readCustomerServiceResponseJSON
                        .optJSONArray("customerservice");
                if (!(readCustomerServiceJSONArray == null || readCustomerServiceJSONArray.length() < 1)) {
                	JSONObject readCustomerServiceTextRecord = readCustomerServiceJSONArray.getJSONObject(0);
                    String currServiceId = readCustomerServiceTextRecord.getString("id");
                    if (!StringUtils.equals(serviceId, currServiceId)) {
                    	result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        ErrorCodeEnum.ERR_20337.setErrorCode(result);
                        LOG.error("Duplicate entry for service name");
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERCARE, EventEnum.UPDATE,
                                ActivityStatusEnum.FAILED, "Duplicate entry for service name");
                    }
                    
                }
            }
        } catch (Exception e) {
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20339.setErrorCode(result);
            LOG.error("Failed to fetch service communication");
        }
    }

    private void deleteCommunicationRecord(String currCommunicationID, DataControllerRequest requestInstance)
            throws ApplicationException {
        // Delete the Communication Record
        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("id", currCommunicationID);
        String operationResponse = Executor.invokeService(ServiceURLEnum.SERVICECOMMUNICATION_DELETE, inputMap, null,
                requestInstance);
        JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
        if (operationResponseJSON == null || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            // Failed Service Operation
            LOG.error("Failed Operation. Response" + operationResponse);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERCARE, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "Failed to process customer service");
            throw new ApplicationException(ErrorCodeEnum.ERR_20340);
        }
        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERCARE, EventEnum.DELETE,
                ActivityStatusEnum.SUCCESSFUL, "Customer service processed succesfully");

    }

    private Result deleteCustomerServiceAndCommunicationRecords(String userId, String serviceId,
            DataControllerRequest requestInstance) throws ApplicationException {

        if (StringUtils.isBlank(serviceId)) {
            // Missing Mandatory Input
            throw new ApplicationException(ErrorCodeEnum.ERR_20566);
        }

        Result processedResult = new Result();
        Map<String, String> inputMap = new HashMap<String, String>();

        String operationResponse;
        JSONObject operationResponseJSON;

        // Fetch the Communication Ids of the Service
        inputMap.put(ODataQueryConstants.SELECT, "id");
        inputMap.put(ODataQueryConstants.FILTER, "Service_id eq '" + serviceId + "'");
        operationResponse = Executor.invokeService(ServiceURLEnum.SERVICECOMMUNICATION_READ, inputMap, null,
                requestInstance);
        operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
        if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            // Failed Service Operation
            LOG.error("Failed Operation. Response" + operationResponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_20339);
        }

        // Deleting Service Communication Records
        JSONArray serviceCommunicationRecordsJSONArray = operationResponseJSON.getJSONArray("servicecommunication");
        for (int indexVar = 0; indexVar < serviceCommunicationRecordsJSONArray.length(); indexVar++) {
            inputMap.clear();
            JSONObject currRecordJSONObject = serviceCommunicationRecordsJSONArray.getJSONObject(indexVar);
            if (StringUtils.isBlank(currRecordJSONObject.optString("id"))) {
                continue;
            }
            inputMap.put("id", currRecordJSONObject.optString("id"));
            operationResponse = Executor.invokeService(ServiceURLEnum.SERVICECOMMUNICATION_DELETE, inputMap, null,
                    requestInstance);
            operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
            if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                    || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                // Failed Service Operation
                LOG.error("Failed Operation. Response" + operationResponse);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERCARE, EventEnum.DELETE,
                        ActivityStatusEnum.FAILED, "Failed to process customer service");
                throw new ApplicationException(ErrorCodeEnum.ERR_20340);
            }
        }

        // Delete the Service
        inputMap.clear();
        inputMap.put("id", serviceId);
        operationResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERSERVICE_DELETE, inputMap, null,
                requestInstance);
        operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
        if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            // Failed Service Operation
            LOG.error("Failed Operation. Response" + operationResponse);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERCARE, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "Failed to process customer service");
            throw new ApplicationException(ErrorCodeEnum.ERR_20343);
        }
        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERCARE, EventEnum.DELETE,
                ActivityStatusEnum.SUCCESSFUL, "Customer service processed Succesfully");
        return processedResult;
    }

}