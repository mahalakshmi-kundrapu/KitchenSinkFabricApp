package com.kony.adminconsole.loans.preprocessor;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class CoApplicantRequestOtpPreProcessor implements DataPreProcessor2{
    private static final Logger log = Logger.getLogger(CoApplicantRequestOtpPreProcessor.class);

	@Override
	public boolean execute(HashMap inputParams, DataControllerRequest dcRequest, DataControllerResponse dcResponse, Result result)
			throws Exception {
		
		String ServiceID = dcRequest.getParameter("current_serviceID");
		if(ServiceID.equalsIgnoreCase(LoansUtilitiesConstants.Generate_OTP_Service_Two)){
			if(inputParams.containsKey("Phone")){
				inputParams.put("MessageType",LoansUtilitiesConstants.Registration_SMS_Tempalte_Name);
				inputParams.put("Phone","+1"+inputParams.get("Phone"));
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
    
}
