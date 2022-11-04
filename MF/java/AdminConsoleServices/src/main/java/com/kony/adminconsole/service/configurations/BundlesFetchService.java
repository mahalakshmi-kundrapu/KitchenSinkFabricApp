package com.kony.adminconsole.service.configurations;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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

/**
 * Service to fetch configurations bundles from the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class BundlesFetchService implements JavaService2 {

    public static final String CONFIGURATIONBUNDLES = "configurationbundles";
    public static final String BUNDLES = "bundles";

    private static final Logger LOG = Logger.getLogger(BundlesFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();

        try {

            // ** Reading entries from 'configurationbundles' table **
            Map<String, String> configurationBundlesTableMap = new HashMap<>();

            String readConfigurationBundlesResponse = Executor.invokeService(ServiceURLEnum.CONFIGURATIONSBUNDLE_READ,
                    configurationBundlesTableMap, null, requestInstance);
            JSONObject readConfigurationBundlesResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readConfigurationBundlesResponse);

            if (readConfigurationBundlesResponseJSON != null
                    && readConfigurationBundlesResponseJSON.has(FabricConstants.OPSTATUS)
                    && readConfigurationBundlesResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readConfigurationBundlesResponseJSON.has(CONFIGURATIONBUNDLES)) {

                JSONArray readConfigurationBundlesResponseJSONArray = readConfigurationBundlesResponseJSON
                        .getJSONArray(CONFIGURATIONBUNDLES);
                Dataset configurationBundlesDataSet = new Dataset();
                configurationBundlesDataSet.setId(BUNDLES);

                for (int i = 0; i < readConfigurationBundlesResponseJSONArray.length(); ++i) {

                    JSONObject bundleJSONObject = readConfigurationBundlesResponseJSONArray.getJSONObject(i);
                    Record bundleRecord = new Record();
                    if (StringUtils.isNotBlank(bundleJSONObject.optString("bundle_id"))
                            && StringUtils.isNotBlank(bundleJSONObject.optString("bundle_name"))
                            && StringUtils.isNotBlank(bundleJSONObject.optString("app_id"))) {

                        bundleRecord.addParam(
                                new Param("bundleId", bundleJSONObject.getString("bundle_id"), FabricConstants.STRING));
                        bundleRecord.addParam(new Param("bundleName", bundleJSONObject.getString("bundle_name"),
                                FabricConstants.STRING));
                        bundleRecord.addParam(
                                new Param("bundleAppId", bundleJSONObject.getString("app_id"), FabricConstants.STRING));
                    }

                    configurationBundlesDataSet.addRecord(bundleRecord);
                }

                result.addDataset(configurationBundlesDataSet);
            } else {
                ErrorCodeEnum.ERR_20482.setErrorCode(result);
            }

        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in BundlesFetchService. Error: ", e);
        }

        return result;
    }
}