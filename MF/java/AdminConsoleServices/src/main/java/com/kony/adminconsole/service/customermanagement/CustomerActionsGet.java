package com.kony.adminconsole.service.customermanagement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.config.EnvironmentConfiguration;
import com.kony.adminconsole.handler.FeatureandActionHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * CustomerActionsGet service is used to fetch all the feature actions of a customer
 * 
 * @author Alahari Prudhvi Akhil
 * 
 */
public class CustomerActionsGet implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerActionsGet.class);
    private static final String DEFAULT_LOCALE = "en-US";
    private static final String INPUT_USERNAME = "username";
    private static final String INPUT_APPID = "appid";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();

        try {
            String featureIdsFilter, actionIdsFilter;
            String username = requestInstance.getParameter(INPUT_USERNAME);
            String appid = requestInstance.getParameter(INPUT_APPID);
            String acceptLanguage = requestInstance.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
            String appIdtoAppMapping = EnvironmentConfiguration.AC_APPID_TO_APP_MAPPING.getValue(requestInstance);

            if (StringUtils.isBlank(appIdtoAppMapping)) {
                LOG.error("Failed to fetch customer feature and actions");
                ErrorCodeEnum.ERR_21403.setErrorCode(processedResult);
                return processedResult;
            }
            JSONObject appIdtoAppMappingJson = CommonUtilities.getStringAsJSONObject(appIdtoAppMapping);
            String currentApp;
            if (appIdtoAppMappingJson.has(appid)) {
                currentApp = appIdtoAppMappingJson.getString(appid);
            } else {
                LOG.error("Failed to fetch customer feature and actions");
                ErrorCodeEnum.ERR_21404.setErrorCode(processedResult);
                return processedResult;
            }

            if (StringUtils.isBlank(acceptLanguage)) {
                acceptLanguage = DEFAULT_LOCALE;
            }
            acceptLanguage = CommonUtilities.formatLanguageIdentifier(acceptLanguage);

            JSONObject customerDetails = getCustomerDetails(username, requestInstance, processedResult);
            if (processedResult.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                return processedResult;
            }
            featureIdsFilter = "Locale_id eq '" + acceptLanguage + "' and (";
            actionIdsFilter = "Locale_id eq '" + acceptLanguage + "' and (";

            JSONArray customerActionandGroupActionLimits = getCustomerActionandGroupActionLimits(
                    customerDetails.getString("id"), requestInstance, processedResult);

            Map<String, Feature> finalCustomerFeatures = new HashMap<String, Feature>();

            for (Object customerActionandGroupActionLimitObj : customerActionandGroupActionLimits) {
                JSONObject customerActionandGroupActionLimit = (JSONObject) customerActionandGroupActionLimitObj;
                Map<String, JSONObject> featureCustomerActions;
                Feature feature = new Feature();

                if (finalCustomerFeatures.containsKey(customerActionandGroupActionLimit.getString("Feature_id"))) {
                    feature = finalCustomerFeatures.get(customerActionandGroupActionLimit.getString("Feature_id"));
                    featureCustomerActions = feature.getActions();

                } else {
                    featureCustomerActions = new HashMap<String, JSONObject>();
                    feature.setProperty("Feature_id", customerActionandGroupActionLimit.optString("Feature_id"));
                    feature.setProperty("Feature_Name", customerActionandGroupActionLimit.optString("Feature_Name"));
                    feature.setProperty("Feature_Code", customerActionandGroupActionLimit.optString("Feature_Code"));
                    feature.setProperty("Feature_Description",
                            customerActionandGroupActionLimit.optString("Feature_Description"));
                    feature.setProperty("Feature_Status_id",
                            customerActionandGroupActionLimit.optString("Feature_Status_id"));

                    // Add feature id to filter
                    if (customerActionandGroupActionLimit.has("Feature_id")) {
                        if (featureIdsFilter.contains("Feature_id")) {
                            featureIdsFilter += " or ";
                        }
                        featureIdsFilter += "Feature_id eq '"
                                + customerActionandGroupActionLimit.getString("Feature_id") + "'";
                    }
                }

                if (featureCustomerActions.containsKey(customerActionandGroupActionLimit.getString("Action_id"))) {
                    // Update existing action
                    JSONObject action = featureCustomerActions
                            .get(customerActionandGroupActionLimit.getString("Action_id"));
                    JSONObject customerLevelLimits = action.getJSONObject("CustomerLevelLimits");
                    JSONObject groupLevelLimits = action.getJSONObject("GroupLevelLimits");
                    JSONObject actionLevelLimts = action.getJSONObject("limits");
                    JSONObject appLimits = new JSONObject();

                    // Customer level limits
                    if (customerActionandGroupActionLimit.has("Customer_LimitType_id")) {
                        customerLevelLimits.put(customerActionandGroupActionLimit.getString("Customer_LimitType_id"),
                                customerActionandGroupActionLimit.optString("Customer_Limit_Value"));
                    }

                    // Group level limits
                    if (customerActionandGroupActionLimit.has("Group_LimitType_id")) {
                        groupLevelLimits.put(customerActionandGroupActionLimit.getString("Group_LimitType_id"),
                                customerActionandGroupActionLimit.optString("Group_Limit_Value"));
                    }

                    // Action level limits
                    if (customerActionandGroupActionLimit.has("App_id")) {
                        if (actionLevelLimts.has(customerActionandGroupActionLimit.getString("App_id"))) {
                            appLimits = actionLevelLimts
                                    .getJSONObject(customerActionandGroupActionLimit.getString("App_id"));
                            if (customerActionandGroupActionLimit.has("LimitType_id")) {
                                appLimits.put(customerActionandGroupActionLimit.getString("LimitType_id"),
                                        customerActionandGroupActionLimit.optString("Limit_Value"));
                            }
                        } else {
                            appLimits = new JSONObject();
                            if (customerActionandGroupActionLimit.has("LimitType_id")) {
                                appLimits.put(customerActionandGroupActionLimit.getString("LimitType_id"),
                                        customerActionandGroupActionLimit.optString("Limit_Value"));
                            }
                            actionLevelLimts.put(customerActionandGroupActionLimit.getString("App_id"), appLimits);
                        }
                    }
                } else {
                    JSONObject action = new JSONObject();
                    JSONObject customerLevelLimits = new JSONObject();
                    JSONObject groupLevelLimits = new JSONObject();
                    JSONObject actionLevelLimts = new JSONObject();
                    JSONObject appLimits = new JSONObject();
                    action.put("id", customerActionandGroupActionLimit.getString("Action_id"));
                    action.put("name", customerActionandGroupActionLimit.getString("Action_Name"));
                    action.put("code", customerActionandGroupActionLimit.optString("Action_Code"));
                    action.put("description", customerActionandGroupActionLimit.optString("Action_Description"));

                    // Add action id to filter
                    if (customerActionandGroupActionLimit.has("Action_id")) {
                        if (actionIdsFilter.contains("Action_id")) {
                            actionIdsFilter += " or ";
                        }
                        actionIdsFilter += "Action_id eq '" + customerActionandGroupActionLimit.getString("Action_id")
                                + "'";
                    }

                    // Customer level limits
                    if (customerActionandGroupActionLimit.has("Customer_LimitType_id")) {
                        customerLevelLimits.put(customerActionandGroupActionLimit.getString("Customer_LimitType_id"),
                                customerActionandGroupActionLimit.optString("Customer_Limit_Value"));
                    }
                    action.put("CustomerLevelLimits", customerLevelLimits);

                    // Group level limits
                    if (customerActionandGroupActionLimit.has("Group_LimitType_id")) {
                        groupLevelLimits.put(customerActionandGroupActionLimit.getString("Group_LimitType_id"),
                                customerActionandGroupActionLimit.optString("Group_Limit_Value"));
                    }
                    action.put("GroupLevelLimits", groupLevelLimits);

                    // Action level limits
                    if (customerActionandGroupActionLimit.has("App_id")) {
                        if (customerActionandGroupActionLimit.has("LimitType_id")) {
                            appLimits.put(customerActionandGroupActionLimit.getString("LimitType_id"),
                                    customerActionandGroupActionLimit.optString("Limit_Value"));
                        }

                        actionLevelLimts.put(customerActionandGroupActionLimit.getString("App_id"), appLimits);
                    }
                    action.put("limits", actionLevelLimts);
                    featureCustomerActions.put(customerActionandGroupActionLimit.getString("Action_id"), action);
                }

                feature.setActions(featureCustomerActions);
                finalCustomerFeatures.put(customerActionandGroupActionLimit.getString("Feature_id"), feature);
            }

            // Fetch feature and action display content
            actionIdsFilter += ")";
            featureIdsFilter += ")";

            Record finalCustomerFeaturesRecordSet = new Record();

            // Process the features
            if (finalCustomerFeatures.size() > 0) {
                HashMap<String, JSONObject> featureDisplayContent = FeatureandActionHandler
                        .getFeatureDisplayContent(featureIdsFilter, requestInstance, processedResult);
                if (featureDisplayContent == null) {
                    // Error while reading feature display content
                    return processedResult;
                }
                HashMap<String, JSONObject> actionDisplayContent = FeatureandActionHandler
                        .getActionDisplayContent(actionIdsFilter, requestInstance, processedResult);
                if (actionDisplayContent == null) {
                    // Error while reading action display content
                    return processedResult;
                }

                finalCustomerFeatures.forEach((featureId, featureObj) -> {

                    Record finalCustomerActionsRecord = new Record();
                    // Iterate through each feature
                    Record featureRecord = new Record();
                    featureRecord
                            .addParam(new Param("id", featureObj.getProperty("Feature_id"), FabricConstants.STRING));
                    featureRecord.addParam(
                            new Param("name", featureObj.getProperty("Feature_Name"), FabricConstants.STRING));
                    featureRecord.addParam(
                            new Param("code", featureObj.getProperty("Feature_Code"), FabricConstants.STRING));
                    featureRecord.addParam(new Param("description", featureObj.getProperty("Feature_Description"),
                            FabricConstants.STRING));
                    // Add display content
                    if (featureDisplayContent.containsKey(featureObj.getProperty("Feature_id"))) {
                        featureRecord.addParam(new Param("display_name", featureDisplayContent
                                .get(featureObj.getProperty("Feature_id")).optString("DisplayName"),
                                FabricConstants.STRING));
                        featureRecord.addParam(new Param("display_description", featureDisplayContent
                                .get(featureObj.getProperty("Feature_id")).optString("DisplayDescription"),
                                FabricConstants.STRING));
                    }

                    Map<String, JSONObject> featureActions = featureObj.getActions();
                    featureActions.forEach((actionId, actionObj) -> {
                        // Iterate through each action
                        JSONObject customerLevelLimitsObj, groupLevelLimitsObj, actionLevelLimtsObj;
                        customerLevelLimitsObj = actionObj.getJSONObject("CustomerLevelLimits");
                        groupLevelLimitsObj = actionObj.getJSONObject("GroupLevelLimits");
                        actionLevelLimtsObj = actionObj.getJSONObject("limits");

                        // Override action level limits with group level limits
                        Iterator<String> groupLimitsIterator = groupLevelLimitsObj.keys();
                        while (groupLimitsIterator.hasNext()) {
                            String limitKey = groupLimitsIterator.next();

                            // Iterate through all the apps and override the limit for each app
                            Iterator<String> appsIterator = actionLevelLimtsObj.keys();
                            while (appsIterator.hasNext()) {
                                String appKey = appsIterator.next();
                                if (actionLevelLimtsObj.getJSONObject(appKey).has(limitKey)) {
                                    actionLevelLimtsObj.getJSONObject(appKey).put(limitKey,
                                            groupLevelLimitsObj.getString(limitKey));
                                }
                            }
                        }

                        // Override action level limits with customer level limits
                        Iterator<String> customerLimitsIterator = customerLevelLimitsObj.keys();
                        while (customerLimitsIterator.hasNext()) {
                            String limitKey = customerLimitsIterator.next();

                            // Iterate through all the apps and override the limit for each app
                            Iterator<String> appsIterator = actionLevelLimtsObj.keys();
                            while (appsIterator.hasNext()) {
                                String appKey = appsIterator.next();
                                if (actionLevelLimtsObj.getJSONObject(appKey).has(limitKey)) {
                                    actionLevelLimtsObj.getJSONObject(appKey).put(limitKey,
                                            customerLevelLimitsObj.getString(limitKey));
                                }
                            }
                        }
                        // Remove app level hierarchy in limits object and override the
                        // limits object with the requested application limits
                        if (actionLevelLimtsObj.has(currentApp)
                                && actionLevelLimtsObj.getJSONObject(currentApp).keySet().size() > 0) {
                            actionObj.put("limits", actionLevelLimtsObj.getJSONObject(currentApp));
                        } else if (actionObj.has("limits")) {
                            // Remove the limits object if exists
                            actionObj.remove("limits");
                        }

                        // Add display content
                        if (actionDisplayContent.containsKey(actionObj.getString("id"))) {
                            actionObj.put("display_name", actionDisplayContent.get(actionObj.getString("Action_id"))
                                    .optString("DisplayName"));
                            actionObj.put("display_description", actionDisplayContent
                                    .get(actionObj.getString("Action_id")).optString("DisplayDescription"));
                        }

                        // Remove the customer and group level limits from the result
                        actionObj.remove("GroupLevelLimits");
                        actionObj.remove("CustomerLevelLimits");

                        Record actionRecord = CommonUtilities.constructRecordFromJSONObject(actionObj);
                        actionRecord.setId(actionObj.getString("code"));
                        finalCustomerActionsRecord.addRecord(actionRecord);
                    });
                    finalCustomerActionsRecord.setId("actions");
                    featureRecord.addRecord(finalCustomerActionsRecord);
                    featureRecord.setId(featureObj.getProperty("Feature_Code"));
                    finalCustomerFeaturesRecordSet.addRecord(featureRecord);
                });
            }

            finalCustomerFeaturesRecordSet.setId("features");
            processedResult.addRecord(finalCustomerFeaturesRecordSet);
        } catch (Exception e) {
            LOG.error("Unexpected error", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(processedResult);
        }
        return processedResult;
    }

    // Fetch Customer details
    private static JSONObject getCustomerDetails(String customerUsername, DataControllerRequest requestInstance,
            Result processedResult) {

        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put(ODataQueryConstants.FILTER, "UserName eq '" + customerUsername + "'");
        inputMap.put(ODataQueryConstants.SELECT, "id,CustomerType_id,FirstName,MiddleName,LastName,Status_id,UserName");

        JSONObject readCustomerResponse = CommonUtilities.getStringAsJSONObject(
                Executor.invokeService(ServiceURLEnum.CUSTOMER_READ, inputMap, null, requestInstance));
        if (readCustomerResponse == null || !readCustomerResponse.has("customer")
                || readCustomerResponse.getJSONArray("customer").length() == 0) {
            ErrorCodeEnum.ERR_20617.setErrorCode(processedResult);
            return null;
        }

        return (JSONObject) readCustomerResponse.getJSONArray("customer").get(0);
    }

    // Fetch Customer details
    private static JSONArray getCustomerActionandGroupActionLimits(String customerId,
            DataControllerRequest requestInstance, Result processedResult) {

        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("_customerID", customerId);

        JSONObject readCustomerResponse = CommonUtilities.getStringAsJSONObject(Executor.invokeService(
                ServiceURLEnum.CUSTOMER_ACTION_GROUP_ACTION_LIMIT_PROC, inputMap, null, requestInstance));
        if (readCustomerResponse == null || !readCustomerResponse.has("records")) {
            ErrorCodeEnum.ERR_20618.setErrorCode(processedResult);
            return null;
        }

        return readCustomerResponse.getJSONArray("records");
    }

    class Feature {

        private Map<String, JSONObject> actions;
        private JSONObject properties;

        Feature() {
            actions = new HashMap<String, JSONObject>();
            properties = new JSONObject();
        }

        public Map<String, JSONObject> getActions() {
            return actions;
        }

        public void setActions(Map<String, JSONObject> actions) {
            this.actions = actions;
        }

        public void setAction(String key, JSONObject value) {
            this.actions.put(key, value);
        }

        public JSONObject getProperties() {
            return properties;
        }

        public void setProperties(JSONObject properties) {
            this.properties = properties;
        }

        public void setProperty(String key, String value) {
            this.properties.put(key, value);
        }

        public String getProperty(String key) {
            return this.properties.optString(key);
        }
    }
}
