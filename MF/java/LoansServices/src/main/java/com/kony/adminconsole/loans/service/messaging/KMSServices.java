package com.kony.adminconsole.loans.service.messaging;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.utils.EnvironmentConfigurationsHandler;
import com.konylabs.middleware.controller.DataControllerRequest;

public class KMSServices {
	
	private static final Logger LOGGER = Logger.getLogger(KMSServices.class);
	
	public static String getClaimsToken(DataControllerRequest dcRequest) {
//		String cloudLoginURL=MessagingProperties.getValueForProperty(MessagingConstants.CLOUD_LOGIN);
		//https://accounts.auth.konycloud.com/login
		String key="LOANS_KMS_CLOUD_LOGINURL";
		String cloudLoginURL=EnvironmentConfigurationsHandler.getValue(key, dcRequest);
			LOGGER.info("KMSServices : KMS : value for ExtKey: "+key+" is : "+cloudLoginURL);
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(cloudLoginURL, getInputParamsForAuth(dcRequest), null, null);
		JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(Value);
		JSONObject claimsTokenJson=responseJSON.getJSONObject(MessagingConstants.CLAIMS_TOKEN);
		String claimsToken=claimsTokenJson.getString(MessagingConstants.VALUE);
		
		return claimsToken;
	}

	private static HashMap<String, String> getInputParamsForAuth(DataControllerRequest dcRequest) {
		HashMap<String, String> inputParams = new HashMap<String, String>();
//		String userid=MessagingProperties.getValueForProperty(MessagingConstants.USERID);
//		String password=MessagingProperties.getValueForProperty(MessagingConstants.PASSWORD);
		
		String key_USERID="LOANS_KMS_CLOUD_USERID";
		String key_PASSWORD="LOANS_KMS_CLOUD_PASSWORD";
		
		String userid=EnvironmentConfigurationsHandler.getValue(key_USERID, dcRequest);
		String password=EnvironmentConfigurationsHandler.getValue(key_PASSWORD, dcRequest);
		
			LOGGER.info("KMS : value for ExtKey: "+key_USERID+" is : "+userid);
			LOGGER.info("KMS : value for ExtKey: "+key_PASSWORD+" is : "+password);
		
		inputParams.put(MessagingConstants.USERID, userid);
		inputParams.put(MessagingConstants.PASSWORD, password);
		
		return inputParams;
	}
	
//	public static JSONObject createUserInKMS(String firstName, String lastName, String email, String mobileNumber, String country, String state) {
//		JSONObject createUserPayloadJson=gen_createUserInKMS_Json(firstName, lastName, email, mobileNumber, country, state);
//		String serviceURL=MessagingProperties.getValueForProperty("KMSBaseURL")+MessagingProperties.getValueForProperty("User.create");
//		
//			HashMap<String, String> headerParams = new HashMap<String, String>();
//			String claimsToken=KMSServices.getClaimsToken(null);
//			headerParams.put("X-Kony-Authorization", claimsToken);
//			headerParams.put("Content-Type", "application/json");
//			
//		String response=HTTPOperations.hitPOSTServiceAndGetResponse(serviceURL, createUserPayloadJson, null, headerParams);
//		JSONObject responseJSON=new JSONObject(response);
//		return responseJSON;
//	}
	
//	String payload = "{\"firstName\" : \"FNTest\", \"lastName\" :
//			\"LNTest\",\"email\" : \"testemail@test1234.com\",\"active\" :
//			true,\"mobileNumber\":\"918238283235\",\"country\" :\"United
//			States\",\"state\" : \"California\"}";
//	public static JSONObject gen_createUserInKMS_Json(String firstName, String lastName, String email, String mobileNumber, String country, String state) {
//		JSONObject createUserPayloadJson=new JSONObject();
//		createUserPayloadJson.put("firstName", firstName);
//		createUserPayloadJson.put("lastName", lastName);
//		createUserPayloadJson.put("email", email);
//		createUserPayloadJson.put("active", true);
//		createUserPayloadJson.put("mobileNumber", mobileNumber);
//		createUserPayloadJson.put("country", country);
//		createUserPayloadJson.put("state", state);
//		return createUserPayloadJson;
//	}
}