package com.kony.adminconsole.loans.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.errorhandling.LoansException;
import com.kony.adminconsole.loans.utils.Executor;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.kony.adminconsole.loans.utils.LoansUtilities;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

@SuppressWarnings({
	"unchecked",
	"rawtypes"
})
public class SimulationQueryResponseCreate implements JavaService2 {

	String Customer_id = "";
	private static final Logger LOGGER = Logger.getLogger(SimulationQueryResponseCreate.class);


	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest,
			DataControllerResponse dcResponse) throws Exception {
		Result result = new Result();
		try {
			Dataset QuestionResponseDataset = new Dataset();
			Dataset CustomerQuerySectionStatusDataset = new Dataset();
			Dataset QueryCoborrowerDataset = new Dataset();

			QuestionResponseDataset.setId("QuestionResponse");
			CustomerQuerySectionStatusDataset.setId("CustomerQuerySectionStatus");
			QueryCoborrowerDataset.setId("QueryCoBorrower");

			ArrayList < Record > records = new ArrayList < Record > ();
			Map inputmap = (Map) inputArray[1];
			JSONArray CustomerQuerySectionStatusJSON = null;
			JSONArray QueryCoborrowerJSON = null;
			String QueryResponse_id = "";
			boolean isCustomerQuerySectionStatus = false;
			boolean shouldCreateQueryResponse = true;
			boolean isQueryCoborrower = false;

			if (inputmap.containsKey("id") && inputmap.get("id") != null && !inputmap.get("id").equals("")) {
				shouldCreateQueryResponse = false;
				QueryResponse_id = (String) inputmap.get("id");
			} else {
				QueryResponse_id = "KL" + CommonUtilities.getNewId().toString();
			}
			if (inputmap.containsKey("CustomerQuerySectionStatus") && inputmap.get("CustomerQuerySectionStatus") != null &&
					!inputmap.get("CustomerQuerySectionStatus").equals("")) {
				isCustomerQuerySectionStatus = true;
				CustomerQuerySectionStatusJSON = new JSONArray(inputmap.get("CustomerQuerySectionStatus").toString());
				inputmap.remove("CustomerQuerySectionStatus");
			}
			if (inputmap.containsKey("QueryCoBorrower") && inputmap.get("QueryCoBorrower") != null &&
					!inputmap.get("QueryCoBorrower").equals("")) {
				isQueryCoborrower = true;
				QueryCoborrowerJSON = new JSONArray(inputmap.get("QueryCoBorrower").toString());
				inputmap.remove("QueryCoBorrower");
			}

			// Removing QuestionReponse
			JSONArray questionResponseJSON = new JSONArray(inputmap.get("QuestionResponse").toString());
			inputmap.remove("QuestionResponse");
			// For QueryReponse
			if (shouldCreateQueryResponse == true) {
				inputmap.put("id", QueryResponse_id);
				Result result1 = hitQueryResponseService(inputmap, dcRequest);
				result.addAllParams(result1.getAllDatasets().get(0).getRecord(0).getAllParams());
			}
			// For QuestionReponse
			for (int i = 0; i < questionResponseJSON.length(); i++) {
				JSONObject record = questionResponseJSON.getJSONObject(i);
				Map paramList = LoansUtilities.convertJSONtoMap(record);
				paramList.put("QueryResponse_id", QueryResponse_id);
				paramList.put("id", "KL" + CommonUtilities.getNewId().toString());
				Result childResult = hitQuestionResponseService(paramList, dcRequest);
				Record recordTemp = childResult.getAllDatasets().get(0).getRecord(0);
				records.add(recordTemp);
			}
			QuestionResponseDataset.addAllRecords(records);
			records.removeAll(records);
			if (isCustomerQuerySectionStatus == true) {
				for (int i = 0; i < CustomerQuerySectionStatusJSON.length(); i++) {
					JSONObject record = CustomerQuerySectionStatusJSON.getJSONObject(i);
					Map paramList = LoansUtilities.convertJSONtoMap(record);
					paramList.put("QueryResponse_id", QueryResponse_id);
					paramList.put("id", "KL" + CommonUtilities.getNewId().toString());
					Result childResult = hitCustomerQuerySectionStatusService(paramList, dcRequest);
					Record recordTemp = childResult.getAllDatasets().get(0).getRecord(0);
					records.add(recordTemp);
				}
				CustomerQuerySectionStatusDataset.addAllRecords(records);
				result.addDataset(CustomerQuerySectionStatusDataset);
			}

			records.removeAll(records);
			if (isQueryCoborrower == true && QueryCoborrowerJSON.length() != 0) {
				JSONObject customerPayload = constructCustomerPayload(QueryCoborrowerJSON.getJSONObject(0));
				if (!customerPayload.getString("Username").equalsIgnoreCase("")) {
					//Create customer record 
					createCustomer(customerPayload, dcRequest);
					for (int i = 0; i < QueryCoborrowerJSON.length(); i++) {
						JSONObject record = QueryCoborrowerJSON.getJSONObject(i);
						Map paramList = LoansUtilities.convertJSONtoMap(record);
						paramList.put("Customer_id", Customer_id);
						paramList.put("QueryResponse_id", QueryResponse_id);
						paramList.put("id", "KL" + CommonUtilities.getNewId().toString());
						Result childResult = hitQueryCoborrowerService(paramList, dcRequest);
						Record recordTemp = childResult.getAllDatasets().get(0).getRecord(0);
						records.add(recordTemp);
					}
					QueryCoborrowerDataset.addAllRecords(records);
					result.addDataset(QueryCoborrowerDataset);
				}
			}
			result.addDataset(QuestionResponseDataset);
			result.addParam(new Param("id", QueryResponse_id, "String"));
			return result;
		} catch (Exception e) {
			LOGGER.error("Error Occured in UpdateQueryResponseNew class", e);
			LoansException unknownException=new LoansException(ErrorCodeEnum.ERR_31000);
			result=unknownException.updateResultObject(result);
			return result;
		}
	}

	private JSONObject constructCustomerPayload(JSONObject inputParams) {
		JSONObject newParams = inputParams;
		newParams.put("Is_MemberEligible", "1");
		newParams.put("CustomerType_id", "Lead");
		newParams.put("Password", "Kony@123");
		if (inputParams.has("Email")) {
			newParams.put("Username", inputParams.get("Email").toString());
		} else {
			newParams.put("Username", "");
		}
		return newParams;
	}

	private Result createCustomer(JSONObject inputParams, DataControllerRequest dcRequest) throws Exception {
		Result result = null;
		HashMap customHeaders = new HashMap();
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(LoansServiceURLEnum.LOANS_CUSTOMER_CREATE.getServiceURL(dcRequest), inputParams, null, customHeaders);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		if (ValueResponseJSON.has("Customer")) {
			JSONArray customerArray = (JSONArray) ValueResponseJSON.get("Customer");
			JSONObject customerObj = (JSONObject) customerArray.get(0);
			Customer_id = (String) customerObj.get("id");
		}
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		return result;
	}

	private Result hitQueryCoborrowerService(Map inputParams, DataControllerRequest dcRequest) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_COBORROWER_CREATE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		return CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
	}

	public Result hitCustomerQuerySectionStatusService(Map inputParams, DataControllerRequest dcRequest) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.CUSTOMER_QUERY_SECTION_STATUS_CREATE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		return CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
	}

	public Result hitQuestionResponseService(Map inputParams, DataControllerRequest dcRequest) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.QUESTION_RESPONSE_CREATE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		return CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
	}

	public Result hitQueryResponseService(Map inputParams, DataControllerRequest dcRequest) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_RESPONSE_CREATE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		return CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
	}
}