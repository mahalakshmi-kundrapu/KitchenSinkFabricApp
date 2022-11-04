package com.kony.adminconsole.loans.postprocessor;

import java.util.List;

import org.apache.log4j.Logger;

import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.convertions.ResultToJSON;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
	
public class CustomerRegistrationOrchPostProcessor implements DataPostProcessor2{
	
	
	/**
	 * (non-Javadoc)
	 * 
	 * @author KH2267 DINESH KUMAR
	 * 
	 * Post processor to centralize the response and send single response to client
	 * 
	 * @see com.konylabs.middleware.common.DataPostProcessor2#execute(com.konylabs.middleware.dataobject.Result, com.konylabs.middleware.controller.DataControllerRequest, com.konylabs.middleware.controller.DataControllerResponse)
	 */
	
	private static final Logger log = Logger
	.getLogger(CustomerRegistrationOrchPostProcessor.class);
	
	@Override
	public Object execute(Result result, DataControllerRequest dcRequest, DataControllerResponse dcResponse) throws Exception {
		Result resultToSendToClient = new Result();
		try {
			Param param = result.getParamByName("isCreateProspectSuccessful");
			log.error("#### Result Params : "+result.getAllParams().toString());
			if(param==null) {
				if(result.getParamByName("isVerifyOTPSuccess")!=null && result.getParamByName("isVerifyOTPSuccess").getValue()!=null && result.getParamByName("isVerifyOTPSuccess").getValue().contains("true")) {
					resultToSendToClient = ErrorCodeEnum.ERR_35005.constructResultObject();
				}
				else {
					resultToSendToClient = ErrorCodeEnum.ERR_35004.constructResultObject();
				}
				return resultToSendToClient;
			}
			if(result.getParamByName("Email_message")!=null && result.getParamByName("Email_message").getValue()!=null) {
				resultToSendToClient.addParam(new Param("Email_message",result.getParamByName("Email_message").getValue()));
			}
			else {
				resultToSendToClient.addParam(new Param("Email_message","Null response received from Email Service"));
			}
		}
		catch(Exception e) {
			log.error("Unknown Error occured while proceeding in Post processor");
			log.error(e.getMessage());
			resultToSendToClient = ErrorCodeEnum.ERR_31001.updateResultObject(resultToSendToClient);
		}
		return resultToSendToClient;
	}
	

}
