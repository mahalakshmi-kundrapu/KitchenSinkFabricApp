package com.kony.adminconsole.loans.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.crypto.EncryptionUtils;
import com.kony.adminconsole.commons.handler.EnvironmentConfigurationsHandler;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.errorhandling.LoansException;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.kony.adminconsole.loans.utils.LoansUtilities;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class VerifyCoBorrower implements JavaService2 {
	
	private static final Logger LOGGER = Logger.getLogger(VerifyCoBorrower.class);

	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(String methodID, Object[] inputArray,DataControllerRequest dcRequest, DataControllerResponse dcResponse) throws Exception {
		Result result = new Result();
		try{
			Map<String, String> inputParams = (HashMap <String, String>)inputArray[1];
			String key = EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.ENCRYPTIONKEY,dcRequest);
			String encryptedID = inputParams.get("id");
			String id = EncryptionUtils.decrypt(encryptedID , key);
			inputParams.put("$filter", "id eq " + id);			
			JSONObject queryCoborrowerResponse = hitQueryCoborrowerGetService(inputParams, dcRequest);
			JSONArray queryResponse = hitQueryResponseGetService(queryCoborrowerResponse , dcRequest);
			JSONObject queryResponseObject = queryResponse.getJSONObject(0);
			Map dateRetrievalMap = LoansUtilities.convertJSONtoMap(queryResponseObject);
			JSONObject configResponse = hitConfigGetResponse(dcRequest);
			String linkExpiryTime = configResponse.getString("config_value");
			Integer time;
			if(linkExpiryTime == "" || linkExpiryTime == null){
				time = 1;
			}else{
				time = Integer.parseInt(linkExpiryTime);
			} 
			SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
			long lastmodifiedts = sdf.parse(queryCoborrowerResponse.getString(LoansUtilitiesConstants.COAPPLICANTINVITATIONLINKVALIDITY)).getTime();
			long currentts = new Date().getTime();
			if (currentts - lastmodifiedts >= 24*time*2*30*60*1000) {
				throw new LoansException(ErrorCodeEnum.ERR_31003);
			}

			if(queryCoborrowerResponse.has("InvitationLinkStatus") && queryCoborrowerResponse.get("InvitationLinkStatus").equals("Link Expired")){
                throw new LoansException(ErrorCodeEnum.ERR_31003);
            }
            if (queryCoborrowerResponse.getBoolean("softdeleteflag") == true) {
                    throw new LoansException(ErrorCodeEnum.ERR_31004);
            }
			result= CommonUtilities.getResultObjectFromJSONObject(queryCoborrowerResponse);
			result.addParam(new Param(LoansUtilitiesConstants.QUERY_DEFINITION_ID, (String)dateRetrievalMap.get(LoansUtilitiesConstants.QUERY_DEFINITION_ID)));
		} catch (LoansException exception) {
			LOGGER.debug(exception);
			result=exception.updateResultObject(result);
		} catch (Exception e) {
			LOGGER.debug(e);
			LoansException unknownException=new LoansException(ErrorCodeEnum.ERR_31000);
			result=unknownException.updateResultObject(result);
		}
		return result;
	}
	private JSONArray hitQueryResponseGetService(JSONObject coborrowerInput , DataControllerRequest dcRequest) throws Exception{
		String id = coborrowerInput.getString(LoansUtilitiesConstants.QUERY_RESPONSE_ID);
		Map<String, String> inputParams = new HashMap <String, String>();
		inputParams.put("$filter", LoansUtilitiesConstants.QUERY_RESPONSE_ID + " eq " + id);
		GetLoanAnswers getloanAnswers= new GetLoanAnswers();
		return getloanAnswers.hitQueryResponseGetService(inputParams, dcRequest, id);	   
	}
	private JSONObject hitConfigGetResponse(DataControllerRequest dcRequest) throws Exception {
		// TODO Auto-generated method stub
		String urlBase = LoansServiceURLEnum.CONFIGURATIONS_GET.getServiceURL(dcRequest);
		Map<String, String> inputParams = new HashMap <String, String>();
		inputParams.put("$filter", "config_key eq LINK_EXPIRY_TIME");
		HashMap<String, String> customHeaders = new HashMap<String, String>();
		customHeaders.put("X-Kony-Authorization", dcRequest.getHeader("X-Kony-Authorization"));
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(urlBase + "loansconfigurations_get",(HashMap<String, String>) inputParams, customHeaders, null);
		JSONObject valueResponseJSON= CommonUtilities.getStringAsJSONObject(Value);
		JSONArray responseArray = valueResponseJSON.getJSONArray("loansconfigurations");
		if(responseArray.length() == 0){
			return null;
		}else{
			return responseArray.getJSONObject(0);
		}
	}
	private static JSONObject hitQueryCoborrowerGetService(Map<String, String> inputParams, DataControllerRequest dcRequest) throws Exception {
		HashMap<String, String> customHeaders = new HashMap<String, String>();
		customHeaders.put("X-Kony-Authorization", dcRequest.getHeader("X-Kony-Authorization"));
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(LoansServiceURLEnum.QUERY_COBORROWER_GET.getServiceURL(dcRequest),(HashMap<String, String>) inputParams, customHeaders, null);
		JSONObject valueResponseJSON= CommonUtilities.getStringAsJSONObject(Value);
		JSONArray responseArray = valueResponseJSON.getJSONArray("querycoborrower");
		if(responseArray.length() == 0){
			return null;
		}else{
			return responseArray.getJSONObject(0);
		}
	}
}
