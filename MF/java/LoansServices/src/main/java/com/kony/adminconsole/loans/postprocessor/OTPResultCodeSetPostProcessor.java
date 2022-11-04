package com.kony.adminconsole.loans.postprocessor;

import org.apache.log4j.Logger;

import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.preprocessor.OTPValidationCheckPreprocessor;
import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;

public class OTPResultCodeSetPostProcessor implements DataPostProcessor2{
	
	private static final Logger log = Logger.getLogger(OTPResultCodeSetPostProcessor.class);
	
	@Override
	public Object execute(Result result, DataControllerRequest dcRequest, DataControllerResponse dcResponse) throws Exception {
		try {
				dcRequest.getSession().setAttribute("IsValidOTP","true");
		} catch(Exception e) {
			log.error("Error in OTPResultCodeSetPostProcessor : "+e.getMessage());
			result = ErrorCodeEnum.ERR_31000.updateResultObject(result);
		}
		return result;
	}
}
