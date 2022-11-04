package com.kony.adminconsole.service.featuresandactions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.dto.CustomerBean;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.handler.FeatureandActionHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/*
  Todo:
	1. Read Customer direct permissions: default limits needs to be applied
	
*/
public class CustomerFeaturesAndActionsGet implements JavaService2 {
	private static final String INPUT_USERNAME = "username";
	
	private static final String STATUS_FEATURE_ACTIVE = "SID_FEATURE_ACTIVE";
	
	private static final String ROLE_SMALL_BUSINESS = "TYPE_ID_SMALL_BUSINESS";
	private static final String ROLE_MICRO_BUSINESS = "TYPE_ID_MICRO_BUSINESS";
	private static final String ROLE_RETAIL = "TYPE_ID_RETAIL";
	private static final Logger LOG = Logger.getLogger(CustomerFeaturesAndActionsGet.class);
	
	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {
		Result processedResult = new Result();

		try {
			
			String username = requestInstance.getParameter(INPUT_USERNAME);
			Map<String, JSONObject> finalPermissions = new HashMap<>();
			if(StringUtils.isBlank(username)) {
				ErrorCodeEnum.ERR_20533.setErrorCode(processedResult);
				return processedResult;
			}
			CustomerBean customerBean = CustomerHandler.getCustomerGeneralInformation(username,requestInstance);
			if(customerBean == null) {
				ErrorCodeEnum.ERR_21850.setErrorCode(processedResult);
				return processedResult;
			}
			Map<String,JSONObject> companyActions = null;
			JSONArray businessAccounts = null;
			if(StringUtils.isNotBlank(customerBean.getOrganizationId())) {
				companyActions = FeatureandActionHandler.getCompanyActions(customerBean.getOrganizationId(), requestInstance, processedResult);
				
				businessAccounts = FeatureandActionHandler.getBusinessAccountsOfCustomer(customerBean.getId(),  requestInstance, processedResult);
			}
			
			Map<String,JSONObject> actionDetails = FeatureandActionHandler.getAllActionDetails(requestInstance, processedResult);
			
			JSONArray customergroups = FeatureandActionHandler.getCustomerGroups(customerBean.getId(), requestInstance, processedResult);
			
			//Process all associate roles
			Map<String, String> groupTypeMap = new HashMap<>();
			String filter = StringUtils.EMPTY;
			for(Object groupObj : customergroups) {
				JSONObject group = (JSONObject) groupObj;
				groupTypeMap.put(group.getString("Group_id"), group.getString("Group_Type_id"));
				JSONObject groupTypeActions = new JSONObject();
				groupTypeActions.put("features", new JSONObject());
				groupTypeActions.put("actions", new JSONObject());
				if(group.getString("Group_Type_id").equals(ROLE_MICRO_BUSINESS) 
						|| group.getString("Group_Type_id").equalsIgnoreCase(ROLE_SMALL_BUSINESS)) {
					if(businessAccounts != null) {
						for(int i=0; i<businessAccounts.length(); i++) {
							JSONObject account = new JSONObject();
							account.put("features", new JSONObject());
							account.put("actions", new JSONObject());
							groupTypeActions.put(businessAccounts.getJSONObject(i).getString("Account_id"), account);
						}
					}
				}
				finalPermissions.put(group.getString("Group_Type_id"), groupTypeActions);
				if(StringUtils.isNotBlank(filter)) {
					filter += " or ";
				}
				filter = "Group_id eq '"+ group.getString("Group_id") + "'";
			}
			
			//Process indirect permissions via roles
			JSONArray groupactions = FeatureandActionHandler.getGroupActionLimits(filter, requestInstance, processedResult);
			
			for(int i=0; i<groupactions.length(); i++) {
				JSONObject current_object = groupactions.getJSONObject(i);
				String current_group_type = groupTypeMap.get(current_object.getString("Group_id"));
				JSONObject current_featuresandActions = finalPermissions.get(current_group_type);
				String current_action = current_object.getString("Action_id");
				
				if(!actionDetails.get(current_action).getString("feature_status_id").equalsIgnoreCase(STATUS_FEATURE_ACTIVE)) {
					// Ignore the actions whose feature is not active
					continue;
				}
				
				if(current_group_type.equals(ROLE_MICRO_BUSINESS) || current_group_type.equals(ROLE_SMALL_BUSINESS)) {
					
					if(!companyActions.containsKey(current_action)) {
						continue;
					}
					if(actionDetails.get(current_action).getString("isAccountLevel").equalsIgnoreCase("TRUE")) {
						Iterator<String> keys = current_featuresandActions.keys();
						while(keys.hasNext()) {
						    String key = keys.next();
						    
						    if(key.equals("features") || key.equals("actions")) {
						    	continue;
						    }
						    
						    JSONObject features = current_featuresandActions.getJSONObject(key).getJSONObject("features");
						    JSONObject actions = current_featuresandActions.getJSONObject(key).getJSONObject("actions");
						    processIndirectPermission(current_object, actions, features, actionDetails);
						}
					}else {
						JSONObject features = current_featuresandActions.getJSONObject("features");
					    JSONObject actions = current_featuresandActions.getJSONObject("actions");
					    processIndirectPermission(current_object, actions, features, actionDetails);
					}
				}else if(current_group_type.equals(ROLE_RETAIL)) {
					// Retail level
					JSONObject features = current_featuresandActions.getJSONObject("features");
				    JSONObject actions = current_featuresandActions.getJSONObject("actions");
				    processIndirectPermission(current_object, actions, features, actionDetails);
				}
			}
			
			//Process direct permissions
			JSONArray customerDirectPermissions = FeatureandActionHandler.getCustomerDirectPermissions(customerBean.getId(), requestInstance, processedResult);
			
			for(int i=0; i<customerDirectPermissions.length(); i++) {
				JSONObject action = customerDirectPermissions.getJSONObject(i);
				String current_role_type = action.getString("RoleType_id");
				String current_account = action.getString("Account_id");
				String isActionAccountLevel = actionDetails.get(action.getString("Action_id")).getString("isAccountLevel");
				JSONObject current_featuresandActions = finalPermissions.get(current_role_type);
				if(current_role_type.equals(ROLE_MICRO_BUSINESS) || current_role_type.equals(ROLE_SMALL_BUSINESS)) {
					if(finalPermissions.containsKey(current_role_type)) {
						
						if(isActionAccountLevel.equalsIgnoreCase("true")) {
							Iterator<String> keys = current_featuresandActions.keys();
							while(keys.hasNext()) {
							    String key = keys.next();
							    
							    if(current_account.equals(key)) {
							    	JSONObject actions = current_featuresandActions.getJSONObject(key).getJSONObject("actions");
									JSONObject features = current_featuresandActions.getJSONObject(key).getJSONObject("features");
									
									processDirectPermission(action,actions, features, actionDetails);
							    }
							    
							}
							    
						}else {
							JSONObject actions = current_featuresandActions.getJSONObject("actions");
							JSONObject features = current_featuresandActions.getJSONObject("features");
							
							processDirectPermission(action,actions, features, actionDetails);
						}
						
					}
					
				}else if(current_role_type.equals(ROLE_RETAIL)) {
					
					if(finalPermissions.containsKey(current_role_type)) {
						JSONObject actions = current_featuresandActions.getJSONObject("actions");
						JSONObject features = current_featuresandActions.getJSONObject("features");
						
						processDirectPermission(action,actions, features, actionDetails);
					}
				}
			}
			
			constructFinalResult(finalPermissions, processedResult, actionDetails);
			
			return processedResult;
			
			
		}catch (ApplicationException ae) {
			LOG.error("Unexpected error", ae);
			ErrorCodeEnum.ERR_20001.setErrorCode(processedResult);
		}
		catch (Exception e) {
			LOG.error("Unexpected error", e);
			ErrorCodeEnum.ERR_20001.setErrorCode(processedResult);
		}
		return processedResult;
	}
	
