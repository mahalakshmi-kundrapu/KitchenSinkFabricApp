package com.kony.adminconsole.service.customermanagement;

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
 * CustomerGetTransactions service will fetch customer transactions from core banking
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerGetTransactions implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(CustomerGetTransactions.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result processedResult = new Result();
        try {
            String accountNumber = requestInstance.getParameter("AccountNumber");
            String startDate = requestInstance.getParameter("StartDate");
            String endDate = requestInstance.getParameter("EndDate");

            // get Transactions
            JSONObject transactions = DBPServices.getTransactions(accountNumber, startDate, endDate, requestInstance);

            if (transactions == null || (!transactions.has("opstatus")) || transactions.getInt("opstatus") != 0) {
                ErrorCodeEnum.ERR_20655.setErrorCode(processedResult);
                Param statusParam = new Param("Status", "Failure", FabricConstants.STRING);
                processedResult.addParam(statusParam);

                return processedResult;
            }
            Dataset transactionsDataset = new Dataset();
            if (transactions.has("Transactions")) {
                transactionsDataset = CommonUtilities
                        .constructDatasetFromJSONArray((JSONArray) transactions.get("Transactions"));
            }
            transactionsDataset.setId("Transactions");

            Param statusParam = new Param("Status", "Successful", FabricConstants.STRING);
            processedResult.addDataset(transactionsDataset);
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