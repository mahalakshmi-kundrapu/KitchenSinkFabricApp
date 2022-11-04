package com.kony.adminconsole.service.limitsandfees.transactionfeesusergroup;

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

public class TransactionFeeUserGroupAdd implements JavaService2 {

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {

		String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
		Result result = new Result();
		String groupId = requestInstance.getParameter("groupId");
		String serviceId = requestInstance.getParameter("serviceId");
		String systemUser = requestInstance.getParameter("systemUserId");
		String name = requestInstance.getParameter("transactionFeeName");
		String description = requestInstance.getParameter("transactionFeeDescription");
		JSONArray transactionFeeLimits = CommonUtilities.getStringAsJSONArray(requestInstance.getParameter("transactionFeeLimits"));

		if (StringUtils.isBlank(groupId)) {
			return ErrorCodeEnum.ERR_20569.setErrorCode(null);
		}

		if (StringUtils.isBlank(serviceId)) {
			return ErrorCodeEnum.ERR_20570.setErrorCode(null);
		}

		if (StringUtils.isBlank(systemUser)) {
			return ErrorCodeEnum.ERR_20571.setErrorCode(null);
		}
		// add validation for transactionDetails

		if (transactionFeeLimits == null || transactionFeeLimits.length() == 0) {
			return ErrorCodeEnum.ERR_20596.setErrorCode(null);
		}
		try {

			JSONObject prevPL = fetchGroupEntitlement(groupId, serviceId, authToken, requestInstance);
			if (prevPL == null || !prevPL.has(FabricConstants.OPSTATUS) || prevPL.getInt(FabricConstants.OPSTATUS) != 0 || !prevPL.has("transactionfeegroup_view")) {
				return ErrorCodeEnum.ERR_20423.setErrorCode(null);
			}
			Dataset ds = CommonUtilities.constructDatasetFromJSONArray(prevPL.getJSONArray("transactionfeegroup_view"));

			ValidateTransactionFeeLimits validateUtil = new ValidateTransactionFeeLimits();
			Dataset validationDS = validateUtil.validateFeeLimits(ds, transactionFeeLimits);

			if (!validationDS.getAllRecords().isEmpty()) {
				result.addDataset(validationDS);
				return result;
			}
			String transactionFeeId = ds.getAllRecords().isEmpty() ? "tfi-" + CommonUtilities.getNewId().toString()
					: ds.getRecord(0).getParam("TransactionLimit_id").getValue();

			JSONObject createResponseJSON = createTransactionFee(transactionFeeId, name, description, systemUser, authToken, requestInstance);
			if (createResponseJSON != null && createResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
				return ErrorCodeEnum.ERR_20801.setErrorCode(null);
			}
			JSONObject createTransactionFeeSlabResponse = createTransactionFeeSlab(transactionFeeId,
					transactionFeeLimits, systemUser, authToken, requestInstance);
			if (createTransactionFeeSlabResponse != null && createTransactionFeeSlabResponse.getInt(FabricConstants.OPSTATUS) != 0) {
				return ErrorCodeEnum.ERR_20805.setErrorCode(null);
			}

			JSONObject groupEntitlementResponse = groupEntitlement(ds.getAllRecords().isEmpty(), groupId, serviceId,
					transactionFeeId, systemUser, authToken, requestInstance);
			if (groupEntitlementResponse != null && groupEntitlementResponse.getInt(FabricConstants.OPSTATUS) != 0) {
				return ErrorCodeEnum.ERR_20422.setErrorCode(null);
			}
			result.addParam(new Param("transactionFeeId", transactionFeeId, FabricConstants.STRING));
		} catch (ClassCastException e) {
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.CREATE, ActivityStatusEnum.FAILED,
					"User group create failed. errmsg: " + e.getMessage());
		} catch (Exception e) {
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.CREATE, ActivityStatusEnum.FAILED,
					"User group create failed. errmsg: " + e.getMessage());
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
				EventEnum.CREATE, ActivityStatusEnum.SUCCESSFUL,
				"User group create success.");
		return result;
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
		String createResponse = Executor.invokeService(
				ServiceURLEnum.TRANSACTIONFEE_CREATE,
				postParametersMap, null, requestInstance);

		createResponseJSON = CommonUtilities.getStringAsJSONObject(createResponse);
		int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.CREATE, ActivityStatusEnum.FAILED,
					"Transaction fee create failed.");
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
				EventEnum.CREATE, ActivityStatusEnum.SUCCESSFUL,
				"Transaction fee create success.");
		return createResponseJSON;
	}

	private JSONObject createTransactionFeeSlab(String transactionFeeId, JSONArray transactionFeeSlabsArray,
			String systemUser, String authToken, DataControllerRequest requestInstance) {
		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		String transactionFeeSlabId = null;
		for (int i = 0; i < transactionFeeSlabsArray.length(); i++) {
			JSONObject temp = transactionFeeSlabsArray.getJSONObject(i);
			transactionFeeSlabId = CommonUtilities.getNewId().toString();
			String minimumTransactionValue = temp.getString("minAmount").trim();
			String maximumTransactionValue = temp.getString("maxAmount").trim();
			String fees = temp.getString("fees").trim();
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
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
						EventEnum.CREATE, ActivityStatusEnum.FAILED,
						"Transaction fee slab create failed.");
				return createResponseJSON;
			}
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.CREATE, ActivityStatusEnum.SUCCESSFUL,
					"Transaction fee slab create success.");
		}
		return createResponseJSON;
	}

	private JSONObject groupEntitlement(boolean isCreate, String groupId, String serviceId,
			String transactionFeeId, String systemUser, String authToken, DataControllerRequest requestInstance) {

		ServiceURLEnum serviceURLEnum = isCreate ? ServiceURLEnum.GROUPENTITLEMENT_CREATE
				: ServiceURLEnum.GROUPENTITLEMENT_UPDATE;
		JSONObject createResponseJSON = null;
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("Group_id", groupId);
		postParametersMap.put("Service_id", serviceId);
		postParametersMap.put("TransactionFee_id", transactionFeeId);
		postParametersMap.put("createdby", "NULL");
		postParametersMap.put("modifiedby", "NULL");
		postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("softdeleteflag", "0");
		String createGroupEntitlementResponse = Executor.invokeService(serviceURLEnum, postParametersMap, null, requestInstance);

		createResponseJSON = CommonUtilities.getStringAsJSONObject(createGroupEntitlementResponse);
		int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.CREATE, ActivityStatusEnum.FAILED,
					"Group entitlement update failed. groupId: " + groupId);
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
				EventEnum.CREATE, ActivityStatusEnum.SUCCESSFUL,
				"Group entitlement update success. groupId: " + groupId);
		return createResponseJSON;
	}

	private JSONObject fetchGroupEntitlement(String groupId, String serviceId, String authToken,
			DataControllerRequest requestInstance) {
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.put(ODataQueryConstants.FILTER, "Group_id eq '" + groupId + "' and Service_id eq '" + serviceId + "'");
		String viewResponse = Executor.invokeService(
				ServiceURLEnum.TRANSACTIONFEE_READ, postParametersMap, null, requestInstance);
		JSONObject response = CommonUtilities.getStringAsJSONObject(viewResponse);
		return response;
	}
}