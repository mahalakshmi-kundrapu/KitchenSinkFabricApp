package com.kony.adminconsole.loans.service.dpservices;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.errorhandling.LoansException;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

/**
 * @author KH2302 Bhowmik
 *
 */

public class DpExecution extends AbstractHitDp implements JavaService2 {

	/**
	 * Logger logger used to log the debug/info/error statements.
	 */
	private static final Logger LOGGER = Logger.getLogger(DpExecution.class);

	/**
	 * 
	 * This method is executed automatically by middleware as soon as the
	 * request is sent by the user.
	 * 
	 * @param methodID
	 *            Name of operation that is being executed.
	 * @param inputArray
	 *            request properties such as SESSIONID,Preprocessor name,Method
	 *            name,userAgent. The request payload. Service execution time.
	 * @param dcRequest
	 *            Session Details,RemoteAdress,HeaderMap,files
	 * @param dcResponse
	 *            Contains Charset Encoding,devicecookies,deviceheaders
	 * @return result Contains Object returned to the console after processing
	 *         the request.
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse dcResponse) throws LoansException {
		Result result = new Result();
		Map inputmap = (Map) inputArray[1];
		Map<String, Integer> response = new HashMap<String, Integer>();
		final String jobId;
		int batch = 0;
		int completedRecordCounter = 0;
		JSONObject responsejson = new JSONObject();
		try {
			if (inputmap.get("job_id") == null) {
				jobId = "Job-" + CommonUtilities.getNewId().toString();
				inputmap.put("job_id", jobId);
				inputmap.put("completedRecordCounter", String.valueOf(completedRecordCounter));
				inputArray[1] = inputmap;
				String url = LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("DecisionBatchRun_create");
				LOGGER.debug("DecisionBatchRunResponse: " + HTTPOperations.hitPOSTServiceAndGetResponse(url,(HashMap<String, String>) inputmap, null, null));
			} else {
				jobId = inputmap.get("job_id").toString();
				batch = getAlreadyCompletedRecords(dcRequest, jobId);
				completedRecordCounter = batch;
			}
			String url = LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("Customerdpdata_get");
			while (true) {
				String filter = "?$orderby=createdts,id&$top=" + CUSTOMER_BATCHSIZE + "&$skip=" + (batch);
				String responseString = HTTPOperations.hitPOSTServiceAndGetResponse(url + filter,(HashMap<String, String>) inputmap, null, null);
				JSONObject customerdlpdataresponse = new JSONObject(responseString);
				if (0 == customerdlpdataresponse.getInt("opstatus")) {
					JSONArray customerdlpdataarray = new JSONArray(customerdlpdataresponse.get("customerdpdata").toString());
					completedRecordCounter = completedRecordCounter + customerdlpdataarray.length();
					if (customerdlpdataarray.length() == 0) {
						inputmap.put("completedRecordCounter", String.valueOf(completedRecordCounter));
						inputmap.put("successflag", "1");
						LOGGER.debug("DecisionBatchRunResponse: " + HTTPOperations.hitPOSTServiceAndGetResponse(LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("DecisionBatchRun_update"),(HashMap<String, String>) inputmap, null, null));
						break;
					}
					customerdlpdataresponse = hitDp(dcRequest, customerdlpdataarray, methodID, inputmap, jobId);
					commit(dcRequest, customerdlpdataresponse, methodID, jobId).forEach((key, value) -> response.put(key, response.getOrDefault(key, 0) + value));
					inputmap.put("completedRecordCounter", String.valueOf(completedRecordCounter));
					LOGGER.debug("DecisionBatchRunResponse: " + HTTPOperations.hitPOSTServiceAndGetResponse(LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("DecisionBatchRun_update"),(HashMap<String, String>) inputmap, null, null));
				} else {
					LOGGER.debug(customerdlpdataresponse.get("errmsg").toString());
					throw new LoansException(ErrorCodeEnum.ERR_35201);
				}
				batch = batch + CUSTOMER_BATCHSIZE;
			}
		} catch (LoansException exception) {
			result=exception.updateResultObject(result);
			return result;
		} catch (Exception e) {
			LOGGER.debug(e);
			LoansException unknownException=new LoansException(ErrorCodeEnum.ERR_31000);
			result=unknownException.updateResultObject(result);
			return result;
		}
		JSONObject oreResponse = new JSONObject();
		oreResponse.put("recordCount", response.getOrDefault("OREResponseRecordCount", 0));
		oreResponse.put("successCount", response.getOrDefault("OREResponseSuccessCount", 0));
		oreResponse.put("failureCount", response.getOrDefault("OREResponseFailureCount", 0));
		JSONObject commitResponse = new JSONObject();
		commitResponse.put("recordCount", response.getOrDefault("CommitResponseRecordCount", 0));
		commitResponse.put("successCount", response.getOrDefault("CommitResponseSuccessCount", 0));
		commitResponse.put("failureCount", response.getOrDefault("CommitResponseFailureCount", 0));
		responsejson.put("OREResponse", oreResponse);
		responsejson.put("CommitResponse ", commitResponse);
		JSONObject finalResponse = new JSONObject();
		finalResponse.put(jobId, responsejson);
		LOGGER.debug("Response: " + finalResponse);
		result = CommonUtilities.getResultObjectFromJSONObject(finalResponse);
		return result;
	}
}
