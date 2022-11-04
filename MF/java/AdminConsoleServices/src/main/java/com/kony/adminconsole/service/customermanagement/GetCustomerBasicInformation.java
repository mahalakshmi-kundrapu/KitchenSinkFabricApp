package com.kony.adminconsole.service.customermanagement;

import org.apache.log4j.Logger;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.exception.DBPAuthenticationException;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * This service will fetch basic information of a customer
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class GetCustomerBasicInformation implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(GetCustomerBasicInformation.class);
    public static final int DEFAULT_UNLOCK_COUNT = 4;

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        String customerId = requestInstance.getParameter("Customer_id");
        String username = requestInstance.getParameter("Customer_username");
        try {
            CustomerHandler customerHandler = new CustomerHandler();
            customerHandler.computeCustomerBasicInformation(requestInstance, processedResult, customerId, username);

        } catch (DBPAuthenticationException dbpException) {
            LOG.error("DBP login failed. " + dbpException.getMessage());
            ErrorCodeEnum.ERR_20933.setErrorCode(processedResult);
            processedResult.addParam(new Param("FailureReason", dbpException.getMessage(), FabricConstants.STRING));
        } catch (Exception e) {
            LOG.error("Unexpected error has occurred. " + e.getMessage());
            ErrorCodeEnum.ERR_20717.setErrorCode(processedResult);
            processedResult.addParam(new Param("FailureReason", e.getMessage(), FabricConstants.STRING));
        }
        return processedResult;

    }

}