
package com.kony.adminconsole.loans.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.crypto.EncryptionUtils;
import com.kony.adminconsole.commons.handler.EnvironmentConfigurationsHandler;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.utils.Executor;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.kony.adminconsole.loans.utils.LoansUtilities;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

@SuppressWarnings({
	"unchecked",
	"rawtypes"
})
public class LoanApplicationQueryResponseCreate implements JavaService2 {

	private static final Logger LOGGER = Logger.getLogger(LoanApplicationQueryResponseCreate.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest,
			DataControllerResponse dcResponse) throws Exception {
		try {
			Result result = new Result();
			Map < String, String > inputParams = (HashMap) inputArray[1];
			SimulationQueryResponseCreate createQueryResponse = new SimulationQueryResponseCreate();
			Dataset QuestionResponseDataset = new Dataset();
			QuestionResponseDataset.setId(LoansUtilitiesConstants.QUESTION_RESPONSE);		
			String queryResponseId;
			
			ArrayList < Record > records = new ArrayList < Record > ();
			boolean shouldCreateQueryResponse = true;
			JSONArray queryCoborrowerArray = null;
			JSONArray customerQSSArray = null;

			String queryDefinitionId = inputParams.get(LoansUtilitiesConstants.QUERY_DEFINITION_ID);
			
			//Creating QueryResponse id 
			if (inputParams.containsKey("id") && inputParams.get("id") != null && !inputParams.get("id").equals("")) {
				shouldCreateQueryResponse = false;
				queryResponseId = (String) inputParams.get("id");
			} else {
				queryResponseId = "KL" + CommonUtilities.getNewId().toString();
				//Map<String, String> inputParamsUser = new HashMap<String, String>();
				//inputParamsUser.put("Customer_id", (String) inputParams.get("Customer_id"));
			    //Result getResult = hitGetApplicantInfo(inputParamsUser, dcRequest);
			    Dataset ds = new Dataset();
			    ds.setId("UserInfo");
			    result.addDataset(ds);
			}
			//Removing QueryCoBorrower
			if (inputParams.containsKey(LoansUtilitiesConstants.QUERY_COBORROWER) && inputParams.get(LoansUtilitiesConstants.QUERY_COBORROWER) != null &&
					!inputParams.get(LoansUtilitiesConstants.QUERY_COBORROWER).equals("")) {
				queryCoborrowerArray = new JSONArray(inputParams.get(LoansUtilitiesConstants.QUERY_COBORROWER).toString());
				inputParams.remove(LoansUtilitiesConstants.QUERY_COBORROWER);
			}
			//Removing CustomerQuerySectionStatus
			if (inputParams.containsKey(LoansUtilitiesConstants.CUSTOMER_QUERY_QUESTION_STATUS) && inputParams.get(LoansUtilitiesConstants.CUSTOMER_QUERY_QUESTION_STATUS) != null &&
					!inputParams.get(LoansUtilitiesConstants.CUSTOMER_QUERY_QUESTION_STATUS).equals("")) {
				customerQSSArray = new JSONArray(inputParams.get(LoansUtilitiesConstants.CUSTOMER_QUERY_QUESTION_STATUS).toString());
				inputParams.remove(LoansUtilitiesConstants.CUSTOMER_QUERY_QUESTION_STATUS);
			}
			inputParams.put("id", queryResponseId);
					
			if (shouldCreateQueryResponse) {
				Result result1 = createQueryResponse.hitQueryResponseService(inputParams, dcRequest);
				result.addAllParams(result1.getAllDatasets().get(0).getRecord(0).getAllParams());
			} else {
				result.addParam(new Param("id", queryResponseId));
			}
			
			//retrieving created date and modified date
			JSONArray dateRetrieval=retriveCreatedDateFromQueryResponse(inputParams,dcRequest,queryResponseId);
			JSONObject dateRetrievalObject = dateRetrieval.getJSONObject(0);
			Map dateRetrievalMap = LoansUtilities.convertJSONtoMap(dateRetrievalObject);
			result.addParam(new Param("createdts", (String)dateRetrievalMap.get("createdts")));
			result.addParam(new Param("lastmodifiedts", (String)dateRetrievalMap.get("lastmodifiedts")));
			
			JSONArray abstractNames = (JSONArray) hitQuerySectionQuestionService(queryDefinitionId, dcRequest).get(LoansUtilitiesConstants.QUERY_SECTION_STATUS);

			//Create QuestionResponse
			JSONArray questionResponseJSON = new JSONArray(inputParams.get(LoansUtilitiesConstants.QUESTION_RESPONSE).toString());
			inputParams.remove(LoansUtilitiesConstants.QUESTION_RESPONSE);
			for (int i = 0; i < questionResponseJSON.length(); i++) {
				JSONObject questionResponseObj = constructQuestionReponsePayload(questionResponseJSON.getJSONObject(i), inputParams, queryResponseId, abstractNames);
				Map paramList = LoansUtilities.convertJSONtoMap(questionResponseObj);
				Result childResult = createQueryResponse.hitQuestionResponseService(paramList, dcRequest);
				Record recordTemp = childResult.getAllDatasets().get(0).getRecord(0);
				records.add(recordTemp);
			}
			QuestionResponseDataset.addAllRecords(records);
			records.removeAll(records);
			result.addDataset(QuestionResponseDataset);

			//Create CustomerQuerySectionStatus 
			if (customerQSSArray != null) {
				JSONArray querySectionArray = hitQuerySectionGetService(inputParams, dcRequest, queryDefinitionId);
				for (int i = 0; i < customerQSSArray.length(); i++) {
					JSONObject record = customerQSSArray.getJSONObject(i);
					record = constructCQSSPayload(record, querySectionArray);							
					Map paramList = LoansUtilities.convertJSONtoMap(record);
					paramList.put("QueryResponse_id", queryResponseId);
					paramList.put("id", "KL" + CommonUtilities.getNewId().toString());
					Result childResult = createQueryResponse.hitCustomerQuerySectionStatusService(paramList, dcRequest);
					Record recordTemp = childResult.getAllDatasets().get(0).getRecord(0);
					records.add(recordTemp);
				}
				Dataset CustomerQuerySectionStatusDataset = new Dataset();
				CustomerQuerySectionStatusDataset.setId(LoansUtilitiesConstants.CUSTOMER_QUERY_QUESTION_STATUS);
				CustomerQuerySectionStatusDataset.addAllRecords(records);
				records.removeAll(records);
				result.addDataset(CustomerQuerySectionStatusDataset);				
			}
			
			if (queryCoborrowerArray != null) {
				CoBorrowerExecution(queryCoborrowerArray, queryResponseId, queryDefinitionId,dcRequest);
				return result;	
			}
			return result;
			
		} catch (Exception e) {
			LOGGER.error("Error Occured in CreateQueryResponseNew class", e);
			Result errorResult=ErrorCodeEnum.ERR_31000.constructResultObject();
			errorResult.addParam(new Param("localmessage", e.getMessage()));
			return errorResult;
		}
	}
   private JSONArray retriveCreatedDateFromQueryResponse(Map<String, String> inputParams, DataControllerRequest dcRequest, String queryResponseId) throws Exception {
	   
	  GetLoanAnswers getloanAnswers= new GetLoanAnswers();
	  return getloanAnswers.hitQueryResponseGetService(inputParams, dcRequest, queryResponseId);	   
   }

