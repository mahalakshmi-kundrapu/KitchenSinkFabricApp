package com.kony.adminconsole.loans.postprocessor;

import org.apache.log4j.Logger;
import com.kony.adminconsole.loans.utils.LoansUtilitiesConstants;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;

public class GetPhoneAndSendSmsOrchPostProcessor implements DataPostProcessor2{
	private static final Logger log = Logger
			.getLogger(GetPhoneAndSendSmsOrchPostProcessor.class);
	@Override
	public Object execute(Result result, DataControllerRequest dcRequest, DataControllerResponse dcResponse) throws Exception {
		try {
			Param dbpErrCode = result.getParamByName("dbpErrCode");
			if(dbpErrCode!=null && dbpErrCode.getValue().equals(LoansUtilitiesConstants.SMS_SENT_SUCCESSFULLY)) {
				result.removeParamByName("dbpErrCode");
			}
			result.removeParamByName("Email");
			result.removeParamByName("Phone");
			result.removeParamByName("UserName");
			result.removeParamByName("MessageType");
			Param sKey = result.getParamByName("securityKey");
			if(sKey!=null) {
				Session sessionObject = dcRequest.getSession();
				sessionObject.setAttribute("securityKey", sKey.getValue());
			}	
	}
	catch(Exception e) {
		log.error("Unknown Error occured while proceeding in getPhoneAndSendSmsOrchPostProcessor");
		log.error(e.getMessage());
		result = ErrorCodeEnum.ERR_31000.updateResultObject(result);
	}
	return result;
 }
}
