package com.kony.adminconsole.loans.service;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.errorhandling.LoansException;
import com.kony.adminconsole.loans.utils.Executor;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class GetQueryDefinitions implements JavaService2 {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object invoke(String methodID, Object[] inputArray,
			DataControllerRequest dcRequest, DataControllerResponse dcResponse)
					throws Exception {
		Result result = new Result();
		HashMap<String, String> inputParams = (HashMap)inputArray[1];
		try {
			String Value = Executor.invokeService(LoansServiceURLEnum.LOANS_GET, (HashMap<String, String>) inputParams, null, dcRequest);
			JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
			result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		}
		catch (Exception e)
		{
			LoansException unknownException=new LoansException(ErrorCodeEnum.ERR_31000);
			result=unknownException.updateResultObject(result);
		}
		return result;
	}

}