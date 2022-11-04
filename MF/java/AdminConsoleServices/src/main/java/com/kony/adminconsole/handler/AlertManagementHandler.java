package com.kony.adminconsole.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.kony.adminconsole.utilities.StatusEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;

/**
 * Handler to perform common operations on Alerts
 * 
 * @author Aditya Mankal
 */
public class AlertManagementHandler {

    public static final String DEFAULT_LOCALE = "en-US";
    public static final String IS_SUBSCRIBED_PARAM = "isSubscribed";
    public static final String IS_INITIAL_LOAD_PARAM = "isInitialLoad";
    public static final String CATEGORY_SUBSCRIPTION_PARAM = "categorySubscription";
    private static Map<String, String> ACCOUNT_TYPE_MAP = null;
    private static Map<String, String> ACCOUNT_TYPE_IDS_MAP = null;

    private static final Logger LOG = Logger.getLogger(AlertManagementHandler.class);

    /**
     * Method to return the Channels selected by a customer for an Alert Category
     * 
     * @param customerId
     * @param alertCategoryId
     * @param requestInstance
     * @return Channels selected by a customer for an Alert Category
     * @throws ApplicationException
     */
    public static List<String> getCustomerChannelPreferencesOfAlertCategory(String customerId, String accountId,
            String accountTypeId, String alertCategoryId, DataControllerRequest requestInstance)
            throws ApplicationException {
        if (StringUtils.isBlank(customerId) || StringUtils.isBlank(alertCategoryId) || requestInstance == null) {
            LOG.error("Invalid Parameters. Failed to fetch Alert Category Preferences of Customer:" + customerId);
            throw new ApplicationException(ErrorCodeEnum.ERR_20913);
        }
        List<String> selectedChannelsList = new ArrayList<>();
        Map<String, String> parameterMap = new HashMap<>();
        String filter = "AlertCategoryID eq '" + alertCategoryId + "' and Customer_id eq'" + customerId + "'";

        if (StringUtils.isNotBlank(accountTypeId)) {
            filter += " and AccountType eq '" + accountTypeId + "'";
        }

        if (StringUtils.isNotBlank(accountId)) {
            filter += " and AccountId eq '" + accountId + "'";
        }

        parameterMap.put(ODataQueryConstants.FILTER, filter);
        String readCustomerAlertCategoryChannelResponse = Executor
                .invokeService(ServiceURLEnum.CUSTOMERALERTCATEGORYCHANNEL_READ, parameterMap, null, requestInstance);
        JSONObject readCustomerAlertCategoryChannelResponseJSON = CommonUtilities
                .getStringAsJSONObject(readCustomerAlertCategoryChannelResponse);
        if (readCustomerAlertCategoryChannelResponseJSON == null
                || !readCustomerAlertCategoryChannelResponseJSON.has(FabricConstants.OPSTATUS)
                || readCustomerAlertCategoryChannelResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || !readCustomerAlertCategoryChannelResponseJSON.has("customeralertcategorychannel")) {
            // Failed read operation of Alert Category Channel Preferences
            LOG.error("Failed to fetch Alert Category Preferences of Customer:" + customerId);
            throw new ApplicationException(ErrorCodeEnum.ERR_20913);
        }

        LOG.error("Succesfully Fetched Alert Category Preferences of Customer:" + customerId);
        JSONArray customerAlertCategoryPreference = readCustomerAlertCategoryChannelResponseJSON
                .optJSONArray("customeralertcategorychannel");
        JSONObject currJSONObject;
        for (Object currObject : customerAlertCategoryPreference) {
            if (currObject instanceof JSONObject) {
                currJSONObject = (JSONObject) currObject;
                selectedChannelsList.add(currJSONObject.optString("ChannelId"));
            }
        }

        return selectedChannelsList;
    }

    /**
     * Method to fetch the List of supported channels of an Alert Category
     * 
     * @param alertCategoryId
     * @param requestInstance
     * @return List of Supported Channels
     * @throws ApplicationException
     */
    public static List<String> getSupportedChannelsOfAlertCategory(String alertCategoryId,
            DataControllerRequest requestInstance) throws ApplicationException {

        if (StringUtils.isBlank(alertCategoryId) || requestInstance == null) {
            LOG.error("Invalid Parameters. Failed to fetch List of Supported Channels for Alert Category:"
                    + alertCategoryId);
            throw new ApplicationException(ErrorCodeEnum.ERR_20912);
        }

        List<String> supportedChannelsList = new ArrayList<>();
        LOG.debug("Fetching List of Supported Channels for Alert Category:" + alertCategoryId);

        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put(ODataQueryConstants.FILTER, "AlertCategoryId eq '" + alertCategoryId + "'");
        String readAlertCategoryChannelResponse = Executor.invokeService(ServiceURLEnum.ALERTCATEGORYCHANNEL_READ,
                parameterMap, null, requestInstance);
        JSONObject readAlertCategoryChannelResponseJSON = CommonUtilities
                .getStringAsJSONObject(readAlertCategoryChannelResponse);
        if (readAlertCategoryChannelResponseJSON == null
                || !readAlertCategoryChannelResponseJSON.has(FabricConstants.OPSTATUS)
                || readAlertCategoryChannelResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || !readAlertCategoryChannelResponseJSON.has("alertcategorychannel")) {
            // Failed read operation of Alert Category Channel Preferences
            LOG.error("Failed to Fetch List of Supported Channels for Alert Category:" + alertCategoryId);
            throw new ApplicationException(ErrorCodeEnum.ERR_20912);
        } else {
            // Successful read operation of Alert Category Channel Preferences
            JSONObject currJSONObject;
            LOG.error("Succesfully Fetched List of Supported Channels for Alert Category:" + alertCategoryId);
            JSONArray alertCategoryChannelsArray = readAlertCategoryChannelResponseJSON
                    .optJSONArray("alertcategorychannel");
            for (Object currObject : alertCategoryChannelsArray) {
                if (currObject instanceof JSONObject) {
                    currJSONObject = (JSONObject) currObject;
                    supportedChannelsList.add(currJSONObject.optString("ChannelID"));
                }
            }
        }
        LOG.debug("Sucesfully Fetched List of Supported Channels for Alert Category:" + alertCategoryId);
        return supportedChannelsList;

    }

