package com.kony.adminconsole.loans.errorhandling;

import org.apache.log4j.Logger;

import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * LoansException to form the error message with error code and message
 *         
 * @author Logesh.R 
 */
public class LoansException extends Exception {

	private static final long serialVersionUID=1L;
	private String dbpErrCode;
	private String dbpErrMsg;
	private static final Logger LOGGER = Logger.getLogger(LoansException.class);

	public LoansException(ErrorCodeEnum errorCodeEnum) {
		if(errorCodeEnum==null){
			//if null -> return the LoanException object with common unknown error scenario
			LOGGER.error("Invalid input errorCodeEnum: null");
			initializeLoansException(ErrorCodeEnum.ERR_31000);
		}else{
			initializeLoansException(errorCodeEnum);
		}
	}
	
	private void initializeLoansException(ErrorCodeEnum errorCodeEnum) {
		this.dbpErrCode=errorCodeEnum.getErrorCodeAsString();
		this.dbpErrMsg=errorCodeEnum.getErrorMessage();
	}
	
	public String getErrorCode() {
		return this.dbpErrCode;
	}

	public String getErrorMessage() {
		return this.dbpErrMsg;
	}
	
	public void appendToErrorMessage(String stringToBeAppended) {
		this.dbpErrMsg+=". "+stringToBeAppended;
	}
	
	//Keys
	public static final String KEY_DBPERRCODE="dbpErrCode";
	public static final String KEY_DBPERRMSG="dbpErrMsg";
	public Result constructResultObject() {
		Result result=new Result();
		result.addParam(new Param(KEY_DBPERRCODE, this.dbpErrCode));
		result.addParam(new Param(KEY_DBPERRMSG, this.dbpErrMsg));
		return result;
	}
	
	public Result updateResultObject(Result result) {
		result.addParam(new Param(KEY_DBPERRCODE, this.dbpErrCode));
		result.addParam(new Param(KEY_DBPERRMSG, this.dbpErrMsg));
		return result;
	}
}