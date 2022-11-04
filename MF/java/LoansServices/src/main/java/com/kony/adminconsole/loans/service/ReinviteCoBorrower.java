package com.kony.adminconsole.loans.service;

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
import com.konylabs.middleware.dataobject.Result;

public class ReinviteCoBorrower implements JavaService2 {
	
	private static final Logger LOGGER = Logger.getLogger(VerifyCoBorrower.class);
	@SuppressWarnings({ "unchecked" })
	@Override
	public Object invoke(String methodID, Object[] inputArray,DataControllerRequest dcRequest, DataControllerResponse dcResponse) throws Exception {
		Result result = new Result();
		try{
			Map<String, String> paramList = (HashMap <String, String>)inputArray[1];
			String queryDefinitionId = paramList.getOrDefault(LoansUtilitiesConstants.QUERY_DEFINITION_ID, "Loan");
			paramList.put("$filter", "QueryResponse_id eq " + paramList.get("id").toString() + " and softdeleteflag eq 0");
			JSONArray responseArray = hitQueryCoborrowerGetService(paramList, dcRequest);
			if(responseArray!=null){
				JSONObject responseJson = responseArray.getJSONObject(0);
				paramList = LoansUtilities.convertJSONtoMap(responseJson);
				paramList.put("InvitationLinkStatus", "Reinvited");
				paramList.remove("lastmodifiedts");paramList.remove("createdts");
				hitQueryCoborrowerUpdateService(paramList, dcRequest);
				hitQueryCoborrowerDeleteService(paramList, dcRequest);
				paramList.remove("InvitationLinkStatus");
				String id = "KL" + CommonUtilities.getNewId().toString();
				paramList.put("id", id);
				paramList.put(LoansUtilitiesConstants.COAPPLICANTINVITATIONLINK, GenerateInviteLink(id , dcRequest));
				paramList.put(LoansUtilitiesConstants.COAPPLICANTINVITATIONLINKVALIDITY, CommonUtilities.convertTimetoISO8601Format(new Date()));
				paramList.put("softdeleteflag", "0");
				hitQueryCoborrowerCreateService(paramList, dcRequest);
				sendinvite(paramList, dcRequest ,queryDefinitionId);
			} else{
				result=ErrorCodeEnum.ERR_31007.updateResultObject(result);
			}
			
		} catch (Exception e) {
			LOGGER.debug(e);
			LoansException unknownException=new LoansException(ErrorCodeEnum.ERR_31000);
			result=unknownException.updateResultObject(result);
		}
		return result;
	}
	
	private String GenerateInviteLink(String id, DataControllerRequest dcRequest) throws Exception {
		String link = LoansServiceURLEnum.APP_URL.getServiceURL(dcRequest);
		String key = EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.ENCRYPTIONKEY,dcRequest);
		String encryptedLink = EncryptionUtils.encrypt(id, key);
		return link + "#" + encryptedLink;
	}
	
	private void sendinvite(Map paramlist , DataControllerRequest dcRequest, String queryDefinitionId) {
		
		Map payLoad = constructEmailPayLoad(paramlist ,  queryDefinitionId);
		
		HashMap < String, String > customHeaders = new HashMap < String, String > ();
		customHeaders.put("X-Kony-Authorization", dcRequest.getHeader("X-Kony-Authorization"));
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(
				LoansServiceURLEnum.MESSAGING_SENDEMAIL.getServiceURL(dcRequest),
				(HashMap < String, String > ) payLoad, customHeaders, null);
		JSONObject valueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
	}
	private Map constructEmailPayLoad(Map paramlist, String queryDefinitionId){
		String str = paramlist.get(LoansUtilitiesConstants.COAPPLICANTINVITATIONLINK).toString();
		String email = paramlist.get(LoansUtilitiesConstants.COAPPLICANTEMAIL).toString();
		Map payLoad  = new HashMap< String, String>();
		payLoad.put("sendToEmails", email);
		payLoad.put("senderEmail","logeswaran.rathinasamy@kony.com");
		payLoad.put("copyToEmails","");
		payLoad.put("bccToEmails","");
		payLoad.put("senderName","KonyDBX");
		payLoad.put("subject","You have been invited as a Co-Applicant");
		payLoad.put("content",  createContentJSONObjectForEmail(paramlist , str , queryDefinitionId));	
		return payLoad;
	}
	private static JSONObject createContentJSONObjectForEmail(Map paramlist, String str, String queryDefinitionId) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("INVITATION_URL", str);
		jsonObject.put("TemplateKey","COAPPLICANT_INVITE_EMAIL_TEMPLATE");
		jsonObject.put("COAPPLICANT_NAME", paramlist.get(LoansUtilitiesConstants.COAPPLICANTFIRSTNAME).toString());
		jsonObject.put("APPLICANT_NAME" , "KonyDBX");
		jsonObject.put("APPLICANT_NAME","KonyDBXUser");
		jsonObject.put("BANK_CU_NAME","KonyDBXUser");
		if(queryDefinitionId.equalsIgnoreCase("PERSONAL_APPLICATION")){
			queryDefinitionId = "Personal";
		}else{
			queryDefinitionId = "Vehicle";
		}
		jsonObject.put("LOAN_TYPE", queryDefinitionId);
		return jsonObject;
	}
	private Result hitQueryCoborrowerCreateService(Map < String, String > inputParams, DataControllerRequest dcRequest) throws Exception {
		Result result = null;
		HashMap < String, String > customHeaders = new HashMap < String, String > ();
		customHeaders.put("X-Kony-Authorization", dcRequest.getHeader("X-Kony-Authorization"));
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(
				LoansServiceURLEnum.QUERY_COBORROWER_CREATE.getServiceURL(dcRequest),
				(HashMap < String, String > ) inputParams, customHeaders, null);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		return result;
	}
	
	private JSONArray hitQueryCoborrowerGetService(Map < String, String > inputParams, DataControllerRequest dcRequest) throws Exception {
		HashMap < String, String > customHeaders = new HashMap < String, String > ();
		customHeaders.put("X-Kony-Authorization", dcRequest.getHeader("X-Kony-Authorization"));
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(
				LoansServiceURLEnum.QUERY_COBORROWER_GET.getServiceURL(dcRequest),
				(HashMap < String, String > ) inputParams, customHeaders, null);
		JSONObject valueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		JSONArray responseArray = valueResponseJSON.getJSONArray("querycoborrower");
		if (responseArray.length() == 0) {
			return null;
		} else {
			return responseArray;
		}
	}
	
	public Result hitQueryCoborrowerUpdateService(Map < String, String > inputParams, DataControllerRequest dcRequest) throws Exception {
		Result result = null;
		HashMap < String, String > customHeaders = new HashMap < String, String > ();
		customHeaders.put("X-Kony-Authorization", dcRequest.getHeader("X-Kony-Authorization"));
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(
				LoansServiceURLEnum.QUERY_COBORROWER_UPDATE.getServiceURL(dcRequest),
				(HashMap < String, String > ) inputParams, customHeaders, null);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		return result;
	}
	
	private Result hitQueryCoborrowerDeleteService(Map < String, String > inputParams, DataControllerRequest dcRequest) throws Exception {
		Result result = null;
		HashMap < String, String > customHeaders = new HashMap < String, String > ();
		customHeaders.put("X-Kony-Authorization", dcRequest.getHeader("X-Kony-Authorization"));
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(
				LoansServiceURLEnum.QUERY_COBORROWER_DELETE.getServiceURL(dcRequest),
				(HashMap < String, String > ) inputParams, customHeaders, null);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		return result;
	}

}
