package com.kony.adminconsole.loans.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import com.kony.adminconsole.commons.dto.JSONResponseHandlerBean;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class LoansUtilities {

	public LoansUtilities() {

	}

	public static Map getInputParamMap(Object[] inputArray) {
		return (HashMap) inputArray[1];
	}

	public static boolean isNotBlank(String s) {
		return !(null == s || s.isEmpty() || s.trim().isEmpty());
	}

	public static void setValidationMsg(String message, DataControllerRequest dcRequest, Result result) {
		Param validionMsg = new Param(LoansUtilitiesConstants.VALIDATION_ERROR, message,
				LoansUtilitiesConstants.STRING_TYPE);
		result.addParam(validionMsg);
		Param status = new Param(LoansUtilitiesConstants.HTTP_STATUS_CODE, LoansUtilitiesConstants.HTTP_ERROR_CODE,
				LoansUtilitiesConstants.STRING_TYPE);
		result.addParam(status);
		Param opstatus = new Param(LoansUtilitiesConstants.OP_STATUS, LoansUtilitiesConstants.HTTP_ERROR_CODE,
				LoansUtilitiesConstants.STRING_TYPE);
		result.addParam(opstatus);
	}

	public static boolean hasRecords(Result result) {
		if (hasError(result)) {
			return false;
		}
		Dataset ds = result.getAllDatasets().get(0);
		return null != ds && null != ds.getAllRecords() && ds.getAllRecords().size() > 0;
	}

	public static boolean hasError(Result result) {
		boolean status = false;
		if (null == result || null != result.getParamByName(LoansUtilitiesConstants.VALIDATION_ERROR)) {
			status = true;
		}
		return status;
	}

	public static Result hitPostOperationAndGetResultObject(Map input, String url, String X_Kony_Authorization) {
		return hitPostOperationAndGetResultObject(input, null, url, X_Kony_Authorization);
	}

	public static Result hitPostOperationAndGetResultObject(Map inputParams, Map headerParams, String url,
			String X_Kony_Authorization) {
		JSONResponseHandlerBean response = HTTPOperations.hitPOSTServiceAndGetJSONResponse(url, (HashMap) inputParams,
				X_Kony_Authorization, (HashMap) headerParams);
		Result result = CommonUtilities.getResultObjectFromJSONObject(response.getResponseAsJSONObject());
		return (null == result) ? new Result() : result;
	}

	public static Map convertJSONtoMap(JSONObject customerdlpdata) {
		Map<String, String> input = new HashMap<String, String>();
		Iterator<?> itr = customerdlpdata.keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			input.put(key, customerdlpdata.getString(key));
		}
		return input;
	}
}
