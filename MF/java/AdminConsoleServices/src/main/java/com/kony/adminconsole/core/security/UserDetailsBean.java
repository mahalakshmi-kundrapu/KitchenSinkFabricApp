package com.kony.adminconsole.core.security;

import java.util.List;

/**
 * Bean used to hold user details and auth data of current logged in user to admin portal
 * 
 * @author Venkateswara Rao Alla
 *
 */

public class UserDetailsBean {

    private String userId;
    private String userName;
    private String emailId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String roleName;
    private String roleId;
    private boolean isAPIUser;
    private List<String> authData;

    /**
     * @param userId
     * @param userName
     * @param emailId
     * @param firstName
     * @param middleName
     * @param lastName
     * @param roleName
     * @param roleId
     * @param isAPIUser
     * @param authData
     */
    public UserDetailsBean(String userId, String userName, String emailId, String firstName, String middleName,
            String lastName, String roleName, String roleId, boolean isAPIUser, List<String> authData) {
        super();
        this.userId = userId;
        this.userName = userName;
        this.emailId = emailId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.roleName = roleName;
        this.roleId = roleId;
        this.isAPIUser = isAPIUser;
        this.authData = authData;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName
     *            the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the emailId
     */
    public String getEmailId() {
        return emailId;
    }

    /**
     * @param emailId
     *            the emailId to set
     */
    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName
     *            the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the middleName
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * @param middleName
     *            the middleName to set
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName
     *            the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the roleName
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * @param roleName
     *            the roleName to set
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * @return the roleId
     */
    public String getRoleId() {
        return roleId;
    }

    /**
     * @param roleId
     *            the roleId to set
     */
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    /**
     * @return the isAPIUser
     */
    public boolean isAPIUser() {
        return isAPIUser;
    }

    /**
     * @param isAPIUser
     *            the isAPIUser to set
     */
    public void setAPIUser(boolean isAPIUser) {
        this.isAPIUser = isAPIUser;
    }

    /**
     * @return the authData
     */
    public List<String> getAuthData() {
        return authData;
    }

    /**
     * @param authData
     *            the authData to set
     */
    public void setAuthData(List<String> authData) {
        this.authData = authData;
    }

}
