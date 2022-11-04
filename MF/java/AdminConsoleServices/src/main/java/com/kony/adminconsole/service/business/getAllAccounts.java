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

public class getAllAccounts implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(getAllAccounts.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        try {
            String Account_id = null;
            if (requestInstance.getParameter("Account_id") != null) {
                Account_id = requestInstance.getParameter("Account_id");
            }
            String Membership_id = null;
            if (requestInstance.getParameter("Membership_id") != null) {
                Membership_id = requestInstance.getParameter("Membership_id");
            }
            String Taxid = null;
            if (requestInstance.getParameter("Taxid") != null) {
                Taxid = requestInstance.getParameter("Taxid");
            }
            JSONObject getAccountsresponse = DBPServices.getAllAccounts(Account_id, Membership_id, Taxid,
                    requestInstance);
            if (getAccountsresponse == null || !getAccountsresponse.has(FabricConstants.OPSTATUS)
                    || getAccountsresponse.getInt(FabricConstants.OPSTATUS) != 0) {
                ErrorCodeEnum.ERR_21019.setErrorCode(result);
                result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                return result;
            } else {
                result.addParam(new Param("status", "Success", FabricConstants.STRING));
                result.addParam(
                        new Param("opstatus", getAccountsresponse.get("opstatus").toString(), FabricConstants.STRING));
                // Creating Dataset and adding to result
                JSONArray readResponseJSONArray = getAccountsresponse.getJSONArray("Accounts");
                Dataset dataSet = new Dataset();
                dataSet.setId("Accounts");
                for (int indexVar = 0; indexVar < readResponseJSONArray.length(); indexVar++) {
                    JSONObject currJSONObject = readResponseJSONArray.getJSONObject(indexVar);
                    Record currRecord = new Record();
                    if (currJSONObject.length() != 0) {
                        for (String currKey : currJSONObject.keySet()) {
                            if (currJSONObject.has(currKey)) {
                                currRecord.addParam(
                                        new Param(currKey, currJSONObject.getString(currKey), FabricConstants.STRING));
                            }
                        }
                        dataSet.addRecord(currRecord);
                    }
                }
                result.addDataset(dataSet);
                // result.addParam(new Param("Accounts",
                // getAccountsresponse.get("Accounts").toString(), FabricConstants.STRING));
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in get Accounts", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
        }
        return result;
    }
}