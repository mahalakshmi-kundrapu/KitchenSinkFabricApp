package com.kony.adminconsole.loans.postprocessor;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.service.messaging.KMSServices;
import com.kony.adminconsole.loans.service.messaging.MessagingProperties;
import com.kony.adminconsole.loans.utils.EnvironmentConfigurationsHandler;
import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;

public class UpdateKMSDetailsPostProcessor implements DataPostProcessor2{

	/**
	 * @author kh2267 Dinesh Kumar
	 */
	private static final Logger log = Logger
			.getLogger(UpdateKMSDetailsPostProcessor.class);
	@Override
	public Object execute(Result result, DataControllerRequest dcRequest, DataControllerResponse dcResponse) throws Exception {
		try {
			if(preProcess(result,dcRequest)) {
				HashMap<String, String> headerParams = new HashMap<String, String>();
				String claimsToken=KMSServices.getClaimsToken(dcRequest);
					headerParams.put("X-Kony-Authorization", claimsToken);
					headerParams.put("Content-Type", "application/json");
				String email = (String) dcRequest.getSession().getAttribute("EmailToStore");
				
				JSONObject updateUserPayloadJson = gen_createUserInKMS_Json(email);
//				String serviceURL=MessagingProperties.getValueForProperty("KMSBaseURL")+MessagingProperties.getValueForProperty("User.create")+"/"+(String)(dcRequest.getSession().getAttribute("KMS_Id"));
				String key="LOANS_KMS_BASEURL";
				String KMSBaseURL=EnvironmentConfigurationsHandler.getValue(key, dcRequest);
					log.info("KMS : value for ExtKey: "+key+" is : "+KMSBaseURL);
				
					String serviceURL=KMSBaseURL+MessagingProperties.getValueForProperty("User.create")+"/"+(String)(dcRequest.getSession().getAttribute("KMS_Id"));
				String response=HTTPOperations.hitPUTServiceAndGetResponse(serviceURL, updateUserPayloadJson, null, headerParams);
				
				//Preparing Result object
				JSONObject responseJSON=new JSONObject(response);
				String response_id=responseJSON.getString("id");
				String response_message=responseJSON.getString("message");
				
				//remvoing CreateKMS messages
				result.removeParamByName("id");
				result.removeParamByName("message");
				
				result.addParam(new Param("id", response_id));
				result.addParam(new Param("message", response_message));
				
				if(response_message.equalsIgnoreCase("Details updated successfully")){
					//Updation success
					result=ErrorCodeEnum.ERR_34003.updateResultObject(result);
				}else{
					result=ErrorCodeEnum.ERR_34004.updateResultObject(result, response_message);
				}
			}
		}
		catch(Exception e) {
			result=ErrorCodeEnum.ERR_31000.constructResultObject();
			result = ErrorCodeEnum.ERR_31000.updateResultObject(result,"Error in PostProcessor KMS User : "+e.getMessage());
		}
		return result;
	}
	public static boolean preProcess(Result result,DataControllerRequest request) throws Exception{
		try {
			Session currentSessionObject = request.getSession();
			if(result.getParamByName("errorCode").getValue()!=null && result.getParamByName("errorCode").getValue().equals("4002")) {
				String KMS_Id = (String)(currentSessionObject.getAttribute("KMS_Id"));
				if(KMS_Id!=null && !KMS_Id.equals("")) {
					return true;
				}
			}
			else if(result.getParamByName("errorCode").getValue()!=null && result.getParamByName("errorCode").getValue().equals("4001")){
				currentSessionObject.setAttribute("KMS_Id",result.getParamByName("id").getValue());
				return false;
			}
		}
		catch(Exception e) {
			log.error("Error oocured while processing in KMS postprocessor"+e.getMessage());
			result = ErrorCodeEnum.ERR_31001.updateResultObject(result,"KMS PostProcessor Error : "+e.getMessage());
			throw e;
		}
		return false;
	}
	public static JSONObject gen_createUserInKMS_Json(String email) {
		JSONObject createUserPayloadJson=new JSONObject();
		createUserPayloadJson.put("email", email);
		createUserPayloadJson.put("firstName", "KonyDBX");
		createUserPayloadJson.put("lastName", "KonyDBX");
		createUserPayloadJson.put("active", true);
		createUserPayloadJson.put("country", "United States");
		createUserPayloadJson.put("state", "New york");
		return createUserPayloadJson;
	}

}