	private void CoBorrowerExecution(JSONArray queryCoborrowerArray, String queryResponseId,String queryDefinitionId, DataControllerRequest dcRequest) throws Exception {
		Boolean CoBorrowerRegisterd = false;
		Map< String, String > QRList= new HashMap < String, String >();
		QRList.put("$filter", "id eq " + queryResponseId);
		QRList.put("$select", "CoBorrower_id");
		JSONArray queryResponseJson = hitQueryResponseGetService(QRList, dcRequest);
		if(queryResponseJson!=null){
			CoBorrowerRegisterd=true;
		}
		for (int i = 0; i < queryCoborrowerArray.length(); i++) {
			JSONObject record = queryCoborrowerArray.getJSONObject(i);
			Map paramList = LoansUtilities.convertJSONtoMap(record);
			if (LoansUtilitiesConstants.INDIVIDUALLY.equals(paramList.get("CoBorrower_Type"))) {
				
				paramList.put("$filter", "QueryResponse_id eq " + queryResponseId +  " and softdeleteflag eq 0");
				JSONArray responseJson = hitQueryCoborrowerGetService(paramList, dcRequest);
				if (responseJson != null) {
					for (int j = 0; j < responseJson.length(); j++) {
						JSONObject response = responseJson.getJSONObject(j);
						paramList.put("id", response.getString("id"));
						hitQueryCoborrowerDeleteService(paramList,queryResponseId,queryDefinitionId, dcRequest);
						RemoveCoBorrowerId(queryResponseId, dcRequest);
					}
				}
				DeleteCoApplicantDataInQuestionResponseTable(dcRequest,queryResponseId,queryDefinitionId);
			} else if(!paramList.get(LoansUtilitiesConstants.COAPPLICANTEMAIL).toString().isEmpty() && !(paramList.get(LoansUtilitiesConstants.COAPPLICANTEMAIL)==null) && 
					!paramList.get(LoansUtilitiesConstants.COAPPLICANTFIRSTNAME).toString().isEmpty() && !(paramList.get(LoansUtilitiesConstants.COAPPLICANTFIRSTNAME)==null)&&   
					!paramList.get(LoansUtilitiesConstants.COAPPLICANTLASTNAME).toString().isEmpty() && !(paramList.get(LoansUtilitiesConstants.COAPPLICANTLASTNAME)==null) &&
					!paramList.get(LoansUtilitiesConstants.COAPPLICANTMOBILENUMBER).toString().isEmpty() && !(paramList.get(LoansUtilitiesConstants.COAPPLICANTMOBILENUMBER)==null) &&
					!CoBorrowerRegisterd){
				paramList.put("$filter", "QueryResponse_id eq " + queryResponseId +  " and softdeleteflag eq 0");
				JSONArray responseArray = hitQueryCoborrowerGetService(paramList, dcRequest);
				if (responseArray == null) {
					String id = "KL" + CommonUtilities.getNewId().toString();
					paramList.put("QueryResponse_id", queryResponseId);
					paramList.put("id", id);
					paramList.put(LoansUtilitiesConstants.SOFT_DELETE_FLAG, "0");
					paramList.put(LoansUtilitiesConstants.COAPPLICANTINVITATIONLINKVALIDITY, CommonUtilities.convertTimetoISO8601Format(new Date()));
					paramList.put(LoansUtilitiesConstants.COAPPLICANTINVITATIONLINK, GenerateInviteLink(id, dcRequest));
					hitQueryCoborrowerCreateService(paramList, dcRequest);
				    sendinvite(paramList, dcRequest , queryDefinitionId);  
				} else {
					JSONObject responseJson = responseArray.getJSONObject(0);
					Map responseList = LoansUtilities.convertJSONtoMap(responseJson);
					if(!responseList.get(LoansUtilitiesConstants.COAPPLICANTEMAIL).equals(paramList.get(LoansUtilitiesConstants.COAPPLICANTEMAIL)) || 
							!responseList.get(LoansUtilitiesConstants.COAPPLICANTFIRSTNAME).equals(paramList.get(LoansUtilitiesConstants.COAPPLICANTFIRSTNAME)) ||
							!responseList.get(LoansUtilitiesConstants.COAPPLICANTLASTNAME).equals(paramList.get(LoansUtilitiesConstants.COAPPLICANTLASTNAME)) ||
							!responseList.get(LoansUtilitiesConstants.COAPPLICANTMOBILENUMBER).equals(paramList.get(LoansUtilitiesConstants.COAPPLICANTMOBILENUMBER))){
						hitQueryCoborrowerDeleteService(responseList,queryResponseId,queryDefinitionId, dcRequest);
						String id = "KL" + CommonUtilities.getNewId().toString();
						paramList.put("id", id);
						paramList.put("QueryResponse_id", queryResponseId);
						paramList.put(LoansUtilitiesConstants.COAPPLICANTINVITATIONLINKVALIDITY, CommonUtilities.convertTimetoISO8601Format(new Date()));
						paramList.put(LoansUtilitiesConstants.SOFT_DELETE_FLAG, "0");
						paramList.put(LoansUtilitiesConstants.COAPPLICANTINVITATIONLINK, GenerateInviteLink(id, dcRequest));
						hitQueryCoborrowerCreateService(paramList, dcRequest);
					    sendinvite(paramList, dcRequest,queryDefinitionId);  
					}
				}
			}
		}
	}

