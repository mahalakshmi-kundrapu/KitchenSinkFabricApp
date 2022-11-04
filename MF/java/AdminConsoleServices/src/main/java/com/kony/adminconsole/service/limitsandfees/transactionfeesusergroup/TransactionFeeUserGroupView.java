package com.kony.adminconsole.service.limitsandfees.transactionfeesusergroup;

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

public class TransactionFeeUserGroupView implements JavaService2 {

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {

		Result result = new Result();
		String groupId = requestInstance.getParameter("groupId");
		if (StringUtils.isBlank(groupId)) {
			return ErrorCodeEnum.ERR_20569.setErrorCode(result);
		}
		try {

			JSONObject readResJSON = getTransferFeesForUserGroup(groupId, requestInstance);
			if (readResJSON.getInt(FabricConstants.OPSTATUS) != 0) {
				return ErrorCodeEnum.ERR_20802.setErrorCode(result);
			}
			JSONArray periodicLimitsService = (JSONArray) readResJSON.get("transactionfeegroup_view");

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
			Result processedResult = new Result();
			processedResult.addDataset(periodicLimitDataSet);
			return processedResult;
		} catch (ClassCastException e) {
			Param statusParam = new Param("Status", "Error", FabricConstants.STRING);
			result.addParam(statusParam);
		}
		return result;
	}

	private JSONObject getTransferFeesForUserGroup(String groupId,
			DataControllerRequest requestInstance) {
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.put(ODataQueryConstants.FILTER, "Group_id eq '" + groupId + "'");
		String viewResponse = Executor.invokeService(
				ServiceURLEnum.TRANSACTIONFEE_READ, postParametersMap, null, requestInstance);
		JSONObject response = CommonUtilities.getStringAsJSONObject(viewResponse);
		return response;
	}
}