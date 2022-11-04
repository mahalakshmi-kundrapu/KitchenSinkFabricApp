package com.kony.adminconsole.service.business;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.DBPServices;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class UnlinkAccountsService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(UnlinkAccountsService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        try {
            if (requestInstance.getParameter("Organization_id") == null) {
                ErrorCodeEnum.ERR_21011.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("AccountsList") == null) {
                ErrorCodeEnum.ERR_21008.setErrorCode(result);
                return result;
            } else {
                String Organization_id = requestInstance.getParameter("Organization_id");
                String accounts = requestInstance.getParameter("AccountsList");
                JSONObject editAccountsresponse = DBPServices.unlinkAccounts(Organization_id, accounts,
                        requestInstance);
                if (editAccountsresponse.has("errorMessage")) {
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    result.addParam(new Param("errMsg", editAccountsresponse.getString("errorMessage"),
                            FabricConstants.STRING));
                    return result;
                } else {
                    if (editAccountsresponse == null || !editAccountsresponse.has(FabricConstants.OPSTATUS)
                            || editAccountsresponse.getInt(FabricConstants.OPSTATUS) != 0) {
                        ErrorCodeEnum.ERR_21014.setErrorCode(result);
                        result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                        return result;
                    } else {
                        result.addParam(new Param("status", "Success", FabricConstants.STRING));
                        result.addParam(new Param("opstatus", editAccountsresponse.get("opstatus").toString(),
                                FabricConstants.STRING));
                        result.addParam(new Param("msg", editAccountsresponse.get("success").toString(),
                                FabricConstants.STRING));
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in edit accounts ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
        }
        return result;
    }
}