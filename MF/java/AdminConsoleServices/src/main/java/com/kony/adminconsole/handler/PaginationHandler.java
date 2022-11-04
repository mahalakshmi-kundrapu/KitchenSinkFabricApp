/**
 * 
 */
package com.kony.adminconsole.handler;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * 
 * Handler to fetch records. Supports pagination and sorting
 * 
 * @author Aditya Mankal
 * 
 * 
 */
public class PaginationHandler {

    private static final int DEFAULT_COUNT_OF_RECORDS_TO_FETCH = 100;
    private static final Logger LOG = Logger.getLogger(PaginationHandler.class);
    public static final String IS_LAST_PAGE_KEY = "isLastPage";
    public static final String RECORDS_PER_PAGE_KEY = "recordsPerPage";

    public static void setOffset(DataControllerRequest requestInstance, Map<String, String> postParametersMap) {
        String _$top = requestInstance.getParameter(ODataQueryConstants.TOP);
        String _$skip = requestInstance.getParameter(ODataQueryConstants.SKIP);

        LOG.debug("$top value:" + _$top);
        LOG.debug("$skip value:" + _$skip);

        if (StringUtils.isNotBlank(_$top)) {
            postParametersMap.put(ODataQueryConstants.TOP, _$top);
        }
        if (StringUtils.isNotBlank(_$skip)) {
            postParametersMap.put(ODataQueryConstants.SKIP, _$skip);
        }

    }

    public static JSONObject getPaginatedData(ServiceURLEnum serviceURLEnum, Map<String, String> oDataQueryMap,
            Map<String, String> headerMap, DataControllerRequest requestInstance) {

        LOG.debug("Get Service URL:" + serviceURLEnum.getServiceURL());

        try {
            if (oDataQueryMap.containsKey(ODataQueryConstants.TOP)) {
                oDataQueryMap.put(ODataQueryConstants.TOP,
                        String.valueOf(Integer.parseInt(oDataQueryMap.get(ODataQueryConstants.TOP)) + 1));
            }
        } catch (NumberFormatException e) {
            LOG.error(e);
            oDataQueryMap.put(ODataQueryConstants.TOP, String.valueOf(DEFAULT_COUNT_OF_RECORDS_TO_FETCH));
        }
        String getResponse = Executor.invokeService(serviceURLEnum, oDataQueryMap, headerMap, requestInstance);
        JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(getResponse);
        if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
                && responseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            LOG.debug("Response Received. Call Successful. Opstatus value : 0");
            try {
                if (oDataQueryMap.containsKey(ODataQueryConstants.TOP)) {
                    int recordsPerPage = Integer.parseInt(oDataQueryMap.get(ODataQueryConstants.TOP)) - 1;
                    responseJSON.put("recordsPerPage", recordsPerPage);
                    JSONArray recordsJSONArray = null;
                    for (String currKey : responseJSON.keySet()) {
                        if (responseJSON.get(currKey) instanceof JSONArray) {
                            recordsJSONArray = responseJSON.getJSONArray(currKey);
                            break;
                        }
                    }
                    if (recordsJSONArray == null) {
                        LOG.error("Records JSON Array missing in the response");
                        return responseJSON;
                    }
                    int countOfFetchedRecords = recordsJSONArray.length();
                    if (countOfFetchedRecords > recordsPerPage) {
                        responseJSON.put(IS_LAST_PAGE_KEY, false);
                        recordsJSONArray.remove(recordsJSONArray.length() - 1);
                    } else {
                        responseJSON.put(IS_LAST_PAGE_KEY, true);
                    }
                    LOG.debug(IS_LAST_PAGE_KEY + responseJSON.optString(IS_LAST_PAGE_KEY));
                    return responseJSON;
                }
            } catch (Exception e) {
                LOG.error(e);
            }
        }
        LOG.error("Response Received. Call Failed. Opstatus value : Non Zero/Not Found");
        return responseJSON;
    }

    public static void addPaginationMetadataToRecordObject(Record operationRecord,
            JSONObject paginationHandlerResponseJSON) {
        if (operationRecord == null || paginationHandlerResponseJSON == null
                || !paginationHandlerResponseJSON.has(IS_LAST_PAGE_KEY)) {
            return;
        }
        operationRecord.addParam(new Param(IS_LAST_PAGE_KEY, paginationHandlerResponseJSON.optString(IS_LAST_PAGE_KEY),
                FabricConstants.STRING));
    }

    public static void addPaginationMetadataToResultObject(Result processedResult,
            JSONObject paginationHandlerResponseJSON) {
        if (processedResult == null || paginationHandlerResponseJSON == null) {
            return;
        }
        if (paginationHandlerResponseJSON.has(IS_LAST_PAGE_KEY)) {
            processedResult.addParam(new Param(IS_LAST_PAGE_KEY,
                    paginationHandlerResponseJSON.optString(IS_LAST_PAGE_KEY), FabricConstants.STRING));
        }
        if (paginationHandlerResponseJSON.has(RECORDS_PER_PAGE_KEY)) {
            processedResult.addParam(new Param(RECORDS_PER_PAGE_KEY,
                    paginationHandlerResponseJSON.optString(RECORDS_PER_PAGE_KEY), FabricConstants.STRING));
        }
    }
}
