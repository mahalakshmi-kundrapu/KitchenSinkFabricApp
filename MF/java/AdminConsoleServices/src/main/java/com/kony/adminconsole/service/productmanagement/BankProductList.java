package com.kony.adminconsole.service.productmanagement;

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

public class BankProductList implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(BankProductList.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();

            Map<String, String> postParametersMap = new HashMap<String, String>();
            PaginationHandler.setOffset(requestInstance, postParametersMap);

            JSONObject readProductDetailViewResponseJSON = PaginationHandler
                    .getPaginatedData(ServiceURLEnum.PRODUCTDETAIL_VIEW_READ, postParametersMap, null, requestInstance);
            if (readProductDetailViewResponseJSON != null
                    && readProductDetailViewResponseJSON.has(FabricConstants.OPSTATUS)
                    && readProductDetailViewResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readProductDetailViewResponseJSON.has("productdetail_view")) {
                JSONArray productDetailsList = (JSONArray) readProductDetailViewResponseJSON.get("productdetail_view");
                Dataset productListDataset = new Dataset();
                productListDataset.setId("records");

                for (int indexVar = 0; indexVar < productDetailsList.length(); indexVar++) {
                    JSONObject periodicLimitInfoJSON = productDetailsList.getJSONObject(indexVar);
                    Record currCommunicationInfoReord = new Record();
                    for (String currKey : periodicLimitInfoJSON.keySet()) {
                        Param currValParam = new Param(currKey, periodicLimitInfoJSON.getString(currKey),
                                FabricConstants.STRING);
                        currCommunicationInfoReord.addParam(currValParam);
                    }
                    productListDataset.addRecord(currCommunicationInfoReord);
                }
                processedResult = new Result();
                processedResult.addDataset(productListDataset);
                return processedResult;
            }
            ErrorCodeEnum.ERR_20442.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}