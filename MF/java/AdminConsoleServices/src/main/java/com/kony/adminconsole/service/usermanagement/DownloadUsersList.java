package com.kony.adminconsole.service.usermanagement;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

public class DownloadUsersList implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(DownloadUsersList.class);

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();

        try {

            CommonUtilities.getAuthToken(requestInstance);

            Map<String, String> queryParamsMap = (Map<String, String>) requestInstance.getAttribute("queryparams");
            String searchText = queryParamsMap.containsKey("searchText") ? queryParamsMap.get("searchText") : null;
            String role = queryParamsMap.containsKey("role") ? queryParamsMap.get("role") : null;
            String status = queryParamsMap.containsKey("status") ? queryParamsMap.get("status") : null;
            String createdStartDate = queryParamsMap.containsKey("createdStartDate")
                    ? queryParamsMap.get("createdStartDate")
                    : null;
            String createdEndDate = queryParamsMap.containsKey("createdEndDate") ? queryParamsMap.get("createdEndDate")
                    : null;
            String updatedStartDate = queryParamsMap.containsKey("updatedStartDate")
                    ? queryParamsMap.get("updatedStartDate")
                    : null;
            String updatedEndDate = queryParamsMap.containsKey("updatedEndDate") ? queryParamsMap.get("updatedEndDate")
                    : null;

            // ** Reading entries from 'internalusers_view' view **
            Map<String, String> internalUsersViewMap = new HashMap<String, String>();
            internalUsersViewMap.put(ODataQueryConstants.SELECT,
                    "FirstName, LastName, Username, Email, Role_Name, Permission_Count, Status_Desc");

            StringBuilder filterString = new StringBuilder();

            if (searchText == null && status == null) {
                filterString.append("(Status_Desc eq 'Active' or Status_Desc eq 'Suspended')");
            } else {
                String[] statuses = status.split("_");
                filterString.append("(");
                for (int i = 0; i < statuses.length - 1; ++i) {
                    if (statuses[i].equals("Disabled")) {
                        filterString.append("Status_Desc eq 'Inactive'");
                    } else {
                        filterString.append("Status_Desc eq '" + statuses[i] + "'");
                    }
                    filterString.append(" or ");
                }
                if (statuses[statuses.length - 1].equals("Disabled")) {
                    filterString.append("Status_Desc eq 'Inactive')");
                } else {
                    filterString.append("Status_Desc eq '" + statuses[statuses.length - 1] + "')");
                }
            }

            if (role != null) {
                if (filterString != null) {
                    filterString.append(" and ");
                }
                String[] roles = role.split("_");
                filterString.append("(");
                for (int i = 0; i < roles.length - 1; ++i) {
                    filterString.append("Role_Name eq '" + roles[i] + "'");
                    filterString.append(" or ");
                }
                filterString.append("Role_Name eq '" + roles[roles.length - 1] + "')");
            }
            if (createdStartDate != null && createdEndDate != null) {
                if (filterString != null) {
                    filterString.append(" and ");
                }
                filterString.append("createdts ge '" + getStartDateInOAuthFormat(createdStartDate)
                        + "' and createdts le '" + getEndDateInOAuthFormat(createdEndDate) + "'");
            }
            if (updatedStartDate != null && updatedEndDate != null) {
                if (filterString != null) {
                    filterString.append(" and ");
                }
                filterString.append("lastmodifiedts ge '" + getStartDateInOAuthFormat(updatedStartDate)
                        + "' and lastmodifiedts le '" + getEndDateInOAuthFormat(updatedEndDate) + "'");
            }

            if (filterString != null) {
                internalUsersViewMap.put(ODataQueryConstants.FILTER, filterString.toString());
            }

            String readInternalUsersViewResponse = Executor.invokeService(ServiceURLEnum.INTERNALUSERS_VIEW_READ,
                    internalUsersViewMap, null, requestInstance);
            JSONObject readInternalUsersViewResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readInternalUsersViewResponse);

            if (readInternalUsersViewResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readInternalUsersViewResponseJSON.getJSONArray("internalusers_view") != null) {

                StringBuilder responseCsvBuilder = new StringBuilder(); // Contains the text for response CSV file
                CSVPrinter responseCsvPrinter = CSVFormat.DEFAULT
                        .withHeader("Name", "Username", "Email", "Role", "No. of Permissions", "Status")
                        .print(responseCsvBuilder);

                JSONArray internalUsers = readInternalUsersViewResponseJSON.getJSONArray("internalusers_view");

                for (int i = 0; i < internalUsers.length(); ++i) {

                    String nameColumn = internalUsers.getJSONObject(i).optString("FirstName") + " "
                            + internalUsers.getJSONObject(i).optString("LastName");
                    String userNameColumn = internalUsers.getJSONObject(i).optString("Username");
                    String emailColumn = internalUsers.getJSONObject(i).optString("Email");
                    String roleColumn = internalUsers.getJSONObject(i).optString("Role_Name");
                    String permissionCountColumn = internalUsers.getJSONObject(i).optString("Permission_Count");
                    String statusColumn = internalUsers.getJSONObject(i).optString("Status_Desc");

                    if (searchText == null
                            || (searchText != null && (nameColumn.toLowerCase().contains(searchText.toLowerCase())
                                    || userNameColumn.toLowerCase().contains(searchText.toLowerCase())))) {
                        responseCsvPrinter.printRecord(nameColumn, userNameColumn, emailColumn, roleColumn,
                                permissionCountColumn, statusColumn);
                    }
                }

                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.DOWNLOADFILE,
                        ActivityStatusEnum.SUCCESSFUL, "Users file download successful");

                Map<String, String> customHeaders = new HashMap<String, String>();
                customHeaders.put("Content-Type", "text/plain; charset=utf-8");
                customHeaders.put("Content-Disposition", "attachment; filename=\"Users_List.csv\"");

                responseInstance.setAttribute(FabricConstants.CHUNKED_RESULTS_IN_JSON, new BufferedHttpEntity(
                        new StringEntity(responseCsvBuilder.toString(), StandardCharsets.UTF_8)));
                responseInstance.getHeaders().putAll(customHeaders);
                responseInstance.setStatusCode(HttpStatus.SC_OK);
            } else {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.USERS, EventEnum.DOWNLOADFILE,
                        ActivityStatusEnum.FAILED, "Users file download failed");
            }

        } catch (Exception e) {
            LOG.error("Failed while downloading users list", e);
            ErrorCodeEnum.ERR_20687.setErrorCode(result);

            String errorMessage = "Failed to download users list. Please contact administrator.";
            CommonUtilities.fileDownloadFailure(responseInstance, errorMessage);
        }
        return result;
    }

    public static String getStartDateInOAuthFormat(String inputDateString) throws ParseException {
        DateFormat originalFormat = new SimpleDateFormat("MM/dd/yyyy");
        DateFormat oauthFormat = new SimpleDateFormat("yyyy-MM-dd");

        return oauthFormat.format(originalFormat.parse(inputDateString)) + "T00:00:00";
    }

    public static String getEndDateInOAuthFormat(String inputDateString) throws ParseException {
        DateFormat originalFormat = new SimpleDateFormat("MM/dd/yyyy");
        DateFormat oauthFormat = new SimpleDateFormat("yyyy-MM-dd");

        return oauthFormat.format(originalFormat.parse(inputDateString)) + "T23:59:59";
    }
}