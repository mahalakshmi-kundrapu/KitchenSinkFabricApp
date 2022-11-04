package com.kony.adminconsole.service.customermanagement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.DBPServices;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * CustomerGetAccountSpecificAlerts service will fetch account specific alerts from core banking
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerGetAccountSpecificAlerts implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(CustomerGetAccountSpecificAlerts.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        try {

            String customerUsername = requestInstance.getParameter("customerUsername");
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);

            if (StringUtils.isBlank(customerUsername)) {
                ErrorCodeEnum.ERR_20653.setErrorCode(processedResult);
                Param statusParam = new Param("Status", "Failure", FabricConstants.STRING);
                processedResult.addParam(statusParam);
                return processedResult;
            }

            // get account specific alerts
            JSONObject accountSpecificAlerts = DBPServices.getAccountSpecificAlerts(authToken, customerUsername,
                    requestInstance);
            // Validate response
            if (accountSpecificAlerts == null || !accountSpecificAlerts.has(FabricConstants.OPSTATUS)
                    || (accountSpecificAlerts.getInt(FabricConstants.OPSTATUS) != 0)) {
                ErrorCodeEnum.ERR_20656.setErrorCode(processedResult);
                Param statusParam = new Param("Status", "Failure", FabricConstants.STRING);
                processedResult.addParam(statusParam);
                return processedResult;
            }
            Dataset accountsDataset = new Dataset();
            if (accountSpecificAlerts.has("accountSpecificAlerts")) {
                accountsDataset = CommonUtilities
                        .constructDatasetFromJSONArray((JSONArray) accountSpecificAlerts.get("accountSpecificAlerts"));
            }
            accountsDataset.setId("accountSpecificAlerts");

            Param statusParam = new Param("Status", "Successful", FabricConstants.STRING);
            processedResult.addDataset(accountsDataset);
            processedResult.addParam(statusParam);
            return processedResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

}