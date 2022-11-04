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
 * CustomerUpdateGroups service will update mapping between customer and groups
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerUpdateGroups implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(CustomerUpdateGroups.class);
    private static final String INPUT_LIST_OF_ADDED_GROUPS = "listOfAddedGroups";
    private static final String INPUT_LIST_OF_REMOVED_GROUPS = "listOfRemovedGroups";
    private static final String INPUT_CUSTOMER_ID = "Customer_id";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            String AuthToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String customerId = requestInstance.getParameter(INPUT_CUSTOMER_ID);
            String modifiedByName = userDetailsBeanInstance.getUserName();
            JSONArray listOfAddedGroups = null, listOfRemovedGroups = null;
            if (requestInstance.getParameter(INPUT_LIST_OF_ADDED_GROUPS) != null)
                listOfAddedGroups = new JSONArray(requestInstance.getParameter(INPUT_LIST_OF_ADDED_GROUPS));
            if (requestInstance.getParameter(INPUT_LIST_OF_REMOVED_GROUPS) != null)
                listOfRemovedGroups = new JSONArray(requestInstance.getParameter(INPUT_LIST_OF_REMOVED_GROUPS));

            if (StringUtils.isBlank(customerId)) {
                ErrorCodeEnum.ERR_20565.setErrorCode(processedResult);
                return processedResult;
            }
            if (StringUtils.isBlank(requestInstance.getParameter(INPUT_LIST_OF_ADDED_GROUPS))
                    && StringUtils.isBlank(requestInstance.getParameter(INPUT_LIST_OF_REMOVED_GROUPS))) {
                ErrorCodeEnum.ERR_20543.setErrorCode(processedResult);
                return processedResult;
            }

            // Check the access control for this customer for current logged-in internal
            // user
            CustomerHandler.doesCurrentLoggedinUserHasAccessToCustomer(null, customerId, requestInstance,
                    processedResult);
            if (processedResult.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                return processedResult;
            }
            // End of access check

            // Remove customer groups
            if (listOfRemovedGroups != null && listOfRemovedGroups.length() >= 1) {
                deleteGroups(AuthToken, modifiedByName, listOfRemovedGroups, customerId, requestInstance);
            }

            // Insert customer groups
            if (listOfAddedGroups != null && listOfAddedGroups.length() >= 1) {
                JSONObject createResponseJSON = createGroups(AuthToken, modifiedByName, listOfAddedGroups, customerId,
                        requestInstance);
                if (createResponseJSON == null || !createResponseJSON.has(FabricConstants.OPSTATUS)
                        || createResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                            ActivityStatusEnum.FAILED, "Update customer groups failed. Customer id: " + customerId);
                    ErrorCodeEnum.ERR_20401.setErrorCode(processedResult);
                    Param statusParam = new Param("Status", "Edit failure", FabricConstants.STRING);
                    processedResult.addParam(statusParam);
                    return processedResult;

                }
            }

            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL, "Assign groups to customer successful. Customer id: " + customerId);
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

    public void deleteGroups(String AuthToken, String modifiedByName, JSONArray listOfRemovedGroups, String CustomerID,
            DataControllerRequest requestInstance) {

        for (int indexVar = 0; indexVar < listOfRemovedGroups.length(); indexVar++) {
            String groupID = listOfRemovedGroups.getString(indexVar);
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.clear();
            postParametersMap.put("Group_id", groupID);
            postParametersMap.put("Customer_id", CustomerID);
            Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_DELETE, postParametersMap, null, requestInstance);
        }
    }

    public JSONObject createGroups(String AuthToken, String modifiedByName, JSONArray listOfAddedGroups,
            String CustomerID, DataControllerRequest requestInstance) {

        JSONObject createResponseJSON = null;
        for (int indexVar = 0; indexVar < listOfAddedGroups.length(); indexVar++) {
            String groupID = listOfAddedGroups.getString(indexVar);
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.clear();
            postParametersMap.put("Group_id", groupID);
            postParametersMap.put("Customer_id", CustomerID);
            postParametersMap.put("createdby", modifiedByName);
            postParametersMap.put("modifiedby", modifiedByName);
            String addResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_CREATE, postParametersMap, null,
                    requestInstance);
            postParametersMap.remove("User_id");
            createResponseJSON = CommonUtilities.getStringAsJSONObject(addResponse);
        }
        return createResponseJSON;
    }

}