package com.kony.adminconsole.loans.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.errorhandling.LoansException;
import com.kony.adminconsole.loans.service.messaging.SendEmail;
import com.kony.adminconsole.loans.utils.Executor;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class GetQuestionResponse implements JavaService2 {

	private static final Logger log = Logger.getLogger(GetQuestionResponse.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest,
			DataControllerResponse dcResponse) throws LoansException, Exception {
		Result result = new Result();
		try {
			HashMap<String, String> inputParams = (HashMap) inputArray[1];
			String Value = Executor.invokeService(LoansServiceURLEnum.QUESTION_RESPONSE_GET, inputParams, null, dcRequest);
			JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
			result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		} catch (Exception e) {
			LoansException unknownException=new LoansException(ErrorCodeEnum.ERR_31000);
			result=unknownException.updateResultObject(result);
			/*
			 * Failure Sample Response response : { "errorCode" : "1000", "errorMessage" :
			 * "An unknown error occurred while processing the request" }
			 */
		}

		return result;
	}

}