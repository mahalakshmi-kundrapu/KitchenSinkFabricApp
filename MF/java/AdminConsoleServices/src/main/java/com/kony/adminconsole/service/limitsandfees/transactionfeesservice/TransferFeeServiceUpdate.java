package com.kony.adminconsole.service.limitsandfees.transactionfeesservice;

import java.util.HashMap;
import java.util.Map;

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

public class TransferFeeServiceUpdate implements JavaService2 {

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {
		Result result = new Result();
		String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
		Param statusParam = null;
		Result processedResult = new Result();
		String serviceId = "";
		String transactionFeeId = "";
		JSONArray transactionFeeSlabsArray = null;
		String slabId = "";
		String minLimit = "";
		String maxLimit = "";
		String fees = "";
		String deleteFlag = "";
		String systemUser = "";
		if (requestInstance.getParameter("serviceId") != null) {
			serviceId = requestInstance.getParameter("serviceId");
		} else {
			return ErrorCodeEnum.ERR_20570.setErrorCode(processedResult);
		}
		if (requestInstance.getParameter("systemUserId") != null) {
			systemUser = requestInstance.getParameter("systemUserId");
		} else {
			return ErrorCodeEnum.ERR_20570.setErrorCode(processedResult);
		}
		if (requestInstance.getParameter("transactionFeeId") != null) {
			transactionFeeId = requestInstance.getParameter("transactionFeeId");
		}
		if (requestInstance.getParameter("transferFeeSlabs") != null) {
			transactionFeeSlabsArray = new JSONArray(requestInstance.getParameter("transferFeeSlabs"));
		}

		try {
			JSONObject prevPL = getRecordsForServiceID(serviceId, authToken, requestInstance);
			if (prevPL != null && prevPL.getInt(FabricConstants.OPSTATUS) != 0) {
				return ErrorCodeEnum.ERR_20822.setErrorCode(processedResult);
			}
			Dataset ds = CommonUtilities.constructDatasetFromJSONArray(prevPL.getJSONArray("transactionfeeservice_view"));
			ValidateTransactionFeeLimits validateUtil = new ValidateTransactionFeeLimits();
			Dataset validationDS = validateUtil.validateFeeLimits(ds, transactionFeeSlabsArray);
			if (!validationDS.getAllRecords().isEmpty()) {
				result.addDataset(validationDS);
				return result;

			}
			for (int i = 0; i < transactionFeeSlabsArray.length(); i++) {
				slabId = ((JSONObject) transactionFeeSlabsArray.get(i)).optString("transactionFeesSlabId");
				minLimit = ((JSONObject) transactionFeeSlabsArray.get(i)).optString("minAmount");
				maxLimit = ((JSONObject) transactionFeeSlabsArray.get(i)).optString("maxAmount");
				fees = ((JSONObject) transactionFeeSlabsArray.get(i)).optString("fees");
				deleteFlag = ((JSONObject) transactionFeeSlabsArray.get(i)).optString("deleteFlag");

				if (("Y").equalsIgnoreCase(deleteFlag)) {
					// delete
					JSONObject deletePeriodicLimitResponseJSON = deleteTransferFeeSlab(slabId, authToken,
							systemUser, requestInstance);
					if (deletePeriodicLimitResponseJSON != null
							&& deletePeriodicLimitResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						return ErrorCodeEnum.ERR_20808.setErrorCode(processedResult);
					}
					continue;
				}

				if (slabId == null || slabId.isEmpty() || slabId.trim().length() == 0) {
					slabId = CommonUtilities.getNewId().toString();
					JSONObject createPeriodicLimitResponseJSON = createTransactionFeeSlab(serviceId, transactionFeeId,
							transactionFeeSlabsArray, authToken, systemUser, requestInstance);
					if (createPeriodicLimitResponseJSON != null
							&& createPeriodicLimitResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						return ErrorCodeEnum.ERR_20805.setErrorCode(processedResult);
					}
					continue;
				} else {
					// update
					JSONObject updateTransferFeeSlabResponseJSON = updateTransferFeeSlab(slabId,
							transactionFeeId, minLimit, maxLimit, fees, authToken, systemUser, requestInstance);
					if (updateTransferFeeSlabResponseJSON != null
							&& updateTransferFeeSlabResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						return ErrorCodeEnum.ERR_20807.setErrorCode(processedResult);
					}
					continue;
				}
			}
		} catch (Exception e) {
			statusParam = new Param("Status", "Error", FabricConstants.STRING);
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.CREATE, ActivityStatusEnum.FAILED,
					"Transaction fee service create failed. errmsg: " + e.getMessage());
			result.addParam(statusParam);
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
				EventEnum.CREATE, ActivityStatusEnum.SUCCESSFUL,
				"Transaction fee service create success");
		return result;

	}

	private JSONObject deleteTransferFeeSlab(String slabId, String authToken, String systemUser,
			DataControllerRequest requestInstance) {
		JSONObject updateResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", slabId);
		String updatePeriodicLimitResponse = Executor.invokeService(
				ServiceURLEnum.TRANSACTIONFEESLAB_DELETE,
				postParametersMap, null, requestInstance);

		updateResponseJSON = CommonUtilities.getStringAsJSONObject(updatePeriodicLimitResponse);
		int getOpStatusCode = updateResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.DELETE, ActivityStatusEnum.FAILED,
					"transactoin fee slab delete failed. ");
			return updateResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
				EventEnum.DELETE, ActivityStatusEnum.SUCCESSFUL,
				"Transaction fee slab delete success.");
		return updateResponseJSON;
	}

	private JSONObject createTransactionFeeSlab(String serviceId, String transactionFeeId,
			JSONArray transactionFeeSlabsArray, String authToken, String systemUser,
			DataControllerRequest requestInstance) {
		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		String transactionFeeSlabId = null;
		for (int i = 0; i < transactionFeeSlabsArray.length(); i++) {
			transactionFeeSlabId = CommonUtilities.getNewId().toString();
			String minimumTransactionValue = ((JSONObject) transactionFeeSlabsArray.get(i)).get("minAmount").toString().trim();
			String maximumTransactionValue = ((JSONObject) transactionFeeSlabsArray.get(i)).get("maxAmount").toString().trim();
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

			String createPeriodicLimitResponse = Executor.invokeService(
					ServiceURLEnum.TRANSACTIONFEESLAB_CREATE,
					postParametersMap, null, requestInstance);

			createResponseJSON = CommonUtilities.getStringAsJSONObject(createPeriodicLimitResponse);
			int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
			if (getOpStatusCode != 0) {
				// rollback logic
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
						EventEnum.CREATE, ActivityStatusEnum.FAILED,
						"transactoin fee slab create failed. transactionFeeSlabId: " + transactionFeeSlabId);
				return createResponseJSON;
			}
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.CREATE, ActivityStatusEnum.SUCCESSFUL,
					"transaction fee slab create success. transactionFeeSlabId: " + transactionFeeSlabId);
		}
		return createResponseJSON;
	}

	private JSONObject updateTransferFeeSlab(String slabId, String transactionFeeId, String minLimit, String maxLimit,
			String fees, String authToken, String systemUser, DataControllerRequest requestInstance) {
		JSONObject updateResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", slabId);
		postParametersMap.put("TransactionFee_id", transactionFeeId);
		postParametersMap.put("MinimumTransactionValue", minLimit);
		postParametersMap.put("MaximumTransactionValue", maxLimit);
		postParametersMap.put("Fees", fees);
		postParametersMap.put("modifiedby", systemUser);
		String updatePeriodicLimitResponse = Executor.invokeService(
				ServiceURLEnum.TRANSACTIONFEESLAB_UPDATE,
				postParametersMap, null, requestInstance);

		updateResponseJSON = CommonUtilities.getStringAsJSONObject(updatePeriodicLimitResponse);
		int getOpStatusCode = updateResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.UPDATE, ActivityStatusEnum.FAILED,
					"transactoin fee slab update failed. transactionFeeSlabId: " + slabId);
			return updateResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
				EventEnum.UPDATE, ActivityStatusEnum.SUCCESSFUL,
				"transaction fee slab update success. transactionFeeSlabId: " + slabId);
		return updateResponseJSON;
	}

	public JSONObject getRecordsForServiceID(String serviceID, String authToken,
			DataControllerRequest requestInstance) {

		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.put(ODataQueryConstants.FILTER, "Service_id eq '" + serviceID + "'");
		String readEndpointResponse = Executor.invokeService(
				ServiceURLEnum.TRANSACTIONFEESERVICE_VIEW_READ,
				postParametersMap, null, requestInstance);
		return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

	}
}