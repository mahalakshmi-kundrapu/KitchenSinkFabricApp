package com.kony.adminconsole.service.limitsandfees.periodiclimitusergroup;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
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

public class PeriodLimitUserGroupUpdate implements JavaService2 {
	private static final Logger LOG = Logger.getLogger(PeriodLimitUserGroupUpdate.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {
		try {
			Result result = new Result();
			String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);

			String groupId = requestInstance.getParameter("groupId");
			String transactionLimitId = requestInstance.getParameter("transactionLimitId");
			String systemUser = requestInstance.getParameter("systemUserId");
			JSONArray periodsLimitArray = CommonUtilities
					.getStringAsJSONArray(requestInstance.getParameter("periodLimits"));

			if (StringUtils.isBlank(groupId)) {
				return ErrorCodeEnum.ERR_20569.setErrorCode(result);
			}

			if (StringUtils.isBlank(transactionLimitId)) {
				return ErrorCodeEnum.ERR_20574.setErrorCode(result);
			}

			if (StringUtils.isBlank(systemUser)) {
				return ErrorCodeEnum.ERR_20571.setErrorCode(result);
			}

			if (periodsLimitArray == null || periodsLimitArray.length() == 0) {
				return ErrorCodeEnum.ERR_20572.setErrorCode(result);
			}
			try {
				PeriodicLimitsValidationUtil validateUtil = new PeriodicLimitsValidationUtil();
				Dataset errors = validateUtil.validateLimits(periodsLimitArray, ServiceURLEnum.PERIOD_READ, authToken,
						requestInstance);
				if (!errors.getAllRecords().isEmpty()) {
					result = ErrorCodeEnum.ERR_20595.setErrorCode(result);
					result.addDataset(errors);
					return result;
				}
				JSONObject response = modifyPeriodicLimits(transactionLimitId, periodsLimitArray, systemUser, authToken,
						requestInstance);
				if (response != null && response.getInt(FabricConstants.OPSTATUS) != 0) {
					return ErrorCodeEnum.ERR_20595.setErrorCode(result);
				}
			} catch (ClassCastException e) {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
						ActivityStatusEnum.FAILED, "User Group Period Limit create failed. errmsg: " + e.getMessage());
			}
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.SUCCESSFUL, "Group Entitlement create Successful.");
			return result;
		} catch (Exception e) {
			Result errorResult = new Result();
			LOG.debug("Runtime Exception.Exception Trace:", e);
			ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
			return errorResult;
		}
	}

	private JSONObject modifyPeriodicLimits(String transactionLimitId, JSONArray periodsLimitArray, String systemUser,
			String authToken, DataControllerRequest requestInstance) {
		JSONObject response = null;
		String periodicLimitId;
		for (int i = 0; i < periodsLimitArray.length(); i++) {
			JSONObject temp = periodsLimitArray.getJSONObject(i);
			periodicLimitId = temp.getString("periodicLimitId");
			String periodId = temp.getString("periodId");
			String periodLimit = temp.get("periodLimit").toString().trim();
			boolean isDelete = temp.optString("deleteFlag").equalsIgnoreCase("Y");
			if (isDelete) {
				response = deletePeriodicLimit(periodicLimitId, authToken, requestInstance);
			} else if (StringUtils.isBlank(periodicLimitId)) {
				response = createPeriodicLimit(transactionLimitId, periodId, periodLimit, systemUser, authToken,
						requestInstance);
			} else {
				response = updatePeriodicLimit(transactionLimitId, periodicLimitId, periodId, periodLimit, systemUser,
						authToken, requestInstance);
			}
			if (response != null && response.getInt(FabricConstants.OPSTATUS) != 0) {
				return response;
			}
		}
		return response;
	}

	private JSONObject deletePeriodicLimit(String periodicLimitId, String authToken,
			DataControllerRequest requestInstance) {
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", periodicLimitId);
		String deleteResponse = Executor.invokeService(ServiceURLEnum.PERIODICLIMIT_DELETE, postParametersMap, null,
				requestInstance);
		JSONObject deleteResponseJSON = CommonUtilities.getStringAsJSONObject(deleteResponse);
		return deleteResponseJSON;
	}

	private JSONObject createPeriodicLimit(String transactionLimitId, String periodId, String periodLimit,
			String systemUser, String authToken, DataControllerRequest requestInstance) {
		String periodicLimitId = CommonUtilities.getNewId().toString();
		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", periodicLimitId);
		postParametersMap.put("TransactionLimit_id", transactionLimitId);
		postParametersMap.put("Period_id", periodId);
		postParametersMap.put("Code", "NULL");
		postParametersMap.put("MaximumLimit", periodLimit);
		postParametersMap.put("Currency", "INR");
		postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("modifiedby", "NULL");
		postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("softdeleteflag", "0");
		String createPeriodicLimitResponse = Executor.invokeService(ServiceURLEnum.PERIODICLIMIT_CREATE,
				postParametersMap, null, requestInstance);

		createResponseJSON = CommonUtilities.getStringAsJSONObject(createPeriodicLimitResponse);
		int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.FAILED, "Periodic Limit create failed. periodicLimitId: " + periodicLimitId);
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
				ActivityStatusEnum.SUCCESSFUL, "Periodic Limit create Successful. periodicLimitId: " + periodicLimitId);
		return createResponseJSON;
	}

	private JSONObject updatePeriodicLimit(String transactionLimitId, String periodicLimitId, String periodId,
			String periodLimit, String systemUser, String authToken, DataControllerRequest requestInstance) {
		JSONObject upadteResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", periodicLimitId);
		postParametersMap.put("TransactionLimit_id", transactionLimitId);
		postParametersMap.put("Period_id", periodId);
		postParametersMap.put("Code", "NULL");
		postParametersMap.put("MaximumLimit", periodLimit);
		postParametersMap.put("Currency", "INR");
		postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("modifiedby", "NULL");
		postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("softdeleteflag", "0");
		String updatePeriodicLimitResponse = Executor.invokeService(ServiceURLEnum.PERIODICLIMIT_UPDATE,
				postParametersMap, null, requestInstance);

		upadteResponseJSON = CommonUtilities.getStringAsJSONObject(updatePeriodicLimitResponse);
		int getOpStatusCode = upadteResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.UPDATE,
					ActivityStatusEnum.FAILED, "Periodic Limit create failed. periodicLimitId: " + periodicLimitId);
			return upadteResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.UPDATE,
				ActivityStatusEnum.SUCCESSFUL, "Periodic Limit create Successful. periodicLimitId: " + periodicLimitId);
		return upadteResponseJSON;
	}
}