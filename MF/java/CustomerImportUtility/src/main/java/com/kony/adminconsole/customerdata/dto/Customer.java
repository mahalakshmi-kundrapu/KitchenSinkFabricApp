/**
 * 
 */
package com.kony.adminconsole.customerdata.dto;

/**
 * Customer DTO
 * 
 * @author Aditya Mankal - KH2322
 * @version 1.0
 * 
 */
public class Customer {
    private String id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String username;
    private String password;
    private String hashedPassword;
    private String salutation;
    private String gender;
    private String dateOfBirth;
    private String status;
    private String ssn;
    private String maritalStatus;
    private String spouseName;
    private String employementStatus;
    private String isOlbAllowed;
    private String isEnrolledForOlb;
    private String isStaffMember;
    private String locationCode;
    private String PreferredContactMethod;
    private String preferredContactTime;
    private String isAssistConsented;
    private String createdtimestamp;
    private String risks;
    private String primaryRegion;
    private String primaryCity;
    private String primaryAddressline1;
    private String primaryAddressline2;
    private String primaryAddressline3;
    private String primaryZipCode;
    private String primaryAddressType;
    private String secondaryRegion;
    private String secondaryCity;
    private String secondaryAddressline1;
    private String secondaryAddressline2;
    private String secondaryAddressline3;
    private String secondaryZipCode;
    private String secondaryAddressType;
    private String tertiaryRegion;
    private String tertiaryCity;
    private String tertiaryAddressline1;
    private String tertiaryAddressline2;
    private String tertiaryAddressline3;
    private String tertiaryZipCode;
    private String tertiaryAddressType;
    private String primaryEmail;
    private String primaryEmailLocType;
    private String secondaryEmail;
    private String secondaryEmailLocType;
    private String teritaryEmail;
    private String teritaryEmailLocType;
    private String primaryPhone;
    private String primaryPhoneLocType;
    private String secondaryPhone;
    private String secondaryPhoneLocType;
    private String teritaryPhone;
    private String teritaryPhoneLocType;
    private String group;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getSpouseName() {
        return spouseName;
    }

    public void setSpouseName(String spouseName) {
        this.spouseName = spouseName;
    }

    public String getEmployementStatus() {
        return employementStatus;
    }

    public void setEmployementStatus(String employementStatus) {
        this.employementStatus = employementStatus;
    }

    public String getIsOlbAllowed() {
        return isOlbAllowed;
    }

    public void setIsOlbAllowed(String isOlbAllowed) {
        this.isOlbAllowed = isOlbAllowed;
    }

    public String getIsEnrolledForOlb() {
        return isEnrolledForOlb;
    }

    public void setIsEnrolledForOlb(String isEnrolledForOlb) {
        this.isEnrolledForOlb = isEnrolledForOlb;
    }

    public String getIsStaffMember() {
        return isStaffMember;
    }

