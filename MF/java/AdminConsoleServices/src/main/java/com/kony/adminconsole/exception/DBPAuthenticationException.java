package com.kony.adminconsole.exception;

import com.kony.adminconsole.utilities.ErrorCodeEnum;

/**
 * 
 * Exception class used for DBP Authentication Exceptions
 * 
 * @author Aditya Mankal
 * 
 */

public class DBPAuthenticationException extends ApplicationException {

    private static final long serialVersionUID = 8378581456282862036L;

    public DBPAuthenticationException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum);
    }

    public DBPAuthenticationException(ErrorCodeEnum errorCodeEnum, Throwable cause) {
        super(errorCodeEnum, cause);
    }

}
