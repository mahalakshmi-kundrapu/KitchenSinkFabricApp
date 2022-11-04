package com.kony.adminconsole.service.limitsandfees.transactionfeesservice;

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

public class TransferFeeServiceView implements JavaService2 {

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {

		Result processedResult = new Result();

		try {
			String serviceId = "";
			serviceId = requestInstance.getParameter("serviceId");
			if (StringUtils.isBlank(serviceId)) {
				ErrorCodeEnum.ERR_20866.setErrorCode(processedResult);
			}

			JSONObject readResJSON = getTransferFeesForService(serviceId, requestInstance);
			if (readResJSON == null || !readResJSON.has(FabricConstants.OPSTATUS) || readResJSON.getInt(FabricConstants.OPSTATUS) != 0) {
				return ErrorCodeEnum.ERR_20845.setErrorCode(processedResult);
			}
			JSONArray periodicLimitsService = (JSONArray) readResJSON.get("transactionfeeservice_view");

			Dataset periodicLimitDataSet = new Dataset();
			periodicLimitDataSet.setId("records");

			for (int indexVar = 0; indexVar < periodicLimitsService.length(); indexVar++) {
				JSONObject periodicLimitInfoJSON = periodicLimitsService.getJSONObject(indexVar);
				Record servCommunicationInfoReord = new Record();
				for (String currKey : periodicLimitInfoJSON.keySet()) {
					Param currValParam = new Param(currKey, periodicLimitInfoJSON.getString(currKey), FabricConstants.STRING);
					servCommunicationInfoReord.addParam(currValParam);
				}
				periodicLimitDataSet.addRecord(servCommunicationInfoReord);
			}
			processedResult = new Result();
			processedResult.addDataset(periodicLimitDataSet);
			return processedResult;
		} catch (Exception e) {
			processedResult.addParam(new Param("exception", e.getMessage(), FabricConstants.STRING));
			processedResult.addParam(new Param("Status", "Error", FabricConstants.STRING));
			return ErrorCodeEnum.ERR_20845.setErrorCode(processedResult);
		}
	}

	public JSONObject getTransferFeesForService(String serviceId, DataControllerRequest requestInstance) {

		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.put(ODataQueryConstants.FILTER, "Service_id eq '" + serviceId + "'");
		String readEndpointResponse = Executor.invokeService(
				ServiceURLEnum.TRANSACTIONFEESERVICE_VIEW_READ, postParametersMap, null, requestInstance);
		return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

	}

}
