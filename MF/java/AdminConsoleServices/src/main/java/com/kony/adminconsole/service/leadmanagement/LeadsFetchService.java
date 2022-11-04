package com.kony.adminconsole.service.leadmanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ThreadExecutor;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.LeadHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to fetch the Leads based on the Search Criteria
 *
 * @author Aditya Mankal
 */
public class LeadsFetchService implements JavaService2 {

    /* Allowed values for Lead Type: CUSTOMER, NON_CUSTOMER */

    private static final Logger LOG = Logger.getLogger(LeadsFetchService.class);

    private static final int DEFAULT_RECORDS_PER_PAGE = 100;

    private static final String DEFAULT_SORT_ORDER = "desc";
    private static final String DEFAULT_SORT_CRITERIA = "lastmodifiedts";

    private static final String SORT_ON_NAME_KEY = "name";
    private static final List<String> RESPONSE_ATTRIBUTES = new ArrayList<>(Arrays.asList("id", "firstName",
            "middleName", "lastName", "salutation", "isCustomer", "customerId", "productId", "productType",
            "productName", "csrId", "csrFirstName", "csrMiddleName", "csrLastName", "statusId", "countryCode",
            "phoneNumber", "extension", "email", "createdby", "modifiedby", "createdts", "lastmodifiedts",
            "synctimestamp", "softdeleteflag", "assignedToFirstName", "assignedToMiddleName", "assignedToLastName"));

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) {

        try {
            // Read Inputs
            String leadId = StringUtils.trim(requestInstance.getParameter("leadId"));
            String leadType = StringUtils.trim(requestInstance.getParameter("leadType"));
            String statusIds = StringUtils.trim(requestInstance.getParameter("statusIds"));
            String customerId = StringUtils.trim(requestInstance.getParameter("customerId"));
            String assignedCSRId = StringUtils.trim(requestInstance.getParameter("assignedTo"));

            String productId = StringUtils.trim(requestInstance.getParameter("productId"));
            String productType = StringUtils.trim(requestInstance.getParameter("productType"));

            String modifiedStartDate = StringUtils.trim(requestInstance.getParameter("modifiedStartDate"));
            String modifiedEndDate = StringUtils.trim(requestInstance.getParameter("modifiedEndDate"));

            String phoneNumber = StringUtils.trim(requestInstance.getParameter("phoneNumber"));
            String emailAddress = StringUtils.trim(requestInstance.getParameter("emailAddress"));

            String pageNumber = StringUtils.trim(requestInstance.getParameter("pageNumber"));
            String recordsPerPage = StringUtils.trim(requestInstance.getParameter("recordsPerPage"));

            String sortOrder = StringUtils.trim(requestInstance.getParameter("sortOrder"));
            String sortCriteria = StringUtils.trim(requestInstance.getParameter("sortCriteria"));

            // Fetch Logged In User Info
            String loggedInUser = StringUtils.EMPTY;
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            if (userDetailsBeanInstance != null) {
                loggedInUser = userDetailsBeanInstance.getUserId();
            }

            // Fetch leads
            return fetchLeads(leadId, productId, productType, customerId, leadType, statusIds, assignedCSRId,
                    modifiedStartDate, modifiedEndDate, phoneNumber, emailAddress, pageNumber, recordsPerPage,
                    sortOrder, sortCriteria, loggedInUser, requestInstance);

        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.error("Exception in Fetching Leads. Exception:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

    /**
     * Method to fetch the leads records based on the filter criteria
     * 
     * @param leadId
     * @param productId
     * @param leadType
     * @param statusIds
     * @param assignedCSRId
     * @param modifiedOn
     * @param modifiedUpto
     * @param phoneNumber
     * @param emailAddress
     * @param pageNumber
     * @param recordsPerPage
     * @param sortOrder
     * @param sortCriteria
     * @param loggedInUser
     * @param requestInstance
     * @return operation Result
     * @throws ApplicationException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private Result fetchLeads(String leadId, String productId, String productType, String customerId, String leadType,
            String statusIds, String assignedCSRId, String modifiedOn, String modifiedUpto, String phoneNumber,
            String emailAddress, String pageNumber, String recordsPerPage, String sortOrder, String sortCriteria,
            String loggedInUser, DataControllerRequest requestInstance)
            throws ApplicationException, InterruptedException, ExecutionException {

        Result operationResult = new Result();

        try {

            // Prepare Input Map
            Date startTime = new Date();
            Map<String, String> queryMap = getQueryMap(leadId, productId, productType, customerId, leadType, statusIds,
                    assignedCSRId, modifiedOn, modifiedUpto, phoneNumber, emailAddress, sortOrder, sortCriteria,
                    pageNumber, recordsPerPage);

            // Execute service
            List<Callable<Result>> listOfCallable = Arrays.asList(

                    new Callable<Result>() {
                        @Override
                        public Result call() throws ApplicationException {

                            // Read leads Information
                            JSONObject readLeadResponseJSON = CommonUtilities.getStringAsJSONObject(
                                    Executor.invokeService(ServiceURLEnum.LEAD_SEARCH_PROC_SERVICE, queryMap, null,
                                            requestInstance));

                            if (readLeadResponseJSON == null || !readLeadResponseJSON.has(FabricConstants.OPSTATUS)
                                    || readLeadResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                                    || !readLeadResponseJSON.has("records")) {
                                LOG.error("Failed CRUD Operation");
                                throw new ApplicationException(ErrorCodeEnum.ERR_21500);
                            }
                            LOG.debug("Successful CRUD Operation");
                            JSONArray leads = readLeadResponseJSON.optJSONArray("records");

                            // Add Leads Information to Result
                            Dataset leadsDataset = CommonUtilities.constructDatasetFromJSONArray(leads);
                            leadsDataset.setId("leads");
                            operationResult.addDataset(leadsDataset);
                            return operationResult;
                        }
                    },

                    new Callable<Result>() {
                        @Override
                        public Result call() throws ApplicationException {

                            // Add Status Summary Information to Result
                            Dataset leadsStatusCountSummary = LeadHandler.getLeadsStatusCountSummary(requestInstance);
                            operationResult.addDataset(leadsStatusCountSummary);
                            return operationResult;
                        }
                    },

                    new Callable<Result>() {
                        @Override
                        public Result call() throws ApplicationException {

                            // Add count of leads assigned to logged-in user
                            int assignedCount = LeadHandler.getLeadsAssignedCount(loggedInUser, requestInstance);
                            operationResult.addParam(
                                    new Param("CSR_COUNT", String.valueOf(assignedCount), FabricConstants.INT));
                            return operationResult;
                        }
                    },

                    new Callable<Result>() {
                        @Override
                        public Result call() throws ApplicationException {

                            // Add count of leads matching the search criteria
                            int count = getCountOfMatchingLeads(productId, productType, customerId, leadType, statusIds,
                                    assignedCSRId, modifiedOn, modifiedUpto, phoneNumber, emailAddress,
                                    requestInstance);
                            operationResult
                                    .addParam(new Param("MATCH_COUNT", String.valueOf(count), FabricConstants.INT));
                            return operationResult;
                        }
                    });

            ThreadExecutor.executeAndWaitforCompletion(listOfCallable);
            Date endTime = new Date();
            double processingTime = (endTime.getTime() - startTime.getTime());
            operationResult.addParam(
                    new Param("processingTime", String.valueOf(processingTime) + "ms", FabricConstants.STRING));

        } catch (ExecutionException e) {
            if (e.getCause() instanceof ApplicationException) {
                ApplicationException ae = (ApplicationException) e.getCause();
                Result errorResult = new Result();
                LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
                ae.getErrorCodeEnum().setErrorCode(errorResult);
                return errorResult;
            } else {
                Result errorResult = new Result();
                LOG.error("Exception in Fetching Leads. Exception:", e);
                ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
                return errorResult;
            }
        }
        return operationResult;
    }

    /**
     * Method to get the count of leads matching the search criteria
     * 
     * @param productId
     * @param customerId
     * @param leadType
     * @param statusIds
     * @param assignedCSRId
     * @param modifiedStartDate
     * @param modifiedEndDate
     * @param phoneNumber
     * @param emailAddress
     * @param requestInstance
     * @return count of leads matching the search criteria
     * @throws ApplicationException
     */
    private int getCountOfMatchingLeads(String productId, String productType, String customerId, String leadType,
            String statusIds, String assignedCSRId, String modifiedStartDate, String modifiedEndDate,
            String phoneNumber, String emailAddress, DataControllerRequest requestInstance)
            throws ApplicationException {

        // Prepare Input Map
        Map<String, String> queryMap = new HashMap<>();

        queryMap.put("_productId", StringUtils.isNotBlank(productId) ? productId : StringUtils.EMPTY);
        queryMap.put("_productType", StringUtils.isNotBlank(productType) ? productType : StringUtils.EMPTY);
        queryMap.put("_customerId", StringUtils.isNotBlank(customerId) ? customerId : StringUtils.EMPTY);
        queryMap.put("_leadType", StringUtils.isNotBlank(leadType) ? leadType : StringUtils.EMPTY);
        queryMap.put("_statusIds", StringUtils.isNotBlank(statusIds) ? statusIds : StringUtils.EMPTY);
        queryMap.put("_assignedCSRID", StringUtils.isNotBlank(assignedCSRId) ? assignedCSRId : StringUtils.EMPTY);
        queryMap.put("_modifiedStartDate",
                StringUtils.isNotBlank(modifiedStartDate) ? modifiedStartDate : StringUtils.EMPTY);
        queryMap.put("_modifiedEndDate", StringUtils.isNotBlank(modifiedEndDate) ? modifiedEndDate : StringUtils.EMPTY);
        queryMap.put("_phoneNumber", StringUtils.isNotBlank(phoneNumber) ? phoneNumber : StringUtils.EMPTY);
        queryMap.put("_emailAddress", StringUtils.isNotBlank(emailAddress) ? emailAddress : StringUtils.EMPTY);
        queryMap.put("_productId", StringUtils.isNotBlank(productId) ? productId : StringUtils.EMPTY);

        // Execute service
        String serviceResponse = Executor.invokeService(ServiceURLEnum.LEAD_SEARCH_COUNT_PROC_SERVICE, queryMap, null,
                requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0 || !serviceResponseJSON.has("records")) {
            LOG.error("Failed CRUD Operation");
            throw new ApplicationException(ErrorCodeEnum.ERR_21517);
        }
        LOG.debug("Successful CRUD Operation");

        // Parse Response
        JSONArray countArray = serviceResponseJSON.optJSONArray("records");
        if (countArray.optJSONObject(0) != null
                && NumberUtils.isParsable(countArray.optJSONObject(0).optString("count"))) {
            return Integer.parseInt(countArray.optJSONObject(0).optString("count"));
        }
        throw new ApplicationException(ErrorCodeEnum.ERR_21517);
    }

    /**
     * Method to construct the Query Map to be passed to stored procedure: lead_search_proc
     * 
     * @param productId
     * @param leadType
     * @param statusIds
     * @param assignedCSRId
     * @param modifiedStartDate
     * @param modifiedEndDate
     * @param phoneNumber
     * @param emailAddress
     * @param sortOrder
     * @param sortCriteria
     * @param pageNumber
     * @param recordsPerPage
     * @return Query Map
     */
    private Map<String, String> getQueryMap(String leadId, String productId, String productType, String customerId,
            String leadType, String statusIds, String assignedCSRId, String modifiedStartDate, String modifiedEndDate,
            String phoneNumber, String emailAddress, String sortOrder, String sortCriteria, String pageNumber,
            String recordsPerPage) {

        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("_leadId", StringUtils.isNotBlank(leadId) ? leadId : StringUtils.EMPTY);
        queryMap.put("_productId", StringUtils.isNotBlank(productId) ? productId : StringUtils.EMPTY);
        queryMap.put("_productType", StringUtils.isNotBlank(productType) ? productType : StringUtils.EMPTY);
        queryMap.put("_customerId", StringUtils.isNotBlank(customerId) ? customerId : StringUtils.EMPTY);
        queryMap.put("_leadType", StringUtils.isNotBlank(leadType) ? leadType : StringUtils.EMPTY);
        queryMap.put("_statusIds", StringUtils.isNotBlank(statusIds) ? statusIds : StringUtils.EMPTY);
        queryMap.put("_assignedCSRID", StringUtils.isNotBlank(assignedCSRId) ? assignedCSRId : StringUtils.EMPTY);
        queryMap.put("_modifiedStartDate",
                StringUtils.isNotBlank(modifiedStartDate) ? modifiedStartDate : StringUtils.EMPTY);
        queryMap.put("_modifiedEndDate", StringUtils.isNotBlank(modifiedEndDate) ? modifiedEndDate : StringUtils.EMPTY);
        queryMap.put("_phoneNumber", StringUtils.isNotBlank(phoneNumber) ? phoneNumber : StringUtils.EMPTY);
        queryMap.put("_emailAddress", StringUtils.isNotBlank(emailAddress) ? emailAddress : StringUtils.EMPTY);

        sortOrder = StringUtils.equalsAnyIgnoreCase(sortOrder, "asc", "desc") ? sortOrder : DEFAULT_SORT_ORDER;
        queryMap.put("_sortOrder", sortOrder);

        // Check if the sortcriteria attribute is supported. Else, use default sort
        // criteria
        if (StringUtils.equals(sortCriteria, SORT_ON_NAME_KEY)) {
            sortCriteria = "firstName,middleName,lastName";
        } else {
            sortCriteria = RESPONSE_ATTRIBUTES.contains(sortCriteria) ? sortCriteria : DEFAULT_SORT_CRITERIA;
        }
        queryMap.put("_sortCriteria", StringUtils.isNotBlank(sortCriteria) ? sortCriteria : StringUtils.EMPTY);

        /* Set Pagination Filter */
        // Validate Records per page
        int recordsPerPageValue = DEFAULT_RECORDS_PER_PAGE;
        if (NumberUtils.isParsable(recordsPerPage) && Integer.parseInt(recordsPerPage) <= DEFAULT_RECORDS_PER_PAGE) {
            recordsPerPageValue = Integer.parseInt(recordsPerPage);
        }
        queryMap.put("_recordsPerPage", String.valueOf(recordsPerPageValue));
        // Calculate offset
        int offset = 0;
        if (NumberUtils.isParsable(pageNumber)) {
            offset = CommonUtilities.calculateOffset(Integer.parseInt(recordsPerPage), Integer.parseInt(pageNumber));
        }
        queryMap.put("_offset", String.valueOf(offset));
        return queryMap;
    }

}
