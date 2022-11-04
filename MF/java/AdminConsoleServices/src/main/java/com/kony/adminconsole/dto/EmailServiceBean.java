package com.kony.adminconsole.dto;

import java.io.Serializable;

import org.json.JSONObject;

public class EmailServiceBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private String emailSubject;
    private String senderEmailId;
    private String recipientEmailId;
    private String cc;
    private String emailType;
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String vizServerURL;
    private String passwordUUID;
    private String claimsToken;
    private JSONObject additionalContext;

    public String getClaimsToken() {
        return claimsToken;
    }

    public void setClaimsToken(String claimsToken) {
        this.claimsToken = claimsToken;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getSenderEmailId() {
        return senderEmailId;
    }

    public void setSenderEmailId(String senderEmailId) {
        this.senderEmailId = senderEmailId;
    }

    public String getRecipientEmailId() {
        return recipientEmailId;
    }

    public void setRecipientEmailId(String recipientEmailId) {
        this.recipientEmailId = recipientEmailId;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getEmailType() {
        return emailType;
    }

    public void setEmailType(String emailType) {
        this.emailType = emailType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getVizServerURL() {
        return vizServerURL;
    }

    public void setVizServerURL(String vizServerURL) {
        this.vizServerURL = vizServerURL;
    }

    public String getPasswordUUID() {
        return passwordUUID;
    }

    public void setPasswordUUID(String passwordUUID) {
        this.passwordUUID = passwordUUID;
    }

    public JSONObject getAdditionalContext() {
        return additionalContext;
    }

    public void setAdditionalContext(JSONObject additionalContext) {
        this.additionalContext = additionalContext;
    }
}
