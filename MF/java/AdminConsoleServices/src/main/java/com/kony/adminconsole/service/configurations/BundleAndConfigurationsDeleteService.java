package com.kony.adminconsole.service.configurations;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
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
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to delete configuration bundles & configurations at the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class BundleAndConfigurationsDeleteService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(BundleAndConfigurationsDeleteService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        Result result = new Result();

        try {

            String bundleId = requestInstance.getParameter("bundleId");
            String configurationId = requestInstance.getParameter("configurationId");

            if (bundleId != null && !bundleId.equals("")) {

                // ** Reading entries from 'configurations' table **
                Map<String, String> configurationsTableMap = new HashMap<>();
                configurationsTableMap.put(ODataQueryConstants.SELECT, "configuration_id");
                configurationsTableMap.put(ODataQueryConstants.FILTER, "bundle_id eq '" + bundleId + "'");

                String readConfigurationsResponse = Executor.invokeService(ServiceURLEnum.CONFIGURATION_READ,
                        configurationsTableMap, null, requestInstance);
                JSONObject readConfigurationsResponseJSON = CommonUtilities
                        .getStringAsJSONObject(readConfigurationsResponse);

                if (readConfigurationsResponseJSON != null
                        && readConfigurationsResponseJSON.has(FabricConstants.OPSTATUS)
                        && readConfigurationsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                        && readConfigurationsResponseJSON.has("configurations")) {

                    JSONArray readConfigurationsResponseJSONArray = readConfigurationsResponseJSON
                            .getJSONArray("configurations");
                    for (int i = 0; i < readConfigurationsResponseJSONArray.length(); ++i) {

                        JSONObject readConfigurationsResponseJSONObject = readConfigurationsResponseJSONArray
                                .getJSONObject(i);
                        result = deleteConfiguration(authToken, requestInstance,
                                readConfigurationsResponseJSONObject.getString("configuration_id"), result);
                    }

                } else {
                    AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONFIGURATIONSFRAMEWORK,
                            EventEnum.DELETE, ActivityStatusEnum.FAILED, "Configuration delete failed");
                    throw new ApplicationException(ErrorCodeEnum.ERR_20488);
                }

                result = deleteBundle(authToken, requestInstance, bundleId, result);

            } else if (configurationId != null && !configurationId.equals("")) {

                result = deleteConfiguration(authToken, requestInstance, configurationId, result);
            }

        } catch (ApplicationException ae) {

            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in BundleAndConfigurationsDeleteService. Error: ", ae);

        } catch (Exception e) {

            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONFIGURATIONSFRAMEWORK, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "Configurations bundle / configuration delete failed");
            LOG.error("Exception occured in BundleAndConfigurationsDeleteService. Error: ", e);

        }

        return result;
    }

    public Result deleteBundle(String authToken, DataControllerRequest requestInstance, String bundleId, Result result)
            throws ApplicationException {

        if (StringUtils.isBlank(bundleId)) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20471);
        }

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("bundle_id", bundleId);

        String deleteBundleResponse = Executor.invokeService(ServiceURLEnum.CONFIGURATIONSBUNDLE_DELETE,
                postParametersMap, null, requestInstance);

        JSONObject deleteBundleResponseJSON = CommonUtilities.getStringAsJSONObject(deleteBundleResponse);
        if (deleteBundleResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONFIGURATIONSFRAMEWORK, EventEnum.DELETE,
                    ActivityStatusEnum.SUCCESSFUL, "Configurations bundle delete successful");
        } else {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONFIGURATIONSFRAMEWORK, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "Configurations bundle delete failed");
            throw new ApplicationException(ErrorCodeEnum.ERR_20484);
        }

        return result;
    }

    public Result deleteConfiguration(String authToken, DataControllerRequest requestInstance, String configurationId,
            Result result) throws ApplicationException {

        if (StringUtils.isBlank(configurationId)) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20472);
        }

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("configuration_id", configurationId);

        String deleteConfigurationResponse = Executor.invokeService(ServiceURLEnum.CONFIGURATION_DELETE,
                postParametersMap, null, requestInstance);

        JSONObject deleteConfigurationResponseJSON = CommonUtilities.getStringAsJSONObject(deleteConfigurationResponse);
        if (deleteConfigurationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONFIGURATIONSFRAMEWORK, EventEnum.DELETE,
                    ActivityStatusEnum.SUCCESSFUL, "Configuration delete successful");
        } else {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONFIGURATIONSFRAMEWORK, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "Configuration delete failed");
            throw new ApplicationException(ErrorCodeEnum.ERR_20488);
        }

        return result;
    }
}