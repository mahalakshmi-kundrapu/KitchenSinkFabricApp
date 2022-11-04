package com.kony.adminconsole.loans.errorhandling;

import com.google.gson.JsonObject;
import com.konylabs.middleware.api.processor.manager.FabricResponseManager;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Enum that holds error codes and messages
 * 
 * @author Logesh R
 *
 */

public enum ErrorCodeEnum {

	ERR_31000(31000, "An unknown error occurred while processing the request"),
	ERR_31001(31001, "Authentication Error - Not authorized for this request"),
	ERR_31002(31002, "Resource not found"),
	ERR_31003(31003, "Link has expired"),
	ERR_31004(31004, "Coborrower is removed by Applicant"),
	ERR_31005(31005, "Null response recieved from backend"),
	ERR_31006(31006, "Reinvite Successful"),
	ERR_31007(31007, "No Active CoBorrower found"),

	ERR_32000(32000, "Authentication Successful"),
	ERR_32001(32001, "Invalid Username/password"),
	ERR_32002(32002, "Missing Username/password"),

	ERR_33001(33001, "Attempted Operation not available for the Object"),

	ERR_33100(33100, "Object created successfully"),
	ERR_33101(33101, "Object creation failed - Missing values for mandatory param(s)"),
	ERR_33102(33102, "Object creation failed - Datatype & Value mismatch"),
	ERR_33103(33103, "Object creation failed - A record with same identifier(s) already exists."),
	ERR_33104(33104, "Object creation failed - Foreign key reference missing for one/more fields"),
	ERR_33105(33105, "Object creation failed - Value Length too long for one/more fields"),
	ERR_33106(33106, "Object creation failed - Incorrect date format for one/more fields"),

	ERR_33200(33200, "Object updated successfully"),
	ERR_33201(33201, "Object update failed - Value missing for Primary key(s)"),
	ERR_33202(33202, "Object update failed - Missing values for mandatory param(s)"),
	ERR_33203(33203, "Object update failed - Datatype & Value mismatch"),
	ERR_33204(33204, "Object update failed - A record with same identifier(s) already exists."),
	ERR_33205(33205, "Object update failed - Foreign key reference missing for one/more fields"),
	ERR_33206(33206, "Object update failed - Value Length too long for one/more fields"),
	ERR_33207(33207, "Object update failed - Incorrect date format for one/more fields"),

	ERR_33300(33300, "Object deleted successfully"),
	ERR_33301(33301, "Object delete failed - Value missing for Primary key(s)"),
	ERR_33302(33302, "Object delete failed - Referencing Child record(s) present in one/more tables"),

	ERR_33400(33400, "Object read successful"),
	ERR_33401(33401, "Object read failed - Value missing for Primary key(s)"),
	ERR_33402(33402, "Object read failed - No records found matching the input query"),
    ERR_33403(33403, "Object read failed - Required Params are missing"),
    
    ERR_33500(33500, "Submitted successfully"),
	ERR_33501(33501, "Submittion failed - Failed to submit application in LOS"),
	ERR_33502(33502, "Submittion failed - Application already submitted."),

    ERR_34001(34001, "User successfully registered in KMS"),
    ERR_34002(34002, "User registration failed in KMS"),
    ERR_34003(34003, "User details updated successfully"),
    ERR_34004(34004, "User registration updation failed in KMS"),
	ERR_34100(34100, "Email request is queued at the Email server"),
	ERR_34101(34101, "Email request failed in KMS"),
	ERR_34102(34102, "Email Template is not available"),
	ERR_34103(34103, "Improper content in the request payload"),
	ERR_34104(34104, "Invalid Email Template Key"),
	ERR_34105(34105, "Required Dynamic Keys missing for provided Email Template Key"),
	ERR_34200(34200, "SMS request is queued at the SMS server"),
	ERR_34202(34202, "SMS Template is not available"),
	ERR_34203(34203, "Improper content in the request payload"),
	ERR_34204(34204, "Invalid SMS Template Key"),
	ERR_34205(34205, "Required Dynamic Keys missing for provided SMS Template Key"),
	
	ERR_35001(35001,"OTP generated successfully"),
	ERR_35002(35002,"OTP generation failed"),
	ERR_35003(35003,"OTP validated successfully"),
	ERR_35004(35004,"OTP validation failed"),
	ERR_35005(35005,"User Registration failed"),
	ERR_35006(35006,"User Registration Successful"),
	ERR_35007(35007,"OTP Limit Reached"),
	
