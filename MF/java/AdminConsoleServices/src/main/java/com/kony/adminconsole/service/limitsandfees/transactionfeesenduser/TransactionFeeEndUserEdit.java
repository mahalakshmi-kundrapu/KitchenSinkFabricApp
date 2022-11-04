package com.kony.adminconsole.service.limitsandfees.transactionfeesenduser;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
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

public class TransactionFeeEndUserEdit implements JavaService2 {

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {
		Result result = new Result();
		Param statusParam;
		String fees = "";
		String minAmount = "";
		String maxAmount = "";
		String customerId = "";
		String systemUser = "";
		String deleteFlag = "";
		String transactionFeeId = "";
		String transactionFeesSlabId = "";
		JSONArray transactionFeesArray = null;

		customerId = requestInstance.getParameter("customerId");
		if (StringUtils.isBlank(customerId)) {
			return ErrorCodeEnum.ERR_20565.setErrorCode(null);
		}

		transactionFeeId = requestInstance.getParameter("transactionFeeId");
		if (StringUtils.isBlank(transactionFeeId)) {
			return ErrorCodeEnum.ERR_20573.setErrorCode(null);
		}
		systemUser = requestInstance.getParameter("systemUserId");
		if (requestInstance.getParameter("transactionFeesArray") != null) {
			transactionFeesArray = new JSONArray(requestInstance.getParameter("transactionFeesArray"));
		} else {
			return ErrorCodeEnum.ERR_20581.setErrorCode(null);
		}
		try {

			ValidateTransactionFeeLimits vtflObj = new ValidateTransactionFeeLimits();
			Dataset validationDS = vtflObj.validateFeeLimits(new Dataset(), transactionFeesArray);
			if (!validationDS.getAllRecords().isEmpty()) {
				result.addDataset(validationDS);
				return result;
			}

			for (int i = 0; i < transactionFeesArray.length(); i++) {
				transactionFeesSlabId = ((JSONObject) transactionFeesArray.get(i)).get("transactionFeesSlabId").toString().trim();
				minAmount = ((JSONObject) transactionFeesArray.get(i)).get("minAmount").toString().trim();
				maxAmount = ((JSONObject) transactionFeesArray.get(i)).get("maxAmount").toString().trim();
				fees = ((JSONObject) transactionFeesArray.get(i)).get("fees").toString().trim();
				deleteFlag = ((JSONObject) transactionFeesArray.get(i)).get("deleteFlag").toString().trim();

				if (("Y").equalsIgnoreCase(deleteFlag)) {
					// delete
					JSONObject deletePeriodicLimitResponseJSON = deleteTransactionFeesSlab(transactionFeesSlabId,
							systemUser, requestInstance);
					if (deletePeriodicLimitResponseJSON != null && deletePeriodicLimitResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						return ErrorCodeEnum.ERR_20808.setErrorCode(null);
					}
					continue;
				}

				if (transactionFeesSlabId == null || transactionFeesSlabId.isEmpty()
						|| transactionFeesSlabId.trim().length() == 0) {
					transactionFeesSlabId = CommonUtilities.getNewId().toString();
					JSONObject createTransactionFeesSlabResponseJSON = createTransactionFeesSlab(transactionFeesSlabId,
							transactionFeeId, minAmount, maxAmount, fees, systemUser, requestInstance);
					if (createTransactionFeesSlabResponseJSON != null && createTransactionFeesSlabResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						return ErrorCodeEnum.ERR_20805.setErrorCode(null);
					}
					continue;
				} else {
					// update
					JSONObject updatePeriodicLimitResponseJSON = updateTransactionFeesSlab(transactionFeesSlabId,
							transactionFeeId, minAmount, maxAmount, fees, systemUser, requestInstance);
					if (updatePeriodicLimitResponseJSON != null && updatePeriodicLimitResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						return ErrorCodeEnum.ERR_20807.setErrorCode(null);
					}
					continue;
				}
			}
		} catch (Exception e) {
			statusParam = new Param("Status", "EXCEPTION", FabricConstants.STRING);
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.UPDATE, ActivityStatusEnum.FAILED,
					"Transaction feeslab update failed. errmsg: " + e.getMessage());
			result.addParam(statusParam);
			return result;
		}
		statusParam = new Param("Status", "SUCCESSFUL", FabricConstants.STRING);
		result.addParam(statusParam);
		return result;
	}

	private JSONObject createTransactionFeesSlab(String transactionFeeSlabId, String transactionFeeId, String minAmount,
			String maxAmount, String fees, String systemUser, DataControllerRequest requestInstance) {
		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", transactionFeeSlabId);
		postParametersMap.put("TransactionFee_id", transactionFeeId);
		postParametersMap.put("MinimumTransactionValue", minAmount);
		postParametersMap.put("MaximumTransactionValue", maxAmount);
		postParametersMap.put("Currency", "INR");
		postParametersMap.put("Fees", fees);
		postParametersMap.put("createdby", systemUser);
		postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("modifiedby", "NULL");
		postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("softdeleteflag", "0");
		String createTransactionFeesSlabResponse = Executor.invokeService(
				ServiceURLEnum.TRANSACTIONFEESLAB_CREATE, postParametersMap, null, requestInstance);
		createResponseJSON = CommonUtilities.getStringAsJSONObject(createTransactionFeesSlabResponse);
		int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.CREATE, ActivityStatusEnum.FAILED,
					"Transaction feeslab create failed. transactionFeeSlabId: " + transactionFeeSlabId);
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
				EventEnum.CREATE, ActivityStatusEnum.SUCCESSFUL,
				"Transaction feeslab create successful. transactionFeeSlabId: " + transactionFeeSlabId);
		return createResponseJSON;
	}

	private JSONObject updateTransactionFeesSlab(String transactionFeeSlabId, String transactionFeeId, String minAmount,
			String maxAmount, String fees, String systemUser, DataControllerRequest requestInstance) {
		JSONObject updateResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", transactionFeeSlabId);
		postParametersMap.put("TransactionFee_id", transactionFeeId);
		postParametersMap.put("MinimumTransactionValue", minAmount);
		postParametersMap.put("MaximumTransactionValue", maxAmount);
		postParametersMap.put("Currency", "INR");
		postParametersMap.put("Fees", fees);
		postParametersMap.put("modifiedby", systemUser);
		postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("softdeleteflag", "0");

		String updateTransactionFeesResponse = Executor.invokeService(
				ServiceURLEnum.TRANSACTIONFEESLAB_UPDATE, postParametersMap, null, requestInstance);

		updateResponseJSON = CommonUtilities.getStringAsJSONObject(updateTransactionFeesResponse);
		int getOpStatusCode = updateResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.UPDATE, ActivityStatusEnum.FAILED,
					"Transaction feeslab update failed. transactionFeeSlabId: " + transactionFeeSlabId);
			return updateResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
				EventEnum.UPDATE, ActivityStatusEnum.SUCCESSFUL,
				"Transaction feeslab update successful. transactionFeeSlabId: " + transactionFeeSlabId);
		return updateResponseJSON;
	}

	private JSONObject deleteTransactionFeesSlab(String transactionFeesSlabId, String systemUser,
			DataControllerRequest requestInstance) {
		JSONObject deleteResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", transactionFeesSlabId);
		String deleteTransactionFeesSlabResponse = Executor.invokeService(
				ServiceURLEnum.TRANSACTIONFEESLAB_DELETE, postParametersMap, null, requestInstance);

		deleteResponseJSON = CommonUtilities.getStringAsJSONObject(deleteTransactionFeesSlabResponse);
		int getOpStatusCode = deleteResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.DELETE, ActivityStatusEnum.FAILED,
					"Transaction feeslab delete failed. transactionFeesSlabId: " + transactionFeesSlabId);
			return deleteResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
				EventEnum.DELETE, ActivityStatusEnum.SUCCESSFUL,
				"Transaction feeslab delete successful. transactionFeesSlabId: " + transactionFeesSlabId);
		return deleteResponseJSON;
	}
}