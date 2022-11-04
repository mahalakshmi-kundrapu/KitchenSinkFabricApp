package com.kony.adminconsole.service.limitsandfees.overallpaymentlimits;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class OverallPaymentLimitsUpdate implements JavaService2 {

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {

		String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
		JSONArray serviceIds = CommonUtilities.getStringAsJSONArray(requestInstance.getParameter("serviceIds"));
		JSONArray periodsLimitArray = CommonUtilities
				.getStringAsJSONArray(requestInstance.getParameter("periodLimits"));
		String statusId = requestInstance.getParameter("statusId");
		String systemUser = requestInstance.getParameter("systemUserId");
		String transactionGroupId = requestInstance.getParameter("transactionGroupId");
		String transactionGroupName = requestInstance.getParameter("transactionGroupName");
		String transactionLimitId = requestInstance.getParameter("transactionLimitId");
		Result result = new Result();

		if (StringUtils.isBlank(systemUser)) {
			return ErrorCodeEnum.ERR_20571.setErrorCode(null);
		}

		if (StringUtils.isBlank(transactionGroupId)) {
			return ErrorCodeEnum.ERR_20590.setErrorCode(null);
		}

		if (StringUtils.isBlank(transactionLimitId)) {
			return ErrorCodeEnum.ERR_20574.setErrorCode(null);
		}

		if (serviceIds == null || serviceIds.length() == 0) {
			return ErrorCodeEnum.ERR_20589.setErrorCode(null);
		}
		try {

			OverallPaymentLimitsView view = new OverallPaymentLimitsView();
			Dataset dataset = view.fetchAllTransactionGroupsByIdServices(transactionGroupId,
					ServiceURLEnum.OVERALLPAYMENTLIMITS_VIEW_READ, authToken, requestInstance);

			ValidateOverallPaymentLimits validateUtil = new ValidateOverallPaymentLimits();
			Dataset errors = validateUtil.validateLimits(dataset, periodsLimitArray,
					ServiceURLEnum.PERIOD_READ, authToken, requestInstance);
			if (!errors.getAllRecords().isEmpty()) {
				result = ErrorCodeEnum.ERR_20595.setErrorCode(result);
				result.addDataset(errors);
				return result;
			}

			Set<String> serviceIdsInEditReq = fetchServicesInRequest(serviceIds);
			Set<String> transactionGroupserviceIdsToDelete = new HashSet<String>();
			Set<String> existingServices = fetchPreviousServices(dataset.getRecord(0).getDatasetById("services"),
					serviceIdsInEditReq, transactionGroupserviceIdsToDelete);
			Set<String> existingPeriods = fetchPreviousPeriods(dataset.getRecord(0).getDatasetById("periodicLimits"), serviceIds);

			boolean isStatusChange = !dataset.getRecord(0).getParam("statusId").getValue().equalsIgnoreCase(statusId);
			boolean isNameChange = !dataset.getRecord(0).getParam("transactionGroupName").getValue()
					.equalsIgnoreCase(transactionGroupName);

			if (isStatusChange || isNameChange) {
				JSONObject transactionGroupResponse = updateTransactionGroup(transactionGroupId, statusId,
						transactionGroupName, systemUser, authToken, requestInstance);
				if (transactionGroupResponse != null && transactionGroupResponse.getInt(FabricConstants.OPSTATUS) != 0) {
					return ErrorCodeEnum.ERR_20835.setErrorCode(result);
				}
			}

			// This is to add a new service to the existing group
			for (String key : serviceIdsInEditReq) {
				boolean isNewService = !existingServices.contains(key);
				if (isNewService) {
					JSONObject transactionGroupServiceResponse = createTransactionGroupService(transactionGroupId, key,
							systemUser, authToken, requestInstance);
					if (transactionGroupServiceResponse != null
							&& transactionGroupServiceResponse.getInt(FabricConstants.OPSTATUS) != 0) {
						return ErrorCodeEnum.ERR_20833.setErrorCode(result);
					}
				}
			}

			// This is to delete a service from the existing group
			for (String key : transactionGroupserviceIdsToDelete) {
				JSONObject deleteTransactionGroupServiceResponse = deleteTransactionGroupService(key, systemUser, authToken, requestInstance);
				if (deleteTransactionGroupServiceResponse != null
						&& deleteTransactionGroupServiceResponse.getInt(FabricConstants.OPSTATUS) != 0) {
					return ErrorCodeEnum.ERR_20836.setErrorCode(result);
				}
			}

			if (periodsLimitArray != null) {
				for (int i = 0; i < periodsLimitArray.length(); i++) {
					JSONObject temp = periodsLimitArray.getJSONObject(i);
					String periodId = temp.optString("periodId");
					String periodicLimitId = temp.optString("periodicLimitId").trim();
					String periodLimit = temp.optString("periodLimit").trim();
					boolean isDelete = temp.optString("deleteFlag").equalsIgnoreCase("Y");
					boolean isNewPeriod = !existingPeriods.contains(periodId);
					JSONObject response = null;

					if (isDelete) {
						response = deletePeriodicLimit(periodicLimitId, authToken, requestInstance);
					} else if (isNewPeriod) {
						response = createPeriodicLimit(transactionLimitId, temp, systemUser, authToken, requestInstance);
					} else {
						response = updatePeriodicLimit(periodicLimitId, periodLimit, systemUser, authToken,
								requestInstance);
					}
					if (response != null && response.getInt(FabricConstants.OPSTATUS) != 0) {
						ErrorCodeEnum enumType = (isDelete == true) ? ErrorCodeEnum.ERR_20840 : ((isNewPeriod == true) ? ErrorCodeEnum.ERR_20837 : ErrorCodeEnum.ERR_20839);
						return enumType.setErrorCode(result);
					}
				}
			}

		} catch (Exception e) {
			return ErrorCodeEnum.ERR_20001.setErrorCode(null);
		}
		return result;
	}

	private JSONObject updateTransactionGroup(String transactionGroupId, String statusId,
			String transactionGroupName, String systemUser, String authToken, DataControllerRequest requestInstance) {
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", transactionGroupId);
		if (statusId != null) {
			postParametersMap.put("Status_id", statusId);
		}
		if (transactionGroupName != null) {
			postParametersMap.put("Name", transactionGroupName);
		}
		postParametersMap.put("modifiedby", systemUser);
		postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("softdeleteflag", "0");

		String updateTransactionGroupResponse = Executor.invokeService(
				ServiceURLEnum.TRANSACTIONGROUP_UPDATE,
				postParametersMap, null, requestInstance);

		JSONObject upadteResponseJSON = CommonUtilities.getStringAsJSONObject(updateTransactionGroupResponse);
		int getOpStatusCode = upadteResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.UPDATE, ActivityStatusEnum.FAILED,
					"Transaction group update failed. transactionGroupId: " + transactionGroupId);
			return upadteResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
				EventEnum.UPDATE, ActivityStatusEnum.SUCCESSFUL,
				"Transaction group update successful. transactionGroupId: " + transactionGroupId);
		return upadteResponseJSON;
	}

	private Set<String> fetchPreviousServices(Dataset dataset, Set<String> serviceIdsInEditReq,
			Set<String> transactionGroupserviceIdsToDelete) {
		Set<String> existingServices = new HashSet<String>();
		for (Record record : dataset.getAllRecords()) {
			String serviceId = record.getParam("serviceId").getValue();
			existingServices.add(serviceId);
			if (!serviceIdsInEditReq.contains(serviceId)) {
				transactionGroupserviceIdsToDelete.add(record.getParam("transactionGroupServiceId").getValue());
			}

		}
		return existingServices;
	}

	private Set<String> fetchServicesInRequest(JSONArray serviceIds) {
		Set<String> serviceIdsInEditReq = new HashSet<String>();
		for (int i = 0; i < serviceIds.length(); i++) {
			serviceIdsInEditReq.add(serviceIds.getString(i));
		}
		return serviceIdsInEditReq;
	}

	private Set<String> fetchPreviousPeriods(Dataset dataset, JSONArray serviceIds) {
		Set<String> existingPeriods = new HashSet<String>();
		for (Record record : dataset.getAllRecords()) {
			existingPeriods.add(record.getParam("periodId").getValue());
		}
		return existingPeriods;
	}

	private JSONObject createTransactionGroupService(String transactionGroupId, String serviceId, String systemUser,
			String authToken, DataControllerRequest requestInstance) {

		String transactionGroupServiceId = "tgsi" + CommonUtilities.getNewId().toString();
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", transactionGroupServiceId);
		postParametersMap.put("TransactionGroup_id", transactionGroupId);
		postParametersMap.put("Service_id", serviceId);
		postParametersMap.put("createdby", systemUser);
		postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
		String createTransactionLimitResponse = Executor.invokeService(
				ServiceURLEnum.TRANSACTIONGROUPSERVICE_CREATE, postParametersMap,
				null, requestInstance);

		JSONObject createResponseJSON = CommonUtilities.getStringAsJSONObject(createTransactionLimitResponse);
		int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.CREATE, ActivityStatusEnum.FAILED,
					"Transaction group service create failed. transactionGroupServiceId: " + transactionGroupServiceId);
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
				EventEnum.CREATE, ActivityStatusEnum.SUCCESSFUL,
				"Transaction group service create successful. transactionGroupServiceId: " + transactionGroupServiceId);
		return createResponseJSON;
	}

	private JSONObject deleteTransactionGroupService(String transactionGroupServiceId, String systemUser,
			String authToken, DataControllerRequest requestInstance) {
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", transactionGroupServiceId);
		String deleteResponse = Executor.invokeService(
				ServiceURLEnum.TRANSACTIONGROUPSERVICE_DELETE,
				postParametersMap, null, requestInstance);
		JSONObject deleteResponseJSON = CommonUtilities.getStringAsJSONObject(deleteResponse);
		return deleteResponseJSON;
	}

	private JSONObject createPeriodicLimit(String transactionLimitId, JSONObject temp, String systemUser,
			String authToken, DataControllerRequest requestInstance) {

		String periodId = temp.getString("periodId");
		String periodLimit = temp.get("periodLimit").toString().trim();
		String periodicLimitId = "pli-" + CommonUtilities.getNewId().toString();

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

		String createPeriodicLimitResponse = Executor.invokeService(
				ServiceURLEnum.PERIODICLIMIT_CREATE,
				postParametersMap, null, requestInstance);

		JSONObject createResponseJSON = CommonUtilities.getStringAsJSONObject(createPeriodicLimitResponse);
		int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.CREATE, ActivityStatusEnum.FAILED,
					"Periodic limit create failed. periodicLimitId: " + periodicLimitId);
			return createResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
				EventEnum.CREATE, ActivityStatusEnum.SUCCESSFUL,
				"Periodic limit create successful. periodicLimitId: " + periodicLimitId);
		return createResponseJSON;
	}

	private JSONObject updatePeriodicLimit(String periodicLimitId, String periodLimit, String systemUser,
			String authToken, DataControllerRequest requestInstance) {
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", periodicLimitId);
		postParametersMap.put("MaximumLimit", periodLimit);
		postParametersMap.put("modifiedby", systemUser);
		postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
		postParametersMap.put("softdeleteflag", "0");

		String updatePeriodicLimitResponse = Executor.invokeService(
				ServiceURLEnum.PERIODICLIMIT_UPDATE,
				postParametersMap, null, requestInstance);

		JSONObject upadteResponseJSON = CommonUtilities.getStringAsJSONObject(updatePeriodicLimitResponse);
		int getOpStatusCode = upadteResponseJSON.getInt(FabricConstants.OPSTATUS);
		if (getOpStatusCode != 0) {
			// rollback logic
			AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
					EventEnum.UPDATE, ActivityStatusEnum.FAILED,
					"Periodic limit update failed. periodicLimitId: " + periodicLimitId);
			return upadteResponseJSON;
		}
		AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS,
				EventEnum.UPDATE, ActivityStatusEnum.SUCCESSFUL,
				"Periodic limit update successful. periodicLimitId: " + periodicLimitId);
		return upadteResponseJSON;
	}

	private JSONObject deletePeriodicLimit(String periodicLimitId, String authToken,
			DataControllerRequest requestInstance) {
		Map<String, String> postParametersMap = new HashMap<String, String>();
		postParametersMap.clear();
		postParametersMap.put("id", periodicLimitId);
		String deleteResponse = Executor.invokeService(
				ServiceURLEnum.PERIODICLIMIT_DELETE,
				postParametersMap, null, requestInstance);
		JSONObject deleteResponseJSON = CommonUtilities.getStringAsJSONObject(deleteResponse);
		return deleteResponseJSON;
	}
}