package com.kony.adminconsole.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.dto.FileStreamHandlerBean;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.config.EnvironmentConfiguration;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.exception.DBPAuthenticationException;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * Collection of operations being performed via DBP
 * 
 * 
 */

public class DBPServices {

    private static final Logger LOG = Logger.getLogger(DBPServices.class);

    private static final String APP_KEY_HEADER = "DBP-X-Kony-App-Key";
    private static final String APP_SECRET_HEADER = "DBP-X-Kony-App-Secret";
    private static final String DBP_API_ACCESS_TOKEN_HEADER = "X-Kony-DBP-API-Access-Token";
    private static final String CLAIMS_TOKEN_KEY = "claims_token";

    private DBPServices() {
        // Private Constructor
    }

    /**
     * Login call to Kony DBP Services
     * 
     * @param dataControllerRequest
     * @return Session token
     * @throws DBPAuthenticationException
     */
    public static String getDBPServicesClaimsToken(DataControllerRequest dataControllerRequest)
            throws DBPAuthenticationException {
        String[] loginKeys = authenticateToDBP(null, null, null, dataControllerRequest);
        return loginKeys[0];
    }

    private static String[] authenticateToDBP(String customerUsername, JSONObject adminConsoleUserDetails,
            JSONArray CSRAssistCompositePermissions, DataControllerRequest requestInstance)
            throws DBPAuthenticationException {
        String[] loginKeys = new String[2];
        try {

            Map<String, String> headerMap = new HashMap<>();
            headerMap.put(APP_KEY_HEADER, EnvironmentConfiguration.AC_DBP_APP_KEY.getValue(requestInstance));
            headerMap.put(APP_SECRET_HEADER, EnvironmentConfiguration.AC_DBP_APP_SECRET.getValue(requestInstance));
            headerMap.put(DBP_API_ACCESS_TOKEN_HEADER,
                    EnvironmentConfiguration.AC_DBP_SHARED_SECRET.getValue(requestInstance));

            String dbpLoginResponse = Executor.invokeService(ServiceURLEnum.KONY_DBP_IDENTITYSERVICE, new HashMap<>(),
                    headerMap, requestInstance);
            JSONObject dbpLoginResponseJSON = CommonUtilities.getStringAsJSONObject(dbpLoginResponse);

            if (dbpLoginResponseJSON != null && dbpLoginResponseJSON.has(CLAIMS_TOKEN_KEY)
                    && StringUtils.isNotBlank(dbpLoginResponseJSON.optString(CLAIMS_TOKEN_KEY))) {
                LOG.debug("DBP Login Successful");
                loginKeys[0] = dbpLoginResponseJSON.optString(CLAIMS_TOKEN_KEY);
            } else {
                LOG.error("Failed to login into DBP. No Tokens found on response.");
                throw new DBPAuthenticationException(ErrorCodeEnum.ERR_20933);
            }

        } catch (DBPAuthenticationException authenticationException) {
            throw authenticationException;
        } catch (Exception exception) {
            LOG.error("Internal exception while logging to DBP", exception);
            throw new DBPAuthenticationException(ErrorCodeEnum.ERR_20933);
        }
        return loginKeys;
    }

    /**
     * enroll DBX user
     * 
     * @param CustomerUsername
     * @param Customer
     *            email
     * @param requestInstance
     * 
     * @return JSONObject
     * @throws DBPAuthenticationException
     */
    public static JSONObject enrollDBXUser(String customerUsername, String email, DataControllerRequest requestInstance)
            throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("UserName", customerUsername);
        postParametersMap.put("Email", email);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokeService(ServiceURLEnum.DBPSERVICE_ENROLLDBXUSER, postParametersMap,
                headerMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(endpointResponse);
    }

    /**
     * Gets current DBP user status
     * 
     * @param AuthToken
     *            for AdminConsole
     * @param CustomerUsername
     * @param requestInstance
     * 
     * @return JSONObject
     * @throws DBPAuthenticationException
     */
    public static JSONObject getDBPUserStatus(String customerUsername, DataControllerRequest requestInstance)
            throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();

        postParametersMap.put("UserName", customerUsername);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokeService(ServiceURLEnum.DBPSERVICE_GETDBPUSERSTATUS, postParametersMap,
                headerMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(endpointResponse);
    }

