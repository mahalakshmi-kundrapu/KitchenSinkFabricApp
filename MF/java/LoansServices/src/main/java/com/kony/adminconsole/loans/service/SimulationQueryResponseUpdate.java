package com.kony.adminconsole.loans.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.errorhandling.LoansException;
import com.kony.adminconsole.loans.utils.Executor;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class SimulationQueryResponseUpdate implements JavaService2 {
	
	private static final Logger LOGGER = Logger.getLogger(SimulationQueryResponseUpdate.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object invoke(String methodID, Object[] inputArray,
			DataControllerRequest dcRequest, DataControllerResponse dcResponse)
					throws Exception {
		Result result = new Result();
		try {
			JSONArray questionResponseJSON = null;
			JSONArray customerQuerySectionStatusJSON = null;

			Dataset datasets = new Dataset();
			datasets.setId("QuestionResponse");
			ArrayList < Record > records = new ArrayList < Record > ();
			Map inputmap = (Map) inputArray[1];

			if (inputmap.containsKey("QuestionResponse") && inputmap.get("QuestionResponse") != null) {
				questionResponseJSON = new JSONArray(inputmap.get("QuestionResponse").toString());
				inputmap.remove("QuestionResponse");
			}

			if (inputmap.containsKey("CustomerQuerySectionStatus") && inputmap.get("CustomerQuerySectionStatus") != null) {
				customerQuerySectionStatusJSON = new JSONArray(inputmap.get("CustomerQuerySectionStatus").toString());
				inputmap.remove("CustomerQuerySectionStatus");
				for (int i = 0; i < customerQuerySectionStatusJSON.length(); i++) {
					JSONObject record = customerQuerySectionStatusJSON.getJSONObject(i);
					Map paramList = createMapFromJsonObject(record);
					hitCustomerQuerySectionStatusService(paramList, dcRequest);
				}
			}
			if (inputmap.containsKey("id")) {
				inputmap.put("lastmodifiedts", CommonUtilities.convertTimetoISO8601Format(new Date()));
				hitQueryResponseService(inputmap, dcRequest);
			}
			if (questionResponseJSON != null) {
				for (int i = 0; i < questionResponseJSON.length(); i++) {
					JSONObject record = questionResponseJSON.getJSONObject(i);
					Map paramList = createMapFromJsonObject(record);
					if(record.has("softdeleteflag") && record.get("softdeleteflag").equals("1")) {
						hitQuestionResponseDeleteService(paramList, dcRequest);
					}else {
						Result childResult = hitQuestionResponseService(paramList, dcRequest);
						records.add(childResult.getAllDatasets().get(0).getRecord(0));
					}
				}
			}
			datasets.addAllRecords(records);
			result.addDataset(datasets);
			return result;
		} catch(Exception e) {
			LOGGER.error("Error Occured in UpdateQueryResponseNew class", e);
			LoansException unknownException=new LoansException(ErrorCodeEnum.ERR_31000);
			result=unknownException.updateResultObject(result);
			return result;
		}
	}

	public Map<String, String> createMapFromJsonObject(JSONObject jsonObject) {
		Map<String, String> paramList = new HashMap<String, String>();
		JSONArray keys = jsonObject.names();
		for (int i = 0; i < keys.length(); ++i) {
			String key = keys.getString(i);
			String value = jsonObject.getString(key);
			paramList.put(key, value);
		}
		return paramList;
	}

	public Result hitQuestionResponseService(Map<String, String> inputParams, DataControllerRequest dcRequest) {
		String Value = Executor.invokeService(LoansServiceURLEnum.QUESTION_RESPONSE_UPDATE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		return CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
	}

	public Result hitQuestionResponseDeleteService(Map<String, String> inputParams, DataControllerRequest dcRequest) {
		String Value = Executor.invokeService(LoansServiceURLEnum.QUESTION_RESPONSE_DELETE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		return CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
	}

	public Result hitQueryResponseService(Map<String, String> inputParams, DataControllerRequest dcRequest) {
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_RESPONSE_UPDATE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		return CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
	}

	private Result hitCustomerQuerySectionStatusService(Map<String, String> inputParams, DataControllerRequest dcRequest) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.CUSTOMER_QUERY_SECTION_STATUS_UPDATE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		return CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
	}

}