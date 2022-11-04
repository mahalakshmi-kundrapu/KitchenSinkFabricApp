package com.kony.adminconsole.service.group;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.GroupHandler;
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

public class GroupEditService extends GroupCreateService implements JavaService2 {

    private static final String MANAGE_STATUS_METHOD = "manageStatus";
    private static final Logger LOG = Logger.getLogger(GroupEditService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        
    	Result result = new Result();
    	try {
            if (methodID.equals("editGroup")) {
                EditGroupData(inputArray, requestInstance, result);
            } else if (methodID.equals(MANAGE_STATUS_METHOD)) {
                EditStatus(inputArray, requestInstance, result);
            }
        }catch (ApplicationException e) {
			e.getErrorCodeEnum().setErrorCode(result);
			LOG.error("Exception occured in GroupEditService JAVA service. ApplicationException: ", e);
		} catch (Exception e) {
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
        }
    	return result;

    }

    public void EditStatus(Object[] inputArray, DataControllerRequest requestInstance, Result result) {
        @SuppressWarnings("unchecked")
        Map<String, String> input = (HashMap<String, String>) inputArray[1];
        requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        String User_id = String.valueOf(input.get("User_id"));
        if (StringUtils.isBlank(input.get("Group_id"))) {
            Param result_param = new Param("Invalid", "id cannot be null", FabricConstants.STRING);
            result.addParam(result_param);
            return;
        } else if (StringUtils.isBlank(input.get("Status_id"))) {
            Param result_param = new Param("Invalid", "Status cannot be null", FabricConstants.STRING);
            result.addParam(result_param);
            return;
        } else {
            String groupName = null;
            try {
                Map<String, String> readPostParametersMap = new HashMap<String, String>();
                readPostParametersMap.put(ODataQueryConstants.FILTER, "id eq " + input.get("Group_id").toString());
                JSONObject roleReadResponse = CommonUtilities.getStringAsJSONObject(Executor
                        .invokeService(ServiceURLEnum.MEMBERGROUP_READ, readPostParametersMap, null, requestInstance));
                groupName = roleReadResponse.getJSONArray("membergroup").getJSONObject(0).getString("Name");
            } catch (Exception ignored) {
            }

            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.clear();
            postParametersMap.put("id", input.get("Group_id").toString());
            postParametersMap.put("Status_id", input.get("Status_id").toString());
            postParametersMap.put("modifiedby", User_id);
            Param result_param = new Param();
            String editGroupResponse = Executor.invokeService(ServiceURLEnum.MEMBERGROUP_UPDATE, postParametersMap,
                    null, requestInstance);
            JSONObject editGroupResponseJSON = CommonUtilities.getStringAsJSONObject(editGroupResponse);
            int getOpStatusCode = editGroupResponseJSON.getInt(FabricConstants.OPSTATUS);
            if (getOpStatusCode == 0) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERROLES, EventEnum.UPDATE,
                        ActivityStatusEnum.SUCCESSFUL, "Group status update successful. Group Name: '" + groupName);

                result_param = new Param("Status", "SUCCESS", FabricConstants.STRING);
                result.addParam(result_param);
                return;
            } else {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERROLES, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "Group status update failed. Group Name: '" + groupName);
                ErrorCodeEnum.ERR_20402.setErrorCode(result);
                return;
            }
        }
    }

    public void EditGroupData(Object[] inputArray, DataControllerRequest requestInstance, Result result)
            throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String> input = (HashMap<String, String>) inputArray[1];
        String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        if (StringUtils.isBlank(input.get("Group_id"))) {
            Param result_param = new Param("Invalid", "id cannot be null", FabricConstants.STRING);
            result.addParam(result_param);
            return;
        } else if (StringUtils.isBlank(input.get("Name"))) {
            Param result_param = new Param("Invalid", "Name cannot be null", FabricConstants.STRING);
            result.addParam(result_param);
            return;
        } else if (StringUtils.isBlank(input.get("Description"))) {
            Param result_param = new Param("Invalid", "Description cannot be null", FabricConstants.STRING);
            result.addParam(result_param);
            return;
        } else if (StringUtils.isBlank(input.get("Status_id"))) {
            Param result_param = new Param("Invalid", "Status cannot be null", FabricConstants.STRING);
            result.addParam(result_param);
            return;
        } else if (StringUtils.isBlank(input.get("User_id"))) {
            Param result_param = new Param("Invalid", "User_id cannot be null", FabricConstants.STRING);
            result.addParam(result_param);
            return;
        } else if (StringUtils.isBlank(input.get("isEAgreementActive"))) {
            Param result_param = new Param("Invalid", "IEAgreementActive cannot be null", FabricConstants.STRING);
            result.addParam(result_param);
            return;
        } else {
            updateGroup(input.get("Group_id").toString(), input.get("Name").toString(),
                    input.get("Description").toString(), input.get("Status_id").toString(),
                    input.get("User_id").toString(), input.get("isEAgreementActive"), authToken, requestInstance);
            String group_id = input.get("Group_id").toString();
            JSONArray removedActions = new JSONArray(requestInstance.getParameter("removedActions"));
            if (removedActions.length() > 0) {
				GroupHandler.removeGroupActions(removedActions, group_id, requestInstance, result);
            }
            JSONArray addedActions = new JSONArray(requestInstance.getParameter("addedOrUpdatedActions"));
            if (addedActions.length() > 0) {
                GroupHandler.addGroupActions(addedActions, group_id, requestInstance, result);
            }
            if (!(input.get("Type_id").equalsIgnoreCase("TYPE_ID_MICRO_BUSINESS"))
                    && (!(input.get("Type_id").equalsIgnoreCase("TYPE_ID_SMALL_BUSINESS")))) {
                JSONArray removedCustomers = new JSONArray(requestInstance.getParameter("removedCustomerIds"));
                String isRemoveAll = requestInstance.getParameter("IsRemoveAll");
                if (StringUtils.equalsIgnoreCase(isRemoveAll, "true")) {
                    Map<String, String> inputMap = new HashMap<>();
                    inputMap.put("roleId", group_id);
                    String operationResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_GROUP_UNLINK_PROC_SERVICE,
                            inputMap, null, requestInstance);
                    JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
                    if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                            || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                        LOG.error("Failed CRUD Operation" + operationResponse);
                        throw new ApplicationException(ErrorCodeEnum.ERR_20509);
                    }
                    LOG.debug("Succesful CRUD Operation");
                } else if (removedCustomers.length() > 0) {
                    removeCustomers(removedCustomers, authToken, input.get("User_id").toString(), group_id,
                            requestInstance);
                }
                JSONArray addedCustomers = new JSONArray(requestInstance.getParameter("addedCustomerIds"));
                if (addedCustomers.length() > 0) {
                    GroupCreateService createObj = new GroupCreateService();
                    createObj.createCustomersGroup(addedCustomers, group_id, authToken, input.get("User_id").toString(),
                            ServiceURLEnum.CUSTOMERGROUP_CREATE, requestInstance);
                }
            }
        }
        Param result_param = new Param("Status", "Success", FabricConstants.STRING);
        result.addParam(result_param);
    }

    public Result updateGroup(String id, String name, String Desc, String Status_id, String User_id,
            String isEAgreementActive, String authToken, DataControllerRequest requestInstance) {
        Result result = new Result();
        Param result_param = new Param();

        if (StringUtils.isBlank(name)) {
            try {
                Map<String, String> readPostParametersMap = new HashMap<String, String>();
                readPostParametersMap.put(ODataQueryConstants.FILTER, "id eq " + id);
                JSONObject roleReadResponse = CommonUtilities.getStringAsJSONObject(Executor
                        .invokeService(ServiceURLEnum.MEMBERGROUP_READ, readPostParametersMap, null, requestInstance));
                name = roleReadResponse.getJSONArray("membergroup").getJSONObject(0).getString("Name");
            } catch (Exception ignored) {
            }

        }

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        postParametersMap.put("id", id);
        postParametersMap.put("Name", name);
        postParametersMap.put("Description", Desc);
        postParametersMap.put("Status_id", Status_id);
        postParametersMap.put("modifiedby", User_id);
        postParametersMap.put("isEAgreementActive", isEAgreementActive);
        String createGroupResponse = Executor.invokeService(ServiceURLEnum.MEMBERGROUP_UPDATE, postParametersMap, null,
                requestInstance);
        JSONObject createGroupResponseJSON = CommonUtilities.getStringAsJSONObject(createGroupResponse);
        int getOpStatusCode = createGroupResponseJSON.getInt(FabricConstants.OPSTATUS);
        if (getOpStatusCode == 0) {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERROLES, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL, "Group update successful. Group Name: '" + name + "'.");
            result_param = new Param("Group_id", id.toString(), FabricConstants.STRING);
            result.addParam(result_param);
            return result;
        } else {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERROLES, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Group update failed. Group Name: '" + name + "'.");
            ErrorCodeEnum.ERR_20402.setErrorCode(result);
            return result;
        }
    }

    public Result removeEntitlements(JSONArray removedEntitlementIds, String authToken, String User_id,
            DataControllerRequest requestInstance) {
        Result result = new Result();
        Param result_param = new Param();

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        for (int i = 0; i < removedEntitlementIds.length(); i++) {

            String Group_id = ((JSONObject) removedEntitlementIds.get(i)).getString("Group_id");
            String Service_id = ((JSONObject) removedEntitlementIds.get(i)).getString("Service_id");
            // postParametersMap.put(ODataConstants.FILTER, "Group_id eq '" + Group_id + "'
            // and Service_id eq '" + Service_id +
            // "'");
            postParametersMap.put("Group_id", Group_id);
            postParametersMap.put("Service_id", Service_id);
            String deleteGroupEntitlementsResponse = Executor.invokeService(ServiceURLEnum.GROUPENTITLEMENT_DELETE,
                    postParametersMap, null, requestInstance);
            JSONObject createGroupResponseJSON = CommonUtilities.getStringAsJSONObject(deleteGroupEntitlementsResponse);
            if (createGroupResponseJSON != null && createGroupResponseJSON.has(FabricConstants.OPSTATUS)
                    && createGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                continue;
            }
            ErrorCodeEnum.ERR_20422.setErrorCode(result);
            return result;
        }
        result_param = new Param("Status", "Success", FabricConstants.STRING);
        result.addParam(result_param);
        return result;

    }

    public Result removeCustomers(JSONArray removedCustomerIds, String authToken, String User_id, String Group_id,
            DataControllerRequest requestInstance) {
        Result result = new Result();
        Param result_param = new Param();

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        for (int i = 0; i < removedCustomerIds.length(); i++) {

            String Customer_id = ((JSONObject) removedCustomerIds.get(i)).getString("Customer_id");
            postParametersMap.put("Group_id", Group_id);
            postParametersMap.put("Customer_id", Customer_id);
            String deleteGroupCustomersResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_DELETE,
                    postParametersMap, null, requestInstance);
            JSONObject deleteGroupCustomersResponseJSON = CommonUtilities
                    .getStringAsJSONObject(deleteGroupCustomersResponse);
            if (deleteGroupCustomersResponseJSON != null
                    && deleteGroupCustomersResponseJSON.has(FabricConstants.OPSTATUS)
                    && deleteGroupCustomersResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                continue;
            }
            ErrorCodeEnum.ERR_20425.setErrorCode(result);
            return result;
        }
        result_param = new Param("Status", "Success", FabricConstants.STRING);
        result.addParam(result_param);
        return result;

    }

}