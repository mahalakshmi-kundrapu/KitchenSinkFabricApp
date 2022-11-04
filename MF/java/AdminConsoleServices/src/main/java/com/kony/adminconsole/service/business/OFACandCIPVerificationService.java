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

public class OFACandCIPVerificationService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(OFACandCIPVerificationService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        try {
            if (requestInstance.getParameter("DateOfBirth") == null) {
                ErrorCodeEnum.ERR_20878.setErrorCode(result);
                return result;
            } else if (requestInstance.getParameter("Ssn") == null) {
                ErrorCodeEnum.ERR_21009.setErrorCode(result);
                return result;
            } else {
                String DateOfBirth = requestInstance.getParameter("DateOfBirth");
                String Ssn = requestInstance.getParameter("Ssn");
                JSONObject ofacAndCipVerifyresponse = DBPServices.ssnVerification(DateOfBirth, Ssn, requestInstance);
                if (ofacAndCipVerifyresponse == null || !ofacAndCipVerifyresponse.has(FabricConstants.OPSTATUS)
                        || ofacAndCipVerifyresponse.getInt(FabricConstants.OPSTATUS) != 0) {
                    result.addParam(new Param("status", "Failure", FabricConstants.STRING));
                    ErrorCodeEnum.ERR_20001.setErrorCode(result);
                    return result;
                } else if (ofacAndCipVerifyresponse.has(ErrorCodeEnum.ERROR_CODE_KEY)
                        && ofacAndCipVerifyresponse.getInt(ErrorCodeEnum.ERROR_CODE_KEY) == 12426) {
                    ErrorCodeEnum.ERR_20661.setErrorCode(result);
                    return result;
                } else {
                    if (ofacAndCipVerifyresponse.has("success")) {
                        result.addParam(new Param("msg", ofacAndCipVerifyresponse.get("success").toString(),
                                FabricConstants.STRING));
                    } else {
                        result.addParam(new Param("msg", ofacAndCipVerifyresponse.get("errorMessage").toString(),
                                FabricConstants.STRING));
                    }
                    result.addParam(
                            new Param("Status", ofacAndCipVerifyresponse.getString("Status"), FabricConstants.STRING));
                }
            }
        } catch (Exception e) {
            LOG.error("Unexepected Error in OFAC and CIP Verification Service", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
        }
        return result;
    }
}