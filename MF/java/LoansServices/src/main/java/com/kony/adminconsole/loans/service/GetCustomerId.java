package com.kony.adminconsole.loans.service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.kony.adminconsole.loans.utils.LoansUtilities;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.exceptions.MetricsException;
import com.konylabs.middleware.session.Session;

public class GetCustomerId implements JavaService2 {
	/**
	 * Constants to hold the field names as Strings...
	 */
	public static final String CUSTOMER_LAST_NAME = "LastName";
	public static final String CUSTOMER_SSN = "Ssn";
	public static final String CUSTOMER_DATE_OF_BIRTH = "DateOfBirth";

	@SuppressWarnings("rawtypes")
	@Override
	public Object invoke(String methodID, Object[] inputArray,
			DataControllerRequest dcRequest, DataControllerResponse dcResponse)
			throws Exception {
		Result result = new Result();
		Map inputParams = getInputParamMap(inputArray);

		if (preProcess(inputParams, dcRequest, result)) {
			String XKonyToken = dcRequest.getHeader("X-Kony-Authorization");
			result = LoansUtilities.hitPostOperationAndGetResultObject(inputParams,LoansServiceURLEnum.CUSTOMER_GET.getServiceURL(dcRequest),XKonyToken);
			Session sessionObject = dcRequest.getSession();
			if(result.getDatasetById("customer")!=null && result.getDatasetById("customer").getAllRecords().size()!=0){
				sessionObject.setAttribute("Customer.id",result.getDatasetById("customer").getRecord(0).getParam("id").getValue());
				sessionObject.setAttribute("Customer.username",result.getDatasetById("customer").getRecord(0).getParam("Username").getValue());
			}
			result = postProcess(inputParams, dcRequest, result);
		}

		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean preProcess(Map inputParams,
			DataControllerRequest dcRequest, Result result) {
		boolean status = true;
		String lastnameValue=(String) inputParams.get(CUSTOMER_LAST_NAME);
		String ssnValue=(String) inputParams.get(CUSTOMER_SSN);
		String dateOfBirthValue=(String) inputParams.get(CUSTOMER_DATE_OF_BIRTH);
		
		StringBuilder sb = new StringBuilder();
		if(LoansUtilities.isNotBlank(lastnameValue) && LoansUtilities.isNotBlank(ssnValue) && LoansUtilities.isNotBlank(dateOfBirthValue)) {
			sb.append(CUSTOMER_LAST_NAME).append(LoansUtilitiesConstants.EQUAL).append(lastnameValue)
				.append(LoansUtilitiesConstants.AND)
				.append(CUSTOMER_SSN).append(LoansUtilitiesConstants.EQUAL).append(ssnValue)
				.append(LoansUtilitiesConstants.AND)
				.append(CUSTOMER_DATE_OF_BIRTH).append(LoansUtilitiesConstants.EQUAL).append(dateOfBirthValue);
		}else{
			LoansUtilities.setValidationMsg("One or more Input params empty", dcRequest,
					result);
			status = false;
		}
		inputParams.put(LoansUtilitiesConstants.FILTER, sb.toString());
		return status;
	}
	
	@SuppressWarnings("rawtypes")
	private Result postProcess(Map inputParams, DataControllerRequest dcRequest, Result result)
			throws MetricsException, ParseException {
		Result retVal = new Result();
		if (LoansUtilities.hasRecords(result)) {
//			result.getAllRecords()
		} else {
			
		}
		retVal=result;
		return retVal;
	}
	public static Map getInputParamMap(Object[] inputArray) {
		return (HashMap)inputArray[1];
	}
}