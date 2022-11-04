package com.kony.adminconsole.loans.service;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.errorhandling.LoansException;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class GetLoanProduct implements JavaService2 {
	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest,
			DataControllerResponse dcResponse) throws Exception {
		Result result = new Result();
		Map<String, String> inputParams = (HashMap) inputArray[1];
		if (dcRequest.getAttribute("queryparams") != null)
			inputParams.putAll((Map<String, String>) dcRequest.getAttribute("queryparams"));
		HashMap<String, String> customHeaders = new HashMap();
		customHeaders.put("X-Kony-Authorization", dcRequest.getHeader("X-Kony-Authorization"));
		if (preProcess(inputParams, dcRequest, result)) {
			try {
				String Value = HTTPOperations.hitPOSTServiceAndGetResponse(
						LoansServiceURLEnum.LOANPRODUCT_GET.getServiceURL(dcRequest),
						(HashMap<String, String>) inputParams, customHeaders, null);
				JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
				result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
			} catch (Exception e) {
				LoansException unknownException = new LoansException(ErrorCodeEnum.ERR_31000);
				result = unknownException.updateResultObject(result);
			}
		}
		return result;
	}

	public static Map addOperationName(Map inputParams, String operationName) {
		// TODO Auto-generated method stub
		inputParams.put("operationName", operationName);
		return inputParams;
	}

	@SuppressWarnings({ "rawtypes" })
	private boolean preProcess(Map inputParams, DataControllerRequest dcRequest, Result result) {
		boolean status = true;
		return status;
	}
}
