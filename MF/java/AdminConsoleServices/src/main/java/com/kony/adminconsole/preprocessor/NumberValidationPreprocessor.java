package com.kony.adminconsole.preprocessor;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.kony.adminconsole.core.config.EnvironmentConfiguration;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class NumberValidationPreprocessor implements DataPreProcessor2 {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(NumberValidationPreprocessor.class);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean execute(HashMap inputParams, DataControllerRequest request, DataControllerResponse response,
            Result result) throws Exception {
        String API_KEY = EnvironmentConfiguration.AC_NUMVERIFY_API_KEY.getValue(request);
        if (StringUtils.isNotBlank(API_KEY)) {
            inputParams.put("apikey", API_KEY);
        } else {
            ErrorCodeEnum.ERR_20137.setErrorCode(result);
            return false;
        }
        return true;
    }

}
