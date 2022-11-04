package com.kony.adminconsole.loans.postprocessor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.omg.CORBA.Request;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.preprocessor.AddUserIdenitificationToServicePreprocessor;
import com.kony.adminconsole.loans.preprocessor.GetClaimsTokenAPIIdentityService;
import com.kony.adminconsole.loans.utils.EnvironmentConfigurationsHandler;
import com.kony.adminconsole.loans.utils.Executor;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;

public class SubmitLoanPostprocessor implements DataPostProcessor2 {

	@Override
	public Object execute(Result result, DataControllerRequest dcrequest, DataControllerResponse dcresponse)
			throws Exception {
		// TODO Auto-generated method stub

		final Logger log = Logger.getLogger(SubmitLoanPostprocessor.class);
		JSONObject contentPayload = new JSONObject();
		JSONObject ValueResponseJSON = new JSONObject();
		JSONArray inarray = new JSONArray();
		Date submissiondate = new Date();
		String Uname = null;

		Dataset dataset = result.getDatasetById("QuestionResponse");
		int size = dataset.getAllRecords().size();

		JSONObject inputParams1 = new JSONObject();
		inputParams1.put("Customer_id", result.getParamByName("Customer_id").getValue().toString());

		try {
			String serviceURL = LoansServiceURLEnum.CUSTOMER_GETINFO.getServiceURL(dcrequest);
			String Value = HTTPOperations.hitPOSTServiceAndGetResponse(serviceURL, inputParams1, null, null);
			ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONArray jarray = new JSONArray();
		jarray = (JSONArray) ValueResponseJSON.getJSONArray("ApplicantInfo");

		JSONObject obj = (JSONObject) jarray.getJSONObject(0);
		if (obj.has("Email")) {
			Uname = obj.get("Email").toString();
		}

		if (Uname == null) {
			log.error("Unable to find User Name for this session");
		}
		for (int i = 0; i < size; i++) {
			Record rcd = dataset.getRecord(i);
			if (rcd.getParamByName("abstractName").getValue().toString().equals("Email")) {
				if (Uname != null) {
					if (rcd.getParamByName("ResponseValue").getValue().toString().equalsIgnoreCase(Uname)) {
						result.addParam(
								new Param("sendToEmails", rcd.getParamByName("ResponseValue").getValue().toString()));
					}

					result.addParam(new Param("sendToEmails",
							rcd.getParamByName("ResponseValue").getValue().toString() + "," + Uname));
				} else {
					result.addParam(
							new Param("sendToEmails", rcd.getParamByName("ResponseValue").getValue().toString()));
				}

				result.addParam(new Param("email", rcd.getParamByName("ResponseValue").getValue().toString()));
			}
			if (rcd.getParamByName("abstractName").getValue().toString().equals("FirstName")) {
				contentPayload.put("APPLICANT_NAME", rcd.getParamByName("ResponseValue").getValue().toString());
				result.addParam(new Param("firstName", rcd.getParamByName("ResponseValue").getValue().toString()));
			}
			if (rcd.getParamByName("abstractName").getValue().toString().equals("LastName")) {
				result.addParam(new Param("lastName", rcd.getParamByName("ResponseValue").getValue().toString()));
			}
		}

		HashMap<String, String> inputParams = new HashMap<String, String>();

		inputParams.put("$filter", "id eq " + result.getParamByName("LoanProduct_id").getValue().toString());

		try {
			String Value = Executor.invokeService(LoansServiceURLEnum.LOANPRODUCT_GET,
					(HashMap<String, String>) inputParams, null, dcrequest);
			ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
			inarray = ValueResponseJSON.getJSONArray("loanproduct");
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject jsons = inarray.getJSONObject(0);

		contentPayload.put("TemplateKey", "APPLICATION_SUCCESSFUL_SUBMISSION_EMAIL_TEMPLATE");
		contentPayload.put("BANK_CU_NAME", "KonyDBX");
		contentPayload.put("APPLICATION_NUMBER", result.getParamByName("id").getValue().toString());
		contentPayload.put("CONTACT_NUMBER", "(123)-456-7890");
		contentPayload.put("DATE_OF_SUBMISSION", new SimpleDateFormat("MM-dd-yyyy").format(submissiondate));
		contentPayload.put("LOAN_TYPE", jsons.get("Name").toString());

		result.addParam(new Param("senderEmail", "logeswaran.rathinasamy@kony.com"));
		result.addParam(new Param("copyToEmails", ""));
		result.addParam(new Param("bccToEmails", ""));
		result.addParam(new Param("senderName", "KonyDBXUser"));
		result.addParam(
				new Param("subject", "Your Application for " + jsons.get("Name").toString() + " is submitted."));
		result.addParam(new Param("content", contentPayload.toString()));
		result.addParam(new Param("country", "United States"));
		result.addParam(new Param("state", "Nebraska"));

		// TODO Auto-generated method stub
		return result;
	}

}