package com.kony.adminconsole.utilities;

public enum AuthenticationResults {

    INVALID_CREDENTIALS(-1, "Invalid Credentials"),

    ACCOUNT_SUSPENDED(-2, "Account Suspended"),

    UNRECOGNIZED_ACCOUNT(-3, "Unrecognised Account"),

    INVALID_FILTER(-4, "Invalid Filter"),

    PASSWORD_EXPIRED(-5, "Password Validity Expired"),

    ROLE_INACTIVE(-6, "Inactive Role"),

    ACCOUNT_INACTIVE(-7, "Inactive Account"),

    APPLICATION_EXCEPTION(-9, "Application Exception"),

    UNEXPECTED_EXCEPTION(-10, "Unexpected Exception"),

    AUTHENTICATION_SUCCESSFUL(0, "Authentication Successful");

    private final int code;
    private final String message;

    AuthenticationResults(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

}
