package com.kony.adminconsole.service.limitsandfees.transactionfeesenduser;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
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

public class TransactionFeeEndUserView implements JavaService2 {

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {

		Result processedResult = new Result();

		String customerId = requestInstance.getParameter("customerId");
		if (StringUtils.isBlank(customerId)) {
			return ErrorCodeEnum.ERR_20565.setErrorCode(processedResult);
		}

		try {
			JSONObject readResJSON = getPeriodicLimitsForCustomer(customerId, requestInstance);
			if (readResJSON == null || !readResJSON.has(FabricConstants.OPSTATUS) || readResJSON.getInt(FabricConstants.OPSTATUS) != 0) {
				return ErrorCodeEnum.ERR_20818.setErrorCode(processedResult);
			}
			JSONArray periodicLimitsServiceWiseForCustomer = (JSONArray) readResJSON.get("transactionfeesenduser_view");

			Dataset periodicLimitDataSet = new Dataset();
			periodicLimitDataSet.setId("records");

			for (int indexVar = 0; indexVar < periodicLimitsServiceWiseForCustomer.length(); indexVar++) {
				JSONObject periodicLimitInfoJSON = periodicLimitsServiceWiseForCustomer.getJSONObject(indexVar);
				Record currCommunicationInfoReord = new Record();
				for (String currKey : periodicLimitInfoJSON.keySet()) {
					Param currValParam = new Param(currKey, periodicLimitInfoJSON.getString(currKey), FabricConstants.STRING);
					currCommunicationInfoReord.addParam(currValParam);
				}
				periodicLimitDataSet.addRecord(currCommunicationInfoReord);
			}
			processedResult = new Result();
			processedResult.addDataset(periodicLimitDataSet);
			return processedResult;
		} catch (Exception e) {
			ErrorCodeEnum.ERR_20818.setErrorCode(processedResult);
			return processedResult;
		}

	}

	public JSONObject getPeriodicLimitsForCustomer(String customerId, DataControllerRequest requestInstance) {
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.put(ODataQueryConstants.FILTER, "customer_id eq '" + customerId + "'");
		String readEndpointResponse = Executor.invokeService(
				ServiceURLEnum.TRANSACTIONFEESENDUSER_VIEW_READ, postParametersMap, null, requestInstance);
		return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
	}

}
