package com.kony.adminconsole.service.productmanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.service.customermanagement.CustomerSecurityQuestionRead;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class BankDetailsFetchService implements JavaService2 {

	private static final Logger LOG = Logger.getLogger(CustomerSecurityQuestionRead.class);
	private static final String DEFAULT_BANK_NAME = "Infinity";

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {
		Result processedResult = new Result();
		String routingNumber, swiftCode, serviceName = null, bankName = null;
		try {

			routingNumber = requestInstance.getParameter("routingNumber");
			swiftCode = requestInstance.getParameter("swiftCode");
			serviceName = requestInstance.getParameter("serviceName");

			if (StringUtils.isBlank(routingNumber) && StringUtils.isBlank(swiftCode)) {
				ErrorCodeEnum.ERR_20682.setErrorCode(processedResult);
				return processedResult;
			}

			if (StringUtils.isBlank(serviceName)) {
				ErrorCodeEnum.ERR_20683.setErrorCode(processedResult);
				return processedResult;
			}

			JSONObject readResJSON = null;
			if (StringUtils.isNotBlank(routingNumber)) {
				readResJSON = getBankDetailsWithRoutingNumber(routingNumber, serviceName, requestInstance);
			} else if (StringUtils.isNotBlank(swiftCode)) {
				readResJSON = getBankDetailsWithSwiftCode(swiftCode, serviceName, requestInstance);
			} else {
				ErrorCodeEnum.ERR_20682.setErrorCode(processedResult);
				return processedResult;
			}

			if (readResJSON != null && readResJSON.has(FabricConstants.OPSTATUS) && readResJSON.getInt(FabricConstants.OPSTATUS) == 0 && readResJSON.has("bankfortransfer_view")) {
				JSONArray bankDetail = readResJSON.getJSONArray("bankfortransfer_view");
				if (bankDetail.length() > 0) {
					JSONObject bankDetailInfoJSON = bankDetail.getJSONObject(0);
					if (bankDetailInfoJSON.has("Bank_Name")) {
						bankName = bankDetailInfoJSON.getString("Bank_Name");
					}
				}
			}
			if (StringUtils.isBlank(bankName)) {
				bankName = DEFAULT_BANK_NAME;
			}

			processedResult.addParam(new Param("bankName", bankName, FabricConstants.STRING));
		} catch (Exception e) {
			LOG.error("Exception in fetching bank information.", e);
			ErrorCodeEnum.ERR_20681.setErrorCode(processedResult);
		}
		return processedResult;
	}

	private JSONObject getBankDetailsWithRoutingNumber(String routingNumber, String serviceName,

			DataControllerRequest requestInstance) {
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.put(ODataQueryConstants.FILTER,
				"Routing_Number eq '" + routingNumber + "' and Service_Name eq '" + serviceName + "'");
		String readEndpointResponse = Executor.invokeService(
				ServiceURLEnum.BANKFORTRANSFER_VIEW_READ,
				postParametersMap, null, requestInstance);
		return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
	}

	private JSONObject getBankDetailsWithSwiftCode(String swiftCode, String serviceName,
			DataControllerRequest requestInstance) {
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.put(ODataQueryConstants.FILTER,
				"Routing_Number eq '" + swiftCode + "' and Service_Name eq '" + serviceName + "'");
		String readEndpointResponse = Executor.invokeService(
				ServiceURLEnum.BANKFORTRANSFER_VIEW_READ,
				postParametersMap, null, requestInstance);
		return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
	}

}