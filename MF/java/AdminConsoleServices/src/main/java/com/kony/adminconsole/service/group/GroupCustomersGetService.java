package com.kony.adminconsole.service.group;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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

public class GroupCustomersGetService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(GroupCustomersGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();
            requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            Map<String, String> postParametersMap = new HashMap<String, String>();
            @SuppressWarnings("unchecked")
            Map<String, String> input = (HashMap<String, String>) inputArray[1];
            String GroupId = input.get("Group_id");
            if (StringUtils.isNotBlank(GroupId)) {
                postParametersMap.put(ODataQueryConstants.FILTER,
                        "Group_id eq '" + input.get("Group_id").toString() + "'");
            }

            String readGroupsCustomersResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_READ,
                    postParametersMap, null, requestInstance);
            JSONObject readGroupsCustomersResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readGroupsCustomersResponse);

            if (readGroupsCustomersResponseJSON != null && readGroupsCustomersResponseJSON.has(FabricConstants.OPSTATUS)
                    && readGroupsCustomersResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                JSONArray readGroupsCustomerIdsJSONArray = readGroupsCustomersResponseJSON
                        .getJSONArray("customergroup");
                Dataset EntitlementsDataSet = new Dataset();
                EntitlementsDataSet.setId("GroupCustomers");
                for (int indexVar = 0; indexVar < readGroupsCustomerIdsJSONArray.length(); indexVar++) {
                    JSONObject currServiceJSONObject = readGroupsCustomerIdsJSONArray.getJSONObject(indexVar);
                    Record currRecord = new Record();
                    Param Customer_id_Param = new Param("Customer_id", currServiceJSONObject.getString("Customer_id"),
                            FabricConstants.STRING);
                    currRecord.addParam(Customer_id_Param);

                    Map<String, String> postParametersMap1 = new HashMap<String, String>();
                    postParametersMap1.put(ODataQueryConstants.FILTER,
                            "id eq '" + Customer_id_Param.getValue().toString() + "'");
                    String readCustomerResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_READ,
                            postParametersMap1, null, requestInstance);
                    JSONObject readCustomerResponseJSON = CommonUtilities.getStringAsJSONObject(readCustomerResponse);
                    if (readCustomerResponseJSON != null && readCustomerResponseJSON.has(FabricConstants.OPSTATUS)
                            && readCustomerResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                        JSONArray readCustomerJSONArray = readCustomerResponseJSON.getJSONArray("customer");
                        if (readCustomerJSONArray.length() != 0) {
                            JSONObject currCustomerJSONObject = readCustomerJSONArray.getJSONObject(0);
                            String FullName_String = "";
                            if (currCustomerJSONObject.has("FirstName")) {
                                FullName_String = currCustomerJSONObject.getString("FirstName");
                            }
                            if (currCustomerJSONObject.has("LastName")) {
                                FullName_String += currCustomerJSONObject.getString("LastName");
                            }
                            Param FullName_Param = new Param("FullName", FullName_String, FabricConstants.STRING);
                            currRecord.addParam(FullName_Param);
                            if (currCustomerJSONObject.has("UserName")) {
                                Param Username_Param = new Param("Username",
                                        currCustomerJSONObject.getString("UserName"), FabricConstants.STRING);
                                currRecord.addParam(Username_Param);
                            } else {
                                Param Username_Param = new Param("Username", "", FabricConstants.STRING);
                                currRecord.addParam(Username_Param);
                            }
                            if (currCustomerJSONObject.has("Status_id")) {
                                Param Status_id_Param = new Param("Status_id",
                                        currCustomerJSONObject.getString("Status_id"), FabricConstants.STRING);
                                currRecord.addParam(Status_id_Param);
                            } else {
                                Param Status_id_Param = new Param("Status_id", "", FabricConstants.STRING);
                                currRecord.addParam(Status_id_Param);
                            }
                            if (currCustomerJSONObject.has("modifiedby")) {
                                Param UpdatedBy_Param = new Param("UpdatedBy",
                                        currCustomerJSONObject.optString("modifiedby"), FabricConstants.STRING);
                                currRecord.addParam(UpdatedBy_Param);
                            } else {
                                Param UpdatedBy_Param = new Param("UpdatedBy", "", FabricConstants.STRING);
                                currRecord.addParam(UpdatedBy_Param);
                            }
                            if (currCustomerJSONObject.has("lastmodifiedts")) {
                                Param UpdatedOn_Param = new Param("UpdatedOn",
                                        currCustomerJSONObject.getString("lastmodifiedts"), FabricConstants.STRING);
                                currRecord.addParam(UpdatedOn_Param);
                            } else {
                                Param UpdatedOn_Param = new Param("UpdatedOn", "", FabricConstants.STRING);
                                currRecord.addParam(UpdatedOn_Param);
                            }

                        } else {
                            Param FullName_Param = new Param("FullName", "", FabricConstants.STRING);
                            currRecord.addParam(FullName_Param);
                            Param Username_Param = new Param("Username", "", FabricConstants.STRING);
                            currRecord.addParam(Username_Param);
                            Param Status_id_Param = new Param("Status_id", "", FabricConstants.STRING);
                            currRecord.addParam(Status_id_Param);
                            Param UpdatedBy_Param = new Param("UpdatedBy", "", FabricConstants.STRING);
                            currRecord.addParam(UpdatedBy_Param);
                            Param UpdatedOn_Param = new Param("UpdatedOn", "", FabricConstants.STRING);
                            currRecord.addParam(UpdatedOn_Param);
                        }
                    } else {
                        ErrorCodeEnum.ERR_20426.setErrorCode(processedResult);
                        return processedResult;
                    }

                    Map<String, String> postParametersMap2 = new HashMap<String, String>();
                    postParametersMap2.put(ODataQueryConstants.FILTER, "Customer_id eq '"
                            + Customer_id_Param.getValue().toString() + "' and Type_id eq 'COMM_TYPE_EMAIL'");
                    String readEmailResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERCOMMUNICATION_READ,
                            postParametersMap2, null, requestInstance);
                    JSONObject readEmailResponseJSON = CommonUtilities.getStringAsJSONObject(readEmailResponse);
                    if (readEmailResponseJSON != null && readEmailResponseJSON.has(FabricConstants.OPSTATUS)
                            && readEmailResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                        JSONArray readEmailJSONArray = readEmailResponseJSON.getJSONArray("customercommunication");
                        if (readEmailJSONArray.length() != 0) {
                            JSONObject currJSONObject = readEmailJSONArray.getJSONObject(0);
                            Param Email_Param = new Param("Email", currJSONObject.getString("Value"),
                                    FabricConstants.STRING);
                            currRecord.addParam(Email_Param);
                        } else {
                            Param Email_Param = new Param("Email", "", FabricConstants.STRING);
                            currRecord.addParam(Email_Param);
                        }
                    } else {
                        ErrorCodeEnum.ERR_20879.setErrorCode(processedResult);
                        return processedResult;
                    }

                    EntitlementsDataSet.addRecord(currRecord);
                }

                processedResult.addDataset(EntitlementsDataSet);
                return processedResult;
            }
            ErrorCodeEnum.ERR_20426.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}