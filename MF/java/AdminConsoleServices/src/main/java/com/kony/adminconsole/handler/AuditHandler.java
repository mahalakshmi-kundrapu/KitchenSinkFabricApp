package com.kony.adminconsole.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.core.config.EnvironmentConfiguration;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.controller.DataControllerRequest;

public class AuditHandler {

    private static final Logger LOG = Logger.getLogger(AuditHandler.class);

    public static final String LOG_SERVICES_API_ACCESS_TOKEN_HEADER = "X-Kony-Log-Services-API-Access-Token";

    public static String auditAdminActivity(DataControllerRequest requestInstance, ModuleNameEnum moduleName,
            EventEnum event, ActivityStatusEnum status, String description) {
        try {
            if (requestInstance.getAttribute("isServiceBeingAccessedByOLB") != null
                    && Boolean.parseBoolean(requestInstance.getAttribute("isServiceBeingAccessedByOLB").toString()))
                return null;

            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);

            JSONObject auditInformation = new JSONObject();
            auditInformation.put("logType", "AdminActivity");
            auditInformation.put("username", userDetailsBeanInstance.getUserName());
            auditInformation.put("userRole", userDetailsBeanInstance.getRoleName());
            auditInformation.put("moduleName", moduleName.getModuleNameAlias());
            auditInformation.put("event", event.getEventNameAlias());
            auditInformation.put("status", status.getStatusAlias());
            auditInformation.put("description", description);
            auditInformation.put("eventts", CommonUtilities.getISOFormattedLocalTimestamp());

            return auditLog(requestInstance, auditInformation);
        } catch (Exception e) {
            LOG.error("AdminConsole: Error occured while pushing log statement!", e);
            return null;
        }

    }

    public static String auditAdminActivity(DataControllerRequest requestInstance, String username, String userRole,
            ModuleNameEnum moduleName, EventEnum event, ActivityStatusEnum status, String description) {

        try {
            if (requestInstance.getAttribute("isServiceBeingAccessedByOLB") != null
                    && Boolean.parseBoolean(requestInstance.getAttribute("isServiceBeingAccessedByOLB").toString()))
                return null;

            JSONObject auditInformation = new JSONObject();
            auditInformation.put("logType", "AdminActivity");
            auditInformation.put("username", username);
            auditInformation.put("userRole", userRole);
            auditInformation.put("moduleName", moduleName.getModuleNameAlias());
            auditInformation.put("event", event.getEventNameAlias());
            auditInformation.put("status", status.getStatusAlias());
            auditInformation.put("description", description);
            auditInformation.put("eventts", CommonUtilities.getISOFormattedLocalTimestamp());

            return auditLog(requestInstance, auditInformation);
        } catch (Exception e) {
            LOG.error("AdminConsole: Error occured while pushing log statement!", e);
            return null;
        }
    }

    private static String auditLog(DataControllerRequest requestInstance, JSONObject auditInformation) {
        Map<String, String> headerParametersMap = new HashMap<String, String>();
        headerParametersMap.put(LOG_SERVICES_API_ACCESS_TOKEN_HEADER,
                EnvironmentConfiguration.AC_LOG_SERVICES_API_ACCESS_TOKEN.getValue(requestInstance));
        String auditActionResponse = HTTPOperations.hitPOSTServiceAndGetResponse(
                ServiceURLEnum.LOGMANAGEMENT_AUDITLOG.getServiceURL(requestInstance), auditInformation, null,
                headerParametersMap);

        return auditActionResponse;
    }

}
