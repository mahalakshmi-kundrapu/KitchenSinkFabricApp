package com.kony.adminconsole.service.permissions;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.handler.PermissionHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Result;

/**
 * GetUserOrRoleCompositePermissions service will fetch Role or User level composite permissions
 * 
 * @author Alahari Prudhvi Akhil
 * 
 */
public class CompositePermissionsGetService implements JavaService2 {
    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        try {
            String userID = requestInstance.getParameter("User_id");
            String roleID = requestInstance.getParameter("Role_id");
            String permissionID = requestInstance.getParameter("Permission_id");

            if (StringUtils.isBlank(permissionID)) {
                return ErrorCodeEnum.ERR_20743.setErrorCode(processedResult);
            }
            JSONObject response = PermissionHandler.getAggregateCompositePermissions(userID, roleID, permissionID,
                    requestInstance, "id,Permission_id,Name,Description,isEnabled", null);
            if (response.has("ErrorEnum")) {
                ErrorCodeEnum errorEnumType = (ErrorCodeEnum) response.get("ErrorEnum");
                return errorEnumType.setErrorCode(processedResult);
            }
            Dataset finalCompositePermissionsDataset = CommonUtilities
                    .constructDatasetFromJSONArray(response.getJSONArray("CompositePermissions"));
            finalCompositePermissionsDataset.setId("CompositePermissions");
            processedResult.addDataset(finalCompositePermissionsDataset);
            return processedResult;
        } catch (Exception e) {
            return ErrorCodeEnum.ERR_20001.setErrorCode(processedResult);
        }
    }
}