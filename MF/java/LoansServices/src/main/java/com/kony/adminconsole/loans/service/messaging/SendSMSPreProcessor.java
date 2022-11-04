package com.kony.adminconsole.loans.service.messaging;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.kony.adminconsole.loans.postprocessor.GetCustomerIdPostProcessor;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;

public class SendSMSPreProcessor implements DataPreProcessor2{

	private static final Logger LOGGER = Logger.getLogger(SendSMSPreProcessor.class);
	
	@Override
	public boolean execute(HashMap hashMap, DataControllerRequest request,
			DataControllerResponse response, Result result) throws Exception {
		try{
			boolean shouldSendSMS=Boolean.valueOf((String)request.getAttribute("DBX_shouldSendSMS"));
			return shouldSendSMS;
		}
		catch(Exception e){
			LOGGER.error("Exception in SendSMSPreProcessor : "+e.getMessage());
			Param exceptionToBeShownToUser = new Param();
			exceptionToBeShownToUser.setValue(e.getMessage());
			result.addParam(exceptionToBeShownToUser);
			return false;
		}
	}
}
