package com.kony.adminconsole.service.usermanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.dto.AddressBean;
import com.kony.adminconsole.dto.SystemUserBean;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.EmailHandler;
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

public class CreateInternalUserService implements JavaService2 {

    private static final String SOFT_DELETE_FLAG = "0";

    private Param createInternalUser(SystemUserBean sysUserBean, String claimsToken,
            DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        String id = CommonUtilities.getNewId().toString();
        postParametersMap.put("id", id.toString());
        postParametersMap.put("Status_id", sysUserBean.getStatus_id());
        postParametersMap.put("Username", sysUserBean.getUsername());
        postParametersMap.put("Password", sysUserBean.getUsername());
        postParametersMap.put("Email", sysUserBean.getEmail());
        postParametersMap.put("FirstName", sysUserBean.getFirstName());
        postParametersMap.put("LastName", sysUserBean.getLastName());
        postParametersMap.put("MiddleName", sysUserBean.getMiddleName());
        postParametersMap.put("createdby", sysUserBean.getCurrUser());
        postParametersMap.put("modifiedby", sysUserBean.getCurrUser());
        postParametersMap.put("FailedCount", SOFT_DELETE_FLAG);

        Param userUUIDParam;

        String createUserResponse = Executor.invokeService(ServiceURLEnum.SYSTEMUSER_CREATE, postParametersMap, null,
                requestInstance);

        JSONObject userCreationResponseJSON = CommonUtilities.getStringAsJSONObject(createUserResponse);
        int getOpStatusCode = userCreationResponseJSON.getInt(FabricConstants.OPSTATUS);
        if (getOpStatusCode == 0) {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.CREATE,
                    ActivityStatusEnum.SUCCESSFUL, "User created successfully. Username:" + sysUserBean.getUsername());
            userUUIDParam = new Param("UUID", id, FabricConstants.STRING);
        } else {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "User creation failed. Username:" + sysUserBean.getUsername());
            userUUIDParam = new Param("UUID", "ERROR", FabricConstants.STRING);
        }
        return userUUIDParam;

    }

    private Param createAddress(AddressBean addBean, SystemUserBean sysUserBean, String claimsToken,
            DataControllerRequest requestInstance) {
        Param addressID;
        Map<String, String> postParametersMap = new HashMap<String, String>();
        String id = CommonUtilities.getNewId().toString();
        postParametersMap.clear();
        postParametersMap.put("id", id.toString());
        postParametersMap.put("City_id", addBean.getCity_id());
        postParametersMap.put("Region_id", addBean.getRegion_id());
        postParametersMap.put("addressLine1", addBean.getAddressLine1());
        postParametersMap.put("addressLine2", addBean.getAddressLine2());
        postParametersMap.put("addressLine3", addBean.getAddressLine3());
        postParametersMap.put("zipCode", addBean.getZipCode());
        postParametersMap.put("createdby", sysUserBean.getCurrUser());
        postParametersMap.put("modifiedby", sysUserBean.getCurrUser());
        postParametersMap.put("softdeleteflag", SOFT_DELETE_FLAG);

        String createAddressResponse = Executor.invokeService(ServiceURLEnum.ADDRESS_CREATE, postParametersMap, null,
                requestInstance);

        JSONObject addressCreationResponseJSON = CommonUtilities.getStringAsJSONObject(createAddressResponse);
        int getOpStatusCode = addressCreationResponseJSON.getInt(FabricConstants.OPSTATUS);
        if (getOpStatusCode == 0) {
            addressID = new Param("UUID", id, FabricConstants.STRING);
        } else {
            addressID = new Param("UUID", "ERROR", FabricConstants.STRING);
        }
        return addressID;

    }

    private Param createUserAddress(String UserID, String AddID, String AddType, SystemUserBean sysUserBean,
            String claimsToken, DataControllerRequest requestInstance) {
        Param statusParam;
        Map<String, String> postParametersMap = new HashMap<String, String>();
        String id = CommonUtilities.getNewId().toString();
        postParametersMap.clear();
        postParametersMap.put("id", id.toString());
        postParametersMap.put("User_id", UserID);
        postParametersMap.put("Address_id", AddID);
        postParametersMap.put("Type_id", AddType);
        postParametersMap.put("createdby", sysUserBean.getCurrUser());
        postParametersMap.put("modifiedby", sysUserBean.getCurrUser());

        String createUserAddressResponse = Executor.invokeService(ServiceURLEnum.USERADDRESS_CREATE, postParametersMap,
                null, requestInstance);

        JSONObject userAddressCreationResponseJSON = CommonUtilities.getStringAsJSONObject(createUserAddressResponse);
        int getOpStatusCode = userAddressCreationResponseJSON.getInt(FabricConstants.OPSTATUS);
        if (getOpStatusCode == 0) {
            statusParam = new Param("Status", "Success", FabricConstants.STRING);
        } else {
            statusParam = new Param("Status", "Error", FabricConstants.STRING);
        }
        return statusParam;
    }

    private Param assignroleToUser(String UserID, String roleId, SystemUserBean sysUserBean, String claimsToken,
            DataControllerRequest requestInstance) {
        Param statusParam;
        Map<String, String> postParametersMap = new HashMap<String, String>();
        String id = CommonUtilities.getNewId().toString();
        postParametersMap.clear();
        postParametersMap.put("id", id.toString());
        postParametersMap.put("User_id", UserID);
        postParametersMap.put("Role_id", roleId);
        postParametersMap.put("hasSuperAdminPrivilages", SOFT_DELETE_FLAG);
        postParametersMap.put("createdby", sysUserBean.getCurrUser());
        postParametersMap.put("modifiedby", sysUserBean.getCurrUser());

        String createUserRoleResponse = Executor.invokeService(ServiceURLEnum.USERROLE_CREATE, postParametersMap, null,
                requestInstance);

        JSONObject userAddressRoleResponseJSON = CommonUtilities.getStringAsJSONObject(createUserRoleResponse);
        int getOpStatusCode = userAddressRoleResponseJSON.getInt(FabricConstants.OPSTATUS);
        if (getOpStatusCode == 0) {
            statusParam = new Param("Status", "Success", FabricConstants.STRING);
        } else {
            statusParam = new Param("Status", "Error", FabricConstants.STRING);
        }
        return statusParam;
    }

    private Param assignPermissionsToUser(JSONArray permission_ids, String claimsToken, SystemUserBean sysUserBean,
            String User_id, DataControllerRequest requestInstance) {
        Param statusParam = new Param("Status", "Success", FabricConstants.STRING);
        permission_ids.get(0);
        Map<String, String> postParametersMap = new HashMap<String, String>();
        for (int i = 0; i < permission_ids.length(); i++) {
            postParametersMap.clear();
            String id = CommonUtilities.getNewId().toString();
            String Permission_id = permission_ids.getString(i).toString();
            postParametersMap.put("id", id.toString());
            postParametersMap.put("Permission_id", Permission_id);
            postParametersMap.put("User_id", User_id.toString());
            postParametersMap.put("createdby", sysUserBean.getCurrUser());
            postParametersMap.put("modifiedby", sysUserBean.getCurrUser());

            String createUserPermissionResponse = Executor.invokeService(ServiceURLEnum.USERPERMISSION_CREATE,
                    postParametersMap, null, requestInstance);
            JSONObject userPermisionJSON = CommonUtilities.getStringAsJSONObject(createUserPermissionResponse);
            int getOpStatusCode = userPermisionJSON.getInt(FabricConstants.OPSTATUS);

            if (getOpStatusCode == 0) {
                statusParam = new Param("UserPermissionStatus", "Success", FabricConstants.STRING);
            } else {
                statusParam = new Param("UserPermissionStatus", "Error", FabricConstants.STRING);
                return statusParam;
            }
        }
        return statusParam;
    }

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        Param statusParam;
        SystemUserBean sysUserBean = new SystemUserBean();
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> input = (HashMap<String, String>) inputArray[1];
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            sysUserBean.setStatus_id(String.valueOf(input.get("Status_id")));
            sysUserBean.setUsername(String.valueOf(input.get("Username")));
            sysUserBean.setEmail(String.valueOf(input.get("Email")));
            sysUserBean.setFirstName(String.valueOf(input.get("FirstName")));
            if (StringUtils.isNotBlank(input.get("MiddleName")))
                sysUserBean.setMiddleName(input.get("MiddleName").toString());
            sysUserBean.setLastName(String.valueOf(input.get("LastName")));
            sysUserBean.setCurrUser(String.valueOf(input.get("currUser")));

            // ** Checking if username or email exists in backend **
            Map<String, String> systemuserTableMap = new HashMap<String, String>();
            systemuserTableMap.put(ODataQueryConstants.SELECT, "UserID");
            systemuserTableMap.put(ODataQueryConstants.FILTER,
                    "Username eq '" + sysUserBean.getUsername() + "' or Email eq '" + sysUserBean.getEmail() + "'");

            String readSystemUserResponse = Executor.invokeService(ServiceURLEnum.SYSTEMUSER_VIEW_READ,
                    systemuserTableMap, null, requestInstance);
            JSONObject readSystemUserResponseJSON = CommonUtilities.getStringAsJSONObject(readSystemUserResponse);

            if (readSystemUserResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readSystemUserResponseJSON.getJSONArray("systemuser_view").length() == 0) {

                Param userResponse = createInternalUser(sysUserBean, authToken, requestInstance);
                JSONArray newPermissions = new JSONArray(requestInstance.getParameter("permission_ids"));
                if (!StringUtils.equals(userResponse.getValue(), "ERROR")) {
                    Param userStatusParam = new Param("UserStatus", "Success", FabricConstants.STRING);
                    result.addParam(userStatusParam);
                    AddressBean addBean = new AddressBean();
                    addBean.setCity_id(String.valueOf(input.get("City_id")));
                    addBean.setAddressLine1(String.valueOf(input.get("AddressLine1")));
                    if (StringUtils.isNotBlank(input.get("AddressLine2")))
                        addBean.setAddressLine2(input.get("AddressLine2").toString());
                    addBean.setRegion_id(input.get("Region_id"));
                    addBean.setZipCode(input.get("ZipCode"));
                    Param addressResponse = createAddress(addBean, sysUserBean, authToken, requestInstance);
                    if (!StringUtils.equals(addressResponse.getValue(), "ERROR")) {
                        Param addStatusParam = new Param("AddressStatus", "Success", FabricConstants.STRING);
                        result.addParam(addStatusParam);
                        createUserAddress(userResponse.getValue(), addressResponse.getValue(), "ADR_TYPE_HOME",
                                sysUserBean, authToken, requestInstance);
                        JSONObject getBranchAddr = getBranchAddr(authToken, String.valueOf(input.get("WorkID")),
                                requestInstance);
                        String WorkID = ((JSONObject) ((JSONArray) getBranchAddr.get("location")).get(0))
                                .getString("Address_id");
                        createUserAddress(userResponse.getValue(), WorkID, "ADR_TYPE_WORK", sysUserBean, authToken,
                                requestInstance);
                    } else {
                        Param addStatusParam = new Param("AddressStatus", "Error While Creation",
                                FabricConstants.STRING);
                        result.addParam(addStatusParam);
                    }
                    if (StringUtils.isNotBlank(input.get("role_id"))) {
                        Param status = assignroleToUser(userResponse.getValue(), input.get("role_id").toString(),
                                sysUserBean, authToken, requestInstance);
                        if (StringUtils.equals(status.getValue(), "Success")) {
                            Param roleStatusParam = new Param("RoleStatus", "Success", FabricConstants.STRING);
                            result.addParam(roleStatusParam);
                        }
                    }
                    if (newPermissions.length() != 0) {
                        assignPermissionsToUser(newPermissions, authToken, sysUserBean, userResponse.getValue(),
                                requestInstance);
                    }

                    // Email service
                    String subject = "User Creation successful";
                    String recipientEmailId = sysUserBean.getEmail();
                    String emailType = "createUser";
                    JSONObject context = new JSONObject();
                    context.put("vizServerURL", requestInstance.getParameter("vizServerURL"));

                    JSONObject eamilres = EmailHandler.invokeSendEmailObjectService(requestInstance, authToken,
                            recipientEmailId, null, subject, emailType, context);

                    Param EmailStatus;
                    if (eamilres.getInt(FabricConstants.OPSTATUS) != 0) {
                        EmailStatus = new Param("EmailStatus", "Error While Sending", FabricConstants.STRING);
                    } else {
                        EmailStatus = new Param("EmailStatus", "Success", FabricConstants.STRING);
                    }

                    result.addParam(EmailStatus);
                } else {
                    Param userStatusParam = new Param("UserStatus", "Error While Creation", FabricConstants.STRING);
                    result.addParam(userStatusParam);
                }
            } else {
                Param userStatusParam = new Param("UserStatus", "Error While Creation", FabricConstants.STRING);
                result.addParam(userStatusParam);
            }
        } catch (Exception e) {
            statusParam = new Param("Status", "Error", FabricConstants.STRING);
            result.addParam(statusParam);
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "User creation failed. Username:" + sysUserBean.getUsername());
        }
        return result;
    }

    public JSONObject getBranchAddr(String AuthToken, String branchID, DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "id eq '" + branchID + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.LOCATION_READ, postParametersMap, null,
                requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
    }
}