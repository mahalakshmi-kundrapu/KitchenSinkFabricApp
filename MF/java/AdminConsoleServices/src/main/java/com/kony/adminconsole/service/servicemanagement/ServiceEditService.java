package com.kony.adminconsole.service.servicemanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.service.limitsandfees.periodiclimitservice.PeriodLimitServiceUpdate;
import com.kony.adminconsole.service.limitsandfees.transactionfeesservice.TransferFeeServiceUpdate;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class ServiceEditService implements JavaService2 {

    private static final String MANAGE_STATUS_METHOD = "manageStatus";
    private static final Logger LOG = Logger.getLogger(ServiceEditService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result result = new Result();

            @SuppressWarnings("unchecked")
            Map<String, String> input = (HashMap<String, String>) inputArray[1];

            String loggedInUserId = LoggedInUserHandler.getUserDetails(requestInstance).getUserId();
            JSONArray editedEntitlements = CommonUtilities
                    .getStringAsJSONArray(requestInstance.getParameter("editedEntitlement"));

            if (methodID.equalsIgnoreCase(MANAGE_STATUS_METHOD)) {
                String Service_id = String.valueOf(input.get("Service_id"));
                String statusId = String.valueOf(input.get("Status_id"));
                return manageStatus(statusId, Service_id, loggedInUserId, requestInstance);
            }

            else {

                String serviceId = ((JSONObject) editedEntitlements.get(0)).optString("Service_id");
                requestInstance.addRequestParam_("serviceId", serviceId);
                requestInstance.addRequestParam_("systemUserId", loggedInUserId);
                requestInstance.addRequestParam_("transactionFeeId",
                        ((JSONObject) editedEntitlements.get(0)).optString("TransactionFee_id"));
                requestInstance.addRequestParam_("transactionLimitId",
                        ((JSONObject) editedEntitlements.get(0)).optString("TransactionLimit_id"));

                if (editedEntitlements != null && editedEntitlements.length() > 0) {

                    PeriodLimitServiceUpdate periodLimitServiceUpdateObj = new PeriodLimitServiceUpdate();
                    periodLimitServiceUpdateObj.invoke(methodID, inputArray, requestInstance, responseInstance);

                    TransferFeeServiceUpdate transferFeeServiceUpdateObj = new TransferFeeServiceUpdate();
                    transferFeeServiceUpdateObj.invoke(methodID, inputArray, requestInstance, responseInstance);

                    return editEntitlements(editedEntitlements, loggedInUserId, requestInstance);
                }
            }
            return result;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    private Result manageStatus(String statusId, String serviceId, String loggedInUserId,
            DataControllerRequest requestInstance) {

        Result result = new Result();
        Map<String, String> inputMap = new HashMap<>();

        inputMap.put("id", serviceId);
        inputMap.put("Status_id", statusId);
        inputMap.put("modifiedby", loggedInUserId);
        String editServiceResponse = Executor.invokeService(ServiceURLEnum.SERVICE_UPDATE, inputMap, null,
                requestInstance);
        JSONObject editServiceResponseJSON = CommonUtilities.getStringAsJSONObject(editServiceResponse);

        String serviceName = StringUtils.EMPTY;
        try {
            Map<String, String> readPostParametersMap = new HashMap<String, String>();
            readPostParametersMap.put(ODataQueryConstants.FILTER, "id eq " + serviceId);
            JSONObject readResponse = CommonUtilities.getStringAsJSONObject(
                    Executor.invokeService(ServiceURLEnum.SERVICE_READ, readPostParametersMap, null, requestInstance));
            serviceName = readResponse.getJSONArray("service").getJSONObject(0).optString("Name");
        } catch (Exception ignored) {
            // Ignored Exception
        }

        if (editServiceResponseJSON != null && editServiceResponseJSON.has(FabricConstants.OPSTATUS)
                && editServiceResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SERVICES, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL, "Service update successful. Service Name: " + serviceName);
            result.addParam(new Param("status", "success", FabricConstants.STRING));
        } else {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SERVICES, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Service update failed. Service Name: " + serviceName);
            ErrorCodeEnum.ERR_20382.setErrorCode(result);
            result.addParam(new Param("status", "fail", FabricConstants.STRING));
        }
        return result;
    }

    private Result editEntitlements(JSONArray entitlements, String User_id, DataControllerRequest requestInstance) {

        Result result = new Result();

        JSONObject serviceObject = entitlements.optJSONObject(0);
        String serviceId = serviceObject.optString("Service_id");

        Param errorParam = null;
        if (StringUtils.isBlank(serviceObject.optString("Service_id"))) {
            errorParam = new Param("Invalid", "id cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("Type_id"))) {
            errorParam = new Param("Invalid", "Type_id cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("Channel_id"))) {
            errorParam = new Param("Invalid", "Channel_id cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("Name"))) {
            errorParam = new Param("Invalid", "Name cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("Description"))) {
            errorParam = new Param("Invalid", "Description cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("Status_id"))) {
            errorParam = new Param("Invalid", "Status_id cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("MaxTransferLimit"))) {
            errorParam = new Param("Invalid", "MaxTransferLimit cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("MinTransferLimit"))) {
            errorParam = new Param("Invalid", "MinTransferLimit cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("TransferDenominations"))) {
            errorParam = new Param("Invalid", "TransferDenominations cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("IsFutureTransaction"))) {
            errorParam = new Param("Invalid", "IsFutureTransaction cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }

        if (StringUtils.isBlank(serviceObject.optString("TransactionCharges"))) {
            errorParam = new Param("Invalid", "TransactionCharges cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("IsAuthorizationRequired"))) {
            errorParam = new Param("Invalid", "IsAuthorizationRequired cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("IsSMSAlertActivated"))) {
            errorParam = new Param("Invalid", "IsSMSAlertActivated cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("SMSCharges"))) {
            errorParam = new Param("Invalid", "SMSCharges cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("IsBeneficiarySMSAlertActivated"))) {
            errorParam = new Param("Invalid", "IsBeneficiarySMSAlertActivated cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("BeneficiarySMSCharge"))) {
            errorParam = new Param("Invalid", "BeneficiarySMSCharge cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("HasWeekendOperation"))) {
            errorParam = new Param("Invalid", "HasWeekendOperation cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("IsOutageMessageActive"))) {
            errorParam = new Param("Invalid", "IsOutageMessageActive cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("IsAlertActive"))) {
            errorParam = new Param("Invalid", "IsAlertActive cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("IsTCActive"))) {
            errorParam = new Param("Invalid", "IsTCActive cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("IsAgreementActive"))) {
            errorParam = new Param("Invalid", "IsAgreementActive cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("IsCampaignActive"))) {
            errorParam = new Param("Invalid", "IsCampaignActive cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        }
        if (StringUtils.isBlank(serviceObject.optString("WorkSchedule_id"))) {
            errorParam = new Param("Invalid", "WorkSchedule_id cannot be null", FabricConstants.STRING);
            result.addParam(errorParam);
            return result;
        } else {

            Map<String, String> inputMap = new HashMap<>();
            inputMap.put("id", serviceId);
            inputMap.put("Type_id", serviceObject.optString("Type_id"));
            inputMap.put("Code", serviceObject.optString("Code"));
            inputMap.put("Category_Id", serviceObject.optString("Category_Id"));
            inputMap.put("DisplayName", serviceObject.optString("DisplayName"));
            inputMap.put("DisplayDescription", serviceObject.optString("DisplayDescription"));
            inputMap.put("Name", serviceObject.optString("Name"));
            inputMap.put("Description", serviceObject.optString("Description"));
            inputMap.put("Status_id", serviceObject.optString("Status_id"));
            inputMap.put("MaxTransferLimit", serviceObject.optString("MaxTransferLimit"));
            inputMap.put("MinTransferLimit", serviceObject.optString("MinTransferLimit"));
            inputMap.put("TransferDenominations", serviceObject.optString("TransferDenominations"));
            inputMap.put("IsFutureTransaction", serviceObject.optString("IsFutureTransaction"));
            inputMap.put("TransactionCharges", serviceObject.optString("TransactionCharges"));
            inputMap.put("IsAuthorizationRequired", serviceObject.optString("IsAuthorizationRequired"));
            inputMap.put("IsSMSAlertActivated", serviceObject.optString("IsSMSAlertActivated"));
            inputMap.put("SMSCharges", serviceObject.optString("SMSCharges"));
            inputMap.put("IsBeneficiarySMSAlertActivated", serviceObject.optString("IsBeneficiarySMSAlertActivated"));
            inputMap.put("BeneficiarySMSCharge", serviceObject.optString("BeneficiarySMSCharge"));
            inputMap.put("HasWeekendOperation", serviceObject.optString("HasWeekendOperation"));
            inputMap.put("IsOutageMessageActive", serviceObject.optString("IsOutageMessageActive"));
            inputMap.put("IsAlertActive", serviceObject.optString("IsAlertActive"));
            inputMap.put("IsTCActive", serviceObject.optString("IsTCActive"));
            inputMap.put("IsAgreementActive", serviceObject.optString("IsAgreementActive"));
            inputMap.put("IsCampaignActive", serviceObject.optString("IsCampaignActive"));
            inputMap.put("WorkSchedule_id", serviceObject.optString("WorkSchedule_id"));
            if (StringUtils.isNotBlank(serviceObject.optString("TransactionFee_id"))) {
                inputMap.put("TransactionFee_id", serviceObject.optString("TransactionFee_id"));
            }
            if (StringUtils.isNotBlank(serviceObject.optString("TransactionLimit_id"))) {
                inputMap.put("TransactionLimit_id", serviceObject.optString("TransactionLimit_id"));
            }

            String editServiceResponse = Executor.invokeService(ServiceURLEnum.SERVICE_UPDATE, inputMap, null,
                    requestInstance);
            JSONObject editServiceResponseJSON = CommonUtilities.getStringAsJSONObject(editServiceResponse);

            if (editServiceResponseJSON != null && editServiceResponseJSON.has(FabricConstants.OPSTATUS)
                    && editServiceResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                result.addParam(new Param("serviceId", serviceId, FabricConstants.STRING));
                result.addParam(new Param("status", "success", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SERVICES, EventEnum.UPDATE,
                        ActivityStatusEnum.SUCCESSFUL,
                        "Service update successful. Service Name: " + serviceObject.optString("Name"));

                // create service channel mapping
                ServiceCreateService createService = new ServiceCreateService();
                deleteAllServiceChannels(serviceId, result, requestInstance);
                createService.createServiceChannel(serviceId, serviceObject.optString("Channel_id"), requestInstance,
                        result);
            } else {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SERVICES, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Service update failed. Service Name: " + serviceObject.optString("Name"));
                result.addParam(new Param("status", "fail", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20382.setErrorCode(result);
                return result;
            }
        }
        return result;
    }

    private void deleteAllServiceChannels(String serviceId, Result processedResult,
            DataControllerRequest requestInstance) {
        Map<String, String> readInputMap = new HashMap<>();
        readInputMap.put(ODataQueryConstants.FILTER, "Service_id eq '" + serviceId + "'");

        String readServiceChannelResponse = Executor.invokeService(ServiceURLEnum.SERVICE_CHANNELS_READ, readInputMap,
                null, requestInstance);
        JSONObject readServiceResponseJSON = CommonUtilities.getStringAsJSONObject(readServiceChannelResponse);
        if (readServiceResponseJSON == null || !readServiceResponseJSON.has(FabricConstants.OPSTATUS)
                || readServiceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || !readServiceResponseJSON.has("service_channels")) {
            ErrorCodeEnum.ERR_20391.setErrorCode(processedResult);
            processedResult.addParam(new Param("FailureReason", readServiceChannelResponse));
            return;
        }
        JSONArray channels = readServiceResponseJSON.getJSONArray("service_channels");
        channels.forEach((channel) -> {
            JSONObject channelRecord = (JSONObject) channel;
            Map<String, String> inputMap = new HashMap<>();
            inputMap.put("Service_id", serviceId);
            inputMap.put("Channel_id", channelRecord.getString("Channel_id"));
            String deleteServiceChannelResponse = Executor.invokeService(ServiceURLEnum.SERVICE_CHANNELS_DELETE,
                    inputMap, null, requestInstance);
            JSONObject deleteServiceResponseJSON = CommonUtilities.getStringAsJSONObject(deleteServiceChannelResponse);
            if (deleteServiceResponseJSON == null || !deleteServiceResponseJSON.has(FabricConstants.OPSTATUS)
                    || deleteServiceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                ErrorCodeEnum.ERR_20390.setErrorCode(processedResult);
                processedResult.addParam(new Param("FailureReason", deleteServiceChannelResponse));
                return;
            }
        });
    }

}