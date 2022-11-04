package com.kony.adminconsole.loans.postprocessor;

import org.apache.log4j.Logger;

import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;

public class VerifyUserNamePostProcessor implements DataPostProcessor2 {
	private static final Logger log = Logger.getLogger(VerifyUserNamePostProcessor.class);

	@Override
	public Object execute(Result inputResult, DataControllerRequest dcRequest, DataControllerResponse dcResponse)
			throws Exception {
		log.info("Entered VerifyUserNamePostProcessor");
		try {
				Session sessionObject = dcRequest.getSession();
				Param userExists = inputResult.getParamByName("isUserNameExists");
				if(userExists!= null) {
					if(userExists.getValue().equalsIgnoreCase("false")) {
						inputResult = ErrorCodeEnum.ERR_38000.updateResultObject(inputResult);
					} else {
						sessionObject.setAttribute("isdbxVerifyCustomer","true");
					}
				} else {
					inputResult = ErrorCodeEnum.ERR_31000.updateResultObject(inputResult);
				}
		} catch (Exception e) {
			inputResult = ErrorCodeEnum.ERR_31000.updateResultObject(inputResult);
			log.error("Exception in VerifyUserNamePostProcessor", e);
		}
		return inputResult;
	}
}