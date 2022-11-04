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
import com.kony.adminconsole.loans.utils.DpUtilities;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

/**
 * @author KH2302 Bhowmik
 *
 */

public class DpFailureCase extends AbstractHitDp implements JavaService2 {

	/**
	 * Logger logger used to log the debug/info/error statements.
	 */
	private static final Logger LOGGER = Logger.getLogger(DpFailureCase.class);
	/**
	 * FailureJob is configuration driven containing number of iterations of
	 * failure job.
	 */
	protected int failureJob = Integer.parseInt(LOANSERVICEURLPROP.getString("FailureJobCounter"));

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
	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest, DataControllerResponse dcResponse) throws LoansException {
		Result result = new Result();
		DpUtilities dputilities = new DpUtilities();
		int failureCounter = 0;
		JSONObject finalResponse = new JSONObject();
		final String jobIdFinal = "FailureJob-" + CommonUtilities.getNewId().toString();
		do {
			Map<String, Integer> response = new HashMap<String, Integer>();
			int batchStartId = 0;
			int batchEndId = 0;
			final String jobId = jobIdFinal + "-" + failureCounter;
			JSONObject responsejson = new JSONObject();
			int batch = 0;
			try {
				Map inputmap = (Map) inputArray[1];
				JSONObject inputJson = dputilities.mapToJsonObject(inputmap);
				if (!inputJson.optString("id_list").isEmpty() || !inputJson.optString("job_id").isEmpty()) {
					failureJob = 1;
				}
				inputJson.put("top", CUSTOMER_BATCHSIZE);
				while (true) {
					inputJson.put("skip", batch);
					String url = LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("DecisionFailureData_get");
					String responseString = HTTPOperations.hitPOSTServiceAndGetResponse(url, inputJson, null, null);
					LOGGER.debug("DecisionFailureDataResponse: " + responseString);
					JSONObject customerdlpdataresponse = new JSONObject(responseString);
					if (0 == customerdlpdataresponse.getInt("opstatus")) {
						JSONArray customerdlpdataarray = new JSONArray(customerdlpdataresponse.get("records").toString());
						if (customerdlpdataarray.length() != 0) {
							updateFailureRecordsJobId(dcRequest, customerdlpdataresponse, jobId);
							customerdlpdataresponse = hitDpFailure(dcRequest, customerdlpdataarray, methodID, inputmap, jobId);
							if (batchStartId == 0 || customerdlpdataresponse.getJSONArray("records").getJSONObject(0).getInt("id") < batchStartId) {
								batchStartId = customerdlpdataresponse.getJSONArray("records").getJSONObject(0).getInt("id");
							}
							if (batchEndId == 0 || customerdlpdataresponse.getJSONArray("records").getJSONObject(customerdlpdataresponse.getJSONArray("records").length() - 1).getInt("id") > batchEndId) {
								batchEndId = customerdlpdataresponse.getJSONArray("records").getJSONObject(customerdlpdataresponse.getJSONArray("records").length() - 1).getInt("id");
							}
						} else {
							break;
						}
					} else {
						LOGGER.debug(customerdlpdataresponse.get("errmsg").toString());
						throw new LoansException(ErrorCodeEnum.ERR_35201);
					}
					batch = batch + CUSTOMER_BATCHSIZE;
				}
				commitFailure(dcRequest, batchStartId, batchEndId, methodID, jobId).forEach((key, value) -> response.put(key, response.getOrDefault(key, 0) + value));
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
			finalResponse.put(jobId, responsejson);
			failureCounter = failureCounter + 1;
		} while (failureCounter != failureJob);
		LOGGER.debug("Response: " + finalResponse);
		result = CommonUtilities.getResultObjectFromJSONObject(finalResponse);
		return result;
	}
}
