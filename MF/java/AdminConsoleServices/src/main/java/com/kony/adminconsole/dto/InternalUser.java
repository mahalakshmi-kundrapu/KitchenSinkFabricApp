package com.kony.adminconsole.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kony.adminconsole.commons.utils.FabricConstants;

/**
 * DTO of Internal User
 * 
 * @author Aditya Mankal
 *
 */
@JsonInclude(value = Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InternalUser {

    @JsonProperty("id")
    private String id;

    @JsonProperty("Username")
    private String username;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("Status_id")
    private String status;

    @JsonProperty("Password")
    private String hashedPassword;

    @JsonProperty("Code")
    private String code;

    @JsonProperty("FirstName")
    private String firstName;

    @JsonProperty("MiddleName")
    private String middleName;

    @JsonProperty("LastName")
    private String lastName;

    @JsonProperty("FailedCount")
    private int failedLoginAttemptCount;

    @JsonProperty("LastPasswordChangedts")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FabricConstants.DATABASE_DATE_FORMAT)
    private Date lastPasswordChangedTime;

    @JsonProperty("ResetpasswordLink")
    private String passwordResetLink;

    @JsonProperty("ResetPasswordExpdts")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FabricConstants.DATABASE_DATE_FORMAT)
    private Date passwordResetLinkExpiryTime;

    @JsonProperty("lastLogints")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FabricConstants.DATABASE_DATE_FORMAT)
    private Date lastLoginTime;

    @JsonProperty("Role_id")
    private String roleId;

    @JsonProperty("Role_Name")
    private String roleName;

    @JsonProperty("Role_Status_id")
    private String roleStatusId;

    @JsonProperty("hasSuperAdminPrivilages")
    private boolean hasSuperAdminPrivilages;

    @JsonProperty("createdby")
    private String createdby;

    @JsonProperty("modifiedby")
    private String modifiedby;

    @JsonProperty("createdts")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FabricConstants.DATABASE_DATE_FORMAT)
    private Date createdTime;

    @JsonProperty("lastmodifiedts")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FabricConstants.DATABASE_DATE_FORMAT)
    private Date lastModifiedTime;

    @JsonProperty("synctimestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FabricConstants.DATABASE_DATE_FORMAT)
    private Date syncTime;

    @JsonProperty("softdeleteflag")
    private boolean softDeleteFlag;

    /**
     * 
     */
    public InternalUser() {
        super();
    }

    /**
     * @param id
     * @param username
     * @param email
     * @param status
     * @param hashedPassword
     * @param code
     * @param firstName
     * @param middleName
     * @param lastName
     * @param failedLoginAttemptCount
     * @param lastPasswordChangedTime
     * @param passwordResetLink
     * @param passwordResetLinkExpiryTime
     * @param lastLoginTime
     * @param roleId
     * @param roleName
     * @param roleStatusId
     * @param hasSuperAdminPrivilages
     * @param createdby
     * @param modifiedby
     * @param createdTime
     * @param lastModifiedTime
     * @param syncTime
     * @param softDeleteFlag
     */
    public InternalUser(String id, String username, String email, String status, String hashedPassword, String code,
            String firstName, String middleName, String lastName, int failedLoginAttemptCount,
            Date lastPasswordChangedTime, String passwordResetLink, Date passwordResetLinkExpiryTime,
            Date lastLoginTime, String roleId, String roleName, String roleStatusId, boolean hasSuperAdminPrivilages,
            String createdby, String modifiedby, Date createdTime, Date lastModifiedTime, Date syncTime,
            boolean softDeleteFlag) {
        super();
        this.id = id;
        this.username = username;
        this.email = email;
        this.status = status;
        this.hashedPassword = hashedPassword;
        this.code = code;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.failedLoginAttemptCount = failedLoginAttemptCount;
        this.lastPasswordChangedTime = lastPasswordChangedTime;
        this.passwordResetLink = passwordResetLink;
        this.passwordResetLinkExpiryTime = passwordResetLinkExpiryTime;
        this.lastLoginTime = lastLoginTime;
        this.roleId = roleId;
        this.roleName = roleName;
        this.roleStatusId = roleStatusId;
        this.hasSuperAdminPrivilages = hasSuperAdminPrivilages;
        this.createdby = createdby;
        this.modifiedby = modifiedby;
        this.createdTime = createdTime;
        this.lastModifiedTime = lastModifiedTime;
        this.syncTime = syncTime;
        this.softDeleteFlag = softDeleteFlag;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     *            the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email
     *            the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the hashedPassword
     */
    public String getHashedPassword() {
        return hashedPassword;
    }

    /**
     * @param hashedPassword
     *            the hashedPassword to set
     */
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code
     *            the code to set
     */
    public void setCode(String code) {
        this.code = code;
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
     * @return the failedLoginAttemptCount
     */
    public int getFailedLoginAttemptCount() {
        return failedLoginAttemptCount;
    }

    /**
     * @param failedLoginAttemptCount
     *            the failedLoginAttemptCount to set
     */
    public void setFailedLoginAttemptCount(int failedLoginAttemptCount) {
        this.failedLoginAttemptCount = failedLoginAttemptCount;
    }

    /**
     * @return the lastPasswordChangedTime
     */
    public Date getLastPasswordChangedTime() {
        return lastPasswordChangedTime;
    }

    /**
     * @param lastPasswordChangedTime
     *            the lastPasswordChangedTime to set
     */
    public void setLastPasswordChangedTime(Date lastPasswordChangedTime) {
        this.lastPasswordChangedTime = lastPasswordChangedTime;
    }

    /**
     * @return the passwordResetLink
     */
    public String getPasswordResetLink() {
        return passwordResetLink;
    }

    /**
     * @param passwordResetLink
     *            the passwordResetLink to set
     */
    public void setPasswordResetLink(String passwordResetLink) {
        this.passwordResetLink = passwordResetLink;
    }

    /**
     * @return the passwordResetLinkExpiryTime
     */
    public Date getPasswordResetLinkExpiryTime() {
        return passwordResetLinkExpiryTime;
    }

    /**
     * @param passwordResetLinkExpiryTime
     *            the passwordResetLinkExpiryTime to set
     */
    public void setPasswordResetLinkExpiryTime(Date passwordResetLinkExpiryTime) {
        this.passwordResetLinkExpiryTime = passwordResetLinkExpiryTime;
    }

    /**
     * @return the lastLoginTime
     */
    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    /**
     * @param lastLoginTime
     *            the lastLoginTime to set
     */
    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
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
     * @return the roleStatusId
     */
    public String getRoleStatusId() {
        return roleStatusId;
    }

    /**
     * @param roleStatusId
     *            the roleStatusId to set
     */
    public void setRoleStatusId(String roleStatusId) {
        this.roleStatusId = roleStatusId;
    }

    /**
     * @return the hasSuperAdminPrivilages
     */
    public boolean hasSuperAdminPrivilages() {
        return hasSuperAdminPrivilages;
    }

    /**
     * @param hasSuperAdminPrivilages
     *            the hasSuperAdminPrivilages to set
     */
    public void setHasSuperAdminPrivilages(boolean hasSuperAdminPrivilages) {
        this.hasSuperAdminPrivilages = hasSuperAdminPrivilages;
    }

    /**
     * @return the createdby
     */
    public String getCreatedby() {
        return createdby;
    }

    /**
     * @param createdby
     *            the createdby to set
     */
    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    /**
     * @return the modifiedby
     */
    public String getModifiedby() {
        return modifiedby;
    }

    /**
     * @param modifiedby
     *            the modifiedby to set
     */
    public void setModifiedby(String modifiedby) {
        this.modifiedby = modifiedby;
    }

    /**
     * @return the createdTime
     */
    public Date getCreatedTime() {
        return createdTime;
    }

    /**
     * @param createdTime
     *            the createdTime to set
     */
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * @return the lastModifiedTime
     */
    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * @param lastModifiedTime
     *            the lastModifiedTime to set
     */
    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * @return the syncTime
     */
    public Date getSyncTime() {
        return syncTime;
    }

    /**
     * @param syncTime
     *            the syncTime to set
     */
    public void setSyncTime(Date syncTime) {
        this.syncTime = syncTime;
    }

    /**
     * @return the softDeleteFlag
     */
    public boolean isSoftDeleteFlag() {
        return softDeleteFlag;
    }

    /**
     * @param softDeleteFlag
     *            the softDeleteFlag to set
     */
    public void setSoftDeleteFlag(boolean softDeleteFlag) {
        this.softDeleteFlag = softDeleteFlag;
    }

}
