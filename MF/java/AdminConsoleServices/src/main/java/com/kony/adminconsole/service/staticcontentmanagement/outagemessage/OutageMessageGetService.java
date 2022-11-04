package com.kony.adminconsole.service.staticcontentmanagement.outagemessage;

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

public class OutageMessageGetService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(OutageMessageGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();
            Map<String, String> postParametersMap = new HashMap<String, String>();
            String readOutageMessageResponse = Executor.invokeService(ServiceURLEnum.OUTAGEMESSAGE_VIEW_READ,
                    postParametersMap, null, requestInstance);
            JSONObject readOutageMessageResponseJSON = CommonUtilities.getStringAsJSONObject(readOutageMessageResponse);
            if (readOutageMessageResponseJSON != null && readOutageMessageResponseJSON.has(FabricConstants.OPSTATUS)
                    && readOutageMessageResponseJSON.optInt(FabricConstants.OPSTATUS) == 0
                    && readOutageMessageResponseJSON.has("outagemessage_view")) {
                JSONArray outageMessageJSONArray = readOutageMessageResponseJSON.getJSONArray("outagemessage_view");
                Dataset outageMessageDataSet = new Dataset();
                outageMessageDataSet.setId("records");

                JSONObject currOutageMessageJSONObject;
                for (int indexVar = 0; indexVar < outageMessageJSONArray.length(); indexVar++) {
                    currOutageMessageJSONObject = outageMessageJSONArray.getJSONObject(indexVar);
                    Record currOutageMessageRecord = new Record();
                    for (String currKey : currOutageMessageJSONObject.keySet()) {
                        Param currValParam = new Param(currKey, currOutageMessageJSONObject.optString(currKey),
                                FabricConstants.STRING);
                        currOutageMessageRecord.addParam(currValParam);
                    }
                    outageMessageDataSet.addRecord(currOutageMessageRecord);
                }
                processedResult.addDataset(outageMessageDataSet);
                return processedResult;
            }
            ErrorCodeEnum.ERR_20185.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}