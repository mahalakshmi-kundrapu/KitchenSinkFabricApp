package com.kony.adminconsole.service.customermanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.exception.DBPAuthenticationException;
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
 * CustomerUpdateBasicInfo service will update the basic information of a customer
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerUpdateBasicInfo implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerUpdateBasicInfo.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();
            CustomerHandler customerHandler = new CustomerHandler();

            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String customerId = requestInstance.getParameter("Customer_id");
            String customerUsername = null;
            String newUserName = null;

            if ((customerId == null || StringUtils.isBlank(customerId))
                    && requestInstance.getParameter("userName") != null) {
                requestInstance.setAttribute("isServiceBeingAccessedByOLB", true);
                customerUsername = requestInstance.getParameter("userName");
                if (StringUtils.isBlank(customerUsername)) {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                            ActivityStatusEnum.FAILED,
                            "Customer basic information update failed. Customer id:" + customerId);
                    ErrorCodeEnum.ERR_20612.setErrorCode(processedResult);
                    Param statusParam = new Param("Status", "Edit failure", FabricConstants.STRING);
                    processedResult.addParam(statusParam);
                    return processedResult;

                }

                customerId = customerHandler.getCustomerId(customerUsername, requestInstance);
            }

            if (StringUtils.isBlank(customerId)) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Customer basic information update failed. Customer id:" + customerId);

                ErrorCodeEnum.ERR_20613.setErrorCode(processedResult);
                Param statusParam = new Param("Status", "Edit failure", FabricConstants.STRING);
                processedResult.addParam(statusParam);
                return processedResult;
            }

            if (LoggedInUserHandler.getUserDetails(requestInstance).isAPIUser() == false) {
                // Check the access control for this customer for current logged-in internal
                // user
                CustomerHandler.doesCurrentLoggedinUserHasAccessToCustomer(null, customerId, requestInstance,
                        processedResult);
                if (processedResult.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                    return processedResult;
                }
                // End of access check
            }

            String Salutation = requestInstance.getParameter("Salutation");
            String CustomerStatus_id = requestInstance.getParameter("Status_id");
            boolean isOnlyStatusUpdate = false;
            if (requestInstance.getParameter("isOnlyStatusUpdate") != null) {
                isOnlyStatusUpdate = Boolean.parseBoolean(requestInstance.getParameter("isOnlyStatusUpdate"));
            }
            String MaritialStatus = requestInstance.getParameter("MaritalStatus_id");
            String EmployementStatus_id = requestInstance.getParameter("EmployementStatus_id");
            String SpouseName = requestInstance.getParameter("SpouseName");
            String modifiedByName = userDetailsBeanInstance.getUserName();
            String eagreementStatus = requestInstance.getParameter("eagreementStatus");
            newUserName = requestInstance.getParameter("newUserName");
            JSONArray listOfAddedRisks = null, listOfRemovedRisks = null;
            if (requestInstance.getParameter("listOfAddedRisks") != null)
                listOfAddedRisks = new JSONArray(requestInstance.getParameter("listOfAddedRisks"));
            if (requestInstance.getParameter("listOfRemovedRisks") != null)
                listOfRemovedRisks = new JSONArray(requestInstance.getParameter("listOfRemovedRisks"));

            JSONObject updateCustomerInfo;
            if (isOnlyStatusUpdate) {
                updateCustomerInfo = updateCustomerStatus(authToken, customerId, CustomerStatus_id, requestInstance);
            } else {
                try {
                    updateCustomerInfo = updateCustomerInfo(processedResult, authToken, customerUsername, customerId,
                            Salutation, MaritialStatus, EmployementStatus_id, SpouseName, modifiedByName,
                            eagreementStatus, CustomerStatus_id, newUserName, requestInstance);
                } catch (DBPAuthenticationException e) {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                            ActivityStatusEnum.FAILED,
                            "Customer basic information update failed. Customer id:" + customerId);

                    e.getErrorCodeEnum().setErrorCode(processedResult);
                    return processedResult;
                }
            }
            if (updateCustomerInfo == null || !updateCustomerInfo.has(FabricConstants.OPSTATUS)
                    || updateCustomerInfo.getInt(FabricConstants.OPSTATUS) != 0) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Customer basic information update failed. Customer id:" + customerId);

                ErrorCodeEnum.ERR_20885.setErrorCode(processedResult);
                return processedResult;
            }
            // Remove customer risks

            if (listOfRemovedRisks != null && listOfRemovedRisks.length() >= 1) {
                deleteRisks(authToken, modifiedByName, listOfRemovedRisks, customerId, requestInstance);
            }
            // Insert customer risks

            if (listOfAddedRisks != null && listOfAddedRisks.length() >= 1) {
                JSONObject createResponseJSON = createRisks(authToken, modifiedByName, listOfAddedRisks, customerId,
                        requestInstance);
                if (createResponseJSON == null || !createResponseJSON.has(FabricConstants.OPSTATUS)
                        || createResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    ErrorCodeEnum.ERR_20886.setErrorCode(processedResult);
                    return processedResult;
                }
            }

            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL,
                    "Customer basic information update successful. Customer id:" + customerId);
            Param statusParam = new Param("Status", "Edit successful", FabricConstants.STRING);
            processedResult.addParam(statusParam);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    private JSONObject updateCustomerStatus(String AuthToken, String CustomerID, String CustomerStatus_id,
            DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("id", CustomerID);
        postParametersMap.put("Status_id", CustomerStatus_id);
        postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

        String updateEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_UPDATE, postParametersMap, null,
                requestInstance);
        return CommonUtilities.getStringAsJSONObject(updateEndpointResponse);
    }

    private JSONObject updateCustomerInfo(Result processedResult, String authToken, String customerUsername,
            String customerID, String Salutation, String MaritialStatus, String EmployementStatus_id, String SpouseName,
            String modifiedByName, String eagreementStatus, String CustomerStatus_id, String newUserName,
            DataControllerRequest requestInstance) throws DBPAuthenticationException {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("id", customerID);

        if (StringUtils.isNotBlank(Salutation))
            postParametersMap.put("Salutation", Salutation);
        if (StringUtils.isNotBlank(CustomerStatus_id)) {
            postParametersMap.put("Status_id", CustomerStatus_id);
        }

        if (StringUtils.isNotBlank(MaritialStatus))
            postParametersMap.put("MaritalStatus_id", MaritialStatus);

        if (StringUtils.isNotBlank(SpouseName))
            postParametersMap.put("SpouseName", SpouseName);

        if (StringUtils.isNotBlank(EmployementStatus_id))
            postParametersMap.put("EmployementStatus_id", EmployementStatus_id);

        if (StringUtils.isNotBlank(modifiedByName))
            postParametersMap.put("modifiedby", modifiedByName);

        if (StringUtils.isNotBlank(eagreementStatus))
            postParametersMap.put("isEagreementSigned", eagreementStatus);
        postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

        if (StringUtils.isNotBlank(newUserName))
            postParametersMap.put("UserName", newUserName);

        String updateEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_UPDATE, postParametersMap, null,
                requestInstance);

        JSONObject updateResponseJson = CommonUtilities.getStringAsJSONObject(updateEndpointResponse);
        return updateResponseJson;
    }

    public void deleteRisks(String AuthToken, String modifiedByName, JSONArray listOfRemovedRisks, String CustomerID,
            DataControllerRequest requestInstance) {

        for (int indexVar = 0; indexVar < listOfRemovedRisks.length(); indexVar++) {
            String riskID = listOfRemovedRisks.getString(indexVar);
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.clear();
            postParametersMap.put("Status_id", riskID);
            postParametersMap.put("Customer_id", CustomerID);

            Executor.invokeService(ServiceURLEnum.CUSTOMERFLAGSTATUS_DELETE, postParametersMap, null, requestInstance);
        }
    }

    public JSONObject createRisks(String AuthToken, String modifiedByName, JSONArray listOfAddedRisks,
            String CustomerID, DataControllerRequest requestInstance) {

        JSONObject createResponseJSON = null;
        for (int indexVar = 0; indexVar < listOfAddedRisks.length(); indexVar++) {
            String riskID = listOfAddedRisks.getString(indexVar);
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.clear();
            postParametersMap.put("Status_id", riskID);
            postParametersMap.put("Customer_id", CustomerID);
            postParametersMap.put("createdby", modifiedByName);
            postParametersMap.put("modifiedby", modifiedByName);
            String addResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERFLAGSTATUS_CREATE, postParametersMap,
                    null, requestInstance);
            createResponseJSON = CommonUtilities.getStringAsJSONObject(addResponse);
        }
        return createResponseJSON;
    }

}