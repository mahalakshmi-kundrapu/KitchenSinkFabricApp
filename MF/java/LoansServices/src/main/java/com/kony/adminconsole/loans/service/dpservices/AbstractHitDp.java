package com.kony.adminconsole.loans.service.dpservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.errorhandling.LoansException;
import com.kony.adminconsole.loans.utils.DpUtilities;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * @author KH2302 Bhowmik
 *
 */

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AbstractHitDp {

	/**
	 * Logger logger used to log the debug/info/error statements
	 */
	private static final Logger LOGGER = Logger.getLogger(AbstractHitDp.class);

	/**
	 * This field holds the ResourceBundle instance for
	 * LoansServiceURLCollection.properties which contains all the corresponding
	 * configurations and urls.
	 */
	protected static final ResourceBundle LOANSERVICEURLPROP = ResourceBundle.getBundle("LoansServiceURLCollection");

	/**
	 * Customer_batchsize contains number of records to be fetched from Core in
	 * one service call.
	 */
	protected static final int CUSTOMER_BATCHSIZE = Integer.parseInt(LOANSERVICEURLPROP.getString("Customer_batch"));

	/**
	 * maxThread contains value for maximum thread Executor can initiate.
	 */
	protected static final int MAXTHREADS = Integer.parseInt(LOANSERVICEURLPROP.getString("MaxThreadAllowed"));

	/**
	 * ORE_batchSize contains value for number of records to be sent to ORE in
	 * single batch and single thread.
	 */
	protected static final double ORE_BATCHSIZE = Integer.parseInt(LOANSERVICEURLPROP.getString("ORE_batch"));

	/**
	 * ExecutorService is used for thread-handling on the Java platform and
	 * provides methods to manage the progress-tracking and termination of
	 * asynchronous tasks
	 */
	protected final ExecutorService executor = Executors.newWorkStealingPool(MAXTHREADS);

	/**
	 * 
	 * This method is accept set of records as JSONArray and start asynchronous
	 * service call to ORE with batches of ORE_batchSize and returns the
	 * response given by ORE.
	 * 
	 * @param dcRequest
	 *            contains Session Details,RemoteAdress,HeaderMap,files used to
	 *            fetch base URL.
	 * @param dataArray
	 *            Contains array of records fetched from backend.
	 * @param methodID
	 *            Name of operation that is being executed.
	 * @param inputmap
	 *            Contains request payload.
	 * @param jobId
	 *            Contains UUID generated jobId or id given in request payload.
	 * @return batchCommitJSON Returns the response given by ORE after saving it
	 *         to backend.
	 * @throws Exception
	 */
	protected JSONObject hitDp(DataControllerRequest dcRequest, JSONArray dataArray, String methodID, Map inputmap, String jobId) throws Exception {
		DpUtilities dputilities = new DpUtilities();
		List<Future<JSONObject>> list = new ArrayList<Future<JSONObject>>();
		double length = dataArray.length();
		double batchStart = 0;
		double batchEnd = ORE_BATCHSIZE;
		for (int counter = 0; counter < Math.ceil(length / ORE_BATCHSIZE); counter++) {
			if (batchEnd > length) {
				batchEnd = batchStart + (length % ORE_BATCHSIZE);
			}
			Map<String, String> tempmap = new HashMap<String, String>();
			for (int i = (int) batchStart; i < batchEnd; i++) {
				JSONObject customerdlpdata = (JSONObject) dataArray.get(i);
				customerdlpdata.put("packageName", inputmap.getOrDefault("packageName", null));
				tempmap = dputilities.appendJSONtoMap(tempmap, customerdlpdata);
			}
			Future<JSONObject> future = executor.submit(new HitDpCallable(LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString(methodID), tempmap));
			list.add(future);
			batchStart = batchStart + ORE_BATCHSIZE;
			batchEnd = batchStart + ORE_BATCHSIZE;
		}
		JSONObject batchCommitJSON = saveToDatabase(LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("DecisionResultObject_Url"), list, jobId);
		LOGGER.debug(jobId + "-OREResponse: " + batchCommitJSON);
		return batchCommitJSON;
	}

	/**
	 * 
	 * This method is used to accept set of records from failure job and start
	 * asynchronous service call to ORE with batches of ORE_batchSize and
	 * returns the response given by ORE.
	 * 
	 * @param dcRequest
	 *            contains Session Details,RemoteAdress,HeaderMap,files used to
	 *            fetch base URL.
	 * @param dataArray
	 *            Contains array of records fetched from backend.
	 * @param methodID
	 *            Name of operation that is being executed.
	 * @param inputmap
	 *            Contains request payload.
	 * @param jobId
	 *            Contains UUID generated jobId or id given in request payload.
	 * @return batchCommitJSON Returns the response given by ORE after saving it
	 *         to backend.
	 * @throws Exception
	 */
	protected JSONObject hitDpFailure(DataControllerRequest dcRequest, JSONArray dataArray, String methodID, Map inputmap, String jobId) throws Exception {
		DpUtilities dputilities = new DpUtilities();
		List<Future<JSONObject>> list = new ArrayList<Future<JSONObject>>();
		double length = dataArray.length();
		double batchStart = 0;
		double batchEnd = ORE_BATCHSIZE;
		for (int counter = 0; counter < Math.ceil(length / ORE_BATCHSIZE); counter++) {
			if (batchEnd > length) {
				batchEnd = batchStart + (length % ORE_BATCHSIZE);
			}
			Map<String, String> scoreMap = new HashMap<String, String>();
			Map<String, String> packageMap = new HashMap<String, String>();
			for (int i = (int) batchStart; i < batchEnd; i++) {
				JSONObject customerdlpdata = (JSONObject) dataArray.get(i);
				methodID = customerdlpdata.get("decision_id").toString();
				JSONObject tempobj = new JSONObject(customerdlpdata.optString("exception"));
				inputmap.put("packageName", tempobj.optString("packageName"));
				if ("DetermineLoanPreQualificationScore".equals(methodID)) {
					scoreMap = dputilities.appendJSONtoMap(scoreMap, customerdlpdata);
				}
				if ("DetermineLoanPreQualificationPackage".equals(methodID)) {
					customerdlpdata.put("packageName", inputmap.getOrDefault("packageName", null));
					packageMap = dputilities.appendJSONtoMap(packageMap, customerdlpdata);
				}
			}
			if (!scoreMap.isEmpty()) {
				Future<JSONObject> future = executor.submit(new HitDpCallable(LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("DetermineLoanPreQualificationScore"), scoreMap));
				list.add(future);
			}
			if (!packageMap.isEmpty()) {
				Future<JSONObject> future = executor.submit(new HitDpCallable(LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("DetermineLoanPreQualificationPackage"), packageMap));
				list.add(future);
			}
			batchStart = batchStart + ORE_BATCHSIZE;
			batchEnd = batchStart + ORE_BATCHSIZE;
		}
		JSONObject batchCommitJSON = saveToDatabase(LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("DecisionResultObject_Url"), list, jobId);
		LOGGER.debug(jobId + "-OREResponse: " + batchCommitJSON);
		return batchCommitJSON;
	}

	/**
	 * 
	 * This method is used to save ORE response in backend
	 * 
	 * @param baseurl
	 *            Contains baseurl of backend where all response is saved.
	 * @param list
	 *            Contains List<Future<JSONObject>> of response from ORE to be
	 *            saved when executed threads are completed.
	 * @param jobId
	 *            Contains UUID generated jobId or id given in request payload.
	 * @return response Returns the response given by ORE after saving it to
	 *         backend.
	 * @throws Exception
	 */
	protected JSONObject saveToDatabase(String baseurl, List<Future<JSONObject>> list, String jobId) throws Exception {
		JSONObject batchCommitJSON = new JSONObject();
		JSONArray batchCommitArray = new JSONArray();
		for (Future<JSONObject> fut : list) {
			JSONArray decisionresults = new JSONArray(fut.get().get("decisionresult").toString());
			for (int i = 0; i < decisionresults.length(); i++) {
				JSONObject decisionresult = (JSONObject) decisionresults.get(i);
				decisionresult.put("job_id", jobId);
				if (decisionresult.has("exception"))
					decisionresult.put("exception", decisionresult.optString("exception"));
				batchCommitArray.put(decisionresult);
			}
		}
		batchCommitJSON.put("records", batchCommitArray);
		JSONObject response = new JSONObject(HTTPOperations.hitPOSTServiceAndGetResponse(baseurl, batchCommitJSON, null, null));
		response = (JSONObject) response.getJSONArray("objects").get(0);
		LOGGER.debug("DecisionResultObjectResponse: " + response);
		return response;
	}

	/**
	 * 
	 * This method is takes ORE Response as an input and formats/calculate the
	 * response in required manner. Then makes service call to backend and
	 * updates results fetched from ORE.
	 * 
	 * @param dcRequest
	 *            contains Session Details,RemoteAdress,HeaderMap,files used to
	 *            fetch base URL.
	 * @param dlpResponse
	 *            Contains array of records fetched from backend.
	 * @param methodID
	 *            Name of operation that is being executed.
	 * @param inputmap
	 *            Contains request payload.
	 * @param jobId
	 *            Contains UUID generated jobId or id given in request payload.
	 * @return Map Returns map with success and failure counts of ORE and Commit
	 * @throws Exception
	 */
	protected Map<String, Integer> commit(DataControllerRequest dcRequest, JSONObject dlpResponse, String methodID, String jobId) throws Exception {
		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			String endpointResponse = null;
			JSONObject batchSuccessCommitJSON = new JSONObject();
			JSONObject batchFailureCommitJSON = new JSONObject();
			JSONArray batchCommitArray = new JSONArray();
			JSONArray batchFailureArray = new JSONArray();
			JSONArray decisionresults = new JSONArray(dlpResponse.get("records").toString());
			map.compute("OREResponseRecordCount", (key, value) -> decisionresults.length());
			switch (methodID) {
			case "DetermineLoanPreQualificationScore":
				for (int i = 0; i < decisionresults.length(); i++) {
					JSONObject decisionresult = (JSONObject) decisionresults.get(i);
					if (decisionresult.has("exception")) {
						decisionresult.put("job_id", jobId);
						batchFailureArray.put(decisionresult);
						map.compute("OREResponseFailureCount", (key, value) -> value == null ? 1 : value + 1);
					} else {
						JSONObject tempJson = new JSONObject();
						tempJson.put("id", decisionresult.get("baseAttributeValue").toString());
						tempJson.put(decisionresult.get("resultAttributeName").toString(), decisionresult.get("resultAttributeValue").toString());
						batchCommitArray.put(tempJson);
						map.compute("OREResponseSuccessCount", (key, value) -> value == null ? 1 : value + 1);
					}
				}
				batchSuccessCommitJSON.put("records", batchCommitArray);
				endpointResponse = HTTPOperations.hitPUTServiceAndGetResponse(LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("CustomerDpDataObject_Url"), batchSuccessCommitJSON, null, null);
				LOGGER.debug(jobId + "-CommitResponse: " + endpointResponse);
				break;

			case "DetermineLoanPreQualificationPackage":
				for (int i = 0; i < decisionresults.length(); i++) {
					JSONObject decisionresult = (JSONObject) decisionresults.get(i);
					if (decisionresult.has("exception")) {
						decisionresult.put("job_id", jobId);
						batchFailureArray.put(decisionresult);
						map.compute("OREResponseFailureCount", (key, value) -> value == null ? 1 : value + 1);
					} else {
						if (!"No Package".equals(decisionresult.get("resultAttributeValue").toString())) {
							JSONObject tempJson = new JSONObject();
							tempJson.put("id", decisionresult.get("baseAttributeValue").toString() + "-" + decisionresult.get("resultAttributeValue").toString());
							tempJson.put(decisionresult.get("baseAttributeName").toString(), decisionresult.get("baseAttributeValue").toString());
							tempJson.put(decisionresult.get("resultAttributeName").toString(), decisionresult.get("resultAttributeValue").toString());
							batchCommitArray.put(tempJson);
						}
						map.compute("OREResponseSuccessCount", (key, value) -> value == null ? 1 : value + 1);
					}
				}
				batchSuccessCommitJSON.put("records", batchCommitArray);
				endpointResponse = HTTPOperations.hitPOSTServiceAndGetResponse(LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("CustomerPrequalifyPackageObject_Url"), batchSuccessCommitJSON, null, null);
				LOGGER.debug(jobId + "-CommitResponse: " + endpointResponse);
				break;
			}
			batchFailureCommitJSON.put("records", batchFailureArray);
			LOGGER.debug("DecisionFailureObjectResponse: " + HTTPOperations.hitPOSTServiceAndGetResponse(LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("DecisionFailureObject_Url"), batchFailureCommitJSON, null, null));
			JSONObject commitResponse = new JSONObject(endpointResponse);
			JSONObject metadata = (JSONObject) commitResponse.get("_metadata");
			map.compute("CommitResponseRecordCount", (key, value) -> metadata.optInt("recordCount"));
			map.compute("CommitResponseSuccessCount", (key, value) -> metadata.optInt("successCount"));
			map.compute("CommitResponseFailureCount", (key, value) -> metadata.optInt("failureCount"));
		} catch (Exception e) {
			LOGGER.debug(e);
		}
		return map;
	}

	/**
	 * 
	 * This method is used failure service which makes call to ORE results at
	 * backend and formats/calculate the response in required manner. Then makes
	 * service call to backend and updates results fetched from ORE.
	 * 
	 * @param dcRequest
	 *            contains Session Details,RemoteAdress,HeaderMap,files used to
	 *            fetch base URL.
	 * @param batchStartId
	 *            Contains start id for executed ORE results.
	 * @param batchEndId
	 *            Contains end id for executed ORE results.
	 * @param methodID
	 *            Name of operation that is being executed.
	 * @param inputmap
	 *            Contains request payload.
	 * @param jobId
	 *            Contains UUID generated jobId or id given in request payload.
	 * @return Map Returns map with success and failure counts of ORE and Commit
	 * @throws Exception
	 */
	protected Map<String, Integer> commitFailure(DataControllerRequest dcRequest, int batchStartId, int batchEndId, String methodID, String jobId) throws Exception {
		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			int batch = 0;
			String url = LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("Decisionresult_get");
			while (true) {
				JSONObject batchSuccessCommitScoreJSON = new JSONObject();
				JSONObject batchSuccessCommitPackageJSON = new JSONObject();
				JSONObject batchFailureCommitJSON = new JSONObject();
				JSONArray batchFailureArray = new JSONArray();
				JSONArray batchCommitScoreArray = new JSONArray();
				JSONArray batchCommitPackageArray = new JSONArray();
				String query = "?$filter=id%20ge%20" + batchStartId + "%20and%20id%20le%20" + batchEndId + "&$top=" + CUSTOMER_BATCHSIZE + "&$skip=" + batch;
				HashMap inputmap = null;
				JSONObject dlpResponse = new JSONObject(HTTPOperations.hitPOSTServiceAndGetResponse(url + query, inputmap, null, null));
				JSONArray decisionresults = new JSONArray(dlpResponse.get("decisionresult").toString());
				if (decisionresults.length() == 0) {
					break;
				}
				map.compute("OREResponseRecordCount", (key, value) -> value == null ? decisionresults.length() : value + decisionresults.length());
				for (int i = 0; i < decisionresults.length(); i++) {
					JSONObject decisionresult = (JSONObject) decisionresults.get(i);
					methodID = decisionresult.optString("decision_id");
					switch (methodID) {
					case "DetermineLoanPreQualificationScore":
						if (decisionresult.has("exception")) {
							decisionresult.put("job_id", jobId);
							batchFailureArray.put(decisionresult);
							map.compute("OREResponseFailureCount", (key, value) -> value == null ? 1 : value + 1);
						} else {
							JSONObject tempJson = new JSONObject();
							tempJson.put("id", decisionresult.get("baseAttributeValue").toString());
							tempJson.put(decisionresult.get("resultAttributeName").toString(), decisionresult.get("resultAttributeValue").toString());
							batchCommitScoreArray.put(tempJson);
							map.compute("OREResponseSuccessCount", (key, value) -> value == null ? 1 : value + 1);
						}
						break;
					case "DetermineLoanPreQualificationPackage":
						if (decisionresult.has("exception")) {
							decisionresult.put("job_id", jobId);
							batchFailureArray.put(decisionresult);
							map.compute("OREResponseFailureCount", (key, value) -> value == null ? 1 : value + 1);
						} else {
							if (!"No Package".equals(decisionresult.get("resultAttributeValue").toString())) {
								JSONObject tempJson = new JSONObject();
								tempJson.put("id", decisionresult.get("baseAttributeValue").toString() + "-" + decisionresult.get("resultAttributeValue").toString());
								tempJson.put(decisionresult.get("baseAttributeName").toString(), decisionresult.get("baseAttributeValue").toString());
								tempJson.put(decisionresult.get("resultAttributeName").toString(), decisionresult.get("resultAttributeValue").toString());
								batchCommitPackageArray.put(tempJson);
							}
							map.compute("OREResponseSuccessCount", (key, value) -> value == null ? 1 : value + 1);
						}
						break;
					}
				}
				if (batchCommitScoreArray.length() != 0) {
					batchSuccessCommitScoreJSON.put("records", batchCommitScoreArray);
					JSONObject commitResponse = new JSONObject(HTTPOperations.hitPUTServiceAndGetResponse(LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("CustomerDpDataObject_Url"), batchSuccessCommitScoreJSON, null, null));
					LOGGER.debug(commitResponse);
						JSONObject metadata = (JSONObject) commitResponse.get("_metadata");
						map.compute("CommitResponseRecordCount", (key, value) -> value == null ? metadata.optInt("recordCount") : value + metadata.optInt("recordCount"));
						map.compute("CommitResponseSuccessCount", (key, value) -> value == null ? metadata.optInt("successCount") : value + metadata.optInt("successCount"));
						map.compute("CommitResponseFailureCount", (key, value) -> value == null ? metadata.optInt("failureCount") : value + metadata.optInt("failureCount"));					
				}
				if (batchCommitPackageArray.length() != 0) {
					batchSuccessCommitPackageJSON.put("records", batchCommitPackageArray);
					JSONObject commitResponse = new JSONObject(HTTPOperations.hitPOSTServiceAndGetResponse(LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("CustomerPrequalifyPackageObject_Url"), batchSuccessCommitPackageJSON, null, null));
					LOGGER.debug(commitResponse);
						JSONObject metadata = (JSONObject) commitResponse.get("_metadata");
						map.compute("CommitResponseRecordCount", (key, value) -> value == null ? metadata.optInt("recordCount") : value + metadata.optInt("recordCount"));
						map.compute("CommitResponseSuccessCount", (key, value) -> value == null ? metadata.optInt("successCount") : value + metadata.optInt("successCount"));
						map.compute("CommitResponseFailureCount", (key, value) -> value == null ? metadata.optInt("failureCount") : value + metadata.optInt("failureCount"));
				}
				if (batchFailureArray.length() != 0) {
					batchFailureCommitJSON.put("records", batchFailureArray);
					HTTPOperations.hitPOSTServiceAndGetResponse(LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("DecisionFailureObject_Url"), batchFailureCommitJSON, null, null);
				}
				batch = batch + CUSTOMER_BATCHSIZE;
			}
		} catch (Exception e) {
			LOGGER.debug(e);
		}
		return map;
	}

	/**
	 * 
	 * This method updated failed records in backend with new Failure jobId
	 * 
	 * @param dcRequest
	 *            contains Session Details,RemoteAdress,HeaderMap,files used to
	 *            fetch base URL.
	 * @param jsonPayload
	 *            Contains list of records fetched from failure table.
	 * @param jobId
	 *            Contains UUID generated jobId or id given in request payload.
	 * @throws Exception
	 */
	protected void updateFailureRecordsJobId(DataControllerRequest dcRequest, JSONObject jsonPayload, String jobId) throws Exception {
		JSONArray jsonArray = jsonPayload.getJSONArray("records");
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = (JSONObject) jsonArray.get(i);
			obj.put("failureTriggerJob_id", jobId);
			jsonArray.put(i, obj);
		}
		jsonPayload.put("records", jsonArray);
		LOGGER.debug("DecisionFailureObjectResponse: " + HTTPOperations.hitPUTServiceAndGetResponse(LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("DecisionFailureObject_Url"), jsonPayload, null, null));
	}

	/**
	 * 
	 * This method is used to check if given jobId is already completed, if not
	 * then gets remaining records count.
	 * 
	 * @param dcRequest
	 *            contains Session Details,RemoteAdress,HeaderMap,files used to
	 *            fetch base URL.
	 * @param jobId
	 *            Contains UUID generated jobId or id given in request payload.
	 * @return int Returns count for remaining records for execution.
	 * @throws Exception
	 */
	protected int getAlreadyCompletedRecords(DataControllerRequest dcRequest, String jobId) throws Exception,LoansException {
		String url = LOANSERVICEURLPROP.getString("httpProtocol") + "://" + dcRequest.getHeader("host") + LOANSERVICEURLPROP.getString("DecisionBatchRun_get");
		String filter = "?$filter=job_id%20eq%20" + jobId;
		Map<String, String> inputmap = null;
		String responseString = HTTPOperations.hitPOSTServiceAndGetResponse(url + filter, (HashMap<String, String>) inputmap, null, null);
		LOGGER.debug("DecisionBatchRunResponse: " + responseString);
		JSONObject decisionbatchrunresponse = new JSONObject(responseString);
		JSONObject decisionbatchrunpayload = new JSONObject();
		if (0 == decisionbatchrunresponse.getInt("opstatus")) {
			JSONArray decisionbatchrunarray = (JSONArray) decisionbatchrunresponse.get("decisionbatchrun");
			if (decisionbatchrunarray.length() == 0) {
				throw new LoansException(ErrorCodeEnum.ERR_35202);
			}
			decisionbatchrunpayload = (JSONObject) decisionbatchrunarray.get(0);
			if (0 != Integer.parseInt(decisionbatchrunpayload.optString("successflag"))) {
				throw new LoansException(ErrorCodeEnum.ERR_35203);
			}
		} else {
			LOGGER.debug(decisionbatchrunresponse.get("errmsg").toString());
			throw new LoansException(ErrorCodeEnum.ERR_31000);
		}
		return Integer.parseInt(decisionbatchrunpayload.optString("completedRecordCounter"));
	}
}
