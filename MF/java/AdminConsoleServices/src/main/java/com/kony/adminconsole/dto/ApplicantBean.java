package com.kony.adminconsole.dto;

public class ApplicantBean {

    private String id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String product_id;
    private String status_id;
    private String channel_id;
    private String documentsSubmitted;
    private String contactNumber;
    private String emailId;
    private String createdby;
    private String modifiedby;
    private String createdts;
    private String lastmodifiedts;
    private String synctimestamp;
    private String softdeleteflag;

    public ApplicantBean() {

        this.id = "";
        this.firstName = "";
        this.middleName = "";
        this.lastName = "";
        this.product_id = "";
        this.status_id = "";
        this.channel_id = "";
        this.documentsSubmitted = "";
        this.contactNumber = "";
        this.emailId = "";
        this.createdby = "NULL";
        this.modifiedby = "";
        this.createdts = "";
        this.lastmodifiedts = "";
        this.synctimestamp = "";
        this.softdeleteflag = "0";
    }

    public ApplicantBean(String id, String firstName, String middleName, String lastName, String product_id,
            String status_id, String channel_id, String documentsSubmitted, String contactNumber, String emailId,
            String createdby, String modifiedby, String createdts, String lastmodifiedts, String synctimestamp,
            String softdeleteflag) {
        super();
        this.id = id;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.product_id = product_id;
        this.status_id = status_id;
        this.channel_id = channel_id;
        this.documentsSubmitted = documentsSubmitted;
        this.contactNumber = contactNumber;
        this.emailId = emailId;
        this.createdby = createdby;
        this.modifiedby = modifiedby;
        this.createdts = createdts;
        this.lastmodifiedts = lastmodifiedts;
        this.synctimestamp = synctimestamp;
        this.softdeleteflag = softdeleteflag;
    }

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

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getStatus_id() {
        return status_id;
    }

    public void setStatus_id(String status_id) {
        this.status_id = status_id;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getDocumentsSubmitted() {
        return documentsSubmitted;
    }

    public void setDocumentsSubmitted(String documentsSubmitted) {
        this.documentsSubmitted = documentsSubmitted;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public String getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(String modifiedby) {
        this.modifiedby = modifiedby;
    }

    public String getCreatedts() {
        return createdts;
    }

    public void setCreatedts(String createdts) {
        this.createdts = createdts;
    }

    public String getLastmodifiedts() {
        return lastmodifiedts;
    }

    public void setLastmodifiedts(String lastmodifiedts) {
        this.lastmodifiedts = lastmodifiedts;
    }

    public String getSynctimestamp() {
        return synctimestamp;
    }

    public void setSynctimestamp(String synctimestamp) {
        this.synctimestamp = synctimestamp;
    }

    public String getSoftdeleteflag() {
        return softdeleteflag;
    }

    public void setSoftdeleteflag(String softdeleteflag) {
        this.softdeleteflag = softdeleteflag;
    }

    @Override
    public String toString() {
        return "ApplicantBean [id=" + id + ", firstName=" + firstName + ", middleName=" + middleName + ", lastName="
                + lastName + ", product_id=" + product_id + ", status_id=" + status_id + ", channel_id=" + channel_id
                + ", documentsSubmitted=" + documentsSubmitted + ", contactNumber=" + contactNumber + ", emailId="
                + emailId + ", createdby=" + createdby + ", modifiedby=" + modifiedby + ", createdts=" + createdts
                + ", lastmodifiedts=" + lastmodifiedts + ", synctimestamp=" + synctimestamp + ", softdeleteflag="
                + softdeleteflag + "]";
    }

}
