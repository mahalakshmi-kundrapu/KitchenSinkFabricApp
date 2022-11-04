package com.kony.adminconsole.service.customermanagement;

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

public class CustomerTypeGetService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerTypeGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();
            Map<String, String> postParametersMap = new HashMap<String, String>();

            PaginationHandler.setOffset(requestInstance, postParametersMap);
            JSONObject readCustomerTypeResponseJSON = PaginationHandler
                    .getPaginatedData(ServiceURLEnum.CUSTOMERTYPE_READ, postParametersMap, null, requestInstance);

            if (readCustomerTypeResponseJSON != null && readCustomerTypeResponseJSON.has(FabricConstants.OPSTATUS)
                    && readCustomerTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readCustomerTypeResponseJSON.has("customertype")) {
                LOG.debug("Fetch Customer Type Status:Successful");

                PaginationHandler.addPaginationMetadataToResultObject(processedResult, readCustomerTypeResponseJSON);
                JSONArray readCustomerTypeJSONArray = readCustomerTypeResponseJSON.getJSONArray("customertype");
                Dataset customerTypeDataSet = new Dataset();
                customerTypeDataSet.setId("CustomerTypeRecords");
                JSONObject currCustomerTypeJSONObject;

                for (int indexVar = 0; indexVar < readCustomerTypeJSONArray.length(); indexVar++) {
                    currCustomerTypeJSONObject = readCustomerTypeJSONArray.getJSONObject(indexVar);
                    Record currRecord = new Record();
                    for (String currKey : currCustomerTypeJSONObject.keySet()) {
                        currRecord.addParam(new Param(currKey, currCustomerTypeJSONObject.optString(currKey),
                                FabricConstants.STRING));
                    }
                    customerTypeDataSet.addRecord(currRecord);
                }
                processedResult.addDataset(customerTypeDataSet);
                return processedResult;
            }
            LOG.error("Fetch Customer Type Status:Failed");
            ErrorCodeEnum.ERR_20404.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}