package com.kony.adminconsole.service.auditlogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class CustomerActivityMasterData implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerActivityMasterData.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Events", EventEnum.getCustomerEventAliases());
            jsonObject.put("Roles", getRoles(requestInstance));
            String resultJSON = jsonObject.toString();
            JSONObject finalJSONResult = new JSONObject(resultJSON);

            result.addParam(new Param("Events", finalJSONResult.get("Events").toString()));
            result.addParam(new Param("Roles", finalJSONResult.get("Roles").toString()));

        } catch (Exception e) {
            LOG.error("Failed while executing get transaction logs", e);
            ErrorCodeEnum.ERR_20684.setErrorCode(result);
        }
        return result;
    }

    private List<String> getRoles(DataControllerRequest requestInstance) {
        List<String> roles = new ArrayList<String>();
        Map<String, String> postParametersMap = new HashMap<String, String>();
        requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        String getRolesResponse = Executor.invokeService(ServiceURLEnum.ROLE_READ, postParametersMap, null,
                requestInstance);
        JSONObject getRolesResponseJSON = CommonUtilities.getStringAsJSONObject(getRolesResponse);
        if (getRolesResponseJSON != null && getRolesResponseJSON.has(FabricConstants.OPSTATUS)
                && getRolesResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            JSONArray listOfRoles = getRolesResponseJSON.getJSONArray("role");
            JSONObject currRecordJSONObject;
            for (int indexVar = 0; indexVar < listOfRoles.length(); indexVar++) {
                currRecordJSONObject = listOfRoles.getJSONObject(indexVar);
                if (currRecordJSONObject.has("Name")) {
                    roles.add(currRecordJSONObject.getString("Name"));
                }
            }
        }
        return roles;
    }
}