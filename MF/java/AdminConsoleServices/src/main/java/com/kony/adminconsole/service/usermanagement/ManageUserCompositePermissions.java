/**
 * 
 */
package com.kony.adminconsole.service.usermanagement;

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
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * @author Aditya Mankal
 * 
 * 
 *         Service to manage the added/removed Composite permissions of a User
 *
 */
public class ManageUserCompositePermissions implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(ManageUserCompositePermissions.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();
            ErrorCodeEnum errorInformation = null;

            String userId = requestInstance.getParameter("userId");
            String addedCompositePermissions = requestInstance.getParameter("addedCompositePermissions");
            String removedCompositePermissions = requestInstance.getParameter("removedCompositePermissions");
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);

            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);

            if (StringUtils.isEmpty(userId)) {
                errorInformation = ErrorCodeEnum.ERR_20522;
            } else {
                JSONArray listOfAddedCompositePermissions = CommonUtilities
                        .getStringAsJSONArray(addedCompositePermissions);
                JSONArray listOfRemovedCompositePermissions = CommonUtilities
                        .getStringAsJSONArray(removedCompositePermissions);

                Record initializeUserPermissionMappingOperationRecord = initUserCompositePermissionMapping(userId,
                        listOfAddedCompositePermissions, listOfRemovedCompositePermissions, userDetailsBeanInstance,
                        authToken, requestInstance);
                processedResult.addRecord(initializeUserPermissionMappingOperationRecord);

                Record manageAddedCompositePermissionsOperationRecord = processCompositePermissions(userId,
                        listOfAddedCompositePermissions, userDetailsBeanInstance, true, authToken, requestInstance);
                if (manageAddedCompositePermissionsOperationRecord != null) {
                    if (manageAddedCompositePermissionsOperationRecord.getParamByName("status") != null
                            && manageAddedCompositePermissionsOperationRecord.getParamByName("status").getValue()
                                    .equalsIgnoreCase("failure")) {
                        errorInformation = ErrorCodeEnum.ERR_20526;
                    }
                }

                Record manageRemovedCompositePermissionsOperationRecord = processCompositePermissions(userId,
                        listOfRemovedCompositePermissions, userDetailsBeanInstance, false, authToken, requestInstance);
                if (manageRemovedCompositePermissionsOperationRecord != null) {
                    if (manageRemovedCompositePermissionsOperationRecord.getParamByName("status") != null
                            && manageRemovedCompositePermissionsOperationRecord.getParamByName("status").getValue()
                                    .equalsIgnoreCase("failure")) {
                        errorInformation = ErrorCodeEnum.ERR_20526;
                    }
                }
            }
            if (errorInformation != null) {
                errorInformation.setErrorCode(processedResult);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "Composite Permissions update failed for the user:" + userId);
                return processedResult;
            }
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL, "Composite Permissions updated Successfully for the user:" + userId);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Composite Permissions update failed for the user");
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    private Record processCompositePermissions(String userId, JSONArray compositePermissionsList,
            UserDetailsBean userDetailsBeanInstance, boolean isAddedPermissionsOperation, String authToken,
            DataControllerRequest requestInstance) {

        if (compositePermissionsList == null || compositePermissionsList.length() == 0) {
            return null;
        }

        Record addCompositePermissionsResponse = new Record();
        Param operationStatus = new Param("status", "Successful", FabricConstants.STRING);
        addCompositePermissionsResponse.addParam(operationStatus);

        String currPermissionId, currOperationResponse, isEnabledFlag;
        JSONObject currOperationResponseJSON;
        Param currOperationParam;

        if (isAddedPermissionsOperation) {
            addCompositePermissionsResponse.setId("addCompositePermissions");
            isEnabledFlag = "1";
        } else {
            addCompositePermissionsResponse.setId("removedCompositePermissions");
            isEnabledFlag = "0";
        }

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("User_id", userId);
        postParametersMap.put("isEnabled", isEnabledFlag);
        postParametersMap.put("modifiedby", userDetailsBeanInstance.getUserId());
        postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

        for (int indexVar = 0; indexVar < compositePermissionsList.length(); indexVar++) {

            currPermissionId = compositePermissionsList.optString(indexVar);
            postParametersMap.put("CompositePermission_id", currPermissionId);

            currOperationResponse = Executor.invokeService(ServiceURLEnum.USERCOMPOSITEPERMISSION_UPDATE,
                    postParametersMap, null, requestInstance);
            currOperationResponseJSON = CommonUtilities.getStringAsJSONObject(currOperationResponse);
            if (currOperationResponseJSON == null || !currOperationResponseJSON.has(FabricConstants.OPSTATUS)
                    || currOperationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                operationStatus.setValue("Failure");
            }
            currOperationParam = new Param("permission: " + currPermissionId, currOperationResponse,
                    FabricConstants.STRING);
            addCompositePermissionsResponse.addParam(currOperationParam);

        }
        return addCompositePermissionsResponse;
    }

    private Record initUserCompositePermissionMapping(String userId, JSONArray listOfAddedCompositePermissions,
            JSONArray listOfRemovedCompositePermissions, UserDetailsBean userDetailsBeanInstance, String authToken,
            DataControllerRequest requestInstance) {

        Record operationRecord = new Record();
        operationRecord.setId("initializeUserCompositePermissionMapping");
        Map<String, String> postParametersMap = new HashMap<String, String>();

        postParametersMap.put(ODataQueryConstants.SELECT, "CompositePermission_id");
        postParametersMap.put(ODataQueryConstants.FILTER, "User_Id eq '" + userId + "'");
        String readUserCompositePermissionResponse = Executor.invokeService(ServiceURLEnum.USERCOMPOSITEPERMISSION_READ,
                postParametersMap, null, requestInstance);

        Set<String> compositePermissionSet = new HashSet<String>();
        String currPermissionId, currOperationResponse;

        if (listOfAddedCompositePermissions != null && listOfAddedCompositePermissions.length() > 0) {
            for (int indexVar = 0; indexVar < listOfAddedCompositePermissions.length(); indexVar++) {
                currPermissionId = listOfAddedCompositePermissions.optString(indexVar);
                compositePermissionSet.add(currPermissionId);
            }
        }

        if (listOfRemovedCompositePermissions != null && listOfRemovedCompositePermissions.length() > 0) {
            for (int indexVar = 0; indexVar < listOfRemovedCompositePermissions.length(); indexVar++) {
                currPermissionId = listOfRemovedCompositePermissions.optString(indexVar);
                compositePermissionSet.add(currPermissionId);
            }
        }

        JSONObject readUserCompositePermissionResponseJSON = CommonUtilities
                .getStringAsJSONObject(readUserCompositePermissionResponse);
        if (readUserCompositePermissionResponseJSON != null
                && readUserCompositePermissionResponseJSON.has(FabricConstants.OPSTATUS)
                && readUserCompositePermissionResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readUserCompositePermissionResponseJSON.has("usercompositepermission")) {
            JSONArray permissionsArray = readUserCompositePermissionResponseJSON
                    .getJSONArray("usercompositepermission");
            if (permissionsArray != null) {
                for (int indexVar = 0; indexVar < permissionsArray.length(); indexVar++) {
                    compositePermissionSet.remove(permissionsArray.get(indexVar));
                }
            }
        }

        postParametersMap.put("User_id", userId);
        postParametersMap.put("isEnabled", "0");
        postParametersMap.put("createdby", userDetailsBeanInstance.getUserId());
        postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
        postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
        postParametersMap.put("softdeleteflag", "0");

        Param currOperationParam = null;
        for (String currPermission : compositePermissionSet) {
            postParametersMap.put("CompositePermission_id", currPermission);
            currOperationResponse = Executor.invokeService(ServiceURLEnum.USERCOMPOSITEPERMISSION_CREATE,
                    postParametersMap, null, requestInstance);
            currOperationParam = new Param("insertPermission: " + currPermission, currOperationResponse,
                    FabricConstants.STRING);
            operationRecord.addParam(currOperationParam);
        }
        return operationRecord;
    }
}
