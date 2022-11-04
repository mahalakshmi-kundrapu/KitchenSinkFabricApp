package com.kony.adminconsole.service.campaignmanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.dto.AttributeBean;
import com.kony.adminconsole.dto.ModelBean;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Dataset;

/**
 * Contains functions to handle Campaign Management.
 * Common business logic shared by Campaign JAVA classes has been singularized and kept here.
 * 
 * @author Mohit Khosla (KH2356)
 */

public class CampaignHandler {

	public static List<String> findAndAddCoreBankingCustomersToGroups(DataControllerRequest requestInstance, String userId, List<String> groupIds)
			throws ApplicationException {

		Map<String, String> coreBankingToDBXCustomerMap = CampaignHandler.getCoreBankingToDBXCustomersMap(requestInstance);

		StringBuilder groupIdFilter = new StringBuilder("group_id in '");
		for (int i = 0; i < groupIds.size(); ++i) {
			groupIdFilter.append(groupIds.get(i));
			groupIdFilter.append(i == groupIds.size() - 1 ? "'" : ",");
		}

		Map<String, String> groupModelEndpointURLsMap = new HashMap<String, String>();

		// ** Reading from 'groupattribute' table **
		Map<String, String> groupattributeMap = new HashMap<>();
		groupattributeMap.put(ODataQueryConstants.FILTER, groupIdFilter.toString());

		String response = Executor.invokeService(ServiceURLEnum.GROUPATTRIBUTE_READ, groupattributeMap, null, requestInstance);
		JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

		if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
				&& responseJSON.getInt(FabricConstants.OPSTATUS) == 0 && responseJSON.has("groupattribute")) {

			JSONArray responseJSONArray = responseJSON.getJSONArray("groupattribute");

			for (int i = 0; i < responseJSONArray.length(); ++i) {
				JSONObject responseJSONObject = responseJSON.getJSONArray("groupattribute").getJSONObject(i);

				String groupId = responseJSONObject.getString("group_id");
				String attributes = responseJSONObject.getString("admin_attributes");

				groupModelEndpointURLsMap.put(groupId, attributes);
			}
		}
		else {
			throw new ApplicationException(ErrorCodeEnum.ERR_21773);
		}

		// ** Fetching customers from model URLs **
		for (Map.Entry<String, String> entry : groupModelEndpointURLsMap.entrySet()) {

			String groupId = entry.getKey();
			String models = entry.getValue();

			Set<String> coreBankingCustomerIds = new HashSet<String>();
			JSONArray modelsJSONArray = new JSONArray(models);

			for (int i = 0; i < modelsJSONArray.length(); ++i) {
				String endpointURL = modelsJSONArray.getJSONObject(i).getString("endpoint_url");
				List<String> customerIdsFromOneURL = CampaignHandler.getCustomerIdsFromCoreBanking(requestInstance, endpointURL);

				for (String customerId : customerIdsFromOneURL) {
					coreBankingCustomerIds.add(customerId);
				}
			}

			List<String> dbxCustomerIds = new ArrayList<String>();

			for (String coreBankingCustomerId : coreBankingCustomerIds) {
				dbxCustomerIds.add(coreBankingToDBXCustomerMap.get(coreBankingCustomerId));
			}

			addCustomersToCampaignGroups(requestInstance, userId, dbxCustomerIds, groupId);
		}

