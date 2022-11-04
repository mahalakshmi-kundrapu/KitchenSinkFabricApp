package com.kony.adminconsole.service.staticcontentmanagement.outagemessage;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
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

public class OutageMessageDeleteService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(OutageMessageDeleteService.class);
    private static final SimpleDateFormat TARGET_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Map<String, String> postParametersMap = new HashMap<String, String>();
            Param statusParam = new Param("Status", "Success", FabricConstants.STRING);
            Result result = new Result();

            JSONArray OutageMessageIds = new JSONArray(requestInstance.getParameter("OutageMessageIds"));
            StringBuilder filter = new StringBuilder("");
            OutageMessageIds.forEach((outageObject) -> {
                JSONObject outage = (JSONObject) outageObject;
                if (StringUtils.isNotBlank(filter)) {
                    filter.append(" or ");
                }
                filter.append("id eq '" + outage.getString("id") + "'");
            });
            postParametersMap.put(ODataQueryConstants.FILTER, filter.toString());
            JSONObject readOutageMessages = CommonUtilities.getStringAsJSONObject(Executor
                    .invokeService(ServiceURLEnum.OUTAGEMESSAGE_READ, postParametersMap, null, requestInstance));

            if (readOutageMessages == null || !readOutageMessages.has(FabricConstants.OPSTATUS)
                    || readOutageMessages.getInt(FabricConstants.OPSTATUS) != 0
                    || !readOutageMessages.has("outagemessage")) {
                ErrorCodeEnum.ERR_20185.setErrorCode(result);
                return result;
            }
            Map<String, JSONObject> outageDetails = getOutageMap(readOutageMessages.getJSONArray("outagemessage"));

            postParametersMap.clear();
            for (int i = 0; i < OutageMessageIds.length(); i++) {
                postParametersMap.clear();
                JSONObject currentRecord = (JSONObject) OutageMessageIds.get(i);
                // Delete app association
                deleteAppMapping(requestInstance, result, currentRecord.getString("id"));

                if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                    statusParam = new Param(currentRecord.getString("id"), "Error", FabricConstants.STRING);
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.OUTAGEMESSAGE, EventEnum.DELETE,
                            ActivityStatusEnum.FAILED,
                            "Outage message delete failed. Outage name: "
                                    + outageDetails.get(currentRecord.getString("id")).getString("name")
                                    + " Outage Message: "
                                    + outageDetails.get(currentRecord.getString("id")).getString("MessageText")
                                    + " Start time: "
                                    + TARGET_FORMAT.format(INPUT_FORMAT.parse(
                                            outageDetails.get(currentRecord.getString("id")).getString("startTime")))
                                    + " UTC." + " End time: "
                                    + TARGET_FORMAT.format(INPUT_FORMAT.parse(
                                            outageDetails.get(currentRecord.getString("id")).getString("endTime")))
                                    + " UTC");
                    continue;
                }

                postParametersMap.put("id", currentRecord.getString("id"));
                String deleteOutageMessageResponse = Executor.invokeService(ServiceURLEnum.OUTAGEMESSAGE_DELETE,
                        postParametersMap, null, requestInstance);
                JSONObject deleteOutageMessageURLJSON = CommonUtilities
                        .getStringAsJSONObject(deleteOutageMessageResponse);
                if (deleteOutageMessageURLJSON == null || !deleteOutageMessageURLJSON.has(FabricConstants.OPSTATUS)
                        || deleteOutageMessageURLJSON.getInt(FabricConstants.OPSTATUS) != 0) {

                    statusParam = new Param(currentRecord.getString("id"), "Error", FabricConstants.STRING);
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.OUTAGEMESSAGE, EventEnum.DELETE,
                            ActivityStatusEnum.FAILED,
                            "Outage message delete failed. Outage name: "
                                    + outageDetails.get(currentRecord.getString("id")).getString("name")
                                    + " Outage Message: "
                                    + outageDetails.get(currentRecord.getString("id")).getString("MessageText")
                                    + " Start time: "
                                    + TARGET_FORMAT.format(INPUT_FORMAT.parse(
                                            outageDetails.get(currentRecord.getString("id")).getString("startTime")))
                                    + " UTC." + " End time: "
                                    + TARGET_FORMAT.format(INPUT_FORMAT.parse(
                                            outageDetails.get(currentRecord.getString("id")).getString("endTime")))
                                    + " UTC");
                    ErrorCodeEnum.ERR_20184.setErrorCode(result);
                    continue;
                }

                statusParam = new Param(currentRecord.getString("id"), "Success", FabricConstants.STRING);
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.OUTAGEMESSAGE, EventEnum.DELETE,
                        ActivityStatusEnum.SUCCESSFUL,
                        "Outage message delete successful. Outage name: "
                                + outageDetails.get(currentRecord.getString("id")).getString("name")
                                + " Outage Message: "
                                + outageDetails.get(currentRecord.getString("id")).getString("MessageText")
                                + " Start time: "
                                + TARGET_FORMAT.format(INPUT_FORMAT
                                        .parse(outageDetails.get(currentRecord.getString("id")).getString("startTime")))
                                + " UTC." + " End time: "
                                + TARGET_FORMAT.format(INPUT_FORMAT
                                        .parse(outageDetails.get(currentRecord.getString("id")).getString("endTime")))
                                + " UTC");
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

    static Map<String, JSONObject> getOutageMap(JSONArray array) {
        Map<String, JSONObject> map = new HashMap<>();
        array.forEach((recordObject) -> {
            JSONObject record = (JSONObject) recordObject;
            map.put(record.getString("id"), record);
        });
        return map;
    }

    private static void deleteAppMapping(DataControllerRequest requestInstance, Result result, String id) {

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "Outagemessage_id eq '" + id + "'");
        String readResponse = Executor.invokeService(ServiceURLEnum.OUTAGEMESSAGEAPP_READ, postParametersMap, null,
                requestInstance);
        JSONObject readResponseJSON = CommonUtilities.getStringAsJSONObject(readResponse);
        if (readResponseJSON == null || !readResponseJSON.has(FabricConstants.OPSTATUS)
                || readResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || !readResponseJSON.has("outagemessageapp")) {
            ErrorCodeEnum.ERR_20195.setErrorCode(result);
            return;
        }
        JSONArray appMappingArray = readResponseJSON.getJSONArray("outagemessageapp");
        appMappingArray.forEach((outagemessageappObject) -> {
            JSONObject outagemessageappRecord = (JSONObject) outagemessageappObject;
            postParametersMap.clear();
            postParametersMap.put("Outagemessage_id", id);
            postParametersMap.put("App_id", outagemessageappRecord.getString("App_id"));

            String deleteOutageMessageResponse = Executor.invokeService(ServiceURLEnum.OUTAGEMESSAGEAPP_DELETE,
                    postParametersMap, null, requestInstance);
            JSONObject deleteOutageMessageResponseJSON = CommonUtilities
                    .getStringAsJSONObject(deleteOutageMessageResponse);
            if (deleteOutageMessageResponseJSON == null
                    || !deleteOutageMessageResponseJSON.has(FabricConstants.OPSTATUS)
                    || deleteOutageMessageResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                ErrorCodeEnum.ERR_20195.setErrorCode(result);
                return;
            }
        });

    }

}