    /**
     * Updates DBP user status
     * 
     * @param AuthToken
     *            for AdminConsole
     * @param CustomerUsername
     * @param requestInstance
     * 
     * @return JSONObject
     * @throws DBPAuthenticationException
     */
    public static JSONObject updateDBPUserStatus(String customerUsername, String status,
            DataControllerRequest requestInstance) throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("UserName", customerUsername);
        postParametersMap.put("Status", status);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokeService(ServiceURLEnum.DBPSERVICE_UPDATEDBPUSERSTATUS,
                postParametersMap, headerMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(endpointResponse);
    }

    /**
     * unlock DBP user
     * 
     * @param AuthToken
     *            for AdminConsole
     * @param CustomerUsername
     * @param requestInstance
     * 
     * @return JSONObject
     * @throws DBPAuthenticationException
     */
    public static JSONObject unlockDBXUser(String customerUsername, DataControllerRequest requestInstance)
            throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("UserName", customerUsername);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokeService(ServiceURLEnum.DBPSERVICE_UNLOCKDBXUSER, postParametersMap,
                headerMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(endpointResponse);
    }

    /**
     * reset DBP user password
     * 
     * @param CustomerUsername
     * @param requestInstance
     * 
     * @return JSONObject
     * @throws DBPAuthenticationException
     */
    public static JSONObject resetDBPUserPassword(String emailAddress, String customerUsername,
            DataControllerRequest requestInstance) throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);
        if (dbpServicesClaimsToken == null)
            return null;

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("Email", emailAddress);
        postParametersMap.put("UserName", customerUsername);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokeService(ServiceURLEnum.DBPSERVICE_RESETDBPUSERPASSWORD,
                postParametersMap, headerMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(endpointResponse);

    }

    /**
     * Gets all the accounts for a customer or details of an account
     * 
     * @param AuthToken
     *            for AdminConsole
     * @param CustomerUsername
     * @param requestInstance
     * 
     * @return JSONObject of Account(s)
     * @throws DBPAuthenticationException
     */
    public static JSONObject getAccounts(String customerUsername, DataControllerRequest requestInstance)
            throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        if (customerUsername != null)
            postParametersMap.put("customerUsername", customerUsername);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.DBPSERVICE_GETACCOUNTSFORADMIN,
                postParametersMap, headerMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
    }

    /**
     * Gets all the transactions for a given account within the start and end date
     * 
     * @param authToken
     *            for AdminConsole
     * @param accountNumber
     * @param StartDate
     * @param EndDate
     * @param requestInstance
     * 
     * @return JSONObject of Transactions
     * @throws DBPAuthenticationException
     */
    public static JSONObject getTransactions(String accountNumber, String StartDate, String EndDate,
            DataControllerRequest requestInstance) throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        if (accountNumber != null)
            postParametersMap.put("accountNumber", accountNumber);
        if (StartDate != null)
            postParametersMap.put("searchStartDate", StartDate);
        if (EndDate != null)
            postParametersMap.put("searchEndDate", EndDate);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.DBPSERVICE_GETALLTRANSACTIONS,
                postParametersMap, headerMap, requestInstance);

        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
    }

    /**
     * Gets customer details with account number
     * 
     * @param authToken
     *            for AdminConsole
     * @param accountNumber
     * @param requestInstance
     * 
     * @return JSONObject customer details
     * @throws DBPAuthenticationException
     */
    public static JSONObject getCustomerWithAccountNumber(String authToken, String accountNumber,
            DataControllerRequest requestInstance) throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("accountNumber", accountNumber);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.DBPSERVICE_GETUSERDETAILSFROMACCOUNT,
                postParametersMap, headerMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
    }

    /**
     * Toggles the lock status for any given customer
     * 
     * @param authToken
     *            for AdminConsole
     * @param username
     * @param requestInstance
     * 
     * @return JSONObject lock status
     * @throws DBPAuthenticationException
     */
    public static JSONObject updateLockStatus(String authToken, String username, DataControllerRequest requestInstance)
            throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("userName", username);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        // Update call
        String updateEndpointResponse = Executor.invokeService(ServiceURLEnum.DBPSERVICE_UPDATELOCKSTATUS,
                postParametersMap, headerMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(updateEndpointResponse);

    }

    public static JSONObject sendUnlockLinkToCustomer(String username, DataControllerRequest requestInstance)
            throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("userName", username);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String updateEndpointResponse = Executor.invokePassThroughServiceAndGetString(
                ServiceURLEnum.DBPSERVICE_SEND_UNLOCK_LINK_TO_CUSTOMER, postParametersMap, headerMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(updateEndpointResponse);

    }

    /**
     * Gets current lock status for any given customer
     * 
     * @param authToken
     *            for AdminConsole
     * @param customerUsername
     * @param requestInstance
     * 
     * @return JSONObject lock status details
     * @throws DBPAuthenticationException
     */
    public static JSONObject getlockStatus(String authToken, String customerUsername,
            DataControllerRequest requestInstance) throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("userName", customerUsername);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.DBPSERVICE_GETLOCKSTATUS, postParametersMap,
                headerMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
    }

    /**
     * Function to fetch Customer Cards Data
     * 
     * @param dataControllerRequest
     * @param String
     * @param String
     * 
     * @return Customer Cards Data
     * @throws DBPAuthenticationException
     */
    public static JSONObject getCustomerCards(DataControllerRequest requestInstance, String customerUsername,
            String authToken) throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("username", customerUsername);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);
        // read call
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.DBPSERVICE_GETCUSTOMERCARDS,
                postParametersMap, headerMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
    }

    /**
     * Function to update Customer Cards Data
     * 
     * @param dataControllerRequest
     * @param String
     * @param String
     * @param String
     * @param String
     * 
     * @return Customer Cards Data
     * @throws DBPAuthenticationException
     */
    public static JSONObject updateCustomerCardStatus(DataControllerRequest requestInstance, String cardNumber,
            String customerUsername, String cardAction, String actionReason) throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("maskedCardNumber", cardNumber);
        postParametersMap.put("Action", cardAction);
        if (StringUtils.isNotBlank(actionReason))
            postParametersMap.put("Reason", actionReason);
        postParametersMap.put("username", customerUsername);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String updateCardStatusResponse = Executor.invokeService(ServiceURLEnum.DBPSERVICE_UPDATECUSTOMERCARD,
                postParametersMap, headerMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(updateCardStatusResponse);
    }

    /**
     * Update the e-Statement status of a customer account (true -> e-Statement, false -> paper)
     * 
     * @param authToken
     *            for AdminConsole
     * @param accountID
     * @param eStatementStatus
     * @param eStatementEmail
     * @param requestInstance
     * 
     * @return response JSONObject from OLB service
     * @throws ApplicationException
     */
    public static JSONObject updateEstatementStatus(String authToken, String accountID, String eStatementStatus,
            String eStatementEmail, DataControllerRequest requestInstance) throws ApplicationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        if (accountID != null) {
            postParametersMap.put("accountID", accountID);
        }
        if (eStatementStatus != null) {
            postParametersMap.put("eStatementEnable", eStatementStatus);
        }
        if (eStatementStatus != null && eStatementEmail != null && eStatementStatus.equals("true")) {
            postParametersMap.put("email", eStatementEmail);
        }
        UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
        postParametersMap.put("UpdatedBy", userDetailsBeanInstance.getUserName());

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String readEndpointResponse = Executor.invokeService(
                ServiceURLEnum.DBPSERVICE_UPDATEUSERACCOUNTSETTINGSFORADMIN, postParametersMap, headerMap,
                requestInstance);
        JSONObject readEndpointResponseJSON = CommonUtilities.getStringAsJSONObject(readEndpointResponse);

        return readEndpointResponseJSON;
    }

    /**
     * Gets account specific alerts for a customer
     * 
     * @param customerUsername
     * 
     * @return JSONObject account specific alerts
     * @throws DBPAuthenticationException
     */
    public static JSONObject getAccountSpecificAlerts(String authToken, String customerUsername,
            DataControllerRequest requestInstance) throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("username", customerUsername);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.DBPSERVICE_GETACCOUNTSPECIFICALERTS,
                postParametersMap, headerMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
    }

    /**
     * Creates an Applicant
     * 
     * @param createApplicantPayload
     * @param requestInstance
     * @return
     * @throws DBPAuthenticationException
     */
    public static JSONObject createApplicant(Map<String, String> createApplicantPayload,
            DataControllerRequest requestInstance) throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);
        String createApplicantResponse = Executor.invokePassThroughServiceAndGetString(
                ServiceURLEnum.DBPSERVICE_CREATEDBXCUSTOMER, createApplicantPayload, headerMap, requestInstance);

        return CommonUtilities.getStringAsJSONObject(createApplicantResponse);
    }

    public static JSONObject createCompany(String type, String name, String description, String communication,
            String address, String owner, String membership, String accountsList, DataControllerRequest requestInstance)
            throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("Type", type);
        postParametersMap.put("Name", name);
        postParametersMap.put("Description", description);
        postParametersMap.put("Communication", communication);
        postParametersMap.put("Address", address);
        if (StringUtils.isNotBlank(owner)) {
            postParametersMap.put("Owner", owner);
        }
        if (StringUtils.isNotBlank(membership)) {
            postParametersMap.put("Membership", membership);
        }
        postParametersMap.put("AccountsList", accountsList);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokePassThroughServiceAndGetString(
                ServiceURLEnum.DBPSERVICE_CREATEORGANIZATION, postParametersMap, headerMap, requestInstance);

        return CommonUtilities.getStringAsJSONObject(endpointResponse);
    }

    public static JSONObject editCompany(String id, String type, String name, String description, String communication,
            String address, String owner, String accountsList, DataControllerRequest requestInstance)
            throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("Type", type);
        postParametersMap.put("Name", name);
        postParametersMap.put("Description", description);
        postParametersMap.put("id", id);
        postParametersMap.put("Communication", communication);
        postParametersMap.put("Address", address);
        if (StringUtils.isNotBlank(owner)) {
            postParametersMap.put("Owner", owner);
        }
        postParametersMap.put("AccountsList", accountsList);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokePassThroughServiceAndGetString(
                ServiceURLEnum.DBPSERVICE_EDITORGANIZATION, postParametersMap, headerMap, requestInstance);

        return CommonUtilities.getStringAsJSONObject(endpointResponse);

    }

    public static JSONObject createCustomer(String typeId, String organizationId, String emailAddress, String ssn,
            String phoneNumber, String firstName, String lastName, String dateOfBirth, String username, String accounts,
            String roleId, String middleName, String drivingLicenseNumber, String services,
            DataControllerRequest requestInstance) throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);
        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("Organization_id", organizationId);
        postParametersMap.put("Email", emailAddress);
        postParametersMap.put("Ssn", ssn);
        postParametersMap.put("Phone", phoneNumber);
        postParametersMap.put("FirstName", firstName);
        postParametersMap.put("LastName", lastName);
        postParametersMap.put("DateOfBirth", dateOfBirth);
        postParametersMap.put("UserName", username);
        postParametersMap.put("accounts", accounts);
        postParametersMap.put("Role_id", roleId);
        postParametersMap.put("MiddleName", middleName);
        postParametersMap.put("DrivingLicenseNumber", drivingLicenseNumber);
        postParametersMap.put("services", services);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = null;
        if (typeId.equalsIgnoreCase("TYPE_ID_SMALL_BUSINESS")) {
            endpointResponse = Executor.invokePassThroughServiceAndGetString(ServiceURLEnum.DBPSERVICE_CREATECUSTOMER,
                    postParametersMap, headerMap, requestInstance);
        } else if (typeId.equalsIgnoreCase("TYPE_ID_MICRO_BUSINESS")) {
            endpointResponse = Executor.invokePassThroughServiceAndGetString(
                    ServiceURLEnum.DBPSERVICE_CREATEMICROCUSTOMER, postParametersMap, headerMap, requestInstance);
        }
        return CommonUtilities.getStringAsJSONObject(endpointResponse);

    }

    /**
     * edit Customer
     * 
     * @param requestInstance
     * 
     * @return JSONObject
     * @throws DBPAuthenticationException
     */
    public static JSONObject editCustomer(String id, String Email, String Ssn, String Phone, String FirstName,
            String LastName, String DateOfBirth, String UserName, String accounts, String Role_id, String MiddleName,
            String DrivingLicenseNumber, String services, String riskStatus, String isEagreementSigned,
            DataControllerRequest requestInstance) throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);
        if (dbpServicesClaimsToken == null)
            return null;

        Map<String, String> postParametersMap = new HashMap<>();

        postParametersMap.put("id", id);
        postParametersMap.put("Email", Email);
        postParametersMap.put("Ssn", Ssn);
        postParametersMap.put("Phone", Phone);
        postParametersMap.put("FirstName", FirstName);
        postParametersMap.put("LastName", LastName);
        postParametersMap.put("DateOfBirth", DateOfBirth);
        postParametersMap.put("UserName", UserName);
        postParametersMap.put("accounts", accounts);
        postParametersMap.put("Role_id", Role_id);
        postParametersMap.put("MiddleName", MiddleName);
        postParametersMap.put("DrivingLicenseNumber", DrivingLicenseNumber);
        postParametersMap.put("services", services);
        postParametersMap.put("RiskStatus", riskStatus);
        postParametersMap.put("isEagreementSigned", isEagreementSigned);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokePassThroughServiceAndGetString(ServiceURLEnum.DBPSERVICE_EDITCUSTOMER,
                postParametersMap, headerMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(endpointResponse);

    }

    public static JSONObject getCompanySearch(String searchType, String emailAddress, String name, String id,
            DataControllerRequest requestInstance) throws DBPAuthenticationException, IOException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);
        if (StringUtils.isBlank(emailAddress)) {
            emailAddress = "";
        }
        if (StringUtils.isBlank(name)) {
            name = "";
        }
        if (StringUtils.isBlank(id)) {
            id = "";
        }
        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("Email", emailAddress);
        postParametersMap.put("searchType", searchType);
        postParametersMap.put("Name", name);
        postParametersMap.put("id", id);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokePassThroughServiceAndGetString(
                ServiceURLEnum.DBPSERVICE_GETCOMPANYSEARCH, postParametersMap, headerMap, requestInstance);

        return CommonUtilities.getStringAsJSONObject(endpointResponse);
    }

    public static JSONObject getCompanyCustomers(String organizationId, DataControllerRequest requestInstance)
            throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("Organization_id", organizationId);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);
        String endpointResponse = Executor.invokePassThroughServiceAndGetString(
                ServiceURLEnum.DBPSERVICE_GETCOMPANYCUSTOMERS, postParametersMap, headerMap, requestInstance);

        return CommonUtilities.getStringAsJSONObject(endpointResponse);

    }

    public static JSONObject getCompanyAccounts(String organizationId, DataControllerRequest requestInstance)
            throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("Organization_Id", organizationId);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokePassThroughServiceAndGetString(
                ServiceURLEnum.DBPSERVICE_GETCOMPANYACCOUNTS, postParametersMap, headerMap, requestInstance);

        return CommonUtilities.getStringAsJSONObject(endpointResponse);

    }

    public static JSONObject verifyUsername(String username, String idmIdentifier,
            DataControllerRequest requestInstance) throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("UserName", username);
        postParametersMap.put("IDMidentifier", idmIdentifier);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokePassThroughServiceAndGetString(
                ServiceURLEnum.DBPSERVICE_VERIFYUSERNAME, postParametersMap, headerMap, requestInstance);

        return CommonUtilities.getStringAsJSONObject(endpointResponse);

    }

    public static JSONObject validateTIN(String tinNumber, DataControllerRequest requestInstance)
            throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("Tin", tinNumber);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokePassThroughServiceAndGetString(ServiceURLEnum.DBPSERVICE_VALIDATETIN,
                postParametersMap, headerMap, requestInstance);

        return CommonUtilities.getStringAsJSONObject(endpointResponse);

    }

    public static JSONObject getAllAccounts(String accountId, String membershipId, String taxid,
            DataControllerRequest requestInstance) throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        if (accountId == null) {
            accountId = "";
        }
        if (membershipId == null) {
            membershipId = "";
        }
        if (taxid == null) {
            taxid = "";
        }
        postParametersMap.put("Account_id", accountId);
        postParametersMap.put("Membership_id", membershipId);
        postParametersMap.put("Taxid", taxid);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokePassThroughServiceAndGetString(
                ServiceURLEnum.DBPSERVICE_GETALLACCOUNTS, postParametersMap, headerMap, requestInstance);

        return CommonUtilities.getStringAsJSONObject(endpointResponse);
    }

    public static JSONObject unlinkAccounts(String organizationId, String accountsList,
            DataControllerRequest requestInstance) throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("Organization_id", organizationId);
        postParametersMap.put("AccountsList", accountsList);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokePassThroughServiceAndGetString(
                ServiceURLEnum.DBPSERVICE_UNLINKACCOUNTS, postParametersMap, headerMap, requestInstance);

        return CommonUtilities.getStringAsJSONObject(endpointResponse);
    }

    public static JSONObject ssnVerification(String dateOfBirth, String ssn, DataControllerRequest requestInstance)
            throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("DateOfBirth", dateOfBirth);
        postParametersMap.put("Ssn", ssn);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokePassThroughServiceAndGetString(
                ServiceURLEnum.DBPSERVICE_VERIFYOFACANDCIP, postParametersMap, headerMap, requestInstance);

        return CommonUtilities.getStringAsJSONObject(endpointResponse);
    }

    public static JSONObject getCustomerAccounts(String customerId, DataControllerRequest requestInstance)
            throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("Customer_id", customerId);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokePassThroughServiceAndGetString(
                ServiceURLEnum.DBPSERVICE_GETCUSTOMERACCOUNTS, postParametersMap, headerMap, requestInstance);

        return CommonUtilities.getStringAsJSONObject(endpointResponse);

    }

    public static JSONObject upgradeUser(String userName, String name, String communication, String address,
            String membership, DataControllerRequest requestInstance) throws DBPAuthenticationException {

        String dbpServicesClaimsToken = getDBPServicesClaimsToken(requestInstance);

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("UserName", userName);
        postParametersMap.put("Name", name);
        postParametersMap.put("Communication", communication);
        postParametersMap.put("Address", address);
        if (StringUtils.isNotBlank(membership)) {
            postParametersMap.put("Membership", membership);
        }

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("backendToken", dbpServicesClaimsToken);

        String endpointResponse = Executor.invokePassThroughServiceAndGetString(ServiceURLEnum.DBPSERVICE_UPGRADEUSER,
                postParametersMap, headerMap, requestInstance);

        return CommonUtilities.getStringAsJSONObject(endpointResponse);
    }

    public static JSONObject createDecisionRule(String decisionName, String description,
            DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("decisionName", decisionName);
        postParametersMap.put("description", description);
        String endpointResponse = Executor.invokeService(ServiceURLEnum.DBPSERVICE_CREATEDECISIONRULE,
                postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(endpointResponse);
    }

    public static JSONObject getDecisionRules(DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<>();
        String endpointResponse = Executor.invokePassThroughServiceAndGetString(
                ServiceURLEnum.DBPSERVICE_GETDECISIONRULE, postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(endpointResponse);
    }

    public static JSONObject editDecisionRule(String decisionId, String decisionName, String description,
            String isActive, String isSoftDeleted, DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("decisionId", decisionId);
        postParametersMap.put("decisionName", decisionName);
        postParametersMap.put("description", description);
        postParametersMap.put("isActive", isActive);
        postParametersMap.put("isSoftDeleted", isSoftDeleted);
        String endpointResponse = Executor.invokeService(ServiceURLEnum.DBPSERVICE_EDITDECISIONRULE, postParametersMap,
                null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(endpointResponse);
    }

    public static JSONObject getAllFilesforDecisionRule(String decisionId, String decisionName,
            DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("decisionId", decisionId);
        postParametersMap.put("decisionName", decisionName);
        String endpointResponse = Executor.invokePassThroughServiceAndGetString(
                ServiceURLEnum.DBPSERVICE_GETALLRULEFILESFORDECISION, postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(endpointResponse);
    }

    public static JSONObject uploadRuleFile(DataControllerRequest requestInstance)
            throws DBPAuthenticationException, IOException {

        @SuppressWarnings("deprecation")
        HttpServletRequest request = (HttpServletRequest) requestInstance.getOriginalRequest();
        InputStream inputStream = request.getInputStream();
        // Procedure suggested by Fabric team to pass input stream
        requestInstance.setAttribute(FabricConstants.PASS_THROUGH_HTTP_ENTITY, inputStream);

        String serviceResponse = Executor.invokePassThroughServiceAndGetString(ServiceURLEnum.DBPSERVICE_UPLOADRULEFILE,
                null, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(serviceResponse);

    }

    public static FileStreamHandlerBean downloadRuleFile(String decisionName, String version, String decisionId,
            DataControllerRequest requestInstance) throws Exception {

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("decisionName", decisionName);
        postParametersMap.put("version", version);
        postParametersMap.put("decisionId", decisionId);
        InputStream stream = Executor.invokePassThroughServiceAndGetEntity(ServiceURLEnum.DBPSERVICE_DOWNLOADRULEFILE,
                postParametersMap, null, requestInstance);

        FileStreamHandlerBean fileStreamHandlerBean = new FileStreamHandlerBean();
        if (stream != null) {
            fileStreamHandlerBean.setFile(CommonUtilities.getInputStreamAsFile(stream));
            fileStreamHandlerBean.setFileName(StringUtils.EMPTY);// TODO: Add Service Call to Fetch File Meta
        }

        return fileStreamHandlerBean;

    }

}