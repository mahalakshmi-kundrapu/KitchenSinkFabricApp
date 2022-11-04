package com.kony.adminconsole.service.limitsandfees.periodiclimitservice;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.service.limitsandfees.PeriodicLimitsValidationUtil;
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
import com.konylabs.middleware.dataobject.Result;

public class PeriodLimitServiceUpdate implements JavaService2 {

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {

		Result result = new Result();
		String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
		Result processedResult = new Result();

		String transactionLimitId = "";
		String periodicLimitId = "";
		String periodLimit = "";
		String periodId = "";
		String systemUser = "";
		String deleteFlag = "";
		JSONArray periodsLimitArray = null;
		if (requestInstance.getParameter("transactionLimitId") != null) {
			transactionLimitId = requestInstance.getParameter("transactionLimitId");
		} else {
			return ErrorCodeEnum.ERR_20574.setErrorCode(result);
		}
		if (requestInstance.getParameter("systemUserId") != null) {
			systemUser = requestInstance.getParameter("systemUserId");
		}
		if (requestInstance.getParameter("periodLimits") != null) {
			periodsLimitArray = new JSONArray(requestInstance.getParameter("periodLimits"));
		} else {
			return ErrorCodeEnum.ERR_20575.setErrorCode(result);
		}

		try {

			for (int i = 0; i < periodsLimitArray.length(); i++) {
				periodId = ((JSONObject) periodsLimitArray.get(i)).get("periodId").toString().trim();
				periodLimit = ((JSONObject) periodsLimitArray.get(i)).get("periodLimit").toString().trim();
				periodicLimitId = ((JSONObject) periodsLimitArray.get(i)).get("periodicLimitId").toString().trim();
				deleteFlag = ((JSONObject) periodsLimitArray.get(i)).get("deleteFlag").toString().trim();

				if (("Y").equalsIgnoreCase(deleteFlag)) {
					// delete
					JSONObject deletePeriodicLimitResponseJSON = deletePeriodicLimit(periodicLimitId, authToken,
							systemUser, requestInstance);
					if (deletePeriodicLimitResponseJSON != null && deletePeriodicLimitResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						return ErrorCodeEnum.ERR_20840.setErrorCode(result);
					}
					continue;
				}

				if (periodicLimitId == null || periodicLimitId.isEmpty() || periodicLimitId.trim().length() == 0) {
					PeriodicLimitsValidationUtil validateUtil = new PeriodicLimitsValidationUtil();
					Dataset errors = validateUtil.validateLimits(periodsLimitArray,
							ServiceURLEnum.PERIOD_READ, authToken, requestInstance);
					if (!errors.getAllRecords().isEmpty()) {
						result = ErrorCodeEnum.ERR_20595.setErrorCode(result);
						result.addDataset(errors);
						return result;
					}
					periodicLimitId = CommonUtilities.getNewId().toString();
					JSONObject createPeriodicLimitResponseJSON = createPeriodicLimit(periodicLimitId,
							transactionLimitId, periodId, periodLimit, authToken, systemUser, requestInstance);
					if (createPeriodicLimitResponseJSON != null && createPeriodicLimitResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						return ErrorCodeEnum.ERR_20837.setErrorCode(result);
					}
					continue;
				} else {
					// update
					PeriodicLimitsValidationUtil validateUtil = new PeriodicLimitsValidationUtil();
					Dataset errors = validateUtil.validateLimits(periodsLimitArray,
							ServiceURLEnum.PERIOD_READ, authToken, requestInstance);
					if (!errors.getAllRecords().isEmpty()) {
						result = ErrorCodeEnum.ERR_20595.setErrorCode(result);
						result.addDataset(errors);
						return result;
					}
					JSONObject updatePeriodicLimitResponseJSON = updatePeriodicLimit(periodicLimitId,
							transactionLimitId, periodId, periodLimit, authToken, systemUser, requestInstance);
					if (updatePeriodicLimitResponseJSON != null && updatePeriodicLimitResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						return ErrorCodeEnum.ERR_20839.setErrorCode(result);
					}
					continue;
				}
			}
			return result;
		} catch (Exception e) {
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.UPDATE, ActivityStatusEnum.FAILED,
					"Periodic Limit update failed. errmsg: " + e.getMessage());
			return ErrorCodeEnum.ERR_20567.setErrorCode(processedResult);
		}
	}

	private JSONObject updatePeriodicLimit(String periodicLimitId, String transactionLimitId, String periodId,
			String periodLimit, String authToken, String systemUser, DataControllerRequest requestInstance) {
		JSONObject updateResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", periodicLimitId);
		postParametersMap.put("TransactionLimit_id", transactionLimitId);
		postParametersMap.put("Period_id", periodId);
		postParametersMap.put("MaximumLimit", periodLimit);
		postParametersMap.put("modifiedby", systemUser);
		postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("softdeleteflag", "0");

		String updatePeriodicLimitResponse = Executor.invokeService(
				ServiceURLEnum.PERIODICLIMIT_UPDATE,
				postParametersMap, null, requestInstance);

		updateResponseJSON = CommonUtilities.getStringAsJSONObject(updatePeriodicLimitResponse);
		int getOpStatusCode = updateResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.UPDATE, ActivityStatusEnum.FAILED,
					"Periodic Limit update failed. transactionLimitId: " + transactionLimitId);
			return updateResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
				EventEnum.UPDATE, ActivityStatusEnum.SUCCESSFUL,
				"Periodic Limit update failed. transactionLimitId: " + transactionLimitId);
		return updateResponseJSON;
	}

	private JSONObject createPeriodicLimit(String periodicLimitId, String transactionLimitId, String periodId,
			String periodLimit, String authToken, String systemUser, DataControllerRequest requestInstance) {
		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", periodicLimitId);
		postParametersMap.put("TransactionLimit_id", transactionLimitId);
		postParametersMap.put("Period_id", periodId);
		postParametersMap.put("Code", "Something");
		postParametersMap.put("MaximumLimit", periodLimit);
		postParametersMap.put("Currency", "INR");
		postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("modifiedby", "NULL");
		postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("softdeleteflag", "0");

		String createPeriodicLimitResponse = Executor.invokeService(
				ServiceURLEnum.PERIODICLIMIT_CREATE,
				postParametersMap, null, requestInstance);

		createResponseJSON = CommonUtilities.getStringAsJSONObject(createPeriodicLimitResponse);
		int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.CREATE, ActivityStatusEnum.FAILED,
					"Periodic Limit create failed. periodicLimitId: " + periodicLimitId);
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
				EventEnum.CREATE, ActivityStatusEnum.SUCCESSFUL,
				"Periodic Limit create success. periodicLimitId: " + periodicLimitId);
		return createResponseJSON;
	}

	private JSONObject deletePeriodicLimit(String periodicLimitId, String authToken, String systemUser,
			DataControllerRequest requestInstance) {
		JSONObject updateResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", periodicLimitId);
		String updatePeriodicLimitResponse = Executor.invokeService(
				ServiceURLEnum.PERIODICLIMIT_DELETE,
				postParametersMap, null, requestInstance);

		updateResponseJSON = CommonUtilities.getStringAsJSONObject(updatePeriodicLimitResponse);
		int getOpStatusCode = updateResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.DELETE, ActivityStatusEnum.FAILED,
					"Periodic Limit delete failed. transactionLimitId: " + periodicLimitId);
			return updateResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
				EventEnum.DELETE, ActivityStatusEnum.SUCCESSFUL,
				"Periodic Limit delete success. transactionLimitId: " + periodicLimitId);
		return updateResponseJSON;
	}
}