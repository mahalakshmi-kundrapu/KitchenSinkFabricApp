package com.kony.adminconsole.loans.postprocessor;

import org.apache.log4j.Logger;

import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.konylabs.middleware.common.DataPostProcessor;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;

/*
 * @author KH2267 Dinesh Kumar
 */
public class ServiceValidationFlagSetPostProcessor implements DataPostProcessor {
	/*
	 * To log debug, info and error statements  
	 */
	private static final Logger log = Logger.getLogger(ServiceValidationFlagSetPostProcessor.class);
	
	/**
	 * Constants to hold the field names as Strings 
	 */
	

	@Override
	public Object execute(Result results, DataControllerRequest dataControllerRequest) throws Exception {
		Session sessionObject = dataControllerRequest.getSession();
		Result returnResult = new Result();
		try{
			String ServiceID = dataControllerRequest.getParameter("current_serviceID");
			Param Opstatus = results.getParamByName("errcode");
			String OpstatusValue = Opstatus.getValue();
			if(OpstatusValue.equals("0")){
				if(ServiceID.equals("GenerateOTP")){
					sessionObject.setAttribute("IsGenerateOTPCalled","true");
					sessionObject.setAttribute("isValidateOTPCalled","false");
				}
				else if(ServiceID.equals("ValidateOTP")){
					sessionObject.setAttribute("isValidateOTPCalled","true");
					sessionObject.setAttribute("IsGenerateOTPCalled","false");
				}
			}
			returnResult.addParam(getParamObject("errcode",OpstatusValue));
			returnResult.addParam(getParamObject("errmsg",results.getParamByName("errmsg").getValue()));
		} catch (Exception e) {
			log.error(" Error Occured in GetCustomerIdPostProcessor - execute Method: ", e);
			returnResult.addParam(getParamObject("errmsg", "Unknown error occured."+e.getMessage()));
			returnResult.addParam(getParamObject("Opstatus","9001"));
		}	
		
		return results;
	}
	private Param getParamObject(String Key, String Value) {
		Param param= new Param();
		param.setName(Key);
		param.setValue(Value);
		return param;
	}
}