package com.kony.adminconsole.loans.postprocessor;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.function.BiFunction;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.konylabs.middleware.api.processor.PayloadHandler;
import com.konylabs.middleware.api.processor.manager.FabricRequestManager;
import com.konylabs.middleware.api.processor.manager.FabricResponseManager;
import com.konylabs.middleware.common.objectservice.ObjectServicePostProcessor;

public class StoreApplicationIdInSessionPostprocessor implements ObjectServicePostProcessor{

	/**
	 * @author kh2267
	 * 
	 * Stores Application Ids from response to session
	 * 
	 */
	private static final Logger log = Logger
			.getLogger(StoreApplicationIdInSessionPostprocessor.class);
	
		 @Override
		 public void execute(FabricRequestManager requestManager, FabricResponseManager responseManager)
		      throws ParseException,Exception {
			 try {
			    PayloadHandler responsePayloadHandler = responseManager.getPayloadHandler();
			    BiFunction<InputStream, String, String> encrypt = (stream, encoding) -> {
			        try {
			          encoding = StringUtils.isBlank(encoding) ? StandardCharsets.UTF_8.name() : encoding;
			          String payload = IOUtils.toString(stream, encoding);
	//		          JsonObject jsonObject = new JsonParser().parse(payload).getAsJsonObject();
			          return payload;
			        } catch (Exception e) {
			          return "Error in encrypting: " + e.getMessage();
			        }
			      };
			    String responseAsPayload = responsePayloadHandler.processAndUpdatePayload(StandardCharsets.UTF_8.name(), encrypt);
			    JSONObject responseAsPayloadJSON = new JSONObject(responseAsPayload);
			    if(responseAsPayloadJSON.has("records") && responseAsPayloadJSON.get("records")!=null) {
			    	JSONArray responseJSONArray = new JSONArray(responseAsPayloadJSON.get("records").toString());
			    	JSONObject ApplicationIds = new JSONObject();
			    	for(int indexResponseJSONArray=0;indexResponseJSONArray<responseJSONArray.length();indexResponseJSONArray++) {
			    		JSONObject ApplicationJSONObject = responseJSONArray.getJSONObject(indexResponseJSONArray);
			    		String ApplicationId = ApplicationJSONObject.has("id")?ApplicationJSONObject.getString("id"):null;
			    		if(ApplicationId!=null) {
			    			ApplicationIds.put(ApplicationId,true);
			    		}
			    		else {
			    			log.error("Invalid Application ID in response"+ApplicationJSONObject.toString());
			    		}
			    	}
			    	requestManager.getSessionHandler().setAttribute("UserSpecificApplicationIds",ApplicationIds);
			    }
			    else {
			    	log.error("Null Response recieved for getAllApplications"+responseAsPayload);
			    }
			 }
			 catch(JSONException jsonException) {
				log.error("Error in JSON Syntax of records : "+jsonException); 
			 }
			 catch(Exception e) {
				 log.error("Unknown Error while processing the Postprocessor "+e.getMessage());
			 }
		  }
	}
