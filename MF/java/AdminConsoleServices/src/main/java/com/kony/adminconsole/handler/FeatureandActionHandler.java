package com.kony.adminconsole.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.dto.GroupActionLimitView;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * FeatureandActionHandler is used to maintain the modular code for performing actions on feature and actions module
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class FeatureandActionHandler {
	private static final Logger LOG = Logger.getLogger(FeatureandActionHandler.class);

	public static HashMap<String, JSONObject> getFeatureDisplayContent(String filter, DataControllerRequest requestInstance, Result processedResult) {

		Map<String, String> inputMap = new HashMap<String, String>();
		inputMap.put(ODataQueryConstants.FILTER, filter);
		LOG.error("Feature Filter:" + filter);

		JSONObject readCustomerResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
				ServiceURLEnum.FEATUREDISPLAYNAMEDESCRIPTION_READ, inputMap, null, requestInstance));
		if (readCustomerResponse == null || !readCustomerResponse.has(FabricConstants.OPSTATUS) ||
				readCustomerResponse.getInt(FabricConstants.OPSTATUS) != 0 || !readCustomerResponse.has("featuredisplaynamedescription")) {
			processedResult.addParam(new Param("FailureReason", String.valueOf(readCustomerResponse), FabricConstants.STRING));
			ErrorCodeEnum.ERR_21400.setErrorCode(processedResult);
			return null;
		}

		HashMap<String, JSONObject> featureContentMap = new HashMap<>();
		JSONArray featureDisplayContent = readCustomerResponse.getJSONArray("featuredisplaynamedescription");
		for (Object featureObject : featureDisplayContent) {
			JSONObject featureJSON = (JSONObject) featureObject;
			featureContentMap.put(featureJSON.getString("Feature_id"), featureJSON);
		}
		return featureContentMap;
	}

	public static HashMap<String, JSONObject> getActionDisplayContent(String filter, DataControllerRequest requestInstance, Result processedResult) {

		Map<String, String> inputMap = new HashMap<String, String>();
		inputMap.put(ODataQueryConstants.FILTER, filter);
		LOG.error("Action Filter:" + filter);
		JSONObject readCustomerResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
				ServiceURLEnum.ACTIONDISPLAYNAMEDESCRIPTION_READ, inputMap, null, requestInstance));
		if (readCustomerResponse == null || !readCustomerResponse.has(FabricConstants.OPSTATUS) ||
				readCustomerResponse.getInt(FabricConstants.OPSTATUS) != 0 || !readCustomerResponse.has("actiondisplaynamedescription")) {
			processedResult.addParam(new Param("FailureReason", String.valueOf(readCustomerResponse), FabricConstants.STRING));
			ErrorCodeEnum.ERR_21401.setErrorCode(processedResult);
			return null;
		}

		HashMap<String, JSONObject> actionContentMap = new HashMap<>();
		JSONArray actionDisplayContent = readCustomerResponse.getJSONArray("actiondisplaynamedescription");
		for (Object actionObject : actionDisplayContent) {
			JSONObject actionJSON = (JSONObject) actionObject;
			actionContentMap.put(actionJSON.getString("Action_id"), actionJSON);
		}
		return actionContentMap;
	}
	
	public static JSONArray getCustomerGroups(String customerId, DataControllerRequest requestInstance, Result processedResult) throws ApplicationException {

		Map<String, String> inputMap = new HashMap<String, String>();
		inputMap.put(ODataQueryConstants.FILTER, "Customer_id eq '"+customerId+"'");
		JSONObject readResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
				ServiceURLEnum.CUSTOMERGROUPINFO_VIEW_READ, inputMap, null, requestInstance));
		if (readResponse == null || !readResponse.has(FabricConstants.OPSTATUS) ||
				readResponse.getInt(FabricConstants.OPSTATUS) != 0 || !readResponse.has("customergroupinfo_view")) {
			processedResult.addParam(new Param("FailureReason", String.valueOf(readResponse), FabricConstants.STRING));
			throw new ApplicationException(ErrorCodeEnum.ERR_21852);
		}
		return readResponse.getJSONArray("customergroupinfo_view");
	}
	
	public static JSONArray getGroupActionLimits(String filter, DataControllerRequest requestInstance, Result processedResult) throws ApplicationException {

		Map<String, String> inputMap = new HashMap<String, String>();
		inputMap.put(ODataQueryConstants.FILTER, filter);
		JSONObject readResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
				ServiceURLEnum.GROUPACTIONLIMIT_READ, inputMap, null, requestInstance));
		if (readResponse == null || !readResponse.has(FabricConstants.OPSTATUS) ||
				readResponse.getInt(FabricConstants.OPSTATUS) != 0 || !readResponse.has("groupactionlimit")) {
			processedResult.addParam(new Param("FailureReason", String.valueOf(readResponse), FabricConstants.STRING));
			throw new ApplicationException(ErrorCodeEnum.ERR_21853);
		}
		return readResponse.getJSONArray("groupactionlimit");
	}
	
	public static Map<String,JSONObject> getAllActionDetails(DataControllerRequest requestInstance, Result processedResult) throws ApplicationException {

		Map<String, String> inputMap = new HashMap<String, String>();
		JSONObject readResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
				ServiceURLEnum.FEATURE_ACTIONS_VIEW_READ, inputMap, null, requestInstance));
		if (readResponse == null || !readResponse.has(FabricConstants.OPSTATUS) ||
				readResponse.getInt(FabricConstants.OPSTATUS) != 0 || !readResponse.has("feature_actions_view")) {
			processedResult.addParam(new Param("FailureReason", String.valueOf(readResponse), FabricConstants.STRING));
			throw new ApplicationException(ErrorCodeEnum.ERR_21856);
		}
		JSONArray actions = readResponse.getJSONArray("feature_actions_view");
		Map<String, JSONObject> actionsMap = new HashMap<>();
		for(Object actionObject : actions) {
			JSONObject action = (JSONObject) actionObject;
			if(actionsMap.containsKey(action.getString("id"))) {
				JSONObject actionInMap = actionsMap.get(action.getString("id"));
				if(action.has("LimitType_id")) {
					JSONObject limits = actionInMap.getJSONObject("limits");
					limits.put(action.getString("LimitType_id"), action.getString("value"));
				}
			}else {
				if(action.has("LimitType_id")) {
					JSONObject limits = new JSONObject();
					limits.put(action.getString("LimitType_id"), action.getString("value"));
					action.put("limits", limits);
					action.remove("LimitType_id");
					action.remove("value");
				}
				actionsMap.put(action.getString("id"), action);	
			}
			
		}
		return actionsMap;
	}
	
	public static JSONArray getAllFeatureActions(String filterForAllActions, DataControllerRequest requestInstance, Result processedResult) throws ApplicationException {
		Map<String, String> inputMap = new HashMap<String, String>();
		if(StringUtils.isNotBlank(filterForAllActions)) {
			inputMap.put(ODataQueryConstants.FILTER, filterForAllActions);
		}
		JSONObject readResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
				ServiceURLEnum.FEATURE_ACTIONS_VIEW_READ, inputMap, null, requestInstance));
		if (readResponse == null || !readResponse.has(FabricConstants.OPSTATUS) ||
				readResponse.getInt(FabricConstants.OPSTATUS) != 0 || !readResponse.has("feature_actions_view")) {
			processedResult.addParam(new Param("FailureReason", String.valueOf(readResponse), FabricConstants.STRING));
			throw new ApplicationException(ErrorCodeEnum.ERR_21856);
		}
		return readResponse.getJSONArray("feature_actions_view");
	}
	
	public static Map<String,String> getLimitSubTypeMap(DataControllerRequest requestInstance, Result processedResult) throws ApplicationException {

		Map<String, String> inputMap = new HashMap<String, String>();
		JSONObject readCustomerResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
				ServiceURLEnum.LIMITSUBTYPE_READ, inputMap, null, requestInstance));
		if (readCustomerResponse == null || !readCustomerResponse.has(FabricConstants.OPSTATUS) ||
				readCustomerResponse.getInt(FabricConstants.OPSTATUS) != 0 || !readCustomerResponse.has("limitsubtype")) {
			processedResult.addParam(new Param("FailureReason", String.valueOf(readCustomerResponse), FabricConstants.STRING));
			throw new ApplicationException(ErrorCodeEnum.ERR_21855);
		}
		JSONArray limits = readCustomerResponse.getJSONArray("limitsubtype");
		Map<String, String> limitsMap = new HashMap<>();
		for(Object limitObject : limits) {
			JSONObject limit = (JSONObject) limitObject;
			limitsMap.put(limit.getString("id"), limit.getString("LimitType_id"));
		}
		return limitsMap;
	}
	
	public static JSONArray getBusinessAccountsOfCustomer(String customerId, DataControllerRequest requestInstance, Result processedResult) throws ApplicationException {

		Map<String, String> inputMap = new HashMap<String, String>();
		inputMap.put(ODataQueryConstants.FILTER, "Customer_id eq '"+customerId+"'");
		JSONObject readCustomerResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
				ServiceURLEnum.CUSTOMERACCOUNTS_READ, inputMap, null, requestInstance));
		if (readCustomerResponse == null || !readCustomerResponse.has(FabricConstants.OPSTATUS) ||
				readCustomerResponse.getInt(FabricConstants.OPSTATUS) != 0 || !readCustomerResponse.has("customeraccounts")) {
			processedResult.addParam(new Param("FailureReason", String.valueOf(readCustomerResponse), FabricConstants.STRING));
			throw new ApplicationException(ErrorCodeEnum.ERR_21854);
		}
		return readCustomerResponse.getJSONArray("customeraccounts");
	}
	
	public static JSONArray getCustomerDirectPermissions(String customerId, DataControllerRequest requestInstance, Result processedResult) throws ApplicationException {

		Map<String, String> inputMap = new HashMap<String, String>();
		inputMap.put(ODataQueryConstants.FILTER, "Customer_id eq '"+customerId+"'");
		JSONObject readCustomerResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
				ServiceURLEnum.CUSTOMERACTION_READ, inputMap, null, requestInstance));
		if (readCustomerResponse == null || !readCustomerResponse.has(FabricConstants.OPSTATUS) ||
				readCustomerResponse.getInt(FabricConstants.OPSTATUS) != 0 || !readCustomerResponse.has("customeraction")) {
			processedResult.addParam(new Param("FailureReason", String.valueOf(readCustomerResponse), FabricConstants.STRING));
			throw new ApplicationException(ErrorCodeEnum.ERR_21854);
		}
		return readCustomerResponse.getJSONArray("customeraction");
	}
	
	public static JSONArray getGroupFeatureActions(String groupId, DataControllerRequest requestInstance, Result processedResult) throws ApplicationException {

		Map<String, String> inputMap = new HashMap<String, String>();
		inputMap.put(ODataQueryConstants.FILTER, "Group_id eq '"+groupId+"'");
		JSONObject readResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
				ServiceURLEnum.GROUP_FEATURES_ACTIONS_VIEW_READ, inputMap, null, requestInstance));
		if (readResponse == null || !readResponse.has(FabricConstants.OPSTATUS) ||
				readResponse.getInt(FabricConstants.OPSTATUS) != 0 || !readResponse.has("group_features_actions_view")) {
			processedResult.addParam(new Param("FailureReason", String.valueOf(readResponse), FabricConstants.STRING));
			throw new ApplicationException(ErrorCodeEnum.ERR_21857);
		}
		return readResponse.getJSONArray("group_features_actions_view");
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, JSONObject> getCompanyActions(String companyId, DataControllerRequest requestInstance, Result processedResult) throws ApplicationException {

		Map<String, String> inputMap = new HashMap<String, String>();
		inputMap.put(ODataQueryConstants.FILTER, "Organisation_id eq '"+companyId+"'");
		
		JSONObject readCompanyResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
				ServiceURLEnum.ORGANISATION_ACTION_LIMITS_VIEW_READ, inputMap, null, requestInstance));
		if (readCompanyResponse == null || !readCompanyResponse.has(FabricConstants.OPSTATUS) ||
				readCompanyResponse.getInt(FabricConstants.OPSTATUS) != 0 || !readCompanyResponse.has("organisation_action_limits_view")) {
			processedResult.addParam(new Param("FailureReason", String.valueOf(readCompanyResponse), FabricConstants.STRING));
			throw new ApplicationException(ErrorCodeEnum.ERR_21851);
		}

		Map<String, JSONObject> actionsMap = new HashMap<>();
		JSONArray actionsArray = readCompanyResponse.getJSONArray("organisation_action_limits_view");
		for (Object actionObject : actionsArray) {
			JSONObject actionJSON = (JSONObject) actionObject;
			if(actionsMap.containsKey(actionJSON.getString("Action_id"))) {
				JSONObject action = actionsMap.get(actionJSON.getString("Action_id"));
				Map<String,JSONObject> limits = ((Map<String,JSONObject>) action.get("limits"));
				JSONObject limit = limits.get(actionJSON.getString("Action_id"));
				if(limit != null) {
					limit.put(actionJSON.getString("LimitSubType_id"), actionJSON.optString("value"));
				}else {
					JSONObject limitSubtype = new JSONObject();
					limitSubtype.put(actionJSON.getString("LimitSubType_id"), actionJSON.optString("value"));
					limits.put(actionJSON.getString("LimitType_id"), limitSubtype);
				}
			}else {
				JSONObject action = new JSONObject();
				action.put("code", actionJSON.getString("Action_id"));
				action.put("isAccountLevel", actionJSON.getString("isAccountLevel"));
				
				Map<String,JSONObject> limits = new HashMap<>();
				if(actionJSON.has("LimitType_id")) {
					JSONObject limitSubtype = new JSONObject();
					limitSubtype.put(actionJSON.getString("LimitSubType_id"), actionJSON.optString("value"));
					limits.put(actionJSON.getString("LimitType_id"), limitSubtype);
				}
				action.put("limits", limits);
				
			}
		}
		return actionsMap;
	}
	
	public static GroupActionLimitView getGroupActionObject(JSONObject actionObject) {
		GroupActionLimitView groupActionLimitView = new GroupActionLimitView();
		groupActionLimitView.setGroup_id(actionObject.optString("Group_id"));
		groupActionLimitView.setAction_id(actionObject.optString("Action_id"));
		groupActionLimitView.setLimitType_id(actionObject.optString("LimitType_id"));
		groupActionLimitView.setValue(actionObject.optString("value"));
		groupActionLimitView.setGroupactionlimit_id(actionObject.optString("groupactionlimit_id"));
		groupActionLimitView.setAction_name(actionObject.optString("Action_name"));
		groupActionLimitView.setAction_description(actionObject.optString("Action_description"));
		groupActionLimitView.setAction_Type_id(actionObject.optString("Action_Type_id"));
		groupActionLimitView.setFeature_id(actionObject.optString("Feature_id"));
		groupActionLimitView.setIsMFAApplicable(actionObject.optString("isMFAApplicable"));
		groupActionLimitView.setIsAccountLevel(actionObject.optString("isAccountLevel"));
		groupActionLimitView.setFeature_name(actionObject.optString("Feature_name"));
		groupActionLimitView.setFeature_description(actionObject.optString("Feature_description"));
		groupActionLimitView.setFeature_Type_id(actionObject.optString("Feature_Type_id"));
		groupActionLimitView.setFeature_Status_id(actionObject.optString("Feature_Status_id"));
		return groupActionLimitView;
	}
	
	public static GroupActionLimitView getGroupActionObjectFromAction(JSONObject actionObject) {
		GroupActionLimitView groupActionLimitView = new GroupActionLimitView();
		groupActionLimitView.setAction_id(actionObject.optString("id"));
		groupActionLimitView.setLimitType_id(actionObject.optString("LimitType_id"));
		groupActionLimitView.setValue(actionObject.optString("value"));
		groupActionLimitView.setAction_name(actionObject.optString("action_name"));
		groupActionLimitView.setAction_description(actionObject.optString("action_description"));
		groupActionLimitView.setAction_Type_id(actionObject.optString("action_Type_id"));
		groupActionLimitView.setFeature_id(actionObject.optString("Feature_id"));
		groupActionLimitView.setIsMFAApplicable(actionObject.optString("isMFAApplicable"));
		groupActionLimitView.setIsAccountLevel(actionObject.optString("isAccountLevel"));
		groupActionLimitView.setFeature_name(actionObject.optString("feature_name"));
		groupActionLimitView.setFeature_description(actionObject.optString("feature_description"));
		groupActionLimitView.setFeature_Type_id(actionObject.optString("feature_Type_id"));
		groupActionLimitView.setFeature_Status_id(actionObject.optString("feature_status_id"));
		return groupActionLimitView;
	}

}
