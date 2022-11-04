package com.kony.adminconsole.service.auditlogs;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.utilities.AccountTypeEnum;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ChannelEnum;
import com.kony.adminconsole.utilities.CurrencyEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.WireTransferTypesEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class TransactionLogsMasterDataGetService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(TransactionLogsMasterDataGetService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result result = new Result();
        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("AccountTypes", AccountTypeEnum.getAllAccountTypeAliases());
            jsonObject.put("currency", CurrencyEnum.getAllCurrencyAliases());
            jsonObject.put("Status", ActivityStatusEnum.getTransactionStatusAliases());
            jsonObject.put("Type", WireTransferTypesEnum.getAllTypeAliases());
            jsonObject.put("Channel", ChannelEnum.getAllChannelAliases());

            String resultJSON = jsonObject.toString();
            JSONObject finalJSONResult = new JSONObject(resultJSON);

            result.addParam(new Param("AccountTypes", finalJSONResult.get("AccountTypes").toString()));
            result.addParam(new Param("currency", finalJSONResult.get("currency").toString()));
            result.addParam(new Param("Status", finalJSONResult.get("Status").toString()));
            result.addParam(new Param("Type", finalJSONResult.get("Type").toString()));
            result.addParam(new Param("Channel", finalJSONResult.get("Channel").toString()));

        } catch (Exception e) {
            LOG.error("Failed while executing get transaction logs", e);
            ErrorCodeEnum.ERR_20684.setErrorCode(result);
        }
        return result;
    }

}