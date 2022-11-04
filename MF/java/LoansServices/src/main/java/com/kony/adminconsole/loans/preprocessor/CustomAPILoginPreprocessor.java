package com.kony.adminconsole.loans.preprocessor;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.utils.EnvironmentConfigurationsHandler;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

/**
 * @author KH2298
 *
 */

public class CustomAPILoginPreprocessor implements DataPreProcessor2 {

	private static final Logger log = Logger.getLogger(CustomAPILoginPreprocessor.class);
	private static final String AC_APP_KEY = "AC_APP_KEY";
	private static final String AC_APP_SECRET = "AC_APP_SECRET";
	private static final String X_KONY_APP_KEY = "X-Kony-App-Key";
	private static final String X_KONY_APP_SECRET = "X-Kony-App-Secret";
	
	public CustomAPILoginPreprocessor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean execute(HashMap arg0, DataControllerRequest request, DataControllerResponse response, Result result)
			throws Exception {
		// TODO Auto-generated method stub
		try {
			log.info("[CustomAPILoginPreprocessor] Entered PreProcessor");
			String appKey = EnvironmentConfigurationsHandler.getValue(AC_APP_KEY, request);
			String appSecret = EnvironmentConfigurationsHandler.getValue(AC_APP_SECRET, request);
			log.info("[CustomAPILoginPreprocessor] AppKey: "+appKey+" AppSecret: "+appSecret);
			if(appKey != "" && appKey != null && appSecret != "" && appSecret != null) {
				request.getSession().setAttribute(X_KONY_APP_KEY, appKey);
				request.getSession().setAttribute(X_KONY_APP_SECRET,appSecret);
				return true;
			} else {
				result = ErrorCodeEnum.ERR_37101.updateResultObject(result);
				return false;
			}
		} catch(Exception e) {
			log.error("[CustomAPILoginPreprocessor] Error occured in PreProcessor", e);
			result = ErrorCodeEnum.ERR_31001.updateResultObject(result);
			return false;
		}
	}

}
