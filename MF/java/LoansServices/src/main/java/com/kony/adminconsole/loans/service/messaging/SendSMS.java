package com.kony.adminconsole.loans.service.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.config.MessageConstraints;
import org.apache.log4j.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kony.adminconsole.commons.handler.EnvironmentConfigurationsHandler;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.errorhandling.LoansException;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SendSMS implements JavaService2 {

	@SuppressWarnings("rawtypes")
	@Override	
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse dcResponse) throws Exception {
		Result result = new Result();
		try{
//			HashMap<String, String> headerParams = new HashMap<String, String>();
//				String claimsToken=KMSServices.getClaimsToken();
//				headerParams.put("X-Kony-Authorization", claimsToken);
//				headerParams.put("Content-Type", "application/json");
			String demoFlag = EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.DEMO_FLAG_KEY,dcRequest);	
			String TwlioDemoNumber = EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.DEMO_TWILIO_NUMBER,dcRequest);
			Map inputParams = getInputParamMap(inputArray);
			String MobileNumber = (String)inputParams.get("sendToMobiles");
			//DEMO FLAG FOR BYPASSING THE CODE
			if(demoFlag.equalsIgnoreCase("true") && inputParams.containsKey("NeedToSendSMS") && inputParams.get("NeedToSendSMS").equals("true")) {
				result.addParam(new Param("id", "1"));
				result.addParam(new Param("SMS_Message",MessagingConstants.TWILIO_SUCCESS_MESSAGE));
				result=ErrorCodeEnum.ERR_34200.updateResultObject(result);
				return result;
			}
			else if(demoFlag.equalsIgnoreCase("false") && MobileNumber.equalsIgnoreCase(TwlioDemoNumber)) {
				result.addParam(new Param("id", "1"));
				result.addParam(new Param("SMS_Message",MessagingConstants.TWILIO_SUCCESS_MESSAGE));
				result=ErrorCodeEnum.ERR_34200.updateResultObject(result);
				return result;
			}
			if(inputParams.containsKey("NeedToSendSMS") && inputParams.get("NeedToSendSMS").equals("true")) {
//				JSONObject smsPayloadJson=constructPayload(inputParams, dcRequest);
//				String serviceURL=MessagingProperties.getValueForProperty("KMSBaseURL")+MessagingProperties.getValueForProperty("SMS");
//				String response=HTTPOperations.hitPOSTServiceAndGetResponse(serviceURL, smsPayloadJson, null, headerParams);
				loadSMSTemplateConfigJson();
				String messageContent = constructSMSContent((String)inputParams.get("content")); //Content from InputParams
				
				//TWILIO API 
				String TWILIO_ACCOUNT_SID = EnvironmentConfigurationsHandler.getValue("LOANS_TWILIO_ACCOUNT_SID",dcRequest);
				String TWILIO_AUTH_TOKEN = EnvironmentConfigurationsHandler.getValue("LOANS_TWILIO_AUTH_TOKEN",dcRequest);
				String TWILIO_SOURCE_PHONE_NUMBER = EnvironmentConfigurationsHandler.getValue("LOANS_TWILIO_SOURCE_PHONE_NUMBER",dcRequest);
				
				Twilio.init(TWILIO_ACCOUNT_SID,TWILIO_AUTH_TOKEN);
			     Message messageClientToSendSMS = Message
			             .creator(new PhoneNumber((String)inputParams.get("sendToMobiles")), // to
			                     new PhoneNumber(TWILIO_SOURCE_PHONE_NUMBER), // from
			                     messageContent).create();
			     
			     LOGGER.debug("TWILIO SMS response : "+messageClientToSendSMS.toString());
			    
//				JSONObject responseJSON=new JSONObject(response);
				String response_id=messageClientToSendSMS.getSid()!=null?messageClientToSendSMS.getSid().toString():MessagingConstants.TWILIO_NULL_Sid_MESSAGE;
				String response_message=messageClientToSendSMS.getStatus()!=null?messageClientToSendSMS.getStatus().toString():MessagingConstants.TWILIO_NULL_STATUS_MESSAGE;
				
				result.addParam(new Param("id", response_id));
				result.addParam(new Param("SMS_Message", response_message));
				
				result=ErrorCodeEnum.ERR_34200.updateResultObject(result);
			}
			else {
				//Otp generation failed in this case hence throwing error
				result = ErrorCodeEnum.ERR_35002.updateResultObject(result);			
			}
		}catch(LoansException loansException){
			LOGGER.error("Exception in SMS API "+loansException.getMessage());
			result=loansException.constructResultObject();
			result = ErrorCodeEnum.ERR_31000.updateResultObject(result,loansException.getMessage());
			result.addParam(new Param("SMS_Message", loansException.getMessage()));
		}catch(Exception e){
			LOGGER.error("Unknown Exception in SMS API "+e.getMessage());
			result=ErrorCodeEnum.ERR_31000.constructResultObject();
			result= ErrorCodeEnum.ERR_31000.updateResultObject(result,e.getMessage());
			result.addParam(new Param("SMS_Message", e.getMessage()));
		}
		return result;
	}
	
	@SuppressWarnings("rawtypes")
	public JSONObject constructPayload(Map inputParams, DataControllerRequest request) throws LoansException {
		try{
			String sendToMobiles=(String) inputParams.get("sendToMobiles");
			String inputContent=(String) inputParams.get("content");
			
			JSONArray recipient=constructRecipientArray(sendToMobiles);
			JSONObject recipients = new JSONObject();
			recipients.put("recipient", recipient);
			
			loadSMSTemplateConfigJson();
			
			String content = constructSMSContent(inputContent);
			JSONObject message = new JSONObject();
				message.put("recipients", recipients);
				message.put("content", content);
				message.put("priorityService", "true");
				message.put("startTimestamp", "0");
				message.put("expiryTimestamp", "0");

			JSONObject messages = new JSONObject();
			messages.put("message", message);
			
			JSONObject smsServiceRequest = new JSONObject();
			smsServiceRequest.put("messages", messages);
			JSONObject finalPayload=new JSONObject();
			finalPayload.put("smsServiceRequest", smsServiceRequest);
				LOGGER.info("KMS SMS finalPayload:"+finalPayload.toString());
			return finalPayload;
		}catch(LoansException loansException){
			throw loansException;
		}catch(Exception e){
			LoansException loansException=new LoansException(ErrorCodeEnum.ERR_31000);
			throw loansException;
		}
	}
	
	private JSONObject SMSTemplateConfigJson=new JSONObject();
	private void loadSMSTemplateConfigJson() throws LoansException{
		try{
			if(SMSTemplateConfigJson.length()<=0){
					LOGGER.info("loading SMSTemplateConfigJson");
				SMSTemplateConfigJson=readSMSTemplateConfigJson();
					LOGGER.info("loaded SMSTemplateConfigJson:"+SMSTemplateConfigJson);
			}
		}catch(LoansException loansException){
			throw loansException;
		}catch(JSONException exception){
			LoansException loansException=new LoansException(ErrorCodeEnum.ERR_34202);
			loansException.appendToErrorMessage(exception.getMessage());
			throw loansException;
		}
	}
	
	private String processTemplateContent(String TemplateKey, JSONObject inputContentJson) throws LoansException {
		try{
			JSONObject SMSTemplateJson=SMSTemplateConfigJson.getJSONObject(TemplateKey);
				String TxtFileName=SMSTemplateJson.getString("TemplateTxt");
				String SMSTxtTemplatesFolderName="SMSTxtTemplates";
			String templateContent=readContentFromFile(SMSTxtTemplatesFolderName+"/"+TxtFileName);
				LOGGER.info("templateContent:"+templateContent);
			String[] RequiredDynamicKeys=getDynamicKeysForTemplate(TemplateKey, SMSTemplateJson);
			
			for (int i = 0; i < RequiredDynamicKeys.length; i++) {
				String requiredDynamicKey=RequiredDynamicKeys[i];
				if(inputContentJson.has(requiredDynamicKey)){
					String value_DynamicKey=inputContentJson.getString(requiredDynamicKey);
					templateContent=templateContent.replaceAll(requiredDynamicKey, value_DynamicKey);
				}else{
					LoansException loansException=new LoansException(ErrorCodeEnum.ERR_34205);
					loansException.appendToErrorMessage("Dynamic Key :"+requiredDynamicKey+" for Template :"+TemplateKey);
					throw loansException;
				}
			}
				LOGGER.info("processed templateContent:"+templateContent);
			return templateContent;
		}catch(LoansException loansException){
			throw loansException;
		}catch(IOException exception){
			LoansException loansException=new LoansException(ErrorCodeEnum.ERR_34202);
			loansException.appendToErrorMessage("for Template:"+TemplateKey+"."+exception.getMessage());
			throw loansException;
		}
	}
	
	private static final Logger LOGGER = Logger.getLogger(SendSMS.class);
	public String constructSMSContent(String inputContent) throws LoansException {
		String content="";
		try{
			if(isJSONValid(inputContent)){
				JSONObject inputContentJson=new JSONObject(inputContent);
				String TemplateKey=inputContentJson.getString("TemplateKey");
				boolean isValidTemplateKey=false;
				if(!(TemplateKey.isEmpty()) && (SMSTemplateConfigJson.has(TemplateKey))){
						//Predefined SMS template flow
						LOGGER.error("Valid template key:"+TemplateKey);
						isValidTemplateKey=true;
					content=processTemplateContent(TemplateKey, inputContentJson);
				}
				if(!isValidTemplateKey){
						LOGGER.error("Invalid template key:"+TemplateKey);
					LoansException loansException=new LoansException(ErrorCodeEnum.ERR_34204);
					loansException.appendToErrorMessage(":"+TemplateKey);
					throw loansException;
				}
			}else{
				content=inputContent;
			}
		}catch(LoansException loansException){
			throw loansException;
		}catch(Exception e){
			LoansException loansException=new LoansException(ErrorCodeEnum.ERR_34203);
			throw loansException;
		}
		
		return content;
	}
	
	private String[] getDynamicKeysForTemplate(String TemplateKey, JSONObject SMSTemplateJson) throws LoansException {
		try{
			String RequiredDynamicKeys=SMSTemplateJson.getString("RequiredDynamicKeys");
			String[] RequiredDynamicKeysArray=RequiredDynamicKeys.split(",");
			return RequiredDynamicKeysArray;
		}catch(Exception exception){
			LoansException loansException=new LoansException(ErrorCodeEnum.ERR_34202);
			loansException.appendToErrorMessage("for Template:"+TemplateKey);
			throw loansException;
		}
	}
	
	private JSONObject readSMSTemplateConfigJson() throws LoansException {
		String FileName="SMSTemplateConfig.json";
		try{
			JSONObject SMSTemplateConfigJson=new JSONObject(readContentFromFile(FileName));
			return SMSTemplateConfigJson;
		}catch(IOException exception){
			LoansException loansException=new LoansException(ErrorCodeEnum.ERR_31002);
			loansException.appendToErrorMessage("for file:"+FileName);
			throw loansException;
		}
	}
	
	private String readContentFromFile(String FileName) throws IOException {
		InputStream input=SendSMS.class.getClassLoader().getResourceAsStream(FileName);
		return IOUtils.toString(input);
	}
	
	private static JSONArray constructRecipientArray(String mobileNumbers) {
    	String[] mobileNumberArray=mobileNumbers.split(",");
    	JSONArray recipientArray = new JSONArray();
    	for (int i = 0; i < mobileNumberArray.length; i++) {
    		String mobileNumber=mobileNumberArray[i];
    		if(!mobileNumber.isEmpty()){
    			JSONObject recipientJSON=new JSONObject();
    			recipientJSON.put("mobile", mobileNumber);
        		
    			recipientArray.put(i, recipientJSON);
    		}
		}
    	return recipientArray;
	}
	
	public boolean isJSONValid(String test) {
	    try {
	        new JSONObject(test);
	    } catch (JSONException ex) {
            return false;
	    }
	    return true;
	}
	
	@SuppressWarnings("rawtypes")
	public static Map getInputParamMap(Object[] inputArray) {
		return (HashMap)inputArray[1];
	}
	
/*	{
		  "smsServiceRequest" : {
		    "messages" : {
		      "message" : {
		        "startTimestamp" : "0",
		        "expiryTimestamp" : "0",
		        "priorityService" : "true",
		        "recipients" : {
		          "recipient" : [ {
		            "mobile" : "+919789710266"
		          } ]
		        },
		        "content" : "Test Message from TWILIO 04Dec2018"
		      }
		    }
		  }
		}
*/
}