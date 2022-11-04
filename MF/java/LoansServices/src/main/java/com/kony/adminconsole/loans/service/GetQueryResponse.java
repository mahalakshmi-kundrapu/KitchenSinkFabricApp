package com.kony.adminconsole.loans.service;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.loans.utils.Executor;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;

public class GetQueryResponse implements JavaService2  {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object invoke(String methodID, Object[] inputArray,
			DataControllerRequest dcRequest, DataControllerResponse dcResponse)
			throws Exception {
		HashMap<String, String> inputParams = (HashMap)inputArray[1];
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_RESPONSE_GET, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		return CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON); 			
	}
	
}