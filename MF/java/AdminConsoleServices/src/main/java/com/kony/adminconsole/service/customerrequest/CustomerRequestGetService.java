package com.kony.adminconsole.service.customerrequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
 * Service to retrieve the Customer Requests as per the search criteria
 *
 * @author Aditya Mankal
 * 
 */
public class CustomerRequestGetService implements JavaService2 {

    private static final String DRAFT_KEY = "DRAFT";
    private static final String MY_QUEUE_KEY = "MY_QUEUE";
    private static final String FILTERED_REQUESTS_KEY = "FILTERED_REQUESTS";

    private static final String ARCHIVED_REQUEST_PROC_NAME_PREFIX = "archived";
    private static final String ARCHIVED_REQUEST_STATUS_ID = StatusEnum.SID_ARCHIVED.name();
    private static final String OPEN_REQUEST_STATUS_ID = StatusEnum.SID_OPEN.name();
    private static final String INPROGRESS_REQUEST_STATUS_ID = StatusEnum.SID_INPROGRESS.name();
    private static final String[] REQUEST_PARAMS_LIST = new String[] { "dateInitialPoint", "dateFinalPoint",
            "requestStatusID", "requestCategory", "sortCriteria", "sortOrder", "requestAssignedTo", "searchKey",
            "messageRepliedBy", "recordsPerPage" };

