package com.kony.adminconsole.service.group;

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

public class DownloadGroupsList implements JavaService2 {

	private static final Logger LOG = Logger.getLogger(DownloadGroupsList.class);

	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {

		Result result = new Result();

		try {

			CommonUtilities.getAuthToken(requestInstance);

			Map<String, String> queryParamsMap = (Map<String, String>) requestInstance.getAttribute("queryparams");
			String searchText = queryParamsMap.containsKey("searchText") ? queryParamsMap.get("searchText") : null;
			String type = queryParamsMap.containsKey("type") ? queryParamsMap.get("type") : null;
			String status = queryParamsMap.containsKey("status") ? queryParamsMap.get("status") : null;

			// ** Reading entries from 'groups_view' view **
			Map<String, String> groupsViewMap = new HashMap<String, String>();
			groupsViewMap.put(ODataQueryConstants.SELECT,
					"Group_Name, Group_Desc, Customers_Count, Entitlements_Count, Status");

			StringBuilder filterString = new StringBuilder();

			if (type != null) {
				String[] types = type.split(",");
				filterString.append("(");
				for (int i = 0; i < types.length - 1; ++i) {
					filterString.append("Type_Name eq '" + types[i] + "'");
					filterString.append(" or ");
				}
				filterString.append("Type_Name eq '" + types[types.length - 1] + "')");
			}

			if (status != null) {
				if (filterString.length() != 0) {
					filterString.append(" and ");
				}

				String[] statuses = status.split(",");
				filterString.append("(");
				for (int i = 0; i < statuses.length - 1; ++i) {
					filterString.append("Status_id eq '" + statuses[i] + "'");
					filterString.append(" or ");
				}
				filterString.append("Status_id eq '" + statuses[statuses.length - 1] + "')");
			}

			if (filterString != null) {
				groupsViewMap.put(ODataQueryConstants.FILTER, filterString.toString());
			}

			String groupsViewResponse = Executor.invokeService(ServiceURLEnum.GROUPS_VIEW_READ, groupsViewMap, null,
					requestInstance);
			JSONObject groupsViewResponseJSON = CommonUtilities.getStringAsJSONObject(groupsViewResponse);

			if (groupsViewResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
					&& groupsViewResponseJSON.getJSONArray("groups_view") != null) {

				StringBuilder responseCsvBuilder = new StringBuilder(); // Contains the text for response CSV file
				CSVPrinter responseCsvPrinter = CSVFormat.DEFAULT
						.withHeader("Name", "Description", "Customers", "Entitlements", "Status")
						.print(responseCsvBuilder);

				JSONArray groups = groupsViewResponseJSON.getJSONArray("groups_view");

				for (int i = 0; i < groups.length(); ++i) {

					String nameColumn = groups.getJSONObject(i).optString("Group_Name");
					String descriptionColumn = groups.getJSONObject(i).optString("Group_Desc");
					String customersCountColumn = groups.getJSONObject(i).optString("Customers_Count");
					String entitlementsCountColumn = groups.getJSONObject(i).optString("Entitlements_Count");
					String statusColumn = groups.getJSONObject(i).optString("Status");

					if (searchText == null
							|| (searchText != null && (nameColumn.toLowerCase().contains(searchText.toLowerCase())))) {
						responseCsvPrinter.printRecord(nameColumn, descriptionColumn, customersCountColumn,
								entitlementsCountColumn, statusColumn);
					}
				}

				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERROLES, EventEnum.DOWNLOADFILE,
						ActivityStatusEnum.SUCCESSFUL, "Groups file download successful");

				Map<String, String> customHeaders = new HashMap<String, String>();
				customHeaders.put("Content-Type", "text/plain; charset=utf-8");
				customHeaders.put("Content-Disposition", "attachment; filename=\"Groups_List.csv\"");

				responseInstance.setAttribute(FabricConstants.CHUNKED_RESULTS_IN_JSON, new BufferedHttpEntity(
						new StringEntity(responseCsvBuilder.toString(), StandardCharsets.UTF_8)));
				responseInstance.getHeaders().putAll(customHeaders);
				responseInstance.setStatusCode(HttpStatus.SC_OK);
			}
			else {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERROLES, EventEnum.DOWNLOADFILE,
						ActivityStatusEnum.FAILED, "Groups file download failed");
			}

		}
		catch (Exception e) {
			LOG.error("Failed while downloading groups list", e);
			ErrorCodeEnum.ERR_20687.setErrorCode(result);

			String errorMessage = "Failed to download groups list. Please contact administrator.";
			CommonUtilities.fileDownloadFailure(responseInstance, errorMessage);
		}
		return result;
	}
}