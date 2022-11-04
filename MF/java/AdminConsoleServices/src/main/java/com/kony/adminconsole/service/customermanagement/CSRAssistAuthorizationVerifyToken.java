package com.kony.adminconsole.service.customermanagement;

import java.net.URLDecoder;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.CSRAssistHandler;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.handler.PermissionHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * CSRAssistAuthorization service is used to authorize for CSR assist
 * 
 * @author Alahari Prudhvi Akhil , Aditya Mankal
 * 
 * 
 */
public class CSRAssistAuthorizationVerifyToken implements JavaService2 {

    public static final String CSR_ASSIST_PERMISSIONID = "PID45";
    private static final Logger LOG = Logger.getLogger(CSRAssistAuthorizationVerifyToken.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            CustomerHandler customerHandler = new CustomerHandler();
            Result processedResult = new Result();
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            UserDetailsBean loggedInUserDetails = null;
            try {
                loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            } catch (Exception e) {
                LOG.error("Failed to fetch details from identity scope");
            }
            String adminName = loggedInUserDetails != null ? loggedInUserDetails.getUserId() : null;
            String customerId = null;
            String adminRole = loggedInUserDetails != null ? loggedInUserDetails.getUserId() : null;

            String reportingParams = requestInstance.getParameter("ReportingParams");
            String csrAssistGrantToken = requestInstance.getParameter("csrAssistGrantToken");
            if (StringUtils.isBlank(csrAssistGrantToken)) {
                ErrorCodeEnum.ERR_20940.setErrorCode(processedResult);
                processedResult.addParam(new Param("isTokenValid", "false", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, adminName, adminRole, ModuleNameEnum.CUSTOMERS,
                        EventEnum.LOGIN, ActivityStatusEnum.FAILED,
                        "CSR Assist: Session token consumption failed. Session token:" + csrAssistGrantToken);
                return processedResult;
            }
            if (StringUtils.isBlank(reportingParams)) {
                ErrorCodeEnum.ERR_20952.setErrorCode(processedResult);
                processedResult.addParam(new Param("isTokenValid", "false", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, adminName, adminRole, ModuleNameEnum.CUSTOMERS,
                        EventEnum.LOGIN, ActivityStatusEnum.FAILED,
                        "CSR Assist: Session token consumption failed. Session token:" + csrAssistGrantToken);
                return processedResult;
            }

            // Read application id
            JSONObject reportingParamsJson = new JSONObject(URLDecoder.decode(reportingParams, "UTF-8"));
            String appId = reportingParamsJson.optString("aid");
            if (StringUtils.isBlank(appId)) {
                ErrorCodeEnum.ERR_20953.setErrorCode(processedResult);
                processedResult.addParam(new Param("isTokenValid", "false", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, adminName, adminRole, ModuleNameEnum.CUSTOMERS,
                        EventEnum.LOGIN, ActivityStatusEnum.FAILED,
                        "CSR Assist: Session token consumption failed. Session token:" + csrAssistGrantToken);
                return processedResult;
            }

            CSRAssistHandler csrAssistHandler = new CSRAssistHandler();

            boolean tokenValidationResponse = csrAssistHandler.validateCSRAssistGrantToken(csrAssistGrantToken, appId,
                    requestInstance, processedResult);
            if (requestInstance.getAttribute("CustomerId") != null) {
                customerId = (String) requestInstance.getAttribute("CustomerId");
            }
            if (!tokenValidationResponse) {
                processedResult.addParam(new Param("isTokenValid", "false", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, adminName, adminRole, ModuleNameEnum.CUSTOMERS,
                        EventEnum.LOGIN, ActivityStatusEnum.FAILED,
                        "CSR Assist: Session token consumption failed. Session token:" + csrAssistGrantToken);
                return processedResult;
            }
            csrAssistHandler.updateTokenRetrievedAndConsumedTimeStamp(csrAssistGrantToken, requestInstance, authToken);
            adminName = requestInstance.getAttribute("adminName") != null
                    ? (String) requestInstance.getAttribute("adminName")
                    : null;
            adminRole = requestInstance.getAttribute("adminRole") != null
                    ? (String) requestInstance.getAttribute("adminRole")
                    : null;
            String adminId = requestInstance.getAttribute("UserId") != null
                    ? (String) requestInstance.getAttribute("UserId")
                    : null;
            String adminUserName = requestInstance.getAttribute("adminUserName") != null
                    ? (String) requestInstance.getAttribute("adminUserName")
                    : null;
            String customerTypeId = requestInstance.getAttribute("CustomerType") != null
                    ? (String) requestInstance.getAttribute("CustomerType")
                    : null;

            // Fetch CSRAssist composite permissions
            JSONObject responseJSON = PermissionHandler.getAggregateCompositePermissions(adminId, adminRole,
                    CSR_ASSIST_PERMISSIONID, requestInstance, "id,Name,Service_id,Description,isEnabled", null);
            if (responseJSON.has("ErrorEnum")) {
                ErrorCodeEnum errorCode = (ErrorCodeEnum) responseJSON.get("ErrorEnum");
                errorCode.setErrorCode(processedResult);
                if (responseJSON.has("response"))
                    processedResult.addParam(
                            new Param("response", responseJSON.getString("response"), FabricConstants.STRING));
                return processedResult;
            }
            JSONArray csrCompositePermissions = responseJSON.getJSONArray("CompositePermissions");
            JSONArray finalcsrCompositePermissions = new JSONArray();
            csrCompositePermissions.forEach((permissionObject) -> {
                JSONObject permission = (JSONObject) permissionObject;
                if (permission.getString("isEnabled").equalsIgnoreCase("true")) {
                    finalcsrCompositePermissions.put(permission);
                }
            });
            JSONArray intersectionOfCustomerandCSREntitltments = customerHandler
                    .getIntersectionOfCustomerandCSREntitlements(customerId, finalcsrCompositePermissions,
                            requestInstance);
            String username = customerHandler.getCustomerUsername(customerId, requestInstance);

            Dataset finalCompositePermissionsDataset = CommonUtilities
                    .constructDatasetFromJSONArray(intersectionOfCustomerandCSREntitltments);
            finalCompositePermissionsDataset.setId("CSRAssistCompositePermissions");
            processedResult.addDataset(finalCompositePermissionsDataset);

            // Add C360 user details details
            Record adminDetails = new Record();
            adminDetails.addParam(new Param("name", adminName));
            adminDetails.addParam(new Param("id", adminId));
            adminDetails.addParam(new Param("role", adminRole));
            adminDetails.addParam(new Param("username", adminUserName));
            adminDetails.setId("C360UserDetails");
            processedResult.addRecord(adminDetails);

            processedResult.addParam(new Param("CustomerType", customerTypeId, FabricConstants.STRING));
            processedResult.addParam(new Param("CustomerUsername", username, FabricConstants.STRING));
            processedResult.addParam(new Param("CustomerId", customerId, FabricConstants.STRING));
            processedResult.addParam(new Param("isTokenValid", "true", FabricConstants.STRING));

            // Log event
            AuditHandler.auditAdminActivity(requestInstance, adminName, adminRole, ModuleNameEnum.CUSTOMERS,
                    EventEnum.LOGIN, ActivityStatusEnum.SUCCESSFUL, "CSR Assist: Session token consumed. Session token:"
                            + csrAssistGrantToken + " username:" + username);

            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.error("Unexpected exception in CSR assist verify token service", e);
            ErrorCodeEnum.ERR_20954.setErrorCode(errorResult);
            return errorResult;
        }
    }

}
