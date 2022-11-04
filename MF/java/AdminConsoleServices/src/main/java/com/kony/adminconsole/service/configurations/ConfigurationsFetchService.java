package com.kony.adminconsole.service.configurations;

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

/**
 * Service to fetch configurations bundles from the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class ConfigurationsFetchService implements JavaService2 {

    public static final String CONFIGURATIONS = "configurations";

    private static final Logger LOG = Logger.getLogger(BundlesFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();

        try {

            // ** Reading entries from 'configurations' table **
            Map<String, String> configurationsTableMap = new HashMap<>();

            String bundleId = requestInstance.getParameter("bundleId");

            if (bundleId != null && !bundleId.equals("")) {
                configurationsTableMap.put(ODataQueryConstants.FILTER, "bundle_id eq '" + bundleId + "'");
            }
            /*
             * Service will return configurations corresponding to the 'bundleId' received from the client. If no
             * bundleId is received, all configurations will be sent to the client.
             */

            String readConfigurationsResponse = Executor.invokeService(ServiceURLEnum.CONFIGURATIONS_READ,
                    configurationsTableMap, null, requestInstance);
            JSONObject readConfigurationsResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readConfigurationsResponse);

            if (readConfigurationsResponseJSON != null && readConfigurationsResponseJSON.has(FabricConstants.OPSTATUS)
                    && readConfigurationsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readConfigurationsResponseJSON.has(CONFIGURATIONS)) {

                JSONArray readConfigurationsResponseJSONArray = readConfigurationsResponseJSON
                        .getJSONArray(CONFIGURATIONS);
                Dataset configurationsDataSet = new Dataset();
                configurationsDataSet.setId(CONFIGURATIONS);

                for (int i = 0; i < readConfigurationsResponseJSONArray.length(); ++i) {

                    JSONObject configurationJSONObject = readConfigurationsResponseJSONArray.getJSONObject(i);
                    Record configurationRecord = new Record();

                    configurationRecord.addParam(new Param("bundleId", configurationJSONObject.getString("bundle_id"),
                            FabricConstants.STRING));
                    configurationRecord.addParam(new Param("configurationId",
                            configurationJSONObject.getString("configuration_id"), FabricConstants.STRING));
                    configurationRecord.addParam(new Param("configurationKey",
                            configurationJSONObject.getString("config_key"), FabricConstants.STRING));
                    configurationRecord.addParam(new Param("configurationValue",
                            configurationJSONObject.getString("config_value"), FabricConstants.STRING));
                    configurationRecord.addParam(new Param("configurationDescription",
                            configurationJSONObject.getString("description"), FabricConstants.STRING));
                    configurationRecord.addParam(new Param("configurationType",
                            configurationJSONObject.getString("config_type"), FabricConstants.STRING));
                    configurationRecord.addParam(new Param("configurationTarget",
                            configurationJSONObject.getString("target"), FabricConstants.STRING));

                    configurationsDataSet.addRecord(configurationRecord);
                }

                result.addDataset(configurationsDataSet);
            } else {
                ErrorCodeEnum.ERR_20486.setErrorCode(result);
            }
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in BundlesFetchService. Error: ", e);
        }

        return result;
    }
}