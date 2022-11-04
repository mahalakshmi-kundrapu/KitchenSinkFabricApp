package com.kony.adminconsole.service.reports;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * @author Mohit Khosla
 *
 */
public class MessagesReport implements JavaService2 {

	private static final Logger LOG = Logger.getLogger(MessagesReport.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {

		requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
		Result processedResult = new Result();

		String startDate = requestInstance.getParameter("startDate");
		String endDate = requestInstance.getParameter("endDate");
		String category = requestInstance.getParameter("category");
		String csrName = requestInstance.getParameter("csrName");

		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.put("from_date", startDate);
		postParametersMap.put("to_date", endDate);
		if (!category.equals("Select category")) {
			postParametersMap.put("category_id", category);
		}
		else {
			postParametersMap.put("category_id", "");
		}
		if (StringUtils.isBlank(csrName)) {
			postParametersMap.put("csr_name", "");
		}
		else {
			postParametersMap.put("csr_name", csrName);
		}

		Dataset messagesDataSet = new Dataset();
		messagesDataSet.setId("messages");

		// Fetch count of Received Messages
		LOG.debug("Fetching Count of Receieved Messages");
		String messagesReceivedResponse = Executor.invokeService(ServiceURLEnum.REPORTS_MESSAGES_RECEIVED_SERVICE,
				postParametersMap, null, requestInstance);
		JSONObject messagesReceivedResponseJSON = CommonUtilities.getStringAsJSONObject(messagesReceivedResponse);
		if (messagesReceivedResponseJSON != null && messagesReceivedResponseJSON.has(FabricConstants.OPSTATUS)
				&& messagesReceivedResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
				&& messagesReceivedResponseJSON.has("records")) {
			LOG.debug("Fetch Count of Receieved Messages - Operation Status:Success");
			if (messagesReceivedResponseJSON.optJSONArray("records") != null
					&& messagesReceivedResponseJSON.optJSONArray("records").length() > 0) {
				int messagesReceived = messagesReceivedResponseJSON.optJSONArray("records").getJSONObject(0)
						.optInt("messages_received_count");
				Record messagesReceivedRecord = new Record();
				messagesReceivedRecord.addParam(new Param("name", "Received", FabricConstants.STRING));
				messagesReceivedRecord
						.addParam(new Param("value", String.valueOf(messagesReceived), FabricConstants.STRING));
				messagesDataSet.addRecord(messagesReceivedRecord);
			}
		}

		// Fetch count of Sent Messages
		String messagesSentResponse = Executor.invokeService(ServiceURLEnum.REPORTS_MESSAGES_SENT_SERVICE,
				postParametersMap, null, requestInstance);
		JSONObject messagesSentResponseJSON = CommonUtilities.getStringAsJSONObject(messagesSentResponse);

		if (messagesSentResponseJSON != null && messagesSentResponseJSON.has(FabricConstants.OPSTATUS)
				&& messagesSentResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
				&& messagesSentResponseJSON.has("records")) {
			if (messagesSentResponseJSON.optJSONArray("records") != null
					&& messagesSentResponseJSON.optJSONArray("records").length() > 0) {
				int messagesSent = messagesSentResponseJSON.getJSONArray("records").getJSONObject(0)
						.optInt("messages_sent_count");

				Record messagesSentRecord = new Record();
				messagesSentRecord.addParam(new Param("name", "Sent", FabricConstants.STRING));
				messagesSentRecord.addParam(new Param("value", String.valueOf(messagesSent), FabricConstants.STRING));
				messagesDataSet.addRecord(messagesSentRecord);
			}
		}
		processedResult.addDataset(messagesDataSet);

		Dataset threadsDataSet = new Dataset();
		threadsDataSet.setId("threads");

		// Fetch count of Resolved Threads
		String threadsResolvedResponse = Executor.invokeService(ServiceURLEnum.REPORTS_THREADS_RESOLVED_SERVICE,
				postParametersMap, null, requestInstance);
		JSONObject threadsResolvedResponseJSON = CommonUtilities.getStringAsJSONObject(threadsResolvedResponse);

		if (threadsResolvedResponseJSON != null && threadsResolvedResponseJSON.has(FabricConstants.OPSTATUS)
				&& threadsResolvedResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
			int threadsResolved = threadsResolvedResponseJSON.getJSONArray("records").getJSONObject(0)
					.getInt("threads_resolved_count");

			Record threadsResolvedRecord = new Record();
			threadsResolvedRecord.addParam(new Param("name", "Resolved", FabricConstants.STRING));
			threadsResolvedRecord.addParam(new Param("value", String.valueOf(threadsResolved), FabricConstants.STRING));
			threadsDataSet.addRecord(threadsResolvedRecord);
		}

		// Fetch count of New Threads
		String threadsNewResponse = Executor.invokeService(ServiceURLEnum.REPORTS_THREADS_NEW_SERVICE,
				postParametersMap, null, requestInstance);
		JSONObject threadsNewResponseJSON = CommonUtilities.getStringAsJSONObject(threadsNewResponse);

		if (threadsNewResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
			int threadsNew = threadsNewResponseJSON.getJSONArray("records").getJSONObject(0)
					.getInt("threads_new_count");

			Record threadsNewRecord = new Record();
			threadsNewRecord.addParam(new Param("name", "New", FabricConstants.STRING));
			threadsNewRecord.addParam(new Param("value", String.valueOf(threadsNew), FabricConstants.STRING));
			threadsDataSet.addRecord(threadsNewRecord);
		}

		// Fetch Average Age of Threads
		String threadsAverageAgeResponse = Executor.invokeService(ServiceURLEnum.REPORTS_THREADS_AVERAGEAGE_SERVICE,
				postParametersMap, null, requestInstance);
		JSONObject threadsAverageAgeResponseJSON = CommonUtilities.getStringAsJSONObject(threadsAverageAgeResponse);

		if (threadsAverageAgeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {

			String threadsAverageAge = "0.0";
			JSONObject threadsAverageAgeResponseJSONObject = threadsAverageAgeResponseJSON.getJSONArray("records")
					.getJSONObject(0);
			if (!threadsAverageAgeResponseJSONObject.isNull("threads_averageage_count")) {
				threadsAverageAge = ""
						+ Math.round(Double.parseDouble(threadsAverageAgeResponseJSON.getJSONArray("records")
								.getJSONObject(0).getString("threads_averageage_count")) * 100.0) / 100.0;
			}

			Record threadsAverageAgeRecord = new Record();
			threadsAverageAgeRecord.addParam(new Param("name", "Average Age (Days)", FabricConstants.STRING));
			threadsAverageAgeRecord.addParam(new Param("value", threadsAverageAge, FabricConstants.STRING));
			threadsDataSet.addRecord(threadsAverageAgeRecord);
		}

		processedResult.addDataset(threadsDataSet);

		return processedResult;
	}

}