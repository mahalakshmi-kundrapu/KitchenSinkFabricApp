package com.kony.adminconsole.loans.preprocessor;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.kony.adminconsole.commons.handler.EnvironmentConfigurationsHandler;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class OTPValidationCheckPreprocessor implements DataPreProcessor2{

	/*
	 * @author KH2267 Dinesh Kumar
	 * @see com.konylabs.middleware.common.DataPostProcessor2#execute(com.konylabs.middleware.dataobject.Result, com.konylabs.middleware.controller.DataControllerRequest, com.konylabs.middleware.controller.DataControllerResponse)
	 * Preprocessor to validate OTP either from service or here only depending on demo flag
	 */
	private static final Logger log = Logger.getLogger(OTPValidationCheckPreprocessor.class);
	
	@Override
	public boolean execute(HashMap inputParams, DataControllerRequest dcRequest, DataControllerResponse dcResponse, Result result)
			throws Exception {
		try {
			String demoflag = EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.DEMO_FLAG_KEY,dcRequest);
			if(inputParams.containsKey(LoansUtilitiesConstants.OTP_Param_Name)) {
				if(demoflag!=null && demoflag.equalsIgnoreCase("true")){
					if(inputParams.get(LoansUtilitiesConstants.OTP_Param_Name).equals("123456")) {
						result = ErrorCodeEnum.ERR_35003.updateResultObject(result);
						return false;
					}
					else {
						result = ErrorCodeEnum.ERR_35004.updateResultObject(result);
						return false;
					}
				}
			}
			else {
				result = ErrorCodeEnum.ERR_33202.updateResultObject(result);
				return false;
			}
		}
		catch(Exception e) {
			log.error("Error in OTPValidationCheckPreprocessor"+e.getMessage());
			result = ErrorCodeEnum.ERR_31001.updateResultObject(result);
			return false;
		}
		return true;
	}
	
}
