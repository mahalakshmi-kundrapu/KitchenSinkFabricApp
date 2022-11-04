package com.kony.adminconsole.service.leadmanagement;

import java.util.HashMap;
import java.util.List;
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
import com.kony.adminconsole.dto.Lead;
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
 * Service to manage leads
 *
 * @author Aditya Mankal
 */
public class LeadManagementService implements JavaService2 {

    private static final String UPDATE_LEAD_METHOD_ID = "updateLead";
    private static final String CREATE_LEAD_METHOD_ID = "createLead";
    private static final String ASSIGN_LEADS_METHOD_ID = "assignLeads";
    private static final String CLOSE_LEAD_METHOD_ID = "closeLead";

    private static final String NEW_LEAD_STATUS_ID = StatusEnum.SID_NEW.name();

    private static final Logger LOG = Logger.getLogger(LeadManagementService.class);

    private static final int FIRST_NAME_MIN_CHARS = 3;
    private static final int FIRST_NAME_MAX_CHARS = 20;

    private static final int MIDDLE_NAME_MAX_CHARS = 20;

    private static final int LAST_NAME_MIN_CHARS = 3;
    private static final int LAST_NAME_MAX_CHARS = 20;

    private static final int NOTE_MAX_CHARS = 2000;

    private static final int LEAD_CLOSURE_REASON_MIN_LENGTH = 5;
    private static final int LEAD_CLOSURE_REASON_MAX_LENGHT = 50;

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) {

        try {

            LOG.debug("Method Id: " + methodID);
            // Fetch Logged In User Info
            String loggedInUser = StringUtils.EMPTY;
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            if (userDetailsBeanInstance != null) {
                loggedInUser = userDetailsBeanInstance.getUserId();
            }

            if (StringUtils.equalsAny(methodID, CREATE_LEAD_METHOD_ID, UPDATE_LEAD_METHOD_ID)) {

                // Read Inputs
                String leadId = StringUtils.trim(requestInstance.getParameter("leadId"));
                String statusId = StringUtils.trim(requestInstance.getParameter("statusId"));
                String salutation = StringUtils.trim(requestInstance.getParameter("saluation"));
                String firstName = StringUtils.trim(requestInstance.getParameter("firstName"));
                String middleName = StringUtils.trim(requestInstance.getParameter("middleName"));
                String lastName = StringUtils.trim(requestInstance.getParameter("lastName"));
                String email = StringUtils.trim(requestInstance.getParameter("email"));
                String countryCode = StringUtils.trim(requestInstance.getParameter("countryCode"));
                String phoneNumber = StringUtils.trim(requestInstance.getParameter("phoneNumber"));
                String extension = StringUtils.trim(requestInstance.getParameter("extension"));
                String productId = StringUtils.trim(requestInstance.getParameter("productId"));
                String note = StringUtils.trim(requestInstance.getParameter("note"));

                // Construct Lead Instance
                Lead lead = new Lead(leadId, salutation, firstName, middleName, lastName, statusId, email, countryCode,
                        phoneNumber, extension, productId, note);

                // Process request
                return processLead(lead, loggedInUser, requestInstance);
            }

            if (StringUtils.equals(methodID, CLOSE_LEAD_METHOD_ID)) {
                // Read Inputs
                String leadId = StringUtils.trim(requestInstance.getParameter("leadId"));
                String closureReason = StringUtils.trim(requestInstance.getParameter("closureReason"));

                // Process request
                return closeLead(leadId, closureReason, loggedInUser, requestInstance);
            }
            if (StringUtils.equals(methodID, ASSIGN_LEADS_METHOD_ID)) {
                // Read Inputs
                String csrId = StringUtils.trim(requestInstance.getParameter("csrId"));
                String leads = StringUtils.trim(requestInstance.getParameter("leads"));
                List<String> leadsList = CommonUtilities.getStringifiedArrayAsList(leads);

                // Process request
                return assignLeads(csrId, leadsList, loggedInUser, requestInstance);
            }
            return new Result();
        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.error("Exception in Fetching Leads Master Data. Exception:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    /**
     * Method to close a lead
     * 
     * @param leadId
     * @param closureReason
     * @param loggedInUserId
     * @param requestInstance
     * @return operation state
     * @throws ApplicationException
     */
    private Result closeLead(String leadId, String closureReason, String loggedInUserId,
            DataControllerRequest requestInstance) throws ApplicationException {

        // Validate Inputs
        if (StringUtils.isBlank(leadId)) {
            LOG.error("Missing mandatory Input" + leadId);
            throw new ApplicationException(ErrorCodeEnum.ERR_21515);
        }
        if (StringUtils.length(closureReason) < LEAD_CLOSURE_REASON_MIN_LENGTH
                || StringUtils.length(closureReason) > LEAD_CLOSURE_REASON_MAX_LENGHT) {
            LOG.error("Lead closure reason does not meet the validation criteria. Passed value:" + closureReason);
            throw new ApplicationException(ErrorCodeEnum.ERR_21553);
        }

        // Prepare Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("_leadId", leadId);
        inputMap.put("_leadClosureReason", closureReason);
        inputMap.put("_modifiedby", loggedInUserId);

        // Execute service
        String serviceResponse = Executor.invokeService(ServiceURLEnum.LEAD_ARCHIVE_PROC_SERVICE, inputMap, null,
                requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);

        // Check operation status
        serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            // Failed operation
            LOG.error("Failed CRUD Operation. Response:" + serviceResponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_21554);
        }

        // Audit success action
        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LEADMANAGEMENT, EventEnum.UPDATE,
                ActivityStatusEnum.SUCCESSFUL, "Archived Lead" + leadId);

        // Return success result
        Result result = new Result();
        result.addParam(new Param("leadId", leadId, FabricConstants.STRING));
        result.addParam(new Param("status", "success", FabricConstants.STRING));
        return result;
    }

    /**
     * Method to assign leads to a CSR
     * 
     * @param csrId
     * @param leads
     * @param loggedInUser
     * @param requestInstance
     * @return operation Result
     * @throws ApplicationException
     */
    private Result assignLeads(String csrId, List<String> leads, String loggedInUser,
            DataControllerRequest requestInstance) throws ApplicationException {

        Result result = new Result();

        // Validate Inputs
        if (StringUtils.isBlank(csrId)) {
            LOG.error("CSR ID is a mandatory input to assign leads");
            ErrorCodeEnum.ERR_21512.setErrorCode(result);
            return result;
        }

        if (leads != null && !leads.isEmpty()) {

            // Prepare Input Map
            String leadIds = String.join(", ", leads);
            Map<String, String> inputMap = new HashMap<>();
            inputMap.put("_leadIds", leadIds);
            inputMap.put("_csrID", csrId);
            inputMap.put("_modifiedby", loggedInUser);

            // Execute Service
            String serviceResponse = Executor.invokeService(ServiceURLEnum.LEAD_ASSIGN_PROC_SERVICE, inputMap, null,
                    requestInstance);

            // Check operation status
            JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
            if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                    || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                // Failed operation
                LOG.error("Failed CRUD Operation. Response:" + serviceResponse);
                throw new ApplicationException(ErrorCodeEnum.ERR_21513);
            }

            // Audit Action
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LEADMANAGEMENT, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL, "Assigned " + leads.size() + " lead(s) to CSR:" + csrId);

            // Add operation meta
            LOG.debug("Successful CRUD Operation");
            result.addParam(new Param("leads", String.join(",", leads), FabricConstants.STRING));
        }

        // Return success message
        result.addParam(new Param("csrId", csrId, FabricConstants.STRING));
        result.addParam(new Param("status", "success", FabricConstants.STRING));
        return result;
    }

    /**
     * Method to create/update a Lead
     * 
     * @param lead
     * @param loggedInUser
     * @param requestInstance
     * @return operation status
     * @throws ApplicationException
     */
    private Result processLead(Lead lead, String loggedInUser, DataControllerRequest requestInstance)
            throws ApplicationException {

        Result result = new Result();

        // Read Inputs
        String leadId = lead.getId();
        String salutation = lead.getSalutation();
        String firstName = lead.getFirstName();
        String middleName = lead.getMiddleName();
        String lastName = lead.getLastName();
        String emailAddress = lead.getEmail();
        String countryCode = lead.getCountryCode();
        String phoneNumber = lead.getPhoneNumber();
        String extension = lead.getExtension();
        String productId = lead.getProductId();
        String note = lead.getNote();
        String statusId = lead.getStatusId();
        boolean isCreateMode = StringUtils.isBlank(leadId);

        // Validate Data
        StringBuffer errorMessageBuffer = new StringBuffer();
        if (isCreateMode == true) {

            // Set Lead Id
            leadId = String.valueOf(CommonUtilities.getNumericId());
            lead.setId(leadId);

            // Set default Lead Status
            statusId = NEW_LEAD_STATUS_ID;
            lead.setStatusId(statusId);

            // Validate First Name
            if (StringUtils.length(firstName) < FIRST_NAME_MIN_CHARS
                    || StringUtils.length(firstName) > FIRST_NAME_MAX_CHARS) {
                // Invalid First Name
                errorMessageBuffer.append("First Name should have a minimum of " + FIRST_NAME_MIN_CHARS
                        + " characters and a maximum of " + FIRST_NAME_MAX_CHARS + " characters.");
            }

            // Validate MiddleName Name
            if (StringUtils.length(middleName) > MIDDLE_NAME_MAX_CHARS) {
                // Invalid Middle Name
                errorMessageBuffer
                        .append("Middle Name can have a maximum of " + MIDDLE_NAME_MAX_CHARS + " characters.");
            }

            // Validate Last Name
            if (StringUtils.length(lastName) < LAST_NAME_MIN_CHARS
                    || StringUtils.length(lastName) > LAST_NAME_MAX_CHARS) {
                // Invalid Last Name
                errorMessageBuffer.append("Last Name should have a minimum of " + LAST_NAME_MIN_CHARS
                        + " characters and a maximum of " + LAST_NAME_MAX_CHARS + " characters.");
            }

            // Validate Email Address
            if (!CommonUtilities.isValidEmailID(emailAddress)) {
                // Invalid Email Address
                errorMessageBuffer.append("Invalid Email Address");
            }

            // Validate Note
            if (StringUtils.length(note) > NOTE_MAX_CHARS) {
                // Invalid Note
                errorMessageBuffer.append("Note can have a maximum of " + NOTE_MAX_CHARS + " characters.");
            }

            if (StringUtils.isNotBlank(errorMessageBuffer.toString())) {
                // Invalid Data
                result.addParam(new Param("message", errorMessageBuffer.toString(), FabricConstants.STRING));
                ErrorCodeEnum.ERR_21506.setErrorCode(result);
                return result;// Return Error Result

            }
        }

        // Prepare Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("id",
                StringUtils.isBlank(lead.getId()) ? String.valueOf(CommonUtilities.getNumericId()) : lead.getId());

        if (isCreateMode == true) {
            String customerId = getCustomerIdWithEmailOrPhone(emailAddress, phoneNumber, requestInstance);
            boolean isCustomer = StringUtils.isNotBlank(customerId);
            inputMap.put("isCustomer", isCustomer ? "1" : "0");
            result.addParam(new Param("isCustomer", String.valueOf(isCustomer), FabricConstants.STRING));

            if (isCustomer) {
                // Add customer ID to result if the lead is an existing customer
                inputMap.put("customerId", customerId);
                result.addParam(new Param("customerId", customerId, FabricConstants.STRING));
            }

        }
        if (StringUtils.isNotBlank(firstName)) {
            inputMap.put("firstName", firstName);
        }
        if (StringUtils.isNotBlank(middleName)) {
            inputMap.put("middleName", middleName);
        }
        if (StringUtils.isNotBlank(lastName)) {
            inputMap.put("lastName", lastName);
        }
        if (StringUtils.isNotBlank(salutation)) {
            inputMap.put("salutation", salutation);
        }
        if (StringUtils.isNotBlank(productId)) {
            inputMap.put("product_id", productId);
        }
        if (StringUtils.isNotBlank(statusId)) {
            inputMap.put("status_id", statusId);
        }
        if (StringUtils.isNotBlank(countryCode)) {
            inputMap.put("countryCode", countryCode);
        }
        if (StringUtils.isNotBlank(phoneNumber)) {
            inputMap.put("phoneNumber", phoneNumber);
        }
        if (StringUtils.isNotBlank(extension)) {
            inputMap.put("extension", extension);
        }
        if (StringUtils.isNotBlank(emailAddress)) {
            inputMap.put("email", emailAddress);
        }

        // Execute Service
        String serviceResponse;
        EventEnum eventEnum = null;
        ErrorCodeEnum errorCodeEnum = null;
        if (isCreateMode) {
            errorCodeEnum = ErrorCodeEnum.ERR_21507;
            eventEnum = EventEnum.CREATE;
            inputMap.put("createdby", loggedInUser);
            inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
            serviceResponse = Executor.invokeService(ServiceURLEnum.LEAD_CREATE, inputMap, null, requestInstance);
        } else {
            errorCodeEnum = ErrorCodeEnum.ERR_21508;
            eventEnum = EventEnum.UPDATE;
            inputMap.put("modifiedby", loggedInUser);
            inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            serviceResponse = Executor.invokeService(ServiceURLEnum.LEAD_UPDATE, inputMap, null, requestInstance);
        }

        // Check operation status
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            // Failed operation
            LOG.error("Failed CRUD Operation. Response:" + serviceResponse);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LEADMANAGEMENT, eventEnum,
                    ActivityStatusEnum.FAILED, "Lead updation/creation failed. Lead id:" + leadId);
            throw new ApplicationException(errorCodeEnum);
        }

        // Process Lead Note
        if (StringUtils.isNotBlank(note)) {
            inputMap.clear();
            inputMap.put("id", CommonUtilities.getNewId().toString());
            inputMap.put("lead_Id", leadId);
            inputMap.put("note", note);
            inputMap.put("createdby", loggedInUser);
            inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
            serviceResponse = Executor.invokeService(ServiceURLEnum.LEADNOTE_CREATE, inputMap, null, requestInstance);

            serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
            if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                    || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                // Failed operation
                LOG.error("Failed CRUD Operation. Response:" + serviceResponse);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LEADMANAGEMENT, eventEnum,
                        ActivityStatusEnum.FAILED, "Lead note creation failed. Lead id:" + leadId);
                throw new ApplicationException(ErrorCodeEnum.ERR_21514);
            }
        }

        // Return success message
        LOG.debug("Successful CRUD Operation");
        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LEADMANAGEMENT, eventEnum,
                ActivityStatusEnum.SUCCESSFUL, "Lead updated/created successfully. Lead id:" + leadId);
        result.addParam(new Param("status", "success", FabricConstants.STRING));
        result.addParam(new Param("leadId", leadId, FabricConstants.STRING));
        return result;

    }

    /**
     * Method to determine if there is a customer with the given communication details
     * 
     * @param emailAddress
     * @param phoneNumber
     * @param requestInstance
     * @return
     * @throws ApplicationException
     */
    private String getCustomerIdWithEmailOrPhone(String emailAddress, String phoneNumber,
            DataControllerRequest requestInstance) throws ApplicationException {

        // Prepare Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put(ODataQueryConstants.SELECT, "customer_id");
        inputMap.put(ODataQueryConstants.FILTER, "customercommunication_Value eq '" + emailAddress
                + "' or customercommunication_Value eq '" + phoneNumber + "'");

        // Read Customer Communication Records
        String serviceResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_COMMUNICATION_VIEW_READ, inputMap, null,
                requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            // Failed operation
            LOG.error("Failed CRUD Operation. Response:" + serviceResponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_20879);
        }

        // Traverse customer communication records
        JSONArray customerCommunicationArray = serviceResponseJSON.optJSONArray("customer_communication_view");

        if (customerCommunicationArray != null) {
            if (customerCommunicationArray.length() == 0) {
                // No customer found with the given communication details
                return null;
            }

            else {
                // One or more than one record
                String currentCustomerIdValue = customerCommunicationArray.optJSONObject(0).optString("customer_id");

                if (customerCommunicationArray.length() == 1) {
                    // Single communication record found
                    return currentCustomerIdValue;
                }

                // If more than one matching communication records are found, verify if the
                // customer ID is the same in all records.
                JSONObject currJSON;
                for (int index = 1; index < customerCommunicationArray.length(); index++) {
                    currJSON = customerCommunicationArray.optJSONObject(index);
                    if (!StringUtils.equals(currentCustomerIdValue, currJSON.optString("customer_id"))) {
                        return null;
                    }
                }
                return currentCustomerIdValue;
            }
        }
        return null;
    }

}
