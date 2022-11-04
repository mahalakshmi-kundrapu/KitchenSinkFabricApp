package com.kony.adminconsole.service.limitsandfees.periodiclimitenduser;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
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
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class PeriodLimitEndUserAdd implements JavaService2 {
	private static final Logger LOG = Logger.getLogger(PeriodLimitEndUserAdd.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {
		try {

			Result result = new Result();
			String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
			Result processedResult = new Result();
			String customerEntitlementId = CommonUtilities.getNewId().toString();
			String transactionLimitId = CommonUtilities.getNewId().toString();
			String customerId = "";
			String serviceId = "";
			String transactionFeeId = "";

			String systemUser = "";
			JSONArray periodsLimitArray = null;
			if (requestInstance.getParameter("customerId") != null) {
				customerId = requestInstance.getParameter("customerId");
			} else {
				return ErrorCodeEnum.ERR_20565.setErrorCode(processedResult);
			}
			if (requestInstance.getParameter("serviceId") != null) {
				serviceId = requestInstance.getParameter("serviceId");
			} else {
				return ErrorCodeEnum.ERR_20566.setErrorCode(processedResult);
			}
			if (requestInstance.getParameter("transactionFeeId") != null) {
				transactionFeeId = requestInstance.getParameter("transactionFeeId");
			}
			if (requestInstance.getParameter("systemUserId") != null) {
				systemUser = requestInstance.getParameter("systemUserId");
			}
			if (requestInstance.getParameter("periodLimits") != null) {
				periodsLimitArray = new JSONArray(requestInstance.getParameter("periodLimits"));
			}

			try {
				JSONObject getRecordForServiceId = getRecordsForServiceID(serviceId, authToken, requestInstance);
				if (getRecordForServiceId.getInt(FabricConstants.OPSTATUS) != 0) {
					return ErrorCodeEnum.ERR_20384.setErrorCode(processedResult);
				}
				if (validateServiceMaxAndMinLimit(getRecordForServiceId, periodsLimitArray)) {
					JSONObject getPeriodicRecordForServiceId = getPeriodicLimitForServiceID(serviceId, authToken,
							requestInstance);
					if (getRecordForServiceId.getInt(FabricConstants.OPSTATUS) != 0) {
						return ErrorCodeEnum.ERR_20818.setErrorCode(processedResult);
					}
					PeriodicLimitsValidationUtil validateUtil = new PeriodicLimitsValidationUtil();
					Dataset errors = validateUtil.validateLimits(periodsLimitArray, ServiceURLEnum.PERIOD_READ,
							authToken, requestInstance);
					if (!errors.getAllRecords().isEmpty()) {
						result = ErrorCodeEnum.ERR_20595.setErrorCode(result);
						result.addDataset(errors);
						return result;
					}
					if (validateServicePeriodicLimit(getPeriodicRecordForServiceId, periodsLimitArray)) {
						JSONObject createResponseJSON = createTransactionLimit(transactionLimitId, serviceId,
								systemUser, authToken, requestInstance);
						if (createResponseJSON != null && createResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
							return ErrorCodeEnum.ERR_20801.setErrorCode(processedResult);
						}
						JSONObject createPeriodicLimitResponseJSON = createPeriodicLimit(transactionLimitId, customerId,
								serviceId, transactionFeeId, periodsLimitArray, authToken, systemUser, requestInstance);
						if (createPeriodicLimitResponseJSON != null
								&& createPeriodicLimitResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
							return ErrorCodeEnum.ERR_20837.setErrorCode(processedResult);
						}

						JSONObject createCustomerEntitlementResponseJSON = createCustomerEntitlement(
								customerEntitlementId, transactionLimitId, customerId, serviceId, transactionFeeId,
								systemUser, authToken, requestInstance);
						if (createCustomerEntitlementResponseJSON != null
								&& createCustomerEntitlementResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
							return ErrorCodeEnum.ERR_20809.setErrorCode(processedResult);
						}
					} else {
						return ErrorCodeEnum.ERR_20568.setErrorCode(processedResult);
					}

				} else {
					return ErrorCodeEnum.ERR_20567.setErrorCode(processedResult);
				}
				result.addParam(new Param("transactionLimitId", transactionLimitId, FabricConstants.STRING));
			} catch (Exception e) {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
						ActivityStatusEnum.FAILED, "Customer entitlement create failed.");
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

	public boolean validateServicePeriodicLimit(JSONObject getPeriodicRecordForServiceId, JSONArray periodsLimitArray)
			throws Exception {

		try {
			int inputPeriodsLength = periodsLimitArray.length();

			JSONArray servicePeriodArray = (JSONArray) getPeriodicRecordForServiceId.get("periodiclimitservice_view");
			int servicePeriodLength = servicePeriodArray.length();

			for (int i = 0; i < inputPeriodsLength; i++) {
				String periodId = ((JSONObject) periodsLimitArray.get(i)).get("periodId").toString().trim();
				String periodLimit = ((JSONObject) periodsLimitArray.get(i)).get("periodLimit").toString().trim();
				for (int j = 0; j < servicePeriodLength; j++) {

					String servicePeriodId = ((JSONObject) servicePeriodArray.get(i)).get("Period_id").toString()
							.trim();
					if (servicePeriodId.equalsIgnoreCase(periodId)) {
						String servicePeriodLimit = ((JSONObject) servicePeriodArray.get(i)).get("MaximumLimit")
								.toString().trim();
						// float iperiodLimit = Integer.parseInt(periodLimit);
						float iperiodLimit = Float.parseFloat(periodLimit);
						float sPeriodLimit = Float.parseFloat(servicePeriodLimit);

						if (iperiodLimit > sPeriodLimit) {
							return false;
						}
					}
				}
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}

		return true;
	}

	public boolean validateServiceMaxAndMinLimit(JSONObject getRecordForServiceId, JSONArray periodsLimitArray)
			throws Exception {

		String serviceMaxTransferLimit = null;
		String serviceMinTransferLimit = "0";

		if (((JSONObject) ((JSONArray) getRecordForServiceId.get("service")).get(0)).has("MaxTransferLimit")) {
			serviceMaxTransferLimit = ((JSONObject) ((JSONArray) getRecordForServiceId.get("service")).get(0))
					.getString("MaxTransferLimit");
		}
		if (((JSONObject) ((JSONArray) getRecordForServiceId.get("service")).get(0)).has("MinTransferLimit")) {
			serviceMinTransferLimit = ((JSONObject) ((JSONArray) getRecordForServiceId.get("service")).get(0))
					.getString("MinTransferLimit");
		}
		if (serviceMaxTransferLimit == null) {
			// no maximum limit hence true
			return true;
		}

		for (int i = 0; i < periodsLimitArray.length(); i++) {
			String periodId = ((JSONObject) periodsLimitArray.get(i)).get("periodId").toString().trim();
			String periodLimit = ((JSONObject) periodsLimitArray.get(i)).get("periodLimit").toString().trim();
			if (periodId != null && ("P1").equalsIgnoreCase(periodId)) { // validating for daily
				if (periodLimit != null) {
					float plimit = Float.parseFloat(periodLimit.trim());
					float maxSerLimit = Float.parseFloat(serviceMaxTransferLimit.trim());
					float minSerLimit = Float.parseFloat(serviceMinTransferLimit.trim());
					if (plimit < minSerLimit || plimit > maxSerLimit) {
						return false;
					}
				} else {
					throw new Exception("PERIOD_LIMIT_EMPTY");
				}
				return true;
			}
		}
		return true;
	}

	public JSONObject getPeriodicLimitForServiceID(String serviceID, String authToken,
			DataControllerRequest requestInstance) {

		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.put(ODataQueryConstants.FILTER, "Service_id eq '" + serviceID + "'");
		String readEndpointResponse = Executor.invokeService(ServiceURLEnum.PERIODICLIMITSERVICE_VIEW_READ,
				postParametersMap, null, requestInstance);
		return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

	}

	public JSONObject getRecordsForServiceID(String serviceID, String authToken,
			DataControllerRequest requestInstance) {

		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.put(ODataQueryConstants.FILTER, "id eq '" + serviceID + "'");
		String readEndpointResponse = Executor.invokeService(ServiceURLEnum.SERVICE_READ, postParametersMap, null,
				requestInstance);
		return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

	}

	private JSONObject createTransactionLimit(String transactionLimitId, String serviceId, String systemUser,
			String authToken, DataControllerRequest requestInstance) {
		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", transactionLimitId);
		postParametersMap.put("Description", serviceId);
		postParametersMap.put("createdby", systemUser);
		postParametersMap.put("modifiedby", "NULL");
		postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("softdeleteflag", "0");
		String createRoleResponse = Executor.invokeService(ServiceURLEnum.TRANSACTIONLIMIT_CREATE, postParametersMap,
				null, requestInstance);

		createResponseJSON = CommonUtilities.getStringAsJSONObject(createRoleResponse);
		int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.FAILED,
					"TransactionLimit Create failed. transactionLimitId: " + transactionLimitId);
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
				ActivityStatusEnum.SUCCESSFUL,
				"TransactionLimit Create successful. transactionLimitId: " + transactionLimitId);
		return createResponseJSON;
	}

	private JSONObject createPeriodicLimit(String transactionLimitId, String customerId, String serviceId,
			String transactionFeeId, JSONArray periodsLimitArray, String authToken, String systemUser,
			DataControllerRequest requestInstance) {

		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		String periodicLimitId = null;

		for (int i = 0; i < periodsLimitArray.length(); i++) {
			periodicLimitId = CommonUtilities.getNewId().toString();
			String periodId = ((JSONObject) periodsLimitArray.get(i)).get("periodId").toString().trim();
			String periodLimit = ((JSONObject) periodsLimitArray.get(i)).get("periodLimit").toString().trim();
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

			String createPeriodicLimitResponse = Executor.invokeService(ServiceURLEnum.PERIODICLIMIT_CREATE,
					postParametersMap, null, requestInstance);

			createResponseJSON = CommonUtilities.getStringAsJSONObject(createPeriodicLimitResponse);
			int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
			if (getOpStatusCode != 0) {
				// rollback logic
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
						ActivityStatusEnum.FAILED, "Periodic Limit Create failed. periodicLimitId: " + periodicLimitId);
				return createResponseJSON;
			}
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.SUCCESSFUL,
					"Periodic Limit Create successful. periodicLimitId: " + periodicLimitId);
		}

		return createResponseJSON;
	}

	private JSONObject createCustomerEntitlement(String customerEntitlementId, String transactionLimitId,
			String customerId, String serviceId, String transactionFeeId, String systemUser, String authToken,
			DataControllerRequest requestInstance) {
		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();

		postParametersMap.clear();
		postParametersMap.put("id", customerEntitlementId);
		postParametersMap.put("Customer_id", customerId);
		postParametersMap.put("Service_id", serviceId);
		postParametersMap.put("TransactionFee_id", transactionFeeId);
		postParametersMap.put("TransactionLimit_id", transactionLimitId);

		postParametersMap.put("createdby", "NULL");
		postParametersMap.put("modifiedby", "NULL");
		postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("softdeleteflag", "0");
		String createRoleResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERENTITLEMENT_CREATE, postParametersMap,
				null, requestInstance);

		createResponseJSON = CommonUtilities.getStringAsJSONObject(createRoleResponse);
		int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.FAILED,
					"Customer Entitlement Create failed. customerEntitlementId: " + customerEntitlementId);
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
				ActivityStatusEnum.SUCCESSFUL,
				"Customer Entitlement Create successful. customerEntitlementId: " + customerEntitlementId);

		return createResponseJSON;
	}
}