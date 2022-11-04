package com.kony.adminconsole.service.period;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
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

public class CreatePeriodService implements JavaService2 {

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        Param errorCodeParam;
        Param errorMessageParam;
        Param statusParam = null;
        Result processedResult = new Result();
        Result result = new Result();
        String periodId = CommonUtilities.getNewId().toString();
        String periodName = "";
        Integer dayCount = 0;
        Integer order = 0;
        String code = "";
        String systemUser = "";
        if (requestInstance.getParameter("periodName") != null) {
            periodName = requestInstance.getParameter("periodName");
        } else {
            errorCodeParam = new Param("errorCode", "5021", "string");
            errorMessageParam = new Param("errorMessage", "PERIOD_NAME_EMPTY", "string");
            processedResult.addParam(errorCodeParam);
            processedResult.addParam(errorMessageParam);
            return processedResult;
        }
        if (requestInstance.getParameter("dayCount") != null) {
            dayCount = Integer.parseInt(requestInstance.getParameter("dayCount"));
        } else {
            errorCodeParam = new Param("errorCode", "5022", "string");
            errorMessageParam = new Param("errorMessage", "DAY_COUNT_EMPTY", "string");
            processedResult.addParam(errorCodeParam);
            processedResult.addParam(errorMessageParam);
            return processedResult;
        }
        if (requestInstance.getParameter("order") != null) {
            order = Integer.parseInt(requestInstance.getParameter("order"));
        } else {
            errorCodeParam = new Param("errorCode", "5023", "string");
            errorMessageParam = new Param("errorMessage", "PERIOD_ORDER_EMPTY", "string");
            processedResult.addParam(errorCodeParam);
            processedResult.addParam(errorMessageParam);
            return processedResult;
        }
        if (requestInstance.getParameter("code") != null) {
            code = requestInstance.getParameter("code");
        } else {
            errorCodeParam = new Param("errorCode", "5024", "string");
            errorMessageParam = new Param("errorMessage", "PERIOD_CODE_EMPTY", "string");
            processedResult.addParam(errorCodeParam);
            processedResult.addParam(errorMessageParam);
            return processedResult;
        }
        try {
            JSONObject createPeriod = createPeriod(periodId, periodName, dayCount, order, code, systemUser, authToken,
                    requestInstance);
            if (createPeriod.getInt(FabricConstants.OPSTATUS) != 0) {
                return constructFailureResult(createPeriod);
            }
            result.addParam(new Param("periodId", periodId, "string"));
        } catch (Exception e) {
            statusParam = new Param("Status", "Error", "string");
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.PERIODICLIMITS, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "Period creation failed. Period Name: " + periodName);
            result.addParam(statusParam);
        }
        result.addParam(statusParam);
        return result;
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
