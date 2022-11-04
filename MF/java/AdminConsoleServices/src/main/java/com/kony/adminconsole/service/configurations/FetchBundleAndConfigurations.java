package com.kony.adminconsole.service.configurations;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
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
 * Service to fetch configurations bundles & configurations at the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class FetchBundleAndConfigurations implements JavaService2 {

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        return getConfigurationsMasters(inputArray, requestInstance);
    }

    public Object getConfigurationsMasters(Object[] inputArray, DataControllerRequest requestInstance)
            throws Exception {
        Result result = new Result();
        String tempTime = null;
        String greatest_timestamp_String = null;

        @SuppressWarnings("rawtypes")
        Map inputMap = (Map) inputArray[1];
        String input = (String) inputMap.get("id1");
        int input_count = StringUtils.countMatches(input, "|");
        String id1val;
        JSONObject id2;
        if (input_count == 1) {
            id2 = new JSONObject(input.substring(input.indexOf("|") + 1, input.length()));
            id1val = input.substring(0, input.indexOf("|"));
        } else {
            id2 = null;
            id1val = input;
        }

        String[] combinations = {};
        if (id1val != null) {
            combinations = id1val.split(";");
        }
        String downloadServerKeys = new String();
        Record recordResult = new Record();
        Record updatedConfigurations = new Record();
        updatedConfigurations.setId("updatedConfigurations");
        Record timeStamp = new Record();
        timeStamp.setId("timeStamp");
        Record finalBundlesRecord = new Record();
        finalBundlesRecord.setId("finalBundles");
        recordResult.setId("output");
        LinkedHashSet<String> finalBundles = new LinkedHashSet<String>();
        LinkedHashSet<String> bundles_in_combination = new LinkedHashSet<String>();

        String localTime = new String();
        if (id2 != null) {
            localTime = id2.get("LastUpdatedDateTime").toString();
        }

        Map<String, String> configurationmastersTableMap = new HashMap<String, String>();

        for (int i = 0; i < combinations.length; ++i) {
            String[] key_val = combinations[i].split(":");
            if (key_val[0].equalsIgnoreCase("downloadServerKeys")) {
                downloadServerKeys = key_val[1];
            }
            if (!key_val[0].equalsIgnoreCase("downloadServerKeys")) {
                configurationmastersTableMap.put(ODataQueryConstants.FILTER, key_val[0] + " eq '" + key_val[1] + "'");
            }
        }

        String readBundleResponse = Executor.invokeService(ServiceURLEnum.CONFIGURATIONSBUNDLE_READ,
                configurationmastersTableMap, null, requestInstance);
        JSONObject readBundleResponseJSON = CommonUtilities.getStringAsJSONObject(readBundleResponse);

        if (readBundleResponseJSON != null && readBundleResponseJSON.has(FabricConstants.OPSTATUS)
                && readBundleResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readBundleResponseJSON.has("configurationbundles")) {

            JSONArray readBundleResponseJSONArray = readBundleResponseJSON.getJSONArray("configurationbundles");

            for (int i = 0; i < readBundleResponseJSONArray.length(); ++i) {

                JSONObject readBundleResponseJSONObject = readBundleResponseJSONArray.getJSONObject(i);

                String[] bundles = readBundleResponseJSONObject.get("bundle_id").toString().split(",");
                for (int x = 0; x < bundles.length; x++) {
                    if ((bundles_in_combination.isEmpty()) || (!bundles_in_combination.contains(bundles[x]))) {
                        bundles_in_combination.add(bundles[x]);
                        if ((finalBundles.isEmpty()) || (!finalBundles.contains(bundles[x]))) {
                            finalBundles.add(bundles[x]);
                        }

                    }
                }
            }
        }

        finalBundlesRecord.addParam(new Param("bundles", StringUtils.join(finalBundles, ","), "JSON"));
        finalBundlesRecord.addParam(new Param("downloadServerKeys", downloadServerKeys, "JSON"));
        String[] finalBundlesString = StringUtils.join(finalBundles, ",").split(",");

        for (int bundle_num = 0; bundle_num < finalBundlesString.length; bundle_num++) {
            if (finalBundlesString[bundle_num].length() != 0) {
                JSONArray updatedConfigBundleReturned = getConfigurations(finalBundlesString[bundle_num],
                        requestInstance);
                String serverTime = updatedConfigBundleReturned.getJSONObject(0).get("lastmodifiedts").toString();

                String updatedTime = new String();
                if (id2 != null)
                    updatedTime = getUpdateTime(serverTime, localTime);
                if (updatedTime != null) {

                    Dataset bundleDataset = new Dataset();
                    bundleDataset.setId(finalBundlesString[bundle_num]);

                    for (int i = 0; i < updatedConfigBundleReturned.length(); ++i) {

                        JSONObject backendJsonObj = updatedConfigBundleReturned.getJSONObject(i);
                        Record configurationRecord = new Record();

                        configurationRecord.addParam(
                                new Param("key", backendJsonObj.get("config_key").toString(), FabricConstants.STRING));
                        configurationRecord.addParam(new Param("value", backendJsonObj.get("config_value").toString(),
                                FabricConstants.STRING));
                        configurationRecord.addParam(new Param("type", backendJsonObj.get("config_type").toString(),
                                FabricConstants.STRING));
                        configurationRecord.addParam(
                                new Param("target", backendJsonObj.get("target").toString(), FabricConstants.STRING));
                        configurationRecord.addParam(new Param("description",
                                backendJsonObj.get("description").toString(), FabricConstants.STRING));

                        bundleDataset.addRecord(configurationRecord);
                    }

                    updatedConfigurations.addDataset(bundleDataset);
                }

                if (tempTime == null) {
                    tempTime = serverTime;

                } else {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.ENGLISH);
                    Date da = df.parse(tempTime);
                    double timestamp = Math.floor(da.getTime() / 1000D);

                    SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.ENGLISH);
                    Date da1 = df1.parse(serverTime);
                    double timestamp1 = Math.floor(da1.getTime() / 1000D);

                    int resVal = Double.compare(timestamp, timestamp1);
                    if (resVal < 0) {
                        tempTime = serverTime;
                    }
                }
                greatest_timestamp_String = tempTime;
            }
        }

        timeStamp.addParam(new Param("LastUpdatedDateTime", greatest_timestamp_String, "JSON"));

        recordResult.addRecord(timeStamp);
        recordResult.addRecord(updatedConfigurations);
        recordResult.addRecord(finalBundlesRecord);
        result.addRecord(recordResult);
        return result;
    }

    public JSONArray getConfigurations(String bundle_id, DataControllerRequest requestInstance) throws Exception {

        JSONArray readConfigurationsResponseJSONArray = null;

        // ** Reading entries from 'configurations' table **
        Map<String, String> configurationsTableMap = new HashMap<String, String>();
        configurationsTableMap.put(ODataQueryConstants.FILTER, "bundle_id eq '" + bundle_id + "'");

        String readConfigurationsResponse = Executor.invokeService(ServiceURLEnum.CONFIGURATION_READ,
                configurationsTableMap, null, requestInstance);
        JSONObject readConfigurationsResponseJSON = CommonUtilities.getStringAsJSONObject(readConfigurationsResponse);

        if (readConfigurationsResponseJSON != null && readConfigurationsResponseJSON.has(FabricConstants.OPSTATUS)
                && readConfigurationsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readConfigurationsResponseJSON.has("configurations")) {
            readConfigurationsResponseJSONArray = readConfigurationsResponseJSON.getJSONArray("configurations");
        }

        return readConfigurationsResponseJSONArray;
    }

    public String getUpdateTime(String serverTime, String localTime) throws ParseException {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.ENGLISH);
        Date da = df.parse(serverTime);
        Date db = df.parse(localTime);
        double serverTimeStamp = Math.floor(da.getTime() / 1000D);
        double localTimeStamp = Math.floor(db.getTime() / 1000D);
        int resval = Double.compare(serverTimeStamp, localTimeStamp);
        if (resval > 0) {
            return serverTime;
        }
        return null;
    }
}