    private static final Logger LOG = Logger.getLogger(CustomerRequestGetService.class);

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        try {

            Result processedResult = new Result();

            // Read Inputs and set Input Map
            String requestStatusId = requestInstance.getParameter("requestStatusID");
            String sortCriteria = requestInstance.getParameter("sortCriteria");
            String sortOrder = requestInstance.getParameter("sortOrder");
            Map<String, String> inputMap = (HashMap<String, String>) Arrays.stream(REQUEST_PARAMS_LIST)
                    .collect(Collectors.toMap("_"::concat, param -> {
                        String paramValue = Objects.toString(requestInstance.getParameter(param), "");
                        if ("requestStatusID".equals(param)) {
                            return handleRequestStatus(paramValue);
                        }
                        return paramValue;
                    }));
            inputMap.put("_offset", calculateOffset(requestInstance));

            // Fetch CSR Queue Count
            Record requestsSummaryRecord = getRequestsSummaryRecord(requestInstance);// Requests Summary
            Param myQueueParam = getMyQueueCount(requestInstance, requestInstance.getParameter("csrRepID"));
            requestsSummaryRecord.addParam(myQueueParam);
            requestsSummaryRecord.addParam(getRequestCount(inputMap, requestInstance));
            processedResult.addRecord(requestsSummaryRecord);

            // Fetch Customer Request Records
            String operationResponse, currRequestId;
            inputMap.put("_queryType", StringUtils.EMPTY);
            String fieldPrefixString = StringUtils.EMPTY;
            if (StringUtils.contains(requestStatusId, ARCHIVED_REQUEST_STATUS_ID)) {
                fieldPrefixString = ARCHIVED_REQUEST_PROC_NAME_PREFIX;
                operationResponse = Executor.invokeService(ServiceURLEnum.ARCHIVED_REQUEST_SEARCH_PROC_SERVICE,
                        inputMap, null, requestInstance);
            } else {
                operationResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_REQUEST_SEARCH_PROC_SERVICE,
                        inputMap, null, requestInstance);
            }
            JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
            if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                    || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                    || !operationResponseJSON.has("records")) {
                throw new ApplicationException(ErrorCodeEnum.ERR_20124);
            }
            JSONArray customerRequestRecordsArray = operationResponseJSON.getJSONArray("records");

            // Store Request Id's which meet search criteria into a collection
            Set<String> targetRequests = new LinkedHashSet<>();
            JSONObject currRequestJSONObject;
            for (int indexVar = 0; indexVar < customerRequestRecordsArray.length(); indexVar++) {
                if (customerRequestRecordsArray.get(indexVar) instanceof JSONObject) {
                    currRequestJSONObject = (JSONObject) customerRequestRecordsArray.get(indexVar);
                    currRequestId = currRequestJSONObject.optString("customerrequest_id");
                    targetRequests.add(currRequestId);
                }
            }
            LOG.debug("Matched Requests Count:" + targetRequests.size());

            // Prepare response object
            Record resultRecord = new Record();
            resultRecord.setId("records");

            if (!targetRequests.isEmpty()) {
                LOG.debug("Fetching Customer Request Data of requests that match the search criteria");

                // Construct Input Map
                inputMap.clear();
                StringBuilder filterQueryBuffer = new StringBuilder();
                for (String currRequestIdVal : targetRequests) {
                    filterQueryBuffer.append("customerrequest_id eq '").append(currRequestIdVal).append("' or ");
                }
                filterQueryBuffer.trimToSize();
                String filterQuery = CommonUtilities.replaceLastOccuranceOfString(filterQueryBuffer.toString(), " or ",
                        "");
                inputMap.put(ODataQueryConstants.FILTER, StringUtils.trim(filterQuery));
                if (!StringUtils.equals(sortOrder, "asc") && !StringUtils.equals(sortOrder, "desc"))
                    sortOrder = "desc";// Default Sort order

                if (StringUtils.isBlank(sortCriteria)) {
                    sortCriteria = "customerrequest_lastmodifiedts";// Default Sort criteria
                }
                inputMap.put(ODataQueryConstants.ORDER_BY, sortCriteria + " " + sortOrder);

                // Fetch Customer Request Data
                if (StringUtils.contains(requestStatusId, ARCHIVED_REQUEST_STATUS_ID)) {
                    operationResponse = Executor.invokeService(
                            ServiceURLEnum.ARCHIVEDCUSTOMER_REQUEST_DETAILED_VIEW_READ, inputMap, null,
                            requestInstance);
                } else {
                    operationResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_REQUEST_DETAILED_VIEW_READ,
                            inputMap, null, requestInstance);
                }
                operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
                if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                        || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                        || !operationResponseJSON.has(fieldPrefixString + "customer_request_detailed_view")) {
                    LOG.error("Failed to Fetch Customer Requests Data");
                    throw new ApplicationException(ErrorCodeEnum.ERR_20124);
                }

                // Construct Response Structure
                LOG.debug("Fetched Customer Requests Data");
                ArrayList<String> listOfInitialisedRequests = new ArrayList<>();
                customerRequestRecordsArray = operationResponseJSON
                        .getJSONArray(fieldPrefixString + "customer_request_detailed_view");
                for (int indexVar = 0; indexVar < customerRequestRecordsArray.length(); indexVar++) {
                    if (!(customerRequestRecordsArray.get(indexVar) instanceof JSONObject)) {
                        continue;
                    }

                    Record currRequestRecord;
                    currRequestJSONObject = (JSONObject) customerRequestRecordsArray.get(indexVar);
                    currRequestId = currRequestJSONObject.getString("customerrequest_id");

                    if (listOfInitialisedRequests.contains(currRequestId)) {
                        // Check if the current object indicates a draft message, and update the request
                        // record accordingly
                        currRequestRecord = resultRecord.getRecordById(currRequestId);
                        if (StringUtils.equalsIgnoreCase(currRequestJSONObject.optString("requestmessage_IsRead"),
                                DRAFT_KEY)) {
                            Param currRequestDraftStatusParam;
                            if (currRequestRecord.getParamByName("customerrequest_hasDraftMessage") != null) {
                                currRequestDraftStatusParam = currRequestRecord
                                        .getParamByName("customerrequest_hasDraftMessage");
                                currRequestDraftStatusParam.setValue(String.valueOf(1));
                            } else {
                                currRequestDraftStatusParam = new Param("customerrequest_hasDraftMessage",
                                        String.valueOf(1), FabricConstants.INT);
                                currRequestRecord.addParam(currRequestDraftStatusParam);
                            }
                        }
                    } else {
                        listOfInitialisedRequests.add(currRequestId);
                        currRequestRecord = new Record();
                        currRequestRecord.setId(currRequestId);

                        Param currRequestEntryItemParam;
                        for (String currKey : currRequestJSONObject.keySet()) {
                            currRequestEntryItemParam = new Param(currKey, currRequestJSONObject.optString(currKey),
                                    FabricConstants.STRING);
                            currRequestRecord.addParam(currRequestEntryItemParam);
                        }
                        currRequestRecord.removeParamByName("requestmessage_IsRead");

                        // Check if the current request has a Draft message
                        Param currRequestDraftStatusParam = new Param("customerrequest_hasDraftMessage",
                                String.valueOf(0), FabricConstants.INT);
                        if (StringUtils.equalsIgnoreCase(currRequestJSONObject.optString("requestmessage_IsRead"),
                                DRAFT_KEY)) {
                            currRequestDraftStatusParam.setValue(String.valueOf(1));
                        }
                        currRequestRecord.addParam(currRequestDraftStatusParam);
                        resultRecord.addRecord(currRequestRecord);
                    }
                }
            }
            LOG.debug("Returning Success Response");

            Dataset resultDataset = new Dataset();
            resultDataset.setId("records");
            resultDataset.addAllRecords(resultRecord.getAllRecords());
            processedResult.addDataset(resultDataset);
            return processedResult;

        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception. Exception Trace:", e);
            ErrorCodeEnum.ERR_20135.setErrorCode(errorResult);
            return errorResult;
        }

    }

    private Record getRequestsSummaryRecord(DataControllerRequest requestInstance) throws ApplicationException {

        LOG.debug("Fetching Request Summary Count");
        Map<String, String> inputMap = new HashMap<>();
        String operationResponse, currStatusId, currCount;
        JSONObject operationResponseJSON, currRecordJSONObject;

        operationResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_REQUEST_STATUS_COUNT_VIEW_READ, inputMap,
                null, requestInstance);
        operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
        if (operationResponseJSON != null && operationResponseJSON.has(FabricConstants.OPSTATUS)
                && operationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && operationResponseJSON.has("customer_request_status_count_view")) {

            Record requestsSummaryRecord = new Record();
            requestsSummaryRecord.setId("requestsSummary");

            JSONArray customerRequestRecordsArray = operationResponseJSON
                    .getJSONArray("customer_request_status_count_view");
            for (int indexVar = 0; indexVar < customerRequestRecordsArray.length(); indexVar++) {
                currRecordJSONObject = customerRequestRecordsArray.getJSONObject(indexVar);
                if (currRecordJSONObject.has("Count") && currRecordJSONObject.has("Status_id")) {
                    currStatusId = currRecordJSONObject.optString("Status_id");
                    currCount = currRecordJSONObject.optString("Count");
                    Param requestStatusParam = new Param(currStatusId, currCount, FabricConstants.INT);
                    requestsSummaryRecord.addParam(requestStatusParam);
                }
            }
            LOG.debug("Fetched Request Summary Count");
            return requestsSummaryRecord;
        }
        LOG.error("Failed to Fetch Request Summary Count. Response" + operationResponse);
        throw new ApplicationException(ErrorCodeEnum.ERR_20129);
    }

    private Param getMyQueueCount(DataControllerRequest requestInstance, String csrRepId) throws ApplicationException {

        LOG.debug("Fetching My Queue Count");
        UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
        if (StringUtils.isBlank(csrRepId)) {
            LOG.debug("Considering the logged-in CSR as the CSR ID to fetch My-Queue Count");
            csrRepId = userDetailsBeanInstance.getUserId();// Taking the logged in CSR as the default CSR Id
        }

        Map<String, String> inputMap = new HashMap<>();
        inputMap.put(ODataQueryConstants.FILTER, "(AssignedTo eq '" + csrRepId + "') and (Status_id eq '"
                + OPEN_REQUEST_STATUS_ID + "' or Status_id eq '" + INPROGRESS_REQUEST_STATUS_ID + "')");
        inputMap.put(ODataQueryConstants.SELECT, "id");
        String operationResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERREQUEST_READ, inputMap, null,
                requestInstance);
        JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

        if (operationResponseJSON != null && operationResponseJSON.has(FabricConstants.OPSTATUS)
                && operationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && operationResponseJSON.has("customerrequest")) {

            JSONArray customerRequestRecordsArray = operationResponseJSON.optJSONArray("customerrequest");
            LOG.debug("Fetched My Queue Count");
            if (customerRequestRecordsArray != null) {
                return new Param(MY_QUEUE_KEY, Integer.toString(customerRequestRecordsArray.length()),
                        FabricConstants.INT);
            } else {
                return new Param(MY_QUEUE_KEY, String.valueOf(0), FabricConstants.INT);
            }
        }
        LOG.error("Failed to Fetch My Queue Count. Response" + operationResponse);
        throw new ApplicationException(ErrorCodeEnum.ERR_20134);
    }

    private Param getRequestCount(Map<String, String> inputMap, DataControllerRequest requestInstance)
            throws ApplicationException {

        LOG.debug("Fetching Request Count");
        String requestStatusId = requestInstance.getParameter("requestStatusID");

        String operationResponse;
        inputMap.put("_queryType", "count");
        if (requestStatusId != null && requestStatusId.contains(ARCHIVED_REQUEST_STATUS_ID)) {
            operationResponse = Executor.invokeService(ServiceURLEnum.ARCHIVED_REQUEST_SEARCH_PROC_SERVICE, inputMap,
                    null, requestInstance);
        } else {
            operationResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_REQUEST_SEARCH_PROC_SERVICE, inputMap,
                    null, requestInstance);
        }
        JSONObject customerRequestCountJson = CommonUtilities.getStringAsJSONObject(operationResponse);
        if (customerRequestCountJson != null && customerRequestCountJson.has(FabricConstants.OPSTATUS)
                && customerRequestCountJson.getInt(FabricConstants.OPSTATUS) == 0
                && customerRequestCountJson.has("records")) {
            JSONArray countRecordsArray = customerRequestCountJson.getJSONArray("records");
            LOG.debug("Fetched Request Count");
            if (countRecordsArray.length() != 0) {
                String filterCount = countRecordsArray.getJSONObject(0).optString("cnt");
                LOG.debug("Filter Count" + filterCount);
                return new Param(FILTERED_REQUESTS_KEY, filterCount, FabricConstants.INT);
            }
        }
        LOG.error("Failed to Fetch Request Count. Response" + operationResponse);
        throw new ApplicationException(ErrorCodeEnum.ERR_20127);
    }

    private String handleRequestStatus(String statusId) {
        JSONArray requestStatusArray = CommonUtilities.getStringAsJSONArray(statusId);
        if (requestStatusArray == null) {
            // Indicates the search is on a Single Request Status and it has not been passed
            // as an Array
            return statusId;
        } else if (requestStatusArray.length() == 1) {
            return requestStatusArray.optString(0);
        } else {
            StringBuilder statusIds = new StringBuilder(requestStatusArray.optString(0));
            for (int indexVar = 1; indexVar < requestStatusArray.length(); indexVar++) {
                statusIds.append(",").append(requestStatusArray.optString(indexVar));
            }
            return statusIds.toString();
        }
    }

    private String calculateOffset(DataControllerRequest requestInstance) {
        LOG.debug("Calculating offset");
        String recordsPerPage = requestInstance.getParameter("recordsPerPage");
        String currentPage = Objects.toString(requestInstance.getParameter("currPageIndex"), "");
        try {
            int pageIndex = Integer.parseInt(currentPage);
            int offset = Integer.parseInt(recordsPerPage) * (pageIndex - 1);
            return String.valueOf(offset);
        } catch (NumberFormatException ex) {
            LOG.error("Exception In Calculating Offset. Exception:", ex);
            return String.valueOf(0);
        }
    }

}