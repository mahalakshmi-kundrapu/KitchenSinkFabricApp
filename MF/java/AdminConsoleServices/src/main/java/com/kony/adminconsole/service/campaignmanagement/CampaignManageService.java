package com.kony.adminconsole.service.campaignmanagement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.commons.utils.ThreadExecutor;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.dto.CampaignBean;
import com.kony.adminconsole.dto.CampaignSpecificationBean;
import com.kony.adminconsole.exception.ApplicationException;
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

/**
 * Service to create or update Campaign / update Campaign status at the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class CampaignManageService implements JavaService2 {

	public static final String CAMPAIGN_STATUS = "SID_SCHEDULED_ACTIVE_COMPLETED";

	private static final Logger LOG = Logger.getLogger(CampaignManageService.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {

		Result result = new Result();
		String userId = LoggedInUserHandler.getUserDetails(requestInstance).getUserId();

		try {

			if (!methodID.contains("Status")) {
				createOrUpdateCampaign(methodID, requestInstance, userId, result);
			}
			else {
				updateCampaignStatus(methodID, requestInstance, userId, result);
			}
		}
		catch (ApplicationException ae) {
			ae.getErrorCodeEnum().setErrorCode(result);
			LOG.error("ApplicationException occured in CampaignManageService JAVA service. Error: ", ae);
		}
		catch (Exception e) {
			ErrorCodeEnum.ERR_20001.setErrorCode(result);
			LOG.error("Exception occured in CampaignManageService JAVA service. Error: ", e);
		}

		return result;
	}

	Result createOrUpdateCampaign(String methodID, DataControllerRequest requestInstance, String userId, Result result) throws Exception {

		CampaignBean campaignBean = new CampaignBean();
		List<CampaignSpecificationBean> campaignSpecificationsList = new ArrayList<CampaignSpecificationBean>();
		List<CampaignSpecificationBean> campaignSpecificationsToBeRemovedList = new ArrayList<CampaignSpecificationBean>();
		List<String> groupsToBeAddedList = new ArrayList<String>();
		List<String> groupsToBeRemovedList = new ArrayList<String>();

		// -> -> CAMPAIGN table fields <- <-

		// ** Campaign ID **
		String id = requestInstance.getParameter("id");
		if (!StringUtils.isBlank(id)) {
			campaignBean.setId(requestInstance.getParameter("id"));
		}
		else if (methodID.contains("update")) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21709);
		}
		else if (methodID.contains("create")) {
			campaignBean.setId(CommonUtilities.getNewId().toString());
		}

		// ** Campaign name **
		String name = requestInstance.getParameter("name");
		if (!StringUtils.isBlank(name) && name.length() <= 50) {
			campaignBean.setName(name);
		}
		else if (!StringUtils.isBlank(name) && name.length() > 50) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21731);
		}
		else if (methodID.contains("create")) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21710);
		}

		// ** Campaign priority **
		String priority = requestInstance.getParameter("priority");
		if (!StringUtils.isBlank(priority) && StringUtils.isNumeric(priority) && Integer.parseInt(priority) != 0) {
			campaignBean.setPriority(Integer.parseInt(priority));
		}
		else if (methodID.contains("create") && StringUtils.isBlank(priority)) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21711);
		}
		else if (methodID.contains("create") && (!StringUtils.isNumeric(priority)
				|| (StringUtils.isNumeric(priority) && Integer.parseInt(priority) == 0))) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21718);
		}

		// ** Campaign description **
		String description = requestInstance.getParameter("description");
		if (!StringUtils.isBlank(description) && description.length() <= 150) {
			campaignBean.setDescription(description);
		}
		else if (!StringUtils.isBlank(description) && description.length() > 150) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21732);
		}
		else if (methodID.contains("create")) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21712);
		}

		// ** Campaign start date time **
		String startDateTime = requestInstance.getParameter("startDateTime");
		if (!StringUtils.isBlank(startDateTime) && !StringUtils.isBlank(parseDateToISOFormat(startDateTime))) {
			campaignBean.setStartDateTime(parseDateToISOFormat(startDateTime));
		}
		else if (methodID.contains("create") && StringUtils.isBlank(startDateTime)) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21713);
		}
		else if (StringUtils.isBlank(parseDateToISOFormat(startDateTime))) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21733);
		}

		// ** Campaign end date time **
		String endDateTime = requestInstance.getParameter("endDateTime");
		if (!StringUtils.isBlank(endDateTime) && !StringUtils.isBlank(parseDateToISOFormat(endDateTime))) {
			campaignBean.setEndDateTime(parseDateToISOFormat(endDateTime));
		}
		else if (methodID.contains("create") && StringUtils.isBlank(endDateTime)) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21714);
		}
		else if (StringUtils.isBlank(parseDateToISOFormat(endDateTime))) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21734);
		}

		// -> -> CAMPAIGNSPECIFICATION table records <- <-
		if (requestInstance.getParameter("specifications") != null) {

			JSONArray specifications = new JSONArray(requestInstance.getParameter("specifications"));

			for (int i = 0; i < specifications.length(); ++i) {

				CampaignSpecificationBean specificationBean = new CampaignSpecificationBean();
				JSONObject specification = specifications.getJSONObject(i);

				// ** Campaign ID **
				specificationBean.setCampaignId(campaignBean.getId());

				// ** Channel ID **
				if (StringUtils.isBlank(specification.optString("channelId"))) {
					throw new ApplicationException(ErrorCodeEnum.ERR_21725);
				}
				specificationBean.setChannelId(specification.getString("channelId"));

				// ** Screen ID **
				if (StringUtils.isBlank(specification.optString("screenId"))) {
					throw new ApplicationException(ErrorCodeEnum.ERR_21726);
				}
				specificationBean.setScreenId(specification.getString("screenId"));

				// ** Image resolution **
				if (StringUtils.isBlank(specification.optString("imageResolution"))) {
					throw new ApplicationException(ErrorCodeEnum.ERR_21728);
				}
				specificationBean.setImageResolution(specification.getString("imageResolution"));

				// ** Image URL **
				if (!StringUtils.isBlank(specification.optString("imageURL"))) {
					specificationBean.setImageURL(specification.optString("imageURL"));
				}
				else if (methodID.contains("create")) {
					throw new ApplicationException(ErrorCodeEnum.ERR_21727);
				}

				// ** Destination URL **
				if (!StringUtils.isBlank(specification.optString("destinationURL"))) {
					specificationBean.setDestinationURL(specification.optString("destinationURL"));
				}

				campaignSpecificationsList.add(specificationBean);
			}
		}
		else if (methodID.contains("create")) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21715);
		}

		// ** CAMPAIGNGROUP table fields **
		if (requestInstance.getParameter("groupsToBeAdded") != null) {

			JSONArray groups = new JSONArray(requestInstance.getParameter("groupsToBeAdded"));

			for (int i = 0; i < groups.length(); ++i) {
				JSONObject group = groups.getJSONObject(i);

				if (!StringUtils.isBlank(group.optString("groupId"))) {
					groupsToBeAddedList.add(group.getString("groupId"));
				}
				else {
					throw new ApplicationException(ErrorCodeEnum.ERR_21729);
				}
			}
		}

		if (!methodID.contains("create")) {
			if (requestInstance.getParameter("groupsToBeRemoved") != null) {

				JSONArray groups = new JSONArray(requestInstance.getParameter("groupsToBeRemoved"));

				for (int i = 0; i < groups.length(); ++i) {
					JSONObject group = groups.getJSONObject(i);

					if (!StringUtils.isBlank(group.optString("groupId"))) {
						groupsToBeRemovedList.add(group.getString("groupId"));
					}
					else {
						throw new ApplicationException(ErrorCodeEnum.ERR_21729);
					}
				}
			}

			if (requestInstance.getParameter("specificationsToBeRemoved") != null) {

				JSONArray specifications = new JSONArray(requestInstance.getParameter("specificationsToBeRemoved"));

				for (int i = 0; i < specifications.length(); ++i) {

					JSONObject specification = specifications.getJSONObject(i);

					CampaignSpecificationBean specificationBean = new CampaignSpecificationBean();
					specificationBean.setCampaignId(campaignBean.getId());

					if (StringUtils.isBlank(specification.optString("channelId"))) {
						throw new ApplicationException(ErrorCodeEnum.ERR_21725);
					}
					specificationBean.setChannelId(specification.getString("channelId"));

					if (StringUtils.isBlank(specification.optString("screenId"))) {
						throw new ApplicationException(ErrorCodeEnum.ERR_21726);
					}
					specificationBean.setScreenId(specification.getString("screenId"));

					campaignSpecificationsToBeRemovedList.add(specificationBean);
				}
			}
		}

		/*
		 * The C360 Administrator may want to add a new Campaign with a priority value that already exists at the backend.
		 * In such a scenario, existing campaigns need to shift down their priority values by 1 value, until an available campaign priority value is found.
		 * 
		 * For eg. existing campaign priority values = 1,2,3,4,5,7,8,9
		 * New campaign has priority = 3
		 * Then, existing priority 3 becomes 4, 4 becomes 5, 5 becomes 6.
		 * As 6 is a vacant slot, campaign priority shift should stop here.
		 */

		HashSet<Integer> existingPriorities = getExistingPriorities(requestInstance);
		int desiredPriority = campaignBean.getPriority();

		if (desiredPriority != 0 && existingPriorities.contains(desiredPriority)) {
			int availablePriority = -1;
			int currentPriority = desiredPriority;

			while (true) {
				if (!existingPriorities.contains(++currentPriority)) {
					availablePriority = currentPriority;
					break;
				}
			}

			// ** Updating campaign entries using 'campaign_priority_update_proc' stored procedure **
			Map<String, String> postParametersMap = new HashMap<String, String>();
			postParametersMap.put("_priorityStart", "" + desiredPriority);
			postParametersMap.put("_priorityEnd", "" + availablePriority);

			String updateCampaignResponse = Executor.invokeService(ServiceURLEnum.CAMPAIGN_C360_PRIORITY_UPDATE_PROC,
					postParametersMap, null, requestInstance);
			JSONObject updateCampaignResponseJSON = CommonUtilities.getStringAsJSONObject(updateCampaignResponse);

			if (updateCampaignResponseJSON != null && updateCampaignResponseJSON.has(FabricConstants.OPSTATUS)
					&& updateCampaignResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.UPDATE,
						ActivityStatusEnum.SUCCESSFUL, "Campaign priority update successful");
			}
			else {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.UPDATE,
						ActivityStatusEnum.FAILED, "Campaign priority update failed");
				throw new ApplicationException(ErrorCodeEnum.ERR_21741);
			}
		}

		// -> Adding / updating 1 record to CAMPAIGN table <-

		Map<String, String> campaignMap = new HashMap<>();
		campaignMap.put("id", campaignBean.getId());
		campaignMap.put("status_id", "SID_SCHEDULED_ACTIVE_COMPLETED");

		if (campaignBean.getName() != null) {
			campaignMap.put("name", campaignBean.getName());
		}
		if (campaignBean.getPriority() != 0) {
			campaignMap.put("priority", "" + campaignBean.getPriority());
		}
		if (campaignBean.getStartDateTime() != null) {
			campaignMap.put("start_datetime", campaignBean.getStartDateTime());
		}
		if (campaignBean.getEndDateTime() != null) {
			campaignMap.put("end_datetime", campaignBean.getEndDateTime());
		}
		if (campaignBean.getDescription() != null) {
			campaignMap.put("description", campaignBean.getDescription());
		}

		if (methodID.contains("create")) {
			campaignMap.put("createdby", userId);
		}
		else {
			campaignMap.put("modifiedby", userId);
		}

		String createCampaignResponse = Executor.invokeService(
				methodID.contains("create") ? ServiceURLEnum.CAMPAIGN_CREATE : ServiceURLEnum.CAMPAIGN_UPDATE, campaignMap, null, requestInstance);
		JSONObject createCampaignResponseJSON = CommonUtilities.getStringAsJSONObject(createCampaignResponse);

		if (createCampaignResponseJSON != null && createCampaignResponseJSON.has(FabricConstants.OPSTATUS)
				&& createCampaignResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
			if (methodID.contains("create")) {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.CREATE,
						ActivityStatusEnum.SUCCESSFUL, "Campaign create successful");
			}
			else {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.UPDATE,
						ActivityStatusEnum.SUCCESSFUL, "Campaign update successful");
			}
		}
		else {
			if (methodID.contains("create")) {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.CREATE,
						ActivityStatusEnum.FAILED, "Campaign create failed");
				throw new ApplicationException(ErrorCodeEnum.ERR_21702);
			}
			else {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.UPDATE,
						ActivityStatusEnum.FAILED, "Campaign update failed");
				throw new ApplicationException(ErrorCodeEnum.ERR_21703);
			}
		}

		// -> Adding records to CAMPAIGNSPECIFICATION table <-
		for (CampaignSpecificationBean campaignSpecificationBean : campaignSpecificationsList) {

			Map<String, String> campaignSpecificationProcMap = new HashMap<>();
			campaignSpecificationProcMap.put("_campaignId", campaignBean.getId());
			campaignSpecificationProcMap.put("_channel", campaignSpecificationBean.getChannelId());
			campaignSpecificationProcMap.put("_screen", campaignSpecificationBean.getScreenId());
			campaignSpecificationProcMap.put("_imageResolution", campaignSpecificationBean.getImageResolution());
			campaignSpecificationProcMap.put("_imageURL", campaignSpecificationBean.getImageURL());
			campaignSpecificationProcMap.put("_destinationURL",
					StringUtils.isBlank(campaignSpecificationBean.getDestinationURL()) ? "" : campaignSpecificationBean.getDestinationURL());
			campaignSpecificationProcMap.put("_userId", userId);

			String readResponse = Executor.invokeService(ServiceURLEnum.CAMPAIGN_C360_SPECIFICATION_MANAGE_PROC,
					campaignSpecificationProcMap, null, requestInstance);
			JSONObject createCampaignSpecProcResponseJSON = CommonUtilities.getStringAsJSONObject(readResponse);

			if (createCampaignSpecProcResponseJSON != null && createCampaignSpecProcResponseJSON.has(FabricConstants.OPSTATUS)
					&& createCampaignSpecProcResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
				if (methodID.contains("create")) {
					AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.CREATE,
							ActivityStatusEnum.SUCCESSFUL, "Campaign specification create successful");
				}
				else {
					AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.UPDATE,
							ActivityStatusEnum.SUCCESSFUL, "Campaign specification update successful");
				}
			}
			else {
				if (methodID.contains("create")) {
					AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.CREATE,
							ActivityStatusEnum.FAILED, "Campaign specification create failed");
					throw new ApplicationException(ErrorCodeEnum.ERR_21706);
				}
				else {
					AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.UPDATE,
							ActivityStatusEnum.FAILED, "Campaign specification update failed");
					throw new ApplicationException(ErrorCodeEnum.ERR_21707);
				}
			}
		}

		// -> Removing records from CAMPAIGNSPECIFICATION table <-
		for (CampaignSpecificationBean campaignSpecificationBean : campaignSpecificationsToBeRemovedList) {

			Map<String, String> campaignSpecificationProcMap = new HashMap<>();
			campaignSpecificationProcMap.put("_campaignId", campaignSpecificationBean.getCampaignId());
			campaignSpecificationProcMap.put("_channel", campaignSpecificationBean.getChannelId());
			campaignSpecificationProcMap.put("_screen", campaignSpecificationBean.getScreenId());

			String response = Executor.invokeService(ServiceURLEnum.CAMPAIGN_C360_SPECIFICATION_DELETE_PROC,
					campaignSpecificationProcMap, null, requestInstance);
			JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

			if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
					&& responseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.DELETE,
						ActivityStatusEnum.SUCCESSFUL, "Campaign specification delete successful");
			}
			else {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.DELETE,
						ActivityStatusEnum.FAILED, "Campaign specification delete failed");
				throw new ApplicationException(ErrorCodeEnum.ERR_21708);
			}
		}

		// -> Adding records to CAMPAIGNGROUP table <-
		for (String groupId : groupsToBeAddedList) {

			Map<String, String> campaignGroupMap = new HashMap<>();
			campaignGroupMap.put("campaign_id", campaignBean.getId());
			campaignGroupMap.put("group_id", groupId);
			campaignGroupMap.put("createdby", userId);

			String createCampaignGroupResponse = Executor.invokeService(ServiceURLEnum.CAMPAIGNGROUP_CREATE, campaignGroupMap, null, requestInstance);
			JSONObject createCampaignGroupResponseJSON = CommonUtilities.getStringAsJSONObject(createCampaignGroupResponse);

			if (createCampaignGroupResponseJSON != null && createCampaignGroupResponseJSON.has(FabricConstants.OPSTATUS)
					&& createCampaignGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.CREATE,
						ActivityStatusEnum.SUCCESSFUL, "Campaign customer role create successful");
			}
			else {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.CREATE,
						ActivityStatusEnum.FAILED, "Campaign customer role create failed");
				throw new ApplicationException(ErrorCodeEnum.ERR_21722);
			}
		}

		// -> Removing records from CAMPAIGNGROUP table <-
		for (String groupId : groupsToBeRemovedList) {

			Map<String, String> campaignGroupMap = new HashMap<>();
			campaignGroupMap.put("campaign_id", campaignBean.getId());
			campaignGroupMap.put("group_id", groupId);

			String deleteCampaignGroupResponse = Executor.invokeService(ServiceURLEnum.CAMPAIGNGROUP_DELETE, campaignGroupMap, null, requestInstance);
			JSONObject deleteCampaignGroupResponseJSON = CommonUtilities.getStringAsJSONObject(deleteCampaignGroupResponse);

			if (deleteCampaignGroupResponseJSON != null && deleteCampaignGroupResponseJSON.has(FabricConstants.OPSTATUS)
					&& deleteCampaignGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.DELETE,
						ActivityStatusEnum.SUCCESSFUL, "Campaign customer role delete successful");
			}
			else {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.DELETE,
						ActivityStatusEnum.FAILED, "Campaign customer role delete failed");
				throw new ApplicationException(ErrorCodeEnum.ERR_21724);
			}
		}

		// ** Finding core banking customers for new groups **
		Callable<List<String>> findAndAddCoreBankingCustomersToGroupsCallable = new Callable<List<String>>() {
			@Override
			public List<String> call() throws Exception {
				return CampaignHandler.findAndAddCoreBankingCustomersToGroups(requestInstance, userId, groupsToBeAddedList);
			}
		};

		ThreadExecutor.execute(findAndAddCoreBankingCustomersToGroupsCallable);

		return result;
	}

	Result updateCampaignStatus(String methodID, DataControllerRequest requestInstance, String userId, Result result) throws ApplicationException {

		CampaignBean campaignBean = new CampaignBean();

		// ** CAMPAIGN table fields **
		String campaignId = requestInstance.getParameter("id");
		if (StringUtils.isBlank(campaignId)) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21709);
		}
		else {
			campaignBean.setId(campaignId);
		}

		String campaignStatusId = requestInstance.getParameter("statusId");
		if (StringUtils.isBlank(campaignStatusId)) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21717);
		}
		else {
			campaignBean.setStatusId(campaignStatusId);
		}

		// -> Adding 1 record to CAMPAIGN table <-
		Map<String, String> campaignMap = new HashMap<>();
		campaignMap.put("id", campaignBean.getId());
		campaignMap.put("status_id", campaignBean.getStatusId());
		campaignMap.put("modifiedby", userId);

		String updateCampaignStatusResponse = Executor.invokeService(ServiceURLEnum.CAMPAIGN_UPDATE, campaignMap, null, requestInstance);
		JSONObject updateCampaignStatusResponseJSON = CommonUtilities.getStringAsJSONObject(updateCampaignStatusResponse);

		if (updateCampaignStatusResponseJSON != null && updateCampaignStatusResponseJSON.has(FabricConstants.OPSTATUS)
				&& updateCampaignStatusResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.UPDATE,
					ActivityStatusEnum.SUCCESSFUL, "Campaign status update successful");
		}
		else {
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.UPDATE,
					ActivityStatusEnum.FAILED, "Campaign status update failed");
			throw new ApplicationException(ErrorCodeEnum.ERR_21742);
		}

		return result;
	}

	HashSet<Integer> getExistingPriorities(DataControllerRequest requestInstance) throws ApplicationException {

		HashSet<Integer> prioritySet = new HashSet<Integer>();

		// ** Reading from 'campaign' table **
		Map<String, String> campaignMap = new HashMap<>();
		campaignMap.put(ODataQueryConstants.FILTER, "status_id ne 'SID_TERMINATED' and end_datetime ge '" + LocalDate.now().toString() + "'");
		campaignMap.put(ODataQueryConstants.ORDER_BY, "priority");

		String readCampaignResponse = Executor.invokeService(ServiceURLEnum.CAMPAIGN_READ, campaignMap, null, requestInstance);
		JSONObject readCampaignResponseJSON = CommonUtilities.getStringAsJSONObject(readCampaignResponse);

		if (readCampaignResponseJSON != null && readCampaignResponseJSON.has(FabricConstants.OPSTATUS)
				&& readCampaignResponseJSON.getInt(FabricConstants.OPSTATUS) == 0 && readCampaignResponseJSON.has("campaign")) {

			JSONArray readCampaignResponseJSONArray = readCampaignResponseJSON.getJSONArray("campaign");

			for (int i = 0; i < readCampaignResponseJSONArray.length(); ++i) {

				JSONObject campaignResponse = readCampaignResponseJSONArray.getJSONObject(i);

				String priority = campaignResponse.getString("priority");
				if (!StringUtils.isBlank(priority)) {
					prioritySet.add(Integer.parseInt(priority));
				}
				else {
					throw new ApplicationException(ErrorCodeEnum.ERR_21719);
				}
			}
		}
		else {
			throw new ApplicationException(ErrorCodeEnum.ERR_21719);
		}

		return prioritySet;
	}

	public static String parseDateToISOFormat(String date) {

		try {
			DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd/yyyy");
			return LocalDate.parse(date, format).toString() + "T00:00:00";
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	Set<String> getCustomersFromModelsJSON(DataControllerRequest requestInstance, String models) throws ApplicationException {

		Set<String> customerIds = new HashSet<String>();
		JSONArray modelsJSONArray = new JSONArray(models);

		for (int i = 0; i < modelsJSONArray.length(); ++i) {
			String endpointURL = modelsJSONArray.getJSONObject(i).getString("endpoint_url");
			List<String> customerIdsFromOneURL = CampaignHandler.getCustomerIdsFromCoreBanking(requestInstance, endpointURL);

			for (String customerId : customerIdsFromOneURL) {
				customerIds.add(customerId);
			}
		}

		return customerIds;
	}
}
