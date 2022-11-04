package com.kony.adminconsole.loans.preprocessor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.LoansException;
import com.kony.adminconsole.loans.utils.EnvironmentConfigurationsHandler;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.api.processor.FabricRequestChain;
import com.konylabs.middleware.api.processor.PayloadHandler;
import com.konylabs.middleware.api.processor.SessionHandler;
import com.konylabs.middleware.api.processor.manager.FabricRequestManager;
import com.konylabs.middleware.api.processor.manager.FabricResponseManager;
import com.konylabs.middleware.common.objectservice.ObjectServicePreProcessor;
import com.konylabs.middleware.session.Session;

public class AddUserIdenitificationToServicePreprocessor implements ObjectServicePreProcessor {

	private static final Logger log = Logger
			.getLogger(AddUserIdenitificationToServicePreprocessor.class);
	/**
	 * @author kh2267
	 */
	@Override
	public void execute(FabricRequestManager fabricRequestManager, FabricResponseManager fabricResponseManager, FabricRequestChain fabricRequestChain)
			throws Exception {
		PayloadHandler requestPayloadHandler = fabricRequestManager.getPayloadHandler();
		JsonObject requestPayloadJsonObject = requestPayloadHandler.getPayloadAsJson().getAsJsonObject();
		if(requestPayloadJsonObject!=null) {
			String Customer_id = getCustomerIdFromIdentityService(fabricRequestManager);
			requestPayloadJsonObject.addProperty("Customer_id",Customer_id);
			continueTheExecution(requestPayloadJsonObject, requestPayloadHandler, fabricRequestChain);
		}
		else {
			log.error("Null Request object received from Client "+requestPayloadJsonObject);
			continueTheExecution(requestPayloadJsonObject, requestPayloadHandler, fabricRequestChain);
		}
	}
	public static String getCustomerIdFromIdentityService(FabricRequestManager fabricRequestManager) {
		String CustomerId ="";
		try {
	            String IdentityServiceURL = EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.MF_IDENTITY_URL,fabricRequestManager)+LoansUtilitiesConstants.USER_SESSION_URL;
	            String Response = HTTPOperations.hitGETServiceAndGetResponse(IdentityServiceURL,null,fabricRequestManager.getHeadersHandler().getHeader("X-Kony-Authorization"));
	            if(Response!=null) {
	                JSONObject responseJSONObject = new JSONObject(Response);
	                if(responseJSONObject.has("user_id")) {
	                    CustomerId = responseJSONObject.get("user_id")==null?null:responseJSONObject.get("user_id").toString();
	                    String UserName = responseJSONObject.get("UserName")==null?null:responseJSONObject.get("UserName").toString();
	                }
	                else {
	                    log.error("Unable to find Userid for this session");
	                }
	            }
	            else {
	                log.error("Unable to find UserId for this session");
	            }
		}
		catch(Exception exception) {
			log.error("Unknown Exception while retrieving Customer id from identity"+exception.getMessage());
			return CustomerId;
		}
        return CustomerId;
	}
	
	public static String getCustomerUserNameFromIdentityService(FabricRequestManager fabricRequestManager) {
		String UserName ="";
		try {
	            String IdentityServiceURL = EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.MF_IDENTITY_URL,fabricRequestManager)+LoansUtilitiesConstants.USER_SESSION_URL;
	            String Response = HTTPOperations.hitGETServiceAndGetResponse(IdentityServiceURL,null,fabricRequestManager.getHeadersHandler().getHeader("X-Kony-Authorization"));
	            if(Response!=null) {
	                JSONObject responseJSONObject = new JSONObject(Response);
	                if(responseJSONObject.has("user_id")) {
	                    UserName = responseJSONObject.get("UserName")==null?null:responseJSONObject.get("UserName").toString();
	                }
	                else {
	                    log.error("Unable to find Userid for this session");
	                }
	            }
	            else {
	                log.error("Unable to find UserId for this session");
	            }
		}
		catch(Exception exception) {
			log.error("Unknown Exception while retrieving Customer id from identity"+exception.getMessage());
			return UserName;
		}
        return UserName;
	}
	
	private void continueTheExecution(JsonObject requestPayloadJsonObject,PayloadHandler requestPayloadHandler,FabricRequestChain fabricRequestChain) {
		BiFunction<InputStream, String, InputStream> convertToStream = (stream, encoding) -> {
	        try {
	          encoding = StringUtils.isBlank(encoding) ? StandardCharsets.UTF_8.name() : encoding;
	          return new ByteArrayInputStream(requestPayloadJsonObject.toString().getBytes(encoding));
	        } catch (Exception e) {
	          log.error("Unknown Error in executing the Preprocessor "+e.getLocalizedMessage());
	        }
	        return null;
	      };
	    requestPayloadHandler.processAndUpdatePayload(StandardCharsets.UTF_8.name(), convertToStream);
		fabricRequestChain.execute();
	}
	
}
