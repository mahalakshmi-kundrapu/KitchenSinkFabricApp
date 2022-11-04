package com.kony.adminconsole.service.limitsandfees.periodiclimitenduser;

import java.util.HashMap;
import java.util.Map;

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

public class PeriodLimitEndUserView implements JavaService2 {

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {

		Result result = new Result();
		String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
		Result processedResult = new Result();

		String customerId = "";

		if (requestInstance.getParameter("customerId") != null) {
			customerId = requestInstance.getParameter("customerId");
		} else {
			return ErrorCodeEnum.ERR_20565.setErrorCode(result);
		}

		try {
			JSONObject readResJSON = getPeriodicLimitsForCustomer(customerId, authToken, requestInstance);
			if (readResJSON.getInt(FabricConstants.OPSTATUS) != 0) {
				return ErrorCodeEnum.ERR_20838.setErrorCode(result);
			}
			JSONArray periodicLimitsServiceWiseForCustomer = (JSONArray) readResJSON.get("periodiclimitenduser_view");

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
		} catch (ClassCastException e) {
			return ErrorCodeEnum.ERR_20001.setErrorCode(result);
		}
	}

	public JSONObject getPeriodicLimitsForCustomer(String customerId, String authToken,
			DataControllerRequest requestInstance) {

		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.put(ODataQueryConstants.FILTER, "Customer_id eq '" + customerId + "'");
		String readEndpointResponse = Executor.invokeService(
				ServiceURLEnum.PERIODICLIMITENDUSER_VIEW_READ, postParametersMap,
				null, requestInstance);
		return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

	}
}