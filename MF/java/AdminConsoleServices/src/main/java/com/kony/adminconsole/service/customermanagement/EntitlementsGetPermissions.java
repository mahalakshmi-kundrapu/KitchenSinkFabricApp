package com.kony.adminconsole.service.customermanagement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Result;

public class EntitlementsGetPermissions implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(EntitlementsGetPermissions.class);
    private static final String INPUT_CUSTOMER_ID = "customerId";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        String customerId = requestInstance.getParameter(INPUT_CUSTOMER_ID);

        if (StringUtils.isBlank(customerId)) {
            ErrorCodeEnum.ERR_20688.setErrorCode(processedResult);
            return processedResult;
        }

        try {
            // Fetch customer entitlements
            JSONArray customerEntitlements = CustomerHandler.getCustomerEntitlements(customerId, requestInstance,
                    processedResult);
            if (customerEntitlements == null) {
                throw new ApplicationException(ErrorCodeEnum.ERR_20983);
            }
            Dataset customerEntitlementsDataset = CommonUtilities.constructDatasetFromJSONArray(customerEntitlements);
            customerEntitlementsDataset.setId("services");
            processedResult.addDataset(customerEntitlementsDataset);
            return processedResult;

        } catch (ApplicationException applicationException) {
            applicationException.getErrorCodeEnum().setErrorCode(processedResult);
            LOG.error("Exception occurred while processing customer entitlements service ", applicationException);
        } catch (Exception e) {
            LOG.error("Exception occurred while processing customer entitlements service ", e);
            ErrorCodeEnum.ERR_20983.setErrorCode(processedResult);
        }
        return processedResult;
    }

}
