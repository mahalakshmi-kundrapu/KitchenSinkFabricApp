package com.kony.adminconsole.service.business;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.DBPServices;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Class to validate a TIN Number
 * 
 * @author Tejaswi Manchineela
 */
public class ValidateTINService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(ValidateTINService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        try {
            Result result = new Result();

            // Read Inputs
            String tinNumber = requestInstance.getParameter("Tin");

            // Validate Inputs
            if (StringUtils.isBlank(tinNumber)) {
                ErrorCodeEnum.ERR_21021.setErrorCode(result);
                return result;
            }

            // Validate TIN Number
            boolean isExistingTin = isExistingTin(tinNumber, requestInstance);
            LOG.debug("Validation successful");

            // Construct Result
            result.addParam(new Param("isTINExists", String.valueOf(isExistingTin), FabricConstants.STRING));
            result.addParam(new Param("status", "success", FabricConstants.STRING));

            // Return result
            return result;
        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception in Validate TIN", e);
            errorResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.error("Unexepected Error in Validate TIN", e);
            errorResult.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    private boolean isExistingTin(String tinNumber, DataControllerRequest requestInstance) throws ApplicationException {
        // Validate TIN Number
        JSONObject validateTINresponse = DBPServices.validateTIN(tinNumber, requestInstance);
        if (validateTINresponse == null || !validateTINresponse.has(FabricConstants.OPSTATUS)
                || validateTINresponse.getInt(FabricConstants.OPSTATUS) != 0) {
            LOG.error("Failure at DBP level. Response" + validateTINresponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_21022);
        }
        LOG.debug("Validation successful");
        return StringUtils.equalsIgnoreCase(validateTINresponse.optString("isExists"), String.valueOf(true));
    }

}