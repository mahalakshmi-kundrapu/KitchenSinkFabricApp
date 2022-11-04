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
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class GroupHandler {
	private static final Logger LOG = Logger.getLogger(GroupHandler.class);
	
	public static void removeGroupActions(JSONArray actions, String group_id, DataControllerRequest requestInstance, Result processedResult) throws ApplicationException, Exception {
		
		Map<String, String> inputMap = new HashMap<String, String>();
		String filter = "";
		for(int i=0;i <actions.length(); i++) {
			if(StringUtils.isNotBlank(filter)) {
				filter += " or ";
			}else {
				filter += "(";
			}
			filter += "Action_id eq '"+actions.getString(i)+"'";
		}
		filter += ")";
		filter += "and Group_id eq '"+group_id+"'";
		inputMap.put(ODataQueryConstants.FILTER, filter);
		JSONObject readResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
				ServiceURLEnum.GROUPACTIONLIMIT_READ, inputMap, null, requestInstance));
		if (readResponse == null || !readResponse.has(FabricConstants.OPSTATUS) ||
				readResponse.getInt(FabricConstants.OPSTATUS) != 0 || !readResponse.has("groupactionlimit")) {
			processedResult.addParam(new Param("FailureReason", String.valueOf(readResponse), FabricConstants.STRING));
			LOG.error("Failed to read group action limit");
			throw new ApplicationException(ErrorCodeEnum.ERR_21859);
		}
		JSONArray groupActionRecords = readResponse.getJSONArray("groupactionlimit");
		for(int i=0; i<groupActionRecords.length(); i++) {
			inputMap.clear();
			JSONObject record = groupActionRecords.getJSONObject(i);
			inputMap.put("id", record.getString("id"));
			JSONObject deleteResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
					ServiceURLEnum.GROUPACTIONLIMIT_DELETE, inputMap, null, requestInstance));
			if (deleteResponse == null || !deleteResponse.has(FabricConstants.OPSTATUS) ||
					deleteResponse.getInt(FabricConstants.OPSTATUS) != 0) {
				processedResult.addParam(new Param("FailureReason", String.valueOf(deleteResponse), FabricConstants.STRING));
				throw new ApplicationException(ErrorCodeEnum.ERR_21860);
			}
		}
	}
	
	public static void addGroupActions(JSONArray actionlimits, String group_id, DataControllerRequest requestInstance, Result processedResult) throws ApplicationException,Exception {
		
		Map<String,String> existingMap = getExistingActionsMap(group_id, requestInstance, processedResult);
		
		Map<String, String> inputMap = new HashMap<String, String>();
		inputMap.put("Group_id", group_id);
		for(int i=0; i<actionlimits.length(); i++) {
			String action_id = actionlimits.getJSONObject(i).getString("id");
			String key = group_id;
			key+=action_id;
			inputMap.put("Action_id", action_id);
			if(actionlimits.getJSONObject(i).has("limits")) {
				JSONArray limits = actionlimits.getJSONObject(i).getJSONArray("limits");
				for(int j=0; j<limits.length(); j++) {
					String currentKey = key+limits.getJSONObject(j).getString("id");
					inputMap.put("LimitType_id", limits.getJSONObject(j).getString("id"));
					inputMap.put("value", limits.getJSONObject(j).getString("value"));
					inputMap.put("id", getId(currentKey,existingMap));
					ServiceURLEnum serviceURL = getServiceURL(currentKey,existingMap);
					
					JSONObject createResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
							serviceURL, inputMap, null, requestInstance));
					if (createResponse == null || !createResponse.has(FabricConstants.OPSTATUS) ||
							createResponse.getInt(FabricConstants.OPSTATUS) != 0) {
						processedResult.addParam(new Param("FailureReason", String.valueOf(createResponse), FabricConstants.STRING));
						throw new ApplicationException(ErrorCodeEnum.ERR_21861);
					}
				}
			}else {
				inputMap.put("id", getId(key,existingMap));
				ServiceURLEnum serviceURL = getServiceURL(key,existingMap);
				JSONObject createResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
						serviceURL, inputMap, null, requestInstance));
				if (createResponse == null || !createResponse.has(FabricConstants.OPSTATUS) ||
						createResponse.getInt(FabricConstants.OPSTATUS) != 0) {
					processedResult.addParam(new Param("FailureReason", String.valueOf(createResponse), FabricConstants.STRING));
					throw new ApplicationException(ErrorCodeEnum.ERR_21861);
				}
			}
		}
	}
	
	private static ServiceURLEnum getServiceURL(String key, Map<String,String> existingMap) {
		if(existingMap.containsKey(key)) {
			return ServiceURLEnum.GROUPACTIONLIMIT_UPDATE;
		}
		return ServiceURLEnum.GROUPACTIONLIMIT_CREATE;
	}
	
	private static String getId(String key, Map<String,String> existingMap) {
		if(existingMap.containsKey(key)) {
			return existingMap.get(key);
		}
		return String.valueOf(CommonUtilities.getNewId());
	}
	
	private static Map<String, String> getExistingActionsMap(String group_id, DataControllerRequest requestInstance, Result processedResult) throws ApplicationException{
		Map<String, String> inputMap = new HashMap<String, String>();
		inputMap.put(ODataQueryConstants.FILTER, "Group_id eq '"+group_id+"'");
		JSONObject readResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
				ServiceURLEnum.GROUPACTIONLIMIT_READ, inputMap, null, requestInstance));
		if (readResponse == null || !readResponse.has(FabricConstants.OPSTATUS) ||
				readResponse.getInt(FabricConstants.OPSTATUS) != 0 || !readResponse.has("groupactionlimit")) {
			processedResult.addParam(new Param("FailureReason", String.valueOf(readResponse), FabricConstants.STRING));
			LOG.error("Failed to read group action limit");
			throw new ApplicationException(ErrorCodeEnum.ERR_21859);
		}
		
		Map<String,String> resultMap = new HashMap<>();
		JSONArray groupActions = readResponse.getJSONArray("groupactionlimit");
		for(int i=0; i<groupActions.length(); i++) {
			JSONObject record = groupActions.getJSONObject(i);
			String key = group_id;
			key += record.getString("Action_id");
			if(record.has("LimitType_id")) {
				key += record.getString("LimitType_id");
			}
			resultMap.put(key, record.getString("id"));
		}
		
		return resultMap;
	}
}
