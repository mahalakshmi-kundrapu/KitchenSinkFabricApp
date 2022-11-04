package com.kony.adminconsole.service.customermanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.dto.MemberBean;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.CustomerHandler;
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

/**
 * @author Alahari Prudhvi Akhil (KH2346)
 *
 */
public class MemberCreate implements JavaService2 {

    private static final String INPUT_USERNAME = "Username";
    private static final String INPUT_FIRSTNAME = "FirstName";
    private static final String INPUT_LASTNAME = "LastName";
    private static final String INPUT_EMAIL = "PrimaryEmail";
    private static final String INPUT_PHONE = "PrimaryContactNumber";
    private static final String INPUT_SALUTATION = "Salutation";
    private static final String INPUT_GENDER = "Gender";
    private static final String INPUT_DATEOFBIRTH = "DateOfBirth";
    private static final String INPUT_SSN = "Ssn";
    private static final String INPUT_MARITALSTATUS_ID = "MaritalStatus_id";
    private static final String INPUT_SPOUSENAME = "SpouseName";
    private static final String INPUT_EMPLOYEMENTSTATUS_ID = "EmployementStatus_id";

    protected static final String COMMUNICATION_TYPE_EMAIL = "COMM_TYPE_EMAIL";
    protected static final String COMMUNICATION_TYPE_PHONE = "COMM_TYPE_PHONE";

    private static final String INPUT_COUNTRY_CODE = "CountryCode";

    // Default values
    public static final String DEFAULT_GROUP = "DEFAULT_GROUP";
    public static final String DEFAULT_EUROPE_GROUP = "DEFAULT_EUROPE_GROUP";
    public static final String DEFAULT_MARITAL_STATUS = "SID_SINGLE";
    public static final String DEFAULT_EMPLOYEMENT_STATUS = "SID_EMPLOYED";
    public static final String DEFAULT_CUSTOMER_STATUS = "SID_CUS_ACTIVE";
    public static final String DEFAULT_PREFERREDCONTACTMETHOD = "Call, Email";
    public static final String DEFAULT_PREFERREDCONTACTTIME = "Morning, Afternoon";

