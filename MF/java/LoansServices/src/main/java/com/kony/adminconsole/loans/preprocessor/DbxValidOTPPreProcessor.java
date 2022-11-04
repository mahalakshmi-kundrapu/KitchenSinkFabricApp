package com.kony.adminconsole.loans.preprocessor;
 
import java.util.HashMap;
 
import org.apache.log4j.Logger;

import com.kony.adminconsole.commons.handler.EnvironmentConfigurationsHandler;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;
 
public class DbxValidOTPPreProcessor implements DataPreProcessor2 {
    private static final Logger log = Logger.getLogger(DbxValidOTPPreProcessor.class);
 
    @SuppressWarnings("unchecked")
    @Override
    public boolean execute(HashMap inputPayload, DataControllerRequest dcRequest, DataControllerResponse dcResponse,
            Result result) {
        // TODO Auto-generated method stub
    	try {
			String demoFlag = EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.DEMO_FLAG_KEY,dcRequest);
			Session sessionObject = dcRequest.getSession();
			String requestValues = (String) sessionObject.getAttribute("isRequestOTPCalled");
			String demoOTPNumber = EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.DEMO_OTP_NUMBER,dcRequest);
			if(requestValues != null && requestValues.equals("false")){
				result = ErrorCodeEnum.ERR_31001.updateResultObject(result);
				return false;				
			}else{
				if((sessionObject.getAttribute("securityKey")) != null) {
					if(demoFlag.equalsIgnoreCase("true") && inputPayload.containsKey("Otp")) {
						sessionObject.setAttribute("IsValidOTP","true");
						return false;
					}
					else if(demoOTPNumber!=null && inputPayload.containsKey("Otp") && demoFlag.equalsIgnoreCase("false") && demoOTPNumber.equals(inputPayload.get("Otp")) && !demoOTPNumber.equals("000000")){
						sessionObject.setAttribute("IsValidOTP","true");
						return false;
					}
				}else {
					result = ErrorCodeEnum.ERR_31000.updateResultObject(result);
					return false;
				}
			}
		}
		catch(Exception e) {
			log.error("Error in OTPValidationCheckPreprocessor"+e.getMessage());
			result = ErrorCodeEnum.ERR_31000.updateResultObject(result);
			return false;
		}
        try {
            log.info("Entered DbxValidOTPPreProcessor");
            String sKey = (String) dcRequest.getSession().getAttribute("securityKey");
            inputPayload.put("securityKey", sKey);
            return true;
        } catch (Exception e) {
            log.error("Exeption in DbxValidOTPPreProcessor", e);
            result = ErrorCodeEnum.ERR_31001.updateResultObject(result);
        }
        return false;
    }
 
}