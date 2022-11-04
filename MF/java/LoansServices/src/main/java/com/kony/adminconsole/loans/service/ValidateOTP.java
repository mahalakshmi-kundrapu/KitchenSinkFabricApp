package com.kony.adminconsole.loans.service;

import java.text.ParseException;
import java.util.Map;

import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.kony.adminconsole.loans.utils.LoansUtilities;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.exceptions.MetricsException;

public class ValidateOTP implements JavaService2 {
	/**
	 * Constants to hold the field names as Strings 
	 */
	public static final String CUSTOMER_ID = "id";
	public static final String CUSTOMER_OTP = "Otp";
	public static final String CUSTOMER_OTPGENARATEDTS = "OtpGenaratedts";

	@SuppressWarnings("rawtypes")
	@Override
	public Object invoke(String methodID, Object[] inputArray,
			DataControllerRequest dcRequest, DataControllerResponse dcResponse)
			throws Exception {
		Result result = new Result();
		Map inputParams = LoansUtilities.getInputParamMap(inputArray);
		if (preProcess(inputParams, dcRequest, result)) {
			String XKonyToken = dcRequest.getHeader("X-Kony-Authorization");
			result = LoansUtilities.hitPostOperationAndGetResultObject(inputParams,LoansServiceURLEnum.CUSTOMER_GET.getServiceURL(dcRequest),XKonyToken);
			result = postProcess(inputParams, dcRequest, result);
		}

		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean preProcess(Map inputParams,DataControllerRequest dcRequest, Result result) {
		boolean status = true;
		String customer_idValue=(String) inputParams.get(CUSTOMER_ID);
		StringBuilder sb = new StringBuilder();
		String input_OTPValue=(String) inputParams.get(CUSTOMER_OTP);
		if(input_OTPValue!=null && LoansUtilities.isNotBlank(input_OTPValue)) {
			if(LoansUtilities.isNotBlank(customer_idValue)) {
				sb.append(CUSTOMER_ID).append(LoansUtilitiesConstants.EQUAL).append(customer_idValue);
			}else{
				LoansUtilities.setValidationMsg("Customer id is empty", dcRequest, result);
				status = false;
			}
		}else {
			LoansUtilities.setValidationMsg("OTP is empty", dcRequest, result);
		}
		inputParams.put(LoansUtilitiesConstants.FILTER, sb.toString());
		return status;
	}
	
	@SuppressWarnings("rawtypes")
	private Result postProcess(Map inputParams, DataControllerRequest dcRequest, Result result)
			throws MetricsException, ParseException {
		Result retVal = new Result();
		if (LoansUtilities.hasRecords(result)) {
			Dataset dataset = result.getDatasetById(LoansUtilitiesConstants.DS_USER);
			Record record = dataset.getRecord(0);
			String db_OTPValue=record.getParamByName(CUSTOMER_OTP).getValue();
//			String db_OTPGenaratedtsValue=record.getParamByName(CUSTOMER_OTPGENARATEDTS).getValue();
			String input_OTPValue=(String) inputParams.get(CUSTOMER_OTP);
			
			if(input_OTPValue.equalsIgnoreCase(db_OTPValue)){
				Param p = new Param(LoansUtilitiesConstants.VALIDATION_ERROR, "OTP is matching", "String");
				Param opstatusParam = new Param("errcode", "0", "String");
				retVal.addParam(p);
				retVal.addParam(opstatusParam);
			}else{
				Param p = new Param(LoansUtilitiesConstants.VALIDATION_ERROR, "OTP is not matching", "String");
				Param opstatusParam = new Param("errcode", "9001", "String");
				retVal.addParam(p);
				retVal.addParam(opstatusParam);
			}
		} else {
			Param p = new Param(LoansUtilitiesConstants.VALIDATION_ERROR, "No records found for given customer id", "String");
			Param opstatusParam = new Param("errcode", "9001", "String");
			retVal.addParam(p);
			retVal.addParam(opstatusParam);
			
		}
		return retVal;
	}
}