package com.kony.adminconsole.service.configurations;

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

/**
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class ConfigurationGet implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(ConfigurationGet.class);

    private static final String INPUT_BUNDLE_NAME = "bundle_name";
    private static final String INPUT_CONFIG_KEY = "config_key";
    private static final String METHOD_ID_CONFIGURATIONS_PRE_LOGIN = "getConfigurationsPreLogin";
    private static final String METHOD_ID_CONFIGURATIONS = "getConfigurations";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result processedResult = new Result();
        try {
            String bundleName = requestInstance.getParameter(INPUT_BUNDLE_NAME);
            String configKey = requestInstance.getParameter(INPUT_CONFIG_KEY);

            if (StringUtils.isBlank(bundleName)) {
                ErrorCodeEnum.ERR_20712.setErrorCode(processedResult);
                return processedResult;
            }
            if (methodID.equalsIgnoreCase(METHOD_ID_CONFIGURATIONS_PRE_LOGIN)) {
                computeConfigurations(bundleName, configKey, processedResult, requestInstance, true);
            } else if (methodID.equalsIgnoreCase(METHOD_ID_CONFIGURATIONS)) {
                computeConfigurations(bundleName, configKey, processedResult, requestInstance, false);
            }

        } catch (Exception e) {
            ErrorCodeEnum.ERR_21407.setErrorCode(processedResult);
            LOG.error("Unexpected error has occurred. " + e.getMessage());
            processedResult.addParam(new Param("FailureReason", e.getMessage(), FabricConstants.STRING));
        }
        return processedResult;
    }

    public static void computeConfigurations(String bundleName, String configKey, Result processedResult,
            DataControllerRequest requestInstance, Boolean isPreLoginConfiguration) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        String filter = "bundle_name eq '" + bundleName + "'";

        if (StringUtils.isNotBlank(configKey)) {
            filter += " and key eq '" + configKey + "'";
        }

        if (isPreLoginConfiguration) {
            filter += " and isPreLoginConfiguration eq 1";
        }

        postParametersMap.put(ODataQueryConstants.FILTER, filter);
        JSONObject readEndpointResponse = CommonUtilities.getStringAsJSONObject(Executor
                .invokeService(ServiceURLEnum.CONFIGURATION_VIEW_READ, postParametersMap, null, requestInstance));

        if (readEndpointResponse != null && readEndpointResponse.has(FabricConstants.OPSTATUS)
                && readEndpointResponse.getInt(FabricConstants.OPSTATUS) == 0
                && readEndpointResponse.has("configuration_view")) {

            JSONArray configurationsArray = readEndpointResponse.getJSONArray("configuration_view");

            if (StringUtils.isNotBlank(configKey)) {
                configurationsArray.forEach((element) -> {

                    JSONObject configurationObject = (JSONObject) element;

                    if (configurationObject.has("key") && configurationObject.getString("key").equals(configKey)) {
                        Record configurationRecord = CommonUtilities.constructRecordFromJSONObject(configurationObject);
                        configurationRecord.setId("Configuration");
                        processedResult.addRecord(configurationRecord);
                    }
                });
                if (processedResult.getRecordById("Configuration") == null) {
                    ErrorCodeEnum.ERR_21409.setErrorCode(processedResult);
                    return;
                }
            } else {
                Dataset configurationDataset = CommonUtilities.constructDatasetFromJSONArray(configurationsArray);
                configurationDataset.setId("Configurations");
                processedResult.addDataset(configurationDataset);
            }
        } else {
            ErrorCodeEnum.ERR_21407.setErrorCode(processedResult);
            processedResult
                    .addParam(new Param("FailureReason", String.valueOf(readEndpointResponse), FabricConstants.STRING));
        }
    }
}