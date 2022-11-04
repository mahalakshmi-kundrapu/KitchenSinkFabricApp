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
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.kony.adminconsole.utilities.StatusEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * @author Sowmya Mortha
 *
 */
public class CustomerAlertsGetService implements JavaService2 {

    private static final Logger logger = Logger.getLogger(CustomerAlertsGetService.class);
    // private static final String ACCOUNT = "Account";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result result = new Result();
        try {
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String customerId = null;

            if (requestInstance.getParameter("userName") != null) {
                CustomerHandler customerHandler = new CustomerHandler();
                customerId = customerHandler.getCustomerId(requestInstance.getParameter("userName"), requestInstance);
            } else {
                ErrorCodeEnum.ERR_20612.setErrorCode(result);

            }

            if (StringUtils.isBlank(customerId)) {
                ErrorCodeEnum.ERR_20613.setErrorCode(result);
            }

            String alertTypeId = requestInstance.getParameter("alertTypeId");
            String alertTypeName = requestInstance.getParameter("alertTypeName");

            JSONArray alertTypeDtls = getAlertTypeId(alertTypeId, alertTypeName, requestInstance, authToken);
            if (alertTypeDtls == null || alertTypeDtls.length() == 0) {
                ErrorCodeEnum.ERR_20642.setErrorCode(result);
                result.addParam(new Param("message", "alert type details cannot be empty", FabricConstants.STRING));
                result.addParam(new Param("status", "Failed", FabricConstants.STRING));
                return result;
            }

            Dataset alertType = new Dataset("alertTypes");
            Dataset alerts = null;
            Record record = null;
            JSONObject alertTypeJSON = null;
            JSONObject alertJSON = null;
            JSONObject custAlertJSON = null;
            Record alertRecord = null;
            String alertId;
            String value;
            boolean canBeSelected = false;
            boolean canSmsBeSelected = false;
            boolean canEmailBeSelected = false;
            boolean canPushBeSelected = false;
            boolean isAlertSelected = false;
            boolean isSmsActive = false;
            boolean isEmailActive = false;
            boolean isPushActive = false;
            boolean hasCustomerMapping = false;

            for (int index = 0; index < alertTypeDtls.length(); index++) {
                alertTypeJSON = alertTypeDtls.getJSONObject(index);
                // if (!alertTypeJSON.optString("Name").equalsIgnoreCase(ACCOUNT)) {
                alerts = new Dataset("alerts");
                hasCustomerMapping = false;
                JSONArray allAlerts = fetchAlerts(alertTypeJSON.getString("id"), requestInstance, authToken);
                JSONArray custAlerts = fetchCustomerPreferenceAlerts(customerId, requestInstance, authToken);
                for (int i = 0; i < allAlerts.length(); i++) {
                    alertJSON = allAlerts.getJSONObject(i);
                    alertRecord = new Record();
                    alertId = alertJSON.getString("id");
                    value = "";
                    canBeSelected = alertJSON.optBoolean("IsSubscriptionNeeded");
                    canSmsBeSelected = Boolean.parseBoolean(alertJSON.getString("IsSmsActive"));
                    canEmailBeSelected = Boolean.parseBoolean(alertJSON.getString("IsEmailActive"));
                    canPushBeSelected = Boolean.parseBoolean(alertJSON.getString("IsPushActive"));
                    isAlertSelected = false;
                    isSmsActive = false;
                    isEmailActive = false;
                    isPushActive = false;

                    if (!canBeSelected) {
                        isSmsActive = canSmsBeSelected;
                        isEmailActive = canEmailBeSelected;
                        isPushActive = canPushBeSelected;
                        isAlertSelected = true;
                    }
                    for (int custCount = 0; custCount < custAlerts.length(); custCount++) {
                        custAlertJSON = custAlerts.getJSONObject(custCount);
                        if (alertId.equalsIgnoreCase(custAlertJSON.getString("Alert_id"))) {
                            hasCustomerMapping = true;
                            if (canBeSelected) {
                                isSmsActive = custAlertJSON.optBoolean("IsSmsActive");
                                isEmailActive = custAlertJSON.optBoolean("IsEmailActive");
                                isPushActive = custAlertJSON.optBoolean("IsPushActive");
                                value = custAlertJSON.optString("Value");
                            }
                            isAlertSelected = true;
                            custAlerts.remove(custCount);
                            break;
                        }
                    }
                    alertRecord.addParam(new Param("alertId", alertId, FabricConstants.STRING));
                    alertRecord.addParam(new Param("name", alertJSON.getString("Name"), FabricConstants.STRING));
                    alertRecord.addParam(
                            new Param("canBeSelected", Boolean.toString(canBeSelected), FabricConstants.STRING));
                    alertRecord.addParam(
                            new Param("isSelected", Boolean.toString(isAlertSelected), FabricConstants.STRING));
                    alertRecord.addParam(
                            new Param("canSmsBeSelected", Boolean.toString(canSmsBeSelected), FabricConstants.STRING));
                    alertRecord.addParam(new Param("canEmailBeSelected", Boolean.toString(canEmailBeSelected),
                            FabricConstants.STRING));
                    alertRecord.addParam(new Param("canPushBeSelected", Boolean.toString(canPushBeSelected),
                            FabricConstants.STRING));

                    alertRecord
                            .addParam(new Param("isSmsActive", Boolean.toString(isSmsActive), FabricConstants.STRING));
                    alertRecord.addParam(
                            new Param("isEmailActive", Boolean.toString(isEmailActive), FabricConstants.STRING));
                    alertRecord.addParam(
                            new Param("isPushActive", Boolean.toString(isPushActive), FabricConstants.STRING));
                    alertRecord.addParam(new Param("value", value, FabricConstants.STRING));

                    alerts.addRecord(alertRecord);
                }
                record = new Record();
                record.addParam(new Param("alertType", alertTypeJSON.getString("Name"), FabricConstants.STRING));
                record.addParam(new Param("isSelected", Boolean.toString(hasCustomerMapping), FabricConstants.STRING));
                record.addParam(new Param("canBeSelected",
                        Boolean.toString(alertTypeJSON.optBoolean("IsSubscriptionNeeded")), FabricConstants.STRING));
                record.addDataset(alerts);

                alertType.addRecord(record);
                // }
            }
            result.addDataset(alertType);
            return result;
        } catch (Exception e) {
            logger.error("Error occured in fetching customer alert", e);
            ErrorCodeEnum.ERR_20910.setErrorCode(result);

            return result;
        }
    }

    private JSONArray getAlertTypeId(String alertTypeId, String alertTypeName, DataControllerRequest requestInstance,
            String authToken) throws Exception {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        List<String> $filter = new ArrayList<String>();
        if (StringUtils.isNotBlank(alertTypeName)) {
            $filter.add(" Name eq '" + alertTypeName + "'");
        }
        if (StringUtils.isNotBlank(alertTypeId)) {
            $filter.add(" id eq '" + alertTypeId + "'");
        }
        if (!$filter.isEmpty()) {
            postParametersMap.put(ODataQueryConstants.FILTER, StringUtils.join($filter, " or "));
        }
        postParametersMap.put(ODataQueryConstants.SELECT, "id,Name,IsSubscriptionNeeded");
        JSONObject readResponse = CommonUtilities.getStringAsJSONObject(
                Executor.invokeService(ServiceURLEnum.ALERTTYPE_READ, postParametersMap, null, requestInstance));
        if (readResponse.optInt(FabricConstants.OPSTATUS) != 0) {
            throw new Exception(readResponse.toString());
        }
        return readResponse.getJSONArray("alerttype");
    }

    private JSONArray fetchAlerts(String alertTypeId, DataControllerRequest requestInstance, String authToken)
            throws Exception {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        String $filter;
        $filter = "Status_id eq '" + StatusEnum.SID_ACTIVE.name() + "'";
        if (StringUtils.isNotBlank(alertTypeId)) {
            $filter += " and AlertType_id eq '" + alertTypeId + "'";
        }
        postParametersMap.put(ODataQueryConstants.FILTER, $filter);
        JSONObject readResponse = CommonUtilities.getStringAsJSONObject(
                Executor.invokeService(ServiceURLEnum.ALERT_READ, postParametersMap, null, requestInstance));
        if (readResponse.optInt(FabricConstants.OPSTATUS) != 0) {
            throw new Exception(readResponse.toString());
        }
        return readResponse.getJSONArray("alert");
    }

    private JSONArray fetchCustomerPreferenceAlerts(String customerId, DataControllerRequest requestInstance,
            String authToken) throws Exception {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "Customer_id eq '" + customerId + "'");
        JSONObject readResponse = CommonUtilities.getStringAsJSONObject(Executor
                .invokeService(ServiceURLEnum.CUSTOMERALERTENTITLEMENT_READ, postParametersMap, null, requestInstance));
        if (readResponse.optInt(FabricConstants.OPSTATUS) != 0) {
            throw new Exception(readResponse.toString());
        }
        return readResponse.getJSONArray("customeralertentitlement");
    }

}