package com.kony.logservices.util;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Enum that holds error codes related to admin console services and processors
 * 
 * @author Venkateswara Rao Alla
 *
 */

public enum ErrorCodeEnum {

    ERR_20000(20000, "Unauthorized access"), ERR_20001(20001, "Internal error"),
    ERR_20002(20002, "Error in verifying the authenticity of API request"),

    ERR_29001(29001, "Invalid LogType in the request payload!"), ERR_29002(29002, "Invalid request payload!"),
    ERR_29003(29003, "Failed to Get Transaction Logs"), ERR_29004(29004, "Failed to Get Customer Activity Logs"),
    ERR_29005(29005, "Customer username cannot be empty"), ERR_29006(29006, "Failed to get Admin Console Logs"),
    ERR_29007(29007, "Date Range is Missing"), ERR_29008(29008, "sessionId cannot be empty"),
    ERR_29009(29009, "Log Retention Period has not been cofigured"),
    ERR_29010(29010, "Failed to fetch the customer audit logs");

    private int errorCode;
    private String message;
    public static final String ERROR_CODE_KEY = "dbpErrCode";
    private static final String ERROR_MESSAGE_KEY = "dbpErrMsg";

    private ErrorCodeEnum(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCodeAsString() {
        return String.valueOf(errorCode);
    }

    /**
     * Sets the {@link Result} instance with opstatus, error message from this {@link ErrorCodeEnum} enum constant
     * 
     * @param result
     */
    public Result setErrorCode(Result result) {
        if (result == null) {
            result = new Result();
        }
        result.addParam(new Param(ERROR_CODE_KEY, this.getErrorCodeAsString(), FabricConstants.INT));
        result.addParam(new Param(ERROR_MESSAGE_KEY, this.getMessage(), FabricConstants.STRING));
        return result;
    }

}
