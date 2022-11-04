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
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;

/*
 * @author KH2267 Dinesh Kumar
 */
public class CustomerRegistrationValidationPreprocessor implements DataPreProcessor2{

	private static final Logger log = Logger
			.getLogger(CustomerRegistrationValidationPreprocessor.class);
	static Properties LoansConfigProperties= new Properties();
	@Override
	public boolean execute(HashMap inputParams, DataControllerRequest dcRequest, DataControllerResponse dcResponse, Result result)
			throws Exception {
		String demoFlag = EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.DEMO_FLAG_KEY,dcRequest);
		String demoOTPNumber = EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.DEMO_OTP_NUMBER,dcRequest);
		
		String ServiceID = dcRequest.getParameter("current_serviceID");
		String deviceId = dcRequest.getHeader("X-Kony-DeviceId");
		Session sessionObject = dcRequest.getSession();
		if(ServiceID.contains(LoansUtilitiesConstants.VerifyUserNameService)){
			String XKonyToken = dcRequest.getHeader("X-Kony-Authorization");
			sessionObject.setAttribute("KonyDeviceId",dcRequest.getHeader("X-Kony-DeviceId"));
			sessionObject.setAttribute("isRequestOTPCalled","false");
			sessionObject.setAttribute("isGetCustomerCommunicationCalled","false");
			sessionObject.setAttribute("isVerifyOTPCalled","false");
			if(inputParams.containsKey("Email") && inputParams.containsKey("Password")) {
				sessionObject.setAttribute("EmailToStore",inputParams.get("Email").toString());
				sessionObject.setAttribute("PasswordToStore",inputParams.get("Password").toString());
			}
			else {
				result = ErrorCodeEnum.ERR_33403.constructResultObject();
				return false;
			}
			}
		else{
			if(deviceId.equals(sessionObject.getAttribute("KonyDeviceId"))){
				if(ServiceID.contains(LoansUtilitiesConstants.Generate_OTP_Service) && sessionObject.getAttribute("isGetCustomerCommunicationCalled").equals("false")){
					log.debug("Not authorized for this request");
					result = ErrorCodeEnum.ERR_31001.constructResultObject();
					return false;
				}
				else if(ServiceID.contains(LoansUtilitiesConstants.Generate_OTP_Service) && sessionObject.getAttribute("isGetCustomerCommunicationCalled").equals("true") && inputParams.containsKey("Phone")) {
					sessionObject.setAttribute("Phone",inputParams.get("Phone"));
					inputParams.put("MessageType",LoansUtilitiesConstants.Registration_SMS_Tempalte_Name);
					inputParams.put("Phone","+1"+inputParams.get("Phone"));
				}
				if(ServiceID.contains(LoansUtilitiesConstants.Verify_OTP_Service) && sessionObject.getAttribute("isRequestOTPCalled").equals("false")){
					log.debug("Not authorized for this request");
					result = ErrorCodeEnum.ERR_31001.constructResultObject();
					result.addParam(new Param("opstatus","5004"));
					return false;
				}
				else if(ServiceID.contains(LoansUtilitiesConstants.Verify_OTP_Service)&& inputParams.containsKey("securityKey")) {	
						if(inputParams.get("securityKey").toString().equals(sessionObject.getAttribute("securityKey"))) {
							if(demoFlag.equalsIgnoreCase("true") && inputParams.containsKey("Otp")) {
								sessionObject.setAttribute("isVerifyOTPCalled","true");
								sessionObject.setAttribute("isRequestOTPCalled","false");
								result.addParam(new Param("isVerifyOTPSuccess","true"));
									return false;
							}
							else if(demoOTPNumber!=null && inputParams.containsKey("Otp") && demoFlag.equalsIgnoreCase("false") && demoOTPNumber.equals(inputParams.get("Otp")) && !demoOTPNumber.equals("000000")){
								result = ErrorCodeEnum.ERR_35003.updateResultObject(result);
								sessionObject.setAttribute("isVerifyOTPCalled","true");
								sessionObject.setAttribute("isRequestOTPCalled","false");
								return false;
							}
						}
						else {
							result = ErrorCodeEnum.ERR_35004.updateResultObject(result);
							return false;
						}
				}
				if(ServiceID.contains(LoansUtilitiesConstants.NewCustomer_Registration_Service) && sessionObject.getAttribute("isVerifyOTPCalled").equals("true")){
					inputParams.put("Email",sessionObject.getAttribute("EmailToStore"));
					inputParams.put("Password",sessionObject.getAttribute("PasswordToStore"));
					inputParams.put("UserName",sessionObject.getAttribute("EmailToStore"));
					inputParams.put("Phone",sessionObject.getAttribute("Phone"));
					inputParams.put("Is_MemberEligibile","1");
				}
				else if(ServiceID.contains(LoansUtilitiesConstants.NewCustomer_Registration_Service) && sessionObject.getAttribute("isVerifyOTPCalled").equals("false")){
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
