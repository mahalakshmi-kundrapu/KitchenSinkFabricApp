package com.kony.adminconsole.service.limitsandfees.periodiclimitusergroup;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.config.EnvironmentConfiguration;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.service.limitsandfees.PeriodicLimitsValidationUtil;
import com.kony.adminconsole.service.limitsandfees.ValidatePeriodicLimitUtil;
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

public class PeriodLimitUserGroupAdd implements JavaService2 {
	private static final Logger LOG = Logger.getLogger(PeriodLimitUserGroupAdd.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {
		try {

			String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
			Result result = new Result();

			String groupId = requestInstance.getParameter("groupId");
			String serviceId = requestInstance.getParameter("serviceId");
			String systemUser = requestInstance.getParameter("systemUserId");
			JSONArray periodsLimitArray = CommonUtilities
					.getStringAsJSONArray(requestInstance.getParameter("periodLimits"));
			String transactionFeeId = requestInstance.getParameter("transactionFeeId");

			if (StringUtils.isBlank(groupId)) {
				return ErrorCodeEnum.ERR_20569.setErrorCode(null);
			}

			if (StringUtils.isBlank(serviceId)) {
				return ErrorCodeEnum.ERR_20570.setErrorCode(null);
			}

			if (StringUtils.isBlank(systemUser)) {
				return ErrorCodeEnum.ERR_20571.setErrorCode(null);
			}

			if (periodsLimitArray == null || periodsLimitArray.length() == 0) {
				return ErrorCodeEnum.ERR_20572.setErrorCode(null);
			}
			if (StringUtils.isBlank(transactionFeeId)) {
				return ErrorCodeEnum.ERR_20573.setErrorCode(null);
			}

			try {
				String baseURL = EnvironmentConfiguration.AC_HOST_URL.getValue(requestInstance);
				ValidatePeriodicLimitUtil util = new ValidatePeriodicLimitUtil();
				JSONObject validateJSON = util.validateLimits(serviceId, periodsLimitArray, baseURL, authToken,
						requestInstance);

				if (validateJSON == null) {

					String transactionLimitId = CommonUtilities.getNewId().toString();

					JSONObject createResponseJSON = createTransactionLimit(transactionLimitId, serviceId, groupId,
							systemUser, authToken, requestInstance);
					if (createResponseJSON != null && createResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
						return ErrorCodeEnum.ERR_20841.setErrorCode(null);
					}
					PeriodicLimitsValidationUtil validateUtil = new PeriodicLimitsValidationUtil();
					Dataset errors = validateUtil.validateLimits(periodsLimitArray, ServiceURLEnum.PERIOD_READ,
							authToken, requestInstance);
					if (!errors.getAllRecords().isEmpty()) {
						result = ErrorCodeEnum.ERR_20595.setErrorCode(result);
						result.addDataset(errors);
						return result;
					}
					JSONObject createPeriodicLimitResponse = createPeriodicLimit(transactionLimitId, periodsLimitArray,
							systemUser, authToken, requestInstance);
					if (createPeriodicLimitResponse != null
							&& createPeriodicLimitResponse.getInt(FabricConstants.OPSTATUS) != 0) {
						return ErrorCodeEnum.ERR_20837.setErrorCode(null);
					}
					JSONObject createGroupEntitlementResponse = createGroupEntitlement(groupId, serviceId,
							transactionLimitId, transactionFeeId, systemUser, authToken, requestInstance);
					if (createGroupEntitlementResponse != null
							&& createGroupEntitlementResponse.getInt(FabricConstants.OPSTATUS) != 0) {
						return ErrorCodeEnum.ERR_20421.setErrorCode(null);
					}
					result.addParam(new Param("transactionLimitId", transactionLimitId, FabricConstants.STRING));
				} else {
					return ErrorCodeEnum.ERR_20568.setErrorCode(null);
				}

			} catch (ClassCastException e) {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
						ActivityStatusEnum.FAILED, "User group create failed. errmsg: " + e.getMessage());
				return ErrorCodeEnum.ERR_20001.setErrorCode(result);
			} catch (Exception e) {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
						ActivityStatusEnum.FAILED, "User group create failed. errmsg: " + e.getMessage());
				return ErrorCodeEnum.ERR_20001.setErrorCode(result);
			}
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.SUCCESSFUL, "User group create successful.");
			return result;
		} catch (Exception e) {
			Result errorResult = new Result();
			LOG.debug("Runtime Exception.Exception Trace:", e);
			ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
			return errorResult;
		}
	}

	private JSONObject createTransactionLimit(String transactionLimitId, String serviceId, String groupId,
			String systemUser, String authToken, DataControllerRequest requestInstance) {
		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", transactionLimitId);
		postParametersMap.put("description", serviceId);
		postParametersMap.put("createdby", systemUser);
		postParametersMap.put("modifiedby", "NULL");
		postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("softdeleteflag", "0");
		String createTransactionLimitResponse = Executor.invokeService(ServiceURLEnum.TRANSACTIONLIMIT_CREATE,
				postParametersMap, null, requestInstance);

		createResponseJSON = CommonUtilities.getStringAsJSONObject(createTransactionLimitResponse);
		int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.FAILED,
					"Transaction Limit create failed. transactionLimitId: " + transactionLimitId);
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
				ActivityStatusEnum.SUCCESSFUL,
				"Transaction Limit create successful. transactionLimitId: " + transactionLimitId);

		return createResponseJSON;
	}

	private JSONObject createPeriodicLimit(String transactionLimitId, JSONArray periodsLimitArray, String systemUser,
			String authToken, DataControllerRequest requestInstance) {
		JSONObject createResponseJSON = null;
		String periodicLimitId = null;
		for (int i = 0; i < periodsLimitArray.length(); i++) {
			JSONObject temp = periodsLimitArray.getJSONObject(i);
			periodicLimitId = CommonUtilities.getNewId().toString();
			String periodId = temp.getString("periodId");
			String periodLimit = temp.get("periodLimit").toString().trim();

			if (StringUtils.isBlank(periodId)) {
				periodId = CommonUtilities.getNewId().toString();
				JSONObject createCustomPeriodResponse = createCustomPeriod(temp, periodId, systemUser, authToken,
						requestInstance);
				if (createCustomPeriodResponse != null
						&& createCustomPeriodResponse.getInt(FabricConstants.OPSTATUS) != 0) {
					return createCustomPeriodResponse;
				}

			}
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
					ActivityStatusEnum.SUCCESSFUL,
					"Periodic Limit create successful. periodicLimitId: " + periodicLimitId);
		}
		return createResponseJSON;
	}

	private JSONObject createCustomPeriod(JSONObject period, String periodId, String systemUser, String authToken,
			DataControllerRequest requestInstance) {
		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", periodId);
		postParametersMap.put("Name", period.getString("periodName"));
		postParametersMap.put("dayCount", period.get("dayCount").toString());
		postParametersMap.put("order", "NULL");
		postParametersMap.put("code", "NULL");
		postParametersMap.put("createdby", "NULL");
		postParametersMap.put("modifiedby", "NULL");
		postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("softdeleteflag", "0");
		String createPeriodResponse = Executor.invokeService(ServiceURLEnum.PERIOD_CREATE, postParametersMap, null,
				requestInstance);

		createResponseJSON = CommonUtilities.getStringAsJSONObject(createPeriodResponse);
		int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.FAILED, "Period create failed. periodId: " + periodId);
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
				ActivityStatusEnum.SUCCESSFUL, "Period create successful. periodId: " + periodId);
		return createResponseJSON;
	}

	private JSONObject createGroupEntitlement(String groupId, String serviceId, String transactionLimitId,
			String transactionFeeId, String systemUser, String authToken, DataControllerRequest requestInstance) {
		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		String groupEntitlementId = CommonUtilities.getNewId().toString();
		postParametersMap.put("id", groupEntitlementId);
		postParametersMap.put("Group_id", groupId);
		postParametersMap.put("Service_id", serviceId);
		postParametersMap.put("TransactionFee_id", transactionFeeId);
		postParametersMap.put("TransactionLimit_id", transactionLimitId);
		postParametersMap.put("createdby", "NULL");
		postParametersMap.put("modifiedby", "NULL");
		postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("softdeleteflag", "0");
		String createGroupEntitlementResponse = Executor.invokeService(ServiceURLEnum.GROUPENTITLEMENT_CREATE,
				postParametersMap, null, requestInstance);

		createResponseJSON = CommonUtilities.getStringAsJSONObject(createGroupEntitlementResponse);
		int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.FAILED,
					"Group Entitlement create failed. groupEntitlementId: " + groupEntitlementId);
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
				ActivityStatusEnum.SUCCESSFUL,
				"Group Entitlement create successful. groupEntitlementId: " + groupEntitlementId);
		return createResponseJSON;
	}
}