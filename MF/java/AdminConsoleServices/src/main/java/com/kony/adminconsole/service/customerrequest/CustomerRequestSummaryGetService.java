package com.kony.adminconsole.service.customerrequest;

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
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to compute and return the summary of the Customer Requests
 *
 * @author Aditya Mankal
 * 
 */
public class CustomerRequestSummaryGetService implements JavaService2 {

    private static final String GET_UNREAD_MESSAGE_COUNT_METHOD_ID = "getUnreadMessageCount";
    private static final String GET_REQUEST_SUMMARY_COUNT_METHOD_ID = "getRequestSummaryCount";

    private static final Logger LOG = Logger.getLogger(CustomerRequestSummaryGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        try {
            if (StringUtils.equalsIgnoreCase(methodID, GET_UNREAD_MESSAGE_COUNT_METHOD_ID)) {
                String customerUsername = StringUtils.trim(requestInstance.getParameter("username"));
                CustomerHandler customerHandler = new CustomerHandler();
                String customerId = customerHandler.getCustomerId(customerUsername, requestInstance);
                return customerHandler.getUnreadMessageCount(requestInstance, customerId);
            }
            if (StringUtils.equalsIgnoreCase(methodID, GET_REQUEST_SUMMARY_COUNT_METHOD_ID)) {
                String csrId = StringUtils.trim(requestInstance.getParameter("csrID"));
                if (StringUtils.isBlank(csrId)) {
                    UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
                    csrId = userDetailsBeanInstance.getUserId(); // Considering the Logged-In User as the CSR
                }
                return getRequestSummaryCount(requestInstance, csrId);
            }
            return new Result();
        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    private Result getRequestSummaryCount(DataControllerRequest requestInstance, String csrId)
            throws ApplicationException {

        Result processedResult = new Result();
        Map<String, String> inputMap = new HashMap<>();

        // Fetch Category wise count
        inputMap.put(ODataQueryConstants.ORDER_BY, "request_count desc");
        String operationResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_REQUEST_CATEGORY_COUNT_VIEW_READ,
                inputMap, null, requestInstance);
        JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
        if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                || operationResponseJSON.optInt(FabricConstants.OPSTATUS) != 0
                || !operationResponseJSON.has("customer_request_category_count_view")) {
            LOG.error("Failed to execute CRUD operation: CUSTOMER_REQUEST_CATEGORY_COUNT_VIEW_READ. Service Response:"
                    + operationResponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_20127);
        }

        JSONObject currJSON;
        Dataset categoryWiseCountDataset = new Dataset();
        categoryWiseCountDataset.setId("categorySummary");
        processedResult.addDataset(categoryWiseCountDataset);

        JSONArray requestCategoryCountViewJSONArray = operationResponseJSON
                .getJSONArray("customer_request_category_count_view");
        for (int indexVar = 0; indexVar < requestCategoryCountViewJSONArray.length(); indexVar++) {
            Record currCategoryRecord = new Record();
            currJSON = requestCategoryCountViewJSONArray.getJSONObject(indexVar);
            for (String currKey : currJSON.keySet()) {
                currCategoryRecord.addParam(new Param(currKey, currJSON.optString(currKey), FabricConstants.STRING));
            }
            categoryWiseCountDataset.addRecord(currCategoryRecord);
        }

        // Fetch Category wise count linked to the current CSR
        inputMap.clear();
        inputMap.put(ODataQueryConstants.ORDER_BY, "request_count desc");
        inputMap.put(ODataQueryConstants.FILTER, "customerrequest_assignedTo eq '" + csrId + "'");
        operationResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_REQUEST_CSR_COUNT_VIEW_READ, inputMap, null,
                requestInstance);
        operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

        if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                || operationResponseJSON.optInt(FabricConstants.OPSTATUS) != 0
                || !operationResponseJSON.has("customer_request_csr_count_view")) {
            LOG.error("Failed to execute CRUD operation: CUSTOMER_REQUEST_CSR_COUNT_VIEW_READ. Service Response:"
                    + operationResponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_20128);
        }

        Dataset csrWiseCountDataset = new Dataset();
        csrWiseCountDataset.setId("csrSummary");
        processedResult.addDataset(csrWiseCountDataset);
        JSONArray requestCSRCountViewJSONArray = operationResponseJSON.getJSONArray("customer_request_csr_count_view");
        for (int indexVar = 0; indexVar < requestCSRCountViewJSONArray.length(); indexVar++) {
            Record currCountRecord = new Record();
            currJSON = requestCSRCountViewJSONArray.getJSONObject(indexVar);
            for (String currKey : currJSON.keySet()) {
                currCountRecord.addParam(new Param(currKey, currJSON.optString(currKey), FabricConstants.STRING));
            }
            csrWiseCountDataset.addRecord(currCountRecord);
        }

        return processedResult;
    }

}