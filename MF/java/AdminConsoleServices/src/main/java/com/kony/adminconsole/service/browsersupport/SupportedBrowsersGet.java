package com.kony.adminconsole.service.browsersupport;

/**
 * Service to fetch supported browsers for OLB
 *
 * @author Alahari Prudhvi Akhil
 * 
 */
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
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
import com.konylabs.middleware.dataobject.Result;

public class SupportedBrowsersGet implements JavaService2 {

    private static final String DEFAULT_LOCALE = "en-US";
    private static final Logger LOG = Logger.getLogger(SupportedBrowsersGet.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        String acceptLanguage = requestInstance.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        LOG.debug("Received Accept-Language Header:" + acceptLanguage);

        if (StringUtils.isBlank(acceptLanguage)) {
            acceptLanguage = DEFAULT_LOCALE;
        }
        if (acceptLanguage.contains("_")) {
            acceptLanguage = acceptLanguage.replace('_', '-');
        }

        JSONArray browserSupportArray = getBrowserData(requestInstance, processedResult);
        JSONArray mutilLingualDisplayNames = getBrowserDisplayContent(acceptLanguage, requestInstance, processedResult);
        if (browserSupportArray == null || mutilLingualDisplayNames == null) {
            return processedResult;
        }

        Map<String, String> displayContentMap = new HashMap<>();
        mutilLingualDisplayNames.forEach((displayObject) -> {
            JSONObject displayJSONObject = (JSONObject) displayObject;
            displayContentMap.put(
                    displayJSONObject.getString("BrowserSupport_id") + displayJSONObject.getString("Locale_id"),
                    displayJSONObject.getString("displayNameText"));
        });

        Dataset browserDataset = new Dataset();
        browserDataset.setId("browsersupport");

        for (int ctr = 0; ctr < browserSupportArray.length(); ctr++) {
            JSONObject browserJSONObject = browserSupportArray.getJSONObject(ctr);
            if (displayContentMap.containsKey(browserJSONObject.getString("id") + acceptLanguage)) {
                browserJSONObject.put("displayName",
                        displayContentMap.get(browserJSONObject.getString("id") + acceptLanguage));
            } else {
                browserJSONObject.put("displayName",
                        displayContentMap.get(browserJSONObject.getString("id") + DEFAULT_LOCALE));
            }
            browserDataset.addRecord(CommonUtilities.constructRecordFromJSONObject(browserJSONObject));
        }

        processedResult.addDataset(browserDataset);
        return processedResult;
    }

    private static JSONArray getBrowserData(DataControllerRequest requestInstance, Result processedResult) {
        Map<String, String> postParametersMap = new HashMap<>();
        String endpointResponse = Executor.invokeService(ServiceURLEnum.BROWSERSUPPORT_READ, postParametersMap, null,
                requestInstance);
        JSONObject response = CommonUtilities.getStringAsJSONObject(endpointResponse);
        if (response == null || !response.has(FabricConstants.OPSTATUS)
                || response.getInt(FabricConstants.OPSTATUS) != 0 || !response.has("browsersupport")) {
            ErrorCodeEnum.ERR_21405.setErrorCode(processedResult);
            return null;
        }
        return response.getJSONArray("browsersupport");
    }

    private static JSONArray getBrowserDisplayContent(String acceptLanguage, DataControllerRequest requestInstance,
            Result processedResult) {
        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put(ODataQueryConstants.FILTER,
                "Locale_id eq '" + acceptLanguage + "' or Locale_id eq '" + DEFAULT_LOCALE + "'");

        String endpointResponse = Executor.invokeService(ServiceURLEnum.BROWSERSUPPORTDISPLAYNAMETEXT_READ,
                postParametersMap, null, requestInstance);
        JSONObject response = CommonUtilities.getStringAsJSONObject(endpointResponse);
        if (response == null || !response.has(FabricConstants.OPSTATUS)
                || response.getInt(FabricConstants.OPSTATUS) != 0 || !response.has("browsersupportdisplaynametext")) {
            ErrorCodeEnum.ERR_21406.setErrorCode(processedResult);
            return null;
        }
        return response.getJSONArray("browsersupportdisplaynametext");
    }

}
