package com.kony.adminconsole.service.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.dto.Permission;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.PermissionHandler;
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
 * Service to manage the Roles(Edit Role,assign/remove users,assign/remove permissions)
 *
 * @author Aditya Mankal, Akhil
 * 
 */
public class ManageRolesService implements JavaService2 {

    private static final int ROLE_NAME_MAX_CHARS = 50;
    private static final int ROLE_DESCRIPTION_MAX_CHARS = 300;
    private static final String INPUT_ADDED_ROLES = "AddedRoles";
    private static final String INPUT_REMOVED_ROLES = "RemovedRoles";

    private static final Logger LOG = Logger.getLogger(ManageRolesService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result processedResult = new Result();

        try {

            // Fetch Logged in User Info
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);

            // Read Input Parameters
            String userId = userDetailsBeanInstance.getUserId();
            String roleDetailsJSONString = requestInstance.getParameter("Role_Details");
            String roleAssignedToJSONString = requestInstance.getParameter("AssignedTo");
            String roleRemovedFromJSONString = requestInstance.getParameter("RemovedFrom");
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

            String roleName = null;
            boolean isValidRoleData = true;

            JSONObject roleDetailsJSONObject = CommonUtilities.getStringAsJSONObject(roleDetailsJSONString);
            JSONObject roleAssignedToJSONObject = CommonUtilities.getStringAsJSONObject(roleAssignedToJSONString);
            JSONObject roleRemovedFromJSONObject = CommonUtilities.getStringAsJSONObject(roleRemovedFromJSONString);
            String roleId = roleDetailsJSONObject.optString("id");

            Map<String, String> inputMap = new HashMap<>();

            // Validate the Role information
            String roleDescription = null, updateRoleResponse = null;
            StringBuffer errorMessageBuffer = new StringBuffer();
            inputMap.put("id", roleId);
            if (roleDetailsJSONObject != null) {

                if (roleDetailsJSONObject.has("Name")) {
                    roleName = roleDetailsJSONObject.getString("Name");
                    if (StringUtils.isBlank(roleName)) {
                        errorMessageBuffer.append("Role Name cannot be an empty string\n");
                        isValidRoleData = false;
                    } else if (roleName.length() > ROLE_NAME_MAX_CHARS) {
                        errorMessageBuffer
                                .append("Role Name cannot have more than " + ROLE_NAME_MAX_CHARS + " characters\n");
                        isValidRoleData = false;
                    } else
                        inputMap.put("Name", roleName);
                }

                if (roleDetailsJSONObject.has("Description")) {
                    roleDescription = roleDetailsJSONObject.getString("Description");
                    if (StringUtils.isBlank(roleDescription)) {
                        errorMessageBuffer.append("Role Description cannot be an empty string\n");
                        isValidRoleData = false;
                    } else if (roleDescription.length() > ROLE_DESCRIPTION_MAX_CHARS) {
                        errorMessageBuffer.append("Role Description cannot have more than " + ROLE_DESCRIPTION_MAX_CHARS
                                + " characters\n");
                        isValidRoleData = false;
                    } else
                        inputMap.put("Description", roleDescription);
                }

                if (roleDetailsJSONObject.has("Status_id")) {
                    inputMap.put("Status_id", roleDetailsJSONObject.getString("Status_id"));
                }

                if (isValidRoleData) {

                    // For logging, Role Name is mandatory.Fetch the value
                    if (StringUtils.isBlank(roleName)) {
                        try {
                            Map<String, String> readPostParametersMap = new HashMap<>();
                            readPostParametersMap.put(ODataQueryConstants.FILTER, "id eq " + roleId);
                            JSONObject roleReadResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
                                    ServiceURLEnum.ROLE_READ, readPostParametersMap, null, requestInstance));
                            roleName = roleReadResponse.getJSONArray("role").getJSONObject(0).getString("Name");
                        } catch (Exception e) {
                            // Can be ignored
                        }
                    }

                    updateRoleResponse = Executor.invokeService(ServiceURLEnum.ROLE_UPDATE, inputMap, null,
                            requestInstance);
                    JSONObject updateRoleResponseJSON = CommonUtilities.getStringAsJSONObject(updateRoleResponse);
                    if (updateRoleResponseJSON != null && updateRoleResponseJSON.has(FabricConstants.OPSTATUS)
                            && updateRoleResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ROLES, EventEnum.UPDATE,
                                ActivityStatusEnum.SUCCESSFUL, "Role name: " + roleName);
                    } else {
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ROLES, EventEnum.UPDATE,
                                ActivityStatusEnum.FAILED, "Role name: " + roleName);
                        throw new ApplicationException(ErrorCodeEnum.ERR_20543);
                    }
                } else { // Invalid Role data
                    throw new ApplicationException(ErrorCodeEnum.ERR_20524);
                }
            }

            Set<String> listOfPermissions = new HashSet<>();
            JSONArray permissionsRemovedFromRoleArray = null, permissionsAssignedToRoleArray = null,
                    roleRemovedFromUsersArray = null, roleAssignedToUsersArray = null;

            // Process removed permissions and users
            if (roleRemovedFromJSONObject != null) {
                if (roleRemovedFromJSONObject.has("permissionList")) {
                    permissionsRemovedFromRoleArray = roleRemovedFromJSONObject.getJSONArray("permissionList");
                    for (int indexVar = 0; indexVar < permissionsRemovedFromRoleArray.length(); indexVar++) {
                        listOfPermissions.add(permissionsRemovedFromRoleArray.optString(indexVar));
                    }
                }
                if (roleRemovedFromJSONObject.has("usersList")) {
                    roleRemovedFromUsersArray = roleRemovedFromJSONObject.getJSONArray("usersList");
                }
            }

            // Process added permissions and users
            if (roleAssignedToJSONObject != null) {
                if (roleAssignedToJSONObject.has("permissionList")) {
                    permissionsAssignedToRoleArray = roleAssignedToJSONObject.getJSONArray("permissionList");
                    for (int indexVar = 0; indexVar < permissionsAssignedToRoleArray.length(); indexVar++) {
                        listOfPermissions.add(permissionsAssignedToRoleArray.optString(indexVar));
                    }
                }
                if (roleAssignedToJSONObject.has("usersList")) {
                    roleAssignedToUsersArray = roleAssignedToJSONObject.getJSONArray("usersList");
                }
            }

            // Get the Composite Permission Information for all the permissions listed in
            // the added/removed list
            HashMap<String, ArrayList<Permission>> compositePermissionMapping = PermissionHandler
                    .getChildPermissions(listOfPermissions, requestInstance);

            // Processing the removed Permissions list. Composite permissions corresponding
            // to each parent permission are also removed
            RoleHandler.removePermissionsFromRole(requestInstance, roleId,
                    CommonUtilities.getJSONArrayAsList(permissionsRemovedFromRoleArray), compositePermissionMapping);

            // Processing the added Permissions list. Composite permissions corresponding to
            // each parent permission are also added
            RoleHandler.assignPermissionToRole(requestInstance, userId, roleId,
                    CommonUtilities.getJSONArrayAsList(permissionsAssignedToRoleArray), compositePermissionMapping);

            // Manage the internal user role to customer role mapping
            if (addedCustomerRoles != null && removedCustomerRoles != null) {
                RoleHandler.editMappingForUserroleToCustomerRole(roleId, addedCustomerRoles, removedCustomerRoles,
                        processedResult, requestInstance);
            }

            // Processing the removed users list. The listed users are unlinked from the
            // stated role
            RoleHandler.removeRoleFromUsers(requestInstance, roleId,
                    CommonUtilities.getJSONArrayAsList(roleRemovedFromUsersArray));

            // Processing the assigned users list.The listed users are assigned the stated
            // role
            RoleHandler.assignRoleToUsers(requestInstance, userId, roleId,
                    CommonUtilities.getJSONArrayAsList(roleAssignedToUsersArray));

            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ROLES, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL, "Role name: " + roleName);

        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            Param javaExceptionParam = new Param("JavaError", e.getMessage(), FabricConstants.STRING);
            errorResult.addParam(javaExceptionParam);
            LOG.error("Exception in Managing Role Configuration. Exception:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
        return processedResult;
    }
}