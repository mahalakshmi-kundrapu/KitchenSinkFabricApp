package com.kony.adminconsole.loans.postprocessor;

import org.apache.log4j.Logger;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class GetPhoneNumberAndSendSMSPostProcessor implements DataPostProcessor2{
	private static final Logger log = Logger.getLogger(GetPhoneNumberAndSendSMSPostProcessor.class);
	@Override
	public Object execute(Result result, DataControllerRequest dcRequest, DataControllerResponse dcResponse) throws Exception {
		try {
				result.addParam(new Param("NeedToSendSMS","true"));
			    setAttributesToResultObjectForSMS(result, dcRequest);
		}catch(Exception e) {
			result.addParam(new Param("NeedToSendSMS","false"));
			log.error("Exception in GetPhoneNumberAndSendSMSPostProcessor", e);
		}
		return result;
	}
	
	private void setAttributesToResultObjectForSMS(Result result,DataControllerRequest dcRequest) {
		Param phoneNumber = result.getParamByName("Phone");
		if(phoneNumber != null) {
		result.addParam(new Param("Phone","+1" +(phoneNumber.getValue())));
		result.addParam(new Param("MessageType",LoansUtilitiesConstants.Registration_SMS_Tempalte_Name));
		}
		else {
			result.addParam(new Param("NeedToSendSMS","false"));
			log.error("Exception in GetPhoneNumberAndSendSMSPostProcessor - unable to fetch phone number");
		}
	}

}
