package com.kony.olb;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.service.business.BusinessBankingCustomerServiceLimitManageService;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class IsEAgreementAvailableForUser implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(BusinessBankingCustomerServiceLimitManageService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        try {
            // Validate UserName
            if (requestInstance.getParameter("Username") == null) {
                ErrorCodeEnum.ERR_20705.setErrorCode(result);
                return result;
            }
            CustomerHandler customerHandler = new CustomerHandler();
            String userName = requestInstance.getParameter("Username");

            // Validate CustomerID
            String customerID = customerHandler.getCustomerId(userName, requestInstance);
            if (StringUtils.isBlank(customerID)) {
                ErrorCodeEnum.ERR_20688.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Fetch GroupId from customergroup table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put("$filter", "Customer_id eq '" + customerID + "'");
            inputMap.put("$select", "Group_id");

            String readCustomerGroupResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_READ, inputMap, null,
                    requestInstance);

            String groupId = null;
            JSONObject readCustomerGroupResponseJSON = CommonUtilities.getStringAsJSONObject(readCustomerGroupResponse);
            if (readCustomerGroupResponseJSON != null && readCustomerGroupResponseJSON.has(FabricConstants.OPSTATUS)
                    && readCustomerGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readCustomerGroupResponseJSON.has("customergroup")) {
                JSONArray readCustomerGroupJSONArray = readCustomerGroupResponseJSON.optJSONArray("customergroup");
                if (!(readCustomerGroupJSONArray == null || readCustomerGroupJSONArray.length() < 1)) {
                    JSONObject customerGroupObj = readCustomerGroupJSONArray.getJSONObject(0);
                    groupId = customerGroupObj.getString("Group_id");
                }
            }

            // Fetch GroupName from membergroup table
            inputMap.clear();
            inputMap.put("$filter", "id eq '" + groupId + "'");
            inputMap.put("$select", "isEAgreementActive");

            String readGroupResponse = Executor.invokeService(ServiceURLEnum.MEMBERGROUP_READ, inputMap, null,
                    requestInstance);

            String isEAgreementAvailable = null;

            JSONObject readGroupResponseJSON = CommonUtilities.getStringAsJSONObject(readGroupResponse);
            if (readGroupResponseJSON != null && readGroupResponseJSON.has(FabricConstants.OPSTATUS)
                    && readGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readGroupResponseJSON.has("membergroup")) {
                JSONArray readGroupJSONArray = readGroupResponseJSON.optJSONArray("membergroup");
                if (!(readGroupJSONArray == null || readGroupJSONArray.length() < 1)) {
                    JSONObject groupObj = readGroupJSONArray.getJSONObject(0);
                    isEAgreementAvailable = groupObj.optString("isEAgreementActive");
                    result.addParam(new Param("Status", "Success", FabricConstants.STRING));
                    result.addParam(new Param("isEAgreementAvailable", isEAgreementAvailable, FabricConstants.STRING));
                }
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in get EAgreement For Customer Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
        }
        return result;
    }

}
