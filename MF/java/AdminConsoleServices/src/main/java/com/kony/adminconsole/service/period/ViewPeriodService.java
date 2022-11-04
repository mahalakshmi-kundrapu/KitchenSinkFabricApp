package com.kony.adminconsole.service.period;

import java.util.HashMap;
import java.util.Map;

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

public class ViewPeriodService implements JavaService2 {

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Param statusParam = null;
        String systemUser = null;
        Result processedResult = new Result();

        if (requestInstance.getParameter("systemUser") != null) {
            systemUser = requestInstance.getParameter("systemUser");
        }

        try {
            JSONObject readResJSON = viewPeriod(systemUser, requestInstance);
            if (readResJSON == null || !readResJSON.has(FabricConstants.OPSTATUS)
                    || readResJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                return ErrorCodeEnum.ERR_20506.setErrorCode(processedResult);
            }
            JSONArray periodService = (JSONArray) readResJSON.get("period");

            Dataset periodicLimitDataSet = new Dataset();
            periodicLimitDataSet.setId("records");

            for (int indexVar = 0; indexVar < periodService.length(); indexVar++) {
                JSONObject periodicLimitInfoJSON = periodService.getJSONObject(indexVar);
                Record servCommunicationInfoReord = new Record();
                for (String currKey : periodicLimitInfoJSON.keySet()) {
                    Param currValParam = new Param(currKey, periodicLimitInfoJSON.getString(currKey), "string");
                    servCommunicationInfoReord.addParam(currValParam);
                }
                periodicLimitDataSet.addRecord(servCommunicationInfoReord);
            }
            processedResult = new Result();
            processedResult.addDataset(periodicLimitDataSet);
            return processedResult;
        } catch (Exception e) {
            statusParam = new Param("Status", "Error", "string");
            processedResult.addParam(statusParam);
            processedResult.addParam(new Param("exception", e.getMessage(), FabricConstants.STRING));
            return ErrorCodeEnum.ERR_20506.setErrorCode(processedResult);
        }

    }

    private JSONObject viewPeriod(String systemUser, DataControllerRequest requestInstance) {
        JSONObject createResponseJSON = null;
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        String createPeriodResponse = Executor.invokeService(ServiceURLEnum.PERIOD_READ, postParametersMap, null,
                requestInstance);
        createResponseJSON = CommonUtilities.getStringAsJSONObject(createPeriodResponse);
        return createResponseJSON;
    }

}
