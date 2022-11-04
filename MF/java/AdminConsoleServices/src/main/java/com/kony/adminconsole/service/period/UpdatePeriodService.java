package com.kony.adminconsole.service.period;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class UpdatePeriodService implements JavaService2 {

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        Param errorCodeParam;
        Param errorMessageParam;
        Param statusParam = null;
        Result processedResult = new Result();
        Result result = new Result();
        String periodId = requestInstance.getParameter("periodId");
        ;
        String periodName = requestInstance.getParameter("periodName");
        ;
        Integer dayCount = requestInstance.getParameter("dayCount") != null
                ? Integer.parseInt(requestInstance.getParameter("dayCount"))
                : -1;
        Integer order = requestInstance.getParameter("order") != null
                ? Integer.parseInt(requestInstance.getParameter("order"))
                : -1;
        String code = requestInstance.getParameter("code");
        String systemUser = requestInstance.getParameter("systemUserId");
        String isEditable = requestInstance.getParameter("isEditable");
        String deleteFlag = requestInstance.getParameter("deleteFlag");

        if (StringUtils.isBlank(periodId)) {
            errorCodeParam = new Param("errorCode", "5025", "string");
            errorMessageParam = new Param("errorMessage", "PERIOD_ID_EMPTY", "string");
            processedResult.addParam(errorCodeParam);
            processedResult.addParam(errorMessageParam);
            return processedResult;
        }
        if (StringUtils.isBlank(periodName)) {
            errorCodeParam = new Param("errorCode", "5026", "string");
            errorMessageParam = new Param("errorMessage", "PERIOD_NAME_EMPTY", "string");
            processedResult.addParam(errorCodeParam);
            processedResult.addParam(errorMessageParam);
            return processedResult;
        }
        if (dayCount == -1) {
            errorCodeParam = new Param("errorCode", "5027", "string");
            errorMessageParam = new Param("errorMessage", "PERIOD_DAY_COUNT_EMPTY", "string");
            processedResult.addParam(errorCodeParam);
            processedResult.addParam(errorMessageParam);
            return processedResult;
        }
        if (order == -1) {
            errorCodeParam = new Param("errorCode", "5028", "string");
            errorMessageParam = new Param("errorMessage", "PERIOD_ORDER_EMPTY", "string");
            processedResult.addParam(errorCodeParam);
            processedResult.addParam(errorMessageParam);
            return processedResult;
        }
        if (StringUtils.isBlank(code)) {
            errorCodeParam = new Param("errorCode", "5029", "string");
            errorMessageParam = new Param("errorMessage", "PERIOD_CODE_EMPTY", "string");
            processedResult.addParam(errorCodeParam);
            processedResult.addParam(errorMessageParam);
            return processedResult;
        }
        if (isEditable == null) {
            errorCodeParam = new Param("errorCode", "5029", "string");
            errorMessageParam = new Param("errorMessage", "ISEDITABLE_EMPTY", "string");
            processedResult.addParam(errorCodeParam);
            processedResult.addParam(errorMessageParam);
            return processedResult;
        }
        if (requestInstance.getParameter("deleteFlag") == null) {
            errorCodeParam = new Param("errorCode", "5029", "string");
            errorMessageParam = new Param("errorMessage", "DELETE_FLAG_EMPTY", "string");
            processedResult.addParam(errorCodeParam);
            processedResult.addParam(errorMessageParam);
            return processedResult;
        }
        try {
            if (("1").equalsIgnoreCase(isEditable)) {
                if (("Y").equalsIgnoreCase(deleteFlag)) {
                    JSONObject deletePeriodResponseJSON = deletePeriod(periodId, authToken, systemUser,
                            requestInstance);
                    if (deletePeriodResponseJSON != null
                            && deletePeriodResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                        // create call back to delete inserted record in role table
                        return constructFailureResult(deletePeriodResponseJSON);
                    }
                }
                if (periodId == null || periodId.isEmpty() || periodId.trim().length() == 0) {
                    periodId = CommonUtilities.getNewId().toString();
                    JSONObject createPeriod = createPeriod(periodId, periodName, dayCount, order, code, systemUser,
                            authToken, requestInstance);
                    if (createPeriod.getInt(FabricConstants.OPSTATUS) != 0) {
                        return constructFailureResult(createPeriod);
                    }
                } else {
                    JSONObject updatePeriod = updatePeriod(periodId, periodName, dayCount, order, code, systemUser,
                            authToken, requestInstance);
                    if (updatePeriod.getInt(FabricConstants.OPSTATUS) != 0) {
                        return constructFailureResult(updatePeriod);
                    }
                }
            } else if (("0").equalsIgnoreCase(isEditable)) {
                errorCodeParam = new Param("errorCode", "5042", "string");
                errorMessageParam = new Param("errorMessage", "NON_CUSTOM_PERIODS_CANNOT_BE_UPDATED", "string");
                processedResult.addParam(errorCodeParam);
                processedResult.addParam(errorMessageParam);
                return processedResult;
            }

        } catch (Exception e) {
            statusParam = new Param("Status", "Error", "string");
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Period update failed. Period_ID: " + periodId);
            result.addParam(statusParam);
        }
        result.addParam(statusParam);
        return result;
    }

    private JSONObject deletePeriod(String periodId, String authToken, String systemUser,
            DataControllerRequest requestInstance) {
        JSONObject updateResponseJSON = null;

        String periodName = null;
        try {
            Map<String, String> readPostParametersMap = new HashMap<String, String>();
            readPostParametersMap.put(ODataQueryConstants.FILTER, "id eq " + periodId);
            JSONObject roleReadResponse = CommonUtilities.getStringAsJSONObject(
                    Executor.invokeService(ServiceURLEnum.PERIOD_READ, readPostParametersMap, null, requestInstance));
            periodName = roleReadResponse.getJSONArray("period").getJSONObject(0).getString("Name");
        } catch (Exception ignored) {
        }

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        postParametersMap.put("id", periodId);
        String updatePeriodicLimitResponse = Executor.invokeService(ServiceURLEnum.PERIOD_DELETE, postParametersMap,
                null, requestInstance);

        updateResponseJSON = CommonUtilities.getStringAsJSONObject(updatePeriodicLimitResponse);
        int getOpStatusCode = updateResponseJSON.getInt(FabricConstants.OPSTATUS);
        if (getOpStatusCode != 0) {
            // rollback logic
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "Period delete failed. Period Name: " + periodName);
            return updateResponseJSON;
        }
        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.DELETE,
                ActivityStatusEnum.SUCCESSFUL, "Period deleted successfully. Period Name: " + periodName);
        return updateResponseJSON;
    }

    private JSONObject createPeriod(String periodId, String periodName, Integer dayCount, Integer order, String code,
            String systemUser, String authToken, DataControllerRequest requestInstance) {
        JSONObject createResponseJSON = null;
        Integer isEditable = 1;
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        postParametersMap.put("id", periodId);
        postParametersMap.put("Name", periodName);
        postParametersMap.put("DayCount", dayCount.toString());
        postParametersMap.put("Order", order.toString());
        postParametersMap.put("Code", code);
        postParametersMap.put("isEditable", isEditable.toString());
        postParametersMap.put("createdby", systemUser);
        postParametersMap.put("modifiedby", "NULL");
        postParametersMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
        postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
        postParametersMap.put("synctimestamp", CommonUtilities.getISOFormattedLocalTimestamp());
        postParametersMap.put("softdeleteflag", "0");
        String createPeriodResponse = Executor.invokeService(ServiceURLEnum.PERIOD_CREATE, postParametersMap, null,
                requestInstance);

        createResponseJSON = CommonUtilities.getStringAsJSONObject(createPeriodResponse);
        int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);
        if (StringUtils.isBlank(periodName)) {
            try {
                Map<String, String> readPostParametersMap = new HashMap<String, String>();
                readPostParametersMap.put(ODataQueryConstants.FILTER, "id eq " + periodId);
                JSONObject roleReadResponse = CommonUtilities.getStringAsJSONObject(Executor
                        .invokeService(ServiceURLEnum.PERIOD_READ, readPostParametersMap, null, requestInstance));
                periodName = roleReadResponse.getJSONArray("period").getJSONObject(0).getString("Name");
            } catch (Exception ignored) {
            }
        }

        if (getOpStatusCode != 0) {
            // rollback logic
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "Period creation failed. Period Name: " + periodName);
            return createResponseJSON;
        }
        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
                ActivityStatusEnum.SUCCESSFUL, "Period created successfully. Period Name: " + periodName);
        return createResponseJSON;
    }

    private JSONObject updatePeriod(String periodId, String periodName, Integer dayCount, Integer order, String code,
            String systemUser, String authToken, DataControllerRequest requestInstance) {
        JSONObject createResponseJSON = null;
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        postParametersMap.put("id", periodId);
        postParametersMap.put("Name", periodName);
        postParametersMap.put("DayCount", dayCount.toString());
        postParametersMap.put("Order", order.toString());
        postParametersMap.put("Code", code);
        postParametersMap.put("modifiedby", systemUser);
        String createPeriodResponse = Executor.invokeService(ServiceURLEnum.PERIOD_UPDATE, postParametersMap, null,
                requestInstance);

        createResponseJSON = CommonUtilities.getStringAsJSONObject(createPeriodResponse);
        int getOpStatusCode = createResponseJSON.getInt(FabricConstants.OPSTATUS);

        if (StringUtils.isBlank(periodName)) {
            try {
                Map<String, String> readPostParametersMap = new HashMap<String, String>();
                readPostParametersMap.put(ODataQueryConstants.FILTER, "id eq " + periodId);
                JSONObject roleReadResponse = CommonUtilities.getStringAsJSONObject(Executor
                        .invokeService(ServiceURLEnum.PERIOD_READ, readPostParametersMap, null, requestInstance));
                periodName = roleReadResponse.getJSONArray("period").getJSONObject(0).getString("Name");
            } catch (Exception ignored) {
            }
        }

        if (getOpStatusCode != 0) {
            // rollback logic
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Period update failed. Period Name: " + periodName);
            return createResponseJSON;
        }
        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.UPDATE,
                ActivityStatusEnum.SUCCESSFUL, "Period updated successfully. Period Name: " + periodName);
        return createResponseJSON;
    }

    private Result constructFailureResult(JSONObject ResponseJSON) {
        Result Result = new Result();
        Param errCodeParam = new Param("backend_error_code", "" + ResponseJSON.getInt(FabricConstants.OPSTATUS),
                FabricConstants.INT);
        Param errMsgParam = new Param("backend_error_message", ResponseJSON.toString(), "string");
        Param serviceMessageParam = new Param("errmsg", ResponseJSON.toString(), "string");
        Param statusParam = new Param("Status", "Failure", "string");
        Result.addParam(errCodeParam);
        Result.addParam(errMsgParam);
        Result.addParam(serviceMessageParam);
        Result.addParam(statusParam);
        return Result;
    }
}
