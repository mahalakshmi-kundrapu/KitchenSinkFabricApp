package com.kony.adminconsole.loans.preprocessor;

import java.util.HashMap;
import java.util.Properties;
import org.apache.log4j.Logger;
import com.kony.adminconsole.commons.handler.EnvironmentConfigurationsHandler;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;

public class ForgotFlowValidationPreProcessor implements DataPreProcessor2{

	private static final Logger log = Logger
			.getLogger(ForgotFlowValidationPreProcessor.class);
	static Properties LoansConfigProperties= new Properties();
	@Override
	public boolean execute(HashMap inputParams, DataControllerRequest dcRequest, DataControllerResponse dcResponse, Result result)
			throws Exception {
		String demoFlag = EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.DEMO_FLAG_KEY,dcRequest);
		String ServiceID = dcRequest.getParameter("current_serviceID");
		String deviceId = dcRequest.getHeader("X-Kony-DeviceId");
		Session sessionObject = dcRequest.getSession();
		String demoOTPNumber = EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.DEMO_OTP_NUMBER,dcRequest);
		if(ServiceID.contains(LoansUtilitiesConstants.VerifydbxUserName_service)){
			String XKonyToken = dcRequest.getHeader("X-Kony-Authorization");
			sessionObject.setAttribute("KonyDeviceId",dcRequest.getHeader("X-Kony-DeviceId"));
			sessionObject.setAttribute("isSendOTP","false");
			sessionObject.setAttribute("IsValidOTP","false");
			sessionObject.setAttribute("isResetPassword","false");
			sessionObject.setAttribute("isdbxVerifyCustomer","false");
			if(inputParams.containsKey("UserName") ) {
				sessionObject.setAttribute("UserNameForForgotFlow",inputParams.get("UserName").toString());
			}
			else {
				result = ErrorCodeEnum.ERR_33403.constructResultObject();
				return false;
			}
			}
		else{
			if(deviceId.equals(sessionObject.getAttribute("KonyDeviceId"))){
				if(ServiceID.contains(LoansUtilitiesConstants.GetPhoneandSendSMS_service) && sessionObject.getAttribute("isdbxVerifyCustomer").equals("false")){
					log.debug("Not authorized for this request");
					result = ErrorCodeEnum.ERR_31001.constructResultObject();
					return false;
				}
				else if(ServiceID.contains(LoansUtilitiesConstants.GetPhoneandSendSMS_service) && sessionObject.getAttribute("isdbxVerifyCustomer").equals("true")) {
					inputParams.remove("UserName");
					inputParams.put("UserName",sessionObject.getAttribute("UserNameForForgotFlow"));
				}
			 if(ServiceID.contains(LoansUtilitiesConstants.Validate_dbx_OTP_service)&& inputParams.containsKey("securityKey")) {	
						if(inputParams.get("securityKey").toString().equals(sessionObject.getAttribute("securityKey"))) {
							if(demoFlag.equalsIgnoreCase("true") && inputParams.containsKey("Otp")) {
								sessionObject.setAttribute("IsValidOTP","true");
									return false;
							}
							else if(demoOTPNumber!=null && inputParams.containsKey("Otp") && demoFlag.equalsIgnoreCase("false") && demoOTPNumber.equals(inputParams.get("Otp")) && !demoOTPNumber.equals("000000")){
								sessionObject.setAttribute("IsValidOTP","true");
								return false;
							}
						}
						else {
							result = ErrorCodeEnum.ERR_35004.updateResultObject(result);
							return false;
						}
				}
				if(ServiceID.contains(LoansUtilitiesConstants.Reset_Password_service) && sessionObject.getAttribute("IsValidOTP").equals("true")){
					dcRequest.getHeaderMap().put(LoansUtilitiesConstants.DBP_REPORTING_PARAMS, EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.AC_DBP_AUTH_REPORTING_PARAMS, dcRequest));
					inputParams.remove("UserName");
					inputParams.put("UserName",sessionObject.getAttribute("UserNameForForgotFlow"));	
				}
				else if(ServiceID.contains(LoansUtilitiesConstants.Reset_Password_service) && sessionObject.getAttribute("IsValidOTP").equals("false")){
					result = ErrorCodeEnum.ERR_35005.constructResultObject();
					return false;
				}
			}
			else{
				log.debug("Device is not registered for this Service");
				ErrorCodeEnum.ERR_31001.updateResultObject(result);
				return false;
			}
		}
		return true;
	}
}
