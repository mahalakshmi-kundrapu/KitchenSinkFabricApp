package com.kony.adminconsole.service.campaignmanagement;

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
import com.kony.adminconsole.commons.utils.ThreadExecutor;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.dto.AttributeBean;
import com.kony.adminconsole.dto.GroupBean;
import com.kony.adminconsole.dto.ModelBean;
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
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to create or update group attributes at the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class GroupAttributesManageService implements JavaService2 {

	private static final Logger LOG = Logger.getLogger(GroupAttributesManageService.class);

	Map<String, AttributeBean> backendAttributes = null;
	Set<String> backendAttributeCriterias = null;
	Map<String, String> backendAttributeOptions = null;

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {

		Result result = new Result();
		String userId = LoggedInUserHandler.getUserDetails(requestInstance).getUserId();

		try {
			createOrUpdateGroup(methodID, requestInstance, userId, result);
		}
		catch (ApplicationException ae) {
			ae.getErrorCodeEnum().setErrorCode(result);
			LOG.error("ApplicationException occured in GroupAttributesManageService JAVA service. Error: ", ae);
		}
		catch (Exception e) {
			ErrorCodeEnum.ERR_20001.setErrorCode(result);
			LOG.error("Exception occured in GroupAttributesManageService JAVA service. Error: ", e);
		}

		return result;
	}

	Result createOrUpdateGroup(String methodID, DataControllerRequest requestInstance, String userId, Result result)
			throws Exception {

		GroupBean groupBean = new GroupBean();

		// -> -> GROUP table record <- <-
		if (requestInstance.getParameter("group") != null) {

			JSONObject group = new JSONObject(requestInstance.getParameter("group"));

			// ** Group ID **
			String groupId = group.optString("id");
			if (!StringUtils.isBlank(groupId)) {
				groupBean.setId(groupId);
			}
			else if (methodID.contains("update")) {
				throw new ApplicationException(ErrorCodeEnum.ERR_21786);
			}
			else if (methodID.contains("create")) {
				groupBean.setId(CommonUtilities.getNewId().toString());
			}

			// ** Group name **
			String name = group.optString("name");
			if (!StringUtils.isBlank(name) && name.length() <= 50) {
				groupBean.setName(name);
			}
			else if (!StringUtils.isBlank(name) && name.length() > 100) {
				throw new ApplicationException(ErrorCodeEnum.ERR_21731);
			}
			else if (methodID.contains("create")) {
				throw new ApplicationException(ErrorCodeEnum.ERR_21710);
			}

			// ** Group description **
			String description = group.optString("description");
			if (!StringUtils.isBlank(description) && description.length() <= 250) {
				groupBean.setDescription(description);
			}
			else if (!StringUtils.isBlank(description) && description.length() > 250) {
				throw new ApplicationException(ErrorCodeEnum.ERR_21732);
			}
			else if (methodID.contains("create")) {
				throw new ApplicationException(ErrorCodeEnum.ERR_21712);
			}

			// -> Adding / updating 1 record to GROUP table <-

			Map<String, String> groupMap = new HashMap<>();
			groupMap.put("id", groupBean.getId());
			groupMap.put("Name", groupBean.getName());
			groupMap.put("Description", groupBean.getDescription());
			groupMap.put("Type_id", "TYPE_ID_CAMPAIGN");
			groupMap.put("Status_id", "SID_ACTIVE");
			groupMap.put(methodID.contains("create") ? "createdby" : "modifiedby", userId);

			String groupResponse = Executor.invokeService(
					methodID.contains("create") ? ServiceURLEnum.MEMBERGROUP_CREATE : ServiceURLEnum.MEMBERGROUP_UPDATE,
					groupMap, null, requestInstance);
			JSONObject groupResponseJSON = CommonUtilities.getStringAsJSONObject(groupResponse);

			if (groupResponseJSON != null && groupResponseJSON.has(FabricConstants.OPSTATUS)
					&& groupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
				if (methodID.contains("create")) {
					AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERROLES, EventEnum.CREATE,
							ActivityStatusEnum.SUCCESSFUL, "Group create successful");
				}
				else {
					AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERROLES, EventEnum.UPDATE,
							ActivityStatusEnum.SUCCESSFUL, "Group update successful");
				}
			}
			else {
				if (methodID.contains("create")) {
					AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERROLES, EventEnum.CREATE,
							ActivityStatusEnum.FAILED, "Group create failed");
					throw new ApplicationException(ErrorCodeEnum.ERR_21782);
				}
				else {
					AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERROLES, EventEnum.UPDATE,
							ActivityStatusEnum.FAILED, "Group update failed");
					throw new ApplicationException(ErrorCodeEnum.ERR_21783);
				}
			}
		}
		else if (methodID.contains("create")) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21785);
		}

		// -> -> ATTRIBUTEGROUP table records <- <-
		if (requestInstance.getParameter("attributes") != null) {

			String attributes = requestInstance.getParameter("attributes");
			JSONArray attributesJSONArray = new JSONArray(requestInstance.getParameter("attributes"));

			backendAttributes = CampaignHandler.getAllAttributes(requestInstance);
			backendAttributeCriterias = CampaignHandler.getAttributeCriterias(requestInstance);
			backendAttributeOptions = CampaignHandler.getAttributeOptions(requestInstance);

			boolean areAttributesValid = validateAttributeJSONArray(requestInstance, attributesJSONArray);

			if (areAttributesValid) {

				String models = constructModelsJSON(requestInstance, attributesJSONArray);

				Set<String> coreBankingCustomerIds = getCustomersFromModelsJSON(requestInstance, models);
				String customerCount = "" + coreBankingCustomerIds.size();

				Map<String, String> groupAttributeMap = new HashMap<>();
				groupAttributeMap.put("group_id", groupBean.getId());
				groupAttributeMap.put("admin_attributes", attributes);
				groupAttributeMap.put("endpoint_model_urls", models);
				groupAttributeMap.put("customer_count", customerCount);
				groupAttributeMap.put(methodID.contains("create") ? "createdby" : "modifiedby", userId);

				String attributegroupResponse = Executor
						.invokeService(
								methodID.contains("create") ? ServiceURLEnum.GROUPATTRIBUTE_CREATE
										: ServiceURLEnum.GROUPATTRIBUTE_UPDATE,
								groupAttributeMap, null, requestInstance);
				JSONObject attributegroupResponseJSON = CommonUtilities.getStringAsJSONObject(attributegroupResponse);

				if (attributegroupResponseJSON != null && attributegroupResponseJSON.has(FabricConstants.OPSTATUS)
						&& attributegroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
					if (methodID.contains("create")) {
						AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.CREATE,
								ActivityStatusEnum.SUCCESSFUL, "Attribute group create successful");
					}
					else {
						AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.UPDATE,
								ActivityStatusEnum.SUCCESSFUL, "Attribute group update successful");
					}
				}
				else {
					if (methodID.contains("create")) {
						AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.CREATE,
								ActivityStatusEnum.FAILED, "Attribute group create failed");
						throw new ApplicationException(ErrorCodeEnum.ERR_21774);
					}
					else {
						AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.UPDATE,
								ActivityStatusEnum.FAILED, "Attribute group update failed");
						throw new ApplicationException(ErrorCodeEnum.ERR_21775);
					}
				}

				result.addParam(new Param("customerCount", customerCount, FabricConstants.INT));

				Map<String, String> coreBankingToDBXCustomerMap = CampaignHandler
						.getCoreBankingToDBXCustomersMap(requestInstance);
				List<String> dbxCustomerIds = new ArrayList<String>();

				for (String coreBankingCustomerId : coreBankingCustomerIds) {
					dbxCustomerIds.add(coreBankingToDBXCustomerMap.get(coreBankingCustomerId));
				}

				// ** Mapping DBX customers to newly created group **
				Callable<List<String>> addCustomersToCampaignGroupsCallable = new Callable<List<String>>() {
					@Override
					public List<String> call() throws Exception {
						return CampaignHandler.addCustomersToCampaignGroups(requestInstance, userId, dbxCustomerIds,
								groupBean.getId());
					}
				};

				ThreadExecutor.execute(addCustomersToCampaignGroupsCallable);
			}
			else {
				throw new ApplicationException(ErrorCodeEnum.ERR_21788);
			}
		}
		else if (methodID.contains("create")) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21787);
		}

		result.addParam(new Param("groupId", groupBean.getId(), FabricConstants.STRING));

		return result;
	}

	boolean validateAttributeJSONArray(DataControllerRequest requestInstance, JSONArray attributes)
			throws ApplicationException {

		for (int i = 0; i < attributes.length(); ++i) {

			JSONObject attribute = attributes.getJSONObject(i);

			// Checking if attribute id, criteria and option exists
			if (StringUtils.isBlank(attribute.optString("id")) || StringUtils.isBlank(attribute.optString("criteria"))
					|| StringUtils.isBlank(attribute.optString("value"))) {
				return false;
			}

			// Checking if attribute id & criteria are valid
			if (!backendAttributes.containsKey(attribute.getString("id"))
					|| !backendAttributeCriterias.contains(attribute.getString("criteria"))) {
				return false;
			}
			else {
				// Checking if attribute is a SELECT type and if yes, checking if value is a
				// valid option
				if (backendAttributes.get(attribute.getString("id")).getType().equals("SELECT")) {
					if (!backendAttributeOptions.containsKey(attribute.getString("value"))) {
						return false;
					}
				}
			}
		}

		return true;

	}

	String constructModelsJSON(DataControllerRequest requestInstance, JSONArray attributes)
			throws ApplicationException {

		Map<String, String> modelMap = new HashMap<>();

		for (int i = 0; i < attributes.length(); ++i) {

			JSONObject attribute = attributes.getJSONObject(i);

			String attributeId = attribute.getString("id");
			String attributeCriteria = attribute.getString("criteria");
			String attributeValue = attribute.getString("value");

			List<ModelBean> models = CampaignHandler.getModelsByAttribute(requestInstance, attributeId);

			for (ModelBean model : models) {

				String modelId = model.getId();

				StringBuilder modelURL = new StringBuilder(
						modelMap.containsKey(modelId) ? modelMap.get(modelId) : model.getEndpointURL());

				modelURL.append(modelMap.containsKey(modelId) ? "%20and%20" : "?$filter=");
				modelURL.append(backendAttributes.get(attributeId).getEndpointAttributeId());
				modelURL.append("%20" + attributeCriteria + "%20");
				modelURL.append(backendAttributes.get(attributeId).getType().equals("NUMERIC")
						|| backendAttributes.get(attributeId).getType().equals("DATE") ? "" : "'");
				modelURL.append(backendAttributes.get(attributeId).getType().equals("SELECT")
						? backendAttributeOptions.get(attributeValue)
						: attributeValue);
				modelURL.append(backendAttributes.get(attributeId).getType().equals("NUMERIC")
						|| backendAttributes.get(attributeId).getType().equals("DATE") ? "" : "'");

				modelMap.put(modelId, modelURL.toString());
			}
		}

		JSONArray models = new JSONArray();

		for (Map.Entry<String, String> entry : modelMap.entrySet()) {
			JSONObject model = new JSONObject();
			model.put("id", entry.getKey());
			model.put("endpoint_url", entry.getValue());

			models.put(model);
		}

		return models.toString();
	}

	Set<String> getCustomersFromModelsJSON(DataControllerRequest requestInstance, String models)
			throws ApplicationException {

		Set<String> customerIds = new HashSet<String>();
		JSONArray modelsJSONArray = new JSONArray(models);

		for (int i = 0; i < modelsJSONArray.length(); ++i) {
			String endpointURL = modelsJSONArray.getJSONObject(i).getString("endpoint_url");
			List<String> customerIdsFromOneURL = CampaignHandler.getCustomerIdsFromCoreBanking(requestInstance,
					endpointURL);

			for (String customerId : customerIdsFromOneURL) {
				customerIds.add(customerId);
			}
		}

		return customerIds;
	}
}
