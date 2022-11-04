package com.kony.logservices.service;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.JSONUtils;
import com.kony.logservices.core.AbstractLogJavaService;
import com.kony.logservices.dao.LogDAO;
import com.kony.logservices.dto.AdminActivityDTO;
import com.kony.logservices.dto.PaginationDTO;
import com.kony.logservices.util.ErrorCodeEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class AdminConsoleLogsGetService extends AbstractLogJavaService {

    private static final Logger LOG = Logger.getLogger(AdminConsoleLogsGetService.class);

    @Override
    public Object execute(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result result = new Result();
        try {
            String searchText = null;
            String moduleName = null;
            String startDate = null;
            String endDate = null;
            int pageNumber = 1;
            int noOfRecords = 10;
            String sortBy = null;
            String sortDirection = null;
            JSONArray event = null;
            ArrayList<String> eventTypeArray = null;
            JSONArray userRole = null;
            ArrayList<String> userRoleTypeArray = null;
            JSONArray status = null;
            ArrayList<String> statusTypeArray = null;
            JSONObject filterDataJSON = CommonUtilities
                    .getStringAsJSONObject(requestInstance.getParameter("FilterData"));
            if (filterDataJSON.has("Event")) {
                event = filterDataJSON.getJSONArray("Event");
                eventTypeArray = new ArrayList<String>(event.length());
                for (int i = 0; i < event.length(); i++) {
                    eventTypeArray.add(event.getString(i));
                }
            }
            if (filterDataJSON.has("UserRole")) {
                userRole = filterDataJSON.getJSONArray("UserRole");
                userRoleTypeArray = new ArrayList<String>(userRole.length());
                for (int i = 0; i < userRole.length(); i++) {
                    userRoleTypeArray.add(userRole.getString(i));
                }
            }
            if (filterDataJSON.has("Status")) {
                status = filterDataJSON.getJSONArray("Status");
                statusTypeArray = new ArrayList<String>(status.length());
                for (int i = 0; i < status.length(); i++) {
                    statusTypeArray.add(status.getString(i));
                }
            }
            if (filterDataJSON.has("StartDate")) {
                startDate = filterDataJSON.getString("StartDate");
            }
            if (filterDataJSON.has("SearchText")) {
                searchText = filterDataJSON.getString("SearchText");
            }
            if (filterDataJSON.has("ModuleName")) {
                moduleName = filterDataJSON.getString("ModuleName");
            }
            if (filterDataJSON.has("EndDate")) {
                endDate = filterDataJSON.getString("EndDate");
            }
            if (filterDataJSON.has("PageNumber")) {
                pageNumber = filterDataJSON.getInt(("PageNumber"));
            }
            if (filterDataJSON.has("NoOfRecords")) {
                noOfRecords = filterDataJSON.getInt(("NoOfRecords"));
            }
            if (filterDataJSON.has("SortBy")) {
                sortBy = filterDataJSON.getString("SortBy");
            }
            if (filterDataJSON.has("SortDirection")) {
                sortDirection = filterDataJSON.getString("SortDirection");
            }
            PaginationDTO<AdminActivityDTO> adminConsoleActivity = LogDAO.getPaginatedAdminConsoleLogs(searchText,
                    moduleName, startDate, endDate, sortBy, sortDirection, pageNumber, noOfRecords, eventTypeArray,
                    userRoleTypeArray, statusTypeArray);
            String resultJSON = JSONUtils.stringify(adminConsoleActivity);

            JSONObject resultJSONObject = new JSONObject(resultJSON);
            return CommonUtilities.constructResultFromJSONObject(resultJSONObject);
        } catch (Exception e) {
            LOG.error("Failed to get Admin Console Logs", e);
            ErrorCodeEnum.ERR_29006.setErrorCode(result);
        }
        return result;
    }

}
