package com.kony.adminconsole.service.usermanagement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class GetInternalUserProfile implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(GetInternalUserProfile.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {

            Result processedResult = new Result();
            String AuthToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String UserID = requestInstance.getParameter("User_id");
            String isEditString = requestInstance.getParameter("isEdit");
            boolean isEdit = false;
            if (isEditString != null) {
                isEdit = Boolean.parseBoolean(isEditString);
            }

            // read from internal users view
            JSONObject readInternalUser = readInternalUser(AuthToken, UserID, requestInstance);

            if (readInternalUser.getInt(FabricConstants.OPSTATUS) != 0) {
                return constructFailureResult(readInternalUser);
            }

            // read from userpermissions view
            JSONObject readUserPermissions = readUserPermissions(AuthToken, UserID, requestInstance);

            if (readUserPermissions.getInt(FabricConstants.OPSTATUS) != 0) {
                return constructFailureResult(readUserPermissions);
            }

            // direct permissions
            JSONObject readUserDirectPermissions = readUserDirectPermissions(AuthToken, UserID, requestInstance);
            if (readUserDirectPermissions.getInt(FabricConstants.OPSTATUS) != 0) {
                return constructFailureResult(readUserDirectPermissions);
            }

            JSONArray UserDetails = (JSONArray) readInternalUser.get("internalusers_view");
            Record userDetails = constructRecordFromJSON((JSONObject) UserDetails.get(0));
            Dataset userDetailsArray = new Dataset();
            userDetailsArray.setId("internalusers_view");
            userDetailsArray.addRecord(userDetails);

            JSONArray userPermissions = (JSONArray) readUserPermissions.get("userpermission_view");
            Dataset userPermissionsArray = new Dataset();
            userPermissionsArray.setId("userpermission_view");
            for (int count = 0; count < userPermissions.length(); count++) {
                Record userpermission = constructRecordFromJSON((JSONObject) userPermissions.get(count));
                userPermissionsArray.addRecord(userpermission);
            }

            JSONArray userdirectpermissionJSON = (JSONArray) readUserDirectPermissions.get("userdirectpermission_view");
            Dataset userDirectPermissionsDataset = new Dataset();
            userDirectPermissionsDataset.setId("userdirectpermission_view");
            for (int count = 0; count < userdirectpermissionJSON.length(); count++) {
                Record userpermission = constructRecordFromJSON((JSONObject) userdirectpermissionJSON.get(count));
                userDirectPermissionsDataset.addRecord(userpermission);
            }

            if (isEdit) {
                // Read addresses for the user
                JSONObject getInternalUserAddresses = getInternalUserAddresses(AuthToken, UserID, requestInstance);
                if (getInternalUserAddresses.getInt(FabricConstants.OPSTATUS) != 0) {
                    return constructFailureResult(getInternalUserAddresses);
                }

                String homeAddressID = null, workAddressID = null;
                JSONArray Addresses = ((JSONArray) getInternalUserAddresses.get("useraddress"));
                for (int i = 0; i < Addresses.length(); i++) {
                    if (((JSONObject) Addresses.get(i)).get("Type_id").toString().equalsIgnoreCase("ADR_TYPE_HOME")) {
                        homeAddressID = ((JSONObject) Addresses.get(i)).get("Address_id").toString();
                    } else if (((JSONObject) Addresses.get(i)).get("Type_id").toString()
                            .equalsIgnoreCase("ADR_TYPE_WORK")) {
                        workAddressID = ((JSONObject) Addresses.get(i)).get("Address_id").toString();
                    }
                }

                JSONObject getUserAddresses = getUserAddresses(AuthToken, homeAddressID, workAddressID,
                        requestInstance);
                if (getUserAddresses.getInt(FabricConstants.OPSTATUS) != 0) {
                    return constructFailureResult(getUserAddresses);
                }

                JSONObject homeAddressJSON = null, workAddressJSON = null;
                JSONArray UserAddresses = ((JSONArray) getUserAddresses.get("address"));
                for (int i = 0; i < UserAddresses.length(); i++) {
                    if (((JSONObject) UserAddresses.get(i)).get("id").toString().equalsIgnoreCase(homeAddressID)) {
                        homeAddressJSON = (JSONObject) UserAddresses.get(i);

                    } else if (((JSONObject) UserAddresses.get(i)).get("id").toString()
                            .equalsIgnoreCase(workAddressID)) {
                        workAddressJSON = (JSONObject) UserAddresses.get(i);
                    }
                }
                // read country
                if (homeAddressJSON != null) {
                    JSONObject readCountry = readCity(AuthToken, homeAddressJSON.getString("City_id"), requestInstance);
                    homeAddressJSON.put("Country_id",
                            ((JSONObject) ((JSONArray) readCountry.get("city")).get(0)).getString("Country_id"));
                }

                // Prepare Datasets
                Record homeAddrRecord = constructRecordFromJSON(homeAddressJSON);
                Dataset homeAddrDataset = new Dataset();
                homeAddrDataset.setId("Home_addr");
                homeAddrDataset.addRecord(homeAddrRecord);

                Record workAddrRecord = constructRecordFromJSON(workAddressJSON);
                Dataset workAddrDataset = new Dataset();
                workAddrDataset.setId("Work_addr");
                workAddrDataset.addRecord(workAddrRecord);

                processedResult.addDataset(homeAddrDataset);
                processedResult.addDataset(workAddrDataset);

                String roleID;
                try {
                    roleID = ((JSONObject) UserDetails.get(0)).getString("Role_id");
                } catch (Exception e) {
                    roleID = null;
                }

                // read roles
                JSONObject readRoles = readRoles(AuthToken, roleID, requestInstance);
                if (readRoles.getInt(FabricConstants.OPSTATUS) != 0) {
                    return constructFailureResult(readRoles);
                }

                JSONArray unassignedRoles = ((JSONArray) readRoles.get("role"));
                Dataset unassignedRolesDataset = new Dataset();
                unassignedRolesDataset.setId("unassigned_Roles");
                for (int count = 0; count < unassignedRoles.length(); count++) {
                    Record role = constructRecordFromJSON((JSONObject) unassignedRoles.get(count));
                    unassignedRolesDataset.addRecord(role);
                }

                processedResult.addDataset(unassignedRolesDataset);

                // read all permissions
                JSONObject readAllPermissions = readAllPermissions(AuthToken, requestInstance);
                if (readAllPermissions.getInt(FabricConstants.OPSTATUS) != 0) {
                    return constructFailureResult(readAllPermissions);
                }

                // read all role permissions
                JSONObject readRolePermissions = readRolePermissions(AuthToken, requestInstance);
                if (readRolePermissions.getInt(FabricConstants.OPSTATUS) != 0) {
                    return constructFailureResult(readRolePermissions);
                }

                JSONArray AllPermissionsJSONArray = ((JSONArray) readAllPermissions.get("permission"));
                Dataset AllPermissionsDataset = new Dataset();
                AllPermissionsDataset.setId("Permissions");
                for (int count = 0; count < AllPermissionsJSONArray.length(); count++) {
                    Record permission = constructRecordFromJSON((JSONObject) AllPermissionsJSONArray.get(count));
                    AllPermissionsDataset.addRecord(permission);
                }

                JSONArray AllRolePermissionsJSONArray = ((JSONArray) readRolePermissions.get("rolepermission"));
                Dataset RolePermissionsDataset = new Dataset();
                RolePermissionsDataset.setId("RolePermissions");
                for (int count = 0; count < AllRolePermissionsJSONArray.length(); count++) {
                    Record permission = constructRecordFromJSON((JSONObject) AllRolePermissionsJSONArray.get(count));
                    RolePermissionsDataset.addRecord(permission);
                }

                processedResult.addDataset(AllPermissionsDataset);
                processedResult.addDataset(RolePermissionsDataset);
            }

            processedResult.addDataset(userDetailsArray);
            processedResult.addDataset(userPermissionsArray);
            processedResult.addDataset(userDirectPermissionsDataset);

            Param statusParam = new Param("Status", "Succesful", FabricConstants.STRING);
            processedResult.addParam(statusParam);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    public JSONObject readCity(String AuthToken, String cityID, DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "id eq '" + cityID + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.CITY_READ, postParametersMap, null,
                requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public JSONObject readInternalUser(String AuthToken, String UserID, DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "User_id eq '" + UserID + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.INTERNALUSERS_VIEW_READ, postParametersMap,
                null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public JSONObject readAllPermissions(String AuthToken, DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "Status_id eq 'SID_ACTIVE'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.PERMISSION_READ, postParametersMap, null,
                requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public JSONObject readRolePermissions(String AuthToken, DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.ROLEPERMISSION_READ, postParametersMap,
                null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public JSONObject readRoles(String AuthToken, String roleID, DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        if (roleID != null) {
            postParametersMap.put(ODataQueryConstants.FILTER, "id ne '" + roleID + "' and Status_id eq 'SID_ACTIVE'");
        } else {
            postParametersMap.put(ODataQueryConstants.FILTER, "Status_id eq 'SID_ACTIVE'");
        }
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.ROLE_READ, postParametersMap, null,
                requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public JSONObject readUserPermissions(String AuthToken, String UserID, DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "User_id eq '" + UserID + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.USERPERMISSION_VIEW_READ, postParametersMap,
                null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public JSONObject readUserDirectPermissions(String AuthToken, String UserID,
            DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "User_id eq '" + UserID + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.USERDIRECTPERMISSION_VIEW_READ,
                postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public JSONObject getInternalUserAddresses(String AuthToken, String userID, DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "User_id eq '" + userID + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.USERADDRESS_READ, postParametersMap, null,
                requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public JSONObject getUserAddresses(String AuthToken, String homeAddressID, String workAddressID,
            DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER,
                "id eq '" + homeAddressID + "' or id eq '" + workAddressID + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.ADDRESS_READ, postParametersMap, null,
                requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public Result constructFailureResult(JSONObject ResponseJSON) {
        Result Result = new Result();
        Param errCodeParam = new Param("backend_error_code", "" + ResponseJSON.getInt(FabricConstants.OPSTATUS),
                FabricConstants.INT);
        Param errMsgParam = new Param("backend_error_message", ResponseJSON.toString(), FabricConstants.STRING);
        Param serviceMessageParam = new Param("errmsg", ResponseJSON.toString(), FabricConstants.STRING);
        Param statusParam = new Param("Status", "Failure", FabricConstants.STRING);
        Result.addParam(errCodeParam);
        Result.addParam(errMsgParam);
        Result.addParam(serviceMessageParam);
        Result.addParam(statusParam);
        return Result;
    }

    public Record constructRecordFromJSON(JSONObject JSON) {
        Record response = new Record();
        if (JSON == null || JSON.length() == 0) {
            return response;
        }
        Iterator<String> keys = JSON.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            Param param = new Param(key, JSON.getString(key), FabricConstants.STRING);
            response.addParam(param);
        }

        return response;
    }

}