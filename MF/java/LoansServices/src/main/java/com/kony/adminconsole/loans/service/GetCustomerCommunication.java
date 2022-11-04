package com.kony.adminconsole.loans.service;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class GetCustomerCommunication implements JavaService2 {

	/*
	 * @author
	 * KH2267
	 * @see com.konylabs.middleware.common.JavaService2#invoke(java.lang.String, java.lang.Object[], com.konylabs.middleware.controller.DataControllerRequest, com.konylabs.middleware.controller.DataControllerResponse)
	 */
	@Override
	public Object invoke(String methodID, Object[] inputParams, DataControllerRequest dcRequest, DataControllerResponse dcResponse)
			throws Exception {
		Result result = new Result();
		Map < String, String > inputMap = (Map < String, String > ) inputParams[1];
		HashMap<String, String> customHeaders = new HashMap();
		customHeaders.put("X-Kony-Authorization", dcRequest.getHeader("X-Kony-Authorization"));
		if(preProcess(inputMap, dcRequest)) {
			try
			{
				String EmailValue = inputMap.get("EMail");
				inputMap.remove("EMail");
				inputMap.put("$filter","Value eq "+EmailValue);
				String Value = HTTPOperations.hitPOSTServiceAndGetResponse(LoansServiceURLEnum.CUSTOMERCOMMUNICATION_GET.getServiceURL(dcRequest), (HashMap)inputMap, customHeaders, null);
				JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
				if(ValueResponseJSON.getJSONArray("customercommunication").length() == 0) {
					result = ErrorCodeEnum.ERR_33402.constructResultObject();
					return result;
				}
				result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
				ErrorCodeEnum.ERR_33400.updateResultObject(result);
			}
			catch(Exception exception) {
				result = ErrorCodeEnum.ERR_31000.constructResultObject();
			}
		}
		else {
			result = ErrorCodeEnum.ERR_33402.constructResultObject();
		}
		return result;
	}
	@SuppressWarnings({ "rawtypes" })
	private static boolean preProcess(Map inputParams,
			DataControllerRequest dcRequest) {
		boolean status = true;
		if(inputParams.containsKey("EMail") && inputParams.get("EMail")!=null){
			status = true; 
		}
		else {
			status = false;
		}
		return status;
	}
}
