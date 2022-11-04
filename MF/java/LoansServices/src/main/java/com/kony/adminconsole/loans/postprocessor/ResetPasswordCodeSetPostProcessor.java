package com.kony.adminconsole.loans.postprocessor;

import org.apache.log4j.Logger;

import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;

public class ResetPasswordCodeSetPostProcessor implements DataPostProcessor2{
	
    private static final Logger log = Logger.getLogger(ResetPasswordCodeSetPostProcessor.class);
	@Override
	public Object execute(Result result, DataControllerRequest arg1, DataControllerResponse arg2) throws Exception {
		try {
				Session sessionObject = arg1.getSession();
				sessionObject.setAttribute("isSendOTP","false");
				sessionObject.setAttribute("IsValidOTP","false");
				sessionObject.setAttribute("isResetPassword","false");
				sessionObject.setAttribute("isdbxVerifyCustomer","false");
			} catch(Exception e) {
			log.error("Error in OTPResultCodeSetPostProcessor : "+e.getMessage());
			result = ErrorCodeEnum.ERR_31001.updateResultObject(result);
		}
	return result;
 }
}