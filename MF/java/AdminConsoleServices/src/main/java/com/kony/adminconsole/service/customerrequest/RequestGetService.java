package com.kony.adminconsole.service.customerrequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.commons.utils.ThreadExecutor;
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
import com.konylabs.middleware.dataobject.Result;

public class RequestGetService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(RequestGetService.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String ARRAY_KEY = "customerrequests_view";

    @Override
    public Object invoke(String methodId, Object[] arg1, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result processedResult = new Result();
        try {
            CustomerHandler customerHandler = new CustomerHandler();
            String customerUsername = requestInstance.getParameter("username");
            String softdeleteflag = requestInstance.getParameter("softdeleteflag");
            String filter = null;
            String customerId = null;

            if (StringUtils.isBlank(customerUsername)) {
                ErrorCodeEnum.ERR_20533.setErrorCode(processedResult);
                return processedResult;
            }

            customerId = customerHandler.getCustomerId(customerUsername, requestInstance);
            filter = "username eq '" + customerUsername + "'";
            if (StringUtils.isNotBlank(softdeleteflag)) {
                filter += " and softdeleteflag eq '" + softdeleteflag + "'";
            }
            filter += " and status_id ne 'Hard Deleted'";

            final String finalCustomerId = customerId;
            final String finalFilter = filter;

            List<Callable<Result>> listOfCallable = Arrays.asList(new Callable<Result>() {
                @Override
                public Result call() throws ApplicationException {

                    // Fetch customer requests
                    Map<String, String> paramMap = new HashMap<>();
                    paramMap.put(ODataQueryConstants.FILTER, finalFilter);
                    paramMap.put(ODataQueryConstants.ORDER_BY, "recentMsgDate desc");
                    String operationResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERREQUESTS_VIEW_READ,
                            paramMap, null, requestInstance);
                    JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
                    if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                            && operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                            && !operationResponseJSON.has(ARRAY_KEY)) {
                        // Failed CRUD Operation
                        throw new ApplicationException(ErrorCodeEnum.ERR_20124);
                    }
                    JSONArray readResponseJSONArray = operationResponseJSON.getJSONArray(ARRAY_KEY);
                    Dataset resultDataset = new Dataset();
                    resultDataset.setId(ARRAY_KEY);
                    for (Object requestObject : readResponseJSONArray) {
                        JSONObject requestJSONObject = (JSONObject) requestObject;

                        // Parse the database time stamp to ISO format
                        requestJSONObject.put("recentMsgDate",
                                CommonUtilities.convertTimetoISO8601Format(CommonUtilities.parseDateStringToDate(
                                        requestJSONObject.optString("recentMsgDate"), DATE_FORMAT)));
                        requestJSONObject.put("requestCreatedDate",
                                CommonUtilities.convertTimetoISO8601Format(CommonUtilities.parseDateStringToDate(
                                        requestJSONObject.optString("requestCreatedDate"), DATE_FORMAT)));

                        // Remove unused parameter
                        requestJSONObject.remove("accountid");
                        resultDataset.addRecord(CommonUtilities.constructRecordFromJSONObject(requestJSONObject));
                    }
                    processedResult.addDataset(resultDataset);
                    return processedResult;
                }
            }, new Callable<Result>() {
                @Override
                public Result call() throws ApplicationException {
                    // Fetch unread messages count

                    if (StringUtils.isNotBlank(finalCustomerId)) {
                        Result unreadMessagesResult = customerHandler.getUnreadMessageCount(requestInstance,
                                finalCustomerId);
                        processedResult.addParam(new Param("unreadMessageCount",
                                unreadMessagesResult.getParamByName("unreadMessageCount").getValue(),
                                FabricConstants.STRING));
                    }
                    return processedResult;
                }
            });

            ThreadExecutor.executeAndWaitforCompletion(listOfCallable);

        } catch (ExecutionException executionException) {
            if (executionException.getCause() instanceof ApplicationException) {
                Result errorResult = new Result();
                ApplicationException applicationException = (ApplicationException) executionException.getCause();
                applicationException.getErrorCodeEnum().setErrorCode(errorResult);
                LOG.error("Exception occurred while processing customer requests get service", applicationException);
                return errorResult;
            }
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(processedResult);
        }
        return processedResult;
    }
}
