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

@SuppressWarnings({"rawtypes", "unchecked"})
public class LoanApplicationQueryResponseUpdate implements JavaService2 {

	private static final Logger LOGGER = Logger.getLogger(LoanApplicationQueryResponseUpdate.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest,
			DataControllerResponse dcResponse) throws Exception {
		Result result = new Result();
		try {
			Map inputmap = (Map) inputArray[1];
			
			if (!inputmap.containsKey("id")) {
				Result errorResult = ErrorCodeEnum.ERR_33201.constructResultObject();
				return errorResult;
			}

			//Submit flow 
			if (inputmap.containsKey("LOSStatus") && inputmap.get("LOSStatus") != null) {
				String status = inputmap.get("LOSStatus").toString();
				if (!status.equalsIgnoreCase("Successful")) {
					Result errorResult = ErrorCodeEnum.ERR_33501.constructResultObject();
					return errorResult;
				}
				GetLoanAnswers getLoanAnswers = new GetLoanAnswers();
				JSONArray queryResponseArray = getLoanAnswers.hitQueryResponseGetService(inputmap, dcRequest, (String) inputmap.get("id"));
				if (queryResponseArray.getJSONObject(0).getString("Status_id").equalsIgnoreCase(LoansUtilitiesConstants.SUBMITTED_STATUS)) {
					Result errorResult = ErrorCodeEnum.ERR_33502.constructResultObject();
					return errorResult;
				}				
			}

			String QueryResponse_id = (String) inputmap.get("id");
			String queryDefinitionId= "";
			queryDefinitionId = (String) inputmap.get(LoansUtilitiesConstants.QUERY_DEFINITION_ID);
			JSONArray questionResponseJSON = null;
			JSONArray queryCoborrowerArray = null;		
			JSONArray customerQuerySectionStatusJSON = null;
			Dataset datasets = new Dataset();
			datasets.setId(LoansUtilitiesConstants.QUESTION_RESPONSE);
			Dataset cQSSDatasets = new Dataset();
			cQSSDatasets.setId(LoansUtilitiesConstants.CUSTOMER_QUERY_QUESTION_STATUS);
			Dataset QueryCoborrowerDataset = new Dataset();
			QueryCoborrowerDataset.setId(LoansUtilitiesConstants.QUERY_COBORROWER);
			SimulationQueryResponseCreate simulationQueryResponseCreate = new SimulationQueryResponseCreate();
			SimulationQueryResponseUpdate simulationQueryResponseUpdate = new SimulationQueryResponseUpdate();
			LoanApplicationQueryResponseCreate loanApplicationQueryResponseCreate = new LoanApplicationQueryResponseCreate();
			ArrayList <Record> records = new ArrayList <Record> ();		

			if (inputmap.containsKey(LoansUtilitiesConstants.QUESTION_RESPONSE) && inputmap.get(LoansUtilitiesConstants.QUESTION_RESPONSE) != null) {
				questionResponseJSON = new JSONArray(inputmap.get(LoansUtilitiesConstants.QUESTION_RESPONSE).toString());
				inputmap.remove(LoansUtilitiesConstants.QUESTION_RESPONSE);
			}

			if (inputmap.containsKey(LoansUtilitiesConstants.QUERY_COBORROWER) && inputmap.get(LoansUtilitiesConstants.QUERY_COBORROWER) != null) {
				queryCoborrowerArray = new JSONArray(inputmap.get(LoansUtilitiesConstants.QUERY_COBORROWER).toString());
				inputmap.remove(LoansUtilitiesConstants.QUERY_COBORROWER);
			}
	
			JSONArray abstractNamesWithId = new JSONArray();		

			if (questionResponseJSON != null) {

				JSONObject abstractNameIdMap = hitLoansAnswersForQuestionResponseIds(inputmap, dcRequest);
				if (abstractNameIdMap.has("records")) 
					abstractNamesWithId = abstractNameIdMap.getJSONArray("records");		
				JSONArray abstractNames = (JSONArray) loanApplicationQueryResponseCreate.hitQuerySectionQuestionService(queryDefinitionId, dcRequest).get(LoansUtilitiesConstants.QUERY_SECTION_STATUS);
				for (int i = 0; i < questionResponseJSON.length(); i++) {					
					JSONObject record = constructQuestionResponsePayload(questionResponseJSON.getJSONObject(i), abstractNamesWithId);				
					if (!record.has("id")) {
						//QuestionResponse Create
						record = constructQuestionReponsePayload(questionResponseJSON.getJSONObject(i), inputmap, abstractNames);
						Map paramList = simulationQueryResponseUpdate.createMapFromJsonObject(record);
						Result childResult = simulationQueryResponseCreate.hitQuestionResponseService(paramList, dcRequest);
						if(childResult.getAllDatasets().size() > 0)	{
							Record questionResponseRecord = childResult.getAllDatasets().get(0).getRecord(0);
							String abstractNameForQR = record.getString(LoansUtilitiesConstants.ABSTRACT_NAME);
							questionResponseRecord.addParam(new Param(LoansUtilitiesConstants.ABSTRACT_NAME, abstractNameForQR));
							records.add(questionResponseRecord);
						}		
					}		
					else {			
						//QuestionResponse Update 
						Map paramList = simulationQueryResponseUpdate.createMapFromJsonObject(record);
						if((paramList.get("abstractName").equals("CoApplicantFirstName") && paramList.get("ResponseValue").equals("")) || (paramList.get("abstractName").equals("CoApplicantLastName") && paramList.get("ResponseValue").equals("")) || (paramList.get("abstractName").equals("CoApplicantEmail") && paramList.get("ResponseValue").equals("")) || (paramList.get("abstractName").equals("CoApplicantMobileNumber") && paramList.get("ResponseValue").equals(""))) {  
							continue;               
						}
						if(record.has(LoansUtilitiesConstants.SOFT_DELETE_FLAG) && record.get(LoansUtilitiesConstants.SOFT_DELETE_FLAG).equals("1")) {
							simulationQueryResponseUpdate.hitQuestionResponseDeleteService(paramList, dcRequest);
						}else {
							Result childResult = simulationQueryResponseUpdate.hitQuestionResponseService(paramList, dcRequest);
							if(childResult.getAllDatasets().size() > 0)	{
								Record questionResponseRecord = childResult.getAllDatasets().get(0).getRecord(0);
								String abstractNameForQR = record.getString(LoansUtilitiesConstants.ABSTRACT_NAME);
								questionResponseRecord.addParam(new Param(LoansUtilitiesConstants.ABSTRACT_NAME, abstractNameForQR));
								records.add(questionResponseRecord);
							}						
						}
					}				
				}
			}
			datasets.addAllRecords(records);
			records.removeAll(records);
			result.addDataset(datasets);
			
			if (inputmap.containsKey(LoansUtilitiesConstants.CUSTOMER_QUERY_QUESTION_STATUS) && inputmap.get(LoansUtilitiesConstants.CUSTOMER_QUERY_QUESTION_STATUS) != null) {
				customerQuerySectionStatusJSON = new JSONArray(inputmap.get("CustomerQuerySectionStatus").toString());
				JSONArray querySectionArray = hitQuerySectionGetService(inputmap, dcRequest, queryDefinitionId);
				JSONArray cQSSGetJSON = hitCustomerQuerySectionStatusGetService(inputmap, dcRequest, (String) inputmap.get("id")); 
				inputmap.remove(LoansUtilitiesConstants.CUSTOMER_QUERY_QUESTION_STATUS);
				for (int i = 0; i < customerQuerySectionStatusJSON.length(); i++) {
					JSONObject record = customerQuerySectionStatusJSON.getJSONObject(i);
					if (!record.has("QuerySection_FriendlyName")) 
						continue;
					record = constructCQSSPayload(record, querySectionArray);
					record.put("QueryResponse_id", QueryResponse_id);
					String idForObj = getIdForQuerySectionName(cQSSGetJSON, record.getString("QuerySection_id"));
					Result childResult;
					if (idForObj == null) {
						//Id not found, so create
						record.put("id",  "KL" + CommonUtilities.getNewId().toString());
						Map paramList = simulationQueryResponseUpdate.createMapFromJsonObject(record);
						childResult = hitCustomerQuerySectionStatusCreateService(paramList, dcRequest);
					}
					else {
						//Found id, so update
						record.put("id", idForObj);
						Map paramList = simulationQueryResponseUpdate.createMapFromJsonObject(record);
						childResult = hitCustomerQuerySectionStatusUpdateService(paramList, dcRequest);
					}
					Record recordTemp = childResult.getAllDatasets().get(0).getRecord(0);
					records.add(recordTemp);					
				}
			}
			
			cQSSDatasets.addAllRecords(records);
			result.addDataset(cQSSDatasets);
			
			Result parentResult= simulationQueryResponseUpdate.hitQueryResponseService(inputmap, dcRequest);
			result.addAllDatasets(parentResult.getAllDatasets());
			updateRootTimeStamp(populateValuesForUpdatingTimeStamp(inputmap), dcRequest);

			if (queryCoborrowerArray != null)
				CoBorrowerExecution(queryCoborrowerArray, QueryResponse_id,queryDefinitionId, dcRequest);

		}
		catch(Exception e) {
			LOGGER.error("Error Occured in UpdateQueryResponseNew class", e);
			Result errorResult=ErrorCodeEnum.ERR_31000.constructResultObject();
			return errorResult;
		}
		return result;
	}
	
