package com.kony.adminconsole.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.JSONUtils;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.dto.Permission;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * Handler to perform operations related to Permission
 * 
 * @author Aditya Mankal, Akhil Alahari
 * 
 */
public class PermissionHandler {

    private static final Logger LOG = Logger.getLogger(PermissionHandler.class);

    private PermissionHandler() {
        // Private Constructor
    }

    /**
     * Method to get list of permissions granted to an Internal User
     * 
     * @param userId
     * @param requestInstance
     * @return List of Permissions
     * @throws ApplicationException
     * @throws IOException
     */
    public static List<Permission> getUserGrantedPermissions(String userId, DataControllerRequest requestInstance)
            throws ApplicationException, IOException {

        List<Permission> permissions = new ArrayList<>();

        // Prepare Query Map
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("_userId", userId);

        // Fetch granted permissions
        String serviceResponse = Executor.invokeService(ServiceURLEnum.SYSTEMUSER_PERMISSION_PROC_SERVICE, queryMap,
                null, requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            LOG.error("Failed to fetch granted permissions. Service Response:" + serviceResponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_20006);
        }
        LOG.debug("Fetched granted permissions");

        // Construct result DTO
        JSONArray userPermissionsJSONArray = serviceResponseJSON.getJSONArray("records");
        if (userPermissionsJSONArray != null && userPermissionsJSONArray.length() > 0) {
            String permissionsArrayString = userPermissionsJSONArray.toString();
            permissionsArrayString = StringUtils.replaceIgnoreCase(permissionsArrayString, "\"TRUE\"", "true");
            permissionsArrayString = StringUtils.replaceIgnoreCase(permissionsArrayString, "\"FALSE\"", "false");
            permissions = JSONUtils.parseAsList(permissionsArrayString, Permission.class);
        }

        // Return granted permissions
        List<Permission> grantedPermissions = new ArrayList<>();
        for (Permission permission : permissions) {
            if (permission.isEnabled() && permission.getSoftdeleteflag() == 0) {
                grantedPermissions.add(permission);
            }
        }
        return grantedPermissions;
    }

    public static HashMap<String, ArrayList<Permission>> getChildPermissions(Set<String> permissionsList,
            DataControllerRequest requestInstance) throws ApplicationException {

        Map<String, String> inputMap = new HashMap<>();
        StringBuilder filterQueryBuffer = new StringBuilder();
        HashMap<String, ArrayList<Permission>> permissionsInfo = new HashMap<>();
        inputMap.put(ODataQueryConstants.SELECT, "id,Name,Permission_id,isEnabled");
        for (String currPermission : permissionsList) {
            filterQueryBuffer.append("Permission_id eq '" + currPermission + "' or ");
        }
        if (filterQueryBuffer.toString().trim().endsWith("or")) {
            filterQueryBuffer.delete(filterQueryBuffer.lastIndexOf("or"), filterQueryBuffer.length());
            filterQueryBuffer.trimToSize();
        }
        inputMap.put(ODataQueryConstants.FILTER, filterQueryBuffer.toString().trim());
        String operationResponse = Executor.invokeService(ServiceURLEnum.COMPOSITEPERMISSION_READ, inputMap, null,
                requestInstance);
        JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

        if (operationResponseJSON != null && operationResponseJSON.has(FabricConstants.OPSTATUS)
                && operationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && operationResponseJSON.has("compositepermission")) {

            JSONArray permissionsRecords = operationResponseJSON.getJSONArray("compositepermission");

            String childPermissionName, childPermissionId, parentPermissionId;
            JSONObject currPermissionJSONObject;
            ArrayList<Permission> childPermissionsList;

            for (int indexVar = 0; indexVar < permissionsRecords.length(); indexVar++) {

                if (permissionsRecords.get(indexVar) instanceof JSONObject) {

                    currPermissionJSONObject = permissionsRecords.getJSONObject(indexVar);
                    parentPermissionId = currPermissionJSONObject.optString("Permission_id");
                    if (!permissionsInfo.containsKey(parentPermissionId)) {
                        childPermissionsList = new ArrayList<Permission>();
                        permissionsInfo.put(parentPermissionId, childPermissionsList);
                    }
                    childPermissionsList = permissionsInfo.get(parentPermissionId);

                    Permission currChildPermissionObject = new Permission();
                    childPermissionName = currPermissionJSONObject.optString("Name");
                    childPermissionId = currPermissionJSONObject.optString("id");
                    if (StringUtils.equalsIgnoreCase(currPermissionJSONObject.optString("isEnabled"), "1")
                            || StringUtils.equalsIgnoreCase(currPermissionJSONObject.optString("isEnabled"),
                                    String.valueOf(true))) {
                        currChildPermissionObject.setIsEnabled(true);
                    } else {
                        currChildPermissionObject.setIsEnabled(false);
                    }

                    currChildPermissionObject.setParentPermissionId(parentPermissionId);
                    currChildPermissionObject.setId(childPermissionId);
                    currChildPermissionObject.setName(childPermissionName);
                    childPermissionsList.add(currChildPermissionObject);
                }
            }
            return permissionsInfo;
        } else {
            throw new ApplicationException(ErrorCodeEnum.ERR_20744);
        }
    }

