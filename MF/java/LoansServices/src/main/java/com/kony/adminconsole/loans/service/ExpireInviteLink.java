package com.kony.adminconsole.loans.service;

import java.util.HashMap;
import java.util.Map;

import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.errorhandling.LoansException;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

@SuppressWarnings({ "rawtypes" })
public class ExpireInviteLink implements JavaService2{

	@SuppressWarnings({ "unused", "unchecked" })
	@Override
	public Object invoke(String arg0, Object[] arg1, DataControllerRequest dcRequest, DataControllerResponse dcResponse)
			throws Exception {
		Result res = new Result();
		Map inputmap = (Map)arg1[1];
		String id = inputmap.get("id").toString();
		Map<String, String> inputParams = new HashMap <String, String>();
		inputParams.put("id", id);
		inputParams.put("InvitationLinkStatus", "Link Expired");
		LoanApplicationQueryResponseCreate createQueryResponseNew = new LoanApplicationQueryResponseCreate();
		Result updatedResult = createQueryResponseNew.hitQueryCoborrowerUpdateService(inputParams, dcRequest);
		if(updatedResult.getParamByName("errmsg") != null){
			updatedResult = ErrorCodeEnum.ERR_33201.updateResultObject(updatedResult);
		}
		return updatedResult;
	}
}
