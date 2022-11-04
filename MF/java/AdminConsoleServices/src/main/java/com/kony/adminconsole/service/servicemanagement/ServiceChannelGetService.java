package com.kony.adminconsole.service.servicemanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
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

public class ServiceChannelGetService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(ServiceChannelGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {

            Result processedResult = new Result();

            Map<String, String> postParametersMap = new HashMap<String, String>();

            String readServiceChannelsResponse = Executor.invokeService(ServiceURLEnum.SERVICECHANNEL_READ,
                    postParametersMap, null, requestInstance);
            JSONObject readServiceChannelsResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readServiceChannelsResponse);

            if (readServiceChannelsResponseJSON != null && readServiceChannelsResponseJSON.has(FabricConstants.OPSTATUS)
                    && readServiceChannelsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                JSONArray readGroupsCustomerIdsJSONArray = readServiceChannelsResponseJSON
                        .getJSONArray("servicechannel");
                Dataset EntitlementsDataSet = new Dataset();
                EntitlementsDataSet.setId("ServiceChannels");
                for (int indexVar = 0; indexVar < readGroupsCustomerIdsJSONArray.length(); indexVar++) {
                    JSONObject currServiceJSONObject = readGroupsCustomerIdsJSONArray.getJSONObject(indexVar);
                    Record currRecord = new Record();
                    Param id_Param = new Param("id", currServiceJSONObject.getString("id"), FabricConstants.STRING);
                    currRecord.addParam(id_Param);
                    Param Description_Param = new Param("Description", currServiceJSONObject.getString("Description"),
                            FabricConstants.STRING);
                    currRecord.addParam(Description_Param);

                    EntitlementsDataSet.addRecord(currRecord);
                }
                processedResult.addDataset(EntitlementsDataSet);
                return processedResult;
            }
            ErrorCodeEnum.ERR_20388.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}