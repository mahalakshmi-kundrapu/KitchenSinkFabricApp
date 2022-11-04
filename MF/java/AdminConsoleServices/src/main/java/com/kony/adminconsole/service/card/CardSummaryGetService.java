package com.kony.adminconsole.service.card;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

/**
 * 
 * Service to get the Summary of the Requests and Notifications raised by a customer
 * 
 * @author Aditya Mankal
 * 
 * 
 */
public class CardSummaryGetService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CardSummaryGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) {
        try {
            Result processedResult = new Result();
            String customerId = requestInstance.getParameter("Customer_id");
            String username = requestInstance.getParameter("customerUsername");
            CustomerHandler customerHandler = new CustomerHandler();

            if (StringUtils.isBlank(customerId)) {
                if (StringUtils.isBlank(username)) {
                    ErrorCodeEnum.ERR_20612.setErrorCode(processedResult);
                    return processedResult;
                }

                customerId = customerHandler.getCustomerId(username, requestInstance);
            }

            if (methodID.equalsIgnoreCase("getCustomerCardRequestNotificationSummary"))
                return customerHandler.getCustomerRequestNotificationCount(requestInstance, customerId,
                        processedResult);
            return null;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

}