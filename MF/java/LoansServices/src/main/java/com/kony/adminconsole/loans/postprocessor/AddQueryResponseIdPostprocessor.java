package com.kony.adminconsole.loans.postprocessor;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.konylabs.middleware.api.processor.FabricRequestChain;
import com.konylabs.middleware.api.processor.PayloadHandler;
import com.konylabs.middleware.api.processor.manager.FabricRequestManager;
import com.konylabs.middleware.api.processor.manager.FabricResponseManager;
import com.konylabs.middleware.common.objectservice.ObjectServicePostProcessor;


public class AddQueryResponseIdPostprocessor implements ObjectServicePostProcessor{

	private static final Logger log = Logger
			.getLogger(AddQueryResponseIdPostprocessor.class);
	/**
	 * @author kh2267
	 */
	@Override
	public void execute(FabricRequestManager fabricRequestManager, FabricResponseManager fabricResponseManager) throws Exception {
		PayloadHandler responsePayloadHandler = fabricResponseManager.getPayloadHandler();
		try {
		    BiFunction<InputStream, String, String> getResponseObjectAsStringFromInputStream = (stream, encoding) -> {
		        try {
		          encoding = StringUtils.isBlank(encoding) ? StandardCharsets.UTF_8.name() : encoding;
		          String payload = IOUtils.toString(stream, encoding);
		          return payload;
		        } catch (Exception e) {
		          return "Error while getting data from Stream : " + e.getMessage();
		        }
		      };
		    String responseAsPayload = responsePayloadHandler.processAndUpdatePayload(StandardCharsets.UTF_8.name(), getResponseObjectAsStringFromInputStream);
		    JSONObject responseAsPayloadJSON = new JSONObject(responseAsPayload);
		    if(responseAsPayloadJSON.has("QueryResponse_id") && responseAsPayloadJSON.getString("QueryResponse_id")!=null) {
		    		String ApplicationId = responseAsPayloadJSON.getString("QueryResponse_id");
		    		if(ApplicationId!=null) {
		    			JSONObject ApplicationIds = (JSONObject) fabricRequestManager.getSessionHandler().getAttribute("UserSpecificApplicationIds");
		    			if(ApplicationIds==null) {
		    				// there are no applications user just now got registered
		    				ApplicationIds = new JSONObject();
		    			}
		    			ApplicationIds.put(ApplicationId,true);
		    			fabricRequestManager.getSessionHandler().setAttribute("UserSpecificApplicationIds",ApplicationIds);
		    		}
		    		else {
		    			log.error("Invalid Application ID in response - Unable to add in response"+responseAsPayloadJSON.toString());
		    		}
		    	}
			    else {
			    	log.error("Null Response recieved for createApplication - Unable to add in Session Application Ids"+responseAsPayloadJSON);
			    }
		}
		catch(Exception e) {
			log.error("Unknown Error while validating response"+e.getMessage());
		}
	}
}