		return null;
	}

	public static Map<String, AttributeBean> getAllAttributes(DataControllerRequest requestInstance) throws ApplicationException {

		Map<String, AttributeBean> attributeMap = new HashMap<>();

		// ** Reading from 'attribute' table **
		String response = Executor.invokeService(ServiceURLEnum.ATTRIBUTE_READ, new HashMap<>(), null, requestInstance);
		JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

		if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
				&& responseJSON.getInt(FabricConstants.OPSTATUS) == 0 && responseJSON.has("attribute")) {

			JSONArray responseJSONArray = responseJSON.getJSONArray("attribute");

			for (int i = 0; i < responseJSONArray.length(); ++i) {
				JSONObject responseJSONObject = responseJSONArray.getJSONObject(i);

				AttributeBean attribute = new AttributeBean();

				attribute.setId(responseJSONObject.getString("id"));
				attribute.setEndpointAttributeId(responseJSONObject.getString("endpoint_attribute_id"));
				attribute.setName(responseJSONObject.getString("name"));
				attribute.setType(responseJSONObject.getString("attributetype"));
				attribute.setCriterias(responseJSONObject.getString("criterias"));

				if (!StringUtils.isBlank(responseJSONObject.optString("options"))) {
					attribute.setOptions(responseJSONObject.getString("options"));
				}
				if (!StringUtils.isBlank(responseJSONObject.optString("range"))) {
					attribute.setRange(responseJSONObject.getString("range"));
				}
				if (!StringUtils.isBlank(responseJSONObject.optString("helptext"))) {
					attribute.setHelpText(responseJSONObject.getString("helptext"));
				}

				attributeMap.put(responseJSONObject.getString("id"), attribute);
			}
		}
		else {
			throw new ApplicationException(ErrorCodeEnum.ERR_21761);
		}

		return attributeMap;
	}

	public static List<ModelBean> getAllModels(DataControllerRequest requestInstance) throws ApplicationException {

		List<ModelBean> modelList = new ArrayList<>();

		// ** Reading from 'model' table **
		String response = Executor.invokeService(ServiceURLEnum.MODEL_READ, new HashMap<>(), null, requestInstance);
		JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

		if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
				&& responseJSON.getInt(FabricConstants.OPSTATUS) == 0 && responseJSON.has("model")) {

			JSONArray responseJSONArray = responseJSON.getJSONArray("model");

			for (int i = 0; i < responseJSONArray.length(); ++i) {
				JSONObject responseJSONObject = responseJSONArray.getJSONObject(i);

				ModelBean model = new ModelBean();

				model.setId(responseJSONObject.getString("id"));
				model.setName(responseJSONObject.getString("name"));
				model.setEndpointURL(responseJSONObject.getString("endpoint_url"));

				modelList.add(model);
			}
		}
		else {
			throw new ApplicationException(ErrorCodeEnum.ERR_21777);
		}

		return modelList;
	}

	public static Set<String> getAttributeCriterias(DataControllerRequest requestInstance) throws ApplicationException {

		Set<String> attributeCriteriaSet = new HashSet<>();

		// ** Reading from 'attributecriteria' table **
		String response = Executor.invokeService(ServiceURLEnum.ATTRIBUTECRITERIA_READ, new HashMap<>(), null,
				requestInstance);
		JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

		if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
				&& responseJSON.getInt(FabricConstants.OPSTATUS) == 0 && responseJSON.has("attributecriteria")) {

			JSONArray responseJSONArray = responseJSON.getJSONArray("attributecriteria");

			for (int i = 0; i < responseJSONArray.length(); ++i) {
				JSONObject attributeCriteriaResponse = responseJSONArray.getJSONObject(i);

				attributeCriteriaSet.add(attributeCriteriaResponse.getString("id"));
			}
		}
		else {
			throw new ApplicationException(ErrorCodeEnum.ERR_21765);
		}

		return attributeCriteriaSet;
	}

	public static Map<String, String> getAttributeOptions(DataControllerRequest requestInstance) throws ApplicationException {

		Map<String, String> attributeOptionMap = new HashMap<>();

		// ** Reading from 'attributeoption' table **
		String response = Executor.invokeService(ServiceURLEnum.ATTRIBUTEOPTION_READ, new HashMap<>(), null,
				requestInstance);
		JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

		if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
				&& responseJSON.getInt(FabricConstants.OPSTATUS) == 0 && responseJSON.has("attributeoption")) {

			JSONArray responseJSONArray = responseJSON.getJSONArray("attributeoption");

			for (int i = 0; i < responseJSONArray.length(); ++i) {
				JSONObject attributeOptionResponse = responseJSONArray.getJSONObject(i);

				attributeOptionMap.put(attributeOptionResponse.getString("id"),
						attributeOptionResponse.getString("endpoint_attributeoption_id"));
			}
		}
		else {
			throw new ApplicationException(ErrorCodeEnum.ERR_21769);
		}

		return attributeOptionMap;
	}

	public static List<ModelBean> getModelsByAttribute(DataControllerRequest requestInstance, String attributeId) throws ApplicationException {

		List<ModelBean> models = new ArrayList<ModelBean>();

		// ** Reading from 'campaigngroup_membergroup_read' stored procedure **
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.put("_attributeId", attributeId);

		String readResponse = Executor.invokeService(ServiceURLEnum.CAMPAIGN_C360_MODEL_GET_PROC, postParametersMap,
				null, requestInstance);
		JSONObject readResponseJSON = CommonUtilities.getStringAsJSONObject(readResponse);

		if (readResponseJSON != null && readResponseJSON.has(FabricConstants.OPSTATUS)
				&& readResponseJSON.getInt(FabricConstants.OPSTATUS) == 0 && readResponseJSON.has("records")) {

			Dataset campaigngroupDataset = new Dataset();
			campaigngroupDataset.setId("campaignGroups");

			JSONArray readResponseJSONArray = readResponseJSON.optJSONArray("records");
			for (int i = 0; i < readResponseJSONArray.length(); ++i) {

				JSONObject readResponseJSONObject = readResponseJSONArray.getJSONObject(i);
				ModelBean model = new ModelBean();

				model.setId(readResponseJSONObject.getString("id"));
				model.setName(readResponseJSONObject.getString("name"));
				model.setEndpointURL(readResponseJSONObject.getString("endpoint_url"));

				models.add(model);
			}
		}
		else {
			throw new ApplicationException(ErrorCodeEnum.ERR_21777);
		}

		return models;
	}

	public static Map<String, String> getCoreBankingToDBXCustomersMap(DataControllerRequest requestInstance) throws ApplicationException {

		Map<String, String> customersMap = new HashMap<>();

		// ** Reading from 'backendidentifier' table **
		String response = Executor.invokeService(ServiceURLEnum.BACKENDIDENTIFIER_READ, new HashMap<>(), null,
				requestInstance);
		JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

		if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
				&& responseJSON.getInt(FabricConstants.OPSTATUS) == 0 && responseJSON.has("backendidentifier")) {

			JSONArray responseJSONArray = responseJSON.getJSONArray("backendidentifier");

			for (int i = 0; i < responseJSONArray.length(); ++i) {
				JSONObject attributeOptionResponse = responseJSONArray.getJSONObject(i);

				customersMap.put(attributeOptionResponse.getString("BackendId"),
						attributeOptionResponse.getString("Customer_id"));
			}
		}
		else {
			throw new ApplicationException(ErrorCodeEnum.ERR_21791);
		}

		return customersMap;
	}

	public static List<String> getCustomerIdsFromCoreBanking(DataControllerRequest requestInstance, String endpointURL) throws ApplicationException {

		List<String> customerIds = new ArrayList<String>();

		/*
		 * -- T24 TRANSACT services are HTTP basic authorization type. To invoke the service, a request header needs to
		 * be added with key 'Authorization'. Value of the header -> 'Basic $authKey' where authKey = base64-encoded
		 * equivalent of username:password (colon separated username & password) At the time of writing the service,
		 * username: admin@temenos.com & password: password --
		 */

		Map<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("Authorization", "Basic YWRtaW5AdGVtZW5vcy5jb206cGFzc3dvcmQ=");

		String authToken = requestInstance.getHeader(HTTPOperations.X_KONY_AUTHORIZATION_HEADER);

		String response = HTTPOperations.hitGETServiceAndGetResponse(endpointURL, requestHeaders, authToken);
		JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

		if (responseJSON != null && responseJSON.has("value")) {

			JSONArray responseJSONArray = responseJSON.getJSONArray("value");

			for (int i = 0; i < responseJSONArray.length(); ++i) {
				customerIds.add(responseJSONArray.getJSONObject(i).getString("CustomerNumber"));
			}
		}
		else {
			throw new ApplicationException(ErrorCodeEnum.ERR_21790);
		}

		return customerIds;
	}

	public static List<String> addCustomersToCampaignGroups(DataControllerRequest requestInstance, String userId, List<String> customers, String groupId) {

		for (String customer : customers) {
			Map<String, String> campaignGroupMap = new HashMap<>();
			campaignGroupMap.put("Customer_id", customer);
			campaignGroupMap.put("Group_id", groupId);
			campaignGroupMap.put("createdby", userId);

			Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_CREATE, campaignGroupMap, null, requestInstance);
		}

		return null;
	}
}
