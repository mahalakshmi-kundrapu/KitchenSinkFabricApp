package com.kony.adminconsole.service.business;

import java.math.BigDecimal;

/**
 * Service to Manage Requests related to BusinessBankingCustomerServiceLimit
 * 
 * @author Chandan Gupta
 *
 */

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class BusinessBankingCustomerServiceLimitManageService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(BusinessBankingCustomerServiceLimitManageService.class);

    private static final String CREATE_BB_CUSTOMER_SERVICE_LIMIT_METHOD_NAME = "createBBCustomerServiceLimit";
    private static final String EDIT_BB_CUSTOMER_SERVICE_LIMIT_METHOD_NAME = "editBBCustomerServiceLimit";
    private static final String GET_BB_CUSTOMER_SERVICE_LIMIT_METHOD_NAME = "getBBCustomerServiceLimit";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            if (StringUtils.equalsIgnoreCase(methodID, CREATE_BB_CUSTOMER_SERVICE_LIMIT_METHOD_NAME)) {
                return createBBCustomerServiceLimit(requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, EDIT_BB_CUSTOMER_SERVICE_LIMIT_METHOD_NAME)) {
                return editBBCustomerServiceLimit(requestInstance);
            } else if (StringUtils.equalsIgnoreCase(methodID, GET_BB_CUSTOMER_SERVICE_LIMIT_METHOD_NAME)) {
                return getBBCustomerServiceLimit(requestInstance);
            }

            return null;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    public Result createBBCustomerServiceLimit(DataControllerRequest requestInstance) {
        Result result = new Result();
        try {
            String loggedInUserId = null;
            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            if (loggedInUserDetails != null) {
                loggedInUserId = loggedInUserDetails.getUserId();
            }

            // Validate CustomerID
            String customerID = requestInstance.getParameter("Customer_id");
            if (StringUtils.isBlank(customerID)) {
                ErrorCodeEnum.ERR_20688.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Validate GroupID
            String groupID = requestInstance.getParameter("Role_id");
            if (StringUtils.isBlank(groupID)) {
                ErrorCodeEnum.ERR_20569.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Fetch Type_id from membergroup table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.FILTER, "id eq '" + groupID + "'");
            inputMap.put(ODataQueryConstants.SELECT, "Type_id");

            String readGroupResponse = Executor.invokeService(ServiceURLEnum.MEMBERGROUP_READ, inputMap, null,
                    requestInstance);

            String groupTypeId = null;

            JSONObject readGroupResponseJSON = CommonUtilities.getStringAsJSONObject(readGroupResponse);
            if (readGroupResponseJSON != null && readGroupResponseJSON.has(FabricConstants.OPSTATUS)
                    && readGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readGroupResponseJSON.has("membergroup")) {
                JSONArray readGroupJSONArray = readGroupResponseJSON.optJSONArray("membergroup");
                if (!(readGroupJSONArray == null || readGroupJSONArray.length() < 1)) {
                    JSONObject groupObj = readGroupJSONArray.getJSONObject(0);
                    groupTypeId = groupObj.getString("Type_id");
                }
            }

            if (StringUtils.isBlank(groupTypeId)) {
                ErrorCodeEnum.ERR_20789.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Fetch CustomerType_id from customer table
            inputMap.clear();
            inputMap.put(ODataQueryConstants.FILTER, "id eq '" + customerID + "'");
            inputMap.put(ODataQueryConstants.SELECT, "CustomerType_id");

            String readCustomerResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_READ, inputMap, null,
                    requestInstance);

            String customerTypeId = null;

            JSONObject readCustomerResponseJSON = CommonUtilities.getStringAsJSONObject(readCustomerResponse);
            if (readCustomerResponseJSON != null && readCustomerResponseJSON.has(FabricConstants.OPSTATUS)
                    && readCustomerResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readCustomerResponseJSON.has("customer")) {
                JSONArray readCustomerJSONArray = readCustomerResponseJSON.optJSONArray("customer");
                if (!(readCustomerJSONArray == null || readCustomerJSONArray.length() < 1)) {
                    JSONObject customerObj = readCustomerJSONArray.getJSONObject(0);
                    customerTypeId = customerObj.getString("CustomerType_id");
                }
            }

            if (StringUtils.isBlank(customerTypeId)) {
                ErrorCodeEnum.ERR_20790.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            if (!StringUtils.equals(groupTypeId, customerTypeId)) {
                ErrorCodeEnum.ERR_20791.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Fetch GroupId from customergroup table
            inputMap.clear();
            inputMap.put(ODataQueryConstants.FILTER, "Customer_id eq '" + customerID + "'");
            inputMap.put(ODataQueryConstants.SELECT, "Group_id");

            String readCustomerGroupResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_READ, inputMap, null,
                    requestInstance);

            String currGroupId = null;

            JSONObject readCustomerGroupResponseJSON = CommonUtilities.getStringAsJSONObject(readCustomerGroupResponse);
            if (readCustomerGroupResponseJSON != null && readCustomerGroupResponseJSON.has(FabricConstants.OPSTATUS)
                    && readCustomerGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readCustomerGroupResponseJSON.has("customergroup")) {
                JSONArray readCustomerGroupJSONArray = readCustomerGroupResponseJSON.optJSONArray("customergroup");
                if (!(readCustomerGroupJSONArray == null || readCustomerGroupJSONArray.length() < 1)) {
                    JSONObject customerGroupObj = readCustomerGroupJSONArray.getJSONObject(0);
                    currGroupId = customerGroupObj.getString("Group_id");
                }
            }

            // If record exist then we'll delete it
            if ((currGroupId != null) && StringUtils.isBlank(currGroupId) == false) {
                // Delete customergroup record
                inputMap.clear();
                inputMap.put("Group_id", currGroupId);
                inputMap.put("Customer_id", customerID);
                String deleteCustomerGroupResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_DELETE,
                        inputMap, null, requestInstance);
                JSONObject deleteCustomerGroupResponseJSON = CommonUtilities
                        .getStringAsJSONObject(deleteCustomerGroupResponse);
                if (deleteCustomerGroupResponseJSON != null
                        && deleteCustomerGroupResponseJSON.has(FabricConstants.OPSTATUS)
                        && deleteCustomerGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                    LOG.debug("Deleted customer group successfully");
                } else {
                    LOG.debug("Failed to delete customer group");
                    ErrorCodeEnum.ERR_20507.setErrorCode(result);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                }
            }

            // fetch and delete customer entitlements if customer entitlements exists
            result = fetchAndDeleteCustomerEntitlements(customerID, requestInstance);
            if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                return result;
            }

            // Create CustomerGroup
            Map<String, String> inputBodyMap = new HashMap<String, String>();
            inputBodyMap.put("Customer_id", customerID);
            inputBodyMap.put("Group_id", groupID);
            inputBodyMap.put("createdby", loggedInUserId);
            inputBodyMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());

            String createCustomerGroupResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_CREATE,
                    inputBodyMap, null, requestInstance);

            JSONObject createCustomerGroupResponseJSON = CommonUtilities
                    .getStringAsJSONObject(createCustomerGroupResponse);
            if (createCustomerGroupResponseJSON != null && createCustomerGroupResponseJSON.has(FabricConstants.OPSTATUS)
                    && createCustomerGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                LOG.debug("CustomerGroup created successfully.");
            } else {
                LOG.error("Failed to create CustomerGroup");
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20505.setErrorCode(result);
                return result;
            }

            // delete customer entitlements if exists
            if (groupID.equalsIgnoreCase("GROUP_ADMINISTRATOR")
                    || groupID.equalsIgnoreCase("GROUP_MICRO_ADMINISTRATOR")) {
                inputBodyMap.clear();
                inputBodyMap.put(ODataQueryConstants.FILTER, "Customer_id eq '" + customerID + "'");
                inputBodyMap.put(ODataQueryConstants.SELECT, "Service_id");
                String getCustomerEntitlementResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERENTITLEMENT_READ,
                        inputBodyMap, null, requestInstance);

                JSONObject getCustomerEntitlementResponseJSON = CommonUtilities
                        .getStringAsJSONObject(getCustomerEntitlementResponse);
                String currServiceId;
                if (getCustomerEntitlementResponseJSON != null
                        && getCustomerEntitlementResponseJSON.has(FabricConstants.OPSTATUS)
                        && getCustomerEntitlementResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                    JSONArray getCustomerEntitlementResponseJSONArray = getCustomerEntitlementResponseJSON
                            .optJSONArray("customerentitlement");

                    if (!(getCustomerEntitlementResponseJSONArray == null
                            || getCustomerEntitlementResponseJSONArray.length() < 1)) {
                        for (int i = 0; i < getCustomerEntitlementResponseJSONArray.length(); i++) {
                            JSONObject customerGroupObj = getCustomerEntitlementResponseJSONArray.getJSONObject(i);
                            currServiceId = customerGroupObj.getString("Service_id");
                            Map<String, String> postParameterMap = new HashMap<String, String>();
                            postParameterMap.clear();
                            postParameterMap.put("Customer_id", customerID);
                            postParameterMap.put("Service_id", currServiceId);
                            String deleteCustomerEntitlementResponse = Executor.invokeService(
                                    ServiceURLEnum.CUSTOMERENTITLEMENT_DELETE, postParameterMap, null, requestInstance);

                            JSONObject deleteCustomerEntitlementResponseJSON = CommonUtilities
                                    .getStringAsJSONObject(deleteCustomerEntitlementResponse);
                            if (deleteCustomerEntitlementResponseJSON != null
                                    && deleteCustomerEntitlementResponseJSON.has(FabricConstants.OPSTATUS)
                                    && deleteCustomerEntitlementResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                                LOG.error("CustomerEntitlements Deleted successfully");
                                result.addParam(new Param("status", "Success", FabricConstants.STRING));
                            } else {
                                LOG.error("Failed to delete CustomerEntitlements");
                                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                                ErrorCodeEnum.ERR_21024.setErrorCode(result);
                                return result;
                            }
                        }
                    }
                } else {
                    LOG.error("Failed to get CustomerEntitlements");
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    ErrorCodeEnum.ERR_21025.setErrorCode(result);
                    return result;
                }
            }

            // Create Customer Entitlements
            // if (groupID.equalsIgnoreCase("GROUP_ADMINISTRATOR")
            // || groupID.equalsIgnoreCase("GROUP_MICRO_ADMINISTRATOR")) {
            // inputBodyMap.clear();
            // inputBodyMap.put("Customer_id", customerID);
            // inputBodyMap.put("Service_id", "SERVICE_ID_39");
            // String createCustomerEntitlementResponse = Executor
            // .invokeService(ServiceURLEnum.CUSTOMERENTITLEMENT_CREATE, inputBodyMap, null,
            // requestInstance);

            // JSONObject createCustomerGroupEntitlementsJSON = CommonUtilities
            // .getStringAsJSONObject(createCustomerEntitlementResponse);
            // if (createCustomerGroupEntitlementsJSON != null
            // && createCustomerGroupEntitlementsJSON.has(FabricConstants.OPSTATUS)
            // && createCustomerGroupEntitlementsJSON.getInt(FabricConstants.OPSTATUS) == 0)
            // {
            // LOG.debug("CustomerEntitlements created successfully.");
            // result.addParam(new Param("status", "Success", FabricConstants.STRING));
            // } else {
            // LOG.error("Failed to create CustomerEntitlements");
            // result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            // ErrorCodeEnum.ERR_23728.setErrorCode(result);
            // return result;
            // }
            // }
            // Create BBLimits Through Enrollment

            if (groupID.equalsIgnoreCase("GROUP_ADMINISTRATOR")
                    || groupID.equalsIgnoreCase("GROUP_MICRO_ADMINISTRATOR")) {
                if (requestInstance.getParameter("services") == null
                        || requestInstance.getParameter("services").length() == 0) {
                    inputBodyMap.clear();
                    inputBodyMap.put(ODataQueryConstants.FILTER, "Group_id eq '" + groupID + "'");
                    inputBodyMap.put(ODataQueryConstants.SELECT, "Service_id");
                    // Get Group Services
                    String getGroupEntitlementResponse = Executor.invokeService(ServiceURLEnum.GROUPENTITLEMENT_READ,
                            inputBodyMap, null, requestInstance);

                    JSONObject getGroupEntitlementResponseJSON = CommonUtilities
                            .getStringAsJSONObject(getGroupEntitlementResponse);
                    String currServiceId;
                    if (getGroupEntitlementResponseJSON != null
                            && getGroupEntitlementResponseJSON.has(FabricConstants.OPSTATUS)
                            && getGroupEntitlementResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                        JSONArray getGroupEntitlementResponseJSONArray = getGroupEntitlementResponseJSON
                                .optJSONArray("groupentitlement");
                        if (!(getGroupEntitlementResponseJSONArray == null
                                || getGroupEntitlementResponseJSONArray.length() < 1)) {
                            for (int i = 0; i < getGroupEntitlementResponseJSONArray.length(); i++) {
                                JSONObject customerGroupObj = getGroupEntitlementResponseJSONArray.getJSONObject(i);
                                currServiceId = customerGroupObj.getString("Service_id");
                                Map<String, String> postParameterMap = new HashMap<String, String>();
                                if (!(currServiceId.equalsIgnoreCase("SERVICE_ID_39"))) {
                                    postParameterMap.clear();
                                    postParameterMap.put(ODataQueryConstants.FILTER,
                                            "id eq '" + currServiceId + "' and Type_id eq 'SER_TYPE_TRNS'");
                                    postParameterMap.put(ODataQueryConstants.SELECT,
                                            "MaxTransferLimit, TransactionLimit_id");
                                    // Get Each Service MaxTransferlimit and Transaction limit Id
                                    String readServiceResponse = Executor.invokeService(ServiceURLEnum.SERVICE_READ,
                                            postParameterMap, null, requestInstance);
                                    JSONObject readServiceResponseJSON = CommonUtilities
                                            .getStringAsJSONObject(readServiceResponse);
                                    if (getGroupEntitlementResponseJSON != null
                                            && getGroupEntitlementResponseJSON.has(FabricConstants.OPSTATUS)
                                            && getGroupEntitlementResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                                        JSONArray readServiceResponseJSONArray = readServiceResponseJSON
                                                .optJSONArray("service");
                                        String currMaxTransferLimit;
                                        String currTransactionLimitId;
                                        String currMaxDailyLimit = "0";
                                        if (!(readServiceResponseJSONArray == null
                                                || readServiceResponseJSONArray.length() < 1)) {
                                            JSONObject currServiceRecord = readServiceResponseJSONArray
                                                    .getJSONObject(0);
                                            currMaxTransferLimit = currServiceRecord.getString("MaxTransferLimit");
                                            currTransactionLimitId = currServiceRecord.getString("TransactionLimit_id");
                                            postParameterMap.clear();
                                            postParameterMap.put(ODataQueryConstants.FILTER,
                                                    "TransactionLimit_id eq '" + currTransactionLimitId + "'");
                                            postParameterMap.put(ODataQueryConstants.SELECT, "MaximumLimit");

                                            // Get MaxDailyLimit of Transaction limit Id
                                            String readPeriodicLimitsResponse = Executor.invokeService(
                                                    ServiceURLEnum.PERIODICLIMIT_READ, postParameterMap, null,
                                                    requestInstance);
                                            JSONObject readPeriodicLimitsResponseJSON = CommonUtilities
                                                    .getStringAsJSONObject(readPeriodicLimitsResponse);
                                            if (readPeriodicLimitsResponseJSON != null
                                                    && readPeriodicLimitsResponseJSON.has(FabricConstants.OPSTATUS)
                                                    && readPeriodicLimitsResponseJSON
                                                            .getInt(FabricConstants.OPSTATUS) == 0) {
                                                JSONArray readPeriodicLimitsResponseJSONArray =
                                                        readPeriodicLimitsResponseJSON
                                                                .optJSONArray("periodiclimit");
                                                if (!(readPeriodicLimitsResponseJSONArray == null
                                                        || readPeriodicLimitsResponseJSONArray.length() < 1)) {
                                                    JSONObject currPeriodiclimitRecord =
                                                            readPeriodicLimitsResponseJSONArray
                                                                    .getJSONObject(0);
                                                    currMaxDailyLimit = currPeriodiclimitRecord
                                                            .getString("MaximumLimit");
                                                }
                                            } else {
                                                LOG.error("Failed to fetch Periodic limits");
                                                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                                                ErrorCodeEnum.ERR_20818.setErrorCode(result);
                                                return result;
                                            }

                                            // Creating BBCustomerLimits
                                            inputBodyMap.clear();
                                            inputBodyMap.put("Customer_id", customerID);
                                            inputBodyMap.put("Service_id", currServiceId);
                                            inputBodyMap.put("MaxTransactionLimit", currMaxTransferLimit);
                                            inputBodyMap.put("MaxDailyLimit", currMaxDailyLimit);
                                            inputBodyMap.put("createdby", loggedInUserId);
                                            inputBodyMap.put("createdts",
                                                    CommonUtilities.getISOFormattedLocalTimestamp());

                                            String createBBCustomerServiceLimitResponse = Executor.invokeService(
                                                    ServiceURLEnum.CUSTOMERENTITLEMENT_CREATE, inputBodyMap, null,
                                                    requestInstance);

                                            JSONObject createBBCustomerServiceLimitResponseJSON = CommonUtilities
                                                    .getStringAsJSONObject(createBBCustomerServiceLimitResponse);
                                            if (createBBCustomerServiceLimitResponseJSON != null
                                                    && createBBCustomerServiceLimitResponseJSON
                                                            .has(FabricConstants.OPSTATUS)
                                                    && createBBCustomerServiceLimitResponseJSON
                                                            .getInt(FabricConstants.OPSTATUS) == 0) {
                                                LOG.debug("BBCustomerServiceLimit created successfully.");
                                                result.addParam(new Param("status", "Success", FabricConstants.STRING));
                                            } else {
                                                LOG.error("Failed to create BBCustomerServiceLimit");
                                                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                                                ErrorCodeEnum.ERR_20501.setErrorCode(result);
                                                return result;
                                            }

                                        } else {
                                            LOG.error("Failed to get service list");
                                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                                            ErrorCodeEnum.ERR_20384.setErrorCode(result);
                                            return result;
                                        }
                                    }

                                }
                            }
                        }
                    } else {
                        LOG.error("Failed to get Group Entitlements");
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        ErrorCodeEnum.ERR_20424.setErrorCode(result);
                        return result;
                    }
                }
            }
            if (requestInstance.containsKeyInRequest("services") == false) {
                // Services key will not be present during enrollment call
                LOG.debug("BBCustomerServiceLimit created successfully.");
                result.addParam(new Param("status", "Success", FabricConstants.STRING));
                return result;
            }
            JSONArray bbCustomerServiceLimitJSONArray = CommonUtilities
                    .getStringAsJSONArray(requestInstance.getParameter("services"));
            if (bbCustomerServiceLimitJSONArray == null || bbCustomerServiceLimitJSONArray.length() == 0) {
                LOG.debug("BBCustomerServiceLimit created successfully.");
                result.addParam(new Param("status", "Success", FabricConstants.STRING));
                return result;
            } else {
                // Create BBCustomer Limits
                for (int indexVar = 0; indexVar < bbCustomerServiceLimitJSONArray.length(); indexVar++) {
                    JSONObject bbCustomerServiceLimitObj = bbCustomerServiceLimitJSONArray.getJSONObject(indexVar);

                    // Validate ServiceID
                    String serviceID = bbCustomerServiceLimitObj.getString("Service_id");
                    if (StringUtils.isBlank(serviceID)) {
                        ErrorCodeEnum.ERR_20866.setErrorCode(result);
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        return result;
                    }

                    // Fetch serviceTypeID
                    inputBodyMap.clear();
                    inputBodyMap.put(ODataQueryConstants.FILTER, "id eq '" + serviceID + "'");
                    inputBodyMap.put(ODataQueryConstants.SELECT, "Type_id, MaxTransferLimit, TransactionLimit_id");

                    String readServiceResponse = Executor.invokeService(ServiceURLEnum.SERVICE_READ, inputBodyMap, null,
                            requestInstance);

                    String serviceTypeID = null;
                    String serviceMaxTransferLimit = null;
                    String serviceTransactionLimitId = null;

                    JSONObject readServiceResponseJSON = CommonUtilities.getStringAsJSONObject(readServiceResponse);
                    if (readServiceResponseJSON != null && readServiceResponseJSON.has(FabricConstants.OPSTATUS)
                            && readServiceResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                            && readServiceResponseJSON.has("service")) {
                        JSONArray readServiceJSONArray = readServiceResponseJSON.optJSONArray("service");
                        if (readServiceJSONArray == null || readServiceJSONArray.length() < 1) {
                            // Invalid BBCustomerServiceLimit ID.
                            LOG.error("Failed to get Services");
                            ErrorCodeEnum.ERR_20384.setErrorCode(result);
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            return result;
                        }
                        JSONObject serviceObj = readServiceJSONArray.getJSONObject(0);
                        serviceTypeID = serviceObj.getString("Type_id");
                        serviceMaxTransferLimit = serviceObj.getString("MaxTransferLimit");
                        serviceTransactionLimitId = serviceObj.getString("TransactionLimit_id");
                    }

                    // Validate serviceTypeID
                    if (StringUtils.isBlank(serviceTypeID)) {
                        ErrorCodeEnum.ERR_20783.setErrorCode(result);
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        return result;
                    }

                    // Validate serviceMaxTransferLimit
                    if (StringUtils.isBlank(serviceMaxTransferLimit)) {
                        ErrorCodeEnum.ERR_20781.setErrorCode(result);
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        return result;
                    }

                    // Validate serviceTransactionLimitId
                    if (StringUtils.isBlank(serviceTransactionLimitId)) {
                        ErrorCodeEnum.ERR_20574.setErrorCode(result);
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        return result;
                    }

                    if (serviceTypeID.equals("SER_TYPE_TRNS")) {

                        // Validate MaxTransactionLimit
                        String maxTransactionLimit = bbCustomerServiceLimitObj.getString("MaxTransactionLimit").trim();
                        if (StringUtils.isBlank(maxTransactionLimit)) {
                            ErrorCodeEnum.ERR_20781.setErrorCode(result);
                            result.addParam(new Param("message", "MaxTransactionLimit cannot be empty",
                                    FabricConstants.STRING));
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            return result;
                        }

                        // Checking maxTransactionLimit is decimal type
                        if (NumberUtils.isParsable(maxTransactionLimit) == false) {
                            ErrorCodeEnum.ERR_20785.setErrorCode(result);
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            return result;
                        }

                        // customer maxTransactionLimit <= service maxTransactionLimit
                        BigDecimal serviceMaxTransLimit = new BigDecimal(serviceMaxTransferLimit);
                        BigDecimal currMaxTransLimit = new BigDecimal(maxTransactionLimit);
                        if (serviceMaxTransLimit.compareTo(currMaxTransLimit) < 0) {
                            ErrorCodeEnum.ERR_20787.setErrorCode(result);
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            return result;
                        }

                        // Validate MaxDailyLimit
                        String maxDailyLimit = bbCustomerServiceLimitObj.getString("MaxDailyLimit").trim();
                        if (StringUtils.isBlank(maxDailyLimit)) {
                            ErrorCodeEnum.ERR_20782.setErrorCode(result);
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            return result;
                        }

                        // Checking maxDailyLimit is decimal type
                        if (NumberUtils.isParsable(maxDailyLimit) == false) {
                            ErrorCodeEnum.ERR_20786.setErrorCode(result);
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            return result;
                        }

                        // Fetch periodId
                        inputBodyMap.clear();
                        inputBodyMap.put(ODataQueryConstants.FILTER, "Code eq '" + "DAILY" + "'");
                        inputBodyMap.put(ODataQueryConstants.SELECT, "id");

                        String readPeriodResponse = Executor.invokeService(ServiceURLEnum.PERIOD_READ, inputBodyMap,
                                null, requestInstance);

                        String periodID = null;
                        JSONObject readPeriodResponseJSON = CommonUtilities.getStringAsJSONObject(readPeriodResponse);
                        if (readPeriodResponseJSON != null && readPeriodResponseJSON.has(FabricConstants.OPSTATUS)
                                && readPeriodResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                                && readPeriodResponseJSON.has("period")) {
                            JSONArray readPeriodJSONArray = readPeriodResponseJSON.optJSONArray("period");
                            if (readPeriodJSONArray == null || readPeriodJSONArray.length() < 1) {
                                // Invalid Period ID.
                                LOG.error("Failed to get Period");
                                ErrorCodeEnum.ERR_20506.setErrorCode(result);
                                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                                return result;
                            }
                            JSONObject periodObj = readPeriodJSONArray.getJSONObject(0);
                            periodID = periodObj.getString("id");
                        }

                        // Fetch periodicMaxDailyLimit
                        inputBodyMap.clear();
                        inputBodyMap.put(ODataQueryConstants.FILTER, "TransactionLimit_id eq '"
                                + serviceTransactionLimitId + "' and Period_id eq '" + periodID + "'");
                        inputBodyMap.put(ODataQueryConstants.SELECT, "MaximumLimit");

                        String readPeriodicLimitResponse = Executor.invokeService(ServiceURLEnum.PERIODICLIMIT_READ,
                                inputBodyMap, null, requestInstance);

                        String serviceMaxDailyLimit = null;
                        JSONObject readPeriodicLimitResponseJSON = CommonUtilities
                                .getStringAsJSONObject(readPeriodicLimitResponse);
                        if (readPeriodicLimitResponseJSON != null
                                && readPeriodicLimitResponseJSON.has(FabricConstants.OPSTATUS)
                                && readPeriodicLimitResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                                && readPeriodicLimitResponseJSON.has("periodiclimit")) {
                            JSONArray readPeriodicLimitJSONArray = readPeriodicLimitResponseJSON
                                    .optJSONArray("periodiclimit");
                            if (readPeriodicLimitJSONArray == null || readPeriodicLimitJSONArray.length() < 1) {
                                // Invalid Periodic limit.
                                LOG.error("Failed to get maxDailyPeriodiclimit");
                                ErrorCodeEnum.ERR_20572.setErrorCode(result);
                                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                                return result;
                            }
                            JSONObject periodicLimitObj = readPeriodicLimitJSONArray.getJSONObject(0);
                            serviceMaxDailyLimit = periodicLimitObj.getString("MaximumLimit");
                        }

                        BigDecimal sMaxDailyLimit = new BigDecimal(serviceMaxDailyLimit);
                        BigDecimal currMaxDailyLimit = new BigDecimal(maxDailyLimit);
                        if (sMaxDailyLimit.compareTo(currMaxDailyLimit) < 0) {
                            ErrorCodeEnum.ERR_20788.setErrorCode(result);
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            return result;
                        }

                        // Create BBCustomerServiceLimit
                        inputBodyMap.clear();
                        inputBodyMap.put("Customer_id", customerID);
                        inputBodyMap.put("Service_id", serviceID);
                        inputBodyMap.put("MaxTransactionLimit", maxTransactionLimit);
                        inputBodyMap.put("MaxDailyLimit", maxDailyLimit);
                        inputBodyMap.put("createdby", loggedInUserId);
                        inputBodyMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());

                        String createBBCustomerServiceLimitResponse = Executor.invokeService(
                                ServiceURLEnum.CUSTOMERENTITLEMENT_CREATE, inputBodyMap, null, requestInstance);

                        JSONObject createBBCustomerServiceLimitResponseJSON = CommonUtilities
                                .getStringAsJSONObject(createBBCustomerServiceLimitResponse);
                        if (createBBCustomerServiceLimitResponseJSON != null
                                && createBBCustomerServiceLimitResponseJSON.has(FabricConstants.OPSTATUS)
                                && createBBCustomerServiceLimitResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                            LOG.debug("BBCustomerServiceLimit created successfully.");
                            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.BUSINESSCONFIGURATION,
                                    EventEnum.CREATE, ActivityStatusEnum.SUCCESSFUL,
                                    "Customer service limit created successfully." + "Customer id: " + customerID);
                            result.addParam(new Param("status", "Success", FabricConstants.STRING));

                        } else {
                            LOG.error("Failed to create BBCustomerServiceLimit");
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.BUSINESSCONFIGURATION,
                                    EventEnum.CREATE, ActivityStatusEnum.FAILED,
                                    "Failed to create BBCustomerServiceLimit" + "Customer id: " + customerID);
                            ErrorCodeEnum.ERR_20501.setErrorCode(result);
                            return result;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in Create BBCustomerServiceLimit Flow. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
        }
        return result;
    }

    public Result editBBCustomerServiceLimit(DataControllerRequest requestInstance) {
        Result result = new Result();
        try {
            // Validate CustomerID
            String customerID = requestInstance.getParameter("Customer_id");
            if (StringUtils.isBlank(customerID)) {
                ErrorCodeEnum.ERR_20688.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Validate GroupID
            String groupID = requestInstance.getParameter("Role_id");
            if (StringUtils.isBlank(groupID)) {
                ErrorCodeEnum.ERR_20569.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Fetch GroupId from customergroup table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.FILTER, "Customer_id eq '" + customerID + "'");
            inputMap.put(ODataQueryConstants.SELECT, "Group_id");

            String readCustomerGroupResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_READ, inputMap, null,
                    requestInstance);

            String currGroupId = null;

            JSONObject readCustomerGroupResponseJSON = CommonUtilities.getStringAsJSONObject(readCustomerGroupResponse);
            if (readCustomerGroupResponseJSON != null && readCustomerGroupResponseJSON.has(FabricConstants.OPSTATUS)
                    && readCustomerGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readCustomerGroupResponseJSON.has("customergroup")) {
                JSONArray readCustomerGroupJSONArray = readCustomerGroupResponseJSON.optJSONArray("customergroup");
                if (!(readCustomerGroupJSONArray == null || readCustomerGroupJSONArray.length() < 1)) {
                    JSONObject customerGroupObj = readCustomerGroupJSONArray.getJSONObject(0);
                    currGroupId = customerGroupObj.getString("Group_id");
                }
            }

            // If record exist then we'll delete it
            if ((currGroupId != null) && StringUtils.isBlank(currGroupId) == false) {
                // Delete customergroup record
                inputMap.clear();
                inputMap.put("Group_id", currGroupId);
                inputMap.put("Customer_id", customerID);
                String deleteCustomerGroupResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_DELETE,
                        inputMap, null, requestInstance);
                JSONObject deleteCustomerGroupResponseJSON = CommonUtilities
                        .getStringAsJSONObject(deleteCustomerGroupResponse);
                if (deleteCustomerGroupResponseJSON != null
                        && deleteCustomerGroupResponseJSON.has(FabricConstants.OPSTATUS)
                        && deleteCustomerGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                    LOG.debug("Deleted customer group successfully");
                } else {
                    LOG.debug("Failed to delete customer group");
                    ErrorCodeEnum.ERR_20507.setErrorCode(result);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                }
            }

            // fetch and delete customer entitlements if customer entitlements exists
            result = fetchAndDeleteCustomerEntitlements(customerID, requestInstance);
            if (result.getParamByName(ErrorCodeEnum.ERROR_CODE_KEY) != null) {
                return result;
            }

            // Deleting all entries associated with customerID and creating new records
            // again using current payload

            inputMap.clear();
            inputMap.put(ODataQueryConstants.FILTER, "Customer_id eq '" + customerID + "'");
            inputMap.put(ODataQueryConstants.SELECT, "Customer_id,Service_id");

            String readBBCustomerServiceLimitResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERENTITLEMENT_READ,
                    inputMap, null, requestInstance);

            JSONObject readBBCustomerServiceLimitResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readBBCustomerServiceLimitResponse);
            if (readBBCustomerServiceLimitResponseJSON != null
                    && readBBCustomerServiceLimitResponseJSON.has(FabricConstants.OPSTATUS)
                    && readBBCustomerServiceLimitResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readBBCustomerServiceLimitResponseJSON.has("customerentitlement")) {
                JSONArray bbCustomerServiceLimitJSONArray = readBBCustomerServiceLimitResponseJSON
                        .optJSONArray("customerentitlement");
                if (!(bbCustomerServiceLimitJSONArray == null || bbCustomerServiceLimitJSONArray.length() < 1)) {
                    for (int indexVar = 0; indexVar < bbCustomerServiceLimitJSONArray.length(); indexVar++) {
                        JSONObject bbCustomerServiceLimitObj = bbCustomerServiceLimitJSONArray.getJSONObject(indexVar);
                        Map<String, String> postParametersMap = new HashMap<String, String>();
                        postParametersMap.clear();
                        postParametersMap.put("Customer_id", bbCustomerServiceLimitObj.getString("Customer_id"));
                        postParametersMap.put("Service_id", bbCustomerServiceLimitObj.getString("Service_id"));

                        String deleteBBCustomerServiceLimitResponse = Executor.invokeService(
                                ServiceURLEnum.CUSTOMERENTITLEMENT_DELETE, postParametersMap, null, requestInstance);
                        JSONObject deleteBBCustomerServiceLimitResponseJSON = CommonUtilities
                                .getStringAsJSONObject(deleteBBCustomerServiceLimitResponse);
                        if (deleteBBCustomerServiceLimitResponseJSON != null
                                && deleteBBCustomerServiceLimitResponseJSON.has(FabricConstants.OPSTATUS)
                                && deleteBBCustomerServiceLimitResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                            LOG.debug("Deleted BBCustomerServiceLimit successfully");
                        } else {
                            LOG.debug("Failed to delete BBCustomerServiceLimit");
                            ErrorCodeEnum.ERR_20503.setErrorCode(result);
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            return result;
                        }
                    }
                }
            }

            return createBBCustomerServiceLimit(requestInstance);

        } catch (Exception e) {
            LOG.error("Unexepected Error in Update BBCustomerServiceLimit Flow. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
        }
        return result;
    }

    public Result getBBCustomerServiceLimit(DataControllerRequest requestInstance) {
        Result result = new Result();
        try {

            // Validate Username
            String userName = requestInstance.getParameter("Username");
            if (userName == null) {
                ErrorCodeEnum.ERR_20705.setErrorCode(result);
                return result;
            }
            CustomerHandler customerHandler = new CustomerHandler();

            // Validate CustomerID
            String customerID = customerHandler.getCustomerId(userName, requestInstance);
            if (StringUtils.isBlank(customerID)) {
                ErrorCodeEnum.ERR_20688.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }

            // Fetch GroupId from customergroup table
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.FILTER, "Customer_id eq '" + customerID + "'");
            inputMap.put(ODataQueryConstants.SELECT, "Group_id");

            String readCustomerGroupResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_READ, inputMap, null,
                    requestInstance);

            String groupId = null;

            JSONObject readCustomerGroupResponseJSON = CommonUtilities.getStringAsJSONObject(readCustomerGroupResponse);
            if (readCustomerGroupResponseJSON != null && readCustomerGroupResponseJSON.has(FabricConstants.OPSTATUS)
                    && readCustomerGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readCustomerGroupResponseJSON.has("customergroup")) {
                JSONArray readCustomerGroupJSONArray = readCustomerGroupResponseJSON.optJSONArray("customergroup");
                if (!(readCustomerGroupJSONArray == null || readCustomerGroupJSONArray.length() < 1)) {
                    JSONObject customerGroupObj = readCustomerGroupJSONArray.getJSONObject(0);
                    groupId = customerGroupObj.getString("Group_id");
                }
            }

            // Fetch GroupName from membergroup table
            inputMap.clear();
            inputMap.put(ODataQueryConstants.FILTER, "id eq '" + groupId + "'");
            inputMap.put(ODataQueryConstants.SELECT, "Name, Description");

            String readGroupResponse = Executor.invokeService(ServiceURLEnum.MEMBERGROUP_READ, inputMap, null,
                    requestInstance);

            String groupName = null;
            String groupDescription = null;

            JSONObject readGroupResponseJSON = CommonUtilities.getStringAsJSONObject(readGroupResponse);
            if (readGroupResponseJSON != null && readGroupResponseJSON.has(FabricConstants.OPSTATUS)
                    && readGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readGroupResponseJSON.has("membergroup")) {
                JSONArray readGroupJSONArray = readGroupResponseJSON.optJSONArray("membergroup");
                if (!(readGroupJSONArray == null || readGroupJSONArray.length() < 1)) {
                    JSONObject groupObj = readGroupJSONArray.getJSONObject(0);
                    groupName = groupObj.getString("Name");
                    groupDescription = groupObj.getString("Description");
                }
            }

            // Fetch Customer Service Limits
            inputMap.clear();

            inputMap.put(ODataQueryConstants.FILTER, "Customer_id eq '" + customerID + "'");
            String readBBCustomerServiceLimitResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERENTITLEMENT_READ,
                    inputMap, null, requestInstance);

            // Creating payload
            Param Service_Group_Id_Param = new Param("Group_id", groupId, FabricConstants.STRING);
            result.addParam(Service_Group_Id_Param);
            Param Service_Group_Name_Param = new Param("Group_Name", groupName, FabricConstants.STRING);
            result.addParam(Service_Group_Name_Param);
            Param Service_Group_Description_Param = new Param("Group_Description", groupDescription,
                    FabricConstants.STRING);
            result.addParam(Service_Group_Description_Param);
            JSONObject readBBCustomerServiceLimitResponseJSON = CommonUtilities
                    .getStringAsJSONObject(readBBCustomerServiceLimitResponse);
            if (readBBCustomerServiceLimitResponseJSON != null
                    && readBBCustomerServiceLimitResponseJSON.has(FabricConstants.OPSTATUS)
                    && readBBCustomerServiceLimitResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readBBCustomerServiceLimitResponseJSON.has("customerentitlement")) {
                JSONArray bbCustomerServiceLimitJSONArray = readBBCustomerServiceLimitResponseJSON
                        .optJSONArray("customerentitlement");
                Dataset serviceDataset = new Dataset();
                serviceDataset.setId("services");
                if (bbCustomerServiceLimitJSONArray != null && bbCustomerServiceLimitJSONArray.length() > 0) {
                    JSONObject bbCustomerServiceLimitJSONObject = null;
                    for (int indexVar = 0; indexVar < bbCustomerServiceLimitJSONArray.length(); indexVar++) {
                        bbCustomerServiceLimitJSONObject = bbCustomerServiceLimitJSONArray.getJSONObject(indexVar);
                        inputMap.clear();
                        inputMap.put(ODataQueryConstants.FILTER,
                                "id eq '" + bbCustomerServiceLimitJSONObject.getString("Service_id") + "'");
                        inputMap.put(ODataQueryConstants.SELECT, "Name,Description,Type_id");

                        String readServiceResponse = Executor.invokeService(ServiceURLEnum.SERVICE_READ, inputMap, null,
                                requestInstance);
                        String serviceName = null;
                        String serviceDescription = null;

                        JSONObject readServiceResponseJSON = CommonUtilities.getStringAsJSONObject(readServiceResponse);
                        if (readServiceResponseJSON != null && readServiceResponseJSON.has(FabricConstants.OPSTATUS)
                                && readServiceResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                                && readServiceResponseJSON.has("service")) {
                            JSONArray readServiceJSONArray = readServiceResponseJSON.optJSONArray("service");
                            if (readServiceJSONArray == null || readServiceJSONArray.length() < 1) {
                                LOG.error("Failed to get Services");
                                ErrorCodeEnum.ERR_20384.setErrorCode(result);
                                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                                return result;
                            }
                            JSONObject serviceObj = readServiceJSONArray.getJSONObject(0);
                            serviceName = serviceObj.getString("Name");
                            serviceDescription = serviceObj.getString("Description");
                            // Check for non-transactional service and skip adding the record in the final
                            // set
                            if (serviceObj.has("Type_id")
                                    && serviceObj.getString("Type_id").equalsIgnoreCase("SER_TYPE_NONTRANS")) {
                                continue;
                            }
                        }
                        Record currRecord = new Record();
                        String serviceId = bbCustomerServiceLimitJSONObject.getString("Service_id");
                        Param Service_Id_Param = new Param("Id", serviceId, FabricConstants.STRING);
                        currRecord.addParam(Service_Id_Param);
                        Param Service_Name_Param = new Param("Name", serviceName, FabricConstants.STRING);
                        currRecord.addParam(Service_Name_Param);
                        Param Service_Description_Param = new Param("Description", serviceDescription,
                                FabricConstants.STRING);
                        currRecord.addParam(Service_Description_Param);
                        Param Max_Transaction_Limit_Param = new Param("MaxTransactionLimit",
                                bbCustomerServiceLimitJSONObject.getString("MaxTransactionLimit"),
                                FabricConstants.STRING);
                        currRecord.addParam(Max_Transaction_Limit_Param);
                        Param Max_Daily_Limit_Param = new Param("MaxDailyLimit",
                                bbCustomerServiceLimitJSONObject.getString("MaxDailyLimit"), FabricConstants.STRING);
                        currRecord.addParam(Max_Daily_Limit_Param);
                        serviceDataset.addRecord(currRecord);
                    }
                }
                result.addDataset(serviceDataset);
                return result;
            }
            result.addParam(new Param("message", readBBCustomerServiceLimitResponse, FabricConstants.STRING));
            LOG.error("Failed to Fetch BBcustomerservicelimit. Response: " + readBBCustomerServiceLimitResponse);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20504.setErrorCode(result);

        } catch (Exception e) {
            LOG.error("Unexepected Error in Fetching bbcustomerservicelimit. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20504.setErrorCode(result);
        }
        return result;
    }

    public Result fetchAndDeleteCustomerEntitlements(String customerID, DataControllerRequest requestInstance) {
        Result result = new Result();
        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.clear();
        inputMap.put(ODataQueryConstants.FILTER,
                "Customer_id eq '" + customerID + "' and Service_id eq 'SERVICE_ID_39'");
        inputMap.put(ODataQueryConstants.SELECT, "Customer_id");

        String readCustomerEntitlementResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERENTITLEMENT_READ,
                inputMap, null, requestInstance);

        JSONObject readCustomerEntitlementsResponseJSON = CommonUtilities
                .getStringAsJSONObject(readCustomerEntitlementResponse);
        if (readCustomerEntitlementsResponseJSON != null
                && readCustomerEntitlementsResponseJSON.has(FabricConstants.OPSTATUS)
                && readCustomerEntitlementsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readCustomerEntitlementsResponseJSON.has("customerentitlement")) {
            JSONArray readCustomerGroupJSONArray = readCustomerEntitlementsResponseJSON
                    .optJSONArray("customerentitlement");
            if (!(readCustomerGroupJSONArray == null || readCustomerGroupJSONArray.length() < 1)) {
                inputMap.clear();
                inputMap.put("Customer_id", customerID);
                inputMap.put("Service_id", "SERVICE_ID_39");
                String deleteCustomerEntitltmentResponse = Executor
                        .invokeService(ServiceURLEnum.CUSTOMERENTITLEMENT_DELETE, inputMap, null, requestInstance);
                JSONObject deleteCustomerEntitlementResponseJSON = CommonUtilities
                        .getStringAsJSONObject(deleteCustomerEntitltmentResponse);
                if (deleteCustomerEntitlementResponseJSON != null
                        && deleteCustomerEntitlementResponseJSON.has(FabricConstants.OPSTATUS)
                        && deleteCustomerEntitlementResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                    LOG.debug("Deleted customer Entitlement successfully");
                } else {
                    LOG.debug("Failed to delete customer Entitlement");
                    ErrorCodeEnum.ERR_20507.setErrorCode(result);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                }
            }
        } else {
            LOG.debug("Failed to fetch customer Entitlement");
            ErrorCodeEnum.ERR_20983.setErrorCode(result);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            return result;
        }

        return result;
    }

}