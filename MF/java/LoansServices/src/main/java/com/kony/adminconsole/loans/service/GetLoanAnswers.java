package com.kony.adminconsole.loans.service;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.utils.Executor;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GetLoanAnswers
implements JavaService2
{
	
	private static final Logger LOGGER = Logger.getLogger(GetLoanAnswers.class);

	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse dcResponse)
			throws Exception
	{
		Result result = new Result();
		Map inputmap = (Map)inputArray[1];
		String queryResponseId = inputmap.get("QueryResponseID").toString();
		
		HashMap<String, String> customHeaders = new HashMap();
		customHeaders.put(LoansUtilitiesConstants.X_KONY_AUTHORIZATION, dcRequest.getHeader(LoansUtilitiesConstants.X_KONY_AUTHORIZATION));
		try
		{
			String value = HTTPOperations.hitPOSTServiceAndGetResponse(LoansServiceURLEnum.LOAN_ANSWERS_GET.getServiceURL(dcRequest), (HashMap)inputmap, customHeaders, null);
			JSONObject valueResponseJSON = CommonUtilities.getStringAsJSONObject(value);
			
			if (valueResponseJSON.has(LoansUtilitiesConstants.ERROR_CODE)) {
				if (valueResponseJSON.getString(LoansUtilitiesConstants.ERROR_CODE).equalsIgnoreCase(LoansUtilitiesConstants.OBJECT_READ_ERROR_CODE)) {
					valueResponseJSON.remove(LoansUtilitiesConstants.ERROR_CODE);
					valueResponseJSON.remove(LoansUtilitiesConstants.ERROR_MESSAGE);
					valueResponseJSON.put(LoansUtilitiesConstants.QUESTION_RESPONSE, new JSONArray());
				}
			}
			
			//QueryResponse get 
			JSONArray queryResponse = hitQueryResponseGetService(inputmap, dcRequest, queryResponseId);
			valueResponseJSON = copyQueryResponseObject(valueResponseJSON, (JSONObject) queryResponse.get(0));
						
			//CustomerQuerySectionStatus get
			JSONArray customerQSS = hitCustomerQuerySectionStatusGetService(inputmap, dcRequest, queryResponseId);
			String queryDefinitionId = valueResponseJSON.getString(LoansUtilitiesConstants.QUERY_DEFINITION_ID);
			if (queryDefinitionId != null && customerQSS != null) {
				customerQSS = modifyQuerySectionIds(customerQSS, inputmap, dcRequest, queryDefinitionId);
			}
			valueResponseJSON.put(LoansUtilitiesConstants.CUSTOMER_QUERY_QUESTION_STATUS, customerQSS);
			
			//QueryCoBorrower get
			JSONArray queryCoBorrower = hitQueryCoBorrowerGetService(inputmap, dcRequest, queryResponseId);
			valueResponseJSON.put(LoansUtilitiesConstants.QUERY_COBORROWER, queryCoBorrower);
			
			result = CommonUtilities.getResultObjectFromJSONObject(valueResponseJSON);
		}
		catch (Exception e)	{
			LOGGER.error("Error in GetLoanAsnwers " + e.getLocalizedMessage());
			e.printStackTrace();
			return ErrorCodeEnum.ERR_31000.constructResultObject();
			
		}
		return result;
	}

	public JSONObject copyQueryResponseObject(JSONObject valueResponseJSON, JSONObject queryResponse) {
		Iterator<String> keys = queryResponse.keys();
		while(keys.hasNext()) {
		    String key = keys.next();
		    valueResponseJSON.put(key, queryResponse.get(key));
		}
		return valueResponseJSON;
	}
	
	private JSONArray modifyQuerySectionIds(JSONArray customerQSS, Map<String, String> inputParams, DataControllerRequest dcRequest, String queryDefinition_id)
			throws Exception
	{
		if (customerQSS.length() == 0) {
			return customerQSS;
		}
		LoanApplicationQueryResponseCreate createQueryResponseNew = new LoanApplicationQueryResponseCreate();
		JSONArray querySectionArray = createQueryResponseNew.hitQuerySectionGetService(inputParams, dcRequest, queryDefinition_id);
		for (int i = 0; i < customerQSS.length(); i++)
		{
			JSONObject customerQSSObj = customerQSS.getJSONObject(i);
			customerQSSObj = modifyCQSSObject(customerQSSObj, querySectionArray);
			customerQSS.put(i, customerQSSObj);
		}
		return customerQSS;
	}

	private JSONObject modifyCQSSObject(JSONObject cqssObj, JSONArray querySectionArray)
	{
		String abstractName = getAbstractNameByIdQuerySection(cqssObj.getString("QuerySection_id"), querySectionArray);
		cqssObj.put("QuerySection_FriendlyName", abstractName);
		return cqssObj;
	}

	private String getAbstractNameByIdQuerySection(String id, JSONArray querySectionArray)
	{
		for (int i = 0; i < querySectionArray.length(); i++)
		{
			JSONObject querySection = querySectionArray.getJSONObject(i);
			if (querySection.getString("id").equalsIgnoreCase(id)) {
				return querySection.getString("abstractname");
			}
		}
		return "";
	}

	private JSONArray hitCustomerQuerySectionStatusGetService(Map<String, String> inputParams, DataControllerRequest dcRequest, String queryResponseId)	throws Exception {
		inputParams.put("$filter", "QueryResponse_id eq " + queryResponseId);
		String Value = Executor.invokeService(LoansServiceURLEnum.CUSTOMER_QUERY_SECTION_STATUS_GET, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject valueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		JSONArray responseArray = valueResponseJSON.getJSONArray("customerquerysectionstatus");
		if (responseArray.length() == 0) 
			return null;
		return responseArray;
	}
	private JSONArray hitQueryCoBorrowerGetService(Map<String, String> inputParams, DataControllerRequest dcRequest, String queryResponseId) throws Exception {
		inputParams.put("$filter", "QueryResponse_id eq " + queryResponseId);
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_COBORROWER_GET, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject valueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		JSONArray responseArray = valueResponseJSON.getJSONArray("querycoborrower");
		if (responseArray.length() == 0) 
			return null;
		return responseArray;
	}
	public JSONArray hitQueryResponseGetService(Map<String, String> inputParams, DataControllerRequest dcRequest, String queryResponseId) throws Exception {
		inputParams.put("$filter", "id eq " + queryResponseId);
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_RESPONSE_GET_, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject valueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		JSONArray responseArray = valueResponseJSON.getJSONArray("queryresponse");
		if (responseArray.length() == 0) 
			return null;
		return responseArray;
	}
}