	private void processIndirectPermission(JSONObject action, JSONObject actions, JSONObject features, Map<String, JSONObject> actionDetails) {
		
		String current_action = action.getString("Action_id");
		String current_limit = action.optString("LimitType_id");
		String current_limit_value = action.optString("value");
		String current_feature = actionDetails.get(current_action).getString("Feature_id");
		
		if(!features.has(current_feature)) {
			JSONObject featureObject = new JSONObject();
			featureObject.put("code", current_feature);
			featureObject.put("numberOfActions", 1);
			features.put(current_feature, featureObject);
		}else {
			JSONObject featureObject = features.getJSONObject(current_feature);
			featureObject.put("numberOfActions", featureObject.getInt("numberOfActions")+1);
		}
		if(!actions.has(current_action)) {
			JSONObject actionObject = new JSONObject();
			actionObject.put("code", current_action);
			
			if(StringUtils.isNotBlank(current_limit)) {
				JSONObject limits = new JSONObject();
				
				limits.put(current_limit, current_limit_value);
				actionObject.put("limits", limits);
			}
			actions.put(current_action, actionObject);
		}else {
			// update existing action
			JSONObject actionObject = actions.getJSONObject(current_action);
			
			if(StringUtils.isNotBlank(current_limit)) {
				
				JSONObject limits = new JSONObject();
				if(actionObject.has("limits")) {
					limits = actionObject.getJSONObject("limits");
				}
				
				if(limits.has(current_limit)) {
					// Choose min of both
					current_limit_value = String.valueOf(Math.min(Double.parseDouble(current_limit_value), Double.parseDouble(limits.getString(current_limit))));
				}
				limits.put(current_limit, current_limit_value);
				actionObject.put("limits", limits);
			}
		}
	}

