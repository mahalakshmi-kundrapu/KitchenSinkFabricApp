package com.kony.adminconsole.service.role;

import java.util.HashMap;
import java.util.List;
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
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.RoleHandler;
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
 * Service to create a Role
 * 
 * @author Aditya Mankal
 *
 */
public class RoleCreateService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(RoleCreateService.class);

    private static final int ROLE_NAME_MIN_LENGTH = 5;
    private static final int ROLE_NAME_MAX_LENGTH = 25;

    private static final int ROLE_DESCRIPTION_MIN_LENGTH = 5;
    private static final int ROLE_DESCRIPTION_MAX_LENGTH = 300;

    private static final String DEFAULT_ROLE_TYPE_ID = "ROLE_TYPE_1";
    private static final String INPUT_ADDED_ROLES = "AddedRoles";
    private static final String INPUT_REMOVED_ROLES = "RemovedRoles";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result processedResult = new Result();
        String roleName = requestInstance.getParameter("Role_Name");
        String roleDescription = requestInstance.getParameter("Role_Desc");
        String statusId = requestInstance.getParameter("Status_id");
        String inputAddedCustomerRoles = requestInstance.getParameter(INPUT_ADDED_ROLES);
        String inputRemovedCustomerRoles = requestInstance.getParameter(INPUT_REMOVED_ROLES);
        JSONArray addedCustomerRoles = null;
        JSONArray removedCustomerRoles = null;

        if (StringUtils.isNotBlank(inputAddedCustomerRoles)) {
            addedCustomerRoles = new JSONArray(inputAddedCustomerRoles);
        }
        if (StringUtils.isNotBlank(inputRemovedCustomerRoles)) {
            removedCustomerRoles = new JSONArray(inputRemovedCustomerRoles);
        }

        try {

            // Read and validate Inputs
            if (StringUtils.length(roleName) < ROLE_NAME_MIN_LENGTH
                    || StringUtils.length(roleName) > ROLE_NAME_MAX_LENGTH) {
                // Return Error Response
                String message = "Role Name should have a minimum of " + ROLE_NAME_MIN_LENGTH
                        + " characters and a maximum of " + ROLE_NAME_MAX_LENGTH + " characters";
                processedResult.addParam(new Param("message", message, FabricConstants.STRING));
                ErrorCodeEnum.ERR_20524.setErrorCode(processedResult);
                return processedResult;
            }

            if (StringUtils.length(roleDescription) < ROLE_DESCRIPTION_MIN_LENGTH
                    || StringUtils.length(roleDescription) > ROLE_DESCRIPTION_MAX_LENGTH) {
                // Return Error Response
                String message = "Role Description should have a minimum of " + ROLE_DESCRIPTION_MIN_LENGTH
                        + " characters and a maximum of " + ROLE_DESCRIPTION_MAX_LENGTH + " characters";
                processedResult.addParam(new Param("message", message, FabricConstants.STRING));
                ErrorCodeEnum.ERR_20524.setErrorCode(processedResult);
                return processedResult;
            }

            // Fetch Logged In User Info
            String loggedInUserId = null;
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            if (userDetailsBeanInstance != null) {
                loggedInUserId = userDetailsBeanInstance.getUserId();
            }

            // Create Role
            String roleId = CommonUtilities.getNewId().toString();
            createRole(roleId, roleName, roleDescription, statusId, loggedInUserId, requestInstance);

            // Assign Role Permissions
            JSONArray newPermissions = CommonUtilities
                    .getStringAsJSONArray(requestInstance.getParameter("Permission_ids"));
            assignPermissionsToRole(roleId, CommonUtilities.getJSONArrayAsList(newPermissions), loggedInUserId,
                    requestInstance);

            // Manage the internal user role to customer role mapping
            if (addedCustomerRoles != null && removedCustomerRoles != null) {
                RoleHandler.editMappingForUserroleToCustomerRole(roleId, addedCustomerRoles, removedCustomerRoles,
                        processedResult, requestInstance);
            }

            // Link Internal Users
            JSONArray newUsers = CommonUtilities.getStringAsJSONArray(requestInstance.getParameter("User_ids"));
            RoleHandler.assignRoleToUsers(requestInstance, loggedInUserId, roleId,
                    CommonUtilities.getJSONArrayAsList(newUsers));

            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ROLES, EventEnum.CREATE,
                    ActivityStatusEnum.SUCCESSFUL, "Role name: " + roleName);
            return processedResult;

        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ROLES, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "Role name: " + roleName);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ROLES, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "Role name: " + roleName);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

    private void createRole(String roleId, String name, String description, String status, String loggedInUser,
            DataControllerRequest requestInstance) throws ApplicationException {

        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("id", roleId);
        inputMap.put("Name", name);
        inputMap.put("Status_id", status);
        inputMap.put("Parent_id", roleId);
        inputMap.put("Description", description);
        inputMap.put("createdby", loggedInUser);
        inputMap.put("Type_id", DEFAULT_ROLE_TYPE_ID);
        inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
        String serviceResponse = Executor.invokeService(ServiceURLEnum.ROLE_CREATE, inputMap, null, requestInstance);
        JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (responseJSON == null || !responseJSON.has(FabricConstants.OPSTATUS)
                || responseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            LOG.error("Failed CRUD Operation. Response" + serviceResponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_20546);
        }

    }

    public void assignPermissionsToRole(String roleId, List<String> permissionsList, String loggedInUser,
            DataControllerRequest requestInstance) throws ApplicationException {

        String serviceResponse = null;
        JSONObject serviceResponseJSON = null;
        Map<String, String> inputMap = new HashMap<>();

        for (int i = 0; i < permissionsList.size(); i++) {
            inputMap.put("Role_id", roleId);
            inputMap.put("Permission_id", permissionsList.get(i));
            inputMap.put("createdby", "NULL");
            inputMap.put("modifiedby", "NULL");
            inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
            inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            inputMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
            inputMap.put("softdeleteflag", "0");
            serviceResponse = Executor.invokeService(ServiceURLEnum.ROLEPERMISSION_CREATE, inputMap, null,
                    requestInstance);
            serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
            if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                    || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                // Throw Application Exception
                LOG.error("Failed CRUD Operation. Response" + serviceResponse);
                throw new ApplicationException(ErrorCodeEnum.ERR_20528);
            }
            inputMap.clear();
        }
    }

    public void removeUserRoleAssociation(String userId, String roleId, DataControllerRequest requestInstance)
            throws ApplicationException {

        if (StringUtils.isBlank(roleId) || StringUtils.isBlank(userId)) {
            return;
        }

        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("Role_id", roleId);
        inputMap.put("User_id", roleId);
        String serviceResponse = Executor.invokeService(ServiceURLEnum.USERROLE_DELETE, inputMap, null,
                requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            LOG.error("Failed CRUD Operation. Response" + serviceResponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_20547);
        }

    }

    public JSONObject getUserProfile(String userId, DataControllerRequest requestInstance) throws ApplicationException {

        Map<String, String> inputMap = new HashMap<>();
        inputMap.put(ODataQueryConstants.FILTER, "User_id eq '" + userId + "'");

        String serviceResponse = Executor.invokeService(ServiceURLEnum.USERROLE_READ, inputMap, null, requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            LOG.error("Failed CRUD Operation. Response" + serviceResponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_20530);
        }
        JSONArray userRoleRecords = serviceResponseJSON.optJSONArray("userrole");
        if (userRoleRecords != null && userRoleRecords.optJSONObject(0) != null) {
            return userRoleRecords.optJSONObject(0);
        }
        return new JSONObject();
    }

}