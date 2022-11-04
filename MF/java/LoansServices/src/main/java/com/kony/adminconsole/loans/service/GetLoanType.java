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
import com.konylabs.middleware.dataobject.Result;

/*
 * GetLoanType is the JavaService to return data for the 'LoanType' object. 
 * This internally calls the RDBMS integration service.
 * 
 */
public class GetLoanType implements JavaService2 {

	/*
	 * Constant to hold the operation name as a String
	 */
	private static final String OPERATION_NAME_LOANTYPE = "dbploansdev_loantype_get";
	
	@Override
	public Object invoke(String methodID, Object[] inputArray,
			DataControllerRequest dcRequest, DataControllerResponse dcResponse)
			throws Exception {
		Result result = new Result();

		Map<?, ?> inputParams = (HashMap)inputArray[1];
		if (preProcess(inputParams, dcRequest, result)) {
			inputParams=addOperationName(inputParams,OPERATION_NAME_LOANTYPE);
			try {
//				String serviceURL = LoansServiceURLEnum.LOANTYPE_GET.getServiceURL(dcRequest);
//				String Value = HTTPOperations.hitPOSTServiceAndGetResponse(serviceURL, (HashMap<String, String>) inputParams, null, null);
//				Inline invocation instead of calling Integration using HttpOperation(InlineServiceExecutor)
				String Value = Executor.invokeService(LoansServiceURLEnum.LOANTYPE_GET, (HashMap<String, String>) inputParams, null, dcRequest);
				JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
				result= CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
			} 
		    catch (Exception e) {
		    	e.printStackTrace();
			}
		}
		
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map addOperationName(Map inputParams, String operationName) {
		inputParams.put("operationName", operationName);
		return inputParams;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private boolean preProcess(Map inputParams,
			DataControllerRequest dcRequest, Result result) {
		boolean status = true;
		return status;
	}

}