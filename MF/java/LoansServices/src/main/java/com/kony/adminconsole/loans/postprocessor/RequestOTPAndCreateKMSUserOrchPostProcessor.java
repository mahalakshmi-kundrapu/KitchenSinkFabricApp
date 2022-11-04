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
	
public class RequestOTPAndCreateKMSUserOrchPostProcessor implements DataPostProcessor2{
	
	
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
	.getLogger(RequestOTPAndCreateKMSUserOrchPostProcessor.class);
	
	@Override
	public Object execute(Result result, DataControllerRequest dcRequest, DataControllerResponse dcResponse) throws Exception {
		Result resultToSendToClient = new Result();
		try {
			Param param = result.getParamByName("securityKey");
			log.error("#### Result Params : "+result.getAllParams().toString());
			if(param==null) {
				if(result.getParamByName("errorCode")!=null && result.getParamByName("errorCode").getValue()!=null && result.getParamByName("errorCode").getValue().contains("4002")) {
					resultToSendToClient = ErrorCodeEnum.ERR_35002.constructResultObject();
				}			
				return resultToSendToClient;
			}
			if(param.getValue()!=null) {
				resultToSendToClient = ErrorCodeEnum.ERR_35001.constructResultObject();
				resultToSendToClient.addParam(param);
			}
			if(result.getParamByName("message")!=null && result.getParamByName("message").getValue()!=null) {
				if(result.getParamByName("message")!=null) {
					resultToSendToClient.addParam(new Param("KMS_message",result.getParamByName("message").getValue()));
				}
				else {
					resultToSendToClient.addParam(new Param("KMS_message","Null response received from KMS Service"));
				}
				if(result.getParamByName("SMS_Message")!=null && result.getParamByName("SMS_Message").getValue()!=null && result.getParamByName("SMS_Message").getValue().equalsIgnoreCase("queued")) {
					resultToSendToClient.addParam(new Param("SMS_Message",result.getParamByName("SMS_Message").getValue()));
				}
				else {
					resultToSendToClient.addParam(new Param("SMS_Message","Null response received from Email Service. "+result.getParamByName("SMS_Message")!=null?result.getParamByName("SMS_Message").getValue():""));
					resultToSendToClient.removeParamByName(ErrorCodeEnum.KEY_DBPERRCODE);
					resultToSendToClient = ErrorCodeEnum.ERR_35002.updateResultObject(resultToSendToClient);
				}
			}
		}
		catch(Exception e) {
			log.error("Unknown Error occured while proceeding in Post processor OTP generation : ");
			log.error(e.getMessage());
			resultToSendToClient = ErrorCodeEnum.ERR_31001.updateResultObject(resultToSendToClient,e.getMessage());
		}
		return resultToSendToClient;
	}
	

}
