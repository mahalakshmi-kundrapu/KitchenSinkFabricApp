package com.kony.adminconsole.service.customermanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.handler.AuditHandler;
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
 * CustomerUpdatePermissions service will update mapping between customer and entitlements
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerUpdatePermissions implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(CustomerUpdatePermissions.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            String AuthToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String customerID = requestInstance.getParameter("Customer_id");
            String modifiedByName = userDetailsBeanInstance.getUserName();
            JSONArray listOfAddedPermissions = null, listOfRemovedPermissions = null;
            if (requestInstance.getParameter("listOfAddedPermissions") != null)
                listOfAddedPermissions = new JSONArray(requestInstance.getParameter("listOfAddedPermissions"));
            if (requestInstance.getParameter("listOfRemovedPermissions") != null)
                listOfRemovedPermissions = new JSONArray(requestInstance.getParameter("listOfRemovedPermissions"));

            // Remove customer groups
            if (listOfRemovedPermissions != null && listOfRemovedPermissions.length() >= 1) {
                deletePermissions(AuthToken, modifiedByName, listOfRemovedPermissions, customerID, requestInstance);
            }

            // Insert customer groups
            if (listOfAddedPermissions != null && listOfAddedPermissions.length() >= 1) {
                JSONObject createResponseJSON = createPermissions(AuthToken, modifiedByName, listOfAddedPermissions,
                        customerID, requestInstance);
                if (createResponseJSON == null || !createResponseJSON.has(FabricConstants.OPSTATUS)
                        || createResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                            ActivityStatusEnum.FAILED,
                            "Updating customer entitlements failed. Customer id: " + customerID);
                    ErrorCodeEnum.ERR_20529.setErrorCode(processedResult);
                    Param statusParam = new Param("Status", "Edit failure", FabricConstants.STRING);
                    processedResult.addParam(statusParam);
                    return processedResult;

                }
            }

            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL,
                    "Assign entitlements to customer successful. Customer id: " + customerID);
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

    public void deletePermissions(String AuthToken, String modifiedByName, JSONArray listOfRemovedPermissions,
            String CustomerID, DataControllerRequest requestInstance) {

        for (int indexVar = 0; indexVar < listOfRemovedPermissions.length(); indexVar++) {
            String serviceID = listOfRemovedPermissions.getString(indexVar);
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.clear();
            postParametersMap.put("Customer_id", CustomerID);
            postParametersMap.put("Service_id", serviceID);

            Executor.invokeService(ServiceURLEnum.CUSTOMERENTITLEMENT_DELETE, postParametersMap, null, requestInstance);
        }
    }

    public JSONObject createPermissions(String AuthToken, String modifiedByName, JSONArray listOfAddedPermissions,
            String CustomerID, DataControllerRequest requestInstance) {

        JSONObject createResponseJSON = null;
        for (int indexVar = 0; indexVar < listOfAddedPermissions.length(); indexVar++) {
            String serviceID = ((JSONObject) listOfAddedPermissions.get(indexVar)).getString("Service_id");
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.clear();
            postParametersMap.put("Service_id", serviceID);
            postParametersMap.put("Customer_id", CustomerID);
            postParametersMap.put("createdby", modifiedByName);
            postParametersMap.put("modifiedby", modifiedByName);

            if (((JSONObject) listOfAddedPermissions.get(indexVar)).has("TransactionFee_id")) {
                postParametersMap.put("TransactionFee_id",
                        ((JSONObject) listOfAddedPermissions.get(indexVar)).getString("TransactionFee_id"));
            }
            if (((JSONObject) listOfAddedPermissions.get(indexVar)).has("TransactionLimit_id")) {
                postParametersMap.put("TransactionLimit_id",
                        ((JSONObject) listOfAddedPermissions.get(indexVar)).getString("TransactionLimit_id"));
            }
            String addResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERENTITLEMENT_CREATE, postParametersMap,
                    null, requestInstance);
            createResponseJSON = CommonUtilities.getStringAsJSONObject(addResponse);
        }
        return createResponseJSON;
    }

}