    public void setIsStaffMember(String isStaffMember) {
        this.isStaffMember = isStaffMember;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getPreferredContactMethod() {
        return PreferredContactMethod;
    }

    public void setPreferredContactMethod(String preferredContactMethod) {
        PreferredContactMethod = preferredContactMethod;
    }

    public String getPreferredContactTime() {
        return preferredContactTime;
    }

    public void setPreferredContactTime(String preferredContactTime) {
        this.preferredContactTime = preferredContactTime;
    }

    public String getIsAssistConsented() {
        return isAssistConsented;
    }

    public void setIsAssistConsented(String isAssistConsented) {
        this.isAssistConsented = isAssistConsented;
    }

    public String getCreatedtimestamp() {
        return createdtimestamp;
    }

    public void setCreatedtimestamp(String createdtimestamp) {
        this.createdtimestamp = createdtimestamp;
    }

    public String getRisks() {
        return risks;
    }

    public void setRisks(String risks) {
        this.risks = risks;
    }

    public String getPrimaryRegion() {
        return primaryRegion;
    }

    public void setPrimaryRegion(String primaryRegion) {
        this.primaryRegion = primaryRegion;
    }

    public String getPrimaryCity() {
        return primaryCity;
    }

    public void setPrimaryCity(String primaryCity) {
        this.primaryCity = primaryCity;
    }

    public String getPrimaryAddressline1() {
        return primaryAddressline1;
    }

    public void setPrimaryAddressline1(String primaryAddressline1) {
        this.primaryAddressline1 = primaryAddressline1;
    }

    public String getPrimaryAddressline2() {
        return primaryAddressline2;
    }

    public void setPrimaryAddressline2(String primaryAddressline2) {
        this.primaryAddressline2 = primaryAddressline2;
    }

    public String getPrimaryAddressline3() {
        return primaryAddressline3;
    }

    public void setPrimaryAddressline3(String primaryAddressline3) {
        this.primaryAddressline3 = primaryAddressline3;
    }

    public String getPrimaryZipCode() {
        return primaryZipCode;
    }

    public void setPrimaryZipCode(String primaryZipCode) {
        this.primaryZipCode = primaryZipCode;
    }

    public String getPrimaryAddressType() {
        return primaryAddressType;
    }

    public void setPrimaryAddressType(String primaryAddressType) {
        this.primaryAddressType = primaryAddressType;
    }

    public String getSecondaryRegion() {
        return secondaryRegion;
    }

    public void setSecondaryRegion(String secondaryRegion) {
        this.secondaryRegion = secondaryRegion;
    }

    public String getSecondaryCity() {
        return secondaryCity;
    }

    public void setSecondaryCity(String secondaryCity) {
        this.secondaryCity = secondaryCity;
    }

    public String getSecondaryAddressline1() {
        return secondaryAddressline1;
    }

    public void setSecondaryAddressline1(String secondaryAddressline1) {
        this.secondaryAddressline1 = secondaryAddressline1;
    }

    public String getSecondaryAddressline2() {
        return secondaryAddressline2;
    }

    public void setSecondaryAddressline2(String secondaryAddressline2) {
        this.secondaryAddressline2 = secondaryAddressline2;
    }

    public String getSecondaryAddressline3() {
        return secondaryAddressline3;
    }

    public void setSecondaryAddressline3(String secondaryAddressline3) {
        this.secondaryAddressline3 = secondaryAddressline3;
    }

    public String getSecondaryZipCode() {
        return secondaryZipCode;
    }

    public void setSecondaryZipCode(String secondaryZipCode) {
        this.secondaryZipCode = secondaryZipCode;
    }

    public String getSecondaryAddressType() {
        return secondaryAddressType;
    }

    public void setSecondaryAddressType(String secondaryAddressType) {
        this.secondaryAddressType = secondaryAddressType;
    }

    public String getTertiaryRegion() {
        return tertiaryRegion;
    }

    public void setTertiaryRegion(String tertiaryRegion) {
        this.tertiaryRegion = tertiaryRegion;
    }

    public String getTertiaryCity() {
        return tertiaryCity;
    }

    public void setTertiaryCity(String tertiaryCity) {
        this.tertiaryCity = tertiaryCity;
    }

    public String getTertiaryAddressline1() {
        return tertiaryAddressline1;
    }

    public void setTertiaryAddressline1(String tertiaryAddressline1) {
        this.tertiaryAddressline1 = tertiaryAddressline1;
    }

    public String getTertiaryAddressline2() {
        return tertiaryAddressline2;
    }

    public void setTertiaryAddressline2(String tertiaryAddressline2) {
        this.tertiaryAddressline2 = tertiaryAddressline2;
    }

    public String getTertiaryAddressline3() {
        return tertiaryAddressline3;
    }

    public void setTertiaryAddressline3(String tertiaryAddressline3) {
        this.tertiaryAddressline3 = tertiaryAddressline3;
    }

    public String getTertiaryZipCode() {
        return tertiaryZipCode;
    }

    public void setTertiaryZipCode(String tertiaryZipCode) {
        this.tertiaryZipCode = tertiaryZipCode;
    }

    public String getTertiaryAddressType() {
        return tertiaryAddressType;
    }

    public void setTertiaryAddressType(String tertiaryAddressType) {
        this.tertiaryAddressType = tertiaryAddressType;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public void setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    public String getPrimaryEmailLocType() {
        return primaryEmailLocType;
    }

    public void setPrimaryEmailLocType(String primaryEmailLocType) {
        this.primaryEmailLocType = primaryEmailLocType;
    }

    public String getSecondaryEmail() {
        return secondaryEmail;
    }

    public void setSecondaryEmail(String secondaryEmail) {
        this.secondaryEmail = secondaryEmail;
    }

    public String getSecondaryEmailLocType() {
        return secondaryEmailLocType;
    }

    public void setSecondaryEmailLocType(String secondaryEmailLocType) {
        this.secondaryEmailLocType = secondaryEmailLocType;
    }

    public String getTeritaryEmail() {
        return teritaryEmail;
    }

    public void setTeritaryEmail(String teritaryEmail) {
        this.teritaryEmail = teritaryEmail;
    }

    public String getTeritaryEmailLocType() {
        return teritaryEmailLocType;
    }

    public void setTeritaryEmailLocType(String teritaryEmailLocType) {
        this.teritaryEmailLocType = teritaryEmailLocType;
    }

    public String getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public String getPrimaryPhoneLocType() {
        return primaryPhoneLocType;
    }

    public void setPrimaryPhoneLocType(String primaryPhoneLocType) {
        this.primaryPhoneLocType = primaryPhoneLocType;
    }

    public String getSecondaryPhone() {
        return secondaryPhone;
    }

    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
    }

    public String getSecondaryPhoneLocType() {
        return secondaryPhoneLocType;
    }

    public void setSecondaryPhoneLocType(String secondaryPhoneLocType) {
        this.secondaryPhoneLocType = secondaryPhoneLocType;
    }

    public String getTeritaryPhone() {
        return teritaryPhone;
    }

    public void setTeritaryPhone(String teritaryPhone) {
        this.teritaryPhone = teritaryPhone;
    }

    public String getTeritaryPhoneLocType() {
        return teritaryPhoneLocType;
    }

    public void setTeritaryPhoneLocType(String teritaryPhoneLocType) {
        this.teritaryPhoneLocType = teritaryPhoneLocType;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

}
