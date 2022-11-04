package com.kony.adminconsole.service.customermanagement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.config.EnvironmentConfiguration;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.CSRAssistHandler;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * CSRAssistAuthorization service is used to authorize for CSR assist
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CSRAssistAuthorization implements JavaService2 {

    public static final String CSRAssistPermissionID = "PID45";
    private static final String ONLINE_BANKING_DASHBOARD_URL = "/apps/KonyOLB/#_frmAccountsLanding";
    private static final String LOANS_APPLY_PERSONAL_LOAN_URL = "/apps/ConsumerLending/#_frmYourLoanNewKA";
    private static final String LOANS_APPLY_CREDIT_LOAN_URL = "/apps/ConsumerLending/#_frmCashRewardsCreditCardsKA";
    private static final String LOANS_APPLY_VEHICLE_LOAN_URL = "/apps/ConsumerLending/#_frmYourVehicleLoanKA";
    private static final String LOANS_LEARN_CREDIT_LOAN_URL = "/apps/ConsumerLending/#_frmCashRewardsCreditCardsKA";
    private static final String LOANS_LEARN_PERSONAL_LOAN_URL = "/apps/ConsumerLending/#_frmPersonalLoanPostloginKA";
    private static final String LOANS_LEARN_VEHICLE_LOAN_URL = "/apps/ConsumerLending/#_frmVehicleLoanTypeKA";
    private static final String LOANS_RESUME_CREDIT_LOAN_URL = "/apps/ConsumerLending/#_frmVerifyCreditCard";
    private static final String LOANS_CREATE_APPLICANT_URL =
            "/apps/ConsumerLending/#_frmLogin2KA?new_customer=true&loantype=";
    private static final String LOANS_DASHBOARD_URL = "/apps/ConsumerLending/#_frmLogin2KA";

    private static final String APP_ID_KONY_OLB = "KonyOLB";
    private static final String APP_ID_KONY_CL = "ConsumerLending";
    private static final Logger LOG = Logger.getLogger(CSRAssistAuthorization.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();
            String customerId = requestInstance.getParameter("customerid");
            String customerUsername = requestInstance.getParameter("customer_username");
            String loanId = requestInstance.getParameter("Loan_id");
            String loanType = requestInstance.getParameter("Loan_Type");
            String consumerLendingHostURL = EnvironmentConfiguration.AC_CSR_ASSIST_CL_HOST_URL
                    .getValue(requestInstance);

            if (methodID.equalsIgnoreCase("CSRAssistAuthorizationCreateApplicant")) {
                if (StringUtils.isBlank(loanType)) {
                    ErrorCodeEnum.ERR_20713.setErrorCode(processedResult);
                    return processedResult;
                }

                processedResult.addParam(
                        new Param("BankingURL", consumerLendingHostURL + LOANS_CREATE_APPLICANT_URL + loanType));
                return processedResult;

            }

            // Check mandatory fields
            if (StringUtils.isBlank(customerId) && StringUtils.isBlank(customerUsername)) {
                ErrorCodeEnum.ERR_20688.setErrorCode(processedResult);
                return processedResult;
            }

            // Check the access control for this customer for current logged-in internal
            // user
            CustomerHandler.doesCurrentLoggedinUserHasAccessToCustomer(customerUsername, customerId, requestInstance,
                    processedResult);
            if (processedResult.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                return processedResult;
            }
            // End of access check

            CustomerHandler customerHandler = new CustomerHandler();
            JSONObject customerDetails = customerHandler.getCustomerDetails(customerUsername, customerId,
                    requestInstance);
            customerId = customerDetails.getString("id");
            customerUsername = customerDetails.getString("UserName");

            // Check for member consent
            if (customerDetails.getString("IsAssistConsented").equalsIgnoreCase("false")
                    || customerDetails.getString("IsAssistConsented").equalsIgnoreCase("0")) {
                ErrorCodeEnum.ERR_20714.setErrorCode(processedResult);
                return processedResult;
            }

            String currentActionLogs = "", resolvedURL = "", appId = "";
            if (methodID.equalsIgnoreCase("CSRAssistAuthorizationCreateApplicant")) {

                processedResult.addParam(
                        new Param("BankingURL", consumerLendingHostURL + LOANS_CREATE_APPLICANT_URL + loanType));
                return processedResult;

            } else if (methodID.equalsIgnoreCase("CSRAssistAuthorization")) {
                // Check for member status. If status is new then CSR assist is not allowed
                if (customerDetails.getString("Status_id").equalsIgnoreCase("SID_CUS_NEW")) {
                    ErrorCodeEnum.ERR_20715.setErrorCode(processedResult);
                    return processedResult;
                }
                resolvedURL = EnvironmentConfiguration.AC_CSR_ASSIST_OLB_HOST_URL.getValue(requestInstance)
                        + ONLINE_BANKING_DASHBOARD_URL;
                currentActionLogs = "OLB application";
                appId = APP_ID_KONY_OLB;

            } else if (methodID.equalsIgnoreCase("CSRAssistAuthorizationApplyPersonalLoan")) {
                resolvedURL = consumerLendingHostURL + LOANS_APPLY_PERSONAL_LOAN_URL;
                currentActionLogs = "apply Personal loan";
                appId = APP_ID_KONY_CL;

            } else if (methodID.equalsIgnoreCase("CSRAssistAuthorizationApplyCreditLoan")) {
                resolvedURL = consumerLendingHostURL + LOANS_APPLY_CREDIT_LOAN_URL;
                currentActionLogs = "apply Credit Card loan";
                appId = APP_ID_KONY_CL;

            } else if (methodID.equalsIgnoreCase("CSRAssistAuthorizationApplyVehicleLoan")) {
                resolvedURL = consumerLendingHostURL + LOANS_APPLY_VEHICLE_LOAN_URL;
                currentActionLogs = "apply Vehicle loan";
                appId = APP_ID_KONY_CL;

            } else if (methodID.equalsIgnoreCase("CSRAssistAuthorizationLearnCreditLoan")) {
                resolvedURL = consumerLendingHostURL + LOANS_LEARN_CREDIT_LOAN_URL;
                currentActionLogs = "learn about Credit Card loan";
                appId = APP_ID_KONY_CL;

            } else if (methodID.equalsIgnoreCase("CSRAssistAuthorizationLearnVehicleLoan")) {
                resolvedURL = consumerLendingHostURL + LOANS_LEARN_VEHICLE_LOAN_URL;
                currentActionLogs = "learn about Vehicle loan";
                appId = APP_ID_KONY_CL;

            } else if (methodID.equalsIgnoreCase("CSRAssistAuthorizationLearnPersonalLoan")) {
                resolvedURL = consumerLendingHostURL + LOANS_LEARN_PERSONAL_LOAN_URL;
                currentActionLogs = "learn about Personal loan";
                appId = APP_ID_KONY_CL;

            } else if (methodID.equalsIgnoreCase("CSRAssistAuthorizationResumeLoan")) {
                resolvedURL = consumerLendingHostURL;
                if (loanType.equalsIgnoreCase("PERSONAL_APPLICATION")) {
                    resolvedURL += LOANS_APPLY_PERSONAL_LOAN_URL;
                } else if (loanType.equalsIgnoreCase("VEHICLE_APPLICATION")) {
                    resolvedURL += LOANS_APPLY_VEHICLE_LOAN_URL;
                } else if (loanType.equalsIgnoreCase("CREDIT_CARD_APPLICATION")) {
                    resolvedURL += LOANS_RESUME_CREDIT_LOAN_URL;
                }
                currentActionLogs = "resume a loan application with id: " + loanId + " ";
                appId = APP_ID_KONY_CL;

            } else if (methodID.equalsIgnoreCase("CSRAssistAuthorizationNativeApplication")) {
                requestInstance.setAttribute("isServiceBeingAccessedByOLB", true);
                resolvedURL = consumerLendingHostURL + LOANS_DASHBOARD_URL;
                appId = APP_ID_KONY_CL;

            }
            resolvedURL += "?session_token=";

            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            CSRAssistHandler csrAssistHandler = new CSRAssistHandler();

            String csrAssistGrantToken;
            try {
                csrAssistGrantToken = csrAssistHandler.generateCSRAssistGrantToken(customerId,
                        customerDetails.getString("CustomerType_id"), userDetailsBeanInstance, appId, requestInstance);
            } catch (Exception e) {
                ErrorCodeEnum.ERR_20001.setErrorCode(processedResult);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.LOGIN,
                        ActivityStatusEnum.FAILED, "CSR Assist for " + currentActionLogs
                                + ": Session token generation Failed. username: " + customerUsername);
                return processedResult;
            }

            if (StringUtils.isBlank(csrAssistGrantToken)) {
                ErrorCodeEnum.ERR_20001.setErrorCode(processedResult);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.LOGIN,
                        ActivityStatusEnum.FAILED, "CSR Assist for " + currentActionLogs
                                + ": Session token generation Failed. username: " + customerUsername);
                return processedResult;
            }

            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.LOGIN,
                    ActivityStatusEnum.SUCCESSFUL, "CSR Assist for " + currentActionLogs
                            + ": Session token generation successful. username: " + customerUsername);
            // Return session_token
            resolvedURL += csrAssistGrantToken;
            if (methodID.equalsIgnoreCase("CSRAssistAuthorizationResumeLoan")) {
                resolvedURL += "&Loan_id=" + loanId + "&isResume=true";
            }
            processedResult.addParam(new Param("BankingURL", resolvedURL, FabricConstants.STRING));

            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}