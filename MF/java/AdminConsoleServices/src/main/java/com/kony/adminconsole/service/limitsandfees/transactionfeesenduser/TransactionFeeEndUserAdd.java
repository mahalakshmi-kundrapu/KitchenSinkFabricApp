package com.kony.adminconsole.service.limitsandfees.transactionfeesenduser;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
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

public class TransactionFeeEndUserAdd implements JavaService2 {
	private static final Logger LOG = Logger.getLogger(TransactionFeeEndUserAdd.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {
		try {

			Result result = new Result();
			String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
			Param statusParam;
			String customerEntitlementId = CommonUtilities.getNewId().toString();
			String transactionFeeId = CommonUtilities.getNewId().toString();
			String customerId = "";
			String serviceId = "";
			String systemUser = "";
			JSONArray transactionFeesArray = null;
			if (requestInstance.getParameter("customerId") != null) {
				customerId = requestInstance.getParameter("customerId");
				if (StringUtils.isBlank(customerId)) {
					return ErrorCodeEnum.ERR_20565.setErrorCode(null);
				}
			} else {
				return ErrorCodeEnum.ERR_20582.setErrorCode(null);
			}
			if (requestInstance.getParameter("serviceId") != null) {
				serviceId = requestInstance.getParameter("serviceId");
				if (StringUtils.isBlank(serviceId)) {
					return ErrorCodeEnum.ERR_20566.setErrorCode(null);
				}
			} else {
				return ErrorCodeEnum.ERR_20583.setErrorCode(null);
			}
			if (requestInstance.getParameter("systemUserId") != null) {
				systemUser = requestInstance.getParameter("systemUserId");
			}
			if (requestInstance.getParameter("transactionFees") != null) {
				transactionFeesArray = new JSONArray(requestInstance.getParameter("transactionFees"));
				if (transactionFeesArray.length() == 0) {
					return ErrorCodeEnum.ERR_20585.setErrorCode(null);
				}
			} else {
				return ErrorCodeEnum.ERR_20584.setErrorCode(null);
			}
			String name = requestInstance.getParameter("transactionFeeName");
			String description = requestInstance.getParameter("transactionFeeDescription");
			try {
				ValidateTransactionFeeLimits vtflObj = new ValidateTransactionFeeLimits();
				Dataset validationDS = vtflObj.validateFeeLimits(new Dataset(), transactionFeesArray);
				if (!validationDS.getAllRecords().isEmpty()) {
					result.addDataset(validationDS);
					return result;
				}
				JSONObject createResponseJSON = createTransactionFee(transactionFeeId, name, description, systemUser,
						authToken, requestInstance);
				if (createResponseJSON != null && createResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
					return ErrorCodeEnum.ERR_20801.setErrorCode(null);
				}

				JSONObject createTransactionFeesSlabResponseJSON = createTransactionFeesSlab(transactionFeeId,
						customerId, serviceId, transactionFeesArray, authToken, systemUser, requestInstance);
				if (createTransactionFeesSlabResponseJSON != null
						&& createTransactionFeesSlabResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
					return ErrorCodeEnum.ERR_20805.setErrorCode(null);
				}

				JSONObject createCustomerEntitlementResponseJSON = createCustomerEntitlement(customerEntitlementId,
						transactionFeeId, customerId, serviceId, systemUser, authToken, requestInstance);
				if (createCustomerEntitlementResponseJSON != null
						&& createCustomerEntitlementResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
					return ErrorCodeEnum.ERR_20809.setErrorCode(null);
				}

				result.addParam(new Param("transactionFeeId", transactionFeeId, FabricConstants.STRING));
			} catch (Exception e) {
				statusParam = new Param("Status", "Error", FabricConstants.STRING);
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
						ActivityStatusEnum.FAILED, "Customer Entitlement create failed. errmsg: " + e.getMessage());
				result.addParam(statusParam);
			}
			return result;
		} catch (Exception e) {
			Result errorResult = new Result();
			LOG.debug("Runtime Exception.Exception Trace:", e);
			ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
			return errorResult;
		}
	}

	private JSONObject createTransactionFee(String transactionFeeId, String name, String description, String systemUser,
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
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.FAILED, "Transaction Fees create failed. transactionFeeId: " + transactionFeeId);
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
				ActivityStatusEnum.SUCCESSFUL,
				"Transaction Fees create Successful. transactionFeeId: " + transactionFeeId);
		return createResponseJSON;
	}

	private JSONObject createTransactionFeesSlab(String transactionFeeId, String customerId, String serviceId,
			JSONArray periodsLimitArray, String authToken, String systemUser, DataControllerRequest requestInstance) {
		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		String transactionFeeSlabId = null;
		for (int i = 0; i < periodsLimitArray.length(); i++) {
			transactionFeeSlabId = CommonUtilities.getNewId().toString();
			String minAmount = ((JSONObject) periodsLimitArray.get(i)).get("minAmount").toString().trim();
			String maxAmount = ((JSONObject) periodsLimitArray.get(i)).get("maxAmount").toString().trim();
			String fees = ((JSONObject) periodsLimitArray.get(i)).get("fees").toString().trim();
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
			String createPeriodicLimitResponse = Executor.invokeService(ServiceURLEnum.TRANSACTIONFEESLAB_CREATE,
					postParametersMap, null, requestInstance);
			createResponseJSON = CommonUtilities.getStringAsJSONObject(createPeriodicLimitResponse);
			int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
			if (getOpStatusCode != 0) {
				// rollback logic
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
						ActivityStatusEnum.FAILED,
						"Transaction Feeslab create failed. transactionFeeSlabId: " + transactionFeeSlabId);
				return createResponseJSON;
			}
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
					ActivityStatusEnum.SUCCESSFUL,
					"Transaction Feeslab create Successful. transactionFeeSlabId: " + transactionFeeSlabId);
		}
		return createResponseJSON;
	}

	private JSONObject createCustomerEntitlement(String customerEntitlementId, String transactionFeeId,
			String customerId, String serviceId, String systemUser, String authToken,
			DataControllerRequest requestInstance) {
		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("Customer_id", customerId);
		postParametersMap.put("Service_id", serviceId);
		postParametersMap.put("TransactionFee_id", transactionFeeId);
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
					"Customer Entitlement create failed. customerId: " + customerId + " serviceId: " + serviceId);
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
				ActivityStatusEnum.SUCCESSFUL,
				"Customer Entitlement create Successful. customerId: " + customerId + " serviceId: " + serviceId);
		return createResponseJSON;
	}
}