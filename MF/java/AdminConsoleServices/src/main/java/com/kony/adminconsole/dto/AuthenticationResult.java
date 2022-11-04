package com.kony.adminconsole.dto;

public class AuthenticationResult {

    private int code;
    private String message;
    private InternalUser userProfile;
    private boolean isAuthenticationSuccessful;

    /**
     * 
     */
    public AuthenticationResult() {
        super();
    }

    /**
     * @param code
     * @param message
     * @param userProfile
     * @param isAuthenticationSuccessful
     */
    public AuthenticationResult(int code, String message, InternalUser userProfile,
            boolean isAuthenticationSuccessful) {
        super();
        this.code = code;
        this.message = message;
        this.userProfile = userProfile;
        this.isAuthenticationSuccessful = isAuthenticationSuccessful;
    }

    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * @param code
     *            the code to set
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     *            the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the userProfile
     */
    public InternalUser getUserProfile() {
        return userProfile;
    }

    /**
     * @param userProfile
     *            the userProfile to set
     */
    public void setUserProfile(InternalUser userProfile) {
        this.userProfile = userProfile;
    }

    /**
     * @return the isAuthenticationSuccessful
     */
    public boolean isAuthenticationSuccessful() {
        return isAuthenticationSuccessful;
    }

    /**
     * @param isAuthenticationSuccessful
     *            the isAuthenticationSuccessful to set
     */
    public void setAuthenticationSuccessful(boolean isAuthenticationSuccessful) {
        this.isAuthenticationSuccessful = isAuthenticationSuccessful;
    }

}
