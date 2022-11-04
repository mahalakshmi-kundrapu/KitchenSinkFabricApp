package com.kony.adminconsole.service.group;

import java.util.HashMap;
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

public class GroupEntitlementGetService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(GroupEntitlementGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();

        try {
            String groupId = requestInstance.getParameter("Group_id");

            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put(ODataQueryConstants.FILTER, "Group_id eq '" + groupId + "'");

            JSONObject readEndpointResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
                    ServiceURLEnum.GROUPENTITLEMENT_VIEW_READ, postParametersMap, null, requestInstance));

            if (readEndpointResponse != null && readEndpointResponse.has(FabricConstants.OPSTATUS)
                    && readEndpointResponse.getInt(FabricConstants.OPSTATUS) == 0
                    && readEndpointResponse.has("groupentitlement_view")) {

                JSONArray groupEntitlementArray = readEndpointResponse.getJSONArray("groupentitlement_view");
                Dataset groupEntitlementDataset = new Dataset();
                groupEntitlementDataset.setId("GroupEntitlements");

                groupEntitlementArray.forEach((element) -> {

                    JSONObject groupEntitlement = (JSONObject) element;
                    Record groupEntitlementRecord = new Record();

                    groupEntitlementRecord.addParam(
                            new Param("Group_id", groupEntitlement.getString("Group_id"), FabricConstants.STRING));
                    groupEntitlementRecord.addParam(
                            new Param("Service_id", groupEntitlement.getString("Service_id"), FabricConstants.STRING));
                    groupEntitlementRecord.addParam(
                            new Param("Name", groupEntitlement.getString("Service_name"), FabricConstants.STRING));
                    groupEntitlementRecord.addParam(new Param("Description",
                            groupEntitlement.getString("Service_description"), FabricConstants.STRING));
                    groupEntitlementRecord.addParam(new Param("Type_id", groupEntitlement.getString("Service_type_id"),
                            FabricConstants.STRING));
                    groupEntitlementRecord.addParam(new Param("MaxDailyLimit",
                            groupEntitlement.optString("MaxDailyLimit"), FabricConstants.STRING));
                    groupEntitlementRecord.addParam(new Param("MaxTransferLimit",
                            groupEntitlement.optString("MaxTransferLimit"), FabricConstants.STRING));
                    groupEntitlementRecord.addParam(new Param("MinTransferLimit",
                            groupEntitlement.optString("MinTransferLimit"), FabricConstants.STRING));
                    groupEntitlementRecord.addParam(new Param("TransactionFee_id",
                            groupEntitlement.optString("TransactionFee_id"), FabricConstants.STRING));
                    groupEntitlementRecord.addParam(new Param("TransactionLimit_id",
                            groupEntitlement.optString("TransactionLimit_id"), FabricConstants.STRING));
                    groupEntitlementRecord
                            .addParam(new Param("Code", groupEntitlement.optString("Code"), FabricConstants.STRING));

                    groupEntitlementDataset.addRecord(groupEntitlementRecord);
                });

                result.addDataset(groupEntitlementDataset);
            } else {
                ErrorCodeEnum.ERR_20424.setErrorCode(result);
            }
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in GroupEntitlementGetService JAVA service. Error: ", e);
        }

        return result;
    }
}