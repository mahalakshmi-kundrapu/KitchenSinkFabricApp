package com.kony.adminconsole.service.staticcontentmanagement.outagemessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.DateUtils;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.exception.ApplicationException;
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

public class OutageMessageCreateService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(OutageMessageCreateService.class);
    private static final SimpleDateFormat TARGET_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result result = new Result();

            String service_id = requestInstance.getParameter("Service_id");
            String status_id = requestInstance.getParameter("Status_id");
            String outageName = requestInstance.getParameter("Name");
            String outageText = requestInstance.getParameter("MessageText");
            String app_ids_Added = requestInstance.getParameter("App_ids_Added");
            String startTime = requestInstance.getParameter("Start_Time");
            String endTime = requestInstance.getParameter("End_Time");
            String timezoneOffset = requestInstance.getParameter("timezoneOffset");

            if (StringUtils.isBlank(status_id)) {
                ErrorCodeEnum.ERR_20186.setErrorCode(result);
                return result;
            }
            if (StringUtils.isBlank(outageName)) {
                ErrorCodeEnum.ERR_20187.setErrorCode(result);
                return result;
            }
            if (StringUtils.isBlank(outageText)) {
                ErrorCodeEnum.ERR_20188.setErrorCode(result);
                return result;
            }
            if (StringUtils.isBlank(startTime)) {
                ErrorCodeEnum.ERR_20189.setErrorCode(result);
                return result;
            }
            if (StringUtils.isBlank(endTime)) {
                ErrorCodeEnum.ERR_20190.setErrorCode(result);
                return result;
            }
            if (StringUtils.isBlank(app_ids_Added)) {
                ErrorCodeEnum.ERR_20191.setErrorCode(result);
                return result;
            }
            if (StringUtils.isBlank(timezoneOffset)) {
                ErrorCodeEnum.ERR_20192.setErrorCode(result);
                return result;
            }

            try {
                startTime = DateUtils.convertLocalDateTimeToServerZone(startTime, Integer.parseInt(timezoneOffset));
                endTime = DateUtils.convertLocalDateTimeToServerZone(endTime, Integer.parseInt(timezoneOffset));

            } catch (ParseException pe) {
                ErrorCodeEnum.ERR_20193.setErrorCode(result);
                return result;
            }

            JSONArray appIdsArray = new JSONArray(app_ids_Added);
            if (!validateStartAndEndTimes(requestInstance, result, startTime, endTime, appIdsArray)) {
                result.addParam(new Param(ErrorCodeEnum.ERROR_CODE_KEY,
                        String.valueOf(ErrorCodeEnum.ERR_20197.getErrorCode())));
                result.addParam(new Param(ErrorCodeEnum.ERROR_MESSAGE_KEY,
                        requestInstance.getAttribute("MaxActiveMessages") + ErrorCodeEnum.ERR_20197.getMessage()));
                return result;
            }
            if (!validateName(requestInstance, result, outageName, null)) {
                ErrorCodeEnum.ERR_20198.setErrorCode(result);
                return result;
            }
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);

            Map<String, String> postParametersMap = new HashMap<String, String>();
            String id = CommonUtilities.getNewId().toString();
            postParametersMap.clear();
            postParametersMap.put("id", id.toString());
            if (StringUtils.isNotBlank(service_id)) {
                postParametersMap.put("Service_id", service_id);
            }

            postParametersMap.put("Status_id", status_id);
            postParametersMap.put("name", outageName);
            postParametersMap.put("MessageText", outageText);
            postParametersMap.put("startTime", startTime);
            postParametersMap.put("endTime", endTime);
            postParametersMap.put("createdby", userDetailsBeanInstance.getUserName());
            postParametersMap.put("modifiedby", userDetailsBeanInstance.getUserName());

            String createResponse = Executor.invokeService(ServiceURLEnum.OUTAGEMESSAGE_CREATE, postParametersMap, null,
                    requestInstance);
            JSONObject createJSON = CommonUtilities.getStringAsJSONObject(createResponse);
            if (createJSON == null || !createJSON.has(FabricConstants.OPSTATUS)
                    || createJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                result.addParam(new Param("Status", "Error", FabricConstants.STRING));
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.OUTAGEMESSAGE, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED,
                        "Outage message create failed Outage name: " + outageName + " Outage Message: " + outageText
                                + " Start time: " + TARGET_FORMAT.format(INPUT_FORMAT.parse(startTime))
                                + " UTC. End time: " + TARGET_FORMAT.format(INPUT_FORMAT.parse(endTime)) + " UTC");
                ErrorCodeEnum.ERR_20182.setErrorCode(result);
                result.addParam(new Param("FailureReason", createResponse, FabricConstants.STRING));
                return result;
            }

            // Add App to outage message mapping
            OutageMessageEditService.addAppMapping(requestInstance, result, id, app_ids_Added);
            if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                return result;
            }

            result.addParam(new Param("Status", "Success", FabricConstants.STRING));
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.OUTAGEMESSAGE, EventEnum.CREATE,
                    ActivityStatusEnum.SUCCESSFUL,
                    "Outage message create successful. Outage name: " + outageName + " Outage Message: " + outageText
                            + " Start time: " + TARGET_FORMAT.format(INPUT_FORMAT.parse(startTime)) + " UTC. End time: "
                            + TARGET_FORMAT.format(INPUT_FORMAT.parse(endTime)) + " UTC");
            return result;

        } catch (ApplicationException ae) {
            Result errorResult = new Result();
            ae.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace: ", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    public static Boolean validateStartAndEndTimes(DataControllerRequest requestInstance, Result result,
            String startTime, String endTime, JSONArray app_ids_Added) throws ApplicationException {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "(startTime le '" + startTime + "' and endTime ge '"
                + startTime + "') or" + "( startTime le '" + endTime + "' and endTime ge '" + endTime + "')");

        String readResponse = Executor.invokeService(ServiceURLEnum.OUTAGEMESSAGE_READ, postParametersMap, null,
                requestInstance);
        JSONObject readResponseJSON = CommonUtilities.getStringAsJSONObject(readResponse);
        if (readResponseJSON == null || !readResponseJSON.has(FabricConstants.OPSTATUS)
                || readResponseJSON.getInt(FabricConstants.OPSTATUS) != 0 || !readResponseJSON.has("outagemessage")) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20185);
        }
        String filter = "";
        JSONArray outagesArray = readResponseJSON.getJSONArray("outagemessage");

        if (outagesArray.length() == 0) {
            return true;
        }
        for (int i = 0; i < outagesArray.length(); i++) {
            JSONObject outage = outagesArray.getJSONObject(i);
            if (StringUtils.isNotBlank(filter)) {
                filter += " or ";
            }
            filter += "Outagemessage_id eq '" + outage.getString("id") + "'";
        }

        postParametersMap.clear();
        postParametersMap.put(ODataQueryConstants.FILTER, filter);
        String readAppResponse = Executor.invokeService(ServiceURLEnum.OUTAGEMESSAGEAPP_READ, postParametersMap, null,
                requestInstance);
        JSONObject readAppResponseJSON = CommonUtilities.getStringAsJSONObject(readAppResponse);
        if (readAppResponseJSON == null || !readAppResponseJSON.has(FabricConstants.OPSTATUS)
                || readAppResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || !readAppResponseJSON.has("outagemessageapp")) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20199);
        }
        JSONArray appMapping = readAppResponseJSON.getJSONArray("outagemessageapp");
        Map<String, Integer> outageAppCount = new HashMap<>();
        appMapping.forEach((appMapObject) -> {
            JSONObject appMap = (JSONObject) appMapObject;
            if (outageAppCount.containsKey(appMap.getString("App_id"))) {
                int previousCtr = outageAppCount.get(appMap.getString("App_id"));
                previousCtr++;
                outageAppCount.put(appMap.getString("App_id"), previousCtr);
            } else {
                outageAppCount.put(appMap.getString("App_id"), 1);
            }
        });

        int maxActiveMessages = getMaxValueFromConfiguration(requestInstance);
        requestInstance.setAttribute("MaxActiveMessages", maxActiveMessages);

        for (Object appIdObject : app_ids_Added) {
            String appId = (String) appIdObject;
            if (outageAppCount.containsKey(appId) && outageAppCount.get(appId) >= maxActiveMessages) {
                return false;
            }
        }
        ;

        return true;
    }

    private static int getMaxValueFromConfiguration(DataControllerRequest requestInstance) throws ApplicationException {
        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put(ODataQueryConstants.FILTER,
                "bundle_id eq 'C360_CONFIG_BUNDLE' and config_key eq 'CONCURRENT_ACTIVE_OUTAGE_MESSAGES'");
        String readResponse = Executor.invokeService(ServiceURLEnum.CONFIGURATION_READ, postParametersMap, null,
                requestInstance);
        JSONObject readResponseJSON = CommonUtilities.getStringAsJSONObject(readResponse);
        if (readResponseJSON == null || !readResponseJSON.has(FabricConstants.OPSTATUS)
                || readResponseJSON.getInt(FabricConstants.OPSTATUS) != 0 || !readResponseJSON.has("configurations")) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20200);
        }
        try {
            return Integer.parseInt(
                    readResponseJSON.getJSONArray("configurations").getJSONObject(0).getString("config_value"));
        } catch (Exception e) {
            LOG.error("Unable to parse the runtime configuration 'CONCURRENT_ACTIVE_OUTAGE_MESSAGES'");
            // Return default setting
            return 2;
        }
    }

    public static Boolean validateName(DataControllerRequest requestInstance, Result result, String name,
            String outageId) throws ApplicationException {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        String filter = "";
        if (StringUtils.isNotBlank(outageId)) {
            filter += "id ne '" + outageId + "'";
        }
        if (StringUtils.isNotBlank(filter)) {
            filter += " and ";
        }
        filter += "name eq '" + name + "'";
        postParametersMap.put(ODataQueryConstants.FILTER, filter);

        String readResponse = Executor.invokeService(ServiceURLEnum.OUTAGEMESSAGE_READ, postParametersMap, null,
                requestInstance);
        JSONObject readResponseJSON = CommonUtilities.getStringAsJSONObject(readResponse);
        if (readResponseJSON == null || !readResponseJSON.has(FabricConstants.OPSTATUS)
                || readResponseJSON.getInt(FabricConstants.OPSTATUS) != 0 || !readResponseJSON.has("outagemessage")) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20185);
        }
        if (readResponseJSON.getJSONArray("outagemessage").length() > 0) {
            return false;
        }
        return true;
    }
}