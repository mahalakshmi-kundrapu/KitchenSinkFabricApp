package com.kony.adminconsole.service.customermanagement;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.DBPServices;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
//import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * CustomerLockStatusGet service will fetch the lock status of a customer
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerLockStatusGet implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(CustomerLockStatusGet.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            Result processedResult = new Result();
            String customerUsername = requestInstance.getParameter("customerUsername");
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);

            // Get customer status from CoreBanking.

            JSONObject readResponse = DBPServices.getlockStatus(authToken, customerUsername, requestInstance);
            if (readResponse != null && readResponse.has(FabricConstants.OPSTATUS)
                    && readResponse.getInt(FabricConstants.OPSTATUS) == 0) {
                Record statusResponse = CommonUtilities
                        .constructRecordFromJSONObject(readResponse.getJSONObject("customer"));
                statusResponse.setId("Customer");
                processedResult.addRecord(statusResponse);
            } else {
                ErrorCodeEnum.ERR_20657.setErrorCode(processedResult);
                return processedResult;
            }

            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

}