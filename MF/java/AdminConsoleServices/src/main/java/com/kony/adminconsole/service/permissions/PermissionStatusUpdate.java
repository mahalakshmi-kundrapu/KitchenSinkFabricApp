package com.kony.adminconsole.service.permissions;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
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
import com.konylabs.middleware.dataobject.Result;

public class PermissionStatusUpdate implements JavaService2 {

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        Param statusParam;
        String statusId = "";
        String systemUser = "";
        String permissionId = "";

        permissionId = requestInstance.getParameter("id");
        if (StringUtils.isBlank(permissionId)) {
            return ErrorCodeEnum.ERR_20520.setErrorCode(result);
        }

        statusId = requestInstance.getParameter("Status_id");
        if (StringUtils.isBlank(permissionId)) {
            return ErrorCodeEnum.ERR_20609.setErrorCode(result);
        }

        if (requestInstance.getParameter("systemUser") != null) {
            systemUser = requestInstance.getParameter("systemUser");
        }

        String permissionName = null;
        try {
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put(ODataQueryConstants.FILTER, "id eq " + permissionId);
            JSONObject PermissionReadResponse = CommonUtilities.getStringAsJSONObject(
                    Executor.invokeService(ServiceURLEnum.PERMISSION_READ, postParametersMap, null, requestInstance));
            permissionName = PermissionReadResponse.getJSONArray("permission").getJSONObject(0).getString("Name");
            postParametersMap.clear();
        } catch (Exception ignored) {
        }
        try {
            // update
            JSONObject updateResponseJSON = updatePermissionStatus(permissionId, permissionName, statusId, systemUser,
                    requestInstance);
            if (updateResponseJSON != null && updateResponseJSON.has(FabricConstants.OPSTATUS)
                    && updateResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                // create call back to delete inserted record in role table
                return ErrorCodeEnum.ERR_20544.setErrorCode(result);
            }
        } catch (Exception e) {
            statusParam = new Param("Status", "Error", FabricConstants.STRING);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERMISSIONS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Permission status update failed. Permission name: " + permissionName);
            result.addParam(statusParam);
        }
        statusParam = new Param("Status", "UPDATE SUCCESSFUL", FabricConstants.STRING);
        result.addParam(statusParam);
        return result;
    }

    private JSONObject updatePermissionStatus(String permissionId, String permissionName, String statusId,
            String systemUser, DataControllerRequest requestInstance) {
        JSONObject updateResponseJSON = null;
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        postParametersMap.put("id", permissionId);
        postParametersMap.put("Status_id", statusId);
        postParametersMap.put("modifiedby", systemUser);
        postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

        String updatePeriodicLimitResponse = Executor.invokeService(ServiceURLEnum.PERMISSION_UPDATE, postParametersMap,
                null, requestInstance);

        updateResponseJSON = CommonUtilities.getStringAsJSONObject(updatePeriodicLimitResponse);
        int getOpStatusCode = updateResponseJSON.getInt(FabricConstants.OPSTATUS);
        if (getOpStatusCode != 0) {
            // rollback logic
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERMISSIONS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Permission status update failed. Permission name: " + permissionName);
            return updateResponseJSON;
        }
        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERMISSIONS, EventEnum.UPDATE,
                ActivityStatusEnum.SUCCESSFUL,
                "Permission status update successful. Permission name: " + permissionName);
        return updateResponseJSON;
    }

}
