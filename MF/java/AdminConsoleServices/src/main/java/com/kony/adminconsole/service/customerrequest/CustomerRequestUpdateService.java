package com.kony.adminconsole.service.customerrequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * <p>
 * Service to Update Customer Requests
 * </p>
 * 
 * @author Aditya Mankal
 *
 */
public class CustomerRequestUpdateService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CustomerRequestUpdateService.class);

    private static final String CSR_ID_PARAM = "csrId";
    private static final String REQUEST_IDS_PARAM = "requestIds";

    private static final String ASSIGN_REQUESTS_METHOD_ID = "assignRequests";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) {

        try {

            Result processedResult = new Result();

            if (StringUtils.equals(methodID, ASSIGN_REQUESTS_METHOD_ID)) {
                String csrId = requestInstance.getParameter(CSR_ID_PARAM);
                LOG.debug("Is CSR ID Null?" + StringUtils.isBlank(csrId));

                String requestIdsStr = requestInstance.getParameter(REQUEST_IDS_PARAM);
                LOG.debug("Is RequestIds Null?" + StringUtils.isBlank(requestIdsStr));

                List<String> requestIds = new ArrayList<>();
                if (requestIdsStr != null) {
                    requestIds = Arrays.stream(requestIdsStr.split("\\s*,\\s*")).collect(Collectors.toList());
                    assignRequests(requestIds, csrId, requestInstance);
                }
                processedResult.addParam(
                        new Param("assignedRequests", Integer.toString(requestIds.size()), FabricConstants.INT));
            }

            return processedResult;

        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20921.setErrorCode(errorResult);
            return errorResult;
        }

    }

    /**
     * Method to assign Customer Request(s) to CSR
     * 
     * @param requestIds
     * @param csrId
     * @param requestInstance
     * @throws ApplicationException
     */
    private void assignRequests(List<String> requestIds, String csrId, DataControllerRequest requestInstance)
            throws ApplicationException {

        // Validate Inputs
        if (requestIds == null || requestIds.isEmpty()) {
            LOG.debug("Request Ids list is empty. Returning..");
            return;
        }
        if (StringUtils.isBlank(csrId)) {
            LOG.error("Customer Id not provided. Throwing Exception..");
            throw new ApplicationException(ErrorCodeEnum.ERR_20688);
        }

        // Execute Update Operation
        Map<String, String> inputMap = new HashMap<>();
        String requestIdsStr = String.join(",", requestIds);
        inputMap.put("_requestIds", requestIdsStr);
        inputMap.put("_csrID", csrId);
        String serviceResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_REQUESTS_ASSIGN_PROC_SERVICE, inputMap,
                null, requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            LOG.error("Failed Operation. Service Response:" + serviceResponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_20147);
        }
        LOG.debug("Assigned Customer Request(s) to CSR");
    }

}
