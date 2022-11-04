package com.kony.adminconsole.service.campaignmanagement;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.exception.ApplicationException;
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
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to update the customers associated to the groups w.r.t to the selected attributes in campaign
 * 
 * @author Sowmya Mortha (KH2256)
 * 
 */
public class CoreBankingCustomersManageScheduler implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CoreBankingCustomersManageScheduler.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        String groupId = null;
        JSONArray customersArray = new JSONArray();

        try {
            String userId = LoggedInUserHandler.getUserDetails(requestInstance).getUserId();
            JSONArray groupsJSONArray = getAllGroups(requestInstance);
            List<String> groupIdsList = null;

            for (int i = 0; i < groupsJSONArray.length(); i++) {
                groupId = groupsJSONArray.getJSONObject(i).getString("id");

                // fetch all customers mapped to the group
                customersArray = getCustomersOfGroup(groupId, requestInstance);

                // delete all customer mapped to the group
                deleteCustomers(groupId, requestInstance);

                groupIdsList = new ArrayList<String>();
                groupIdsList.add(groupId);

                // add customer to the group based on group attributes
                CampaignHandler.findAndAddCoreBankingCustomersToGroups(requestInstance, userId, groupIdsList);
            }
        } catch (ApplicationException ae) {
            // if error is not not occurred in deleting the customers, then roll-back the customers
            if (ae.getErrorCodeEnum().compareTo(ErrorCodeEnum.ERR_21795) != 0) {
                rollbackCustomers(groupId, customersArray, requestInstance);
            }
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in CoreBankingCustomersManageScheduler JAVA service. Error: ", ae);
        }
        return result;
    }

    /**
     * This method is to rollback all the customer associated with the group
     * 
     */
    private void rollbackCustomers(String groupId, JSONArray customersArray, DataControllerRequest requestInstance)
            throws ApplicationException {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        String customerId;
        String createCustomerGroupResponse;
        JSONObject createCustomerGroupResponseJSON;
        for (int i = 0; i < customersArray.length(); i++) {
            customerId = customersArray.getJSONObject(i).getString("Customer_id");
            postParametersMap.clear();
            postParametersMap.put("Customer_id", customerId);
            postParametersMap.put("Group_id", groupId);
            createCustomerGroupResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_CREATE,
                    postParametersMap, null, requestInstance);
            createCustomerGroupResponseJSON =
                    CommonUtilities.getStringAsJSONObject(createCustomerGroupResponse);
            if (createCustomerGroupResponseJSON == null
                    || !createCustomerGroupResponseJSON.has(FabricConstants.OPSTATUS)
                    || createCustomerGroupResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.DELETE,
                        ActivityStatusEnum.FAILED,
                        "customers rollback failed by scheduler groupId: " + groupId + " customerId: " + customerId);
                throw new ApplicationException(ErrorCodeEnum.ERR_21796);
            }
        }
    }

    /**
     * This method is to get all the customer associated with the group
     * 
     */
    private JSONArray getCustomersOfGroup(String groupId, DataControllerRequest requestInstance) {
        JSONArray readCustomerGroupJSONArray = new JSONArray();
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER,
                "Group_id eq '" + groupId + "'");
        postParametersMap.put(ODataQueryConstants.SELECT, "Customer_id");

        String readCustomerGroupResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_READ,
                postParametersMap, null, requestInstance);
        JSONObject readCustomerGroupResponseJSON = CommonUtilities.getStringAsJSONObject(readCustomerGroupResponse);

        if (readCustomerGroupResponseJSON != null && readCustomerGroupResponseJSON.has(FabricConstants.OPSTATUS)
                && readCustomerGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readCustomerGroupResponseJSON.has("customergroup")) {
            readCustomerGroupJSONArray = readCustomerGroupResponseJSON.optJSONArray("customergroup");
        }
        return readCustomerGroupJSONArray;
    }

    /**
     * This method is to delete all the customer associated with the group
     * 
     */
    private void deleteCustomers(String groupId, DataControllerRequest requestInstance) throws ApplicationException {
        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("_groupId", groupId);

        String response = Executor.invokeService(ServiceURLEnum.CAMPAIGN_C360_CUSTOMERGROUP_DELETE_PROC,
                postParametersMap, null, requestInstance);
        JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

        if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
                && responseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            // need to add scheduler here as module name
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.DELETE,
                    ActivityStatusEnum.SUCCESSFUL,
                    "customers for the group are delete successfully by scheduler" + groupId);
        } else {
            // need to add scheduler here as module name
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "customers for the group are delete failed by scheduler" + groupId);
            throw new ApplicationException(ErrorCodeEnum.ERR_21795);
        }
    }

    private JSONArray getAllGroups(DataControllerRequest requestInstance) throws ParseException {
        JSONArray readMemberGroupJSONArray = new JSONArray();
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER,
                "Type_id eq 'TYPE_ID_CAMPAIGN'");
        postParametersMap.put(ODataQueryConstants.SELECT, "id");

        String readMemberGroupResponse = Executor.invokeService(ServiceURLEnum.MEMBERGROUP_READ,
                postParametersMap, null, requestInstance);
        JSONObject readMemberGroupResponseJSON = CommonUtilities.getStringAsJSONObject(readMemberGroupResponse);

        if (readMemberGroupResponseJSON != null && readMemberGroupResponseJSON.has(FabricConstants.OPSTATUS)
                && readMemberGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readMemberGroupResponseJSON.has("membergroup")) {
            readMemberGroupJSONArray = readMemberGroupResponseJSON.optJSONArray("membergroup");
        }
        return readMemberGroupJSONArray;
    }
}