	private void sendinvite(Map paramlist , DataControllerRequest dcRequest ,String queryDefinitionId) throws Exception {
		createKMSUSer(paramlist , dcRequest);		
		Map payLoad = constructEmailPayLoad(paramlist ,  queryDefinitionId);		
		Executor.invokeService(LoansServiceURLEnum.MESSAGING_SENDEMAIL, (HashMap<String, String>) payLoad, null, dcRequest);
	}
	private void createKMSUSer(Map paramlist, DataControllerRequest dcRequest) throws Exception {
		Map payLoad = setAttributesToResultObjectForCreateKMS(paramlist, dcRequest);
		Executor.invokeService(LoansServiceURLEnum.MESSAGING_CREATEKMS, (HashMap<String, String>) payLoad, null, dcRequest);
	}

	private static JSONObject createContentJSONObjectForEmail(Map paramlist, String str, String queryDefinitionId) throws Exception {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("INVITATION_URL", str);
		jsonObject.put("TemplateKey","COAPPLICANT_INVITE_EMAIL_TEMPLATE");
		jsonObject.put("COAPPLICANT_NAME", paramlist.get(LoansUtilitiesConstants.COAPPLICANTFIRSTNAME).toString());
		jsonObject.put("APPLICANT_NAME","KonyDBXUser");
		jsonObject.put("BANK_CU_NAME","KonyDBX");
		if(queryDefinitionId.equalsIgnoreCase("PERSONAL_APPLICATION")){
			queryDefinitionId = "Personal";
		}else{
			queryDefinitionId = "Vehicle";
		}
		jsonObject.put("LOAN_TYPE", queryDefinitionId);
		return jsonObject;
	}
	private static Map setAttributesToResultObjectForCreateKMS(Map paramlist,DataControllerRequest dcRequest) throws Exception {
		Map payLoad  = new HashMap< String, String>();
		payLoad.put("firstName","KonyDBX");
		payLoad.put("lastName","KonyDBX");
		payLoad.put("email",paramlist.get(LoansUtilitiesConstants.COAPPLICANTEMAIL).toString());
		payLoad.put("mobileNumber", "+1" + paramlist.get(LoansUtilitiesConstants.COAPPLICANTMOBILENUMBER).toString());
		payLoad.put("country","United States");
		payLoad.put("state","New york");
		return payLoad;
	}
	private Map constructEmailPayLoad(Map paramlist, String queryDefinitionId) throws Exception{
		String str = paramlist.get(LoansUtilitiesConstants.COAPPLICANTINVITATIONLINK).toString();
		String email = paramlist.get(LoansUtilitiesConstants.COAPPLICANTEMAIL).toString();
		Map payLoad  = new HashMap< String, String>();
		payLoad.put("sendToEmails", email);
		payLoad.put("senderEmail","dbx_cl@kony.com");
		payLoad.put("copyToEmails","");
		payLoad.put("bccToEmails","");
		payLoad.put("senderName","KonyDBX");
		payLoad.put("subject","You have been invited as a Co-Applicant");
		payLoad.put("content",  createContentJSONObjectForEmail(paramlist , str , queryDefinitionId));	
		return payLoad;
	}

