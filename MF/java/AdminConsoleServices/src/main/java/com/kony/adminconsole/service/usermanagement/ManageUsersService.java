package com.kony.adminconsole.service.usermanagement;

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
import com.kony.adminconsole.dto.Permission;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.EmailHandler;
import com.kony.adminconsole.handler.PermissionHandler;
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
 * Service to manage the Users(edit Details,edit Role,assign/remove permissions)
 *
 * @author Aditya Mankal, Akhil Alahari
 * 
 */
public class ManageUsersService implements JavaService2 {

    private static final String SOFT_DELETE_FLAG = "0";
    private static final Logger LOG = Logger.getLogger(ManageUsersService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        try {

            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String userID = requestInstance.getParameter("User_id");
            String firstName = requestInstance.getParameter("FirstName");
            String middleName = requestInstance.getParameter("MiddleName");
            String LastName = requestInstance.getParameter("LastName");
            String email = requestInstance.getParameter("Email");
            String username = requestInstance.getParameter("UserName");

            String modifiedByName = requestInstance.getParameter("ModifiedByName");

            String addr1 = requestInstance.getParameter("addr1");
            String addr2 = requestInstance.getParameter("addr2");
            String cityID = requestInstance.getParameter("City_id");
            String stateID = requestInstance.getParameter("State_id");
            String countryID = requestInstance.getParameter("Country_id");
            String cityName = requestInstance.getParameter("City_Name");
            String stateName = requestInstance.getParameter("State_Name");
            String countryName = requestInstance.getParameter("Country_Name");
            String zipcode = requestInstance.getParameter("Zipcode");
            String branchID = requestInstance.getParameter("BranchLocation_id");
            String branchName = requestInstance.getParameter("BranchLocation_Name");
            String roleID = requestInstance.getParameter("Role_id");
            String roleName = requestInstance.getParameter("Role_Name");
            JSONArray listOfAddedPermissions = null, listOfRemovedPermissions = null,
                    listOfRemovedPermissionsNames = null, listOfAddedPermissionsNames = null;

            if (requestInstance.getParameter("listOfAddedPermissions") != null)
                listOfAddedPermissions = new JSONArray(requestInstance.getParameter("listOfAddedPermissions"));
            if (requestInstance.getParameter("listOfRemovedPermissions") != null)
                listOfRemovedPermissions = new JSONArray(requestInstance.getParameter("listOfRemovedPermissions"));
            if (requestInstance.getParameter("listOfRemovedPermissionsNames") != null)
                listOfRemovedPermissionsNames = new JSONArray(
                        requestInstance.getParameter("listOfRemovedPermissionsNames"));
            if (requestInstance.getParameter("listOfAddedPermissionsNames") != null)
                listOfAddedPermissionsNames = new JSONArray(
                        requestInstance.getParameter("listOfAddedPermissionsNames"));

            // read from internal users view
            JSONObject readInternalUser = readInternalUser(authToken, userID, requestInstance);

            if (readInternalUser == null || !readInternalUser.has(FabricConstants.OPSTATUS)
                    || readInternalUser.getInt(FabricConstants.OPSTATUS) != 0) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Manage users failed. Unable to read system user details. username: " + username);
                ErrorCodeEnum.ERR_21450.setErrorCode(processedResult);
                return processedResult;
            }
            JSONObject UserDetails = (JSONObject) ((JSONArray) readInternalUser.get("internalusers_view")).get(0);

            String changedFields = changedItems(UserDetails, firstName, middleName, LastName, email, username, addr1,
                    addr2, cityID, stateID, countryID, cityName, stateName, countryName, zipcode, roleID, roleName,
                    listOfAddedPermissions, listOfRemovedPermissions, listOfAddedPermissionsNames,
                    listOfRemovedPermissionsNames);

            if (StringUtils.isBlank(username)) {
                username = UserDetails.getString("Username");
            }
            JSONObject updateInternalUser = updateInternalUser(authToken, modifiedByName, userID, firstName, middleName,
                    LastName, email, username, requestInstance);
            if (updateInternalUser == null || !updateInternalUser.has(FabricConstants.OPSTATUS)
                    || updateInternalUser.getInt(FabricConstants.OPSTATUS) != 0) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Manage users failed. Unable to update system user details. username: " + username);
                ErrorCodeEnum.ERR_21451.setErrorCode(processedResult);
                return processedResult;
            }

