package com.kony.adminconsole.service.customermanagement;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Result;

/**
 * fetch all the entitlements assigned to the customer - directly or through groups and also their respective limits and
 * fees
 * 
 * @author Sowmya Mortha, Alahari Akhil
 *
 */
public class CustomerEntitlementsLimitsAndFees {

    private static final Logger logger = Logger.getLogger(CustomerEntitlementsLimitsAndFees.class);

    public Result fetchCustomerEntitlements(String customerId, String systemUser, String authToken,
            DataControllerRequest requestInstance) {
        Result processedResult = new Result();
        try {
            JSONArray customerEntitlements = CustomerHandler.getCustomerEntitlements(customerId, requestInstance,
                    processedResult);
            if (customerEntitlements == null) {
                return processedResult;
            }
            Dataset customerEntitlementsDataset = CommonUtilities.constructDatasetFromJSONArray(customerEntitlements);
            customerEntitlementsDataset.setId("services");

            processedResult.addDataset(customerEntitlementsDataset);
        } catch (Exception e) {
            logger.error("Error in fetching services entitled for customers", e);
            ErrorCodeEnum.ERR_20983.setErrorCode(processedResult);
        }
        return processedResult;
    }

}