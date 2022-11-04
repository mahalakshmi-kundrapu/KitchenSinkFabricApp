package com.kony.adminconsole.service.servicemanagement;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.json.JSONArray;
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

public class DownloadServicesList implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(DownloadServicesList.class);

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();

        try {

            CommonUtilities.getAuthToken(requestInstance);

            Map<String, String> queryParamsMap = (Map<String, String>) requestInstance.getAttribute("queryparams");
            String searchText = queryParamsMap.containsKey("searchText") ? queryParamsMap.get("searchText") : null;
            String status = queryParamsMap.containsKey("status") ? queryParamsMap.get("status") : null;

            // ** Reading entries from 'service_view' view **
            Map<String, String> serviceViewMap = new HashMap<String, String>();
            serviceViewMap.put(ODataQueryConstants.SELECT, "Name, Code, Type_Name, Category_Name, Channel, Status");

            StringBuilder filterString = new StringBuilder();

            if (status != null) {
                String[] statuses = status.split("_");
                filterString.append("(");
                for (int i = 0; i < statuses.length - 1; ++i) {
                    filterString.append("Status eq '" + statuses[i] + "'");
                    filterString.append(" or ");
                }
                filterString.append("Status eq '" + statuses[statuses.length - 1] + "')");
            }

            if (filterString != null) {
                serviceViewMap.put(ODataQueryConstants.FILTER, filterString.toString());
            }

            serviceViewMap.put(ODataQueryConstants.ORDER_BY, "Name");
            String serviceResponse = Executor.invokeService(ServiceURLEnum.SERVICE_VIEW_READ, serviceViewMap, null,
                    requestInstance);
            JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);

            if (serviceResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && serviceResponseJSON.getJSONArray("service_view") != null) {

                StringBuilder responseCsvBuilder = new StringBuilder(); // Contains the text for response CSV file
                CSVPrinter responseCsvPrinter = CSVFormat.DEFAULT
                        .withHeader("Name", "Code", "Type", "Category", "Supported Channels", "Status")
                        .print(responseCsvBuilder);

                JSONArray services = serviceResponseJSON.getJSONArray("service_view");

                for (int i = 0; i < services.length(); ++i) {

                    String nameColumn = services.getJSONObject(i).optString("Name");
                    String codeColumn = services.getJSONObject(i).optString("Code");
                    String typeColumn = services.getJSONObject(i).optString("Type_Name");
                    String categoryColumn = services.getJSONObject(i).optString("Category_Name");
                    String channelColumn = services.getJSONObject(i).optString("Channel");
                    String statusColumn = services.getJSONObject(i).optString("Status");

                    if (searchText == null
                            || (searchText != null && (nameColumn.toLowerCase().contains(searchText.toLowerCase())))) {
                        responseCsvPrinter.printRecord(nameColumn, codeColumn, typeColumn, categoryColumn,
                                channelColumn, statusColumn);
                    }
                }

                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SERVICES, EventEnum.DOWNLOADFILE,
                        ActivityStatusEnum.SUCCESSFUL, "Services file download successful");

                Map<String, String> customHeaders = new HashMap<String, String>();
                customHeaders.put("Content-Type", "text/plain; charset=utf-8");
                customHeaders.put("Content-Disposition", "attachment; filename=\"Services_List.csv\"");

                responseInstance.setAttribute(FabricConstants.CHUNKED_RESULTS_IN_JSON, new BufferedHttpEntity(
                        new StringEntity(responseCsvBuilder.toString(), StandardCharsets.UTF_8)));
                responseInstance.getHeaders().putAll(customHeaders);
                responseInstance.setStatusCode(HttpStatus.SC_OK);
            } else {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SERVICES, EventEnum.DOWNLOADFILE,
                        ActivityStatusEnum.FAILED, "Services file download failed");
            }

        } catch (Exception e) {
            LOG.error("Failed while downloading services list", e);
            ErrorCodeEnum.ERR_20687.setErrorCode(result);

            String errorMessage = "Failed to download services list. Please contact administrator.";
            CommonUtilities.fileDownloadFailure(responseInstance, errorMessage);
        }
        return result;
    }
}