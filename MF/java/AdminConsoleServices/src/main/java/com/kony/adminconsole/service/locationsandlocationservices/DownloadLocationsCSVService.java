package com.kony.adminconsole.service.locationsandlocationservices;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class DownloadLocationsCSVService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(DownloadLocationsCSVService.class);

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result processedResult = new Result();

        Map<String, String> queryParamsMap = (Map<String, String>) requestInstance.getAttribute("queryparams");
        String locationfileId = queryParamsMap.containsKey("locationfileId") ? queryParamsMap.get("locationfileId")
                : null;

        try {

            if (locationfileId != null && locationfileId.equals("locationTemplate")) {
                Map<String, String> customHeaders = new HashMap<String, String>();
                customHeaders.put("Content-Type", "text/plain; charset=utf-8");
                customHeaders.put("Content-Disposition", "attachment; filename=\"locationTemplate.csv\"");

                responseInstance.setAttribute(FabricConstants.CHUNKED_RESULTS_IN_JSON,
                        new BufferedHttpEntity(new InputStreamEntity(
                                this.getClass().getClassLoader().getResourceAsStream("locationTemplate.csv"))));
                responseInstance.getHeaders().putAll(customHeaders);
            }

            else {
                String locationfileclob = "";

                // ** Reading entry from 'locationfile' table **
                Map<String, String> locationfileTableMap = new HashMap<String, String>();
                locationfileTableMap.put(ODataQueryConstants.SELECT, "locationfileclob");
                locationfileTableMap.put(ODataQueryConstants.FILTER, "id eq '" + locationfileId + "'");

                String readLocationfileResponse = Executor.invokeService(ServiceURLEnum.LOCATIONFILE_READ,
                        locationfileTableMap, null, requestInstance);
                JSONObject readLocationfileJSON = CommonUtilities.getStringAsJSONObject(readLocationfileResponse);

                if (readLocationfileJSON.getInt(FabricConstants.OPSTATUS) == 0
                        && readLocationfileJSON.getJSONArray("locationfile") != null) {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOCATIONS, EventEnum.DOWNLOADFILE,
                            ActivityStatusEnum.SUCCESSFUL, "CSV file downloaded successfully");
                    locationfileclob = readLocationfileJSON.getJSONArray("locationfile").getJSONObject(0)
                            .getString("locationfileclob");
                } else {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOCATIONS, EventEnum.DOWNLOADFILE,
                            ActivityStatusEnum.FAILED, "CSV file download failed");

                }

                Map<String, String> customHeaders = new HashMap<String, String>();
                customHeaders.put("Content-Type", "text/plain; charset=utf-8");
                customHeaders.put("Content-Disposition", "attachment; filename=\"branchAtmCSV.csv\"");

                responseInstance.setAttribute(FabricConstants.CHUNKED_RESULTS_IN_JSON,
                        new BufferedHttpEntity(new StringEntity(locationfileclob, StandardCharsets.UTF_8)));
                responseInstance.getHeaders().putAll(customHeaders);
                responseInstance.setStatusCode(HttpStatus.SC_OK);
            }
        }

        catch (Exception e) {
            LOG.error("Failed while downloading Locations CSV file", e);
            ErrorCodeEnum.ERR_20687.setErrorCode(processedResult);

            String errorMessage = "Failed to download Locations CSV file. Please contact administrator.";
            CommonUtilities.fileDownloadFailure(responseInstance, errorMessage);
        }

        return processedResult;
    }
}