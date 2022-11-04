package com.kony.adminconsole.loans.preprocessor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.api.ConfigurableParametersHelper;
import com.konylabs.middleware.api.processor.FabricRequestChain;
import com.konylabs.middleware.api.processor.PayloadHandler;
import com.konylabs.middleware.api.processor.manager.FabricRequestManager;
import com.konylabs.middleware.api.processor.manager.FabricResponseManager;
import com.konylabs.middleware.common.objectservice.ObjectServicePreProcessor;

public class UpdateLoanPreprocessor implements ObjectServicePreProcessor{

	private static final Logger log = Logger
			.getLogger(UpdateLoanPreprocessor.class);
	/**
	 * @author kh2267
	 */
	@Override
	public void execute(FabricRequestManager fabricRequestManager, FabricResponseManager fabricResponseManager, FabricRequestChain fabricRequestChain)
			throws Exception {
		try {
			PayloadHandler requestPayloadHandler = fabricRequestManager.getPayloadHandler();
			JsonObject requestPayloadJsonObject = requestPayloadHandler.getPayloadAsJson().getAsJsonObject();
			String loggedInUserCustomer_id = AddUserIdenitificationToServicePreprocessor.getCustomerIdFromIdentityService(fabricRequestManager);
			if(requestPayloadJsonObject!=null && requestPayloadJsonObject.has("id") && requestPayloadJsonObject.get("id")!=null){
				if(isThisValidRequest(requestPayloadJsonObject,loggedInUserCustomer_id,"id",fabricRequestManager)) {
					continueTheExecution(requestPayloadJsonObject,requestPayloadHandler,fabricRequestChain);
				}
				else {
					ErrorCodeEnum.ERR_31001.updateResultObject(fabricResponseManager);
				}
			}
			else if(requestPayloadJsonObject!=null && requestPayloadJsonObject.has("QueryResponseID") && requestPayloadJsonObject.get("QueryResponseID")!=null) {
				if(isThisValidRequest(requestPayloadJsonObject,loggedInUserCustomer_id,"QueryResponseID",fabricRequestManager)) {
					continueTheExecution(requestPayloadJsonObject,requestPayloadHandler,fabricRequestChain);
				}
				else {
					ErrorCodeEnum.ERR_31001.updateResultObject(fabricResponseManager);
				}
			}
			else {
				log.error("No LoanId/ApplicationId/QueryResponseID in the payload to compare. Sending the error response to client");
				ErrorCodeEnum.ERR_33403.updateResultObject(fabricResponseManager);
			}
		}
		catch(Exception e) {
			log.error("Unknown Exception : "+e.getLocalizedMessage());
			ErrorCodeEnum.ERR_31000.updateResultObject(fabricResponseManager);
		}
	}
	
	/**
	 * 
	 * @param RequestParam received from client
	 * @param requestPayloadHandler 
	 * @param fabricRequestChain
	 * 
	 * It will pass this request to object service
	 */
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
	
	/**
	 * 
	 *Validates whether this service is requested by authorized user or not.
	 * 
	 * @param requestPayloadJsonObject RequestParam received from client
	 * @param loggedInUserCustomer_id CustomerId retrieved from IdentityScope
	 * @param ApplicationIdentifier Column that we need to retrieve from RequestParam to get the applicationId 
	 * @param fabricRequestManager FabricRequestManager from interface
	 * @return true if it validates with CustomerId retrieved from backend.
	 */
	private boolean isThisValidRequest(JsonObject requestPayloadJsonObject,String loggedInUserCustomer_id,String ApplicationIdentifier,FabricRequestManager fabricRequestManager) {
		
		JSONObject requestParamGetApplicationCustomer = new JSONObject();
		String applicationCustomer_id=null;
		String applicationCoborrowerId=null;
		
		//Adding Applicationid to payload to send to getApplicationCustomer
		String ApplicationId = requestPayloadJsonObject.get(ApplicationIdentifier).getAsString();
		requestParamGetApplicationCustomer.put("id",ApplicationId);
		
		//Getting CustomerId/CoborrowerId for ApplicationId from database
		String getApplicationCustomerResponse = HTTPOperations.hitPOSTServiceAndGetResponse(LoansServiceURLEnum.GET_APPLICATION_CUSTOMER.getServiceURL(fabricRequestManager), requestParamGetApplicationCustomer,fabricRequestManager.getHeadersHandler().getHeader("X-Kony-Authorization"),null);
		JSONObject getApplicationCustomerJSONResponse = new JSONObject(getApplicationCustomerResponse);
		JSONArray queryResponseJSONArray = new JSONArray(getApplicationCustomerJSONResponse.get("QueryResponse").toString());
		JSONObject applicationInfoJSONObject = queryResponseJSONArray.getJSONObject(0);
		
		//Validating the request
		if(applicationInfoJSONObject!=null) {
			if(applicationInfoJSONObject.has("Customer_id")) {
				applicationCustomer_id = applicationInfoJSONObject.getString("Customer_id");
			}
			if(applicationInfoJSONObject.has("CoBorrower_id")) {
				applicationCoborrowerId = applicationInfoJSONObject.getString("CoBorrower_id");
			}
			if(loggedInUserCustomer_id.equals(applicationCustomer_id) || loggedInUserCustomer_id.equals(applicationCoborrowerId)) {
				return true;
			}
			else {
				log.error("Customer_id doesn't match with requested Customer's Id; Request Object Received : "+requestPayloadJsonObject.toString());
				return false;
			}
		}
		else {
			log.error("Not able to retrieve data for the requested ApplicationId :- "+requestPayloadJsonObject);
			return false;
		}
	}
}
