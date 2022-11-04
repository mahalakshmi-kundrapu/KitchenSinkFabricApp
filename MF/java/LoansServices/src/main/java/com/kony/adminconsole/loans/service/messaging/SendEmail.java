package com.kony.adminconsole.loans.service.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.errorhandling.LoansException;
import com.kony.adminconsole.loans.utils.EnvironmentConfigurationsHandler;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class SendEmail implements JavaService2 {

	private static final Logger logger = Logger.getLogger(SendEmail.class);
	
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
			JSONObject emailPayloadJson=constructPayload(inputParams, dcRequest);
//			String serviceURL=MessagingProperties.getValueForProperty("KMSBaseURL")+MessagingProperties.getValueForProperty("Email");
			String key="LOANS_KMS_BASEURL";
			String KMSBaseURL=EnvironmentConfigurationsHandler.getValue(key, dcRequest);
				logger.info("SendEmail : KMS : value for ExtKey: "+key+" is : "+KMSBaseURL);
			String serviceURL=KMSBaseURL+MessagingProperties.getValueForProperty("Email");
			String response=HTTPOperations.hitPOSTServiceAndGetResponse(serviceURL, emailPayloadJson, null, headerParams);
			
			
			JSONObject responseJSON=new JSONObject(response);
			String response_id=responseJSON.getString("id");
			String response_message=responseJSON.getString("message");
	
			result.addParam(new Param("id", response_id));
			result.addParam(new Param("Email_message", response_message));
			result=ErrorCodeEnum.ERR_34100.updateResultObject(result);

			if(response_message.contains("Request Queued")){
				//Send Email success
				result=ErrorCodeEnum.ERR_34100.updateResultObject(result);
			}else{
				result=ErrorCodeEnum.ERR_34101.updateResultObject(result, response_message);
			}

			/*Success Sample Response
			response : {
				  "id" : "7699678711458226067",
				  "message" : "Request Queued. "
				  "dbpErrCode" : "34100",
				  "dbpErrMsg" : "Email request is queued at the Email server"
				}
			*/
		}catch(LoansException loansException){
			result=loansException.constructResultObject();
		}catch(Exception e){
			result=ErrorCodeEnum.ERR_31000.constructResultObject();
				logger.error(e.getMessage());
			/*Failure Sample Response
			response : {
				  "dbpErrCode" : "31000",
				  "dbpErrMsg" : "An unknown error occurred while processing the request"
				}
			*/
		}
		
		return result;
	}

	@SuppressWarnings("rawtypes")
	public JSONObject constructPayload(Map inputParams, DataControllerRequest request) throws LoansException {
		try{
			String sendToEmails=(String) inputParams.get("sendToEmails");
			String copyToEmails=(String) inputParams.get("copyToEmails");
			//String bccToEmails=(String) inputParams.get("bccToEmails");
		    String bccToEmails="";
		    if(inputParams.containsKey("bccToEmails")){
		          bccToEmails=(String) inputParams.get("bccToEmails");   
		      }
			String senderName=(String) inputParams.get("senderName");
			String subject=(String) inputParams.get("subject");
//			String senderEmail=(String) inputParams.get("senderEmail");
				//discarding the payload value for KMS requirement to strictly use the Sender EMail id as it is configured in KMS
			String senderEmail="";
				//marking senderEmail as empty, in order to support any email configured in the pointing KMS env
			String inputContent=(String) inputParams.get("content");
				logger.info("inputContent:"+inputContent);
			JSONArray recipient=constructRecipientArray(sendToEmails, bccToEmails);
			JSONObject recipients = new JSONObject();
			recipients.put("recipient", recipient);
			
			loadEmailTemplateConfigJson();
			
			String content = constructEmailContent(inputContent);
			JSONObject email = new JSONObject();
				email.put("recipients", recipients);
				email.put("senderEmail", senderEmail);
				email.put("senderName", senderName);
				email.put("copyTo", copyToEmails);
				email.put("subject", subject);
				email.put("content", content);
				email.put("priority", "true");

			JSONObject emails = new JSONObject();
			emails.put("email", email);
			
			JSONObject emailServiceRequest = new JSONObject();
			emailServiceRequest.put("emails", emails);
			JSONObject finalPayload=new JSONObject();
			finalPayload.put("emailServiceRequest", emailServiceRequest);
				logger.info("KMS Email finalPayload:"+finalPayload.toString());
			return finalPayload;
		}catch(LoansException loansException){
			throw loansException;
		}catch(Exception e){
			LoansException loansException=new LoansException(ErrorCodeEnum.ERR_31000);
				logger.error(e.getMessage());
			throw loansException;
		}
	}
	
	private static JSONArray constructRecipientArray(String toEmailIds, String bccEmailIds) throws LoansException {
		try{
			JSONArray recipientArray = new JSONArray();
	    	
	    	//To List
	    	String[] toEmailIdArray=toEmailIds.split(",");
	    	for (int i = 0; i < toEmailIdArray.length; i++) {
	    		String EmailId=toEmailIdArray[i];
	    		if(!EmailId.isEmpty()){
	    			JSONObject recipientJSON=new JSONObject();
	    			recipientJSON.put("type", "TO");
	    			recipientJSON.put("emailId", EmailId);
	        		
	    			recipientArray.put(i, recipientJSON);
	    		}
			}
	    	int toEmailIdSize=recipientArray.length();
	    	//BCC List
	    	if(!bccEmailIds.isEmpty()){
	    		String[] bccEmailIdArray=bccEmailIds.split(",");
	        	for (int i = 0; i < bccEmailIdArray.length; i++) {
	        		String EmailId=bccEmailIdArray[i];
	        		if(!EmailId.isEmpty()){
	        			JSONObject recipientJSON=new JSONObject();
	        			recipientJSON.put("type", "BCC");
	        			recipientJSON.put("emailId", EmailId);
	            		
	        			recipientArray.put((toEmailIdSize+i), recipientJSON);
	        		}
	    		}
	    	}
	    	
	    	return recipientArray;
		}catch(Exception e){
			LoansException loansException=new LoansException(ErrorCodeEnum.ERR_31000);
				logger.error(e.getMessage());
			throw loansException;
		}
    	
	}
	
	private JSONObject EmailTemplateConfigJson=new JSONObject();
	private void loadEmailTemplateConfigJson() throws LoansException{
		try{
			if(EmailTemplateConfigJson.length()<=0){
					logger.info("loading EmailTemplateConfigJson");
				EmailTemplateConfigJson=readEmailTemplateConfigJson();
					logger.info("loaded EmailTemplateConfigJson:"+EmailTemplateConfigJson);
			}
		}catch(LoansException loansException){
			throw loansException;
		}catch(JSONException exception){
				logger.error(exception.getMessage());
			LoansException loansException=new LoansException(ErrorCodeEnum.ERR_34102);
			loansException.appendToErrorMessage(exception.getMessage());
			throw loansException;
		}
	}
	
	public String constructEmailContent(String inputContent) throws LoansException {
		String content="";
		try{
			if(isJSONValid(inputContent)){
				JSONObject inputContentJson=new JSONObject(inputContent);
				String TemplateKey=inputContentJson.getString("TemplateKey");
				boolean isValidTemplateKey=false;
				if(!(TemplateKey.isEmpty()) && (EmailTemplateConfigJson.has(TemplateKey))){
						//Predefined email template flow
						logger.error("Valid template key:"+TemplateKey);
						isValidTemplateKey=true;
					content=processTemplateContent(TemplateKey, inputContentJson);
					isValidTemplateKey=true;
				}
				if(!isValidTemplateKey){
						logger.error("Invalid template key:"+TemplateKey);
					LoansException loansException=new LoansException(ErrorCodeEnum.ERR_34104);
					loansException.appendToErrorMessage(":"+TemplateKey);
					throw loansException;
				}
			}else{
				content="<html><head>\n\t<title></title>\n</head>\n<body>\n<p>"+inputContent+"</p>\n\n\n</body></html>";
			}
		}catch(LoansException loansException){
			throw loansException;
		}catch(Exception exception){
				logger.error(exception.getMessage());
				logger.error(exception.getStackTrace());
			LoansException loansException=new LoansException(ErrorCodeEnum.ERR_34103);
			throw loansException;
		}
		return content;
	}
	
	private String processTemplateContent(String TemplateKey, JSONObject inputContentJson) throws LoansException {
		try{
			JSONObject EmailTemplateJson=EmailTemplateConfigJson.getJSONObject(TemplateKey);
				String HTMLFileName=EmailTemplateJson.getString("TemplateHTML");
				String EmailHTMLTemplatesFolderName="EmailHTMLTemplates";
			String templateContent=readContentFromFile(EmailHTMLTemplatesFolderName+"/"+HTMLFileName);
				logger.info("templateContent:"+templateContent);
			String[] RequiredDynamicKeys=getDynamicKeysForTemplate(TemplateKey, EmailTemplateJson);
			
			for (int i = 0; i < RequiredDynamicKeys.length; i++) {
				String requiredDynamicKey=RequiredDynamicKeys[i];
				if(inputContentJson.has(requiredDynamicKey)){
					String value_DynamicKey=inputContentJson.getString(requiredDynamicKey);
					templateContent=templateContent.replaceAll(requiredDynamicKey, value_DynamicKey);
				}else{
					LoansException loansException=new LoansException(ErrorCodeEnum.ERR_34105);
					loansException.appendToErrorMessage("Dynamic Key :"+requiredDynamicKey+" for Template :"+TemplateKey);
					throw loansException;
				}
			}
				logger.info("processed templateContent:"+templateContent);
			return templateContent;
		}catch(LoansException loansException){
			throw loansException;
		}catch(IOException ioException){
				logger.error(ioException.getStackTrace());
			LoansException loansException=new LoansException(ErrorCodeEnum.ERR_34102);
			loansException.appendToErrorMessage("for Template:"+TemplateKey+"."+ioException.getMessage());
			throw loansException;
		}
		
	}

	private String[] getDynamicKeysForTemplate(String TemplateKey, JSONObject EmailTemplateJson) throws LoansException {
		try{
			String RequiredDynamicKeys=EmailTemplateJson.getString("RequiredDynamicKeys");
			String[] RequiredDynamicKeysArray=RequiredDynamicKeys.split(",");
			return RequiredDynamicKeysArray;
		}catch(Exception exception){
				logger.error(exception.getStackTrace());
			LoansException loansException=new LoansException(ErrorCodeEnum.ERR_34102);
			loansException.appendToErrorMessage("for Template:"+TemplateKey);
			throw loansException;
		}
	}
	
	private JSONObject readEmailTemplateConfigJson() throws LoansException {
		String FileName="EmailTemplateConfig.json";
		try{
			JSONObject EmailTemplateConfigJson=new JSONObject(readContentFromFile(FileName));
			return EmailTemplateConfigJson;
		}catch(IOException ioException){
				logger.error(ioException.getStackTrace());
			LoansException loansException=new LoansException(ErrorCodeEnum.ERR_31002);
			loansException.appendToErrorMessage("for file:"+FileName);
			throw loansException;
		}
	}
	
	public boolean isJSONValid(String test) {
	    try {
	        new JSONObject(test);
	    } catch (JSONException ex) {
            return false;
	    }
	    return true;
	}
	
	private String readContentFromFile(String FileName) throws IOException {
		InputStream input=SendEmail.class.getClassLoader().getResourceAsStream(FileName);
		return IOUtils.toString(input);
	}

	@SuppressWarnings("rawtypes")
	public static Map getInputParamMap(Object[] inputArray) {
		return (HashMap)inputArray[1];
	}
}