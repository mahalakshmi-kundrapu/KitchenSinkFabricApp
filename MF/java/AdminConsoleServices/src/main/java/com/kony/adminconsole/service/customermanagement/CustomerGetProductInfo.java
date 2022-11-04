package com.kony.adminconsole.service.customermanagement;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.DBPServices;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * CustomerGetProductInfo service will fetch customer products from core banking
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerGetProductInfo implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(CustomerGetProductInfo.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        try {

            String customerUsername = requestInstance.getParameter("CustomerUsername");

            // get accounts
            JSONObject accounts = DBPServices.getAccounts(customerUsername, requestInstance);

            // Validate response
            if (accounts == null || !accounts.has(FabricConstants.OPSTATUS)
                    || (accounts.getInt(FabricConstants.OPSTATUS) != 0)) {
                processedResult.addParam(new Param("Status", "failure", FabricConstants.STRING));
                ErrorCodeEnum.ERR_20656.setErrorCode(processedResult);
                return processedResult;
            }

            // Get the access control for this customer for current logged-in internal user
            Boolean isCustomerAccessiable = CustomerHandler.doesCurrentLoggedinUserHasAccessToGivenCustomer(
                    customerUsername, null, requestInstance, processedResult);

            Dataset accountsDataset = new Dataset();
            if (accounts.has("Accounts")) {
                JSONArray accountsArray = accounts.getJSONArray("Accounts");
                for (Object accountObject : accountsArray) {
                    JSONObject accountJSONObject = (JSONObject) accountObject;
                    if (!isCustomerAccessiable) {
                        accountJSONObject.put("availableBalance", "XXXXX");
                        accountJSONObject.put("currentBalance", "XXXXX");
                    }
                    Record accountsRecord = CommonUtilities.constructRecordFromJSONObject(accountJSONObject);
                    accountsDataset.addRecord(accountsRecord);
                }
            }
            accountsDataset.setId("Accounts");

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