package com.kony.adminconsole.service.customermanagement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class EntitlementsGetContactInfomation implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(EntitlementsGetContactInfomation.class);
    private static final String INPUT_CUSTOMER_ID = "customerId";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        String customerId = requestInstance.getParameter(INPUT_CUSTOMER_ID);
        CustomerHandler customerHandler = new CustomerHandler();

        if (StringUtils.isBlank(customerId)) {
            ErrorCodeEnum.ERR_20688.setErrorCode(processedResult);
            return processedResult;
        }

        try {
            // Fetch contact information
            return customerHandler.readCustomerMobileAndEmailInfo(customerId, processedResult, requestInstance);

        } catch (Exception e) {
            LOG.error("Exception occurred while processing customer entitlements service ", e);
            ErrorCodeEnum.ERR_20983.setErrorCode(processedResult);
            return processedResult;
        }
    }

}
