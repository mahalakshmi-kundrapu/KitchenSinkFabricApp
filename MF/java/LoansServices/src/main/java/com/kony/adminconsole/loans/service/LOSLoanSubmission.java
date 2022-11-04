package com.kony.adminconsole.loans.service;

import java.util.Map;

import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class LOSLoanSubmission implements JavaService2 {

	@SuppressWarnings({ "unchecked" })
	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest,
			DataControllerResponse dcResponse) throws Exception {
		Result result = new Result();

		// Modify payload modifyPayloadForLISfor LOS
		Map<String, String> LOSpayload = (Map<String, String>) inputArray[1];

		// Hit LOS
		String status = hitLOSforSubmission(LOSpayload);

		// Modify payload for LIS
		JSONObject LISpayload = new JSONObject((Map<String, String>) inputArray[1]);
		LISpayload = modifyPayloadForLIS(LISpayload);

		Dataset dataset = new Dataset();
		dataset.setId("LISSubmitPayload");

		result = CommonUtilities.getResultObjectFromJSONObject(LISpayload);

		// Set Status of LOS submit operation
		result.addParam(new Param("LOSStatus", status));

		return result;
	}

	public JSONObject modifyPayloadForLIS(JSONObject LISPayload) {
		if (LISPayload.has("CustomerQuerySectionStatus"))
			LISPayload.remove("CustomerQuerySectionStatus");
		if (LISPayload.has("QuestionResponse"))
			LISPayload.remove("QuestionResponse");
		return LISPayload;
	}

	public String hitLOSforSubmission(Map<String, String> LOSPayload) {
		return "Successful";
	}

}