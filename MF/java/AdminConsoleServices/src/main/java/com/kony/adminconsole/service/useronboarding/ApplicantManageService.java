package com.kony.adminconsole.service.useronboarding;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.handler.AddressHandler;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.DBPServices;
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
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to Manage Requests related to Applicant
 * 
 * @author Aditya Mankal
 *
 */
public class ApplicantManageService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(ApplicantManageService.class);

    protected static final String COMMUNICATION_TYPE_EMAIL = "COMM_TYPE_EMAIL";
    protected static final String COMMUNICATION_TYPE_PHONE = "COMM_TYPE_PHONE";
    private static final String APPLICANT_STATUS_PARAM_NAME = "applicantStatus";
    private static final String APPLICANT_ACCEPT_STATUS_PARAM_VALUE = "ACCEPTED";
    private static final String APPLICANT_REJECT_STATUS_PARAM_VALUE = "REJECTED";
    private static final String APPLICANT_ID_PARAM_NAME = "applicantID";

    private static final String CREATE_APPLICANT_METHOD_NAME = "createApplicant";
    private static final String CREATE_APPLICANT_ASSISTED_ONBOARDING_METHOD_NAME =
            "createApplicantViaAssistedOnboarding";

    @SuppressWarnings("unused")
    private static final String EDIT_APPLICANT_METHOD_NAME = "editApplicant";

    private static final String DATE_FORMAT = "dd-MM-yyyy";

    private static final int ADDRESS_LINE_1_MIN_CHARACTERS = 1;
    private static final int ADDRESS_LINE_1_MAX_CHARACTERS = 35;

    @SuppressWarnings("unused")
    private static final int ADDRESS_LINE_2_MIN_CHARACTERS = 1;
    private static final int ADDRESS_LINE_2_MAX_CHARACTERS = 35;

    private static final int ZIPCODE_MIN_CHARACTERS = 1;
    private static final int ZIPCODE_MAX_CHARACTERS = 35;

    private static final int FIRST_NAME_MIN_LENGTH = 5;

    private static final int LAST_NAME_MIN_LENGTH = 5;

    // private static final int MOTHERS_MAIDEN_NAME_MIN_LENGTH = 5;

    private static final int SSN_MIN_LENGTH = 9;

    private static final int APPLICANT_MIN_AGE = 18;

    private static final String APPLICANT_DEFAULT_STATUS_ID = StatusEnum.SID_CUS_ACTIVE.name();

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            if (StringUtils.equalsIgnoreCase(methodID, CREATE_APPLICANT_METHOD_NAME)) {
                return createApplicant(requestInstance);
            }
            if (StringUtils.equalsIgnoreCase(methodID, CREATE_APPLICANT_ASSISTED_ONBOARDING_METHOD_NAME)) {
                return createApplicantViaAssistedOnboarding(requestInstance);
            }

            return new Result();
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    public Result createApplicantViaAssistedOnboarding(DataControllerRequest requestInstance) {

        Result result = new Result();

        Record applicantInformationRecord = new Record();
        applicantInformationRecord.setId("applicantInformation");

        Param applicantIDParam = new Param();
        applicantIDParam.setName(APPLICANT_ID_PARAM_NAME);
        applicantIDParam.setType(FabricConstants.STRING);

        Param statusParam = new Param();
        statusParam.setType(FabricConstants.STRING);
        statusParam.setName(APPLICANT_STATUS_PARAM_NAME);
        try {
            Map<String, String> requestParameters = new HashMap<String, String>();

            String membershipCriteria = requestInstance.getParameter("criteriaID");
            String contactInformation = requestInstance.getParameter("contactInformation");
            String personalInformation = requestInstance.getParameter("personalInformation");
            String addressInformation = requestInstance.getParameter("addressInformation");
            String identityInformation = requestInstance.getParameter("identityInformation");

            requestParameters.put("criteriaID", StringUtils.trim(membershipCriteria));
            requestParameters.put("contactInformation", StringUtils.trim(contactInformation));
            requestParameters.put("personalInformation", StringUtils.trim(personalInformation));
            requestParameters.put("addressInformation", StringUtils.trim(addressInformation));
            requestParameters.put("identityInformation", StringUtils.trim(identityInformation));

            JSONObject createApplicantResponseJSON = DBPServices.createApplicant(requestParameters, requestInstance);

            if (createApplicantResponseJSON != null && createApplicantResponseJSON.has(FabricConstants.OPSTATUS)
                    && createApplicantResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {

                if (createApplicantResponseJSON.has(ErrorCodeEnum.ERROR_CODE_KEY)
                        && ((createApplicantResponseJSON.optInt(ErrorCodeEnum.ERROR_CODE_KEY) == 11017)
                                || (createApplicantResponseJSON.optInt(ErrorCodeEnum.ERROR_CODE_KEY) == 11018)
                                || (createApplicantResponseJSON.optInt(ErrorCodeEnum.ERROR_CODE_KEY) == 11019))) {
                    statusParam.setValue(APPLICANT_REJECT_STATUS_PARAM_VALUE);
                } else {
                    statusParam.setValue(APPLICANT_ACCEPT_STATUS_PARAM_VALUE);
                }
                result.addParam(statusParam);

                for (String currKey : createApplicantResponseJSON.keySet()) {
                    applicantInformationRecord.addParam(
                            new Param(currKey, createApplicantResponseJSON.optString(currKey), FabricConstants.STRING));
                }
                result.addRecord(applicantInformationRecord);

                result.addParam(new Param("status", "Success", FabricConstants.STRING));

            } else {

                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                LOG.error("Error at DBP Service Layer. Failed to create applicant - Assisted Onboarding. Exception: ");
            }

        } catch (Exception e) {
            LOG.error("Unexepected Error in Create Applicant Flow - Assisted Onboarding. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20461.setErrorCode(result);
        }

        return result;
    }

    public Result createApplicant(DataControllerRequest requestInstance) {
        Result result = new Result();
        try {
            AddressHandler addressHandler = new AddressHandler();
            String loggedInUserId = null;
            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            if (loggedInUserDetails != null) {
                loggedInUserId = loggedInUserDetails.getUserId();
            }

            // Validate Membership Criteria
            String membershipCriteria = requestInstance.getParameter("criteriaID");
            if (StringUtils.isBlank(membershipCriteria)) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "Membership Criteria ID cannot be empty", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Validate Contact Information
            String contactInformation = requestInstance.getParameter("contactInformation");
            String emailAddress = null, phoneNumber = null;
            JSONObject contactInformationJSON = CommonUtilities.getStringAsJSONObject(contactInformation);
            if (contactInformationJSON != null) {
                emailAddress = contactInformationJSON.optString("emailAddress");
                phoneNumber = contactInformationJSON.optString("phoneNumber");
            }

            if (StringUtils.isBlank(phoneNumber)) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "Phonenumber cannot be empty", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }
            if (StringUtils.isBlank(emailAddress) || !CommonUtilities.isValidEmailID(emailAddress)) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "Invalid emailAddress ", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Validate Personal Information
            String personalInformation = requestInstance.getParameter("personalInformation");
            String firstName = null, middleName = null, lastName = null, dateofBirth_str = null, ssn = null;
            JSONObject personalInformationJSON = CommonUtilities.getStringAsJSONObject(personalInformation);

            if (personalInformationJSON != null) {
                firstName = personalInformationJSON.optString("firstName");
                middleName = personalInformationJSON.optString("middleName");
                lastName = personalInformationJSON.optString("lastName");
                dateofBirth_str = personalInformationJSON.optString("dateofBirth");
                ssn = personalInformationJSON.optString("SSN");
            }

            if (StringUtils.length(firstName) < FIRST_NAME_MIN_LENGTH) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message",
                        "First Name should have a minumum of " + FIRST_NAME_MIN_LENGTH + " characters",
                        FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            if (StringUtils.length(lastName) < LAST_NAME_MIN_LENGTH) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message",
                        "Last Name should have a minumum of " + LAST_NAME_MIN_LENGTH + " characters",
                        FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            Date dateofBirth = CommonUtilities.parseDateStringToDate(dateofBirth_str, DATE_FORMAT);
            if (StringUtils.isBlank(dateofBirth_str) || dateofBirth == null) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "Invalid Date of Birth", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            } else {
                int age = CommonUtilities.calculateAge(dateofBirth);
                if (age < APPLICANT_MIN_AGE) {
                    ErrorCodeEnum.ERR_20461.setErrorCode(result);
                    result.addParam(new Param("message", "Minimum age to apply is " + APPLICANT_MIN_AGE + " years.",
                            FabricConstants.STRING));
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                }
            }

            if (StringUtils.length(ssn) < SSN_MIN_LENGTH) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "SSN should have a minumum of " + SSN_MIN_LENGTH + " characters",
                        FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Validate Address Information
            String addressInformation = requestInstance.getParameter("addressInformation");
            String addressLine1 = null, addressLine2 = null, city = null, state = null, country = null, zipcode = null,
                    uspsValidationStatus = null;

            JSONObject addressInformationJSON = CommonUtilities.getStringAsJSONObject(addressInformation);
            if (addressInformationJSON != null) {
                addressLine1 = addressInformationJSON.optString("addressLine1");
                addressLine2 = addressInformationJSON.optString("addressLine2");
                city = addressInformationJSON.optString("city");
                country = addressInformationJSON.optString("country");
                state = addressInformationJSON.optString("state");
                zipcode = addressInformationJSON.optString("zipcode");
                uspsValidationStatus = addressInformationJSON.optString("uspsValidationStatus");
            }

            if (StringUtils.isNotBlank(uspsValidationStatus)) {
                if (StringUtils.equalsIgnoreCase(uspsValidationStatus, "TRUE")
                        || (StringUtils.equalsIgnoreCase(uspsValidationStatus, "FALSE"))) {
                    uspsValidationStatus = uspsValidationStatus.toUpperCase();
                } else {
                    ErrorCodeEnum.ERR_20461.setErrorCode(result);
                    result.addParam(new Param("message", "Invalid USPS Validation Status", FabricConstants.STRING));
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                }
            }

            if (StringUtils.length(addressLine1) < ADDRESS_LINE_1_MIN_CHARACTERS
                    || StringUtils.length(addressLine1) > ADDRESS_LINE_1_MAX_CHARACTERS) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param(
                        "message", "Address Line 1 should have a minimum of" + ADDRESS_LINE_1_MIN_CHARACTERS
                                + " and a maximum of " + ADDRESS_LINE_1_MAX_CHARACTERS + " characters",
                        FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            if (StringUtils.length(addressLine2) > ADDRESS_LINE_2_MAX_CHARACTERS) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message",
                        "Address Line 2 should have a maximum of" + ADDRESS_LINE_2_MAX_CHARACTERS + " characters",
                        FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            if (StringUtils.isBlank(city)) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "Invalid City Name", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            }

            if (StringUtils.isBlank(state)) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "Invalid State Name", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            if (StringUtils.length(zipcode) < ZIPCODE_MIN_CHARACTERS
                    || StringUtils.length(zipcode) > ZIPCODE_MAX_CHARACTERS) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "Zipcode should have a minimum of" + ZIPCODE_MIN_CHARACTERS
                        + " and a maximum of " + ZIPCODE_MAX_CHARACTERS + " characters", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            if (StringUtils.isBlank(country)) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "Invalid Country Name", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Validate Identity Information
            String identityInformation = requestInstance.getParameter("identityInformation");
            String mothersMaidenName = null, idType = null, idValue = null, idState = null, idCountry = null,
                    issueDate_str = null, expiryDate_str = null;
            JSONObject identityInformationJSON = CommonUtilities.getStringAsJSONObject(identityInformation);

            if (identityInformationJSON != null) {
                mothersMaidenName = identityInformationJSON.optString("mothersMaidenName");
                idType = identityInformationJSON.optString("idType");
                idValue = identityInformationJSON.optString("idValue");
                idState = identityInformationJSON.optString("idState");
                idCountry = identityInformationJSON.optString("idCountry");
                issueDate_str = identityInformationJSON.optString("issueDate");
                expiryDate_str = identityInformationJSON.optString("expiryDate");
            }

            if (StringUtils.isBlank(idType)) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "Identity Type cannot be empty", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            if (StringUtils.isBlank(idValue)) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "Identity Value cannot be empty", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            if (StringUtils.isBlank(idState)) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "Invalid Identity State", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            if (StringUtils.isBlank(idCountry)) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "Invalid Identity Country", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            Date issueDate = CommonUtilities.parseDateStringToDate(issueDate_str, DATE_FORMAT);
            if (StringUtils.isBlank(issueDate_str) || issueDate == null) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "Invalid Identity Issue Date", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            Date expiryDate = CommonUtilities.parseDateStringToDate(expiryDate_str, DATE_FORMAT);
            if (StringUtils.isBlank(expiryDate_str) || expiryDate == null) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "Invalid Identity Expiry date", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            if (issueDate.after(expiryDate)) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "Identity Expirty Date cannot preceed Identity Issue Date",
                        FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            if (dateofBirth.after(issueDate)) {
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                result.addParam(new Param("message", "Identity Issue Date cannot preceed Date of Birth",
                        FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Validate Address Fields as per Master Data
            String countryID = addressHandler.getCountryID(country, requestInstance);
            if (StringUtils.isBlank(countryID)) {
                ErrorCodeEnum.ERR_20462.setErrorCode(result);
                result.addParam(new Param("message", "Invalid Country Name", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            String regionID = addressHandler.getRegionID(countryID, state, requestInstance);
            if (StringUtils.isBlank(regionID)) {
                ErrorCodeEnum.ERR_20462.setErrorCode(result);
                result.addParam(new Param("message", "Invalid State Name", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }
            String cityID = addressHandler.getCityID(regionID, countryID, city, requestInstance);
            if (StringUtils.isBlank(cityID)) {
                ErrorCodeEnum.ERR_20462.setErrorCode(result);
                result.addParam(new Param("message", "Invalid City Name", FabricConstants.STRING));
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Cause of Rejection
            String reason = requestInstance.getParameter("reason");
            String status = requestInstance.getParameter("status");
            if (StringUtils.isBlank(status)) {
                status = APPLICANT_DEFAULT_STATUS_ID;
            }

            // Store Applicant Address
            Map<String, String> inputBodyMap = new HashMap<String, String>();
            String addressID = CommonUtilities.getNewId().toString();
            inputBodyMap.put("id", addressID);
            inputBodyMap.put("Region_id", regionID);
            inputBodyMap.put("City_id", cityID);
            inputBodyMap.put("addressLine1", addressLine1);
            inputBodyMap.put("addressLine2", addressLine2);
            inputBodyMap.put("zipCode", zipcode);
            inputBodyMap.put("createdby", loggedInUserId);
            inputBodyMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());

            String createAddressResponse = Executor.invokeService(ServiceURLEnum.ADDRESS_CREATE, inputBodyMap, null,
                    requestInstance);
            JSONObject createAddressResponseJSON = CommonUtilities.getStringAsJSONObject(createAddressResponse);
            if (createAddressResponseJSON != null && createAddressResponseJSON.has(FabricConstants.OPSTATUS)
                    && createAddressResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                LOG.debug("Applicant address created succesfully.Proceeding further...");
            } else {
                LOG.debug("Failed to create Applicant address. Terminating process...");
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                return result;
            }

            // Store Applicant Information
            inputBodyMap.clear();
            String applicantID = Long.toString(CommonUtilities.getNumericId());
            inputBodyMap.put("id", applicantID);
            inputBodyMap.put("CustomerType_id", "TYPE_ID_PROSPECT");
            inputBodyMap.put("Status_id", APPLICANT_DEFAULT_STATUS_ID);
            inputBodyMap.put("FirstName", firstName);
            inputBodyMap.put("MiddleName", middleName);
            inputBodyMap.put("LastName", lastName);
            inputBodyMap.put("UserName", applicantID);
            inputBodyMap.put("DateOfBirth", CommonUtilities.convertTimetoISO8601Format(dateofBirth));
            inputBodyMap.put("MothersMaidenName", mothersMaidenName);
            inputBodyMap.put("EligbilityCriteria", membershipCriteria);
            inputBodyMap.put("Reason", reason);
            inputBodyMap.put("AddressValidationStatus", uspsValidationStatus);
            inputBodyMap.put("IDType", idType);
            inputBodyMap.put("IDValue", idValue);
            inputBodyMap.put("IDState", idState);
            inputBodyMap.put("IDCountry", idCountry);
            inputBodyMap.put("IDIssueDate", CommonUtilities.convertTimetoISO8601Format(issueDate));
            inputBodyMap.put("IDExpiryDate", CommonUtilities.convertTimetoISO8601Format(expiryDate));
            inputBodyMap.put("createdby", loggedInUserId);
            inputBodyMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());

            String createApplicantResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_CREATE, inputBodyMap, null,
                    requestInstance);
            result.addParam(new Param("applicantID", applicantID, FabricConstants.STRING));
            result.addParam(new Param("message", createApplicantResponse, FabricConstants.STRING));

            JSONObject createApplicantResponseJSON = CommonUtilities.getStringAsJSONObject(createApplicantResponse);
            if (createApplicantResponseJSON != null && createApplicantResponseJSON.has(FabricConstants.OPSTATUS)
                    && createApplicantResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                LOG.debug("Applicant created successfully.");
                result.addParam(new Param("status", "Success", FabricConstants.STRING));

            } else {
                LOG.error("Failed to create applicant");
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED, "Customer onboarding failed. Customer first name: " + firstName);
                ErrorCodeEnum.ERR_20461.setErrorCode(result);
                return result;
            }

            // Create Applicant Address
            inputBodyMap.clear();
            inputBodyMap.put("Customer_id", applicantID);
            inputBodyMap.put("Address_id", addressID);
            String createApplicantAddressResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERADDRESS_CREATE,
                    inputBodyMap, null, requestInstance);
            result.addParam(new Param("applicantID", applicantID, FabricConstants.STRING));
            result.addParam(new Param("message", createApplicantResponse, FabricConstants.STRING));

            JSONObject createApplicantAddressResponseJSON = CommonUtilities
                    .getStringAsJSONObject(createApplicantAddressResponse);
            if (createApplicantAddressResponseJSON != null
                    && createApplicantAddressResponseJSON.has(FabricConstants.OPSTATUS)
                    && createApplicantAddressResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                LOG.debug("Applicant address created successfully.");
                result.addParam(new Param("status", "Success", FabricConstants.STRING));

            } else {
                LOG.error("Failed to create applicant address");
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20465.setErrorCode(result);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED, "Customer onboarding failed. Customer first name: " + firstName);
                return result;
            }

            // Create Applicant email
            Map<String, String> postParamMapEmail = new HashMap<>();
            postParamMapEmail.put("id", String.valueOf(CommonUtilities.getNewId()));
            postParamMapEmail.put("Type_id", COMMUNICATION_TYPE_EMAIL);
            postParamMapEmail.put("Customer_id", applicantID);
            postParamMapEmail.put("isPrimary", "1");
            postParamMapEmail.put("Value", emailAddress);
            postParamMapEmail.put("Extension", "Personal");
            postParamMapEmail.put("createdts", CommonUtilities.convertTimetoISO8601Format(issueDate));
            postParamMapEmail.put("lastmodifiedts", CommonUtilities.convertTimetoISO8601Format(issueDate));
            postParamMapEmail.put("synctimestamp", CommonUtilities.convertTimetoISO8601Format(issueDate));
            JSONObject createEmailEndpointResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
                    ServiceURLEnum.CUSTOMERCOMMUNICATION_CREATE, postParamMapEmail, null, requestInstance));
            if (createEmailEndpointResponse != null && createEmailEndpointResponse.has(FabricConstants.OPSTATUS)
                    && createEmailEndpointResponse.getInt(FabricConstants.OPSTATUS) == 0) {
                LOG.debug("Applicant email created successfully.");
                result.addParam(new Param("status", "Success", FabricConstants.STRING));

            } else {
                LOG.error("Failed to create applicant email");
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20466.setErrorCode(result);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED, "Customer onboarding failed. Customer first name: " + firstName);
                return result;
            }

            // Create Applicant phone number
            Map<String, String> postParamMapPhone = new HashMap<>();
            postParamMapPhone.put("id", String.valueOf(CommonUtilities.getNewId()));
            postParamMapPhone.put("Type_id", COMMUNICATION_TYPE_PHONE);
            postParamMapPhone.put("Customer_id", applicantID);
            postParamMapPhone.put("isPrimary", "1");
            postParamMapPhone.put("Value", phoneNumber);
            postParamMapPhone.put("Extension", "Personal");
            postParamMapPhone.put("createdts", CommonUtilities.convertTimetoISO8601Format(issueDate));
            postParamMapPhone.put("lastmodifiedts", CommonUtilities.convertTimetoISO8601Format(issueDate));
            postParamMapPhone.put("synctimestamp", CommonUtilities.convertTimetoISO8601Format(issueDate));
            JSONObject createPhoneEndpointResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
                    ServiceURLEnum.CUSTOMERCOMMUNICATION_CREATE, postParamMapPhone, null, requestInstance));
            if (createPhoneEndpointResponse != null && createPhoneEndpointResponse.has(FabricConstants.OPSTATUS)
                    && createPhoneEndpointResponse.getInt(FabricConstants.OPSTATUS) == 0) {
                LOG.debug("Applicant phone created successfully.");
                result.addParam(new Param("status", "Success", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.CREATE,
                        ActivityStatusEnum.SUCCESSFUL,
                        "Customer onboarding successful. Customer first name: " + firstName);

            } else {
                LOG.error("Failed to create applicant phone");
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20467.setErrorCode(result);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED, "Customer onboarding failed. Customer first name: " + firstName);
                return result;
            }

        } catch (Exception e) {
            LOG.error("Unexepected Error in Create Applicant Flow. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED,
                    "Customer onboarding failed. Customer first name: "
                            + CommonUtilities.getStringAsJSONObject(requestInstance.getParameter("personalInformation"))
                                    .optString("firstName"));
            ErrorCodeEnum.ERR_20461.setErrorCode(result);
        }
        return result;
    }

}