            JSONObject getInternalUserHomeAddr = getInternalUserAddresses(authToken, userID, requestInstance);
            if (getInternalUserHomeAddr == null || !getInternalUserHomeAddr.has(FabricConstants.OPSTATUS)
                    || getInternalUserHomeAddr.getInt(FabricConstants.OPSTATUS) != 0) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Manage users failed. Unable to read user address details. username: " + username);
                ErrorCodeEnum.ERR_21452.setErrorCode(processedResult);
                return processedResult;
            }

            String homeAddressID = null;
            JSONArray Addresses = ((JSONArray) getInternalUserHomeAddr.get("useraddress"));
            for (int i = 0; i < Addresses.length(); i++) {
                if (((JSONObject) Addresses.get(i)).get("Type_id").toString().equalsIgnoreCase("ADR_TYPE_HOME")) {
                    homeAddressID = ((JSONObject) Addresses.get(i)).get("Address_id").toString();
                }
            }

            if (cityID != null) {
                if (homeAddressID != null) {
                    // Update home address
                    JSONObject updateHomeAddress = updateHomeAddress(authToken, modifiedByName, username, homeAddressID,
                            addr1, addr2, cityID, stateID, countryID, zipcode, requestInstance);
                    if (updateHomeAddress == null || !updateHomeAddress.has(FabricConstants.OPSTATUS)
                            || updateHomeAddress.getInt(FabricConstants.OPSTATUS) != 0) {
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.UPDATE,
                                ActivityStatusEnum.FAILED,
                                "Manage users failed. Unable to update user home address. username: " + username);
                        ErrorCodeEnum.ERR_21452.setErrorCode(processedResult);
                        return processedResult;
                    }
                } else {
                    // Create home address
                    JSONObject createHomeAddress = createHomeAddress(authToken, modifiedByName, username, userID, addr1,
                            addr2, cityID, stateID, countryID, zipcode, requestInstance);
                    if (createHomeAddress == null || !createHomeAddress.has(FabricConstants.OPSTATUS)
                            || createHomeAddress.getInt(FabricConstants.OPSTATUS) != 0) {
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.UPDATE,
                                ActivityStatusEnum.FAILED,
                                "Manage users failed. Unable to create user home address. username: " + username);
                        ErrorCodeEnum.ERR_21453.setErrorCode(processedResult);
                        return processedResult;
                    }
                }

            }

            if (branchID != null) {
                JSONObject getBranchAddr = getBranchAddr(authToken, branchID, requestInstance);
                if (getBranchAddr == null || !getBranchAddr.has(FabricConstants.OPSTATUS)
                        || getBranchAddr.getInt(FabricConstants.OPSTATUS) != 0) {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.UPDATE,
                            ActivityStatusEnum.FAILED,
                            "Manage users failed. Unable to read user branch address. username: " + username);
                    ErrorCodeEnum.ERR_21454.setErrorCode(processedResult);
                    return processedResult;
                }
                JSONObject location = ((JSONObject) ((JSONArray) getBranchAddr.get("location")).get(0));
                if (!(location.getString("Address_id").equalsIgnoreCase(UserDetails.getString("Work_AddressID")))) {
                    changedFields += "<br><b>Branch changed to</b> '" + branchName + "'";
                    JSONObject updateWorkAddress = updateWorkAddress(authToken, modifiedByName, userID, username,
                            location.getString("Address_id"), requestInstance);
                    if (updateWorkAddress == null || !updateWorkAddress.has(FabricConstants.OPSTATUS)
                            || updateWorkAddress.getInt(FabricConstants.OPSTATUS) != 0) {
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.UPDATE,
                                ActivityStatusEnum.FAILED,
                                "Manage users failed. Unable to update user branch address. username: " + username);
                        ErrorCodeEnum.ERR_21455.setErrorCode(processedResult);
                        return processedResult;
                    }
                }

            }

            // For userrole table

            if (roleID != null) {
                JSONObject getRecordsForUserID = getRecordsForUserIDFromUserRole(authToken, userID, requestInstance);
                if (getRecordsForUserID == null || !getRecordsForUserID.has(FabricConstants.OPSTATUS)
                        || getRecordsForUserID.getInt(FabricConstants.OPSTATUS) != 0) {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.UPDATE,
                            ActivityStatusEnum.FAILED,
                            "Manage users failed. Unable to read user role details. username: " + username);
                    ErrorCodeEnum.ERR_21456.setErrorCode(processedResult);
                    return processedResult;
                }

                if (((JSONArray) getRecordsForUserID.get("userrole")).length() > 0) {
                    JSONObject deleteResJSON = deleteUserRoles(authToken, userID, username,
                            ((JSONObject) ((JSONArray) getRecordsForUserID.get("userrole")).get(0))
                                    .getString("Role_id"),
                            requestInstance);
                    if (deleteResJSON == null || !deleteResJSON.has(FabricConstants.OPSTATUS)
                            || deleteResJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.UPDATE,
                                ActivityStatusEnum.FAILED,
                                "Manage users failed. Unable to delete user role details. username: " + username);
                        ErrorCodeEnum.ERR_21457.setErrorCode(processedResult);
                        return processedResult;
                    }
                }

                if (!roleID.equals("")) {
                    JSONObject createResJSON = createUserRole(authToken, modifiedByName, userID, username, roleID,
                            requestInstance);
                    if (createResJSON == null || !createResJSON.has(FabricConstants.OPSTATUS)
                            || createResJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.UPDATE,
                                ActivityStatusEnum.FAILED,
                                "Manage users failed. Unable to create user role. username: " + username);
                        ErrorCodeEnum.ERR_21458.setErrorCode(processedResult);
                        return processedResult;
                    }
                }
            }
            Set<String> listOfPermissions = new HashSet<String>();
            if (listOfRemovedPermissions != null) {
                for (int indexVar = 0; indexVar < listOfRemovedPermissions.length(); indexVar++) {
                    listOfPermissions.add(listOfRemovedPermissions.optString(indexVar));
                }
            }
            if (listOfAddedPermissions != null) {
                for (int indexVar = 0; indexVar < listOfAddedPermissions.length(); indexVar++) {
                    listOfPermissions.add(listOfAddedPermissions.optString(indexVar));
                }
            }

            // Get the Composite Permission Information for all the permissions listed in
            // the added/removed list
            HashMap<String, ArrayList<Permission>> compositePermissionMapping = PermissionHandler
                    .getChildPermissions(listOfPermissions, requestInstance);

            // Remove user permissions
            if (listOfRemovedPermissions != null && listOfRemovedPermissions.length() >= 1) {
                deletePermissions(authToken, listOfRemovedPermissions, userID, username, requestInstance,
                        compositePermissionMapping);
            }

            // Insert user permissions
            if (listOfAddedPermissions != null && listOfAddedPermissions.length() >= 1) {
                createPermissions(authToken, modifiedByName, listOfAddedPermissions, userID, username, requestInstance,
                        compositePermissionMapping);
            }

            // Audit action
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL, "Username:" + username);

            // Send status change email
            String subject = "Account Edited";
            String recipientEmailId = UserDetails.getString("Email");
            String emailType = "InternalUserEdit";
            JSONObject AdditionalContext = new JSONObject();
            AdditionalContext.put("name", firstName);
            AdditionalContext.put("username", username);
            AdditionalContext.put("InternalUser_id", userID);
            AdditionalContext.put("ChangedFields", changedFields);
            JSONObject eamilres = EmailHandler.invokeSendEmailObjectService(requestInstance, authToken,
                    recipientEmailId, null, subject, emailType, AdditionalContext);
            JSONObject eamilResponseForNewEmail = null;
            if ((email != null) && (!UserDetails.getString("Email").equals(email))) {
                eamilResponseForNewEmail = EmailHandler.invokeSendEmailObjectService(requestInstance, authToken, email,
                        null, subject, emailType, AdditionalContext);
            }
            Param EmailStatus;
            if (eamilres.getInt(FabricConstants.OPSTATUS) != 0) {
                EmailStatus = new Param("EmailStatus", eamilres.toString(), FabricConstants.STRING);
            } else {
                EmailStatus = new Param("EmailStatus", "Success", FabricConstants.STRING);
            }

            if (eamilResponseForNewEmail != null) {
                if (eamilres.getInt(FabricConstants.OPSTATUS) != 0) {
                    processedResult.addParam(new Param("EmailStatusForNewEmail", eamilResponseForNewEmail.toString(),
                            FabricConstants.STRING));
                } else {
                    processedResult.addParam(new Param("EmailStatusForNewEmail", "Success", FabricConstants.STRING));
                }
            }
            Param statusParam = new Param("Status", "Edited successfully", FabricConstants.STRING);
            processedResult.addParam(EmailStatus);
            processedResult.addParam(statusParam);
            return processedResult;

        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception exception) {
            LOG.error("Exception", exception);
            processedResult.addParam(new Param("Status", "Edit Failed", FabricConstants.STRING));
            processedResult.addParam(new Param("Exception Message", exception.getMessage(), FabricConstants.STRING));
            return processedResult;
        }
    }

    public String changedItems(JSONObject UserDetails, String firstName, String middleName, String LastName,
            String email, String username, String addr1, String addr2, String cityID, String stateID, String countryID,
            String cityName, String stateName, String countryName, String zipcode, String roleID, String roleName,
            JSONArray listOfAddedPermissions, JSONArray listOfRemovedPermissions, JSONArray listOfAddedPermissionsNames,
            JSONArray listOfRemovedPermissionsNames) {

        String changedFields = "<br><b>Changed Fields</b><br>";
        String check = changedFields;

        String previousFirstName = UserDetails.has("FirstName") ? UserDetails.getString("FirstName") : "";
        if (isChanged(previousFirstName, firstName))
            changedFields += "<br><b> First name:</b> '" + previousFirstName + "'  <b>CHANGED TO</b>  '" + firstName
                    + "'";

        String previousMiddleName = UserDetails.has("MiddleName") ? UserDetails.getString("MiddleName") : "";
        if (isChanged(previousMiddleName, middleName))
            changedFields += "<br><b> Middle name:</b> '" + previousMiddleName + "'  <b>CHANGED TO</b>  '" + middleName
                    + "'";

        String previousLastName = UserDetails.has("LastName") ? UserDetails.getString("LastName") : "";
        if (isChanged(previousLastName, LastName))
            changedFields += "<br><b>Last name:</b> '" + previousLastName + "' <b>CHANGED TO</b>  '" + LastName + "'";

        String previousEmail = UserDetails.has("Email") ? UserDetails.getString("Email") : "";
        if (isChanged(previousEmail, email))
            changedFields += "<br><b>Email:</b> " + previousEmail + "  <b>CHANGED TO</b>  '" + email + "'";

        String previousUserName = UserDetails.has("Username") ? UserDetails.getString("Username") : "";
        if (isChanged(previousUserName, username))
            changedFields += "<br><b>Username:</b> " + previousUserName + "  <b>CHANGED TO</b>  '" + username + "'";

        String previousAddrLine1 = UserDetails.has("Home_AddressLine1") ? UserDetails.getString("Home_AddressLine1")
                : "";
        if (isChanged(previousAddrLine1, addr1))
            changedFields += "<br><b>Home address line1:</b> '" + previousAddrLine1 + "'  <b>CHANGED TO</b>  '" + addr1
                    + "'";

        String previousAddrLine2 = UserDetails.has("Home_AddressLine2") ? UserDetails.getString("Home_AddressLine2")
                : "";
        if (isChanged(previousAddrLine2, addr2))
            changedFields += "<br><b>Home address line2:</b> '" + previousAddrLine2 + "' <b>CHANGED TO</b>  '" + addr2
                    + "'";

        if (UserDetails.has("Home_CityID") && isChanged(UserDetails.getString("Home_CityID"), cityID))
            changedFields += "<br><b>Home City:</b> " + UserDetails.getString("Home_CityName")
                    + "  <b>CHANGED TO</b>  '" + cityName + "'";

        if (UserDetails.has("Home_StateID") && isChanged(UserDetails.getString("Home_StateID"), stateID))
            changedFields += "<br><b>Home State:</b> " + UserDetails.getString("Home_StateName")
                    + " <b>CHANGED TO</b>  '" + stateName + "'";

        if (UserDetails.has("Home_CountryID") && isChanged(UserDetails.getString("Home_CountryID"), countryID))
            changedFields += "<br><b>Home Country:</b> " + UserDetails.getString("Home_CountryName")
                    + " <b>CHANGED TO</b>  '" + countryName + "'";

        String previousZipcode = UserDetails.has("Home_Zipcode") ? UserDetails.getString("Home_Zipcode") : "";
        if (isChanged(previousZipcode, zipcode))
            changedFields += "<br><b>Home zipcode:</b> '" + previousZipcode + "'  <b>CHANGED TO</b>  '" + zipcode + "'";

        String previousRoleID = UserDetails.has("Role_id") ? UserDetails.getString("Role_id") : "";
        String previousRoleName = UserDetails.has("Role_Name") ? UserDetails.getString("Role_Name") : "";
        if (isChanged(previousRoleID, roleID)) {
            if (roleID.equals("")) {
                changedFields += "<br><b>Role:</b> '" + previousRoleName + "' <b>REMOVED</b>";
            } else {
                changedFields += "<br><b>Role:</b> '" + previousRoleName + "'  <b>CHANGED TO</b>  '" + roleName + "'";
            }

        }

        if (listOfAddedPermissions != null && listOfAddedPermissions.length() > 0) {
            changedFields += "<br><b>Added Permissions:</b> " + listOfAddedPermissionsNames.join(", ");
        }

        if (listOfRemovedPermissions != null && listOfRemovedPermissions.length() > 0) {
            changedFields += "<br><b>Removed Permissions:</b> " + listOfRemovedPermissionsNames.join(", ");
        }

        if (changedFields.equals(check))
            return "<br><b>Nothing changed</b><br>";

        return changedFields + "<br>";
    }

    public boolean isChanged(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return false;
        } else if (str1.equals(str2)) {
            return false;
        }
        return true;
    }

    public JSONObject readInternalUser(String AuthToken, String UserID, DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "User_id eq '" + UserID + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.INTERNALUSERS_VIEW_READ, postParametersMap,
                null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public void deletePermissions(String authToken, JSONArray listOfRemovedPermissions, String userID, String username,
            DataControllerRequest requestInstance, HashMap<String, ArrayList<Permission>> compositePermissionMapping) {
        ArrayList<Permission> currentChildPermissions;
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("User_id", userID);
        for (int indexVar = 0; indexVar < listOfRemovedPermissions.length(); indexVar++) {
            String permissionID = listOfRemovedPermissions.getString(indexVar);
            postParametersMap.put("Permission_id", permissionID);

            // If the current permission is composite, then the corresponding child
            // permissions are to be removed
            if (compositePermissionMapping.containsKey(permissionID)) {
                currentChildPermissions = compositePermissionMapping.get(permissionID);
                for (Permission currPermission : currentChildPermissions) {
                    postParametersMap.put("CompositePermission_id", currPermission.getId());
                    Executor.invokeService(ServiceURLEnum.ROLECOMPOSITEPERMISSION_DELETE, postParametersMap, null,
                            requestInstance);
                }
            }
            postParametersMap.remove("CompositePermission_id");

            Executor.invokeService(ServiceURLEnum.USERPERMISSION_DELETE, postParametersMap, null, requestInstance);
        }
    }

    public void createPermissions(String authToken, String modifiedByName, JSONArray listOfAddedPermissions,
            String userID, String username, DataControllerRequest requestInstance,
            HashMap<String, ArrayList<Permission>> compositePermissionMapping) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        ArrayList<Permission> currentChildPermissions;
        postParametersMap.put("User_id", userID);
        postParametersMap.put("createdby", modifiedByName);
        postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
        postParametersMap.put("modifiedby", modifiedByName);
        for (int indexVar = 0; indexVar < listOfAddedPermissions.length(); indexVar++) {
            String permissionID = listOfAddedPermissions.getString(indexVar);

            // If the current permission is composite, then the corresponding child
            // permissions are to be added
            if (compositePermissionMapping.containsKey(permissionID)) {
                currentChildPermissions = compositePermissionMapping.get(permissionID);
                for (Permission currPermission : currentChildPermissions) {
                    postParametersMap.put("CompositePermission_id", currPermission.getId());
                    if (currPermission.isEnabled()) {
                        postParametersMap.put("isEnabled", "1");
                    } else {
                        postParametersMap.put("isEnabled", "0");
                    }
                    Executor.invokeService(ServiceURLEnum.USERCOMPOSITEPERMISSION_CREATE, postParametersMap, null,
                            requestInstance);
                }
            }
            postParametersMap.remove("CompositePermission_id");

            postParametersMap.put("Permission_id", permissionID);
            Executor.invokeService(ServiceURLEnum.USERPERMISSION_CREATE, postParametersMap, null, requestInstance);
        }
    }

    public JSONObject getRecordsForUserIDFromUserRole(String AuthToken, String UserID,
            DataControllerRequest requestInstance) {
        Map<String, String> headerParametersMap = new HashMap<String, String>();

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "User_id eq '" + UserID + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.USERROLE_READ, postParametersMap,
                headerParametersMap, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public JSONObject getAllRecordsForUserID(String AuthToken, String UserID, DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "User_id eq '" + UserID + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.USERPERMISSION_READ, postParametersMap,
                null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public JSONObject createUserRole(String authToken, String modifiedByName, String userID, String username,
            String roleID, DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("User_id", userID);
        postParametersMap.put("Role_id", roleID);
        if (roleID.equalsIgnoreCase("RID_SUPERADMIN")) {
            postParametersMap.put("hasSuperAdminPrivilages", "1");
        } else {
            postParametersMap.put("hasSuperAdminPrivilages", "0");
        }

        postParametersMap.put("createdby", modifiedByName);
        postParametersMap.put("modifiedby", modifiedByName);
        postParametersMap.put("lastmodifedts", CommonUtilities.getISOFormattedLocalTimestamp());
        postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
        postParametersMap.put("softdeleteflag", "0");
        String createResponse = Executor.invokeService(ServiceURLEnum.USERROLE_CREATE, postParametersMap, null,
                requestInstance);
        JSONObject createResponseJSON = CommonUtilities.getStringAsJSONObject(createResponse);
        return createResponseJSON;

    }

    public JSONObject deleteUserRoles(String authToken, String userID, String username, String roleID,
            DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("Role_id", roleID);
        postParametersMap.put("User_id", userID);
        String deleteResponse = Executor.invokeService(ServiceURLEnum.USERROLE_DELETE, postParametersMap, null,
                requestInstance);
        JSONObject deleteResponseJSON = CommonUtilities.getStringAsJSONObject(deleteResponse);
        return deleteResponseJSON;
    }

    public JSONObject updateHomeAddress(String authToken, String modifiedByName, String username, String addressID,
            String addr1, String addr2, String cityID, String stateID, String countryID, String zipcode,
            DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("id", addressID);
        postParametersMap.put("addressLine1", addr1);
        postParametersMap.put("addressLine2", addr2);
        postParametersMap.put("Region_id", stateID);
        postParametersMap.put("City_id", cityID);
        postParametersMap.put("zipCode", zipcode);
        postParametersMap.put("modifiedby", modifiedByName);
        postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

        String updateEndpointResponse = Executor.invokeService(ServiceURLEnum.ADDRESS_UPDATE, postParametersMap, null,
                requestInstance);
        return CommonUtilities.getStringAsJSONObject(updateEndpointResponse);

    }

    public JSONObject createHomeAddress(String AuthToken, String modifiedByName, String username, String userID,
            String addr1, String addr2, String cityID, String stateID, String countryID, String zipcode,
            DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        String id = CommonUtilities.getNewId().toString();
        postParametersMap.clear();
        postParametersMap.put("id", id.toString());
        postParametersMap.put("City_id", cityID);
        postParametersMap.put("Region_id", stateID);
        postParametersMap.put("addressLine1", addr1);
        postParametersMap.put("addressLine2", addr2);
        postParametersMap.put("zipCode", zipcode);
        postParametersMap.put("createdby", modifiedByName);
        postParametersMap.put("modifiedby", modifiedByName);
        postParametersMap.put("softdeleteflag", SOFT_DELETE_FLAG);

        Executor.invokeService(ServiceURLEnum.ADDRESS_CREATE, postParametersMap, null, requestInstance);
        // create user home address
        postParametersMap.clear();
        postParametersMap.put("User_id", userID);
        postParametersMap.put("Address_id", id);
        postParametersMap.put("Type_id", "ADR_TYPE_HOME");
        postParametersMap.put("createdby", modifiedByName);
        postParametersMap.put("modifiedby", modifiedByName);
        postParametersMap.put("softdeleteflag", SOFT_DELETE_FLAG);
        String createEndpointResponse = Executor.invokeService(ServiceURLEnum.USERADDRESS_CREATE, postParametersMap,
                null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(createEndpointResponse);

    }

    public JSONObject updateWorkAddress(String AuthToken, String modifiedByName, String userID, String username,
            String addressID, DataControllerRequest requestInstance) {

        // read user address for the work address entry
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "User_id eq '" + userID + "' and Type_id eq 'ADR_TYPE_WORK'");
        String readUserAddress = Executor.invokeService(ServiceURLEnum.USERADDRESS_READ, postParametersMap, null,
                requestInstance);
        JSONObject userAddress = CommonUtilities.getStringAsJSONObject(readUserAddress);

        if (((JSONArray) userAddress.get("useraddress")).length() > 0) {
            // Remove user address entry
            postParametersMap.clear();
            postParametersMap.put("User_id", userID);
            postParametersMap.put("Address_id",
                    ((JSONObject) ((JSONArray) userAddress.get("useraddress")).get(0)).getString("Address_id"));
            postParametersMap.put("Type_id", "ADR_TYPE_WORK");
            Executor.invokeService(ServiceURLEnum.USERADDRESS_DELETE, postParametersMap, null, requestInstance);
        }
        // Add user address entry
        postParametersMap.clear();
        postParametersMap.put("User_id", userID);
        postParametersMap.put("Address_id", addressID);
        postParametersMap.put("Type_id", "ADR_TYPE_WORK");
        postParametersMap.put("createdby", modifiedByName);
        postParametersMap.put("modifiedby", modifiedByName);
        postParametersMap.put("softdeleteflag", SOFT_DELETE_FLAG);
        String createEndpointResponse = Executor.invokeService(ServiceURLEnum.USERADDRESS_CREATE, postParametersMap,
                null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(createEndpointResponse);

    }

    public JSONObject updateInternalUser(String AuthToken, String modifiedByName, String userID, String firstName,
            String middleName, String LastName, String email, String username, DataControllerRequest requestInstance) {
        Map<String, String> headerParametersMap = new HashMap<String, String>();

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("id", userID);

        if (StringUtils.isNotBlank(firstName))
            postParametersMap.put("FirstName", firstName);

        if (StringUtils.isNotBlank(middleName))
            postParametersMap.put("MiddleName", middleName);

        if (StringUtils.isNotBlank(LastName))
            postParametersMap.put("LastName", LastName);

        if (StringUtils.isNotBlank(email))
            postParametersMap.put("Email", email);

        if (StringUtils.isNotBlank(username))
            postParametersMap.put("Username", username);

        if (StringUtils.isNotBlank(modifiedByName))
            postParametersMap.put("modifiedby", modifiedByName);

        if (postParametersMap.size() > 1) {
            postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

            String updateEndpointResponse = Executor.invokeService(ServiceURLEnum.SYSTEMUSER_UPDATE, postParametersMap,
                    headerParametersMap, requestInstance);
            return CommonUtilities.getStringAsJSONObject(updateEndpointResponse);
        }

        return null;
    }

    public JSONObject getBranchAddr(String AuthToken, String branchID, DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "id eq '" + branchID + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.LOCATION_READ, postParametersMap, null,
                requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public JSONObject getInternalUserAddresses(String AuthToken, String userID, DataControllerRequest requestInstance) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "User_id eq '" + userID + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.USERADDRESS_READ, postParametersMap, null,
                requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

}