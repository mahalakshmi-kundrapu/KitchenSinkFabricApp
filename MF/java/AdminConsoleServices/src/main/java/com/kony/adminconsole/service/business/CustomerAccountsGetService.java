package com.kony.adminconsole.service.business;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.DBPServices;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class CustomerAccountsGetService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CompanyAccountsGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        try {
            if (requestInstance.getParameter("Customer_id") == null) {
                ErrorCodeEnum.ERR_20565.setErrorCode(result);
                return result;
            } else {
                String Customer_id = requestInstance.getParameter("Customer_id");
                JSONObject getCustomerAccountsresponse = DBPServices.getCustomerAccounts(Customer_id, requestInstance);
                if (getCustomerAccountsresponse == null || !getCustomerAccountsresponse.has(FabricConstants.OPSTATUS)
                        || getCustomerAccountsresponse.getInt(FabricConstants.OPSTATUS) != 0) {
                    ErrorCodeEnum.ERR_21015.setErrorCode(result);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                } else {
                    result.addParam(new Param("status", "Success", FabricConstants.STRING));
                    result.addParam(new Param("opstatus", getCustomerAccountsresponse.get("opstatus").toString(),
                            FabricConstants.STRING));

                    // Creating Dataset and adding to result
                    JSONArray readResponseJSONArray = getCustomerAccountsresponse.getJSONArray("Accounts");
                    Dataset dataSet = new Dataset();
                    dataSet.setId("Accounts");
                    for (int indexVar = 0; indexVar < readResponseJSONArray.length(); indexVar++) {
                        JSONObject currJSONObject = readResponseJSONArray.getJSONObject(indexVar);
                        Record currRecord = new Record();
                        if (currJSONObject.length() != 0) {
                            for (String currKey : currJSONObject.keySet()) {
                                if (currJSONObject.has(currKey)) {
                                    currRecord.addParam(new Param(currKey, currJSONObject.getString(currKey),
                                            FabricConstants.STRING));
                                }
                            }
                            dataSet.addRecord(currRecord);
                        }
                    }
                    result.addDataset(dataSet);
                }
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in get Company Accounts", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
        }
        return result;
    }
}