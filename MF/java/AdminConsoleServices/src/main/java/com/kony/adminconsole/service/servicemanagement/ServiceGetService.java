package com.kony.adminconsole.service.servicemanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.handler.PaginationHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class ServiceGetService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(ServiceGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Map<String, String> postParametersMap = new HashMap<String, String>();
            Result processedResult = new Result();

            PaginationHandler.setOffset(requestInstance, postParametersMap);
            JSONObject readServicesResponseJSON = PaginationHandler.getPaginatedData(ServiceURLEnum.SERVICE_READ,
                    postParametersMap, null, requestInstance);

            if (readServicesResponseJSON != null && readServicesResponseJSON.has(FabricConstants.OPSTATUS)
                    && readServicesResponseJSON.getInt("opstatus") == 0 && readServicesResponseJSON.has("service")) {
                LOG.debug("Fetch Service Status:Successful");
                PaginationHandler.addPaginationMetadataToResultObject(processedResult, readServicesResponseJSON);
                JSONArray readServicesResponseJSONArray = readServicesResponseJSON.getJSONArray("service");
                Dataset ServicesDataSet = new Dataset();
                ServicesDataSet.setId("Services");
                JSONObject currServiceJSONObject = null;
                for (Object currObject : readServicesResponseJSONArray) {
                    if (currObject instanceof JSONObject) {
                        currServiceJSONObject = (JSONObject) currObject;
                        Record currRecord = new Record();
                        for (String currKey : currServiceJSONObject.keySet()) {
                            currRecord.addParam(new Param(currKey, currServiceJSONObject.optString(currKey),
                                    FabricConstants.STRING));
                        }
                        ServicesDataSet.addRecord(currRecord);
                    }
                }
                processedResult.addDataset(ServicesDataSet);
                return processedResult;
            }
            LOG.error("Fetch Service Status:Failed");
            ErrorCodeEnum.ERR_20384.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

}