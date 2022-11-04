package com.kony.adminconsole.loans.preprocessor;


import java.util.*;
import org.apache.log4j.Logger;

import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.utils.EnvironmentConfigurationsHandler;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;


public class NumberValidationPreprocessor implements DataPreProcessor2{
	private static final Logger log = Logger
			.getLogger(NumberValidationPreprocessor.class);
	static Properties LoansConfigProperties= new Properties();
	@Override
	public boolean execute(HashMap inputParams, DataControllerRequest request, DataControllerResponse response, Result result)            throws Exception {
        String API_KEY = EnvironmentConfigurationsHandler.getValue("NUMVERIFY_API_KEY", request);
        String demoFlag = EnvironmentConfigurationsHandler.getValue("LOANS_DEMOFLAG_VALUE", request);
        if(API_KEY != null) {
            try {
            	if(demoFlag != null && demoFlag.equalsIgnoreCase("true")){
					result.addParam(new Param("valid", "true"));
					result.addParam(new Param("isMobileNumber", "true"));
					return false;
				} else{
					inputParams.put("api", API_KEY);
				}            
            }
            catch(Exception e) {
                log.error("Error in NumberValidationPreprocessor"+e.getMessage());
                result = ErrorCodeEnum.ERR_31000.updateResultObject(result);
                return false;
            }
        }
        else {
            result = ErrorCodeEnum.ERR_33403.updateResultObject(result);
            return false;
        }
        return true;
    }
	

}
