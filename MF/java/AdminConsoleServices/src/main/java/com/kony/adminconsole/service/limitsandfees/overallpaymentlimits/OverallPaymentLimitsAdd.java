package com.kony.adminconsole.service.limitsandfees.overallpaymentlimits;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.handler.AuditHandler;
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

public class OverallPaymentLimitsAdd implements JavaService2 {
	private static final Logger LOG = Logger.getLogger(OverallPaymentLimitsAdd.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {
		try {
			String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
			JSONArray serviceIds = CommonUtilities.getStringAsJSONArray(requestInstance.getParameter("serviceIds"));
			JSONArray periodsLimitArray = CommonUtilities
					.getStringAsJSONArray(requestInstance.getParameter("periodLimits"));
			String statusId = requestInstance.getParameter("statusId");
			String transactionGroupName = requestInstance.getParameter("transactionGroupName");
			String systemUser = requestInstance.getParameter("systemUserId");
			Result result = new Result();

			if (StringUtils.isBlank(systemUser)) {
				return ErrorCodeEnum.ERR_20571.setErrorCode(result);
			}

			if (periodsLimitArray == null || periodsLimitArray.length() == 0) {
				return ErrorCodeEnum.ERR_20572.setErrorCode(result);
			}

			if (serviceIds == null || serviceIds.length() == 0) {
				return ErrorCodeEnum.ERR_20589.setErrorCode(result);
			}
			try {

				ValidateOverallPaymentLimits validateUtil = new ValidateOverallPaymentLimits();
				Dataset errors = validateUtil.validateLimits(null, periodsLimitArray, ServiceURLEnum.PERIOD_READ,
						authToken, requestInstance);
				if (!errors.getAllRecords().isEmpty()) {
					result = ErrorCodeEnum.ERR_20595.setErrorCode(result);
					result.addDataset(errors);
					return result;
				}

				String transactionLimitId = CommonUtilities.getNewId().toString();
				String transactionGroupId = CommonUtilities.getNewId().toString();

				JSONObject createResponseJSON = createTransactionLimit(transactionLimitId, systemUser, authToken,
						requestInstance);
				if (createResponseJSON != null && createResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
					return ErrorCodeEnum.ERR_20841.setErrorCode(result);
				}
				JSONObject createPeriodicLimitResponse = createPeriodicLimit(transactionLimitId, periodsLimitArray,
						systemUser, authToken, requestInstance);
				if (createPeriodicLimitResponse != null
						&& createPeriodicLimitResponse.getInt(FabricConstants.OPSTATUS) != 0) {
					return ErrorCodeEnum.ERR_20837.setErrorCode(result);
				}
				JSONObject createTransactionGroupResponse = createTransactionGroup(transactionGroupId,
						transactionLimitId, transactionGroupName, statusId, systemUser, authToken, requestInstance);
				if (createTransactionGroupResponse != null
						&& createTransactionGroupResponse.getInt(FabricConstants.OPSTATUS) != 0) {
					return ErrorCodeEnum.ERR_20833.setErrorCode(result);
				}
				JSONObject transactionGroupServiceResponse = createTransactionGroupService(transactionGroupId,
						serviceIds, systemUser, authToken, requestInstance);
				if (transactionGroupServiceResponse != null
						&& transactionGroupServiceResponse.getInt(FabricConstants.OPSTATUS) != 0) {
					return ErrorCodeEnum.ERR_20825.setErrorCode(result);
				}
				result.addParam(new Param("transactionGroupId", transactionGroupId, FabricConstants.STRING));
			} catch (Exception e) {
				return ErrorCodeEnum.ERR_20001.setErrorCode(result);
			}
			return result;
		} catch (Exception e) {
			Result errorResult = new Result();
			LOG.debug("Runtime Exception.Exception Trace:", e);
			ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
			return errorResult;
		}
	}

	private JSONObject createTransactionLimit(String transactionLimitId, String systemUser, String authToken,
			DataControllerRequest requestInstance) {
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", transactionLimitId);
		postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("createdby", systemUser);
		String createTransactionLimitResponse = Executor.invokeService(ServiceURLEnum.TRANSACTIONLIMIT_CREATE,
				postParametersMap, null, requestInstance);

		JSONObject createResponseJSON = CommonUtilities.getStringAsJSONObject(createTransactionLimitResponse);
		int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.FAILED, "overallPaymentLimts TransactionLimit Create Failed.");
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
				ActivityStatusEnum.SUCCESSFUL, "overallPaymentLimts TransactionLimit Create successful.");

