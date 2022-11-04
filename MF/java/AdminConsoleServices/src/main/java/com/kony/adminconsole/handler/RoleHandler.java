/**
 * 
 */
package com.kony.adminconsole.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.dto.Permission;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Result;

/**
 * 
 * Handler to perform operations related to Role
 * 
 * @author Aditya Mankal
 * 
 */
public class RoleHandler {

    private RoleHandler() {
        // Private Constructor
    }

    public static void removePermissionsFromRole(DataControllerRequest requestInstance, String roleId,
            List<String> permissionsList, Map<String, ArrayList<Permission>> compositePermissionMapping)
            throws ApplicationException {

        if (permissionsList != null && !permissionsList.isEmpty()) {
            Map<String, String> inputMap = new HashMap<>();
            ArrayList<Permission> currentChildPermissions = null;
            String operationResponse = StringUtils.EMPTY;
            JSONObject operationResponseJSON;
            inputMap.put("Role_id", roleId);

            for (String currPermissionId : permissionsList) {
                if (StringUtils.isBlank(currPermissionId))
                    continue;
                // If the current permission is composite, then the corresponding child
                // permissions are to be removed
                inputMap.remove("Permission_id");
                if (compositePermissionMapping.containsKey(currPermissionId)) {
                    currentChildPermissions = compositePermissionMapping.get(currPermissionId);
                    for (Permission currPermission : currentChildPermissions) {
                        inputMap.put("CompositePermission_id", currPermission.getId());
                        Executor.invokeService(ServiceURLEnum.ROLECOMPOSITEPERMISSION_DELETE, inputMap, null,
                                requestInstance);
                    }
                }
                inputMap.remove("CompositePermission_id");

                inputMap.put("Permission_id", currPermissionId);
                operationResponse = Executor.invokeService(ServiceURLEnum.ROLEPERMISSION_DELETE, inputMap, null,
                        requestInstance);

                operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

                if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                        || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    throw new ApplicationException(ErrorCodeEnum.ERR_20528);
                }
            }
        }
    }

    public static void assignPermissionToRole(DataControllerRequest requestInstance, String userID, String roleID,
            List<String> permissionsList, Map<String, ArrayList<Permission>> compositePermissionMapping)
            throws ApplicationException {

        if (permissionsList != null && !permissionsList.isEmpty()) {
            String operationResponse;
            JSONObject operationResponseJSON;
            ArrayList<Permission> currentChildPermissions;
            Map<String, String> inputMap = new HashMap<>();
            inputMap.clear();
            inputMap.put("Role_id", roleID);
            inputMap.put("createdby", userID);
            inputMap.put("modifiedby", userID);
            inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
            for (String currPermissionId : permissionsList) {
                if (StringUtils.isBlank(currPermissionId)) {
                    continue;
                }
                // If the current permission is composite, then the corresponding child
                // permissions are to be added
                inputMap.remove("Permission_id");
                if (compositePermissionMapping.containsKey(currPermissionId)) {
                    currentChildPermissions = compositePermissionMapping.get(currPermissionId);
                    for (Permission currPermission : currentChildPermissions) {
                        inputMap.put("CompositePermission_id", currPermission.getId());
                        if (currPermission.isEnabled()) {
                            inputMap.put("isEnabled", "1");
                        } else {
                            inputMap.put("isEnabled", "0");
                        }
                        Executor.invokeService(ServiceURLEnum.ROLECOMPOSITEPERMISSION_CREATE, inputMap, null,
                                requestInstance);
                    }
                }
                inputMap.remove("isEnabled");
                inputMap.remove("CompositePermission_id");

                inputMap.put("Permission_id", currPermissionId);
                operationResponse = Executor.invokeService(ServiceURLEnum.ROLEPERMISSION_CREATE, inputMap, null,
                        requestInstance);
                operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

                if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                        || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    throw new ApplicationException(ErrorCodeEnum.ERR_20528);
                }
            }
        }
    }

    public static void removeRoleFromUsers(DataControllerRequest requestInstance, String roleID, List<String> usersList)
            throws ApplicationException {

        if (usersList != null && !usersList.isEmpty()) {
            String operationResponse;
            JSONObject operationResponseJSON;
            Map<String, String> inputMap = new HashMap<>();
            inputMap.put("Role_id", roleID);
            for (String currUserId : usersList) {
                if (StringUtils.isBlank(currUserId)) {
                    continue;
                }
                inputMap.put("User_id", currUserId);
                operationResponse = Executor.invokeService(ServiceURLEnum.USERROLE_DELETE, inputMap, null,
                        requestInstance);
                operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
                if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                        || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    throw new ApplicationException(ErrorCodeEnum.ERR_20527);
                }
            }
        }
    }

    public static void assignRoleToUsers(DataControllerRequest requestInstance, String loggedInUserID, String roleID,
            List<String> usersList) throws ApplicationException {

        if (usersList != null && !usersList.isEmpty()) {
            Map<String, String> userRoleMap = InternalUserHandler.getUsersRoleMap(requestInstance, usersList);
            Map<String, String> inputMap = new HashMap<>();
            String operationResponse;
            JSONObject operationResponseJSON;
            for (Map.Entry<String, String> userRoleMapEntrySet : userRoleMap.entrySet()) {
                // Delete the current Role of the users to whom the new Role is to be assigned
                inputMap.clear();
                inputMap.put("User_id", userRoleMapEntrySet.getKey());
                inputMap.put("Role_id", userRoleMapEntrySet.getValue());
                Executor.invokeService(ServiceURLEnum.USERROLE_DELETE, inputMap, null, requestInstance);
            }

            inputMap.clear();
            inputMap.put("Role_id", roleID);
            inputMap.put("createdby", loggedInUserID);
            inputMap.put("modifiedby", loggedInUserID);
            inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
            inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            inputMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
            if (roleID.equalsIgnoreCase("RID_SUPERADMIN")) {
                inputMap.put("hasSuperAdminPrivilages", "1");
            } else {
                inputMap.put("hasSuperAdminPrivilages", "0");
            }
            for (String currUserId : usersList) {
                if (StringUtils.isBlank(currUserId)) {
                    continue;
                }
                inputMap.put("User_id", currUserId);
                operationResponse = Executor.invokeService(ServiceURLEnum.USERROLE_CREATE, inputMap, null,
                        requestInstance);
                operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
                if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                        || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    throw new ApplicationException(ErrorCodeEnum.ERR_20527);
                }
            }
        }
    }

    public static void editMappingForUserroleToCustomerRole(String inputCurrentInternalRole, JSONArray addedRoles,
            JSONArray removedRoles, Result processedResult, DataControllerRequest requestInstance)
            throws ApplicationException {
        // Delete mapping
        for (Object removedRole : removedRoles) {
            String customerRoleId = (String) removedRole;
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put("UserRole_id", inputCurrentInternalRole);
            postParametersMap.put("CustomerRole_id", customerRoleId);
            String deleteEndpointResponse = Executor.invokeService(ServiceURLEnum.USERROLECUSTOMERROLE_DELETE,
                    postParametersMap, null, requestInstance);
            CommonUtilities.getStringAsJSONObject(deleteEndpointResponse);
            // Ignore any failure in deletion
        }

        // Create mapping
        for (Object addedRole : addedRoles) {
            String customerRoleId = (String) addedRole;
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put("UserRole_id", inputCurrentInternalRole);
            postParametersMap.put("CustomerRole_id", customerRoleId);
            String createEndpointResponse = Executor.invokeService(ServiceURLEnum.USERROLECUSTOMERROLE_CREATE,
                    postParametersMap, null, requestInstance);
            JSONObject createResponse = CommonUtilities.getStringAsJSONObject(createEndpointResponse);
            if (createResponse == null || !createResponse.has(FabricConstants.OPSTATUS)
                    || createResponse.getInt(FabricConstants.OPSTATUS) != 0) {
                throw new ApplicationException(ErrorCodeEnum.ERR_21597);
            }
        }

    }
}
