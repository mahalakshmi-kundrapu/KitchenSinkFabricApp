package com.kony.adminconsole.loans.service.messaging;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.utils.EnvironmentConfigurationsHandler;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class CreateKMSUser implements JavaService2{

	@SuppressWarnings("rawtypes")
	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse dcResponse){
		Result result = new Result();
		try{
			HashMap<String, String> headerParams = new HashMap<String, String>();
				String claimsToken=KMSServices.getClaimsToken(dcRequest);
				headerParams.put("X-Kony-Authorization", claimsToken);
				headerParams.put("Content-Type", "application/json");
	
			Map inputParams = getInputParamMap(inputArray);
			JSONObject createUserPayloadJson=constructPayload(inputParams, dcRequest);
//			String serviceURL=MessagingProperties.getValueForProperty("KMSBaseURL")+MessagingProperties.getValueForProperty("User.create");
			String key="LOANS_KMS_BASEURL";
			String KMSBaseURL=EnvironmentConfigurationsHandler.getValue(key, dcRequest);
			String serviceURL=KMSBaseURL+MessagingProperties.getValueForProperty("User.create");
			String response=HTTPOperations.hitPOSTServiceAndGetResponse(serviceURL, createUserPayloadJson, null, headerParams);
			
			
			JSONObject responseJSON=new JSONObject(response);
			String response_id=responseJSON.getString("id");
			String response_message=responseJSON.getString("message");
	
			result.addParam(new Param("id", response_id));
			result.addParam(new Param("Email",(String) inputParams.get("email")));
			result.addParam(new Param("Phone",(String) inputParams.get("mobileNumber")));
			result.addParam(new Param("message", response_message));
			
			if(response_message.equalsIgnoreCase("Details added successfully")){
				//Creation success
				result=ErrorCodeEnum.ERR_34001.updateResultObject(result);
			}else{
				result=ErrorCodeEnum.ERR_34002.updateResultObject(result, response_message);
			}
			
		}catch(Exception e){
			result=ErrorCodeEnum.ERR_31000.constructResultObject();
		}
		
		return result;
	}
	
	@SuppressWarnings("rawtypes")
	private JSONObject constructPayload(Map inputParams, DataControllerRequest dcRequest) {
		String firstName=(String) inputParams.get("firstName");
		String lastName=(String) inputParams.get("lastName");
		String email=(String) inputParams.get("email");
		String country=(String) inputParams.get("country");
		String state=(String) inputParams.get("state");
		return gen_createUserInKMS_Json(firstName, lastName, email, country, state, dcRequest);
	}
	
	//	String payload = "{\"firstName\" : \"FNTest\", \"lastName\" :
	//	\"LNTest\",\"email\" : \"testemail@test1234.com\",\"active\" :
	//	true,\"mobileNumber\":\"918238283235\",\"country\" :\"United
	//	States\",\"state\" : \"California\"}";
	public static JSONObject gen_createUserInKMS_Json(String firstName, String lastName, String email, String country, String state, DataControllerRequest dcRequest) {
		JSONObject createUserPayloadJson=new JSONObject();
		createUserPayloadJson.put("firstName", firstName);
		createUserPayloadJson.put("lastName", lastName);
		createUserPayloadJson.put("email", email);
		createUserPayloadJson.put("active", true);
		createUserPayloadJson.put("country", country);
		createUserPayloadJson.put("state", state);
		return createUserPayloadJson;
	}

	@SuppressWarnings("rawtypes")
	public static Map getInputParamMap(Object[] inputArray) {
		return (HashMap)inputArray[1];
	}
	
}
