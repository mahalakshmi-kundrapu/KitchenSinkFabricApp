package com.kony.adminconsole.dto;

import com.kony.adminconsole.service.customermanagement.MemberCreate;

public class MemberBean {

    private String memberId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String username;
    private String primaryEmailId;
    private String primaryContactNumber;
    private String status_id = MemberCreate.DEFAULT_CUSTOMER_STATUS;
    private String preferredContactMethod = MemberCreate.DEFAULT_PREFERREDCONTACTMETHOD;
    private String preferredContactTime = MemberCreate.DEFAULT_PREFERREDCONTACTTIME;
    private String salutation;
    private String gender;
    private String dateOfBirth;
    private String ssn;
    private String maritalStatus_id = MemberCreate.DEFAULT_MARITAL_STATUS;
    private String spouseName;
    private String employementStatus_id = MemberCreate.DEFAULT_EMPLOYEMENT_STATUS;
    private Boolean isOlbAllowed = false;
    private Boolean isStaffMember = false;
    private Boolean isEnrolledForOlb = false;
    private Boolean isAssistConsented = true;
    private Boolean isCreateMemberRquired = true;

    private String currentTimestamp;
    private Boolean isCreationSuccessful = true;
    private String failureReason;

    public Boolean getIsCreateMemberRquired() {
        return isCreateMemberRquired;
    }

    public void setIsCreateMemberRquired(Boolean isCreateMemberRquired) {
        this.isCreateMemberRquired = isCreateMemberRquired;
    }

    public String getStatus_id() {
        return status_id;
    }

    public void setStatus_id(String status_id) {
        this.status_id = status_id;
    }

    public String getPreferredContactMethod() {
        return preferredContactMethod;
    }

    public void setPreferredContactMethod(String preferredContactMethod) {
        this.preferredContactMethod = preferredContactMethod;
    }

    public String getPreferredContactTime() {
        return preferredContactTime;
    }

    public void setPreferredContactTime(String preferredContactTime) {
        this.preferredContactTime = preferredContactTime;
    }

    public Boolean getIsOlbAllowed() {
        return isOlbAllowed;
    }

    public void setIsOlbAllowed(Boolean isOlbAllowed) {
        this.isOlbAllowed = isOlbAllowed;
    }

    public Boolean getIsStaffMember() {
        return isStaffMember;
    }

    public void setIsStaffMember(Boolean isStaffMember) {
        this.isStaffMember = isStaffMember;
    }

    public Boolean getIsEnrolledForOlb() {
        return isEnrolledForOlb;
    }

    public void setIsEnrolledForOlb(Boolean isEnrolledForOlb) {
        this.isEnrolledForOlb = isEnrolledForOlb;
    }

    public Boolean getIsAssistConsented() {
        return isAssistConsented;
    }

    public void setIsAssistConsented(Boolean isAssistConsented) {
        this.isAssistConsented = isAssistConsented;
    }

    public String getPrimaryEmailId() {
        return primaryEmailId;
    }

    public void setPrimaryEmailId(String primaryEmailId) {
        this.primaryEmailId = primaryEmailId;
    }

    public String getPrimaryContactNumber() {
        return primaryContactNumber;
    }

    public void setPrimaryContactNumber(String primaryContactNumber) {
        this.primaryContactNumber = primaryContactNumber;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getMaritalStatus_id() {
        return maritalStatus_id;
    }

    public void setMaritalStatus_id(String maritalStatus_id) {
        this.maritalStatus_id = maritalStatus_id;
    }

    public String getSpouseName() {
        return spouseName;
    }

    public void setSpouseName(String spouseName) {
        this.spouseName = spouseName;
    }

    public String getEmployementStatus_id() {
        return employementStatus_id;
    }

    public void setEmployementStatus_id(String employementStatus_id) {
        this.employementStatus_id = employementStatus_id;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Boolean getIsCreationSuccessful() {
        return isCreationSuccessful;
    }

    public void setIsCreationSuccessful(Boolean isCreationSuccessful) {
        this.isCreationSuccessful = isCreationSuccessful;
    }

    public String getCurrentTimestamp() {
        return currentTimestamp;
    }

    public void setCurrentTimestamp(String currentTimestamp) {
        this.currentTimestamp = currentTimestamp;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmailId() {
        return primaryEmailId;
    }

    public void setEmailId(String emailId) {
        this.primaryEmailId = emailId;
    }

    public String getContactNumber() {
        return primaryContactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.primaryContactNumber = contactNumber;
    }

}
