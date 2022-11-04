package com.kony.adminconsole.loans.postprocessor;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;

public class CoApplicantRequestOTPPostProcessor implements DataPostProcessor2{

	
	private static final Logger log = Logger.getLogger(CoApplicantRequestOTPPostProcessor.class);
	
	@Override
	public Object execute(Result result, DataControllerRequest dcRequest, DataControllerResponse dcResponse) throws Exception{
		Session sessionObject = dcRequest.getSession();
		try{
			String ServiceID = dcRequest.getParameter("current_serviceID");
			if(ServiceID.equals(LoansUtilitiesConstants.Generate_OTP_Service_Two)){
				Param dbpErrCode = result.getParamByName("dbpErrCode");
				if(dbpErrCode == null && result.getParamByName("securityKey") != null) {
					
					sessionObject.setAttribute("isRequestOTPCalled","true");
					sessionObject.setAttribute("isVerifyOTPCalled","false");
					result.removeParamByName("Email");
					result.removeParamByName("Phone");
					result.removeParamByName("UserName");
					result.removeParamByName("MessageType");
					sessionObject.setAttribute("securityKey",result.getParamByName("securityKey").getValue());
				}
			}
			if(ServiceID.equalsIgnoreCase(LoansUtilitiesConstants.Verify_OTP_Service_Two)){
				sessionObject.setAttribute("isVerifyOTPCalled","true");
				sessionObject.setAttribute("isRequestOTPCalled","false");
			}
		}catch(Exception e){
			log.error(" Error Occured in RegistrationPostProcessor - execute Method: ", e);
			result = ErrorCodeEnum.ERR_31000.updateResultObject(result);
		}
		
		return result;
	}
	
	/*
	 * Content template is defined here
	 */
	private static JSONObject createContentJSONObjectForEmail() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("TemplateKey","SUCCESSFUL_REGISTRATION_EMAIL_TEMPLATE");
		jsonObject.put("APPLICANT_NAME","KonyDBXUser");
		jsonObject.put("BANK_CU_NAME","KonyDBXUser");
		return jsonObject;
	}
	
	private static JSONObject createContentJSONObjectSMS(String OTP) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("TemplateKey","OTP_USER_REGISTRATION_INVITE_SMS_TEMPLATE");
		jsonObject.put("OTP_NUMBER",OTP);
		jsonObject.put("CONTACT_US","(123)-456-7890");
		return jsonObject;
	}
	/*
	 * Set Attributes for Email/CreateKMS services 
	 * Preprocessors for both the services will access and set as input params
	 */
	private static void setAttributesToResultObjectForCreateKMS(Result result,DataControllerRequest dcRequest) {
		result.addParam(new Param("firstName","KonyDBX"));
		result.addParam(new Param("lastName","KonyDBX"));
		result.addParam(new Param("email",dcRequest.getParameter("Email").toString()));
		result.addParam(new Param("mobileNumber", "+1" + dcRequest.getParameter("Phone").toString()));
		result.addParam(new Param("country","United States"));
		result.addParam(new Param("state","New york"));
	}
	private static void setAttributesToResultObjectForEmail(Result result,DataControllerRequest dcRequest) {
		result.addParam(new Param("sendToEmails",dcRequest.getParameter("Email").toString()));
		result.addParam(new Param("senderEmail","logeswaran.rathinasamy@kony.com"));
		result.addParam(new Param("copyToEmails",""));
		result.addParam(new Param("bccToEmails",""));
		result.addParam(new Param("senderName","KonyDBX"));
		result.addParam(new Param("subject","Your registration is successful"));
		result.addParam(new Param("content",createContentJSONObjectForEmail().toString()));
	}
	private void setAttributesToResultObjectForSMS(Result result,DataControllerRequest dcRequest,String OTP) {
		result.addParam(new Param("sendToMobiles","+1" +(String) dcRequest.getParameter("Phone")));
		result.addParam(new Param("content",createContentJSONObjectSMS(OTP).toString()));
		result.addParam(new Param("NeedToSendSMS","true"));
	}
}
