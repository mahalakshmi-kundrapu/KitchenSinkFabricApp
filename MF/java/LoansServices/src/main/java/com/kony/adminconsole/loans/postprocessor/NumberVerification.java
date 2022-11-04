package com.kony.adminconsole.loans.postprocessor;

import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import org.apache.log4j.Logger;

public class NumberVerification implements DataPostProcessor2 {

	private static final Logger log = Logger.getLogger(NumberVerification.class);
	
	@Override
	public Object execute(Result result, DataControllerRequest dcrequest, DataControllerResponse dcresponse) throws Exception {
		Result processedResult = new Result();
		try {
			if(result.getParamByName("valid").getValue().equalsIgnoreCase("false")) {
				processedResult.addParam(new Param("valid","false"));
				processedResult.addParam(new Param("isMobileNumber","false"));
				processedResult = ErrorCodeEnum.ERR_36106.updateResultObject(processedResult);
			}
			else if(result.getParamByName("line_type").getValue().equalsIgnoreCase("mobile")) {
				processedResult.addParam(new Param("valid","true"));
				processedResult.addParam(new Param("isMobileNumber","true"));
			}
			else if(!result.getParamByName("line_type").getValue().isEmpty()) {
				processedResult.addParam(new Param("valid","true"));
				processedResult.addParam(new Param("isMobileNumber","false"));
			}
			else if(!result.getParamByName("errorcode").getValue().isEmpty()){
				String error = result.getParamByName("errorcode").getValue().toString();
				processedResult.addParam(new Param("valid","false"));
				processedResult.addParam(new Param("isMobileNumber","false"));
				switch(error) {
				case "210":
					processedResult = ErrorCodeEnum.ERR_36101.updateResultObject(processedResult);
					break;
				case "211":
					processedResult = ErrorCodeEnum.ERR_36102.updateResultObject(processedResult);
					break;
				case "310":
					processedResult = ErrorCodeEnum.ERR_36103.updateResultObject(processedResult);
					break;
				case "101":
					processedResult = ErrorCodeEnum.ERR_36104.updateResultObject(processedResult);
					break;
				case "104":
					processedResult = ErrorCodeEnum.ERR_36105.updateResultObject(processedResult);
					break;
				default:
					processedResult = ErrorCodeEnum.ERR_31000.updateResultObject(processedResult);
					break;
				}
			}
		}
		catch(Exception e) {
			log.error("Error in NumberValidationPostprocessor"+e.getMessage());
			processedResult = ErrorCodeEnum.ERR_31000.updateResultObject(processedResult);
		}
		return processedResult;
	}

}