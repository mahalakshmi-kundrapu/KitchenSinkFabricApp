package com.kony.adminconsole.service.limitsandfees.periodiclimitusergroup;

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
import com.konylabs.middleware.dataobject.Result;

/**
 * @author KH2256 this class should return groupId, groupEntitlementIds, serviceId, TransactionFee_id,
 *         TransactionLimit_id Period_id ,MaximumLimit, Currency,Name,DayCount
 *
 */
public class PeriodLimitUserGroupView implements JavaService2 {

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {

		String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);

		String groupId = requestInstance.getParameter("groupId");

		if (StringUtils.isBlank(groupId)) {
			return ErrorCodeEnum.ERR_20569.setErrorCode(null);
		}

		try {
			JSONObject response = fetchPeriodicLimitsByGroupId(groupId, authToken, requestInstance);
			if (response.getInt(FabricConstants.OPSTATUS) != 0) {
				return ErrorCodeEnum.ERR_20814.setErrorCode(null);
			}

			Dataset dataset = CommonUtilities
					.constructDatasetFromJSONArray((JSONArray) response.get("periodiclimitusergroup_view"));
			Result result = new Result();
			dataset.setId("groupLimits");
			result.addDataset(dataset);
			return result;

		} catch (Exception e) {
			return ErrorCodeEnum.ERR_20001.setErrorCode(null);
		}
	}

	private JSONObject fetchPeriodicLimitsByGroupId(String groupId, String authToken,
			DataControllerRequest requestInstance) {
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.put(ODataQueryConstants.FILTER, "Group_id eq '" + groupId + "'");
		String viewResponse = Executor.invokeService(
				ServiceURLEnum.PERIODICLIMITUSERGROUP_VIEW_READ, postParametersMap,
				null, requestInstance);
		return CommonUtilities.getStringAsJSONObject(viewResponse);
	}
}