    public static final String EUROPE_COUNTRY_CODE = "COUNTRY_ID5";
    private static final Logger LOG = Logger.getLogger(MemberCreate.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse dataControllerResponse) throws Exception {
        MemberBean memberBeanInstance = new MemberBean();
        CustomerHandler customerHandler = new CustomerHandler();
        String customerId = customerHandler.getCustomerId(memberBeanInstance.getUsername(), requestInstance);
        try {
            Result processedResult = new Result();
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);

            Result initResult = initMemberBean(requestInstance, memberBeanInstance);
            if (initResult != null)
                return initResult;

            // Check if the user exists and update customer id accordingly
            checkUserExistence(authToken, requestInstance, memberBeanInstance);

            createDBPCustomer(authToken, requestInstance, memberBeanInstance);

            if (!memberBeanInstance.getIsCreationSuccessful()) {
                processedResult.addParam(new Param("Status", "Failed", FabricConstants.STRING));
                processedResult.addParam(
                        new Param("FailureReason", memberBeanInstance.getFailureReason(), FabricConstants.STRING));
                ErrorCodeEnum.ERR_20877.setErrorCode(processedResult);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED, "Member creation failed. Customer id: " + customerId);
                return processedResult;
            }
            processedResult.addParam(new Param("Status", "Successful", FabricConstants.STRING));
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.CREATE,
                    ActivityStatusEnum.SUCCESSFUL, "Member Created Successfully. Customer id: " + customerId);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "Member Creation failed. Customer id: " + customerId);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    private void checkUserExistence(String authToken, DataControllerRequest requestInstance,
            MemberBean memberBeanInstance) {

        CustomerHandler customerHandler = new CustomerHandler();
        String customerId = customerHandler.getCustomerId(memberBeanInstance.getUsername(), requestInstance);
        if (StringUtils.isNotBlank(customerId)) {
            memberBeanInstance.setMemberId(customerId);
            memberBeanInstance.setIsCreateMemberRquired(false);
        }

    }

    private Result initMemberBean(DataControllerRequest requestInstance, MemberBean memberBeanInstance) {

        Result result = new Result();
        if (StringUtils.isBlank(requestInstance.getParameter(INPUT_USERNAME))) {
            ErrorCodeEnum.ERR_20868.setErrorCode(result);
            result.addParam(new Param("Status", "Failure", FabricConstants.STRING));
            return result;
        }

        memberBeanInstance.setMemberId(String.valueOf(CommonUtilities.getNumericId()));
        memberBeanInstance.setUsername(requestInstance.getParameter(INPUT_USERNAME));
        memberBeanInstance.setFirstName(requestInstance.getParameter(INPUT_FIRSTNAME));
        memberBeanInstance.setMiddleName("");
        memberBeanInstance.setLastName(requestInstance.getParameter(INPUT_LASTNAME));
        memberBeanInstance.setEmailId(requestInstance.getParameter(INPUT_EMAIL));
        memberBeanInstance.setContactNumber(requestInstance.getParameter(INPUT_PHONE));
        memberBeanInstance.setGender(requestInstance.getParameter(INPUT_GENDER));
        memberBeanInstance.setSpouseName(requestInstance.getParameter(INPUT_SPOUSENAME));
        memberBeanInstance.setDateOfBirth(requestInstance.getParameter(INPUT_DATEOFBIRTH));
        memberBeanInstance.setSalutation(requestInstance.getParameter(INPUT_SALUTATION));
        memberBeanInstance.setSsn(requestInstance.getParameter(INPUT_SSN));
        if (StringUtils.isNotBlank(requestInstance.getParameter(INPUT_MARITALSTATUS_ID)))
            memberBeanInstance.setMaritalStatus_id(requestInstance.getParameter(INPUT_MARITALSTATUS_ID));
        if (StringUtils.isNotBlank(requestInstance.getParameter(INPUT_EMPLOYEMENTSTATUS_ID)))
            memberBeanInstance.setEmployementStatus_id(requestInstance.getParameter(INPUT_EMPLOYEMENTSTATUS_ID));
        memberBeanInstance.setCurrentTimestamp(CommonUtilities.getISOFormattedLocalTimestamp());
        return null;
    }

    private void createDBPCustomer(String authToken, DataControllerRequest requestInstance,
            MemberBean memberBeanInstance) {

        // Create a customer
        if (memberBeanInstance.getIsCreateMemberRquired()) {
            Map<String, String> postParamMapCreateCustomer = new HashMap<>();
            postParamMapCreateCustomer.put("id", memberBeanInstance.getMemberId());
            postParamMapCreateCustomer.put("FirstName", memberBeanInstance.getFirstName());
            postParamMapCreateCustomer.put("MiddleName", memberBeanInstance.getMiddleName());
            postParamMapCreateCustomer.put("LastName", memberBeanInstance.getLastName());
            postParamMapCreateCustomer.put("UserName", memberBeanInstance.getUsername());
            postParamMapCreateCustomer.put("Status_id", memberBeanInstance.getStatus_id());
            postParamMapCreateCustomer.put("Salutation", memberBeanInstance.getSalutation());
            postParamMapCreateCustomer.put("Gender", memberBeanInstance.getGender());
            postParamMapCreateCustomer.put("DateOfBirth", memberBeanInstance.getDateOfBirth());
            postParamMapCreateCustomer.put("Ssn", memberBeanInstance.getSsn());
            postParamMapCreateCustomer.put("PreferredContactMethod", memberBeanInstance.getPreferredContactMethod());
            postParamMapCreateCustomer.put("PreferredContactTime", memberBeanInstance.getPreferredContactTime());
            postParamMapCreateCustomer.put("MaritalStatus_id", memberBeanInstance.getMaritalStatus_id());
            postParamMapCreateCustomer.put("SpouseName", memberBeanInstance.getSpouseName());
            postParamMapCreateCustomer.put("EmployementStatus_id", memberBeanInstance.getEmployementStatus_id());
            postParamMapCreateCustomer.put("IsAssistConsented",
                    String.valueOf(memberBeanInstance.getIsAssistConsented()));
            postParamMapCreateCustomer.put("createdby", memberBeanInstance.getUsername());
            postParamMapCreateCustomer.put("modifiedby", memberBeanInstance.getUsername());
            postParamMapCreateCustomer.put("createdts", memberBeanInstance.getCurrentTimestamp());
            postParamMapCreateCustomer.put("lastmodifiedts", memberBeanInstance.getCurrentTimestamp());
            postParamMapCreateCustomer.put("synctimestamp", memberBeanInstance.getCurrentTimestamp());
            JSONObject createCustomerEndpointResponse = CommonUtilities.getStringAsJSONObject(Executor
                    .invokeService(ServiceURLEnum.CUSTOMER_CREATE, postParamMapCreateCustomer, null, requestInstance));
            if (createCustomerEndpointResponse == null || !checkResponseOpstatus(createCustomerEndpointResponse)) {
                memberBeanInstance.setIsCreationSuccessful(false);
                memberBeanInstance.setFailureReason(String.valueOf(createCustomerEndpointResponse));
                return;
            }

            if (StringUtils.isNotBlank(memberBeanInstance.getEmailId())) {
                // Create a customer email
                String EmailUUID = String.valueOf(CommonUtilities.getNewId());
                Map<String, String> postParamMapEmail = new HashMap<>();
                postParamMapEmail.put("id", EmailUUID);
                postParamMapEmail.put("Type_id", COMMUNICATION_TYPE_EMAIL);
                postParamMapEmail.put("Customer_id", memberBeanInstance.getMemberId());
                postParamMapEmail.put("isPrimary", "1");
                postParamMapEmail.put("Value", memberBeanInstance.getEmailId());
                postParamMapEmail.put("Extension", "Personal");
                postParamMapEmail.put("createdts", memberBeanInstance.getCurrentTimestamp());
                postParamMapEmail.put("lastmodifiedts", memberBeanInstance.getCurrentTimestamp());
                postParamMapEmail.put("synctimestamp", memberBeanInstance.getCurrentTimestamp());
                JSONObject createEmailEndpointResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
                        ServiceURLEnum.CUSTOMERCOMMUNICATION_CREATE, postParamMapEmail, null, requestInstance));
                if (createEmailEndpointResponse == null || !checkResponseOpstatus(createEmailEndpointResponse)) {
                    memberBeanInstance.setIsCreationSuccessful(false);
                    memberBeanInstance.setFailureReason(String.valueOf(createEmailEndpointResponse));
                    return;
                }
            }

            if (StringUtils.isNotBlank(memberBeanInstance.getContactNumber())) {
                // Create a customer phone number
                String PhoneUUID = String.valueOf(CommonUtilities.getNewId());
                Map<String, String> postParamMapPhone = new HashMap<>();
                postParamMapPhone.put("id", PhoneUUID);
                postParamMapPhone.put("Type_id", COMMUNICATION_TYPE_PHONE);
                postParamMapPhone.put("Customer_id", memberBeanInstance.getMemberId());
                postParamMapPhone.put("isPrimary", "1");
                postParamMapPhone.put("Extension", "Personal");
                postParamMapPhone.put("Value", memberBeanInstance.getContactNumber());
                postParamMapPhone.put("createdts", memberBeanInstance.getCurrentTimestamp());
                postParamMapPhone.put("lastmodifiedts", memberBeanInstance.getCurrentTimestamp());
                postParamMapPhone.put("synctimestamp", memberBeanInstance.getCurrentTimestamp());
                JSONObject createPhoneEndpointResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
                        ServiceURLEnum.CUSTOMERCOMMUNICATION_CREATE, postParamMapPhone, null, requestInstance));
                if (createPhoneEndpointResponse == null || !checkResponseOpstatus(createPhoneEndpointResponse)) {
                    memberBeanInstance.setIsCreationSuccessful(false);
                    memberBeanInstance.setFailureReason(String.valueOf(createPhoneEndpointResponse));
                    return;
                }
            }
        }

        // Create a customer group mapping
        Map<String, String> postParamMapGroup = new HashMap<>();
        postParamMapGroup.put("Customer_id", memberBeanInstance.getMemberId());
        postParamMapGroup.put("Group_id", DEFAULT_GROUP);
        if (!(StringUtils.isBlank(requestInstance.getParameter(INPUT_COUNTRY_CODE)))) {
            if (requestInstance.getParameter(INPUT_COUNTRY_CODE).equalsIgnoreCase(EUROPE_COUNTRY_CODE)) {
                postParamMapGroup.put("Group_id", DEFAULT_EUROPE_GROUP);
            }
        }

        postParamMapGroup.put("createdts", memberBeanInstance.getCurrentTimestamp());
        postParamMapGroup.put("lastmodifiedts", memberBeanInstance.getCurrentTimestamp());
        postParamMapGroup.put("synctimestamp", memberBeanInstance.getCurrentTimestamp());
        JSONObject createCustomerGroupEndpointResponse = CommonUtilities.getStringAsJSONObject(
                Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_CREATE, postParamMapGroup, null, requestInstance));
        if (createCustomerGroupEndpointResponse == null
                || !checkResponseOpstatus(createCustomerGroupEndpointResponse)) {
            memberBeanInstance.setIsCreationSuccessful(false);
            memberBeanInstance.setFailureReason(String.valueOf(createCustomerGroupEndpointResponse));
            return;
        }

        memberBeanInstance.setIsCreationSuccessful(true);
        return;
    }

    private boolean checkResponseOpstatus(JSONObject response) {
        if (response != null && response.has(FabricConstants.OPSTATUS)
                && response.getInt(FabricConstants.OPSTATUS) == 0) {
            return true;
        }
        return false;
    }

}