package com.kony.adminconsole.service.systemconfiguration;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
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

/**
 * Service to update the SystemConfiguration Values
 *
 * @author Aditya Mankal
 * 
 */
public class SystemConfigurationUpdateService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(SystemConfigurationUpdateService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();

            Map<String, String> postParametersMap = new HashMap<String, String>();

            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            String userID = userDetailsBeanInstance.getUserId();
            String propertyName = requestInstance.getParameter("propertyName");
            String propertyValue = requestInstance.getParameter("propertyValue");
            postParametersMap.put("PropertyName", propertyName);
            postParametersMap.put("PropertyValue", propertyValue);
            postParametersMap.put("modifiedby", userID);
            postParametersMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

            String updateSystemConfigurationResponse = Executor.invokeService(ServiceURLEnum.SYSTEMCONFIGURATION_UPDATE,
                    postParametersMap, null, requestInstance);
            JSONObject updateSystemConfigurationResponseJSON = CommonUtilities
                    .getStringAsJSONObject(updateSystemConfigurationResponse);
            if (updateSystemConfigurationResponseJSON != null
                    && updateSystemConfigurationResponseJSON.has(FabricConstants.OPSTATUS)
                    && updateSystemConfigurationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {

                if (updateSystemConfigurationResponseJSON.has("updatedRecords")) {
                    Param updatedRecords_Param = new Param("updatedRecords",
                            Integer.toString(updateSystemConfigurationResponseJSON.getInt("updatedRecords")),
                            FabricConstants.INT);
                    processedResult.addParam(updatedRecords_Param);
                    if (updateSystemConfigurationResponseJSON.getInt("updatedRecords") >= 1) {
                        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOGIN, EventEnum.UPDATE,
                                ActivityStatusEnum.SUCCESSFUL, "System configurations update success.");
                    }
                }
                return processedResult;
            }
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOGIN, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "System configurations update failed.");
            ErrorCodeEnum.ERR_20282.setErrorCode(processedResult);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}