package com.kony.adminconsole.service.business;

import java.util.HashMap;
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
import com.kony.adminconsole.exception.ApplicationException;
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

public class UpgradeRolesAndPermissionsService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(UpgradeRolesAndPermissionsService.class);

    private static final String TYPE_ID_MICRO_BUSINESS = "TYPE_ID_MICRO_BUSINESS";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result result = new Result();
        String customerId = "";
        String userName = "";

        CustomerHandler customerHandler = new CustomerHandler();
        Map<String, String> inputMap = new HashMap<String, String>();
        try {
            if (requestInstance.getParameter("userName") != null) {
                userName = requestInstance.getParameter("userName");
            } else {
                ErrorCodeEnum.ERR_20533.setErrorCode(result);
                result.addParam(new Param("Status", "Failure", FabricConstants.STRING));
                return result;

            }
            customerId = customerHandler.getCustomerId(userName, requestInstance);
            if (StringUtils.isBlank(customerId)) {
                ErrorCodeEnum.ERR_20613.setErrorCode(result);
                if (result.getParamByName(FabricConstants.OPSTATUS) != null) {
                    // opstatus '0' required for OLB to display appropriate error message
                    result.getParamByName(FabricConstants.OPSTATUS).setValue("0");
                }
                result.addParam(new Param("Status", "Failure", FabricConstants.STRING));
                return result;

            }

            // Fetch CustomerType_id from customer table
            inputMap.clear();
            inputMap.put(ODataQueryConstants.FILTER, "id eq '" + customerId + "'");
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

            if (!StringUtils.equalsIgnoreCase(customerTypeId, TYPE_ID_MICRO_BUSINESS)) {
                ErrorCodeEnum.ERR_20791.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            }
            if (!(fetchAndDeleteRolesOfCustomer(customerId, requestInstance).getParamByName("status").getValue()
                    .equalsIgnoreCase("success"))) {
                LOG.debug("Error while fetching and deleting Roles of Customer");
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20507.setErrorCode(result);
                return result;
            } else {
                if (!(fetchAndDeleteEntitlementsOfCustomer(customerId, requestInstance).getParamByName("status")
                        .getValue().equalsIgnoreCase("success"))) {
                    LOG.debug("Error while fetching and deleting Entitlements of Customer");
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    ErrorCodeEnum.ERR_21024.setErrorCode(result);
                    return result;
                } else {
                    if (!(fetchAndDeleteLimitsOfCustomer(customerId, requestInstance).getParamByName("status")
                            .getValue().equalsIgnoreCase("success"))) {
                        LOG.debug("Error while fetching and deleting Sevice Limits of Customer");
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        ErrorCodeEnum.ERR_20503.setErrorCode(result);
                        return result;
                    } else {
                        if (setRoleAndServiceDefaultLimitsToCustomer(customerId, requestInstance)
                                .getParamByName("status").getValue().equalsIgnoreCase("Failure")) {
                            LOG.debug("Error while Creating roles and Sevice Limits to Customer");
                            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                                    ActivityStatusEnum.FAILED,
                                    "Upgrade Roles and Permissions Failed for the customer:" + customerId);
                            ErrorCodeEnum.ERR_20503.setErrorCode(result);
                            return result;
                        }
                    }
                }
            }

        } catch (Exception e) {
            LOG.error("Unexepected Error in Upgrading roles and Permissions. Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Upgrade Roles and Permissions Failed for the customer:" + customerId);
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
        }
        result.addParam(new Param("status", "Success", FabricConstants.STRING));
        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                ActivityStatusEnum.SUCCESSFUL, "Upgrade Roles and Permissions Success for the customer:" + customerId);
        return result;
    }

    public Result fetchAndDeleteRolesOfCustomer(String customerID, DataControllerRequest requestInstance) {
        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.clear();
        inputMap.put(ODataQueryConstants.FILTER, "Customer_id eq '" + customerID + "'");
        inputMap.put(ODataQueryConstants.SELECT, "Group_id");
        Result result = new Result();
        String currGroupId;
        String readCustomerGroupResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_READ, inputMap, null,
                requestInstance);

        JSONObject readCustomerGroupResponseJSON = CommonUtilities.getStringAsJSONObject(readCustomerGroupResponse);
        if (readCustomerGroupResponseJSON != null && readCustomerGroupResponseJSON.has(FabricConstants.OPSTATUS)
                && readCustomerGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readCustomerGroupResponseJSON.has("customergroup")) {
            JSONArray readCustomerGroupJSONArray = readCustomerGroupResponseJSON.optJSONArray("customergroup");
            if (!(readCustomerGroupJSONArray == null || readCustomerGroupJSONArray.length() < 1)) {
                for (int i = 0; i < readCustomerGroupJSONArray.length(); i++) {
                    JSONObject customerGroupObj = readCustomerGroupJSONArray.getJSONObject(i);
                    currGroupId = customerGroupObj.getString("Group_id");

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
            } else {
                // No Records Exists
            }
        } else {
            LOG.debug("Failed to get customer group");
            ErrorCodeEnum.ERR_20404.setErrorCode(result);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
        }
        result.addParam(new Param("status", "Success", FabricConstants.STRING));
        return result;
    }

    public Result fetchAndDeleteEntitlementsOfCustomer(String customerID, DataControllerRequest requestInstance) {
        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.clear();
        inputMap.put(ODataQueryConstants.FILTER, "Customer_id eq '" + customerID + "'");
        inputMap.put(ODataQueryConstants.SELECT, "Service_id");
        Result result = new Result();
        String currServiceId;
        String readCustomerEntitlementsResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERENTITLEMENT_READ,
                inputMap, null, requestInstance);

        JSONObject readCustomerEntitlementsResponseJSON = CommonUtilities
                .getStringAsJSONObject(readCustomerEntitlementsResponse);
        if (readCustomerEntitlementsResponseJSON != null
                && readCustomerEntitlementsResponseJSON.has(FabricConstants.OPSTATUS)
                && readCustomerEntitlementsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readCustomerEntitlementsResponseJSON.has("customerentitlement")) {
            JSONArray readCustomerEntitlementJSONArray = readCustomerEntitlementsResponseJSON
                    .optJSONArray("customerentitlement");
            if (!(readCustomerEntitlementJSONArray == null || readCustomerEntitlementJSONArray.length() < 1)) {
                for (int i = 0; i < readCustomerEntitlementJSONArray.length(); i++) {
                    JSONObject customerEntitlementObj = readCustomerEntitlementJSONArray.getJSONObject(i);
                    currServiceId = customerEntitlementObj.getString("Service_id");

                    inputMap.clear();
                    inputMap.put("Service_id", currServiceId);
                    inputMap.put("Customer_id", customerID);
                    String deleteCustomerGroupResponse = Executor
                            .invokeService(ServiceURLEnum.CUSTOMERENTITLEMENT_DELETE, inputMap, null, requestInstance);
                    JSONObject deleteCustomerGroupResponseJSON = CommonUtilities
                            .getStringAsJSONObject(deleteCustomerGroupResponse);
                    if (deleteCustomerGroupResponseJSON != null
                            && deleteCustomerGroupResponseJSON.has(FabricConstants.OPSTATUS)
                            && deleteCustomerGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                        LOG.debug("Deleted customer Entitlement successfully");
                    } else {
                        LOG.debug("Failed to delete customer entitlements");
                        ErrorCodeEnum.ERR_21024.setErrorCode(result);
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        return result;
                    }
                }
            } else {
                // No Records Exists
            }
        } else {
            LOG.debug("Failed to get customer group");
            ErrorCodeEnum.ERR_20983.setErrorCode(result);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
        }
        result.addParam(new Param("status", "Success", FabricConstants.STRING));
        return result;
    }

    public Result fetchAndDeleteLimitsOfCustomer(String customerID, DataControllerRequest requestInstance) {
        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.clear();
        inputMap.put(ODataQueryConstants.FILTER, "Customer_id eq '" + customerID + "'");
        inputMap.put(ODataQueryConstants.SELECT, "Customer_id,Service_id");
        Result result = new Result();
        String readCustomerServiceLimitsResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERENTITLEMENT_READ,
                inputMap, null, requestInstance);

        JSONObject readCustomerServiceLimitsResponseJSON = CommonUtilities
                .getStringAsJSONObject(readCustomerServiceLimitsResponse);
        if (readCustomerServiceLimitsResponseJSON != null
                && readCustomerServiceLimitsResponseJSON.has(FabricConstants.OPSTATUS)
                && readCustomerServiceLimitsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readCustomerServiceLimitsResponseJSON.has("customerentitlement")) {
            JSONArray readCustomerServiceLimitsResponseJSONArray = readCustomerServiceLimitsResponseJSON
                    .optJSONArray("customerentitlement");
            if (!(readCustomerServiceLimitsResponseJSONArray == null
                    || readCustomerServiceLimitsResponseJSONArray.length() < 1)) {
                for (int i = 0; i < readCustomerServiceLimitsResponseJSONArray.length(); i++) {
                    JSONObject customerServiceLimitsObj = readCustomerServiceLimitsResponseJSONArray.getJSONObject(i);

                    inputMap.clear();
                    inputMap.put("Customer_id", customerServiceLimitsObj.getString("Customer_id"));
                    inputMap.put("Service_id", customerServiceLimitsObj.getString("Service_id"));
                    String deleteCustomerServiceLimitResponse = Executor
                            .invokeService(ServiceURLEnum.CUSTOMERENTITLEMENT_DELETE, inputMap, null, requestInstance);
                    JSONObject deleteCustomerServiceLimitResponseJSON = CommonUtilities
                            .getStringAsJSONObject(deleteCustomerServiceLimitResponse);
                    if (deleteCustomerServiceLimitResponseJSON != null
                            && deleteCustomerServiceLimitResponseJSON.has(FabricConstants.OPSTATUS)
                            && deleteCustomerServiceLimitResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                        LOG.debug("Deleted Customer service limits successfully");
                    } else {
                        LOG.debug("Failed to delete Customer service limits");
                        ErrorCodeEnum.ERR_20503.setErrorCode(result);
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        return result;
                    }
                }
            } else {
                // No Records Exists
            }
        } else {
            LOG.debug("Failed to get customer service limits");
            ErrorCodeEnum.ERR_20504.setErrorCode(result);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
        }
        result.addParam(new Param("status", "Success", FabricConstants.STRING));
        return result;
    }

    public Result setRoleAndServiceDefaultLimitsToCustomer(String customerID, DataControllerRequest requestInstance)
            throws ApplicationException {
        Result result = new Result();
        String groupID = "GROUP_MICRO_ADMINISTRATOR";
        Map<String, String> inputBodyMap = new HashMap<String, String>();
        String loggedInUserId = null;
        UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
        if (loggedInUserDetails != null) {
            loggedInUserId = loggedInUserDetails.getUserId();
        }
        // Create CustomerGroup
        inputBodyMap.clear();
        inputBodyMap.put("Customer_id", customerID);
        inputBodyMap.put("Group_id", groupID);
        inputBodyMap.put("createdby", loggedInUserId);
        inputBodyMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());

        String createCustomerGroupResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_CREATE, inputBodyMap,
                null, requestInstance);

        JSONObject createCustomerGroupResponseJSON = CommonUtilities.getStringAsJSONObject(createCustomerGroupResponse);
        if (createCustomerGroupResponseJSON != null && createCustomerGroupResponseJSON.has(FabricConstants.OPSTATUS)
                && createCustomerGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            LOG.debug("CustomerGroup created successfully.");
        } else {
            LOG.error("Failed to create CustomerGroup");
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20505.setErrorCode(result);
            return result;
        }

        // Assigning Service level limits to customer
        inputBodyMap.clear();
        inputBodyMap.put(ODataQueryConstants.FILTER, "Group_id eq '" + groupID + "'");
        inputBodyMap.put(ODataQueryConstants.SELECT, "Service_id");
        // Get Group Services
        String getGroupEntitlementResponse = Executor.invokeService(ServiceURLEnum.GROUPENTITLEMENT_READ, inputBodyMap,
                null, requestInstance);

        JSONObject getGroupEntitlementResponseJSON = CommonUtilities.getStringAsJSONObject(getGroupEntitlementResponse);
        String currServiceId;
        if (getGroupEntitlementResponseJSON != null && getGroupEntitlementResponseJSON.has(FabricConstants.OPSTATUS)
                && getGroupEntitlementResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            JSONArray getGroupEntitlementResponseJSONArray = getGroupEntitlementResponseJSON
                    .optJSONArray("groupentitlement");
            if (!(getGroupEntitlementResponseJSONArray == null || getGroupEntitlementResponseJSONArray.length() < 1)) {
                for (int i = 0; i < getGroupEntitlementResponseJSONArray.length(); i++) {
                    JSONObject customerGroupObj = getGroupEntitlementResponseJSONArray.getJSONObject(i);
                    currServiceId = customerGroupObj.getString("Service_id");
                    Map<String, String> postParameterMap = new HashMap<String, String>();
                    if (!(currServiceId.equalsIgnoreCase("SERVICE_ID_39"))) {
                        postParameterMap.clear();
                        postParameterMap.put(ODataQueryConstants.FILTER,
                                "id eq '" + currServiceId + "' and Type_id eq 'SER_TYPE_TRNS'");
                        postParameterMap.put(ODataQueryConstants.SELECT, "MaxTransferLimit, TransactionLimit_id");
                        // Get Each Service MaxTransferlimit and Transaction limit Id
                        String readServiceResponse = Executor.invokeService(ServiceURLEnum.SERVICE_READ,
                                postParameterMap, null, requestInstance);
                        JSONObject readServiceResponseJSON = CommonUtilities.getStringAsJSONObject(readServiceResponse);
                        if (getGroupEntitlementResponseJSON != null
                                && getGroupEntitlementResponseJSON.has(FabricConstants.OPSTATUS)
                                && getGroupEntitlementResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                            JSONArray readServiceResponseJSONArray = readServiceResponseJSON.optJSONArray("service");
                            String currMaxTransferLimit;
                            String currTransactionLimitId;
                            String currMaxDailyLimit = "0";
                            if (!(readServiceResponseJSONArray == null || readServiceResponseJSONArray.length() < 1)) {
                                JSONObject currServiceRecord = readServiceResponseJSONArray.getJSONObject(0);
                                currMaxTransferLimit = currServiceRecord.getString("MaxTransferLimit");
                                currTransactionLimitId = currServiceRecord.getString("TransactionLimit_id");
                                postParameterMap.clear();
                                postParameterMap.put(ODataQueryConstants.FILTER,
                                        "TransactionLimit_id eq '" + currTransactionLimitId + "'");
                                postParameterMap.put(ODataQueryConstants.SELECT, "MaximumLimit");

                                // Get MaxDailyLimit of Transaction limit Id
                                String readPeriodicLimitsResponse = Executor.invokeService(
                                        ServiceURLEnum.PERIODICLIMIT_READ, postParameterMap, null, requestInstance);
                                JSONObject readPeriodicLimitsResponseJSON = CommonUtilities
                                        .getStringAsJSONObject(readPeriodicLimitsResponse);
                                if (readPeriodicLimitsResponseJSON != null
                                        && readPeriodicLimitsResponseJSON.has(FabricConstants.OPSTATUS)
                                        && readPeriodicLimitsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                                    JSONArray readPeriodicLimitsResponseJSONArray = readPeriodicLimitsResponseJSON
                                            .optJSONArray("periodiclimit");
                                    if (!(readPeriodicLimitsResponseJSONArray == null
                                            || readPeriodicLimitsResponseJSONArray.length() < 1)) {
                                        JSONObject currPeriodiclimitRecord = readPeriodicLimitsResponseJSONArray
                                                .getJSONObject(0);
                                        currMaxDailyLimit = currPeriodiclimitRecord.getString("MaximumLimit");
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
                                inputBodyMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());

                                String createBBCustomerServiceLimitResponse = Executor.invokeService(
                                        ServiceURLEnum.CUSTOMERENTITLEMENT_CREATE, inputBodyMap, null, requestInstance);

                                JSONObject createBBCustomerServiceLimitResponseJSON = CommonUtilities
                                        .getStringAsJSONObject(createBBCustomerServiceLimitResponse);
                                if (createBBCustomerServiceLimitResponseJSON != null
                                        && createBBCustomerServiceLimitResponseJSON.has(FabricConstants.OPSTATUS)
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
        return result;

    }
}
