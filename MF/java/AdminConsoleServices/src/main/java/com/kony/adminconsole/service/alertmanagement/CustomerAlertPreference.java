/**
 * 
 */
package com.kony.adminconsole.service.alertmanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.CustomerHandler;
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
 * @author Sowmya Mortha
 *
 */
public class CustomerAlertPreference implements JavaService2 {

    private static final Logger logger = Logger.getLogger(CustomerAlertPreference.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        String customerId = null;
        Result result = new Result();
        try {
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String systemUser = "";
            String alerts = requestInstance.getParameter("alerts");

            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            if (loggedInUserDetails != null) {
                systemUser = loggedInUserDetails.getUserId();
            }

            if (requestInstance.getParameter("userName") != null) {
                CustomerHandler customerHandler = new CustomerHandler();
                customerId = customerHandler.getCustomerId(requestInstance.getParameter("userName"), requestInstance);
            } else {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Manage customer alert preferences failed." + " Customer id:" + customerId);
                ErrorCodeEnum.ERR_20612.setErrorCode(result);

                return result;

            }

            if (StringUtils.isBlank(customerId)) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Manage customer alert preferences failed." + " Customer id:" + customerId);
                ErrorCodeEnum.ERR_20613.setErrorCode(result);

                return result;
            }

            JSONArray alertArray = new JSONArray();
            if (StringUtils.isNotBlank(alerts)) {
                alertArray = CommonUtilities.getStringAsJSONArray(alerts);
            }
            if (alertArray.length() == 0) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED,
                        "Manage customer alert preferences failed." + " Customer id:" + customerId);
                ErrorCodeEnum.ERR_20641.setErrorCode(result);
                return result;
            }

            List<String> savedPrefs = fetchCustomerPreferenceAlerts(customerId, requestInstance, authToken);

            boolean isSubcribed = Boolean.parseBoolean(requestInstance.getParameter("isSelected"));
            updateCustomerPreferences(isSubcribed, alertArray, customerId, savedPrefs, systemUser, authToken,
                    requestInstance);

            result.addParam(new Param("status", "success", FabricConstants.STRING));

            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL,
                    "Manage customer alert preferences successful." + " Customer id:" + customerId);
            return result;

        } catch (Exception e) {
            logger.error("Error occured in fetching customer alert", e);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.ALERTS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED,
                    "Manage customer alert preferences failed" + ". Customer id:" + customerId);
            ErrorCodeEnum.ERR_20641.setErrorCode(result);
            return result;
        }
    }

    private List<String> fetchCustomerPreferenceAlerts(String customerId, DataControllerRequest requestInstance,
            String authToken) throws Exception {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "Customer_id eq '" + customerId + "'");
        postParametersMap.put(ODataQueryConstants.SELECT, "Alert_id");
        JSONObject readResponse = CommonUtilities.getStringAsJSONObject(Executor
                .invokeService(ServiceURLEnum.CUSTOMERALERTENTITLEMENT_READ, postParametersMap, null, requestInstance));
        if (readResponse.optInt(FabricConstants.OPSTATUS) != 0) {
            throw new Exception(readResponse.toString());
        }
        JSONArray savedPrefsArray = readResponse.getJSONArray("customeralertentitlement");
        List<String> savedPreferences = new ArrayList<String>();
        for (int i = 0; i < savedPrefsArray.length(); i++) {
            savedPreferences.add(savedPrefsArray.getJSONObject(i).getString("Alert_id"));
        }
        return savedPreferences;
    }

    private void updateCustomerPreferences(boolean isSubcribed, JSONArray alertArray, String customerId,
            List<String> savedPrefs, String systemUser, String authToken, DataControllerRequest requestInstance)
            throws Exception {
        JSONObject alertJSON = null;
        boolean isSelected = false;
        boolean canBeSelected = false;
        boolean isSmsActive = false;
        boolean isEmailActive = false;
        boolean isPushActive = false;
        String value = null;
        String alertId = null;
        for (int i = 0; i < alertArray.length(); i++) {
            alertJSON = alertArray.getJSONObject(i);
            canBeSelected = alertJSON.optBoolean("canBeSelected");
            isSelected = alertJSON.optBoolean("isSelected");
            isSmsActive = alertJSON.optBoolean("isSmsActive");
            isEmailActive = alertJSON.optBoolean("isEmailActive");
            isPushActive = alertJSON.optBoolean("isPushActive");
            value = alertJSON.optString("value");
            alertId = alertJSON.getString("alertId");
            if (!isSubcribed && savedPrefs.contains(alertId)) {
                deleteEntitlement(customerId, alertId, systemUser, authToken, requestInstance);
            } else if (isSubcribed && (isSelected || !canBeSelected) && !savedPrefs.contains(alertId)) {
                addEntitlement(customerId, alertId, isSmsActive, isEmailActive, isPushActive, systemUser, authToken,
                        requestInstance);
            } else if (isSubcribed && (isSelected || !canBeSelected) && savedPrefs.contains(alertId)) {
                updateEntitlement(customerId, alertId, isSmsActive, isEmailActive, isPushActive, value, systemUser,
                        authToken, requestInstance);
            } else if (isSubcribed && !isSelected && savedPrefs.contains(alertId)) {
                deleteEntitlement(customerId, alertId, systemUser, authToken, requestInstance);
            }
        }
    }

    private void addEntitlement(String customerId, String alertId, boolean isSmsActive, boolean isEmailActive,
            boolean isPushActive, String systemUser, String authToken, DataControllerRequest requestInstance)
            throws Exception {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("Customer_id", customerId);
        postParametersMap.put("Alert_id", alertId);
        postParametersMap.put("IsSmsActive", Boolean.toString(isSmsActive));
        postParametersMap.put("IsEmailActive", Boolean.toString(isEmailActive));
        postParametersMap.put("IsPushActive", Boolean.toString(isPushActive));
        JSONObject readResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
                ServiceURLEnum.CUSTOMERALERTENTITLEMENT_CREATE, postParametersMap, null, requestInstance));
        if (readResponse.optInt(FabricConstants.OPSTATUS) != 0) {
            throw new Exception(readResponse.toString());
        }
    }

    private void updateEntitlement(String customerId, String alertId, boolean isSmsActive, boolean isEmailActive,
            boolean isPushActive, String value, String systemUser, String authToken,
            DataControllerRequest requestInstance) throws Exception {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("Customer_id", customerId);
        postParametersMap.put("Alert_id", alertId);
        postParametersMap.put("IsSmsActive", Boolean.toString(isSmsActive));
        postParametersMap.put("IsEmailActive", Boolean.toString(isEmailActive));
        postParametersMap.put("IsPushActive", Boolean.toString(isPushActive));
        postParametersMap.put("FilterValue", value);
        JSONObject readResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
                ServiceURLEnum.CUSTOMERALERTENTITLEMENT_UPDATE, postParametersMap, null, requestInstance));
        if (readResponse.optInt(FabricConstants.OPSTATUS) != 0) {
            throw new Exception(readResponse.toString());
        }
    }

    private void deleteEntitlement(String customerId, String alertId, String systemUser, String authToken,
            DataControllerRequest requestInstance) throws Exception {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("Customer_id", customerId);
        postParametersMap.put("Alert_id", alertId);
        JSONObject readResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
                ServiceURLEnum.CUSTOMERALERTENTITLEMENT_DELETE, postParametersMap, null, requestInstance));
        if (readResponse.optInt(FabricConstants.OPSTATUS) != 0) {
            throw new Exception(readResponse.toString());
        }
    }

}