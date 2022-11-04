package com.kony.adminconsole.service.configurations;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.dto.BundleBean;
import com.kony.adminconsole.dto.ConfigurationBean;
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
 * Service to create/update configuration bundles & configurations at the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class BundleAndConfigurationsManageService implements JavaService2 {

    public static final Pattern BUNDLENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\s]*$");
    public static final Pattern BUNDLEAPPID_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\s]*$");

    private static final Logger LOG = Logger.getLogger(BundleAndConfigurationsManageService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        Result result = new Result();

        EventEnum eventEnum = EventEnum.CREATE;

        String userId = LoggedInUserHandler.getUserDetails(requestInstance).getUserId();

        try {

            String bundleStatus = requestInstance.getParameter("bundleStatus");
            if (bundleStatus.equals("CREATE") || bundleStatus.equals("UPDATE")) {

                BundleBean bundleBean = new BundleBean();
                bundleBean.setUserId(userId);
                bundleBean.setBundleName(requestInstance.getParameter("bundleName"));
                bundleBean.setBundleAppId(requestInstance.getParameter("bundleAppId"));

                if (bundleStatus.equals("CREATE")) {

                    eventEnum = EventEnum.CREATE;
                    bundleBean.setBundleId(CommonUtilities.getNewId().toString());
                    result = createBundle(authToken, requestInstance, bundleBean, result);

                } else if (bundleStatus.equals("UPDATE")) {

                    eventEnum = EventEnum.UPDATE;
                    bundleBean.setBundleId(requestInstance.getParameter("bundleId"));
                    result = updateBundle(authToken, requestInstance, bundleBean, result);

                }

                if (requestInstance.getParameter("configurations") != null) {

                    JSONArray configurations = new JSONArray(requestInstance.getParameter("configurations"));
                    for (int i = 0; i < configurations.length(); ++i) {

                        JSONObject configuration = configurations.getJSONObject(i);

                        if (configuration.has("configurationStatus")) {

                            ConfigurationBean configurationBean = new ConfigurationBean();
                            configurationBean.setUserId(userId);
                            configurationBean.setBundleId(bundleBean.getBundleId());

                            configurationBean.setConfigurationKey(configuration.optString("configurationKey"));
                            configurationBean.setConfigurationValue(configuration.optString("configurationValue"));
                            configurationBean
                                    .setConfigurationDescription(configuration.optString("configurationDescription"));
                            configurationBean
                                    .setConfigurationType(configuration.optString("configurationType").toUpperCase());
                            configurationBean.setConfigurationTarget(
                                    configuration.optString("configurationTarget").toUpperCase());

                            if (configuration.getString("configurationStatus").equals("CREATE")) {

                                eventEnum = EventEnum.CREATE;
                                configurationBean.setConfigurationId(CommonUtilities.getNewId().toString());
                                result = createConfiguration(authToken, requestInstance, configurationBean, result);

                            } else if (configuration.getString("configurationStatus").equals("UPDATE")) {

                                eventEnum = EventEnum.UPDATE;
                                configurationBean.setConfigurationId(configuration.optString("configurationId"));
                                result = updateConfiguration(authToken, requestInstance, configurationBean, result);

                            }
                        }
                    }
                }
            }

        } catch (ApplicationException ae) {

            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in BundleAndConfigurationsManageService. Error: ", ae);

        } catch (Exception e) {

            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONFIGURATIONSFRAMEWORK, eventEnum,
                    ActivityStatusEnum.FAILED, "Bundles/Configuration create or update failed");
            LOG.error("Exception occured in BundleAndConfigurationsManageService. Error: ", e);

        }

        return result;
    }

    public Result createBundle(String authToken, DataControllerRequest requestInstance, BundleBean bundleBean,
            Result result) throws ApplicationException {

        if (StringUtils.isBlank(bundleBean.getBundleName())) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20473);
        }
        if (!BUNDLENAME_PATTERN.matcher(bundleBean.getBundleName()).matches()) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20474);
        }

        if (StringUtils.isBlank(bundleBean.getBundleAppId())) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20475);
        }
        if (!BUNDLEAPPID_PATTERN.matcher(bundleBean.getBundleAppId()).matches()) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20476);
        }

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("bundle_id", bundleBean.getBundleId());
        postParametersMap.put("bundle_name", bundleBean.getBundleName());
        postParametersMap.put("app_id", bundleBean.getBundleAppId());
        postParametersMap.put("createdby", bundleBean.getUserId());

        String createBundleResponse = Executor.invokeService(ServiceURLEnum.CONFIGURATIONSBUNDLE_CREATE,
                postParametersMap, null, requestInstance);

        JSONObject createBundleResponseJSON = CommonUtilities.getStringAsJSONObject(createBundleResponse);
        if (createBundleResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONFIGURATIONSFRAMEWORK, EventEnum.CREATE,
                    ActivityStatusEnum.SUCCESSFUL, "Configurations bundle create successful");
        } else {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONFIGURATIONSFRAMEWORK, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "Configurations bundle create failed");
            throw new ApplicationException(ErrorCodeEnum.ERR_20481);
        }

        return result;
    }

    public Result updateBundle(String authToken, DataControllerRequest requestInstance, BundleBean bundleBean,
            Result result) throws ApplicationException {

        if (StringUtils.isBlank(bundleBean.getBundleId())) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20471);
        }

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("bundle_id", bundleBean.getBundleId());
        postParametersMap.put("modifiedby", bundleBean.getUserId());

        if (bundleBean.getBundleName() != null) {
            if (StringUtils.isBlank(bundleBean.getBundleName())
                    || !BUNDLENAME_PATTERN.matcher(bundleBean.getBundleName()).matches()) {
                throw new ApplicationException(ErrorCodeEnum.ERR_20474);
            }
            postParametersMap.put("bundle_name", bundleBean.getBundleName());
        }

        if (bundleBean.getBundleAppId() != null) {
            if (StringUtils.isBlank(bundleBean.getBundleAppId())
                    || !BUNDLEAPPID_PATTERN.matcher(bundleBean.getBundleAppId()).matches()) {
                throw new ApplicationException(ErrorCodeEnum.ERR_20476);
            }
            postParametersMap.put("app_id", bundleBean.getBundleAppId());
        }

        String updateBundleResponse = Executor.invokeService(ServiceURLEnum.CONFIGURATIONSBUNDLE_UPDATE,
                postParametersMap, null, requestInstance);

        JSONObject updateBundleResponseJSON = CommonUtilities.getStringAsJSONObject(updateBundleResponse);
        if (updateBundleResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONFIGURATIONSFRAMEWORK, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL, "Configurations bundle update successful");
        } else {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONFIGURATIONSFRAMEWORK, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Configurations bundle update failed");
            throw new ApplicationException(ErrorCodeEnum.ERR_20483);
        }

        return result;
    }

    public Result createConfiguration(String authToken, DataControllerRequest requestInstance,
            ConfigurationBean configurationBean, Result result) throws ApplicationException {

        if (StringUtils.isBlank(configurationBean.getConfigurationKey())) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20489);
        }

        if (StringUtils.isBlank(configurationBean.getConfigurationValue())) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20490);
        }

        if (StringUtils.isBlank(configurationBean.getConfigurationDescription())) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20491);
        }
        if (configurationBean.getConfigurationDescription().length() > 1000) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20492);
        }

        if (StringUtils.isBlank(configurationBean.getConfigurationType())) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20494);
        }
        if (!(configurationBean.getConfigurationType().equals("IMAGE/ICON")
                || configurationBean.getConfigurationType().equals("SKIN")
                || configurationBean.getConfigurationType().equals("PREFERENCE"))) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20495);
        }

        if (StringUtils.isBlank(configurationBean.getConfigurationTarget())) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20496);
        }
        if (!(configurationBean.getConfigurationTarget().equals("CLIENT")
                || configurationBean.getConfigurationTarget().equals("SERVER"))) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20497);
        }

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("configuration_id", configurationBean.getConfigurationId());
        postParametersMap.put("bundle_id", configurationBean.getBundleId());
        postParametersMap.put("config_type", configurationBean.getConfigurationType());
        postParametersMap.put("description", configurationBean.getConfigurationDescription());
        postParametersMap.put("config_key", configurationBean.getConfigurationKey());
        postParametersMap.put("config_value", configurationBean.getConfigurationValue());
        postParametersMap.put("target", configurationBean.getConfigurationTarget());
        postParametersMap.put("createdby", configurationBean.getUserId());

        String createConfigurationResponse = Executor.invokeService(ServiceURLEnum.CONFIGURATION_CREATE,
                postParametersMap, null, requestInstance);

        JSONObject createConfigurationResponseJSON = CommonUtilities.getStringAsJSONObject(createConfigurationResponse);
        if (createConfigurationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONFIGURATIONSFRAMEWORK, EventEnum.CREATE,
                    ActivityStatusEnum.SUCCESSFUL, "Configuration create successful");
        } else {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONFIGURATIONSFRAMEWORK, EventEnum.CREATE,
                    ActivityStatusEnum.FAILED, "Configuration create failed");
            throw new ApplicationException(ErrorCodeEnum.ERR_20485);
        }

        return result;
    }

    public Result updateConfiguration(String authToken, DataControllerRequest requestInstance,
            ConfigurationBean configurationBean, Result result) throws ApplicationException {

        if (StringUtils.isBlank(configurationBean.getBundleId())) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20471);
        }
        if (StringUtils.isBlank(configurationBean.getConfigurationId())) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20472);
        }

        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("bundle_id", configurationBean.getBundleId());
        postParametersMap.put("configuration_id", configurationBean.getConfigurationId());

        if (configurationBean.getConfigurationKey() != null) {
            postParametersMap.put("config_key", configurationBean.getConfigurationKey());
        }

        if (configurationBean.getConfigurationValue() != null) {
            postParametersMap.put("config_value", configurationBean.getConfigurationValue());
        }

        if (configurationBean.getConfigurationDescription() != null) {

            if (StringUtils.isBlank(configurationBean.getConfigurationDescription())) {
                throw new ApplicationException(ErrorCodeEnum.ERR_20492);
            }
            if (configurationBean.getConfigurationDescription().length() > 1000) {
                throw new ApplicationException(ErrorCodeEnum.ERR_20493);
            }
            postParametersMap.put("description", configurationBean.getConfigurationDescription());
        }

        if (configurationBean.getConfigurationType() != null) {

            if (!(configurationBean.getConfigurationType().equals("IMAGE/ICON")
                    || configurationBean.getConfigurationType().equals("SKIN")
                    || configurationBean.getConfigurationType().equals("PREFERENCE"))) {
                throw new ApplicationException(ErrorCodeEnum.ERR_20495);
            }
            postParametersMap.put("config_type", configurationBean.getConfigurationType());
        }

        if (configurationBean.getConfigurationTarget() != null) {

            if (!(configurationBean.getConfigurationTarget().equals("CLIENT")
                    || configurationBean.getConfigurationTarget().equals("SERVER"))) {
                throw new ApplicationException(ErrorCodeEnum.ERR_20497);
            }
            postParametersMap.put("target", configurationBean.getConfigurationTarget());
        }

        postParametersMap.put("modifiedby", configurationBean.getUserId());

        String updateConfigurationResponse = Executor.invokeService(ServiceURLEnum.CONFIGURATION_UPDATE,
                postParametersMap, null, requestInstance);

        JSONObject updateConfigurationResponseJSON = CommonUtilities.getStringAsJSONObject(updateConfigurationResponse);
        if (updateConfigurationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONFIGURATIONSFRAMEWORK, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL, "Configuration update successful");
        } else {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CONFIGURATIONSFRAMEWORK, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Configuration update failed");
            throw new ApplicationException(ErrorCodeEnum.ERR_20486);
        }

        return result;
    }
}