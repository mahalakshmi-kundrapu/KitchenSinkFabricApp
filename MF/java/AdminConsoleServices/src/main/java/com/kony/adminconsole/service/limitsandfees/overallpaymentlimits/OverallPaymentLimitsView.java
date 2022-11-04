package com.kony.adminconsole.service.limitsandfees.overallpaymentlimits;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class OverallPaymentLimitsView implements JavaService2 {
	private static final Logger LOG = Logger.getLogger(OverallPaymentLimitsView.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {
		try {
			String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
			String systemUser = requestInstance.getParameter("systemUserId");
			Result result = new Result();

			if (StringUtils.isBlank(systemUser)) {
				return ErrorCodeEnum.ERR_20571.setErrorCode(result);
			}
			Dataset resultDataset = fetchAllTransactionGroupsByIdServices(null,
					ServiceURLEnum.OVERALLPAYMENTLIMITS_VIEW_READ, authToken, requestInstance);
			result.addDataset(resultDataset);
			return result;
		} catch (Exception e) {
			Result errorResult = new Result();
			LOG.debug("Runtime Exception.Exception Trace:", e);
			ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
			return errorResult;
		}
	}

	public Dataset fetchAllTransactionGroupsByIdServices(String id, ServiceURLEnum serviceURLEnum, String authToken,
			DataControllerRequest requestInstance) throws Exception {
		Map<String, String> postParametersMap = new HashMap<String, String>();
		if (StringUtils.isNotBlank(id)) {
			postParametersMap.put(ODataQueryConstants.FILTER, "TransactionGroupId eq '" + id + "'");
		} else {
			postParametersMap.put(ODataQueryConstants.ORDER_BY, "TransactionGroupId, DayCount desc ");
		}
		String viewResponse = Executor.invokeService(serviceURLEnum, postParametersMap, null, requestInstance);
		JSONObject response = CommonUtilities.getStringAsJSONObject(viewResponse);
		if (response.getInt(FabricConstants.OPSTATUS) != 0) {
			throw new Exception("Error in fetching Overall Payment Limits");
		}

		Dataset dataset = CommonUtilities
				.constructDatasetFromJSONArray(response.getJSONArray("overallpaymentlimits_view"));

		Dataset resultDataset = new Dataset("overallPaymentLimits");
		Set<String> serviceIds = new HashSet<String>();
		Set<String> periodIds = new HashSet<String>();

		Record TransactionGroupRecord = new Record();
		Dataset services = new Dataset("services");
		Dataset periodicLimits = new Dataset("periodicLimits");
		int totalRecords = dataset.getAllRecords().size();

		for (int i = 0; i < totalRecords; i++) {
			Record record = dataset.getRecord(i);
			String transactionGroupId = record.getParam("TransactionGroupId").getValue();
			String transactionGroupName = record.getParam("TransactionGroupName") == null ? ""
					: record.getParam("TransactionGroupName").getValue();
			String transactionLimitId = record.getParam("TransactionLimitId").getValue();
			String statusId = record.getParam("Status_id") == null ? "" : record.getParam("Status_id").getValue();
			String serviceId = record.getParam("ServiceId").getValue();
			String serviceName = record.getParam("ServiceName").getValue();
			String transactionGroupServiceId = record.getParam("TransactionGroupServiceId").getValue();
			String periodId = record.getParam("Period_id").getValue();
			String periodicLimitId = record.getParam("PeriodicLimitId").getValue();
			String periodName = record.getParam("PeriodName").getValue();
			String maximumLimit = record.getParam("MaximumLimit").getValue();
			String dayCount = record.getParam("DayCount").getValue();

			TransactionGroupRecord = new Record();
			if (!serviceIds.contains(serviceId)) {
				Record serviceRecord = new Record();
				serviceRecord.addParam(
						new Param("transactionGroupServiceId", transactionGroupServiceId, FabricConstants.STRING));
				serviceRecord.addParam(new Param("serviceId", serviceId, FabricConstants.STRING));
				serviceRecord.addParam(new Param("serviceName", serviceName, FabricConstants.STRING));
				services.addRecord(serviceRecord);
				serviceIds.add(serviceId);
			}
			if (!periodIds.contains(periodId)) {
				Record periodicLimitsRecord = new Record();
				periodicLimitsRecord.addParam(new Param("periodId", periodId, FabricConstants.STRING));
				periodicLimitsRecord.addParam(new Param("periodName", periodName, FabricConstants.STRING));
				periodicLimitsRecord.addParam(new Param("maximumLimit", maximumLimit, FabricConstants.STRING));
				periodicLimitsRecord.addParam(new Param("periodicLimitId", periodicLimitId, FabricConstants.STRING));
				periodicLimitsRecord.addParam(new Param("dayCount", dayCount, FabricConstants.STRING));
				periodicLimits.addRecord(periodicLimitsRecord);
				periodIds.add(periodId);
			}

			if (totalRecords == 1 || i == totalRecords - 1) {
				TransactionGroupRecord
						.addParam(new Param("transactionGroupId", transactionGroupId, FabricConstants.STRING));
				TransactionGroupRecord
						.addParam(new Param("transactionGroupName", transactionGroupName, FabricConstants.STRING));
				TransactionGroupRecord
						.addParam(new Param("transactionLimitId", transactionLimitId, FabricConstants.STRING));
				TransactionGroupRecord.addParam(new Param("statusId", statusId, FabricConstants.STRING));
				TransactionGroupRecord.addDataset(services);
				TransactionGroupRecord.addDataset(periodicLimits);
				resultDataset.addRecord(TransactionGroupRecord);
				serviceIds = new HashSet<String>();
				periodIds = new HashSet<String>();
				services = new Dataset("services");
				periodicLimits = new Dataset("periodicLimits");
			} else {
				Record nextRecord = dataset.getRecord(i + 1);
				String nextTransactionGroupId = nextRecord.getParam("TransactionGroupId").getValue();
				if (!nextTransactionGroupId.equalsIgnoreCase(transactionGroupId)) {
					TransactionGroupRecord
							.addParam(new Param("transactionGroupId", transactionGroupId, FabricConstants.STRING));
					TransactionGroupRecord
							.addParam(new Param("transactionGroupName", transactionGroupName, FabricConstants.STRING));
					TransactionGroupRecord
							.addParam(new Param("transactionLimitId", transactionLimitId, FabricConstants.STRING));
					TransactionGroupRecord.addParam(new Param("statusId", statusId, FabricConstants.STRING));
					TransactionGroupRecord.addDataset(services);
					TransactionGroupRecord.addDataset(periodicLimits);
					resultDataset.addRecord(TransactionGroupRecord);
					serviceIds = new HashSet<String>();
					periodIds = new HashSet<String>();
					services = new Dataset("services");
					periodicLimits = new Dataset("periodicLimits");
				}
			}

		}
		return resultDataset;
	}
}