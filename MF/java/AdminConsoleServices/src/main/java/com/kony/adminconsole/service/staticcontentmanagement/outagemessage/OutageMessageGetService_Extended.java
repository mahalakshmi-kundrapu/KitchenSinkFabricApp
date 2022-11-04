package com.kony.adminconsole.service.staticcontentmanagement.outagemessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.config.EnvironmentConfiguration;
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
 * Service to retrieve the Outage Messages of Deactivated Services
 *
 * @author Aditya Mankal
 * 
 */
public class OutageMessageGetService_Extended implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(OutageMessageGetService_Extended.class);
    public static final String PATTERN_YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();
            String clientAppId = requestInstance.getParameter("appid");

            Date currentTime = new Date();
            SimpleDateFormat dateFormatter = new SimpleDateFormat(PATTERN_YYYY_MM_DD_T_HH_MM_SS);
            String currentDateStr = dateFormatter.format(currentTime);

            Map<String, String> postParametersMap = new HashMap<String, String>();
            String filter = "Status_id eq 'SID_OUTAGE_SCHEDULED_ACTIVE_COMPLETED' and ( startTime le '" + currentDateStr
                    + "' and endTime ge '" + currentDateStr + "')";

            if (StringUtils.isNotBlank(clientAppId)) {
                JSONObject clientAppIdToServerMap = getClientAppIdMap(requestInstance);
                if (clientAppIdToServerMap == null) {
                    ErrorCodeEnum.ERR_20887.setErrorCode(processedResult);
                    return processedResult;
                }
                String serverAppId = null;
                if (clientAppIdToServerMap.has(clientAppId)) {
                    serverAppId = clientAppIdToServerMap.getString(clientAppId);
                } else {
                    ErrorCodeEnum.ERR_20888.setErrorCode(processedResult);
                    return processedResult;
                }
                filter += " and substringof('app_id', '" + serverAppId + "') eq true";
            }

            postParametersMap.put(ODataQueryConstants.FILTER, filter);
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
            processedResult.addParam(new Param("FailureReason", readOutageMessageResponse, FabricConstants.STRING));
            ErrorCodeEnum.ERR_20181.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }
    
    public static JSONObject getClientAppIdMap(DataControllerRequest requestInstance) {
        try {
            String clientAppIdMapping = EnvironmentConfiguration.AC_APPID_TO_APP_MAPPING.getValue(requestInstance);
            if (StringUtils.isNotBlank(clientAppIdMapping)) {
                JSONObject clientAppIdMappingJson = new JSONObject(clientAppIdMapping);
                return clientAppIdMappingJson;
            }
        } catch (Exception e) {
            LOG.error("Failed while parsing runtime configuration AC_APPID_TO_APP_MAPPING", e);
        }
        return null;
    }

}