	private String GenerateInviteLink(String id, DataControllerRequest dcRequest) throws Exception {
		String link = LoansServiceURLEnum.APP_URL.getServiceURL(dcRequest);
		String key = EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.ENCRYPTIONKEY,dcRequest);
		String encryptedLink = EncryptionUtils.encrypt(id, key);
		return link + "#" + encryptedLink;
	}
	private JSONObject constructCQSSPayload(JSONObject payload, JSONArray querySectionArray) {
		payload.put("QuerySection_id", getIdByAbstractNameQuerySection(payload.getString("QuerySection_FriendlyName"), querySectionArray));
		return payload;
	}
	
	private String getIdByAbstractNameQuerySection(String abstractName, JSONArray querySectionArray) {
		for (int i = 0; i < querySectionArray.length(); i++) {
			JSONObject querySection = querySectionArray.getJSONObject(i);
			if (querySection.getString("abstractname").equalsIgnoreCase(abstractName)) 
				return querySection.getString("id");
		}
		return "";
	}
	
	private JSONObject constructQuestionReponsePayload(JSONObject questionResponsePayload, Map < String, String > inputParams, String queryResponseId, JSONArray abstractNames) throws Exception {
		HashMap questionResponse = getQSQIdAndResponseValueFromJSONObject(questionResponsePayload, abstractNames);
		questionResponsePayload.put("id", "KL" + CommonUtilities.getNewId().toString());
		questionResponsePayload.put("QueryResponse_id", queryResponseId);
		questionResponsePayload.put("QueryDefinition_id", inputParams.get(LoansUtilitiesConstants.QUERY_DEFINITION_ID));
		questionResponsePayload.put("QuerySectionQuestion_id", questionResponse.get("QSQid"));
		questionResponsePayload.put("ResponseValue", questionResponse.get("Value"));
		if(questionResponse.containsKey("OptionItem_id")) {
			questionResponsePayload.put("OptionItem_id", questionResponse.get("OptionItem_id"));
		}
		return questionResponsePayload;
	}

	private HashMap getQSQIdAndResponseValueFromJSONObject(JSONObject questionResponsePayload, JSONArray abstractNames) {
		HashMap questionResponse = new HashMap();
		String abstractName = questionResponsePayload.getString(LoansUtilitiesConstants.ABSTRACT_NAME);
		questionResponse.put("QSQid", getQSQIdByAbstractName(abstractName, abstractNames));
		questionResponse.put("Value", questionResponsePayload.getString("Value"));
		if(questionResponsePayload.has("OptionItem_id")) {
			questionResponse.put("OptionItem_id", questionResponsePayload.getString("OptionItem_id"));
		}
		return questionResponse;
	}

	private String getQSQIdByAbstractName(String abstractName, JSONArray abstractNames) {
		for (int i = 0; i < abstractNames.length(); i++) {
			JSONObject obj = abstractNames.getJSONObject(i);
			if (obj.getString(LoansUtilitiesConstants.ABSTRACT_NAME).equalsIgnoreCase(abstractName))
				return obj.getString("id");
		}
		return "";
	}

	public JSONObject hitQuerySectionQuestionService(String queryDefId, DataControllerRequest dcRequest)
			throws Exception {
		Map < String, String > inputParams = new HashMap < String, String > ();
		inputParams.put("$filter", LoansUtilitiesConstants.QUERY_DEFINITION_ID + " eq " + queryDefId);
		inputParams.put("$select", LoansUtilitiesConstants.SELECT_FEILDS);
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_SECTION_QUESTION_GET, (HashMap<String, String>) inputParams, null, dcRequest);
		return CommonUtilities.getStringAsJSONObject(Value);
	}
	
	public JSONArray hitQuerySectionGetService(Map < String, String > inputParams, DataControllerRequest dcRequest, String queryDefinition_id) throws Exception {
		inputParams.put("$filter", "QueryDefinition_id eq " + queryDefinition_id);
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_SECTION_GET, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject valueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		JSONArray responseArray = valueResponseJSON.getJSONArray("querysection");
		if (responseArray.length() == 0) {
			return null;
		} else {
			return responseArray;
		}
	}

	private JSONArray hitQueryCoborrowerGetService(Map < String, String > inputParams, DataControllerRequest dcRequest) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_COBORROWER_GET, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject valueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		JSONArray responseArray = valueResponseJSON.getJSONArray("querycoborrower");
		if (responseArray.length() == 0) {
			return null;
		} else {
			return responseArray;
		}
	}
	
	private Result hitQueryCoborrowerCreateService(Map < String, String > inputParams, DataControllerRequest dcRequest) throws Exception {
		Result result = null;
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_COBORROWER_CREATE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		return result;
	}

	public Result hitQueryCoborrowerUpdateService(Map < String, String > inputParams, DataControllerRequest dcRequest) throws Exception {
		Result result = null;
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_COBORROWER_UPDATE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		return result;
	}

	public Result hitQueryCoborrowerDeleteService(Map < String, String > inputParams, String queryResponseId, String queryDefinitionId, DataControllerRequest dcRequest) throws Exception {
		Result result = null;
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_COBORROWER_DELETE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		return result;
	}
	private void DeleteCoApplicantDataInQuestionResponseTable(DataControllerRequest dcRequest, String queryResponseID, String queryDefinitionId) throws Exception {
		String sectionsToDelete="";
		if(queryDefinitionId.equalsIgnoreCase("VEHICLE_APPLICATION")) {
			sectionsToDelete="'VEHICLE_APPLICATION_PERSONAL_INFO_COAPPLICANT','VEHICLE_APPLICATION_INCOME_COAPPLICANT','VEHICLE_APPLICATION_CAFLAGS_GENERAL'";
		}
		else if(queryDefinitionId.equalsIgnoreCase("PERSONAL_APPLICATION")) {
			sectionsToDelete="'PERSONAL_INFO_COAPPLICANT','PERSONAL_INCOME_COAPPLICANT','PERSONAL_CAFLAGS_GENERAL'";	
		}
		Map < String, String > parametersRequiredForDeleteCoApplicant = new HashMap < String, String > ();
		parametersRequiredForDeleteCoApplicant.put("sectionsToDelete", sectionsToDelete);
		parametersRequiredForDeleteCoApplicant.put("applicationID", queryResponseID);
		Executor.invokeService(LoansServiceURLEnum.QUESTION_RESPONSE_DELETECOBBOWERDATA, (HashMap<String, String>) parametersRequiredForDeleteCoApplicant, null, dcRequest);
	}

	public Map < String, String > constructCustomerPayload(Map < String, String > paramList) {
		paramList.put("id", "KL" + CommonUtilities.getNewId().toString());
		paramList.put("Is_MemberEligible", "1");
		paramList.put("CustomerType_id", "Lead");
		paramList.put("Password", "Kony@123");
		if (paramList.containsKey("Email")) {
			paramList.put("Username", paramList.get("Email").toString());
		} else {
			paramList.put("Username", "");
		}
		return paramList;
	}

	public String getCustomer(Map < String, String > inputParams, DataControllerRequest dcRequest) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.CUSTOMER_GET, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		JSONArray customerArray = (JSONArray) ValueResponseJSON.get("customer");
		if (customerArray.length() != 0) {
			JSONObject customerObj = (JSONObject) customerArray.get(0);
			return (String) customerObj.get("id");
		}
		return null;
	}

	public String createCustomer(Map < String, String > inputParams, DataControllerRequest dcRequest) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.CUSTOMER_CREATE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		JSONArray customerArray = (JSONArray) ValueResponseJSON.get("customer");
		if (customerArray.length() != 0) {
			JSONObject customerObj = (JSONObject) customerArray.get(0);
			return (String) customerObj.get("id");
		}
		return null;
	}
	
	/*public Result hitGetApplicantInfo(Map inputParams, DataControllerRequest dcRequest) throws Exception {
		  Result result = null;
		  HashMap customHeaders = new HashMap();
		  customHeaders.put("X-Kony-Authorization", dcRequest.getHeader("X-Kony-Authorization"));

		  String Value = HTTPOperations.hitPOSTServiceAndGetResponse(
		   LoansServiceURLEnum.CUSTOMER_GETAPPLICANTINFO.getServiceURL(dcRequest),
		   (HashMap) inputParams, customHeaders, null);
		  JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		  result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);

		  return result;
	 }*/
	
	public Result hitQueryResponseUpdateService(Map inputParams, DataControllerRequest dcRequest) throws Exception {
		Result result = null;
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_RESPONSE_UPDATE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		return result;
	}
	
	private JSONArray hitQueryResponseGetService(Map < String, String > inputParams, DataControllerRequest dcRequest) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_RESPONSE_GET_, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject valueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		JSONArray responseArray = valueResponseJSON.getJSONArray("queryresponse");
		if (responseArray.length() == 0 || (responseArray.length() != 0 && responseArray.getJSONObject(0).length()==0)) 
			return null;
		else 
			return responseArray;
	}
	
	private void RemoveCoBorrowerId(String queryResponseId, DataControllerRequest dcRequest) throws Exception {
		Map < String, String > params = new HashMap < String, String > ();
		params.put("id", queryResponseId);
		params.put("CoBorrower_id", "null");
		hitQueryResponseUpdateService(params, dcRequest);
	}

}