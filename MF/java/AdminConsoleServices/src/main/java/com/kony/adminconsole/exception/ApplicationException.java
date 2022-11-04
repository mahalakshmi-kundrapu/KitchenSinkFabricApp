package com.kony.adminconsole.exception;

import org.apache.commons.lang3.StringUtils;

import com.kony.adminconsole.utilities.ErrorCodeEnum;

/**
 * Exception class used to hold the error code and error message. This exception can be thrown from the calling classes
 * or methods used by java services, pre/post processors and can further used for catching it and sending the
 * appropriate error code and message to requested client.
 * 
 * @author Venkateswara Rao Alla
 *
 */

public class ApplicationException extends Exception {

    private static final long serialVersionUID = 7065601577555260335L;

    private ErrorCodeEnum errorCodeEnum;

    public ApplicationException(ErrorCodeEnum errorCodeEnum) {
        this.errorCodeEnum = errorCodeEnum;
    }

    public ApplicationException(ErrorCodeEnum errorCodeEnum, Throwable cause) {
        super(cause);
        this.errorCodeEnum = errorCodeEnum;
    }

    public ErrorCodeEnum getErrorCodeEnum() {
        return errorCodeEnum;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();

        String message = super.getMessage();
        if (StringUtils.isNotBlank(message)) {
            builder.append(message);
        }

        builder.append(" [ErrorCode=").append(errorCodeEnum.getErrorCode()).append(", ").append(" Message=")
                .append(String.valueOf(errorCodeEnum.getMessage())).append("]");

        return builder.toString();
    }

}