	public Result hitCustomerQuerySectionStatusCreateService(Map inputParams, DataControllerRequest dcRequest) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.CUSTOMER_QUERY_SECTION_STATUS_CREATE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		return CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
	}

	private String getIdForQuerySectionName(JSONArray cQSSGetJSON, String querySectionID) {
		if (cQSSGetJSON == null) 
			return null;
		for (int i = 0; i < cQSSGetJSON.length(); i++) {
			JSONObject record = cQSSGetJSON.getJSONObject(i);
			if (record.getString("QuerySection_id").equalsIgnoreCase(querySectionID)) 
				return record.getString("id");
		}	
		return null;
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
	
	private JSONArray hitCustomerQuerySectionStatusGetService(Map<String, String> inputParams, DataControllerRequest dcRequest, String queryResponseId)	throws Exception {
		inputParams.put("$filter", "QueryResponse_id eq " + queryResponseId);
		String Value = Executor.invokeService(LoansServiceURLEnum.CUSTOMER_QUERY_SECTION_STATUS_GET, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject valueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		JSONArray responseArray = valueResponseJSON.getJSONArray("customerquerysectionstatus");
		if (responseArray.length() == 0) 
			return null;
		return responseArray;
	}
	
	private Result hitCustomerQuerySectionStatusUpdateService(Map<String, String> inputParams, DataControllerRequest dcRequest) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.CUSTOMER_QUERY_SECTION_STATUS_UPDATE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		return CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
	}

	private JSONObject constructQuestionReponsePayload(JSONObject questionResponsePayload, Map < String, String > inputParams, JSONArray abstractNames) throws Exception {
		HashMap questionResponse = getQSQIdAndResponseValueFromJSONObject(questionResponsePayload, abstractNames);
		questionResponsePayload.put("id", "KL" + CommonUtilities.getNewId().toString());
		questionResponsePayload.put("QueryResponse_id", inputParams.get("id"));
		questionResponsePayload.put("QueryDefinition_id", inputParams.get(LoansUtilitiesConstants.QUERY_DEFINITION_ID));
		questionResponsePayload.put("QuerySectionQuestion_id", questionResponse.get("QSQid"));
		questionResponsePayload.put("ResponseValue", questionResponse.get("Value"));
		if(questionResponse.containsKey("OptionItem_id")) {
			questionResponsePayload.put("OptionItem_id", questionResponse.get("OptionItem_id"));
		}
		return questionResponsePayload;
	}

	private HashMap getQSQIdAndResponseValueFromJSONObject(JSONObject questionResponsePayload, JSONArray abstractNames) throws Exception {
		HashMap questionResponse = new HashMap();
		String abstractName = questionResponsePayload.getString(LoansUtilitiesConstants.ABSTRACT_NAME);
		questionResponse.put("QSQid", getQSQIdByAbstractName(abstractName, abstractNames));
		questionResponse.put("Value", questionResponsePayload.getString("ResponseValue"));
		if(questionResponsePayload.has("OptionItem_id")) {
			questionResponse.put("OptionItem_id", questionResponsePayload.getString("OptionItem_id"));
		}
		return questionResponse;
	}

	private String getQSQIdByAbstractName(String abstractName, JSONArray abstractNames) throws Exception {
		for (int i = 0; i < abstractNames.length(); i++) {
			JSONObject obj = abstractNames.getJSONObject(i);
			if (obj.getString(LoansUtilitiesConstants.ABSTRACT_NAME).equalsIgnoreCase(abstractName))
				return obj.getString("id");
		}
		return "";
	}

	private void CoBorrowerExecution(JSONArray queryCoborrowerArray, String queryResponseId,String queryDefinitionId, DataControllerRequest dcRequest) throws Exception {
		Boolean CoBorrowerRegisterd = false;	
		Map< String, String > QRList= new HashMap < String, String >();
		QRList.put(LoansUtilitiesConstants.FILTER, "id eq " + queryResponseId);
		QRList.put(LoansUtilitiesConstants.SELECT, "CoBorrower_id");
		JSONArray queryResponseJson = hitQueryResponseGetService(QRList, dcRequest);
		if(queryResponseJson!=null){
			CoBorrowerRegisterd=true;
		}
		for (int i = 0; i < queryCoborrowerArray.length(); i++) {
			JSONObject record = queryCoborrowerArray.getJSONObject(i);
			Map paramList = LoansUtilities.convertJSONtoMap(record);
			if (LoansUtilitiesConstants.INDIVIDUALLY.equals(paramList.get("CoBorrower_Type"))) {
				paramList.put(LoansUtilitiesConstants.FILTER, "QueryResponse_id eq " + queryResponseId +  " and softdeleteflag eq 0");
				JSONArray responseJson = hitQueryCoborrowerGetService(paramList, dcRequest);
				if (responseJson != null) {
					for (int j = 0; j < responseJson.length(); j++) {
						JSONObject response = responseJson.getJSONObject(j);
						paramList.put("id", response.getString("id"));
						hitQueryCoborrowerDeleteService(paramList, dcRequest,queryResponseId,queryDefinitionId);
						RemoveCoBorrowerId(queryResponseId, dcRequest);
					}
				}
				DeleteCoApplicantDataInQuestionResponseTable(dcRequest,queryResponseId,queryDefinitionId);
			} else if(!paramList.get(LoansUtilitiesConstants.COAPPLICANTEMAIL).toString().isEmpty() && !(paramList.get(LoansUtilitiesConstants.COAPPLICANTEMAIL)==null) && 
					!paramList.get(LoansUtilitiesConstants.COAPPLICANTFIRSTNAME).toString().isEmpty() && !(paramList.get(LoansUtilitiesConstants.COAPPLICANTFIRSTNAME)==null)&&   
					!paramList.get(LoansUtilitiesConstants.COAPPLICANTLASTNAME).toString().isEmpty() && !(paramList.get(LoansUtilitiesConstants.COAPPLICANTLASTNAME)==null) &&
					!paramList.get(LoansUtilitiesConstants.COAPPLICANTMOBILENUMBER).toString().isEmpty() && !(paramList.get(LoansUtilitiesConstants.COAPPLICANTMOBILENUMBER)==null) &&
					!CoBorrowerRegisterd){
				paramList.put(LoansUtilitiesConstants.FILTER, "QueryResponse_id eq " + queryResponseId +  " and softdeleteflag eq 0");
				JSONArray responseArray = hitQueryCoborrowerGetService(paramList, dcRequest);
				if (responseArray == null) {
					String id = "KL" + CommonUtilities.getNewId().toString();
					paramList.put("QueryResponse_id", queryResponseId);
					paramList.put("id", id);
					paramList.put(LoansUtilitiesConstants.COAPPLICANTINVITATIONLINKVALIDITY, CommonUtilities.convertTimetoISO8601Format(new Date()));
					paramList.put(LoansUtilitiesConstants.SOFT_DELETE_FLAG, "0");
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
						hitQueryCoborrowerDeleteService(responseList, dcRequest,queryResponseId,queryDefinitionId);
						String id = "KL" + CommonUtilities.getNewId().toString();
						paramList.put("id", id);
						paramList.put("QueryResponse_id", queryResponseId);
						paramList.put(LoansUtilitiesConstants.COAPPLICANTINVITATIONLINKVALIDITY, CommonUtilities.convertTimetoISO8601Format(new Date()));
						paramList.put(LoansUtilitiesConstants.SOFT_DELETE_FLAG, "0");
						paramList.put(LoansUtilitiesConstants.COAPPLICANTINVITATIONLINK, GenerateInviteLink(id, dcRequest));
						hitQueryCoborrowerCreateService(paramList, dcRequest);
						sendinvite(paramList, dcRequest , queryDefinitionId);  
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
	private String GenerateInviteLink(String id, DataControllerRequest dcRequest) throws Exception {
		String link = LoansServiceURLEnum.APP_URL.getServiceURL(dcRequest);
		String key = EnvironmentConfigurationsHandler.getValue(LoansUtilitiesConstants.ENCRYPTIONKEY,dcRequest);
		String encryptedLink = EncryptionUtils.encrypt(id, key);
		return link + "#" + encryptedLink;
	}

	private JSONObject constructQuestionResponsePayload(JSONObject payload, JSONArray abstractNamesWithId) {
		String id = getIdforAbstractName(payload.getString("abstractName"), abstractNamesWithId);
		if (id != null)
			payload.put("id", id);
		return payload;
	}

	private String getIdforAbstractName(String abstractName, JSONArray abstractNamesWithId) {	
		JSONObject obj = null;
		for (int i = 0; i < abstractNamesWithId.length(); i++) {
			obj = (JSONObject) abstractNamesWithId.get(i);
			if (obj.getString("Question_FriendlyName").equalsIgnoreCase(abstractName)) {
				return obj.getString("QuestionResponse_id");				
			}
		}
		return null;
	}
	
	private JSONObject hitLoansAnswersForQuestionResponseIds(Map inputParams, DataControllerRequest dcRequest) throws Exception {
		inputParams.put("QueryResponseID", inputParams.get("id"));
		String Value = Executor.invokeService(LoansServiceURLEnum.LOAN_ANSWERS_GET, (HashMap<String, String>) inputParams, null, dcRequest);
		return CommonUtilities.getStringAsJSONObject(Value);
	}

	private JSONArray hitQueryCoborrowerGetService(Map<String, String> inputParams, DataControllerRequest dcRequest) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_COBORROWER_GET, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject valueResponseJSON= CommonUtilities.getStringAsJSONObject(Value);
		JSONArray responseArray = valueResponseJSON.getJSONArray("querycoborrower");
		if(responseArray.length() == 0){
			return null;
		}else{
			return responseArray;
		}
	}

	private Result updateRootTimeStamp(Map<String, String> inputParams, DataControllerRequest dcRequest) {
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_RESPONSE_UPDATE_TIMESTAMP, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		return CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
	}
	
	private Map<String, String> populateValuesForUpdatingTimeStamp(Map<String, String> inputParams) {
		inputParams.put("tablename", "queryresponse");
		inputParams.put("primarykeyfield", "id");
		inputParams.put("primarykeyvalue", inputParams.get("id"));
		inputParams.put("fieldname", "lastmodifiedts");
		inputParams.put("fieldvalue", "now()");
		return inputParams;
	}
	
	private Result hitQueryCoborrowerCreateService(Map<String, String> inputParams, DataControllerRequest dcRequest) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_COBORROWER_CREATE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		return CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
	}

	private Result hitQueryCoborrowerDeleteService(Map<String, String> inputParams, DataControllerRequest dcRequest, String queryResponseId,String queryDefinitionId ) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_COBORROWER_DELETE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		return CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
	}
	private void DeleteCoApplicantDataInQuestionResponseTable( DataControllerRequest dcRequest, String queryResponseID, String queryDefinitionId) throws Exception {
		try {
			String val="";
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
			val=Executor.invokeService(LoansServiceURLEnum.QUESTION_RESPONSE_DELETECOBBOWERDATA, (HashMap<String, String>) parametersRequiredForDeleteCoApplicant, null, dcRequest);

		}
		catch(Exception e) {
			LOGGER.debug("coapplicant error"+e);
		}
	}
	public Map<String, String> constructCustomerPayload(Map<String,String> paramList) throws Exception {		
		paramList.put("id",  "KL" + CommonUtilities.getNewId().toString());
		paramList.put("Is_MemberEligible", "1");
		paramList.put("CustomerType_id", "Lead");
		paramList.put("Password", "Kony@123");
		if (paramList.containsKey("Email")) {
			paramList.put("Username", paramList.get("Email").toString());
		}
		else {
			paramList.put("Username", "");
		}
		return paramList;		
	}

	public String getCustomer(Map<String,String> inputParams, DataControllerRequest dcRequest) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.CUSTOMER_GET, (HashMap<String, String>) inputParams, null, dcRequest);	
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		JSONArray customerArray =  (JSONArray) ValueResponseJSON.get("customer");
		if (customerArray.length()!=0) {
			JSONObject customerObj = (JSONObject) customerArray.get(0);
			return (String) customerObj.get("id");
		}
		return null;
	}

	public String createCustomer(Map<String,String> inputParams, DataControllerRequest dcRequest) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.CUSTOMER_CREATE, (HashMap<String, String>) inputParams, null, dcRequest);	
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		JSONArray customerArray =  (JSONArray) ValueResponseJSON.get("customer");
		if (customerArray.length()!=0) {
			JSONObject customerObj = (JSONObject) customerArray.get(0);
			return customerObj.get("id").toString();
		}
		return null;
	}

	public Result hitQueryResponseUpdateService(Map inputParams, DataControllerRequest dcRequest) throws Exception {
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_RESPONSE_UPDATE, (HashMap<String, String>) inputParams, null, dcRequest);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		return CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
	}

	private JSONArray hitQueryResponseGetService(Map < String, String > inputParams, DataControllerRequest dcRequest) throws Exception {		
		String Value = Executor.invokeService(LoansServiceURLEnum.QUERY_RESPONSE_GET_, (HashMap<String, String>) inputParams, null, dcRequest);		
		JSONObject valueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		JSONArray responseArray = valueResponseJSON.getJSONArray("queryresponse");
		if (responseArray.length() == 0 || (responseArray.length() != 0 && responseArray.getJSONObject(0).length()==0)) 
			return null;
		return responseArray;
	}

	private void RemoveCoBorrowerId(String queryResponseId, DataControllerRequest dcRequest) throws Exception {
		Map < String, String > params = new HashMap < String, String > ();
		params.put("id", queryResponseId);
		params.put("CoBorrower_id", "null");
		hitQueryResponseUpdateService(params, dcRequest);
	}
}