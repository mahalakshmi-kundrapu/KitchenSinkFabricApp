package com.kony.adminconsole.service.leadmanagement;

import java.util.HashMap;
import java.util.List;
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
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.kony.adminconsole.utilities.StatusEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to set Lead Configurations (Closure Reasons, Auto-Archive Period)
 *
 * @author Aditya Mankal
 */
public class LeadConfigurationManagementService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(LeadConfigurationManagementService.class);

    private static final String GET_LEAD_CONFIGURATION_METHOD_ID = "getLeadConfiguration";
    private static final String SET_LEAD_CONFIGURATION_METHOD_ID = "setLeadConfiguration";
    private static final String LEAD_AUTO_ARCHIVE_PERIOD_CONFIGURATION_KEY = "LEAD_AUTO_ARCHIVE_PERIOD_IN_DAYS";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) {

        try {

            // Read Inputs
            String addedReasons = requestInstance.getParameter("addedReasons");
            String removedReasons = requestInstance.getParameter("removedReasons");
            String autoArchivePeriod = requestInstance.getParameter("autoArchivePeriod");

            // Fetch Logged In User Info
            String loggedInUser = StringUtils.EMPTY;
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            if (userDetailsBeanInstance != null) {
                loggedInUser = userDetailsBeanInstance.getUserId();
            }

            if (StringUtils.equals(methodID, SET_LEAD_CONFIGURATION_METHOD_ID)) {
                // Set Lead Configuration

                Result result = new Result();

                // Set auto-archive period
                if (NumberUtils.isParsable(autoArchivePeriod)) {
                    setLeadAutoArchivePeriod(requestInstance, Integer.parseInt(autoArchivePeriod));
                    result.addParam(new Param("autoArchivePeriodInDays", String.valueOf(autoArchivePeriod),
                            FabricConstants.INT));
                }

                // Set Closure Reasons
                List<String> addedReasonsList = CommonUtilities.getStringifiedArrayAsList(addedReasons);
                List<String> removedReasonsList = CommonUtilities.getStringifiedArrayAsList(removedReasons);
                setClosureReasons(addedReasonsList, removedReasonsList, loggedInUser, requestInstance);

                // Return success response
                result.addParam(new Param("status", "success", FabricConstants.STRING));

                return result;

            } else if (StringUtils.equals(methodID, GET_LEAD_CONFIGURATION_METHOD_ID)) {
                // Get Lead Configuration
                return getLeadConfiguration(requestInstance);
            }

            return new Result();
        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.error("Exception in Fetching Leads Master Data. Exception:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    /**
     * Method to add/delete lead closure reasons
     * 
     * @param addedReasons
     * @param removedReasons
     * @param loggedInUserId
     * @param requestInstance
     * @throws ApplicationException
     */
    private void setClosureReasons(List<String> addedReasons, List<String> removedReasons, String loggedInUserId,
            DataControllerRequest requestInstance) throws ApplicationException {

        String serviceResponse;
        JSONObject serviceResponseJSON;
        Map<String, String> inputMap = new HashMap<>();

        // Process removed reasons
        if (removedReasons != null && !removedReasons.isEmpty()) {
            for (String reasonId : removedReasons) {
                inputMap.put("id", reasonId);
                serviceResponse = Executor.invokeService(ServiceURLEnum.CLOSUREREASON_DELETE, inputMap, null,
                        requestInstance);
                serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
                // Check operation status
                if (serviceResponse == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                        || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    // Failed operation
                    LOG.error("Failed CRUD Operation. Response:" + serviceResponse);
                    throw new ApplicationException(ErrorCodeEnum.ERR_21550);
                }

                // Return success message
                LOG.debug("Successful CRUD Operation");
            }
        }

        inputMap.clear();
        // Process added reasons
        if (addedReasons != null && !addedReasons.isEmpty()) {
            for (String reason : addedReasons) {
                inputMap.put("id", CommonUtilities.getNewId().toString());
                inputMap.put("reason", reason);
                inputMap.put("status_id", StatusEnum.SID_ACTIVE.name());
                inputMap.put("createdby", loggedInUserId);
                inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
                serviceResponse = Executor.invokeService(ServiceURLEnum.CLOSUREREASON_CREATE, inputMap, null,
                        requestInstance);
                serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
                // Check operation status
                if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                        || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    // Failed operation
                    LOG.error("Failed CRUD Operation. Response:" + serviceResponse);
                    throw new ApplicationException(ErrorCodeEnum.ERR_21551);
                }
                // Return success message
                LOG.debug("Successful CRUD Operation");
            }
        }

        // Audit activity
        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LEADMANAGEMENT, EventEnum.UPDATE,
                ActivityStatusEnum.SUCCESSFUL, "Lead Closure reasons configured");

    }

    /**
     * Method to set the auto-archive period of lead
     * 
     * @param requestInstance
     * @param numberOfDays
     * @throws ApplicationException
     */
    private void setLeadAutoArchivePeriod(DataControllerRequest requestInstance, int numberOfDays)
            throws ApplicationException {

        if (numberOfDays < 1) {
            // Invalid value
            LOG.error("Invalid auto-archive period. Passed value:" + numberOfDays);
            throw new ApplicationException(ErrorCodeEnum.ERR_21510);
        }

        // Prepare Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("PropertyName", LEAD_AUTO_ARCHIVE_PERIOD_CONFIGURATION_KEY);
        inputMap.put("PropertyValue", String.valueOf(numberOfDays));

        // Execute service
        String serviceResponse = Executor.invokeService(ServiceURLEnum.SYSTEMCONFIGURATION_UPDATE, inputMap, null,
                requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);

        // Check operation status
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            // Failed operation
            LOG.error("Failed CRUD Operation. Response:" + serviceResponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_21509);
        }
        LOG.debug("Successful CRUD Operation");

        // Audit activity
        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LEADMANAGEMENT, EventEnum.UPDATE,
                ActivityStatusEnum.SUCCESSFUL, "Lead auto-archive period set. Days:" + numberOfDays);
    }

    /**
     * Method to get the lead auto-archive period and the defined lead closure reasons
     * 
     * @param requestInstance
     * @return Lead Configuration
     * @throws ApplicationException
     */
    private Result getLeadConfiguration(DataControllerRequest requestInstance) throws ApplicationException {

        Result result = new Result();

        // Fetch Lead closure reasons
        Dataset leadClosureReasonsDataset = getLeadClosureReasons(requestInstance);
        result.addDataset(leadClosureReasonsDataset);

        // Fetch auto-archive period
        int autoArchivePeriod = getLeadAutoArchivePeriod(requestInstance);
        result.addParam(new Param("autoArchivePeriodInDays", String.valueOf(autoArchivePeriod), FabricConstants.INT));

        // Return result
        return result;
    }

    /**
     * Method to get the lead auto-archive period
     * 
     * @param requestInstance
     * @return auto-archive period in days
     * @throws ApplicationException
     */
    private int getLeadAutoArchivePeriod(DataControllerRequest requestInstance) throws ApplicationException {

        // Prepare query Map
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(ODataQueryConstants.SELECT, "PropertyValue");
        queryMap.put(ODataQueryConstants.FILTER,
                "PropertyName eq '" + LEAD_AUTO_ARCHIVE_PERIOD_CONFIGURATION_KEY + "'");

        // Fetch lead auto-archive period
        String serviceResponse = Executor.invokeService(ServiceURLEnum.SYSTEMCONFIGURATION_READ, queryMap, null,
                requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);

        // Check operation status
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || !serviceResponseJSON.has("systemconfiguration")) {
            // Failed operation
            LOG.error("Failed CRUD Operation. Response:" + serviceResponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_20281);
        }

        // Parse response
        JSONArray systemConfigurations = serviceResponseJSON.optJSONArray("systemconfiguration");
        JSONObject systemConfigurationJSON = systemConfigurations.optJSONObject(0);
        if (systemConfigurationJSON == null || !systemConfigurationJSON.has("PropertyValue")
                || !NumberUtils.isParsable(systemConfigurationJSON.optString("PropertyValue"))) {
            LOG.error("Invalid lead auto-archive period configuration. Service Response:" + serviceResponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_21511);
        }

        // Return archive period value
        return systemConfigurationJSON.optInt("PropertyValue");
    }

    /**
     * Method to get the defined Lead Closure Reasons
     * 
     * @param requestInstance
     * @return dataset containing lead closure reasons
     * @throws ApplicationException
     */
    private Dataset getLeadClosureReasons(DataControllerRequest requestInstance) throws ApplicationException {

        // Execute Service
        String serviceResponse = Executor.invokeService(ServiceURLEnum.CLOSUREREASON_READ, new HashMap<>(),
                new HashMap<>(), requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);

        // Check operation status
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || !serviceResponseJSON.has("closurereason")) {
            // Failed operation
            LOG.error("Failed CRUD Operation. Response:" + serviceResponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_21552);
        }

        // Prepare lead-closure reasons response
        JSONArray leadClosureReasons = serviceResponseJSON.optJSONArray("closurereason");
        Dataset leadClosureReasonsDataset = CommonUtilities.constructDatasetFromJSONArray(leadClosureReasons);
        leadClosureReasonsDataset.setId("leadClosureReasons");
        return leadClosureReasonsDataset;
    }

}
