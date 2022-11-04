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

public class VerifyUsernameService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(VerifyUsernameService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        try {
            if (requestInstance.getParameter("UserName") == null) {
                ErrorCodeEnum.ERR_20536.setErrorCode(result);
                return result;
            } else {
                JSONObject verifyUsernameresponse = DBPServices.verifyUsername(requestInstance.getParameter("UserName"),
                        "DBX", requestInstance);
                if (verifyUsernameresponse == null || !verifyUsernameresponse.has(FabricConstants.OPSTATUS)
                        || verifyUsernameresponse.getInt(FabricConstants.OPSTATUS) != 0) {
                    ErrorCodeEnum.ERR_20538.setErrorCode(result);
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    return result;
                } else {
                    result.addParam(new Param("status", "Success", FabricConstants.STRING));
                    result.addParam(new Param("opstatus", verifyUsernameresponse.get("opstatus").toString(),
                            FabricConstants.STRING));
                    result.addParam(new Param("msg", verifyUsernameresponse.get("isUserNameExists").toString(),
                            FabricConstants.STRING));
                }
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in validate Username", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
        }
        return result;
    }
}