    /**
     * Method to get the specific Alert Types based on Alert Type ID of an Alert Category
     * 
     * @param alertTypeIdsList
     * @param alertCategoryId
     * @param acceptLanguage
     * @param requestInstance
     * @return Array of Alert Types
     * @throws ApplicationException
     */
    public static JSONArray getAlertTypes(List<String> alertTypeIdsList, String alertCategoryId, String acceptLanguage,
            boolean fetchGlobalAlerts, boolean fetchActiveAlertsOnly, DataControllerRequest requestInstance)
            throws ApplicationException {

        // Construct Filter Query
        StringBuffer filterQueryBuffer = new StringBuffer();
        if (StringUtils.isBlank(acceptLanguage)) {
            acceptLanguage = DEFAULT_LOCALE;
        }
        if (StringUtils.equals(acceptLanguage, DEFAULT_LOCALE)) {
            filterQueryBuffer.append("(alerttypetext_LanguageCode eq '" + acceptLanguage + "')");
        } else {
            filterQueryBuffer.append("(alerttypetext_LanguageCode eq '" + acceptLanguage
                    + "' or alerttypetext_LanguageCode eq '" + DEFAULT_LOCALE + "')");
        }

        // Append ID values of Alert Types to Filter Querty
        if (alertTypeIdsList != null && !alertTypeIdsList.isEmpty()) {
            if (filterQueryBuffer.length() > 0) {
                filterQueryBuffer.append(" and (");
            }

            for (int index = 0; index < alertTypeIdsList.size(); index++) {
                filterQueryBuffer.append("alerttype_id eq '" + alertTypeIdsList.get(index) + "'");
                if (index != alertTypeIdsList.size() - 1) {
                    filterQueryBuffer.append(" or ");
                }
            }
            filterQueryBuffer.append(")");
            filterQueryBuffer.trimToSize();
        }

        // Add Alert Category Id to Filter Query
        if (StringUtils.isNotBlank(alertCategoryId)) {
            if (filterQueryBuffer.length() > 0) {
                filterQueryBuffer.append(" and ");
            }
            filterQueryBuffer.append("(alerttype_AlertCategoryId eq '" + alertCategoryId + "')");
        }
        if (fetchGlobalAlerts == false) {
            if (filterQueryBuffer.length() > 0) {
                filterQueryBuffer.append(" and ");
            }
            filterQueryBuffer.append("(alerttype_IsGlobal eq '0')");
        }
        if (fetchActiveAlertsOnly == true) {
            if (filterQueryBuffer.length() > 0) {
                filterQueryBuffer.append(" and ");
            }
            filterQueryBuffer.append("(alerttype_Status_id eq '" + StatusEnum.SID_ACTIVE.name() + "')");
        }

        String filterQuery = filterQueryBuffer.toString();
        filterQuery = filterQuery.trim();
        LOG.debug("Consctructed Filter Query:" + filterQuery);

        // Construct Input map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put(ODataQueryConstants.FILTER, filterQuery);
        inputMap.put(ODataQueryConstants.ORDER_BY, "alerttype_DisplaySequence asc");

        // Read Alert Type Texts
        String readAlertTypeTextResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPE_VIEW_READ, inputMap, null,
                requestInstance);
        JSONObject readAlertTypeTextResponseJSON = CommonUtilities.getStringAsJSONObject(readAlertTypeTextResponse);
        if (readAlertTypeTextResponseJSON != null && readAlertTypeTextResponseJSON.has(FabricConstants.OPSTATUS)
                && readAlertTypeTextResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readAlertTypeTextResponseJSON.has("alerttype_view")) {
            LOG.debug("Successful Read Operation of alerttype_view");
            JSONArray alertTypeTextJSONArray = readAlertTypeTextResponseJSON.optJSONArray("alerttype_view");
            alertTypeTextJSONArray = CommonUtilities.filterRecordsByLocale(alertTypeTextJSONArray,
                    "alerttypetext_LanguageCode", "alerttype_id", DEFAULT_LOCALE);
            return alertTypeTextJSONArray;
        } else {
            LOG.error("Failed Read Operation of alerttype_view");
            throw new ApplicationException(ErrorCodeEnum.ERR_20918);
        }

    }

    /**
     * Method to get the Alert Types of an Alert Category
     * 
     * @param alertCategoryID
     * @param acceptLanguage
     * @param requestInstance
     * @return Array of Alert Types
     * @throws ApplicationException
     */
    public static JSONArray getAlertTypesOfAlertCategory(String alertCategoryID, String acceptLanguage,
            DataControllerRequest requestInstance) throws ApplicationException {
        return getAlertTypes(null, alertCategoryID, acceptLanguage, true, false, requestInstance);
    }

    /**
     * Method to get the details of an Alert Type with Alert Id
     * 
     * @param alertTypeId
     * @param acceptLanguage
     * @param requestInstance
     * @return JSON Object representing the Alert Type
     * @throws ApplicationException
     */
    public static JSONObject getAlertTypeDefinition(String alertTypeId, String acceptLanguage,
            DataControllerRequest requestInstance) throws ApplicationException {

        if (StringUtils.isBlank(acceptLanguage)) {
            acceptLanguage = DEFAULT_LOCALE;
        }

        // Construct Filter Query
        StringBuffer filterQueryBuffer = new StringBuffer();
        if (StringUtils.equals(acceptLanguage, DEFAULT_LOCALE)) {
            filterQueryBuffer.append("(alerttypetext_LanguageCode eq '" + acceptLanguage + "')");
        } else {
            filterQueryBuffer.append("(alerttypetext_LanguageCode eq '" + acceptLanguage
                    + "' or alerttypetext_LanguageCode eq '" + DEFAULT_LOCALE + "')");
        }
        filterQueryBuffer.append(" and alerttype_id eq '" + alertTypeId + "'");
        String filterQuery = filterQueryBuffer.toString();
        filterQuery = filterQuery.trim();

        // Construct Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put(ODataQueryConstants.FILTER, filterQuery);

        // Read Alert Type Text
        String readAlertTypeTextResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPE_VIEW_READ, inputMap, null,
                requestInstance);
        JSONObject readAlertTypeTextResponseJSON = CommonUtilities.getStringAsJSONObject(readAlertTypeTextResponse);
        if (readAlertTypeTextResponseJSON != null && readAlertTypeTextResponseJSON.has(FabricConstants.OPSTATUS)
                && readAlertTypeTextResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readAlertTypeTextResponseJSON.has("alerttype_view")) {
            LOG.debug("Successful Read Operation of alerttype_view");
            JSONArray alertTypeTextJSONArray = readAlertTypeTextResponseJSON.optJSONArray("alerttype_view");
            alertTypeTextJSONArray = CommonUtilities.filterRecordsByLocale(alertTypeTextJSONArray,
                    "alerttypetext_LanguageCode", "alerttype_id", DEFAULT_LOCALE);
            return alertTypeTextJSONArray.optJSONObject(0);
        } else {
            LOG.error("Failed Read Operation of alerttype_view");
            throw new ApplicationException(ErrorCodeEnum.ERR_20918);
        }
    }

    /**
     * Method to get the list of Alert Types applicable to given Customer and Account(Optional)
     * 
     * @param customerID
     * @param accountID
     * @param requestInstance
     * @return Array of Alert Types
     * @throws ApplicationException
     */
    public static JSONArray getCustomerAlerts(String customerID, String accountID, String accountTypeId,
            DataControllerRequest requestInstance) throws ApplicationException {

        if (StringUtils.isBlank(customerID) || requestInstance == null) {
            LOG.error("Invalid Parameters. Failed to fetch Alert Preferences of Customer");
            throw new ApplicationException(ErrorCodeEnum.ERR_20919);
        }

        Map<String, String> inputMap = new HashMap<>();
        // Construct Input Map
        if (StringUtils.isNotBlank(accountTypeId)) {
            inputMap.put(ODataQueryConstants.FILTER,
                    "AccountType eq '" + accountTypeId + "' and Customer_id eq '" + customerID + "'");
        } else if (StringUtils.isNotBlank(accountID)) {
            inputMap.put(ODataQueryConstants.FILTER,
                    "AccountId eq '" + accountID + "' and Customer_id eq '" + customerID + "'");
        } else {
            inputMap.put(ODataQueryConstants.FILTER, "Customer_id eq '" + customerID + "'");
        }
        String readCustomerAlertEntitlementResponse = Executor
                .invokeService(ServiceURLEnum.DBXCUSTOMERALERTENTITLEMENT_READ, inputMap, null, requestInstance);
        JSONObject readCustomerAlertEntitlementResponseJSON = CommonUtilities
                .getStringAsJSONObject(readCustomerAlertEntitlementResponse);
        if (readCustomerAlertEntitlementResponseJSON != null
                && readCustomerAlertEntitlementResponseJSON.has(FabricConstants.OPSTATUS)
                && readCustomerAlertEntitlementResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readCustomerAlertEntitlementResponseJSON.has("dbxcustomeralertentitlement")) {
            JSONArray customerAlertEntitlementsArray = readCustomerAlertEntitlementResponseJSON
                    .optJSONArray("dbxcustomeralertentitlement");
            return customerAlertEntitlementsArray;
        } else {
            throw new ApplicationException(ErrorCodeEnum.ERR_20919);
        }
    }

    /**
     * Method to get the Set of Alert Types applicable to given Customer Type
     * 
     * @param customerTypeID
     * @param requestInstance
     * @return Array of Alert Types
     * @throws ApplicationException
     */
    public static Set<String> getAlertTypesofCustomerType(String customerTypeID, DataControllerRequest requestInstance)
            throws ApplicationException {

        if (StringUtils.isBlank(customerTypeID) || requestInstance == null) {
            LOG.error("Invalid Parameters. Failed to fetch Alert Types of given Customer Type");
            throw new ApplicationException(ErrorCodeEnum.ERR_20916);
        }

        Map<String, String> inputMap = new HashMap<>();
        inputMap.put(ODataQueryConstants.FILTER, "CustomerTypeId eq '" + customerTypeID + "'");
        inputMap.put(ODataQueryConstants.SELECT, "AlertTypeId");
        String readAlertTypeCustomerTypeResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPECUSTOMERTYPE_READ,
                inputMap, null, requestInstance);
        JSONObject readAlertTypeCustomerTypeResponseJSON = CommonUtilities
                .getStringAsJSONObject(readAlertTypeCustomerTypeResponse);
        if (readAlertTypeCustomerTypeResponseJSON != null
                && readAlertTypeCustomerTypeResponseJSON.has(FabricConstants.OPSTATUS)
                && readAlertTypeCustomerTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readAlertTypeCustomerTypeResponseJSON.has("alerttypecustomertype")) {
            LOG.debug("Successful Read Operation");
            Set<String> alertTypes = new HashSet<>();
            JSONArray alertTypeCustomerTypeArray = readAlertTypeCustomerTypeResponseJSON
                    .optJSONArray("alerttypecustomertype");
            if (alertTypeCustomerTypeArray != null && alertTypeCustomerTypeArray.length() > 0) {
                JSONObject currJSONObject = null;
                for (Object currObject : alertTypeCustomerTypeArray) {
                    if (currObject instanceof JSONObject) {
                        currJSONObject = (JSONObject) currObject;
                        if (currJSONObject.has("AlertTypeId")) {
                            alertTypes.add(currJSONObject.optString("AlertTypeId"));
                        }
                    }
                }
            }
            return alertTypes;
        } else {
            throw new ApplicationException(ErrorCodeEnum.ERR_20917);
        }

    }

    /**
     * Method to get map of account types
     * 
     * @param requestInstance
     * @return Map of account types
     * @throws ApplicationException
     */
    public static Map<String, String> getAccountTypesMap(DataControllerRequest requestInstance)
            throws ApplicationException {

        if (ACCOUNT_TYPE_MAP != null) {
            return ACCOUNT_TYPE_MAP;
        } else {
            Map<String, String> accountTypes = new HashMap<>();

            Map<String, String> inputMap = new HashMap<>();
            String readAlertTypeAccountTypeResponse = Executor.invokeService(ServiceURLEnum.ACCOUNTTYPE_READ, inputMap,
                    null, requestInstance);
            JSONObject readAlertTypeAccountTypeResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readAlertTypeAccountTypeResponse);
            if (readAlertTypeAccountTypeResponseJSON != null
                    && readAlertTypeAccountTypeResponseJSON.has(FabricConstants.OPSTATUS)
                    && readAlertTypeAccountTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readAlertTypeAccountTypeResponseJSON.has("accounttype")) {
                LOG.debug("Successful Read Operation");

                JSONArray alertTypeAccountTypeArray = readAlertTypeAccountTypeResponseJSON.optJSONArray("accounttype");
                if (alertTypeAccountTypeArray != null && alertTypeAccountTypeArray.length() > 0) {
                    JSONObject currJSONObject = null;
                    for (Object currObject : alertTypeAccountTypeArray) {
                        if (currObject instanceof JSONObject) {
                            currJSONObject = (JSONObject) currObject;
                            if (currJSONObject.has("TypeID") && currJSONObject.has("TypeDescription")) {
                                accountTypes.put(currJSONObject.getString("TypeDescription"),
                                        currJSONObject.getString("TypeID"));
                            }
                        }
                    }
                }
            } else {
                throw new ApplicationException(ErrorCodeEnum.ERR_20891);
            }
            ACCOUNT_TYPE_MAP = accountTypes;
            return ACCOUNT_TYPE_MAP;

        }
    }

    /**
     * Method to get map of account type ids
     * 
     * @param requestInstance
     * @return Map of account types
     * @throws ApplicationException
     */
    public static Map<String, String> getAccountTypeIdsMap(DataControllerRequest requestInstance)
            throws ApplicationException {

        if (ACCOUNT_TYPE_IDS_MAP != null) {
            return ACCOUNT_TYPE_IDS_MAP;
        } else {
            Map<String, String> accountTypes = new HashMap<>();

            Map<String, String> inputMap = new HashMap<>();
            String readAlertTypeAccountTypeResponse = Executor.invokeService(ServiceURLEnum.ACCOUNTTYPE_READ, inputMap,
                    null, requestInstance);
            JSONObject readAlertTypeAccountTypeResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readAlertTypeAccountTypeResponse);
            if (readAlertTypeAccountTypeResponseJSON != null
                    && readAlertTypeAccountTypeResponseJSON.has(FabricConstants.OPSTATUS)
                    && readAlertTypeAccountTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readAlertTypeAccountTypeResponseJSON.has("accounttype")) {
                LOG.debug("Successful Read Operation");

                JSONArray alertTypeAccountTypeArray = readAlertTypeAccountTypeResponseJSON.optJSONArray("accounttype");
                if (alertTypeAccountTypeArray != null && alertTypeAccountTypeArray.length() > 0) {
                    JSONObject currJSONObject = null;
                    for (Object currObject : alertTypeAccountTypeArray) {
                        if (currObject instanceof JSONObject) {
                            currJSONObject = (JSONObject) currObject;
                            if (currJSONObject.has("TypeID") && currJSONObject.has("TypeDescription")) {
                                accountTypes.put(currJSONObject.getString("TypeID"),
                                        currJSONObject.getString("TypeDescription"));
                            }
                        }
                    }
                }
            } else {
                throw new ApplicationException(ErrorCodeEnum.ERR_20891);
            }
            ACCOUNT_TYPE_IDS_MAP = accountTypes;
            return ACCOUNT_TYPE_IDS_MAP;

        }
    }

    /**
     * Method to get the Set of Alert Types applicable to given Account Type
     * 
     * @param accountTypeID
     * @param requestInstance
     * @return Array of Alert Types
     * @throws ApplicationException
     */
    public static Set<String> getAlertTypesofAccountType(String accountTypeID, DataControllerRequest requestInstance)
            throws ApplicationException {

        if (StringUtils.isBlank(accountTypeID) || requestInstance == null) {
            LOG.error("Invalid Parameters. Failed to fetch Alert Types of given Account Type");
            throw new ApplicationException(ErrorCodeEnum.ERR_20916);
        }
        Set<String> alertTypes = new HashSet<>();

        Map<String, String> inputMap = new HashMap<>();
        inputMap.put(ODataQueryConstants.FILTER, "AccountTypeId eq '" + accountTypeID + "'");
        inputMap.put(ODataQueryConstants.SELECT, "AlertTypeId");
        String readAlertTypeAccountTypeResponse = Executor.invokeService(ServiceURLEnum.ALERTTYPEACCOUNTTYPE_READ,
                inputMap, null, requestInstance);
        JSONObject readAlertTypeAccountTypeResponseJSON = CommonUtilities
                .getStringAsJSONObject(readAlertTypeAccountTypeResponse);
        if (readAlertTypeAccountTypeResponseJSON != null
                && readAlertTypeAccountTypeResponseJSON.has(FabricConstants.OPSTATUS)
                && readAlertTypeAccountTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readAlertTypeAccountTypeResponseJSON.has("alerttypeaccounttype")) {
            LOG.debug("Successful Read Operation");

            JSONArray alertTypeAccountTypeArray = readAlertTypeAccountTypeResponseJSON
                    .optJSONArray("alerttypeaccounttype");
            if (alertTypeAccountTypeArray != null && alertTypeAccountTypeArray.length() > 0) {
                JSONObject currJSONObject = null;
                for (Object currObject : alertTypeAccountTypeArray) {
                    if (currObject instanceof JSONObject) {
                        currJSONObject = (JSONObject) currObject;
                        if (currJSONObject.has("AlertTypeId")) {
                            alertTypes.add(currJSONObject.optString("AlertTypeId"));
                        }
                    }
                }
            }
        }
        return alertTypes;
    }

    /**
     * Method to get the Alert Attributes
     * 
     * @param acceptLanguage
     * @param requestInstance
     * @return Map of structure <AlertAttributeID, AlertAttributeRecord>
     * @throws ApplicationException
     */
    public static Map<String, Record> getAlertAttributes(String acceptLanguage, DataControllerRequest requestInstance)
            throws ApplicationException {
        return getAlertAttributes(null, acceptLanguage, requestInstance);
    }

    /**
     * Method to get the Alert Attributes based on Alert Attribute ID
     * 
     * @param alertAttributeIds
     * @param acceptLanguage
     * @param requestInstance
     * @return Map of structure <AlertAttributeID, AlertAttributeRecord>
     * @throws ApplicationException
     */
    public static Map<String, Record> getAlertAttributes(Set<String> alertAttributeIds, String acceptLanguage,
            DataControllerRequest requestInstance) throws ApplicationException {

        if (requestInstance == null) {
            LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception.");
            throw new ApplicationException(ErrorCodeEnum.ERR_20922);
        }

        // Construct Filter Query
        String currentLocale;
        StringBuffer filterQueryBuffer = new StringBuffer();
        currentLocale = acceptLanguage;
        if (StringUtils.isBlank(acceptLanguage)) {
            currentLocale = DEFAULT_LOCALE;
        }
        if (StringUtils.equals(currentLocale, DEFAULT_LOCALE)) {
            filterQueryBuffer.append("(alertattribute_LanguageCode eq '" + acceptLanguage + "')");
        } else {
            filterQueryBuffer.append("(alertattribute_LanguageCode eq '" + acceptLanguage
                    + "' or alertattribute_LanguageCode eq '" + DEFAULT_LOCALE + "')");
        }
        if (alertAttributeIds != null && !alertAttributeIds.isEmpty()) {
            if (filterQueryBuffer.length() > 0) {
                filterQueryBuffer.append(" and (");
            }
            int index = 0;
            for (String currAlertAttributeId : alertAttributeIds) {
                filterQueryBuffer.append("alertattribute_id eq '" + currAlertAttributeId + "'");
                if (index != alertAttributeIds.size() - 1) {
                    filterQueryBuffer.append(" or ");
                }
                index++;
            }
            filterQueryBuffer.append(")");
            filterQueryBuffer.trimToSize();
        }
        String filterQuery = filterQueryBuffer.toString();
        filterQuery = filterQuery.trim();

        // Construct Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put(ODataQueryConstants.FILTER, filterQuery);

        // Read Alert Attributes View
        String readAlertAttributeViewResponse = Executor.invokeService(ServiceURLEnum.ALERTATTRIBUTE_VIEW_READ,
                inputMap, null, requestInstance);
        JSONObject readAlertAttributeViewResponseJSON = CommonUtilities
                .getStringAsJSONObject(readAlertAttributeViewResponse);
        if (readAlertAttributeViewResponseJSON == null
                || !readAlertAttributeViewResponseJSON.has(FabricConstants.OPSTATUS)
                || readAlertAttributeViewResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || !readAlertAttributeViewResponseJSON.has("alertattribute_view")) {
            LOG.error("Failed CRUD Operation");
            throw new ApplicationException(ErrorCodeEnum.ERR_20923);
        }
        LOG.debug("Successful CRUD Operation");

        // Construct Result Map
        JSONObject currJSON;
        String currAlertAttributeId;
        Map<String, Record> alertAttributesMap = new HashMap<>();
        JSONArray alertAttributesJSONArray = readAlertAttributeViewResponseJSON.optJSONArray("alertattribute_view");

        Map<String, Record> userPreferredLocaleRecords = new HashMap<>();
        Map<String, Record> defaultLocaleRecords = new HashMap<>();
        // alertattribute_LanguageCode alertattribute_id

        for (Object currObject : alertAttributesJSONArray) {
            if (currObject instanceof JSONObject) {

                currJSON = (JSONObject) currObject;
                Record currAlertAttributeRecord = null;
                currAlertAttributeId = currJSON.optString("alertattribute_id");

                if (userPreferredLocaleRecords.containsKey(currAlertAttributeId)) {
                    currAlertAttributeRecord = userPreferredLocaleRecords.get(currAlertAttributeId);
                } else if (defaultLocaleRecords.containsKey(currAlertAttributeId)) {
                    currAlertAttributeRecord = defaultLocaleRecords.get(currAlertAttributeId);
                } else {
                    currAlertAttributeRecord = new Record();
                    for (String currKey : currJSON.keySet()) {
                        if (currKey.startsWith("alertattribute_")) {
                            currAlertAttributeRecord
                                    .addParam(new Param(currKey, currJSON.optString(currKey), FabricConstants.STRING));
                        }
                    }
                    currAlertAttributeRecord.setId("alertAttribute");

                    if (StringUtils.equals(currJSON.optString("alertattribute_LanguageCode"), currentLocale)) {
                        userPreferredLocaleRecords.put(currAlertAttributeId, currAlertAttributeRecord);
                    } else {
                        defaultLocaleRecords.put(currAlertAttributeId, currAlertAttributeRecord);
                    }

                }

                // Add list of possible values if current Attribute is of type list
                if (currJSON.has("alertattributelistvalues_id") && StringUtils
                        .equalsIgnoreCase(currJSON.optString("alertattributelistvalues_LanguageCode"), currentLocale)) {
                    // Alert Attribute is of list type
                    Dataset valuesDataset = currAlertAttributeRecord.getDatasetById("values");
                    if (valuesDataset == null) {
                        valuesDataset = new Dataset();
                        valuesDataset.setId("values");
                        currAlertAttributeRecord.addDataset(valuesDataset);
                    }
                    Record currValueRecord = new Record();
                    for (String currKey : currJSON.keySet()) {
                        if (currKey.startsWith("alertattributelistvalues_")) {
                            currValueRecord
                                    .addParam(new Param(currKey, currJSON.optString(currKey), FabricConstants.STRING));
                        }
                    }
                    valuesDataset.addRecord(currValueRecord);
                }
            }
        }

        for (Entry<String, Record> currEntry : userPreferredLocaleRecords.entrySet()) {
            alertAttributesMap.put(currEntry.getKey(), currEntry.getValue());
            if (defaultLocaleRecords.containsKey(currEntry.getKey())) {
                // Remove Records of Default Locale for which the records of user preferred
                // Locale are present
                defaultLocaleRecords.remove(currEntry.getKey());
            }
        }

        // Add Default Locale Records for which records of User Preferred Locale are not
        // available
        for (Entry<String, Record> currEntry : defaultLocaleRecords.entrySet()) {
            alertAttributesMap.put(currEntry.getKey(), currEntry.getValue());
        }

        return alertAttributesMap;
    }

    /**
     * Method to get the Alert Conditions
     * 
     * @param acceptLanguage
     * @param requestInstance
     * @return Map of structure <AlertConditionID, AlertConditionRecord>
     * @throws ApplicationException
     */
    public static Map<String, Record> getAlertConditions(String acceptLanguage, DataControllerRequest requestInstance)
            throws ApplicationException {
        return getAlertConditions(null, acceptLanguage, requestInstance);
    }

    /**
     * Method to get the Alert Conditions based on Alert Condition ID
     * 
     * @param alertConditionIds
     * @param acceptLanguage
     * @param requestInstance
     * @return Map of structure <AlertConditionID, AlertConditionRecord>
     * @throws ApplicationException
     */
    public static Map<String, Record> getAlertConditions(Set<String> alertConditionIds, String acceptLanguage,
            DataControllerRequest requestInstance) throws ApplicationException {

        if (requestInstance == null) {
            LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception.");
            throw new ApplicationException(ErrorCodeEnum.ERR_20922);
        }

        // Construct Filter Query
        String currentLocale;
        StringBuffer filterQueryBuffer = new StringBuffer();
        if (StringUtils.isBlank(acceptLanguage)) {
            currentLocale = DEFAULT_LOCALE;
        } else {
            currentLocale = acceptLanguage;
        }
        if (StringUtils.equals(currentLocale, DEFAULT_LOCALE)) {
            filterQueryBuffer.append("(LanguageCode eq '" + acceptLanguage + "')");
        } else {
            filterQueryBuffer
                    .append("(LanguageCode eq '" + acceptLanguage + "' or LanguageCode eq '" + DEFAULT_LOCALE + "')");
        }
        if (alertConditionIds != null && !alertConditionIds.isEmpty()) {
            if (filterQueryBuffer.length() > 0) {
                filterQueryBuffer.append(" and (");
            }
            int index = 0;
            for (String currAlertConditionId : alertConditionIds) {
                filterQueryBuffer.append("id eq '" + currAlertConditionId + "'");
                if (index != alertConditionIds.size() - 1) {
                    filterQueryBuffer.append(" or ");
                }
                index++;
            }
            filterQueryBuffer.append(")");
            filterQueryBuffer.trimToSize();
        }
        String filterQuery = filterQueryBuffer.toString();
        filterQuery = filterQuery.trim();
        LOG.debug("Filter Query:" + filterQuery);

        // Construct Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put(ODataQueryConstants.FILTER, filterQuery);
        inputMap.put(ODataQueryConstants.SELECT, "id,LanguageCode,Name,NoOfFields");

        // Read Alert Condition
        String readAlertConditionResponse = Executor.invokeService(ServiceURLEnum.ALERTCONDITION_READ, inputMap, null,
                requestInstance);
        JSONObject readAlertConditionResponseJSON = CommonUtilities.getStringAsJSONObject(readAlertConditionResponse);
        if (readAlertConditionResponseJSON == null || !readAlertConditionResponseJSON.has(FabricConstants.OPSTATUS)
                || readAlertConditionResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || !readAlertConditionResponseJSON.has("alertcondition")) {
            LOG.error("Failed CRUD Operation");
            throw new ApplicationException(ErrorCodeEnum.ERR_20922);
        }
        LOG.debug("Successful CRUD Operation");

        JSONObject currJSON;
        Map<String, Record> alertConditionMap = new HashMap<>();
        JSONArray alertConditionJSONArray = readAlertConditionResponseJSON.optJSONArray("alertcondition");

        // Filter Alert Conditions based on Locale
        alertConditionJSONArray = CommonUtilities.filterRecordsByLocale(alertConditionJSONArray, "LanguageCode", "id",
                DEFAULT_LOCALE);

        // Construct Result Map
        for (Object currObject : alertConditionJSONArray) {
            if (currObject instanceof JSONObject) {
                currJSON = (JSONObject) currObject;
                Record currRecord = new Record();
                currRecord.setId("alertCondition");
                for (String currKey : currJSON.keySet()) {
                    currRecord.addParam(new Param(currKey, currJSON.optString(currKey), FabricConstants.STRING));
                }
                alertConditionMap.put(currJSON.optString("id"), currRecord);
            }
        }

        LOG.debug("Returning Success Response");
        return alertConditionMap;
    }

    /**
     * Method to fetch the association of Customer to the defined Alert Categories
     * 
     * @param alertCategoryID
     * @param customerId
     * @param accountId
     * @param requestInstance
     * @return
     * @throws ApplicationException
     */
    public static Map<String, JSONObject> getCustomerCategorySubscription(String customerId, String accountId,
            String accountTypeId, String acceptLanguage, DataControllerRequest requestInstance)
            throws ApplicationException {
        if (requestInstance == null) {
            LOG.error("Data Controller Request Instance is NULL. Throwing Application Exception.");
            throw new ApplicationException(ErrorCodeEnum.ERR_20925);
        }

        // Fetch all of Alert Categories
        JSONArray alertCategoriesJSONArray = fetchAlertCategories(acceptLanguage, requestInstance);

        // Fetch associated Alert Categories
        Map<String, JSONObject> associatedAlertCategoryMap = getCustomerAlertCategoryAssociation(customerId, accountId,
                accountTypeId, requestInstance);

        // Collate associated Alert Categories list with the master list of Alert
        // Categories

        if (alertCategoriesJSONArray != null && alertCategoriesJSONArray.length() > 0) {
            JSONObject currJSONObject;
            String currAlertCategoryId = StringUtils.EMPTY;

            for (Object currObject : alertCategoriesJSONArray) {
                if (currObject instanceof JSONObject) {
                    currJSONObject = (JSONObject) currObject;

                    JSONObject currAlertCategoryObject = new JSONObject();

                    for (String key : currJSONObject.keySet()) {
                        currAlertCategoryObject.put(key, currJSONObject.optString(key));
                    }
                    currAlertCategoryId = currJSONObject.optString("alertcategory_id");
                    if (associatedAlertCategoryMap.containsKey(currAlertCategoryId)) {
                        currAlertCategoryObject.put(CATEGORY_SUBSCRIPTION_PARAM,
                                associatedAlertCategoryMap.get(currAlertCategoryId));
                    } else {
                        JSONObject currSubscriptionObject = new JSONObject();
                        currSubscriptionObject.put(IS_SUBSCRIBED_PARAM, String.valueOf(false));
                        currSubscriptionObject.put(IS_INITIAL_LOAD_PARAM, String.valueOf(true));
                        currAlertCategoryObject.put(CATEGORY_SUBSCRIPTION_PARAM, currSubscriptionObject);
                    }
                    associatedAlertCategoryMap.put(currAlertCategoryId, currAlertCategoryObject);
                }
            }
        }

        LOG.debug("Returning Success Response");
        return associatedAlertCategoryMap;
    }

    /**
     * Method to get the list of Alert Categories associated to a customer. This method returns only those Alert
     * Categories to which a customer has performed a subscribe or unsubscribe action. Alert Categories for which the
     * customer has not set any preference are not a part of the returned map.
     * 
     * @param customerId
     * @param accountId
     * @param requestInstance
     * @return Map denoting the association of Customer with Alert Categories
     * @throws ApplicationException
     */
    public static Map<String, JSONObject> getCustomerAlertCategoryAssociation(String customerId, String accountId,
            String accountTypeId, DataControllerRequest requestInstance) throws ApplicationException {
        // Construct Filter Query
        StringBuffer filterQueryBuffer = new StringBuffer();
        filterQueryBuffer.append("Customer_id eq '" + customerId + "'");
        if (StringUtils.isNotBlank(accountId)) {
            filterQueryBuffer.append(" and AccountID eq '" + accountId + "'");
        }
        if (StringUtils.isNotBlank(accountTypeId)) {
            filterQueryBuffer.append(" and AccountType eq '" + accountTypeId + "'");
        }

        filterQueryBuffer.trimToSize();
        String filterQuery = filterQueryBuffer.toString();
        filterQuery = filterQuery.trim();

        // Construct Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put(ODataQueryConstants.FILTER, filterQuery);

        // Read Customer Alert Switch
        String readCustomerAlertSwitchResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERALERTSWITCH_READ,
                inputMap, null, requestInstance);
        JSONObject readCustomerAlertSwitchResponseJSON = CommonUtilities
                .getStringAsJSONObject(readCustomerAlertSwitchResponse);
        if (readCustomerAlertSwitchResponseJSON == null
                || !readCustomerAlertSwitchResponseJSON.has(FabricConstants.OPSTATUS)
                || readCustomerAlertSwitchResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || !readCustomerAlertSwitchResponseJSON.has("customeralertswitch")) {
            LOG.error("Failed CRUD Operation");
            throw new ApplicationException(ErrorCodeEnum.ERR_20925);
        }
        LOG.debug("Successful CRUD Operation");

        // Prepare Map of Associated Alert Categories
        Map<String, JSONObject> associatedAlertCategoryMap = new HashMap<>();
        JSONArray customerAlertSwitchArray = readCustomerAlertSwitchResponseJSON.optJSONArray("customeralertswitch");
        if (customerAlertSwitchArray != null) {
            JSONObject currJSON;
            boolean isSubscribed = false;
            String currAlertCategoryId = StringUtils.EMPTY;
            for (Object currObject : customerAlertSwitchArray) {

                if (currObject instanceof JSONObject) {
                    currJSON = (JSONObject) currObject;

                    JSONObject currSubscriptionObject = new JSONObject();

                    if (StringUtils.equalsIgnoreCase(StatusEnum.SID_SUBSCRIBED.name(),
                            currJSON.optString("Status_id"))) {
                        isSubscribed = true;
                    } else {
                        isSubscribed = false;
                    }
                    currSubscriptionObject.put(IS_SUBSCRIBED_PARAM, String.valueOf(isSubscribed));
                    currSubscriptionObject.put(IS_INITIAL_LOAD_PARAM, String.valueOf(false));

                    currAlertCategoryId = currJSON.optString("AlertCategoryId");
                    associatedAlertCategoryMap.put(currAlertCategoryId, currSubscriptionObject);
                }
            }

        }
        return associatedAlertCategoryMap;
    }

    /**
     * Method to get all Alert Categories
     * 
     * @param acceptLanguage
     * @param requestInstance
     * @return JSON Array of Alert Categories
     * @throws ApplicationException
     */
    public static JSONArray fetchAlertCategories(String acceptLanguage, DataControllerRequest requestInstance)
            throws ApplicationException {
        // Fetch list of Alert Categories
        StringBuffer filterQueryBuffer = new StringBuffer();
        if (StringUtils.equals(acceptLanguage, DEFAULT_LOCALE)) {
            filterQueryBuffer.append("(alertcategorytext_LanguageCode eq '" + acceptLanguage + "')");
        } else {
            filterQueryBuffer.append("(alertcategorytext_LanguageCode eq '" + acceptLanguage
                    + "' or alertcategorytext_LanguageCode eq '" + DEFAULT_LOCALE + "')");
        }
        filterQueryBuffer.trimToSize();

        // Construct Input Map
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put(ODataQueryConstants.FILTER, filterQueryBuffer.toString());
        parameterMap.put(ODataQueryConstants.ORDER_BY, "alertcategory_DisplaySequence asc");

        String readAlertCategoryViewResponse = Executor.invokeService(ServiceURLEnum.ALERTCATEGORY_VIEW_READ,
                parameterMap, null, requestInstance);
        JSONObject readAlertCategoryViewResponseJSON = CommonUtilities
                .getStringAsJSONObject(readAlertCategoryViewResponse);
        if (readAlertCategoryViewResponseJSON == null
                || !readAlertCategoryViewResponseJSON.has(FabricConstants.OPSTATUS)
                || readAlertCategoryViewResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || !readAlertCategoryViewResponseJSON.has("alertcategory_view")) {
            LOG.error("Failed CRUD Operation");
            throw new ApplicationException(ErrorCodeEnum.ERR_20921);
        }
        LOG.debug("Successful CRUD Operation");
        JSONArray alertCategoriesJSONArray = readAlertCategoryViewResponseJSON.optJSONArray("alertcategory_view");

        // Filter Alert Category Records based on Locale
        alertCategoriesJSONArray = CommonUtilities.filterRecordsByLocale(alertCategoriesJSONArray,
                "alertcategorytext_LanguageCode", "alertcategory_id", DEFAULT_LOCALE);
        return alertCategoriesJSONArray;
    }

    /**
     * Method to determine if an Alert with the alertCode already exists
     * 
     * @param alertCode
     * @param requestInstance
     * @return availability status
     * @throws ApplicationException
     */
    public static boolean isAlertCodeAvaialable(String alertCode, DataControllerRequest requestInstance)
            throws ApplicationException {

        if (requestInstance == null) {
            LOG.error("DataControllerRequest Instance is NULL. Returning Error Response");
            throw new ApplicationException(ErrorCodeEnum.ERR_20918);
        }

        // Prepare Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put(ODataQueryConstants.FILTER, "id eq '" + alertCode + "'");
        inputMap.put(ODataQueryConstants.SELECT, "id");

        // Read Alert Types
        String readAlertTypeResponse = Executor.invokeService(ServiceURLEnum.DBXALERTTYPE_READ, inputMap, null,
                requestInstance);
        JSONObject readAlertTypeResponseJSON = CommonUtilities.getStringAsJSONObject(readAlertTypeResponse);

        if (readAlertTypeResponseJSON != null && readAlertTypeResponseJSON.has(FabricConstants.OPSTATUS)
                && readAlertTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readAlertTypeResponseJSON.optJSONArray("dbxalerttype") != null) {
            LOG.debug("Successful CRUD Operation");

            // Check if an Alert with the given code already exists
            JSONArray alertTypeRecords = readAlertTypeResponseJSON.optJSONArray("dbxalerttype");
            if (alertTypeRecords.length() > 0) {
                LOG.debug("Alert with the Code " + alertCode + " already exists. The same code cannot be reused");
                return false;
            } else {
                LOG.debug("Alert with the Code " + alertCode + " does not exist. The code can be used");
                return true;
            }
        }

        LOG.error("Failed CRUD Operation");
        throw new ApplicationException(ErrorCodeEnum.ERR_20918);

    }

    public static String getAccountTypeIdFromName(String accountTypeName, DataControllerRequest requestInstance)
            throws ApplicationException {
        // Prepare Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put(ODataQueryConstants.FILTER, "TypeDescription eq '" + accountTypeName + "'");
        inputMap.put(ODataQueryConstants.SELECT, "TypeID");

        // Read Alert Types
        String readAccountTypeResponse = Executor.invokeService(ServiceURLEnum.ACCOUNTTYPE_READ, inputMap, null,
                requestInstance);
        JSONObject readAccountTypeResponseJSON = CommonUtilities.getStringAsJSONObject(readAccountTypeResponse);

        if (readAccountTypeResponseJSON != null && readAccountTypeResponseJSON.has(FabricConstants.OPSTATUS)
                && readAccountTypeResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readAccountTypeResponseJSON.optJSONArray("accounttype") != null) {
            LOG.debug("Successful CRUD Operation");

            JSONArray accountTypeResponse = readAccountTypeResponseJSON.optJSONArray("accounttype");

            if (accountTypeResponse.length() == 0 || !accountTypeResponse.getJSONObject(0).has("TypeID")) {
                return null;
            }
            return accountTypeResponse.getJSONObject(0).getString("TypeID");
        }
        LOG.error("Failed CRUD Operation");
        throw new ApplicationException(ErrorCodeEnum.ERR_20889);
    }

    /**
     * Method to custom format the locale as per Kony Client SDK
     * 
     * @param locale
     * @return
     */
    public static String formatLocaleAsPerKonyMobileSDK(String locale) {

        if (StringUtils.equalsIgnoreCase(locale, "en")) {
            return "en-US";
        }
        return locale;
    }

    /**
     * Method to construct the customer account numbers/types map
     * 
     * @param isAlertsAccountNumberLevel
     * @return
     */
    public static Set<String> getAccountStatusArray(boolean isAlertsAccountNumberLevel, JSONArray customeralerts) {
        Set<String> accountsArray = new HashSet<>();
        customeralerts.forEach((alert) -> {
            JSONObject alertObject = (JSONObject) alert;
            if (isAlertsAccountNumberLevel && alertObject.has("AccountID")
                    && alertObject.getString("Status_id").equalsIgnoreCase(StatusEnum.SID_SUBSCRIBED.name())) {
                accountsArray.add(alertObject.getString("AccountID"));

            } else if (alertObject.has("AccountType")
                    && alertObject.getString("Status_id").equalsIgnoreCase(StatusEnum.SID_SUBSCRIBED.name())) {
                accountsArray.add(alertObject.getString("AccountType"));
            }
        });

        return accountsArray;
    }
}
