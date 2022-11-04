package com.kony.olb;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class ConfigurationsGetService implements JavaService2 {

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        String isPreLogin = "1";
        Map<String, String> postParametersMap = new HashMap<String, String>();
        requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        if (StringUtils.isNotBlank(requestInstance.getParameter("username"))) {
            isPreLogin = "0";
        }
        postParametersMap.put("$filter", "isPreLogin eq '" + isPreLogin + "'");
        String getConfigurationsResponse = Executor.invokeService(ServiceURLEnum.CONFIGURATIONS_READ, postParametersMap,
                null, requestInstance);
        JSONObject getConfigurationsJSON = CommonUtilities.getStringAsJSONObject(getConfigurationsResponse);
        Dataset configurationssDataSet = new Dataset();
        configurationssDataSet.setId("ConfigurationData");
        if (getConfigurationsJSON != null && getConfigurationsJSON.has(FabricConstants.OPSTATUS)
                && getConfigurationsJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            JSONArray listOfConfigurations = getConfigurationsJSON.getJSONArray("configurations");
            JSONObject currRecordJSONObject;
            for (int indexVar = 0; indexVar < listOfConfigurations.length(); indexVar++) {
                currRecordJSONObject = listOfConfigurations.getJSONObject(indexVar);
                Record currRecord = new Record();
                if (currRecordJSONObject.has("key")) {
                    Param key_Param = new Param("key", currRecordJSONObject.getString("key"));
                    currRecord.addParam(key_Param);
                }
                if (currRecordJSONObject.has("value")) {
                    Param value_Param = new Param("value", currRecordJSONObject.getString("value"));
                    currRecord.addParam(value_Param);
                }
                configurationssDataSet.addRecord(currRecord);
            }
        }
        result.addDataset(configurationssDataSet);
        return result;
    }

}
