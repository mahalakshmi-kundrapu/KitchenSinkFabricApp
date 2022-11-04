package com.kony.adminconsole.service.campaignmanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to fetch attributes & customer count for a campaign customer role from the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class GroupAttributesFetchService implements JavaService2 {

	private static final Logger LOG = Logger.getLogger(GroupAttributesFetchService.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {

		Result result = new Result();

		try {
			result = getCampaignGroupAttributes(methodID, requestInstance, result);
		}
		catch (ApplicationException ae) {
			ae.getErrorCodeEnum().setErrorCode(result);
			LOG.error("ApplicationException occured in GroupAttributesFetchService JAVA service. Error: ", ae);
		}
		catch (Exception e) {
			ErrorCodeEnum.ERR_20001.setErrorCode(result);
			LOG.error("Exception occured in GroupAttributesFetchService JAVA service. Error: ", e);
		}

		return result;
	}

	Result getCampaignGroupAttributes(String methodID, DataControllerRequest requestInstance, Result result)
			throws ApplicationException {

		String groupId = requestInstance.getParameter("groupId");
		if (StringUtils.isBlank(groupId)) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21729);
		}

		// ** Reading from 'groupattribute' table **
		Map<String, String> groupattributeMap = new HashMap<>();
		groupattributeMap.put(ODataQueryConstants.FILTER, "group_id eq '" + groupId + "'");

		String response = Executor.invokeService(ServiceURLEnum.GROUPATTRIBUTE_READ, groupattributeMap, null,
				requestInstance);
		JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

		if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
				&& responseJSON.getInt(FabricConstants.OPSTATUS) == 0 && responseJSON.has("groupattribute")) {

			JSONArray responseJSONArray = responseJSON.getJSONArray("groupattribute");

			Dataset attributesDataset = null;
			int customerCount = 0;

			if (responseJSONArray != null && responseJSONArray.length() == 1) {
				JSONObject groupResponse = responseJSON.getJSONArray("groupattribute").getJSONObject(0);

				JSONArray adminattributesJSONArray = CommonUtilities.getStringAsJSONArray(groupResponse.optString("admin_attributes"));
				attributesDataset = adminattributesJSONArray != null
						? CommonUtilities.constructDatasetFromJSONArray(adminattributesJSONArray)
						: new Dataset();
				customerCount = groupResponse.optInt("customer_count");
			}
			else {
				attributesDataset = new Dataset();
			}

			result.addParam(new Param("customerCount", "" + customerCount, FabricConstants.INT));

			attributesDataset.setId("attributes");
			result.addDataset(attributesDataset);
		}
		else {
			throw new ApplicationException(ErrorCodeEnum.ERR_21773);
		}

		return result;
	}
}