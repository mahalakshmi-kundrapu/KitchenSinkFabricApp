package com.kony.adminconsole.service.servicemanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
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
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class ServiceCreateService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(ServiceCreateService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {

            String loggedInUserId = LoggedInUserHandler.getUserDetails(requestInstance).getUserId();
            JSONArray newService = CommonUtilities.getStringAsJSONArray(requestInstance.getParameter("Service"));
            return setEntitlements(newService, loggedInUserId, requestInstance);

        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    private Result setEntitlements(JSONArray newService, String loggedInUserId, DataControllerRequest requestInstance) {

        Result result = new Result();

        String Service_id = CommonUtilities.getNewId().toString();
        JSONObject serviceInfo = newService.getJSONObject(0);

        Param errorMessageParam = null;
        if (StringUtils.isBlank(serviceInfo.optString("Type_id"))) {
            errorMessageParam = new Param("Invalid", "Type_id cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("Channel_id"))) {
            errorMessageParam = new Param("Invalid", "Channel_id cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("Category_Id"))) {
            errorMessageParam = new Param("Invalid", "Category_Id cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("DisplayName"))) {
            errorMessageParam = new Param("Invalid", "DisplayName cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("DisplayDescription"))) {
            errorMessageParam = new Param("Invalid", "DisplayDescription cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("Code"))) {
            errorMessageParam = new Param("Invalid", "Code cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("Name"))) {
            errorMessageParam = new Param("Invalid", "Name cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("Description"))) {
            errorMessageParam = new Param("Invalid", "Description cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("Status_id"))) {
            errorMessageParam = new Param("Invalid", "Status_id cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("MaxTransferLimit"))) {
            errorMessageParam = new Param("Invalid", "MaxTransferLimit cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("MinTransferLimit"))) {
            errorMessageParam = new Param("Invalid", "MinTransferLimit cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("TransferDenominations"))) {
            errorMessageParam = new Param("Invalid", "TransferDenominations cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("IsFutureTransaction"))) {
            errorMessageParam = new Param("Invalid", "IsFutureTransaction cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("TransactionCharges"))) {
            errorMessageParam = new Param("Invalid", "TransactionCharges cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("IsAuthorizationRequired"))) {
            errorMessageParam = new Param("Invalid", "IsAuthorizationRequired cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("IsSMSAlertActivated"))) {
            errorMessageParam = new Param("Invalid", "IsSMSAlertActivated cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("SMSCharges"))) {
            errorMessageParam = new Param("Invalid", "SMSCharges cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("IsBeneficiarySMSAlertActivated"))) {
            errorMessageParam = new Param("Invalid", "IsBeneficiarySMSAlertActivated cannot be null",
                    FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("BeneficiarySMSCharge"))) {
            errorMessageParam = new Param("Invalid", "BeneficiarySMSCharge cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("HasWeekendOperation"))) {
            errorMessageParam = new Param("Invalid", "HasWeekendOperation cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("IsOutageMessageActive"))) {
            errorMessageParam = new Param("Invalid", "IsOutageMessageActive cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("IsAlertActive"))) {
            errorMessageParam = new Param("Invalid", "IsAlertActive cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("IsTCActive"))) {
            errorMessageParam = new Param("Invalid", "IsTCActive cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("IsAgreementActive"))) {
            errorMessageParam = new Param("Invalid", "IsAgreementActive cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("IsCampaignActive"))) {
            errorMessageParam = new Param("Invalid", "IsCampaignActive cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("WorkSchedule_id"))) {
            errorMessageParam = new Param("Invalid", "WorkSchedule_id cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }
        if (StringUtils.isBlank(serviceInfo.optString("TransactionLimit_id"))) {
            errorMessageParam = new Param("Invalid", "TransactionLimit_id cannot be null", FabricConstants.STRING);
            result.addParam(errorMessageParam);
            return result;
        }

        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("id", Service_id);
        inputMap.put("Type_id", serviceInfo.optString("Type_id"));
        inputMap.put("code", serviceInfo.optString("Code"));
        inputMap.put("Category_id", serviceInfo.optString("Category_Id"));
        inputMap.put("Name", serviceInfo.optString("Name"));
        inputMap.put("DisplayName", serviceInfo.optString("DisplayName"));
        inputMap.put("DisplayDescription", serviceInfo.optString("DisplayDescription"));
        inputMap.put("Description", serviceInfo.optString("Description"));
        inputMap.put("Status_id", serviceInfo.optString("Status_id"));
        inputMap.put("MaxTransferLimit", serviceInfo.optString("MaxTransferLimit"));
        inputMap.put("MinTransferLimit", serviceInfo.optString("MinTransferLimit"));
        inputMap.put("TransferDenominations", serviceInfo.optString("TransferDenominations"));
        inputMap.put("IsFutureTransaction", serviceInfo.optString("IsFutureTransaction"));
        inputMap.put("TransactionCharges", serviceInfo.optString("TransactionCharges"));
        inputMap.put("IsAuthorizationRequired", serviceInfo.optString("IsAuthorizationRequired"));
        inputMap.put("IsSMSAlertActivated", serviceInfo.optString("IsSMSAlertActivated"));
        inputMap.put("SMSCharges", serviceInfo.optString("SMSCharges"));
        inputMap.put("IsBeneficiarySMSAlertActivated", serviceInfo.optString("IsBeneficiarySMSAlertActivated"));
        inputMap.put("BeneficiarySMSCharge", serviceInfo.optString("BeneficiarySMSCharge"));
        inputMap.put("HasWeekendOperation", serviceInfo.optString("HasWeekendOperation"));
        inputMap.put("IsOutageMessageActive", serviceInfo.optString("IsOutageMessageActive"));
        inputMap.put("IsAlertActive", serviceInfo.optString("IsAlertActive"));
        inputMap.put("IsTCActive", serviceInfo.optString("IsTCActive"));
        inputMap.put("IsAgreementActive", serviceInfo.optString("IsAgreementActive"));
        inputMap.put("IsCampaignActive", serviceInfo.optString("IsCampaignActive"));
        inputMap.put("WorkSchedule_id", serviceInfo.optString("WorkSchedule_id"));
        inputMap.put("createdby", loggedInUserId);
        inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());

        String createServiceResponse = Executor.invokeService(ServiceURLEnum.SERVICE_CREATE, inputMap, null,
                requestInstance);
        JSONObject createServiceResponseJSON = CommonUtilities.getStringAsJSONObject(createServiceResponse);

        if (createServiceResponseJSON != null && createServiceResponseJSON.has(FabricConstants.OPSTATUS)
                && createServiceResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            LOG.debug("Service Created successfully");
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SERVICES, EventEnum.CREATE,
                    ActivityStatusEnum.SUCCESSFUL,
                    "Service created successfully. Service Name: " + serviceInfo.optString("Name"));
            result.addParam(new Param("status", "success", FabricConstants.STRING));
            result.addParam(new Param("Service_Id", Service_id, FabricConstants.STRING));

            // create service channel mapping
            createServiceChannel(Service_id, serviceInfo.optString("Channel_id"), requestInstance, result);
        } else {
            LOG.error("Failed to create service. Response:" + createServiceResponse);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.SERVICES, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED,
                    "Service create failed. Service Name: " + ((JSONObject) newService.get(0)).optString("Name"));
            result.addParam(new Param("status", "failed", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20381.setErrorCode(result);
        }

        return result;
    }

    public void createServiceChannel(String service_id, String channel_ids, DataControllerRequest requestInstance,
            Result processedResult) {
        JSONArray channels = null;

        if (StringUtils.isBlank(channel_ids)) {
            return;
        }
        try {
            channels = new JSONArray(channel_ids);
        } catch (JSONException je) {
            channels = new JSONArray();
            channels.put(channel_ids);
        }
        channels.forEach((channel) -> {
            Map<String, String> inputMap = new HashMap<>();
            inputMap.put("Service_id", service_id);
            inputMap.put("Channel_id", (String) channel);
            String createServiceChannelResponse = Executor.invokeService(ServiceURLEnum.SERVICE_CHANNELS_CREATE,
                    inputMap, null, requestInstance);
            JSONObject createServiceResponseJSON = CommonUtilities.getStringAsJSONObject(createServiceChannelResponse);
            if (createServiceResponseJSON == null || !createServiceResponseJSON.has(FabricConstants.OPSTATUS)
                    || createServiceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                ErrorCodeEnum.ERR_20389.setErrorCode(processedResult);
                processedResult.addParam(new Param("FailureReason", createServiceChannelResponse));
                return;
            }
        });

    }
}