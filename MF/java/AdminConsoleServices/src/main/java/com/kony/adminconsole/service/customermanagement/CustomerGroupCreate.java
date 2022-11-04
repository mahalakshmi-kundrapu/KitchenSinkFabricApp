package com.kony.adminconsole.service.customermanagement;

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
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.CustomerHandler;
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
 * @author Sowmya Mortha
 */
public class CustomerGroupCreate implements JavaService2 {

    private static final Logger logger = Logger.getLogger(CustomerGroupCreate.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result result = new Result();
        try {
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            JSONArray custUserNames = CommonUtilities.getStringAsJSONArray(requestInstance.getParameter("usernames"));
            JSONArray groupNames = CommonUtilities.getStringAsJSONArray(requestInstance.getParameter("groupNames"));
            // check for customer names
            if (custUserNames == null || custUserNames.length() == 0) {
                ErrorCodeEnum.ERR_20615.setErrorCode(result);
                result.addParam(new Param("status", "failure", FabricConstants.STRING));
                return result;

            }
            // check for group names
            if (groupNames == null || groupNames.length() == 0) {
                ErrorCodeEnum.ERR_20616.setErrorCode(result);
                result.addParam(new Param("status", "failure", FabricConstants.STRING));
                return result;

            }
            // fetch user details
            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            String systemUser = (loggedInUserDetails != null) ? loggedInUserDetails.getUserId() : "";

            // fetch customer id from customer user name
            CustomerHandler customerHandler = new CustomerHandler();
            JSONArray customerIdsArray = new JSONArray();
            String customerId = null;
            for (int i = 0; i < custUserNames.length(); i++) {
                customerId = customerHandler.getCustomerId(custUserNames.getString(i), requestInstance);
                if (StringUtils.isBlank(customerId)) {
                    ErrorCodeEnum.ERR_20613.setErrorCode(result);
                    result.addParam(new Param("status", "failure", FabricConstants.STRING));
                    return result;

                }
                customerIdsArray.put(customerId);
            }
            // fetch group ids
            JSONArray groupsIds = fetchGroupIdByGroupName(groupNames, requestInstance);
            if (groupsIds.length() != groupNames.length()) {
                ErrorCodeEnum.ERR_20614.setErrorCode(result);
                result.addParam(new Param("status", "failure", FabricConstants.STRING));
                return result;

            }
            // add customer to group
            CustomerUpdateGroups customerUpdateGroups = new CustomerUpdateGroups();
            for (int count = 0; count < customerIdsArray.length(); count++) {
                JSONObject response = customerUpdateGroups.createGroups(authToken, systemUser, groupsIds,
                        customerIdsArray.getString(count), requestInstance);
                if (response == null || !response.has(FabricConstants.OPSTATUS)
                        || response.getInt(FabricConstants.OPSTATUS) != 0) {
                    logger.error("Error in adding customer to group - " + customerIdsArray.getString(count));
                    throw new Exception(String.valueOf(response));
                }
            }
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.CREATE,
                    ActivityStatusEnum.SUCCESSFUL, "Customer Group Created Successfully");
            result.addParam(new Param("status", "success", FabricConstants.STRING));
        } catch (Exception e) {
            logger.error("Error in assigning new customer to group ", e);
            result.addParam(new Param("errorCode", "EXCEPTION", FabricConstants.STRING));
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "Customer Group Creation Failed");
            result.addParam(new Param("errorMessage", e.getMessage(), FabricConstants.STRING));
        }
        return result;
    }

    private JSONArray fetchGroupIdByGroupName(JSONArray groupNames, DataControllerRequest requestInstance) {
        requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "Status_id eq '" + StatusEnum.SID_ACTIVE.name() + "'");
        postParametersMap.put(ODataQueryConstants.SELECT, "id,Name");
        String readGroupResponse = Executor.invokeService(ServiceURLEnum.MEMBERGROUP_READ, postParametersMap, null,
                requestInstance);
        JSONArray responseArray = (JSONArray) CommonUtilities.getStringAsJSONObject(readGroupResponse).get("group");
        JSONArray groupIds = new JSONArray();
        JSONObject temp = null;
        for (int i = 0; i < groupNames.length(); i++) {
            for (int j = 0; j < responseArray.length(); j++) {
                temp = responseArray.getJSONObject(j);
                if (groupNames.getString(i).equalsIgnoreCase(temp.getString("Name"))) {
                    groupIds.put(temp.getString("id"));
                    break;
                }
            }
        }
        return groupIds;
    }

}