    public static void removePermissionFromRoles(DataControllerRequest requestInstance, String permissionId,
            List<String> rolesList, Map<String, ArrayList<Permission>> compositePermissionMapping)
            throws ApplicationException {

        Map<String, String> inputMap = new HashMap<>();

        if (rolesList != null && !rolesList.isEmpty()) {
            ArrayList<Permission> currentChildPermissions = new ArrayList<Permission>();
            String operationResponse;
            JSONObject operationResponseJSON;
            if (compositePermissionMapping.containsKey(permissionId)) {
                currentChildPermissions = compositePermissionMapping.get(permissionId);
            }

            for (String currRoleId : rolesList) {
                if (StringUtils.isBlank(currRoleId)) {
                    continue;
                }
                inputMap.put("Role_id", currRoleId);

                // If the current permission is composite, then the corresponding child
                // permissions are to be removed
                inputMap.remove("Permission_id");
                for (Permission currPermission : currentChildPermissions) {
                    inputMap.put("CompositePermission_id", currPermission.getId());
                    Executor.invokeService(ServiceURLEnum.ROLECOMPOSITEPERMISSION_DELETE, inputMap, null,
                            requestInstance);
                }
                inputMap.remove("CompositePermission_id");
                inputMap.put("Permission_id", permissionId);
                operationResponse = Executor.invokeService(ServiceURLEnum.ROLEPERMISSION_DELETE, inputMap, null,
                        requestInstance);
                operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
                if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                        || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    throw new ApplicationException(ErrorCodeEnum.ERR_20527);
                }
            }
        }
    }

    public static void assignPermissionToRoles(DataControllerRequest requestInstance, String loggedInUserId,
            String permissionId, List<String> rolesList,
            HashMap<String, ArrayList<Permission>> compositePermissionMapping) throws ApplicationException {

        Map<String, String> inputMap = new HashMap<>();

        if (rolesList != null && !rolesList.isEmpty()) {
            String operationResponse;
            JSONObject operationResponseJSON;
            ArrayList<Permission> currentChildPermissions = new ArrayList<Permission>();
            inputMap.put("Permission_id", permissionId);
            inputMap.put("createdby", loggedInUserId);
            inputMap.put("modifiedby", loggedInUserId);
            inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
            inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            inputMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());

            if (compositePermissionMapping.containsKey(permissionId)) {
                currentChildPermissions = compositePermissionMapping.get(permissionId);
            }

            for (String currRoleId : rolesList) {
                if (StringUtils.isBlank(currRoleId)) {
                    continue;
                }
                inputMap.put("Role_id", currRoleId);

                // If the current permission is composite, then the corresponding child
                // permissions are to be added
                if (!currentChildPermissions.isEmpty()) {
                    inputMap.remove("Permission_id");
                    for (Permission currPermission : currentChildPermissions) {
                        inputMap.put("CompositePermission_id", currPermission.getId());
                        if (currPermission.isEnabled()) {
                            inputMap.put("isEnabled", "1");
                        } else {
                            inputMap.put("isEnabled", "0");
                        }
                        operationResponse = Executor.invokeService(ServiceURLEnum.ROLECOMPOSITEPERMISSION_CREATE,
                                inputMap, null, requestInstance);
                        operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
                        if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                                || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                            throw new ApplicationException(ErrorCodeEnum.ERR_20525);
                        }
                    }
                    inputMap.remove("isEnabled");
                    inputMap.remove("CompositePermission_id");
                    inputMap.put("Permission_id", permissionId);
                }

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

    public static void removePermissionFromUsers(DataControllerRequest requestInstance, String permissionId,
            List<String> usersList, Map<String, ArrayList<Permission>> compositePermissionMapping)
            throws ApplicationException {

        if (usersList != null && !usersList.isEmpty()) {

            String operationResponse;
            JSONObject operationResponseJSON;
            Map<String, String> inputMap = new HashMap<>();
            ArrayList<Permission> currentChildPermissions = new ArrayList<Permission>();

            if (compositePermissionMapping.containsKey(permissionId)) {
                currentChildPermissions = compositePermissionMapping.get(permissionId);
            }

            for (String currUserID : usersList) {
                if (StringUtils.isBlank(currUserID)) {
                    continue;
                }
                inputMap.put("User_id", currUserID);
                inputMap.put("Permission_id", permissionId);

                // If the current permission is composite, then the corresponding child
                // permissions are to be removed
                if (!currentChildPermissions.isEmpty()) {
                    inputMap.remove("Permission_id");
                    for (Permission currPermission : currentChildPermissions) {
                        inputMap.put("CompositePermission_id", currPermission.getId());
                        operationResponse = Executor.invokeService(ServiceURLEnum.USERCOMPOSITEPERMISSION_DELETE,
                                inputMap, null, requestInstance);
                        operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
                        if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                                || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                            throw new ApplicationException(ErrorCodeEnum.ERR_20528);
                        }
                    }
                    inputMap.remove("CompositePermission_id");
                    inputMap.put("Permission_id", permissionId);
                }

                operationResponse = Executor.invokeService(ServiceURLEnum.USERPERMISSION_DELETE, inputMap, null,
                        requestInstance);
                operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
                if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                        || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    throw new ApplicationException(ErrorCodeEnum.ERR_20528);
                }
            }
        }
    }

    public static void assignPermissionToUsers(DataControllerRequest requestInstance, String loggedInUserId,
            String permissionID, List<String> usersList,
            HashMap<String, ArrayList<Permission>> compositePermissionMapping) throws ApplicationException {

        if (usersList != null && !usersList.isEmpty()) {
            String operationResponse;
            JSONObject operationResponseJSON;
            Map<String, String> inputMap = new HashMap<>();
            ArrayList<Permission> currentChildPermissions = new ArrayList<Permission>();
            inputMap.put("createdby", loggedInUserId);
            inputMap.put("modifiedby", loggedInUserId);
            inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
            inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            inputMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());

            if (compositePermissionMapping.containsKey(permissionID)) {
                currentChildPermissions = compositePermissionMapping.get(permissionID);
            }

            for (String currUserId : usersList) {
                if (StringUtils.isBlank(currUserId)) {
                    continue;
                }
                inputMap.put("User_id", currUserId);

                // If the current permission is composite, then the corresponding child
                // permissions are to be added
                inputMap.remove("Permission_id");
                for (Permission currPermission : currentChildPermissions) {
                    inputMap.put("CompositePermission_id", currPermission.getId());
                    if (currPermission.isEnabled()) {
                        inputMap.put("isEnabled", "1");
                    } else {
                        inputMap.put("isEnabled", "0");
                    }
                    operationResponse = Executor.invokeService(ServiceURLEnum.USERCOMPOSITEPERMISSION_CREATE, inputMap,
                            null, requestInstance);
                    operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
                    if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                            || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                        throw new ApplicationException(ErrorCodeEnum.ERR_20527);
                    }
                }
                inputMap.remove("isEnabled");
                inputMap.remove("CompositePermission_id");

                inputMap.put("Permission_id", permissionID);
                operationResponse = Executor.invokeService(ServiceURLEnum.USERPERMISSION_CREATE, inputMap, null,
                        requestInstance);
                operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
                if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                        || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    throw new ApplicationException(ErrorCodeEnum.ERR_20527);
                }
            }
        }
    }

    public static JSONObject getAggregateCompositePermissions(String userID, String roleID, String permissionID,
            DataControllerRequest requestInstance, String selectQuery, String isEnabled) {

        JSONObject readCompositePermission = getCompositePermissions(selectQuery, permissionID, isEnabled,
                requestInstance);
        if ((!readCompositePermission.has(FabricConstants.OPSTATUS))
                || readCompositePermission.getInt(FabricConstants.OPSTATUS) != 0
                || (!readCompositePermission.has("composite_permissions_view"))) {
            JSONObject res = new JSONObject();
            res.put("ErrorEnum", ErrorCodeEnum.ERR_20744);
            res.put("response", String.valueOf(readCompositePermission));
            return res;
        }
        JSONArray finalCompositePermissions = readCompositePermission.getJSONArray("composite_permissions_view");

        if (StringUtils.isNotBlank(roleID)) {
            JSONObject readRoleCompositePermission = getRoleCompositePermissions(roleID, isEnabled, requestInstance);
            if ((!readRoleCompositePermission.has(FabricConstants.OPSTATUS))
                    || readRoleCompositePermission.getInt(FabricConstants.OPSTATUS) != 0
                    || (!readRoleCompositePermission.has("rolecompositepermission"))) {
                JSONObject res = new JSONObject();
                res.put("ErrorEnum", ErrorCodeEnum.ERR_20745);
                res.put("response", String.valueOf(readRoleCompositePermission));
                return res;
            }
            JSONArray roleCompositePermissios = readRoleCompositePermission.getJSONArray("rolecompositepermission");

            // Override role based composite permissions on final composite permissions
            for (Object permissionObject : roleCompositePermissios) {
                JSONObject permission = (JSONObject) permissionObject;
                for (Object finalPermissionObject : finalCompositePermissions) {
                    JSONObject finalPermission = (JSONObject) finalPermissionObject;
                    if (finalPermission.getString("id").equals(permission.getString("CompositePermission_id"))) {
                        finalPermission.put("isEnabled", permission.getString("isEnabled"));
                    }
                }
            }
        }

        if (StringUtils.isNotBlank(userID)) {
            JSONObject readUserCompositePermission = getUserCompositePermissions(userID, isEnabled, requestInstance);
            if ((!readUserCompositePermission.has(FabricConstants.OPSTATUS))
                    || readUserCompositePermission.getInt(FabricConstants.OPSTATUS) != 0
                    || (!readUserCompositePermission.has("usercompositepermission"))) {

                JSONObject res = new JSONObject();
                res.put("ErrorEnum", ErrorCodeEnum.ERR_20746);
                res.put("response", String.valueOf(readUserCompositePermission));
                return res;
            }
            JSONArray userCompositePermissions = readUserCompositePermission.getJSONArray("usercompositepermission");

            // Override user based composite permissions on final composite permissions
            for (Object permissionObject : userCompositePermissions) {
                JSONObject permission = (JSONObject) permissionObject;
                for (Object finalPermissionObject : finalCompositePermissions) {
                    JSONObject finalPermission = (JSONObject) finalPermissionObject;
                    if (finalPermission.getString("id").equals(permission.getString("CompositePermission_id"))) {
                        finalPermission.put("isEnabled", permission.getString("isEnabled"));
                    }
                }
            }
        }
        JSONObject aggregateCompositePermissions = new JSONObject();
        aggregateCompositePermissions.put("CompositePermissions", finalCompositePermissions);
        return aggregateCompositePermissions;
    }

    public static JSONObject getCompositePermissions(String selectQuery, String permissionID, String isEnabled,
            DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.SELECT, selectQuery);
        String filter = "Permission_id eq '" + permissionID + "'";
        if (isEnabled != null) {
            filter += " and isEnabled eq '" + isEnabled + "'";
        }
        postParametersMap.put(ODataQueryConstants.FILTER, filter);
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.COMPOSITEPERMISSION_VIEW_READ,
                postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public static JSONObject getRoleCompositePermissions(String roleID, String isEnabled,
            DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        String filter = "Role_id eq '" + roleID + "'";
        if (isEnabled != null) {
            filter += " and isEnabled eq '" + isEnabled + "'";
        }
        postParametersMap.put(ODataQueryConstants.FILTER, filter);
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.ROLECOMPOSITEPERMISSION_READ,
                postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public static JSONObject getUserCompositePermissions(String userID, String isEnabled,
            DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        String filter = "User_id eq '" + userID + "'";
        if (isEnabled != null) {
            filter += " and isEnabled eq '" + isEnabled + "'";
        }
        postParametersMap.put(ODataQueryConstants.FILTER, filter);
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.USERCOMPOSITEPERMISSION_READ,
                postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }
}
