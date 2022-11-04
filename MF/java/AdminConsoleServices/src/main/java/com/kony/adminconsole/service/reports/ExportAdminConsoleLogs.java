package com.kony.adminconsole.service.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.dto.ExportColumnDetailsBean;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.ExcelGenerator;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class ExportAdminConsoleLogs extends ExcelGenerator implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(ExportAdminConsoleLogs.class);
    private static final String REPORT_TITLE = "Admin Console Logs";

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result result = new Result();
        try {
            Map<String, String> queryParamsMap = (Map<String, String>) requestInstance.getAttribute("queryparams");
            CommonUtilities.getAuthToken(requestInstance);

            JSONObject filterData = new JSONObject();
            StringBuilder filters = new StringBuilder();

            // construct filter conditions
            if (queryParamsMap.containsKey("SearchText")) {
                filterData.put("SearchText", queryParamsMap.get("SearchText"));
                filters.append("Username STARTS WITH \"").append(queryParamsMap.get("SearchText")).append("\" AND ");
            }
            if (StringUtils.isNotBlank(queryParamsMap.get("ModuleName"))) {
                filterData.put("ModuleName", queryParamsMap.get("ModuleName"));
                filters.append("Module EQUALS \"").append(queryParamsMap.get("ModuleName")).append("\" AND ");
            }
            if (queryParamsMap.containsKey("StartDate") && queryParamsMap.containsKey("EndDate")) {
                filterData.put("StartDate", queryParamsMap.get("StartDate"));
                filterData.put("EndDate", queryParamsMap.get("EndDate"));
                filters.append("Date BETWEEN \"").append(queryParamsMap.get("StartDate")).append(" - ")
                        .append(queryParamsMap.get("EndDate")).append("\" AND ");
            }
            if (queryParamsMap.containsKey("Event")) {
                JSONArray event = CommonUtilities.getStringAsJSONArray(queryParamsMap.get("Event"));
                if (event.length() > 0) {
                    filterData.put("Event", event);
                    filters.append("Event IN (");
                    for (int i = 0; i < event.length(); i++) {
                        filters.append(event.getString(i));
                    }
                    filters.append(") AND ");
                }
            }
            if (queryParamsMap.containsKey("UserRole")) {
                JSONArray userRole = CommonUtilities.getStringAsJSONArray(queryParamsMap.get("UserRole"));
                if (userRole.length() > 0) {
                    filterData.put("User Role", userRole);
                    filters.append("UserRole IN (");
                    for (int i = 0; i < userRole.length(); i++) {
                        filters.append(userRole.getString(i));
                    }
                    filters.append(") AND ");
                }
            }
            if (queryParamsMap.containsKey("Status")) {
                JSONArray status = CommonUtilities.getStringAsJSONArray(queryParamsMap.get("Status"));
                if (status.length() > 0) {
                    filterData.put("Status", status);
                    filters.append("Status IN (");
                    for (int i = 0; i < status.length(); i++) {
                        filters.append(status);
                    }
                    filters.append(") AND ");
                }
            }
            if (filters.length() > 0) {
                filters.delete(filters.lastIndexOf("AND"), filters.length());
            }
            if (queryParamsMap.containsKey("SortBy")) {
                filterData.put("SortBy", queryParamsMap.containsKey("SortBy"));
            }
            if (queryParamsMap.containsKey("SortDirection")) {
                filterData.put("SortDirection", queryParamsMap.containsKey("SortDirection"));
            }
            // setting pageNumber and NoOfRecords to zero
            // as all the data has to exported
            filterData.put("PageNumber", 0);
            filterData.put("NoOfRecords", 0);
            Map<String, String> postParametersMap = new HashMap<String, String>();
            postParametersMap.put("FilterData", filterData.toString());

            // fetch logs data
            String readAdminConsoleLogsResponse = Executor.invokeService(ServiceURLEnum.ADMINCONSOLELOGS_READ,
                    postParametersMap, null, requestInstance);
            JSONObject readResponse = CommonUtilities.getStringAsJSONObject(readAdminConsoleLogsResponse);
            JSONArray logsArray = readResponse.optJSONArray("logs");

            // get reported generated by from login
            StringBuilder generatedBy = new StringBuilder();
            UserDetailsBean loggedInUserDetails = null;
            try {
                loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            } catch (Exception e) {
                LOG.error("Failed to fetch details from identity scope");
            }

            if (loggedInUserDetails != null) {
                generatedBy.append(StringUtils.defaultString(loggedInUserDetails.getFirstName())).append(" ");
                generatedBy.append(StringUtils.defaultString(loggedInUserDetails.getMiddleName())).append(" ");
                generatedBy.append(StringUtils.defaultString(loggedInUserDetails.getLastName()));
            }
            Integer offset = queryParamsMap.containsKey("offset") ? Integer.parseInt(queryParamsMap.get("offset")) : 0;

            // get headers
            List<ExportColumnDetailsBean> fieldList = getHeaders();

            // write to excel
            byte[] bytes = generateExcel(logsArray, REPORT_TITLE, generatedBy.toString(), offset, fieldList,
                    filters.toString());
            Map<String, String> customHeaders = new HashMap<String, String>();
            customHeaders.put("Content-Type", "application/vnd.ms-excel");
            customHeaders.put("Content-Disposition", "attachment; filename=\"AdminConsoleLogs.xlsx\"");
            responseInstance.setAttribute(FabricConstants.CHUNKED_RESULTS_IN_JSON,
                    new BufferedHttpEntity(new ByteArrayEntity(bytes)));
            responseInstance.setStatusCode(HttpStatus.SC_OK);
            responseInstance.getHeaders().putAll(customHeaders);

        } catch (Exception e) {
            LOG.error("Failed while downloading Admin Console Logs", e);
            ErrorCodeEnum.ERR_20687.setErrorCode(result);

            String errorMessage = "Failed to download admin console logs. Please contact administrator.";
            CommonUtilities.fileDownloadFailure(responseInstance, errorMessage);
        }
        return result;
    }

    private List<ExportColumnDetailsBean> getHeaders() {
        List<ExportColumnDetailsBean> fieldList = new ArrayList<ExportColumnDetailsBean>();
        fieldList.add(new ExportColumnDetailsBean(0, "EVENT", "event"));
        fieldList.add(new ExportColumnDetailsBean(1, "USERNAME", "username"));
        fieldList.add(new ExportColumnDetailsBean(2, "USER ROLE", "userRole"));
        fieldList.add(new ExportColumnDetailsBean(3, "MODULE NAME", "moduleName"));
        fieldList.add(new ExportColumnDetailsBean(4, "STATUS", "status"));
        fieldList.add(new ExportColumnDetailsBean(5, "DATE & TIME", "eventts", "date"));
        fieldList.add(new ExportColumnDetailsBean(6, "DESCRIPTION", "description"));
        fieldList.sort(null);
        return fieldList;
    }

}