	ERR_35201(35201,"Service Call failure"),
	ERR_35202(35202,"No Record found with given Job-id"),
	ERR_35203(35203,"Job already completed with given Job-id"),
	
	ERR_36001(36001,"Incorrect VIN Number entered"),
	ERR_36002(36002,"Given VIN number is not supported"),
	
	ERR_36101(36101,"Provide a phone number."),
	ERR_36102(36102,"Provide a numeric phone number."),
	ERR_36103(36103,"Provided an invalid 2-letter country code."),
	ERR_36104(36104,"Provided an invalid Access Key."),
	ERR_36105(36105,"API Key exceeded the limit."),
	ERR_36106(36106,"Provided number is not a valid number"),
	
	ERR_37101(37101, "AC_APP_KEY or AC_APP_SECRET is null"),
	
	ERR_37201(37201, "Provide a valid phone number"),
	ERR_37202(37202, "Unable to receive SMS for OTP."),
	ERR_37203(37203, "We're unable to send you the SMS and Email at this point of time. We will try sending it again"),
	ERR_37204(37204, "OTP request limit is excceeded than allowed number"),
	ERR_37205(37205, " Email address you have provided is already registered"),
	ERR_37206(37206, " Verify Username returned an empty response"),
	ERR_37207(37207, " Neither the security key nor the dbp error code received from the service"),
	ERR_38000(38000,"Invalid UserName");
	
	private int dbpErrCode;
	private String dbpErrMsg;

	private ErrorCodeEnum(int dbpErrCode, String dbpErrMsg) {
		this.dbpErrCode = dbpErrCode;
		this.dbpErrMsg = dbpErrMsg;
	}

	public int getErrorCode() {
		return dbpErrCode;
	}

	public String getErrorMessage() {
		return dbpErrMsg;
	}

	public String getErrorCodeAsString() {
		return String.valueOf(dbpErrCode);
	}
	
	public void appendToErrorMessage(String stringToBeAppended) {
		this.dbpErrMsg+=". "+stringToBeAppended;
	}

	public Result constructResultObject() {
		Result result=new Result();
		return addAttributesToResultObject(result);
	}
	
	public Result updateResultObject(Result result) {
		if(result==null){
			return constructResultObject();
		}else{
			return addAttributesToResultObject(result);
		}
	}
	
	public FabricResponseManager updateResultObject(FabricResponseManager fabricResponseManager) {
			return addAttributesToResultObject(fabricResponseManager);
	}

	//Keys
	public static final String KEY_DBPERRCODE="dbpErrCode";
	public static final String KEY_DBPERRMSG="dbpErrMsg";
	private Result addAttributesToResultObject(Result result) {
		result.addParam(new Param(KEY_DBPERRCODE, this.getErrorCodeAsString()));
		result.addParam(new Param(KEY_DBPERRMSG, this.dbpErrMsg));
		return result;
	}
	
	private FabricResponseManager addAttributesToResultObject(FabricResponseManager result) {
		JsonObject responseJson = new JsonObject();
		responseJson.addProperty(KEY_DBPERRCODE,this.getErrorCodeAsString());
		responseJson.addProperty(KEY_DBPERRMSG,this.getErrorMessage());
		result.getPayloadHandler().updatePayloadAsJson(responseJson);
		return result;
	}
	
	/*
	 * Second variant of construct and update methods
	 * Usage : when some custom Error Message is also To Be Appended to the pre-defined one.
	 */
	
	public Result constructResultObject(String customErrorMessageToBeAppended) {
		Result result=new Result();
		return addAttributesToResultObject(result, customErrorMessageToBeAppended);
	}
	
	public Result updateResultObject(Result result, String customErrorMessageToBeAppended) {
		if(result==null){
			return constructResultObject(customErrorMessageToBeAppended);
		}else{
			return addAttributesToResultObject(result, customErrorMessageToBeAppended);
		}
	}

	private Result addAttributesToResultObject(Result result, String customErrorMessageToBeAppended) {
		result.addParam(new Param(KEY_DBPERRCODE, this.getErrorCodeAsString()));
		result.addParam(new Param(KEY_DBPERRMSG, this.dbpErrMsg+". "+customErrorMessageToBeAppended));
		return result;
	}
}