		return createResponseJSON;
	}

	private JSONObject createPeriodicLimit(String transactionLimitId, JSONArray periodsLimitArray, String systemUser,
			String authToken, DataControllerRequest requestInstance) {
		JSONObject createResponseJSON = null;
		for (int i = 0; i < periodsLimitArray.length(); i++) {

			JSONObject temp = periodsLimitArray.getJSONObject(i);

			String periodicLimitId = CommonUtilities.getNewId().toString();
			String periodId = temp.getString("periodId");
			String periodLimit = temp.get("periodLimit").toString().trim();

			Map<String, String> postParametersMap = new HashMap<String, String>();
			postParametersMap.clear();
			postParametersMap.put("id", periodicLimitId);
			postParametersMap.put("TransactionLimit_id", transactionLimitId);
			postParametersMap.put("Period_id", periodId);
			postParametersMap.put("Code", "NULL");
			postParametersMap.put("MaximumLimit", periodLimit);
			postParametersMap.put("Currency", "INR");
			postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
			postParametersMap.put("createdby", systemUser);

			String createPeriodicLimitResponse = Executor.invokeService(ServiceURLEnum.PERIODICLIMIT_CREATE,
					postParametersMap, null, requestInstance);

			createResponseJSON = CommonUtilities.getStringAsJSONObject(createPeriodicLimitResponse);
			int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
			if (getOpStatusCode != 0) {
				// rollback logic
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
						ActivityStatusEnum.FAILED, "Periodic limit create failed. periodicLimitId: " + periodicLimitId);
				return createResponseJSON;
			}
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.SUCCESSFUL,
					"Periodic limit create successful. periodicLimitId: " + periodicLimitId);
		}
		return createResponseJSON;
	}

	private JSONObject createTransactionGroup(String transactionGroupId, String transactionLimitId,
			String transactionGroupName, String statusId, String systemUser, String authToken,
			DataControllerRequest requestInstance) {
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", transactionGroupId);
		postParametersMap.put("Name", transactionGroupName);
		postParametersMap.put("TransactionLimit_id", transactionLimitId);
		if (StringUtils.isNotBlank(statusId)) {
			postParametersMap.put("Status_id", statusId);
		}
		postParametersMap.put("createdby", systemUser);
		postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
		String createTransactionLimitResponse = Executor.invokeService(ServiceURLEnum.TRANSACTIONGROUP_CREATE,
				postParametersMap, null, requestInstance);

		JSONObject createResponseJSON = CommonUtilities.getStringAsJSONObject(createTransactionLimitResponse);
		int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.FAILED,
					"Transaction group create failed. transactionGroupId: " + transactionGroupId);
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
				ActivityStatusEnum.SUCCESSFUL,
				"Transaction group create successful. transactionGroupId: " + transactionGroupId);

		return createResponseJSON;
	}

	private JSONObject createTransactionGroupService(String transactionGroupId, JSONArray serviceIds, String systemUser,
			String authToken, DataControllerRequest requestInstance) {
		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		for (int i = 0; i < serviceIds.length(); i++) {

			String serviceId = serviceIds.getString(i);
			String transactionGroupServiceId = CommonUtilities.getNewId().toString();

			postParametersMap.clear();
			postParametersMap.put("id", transactionGroupServiceId);
			postParametersMap.put("TransactionGroup_id", transactionGroupId);
			postParametersMap.put("Service_id", serviceId);
			postParametersMap.put("createdby", systemUser);
			postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
			String createTransactionLimitResponse = Executor.invokeService(
					ServiceURLEnum.TRANSACTIONGROUPSERVICE_CREATE, postParametersMap, null, requestInstance);

			createResponseJSON = CommonUtilities.getStringAsJSONObject(createTransactionLimitResponse);
			int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
			if (getOpStatusCode != 0) {
				// rollback logic
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
						ActivityStatusEnum.FAILED,
						"Transaction group service create failed. transactionGroupServiceId: "
								+ transactionGroupServiceId);
				return createResponseJSON;
			}
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.SUCCESSFUL,
					"Transaction group service create successful. transactionGroupServiceId: "
							+ transactionGroupServiceId);
		}
		return createResponseJSON;
	}
}