package com.kony.adminconsole.service.limitsandfees.transactionfeesservice;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.service.limitsandfees.ValidateTransactionFeeLimits;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class TransferFeeServiceAdd implements JavaService2 {

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {
		Result result = new Result();
		String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
		Param statusParam = null;
		Result processedResult = new Result();
		String serviceId = "";
		String transactionFeeId = CommonUtilities.getNewId().toString();
		JSONArray transactionFeeSlabsArray = null;
		String systemUser = "";
		if (requestInstance.getParameter("serviceId") != null) {
			serviceId = requestInstance.getParameter("serviceId");
			if (StringUtils.isBlank(serviceId)) {
				return ErrorCodeEnum.ERR_20570.setErrorCode(processedResult);
			}
		} else {
			return ErrorCodeEnum.ERR_20570.setErrorCode(processedResult);
		}
		if (requestInstance.getParameter("systemUserId") != null) {
			systemUser = requestInstance.getParameter("systemUserId");
		} else {
			return ErrorCodeEnum.ERR_20570.setErrorCode(processedResult);
		}
		if (requestInstance.getParameter("transferFeeSlabs") != null) {
			transactionFeeSlabsArray = new JSONArray(requestInstance.getParameter("transferFeeSlabs"));
		}
		String name = requestInstance.getParameter("transactionFeeName");
		String description = requestInstance.getParameter("transactionFeeDescription");
		try {
			JSONObject prevPL = getRecordsForServiceID(serviceId, authToken, requestInstance);
			if (prevPL == null || prevPL.getInt(FabricConstants.OPSTATUS) != 0) {
				return ErrorCodeEnum.ERR_20802.setErrorCode(processedResult);
			}
			Dataset ds = CommonUtilities.constructDatasetFromJSONArray(prevPL.getJSONArray("transactionfeeservice_view"));
			ValidateTransactionFeeLimits validateUtil = new ValidateTransactionFeeLimits();
			Dataset validationDS = validateUtil.validateFeeLimits(ds, transactionFeeSlabsArray);
			if (!validationDS.getAllRecords().isEmpty()) {
				result.addDataset(validationDS);
				return result;

			}
			JSONObject createResponseJSON = createTransactionFee(transactionFeeId, name, description, systemUser,
					authToken, requestInstance);
			if (createResponseJSON != null && createResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
				return ErrorCodeEnum.ERR_20801.setErrorCode(processedResult);
			}
			JSONObject createPeriodicLimitResponseJSON = createTransactionFeeSlab(serviceId, transactionFeeId,
					transactionFeeSlabsArray, authToken, systemUser, requestInstance);
			if (createPeriodicLimitResponseJSON != null
					&& createPeriodicLimitResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
				return ErrorCodeEnum.ERR_20805.setErrorCode(processedResult);
			}
			result.addParam(new Param("transactionFeeId", transactionFeeId, FabricConstants.STRING));
		} catch (Exception e) {
			statusParam = new Param("Status", "Error", FabricConstants.STRING);
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.FAILED, "Transaction fee service create failed. errmsg: " + e.getMessage());
			result.addParam(statusParam);
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
				ActivityStatusEnum.SUCCESSFUL, "Transaction fee service create success.");
		return result;

	}

	JSONObject createTransactionFee(String transactionFeeId, String name, String description, String systemUser,
			String authToken, DataControllerRequest requestInstance) {
		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", transactionFeeId);
		postParametersMap.put("Name", name);
		postParametersMap.put("Description", description);
		postParametersMap.put("createdby", systemUser);
		postParametersMap.put("modifiedby", "NULL");
		postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("softdeleteflag", "0");
		String createRoleResponse = Executor.invokeService(ServiceURLEnum.TRANSACTIONFEE_CREATE, postParametersMap,
				null, requestInstance);

		createResponseJSON = CommonUtilities.getStringAsJSONObject(createRoleResponse);
		int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.FAILED, "Transaction fee create failed. transactionFeeId: " + transactionFeeId);
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
				ActivityStatusEnum.SUCCESSFUL, "Transaction fee create success. transactionFeeId: " + transactionFeeId);
		return createResponseJSON;
	}

	private JSONObject createTransactionFeeSlab(String serviceId, String transactionFeeId,
			JSONArray transactionFeeSlabsArray, String authToken, String systemUser,
			DataControllerRequest requestInstance) {
		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		String transactionFeeSlabId = null;
		for (int i = 0; i < transactionFeeSlabsArray.length(); i++) {
			transactionFeeSlabId = CommonUtilities.getNewId().toString();
			String minimumTransactionValue = ((JSONObject) transactionFeeSlabsArray.get(i)).get("minAmount").toString()
					.trim();
			String maximumTransactionValue = ((JSONObject) transactionFeeSlabsArray.get(i)).get("maxAmount").toString()
					.trim();
			String fees = ((JSONObject) transactionFeeSlabsArray.get(i)).get("fees").toString().trim();
			postParametersMap.clear();
			postParametersMap.put("id", transactionFeeSlabId);
			postParametersMap.put("TransactionFee_id", transactionFeeId);
			postParametersMap.put("MinimumTransactionValue", minimumTransactionValue);
			postParametersMap.put("MaximumTransactionValue", maximumTransactionValue);
			postParametersMap.put("Fees", fees);
			postParametersMap.put("Currency", "INR");
			postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
			postParametersMap.put("modifiedby", "NULL");
			postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
			postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
			postParametersMap.put("softdeleteflag", "0");

			String createPeriodicLimitResponse = Executor.invokeService(ServiceURLEnum.TRANSACTIONFEESLAB_CREATE,
					postParametersMap, null, requestInstance);

			createResponseJSON = CommonUtilities.getStringAsJSONObject(createPeriodicLimitResponse);
			int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
			if (getOpStatusCode != 0) {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
						ActivityStatusEnum.FAILED,
						"Transaction feeslab create failed. transactionFeeSlabId: " + transactionFeeSlabId);
				return createResponseJSON;
			}
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.SUCCESSFUL,
					"Transaction feeslab create success. transactionFeeSlabId: " + transactionFeeSlabId);
		}
		return createResponseJSON;
	}

	public JSONObject updateServiceObj(String serviceId, String transactionFeeId, String systemUser, String authToken,
			DataControllerRequest requestInstance) {
		JSONObject updateResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", serviceId);
		postParametersMap.put("TransactionFee_id", transactionFeeId);
		postParametersMap.put("modifiedby", systemUser);
		String updateServiceResponse = Executor.invokeService(ServiceURLEnum.SERVICE_UPDATE, postParametersMap, null,
				requestInstance);

		updateResponseJSON = CommonUtilities.getStringAsJSONObject(updateServiceResponse);
		int getOpStatusCode = updateResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			return updateResponseJSON;
		}

		return updateResponseJSON;
	}

	public JSONObject getRecordsForServiceID(String serviceID, String authToken,
			DataControllerRequest requestInstance) {

		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.put(ODataQueryConstants.FILTER, "Service_id eq '" + serviceID + "'");
		String readEndpointResponse = Executor.invokeService(ServiceURLEnum.TRANSACTIONFEESERVICE_VIEW_READ,
				postParametersMap, null, requestInstance);
		return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

	}
}