	private void processDirectPermission(JSONObject action, JSONObject actions, JSONObject features, Map<String, JSONObject> actionDetails) {
		
		String current_action = action.getString("Action_id");
		String current_isallowed = action.getString("isAllowed");
		if(current_isallowed.equalsIgnoreCase("true")) {
			if(actions.has(current_action)) {
				return;
			}
			JSONObject a = new JSONObject();
			a.put("code", current_action);
			// TODO: add limits from action level if available.
			actions.put(current_action, a);
		}else {
			actions.remove(current_action);
			String feature_potential_to_be_removed = actionDetails.get(current_action).getString("Feature_id");
			JSONObject feature = features.getJSONObject(feature_potential_to_be_removed);
			feature.put("numberOfActions", feature.getInt("numberOfActions")-1);
			
			if(feature.getInt("numberOfActions") == 0) {
				features.remove(feature_potential_to_be_removed);
			}
			
		}
	}
	
	private void constructFinalResult(Map<String, JSONObject> finalPermissions, Result processedResult, Map<String,JSONObject> actionDetails) {
		
		for(Map.Entry<String, JSONObject> e: finalPermissions.entrySet()) {
			Record r = customconstructRecordFromJSONObject(e.getValue(), actionDetails);
			r.setId(e.getKey());
			processedResult.addRecord(r);
		}
	}
	
	private static Record customconstructRecordFromJSONObject(JSONObject jsonObject, Map<String,JSONObject> actionDetails) {
		Record response = new Record();
		if (jsonObject == null || jsonObject.length() == 0) {
			return response;
		}
		Iterator<String> keys = jsonObject.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (jsonObject.get(key) instanceof Integer) {
				Param param = new Param(key, jsonObject.get(key).toString(), FabricConstants.INT);
				response.addParam(param);

			}
			else if (jsonObject.get(key) instanceof Boolean) {
				Param param = new Param(key, jsonObject.get(key).toString(), FabricConstants.BOOLEAN);
				response.addParam(param);

			}
			else if (jsonObject.get(key) instanceof JSONArray) {
				Dataset dataset = customconstructDatasetFromJSONArray(jsonObject.getJSONArray(key), actionDetails);
				dataset.setId(key);
				response.addDataset(dataset);
			}
			else if (jsonObject.get(key) instanceof JSONObject) {
				//convert object to array for features and actions
				if(key == "features" || key == "actions") {
					JSONArray arrayOfValues = new JSONArray();
					Iterator<String> objectkeys = jsonObject.getJSONObject(key).keys();
					while(objectkeys.hasNext()) {
						arrayOfValues.put(jsonObject.getJSONObject(key).getJSONObject(objectkeys.next()));
					}
					Dataset dataset = customconstructDatasetFromJSONArray(arrayOfValues, actionDetails);
					dataset.setId(key);
					response.addDataset(dataset);
				}else {
					Record record = customconstructRecordFromJSONObject(jsonObject.getJSONObject(key), actionDetails);
					record.setId(key);
					response.addRecord(record);	
				}
				
			}
			else {
				Param param = new Param(key, jsonObject.optString(key), FabricConstants.STRING);
				response.addParam(param);
			}
		}

		return response;
	}
	
	private static Dataset customconstructDatasetFromJSONArray(JSONArray JSONArray, Map<String,JSONObject> actionDetails) {
		Dataset dataset = new Dataset();
		for (int count = 0; count < JSONArray.length(); count++) {
			JSONObject obj = (JSONObject) JSONArray.get(count);
			if(obj.has("limits")) {
				processFILevelLimits(obj, actionDetails);
			}
			Record record = customconstructRecordFromJSONObject(obj, actionDetails);
			dataset.addRecord(record);
		}
		return dataset;
	}
	
	private static void processFILevelLimits(JSONObject obj, Map<String,JSONObject> actionDetails) {
		JSONObject limitsAtFILevel;
		if(actionDetails.get(obj.getString("code")).has("limits")) {
			limitsAtFILevel = actionDetails.get(obj.getString("code")).getJSONObject("limits");
		}else {
			return;
		}
		
		JSONObject customerlimits = obj.getJSONObject("limits");
		
		Iterator<String> keys = limitsAtFILevel.keys();
		while(keys.hasNext()) {
			String key = keys.next();
			String value = "";
			if(customerlimits.has(key)) {
				value = String.valueOf(Math.min(Double.parseDouble(customerlimits.getString(key)), 
						Double.parseDouble(limitsAtFILevel.getString(key))));
				
			}else {
				value = limitsAtFILevel.getString(key);
			}
			customerlimits.put(key, value);
		}
	}
	
}
